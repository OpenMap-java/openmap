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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/PropUtils.java,v $
// $RCSfile: PropUtils.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util;

import java.io.*;
import java.util.*;
import javax.swing.JFileChooser;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.Environment;

public class PropUtils {

    /**
     * Parse a list of marker names from a space separated list within
     * a String.  <p>
     * @param markerList a string containing a space delimited list of
     * marker names.
     * @return Vector of marker names.  
     */
    public static Vector parseSpacedMarkers(String markerList) {
	return parseMarkers(markerList, " ");
    } 

    /**
     * Parse a list of marker names from a space separated list within
     * a String.  <p>
     * @param markerList a string containing a space delimited list of
     * marker names.
     * @param delim the list of tokens to look for which separate the
     * list elements.
     * @return Vector of marker names.  
     */
    public static Vector parseMarkers(String markerList, String delim){
	Vector vector = null;

	if (markerList == null){
	    Debug.message("propertiesdetail", "PropUtils: marker list null!");
	    return new Vector(0);
	}

	if (Debug.debugging("propertiesdetail")) {
	    Debug.output("PropertyHandler: parsing marker list |" +
			 markerList + "|");
	}

	// First, get rid of the quotation marks;
	markerList = markerList.replace('\"', '\0');
	// Next, tokenize the space delimited string
	StringTokenizer tokens = new StringTokenizer(markerList, delim);
	vector = new Vector(tokens.countTokens());
	while (tokens.hasMoreTokens()) {
	    String name = tokens.nextToken().trim();
	    vector.addElement(name);
	}
	return vector;
    }

    /** Borrowed from Properites.java */
    public static final String keyValueSeparators = "=: \t\r\n\f";
    /** Borrowed from Properites.java */
    public static final String strictKeyValueSeparators = "=:";
    /** Borrowed from Properites.java */
    public static final String whiteSpaceChars = " \t\r\n\f";
    /** As defined in the OGC Web Mapping Testbed. */
    public static final String propertySeparators = "&";

    /**
     * Take a property list, defined in a single string, and return a
     * Properties object.  The properties, as key-value pairs, are
     * separated by another type of symbol.  In this method, the
     * key-values are assumed to be separated from other key-value
     * pairs by PropUtils.propertySeparators String characters, and
     * each key is separated from its value by any character in the
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
     * Take a property list, defined in a single string, and return a
     * Properties object.  The properties, as key-value pairs, are
     * separated by another type of symbol.
     *
     * @param list the properties list string.
     * @return Properties object containing keys and values.
     * @param propertySeparators the key-values are assumed to be
     * separated from other key-value pairs by any character in this
     * String.
     * @param keyValueSeparators each key is separated from its value
     * by any character in this String.
     * @throws PropertyStringFormatException if a key doesn't have a value.
     */
    public static Properties parsePropertyList(String list,
					       String propertySeparators, 
					       String keyValueSeparators) 
	throws PropertyStringFormatException {

	Properties props = new Properties();

	Vector keyValuePairs = parseMarkers(list, propertySeparators);
	for (int i = 0; i < keyValuePairs.size(); i++) {
	    // Next, tokenize the space delimited string
	    StringTokenizer tokens = new StringTokenizer((String)keyValuePairs.elementAt(i), keyValueSeparators);

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
     * @param from the source Properties object.
     * @param to the destination Properties object.
     */
    public static void copyProperties(Properties from, Properties to)	{
	Enumeration keys = from.keys();
	
	while (keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();
	    to.put(key, from.getProperty(key) );
	}
    }

    /**
     * Load the named file from the named directory into the given
     * <code>Properties</code> instance.  If the file is not found
     * a warning is issued.  If an IOException occurs, a fatal error
     * is printed.
     *
     * @param props the instance to receive the loaded properties
     * @param dir the directory where the properties file resides
     * @param file the name of the file
     */
    public static boolean loadProperties(Properties props, 
					 String dir, String file) {
	File propsFile = new File(dir, file);

	try {
	    InputStream propsStream = new FileInputStream(propsFile);
	    props.load(propsStream);
	    if (Debug.debugging("properties")){
		Debug.output("PropUtils: Found " + propsFile);
	    }
	    return true;

	} catch (java.io.FileNotFoundException e) {
	    if (Debug.debugging("properties")){
		Debug.output("PropUtils: File not found -  \"" +
			     propsFile + "\"");
	    }
	} catch (java.io.IOException e) {
	    Debug.error("PropUtils: Caught IO Exception reading \""
			+ propsFile + "\"");
	    e.printStackTrace();
	} catch (java.security.AccessControlException ace) {
	}
	return false;
    }

    /**
     * Loads properties from a java resource.  This will load the
     * named resource identifier into the given properties instance.
     *
     * @param properties the Properties instance to receive the properties.
     * @param resourceName the name of the resource to load.
     */
    public static boolean loadProperties(Properties properties,
					 InputStream propsIn) {	
	try {
	    properties.load(propsIn);
	    return true;
	} catch (java.io.IOException e) {
	    if (Debug.debugging("properties")) {
		Debug.error("PropUtils: Caught IOException loading properties from InputStream.");
	    }
	    return false;
	}
    }

    /**
     * A function that brings up a file chooser window in order to
     * have the user look for a valid Java properties file.
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
	    FileInputStream inputStream = 
		new FileInputStream(fileChooser.getSelectedFile());
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
     * It seems like every PropertyConsumer wrestles with having a
     * prefix or not.  This method lets you just get the prefix with a
     * period on the end (for scoping purposes), or just returns an
     * empty String.  Either way, you get a String you can slap on the
     * beginning of your defined propery names to get a valid property
     * based on what the prefix is.
     */
    public static String getScopedPropertyPrefix(com.bbn.openmap.PropertyConsumer pc) {
	return getScopedPropertyPrefix(pc.getPropertyPrefix());
    }

    /**
     * Given the string, check if it's null.  If it is, return an
     * empty string. If it isn't, check to see if it ends with a
     * period, and do nothing if it does.  If it doesn't end in a
     * period, add one, and then return that.  The returned string
     * should be good for prepending to other properties.
     */
    public static String getScopedPropertyPrefix(String pre) {
	if (pre == null) {
	    return "";
	} else if (pre.endsWith(".")) {
	    return pre;
	} else {
	    return pre + ".";
	}
    }

    /**
     * It kills Properties to have null values set.  You can wrap a
     * property value in this in PropertyConsumer.getProperties() to
     * not worry about it.  Returns "" if prop == null, else returns
     * what was passed in.
     */
    public static String unnull(String prop) {
	if (prop == null) {
	    return "";
	}
	return prop;
    }
}
