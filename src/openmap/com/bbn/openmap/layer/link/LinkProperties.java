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
// $Revision: 1.2 $
// $Date: 2003/08/14 22:28:46 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

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
    implements LinkPropertiesConstants, LinkGraphicConstants {

    /** 
     * Used by the graphics if no properties were sent with it. 
     */
    public static final LinkProperties EMPTY_PROPERTIES = new LinkProperties();

    public LinkProperties() {
	super();
    }

    /** 
     * Create a LinkProperties object with it's first pair.
     * @param keyString the key for the pair.
     * @param valueString the value for the pair.
     */
    public LinkProperties(String keyString, String valueString) {
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
	
	for (i = 0; i < numArgs; i+=2) {
	    int argLength = dis.readInt();
	    argStrings[i] = LinkUtil.readString(dis, argLength);
	    argLength = dis.readInt();
	    argStrings[i+1] = LinkUtil.readString(dis, argLength);

	    put(argStrings[i], argStrings[i+1]);
	}
	if (Debug.debugging("linkdetail")) {
	    System.out.println("LinkProperties | Read:  " + this);
	}
    }

    /**
     * Method to call on the LinkProperties object to set the
     * DrawingAttributes properties on an OMGraphic.  Will set the
     * line and select colors, fill paints (including patterns) and
     * stroke based on the properties contained in this LinkProperties
     * object.  Will set default values in the OMGraphic if the
     * applicable properties aren't defined, and will set the
     * LinkProperties in the AppObject of the OMGraphic.
     */
    public void setProperties(OMGraphic omg) {
	if (omg == null) return;

	omg.setLinePaint(getPaint(LPC_LINECOLOR, BLACK_COLOR_STRING));
	omg.setFillPaint(getFillPaint());
	omg.setSelectPaint(getPaint(LPC_HIGHLIGHTCOLOR, BLACK_COLOR_STRING));
	omg.setStroke(getStroke());
	omg.setAppObject(this);
    }

    public Stroke getStroke() {
	int lineWidth = PropUtils.intFromProperties(this, LPC_LINEWIDTH, 1);
	int cap = BasicStroke.CAP_SQUARE;
	int join = BasicStroke.JOIN_MITER;
	float miterLimit = 10f;
	float dashPhase = 0f;
	String strokeString = getProperty(LPC_LINESTYLE);
	Stroke stroke = null;
	float[] dash = null;

	if (strokeString != null) {
	    if (strokeString.equalsIgnoreCase(LPC_LONG_DASH)) {
		dash = new float[] {10f, 10f};
	    } else if (strokeString.equalsIgnoreCase(LPC_DASH)) {
		dash = new float[] {6f, 6f};
	    } else if (strokeString.equalsIgnoreCase(LPC_DOT)) {
		dash = new float[] {3f, 6f};
	    } else if (strokeString.equalsIgnoreCase(LPC_DASH_DOT)) {
		dash = new float[] {6f, 6f, 3f, 6f};
	    } else if (strokeString.equalsIgnoreCase(LPC_DASH_DOT_DOT)) {
		dash = new float[] {6f, 6f, 3f, 6f, 3f, 6f};
	    }

	    if (dash != null) {
		stroke = new BasicStroke(lineWidth, cap, join, miterLimit, dash, dashPhase);
	    }
	}

	if (stroke == null) {
	    stroke = new BasicStroke(lineWidth);
	}

	return stroke;
    }

    public Paint getPaint(String paintProperty, String defaultPaintString) {
	return ColorFactory.parseColorFromProperties(this, paintProperty, defaultPaintString, true);
    }

    public Paint getFillPaint() {
	Paint fillPaint = getPaint(LPC_FILLCOLOR, CLEAR_COLOR_STRING);

	String pattern = getProperty(LPC_FILLPATTERN);
	if (pattern == null || pattern.equalsIgnoreCase(LPC_SOLID_PATTERN)) {
	    return fillPaint;
	} else {
	    BufferedImage bi = new BufferedImage(8,8, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D big = bi.createGraphics();
	    big.setColor(new Color(0,true)); // clear
	    big.fillRect(0,0,8,8);
	    big.setPaint(fillPaint);

	    if (pattern.equalsIgnoreCase(LPC_HORIZONTAL_PATTERN)) {
		big.draw(new Line2D.Double(0, 0, 7, 0));
		big.draw(new Line2D.Double(0, 4, 7, 4));
	    } else if (pattern.equalsIgnoreCase(LPC_VERTICAL_PATTERN)) {
		big.draw(new Line2D.Double(0, 0, 0, 7));
		big.draw(new Line2D.Double(4, 0, 4, 7));
	    } else if (pattern.equalsIgnoreCase(LPC_CROSS_PATTERN)) {
		big.draw(new Line2D.Double(0, 0, 7, 0));
		big.draw(new Line2D.Double(0, 4, 7, 4));
		big.draw(new Line2D.Double(0, 0, 0, 7));
		big.draw(new Line2D.Double(4, 0, 4, 7));
	    } else if (pattern.equalsIgnoreCase(LPC_DIAG_CROSS_PATTERN)) {
		big.draw(new Line2D.Double(0, 0, 7, 7));
		big.draw(new Line2D.Double(0, 4, 3, 7));
		big.draw(new Line2D.Double(4, 0, 7, 3));
		big.draw(new Line2D.Double(0, 7, 7, 0));
		big.draw(new Line2D.Double(0, 3, 3, 0));
		big.draw(new Line2D.Double(4, 7, 7, 4));
	    } else if (pattern.equalsIgnoreCase(LPC_BACKWARD_DIAG_PATTERN)) {
		big.draw(new Line2D.Double(0, 0, 7, 7));
		big.draw(new Line2D.Double(0, 4, 3, 7));
		big.draw(new Line2D.Double(4, 0, 7, 3));
	    } else if (pattern.equalsIgnoreCase(LPC_FORWARD_DIAG_PATTERN)) {
		big.draw(new Line2D.Double(0, 7, 7, 0));
		big.draw(new Line2D.Double(0, 3, 3, 0));
		big.draw(new Line2D.Double(4, 7, 7, 4));
	    } else {
		// default to solid
		big.fillRect(0,0,8,8);
	    }

	    Rectangle r = new Rectangle(0, 0, 8, 8);
	    return new TexturePaint(bi, r);
	}
    }
}



