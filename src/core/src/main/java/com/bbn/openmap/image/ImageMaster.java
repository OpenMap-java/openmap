// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageMaster.java,v $
// $RCSfile: ImageMaster.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The ImageMaster is an organizer for running the ImageServer to create one or
 * more images. It relies on a properties file, which sets up a series of
 * entries for an ImageServer. Each entry has parameters for setting up a
 * projection for an image, a parameters for a URL for the ImageServer to use to
 * set up the layers for an image, and a parameter to set the name and path of
 * the output image file.
 * <P>
 * Each map entry in the ImageServer has parameters for the projection and layer
 * properties to use for the map image, and the size, location and format of the
 * output image.
 */
public class ImageMaster {

    /** Property for space separated image servers to be created. */
    public static final String ImageServersProperty = "servers";
    /**
     * Property for the properties file holding property for a particular image.
     */
    public static final String ServerPropertiesProperty = "properties";
    /** Property for an image's projection type. */
    public static final String ImageProjectionProperty = "imageProjection";
    /** Property for an image's projection center latitude. */
    public static final String ImageLatitudeProperty = "imageLatitude";
    /** Property for an image's projection center longitude. */
    public static final String ImageLongitudeProperty = "imageLongitude";
    /** Property for an image's projection scale. */
    public static final String ImageScaleProperty = "imageScale";
    /** Property for an image's height. */
    public static final String ImageHeightProperty = "imageHeight";
    /** Property for an image's width. */
    public static final String ImageWidthProperty = "imageWidth";
    /** Property for the image's background color. */
    public static final String ImageBackgroundColorProperty = "imageBackgroundColor";
    /** Property for an image's output name. */
    public static final String ImageNameProperty = "outputName";
    /** Property for scaling the width of image after creation. */
    public static final String ScaleToWidthProperty = "scaleToWidth";
    /** Property for scaling the height of image after creation. */
    public static final String ScaleToHeightProperty = "scaleToHeight";
    /** Property for an output log file. */
    public static final String OutputLogFileProperty = "outputLogFile";
    /** Property for an error log file. */
    public static final String ErrorLogFileProperty = "errorLogFile";
    /**
     * Hashtable of instantiated layers across servers, to reduce duplication of
     * same layers.
     */
    protected Hashtable instantiatedLayers = new Hashtable();

    ImageMasterHelper[] helpers;

    /** Create with properties. */
    public ImageMaster(Properties props) {
        setProperties(props);
    }

    /** Create with properties file. */
    public ImageMaster(String propertiesFile) {
        Properties props = new Properties();
        loadProperties(props, propertiesFile);
        setProperties(props);
    }

    /** Create with properties file URL. */
    public ImageMaster(URL propertiesURL) {
        Properties props = new Properties();
        loadProperties(props, propertiesURL);
        setProperties(props);
    }

    /**
     * Loads properties from a java resource. This will load the named resource
     * identifier into the given properties instance.
     * 
     * @param props the Properties instance to receive the properties
     * @param resourceName the name of the resource to load
     * @return true if all's well.
     */
    protected boolean loadPropertiesFromResource(Properties props, String resourceName) {

        InputStream propsIn = getClass().getResourceAsStream(resourceName);

        if (propsIn == null) {
            if (Debug.debugging("imagemaster")) {
                Debug.error("Unable to locate resources: " + resourceName);
            }
            return false;
        } else {
            try {
                props.load(propsIn);
                return true;
            } catch (java.io.IOException e) {
                Debug.error("ImageMaster: Caught IOException loading resources: " + resourceName);
                return false;
            }
        }
    }

    /**
     * Loads properties from a java resource. This will load the named resource
     * identifier into the given properties instance.
     * 
     * @param props the Properties instance to receive the properties
     * @param url the url to load
     * @return true if all's well.
     */
    public boolean loadProperties(Properties props, URL url) {
        try {
            InputStream propsIn = url.openStream();
            props.load(propsIn);
            return true;
        } catch (java.io.IOException e) {
            Debug.error("ImageMaster: Caught IOException loading resources: " + url);
            return false;
        }
    }

    /**
     * Load the named file from the named directory into the given
     * <code>Properties</code> instance. If the file is not found a warning is
     * issued. If an IOException occurs, a fatal error is printed and the
     * application will exit.
     * 
     * @param file the name of the file
     * @return true if all's well.
     */
    public boolean loadProperties(Properties props, String file) {
        File propsFile = new File(file);
        try {
            InputStream propsStream = new FileInputStream(propsFile);
            props.load(propsStream);
            return true;
        } catch (java.io.FileNotFoundException e) {
            Debug.error("ImageMaster.loadProperties(): Unable to read configuration file \"" + propsFile + "\"");
        } catch (java.io.IOException e) {
            Debug.error("ImageMaster.loadProperties(): Caught IO Exception reading configuration file \"" + propsFile + "\" \n" + e);
        }
        return false;
    }

    /**
     * Set the properties for the ImageMaster, which also gets all the
     * ImageMasterHelpers created.
     */
    public void setProperties(Properties properties) {
        helpers = setImageServers(properties);
    }

    /** Start the ImageMaster to go through the ImageMasterHelpers. */
    public void run() {
        doNext();
    }

    /**
     * This causes the ImageMaster to look through the list of
     * ImageMasterHelpers and launch the next one that hasn't been completed. It
     * will cause the program to exit if there is nothing more to do.
     */
    protected void doNext() {
        for (int i = 0; i < helpers.length; i++) {
            if (!helpers[i].complete) {
                helpers[i].create();
                return;
            }
        }
        System.exit(0);
    }

    /**
     * Creates the ImageMasterHelper array from an ImageMaster properties
     * object. After this method is called, call run() to start the servers on
     * their creative ways.
     * 
     * @param properties the ImageMaster properties.
     * @return ImageMasterHelper array.
     */
    public ImageMasterHelper[] setImageServers(Properties properties) {

        String serversValue = properties.getProperty(ImageServersProperty);

        if (Debug.debugging("imagemaster")) {
            Debug.output("ImageMaster.setImageServers(): servers = \"" + serversValue + "\"");
        }

        if (serversValue == null) {
            Debug.error("ImageMaster.setImageServers(): No property \"" + ImageServersProperty
                    + "\" found in application properties.");
            return new ImageMasterHelper[0];
        }

        // Divide up the names ...
        StringTokenizer tokens = new StringTokenizer(serversValue, " ");
        Vector serverNames = new Vector();
        while (tokens.hasMoreTokens()) {
            serverNames.addElement(tokens.nextToken());
        }

        if (Debug.debugging("imagemaster")) {
            Debug.output("ImageMaster.setImageServers(): " + serverNames);
        }

        int nServerNames = serverNames.size();
        ImageMasterHelper[] masterHelpers = new ImageMasterHelper[nServerNames];

        for (int i = 0; i < nServerNames; i++) {
            String serverName = (String) serverNames.elementAt(i);
            masterHelpers[i] = new ImageMasterHelper(serverName, properties, this);

        }
        return masterHelpers;
    }

    /**
     * Start up and go.
     */
    public static void main(String[] args) {
        Debug.init();
        ImageMaster master = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-file")) {
                master = new ImageMaster(args[++i]);
            } else if (args[i].equalsIgnoreCase("-url")) {
                String url = null;
                try {
                    url = args[++i];
                    master = new ImageMaster(new URL(url));
                } catch (MalformedURLException mue) {
                    Debug.output("ImageMaster: Malformed URL: " + url);
                    master = null;
                }
            } else if (args[i].equalsIgnoreCase("-masterprops")) {
                printMasterProps();
            } else if (args[i].equalsIgnoreCase("-serverprops")) {
                printServerProps();
            } else if (args[i].equalsIgnoreCase("-h")) {
                printHelp();
            }
        }

        if (master != null) {
            master.run();
        } else {
            printHelp();
        }
    }

    /**
     * <b>printHelp </b> should print a usage statement which reflects the
     * command line needs of the ImageServer.
     */
    public static void printHelp() {
        Debug.output("");
        Debug.output("usage: java com.bbn.openmap.image.ImageMaster [-file <properties file> || -url <properties file>] [-masterprops || -serverprops");
        Debug.output("     -file requires a complete path to a ImageMaster properties file.");
        Debug.output("     -url requires an URL to a ImageMaster properties file.");
        Debug.output("     -masterprops prints out an example of a ImageMaster properties file.");
        Debug.output("     -serverprops prints out an example of a ImageServer properties file.");
        Debug.output("");

        System.exit(1);
    }

    /**
     * Prints an example of the ImageMaster properties file.
     */
    public static void printMasterProps() {
        Debug.output("");
        Debug.output("#################################################");
        Debug.output("# Properties file for the ImageMaster");
        Debug.output("# List of unique server nicknames (your choice).");
        Debug.output("servers=<server1> <server2> <server3> <etc>");
        Debug.output("");
        Debug.output("# URL of server1 properties");
        Debug.output("# If this is not included, it is assumed that ");
        Debug.output("# the ImageServer properties reside in the ");
        Debug.output("# ImageMaster properties file.");
        Debug.output("server1.properties=http://<url to server1 properties>");
        Debug.output("# Projection type of server1 image.");
        Debug.output("server1.imageProjection=mercator");
        Debug.output("# Center latitude of server1 image.");
        Debug.output("server1.imageLatitude=40f");
        Debug.output("# Center longitude of server1 image.");
        Debug.output("server1.imageLongitude=-72f");
        Debug.output("# Projection scale of server1 image.");
        Debug.output("server1.imageScale=20000000");
        Debug.output("# Pixel height of server1 image.");
        Debug.output("server1.imageHeight=640");
        Debug.output("# Pixel width of server1 image.");
        Debug.output("server1.imageWidth=480");
        Debug.output("# ARGB representation of the map background color (default is a saucy blue)");
        Debug.output("server1.imageBackgroundColor=ffffffff");
        Debug.output("# Complete path to server1 image output.");
        Debug.output("server1.outputName=<path to output file>");
        Debug.output("");
        Debug.output("# Repeat for each server listed in the servers property");
        Debug.output("#################################################");
        Debug.output("");
    }

    /**
     * Print the ImageServer properties file, referenced by the ImageMaster
     * properties file.
     */
    public static void printServerProps() {
        Debug.output("");
        Debug.output("#################################################");
        Debug.output("# Properties for ImageServer");
        Debug.output("# List of unique layer nicknames to use for the image (your choice).");
        Debug.output("# server1 is the name specified in the ImageMaster properties.");
        Debug.output("server1.imageServer.layers=<layer1> <layer2> <etc>");
        Debug.output("# Classname of object to determine image format.");
        Debug.output("server1.imageServer.formatter=<classname of ImageFormatter>");
        Debug.output("");
        Debug.output("layer1.class=<com.bbn.openmap.layer.ShapeLayer");
        Debug.output("layer1.prettyName=ShapeLayer");
        Debug.output("# Continue with layer specific properties.  See each layer's documentation or source for more details.");
        Debug.output("");
        Debug.output("# Continue for each layer listed in the imageServer.layers property.");
        Debug.output("#################################################");
        Debug.output("");
    }

    /**
     * The ImageMasterHelper contains an ImageServer, and acts like the
     * ImageReceiver to create the Image file when the bits are ready.
     */
    public class ImageMasterHelper
            implements ImageReceiver {
        public ImageServer iServer;
        public String outputFileName;
        public boolean complete = false;
        public ImageMaster iMaster;
        public Proj proj;
        public String outputLogFileName;
        public String errorLogFileName;
        public int scaleToWidth = -1;
        public int scaleToHeight = -1;

        public ImageMasterHelper(String prefix, Properties props, ImageMaster master) {

            String propPrefix = prefix + ".";

            float scale = PropUtils.floatFromProperties(props, propPrefix + ImageScaleProperty, 20000000f);
            int height = PropUtils.intFromProperties(props, propPrefix + ImageHeightProperty, 480);
            int width = PropUtils.intFromProperties(props, propPrefix + ImageWidthProperty, 640);
            float longitude = PropUtils.floatFromProperties(props, propPrefix + ImageLongitudeProperty, -71f);
            float latitude = PropUtils.floatFromProperties(props, propPrefix + ImageLatitudeProperty, 42f);
            String projType = props.getProperty(propPrefix + ImageProjectionProperty);
            String uniquePropsURL = props.getProperty(propPrefix + ServerPropertiesProperty);
            scaleToWidth = PropUtils.intFromProperties(props, propPrefix + ScaleToWidthProperty, -1);
            scaleToHeight = PropUtils.intFromProperties(props, propPrefix + ScaleToHeightProperty, -1);

            outputFileName = props.getProperty(propPrefix + ImageNameProperty);
            outputLogFileName = props.getProperty(propPrefix + OutputLogFileProperty);
            errorLogFileName = props.getProperty(propPrefix + ErrorLogFileProperty);

            if (outputLogFileName != null && errorLogFileName != null) {
                if (outputLogFileName.equalsIgnoreCase(errorLogFileName)) {
                    Debug.setLog(new File(outputFileName), true);
                } else {
                    Debug.directErrors(errorLogFileName, true, true);
                    Debug.directOutput(new File(outputLogFileName), true);
                }
            } else {
                if (errorLogFileName != null) {
                    Debug.directErrors(errorLogFileName, true, true);
                }
                if (outputLogFileName != null) {
                    Debug.directOutput(new File(outputLogFileName), true);
                }
            }

            ProjectionFactory projectionFactory = getProjectionFactory();

            Class<? extends Projection> projClass = projectionFactory.getProjClassForName(projType);
            if (projClass == null) {
                projClass = Mercator.class;
            }

            proj = (Proj) projectionFactory.makeProjection(projClass, new Point2D.Float(latitude, longitude), scale, width, height);

            // Set the background color of the map
            Color background =
                    (Color) PropUtils.parseColorFromProperties(props, propPrefix + ImageBackgroundColorProperty,
                                                               MapBean.DEFAULT_BACKGROUND_COLOR);

            iMaster = master;

            Properties uniqueProps;
            // if there isn't a unique properties file designated for
            // the imageserver layers, assume that the layer properties
            // reside in the ImageMaster properties file.

            if (uniquePropsURL != null) {
                uniqueProps = new Properties();
                try {
                    loadProperties(uniqueProps, new URL(uniquePropsURL));
                } catch (MalformedURLException mue) {
                    Debug.error("ImageMaster: Malformed URL for server properties: " + uniquePropsURL);
                    uniqueProps = null;
                }
            } else {
                uniqueProps = props;
            }

            if (uniqueProps != null && outputFileName != null) {
                iServer = new ImageServer(propPrefix, uniqueProps, instantiatedLayers);
                iServer.setBackground(background);
            }
        }

        /**
         * Override this method if you want to change the available projections
         * from the defaults.
         * 
         * @return the ProjectionFactory with projections
         */
        public ProjectionFactory getProjectionFactory() {
            return ProjectionFactory.loadDefaultProjections();
        }

        /**
         * Start the ImageServer on it's creative journey.
         */
        public void create() {
            receiveImageData(iServer.createImage(proj, scaleToWidth, scaleToHeight));
        }

        /**
         * Receive the bytes from a image. ImageReceiver interface function.
         * 
         * @param imageBytes the formatted image..
         */
        public void receiveImageData(byte[] imageBytes) {
            writeDataFile(outputFileName, imageBytes);
            complete = true;
            iMaster.doNext();
        }

        /**
         * Write the image to a file.
         * 
         * @param fileName the file name to write the image into.
         * @param imageData the image data to put in the file.
         */
        public void writeDataFile(String fileName, byte[] imageData) {
            try {
                Debug.message("imagemaster", "ImageMasterHelper: Writing image file " + fileName);

                FileOutputStream binFile = new FileOutputStream(fileName);
                binFile.write(imageData);
                binFile.close();

            } catch (IOException ioe) {
                Debug.error("ImageMasterHelper: Error writing image file " + fileName);
            }
        }
    }
}