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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/time/TimerControlButtonPanel.java,v $
// $RCSfile: TimerControlButtonPanel.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.time;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.gui.OMComponentPanel;
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
 * The TimerControlButtonPanel provides control for starting and stopping
 * a clock contained in a RealTimeHandler.  This class also has
 * provisions for having the clock run the reverse direction, and for
 * stepping the clock forward and backward one interval.
 */
public class TimerControlButtonPanel extends OMComponentPanel 
    implements PropertyChangeListener, ActionListener, TimeConstants {

    protected ImageIcon backwardStepIcon;
    protected ImageIcon backwardIcon;
    protected ImageIcon forwardIcon;
    protected ImageIcon forwardStepIcon;
    protected ImageIcon pauseIcon;

    protected String DefaultBackwardStepIconURL = "stepbackward.png";
    protected String DefaultBackwardIconURL = "playbackward.png";
    protected String DefaultForwardIconURL = "playforward.png";
    protected String DefaultForwardStepIconURL = "stepforward.png";
    protected String DefaultPauseIconURL = "pause.png";

    protected String backwardStepIconURL = DefaultBackwardStepIconURL;
    protected String backwardIconURL = DefaultBackwardIconURL;
    protected String forwardIconURL = DefaultForwardIconURL;
    protected String forwardStepIconURL = DefaultForwardStepIconURL;
    protected String pauseIconURL = DefaultPauseIconURL;

    protected RealTimeHandler timeHandler;
    protected JButton forwardButton;
    protected JButton backwardButton;

    public final static String BackwardStepIconProperty = "backwardStepIcon";
    public final static String BackwardIconProperty = "backwardIcon";
    public final static String ForwardStepIconProperty = "forwardStepIcon";
    public final static String ForwardIconProperty = "forwardIcon";
    public final static String PauseIconProperty = "pauseIcon";

    public TimerControlButtonPanel(RealTimeHandler rth) {
        super();
        setTimeHandler(rth);
        initGUI();
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
    public void initGUI() {
        removeAll();
        try {
            URL url = PropUtils.getResourceOrFileOrURL(this, forwardIconURL);
            forwardIcon = new ImageIcon(url);
            
            url = PropUtils.getResourceOrFileOrURL(this, forwardStepIconURL);
            forwardStepIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(this, backwardIconURL);
            backwardIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(this, backwardStepIconURL);
            backwardStepIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(this, pauseIconURL);
            pauseIcon = new ImageIcon(url);
        } catch (MalformedURLException murle) {
            Debug.error("TimerToggleButton: initGUI() bad icon.");
        } catch (NullPointerException npe) {
            Debug.error("TimerToggleButton: initGUI() bad icon.");
            npe.printStackTrace();
        }

        JToolBar jtb = new JToolBar();
        jtb.setFloatable(false);

        backwardButton = new JButton(backwardIcon);
        backwardButton.setToolTipText("Run Timer Backwards");
        backwardButton.setActionCommand(TIMER_BACKWARD);
        backwardButton.addActionListener(this);
        jtb.add(backwardButton);

        JButton button = new JButton(backwardStepIcon);
        button.setToolTipText("Step Timer Backward");
        button.setActionCommand(TIMER_STEP_BACKWARD);
        button.addActionListener(this);
        jtb.add(button);

        button = new JButton(forwardStepIcon);
        button.setToolTipText("Step Timer Forward");
        button.setActionCommand(TIMER_STEP_FORWARD);
        button.addActionListener(this);
        jtb.add(button);

        forwardButton = new JButton(forwardIcon);
        forwardButton.setToolTipText("Run Timer Forward");
        forwardButton.setActionCommand(TIMER_FORWARD);
        forwardButton.addActionListener(this);
        jtb.add(forwardButton);

        add(jtb);
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
        if (propName == TIMER_RUNNING_STATUS && obj instanceof String) {
            update((String)obj);
        }
    }

    protected void update(String newStatus) {
        if (newStatus == TIMER_FORWARD) {
            if (Debug.debugging("timedetail")) {
                Debug.output("TimerControlButtonPanel: TIMER_FORWARD");
            }
            backwardButton.setIcon(backwardIcon);
            backwardButton.setActionCommand(TIMER_BACKWARD);
            forwardButton.setIcon(pauseIcon);
            forwardButton.setActionCommand(TIMER_STOPPED);
        } else if (newStatus == TIMER_BACKWARD) {
            if (Debug.debugging("timedetail")) {
                Debug.output("TimerControlButtonPanel: TIMER_BACKWARD");
            }
            forwardButton.setIcon(forwardIcon);
            forwardButton.setActionCommand(TIMER_FORWARD);
            backwardButton.setIcon(pauseIcon);
            backwardButton.setActionCommand(TIMER_STOPPED);
        } else if (newStatus == TIMER_STOPPED) {
            if (Debug.debugging("timedetail")) {
                Debug.output("TimerControlButtonPanel: TIMER_STOPPED");
            }
            forwardButton.setIcon(forwardIcon);
            forwardButton.setActionCommand(TIMER_FORWARD);
            backwardButton.setIcon(backwardIcon);
            backwardButton.setActionCommand(TIMER_BACKWARD);
        }
    }

    /**
     * ActionListener Interface Method listens to the timer, in case
     * something else starts it, we can update the gui.  Also
     * listens to this button, to start and stop the given timer.
     */
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if (cmd == TIMER_FORWARD) {
            timeHandler.setClockDirection(1);
            timeHandler.startClock();
        } else if (cmd == TIMER_BACKWARD) {
            timeHandler.setClockDirection(-1);
            timeHandler.startClock();
        } else if (cmd == TIMER_STEP_BACKWARD) {
            timeHandler.stepBackward();
        } else if (cmd == TIMER_STEP_FORWARD) {
            timeHandler.stepForward();
        } else if (cmd == TIMER_STOPPED) {
            timeHandler.stopClock();
        }
    }
}
