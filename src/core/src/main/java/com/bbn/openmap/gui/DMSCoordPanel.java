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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/DMSCoordPanel.java,v $
// $RCSfile: DMSCoordPanel.java,v $
// $Revision: 1.9 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * DMSCoordPanel is a simple gui with entry boxes and labels for
 * Degree-Minute-Second representation of latitude and longitude. It
 * sets the center of a map with the entered coordinates by firing
 * CenterEvents.
 */
public class DMSCoordPanel extends CoordPanel implements Serializable {

    protected transient JTextField degLat, minLat, secLat, degLon, minLon,
            secLon;

    /**
     * Creates the panel.
     */
    public DMSCoordPanel() {
        super();
    }

    /**
     * Creates the panel.
     */
    public DMSCoordPanel(CenterSupport support) {
        super(support);
    }

    /**
     * Creates and adds the labels and entry fields for latitude and
     * longitude
     */
    protected void makeWidgets() {
        String locText;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        Insets leftInsets = new Insets(0, 10, 0, 10);
        Insets centerInsets = new Insets(0, 0, 0, 0);
        Insets rightInsets = new Insets(0, 0, 0, 10);
        
        setLayout(gridbag);
        locText = i18n.get(DMSCoordPanel.class,
                "border",
                "Degrees|Minutes|Seconds");
        setBorder(new TitledBorder(new EtchedBorder(), locText));

        locText = i18n.get(DMSCoordPanel.class, "latlabel", "Latitude DMS: ");
        JLabel latlabel = new JLabel(locText);
        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0f;
        c.insets = leftInsets;
        c.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(latlabel, c);
        add(latlabel);

        c.gridx = GridBagConstraints.RELATIVE;
        degLat = new JTextField(4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = .33f;
        c.insets = centerInsets;
        gridbag.setConstraints(degLat, c);
        add(degLat);

        minLat = new JTextField(4);
        gridbag.setConstraints(minLat, c);
        add(minLat);

        secLat = new JTextField(4);
        c.insets = rightInsets;
        gridbag.setConstraints(secLat, c);
        add(secLat);

        locText = i18n.get(DMSCoordPanel.class, "lonlabel", "Longitude DMS: ");
        JLabel lonlabel = new JLabel(locText);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0f;
        c.insets = leftInsets;
        c.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(lonlabel, c);
        add(lonlabel);

        c.gridx = GridBagConstraints.RELATIVE;
        degLon = new JTextField(4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = .33f;
        c.insets = centerInsets;
        gridbag.setConstraints(degLon, c);
        add(degLon);

        minLon = new JTextField(4);
        gridbag.setConstraints(minLon, c);
        add(minLon);

        secLon = new JTextField(4);
        c.insets = rightInsets;
        gridbag.setConstraints(secLon, c);
        add(secLon);
    }

    /**
     * @return the LatLonPoint represented by contents of the entry
     *         boxes
     */
    public LatLonPoint getLatLon() {
        int deglat, minlat, deglon, minlon;
        float seclat, seclon, lat, lon;
        try {
            // Allow blank minutes and seconds fields to represent
            // zero
            deglat = Float.valueOf(degLat.getText()).intValue();

            // We just reset the text in the fields to pretty things
            // up a little. Also lets the user know what we think we
            // read.

            minlat = minLat.getText().length() == 0 ? 0
                    : Float.valueOf(minLat.getText()).intValue();
            minLat.setText(Integer.toString(Math.abs(minlat)));
            seclat = secLat.getText().length() == 0 ? 0.0f
                    : Float.valueOf(secLat.getText()).floatValue();
            secLat.setText(Float.toString(Math.abs(seclat)));

            deglon = Float.valueOf(degLon.getText()).intValue();

            minlon = minLon.getText().length() == 0 ? 0
                    : Float.valueOf(minLon.getText()).intValue();
            minLon.setText(Integer.toString(Math.abs(minlon)));

            seclon = secLon.getText().length() == 0 ? 0.0f
                    : Float.valueOf(secLon.getText()).floatValue();
            secLon.setText(Float.toString(Math.abs(seclon)));

        } catch (NumberFormatException except) {
            // System.out.println(except.toString());
            clearTextBoxes();
            return null;
        }

        degLat.setText(Integer.toString(deglat));
        degLon.setText(Integer.toString(deglon));

        // I don't this gives the right behavior. The sign given to
        // the minutes and seconds should be ignored, giving way to
        // the sign of the degree value.
        // lat = (float)(deglat + ((minlat * 60.0) + seclat)/3600);
        // lon = (float)(deglon + ((minlon * 60.0) + seclon)/3600);

        // So, I'm going to keep track of the degree behavior, and the
        // minutes and seconds will just build on it. So for a
        // negative degree value, positive minutes make the overall
        // value more negative. Negative minutes will be abs'ed.

        float direction = 1f;
        if (deglat < 0f)
            direction = -1f;

        lat = (float) (deglat + (Math.abs(minlat * 60.0f) + Math.abs(seclat))
                / 3600f * direction);

        direction = 1f;
        if (deglon < 0f)
            direction = -1f;
        lon = (float) (deglon + (Math.abs(minlon * 60.0f) + Math.abs(seclon))
                / 3600f * direction);
        // System.out.println("lat: " +lat + " lon: "+lon);

        return (new LatLonPoint.Double(lat, lon));
    }

    /**
     * Sets the contents of the latitude and longitude entry boxes
     * 
     * @param llpoint the object containing the coordinates that
     *        should go in the boxes.
     */
    public void setLatLon(LatLonPoint llpoint) {

        if (llpoint == null) {
            clearTextBoxes();
            return;
        }

        double lat = llpoint.getY();
        double lon = llpoint.getX();

        double direction = 1;
        if (lat < 0)
            direction = -1;

        lat = Math.abs(lat);
        double fractLat = lat - Math.floor(lat);

        degLat.setText(Integer.toString((int) (Math.floor(lat) * direction)));
        minLat.setText(Integer.toString((int) Math.floor(fractLat * 60)));
        secLat.setText(df.format((float) Math.floor((fractLat * 60) % 1 * 60)));

        direction = 1;
        if (lon < 0)
            direction = -1;

        lon = Math.abs(lon);
        double fractLon = lon - Math.floor(lon);

        degLon.setText(Integer.toString((int) (Math.floor(lon) * direction)));
        minLon.setText(Integer.toString((int) Math.floor(fractLon * 60)));
        secLon.setText(df.format((float) Math.floor((fractLon * 60) % 1 * 60)));

        if (Debug.debugging("coordpanel")) {
            Debug.output("DMSCoordPanel.setLatLon(): setting " + llpoint
                    + " to " + degLat.getText() + ", " + minLat.getText()
                    + ", " + secLat.getText() + " and " + degLon.getText()
                    + ", " + minLon.getText() + ", " + secLon.getText()
                    + " : backcheck = " + getLatLon());
        }

    }

    protected void clearTextBoxes() {
        degLat.setText("");
        minLat.setText("");
        secLat.setText("");
        degLon.setText("");
        minLon.setText("");
        secLon.setText("");
    }

    public static void main(String[] argv) {
        Debug.init();
        Debug.put("coordpanel");
        DMSCoordPanel dms = new DMSCoordPanel();
        dms.setLatLon(new LatLonPoint.Double(40.8000000000f, -75.200000000000f));
        System.exit(0);
    }
}