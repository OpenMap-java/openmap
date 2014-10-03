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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/ListElement.java,v $
// $RCSfile: ListElement.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

import java.util.Enumeration;
import java.util.Vector;

/** A basic html container type */
public class ListElement implements ContainerElement {

    /** An ordered vector of elements */
    protected Vector v = new Vector();

    /** Construct a new ListElement */
    public ListElement() {}

    /**
     * Add an element to the end of the list
     * 
     * @param e the element to add
     */
    public void addElement(Element e) {
        v.addElement(e);
    }

    /**
     * Add an element to the end of the list
     * 
     * @param s the string to add
     */
    public void addElement(String s) {
        addElement(new StringElement(s));
    }

    /**
     * convert representation to html and write it out
     * 
     * @param out the output Writer
     * @exception java.io.IOException an IO error occurred accessing
     *            out
     */
    public void generate(java.io.Writer out) throws java.io.IOException {
        for (Enumeration e = v.elements(); e.hasMoreElements();)
            ((Element) e.nextElement()).generate(out);
    }
}