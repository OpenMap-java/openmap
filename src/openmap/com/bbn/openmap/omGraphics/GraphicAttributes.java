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
// $Revision: 1.8 $
// $Date: 2004/09/22 20:49:20 $
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
    /** Flag to disable choice of line type, from an external source. */
    protected boolean enableLineTypeChoice = true;

    public final static GraphicAttributes DEFAULT = new GraphicAttributes();

    /**
     * Create a GraphicAttributes with the default settings - clear
     * fill paint and pattern, sold black edge line of width 1.  
     */
    public GraphicAttributes() {
        super();
    }

    /**
     * Create the GraphicAttributes and call init without a prefix for
     * the properties.  Call init without a prefix for the properties.
     * @param props the Properties to look in.  
     */
    public GraphicAttributes(Properties props) {
        super(props);
    }

    /**
     * Create the GraphicAttributes and call init with a prefix for
     * the properties.  
     * @param prefix the prefix marker to use for a property, like
     * prefix.propertyName.  The period is added in this function.
     * @param props the Properties to look in.  
     */
    public GraphicAttributes(String prefix, Properties props) {
        super(prefix, props);
    }

    /**
     * If you want to get a DEFAULT DrawingAttributes object that you
     * may modify, get your own copy.
     */
    public static GraphicAttributes getGADefaultClone() {
        return (GraphicAttributes) DEFAULT.clone();
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
        setTo(clone);
        return clone;
    }

    public void setTo(GraphicAttributes clone) {
        super.setTo(clone);
        clone.renderType = renderType;
        clone.lineType = lineType;
        clone.enableLineTypeChoice = enableLineTypeChoice;
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
        if (graphic == null) return;

        super.setFrom(graphic);
        lineType = graphic.getLineType();
        renderType = graphic.getRenderType();
        enableLineTypeChoice = graphic.hasLineTypeChoice();
    }

    /**
     * Set all the attributes for the graphic that are contained
     * within this GraphicAttributes class.
     * 
     * @param graphic OMGraphic.  
     */
    public void setTo(OMGraphic graphic) {
        if (graphic == null) return;

        super.setTo(graphic);
        graphic.setLineType(lineType);
        graphic.setRenderType(renderType);
    }

    /**
     * Method should be called on this GraphicAttributes object if the
     * OMGraphic type doesn't support line types to disable the choice
     * from the line menu. Circles, range rings, points, etc. are all
     * examples of shapes that disable linetype choice.
     */
    public void setEnableLineTypeChoice(boolean value) {
        enableLineTypeChoice = value;
    }

    public boolean getEnableLineTypeChoice() {
        return enableLineTypeChoice;
    }

    public JMenu getLineTypeMenu() {
        JMenu lineTypeMenu = null;

        if (renderType == RENDERTYPE_LATLON && enableLineTypeChoice) {
            lineTypeMenu = new JMenu("Line Type");

            ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String command  = ae.getActionCommand();
                        try {
                            setLineType(Integer.parseInt(command));
                        } catch (NumberFormatException e) {}
                    }
                };

            ButtonGroup group = new ButtonGroup(); 
            JRadioButtonMenuItem button = 
                new JRadioButtonMenuItem("Great Circle", lineType == LineType.GreatCircle);
            button.setActionCommand(String.valueOf(LineType.GreatCircle));
            group.add(button);
            button.addActionListener(listener);
            lineTypeMenu.add(button);
        
            button = new JRadioButtonMenuItem("Rhumb", lineType == LineType.Rhumb);
            button.setActionCommand(String.valueOf(LineType.Rhumb));
            group.add(button);
            button.addActionListener(listener);
            lineTypeMenu.add(button);

            button = new JRadioButtonMenuItem("Straight", lineType == LineType.Straight);
            button.setActionCommand(String.valueOf(LineType.Straight));
            group.add(button);
            button.addActionListener(listener);
            lineTypeMenu.add(button);
        }
        return lineTypeMenu;
    }

}
