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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/UTMCoordPanel.java,v $
// $RCSfile: UTMCoordPanel.java,v $
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
 *  UTMCoordPanel is a simple gui with entry boxes and labels for Zone
 *  number and letters, and easting and northing representation of
 *  latitude and longitude. It sets the center of a map with the
 *  entered coordinates by firing CenterEvents.
 */
public class UTMCoordPanel extends CoordPanel implements Serializable {

    protected transient JTextField zoneLetter, zoneNumber, easting, northing;

    /**
     *  Creates the panel.
     */
    public UTMCoordPanel() {
	super();
    }

    /**
     *  Creates the panel.
     */
    public UTMCoordPanel(CenterSupport support) {
	super(support);
    }

    /**
     *  Creates and adds the labels and entry fields for latitude and longitude
     */
    protected void makeWidgets() {
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	setLayout(gridbag);
	setBorder(new TitledBorder(new EtchedBorder(), "Zone Number|Zone Letter|Easting|Northing"));

	JLabel utmLabel = new JLabel("UTM: ");
	c.gridx = 0;
	gridbag.setConstraints(utmLabel, c);
	add(utmLabel);

	c.gridx = GridBagConstraints.RELATIVE;
	zoneNumber = new JTextField(3);
	gridbag.setConstraints(zoneNumber, c);
	add(zoneNumber);

	zoneLetter = new JTextField(2);
	gridbag.setConstraints(zoneLetter, c);
	add(zoneLetter);

	easting = new JTextField(8);
	gridbag.setConstraints(easting, c);
	add(easting);

	northing = new JTextField(8);
	gridbag.setConstraints(northing, c);
	add(northing);
    }

    /**
     *  @return the LatLonPoint represented by contents of the entry boxes
     */
    public LatLonPoint getLatLon() {
	float fnorthing, feasting;
	int iZoneNumber;
	char cZoneLetter;

	try {
	    // Allow blank minutes and seconds fields to represent zero
	    iZoneNumber = Float.valueOf(zoneNumber.getText()).intValue();
	    cZoneLetter = zoneLetter.getText().charAt(0);

	    float minEasting = easting.getText().equals("") ? 0f :
	        Float.valueOf(easting.getText()).floatValue();
	    easting.setText(Float.toString(Math.abs(minEasting)));

	    float minNorthing = northing.getText().equals("") ? 0 :
	        Float.valueOf(northing.getText()).floatValue();
	    northing.setText(Float.toString(Math.abs(minNorthing)));

	    UTMPoint utm = new UTMPoint(minNorthing, minEasting, iZoneNumber, cZoneLetter);
	    return utm.toLatLonPoint();

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

	 UTMPoint utm = new UTMPoint(llpoint);
	 northing.setText(Float.toString(utm.northing));
	 easting.setText(Float.toString(utm.easting));
	 zoneNumber.setText(Integer.toString(utm.zone_number));
	 zoneLetter.setText((char)utm.zone_letter + "");
     }

    protected void clearTextBoxes() {
	northing.setText("");
	easting.setText("");
	zoneLetter.setText("");
	zoneNumber.setText("");
    }
}
