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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MultipleSoloMapComponentException.java,v $
// $RCSfile: MultipleSoloMapComponentException.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

/**
 * An Exception indicating that an attempt was made to add a duplicate
 * SoloMapComponent to BeanContext.
 */
public class MultipleSoloMapComponentException extends RuntimeException {

    /**
     * Construct an exception without a reason.
     */
    public MultipleSoloMapComponentException() {
        super();
    }

    /**
     * Construct an exception with a reason string.
     * 
     * @param s the reason for the exception
     */
    public MultipleSoloMapComponentException(String s) {
        super(s);
    }

    /**
     * Construct an exception, generating a reason from the
     * conflicting classes.
     * 
     * @param c1 the class that was being added
     * @param c2 the class that already exists in the BeanContext
     */
    public MultipleSoloMapComponentException(Class c1, Class c2) {
        super("Class " + c1 + " conflicts with Class " + c2);
    }
}