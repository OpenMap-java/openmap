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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/OMComponent.java,v $
// $RCSfile: OMComponent.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.util.Properties;

/**
 * A OMComponent is an OpenMap component that does two basic things:
 * it is a MapHandlerChilds, so it knows how to use the MapHandler to
 * find other components it needs, and it is a PropertyConsumer, so it
 * can be configured by a set of Properties.  An OMComponent is a
 * perfect candidate to be created by being listed in the
 * openmap.components property of the openmap.properties file.
 */
public class OMComponent extends MapHandlerChild implements PropertyConsumer {

    /**
     * Token uniquely identifying this compoentns in the application
     * properties.
     */
    protected String propertyPrefix = null;

    /**
     * Sets the properties for the OMComponent.
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(java.util.Properties props) {
	setProperties(getPropertyPrefix(), props);
    }

    /**
     * Sets the properties for the OMComponent.
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, java.util.Properties props) {
	setPropertyPrefix(prefix);

//  	String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

    }

    /**
     * PropertyConsumer method, to fill in a Properties object,
     * reflecting the current values of the OMComponent.  If the
     * component has a propertyPrefix set, the property keys should
     * have that prefix plus a separating '.' prepended to each
     * propery key it uses for configuration.
     *
     * @param props a Properties object to load the PropertyConsumer
     * properties into.  If props equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

//  	String prefix = PropUtils.getScopedPropertyPrefix(propertyPrefix);

	return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).  For Layer, this method should at least return the
     * 'prettyName' property.
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer. 
     */
    public Properties getPropertyInfo(Properties list) {
	if (list == null) {
	    list = new Properties();
	}
	return list;
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
     * @param String prefix String.  
     */
    public String getPropertyPrefix() {
	return propertyPrefix;
    }
}
