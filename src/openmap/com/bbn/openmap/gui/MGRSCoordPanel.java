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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MGRSCoordPanel.java,v $
// $RCSfile: MGRSCoordPanel.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.border.*;
import javax.accessibility.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.util.Debug;


/**
 * MGRSCoordPanel is a simple gui with an entry box for a MGRS
 * coordinate. It sets the center of a map by firing CenterEvents.
 */
public class MGRSCoordPanel extends CoordPanel implements Serializable {

    protected transient JTextField mgrs;

    /**
     *  Creates the panel.
     */
    public MGRSCoordPanel() {
	super();
    }

    /**
     *  Creates the panel.
     */
    public MGRSCoordPanel(CenterSupport support) {
	super(support);
    }

    /**
     *  Creates and adds the labels and entry fields for latitude and longitude
     */
    protected void makeWidgets() {
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	setLayout(gridbag);
	setBorder(new TitledBorder(new EtchedBorder(), "MGRS Coordinate"));

	JLabel mgrsLabel = new JLabel("MGRS: ");
	c.gridx = 0;
	gridbag.setConstraints(mgrsLabel, c);
	add(mgrsLabel);

	mgrs = new JTextField(20);
	c.gridx = 1;
	gridbag.setConstraints(mgrs, c);
	add(mgrs);
    }

    /**
     *  @return the LatLonPoint represented by contents of the entry boxes
     */
    public LatLonPoint getLatLon() {

	String mgrsString;

	try {
	    // Allow blank minutes and seconds fields to represent zero
	    
	    
	    return new MGRSPoint(mgrs.getText()).toLatLonPoint();

	} catch (NumberFormatException except) {
//  	    System.out.println(except.toString());
	    clearTextBoxes();
	}
	return null;
    }

    /**
     *  Sets the contents of the latitude and longitude entry boxes
     *  @param llpoint the object containing the coordinates that
     *  should go in the boxes.
     */
     public void setLatLon(LatLonPoint llpoint) {
	 if (llpoint == null) {
	     clearTextBoxes();
	     return;
	 }

	 MGRSPoint mgrsp = new MGRSPoint(llpoint);
	 mgrs.setText(mgrsp.getMGRS());
     }

    protected void clearTextBoxes() {
	mgrs.setText("");
    }
}
