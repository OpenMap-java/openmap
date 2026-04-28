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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/CombinedCoordPanel.java,v $
// $RCSfile: CombinedCoordPanel.java,v $
// $Revision: 1.8 $
// $Date: 2006/05/22 23:53:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * A JPanel containing a tabbed set of CoordPanels. Hitting the apply
 * button not only notifies any CenterListeners of the coordinates
 * stored in the active CoordPanel, it also causes the other
 * CoordPanels to translate the active panel's values to their format.
 * If the CombinedCoordPanel is given an ActionListener for the close
 * button, a close button will be added to the panel. Otherwise, just
 * the 'Apply' button will be added to the panel.
 */
public class CombinedCoordPanel extends OMComponentPanel implements
        Serializable, ActionListener, CenterListener {

    protected transient JButton closebutton;
    protected transient JButton applybutton;
    protected transient JTabbedPane tabPane;
    protected transient CoordPanel coordPanel;
    protected transient DMSCoordPanel dmsPanel;
    protected transient UTMCoordPanel utmPanel;
    protected transient MGRSCoordPanel mgrsPanel;

    protected transient CenterSupport centerDelegate;

    public final static String CloseCmd = "Close";

    /**
     * Creates a CombinedCoordPanel with Apply button.
     */
    public CombinedCoordPanel() {
        this(null);
    }

    /**
     * Creates a CombinedCoordPanel with Apply button, and a Close
     * button if the closeButtonListener is not null.
     */
    public CombinedCoordPanel(ActionListener closeButtonListener) {
        setup(i18n.get(CombinedCoordPanel.class,
                "defaultComment",
                "Set Center of Map to Coordinates:"), closeButtonListener);
    }

    /**
     * Creates a CombinedCoordPanel with Apply and Close buttons with
     * a specified comment above the tabbed panel.
     */
    public CombinedCoordPanel(String comment, ActionListener closeButtonListener) {
        setup(comment, closeButtonListener);
    }

    /**
     * Create the panel and set up the listeners. Called from the
     * constructor.
     */
    protected void setup(String comment, ActionListener closeButtonListener) {
        String locText;

        centerDelegate = new CenterSupport(this);
        // We want to set all of the current tabs with the current
        // center.
        addCenterListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        JLabel clarification = new JLabel(comment);
        titlePanel.add(clarification);
        add(titlePanel);

        coordPanel = new CoordPanel(centerDelegate);
        dmsPanel = new DMSCoordPanel(centerDelegate);
        utmPanel = new UTMCoordPanel(centerDelegate);
        mgrsPanel = new MGRSCoordPanel(centerDelegate);

        tabPane = new JTabbedPane();

        locText = i18n.get(CombinedCoordPanel.class,
                "tabPane.decdeg",
                "Dec Deg");
        tabPane.addTab(locText, coordPanel);
        locText = i18n.get(CombinedCoordPanel.class, "tabPane.dms", "DMS");
        tabPane.addTab(locText, dmsPanel);
        locText = i18n.get(CombinedCoordPanel.class, "tabPane.utm", "UTM");
        tabPane.addTab(locText, utmPanel);
        locText = i18n.get(CombinedCoordPanel.class, "tabPane.mgrs", "MGRS");
        tabPane.addTab(locText, mgrsPanel);

        add(tabPane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        locText = i18n.get(CombinedCoordPanel.class, "applybutton", "Apply");
        applybutton = new JButton(locText);
        applybutton.addActionListener(this);
        buttonPanel.add(applybutton);

        if (closeButtonListener != null) {
            locText = i18n.get(CombinedCoordPanel.class, "closebutton", "Close");
            closebutton = new JButton(locText);
            closebutton.setActionCommand(CloseCmd);
            closebutton.addActionListener(closeButtonListener);
            buttonPanel.add(closebutton);
        }

        add(buttonPanel);
    }

    /**
     * @return the LatLonPoint represented by contents of the entry
     *         boxes in the CoordPanel
     */
    public LatLonPoint getLatLon() {
        return coordPanel.getLatLon();
    }

    /**
     * Sets the contents of the latitude and longitude entry boxes in
     * CoordPanel.
     * 
     * @param llpoint the object contains the coordinates that should
     *        go in the boxes.
     */
    public void setLatLon(LatLonPoint llpoint) {
        coordPanel.setLatLon(llpoint);
        dmsPanel.setLatLon(llpoint);
        utmPanel.setLatLon(llpoint);
        mgrsPanel.setLatLon(llpoint);
    }

    /**
     * Tells the active CoordPanel to set the center of the map,
     * firing a center event to all listeners.
     */
    public boolean setCenter() {
        return ((CoordPanel) tabPane.getSelectedComponent()).setCenter();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == applybutton) {
            boolean allOK = setCenter();
            if (!allOK) {
                setLatLon(null);
            }
        }
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

    public void center(CenterEvent centerEvent) {
        setLatLon(new LatLonPoint.Double(centerEvent.getLatitude(), centerEvent.getLongitude()));
    }

    /**
     * MapHandlerChild method. If the object is a MapBean, the
     * CombinedCoordPanel will add it to itself as a CenterListener.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof CenterListener) {
            addCenterListener((CenterListener) someObj);
        }
    }

    /**
     * MapHandlerChild method. If the object is a MapBean, the
     * CombinedCoordPanel will remove it from itself as a
     * CenterListener.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof CenterListener) {
            removeCenterListener((CenterListener) someObj);
        }
    }

}