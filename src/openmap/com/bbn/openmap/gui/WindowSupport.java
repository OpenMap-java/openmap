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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/WindowSupport.java,v $
// $RCSfile: WindowSupport.java,v $
// $Revision: 1.14.2.9 $
// $Date: 2007/03/08 19:22:19 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

import com.bbn.openmap.Environment;
import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.util.Debug;

/**
 * The WindowSupport class provides support for managing JFrames or
 * JInternalFrames for other components. The frame is disposed of when the
 * window is closed, and recreated when displayInWindow is called. The
 * WindowSupport remembers size and location changes for the window when it is
 * recreated.
 * <p>
 * 
 * The WindowSupport class now has inner classes that are used to create
 * different types of windows for components. Dlg is used for JDialogs, which
 * remain on top of the main application frame even when they don't have focus.
 * IntrnlFrm is used for JInternalFrames, for windows that get clipped if the
 * move off the area of the main application. Frm is used for a standard JFrame
 * which can be obscured by the main application window. Components are free to
 * specify which WSDisplay type they always want to be display in, or they can
 * ask for a standard WindowSupport object which will use the static
 * defaultWindowSupportDisplayType variable to determine the WSDisplay type (for
 * a consistent feel across the application). The default setting for this
 * variable is to use the Frm type.
 */
public class WindowSupport
        extends ListenerSupport<ComponentListener>
        implements ComponentListener, ActionListener {

    protected Component content;
    protected String title;
    protected Point componentLocation;
    protected Dimension componentSize;

    public final static String DisplayWindowCmd = "displayWindowCmd";
    public final static String KillWindowCmd = "killWindowCmd";

    protected transient WSDisplay display;

    protected static Class defaultWindowSupportDisplayType = Frm.class;

    /**
     * Create the window support.
     * 
     * @param content the content to display in the window.
     * @param windowTitle the title of the window.
     */
    public WindowSupport(Component content, String windowTitle) {
        super(content);
        setContent(content);
        this.title = windowTitle;
    }

    public WindowSupport(Component content, WSDisplay display) {
        super(content);
        setContent(content);
        setDisplay(display);
        if (display != null) {
            this.title = display.getTitle();
        }
    }

    /**
     * Set the location of the window.
     */
    public void setComponentLocation(Point p) {
        componentLocation = p;
    }

    /**
     * Get the location of the window.
     */
    public Point getComponentLocation() {
        return componentLocation;
    }

    /**
     * Set the size of the window.
     */
    public void setComponentSize(Dimension dim) {
        componentSize = dim;
    }

    /**
     * Get the size of the window.
     */
    public Dimension getComponentSize() {
        return componentSize;
    }

    /**
     * ComponentListener method, new size is noted.
     */
    public void componentResized(ComponentEvent e) {
        Component source = (Component) e.getSource();
        setComponentSize(source.getSize());

        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener) it.next()).componentResized(e);
        }
    }

    /**
     * ComponentListener method, new location is noted.
     */
    public void componentMoved(ComponentEvent e) {
        setComponentLocation(((Component) e.getSource()).getLocation());
        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener) it.next()).componentMoved(e);
        }
    }

    /**
     * ComponentListener method.
     */
    public void componentShown(ComponentEvent e) {
        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener) it.next()).componentShown(e);
        }
    }

    /**
     * ComponentListener method. WindowSupport kills the window when it is
     * hidden.
     */
    public void componentHidden(ComponentEvent e) {
        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener) it.next()).componentHidden(e);
        }

        // We need to do this after componentHidden notifications,
        // otherwise the component never finds out it's been hidden,
        // it gets removed as a ComponentListener at cleanup.
        Component source = (Component) e.getSource();
        if (display == source) {
            cleanUp();
        }
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();
        if (command == KillWindowCmd) {
            killWindow();
        } else if (command == DisplayWindowCmd) {
            displayInWindow();
        }
    }

    protected void finalize() {
        if (Debug.debugging("gc")) {
            Debug.output("WindowSupport being gc'd");
        }
    }

    /**
     * Sets the title of the JInternalFrame/JDialog.
     */
    public void setTitle(String tle) {
        title = tle;
        if (display != null) {
            display.setTitle(tle);
        }
    }

    public String getTitle() {
        return title;
    }

    /**
     * Sets the content in the JInternalFrame/JDialog.
     */
    public void setContent(Component comp) {

        if (content instanceof ComponentListener) {
            removeComponentListener((ComponentListener) content);
        }

        content = comp;
        if (display != null) {
            display.setContent(comp);
        }

        if (content instanceof ComponentListener) {
            addComponentListener((ComponentListener) content);
        }
    }

    public Component getContent() {
        return content;
    }

    protected int maxHeight = -1;
    protected int maxWidth = -1;

    /**
     * Sets the maximum pixel size of the window. If you don't care about a
     * particular dimension, set it to be less than zero and the natural size of
     * the content will be displayed.
     */
    public void setMaxSize(int width, int height) {
        maxHeight = height;
        maxWidth = width;
    }

    /**
     * Called when a component hasn't specified what kind of window they want.
     * If the Environment.useInternalFrames flag isn't set, then the
     * getDefaultWindowSupportDisplayType() method is called to find out which
     * WSDisplay type class should be created for the component. IF that returns
     * null, a Frm is created.
     * 
     * @param owner
     * @return WSDisplay
     */
    protected WSDisplay createDisplay(Frame owner) {
        WSDisplay wsd;
        if (persistentDisplayType == null && Environment.getBoolean(Environment.UseInternalFrames)) {
            wsd = new IntrnlFrm(title);
        } else {
            Class wTypeClass = persistentDisplayType == null ? getDefaultWindowSupportDisplayType() : persistentDisplayType;
            if (wTypeClass == Dlg.class) {
                wsd = new Dlg(owner, title);
            } else if (wTypeClass == IntrnlFrm.class) {
                wsd = new IntrnlFrm(title);
            } else {
                wsd = new Frm(title);
            }
        }
        setDisplay(wsd);
        return wsd;
    }

    /**
     * Called when a display type is known, and may override the default
     * settings. A Frm is created if the displayType is not null but not a valid
     * WSDisplay type.
     * 
     * @param owner
     * @param displayType a WSDisplay class to create. If null, the
     *        persistentDisplayType will be used.
     * @return WSDisplay
     */
    protected WSDisplay createDisplay(Frame owner, Class displayType) {
        WSDisplay wsd;

        if (displayType == null) {
            return createDisplay(owner);
        }

        if (displayType == Dlg.class) {
            wsd = new Dlg(owner, title);
        } else if (displayType == IntrnlFrm.class) {
            wsd = new IntrnlFrm(title);
        } else {
            wsd = new Frm(title);
        }

        setDisplay(wsd);
        return wsd;
    }

    public static Class getDefaultWindowSupportDisplayType() {
        return defaultWindowSupportDisplayType;
    }

    public static void setDefaultWindowSupportDisplayType(Class defaultWindowSupportDisplayType) {
        WindowSupport.defaultWindowSupportDisplayType = defaultWindowSupportDisplayType;
    }

    public void setDisplay(WSDisplay dis) {
        if (display != null) {
            display.removeComponentListener(this);
        }

        display = dis;
        if (display != null) {
            display.addComponentListener(this);
            display.setContent(modifyContent(content));
        }

    }

    /**
     * Subclass method to allow modifications to content, wrappers, etc. This
     * version just returns comp.
     */
    public Component modifyContent(Component comp) {
        return comp;
    }

    public WSDisplay getDisplay() {
        return display;
    }

    /**
     * Display the window, and find out what the natural or revised size and
     * location are for the window.
     */
    public void displayInWindow() {
        displayInWindow(null);
    }

    /**
     * Display the window, and find out what the natural or revised size and
     * location are for the window.
     * 
     * @param owner Frame for JDialog
     */
    public void displayInWindow(Frame owner) {

        Dimension dim = getComponentSize();
        if (dim != null) {
            content.setSize(dim);
        }

        // -1 is a flag for the positioning code to recenter the
        // -window on the owner if it's not null, for JDialogs.

        displayInWindow(owner, -1, -1, -1, -1);
    }

    /**
     * Display the window.
     * 
     * @param x the horizontal pixel location for the window.
     * @param y the vertical pixel location for the window.
     * @param width the horizontal size of the window, if less than or equal to
     *        zero the content size will be used.
     * @param height the vertical size of the window, if less than or equal to
     *        zero the content size will be used.
     */
    public void displayInWindow(int x, int y, int width, int height) {
        displayInWindow(null, x, y, width, height);
    }

    /**
     * Display the window.
     * 
     * @param owner Frame for JDialog
     * @param x the horizontal pixel location for the window.
     * @param y the vertical pixel location for the window.
     * @param width the horizontal size of the window, if less than or equal to
     *        zero the content size will be used.
     * @param height the vertical size of the window, if less than or equal to
     *        zero the content size will be used.
     */
    public void displayInWindow(Frame owner, int x, int y, int width, int height) {
        displayInWindow(owner, null, x, y, width, height);
    }

    /**
     * Display the window.
     * 
     * @param owner Frame for JDialog
     * @param displayType the WSDisplay class to use for the window.
     * @param x the horizontal pixel location for the window.
     * @param y the vertical pixel location for the window.
     * @param width the horizontal size of the window, if less than or equal to
     *        zero the content size will be used.
     * @param height the vertical size of the window, if less than or equal to
     *        zero the content size will be used.
     */
    public void displayInWindow(Frame owner, Class displayType, int x, int y, int width, int height) {

        if (content == null) {
            Debug.message("windowsupport", "WindowSupport asked to display window with null content");
            return;
        }

        if (x < 0 && y < 0) {
            // See if we can remember where we were...
            Point loc = getComponentLocation();
            if (loc != null) {
                x = (int) loc.getX();
                y = (int) loc.getY();
            }
        }

        if (display != null && displayType != null && !display.getClass().getName().equals(displayType.getName())) {
            display.dispose();
            display = null;
        }

        if (display == null) {
            display = createDisplay(owner, displayType);
        }

        Container displayWindow = display.getWindow();
        checkBounds(displayWindow, x, y, width, height);

        display.show(x, y);

        setComponentLocation(displayWindow.getLocation());
        setComponentSize(displayWindow.getSize());
    }

    /**
     * Checks the component's dimensions against the requested values and
     * against any maximum limits that may have been set in the WindowSupport.
     * Calls setBounds() on the Component.
     */
    protected void checkBounds(Component comp, int x, int y, int width, int height) {
        if (comp != null) {

            if (width <= 0) {
                width = comp.getWidth();
            }

            if (maxWidth > 0 && width > maxWidth) {
                width = maxWidth;
            }

            if (height <= 0) {
                height = comp.getHeight();
            }

            if (maxHeight > 0 && height > maxHeight) {
                height = maxHeight;
            }

            comp.setBounds(x, y, width, height);
        }
    }

    /**
     * For applications, checks where the Environment says the window should be
     * placed, and then uses the packed height and width to make adjustments.
     */
    protected static void setDefaultPosition(Component comp) {
        // get starting width and height
        int w = comp.getWidth();
        int h = comp.getHeight();

        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5100801
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        int x = width / 2 - w / 2;
        int y = height / 2 - h / 2;
        Debug.message("basic", "Screen dimensions are " + gd.getDisplayMode());
        // Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        // Debug.message("basic", "Screen dimensions are " + d);
        // int x = d.width / 2 - w / 2;
        // int y = d.height / 2 - h / 2;

        if (Debug.debugging("basic")) {
            Debug.output("Setting PLG frame X and Y from properties to " + x + " " + y);
        }

        // compose the frame, but don't show it here
        comp.setBounds(x, y, w, h);
    }

    /**
     * Set the window to be hidden and fire a ComponentEvent for
     * COMPONENT_HIDDEN. Normally, just setting the visibility of the window
     * would be enough, but we're running into that problem we had with the
     * layers not firing ComponentEvents when hidden. This method calls
     * componentHidden, which in turn calls cleanUp.
     */
    public void killWindow() {

        ComponentEvent ce = null;
        if (display != null) {
            ce = display.kill();
        }

        if (ce != null) {
            componentHidden(ce);
        }
    }

    protected Class persistentDisplayType;

    /**
     * Get rid of the window used to display the content.
     */
    protected void cleanUp() {
        if (display != null) {
            persistentDisplayType = display.getClass();
            WSDisplay wsd = display;
            setDisplay(null);
            wsd.dispose();
        }

        // This seems to half-disconnect the window support from the content,
        // and I am not sure it's necessary to remove the listeners. This was
        // originally done to make it easier to release memory, but having the
        // WindowSupport hold on to listeners won't prevent that from happening.

        // if (content instanceof ComponentListener) {
        // removeComponentListener((ComponentListener) content);
        // }
    }

    /**
     * Add a component listener that is interested in hearing about what happens
     * to the window.
     */
    public void addComponentListener(ComponentListener l) {
        add(l);
    }

    /**
     * Remove a component listener that was interested in hearing about what
     * happens to the window.
     */
    public void removeComponentListener(ComponentListener l) {
        remove(l);
    }

    /**
     * Return the window displaying the content. May be null.
     */
    public Container getWindow() {
        if (display != null) {
            return display.getWindow();
        } else {
            return null;
        }
    }

    public static interface WSDisplay {

        public void setTitle(String title);

        public String getTitle();

        public Container getWindow();

        public void setContent(Component content);

        public void show(int x, int y);

        public ComponentEvent kill();

        public void dispose();

        public Dimension getContentSize();

        public void addComponentListener(ComponentListener cl);

        public void removeComponentListener(ComponentListener cl);

    }

    public static class IntrnlFrm
            extends JInternalFrame
            implements WSDisplay {

        public IntrnlFrm(String title) {
            super(title,
            /* resizable */true,
            /* closable */true,
            /* maximizable */true,
            /* iconifiable */true);
            setOpaque(true);

            JLayeredPane desktop = Environment.getInternalFrameDesktop();

            Debug.message("windows", "WindowSupport creating internal frame");

            if (desktop != null) {
                desktop.remove(this);
                desktop.add(this, JLayeredPane.PALETTE_LAYER);
            } else {
                Debug.output("WindowSupport:  No desktop set for internal frame");
            }
        }

        public Container getWindow() {
            return this;
        }

        public ComponentEvent kill() {
            setVisible(false);
            return new ComponentEvent(this, ComponentEvent.COMPONENT_HIDDEN);
        }

        public void setContent(Component content) {
            Container cp = getContentPane();
            // cp.removeAll();
            cp.add(content);
            pack();
        }

        public Dimension getContentSize() {
            return getContentPane().getSize();
        }

        public void show(int x, int y) {
            if (!isVisible()) {
                super.show();
            }
            toFront();
        }

    }

    public static class Dlg
            extends JDialog
            implements WSDisplay {

        public Dlg(Frame owner, String title) {
            super(owner, title);
            Debug.message("windows", "WindowSupport creating frame");
        }

        public Container getWindow() {
            return this;
        }

        public ComponentEvent kill() {
            setVisible(false);
            return new ComponentEvent(this, ComponentEvent.COMPONENT_HIDDEN);
        }

        public void setContent(Component content) {
            Container cp = getContentPane();
            cp.removeAll();
            cp.add(content);
            pack();
        }

        public Dimension getContentSize() {
            return getContentPane().getSize();
        }

        public void show(int x, int y) {
            if (!isVisible()) {
                Window owner = getOwner();
                if (x <= 0 && y <= 0) {
                    if (owner != null) {
                        setLocationRelativeTo(owner);
                    } else if (owner == null) {
                        setDefaultPosition(this);
                    }
                }
            }
            super.setVisible(true);
        }

    }

    public static class Frm
            extends JFrame
            implements WSDisplay {

        public Frm(String title) {
            super(title);
            // Need to call this to get the frame to pay attention to requests
            // on where to locate it if it should be centered on the screen.
            setLocation(-1, -1);
        }

        public Frm(String title, boolean undecorated) {
            super(title);
            setUndecorated(undecorated);
            // Need to call this to get the frame to pay attention to requests
            // on where to locate it if it should be centered on the screen.
            setLocation(-1, -1);
        }

        public Container getWindow() {
            return this;
        }

        public ComponentEvent kill() {
            setVisible(false);
            return new ComponentEvent(this, ComponentEvent.COMPONENT_HIDDEN);
        }

        public void setContent(Component content) {
            Container cp = getContentPane();
            cp.removeAll();
            cp.add(content);
            pack();
        }

        public Dimension getContentSize() {
            return getContentPane().getSize();
        }

        public void show(int x, int y) {
            if (!isVisible()) {
                Window owner = getOwner();
                if (x <= 0 && y <= 0) {
                    if (owner != null) {
                        setLocationRelativeTo(owner);
                    } else if (owner == null) {
                        setDefaultPosition(this);
                    }
                }
                super.setVisible(true);
            }
            toFront();
        }
    }

}