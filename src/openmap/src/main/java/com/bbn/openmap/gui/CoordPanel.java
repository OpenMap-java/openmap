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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/CoordPanel.java,v $
// $RCSfile: CoordPanel.java,v $
// $Revision: 1.8 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * CoordPanel is a simple gui with entry boxes and labels for latitude
 * and longitude. It sets the center of a map with the entered
 * coordinates by firing CenterEvents.
 */
public class CoordPanel extends JPanel implements Serializable {

    protected transient JTextField latitude, longitude;
    protected transient CenterSupport centerDelegate;

    protected I18n i18n = Environment.getI18n();

    protected DecimalFormat df = new DecimalFormat("0.###");

    /**
     * Creates the panel.
     */
    public CoordPanel() {
        centerDelegate = new CenterSupport(this);
        makeWidgets();
    }

    /**
     * Creates the panel.
     */
    public CoordPanel(CenterSupport support) {
        centerDelegate = support;
        makeWidgets();
    }

    /**
     * Creates and adds the labels and entry fields for latitude and
     * longitude
     */
    protected void makeWidgets() {
        String locText;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        locText = i18n.get(CoordPanel.class, "border", "Decimal Degrees");
        setBorder(new TitledBorder(new EtchedBorder(), locText));

        Insets leftInsets = new Insets(0, 10, 0, 10);
        Insets rightInsets = new Insets(0, 0, 0, 10);
        
        locText = i18n.get(CoordPanel.class, "latlabel", "Latitude: ");
        JLabel latlabel = new JLabel(locText);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0f;
        c.insets = leftInsets;
        c.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(latlabel, c);
        add(latlabel);

        latitude = new JTextField(10);
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0f;
        c.insets = rightInsets;
        gridbag.setConstraints(latitude, c);
        add(latitude);

        locText = i18n.get(CoordPanel.class, "lonlabel", "Longitude: ");
        JLabel lonlabel = new JLabel(locText);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0f;
        c.insets = leftInsets;
        gridbag.setConstraints(lonlabel, c);
        add(lonlabel);

        longitude = new JTextField(10);
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0f;
        c.insets = rightInsets;
        gridbag.setConstraints(longitude, c);
        add(longitude);
    }

    /**
     * @return the LatLonPoint represented by contents of the entry
     *         boxes
     */
    public LatLonPoint getLatLon() {
        double lat, lon;
        try {
            lat = Double.valueOf(latitude.getText()).doubleValue();
            lon = Double.valueOf(longitude.getText()).doubleValue();
        } catch (NumberFormatException except) {
            Debug.error("CoordPanel.getLatLon(): " + except.toString());
            clearTextBoxes();
            return null;
        }

        if (Debug.debugging("coordpanel")) {
            Debug.output("CoordPanel.getLatLon(): lat= " + lat + ", lon= "
                    + lon);
        }

        return (new LatLonPoint.Double(lat, lon));
    }

    /**
     * Sets the contents of the latitude and longitude entry boxes.
     * 
     * @param llpoint the object contains the coordinates that should
     *        go in the boxes. If null, text boxes will be cleared.
     */
    public void setLatLon(LatLonPoint llpoint) {
        if (llpoint == null) {
            clearTextBoxes();
        } else {
            latitude.setText("" + llpoint.getY());
            longitude.setText("" + llpoint.getX());
        }
    }

    /**
     * Sets the center of the map to be the coordinates in the
     * latitude and longitude entry boxes
     */
    public boolean setCenter() {

        LatLonPoint llp = getLatLon();
        if (llp == null) {
            return false;// invalid number
        }

        if (Debug.debugging("coordpanel")) {
            Debug.output("CoordPanel.setCenter(): " + llp);
        }

        centerDelegate.fireCenter(llp.getY(), llp.getX());
        return true;
    }

    protected void clearTextBoxes() {
        latitude.setText("");
        longitude.setText("");
    }

    /**
     * Add a CenterListener to the listener list.
     * 
     * @param listener The CenterListener to be added
     */
    public void addCenterListener(CenterListener listener) {
        centerDelegate.add(listener);
    }

    /**
     * Remove a CenterListener from the listener list.
     * 
     * @param listener The CenterListener to be removed
     */
    public void removeCenterListener(CenterListener listener) {
        centerDelegate.remove(listener);
    }
}