// **********************************************************************
// <copyright>
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/Applyable.java,v $
// $Revision: 1.1 $ $Date: 2004/01/25 20:04:45 $ $Author: wjeuerle $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

/**
 * Applyable interface for use with ApplyIterator.
 */
public interface Applyable {
    /**
     * The apply method
     */
    Object apply(Object obj);
}
