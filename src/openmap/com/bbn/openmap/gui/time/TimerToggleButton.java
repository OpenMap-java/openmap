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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/time/TimerToggleButton.java,v $
// $RCSfile: TimerToggleButton.java,v $
// $Revision: 1.1 $
// $Date: 2003/05/06 23:06:46 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.time;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import javax.swing.*;

/**
 * The TimerToggleButton provides a control for starting and stopping
 * a clock contained in a RealTimeHandler.
 */
public class TimerToggleButton extends JToggleButton 
    implements PropertyChangeListener, PropertyConsumer, ActionListener, TimeConstants {

    protected ImageIcon running;
    protected ImageIcon stopped;
    protected ImageIcon inactive;

    protected String DefaultRunningIconURL = "timergreen.png";
    protected String DefaultStoppedIconURL = "timerred.png";
    protected String DefaultInactiveIconURL = "timeroff.png";

    protected String runningIconURL = DefaultRunningIconURL;
    protected String stoppedIconURL = DefaultStoppedIconURL;
    protected String inactiveIconURL = DefaultInactiveIconURL;

    protected RealTimeHandler timeHandler;

    public final static String RunningIconProperty = "runningIcon";
    public final static String StoppedIconProperty = "stoppedIcon";
    public final static String InactiveIconProperty = "inactiveIcon";
    public final static String LabelProperty = "label";

    public TimerToggleButton(RealTimeHandler rth) {
	super();
	addActionListener(this);
	setTimeHandler(rth);
	initIcons();
    }

    public void setTimeHandler(RealTimeHandler rth) {
	timeHandler = rth;
    }

    public RealTimeHandler getTimeHandler() {
	return timeHandler;
    }

    /**
     * Set the ImageIcons to whatever is set on the URL variables.
     * Sets the running icon to be the pressed icon, and makes the
     * stopped and inactive icons.
     */
    public void initIcons() {
	try {
	    URL url = PropUtils.getResourceOrFileOrURL(this, runningIconURL);
	    running = new ImageIcon(url);
	    setPressedIcon(running);
	    url = PropUtils.getResourceOrFileOrURL(this, stoppedIconURL);
	    stopped = new ImageIcon(url);
	    url = PropUtils.getResourceOrFileOrURL(this, inactiveIconURL);
	    inactive = new ImageIcon(url);
	    updateIcon(TIMER_INACTIVE);
	} catch (MalformedURLException murle) {
	    Debug.error("TimerToggleButton: initIcons() bad icon.");
	} catch (NullPointerException npe) {
	    Debug.error("TimerToggleButton: initIcons() bad icon.");
	    npe.printStackTrace();
	}
    }

    /**
     * Set the appearance based on the timer's status.
     */
    protected void updateIcon(String status) {
	if (status == TIMER_RUNNING) {
	    setSelected(true);
	    setIcon(running);
	} else {
	    setSelected(false);
	    if (status == TIMER_STOPPED) {
		setIcon(stopped);
	    } else {
		setIcon(inactive);
	    }
	}
    }

    /**
     * PropertyChangeListener Interface Method used to find out when
     * the timer has been stopped and started.  Is expecting that the
     * property name and value are the actual string objects defined
     * in the TimeConstants interface. It does ==, not equals().
     */
    public void propertyChange(PropertyChangeEvent pce) {
	String propName = pce.getPropertyName();
	Object obj = pce.getNewValue();
	if (propName == TIMER_RUNNING_STATUS) {
	    if (obj == TIMER_STOPPED) {
		updateIcon(TIMER_STOPPED);
	    } else if (obj == TIMER_INACTIVE) {
		updateIcon(TIMER_INACTIVE);
	    } else if (obj == TIMER_RUNNING) {
		updateIcon(TIMER_RUNNING);
	    }
	}
    }

    /**
     * ActionListener Interface Method listens to the timer, in case
     * something else starts it, we can update the gui.  Also
     * listens to this button, to start and stop the given timer.
     */
    public void actionPerformed(ActionEvent ae) {
	Object source = ae.getSource();
	if (source == this) {
	    if (isSelected()) {
		timeHandler.startClock();
	    } else {
		timeHandler.stopClock();
	    }
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

}



