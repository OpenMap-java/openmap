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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/CoordPanel.java,v $
// $RCSfile: CoordPanel.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:07 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serializable;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.*;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.util.Debug;

/**
 *  CoordPanel is a simple gui with entry boxes and labels 
 *  for latitude and longitude. It sets the center
 *  of a map with the entered coordinates by firing CenterEvents.
 */
public class CoordPanel extends JPanel implements Serializable {

    protected transient JTextField latitude, longitude;
    protected transient CenterSupport centerDelegate;

    protected DecimalFormat df = new DecimalFormat("0.###");

    /**
     *  Creates the panel.
     */
    public CoordPanel() {
        centerDelegate = new CenterSupport(this);
        makeWidgets();
    }

    /**
     *  Creates the panel.
     */
    public CoordPanel(CenterSupport support) {
        centerDelegate = support;
        makeWidgets();
    }

    /**
     *  Creates and adds the labels and entry fields for latitude and longitude
     */
    protected void makeWidgets() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        setBorder(new TitledBorder(new EtchedBorder(), "Decimal Degrees"));

        JLabel latlabel = new JLabel("Latitude: ");
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(latlabel, c);
        add(latlabel);

        latitude = new JTextField(10);
        c.gridx = 1;
        c.gridy = 0;
        gridbag.setConstraints(latitude, c);
        add(latitude);

        JLabel lonlabel = new JLabel("Longitude: ");
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(lonlabel, c);
        add(lonlabel);

        longitude = new JTextField(10);
        c.gridx = 1;
        c.gridy = 1;
        gridbag.setConstraints(longitude, c);
        add(longitude);
    }

    /**
     *  @return the LatLonPoint represented by contents of the entry boxes
     */
    public LatLonPoint getLatLon() {
        float lat, lon;
        try {
            lat = Float.valueOf(latitude.getText()).floatValue();
            lon = Float.valueOf(longitude.getText()).floatValue();
        } catch (NumberFormatException except) {
            Debug.error("CoordPanel.getLatLon(): " + except.toString());
            clearTextBoxes();
            return null;
        }

        if (Debug.debugging("coordpanel")) {
            Debug.output("CoordPanel.getLatLon(): lat= " + lat + ", lon= " + lon);
        }

        return (new LatLonPoint(lat,lon));
    }

    /**
     *  Sets the contents of the latitude and longitude entry boxes.
     *  @param llpoint the object containt the coordinates that should
     *  go in the boxes.  If null, text boxes will be cleared.
     */
    public void setLatLon(LatLonPoint llpoint) {
        if (llpoint == null) {
            clearTextBoxes();
        } else {
            latitude.setText(""+llpoint.getLatitude());
            longitude.setText(""+llpoint.getLongitude());
        }
    }

    /**
     *  Sets the center of the map to be the coordinates in the 
     *  latitude and logitude entry boxes
     */
    public boolean setCenter() {
        
        LatLonPoint llp = getLatLon();
        if (llp == null) {
            return false;// invalid number
        }

        if (Debug.debugging("coordpanel")) {
            Debug.output("CoordPanel.setCenter(): "+ llp);
        }

        centerDelegate.fireCenter(llp.getLatitude(), llp.getLongitude());
        return true;
    }

    protected void clearTextBoxes() {
        latitude.setText("");
        longitude.setText("");
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
}
