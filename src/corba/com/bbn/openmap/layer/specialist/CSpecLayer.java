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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/CSpecLayer.java,v $
// $RCSfile: CSpecLayer.java,v $
// $Revision: 1.2 $
// $Date: 2003/03/24 16:21:59 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.specialist;


/*  Java Core  */
import java.applet.Applet;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/*  CORBA  */
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ShortHolder;
import org.omg.CORBA.StringHolder;
import org.omg.CosNaming.*;

import com.bbn.openmap.CSpecialist.BitmapPackage.*;
import com.bbn.openmap.CSpecialist.CProjection;
import com.bbn.openmap.CSpecialist.CirclePackage.*;
import com.bbn.openmap.CSpecialist.GraphicChange;
import com.bbn.openmap.CSpecialist.GraphicPackage.*;
import com.bbn.openmap.CSpecialist.LLPoint;
import com.bbn.openmap.CSpecialist.LinePackage.*;
import com.bbn.openmap.CSpecialist.PolyPackage.*;
import com.bbn.openmap.CSpecialist.RasterPackage.*;
import com.bbn.openmap.CSpecialist.RectanglePackage.*;
import com.bbn.openmap.CSpecialist.Server;
import com.bbn.openmap.CSpecialist.ServerHelper;
import com.bbn.openmap.CSpecialist.TextPackage.*;
import com.bbn.openmap.CSpecialist.U2525SymbolPackage.*;
import com.bbn.openmap.CSpecialist.UGraphic;
import com.bbn.openmap.CSpecialist.UWidget;
import com.bbn.openmap.CSpecialist.UnitSymbolPackage.*;

/*  OpenMap  */
import com.bbn.openmap.Environment;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;

/**
 * CSpecLayer is a Layer which communicates to CORBA Specialists.
 * <P>
 * Properties:
 * <P><pre>
 * # If you have an ior for the server:
 * cspeclayermarker.ior= URL to ior
 * # If you are using the Naming Service:
 * cspeclayermarker.name= SERVER NAME
 * # Static Arguments for the server, to be sent on every map request:
 * cspeclayermarker.staticArgs= space separated arguments
 * # If the network setup allows the server to contact the client (no firewall)
 * cspeclayermarker.allowServerUpdates=true/false (false is default)
 * </pre>
 */
public class CSpecLayer extends Layer
    implements ProjectionListener, MapMouseListener {

    private final static String[] debugTokens = {
	"debug.cspec"
    };

    /** The property specifying the IOR URL. */
    public static final String iorUrlProperty = ".ior";
    public static final String namingProperty = ".name";

    /** The property specifying the static arguments. */
    public static final String staticArgsProperty = ".staticArgs";

    /**
     * The property to use for specifying whether the GraphicChange
     * object should be sent to the server.  The server can use the
     * GraphicChange object to contact the client to notify it that
     * updates are available.  This should only be true if the network
     * setup allows it to be.  Running the client behind a firewall,
     * taking with the server through a Gatekeeper, will not allow the
     * GraphicChange object to be set.  You get a BOA instantiation
     * error.
     */
    public static final String serverUpdateProperty = ".allowServerUpdates";

    protected URL iorURL = null;
    /** Arguments passed in from the OverlayTable/properties file. */
    protected String staticArgs = null;
    /** 
     * Arguments modified by the Layer, or set by the Bean, at
     * runtime.  Historical, should use Properties instead.
     */
    protected String dynamicArgs = null;
    protected String clientID = Environment.generateUniqueString();

    protected UWidget[] widgets = null;
    protected transient CSpecPalette gui = null;
    protected JGraphicList jGraphics;
    protected transient Server specialist = null;
    private transient ORB orb;
    protected ShortHolder selectDist = new ShortHolder();
    protected BooleanHolder wantAreaEvents = new BooleanHolder();
    protected GraphicChange notifyOnChange = null;
    protected MapGesture mapGesture = new MapGesture();

    /**
     * Used for the MapMouseListener interface, to track whether to
     * listen to mouse events, or not.
     */
    protected boolean acceptingEvents = false;
    /** 
     * Used to track if a info line was sent, so that a clearing
     * message can be sent when it is no longer relevant. 
     */
    protected boolean sentInfoLine = false;

    // all the dirty bits
    protected int dirtybits = 0;
    public final transient static int PALETTE_DIRTY = 0x1;
    public final transient static int PREMATURE_FINISH = 0x4;
    public final transient static int EXCEPTION = 0x8;
    public final transient static int DIRTYMASK = 0xFFFFFFFF;

    // new slots
    Projection projection;
    CSpecWorker currentWorker;
    protected boolean showDialogs = Environment.getBoolean("com.bbn.openmap.ShowLayerMessages");

    /**
     * Set when the projection has changed while a swing worker is
     * gathering graphics, and we want him to stop early. 
     */
    protected boolean cancelled = false;

    protected String naming = null;

    /**
     * Customized thread worker for the CSpecLayer class. This thread
     * will do the work collecting the graphics for the CSpecLayer.
     */
    public class CSpecWorker extends SwingWorker {
	public CSpecWorker() {
	    super();
	}

	/** 
	 * Compute the value to be returned by the <code>get</code> method. 
	 */
	public Object construct() {
	    Object obj = null;
	    if (Debug.debugging("cspec")) {
		System.out.println(getName()+"|CSpecWorker.construct()");
	    }
	    try {
		fireStatusUpdate(LayerStatusEvent.START_WORKING);
		obj = prepare();
	    } catch (OutOfMemoryError e) {
		specialist = null;
		jGraphics = null;
		widgets = null;
		gui = null;
		System.err.println(getName() + "|CSpecWorker.construct(): " +e);
		if (showDialogs) {
		    postMemoryErrorMsg("OutOfMemory while getting graphics from\n" +
				       getName() + " specialist.");
		}
	    } catch (Throwable t) {
		specialist = null;
		jGraphics = null;
		widgets = null;
		gui = null;
		System.err.println(getName() + "|CSpecWorker.construct(): " + t);
		t.printStackTrace();
		if (showDialogs) {
		    postException("Exception while getting graphics from\n" +
				  getName() + " specialist:\n" + t.getClass().getName());
		}
	    }
	    return obj;
	}

	/**
	 * Called on the event dispatching thread (not on the worker thread)
	 * after the <code>construct</code> method has returned.
	 */
	public void finished() {
	    workerComplete(this);
	    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	}
    }

    /**
     * Default constructor, that sets the MapMouseListener for this
     * layer to itself.
     */
    public CSpecLayer() {
	handleGraphicChangeRequests(false);

	// Uncomment these to force debugging if OpenMap is wrapped by
	// something else that is not initializing the Debug class.
//  	Debug.init(new Properties());
//  	Debug.put("cspec");
    }

    /**
     * Sets whether the notifyOnChange object will actually be set to
     * anything.  This object can be used to tell the CSpecLayer to go
     * to the specialist with a getRectangle. The Layer handles the
     * creation of the object if this is set to true.  If you are
     * working through a firewall, this might not be allowed,
     * especially if the client is behind the firewall.
     *
     * @param setting if the object should be created or not.  
     */
    public void handleGraphicChangeRequests(boolean setting) {
	if (setting) {
	    notifyOnChange = new JGraphicChange(this);
	} else {
	    notifyOnChange = null;
	}
    }

    /**
     *
     */
    public void finalize() {
	if (Debug.debugging("cspec")) {
	    System.out.println(getName()+"|CSpecLayer.finalize(): calling shutdown");
	}
        try {
            if (specialist != null)
                specialist.signoff(clientID);
            specialist = null;
        } catch (org.omg.CORBA.SystemException e) {
            System.err.println(getName()+"|CSpecLayer.finalize(): " + e);
        } catch (Throwable t) {
            System.err.println(getName()+"|CSpecLayer.finalize(): " + t);
	}
    }

    /**
     *
     */
    public void setProperties(String prefix, java.util.Properties props) {

	super.setProperties(prefix, props);

	String url = props.getProperty(prefix + iorUrlProperty);
	if (url != null) {
	    try {
		setIorUrl(new URL(url));
	    } catch (MalformedURLException e) {
		throw new IllegalArgumentException("\"" + url + "\""


						   + " is malformed.");
	    }
	}

	// Get the naming context to get
	naming = props.getProperty(prefix + namingProperty);

	String staticArgValue = props.getProperty(prefix + staticArgsProperty);
	setStaticArgs(staticArgValue);

	handleGraphicChangeRequests(com.bbn.openmap.layer.util.LayerUtils.booleanFromProperties(props, prefix + serverUpdateProperty, false));
    }

    /**
     * Gets the argv for the layer from the pseudo-overlay-table.
     * Expecting <URL> &rest args.
     */
    public void setArgs(String argv[]) {
	int argc = argv.length;

	if (argc == 0) {
	    // Do nothing.
	    return;
	}

	String url = argv[0];
	StringBuffer argBuf = new StringBuffer();

	if (argc > 1) {
	    // More arguments, append them into one string and
	    // pass it off to setArgs.
	    argBuf.append(argv[1]);
	    for (int i = 2; i < argc; i++) {
		argBuf.append(" ").append(argv[i]);
	    }
	}
	//dbg 	System.out.println("----------------------------------------------");
	//dbg 	System.out.println("CSpecLayer " + getName() + ":");
	//dbg 	System.out.println("\tURL: " + url);
	//dbg 	System.out.println("\targs: " + argBuf);


	try {
	    setIorUrl(new URL(url));
	} catch (MalformedURLException e) {
	    throw new IllegalArgumentException("\"" + url + "\""
					       + " is not a well formed URL");
	}

	setStaticArgs(argBuf.toString());
    }

    /**
     * ProjectionListener method.  This method checks to see if there
     * is a current CSpecWorker.  If there isn't, it starts one.  If
     * there is, it sets the layer cancelled flag.
     *
     * @param e projection event.
     */
    public void projectionChanged(ProjectionEvent e) {
	if (Debug.debugging("cspec"))
	    System.out.println(getName()+"|CSpecLayer: projectionChanged()");

	Projection newP = e.getProjection();
	if (projection != null) {
	    if (projection.equals(newP) && ((dirtybits&DIRTYMASK) == 0)) {
	        repaint();
		return;
	    }
	}
	else {
	    projection = newP.makeClone();
	}

 	jGraphics = null;
	synchronized (this) {
	    // clone projection if different
	    if (!projection.equals(newP))
		projection = (Projection)newP.makeClone();
	    requestNewObjects();
	}
    }

    /**
     *
     */
    protected void requestNewObjects() {
	// If there isn't a worker thread working on collecting
	// objects from the specialist already, create a thread that
	// will do the real work. If there is a thread working on
	// collecting graphics from the specialist, then tell the
	// layer that the worker should be cancelled.  The worker
	// checks this when it is safe to stop early.
	if (currentWorker == null) {
	    currentWorker = new CSpecWorker();
	    currentWorker.execute();
	}
	else {
	    setCancelled(true);
	}
    }

    /**
     * The CSpecWorker calls this method on the layer when it is
     * done working.  A new worker is created if the cancelled flag in
     * the layer is set.
     *
     * @param worker the worker that has the graphics.
     */
    protected synchronized void workerComplete(CSpecWorker worker) {
	if (!isCancelled()) {
	    currentWorker = null;
	    jGraphics = ((JGraphicList)worker.get());
	    repaint();
	} else {
	    dirtybits |= PREMATURE_FINISH;
	    setCancelled(false);
	    currentWorker = new CSpecWorker();
	    currentWorker.execute();
	}
    }

    /**
     * get the specialist proxy.
     *
     * @return Server specialist server or null if error.
     */
    public Server getSpecialist() {
	if (specialist == null) {
	    initSpecialist();
	}
	return specialist;
    }

    /**
     * Bind to the specialist server.
     */
    private void initSpecialist() {
	String ior = null;
	if (iorURL != null) {
	    try {
		URLConnection urlConnection = iorURL.openConnection();
		// 	    urlConnection.setDefaultUseCaches(false);
		// 	    urlConnection.setUseCaches(false);
		// 	    urlConnection.connect();
		
		InputStream is = urlConnection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		ior = reader.readLine();
		reader.close();
	    } catch (java.io.IOException e) {
		System.err.println(getName() + "|CSpecLayer.initSpecialist(): " +
				   "IOException reading IOR from \"" +
				   iorURL + "\"");
		return;
	    }
	}

	try {
 	    orb = CORBAManager.getORB();

	    if (ior != null) {
		//    // HACK seem to need this for vbj33 applet...
		//  	    ((com.visigenic.vbroker.orb.ORB)orb).proxy(false);
		//  	    System.out.println("ORB PROXY="+((com.visigenic.vbroker.orb.ORB)orb).proxy());
		org.omg.CORBA.Object object = orb.string_to_object(ior); 
		specialist = ServerHelper.narrow(object);
	    } 

	    if (specialist == null && naming != null) {
		// Get the root context of the name service.
		//// System.out.print( "Binding to the server objects... " );
		org.omg.CORBA.Object obj = null;
		try {
		    obj = orb.resolve_initial_references( "NameService" );
		} catch(Exception e) {
		    System.err.println( "CSpecLayer: Error getting root naming context: " );
		    e.printStackTrace();
		}
		NamingContext rootContext = NamingContextHelper.narrow( obj );

		if (Debug.debugging("cspec")) {
		    if (rootContext == null) {
			System.out.println("null root context");
		    }
		}
     
		// Resolve the specialist
		String temp = naming;
		Vector components = new Vector();
		int numcomponents = 0;
		String temporaryTemp = null;

		int tindex = temp.indexOf("/");
		while (tindex != -1) {
		    numcomponents++;
		    temporaryTemp=temp.substring(0,tindex);
		    if (Debug.debugging("cspec")) {
			System.out.println("Adding Name component: "+
					   temporaryTemp);
		    }
		    components.addElement(temporaryTemp);
		    temp=temp.substring(tindex+1);
		    tindex = temp.indexOf("/");
		}
		if (Debug.debugging("cspec")) {
		    System.out.println("Adding final Name component: "+ temp);
		}
		components.addElement(temp);
	
		NameComponent[] specialistName = new NameComponent[components.size()];
		for (int i=0; i<components.size(); i++) {
		    specialistName[i] = new NameComponent((String)(components.elementAt(i)), "");
		}
     	
		org.omg.CORBA.Object specialistObject = null;
		try {
		    if (rootContext != null) {
			specialistObject = rootContext.resolve( specialistName );
		    } else {
			System.err.println("CSpecLayer: No Root Context for naming.");
		    }
		} catch(Exception e) {
		    System.err.println( "CEpecLayer: Error resolving the specialist: " );
		    e.printStackTrace();
		}
       
		if (specialistObject == null) {
		    if (Debug.debugging("cspec")) {
			System.out.println("null specObj");
		    }
		}
		else {
		    // // 		    System.out.println("objtostring:");
		    System.out.println(orb.object_to_string(specialistObject));
		}
		specialist = ServerHelper.narrow( specialistObject );
		if (Debug.debugging("cspec")) {
		    System.out.println("Have a specialist:" );
		    System.out.println("*** Specialist Server: is a " + 
				       specialist.getClass().getName() + "\n" + 
				       specialist);
		}
	    } //ior/name service if/else
   
	} catch (org.omg.CORBA.SystemException e) {
	    System.err.println(getName()+"|CSpecLayer.initSpecialist(): " + e);
	    specialist = null;
	    if (showDialogs) {
		postCORBAErrorMsg("CORBA Exception while initializing\n" +
				  getName() + " specialist:\n" + e.getClass().getName());
	    }
	} catch (Throwable t) {
	    System.err.println(getName()+"|CSpecLayer.initSpecialist(): " + t);
	    specialist = null;
	    if (showDialogs) {
		postException("Exception while initializing\n" +
			      getName() + " specialist:\n" + t.getClass().getName());
	    }
	}
	if (specialist == null) {
	    if (Debug.debugging("cspec")) {
		System.err.println("CSpecLayer.initSpecialist: null specialist!\n  IOR=" + ior);
	    }
	}
    }

    /**
     */
    public void setSpecialist(Server aSpecialist) {
	specialist = aSpecialist;
    }

    /**
     */
    public void setOrb(ORB anOrb) {
	orb = anOrb;
    }

    /**
     * Interface Layer method to get the static args, which are
     * usually set via the OverlayTable. 
     */
    public String getStaticArgs() {
	return staticArgs;
    }

    /**
     * Interface Layer method to get the dynamic args.
     * @return String args
     * @deprecated use setProperties
     */
    public String getArgs() {
        return dynamicArgs;
    }

    /**
     * Interface Layer method to set the dynamic args.
     * @param args String
     * @deprecated use setProperties
     */
    public void setArgs(String args) {
	dynamicArgs = args;
    }

    /**
     * Interface Layer method to set the static args, which are
     * usually set via the OverlayTable. 
     */
    public void setStaticArgs(String args) {
 	staticArgs = args;
    }

    public URL getIorUrl() {
	return iorURL;
    }
    public void setIorUrl(URL url) {
	iorURL = url;
    }

    /**
     * Used to set the cancelled flag in the layer.
     *
     * @param set boolean
     */
    public synchronized void setCancelled(boolean set) {
	cancelled = set;
    }

    /**
     * Check to see if the cancelled flag has been set.
     * <p>
     * The swing worker checks this once in a while to see if the
     * projection has changed since it started working.  If this is
     * set to true, the swing worker quits when it is safe.
     *
     * @return boolean 
     */
    public synchronized boolean isCancelled() {
	return cancelled;
    }

    /**
     * Perform the getRectangle() call on the specialist.
     *
     * @param p Projection
     * @return UGraphic[] graphic list or null if error
     */
    protected UGraphic[] getSpecGraphics(Projection p) {
	CProjection cproj;
	LLPoint ll1, ll2;
	StringHolder dynamicArgsHolder;
	UGraphic[] graphics;
	Server spec = getSpecialist();
	if (Debug.debugging("cspec"))
	    System.out.println(getName() +
			       "|CSpecLayer.getSpecGraphics()");

	cproj = new CProjection ((short)(p.getProjectionType()),
				 new LLPoint(p.getCenter().getLatitude(),
					     p.getCenter().getLongitude()),
				 (short)p.getHeight(),
				 (short)p.getWidth(),
				 (int)p.getScale());

	// lat-lon "box", (depends on the projection)
	LatLonPoint ul = p.getUpperLeft();
	LatLonPoint lr = p.getLowerRight();

	ll1 = new LLPoint(ul.getLatitude(), ul.getLongitude());
	ll2 = new LLPoint(lr.getLatitude(), lr.getLongitude());

	// check for cancellation
	if (isCancelled()) {
	    dirtybits |= PREMATURE_FINISH;
	    if (Debug.debugging("cspec"))
		System.out.println(getName() + 
				   "|CSpecLayer.getSpecGraphics(): aborted.");
	    return null;
	}
	// check for null specialist
	if (spec == null) {
	    if (Debug.debugging("cspec")) {
		System.err.println(getName() + 
				   "|CSpecLayer.getSpecGraphics(): null specialist!");
	    }
	    return null;
	}

	try {
	    // Keep the gestures up-to-date
	    mapGesture.setProjection(p);

	    // Static Args can't go out null....
	    String staticArguments = getStaticArgs();
	    if (staticArguments == null) {
		staticArguments = "";
		setStaticArgs(staticArguments);
	    }

	    // neither can dynamic args
	    // Layer.getArgs() was deprecated and removed
 	    dynamicArgsHolder = new StringHolder(getArgs());
	    if (dynamicArgsHolder.value == null) {
		dynamicArgsHolder.value = "";
	    }

	    // call getRectangle();
	    if (Debug.debugging("cspec")) {
		System.out.println(getName() +
				   "|CSpecLayer.getSpecGraphics():" +
				   " calling getRectangle with projection: " + p +
				   " ul=" + ul + " lr=" + lr +
				   " staticArgs=\"" + staticArguments + "\"" +
				   " dynamicArgs=\"" + dynamicArgsHolder.value + "\"" +
				   " clientID=" + clientID); 
	    }
	    long start = System.currentTimeMillis();

//  	    System.out.println("*** Specialist Server: is a " + spec.getClass().getName() + "\n" + spec);

	    graphics = spec.getRectangle(cproj,
					 ll1, ll2,
					 staticArguments,
					 dynamicArgsHolder,
					 selectDist,
					 wantAreaEvents,
					 notifyOnChange,
					 clientID);
	    long stop = System.currentTimeMillis();

	    if (Debug.debugging("cspec")) {
		System.out.println(getName()+"|CSpecLayer.getSpecGraphics(): got " +
				   graphics.length + " graphics in " + ((stop-start)/1000d) +
				   " seconds.");
	    }
	} catch (org.omg.CORBA.SystemException e) {
	    dirtybits |= EXCEPTION;
	    // don't freak out if we were only interrupted...
	    if (e.toString().indexOf("InterruptedIOException") != -1) {
		System.err.println(getName()+"|CSpecLayer.getSpecGraphics(): " +
				   "getRectangle() call interrupted!");
	    } else {
		System.err.println(getName()+"|CSpecLayer.getSpecGraphics(): " +
				   "Caught CORBA exception: " + e);
		System.err.println(getName()+"|CSpecLayer.getSpecGraphics(): " +
				   "Exception class: " + e.getClass().getName());
		e.printStackTrace();
	    }

	    specialist = null;// dontcha just love CORBA? reinit later
	    graphics = null;
	    jGraphics = null;
	    widgets = null;
	    gui = null;
	    if (showDialogs)
		postCORBAErrorMsg(
		    "CORBA Exception while getting graphics from\n" +
		    getName() + " specialist:\n" + e.getClass().getName());
	}
	return graphics;
    }

    /**
     * Prepares the graphics for the layer.
     * <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the requestNewObjects() call will set the cancel
     * bit on the SwingWorker.  The worker will get restarted after it
     * finishes doing its cleanup.
     *
     * @return a JGraphicList from the server.
     */
    public JGraphicList prepare() {
	if (isCancelled()) {
	    dirtybits |= PREMATURE_FINISH;
	    if (Debug.debugging("basic")) {
		System.out.println(getName() + "|CSpecLayer.prepare(): aborted.");
	    }
	    return null;
	}

	if (Debug.debugging("basic")) {
	    System.out.println(getName()+"|CSpecLayer.prepare(): doing it");
	}
	dirtybits = 0;//reset the dirty bits

	// Now we're going to shut off event processing.  The only
	// thing that turns them on again is finishing successfully.
 	setAcceptingEvents(false);

	// get the graphics from the specialist
	UGraphic[] specGraphics = getSpecGraphics(projection);
	if (isCancelled()) {
	    dirtybits |= PREMATURE_FINISH;
	    if (Debug.debugging("basic"))
		System.out.println(getName() + "|CSpecLayer.prepare(): " +
				   "aborted during/after getRectangle().");
	    return null;
	}

	if (specGraphics == null) {
	    return null;
	}

	// process the graphics
	long start = System.currentTimeMillis();
	JGraphicList graphics = createGraphicsList(specGraphics, projection);
	long stop = System.currentTimeMillis();
	if (Debug.debugging("cspec")) {
	    System.out.println(getName()+ "|CSpecLayer.prepare(): generated " + 
			       specGraphics.length + " graphics in " + 
			       ((stop-start)/1000d) + " seconds.");
	}

	if (isCancelled()) {
	    dirtybits |= PREMATURE_FINISH;
	    if (Debug.debugging("basic")) {
		System.out.println(getName() + "|CSpecLayer.prepare(): " +
				   "aborted while generating graphics.");
	    }
	    return null;
	}

	// safe quit
	if (Debug.debugging("basic")) {
	    System.out.println(getName()+"|CSpecLayer.prepare(): finished preparing " + 
			       graphics.size() + " graphics");
	}
 	setAcceptingEvents(true);
	return graphics;
    }

    /**
     * Create an JGraphicList based on UGraphics and a Projection.
     * <p>
     * This is public static to enable out-of-package delegation.
     * <p>
     * @param uGraphics UGraphic[]
     * @param proj Projection
     * @return JGraphicList
     */
    public static JGraphicList createGraphicsList(UGraphic[] uGraphics, 
						  Projection proj) {

	int nGraphics = uGraphics.length;
	JGraphicList graphics = new JGraphicList(nGraphics);
	graphics.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);

	// generate a JGraphic for each CSpecialist graphic and store it
	for (int i=0; i<nGraphics; i++)	{
	    switch (uGraphics[i].discriminator().value()) {
	    case GraphicType._GT_Poly:
		JPoly jpoly = new JPoly(uGraphics[i].epoly());
		jpoly.generate(proj);
  	        graphics.addOMGraphic(jpoly);
		break;
	    case GraphicType._GT_Raster:
		JRaster jraster = new JRaster(uGraphics[i].eras());
		jraster.generate(proj);
  	        graphics.addOMGraphic(jraster);
		break;
	    case GraphicType._GT_Bitmap:
		JBitmap jbitmap = new JBitmap(uGraphics[i].ebit());
		jbitmap.generate(proj);
  	        graphics.addOMGraphic(jbitmap);
		break;
	    case GraphicType._GT_Text:
		JText jtext = new JText(uGraphics[i].etext());
		jtext.generate(proj);
  	        graphics.addOMGraphic(jtext);
		break;
	    case GraphicType._GT_Line:
		JLine jline = new JLine(uGraphics[i].eline());
		jline.generate(proj);
  	        graphics.addOMGraphic(jline);
		break;
	    case GraphicType._GT_UnitSymbol:
		JUnit junit = new JUnit(uGraphics[i].eunit());
		junit.generate(proj);
  	        graphics.addOMGraphic(junit);
		break;
	    case GraphicType._GT_2525Symbol:
		J2525 j2525 = new J2525(uGraphics[i].e2525());
		j2525.generate(proj);
  	        graphics.addOMGraphic(j2525);
		break;
	    case GraphicType._GT_Rectangle:
		JRect jrect = new JRect(uGraphics[i].erect());
		jrect.generate(proj);
  	        graphics.addOMGraphic(jrect);
		break;
	    case GraphicType._GT_Circle:
		JCircle jcircle = new JCircle(uGraphics[i].ecirc());
		jcircle.generate(proj);
  	        graphics.addOMGraphic(jcircle);
		break;
	    case GraphicType._GT_NewGraphic:
	    case GraphicType._GT_ReorderGraphic:
	    default:
		System.err.println("JGraphic.generateGraphics: " +
				   "ignoring invalid type");
		break;
	    }
	}
	return graphics;
    }

    /**
     * Paints the layer, starting to paint the graphics at the
     * beginning of the list and finishing with those at the end
     * (opposite of thegesture search).
     *
     * @param g the Graphics context for painting
     */
    public void paint(java.awt.Graphics g) {
	if (Debug.debugging("cspec")) {
	    System.out.println(getName() + "|CSpecLayer.paint()");
	}

	if (projection != null) {
	    // get data again if palette was changed
	    if ((dirtybits&PALETTE_DIRTY) != 0) {
		dirtybits &= (~PALETTE_DIRTY);//palette not dirty
		synchronized (this) {
		    if (Debug.debugging("cspec")) {
			System.out.println(getName() +
					   "|CSpecLayer.paint(): getting data.");
		    }
		    requestNewObjects();
		}
	    }
	}

	if (jGraphics == null) {
	    return;
	}
	jGraphics.render(g);
    }

    /**
     * Indicates whether this layer has a GUI.
     *
     * @return true if it has a GUI, false if it does not
     */
    public boolean hasGUI() {
	return true;
    }

    /** 
     * Gets the palette associated with the layer.<p>
     *
     * @return Component or null
     */
    public Component getGUI() {
	if (specialist == null)
	    initSpecialist();
	if (specialist == null) {
	    if (Debug.debugging("cspec"))
		System.out.println(getName()+ "|CSpecLayer.getGUI(): initSpecialist() unsuccessful!");
	    return null;
	}

	try {
	    if (widgets == null) {
		org.omg.CORBA.StringHolder paletteDynamicArgs =
		    new org.omg.CORBA.StringHolder(getArgs());
	    
		if (paletteDynamicArgs.value == null) {
		    paletteDynamicArgs.value = "";
		}

		if (Debug.debugging("cspec")) {
		    System.out.println(getName()+"|CSpecLayer.getGUI(): calling getPaletteConfig(" + getStaticArgs() + "," + paletteDynamicArgs.value + "," + clientID + ")");
		}

		try {

		    widgets = specialist.getPaletteConfig(null/*widgetChange*/, 
							  getStaticArgs(),
							  paletteDynamicArgs,
							  clientID);

		} catch (org.omg.CORBA.SystemException e) {
		    System.err.println(getName() + "|CSpecLayer.getGUI(): " + e);
		    e.printStackTrace();
		    widgets = null;
		    gui = null;
		    specialist = null;
		    if (showDialogs) {
			postCORBAErrorMsg("CORBA Exception while getting palette from\n" +
					  getName() + " specialist:\n" + e.getClass().getName());
		    }
		}
		if (widgets != null) {
		    gui = new CSpecPalette(widgets, clientID, this);
		} else {
		    gui = null;
		}
	    }
	} catch (OutOfMemoryError e) {
	    widgets = null;
	    gui = null;
	    specialist = null;
	    jGraphics = null;
	    System.err.println(getName() + "|CSpecLayer.getGUI(): " + e);
	    if (showDialogs) {
		postMemoryErrorMsg("OutOfMemory while getting palette from\n" +
				   getName() + " specialist.");
	    }
	} catch (Throwable t) {
	    widgets = null;
	    gui = null;
	    specialist = null;
	    System.err.println(getName() + "|CSpecLayer.getGUI(): " + t);
	    t.printStackTrace();
	    if (showDialogs) {
		postException("Exception while getting palette from\n" +
			      getName() + " specialist:\n" + 
			      t.getClass().getName());
	    }
	}

	return gui;
    }

    /**
     * A palette button has changed (we should indeed prepare when we get the
     * call).
     *
     * @param paletteIsDirty true or false
     */
    protected void setPaletteIsDirty(boolean paletteIsDirty) {
	if (paletteIsDirty) {
	    dirtybits |= PALETTE_DIRTY;
	}
    }

    /**
     * Destroy the current palette stuff.
     */
    protected void forgetPalette() {
	widgets = null;
	gui = null;
    }

    /**
     * Used to set whether the MapMouseListener is listening for
     * events or ignoring them.
     *
     * @param listening true if the listener should process mouse
     * events.
     */
    public void setAcceptingEvents(boolean listening) {
	acceptingEvents = listening;
    }

    /**
     * Used to tell if the listener is accepting mouse events for
     * processing.
     *
     * @return true if the listener is processing mouse events.
     */    
    public boolean isAcceptingEvents() {
	return acceptingEvents;
    }

    // Mouse Listener events
    ////////////////////////

    /**
     * Returns the MapMouseListener object (this object) that handles
     * the mouse events.
     * @return MapMouseListener this
     */
    public MapMouseListener getMapMouseListener() {
	return this;
    }

    public String[] getMouseModeServiceList() {
	String[] ret = new String[1];
	ret[0] = new String(SelectMouseMode.modeID);
	return ret;
    }

    /**
     * Handle a mouse button being pressed.
     *
     * @param e MouseListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mousePressed(MouseEvent e) {
	if (acceptingEvents && specialist != null) {
	    return handleGesture(e, MapGesture.clickEvent, true);
	}
	return false;
    }
  
    /** 
     * Handle a mouse button being released.
     *
     * @param e MouseListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mouseReleased(MouseEvent e) {
	if (acceptingEvents && specialist != null) {
	    return handleGesture(e, MapGesture.clickEvent, false);
	}
	return false;
    }
   
    /** 
     * Handle a mouse button being clicked - pressed and released.
     *
     * @param e MouseListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mouseClicked(MouseEvent e) {
	if (acceptingEvents && specialist != null) {
	    return handleGesture(e, MapGesture.clickEvent, false);
	}
	return false;
    }

    /** 
     * Handle a mouse cursor entering a window or area.
     *
     * @param e MouseListener MouseEvent to handle.
     */
    public void mouseEntered(MouseEvent e) {
	if (acceptingEvents && specialist != null) {
	    handleGesture(e, MapGesture.motionEvent, false);
	}
    }
  
    /** 
     * Handle a mouse cursor leaving a window or area.
     *
     * @param e MouseListener MouseEvent to handle.
     */
    public void mouseExited(MouseEvent e) {
	if (acceptingEvents && specialist != null) {
	    handleGesture(e, MapGesture.motionEvent, false);
	}
    }
    
    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Handle a mouse button being pressed while the mouse cursor is
     * moving.
     *
     * @param e MouseMotionListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mouseDragged(MouseEvent e) {
	if (acceptingEvents && specialist != null) {
	    return handleGesture(e, MapGesture.motionEvent, true);
	}
	return false;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     *
     * @param e MouseListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mouseMoved(MouseEvent e) {
	if (acceptingEvents && specialist != null) {
	    return handleGesture(e, MapGesture.motionEvent, false);
	}
	return false;
    }

    /** 
     * Handle a mouse cursor moving without the button being pressed,
     * for events that have been used by something else.
     */
    public void mouseMoved() {
	if (acceptingEvents && specialist != null) {
	    handleGesture(null, MapGesture.motionEvent, false);
	}
    }

    /**
     * Relays user gestures to the specialist or to the mousable
     * objects of the CSpecLayer.  The function finds the closest
     * object and then its comp object all by itself.<p>
     *
     * @param evt MouseEvent
     * @param MouseDown true if the mouse button is down
     * @return true if gesture was consumed, false if not.
     */
    public boolean handleGesture(MouseEvent evt, int eventType, 
				 boolean MouseDown) {

	boolean got_info = false;
	boolean got_the_stuff = false;
	boolean updated_graphics = false;
	OMGraphic moused = null;
	JGraphicList jjGraphics = jGraphics;

	// Do this, so when there was a one-liner about a graphic (or
	// something) sent, and now there isn't a graphic associated
	// with the layer, to reset the message window to nothing, so
	// stale info just doesn't hang out.
	if (sentInfoLine) {
	    fireRequestInfoLine("");
	    sentInfoLine = false;
	}

	if (!isAcceptingEvents() || (jjGraphics == null)) {
	    return false;
	}

	// This will need to change, when we figure out who to make a
	// Cspecialist capabile of handling a null event, so signify a
	// reset...
	if (evt == null) {
	    if (Debug.debugging("cspec")) {
		System.out.println(getName()+"|CSpecLayer.handleGesture(): null evt!");
	    }
	    return false;//didn't consume gesture
	}

	try {
	    mapGesture.setMouseEvent(evt, eventType, MouseDown);
	    moused = jjGraphics.findClosest(evt.getX(), evt.getY(),
					    selectDist.value);
	    
	    com.bbn.openmap.CSpecialist.ActionUnion[] action = null;
	    
	    switch (mapGesture.getMode()) {
	    case (short) MapGesture.Raw:
		// send the gesture to the comp object or the specialist
		// if it wants area events.
		if (moused != null && ((JObjectHolder)moused).getObject().comp != null) {
		    action = ((JObjectHolder)moused).getObject().comp.
			sendGesture(JGraphic.constructGesture(mapGesture), clientID);
		} else if (specialist != null) {
		    action = wantAreaEvents.value
			? specialist.sendGesture(JGraphic.constructGesture(mapGesture), clientID)
			: null;
		    if (action == null) {
			if (Debug.debugging("cspec"))
			    System.out.println(getName()+"|CSpecLayer.handleGesture(): null action!");
			return false; //didn't consume gesture
		    }
		    if (action.length == 0) {
			return false; //didn't consume gesture
		    }
		}
		if (action == null) {
		    if (Debug.debugging("cspec")) {
			System.err.println(getName()+"|CSpecLayer.handleGesture(): null action!");
		    }
		    return false; //didn't consume gesture
		}
		break;
	    case (short) MapGesture.Cooked: 
	    default:
		System.err.println("CSpecLayer|"+getName()+
				   "|handleGesture() - cooked modes not supported");
		break;
	    }

	    // parse the action sequence, ignore duplicate action directives
	    mapGesture.actionType = new int[action.length];
	    for (int i = 0; i < action.length; i++) {
		switch (action[i].discriminator().value()) {
		    
		case MapGesture.NoAction:
		    break;
		case MapGesture.UpdateGraphics:
		    updated_graphics = true;
		    // now update the specified graphics
		    updateGraphics(action[i].ginfo());
		    break;
		case MapGesture.InfoText:
		    if (!got_info) {	// can only have one instance
			if (Debug.debugging("cspec")) {
			    System.out.println("CSpecLayer|"+getName()+
					       "|handleGesture(): Requesting Info Text " + 
					       action[i].itext());
			}
			fireRequestInfoLine(action[i].itext());
			sentInfoLine = true;
			got_info = true;
		    }
		    break;
		case MapGesture.PlainText:
		    if (!got_the_stuff) {
			if (Debug.debugging("cspec")) {
			    System.out.println("CSpecLayer|"+getName()+
					       "|handleGesture(): Requesting Plain Text " + 
					       action[i].ptext());
			}
			fireRequestBrowserContent(action[i].ptext());
			got_the_stuff = true;
		    }
		    break;
		case MapGesture.HTMLText:
		    if (!got_the_stuff) {
			if (Debug.debugging("cspec")) {
			    System.out.println("CSpecLayer|"+getName()+
					       "|handleGesture(): Requesting HTML Text " + 
					       action[i].htext());
			}
			fireRequestBrowserContent(action[i].htext());
			got_the_stuff = true;
		    }
		    break;
		case MapGesture.URL:
		    if (!got_the_stuff) {
			if (Debug.debugging("cspec")) {
			    System.out.println("CSpecLayer|"+getName()+
					       "|handleGesture(): Requesting URL " + 
					       action[i].url());
			}
			fireRequestURL(action[i].url());
			got_the_stuff = true;
		    }
		    break;
		case MapGesture.UpdatePalette:
		default:
		    System.err.println(
			"CSpecLayer|"+getName()+
			"|handleGesture(): invalid ActionSeq");
		    break;
		}
	    }
	} catch (org.omg.CORBA.SystemException e) {
            System.err.println(getName()+"|CSpecLayer.handleGesture(): " + e);
	    if (showDialogs) {
		postCORBAErrorMsg("CORBA Exception while gesturing on\n" +
				  getName() + " specialist:\n" + e.getClass().getName());
	    }
	    return false;
        } catch (OutOfMemoryError e) {
	    specialist = null;
	    jGraphics = null;
	    if (showDialogs) {
		postMemoryErrorMsg("OutOfMemory while gesturing on\n" +
				   getName() + " specialist.");
	    }
	    return false;
	} catch (Throwable t) {
	    if (showDialogs) {
		postException("Exception while gesturing on\n" +
			      getName() + " specialist:\n" + t.getClass().getName());
	    }
	    t.printStackTrace();
	    return false;
	}



	// TCM 5/6/98
	// 	if (updated_graphics) fireRequestPaint();
	if (updated_graphics) {
	    repaint();
	}
	return true;//consumed the gesture
    }

    /** 
     * Changes attributes of existing graphics, or adds new graphics,
     * or reorders graphics.<p>
     *
     * @param updateRec com.bbn.openmap.CSpecialist.UpdateRecord[]
     */
    protected void updateGraphics(com.bbn.openmap.CSpecialist.UpdateRecord[] updateRec) {

	com.bbn.openmap.CSpecialist.UpdateGraphic upgraphic = null;
	// parse updateRec (an array of UpdateRecord)
	for (int i = 0; i < updateRec.length; i++ ) {
	    String gID = updateRec[i].gID;	// get the graphic ID
	    
	    // parse the sequence of updates to perform on the
	    // graphic.  You need to do this because the types of
	    // changes that can be made to each object can be part of
	    // the specific object, like _GT_Bitmap (location, bits,
	    // height/width), or part of the _GT_Graphic
	    // (color/stipple changes), or _GT_ReorderGraphic, or
	    // whatever.
	    for (int j = 0; j < updateRec[i].objectUpdates.length; j++) {
		upgraphic = updateRec[i].objectUpdates[j];
		
		// determine the type of graphic update
		switch (upgraphic.discriminator().value()) {
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Graphic:
		    JObjectHolder graphic = (JObjectHolder) jGraphics.getOMGraphicWithId(gID);
		    if (graphic != null) {
			graphic.update(upgraphic.gf_update());
		        ((OMGraphic)graphic).regenerate(projection);
		    }
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Bitmap:
		    JBitmap bitmap = (JBitmap) jGraphics.getOMGraphicWithId(gID);
		    if (bitmap != null) {
			bitmap.update(upgraphic.bf_update());
			bitmap.regenerate(projection);
		    }
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Text:
		    JText text = (JText) jGraphics.getOMGraphicWithId(gID);
		    if (text != null) {
			text.update(upgraphic.tf_update());
			text.regenerate(projection);
		    }
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Poly:
		    JPoly poly = (JPoly)jGraphics.getOMGraphicWithId(gID);
		    if (poly != null) {
			poly.update(upgraphic.pf_update());
			poly.regenerate(projection);
		    }
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Line:
		    JLine line = (JLine) jGraphics.getOMGraphicWithId(gID);
		    if (line != null) {
			line.update(upgraphic.lf_update());
			line.regenerate(projection);
		    }			
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Rectangle:
		    JRect rect = (JRect) jGraphics.getOMGraphicWithId(gID);
		    if (rect != null) {
			rect.update(upgraphic.rf_update());
			rect.regenerate(projection);
		    }
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Raster:
		    JRaster raster = (JRaster) jGraphics.getOMGraphicWithId(gID);
		    if (raster != null) {
			raster.update(upgraphic.rasf_update());
			raster.regenerate(projection);
		    }
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_Circle:
		    JCircle circ = (JCircle) jGraphics.getOMGraphicWithId(gID);
		    if (circ != null) {
			circ.update(upgraphic.cf_update());
			circ.regenerate(projection);
		    }
		    break;
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_UnitSymbol:
		    JUnit unitsymbol = (JUnit) jGraphics.getOMGraphicWithId(gID);
		    if (unitsymbol != null) {
			unitsymbol.update(upgraphic.usf_update());
			unitsymbol.regenerate(projection);
		    }
		    break;
		    
		    // Uncomment when implemented!!!!
		    
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_2525Symbol:
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_ForceArrow:
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_NewGraphic:
		case com.bbn.openmap.CSpecialist.GraphicPackage.GraphicType._GT_ReorderGraphic:
		    System.err.println("CSpecLayer|"+getName()+
				       "|updateGraphics: Graphics Update Type not implemented.");
		    break;//HACK - unimplemented
		    
		    // unknown update
		default:
		    System.err.println("CSpecLayer|"+getName()+
				       "|updateGraphics: ignoring weird update");
		    break;
		}
	    }
	}
    }

    /**
     * Check if layer will show error dialogs.
     *
     * @return boolean
     */
    public boolean getShowDialogs() {
	return showDialogs;
    }

    /**
     * Set showDialogs behavior.
     *
     * @param show show dialog popups?
     */
    public void setShowDialogs(boolean show) {
	showDialogs = show;
    }

    /**
     *
     */
    protected void postMemoryErrorMsg(String msg) {
	fireRequestMessage(new InfoDisplayEvent(this, msg));
    }

    /**
     *
     */
    protected void postCORBAErrorMsg(String msg) {
	fireRequestMessage(new InfoDisplayEvent(this, msg));
    }

    /**
     *
     */
    protected void postException(String msg) {
	fireRequestMessage(new InfoDisplayEvent(this, msg));
    }

    /**
     *  Free up memory after being removed from the Map
     */
    public void removed(java.awt.Container cont) {

        if (Debug.debugging("cspec")) {
	    System.out.println(getName()+
			       "CSpecLayer.removed(): Nullifying graphics");
	}

	if (specialist != null) {
	    specialist.signoff(clientID);
	}

	jGraphics=null;
	widgets=null;
	gui=null;
	specialist=null;
	projection=null;
    }
}
