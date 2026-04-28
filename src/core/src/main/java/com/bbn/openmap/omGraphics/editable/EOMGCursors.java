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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/EOMGCursors.java,v
// $
// $RCSfile: EOMGCursors.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;

import com.bbn.openmap.util.Debug;

public class EOMGCursors {

    public final static Cursor DEFAULT = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    // public final static Cursor EDIT = create("edit.gif", new
    // Point(0, 0), "EOMG_EDIT");
    // public final static Cursor PUTNODE = create("putnode.gif", new
    // Point(0, 0), "EOMG_PUTNODE");
    // public final static Cursor MOVE = create("move.gif", new
    // Point(8, 8), "EOMG_MOVE");

    // public final static Cursor EDIT =
    // Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    // public final static Cursor PUTNODE =
    // Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    // public final static Cursor MOVE =
    // Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    // public final static Cursor GRAB =
    // Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    public final static Cursor EDIT = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    public final static Cursor PUTNODE = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    public final static Cursor MOVE = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    public final static Cursor GRAB = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

    public EOMGCursors() {
    }

    public static Cursor create(String resource, Point hotspot, String name) {
        Cursor cursor = DEFAULT;
        try {

            URL url = (new EOMGCursors()).getClass().getResource(resource);
            ImageIcon image = new ImageIcon(url);
            cursor = Toolkit.getDefaultToolkit().createCustomCursor(image.getImage(), hotspot, name);

        } catch (IndexOutOfBoundsException ioobe) {
            Debug.error("LineStateMachine creating cursor:\n " + ioobe.getMessage());
        }
        return cursor;
    }
}