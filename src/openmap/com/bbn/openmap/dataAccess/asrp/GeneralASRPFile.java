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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/GeneralASRPFile.java,v $
// $RCSfile: GeneralASRPFile.java,v $
// $Revision: 1.1 $
// $Date: 2004/03/04 04:14:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import com.bbn.openmap.dataAccess.iso8211.*;
import com.bbn.openmap.util.Debug;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

public class GeneralASRPFile {

    protected DDFModule info;
    protected Hashtable fields = new Hashtable();

    protected DDFModule load(String fileName) throws IOException {
        info = new DDFModule(fileName);
        return info;
    }

    /**
     * If a field has not been loaded, load it from the DDFRecord if
     * it exists and add it to the master field hashtable.
     */
    protected boolean loadField(DDFRecord record, String tagName, int fieldIndex) {
        if (fields.get(tagName) == null) {
            DDFField ddf = record.findField(tagName, fieldIndex);
            if (ddf != null) {
                fields.put(tagName, ddf);
                ddf.toString();
                return true;
            }
        }
        return false;
    }

    public DDFField getField(String tag) {
        return (DDFField) fields.get(tag);
    }

    public void dumpFields() { 
        for (Iterator it = fields.keySet().iterator(); it.hasNext();) {
            String key = (String)it.next();
            DDFField ddff = (DDFField)fields.get(key);
            Debug.output(ddff.toString());
        }
    }

    protected DDFModule getInfo() {
        return info;
    }

    protected void close() {
        if (info != null) {
            info.close();
        }
    }
    
    public void dump() {
        if (info != null) {
            Debug.output(info.dump());
        }
    }

}