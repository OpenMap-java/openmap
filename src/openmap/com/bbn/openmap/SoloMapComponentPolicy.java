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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/SoloMapComponentPolicy.java,v $
// $RCSfile: SoloMapComponentPolicy.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.beans.beancontext.BeanContextSupport;

/**
 * An interface to control the behavior of the BeanContext when 
 * duplicate SoloMapComponents are added to it.
 */
public interface SoloMapComponentPolicy {

    /**
     * Add an object to the context.
     * @param bc the context to add to
     * @param obj the object to add
     */
    public boolean add(BeanContextSupport bc, Object obj) 
	throws MultipleSoloMapComponentException;

}
