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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/MMLGraphicLoader.java,v $
// $RCSfile: MMLGraphicLoader.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader;

import java.awt.event.MouseEvent;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicHandler;

/**
 * The MMLGraphicLoader is an abstract GraphicLoader class that
 * implements the MapMouseListener interface. It extends the
 * AbstractGraphicLoader, so all of those features are included as
 * well. These MapMouseListener methods are provided here as a
 * convenience, so you don't have to write them all if you don't use
 * them.
 */
public abstract class MMLGraphicLoader extends AbstractGraphicLoader implements
        MapMouseListener {

    public MMLGraphicLoader() {
        super();
    }

    public MMLGraphicLoader(OMGraphicHandler receiver) {
        super(receiver);
    }

    ///////// MapMouseListener interface methods

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener. The source MouseEvents will only get sent to
     * the MapMouseListener if the mode is set to one that the
     * listener is interested in. Layers interested in receiving
     * events should register for receiving events in "select" mode:
     * <code>
     * <pre>
     * return new String[] { SelectMouseMode.modeID };
     * </pre>
     * <code>
     * @return String[] of modeID's
     * @see com.bbn.openmap.event.NavMouseMode#modeID
     * @see com.bbn.openmap.event.SelectMouseMode#modeID
     * @see com.bbn.openmap.event.NullMouseMode#modeID
     */
    public String[] getMouseModeServiceList() {
        return new String[] { SelectMouseMode.modeID };
    }

    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mousePressed(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseReleased(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component. The
     * listener will receive this event if it successfully processed
     * <code>mousePressed()</code>, or if no other listener
     * processes the event. If the listener successfully processes
     * <code>mouseClicked()</code>, then it will receive the next
     * <code>mouseClicked()</code> notifications that have a click
     * count greater than one.
     * <p>
     * NOTE: We have noticed that this method can sometimes be
     * erroneously invoked. It seems to occur when a light-weight AWT
     * component (like an internal window or menu) closes (removes
     * itself from the window hierarchy). A specific OpenMap example
     * is when you make a menu selection when the MenuItem you select
     * is above the MapBean canvas. After making the selection, the
     * mouseClicked() gets invoked on the MouseDelegator, which passes
     * it to the appropriate listeners depending on the MouseMode. The
     * best way to avoid this problem is to not implement anything
     * crucial in this method. Use a combination of
     * <code>mousePressed()</code> and <code>mouseReleased()</code>
     * instead.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
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
     * successfully processes mousePressed(), or if no other listener
     * processes the event.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseDragged(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseMoved(MouseEvent e) {
        return false;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * This event is intended to tell the listener that there was a
     * mouse movement, but that the event was consumed by another
     * layer. This will allow a mouse listener to clean up actions
     * that might have happened because of another motion event
     * response.
     */
    public void mouseMoved() {}
}