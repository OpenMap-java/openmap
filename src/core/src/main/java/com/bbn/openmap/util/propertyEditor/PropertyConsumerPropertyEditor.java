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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/PropertyConsumerPropertyEditor.java,v $
// $RCSfile: PropertyConsumerPropertyEditor.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.beans.PropertyEditorSupport;
import java.util.Properties;

import com.bbn.openmap.PropertyConsumer;

/**
 * The base class for property editors that can use properties to
 * provide a more complex interface, and need more flexibility to
 * contribute different parameters. The PropertyEditor's
 * PropertyConsumer methods are called by the Inspector at particular
 * times. The setProperties method is called to configure the
 * PropertyConsumerPropertyEdtior. The prefix used in the
 * setProperties method is the limited scope of just the property name
 * being defined and/or adjusted. Any other properties defined need to
 * also be defined at this level, with the property prefix used as a
 * base. The getProperties() method will be called by the Inspector as
 * a way for this property editor to provide more properties as a
 * result of configuration.
 */
public abstract class PropertyConsumerPropertyEditor extends
        PropertyEditorSupport implements PropertyConsumer {

    protected String propertyPrefix = null;

    public PropertyConsumerPropertyEditor() {}

    /**
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(Properties props) {
        setProperties(getPropertyPrefix(), props);
    }

    /**
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties props) {}

    /**
     * PropertyConsumer method.
     * 
     * @param props a Properties object to load the PropertyConsumer
     *        properties into. If props equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer. The key
     * for each property should be the raw property name (without a
     * prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.). It's not really defined what this method call should be
     * returning.
     * 
     * @param list a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }

        return list;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer. The prefix, along with a '.', should be
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
     * @return the property prefix.
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }
}