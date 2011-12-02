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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkLayer.java,v $
// $RCSfile: LinkLayer.java,v $
// $Revision: 1.17 $
// $Date: 2007/06/21 21:39:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

/*  Java Core  */
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The LinkLayer is a Swing component, and an OpenMap layer, that communicates
 * with a server via the Link protocol. It transmits graphics requests and
 * gesture information, and handles the responses to those queries. The entry in
 * the openmap.properties file looks like this:
 * <P>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 *       # port number of server
 *       link.port=3031
 *      
 *       # host name of server
 *       link.host=host.com
 *      
 *       # URL of properties file for server attributes.  Properties
 *       # contained in this file are passed directly to the server to provide
 *       # additional information to the server about how to provide the
 *       # graphics.  Some standard properties are listed in the
 *       # LinkPropertiesConstants file, but any property can be passed to the
 *       # server.  How the server handles the property depends on the server,
 *       # but non-applicable properties are ignored.
 *       link.propertiesURL=http://location.of.properties.file.com
 * 
 * 
 * 
 * 
 * </pre>
 * 
 * You have to call setProperties() on this layer to set its parameters, and to
 * start the thread that listens to updates from the server.
 */
public class LinkLayer extends OMGraphicHandlerLayer implements
        MapMouseListener, LinkPropertiesConstants, LinkActionConstants,
        DrawingToolRequestor {

    /**
     * The thread listener used to communicate asynchronously. The LinkLayer
     * sends out requests and notifications to the server, and the LinkListener
     * reads any input from the server, making calls on the LinkLayer as
     * appropriate.
     */
    protected LinkListener listener;
    /**
     * A masked integer describing which gestures should be sent to the server.
     */
    protected int gestureDescriptor = 0;
    /** The port to connect to the server on. */
    protected int port;
    /** The host where the server is running. */
    protected String host;
    /**
     * The special parameters (attributes) transmitted to the server with every
     * query.
     */
    protected LinkProperties args;
    /**
     * The object that provides a link to the layer (and its various threads) on
     * a coordinateed basis.
     */
    protected LinkManager linkManager = null;
    /** The flag to suppress pop-up messages. */
    protected boolean quiet = false;
    /** The generator to use with LinkGrid objects. */
    protected OMGridGenerator currentGenerator = null;

    /**
     * The property name to specify what port the server is running on. "port"
     */
    public final static String PortProperty = "port";
    /**
     * The property name to specify the hostname the server is running on.
     * "host"
     */
    public final static String HostProperty = "host";
    /**
     * The property name to specify a URL of a properties file containing
     * properties that will be sent to the server within requests to it. The
     * contents of this file depends on the server. "propertiesURL"
     */
    public final static String ArgsProperty = "propertiesURL";
    public final static String ServerLocationProperty = "isl";
    /**
     * The property to make the layer quiet. "quiet"
     */
    public final static String QuietProperty = "quiet";
    /**
     * The property to specify which grid generator to use for grid objects.
     * "gridGenerator"
     */
    public final static String GridGeneratorProperty = "gridGenerator";
    /**
     * The property to set a pixel distance limit for gestures. "distanceLimit"
     */
    public final static String DistanceLimitProperty = "distanceLimit";

    public final static int DEFAULT_DISTANCE_LIMIT = 4;

    /**
     * The maximum distance away a mouse event can happen away from a graphic in
     * order for it to be considered to have touched.
     */
    protected int distanceLimit = DEFAULT_DISTANCE_LIMIT;

    /**
     * The property to set to true if the server should be able to decide when
     * to kill the client, the overall application. False by default, only
     * modified in setProperties. "exitOnCommand"
     */
    public final static String ExitOnCommandProperty = "exitOnCommand";

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public LinkLayer() {
        // We don't want to reset the OMGraphicsList automatically
        // when the projection changes, now. With ansynchronous
        // behavior, the current list should be reprojected and the
        // server notified, and the server will update itself if
        // needed.
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(
                this) {
            // Modified so it doesn't reset the OMGraphicList when
            // the SwingWorker thread returns. The list has
            // already been nulled out, will be reset when the
            // asynchronous thread decides it is.
            public void workerComplete(OMGraphicList list) {
            }
        });
    }

    /**
     * Constructor to use when LinkLayer is not being used with OpenMap
     * application.
     * 
     * @param host
     *            the hostname of the server's computer.
     * @param port
     *            the port number of the server.
     * @param propertiesURL
     *            the URL of a properties file that contains parameters for the
     *            server.
     */
    public LinkLayer(String host, int port, String propertiesURL) {
        this();
        this.host = host;
        this.port = port;
        linkManager = new LinkManager(host, port);

        args = new LinkProperties();

        if (propertiesURL != null) {
            try {
                URL propertiesFile = new URL(propertiesURL);
                args.load(propertiesFile.openStream());
            } catch (java.net.MalformedURLException mue) {
                System.err.println("LinkLayer:  Properties URL isn't valid: "
                        + propertiesURL);
                System.err.println(mue);
            } catch (IOException ioe) {
                System.err
                        .println("LinkLayer: IOException reading properties file:");
                System.err.println(ioe);
            }
        }
    }

    /**
     * Sets the current graphics list to the given list.
     * 
     * @param aList
     *            a list of OMGraphics
     */
    public synchronized void setGraphicList(LinkOMGraphicList aList) {
        super.setList(aList);
    }

    /**
     * Retrieves the current graphics list.
     */
    public synchronized LinkOMGraphicList getGraphicList() {
        return (LinkOMGraphicList) getList();
    }

    /**
     * Called when the layer is no longer part of the map. In this case, we
     * should disconnect from the server if we have a link.
     */
    public void removed(Container cont) {
        linkManager.resetLink();
    }

    /**
     * Sets the masked integer which indicates what types of events get sent to
     * the server.
     * 
     * @param descriptor
     *            masked int
     * @see LinkActionRequest
     */
    public synchronized void setGestureDescriptor(int descriptor) {
        gestureDescriptor = descriptor;
    }

    /**
     * Gets the masked integer which indicates what types of events get sent to
     * the server.
     * 
     * @return descriptor masked int
     * @see LinkActionRequest
     */
    public synchronized int getGestureDescriptor() {
        return gestureDescriptor;
    }

    /**
     * Set all the Link properties from a properties object.
     * 
     * @param prefix
     *            the prefix to the properties that might individualize it to a
     *            particular layer.
     * @param properties
     *            the properties for the layer.
     */
    public void setProperties(String prefix, Properties properties) {

        super.setProperties(prefix, properties);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        String quietString = properties.getProperty(realPrefix + QuietProperty);
        if (quietString != null && quietString.intern().equals("true")) {
            quiet = true;
        }

        host = properties.getProperty(realPrefix + HostProperty);

        port = PropUtils.intFromProperties(properties, realPrefix
                + PortProperty, LinkServerStarter.DEFAULT_PORT);

        linkManager = new LinkManager(host, port);

        linkManager.setObeyCommandToExit(PropUtils
                .booleanFromProperties(properties, realPrefix
                        + ExitOnCommandProperty, false));

        String propertiesURL = properties
                .getProperty(realPrefix + ArgsProperty);

        args = new LinkProperties(); // Empty if not filled.
        if (propertiesURL != null) {
            try {
                URL propertiesFile = new URL(propertiesURL);
                args.load(propertiesFile.openStream());

                // Need to do something here. The LinkPropertiesConstants have
                // changed in LinkProtocol version .6, to much small strings
                // that don't match up with the DrawingAttributes properties. We
                // need to check for the old property names and replace them
                // with the new property names.

                checkAndReplaceOldPropertyNames(args);

            } catch (java.net.MalformedURLException mue) {
                System.err.println("LinkLayer:  Properties URL isn't valid: "
                        + realPrefix + ArgsProperty);
                System.err.println(mue);
            } catch (IOException ioe) {
                System.err
                        .println("LinkLayer: IOException reading properties file:");
                System.err.println(ioe);
            }
        }

        currentGenerator = (OMGridGenerator) PropUtils
                .objectFromProperties(properties, realPrefix
                        + GridGeneratorProperty);

        if (currentGenerator == null) {
            Debug.message("linkdetail", getName()
                    + "|LinkLayer: no generator for grid objects.");
        }

        distanceLimit = PropUtils.intFromProperties(properties, realPrefix
                + DistanceLimitProperty, distanceLimit);

        // listener = new LinkListener(linkManager, this,
        // currentGenerator);
    }

    /**
     * The LinkPropertiesConstants have changed in LinkProtocol version .6, to
     * much small strings that don't match up with the DrawingAttributes
     * properties. We need to check for the old property names and replace them
     * with the new property names.
     * 
     * @param props
     */
    public void checkAndReplaceOldPropertyNames(LinkProperties props) {
        checkAndReplaceOldPropertyName(props, LPC_OLD_LINECOLOR, LPC_LINECOLOR);
        checkAndReplaceOldPropertyName(props, LPC_OLD_LINESTYLE, LPC_LINESTYLE);
        checkAndReplaceOldPropertyName(props, LPC_OLD_HIGHLIGHTCOLOR,
                                       LPC_HIGHLIGHTCOLOR);
        checkAndReplaceOldPropertyName(props, LPC_OLD_FILLCOLOR, LPC_FILLCOLOR);
        checkAndReplaceOldPropertyName(props, LPC_OLD_FILLPATTERN,
                                       LPC_FILLPATTERN);
        checkAndReplaceOldPropertyName(props, LPC_OLD_LINEWIDTH, LPC_LINEWIDTH);
        checkAndReplaceOldPropertyName(props, LPC_OLD_LINKTEXTSTRING,
                                       LPC_LINKTEXTSTRING);
        checkAndReplaceOldPropertyName(props, LPC_OLD_LINKTEXTFONT,
                                       LPC_LINKTEXTFONT);
    }

    public void checkAndReplaceOldPropertyName(LinkProperties props,
                                               String oldPropertyName,
                                               String newPropertyName) {
        String property = props.getProperty(oldPropertyName);
        if (property != null) {
            props.remove(oldPropertyName);
            props.put(newPropertyName, property);
        }
    }

    protected void setListener(LinkListener ll) {
        listener = ll;
    }

    protected LinkListener getListener() {
        return listener;
    }

    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method call is made on the link.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this happens,
     * the map will set the cancel bit in the LayerThread, (the thread that is
     * running the prepare). If this Layer needs to do any cleanups during the
     * abort, it should do so, but return out of the prepare asap.
     * 
     * @return a list of graphics.
     */
    public synchronized OMGraphicList prepare() {

        OMGraphicList currentList = getList();

        if (listener == null) {
            listener = new LinkListener(linkManager, this, currentGenerator);
        }

        if (listener != null && !listener.isListening()) {
            // Call LinkListener to launch SwingWorker to kick off a
            // thread for the listener.
            listener.startUp();
        }

        if (Debug.debugging("link")) {
            Debug.output(getName() + "|LinkLayer.prepare(): Listener "
                    + (listener == null ? "is null," : "is OK,")
                    + " listening ("
                    + (listener == null ? "nope" : "" + listener.isListening())
                    + ")");
        }

        if (isCancelled()) {
            Debug.message("link", getName() + "|LinkLayer.prepare(): aborted.");
            return currentList;
        }

        Projection projection = getProjection();
        if (projection == null) {
            Debug
                    .error("Link Layer needs to be added to the MapBean before it can get graphics!");
            return currentList;
        } else if (currentList != null) {
            // If the list isn't empty, it isn't being cleared when a
            // new projection is received, as dictated by the policy
            // of the layer. Should regenerate it here. If it's
            // understood that a new list will be sent by the server,
            // then a different ProjectionChangePolicy should be used.
            currentList.generate(projection);
        }

        Debug.message("basic", getName() + "|LinkLayer.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // LinkOMGraphicList is made up of OMGraphics, which are
        // generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        // call getRectangle();
        if (Debug.debugging("link")) {
            System.out.println(getName() + "|LinkLayer.prepare(): "
                    + "calling getRectangle " + " with projection: "
                    + projection + " ul = " + projection.getUpperLeft()
                    + " lr = " + projection.getLowerRight());
        }

        // LinkOMGraphicList omGraphicList;

        // //////////// Call getRectangle for server....
        try {
            // We do want the link object here... If another thread is
            // using the link, wait.
            ClientLink l = linkManager.getLink(true);

            if (l == null) {
                System.err
                        .println("LinkLayer: unable to get link in prepare().");
                return currentList;
            }

            synchronized (l) {
                // omGraphicList = getGraphics(l, projection);
                sendMapRequest(l, projection);
            }

            linkManager.finLink();

        } catch (UnknownHostException uhe) {
            System.err.println("LinkLayer: unknown host!");
            // return currentList;
        } catch (java.io.IOException ioe) {
            System.err
                    .println("LinkLayer: IOException contacting server for map request!");
            System.err.println(ioe);

            linkManager.resetLink();

            if (!quiet) {
                fireRequestMessage("Communication error between " + getName()
                        + " layer\nand Link Server: Host: " + host + ", Port: "
                        + port);
            }

            System.err.println("LinkLayer: Communication error between "
                    + getName() + " layer\nand Link Server: Host: " + host
                    + ", Port: " + port);
            // return currentList;
        }

        // ///////////////////////////////////////////////////
        // With asynchronous behavior, we don't listen to the reply
        // now. The LinkListener will handle setting the new
        // OMGraphicList if one is needed as decided by the server.

        // ///////////////////
        // safe quit
        // int size = 0;
        // if (omGraphicList != null) {
        // size = omGraphicList.size();

        // if (Debug.debugging("basic")) {
        // System.out.println(getName()+
        // "|LinkLayer.prepare(): finished with "+
        // size+" graphics");
        // }

        // // omGraphicList.project(projection);
        // }
        // else
        // Debug.message("basic", getName()+
        // "|LinkLayer.prepare(): finished with null graphics list");

        return currentList;
    }

    /**
     * Creates the LinkMapRequest.
     * 
     * @param link
     *            the link to communicate over.
     * @param proj
     *            the projection to give to the graphics.
     * @throws IOException
     */
    protected void sendMapRequest(ClientLink link, Projection proj)
            throws IOException {

        Point2D ul = proj.getUpperLeft();
        Point2D lr = proj.getLowerRight();
        float ulLat = (float) ul.getY();
        float ulLon = (float) ul.getX();
        float lrLat = (float) lr.getY();
        float lrLon = (float) lr.getX();

        LinkBoundingPoly[] boundingPolys = null;

        if (ProjMath.isCrossingDateline(ulLon, lrLon, proj.getScale())) {
            Debug.message("link", "Dateline is on screen");

            float ymin = (float) Math.min(ulLat, lrLat);
            float ymax = (float) Math.max(ulLat, lrLat);

            // xmin, ymin, xmax, ymax
            boundingPolys = new LinkBoundingPoly[2];
            boundingPolys[0] = new LinkBoundingPoly(ulLon, ymin, 180.0f, ymax);
            boundingPolys[1] = new LinkBoundingPoly(-180.0f, ymin, lrLon, ymax);

        } else {
            boundingPolys = new LinkBoundingPoly[1];
            boundingPolys[0] = new LinkBoundingPoly(ulLon, lrLat, lrLon, ulLat);
        }

        Point2D center = proj.getCenter();
        LinkMapRequest.write((float) center.getY(), (float) center.getX(), proj
                .getScale(), proj.getHeight(), proj.getWidth(), boundingPolys,
                             args, link);

        // ///////////////////////////////////////////////////
        // With asynchronous behavior, we don't listen to the reply
        // now. The LinkListener will handle it.

        // link.readAndParse(proj, currentGenerator);

        // // While we are here, check for any change in gesture query
        // // requests.
        // LinkActionRequest lar = link.getActionRequest();
        // if (lar != null) {
        // setGestureDescriptor(lar.getDescriptor());
        // }

        // handleLinkGraphicList(link.getGraphicList());
        // ///////////////////////////////////////////////////
    }

    public void handleLinkGraphicList(LinkGraphicList lgl) {
        Debug.message("link", "LinkLayer.handleLinkGraphicList()");

        if (lgl != null) {
            // Deal with all the messaging....
            handleMessages(lgl.getProperties());
            LinkOMGraphicList lomgl = lgl.getGraphics();
            setGraphicList(lomgl);
            // Do we need to regenerate?
            Projection proj = getProjection();
            if (lomgl.getNeedToRegenerate(proj)) {
                // set to false in LinkGraphicList.readGraphics if the
                // projection was there when the LinkGraphicList was
                // created. If it wasn't there, we need to try to
                // project them before calling repaint(). Projection
                // will be null if the layer hasn't been added to the
                // map.
                lomgl.generate(proj);
            }

            repaint();
        }
    }

    public void handleLinkActionList(LinkActionList lal) {
        Debug.message("link", "LinkLayer.handleLinkActionList()");

        if (lal == null) {
            return;
        }

        handleMessages(lal.getProperties());

        // The only thing we need to do is handle any gesture
        // changes...
        Vector updates = lal.getGraphicUpdates();
        Enumeration items = updates.elements();
        boolean needRepaint = false;
        LinkOMGraphicList graphics = getGraphicList();

        Projection proj = getProjection();

        if (graphics == null) {
            Debug
                    .message("link",
                             "LinkLayer.handleLinkActionList: null LinkOMGraphicList, making new one...");
            // Why ignore what the server has to say, set the new
            // OMGraphicList and react accordingly.
            graphics = new LinkOMGraphicList();
            setGraphicList(graphics);
        }

        while (items.hasMoreElements()) {
            needRepaint = true; // We do!
            GraphicUpdate gu = (GraphicUpdate) items.nextElement();

            if (gu == null) {
                Debug
                        .message("link",
                                 "LinkLayer.handleLinkActionList: null GraphicUpdate, skipping...");
                continue;
            }

            // Take care of this first.....
            if (LinkUtil.isMask(gu.action, MODIFY_DESELECTALL_GRAPHIC_MASK)) {
                Debug
                        .message("link",
                                 "LinkLayer.handleLinkActionList: deselecting all graphics");
                graphics.deselect();
            }

            // Find the graphic that we are talking about - if the
            // ID is not "none", or if the id doesn't match the
            // gesGraphic LinkGraphicID, then look for the new
            // graphic. Otherwise, assume that the gesGraphic is
            // the one that the action refers to.

            // This code was moved from handleGesture to here, the
            // main difference being that in handleGesture any actual
            // OMGraphic that was gestured over was already known at
            // this point, and there was no sense looking for it if
            // you already had it. Since we moved the code, and this
            // method is being called from a different thread, we
            // don't have that luxury - we have to look up the
            // OMGraphic again...

            OMGraphic gug = gu.graphic;
            OMGraphic reactionGraphic = null;
            int reactionGraphicIndex = Link.UNKNOWN;

            if (LinkUtil.isMask(gu.action, UPDATE_ADD_GRAPHIC_MASK)) {
                if (Debug.debugging("link")) {
                    Debug
                            .output("LinkLayer.handleLinkActionList: adding graphic, id:"
                                    + gu.id);
                }
                if (gug != null) {
                    gug.generate(proj);
                    graphics.add(gug);
                    reactionGraphic = gug;
                } else {
                    Debug.message("link",
                                  "LinkLayer.handleLinkActionList: trying to add null OMGraphic, id: "
                                          + gu.id);
                }
            } else if (gu.id != null) {
                reactionGraphicIndex = graphics.getOMGraphicIndexWithId(gu.id);
                if (reactionGraphicIndex == Link.UNKNOWN) {
                    // Must be an addition/new graphic
                    if (LinkUtil.isMask(gu.action, UPDATE_ADD_GRAPHIC_MASK)) {
                        // If gu.graphic is null, this will throw an
                        // exception
                        if (Debug.debugging("link")) {
                            Debug
                                    .output("LinkLayer.handleLinkActionList: adding graphic "
                                            + gu.id);
                        }
                        if (gug != null) {
                            gug.generate(proj);
                            graphics.add(gug);
                            reactionGraphic = gug;
                        } else {
                            Debug.message("link",
                                          "LinkLayer.handleLinkActionList: trying to add null OMGraphic, id: "
                                                  + gu.id);
                        }
                    } else {
                        gu.action = 0; // No action...
                        Debug
                                .error("LinkLayer.handleLinkActionList: Gesture Response on an unknown graphic.");
                    }
                } else if (LinkUtil.isMask(gu.action, UPDATE_GRAPHIC_MASK)) {
                    if (gug != null) {
                        gug.generate(proj);
                        reactionGraphic = gug;
                    } else {
                        Debug.message("link",
                                      "LinkLayer.handleLinkActionList: trying to update null OMGraphic, id: "
                                              + gu.id);
                    }
                } else {
                    reactionGraphic = graphics.getOMGraphicWithId(gu.id);
                }
            } else {
                Debug
                        .error("LinkLayer.handleLinkActionList:  null ID for graphic");
            }

            // Now, perform the appropriate action on the graphic...

            // Delete a graphic... If you do this, nothing else
            // gets done on the graphic...
            if (LinkUtil.isMask(gu.action, MODIFY_DELETE_GRAPHIC_MASK)) {
                Debug.message("link", "LinkLayer: deleting graphic");
                graphics.remove(reactionGraphicIndex);
            } else {

                // For properties updating, or graphic replacement
                if (LinkUtil.isMask(gu.action, UPDATE_GRAPHIC_MASK)) {
                    Debug.message("link", "LinkLayer: updating graphic");
                    graphics.setOMGraphicAt(reactionGraphic,
                                            reactionGraphicIndex);
                }

                if (reactionGraphic != null) {
                    // For graphic selection and deselection
                    if (LinkUtil.isMask(gu.action, MODIFY_SELECT_GRAPHIC_MASK)) {
                        Debug.message("link", "LinkLayer: selecting graphic");
                        reactionGraphic.select();
                    } else if (LinkUtil.isMask(gu.action,
                                               MODIFY_DESELECT_GRAPHIC_MASK)) {
                        Debug.message("link", "LinkLayer: deselecting graphic");
                        reactionGraphic.deselect();
                    }
                }

                // Now, raising or lowering the graphic...
                if (LinkUtil.isMask(gu.action, MODIFY_RAISE_GRAPHIC_MASK)) {
                    Debug.message("link", "LinkLayer: raising graphic");
                    graphics.moveIndexedToTop(reactionGraphicIndex);
                } else if (LinkUtil
                        .isMask(gu.action, MODIFY_LOWER_GRAPHIC_MASK)) {
                    Debug.message("link", "LinkLayer: lowering graphic");
                    graphics.moveIndexedToBottom(reactionGraphicIndex);
                }

            } // else if not deleting it...
        } // while

        if (lal.getNeedMapUpdate()) {
            updateMap(lal.getMapProperties());
            lal.setNeedMapUpdate(false);
            needRepaint = false;
        }

        if (needRepaint) {
            repaint();
        }
    }

    public void handleLinkActionRequest(LinkActionRequest lar) {
        Debug.message("link", "LinkLayer.handleLinkActionRequest()");
        if (lar != null) {
            setGestureDescriptor(lar.getDescriptor());
        }
    }

    /**
     * Looks at a properties object, and checks for the pre-defined messaging
     * attributes. Then, the information delegator is called to handle their
     * display.
     * 
     * @param props
     *            LinkProperties containing messages.
     */
    public void handleMessages(LinkProperties props) {
        String value = props.getProperty(LPC_INFO);
        if (value != null)
            fireRequestInfoLine(value);

        value = props.getProperty(LPC_URL);
        if (value != null) {
            fireRequestURL(value);
        } else {
            value = props.getProperty(LPC_HTML);
            if (value != null)
                fireRequestBrowserContent(value);
        }
        value = props.getProperty(LPC_MESSAGE);
        if (value != null)
            fireRequestMessage(value);
    }

    // ----------------------------------------------------------------------
    // MapMouseListener interface implementation
    // ----------------------------------------------------------------------
    /** Return the MapMouseListener for the layer. */
    public synchronized MapMouseListener getMapMouseListener() {
        return this;
    }

    /**
     * Return the strings identifying the Mouse Modes that the MapMouseListener
     * wants to receive gestures from.
     */
    public String[] getMouseModeServiceList() {
        String[] services = { SelectMouseMode.modeID };
        return services;
    }

    public boolean mousePressed(MouseEvent e) {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_PRESSED_MASK)) {
            return handleGesture(MOUSE_PRESSED_MASK, e);
        }
        return false;
    }

    public boolean mouseReleased(MouseEvent e) {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_RELEASED_MASK)) {
            return handleGesture(MOUSE_RELEASED_MASK, e);
        }
        return false;
    }

    public boolean mouseClicked(MouseEvent e) {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_CLICKED_MASK)) {
            return handleGesture(MOUSE_CLICKED_MASK, e);
        }
        return false;
    }

    public void mouseEntered(MouseEvent e) {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_ENTERED_MASK)) {
            handleGesture(MOUSE_ENTERED_MASK, e);
        }
    }

    public void mouseExited(MouseEvent e) {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_EXITED_MASK)) {
            handleGesture(MOUSE_EXITED_MASK, e);
        }
    }

    public boolean mouseDragged(MouseEvent e) {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_DRAGGED_MASK)) {
            return handleGesture(MOUSE_DRAGGED_MASK, e);
        }
        return false;
    }

    public boolean mouseMoved(MouseEvent e) {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_MOVED_MASK)) {
            return handleGesture(MOUSE_MOVED_MASK, e);
        }
        return false;
    }

    public void mouseMoved() {
        if (LinkUtil.isMask(getGestureDescriptor(), MOUSE_MOVED_MASK)) {
            handleGesture(MOUSE_MOVED_MASK, null);
        }
    }

    /**
     * Given a graphic and the type of gesture caught, react to it based on the
     * properties object located in the Graphic. The default behavior here is
     * that if the gesture is a MouseMoved, select the graphic, and if there is
     * an info line, show it. If the gesture is a MouseRelease, display the info
     * line, and also check the following, in this order: url and then html. If
     * there is a message property, the message is sent in a pop-up window.
     * 
     * @param graphic
     *            the graphic to check out.
     * @param descriptor
     *            the type of gesture.
     * @param e
     *            mouse event, to get location.
     * @return true if the server still needs to be told - per descriptor bit
     *         11.
     */
    protected boolean graphicGestureReaction(OMGraphic graphic, int descriptor,
                                             MouseEvent e) {
        LinkProperties props = (LinkProperties) graphic.getAppObject();

        // Mouse clicked
        boolean mc = LinkUtil.isMask(descriptor, MOUSE_CLICKED_MASK);
        // Mouse released
        boolean mr = LinkUtil.isMask(descriptor, MOUSE_RELEASED_MASK);
        // Mouse moved
        boolean mm = LinkUtil.isMask(descriptor, MOUSE_MOVED_MASK);
        // server inform
        boolean si = LinkUtil.isMask(getGestureDescriptor(),
                                     SERVER_NOTIFICATION_MASK);

        boolean ret = true;

        if (mr || mc) {
            String url = props.getProperty(LPC_URL);
            if (url != null) {
                if (Debug.debugging("link")) {
                    System.out
                            .println("LinkLayer:graphicGestureReaction: displaying url: "
                                    + url);
                }
                fireRequestURL(url);
                ret = si;
            } else {
                String html = props.getProperty(LPC_HTML);
                if (html != null) {
                    fireRequestBrowserContent(html);
                    ret = si;
                }
            }

            // Just reuse url instead of declaring another string
            // object
            url = props.getProperty(LPC_MESSAGE);
            if (url != null) {
                fireRequestMessage(url);
                ret = si;
            }
        }

        if (mr || mm || mc) {
            String info = props.getProperty(LPC_INFO);
            if (info != null) {
                if (Debug.debugging("link")) {
                    System.out
                            .println("LinkLayer:graphicGestureReaction: displaying info line: "
                                    + info);
                }
                fireRequestInfoLine(info);
                ret = si;
            }
        }

        return ret;
    }

    /**
     * Send the query, act on the response, and tell the caller if the gesture
     * was consumed. The Link actually gets a copy of the layer to handle
     * communication with the InformationDelegator. The GraphicUpdates are
     * handled in this method - the graphics list is modified.
     * 
     * @param descriptor
     *            a masked integer telling the type of gesture.
     * @param e
     *            the MouseEvent.
     * @return true if the event was consumed.
     */
    protected boolean handleGesture(int descriptor, MouseEvent e) {
        Debug.message("link", "LinkLayer: handleGesture:");

        try {
            LinkOMGraphicList graphics = getGraphicList(); // Get old
            // list

            OMGraphic gesGraphic = null;
            if (graphics == null) {
                // Nothing to search on - this condition occurs when
                // the layer is already busy getting new graphics as a
                // result of a changed projection.
                // It also occurs when the layer does not have any graphics
                // in it.
                Debug.message("link", "LinkLayer: null graphics list.");
            } else {
                if (e == null) {
                    graphics.deselect();
                    return false;
                }

                // Find out if a graphic is closeby...
                // int gesGraphicIndex = graphics.findIndexOfClosest(e.getX(),
                // e.getY(),
                // distanceLimit);

                // We need to do this to deselect everything else too.
                gesGraphic = graphics.selectClosest(e.getX(), e.getY(),
                                                    distanceLimit);
            }

            String id = null;

            // If there was a graphic, set the mask to indicate that,
            // and keep track of the graphic and the list index of the
            // graphic for the response. If a graphic modify command
            // comes back without an ID, then we'll assume the server
            // was referring to this graphic.
            if (gesGraphic != null) {

                boolean tellServer = graphicGestureReaction(gesGraphic,
                                                            descriptor, e);

                if (!tellServer) {
                    repaint();
                    return true;
                }

                // needRepaint = true; // Why? At this point, we
                // should wait to see what the server wants us to do,
                // we should only repaint if a graphic update comes
                // back.

                descriptor = LinkUtil.setMask(descriptor, GRAPHIC_ID_MASK);
                id = ((LinkProperties) gesGraphic.getAppObject())
                        .getProperty(LPC_GRAPHICID);
            } else {
                // clear out info line
                fireRequestInfoLine("");
            }

            // server inform
            if (!LinkUtil.isMask(getGestureDescriptor(),
                                 SERVER_NOTIFICATION_MASK)) {
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
                Debug
                        .message("link",
                                 "LinkLayer: unable to get link in handleGesture().");
                return false;
            }

            // Using the link - carefully prevent others from using it
            // too!
            synchronized (l) {
                if (id != null) {
                    args.setProperty(LPC_GRAPHICID, id);
                } else {
                    // Reset this to prevent sending the id of a previously
                    // selected graphic when no graphic is clicked on.
                    args.remove(LPC_GRAPHICID);
                }

                // Send the query
                LinkActionRequest.write(descriptor, e, (float) llpoint.getY(),
                                        (float) llpoint.getX(), args, l);

                // ///////////////////////////////////////////////////
                // With asynchronous behavior, we don't listen to the
                // reply
                // now. The LinkListener will handle it.

                // // Read the response
                // l.readAndParse(getProjection(), currentGenerator,
                // this);

                // lal = l.getActionList();
                // lar = l.getActionRequest();

                // if (id != null) {
                // args.remove(LPC_GRAPHICID);
                // }
                // ///////////////////////////////////////////////////

            }

            linkManager.finLink();

            // ///////////////////////////////////////////////////
            // With asynchronous behavior, we don't listen to the
            // reply
            // now. The LinkListener will handle it.

            // handleLinkActionRequest(lar);

            // // If nothing else was returned concerning the gesture
            // query
            // if (lal == null) {
            // return false;
            // }

            // handleLinkActionList(lal);
            // return lal.consumedGesture();

            // ///////////////////////////////////////////////////

            // I don't know what to answer here, we really don't know
            // at this point. There may be something we can do to set
            // up some lag circle to start returning true if we need
            // to, but if we're not listening in this thread, we just
            // don't know if the gesture is consumed here and we can't
            // hold up the event thread to find out.
            return false;

        } catch (IOException ioe) {
            System.err
                    .println("LinkLayer: IOException contacting server during gesture handling!");
            System.err.println(ioe);
            linkManager.resetLink();
            return false;
        }
    }

    // DrawingToolRequestor method
    public void drawingComplete(OMGraphic omg, OMAction action) {
        // //////////// send the new graphic, along with instructions
        // on what to do with it, to the server.
        String id = null; // unknown

        Object obj = omg.getAppObject();
        LinkProperties lp = null;
        if (obj instanceof LinkProperties) {
            lp = (LinkProperties) obj;
            id = lp.getProperty(LPC_GRAPHICID);
        }

        if (id == null) {
            // Doesn't look like it was a modified graphic already
            // received from the server, so we should tell the server
            // to add it to its list.
            action.setMask(OMAction.ADD_GRAPHIC_MASK);
        }

        try {
            // We do want the link object here... If another thread is
            // using the link, wait.
            ClientLink l = linkManager.getLink(true);

            if (l == null) {
                System.err
                        .println("LinkLayer.drawingComplete: unable to get link.");
                return;
            }

            synchronized (l) {
                LinkActionList lal = new LinkActionList(l, new LinkProperties());

                if (action.isMask(OMAction.ADD_GRAPHIC_MASK)
                        || action.isMask(OMAction.UPDATE_GRAPHIC_MASK)) {
                    lal.writeGraphicGestureHeader(action.getValue());
                    LinkGraphic.write(omg, l);
                } else {
                    // This shouldn't ever get called with a null lp
                    // properties object. If the object is new or
                    // doesn't have an ID, the upper paragraph will
                    // get called.
                    lal.modifyGraphic(action.getValue(), lp);
                }
                lal.end(Link.END_TOTAL);
            }

            // ///////////////////////////////////////////////////
            // With asynchronous behavior, we don't listen to the
            // reply
            // now. The LinkListener will handle it.

            // l.readAndParse(getProjection(), currentGenerator);
            // ///////////////////////////////////////////////////

            linkManager.finLink();

        } catch (UnknownHostException uhe) {
            Debug.error("LinkLayer: unknown host!");
            return;
        } catch (java.io.IOException ioe) {
            Debug.error("LinkLayer: Communication error between " + getName()
                    + " layer\nand Link Server: Host: " + host + ", Port: "
                    + port + "LinkLayer: IOException contacting server!\n"
                    + ioe.getMessage());

            linkManager.resetLink();

            if (!quiet) {
                fireRequestMessage("Communication error between " + getName()
                        + " layer\nand Link Server: Host: " + host + ", Port: "
                        + port);
            }

            return;
        }
    }

    /**
     * Set the search distance limit pixel distance for graphics searches. When
     * the graphics list is checked for a graphic that is closest to a mouse
     * event, this is the pixel limit within hits are considered.
     * 
     * @param limit
     *            the pixel limit to consider something "closest".
     */
    public void setDistanceLimit(int limit) {
        if (limit < 0) {
            distanceLimit = 0;
        } else {
            distanceLimit = limit;
        }
    }

    /**
     * Get the search distance limit pixel distance for graphics searches.
     */
    public int getDistanceLimit() {
        return distanceLimit;
    }

    /**
     * Looks at a properties object, and checks for map updates.
     * 
     * @param props
     *            LinkProperties containing map parameters.
     */
    public void updateMap(LinkProperties props) {

        Proj projection = (Proj) getProjection();
        Point2D center = projection.getCenter();
        float latitude = PropUtils.floatFromProperties(props, LPC_CENTER_LAT,
                                                       (float) center.getY());
        float longitude = PropUtils.floatFromProperties(props, LPC_CENTER_LONG,
                                                        (float) center.getX());
        float scale = PropUtils.floatFromProperties(props, LPC_SCALE,
                                                    projection.getScale());
        int width = PropUtils.intFromProperties(props, LPC_WIDTH, projection
                .getWidth());
        int height = PropUtils.intFromProperties(props, LPC_HEIGHT, projection
                .getHeight());

        String projType = props.getProperty(LPC_PROJECTION);

        float latmin = PropUtils
                .floatFromProperties(props, LPC_LATMIN, -1000.f);
        float latmax = PropUtils
                .floatFromProperties(props, LPC_LATMAX, -1000.f);
        float lonmin = PropUtils
                .floatFromProperties(props, LPC_LONMIN, -1000.f);
        float lonmax = PropUtils
                .floatFromProperties(props, LPC_LONMAX, -1000.f);

        if (latmin >= -90.f && latmax <= 90.f && lonmin >= -180.f
                && lonmax <= 180.f && latmin <= latmax && lonmin <= lonmax) {
            // Calculate center point
            float dist = 0.5f * GreatCircle.sphericalDistance(ProjMath
                    .degToRad(latmax), ProjMath.degToRad(lonmin), ProjMath
                    .degToRad(latmin), ProjMath.degToRad(lonmax));
            float azimuth = GreatCircle.sphericalAzimuth(ProjMath
                    .degToRad(latmax), ProjMath.degToRad(lonmin), ProjMath
                    .degToRad(latmin), ProjMath.degToRad(lonmax));
            center = GreatCircle.sphericalBetween(ProjMath.degToRad(latmax),
                                                  ProjMath.degToRad(lonmin),
                                                  dist, azimuth);
            latitude = (float) center.getY();
            longitude = (float) center.getX();
        }

        MapHandler mapHandler = (MapHandler) getBeanContext();
        if (mapHandler == null) {
            Debug.message("link", "Warning...mapHandler = null");
        } else {
            MapBean mapBean = (MapBean) mapHandler
                    .get("com.bbn.openmap.MapBean");
            if (mapBean == null) {
                Debug.message("link", "Warning...mapBean = null");
            } else {
                center = new Point2D.Float(latitude, longitude);
                ProjectionFactory projFactory = mapBean.getProjectionFactory();
                if (projType != null) {
                    Class<? extends Projection> projClass = projFactory
                            .getProjClassForName(projType);
                    if (projClass == null) {
                        projClass = Mercator.class;
                    }
                    projection = (Proj) projFactory.makeProjection(projClass,
                                                                   center,
                                                                   scale,
                                                                   width,
                                                                   height);
                } else {
                    projection = (Proj) mapBean.getProjection();
                    projection.setCenter(center);
                    projection.setScale(scale);
                    projection.setWidth(width);
                    projection.setHeight(height);
                }

                if (latmin >= -90.f && latmax <= 90.f && lonmin >= -180.f
                        && lonmax <= 180.f && latmin <= latmax
                        && lonmin <= lonmax) {
                    Point2D upperLeft = new Point2D.Float(latmax, lonmin);
                    Point2D lowerRight = new Point2D.Float(latmin, lonmax);
                    scale = ProjMath
                            .getScale(upperLeft, lowerRight, projection);
                    projection.setScale(scale);
                    Point2D ul = projection.getUpperLeft();
                    Point2D lr = projection.getLowerRight();
                    double factor1 = (latmax - latmin)
                            / (ul.getY() - lr.getY());
                    double factor2 = (lonmax - lonmin)
                            / (lr.getX() - ul.getX());
                    if (factor2 > factor1)
                        factor1 = factor2;
                    if (factor1 > 1.0) {
                        scale *= factor1;
                        projection.setScale(scale);
                    }
                }

                mapBean.setProjection(projection);
            }
        }
    }
}
