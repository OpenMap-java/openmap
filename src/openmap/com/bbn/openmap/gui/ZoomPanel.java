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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ZoomPanel.java,v $
// $RCSfile: ZoomPanel.java,v $
// $Revision: 1.4 $
// $Date: 2003/10/24 23:36:28 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import com.bbn.openmap.util.Debug;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import java.io.Serializable;
import java.net.URL;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;

/**
 * Bean to zoom the Map.
 * <p>
 * This bean is a source for ZoomEvents.  It is a simple widget with a ZoomIn
 * button and a ZoomOut button.  When a button is pressed, the appropriate
 * zoom event is fired to all registered listeners.
 * @see #addZoomListener
 */
public class ZoomPanel extends OMToolComponent
    implements ActionListener, Serializable {

    public final static transient String zoomInCmd = "zoomin";
    public final static transient String zoomOutCmd = "zoomout";

    protected transient JButton zoomInButton, zoomOutButton;
    protected transient ZoomSupport zoomDelegate;

    public final static String defaultKey = "zoompanel";

    /**
     * Default Zoom In Factor is 0.5.
     */
    protected transient float zoomInFactor = 0.5f;

    /**
     * Default Zoom Out Factor is 2.0.
     */
    protected transient float zoomOutFactor = 2.0f;

    /**
     * Construct the ZoomPanel.
     */
    public ZoomPanel() {
	super();
	setKey(defaultKey);
//  	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
// 	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	this.setOpaque(false);

	JPanel panel = new JPanel();
	GridBagLayout internalGridbag = new GridBagLayout();
	GridBagConstraints c2 = new GridBagConstraints();
	panel.setLayout(internalGridbag);

	zoomDelegate = new ZoomSupport(this);
	zoomInButton = getButton("zoomIn", "Zoom In", zoomInCmd);
	c2.gridx = 0;
	c2.gridy = 0;
	internalGridbag.setConstraints(zoomInButton, c2);
	panel.add(zoomInButton);

	zoomOutButton = getButton("zoomOut", "Zoom Out", zoomOutCmd);
	c2.gridy = 1;
	internalGridbag.setConstraints(zoomOutButton, c2);
	panel.add(zoomOutButton);

	add(panel);
    }

    /**
     * Get the Zoom In Factor.
     * @return float the degree by which map scale will be multiplied when 
     * zoom in button is pressed
     */
    public float getZoomInFactor() {
	return zoomInFactor;
    }

    /**
     * Sets the Zoom In factor.
     * The factor must be &lt; 1.0.  (otherwise it would make ZoomIn into
     * a ZoomOut).
     * @param factor the degree by which map scale should be multiplied
     */
    public void setZoomInFactor(float factor) {
	if (factor < 1.0f){
	    zoomInFactor=factor;
	    zoomInButton.setToolTipText("zoom in X" +zoomInFactor);
	}
	else {
	    throw new IllegalArgumentException(
		    "Zoom In factor too large (must be < 1.0)");
	}
    }

    /**
     * Get the Zoom Out Factor.
     * @return float the degree by which map scale will be multiplied when 
     * zoom out button is pressed
     */
    public float getZoomOutFactor() {
	return zoomOutFactor;
    }

    /**
     * Sets the Zoom Out Factor.
     * The factor must be &gt; 1.0 (otherwise it would turn ZoomOut into
     * ZoomIn).
     * @param factor the degree by which map scale should be multiplied.
     */
    public void setZoomOutFactor(float factor) {
	if (factor > 1.0f) {
	    zoomOutFactor=factor;
	    zoomOutButton.setToolTipText("zoom out X" +zoomOutFactor);
	}
	else {
	    throw new IllegalArgumentException(
		    "Zoom In factor too small (must be > 1.0)");
	}
    }

    /**
     * Add the named button to the panel.
     *
     * @param name GIF image name
     * @param info ToolTip text
     * @param command String command name
     *
     */
    protected JButton getButton(String name, String info, String command) {
	URL url = ZoomPanel.class.getResource(name + ".gif");
	ImageIcon icon = new ImageIcon(url, info);
	JButton b = new JButton(icon);
	b.setPreferredSize(new Dimension(icon.getIconWidth(), 
					 icon.getIconHeight()));
	b.setToolTipText(info);
	b.setMargin(new Insets(0,0,0,0));
        b.setActionCommand(command);
	b.addActionListener(this);
	b.setBorderPainted(Debug.debugging("layout"));
	b.setOpaque(false);
	return b;
    }

    /**
     * Add a ZoomListener from the listener list.
     *
     * @param listener  The ZoomListener to be added
     */
    public synchronized void addZoomListener(ZoomListener listener) {
	zoomDelegate.addZoomListener(listener);
    }

    /**
     * Remove a ZoomListener from the listener list.
     *
     * @param listener  The ZoomListener to be removed
     */
    public synchronized void removeZoomListener(ZoomListener listener) {
	zoomDelegate.removeZoomListener(listener);
    }

    /**
     * ActionListener interface.
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
	String command = e.getActionCommand();

	if (command.equals(zoomInCmd)) {
	    zoomDelegate.fireZoom(ZoomEvent.RELATIVE, zoomInFactor);
	}
	else if (command.equals(zoomOutCmd)) {
	    zoomDelegate.fireZoom(ZoomEvent.RELATIVE, zoomOutFactor);
	}
    }

    public void setOpaque(boolean set) {
	super.setOpaque(set);
	if (zoomInButton != null) {
	    zoomInButton.setOpaque(set);
	}
	if (zoomOutButton != null) {
	    zoomOutButton.setOpaque(set);
	}
    }

    ///////////////////////////////////////////////////////////////////////////
    ////   OMComponentPanel methods to make the tool work with 
    ////   the MapHandler to find objects it needs.
    ///////////////////////////////////////////////////////////////////////////

    public void findAndInit(Object obj) {
	if (obj instanceof ZoomListener) {
	    addZoomListener((ZoomListener)obj);
	}
    }

    public void findAndUndo(Object obj) {
	if (obj instanceof ZoomListener) {
	    removeZoomListener((ZoomListener)obj);
	}
    }
}
