// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;


/*  Java Core  */
import java.awt.event.*;
import java.awt.Container;
import java.util.Properties;
import java.util.Enumeration;
import java.io.*;
import java.net.*;

/*  OpenMap  */
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.SwingWorker;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.grid.*;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.util.PropUtils;

/**
 * The LinkLayer is a Swing component, and an OpenMap layer, that
 * communicates with a server via the Link protocol.  It transmits
 * graphics requests and gesture information, and handles the
 * responses to those queries.  The entry in the openmap.properties
 * file looks like this:
 * <P><pre>
 *
 * # port number of server
 * link.port=3031
 *
 * # host name of server
 * link.host=host.com
 *
 * # URL of properties file for server attributes.  Properties
 * # contained in this file are passed directly to the server to provide
 * # additional information to the server about how to provide the
 * # graphics.  Some standard properties are listed in the
 * # LinkPropertiesConstants file, but any property can be passed to the
 * # server.  How the server handles the property depends on the server,
 * # but non-applicable properties are ignored.
 * link.propertiesURL=http://location.of.properties.file.com
 * </pre> 
 */
public class LinkLayer extends OMGraphicHandlerLayer 
    implements MapMouseListener, LinkPropertiesConstants, LinkActionConstants, DrawingToolRequestor {
    
    /**
     * A masked integer describing which gestures should be sent to
     * the server. 
     */
    protected int gestureDescriptor = 0;
    /** The port to connect to the server on. */
    protected int port;
    /** The host where the server is running. */
    protected String host;
    /**
     * The special parameters (attributes) transmitted to the server
     * with every query. 
     */
    protected LinkProperties args;
    /**
     * The object that provides a link to the layer (and its various
     * threads) on a coordinateed basis.
     */
    protected LinkManager linkManager = null;
    /** The flag to supress pop-up messages. */
    protected boolean quiet = false;
    /** The generator to use with LinkGrid objects. */
    protected OMGridGenerator currentGenerator = null;

    /** 
     * The property name to specify what port the server is running
     * on. "port" 
     */
    public final static String PortProperty = "port";
    /** 
     * The property name to specify the hostname the server is running
     * on. "host" 
     */
    public final static String HostProperty = "host";
    /**
     * The property name to specify a URL of a properties file
     * containing properties that will be sent to the server within
     * requests to it.  The contents of this file depends on the
     * server. "propertiesURL" 
     */
    public final static String ArgsProperty = "propertiesURL";
    public final static String ServerLocationProperty = "isl";
    /**
     * The property to make the layer quiet. "quiet"
     */
    public final static String QuietProperty = "quiet";
    /**
     * The property to specify which grid generator to use for grid
     * objects. "gridGenerator"
     */
    public final static String GridGeneratorProperty = "gridGenerator";
    /**
     * The property to set a pixel distance limit for gestures. "distanceLimit"
     */
    public final static String DistanceLimitProperty = "distanceLimit";

    public final static int DEFAULT_DISTANCE_LIMIT = 4;

    /** The maximum distance away a mouse event can happen away from a
     *  graphic in order for it to be considered to have touched. */
    protected int distanceLimit = DEFAULT_DISTANCE_LIMIT;

    /**
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     */
    public LinkLayer() {}

    /**
     * Constructor to use when LinkLayer is not being used with
     * OpenMap application.
     * 
     * @param host the hostname of the server's computer.
     * @param port the port number of the server.
     * @param propertiesURL the URL of a properties file that contains
     * parameters for the server.
     */
    public LinkLayer(String host, int port, String propertiesURL) {

	this.host = host;
	this.port = port;
	linkManager = new LinkManager(host, port);

	args = new LinkProperties();

	if (propertiesURL != null) {
	    try{
		URL propertiesFile = new URL(propertiesURL);
		args.load(propertiesFile.openStream());
	    } catch (java.net.MalformedURLException mue) {
		System.err.println("LinkLayer:  Properties URL isn't valid: " +
				   propertiesURL);
		System.err.println(mue);
	    } catch (IOException ioe) {
		System.err.println("LinkLayer: IOException reading properties file:");
		System.err.println(ioe);
	    }
	}
    }

    /**
     * Sets the current graphics list to the given list.
     *
     * @param aList a list of OMGraphics
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
     *  Called when the layer is no longer part of the map.  In this
     *  case, we should disconnect from the server if we have a
     *  link. 
     */
    public void removed(Container cont) {
	linkManager.resetLink();
    }

    /** 
     * Sets the masked integer which indicates what types of events
     * get sent to the server. 
     *
     * @param descriptor masked int
     * @see LinkActionRequest
     */
    public synchronized void setGestureDescriptor(int descriptor) {
	gestureDescriptor = descriptor;
    }

    /** 
     * Gets the masked integer which indicates what types of events
     * get sent to the server. 
     *
     * @return descriptor masked int
     * @see LinkActionRequest
     */
    public synchronized int getGestureDescriptor() {
	return gestureDescriptor;
    }

    /**
     * Set all the Link properties from a properties object.
     * @param prefix the prefix to the properties tha might
     * individualize it to a particular layer.  
     * @param properties the properties for the layer.
     */
    public void setProperties(String prefix, 
			      java.util.Properties properties) {

	super.setProperties(prefix, properties);

	String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

	String quietString = properties.getProperty(realPrefix + QuietProperty);
	if (quietString != null && quietString.intern() == "true") {
	    quiet = true;
	}

	String portString = properties.getProperty(realPrefix + PortProperty);
	host = properties.getProperty(realPrefix + HostProperty);

	port = LayerUtils.intFromProperties(properties, realPrefix + PortProperty,
					    LinkServerStarter.DEFAULT_PORT);

	linkManager = new LinkManager(host, port);

	String propertiesURL = properties.getProperty(realPrefix + ArgsProperty);

	args = new LinkProperties();  // Empty if not filled.
	if (propertiesURL != null) {
	    try{
		URL propertiesFile = new URL(propertiesURL);
		args.load(propertiesFile.openStream());
	    } catch (java.net.MalformedURLException mue) {
		System.err.println("LinkLayer:  Properties URL isn't valid: " +
				   realPrefix + ArgsProperty);
		System.err.println(mue);
	    } catch (IOException ioe) {
		System.err.println("LinkLayer: IOException reading properties file:");
		System.err.println(ioe);
	    }
	}

	currentGenerator = (OMGridGenerator) LayerUtils.objectFromProperties(properties, realPrefix + GridGeneratorProperty);

	if (currentGenerator == null) {
	    Debug.message("linkdetail", getName()+
			  "|LinkLayer: no generator for grid objects.");
	}

	distanceLimit = LayerUtils.intFromProperties(properties, realPrefix + DistanceLimitProperty, distanceLimit);
    }

    /**
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the link.  <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the map will set the cancel bit in the
     * LayerThread, (the thread that is running the prepare).  If this
     * Layer needs to do any cleanups during the abort, it should do
     * so, but return out of the prepare asap.
     * @return a list of graphics.
     */
    public OMGraphicList prepare() {

	if (isCancelled()) {
	    Debug.message("link", getName()+"|LinkLayer.prepare(): aborted.");
	    return null;
	}
	Projection projection = getProjection();
	if (projection == null) {
	    System.err.println("Link Layer needs to be added to the MapBean before it can get graphics!");
	    return new LinkOMGraphicList();
	}

	Debug.message("basic", getName()+"|LinkLayer.prepare(): doing it");

	// Setting the OMGraphicsList for this layer.  Remember, the
	// LinkOMGraphicList is made up of OMGraphics, which are generated
	// (projected) when the graphics are added to the list.  So,
	// after this call, the list is ready for painting.

	// call getRectangle();
	if (Debug.debugging("link")) {
	    System.out.println(
		getName()+"|LinkLayer.prepare(): " +
		"calling getRectangle " +
		" with projection: " + projection +
		" ul = " + projection.getUpperLeft() + 
		" lr = " + projection.getLowerRight()); 
	}

	LinkOMGraphicList omGraphicList;

	////////////// Call getRectangle for server....
	try {
	    // We do want the link object here... If another thread is
	    // using the link, wait.
	    ClientLink l = linkManager.getLink(true);

	    if (l == null) {
		System.err.println("LinkLayer: unable to get link in prepare().");
		return new LinkOMGraphicList();
	    }

	    synchronized(l) {
		omGraphicList = getGraphics(l, projection);
	    }

	    linkManager.finLink();

	} catch (UnknownHostException uhe) {
	    System.err.println("LinkLayer: unknown host!");
	    return new LinkOMGraphicList();
	} catch (java.io.IOException ioe) {
	    System.err.println("LinkLayer: IOException contacting server for map request!");
	    System.err.println(ioe);

	    linkManager.resetLink();

	    if (!quiet) {
		fireRequestMessage("Communication error between " + getName() + 
				   " layer\nand Link Server: Host: " + host + 
				   ", Port: " + port);
	    }

	    System.err.println("LinkLayer: Communication error between " + getName() + 
			       " layer\nand Link Server: Host: " + host + 
			       ", Port: " + port);

	    return new LinkOMGraphicList();
	}

	/////////////////////
	// safe quit
	int size = 0;
	if (omGraphicList != null) {
	    size = omGraphicList.size();	

	    if (Debug.debugging("basic")) {
		System.out.println(getName()+
				   "|LinkLayer.prepare(): finished with "+
				   size+" graphics");
	    }

// 	    omGraphicList.project(projection);
	}
	else 
	    Debug.message("basic", getName()+
	      "|LinkLayer.prepare(): finished with null graphics list");

	return omGraphicList;
    }

    /**
     * Creates the LinkMapRequest, and gets the results.
     *
     * @param link the link to communicate over.
     * @param proj the projection to give to the graphics.
     * @return LinkOMGraphicList containing graphics from the server.
     * @throws IOException
     */
    protected LinkOMGraphicList getGraphics(ClientLink link, Projection proj) 
	throws IOException{

	LatLonPoint ul = proj.getUpperLeft();
	LatLonPoint lr = proj.getLowerRight();
	float ulLat = ul.getLatitude();
	float ulLon = ul.getLongitude();
	float lrLat = lr.getLatitude();
	float lrLon = lr.getLongitude();

	LinkBoundingPoly[] boundingPolys = null;

	if ((ulLon > lrLon) || MoreMath.approximately_equal(ulLon, lrLon, .001f)) {
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

	LinkMapRequest.write(proj.getCenter().getLatitude(),
			     proj.getCenter().getLongitude(),
			     proj.getScale(), 
			     proj.getHeight(), proj.getWidth(),
			     boundingPolys, args, link);

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
	    Debug.message("link","LinkLayer: getGraphics(): no graphic response.");
	    return new LinkOMGraphicList();
	}
    }

    /** 
     * Looks at a properties object, and checks for the pre-defined
     * messaging attributes.  Then, the information delegator is
     * called to handle their display.
     *
     * @param prop LinkProperties containing messages. 
     */
    public void handleMessages(LinkProperties props) {
	String value = props.getProperty(LPC_INFO);
	if (value != null) fireRequestInfoLine(value);
	
	value = props.getProperty(LPC_URL);
	if (value != null) {
	    fireRequestURL(value);
	} else {
	    value = props.getProperty(LPC_HTML);
	    if (value != null) fireRequestBrowserContent(value);
	}
	value = props.getProperty(LPC_MESSAGE);
	if (value != null) fireRequestMessage(value);
    }

    //----------------------------------------------------------------------
    // MapMouseListener interface implementation
    //----------------------------------------------------------------------
    /** Return the MapMouseListener for the layer. */
    public synchronized MapMouseListener getMapMouseListener() {
	return this;
    }

    /** Return the strings identifying the Mouse Modes that the
     *  MapMouseListener wants to receive gestures from. */
    public String[] getMouseModeServiceList() {
	String[] services = {SelectMouseMode.modeID};
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
     * Given a graphic and the type of gesture caught, react to it
     * based on the properties object located in the Graphic.  The
     * default behavior here is that if the gesture is a MouseMoved,
     * select the graphic, and if there is an info line, show it.  If
     * the gesture is a MouseRelease, display the info line, and also
     * check the following, in this order: url and then html.  If
     * there is a message property, the message is sent in a pop-up
     * window.
     *
     * @param graphic the graphic to check out.
     * @param descriptor the type of gesture.
     * @param e mouse event, to get location.
     * @return true if the server still needs to be told - per descriptor bit 11.
     */
    protected boolean graphicGestureReaction(OMGraphic graphic, 
					     int descriptor, MouseEvent e) {
	LinkProperties props = (LinkProperties) graphic.getAppObject();

	// Mouse clicked
	boolean mc = LinkUtil.isMask(descriptor, MOUSE_CLICKED_MASK);
	// Mouse released
	boolean mr = LinkUtil.isMask(descriptor, MOUSE_RELEASED_MASK);
	// Mouse moved
	boolean mm = LinkUtil.isMask(descriptor, MOUSE_MOVED_MASK);
	// server inform
	boolean si = LinkUtil.isMask(getGestureDescriptor(), SERVER_NOTIFICATION_MASK);

	boolean ret = true;

	if (mr || mc) {
	    String url = props.getProperty(LPC_URL);
	    if (url != null) {
		if (Debug.debugging("link")) {
		    System.out.println("LinkLayer:graphicGestureReaction: displaying url: " + url);
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
	    
	    // Just reuse url instead of declaring another string object
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
		    System.out.println("LinkLayer:graphicGestureReaction: displaying info line: " + info);
		}
		fireRequestInfoLine(info);
		ret = si;
	    }
	}

	return ret;
    }

    /** 
     * Send the query, act on the response, and tell the caller if
     * the gesture was consumed.  The Link actually gets a copy of the
     * layer to handle communication with the InformationDelegator.
     * The GraphicUpdates are handled in this method - the graphics
     * list is modified.
     *
     * @param descriptor a masked integer telling the type of gesture.
     * @param e the MouseEvent.
     * @return true if the event was consumed. 
     */
    protected boolean handleGesture(int descriptor, MouseEvent e) {
	Debug.message("link", "LinkLayer: handleGesture:");
	// Set if we need to repaint
	boolean needRepaint = false;	

	try {
	    LinkOMGraphicList graphics = getGraphicList(); // Get old list

	    if (graphics == null) {
		// Nothing to search on - this condition occurs when
		// the layer is already busy getting new graphics as a
		// result of a changed projection.
		Debug.message("link", "LinkLayer: null graphics list.");
		return false;
	    }

	    if (e == null) {
		graphics.deselectAll();
		return false;
	    }

	    // Find out if a graphic is closeby...
	    int gesGraphicIndex = graphics.findIndexOfClosest(e.getX(), e.getY(), 
							      distanceLimit);

	    // We need to do this to deselect everything else too.
	    OMGraphic gesGraphic = graphics.selectClosest(e.getX(), e.getY(), 
							  distanceLimit);

	    String id = null;

	    // If there was a graphic, set the mask to indicate that,
	    // and keep track of the graphic and the list index of the
	    // graphic for the response.  If a graphic modify command
	    // comes back without an ID, then we'll assume the server
	    // was refering to this graphic.
	    if (gesGraphic != null) {

		boolean tellServer = graphicGestureReaction(gesGraphic, descriptor, e);
		
		if (!tellServer) {
		    repaint();
		    return true;
		}

		needRepaint = true;
		descriptor = LinkUtil.setMask(descriptor, GRAPHIC_ID_MASK);
		id = ((LinkProperties) gesGraphic.getAppObject()).getProperty(LPC_GRAPHICID);
	    } else {
		// clear out info line
		fireRequestInfoLine("");
	    }

	    // server inform
	    if (!LinkUtil.isMask(getGestureDescriptor(), SERVER_NOTIFICATION_MASK)) {
		return false;
	    }

	    // Get the lat/lon point of the event
	    LatLonPoint llpoint = getProjection().inverse(e.getX(), e.getY());

	    LinkActionList lal;
	    LinkActionRequest lar;

	    ClientLink l = linkManager.getLink(false);
	    
	    // We'll check this here because we don't want to wait if
	    // it is not available - it could be used for another
	    // graphics or gui fetch.
	    if (l == null) {
		Debug.message("link", "LinkLayer: unable to get link in handleGesture().");
		return false;
	    }

	    // Using the link - carefully prevent others from using it too!
	    synchronized (l) {
		if (id != null) {
		    args.setProperty(LPC_GRAPHICID, id);
		}

		// Send the query
		LinkActionRequest.write(descriptor, e, llpoint.getLatitude(), 
					llpoint.getLongitude(), args, l);
		// Read the response
		l.readAndParse(getProjection(), currentGenerator, this);

		lal = l.getActionList();
		lar = l.getActionRequest();

		if (id != null) {
		    args.remove(LPC_GRAPHICID);
		}

	    }

	    linkManager.finLink();
	    
	    if (lar != null) {
		setGestureDescriptor(lar.getDescriptor());
	    }
	    
	    // If nothing else was returned concerning the gesture query
	    if (lal == null) {
		return false;
	    }

	    handleMessages(lal.getProperties());

	    // The only thing we need to do is handle any gesture
	    // changes...
	    java.util.Vector updates = lal.getGraphicUpdates();
	    java.util.Enumeration items = updates.elements();

	    OMGraphic reactionGraphic = gesGraphic;
	    int reactionGraphicIndex = gesGraphicIndex;

	    while (items.hasMoreElements()) {
		needRepaint = true; // We do!
		GraphicUpdate gu = (GraphicUpdate) items.nextElement();

		// Take care of this first.....
		if (LinkUtil.isMask(gu.action, MODIFY_DESELECTALL_GRAPHIC_MASK)) {
		    Debug.message("link","LinkLayer: deselecting all graphics");
		    graphics.deselectAll();
		}

		// Find the graphic that we are talking about - if the
		// ID is not "none", or if the id doesn't match the
		// gesGraphic LinkGraphicID, then look for the new
		// graphic.  Otherwise, assume that the gesGraphic is
		// the one that the action refers to.
		if (!(gu.id == null) && !gu.id.equals(id)) {
		    reactionGraphicIndex = graphics.getOMGraphicIndexWithId(gu.id);
		    if (reactionGraphicIndex == Link.UNKNOWN) {
			// Must be an addition/new graphic
			if (LinkUtil.isMask(gu.action, 
					    UPDATE_ADD_GRAPHIC_MASK)) {
			    // If gu.graphic is null, this will throw an exception
			    Debug.message("link","LinkLayer: adding graphic");
			    graphics.add(gu.graphic);
			    reactionGraphic = gu.graphic;
			} else {
			    System.err.println("LinkLayer: Gesture Response on an unknown graphic.");
			}
		    } else {
			reactionGraphic = graphics.getOMGraphicAt(reactionGraphicIndex);
		    }
		}

		// Now, perform the appropriate action on the graphic...

		// Delete a graphic...  If you do this, nothing else
		// gets done on the graphic...
		if (LinkUtil.isMask(gu.action, MODIFY_DELETE_GRAPHIC_MASK)) {
		    Debug.message("link","LinkLayer: deleting graphic");
		    graphics.removeOMGraphicAt(reactionGraphicIndex);
		} else {
		    
		    // For properties updating, or graphic replacement
		    if (LinkUtil.isMask(gu.action, UPDATE_GRAPHIC_MASK)) {
			Debug.message("link","LinkLayer: updating graphic");
			graphics.setOMGraphicAt(reactionGraphic, reactionGraphicIndex);
		    }
		    
		    // For graphic selection and deselection
		    if (LinkUtil.isMask(gu.action, MODIFY_SELECT_GRAPHIC_MASK)) {
			Debug.message("link","LinkLayer: selecting graphic");
			reactionGraphic.select();
		    } else if (LinkUtil.isMask(gu.action, MODIFY_DESELECT_GRAPHIC_MASK)) {
			Debug.message("link","LinkLayer: deselecting graphic");
			reactionGraphic.deselect();
		    }

		    // Now, raising or lowering the graphic...
		    if (LinkUtil.isMask(gu.action, MODIFY_RAISE_GRAPHIC_MASK)) {
			Debug.message("link","LinkLayer: raising graphic");
			graphics.moveIndexedToTop(reactionGraphicIndex);
		    } else if (LinkUtil.isMask(gu.action, MODIFY_LOWER_GRAPHIC_MASK)) {
			Debug.message("link","LinkLayer: lowering graphic");
			graphics.moveIndexedToBottom(reactionGraphicIndex);
		    }

		}  // else if not deleting it...
	    } // while

	    if (needRepaint) {
		repaint();
	    }
	    return lal.consumedGesture();

	} catch (IOException ioe) {
	    System.err.println("LinkLayer: IOException contacting server during gesture handling!");
	    System.err.println(ioe);
	    
	    linkManager.resetLink();

	    return false;
	}
    }

    // DrawingToolRequestor method
    public void drawingComplete(OMGraphic omg, OMAction action) {
	////////////// send the new graphic, along with instructions
	//on what to do with it, to the server.
	String id = null; // unknown

	Object obj = omg.getAppObject();
	LinkProperties lp = null;
	if (obj != null && obj instanceof LinkProperties) {
	    lp = (LinkProperties)obj;
	    id = lp.getProperty(LPC_GRAPHICID);
	}

	if (id == null) {
	    // Doesn't look like it was a modified graphic already
	    // recieved from the server, so we should tell the server
	    // to add it to its list.
	    action.setMask(OMAction.ADD_GRAPHIC_MASK);
	}

	try {
	    // We do want the link object here... If another thread is
	    // using the link, wait.
	    ClientLink l = linkManager.getLink(true);

	    if (l == null) {
		System.err.println("LinkLayer.drawingComplete: unable to get link.");
		return;
	    }

	    synchronized(l) {
		LinkActionList lal = new LinkActionList(l, new LinkProperties());

		if (action.isMask(OMAction.ADD_GRAPHIC_MASK) || 
		    action.isMask(OMAction.UPDATE_GRAPHIC_MASK)) {
		    lal.writeGraphicGestureHeader(action.getValue());
		    LinkGraphic.write(omg, l);
		} else {
		    // This shouldn't ever get called with a null lp
		    // properties object.  If the object is new or
		    // doesn't have an ID, the upper paragraph will
		    // get called.
		    lal.modifyGraphic(action.getValue(), lp);
		}
		lal.end(Link.END_TOTAL);
	    }
	    l.readAndParse(getProjection(), currentGenerator);
	    linkManager.finLink();

	} catch (UnknownHostException uhe) {
	    Debug.error("LinkLayer: unknown host!");
	    return;
	} catch (java.io.IOException ioe) {
	    Debug.error("LinkLayer: Communication error between " + getName() + 
			" layer\nand Link Server: Host: " + host + 
			", Port: " + port + 
			"LinkLayer: IOException contacting server!\n" +
			ioe.getMessage());


	    linkManager.resetLink();

	    if (!quiet) {
		fireRequestMessage("Communication error between " + getName() + 
				   " layer\nand Link Server: Host: " + host + 
				   ", Port: " + port);
	    }

	    return;
	}
    }

    /**
     * Set the search distance limit pixel distance for graphics
     * searches.  When the graphics list is checked for a graphic that
     * is closest to a mouse event, this is the pixel limit within
     * hits are considered.
     * @param limit the pixel limit to consider something "closest".
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

}
