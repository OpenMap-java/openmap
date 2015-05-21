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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/dock/DockPanel.java,v $
// $RCSfile: DockPanel.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.dock;

import java.awt.BorderLayout;

import javax.swing.JComponent;

/**
 * An interface onto a component that has a background component and
 * docking children.
 * 
 * @author Ben Lubin
 * @version $Revision: 1.5 $ on $Date: 2004/10/14 18:05:49 $
 * @since 12/5/02
 */
public interface DockPanel {

    /** Constraint for the background component */
    public static final String BACKGROUND = BorderLayout.CENTER;

    //Background Methods:
    /////////////////////

    /**
     * Get the background component.
     */
    public JComponent getBackgroundComponent();

    /**
     * Set the background component.
     */
    public void setBackgroundComponent(JComponent back);

    //Constraint Methods:
    /////////////////////

    /**
     * Set the constraint on the given child.
     */
    public void setConstraint(JComponent child, DockConstraint c);

    /**
     * Get the constraint on the given child.
     */
    public DockConstraint getConstraint(JComponent child);

    /**
     * Remove a constraint on a child.
     */
    public void removeConstraint(JComponent child);

    //Constraint Setup Functions:
    /////////////////////////////

    /**
     * Set the childs preferred height.
     */
    public void setPreferredHeight(JComponent child, int i);

    /**
     * Set the childs preferred width.
     */
    public void setPreferredWidth(JComponent child, int i);

    /**
     * Set that the given child can sit in front of the background
     * component, without forcing the background component to be
     * resized to make room.
     */
    public void setCanOcclude(JComponent child, boolean b);

    /**
     * Set the child component to have a transparent background.
     */
    public void setCanTransparent(JComponent child, boolean b);

    /**
     * Set that the child component can be user-resized.
     */
    public void setCanResize(JComponent child, boolean b);

    /**
     * Set that the child component can be tabbed up, if docked in the
     * same location as other tab-able components.
     */
    public void setCanTab(JComponent child, boolean b);

    /**
     * Set the name of the tab to use when the component is tabbed (if
     * it can tab). If unspecified, defaults to Component.getName()
     */
    public void setTabName(JComponent child, String tabName);

    /**
     * Set that the child can be become an external frame.
     */
    public void setCanExternalFrame(JComponent child, boolean b);

    /**
     * Set that the child can be become an internal frame.
     */
    public void setCanInternalFrame(JComponent child, boolean b);

    /**
     * Set that the child can be closed.
     */
    public void setCanClose(JComponent child, boolean b);

    /**
     * Set that the child can dock on the top-level north.
     */
    public void setCanDockNorth(JComponent child, boolean b);

    /**
     * Set that the child can dock on the top-level south.
     */
    public void setCanDockSouth(JComponent child, boolean b);

    /**
     * Set that the child can dock on the top-level east.
     */
    public void setCanDockEast(JComponent child, boolean b);

    /**
     * Set that the child can dock on the top-level west.
     */
    public void setCanDockWest(JComponent child, boolean b);

    //Docking Functions:
    ////////////////////

    //NORTH:

    /**
     * Dock child on the DockPanel.
     */
    public void dockNorth(JComponent child);

    /**
     * Dock the given child into the given position on the DockPanel
     */
    public void dockNorth(JComponent child, int idx);

    //SOUTH:

    /**
     * Dock child on the DockPanel.
     */
    public void dockSouth(JComponent child);

    /**
     * Dock the given child into the given position on the DockPanel
     */
    public void dockSouth(JComponent child, int idx);

    //EAST:

    /**
     * Dock child on the DockPanel.
     */
    public void dockEast(JComponent child);

    /**
     * Dock the given child into the given position on the DockPanel
     */
    public void dockEast(JComponent child, int idx);

    //WEST:

    /**
     * Dock child on the DockPanel.
     */
    public void dockWest(JComponent child);

    /**
     * Dock the given child into the given position on the DockPanel
     */
    public void dockWest(JComponent child, int idx);

    //ANYWHERE:

    /**
     * Dock the given child somewhere on the DockPanel.
     */
    public void dockSomewhere(JComponent child);

    //DOCK onto existing child:

    /**
     * Dock the given child onto the given parent, which is itself a
     * child.
     */
    public void dock(JComponent outter, JComponent inner);

    /**
     * Dock the given child onto the given parent, which is itself a
     * child.
     */
    public void dock(JComponent outter, JComponent inner, int idx);

    //FRAME Methods:

    /**
     * Set the component to an internal frame
     */
    public void internalFrame(JComponent child);

    /**
     * Set the component to an external frame
     */
    public void externalFrame(JComponent child);
}

