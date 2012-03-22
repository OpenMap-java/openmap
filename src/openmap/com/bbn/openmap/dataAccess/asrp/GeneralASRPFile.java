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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/GeneralASRPFile.java,v
// $
// $RCSfile: GeneralASRPFile.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.bbn.openmap.dataAccess.iso8211.DDFField;
import com.bbn.openmap.dataAccess.iso8211.DDFModule;
import com.bbn.openmap.dataAccess.iso8211.DDFRecord;
import com.bbn.openmap.util.Debug;

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

    /**
     * Add a field to the field list. If a field already exists in the
     * hashtable, the DDFField is replaced by a list of the fields
     * with the same name. Some types of files need this, like the THF
     * file. Others don't seem to.
     */
    protected void addField(DDFField ddf) {

        String fName = ddf.getFieldDefn().getName().trim().intern();
        if (Debug.debugging("asrp")) {
            Debug.output("GeneralASRPFile.addField(" + fName + ")");
        }

        Object f = fields.get(fName);

        if (f == null) {
            fields.put(fName, ddf);
        } else {
            if (f instanceof List) {
                ((List) f).add(ddf);
            } else {
                Vector subList = new Vector();
                subList.add(f);
                subList.add(ddf);
                fields.put(fName, subList);
            }
        }
    }

    public List getFields(String tag) {
        Object obj = fields.get(tag);
        if (obj instanceof List) {
            return (List) obj;
        } else {
            LinkedList ll = new LinkedList();
            ll.add(obj);
            return ll;
        }
    }

    public DDFField getField(String tag) {
        Object obj = fields.get(tag);
        if (obj instanceof List) {
            return (DDFField) ((List) obj).get(0);
        } else {
            return (DDFField) obj;
        }
    }

    public void dumpFields() {
        for (Iterator it = fields.keySet().iterator(); it.hasNext(); Debug.output(fields.get(it.next())
                .toString())) {
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