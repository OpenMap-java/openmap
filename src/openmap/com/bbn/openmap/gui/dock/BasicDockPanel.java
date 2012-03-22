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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/dock/BasicDockPanel.java,v $
// $RCSfile: BasicDockPanel.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 17:50:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.dock;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.bbn.openmap.util.Debug;

/**
 * A component that has a background component and docking children.
 * 
 * @author Ben Lubin
 * @version $Revision: 1.4 $ on $Date: 2005/08/09 17:50:51 $
 * @since 12/5/02
 */
public class BasicDockPanel extends JLayeredPane implements DockPanel {

    /** Layer where we add the background component */
    private static final Integer BACKGROUND_LAYER = new Integer(-100);

    /** The one and only background component */
    private JComponent background;

    /** Maps children to their constraint objects */
    private HashMap childToConstraint = new HashMap(11);

    /**
     * Maps children to their wrappers (not 1-to-1 when tabbing
     * involved)
     */
    private HashMap childToWrapper = new HashMap(11);

    /** A list of wrappers that are currently wrapped in JFrames */
    private List externalFrameWrappers = new ArrayList(0);

    /** A list of wrappers that are currently loose InternalFrames */
    private List internalFrameWrappers = new ArrayList(0);

    /**
     * Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper north = new DockWrapper(this, DockWrapper.DOCK_NORTH);
    /**
     * Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper south = new DockWrapper(this, DockWrapper.DOCK_SOUTH);
    /**
     * Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper east = new DockWrapper(this, DockWrapper.DOCK_EAST);
    /**
     * Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper west = new DockWrapper(this, DockWrapper.DOCK_WEST);

    public BasicDockPanel() {
        super();
        setLayout(new DockLayout(this));
        setBackground(UIManager.getColor("control"));
    }

    //Background Methods:
    /////////////////////

    public JComponent getBackgroundComponent() {
        return background;
    }

    public void setBackgroundComponent(JComponent back) {
        if (getBackgroundComponent() != null) {
            remove(getBackgroundComponent());
        }
        add(back, BACKGROUND);
    }

    //Constraint Methods:
    /////////////////////

    public void setConstraint(JComponent child, DockConstraint c) {
        childToConstraint.put(child, c);
    }

    public DockConstraint getConstraint(JComponent child) {
        return (DockConstraint) childToConstraint.get(child);
    }

    public void removeConstraint(JComponent child) {
        childToConstraint.remove(child);
    }

    //Constraint Setup Functions:
    /////////////////////////////

    public void setPreferredHeight(JComponent child, int i) {
        DockWrapper dw = getWrapper(child);
        if (dw != null) {
            dw.setPreferredHeight(i);
        }
    }

    public void setPreferredWidth(JComponent child, int i) {
        DockWrapper dw = getWrapper(child);
        if (dw != null) {
            dw.setPreferredWidth(i);
        }
    }

    public void setCanOcclude(JComponent child, boolean b) {
        getConstraint(child).setCanOcclude(b);
    }

    public void setCanTransparent(JComponent child, boolean b) {
        getConstraint(child).setCanTransparent(b);
        DockWrapper dw = getWrapper(child);
        if (dw != null) {
            dw.updateTransparency();
        }
    }

    public void setCanResize(JComponent child, boolean b) {
        getConstraint(child).setCanResize(b);
        DockWrapper dw = getWrapper(child);
        if (dw != null) {
            dw.updateResizable();
        }
    }

    public void setCanTab(JComponent child, boolean b) {
        getConstraint(child).setCanTab(b);
        dockSomewhere(child);
    }

    /**
     * Set the name of the tab to use when the component is tabbed (if
     * it can tab). If unspecified, defaults to Component.getName()
     */
    public void setTabName(JComponent child, String tabName) {
        getConstraint(child).setTabName(tabName);
        getWrapper(child).setTabName(child, tabName);
    }

    public void setCanExternalFrame(JComponent child, boolean b) {
        getConstraint(child).setCanExternalFrame(b);
        dockSomewhere(child);
    }

    public void setCanInternalFrame(JComponent child, boolean b) {
        getConstraint(child).setCanInternalFrame(b);
        dockSomewhere(child);
    }

    public void setCanClose(JComponent child, boolean b) {
        getConstraint(child).setCanClose(b);
    }

    public void setCanDockNorth(JComponent child, boolean b) {
        getConstraint(child).setCanDockNorth(b);
        dockSomewhere(child);
    }

    public void setCanDockSouth(JComponent child, boolean b) {
        getConstraint(child).setCanDockSouth(b);
        dockSomewhere(child);
    }

    public void setCanDockEast(JComponent child, boolean b) {
        getConstraint(child).setCanDockEast(b);
        dockSomewhere(child);
    }

    public void setCanDockWest(JComponent child, boolean b) {
        getConstraint(child).setCanDockWest(b);
        dockSomewhere(child);
    }

    //Docking Functions:
    ////////////////////

    public void dockNorth(JComponent child) {
        dockNorth(child, -1);
    }

    public void dockNorth(JComponent child, int idx) {
        dockNorth(getWrapper(child), idx);
    }

    protected void dockNorth(DockWrapper wrapper) {
        dockNorth(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    protected void dockNorth(DockWrapper wrapper, int idx) {
        dock(north, wrapper, idx);
    }

    public void dockSouth(JComponent child) {
        dockSouth(child, -1);
    }

    public void dockSouth(JComponent child, int idx) {
        dockSouth(getWrapper(child), idx);
    }

    protected void dockSouth(DockWrapper wrapper) {
        dockSouth(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    protected void dockSouth(DockWrapper wrapper, int idx) {
        dock(south, wrapper, idx);
    }

    public void dockEast(JComponent child) {
        dockEast(child, -1);
    }

    public void dockEast(JComponent child, int idx) {
        dockEast(getWrapper(child), idx);
    }

    protected void dockEast(DockWrapper wrapper) {
        dockEast(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    protected void dockEast(DockWrapper wrapper, int idx) {
        dock(east, wrapper, idx);
    }

    public void dockWest(JComponent child) {
        dockWest(child, -1);
    }

    public void dockWest(JComponent child, int idx) {
        dockWest(getWrapper(child), idx);
    }

    protected void dockWest(DockWrapper wrapper) {
        dockWest(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    protected void dockWest(DockWrapper wrapper, int idx) {
        dock(west, wrapper, idx);
    }

    /**
     * Dock the given child onto the given parent, which is itself a
     * child of this class.
     */
    public void dock(JComponent outter, JComponent inner) {
        dock(outter, inner, -1);
    }

    /**
     * Dock the given child onto the given parent, which is itself a
     * child of this class.
     */
    public void dock(JComponent outter, JComponent inner, int idx) {
        dock(getWrapper(outter), getWrapper(inner), idx);
    }

    /**
     * Dock the given child onto the given parent, which is itself a
     * child of this class.
     */
    protected void dock(DockWrapper outter, DockWrapper inner) {
        dock(outter, inner, -1);
    }

    /**
     * Dock the given child onto the given parent, which is itself a
     * child of this class.
     */
    protected void dock(DockWrapper outter, DockWrapper inner, int idx) {
        freeWrapper(inner);
        outter.dock(inner, idx);
    }

    /**
     * Set the component to an internal frame
     */
    public void internalFrame(JComponent child) {
        internalFrame(getWrapper(child));
    }

    /**
     * Set the component to an internal frame
     */
    protected void internalFrame(DockWrapper wrapper) {
        freeWrapper(wrapper);
        internalFrameWrappers.add(wrapper);
        wrapper.makeInternalFrame();
    }

    /**
     * Set the component to an external frame
     */
    public void externalFrame(JComponent child) {
        externalFrame(getWrapper(child));
    }

    /**
     * Set the component to an internal frame
     */
    protected void externalFrame(DockWrapper wrapper) {
        freeWrapper(wrapper);
        externalFrameWrappers.add(wrapper);
        wrapper.makeExternalFrame();
    }

    /**
     * Dock the given child somewhere...
     */
    public void dockSomewhere(JComponent child) {
        dockSomewhere(getWrapper(child));
    }

    /**
     * Dock the given child somewhere...
     */
    protected void dockSomewhere(DockWrapper wrapper) {
        if (wrapper == null) {
            throw new RuntimeException("Can't dock null!");
        }
        if (wrapper.canDockNorth()) {
            dockNorth(wrapper);
            return;
        }
        if (wrapper.canDockWest()) {
            dockWest(wrapper);
            return;
        }
        if (wrapper.canDockSouth()) {
            dockSouth(wrapper);
            return;
        }
        if (wrapper.canDockEast()) {
            dockEast(wrapper);
            return;
        }

        if (wrapper.canInternalFrame()) {
            internalFrame(wrapper);
            return;
        }
        if (wrapper.canExternalFrame()) {
            externalFrame(wrapper);
            return;
        }

        Debug.error("DockPanel: Can't dock anywhere...");
        externalFrame(wrapper);
    }

    //Overwrite from Component:
    ///////////////////////////

    /**
     * We need to handle adding the component specially. If it is the
     * background, do one thing. Otherwise wrap it up...
     */
    public Component add(Component comp) {
        add(comp, new DockConstraint());
        return comp;
    }

    /**
     * We need to handle adding the component specially. If it is the
     * background, do one thing. Otherwise wrap it up...
     */
    public void add(Component comp, Object constraints) {
        if (comp == null) {
            throw new RuntimeException("Can't add null component to DockPanel");
        }
        if (comp instanceof DockWrapper) {
            Debug.error("DockPanel: Unexpected call to add with a DockWrapper");
            super.add(comp, constraints);
        } else {
            if (constraints.equals(BACKGROUND)) {
                background = (JComponent) comp;
                super.add(comp, constraints);
                setLayer(comp, BACKGROUND_LAYER.intValue());
            } else if (constraints instanceof DockConstraint) {
                if (comp instanceof JToolBar) {
                    JToolBar t = (JToolBar) comp;
                    t.setFloatable(false);
                }
                setConstraint((JComponent) comp, (DockConstraint) constraints);
                if (!alreadyAdded(comp)) {
                    createDockWrapper((JComponent) comp);
                }
            } else {
                Debug.error("DockPanel: Unexpected constraint: " + constraints);
            }
        }
    }

    public void remove(Component comp) {
        if (comp == null) {
            Debug.error("Trying to remove null component");
        }
        if (comp instanceof DockWrapper) {
            freeWrapper((DockWrapper) comp);
            removeWrapper((DockWrapper) comp);
        } else {
            DockWrapper w = getWrapper((JComponent) comp);
            if (w != null) {
                super.remove(w);
            }
            super.remove(comp);
        }
    }

    public void removeAll() {
        for (int i = 0; i < getComponentCount(); i++) {
            JComponent comp = (JComponent) getComponent(i);
            if (comp instanceof DockWrapper) {
                freeWrapper((DockWrapper) comp);
                removeWrapper((DockWrapper) comp);
            }
        }
        super.removeAll();
    }

    ////
    ////Package/Protected/Private Implementation Functions:
    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////

    protected DockWrapper getNorth() {
        return north;
    }

    protected DockWrapper getSouth() {
        return south;
    }

    protected DockWrapper getEast() {
        return east;
    }

    protected DockWrapper getWest() {
        return west;
    }

    protected int getOverlapTolerance() {
        return 5;
    }

    //Wrapper Functions:
    /////////////////////

    protected void setWrapper(JComponent child, DockWrapper w) {
        childToWrapper.put(child, w);
    }

    protected DockWrapper getWrapper(JComponent child) {
        return (DockWrapper) childToWrapper.get(child);
    }

    protected void removeWrapper(DockWrapper wrapper) {
        for (Iterator iter = wrapper.getChildren().iterator(); iter.hasNext();) {
            JComponent j = (JComponent) iter.next();
            childToWrapper.remove(j);
        }
    }

    /**
     * Remove the wrapper from wherever it is currently
     */
    protected void freeWrapper(DockWrapper w) {
        if (externalFrameWrappers.remove(w)) {
            w.freeWrapper();
            return;
        }
        if (internalFrameWrappers.remove(w)) {
            w.freeWrapper();
            return;
        }
        if (north.freeWrapper(w)) {
            return;
        }
        if (south.freeWrapper(w)) {
            return;
        }
        if (east.freeWrapper(w)) {
            return;
        }
        if (west.freeWrapper(w)) {
            return;
        }
    }

    //Package Accessors for access from DockWrapper:
    ////////////////////////////////////////////////

    /**
     * Get a list of DockConstraint objects for a list of children.
     * 
     * @param children a List of JComponent children
     * @return a List of DockConstraints
     */
    List getConstraints(List children) {
        List ret = new ArrayList(children.size());
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            JComponent child = (JComponent) iter.next();
            ret.add(getConstraint(child));
        }
        return ret;
    }

    /** Create a DockWrapper for the given JComponent. */
    DockWrapper createDockWrapper(JComponent comp) {
        DockWrapper dw = new DockWrapper(this);
        setWrapper(comp, dw);
        dw.addChild(comp);
        dockSomewhere(comp);
        return dw;
    }

    /** Pass back to add the dockwrapper to the layer. */
    void addDockWrapper(DockWrapper dw) {
        super.add(dw, null);
    }

    /** Pass back to remove the dockwrapper from the layer. */
    void removeDockWrapper(DockWrapper dw) {
        super.remove(dw);
    }

    /**
     * Returns true if the component has already been added to a
     * DockWrapper that has been added to the DockPanel.
     */
    boolean alreadyAdded(Component comp) {
        Component components[] = getComponents();
        for (int i = 0; i < components.length; i++) {
            Component c = components[i];
            if (c instanceof DockWrapper) {
                DockWrapper dw = (DockWrapper) c;
                if (dw.getChildren().contains(comp)) {
                    return true;
                }
            }
        }
        return false;
    }
}

