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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/CoordDialog.java,v $
// $RCSfile: CoordDialog.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import com.bbn.openmap.*;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.util.Debug;

import javax.swing.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import java.io.Serializable;

/**
 * A Dialog box wrapper for a CoordPanel
 */
public class CoordDialog extends JDialog 
    implements Serializable, ActionListener, CenterListener {

    protected transient JButton closebutton;
    protected transient JButton applybutton;
    protected transient JTabbedPane tabPane;
    protected transient CoordPanel coordPanel;
    protected transient DMSCoordPanel dmsPanel;
    protected transient UTMCoordPanel utmPanel;
    protected transient MGRSCoordPanel mgrsPanel;

    protected transient CenterSupport centerDelegate;

    /** 
     *  Creates a Dialog Box with a CoordPanel and Apply and Close buttons
     */
    public CoordDialog() {
	super();
	setup();
    }

    /**
     *  Creates a CoordPanel (which has latitude and longitude entry boxes)
     *  and Apply and Close buttons
     */
    protected void setup() {
	centerDelegate = new CenterSupport(this);
	// We want to set all of the current tabs with the current center.
	addCenterListener(this);
	Container contentPane = getContentPane();
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

	setTitle("Go To Coordinates");
	
	JPanel bigPanel = new JPanel();
	JPanel titlePanel = new JPanel();
	titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
	JLabel clarification = new JLabel("Set Center of Map to Coordinates:");
	titlePanel.add(clarification);
	bigPanel.add(titlePanel);

	bigPanel.setLayout(new BoxLayout(bigPanel, BoxLayout.Y_AXIS));
	coordPanel = new CoordPanel(centerDelegate);
	dmsPanel = new DMSCoordPanel(centerDelegate);
	utmPanel = new UTMCoordPanel(centerDelegate);
	mgrsPanel = new MGRSCoordPanel(centerDelegate);

	tabPane = new JTabbedPane();
	tabPane.addTab("Dec Deg", coordPanel);
	tabPane.addTab("DMS", dmsPanel);
	tabPane.addTab("UTM", utmPanel);
	tabPane.addTab("MGRS", mgrsPanel);

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

// 	setSize(200, 150);
//   	setSize(300, 210);

    }

    /**
     *  @return the LatLonPoint represented by contents of the 
     *  entry boxes in the CoordPanel
     */
    public LatLonPoint getLatLon() {
	return coordPanel.getLatLon();
    }

    /**
     *  Sets the contents of the latitude and longitude entry 
     *  boxes in CoordPanel
     *  @param llpoint the object containt the coordinates that should go in the boxes
     */
    public void setLatLon(LatLonPoint llpoint) {
	coordPanel.setLatLon(llpoint);
	dmsPanel.setLatLon(llpoint);
	utmPanel.setLatLon(llpoint);
	mgrsPanel.setLatLon(llpoint);
    }

    /**
     *  Tells the CoordPanel to set the center of the map
     */
    public boolean setCenter() {
	return ((CoordPanel)tabPane.getSelectedComponent()).setCenter();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
	if (e.getSource() == applybutton) {
	    boolean allOK = setCenter();
	    if (!allOK) {
		setLatLon(null);
	    }

	} else if (e.getSource() == closebutton) {
	    setVisible(false);
	}
    }

    /**
     * Add a CenterListener to the listener list.
     *
     * @param listener  The CenterListener to be added
     */
    public void addCenterListener(CenterListener listener) {
	centerDelegate.addCenterListener(listener);
    }


    /**
     * Remove a CenterListener from the listener list.
     *
     * @param listener  The CenterListener to be removed
     */
    public void removeCenterListener(CenterListener listener) {
	centerDelegate.removeCenterListener(listener);
    }

//     /**
//      * Add a CenterListener to the listener list.
//      *
//      * @param listener  The CenterListener to be added
//      */
//     public synchronized void addCenterListener(CenterListener listener) {
// 	coordPanel.addCenterListener(listener);
// 	dmsPanel.addCenterListener(listener);
//     }

//     /**
//      * Remove a CenterListener from the listener list.
//      *
//      * @param listener  The CenterListener to be removed
//      */
//     public synchronized void removeCenterListener(CenterListener listener) {
// 	coordPanel.removeCenterListener(listener);
// 	dmsPanel.removeCenterListener(listener);
//     }

    public void center(CenterEvent centerEvent) {
	setLatLon(new LatLonPoint(centerEvent.getLatitude(), centerEvent.getLongitude()));
    }

    /*
    public static void main(String[] args) {
	JFrame frame = new JFrame("CoordDialog");
	frame.setSize(240,90);
	CoordDialog cb = new CoordDialog();
	cb.setVisible(true);
	frame.setVisible(true);
	
    }
    */
}
