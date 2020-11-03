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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/plugin/pilot/Pilot.java,v $
// $RCSfile: Pilot.java,v $
// $Revision: 1.5 $
// $Date: 2009/02/23 22:37:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.j3d.pilot;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Point2D;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.bbn.openmap.Environment;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 * The Pilot is the base class representing a location over the map.
 */
public class Pilot extends OMPoint implements ActionListener, FocusListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Color CONNECTED_COLOR = Color.green;
    public static Color DISCONNECTED_COLOR = Color.red;
    /** Flag to mark whether the pilot is moving or not. */
    protected boolean stationary = true;
    protected String name;
    protected double height = 0;// meters.

    public Pilot(double lat, double lon, int radius, boolean isOval) {
        super(lat, lon, radius);
        setOval(isOval);
    }

    /** Really has no meaning, other than to set the color. */
    public void resetConnected() {
        setFillPaint(DISCONNECTED_COLOR);
    }

    /** Really has no meaning, other than to set the color. */
    public void connected(boolean connected) {
        if (connected) {
            setFillPaint(CONNECTED_COLOR);
        }
    }

    /**
     * A little method that will cause the location to move around a
     * little.
     */
    protected void moveRandomly(float factor) {
        double hor = Math.random() - .5;
        double vert = Math.random() - .5;

        setLat(getLat() + (float) vert / factor);
        setLon(getLon() + (float) hor / factor);
    }

    /**
     * If not stationary, then the location will move around randomly
     * to a different location.
     */
    public void move(float factor) {
        if (!stationary) {
            moveRandomly(factor);
        }
    }

    public void move(int distance, Length units, float Az) {
        Point2D newLocation = GreatCircle.sphericalBetween(ProjMath.degToRad(getLat()),
                ProjMath.degToRad(getLon()),
                units.toRadians(distance),
                Az);

        setLat((float) newLocation.getY());
        setLon((float) newLocation.getX());
    }

    public void setStationary(boolean set) {
        stationary = set;
        if (movementButton != null) {
            movementButton.setSelected(set);
        }
    }

    public boolean getStationary() {
        return stationary;
    }

    public void setName(String set) {
        name = set;
    }

    public String getName() {
        return name;
    }

    public void setHeight(double h) {
        height = h;
        if (heightField != null) {
            heightField.setText(Double.toString(h));
        }
        if (Debug.debugging("pilotloader")) {
            Debug.output("Pilot: " + getName() + " setting height to : " + h);
        }
    }

    public double getHeight() {
        return height;
    }

    protected transient java.awt.Container palette = null;

    /**
     * Make the palette visible. Will automatically determine if we're
     * running in an applet environment and will use a JInternalFrame
     * over a JFrame if necessary.
     */
    public void showPalette() {
        if (Environment.getBoolean(Environment.UseInternalFrames)) {

            final JLayeredPane desktop = Environment.getInternalFrameDesktop();

            // get the window
            palette = PaletteHelper.getPaletteInternalWindow(getGUI(),
                    getName(),
                    new InternalFrameAdapter() {
                        public void internalFrameClosed(InternalFrameEvent e) {
                            if (desktop != null) {
                                desktop.remove((JInternalFrame) palette);
                                desktop.repaint();
                            }
                            palette = null;
                            // firePaletteEvent(false);
                        }
                    });
            // add the window to the desktop
            if (desktop != null) {
                desktop.add(palette);
                palette.setVisible(true);
            }
        } else {
            if (palette == null) {
                palette = PaletteHelper.getPaletteWindow(getGUI(),
                        getName(),
                        new ComponentAdapter() {
                            public void componentHidden(ComponentEvent e) {
                            // firePaletteEvent(false);
                            }
                        });
            }
            palette.setVisible(true);
            ((JFrame) palette).setState(java.awt.Frame.NORMAL);
        }
    }

    /**
     * Hide the Pilot's palette.
     */
    public void hidePalette() {
        if (palette == null) {
            return;
        }

        if (Environment.getBoolean(Environment.UseInternalFrames)) {
            // close the palette
            try {
                ((JInternalFrame) palette).setClosed(true);
            } catch (java.beans.PropertyVetoException evt) {
                com.bbn.openmap.util.Assert.assertExp(false,
                        "Pilot.hidePalette(): internal error!");
            }
        } else {
            palette.setVisible(false);
        }
    }

    JCheckBox movementButton = null;
    JTextField heightField = null;

    /**
     * Gets the gui controls associated with the Pilot. This default
     * implementation returns null indicating that the Pilot has no
     * gui controls.
     * 
     * @return java.awt.Component or null
     */
    public java.awt.Component getGUI() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        // Only want to do this once...
        if (movementButton == null) {
            movementButton = new JCheckBox("Stationary", getStationary());
            movementButton.addActionListener(this);
            movementButton.setActionCommand(MoveCmd);
        }

        panel.add(movementButton);

        JPanel heightPanel = new JPanel(new GridLayout(0, 3));

        heightPanel.add(new JLabel("Object height: "));
        if (heightField == null) {
            heightField = new JTextField(Double.toString(height), 10);
            heightField.setHorizontalAlignment(JTextField.RIGHT);
            heightField.addActionListener(this);
            heightField.addFocusListener(this);
        }
        heightPanel.add(heightField);
        heightPanel.add(new JLabel(" meters"));

        panel.add(heightPanel);

        return panel;
    }

    public final static String MoveCmd = "MoveCommand";

    public void actionPerformed(java.awt.event.ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if (cmd == MoveCmd) {
            JCheckBox check = (JCheckBox) ae.getSource();
            setStationary(check.isSelected());
        } else {
            try {
                setHeight(Float.parseFloat(cmd));
            } catch (NumberFormatException nfe) {
                setHeight(getHeight());
            }
        }
    }

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
        try {
            setHeight(Float.parseFloat(((JTextField) (e.getSource())).getText()));
        } catch (NumberFormatException nfe) {
            setHeight(0);
        }
    }

}