// **********************************************************************
// <copyright>
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/VDTRowMaker.java,v $
// $Revision: 1.2 $ $Date: 2004/01/26 18:18:16 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.util.*;
import javax.servlet.http.*;

import com.bbn.openmap.layer.util.html.TableRowElement;
import com.bbn.openmap.layer.vpf.*;

/**
 * A RowMaker class for int.vdt and char.vdt tables.  It generates
 * a URL for columns that reference another table.
 */
public class VDTRowMaker extends ReferenceRowMaker {

    /** the path to the coverage */
    final String basepath;
    /** the column with the table name */
    final int tableCol;

    public VDTRowMaker(HttpServletRequest request,
                       HttpServletResponse response,
                       String basepath, DcwRecordFile drf) {
        super(request, response);
        this.basepath = basepath;
        tableCol = drf.whatColumn("table");
    }
    
    public void addToRow(TableRowElement row, List l) {
        int i = 0;
        for (Iterator li = l.iterator(); li.hasNext(); ) {
            Object elt = li.next();
            if (i == tableCol) {
                row.addElement(fileURL(basepath, ((String)elt).toLowerCase()));
            } else {
                row.addElement(elt.toString());
            }
            i++;
        }
    }
}
