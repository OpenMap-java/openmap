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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/stateMachine/State.java,v $
// $RCSfile: State.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.util.stateMachine;

import java.awt.event.*;
import com.bbn.openmap.event.MapMouseListener;

/**
 * The state is intended to be a abstract juncture in a pattern of
 * events. The idea is that for a given state, an event will call a
 * unique response. This class lets you define this juncture, and can
 * be used as a Java adapter to define only the listener responses you
 * care about. The state, as defined in your implementation, can reset
 * the StateMachine to it's new state after it responds to an event.
 * <P>
 * 
 * Since this state machine can be used in OpenMap, the
 * Mouse/MouseMotion Listeners are MapMouseListeners. This is to let
 * OpenMap know not to send the result on to other layers that like to
 * hear about events. This State class has a setting on how you want
 * the unused MapMouseListener functions to respond to events. The
 * default is false, and this means that other layers will have a
 * chance to respond to events. If you want other layer's event
 * reception to pause a little, set the mapMouseListenerResponse to
 * true.
 */
public abstract class State implements ActionListener, AdjustmentListener,
        ComponentListener, ContainerListener, FocusListener, ItemListener,
        KeyListener, MapMouseListener, TextListener, WindowListener {

    /** The default response for the MapMouseListener methods. */
    boolean mapMouseListenerResponse = false;

    /**
     * Set the MapMouseListener method default response value. If
     * value is true, other layers on the map will not receive mouse
     * events.
     */
    public void setMapMouseListenerResponse(boolean value) {
        mapMouseListenerResponse = value;
    }

    /**
     * Get the value of the default response to MapMouseListener
     * methods.
     */
    public boolean getMapMouseListenerResponse() {
        return mapMouseListenerResponse;
    }

    /// ActionListener interface
    public void actionPerformed(ActionEvent e) {}

    /// AdjustmentListener interface
    public void adjustmentValueChanged(AdjustmentEvent e) {}

    /// ComponentListener interface
    public void componentResized(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}

    public void componentShown(ComponentEvent e) {}

    public void componentHidden(ComponentEvent e) {}

    /// ContainerListener interface
    public void componentAdded(ContainerEvent e) {}

    public void componentRemoved(ContainerEvent e) {}

    /// FocusListener interface
    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {}

    /// ItemListener interface
    public void itemStateChanged(ItemEvent e) {}

    /// KeyListener interface
    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    /// MapMouseListener
    public String[] getMouseModeServiceList() {
        return null;
    }

    public boolean mousePressed(MouseEvent e) {
        return mapMouseListenerResponse;
    }

    public boolean mouseReleased(MouseEvent e) {
        return mapMouseListenerResponse;
    }

    public boolean mouseClicked(MouseEvent e) {
        return mapMouseListenerResponse;
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public boolean mouseDragged(MouseEvent e) {
        return mapMouseListenerResponse;
    }

    public boolean mouseMoved(MouseEvent e) {
        return mapMouseListenerResponse;
    }

    public void mouseMoved() {}

    /// TextListener interface
    public void textValueChanged(TextEvent e) {}

    /// WindowListener interface
    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

}

