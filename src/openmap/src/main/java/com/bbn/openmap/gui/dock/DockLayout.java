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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/dock/DockLayout.java,v $
// $RCSfile: DockLayout.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 17:50:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.dock;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JComponent;

import com.bbn.openmap.util.Debug;

/**
 * A Layout manager used for the docking component...
 * 
 * @author Ben Lubin
 * @version $Revision: 1.5 $ on $Date: 2005/08/09 17:50:51 $
 * @since 12/5/02
 */
public class DockLayout implements LayoutManager2 {

    private BasicDockPanel p;

    /** Size of the background control */
    protected Rectangle occludingBounds;

    /** Size of the background control */
    protected Rectangle backgroundBounds;

    public DockLayout(BasicDockPanel p) {
        this.p = p;
    }

    //Layout Query Functions:
    /////////////////////////

    /** Account for the extra size of the cardinal DockWrapper. */
    protected int getHeightAtLeftCardinal(DockWrapper dw) {
        return getHeightAtLeft(dw) - p.getOverlapTolerance();
    }

    /** Account for the extra size of the cardinal DockWrapper. */
    protected int getHeightAtRightCardinal(DockWrapper dw) {
        return getHeightAtRight(dw) - p.getOverlapTolerance();
    }

    /** Account for the extra size of the cardinal DockWrapper. */
    protected int getWidthAtYCardinal(DockWrapper dw, int y) {
        return getWidthAtY(dw, y) - p.getOverlapTolerance();
    }

    /**
     * Get the height at the left point for a north or south
     * DockWrapper.
     */
    protected int getHeightAtLeft(DockWrapper dw) {
        int ret = dw.getPreferredSize().height;
        java.util.List l = dw.getDockedWrappers();
        if (!l.isEmpty()) {
            ret += getHeightAtLeft((DockWrapper) l.get(0));
        }
        return ret;
    }

    /**
     * Get the height at the right point for a north or south
     * DockWrapper.
     */
    protected int getHeightAtRight(DockWrapper dw) {
        int ret = dw.getPreferredSize().height;
        java.util.List l = dw.getDockedWrappers();
        if (!l.isEmpty()) {
            ret += getHeightAtRight((DockWrapper) l.get(l.size() - 1));
        }
        return ret;
    }

    /**
     * Get the width at the given point for a east or west
     * DockWrapper.
     */
    protected int getWidthAtY(DockWrapper dw, int y) {
        Rectangle bounds = dw.getBounds();
        int ret = 0;
        if ((bounds.y <= y) && (bounds.y + bounds.height >= y)) {
            ret = bounds.width;
        }
        for (Iterator iter = dw.getDockedWrappers().iterator(); iter.hasNext();) {
            DockWrapper c = (DockWrapper) iter.next();
            ret += getWidthAtY(c, y);
        }
        return ret;
    }

    //Layout Functions:
    ///////////////////

    /**
     * Layout the entire container.
     */
    protected void layoutContainer() {
        Rectangle inBounds = p.getBounds();
        Insets insets = p.getInsets();
        inBounds.x += insets.left;
        inBounds.width -= insets.left;
        inBounds.width -= insets.right;
        inBounds.y += insets.top;
        inBounds.height -= insets.top;
        inBounds.height -= insets.bottom;

        backgroundBounds = (Rectangle) inBounds.clone();
        occludingBounds = (Rectangle) inBounds.clone();

        layoutCardinals();
        layoutEast(p.getEast(),
                occludingBounds.x + occludingBounds.width,
                occludingBounds.y,
                occludingBounds.width,
                occludingBounds.height);

        layoutWest(p.getWest(),
                occludingBounds.x,
                occludingBounds.y,
                occludingBounds.width,
                occludingBounds.height);

        int southLeft = inBounds.x
                + getWidthAtYCardinal(p.getWest(), inBounds.y + inBounds.height
                        - getHeightAtLeftCardinal(p.getSouth()));
        int southRight = inBounds.x
                + inBounds.width
                - getWidthAtYCardinal(p.getEast(), inBounds.y + inBounds.height
                        - getHeightAtRightCardinal(p.getSouth()));
        layoutSouth(p.getSouth(),
                southLeft,
                occludingBounds.y + occludingBounds.height,
                southRight - southLeft,
                occludingBounds.height);

        int northLeft = inBounds.x
                + getWidthAtYCardinal(p.getWest(), inBounds.y
                        + getHeightAtLeftCardinal(p.getNorth()));
        int northRight = inBounds.x
                + inBounds.width
                - getWidthAtYCardinal(p.getEast(), inBounds.y
                        + getHeightAtRightCardinal(p.getNorth()));
        layoutNorth(p.getNorth(), northLeft, occludingBounds.y, northRight
                - northLeft, occludingBounds.height);

        layoutBackground();
    }

    protected void layoutCardinals() {
        p.getNorth().setBounds(0, 0, p.getWidth(), p.getOverlapTolerance());
        p.getSouth().setBounds(0,
                p.getHeight() - p.getOverlapTolerance(),
                p.getWidth(),
                p.getOverlapTolerance());
        p.getEast().setBounds(p.getWidth() - p.getOverlapTolerance(),
                0,
                p.getOverlapTolerance(),
                p.getWidth());
        p.getWest().setBounds(0, 0, p.getOverlapTolerance(), p.getWidth());
    }

    /**
     * Layout west side.
     * 
     * @param x right most edge.
     * @param y top most edge.
     */
    protected void layoutEast(DockWrapper base, int x, int y, int maxwidth,
                              int maxheight) {
        for (Iterator iter = base.getDockedWrappers().iterator(); iter.hasNext();) {
            DockWrapper dw = (DockWrapper) iter.next();
            Dimension d = dw.getPreferredSize();
            int w = min(d.width, maxwidth);
            int h = min(d.height, maxheight);
            dw.setBounds(x - w, y, w, h);
            if (!dw.canOcclude()) {
                updateRight(backgroundBounds, x - w);
            }
            updateRight(occludingBounds, x - w);
            layoutEast(dw, x - w, y, maxwidth - w, maxheight);
            y += h;
            maxheight -= h;
        }
    }

    /**
     * Layout west side.
     * 
     * @param x left most edge.
     * @param y top most edge.
     */
    protected void layoutWest(DockWrapper base, int x, int y, int maxwidth,
                              int maxheight) {
        for (Iterator iter = base.getDockedWrappers().iterator(); iter.hasNext();) {
            DockWrapper dw = (DockWrapper) iter.next();
            Dimension d = dw.getPreferredSize();
            int w = min(d.width, maxwidth);
            int h = min(d.height, maxheight);
            dw.setBounds(x, y, w, h);
            if (!dw.canOcclude()) {
                updateLeft(backgroundBounds, x + w);
            }
            updateLeft(occludingBounds, x + w);
            layoutWest(dw, x + w, y, maxwidth - w, maxheight);
            y += h;
            maxheight -= h;
        }
    }

    /**
     * Layout north side.
     * 
     * @param x left most edge.
     * @param y top most edge.
     */
    protected void layoutNorth(DockWrapper base, int x, int y, int maxwidth,
                               int maxheight) {
        for (Iterator iter = base.getDockedWrappers().iterator(); iter.hasNext();) {
            DockWrapper dw = (DockWrapper) iter.next();
            Dimension d = dw.getPreferredSize();
            int w = min(d.width, maxwidth);
            int h = min(d.height, maxheight);
            dw.setBounds(x, y, w, h);
            if (!dw.canOcclude()) {
                updateTop(backgroundBounds, y + h);
            }
            updateTop(occludingBounds, y + h);
            layoutNorth(dw, x, y + h, maxwidth, maxheight - h);
            x += w;
            maxwidth -= w;
        }
    }

    /**
     * Layout north side.
     * 
     * @param x left most edge.
     * @param y bottom most edge.
     */
    protected void layoutSouth(DockWrapper base, int x, int y, int maxwidth,
                               int maxheight) {
        for (Iterator iter = base.getDockedWrappers().iterator(); iter.hasNext();) {
            DockWrapper dw = (DockWrapper) iter.next();
            Dimension d = dw.getPreferredSize();
            int w = min(d.width, maxwidth);
            int h = min(d.height, maxheight);
            dw.setBounds(x, y - h, w, h);
            //      System.out.println("RES: "+ dw.getBounds());
            if (!dw.canOcclude()) {
                updateBottom(backgroundBounds, y - h);
            }
            updateBottom(occludingBounds, y - h);
            layoutSouth(dw, x, y - h, maxwidth, maxheight - h);
            x += w;
            maxwidth -= w;
        }
    }

    protected void layoutBackground() {
        JComponent background = p.getBackgroundComponent();
        if (background != null) {
            /*
             * backgroundBounds.x += 10; backgroundBounds.y += 10;
             * backgroundBounds.width -= 20; backgroundBounds.height -=
             * 20;
             */
            background.setBounds(backgroundBounds);
        }
    }

    // Utilities:
    /////////////

    protected int min(int a, int b) {
        return Math.min(a, b);
    }

    protected int max(int a, int b) {
        return Math.max(a, b);
    }

    protected void updateLeft(Rectangle r, int left) {
        int tmp = left - r.x;
        if (tmp > 0) {
            r.x += tmp;
            r.width -= tmp;
        }
    }

    protected void updateRight(Rectangle r, int right) {
        r.width = min(r.x + r.width, right);
    }

    protected void updateTop(Rectangle r, int top) {
        int tmp = top - r.y;
        if (tmp > 0) {
            r.y += tmp;
            r.height -= tmp;
        }
    }

    protected void updateBottom(Rectangle r, int bottom) {
        r.height = min(r.y + r.height, bottom);
    }

    // From LayoutManager2:
    ///////////////////////

    public void layoutContainer(Container parent) {
        if (parent != p) {
            Debug.error("DockLayout: Asked to layout unexpected container");
            return;
        }
        layoutContainer();
    }

    public void addLayoutComponent(String name, Component comp) {}

    public void addLayoutComponent(Component comp, Object constraints) {}

    public void removeLayoutComponent(Component comp) {}

    public Dimension preferredLayoutSize(Container parent) {
        JComponent background = p.getBackgroundComponent();
        if (background != null) {
            return background.getPreferredSize();
        }
        return new Dimension(0, 0);
    }

    public Dimension minimumLayoutSize(Container parent) {
        JComponent background = p.getBackgroundComponent();
        if (background != null) {
            return background.getMinimumSize();
        }
        return new Dimension(0, 0);
    }

    public Dimension maximumLayoutSize(Container parent) {
        JComponent background = p.getBackgroundComponent();
        if (background != null) {
            return background.getMaximumSize();
        }
        return new Dimension(0, 0);
    }

    public float getLayoutAlignmentX(Container target) {
        return .5f;
    }

    public float getLayoutAlignmentY(Container target) {
        return .5f;
    }

    public void invalidateLayout(Container target) {}
}

