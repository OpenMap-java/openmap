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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/TestServerStarter.java,v $
// $RCSfile: TestServerStarter.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:57 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.net.Socket;

/**
 * The LinkServerStarter is the object that listens for Link clients
 * on a specific port. If a Link client contacts it, it uses the
 * LinkServerFactory to create a LinkServer to serve the client on
 * it's own thread. If you want to create a new type of LinkServer,
 * you should also create a new LinkServerStarter to launch it
 * properly. Generally, the main() is the only thing youwould need to
 * modify, to change the type of LinkServerFactory (and therefore, the
 * LinkServer) used for the client.
 */
public class TestServerStarter extends LinkServerStarter {

    /** Starts the LinkServerStarter listening to the specified port. */
    public TestServerStarter(int port) {
        super(port);
    }

    /**
     * From the LinkServerFactory interface, starts up a new
     * LinkServer to handle a client.
     * 
     * @param socket socket to use to communicate to the client.
     */
    public Thread startNewServer(Socket socket) {
        return (new TestLinkServer(socket));
    }

    /**
     * Start up the server. This is the method to change if you want
     * to customize how the LinkServer will handle clients - port,
     * arguments, LinkServerFactory, etc.
     */
    public static void main(String[] argv) {
        int pnumber = -1;

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
            System.out.println("Usage: java com.bbn.openmap.layer.link.TestServerStarter -port <port number>");
            System.exit(0);
        }

        System.out.println("TestServerStarter: Starting up on port " + pnumber
                + ".");

        TestServerStarter serverStarter = new TestServerStarter(pnumber);
        while (true) {
            serverStarter.run();
        }
    }
}