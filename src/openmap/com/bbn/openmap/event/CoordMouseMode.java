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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/CoordMouseMode.java,v $
// $RCSfile: CoordMouseMode.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.*;
import java.beans.beancontext.*;
import java.util.Iterator;

import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.util.Debug;

/**
 * The CoordMouseMode is an abstract MouseMode extension to
 * AbstractMouseMode that can be used for Modes that want to use the
 * BeanContext to hook up with the InformationDelegator, and to send
 * coordinate updates to be displayed in the infoline.  
 */
public abstract class CoordMouseMode extends AbstractMouseMode {

    /**
     * The info delegator that will display the distance information
     */
    public InformationDelegator infoDelegator = null;

    public CoordMouseMode() {}

    /**
     * @param modeID the id for the mouse mode.
     * @param shouldConsumeEvents the mode setting, where the
     * mousemode should pass the events on to other listeners or not,
     * depending if one of the listeners used it or not.
     */
    public CoordMouseMode(String modeID, boolean shouldConsumeEvents) {
	super(modeID, shouldConsumeEvents);
    }

    /**
     * Set the information delegator.
     * @param id the information delegator that displays the distance
     * values.  
     */
    public void setInfoDelegator(InformationDelegator id) {
	infoDelegator = id;
    }

    /**
     * Return the information delegator.
     */
    public InformationDelegator getInfoDelegator() {
	return infoDelegator;
    }

    /**
     * If the MouseMode has been made inactive, clean out any input
     * that might have been made to the info line.
     */
    public void setActive(boolean active) {
	if (Debug.debugging("mousemode")) {
	    Debug.output("CoordMouseMode: made active (" + 
			 active + ")");
	}
	if (!active && infoDelegator != null) {
	    infoDelegator.requestInfoLine(new InfoDisplayEvent(this, ""));
	}
    }

    public void fireMouseLocation(MouseEvent e) {
	int x = e.getX(); 
	int y = e.getY();
	LatLonPoint llp = null;
	Debug.message("mousemodedetail", "CoordMouseMode: firing mouse location");
	
	if (infoDelegator != null) {
	    if (e.getSource() instanceof MapBean) {
		llp = ((MapBean)e.getSource()).getProjection().inverse(x, y);
	    }	
	    String infoLine;
	    infoLine = createCoordinateInformationLine(x, y, llp);
	    
	    // setup the info event
	    InfoDisplayEvent info = new InfoDisplayEvent(this, infoLine);
	    // ask the infoDelegator to display the info
	    infoDelegator.requestInfoLine(info);
	}
    }

    /**
     * Method to create the information string reflecting information
     * at the LatLonPoint provided.  By default, will return a string
     * for the x and y, and the lat/lon.  If llp is null, just the x,
     * y will be returned.  This method can be changed, or overridden
     * to change what kind of coordinates (UTM, DMS, MGRS) are
     * reflected here.
     */
    protected String createCoordinateInformationLine(int x, int y, LatLonPoint llp) {
	if (llp != null) {
	    return "Lat, Lon (" + 
		df.format(llp.getLatitude()) +
		", " + df.format(llp.getLongitude())  + 
		") - x, y (" + x + "," + y + ")";
	} else {
	    return "x, y (" + x + "," + y + ")";
	}
    }

    /**
     * Called when a CoordMouseMode is added to a BeanContext, or when
     * another object is added to the BeanContext after that.  The
     * CoordMouseMode looks for an InformationDelegator to use to fire
     * the coordinate updates.  If another InforationDelegator is
     * added when one is already set, the later one will replace the
     * current one.
     *
     * @param someObj an object being added to the BeanContext.
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof InformationDelegator) {
	    Debug.message("mousemode", "NavMouseMode: found InformationDelegator");
	    setInfoDelegator((InformationDelegator)someObj);
	}
    }

    /** 
     * BeanContextMembershipListener method.  Called when objects have
     * been removed from the parent BeanContext.  If an
     * InformationDelegator is removed from the BeanContext, and it's
     * the same one that is currently held, it will be removed.
     *
     * @param someObj an object being removed from the BeanContext.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof InformationDelegator) {
	    if (getInfoDelegator() == (InformationDelegator) someObj) {
		setInfoDelegator(null);
	    }
	}
    }
}

