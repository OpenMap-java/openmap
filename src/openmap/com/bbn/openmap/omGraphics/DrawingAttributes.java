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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/DrawingAttributes.java,v $
// $RCSfile: DrawingAttributes.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;


/*  Java Core  */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.*;

/* OpenMap */
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.*;

/** 
 * DrawingAttributes provides a mechanism for loading and managing
 * different drawing attributes that may be used.  Several layers need
 * to be able to have Properties define how objects should be
 * drawn, and the list of these drawing attributes tend to be the
 * same.  The DrawingAttributes class fishes out the applicable
 * properties for you, creates the objects needed, and then lets you
 * get those objects when needed. <P>
 *
 * The list of properties that the DrawingAttributes object can handle
 * are listed below.  If a property is not set, the default value will
 * be used.
 *
 * <pre>
 * # The Edge or Line color
 * lineColor=AARRGGBB (Hex ARGB Color, black is default)
 * # The Fill color for 2D shapes
 * fillColor=AARRGGBB (Hex ARGB Color, clean is default)
 * # The Text Color for objects where any text should be different than the line color.
 * textColor=AARRGGBB (Hex ARGB Color, black is default)
 * # A highlight color to switch a graphic to when "selected".
 * selectColor=AARRGGBB (Hex ARGB Color, black is default)
 * # A file or URL that can be used for a fill pattern, in place of the fill color.
 * fillPattern=file://file (default is N/A)
 * # The line width of the edge of the graphic
 * lineWidth=int (1 is default)
 * # A pattern to use for a dashed line, reflected as a
 * # space-separated list of numbers, which are interpreted as on dash
 * # length, off dash length, on dash length, etc.  
 * dashPattern=10 5 3 5 (5 5 is the default if an error occurs reading the numbers, a non-dashed line is the default.)  
 * The phase for the dash pattern,
 * dashPhase=0.0f (0 is the default)
 * # The scale to use for certain measurements, so that fill patterns
 * # can be scaled depending on the map scale compaired to the
 * # baseScale.
 * baseScale=XXXXXX (where 1:XXXXXX is the scale to use.  N/A for the default).
 */
public class DrawingAttributes 
    implements ActionListener, Serializable, Cloneable, PropertyConsumer,
      PropertyChangeListener {

    /** The name of the property that holds the line paint of the graphics. */
    public final static String linePaintProperty = "lineColor";
    /** The name of the property that holds the text paint for Text,
     *  in case that should be different for labels, etc.. */
    public final static String textPaintProperty = "textColor";
    /** The name of the property that holds the fill paint of the graphics. */
    public final static String fillPaintProperty = "fillColor";
    /** The name of the property that holds the select paint of the
     *  graphics, which is the line paint that gets set with the
     *  default OMGraphic.select() action. */
    public final static String selectPaintProperty = "selectColor";
    /** The property that specifies an URL or file a image file to be
     *  used to construct the Paint object for a texture fill pattern.
     *  If the fillPattern is null, the fillPaint will be used. */
    public static final String fillPatternProperty = "fillPattern";
    /** The name of the property that holds the lineWidth of the graphics. */
    public final static String lineWidthProperty = "lineWidth";
    /** The name of the property that holds a dashed pattern for
     *  lines. This will be used to build the stroke object for
     *  lines. This pattern should be two space-separated numbers, the
     *  first representing the pixel length of the line in the dash,
     *  the second being the space pixel length of the dash. */
    public final static String dashPatternProperty = "dashPattern";
    /** The name of the property that holds a dashed phase for
     *  lines. This will be used to build the stroke object for
     *  lines. */
    public final static String dashPhaseProperty = "dashPhase";
    /** The base scale to use for the image provided for the fill
     *  pattern.  As the scale of the map changes, the base scale can
     *  be used as a reference to change the resolution of the
     *  pattern. This scale will also be used for strokes. */
    public static final String baseScaleProperty = "baseScale";
    /** Set whether a thin black matting should be drawing around the
     *  OMGraphic.*/
    public static final String mattedProperty = "matted";

    public final static int NONE = -1;

    /** The default line paint. (black) */
    public final static String defaultLinePaintString = "0"; // black
    /** The default fill paint. (none) */
    public final static String defaultFillPaintString = "-1"; // none
    /** The default fill paint. (none) */
    public final static String defaultSelectPaintString = "0"; // black
    /** The default line width */
    public final static float defaultLineWidth = 1f;
    /** The default dash phase, which is zero. */
    public final static float defaultDashPhase = 0f;
    /** The defaule dash length, for opaque and transparent parts. */
    public final static float defaultDashLength = 5f;

    /** The paint to outline the shapes. */
    protected Paint linePaint = Color.black;
    /** The paint for text.  Default to black. */
    protected Paint textPaint = linePaint;
    /** The select paint for the shapes. */
    protected Paint selectPaint = Color.black;
    /** The paint to fill the shapes. */
    protected Paint fillPaint = OMColor.clear;
    /**
     * A TexturePaint pattern, if defined. Overrules fillPaint if
     * fillPaint is null or clear. 
     */
    protected TexturePaint fillPattern = null;
    /** The line stroke, for dashes, etc. */
    protected Stroke stroke = new BasicStroke(1);
    /**
     * The base scale for scaling the fill pattern image. If NONE,
     * then the resolution of the raw image will always be used. 
     */
    protected float baseScale = NONE;
    /**
     * Whether a thin black matting line should be rendered around the
     * OMGraphic.
     */
    protected boolean matted = false;

    protected String propertyPrefix = null;
    String fPattern = null; // for writing out the properties
    public final static DrawingAttributes DEFAULT = new DrawingAttributes();

    protected PropertyChangeSupport propertyChangeSupport = null;

    /** The organizer for the palette. */
    protected JPanel palette = null;
    /** Command for line color string adjustments. */
    public final static String LineColorCommand = "LineColor";
    /** Command for fill color string adjustments. */
    public final static String FillColorCommand = "FillColor";
    /** Command for select  color string adjustments. */
    public final static String SelectColorCommand = "SelectColor";
    /** Command for adding matting. */
    public final static String MattedCommand = "MattedCommand";

    private JButton lineColorButton;
    private JButton fillColorButton;
    private JButton selectColorButton;
    private JCheckBox mattedCheckBox;
    private JComboBox lineCombo;
    private JComboBox dashCombo;

    protected final static int icon_width = 20;
    protected final static int icon_height = 20;

    public static boolean alwaysSetTextToBlack = false;

    protected BasicStrokeEditor bse;

    /**
     * Create a DrawingAttributes with the default settings - clear
     * fill paint and pattern, sold black edge line of width 1.  
     */
    public DrawingAttributes() {
	setProperties(null, null);
    }

    /**
     * Create the DrawingAttributes and call setProperties without a prefix for
     * the properties.  Call setProperties without a prefix for the properties.
     * @param props the Properties to look in.  
     */
    public DrawingAttributes(Properties props) {
	setProperties(null, props);
    }

    /**
     * Create the DrawingAttributes and call setProperties with a prefix for
     * the properties.  
     * @param prefix the prefix marker to use for a property, like
     * prefix.propertyName.  The period is added in this function.
     * @param props the Properties to look in.  
     */
    public DrawingAttributes(String prefix, Properties props) {
	setProperties(prefix, props);
    }

    /**
     * Shallow clone.
     */
    public Object clone() {
	DrawingAttributes clone = new DrawingAttributes();
	setTo(clone);
	return clone;
    }

    /**
     * Shallow.
     */
    public void setTo(DrawingAttributes clone) {
	clone.linePaint = linePaint;
	clone.textPaint = textPaint;
	clone.selectPaint = selectPaint;
	clone.fillPaint = fillPaint;
	clone.fillPattern = fillPattern;
	clone.setStroke(stroke);
	clone.baseScale = baseScale;
	clone.matted = matted;
    }

    public boolean equals(DrawingAttributes da) {
	return (da.linePaint == linePaint && 
		da.textPaint == textPaint && 
		da.selectPaint == selectPaint && 
		da.fillPaint == fillPaint && 
		da.fillPattern == fillPattern && 
		da.stroke == stroke && 
		da.baseScale == baseScale &&
		da.matted == matted);
    }

    /**
     * If you want to get a DEFAULT DrawingAttributes object that you
     * may modify, get your own copy.
     */
    public static DrawingAttributes getDefaultClone() {
	return (DrawingAttributes) DEFAULT.clone();
    }

    /**
     * Call setProperties without a prefix for the properties.
     * @param props the Properties to look in.
     * @deprecated use setProperties(props).
     */
    public void init(Properties props) {
	setProperties(null, props);
    }

    /**
     * Look at the Properties, and fill in the drawing attributes
     * based in it's contents.  If a property is not in the
     * properties, it's set to its default setting. 
     *
     * @param prefix the prefix marker to use for a property, like
     * prefix.propertyName.  The period is added in this function.
     * @param props the Properties to look in.
     * @deprecated use setProperties(prefix, props).
     */
    public void init(String prefix, Properties props) {
	setProperties(prefix, props);
    }

    /**
     * Set the Stroke to use for the edge of a graphic.
     */
    public void setStroke(Stroke stroke) {
	Stroke oldStroke = this.stroke;
	this.stroke = stroke;

	if (stroke instanceof BasicStroke) {
	    BasicStrokeEditor tmpbse = getBasicStrokeEditor();
	    if (tmpbse != null) {
		tmpbse.setBasicStroke((BasicStroke)stroke);
	    }
	}	

	if (propertyChangeSupport != null) {
	    propertyChangeSupport.firePropertyChange("stroke", 
						     oldStroke, 
						     stroke);
	}
    }

    /**
     * Get the Stroke used for the lines of a graphic.
     */
    public Stroke getStroke() {
	return stroke;
    }

    /**
     * Get the Stroke object, scaled for comparison to the base
     * scale. If the base scale equals NONE, it's the same as
     * getStroke().
     *
     * @param scale scale to compare to the base scale.  
     */
    public Stroke getStrokeForScale(float scale) {
	if (baseScale != NONE && stroke instanceof BasicStroke) {
	    BasicStroke bs = (BasicStroke)stroke;
	    float lineWidth = bs.getLineWidth();
	    float[] dash = bs.getDashArray();
	    float scaleFactor = scale/baseScale;
	    int endCaps = bs.getEndCap();
	    int lineJoins = bs.getLineJoin();
	    float miterLimit = bs.getMiterLimit();

	    lineWidth *= scaleFactor;
	    for (int i = 0; i < dash.length; i++) {
		dash[i] *= scaleFactor;
	    }

	    return new BasicStroke(lineWidth, 
				   endCaps, lineJoins, miterLimit, 
				   dash, bs.getDashPhase());
	}
	return stroke;
    }

    /**
     * Get the Paint for these attributes, and scale it for the scale
     * compaired to the base scale set if the fill Paint is a
     * TexturePattern.  If the base scale equals NONE, or if the Paint
     * is not a TexturePaint, it's the same as getFillPaint().
     *
     * @param scale scale to compare to the base scale.  
     * @return a Paint object to use for the fill, scaled if
     * necessary.
     */
    public Paint getFillPaintForScale(float scale) {
	if (fillPattern != null) {
	    if (baseScale != NONE) {
		BufferedImage bi = fillPattern.getImage();
		float scaleFactor = scale/baseScale;
		Image image = bi.getScaledInstance((int)(bi.getWidth()*scaleFactor), 
						   (int)(bi.getHeight()*scaleFactor),
						   Image.SCALE_SMOOTH);
		try {
		    bi = BufferedImageHelper.getBufferedImage(image, 0, 0, -1, -1);
		    
		    return new TexturePaint(bi, new Rectangle(0,0, bi.getWidth(), 
							      bi.getHeight()));
		} catch (InterruptedException ie) {
		    Debug.error("DrawingAttributes: Interrupted Exception scaling texture paint");
		}
	    }
	    return fillPattern;
	} else {
	    return fillPaint;
	}
    }

    /**
     * Set the edge paint for the graphics created for the coverage
     * type.
     * @param lPaint the paint.  
     */
    public void setLinePaint(Paint lPaint) {
	Paint oldPaint = linePaint;
	linePaint = lPaint;

	if (lPaint instanceof Color && lineColorButton != null) {
 	    lineColorButton.setBackground((Color)linePaint);
	    lineColorButton.setForeground(calculateTextColor((Color)linePaint));
	}

	propertyChangeSupport.firePropertyChange("linePaint", 
						 oldPaint, 
						 linePaint);
    }

    /**
     * Get the line paint for the graphics created for the coverage
     * type.
     * @return the line paint to use for the edges.
     */
    public Paint getLinePaint() {
	return linePaint;
    }

    /**
     * Set the selected edge paint for the graphics created for the coverage
     * type.
     * @param sPaint the paint.  
     */
    public void setSelectPaint(Paint sPaint) {
	Paint oldPaint = selectPaint;
	selectPaint = sPaint;
	
	if (sPaint instanceof Color && selectColorButton != null) {
 	    selectColorButton.setBackground((Color)selectPaint);
	    selectColorButton.setForeground(calculateTextColor((Color)selectPaint));
	}

	propertyChangeSupport.firePropertyChange("selectPaint", 
						 oldPaint, 
						 selectPaint);
    }

    /**
     * Get the line paint for the graphics created for the coverage
     * type.
     * @return the select line paint to use for the edges.
     */
    public Paint getSelectPaint() {
	return selectPaint;
    }

    /**
     * Set the fill paint for the graphics created for the coverage
     * type.
     * @param fPaint the paint.  
     */
    public void setFillPaint(Paint fPaint) {
	Paint oldPaint = fillPaint;
	fillPaint = fPaint;
	
	if (fillPaint instanceof Color && fillColorButton != null) {
 	    fillColorButton.setBackground((Color)fillPaint);
	    fillColorButton.setForeground(calculateTextColor((Color)fillPaint));
	}

	propertyChangeSupport.firePropertyChange("fillPaint", 
						 oldPaint, 
						 fillPaint);
    }

    /**
     * Get the fill paint for the graphics created for the coverage
     * type.  This used to return the fillPattern if it was defined.
     * Now, it always returns the fillPaint.
     *
     * @return the fill paint to use for the areas.  
     */
    public Paint getFillPaint() {
	return fillPaint;
    }

    /**
     * Set the fill pattern TexturePaint to be used as the fill color.
     * If not null, the fillPattern will be returned from
     * getFillPaint() instead of fillPaint.
     *
     * @param fPattern the TexturePaint to set.  
     */
    public void setFillPattern(TexturePaint fPattern) {
	Paint oldPattern = fPattern;
	fillPattern = fPattern;
	
	if (fillColorButton != null) {
	    // GUI doesn't handle fill patterns yet.
	}

	propertyChangeSupport.firePropertyChange("fillPattern", 
						 oldPattern, 
						 fillPattern);
    }

    /**
     * Get the TexturePaint set as the fill pattern.
     * 
     * @return TexturePaint.
     */
    public TexturePaint getFillPattern() {
	return fillPattern;
    }

    /**
     *  Set the base scale to use for the texture paint and stroke.
     *  If this is set to a negative number, then no scaling of the
     *  paint or stroke will be performed.
     * @param bScale the base scale to use - 1:bScale.
     */
    public void setBaseScale(float bScale) {
	if (bScale > 0) {
	    baseScale = bScale;
	} else {
	    baseScale = NONE;
	}
    }

    /**
     * Get the base scale that the texture paint and dashes are set
     * for.  If the texture paint and stroke are asked for with a
     * scale, those values will be adjusted accordingly.
     * @return base scale for paint and stroke.  
     */
    public float getBaseScale() {
	return baseScale;
    }

    /**
     * Return whether the OMGraphic has matting around the edge.
     */
    public boolean isMatted() {
	return matted;
    }

    /**
     * Set whether the OMGraphic should have matting around the edge.
     */
    public void setMatted(boolean set) {
	boolean oldMatted = matted;
	matted = set;

	if (mattedCheckBox != null) {
	    mattedCheckBox.setSelected(matted);
	}

	propertyChangeSupport.firePropertyChange("matted", 
						 oldMatted, 
						 matted);
    }

    /**
     * Set the DrawingAttributes parameters based on the current
     * settings of an OMGraphic.  
     */
    public void setFrom(OMGraphic graphic) {

	matted = graphic.isMatted();

	if (graphic instanceof OMText) {
	    // Seems like we should have a setTextPaint method for 
	    // this, but it's not in the GUI, so it can wait for now.
	    textPaint = graphic.getLinePaint();
	} else {
	    linePaint = graphic.getLinePaint();
	}

	selectPaint = graphic.getSelectPaint();

	fillPaint = graphic.getFillPaint();
	fillPattern = graphic.getTextureMask();

	// Need to put this in to keep the gui up to date.  Calling
	// setStroke fires off a propertyChange reaction that
	// potentially harms other parameters, like renderType.
	stroke = graphic.getStroke();

	resetGUI();

	if (propertyChangeSupport != null) {
	    propertyChangeSupport.firePropertyChange("all", true, true);
	}
    }

    /**
     * Set all the attributes for the graphic that are contained
     * within this DrawingAttributes class.<P>
     *
     * If the fillPattern is set to a TexturePaint, and the fillPaint
     * is null or clear, then the fillPattern will be set as the fill
     * paint.  Otherwise, the fillPaint will be set in the OMGraphic,
     * and the fillPattern will be set too.  If the
     * OMGraphic.textureMask is != null, then it will get painted on
     * top of the fillPaint.  Makes for effects if the fillPattern has
     * some transparent spots.
     * 
     * @param graphic OMGraphic.  
     */
    public void setTo(OMGraphic graphic) {
	setOMGraphicEdgeAttributes(graphic);

	// If the fillPattern is set to a TexturePaint, and the
	// fillPaint is null or clear, then the fillPattern will be
	// set as the fill paint.  Otherwise, the fillPaint will be
	// set in the OMGraphic, and the fillPattern will be set too.
	// If the OMGraphic.textureMask is != null, then it will get
	// painted on top of the fillPaint.  Makes for effects if the
	// fillPattern has some transparent spots.
	if (fillPattern != null && 
	    (fillPaint == null || OMGraphic.isClear(fillPaint))) {
	    graphic.setFillPaint(fillPattern);
	} else {
	    graphic.setFillPaint(fillPaint);
	    graphic.setTextureMask(fillPattern);
	}

	graphic.setMatted(matted);
    }

    /**
     * Set the graphic attributes that only pertain to boundaries.
     * This is good for polylines, where setting the fill paint will
     * close up the polyline making it a polygon.  So if you want to
     * paint edge data, use this function.  Sets line paint, line
     * width, and stroke if graphic is a OMGraphic 
     *
     * @param graphic OMGraphic
     */
    public void setOMGraphicEdgeAttributes(OMGraphic graphic) {
	if (graphic instanceof OMText) {
	    graphic.setLinePaint(textPaint);
	} else {
	    graphic.setLinePaint(linePaint);
	}
	graphic.setSelectPaint(selectPaint);

	if (stroke != null) {
	    graphic.setStroke(stroke);
	} else {
	    graphic.setStroke(OMGraphic.BASIC_STROKE);
	}
    }

    /**
     * Set all the attributes for the graphic that are contained
     * within this DrawingAttributes class.  Get the TexturePaint for
     * these attributes, and scale it for the scale compaired to the
     * base scale set.  If the base scale equals NONE, the fill
     * pattern is not changed with relation to scale.
     *
     * @param graphic OMGraphic.  
     * @param scale scale to compare to the base scale.  
     */
    public void setOMGraphicAttributesForScale(OMGraphic graphic, 
					       float scale) {
	setOMGraphicEdgeAttributesForScale(graphic, scale);
	graphic.setFillPaint(getFillPaintForScale(scale));
    }

    /**
     * Set the graphic attributes that only pertain to boundaries.
     * This is good for polylines, where setting the fill paint will
     * close up the polyline making it a polygon.  So if you want to
     * paint edge data, use this function.  Sets line paint, line
     * width, and stroke if graphic is a OMGraphic 
     * The stroke, if the base scale is set, is adjusted accordingly.
     *
     * @param graphic OMGraphic.  
     * @param scale scale to compare to the base scale.  
     */
    public void setOMGraphicEdgeAttributesForScale(OMGraphic graphic, float scale) {

	if (graphic instanceof OMText) {
	    graphic.setLinePaint(textPaint);
	} else {
	    graphic.setLinePaint(linePaint);
	}

	graphic.setSelectPaint(selectPaint);
	if (stroke != null) {
	    graphic.setStroke(getStrokeForScale(scale));
	} else {
	    graphic.setStroke(OMGraphic.BASIC_STROKE);
	}
    }

    /**
     * A lock to use to limit the number of JColorChoosers that can
     * pop up for a given DrawingAttributes GUI.  
     */
    private boolean colorChooserLock = false;
    
    /**
     * Get the lock to use a JColorChooser.  Returns true if you got
     * the lock, false if you didn't.
     */
    protected synchronized boolean getLock() {
	if (colorChooserLock == false) {
	    colorChooserLock = true;
	    return colorChooserLock;
	} else {
	    return false;
	}
    }

    /**
     * Release the lock on the JColorChooser.
     */
    protected synchronized void releaseLock() {
	colorChooserLock = false;
    }

    /**
     *  The DrawingAttributes method for handling ActionEvents.  Used
     *  to handle the GUI actions, like changing the colors, line
     *  widths, etc.
     */
    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();
	String command = e.getActionCommand();

	Paint oldPaint, tmpPaint;
	if (command == LineColorCommand && 
	    linePaint instanceof Color) {
	    tmpPaint = getNewPaint((Component)source, "Choose Line Color", 
				   (Color)linePaint);
	    if (tmpPaint != null) {
		setLinePaint(tmpPaint);
	    }
	    
	} else if (command == FillColorCommand && 
		   fillPaint instanceof Color) {
	    tmpPaint = getNewPaint((Component)source, "Choose Fill Color", 
				   (Color)fillPaint);
	    if (tmpPaint != null) {
		setFillPaint(tmpPaint);
	    }
	    
	} else if (command == SelectColorCommand && 
		   selectPaint instanceof Color) {
	    tmpPaint = getNewPaint((Component)source, "Choose Select Color", 
				   (Color)selectPaint);
	    if (tmpPaint != null) {
 		setSelectPaint(tmpPaint);
	    }
	} else if (command == MattedCommand) {
	    JCheckBox check = (JCheckBox)e.getSource();
	    setMatted(check.isSelected());
	} else {
	    if (Debug.debugging("drawingattributes")) {
		Debug.output("DrawingAttributes.actionPerformed: unrecognized command > " + command);
	    }
	}
    }

    public void PropertyChange(PropertyChangeEvent event) {
	// getSource();
	// getNewValue();
    }

    /**
     * A convenience method to get a color from a JColorChooser.  Null
     * will be returned if the JColorChooser lock is in place, or if
     * something else is done where the JColorChooser would normally
     * return null.
     *
     * @param source the source component for the JColorChooser.
     * @param title the String to label the JColorChooser window.
     * @param startingColor the color to give to the JColorChooser to
     * start with.  Returned if the cancel button is pressed.  
     * @return Color chosen from the JColorChooser, null if lock for
     * chooser can't be sequired.
     */
    protected Color getNewPaint(Component source, String title, 
				Color startingColor) {
	Color newPaint = null;
	if (getLock()) {
  	    newPaint = OMColorChooser.showDialog(source, title, startingColor);
	    releaseLock();
	}
	return newPaint;
    }

    /**
     * Get the GUI components that control the DrawingAttributes. 
     */
    public Component getGUI() {
	if (Debug.debugging("drawingattributes")) {
 	    Debug.output("DrawingAttributes: creating palette.");
	}

	if (palette == null) {
	    palette = new JPanel();
	    palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));
	    palette.setAlignmentX(Component.CENTER_ALIGNMENT); // LEFT
	    palette.setAlignmentY(Component.CENTER_ALIGNMENT); // BOTTOM
	    
	    JPanel colorPanel = new JPanel();
	    JPanel linePanel = new JPanel();

	    lineColorButton = new JButton("Edge");
	    lineColorButton.setActionCommand(LineColorCommand);
	    lineColorButton.addActionListener(this);
	    lineColorButton.setToolTipText("Change Edge Color");

	    fillColorButton = new JButton("Fill");
	    fillColorButton.setActionCommand(FillColorCommand);
	    fillColorButton.addActionListener(this);
	    fillColorButton.setToolTipText("Change Fill Color");	    
	    
	    selectColorButton = new JButton("Select");
	    selectColorButton.setActionCommand(SelectColorCommand);
	    selectColorButton.addActionListener(this);
	    selectColorButton.setToolTipText("Change Selected Edge Color");

	    JPanel colorBox = new JPanel();
	    colorBox.setLayout(new GridLayout(0, 1));
	    colorBox.add(lineColorButton);
	    colorBox.add(fillColorButton);
	    colorBox.add(selectColorButton);
	    if (stroke instanceof BasicStroke) {
		BasicStrokeEditor tmpbse = getBasicStrokeEditor();
		if (tmpbse != null) {
		    colorBox.add(tmpbse.getLaunchButton());
		}
	    }

	    mattedCheckBox = new JCheckBox("Matted", isMatted());
	    mattedCheckBox.setActionCommand(MattedCommand);
	    mattedCheckBox.addActionListener(this);
	    mattedCheckBox.setToolTipText("Add Black Edge");
	    colorBox.add(mattedCheckBox);

	    colorPanel.add(colorBox);

	    palette.add(colorPanel);
	}

	resetGUI();

//  	lineCombo.setSelectedIndex(LineChoiceComboBox.getLineWidthSelection(stroke));
//  	dashCombo.setSelectedIndex(DashedLineChoiceComboBox.tryToDetermineStroke(stroke));
	return palette;
    }

    public void resetGUI() {
	if (linePaint instanceof Color && lineColorButton != null) {
 	    lineColorButton.setBackground((Color)linePaint);
	    lineColorButton.setForeground(calculateTextColor((Color)linePaint));
	}
	if (fillPaint instanceof Color && fillColorButton != null) {
	    fillColorButton.setBackground((Color)fillPaint);
	    fillColorButton.setForeground(calculateTextColor((Color)fillPaint));
	}
	if (selectPaint instanceof Color && selectColorButton != null) {
	    selectColorButton.setBackground((Color)selectPaint);
	    selectColorButton.setForeground(calculateTextColor((Color)selectPaint));
	}

	if (mattedCheckBox != null) {
	    mattedCheckBox.setSelected(matted);
	}

	if (stroke instanceof BasicStroke) {
	    BasicStrokeEditor tmpbse = getBasicStrokeEditor();
	    if (tmpbse != null) {
		tmpbse.setBasicStroke((BasicStroke)stroke);
	    }
	}
    }
	
    /**
     * Create an ImageIcon from a java.awt.Paint.
     *
     * @param paint java.awt.Paint
     * @param width icon pixel width
     * @param height icon pixel height
     */
    public static ImageIcon getPaletteIcon(Paint paint, int width, int height) {
	BufferedImage bufferedImage = 
	    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics = (Graphics2D)bufferedImage.getGraphics();
	graphics.setPaint(paint);
	graphics.fillRect(0, 0, width, height);

	return new ImageIcon(bufferedImage);
    }

    /**
     * Get the PropertyChangeSupport object to register anything that
     * is interested in finding out when some parameter has changed.
     */
    public PropertyChangeSupport getPropertyChangeSupport() {
	return propertyChangeSupport;
    }

    public void setPropertyChangeSupport(PropertyChangeSupport support) {
	propertyChangeSupport = support;
    }

    public static Color calculateTextColor(Color color) {
	if (alwaysSetTextToBlack) // Mac OS X
	    return Color.black;

	int red = color.getRed();
	int green = color.getGreen();
	int blue = color.getBlue();
	int alpha = color.getAlpha();

	if (alpha < 128) return Color.black;

	int newred, newgreen, newblue;
	
	newred = normalizeOn128(red);
	newgreen = normalizeOn128(green); 
	newblue = normalizeOn128(blue);

	return new Color(newred, newgreen, newblue);
    }

    public static int normalizeOn128(int value) {
       if (value >= 255) return 0;
       else if (value <= 0) return 255;
       else if (value <= 128) return 192;
       return 64;
    }

    /**
     * Sets the properties for the <code>DrawingAttributes</code>. 
     * This particular method assumes
     * that the marker name is not needed, because all of the contents
     * of this Properties object are to be used for this object, and
     * scoping the properties with a prefix is unnecessary.
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(java.util.Properties props) {
	setProperties(getPropertyPrefix(), props);
    }

    public BasicStrokeEditor getBasicStrokeEditor() {
	if (bse == null && stroke instanceof BasicStroke) {
	    try {
		bse = new BasicStrokeEditor((BasicStroke)getStroke());
		bse.getPropertyChangeSupport().addPropertyChangeListener(this);
	    } catch (Exception e) {
		// This happens if a java Toolkit is not available.
		bse = null;
	    }
	}
	return bse;
    }

    /**
     * Sets the properties for the <code>DrawingAttributes</code>.
     * Part of the PropertyConsumer interface.  DrawingAttributess
     * which override this method should do something like:
     *
     * <code><pre>
     * public void setProperties (String prefix, Properties props) {
     *     super.setProperties(prefix, props);
     *     // do local stuff
     * }
     * </pre></code>
     *
     * If the addToBeanContext property is not defined, it is set to
     * false here.
     *
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object 
     */
    public void setProperties(String prefix, java.util.Properties props) {

	propertyChangeSupport = new PropertyChangeSupport(this);
	setPropertyPrefix(prefix);

	if (props == null) {
	    return;
	}

	String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

	//  Set up the drawing attributes.
	linePaint =
	    LayerUtils.parseColorFromProperties(
		props, realPrefix + linePaintProperty,
		linePaint);
	
	selectPaint =
	    LayerUtils.parseColorFromProperties(
		props, realPrefix + selectPaintProperty,
		selectPaint);
	
	textPaint =
	    LayerUtils.parseColorFromProperties(
		props, realPrefix + textPaintProperty,
		textPaint);

	fillPaint =
	    LayerUtils.parseColorFromProperties(
		props, realPrefix + fillPaintProperty,
		fillPaint);

	matted = LayerUtils.booleanFromProperties(props, realPrefix + mattedProperty, matted);

	float lineWidth;
	boolean strokeDefined = false;

	if (stroke != null && stroke instanceof BasicStroke) {
	    strokeDefined = true;
	}
	
	lineWidth =
	    LayerUtils.floatFromProperties(
		props, realPrefix + lineWidthProperty,
		(strokeDefined?((BasicStroke)stroke).getLineWidth():defaultLineWidth));

	baseScale =
	    LayerUtils.floatFromProperties(
		props, realPrefix + baseScaleProperty,
		baseScale);
	
	// Look for a dash pattern properties to come up with a stroke
	String dPattern = props.getProperty(realPrefix + dashPatternProperty);
	if (strokeDefined && dPattern != null && !dPattern.equals("")) {
	    float dashPhase;
	    float[] lineDash;
	    // OK, it exists, come up with a stroke.
	    try {
		StringTokenizer t = new StringTokenizer(dPattern);
		int arraySize = t.countTokens();
		lineDash = new float[arraySize];

		int dashCount = 0;
		while (t.hasMoreTokens()) {
		    String segment = t.nextToken();
		    lineDash[dashCount++] = Float.parseFloat(segment);
		    if (Debug.debugging("drawingattributes")) {
			Debug.output("read " + segment);
		    }
		}

	    } catch (NoSuchElementException nsee) {
		Debug.error("DrawingAttributes.init: dash pattern attributes wrong - should be dashPattern=(number pixels on) (number pixels off)");
		lineDash = null;
	    } catch (NumberFormatException nfe) {
		Debug.error("DrawingAttributes.init: Number format exception for dashPattern");
		lineDash = null;
	    } catch (NullPointerException npe) {
		Debug.error("DrawingAttributes.init: Caught null pointer exception - probably resulting from non-float number format exception for dashPattern");
		lineDash = null;
	    }

	    if (lineDash == null){
		if (strokeDefined) {
		    lineDash = ((BasicStroke)stroke).getDashArray();
		} else {
		    lineDash = new float[2];
		    lineDash[0] = defaultDashLength;
		    lineDash[1] = defaultDashLength;
		}
	    }

	    int dashCount = 0;
	    for (int x = 0; x < lineDash.length; x++) {
		dashCount += lineDash[x];
	    }
	    if (dashCount == 0) {
		lineDash = null;
	    }

	    String dPhase = props.getProperty(realPrefix + dashPhaseProperty);
	    if (dPhase != null && !dPhase.equals("")) {
		try {
		    dashPhase = Float.valueOf(dPhase).floatValue();
		} catch (NumberFormatException nfe) {
		    Debug.error("DrawingAttributes.init: Number format exception for dashPhase");
		    dashPhase = defaultDashPhase;
		}
	    } else {
		if (strokeDefined) {
		    dashPhase = ((BasicStroke)stroke).getDashPhase();
		} else {
		    dashPhase = defaultDashPhase;
		}
	    }

	    setStroke(new BasicStroke(lineWidth, 
				      BasicStroke.CAP_BUTT, 
				      BasicStroke.JOIN_MITER, 10.0f, 
				      lineDash, dashPhase));

	} else if (!strokeDefined) {
	    setStroke(new BasicStroke(lineWidth));
	}

	//  OK, Fill pattern next...
	fPattern = props.getProperty(realPrefix + fillPatternProperty);
	if (fPattern != null && !fPattern.equals("")) {

	    try {

		URL textureImageURL = 
		    LayerUtils.getResourceOrFileOrURL(this, fPattern);

		if (textureImageURL != null) {

		    BufferedImage bi = 
			BufferedImageHelper.getBufferedImage(textureImageURL, 
							     0, 0, -1, -1);
		    
		    fillPattern = 
			new TexturePaint(bi, new Rectangle(0,0, bi.getWidth(), 
							   bi.getHeight()));
		} 
	    } catch (MalformedURLException murle) {
		Debug.error("DrawingAttributes.init: bad texture URL - \n     " + 
			    realPrefix + fillPatternProperty);
		fillPattern = null;
	    } catch (InterruptedException ie) {
		Debug.error("DrawingAttributes.init: bad problems getting texture URL - \n" + ie);
		fillPattern = null;
	    }
	}
    }

    /**
     * PropertyConsumer method, to fill in a Properties object,
     * reflecting the current values of the layer.  If the
     * layer has a propertyPrefix set, the property keys should
     * have that prefix plus a separating '.' prepended to each
     * propery key it uses for configuration.
     *
     * @param props a Properties object to load the PropertyConsumer
     * properties into.  If props equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

	String prefix = PropUtils.getScopedPropertyPrefix(this);

	if (linePaint instanceof Color) {
	    props.put(prefix + linePaintProperty, 
		      Integer.toHexString(((Color)linePaint).getRGB()));
	}
	if (textPaint instanceof Color) {
	    props.put(prefix + textPaintProperty, 
		      Integer.toHexString(((Color)textPaint).getRGB()));
	}
	if (fillPaint instanceof Color) {
	    props.put(prefix + fillPaintProperty, 
		      Integer.toHexString(((Color)fillPaint).getRGB()));
	}
	if (selectPaint instanceof Color) {
	    props.put(prefix + selectPaintProperty, 
		      Integer.toHexString(((Color)selectPaint).getRGB()));
	}

	props.put(prefix + fillPatternProperty, 
		  (fPattern==null?"":fPattern));

	Stroke bs = getStroke();

	if (bs == null) {
	    bs = new BasicStroke();
	}

	if (bs instanceof BasicStroke) {
	    props.put(prefix + lineWidthProperty, Float.toString(((BasicStroke)bs).getLineWidth()));
	    
	    float[] fa = ((BasicStroke)bs).getDashArray();
	    if (fa != null) {
		StringBuffer dp = new StringBuffer();
		for (int i = 0; i < fa.length; i++) {
		    dp.append(" " + Float.toString(fa[i]));
		}
		props.put(prefix + dashPatternProperty, dp.toString());
		props.put(prefix + dashPhaseProperty, Float.toString(((BasicStroke)bs).getDashPhase()));
	    } else {
		props.put(prefix + dashPatternProperty, "");
		props.put(prefix + dashPhaseProperty, "");
	    }
	}

	if (baseScale != NONE) {
	    props.put(prefix + baseScaleProperty, Float.toString(baseScale));
	}

	props.put(prefix + mattedProperty, new Boolean(matted).toString());
	
	return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer. 
     */
    public Properties getPropertyInfo(Properties list) {
	if (list == null) {
	    list = new Properties();
	}

	list.put(linePaintProperty, "Edge color for graphics.");
	list.put(linePaintProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

	list.put(textPaintProperty, "Text color for graphics.");
	list.put(textPaintProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

	list.put(fillPaintProperty, "Fill color for graphics.");
	list.put(fillPaintProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

	list.put(selectPaintProperty, "Selected edge color for graphics.");
	list.put(selectPaintProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

	list.put(fillPatternProperty, "Image file to use for fill pattern for graphics (optional).");
	list.put(fillPatternProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

	list.put(lineWidthProperty, "Line width for edges of graphics");
// 	list.put(dashPatternProperty, "<HTML><BODY>Line dash pattern, represented by<br>space separated numbers<br> (on off on ...)</BODY></HTML>");
	list.put(dashPatternProperty, "Line dash pattern, represented by space separated numbers (on off on ...)");
	list.put(dashPhaseProperty, "Phase for dash pattern (Default is 0)");

	list.put(baseScaleProperty, "<HTML><BODY>Scale which should be used as the base scale for the <br>patterns and line width. If set, size of pattern and <br>widths will be adjusted to the map scale</BODY></HTML>");
	
	list.put(mattedProperty, "Flag to enable a thin black matting to be drawn around graphics..");
	list.put(mattedProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	return list;
    }

    public String getInitPropertiesOrder() {
	return " " + linePaintProperty + " " + selectPaintProperty + " " + fillPaintProperty + " " + textPaintProperty + " " + fillPatternProperty + " " + mattedProperty + " " + lineWidthProperty + " " + dashPatternProperty + " " + dashPhaseProperty;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix) {
	propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     *
     * @param String prefix String.  
     */
    public String getPropertyPrefix() {
	return propertyPrefix;
    }

    public void propertyChange(PropertyChangeEvent pce) {
	if (pce.getSource() instanceof BasicStrokeEditor) {
	    setStroke((BasicStroke)pce.getNewValue());
	}
    }

    public String toString() {
	StringBuffer sb = new StringBuffer("DrawningAttributes[");
	sb.append("linePaint(" + linePaint + "), ");
	sb.append("selectPaint(" + selectPaint + "), ");
	sb.append("textPaint(" + textPaint + "), ");
	sb.append("fillPaint(" + fillPaint + "), ");
	sb.append("fillPattern(" + fillPattern + "), ");
	sb.append("stroke(" + stroke + "), ");
	sb.append("baseScale(" + baseScale + "), ");
	sb.append("matted(" + new Boolean(matted).toString() + ")]");
	return sb.toString();
    }
}




