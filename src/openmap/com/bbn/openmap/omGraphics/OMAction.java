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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMAction.java,v $
// $RCSfile: OMAction.java,v $
// $Revision: 1.7 $
// $Date: 2005/08/09 20:01:45 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import com.bbn.openmap.util.Debug;

/**
 * The OMAction class provides a way to describe one or more actions
 * that should be performed on an OMGraphic.  Each digit of the
 * internal integer represents an action, and the action masks are
 * defined in OMGraphicsConstances.  The class holds the action value,
 * and can respond to queries to check if certain masks are set, or
 * can also set certain masks on the internal value.  There are also
 * static methods provided as a convenience.
 */
public class OMAction implements OMGraphicConstants {
    /**
     * The internal value of the action, representing 0 or more
     * actions to be performed on a graphic, depending on the bits
     * set.
     */
    protected int value;
    
    /**
     * Create an OMAction that represents no action (No bits are set).
     */ 
    public OMAction() {
        this.value = 0;
    }

    /**
     * Create an OMAction with the provided actions.
     */ 
    public OMAction(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Set a particular mask bit in the internal value.
     * @param mask an OMGraphicConstant mask constant.
     * @return the changed integer value.
     */
    public int setMask(int mask) {
        value = OMAction.setMask(value, mask);
        return value;
    }

    /** 
     * Unset a particular mask bit in the internal value.
     * @param mask an OMGraphicConstant mask constant.
     * @return the changed integer value.
     */
    public int unsetMask(int mask) {
        value = unsetMask(value, mask);
        return value;
    }

    /** 
     * Return whether a mask value is set in the internal value.
     * @param mask an OMGraphicConstant mask constant.
     * @return whether the value bit is set on the internal value.
     */
    public boolean isMask(int mask) {
        return isMask(value, mask);
    }

    /**
     * Set a particular mask bit in the provided integer.
     * @param value the integer to set the value(bit) on.
     * @param mask an OMGraphicConstant mask constant.
     * @return the changed integer value.
     */
    public static int setMask(int value, int mask) {
        return (value | mask);
    }

    /**
     * Unset a particular mask bit in the provided integer.
     * @param value the integer to unset the value(bit) on.
     * @param mask an OMGraphicConstant mask constant.
     * @return the changed integer value.
     */
    public static int unsetMask(int value, int mask) {
        return (value & ~mask);
    }

    /**
     * Check to see if a mask bit is set in an integer.
     * @param value the integer to check for the value(bit) on.
     * @param mask an OMGraphicConstant mask constant.
     * @return whether the value bit is set.
     */
    public static boolean isMask(int value, int mask) {
        // Used to be == 0, but this way it returns true if all of the
        // mask bits are set.
        if ((value & mask) < mask) {
            return false;
        }
        return true;
    }

    /**
     * Provide a String that describes what the Action is all about.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("OMAction (").append(value).append(") [ ");

        if (value == 0) {
            sb.append("add ");
        } else {
            if (isMask(RAISE_TO_TOP_GRAPHIC_MASK)) sb.append("raise_to_top ");
            if (isMask(LOWER_TO_BOTTOM_GRAPHIC_MASK)) sb.append("lower_to_bottom ");
            if (isMask(DELETE_GRAPHIC_MASK)) sb.append("delete ");
            if (isMask(SELECT_GRAPHIC_MASK)) sb.append("select ");
            if (isMask(DESELECT_GRAPHIC_MASK)) sb.append("deselect ");
            if (isMask(DESELECTALL_GRAPHIC_MASK)) sb.append("deselect_all ");
            if (isMask(ADD_GRAPHIC_MASK)) sb.append("add ");
            if (isMask(UPDATE_GRAPHIC_MASK)) sb.append("update ");
            if (isMask(RAISE_GRAPHIC_MASK)) sb.append("raise ");
            if (isMask(LOWER_GRAPHIC_MASK)) sb.append("lower ");
            if (isMask(SORT_GRAPHICS_MASK)) sb.append("sort ");
        }
        sb.append("]");
        return sb.toString();
    }

    public final static void main(String[] argv) {

        OMAction action = new OMAction();
        Debug.init();
        Debug.output("Setting add mask...");
        action.setMask(ADD_GRAPHIC_MASK);
        if (action.isMask(ADD_GRAPHIC_MASK)) {
            Debug.output("ADD mask set.");
        } else {
            Debug.output("ADD mask *NOT* set");
        }
    }
}
