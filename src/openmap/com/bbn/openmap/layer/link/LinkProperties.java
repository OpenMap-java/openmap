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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkProperties.java,v $
// $RCSfile: LinkProperties.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.util.Debug;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutputStream;

/** 
 * A LinkProperties object is a set of key-value strings that are
 * going to be sent over the link.  In java-land, they are handled
 * with the Properties object.  In link-land, they are handled like an
 * array of strings.  Requests have a properties section, and graphic
 * objects have them as well.
 */
public class LinkProperties extends Properties 
    implements LinkPropertiesConstants {

    /** 
     * Used by the graphics if no properties were sent with it. 
     */
    public static final LinkProperties EMPTY_PROPERTIES = new LinkProperties();

    public LinkProperties(){
	super();
    }

    /** 
     * Create a LinkProperties object with it's first pair.
     * @param keyString the key for the pair.
     * @param valueString the value for the pair.
     */
    public LinkProperties(String keyString, String valueString){
	super();
	setProperty(keyString, valueString);
    }

    /** 
     * Create a LinkProperties, and read it's contents off a link.
     * Assumes the properties are the next thing to be read, starting
     * with the string count.
     *
     * @param numArgs the number of keys + values to read.
     * @throws IOException.
     */
    public LinkProperties(Link link) throws IOException {
	super();
	read(link.dis);
    }

    /**  
     * Create a LinkProperties, and read it's contents off a link.
     *
     * @param dis DataInput to read from.
     * @param numArgs the number of keys + values to read.
     * @throws IOException. 
     */
    public LinkProperties(DataInput dis) throws IOException {
	read(dis);
    }

    /**
     * Calls the hashtable method <code>put</code>. Provided to
     * provide a similar interface in jdk1.1.x or jdk1.2.x, enforcing
     * that only strings can be in properties files.
     */
    public synchronized Object setProperty(String key, String value) {
        return put(key, value);
    }

    /** 
     * Write the properties as several strings.  There is a string
     * count (Key count + value count), and then for each key and
     * value string, a character count, and the characters. 
     * @param link the link to write to.
     */
    public void write(Link link) throws IOException {
	write(link.dos);
    }

    /** 
     * Write the properties as several strings.  There is a string
     * count (Key count + value count), and then for each key and
     * value string, a character count, and the characters. 
     * @param dos the DataOutputStream to write to.
     */
    public void write(DataOutputStream dos) throws IOException {

	dos.writeInt(size()*2);
	for (Enumeration e = propertyNames() ; e.hasMoreElements() ;) {
	    String key = (String) e.nextElement();
	    String value = getProperty(key);
	    dos.writeInt(key.length());
	    dos.writeChars(key);
	    dos.writeInt(value.length());
	    dos.writeChars(value);
	}
    }

    /** 
     * Read the link to create the properties object.  Assumes the
     * properties are the next thing to be read, starting with the
     * string count.
     *
     * @param dis DataInput to read from.
     * @throws IOException. 
     */
    public void read(DataInput dis) throws IOException {
	int i;

	int numArgs = dis.readInt();
	String[] argStrings = new String[numArgs];
	
	for (i = 0; i < numArgs; i+=2){
	    int argLength = dis.readInt();
	    argStrings[i] = LinkUtil.readString(dis, argLength);
	    argLength = dis.readInt();
	    argStrings[i+1] = LinkUtil.readString(dis, argLength);

	    put(argStrings[i], argStrings[i+1]);
	}
	if (Debug.debugging("linkdetail")){
	    System.out.println("LinkProperties | Read:  " + this);
	}
    }

}

