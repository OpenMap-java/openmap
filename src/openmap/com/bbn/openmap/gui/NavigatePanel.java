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
// $Revision: 1.3 $
// $Date: 2003/08/14 22:56:10 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.net.URL;
import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.util.Debug;


/**
 * A Navigation Rosette Bean.
 * This bean is a source for PanEvents and CenterEvents.
 */
public class NavigatePanel extends OMToolComponent
    implements Serializable, ActionListener {

    public final static String panNWCmd = "panNW";
    public final static String panNCmd = "panN";
    public final static String panNECmd = "panNE";
    public final static String panECmd = "panE";
    public final static String panSECmd = "panSE";
    public final static String panSCmd = "panS";
    public final static String panSWCmd = "panSW";
    public final static String panWCmd = "panW";
    public final static String centerCmd = "center";

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

    protected int height = 0; // calculated
    protected int width = 0; // calculated

    protected boolean useDefaultCenter = false;
    protected float defaultCenterLat = 0;
    protected float defaultCenterLon = 0;

    public final static String defaultKey = "navigatepanel";

    /**
     * Construct the NavigationPanel.
     */
    public NavigatePanel() {
	super();
	setKey(defaultKey);
	panDelegate = new PanSupport(this);
	centerDelegate = new CenterSupport(this);

// 	GridLayout layout = new GridLayout(3,3);
// 	layout.setHgap(20);
// 	layout.setVgap(20);
	setLayout(/*layout*/new GridLayout(3, 3));
// 	setAlignmentX(LEFT_ALIGNMENT);
// 	setAlignmentY(TOP_ALIGNMENT);

	int w, h;

	// begin top row
	ImageIcon nwIcon = addImageIcon(new ImageIcon(), nwName, "northwest");
	w = nwIcon.getIconWidth();
	h = nwIcon.getIconHeight();
	nwButton = new JButton(nwIcon);
	nwButton.setMargin(new Insets(0,0,0,0));
        nwButton.setActionCommand(panNWCmd);
	nwButton.addActionListener(this);
	nwButton.setMinimumSize(new Dimension(w+2,h+2));
	nwButton.setPreferredSize(new Dimension(w+2,h+2));
	nwButton.setBorderPainted(false);
	height += h+2;
	width += w+2;
	add(nwButton);

	ImageIcon nIcon = addImageIcon(new ImageIcon(), nName, "north");
	w = nIcon.getIconWidth();
	h = nIcon.getIconHeight();
	nButton = new JButton(nIcon);
	nButton.setMargin(new Insets(0,0,0,0));
        nButton.setActionCommand(panNCmd);
	nButton.addActionListener(this);
	nButton.setMinimumSize(new Dimension(w+2,h+2));
	nButton.setPreferredSize(new Dimension(w+2,h+2));
	nButton.setBorderPainted(false);
	width += w+2;
	add(nButton);

	ImageIcon neIcon = addImageIcon(new ImageIcon(), neName, "northeast");
	w = neIcon.getIconWidth();
	h = neIcon.getIconHeight();
	neButton = new JButton(neIcon);
	neButton.setMargin(new Insets(0,0,0,0));
        neButton.setActionCommand(panNECmd);
	neButton.addActionListener(this);
	neButton.setMinimumSize(new Dimension(w+2,h+2));
	neButton.setPreferredSize(new Dimension(w+2,h+2));
	neButton.setBorderPainted(false);
	width += w+2;
	add(neButton);

	// begin middle row
	ImageIcon wIcon = addImageIcon(new ImageIcon(), wName, "west");
	w = wIcon.getIconWidth();
	h = wIcon.getIconHeight();
	wButton = new JButton(wIcon);
	wButton.setMargin(new Insets(0,0,0,0));
        wButton.setActionCommand(panWCmd);
	wButton.addActionListener(this);
	wButton.setMinimumSize(new Dimension(w+2,h+2));
	wButton.setPreferredSize(new Dimension(w+2,h+2));
	wButton.setBorderPainted(false);
	height += h+2;
	add(wButton);

	ImageIcon cIcon = addImageIcon(new ImageIcon(), cName, "center");
	w = cIcon.getIconWidth();
	h = cIcon.getIconHeight();
	cButton = new JButton(cIcon);
	cButton.setMargin(new Insets(0,0,0,0));
        cButton.setActionCommand(centerCmd);
	cButton.addActionListener(this);
	cButton.setMinimumSize(new Dimension(w+2,h+2));
	cButton.setPreferredSize(new Dimension(w+2,h+2));
	cButton.setBorderPainted(false);
	add(cButton);

	ImageIcon eIcon = addImageIcon(new ImageIcon(), eName, "east");
	w = eIcon.getIconWidth();
	h = eIcon.getIconHeight();
	eButton = new JButton(eIcon);
	eButton.setMargin(new Insets(0,0,0,0));
        eButton.setActionCommand(panECmd);
	eButton.addActionListener(this);
	eButton.setMinimumSize(new Dimension(w+2,h+2));
	eButton.setPreferredSize(new Dimension(w+2,h+2));
	eButton.setBorderPainted(false);
	add(eButton);

	// begin bottom row
	ImageIcon swIcon = addImageIcon(new ImageIcon(), swName, "southwest");
	w = swIcon.getIconWidth();
	h = swIcon.getIconHeight();
	swButton = new JButton(swIcon);
	swButton.setMargin(new Insets(0,0,0,0));
        swButton.setActionCommand(panSWCmd);
	swButton.addActionListener(this);
	swButton.setMinimumSize(new Dimension(w+2,h+2));
	swButton.setPreferredSize(new Dimension(w+2,h+2));
	swButton.setBorderPainted(false);
	height += h+2;
	add(swButton);

	ImageIcon sIcon = addImageIcon(new ImageIcon(), sName, "south");
	w = sIcon.getIconWidth();
	h = sIcon.getIconHeight();
	sButton = new JButton(sIcon);
	sButton.setMargin(new Insets(0,0,0,0));
        sButton.setActionCommand(panSCmd);
	sButton.addActionListener(this);
	sButton.setMinimumSize(new Dimension(w+2,h+2));
	sButton.setPreferredSize(new Dimension(w+2,h+2));
	sButton.setBorderPainted(false);
	add(sButton);

	ImageIcon seIcon = addImageIcon(new ImageIcon(), seName, "southeast");
	w = seIcon.getIconWidth();
	h = seIcon.getIconHeight();
	seButton = new JButton(seIcon);
	seButton.setMargin(new Insets(0,0,0,0));
        seButton.setActionCommand(panSECmd);
	seButton.addActionListener(this);
	seButton.setMinimumSize(new Dimension(w+2,h+2));
	seButton.setPreferredSize(new Dimension(w+2,h+2));
	seButton.setBorderPainted(false);
	add(seButton);

	setMinimumSize(new Dimension(width, height));
	setPreferredSize(new Dimension(width, height));
    }

    /**
     * Create an ImageIcon.
     * @param imageIcon ImageIcon
     * @param imageName file name
     * @param imageTip text help
     * @return ImageIcon
     */
    protected ImageIcon addImageIcon(ImageIcon imageIcon, 
				     String imageName, 
				     String imageTip) {
	URL url = this.getClass().getResource(imageName);
	imageIcon = new ImageIcon(url, imageTip);
	return imageIcon;
    }

    /**
     * Add a CenterListener.
     * @param listener CenterListener
     */
    public synchronized void addCenterListener(CenterListener listener) {
	centerDelegate.addCenterListener(listener);
    }

    /**
     * Remove a CenterListener
     * @param listener CenterListener
     */
    public synchronized void removeCenterListener(CenterListener listener) {
	centerDelegate.removeCenterListener(listener);
    }

    /**
     * Add a PanListener.
     * @param listener PanListener
     */
    public synchronized void addPanListener(PanListener listener) {
	panDelegate.addPanListener(listener);
    }

    /**
     * Remove a PanListener
     * @param listener PanListener
     */
    public synchronized void removePanListener(PanListener listener) {
	panDelegate.removePanListener(listener);
    }

    /**
     * Fire a CenterEvent.
     * @param direction int
     */
    protected synchronized void fireCenterEvent(float lat, float lon) {
	centerDelegate.fireCenter(lat, lon);
    }

    /**
     * Fire a PanEvent.
     * @param az azimuth east of north
     */
    protected synchronized void firePanEvent(float az) {
	panDelegate.firePan(az);
    }

    /**
     * Get the useToolTips value.
     * @return boolean useTips
     */
    public boolean getUseToolTips() {
	return useTips;
    }

    /**
     * Return the sum of the heights of the icons.
     * @return height
     */
    public int getHeight() {
	return height;
    }

    /**
     * Return the sum of the widths of the icons.
     * @return width
     */
    public int getWidth() {
	return width;
    }

    /**
     * Set the useToolTips value.
     * @param tip useToolTips value.
     */
    public void setUseToolTips(boolean tip) {
	if (tip) {
	    useTips = true;
	    nwButton.setToolTipText("Pan Northwest");
	    nButton.setToolTipText("Pan North");
	    neButton.setToolTipText("Pan Northeast");
	    wButton.setToolTipText("Pan West");
	    eButton.setToolTipText("Pan East");
	    swButton.setToolTipText("Pan Southwest");
	    sButton.setToolTipText("Pan South");
	    seButton.setToolTipText("Pan Southeast");
	    cButton.setToolTipText("Center Map at Starting Coords");
	} else {
	    useTips = false;
	    nwButton.setToolTipText("");
	    nButton.setToolTipText("");
	    neButton.setToolTipText("");
	    wButton.setToolTipText("");
	    eButton.setToolTipText("");
	    swButton.setToolTipText("");
	    sButton.setToolTipText("");
	    seButton.setToolTipText("");
	    cButton.setToolTipText("");
	}			
    }

    /**
     * Get the pan factor.
     * <p>
     * The panFactor is the amount of screen to shift
     * when panning in a certain direction: 0=none, 1=half-screen shift.
     * @return float panFactor (0.0 &lt;= panFactor &lt;= 1.0)
     */
    public float getPanFactor() {
	return panFactor;
    }

    /**
     * Set the pan factor.
     * <p>
     * This defaults to 1.0.  The panFactor is the amount of screen to shift
     * when panning in a certain direction: 0=none, 1=half-screen shift.
     * @param panFactor (0.0 &lt;= panFactor &lt;= 1.0)
     */
    public void setPanFactor(float panFactor) {
	if ((panFactor < 0f) || (panFactor > 1f)) {
	    throw new IllegalArgumentException(
		    "should be: (0.0 <= panFactor <= 1.0)");
	}
	this.panFactor = panFactor;
    }

    /**
     * Use this function to set where you want the map projection to
     * pan to when the user clicks on "center" button on the
     * navigation panel.  The scale does not change.  When you call
     * this function, the projection does not change.
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
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {

	String command = e.getActionCommand();

	Debug.message("navpanel", "NavigatePanel.actionPerformed(): " +
		      command);
	if (command.equals(panNWCmd)) {
	    firePanEvent(-45f);
	} else if (command.equals(panNCmd)) {
	    firePanEvent(0f);
	} else if (command.equals(panNECmd)) {
	    firePanEvent(45f);
	} else if (command.equals(panECmd)) {
	    firePanEvent(90f);
	} else if (command.equals(panSECmd)) {
	    firePanEvent(135f);
	} else if (command.equals(panSCmd)) {
	    firePanEvent(180f);
	} else if (command.equals(panSWCmd)) {
	    firePanEvent(-135f);
	} else if (command.equals(panWCmd)) {
	    firePanEvent(-90f);
	} else if (command.equals(centerCmd)) {
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
	}
    }

    ///////////////////////////////////////////////////////////////////////////
    ////   OMComponentPanel methods to make the tool work with 
    ////   the MapHandler to find objects it needs.
    ///////////////////////////////////////////////////////////////////////////

    public void findAndInit(Object obj) {
	if (obj instanceof PanListener) {
	    addPanListener((PanListener)obj);
	}
	if (obj instanceof CenterListener) {
	    addCenterListener((CenterListener)obj);
	}
    }

    public void findAndUndo(Object obj) {
	if (obj instanceof PanListener) {
	    removePanListener((PanListener)obj);
	}
	if (obj instanceof CenterListener) {
	    removeCenterListener((CenterListener)obj);
	}
    }

}
