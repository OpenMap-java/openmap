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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/TableRowElement.java,v $
// $RCSfile: TableRowElement.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.html;

/** This class implements an entire row of an html table */
public class TableRowElement extends WrapElement implements ContainerElement {

    /** Constuct an empty row */
    public TableRowElement() {
	super("tr", new ListElement());
    }
  
    /** Add a column to the row
     * @param ne add the element wrapped inside a TableDataElement
     * @see TableDataElement */
    public void addElement(Element ne) {
	((ListElement)e).addElement(new TableDataElement(ne));
    }
    
    /** Add a column to the row
     * @param s add the element wrapped inside a TableDataElement
     * @see TableDataElement */
    public void addElement(String s) {
	((ListElement)e).addElement(new TableDataElement(s));
    }
    
    /** Add a column to the row
     * @param c adds the cell to the row (doesn't wrap it) */
    public void addElement(TableCellElement c) {
	((ListElement)e).addElement(c);
    }
}
