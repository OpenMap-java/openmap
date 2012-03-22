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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/dock/DockConstraint.java,v $
// $RCSfile: DockConstraint.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.dock;

import java.util.Iterator;
import java.util.List;

/**
 * Constraints used for DockPanel behavior.
 * 
 * @author Ben Lubin
 * @version $Revision: 1.3 $ on $Date: 2004/10/14 18:05:49 $
 * @since 12/5/02
 */
public class DockConstraint {

    public DockConstraint() {}

    //Constraints used for layout:
    //////////////////////////////

    private String tabName = null;

    private boolean canTransparent = false;
    private boolean canResize = false;
    private boolean canOcclude = false;
    private boolean canTab = false;
    private boolean canClose = false;
    private boolean canExternalFrame = true;
    private boolean canInternalFrame = true;
    private boolean canDockNorth = true;
    private boolean canDockSouth = true;
    private boolean canDockEast = true;
    private boolean canDockWest = true;

    public String getTabName() {
        return tabName;
    }

    /**
     * Set the name of the tab to use when the component is tabbed (if
     * it can tab). If unspecified, defaults to Component.getName()
     */
    public void setTabName(String name) {
        tabName = name;
    }

    /**
     * True iff this class can be transparent
     */
    public boolean canTransparent() {
        return canTransparent;
    }

    public void setCanTransparent(boolean b) {
        canTransparent = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canTransparent(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canTransparent())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be resize
     */
    public boolean canResize() {
        return canResize;
    }

    public void setCanResize(boolean b) {
        canResize = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canResize(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canResize())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can overlap the background
     */
    public boolean canOcclude() {
        return canOcclude;
    }

    public void setCanOcclude(boolean b) {
        canOcclude = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canOcclude(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canOcclude())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be tabbed together.
     */
    public boolean canTab() {
        return canTab;
    }

    public void setCanTab(boolean b) {
        canTab = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canTab(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canTab())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be closed
     */
    public boolean canClose() {
        return canClose;
    }

    public void setCanClose(boolean b) {
        canClose = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canClose(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canClose())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be changed to frame
     */
    public boolean canExternalFrame() {
        return canExternalFrame;
    }

    public void setCanExternalFrame(boolean b) {
        canExternalFrame = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canExternalFrame(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canExternalFrame())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be changed to frame
     */
    public boolean canInternalFrame() {
        return canInternalFrame;
    }

    public void setCanInternalFrame(boolean b) {
        canInternalFrame = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canInternalFrame(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canInternalFrame())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be docked in the given direction
     */
    public boolean canDockNorth() {
        return canDockNorth;
    }

    public void setCanDockNorth(boolean b) {
        canDockNorth = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canDockNorth(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canDockNorth())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be docked in the given direction
     */
    public boolean canDockSouth() {
        return canDockSouth;
    }

    public void setCanDockSouth(boolean b) {
        canDockSouth = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canDockSouth(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canDockSouth())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be docked in the given direction
     */
    public boolean canDockEast() {
        return canDockEast;
    }

    public void setCanDockEast(boolean b) {
        canDockEast = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canDockEast(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canDockEast())
                return false;
        }
        return true;
    }

    /**
     * True iff this class can be docked in the given direction
     */
    public boolean canDockWest() {
        return canDockWest;
    }

    public void setCanDockWest(boolean b) {
        canDockWest = b;
    }

    /**
     * Determine property over a list of DockConstraint objects
     * 
     * @param constraints a List of DockConstraints
     */
    public static boolean canDockWest(List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            DockConstraint dc = (DockConstraint) iter.next();
            if (!dc.canDockWest())
                return false;
        }
        return true;
    }
}