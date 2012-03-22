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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/BufferedLinkLayer.java,v $
// $RCSfile: BufferedLinkLayer.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

/*  Java Core  */
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.UnknownHostException;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The BufferedLinkLayer is a Swing component, and an OpenMap layer,
 * that communicates with a server via the Link protocol. It transmits
 * graphics requests and gesture information, and handles the
 * responses to those queries. The entry in the openmap.properties
 * file looks like this:
 * <P>
 * <code>
 * # port number of server
 * link.port=3031
 * # host name of server
 * link.host=host.com
 * # URL of properties file for server attributes
 * link.propertiesURL=http://location.of.properties.file.com
 * </code>
 */
public class BufferedLinkLayer extends LinkLayer {

    /**
     * The default constructor for the Layer. All of the attributes
     * are set to their default values.
     */
    public BufferedLinkLayer() {
        super();
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this));
    }

    /**
     * Constructor to use when LinkLayer is not being used with
     * OpenMap application.
     * 
     * @param host the hostname of the server's computer.
     * @param port the port number of the server.
     * @param propertiesURL the URL of a properties file that contains
     *        parameters for the server.
     */
    public BufferedLinkLayer(String host, int port, String propertiesURL) {
        super(host, port, propertiesURL);
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this));
    }

    /**
     * Prepares the graphics for the layer. This is where the
     * getRectangle() method call is made on the link.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this
     * happens, the map will set the cancel bit in the LayerThread,
     * (the thread that is running the prepare). If this Layer needs
     * to do any cleanups during the abort, it should do so, but
     * return out of the prepare asap.
     * 
     * @return a list of graphics.
     */
    public synchronized OMGraphicList prepare() {

        if (isCancelled()) {
            Debug.message("link", getName()
                    + "|BufferedLinkLayer.prepare(): aborted.");
            return null;
        }

        Projection projection = getProjection();

        if (projection == null) {
            System.err.println("Link Layer needs to be added to the MapBean before it can get graphics!");
            return new LinkOMGraphicList();
        }

        Debug.message("basic", getName()
                + "|BufferedLinkLayer.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // LinkOMGraphicList is made up of OMGraphics, which are
        // generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        OMGraphicList omGraphics = getList();

        if (omGraphics == null || omGraphics.isEmpty()) {

            ////////////// Call getRectangle for server....
            try {
                // We do want the link object here... If another
                // thread is
                // using the link, wait.
                ClientLink l = linkManager.getLink(true);

                if (l == null) {
                    System.err.println("BufferedLinkLayer: unable to get link in prepare().");
                    return new LinkOMGraphicList();
                }

                synchronized (l) {
                    omGraphics = getAllGraphics(l, projection);
                }

                linkManager.finLink();

            } catch (UnknownHostException uhe) {
                System.err.println("BufferedLinkLayer: unknown host!");
                omGraphics = new LinkOMGraphicList();
            } catch (java.io.IOException ioe) {
                System.err.println("BufferedLinkLayer: IOException contacting server for map request!");
                System.err.println(ioe);

                linkManager.resetLink();

                if (!quiet) {
                    fireRequestMessage("Communication error between "
                            + getName() + " layer\nand Link Server: Host: "
                            + host + ", Port: " + port);
                }

                System.err.println("BufferedLinkLayer: Communication error between "
                        + getName()
                        + " layer\nand Link Server: Host: "
                        + host
                        + ", Port: " + port);

                omGraphics = new LinkOMGraphicList();
            }
        } else {
            omGraphics.project(projection);
        }

        /////////////////////
        // safe quit
        int size = 0;
        if (omGraphics != null) {
            size = omGraphics.size();

            if (Debug.debugging("basic")) {
                System.out.println(getName()
                        + "|BufferedLinkLayer.prepare(): finished with " + size
                        + " graphics");
            }
        } else {
            Debug.message("basic",
                    getName()
                            + "|BufferedLinkLayer.prepare(): finished with null graphics list");
        }

        return omGraphics;
    }

    /**
     * Creates the LinkMapRequest, and gets the results.
     * 
     * @param link the link to communicate over.
     * @param proj the projection to give to the graphics.
     * @return LinkOMGraphicList containing graphics from the server.
     * @throws IOException
     */
    protected LinkOMGraphicList getAllGraphics(ClientLink link, Projection proj)
            throws IOException {

        LinkBoundingPoly[] boundingPolys = new LinkBoundingPoly[1];
        boundingPolys[0] = new LinkBoundingPoly(-180.0f, -90f, 180f, 90);

        
        Point2D center = proj.getCenter();
        LinkMapRequest.write((float) center.getY(),
                (float) center.getX(),
                proj.getScale(),
                proj.getHeight(),
                proj.getWidth(),
                boundingPolys,
                args,
                link);

        link.readAndParse(proj, currentGenerator);

        // While we are here, check for any change in gesture query
        // requests.
        LinkActionRequest lar = link.getActionRequest();
        if (lar != null) {
            setGestureDescriptor(lar.getDescriptor());
        }

        LinkGraphicList lgl = link.getGraphicList();
        if (lgl != null) {
            //Deal with all the messaging....
            handleMessages(lgl.getProperties());
            return lgl.getGraphics();
        } else {
            Debug.message("link",
                    "BufferedLinkLayer: getAllGraphics(): no graphic response.");
            return new LinkOMGraphicList();
        }
    }

}