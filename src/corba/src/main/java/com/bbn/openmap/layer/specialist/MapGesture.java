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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/MapGesture.java,v $
// $RCSfile: MapGesture.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:08:58 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

/** MapGesture - class for handling specialist gestures in OpenMap */
public class MapGesture {

    /** selectionDistance - default selectionDistance. */
    public static final int selectionDistance = 4;
    /**
     * projection to use for point - lat/lon translation. Set and used
     * as a read only.
     */
    protected Projection projection = null;

    // The OM version of ActionTypes is set here.
    public static final int NoAction = -1;
    public static final int UpdateGraphics = com.bbn.openmap.corba.CSpecialist.ActionType._UpdateGraphics;
    public static final int UpdatePalette = com.bbn.openmap.corba.CSpecialist.ActionType._UpdatePalette;
    public static final int InfoText = com.bbn.openmap.corba.CSpecialist.ActionType._InfoText;
    public static final int PlainText = com.bbn.openmap.corba.CSpecialist.ActionType._PlainText;
    public static final int HTMLText = com.bbn.openmap.corba.CSpecialist.ActionType._HTMLText;
    public static final int URL = com.bbn.openmap.corba.CSpecialist.ActionType._URL;

    public static final int clickEvent = com.bbn.openmap.corba.CSpecialist.MouseType._ClickEvent;
    public static final int motionEvent = com.bbn.openmap.corba.CSpecialist.MouseType._MotionEvent;
    public static final int keyEvent = com.bbn.openmap.corba.CSpecialist.MouseType._KeyEvent;

    // gesture modes
    public static final short Raw = 1;
    public static final short Cooked = 2;// HACK: cooked modes
    // unimplemented
    public static final short Burnt = 3;
    public static final short Charcoal = 4;
    private short mode = Raw;

    // outgoing gesture information (sent to specialist)
    public Point point = null;
    public LatLonPoint llpoint = null;
    public short mousebutton = 0;
    public boolean press = false;
    public boolean alt = false;
    public boolean shift = false;
    public boolean control = false;
    public boolean meta = false;
    public char key = 0;
    public int event_type = -1;

    // incoming gesture information (sent from specialist)
    public int[] actionType = null; // matches CSpecialist.ActionType
    public String text = null;
    public String info = null;
    public String url = null;

    public short getMode() {
        return mode;
    }

    public void setMode(short m) {
        mode = Raw;// HACK
    }

    public void setProjection(Projection proj) {
        projection = proj;
    }

    public Projection getProjection() {
        return projection;
    }

    public MapGesture() {}

    public void setMouseEvent(MouseEvent me, int eventType, boolean MouseDown) {
        if (me != null) {
            point = me.getPoint();
            shift = me.isShiftDown();
            control = me.isControlDown();
            meta = me.isMetaDown();
            alt = me.isAltDown();

            if (projection != null) {
                if (llpoint == null) {
                    llpoint = new LatLonPoint.Double();
                }
                projection.inverse(point, llpoint);
            } else {
                llpoint = new LatLonPoint.Double(0f, 0f);
            }
        }

        press = MouseDown;
        event_type = eventType;
    }

    /**
     * determineGesture() - determines what type of cooked mode
     * gesture occurred for specialist layer(s).
     */
    public static void determineGesture(MouseEvent event) {}
}

/**
 * GestureRecord - we may want to be smart about sending gestures to
 * the specialist in the future. In particular we should implement all
 * the high-level gesture handling on the OpenMap side of things
 * instead of the specialist/libCspec side of things. Which means
 * changes to the IDL.
 */

// class GestureRecord {
// }
