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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/PropertyHandler.java,v $
// $RCSfile: PropertyHandler.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.awt.Cursor;
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.JFileChooser;

import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressListener;
import com.bbn.openmap.event.ProgressSupport;
import com.bbn.openmap.gui.ProgressListenerGauge;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.Environment;

/**
 * The PropertyHandler object is the organizer of properties, looking
 * for settings on how to configure OpenMap components.  It is
 * designed to look through a series of locations to find properties
 * files, loading them in order.  If there is a name conflict for a
 * property, the last version of the property set is the one that gets
 * used.  This object isn't really interested in hooking up with other
 * objects.  It's assumed that many objects will want to contact this
 * object, and find the properties that apply to them.  There is one
 * exception this: When components start implementing the
 * PropertyProvider interface, and the PropertyHandler becomes capable
 * of creating an openmap.properties file, then the PropertyHandler
 * will be able to use the BeanContext to query PropertyProviders to
 * get their properties to put in the properties file.<P>
 *
 * The PropertyHandler looks in several places for an openmap.properties file:
 * <UL>
 * <LI> as a resource in the code base.
 * <LI> in the configDir set as a system property at runtime.
 * <LI> in the user's home directory.
 * </UL>
 *
 * For each openmap.properties file, a check is performed to look
 * within for an openmap.include property containing a marker name
 * list.  That list is parsed, and each item is checked
 * (markerName.URL) for an URL to another properties file.<P>
 *
 * Also significant, the PropertyHandler can be given a BeanContext to
 * load components.  For this, the openmap.components property
 * contains a marker name list for openmap objects.  Each member of
 * the list is then used to look for another property
 * (markername.class) which specifies which class names are to be
 * instantiated and added to the BeanContext.  Intelligent components
 * are smart enough to wire themselves together.  Order does matter
 * for the openmap.components property, especially for components that
 * get added to lists and menus.  Place the components in the list in
 * the order that you want components added to the MapHandler.
 */
public class PropertyHandler implements SoloMapComponent {

    /** The name of the properties file to read. */
    public static String propsFileName = "openmap.properties";
    
    /** The name of the system directory containing a properties file. */
    public static String configDirProperty = "openmap.configDir";

    /** 
     * The property name used to hold a list of marker names.  Each
     * marker name is used to create another property to look for to
     * create a component to add to a BeanContext.  For example: <P>
     * <PRE>
     * openmap.components=name1 name2 name3 
     * name1.class=com.bbn.openmap.InformationDelegator
     * name2.class=com.bbn.openmap.MouseDelegator
     * name3.class=com.bbn.openmap.LayerHandler
     * </PRE>
     */
    public static String componentProperty = "openmap.components";

    /** 
     * The property name used to hold a list of marker names.  Each
     * marker name is used to create another property to look for to
     * connect to a URL to load a properties file.  For example: <P>
     * <PRE>
     * openmap.include=name1 name2
     * name1.URL=http://openmap.bbn.com/props/link.properties
     * name2.URL=file:///usr/local/openmap/props/shape.properties
     * </PRE>
     */
    public static String includeProperty = "openmap.include";

    /** Final openmap properties object. */
    protected Properties properties = new Properties();

    /**
     * Container to hold prefixes for components that have been
     * created, in order to determine if duplicates might have been
     * made.  Important if properties are going to be written out, so
     * that property scoping can occur properly.
     */
    protected Set usedPrefixes = Collections.synchronizedSet(new HashSet());

    protected ProgressSupport progressSupport;

    /**
     * Flag to set whether the PropertyHandler should provide status
     * updates to any progress listeners, when it is building
     * components.
     */
    protected boolean updateProgress = false;

    /**
     * Constructor to take resource name, file path or URL string as
     * argument, to create context for a particular map.  
     */
    public PropertyHandler(String urlString) 
	throws MalformedURLException, IOException {
	this(com.bbn.openmap.layer.util.LayerUtils.getResourceOrFileOrURL("com.bbn.openmap.PropertyHandler", urlString));
    }

    /**
     * Constructor to take path (URL) as argument, to create context
     * for a particular map.  
     */
    public PropertyHandler(URL url) 
	throws IOException {

	// Open URL to read in properties
	InputStream is = url.openStream();
	Properties tmpProperties = new Properties();
	tmpProperties.load(is);

	init(tmpProperties, "URL");
	Environment.init(properties);
    }

    /**
     * Constructor to take Properties object as argument, to create
     * context for a particular map.  
     */
    public PropertyHandler(Properties props) {

	init(props, null);
	Environment.init(properties);
    }

    /**
     * Create a PropertyHandler object that checks in the default
     * order for openmap.properties files.  It checks for the
     * openmap.properties file as a resource, in the configDir if
     * specified as a system property, and lastly, in the user's home
     * directory.
     */
    public PropertyHandler() {

	Properties tmpProperties = new Properties();
	Properties includeProperties;
	boolean foundProperties = false;

	boolean showDebugMessages = false;
	
	if (Debug.debugging("properties")) {
	    showDebugMessages = true;
	}

	if (showDebugMessages) {
	    Debug.output("***** Searching for properties ****");
	}
		
	// look for openmap.properties file in jar archive(of course
	// only in same package as this class) or wherever this
	// object's class file lives.
	if (showDebugMessages) {
	    Debug.output("PropertyHandler: Looking for " + 
			 propsFileName + " in Resources");
	}

	InputStream propsIn = getClass().getResourceAsStream(propsFileName);

	// Look in the codebase for applets...
	if (propsIn == null && Environment.isApplet()) {
	    URL[] cba = new URL[1];
	    cba[0] =  Environment.getApplet().getCodeBase();

	    URLClassLoader ucl = URLClassLoader.newInstance(cba);
  	    propsIn = ucl.getResourceAsStream(propsFileName);
	}

	if (propsIn == null) {
  	    propsIn = ClassLoader.getSystemResourceAsStream(propsFileName);
	    
	    if (propsIn != null && showDebugMessages) {
		Debug.output("Loading properties from System Resources: " + 
			     propsFileName);
	    }
	} else {
	    if (showDebugMessages) {
		Debug.output("Loading properties from file " + 
			     propsFileName + " from package of class " + 
			     getClass());
	    }
	}

	if (propsIn != null) {

	    PropUtils.loadProperties(tmpProperties, propsIn);
	    init(tmpProperties, "resources");

	    tmpProperties.clear();
	    foundProperties = true;
	}

	if (foundProperties == false && (Environment.isApplet() || showDebugMessages)) {
	    Debug.output("PropertyHandler: Unable to locate as resource: " + propsFileName);
	}

	//  Seems like we can kick out here in event of Applet...
	if (Environment.isApplet()) {
	    Environment.init(properties);
	    return;
	}

	Properties systemProperties;

	try {
	    systemProperties = System.getProperties();
	} catch (java.security.AccessControlException ace) {
	    systemProperties = new Properties();
	}
	String openmapConfigDirectory = 
	    systemProperties.getProperty(configDirProperty);

	if (openmapConfigDirectory == null) {
	    Vector cps = Environment.getClasspathDirs();
	    String defaultExtraDir = "share";
	    for (int searchCount = 0; searchCount < cps.size(); searchCount++) {
		File shareDir = new File((String)cps.elementAt(searchCount), defaultExtraDir);
		if (shareDir.exists()) {
//  		    Debug.output("Found share directory: " + shareDir.getPath());
		    openmapConfigDirectory = shareDir.getPath();
		    break;
//  		} else {
//  		    Debug.output("No share directory in: " + shareDir.getPath());
		}
	    }
	}

	Environment.addPathToClasspaths(openmapConfigDirectory);

	// in OpenMap config directory
	if (showDebugMessages) {
	    Debug.output("PropertyHandler: Looking for " + 
			 propsFileName + " in configuration directory: " + 
			 (openmapConfigDirectory == null?"not set":openmapConfigDirectory));
	}

	if (PropUtils.loadProperties(tmpProperties, 
				     openmapConfigDirectory, 
				     propsFileName)) {
	    foundProperties = true;
	}
	
	// Include properties from config file properties.
	includeProperties = 
	    getIncludeProperties(tmpProperties.getProperty(includeProperty),
				 tmpProperties);
	merge(includeProperties, properties, 
	      "include file properties", openmapConfigDirectory);
	
	// OK, now merge the config file properties into the main properties
	merge(tmpProperties, properties, 
	      propsFileName, openmapConfigDirectory);
	// Clear out the tmp
	tmpProperties.clear();
	
	// Let system properties take precidence over resource and
	// config dir properties.
	merge(systemProperties, properties,
	      "system properties", "system");
	foundProperties = true;

	// in user's home directory, most precedence.
	String userHomeDirectory  = systemProperties.getProperty("user.home");
	if (showDebugMessages) {
	    Debug.output("PropertyHandler: Looking for " + 
			 propsFileName + " in user's home directory: " + 
			 userHomeDirectory);
	}
	if (PropUtils.loadProperties(tmpProperties, 
				     userHomeDirectory, 
				     propsFileName)) {
	    foundProperties = true;
	}

	if (showDebugMessages) {
	    Debug.output("***** Done with property search ****");
	}

	if (!foundProperties && !Environment.isApplet()) {
	    PropUtils.copyProperties(PropUtils.promptUserForProperties(),
				     properties);
	}

	//  Before we the user properties into the overall properties,
	//  need to check for the include properties URLs, and load
	//  those first.
	includeProperties = 
	    getIncludeProperties(tmpProperties.getProperty(includeProperty),
				 tmpProperties);
	merge(includeProperties, properties, 
	      "include file properties", userHomeDirectory);

	// Now, load the user home preferences last, since they take
	// the highest precidence.
	merge(tmpProperties, properties, propsFileName, userHomeDirectory);

	Environment.init(properties);
    }

    /**
     * Initialize internal properties from Properties object. Appends
     * all the properties it finds, overwriting the ones with the same
     * key.  Called by the two constructors where a Properties object
     * is passed in, or when a URL for a Properties file is provided.
     * This is not called by the consstructor that has to go looking
     * for the properties to use.
     *
     * @param props the properties to merge into the properties held
     * by the PropertyHandler.
     * @param howString a string describing where the properties come
     * from.  Just used for debugging purposes, so passing in a null
     * value is no big deal.
     */
    protected void init(Properties props, String howString) {

	// Include properties noted in resources properties.
	Properties includeProperties = getIncludeProperties(props.getProperty(includeProperty), props);
	merge(includeProperties, properties, 
	      "include file properties", howString);

	if (!Environment.isApplet()) {
	    Properties systemProperties = System.getProperties();
	    merge(systemProperties, props);
	}

	merge(props, properties, "loaded", howString);

	if (Debug.debugging("properties")) {
	    Debug.output("PropertyHandler: loaded properties");
	}
    }

    /**
     * Take a marker name list (space separated names), and open the
     * properties files listed in the propertu with keys of marker.URL.  
     * @param markerList space separated marker names in a single
     * string that needs to be parsed.
     * @param props the properties that the markerList comes from, in
     * order to get the marker.URL properties.
     * @return an allocated Properties object containing all the
     * properties from the inlude files.  If no include files are
     * listed, the Properties object is empty, not null.  
     */
    protected Properties getIncludeProperties(String markerList, 
					      Properties props) {
	Properties newProps = new Properties();
	Properties tmpProps = new Properties();
	Vector includes = PropUtils.parseSpacedMarkers(markerList);
	int size = includes.size();
	if (size > 0) {

	    if (Debug.debugging("propertiesdetail")) {
		Debug.output("PropertyHandler: handling include files: " + 
			     includes);
	    }

	    for (int i = 0; i < size; i++) {
		String includeName = (String) includes.elementAt(i);
		String includeProperty = includeName + ".URL";
		String include = props.getProperty(includeProperty);

		if (include == null) {
		    Debug.error("PropertyHandler.getIncludeProperties(): Failed to locate include file \"" + includeName + "\" with URL \"" + includeProperty + "\"\n  Skipping include file \"" + include + "\"");
		    continue;
		}
		try {
		    tmpProps.clear();
		    // Open URL to read in properties
		    InputStream is = new URL(include).openStream();
		    tmpProps.load(is);
		    if (Debug.debugging("properties")) {
			Debug.output("PropertyHandler.getIncludeProperties(): located include properties file URL: " + include);
		    }
		    // Include properties noted in resources
		    // properties - a little recursive action,
		    // here.
		    Properties includeProperties = getIncludeProperties(tmpProps.getProperty(includeProperty), tmpProps);
		    merge(includeProperties, newProps, 
			  "include file properties", "within " + include);
		    
		    merge(tmpProps, newProps,
			  "include file properties", include);

 		} catch (MalformedURLException e) {
		    Debug.error("PropertyHandler: malformed URL for include file: |" + include + "| for " + includeName);
		} catch (IOException ioe) {
		    Debug.error("PropertyHandler: IOException processing " + include + "| for " + includeName);
		}
	    }
	} else {
	    Debug.message("properties", "PropertyHandler.getIncludeProperties(): no include files found.");
	}
	return newProps;
    }

    /**
     * Take the from properties, copy them into the to properties.
     * @param from the source properties.
     * @param to the destination properties.
     */
    protected void merge(Properties from, Properties to) {
	merge(from, to, null, null);
    }

    /**
     * Take the from properties, copy them into the to properties.
     * The what and where are simple for a more clearly defined Debug
     * statement.  The what and where are only used for debugging
     * statements when there are no properties found, so don't put too
     * much work into creating them, like adding strings together
     * before passing them in.  The what and where fit into a debug
     * output statement like: PropertyHandler.merge(): no _what_ found
     * in _where_.
     * @param from the source properties.
     * @param to the destination properties.
     * @param what a description of what the from properties represent.
     * @param where a description of where the properties were read from.  
     */
    protected void merge(Properties from, Properties to, 
			 String what, String where) {
	if (from.size() > 0) {
	    PropUtils.copyProperties(from, to);
	} else {
	    if (what != null && Debug.debugging("properties")) {
		Debug.output("PropertyHandler.merge(): no " +
			     what + " found" + 
			     (where == null?".":(" in " + where)));
	    }
	}
    }

    /**
     * Merges the properties to the overall properties held by the
     * PropertyHandler.
     */
    public void setProperties(Properties props) {
	init(props, null);
    }

    /**
     * Get the current properties set within this handler.
     */
    public Properties getProperties() {
	return properties;
    }

    /**
     * Given a BeanContext (actually a MapHandler, to handle
     * SoloMapComponents), look for the openmap.components property in
     * the current properties, and parse the list given as that
     * property.  From that list of marker names, look for the
     * marker.class properties and create those Java objects.  Those
     * objects will be added to the BeanContext given.
     *
     * @param context BeanContext.  
     */
    public void createComponents(MapHandler mapHandler) {
	int i; // default counter

	if (mapHandler == null) {
	    Debug.message("properties", "PropertyHandler.createComponents(): null handler.");
	    return;
	}

	
	ProgressListenerGauge plg;

	try {
	    plg = new ProgressListenerGauge("OpenMap Setup");
	    addProgressListener(plg);
	} catch (Exception e) {
	    // Since ProgressListenerGauge is a Swing component, catch
	    // any exception that would be tossed if it can't be
	    // created, like if the PropertyHandler is being used on a
	    // unix system without a DISPLAY.
	    plg = null;
	}

	Vector debugList = PropUtils.parseSpacedMarkers(properties.getProperty(Environment.DebugList));
	int size = debugList.size();

	for (i = 0; i < size; i++) {
	    String debugMarker = (String) debugList.elementAt(i);
	    Debug.put(debugMarker);
	    if (Debug.debugging("properties")) {
		Debug.output("PropertyHandler: adding " + debugMarker + " to Debug list.");
	    }
	}

	Vector componentList = PropUtils.parseSpacedMarkers(properties.getProperty(componentProperty));

	if (Debug.debugging("propertiesdetail")) {
	    Debug.output("PropertyHandler: creating components from " + 
			 componentList);
	}

	fireProgressUpdate(ProgressEvent.START,
			   "OpenMap - Creating Components", 0, 100);

	Vector components = 
	    ComponentFactory.create(componentList, properties, 
				    (updateProgress?getProgressSupport():null));

	size = components.size();

	for (i = 0; i < size; i++) {
	    Object obj = (Object) components.elementAt(i);
	    try {
		if (obj instanceof SoloMapComponent) {
		    mapHandler.add((SoloMapComponent) obj);
		} else {
		    mapHandler.add(obj);
		}
	    
	    } catch (MultipleSoloMapComponentException msmce) {
		Debug.error("PropertyHandler.createComponents(): tried to add multiple components of the same type when only one is allowed! - " + msmce);
	    }
	}

	// Add the used prefixes to the prefix list, for later
	// reference.
	Iterator pres = componentList.iterator();
	while (pres.hasNext()) {
	    addUsedPrefix((String)pres.next());
	}

	fireProgressUpdate(ProgressEvent.DONE,
			   "Created all components, ready...", size, size);
	removeProgressListener(plg);
    }

    public static Properties createOpenMapProperties(MapHandler mapHandler,
						     PrintStream ps) {

	Properties createdProperties = new Properties();

	// First, get all the components in the MapHandler.  Create
	// the openmap.components list, with the .class properties
	// listing all the class names.  Ignore the layers for now,
	// and if the class is a PropertyConsumer, get its properties
	// too.
	if (mapHandler == null) {
	    Debug.error("PropertyHandler.createOpenMapProperties: can't create properties with null MapHandler");
	    return null;
	}
	
	Iterator it = mapHandler.iterator();
	Object someObj;

	Debug.message("properties",
		      "PropertyHandler: Looking for Objects in mapHandler");

	MapBean mapBean = null;
	LayerHandler layerHandler = null;
	PropertyHandler propertyHandler = null;
	InformationDelegator infoDelegator = null;
	Vector otherComponents = new Vector();

	while (it.hasNext()) {
	    someObj = it.next();
	    Debug.message("properties", "PropertyHandler found " + 
			  someObj.getClass().getName());

	    if (someObj instanceof MapBean) {
		mapBean = (MapBean) someObj;
	    } else if (someObj instanceof LayerHandler) {
		layerHandler = (LayerHandler) someObj;
	    } else if (someObj instanceof Layer) {
		// do nothing, layerhandler will handle
	    } else if (someObj instanceof PropertyHandler) {
		propertyHandler = (PropertyHandler) someObj;
		if (infoDelegator != null) {
		    Debug.output("Adding id to ph");
		    propertyHandler.addProgressListener(infoDelegator);
		}
	    } else if (someObj instanceof InformationDelegator) {
		infoDelegator = (InformationDelegator) someObj;
		if (propertyHandler != null) {
		    propertyHandler.addProgressListener((ProgressListener)someObj);
		    Debug.output("Adding id to ph");
		}
	    } else {
		// Add the rest to a component vector thingy.
		otherComponents.add(someObj);
	    }
	}

	// if the MapBean and/or the LayerHandler are null, what's the point?
	if (mapBean == null || layerHandler == null) {
	    Debug.error("PropertyHandler: no MapBean(" + mapBean +
			") or LayerHandler(" + layerHandler + 
			") to use to write properties");
	    return null;
	}

	// First, print the Map parameters...

	ps.println("######  OpenMap properties file ######");
	ps.println("## Refer to original openmap.properties file\n## for instructions on how to modify this file.");
	ps.println("######################################");

	printMapProperties(mapBean, ps);

	printComponentProperties(otherComponents, propertyHandler, ps);

	printLayerProperties(layerHandler, ps);

	return createdProperties;
    }

    protected static void printMapProperties(MapBean mapBean, PrintStream ps) {

	//warning...hackish...
	com.bbn.openmap.proj.Proj proj = mapBean.projection;

	ps.println("\n### OpenMap initial Map Settings ###");
	LatLonPoint llp = proj.getCenter();
	ps.println(Environment.Latitude + "=" +
		   Float.toString(llp.getLatitude()));
	ps.println(Environment.Longitude + "=" + 
		   Float.toString(llp.getLongitude()));
	ps.println(Environment.Scale + "=" + 
		   Float.toString(proj.getScale()));
	ps.println(Environment.Projection + "=" + 
		   proj.getName());

	// Height and Width are in the OpenMapFrame properties, or
	// whatever other component contains everything.

	ps.println(Environment.BackgroundColor + "=" +
		   Integer.toHexString(proj.getBackgroundColor().getRGB()));
	
    }

    protected static void printComponentProperties(Vector components,
						   PropertyHandler ph,
						   PrintStream ps) {

	// this section looks at the components and trys to create
	// the openmap.components list and then write out all the
	// properties for them.
	
	// Since order is important to the look of the application, we
	// need to do work here to maintain the current loaded order
	// of the application components.  Until then, just swipe the
	// openmap.components property to get the list of current
	// components.

	boolean buildConfiguredApplication = true;
	boolean componentListBuilt = false;
	Object someObj;
	int numComponents = 0;
	String markerName;
	StringBuffer componentMarkerString =
	    new StringBuffer(PropertyHandler.componentProperty + "=");

	StringBuffer componentPropsString = new StringBuffer();

	if (ph != null && buildConfiguredApplication) {
	    Properties phProps = ph.getProperties();
	    //  Ahh, phProps'l never be null, right?

	    // Let's build them from the current properties file.
	    componentMarkerString.append(phProps.getProperty(PropertyHandler.componentProperty));

	    Vector componentList = PropUtils.parseSpacedMarkers(phProps.getProperty(componentProperty));
					 
	    for (int i = 0; i < componentList.size(); i++) {
		String markerNameClass = (String)componentList.elementAt(i) + ".class";
		componentPropsString.append(markerNameClass + "=" +phProps.get(markerNameClass) + "\n");

	    }
	    componentListBuilt = true;
	    
	}

	// We're still going through the objects, but only adding the
	// .class properties if the list wasn't built above.
	// Otherwise, the components will be checked to see of they
	// are PropertyConsumers, in order to get their properties
	// written to the file.
	    
	Properties componentProperties = new Properties();
	Enumeration comps = components.elements();
	
	while (comps.hasMoreElements()) {
	    
	    someObj = comps.nextElement();
	    
	    if (someObj instanceof PropertyConsumer) {
		Debug.message("properties", "Getting Property Info for" + 
			      someObj.getClass().getName());
		
		PropertyConsumer pc = (PropertyConsumer)someObj;
		componentProperties.clear();
		markerName = pc.getPropertyPrefix();

		if (!componentListBuilt) {
		    if (markerName != null) {
			componentMarkerString.append(" " + markerName);
		    } else {
			markerName = "component" + (numComponents++);
			componentMarkerString.append(" " + markerName);
			pc.setPropertyPrefix(markerName);
		    }
		
		    componentPropsString.append(markerName + ".class=" +
						someObj.getClass().getName() + "\n");
		}
		
		pc.getProperties(componentProperties);
		
		if (componentProperties.size() > 0) {
		    componentPropsString.append("####\n");
		    Enumeration keys = componentProperties.keys();
		    while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = componentProperties.getProperty(key);
			componentPropsString.append(key + "=" + value + "\n");
		    }
		}
	    } else if (!componentListBuilt) {
		markerName = "component" + (numComponents++);
		componentMarkerString.append(" " + markerName);
		componentPropsString.append(markerName + ".class=" +
					    someObj.getClass().getName() + "\n");
	    }
	}
	
	ps.println("\n\n### OpenMap Components ###");
	ps.println(componentMarkerString.toString());

	ps.println("\n### OpenMap Component Properties ###");
	// list created, add the actual component properties
	ps.println(componentPropsString.toString());
	ps.println("### End Component Properties ###");
    }
	
    protected static void printLayerProperties(LayerHandler layerHandler,
					       PrintStream ps) {

	// Keep track of the LayerHandler.  Use it to get the layers,
	// which can be used to get all the marker names for the
	// openmap.layers property.  The visible layers go to the
	// openmap.startUpLayers property.  Then, cycle through all
	// the layers to get their properties, since they all are
	// PropertyConsumers.
	String markerName;
	StringBuffer layerMarkerString = new StringBuffer("openmap.layers=");
	StringBuffer startUpLayerMarkerString = new StringBuffer("openmap.startUpLayers=");
	StringBuffer layerPropertiesString = new StringBuffer();

	Properties layerProperties = new Properties();

	Layer[] layers = layerHandler.getLayers();
	int numLayers = 0;

	for (int i = 0; i < layers.length; i++) {
	    layerProperties.clear();

	    markerName = layers[i].getPropertyPrefix();

	    if (markerName == null) {
		markerName = "layer" + (numLayers++);
		layers[i].setPropertyPrefix(markerName);
	    }

	    layerMarkerString.append(" " + markerName);

	    if (layers[i].isVisible()) {
		startUpLayerMarkerString.append(" " + markerName);
	    }

	    layers[i].getProperties(layerProperties);
	    layerPropertiesString.append("### -" + markerName +
					 "- layer properties\n");

	    if (layerProperties.size() > 0) {
		Enumeration keys = layerProperties.keys();
		while (keys.hasMoreElements()) {
		    String key = (String) keys.nextElement();
		    String value = layerProperties.getProperty(key);
		    layerPropertiesString.append(key + "=" + value + "\n");
		}
	    }

	    layerPropertiesString.append("### end of -" + markerName + "- properties\n\n");
	}

	ps.println("\n### OpenMap Layers ###");
	ps.println(layerMarkerString.toString());
 	ps.println(startUpLayerMarkerString.toString());
 	ps.println(layerPropertiesString.toString());

    }

    /**
     * Given a MapHandler and a Java Properties object, the
     * LayerHandler will be cleared of it's current layers, and
     * reloaded with the layers in the properties.  The MapBean will
     * be set to the projection settings listed in the properties.
     */
    public void loadProjectionAndLayers(MapHandler mapHandler,
					Properties props) {

	MapBean mapBean = (MapBean) mapHandler.get("com.bbn.openmap.MapBean");
	LayerHandler layerHandler = (LayerHandler) mapHandler.get("com.bbn.openmap.LayerHandler");
	InformationDelegator id = (InformationDelegator) mapHandler.get("com.bbn.openmap.InformationDelegator");

//  	if (id != null) {
//  	    id.requestCursor(new Cursor(Cursor.WAIT_CURSOR));
//  	}
	
	if (layerHandler != null) {
	    layerHandler.removeAll();
	    layerHandler.init(Environment.OpenMapPrefix, props);
	} else {
	    Debug.error("Can't load new layers - can't find LayerHandler");
	}

	if (mapBean != null) {
	    String projName = props.getProperty(Environment.Projection);
	    int projType = com.bbn.openmap.proj.ProjectionFactory.getProjType(projName);
	    mapBean.setProjectionType(projType);

	    mapBean.setScale(LayerUtils.floatFromProperties(
		props, Environment.Scale, Float.POSITIVE_INFINITY));
	    float lat = LayerUtils.floatFromProperties(
		props, Environment.Latitude, 0f);
	    float lon = LayerUtils.floatFromProperties(
		props, Environment.Longitude, 0f);

	    mapBean.setCenter(new LatLonPoint(lat, lon));

	} else {
	    Debug.error("Can't load new projection - can't find MapBean");
	}

//  	if (id != null) {
//  	    id.requestCursor(null);
//  	}
    }

    /**
     * If you are creating a new object, it's important to get a
     * unique prefix for its properties.  This function takes a prefix
     * string and checks it against all others it knows about.  If
     * there is a conflict, it adds a number to the end until it
     * becomes unique.
     */
    public String getUniquePrefix(String prefix) {
	prefix = prefix.replace(' ', '_');

	if (!addUsedPrefix(prefix)) {
	    int count = 2;
	    String nextTry = prefix + (count);
	    while (!addUsedPrefix(nextTry)) {
		nextTry = prefix + (++count);
	    }
	    return nextTry;
	} else {
	    return prefix;
	}
    }

    /**
     * Changes ' ' characters to '_', and then tries to add it to the
     * used prefix list.  Returns true if successful.  
     */
    public boolean addUsedPrefix(String prefix) {
	prefix.replace(' ', '_');

	return usedPrefixes.add(prefix.intern());
    }

    /**
     * Changes ' ' characters to '_', and then tries to remove it to
     * the used prefix list.  Returns true if successful.  
     */
    public boolean removeUsedPrefix(String prefix) {
	prefix.replace(' ', '_');

	return usedPrefixes.remove(prefix.intern());
    }


    /**
     * Add a ProgressListener that will display build progress.
     */
    public void addProgressListener(ProgressListener list) {
	getProgressSupport().addProgressListener(list);
    }

    /**
     * Remove a ProgressListener that displayed build progress.
     */
    public void removeProgressListener(ProgressListener list) {
	getProgressSupport().removeProgressListener(list);
    }

    /**
     * Clear all progress listeners.
     */
    public void clearProgressListeners() {
	getProgressSupport().removeAll();
    }

    /**
     * Get progress support if needed.
     */
    protected ProgressSupport getProgressSupport() {
	if (progressSupport == null) {
	    progressSupport = new ProgressSupport(this);
	}
	return progressSupport;
    }

    /**
     * Fire an build update to progress listeners.
     * @param frameNumber the current frame count
     * @param totalFrames the total number of frames. 
     */
    protected void fireProgressUpdate(int type, String task, 
				      int frameNumber, 
				      int totalFrames) {
	if (updateProgress) {
	    getProgressSupport().fireUpdate(type, task, totalFrames, frameNumber);
	} else if (type == ProgressEvent.DONE) {
	    // At least turn off progress listeners if they are up.
	    getProgressSupport().fireUpdate(ProgressEvent.DONE, task, totalFrames, frameNumber);
	}
    }

    /**
     * Set a flag that will trigger the PropertyHandler to fire
     * progress events when it is going through the creation process.
     */
    public void setUpdateProgress(boolean set) {
	updateProgress = set;
    }

    public boolean getUpdateProgress() {
	return updateProgress;
    }
}

