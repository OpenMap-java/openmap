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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMText.java,v $
// $RCSfile: OMText.java,v $
// $Revision: 1.5 $
// $Date: 2003/09/22 23:28:00 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.DrawUtil;


/**
 * The OMText graphic type lets you put text on the screen. The
 * location of the string is really the location of the lower left
 * corner of the first letter of the string.
 */
public class OMText extends OMGraphic implements Serializable {


    //----------------------------------------------------------------------
    // Static constants
    //----------------------------------------------------------------------

    /** Align the text to the right of the location. */
    public final static transient int JUSTIFY_LEFT = 0;

    /** Align the text centered on the location. */
    public final static transient int JUSTIFY_CENTER = 1;

    /** Align the text to the left of the location. */
    public final static transient int JUSTIFY_RIGHT = 2;

    /**
     * Parameter of Font to count toward footprint of height of Text.
     * This indicates that the ascent, descent and leading of the
     * text should count toward the footprint of the text. This is
     * the same as the full height of the FontMetric, and is the
     * default. 
     */
    public final static transient int HEIGHT = 0;

    /**
     * Parameter of Font to count toward footprint of height of Text.
     * This indicates that the ascent and the descent of the text
     * should count toward the footprint of the text. 
     */
    public final static transient int ASCENT_DESCENT = 1;

    /**
     * Parameter of Font to count toward footprint of height of Text.
     * This indicates that the ascent and the leading of the text
     * should count toward the footprint of the text. 
     */
    public final static transient int ASCENT_LEADING = 2;

    /**
     * Parameter of Font to count toward footprint of height of Text.
     * This indicates that just the ascent of the text should count
     * toward the footprint of the text. 
     */
    public final static transient int ASCENT = 3;

    /**
     * Parameter that dictates where the font baseline will be set
     * compared to the location of the OMText.  The BASELINE_BOTTOM
     * setting, the default, means that the location will be set along
     * the normal bottom edge of the text where the letters rest.
     */
    public final static transient int BASELINE_BOTTOM = 0;

    /**
     * Parameter that dictates where the font baseline will be set
     * compared to the location of the OMText.  The BASELINE_MIDDLE
     * setting means that the location will be set along
     * the middle of the height of the text.
     */
    public final static transient int BASELINE_MIDDLE = 1;

    /**
     * Parameter that dictates where the font baseline will be set
     * compared to the location of the OMText.  The BASELINE_TOP
     * setting means that the location will be set along
     * the top of the height of the text.
     */
    public final static transient int BASELINE_TOP = 2;

    public static final Font DEFAULT_FONT = new Font("SansSerif", java.awt.Font.PLAIN, 12);

    //----------------------------------------------------------------------
    // Fields
    //----------------------------------------------------------------------

    /**
     * The projected xy window location of the bottom left corner of
     * the first letter of the text string.
     */
    protected Point pt;

    /** The X/Y point or the offset amount depending on render type. */
    protected Point point;

    /** The Font type that the string should be displayed with. */
    protected Font f;

    /** The latitude location for the text, used for lat/lon or offset
     * rendertype texts, in decimal degrees. */
    protected float lat = 0.0f;

    /** The longitude location for the text, used for lat/lon or offset
     * rendertype texts, in decimal degrees. */
    protected float lon = 0.0f;

    /** The string to be displayed. */
    protected String data = null;

    /**
     * Justification of the string.
     * Will let you put the text to the right, centered or to the left
     * of the given location.
     */
    protected int justify = JUSTIFY_LEFT;

    /**
     * Location of the baseline of the text compared to the location
     * point of the OMText object.  You can set this to be
     * BASELINE_BOTTOM (default), BASELINE_MIDDLE or BASELINE_TOP,
     * depending on if you want the bottom of the letters to be lined
     * up to the location, or the middle or the top of them.
     */
    protected int baseline = BASELINE_BOTTOM;

    /**
     * The fmHeight is the FontMetric height to use for calculating
     * the footprint for the line.  This becomes important for
     * multi-line text and text in decluttering, because it dictates
     * the amount of space surrounding the text. The default height is
     * to take into account the ascent, descent and leading of the
     * font.
     */
    protected int fmHeight = HEIGHT;

//     /** If we display a boundary rectangle around/underneath the text */
//     protected boolean showBounds = false;

    protected boolean useMaxWidthForBounds = false;

    /** The angle by which the text is to be rotated, in radians */
    protected double rotationAngle = DEFAULT_ROTATIONANGLE;

    //----------------------------------------------------------------------
    // Caches
    //    These fields cache computed data.
    //----------------------------------------------------------------------

    /** The bounding rectangle of this Text. */
    protected transient Polygon polyBounds;

    /** The line color for the polyBounds. */
    protected Paint boundsLineColor = linePaint;

    /** The display color for the polyBounds. */
    protected Paint boundsDisplayColor = boundsLineColor;

    /** The Metrics of the current font. */
    protected transient FontMetrics fm;

    /** The text split by newlines. */
    protected transient String parsedData[];

    /** cached string widths. */
    protected transient int widths[];


    //----------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------

    /**
     * Default constructor.  Produces an instance with no location
     * and an empty string for text.  For this instance to be useful
     * it needs text (setData), a location (setX, setY, setLat, setLon)
     * and a renderType (setRenderType).
     */
    public OMText() {
        super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
	point = new Point(0,0);
	setData("");
	f = DEFAULT_FONT;
    }

    /**
     * Creates a text object, with Lat/Lon placement, and default
     * SansSerif font.
     * @param lt latitude of the string, in decimal degrees.
     * @param ln longitude of the string, in decimal degrees.
     * @param stuff the string to be displayed.
     * @param just the justification of the string
     */
    public OMText(float lt, float ln, String stuff, int just) {
	this(lt, ln, stuff, DEFAULT_FONT, just);
    }

    /**
     * Creates a text object, with Lat/Lon placement.
     * @param lt latitude of the string, in decimal degrees.
     * @param ln longitude of the string, in decimal degrees.
     * @param stuff the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string
     */
    public OMText(float lt, float ln, String stuff, 
		  Font font, int just) {

        super(RENDERTYPE_LATLON, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);

	lat = lt;
	lon = ln;
	setData(stuff);
	f = font;
	justify = just;
    }
  
    /**
     * Creates a text object, with XY placement, and default SansSerif
     * font. 
     * @param px1 horizontal window pixel location of the string.
     * @param py1 vertical window pixel location of the string.
     * @param stuff the string to be displayed.
     * @param just the justification of the string
     */
    public OMText(int px1, int py1, String stuff, int just) { 
	this(px1, py1, stuff, DEFAULT_FONT, just);
    }
  
    /**
     * Creates a text object, with XY placement. 
     * @param px1 horizontal window pixel location of the string.
     * @param py1 vertical window pixel location of the string.
     * @param stuff the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string
     */
    public OMText(int px1, int py1, 
		  String stuff, Font font, int just) { 
        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
	point = new Point(px1, py1);
	setData(stuff);
	f = font;
	justify = just;
    }

    /**
     * Creates a Text object, with lat/lon placement with XY offset,
     * and default SansSerif font.
     * @param lt latitude of the string, in decimal degrees.
     * @param ln longitude of the string, in decimal degrees.
     * @param offX horizontal offset of string
     * @param offY vertical offset of string
     * @param aString the string to be displayed.
     * @param just the justification of the string
     */
    public OMText(float lt, float ln, 
		  int offX, int offY, 
		  String aString, int just) { 
	this(lt, ln, offX, offY, aString, DEFAULT_FONT, just);
    }

    /**
     * Creates a Text object, with lat/lon placement with XY offset. 
     * @param lt latitude of the string, in decimal degrees.
     * @param ln longitude of the string, in decimal degrees.
     * @param offX horizontal offset of string
     * @param offY vertical offset of string
     * @param aString the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string
     */
    public OMText(float lt, float ln, 
		  int offX, int offY, 
		  String aString, Font font, 
		  int just) { 
        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
	lat = lt;
	lon = ln;
	point = new Point(offX, offY);
	setData(aString);
	f = font;
	justify = just;
    }

    /**
     * Get the font of the text object. 
     *
     * @return the font of the object.
     */
    public Font getFont() {
	return f;
    }
    
    /**
     * Set the font.  Will take effect on the next render.
     * Flushes the cache fields <code>fm</code>, <code>widths</code>,
     * and <code>polyBounds</code>.
     *
     * @param aFont font to be used for the text.
     *
     * @see #fm
     * @see #widths
     * @see #polyBounds
     */
    public void setFont(Font aFont) {
	f = aFont;

	// now flush the cached information about the old font
	fm = null;		// flush existing metrics.
	widths = null;		// flush existing width table.
	polyBounds = null;	// flush existing bounds.
    }

    /**
     * Get the x location. Applies to XY and OFFSET text objects. 
     *
     * @return the horizontal window location of the string, from the
     * left of the window.
     */
    public int getX() {
	if (point != null) {
	    return point.x;
	} else {
	    return 0;
	}
    }

    /**
     * Set the x location. Applies to XY and OFFSET text objects. 
     *
     * @param newX the horizontal pixel location of the window to place
     * the string.
     */
    public void setX(int newX) {
	if (point == null && getRenderType() == RENDERTYPE_LATLON) {
	    point = new Point();
	    setRenderType(RENDERTYPE_OFFSET);
	}
	point.x = newX;
	setNeedToRegenerate(true);
    }

    /**
     * Get the y location. Applies to XY and OFFSET text objects. 
     *
     * @return the vertical pixel location of the string, from the top
     * of the window.
     */
    public int getY() {
	if (point != null) {
	    return point.y;
	} else {
	    return 0;
	}
    }

    /**
     * Set the y location. Applies to XY and OFFSET text objects. 
     *
     * @param newY the vertical pixel location of the window to place
     * the string.
     */
    public void setY(int newY) {
	if (point == null && getRenderType() == RENDERTYPE_LATLON) {
	    point = new Point();
	    setRenderType(RENDERTYPE_OFFSET);
	}
	point.y = newY;
	setNeedToRegenerate(true);
    }

    /**
     * Get the latitude location of the string. Applies to LATLON
     * and OFFSET text objects.
     *
     * @return the latitude, in decimal degrees.
     */
    public float getLat() {
	return lat;
    }
 
    /**
     * Set the latitude. Applies to LATLON and OFFSET text
     * objects. 
     *
     * @param l latitude for new location, in decimal degrees.
     */
    public void setLat(float l) {
	lat = l;
	setNeedToRegenerate(true);
    }
    
    /**
     * Return the longitude. Applies to LATLON and OFFSET text objects. 
     *
     * @return the longitude location of the string, in decimal degrees.
     */
    public float getLon() {
	return lon;
    }

    /**
     * Set the longitude. Applies to LATLON and OFFSET text objects. 
     *
     * @param l the longitude location for the string, in decimal degrees.
     */
    public void setLon(float l) {
	lon = l;
	setNeedToRegenerate(true);
    }

    /**
     * Not for the faint hearted.  Used by the DeclutterMatrix to
     * replace text on the map after it has been projected.  This
     * method lets the declutter matrix find out where the text should
     * go.  
     * @return Point on the map where the text has been projected to go.
     */
    public Point getMapLocation() {
	return pt;
    }

    /**
     * Not for the faint hearted.  Used by the DeclutterMatrix to
     * replace text on the map after it has been projected.  This
     * method lets the declutter matrix put the text in an uncluttered
     * place.
     * @param Point on the map where the text being placed.  
     */
    public void setMapLocation(Point point) {
	pt = point;
	polyBounds = null;
    }

    /**
     * Return the string. 
     *
     * @return the string
     */
    public java.lang.String getData() {
	return data;
    }

    /**
     * Sets the string contents that are presented. 
     * Flushes the cache fields <code>parsedData</code>, <code>widths</code>,
     * and <code>polyBounds</code>.
     * HACK synchronized so that it doesn't interfere with other methods that
     * are using parsedData.
     *
     * @param d the text to be displayed
     *
     * @see #parsedData
     * @see #widths
     * @see #polyBounds
     */
    public synchronized void setData(java.lang.String d) {
	data = d;

	// now flush the cached information about the old text
	parsedData = null;	// flush existing parsed line table.
	widths = null;		// flush existing width table.
	polyBounds = null;	// flush existing bounds.
    }

    /**
     * Gets the justification of this OMText.
     *
     * @return one of JUSTIFY_LEFT, JUSTIFY_CENTER, JUSTIFY_RIGHT
     */
    public int getJustify() {
	return justify;
    }

    /**
     * Sets the justification of this OMText.
     * Flushes the cache fields <code>fm</code>, <code>widths</code>,
     * and <code>polyBounds</code>.
     *
     * @param j one of JUSTIFY_LEFT, JUSTIFY_CENTER, JUSTIFY_RIGHT
     * @see #polyBounds
     */
    public void setJustify(int j) {
	justify = j;

	// now flush cached information
	polyBounds = null;	// flush existing bounds.
    }

    /**
     * Gets the baseline location of this OMText.
     *
     * @return one of BASELINE_BOTTOM, BASELINE_MIDDLE or BASELINE_TOP.
     */
    public int getBaseline() {
	return baseline;
    }

    /**
     * Sets the location of the baseline of this OMText.
     * Flushes the cache fields <code>fm</code>, <code>widths</code>,
     * and <code>polyBounds</code>.
     *
     * @param b one of BASELINE_BOTTOM, BASELINE_MIDDLE or BASELINE_TOP.
     * @see #polyBounds
     */
    public void setBaseline(int b) {
	baseline = b;

	// now flush cached information
	polyBounds = null;	// flush existing bounds.
    }

    /**
     * Gets the show bounds field.
     *
     * @deprecated use isMatted() instead.
     * @return true if bounds are shown, false if hidden.
     */
    public boolean getShowBounds() {
	return isMatted();
    }

    /**
     * Sets the show bounds field.  When <code>true</code>, the
     * bounding box of this text is displayed.
     *
     * @deprecated use setMatted(boolean) instead.
     * @param show true to show, false to hide.
     * @see #setBoundsLineColor
     * @see #setBoundsFillColor
     * @see #setFillColor
     */
    public void setShowBounds(boolean show) {
	setMatted(show);
    }

    /**
     * Set flag to specify that the bounds, if displayed, should be
     * rectangular.  Only really affects mult-line text.
     * @param value if true, bounds for multi-line text will be
     * retangular instead of closely following text.
     */
    public void setUseMaxWidthForBounds(boolean value) {
	useMaxWidthForBounds = value;	
    }

    /**
     * Get flag to specify that the bounds, if displayed, should be
     * rectangular.  Only really affects mult-line text.
     * @return true if bounds for multi-line text will be
     * retangular instead of closely following text.
     */
    public boolean getUseMaxWidthForBounds() {
	return useMaxWidthForBounds;
    }

    /**
     * Get the text bounds.
     * @return Polygon or null if bounds not calculated yet
     */
    public Polygon getPolyBounds() {
	if (polyBounds == null) {
	    computeBounds();
	}
	return polyBounds;
    }

    /**
     * Set the fill and line color of the text bounds object.
     * This sets both the fill and line color of the background
     * text-bounds to <code>color</code>.
     * @param color Color
     */
    public void setFillPaint(Paint color) { 
	super.setFillPaint(color);
	setBoundsLineColor(color);
    }

    /**
     * Set the fill color of the text bounds object.
     * @param color Color
     */
    public void setBoundsFillColor(Paint color) { 
	super.setFillPaint(color);
    }

    /**
     * Get the fill color of the text bounds object.
     * @return Color
     */
    public Paint getBoundsFillColor() { 
	return super.getFillPaint();
    }

    /** 
     * Set the fmHeight to use for the footprint.
     * @param fmh the setting for fmHeight, out of the parameters
     * stated above.  
     */
    public void setFMHeight(int fmh) {
	fmHeight = fmh;
    }

    /**
     * Get the fmHeight used for the footprint.
     * @return the setting for fmHeight, out of the parameters
     * stated above.  
     */
    public int getFMHeight() {
	return fmHeight;
    }

    /**
     * Set the line color of the text bounds object.
     * @param color Color
     */
    public void setBoundsLineColor(Paint color) { 
	setMattingPaint(color);
// 	boundsLineColor = (color != null) ? color : Color.black;
// 	if (!selected) {
// 	    boundsDisplayColor = boundsLineColor;
// 	}
    }

    /**
     * Get the line color of the text bounds object.
     * @return Color
     */
    public Paint getBoundsLineColor() { 
	return getMattingPaint();
// 	return boundsLineColor;
    }

    /**
     * Get the display color of the text bounds object.
     * @return Color
     */
    public Paint getBoundsDisplayColor() {
       return boundsDisplayColor;
    }

    /**
     * Set the selected attribute to true, and sets the color to the
     * select color.
     */
    public void select() {
	super.select();
	boundsDisplayColor = getSelectPaint();
    }

    /**
     * Set the selected attribute to false, sets the color to the line
     * color.
     */
    public void deselect() {
	super.deselect();
	boundsDisplayColor = boundsLineColor;
    }

    /**
     * Set the angle by which the text is to rotated.
     *
     * @param rotationAngle the number of radians the text is to be rotated.
     * Measured clockwise from horizontal.  
     * @deprecated use setRotationAngle instead.
     */
    public void setTheta(double theta) {
        setRotationAngle(theta);
	setNeedToRegenerate(true);
    }

    /**
     * Get the current rotation of the text.
     *
     * @return the text rotation.
     * @deprecated use getRotationAngle instead.
     */
    public double getTheta() {
        return getRotationAngle();
    }
    
    /**
     * Set the angle by which the text is to rotated.
     *
     * @param angle the number of radians the text is to be
     * rotated.  Measured clockwise from horizontal.  Positive numbers
     * move the positive x axis toward the positive y axis.
     */
    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
	setNeedToRegenerate(true);
    }

    /**
     * Get the current rotation of the text.
     *
     * @return the text rotation.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Prepares the text for rendering.  Determines the location
     * based on the renderType and possibly the projection.
     * Sets the field <code>pt</code>.
     * Flushes the cache field <code>polyBounds</code>.
     *
     * @param proj the projection of the window.
     * @return true if the placement of the string on the window is
     * valid.
     *
     * @see #pt
     */
    public synchronized boolean generate(Projection proj) {
	// HACK synchronized because of various race conditions that need to
	// be sorted out.

	if (proj == null) {
	    Debug.message("omgraphic", "OMText: null projection in generate!");
	    return false;
	}

	// flush the cached information about the bounding box.
	polyBounds = null;

	// Although it most definately has bounds, OMText is considered a
	// point object by the projection code.  We need to check to make
	// sure the point is plot-able: if not then don't display it.  This
	// might occur, for instance, if we're using the Orthographic and the
	// point is on the other side of the world.
	switch (renderType) {
	case RENDERTYPE_XY:
	    pt = point;
	    break;
	case RENDERTYPE_OFFSET:
	    if (!proj.isPlotable(lat, lon)) {
		if (Debug.debugging("omGraphics"))
		    System.err.println(
			"OMText.generate(): offset point is not plotable!");
		setNeedToRegenerate(true);//so we don't render it!
		return false;
	    }
	    pt = proj.forward(lat, lon);
	    pt.translate(point.x, point.y);
	    break;
	case RENDERTYPE_LATLON:
	    if (!proj.isPlotable(lat, lon)) {
		if (Debug.debugging("omGraphics"))
		    System.err.println(
			"OMText.generate(): llpoint is not plotable!");
		setNeedToRegenerate(true);//so we don't render it!
		return false;
	    }
	    pt = proj.forward(lat, lon);
	    break;
	case RENDERTYPE_UNKNOWN:
	    System.err.println(
		"OMText.render.generate(): invalid RenderType");
	    return false;
	}

	if (f == null) {
	    f = DEFAULT_FONT;
	}

	// Compliance with Shape additions to OMGeometry/OMGraphic.
	// If font metrics are set, we can take care of this now.  If
	// this is the first time this OMText is drawn, then we have
	// to put this off until render.  There will be a
	// one-projection lag for font metrics to catch up with any
	// change.
	computeBounds();

	setNeedToRegenerate(false);
	return true;
    }

    protected Projection hackProj = null;

    /**
     * Build a font out of an X Font description string.  This function
     * take this common string format, and pulls the font type, style,
     * and size out of it.
     *
     * @param fontString the X font description.
     */
    public static Font rebuildFont(String fontString) {
        if (fontString.equals("") )
	    return DEFAULT_FONT;
        int fontStyle = Font.PLAIN;
	int fontSize = 12;
        // Taking the X Font-type string and converting the
        // essential parts to a java Font object.

        int start = fontString.indexOf("-", 1)+1;  //skipping first field
	int end = fontString.indexOf("-", start+1);
	String name = fontString.substring(start, end);
	//System.out.println("rebuildFont: Name is " + name);
	if (fontString.indexOf("-bold-") >= 0) fontStyle = Font.BOLD;
	if (fontString.indexOf("-i-") >= 0) fontStyle += Font.ITALIC;
	//System.out.println("rebuildFont: Style is " + fontStyle);
	start = fontString.indexOf("--") + 2;
	end = fontString.indexOf("-", start+1);
	String tmpFontSize = fontString.substring(start, end);
	if (tmpFontSize.indexOf("*") < 0) 
	    fontSize = Integer.parseInt(tmpFontSize);
	//System.out.println("rebuildFont: Size is " + fontSize);	
	return new Font(name, fontStyle, fontSize);
    }

    /** 
     * In some applications, fonts are represented by a string.
     * Traditionally, with MATT, the font was a X representation of a
     * font.  That's what is being done here - we're taking the Font
     * structure, and then going to XFont type text structure.  Dashes
     * need to be included, line feeds are not.  They are here only
     * for readability.  The rebuildFont method brings this back to a
     * java Font.  
     *
     * @param font the Java font to convert to an XFont string.
     * @return the font as a string.
     */
    public static String fontToXFont(java.awt.Font font) {
	//-foundry(who made it)
	StringBuffer ret = new StringBuffer("-*");
	//-font family(name)
	ret.append("-"+font.getName());
	//-weight(bold, medium)
	if(font.isBold()) ret.append("-bold");
	else ret.append("-normal");
	//-slant(o,i)
	if(font.isItalic()) ret.append("-i");
	else ret.append("-o");
	//-set width(normal, condensed, narrow, double width)
	ret.append("-normal");
	//--pixels(height)
	ret.append("--"+ font.getSize());
	//-points(in tenths of a point, related to screen)
	ret.append("-*");
	//-horizontal resolution in dpi
	ret.append("-*");
	//-vertical resolution in dpi
	ret.append("-*");
	//-spacing(m-monospace or p-proportional)
	ret.append("-*");
	//-average width(of each letter, in tenths of a pixel)
	ret.append("-*");
	//-character set(like an ISO designation.
	ret.append("-*");
	// System.out.println("SText.fontString: " + ret);
	return ret.toString();
    }

    /**
     * Counts occurences of a character in a string.
     *
     * @param str the String
     * @param ch the character to count
     * @return the number of occurences
     */
    protected int countChar(String str, int ch) {
	int fromIndex = 0;
	int count = 0;

	while ((fromIndex = str.indexOf(ch, fromIndex)) != -1) {
	    count++;
	    fromIndex++;	// increment past current index
				// so we don't pick up the same
				// instance again.
	}
	return count;
    }

    /**
     * Breaks the text down into separate lines.
     * Sets the cache field <code>parsedData</code>.
     *
     * @see #parsedData
     */
    protected void parseData() {
	if (parsedData == null) {

	    if (data == null) data = "";

	    int nLines = countChar(data, '\n') + 1;
	    if (nLines <= 1) {
		parsedData = new String[1];
		parsedData[0] = data;
	    } else {
		int i = 0;
		int fromIndex = 0;
		int toIndex = 0;
		parsedData = new String[nLines];

		while ( (toIndex = data.indexOf('\n', fromIndex)) != -1) {
		    parsedData[i] = data.substring(fromIndex, toIndex);
		    fromIndex = toIndex+1;
		    i++;
		}
		parsedData[nLines - 1] = data.substring(fromIndex,
							data.length());
	    }
	}
    }

    /**
     * Computes the widths of each line of the text.
     * Sets the cache field <code>widths</code>.
     *
     * @param fm the metrics to use for computation.
     *
     * @see #widths
     */
    protected void computeStringWidths(FontMetrics fm) {
	if (widths == null && fm != null) {
	    int nLines = parsedData.length;
	    widths = new int[nLines];
	    for (int i=0; i<nLines; i++) {
		widths[i] = fm.stringWidth(parsedData[i]);
	    }
	}
    }

    /** 
     * This function can be called to initialize the internals such
     * as height and width of OMText.  Lets you use the graphics, and
     * thus the FontMetrics object, to figure out the dimensions of
     * the text in order to manipulate the placement of the text on
     * the map.  These internals were otherwise initialized only when
     * render function was called.  
     * @param g the java.awt.Graphics to put the string on.  
     */
    public synchronized void prepareForRender(Graphics g) {
	parseData();
	g.setFont(f);
	
	if (fm == null)	{
	    fm = g.getFontMetrics();
	}
	computeBounds();		
    }

    /**
     * Given a java.awt.Graphics object, set the Stroke and Paint
     * parameters of it to match the OMGraphic's bounds settings.
     *
     * @param g java.awt.Graphics
     * @see #setGraphicsColor
     */
    public void setGraphicsForBounds(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D)g).setStroke(BASIC_STROKE);
        }
        setGraphicsColor(g, getBoundsDisplayColor());
    }

    /**
     * Renders the text onto the given graphics.
     * Sets the cache field <code>fm</code>.
     *
     * @param g the java.awt.Graphics to put the string on.
     *
     * @see #fm
     */
    public synchronized void render(Graphics g) {

        // copy the graphic, so our transform doesn't cascade to
        // others...
        g = g.create();

	if (getNeedToRegenerate() || pt == null || !isVisible()) return;
	

	g.setFont(f);
	setGraphicsForEdge(g);

	if (fm == null) {
	    fm = g.getFontMetrics();
	}

	computeBounds();

	// to use later to unset the transform, if used.
	double rx = 0.0;
	double ry = 0.0;
	double rw = 0.0;
	double rh = 0.0;
	double woffset = 0.0;

	if (g instanceof Graphics2D && 
	    rotationAngle != DEFAULT_ROTATIONANGLE) {

	    Rectangle rect = polyBounds.getBounds();

	    rx = rect.getX();
	    ry = rect.getY();
	    rw = rect.getWidth();
	    rh = rect.getHeight();
	    woffset = 0.0;
	    
	    switch  (justify) {
	    case JUSTIFY_LEFT:
		// woffset = 0.0;
		break;
	    case JUSTIFY_CENTER:
		woffset = rw / 2;
		break;
	    case JUSTIFY_RIGHT:
		woffset = rw;
	    }
	    //rotate about our text anchor point
	    ((Graphics2D)g).rotate(rotationAngle, rx+woffset, pt.y);
	}

	if (isMatted()) {
	    Paint p = getMattingPaint();
	    setGraphicsColor(g, getMattingPaint());
	    fill(g);
	} else if (shouldRenderFill()) {
	    setGraphicsForFill(g);
	    fill(g);
	    
	    if (textureMask != null && textureMask != fillPaint) {
		setGraphicsColor(g, textureMask);
		fill(g);
	    }
	}

// 	// display bounding box
// 	if (showBounds) {
// 	    // This paint stuff allows the border to be a different
// 	    // color than the text
// 	    Paint textPaint = getLinePaint();
// 	    setLinePaint(boundsLineColor);
// 	    super.render(g);
// 	    setLinePaint(textPaint);
// 	}

	setGraphicsForEdge(g);

	int height;
	if (fmHeight == HEIGHT) {
	    height = fm.getHeight();
	} else if (fmHeight == ASCENT_LEADING) {
	    height = fm.getHeight() - fm.getDescent();
	} else if (fmHeight == ASCENT_DESCENT) {
	    height = fm.getAscent() + fm.getDescent();
	} else {
	    height = fm.getAscent();
	}

	int baselineLocation = pt.y; // baseline == BASELINE_BOTTOM, normal.

	if (baseline == BASELINE_MIDDLE) {
	    baselineLocation += fm.getDescent()/2;
	} else if (baseline == BASELINE_TOP) {
	    baselineLocation += fm.getDescent();
	}

	switch (justify) {
	case JUSTIFY_LEFT:
	    // Easy case, just draw them.
	    for (int i=0; i<parsedData.length; i++) {
		g.drawString(parsedData[i], pt.x, baselineLocation + (height*i));
	    }
	    break;
	case JUSTIFY_CENTER:
	    computeStringWidths(fm);
	    for (int i=0; i<parsedData.length; i++) {
		g.drawString(parsedData[i],
			     pt.x - (widths[i]/2),
			     baselineLocation + (height*i));
	    }
	    break;
	case JUSTIFY_RIGHT:
	    computeStringWidths(fm);
	    for (int i=0; i<parsedData.length; i++) {
		g.drawString(parsedData[i],
			     pt.x - widths[i],
			     baselineLocation + (height*i));
	    }
	    break;
	}
    }

    /**
     * Computes the bounding polygon.
     * Sets the cache field <code>polyBounds</code>.
     *
     * @see #polyBounds
     */
    protected void computeBounds() {
	if (parsedData == null) {
	    parseData();
	}

	if (polyBounds == null && pt != null && fm != null) {

	    // 	    System.out.println("\tcomputing poly bounds");

	    int xoffset = 0;
	    int i;
	    
 	    int height;
	    int descent;
	    if (fmHeight == HEIGHT) {
		height = fm.getHeight();
		descent = fm.getDescent();
	    } else if (fmHeight == ASCENT_DESCENT) {
		height = fm.getAscent();
		descent = fm.getDescent();
	    } else if (fmHeight == ASCENT_LEADING) {
		height = fm.getHeight() - fm.getDescent();
		descent = 0;
	    } else {
		height = fm.getAscent();
		descent = 0;
	    }

	    int nLines = parsedData.length;
	    polyBounds = new Polygon();

	    computeStringWidths(fm);

	    int baselineOffset = 0; // baseline == BASELINE_BOTTOM, normal.

	    if (baseline == BASELINE_MIDDLE) {
		baselineOffset = descent/2;
	    } else if (baseline == BASELINE_TOP) {
		baselineOffset = descent;
	    }

	    // or, baselineOffset = height - (baseline * height /2);
	    // But that depends on the actual values of the BASELINE
	    // values, which doesn't seem safe.

	    /* pt.y is bottom of first line, currenty is initialized
	       to top of first line, minus any offset introduced by
	       baseline adjustments. */
	    int currenty = pt.y + descent - height - baselineOffset;

	    // First, all the line endpoints.
	    for (i=0; i<nLines; i++) {

		switch (justify) {
		case JUSTIFY_LEFT:
		    xoffset = widths[i];
		    break;
		case JUSTIFY_CENTER:
		    xoffset = widths[i]/2;
		    break;
		case JUSTIFY_RIGHT:
		    xoffset = 0;
		    break;
		}

		// top of line
		polyBounds.addPoint(pt.x + xoffset, currenty);
		currenty += height;
		// bottom of line
		polyBounds.addPoint(pt.x + xoffset, currenty);
	    }

	    // Next, all line startpoints (the left side)
	    for (i=nLines-1; i >= 0; i--) {
		switch (justify) {
		case JUSTIFY_LEFT:
		    xoffset = 0;
		    break;
		case JUSTIFY_CENTER:
		    xoffset = -widths[i]/2;
		    break;
		case JUSTIFY_RIGHT:
		    xoffset = -widths[i];
		    break;
		}
		polyBounds.addPoint(pt.x + xoffset, currenty);
		currenty -= height;
		polyBounds.addPoint(pt.x + xoffset, currenty);
	    }

	    if (polyBounds != null) {
		if (useMaxWidthForBounds) {
		    shape = new GeneralPath(polyBounds.getBounds());
		} else {
		    shape = new GeneralPath(polyBounds);
		}
	    }

	} else {
	    if (Debug.debugging("omtext")) {
		Debug.output("OMText.computeBounds() didn't compute because polybounds = " + polyBounds + " or  pt = " + pt + " or fm = " + fm + ", (only polybounds should be null)");
	    }
	}
    }

    /**
     * Return the shortest distance from the OMText to an
     * XY-point. <p>
     *
     * This method uses the OMText's internal Shape object, created
     * from the boundary of the text, as its boundary.
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point.
     * Returns Float.POSITIVE_INFINITY if the graphic isn't ready
     * (ungenerated).
     */
    public float distance(int x, int y) {
	return _distance(x, y);
    }
}
