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
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/JoinRowMaker.java,v $
// $Revision: 1.2 $ $Date: 2004/01/26 18:18:16 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.File;
import java.util.*;
import javax.servlet.http.*;

import com.bbn.openmap.layer.util.html.*;
import com.bbn.openmap.layer.vpf.*;
import com.bbn.openmap.io.FormatException;

/**
 * A RowMaker subclass that handles simple joins between tables.
 */
public class JoinRowMaker extends PlainRowMaker {
    /** the column that contains the foreign key to the primitive table */
    final int theColumn;
    /** the column that contains the tile identifier */
    final int tileColumn;
    /** a list reused to load primitive rows */
    final List jtrow = new ArrayList();

    /** the utility class that understands tiled and untiled data */
    final TileHolder tiler;
    final TilingAdapter ta;
    public JoinRowMaker(DcwRecordFile table, String joinColumnName,
                        String tableName, boolean isTiled) throws FormatException {
        theColumn = table.whatColumn(joinColumnName);
        tileColumn = isTiled ? table.whatColumn(FeatureClassInfo.TILE_ID_COLUMN_NAME) : -1;
        ta = table.getTilingAdapter(tileColumn, theColumn);
        tiler = new TileHolder(new File(table.getTableFile()).getParentFile(),
                               tableName, isTiled);
    }

    public void addToRow(TableRowElement row, List l) {
        int i = 0;
        for (Iterator li = l.iterator(); li.hasNext(); ) {
            Object elt = li.next();
            if (i == theColumn) {
                int whatrow = ta.getTilePrimId(l);
                int tileId = ta.getTileId(l);
                try {
                    if (tiler.getRow(ta, l, jtrow)) {
                        for (Iterator it = jtrow.iterator(); it.hasNext(); ) {
                            row.addElement(new TableDataElement("CLASS=JoinColumn",
                                                                it.next().toString()));
                        }
                    } else {
                        row.addElement("Join failed! ["+elt+"]"+ "(" + tileId + "," + whatrow + ")");
                    }
                } catch (FormatException fe) {
                    row.addElement(fe.toString() + "(" + tileId + "," + whatrow + ")");
                }
            } else {
                row.addElement(elt.toString());
            }
            i++;
        }
    }

    public void close() {
        tiler.close();
    }
}

