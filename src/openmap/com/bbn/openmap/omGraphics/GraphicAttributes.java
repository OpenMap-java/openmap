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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/GraphicAttributes.java,v $
// $RCSfile: GraphicAttributes.java,v $
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
import com.bbn.openmap.proj.LineType;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/** 
 * The GraphicAttributes provides an extension to DrawingAttributes by
 * provideing a mechanism for loading and managing different graphic
 * attributes that may be used, such as line type (LINETYPE_STRAIGHT,
 * LINETYPE_GREATCIRCLE, LINETYPE_RHUMB, or LINETYPE_UNKNOWN), or
 * render type (RENDERTYPE_XY, RENDERTYPE_LATLON, RENDERTYPE_OFFSET,
 * or RENDERTYPE_UNKNOWN).  The DrawingAttributes class fishes out the
 * applicable properties for you, creates the objects needed, and then
 * lets you get those objects when needed.  
 */
public class GraphicAttributes extends DrawingAttributes implements ActionListener, Serializable, OMGraphicConstants  {

    /** The name of the property that holds the line type of the graphic. */
    public final static String lineTypeProperty = "lineType";
    /** The name of the property that holds the render type of the graphic. */
    public final static String renderTypeProperty = "renderType";

    /** The line type of a graphic, defaults to LINETYPE_STRAIGHT. */
    protected int lineType = LINETYPE_STRAIGHT;
    /** The rendertype of a graphic.  Default is RENDERTYPE_XY. */
    protected int renderType = RENDERTYPE_XY;

    public final static GraphicAttributes DEFAULT = new GraphicAttributes();

    /**
     * Create a GraphicAttributes with the default settings - clear
     * fill paint and pattern, sold black edge line of width 1.  
     */
    public GraphicAttributes() {
	setProperties(null, null);
    }

    /**
     * Create the GraphicAttributes and call init without a prefix for
     * the properties.  Call init without a prefix for the properties.
     * @param props the Properties to look in.  
     */
    public GraphicAttributes(Properties props) {
	setProperties(null, props);
    }

    /**
     * Create the GraphicAttributes and call init with a prefix for
     * the properties.  
     * @param prefix the prefix marker to use for a property, like
     * prefix.propertyName.  The period is added in this function.
     * @param props the Properties to look in.  
     */
    public GraphicAttributes(String prefix, Properties props) {
	setProperties(prefix, props);
    }

    /**
     *  PropertyConsumer method.
     */
    public void setProperties(String prefix, Properties props) {

	super.setProperties(prefix, props);

	String realPrefix;

	if (props == null) {
	    return;
	}

	if (prefix != null) {
	    realPrefix = prefix + ".";
	} else {
	    realPrefix = "";
	}
	
	//  Set up the Graphic attributes.
	lineType =
	    LayerUtils.intFromProperties(
		props, realPrefix + lineTypeProperty,
  		LINETYPE_UNKNOWN);
	
	renderType =
	    LayerUtils.intFromProperties(
		props, realPrefix + renderTypeProperty,
		RENDERTYPE_UNKNOWN);
    }

    public Object clone() {
	GraphicAttributes clone = new GraphicAttributes();
	clone.linePaint = linePaint;
	clone.textPaint = textPaint;
	clone.selectPaint = selectPaint;
	clone.fillPaint = fillPaint;
	clone.fillPattern = fillPattern;
	clone.setStroke(stroke);
	clone.baseScale = baseScale;
	clone.renderType = renderType;
	clone.lineType = lineType;
	return clone;
    }

    /**
     * Get the lineType.
     */
    public int getLineType() {
	return lineType;
    }

    /**
     * Set the line type.  If it isn't straight, great circle or
     * rhumb, it's set to unknown.  
     */
    public void setLineType(int lt) {
	int oldLineType = lineType;

	if (lt == LINETYPE_STRAIGHT ||
	    lt == LINETYPE_GREATCIRCLE ||
	    lt == LINETYPE_RHUMB) {
	    lineType = lt;
	} else {
	    lineType = LINETYPE_UNKNOWN;
	}

	propertyChangeSupport.firePropertyChange("lineType", 
						 oldLineType, 
						 lineType);
    }

    /**
     * Get the renderType.
     */
    public int getRenderType() {
	return renderType;
    }

    /**
     * Set the render type.  If it isn't xy, lat/lon, or lat/lon with
     * offset, it's set to unknown.
     */
    public void setRenderType(int rt) {
	int oldRenderType = renderType;

	if (rt == RENDERTYPE_XY ||
	    rt == RENDERTYPE_LATLON ||
	    rt == RENDERTYPE_OFFSET) {
	    renderType = rt;

	    if (lineTypeList != null) {
		lineTypeList.setEnabled(renderType == RENDERTYPE_LATLON);
	    }

	} else {
	    renderType = RENDERTYPE_UNKNOWN;
	}

	propertyChangeSupport.firePropertyChange("renderType", 
						 oldRenderType, 
						 renderType);
    }

    /**
     * Set the GraphicAttributes parameters based on the current
     * settings of an OMGraphic.  
     */
    public void setFrom(OMGraphic graphic) {
	super.setFrom(graphic);
	lineType = graphic.getLineType();
	renderType = graphic.getRenderType();
    }


    /**
     * Set all the attributes for the graphic that are contained
     * within this GraphicAttributes class.
     * 
     * @param graphic OMGraphic.  
     */
    public void setTo(OMGraphic graphic) {
	super.setTo(graphic);
	graphic.setLineType(lineType);
	graphic.setRenderType(renderType);
    }

    JComboBox lineTypeList = null;
    public final static String StraightLineType = "Straight Lines";
    public final static String GCLineType = "Great Circle Lines";
    public final static String RhumbLineType = "Rhumb Lines";

    /**
     * Get the GUI components that control the
     * GraphicAttributes. Currently doesn't do anything but pass on
     * the DrawingAttribute GUI.
     */
    public Component getGUI() {
	if (Debug.debugging("graphicattributes")) {
	    Debug.output("GraphicAttributes: creating palette.");
	}

	boolean needToBuild = false;

	if (super.palette == null) {
	    needToBuild = true;
	}

	Component dagui = super.getGUI();

	if (needToBuild) {
	    
	    palette = new JPanel();
	    palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));
	    palette.setAlignmentX(Component.CENTER_ALIGNMENT); // LEFT
	    palette.setAlignmentY(Component.CENTER_ALIGNMENT); // BOTTOM
	    
	    String[] lineTypes = new String[3];
	    
	    lineTypes[LineType.Straight - 1] = StraightLineType;
	    lineTypes[LineType.GreatCircle - 1] = GCLineType;
	    lineTypes[LineType.Rhumb - 1] = RhumbLineType;
	    
	    lineTypeList = new JComboBox(lineTypes);
	    lineTypeList.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			JComboBox jcb = (JComboBox) e.getSource();
			String currentChoice = (String)jcb.getSelectedItem();
			if (currentChoice == GCLineType) {
			    setLineType(LineType.GreatCircle);
			} else if (currentChoice == RhumbLineType) {
			    setLineType(LineType.Rhumb);
			} else {
			    setLineType(LineType.Straight);
			}		    
		    }
		});
	    
	    palette.add(lineTypeList);
	    palette.add(dagui);
	}

	int rt = getRenderType();

	lineTypeList.setEnabled(renderType == RENDERTYPE_LATLON);

	if (rt != RENDERTYPE_LATLON) {
	    lineTypeList.setSelectedIndex(0);
	    lineType = LineType.Straight;
	} else {
	    int currentLineType = getLineType();
	    if (currentLineType == 0) currentLineType++;  // no unknowns

	    lineTypeList.setSelectedIndex(currentLineType - 1); // reset to String[]
	}

	return palette;
    }

    /**
     *  The DrawingAttributes method for handling ActionEvents.  Used
     *  to handle the GUI actions, like changing the colors, line
     *  widths, etc.
     */
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
    }

}
