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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/NavigatePanel.java,v $
// $RCSfile: NavigatePanel.java,v $
// $Revision: 1.11 $
// $Date: 2005/08/10 21:30:47 $
// $Author: dietrick $
// 
// **********************************************************************
package com.bbn.openmap.gui;

import com.bbn.openmap.Environment;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.event.PanListener;
import com.bbn.openmap.event.PanSupport;
import com.bbn.openmap.util.Debug;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A Navigation Rosette Bean. This bean is a source for PanEvents and
 * CenterEvents.
 */
public class NavigatePanel extends OMToolComponent implements Serializable,
        ActionListener {

    public final static String PAN_NW_CMD = "panNW";
    public final static String PAN_N_CMD = "panN";
    public final static String PAN_NE_CMD = "panNE";
    public final static String PAN_E_CMD = "panE";
    public final static String PAN_SE_CMD = "panSE";
    public final static String PAN_S_CMD = "panS";
    public final static String PAN_SW_CMD = "panSW";
    public final static String PAN_W_CMD = "panW";
    public final static String CENTER_CMD = "center";

    protected transient JButton nwButton = null;
    protected transient JButton nButton = null;
    protected transient JButton neButton = null;
    protected transient JButton eButton = null;
    protected transient JButton seButton = null;
    protected transient JButton sButton = null;
    protected transient JButton swButton = null;
    protected transient JButton wButton = null;
    protected transient JButton cButton = null;

    // default icon names
    protected static String nwName = "nw.gif";
    protected static String nName = "n.gif";
    protected static String neName = "ne.gif";
    protected static String eName = "e.gif";
    protected static String seName = "se.gif";
    protected static String sName = "s.gif";
    protected static String swName = "sw.gif";
    protected static String wName = "w.gif";
    protected static String cName = "center.gif";

    protected PanSupport panDelegate;
    protected CenterSupport centerDelegate;
    protected boolean useTips = false;
    protected float panFactor = 1f;

    // protected int height = 0; // calculated
    // protected int width = 0; // calculated
    protected boolean useDefaultCenter = false;
    protected float defaultCenterLat = 0;
    protected float defaultCenterLon = 0;

    public final static String DEFAULT_KEY = "navigatepanel";

    /**
     * Construct the NavigationPanel.
     */
    public NavigatePanel() {
        super();
        setKey(DEFAULT_KEY);
        panDelegate = new PanSupport(this);
        centerDelegate = new CenterSupport(this);

        JPanel panel = new JPanel();
        GridBagLayout internalGridbag = new GridBagLayout();
        GridBagConstraints c2 = new GridBagConstraints();
        panel.setLayout(internalGridbag);

        // begin top row
        String info = i18n.get(NavigatePanel.class,
                "panNW.tooltip",
                "Pan Northwest");
        nwButton = getButton(nwName, info, PAN_NW_CMD);
        c2.gridx = 0;
        c2.gridy = 0;
        internalGridbag.setConstraints(nwButton, c2);
        panel.add(nwButton);

        info = i18n.get(NavigatePanel.class, "panN.tooltip", "Pan North");
        nButton = getButton(nName, info, PAN_N_CMD);
        c2.gridx = 1;
        c2.gridy = 0;
        internalGridbag.setConstraints(nButton, c2);
        panel.add(nButton);

        info = i18n.get(NavigatePanel.class, "panNE.tooltip", "Pan Northeast");
        neButton = getButton(neName, info, PAN_NE_CMD);
        c2.gridx = 2;
        c2.gridy = 0;
        internalGridbag.setConstraints(neButton, c2);
        panel.add(neButton);

        // begin middle row
        info = i18n.get(NavigatePanel.class, "panW.tooltip", "Pan West");
        wButton = getButton(wName, info, PAN_W_CMD);
        c2.gridx = 0;
        c2.gridy = 1;
        internalGridbag.setConstraints(wButton, c2);
        panel.add(wButton);

        info = i18n.get(NavigatePanel.class,
                "center.tooltip",
                "Center Map at Starting Coords");
        cButton = getButton(cName, info, CENTER_CMD);
        c2.gridx = 1;
        c2.gridy = 1;
        internalGridbag.setConstraints(cButton, c2);
        panel.add(cButton);

        info = i18n.get(NavigatePanel.class, "panE.tooltip", "Pan East");
        eButton = getButton(eName, info, PAN_E_CMD);
        c2.gridx = 2;
        c2.gridy = 1;
        internalGridbag.setConstraints(eButton, c2);
        panel.add(eButton);

        // begin bottom row
        info = i18n.get(NavigatePanel.class, "panSW.tooltip", "Pan Southwest");
        swButton = getButton(swName, info, PAN_SW_CMD);
        c2.gridx = 0;
        c2.gridy = 2;
        internalGridbag.setConstraints(swButton, c2);
        panel.add(swButton);

        info = i18n.get(NavigatePanel.class, "panS.tooltip", "Pan South");
        sButton = getButton(sName, info, PAN_S_CMD);
        c2.gridx = 1;
        c2.gridy = 2;
        internalGridbag.setConstraints(sButton, c2);
        panel.add(sButton);

        info = i18n.get(NavigatePanel.class, "panSE.tooltip", "Pan Southeast");
        seButton = getButton(seName, info, PAN_SE_CMD);
        c2.gridx = 2;
        c2.gridy = 2;
        internalGridbag.setConstraints(seButton, c2);
        panel.add(seButton);

        add(panel);
    }

    /**
     * Add the named button to the panel.
     *
     * @param name GIF image name
     * @param info ToolTip text
     * @param command String command name
     * @return JButton
     */
    protected JButton getButton(String name, String info, String command) {
        URL url = NavigatePanel.class.getResource(name);
        ImageIcon icon = new ImageIcon(url, info);
        JButton b = new JButton(icon);
        b.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        b.setToolTipText(info);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setActionCommand(command);
        b.addActionListener(this);
        b.setBorderPainted(Debug.debugging("layout"));
        b.setOpaque(false);
        return b;
    }

    /**
     * Add a CenterListener.
     *
     * @param listener CenterListener
     */
    public synchronized void addCenterListener(CenterListener listener) {
        centerDelegate.add(listener);
    }

    /**
     * Remove a CenterListener
     *
     * @param listener CenterListener
     */
    public synchronized void removeCenterListener(CenterListener listener) {
        centerDelegate.remove(listener);
    }

    /**
     * Add a PanListener.
     *
     * @param listener PanListener
     */
    public synchronized void addPanListener(PanListener listener) {
        panDelegate.add(listener);
    }

    /**
     * Remove a PanListener
     *
     * @param listener PanListener
     */
    public synchronized void removePanListener(PanListener listener) {
        panDelegate.remove(listener);
    }

    /**
     * Fire a CenterEvent.
     */
    protected synchronized void fireCenterEvent(float lat, float lon) {
        centerDelegate.fireCenter(lat, lon);
    }

    /**
     * Fire a PanEvent.
     *
     * @param az azimuth east of north
     */
    protected synchronized void firePanEvent(float az) {
        panDelegate.firePan(az);
    }

    /**
     * Get the pan factor.
     * <p>
     * The panFactor is the amount of screen to shift when panning in a certain
     * direction: 0=none, 1=half-screen shift.
     *
     * @return float panFactor (0.0 &lt;= panFactor &lt;= 1.0)
     */
    public float getPanFactor() {
        return panFactor;
    }

    /**
     * Set the pan factor.
     * <p>
     * This defaults to 1.0. The panFactor is the amount of screen to shift when
     * panning in a certain direction: 0=none, 1=half-screen shift.
     *
     * @param panFactor (0.0 &lt;= panFactor &lt;= 1.0)
     */
    public void setPanFactor(float panFactor) {
        if ((panFactor < 0f) || (panFactor > 1f)) {
            throw new IllegalArgumentException("should be: (0.0 <= panFactor <= 1.0)");
        }
        this.panFactor = panFactor;
    }

    /**
     * Use this function to set where you want the map projection to pan to when
     * the user clicks on "center" button on the navigation panel. The scale
     * does not change. When you call this function, the projection does not
     * change.
     *
     * @param passedLat float the center latitude (in degrees)
     * @param passedLon float the center longitude (in degrees)
     */
    public void setDefaultCenter(float passedLat, float passedLon) {
        useDefaultCenter = true;
        defaultCenterLat = passedLat;
        defaultCenterLon = passedLon;
    }

    /**
     * ActionListener Interface.
     *
     * @param e ActionEvent
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {

        String command = e.getActionCommand();

        Debug.message("navpanel", "NavigatePanel.actionPerformed(): " + command);
        switch (command) {
            case PAN_NW_CMD:
                firePanEvent(-45f);
                break;
            case PAN_N_CMD:
                firePanEvent(0f);
                break;
            case PAN_NE_CMD:
                firePanEvent(45f);
                break;
            case PAN_E_CMD:
                firePanEvent(90f);
                break;
            case PAN_SE_CMD:
                firePanEvent(135f);
                break;
            case PAN_S_CMD:
                firePanEvent(180f);
                break;
            case PAN_SW_CMD:
                firePanEvent(-135f);
                break;
            case PAN_W_CMD:
                firePanEvent(-90f);
                break;
            case CENTER_CMD:
                // go back to the center point

                float lat;
                float lon;
                if (useDefaultCenter) {
                    lat = defaultCenterLat;
                    lon = defaultCenterLon;
                } else {
                    lat = Environment.getFloat(Environment.Latitude, 0f);
                    lon = Environment.getFloat(Environment.Longitude, 0f);
                }
                fireCenterEvent(lat, lon);
                break;
            default:
                break;
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // // OMComponentPanel methods to make the tool work with
    // // the MapHandler to find objects it needs.
    // /////////////////////////////////////////////////////////////////////////
    @Override
    public void findAndInit(Object obj) {
        if (obj instanceof PanListener) {
            addPanListener((PanListener) obj);
        }
        if (obj instanceof CenterListener) {
            addCenterListener((CenterListener) obj);
        }
    }

    @Override
    public void findAndUndo(Object obj) {
        if (obj instanceof PanListener) {
            removePanListener((PanListener) obj);
        }
        if (obj instanceof CenterListener) {
            removeCenterListener((CenterListener) obj);
        }
    }

}
