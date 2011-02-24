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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/PropertyHandler.java,v $
// $RCSfile: PropertyHandler.java,v $
// $Revision: 1.29 $
// $Date: 2008/02/28 23:36:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressListener;
import com.bbn.openmap.event.ProgressSupport;
import com.bbn.openmap.gui.ProgressListenerGauge;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.plugin.PlugIn;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The PropertyHandler object is the organizer of properties, looking for
 * settings on how to configure OpenMap components. It is designed to look
 * through a series of locations to find properties files, loading them in
 * order. If there is a name conflict for a property, the last version of the
 * property set is the one that gets used. This object isn't really interested
 * in hooking up with other objects. It's assumed that many objects will want to
 * contact this object, and find the properties that apply to them. There is one
 * exception this: When components start implementing the PropertyProvider
 * interface, and the PropertyHandler becomes capable of creating an properties
 * file, then the PropertyHandler will be able to use the BeanContext to query
 * PropertyProviders to get their properties to put in the properties file.
 * <P>
 * 
 * The PropertyHandler looks in several places for an openmap.properties file:
 * <UL>
 * <LI>as a resource in the code base.
 * <LI>in the configDir set as a system property at runtime.
 * <LI>in the user's home directory.
 * </UL>
 * 
 * For each properties file, a check is performed to look within for an include
 * property containing a marker name list. That list is parsed, and each item is
 * checked (markerName.URL) for an URL to another properties file.
 * <P>
 * 
 * Also significant, the PropertyHandler can be given a BeanContext to load
 * components. For this, the openmap.components property contains a marker name
 * list for openmap objects. Each member of the list is then used to look for
 * another property (markername.class) which specifies which class names are to
 * be instantiated and added to the BeanContext. Intelligent components are
 * smart enough to wire themselves together. Order does matter for the
 * openmap.components property, especially for components that get added to
 * lists and menus. Place the components in the list in the order that you want
 * components added to the MapHandler.
 * <P>
 * 
 * If the debug.showprogress environment variable is set, the PropertyHandler
 * will display a progress bar when it is creating components. If the
 * debug.properties file is set, the steps that the PropertyHandler takes in
 * looking for property files will be displayed.
 * <P>
 * 
 * If the PropertyHandler is created with an empty constructor or with a null
 * Properties object, it will do the search for an openmap.properties file. If
 * you don't want it to do that search, create it with an empty Properties
 * object.
 */
public class PropertyHandler
        extends MapHandlerChild
        implements SoloMapComponent {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.PropertyHandler");

    /**
     * All components can have access to an I18n object, which is provided by
     * the Environment.
     */
    protected transient I18n i18n = Environment.getI18n();

    /**
     * The propertyPrefix can be set to reflect a particular set of properties,
     * or for an application. If this variable is not set, 'openmap' will be
     * used. This prefix will be placed in front of the default properties file
     * that will be sought if a specific properties file is not specified, and
     * will also be placed in front of the standard application component
     * properties.
     */
    protected String propertyPrefix;

    /**
     * The appendix for the name of the properties file to read. The
     * propertyPrefix will be prepended to this string for the default property
     * file search.
     */
    public final static String propsFileName = "properties";

    /**
     * The name of the system directory containing a properties file. The
     * propertyPrefix.configDir property will be checked for a possible location
     * for properties.
     */
    public final static String configDirProperty = "configDir";

    /**
     * The property name used to hold a list of marker names. Each marker name
     * is used to create another property to look for to create a component to
     * add to a BeanContext. For example:
     * <P>
     * 
     * <PRE>
     * # if 'openmap' is the PropertyHandler property prefix...
     * openmap.components=name1 name2 name3
     * name1.class=com.bbn.openmap.InformationDelegator
     * name2.class=com.bbn.openmap.MouseDelegator
     * name3.class=com.bbn.openmap.LayerHandler
     * 
     * </PRE>
     */
    public final static String componentProperty = "components";

    /**
     * The property name used to hold a list of marker names. Each marker name
     * is used to create another property to look for to connect to a URL to
     * load a properties file. For example:
     * <P>
     * 
     * <PRE>
     * 
     * openmap.include=name1 name2
     * name1.URL=http://openmap.bbn.com/props/link.properties
     * name2.URL=file:///usr/local/openmap/props/shape.properties
     * 
     * </PRE>
     */
    public final static String includeProperty = "include";

    /**
     * The property name used to hold a file, resource or URL of a file to use
     * containing localized properties, like layer names. This is optional, if
     * it's not in the openmap.properties file or the properties file being read
     * in, an openmap_&ltlocalization string&gt.properties file will be searched
     * for in the classpath (i.e. openmap.localized=openmap_en_US.properties).
     */
    public final static String localizedProperty = "localized";

    /** Final openmap properties object. */
    protected Properties properties = new Properties();

    /**
     * Container to hold prefixes for components that have been created, in
     * order to determine if duplicates might have been made. Important if
     * properties are going to be written out, so that property scoping can
     * occur properly. This collection holds prefixes of objects that have been
     * created by this PropertyHandler, and also prefixes that have been given
     * out on request.
     */
    protected Set usedPrefixes = Collections.synchronizedSet(new HashSet());

    protected ProgressSupport progressSupport;

    /**
     * Flag to set whether the PropertyHandler should provide status updates to
     * any progress listeners, when it is building components.
     */
    protected boolean updateProgress = false;

    /**
     * A hashtable to keep track of property prefixes and the objects that were
     * created for them.
     */
    protected Hashtable prefixLibrarian = new Hashtable();

    protected boolean DEBUG = false;

    /**
     * Create a PropertyHandler object that checks in the default order for
     * openmap.properties files. It checks for the openmap.properties file as a
     * resource, in the configDir if specified as a system property, and lastly,
     * in the user's home directory. If you want an empty PropertyHandler that
     * doesn't do the search, use the constructor that takes a
     * java.util.Properties object and provide it with empty Properties.
     */
    public PropertyHandler() {
        this(new Builder());
    }

    /**
     * Create a PropertyHandler object that checks in the default order for
     * openmap.properties files. It checks for the openmap.properties file as a
     * resource, in the configDir if specified as a system property, and lastly,
     * in the user's home directory.
     * 
     * @param provideProgressUpdates if true, a progress bar will be displayed
     *        to show the progress of building components.
     */
    public PropertyHandler(boolean provideProgressUpdates) {
        this(new Builder().setProgressUpdates(provideProgressUpdates));
    }

    /**
     * Constructor to take resource name, file path or URL string as argument,
     * to create context for a particular map.
     */
    public PropertyHandler(String urlString)
            throws MalformedURLException, IOException {
        this(new Builder().setPropertiesFile(urlString));
    }

    /**
     * Constructor to take path (URL) as argument, to create context for a
     * particular map.
     */
    public PropertyHandler(URL url)
            throws IOException {
        this(new Builder().setPropertiesFile(url));
    }

    /**
     * Constructor to take Properties for configuration, using the default
     * "openmap" property prefix for configuration.
     * 
     * @param props
     */
    public PropertyHandler(Properties props) {
        this(new Builder().setProperties(props));
    }

    public PropertyHandler(Builder builder) {
        DEBUG = logger.isLoggable(Level.FINE);

        setPropertyPrefix(builder.propertyPrefix);
        setUpdateProgress(builder.update);

        Properties properties = builder.properties;

        if (properties == null) {
            searchForAndLoadProperties();
        } else {
            init(properties, "URL");
            Environment.init(getProperties());
        }
    }

    /**
     * Provides this class with the default properties file name to look for.
     * Can be overridden by subclasses to return custom file names.
     * 
     * @return String of properties file to search for in classpath, configDir
     *         and user's home directory.
     */
    public String getDefaultPropertyFileName() {
        return PropUtils.getScopedPropertyPrefix(Environment.OpenMapPrefix) + PropertyHandler.propsFileName;
    }

    /**
     * Look for an openmap.properties file in the classpath, configDirectory and
     * user home directory, in that order. If any property is duplicated in any
     * file found in those locations, the last one in wins.
     */
    protected void searchForAndLoadProperties() {
        searchForAndLoadProperties(getDefaultPropertyFileName());
    }

    /**
     * Look for a properties file as a resource in the classpath, in the config
     * directory, and in the user's home directory, in that order. If any
     * property is duplicated in any version, last one wins.
     * 
     * @param propsFileName to search for
     */
    protected void searchForAndLoadProperties(String propsFileName) {

        Properties tmpProperties = new Properties();
        Properties includeProperties;
        Properties localizedProperties;
        boolean foundProperties = false;

        if (Debug.debugging("locale")) {
            java.util.Locale.setDefault(new java.util.Locale("pl", "PL"));
        }

        if (Debug.debugging("showprogress")) {
            updateProgress = true;
        }

        logger.fine("***** Searching for properties ****");

        String propertyPrefix = PropUtils.getScopedPropertyPrefix(getPropertyPrefix());

        // look for openmap.properties file in jar archive(of course
        // only in same package as this class) or wherever this
        // object's class file lives.
        if (DEBUG) {
            logger.fine("Looking for " + propsFileName + " in Resources");
        }

        InputStream propsIn = getClass().getResourceAsStream(propsFileName);

        // Look in the codebase for applets...
        if (propsIn == null && Environment.isApplet()) {
            URL[] cba = new URL[1];
            cba[0] = Environment.getApplet().getCodeBase();

            URLClassLoader ucl = URLClassLoader.newInstance(cba);
            propsIn = ucl.getResourceAsStream(propsFileName);
        }

        if (propsIn == null) {
            propsIn = ClassLoader.getSystemResourceAsStream(propsFileName);

            if (propsIn != null && DEBUG) {
                logger.fine("Loading properties from System Resources: " + propsFileName);
            }
        } else {
            if (DEBUG) {
                logger.fine("Loading properties from file " + propsFileName + " from package of class " + getClass());
            }
        }

        if (propsIn != null) {
            foundProperties = PropUtils.loadProperties(tmpProperties, propsIn);
            init(tmpProperties, "resources");
            tmpProperties.clear();
        }

        if (!foundProperties && (Environment.isApplet() || DEBUG)) {
            logger.fine("Unable to locate as resource: " + propsFileName);
        }

        // Seems like we can kick out here in event of Applet...
        if (Environment.isApplet()) {
            Environment.init(getProperties());
            return;
        }

        Properties systemProperties;

        try {
            systemProperties = System.getProperties();
        } catch (java.security.AccessControlException ace) {
            systemProperties = new Properties();
        }

        String configDirProperty = propertyPrefix + PropertyHandler.configDirProperty;

        String openmapConfigDirectory = systemProperties.getProperty(configDirProperty);

        if (openmapConfigDirectory == null) {
            Vector<String> cps = Environment.getClasspathDirs();
            String defaultExtraDir = "share";
            for (String searchLoc : cps) {
                File shareDir = new File(searchLoc, defaultExtraDir);
                if (shareDir.exists()) {
                    // Debug.output("Found share directory: " +
                    // shareDir.getPath());
                    openmapConfigDirectory = shareDir.getPath();
                    break;
                    // } else {
                    // Debug.output("No share directory in: " +
                    // shareDir.getPath());
                }
            }
        }

        Environment.addPathToClasspaths(openmapConfigDirectory);

        // in OpenMap config directory
        if (DEBUG) {
            logger.fine("PropertyHandler: Looking for " + propsFileName + " in configuration directory: "
                    + (openmapConfigDirectory == null ? "not set" : openmapConfigDirectory));
        }

        // We want foundProperties to reflect if properties have ever
        // been found.
        foundProperties |= PropUtils.loadProperties(tmpProperties, openmapConfigDirectory, propsFileName);

        // Include properties from config file properties.
        includeProperties = getIncludeProperties(tmpProperties.getProperty(propertyPrefix + includeProperty), tmpProperties);
        merge(includeProperties, "include file properties", openmapConfigDirectory);

        // OK, now merge the config file properties into the main
        // properties
        merge(tmpProperties, propsFileName, openmapConfigDirectory);
        // Clear out the tmp
        tmpProperties.clear();

        // Let system properties take precedence over resource and
        // config dir properties.
        merge(systemProperties, "system properties", "system");

        // in user's home directory, most precedence.
        String userHomeDirectory = systemProperties.getProperty("user.home");
        if (DEBUG) {
            logger.fine("Looking for " + propsFileName + " in user's home directory: " + userHomeDirectory);
        }

        // We want foundProperties to reflect if properties have ever
        // been found.
        foundProperties |= PropUtils.loadProperties(tmpProperties, userHomeDirectory, propsFileName);
        if (DEBUG) {
            logger.fine("***** Done with property search ****");
        }

        if (!foundProperties && !Environment.isApplet()) {
            PropUtils.copyProperties(PropUtils.promptUserForProperties(), properties);
        }

        // Before we the user properties into the overall properties,
        // need to check for the include properties URLs, and load
        // those first.
        includeProperties = getIncludeProperties(tmpProperties.getProperty(propertyPrefix + includeProperty), tmpProperties);
        merge(includeProperties, "include file properties", userHomeDirectory);

        // Now, load the user home preferences last, since they take
        // the highest precedence.
        merge(tmpProperties, propsFileName, userHomeDirectory);

        // Well, they used to take the highest precedence. Now, we
        // look for a localized property file, and write those
        // properties on top.
        localizedProperties =
                getLocalizedProperties(tmpProperties.getProperty(propertyPrefix + localizedProperty), userHomeDirectory);
        merge(localizedProperties, "localized properties", null);

        Environment.init(getProperties());
    }

    /**
     * Load the localized properties that will take precedence over all other
     * properties. If the localizedPropertyFile is null, a localized version of
     * the openmap.properties file will be searched for in the classpath and in
     * the user home directory (if that isn't null as well).
     */
    protected Properties getLocalizedProperties(String localizedPropertyFile, String userHomeDirectory) {
        Properties props = null;
        if (localizedPropertyFile == null) {
            java.util.Locale loc = java.util.Locale.getDefault();
            localizedPropertyFile = "openmap_" + loc.toString() + ".properties";
        }

        boolean tryHomeDirectory = false;
        if (DEBUG) {
            logger.fine("Looking for localized file: " + localizedPropertyFile);
        }

        try {
            URL propsURL = PropUtils.getResourceOrFileOrURL(localizedPropertyFile);
            if (propsURL == null) {
                tryHomeDirectory = true;
            } else {
                if (DEBUG) {
                    logger.fine("Found localized properties in classpath");
                }
                props = fetchProperties(propsURL);
            }
        } catch (MalformedURLException murle) {
            logger.warning("PropertyHandler can't find localized property file: " + localizedPropertyFile);
            tryHomeDirectory = true;
        }

        if (tryHomeDirectory) {
            props = new Properties();
            if (!PropUtils.loadProperties(props, userHomeDirectory, localizedPropertyFile)) {
                props = null;
            } else {
                if (DEBUG) {
                    logger.fine("Found localized properties in home directory");
                }
            }
        }

        if (props == null) {
            props = new Properties();
        }

        return props;
    }

    /**
     * Initialize internal properties from Properties object. Appends all the
     * properties it finds, overwriting the ones with the same key. Called by
     * the two constructors where a Properties object is passed in, or when a
     * URL for a Properties file is provided. This is not called by the
     * constructor that has to go looking for the properties to use.
     * 
     * @param props the properties to merge into the properties held by the
     *        PropertyHandler.
     * @param howString a string describing where the properties come from. Just
     *        used for debugging purposes, so passing in a null value is no big
     *        deal.
     */
    protected void init(Properties props, String howString) {

        // Include properties noted in resources properties.
        String prefix = PropUtils.getScopedPropertyPrefix(getPropertyPrefix());
        Properties includeProperties = getIncludeProperties(props.getProperty(prefix + includeProperty), props);
        merge(includeProperties, "include file properties", howString);

        if (!Environment.isApplet()) {
            Properties systemProperties = System.getProperties();
            merge(systemProperties, props);
        }

        merge(props, "loaded", howString);

        if (DEBUG) {
            logger.fine("loaded properties");
        }
    }

    /**
     * Take a marker name list (space separated names), and open the properties
     * files listed in the property with keys of marker.URL.
     * 
     * @param markerList space separated marker names in a single string that
     *        needs to be parsed.
     * @param props the properties that the markerList comes from, in order to
     *        get the marker.URL properties.
     * @return an allocated Properties object containing all the properties from
     *         the include files. If no include files are listed, the Properties
     *         object is empty, not null.
     */
    protected Properties getIncludeProperties(String markerList, Properties props) {
        Properties newProps = new Properties();
        Properties tmpProps = new Properties();
        Vector<String> includes = PropUtils.parseSpacedMarkers(markerList);
        int size = includes.size();
        if (size > 0) {

            if (logger.isLoggable(Level.FINER)) {
                logger.finer("handling include files: " + includes);
            }

            for (int i = 0; i < size; i++) {
                String includeName = (String) includes.elementAt(i);
                String includeProperty = includeName + ".URL";

                String include = props.getProperty(includeProperty);
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("checking " + includeProperty + ", getting: " + include);
                }

                if (include == null) {
                    logger.warning("PropertyHandler.getIncludeProperties(): Failed to locate include file \"" + includeName
                            + "\" with URL \"" + includeProperty + "\"\n  Skipping include file \"" + include + "\"");
                    continue;
                }
                try {
                    tmpProps.clear();
                    // Open URL to read in properties
                    URL tmpInclude = PropUtils.getResourceOrFileOrURL(null, include);

                    if (tmpInclude == null) {
                        logger.fine("Can't locate URL trying to find included properties: " + include);
                        continue;
                    }

                    InputStream is = tmpInclude.openStream();
                    tmpProps.load(is);
                    if (DEBUG) {
                        logger.fine("PropertyHandler.getIncludeProperties(): located include properties file URL: " + include);
                    }
                    // Include properties noted in resources
                    // properties - a little recursive action,
                    // here.
                    Properties includeProperties = getIncludeProperties(tmpProps.getProperty(includeProperty), tmpProps);
                    merge(includeProperties, newProps, "include file properties", "within " + include);

                    merge(tmpProps, newProps, "include file properties", include);

                } catch (MalformedURLException e) {
                    logger.warning("malformed URL for include file: |" + include + "| for " + includeName);
                } catch (IOException ioe) {
                    logger.warning("IOException processing " + include + "| for " + includeName);
                }
            }
        } else {
            logger.fine("no include files found.");
        }
        return newProps;
    }

    /**
     * Take the from properties, copy them into the internal PropertyHandler
     * properties.
     * 
     * @param from the source properties.
     */
    protected void merge(Properties from) {
        merge(from, getProperties(), null, null);
    }

    /**
     * Take the from properties, copy them into the to properties.
     * 
     * @param from the source properties.
     * @param to the destination properties.
     */
    protected void merge(Properties from, Properties to) {
        merge(from, to, null, null);
    }

    /**
     * Take the from properties, copy them into the internal PropertyHandler
     * properties. The what and where are simple for a more clearly defined
     * logging statement. The what and where are only used for debugging
     * statements when there are no properties found, so don't put too much work
     * into creating them, like adding strings together before passing them in.
     * The what and where fit into a debug output statement like:
     * PropertyHandler.merge(): no _what_ found in _where_.
     * 
     * @param from the source properties.
     * @param what a description of what the from properties represent.
     * @param where a description of where the properties were read from.
     */
    protected void merge(Properties from, String what, String where) {
        merge(from, getProperties(), what, where);
    }

    /**
     * Take the from properties, copy them into the to properties. The what and
     * where are simple for a more clearly defined logging statement. The what
     * and where are only used for debugging statements when there are no
     * properties found, so don't put too much work into creating them, like
     * adding strings together before passing them in. The what and where fit
     * into a debug output statement like: PropertyHandler.merge(): no _what_
     * found in _where_.
     * 
     * @param from the source properties.
     * @param to the destination properties.
     * @param what a description of what the from properties represent.
     * @param where a description of where the properties were read from.
     */
    protected void merge(Properties from, Properties to, String what, String where) {
        if (!from.isEmpty()) {

            if (to == null) {
                to = getProperties();
            }
            PropUtils.copyProperties(from, to);
        } else {
            if (what != null && DEBUG) {
                logger.fine("no " + what + " found" + (where == null ? "." : (" in " + where)));
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
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    /**
     * Given a property prefix, or markername, from the properties file, get the
     * object that was created for it. This method uses the prefix librarian.
     */
    public Object get(String markername) {
        return prefixLibrarian.get(markername.intern());
    }

    /**
     * Get a properties object containing all the properties with the given
     * prefix.
     */
    public Properties getProperties(String prefix) {
        Properties prefixProperties = new Properties();
        Properties props = getProperties();
        if (prefix != null) {
            String scopedPrefix = prefix + ".";
            for (Enumeration e = props.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                if (key.startsWith(scopedPrefix)) {
                    prefixProperties.put(key, props.get(key));
                }
            }
        }
        return prefixProperties;
    }

    /**
     * Register an object with the prefix librarian against a specific marker
     * name.
     */
    public void put(String markername, Object obj) {
        prefixLibrarian.put(markername.intern(), obj);
    }

    /**
     * Remove an object from the prefix librarian register, returning that
     * object if it has been found.
     */
    public Object remove(String markername) {
        return prefixLibrarian.remove(markername);
    }

    /**
     * Get the Hashtable being held that matches property prefix strings with
     * components.
     */
    public Hashtable getPrefixLibrarian() {
        return prefixLibrarian;
    }

    /**
     * Given a BeanContext (actually a MapHandler, to handle SoloMapComponents),
     * look for the openmap.components property in the current properties, and
     * parse the list given as that property. From that list of marker names,
     * look for the marker.class properties and create those Java objects. Those
     * objects will be added to the BeanContext given.
     * 
     * @param mapHandler BeanContext.
     */
    public void createComponents(MapHandler mapHandler) {
        int i; // default counter

        if (mapHandler == null) {
            logger.fine("no MapHandler to use to handle created components, skipping creation.");
            return;
        }

        ProgressListenerGauge plg = null;

        if (updateProgress) {
            try {
                String internString = i18n.get(this.getClass(), "progressTitle", "Progress");
                plg = new ProgressListenerGauge(internString);
                plg.setWindowSupport(new WindowSupport(plg, new WindowSupport.Frm("", true)));
                addProgressListener(plg);
            } catch (Exception e) {
                // Since ProgressListenerGauge is a Swing component, catch
                // any exception that would be tossed if it can't be
                // created, like if the PropertyHandler is being used on a
                // unix system without a DISPLAY.
                // plg = null;
            }
        }

        Vector<String> debugList = PropUtils.parseSpacedMarkers(properties.getProperty(Environment.DebugList));
        int size = debugList.size();

        for (String debugMarker : debugList) {
            Debug.put(debugMarker);
            if (DEBUG) {
                logger.fine("adding " + debugMarker + " to Debug list.");
            }
        }

        String propPrefix = PropUtils.getScopedPropertyPrefix(getPropertyPrefix());
        String componentProperty = propPrefix + PropertyHandler.componentProperty;
        Vector<String> componentList = PropUtils.parseSpacedMarkers(properties.getProperty(componentProperty));

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("creating components from " + componentList);
        }

        if (updateProgress) {
            fireProgressUpdate(ProgressEvent.START,
                               i18n.get(this.getClass(), "creatingComponentsProgressMessage", "Creating Components"), 0, 100);
        }

        Vector components =
                ComponentFactory.create(componentList, properties, (updateProgress ? getProgressSupport() : null), true);

        size = components.size();

        for (i = 0; i < size; i++) {
            Object obj = (Object) components.elementAt(i);
            try {
                if (obj instanceof String) {
                    logger.warning("finding out that the " + obj + " wasn't created");
                    continue;
                }

                // mapHandler.add(obj);

                // The call to add(obj) above was replaced by the call to
                // addLayer() below. This seems to speed up the startup process,
                // but if some other component calls mapHandler.add(obj), then
                // this list will be purged.
                mapHandler.addLater(obj);

                String markerName = ((String) componentList.elementAt(i)).intern();
                prefixLibrarian.put(markerName, obj);
                addUsedPrefix(markerName);

            } catch (MultipleSoloMapComponentException msmce) {
                logger.warning("PropertyHandler.createComponents(): " + "tried to add multiple components of the same "
                        + "type when only one is allowed! - " + msmce);
            }
        }

        // Added as a result of the addLater(obj) call above...
        mapHandler.purgeLaterList();

        if (updateProgress) {
            fireProgressUpdate(ProgressEvent.DONE,
                               i18n.get(this.getClass(), "completedProgressMessage", "Created all components, ready..."), size,
                               size);
            removeProgressListener(plg);
        }
    }

    public String getPropertyPrefix() {
        String propertyPrefix = this.propertyPrefix;
        if (propertyPrefix == null) {
            propertyPrefix = Environment.OpenMapPrefix;
        }

        return propertyPrefix;
    }

    public void setPropertyPrefix(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    /**
     * Creates a Properties object containing the current settings as defined by
     * OpenMap components held by the MapHandler. If the MapHandler contains a
     * PropertyHandler, that property handler will be consulted for properties
     * for different objects in case those objects don't know how to provide
     * their settings correctly.
     * 
     * @param mapHandler MapHandler containing components to use for Properties.
     * @param ps PrintStream to write properties to, may be null if you just
     *        want the Properties object that is returned.
     * @return Properties object containing everything written (or that would
     *         have been written, if the PrintStream is null) to PrintStream.
     */
    public static Properties createOpenMapProperties(MapHandler mapHandler, PrintStream ps) {

        Properties createdProperties = new Properties();

        // First, get all the components in the MapHandler. Create
        // the openmap.components list, with the .class properties
        // listing all the class names. Ignore the layers for now,
        // and if the class is a PropertyConsumer, get its properties
        // too.
        if (mapHandler == null) {
            logger.warning("can't create properties with null MapHandler");
            return null;
        }

        Iterator it = mapHandler.iterator();
        Object someObj;

        logger.fine("Looking for objects in MapHandler");

        MapBean mapBean = null;
        LayerHandler layerHandler = null;
        PropertyHandler propertyHandler = null;
        InformationDelegator infoDelegator = null;
        Vector otherComponents = new Vector();

        while (it.hasNext()) {
            someObj = it.next();
            logger.fine("found " + someObj.getClass().getName());

            if (someObj instanceof MapBean) {
                mapBean = (MapBean) someObj;
            } else if (someObj instanceof LayerHandler) {
                layerHandler = (LayerHandler) someObj;
            } else if (someObj instanceof Layer || someObj instanceof PlugIn) {
                // do nothing, layerhandler will handle
            } else if (someObj instanceof PropertyHandler) {
                propertyHandler = (PropertyHandler) someObj;
                if (infoDelegator != null) {
                    propertyHandler.addProgressListener(infoDelegator);
                }
            } else if (someObj instanceof InformationDelegator) {
                infoDelegator = (InformationDelegator) someObj;
                if (propertyHandler != null) {
                    propertyHandler.addProgressListener((ProgressListener) someObj);
                }
            } else {
                // Add the rest to a component vector thingy.
                otherComponents.add(someObj);
            }
        }

        // if the MapBean and/or the LayerHandler are null, what's the
        // point?
        if (mapBean == null || layerHandler == null) {
            logger.warning("no MapBean(" + mapBean + ") or LayerHandler(" + layerHandler + ") to use to write properties");
            return null;
        }

        // First, print the Map parameters...

        ps.println("######  OpenMap properties file ######");
        ps.println("## Refer to original openmap.properties file\n## for instructions on how to modify this file.");
        ps.println("######################################");

        printMapProperties(mapBean, ps, createdProperties);
        printComponentProperties(otherComponents, propertyHandler, ps, createdProperties);
        printLayerProperties(layerHandler, propertyHandler, ps, createdProperties);

        if (logger.isLoggable(Level.FINE) && createdProperties != null) {
            System.out.println(createdProperties);
        }

        return createdProperties;
    }

    /**
     * A simple helper method that writes key-value pairs to a print stream or
     * Properties, whatever is not null.
     */
    protected static void printProperties(String key, String value, PrintStream ps, Properties createdProperties) {
        if (ps != null) {
            ps.println(key + "=" + value);
        }
        if (createdProperties != null) {
            createdProperties.put(key, value);
        }
    }

    /**
     * A helper function to createOpenMapProperties that gets the current
     * properties of the MapBean and prints them out to the PrintStream and the
     * provided Properties object.
     * 
     * @param mapBean MapBean to get parameters from.
     * @param ps PrintStream to write properties to, may be null.
     * @param createdProperties Properties object to store properties in, may be
     *        null.
     */
    protected static void printMapProperties(MapBean mapBean, PrintStream ps, Properties createdProperties) {

        // warning...hackish...
        com.bbn.openmap.proj.Proj proj = mapBean.projection;

        ps.println("\n### OpenMap initial Map Settings ###");
        Point2D llp = proj.getCenter();

        printProperties(Environment.Latitude, Double.toString(llp.getY()), ps, createdProperties);

        printProperties(Environment.Longitude, Double.toString(llp.getX()), ps, createdProperties);

        printProperties(Environment.Scale, Float.toString(proj.getScale()), ps, createdProperties);

        printProperties(Environment.Projection, proj.getName(), ps, createdProperties);

        printProperties(Environment.BackgroundColor, Integer.toHexString(mapBean.getBackground().getRGB()), ps, createdProperties);

        // Height and Width are in the OpenMapFrame properties, or
        // whatever other component contains everything.
    }

    /**
     * A helper function to createOpenMapProperties that gets the current
     * properties of the given components and prints them out to the PrintStream
     * and the provided Properties object.
     * 
     * @param components Vector of components to get parameters from.
     * @param ph PropertyHandler that may have properties to use as a foundation
     *        for the properties for the components. If the component can't
     *        provide properties reflecting its settings, the property handler
     *        will be consulted for properties it knows about for that
     *        component.
     * @param ps PrintStream to write properties to, may be null.
     * @param createdProperties Properties object to store properties in, may be
     *        null.
     */
    protected static void printComponentProperties(Vector components, PropertyHandler ph, PrintStream ps,
                                                   Properties createdProperties) {

        // this section looks at the components and trys to create
        // the openmap.components list and then write out all the
        // properties for them.

        // Since order is important to the look of the application, we
        // need to do work here to maintain the current loaded order
        // of the application components. Until then, just swipe the
        // openmap.components property to get the list of current
        // components.

        boolean buildConfiguredApplication = true;
        boolean componentListBuilt = false;
        Object someObj;
        int numComponents = 0;
        String markerName;
        String componentProperty = PropertyHandler.componentProperty;
        StringBuffer componentMarkerString = new StringBuffer(componentProperty).append("=");
        if (ph != null) {
            String phPrefix = PropUtils.getScopedPropertyPrefix(ph.getPropertyPrefix());
            componentProperty = phPrefix + PropertyHandler.componentProperty;
            componentMarkerString = new StringBuffer(componentProperty).append("=");
        }

        StringBuffer componentPropsString = new StringBuffer();

        if (ph != null && buildConfiguredApplication) {
            Properties phProps = ph.getProperties();
            // Ahh, phProps'l never be null, right?

            // Let's build them from the current properties file.
            componentMarkerString.append(phProps.getProperty(componentProperty));

            Vector componentList = PropUtils.parseSpacedMarkers(phProps.getProperty(componentProperty));

            for (int i = 0; i < componentList.size(); i++) {
                String markerNameClass = (String) componentList.elementAt(i) + ".class";
                componentPropsString.append(markerNameClass).append("=").append(phProps.get(markerNameClass)).append("\n");
                if (createdProperties != null) {
                    createdProperties.put(markerNameClass, phProps.get(markerNameClass));
                }
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
                logger.fine("Getting Property Info for" + someObj.getClass().getName());

                PropertyConsumer pc = (PropertyConsumer) someObj;
                componentProperties.clear();
                markerName = pc.getPropertyPrefix();

                if (ph != null && markerName != null && !markerName.equals("openmap")) {
                    // Gets the properties for the prefix that the
                    // property handler was set with. This should
                    // handle components that aren't good
                    // PropertyConsumers.
                    componentProperties = ph.getProperties(markerName);
                } else {
                    componentProperties.clear();
                }

                if (!componentListBuilt) {
                    if (markerName != null) {
                        componentMarkerString.append(" ").append(markerName);
                    } else {
                        markerName = "component" + (numComponents++);
                        componentMarkerString.append(" ").append(markerName);
                        pc.setPropertyPrefix(markerName);
                    }

                    componentPropsString.append(markerName).append(".class=").append(someObj.getClass().getName()).append("\n");

                    if (createdProperties != null) {
                        createdProperties.put(markerName, someObj.getClass().getName());
                    }
                }

                pc.getProperties(componentProperties);

                TreeMap orderedProperties = new TreeMap(componentProperties);

                if (!componentProperties.isEmpty()) {
                    componentPropsString.append("####\n");
                    for (Iterator keys = orderedProperties.keySet().iterator(); keys.hasNext();) {
                        String key = (String) keys.next();
                        String value = componentProperties.getProperty(key);

                        if (value != null) {
                            componentPropsString.append(key).append("=").append(value).append("\n");
                        }

                        if (createdProperties != null && value != null) {
                            createdProperties.put(key, value);
                        }
                    }
                }
            } else if (!componentListBuilt) {
                markerName = "component" + (numComponents++);
                componentMarkerString.append(" ").append(markerName);
                componentPropsString.append(markerName).append(".class=").append(someObj.getClass().getName()).append("\n");
                if (createdProperties != null) {
                    createdProperties.put(markerName, someObj.getClass().getName());
                }
            }
        }

        if (ps != null) {
            ps.println("\n\n### OpenMap Components ###");
            ps.println(componentMarkerString.toString());

            ps.println("\n### OpenMap Component Properties ###");
            // list created, add the actual component properties
            ps.println(componentPropsString.toString());
            ps.println("### End Component Properties ###");
        }

        if (createdProperties != null) {
            createdProperties.put(PropertyHandler.componentProperty,
                                  componentMarkerString.substring(PropertyHandler.componentProperty.length() + 1));
        }
    }

    /**
     * A helper function to createOpenMapProperties that gets the current
     * properties of the layers in the LayerHandler and prints them out to the
     * PrintStream and the provided Properties object.
     * 
     * @param layerHandler LayerHandler to get layers from.
     * @param ph PropertyHandler that may have properties to use as a foundation
     *        for the properties for the components. If the component can't
     *        provide properties reflecting its settings, the property handler
     *        will be consulted for properties it knows about for that
     *        component.
     * @param ps PrintStream to write properties to, may be null.
     * @param createdProperties Properties object to store properties in, may be
     *        null.
     */
    protected static void printLayerProperties(LayerHandler layerHandler, PropertyHandler ph, PrintStream ps,
                                               Properties createdProperties) {

        // Keep track of the LayerHandler. Use it to get the layers,
        // which can be used to get all the marker names for the
        // openmap.layers property. The visible layers go to the
        // openmap.startUpLayers property. Then, cycle through all
        // the layers to get their properties, since they all are
        // PropertyConsumers.
        String markerName;

        String layerMarkerStringKey = Environment.OpenMapPrefix + "." + LayerHandler.layersProperty;

        StringBuffer layerMarkerString = new StringBuffer(layerMarkerStringKey).append("=");

        String startUpLayerMarkerStringKey = Environment.OpenMapPrefix + "." + LayerHandler.startUpLayersProperty;

        StringBuffer startUpLayerMarkerString = new StringBuffer(startUpLayerMarkerStringKey).append("=");

        StringBuffer layerPropertiesString = new StringBuffer();

        Properties layerProperties = new Properties();

        Layer[] layers = layerHandler.getLayers();
        int numLayers = 0;

        for (int i = 0; i < layers.length; i++) {

            markerName = layers[i].getPropertyPrefix();

            if (markerName == null) {
                markerName = "layer" + (numLayers++);
                layers[i].setPropertyPrefix(markerName);
            }

            if (ph != null) {
                // Gets the properties for the prefix that the
                // property handler was set with. This should
                // handle components that aren't good
                // PropertyConsumers.
                layerProperties = ph.getProperties(markerName);
            } else {
                layerProperties.clear();
            }

            layerMarkerString.append(" ").append(markerName);

            if (layers[i].isVisible()) {
                startUpLayerMarkerString.append(" ").append(markerName);
            }

            layers[i].getProperties(layerProperties);
            layerPropertiesString.append("### -").append(markerName).append("- layer properties\n");

            TreeMap orderedProperties = new TreeMap(layerProperties);
            for (Iterator keys = orderedProperties.keySet().iterator(); keys.hasNext();) {
                String key = (String) keys.next();
                // Could add .replace("\\", "/") to the end of this
                // line to prevent \\ from appearing in the properties
                // file.
                String value = layerProperties.getProperty(key);

                if (value != null) {
                    layerPropertiesString.append(key).append("=").append(value).append("\n");
                }

                if (createdProperties != null && value != null) {
                    createdProperties.put(key, value);
                }
            }

            layerPropertiesString.append("### end of -").append(markerName).append("- properties\n\n");
        }

        if (ps != null) {
            ps.println("\n### OpenMap Layers ###");
            ps.println(layerMarkerString.toString());
            ps.println(startUpLayerMarkerString.toString());
            ps.println(layerPropertiesString.toString());
        }

        if (createdProperties != null) {
            createdProperties.put(layerMarkerStringKey, layerMarkerString.substring(layerMarkerStringKey.length() + 1));
            createdProperties.put(startUpLayerMarkerStringKey,
                                  startUpLayerMarkerString.substring(startUpLayerMarkerStringKey.length() + 1));
        }
    }

    /**
     * Given a MapHandler and a Java Properties object, the LayerHandler will be
     * cleared of it's current layers, and reloaded with the layers in the
     * properties. The MapBean will be set to the projection settings listed in
     * the properties.
     */
    public void loadProjectionAndLayers(MapHandler mapHandler, Properties props) {

        MapBean mapBean = (MapBean) mapHandler.get("com.bbn.openmap.MapBean");
        LayerHandler layerHandler = (LayerHandler) mapHandler.get("com.bbn.openmap.LayerHandler");
        // InformationDelegator id = (InformationDelegator)
        // mapHandler.get("com.bbn.openmap.InformationDelegator");

        // if (id != null) {
        // id.requestCursor(new Cursor(Cursor.WAIT_CURSOR));
        // }

        if (layerHandler != null) {
            layerHandler.removeAll();
            layerHandler.init(Environment.OpenMapPrefix, props);
        } else {
            logger.warning("Can't load new layers - can't find LayerHandler");
        }

        if (mapBean != null) {
            mapBean.setProjection(mapBean.getProjectionFactory().getDefaultProjectionFromEnvironment(Environment.getInstance(),
                                                                                                     mapBean.getWidth(),
                                                                                                     mapBean.getHeight()));
        } else {
            logger.warning("Can't load new projection - can't find MapBean");
        }

        // if (id != null) {
        // id.requestCursor(null);
        // }
    }

    /**
     * If you are creating a new object, it's important to get a unique prefix
     * for its properties. This function takes a prefix string and checks it
     * against all others it knows about. If there is a conflict, it adds a
     * number to the end until it becomes unique. This prefix will be logged by
     * the PropertyHandler as a name given out, so duplicate instances of that
     * string will not be given out later. It doesn't, however, log that name in
     * the prefixLibrarian. That only occurs when the object is programmatically
     * registered with the prefixLibrarian or when the PropertyHandler finds
     * that object in the MapHandler (and even then that object must be a
     * PropertyConsumer to be registered this way).
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
     * Changes ' ' characters to '_', and then tries to add it to the used
     * prefix list. Returns true if successful.
     */
    public boolean addUsedPrefix(String prefix) {
        prefix = prefix.replace(' ', '_');

        return usedPrefixes.add(prefix.intern());
    }

    /**
     * Changes ' ' characters to '_', and then tries to remove it to the used
     * prefix list. Returns true if successful.
     */
    public boolean removeUsedPrefix(String prefix) {
        prefix = prefix.replace(' ', '_');

        return usedPrefixes.remove(prefix.intern());
    }

    /**
     * Add a ProgressListener that will display build progress.
     */
    public void addProgressListener(ProgressListener list) {
        getProgressSupport().add(list);
    }

    /**
     * Remove a ProgressListener that displayed build progress.
     */
    public void removeProgressListener(ProgressListener list) {
        getProgressSupport().remove(list);
    }

    /**
     * Clear all progress listeners.
     */
    public void clearProgressListeners() {
        getProgressSupport().clear();
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
     * 
     * @param frameNumber the current frame count
     * @param totalFrames the total number of frames.
     */
    protected void fireProgressUpdate(int type, String task, int frameNumber, int totalFrames) {
        if (updateProgress) {
            getProgressSupport().fireUpdate(type, task, totalFrames, frameNumber);
        } else if (type == ProgressEvent.DONE) {
            // At least turn off progress listeners if they are up.
            getProgressSupport().fireUpdate(ProgressEvent.DONE, task, totalFrames, frameNumber);
        }
    }

    /**
     * Set a flag that will trigger the PropertyHandler to fire progress events
     * when it is going through the creation process.
     */
    public void setUpdateProgress(boolean set) {
        updateProgress = set;
    }

    public boolean getUpdateProgress() {
        return updateProgress;
    }

    // Property Functions:
    // ///////////////////

    /**
     * Remove an existing property if it exists.
     * 
     * @return true if a property was actually removed.
     */
    public boolean removeProperty(String property) {
        return getProperties().remove(property) != null;
    }

    /**
     * Add (or overwrite) a property to the current properties
     */
    public void addProperty(String property, String value) {
        getProperties().setProperty(property, value);
    }

    /**
     * Add in the properties from the given URL. Any existing properties will be
     * overwritten except for openmap.components, openmap.layers and
     * openmap.startUpLayers which will be appended.
     */
    public void addProperties(URL urlToProperties) {
        addProperties(fetchProperties(urlToProperties));
    }

    /**
     * Add in the properties from the given source, which can be a resource,
     * file or URL. Any existing properties will be overwritten except for
     * openmap.components, openmap.layers and openmap.startUpLayers which will
     * be appended.
     * 
     * @throws MalformedURLException if propFile doesn't resolve properly.
     */
    public void addProperties(String propFile)
            throws MalformedURLException {
        addProperties(fetchProperties(PropUtils.getResourceOrFileOrURL(propFile)));
    }

    /**
     * Add in the properties from the given Properties object. Any existing
     * properties will be overwritten except for openmap.components,
     * openmap.layers and openmap.startUpLayers where values will be prepended
     * to any existing lists.
     */
    public void addProperties(Properties p) {
        String[] specialProps = new String[] {
            Environment.OpenMapPrefix + "." + LayerHandler.layersProperty,
            Environment.OpenMapPrefix + "." + LayerHandler.startUpLayersProperty,
            componentProperty
        };

        Properties tmp = new Properties();
        tmp.putAll(p);

        for (int i = 0; i < specialProps.length; i++) {
            prependProperty(specialProps[i], tmp);
            tmp.remove(specialProps[i]);
        }

        getProperties().putAll(tmp);
    }

    /**
     * remove a marker from a space delimited set of properties.
     */
    public void removeMarker(String property, String marker) {
        // Requires jdk 1.4
        // StringBuffer sb =
        // new StringBuffer(getProperties().getProperty(property,
        // ""));
        // int idx = sb.indexOf(marker);

        // jdk 1.3 version
        String propertyString = getProperties().getProperty(property, "");
        int idx = propertyString.indexOf(marker);
        if (idx != -1) {
            StringBuffer sb = new StringBuffer(propertyString);
            sb.delete(idx, idx + marker.length());
            getProperties().setProperty(property, sb.toString());
        }
    }

    /**
     * Append the given property into the current properties
     */
    public void appendProperty(String property, Properties src) {
        appendProperty(property, src.getProperty(property, ""));
    }

    /**
     * Append the given property into the current properties
     */
    public void appendProperty(String property, String value) {
        String curVal = getProperties().getProperty(property, "");
        getProperties().setProperty(property, curVal + " " + value);
    }

    /**
     * Prepend the given property into the current properties
     */
    public void prependProperty(String property, Properties src) {
        prependProperty(property, src.getProperty(property, ""));
    }

    /**
     * Prepend the given property into the current properties
     */
    public void prependProperty(String property, String value) {
        String curVal = getProperties().getProperty(property, "");
        getProperties().setProperty(property, value + " " + curVal);
    }

    /**
     * Load a Properties object from the classpath. The method always returns a
     * <code>Properties</code> object. If there was an error loading the
     * properties from <code>propsURL</code>, an empty <code>Properties</code>
     * object is returned.
     * 
     * @param propsURL the URL of the properties to be loaded
     * @return the loaded properties, or an empty Properties object if there was
     *         an error.
     */
    public static Properties fetchProperties(URL propsURL) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("checking (" + propsURL + ")");
        }
        Properties p = new Properties();
        if (propsURL != null) {
            try {
                InputStream is = propsURL.openStream();
                p.load(is);
                is.close();
            } catch (IOException e) {
                logger.warning("Exception reading map properties at " + propsURL + ": " + e);
            }
        }
        return p;
    }

    /**
     * All the PropertyHandler does with the MapHandler is look for
     * PropertyConsumers and register their prefixes with the prefixLibarian.
     */
    public void findAndInit(Object obj) {
        if (obj instanceof PropertyConsumer) {
            String prefix = ((PropertyConsumer) obj).getPropertyPrefix();
            if (prefix != null) {
                getPrefixLibrarian().put(prefix, obj);
            }
        }
    }

    public void findAndUndo(Object obj) {
        if (obj instanceof PropertyConsumer) {
            String prefix = ((PropertyConsumer) obj).getPropertyPrefix();
            if (prefix != null) {
                getPrefixLibrarian().remove(prefix);
            }
        }
        if (obj == this) {
            dispose();
        }
    }

    public void dispose() {
        if (prefixLibrarian != null) {
            prefixLibrarian.clear();
        }
        if (properties != null) {
            properties.clear();
        }
        if (usedPrefixes != null) {
            usedPrefixes.clear();
        }
    }

    /**
     * 
     * This Builder class lets you have more control over how a PropertyHandler
     * is constructed. If a properties file location or a properties file is not
     * provided, the PropertyHandler will look for an "openmap.properties" file
     * in the classpath, config directory or user home directory. If you don't
     * want the PropertyHandler to search for a properties file, set an empty
     * Properties object in the Builder.
     * 
     * @author ddietrick
     */
    public static class Builder {

        protected boolean update = false;
        protected Properties properties = null;
        protected String propertyPrefix = null;

        public Builder() {
        }

        /**
         * Have the builder look for a resource, file or URL at the location.
         * 
         * @param location of the properties file
         * @return this Builder, so settings can be stacked.
         * @throws MalformedURLException
         * @throws IOException
         */
        public Builder setPropertiesFile(String location)
                throws MalformedURLException, IOException {
            if (location != null) {
                this.properties = createProperties(PropUtils.getResourceOrFileOrURL(location));
            }
            return this;
        }

        /**
         * Have the builder look for properties file at URL location.
         * 
         * @param url
         * @return this Builder for stacking.
         * @throws IOException
         */
        public Builder setPropertiesFile(URL url)
                throws IOException {
            if (url != null) {
                this.properties = createProperties(url);
            }
            return this;
        }

        /**
         * Have the builder use the provided properties.
         * 
         * @param props Properties to use.
         * @return this Builder for stacking
         */
        public Builder setProperties(Properties props) {
            this.properties = props;
            return this;
        }

        /**
         * Set the property prefix used for general settings in the properties
         * in configuration of application. If not set "openmap" will be used as
         * the default.
         * 
         * @param prefix Set the property prefix for the PropertyHandler
         * @return this builder for stacking
         */
        public Builder setPropertyPrefix(String prefix) {
            this.propertyPrefix = prefix;
            return this;
        }

        /**
         * 
         * @param update flag for providing progress updates
         * @return This builder for stacking
         */
        public Builder setProgressUpdates(boolean update) {
            this.update = update;
            return this;
        }

        /**
         * Reads the file at the given location and creates a Properties file
         * from the contents.
         * 
         * @param location URL of file
         * @return Properties
         * @throws IOException if something goes wrong reading the file.
         */
        protected Properties createProperties(URL location)
                throws IOException {
            InputStream is = null;

            if (location != null) {
                // Open URL to read in properties
                is = location.openStream();
            }

            Properties tmpProperties = new Properties();
            if (is != null) {
                tmpProperties.load(is);
            }

            return tmpProperties;
        }
    }
}
