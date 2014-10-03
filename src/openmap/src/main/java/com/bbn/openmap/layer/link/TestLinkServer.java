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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/TestLinkServer.java,v $
// $RCSfile: TestLinkServer.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 18:08:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * The TestLinkServer is my test implementation of a customized
 * LinkServer.
 */
public class TestLinkServer extends LinkServer implements LinkActionConstants,
        LinkPropertiesConstants {

    int newGraphicCounter = 0;

    LinkOMGraphicList clientCreatedGraphics = new LinkOMGraphicList();

    /**
     * Create child thread that will handle the client.
     * 
     * @param s the socket to communicate over.
     */
    public TestLinkServer(Socket s) {
        super(s);
    }

    /**
     * handleClient is a method that listens to the link to a client,
     * and responds to requests that are made.
     */
    public void handleClient() throws IOException {
        boolean validQuery;

        while (true) {
            link.readAndParse();
            validQuery = false;
            System.out.println("TestLinkServer: fielding request");

            LinkMapRequest graphicsQuery = link.getMapRequest();
            LinkActionRequest gestureQuery = link.getActionRequest();
            LinkActionList actionList = link.getActionList();

            if (graphicsQuery != null) {
                System.out.println((LinkMapRequest) graphicsQuery);
                getRectangle(graphicsQuery, link);
                validQuery = true;
            }
            if (gestureQuery != null) {
                System.out.println(gestureQuery);
                handleGesture(gestureQuery, link);
                validQuery = true;
            }
            if (actionList != null) {
                Vector graphicUpdates = actionList.getGraphicUpdates();
                Iterator it = graphicUpdates.iterator();
                while (it.hasNext()) {
                    GraphicUpdate gu = (GraphicUpdate) it.next();
                    if (gu != null) {
                        String id = gu.id;
                        System.out.println("TestLinkServer: graphic id = " + id);

                        OMGraphic graphic = gu.graphic;
                        int index = clientCreatedGraphics.getOMGraphicIndexWithId(id);
                        if (index != Link.UNKNOWN) {
                            System.out.println("TestLinkServer: modifying graphic");
                            clientCreatedGraphics.setOMGraphicAt(gu.graphic,
                                    index);
                        } else {
                            System.out.println("TestLinkServer: new graphic");
                            // Set the ID for it, so it can be
                            // referred to later.
                            LinkProperties props = (LinkProperties) graphic.getAppObject();

                            props.setProperty(LPC_GRAPHICID, "graphic"
                                    + (newGraphicCounter++));
                            System.out.println("TestLinkServer: new graphic given id "
                                    + props);
                        }

                        clientCreatedGraphics.doAction(gu.graphic,
                                new OMAction(gu.action));

                    }
                }
            }

            if (!validQuery) {
                huh(link);
            }
        }
    }

    /**
     * An example of how to handle GraphicsLinkQueries.
     * 
     * @param query the GraphicsLinkQuery, so you can get more
     *        information about the parameters of the map screen of
     *        the client.
     * @param link the link to communicate the response back to the
     *        client.
     */
    public void getRectangle(LinkMapRequest query, Link link)
            throws IOException {

        LinkProperties lineProperties = new LinkProperties(LPC_LINECOLOR, "FFFF0000");
        lineProperties.setProperty(LPC_GRAPHICID, "testline1");
        lineProperties.setProperty(LPC_FILLCOLOR, "FFFF0000");
        lineProperties.setProperty(LPC_HIGHLIGHTCOLOR, "FFFFFF00");
        lineProperties.setProperty(LPC_LINEWIDTH, "1");
        lineProperties.setProperty(LPC_INFO, "Testing info line.");
        //      lineProperties.setProperty(LPC_URL,
        // "http://blatz.bbn.com/users/dietrick/LinkProtocol.html");

        int count = 0;
        LinkGraphicList gr = new LinkGraphicList(link, new LinkProperties());

        LinkBoundingPoly[] bounds = query.getBoundingPolys();

        for (int i = 0; i < bounds.length; i++) {

            gr.addLine(bounds[i].maxY,
                    bounds[i].minX,
                    bounds[i].minY,
                    bounds[i].maxX,
                    (int) OMGraphic.LINETYPE_STRAIGHT,
                    -1,
                    lineProperties);
            count++;

            int[] data = new int[10000];

            for (int j = 0; j < 10000; j++) {
                if (j < 3000) {
                    data[j] = 0xFFFF0000;
                } else if (j < 6000) {
                    data[j] = 0xFF00FF00;
                } else if (j < 8000) {
                    data[j] = 0xFF0000FF;
                } else {
                    data[j] = 0xFFAAAAAA;
                }
            }

            gr.addGrid(42.0f,
                    -70.0f,
                    100,
                    100,
                    0.0f,
                    .001f,
                    .001f,
                    LinkGraphicConstants.COLUMN_MAJOR,
                    data,
                    new LinkProperties());
            count++;

            //      for (int x = 10; x < 600; x+=2) {
            //          for (int y = 10; y < 500; y+=50) {
            //              link.addLine(x, y, x + 40, y + 40, lineSemantics);
            //              count++;
            //          }
            //      }
        }

        // Handle any other graphics added by client.
        LinkGraphic.write(clientCreatedGraphics, link);

        gr.end(Link.END_SECTION);

        System.out.println("TestLinkServer: Wrote " + count
                + " graphics to output stream");

        int des = LinkUtil.setMask(0, MOUSE_CLICKED_MASK);
        //      des = LinkUtil.setMask(des, MOUSE_MOVED_MASK);
        des = LinkUtil.unsetMask(des, SERVER_NOTIFICATION_MASK);
        new LinkActionRequest(link, des, Link.END_TOTAL);
        System.out.println("TestLinkServer: Wrote gesture descriptor to output stream");

    }

    /**
     * An example of how to handle LinkActionRequest.
     * 
     * @param glq the LinkActionRequest, so you can get more
     *        information about the parameters of the gesture frome
     *        the client.
     * @param link the link to communicate the response back to the
     *        client.
     */
    public void handleGesture(LinkActionRequest glq, Link link)
            throws IOException {

//        int descriptor = glq.getDescriptor();

        LinkProperties props = new LinkProperties();
        props.setProperty(LPC_INFO, ("Mouse Clicked at: x = " + glq.getX()
                + ", y = " + glq.getY()));
        LinkActionList glr = new LinkActionList(link, props);
        String gid = glq.getGraphicID();
        if (gid == null) {
            System.out.println("Deselecting graphic");
            glr.deselectGraphics();
        } else {
            System.out.println("Selecting graphic");
            props.clear();
            props.setProperty(LPC_GRAPHICID, gid);
            glr.modifyGraphic(MODIFY_SELECT_GRAPHIC_MASK, props);
        }

        glr.end(Link.END_TOTAL);
    }

}