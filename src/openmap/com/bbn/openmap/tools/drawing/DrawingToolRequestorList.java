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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/DrawingToolRequestorList.java,v
// $
// $RCSfile: DrawingToolRequestorList.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import java.util.Hashtable;
import java.util.Iterator;

import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

public class DrawingToolRequestorList implements DrawingToolRequestor {

    protected String name;
    protected Hashtable table;

    public DrawingToolRequestorList() {
        table = new Hashtable();
    }

    public void add(OMGraphic omg, DrawingToolRequestor dtr) {
        if (Debug.debugging("drawingtool")) {
            Debug.output("DTRL.add(" + omg.getClass().getName() + ")");
        }
        table.put(omg, dtr);
    }

    public void remove(OMGraphic omg) {
        table.remove(omg);
    }

    public void clear() {
        table.clear();
    }

    /**
     * The method where a graphic, and an action to take on the
     * graphic, arrives.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
        DrawingToolRequestor dtr;
        if (omg instanceof OMGraphicList) {
            if (Debug.debugging("drawingtool")) {
                Debug.output("DTRL.drawingComplete(list)");
            }

            for (Iterator it = ((OMGraphicList) omg).iterator(); it.hasNext();) {
                OMGraphic omgi = (OMGraphic) it.next();
                dtr = (DrawingToolRequestor) table.get(omgi);
                if (dtr != null) {
                    if (Debug.debugging("drawingtool")) {
                        Debug.output("  notifying requestor for list member "
                                + omgi.getClass().getName());
                    }
                    dtr.drawingComplete(omgi, action);
                }
            }
        } else {
            dtr = (DrawingToolRequestor) table.get(omg);
            if (dtr != null) {
                if (Debug.debugging("drawingtool")) {
                    Debug.output("  notifying requestor for "
                            + omg.getClass().getName());
                }
                dtr.drawingComplete(omg, action);
            }
        }

        if (Debug.debugging("drawingtool")) {
            Debug.output("DTRL.drawingComplete complete");
        }
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * Needed to fill in a GUI with a receiver's name, to enable the
     * user to send a graphic to a specific object. Should be a pretty
     * name, suitable to let a user know what it is. It's important
     * that the requestor have a name, because that could be the key
     * that is used in some GUI components.
     */
    public String getName() {
        return name;
    }

}