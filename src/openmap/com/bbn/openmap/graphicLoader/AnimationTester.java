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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/AnimationTester.java,v $
// $RCSfile: AnimationTester.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The AnimationTester is a simple GraphicLoader that lets you toss a
 * bunch of sprites (circles) up on the map to watch them wiggle, to
 * get a feel of the paint delay of the map. You can add sprites to
 * the map (they get placed randomly), and clear the list, and adjust
 * the length of delay between repaints().
 */
public class AnimationTester extends AbstractGraphicLoader {

    OMGraphicList nodes = new OMGraphicList();
    float factor = 1f;

    public AnimationTester() {}

    public void manageGraphics() {
        if (Debug.debugging("animationtester")) {
            Debug.output("AnimationTester.manageGraphics with " + nodes.size()
                    + " node(s).");
        }
        Projection p = getProjection();

        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            GLPoint point = (GLPoint) it.next();
            point.moveRandomly(factor);
            point.generate(p);
        }

        OMGraphicHandler receiver = getReceiver();
        if (receiver != null) {
            receiver.setList(nodes);
        }
    }

    public void addNode() {

        float ranLat = (float) (Math.random() * 180) - 90f;
        float ranLon = (float) (Math.random() * 360) - 180f;

        GLPoint point = new GLPoint(ranLat, ranLon, 5, true);
        point.setFillPaint(Color.red);
        nodes.add(point);
    }

    public void clearNodes() {
        nodes.clear();
    }

    JPanel panel = null;
    final String addCmd = "AddSpriteCommand";
    final String clearCmd = "ClearSpriteCommand";
    final String timerCmd = "TimerCommand";

    public Component getGUI() {
        if (panel == null) {
            panel = new JPanel(new GridLayout(0, 1));

            JPanel buttonBox = new JPanel();
            JButton button = new JButton("Add Sprite");
            button.setActionCommand(addCmd);
            button.addActionListener(this);
            buttonBox.add(button);

            button = new JButton("Clear Sprites");
            button.setActionCommand(clearCmd);
            button.addActionListener(this);
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
                        Debug.output("Animation Tester delay set to: "
                                + interval / 1000f + " seconds");
                        setUpdateInterval((int) interval);

                    }
                }
            });
            panel.add(slider);

            JCheckBox timerButton = new JCheckBox("Run Timer", getTimer().isRunning());
            timerButton.setActionCommand(TimerCmd);
            timerButton.addActionListener(this);

            panel.add(timerButton);

        }
        return panel;
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();

        if (command.equals(addCmd)) {
            addNode();
            if (!getTimer().isRunning()) {
                manageGraphics();
            }
        } else if (command.equals(clearCmd)) {
            clearNodes();
            if (!getTimer().isRunning()) {
                manageGraphics();
            }
        } else if (command.equals(TimerCmd)) {
            JCheckBox check = (JCheckBox) ae.getSource();
            if (check.isSelected()) {
                manageGraphics();
                getTimer().restart();
            } else {
                getTimer().stop();
            }
        } else {
            manageGraphics();
        }
    }
}