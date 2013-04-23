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

import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;

import com.bbn.openmap.Environment;
import com.bbn.openmap.Layer;
import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.image.ImageServer;
import com.bbn.openmap.image.PNG32ImageFormatter;
import com.bbn.openmap.image.SunJPEGFormatter;
import com.bbn.openmap.layer.imageTile.MapTileLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The MapTileMaker is an ImageServer extension that knows how to create image
 * tile sets, like the kind of tiles used by Google Maps and OpenStreetMap, Tile
 * Map Service (TMS). It uses ZoomLayerMarker objects to define how tiles are
 * created for different zoom levels. You can run this class as an application.
 * With the -create option, it will create a sample properties file to
 * demonstrate what properties are needed to run it.
 * <p>
 * 
 * The properties look much like the ImageServer properties, with a couple of
 * additional values:
 * <p>
 * 
 * <pre>
 * ### MapTileMaker/ImageServer properties ###
 * antialiasing=false
 * # Image formatter definition
 * formatters=formatter1
 * # Layer definitions for layers that are available for zoom levels
 * layers=layer1 layer2 ...
 * rootDir=Path to top level directory for tiles
 * zoomLevels=zoom1 zoom2
 * 
 * formatter1=.class=com.bbn.openmap.image.PNG32ImageFormatter
 * layer1.class=com.bbn.openmap.layer.shape.ShapeLayer
 * # ... layer1 properties follow, see layer docs for specific properties for that layer
 * 
 * # Then, for each zoom level
 * zoom1.class=com.bbn.openmap.image.ZoomLevelInfo
 * #Optional, to limit tile areas created, in sets of 4, must be in lat,lon order
 * zoom1.bounds=lat lon lat lon
 * zoom1.description=Tiles for zoom level 4
 * #Marker names for layers to be rendered, the property prefixes for the layers held by TileMaker
 * zoom1.layers=layer1 layer2
 * zoom1.name=ZoomLayerInfo 4
 * zoom1.zoomLevel=4
 * # If defined, copies of zoomLevel tiles will be scaled through range level.
 * zoom1.range=0
 * 
 * # and repeat for every zoomLevel defined
 * </pre>
 * 
 * @author dietrick
 */
public class MapTileMaker extends ImageServer {

    public final static String ROOT_DIRECTORY_PROPERTY = "rootDir";
    public final static String ZOOM_LEVELS_PROPERTY = "zoomLevels";

    protected String rootDir;
    protected List<ZoomLevelMaker> zoomLevels;
    protected MapTileCoordinateTransform mtcTransform = new OSMMapTileCoordinateTransform();
    protected int TILE_SIZE = mtcTransform.getTileSize();
    
    /**
     * Empty constructor that expects to be configured later.
     */
    protected MapTileMaker() {
    }

    /**
     * To create the TileMaker, you hand it a set of properties that let it
     * create an array of layers, and also to set the properties for those
     * layers. The properties file for the ImageServer looks strikingly similar
     * to the openmap.properties file. So, all the layers get set up here...
     */
    public MapTileMaker(Properties props) {
        super(props);
    }

    /**
     * Same as the other constructor, except that the properties can have a
     * prefix in front of them. The format of the prefix has to match how the
     * property is specified the the properties file, which may include the
     * period - i.e server1.imageServer.layers, the server1. is the prefix that
     * should get passed in. The ImageMaster does this.
     */
    public MapTileMaker(String prefix, Properties props) {
        super(prefix, props, null);
    }

    /**
     * Create an TileMaker that should be configured with a Properties file. The
     * prefix given is to scope the ImageServer properties to this instance. The
     * Hashtable is for reusing any layers that may already be instantiated.
     */
    public MapTileMaker(String prefix, Properties props, Map<String, Layer> instantiatedLayers) {
        super(prefix, props, instantiatedLayers);
    }

    /**
     * Create an TileMaker from an array of Layers and an ImageFormatter. It's
     * assumed that the layers are already configured.
     * 
     * @param layers the array of layers.
     * @param formatter the ImageFormatter to use for the output image format.
     */
    public MapTileMaker(Layer[] layers, ImageFormatter formatter) {
        super(layers, formatter);
    }

    @SuppressWarnings("unchecked")
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        rootDir = props.getProperty(prefix + ROOT_DIRECTORY_PROPERTY, rootDir);
        List<ZoomLevelMaker> zoomLevels = (List<ZoomLevelMaker>) PropUtils.objectsFromProperties(props, prefix
                + ZOOM_LEVELS_PROPERTY, ComponentFactory.ClassNameProperty);
        getZoomLevels().addAll(zoomLevels);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + ROOT_DIRECTORY_PROPERTY, PropUtils.unnull(rootDir));

        StringBuffer buf = new StringBuffer();
        for (ZoomLevelMaker zfi : getZoomLevels()) {
            buf.append(zfi.getPropertyPrefix()).append(" ");
            zfi.getProperties(props);
        }

        if (buf.length() > 0) {
            props.put(prefix + ZOOM_LEVELS_PROPERTY, buf.toString().trim());
        }

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        PropUtils.setI18NPropertyInfo(Environment.getI18n(), props, com.bbn.openmap.dataAccess.mapTile.MapTileMaker.class, ROOT_DIRECTORY_PROPERTY, "Tile Directory", "Root directory for holding tile files.", "com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor");
        return props;
    }

    /**
     * Creating the tile using the ImageServer methodology, knowing that the
     * MapTileMaker has been configured with an openmap.properties.file and
     * knows about layers and their marker names.
     * 
     * @param uvx uv x pixel coordinate
     * @param uvy uv y pixel coordinate
     * @param zoomInfo zoom level for image tile
     * @param proj projection for tile
     * @return byte[] for raw image bytes
     */
    public byte[] makeTile(double uvx, double uvy, ZoomLevelMaker zoomInfo, Proj proj) {
        Point2D center = tileUVToLatLon(new Point2D.Double(uvx + .5, uvy + .5), zoomInfo.getZoomLevel());
        proj.setScale(mtcTransform.getScaleForZoom(zoomInfo.getZoomLevel()));
        proj.setCenter(center);
        proj.setHeight(TILE_SIZE);
        proj.setWidth(TILE_SIZE);

        return createImage(proj, -1, -1, zoomInfo.getLayers());
    }

    /**
     * Creating a tile more freely, when you have a set of layers you want to
     * draw into the tile.
     * 
     * @param uvx uv x pixel coordinate
     * @param uvy uv y pixel coordinate
     * @param zoomLevel zoom level for tile
     * @param layers layers to include in image
     * @param proj projection for tile
     * @param background the paint to use for the background of the image.
     * @return byte[] for raw image bytes
     */
    public byte[] makeTile(double uvx, double uvy, int zoomLevel, List<Layer> layers, Proj proj,
                           Paint background) {
        Point2D center = tileUVToLatLon(new Point2D.Double(uvx + .5, uvy + .5), zoomLevel);
        proj.setScale(mtcTransform.getScaleForZoom(zoomLevel));
        proj.setCenter(center);
        proj.setHeight(TILE_SIZE);
        proj.setWidth(TILE_SIZE);

        return createImageFromLayers(proj, -1, -1, layers, background);
    }

    /**
     * The main call to make for a tile to be created. This method will cause
     * the correct mapTile method to be called, depending on the configuration
     * of the ZoomLevelMakers.
     * 
     * @param uvx
     * @param uvy
     * @param zoomInfo
     * @param proj
     * @return the final file path used, with any extensions added.
     * @throws IOException
     */
    public String makeTileFile(double uvx, double uvy, ZoomLevelMaker zoomInfo, Proj proj)
            throws IOException {

        byte[] imageBytes = zoomInfo.makeTile(uvx, uvy, this, proj);

        String filePath = zoomInfo.formatImageFilePath(getRootDir(), (int) uvx, (int) uvy);
        return writeImageFile(imageBytes, filePath, true);
    }

    /**
     * Main call to make a set of tiles. The ZoomLevelMaker objects have to be
     * configured correctly. Each ZoomLevelMaker has to have its zoom levels set
     * (initial and range), the bounds of the areas where tiles are desired, and
     * the layers desired on those tiles. For the layers, the ZoomLevelInfo can
     * have a List of Strings corresponding to the property prefixes of the
     * layers already set on the MapTileMaker, or it can have a List of Layer
     * objects to use. The root output directory has to be set in the
     * MapTileMaker. The image formatter also needs to be set.
     */
    public void makeTiles() {

        if (rootDir != null) {
            File rd = new File(rootDir);
            if (!rd.exists()) {
                rd.mkdir();
            }
        }

        Proj proj = new Mercator(new LatLonPoint.Double(), 10000, MapTileCoordinateTransform.TILE_SIZE, MapTileCoordinateTransform.TILE_SIZE);

        List<ZoomLevelMaker> zoomLevels = getZoomLevels();
        for (ZoomLevelMaker zfi : zoomLevels) {
            logger.info("writing zoom level " + zfi.getName() + " tiles...");
            int zoomLevel = zfi.getZoomLevel();
            for (Rectangle2D bounds : zfi.getUVBounds(mtcTransform, zoomLevel)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(" creating tiles " + bounds);
                }
                int startx = (int) bounds.getX();
                int starty = (int) bounds.getY();
                int xofflimit = (int) bounds.getWidth();
                int yofflimit = (int) bounds.getHeight();

                for (int xoff = 0; xoff < xofflimit; xoff++) {
                    int x = startx + xoff;

                    // Reset every x loop for first time check through y loop
                    String parentDirPath = null;

                    for (int yoff = 0; yoff < yofflimit; yoff++) {
                        int y = starty + yoff;

                        if (parentDirPath == null) {
                            parentDirPath = zfi.formatParentDirectoryName(getRootDir(), x, y);
                            File parentDir = new File(parentDirPath);
                            if (!parentDir.exists()) {
                                parentDir.mkdirs();
                            }
                        }

                        try {
                            String outputFile = makeTileFile(x, y, zfi, proj);
                            if (logger.isLoggable(Level.FINER)) {
                                logger.finer("wrote: " + outputFile);
                            }
                        } catch (IOException ioe) {
                            logger.warning("Caught IOException writing " + x + ", " + y + ", "
                                    + zfi);
                        }
                    }

                }
            }

            // At this point, for a specific ZoomLevelInfo, the basic tiles for
            // it have been created. Now we can check the range and create tiles
            // for the range out of the new tiles.
            int range = zfi.getRange();
            if (range < zoomLevel) {
                Properties rangeProps = new Properties();
                MapTileLayer tileLayer = new MapTileLayer();
                StandardMapTileFactory tileFactory = new StandardMapTileFactory();
                tileFactory.setRootDir(getRootDir());
                tileFactory.setFileExt(getFormatter().getFormatLabel());
                tileLayer.setTileFactory(tileFactory);

                List<Layer> subLayers = new ArrayList<Layer>();
                subLayers.add(tileLayer);

                for (int rangeZoomLevel = zoomLevel - 1; rangeZoomLevel >= range; rangeZoomLevel--) {
                    tileLayer.setZoomLevel(rangeZoomLevel);
                    ZoomLevelInfo rangeZFI = new ZoomLevelInfo();
                    rangeZFI.setZoomLevel(rangeZoomLevel);
                    rangeZFI.setScale(mtcTransform.getScaleForZoom(rangeZoomLevel));
                    // Create new tiles from the tiles one zoom level up
                    tileLayer.setZoomLevel(rangeZoomLevel + 1);

                    for (Rectangle2D rawBounds : zfi.getBounds()) {
                        Rectangle2D bounds = rangeZFI.getUVBounds(rawBounds, mtcTransform, rangeZoomLevel);
                        if (logger.isLoggable(Level.INFO)) {
                            logger.fine(" creating subtiles " + bounds);
                        }
                        int startx = (int) bounds.getX();
                        int starty = (int) bounds.getY();
                        int xofflimit = (int) bounds.getWidth();
                        int yofflimit = (int) bounds.getHeight();

                        for (int xoff = 0; xoff < xofflimit; xoff++) {
                            int x = startx + xoff;

                            // Reset every x loop for first time check through y
                            // loop
                            String parentDirPath = null;

                            for (int yoff = 0; yoff < yofflimit; yoff++) {
                                int y = starty + yoff;

                                if (parentDirPath == null) {
                                    parentDirPath = rangeZFI.formatParentDirectoryName(getRootDir(), x, y);
                                    File parentDir = new File(parentDirPath);
                                    if (!parentDir.exists()) {
                                        parentDir.mkdirs();
                                    }
                                }

                                try {

                                    byte[] imageBytes = makeTile(x, y, rangeZoomLevel, subLayers, proj, OMColor.clear);

                                    String filePath = rangeZFI.formatImageFilePath(getRootDir(), (int) x, (int) y);
                                    String outputFile = writeImageFile(imageBytes, filePath, true);

                                    if (logger.isLoggable(Level.INFO)) {
                                        logger.finer("wrote: " + outputFile);
                                    }
                                } catch (IOException ioe) {
                                    logger.warning("Caught IOException writing " + x + ", " + y
                                            + ", " + zfi);
                                }
                            }

                        }

                    }
                }

            }
        }

        logger.info("done writing tiles");
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public List<ZoomLevelMaker> getZoomLevels() {
        if (zoomLevels == null) {
            zoomLevels = new LinkedList<ZoomLevelMaker>();
        }
        return zoomLevels;
    }

    public void setZoomLevels(List<ZoomLevelMaker> zoomLevels) {
        this.zoomLevels = zoomLevels;
    }

    public void createDefaultZoomLevels(int maxZoomLevel) {
        Layer[] layers = getLayers();
        List<ZoomLevelMaker> zoomLevels = getZoomLevels();
        List<String> layerNames = new LinkedList<String>();
        for (int i = 0; i < layers.length; i++) {
            String layerName = layers[i].getPropertyPrefix();
            if (layerName != null) {
                layerNames.add(layerName);
            } else {
                logger.info("no name for layer[" + i + "]");
            }
        }

        zoomLevels.clear();

        for (int i = 0; i <= maxZoomLevel; i++) {
            ZoomLevelMaker zfi = new ZoomLevelMaker("ZoomLayerInfo " + i, "Tiles for zoom level "
                    + i, i);
            zfi.setLayers(layerNames);
            zfi.setPropertyPrefix("zoom" + i);
            zoomLevels.add(zfi);
        }
    }

    /**
     * @param latlon a Point2D whose x component is the longitude and y
     *        component is the latitude
     * @param zoom Tile Map Service (TMS) style zoom level (0-19 usually)
     * @return The "tile number" whose x and y components each are floating
     *         point numbers that represent the distance in number of tiles from
     *         the origin of the whole map at this zoom level. At zoom=0, the
     *         lat,lon point of 0,0 maps to 0.5,0.5 since there is only one tile
     *         at zoom level 0.
     */
    public Point2D latLonToTileUV(Point2D latlon, int zoom) {
        return mtcTransform.latLonToTileUV(latlon, zoom, null);
    }

    public Point2D latLonToTileUV(Point2D latlon, int zoom, Point2D ret) {
        return mtcTransform.latLonToTileUV(latlon, zoom, ret);
    }

    /**
     * @param tileUV a Point2D whose x,y coordinates represent the distance in
     *        number of tiles (each 256x256) from the origin (where the origin
     *        is 90lat,-180lon)
     * @param zoom Tile Map Service (TMS) style zoom level (0-19 usually)
     * @return a LatLonPoint whose x coordinate is the longitude and y
     *         coordinate is the latitude, decimal degrees.
     */
    public LatLonPoint tileUVToLatLon(Point2D tileUV, int zoom) {
        return mtcTransform.tileUVToLatLon(tileUV, zoom, null);
    }

    public LatLonPoint tileUVToLatLon(Point2D tileUV, int zoom, LatLonPoint ret) {
        return mtcTransform.tileUVToLatLon(tileUV, zoom, ret);
    }

    public static void main(String[] args) {

        com.bbn.openmap.util.ArgParser ap = new com.bbn.openmap.util.ArgParser("MapTileMaker");

        ap.add("properties", "The properties file to use for image tiles.", 1);
        ap.add("create", "Create a sample properties file at a path", 1);

        if (!ap.parse(args)) {
            ap.printUsage();
            System.exit(0);
        }

        String arg[];

        Properties props = null;
        arg = ap.getArgValues("properties");
        if (arg != null) {
            String ps = arg[0];
            try {

                URL url = PropUtils.getResourceOrFileOrURL(null, ps);
                InputStream inputStream = url.openStream();

                props = new Properties();
                props.load(inputStream);

                MapTileMaker tim = new MapTileMaker(props);
                tim.makeTiles();

            } catch (MalformedURLException murle) {
                Debug.error("TileMaker can't find properties file: " + arg[0]);
            } catch (IOException ioe) {
                Debug.error("TileMaker can't create images: IOException");
            }
        }

        arg = ap.getArgValues("create");
        if (arg != null) {
            String outputFile = arg[0];
            MapTileMaker tim;
            if (props == null) {
                ShapeLayer shapeLayer = new ShapeLayer();
                props = new Properties();
                props.put("shape.prettyName", "Countries");
                props.put("shape.shapeFile", "data/shape/world/cntry02/cntry02.shp");
                props.put("shape.fillColor", "FFBBBBBB");
                shapeLayer.setProperties("shape", props);

                tim = new MapTileMaker(new Layer[] { shapeLayer }, new SunJPEGFormatter());
                tim.createDefaultZoomLevels(4);

                tim.setRootDir("<Path to top level directory for tiles>");
                tim.setFormatter(new PNG32ImageFormatter());

            } else {
                tim = new MapTileMaker(props);
            }

            Properties configurationProps = new Properties();
            configurationProps = tim.getProperties(configurationProps);

            StringBuilder sb = new StringBuilder("#### MapTileMaker Properties ####\n");

            if (!configurationProps.isEmpty()) {
                TreeMap orderedProperties = new TreeMap(configurationProps);
                for (Iterator keys = orderedProperties.keySet().iterator(); keys.hasNext();) {
                    String key = (String) keys.next();
                    String value = configurationProps.getProperty(key);

                    if (value != null) {
                        sb.append(key).append("=").append(value).append("\n");
                    }
                }
            }

            try {
                FileOutputStream fos = new FileOutputStream(outputFile);
                PrintStream ps = new PrintStream(fos);
                ps.println(sb.toString());
                ps.close();
            } catch (IOException ioe) {
                logger.warning("caught IOException writing property file: " + ioe.getMessage());
            }

        }

        System.exit(0);
    }

}
