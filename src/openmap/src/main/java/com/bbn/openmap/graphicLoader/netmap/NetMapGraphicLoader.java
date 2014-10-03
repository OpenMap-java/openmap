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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapGraphicLoader.java,v $
// $RCSfile: NetMapGraphicLoader.java,v $
// $Revision: 1.9 $
// $Date: 2005/08/09 19:09:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.awt.Component;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.graphicLoader.MMLGraphicLoader;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.plugin.PlugIn;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The NetMapGraphicLoader is a component that can listen to a NetMapConnector,
 * receive and interpret NetMapEvents, and draw the resulting network on the
 * map. The NetMapConnector does all the heavy work, the NetMapGraphicLoader
 * serves as an interface to get OMGraphics on the map.
 * <P>
 * 
 * The easiest way to use this class is to create it with the NetMapConnector
 * and add it to the LayerHandler or MapHandler. If the NetMapConnector is going
 * to be created by another object for application design reasons, just create a
 * NetMapConnectionHandler and add it to the MapHandler, and then add the
 * NetMapConnector to the MapHandler, too. The NetMapConnectionHandler will
 * create a NetMapGraphicLoader for the NetMapConnector. Make sure a
 * GraphicLoaderConnector is also in the MapHandler, too, because it will create
 * a GraphicLoaderPlugIn/PlugInLayer for the NetMapGraphicLoader.
 */
public class NetMapGraphicLoader extends MMLGraphicLoader implements
        NetMapListener, NetMapConstants {

    /** The list that gets sent to the GraphicLoaderPlugIn. */
    protected OMGraphicList omList = null;

    /**
     * The component that provides controls to the NetMapReader, which in turn
     * reads the server stream.
     */
    private NetMapConnector connector = null;

    /**
     * The cached list of nodes, created from the event properties.
     */
    private NodeCache nodeList = new NodeCache();

    /**
     * The cached list of links between nodes, created from the event
     * properties.
     */
    private LineCache lineList = new LineCache();

    protected boolean DEBUG = false;

    protected String localhostIP = null;

    /**
     * Constructor for the NetMapGraphicLoader, you still have to set the
     * NetMapConnector.
     */
    public NetMapGraphicLoader() {
        DEBUG = Debug.debugging("netmap");

        this.nodeList = new NodeCache();
        this.lineList = new LineCache();

        try {
            localhostIP = InetAddress.getLocalHost().getHostAddress();
            if (DEBUG) {
                Debug.output("NetMapGraphicLoader running on: " + localhostIP);
            }
        } catch (java.net.UnknownHostException uhe) {
            localhostIP = null;
        }
    }

    /**
     * Constructor for the NetMapGraphicLoader that sets the NetMapConnector.
     * 
     * @param nmc the NetMapConnector to listen to.
     */
    public NetMapGraphicLoader(NetMapConnector nmc) {
        this();
        setNetMapConnector(nmc);
    }

    /**
     * Set the NetMapConnector to listen to. This method will add the
     * NetMapGraphicLoader to the NetMapConnector as a NetMapListener. If there
     * is already a NetMapConnector set in this NetMapGraphicLoader, then this
     * method will disconnect from the current NetMapConnector, and reconnect
     * with the new one if it isn't null.
     */
    public void setNetMapConnector(NetMapConnector nmc) {
        if (connector != null) {
            connector.removeNetMapListener(this);
        }

        connector = nmc;
        if (connector != null) {
            connector.addNetMapListener(this);
        }
    }

    /**
     * Get the current NetMapConnector.
     */
    public NetMapConnector getNetMapConnector() {
        return connector;
    }

    /**
     * NetMapListener method, called by the NetMapConnector.
     */
    public void catchEvent(NetMapEvent nme) {
        // for now, print to debug, later, create OMGraphics out of
        // it.
        if (DEBUG) {
            Debug.output(nme.getProperties().toString());
        }
        processEventProperties(nme.getProperties());
    }

    private void setNodePositionFromEventProps(Node node, Properties eventProps) {

        String geo = eventProps.getProperty(LAT_FIELD);
        if (geo != null) {
            try {
                node.setLat(Float.parseFloat(geo));
                node.posLat = geo;
            } catch (Exception e) {
                Debug.error("NetMapGraphicLoader: " + geo
                        + " is not a valid latitude value.");
            }
        }

        geo = eventProps.getProperty(LON_FIELD);

        if (geo != null) {
            try {
                node.setLon(Float.parseFloat(geo));
                node.posLon = geo;
            } catch (Exception e) {
                Debug.error("NetMapGraphicLoader: " + geo
                        + " is not a valid longitude value.");
            }
        }
    }

    /**
     * Process a NetMapEvent Properties object, which means that a Properties
     * object, representing an event from the NetMap server, is evaluated and
     * used to modify the NodeCache and LineCache accordingly.
     * 
     * @param eventProps the properties from a NetMapEvent.
     */
    protected void processEventProperties(Properties eventProps) {
        int status;
        Node node;
        Line line;
        String cmd = eventProps.getProperty(COMMAND_FIELD);

        // Used for many (if not all commands, might as well do this
        // here.
        int index = PropUtils.intFromProperties(eventProps,
                INDEX_FIELD,
                ERROR_VALUE_INT);
        if (cmd.equals(NODE_OBJECT)) {

            int shape = PropUtils.intFromProperties(eventProps,
                    SHAPE_FIELD,
                    ERROR_VALUE_INT);

            if (index == ERROR_VALUE_INT) {
                Debug.error("NMGL: error parsing object index for node.");
                return;
            }

            node = nodeList.get(index);

            if (shape == 11) {
                String icon = eventProps.getProperty(ICON_FIELD);
                if (DEBUG)
                    Debug.output("NetMapReader: jimage  " + icon);
            }

            if (shape == NODE_DELETE) { // Delete

                // While we're at it, we might as well delete all the
                // "Line" entries that terminate on this node as
                // well...
                if (node != null) {
                    lineList.del(node);
                    nodeList.del(node);
                }

            } else if (shape == NODE_MOVE && node != null) { // move

                setNodePositionFromEventProps(node, eventProps);
                lineList.move(node);
                node.setTime(Double.parseDouble(eventProps.getProperty(TIME_FIELD,
                        "0")));

            } else {

                // Define a new entry if "shape" is anything else,
                // including NODE_MOVE without a valid node.

                /*
                 * int posX = LayerUtils.intFromProperties(eventProps,
                 * POSX_FIELD, ERROR_VALUE_INT);
                 * 
                 * int posY = LayerUtils.intFromProperties(eventProps,
                 * POSY_FIELD, ERROR_VALUE_INT);
                 * 
                 * int width = LayerUtils.intFromProperties(eventProps,
                 * WIDTH_FIELD, ERROR_VALUE_INT);
                 * 
                 * int height = LayerUtils.intFromProperties(eventProps,
                 * HEIGHT_FIELD, ERROR_VALUE_INT);
                 */
                status = PropUtils.intFromProperties(eventProps,
                        STATUS_FIELD,
                        0);
                int menu = PropUtils.intFromProperties(eventProps,
                        MENU_FIELD,
                        0);
                /*
                 * int joffset = LayerUtils.intFromProperties(eventProps,
                 * JOFFSET_FIELD, ERROR_VALUE_INT);
                 */

                String label = eventProps.getProperty(LABEL_FIELD);
                if (label == null) {
                    // This label misdirection is temporary...
                    label = eventProps.getProperty(INDEX_FIELD);
                }

                String ip = eventProps.getProperty(IP_FIELD);

                /*
                 * float elevation = LayerUtils.floatFromProperties(eventProps,
                 * ELEVATION_FIELD, 0f);
                 */

                boolean isLocalhost = false;
                if (ip != null && localhostIP != null) {
                    isLocalhost = localhostIP.equals(ip);
                    if (DEBUG) {
                        Debug.output("NetMapGraphicLoader displaying a node running on the localhost: "
                                + localhostIP);
                    }
                }

                if (DEBUG) {
                    Debug.output("Creating node (" + label + ")");
                }

                try {
                    if (shape != ERROR_VALUE_INT) {
                        node = nodeList.add(label, index, shape, menu, status);
                        node.setLocalhost(isLocalhost);
                    }
                } catch (Exception e) {
                    Debug.error("NMGL: error creating node");
                }
                setNodePositionFromEventProps(node, eventProps);
            }

        } else if (cmd.equals(NODE_OBJECT_STATUS)) {

            if (index == ERROR_VALUE_INT) {
                Debug.error("NMGL: error parsing object index for status update.");
                return;
            }

            node = nodeList.get(index);

            if (node != null) {
                status = PropUtils.intFromProperties(eventProps,
                        STATUS_FIELD,
                        ERROR_VALUE_INT);
                if (status != ERROR_VALUE_INT) {
                    node.setStatus(status);
                }
            }

        } else if (cmd.equals(LINK_OBJECT_STATUS)) {

            if (index == ERROR_VALUE_INT) {
                Debug.error("NMGL: error parsing line index for status update.");
                return;
            }

            line = lineList.get(index);

            if (line != null) {
                status = PropUtils.intFromProperties(eventProps,
                        STATUS_FIELD,
                        ERROR_VALUE_INT);
                if (status != ERROR_VALUE_INT) {
                    line.setStatus(status);
                }
            }

        } else if (cmd.equals(LINK_OBJECT)) {

            if (index == ERROR_VALUE_INT) {
                Debug.error("NMGL: error parsing line index for link.");
                return;
            }

            line = lineList.get(index);

            int shape = PropUtils.intFromProperties(eventProps,
                    SHAPE_FIELD,
                    ERROR_VALUE_INT);

            if (shape == NODE_DELETE) {
                lineList.del(index);
            } else {
                status = PropUtils.intFromProperties(eventProps,
                        STATUS_FIELD,
                        0);
                int node1 = PropUtils.intFromProperties(eventProps,
                        LINK_NODE1_FIELD,
                        ERROR_VALUE_INT);
                int node2 = PropUtils.intFromProperties(eventProps,
                        LINK_NODE2_FIELD,
                        ERROR_VALUE_INT);

                if (node1 == ERROR_VALUE_INT || node2 == ERROR_VALUE_INT) {
                    Debug.error("NMGL: error parsing node indexes for link");
                    return;
                }

                Node n1 = nodeList.get(node1);
                Node n2 = nodeList.get(node2);

                if (n1 != null && n2 != null) {
                    lineList.add(String.valueOf(index),
                            index,
                            shape,
                            status,
                            n1,
                            n2);
                } else {
                    if (DEBUG) {
                        Debug.output("NetMapGraphicLoader: can't create lobj, nodes are undefined");
                    }
                }
            }

        } else if (cmd.equals(REFRESH) || cmd.equals(UPDATE)) {
            // manageGraphics();
        } else if (cmd.equals(CLEAR)) {
            if (nodeList != null) {
                nodeList.flush();
            }
            if (lineList != null) {
                lineList.flush();
            }
            // manageGraphics();
        } else {
            if (DEBUG) {
                Debug.output("NMGL: received unused event: "
                        + eventProps.toString());
            }
        }
        manageGraphics();
    }

    /**
     * Internal method used to create a single OMGraphicList from the NodeCache
     * and the LineCache.
     */
    protected OMGraphicList getOMList() {

        /*
         * By creating a new list, we avoid ConcurrentModificationExceptions
         * within the PlugInLayer, and within the GraphicLoaderPlugIn during
         * generate().
         */
        omList = new OMGraphicList();

        if (nodeList != null) {
            Enumeration list = nodeList.elements();

            while ((list != null) && list.hasMoreElements()) {
                Node point = (Node) list.nextElement();
                point.setMatted(point.isLocalhost());
                omList.add(point);
            }
        }

        if (lineList != null) {
            Enumeration list = lineList.elements();
            while ((list != null) && list.hasMoreElements()) {
                Line line = (Line) list.nextElement();

                if (line == null)
                    continue;
                line.setPos();

                omList.add((OMLine) line);
            }
        }

        if (DEBUG) {
            int size = omList.size();
            Debug.output("NMGL.getOMList(): created list with " + size
                    + (size == 1 ? " graphic." : " graphics."));
        }

        return omList;
    }

    /**
     * Called by the GraphicLoaderPlugIn, the GUI is provided from the
     * NetMapConnector.
     */
    public Component getGUI() {
        if (connector != null) {
            return connector.getGUI();
        } else
            return null;
    }

    /**
     * Needed to fill in a GUI with a receiver's name, to enable the user to
     * send a graphic to a specific object. Should be a pretty name, suitable to
     * let a user know what it is.
     */
    public String getName() {
        return "NetMap";
    }

    /**
     * The inherited AbstractGraphicLoader method, that sends the current
     * OMGrapicList to the receiver.
     */
    public void manageGraphics() {
        // receiver is inherited from
        // com.bbn.openmap.graphicLoader.MMLGraphicLoader/AbstractGraphicLoader.

        if (receiver != null) {
            if (DEBUG) {
                Debug.output("NetMapConnector.update: Updating graphics.");
            }
            receiver.setList(getOMList());
        } else {
            if (DEBUG) {
                Debug.output("NetMapConnector.update: no receiver to notify.");
            }
        }
    }

    protected boolean toolTipUp = false;

    /**
     * Invoked when the mouse button has been moved on a component (with no
     * buttons down).
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseMoved(java.awt.event.MouseEvent e) {
        if (receiver instanceof PlugIn && omList != null) {
            OMGraphic graphic = omList.getContains(e.getX(), e.getY());
            String label = null;
            if (graphic instanceof Node) {
                label = ((Node) graphic).getLabel();
                // } else if (graphic instanceof Line) {
                // label = ((Line)graphic).getLabel();
            }
            if (receiver instanceof PlugIn) {
                Component comp = ((PlugIn) receiver).getComponent();
                if (comp instanceof Layer) {
                    if (graphic != null && label != null) {
                        ((Layer) comp).fireRequestToolTip("Node " + label);
                        toolTipUp = true;
                    } else if (toolTipUp) {
                        ((Layer) comp).fireHideToolTip();
                        toolTipUp = false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
