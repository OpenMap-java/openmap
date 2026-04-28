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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/ChoiceItem.java,v
// $
// $RCSfile: ChoiceItem.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

public class ChoiceItem {

    private String label = null;
    private Object value = null;

    public ChoiceItem() {
        label = null;
        value = null;
    }

    public ChoiceItem(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public Object value() {
        return this.value;
    }

    public String label() {
        return this.label;
    }

    public void set(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public void set(Object value) {
        this.value = value;
    }
}