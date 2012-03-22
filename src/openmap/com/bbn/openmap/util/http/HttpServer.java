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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/http/HttpServer.java,v $
// $RCSfile: HttpServer.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 18:57:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A simple HTTP Server implementing HTTP/0.9 protocols.
 * 
 * Cobbled together from a server originally written by David Flanagan
 * for the book <bold>Java in a Nutshell </bold>, Copyright(c) 1996
 * O'Reilly & Associates.
 * 
 * Modified to use JDK 1.1 Readers, and Writers. Further modified to
 * use the JDK 1.1 Event model.
 * 
 * @author Tom Mitchell
 * @version 1.0, 06/13/97
 */
public class HttpServer extends Thread {

    /**
     * The default port. A port of 0 (zero) causes the system to
     * allocate any unused port. With any other number the system will
     * attempt to open that port, and throw an exception if it is in
     * use.
     */
    public final static int DEFAULT_PORT = 0;

    protected int port;
    protected ServerSocket listen_socket;
    protected Vector listeners;

    /**
     * Creates an Http Server on the indicated port, and then starts a
     * thread that listens to that port. The thread will not be a
     * daemon thread.
     * 
     * @param port the port to open
     * @see java.net.ServerSocket
     */
    public HttpServer(int port) throws IOException {
        this(port, false);
    }

    /**
     * Creates an Http Server on the indicated port, and then starts a
     * thread that listens to that port. The thread will be a daemon
     * thread of asDaemon is true.
     * 
     * @param port the port to open
     * @param asDaemon whether to make thread a daemon
     * @see java.net.ServerSocket
     */
    public HttpServer(int port, boolean asDaemon) throws IOException {
        this.port = port;
        listeners = new Vector();
        listen_socket = new ServerSocket(port);
        this.setDaemon(asDaemon);
    }

    /**
     * Creates an Http Server on any free port, and then starts a
     * thread that listens to that port.
     * 
     * @see java.net.ServerSocket
     */
    public HttpServer() throws IOException {
        this(DEFAULT_PORT);
    }

    /**
     * The body of the server thread. Loop forever, listening for and
     * accepting connections from clients. For each connection, create
     * a HttpConnection object to handle communication through the new
     * Socket.
     * 
     * @see HttpConnection
     * @see java.net.Socket
     */
    public void run() {
        try {
            while (true) {
                Socket client_socket = listen_socket.accept();
                HttpConnection httpConnection = new HttpConnection(client_socket, this);
                httpConnection.start();
            }
        } catch (IOException e) {
            System.err.println("Exception while listening for connections");
            e.printStackTrace();
        }
    }

    /**
     * Gets the port associate with this server.
     * 
     * @return the server's port
     */
    public int getPort() {
        return listen_socket.getLocalPort();
    }

    /**
     * Creates a HttpRequestEvent and sends it to all registered
     * listeners.
     * 
     * @param request the parsed http request
     * @param output OutputStream associated with the request's client
     *        connection.
     * @see java.io.DataOutputStream
     * @see HttpRequestListener
     * @see HttpRequestEvent
     */
    public HttpRequestEvent fireHttpRequestEvent(String request,
                                                 OutputStream output)
            throws IOException {

        HttpRequestEvent event = new HttpRequestEvent(this, request, output);

        HttpRequestListener listener;
        // Make a copy of the list and fire the events using that
        // copy.
        // This means that listeners can be added or removed from the
        // original list in response to this event.
        Vector list = (Vector) listeners.clone();
        Enumeration e = list.elements();
        while (e.hasMoreElements()) {
            listener = (HttpRequestListener) e.nextElement();
            listener.httpRequest(event);
        }

        return event;
    }

    /**
     * Adds a new http request listener. Don't add multiple listeners
     * when binary content responses are required! One Listener should
     * handle binary responses, because the result length needs to be
     * calculated. You can add multiple Listeners that use the Writer
     * inside the HttpRequestEvent to concatenate a complete text
     * response.
     * 
     * @param l the listener
     * @see HttpRequestListener
     */
    public void addHttpRequestListener(HttpRequestListener l) {
        listeners.addElement(l);
    }

    /**
     * Removes an http request listener.
     * 
     * @param l a listener
     * @see HttpRequestListener
     */
    public void removeHttpRequestListener(HttpRequestListener l) {
        listeners.removeElement(l);
    }

    /**
     * A main routine for unit testing. Starts a HttpServer, adds
     * several HttpRequestListeners, and waits for connections.
     * <p>
     * Usage: java com.bbn.openmap.layer.util.http.HttpServer [port]
     * <p>
     * If no port is specified, the default port is used.
     * <p>
     * If port zero is specified, the system chooses the port.
     * <p>
     * If a port other than zero is specified, the http server will
     * attempt to open that port, or fail if it is in use.
     * <p>
     * Examples:
     * <p>
     * java com.bbn.openmap.layer.util.http.HttpServer
     * <p>
     * java com.bbn.openmap.layer.util.http.HttpServer 8000
     * <p>
     * java com.bbn.openmap.layer.util.http.HttpServer 0
     * <p>
     * 
     * @param args command line args
     */
    public static void main(String[] args) {
        int port = 0;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = 0;
            }
        }

        try {
            HttpServer server = new HttpServer(port);
            server.addHttpRequestListener(new SeparatorListener());
            server.addHttpRequestListener(new SieveListener());
            server.addHttpRequestListener(new SeparatorListener());
            server.addHttpRequestListener(new ReverseListener());
            server.addHttpRequestListener(new SeparatorListener());
            server.start();
            System.out.println("Server listening on port " + server.getPort());
        } catch (IOException e) {
            System.err.println("Unable to start http server:");
            e.printStackTrace();
        }
    }
}