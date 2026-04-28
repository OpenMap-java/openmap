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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/Environment.java,v $
// $RCSfile: Environment.java,v $
// $Revision: 1.9 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap;

import java.applet.Applet;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JApplet;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;

import com.bbn.openmap.util.BasicI18n;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.I18n;
import com.bbn.openmap.util.PropUtils;

/**
 * An Environment is a set of property lists that together specify the runtime
 * environment for this process.
 * <p>
 * There is only one Environment, and it is accessed through static member
 * functions.
 * <p>
 * The environment is comprised of at least three property lists:
 * <ul>
 * <li>System properties form the basis of the environment. For applications,
 * This can be the System.getProperties(), or something else. In the case of
 * applets, this is a list of the ten system properties that applets are allowed
 * to access plus any applet parameters.
 * <p>
 * <li>Runtime properties are checked next. These are properties that exist only
 * while the process is running. They are calculated during Environment
 * initialization and are not persistent.
 * <p>
 * <li>Hardcoded properties form the last level. These properties are hardcoded
 * default values that are specified in the code of this file. They are meant to
 * be used as a last resort.
 * <p>
 * </ul>
 * When <code>Environment.get()</code> is called, all lists are searched until
 * the property is found. System properties are searched first, then runtime
 * properties, and then hardcoded properties.
 * <p>
 * This search pattern allows system properties and properties specified as
 * applet parameters or command line properties (using -DProperty=value Java
 * flag) to override more hardcoded properties specified elsewhere, say in a
 * user preferences file.
 * 
 * @see java.util.Properties
 * 
 *      <pre>
 * 
 * 
 * 
 *    # metanames of the names of the variables used in the OpenMap
 *    # system to configure the Environment class from the properties.
 *    openmap.Title - String for title of application window.
 *    openmap.Version - Version number of application.
 *    openmap.BuildDate - Build data of code base.
 *    openmap.WebBrowser - The launch command to launch a browser.
 *    openmap.TempDirectory - A path to a directory to use for temporary files.
 *    openmap.UseInternalFrames - For an application, to direct it to use InternalFrames for other windows.
 *    openmap.Latitude - Starting latitude for map projection.
 *    openmap.Longitude - Starting longitude for map projection.
 *    openmap.Scale - Starting scale for map projection.
 *    openmap.Projection - Starting projection type for map projection.
 *    openmap.Width - Pixel width for map.
 *    openmap.Height - Pixel height for map.
 *    openmap.HelpURL - The URL to use for OpenMap/Application help pages.
 *    openmap.BackgroundColor - An ARGB integer to use for the background (sea) color.
 *    openmap.Debug - Debug tokens to activate for printout.  @see com.bbn.openmap.util.Debug
 *   
 *    openmap.UniqueID String for unique identification of OpenMap instance (calculated)
 * 
 * 
 * 
 *      </pre>
 */
public class Environment extends Properties {

	/*--------------------------------------------------
	 * To Do:
	 *
	 * Save user properties
	 *
	 * User properties editor
	 *
	 *--------------------------------------------------*/

	private static final long serialVersionUID = 1L;
	protected static Environment env;
	protected Properties hardcodedProps;
	protected Properties runtimeProps;
	protected JLayeredPane desktop = null;
	protected Applet applet;
	List<String> extraPaths = new ArrayList<>();

	private static int counter = 0;

	// user preferences file (used for later references)
	public static transient final String OpenMapPrefix = "openmap";
	public static transient final String PreferencesURL = OpenMapPrefix + ".PreferencesURL";
	// metanames of the names of the variables used in the OpenMap
	// system
	public static transient final String Title = OpenMapPrefix + ".Title";
	public static transient final String Version = OpenMapPrefix + ".Version";
	public static transient final String BuildDate = OpenMapPrefix + ".BuildDate";
	public static transient final String UniqueID = OpenMapPrefix + ".UniqueID";
	public static transient final String WebBrowser = OpenMapPrefix + ".WebBrowser";
	public static transient final String TmpDir = OpenMapPrefix + ".TempDirectory";
	public static transient final String UseInternalFrames = OpenMapPrefix + ".UseInternalFrames";
	public static transient final String Latitude = OpenMapPrefix + ".Latitude";
	public static transient final String Longitude = OpenMapPrefix + ".Longitude";
	public static transient final String Scale = OpenMapPrefix + ".Scale";
	public static transient final String Projection = OpenMapPrefix + ".Projection";
	public static transient final String Width = OpenMapPrefix + ".Width";
	public static transient final String Height = OpenMapPrefix + ".Height";
	public static transient final String HelpURL = OpenMapPrefix + ".HelpURL";
	public static transient final String BackgroundColor = OpenMapPrefix + ".BackgroundColor";
	public static transient final String DebugList = OpenMapPrefix + ".Debug";
	// default to false
	private transient boolean isXWindows = false;
	// Will do it if isXWindows
	public transient boolean doingXWindowsWorkaround = false;
	public final transient String title = "$$Title=" + MapBean.title;
	public final transient String version = "$$Version=" + MapBean.version;
	// autobuild should set this.
	public final transient String build = "$$BuildDate=";

	public final transient String ThreadPool = OpenMapPrefix + ".ThreadPool";

	static {
		env = new Environment();
	}

	/**
	 * Hardcoded default properties.
	 * <p>
	 * These should be edited before each new version/installation of OpenMap. They
	 * are declared in such a way that they can be easily edited from a build
	 * script.
	 * 
	 * @param p Properties
	 */
	protected final void initHardCodedProperties(Properties p) {

		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(title, "=");
		tokenizer.nextToken();
		p.put(Title, tokenizer.nextToken());

		tokenizer = new StringTokenizer(version, "=");
		tokenizer.nextToken();
		p.put(Version, tokenizer.nextToken());

		tokenizer = new StringTokenizer(build, "=");
		tokenizer.nextToken();
		try {
			p.put(BuildDate, tokenizer.nextToken());
		} catch (NoSuchElementException e) {
		} // no BuildDate
	}

	/**
	 * Initializes the environment of an applet.
	 * 
	 * @param applet An applet
	 * @see java.applet.Applet
	 */
	public static void init(Applet applt) {
		// overwrite properties
		Debug.output("Reinitializing Applet Environment!");
		init(System.getProperties());
		setApplet(applt);

		if (Debug.debugging("env")) {
			env.list(System.out);
		}
	}

	/**
	 * Initializes, or re-initializes the environment of an application.
	 * 
	 * @param sysProps Runtime/System Properties (Top-level)
	 */
	public static void init(Properties sysProps) {
		if (env != null) {
			// Debug.output("Reinitializing Environment!");
			// overwrite properties
			env.installProps(sysProps);
		} else {
			new Environment(sysProps);
			if (Debug.debugging("env")) {
				env.list(System.out);
			}
		}
	}

	/**
	 * Initializes the OpenMap environment. Installs the default System Properties
	 * into the Environment.
	 */
	public static void init() {
		init(System.getProperties());
	}

	protected Environment() {

	}

	/**
	 * Creates an Environment based on applet properties.
	 * 
	 * @param applet an Applet
	 */
	protected Environment(Applet applet) {
		env = this;
		setApplet(applet);
		commonInit();
	}

	/**
	 * Creates an Environment with the specified system properties.
	 * 
	 * @param sysProps system properties
	 */
	protected Environment(Properties sysProps) {
		env = this;
		installProps(sysProps);
		commonInit();
	}

	/**
	 * install Properties directly into the top-level Environment list.
	 * 
	 * @param sysProps system properties
	 * 
	 */
	private void installProps(Properties sysProps) {
		// Copy the specified property list
		for (String key : sysProps.stringPropertyNames()) {
			try {
				this.put(key, sysProps.getProperty(key));
			} catch (NullPointerException ex) {
				// Key or Value must have been null. C'est la vie.
			}
		}
	}

	/**
	 * Adds runtime, user, and base properties to the Environment.
	 */
	protected void commonInit() {
		hardcodedProps = new Properties();
		runtimeProps = new Properties(hardcodedProps);

		// the Environment defaults are the properties lists just
		// constructed
		defaults = runtimeProps;

		initHardCodedProperties(hardcodedProps);
		initRuntimeProperties(runtimeProps);
	}

	/**
	 * Populates the system properties for an applet. Currently this property list
	 * contains the ten system properties available to applets and any applet
	 * parameters specified in Applet.getParameterInfo().
	 * 
	 * @param applet the applet
	 * @see java.applet.Applet#getParameterInfo
	 */
	protected void setAppletProperties(Applet applet) {
		/*
		 * These are the ten properties available to applets.
		 */
		final String[] appletProps = { "java.version", "java.vendor", "java.vendor.url", "java.class.version",
				"os.name", "os.arch", "os.version", "file.separator", "path.separator", "line.separator" };

		int i;

		for (i = 0; i < appletProps.length; i++) {
			String prop = appletProps[i];
			put(prop, System.getProperty(prop));
		}

		String[][] pinfo = applet.getParameterInfo();
		if (pinfo == null)
			return;
		for (i = 0; i < pinfo.length; i++) {
			try {
				String key = pinfo[i][0];
				String value = applet.getParameter(key);
				Debug.message("env", "Applet Parameter " + key + " has value " + value);
				put(key, value);
			} catch (NullPointerException e) {
			}
		}
	}

	/**
	 * Initializes the runtime properties list. Runtime properties are those
	 * properties that exist only while the program is running. They are not
	 * persistent. Persistent properties should be stored in the user properties
	 * list.
	 * 
	 * @param p The runtime properties list
	 */
	protected void initRuntimeProperties(Properties p) {
		if (isApplet()) {
			p.put("user.name", "appletUser");// for convenience
		}

		java.net.InetAddress addr = null;
		try {
			addr = java.net.InetAddress.getLocalHost();
		} catch (NullPointerException npe) {
			// Linux threw a npe when unconnected.
			Debug.output("Environment.init: Can't get hostname from InetAddress!");
		} catch (java.net.UnknownHostException e) {
			Debug.output("Environment.init: I don't know my hostname!");
		} catch (IndexOutOfBoundsException ioobe) {
			// Caught something weird here a couple of times when
			// running unconnected.
			Debug.output("Environment.init: network may not be available");
		}

		// the UniqueID is generated from other runtime values. This
		// should
		// be unique for this Java VM. Note that for security reasons,
		// you
		// might not want to ship around this value on a network
		// because it
		// details some interesting tidbits about the running
		// application.
		p.put(UniqueID,
				"_" + get("user.name") + "_" + get(Version) + "_" + get("os.arch") + "_"
						+ get("os.name") + "_" + ((addr != null) ? addr.getHostName() : "nohost") + "_"
						+ timestamp() + "_");

		// determine window system (for HACKing around
		// Java-under-XWindows
		// polygon wraparound bug.
		String osname = get("os.name");

		if (osname == null) {
			isXWindows = false;
			doingXWindowsWorkaround = false;
			Debug.message("env", "Environment: is applet, Web Start.");
			return;
		}

		if (osname.equalsIgnoreCase("solaris") || osname.equalsIgnoreCase("SunOS")) {
			isXWindows = true;
			doingXWindowsWorkaround = true;
			Debug.message("env", "Environment: is X Windows!");
		} else if (osname.equalsIgnoreCase("linux")) {
			isXWindows = true;
			doingXWindowsWorkaround = true;
			Debug.message("env", "Environment: is X Windows!");
		} else if (osname.startsWith("Windows")) {
			isXWindows = false;
			doingXWindowsWorkaround = false;
			isXWindows = true;
			doingXWindowsWorkaround = true;
			Debug.message("env", "Environment: is MS Windows!");
		} else if (osname.equalsIgnoreCase("Mac OS")) {
			isXWindows = false;
			doingXWindowsWorkaround = false;
			Debug.message("env", "Environment: is Mac OS!");
		} else if (osname.equalsIgnoreCase("Mac OS X")) {
			isXWindows = true;
			doingXWindowsWorkaround = true;
			// isXWindows = false;
			// doingXWindowsWorkaround = false;
			com.bbn.openmap.omGraphics.DrawingAttributes.alwaysSetTextToBlack = true;
			Debug.message("env", "Environment: Excellent! Mac OS X!");
		} else {
			System.err.println("Environment.initRuntimeProperties(): " + "running on unknown/untested OS: " + osname);
		}

		// should have initialized user properties already
		if (get(OpenMapPrefix + ".noXWindowsWorkaround") != null) {
			Debug.message("env", "Environment.initRuntimeProperties(): " + "not working around XWindows clipping bug.");
			doingXWindowsWorkaround = false;
		}
	}

	/**
	 * Indicates whether the current process is an applet.
	 * 
	 * @return <code>true</code> if process is an applet; <code>false</code>
	 *         otherwise.
	 */
	public static boolean isApplet() {
		return (env.applet != null);
	}

	protected static void setApplet(Applet applet) {
		env.applet = applet;
		if (applet instanceof JApplet) {
			useInternalFrames(((JApplet) applet).getRootPane());
		}
		env.setAppletProperties(applet);
	}

	/**
	 * Indicates whether the current process is an application.
	 * 
	 * @return <code>true</code> if process is an application; <code>false</code>
	 *         otherwise.
	 */
	public static boolean isApplication() {
		return (env.applet == null);
	}

	/**
	 * Store a value in the Environment.
	 * 
	 * @param propertyKey
	 * @param value
	 */
	public static void set(String propertyKey, Object value) {
		env.put(propertyKey, value);
	}
	
	/**
	 * Searches for the named property in the environment. If the key is not found,
	 * null is returned. All three property lists, runtime, user, and system are
	 * searched in that order.
	 * 
	 * @param key the property key
	 * @return the value of the property with the specified key or <code>null</code>
	 *         if there is no property with that key.
	 */
	public static String get(String key) {
		return env.getProperty(key, null);
	}

	/**
	 * Searches for a named property in the environment.
	 * 
	 * @param key the key for the property
	 * @param dflt the default value returned if the key is not found
	 * @return the value for the key or the default.
	 */
	public static String get(String key, String dflt) {
		return env.getProperty(key, dflt);
	}
	
	/**
	 * Gets a boolean value out of the Environment.
	 * 
	 * @param key the property key
	 * @return the boolean value of the property or false if there is no property
	 *         with that key
	 * 
	 */
	public static boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	/**
	 * Gets a boolean value out of the Environment.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the boolean value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public static boolean getBoolean(String key, boolean defaultValue) {
		String str = env.getProperty(key, null);
		if (str == null) {
			return defaultValue;
		}

		return (Boolean.valueOf(str)).booleanValue();
	}

	/**
	 * Gets an integer value out of the Environment.
	 * 
	 * @param key the property key
	 * @return the integer value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public static int getInteger(String key) {
		return getInteger(key, Integer.MIN_VALUE, 10);
	}

	/**
	 * Gets an integer value out of the Environment.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the integer value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public static int getInteger(String key, int defaultValue) {
		return getInteger(key, defaultValue, 10);
	}

	/**
	 * Gets an integer value out of the Environment.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @param radix        base value
	 * @return the integer value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public static int getInteger(String key, int defaultValue, int radix) {
		String str = env.getProperty(key, null);
		if (str == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(str, radix);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Gets a float value out of the Environment.
	 * 
	 * @param key the property key
	 * @return the float value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public static float getFloat(String key) {
		return getFloat(key, Float.NaN);
	}

	/**
	 * Gets a float value out of the Environment.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the float value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public static float getFloat(String key, float defaultValue) {
		String str = env.getProperty(key, null);
		if (str == null) {
			return defaultValue;
		}

		try {
			return Float.valueOf(str).floatValue();
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Gets a double value out of the Environment.
	 * 
	 * @param key the property key
	 * @return the double value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public double getDouble(String key) {
		return getDouble(key, Double.NaN);
	}

	/**
	 * Gets a double value out of the Environment.
	 * 
	 * @param key          the property key
	 * @param defaultValue a default value
	 * @return the double value of the property or defaultValue if there is no
	 *         property with that key
	 * 
	 */
	public static double getDouble(String key, double defaultValue) {
		String str = env.getProperty(key, null);
		if (str == null) {
			return defaultValue;
		}

		try {
			return Double.valueOf(str).doubleValue();
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Gets the applet associated with this process.
	 * 
	 * @return the applet, or null if process is an application
	 */
	public static Applet getApplet() {
		return env.applet;
	}

	/**
	 * Adds a key/value pair to the Environment's system properties list.
	 * 
	 * @param key   the key, used later for retrieval
	 * @param value the associate value
	 * @see Environment#get
	 */
	public static void addSystemProperty(String key, String value) {
		env.put(key, value);
	}

	/**
	 * Adds a key/value pair to the Environment's runtime properties list.
	 * 
	 * @param key   the key, used later for retrieval
	 * @param value the associate value
	 * @see Environment#get
	 */
	public static void addRuntimeProperty(String key, String value) {
		env.runtimeProps.put(key, value);
	}

	/**
	 * Returns the toplevel Properties list of the Environment.
	 * 
	 * @return Properties system properties
	 * 
	 */
	public static Properties getProperties() {
		return env;
	}

	/**
	 * Returns a stringified value of the current time.
	 * <p>
	 * Note: you probably don't want to call this in a tight loop.
	 * 
	 * @return String timestamp YYYYMMDDhhmmss
	 * 
	 */
	public static String timestamp() {
		Calendar calendar = Calendar.getInstance();
		return "" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DAY_OF_MONTH)
				+ calendar.get(Calendar.HOUR) + calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND);
	}

	/**
	 * Check if this is an XWindows-based VM.
	 * <p>
	 * Note: this only returns a valid result if the Environment has been
	 * initialized.
	 * <p>
	 * 
	 * @return boolean
	 * 
	 */
	public static final boolean isXWindowSystem() {
		return env.isXWindows;
	}

	/**
	 * Generate a unique string. This should be unique compared to other strings
	 * generated this way.
	 * 
	 * @return String
	 * @see Environment#timestamp
	 */
	public static String generateUniqueString() {
		return get(UniqueID) + (counter++);
	}

	/**
	 * Returns elements of the CLASSPATH that are directories. CLASSPATH elements
	 * that are not directories are not returned.
	 * 
	 * @return Vector of Strings
	 */
	public static final List<String> getClasspathDirs() {
		List<String> v = new ArrayList<>();
		try {
			String classPath = System.getProperty("java.class.path");
			StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);

			while (st.hasMoreTokens()) {
				String path = st.nextToken();
				if ((new File(path)).isDirectory()) {
					v.add(path);
				}
			}
		} catch (java.security.AccessControlException ace) {
			// Running as an applet?!?
		}

		v.addAll(env.extraPaths);
		return v;
	}

	/**
	 * Add a resource path to internal Vector. This path will get added to the
	 * classpath, if the Environment is asked for classpaths.
	 */
	public static void addPathToClasspaths(String path) {
		env.extraPaths.add(path);
	}

	/**
	 * Checks the Environment to see if a BackgroundColor string, set as a hex ARGB
	 * string, has been set. If it hasn't or if it doesn't represent a valid color
	 * number, then null is returned, which should be interpreted as an excuse to
	 * use the default pretty blue embedded in the projection.
	 */
	public static Color getCustomBackgroundColor() {
		String colorRep = get(BackgroundColor);
		if (colorRep == null) {
			return null;
		} else {
			try {
				return PropUtils.parseColor(colorRep);
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
	}

	/**
	 * A method to set the Environment to be able to tell other components to
	 * InternalFrames.
	 * 
	 * @param rootPane to use for the internal frames - the method gets the
	 *                 LayeredPane from the rootPane.
	 */
	public static void useInternalFrames(JRootPane rootPane) {
		if (rootPane != null) {
			env.useInternalFrames(rootPane.getLayeredPane());
		} else {
			env.useInternalFrames((JLayeredPane) null);
		}
	}

	/**
	 * A method to set the Environment to be able to tell other components to
	 * InternalFrames.
	 * 
	 * @param layeredPane to use for the internal frames.
	 */
	public void useInternalFrames(JLayeredPane layeredPane) {
		if (layeredPane != null) {
			desktop = layeredPane;
			desktop.setOpaque(true);
			put(UseInternalFrames, "true");
		} else {
			desktop = null;
			put(UseInternalFrames, "false");
		}
	}

	public static boolean doingXWindowsWorkaround() {
		return env.doingXWindowsWorkaround;
	}
	
	/**
	 * Get the JLayeredPane to use for Internal Frames. May be null if the
	 * Environment hasn't be set with the root pane.
	 */
	public static JLayeredPane getInternalFrameDesktop() {
		return env.desktop;
	}

	private I18n i18n = new BasicI18n();

	/**
	 * Get the Internationalization instance.
	 */
	public static I18n getI18n() {
		return env.i18n;
	}
}
