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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/LibraryBean.java,v $
// $RCSfile: LibraryBean.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import java.io.Serializable;
import java.util.*;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.layer.util.LayerUtils;

/**
 * A bean to be used for sharing LibrarySelectionTable objects between
 * instances of VPFLayer.
 * <pre>
 *# Assuming that you have a VPF Layer specifying a .libraryBean property
 *# with a value of "VMAPData", you need to specify the following properties:
 *# Required - the java class information
 *VMAPData.class=com.bbn.openmap.layer.vpf.LibraryBean
 *# as in the layer .vpfPath, a ';' separated list of paths to VPF data
 *VMAPData.vpfPath=e:/VMAPLV0
 * </pre>
 *
 * The VMAPData maker name, or whatever other name you decide to name
 * it, has to be added to the openmap.components property list so the
 * LibraryBean will be created and added to the MapHandler.  Of
 * course, you could add the LibraryBean to the MapHandler
 * programmatically if you wanted to.
 */
public class LibraryBean implements PropertyConsumer, Serializable { 

    /** used for explicitly naming a library bean */
    public static final String nameProperty = "name";

    /** property extension used to set the VPF root directory */
    public static final String pathProperty = "vpfPath";

    /** the lst for the path */
    private transient LibrarySelectionTable lst = null;

    /** the name of the bean set in properties, or the marker name */
    String beanName;

    /** used by set/getPropertyPrefix */
    private String propertyPrefix = null;

    /** the paths used in constructing the lst */
    private String[] paths;

    /** 
     * Construct an empty bean.
     */
    public LibraryBean() {
    }

    public void setProperties(Properties setList) {
        setProperties(getPropertyPrefix(), setList);
    }

    public void setProperties(String prefix, Properties setList) {
        setPropertyPrefix(prefix);
        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        paths = LayerUtils.initPathsFromProperties(setList,
                                                   realPrefix + pathProperty);

        String beanName = setList.getProperty(realPrefix + nameProperty);
        this.beanName = (beanName == null) ? prefix : beanName;
            
        if (Debug.debugging("vpf")) {
            Debug.output("LibraryBean.setProperties(): " + prefix + " " +
                     this.beanName + " initialized");
        }
        try {
            if (paths == null) {
                Debug.output("VPF LibraryBean: path not set - expected " +
                             realPrefix + pathProperty + " property");
            } else {
                lst = new LibrarySelectionTable(paths);
            }
        } catch (com.bbn.openmap.io.FormatException f) {
            Debug.output(f.getMessage());
        } catch (NullPointerException npe) {
            Debug.output("LibraryBean.setProperties:" + prefix +
                         ": path name not valid");
        }
    }
    
    /**
     * Gets the name of the component - if the name was explicitly set,
     * then return that, otherwise return the property prefix.
     */
    public String getName() {
        return beanName;
    }

    public Properties getProperties(Properties getList) {
        return new Properties();
    }

    public Properties getPropertyInfo(Properties list) {
        return new Properties();
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     *
     * @return the property prefix
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    /**
     * Returns the LST for the path of this object.
     * @return an LST, null if the object didn't construct properly
     */
    public LibrarySelectionTable getLibrarySelectionTable() {
        return lst;
    }
}
