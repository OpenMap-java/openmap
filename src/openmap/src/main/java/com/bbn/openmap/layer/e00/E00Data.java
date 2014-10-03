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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/e00/E00Data.java,v
// $
// $RCSfile: E00Data.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.e00;

public class E00Data {
    int type, valeur, id, valeur2, ID = -1;

    public E00Data(int id) {
        this.id = id;
    }

    public E00Data() {}

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" id:");
        sb.append(id);

        if (ID != -1) {
            sb.append(" ID:");
            sb.append(ID);
        }

        sb.append(" type:");
        sb.append(type);
        sb.append(" value:");
        sb.append(valeur);

        if (valeur != valeur2) {
            sb.append('-');
            sb.append(valeur2);
        }
        sb.append(' ');
        return sb.toString();
    }
}