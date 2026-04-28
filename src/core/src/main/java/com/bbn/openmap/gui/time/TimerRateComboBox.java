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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/time/TimerRateComboBox.java,v $
// $RCSfile: TimerRateComboBox.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.time;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JComboBox;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.time.RealTimeHandler;
import com.bbn.openmap.util.Debug;

/**
 * The TimerRateComboBox is a general pace selector for a
 * RealTimeHandler. It provides a JComboBox interface (pop-up menu)
 * that shows discrete choices of rates for timer/scenario settings.
 * The RealTimeHandler can set these choices, with the GUI display
 * name of the choice, the timer interval and the scenario pace that
 * the interval represents.
 */
public class TimerRateComboBox extends JComboBox implements ActionListener,
        PropertyConsumer {

    /**
     * The RealTimeHandler to be updated.
     */
    protected RealTimeHandler timeHandler;

    public TimerRateComboBox(RealTimeHandler rth) {
        super();
        addActionListener(this);
        setTimeHandler(rth);
    }

    /**
     * Only TimerRateHolders can be added.
     */
    public void addItem(Object obj) {
        if (obj instanceof TimerRateHolder) {
            super.addItem(obj);
        } else {
            Debug.error("TimerRateComboBox: Only TimerRateHolders can be added");
        }
    }

    /**
     * The preferred way to add choices, since it creates the
     * TimerRateHolder for you.
     */
    public void add(String string, int interval, int pace) {
        super.addItem(new TimerRateHolder(string, interval, pace));
    }

    public void setTimeHandler(RealTimeHandler rth) {
        timeHandler = rth;
    }

    public RealTimeHandler getTimeHandler() {
        return timeHandler;
    }

    /**
     * When a choice is made, the TimerRateComboBox updates the
     * RealTimeHandler.
     */
    public void actionPerformed(ActionEvent ae) {
        Object obj = ae.getSource();

        if (obj == this) {
            TimerRateHolder trh = (TimerRateHolder) getSelectedItem();
            trh.modifyTimer(getTimeHandler());
        }
    }

    ///// Property Consumer Interface Methods

    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);
    }

    public Properties getProperties(Properties props) {
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        return props;
    }

    protected String propPrefix = null;

    public String getPropertyPrefix() {
        return propPrefix;
    }

    public void setPropertyPrefix(String prefix) {
        propPrefix = prefix;
    }

    public class TimerRateHolder {

        protected String title;
        protected int timerInterval;
        protected int pace;

        public TimerRateHolder(String t, int ti, int p) {
            title = t;
            timerInterval = ti;
            pace = p;
        }

        public void modifyTimer(RealTimeHandler rth) {
            rth.setPace(pace);
            rth.setUpdateInterval(timerInterval);
        }

        public String toString() {
            return title;
        }
    }
}