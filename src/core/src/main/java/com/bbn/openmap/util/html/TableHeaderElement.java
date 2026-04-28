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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/TableHeaderElement.java,v $
// $RCSfile: TableHeaderElement.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

/** This class is used for Column/Row head elements in html tables */
public class TableHeaderElement extends WrapElement implements TableCellElement {
    /**
     * Construct a column header with an element
     * 
     * @param e the element to put in the cell
     */
    public TableHeaderElement(Element e) {
        super("th", e);
    }

    /**
     * Construct a column header with a string
     * 
     * @param s the string to put in the cell
     */
    public TableHeaderElement(String s) {
        super("th", new StringElement(s));
    }

    /**
     * Construct a column header with an element
     * 
     * @param e the element to put in the cell
     */
    public TableHeaderElement(String paramString, Element e) {
        super("th", paramString, e);
    }

    /**
     * Construct a column header with a string
     * 
     * @param s the string to put in the cell
     */
    public TableHeaderElement(String paramString, String s) {
        super("th", paramString, new StringElement(s));
    }
}