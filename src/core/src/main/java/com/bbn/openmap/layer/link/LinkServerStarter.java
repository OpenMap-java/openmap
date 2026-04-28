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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkServerStarter.java,v $
// $RCSfile: LinkServerStarter.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:57 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * The LinkServerStarter is the object that listens for Link clients
 * on a specific port. If a Link client contacts it, it uses the
 * startNewServer method to create a LinkServer to serve the client on
 * it's own thread. If you want to create a new type of LinkServer,
 * you should also create a new LinkServerStarter to launch it
 * properly. Generally, the main() and startNewServer() methods are
 * the only thing you would need to modify.
 */
public class LinkServerStarter {

    /** Default port that the server starter listens to. */
    public static final int DEFAULT_PORT = 3031;
    /** The port being listened to. */
    protected int serverPort;

    /** Starts the LinkServerStarter listening to the default port. */
    public LinkServerStarter() {
        serverPort = DEFAULT_PORT;
    }

    /** Starts the LinkServerStarter listening to the specified port. */
    public LinkServerStarter(int port) {
        serverPort = port;
    }

    /** Set the port to listen for a connection request. */
    public void setPort(int port) {
        serverPort = port;
    }

    /** Get the port that is being listened to. */
    public int getPort() {
        return serverPort;
    }

    /**
     * The method of the parent server that is listening for clients.
     * When a contact is made, a child thread is spawned off to handle
     * the client.
     */
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            Socket socket;
            System.out.println("LinkServerStarter: running on port "
                    + serverPort);

            while (true) {
                socket = serverSocket.accept();
                System.out.println("LinkServer: fielding connection");
                ((Thread) startNewServer(socket)).start();
            }

        } catch (java.io.IOException ioe) {
            System.err.println("LinkServer: IOException while running:");
            System.err.println(ioe);
        }
    }

    /**
     * This method gets called to create a new server to handle a new
     * connection. Thread.start() will get called on the returned
     * thread. This method should always get overridden.
     * 
     * @param sock the socket connection
     * @return a thread that will get started
     * @see java.lang.Thread#start()
     */
    public Thread startNewServer(Socket sock) {
        return new LinkServer(sock);
    }

    /**
     * Start up the server. This is the method to change if you want
     * to customize how the LinkServer will handle clients - port,
     * arguments, etc.
     */
    public static void main(String[] argv) {

        int pnumber = -1;

        com.bbn.openmap.util.Debug.init();

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-port") && argv.length > i + 1) {
                try {
                    pnumber = Integer.parseInt(argv[i + 1]);
                    break;
                } catch (NumberFormatException e) {
                    pnumber = -1;
                }
            }
        }

        if (pnumber < 0) {
            System.out.println("Need to start the server with a port number.");
            System.out.println("Usage: java com.bbn.openmap.layer.link.LinkServerStarter -port <port number>");
            System.exit(0);
        }

        System.out.println("LinkServerStarter: Starting up on port " + pnumber
                + ".");
        LinkServerStarter serverStarter = new LinkServerStarter(pnumber);
        while (true) {
            serverStarter.run();
        }
    }

}