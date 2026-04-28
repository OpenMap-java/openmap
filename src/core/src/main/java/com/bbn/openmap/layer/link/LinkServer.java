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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkServer.java,v $
// $RCSfile: LinkServer.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 18:08:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.IOException;
import java.net.Socket;

/**
 * The Link Server is the object communicating with the LinkLayer. It
 * is launched within its own thread to handle a specific client. If
 * the server is accessing a database or an object that many
 * LinkServers may be contacting, You'll need to ensure some amount of
 * thread safety for the data if the data is being modified by the
 * LinkServer.
 */
public class LinkServer extends Thread implements LinkPropertiesConstants {
    /** The Link to use to talk to the client. */
    protected Link link;

    /**
     * Constructor used by the LinkServerStarter in using this class
     * as a LinkServerFactory.
     */
    public LinkServer() {}

    /**
     * Create child thread that will handle the client.
     * 
     * @param s the socket to communicate over.
     */
    public LinkServer(Socket s) {
        try {
            link = new Link(s);
        } catch (java.io.IOException ioe) {
            System.err.println("LinkServer: IOException while creating child server:");
            System.err.println(ioe);
        }
    }

    /** From the Runnable interface. The thread starts here... */
    public void run() {
        try {
            handleClient();
        } catch (IOException ioe) {
            link.cleanUp();
            link = null;
            if (com.bbn.openmap.util.Debug.debugging("link")) {
                System.err.println(ioe);
            }
            com.bbn.openmap.util.Debug.output("LinkServer: Client disconnected");
            System.gc();
        }
    }

    /**
     * handleClient is a method that listens to the link to a client,
     * and responds to requests that are made.
     * 
     * @throws IOException
     */
    public void handleClient() throws IOException {
        boolean validQuery;

        while (true) {
            link.readAndParse();
            validQuery = false;

            // For instance, you could do something like this...

            //          LinkMapRequest graphicsQuery = link.getMapRequest();
            //          LinkActionRequest gestureQuery =
            // link.getActionRequest();

            //          if (graphicsQuery != null) {
            //              getRectangle(graphicsQuery, link);
            //              validQuery = true;
            //          }
            //          if (gestureQuery != null) {
            //              handleGesture(gestureQuery, link);
            //              validQuery = true;
            //          }

            if (!validQuery) {
                huh(link);
            }
        }
    }

    /**
     * If a request is not handled, or not understood, reply with
     * this.
     * 
     * @throws IOException
     */
    public void huh(Link link) throws IOException {
        link.start(Link.HUH_HEADER);
        link.end(Link.END_TOTAL);
    }

    /**
     * An example of how to handle GraphicsLinkQueries.
     * 
     * @param query the GraphicsLinkQuery, so you can get more
     *        information about the parameters of the map screen of
     *        the client.
     * @param link the link to communicate the response back to the
     *        client.
     * @throws IOException
     */
    public void getRectangle(LinkMapRequest query, Link link)
            throws IOException {

        LinkGraphicList lgl = new LinkGraphicList(link, new LinkProperties());

        // Send nothing

        lgl.end(Link.END_SECTION);

        int des = LinkUtil.setMask(0, LinkActionRequest.MOUSE_CLICKED_MASK);
        new LinkActionRequest(link, des, Link.END_TOTAL);
    }

    /**
     * An example of how to handle LinkActionRequest.
     * 
     * @param lar the LinkActionRequest, so you can get more
     *        information about the parameters of the gesture frome
     *        the client.
     * @param link the link to communicate the response back to the
     *        client.
     * @throws IOException
     */
    public void handleGesture(LinkActionRequest lar, Link link)
            throws IOException {

        LinkProperties properties = new LinkProperties();
        properties.setProperty(LPC_INFO, ("Mouse Clicked at: x = " + lar.getX()
                + ", y = " + lar.getY()));

        LinkActionList lal = new LinkActionList(link, properties);
//        int descriptor = lar.getDescriptor();

        String gid = lar.getProperties().getProperty(LPC_GRAPHICID);
        if (gid == null) {
            System.out.println("Deselecting graphics");
            lal.deselectGraphics();
        } else {
            System.out.println("Selecting graphic");
            lal.modifyGraphic(LinkActionList.MODIFY_SELECT_GRAPHIC_MASK,
                    lar.getProperties());
        }

        lal.end(Link.END_TOTAL);
    }

}