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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/SoloMapComponentReplacePolicy.java,v $
// $RCSfile: SoloMapComponentReplacePolicy.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.beans.beancontext.BeanContextSupport;
import java.util.Iterator;

/**
 * A SoloMapComponentPolicy that replaces duplicate SoloMapComponents with
 * the new object.
 */
public class SoloMapComponentReplacePolicy implements SoloMapComponentPolicy {

    public boolean add(BeanContextSupport bc, Object obj) 
	throws MultipleSoloMapComponentException {

	if (obj == null) {
	    return false;
	}

	// At first we just added the new item, but we should remove
	// the previous one, too.
	if (obj instanceof SoloMapComponent) {
	    Class firstClass = obj.getClass();
	    for (Iterator it = bc.iterator(); it.hasNext(); ) {
		Object someObj = it.next();
		if (someObj instanceof SoloMapComponent) {
		    Class secondClass = someObj.getClass();

		    if (firstClass == secondClass ||
			firstClass.isAssignableFrom(secondClass) || 
			secondClass.isAssignableFrom(firstClass)) {
			
			bc.remove(someObj);
			break;
		    }
		}
	    }
	}

	return bc.add(obj);
    }
}
