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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/LayerUtils.java,v $
// $RCSfile: LayerUtils.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util;


/*  Java Core  */
import java.awt.Color;
import java.awt.Paint;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/* OpenMap */
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

/** 
 * A Class that provides some static methods useful for generic tasks
 * within the layer, like changing a single string of File.separator
 * paths into an array of Strings, and creating java.awt.Colors from a
 * hex string.
 */
public class LayerUtils {

    /**  
     * Takes a string of `;' separated paths and returns an array of
     * parsed strings.
     * NOTE: this method currently doesn't support appropriate quoting
     * of the `;' character, although it probably should...
     * @param p properties
     * @param propName the name of the property
     * @return Array of strings representing paths.
     */
    public static String[] initPathsFromProperties(Properties p, 
						   String propName) {
	return stringArrayFromProperties(p, propName, ";");
    }

    /** 
     * Gets an integer out of a properties object.  Returns the
     * default value if something goes wrong.
     *
     * @param p properties
     * @param propName name of the property associated with the wanted
     * value.
     * @param defaultValue what to return if the property name doesn't
     * exist, or if the value isn't a numerical value.
     * @return integer value associated with the property.
     */
    public static int intFromProperties(Properties p, 
					String propName,
					int defaultValue) {
	int ret = defaultValue;
	String intString = p.getProperty(propName);

	if (intString != null) {
	    try {
		ret = Integer.parseInt(intString);
	    } catch (NumberFormatException e) {
		ret = defaultValue;
	    }
	}
	return ret;
    }

    /** 
     * Gets an float out of a properties object.  Returns the
     * default value if something goes wrong.
     *
     * @param p properties
     * @param propName name of the property associated with the wanted
     * value.
     * @param defaultValue what to return if the property name doesn't
     * exist, or if the value isn't a numerical value.
     * @return float value associated with the property.
     */
    public static float floatFromProperties(Properties p, 
					    String propName,
					    float defaultValue) {
	float ret = defaultValue;
	String floatString = p.getProperty(propName);

	if (floatString != null) {
	    try {
		ret = Float.valueOf(floatString).floatValue();
	    } catch (NumberFormatException e) {
		ret = defaultValue;
	    }
	}
	return ret;
    }

    /** 
     * Gets an boolean out of a properties object.  Returns the
     * default value if something goes wrong.
     *
     * @param p properties
     * @param propName name of the property associated with the wanted
     * value.
     * @param defaultValue what to return if the property name doesn't
     * exist, or if the value isn't a numerical value.
     * @return boolean value associated with the property.
     */
    public static boolean booleanFromProperties(Properties p, 
						String propName,
						boolean defaultValue) {
	boolean ret = defaultValue;
	String booleanString = p.getProperty(propName);
	if (booleanString != null) {
	    ret = booleanString.toLowerCase().equals("true");
	}
	
	return ret;
    }

    /**
     * Creates an object out of a property name.  If anything fails,
     * return null.
     *
     * @param p properties
     * @param propName name of class to instantiate.
     * @return null on failure, otherwise, a default constructed instance
     * of the class named in the property.
     */
    public static Object objectFromProperties(Properties p, 
					      String propName) {

	Object ret = null;
	String objectName = p.getProperty(propName);
	if (objectName != null) {
	    try {
		ret = Class.forName(objectName).newInstance();// Works for applet!
		//ret = java.beans.Beans.instantiate(null, objectName);
	    } catch (java.lang.InstantiationException e) {
		ret = null;
	    } catch (java.lang.IllegalAccessException e) {
		ret = null;
	    } catch (java.lang.ClassNotFoundException e) {
		ret = null;
	    }
	}
	return ret;
    }

    /**  
     * Takes a string of representing token separated properties and
     * returns an array of parsed strings.
     * NOTE: this method currently doesn't support appropriate quoting
     * of the token, although it probably should...
     * @param p properties
     * @param propName the name of the property
     * @param tok the characters separating the strings.
     * @return Array of strings between the tokens.
     */
    public static String[] stringArrayFromProperties(Properties p, 
						     String propName, 
						     String tok) {

	String[] ret = null;
	String raw = p.getProperty(propName);

	if (raw != null) {

	    try {
		StringTokenizer token = new StringTokenizer(raw, tok);
		int numPaths = token.countTokens();
		
		ret = new String[numPaths];
		for (int i = 0; i < numPaths; i++) {
		    ret[i] = token.nextToken();
		}		    
		return ret;
	    } catch (java.util.NoSuchElementException e) {
		e.printStackTrace();
	    }
	}
	return ret;
    }

  /**
   * Gets a double out of a properties object.  Returns the default value
   * if something goes wrong.
   *
   * @param p properties
   * @param propName name of the property associated with the wanted value.
   * @param defaultValue what to return if the property name doesn't exist,
   * or if the value isn't a numerical value.
   * @return double value associated with the property.
   */

  public static double doubleFromProperties(Properties p,
					    String propName,
					    double defaultValue) {
    double ret = defaultValue;
    String doubleString = p.getProperty(propName);
    
    if(doubleString != null) {
      try {
	ret = Double.valueOf(doubleString).doubleValue();
      } catch (NumberFormatException e) {
	ret = defaultValue;
      }
    }
    return ret;
  }
      
    /** 
     * Take a string from a properties file, representing the 24bit
     * RGB or 32bit ARGB hex values for a color, and convert it to a
     * java.awt.Color.
     * @param p properties
     * @param propName the name of the property
     * @param dfault color to use if the property value doesn't work
     * @return java.awt.Color
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see ColorFactory#parseColorFromProperties(Properties, String, String, boolean)
     */
    public static Color parseColorFromProperties(Properties p, 
						 String propName, 
						 String dfault)
	throws NumberFormatException
    {
	return ColorFactory.parseColorFromProperties(p, propName, dfault, false);
    }

    /** 
     * Take a string from a properties file, representing the 24bit
     * RGB or 32bit ARGB hex values for a color, and convert it to a
     * java.awt.Color.
     * @param p properties
     * @param propName the name of the property
     * @param dfault color to use if the property value doesn't work
     * @return java.awt.Color
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see ColorFactory#parseColorFromProperties(Properties, String,
     * String, boolean) 
     */
    public static Paint parseColorFromProperties(Properties p, 
						 String propName, 
						 Paint dfault)
	throws NumberFormatException
    {
	return ColorFactory.parseColorFromProperties(p, propName, dfault);
    }

    /**
     * Convert a string representing a 24/32bit hex color value into a
     * Color value.
     * NOTE:
     * <ul>
     * <li>Only 24bit (RGB) java.awt.Color is supported on the JDK 1.1
     * platform.
     * <li>Both 24/32bit (ARGB) java.awt.Color is supported on the Java 2
     * platform.
     * </ul>
     * @param colorString the 24/32bit hex string value (ARGB)
     * @return java.awt.Color (24bit RGB on JDK 1.1, 24/32bit ARGB on
     * JDK1.2)
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see ColorFactory#parseColor(String, boolean)
     */
    public static Color parseColor(String colorString)
	throws NumberFormatException
    {
	return ColorFactory.parseColor(colorString, false);
    }

    /**
     * Converts a properties object to an array of Strings.  The
     * resulting array will consist of alternating key-value
     * strings. 
     *
     * @param props the properties object to convert.
     * @return an array of Strings representing key-value pairs.
     */
    public static String[] getPropertiesAsStringArray(Properties props) {

	int size = props.size();
	String[] ret = new String[size*2]; // key and value
	int count = 0;
	Enumeration things = props.propertyNames();
	while (things.hasMoreElements()) {
	    ret[count] = (String)things.nextElement();
	    ret[count+1] = (String)props.getProperty(ret[count]);
	    count+=2;
	}
	return ret;
    }

    /** 
     * Returns a URL that names either a resource, a local file, or an
     * internet URL.
     * @param askingClass the object asking for the URL.
     * @param name name of the resource, file or URL.
     * @throws java.net.MalformedURLException
     * @return URL
     */
    public static URL getResourceOrFileOrURL(Object askingClass, String name)
	throws java.net.MalformedURLException {
	  
	return getResourceOrFileOrURL(askingClass.getClass(), name);
    }

    /** 
     * Returns a URL that names either a resource, a local file, or an
     * internet URL.
     * @param askingClass the class asking for the URL. Can be null.
     * @param name name of the resource, file or URL.
     * @throws java.net.MalformedURLException
     * @return URL
     */
    public static URL getResourceOrFileOrURL(Class askingClass, String name)
	throws java.net.MalformedURLException {

	boolean DEBUG = Debug.debugging("layerutil");

	if (name == null) {
	    if (DEBUG) Debug.output("LayerUtil.getROFOU(): null file name");
  	    return null;
	}

	URL retval = null;
	if (DEBUG) Debug.output("LayerUtil.getROFOU(): looking for " + name);

	if (askingClass != null) {
	    // First see if we have a resource by that name
	    if (DEBUG) Debug.output("LayerUtil.getROFOU(): checking as resource");

	    retval = askingClass.getResource(name);
	}
	if (retval == null) {
	    // Check the general classpath...
	    if (DEBUG) Debug.output("LayerUtil.getROFOU(): checking in general classpath");
	    retval = Thread.currentThread().getContextClassLoader().getResource(name);
	}
	if (retval == null && !com.bbn.openmap.Environment.isApplet()) {
	    // Check the classpath plus the share directory, which may
	    // be in the openmap.jar file or in the development
	    // environment.
	    if (DEBUG) Debug.output("LayerUtil.getROFOU(): checking with ClassLoader");
	    retval = ClassLoader.getSystemResource("share/" + name);
	}

	// If there was no resource by that name available
	if (retval == null) {
	    if (DEBUG) Debug.output("LayerUtil.getROFOU(): not found as resource");

	    java.io.File file = new java.io.File(name);
	    if (file.exists()) {
		retval = file.toURL();
		if (DEBUG) Debug.output("LayerUtil.getROFOU(): found as file :)");

	    } else {
		// Otherwise treat it as a raw URL.
		if (DEBUG) Debug.output("LayerUtil.getROFOU(): Not a file, checking as URL");
		retval = new URL(name);
		try {
		    java.io.InputStream is = retval.openStream();
		    is.close();
		    if (DEBUG) Debug.output("LayerUtil.getROFOU(): OK as URL :)");
		} catch (java.io.IOException ioe) {
		    retval = null;
		} catch (java.security.AccessControlException ace) {
		    Debug.error("LayerUtils: AccessControlException trying to access " + name);
		    retval = null;
		} catch (Exception e) {
		    Debug.error("LayerUtils: caught exception " + e.getMessage());
		    retval = null;
		}
	    }
	}

	if (DEBUG) {
	    if (retval != null) {
		Debug.output("Resource "+ name + "=" + retval.toString());
	    } else {
		Debug.output("Resource " + name + " can't be found..." );
	    }
	}

	return retval;
    }
}
