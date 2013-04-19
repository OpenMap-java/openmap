package com.bbn.openmap.dataAccess.mapTile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import javax.swing.ImageIcon;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.FileUtils;

/**
 * A utility class to help manage tile trees. Use the builders to configure and
 * launch the MapTileUtil.
 * 
 * @author dietrick
 */
public class MapTileUtil {

    static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile");

    public final static String SOURCE_PROPERTY = "source";
    public final static String BOUNDS_PROPERTY = "bounds";
    public final static String DESTINATION_PROPERTY = "destination";
    public final static String IMAGEFORMAT_PROPERTY = "format";
    public final static String ZOOMLEVEL_PROPERTY = "zoom";

    public final static int ZOOM_LEVELS = 21;

    MapTileCoordinateTransform mtcTransform;
    List<double[]> boundsList;
    boolean[] zoomLevels;

    public MapTileUtil(Action builder) {
        boundsList = builder.boundsList;
        zoomLevels = builder.zoomLevels;
        mtcTransform = builder.mtcTransform;
    }

    /**
     * Figure out which tiles need action, based on settings. Calls
     * Action.action() for each tile on the builder. Designed to be called from
     * Action.go().
     * 
     * @param builder
     */
    public void grabTiles(Action builder) {

        if (boundsList == null) {
            boundsList = new ArrayList<double[]>();
            boundsList.add(new double[] {
                80,
                -180,
                -80,
                180
            });
        }

        for (int i = 0; i < ZOOM_LEVELS; i++) {

            // Check the zoom level. If they aren't specified, only do 0-14
            if (zoomLevels == null) {
                if (i > 14)
                    continue;
            } else {
                if (!zoomLevels[i]) {
                    continue;
                }
            }

            for (double[] bounds : boundsList) {

                int[] uvBounds =
                        mtcTransform.getTileBoundsForProjection(new LatLonPoint.Double(bounds[0], bounds[1]),
                                                                new LatLonPoint.Double(bounds[2], bounds[3]), i);
                int uvup = uvBounds[0];
                int uvleft = uvBounds[1];
                int uvbottom = uvBounds[2];
                int uvright = uvBounds[3];

                int uvleftM = (int) Math.min(uvleft, uvright);
                int uvrightM = (int) Math.max(uvleft, uvright);
                int uvupM = (int) Math.min(uvbottom, uvup);
                int uvbottomM = (int) Math.max(uvbottom, uvup);

                for (int x = uvleftM; x < uvrightM; x++) {
                    for (int y = uvupM; y < uvbottomM; y++) {
                        builder.action(x, y, i, this);
                    }
                }
            }
        }
    }

    /**
     * For instance...
     * 
     * @param args
     */
    public static void main(String[] args) {

        // new URLGrabber("http://tah.openstreetmap.org/Tiles/tile", "/data/tiles").addZoomRange(0, 5).go();

        // new Copy("/data/sourcetiles", "/data/desttiles").addZoom(17).addBounds(7.8696, 2.324, 2.899, 9.053).go();

        new Jar("/data/sourcetiles", "/data/dest.jar").addZoomRange(0,17).addBounds(14.042,2.498,.809,15.215).go();
    }

    /**
     * A generic builder Action that handles most configuration issues for the
     * MapTileUtil. Extend to make MTU do what you want by overriding go and
     * action.
     * 
     * @author dietrick
     */
    public abstract static class Action {
        String source;
        String destination;

        // Optional
        String format = "png";
        List<double[]> boundsList;
        boolean[] zoomLevels; // 0-20
        MapTileCoordinateTransform mtcTransform = new OSMMapTileCoordinateTransform();

        public Action(String source, String destination) {
            this.source = source;
            this.destination = destination;
        }

        public Action addBounds(double ulat, double llon, double llat, double rlon) {
            if (boundsList == null) {
                boundsList = new ArrayList<double[]>();
            }

            double[] bnds = new double[] {
                ulat,
                llon,
                llat,
                rlon
            };

            boundsList.add(bnds);
            return this;
        }

        public Action addZoom(int zoom) {
            if (zoomLevels == null) {
                zoomLevels = new boolean[ZOOM_LEVELS];
            }

            try {
                zoomLevels[zoom] = true;
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                logger.warning("zoom level invalid, ignoring: " + zoom);
            }
            return this;
        }

        public Action addZoomRange(int zoom1, int zoom2) {
            int min = Math.min(zoom1, zoom2);
            int max = Math.max(zoom1, zoom2);
            for (int z = min; z <= max; z++) {
                addZoom(z);
            }
            return this;
        }

        public Action format(String format) {
            this.format = format;
            return this;
        }

        public Action transform(MapTileCoordinateTransform transform) {
            mtcTransform = transform;
            return this;
        }

        public abstract void go();

        /**
         * Called from within grabTiles, with the tile info. You can use this
         * information to make a method call on mtu.
         * 
         * @param x tile coordinate
         * @param y tile coordinate
         * @param zoomLevel tile zoom level
         * @param mtu callback
         */
        public abstract void action(int x, int y, int zoomLevel, MapTileUtil mtu);

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public List<double[]> getBoundsList() {
            return boundsList;
        }

        public void setBoundsList(List<double[]> boundsList) {
            this.boundsList = boundsList;
        }

        public boolean[] getZoomLevels() {
            return zoomLevels;
        }

        public void setZoomLevels(boolean[] zoomLevels) {
            this.zoomLevels = zoomLevels;
        }

        public MapTileCoordinateTransform getMtcTransform() {
            return mtcTransform;
        }

        public void setMtcTransform(MapTileCoordinateTransform mtcTransform) {
            this.mtcTransform = mtcTransform;
        }
    }

    /**
     * Action that copies tiles from one directory to another.
     * 
     * @author dietrick
     */
    public static class Copy
            extends Action {

        public Copy(String source, String destination) {
            super(source, destination);
        }

        public void go() {
            if (source != null && destination != null) {
                new MapTileUtil(this).grabTiles(this);
            } else {
                logger.warning("Need a source and destination for tile locations");
            }
        }

        public void action(int x, int y, int zoomLevel, MapTileUtil mtu) {
            File sourceFile = new File(getSource() + "/" + zoomLevel + "/" + x + "/" + y + "." + format);
            File destDir = new File(getDestination() + "/" + zoomLevel + "/" + x);
            destDir.mkdirs();

            File destFile = new File(destDir, y + "." + format);

            try {
                if (sourceFile.exists()) {
                    FileUtils.copy(sourceFile, destFile, 1024);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Action that creates a jar file containing the specified files.
     * 
     * @author dietrick
     */
    public static class Jar
            extends Action {

        /** Will get instantiated if needed */
        ZipOutputStream zoStream = null;
        File destinationFile = null;
        long fileCount = 0;
        int zipTrim = 0;

        public Jar(String source, String destination) {
            super(source, destination);
        }

        public void go() {
            if (source != null && destination != null) {
                new MapTileUtil(this).grabTiles(this);

                if (zoStream != null) {
                    try {
                        zoStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
                logger.info("Created " + getDestination() + " with " + fileCount + " files.");

            } else {
                logger.warning("Need a source and destination for tile locations");
            }
        }

        public void action(int x, int y, int zoomLevel, MapTileUtil mtu) {
            File tile = new File(getSource() + "/" + zoomLevel + "/" + x + "/" + y + "." + format);

            if (tile.exists()) {
                try {
                    if (zoStream == null) {
                        /*
                         * We need to do this because we need to make sure a zip
                         * output stream is created only when a file is going to
                         * be written to it. you can't create a zip file and
                         * then put nothing into it.
                         */
                        destinationFile = new File(getDestination());
                        FileOutputStream fos = new FileOutputStream(destinationFile);
                        zoStream = new ZipOutputStream(fos);
                        zipTrim = destinationFile.getParent().length() + 1;
                        
                        logger.info("creating " + destinationFile);
                        
                        File sourceParentFile = new File(getSource()).getParentFile();
                        if (sourceParentFile.exists()) {
                            File tileDescription = new File(sourceParentFile, StandardMapTileFactory.TILE_PROPERTIES);
                            if (tileDescription.exists()) {
                                FileUtils.writeZipEntry(tileDescription, zoStream, zipTrim);
                                fileCount++;
                                logger.info("adding " + tileDescription);
                            }
                        }
                    }

                    FileUtils.writeZipEntry(tile, zoStream, zipTrim);
                    fileCount++;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

        }
    }

    /**
     * A Builder that knows how to download files from a website.
     * 
     * @author dietrick
     */
    public static class URLGrabber
            extends Action {

        public URLGrabber(String source, String destination) {
            super(source, destination);
        }

        public void go() {
            if (source != null && destination != null) {
                new MapTileUtil(this).grabTiles(this);
            } else {
                logger.warning("Need a source and destination for tile locations");
            }
        }

        public void action(int x, int y, int zoomLevel, MapTileUtil mtu) {
            grabURLTile(x, y, zoomLevel);
        }

        /**
         * An action method that will fetch a tile from a URL and copy it to the
         * destination directory.
         * 
         * @param x
         * @param y
         * @param zoomLevel
         */
        public void grabURLTile(int x, int y, int zoomLevel) {

            java.net.URL url = null;

            String imagePath = source + "/" + zoomLevel + "/" + x + "/" + y + (format.startsWith(".") ? format : "." + format);

            try {

                url = new java.net.URL(imagePath);
                java.net.HttpURLConnection urlc = (java.net.HttpURLConnection) url.openConnection();

                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("url content type: " + urlc.getContentType());
                }

                if (urlc == null) {
                    logger.warning("unable to connect to " + imagePath);
                    return;
                }

                if (urlc.getContentType().startsWith("image")) {

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

                    byte[] imageBytes = out.toByteArray();

                    if (destination != null) {
                        File localFile =
                                new File(destination + "/" + zoomLevel + "/" + x + "/" + y
                                        + (format.startsWith(".") ? format : "." + format));

                        File parentDir = localFile.getParentFile();
                        parentDir.mkdirs();

                        FileOutputStream fos = new FileOutputStream(localFile);
                        fos.write(imageBytes);
                        fos.flush();
                        fos.close();
                    }

                } // end if image
            } catch (java.net.MalformedURLException murle) {
                logger.warning("WebImagePlugIn: URL \"" + imagePath + "\" is malformed.");
            } catch (java.io.IOException ioe) {
                logger.warning("Couldn't connect to " + imagePath + "Connection Problem");
            }

        }

    }

}
