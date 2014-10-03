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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/LightMapHandlerChild.java,v $
// $RCSfile: LightMapHandlerChild.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

/**
 * The LightMapHandlerChild is an interface for an object that can be
 * managed by a MapHandlerChild, and notified of changes in the
 * MapHandler via that managing object. The two methods are intended
 * to called when the LightMapHandlerChild should be given an
 * opportunity to connect to an object or disconnect from an object.
 * The LightMapHandlerChild doesn't have to do anything, this
 * interface is really for the benefit of the managing MapHandlerChild
 * to figure out if some of its child components may want to know
 * about MapHandler events.
 */
public interface LightMapHandlerChild {

    public void findAndInit(Object someObj);

    public void findAndUndo(Object someObj);

}