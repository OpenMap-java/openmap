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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/ListBodyElement.java,v $
// $RCSfile: ListBodyElement.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:11 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.html;
import java.util.*;

/** A container for a list body */
public class ListBodyElement extends ListElement {
    
    /** Construct a new ListElement */
    public ListBodyElement() {
    }
    
    /** Add an element to the end of the list
     * @param e the element to add */
    public void addElement(Element e) {
        super.addElement(new WrapElement("li", e));
    }
}
