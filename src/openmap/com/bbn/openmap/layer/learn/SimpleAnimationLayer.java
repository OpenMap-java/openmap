/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.learn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

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

    /**
     * Managed list of moving points, separate from the OMGraphicList the layer
     * will use to paint on the map. The objects on the lists are the same, but
     * the lists holding them are different to allow one list to be modified
     * without affecting the actions being performed on the other (concurrent
     * modification).
     */
    OMGraphicList movingPoints = new OMGraphicList();
    /**
     * A movement factor for the points.
     */
    double movementFactor = 1.0;
    /**
     * Timer used to drive the animation for the layer.
     */
    Timer timer;
    /**
     * The timer interval used to make the sprites move.
     */
    int timerDelay = 3000;

    public SimpleAnimationLayer() {
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
        // The ret list is the one used for painting.
        return ret;
    }

    /**
     * Called when the layer is removed from the map. Cleanup!
     */
    public void removed(Container cont) {
        movingPoints.clear();
        stopTimer();
    }

    /**
     * Create a new point for the list. We're going to look at the current
     * projection and find some random point within the height and width of the
     * map. If the projection isn't set on the layer yet (it hasn't been added
     * to the map), no point will be added.
     */
    public void addMovingPoint() {

        Projection proj = getProjection();

        if (proj == null) {
            return;
        }

        int numOfSpritesToAdd = 1;

        if (spinner != null) {
            try {
                numOfSpritesToAdd = Integer.parseInt(spinner.getValue().toString());
            } catch (NumberFormatException nfe) {
                spinner.setValue(1);
            }
        }

        double mapHeight = proj.getHeight();
        double mapWidth = proj.getWidth();

        for (int i = 0; i < numOfSpritesToAdd; i++) {

            double ranY = (Math.random() * mapHeight);
            double ranX = (Math.random() * mapWidth);

            Point2D newLoc = proj.inverse(ranX, ranY);

            OMPoint point = new OMPoint(newLoc.getY(), newLoc.getX(), 5);
            point.setFillPaint(Color.red);
            movingPoints.add(point);
        }

        spriteCountLabel.setText(Integer.toString(movingPoints.size()));

        // Update the map
        doPrepare();
    }

    /**
     * Clears the point list.
     */
    public void clearMovingPoints() {
        movingPoints.clear();
        spriteCountLabel.setText(Integer.toString(movingPoints.size()));
        // Update the map
        doPrepare();
        stopTimer();
    }

    /**
     * Called when the timer should be stopped.
     */
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        if (timerButton != null) {
            timerButton.setSelected(false);
        }
    }

    /**
     * Called when the timer should be started.
     * 
     * @param interval the interval that the timer task gets executed on.
     */
    public void resetTimer(int interval) {
        timerDelay = interval;

        stopTimer();

        timer = new Timer();
        timer.scheduleAtFixedRate(new ManageGraphicsTask(SimpleAnimationLayer.this), 0, timerDelay);

        if (timerButton != null) {
            timerButton.setSelected(true);
        }
    }

    JPanel panel = null;
    JButton addButton = null;
    JCheckBox timerButton = null;
    JSpinner spinner = null;
    JLabel spriteCountLabel = null;

    /**
     * This method is called by other OpenMap components to present a GUI
     * interface to a user. We keep an handle to a panel that we reuse for later
     * requests.
     */
    public Component getGUI() {
        if (panel == null) {
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();

            panel = new JPanel(gridbag);

            addButton = new JButton("Add Sprites");
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    addMovingPoint();
                }
            });

            c.insets = new Insets(5, 5, 5, 2);
            c.anchor = GridBagConstraints.NORTHWEST;
            gridbag.setConstraints(addButton, c);
            panel.add(addButton);

            JButton clearButton = new JButton("Clear Sprites");
            clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    clearMovingPoints();
                }
            });

            c.insets = new Insets(5, 2, 5, 5);
            c.anchor = GridBagConstraints.NORTHEAST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(clearButton, c);
            panel.add(clearButton);

            JLabel numSpritesLabel = new JLabel("Number of Sprites to Add:");
            SpinnerModel numSpritesSpinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
            spinner = new JSpinner(numSpritesSpinnerModel);
            numSpritesLabel.setLabelFor(spinner);

            c.insets = new Insets(0, 5, 5, 5);
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.RELATIVE;

            gridbag.setConstraints(numSpritesLabel, c);
            panel.add(numSpritesLabel);

            c.anchor = GridBagConstraints.EAST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(spinner, c);
            panel.add(spinner);

            JLabel numMapSpritesLabel = new JLabel("Number of Sprites on Map:");
            spriteCountLabel = new JLabel(Integer.toString(movingPoints.size()));

            c.insets = new Insets(0, 5, 5, 5);
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.RELATIVE;

            gridbag.setConstraints(numMapSpritesLabel, c);
            panel.add(numMapSpritesLabel);

            c.anchor = GridBagConstraints.EAST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(spriteCountLabel, c);
            panel.add(spriteCountLabel);

            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0/* min */, 20/* max */, 10/* initial */);
            java.util.Hashtable dict = new java.util.Hashtable();
            dict.put(new Integer(5), new JLabel(".5"));
            dict.put(new Integer(10), new JLabel("1"));
            dict.put(new Integer(15), new JLabel("1.5"));
            dict.put(new Integer(20), new JLabel("2"));
            slider.setLabelTable(dict);
            slider.setPaintLabels(true);
            slider.setMajorTickSpacing(2);
            slider.setPaintTicks(true);

            slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ce) {
                    JSlider slider2 = (JSlider) ce.getSource();
                    if (slider2.getValueIsAdjusting()) {
                        float interval = ((float) (slider2.getValue()) + .01f) * 100f;
                        Debug.output("SimpleAnimationLayer delay set to: " + interval / 1000f
                                + " seconds");
                        resetTimer((int) interval);
                    }
                }
            });

            JPanel sliderPanel = PaletteHelper.createHorizontalPanel("Timer interval in seconds:");
            sliderPanel.add(slider);

            c.insets = new Insets(0, 5, 5, 5);
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            gridbag.setConstraints(sliderPanel, c);
            panel.add(sliderPanel);

            timerButton = new JCheckBox("Run Timer", timer != null);
            timerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox check = (JCheckBox) ae.getSource();
                    if (check.isSelected()) {
                        resetTimer(timerDelay);
                    } else {
                        stopTimer();
                    }
                }
            });

            gridbag.setConstraints(timerButton, c);
            panel.add(timerButton);

        }
        return panel;
    }

    /**
     * This is a TimerTask the timer uses to tell the points to update their
     * position.
     * 
     * This is where you would do work to change the position of your OMGraphics
     * - whether by reading a data file, or checking a web site, whatever. This
     * method could also be the call another object could call with new
     * OMGraphics to display, if you wanted a different object to control the
     * updates.
     */
    public class ManageGraphicsTask extends TimerTask {

        final SimpleAnimationLayer sal;

        public ManageGraphicsTask(SimpleAnimationLayer layer) {
            sal = layer;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            Projection proj = sal.getProjection();
            OMGraphicList safeList = new OMGraphicList(sal.movingPoints);

            for (OMGraphic point : safeList) {
                moveRandomly((OMPoint) point, sal.movementFactor, proj);

                if (proj != null) {
                    point.generate(proj);
                }
            }

            /*
             * We have a couple of options here. We could call doPrepare(),
             * which will launch a separate thread to call prepare() and
             * repaint(). prepare() will call generate() on all the OMGraphics,
             * but we've already done that above. So we can ask for the event
             * thread to paint the layer by calling repaint(). This saves us
             * from launching a separate thread to do the same thing. If we
             * didn't call generate() on the points above, we would have to call
             * doPrepare() instead of repaint(), because the points need to be
             * generated after they are moved, in order to know where they get
             * drawn.
             */
            sal.repaint();
        }

        /**
         * Simple method to move an OMPoint around randomly.
         * 
         * @param point the OMPoint to move.
         * @param factor a movement factor, in pixels.
         */
        protected void moveRandomly(OMPoint point, double factor, Projection proj) {
            double hor = Math.random() - .5;
            double vert = Math.random() - .5;

            Point2D mapPoint = proj.forward(point.getLat(), point.getLon());
            mapPoint.setLocation(mapPoint.getX() + (hor * factor), mapPoint.getY()
                    + (vert * factor));
            Point2D llp = proj.inverse(mapPoint);

            point.setLat(llp.getY());
            point.setLon(llp.getX());
        }
    }

}
