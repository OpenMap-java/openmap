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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/PropertyConsumer.java,v $
// $RCSfile: PropertyConsumer.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.util.Properties;

/**
 * A PropertyConsumer is an interface defining an object that is
 * configured by a Properties object, containing property keys and
 * values that is expected by it.  
 */
public interface PropertyConsumer {

    /**
     *  Key in the associated propertyInfo object.  Holds a list of
     *  property names, which should be displayed and editable when
     *  configuring a PropertyConsumer object interatively.  List is
     *  space seperated and the order is the order in which the
     *  properties will appear.  
     */
    public static final String initPropertiesProperty = "initProperties";
    
    /**
     * Keyword for PropertyEditor class from PropertyInfo
     * Property object.
     */
    public static final String EditorProperty = "editor";
    public static final String ScopedEditorProperty = ".editor";

    /**
     * Method to set the properties in the PropertyConsumer.  It is
     * assumed that the properties do not have a prefix associated
     * with them, or that the prefix has already been set.
     *
     * @param setList a properties object that the PropertyConsumer
     * can use to retrieve expected properties it can use for
     * configuration.
     */
    public void setProperties(Properties setList);

    /**
     * Method to set the properties in the PropertyConsumer.  The
     * prefix is a string that should be prepended to each property
     * key (in addition to a separating '.') in order for the
     * PropertyConsumer to uniquely identify properties meant for it, in
     * the midst of of Properties meant for several objects.
     *
     * @param prefix a String used by the PropertyConsumer to prepend
     * to each property value it wants to look up -
     * setList.getProperty(prefix.propertyKey).  If the prefix had
     * already been set, then the prefix passed in should replace that
     * previous value.
     * @param setList a Properties object that the PropertyConsumer
     * can use to retrieve expected properties it can use for
     * configuration.  
     */
    public void setProperties(String prefix, Properties setList);

    /**
     * Method to fill in a Properties object, reflecting the current
     * values of the PropertyConsumer.  If the PropertyConsumer has a
     * prefix set, the property keys should have that prefix plus a
     * separating '.' prepended to each property key it uses for
     * configuration. 
     *
     * @param getList a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getProperties(Properties getList);

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     *
     * @param getList a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.  
     */
    public Properties getPropertyInfo(Properties list);

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix);

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     *
     * @param String prefix String.  
     */
    public String getPropertyPrefix();

}
