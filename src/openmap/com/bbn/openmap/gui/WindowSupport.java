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
// $Revision: 1.5 $
// $Date: 2003/10/03 00:46:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.util.Debug;

/**
 * The WindowSupport class provides support for managing JFrames or
 * JInternalFrames for other components.  The frame is disposed of
 * when the window is closed, and recreated when displayInWindow is
 * called.  The WindowSupport remembers size and location changes for
 * the window when it is recreated.
 */
public class WindowSupport implements ComponentListener, ActionListener {

    protected Component content;
    protected String title;
    protected Point componentLocation;
    protected Dimension componentSize;

    public final static String DisplayWindowCmd = "displayWindowCmd";
    public final static String KillWindowCmd = "killWindowCmd";

    /** 
     * The frame used when the DrawingToolLauncher is used in an
     * applet. 
     */
    protected transient JInternalFrame iFrame;

    protected transient JDialog dialog;

    /**
     * Create the window support.
     * @param content the content to display in the window.
     * @param windowTitle the title of the window.
     */
    public WindowSupport(Component content, String windowTitle) {
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
    }

    /**
     * ComponentListener method, new location is noted.
     */
    public void componentMoved(ComponentEvent e) {
	setComponentLocation(((Component)e.getSource()).getLocation());
    }

    /**
     * ComponentListener method.
     */
    public void componentShown(ComponentEvent e) {}

    /**
     * ComponentListener method. WindowSupport kills the window when
     * it is hidden.
     */
    public void componentHidden(ComponentEvent e) {
	Component source = (Component)e.getSource();
	if (source == dialog || source == iFrame) {
	    killWindow();
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
// 	    w = (int)dim.getWidth();
// 	    h = (int)dim.getHeight();
	}

// 	if (w <= 0) w = content.getWidth();
// 	if (h <= 0) h = content.getHeight();

	int x = 10;
	int y = 10;
	    
	Point loc = getComponentLocation();
	if (loc != null) {
	    x = (int) loc.getX();
	    y = (int) loc.getY();
	}

// 	displayInWindow(x, y, w, h);
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
    public void displayInWindow(Frame owner, int x, int y, int width, int height) {

 	if (iFrame == null && dialog == null) {
	
	    // Try to group the applet-specific stuff in here...
	    if (Environment.getBoolean(Environment.UseInternalFrames)) {

		iFrame = new JInternalFrame(
		    title,
		    /*resizable*/ true,
		    /*closable*/ true,
		    /*maximizable*/ false,
		    /*iconifiable*/ true);
		iFrame.getContentPane().add(content);
		iFrame.setOpaque(true);
		iFrame.pack();
		iFrame.addComponentListener(this);
		if (content instanceof ComponentListener) {
		    iFrame.addComponentListener((ComponentListener)content);
		}
		
		JLayeredPane desktop = 
		    Environment.getInternalFrameDesktop();

		if (desktop != null) {
		    desktop.remove(iFrame);
		    desktop.add(iFrame, JLayeredPane.PALETTE_LAYER);
		    iFrame.show();
		}
		
	    } else { // Working as an application...

		dialog = new JDialog(owner, title);
		dialog.addComponentListener(this);
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(content);
		dialog.pack();

		if (content instanceof ComponentListener) {
		    dialog.addComponentListener((ComponentListener)content);
		}

	    }
	}

	if (iFrame != null) {
	    if (height <= 0 || width <= 0) {
		iFrame.setLocation(x, y);
	    } else {
		iFrame.setBounds(x, y, width, height);
	    }

	    iFrame.show();
	    iFrame.toFront();
	} else if (dialog != null) {
	    if (height <= 0 || width <= 0) {
		dialog.setLocation(x, y);
	    } else {
		dialog.setBounds(x, y, width, height);
	    } 
	    dialog.show();
	}
    }

    /**
     * Get rid of the window used to display the content.
     */
    public void killWindow() {

	if (dialog != null) {
	    dialog.removeComponentListener(this);
	    if (content instanceof ComponentListener) {
		dialog.removeComponentListener((ComponentListener)content);
	    }
	    dialog.dispose();
	} else if (iFrame != null) {
	    iFrame.removeComponentListener(this);
	    if (content instanceof ComponentListener) {
		iFrame.removeComponentListener((ComponentListener)content);
	    }
	    iFrame.dispose();
	    iFrame = null;
	}
    }
}
