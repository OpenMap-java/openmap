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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/SoloMapComponentPolicy.java,v $
// $RCSfile: SoloMapComponentPolicy.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:40 $
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
     * Ask whether an object can be added to the to the context.
     * 
     * @param bc the context to add to
     * @param obj the object to add
     * @return true if the object can be added.
     * @throws MultipleSoloMapComponentException describing the
     *         conflict if there is one and the object can't be added.
     */
    public boolean canAdd(BeanContextSupport bc, Object obj)
            throws MultipleSoloMapComponentException;

}