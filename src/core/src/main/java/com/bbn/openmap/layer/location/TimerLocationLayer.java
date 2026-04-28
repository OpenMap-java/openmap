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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/TimerLocationLayer.java,v $
// $RCSfile: TimerLocationLayer.java,v $
// $Revision: 1.6 $
// $Date: 2006/01/18 17:44:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.util.PropUtils;

/**
 * The TimerLocationLayer is a LocationLayer that has a timer to automatically
 * relod data at specified interval.
 * 
 * The properties for this layer are the same as a LocationLayer, with the
 * addition of two:
 * <P>
 * 
 * <pre>
 * 
 *  # Specify the interval (milliseconds) for the timer. Default is 10 seconds.
 *  layer.updateTimerInterval=10000
 *  # Auto-start/stop the timer when the layer is part of the Default
 *  # is true.  There is a control to start/stop the timer on the
 *  # palette.
 *  layer.automaticTimer=true;
 * 
 * </pre>
 */
public class TimerLocationLayer
        extends LocationLayer {

    /** Reloading timer. */
    protected Timer timer;

    /** updateTimerInterval */
    public static final String UpdateTimerIntervalProperty = "updateTimerInterval";
    /** automaticTimer */
    public static final String AutoTimerProperty = "automaticTimer";
    /**
     * Flag to note whether file reloading should only happen when the layer is
     * visible. True by default.
     */
    protected boolean autoTimer = true;

    private final com.bbn.openmap.Layer layer = this;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public TimerLocationLayer() {
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                if (e.getComponent() == layer && getAutoTimer() && timerButton != null && !timerButton.isSelected()) {

                    timerButton.doClick();
                }
            }

            public void componentHidden(ComponentEvent e) {
                if (e.getComponent() == layer && getAutoTimer() && timerButton != null && timerButton.isSelected()) {

                    timerButton.doClick();
                }
            }
        });
    }

    public void projectionChanged(ProjectionEvent e) {
        super.projectionChanged(e);

        if (autoTimer && timer != null
                && ((timerButton != null && timerButton.isSelected()) || (timerButton == null && getUpdateInterval() > 0))) {

            timer.restart();
            updateTimerButton();
        }
    }

    /**
     * This method is called after the layer is removed from the MapBean and
     * when the projection changes. If the autoTimer is set, the timer is
     * stopped.
     */
    public void removed(java.awt.Container cont) {
        if (autoTimer) {
            timer.stop();
            updateTimerButton();
        }
    }

    protected JCheckBox timerButton = null;
    protected JCheckBox autoTimerButton = null;

    /**
     * Provides the palette widgets to control the options of showing maps, or
     * attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
        box = null;
        box = (Box) super.getGUI();

        // Only want to do this once...
        if (getTimer() != null) {
            if (timerButton == null) {
                String bTitle = "Run Update Timer";
                int interval = getUpdateInterval();
                if (interval > 0) {
                    bTitle = "Reload Data (" + (interval / 1000) + " sec)";
                }
                timerButton = new JCheckBox(bTitle, getTimer().isRunning());
                timerButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        JCheckBox check = (JCheckBox) ae.getSource();
                        Timer t = getTimer();
                        if (t != null) {
                            if (check.isSelected()) {
                                t.restart();
                            } else {
                                t.stop();
                            }
                        }
                    }
                });
                timerButton.setToolTipText("<HTML><BODY>Reload the map data from the original source at specified intervals.</BODY></HTML>");
            }

            if (autoTimerButton == null) {
                autoTimerButton = new JCheckBox("Reload Only When Visible", getAutoTimer());
                autoTimerButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        JCheckBox check = (JCheckBox) ae.getSource();
                        setAutoTimer(check.isSelected());
                    }
                });
                autoTimerButton.setToolTipText("<HTML><BODY>Only run the timer when the layer is active on the map.</BODY></HTML>");
            }

            JPanel tbp = new JPanel(new GridLayout(0, 1));
            tbp.add(timerButton);
            tbp.add(autoTimerButton);
            box.add(tbp);
        }
        return box;
    }

    /**
     * The properties and prefix are managed and decoded here, for the standard
     * uses of the LocationLayer.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);
        String realPrefix = "";

        if (prefix != null) {
            realPrefix = prefix + ".";
        }

        setUpdateInterval(PropUtils.intFromProperties(properties, realPrefix + UpdateTimerIntervalProperty, updateInterval));
        setAutoTimer(PropUtils.booleanFromProperties(properties, realPrefix + AutoTimerProperty, autoTimer));
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
     * 
     * @param props a Properties object to load the PropertyConsumer properties
     *        into. If props equals null, then a new Properties object should be
     *        created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + UpdateTimerIntervalProperty, Integer.toString(updateInterval));
        props.put(prefix + AutoTimerProperty, new Boolean(autoTimer).toString());

        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.). For Layer, this method should at least
     * return the 'prettyName' property.
     * 
     * @param list a Properties object to load the PropertyConsumer properties
     *        into. If getList equals null, then a new Properties object should
     *        be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        PropUtils.setI18NPropertyInfo(i18n, list, TimerLocationLayer.class, UpdateTimerIntervalProperty, "Timer interval",
                                      "Number of milliseconds for automatic file reloading.", null);

        PropUtils.setI18NPropertyInfo(i18n, list, TimerLocationLayer.class, AutoTimerProperty, "Auto Timer",
                                      "Flag to start/stop timer automatically when layer is on map.",
                                      "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

        return list;
    }

    /**
     * Sets whether the timer should automatically be turned on and off when the
     * layer is added and removed from the map.
     * <P>
     * 
     * If the layer is not visible, the timer may be started or stopped when
     * this method is called. If the autoTimer is turned off, and the layer has
     * received a projection before, the timer is turned on. If the autoTimer is
     * on, the timer will be stopped.
     */
    public void setAutoTimer(boolean value) {
        autoTimer = value;
        if (autoTimerButton != null) {
            autoTimerButton.setSelected(getAutoTimer());
        }

        if (!isVisible()) {
            if (!value && getProjection() != null) {
                timer.restart();
            } else if (value) {
                timer.stop();
            }
        }
    }

    public boolean getAutoTimer() {
        return autoTimer;
    }

    /**
     * Get the timer being used for automatic updates. May be null if a timer is
     * not set.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * If you want the layer to update itself at certain intervals, you can set
     * the timer to do that. Set it to null to disable it.
     */
    public void setTimer(Timer t) {
        if (timer != null) {
            timer.stop();
        }

        timer = t;
        updateTimerButton();
    }

    /**
     * What to do when the timer goes off.
     */
    public void timerPing() {
        reloadData();
        if (isVisible()) {
            doPrepare();
        }
    }

    /**
     * Creates a timer with the current updateInterval and calls setTimer().
     */
    public void createTimer() {
        Timer t = new Timer(updateInterval, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                timerPing();
            }
        });

        t.setInitialDelay(0);
        setTimer(t);
    }

    /**
     * The delay between timer pulses, in milliseconds. Default is 10 seconds.
     */
    protected int updateInterval = 10000;

    /**
     * Set how often the timer calls timerPing. If less than or equal to zero,
     * the timer will be stopped, but the interval will not be affected.
     */
    public void setUpdateInterval(int delay) {
        if (delay > 0) {
            updateInterval = delay;
            if (timer == null) {
                createTimer();
            }
            timer.setDelay(updateInterval);
            if (timer.isRunning()) {
                timer.restart();
            }
        } else if (timer != null) {
            timer.stop();
        }
        updateTimerButton();
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Enable the timer button if there is a timer, check it on if the timer is
     * running.
     */
    protected void updateTimerButton() {
        if (timerButton != null) {
            timerButton.setEnabled(timer != null);
            timerButton.setSelected(timer != null && timer.isRunning());
        }
    }

}
