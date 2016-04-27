/*
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */

package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.ImageIcon;

import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ClasspathHacker;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.PropUtils;

/**
 * The WholeWorldTileHandler is a special EmptyTileHandler that manages a
 * special set of jar files containing tiles that cover the entire earth. If you
 * put all of the tiles from zoom levels 0-12 into a jar file, you end up with a
 * 5Gb jar, and java doesn't handle it. Having zoom levels 0-14 on disk takes up
 * 30Gb without being compressed.
 * <p>
 * 
 * This class, run as an application, organizes tiles into several jar files,
 * depending on their zoom level and location. All tiles for the world in zoom
 * levels 0-10 are placed in a single jar file. For all other zoom levels, they
 * are placed together geographically, based on the tile boundaries of some
 * higher level (zoom level 3 is the default, higher numbers might be chosen if
 * tiles are created for higher zoom levels than 15).
 * <p>
 * 
 * This class is consulted when the MapTileFactory can't find a map tile. Since
 * this class is an EmptyTileHandler, it is given free reign to return a tile
 * for the missing one. Instead of assuming that the tiles don't exist and a
 * quick replacement needs to be created, this EmtpyTileHandler assumes that the
 * proper jar file hasn't been loaded yet, which is why the tile wasn't found as
 * a resource. It uses the ux, uv, and zoom level for the tile to figure out
 * what jar it would have been in, and checks to see if that jar has been added
 * to the classpath. If the jar has been added, then the tile doesn't exist. If
 * the jar needs to be added, it is and then the tile retrieval starts.
 * <p>
 * 
 * The properties for the tile set are a little different when using this
 * EmptyTileHandler:
 * 
 * <pre>
 * # specify this empty tile handler
 * emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.WholeWorldTileHandler
 * # instead of the root directory to the jars, specify the root directory of the tiles 
 * # inside the jars.  All of the jars within a set should share the root directory name 
 * # within them.  Each set of jars should have a unique root directory name for that dataset.
 * rootDir=main_tiles
 * # The name of the jar containing world-level coverage.  All of the sub-jars should be 
 * # named based on this jar name.  If this class is run to build the tile jars, 
 * # it should have done this automatically.
 * parentJarName=main_tiles.jar
 * # This is the grid that defines how the subjars are divided.  It matches the tile boundaries 
 * # of a certain zoom level. Level 3 is the default.
 * subJarZoomLevel=9
 * # This is the highest level of tiles held in the part jar name.  Tells the layer when it should
 * # start looking in subjars for tiles at higher zoom levels.
 * worldWideZoomLevel=10
 * # Tells the layer when to stop trying to fill tile orders.
 * noCoverageZoom=15
 * # optional, png is default.
 * fileExt=.png
 * # You can also specify how empty tiles are handled, using the ShpFileEmptyTileHandler.
 * shpFile=/Users/dietrick/dev/openmap/share/data/shape/cntry02/cntry02.shp
 * land.fillColor=DECD8B
 * background.fillColor=EAFFF4
 * mapTileTransform=com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform
 * land.lineColor=DECD8B
 * background.lineColor=EAFFF4
 * 
 * </pre>
 * 
 * NOTE: If you create a set of tile jars to use with a Windows jre, and you're
 * having problems when the jars are added to the classpath, it's because there
 * are too many tiles in the jars. The number for files in this case can't
 * exceed 262144. So you have to increase the zoom level used for the layout of
 * the subjar files.
 * 
 * @author ddietrick
 */
public class WholeWorldTileHandler extends ShpFileEmptyTileHandler {
    public final static String SUBJAR_ZOOMLEVEL_PROPERTY = "subJarZoomLevel";
    public final static String WORLDWIDE_ZOOMLEVEL_PROPERTY = "worldWideZoomLevel";
    public final static String PARENT_JAR_NAME_PROPERTY = "parentJarName";
    public final static int DEFAULT_SUBJAR_DEF_ZOOMLEVEL = 3;
    public final static int DEFAULT_WORLDWIDE_ZOOMLEVEL = 10;
    protected int subFrameDefZoomLevel = DEFAULT_SUBJAR_DEF_ZOOMLEVEL;
    protected int worldWideZoomLevel = DEFAULT_WORLDWIDE_ZOOMLEVEL;
    protected String parentJarName;
    protected Set<String> loadedJars;
    protected String rootDirForJars;
    
    final static String SOURCE = "source";
    final static String TARGET = "target";
    final static String SUB_JAR_ZOOM = "subJarZoom";
    final static String MAX_ZOOM_IN_SUBJARS = "maxZoomInSubJars";
    final static String WORLD_WIDE_ZOOM_LEVEL = "worldWideZoomLevel";
    final static String TILE_EXT = "tileExt";
    final static String MINX = "minx";
    final static String MINY = "miny";
    final static String MAXX = "maxx";
    final static String MAXY = "maxy";
    final static String NO_WORLD_JAR = "noWorldJar";
    final static String VERBOSE = "verbose";
    final static String FILL = "fill";
    
    /**
     * Turns out that the Windows JRE won't add jar files with more than 262144
     * tiles in them.
     */
    public final static int WIN_MAX_FILES_IN_JAR = 262144;

    public WholeWorldTileHandler() {
        loadedJars = Collections.synchronizedSet(new HashSet<String>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.dataAccess.mapTile.EmptyTileHandler#
     * getOMGraphicForEmptyTile (java.lang.String, int, int, int,
     * com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform,
     * com.bbn.openmap.proj.Projection)
     */
    public BufferedImage getImageForEmptyTile(String imagePath, int x, int y, int zoomLevel,
                                              MapTileCoordinateTransform mtcTransform,
                                              Projection proj) {

        BufferedImage bi = null;

        if (parentJarName != null) {

            /**
             * OK, the thing to remember here is that if this method has been
             * called, the tile has not been found. So our job here is to
             * determine if a jar file needs to be loaded, and if it does, load
             * it and see if the missing tile is in it. If a jar doesn't need to
             * be loaded, then we push the action up to the superclass - it's a
             * non-created tile.
             */
            Point2D pnt = new Point2D.Double(x, y);
            Point2D tileUL = mtcTransform.tileUVToLatLon(pnt, zoomLevel);

            StringBuilder jarFilePath = new StringBuilder("jar:file:").append(rootDirForJars).append("/");

            if (zoomLevel <= worldWideZoomLevel) {
                /*
                 * Need to load the primary jar that has worldwide coverage.
                 */
                jarFilePath.append(parentJarName);

            } else {
                /*
                 * Need to figure out what the subjar name is, given the lat/lon
                 * coordinate of the tile.
                 */

                Point2D subframeUVPnt = mtcTransform.latLonToTileUV(tileUL, subFrameDefZoomLevel);
                jarFilePath.append(buildSubJarName(parentJarName, subframeUVPnt.getX(), subframeUVPnt.getY()));
            }

            jarFilePath.append("!/").append(imagePath);

            byte[] imageBytes = getImageBytes(jarFilePath.toString());

            if (imageBytes != null && imageBytes.length > 0) {
                // image found

                try {
                    ImageIcon ii = new ImageIcon(imageBytes);
                    Image origImage = ii.getImage();
                    int imageWidth = ii.getIconWidth();
                    int imageHeight = ii.getIconHeight();

                    if (origImage instanceof BufferedImage) {
                        return (BufferedImage) origImage;
                    } else {
                        return BufferedImageHelper.getBufferedImage(origImage, 0, 0, imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                    }

                } catch (InterruptedException ie) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("WholeWorldTileHandler interrupted fetching " + imagePath);
                    }
                }

            }

        } else {
            logger.fine("parent jar name not set, can't figure out how to load tile jars.");
        }

        // If no image is found, do the default action for an empty tile.
        if (bi == null) {
            bi = super.getImageForEmptyTile(imagePath, x, y, zoomLevel, mtcTransform, proj);
        }

        return bi;
    }

    public static String buildSubJarName(String parentJarNme, double x, double y) {
        int dotIndex = parentJarNme.length();
        if (parentJarNme.endsWith(".jar")) {
            dotIndex = parentJarNme.lastIndexOf('.');
        }

        StringBuilder sBuilder = new StringBuilder(parentJarNme.substring(0, dotIndex));
        sBuilder.append("_" + ((int) x) + "_" + ((int) y) + parentJarNme.substring(dotIndex));

        return sBuilder.toString();
    }

    /**
     * Tries to get the image bytes from imagePath URL. If image found, will
     * write it locally to localFilePath for caching.
     * 
     * @param imagePath the source URL image path.
     * @param localFilePath the caching local file path
     * @return byte[] of image
     */
    public byte[] getImageBytes(String imagePath) {
        byte[] imageBytes = null;

        try {
            java.net.URL url = new java.net.URL(imagePath);
            java.net.URLConnection urlc = url.openConnection();

            if (logger.isLoggable(Level.FINER)) {
                logger.finer("url content type: " + urlc.getContentType());
            }

            if (urlc == null || urlc.getContentType() == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("unable to connect to (tile might be unavailable): " + imagePath);
                }

                // text
            } else if (urlc.getContentType().startsWith("text")) {
                java.io.BufferedReader bin = new java.io.BufferedReader(new java.io.InputStreamReader(urlc.getInputStream()));
                String st;
                StringBuffer message = new StringBuffer();
                while ((st = bin.readLine()) != null) {
                    message.append(st);
                }

                // Debug.error(message.toString());
                // How about we toss the message out to the user
                // instead?
                logger.fine(message.toString());

                // image
            } else if (urlc.getContentType().startsWith("image")) {

                InputStream in = urlc.getInputStream();
                // ------- Testing without this
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int buflen = 2048; // 2k blocks
                byte buf[] = new byte[buflen];
                int len = -1;
                while ((len = in.read(buf, 0, buflen)) != -1) {
                    out.write(buf, 0, len);
                }
                out.flush();
                out.close();

                imageBytes = out.toByteArray();

            } // end if image
        } catch (java.net.MalformedURLException murle) {
            logger.warning("WholeWorldTileHandler: URL \"" + imagePath + "\" is malformed.");
        } catch (java.io.IOException ioe) {
            logger.fine("Couldn't connect to " + imagePath + ", connection problem");
        }

        return imageBytes;

    }

    /**
     * Load the jar if necessary. Not called anymore, but left here as a note on
     * how to dynamically modify the classpath to add a jar at runtime.
     * 
     * @param jarFileName the absolute path to the jar.
     * @return true if the jar was loaded, false if it was added before and no
     *         attempt made.
     */
    protected synchronized boolean loadJar(String jarFileName) {

        boolean ret = loadedJars.contains(jarFileName);

        if (!ret) {
            try {
                logger.fine("adding " + jarFileName + " to classpath");
                ClasspathHacker.addFile(jarFileName);
            } catch (IOException ioe) {
                logger.warning("couldn't add map data jar file: " + jarFileName);
            }

            /*
             * We're going to add the jarFileName to the list regardless of
             * whether it was successful or not, to avoid having repeated
             * failures on subsequent jar loads.
             */
            loadedJars.add(jarFileName);
        }

        return !ret;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        subFrameDefZoomLevel = PropUtils.intFromProperties(props, prefix
                + SUBJAR_ZOOMLEVEL_PROPERTY, subFrameDefZoomLevel);
        worldWideZoomLevel = PropUtils.intFromProperties(props, prefix
                + WORLDWIDE_ZOOMLEVEL_PROPERTY, worldWideZoomLevel);
        rootDirForJars = props.getProperty(prefix
                + StandardMapTileFactory.ROOT_DIR_PATH_PROPERTY, rootDirForJars);

        String jarBaseName = props.getProperty(prefix + PARENT_JAR_NAME_PROPERTY);
        if (jarBaseName != null) {
            setParentJarName(jarBaseName);
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + SUBJAR_ZOOMLEVEL_PROPERTY, Integer.toString(subFrameDefZoomLevel));
        props.put(prefix + WORLDWIDE_ZOOMLEVEL_PROPERTY, Integer.toString(worldWideZoomLevel));
        props.put(prefix + PARENT_JAR_NAME_PROPERTY, PropUtils.unnull(getParentJarName()));

        return props;
    }

    /**
     * Get the tile zoom level that the subjar borders are based on.
     * 
     * @return the subFrameDefZoomLevel
     */
    public int getSubFrameDefZoomLevel() {
        return subFrameDefZoomLevel;
    }

    /**
     * Set the tile zoom level that the subjar borders will be based on.
     * 
     * @param subFrameZoomLevel the subFrameDefZoomLevel to set
     */
    public void setSubFrameDefZoomLevel(int subFrameZoomLevel) {
        this.subFrameDefZoomLevel = subFrameZoomLevel;
    }

    /**
     * Get the name of the jar with worldwide coverage, for zoom levels
     * 0-worldWideZoomLevel.
     * 
     * @return the parentJarName
     */
    public String getParentJarName() {
        return parentJarName;
    }

    /**
     * Set the name of the jar with worldwide coverage, for tiles with zoom
     * levels 0-worldWideZoomLevel.
     * 
     * @param parentJarName the parentJarName to set
     */
    public void setParentJarName(String parentJarName) {
        this.parentJarName = parentJarName;
    }

    /**
     * Get the maximum tile zoom level that is contained in the worldwide jar.
     * 
     * @return the worldWideZoomLevel
     */
    public int getWorldWideZoomLevel() {
        return worldWideZoomLevel;
    }

    /**
     * Set the maximum tile zoom level that is contained in the worldwide jar.
     * 
     * @param worldWideZoomLevel the worldWideZoomLevel to set
     */
    public void setWorldWideZoomLevel(int worldWideZoomLevel) {
        this.worldWideZoomLevel = worldWideZoomLevel;
    }

    /**
     * The builder class takes a map tile directory and creates a set of jars
     * from it, divided so that the WholeWorldTileHandler can deal with it. It
     * will create one jar file that holds the tiles that cover the world for a
     * selected set of tiles. It then creates jar files (sub-jars) that contain
     * tiles for the other zoom levels. These sub-jars are organized by
     * geographic zone as defined by tiles from a lower zoom level. The default
     * level is 3. For instance, subjar_0_0.jar contains all the tiles for zoom
     * levels 11-20 that fit in the geographic area covered by tile 0, 0 for
     * zoom level 3.
     * <P>
     * There are options to control the geographic area definition zoom level,
     * the zoom level for the tiles stored in the world-wide jar, and the
     * maximum zoom level inserted in to the sub-jars.
     * 
     * @author ddietrick
     */
    public static class Builder {
        protected File sourceFile;
        protected File targetFile;
        protected int subJarZoomDef = WholeWorldTileHandler.DEFAULT_SUBJAR_DEF_ZOOMLEVEL;
        protected int worldWideZoomLevel = 10;
        protected int maxZoomLevelInSubJars = 20;
        protected String tileExt = ".png";
        protected int minx = 0;
        protected int miny = 0;
        protected int maxx = -1;
        protected int maxy = -1;
        protected boolean doWorldJar = true;
        protected boolean fill = false;
        protected Level logLevel = Level.FINE;
        
        public Builder(File source) throws FileNotFoundException {

            if (source == null || !source.exists()) {
                throw new FileNotFoundException("Source file invalid");
            }

            this.sourceFile = source;
        }

        public Builder targetFile(File targetFile) {
            this.targetFile = targetFile;

            return this;
        }

        public Builder tileExt(String tileExtension) {
            if (!tileExtension.startsWith(".")) {
                tileExtension = "." + tileExtension;
            }
            this.tileExt = tileExtension;
            return this;
        }

        public Builder subJarZoomDef(int zoomLevel) throws NumberFormatException {
            if (checkZoomLevel(zoomLevel)) {
                this.subJarZoomDef = zoomLevel;
            }
            return this;
        }

        public Builder worldWideZoomLevel(int zoomLevel) throws NumberFormatException {
            if (checkZoomLevel(zoomLevel)) {
                this.worldWideZoomLevel = zoomLevel;
            }
            return this;
        }

        public Builder maxZoomLevelInSubJars(int zoomLevel) throws NumberFormatException {
            if (checkZoomLevel(zoomLevel)) {
                this.maxZoomLevelInSubJars = zoomLevel;
            }
            return this;
        }

        protected boolean checkZoomLevel(int zoomLevel) {
            if (zoomLevel < 0 || zoomLevel > 20) {
                throw new NumberFormatException("Zoom level needs to be > 0 and < 20");
            }
            return true;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("WholeWorldTileHandler[");
            sb.append("source:").append(sourceFile).append(',');
            sb.append("target:").append(targetFile).append(',');
            sb.append("subJarZoomDef:").append(subJarZoomDef).append(',');
            sb.append("worldWideZoomLevel:").append(worldWideZoomLevel).append(',');
            sb.append("maxZoomLevelInSubJars:").append(maxZoomLevelInSubJars).append(',');
            sb.append("tileExt:").append(tileExt);
            if (minx != 0) {
                sb.append(",").append("minx:").append(minx);
            }
            if (miny != 0) {
                sb.append(",").append("miny:").append(miny);
            }
            if (maxx >= 0) {
                sb.append(",").append("maxx:").append(maxx);
            }
            if (maxy >= 0) {
                sb.append(",").append("maxy:").append(maxy);
            }

            sb.append(']');

            return sb.toString();
        }

        protected void copyAndUpdateProperties(File sourceDir, File targetDir) {
            File propertiesFile = new File(sourceDir, StandardMapTileFactory.TILE_PROPERTIES);
            if (propertiesFile.exists()) {
                try {
                    URL propertiesURL = propertiesFile.toURI().toURL();

                    Properties props = new Properties();
                    props.load(propertiesURL.openStream());

                    props.put(StandardMapTileFactory.EMPTY_TILE_HANDLER_PROPERTY, WholeWorldTileHandler.class.getName());
                    props.put(SUBJAR_ZOOMLEVEL_PROPERTY, Integer.toString(subJarZoomDef));
                    props.put(WORLDWIDE_ZOOMLEVEL_PROPERTY, Integer.toString(worldWideZoomLevel));
                    props.put(PARENT_JAR_NAME_PROPERTY, sourceDir.getName() + ".jar");
                    props.put(StandardMapTileFactory.ROOT_DIR_PROPERTY, sourceDir.getName());

                    FileOutputStream fos = new FileOutputStream(new File(targetDir, StandardMapTileFactory.TILE_PROPERTIES));
                    props.store(fos, "Properties for " + targetDir + " tile set");
                    fos.close();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    logger.warning("can't find/read properties file for tile set");
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.warning("exception reading/copying properties file for tile set");
                }
            }
        }

        public void go() throws FileNotFoundException, IOException {

            if (targetFile == null) {
                targetFile = new File(sourceFile, sourceFile.getName());
            }

            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }

            File parentOfSourceFile = sourceFile.getParentFile();
            int numCharsTrimmedOffNameInZip = 0;
            if (parentOfSourceFile != null) {
                numCharsTrimmedOffNameInZip = parentOfSourceFile.getAbsolutePath().length() + 1;
            }

            logger.log(Level.INFO, "inspecting files in " + sourceFile + " ("
                    + (parentOfSourceFile == null ? "CAUTION: nothing"
                            : parentOfSourceFile.getAbsolutePath())
                    + " removed from names in jars).");

            copyAndUpdateProperties(sourceFile, targetFile);
            
            // Create the top-level worldwide jar
            if (doWorldJar) {
                List<File> jarDirs = new ArrayList<File>();

                for (int zoomLevel = 0; zoomLevel <= worldWideZoomLevel; zoomLevel++) {
                    File zoomLevelDir = new File(sourceFile, Integer.toString(zoomLevel));
                    if (zoomLevelDir.exists()) {
                        jarDirs.add(zoomLevelDir);
                    }
                }

                if (!jarDirs.isEmpty() && sourceFile != null) {
                    String worldWideJarFile = targetFile + File.separator + sourceFile.getName()
                            + ".jar";
                    logger.log(logLevel, "writing :" + worldWideJarFile);

                    if (fill) {
                        File fileCheck = new File(worldWideJarFile);
                        if (fileCheck.exists()) {
                            doWorldJar = false;
                        }
                    }

                    // after the fill check, check this again.
                    if (doWorldJar) {

                        FileOutputStream fos = new FileOutputStream(worldWideJarFile);
                        CheckedOutputStream checksum = new CheckedOutputStream(fos, new Adler32());
                        ZipOutputStream zoStream = new ZipOutputStream(new BufferedOutputStream(checksum));

                        // ZipOutputStream zoStream = new ZipOutputStream(fos);
                        // zoStream.setMethod(ZipOutputStream.DEFLATED);
                        for (File file : jarDirs) {
                            FileUtils.writeZipEntry(file, zoStream, numCharsTrimmedOffNameInZip);
                        }
                        zoStream.close();

                    } else {
                        logger.log(logLevel, worldWideJarFile + " already exists, skipping");
                    }
                }
            } else {
                logger.log(logLevel, "skipping world file");
            }
            // Worldwide jar created.

            // Now create subjars
            int dimensionForZoom = (int) Math.pow(2, subJarZoomDef);
            MapTileCoordinateTransform transform = new OSMMapTileCoordinateTransform();

            int startx = 0;
            int starty = 0;
            int endx = dimensionForZoom;
            int endy = dimensionForZoom;

            // OK, look at the minx, miny, maxx, maxy to figure out the jar
            // files to create.
            if (minx >= 0) {
                startx = minx;
            }

            if (maxx >= 0) {
                endx = Math.min(maxx + 1, dimensionForZoom);
            }

            if (miny >= 0) {
                starty = miny;
            }

            if (maxy >= 0) {
                endy = Math.min(maxy + 1, dimensionForZoom);
            }

            // x, y tile coordinates for subjar files.
            for (int x = startx; x < endx; x++) {
                for (int y = starty; y < endy; y++) {

                    // Calculate the lat/lon limits of the current tile
                    Point2D llp1 = transform.tileUVToLatLon(new Point2D.Double(x, y), subJarZoomDef);
                    Point2D llp2 = transform.tileUVToLatLon(new Point2D.Double(x + 1, y
                            + 2), subJarZoomDef);

                    File subJarFile = new File(targetFile, sourceFile.getName() + "_" + x + "_" + y
                            + ".jar");

                    if (fill && subJarFile.exists()) {
                        logger.log(logLevel, subJarFile + " already exists, skipping");
                        continue;
                    }

                    logger.log(logLevel, "Creating: " + subJarFile);
                    long fileCount = 0;
                    ZipOutputStream zoStream = null;

                    for (int zoomLevel = worldWideZoomLevel
                            + 1; zoomLevel <= maxZoomLevelInSubJars; zoomLevel++) {

                        File checkZoomLevelDir = new File(sourceFile, Integer.toString(zoomLevel));
                        if (checkZoomLevelDir.exists()) {

                            // These are the tile coordinate bounds for this
                            // subjar for this zoom level
                            Point2D uv1 = transform.latLonToTileUV(llp1, zoomLevel);
                            Point2D uv2 = transform.latLonToTileUV(llp2, zoomLevel);

                            logger.log(logLevel, "adding zoom level " + zoomLevel + " tiles to "
                                    + subJarFile.getName() + ": " + uv1 + "|" + uv2);

                            for (int u = (int) uv1.getX(); u < uv2.getX(); u++) {
                                for (int v = (int) uv1.getY(); v < uv2.getY(); v++) {
                                    // add file to subjar stream
                                    File tile = new File(sourceFile, zoomLevel + "/" + u + "/" + v
                                            + tileExt);
                                    if (tile.exists()) {

                                        if (zoStream == null) {
                                            /*
                                             * We need to do this because we
                                             * need to make sure a zip output
                                             * stream is created only when a
                                             * file is going to be written to
                                             * it. you can't create a zip file
                                             * and then put nothing into it.
                                             */
                                            @SuppressWarnings("resource")
                                            FileOutputStream fos = new FileOutputStream(subJarFile);

                                            CheckedOutputStream checksum = new CheckedOutputStream(fos, new Adler32());
                                            zoStream = new ZipOutputStream(new BufferedOutputStream(checksum));

                                            // zoStream = new
                                            // ZipOutputStream(fos);
                                            // zoStream.setMethod(ZipOutputStream.DEFLATED);
                                        }

                                        if (tile.getAbsolutePath().length()
                                                - numCharsTrimmedOffNameInZip > 0) {
                                            FileUtils.writeZipEntry(tile, zoStream, numCharsTrimmedOffNameInZip);
                                            fileCount++;
                                        } else {
                                            logger.info("Problem, there's something wrong with tile name: "
                                                    + tile.getAbsolutePath());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (zoStream != null) {
                        zoStream.close();
                        zoStream = null;
                        logger.log(logLevel, "closing zip file (" + subJarFile.getPath()
                                + "), added " + fileCount + " files to jar");
                    }
                }
            }
        }

        /**
         * Set the starting x number of the subjar file to create. Depends on
         * the subjar zoom to figure out what that means.
         * 
         * @param parseInt
         */
        public void minx(int parseInt) {
            minx = parseInt;
        }

        /**
         * Set the starting y number of the subjar file to create. Depends on
         * the subjar zoom to figure out what that means.
         * 
         * @param parseInt
         */
        public void miny(int parseInt) {
            miny = parseInt;
        }

        /**
         * Set the ending y number of the subjar file to create. Depends on the
         * subjar zoom to figure out what that means.
         * 
         * @param parseInt
         */
        public void maxx(int parseInt) {
            maxx = parseInt;
        }

        /**
         * Set the ending y number of the subjar file to create. Depends on the
         * subjar zoom to figure out what that means.
         * 
         * @param parseInt
         */
        public void maxy(int parseInt) {
            maxy = parseInt;
        }

        public void setDoWorldJar(boolean dwj) {
            doWorldJar = dwj;
        }

        /**
         * Check whether the build process will only create jars that don't
         * exist.
         */
        public boolean isFill() {
            return fill;
        }

        /**
         * Set whether the build process will only create jars that don't exist.
         * 
         * @param fill
         */
        public void setFill(boolean fill) {
            this.fill = fill;
        }
    }

    /**
     * Takes arguments for source tile directory, target directory, and option
     * sub-jar zoom level, and creates jars in the right place with expected
     * tiles. Prints usage statement.
     * 
     * @param args
     */
    public static void main(String[] args) {

        com.bbn.openmap.util.ArgParser ap = new com.bbn.openmap.util.ArgParser("WholeWorldTileHandler");

        ap.add(SOURCE, "Path to the tile root directory.", 1);
        ap.add(TARGET, "Path to the tile root directory for the jarred tile files.", 1);
        ap.add(SUB_JAR_ZOOM, "Zoom level tiles that subjar boundaries are based on (3 is default).", 1);
        ap.add(MAX_ZOOM_IN_SUBJARS, "Maximum tile zoom level added to sub jars (20 is default).", 1);
        ap.add(WORLD_WIDE_ZOOM_LEVEL, "Maximum tile zoom level to add to world wide jar (10 is default).", 1);
        ap.add(TILE_EXT, "Tile extension (.png is default).", 1);
        ap.add(MINX, "Subjar x minimum to create", 1);
        ap.add(MINY, "Subjar y minimum to create", 1);
        ap.add(MAXX, "Subjar x maximum to create", 1);
        ap.add(MAXY, "Subjar y maximum to create", 1);
        ap.add(NO_WORLD_JAR, "Don't create world level jar file");
        ap.add(VERBOSE, "Comment on what's going on");
        ap.add(FILL, "Just create jars that don't exist.");
        
        if (!ap.parse(args)) {
            ap.printUsage();
            System.exit(0);
        }

        String[] arg = ap.getArgValues(SOURCE);
        if (arg != null) {
            try {
                Builder wwthBuilder = new Builder(new File(arg[0]));

                arg = ap.getArgValues(TARGET);
                if (arg != null) {
                    wwthBuilder.targetFile(new File(arg[0]));
                }

                arg = ap.getArgValues(SUB_JAR_ZOOM);
                if (arg != null) {
                    wwthBuilder.subJarZoomDef(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues(MAX_ZOOM_IN_SUBJARS);
                if (arg != null) {
                    wwthBuilder.maxZoomLevelInSubJars(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues(WORLD_WIDE_ZOOM_LEVEL);
                if (arg != null) {
                    wwthBuilder.worldWideZoomLevel(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues(TILE_EXT);
                if (arg != null) {
                    wwthBuilder.tileExt(arg[0]);
                }

                arg = ap.getArgValues(MINX);
                if (arg != null) {
                    wwthBuilder.minx(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues(MINY);
                if (arg != null) {
                    wwthBuilder.miny(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues(MAXX);
                if (arg != null) {
                    wwthBuilder.maxx(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues(MAXY);
                if (arg != null) {
                    wwthBuilder.maxy(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues(VERBOSE);
                if (arg != null) {
                    wwthBuilder.logLevel = Level.INFO;
                }

                arg = ap.getArgValues(FILL);
                if (arg != null) {
                    wwthBuilder.setFill(true);
                }

                arg = ap.getArgValues(NO_WORLD_JAR);
                if (arg != null) {
                    logger.log(wwthBuilder.logLevel, "setting build world file to false");
                    wwthBuilder.setDoWorldJar(false);
                }

                System.out.println(wwthBuilder.toString());

                wwthBuilder.go();

            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                logger.warning(nfe.getMessage());
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                logger.warning(fnfe.getMessage());
            } catch (IOException ioe) {
                ioe.printStackTrace();
                logger.warning(ioe.getMessage());
            }
        } else {
            ap.bail("Need a source directory.", true);
        }

        System.exit(0);
    }
}
