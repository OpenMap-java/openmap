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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/StandardMapMouseInterpreter.java,v $
// $RCSfile: StandardMapMouseInterpreter.java,v $
// $Revision: 1.18 $
// $Date: 2007/10/01 21:43:38 $
// $Author: epgordon $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

/**
 * The StandardMapMouseInterpreter is a basic implementation of the
 * MapMouseInterpreter, working with an OMGraphicHandlerLayer to handle
 * MouseEvents on it. This class allows the OMGraphicHandlerLayer, which
 * implements the GestureResponsePolicy, to not have to deal with MouseEvents
 * and the OMGraphicList, but to just react to the meanings of the user's
 * gestures.
 * <p>
 * 
 * The StandardMapMouseInterpreter uses highlighting to indicate that mouse
 * movement is occurring over an OMGraphic, and gives the layer three ways to
 * react to that movement. After finding out if the OMGraphic is highlightable,
 * the SMMI will tell the layer to highlight the OMGraphic (which usually means
 * to call select() on it), provide a tool tip string for the OMGraphic, and
 * provide a string to use on the InformationDelegator info line. The layer can
 * reply or ignore any and all of these notifications, depending on how it's
 * supposed to act.
 * <p>
 * 
 * For left mouse clicks, the SMMI uses selection as a notification that the
 * user is choosing an OMGraphic, and that the OMGraphic should be prepared to
 * be moved, modified or deleted. For a single OMGraphic, this is usually
 * handled by handing the OMGraphic off to the OMDrawingTool. However the
 * GestureResponsPolicy handles the situation where the selection is of multiple
 * OMGraphics, and the layer should prepare to handle those situations as
 * movement or deletion notifications. This usually means to change the
 * OMGraphic's display to indicate that the OMGraphics have been selected.
 * Selection notifications can come in series, and the GestureResponsePolicy is
 * expected to keep track of which OMGraphics it has been told are selected.
 * Deselection notifications may come as well, or other action notifications
 * such as cut or copy may arrive. For cut and copy notifications, the
 * OMGraphics should be removed from any selection list. For pastings, the
 * OMGraphics should be added to the selection list.
 * <p>
 * 
 * For right mouse clicks, the layer will be provided with a JPopupMenu to use
 * to populate with options for actions over a OMGraphic or over the map.
 * <p>
 * 
 * The StandardMapMouseInterpreter uses a timer to pace how mouse movement
 * actions are responded to. Highlight reactions only occur after the mouse has
 * paused over the map for the timer interval, so the application doesn't try to
 * respond to constantly changing mouse locations. You can disable this delay by
 * setting the timer interval to zero.
 */
public class DBLClickSelectMapMouseInterpreter extends StandardMapMouseInterpreter {

    /**
     * The OMGraphicLayer should be set at some point before use.
     */
    public DBLClickSelectMapMouseInterpreter() {
        DEBUG = Debug.debugging("grp");
    }

    /**
     * The standard constructor.
     */
    public DBLClickSelectMapMouseInterpreter(OMGraphicHandlerLayer l) {
        this();
        setLayer(l);
    }

  
    // Mouse Listener events
    // //////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return false if nothing was pressed over, or the consumeEvents setting
     *         if something was.
     */
    public boolean mousePressed(MouseEvent e) {
        if (DEBUG) {
            Debug.output("SMMI: mousePressed()");
        }
        setCurrentMouseEvent(e);
        boolean ret = false;

        GeometryOfInterest goi = getClickInterest();
        OMGraphic omg = getGeometryUnder(e);

        if (goi != null && !goi.appliesTo(omg, e)) {
            // If the click doesn't match the geometry or button
            // of the geometry of interest, need to tell the goi
            // that is was clicked off, and set goi to null.
            if (goi.isLeftButton()) {
                leftClickOff(goi.getGeometry(), e);
            } else {
                rightClickOff(goi.getGeometry(), e);
            }
            setClickInterest(null);
        }

        if (omg != null) {
            setClickInterest(new GeometryOfInterest(omg, e));
        }

//        ret = testForAndHandlePopupTrigger(e);
//
//        if (omg != null && !ret) {
//            select(omg);
//            ret = true;
//        }

        return ret && consumeEvents;
    }




    // Mouse Motion Listener events
    // /////////////////////////////


    /**
     * Handle a left-click on an OMGraphic. Does nothing by default.
     * 
     * @return true
     */
    public boolean leftClick(OMGraphic omg, MouseEvent e) {
        boolean ret = false;

        if (omg != null && !ret && e.getClickCount() > 1) {
            select(omg);
            ret = true;
        }

        return ret && consumeEvents;
    }

}