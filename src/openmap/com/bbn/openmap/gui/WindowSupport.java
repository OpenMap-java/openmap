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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/WindowSupport.java,v $
// $RCSfile: WindowSupport.java,v $
// $Revision: 1.15 $
// $Date: 2004/09/17 18:12:36 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.util.Debug;

/**
 * The WindowSupport class provides support for managing JFrames or
 * JInternalFrames for other components.  The frame is disposed of
 * when the window is closed, and recreated when displayInWindow is
 * called.  The WindowSupport remembers size and location changes for
 * the window when it is recreated.
 */
public class WindowSupport extends ListenerSupport 
    implements ComponentListener, ActionListener {

    protected Component content;
    protected String title;
    protected Point componentLocation;
    protected Dimension componentSize;

    public final static String DisplayWindowCmd = "displayWindowCmd";
    public final static String KillWindowCmd = "killWindowCmd";

    /** 
     * The frame used when the DrawingToolLauncher is used in an
     * applet, or if Environment.useInternalFrames == true;
     */
    protected transient JInternalFrame iFrame;

    /**
     * The dialog used for non-internal windows.
     */
    protected transient JDialog dialog;

    /**
     * Create the window support.
     * @param content the content to display in the window.
     * @param windowTitle the title of the window.
     */
    public WindowSupport(Component content, String windowTitle) {
        super(content);
        this.content = content;
        this.title = windowTitle;
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
        Component source = (Component)e.getSource();
        if (source instanceof JFrame) {
            source = ((JFrame)source).getContentPane();
        } else if (source instanceof JInternalFrame) {
            source = ((JInternalFrame)source).getContentPane();
        } else if (source instanceof JDialog) {
            source = ((JDialog)source).getContentPane();
        }
        setComponentSize(new Dimension(source.getWidth(), source.getHeight()));

        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener)it.next()).componentResized(e);
        }
    }

    /**
     * ComponentListener method, new location is noted.
     */
    public void componentMoved(ComponentEvent e) {
        setComponentLocation(((Component)e.getSource()).getLocation());
        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener)it.next()).componentMoved(e);
        }
    }

    /**
     * ComponentListener method.
     */
    public void componentShown(ComponentEvent e) {
        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener)it.next()).componentShown(e);
        }
    }

    /**
     * ComponentListener method. WindowSupport kills the window when
     * it is hidden.
     */
    public void componentHidden(ComponentEvent e) {
        Component source = (Component)e.getSource();
        if (source == dialog || source == iFrame) {
            cleanUp();
        }

        Iterator it = iterator();
        while (it.hasNext()) {
            ((ComponentListener)it.next()).componentHidden(e);
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
        if (iFrame != null) {
            iFrame.setTitle(tle);
        } else if (dialog != null) {
            dialog.setTitle(tle);
        }
    }

    public String getTitle() {
        return title;
    }

    /**
     * Subclass method to allow modifications to content, wrappers,
     * etc.  This version just returns comp.
     */
    public Component modifyContent(Component comp) {
        return comp;
    }

    /**
     * Sets the content in the JInternalFrame/JDialog.
     */
    public void setContent(Component comp) {
        content = comp;
        if (iFrame != null) {
            iFrame.getContentPane().add(modifyContent(content));
            iFrame.pack();
        } else if (dialog != null) {
            dialog.getContentPane().removeAll();
            dialog.getContentPane().add(modifyContent(content));
            dialog.pack();
        }
    }

    public Component getContent() {
        return content;
    }
    
    protected int maxHeight = -1;
    protected int maxWidth = -1;

    /**
     * Sets the maximum pixel size of the window.  If you don't care
     * about a particular dimension, set it to be less than zero and
     * the natural size of the content will be displayed.
     */
    public void setMaxSize(int width, int height) {
        maxHeight = height;
        maxWidth = width;
    }

    /**
     * Display the window, and find out what the natural or revised
     * size and location are for the window.
     */
    public void displayInWindow() {
        displayInWindow(null);
    }

    /**
     * Display the window, and find out what the natural or revised
     * size and location are for the window.
     * @param owner Frame for JDialog
     */
    public void displayInWindow(Frame owner) {

        int w = 0;
        int h = 0;

        Dimension dim = getComponentSize();
        if (dim != null) {
            content.setSize(dim);
        }

        // -1 is a flag for the positioning code to recenter the
        // -window on the owner if it's not null, for JDialogs.
        int x = -1; // these are now initialised at -1 instead of 10
        int y = -1; // these are now initialised at -1 instead of 10
            
        Point loc = getComponentLocation();
        if (loc != null) {
            x = (int) loc.getX();
            y = (int) loc.getY();
        }

        displayInWindow(owner, x, y, -1, -1);
    }

    /**
     * Display the window.
     * @param x the horizontal pixel location for the window.
     * @param y the vertical pixel location for the window.
     * @param width the horizontal size of the window, if less than or
     * equal to zero the content size will be used.
     * @param height the vertical size of the window, if less than or
     * equal to zero the content size will be used.
     */
    public void displayInWindow(int x, int y, int width, int height) {
        displayInWindow(null, x, y, width, height);
    }

    /**
     * Display the window.
     * @param owner Frame for JDialog
     * @param x the horizontal pixel location for the window.
     * @param y the vertical pixel location for the window.
     * @param width the horizontal size of the window, if less than or
     * equal to zero the content size will be used.
     * @param height the vertical size of the window, if less than or
     * equal to zero the content size will be used.
     */
    public void displayInWindow(Frame owner, int x, int y, 
                                int width, int height) {

        if (content == null) {
            Debug.message("windowsupport", "WindowSupport asked to display window with null content");
            return;
        }

        if (iFrame == null && dialog == null) {
        
            // Try to group the applet-specific stuff in here...
            if (Environment.getBoolean(Environment.UseInternalFrames)) {

                iFrame = new JInternalFrame(
                    title,
                    /*resizable*/ true,
                    /*closable*/ true,
                    /*maximizable*/ true,
                    /*iconifiable*/ true);
                iFrame.setOpaque(true);
                iFrame.addComponentListener(this);
                
                JLayeredPane desktop = 
                    Environment.getInternalFrameDesktop();

                Debug.message("windows", "WindowSupport creating internal frame");

                if (desktop != null) {
                    desktop.remove(iFrame);
                    desktop.add(iFrame, JLayeredPane.PALETTE_LAYER);
                } else {
                    Debug.output("WindowSupport:  No desktop set for internal frame");
                }
                
            } else { // Working as an application...
                dialog = new JDialog(owner, title);
                dialog.addComponentListener(this);
                Debug.message("windows", "WindowSupport creating frame");
            }
        }

        setContent(content);

        if (content instanceof ComponentListener) {
            addComponentListener((ComponentListener)content);
        }

        if (iFrame != null) {
            iFrame.pack();
            checkBounds(iFrame, x, y, width, height);
            iFrame.show();
            iFrame.toFront();
        } else if (dialog != null) {
            dialog.pack();
            checkBounds(dialog, x, y, width, height);
            if (owner != null && x < 0 && y < 0) {
                dialog.setLocationRelativeTo(owner);
            } else if (owner == null) {
                setPosition(dialog);
            }
            dialog.show();
        }
    }

    /**
     * Checks the component's dimensions against the requested values
     * and against any maximum limits that may have been set in the
     * WindowSupport.  Calls setBounds() on the Component.
     */
    protected void checkBounds(Component comp, int x, int y, 
                               int width, int height) {
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
     * For applications, checks where the Environment says the window
     * should be placed, and then uses the packed height and width to
     * make adjustments.
     */
    protected void setPosition(Component comp) {
        // get starting width and height
        int w = comp.getWidth();
        int h = comp.getHeight();

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Debug.message("basic","Screen dimensions are " + d);
        int x = d.width/2 - w/2;
        int y = d.height/2 - h/2;
        
        if (Debug.debugging("basic")) {
            Debug.output("Setting PLG frame X and Y from properties to " + x + " " + y);
        }

        // compose the frame, but don't show it here
        comp.setBounds(x,y,w,h);
    }

    /**
     * Set the window to be hidden and fire a ComponentEvent for
     * COMPONENT_HIDDEN.  Normally, just setting the visibility of the
     * window would be enough, but we're running into that problem we
     * had with the layers not firing ComponentEvents when hidden.
     * This method calls componentHidden, which in turn calls cleanUp.
     */
    public void killWindow() {

        ComponentEvent ce = null;
        JDialog dialogLocal = dialog;
        JInternalFrame iFrameLocal = iFrame;

        if (dialogLocal != null) {
            dialogLocal.setVisible(false);
            ce = new ComponentEvent(dialogLocal, ComponentEvent.COMPONENT_HIDDEN);
        } else if (iFrameLocal != null) {
            iFrameLocal.setVisible(false);
            ce = new ComponentEvent(iFrameLocal, ComponentEvent.COMPONENT_HIDDEN);
        }

        if (ce != null) {
            componentHidden(ce);
        }
    }

    /**
     * Get rid of the window used to display the content.
     */
    protected void cleanUp() {
        if (dialog != null) {
            dialog.removeComponentListener(this);
            dialog.dispose();
            dialog = null;
        } else if (iFrame != null) {
            iFrame.removeComponentListener(this);
            iFrame.dispose();
            iFrame = null;
        }

        if (content instanceof ComponentListener) {
            removeComponentListener((ComponentListener)content);
        }
    }

    /**
     * Add a component listener that is interested in hearing about
     * what happens to the window.
     */
    public void addComponentListener(ComponentListener l) {
        addListener(l);
    }

    /**
     * Remove a component listener that was interested in hearing about
     * what happens to the window.
     */
    public void removeComponentListener(ComponentListener l) {
        removeListener(l);
    }

    /**
     * Return the window displaying the content.  May be null.
     */
    public Container getWindow() {
        if (dialog != null) {
            return dialog;
        } 
        return iFrame;
    }

}
