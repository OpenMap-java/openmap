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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/dock/DockWrapper.java,v $
// $RCSfile: DockWrapper.java,v $
// $Revision: 1.7 $
// $Date: 2005/08/09 17:50:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.dock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ButtonUI;

import com.bbn.openmap.util.Debug;

/**
 * A panel that contains controls that will either be docked,
 * internal-framed or external framed...
 * 
 * @author Ben Lubin
 * @version $Revision: 1.7 $ on $Date: 2005/08/09 17:50:51 $
 * @since 12/5/02
 */
public class DockWrapper extends JPanel {

    /* package */static final int UNDEF = -1;
    /* package */static final int EXTERNAL = 1;
    /* package */static final int INTERNAL = 2;
    /* package */static final int DOCK_NORTH = 3;
    /* package */static final int DOCK_SOUTH = 4;
    /* package */static final int DOCK_WEST = 5;
    /* package */static final int DOCK_EAST = 6;

    /** Is this wrapper currently resizable? */
    private boolean resizable = false;

    /** Which docking state we are in */
    private int state = UNDEF;

    /** are we transparent? */
    private boolean transparent = false;

    /** Holds the tabbed pane if we are currently holding tabs. */
    private JTabbedPane tabPane = null;

    BasicDockPanel dockPanel;

    /** Contents of this wrapper, size >1 if tabbed, 1 if not tabbed. */
    private List children = new ArrayList(1);

    /** Other Wrappers that have been docked onto this one. */
    private List dockedWrappers = new ArrayList(0);

    private MouseHandler mouseHandler = new MouseHandler();

    public DockWrapper(BasicDockPanel dp) {
        dockPanel = dp;
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    /** Special constructor for use in the cardinal DockWrappers */
    /* package */DockWrapper(BasicDockPanel dp, int state) {
        this(dp);
        this.state = state;
    }

    //Accessor Methods:
    ///////////////////

    public void addChild(JComponent child) {
        children.add(child);
        if (isTabbed()) {
            String tabName = dockPanel.getConstraint(child).getTabName();
            if (tabName == null) {
                tabName = child.getName();
            }
            tabPane.insertTab(tabName, null, child, null, 0);
        } else {
            add(child, BorderLayout.CENTER);
        }
    }

    public void removeChild(JComponent child) {
        if (children.size() < 1) {
            Debug.error("DockWrapper: Unexpected children list");
        }
        if (isTabbed()) {
            tabPane.remove(child);
        } else {
            remove(child);
        }
        children.remove(child);
    }

    /**
     * Get all of the children that we are holding. The returned list
     * will have one element iff the DockWrapper is not tabbed.
     * 
     * @return a list of JComponents that are the children.
     */
    public List getChildren() {
        return children;
    }

    /**
     * Get the one and only child if we are not tabbed. If we are
     * tabbed, there may be more than one child...
     */
    public JComponent getChild() {
        return (children.isEmpty() ? null : (JComponent) children.get(0));
    }

    //Tabbing Methods:
    //////////////////

    /* package */void setTabName(JComponent child, String name) {
        if (isTabbed()) {
            tabPane.setTitleAt(tabPane.indexOfComponent(child), name);
        }
    }

    /** Returns true iff this dockable contains more than one component */
    public boolean isTabbed() {
        return tabPane != null;
    }

    /**
     * Get the index of the dockwrapper that we should use for
     * tabbing-up, or -1 if there is none.
     */
    public int getDockedWrapperIndexForTabbing() {
        int idx = 0;
        for (Iterator iter = getDockedWrappers().iterator(); iter.hasNext();) {
            DockWrapper dw = (DockWrapper) iter.next();
            if (dw.canTab()) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    private void tab(DockWrapper w) {
        //Get rid of the existing wrapper:
        dockPanel.removeWrapper(w);
        if (children.size() == 1) {
            //Create the tab pane:
            JComponent child = getChild();
            removeChild(child);
            tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
            add(tabPane, BorderLayout.CENTER);
            setOpaque(true);
            addChild(child);
        }
        for (Iterator iter = w.getChildren().iterator(); iter.hasNext();) {
            JComponent child = (JComponent) iter.next();
            dockPanel.setWrapper(child, this);
            addChild(child);
        }
    }

//    private void untab(JComponent child) {
//        if (child == null) {
//            throw new RuntimeException("Can't untab null");
//        }
//        removeChild(child);
//        if (children.size() == 1) {
//            JComponent curChild = getChild();
//            removeChild(curChild);
//            remove(tabPane);
//            tabPane = null;
//            addChild(curChild);
//            setOpaque(false);
//        }
//        DockWrapper dw = dockPanel.createDockWrapper(child);
//    }

    //Transparency Methods:
    ///////////////////////

    public void doLayout() {
        updateTransparency();
        super.doLayout();
    }

    /**
     * Set the indicated JComponent to transparent or not transparent.
     * 
     * @return true iff this call has changed the state
     */
    protected static boolean setTransparent(JComponent child, boolean t) {
        boolean ret = false;
        if (child instanceof JPanel) {
            child.setOpaque(!t);
            ret |= child.isOpaque() == t;
        }
        if (child instanceof JToolBar) {
            child.setOpaque(!t);
            ret |= child.isOpaque() == t;
        }
        if (child instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) child;
            if (t) {
                if (!(b.getUI() instanceof TransparentButtonUI)) {
                    b.setContentAreaFilled(false);
                    b.setUI((ButtonUI) TransparentButtonUI.createUI(b));
                    ret = true;
                }
            } else {
                if (b.getUI() instanceof TransparentButtonUI) {
                    b.setContentAreaFilled(true);
                    b.setUI((ButtonUI) UIManager.getUI(b));
                    ret = true;
                }
            }
        }

        for (int i = 0; i < child.getComponentCount(); i++) {
            Object o = child.getComponent(i);
            if (o instanceof JComponent) {
                JComponent c = (JComponent) o;
                ret |= setTransparent(c, t);
            }
        }
        return ret;
    }

    //Resizing Methods:
    ///////////////////

    protected void makeNotResizable() {
        setBorder(null);
        removeMouseListener(mouseHandler);
        removeMouseMotionListener(mouseHandler);
    }

    protected void makeResizable() {
        setBorder(makeResizeBorder());
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    /** get the border that mouse is in */
    protected int inBorder(int x, int y) {
        Border b = getBorder();
        if (b == null) {
            return UNDEF;
        }
        Insets i = b.getBorderInsets(this);
        if (x <= i.left) {
            return DOCK_WEST;
        }
        if (x >= getWidth() - i.right) {
            return DOCK_EAST;
        }
        if (y <= i.top) {
            return DOCK_NORTH;
        }
        if (y >= getHeight() - i.bottom) {
            return DOCK_SOUTH;
        }
        return UNDEF;
    }

    protected Border makeResizeBorder() {
        Color highlightOuter = UIManager.getColor("controlLtHighlight");
        Color highlightInner = UIManager.getColor("controlHighlight");
        Color shadowOuter = UIManager.getColor("controlDkShadow");
        Color shadowInner = UIManager.getColor("controlShadow");
        if (transparent) {
            highlightOuter = new Color(0, 0, 0, 50);
            highlightInner = new Color(0, 0, 0, 75);
            shadowOuter = new Color(0, 0, 0, 175);
            shadowInner = new Color(0, 0, 0, 150);
        }
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,
                highlightOuter,
                highlightInner,
                shadowOuter,
                shadowInner),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                        highlightOuter,
                        highlightInner,
                        shadowOuter,
                        shadowInner));
        return border;
    }

    //Constraint methods:
    /////////////////////

    /**
     * Set the transparency of this DockWrapper to whatever the
     * Constraint says it should be.
     * 
     * @return true iff the state has changed.
     */
    public boolean updateTransparency() {
        boolean ret = false;
        if (children.size() == 1) {
            boolean t = dockPanel.getConstraint(getChild()).canTransparent();
            transparent = t;
            ret |= setTransparent(getChild(), t);
        } else {
            transparent = false;
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                JComponent c = (JComponent) iter.next();
                ret |= setTransparent(c, transparent);
            }
        }
        return ret;
    }

    /** Make the dock wrapper's border reflect its resizability */
    public void updateResizable() {
        List constraints = dockPanel.getConstraints(getChildren());
        boolean canResize = DockConstraint.canResize(constraints);
        if (resizable && !canResize) {
            makeNotResizable();
        }
        if (!resizable && canResize) {
            makeResizable();
        }
    }

    /** Determine if this can occlude */
    public boolean canOcclude() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canOcclude(constraints);
    }

    /** Determine if this can tab */
    public boolean canTab() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canTab(constraints);
    }

    /** Determine if this can close */
    public boolean canClose() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canClose(constraints);
    }

    /** Determine if this can internalFrame */
    public boolean canInternalFrame() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canInternalFrame(constraints);
    }

    /** Determine if this can externalFrame */
    public boolean canExternalFrame() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canExternalFrame(constraints);
    }

    /** Determine if this can dockNorth */
    public boolean canDockNorth() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canDockNorth(constraints);
    }

    /** Determine if this can dockSouth */
    public boolean canDockSouth() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canDockSouth(constraints);
    }

    /** Determine if this can dockEast */
    public boolean canDockEast() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canDockEast(constraints);
    }

    /** Determine if this can dockWest */
    public boolean canDockWest() {
        List constraints = dockPanel.getConstraints(getChildren());
        return DockConstraint.canDockWest(constraints);
    }

    //State Methods:
    ////////////////

    public void makeExternalFrame() {
        freeWrapper();
        state = EXTERNAL;
    }

    protected void removeExternalFrame() {
        state = UNDEF;
    }

    public void makeInternalFrame() {
        freeWrapper();
        state = INTERNAL;
    }

    protected void removeInternalFrame() {
        state = UNDEF;
    }

    public void makeDockNorth() {
        makeDock(DOCK_NORTH);
    }

    protected void removeDockNorth() {
        removeDock();
    }

    public void makeDockSouth() {
        makeDock(DOCK_SOUTH);
    }

    protected void removeDockSouth() {
        removeDock();
    }

    public void makeDockEast() {
        makeDock(DOCK_EAST);
    }

    protected void removeDockEast() {
        removeDock();
    }

    public void makeDockWest() {
        makeDock(DOCK_WEST);
    }

    protected void removeDockWest() {
        removeDock();
    }

    protected void makeDock(int state) {
        freeWrapper();
        this.state = state;
        dockPanel.addDockWrapper(this);
        orientToolbars();
    }

    protected void orientToolbars() {
        for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
            JComponent child = (JComponent) iter.next();
            if (!(child instanceof JToolBar)) {
                continue;
            }
            JToolBar t = (JToolBar) child;
            switch (state) {
            case DOCK_NORTH:
            case DOCK_SOUTH:
                t.setOrientation(JToolBar.HORIZONTAL);
                break;
            case DOCK_EAST:
            case DOCK_WEST:
                t.setOrientation(JToolBar.VERTICAL);
                break;
            }
        }
    }

    protected void removeDock() {
        dockPanel.removeDockWrapper(this);
        this.state = UNDEF;
    }

    protected void setState(int state) {
        switch (state) {
        case INTERNAL:
            makeInternalFrame();
            break;
        case EXTERNAL:
            makeExternalFrame();
            break;
        case DOCK_NORTH:
            makeDockNorth();
            break;
        case DOCK_SOUTH:
            makeDockSouth();
            break;
        case DOCK_WEST:
            makeDockWest();
            break;
        case DOCK_EAST:
            makeDockEast();
            break;
        }
    }

    //Docking up Methods:
    /////////////////////

    public void freeWrapper() {
        switch (state) {
        case INTERNAL:
            removeInternalFrame();
            break;
        case EXTERNAL:
            removeExternalFrame();
            break;
        case DOCK_NORTH:
            removeDockNorth();
            break;
        case DOCK_SOUTH:
            removeDockSouth();
            break;
        case DOCK_WEST:
            removeDockWest();
            break;
        case DOCK_EAST:
            removeDockEast();
            break;
        }
    }

    /** Attempt to remove the given wrapper from us */
    public boolean freeWrapper(DockWrapper w) {
        if (dockedWrappers.remove(w)) {
            w.freeWrapper();
            return true;
        }
        for (Iterator iter = dockedWrappers.iterator(); iter.hasNext();) {
            DockWrapper dw = (DockWrapper) iter.next();
            if (dw.freeWrapper(w)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDockedWrappers() {
        return !getDockedWrappers().isEmpty();
    }

    public List getDockedWrappers() {
        return dockedWrappers;
    }

    public DockWrapper getDockedWrapper(int idx) {
        return (DockWrapper) dockedWrappers.get(idx);
    }

    public void dock(DockWrapper w) {
        dock(w, -1);
    }

    public void dock(DockWrapper w, int i) {
        w.setState(state);
        int idx = getDockedWrapperIndexForTabbing();
        if (idx != -1 && w.canTab()) {
            getDockedWrapper(idx).tab(w);
        } else {
            if (i == -1) {
                dockedWrappers.add(w);
            } else {
                dockedWrappers.set(i, w);
            }
        }
    }

    /**
     * Recursively determine if another wrapper is docked on this one.
     */
    public boolean isDockedOnRecurse(DockWrapper w) {
        if (this.equals(w)) {
            return true;
        }
        for (Iterator iter = dockedWrappers.iterator(); iter.hasNext();) {
            DockWrapper dw = (DockWrapper) iter.next();
            if (dw.isDockedOnRecurse(w)) {
                return true;
            }
        }
        return false;
    }

    public void setPreferredHeight(int i) {
        Dimension d = getPreferredSize();
        d.height = i;
        setPreferredSize(d);
    }

    public void setPreferredWidth(int i) {
        Dimension d = getPreferredSize();
        d.width = i;
        setPreferredSize(d);
    }

    //Nested Classes:
    /////////////////

    protected class MouseHandler extends MouseInputAdapter {
        int resizeLoc = UNDEF;
        int pressedVal = 0;

        public void mousePressed(MouseEvent e) {
            resizeLoc = inBorder(e.getX(), e.getY());
            switch (resizeLoc) {
            case DOCK_NORTH:
            case DOCK_SOUTH:
                pressedVal = e.getY();
                break;
            case DOCK_EAST:
            case DOCK_WEST:
                pressedVal = e.getX();
                break;
            }
        }

        public void mouseReleased(MouseEvent e) {
            Dimension d = getSize();
            switch (resizeLoc) {
            case DOCK_NORTH:
                d.height += pressedVal - e.getY();
                setPreferredSize(d);
                revalidate();
                break;
            case DOCK_SOUTH:
                d.height -= pressedVal - e.getY();
                setPreferredSize(d);
                revalidate();
                break;
            case DOCK_EAST:
                d.width -= pressedVal - e.getX();
                setPreferredSize(d);
                revalidate();
                break;
            case DOCK_WEST:
                d.width += pressedVal - e.getX();
                setPreferredSize(d);
                revalidate();
                break;
            }
        }

        public void mouseMoved(MouseEvent e) {
            int loc = inBorder(e.getX(), e.getY());
            switch (loc) {
            case DOCK_NORTH:
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                break;
            case DOCK_SOUTH:
                setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                break;
            case DOCK_EAST:
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                break;
            case DOCK_WEST:
                setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                break;
            default:
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}