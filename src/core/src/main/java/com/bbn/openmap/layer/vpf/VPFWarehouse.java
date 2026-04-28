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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFWarehouse.java,v $
// $RCSfile: VPFWarehouse.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.Component;
import java.util.List;

/**
 * Define an interface for a Graphic Factory for graphics read from
 * VPF.
 */
public interface VPFWarehouse {

    /**
     * Return true if we may draw some edge features.
     */
    public boolean drawEdgeFeatures();

    /**
     * Return true if we may draw some text features.
     */
    public boolean drawTextFeatures();

    /**
     * Return true if we may draw some area features.
     */
    public boolean drawAreaFeatures();

    /**
     * To let the warehouse know that a new CoverageAttributeTable
     * will be using the warehouse. Some warehouses need to reset
     * some tables for this situation.
     */
    public void resetForCAT();

    /**
     * Get the GUI to control different aspects of the warehouse.
     * 
     * @param lst LibrarySelectionTable to use to get information
     *        about the data, if needed.
     */
    public Component getGUI(LibrarySelectionTable lst);

    /**
     * Return true if we may draw some entity node(point) features.
     */
    public boolean drawEPointFeatures();

    /**
     * Return true if we may draw some connected node(point) features.
     */
    public boolean drawCPointFeatures();

    /**
     * Get a List of Strings listing all the feature types wanted.
     * Returned with the area features first, then text features, then
     * line features, then point features.
     */
    public List<String> getFeatures();

    /**
     * Get a library name to limit selections from. Used by the
     * LibrarySelectionTable to find out if selection from database
     * should be limitied to tiles or feature from certain libraries.
     * Specified here instead of the LibrarySelectionTable in case the
     * LST is shared among layers, since the warehouse is specific to
     * a particular layer.
     */
    public List<String> getUseLibraries();

    /**
     * Set a library name to limit selections from. Used by the
     * LibrarySelectionTable to find out if selection from database
     * should be limitied to tiles or feature from certain libraries.
     * Specified here instead of the LibrarySelectionTable in case the
     * LST is shared among layers, since the warehouse is specific to
     * a particular layer. If null the warehouse should use all
     * libraries available to it to gather features.
     */
    public void setUseLibraries(List<String> lib);
    
    /**
     * Utility method to check if the specified library name has been set by the
     * configuration as one to use.
     * 
     * @param libName the library name to test
     * @return true if the useLibrary list has not been set, is empty, or if the
     *         provided name matches an entry on it.
     */
    public boolean checkLibraryForUsage(String libName);
}