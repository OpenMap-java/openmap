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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/PropUtils.java,v
// $
// $RCSfile: PropUtils.java,v $
// $Revision: 1.16 $
// $Date: 2008/09/26 12:07:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

/* Java Core */
import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;

public class PropUtils {

   public static Logger logger = Logger.getLogger("com.bbn.openmap.util.PropUtils");

   /**
    * Parse a list of marker names from a space separated list within a String.
    * <p>
    * 
    * @param markerList a string containing a space delimited list of marker
    *        names.
    * @return Vector of marker names.
    */
   public static Vector<String> parseSpacedMarkers(String markerList) {
      return parseMarkers(markerList, " ");
   }

   /**
    * Parse a list of marker names from a space separated list within a String.
    * <p>
    * 
    * @param markerList a string containing a list of things to be parsed.
    * @param delim the list of tokens to look for which separate the list
    *        elements.
    * @return Vector of marker names.
    */
   public static Vector<String> parseMarkers(String markerList, String delim) {
      Vector<String> vector = null;

      if (markerList == null) {
         logger.fine("marker list null!");
         return new Vector<String>(0);
      }

      if (logger.isLoggable(Level.FINE)) {
         logger.fine("parsing marker list |" + markerList + "|");
      }

      // First, get rid of the quotation marks;
      markerList = markerList.replace('\"', '\0');
      // Next, tokenize the space delimited string
      StringTokenizer tokens = new StringTokenizer(markerList, delim);
      vector = new Vector<String>(tokens.countTokens());
      while (tokens.hasMoreTokens()) {
         String name = tokens.nextToken().trim();
         vector.addElement(name);
      }
      return vector;
   }

   /**
    * A string used in marker names for property editors, when a custom editor
    * used by an Inspector can set several top-level properties.
    */
   public final static String DUMMY_MARKER_NAME = "DUMMY_MARKER_NAME";
   /** Borrowed from Properties.java */
   public static final String keyValueSeparators = "=: \t\r\n\f";
   /** Borrowed from Properties.java */
   public static final String strictKeyValueSeparators = "=:";
   /** Borrowed from Properties.java */
   public static final String whiteSpaceChars = " \t\r\n\f";
   /** As defined in the OGC Web Mapping Test bed. */
   public static final String propertySeparators = "&";

   /**
    * Take a property list, defined in a single string, and return a Properties
    * object. The properties, as key-value pairs, are separated by another type
    * of symbol. In this method, the key-values are assumed to be separated from
    * other key-value pairs by PropUtils.propertySeparators String characters,
    * and each key is separated from its value by any character in the
    * PropUtils.keyValueSeparators list.
    * 
    * @param list the properties list string.
    * @return Properties object containing keys and values.
    * @throws PropertyStringFormatException if a key doesn't have a value.
    */
   public static Properties parsePropertyList(String list)
         throws PropertyStringFormatException {
      return parsePropertyList(list, propertySeparators, keyValueSeparators);
   }

   /**
    * Take a property list, defined in a single string, and return a Properties
    * object. The properties, as key-value pairs, are separated by another type
    * of symbol.
    * 
    * @param list the properties list string.
    * @return Properties object containing keys and values.
    * @param propertySeparators the key-values are assumed to be separated from
    *        other key-value pairs by any character in this String.
    * @param keyValueSeparators each key is separated from its value by any
    *        character in this String.
    * @throws PropertyStringFormatException if a key doesn't have a value.
    */
   public static Properties parsePropertyList(String list, String propertySeparators, String keyValueSeparators)
         throws PropertyStringFormatException {

      Properties props = new Properties();

      Vector<String> keyValuePairs = parseMarkers(list, propertySeparators);
      for (int i = 0; i < keyValuePairs.size(); i++) {
         // Next, tokenize the space delimited string
         StringTokenizer tokens = new StringTokenizer(keyValuePairs.elementAt(i), keyValueSeparators);

         try {
            String key = tokens.nextToken().trim();
            String value = tokens.nextToken().trim();
            props.put(key, value);
         } catch (NoSuchElementException nsee) {
            throw new PropertyStringFormatException(list);
         }
      }
      return props;
   }

   /**
    * Copy the contents from one properties object to another.
    * 
    * @param from the source Properties object.
    * @param to the destination Properties object.
    */
   public static void copyProperties(Properties from, Properties to) {
      Enumeration<Object> keys = from.keys();

      while (keys.hasMoreElements()) {
         String key = (String) keys.nextElement();
         to.put(key, from.getProperty(key));
      }
   }

   /**
    * Load the named file from the named directory into the given
    * <code>Properties</code> instance. If the file is not found a warning is
    * issued. If an IOException occurs, a fatal error is printed.
    * 
    * @param props the instance to receive the loaded properties
    * @param dir the directory where the properties file resides
    * @param file the name of the file
    * @return true if the properties file exists and was loaded.
    */
   public static boolean loadProperties(Properties props, String dir, String file) {
      File propsFile = new File(dir, file);

      try {
         InputStream propsStream = new FileInputStream(propsFile);
         props.load(propsStream);
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("Found " + propsFile);
         }
         return true;

      } catch (java.io.FileNotFoundException e) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("File not found -  \"" + propsFile + "\"");
         }
      } catch (java.io.IOException e) {
         logger.warning("Caught IO Exception reading \"" + propsFile + "\"");
         e.printStackTrace();
      } catch (java.security.AccessControlException ace) {
      }
      return false;
   }

   /**
    * Loads properties from a java resource. This will load the named resource
    * identifier into the given properties instance.
    * 
    * @param properties the Properties instance to receive the properties.
    * @param propsIn an InputStream to read properties from
    * @return true if the properties file exists and was loaded.
    */
   public static boolean loadProperties(Properties properties, InputStream propsIn) {
      try {
         properties.load(propsIn);
         return true;
      } catch (java.io.IOException e) {
         if (logger.isLoggable(Level.FINE)) {
            logger.warning("Caught IOException loading properties from InputStream.");
         }
         return false;
      }
   }

   /**
    * A function that brings up a file chooser window in order to have the user
    * look for a valid Java properties file.
    * 
    * @return properties object with selected file contents.
    */
   public static Properties promptUserForProperties() {
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(null);
      Properties props = new Properties();
      if (retvalue != JFileChooser.APPROVE_OPTION) {
         return props;
      }
      try {
         FileInputStream inputStream = new FileInputStream(fileChooser.getSelectedFile());
         props.load(inputStream);
         return props;
      } catch (Exception ioe) {
         System.err.println("PropUtils.promptUserForProperties: Exception reading properties file.");
         System.err.println(ioe.getMessage());
         ioe.printStackTrace();
         return props;
      }
   }

   /**
    * It seems like every PropertyConsumer wrestles with having a prefix or not.
    * This method lets you just get the prefix with a period on the end (for
    * scoping purposes), or just returns an empty String. Either way, you get a
    * String you can slap on the beginning of your defined property names to get
    * a valid property based on what the prefix is.
    */
   public static String getScopedPropertyPrefix(PropertyConsumer pc) {
      return getScopedPropertyPrefix(pc.getPropertyPrefix());
   }

   /**
    * Given the string, check if it's null. If it is, return an empty string. If
    * it isn't, check to see if it ends with a period, and do nothing if it
    * does. If it doesn't end in a period, add one, and then return that. The
    * returned string should be good for prepending to other properties.
    */
   public static String getScopedPropertyPrefix(String pre) {
      if (pre == null || pre.length() == 0) {
         return "";
      } else if (pre.endsWith(".")) {
         return pre;
      } else {
         return pre + ".";
      }
   }

   /**
    * Should be called by a PropertyConsumer in the
    * getPropertiesInfo(Properties) method to create a property marker name for
    * a custom PropertyEditor that will modify several top-level properties,
    * i.e. the com.bbn.openmap.omGraphics.DrawingAttributesPropertyEditor.
    * 
    * @param realComponentPropertyPrefix the top-level prefix that the
    *        PropertyConsumer being set with the properties has. Can be null.
    * @param anyDesiredScoper an additional scoping mechanism if there are more
    *        than one custom editors being used for a given getPropertyInfo
    *        call.
    * @return The string that is used for a marker for a custom editor.
    */
   public static String getDummyMarkerForPropertyInfo(String realComponentPropertyPrefix, String anyDesiredScoper) {
      return DUMMY_MARKER_NAME + (anyDesiredScoper != null ? anyDesiredScoper : "") + "."
            + (realComponentPropertyPrefix != null ? realComponentPropertyPrefix : "");
   }

   /**
    * Should be used inside a
    * PropertyConsumerPropertyEditor.setProperties(String, Properties) method to
    * set the real property prefix. The PropertyConsumer that the Inspector is
    * looking at should use the getDummyMarker() call to create the marker for
    * the getPropertyInfor(Properties) call.
    * 
    * @param possibleDummyMarker
    * @return the encoded 'real' property prefix for the PropertyConsumer
    *         embedded in the dummy marker, or the possibleDummyMarker if it's
    *         not a dummy marker.
    */
   public static String decodeDummyMarkerFromPropertyInfo(String possibleDummyMarker) {
      if (possibleDummyMarker != null && possibleDummyMarker.startsWith(DUMMY_MARKER_NAME)) {
         int lastDot = possibleDummyMarker.lastIndexOf(".");
         if (lastDot != -1) {
            possibleDummyMarker = possibleDummyMarker.substring(lastDot + 1);
         }
      }
      return possibleDummyMarker;
   }

   /**
    * It kills Properties to have null values set. You can wrap a property value
    * in this in PropertyConsumer.getProperties() to not worry about it. Returns
    * "" if prop == null, else returns what was passed in.
    */
   public static String unnull(String prop) {
      if (prop == null) {
         return "";
      }
      return prop;
   }

   /**
    * Takes a string of `;' separated paths and returns an array of parsed
    * strings. NOTE: this method currently doesn't support appropriate quoting
    * of the `;' character, although it probably should...
    * 
    * @param p properties
    * @param propName the name of the property
    * @return Array of strings representing paths.
    */
   public static String[] initPathsFromProperties(Properties p, String propName) {
      return initPathsFromProperties(p, propName, null);
   }

   /**
    * Takes a string of `;' separated paths and returns an array of parsed
    * strings. NOTE: this method currently doesn't support appropriate quoting
    * of the `;' character, although it probably should...
    * 
    * @param p properties
    * @param propName the name of the property
    * @param defaultPaths the value of the paths to set if the property doesn't
    *        exist, or if is doesn't contain anything.
    * @return Array of strings representing paths.
    */
   public static String[] initPathsFromProperties(Properties p, String propName, String[] defaultPaths) {
      String[] ret = stringArrayFromProperties(p, propName, ";");

      if (ret == null) {
         ret = defaultPaths;
      }

      return ret;
   }

   /**
    * Return the first letter of the property specified by propName. If that
    * value is null or has length of zero, the default char is returned.
    * 
    * @param p
    * @param propName
    * @param defaultValue
    * @return first char from property, or default.
    */
   public static char charFromProperties(Properties p, String propName, char defaultValue) {
      char ret = defaultValue;
      String charString = p.getProperty(propName);
      if (charString != null && charString.length() > 0) {
         return charString.charAt(0);
      }
      return ret;
   }

   /**
    * Gets an integer out of a properties object. Returns the default value if
    * something goes wrong.
    * 
    * @param p properties
    * @param propName name of the property associated with the wanted value.
    * @param defaultValue what to return if the property name doesn't exist, or
    *        if the value isn't a numerical value.
    * @return integer value associated with the property.
    */
   public static int intFromProperties(Properties p, String propName, int defaultValue) {
      int ret = defaultValue;
      String intString = p.getProperty(propName);

      if (intString != null) {
         try {
            ret = Integer.parseInt(intString.trim());
         } catch (NumberFormatException e) {
            ret = defaultValue;
         }
      }
      return ret;
   }

   /**
    * Gets an float out of a properties object. Returns the default value if
    * something goes wrong.
    * 
    * @param p properties
    * @param propName name of the property associated with the wanted value.
    * @param defaultValue what to return if the property name doesn't exist, or
    *        if the value isn't a numerical value.
    * @return float value associated with the property.
    */
   public static float floatFromProperties(Properties p, String propName, float defaultValue) {
      float ret = defaultValue;
      String floatString = p.getProperty(propName);

      if (floatString != null) {
         try {
            ret = Float.parseFloat(floatString.trim());
         } catch (NumberFormatException e) {
            ret = defaultValue;
         }
      }
      return ret;
   }

   /**
    * Gets an boolean out of a properties object. Returns the default value if
    * something goes wrong.
    * 
    * @param p properties
    * @param propName name of the property associated with the wanted value.
    * @param defaultValue what to return if the property name doesn't exist, or
    *        if the value isn't a numerical value.
    * @return boolean value associated with the property.
    */
   public static boolean booleanFromProperties(Properties p, String propName, boolean defaultValue) {
      boolean ret = defaultValue;
      String booleanString = p.getProperty(propName);
      if (booleanString != null) {
         ret = booleanString.trim().equalsIgnoreCase("true");
      }

      return ret;
   }

   /**
    * Creates an object out of a property name. If anything fails, return null.
    * 
    * @param p properties
    * @param propName name of class to instantiate.
    * @return null on failure, otherwise, a default constructed instance of the
    *         class named in the property.
    */
   public static Object objectFromProperties(Properties p, String propName) {

      Object ret = null;
      String objectName = p.getProperty(propName);
      if (objectName != null) {
         ret = ComponentFactory.create(objectName);
      }
      return ret;
   }

   /**
    * Takes a string of representing token separated properties and returns an
    * array of parsed strings. NOTE: this method currently doesn't support
    * appropriate quoting of the token, although it probably should...
    * 
    * @param p properties
    * @param propName the name of the property
    * @param tok the characters separating the strings.
    * @return Array of strings between the tokens.
    */
   public static String[] stringArrayFromProperties(Properties p, String propName, String tok) {

      String[] ret = null;
      String raw = p.getProperty(propName);

      if (raw != null && raw.length() > 0) {

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
    * Gets a double out of a properties object. Returns the default value if
    * something goes wrong.
    * 
    * @param p properties
    * @param propName name of the property associated with the wanted value.
    * @param defaultValue what to return if the property name doesn't exist, or
    *        if the value isn't a numerical value.
    * @return double value associated with the property.
    */
   public static double doubleFromProperties(Properties p, String propName, double defaultValue) {
      double ret = defaultValue;
      String doubleString = p.getProperty(propName);

      if (doubleString != null) {
         try {
            ret = Double.parseDouble(doubleString.trim());
         } catch (NumberFormatException e) {
            ret = defaultValue;
         }
      }
      return ret;
   }

   /**
    * Gets a long out of a properties object. Returns the default value if
    * something goes wrong.
    * 
    * @param p properties
    * @param propName name of the property associated with the wanted value.
    * @param defaultValue what to return if the property name doesn't exist, or
    *        if the value isn't a numerical value.
    * @return long value associated with the property.
    */
   public static long longFromProperties(Properties p, String propName, long defaultValue) {
      long ret = defaultValue;
      String longString = p.getProperty(propName);

      if (longString != null) {
         try {
            ret = Long.parseLong(longString.trim());
         } catch (NumberFormatException e) {
            ret = defaultValue;
         }
      }
      return ret;
   }

   /**
    * Take a string from a properties file, representing the 24bit RGB or 32bit
    * ARGB hex values for a color, and convert it to a java.awt.Color.
    * 
    * @param p properties
    * @param propName the name of the property
    * @param dfault color to use if the property value doesn't work
    * @return java.awt.Color
    * @exception NumberFormatException if the specified string cannot be
    *            interpreted as a hexidecimal integer
    * @see ColorFactory#parseColorFromProperties(Properties, String, String,
    *      boolean)
    */
   public static Color parseColorFromProperties(Properties p, String propName, String dfault)
         throws NumberFormatException {
      return ColorFactory.parseColorFromProperties(p, propName, dfault, false);
   }

   /**
    * Take a string from a properties file, representing the 24bit RGB or 32bit
    * ARGB hex values for a color, and convert it to a java.awt.Color.
    * 
    * @param p properties
    * @param propName the name of the property
    * @param dfault color to use if the property value doesn't work
    * @param forceAlpha force using alpha value
    * @return java.awt.Color
    * @exception NumberFormatException if the specified string cannot be
    *            interpreted as a hexidecimal integer
    * @see ColorFactory#parseColorFromProperties(Properties, String, String,
    *      boolean)
    */
   public static Color parseColorFromProperties(Properties p, String propName, String dfault, boolean forceAlpha)
         throws NumberFormatException {
      return ColorFactory.parseColorFromProperties(p, propName, dfault, forceAlpha);
   }

   /**
    * Take a string from a properties file, representing the 24bit RGB or 32bit
    * ARGB hex values for a color, and convert it to a java.awt.Color.
    * 
    * @param p properties
    * @param propName the name of the property
    * @param dfault color to use if the property value doesn't work
    * @return java.awt.Color
    * @see ColorFactory#parseColorFromProperties(Properties, String, String,
    *      boolean)
    */
   public static Paint parseColorFromProperties(Properties p, String propName, Paint dfault) {
      return ColorFactory.parseColorFromProperties(p, propName, dfault);
   }

   /**
    * Convert a string representing a 24/32bit hex color value into a Color
    * value. NOTE:
    * <ul>
    * <li>Only 24bit (RGB) java.awt.Color is supported on the JDK 1.1 platform.
    * <li>Both 24/32bit (ARGB) java.awt.Color is supported on the Java 2
    * platform.
    * </ul>
    * 
    * @param colorString the 24/32bit hex string value (ARGB)
    * @param forceAlpha force using alpha value
    * @return java.awt.Color (24bit RGB on JDK 1.1, 24/32bit ARGB on JDK1.2)
    * @exception NumberFormatException if the specified string cannot be
    *            interpreted as a hexidecimal integer
    * @see ColorFactory#parseColor(String, boolean)
    */
   public static Color parseColor(String colorString, boolean forceAlpha)
         throws NumberFormatException {
      return ColorFactory.parseColor(colorString, forceAlpha);
   }

   /**
    * Convert a string representing a 24/32bit hex color value into a Color
    * value. NOTE:
    * <ul>
    * <li>Only 24bit (RGB) java.awt.Color is supported on the JDK 1.1 platform.
    * <li>Both 24/32bit (ARGB) java.awt.Color is supported on the Java 2
    * platform.
    * </ul>
    * 
    * @param colorString the 24/32bit hex string value (ARGB)
    * @return java.awt.Color (24bit RGB on JDK 1.1, 24/32bit ARGB on JDK1.2)
    * @exception NumberFormatException if the specified string cannot be
    *            interpreted as a hexidecimal integer
    * @see ColorFactory#parseColor(String, boolean)
    */
   public static Color parseColor(String colorString)
         throws NumberFormatException {
      return ColorFactory.parseColor(colorString, false);
   }

   /**
    * Returns a string representing a color, properly buffered for zeros for
    * different alpha values.
    * 
    * @param color
    * @return string for color with alpha values.
    */
   public static String getProperty(Color color) {
      StringBuffer hexstring = new StringBuffer(Integer.toHexString(color.getRGB()));
      while (hexstring.length() < 8) {
         hexstring.insert(0, '0');
      }
      return hexstring.toString();
   }

   /**
    * Converts a properties object to an array of Strings. The resulting array
    * will consist of alternating key-value strings.
    * 
    * @param props the properties object to convert.
    * @return an array of Strings representing key-value pairs.
    */
   public static String[] getPropertiesAsStringArray(Properties props) {

      int size = props.size();
      String[] ret = new String[size * 2]; // key and value
      int count = 0;
      Enumeration<?> things = props.propertyNames();
      while (things.hasMoreElements()) {
         ret[count] = (String) things.nextElement();
         ret[count + 1] = (String) props.getProperty(ret[count]);
         count += 2;
      }
      return ret;
   }

   /**
    * Returns a URL that names either a resource, a local file, or an internet
    * URL. Resources are checked for in the general classpath.
    * 
    * @param name name of the resource, file or URL.
    * @throws java.net.MalformedURLException
    * @return URL
    */
   public static URL getResourceOrFileOrURL(String name)
         throws java.net.MalformedURLException {
      return getResourceOrFileOrURL(null, name);
   }

   /**
    * Returns a URL that names either a resource, a local file, or an internet
    * URL.
    * 
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
    * Returns a URL that names either a resource, a local file, or an internet
    * URL.
    * 
    * @param askingClass the class asking for the URL. Can be null.
    * @param name name of the resource, file or URL.
    * @throws java.net.MalformedURLException
    * @return URL
    */
   public static URL getResourceOrFileOrURL(Class<? extends Object> askingClass, String name)
         throws java.net.MalformedURLException {

      boolean DEBUG = logger.isLoggable(Level.FINE);

      if (name == null) {
         if (DEBUG)
            logger.fine("null file name");
         return null;
      }

      URL retval = null;
      if (DEBUG)
         logger.fine("looking for " + name);

      if (askingClass != null) {
         // First see if we have a resource by that name
         if (DEBUG)
            logger.fine("checking as resource");

         retval = askingClass.getResource(name);
      }
      if (retval == null) {
         // Check the general classpath...
         if (DEBUG)
            logger.fine("checking in general classpath");
         retval = Thread.currentThread().getContextClassLoader().getResource(name);
      }

      // if (retval == null && !Environment.isApplet()) {
      // // Check the classpath plus the share directory, which may
      // // be in the openmap.jar file or in the development
      // // environment.
      // if (DEBUG)
      // logger.fine("checking with ClassLoader");
      // retval = ClassLoader.getSystemResource("share/" + name);
      // }

      if (retval == null && Environment.isApplet()) {
         if (DEBUG)
            logger.fine("checking with URLClassLoader");
         URL[] cba = new URL[1];
         cba[0] = Environment.getApplet().getCodeBase();
         URLClassLoader ucl = URLClassLoader.newInstance(cba);
         retval = ucl.getResource(name);
      }

      // If there was no resource by that name available
      if (retval == null) {
         if (DEBUG)
            logger.fine("not found as resource");

         try {
            java.io.File file = new java.io.File(name);
            if (file.exists()) {
               retval = file.toURI().toURL();
               if (DEBUG)
                  logger.fine("found as file :)");
            } else {
               // Otherwise treat it as a raw URL.
               if (DEBUG)
                  logger.fine("Not a file, checking as URL");
               retval = new URL(name);
               java.io.InputStream is = retval.openStream();
               is.close();
               if (DEBUG)
                  logger.fine("OK as URL :)");
            }
         } catch (java.io.IOException ioe) {
            retval = null;
         } catch (java.security.AccessControlException ace) {
            logger.warning("AccessControlException trying to access " + name);
            retval = null;
         } catch (Exception e) {
            logger.warning("caught exception " + e.getMessage());
            retval = null;
         }
      }

      if (DEBUG) {
         if (retval != null) {
            logger.fine("Resource " + name + "=" + retval.toString());
         } else {
            logger.fine("Resource " + name + " can't be found...");
         }
      }

      return retval;
   }

   /**
    * Simple space saving implementation of common I18n Property Info setting.
    * 
    * @param i18n i18n object to use to search for internationalized strings.
    * @param info the properties class being used to set information into.
    * @param classToSetFor class to use for i18n search.
    * @param propertyName property to set for.
    * @param label label to use for GUI (can be null if N/A).
    * @param tooltip tooltip to use for GUI (can be null if N/A).
    * @param editor editor class string to use for GUI (can be null if N/A).
    * @return Properties object passed in, or new one if null Properties passed
    *         in.
    */
   public static Properties setI18NPropertyInfo(I18n i18n, Properties info, Class<? extends Object> classToSetFor,
                                                String propertyName, String label, String tooltip, String editor) {
      if (info == null) {
         info = new Properties();
      }
      if (i18n != null) {
         if (tooltip != null) {
            String internString = i18n.get(classToSetFor, propertyName, I18n.TOOLTIP, tooltip);
            info.put(propertyName, internString);
         }
         if (label != null) {
            String internString = i18n.get(classToSetFor, propertyName, label);
            info.put(propertyName + PropertyConsumer.LabelEditorProperty, internString);
         }
         if (editor != null) {
            info.put(propertyName + PropertyConsumer.ScopedEditorProperty, editor);
         }
      }
      return info;
   }

   public static void putDataPrefixToLayerList(Layer layer, Properties props, String layerListProperty) {
      String dataPrefix = (String) layer.getAttribute(Layer.DataPathPrefixProperty);
      if (dataPrefix != null && dataPrefix.length() > 0) {
         putDataPrefixToLayerList(dataPrefix, props, layerListProperty);
      }
   }

   /**
    * Handle setting the dataPathPrefixes on all layer's properties.
    */
   public static void putDataPrefixToLayerList(String dataPrefix, Properties props, String layerListProperty) {
      Vector<String> layersValue = parseSpacedMarkers(props.getProperty(layerListProperty));

      for (Iterator<String> it = layersValue.iterator(); it.hasNext();) {
         String markerName = getScopedPropertyPrefix(it.next());
         props.setProperty(markerName + Layer.DataPathPrefixProperty, dataPrefix);
      }
   }

   /**
    * Get a List of Objects defined by marker names listed in a property. If the
    * objects are PropertyConsumers, they will be given the properties and their
    * scoped property prefix so they can configure themselves.
    * 
    * <pre>
    * 
    * listProperty=markername1 markername2 markername3
    * markername1.definingProperty=classname1
    * markername2.definingProperty=classname2
    * markername3.definingProperty=classname3
    * 
    * </pre>
    * 
    * @param p Properties object containing all properties
    * @param markerListProperty listProperty in example above
    * @param definingProperty definingProperty in example above, scoped property
    *        when combined with marker name to define the class that should be
    *        created for an object.
    * @return List of objects created from properties
    */
   public static List<?> objectsFromProperties(Properties p, String markerListProperty, String definingProperty) {
      return objectsFromScopedProperties(p, markerListProperty, definingProperty, null);
      //
      // String markerList = p.getProperty(markerListProperty);
      // List<Object> ret = new LinkedList<Object>();
      //
      // if (markerList != null) {
      // Vector<String> markerNames = parseSpacedMarkers(markerList);
      // for (String markerName : markerNames) {
      // String classname = p.getProperty(markerName + "."
      // + definingProperty);
      // if (classname != null) {
      // Object obj = ComponentFactory.create(classname, markerName,
      // p);
      //
      // if (obj != null) {
      // ret.add(obj);
      // }
      // }
      // }
      //
      // }
      //
      // return ret;
   }

   /**
    * Get a List of Objects defined by marker names listed in a property, when
    * this list property has been scoped by a parent object definition. If the
    * objects are PropertyConsumers, they will be given the properties and their
    * scoped property prefix so they can configure themselves.
    * 
    * <pre>
    * 
    * parentMarker.listProperty=markername1 markername2 markername3
    * parentMarker.markername1.definingProperty=classname1
    * parentMarker.markername2.definingProperty=classname2
    * parentMarker.markername3.definingProperty=classname3
    * 
    * </pre>
    * 
    * @param p Properties object containing all properties
    * @param markerListProperty listProperty in example above
    * @param definingProperty definingProperty in example above, scoped property
    *        when combined with marker name to define the class that should be
    *        created for an object.
    * @return List of objects created from properties, where properties are
    *         scoped for each object.
    */
   public static List<?> objectsFromScopedProperties(Properties p, String markerListProperty, String definingProperty,
                                                     String parentMarker) {
      String markerList = p.getProperty(markerListProperty);
      List<Object> ret = new LinkedList<Object>();
      parentMarker = PropUtils.getScopedPropertyPrefix(parentMarker);

      if (markerList != null) {
         Vector<String> markerNames = parseSpacedMarkers(markerList);
         for (String markerName : markerNames) {
            String classname = p.getProperty(parentMarker + markerName + "." + definingProperty);
            if (classname != null) {
               Object obj = ComponentFactory.create(classname, markerName, p);

               if (obj != null) {
                  ret.add(obj);
               }
            }
         }
      }

      return ret;
   }

   /**
    * Add the object property value to properties if it's not null or empty,
    * which assumes it should be set to the default value.
    * 
    * @param props Properties to write to
    * @param propertyName the scoped property to enter value under
    * @param value the object to get property from - toString() will be called
    *        if not null.
    */
   public static <T extends Object> void putIfNotDefault(Properties props, String propertyName, T value) {
      putIfNotDefault(props, propertyName, value, null);
   }

   /**
    * Add the object property value to properties if it's not null or default
    * value.
    * 
    * @param props Properties to write to
    * @param propertyName the scoped property to enter value under
    * @param value the object to get property from - toString() will be called
    *        if not null.
    * @param def the default value of the property. If not null and value not
    *        null, toString().equals() will be used to determine equality. If
    *        not equal, then value will be set in props.
    */
   public static <T extends Object> void putIfNotDefault(Properties props, String propertyName, T value, T def) {
      if (value != null) {
         String valString = value.toString().trim();
         if (def == null || (def != null && !def.toString().trim().equals(valString))) {
            // empty property strings are a pain...
	     if (valString.length() != 0) {
               props.put(propertyName, valString);
            }
         }
      }
   }
}