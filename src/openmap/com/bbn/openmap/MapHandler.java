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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MapHandler.java,v $
// $RCSfile: MapHandler.java,v $
// $Revision: 1.2 $
// $Date: 2003/09/05 15:38:20 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.beans.beancontext.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The MapHandler is an extension of the BeanContextServicesSupport, 
 * with the added capability of being able to set the policy on handling
 * SoloMapComponents.  The MapHandler can be thought of as a generic
 * map container, that contains the MapBean, all the layers that may
 * be part of the map, and all the gesture handling components for
 * that map.  Given that definition, there are some OpenMap components
 * that need to have a one-to-one relationship with a MapHandler.  For
 * instance, a MapHandler should only contain one MapBean,
 * LayerHandler, MouseDelegator and PropertyHandler.  Objects that
 * have this one-to-one relationship with the MapHandler should
 * implement the SoloMapComponent interface, so the MapHandler can
 * control what happens when more than one instance of a particular
 * SoloMapComponent type is added to the MapBean.  Other objects that
 * get added to the MapHandler that hook up with SoloMapComponents
 * should expect that only one version of those components should be
 * used - if another instance gets added to the MapHandler, the
 * objects should disconnect from the SoloMapComponent that they are
 * using, and connect to the new object added.  With this behavior,
 * these objects can gracefully adapt to the SoloMapComponent policies
 * that are set in the MapHandler:
 *
 * <UL>
 *
 * <LI> You can set the MapHandler with the policy to let duplicate
 * SoloMapComponents be added to the MapHandler.  If a duplicate
 * SoloMapComponent is added, then all objects using that type of
 * SoloMapComponent should gracefully use the latest version added.
 *
 * <LI> The MapHandler can have to policy to limit the addition of
 * duplicate SoloMapComponents.  If a duplicate is added, and
 * exception is thrown.  In this case, the original SoloMapComponent
 * must be removed from the MapHandler before the second instance is
 * added.
 *
 * </UL>
 */
public class MapHandler extends BeanContextServicesSupport {

    protected SoloMapComponentPolicy policy;

    public MapHandler() {}

    /**
     * Set the policy of behavior for the MapHandler on how it should
     * act when multiple instances of a certain SoloMapComponents are
     * added to it.
     * @see SoloMapComponentReplacePolicy
     * @see SoloMapComponentRejectPolicy
     */
    public void setPolicy(SoloMapComponentPolicy smcp) {
	policy = smcp;
    }

    /**
     * Get the policy that sets the behavior of the MapHandler when it
     * encounters the situation of multiple instances of a particular
     * SoloMapComponent.
     */
    public SoloMapComponentPolicy getPolicy() {
	if (policy == null) {
	    policy = new SoloMapComponentRejectPolicy();
	}
	return policy;
    }

    /**
     * Add an object to the MapHandler BeanContextSupport.  Uses the
     * current SoloMapComponentPolicy to handle the SoloMapComponents
     * added.  May throw MultipleSoloMapComponentException if the
     * policy is a SoloMapComponentRejectPolicy and the
     * SoloMapComponent is a duplicate type of another component
     * already added.
     * @param smc the map component to nest within this BeanContext.
     * @return true if addition is successful, false if not.
     * @throws MultipleSoloMapComponentException.
     */
    public boolean add(Object obj) {
	boolean passedSoloMapComponentTest = true;
	if (obj instanceof SoloMapComponent) {
	    passedSoloMapComponentTest = getPolicy().canAdd(this, obj);
	} 

	if (obj != null && passedSoloMapComponentTest) {
	    return super.add(obj);
	} else {
	    return false;
	}
    }

    public String toString() {
	return getClass().getName();
    }

    /**
     * Given a class name, find the object in the MapHandler.  If the
     * class is not a SoloMapComponent and there are more than one of
     * them in the MapHandler, you will get the first one found.
     */
    public Object get(String classname) {
	Class someClass = null;
	try {
	    someClass = Class.forName(classname);	    
	} catch (ClassNotFoundException cnfe) {}

	return get(someClass);
    }

    /**
     * Given a Class, find the object in the MapHandler.  If the class
     * is not a SoloMapComponent and there are more than one of them
     * in the MapHandler, you will get the first one found.
     */
    public Object get(Class someClass) {
	Collection collection = getAll(someClass);
	
	Iterator it = collection.iterator();
	while (it.hasNext()) {
	    return it.next();
	}

	return null;
    }

    /**
     * Given a Class name, find all the objects in the MapHandler that
     * are assignment-compatible object of that Class.  A Collection
     * is always returned, although it may be empty.
     */
    public Collection getAll(String classname) {
	Class someClass = null;
	try {
	    someClass = Class.forName(classname);	    
	} catch (ClassNotFoundException cnfe) {}

	return getAll(someClass);
    }

    /**
     * Given a Class, find all the objects in the MapHandler that
     * are assignment-compatible with that Class.  A Collection is always
     * returned, although it may be empty.
     */
    public Collection getAll(Class someClass) {
	Collection collection = new LinkedList();
	
	if (someClass != null) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		Object someObj = it.next();
		if (someClass.isInstance(someObj)) {
		    collection.add(someObj);
		}
	    }
	}
	
	return collection;
    }

}
