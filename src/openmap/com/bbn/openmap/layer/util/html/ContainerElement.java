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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/ContainerElement.java,v $
// $RCSfile: ContainerElement.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.html;

/** This interface is used to describe object which maintain some type of
 * (ordered) list of contained objects.
 * @see Document
 * @see ListElement
 */
public interface ContainerElement extends Element {
    /** add an additional element to the Container
     * @param e the element to add */
    public void addElement(Element e);

    /** add an additional element to the Container
     * @param e the element to add */
    public void addElement(String s);
}
