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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/labeled/LabeledOMGraphic.java,v $
// $RCSfile: LabeledOMGraphic.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/11 20:39:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.labeled;

import java.awt.Font;
import java.awt.Point;

/**
 * The LabeledOMGraphic is a simple extension to the OMGraphic where a
 * text string can be set for the graphic on the map. It provides
 * basic functionality. If you need more control, try the Location
 * object or use a OMText object for your label.
 */
public interface LabeledOMGraphic {

    /**
     * Set the String for the label.
     */
    public void setText(String label);

    /**
     * Get the String for the label.
     */
    public String getText();

    /**
     * Set the Font for the label.
     */
    public void setFont(Font f);

    /**
     * Get the Font for the label.
     */
    public Font getFont();

    /**
     * Set the justification setting for the label.
     * 
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_LEFT
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_CENTER
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_RIGHT
     */

    public void setJustify(int just);

    /**
     * Get the justification setting for the label.
     * 
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_LEFT
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_CENTER
     * @see com.bbn.openmap.omGraphics.OMText#JUSTIFY_RIGHT
     */
    public int getJustify();

    /**
     * Tell the LabeledOMGraphic to calculate the location of the
     * String that would put it in the middle of the OMGraphic.
     */
    public void setLocateAtCenter(boolean set);

    /**
     * Get whether the LabeledOMGraphic is placing the label String in
     * the center of the OMGraphic.
     */
    public boolean isLocateAtCenter();

    /**
     * Get the calculated center where the label string is drawn.
     */
    public Point getCenter();

    /**
     * Set the index of the OMGraphic coordinates where the drawing
     * point of the label should be attached. The meaning of the point
     * differs between OMGraphic types.
     */
    public void setIndex(int index);

    /**
     * Get the index of the OMGraphic where the String will be
     * rendered. The meaning of the index differs from OMGraphic type
     * to OMGraphic type.
     */
    public int getIndex();

    /**
     * Set the x, y pixel offsets where the String should be rendered,
     * from the location determined from the index point, or from the
     * calculated center point. Point.x is the horizontal offset,
     * Point.y is the vertical offset.
     */
    public void setOffset(Point p);

    /**
     * Get the x, y pixel offsets set for the rendering of the point.
     */
    public Point getOffset();

    /**
     * Set the angle by which the text is to rotated.
     * 
     * @param angle the number of radians the text is to be rotated.
     *        Measured clockwise from horizontal. Positive numbers
     *        move the positive x axis toward the positive y axis.
     */
    public void setRotationAngle(double angle);

    /**
     * Get the current rotation of the text.
     * 
     * @return the text rotation.
     */
    public double getRotationAngle();

}

