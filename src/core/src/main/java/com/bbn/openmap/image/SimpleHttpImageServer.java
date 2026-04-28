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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/SimpleHttpImageServer.java,v $
// $RCSfile: SimpleHttpImageServer.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:05:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.http.HttpConnection;
import com.bbn.openmap.util.http.HttpRequestEvent;
import com.bbn.openmap.util.http.HttpRequestListener;
import com.bbn.openmap.util.http.HttpServer;

/**
 * The SimpleHttpImageServer is an ImageServer extended into a simple
 * Http server that creates images based on arguments within the
 * request. The Request has been modeled after the OpenGIS WMT map
 * request, although it has to be noted that this server is not yet a
 * OGC WMT-compliant server.
 * <P>
 * 
 * To see the parameters available to run this class, launch the
 * server with:
 * <P>
 * 
 * <pre>
 * 
 *  java com.bbn.openmap.image.SimpleHttpImageServer -help
 * 
 *  
 * </pre>
 * 
 * The server takes two main arguments, a properties file, and a port
 * number to run on. The properties file is a standard ImageServer
 * openmap.properties file, and its format is described in the
 * ImageServer documentation.
 * <P>
 * 
 * The request format can be seen by pointing a browser at the port
 * and host that the server is running on. It basically follows the
 * format:
 * <P>
 * 
 * <pre>
 * 
 *  http://host:port/openmap?REQUEST=map&amp;PROJTYPE=projtype&amp;SCALE=XXXXXXXX&amp;(etc)
 *  
 * </pre>
 * 
 * <P>
 * This SimpleHttpImageServer is expecting a properties file
 * containing the "layers" and "formatters" properties, both without a
 * prefix. i.e:
 * 
 * <pre>
 * 
 * 
 *  layers=markername1 markername2 markername3
 *  formatters=formatter1 formatter2
 *  markername1.class
 *  # other markername1 properties follow...
 * 
 *  formatter1.class
 *  # other formatter1 properties follow...
 * 
 *  
 * </pre>
 * 
 * <P>
 * 
 * If the 'layers' property is not defined the openmap.properties
 * file, then the 'openmap.layers' property will be used, and the
 * 'openmap.startUpLayers' property will be used to define the default
 * set of layers. This lets there be more layers available to the
 * client than would be sent by default (if the client doesn't specify
 * layers).
 * 
 * @see ImageServer
 */
public class SimpleHttpImageServer implements HttpRequestListener {

    protected HttpServer httpd;
    protected MapRequestHandler iServer;
    public final static String ErrorMessage = "OpenMap SimpleHttpImageServer:  Image request not understood.  Please send request in this format:\n\nhttp://server_address/openmap?REQUEST=MAP&SCALE=XXXXXXX&LAT=y&LON=x&...\n\nArguments:\nLAT=latitude\nLON=longitude\nSCALE=1:scale\nPROJTYPE=projection ID\nHEIGHT=pixel height\nWIDTH=pixel width\n\nArguments may be in any order after the '?'";

    public final static char queryChar = '?';

    public SimpleHttpImageServer(Properties props) throws IOException {
        this(HttpServer.DEFAULT_PORT, false, props);
    }

    public SimpleHttpImageServer(int port, boolean asDeamon, Properties props)
            throws IOException {
        httpd = new HttpServer(port, asDeamon);
        httpd.addHttpRequestListener(this);

        iServer = new MapRequestHandler(props);
    }

    public void start() {
        httpd.start();
    }
    
    /**
     * Get the MapRequestHandler that is handling map image requests.
     */
    public MapRequestHandler getMapRequestHandler() {
        return iServer;
    }

    /**
     * If you want to configure the MapRequestHandler and set it in
     * the SHIS, you can use this method. This will cause the SHIS to
     * assume that the MapRequestHandler is ready to go.
     */
    public void setMapRequestHandler(MapRequestHandler mrh) {
        iServer = mrh;
    }

    /**
     * Invoked when an http request is received.
     */
    public void httpRequest(HttpRequestEvent e) throws java.io.IOException {

        String request = e.getRequest();

        if (Debug.debugging("shis")) {
            Debug.output("SHIS: Handling request - \"" + request + "\"");
        }

        if (request.charAt(0) == '/') {
            request = request.substring(0, request.length());
        }

        int index = 0;
        index = request.indexOf('?');
        if (index != -1) { // GET Request
            request = request.substring(index + 1, request.length());
            Debug.message("shis", "SHIS: GET Request received");
        } else {
            Debug.message("shis", "SHIS: Probably a POST Request received");
        }

        try {
            iServer.handleRequest(request, e.getOutputStream());
        } catch (IOException ioe) {
            Debug.error("SHIS: caught IOException - \n" + ioe.getMessage());
        } catch (MapRequestFormatException exception) {
            String message = "OpenMap SimpleHttpImageServer encountered an problem with your request:\n\n"
                    + exception.getMessage() + "\n\n" + ErrorMessage;
            HttpConnection.writeHttpResponse(e.getOutputStream(),
                    HttpConnection.CONTENT_PLAIN,
                    message);
        }
    }

    public static void main(String[] args) {
        Debug.init();

        try {

            ArgParser ap = new ArgParser("SimpleHttpImageServer");
            ap.add("properties",
                    "A URL to use to set the properties for the ImageServer.",
                    1);
            ap.add("port",
                    "The port to listen for new map image requests on. (Default 0)",
                    1);
            ap.add("verbose", "Print action messages.");
            ap.add("test", "Create a test default image.");

            if (!ap.parse(args)) {
                ap.printUsage();
                System.exit(0);
            }

            String proparg[];
            PropertyHandler propHandler;
            proparg = ap.getArgValues("properties");
            if (proparg != null) {
                propHandler = new PropertyHandler(proparg[0]);
            } else {
                propHandler = new PropertyHandler();
            }

            String[] varg = ap.getArgValues("verbose");
            if (varg != null) {
                Debug.put("shis");
                Debug.put("imageserver");
            }

            SimpleHttpImageServer shis;
            String[] portarg = ap.getArgValues("port");
            if (portarg != null) {
                int port = Integer.parseInt(portarg[0]);
                shis = new SimpleHttpImageServer(port, false, propHandler.getProperties());
            } else {
                shis = new SimpleHttpImageServer(propHandler.getProperties());
            }
            
            shis.start();

            Debug.output("OpenMap SimpleHttpImageServer: listening on port: "
                    + shis.httpd.getPort()
                    + (proparg == null ? "" : " with properties in "
                            + proparg[0]));

            String[] testarg = ap.getArgValues("test");
            if (testarg != null) {
                OutputStream out = new FileOutputStream("test.jpg");
                shis.httpRequest(new HttpRequestEvent(shis, "/openmap?REQUEST=map", out));
            }

        } catch (MalformedURLException murle) {
            System.err.println("Bad URL path to properties file.");
            murle.printStackTrace();
        } catch (IOException ioe) {
            System.err.println("Unable to start http server:");
            ioe.printStackTrace();
        }
    }
}