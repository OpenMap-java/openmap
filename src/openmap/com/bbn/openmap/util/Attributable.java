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
//$RCSfile: Attributable.java,v $
//$Revision: 1.1 $
//$Date: 2007/08/16 22:15:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.util;

import java.util.Map;

/**
 * An object that has attributes.
 * 
 * @author dietrick
 */
public interface Attributable {

    /**
     * Put an attribute in the object, the value can be retrieved later via the
     * key.
     * 
     * @param key
     * @param value
     */
    public void putAttribute(Object key, Object value);

    /**
     * Get the attribute stored under a key.
     * 
     * @param key
     * @return value if found, null if not.
     */
    public Object getAttribute(Object key);

    /**
     * Tells the object to clear all of its attributes.
     */
    public void clearAttributes();

    /**
     * Convenience method to allow super class methods access to an attribute
     * table if they are acting as a wrapper.
     * 
     * @return Map of all attributes
     */
    public Map<?, ?> getAttributes();
    
    /**
     * Convenience method to allow super class methods access to an attribute
     * table if they are acting as a wrapper.
     * 
     * @param map Map of all attributes
     */
    public void setAttributes(Map<?, ?> map);
}
