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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/LayerUtils.java,v $
// $RCSfile: LayerUtils.java,v $
// $Revision: 1.7 $
// $Date: 2005/08/09 18:56:25 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.util;

/*  Java Core  */
import java.awt.Color;
import java.awt.Paint;
import java.net.URL;
import java.util.Properties;

/* OpenMap */
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * A Class that provides some static methods useful for generic tasks
 * within the layer, like changing a single string of File.separator
 * paths into an array of Strings, and creating java.awt.Colors from a
 * hex string.
 * <P>
 * 
 * To merge all of the functions that deal with handling properties,
 * these methods have been moved to the com.bbn.openmap.util.PropUtils
 * class. If you call these methods, they now simply call the same
 * PropUtils method.
 * 
 * @deprecated Use com.bbn.openmap.util.PropUtils and
 *             com.bbn.openmap.util.ColorFactory instead.
 */
public class LayerUtils {

    /**
     * Takes a string of `;' separated paths and returns an array of
     * parsed strings. NOTE: this method currently doesn't support
     * appropriate quoting of the `;' character, although it probably
     * should...
     * 
     * @param p properties
     * @param propName the name of the property
     * @return Array of strings representing paths.
     */
    public static String[] initPathsFromProperties(Properties p, String propName) {
        return PropUtils.initPathsFromProperties(p, propName);
    }

    /**
     * Gets an integer out of a properties object. Returns the default
     * value if something goes wrong.
     * 
     * @param p properties
     * @param propName name of the property associated with the wanted
     *        value.
     * @param defaultValue what to return if the property name doesn't
     *        exist, or if the value isn't a numerical value.
     * @return integer value associated with the property.
     */
    public static int intFromProperties(Properties p, String propName,
                                        int defaultValue) {
        return PropUtils.intFromProperties(p, propName, defaultValue);
    }

    /**
     * Gets an float out of a properties object. Returns the default
     * value if something goes wrong.
     * 
     * @param p properties
     * @param propName name of the property associated with the wanted
     *        value.
     * @param defaultValue what to return if the property name doesn't
     *        exist, or if the value isn't a numerical value.
     * @return float value associated with the property.
     */
    public static float floatFromProperties(Properties p, String propName,
                                            float defaultValue) {
        return PropUtils.floatFromProperties(p, propName, defaultValue);
    }

    /**
     * Gets an boolean out of a properties object. Returns the default
     * value if something goes wrong.
     * 
     * @param p properties
     * @param propName name of the property associated with the wanted
     *        value.
     * @param defaultValue what to return if the property name doesn't
     *        exist, or if the value isn't a numerical value.
     * @return boolean value associated with the property.
     */
    public static boolean booleanFromProperties(Properties p, String propName,
                                                boolean defaultValue) {
        return PropUtils.booleanFromProperties(p, propName, defaultValue);
    }

    /**
     * Creates an object out of a property name. If anything fails,
     * return null.
     * 
     * @param p properties
     * @param propName name of class to instantiate.
     * @return null on failure, otherwise, a default constructed
     *         instance of the class named in the property.
     */
    public static Object objectFromProperties(Properties p, String propName) {
        return PropUtils.objectFromProperties(p, propName);
    }

    /**
     * Takes a string of representing token separated properties and
     * returns an array of parsed strings. NOTE: this method currently
     * doesn't support appropriate quoting of the token, although it
     * probably should...
     * 
     * @param p properties
     * @param propName the name of the property
     * @param tok the characters separating the strings.
     * @return Array of strings between the tokens.
     */
    public static String[] stringArrayFromProperties(Properties p,
                                                     String propName, String tok) {
        return PropUtils.stringArrayFromProperties(p, propName, tok);
    }

    /**
     * Gets a double out of a properties object. Returns the default
     * value if something goes wrong.
     * 
     * @param p properties
     * @param propName name of the property associated with the wanted
     *        value.
     * @param defaultValue what to return if the property name doesn't
     *        exist, or if the value isn't a numerical value.
     * @return double value associated with the property.
     */

    public static double doubleFromProperties(Properties p, String propName,
                                              double defaultValue) {
        return PropUtils.doubleFromProperties(p, propName, defaultValue);
    }

    /**
     * Take a string from a properties file, representing the 24bit
     * RGB or 32bit ARGB hex values for a color, and convert it to a
     * java.awt.Color.
     * 
     * @param p properties
     * @param propName the name of the property
     * @param dfault color to use if the property value doesn't work
     * @return java.awt.Color
     * @exception NumberFormatException if the specified string cannot
     *            be interpreted as a hexidecimal integer
     * @see ColorFactory#parseColorFromProperties(Properties, String,
     *      String, boolean)
     */
    public static Color parseColorFromProperties(Properties p, String propName,
                                                 String dfault)
            throws NumberFormatException {
        return ColorFactory.parseColorFromProperties(p, propName, dfault, false);
    }

    /**
     * Take a string from a properties file, representing the 24bit
     * RGB or 32bit ARGB hex values for a color, and convert it to a
     * java.awt.Color.
     * 
     * @param p properties
     * @param propName the name of the property
     * @param dfault color to use if the property value doesn't work
     * @return java.awt.Color
     * @exception NumberFormatException if the specified string cannot
     *            be interpreted as a hexidecimal integer
     * @see ColorFactory#parseColorFromProperties(Properties, String,
     *      String, boolean)
     */
    public static Paint parseColorFromProperties(Properties p, String propName,
                                                 Paint dfault)
            throws NumberFormatException {
        return ColorFactory.parseColorFromProperties(p, propName, dfault);
    }

    /**
     * Convert a string representing a 24/32bit hex color value into a
     * Color value. NOTE:
     * <ul>
     * <li>Only 24bit (RGB) java.awt.Color is supported on the JDK
     * 1.1 platform.
     * <li>Both 24/32bit (ARGB) java.awt.Color is supported on the
     * Java 2 platform.
     * </ul>
     * 
     * @param colorString the 24/32bit hex string value (ARGB)
     * @return java.awt.Color (24bit RGB on JDK 1.1, 24/32bit ARGB on
     *         JDK1.2)
     * @exception NumberFormatException if the specified string cannot
     *            be interpreted as a hexidecimal integer
     * @see ColorFactory#parseColor(String, boolean)
     */
    public static Color parseColor(String colorString)
            throws NumberFormatException {
        return ColorFactory.parseColor(colorString, false);
    }

    /**
     * Converts a properties object to an array of Strings. The
     * resulting array will consist of alternating key-value strings.
     * 
     * @param props the properties object to convert.
     * @return an array of Strings representing key-value pairs.
     */
    public static String[] getPropertiesAsStringArray(Properties props) {

        return PropUtils.getPropertiesAsStringArray(props);
    }

    /**
     * Returns a URL that names either a resource, a local file, or an
     * internet URL. Resources are checked for in the general
     * classpath.
     * 
     * @param name name of the resource, file or URL.
     * @throws java.net.MalformedURLException
     * @return URL
     */
    public static URL getResourceOrFileOrURL(String name)
            throws java.net.MalformedURLException {
        return PropUtils.getResourceOrFileOrURL(null, name);
    }

    /**
     * Returns a URL that names either a resource, a local file, or an
     * internet URL.
     * 
     * @param askingClass the object asking for the URL.
     * @param name name of the resource, file or URL.
     * @throws java.net.MalformedURLException
     * @return URL
     */
    public static URL getResourceOrFileOrURL(Object askingClass, String name)
            throws java.net.MalformedURLException {
        return PropUtils.getResourceOrFileOrURL(askingClass.getClass(), name);
    }

    /**
     * Returns a URL that names either a resource, a local file, or an
     * internet URL.
     * 
     * @param askingClass the class asking for the URL. Can be null.
     * @param name name of the resource, file or URL.
     * @throws java.net.MalformedURLException
     * @return URL
     */
    public static URL getResourceOrFileOrURL(Class askingClass, String name)
            throws java.net.MalformedURLException {
        return PropUtils.getResourceOrFileOrURL(askingClass, name);
    }
}