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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/time/TimeSliderSupport.java,v $
// $RCSfile: TimeSliderSupport.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:30:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import com.bbn.openmap.gui.time.RealTimeHandler;

/**
 * Needs to be overridden to handle the fact that the time slider is
 * in seconds, not millis.
 */
public class TimeSliderSupport extends
        com.bbn.openmap.gui.time.TimeSliderSupport {

    public TimeSliderSupport(JSlider slider, RealTimeHandler rth,
            long startingTime, long endingTime) {
        super(slider, rth, startingTime, endingTime);
    }

    /**
     * returns super.getTime() * 1000, returning the value to millis.
     */
    protected long getTime() {
        return super.getTime() * 1000;
    }
    
    /**
     * ChangeListener method, sets the time on the RealTimeHandler
     * based on the current setting on the slider.
     */
    public void stateChanged(ChangeEvent ce) {
        if (ce.getSource() == timeSlider && timeSlider.getValueIsAdjusting()) {
            rtHandler.setTime(getTime());
        }
    }


}