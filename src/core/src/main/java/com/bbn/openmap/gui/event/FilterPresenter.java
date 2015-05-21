//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
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
//$RCSfile: FilterPresenter.java,v $
//$Revision: 1.1 $
//$Date: 2007/08/16 22:15:20 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui.event;

import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author dietrick
 */
public interface FilterPresenter {

    public final static String FILTER_STATE = "FILTER_STATE";
    
    /**
     * @return the pretty name for the presenter, for the GUI.
     */
    public String getName();

    /**
     * @return the JPanel containing filter controls for this display.
     */
    public JPanel getFilterPanel();

    /**
     * @return a hashtable of filters that can be used for this
     *         display. The keys are the Strings for the filters, the
     *         values are Booleans indicating whether events of the
     *         key type are on or off.
     */
    public Hashtable getFilters();
    
    /**
     * @return the list of strings for things that should be shown.
     */
    public List getActiveFilters();

    /**
     * Master control for turning all filters on or off.
     */
    public void resetFilters(Boolean on_off);
    
    public void addPropertyChangeListener(PropertyChangeListener pcl);
    
    public void removePropertyChangeListener(PropertyChangeListener pcl);
}