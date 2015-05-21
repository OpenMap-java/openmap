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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/ContainerElement.java,v $
// $RCSfile: ContainerElement.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

/**
 * This interface is used to describe object which maintain some type
 * of (ordered) list of contained objects.
 * 
 * @see Document
 * @see ListElement
 */
public interface ContainerElement extends Element {
    /**
     * add an additional element to the Container
     * 
     * @param e the element to add
     */
    public void addElement(Element e);

    /**
     * add an additional element to the Container
     * 
     * @param s the String to add
     */
    public void addElement(String s);
}