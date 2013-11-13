//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.omGraphics.OMWarpingImage;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ClasspathHacker;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * The StandardImageTileFactory is a TileFactory implementation that retrieves
 * image tiles from local storage. These tiles are assumed to be stored in the
 * local file system, at some root directory, and then in some hierarchy like
 * zoom-level/x coord/y coord.file-extension. This class can be extended to
 * allow different tile naming/storing conventions to be used.
 * <p>
 * 
 * This component can be configured using properties:
 * <p>
 * 
 * <pre>
 * rootDir=the path to the parent directory of the tiles. The factory will construct specific file paths that are appended to this value.
 * fileExt=the file extension to append to the tile names
 * cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * # default is OSMMapTileCoordinateTransform, but it depends on the source of tiles.  GDAL is TSMMapTileCoordinateTransform
 * mapTileTransform=com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform, or com.bbn.openmap.dataAccess.mapTile.TSMMapTileCoordinateTransform
 * # what to do about missing tiles?
 * emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.SimpleEmptyTileHandler
 * # Set a tile image preparer, if you want to change how images are rendered (greyscale, for instance)
 * tileImagePreparer=com.bbn.openmap.dataAccess.mapTile.StandardImagePreparer
 * # or
 * tileImagePreparer=com.bbn.openmap.dataAccess.mapTile.GreyscaleImagePreparer
 * </pre>
 * 
 * @author dietrick
 */
public class StandardMapTileFactory extends CacheHandler implements MapTileFactory,
        PropertyConsumer {
    protected String prefix = null;
    protected final static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory");
    protected final static Logger mapTileLogger = Logger.getLogger("MAPTILE_DEBUGGING");
    public final static String ROOT_DIR_PROPERTY = "rootDir";
    public final static String FILE_EXT_PROPERTY = "fileExt";
    public final static String CACHE_SIZE_PROPERTY = "cacheSize";
    public final static String MTCTRANSFORM_PROPERTY = "mapTileTransform";
    public final static String EMPTY_TILE_HANDLER_PROPERTY = "emptyTileHandler";
    public final static String ZOOM_LEVEL_INFO_PROPERTY = "zoomLevelInfo";
    public final static String ZOOM_LEVEL_TILE_SIZE_PROPERTY = "zoomLevelTileSize";
    public final static String TILE_IMAGE_PREPARER_PROPERTY = "tileImagePreparer";
    /**
     * Inserted into properties loaded via tiles.omp, so that the
     * EmptyTileHandler can know where the tile set is located, in case it needs
     * to know the absolute path. Will contain the root directory path specified
     * in the factory properties, as opposed to any rootDir property set in the
     * tiles.omp file that would specify a relative root directory path.
     */
    public final static String ROOT_DIR_PATH_PROPERTY = "rootDirPath";
    /**
     * The name of the properties file that the factory looks for in the root
     * directory of the data (tiles.omp).
     */
    public final static String TILE_PROPERTIES = "tiles.omp";
    protected ZoomLevelInfo zoomLevelInfo = new ZoomLevelInfo();
    protected String rootDir;
    protected String fileExt = ".png";
    protected String rootDirProperty; // For writing out later, if necessary
    protected EmptyTileHandler emptyTileHandler = null;
    protected boolean verbose = false;
    protected int zoomLevelTileSize = 350;
    protected TileImagePreparer tileImagePreparer;

    /**
     * If set, the MapTileRequester will be notified when the list provided in
     * getTiles() has been updated, and asked if it should continue with the
     * getTiles() request at opportune times, when tile fetching is stable.
     */
    protected MapTileRequester mapTileRequester;
    /**
     * Flag to tell the factory to create the extra tiles off-map. Tends to
     * cause the layer to do more work than necessary, so it's not used.
     */
    private boolean doExtraTiles = false;
    /**
     * Coordinate transform for the uv coordinates of the tiles. Different
     * sources have different origins for tile coordinates.
     */
    protected MapTileCoordinateTransform mtcTransform = new OSMMapTileCoordinateTransform();

    public StandardMapTileFactory() {
        super(100);
        verbose = logger.isLoggable(Level.FINE);
    }

    public StandardMapTileFactory(MapTileRequester layer, String rootDir, String tileFileExt) {
        super(100);
        setRootDir(rootDir);
        setFileExt(tileFileExt);
        verbose = logger.isLoggable(Level.FINE);
        this.mapTileRequester = layer;
    }

    @Override
    public CacheObject load(Object key) {
        return null;
    }

    /**
     * Tell the factory to clean up resources.
     */
    public void reset() {
        clear();
    }

    /**
     * Called to load cache object from data source, when not found in cache.
     * 
     * @param key cache key
     * @param x uv x coordinate
     * @param y uv y coordinate
     * @param zoomLevel zoom level for tile to load
     * @param proj passed solely to enable checking if the projection of the
     *        tiles matches the rendered projection.
     * @return CacheObject returned from cache, null if not found
     */
    public CacheObject load(Object key, int x, int y, int zoomLevel, Projection proj) {
        if (key instanceof String) {
            String imagePath = (String) key;
            if (verbose) {
                logger.fine("fetching file for cache: " + imagePath);
            }

            try {
                URL imageURL = PropUtils.getResourceOrFileOrURL(imagePath);
                if (imageURL != null) {

                    BufferedImage bi = BufferedImageHelper.getBufferedImage(imageURL);
                    OMGraphic raster = createOMGraphicFromBufferedImage(bi, x, y, zoomLevel, proj);

                    if (raster != null) {
                        return new CacheObject(imagePath, raster);
                    }

                } else {
                    logger.fine("Can't find resource located at " + imagePath);
                }
            } catch (MalformedURLException e) {
                logger.fine("Can't find resource located at " + imagePath);
            } catch (InterruptedException e) {
                logger.fine("Reading the image file was interrupted: " + imagePath);
            }
        }
        return null;
    }

    /**
     * Creates an OMRaster appropriate for projection and other parameters from
     * a buffered image.
     * 
     * @param bi BufferedImage to use for tile.
     * @param x x uv coordinate for tile.
     * @param y y uv coordinate for tile.
     * @param zoomLevel zoom level for tile.
     * @param proj the current map projection
     * @return OMGraphic (OMScalingRaster or OMWarpingImage, most likely)
     * @throws InterruptedException
     */
    protected OMGraphic createOMGraphicFromBufferedImage(BufferedImage bi, int x, int y,
                                                         int zoomLevel, Projection proj)
            throws InterruptedException {

        OMGraphic raster = null;

        if (bi != null) {
            BufferedImage rasterImage = preprocessImage(bi, bi.getWidth(), bi.getHeight());

            if (proj instanceof Mercator) {
                raster = getTileMatchingProjectionType(rasterImage, x, y, zoomLevel);
            } else {
                raster = getTileNotMatchingProjectionType(rasterImage, x, y, zoomLevel);
            }

            if (mapTileLogger.isLoggable(Level.FINE)) {
                raster.putAttribute(OMGraphic.LABEL, new OMTextLabeler("Tile: " + zoomLevel + "|"
                        + x + "|" + y, OMText.JUSTIFY_CENTER));
                raster.setSelected(true);
            }
        }

        return raster;
    }

    /**
     * Create an OMScalingRaster that matches the basic projection of the
     * current map. Only scales evenly for the opposite corner points.
     * 
     * @param image BufferedImage created from tile file
     * @param x uv x coordinate
     * @param y uv y coordinate
     * @param zoomLevel zoom level for tile retrieval
     * @return OMGraphic, but really an OMScalingRaster.
     */
    protected OMGraphic getTileMatchingProjectionType(BufferedImage image, int x, int y,
                                                      int zoomLevel) {

        Point2D pnt = new Point2D.Double();
        pnt.setLocation(x, y);
        Point2D tileUL = mtcTransform.tileUVToLatLon(pnt, zoomLevel);
        pnt.setLocation(x + 1, y + 1);
        Point2D tileLR = mtcTransform.tileUVToLatLon(pnt, zoomLevel);
        if (verbose) {
            logger.fine("tile coords: " + tileUL + ", " + tileLR);
        }

        double x1 = Math.min(tileUL.getX(), tileLR.getX());
        double x2 = Math.max(tileUL.getX(), tileLR.getX());
        double y1 = Math.min(tileUL.getY(), tileLR.getY());
        double y2 = Math.max(tileUL.getY(), tileLR.getY());

        return new OMScalingRaster(y2, x1, y1, x2, image);
    }

    /**
     * Create an OMWarpingImage that knows how to re-project itself for
     * different projections. The base projection is going to be defined for the
     * mtc transform set on the factory. Warping images are slower to generate
     * for a map projection than scaling rasters.
     * 
     * @param image
     * @param x
     * @param y
     * @param zoomLevel
     * @return OMGraphic, but really an OMWarpingImage
     */
    protected OMGraphic getTileNotMatchingProjectionType(BufferedImage image, int x, int y,
                                                         int zoomLevel) {

        DataBounds dataBounds = new DataBounds(new Point(x, y), new Point(x + 1, y + 1));
        dataBounds.setyDirUp(mtcTransform.isYDirectionUp());

        return new OMWarpingImage(image, mtcTransform.getTransform(zoomLevel), dataBounds);
    }

    /**
     * Method that allows subclasses to modify the image as necessary before it
     * is passed into an OMGraphic.
     * 
     * @param origImage Any java Image
     * @param imageWidth pixel width
     * @param imageHeight pixel height
     * @return BufferedImage with any changes necessary.
     * @throws InterruptedException
     */
    protected BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
            throws InterruptedException {

        return getTileImagePreparer().preprocessImage(origImage, imageWidth, imageHeight);
    }

    /**
     * The main call to retrieve something from the cache, modified to allow
     * load method to do some projection calculations to initialize tile
     * parameters. If the object is not found in the cache, then load is called
     * to get it from the data source.
     * 
     * @param key cache key, usually string of location of a tile
     * @param x uv x location of tile
     * @param y uv y location of tile
     * @param zoomLevel zoom level of tile
     * @param proj passed solely to enable checking if the projection of the
     *        tiles matches the rendered projection.
     * @return object from cache.
     */
    public Object get(Object key, int x, int y, int zoomLevel, Projection proj) {
        CacheObject ret = searchCache(key);
        if (ret != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("found tile (" + x + ", " + y + ") in cache");
            }
            return ret.obj;
        }

        ret = load(key, x, y, zoomLevel, proj);
        if (ret == null) {
            return null;
        }

        replaceLeastUsed(ret);
        return ret.obj;
    }

    /**
     * An auxiliary call to retrieve something from the cache, modified to allow
     * load method to do some projection calculations to initialize tile
     * parameters. If the object is not found in the cache, null is returned.
     * 
     * @param key cache key, usually string of location of a tile
     * @param x uv x location of tile
     * @param y uv y location of tile
     * @param zoomLevel zoom level of tile
     * @return cache object if found, null if not.
     */
    public Object getFromCache(Object key, int x, int y, int zoomLevel) {
        CacheObject ret = searchCache(key);
        if (ret != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("found tile (" + x + ", " + y + ") in cache");
            }
            return ret.obj;
        }

        return null;
    }

    /**
     * Call to make when you want the tile factory to create some empty tile
     * representation for the given location. You can return any type of
     * OMGraphic embedded in a CacheObject.
     * 
     * @param key the cache key for this object
     * @param x the uv x coordinate of the tile
     * @param y the uv y coordinate of the tile
     * @param zoomLevel the zoom level for the tile
     * @param proj the projection being used for the map.
     * @return CacheObject, or null if the empty tile should be blank.
     */
    public CacheObject getEmptyTile(Object key, int x, int y, int zoomLevel, Projection proj) {

        getTileImagePreparer().prepareForEmptyTile(this);

        EmptyTileHandler empTileHandler = getEmptyTileHandler();
        if (empTileHandler != null) {

            BufferedImage bi = empTileHandler.getImageForEmptyTile((String) key, x, y, zoomLevel, mtcTransform, proj);

            OMGraphic raster;
            try {
                raster = createOMGraphicFromBufferedImage(bi, x, y, zoomLevel, proj);
                if (raster != null) {

                    if (mapTileLogger.isLoggable(Level.FINE)) {
                        Object labelObj = raster.getAttribute(OMGraphic.LABEL);
                        if (labelObj instanceof OMTextLabeler) {
                            OMTextLabeler label = (OMTextLabeler) labelObj;
                            label.setData("EMPTY " + label.getData());
                        }
                    }

                    return new CacheObject(key, raster);
                }

            } catch (InterruptedException e) {
                if (logger.isLoggable(Level.FINE)) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Returns projected tiles for the given projection.
     * 
     * @param proj the projection to fetch tiles for.
     * @return OMGraphicList containing projected OMGraphics.
     * @throws InterruptedException
     */
    public OMGraphicList getTiles(Projection proj) {
        return getTiles(proj, -1, new OMGraphicList());
    }

    /**
     * Returns projected tiles for given projection at specified zoom level.
     * 
     * @param proj projection for query
     * @param zoomLevel zoom level 1-20 for tiles to be returned, -1 for code to
     *        figure out appropriate zoom level.
     * @return OMGraphicList with tiles.
     * @throws InterruptedException
     */
    public OMGraphicList getTiles(Projection proj, int zoomLevel) {
        return getTiles(proj, zoomLevel, new OMGraphicList());
    }

    protected Projection lastProj;

    /**
     * Returns projected tiles for given projection at specified zoom level. Use
     * this call if you are providing a repaint callback component to the
     * factory, so you will have a handle on the OMGraphicList to render to.
     * 
     * @param proj projection for query
     * @param zoomLevel zoom level 1-20 for tiles to be returned, -1 for code to
     *        figure out appropriate zoom level.
     * @param list OMGraphicList that is returned, that will also have tiles
     *        added to it.
     * @return OMGraphicList with tiles.
     * @throws InterruptedException
     */
    public OMGraphicList getTiles(Projection proj, int zoomLevel, OMGraphicList list) {

        if (fileExt == null || rootDir == null) {
            logger.warning("No path to tile files provided (" + rootDir + "), or file extension ("
                    + fileExt + ") not specified");
            return list;
        }

        if (lastProj == null || !proj.getClass().isAssignableFrom(lastProj.getClass())) {
            logger.fine("Clearing out cache for new projection type");
            clear(); // empty the cache to rebuild OMGraphics for different type
            // projection.
        }

        lastProj = proj;

        /**
         * Given a projection, a couple of things have to happen.
         * 
         * - First, we need to figure out what zoom level fits us best if it is
         * not specified.
         * 
         * - Second, we need to figure out the uv bounds that fit the
         * projection.
         * 
         * - Third, we need to grab the images for uv grid, by cycling through
         * the limits in both directions.
         * 
         * The TileMaker static methods let us convert uv to lat/lon and back, a
         * ZoomLevelInfo object can be used to figure out what the file path
         * looks like.
         */
        if (zoomLevel < 0) {
            zoomLevel = getZoomLevelForProj(proj);
            if (verbose) {
                logger.fine("Best zoom level calculated at: " + zoomLevel);
            }
        }

        if (zoomLevel >= 0) {

            if (zoomLevel == 0) {
                zoomLevel++;
            }

            zoomLevelInfo.setZoomLevel(zoomLevel);

            Point2D upperLeft = proj.getUpperLeft();
            Point2D lowerRight = proj.getLowerRight();

            int[] uvBounds = mtcTransform.getTileBoundsForProjection(upperLeft, lowerRight, zoomLevel);
            int uvup = uvBounds[0];
            int uvleft = uvBounds[1];
            int uvbottom = uvBounds[2];
            int uvright = uvBounds[3];

            if (verbose) {
                logger.fine("for " + proj + ", fetching tiles between x(" + uvleft + ", " + uvright
                        + ") y(" + uvup + ", " + uvbottom + ")");
            }

            // dateline test
            Point2D datelinePnt = proj.forward(new LatLonPoint.Double(upperLeft.getY(), 180d));
            double dlx = datelinePnt.getX();
            boolean dateline = dlx > 0 & dlx < proj.getWidth();
            logger.fine("Long(180) located at " + dlx);

            if (!dateline) {
                getTiles(uvleft, uvright, uvup, uvbottom, zoomLevelInfo, proj, list);
            } else {
                logger.fine("handling DATELINE");
                getTiles(uvleft, (int) Math.pow(2, zoomLevel), uvup, uvbottom, zoomLevelInfo, proj, list);
                getTiles(0, uvright, uvup, uvbottom, zoomLevelInfo, proj, list);
            }

        }
        return list;
    }

    /**
     * A temporary object used to store information about map tiles that are not
     * found in the cache. The caching mechanism has been modified to search for
     * cached tiles first, and using this object to hold information about map
     * tiles that need to be loaded. The cached tiles will be immediately
     * displayed, and then these tiles will be displayed after that as they are
     * loaded.
     * 
     * @author dietrick
     */
    class LoadObj {
        String imagePath;
        int x;
        int y;
        int zoomLevel;

        LoadObj(String p, int x, int y, int z) {
            this.imagePath = p;
            this.x = x;
            this.y = y;
            this.zoomLevel = z;
        }
    }

    protected void getTiles(int uvleft, int uvright, int uvup, int uvbottom,
                            ZoomLevelInfo zoomLevelInfo, Projection proj, OMGraphicList list) {
        if (verbose) {
            logger.fine("for zoom level: " + zoomLevelInfo.getZoomLevel()
                    + ", screen covers uv coords [t:" + uvup + ", l:" + uvleft + ", b:" + uvbottom
                    + ", r:" + uvright + "]");
        }

        if (zoomLevelInfo.getZoomLevel() == 0) {
            logger.fine("got one tile, OM can't draw a single tile covering the earth. Sorry.");
        }

        List<LoadObj> reloads = new ArrayList<LoadObj>();
        int zoomLevel = zoomLevelInfo.getZoomLevel();

        boolean isMercator = proj instanceof Mercator;

        int uvleftM = (int) Math.min(uvleft, uvright);
        int uvrightM = (int) Math.max(uvleft, uvright);
        int uvupM = (int) Math.min(uvbottom, uvup);
        int uvbottomM = (int) Math.max(uvbottom, uvup);

        for (int x = uvleftM; x < uvrightM; x++) {
            for (int y = uvupM; y < uvbottomM; y++) {

                if (mapTileRequester != null && !mapTileRequester.shouldContinue()) {
                    return;
                }

                String imagePath = buildCacheKey(x, y, zoomLevel, fileExt);

                /**
                 * Need to modify the action of the cache a little to make the
                 * map appear more responsive. So, we cycle through the desired
                 * tiles, gathering all of the tiles that are immediately
                 * available. Generate them, add them to list, and call repaint
                 * when they are set.
                 * 
                 * Keep track of the ones that are not there, and load those
                 * one-by-one after, calling repaint as they are added to the
                 * list.
                 */
                OMGraphic tileGraphic = (OMGraphic) getFromCache(imagePath, x, y, zoomLevel);

                boolean rightOMGraphicType = (tileGraphic instanceof OMScalingRaster && isMercator)
                        || (tileGraphic instanceof OMWarpingImage && !isMercator);

                if (tileGraphic != null/* && rightOMGraphicType */) {

                    if (mapTileLogger.isLoggable(Level.FINE)) {
                        tileGraphic.putAttribute(OMGraphic.LABEL, new OMTextLabeler("Tile: "
                                + zoomLevel + "|" + x + "|" + y, OMText.JUSTIFY_CENTER));
                        tileGraphic.setSelected(true);
                    }

                    tileGraphic.generate(proj);
                    list.add(tileGraphic);
                } else {
                    reloads.add(new LoadObj(imagePath, x, y, zoomLevel));
                }
            }
        }

        if (verbose) {
            logger.fine("found " + list.size() + " frames in cache, loading " + reloads.size()
                    + " others now...");
        }

        if (mapTileRequester != null) {
            mapTileRequester.listUpdated();
        }

        /*
         * Load the tiles that are not already in the cache, that need to be
         * fetched from the source.
         */
        for (LoadObj reload : reloads) {
            // Check and see of we should bother fetching the new tile.
            if (mapTileRequester != null && !mapTileRequester.shouldContinue()) {
                return;
            }

            loadTile(reload.imagePath, reload.x, reload.y, reload.zoomLevel, proj, list);

            // OK, got it, notify requester the list has been updated.
            if (mapTileRequester != null) {
                mapTileRequester.listUpdated();
            }
        }

        if (verbose) {
            logger.fine("finished loading " + reloads.size() + " frames from source for screen"
                    + (doExtraTiles ? ", moving to off-screen frames..." : ""));
        }

        if (!doExtraTiles) {
            return;
        }

        // Just for giggles, lets go ahead and walk around the edge of the area
        // and prefetch tiles to load them into memory...

        // int uvleft, int uvright, int uvup, int uvbottom
        int x1 = uvleft;
        int y1 = uvup;
        int x2 = uvright;
        int y2 = uvbottom;
        boolean top = false;
        boolean left = false;
        boolean right = false;
        boolean bottom = false;
        if (x1 > 0) {
            x1--;
            left = true;
        }
        if (y1 > 0) {
            y1--;
            top = true;
        }
        int edgeTileCount = zoomLevelInfo.getEdgeTileCount();
        if (x2 < edgeTileCount - 1) {
            x2++;
            right = true;
        }
        if (y2 < edgeTileCount - 1) {
            y2++;
            bottom = true;
        }

        // Get the corners
        if (top && left) {
            loadTile(x1, y1, zoomLevel, proj, list);
        }
        if (bottom && left) {
            loadTile(x1, y2, zoomLevel, proj, list);
        }
        if (bottom && right) {
            loadTile(x2, y2, zoomLevel, proj, list);
        }
        if (top && right) {
            loadTile(x2, y1, zoomLevel, proj, list);
        }
        // Now go along the sides
        if (top) {
            for (int x = uvleft; x < uvright; x++) {
                loadTile(x, y1, zoomLevel, proj, list);
            }
        }

        if (bottom) {
            for (int x = uvleft; x < uvright; x++) {
                loadTile(x, y2, zoomLevel, proj, list);
            }
        }

        if (right) {
            for (int y = uvup; y < uvbottom; y++) {
                loadTile(x2, y, zoomLevel, proj, list);
            }
        }

        if (left) {
            for (int y = uvup; y < uvbottom; y++) {
                loadTile(x1, y, zoomLevel, proj, list);
            }
        }

        if (verbose) {
            logger.fine("finished loading all tiles (" + list.size() + ")");
        }
    }

    /**
     * Handles going to the cache, getting the cache to load the tile, and then
     * manage the resulting OMRaster tile. Adds the tile to the list after
     * generating it with the projection, and calls the repaintCallback if there
     * is one.
     * 
     * @param imagePath the image path for the tile
     * @param x the x uv coordinate of the tile
     * @param y the y uv coordinate of the tile
     * @param zoomLevel the zoomLevel of the tile
     * @param proj the current projection.
     * @param list the OMGraphicList to add the tile to.
     * @throws InterruptedException
     */
    private void loadTile(String imagePath, int x, int y, int zoomLevel, Projection proj,
                          OMGraphicList list) {

        CacheObject ret = load(imagePath, x, y, zoomLevel, proj);
        if (ret == null) {

            // Check if the factory wants to do anything for empty tiles.
            ret = getEmptyTile(imagePath, x, y, zoomLevel, proj);
        }

        if (ret != null) {
            replaceLeastUsed(ret);
            OMGraphic raster = (OMGraphic) ret.obj;

            if (raster != null) {

                raster.generate(proj);
                list.add(raster);

                if (logger.isLoggable(Level.FINE)) {
                    raster.putAttribute(OMGraphic.TOOLTIP, imagePath);
                }
            }
        }
    }

    /**
     * Handles going to the cache, getting the cache to load the tile, and then
     * manage the resulting OMRaster tile. Adds the tile to the list after
     * generating it with the projection, and calls the repaintCallback if there
     * is one. Handles creating the image file path given the other info.
     * 
     * @param x the x uv coordinate of the tile
     * @param y the y uv coordinate of the tile
     * @param zoomLevel the zoomLevel of the tile
     * @param proj the current projection.
     * @param list the OMGraphicList to add the tile to.
     * @throws InterruptedException
     */
    private void loadTile(int x, int y, int zoomLevel, Projection proj, OMGraphicList list) {
        // String imagePath = zoomLevelInfo.formatImageFilePath(rootDir, x, y) +
        // fileExt;
        String imagePath = buildFilePath(x, y, zoomLevel, fileExt);
        loadTile(imagePath, x, y, zoomLevel, proj, list);
    }

    /**
     * An array of scales for all of the possible zoom levels, from 1 to 20.
     * They get calculate the first time getZoomLevelForProj is called.
     */
    protected float[] scales;

    /**
     * Build an image path to load, based on specified tile coordinates, zoom
     * level and file extension settings.
     * 
     * Look at the root directory definition and determine if the x,y,z values
     * of the path are specified as the holder values {z}, {x} and {y}.
     * 
     * If they aren't specified, the provided values will be appended to the
     * rootDir as z/x/y.fileExt.
     * 
     * If {z}, {x} or {y} are found in the root dir path, then it's assumed that
     * the rootDir contains all the information needed to specify the path and
     * regular expressions will be used to replace those value holders with the
     * values specified. The file extension in this case will not be appended to
     * the rootDir.
     * 
     * @param x the x tile coordinate
     * @param y the y tile coordinate
     * @param z the zoom level
     * @param fileExt the file extension to use for the path.
     */
    public String buildFilePath(int x, int y, int z, String fileExt) {
        if (tilePathBuilder == null) {
            tilePathBuilder = new TilePathBuilder(rootDir);
        }

        return tilePathBuilder.buildTilePath(x, y, z, fileExt);
    }

    private TilePathBuilder tilePathBuilder = null;

    /**
     * Creates a unique cache key for this tile based on zoom, x, y. This method
     * was created so the ServerMapTileFactory could override it and use local
     * cache names for keys if a local cache was being used.
     * 
     * @param x tile coord.
     * @param y tile coord.
     * @param z zoomLevel.
     * @param fileExt file extension.
     * @return String used in cache.
     */
    protected String buildCacheKey(int x, int y, int z, String fileExt) {
        return buildFilePath(x, y, z, fileExt);
    }

    public static class TilePathBuilder {
        String rez = "(\\{z\\})"; // Curly Braces 1
        String rex = "(\\{x\\})"; // Curly Braces 2
        String rey = "(\\{y\\})"; // Curly Braces 3

        Pattern pz = Pattern.compile(rez, Pattern.CASE_INSENSITIVE);
        Pattern px = Pattern.compile(rex, Pattern.CASE_INSENSITIVE);
        Pattern py = Pattern.compile(rey, Pattern.CASE_INSENSITIVE);

        String startingPath;
        boolean patternsUsed = false;
        boolean patternUseChecked = false;

        public TilePathBuilder(String rootDir) {
            startingPath = rootDir;
        }

        public boolean isPatternsUsed() {
            return patternsUsed;
        }

        public String buildTilePath(int x, int y, int z, String fileExt) {
            String ret = startingPath;
            if (((!patternUseChecked) || (patternUseChecked && patternsUsed))
		&& startingPath != null && startingPath.length() != 0) {
                ret = updatePath(ret, pz, Integer.toString(z));
                ret = updatePath(ret, px, Integer.toString(x));
                ret = updatePath(ret, py, Integer.toString(y));
                patternUseChecked = true;
            }

            if (!patternsUsed) {
                // No pattern matching, need to build from scratch, with fileExt
                return buildDefaultTilePath(x, y, z, fileExt);
            }

            return ret;
        }

        private String buildDefaultTilePath(int x, int y, int z, String fileExt) {
            return startingPath + "/" + z + "/" + x + "/" + y + fileExt;
        }

        private String updatePath(String currentPath, Pattern p, String replaceWith) {
            Matcher m = p.matcher(currentPath);
            if (m.find()) {
                patternsUsed = true;
                return m.replaceAll(replaceWith);
            }
            return currentPath;
        }
    }

    /**
     * Given a projection, figure out the appropriate zoom level for it. Right
     * now, 0 is totally zoomed with one tile for the entire earth. But we don't
     * return 0, we start at 1. OM can't handle one tile that covers the entire
     * earth because of the restriction for handling OMGraphics to less than
     * half of the earth.
     * 
     * @param proj
     * @return the zoom level.
     */
    public int getZoomLevelForProj(Projection proj) {
        int low = 1;
        int high = 20;
        MapTileCoordinateTransform mtct = new OSMMapTileCoordinateTransform();

        if (scales == null) {
            scales = mtct.getScalesForZoomLevels(proj, high);
        }

        float currentScale = proj.getScale();
        int ret = low;
        for (int currentZoom = low; currentZoom <= high; currentZoom++) {
            // nearest tile to center
            Point2D nttc = mtct.latLonToTileUV(proj.getCenter(), currentZoom);

            double nttcX = Math.floor(nttc.getX());
            double nttcY = Math.floor(nttc.getY());
            Point2D originLLUL = mtct.tileUVToLatLon(new Point2D.Double(nttcX, nttcY), currentZoom);
            Point2D originLLLR = mtct.tileUVToLatLon(new Point2D.Double(nttcX + 1, nttcY + 1), currentZoom);

            Point2D projUVUL = proj.forward(originLLUL);
            Point2D projLLLR = proj.forward(originLLLR);

            if (Math.abs(projUVUL.getX() - projLLLR.getX()) <= zoomLevelTileSize) {
                return currentZoom;
            }

            /*
             * Used to try to do this with scale comparisons, now just look at
             * tile sizes. float diff = currentScale - scales[currentZoom]; if
             * (diff > 0) { return currentZoom + 1; }
             */
        }

        return ret;
    }

    public MapTileRequester getMapTileRequester() {
        return mapTileRequester;
    }

    public void setMapTileRequester(MapTileRequester mtRequestor) {
        this.mapTileRequester = mtRequestor;
    }

    public Properties getProperties(Properties getList) {
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        getList.put(prefix + ROOT_DIR_PROPERTY, PropUtils.unnull(rootDirProperty));
        getList.put(prefix + FILE_EXT_PROPERTY, PropUtils.unnull(fileExt));
        getList.put(prefix + CACHE_SIZE_PROPERTY, Integer.toString(getCacheSize()));
        getList.put(prefix + MTCTRANSFORM_PROPERTY, mtcTransform.getClass().toString());
        if (emptyTileHandler != null) {
            getList.put(prefix + EMPTY_TILE_HANDLER_PROPERTY, emptyTileHandler.getClass().toString());
            if (emptyTileHandler instanceof PropertyConsumer) {
                ((PropertyConsumer) emptyTileHandler).getProperties(getList);
            }
        }

        // Only save the zoomLevelInfo property if it's not the default.
        if (zoomLevelInfo != null && !zoomLevelInfo.getClass().equals(ZoomLevelInfo.class)) {
            getList.put(prefix + ZOOM_LEVEL_INFO_PROPERTY, zoomLevelInfo.getClass().getName());
        }

        getList.put(prefix + ZOOM_LEVEL_TILE_SIZE_PROPERTY, Integer.toString(zoomLevelTileSize));
        TileImagePreparer tip = getTileImagePreparer();
        if (!(tip instanceof StandardImagePreparer)) {
            getList.put(prefix + TILE_IMAGE_PREPARER_PROPERTY, tip.getClass().getName());
            if (tip instanceof PropertyConsumer) {
                ((PropertyConsumer) tip).getProperties(getList);
            }
        }

        return getList;
    }

    public Properties getPropertyInfo(Properties list) {
        I18n i18n = Environment.getI18n();
        PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class, ROOT_DIR_PROPERTY, "Tile Directory", "Root directory containing image tiles", "com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class, FILE_EXT_PROPERTY, "Image File Extension", "Extension of image files (.jpg, .png, etc)", null);
        PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class, CACHE_SIZE_PROPERTY, "Cache Size", "Number of tile images held in memory", null);
        PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class, ZOOM_LEVEL_TILE_SIZE_PROPERTY, "Zoom Level Tile Size", "The maximum pixel size of a tile before switching to a higher zoom level (350 is default)", null);
        return list;
    }

    public String getPropertyPrefix() {
        return prefix;
    }

    public void setProperties(Properties setList) {
        setProperties(null, setList);
    }

    public void setProperties(String prefix, Properties setList) {
        setPropertyPrefix(prefix);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String rootDirectory = setList.getProperty(prefix + ROOT_DIR_PROPERTY);
        if (rootDirectory != null) {
            setRootDir(rootDirectory);
        }

        String tmpFileExt = setList.getProperty(prefix + FILE_EXT_PROPERTY);

        // Add a period if it doesn't exist.
        if (tmpFileExt != null) {
            setFileExt(tmpFileExt);
        }

        String mapTileCoordinateTransform = setList.getProperty(prefix + MTCTRANSFORM_PROPERTY);
        if (mapTileCoordinateTransform != null) {
            Object obj = ComponentFactory.create(mapTileCoordinateTransform);

            if (obj instanceof MapTileCoordinateTransform) {
                setMtcTransform((MapTileCoordinateTransform) obj);
            }
        }

        String emptyTileHandlerString = setList.getProperty(prefix + EMPTY_TILE_HANDLER_PROPERTY);
        if (emptyTileHandlerString != null) {
            Object obj = ComponentFactory.create(emptyTileHandlerString, prefix, setList);

            if (obj instanceof EmptyTileHandler) {
                setEmptyTileHandler((EmptyTileHandler) obj);
            }
        }

        String zoomLevelInfoString = setList.getProperty(prefix + ZOOM_LEVEL_INFO_PROPERTY);
        if (zoomLevelInfoString != null) {
            Object obj = ComponentFactory.create(zoomLevelInfoString, prefix, setList);

            if (obj instanceof ZoomLevelInfo) {
                setZoomLevelInfo((ZoomLevelInfo) obj);
            }
        }

        String tileImagePreparerString = setList.getProperty(prefix + TILE_IMAGE_PREPARER_PROPERTY);
        if (tileImagePreparerString != null) {
            Object obj = ComponentFactory.create(tileImagePreparerString, prefix, setList);
            if (obj instanceof TileImagePreparer) {
                setTileImagePreparer((TileImagePreparer) obj);
            }
        }

        super.resetCache(PropUtils.intFromProperties(setList, prefix + CACHE_SIZE_PROPERTY, getCacheSize()));

        zoomLevelTileSize = PropUtils.intFromProperties(setList, prefix
                + ZOOM_LEVEL_TILE_SIZE_PROPERTY, zoomLevelTileSize);
    }

    public void setPropertyPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDirectory) {

        if (rootDirectory != null) {
            if (rootDirectory.endsWith("jar")) {

                rootDirProperty = rootDirectory;
                String jarFileNames = rootDirectory;
                // Only use the tiles.omp file in the first file found.
                boolean tilesFileFound = false;

                Vector<String> jarNames = PropUtils.parseMarkers(jarFileNames, ";");
                for (String jarName : jarNames) {

                    boolean jarFileFound = false;

                    try {

                        if (!tilesFileFound) {
                            URL jarURL = PropUtils.getResourceOrFileOrURL(jarName);

                            if (jarURL != null) {
                                jarFileFound = true;
                                JarInputStream jarStream = new JarInputStream(jarURL.openStream());
                                JarEntry jarEntry = null;

                                while ((jarEntry = jarStream.getNextJarEntry()) != null) {
                                    String entryName = jarEntry.getName();
                                    if (entryName.equals(TILE_PROPERTIES)) {
                                        byte[] readBytes = new byte[100];
                                        byte[] contentBytes = new byte[0];

                                        int numRead = 0;
                                        while ((numRead = jarStream.read(readBytes, 0, readBytes.length)) > 0) {
                                            byte[] tmpBytes = new byte[numRead
                                                    + contentBytes.length];
                                            System.arraycopy(contentBytes, 0, tmpBytes, 0, contentBytes.length);
                                            System.arraycopy(readBytes, 0, tmpBytes, contentBytes.length, numRead);

                                            contentBytes = tmpBytes;
                                        }

                                        ByteArrayInputStream bais = new ByteArrayInputStream(contentBytes);
                                        configureFromProperties(bais, rootDirectory);
                                        bais.close();

                                        jarStream.closeEntry();
                                        tilesFileFound = true;
                                        break;
                                    }
                                }

                                jarStream.close();
                            }
                        }

                        if (jarFileFound) {
                            logger.fine("adding " + jarName + " to classpath");
                            ClasspathHacker.addFile(jarName);
                        } else {
                            logger.fine("can't find " + jarName + ", not adding to classpath");
                        }

                        // JarFile jarFile = new JarFile(jarName);
                        // JarEntry jarPropertyFile = (JarEntry)
                        // jarFile.getEntry(TILE_PROPERTIES);
                        //
                        // if (jarPropertyFile != null) {
                        // InputStream is =
                        // jarFile.getInputStream(jarPropertyFile);
                        // configureFromProperties(is, rootDirectory);
                        // }

                    } catch (IOException ioe) {
                        logger.warning("couldn't add map data jar file: " + jarName);
                    }

                }

                // You might notice that we didn't set the rootDir here if a jar
                // file is being used. That's because we just want to use
                // whatever
                // the tile file says, and this method will be called again if
                // needed when the properties get written.

            } else {
                // check for tile.omp file that may describe how to read tiles.
                File tileProps = new File(rootDirectory, TILE_PROPERTIES);

                // Keep track of what the root directory was before we read
                // tiles.omp
                String currentRootDirectory = this.rootDir;
                String currentRootDirProperty = rootDirProperty;
                if (tileProps.exists()) {
                    try {
                        // Do this in case other properties are set for the tile
                        // set, file ext, transform.
                        configureFromProperties(tileProps.toURI().toURL().openStream(), rootDirectory);
                    } catch (MalformedURLException murle) {
                        logger.warning("tile file for " + rootDirectory + " couldn't be read: "
                                + tileProps.getAbsolutePath());
                    } catch (IOException ioe) {
                        logger.warning("tile file for " + rootDirectory + " couldn't be read");
                    }
                }

                /*
                 * OK, things look a little crazy here, because there is a bit
                 * of recursion going on. The configure call above may cause a
                 * setRootDir call with a relative path stored in a jar file.
                 * These rules below make sure the relative root dir from the
                 * inner loop sets the rootDir, while preserving the
                 * rootDirProperty from the outer loop.
                 */

                if (currentRootDirectory == null && this.rootDir == null) {
                    // the tiles.omp file didn't change the root directory, so
                    // lets go with the directory given.
                    this.rootDir = rootDirectory;
                }

                if (currentRootDirProperty == null) {
                    // Assuming a file path being set, not as a result of a jar
                    // file
                    rootDirProperty = rootDirectory;
                }
            }

        } else {
            // nulled out
            this.rootDir = rootDirectory;
            rootDirProperty = rootDirectory;
        }

        // Reset for new path
        tilePathBuilder = null;

    }

    public TileImagePreparer getTileImagePreparer() {
        if (tileImagePreparer == null) {
            tileImagePreparer = new StandardImagePreparer();
        }
        return tileImagePreparer;
    }

    public void setTileImagePreparer(TileImagePreparer tileImagePreparer) {
        this.tileImagePreparer = tileImagePreparer;
    }

    /**
     * Called with an input stream for a properties file, used for reading
     * tiles.omp files.
     * 
     * @param is input stream for tiles.omp file.
     * @param rootDirectory original path to what was specified as root
     *        directory
     * @throws IOException
     */
    protected void configureFromProperties(InputStream is, String rootDirectory) throws IOException {

        Properties props = new Properties();
        props.load(is);

        props.put(ROOT_DIR_PATH_PROPERTY, rootDirectory);

        String oldPrefix = getPropertyPrefix();
        setProperties(null, props);
        setPropertyPrefix(oldPrefix);
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = (fileExt != null && fileExt.startsWith(".")) ? fileExt : "." + fileExt;
    }

    /**
     * Get the ZoomLevelInfo set on the factory. The ZoomLevelInfo has basic
     * layout information about tiles for a particular zoom level, including how
     * tiles are named and how the factory should go about loading them. The
     * default ZoomLevelInfo is based on the OpenStreetMap tile layout, zoom
     * levels 0-20 (where level 0 is all the way zoomed out), and the tiles are
     * stored zoomLevel/x/y.(fileExt).
     * 
     * @return the zoomLevelInfo
     */
    public ZoomLevelInfo getZoomLevelInfo() {
        return zoomLevelInfo;
    }

    /**
     * Get the ZoomLevelInfo set on the factory. The ZoomLevelInfo has basic
     * layout information about tiles for a particular zoom level, including how
     * tiles are named and how the factory should go about loading them. The
     * default ZoomLevelInfo is based on the OpenStreetMap tile layout, zoom
     * levels 0-20 (where level 0 is all the way zoomed out), and the tiles are
     * stored zoomLevel/x/y.(fileExt). You can set a different zoom level info
     * if you want to work with a tile set that is stored/defined differently
     * than OSM.
     * <p>
     * Won't allow itself to be set to null.
     * 
     * @param zoomLevelInfo the zoomLevelInfo to set
     */
    public void setZoomLevelInfo(ZoomLevelInfo zoomLevelInfo) {
        if (zoomLevelInfo != null) {
            this.zoomLevelInfo = zoomLevelInfo;
        }
    }

    public MapTileCoordinateTransform getMtcTransform() {
        return mtcTransform;
    }

    /**
     * Set the map tile coordinate transformed used to figure out lat/lon to
     * tile coordinates. Can't be null, if you set it to null an
     * OSMMapTileCoordTransform will be created instead.
     * 
     * @param mtcTransform
     */
    public void setMtcTransform(MapTileCoordinateTransform mtcTransform) {
        if (mtcTransform == null) {
            mtcTransform = new OSMMapTileCoordinateTransform();
        }
        this.mtcTransform = mtcTransform;
    }

    /**
     * @return the emptyTileHandler
     */
    public EmptyTileHandler getEmptyTileHandler() {
        return emptyTileHandler;
    }

    /**
     * @param emptyTileHandler the emptyTileHandler to set
     */
    public void setEmptyTileHandler(EmptyTileHandler emptyTileHandler) {
        this.emptyTileHandler = emptyTileHandler;
    }
}
