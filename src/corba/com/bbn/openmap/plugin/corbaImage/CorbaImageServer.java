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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/plugin/corbaImage/CorbaImageServer.java,v $
// $RCSfile: CorbaImageServer.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/11 19:30:01 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.corbaImage;

import java.io.IOException;
import java.util.Properties;

import com.bbn.openmap.image.MapRequestHandler;
import com.bbn.openmap.plugin.corbaImage.corbaImageServer.ServerPOA;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.corba.CORBASupport;

/**
 * The CorbaImageServer is a CORBA implementation of a server that
 * provides images of maps. The request is the same as the meat of the
 * SimpleHttpImageServer, without the http references. The
 * CorbaImageServer uses a MapRequestHandler to create the image to
 * return to the client. The server is configured with an
 * openmap.properties file with the additional ImageServer properties
 * for formatters and layers.
 * <p>
 * 
 * This client-server has been tested with Visibroker 3.4.
 * 
 * @see com.bbn.openmap.image.MapRequestHandler
 * @see com.bbn.openmap.image.SimpleHttpImageServer
 */
public class CorbaImageServer extends ServerPOA {

    protected static String iorfile = null;
    protected static String naming = null;

    public final static String ClassPropertyName = "class";
    protected MapRequestHandler map = null;

    /**
     * Default Constructor.
     */
    public CorbaImageServer() {
        this("Default");
    }

    /**
     * The constructor that you should use.
     * 
     * @param name the identifying name for persistance.
     */
    public CorbaImageServer(String name) {
        super();
    }

    /**
     * Retrieve the subframe data from the frame cache, decompress it,
     * and convert it to an image.
     * 
     * @param request a sequence of key/value pairs from the
     *        client, stating preferences.
     * @return byte[] of image
     */
    public byte[] getImage(String request) {

        Debug.message("cis",
                "CorbaImageServer: handling subframe request for client");

        if (map == null) {
            Debug.error("CorbaImageServer not configured for getting data!  No data source");
            return new byte[0];
        }

        byte[] imageData = new byte[0];

        try {
            imageData = map.handleRequest(request);
        } catch (IOException ioe) {
            Debug.error("CorbaImageServer: IOException processing: " + request);
        }

        if (imageData == null) {
            // If something went wrong, lets send something safe.
            Debug.message("cis",
                    "CorbaImageServer: something went wrong with image creation!");
            imageData = new byte[0];
        }

        Debug.message("cis", "CorbaImageServer: returning image of length: "
                + imageData.length);
        return imageData;
    }

    /**
     * Start the server.
     * 
     * @param args command line arguments.
     */
    public void start(String[] args) {

        CORBASupport cs = new CORBASupport();

        if (args != null) {
            parseArgs(args);
        }

        cs.start(this, args, iorfile, naming);
    }

    /**
     */
    public void parseArgs(String[] args) {
        Properties properties = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-ior")) {
                iorfile = args[++i];
            } else if (args[i].equalsIgnoreCase("-name")) {
                naming = args[++i];
            } else if (args[i].equalsIgnoreCase("-help")) {
                printHelp();
            } else if (args[i].equalsIgnoreCase("-verbose")) {
                Debug.put("cis");
                Debug.put("imageserver");
                Debug.put("formatter");
            } else if (args[i].equalsIgnoreCase("-properties")) {

                String propLocation = null;
                propLocation = args[++i];
                Debug.message("cis",
                        "CorbaImageServer getting properties from "
                                + propLocation);
                properties = loadProps(propLocation);
            } else if (args[i].equalsIgnoreCase("-h")) {
                printHelp();
            }
        }

        // if you didn't specify an iorfile
        if (iorfile == null && naming == null) {
            Debug.error("CorbaImageServer: IOR file and name service name are null!  Use `-ior' or '-name' flag!");
            System.exit(-1);
        }

        if (properties == null) {
            Debug.error("CorbaImageServer: No properties file for server specified!  Use `-properties' flag and a properties file suitable for MapRequestHandler!");
            System.exit(-1);
        } else {
            try {
                map = new MapRequestHandler(properties);
                Debug.output("CorbaImageServer: CorbaImageServer!  Running with properties => "
                        + properties);
            } catch (IOException ioe) {
                Debug.error("CorbaImageServer caught IOException while loading properties into the MapRequestHandler.");
                map = null;
            }
        }
    }

    /**
     * Load the named file from the named directory into the given
     * <code>Properties</code> instance. If the file is not found a
     * warning is issued. If an IOExceptio occurs, a fatal error is
     * printed and the application will exit.
     * 
     * @param file the name of the file
     * @return the loaded properties
     */
    public Properties loadProps(String file) {
        java.io.File propsFile = new java.io.File(file);
        Properties props = new Properties();
        try {
            java.io.InputStream propsStream = new java.io.FileInputStream(propsFile);
            props.load(propsStream);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("CorbaImageServer did not find properties file: \""
                    + file + "\"");
            System.exit(1);
        } catch (java.io.IOException e) {
            System.err.println("Caught IO Exception reading configuration file \""
                    + propsFile + "\"");
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

    /**
     * <b>printHelp </b> should print a usage statement which reflects
     * the command line needs of your specialist.
     */
    public void printHelp() {
        Debug.output("usage: java CorbaImageServer [-ior <file> || -name <NAME>] -properties \"<path to properties file>\"");
        System.exit(1);
    }

    public static void main(String[] args) {
        Debug.init(System.getProperties());

        // Create the specialist server
        CorbaImageServer srv = new CorbaImageServer("CorbaImageServer");
        srv.start(args);
    }

}