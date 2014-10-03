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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/draw/DrawLinkLayer.java,v $
// $RCSfile: DrawLinkLayer.java,v $
// $Revision: 1.2 $
// $Date: 2007/02/26 17:12:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.draw;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import com.bbn.openmap.layer.link.ClientLink;
import com.bbn.openmap.layer.link.LinkActionRequest;
import com.bbn.openmap.layer.link.LinkLayer;
import com.bbn.openmap.layer.link.LinkOMGraphicList;
import com.bbn.openmap.layer.link.LinkProperties;
import com.bbn.openmap.layer.link.LinkUtil;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;

/**
 * The DrawLinkLayer is an extension to the LinkLayer. It allows to selectively
 * disable interacting with graphics in the LinkLayer and to selectively enable
 * using the drawing tool to modify graphics in the LinkLayer.
 * <P>
 * 
 * The entry in the openmap.properties file looks like this:
 * <P>
 * 
 * <pre>
 * 
 * 
 * 
 *      # port number of server
 *      link.port=3031
 *      # host name of server
 *      link.host=host.com
 *      # URL of properties file for server attributes
 *      link.propertiesURL=http://location.of.properties.file.com
 * 
 * 
 * 
 * </pre>
 */
public class DrawLinkLayer extends LinkLayer {

    public final static String LPC_EDITABLE = "editable";
    public final static String LPC_SELECTABLE = "selectable";

    protected OMDrawingTool drawingTool = null;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public DrawLinkLayer() {
    }

    /**
     * Constructor to use when LinkLayer is not being used with OpenMap
     * application.
     * 
     * @param host the hostname of the server's computer.
     * @param port the port number of the server.
     * @param propertiesURL the URL of a properties file that contains
     *        parameters for the server.
     */
    public DrawLinkLayer(String host, int port, String propertiesURL) {
        super(host, port, propertiesURL);
    }

    /**
     * Set all the Link properties from a properties object.
     * 
     * @param prefix the prefix to the properties that might individualize it to
     *        a particular layer.
     * @param properties the properties for the layer.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);
        setAddToBeanContext(true);
    }

    /**
     * Modify the behavior of LinkLayer::mouseClicked to only interact with
     * graphics that have the "selectable" property set to true. If the
     * "editable" property is set then use the drawing tool to edit the graphic.
     */
    public boolean mouseClicked(MouseEvent e) {

        int descriptor = MOUSE_CLICKED_MASK;

        LinkOMGraphicList graphics = getGraphicList();

        if (graphics == null) {
            Debug.message("link", "DrawLinkLayer.mouseClicked: null LinkOMGraphicList, making new one...");
            // If the graphic list is null then LinkLayer::mouseClicked will
            // not report the mouse click to the server. Set it to an empty
            // list to continue processing.
            graphics = new LinkOMGraphicList();
            setGraphicList(graphics);
        }

        Debug.message("link", "DrawLinkLayer mouseClicked");
        OMGraphic gesGraphic = null;

        LinkOMGraphicList selectableList = new LinkOMGraphicList();
        OMGraphic g;
        Properties p;
        String selectable;
        for (Iterator it = graphics.iterator(); it.hasNext();) {
            g = (OMGraphic) it.next();
            p = (Properties) g.getAppObject();
            selectable = p.getProperty(LPC_SELECTABLE);
            if (selectable.equals("true")) {
                selectableList.add(g);
            }
        }

        try {

            if (e == null) {
                graphics.deselect();
                return false;
            }

            gesGraphic = selectableList.findClosest(e.getX(), e.getY(), distanceLimit);

            String id = null;

            // If there was a graphic, set the mask to indicate that,
            // and keep track of the graphic and the list index of the
            // graphic for the response. If a graphic modify command
            // comes back without an ID, then we'll assume the server
            // was referring to this graphic.
            if (gesGraphic != null) {
                Debug.message("link", "LinkLayer: found gesture graphic");

                boolean tellServer = graphicGestureReaction(gesGraphic, descriptor, e);

                if (!tellServer) {
                    repaint();
                    return true;
                }

                descriptor = LinkUtil.setMask(descriptor, GRAPHIC_ID_MASK);
                id = ((LinkProperties) gesGraphic.getAppObject()).getProperty(LPC_GRAPHICID);
            } else {
                Debug.message("link", "LinkLayer: gesture graphic NOT FOUND");
                // clear out info line
                fireRequestInfoLine("");
            }

            // server inform
            if (!LinkUtil.isMask(getGestureDescriptor(), SERVER_NOTIFICATION_MASK)) {
                return false;
            }

            // Get the lat/lon point of the event
            Point2D llpoint = getProjection().inverse(e.getX(), e.getY());

            // Don't need these anymore, look below for explaination
            // for asynchronous operation.
            // LinkActionList lal;
            // LinkActionRequest lar;

            ClientLink l = linkManager.getLink(false);

            // We'll check this here because we don't want to wait if
            // it is not available - it could be used for another
            // graphics or gui fetch.
            if (l == null) {
                Debug.message("link", "LinkLayer: unable to get link in handleGesture().");
                return false;
            }

            // Using the link - carefully prevent others from using it
            // too!
            synchronized (l) {
                if (id != null) {
                    args.setProperty(LPC_GRAPHICID, id);
                } else {
                    // Reset this to prevent sending the id of a graphic
                    // that is not being clicked on.
                    args.remove(LPC_GRAPHICID);
                }

                // Send the query
                LinkActionRequest.write(descriptor, e, (float) llpoint.getY(), (float) llpoint.getX(), args, l);

            }

            linkManager.finLink();

        } catch (IOException ioe) {
            System.err.println("LinkLayer: IOException contacting server during gesture handling!");
            System.err.println(ioe);
            linkManager.resetLink();
            return false;
        }

        OMDrawingTool dt = getDrawingTool();
        if (dt != null) {
            // Stop editing any graphic currently being edited
            dt.setMask(OMDrawingTool.DEACTIVATE_ASAP_BEHAVIOR_MASK);
            EditableOMGraphic egraphic = dt.getCurrentEditable();
            if (egraphic != null) {
                OMGraphic graphic = egraphic.getGraphic();

                OMAction action = new OMAction(UPDATE_GRAPHIC_MASK);
                drawingComplete(graphic, action);
                dt.setCurrentEditable(null);
            }
        }

        // FIXME - we could operate on a subset of graphics by either having
        // multiple graphics lists or by filtering the graphics list
        // to only detect/edit the graphics we want.
        if (gesGraphic != null && drawingTool != null) {
            OMGraphic graphic = null;
            if (dt != null) {

                Properties prop = (Properties) gesGraphic.getAppObject();
                String editable = prop.getProperty(LPC_EDITABLE);
                Debug.message("link", "DrawLinkLayer: editable: " + editable);
                if (editable.equals("false")) {
                    Debug.message("link", "DrawLinkLayer not editing graphic.  editable = false");
                } else {
                    Debug.message("link", "DrawLinkLayer trying to edit graphic");
                    graphic = dt.edit(gesGraphic, layer);
                }
            } else {
                Debug.message("link", "Error: DrawLinkLayer has not drawing tool");
            }

            if (graphic != null) {
                Debug.message("link", "DrawLinkLayer editing graphic");
            } else {
                Debug.message("link", "DrawLinkLayer unable to edit graphic");
            }
        }
        return true;
    }

    protected final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer = this;

    protected String key = "MapToolDrawing";

    /**
     * The retrieval key for this tool
     * 
     * @return String The key for this tool.
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the retrieval key for this tool
     * 
     * @param aKey The key for this tool.
     */
    public void setKey(String aKey) {
        key = aKey;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof OMDrawingTool) {
            Debug.message("link", "DrawLinkLayer: found a drawing tool");
            setDrawingTool((OMDrawingTool) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof OMDrawingTool) {
            if (getDrawingTool() == (OMDrawingTool) someObj) {
                setDrawingTool(null);
            }
        }
    }

    public void setDrawingTool(OMDrawingTool dt) {
        drawingTool = dt;
    }

    public OMDrawingTool getDrawingTool() {
        return drawingTool;
    }
}
