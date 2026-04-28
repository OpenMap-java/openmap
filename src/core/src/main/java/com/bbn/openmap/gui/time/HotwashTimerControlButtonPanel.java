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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/time/HotwashTimerControlButtonPanel.java,v $
// $RCSfile: HotwashTimerControlButtonPanel.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:31:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.time;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.bbn.openmap.time.RealTimeHandler;
import com.bbn.openmap.time.TimerStatus;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The TimerControlButtonPanel provides control for starting and stopping a
 * clock contained in a RealTimeHandler. This class also has provisions for
 * having the clock run the reverse direction, and for stepping the clock
 * forward and backward one interval.
 */
public class HotwashTimerControlButtonPanel extends TimerControlButtonPanel {

    JButton forwardStepButton;
    JButton backwardStepButton;
    
    public HotwashTimerControlButtonPanel() {
    }
    
    public HotwashTimerControlButtonPanel(RealTimeHandler rth) {
        super(rth);
    }

    /**
     * Set the ImageIcons to whatever is set on the URL variables. Sets the
     * running icon to be the pressed icon, and makes the stopped and inactive
     * icons.
     */
    public void initGUI() {
        removeAll();
        try {
            URL url = PropUtils.getResourceOrFileOrURL(TimerControlButtonPanel.class,
                    forwardIconURL);
            forwardIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(TimerControlButtonPanel.class,
                    forwardStepIconURL);
            forwardStepIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(TimerControlButtonPanel.class,
                    backwardIconURL);
            backwardIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(TimerControlButtonPanel.class,
                    backwardStepIconURL);
            backwardStepIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(TimerControlButtonPanel.class,
                    pauseIconURL);
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
        backwardButton.setActionCommand(TimerStatus.TIMER_BACKWARD);
        backwardButton.addActionListener(this);
        // jtb.add(backwardButton);

        forwardStepButton = new JButton(backwardStepIcon);
        forwardStepButton.setToolTipText("Step Timer Backward");
        forwardStepButton.setActionCommand(TimerStatus.TIMER_STEP_BACKWARD);
        forwardStepButton.addActionListener(this);
        jtb.add(forwardStepButton);

        forwardButton = new JButton(forwardIcon);
        forwardButton.setToolTipText("Run Timer Forward");
        forwardButton.setActionCommand(TimerStatus.TIMER_FORWARD);
        forwardButton.addActionListener(this);
        jtb.add(forwardButton);

        backwardStepButton = new JButton(forwardStepIcon);
        backwardStepButton.setToolTipText("Step Timer Forward");
        backwardStepButton.setActionCommand(TimerStatus.TIMER_STEP_FORWARD);
        backwardStepButton.addActionListener(this);
        jtb.add(backwardStepButton);

        add(jtb);
    }

    public void enableForwardButton(boolean set) {
        forwardButton.setEnabled(set && clockEnabled);
    }

    public void enableBackwardButton(boolean set) {
        backwardButton.setEnabled(set && clockEnabled);
    }
    
    public void setEnableState(boolean set) {
        super.setEnableState(set);
        forwardStepButton.setEnabled(set);
        backwardStepButton.setEnabled(set);
    }
}