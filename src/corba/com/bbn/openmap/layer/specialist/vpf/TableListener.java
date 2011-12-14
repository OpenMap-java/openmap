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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/vpf/TableListener.java,v $
// $RCSfile: TableListener.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.vpf;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.bbn.openmap.layer.vpf.DcwColumnInfo;
import com.bbn.openmap.layer.vpf.DcwRecordFile;
import com.bbn.openmap.util.html.Document;
import com.bbn.openmap.util.html.HeaderElement;
import com.bbn.openmap.util.html.StringElement;
import com.bbn.openmap.util.html.TableHeaderElement;
import com.bbn.openmap.util.html.TableRowElement;
import com.bbn.openmap.util.http.HttpRequestEvent;
import com.bbn.openmap.util.http.HttpRequestListener;
/**
 * An HttpRequestListener that returns schema and data from a VPF
 * table
 */
public class TableListener implements HttpRequestListener {

    public TableListener() {}

    /** Just write the request out to the client. */
    public void httpRequest(HttpRequestEvent e) throws IOException {
        String s = e.getRequest();
        System.out.println(s);
        Document d = new Document(s);
        d.addElement(new HeaderElement(1, s));
        try {
            DcwRecordFile f = new DcwRecordFile(s);
            d.addElement(new HeaderElement(3, "Table Description Schema"));
            d.addElement(new StringElement("<table border>"));
            DcwColumnInfo dci[] = f.getColumnInfo();
            TableRowElement t = new TableRowElement();
            d.addElement(t);
            t.addElement(new TableHeaderElement("Column Name"));
            t.addElement(new TableHeaderElement("Field Type"));
            t.addElement(new TableHeaderElement("Number Of Elements"));
            t.addElement(new TableHeaderElement("Key Type"));
            t.addElement(new TableHeaderElement("Column Description"));
            t.addElement(new TableHeaderElement("Value Description Table"));
            t.addElement(new TableHeaderElement("Thematic Index Name"));
            t.addElement(new TableHeaderElement("Narrative Table"));
            TableRowElement rw = new TableRowElement();
            for (int i = 0; i < dci.length; i++) {
                TableRowElement row = new TableRowElement();
                d.addElement(row);
                row.addElement(dci[i].getColumnName());
                char munge[] = dci[i].getColumnName().toCharArray();
                for (int j = 0; j < munge.length; j++)
                    if (munge[j] == '_')
                        munge[j] = ' ';
                rw.addElement(new TableHeaderElement(new String(munge)));
                row.addElement(new Character(dci[i].getFieldType()).toString());
                row.addElement(Integer.toString(dci[i].getNumberOfElements()));
                row.addElement(new Character(dci[i].getKeyType()).toString());
                row.addElement(dci[i].getColumnDescription());
                row.addElement(dci[i].getValueDescriptionTable());
                row.addElement(dci[i].getThematicIndexName());
                row.addElement(dci[i].getNarrativeTable());
            }
            d.addElement(new StringElement("</table>"));
            d.addElement(new HeaderElement(3, "Table Data"));
            d.addElement(new StringElement("<table border>"));
            d.addElement(rw);
            List colhdr;
            while ((colhdr = f.parseRow()) != null) {
                d.addElement(listAsTableRow(colhdr));
            }
            d.addElement(new StringElement("</table>"));
            f.close();
        } catch (com.bbn.openmap.io.FormatException f) {
            d.addElement(new StringElement(f.getMessage()));
        }
        d.generate(e.getWriter());
    }

    private TableRowElement listAsTableRow(List v) {
        TableRowElement rv = new TableRowElement();
        Iterator it = v.iterator();
        while (it.hasNext()) {
            rv.addElement(it.next().toString());
        }
        return rv;
    }
}