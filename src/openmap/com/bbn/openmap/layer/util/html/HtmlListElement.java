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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/HtmlListElement.java,v $
// $RCSfile: HtmlListElement.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/23 20:43:31 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.html;
import java.util.*;

/** A container for a list body */
public class HtmlListElement implements ContainerElement {
    private Element header;
    private ListBodyElement items = new ListBodyElement();

    /** Construct a new ListElement */
    public HtmlListElement() {
	this((Element)null);
    }
    public HtmlListElement(String s) {
	this(new StringElement(s));
    }
    public HtmlListElement(Element header) {
	this.header = header;
    }
    
    public void setTitleElement(Element e) {
	header = e;
    }

    public void setTitleElement(String s) {
	setTitleElement(new StringElement(s));
    }

    public void generate (java.io.Writer out) throws java.io.IOException {
	if (header != null) {
	    header.generate(out);
	}
	new WrapElement("ul", items).generate(out);
    }
    
    /** Add an element to the end of the list
     * @param e the element to add */
    public void addElement(Element e) {
	items.addElement(e);
    }
    /** Add an element to the end of the list
     * @param s the string to add */
    public void addElement(String s) {
	addElement(new StringElement(s));
    }
}
