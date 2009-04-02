//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: AAREventSelectionCoordinator.java,v $
//$Revision: 1.1 $
//$Date: 2007/08/16 22:15:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.event;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.logging.Logger;

import com.bbn.openmap.OMComponent;

public class OMEventSelectionCoordinator extends OMComponent {
    
    public static Logger logger = Logger.getLogger("com.bbn.openmap.event.OMEventSelectionCoordinator");
    public final static String EventsSelectedProperty = "eventsSelected";
    
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public OMEventSelectionCoordinator() {
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(EventsSelectedProperty, pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(EventsSelectedProperty, pcl);
    }
    
    public void eventsSelected(List<OMEvent> selectedEvents) {
        pcs.firePropertyChange(EventsSelectedProperty, null, selectedEvents);
    }

}
