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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/ChoiceList.java,v
// $
// $RCSfile: ChoiceList.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.graphicLoader.netmap;

import java.util.Vector;

public class ChoiceList extends Vector {

    public ChoiceList() {
        super();
    }

    public void add(String label, Object value) {
        ChoiceItem it = null;

        if ((it = this.get(label)) != null) {
            it.set(value);
            return;
        }

        it = new ChoiceItem(label, value);
        super.addElement((Object)it);
    }

    public String labelAt(int index) {
        if (index >= this.size()) {
            return null;
        }

        return (((ChoiceItem)super.elementAt(index)).label());
    }

    public Object valueAt(int index) {
        if (index >= this.size()) {
            return null;
        }

        return (((ChoiceItem)super.elementAt(index)).value());
    }

    public ChoiceItem get(String label) {
        for (int i = 0; i < this.size(); i++) {
            ChoiceItem item = (ChoiceItem)super.elementAt(i);

            if (item.label().equals(label)) {
                return item;
            }
        }

        return null;
    }
}
