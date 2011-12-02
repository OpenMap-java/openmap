/* 
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * 
 * @author ddietrick
 */
public class WholeWorldTileHandler
        extends ShpFileEmptyTileHandler {

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

    public WholeWorldTileHandler() {
        loadedJars = Collections.synchronizedSet(new HashSet<String>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.dataAccess.mapTile.EmptyTileHandler#getOMGraphicForEmptyTile
     * (java.lang.String, int, int, int,
     * com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform,
     * com.bbn.openmap.proj.Projection)
     */
    public BufferedImage getImageForEmptyTile(String imagePath, int x, int y, int zoomLevel,
                                              MapTileCoordinateTransform mtcTransform, Projection proj) {

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

            boolean newlyLoadedJar = false;

            String pathToJars = "";
            if (rootDirForJars != null && rootDirForJars.length() != 0) {
                pathToJars = rootDirForJars + "/";
            }

            if (zoomLevel <= worldWideZoomLevel) {
                /*
                 * Need to load the primary jar that has worldwide coverage.
                 */
                newlyLoadedJar = loadJar(pathToJars + parentJarName);
            } else {
                /*
                 * Need to figure out what the subjar name is, given the lat/lon
                 * coordinate of the tile.
                 */

                Point2D subframeUVPnt = mtcTransform.latLonToTileUV(tileUL, subFrameDefZoomLevel);
                newlyLoadedJar = loadJar(pathToJars + buildSubJarName(parentJarName, subframeUVPnt.getX(), subframeUVPnt.getY()));
            }

            if (newlyLoadedJar) {
                try {
                    URL imageURL = PropUtils.getResourceOrFileOrURL(imagePath);
                    if (imageURL != null) {
                        bi = BufferedImageHelper.getBufferedImage(imageURL);
                    } else {
                        logger.fine("Can't find resource located at " + imagePath);
                    }
                } catch (MalformedURLException e) {
                    logger.fine("Can't find resource located at " + imagePath);
                } catch (InterruptedException e) {
                    logger.fine("Reading the image file was interrupted: " + imagePath);
                }
            }
        } else {
            logger.fine("parent jar name not set, can't figure out how to load tile jars.");
        }

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
     * Load the jar if necessary.
     * 
     * @param jarFileName the absolute path to the jar.
     * @return true if the jar was loaded, false if it was added before and no
     *         attempt made.
     */
    protected boolean loadJar(String jarFileName) {

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

        subFrameDefZoomLevel = PropUtils.intFromProperties(props, prefix + SUBJAR_ZOOMLEVEL_PROPERTY, subFrameDefZoomLevel);
        worldWideZoomLevel = PropUtils.intFromProperties(props, prefix + WORLDWIDE_ZOOMLEVEL_PROPERTY, worldWideZoomLevel);
        rootDirForJars = props.getProperty(prefix + StandardMapTileFactory.ROOT_DIR_PATH_PROPERTY, rootDirForJars);

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

        public Builder(File source)
                throws FileNotFoundException {

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

        public Builder subJarZoomDef(int zoomLevel)
                throws NumberFormatException {
            if (checkZoomLevel(zoomLevel)) {
                this.subJarZoomDef = zoomLevel;
            }
            return this;
        }

        public Builder worldWideZoomLevel(int zoomLevel)
                throws NumberFormatException {
            if (checkZoomLevel(zoomLevel)) {
                this.worldWideZoomLevel = zoomLevel;
            }
            return this;
        }

        public Builder maxZoomLevelInSubJars(int zoomLevel)
                throws NumberFormatException {
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
            sb.append("tileExt:").append(tileExt).append(']');

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

        public void go()
                throws FileNotFoundException, IOException {

            if (targetFile == null) {
                targetFile = new File(sourceFile, sourceFile.getName());
            }

            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }

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
                    String worldWideJarFile = targetFile + File.separator + sourceFile.getName() + ".jar";
                    logger.info("writing :" + worldWideJarFile);

                    FileOutputStream fos = new FileOutputStream(worldWideJarFile);
                    CheckedOutputStream checksum = new CheckedOutputStream(fos, new Adler32());
                    ZipOutputStream zoStream = new ZipOutputStream(new BufferedOutputStream(checksum));

                    // ZipOutputStream zoStream = new ZipOutputStream(fos);
                    // zoStream.setMethod(ZipOutputStream.DEFLATED);
                    for (File file : jarDirs) {
                        int trim = sourceFile.getParent().length() + 1;
                        FileUtils.writeZipEntry(file, zoStream, trim);
                    }
                    zoStream.close();

                    copyAndUpdateProperties(sourceFile, targetFile);
                }
            } else {
                logger.info("skipping world file");
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
                    Point2D llp2 = transform.tileUVToLatLon(new Point2D.Double(x + 1, y + 2), subJarZoomDef);

                    File subJarFile = new File(targetFile, sourceFile.getName() + "_" + x + "_" + y + ".jar");

                    logger.info("Creating: " + subJarFile);
                    long fileCount = 0;
                    ZipOutputStream zoStream = null;
                    int trim = sourceFile.getParent().length() + 1;

                    for (int zoomLevel = worldWideZoomLevel + 1; zoomLevel <= maxZoomLevelInSubJars; zoomLevel++) {

                        File checkZoomLevelDir = new File(sourceFile, Integer.toString(zoomLevel));
                        if (checkZoomLevelDir.exists()) {

                            // These are the tile coordinate bounds for this
                            // subjar for this zoom level
                            Point2D uv1 = transform.latLonToTileUV(llp1, zoomLevel);
                            Point2D uv2 = transform.latLonToTileUV(llp2, zoomLevel);

                            for (int u = (int) uv1.getX(); u < uv2.getX(); u++) {
                                for (int v = (int) uv1.getY(); v < uv2.getY(); v++) {
                                    // add file to subjar stream
                                    File tile = new File(sourceFile, zoomLevel + "/" + u + "/" + v + tileExt);
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
                                            FileOutputStream fos = new FileOutputStream(subJarFile);

                                            CheckedOutputStream checksum = new CheckedOutputStream(fos, new Adler32());
                                            zoStream = new ZipOutputStream(new BufferedOutputStream(checksum));

                                            // zoStream = new
                                            // ZipOutputStream(fos);
                                            // zoStream.setMethod(ZipOutputStream.DEFLATED);
                                        }

                                        if (tile.getAbsolutePath().length() - trim > 0) {
                                            FileUtils.writeZipEntry(tile, zoStream, trim);
                                            fileCount++;
                                        } else {
                                            logger.info("here's the problem");
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (zoStream != null) {
                        zoStream.close();
                        zoStream = null;
                        logger.info("closing zip file (" + subJarFile.getPath() + "), added " + fileCount + " files to jar");
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

        ap.add("source", "Path to the tile root directory.", 1);
        ap.add("target", "Path to the tile root directory for the jarred tile files.", 1);
        ap.add("subJarZoom", "Zoom level tiles that subjar boundaries are based on (3 is default).", 1);
        ap.add("maxZoomInSubJars", "Maximum tile zoom level added to sub jars (20 is default).", 1);
        ap.add("worldWideZoomLevel", "Maximum tile zoom level to add to world wide jar (10 is default).", 1);
        ap.add("tileExt", "Tile extension (.png is default).", 1);
        ap.add("minx", "Subjar x minimum to create", 1);
        ap.add("miny", "Subjar y minimum to create", 1);
        ap.add("maxx", "Subjar x maximum to create", 1);
        ap.add("maxy", "Subjar y maximum to create", 1);
        ap.add("noWorldJar", "Don't create world level jar file");
        ap.add("verbose", "Comment on what's going on");

        if (!ap.parse(args)) {
            ap.printUsage();
            System.exit(0);
        }

        String[] arg = ap.getArgValues("source");
        if (arg != null) {
            try {
                Builder wwthBuilder = new Builder(new File(arg[0]));

                arg = ap.getArgValues("target");
                if (arg != null) {
                    wwthBuilder.targetFile(new File(arg[0]));
                }

                arg = ap.getArgValues("subJarZoom");
                if (arg != null) {
                    wwthBuilder.subJarZoomDef(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues("maxZoomInSubJars");
                if (arg != null) {
                    wwthBuilder.maxZoomLevelInSubJars(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues("worldWideZoomLevel");
                if (arg != null) {
                    wwthBuilder.worldWideZoomLevel(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues("tileExt");
                if (arg != null) {
                    wwthBuilder.tileExt(arg[0]);
                }

                arg = ap.getArgValues("minx");
                if (arg != null) {
                    wwthBuilder.minx(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues("miny");
                if (arg != null) {
                    wwthBuilder.miny(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues("maxx");
                if (arg != null) {
                    wwthBuilder.maxx(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues("maxy");
                if (arg != null) {
                    wwthBuilder.maxy(Integer.parseInt(arg[0]));
                }

                arg = ap.getArgValues("verbose");
                if (arg != null) {
                    logger.setLevel(Level.FINE);
                }

                arg = ap.getArgValues("noWorldJar");
                if (arg != null) {
                    logger.info("setting build world file to false");
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
