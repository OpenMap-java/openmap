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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/TableRowElement.java,v $
// $RCSfile: TableRowElement.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

/** This class implements an entire row of an html table */
public class TableRowElement extends WrapElement implements ContainerElement {

    /** Construct an empty row */
    public TableRowElement() {
        super("tr", new ListElement());
    }

    /**
     * Add a column to the row
     * 
     * @param ne add the element wrapped inside a TableDataElement
     * @see TableDataElement
     */
    public void addElement(Element ne) {
        ((ListElement) e).addElement(new TableDataElement(ne));
    }

    /**
     * Add a column to the row
     * 
     * @param s add the element wrapped inside a TableDataElement
     * @see TableDataElement
     */
    public void addElement(String s) {
        ((ListElement) e).addElement(new TableDataElement(s));
    }

    /**
     * Add a column to the row
     * 
     * @param c adds the cell to the row (doesn't wrap it)
     */
    public void addElement(TableCellElement c) {
        ((ListElement) e).addElement(c);
    }
}