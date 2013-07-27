/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.learn;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.graphicLoader.GLPoint;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A simple demonstration of doing animation on a Layer. This layer has a GUI
 * interface to add/remove points on the map, and they are moved randomly based
 * on timer intervals. All the parameters can be modified in the GUI.
 * 
 * This layer uses code fetched from the AnimationTester and
 * AbstractGraphicLoader.
 * 
 * @author dietrick
 */
public class SimpleAnimationLayer extends OMGraphicHandlerLayer {

    /*
     * Managed list of moving points, separate from the OMGraphicList the layer
     * will use to paint on the map. The objects on the lists are the same, but
     * the lists holding them are different to allow one list to be modified
     * without affecting the actions being performed on the other (concurrent
     * modification).
     */
    OMGraphicList movingPoints = new OMGraphicList();
    /*
     * A movement factor for the points.
     */
    float movementFactor = 1f;
    /*
     * Timer used to drive the animation for the layer.
     */
    Timer timer;

    public SimpleAnimationLayer() {
        // Configure the timer
        timer = new Timer(3000, this);
        timer.setInitialDelay(0);
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                manageGraphics();
            }
        });
    }

    /**
     * The prepare method always returns the OMGraphicList to be drawn on the
     * map. We're assuming that movingPoints holds the OMPoints as they are
     * modified. We need to create a separate OMGraphicList to return for
     * painting after we call generate() on the OMGraphics with the current map.
     * If we forget to call generate(), the OMGraphics will not know where they
     * are on the map, and they won't drawn themselves.
     * 
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#prepare()
     */
    public synchronized OMGraphicList prepare() {
        OMGraphicList ret = new OMGraphicList(movingPoints);
        ret.generate(getProjection());
        return ret;
    }

    /**
     * This is the call the timer makes to tell the points to update their
     * position.
     * 
     * This is where you would do work to change the position of your OMGraphics
     * - whether by reading a data file, or checking a web site, whatever. This
     * method could also be the call another object could call with new
     * OMGraphics to display, if you wanted a different object to control the
     * updates.
     */
    public void manageGraphics() {
        Projection p = getProjection();

        Iterator it = movingPoints.iterator();
        while (it.hasNext()) {
            OMPoint point = (OMPoint) it.next();
            moveRandomly(point, movementFactor);
            point.generate(p);
        }

        // IMPORTANT!!! this causes prepare() to be called in a separate thread,
        // and repaint() will automatically be called. repaint() causes the map
        // to update via the EventDispatch thread.
        doPrepare();
    }

    /**
     * Simple method to move an OMPoint around randomly.
     * 
     * @param point
     * @param factor
     */
    protected void moveRandomly(OMPoint point, float factor) {
        double hor = Math.random() - .5;
        double vert = Math.random() - .5;

        point.setLat(point.getLat() + (float) vert * factor);
        point.setLon(point.getLon() + (float) hor * factor);
    }

    /**
     * Create a new point for the list.
     */
    public void addMovingPoint() {

        float ranLat = (float) (Math.random() * 180) - 90f;
        float ranLon = (float) (Math.random() * 360) - 180f;

        OMPoint point = new OMPoint(ranLat, ranLon, 5);
        point.setFillPaint(Color.red);
        movingPoints.add(point);
    }

    /**
     * Clears the point list.
     */
    public void clearMovingPoints() {
        movingPoints.clear();
    }

    JPanel panel = null;

    public Component getGUI() {
        if (panel == null) {
            panel = new JPanel(new GridLayout(0, 1));

            JPanel buttonBox = new JPanel();
            JButton button = new JButton("Add Sprite");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    addMovingPoint();
                    manageGraphics();
                    timer.restart();
                }
            });
            buttonBox.add(button);

            button = new JButton("Clear Sprites");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    clearMovingPoints();
                    manageGraphics();
                    timer.restart();
                }
            });
            buttonBox.add(button);
            panel.add(buttonBox);

            JLabel label = new JLabel("Timer interval in seconds:");
            panel.add(label);
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0/* min */, 50/* max */, 20/* initial */);
            java.util.Hashtable dict = new java.util.Hashtable();
            dict.put(new Integer(10), new JLabel("1"));
            dict.put(new Integer(20), new JLabel("2"));
            dict.put(new Integer(30), new JLabel("3"));
            dict.put(new Integer(40), new JLabel("4"));
            dict.put(new Integer(50), new JLabel("5"));
            slider.setLabelTable(dict);
            slider.setPaintLabels(true);
            slider.setMajorTickSpacing(5);
            slider.setPaintTicks(true);

            slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ce) {
                    JSlider slider2 = (JSlider) ce.getSource();
                    if (slider2.getValueIsAdjusting()) {
                        manageGraphics();
                        float interval = ((float) (slider2.getValue()) + .01f) * 100f;
                        Debug.output("SimpleAnimationLayer delay set to: " + interval / 1000f
                                + " seconds");
                        timer.setDelay((int) interval);

                    }
                }
            });
            panel.add(slider);

            JCheckBox timerButton = new JCheckBox("Run Timer", timer.isRunning());
            timerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox check = (JCheckBox) ae.getSource();
                    if (check.isSelected()) {
                        manageGraphics();
                        timer.restart();
                    } else {
                        timer.stop();
                    }
                }
            });

            panel.add(timerButton);

        }
        return panel;
    }

}
