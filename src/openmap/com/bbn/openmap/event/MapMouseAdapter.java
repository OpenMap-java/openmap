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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapMouseAdapter.java,v $
// $RCSfile: MapMouseAdapter.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.event.MouseEvent;

/**
 * Basic implementation of the MapMouseListener interface provided as
 * a convenience. If you extend an object from this adapter, you just
 * have to implement the methods that you want to deal with.
 */
public class MapMouseAdapter implements MapMouseListener {

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener. You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
        return null;
    }

    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) {
        return false; // did not handle the event
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged. The listener will receive these events if it
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {
        return false;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {}
}