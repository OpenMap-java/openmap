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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/CoordInternalFrame.java,v $
// $RCSfile: CoordInternalFrame.java,v $
// $Revision: 1.2 $
// $Date: 2003/04/05 05:39:01 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.Serializable;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.accessibility.*;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.Assert;
import com.bbn.openmap.*;
import com.bbn.openmap.event.CenterListener;

/**
 * An Internal Frame wrapper for a CoordPanel
 */
public class CoordInternalFrame extends JInternalFrame
    implements Serializable, ActionListener {

    protected transient JButton closebutton;
    protected transient JButton applybutton;
    protected transient JTabbedPane tabPane;
    protected transient DMSCoordPanel dmsPanel;
    protected transient CoordPanel coordPanel;

    /** 
     * Creates the internal frame with a CoordPanel and Apply and 
     * Close buttons.<br> 
     * You MUST call addCenterListener() for the Apply button to do anything.
     * @param desltp the JLayeredPane on which the internal frame is
     * to be placed.
     */
    public CoordInternalFrame() {
	super("Go To Coordinates",
	      true,		//resizable
	      false,		//closable  - weird bug, won't close the second time
	      false,		//maximizable
	      true		//iconifiable
	      );
	setup();
    }

    /**
     * Creates a CoordPanel (which has latitude and longitude entry boxes)
     * and Apply and Close buttons
     */
    protected void setup() {

	Container contentPane = getContentPane();
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

	JPanel bigPanel = new JPanel();
	bigPanel.setLayout(new BoxLayout(bigPanel, BoxLayout.Y_AXIS));
	bigPanel.setAlignmentX(LEFT_ALIGNMENT);
 	bigPanel.setAlignmentY(BOTTOM_ALIGNMENT);

	coordPanel = new CoordPanel();
	dmsPanel = new DMSCoordPanel();
	tabPane = new JTabbedPane();
	tabPane.addTab("Dec Deg", coordPanel);
	tabPane.addTab("DMS", dmsPanel);
	bigPanel.add(tabPane);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	closebutton = new JButton("Close");
	closebutton.addActionListener(this);
	applybutton = new JButton("Apply");
	applybutton.addActionListener(this);
	buttonPanel.add(applybutton);
	buttonPanel.add(closebutton);

	bigPanel.add(buttonPanel);
	contentPane.add(bigPanel);

 	setOpaque(true);
 	pack();
     }

    /**
     * @return the LatLonPoint represented by contents of the 
     * entry boxes in the CoordPanel
     */
    public LatLonPoint getLatLon() {

	return coordPanel.getLatLon();
    }

    /**
     * Sets the contents of the latitude and longitude entry 
     * boxes in CoordPanel
     * @param llpoint the object containt the coordinates that should
     * go in the boxes
     */
    public void setLatLon(LatLonPoint llpoint) {
        if (tabPane.getSelectedComponent() == coordPanel)
	    coordPanel.setLatLon(llpoint);
	else
	    dmsPanel.setLatLon(llpoint);
    }

    /**
     * Tells the CoordPanel to set the center of the map
     */
    public boolean setCenter() {
        if (tabPane.getSelectedComponent() == coordPanel)
	    return coordPanel.setCenter();
	return dmsPanel.setCenter();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {

	if (e.getSource() == applybutton) {
	    if (tabPane.getSelectedComponent() == coordPanel)
	        coordPanel.setCenter();
	    else
	        dmsPanel.setCenter();
	}

	else if (e.getSource() == closebutton) {
	    try { 
		Component obj = getParent();
		while (!(obj instanceof JLayeredPane)) {
		    obj = obj.getParent();
		}
		((JLayeredPane)obj).remove(this);
		setClosed(true); 
		obj.repaint();
	    }
	    catch (java.beans.PropertyVetoException evt) {
		Assert.assertExp(false,
			      "CoordInternalFrame.actionPerformed("
			      + "close): internal error!");
	    
	    }
	}
    }

    /**
     * Add a CenterListener to the listener list.
     *
     * @param listener  The CenterListener to be added
     */
    public void addCenterListener(CenterListener listener) {
        coordPanel.addCenterListener(listener);
	dmsPanel.addCenterListener(listener);
    }

    /**
     * Remove a CenterListener from the listener list.
     *
     * @param listener  The CenterListener to be removed
     */
    public void removeCenterListener(CenterListener listener) {
	coordPanel.removeCenterListener(listener);
	dmsPanel.removeCenterListener(listener);
    }
}
