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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkProperties.java,v $
// $RCSfile: LinkProperties.java,v $
// $Revision: 1.9 $
// $Date: 2008/02/26 17:39:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A LinkProperties object is a set of key-value strings that are going to be
 * sent over the link. In java-land, they are handled with the Properties
 * object. In link-land, they are handled like an array of strings. Requests
 * have a properties section, and graphic objects have them as well.
 */
public class LinkProperties extends Properties implements LinkPropertiesConstants,
        LinkGraphicConstants {

    /**
     * Used by the graphics if no properties were sent with it. No properties
     * can be set on this LinkProperties object.
     */
    public static final LinkProperties EMPTY_PROPERTIES = new LinkProperties() {
        public Object put(Object obj1, Object obj2) {
            return null;
        }

        public void putAll(Map map) {
        }
    };

    protected Boolean reuseProperties;

    public LinkProperties() {
        super();
    }

    public LinkProperties(LinkProperties settings) {
        super(settings);
    }

    /**
     * Create a LinkProperties object with it's first pair.
     * 
     * @param keyString the key for the pair.
     * @param valueString the value for the pair.
     */
    public LinkProperties(String keyString, String valueString) {
        super();
        setProperty(keyString, valueString);
    }

    /**
     * Create a LinkProperties, and read it's contents off a link. Assumes the
     * properties are the next thing to be read, starting with the string count.
     * 
     * @param link the Link to read properties from
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
     * @throws IOException.
     */
    public LinkProperties(DataInput dis) throws IOException {
        read(dis);
    }

    public Boolean getReuseProperties() {
        return reuseProperties;
    }

    /**
     * A flag that controls how LinkProperties are managed. Setting this object
     * to Boolean.TRUE will indicate that the LinkProperties object decoded
     * previously should be reused, with any properties currently set in this
     * object overwriting the same property previously received. Setting it to
     * Boolean.FALSE, or setting it to null, will indicate than any buffered
     * LinkProperty object should be cleared before these properties are read.
     * 
     * @param reuseProperties
     */
    public void setReuseProperties(Boolean reuseProperties) {
        this.reuseProperties = reuseProperties;
    }

    /**
     * Calls the hashtable method <code>put</code>. Provided to provide a
     * similar interface in jdk1.1.x or jdk1.2.x, enforcing that only strings
     * can be in properties files.
     */
    public synchronized Object setProperty(String key, String value) {
        return put(key, value);
    }

    /**
     * Write the properties as several strings. There is a string count (Key
     * count + value count), and then for each key and value string, a character
     * count, and the characters.
     * 
     * @param link the link to write to.
     */
    public void write(Link link) throws IOException {
        write(link.dos);
    }

    /**
     * Write the properties as several strings. There is a string count (Key
     * count + value count), and then for each key and value string, a character
     * count, and the characters.
     * 
     * @param dos the DataOutputStream to write to.
     */
    public void write(DataOutputStream dos) throws IOException {

        dos.writeInt((size() + 1) * 2);

        dos.writeInt(LPC_PROPERY_MANAGEMENT_POLICY.length());
        dos.writeChars(LPC_PROPERY_MANAGEMENT_POLICY);
        if (reuseProperties == Boolean.TRUE) {
            dos.writeInt(LPC_REUSE_PROPERTIES.length());
            dos.writeChars(LPC_REUSE_PROPERTIES);
        } else {
            dos.writeInt(LPC_CLEAR_PROPERTIES.length());
            dos.writeChars(LPC_CLEAR_PROPERTIES);
        }

        for (Enumeration e = propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = getProperty(key);
            dos.writeInt(key.length());
            dos.writeChars(key);
            dos.writeInt(value.length());
            dos.writeChars(value);
        }
    }

    /**
     * Read the link to create the properties object. Assumes the properties are
     * the next thing to be read, starting with the string count.
     * 
     * @param dis DataInput to read from.
     * @throws IOException.
     */
    public void read(DataInput dis) throws IOException {
        int numArgs = dis.readInt();
        if (numArgs > 0) {
            readArgs(numArgs, dis);
        }
    }

    /**
     * Read the link to fetch properties for this LinkProperties object. Assumes
     * the property count has been read and is being provided to this method
     * 
     * @param numArgs the number of key + value strings to read.
     * @param dis DataInput to read from.
     * @throws IOException.
     */
    public void readArgs(int numArgs, DataInput dis) throws IOException {

        String[] argStrings = new String[numArgs];

        for (int i = 0; i < numArgs; i += 2) {
            int argLength = dis.readInt();

            if (i == 0 && argLength == 1 && dis.readChar() == LPC_PROPERY_MANAGEMENT_POLICY_CHAR) {
                argLength = dis.readInt();
                if (argLength == 1 && dis.readChar() == LPC_CLEAR_PROPERTIES_CHAR) {
                    clear();
                }
                continue;
            }

            argStrings[i] = LinkUtil.readString(dis, argLength);
            argLength = dis.readInt();
            argStrings[i + 1] = LinkUtil.readString(dis, argLength);

            put(argStrings[i], argStrings[i + 1]);
        }

        if (Debug.debugging("linkdetail")) {
            System.out.println("LinkProperties | Read:  " + this);
        }
    }

    /**
     * New, static method for more efficient property handling.
     * 
     * @param dis
     * @param props
     * @return if there are no properties, the EMPTY_PROPERTIES object is
     *         returned. If there are properties and props == null, then a new
     *         LinkProperties object is allocated and returned, otherwise, props
     *         is returned.
     * @throws IOException
     */
    public static LinkProperties read(DataInput dis, LinkProperties props) throws IOException {

        int numArgs = dis.readInt();

        if (numArgs == 0) {
            return EMPTY_PROPERTIES;
        }

        if (props == null) {
            props = new LinkProperties();
        }

        props.readArgs(numArgs, dis);

        return props;
    }

    /**
     * New, static method for more efficient property handling and loading the
     * properties into the OMGraphic.
     * 
     * @param dis
     * @param omg
     * @return if there are no properties, the EMPTY_PROPERTIES object is
     *         returned. If there are properties and props == null, then a new
     *         LinkProperties object is allocated and returned, otherwise, props
     *         is returned. The OMGraphic appObject is set with the read
     *         properties.
     */
    public static LinkProperties loadPropertiesIntoOMGraphic(DataInput dis, OMGraphic omg,
                                                             LinkProperties propertiesBuffer)
            throws IOException {
        LinkProperties readProperties = (LinkProperties) read(dis, propertiesBuffer).clone();
        readProperties.setProperties(omg); // load them into OMGraphic..
        return readProperties;
    }

    /**
     * Method to call on the LinkProperties object to set the DrawingAttributes
     * properties on an OMGraphic. Will set the line and select colors, fill
     * paints (including patterns) and stroke based on the properties contained
     * in this LinkProperties object. Will set default values in the OMGraphic
     * if the applicable properties aren't defined, and will set the
     * LinkProperties in the AppObject of the OMGraphic.
     */
    public void setProperties(OMGraphic omg) {
        if (omg == null)
            return;

        omg.setLinePaint(getPaint(LPC_LINECOLOR, BLACK_COLOR_STRING));
        omg.setFillPaint(getFillPaint());
        omg.setSelectPaint(getPaint(LPC_HIGHLIGHTCOLOR, BLACK_COLOR_STRING));
        omg.setStroke(getStroke());

        if (this != EMPTY_PROPERTIES) {
            omg.setAppObject(this);
        }
    }

    protected Hashtable renderAttributesCache = new Hashtable();

    public Stroke getStroke() {
        int lineWidth = PropUtils.intFromProperties(this, LPC_LINEWIDTH, 1);
        String strokeString = getProperty(LPC_LINESTYLE);

        int cap = BasicStroke.CAP_SQUARE;
        int join = BasicStroke.JOIN_MITER;
        float miterLimit = 10f;
        float dashPhase = 0f;
        Stroke stroke = null;
        float[] dash = null;

        String strokeCode = "stroke" + lineWidth + strokeString;

        stroke = (Stroke) renderAttributesCache.get(strokeCode);

        if (stroke != null) {
            return stroke;
        }

        if (strokeString != null) {
            if (strokeString.equalsIgnoreCase(LPC_LONG_DASH)) {
                dash = new float[] { 10f, 10f };
            } else if (strokeString.equalsIgnoreCase(LPC_DASH)) {
                dash = new float[] { 6f, 6f };
            } else if (strokeString.equalsIgnoreCase(LPC_DOT)) {
                dash = new float[] { 3f, 6f };
            } else if (strokeString.equalsIgnoreCase(LPC_DASH_DOT)) {
                dash = new float[] { 6f, 6f, 3f, 6f };
            } else if (strokeString.equalsIgnoreCase(LPC_DASH_DOT_DOT)) {
                dash = new float[] { 6f, 6f, 3f, 6f, 3f, 6f };
            }

            if (dash != null) {
                stroke = new BasicStroke(lineWidth, cap, join, miterLimit, dash, dashPhase);
            }
        }

        if (stroke == null) {
            stroke = new BasicStroke(lineWidth);
        }

        renderAttributesCache.put(strokeCode, stroke);

        return stroke;
    }

    public Paint getPaint(String paintProperty, String defaultPaintString) {
        Paint paint = null;

        String paintKey = "paint" + getProperty(paintProperty);

        if (paintProperty != null) {
            paint = (Paint) renderAttributesCache.get(paintKey);

            if (paint == null) {
                paint = (Paint) renderAttributesCache.get("paint" + defaultPaintString);
            }

            if (paint != null) {
                return paint;
            }
        }

        if (paintProperty != null && defaultPaintString != null) {
            paint = ColorFactory.parseColorFromProperties(this, paintProperty, defaultPaintString, true);
        } else {
            paint = Color.black;
        }

        renderAttributesCache.put(paintKey, paint);

        return paint;
    }

    public Paint getFillPaint() {

        Paint fillPaint = getPaint(LPC_FILLCOLOR, CLEAR_COLOR_STRING);
        String fillPatternString = getProperty(LPC_FILLPATTERN);

        if (fillPatternString == null || fillPatternString.equalsIgnoreCase(LPC_SOLID_PATTERN)) {
            return fillPaint;
        } else {
            String fillPaintString = getProperty(LPC_FILLCOLOR);
            String texturePaintKey = "fill" + fillPaintString + fillPatternString;

            if (fillPaintString == null) {
                fillPaint = Color.black;

                TexturePaint ret = (TexturePaint) renderAttributesCache.get("fill" + fillPaint
                        + fillPatternString);

                if (ret != null) {
                    return ret;
                } else {
                    ret = (TexturePaint) renderAttributesCache.get(texturePaintKey);

                    if (ret != null) {
                        return ret;
                    }
                }
            }

            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D big = bi.createGraphics();
            big.setColor(new Color(0, true)); // clear
            big.fillRect(0, 0, 8, 8);
            big.setPaint(fillPaint);

            if (fillPatternString.equalsIgnoreCase(LPC_HORIZONTAL_PATTERN)) {
                big.draw(new Line2D.Double(0, 0, 7, 0));
                big.draw(new Line2D.Double(0, 4, 7, 4));
            } else if (fillPatternString.equalsIgnoreCase(LPC_VERTICAL_PATTERN)) {
                big.draw(new Line2D.Double(0, 0, 0, 7));
                big.draw(new Line2D.Double(4, 0, 4, 7));
            } else if (fillPatternString.equalsIgnoreCase(LPC_CROSS_PATTERN)) {
                big.draw(new Line2D.Double(0, 0, 7, 0));
                big.draw(new Line2D.Double(0, 4, 7, 4));
                big.draw(new Line2D.Double(0, 0, 0, 7));
                big.draw(new Line2D.Double(4, 0, 4, 7));
            } else if (fillPatternString.equalsIgnoreCase(LPC_DIAG_CROSS_PATTERN)) {
                big.draw(new Line2D.Double(0, 0, 7, 7));
                big.draw(new Line2D.Double(0, 4, 3, 7));
                big.draw(new Line2D.Double(4, 0, 7, 3));
                big.draw(new Line2D.Double(0, 7, 7, 0));
                big.draw(new Line2D.Double(0, 3, 3, 0));
                big.draw(new Line2D.Double(4, 7, 7, 4));
            } else if (fillPatternString.equalsIgnoreCase(LPC_BACKWARD_DIAG_PATTERN)) {
                big.draw(new Line2D.Double(0, 0, 7, 7));
                big.draw(new Line2D.Double(0, 4, 3, 7));
                big.draw(new Line2D.Double(4, 0, 7, 3));
            } else if (fillPatternString.equalsIgnoreCase(LPC_FORWARD_DIAG_PATTERN)) {
                big.draw(new Line2D.Double(0, 7, 7, 0));
                big.draw(new Line2D.Double(0, 3, 3, 0));
                big.draw(new Line2D.Double(4, 7, 7, 4));
            } else {
                // default to solid
                big.fillRect(0, 0, 8, 8);
            }

            Rectangle r = new Rectangle(0, 0, 8, 8);
            TexturePaint texturePaint = new TexturePaint(bi, r);

            renderAttributesCache.put(texturePaintKey, texturePaint);
            return texturePaint;
        }
    }
}
