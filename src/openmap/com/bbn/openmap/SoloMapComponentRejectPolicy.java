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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/SoloMapComponentRejectPolicy.java,v $
// $RCSfile: SoloMapComponentRejectPolicy.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.beans.beancontext.BeanContextSupport;
import java.util.Iterator;

/**
 * A SoloMapComponentPolicy that rejects attempts to add a duplicate
 * SoloMapComponent to the BeanContext.
 */
public class SoloMapComponentRejectPolicy implements SoloMapComponentPolicy {

    /**
     * @throws a MultipleSoloMapComponentException if a duplicate
     *         instance of SoloMapComponent exists.
     * @return true if the object can be added to the MapHandler.
     */
    public boolean canAdd(BeanContextSupport bc, Object obj)
            throws MultipleSoloMapComponentException {
        if (obj instanceof SoloMapComponent) {
            Class firstClass = obj.getClass();
            for (Iterator it = bc.iterator(); it.hasNext();) {
                Object someObj = it.next();
                if (someObj instanceof SoloMapComponent) {
                    Class secondClass = someObj.getClass();

                    if (firstClass == secondClass
                            || firstClass.isAssignableFrom(secondClass)
                            || secondClass.isAssignableFrom(firstClass)) {

                        throw new MultipleSoloMapComponentException(firstClass, secondClass);
                    }
                }
            }
        }
        return true;
    }
}