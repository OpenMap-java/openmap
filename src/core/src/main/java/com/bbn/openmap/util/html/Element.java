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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/Element.java,v $
// $RCSfile: Element.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

/**
 * This interface describes any html element that knows how to dump
 * itself to an java.io.Writer
 */
public interface Element {
    /**
     * convert representation to html and write it out
     * 
     * @param out the output Writer
     * @exception java.io.IOException an IO error occurred accessing
     *            out
     */
    public void generate(java.io.Writer out) throws java.io.IOException;
}