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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/dock/DockPanel.java,v $
// $RCSfile: DockPanel.java,v $
// $Revision: 1.1 $
// $Date: 2003/04/08 17:33:14 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.dock;

import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.*;
import java.util.*;

import com.bbn.openmap.util.Debug;

/**
 * A component that has a background component and docking children.
 * @author Ben Lubin
 * @version $Revision: 1.1 $ on $Date: 2003/04/08 17:33:14 $
 * @since 12/5/02
 */
public class DockPanel extends JLayeredPane {

    /** Constraint for the background component */
    public static final String BACKGROUND =
	BorderLayout.CENTER;

    /** Layer where we add the background component */
    private static final Integer BACKGROUND_LAYER = new Integer(-100);

    /** The one and only background component */
    private JComponent background;

    /** Maps children to their constraint objects */
    private HashMap childToConstraint = new HashMap(11);

    /** Maps children to their wrappers (not 1-to-1 when tabbing involved) */
    private HashMap childToWrapper = new HashMap(11);

    /** A list of wrappers that are currently wrapped in JFrames */
    private List externalFrameWrappers = new ArrayList(0);

    /** A list of wrappers that are currently loose InternalFrames */
    private List internalFrameWrappers = new ArrayList(0);

    /** 
     *Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper north = new DockWrapper(this, DockWrapper.DOCK_NORTH);
    /** 
     *Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper south = new DockWrapper(this, DockWrapper.DOCK_SOUTH);
    /** 
     *Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper east  = new DockWrapper(this, DockWrapper.DOCK_EAST);
    /** 
     *Invisible root dock wrapper, that will be pegged to the edge of
     * the panel.
     */
    private DockWrapper west  = new DockWrapper(this, DockWrapper.DOCK_WEST);

    public DockPanel() {
	super();
	setLayout(new DockLayout(this));
	setBackground(UIManager.getColor("control"));
    }

    //Accessors:
    ////////////

    public int getOverlapTolerance() {
	return 5;
    }

    /*package*/ DockWrapper getNorth() { return north;}
    /*package*/ DockWrapper getSouth() { return south;}
    /*package*/ DockWrapper getEast() { return east;}
    /*package*/ DockWrapper getWest() { return west;}
    
    public JComponent getBackgroundComponent() {
	return background;
    }

    public void setConstraint(JComponent child, DockConstraint c) {
	childToConstraint.put(child, c);
    }

    public DockConstraint getConstraint(JComponent child) {
	return (DockConstraint) childToConstraint.get(child);
    }

    /**
     * Get a list of DockConstraint objects for a list of children.
     * @param children a List of JComponent children
     * @return a List of DockConstraints
     */
    public List getConstraints(List children) {
	List ret = new ArrayList(children.size());
	for (Iterator iter = children.iterator(); iter.hasNext();) {
	    JComponent child = (JComponent)iter.next();
	    ret.add(getConstraint(child));
	}
	return ret;
    }

    public void removeConstraint(JComponent child) {
	childToConstraint.remove(child);
    }

    public void setWrapper(JComponent child, DockWrapper w) {
	childToWrapper.put(child, w);
    }

    public DockWrapper getWrapper(JComponent child) {
	return (DockWrapper)childToWrapper.get(child);
    }

    public void removeWrapper(DockWrapper wrapper) {
	for(Iterator iter = wrapper.getChildren().iterator(); iter.hasNext();){
	    JComponent j = (JComponent)iter.next();
	    childToWrapper.remove(j);
	}
    }

    //Setup Functions:
    //////////////////

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
     * Set the name of the tab to use when the component is tabbed 
     * (if it can tab).  If unspecified, defaults to Component.getName()
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

    /**
     * Remove the wrapper from wherever it is currently 
     */
    public void freeWrapper(DockWrapper w) {
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

    public void dockNorth(JComponent child) {
	dockNorth(child, -1);
    }

    public void dockNorth(JComponent child, int idx) {
	dockNorth(getWrapper(child), idx);
    }

    public void dockNorth(DockWrapper wrapper) {
	dockNorth(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    public void dockNorth(DockWrapper wrapper, int idx) {
	dock(north, wrapper, idx);
    }

    public void dockSouth(JComponent child) {
	dockSouth(child, -1);
    }

    public void dockSouth(JComponent child, int idx) {
	dockSouth(getWrapper(child), idx);
    }

    public void dockSouth(DockWrapper wrapper) {
	dockSouth(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    public void dockSouth(DockWrapper wrapper, int idx) {
	dock(south, wrapper, idx);
    }

    public void dockEast(JComponent child) {
	dockEast(child, -1);
    }

    public void dockEast(JComponent child, int idx) {
	dockEast(getWrapper(child), idx);
    }

    public void dockEast(DockWrapper wrapper) {
	dockEast(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    public void dockEast(DockWrapper wrapper, int idx) {
	dock(east, wrapper, idx);
    }

    public void dockWest(JComponent child) {
	dockWest(child, -1);
    }

    public void dockWest(JComponent child, int idx) {
	dockWest(getWrapper(child), idx);
    }

    public void dockWest(DockWrapper wrapper) {
	dockWest(wrapper, -1);
    }

    /**
     * Dock the given child into the given position on the MapPanel
     */
    public void dockWest(DockWrapper wrapper, int idx) {
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
    public void dock(DockWrapper outter, DockWrapper inner) {
	dock(outter, inner, -1);
    }    

    /**
     * Dock the given child onto the given parent, which is itself a 
     * child of this class.
     */
    public void dock(DockWrapper outter, DockWrapper inner, int idx) {
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
    public void internalFrame(DockWrapper wrapper) {
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
    public void externalFrame(DockWrapper wrapper) {
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
    public void dockSomewhere(DockWrapper wrapper) {
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

    //Low level functions:
    //////////////////////

    /** 
     * We need to handle adding the component specially.
     * If it is the background, do one thing.  Otherwise
     * wrap it up...
     */
    public Component add(Component comp) {
        add(comp, new DockConstraint());
	return comp;
    }

    /** 
     * We need to handle adding the component specially.
     * If it is the background, do one thing.  Otherwise
     * wrap it up...
     */
    public void add(Component comp, Object constraints) {
	if (comp instanceof DockWrapper) {
	    Debug.error("DockPanel: Unexpected call to add with a DockWrapper");
	    DockWrapper dw = (DockWrapper) comp;
	    super.add(comp, constraints);
	} else {
	    if (constraints.equals(BACKGROUND)) {
		background = (JComponent)comp;
		super.add(comp, constraints);
		setLayer(comp, BACKGROUND_LAYER.intValue());
	    } else if (constraints instanceof DockConstraint) {
		if (comp instanceof JToolBar) {
		    JToolBar t = (JToolBar)comp;
		    t.setFloatable(false);
		}		
		setConstraint((JComponent)comp, 
			      (DockConstraint)constraints);		
		createDockWrapper((JComponent)comp);
	    } else {
		Debug.error("DockPanel: Unexpected constraint: " + constraints);
	    }
	}
    }

    /** Create a DockWrapper for the given JComponent. */
    /* package */ DockWrapper createDockWrapper(JComponent comp) {
	DockWrapper dw = new DockWrapper(this);
	setWrapper(comp, dw);
	dw.addChild(comp);
	dockSomewhere(comp);		
	return dw;
    }

    /** Pass back to add the dockwrapper to the layer. */
    /* package */ void addDockWrapper(DockWrapper dw) {
	super.add(dw, null);	
    }

    /** Pass back to remove the dockwrapper from the layer. */
    /* package */ void removeDockWrapper(DockWrapper dw) {
	super.remove(dw);
    }

    public void remove(Component comp) {
	if (comp instanceof DockWrapper) {
	    freeWrapper((DockWrapper)comp);
	    removeWrapper((DockWrapper)comp);
	} else {
	    DockWrapper w = getWrapper((JComponent)comp);
	    if (w != null) {
		super.remove(w);
	    }
	    super.remove(comp);
	}
    }

    public void removeAll() {
	for (int i=0; i<getComponentCount(); i++) {
	    JComponent comp = (JComponent)getComponent(i);
	    if (comp instanceof DockWrapper) {
		freeWrapper((DockWrapper)comp);
		removeWrapper((DockWrapper)comp);
	    }
	}
	super.removeAll();
    }
}

