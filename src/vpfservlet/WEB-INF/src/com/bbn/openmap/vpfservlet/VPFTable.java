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
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/VPFTable.java,v $
// $Revision: 1.1 $ $Date: 2004/01/25 20:04:45 $ $Author: wjeuerle $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.*;
import java.util.*;

import com.bbn.openmap.layer.vpf.*;
import com.bbn.openmap.io.*;

/**
 * Wrapper for DcwRecordFile objects, to give JSPs a way to interact with
 * them directly.
 */
public class VPFTable {
    
    private DcwRecordFile table;

    public VPFTable() {
    }

    public void setFile(String file) {
	try {
	    if ((file == null) && (table != null)) {
	        table.close();
		table = null;
	    }
	    if (file != null) {
	        table = new DcwRecordFile(file);
	    }
	} catch (FormatException fe) {
	}
    }

    public String getTablename() {
	return (table == null) ? "default" : table.getTableName();
    }

    public String getDescription() {
      return (table == null) ? "default" : table.getDescription();
    }
}
