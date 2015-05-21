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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/TableDataElement.java,v $
// $RCSfile: TableDataElement.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

/** This class is used for a basic cell in a html table */
public class TableDataElement extends WrapElement implements TableCellElement {

    /**
     * Construct a data cell with an element
     * 
     * @param e the element to put in the cell
     */
    public TableDataElement(Element e) {
        super("td", e);
    }

    /**
     * Construct a data cell with a string
     * 
     * @param s the string to put in the cell
     */
    public TableDataElement(String s) {
        super("td", new StringElement(s));
    }

    /**
     * Construct a data cell with an element
     * 
     * @param e the element to put in the cell
     */
    public TableDataElement(String paramString, Element e) {
        super("td", paramString, e);
    }

    /**
     * Construct a data cell with a string
     * 
     * @param s the string to put in the cell
     */
    public TableDataElement(String paramString, String s) {
        super("td", paramString, new StringElement(s));
    }
}