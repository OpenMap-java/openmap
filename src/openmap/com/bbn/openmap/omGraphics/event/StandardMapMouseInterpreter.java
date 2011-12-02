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
public class StandardMapMouseInterpreter
        implements MapMouseInterpreter {

    protected boolean DEBUG = false;
    protected OMGraphicHandlerLayer layer = null;
    protected String[] mouseModeServiceList = null;
    protected String lastToolTip = null;
    protected GestureResponsePolicy grp = null;
    protected GeometryOfInterest clickInterest = null;
    protected GeometryOfInterest movementInterest = null;
    protected boolean consumeEvents = false;

    protected boolean active = true;

    /**
     * The OMGraphicLayer should be set at some point before use.
     */
    public StandardMapMouseInterpreter() {
        DEBUG = Debug.debugging("grp");
    }

    /**
     * The standard constructor.
     */
    public StandardMapMouseInterpreter(OMGraphicHandlerLayer l) {
        this();
        setLayer(l);
    }

    /**
     * Helper class used to keep track of OMGraphics of interest. Interest means
     * that a MouseEvent that occurred over an OMGraphic that combined with
     * another MouseEvent, may be interpreted as a significant event.
     */
    public class GeometryOfInterest {
        OMGraphic omg;
        int button;
        boolean leftButton;

        /**
         * Create a Geometry of Interest with the OMGraphic and the first mouse
         * event.
         */
        public GeometryOfInterest(OMGraphic geom, MouseEvent me) {
            omg = geom;
            button = getButton(me);
            leftButton = isLeftMouseButton(me);
        }

        /**
         * A check to see if an OMGraphic is the same as the one of interest.
         */
        public boolean appliesTo(OMGraphic geom) {
            return (geom != null && geom.equals(omg));
        }

        /**
         * A check to see if a mouse event that is occurring over an OMGraphic
         * is infact occurring over the one of interest, and with the same mouse
         * button.
         */
        public boolean appliesTo(OMGraphic geom, MouseEvent me) {
            return (geom != null && geom.equals(omg) && sameButton(me));
        }

        /**
         * A check to see if the current mouse event concerns the same mouse
         * button as the original.
         */
        public boolean sameButton(MouseEvent me) {
            return button == getButton(me);
        }

        /**
         * Return the OMGraphic of interest.
         */
        public OMGraphic getGeometry() {
            return omg;
        }

        /**
         * Return the button that caused the interest.
         */
        public int getButton() {
            return button;
        }

        /**
         * Utility method to get around MouseEvent.getButton 1.4 requirement.
         */
        protected int getButton(MouseEvent me) {
            // jdk 1.4 version
            // return me.getButton();

            // jdk 1.3 version Don't know if the numbers are the same
            // as in me.getButton, shouldn't make a difference.
            if (me.isControlDown() /* PopupTrigger() */
                    || SwingUtilities.isRightMouseButton(me)) {
                return 1;
            } else if (SwingUtilities.isLeftMouseButton(me)) {
                return 0;
            } else {
                return 2;
            }
        }

        /**
         * Return if the current button is the left one.
         */
        public boolean isLeftButton() {
            return leftButton;
        }

        /**
         * Called when the popup trigger is known to have been triggered and a
         * click interest has been set by it.
         * 
         * @param b
         */
        public void setLeftButton(boolean b) {
            leftButton = b;
        }
    }

    /**
     * A flag to tell the interpreter to be selfish about consuming MouseEvents
     * it receives. If set to true, it will consume events so that other
     * MapMouseListeners will not receive the events. If false, lower layers
     * will also receive events, which will let them react too. Intended to let
     * other layers provide information about what the mouse is over when
     * editing is occurring.
     */
    public void setConsumeEvents(boolean consume) {
        consumeEvents = consume;
    }

    public boolean getConsumeEvents() {
        return consumeEvents;
    }

    public void setLayer(OMGraphicHandlerLayer l) {
        layer = l;
    }

    public OMGraphicHandlerLayer getLayer() {
        return layer;
    }

    /**
     * Set the ID's of the mouse modes that this interpreter should be listening
     * to. If set to null, this SMMI won't receive MouseEvents.
     */
    public void setMouseModeServiceList(String[] list) {
        mouseModeServiceList = list;
    }

    /**
     * A method to set how a left mouse button is interpreted. We count
     * control-clicks as not a left mouse click.
     */
    public boolean isLeftMouseButton(MouseEvent me) {
        return SwingUtilities.isLeftMouseButton(me);
    }

    /**
     * Return a list of the modes that are interesting to the MapMouseListener.
     * You MUST override this with the modes you're interested in, or set the
     * mouse mode service list, or you won't receive mouse events.
     */
    public String[] getMouseModeServiceList() {
        return mouseModeServiceList;
    }

    /**
     * Set the GeometryOfInterest as one that could possibly be in the process
     * of being clicked upon.
     */
    protected void setClickInterest(GeometryOfInterest goi) {
        clickInterest = goi;
    }

    /**
     * Get the GeometryOfInterest as one that could possibly be in the process
     * of being clicked upon.
     */
    protected GeometryOfInterest getClickInterest() {
        return clickInterest;
    }

    /**
     * Set the GeometryOfInterest for something that the mouse is over. Prevents
     * excessive modifications of the GUI if this remains constant.
     */
    protected void setMovementInterest(GeometryOfInterest goi) {
        movementInterest = goi;
    }

    /**
     * Get the GeometryOfInterest for something that the mouse is over. Prevents
     * excessive modifications of the GUI if this remains constant.
     */
    protected GeometryOfInterest getMovementInterest() {
        return movementInterest;
    }

    /**
     * Return the OMGraphic object that is under a mouse event occurrence on the
     * map, null if nothing applies.
     */
    public OMGraphic getGeometryUnder(MouseEvent me) {
        OMGraphic omg = null;
        OMGraphicList list = null;
        if (layer != null) {
            list = layer.getList();
            if (list != null) {
                int x = me.getX();
                int y = me.getY();
                if (me instanceof MapMouseEvent) {
                    Point2D pnt = ((MapMouseEvent) me).getProjectedLocation();
                    x = (int) pnt.getX();
                    y = (int) pnt.getY();
                }

                omg = list.findClosest(x, y, 4);
            } else {
                if (DEBUG) {
                    Debug.output("SMMI: no layer to evaluate mouse event");
                }
            }
        } else {
            if (DEBUG) {
                Debug.output("SMMI: no layer to evaluate mouse event");
            }
        }

        return omg;
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
            Debug.output("SMMI:mousePressed()");
        }
        return setClickInterestFromMouseEvent(e);
    }

    /**
     * Set the GeometryOfInterest based on MouseEvent. The default behavior of
     * mousePressed.
     * 
     * @param e MouseEvent
     * @return whether mouse event was consumed.
     */
    protected boolean setClickInterestFromMouseEvent(MouseEvent e) {
        if (!active) {
            return false;
        }

        if (DEBUG) {
            Debug.output("SMMI: setClickInterestFromMouseEvent()");
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

        ret = testForAndHandlePopupTrigger(e);

        if (omg != null && !ret) {
            select(omg);
            ret = true;
        }

        return ret && consumeEvents;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
        if (!active) {
            return false;
        }

        setCurrentMouseEvent(e);
        return testForAndHandlePopupTrigger(e) && consumeEvents;
    }

    /**
     * Tests the MouseEvent to see if it's a popup trigger, and calls rightClick
     * appropriately if there is an OMGraphic involved.
     * 
     * @param e MouseEvent
     * @return true if the MouseEvent is a popup trigger and has been consumed.
     */
    public boolean testForAndHandlePopupTrigger(MouseEvent e) {
        boolean ret = false;
        if (e.isPopupTrigger()) {
            GeometryOfInterest goi = getClickInterest();
            // If there is a click interest
            if (goi != null) {
                // Tell the policy it an OMGraphic was clicked.
                goi.setLeftButton(false);
                ret = rightClick(goi.getGeometry(), e);
            } else {
                ret = rightClick(e);
            }
        }
        return ret;
    }

    /**
     * Invoked when the mouse has been clicked. Notifies the left click methods
     * for the applicable OMGraphic or the map. Right click methods are handled
     * when the testForAndHandlePopupTrigger method is called in mousePressed
     * and mouseReleased.
     * 
     * @param e MouseEvent
     * @return the consumeEvents setting.
     */
    public boolean mouseClicked(MouseEvent e) {
        if (!active) {
            return false;
        }

        // Should have been done already from the MousePressed, but different
        // OS Java implementations have the pressed occur after click. Gah!
        setClickInterestFromMouseEvent(e);

        if (isLeftMouseButton(e)) {
            GeometryOfInterest goi = getClickInterest();
            // If there is a click interest
            if (goi != null) {
                // Tell the policy it an OMGraphic was clicked.
                if (goi.isLeftButton()) {
                    leftClick(goi.getGeometry(), e);
                } else {
                    rightClick(goi.getGeometry(), e);
                }
            } else {
                leftClick(e);
            }

            return consumeEvents;
        }

        return false;
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        if (!active) {
            return;
        }
        setCurrentMouseEvent(e);
    }

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
        if (!active) {
            return;
        }
        setCurrentMouseEvent(e);
    }

    // Mouse Motion Listener events
    // /////////////////////////////

    /**
     * Invoked when a mouse button has been pressed and is moving. Resets the
     * click geometry of interest to null.
     * 
     * @param e MouseEvent
     * @return the result from mouseMoved (also called from this method)
     *         combined with the consumeEvents setting.
     */
    public boolean mouseDragged(MouseEvent e) {
        if (!active) {
            return false;
        }
        setCurrentMouseEvent(e);
        GeometryOfInterest goi = getClickInterest();
        if (goi != null) {
            setClickInterest(null);
        }

        return mouseMoved(e) && consumeEvents;
    }

    /**
     * Invoked when the mouse has been moved. Sets the movement geometry of
     * interest and updates the movement timer.
     * 
     * @param e MouseEvent
     * @return the result of updateMouseMoved() if the timer isn't being used,
     *         or false.
     */
    public boolean mouseMoved(MouseEvent e) {
        if (!active) {
            return false;
        }
        setCurrentMouseEvent(e);

        if ((noTimerOverOMGraphic && getMovementInterest() != null) || mouseTimerInterval <= 0) {
            return updateMouseMoved(e);
        } else {
            if (mouseTimer == null) {
                mouseTimer = new Timer(mouseTimerInterval, mouseTimerListener);
                mouseTimer.setRepeats(false);
            }

            mouseTimerListener.setEvent(e);
            mouseTimer.restart();
            return false;
        }
    }

    protected boolean noTimerOverOMGraphic = true;

    /**
     * Set whether to ignore the timer when movement is occurring over an
     * OMGraphic. Sometimes unhighlight can be inappropriately delayed when
     * timer is enabled.
     */
    public void setNoTimerOverOMGraphic(boolean val) {
        noTimerOverOMGraphic = val;
    }

    /**
     * Get whether the timer should be ignored when movement is occurring over
     * an OMGraphic.
     */
    public boolean getNoTimerOverOMGraphic() {
        return noTimerOverOMGraphic;
    }

    /**
     * The wait interval before a mouse over event gets triggered.
     */
    protected int mouseTimerInterval = 150;

    /**
     * Set the time interval that the mouse timer waits before calling
     * upateMouseMoved. A negative number or zero will disable the timer.
     */
    public void setMouseTimerInterval(int interval) {
        mouseTimerInterval = interval;
    }

    public int getMouseTimerInterval() {
        return mouseTimerInterval;
    }

    /**
     * The timer used to track the wait interval.
     */
    protected Timer mouseTimer = null;

    /**
     * The timer listener that calls updateMouseMoved.
     */
    protected MouseTimerListener mouseTimerListener = new MouseTimerListener();

    /**
     * The definition of the listener that calls updateMouseMoved when the timer
     * goes off.
     */
    protected class MouseTimerListener
            implements ActionListener {

        private MouseEvent event;

        public synchronized void setEvent(MouseEvent e) {
            event = e;
        }

        public synchronized void actionPerformed(ActionEvent ae) {
            if (event != null) {
                updateMouseMoved(event);
            }
        }
    }

    /**
     * The real mouseMoved call, called when mouseMoved is called and, if there
     * is a mouse timer interval set, that interval time has passed.
     * 
     * @return the consumeEvents setting of the mouse event concerns an
     *         OMGraphic, false if it didn't.
     */
    protected boolean updateMouseMoved(MouseEvent e) {
        boolean ret = false;
        OMGraphic omg = getGeometryUnder(e);
        GeometryOfInterest goi = getMovementInterest();

        if (omg != null && grp != null) {

            // This gets called if the goi is new or if the goi
            // refers to a different OMGraphic as previously noted.
            if (goi == null || !goi.appliesTo(omg)) {

                if (goi != null) {
                    mouseNotOver(goi.getGeometry());
                }

                goi = new GeometryOfInterest(omg, e);
                setMovementInterest(goi);
                setNoTimerOverOMGraphic(!omg.shouldRenderFill());
                ret = mouseOver(omg, e);
            }

        } else {
            if (goi != null) {
                mouseNotOver(goi.getGeometry());
                setMovementInterest(null);
            }
            ret = mouseOver(e);
        }

        return ret && consumeEvents;
    }

    /**
     * Handle notification that another layer consumed a mouse moved event. Sets
     * movement interest to null.
     */
    public void mouseMoved() {
        if (!active) {
            return;
        }
        GeometryOfInterest goi = getMovementInterest();
        if (goi != null) {
            mouseNotOver(goi.getGeometry());
            setMovementInterest(null);
        }
    }

    /**
     * Handle a left-click on the map. Does nothing by default.
     * 
     * @return false
     */
    public boolean leftClick(MouseEvent me) {
        if (DEBUG) {
            Debug.output("leftClick(MAP) at " + me.getX() + ", " + me.getY());
        }

        if (grp != null && grp.receivesMapEvents() && me instanceof MapMouseEvent) {
            return grp.leftClick((MapMouseEvent) me);
        }

        return false;
    }

    /**
     * Handle a left-click on an OMGraphic. Does nothing by default.
     * 
     * @return true
     */
    public boolean leftClick(OMGraphic omg, MouseEvent me) {
        if (DEBUG) {
            Debug.output("leftClick(" + omg.getClass().getName() + ") at " + me.getX() + ", " + me.getY());
        }

        return false;
    }

    /**
     * Notification that the user clicked on something else other than the
     * provided OMGraphic that was previously left-clicked on. Calls
     * deselect(omg).
     * 
     * @return false
     */
    public boolean leftClickOff(OMGraphic omg, MouseEvent me) {
        if (DEBUG) {
            Debug.output("leftClickOff(" + omg.getClass().getName() + ") at " + me.getX() + ", " + me.getY());
        }

        deselect(omg);

        return false;
    }

    /**
     * Notification that the map was right-clicked on.
     * 
     * @return false
     */
    public boolean rightClick(MouseEvent me) {
        if (DEBUG) {
            Debug.output("rightClick(MAP) at " + me.getX() + ", " + me.getY());
        }

        if (me instanceof MapMouseEvent && grp != null) {
            return displayPopup(grp.getItemsForMapMenu((MapMouseEvent) me), me);
        }

        return false;
    }

    /**
     * Notification that an OMGraphic was right-clicked on.
     * 
     * @return true
     */
    public boolean rightClick(OMGraphic omg, MouseEvent me) {
        if (DEBUG) {
            Debug.output("rightClick(" + omg.getClass().getName() + ") at " + me.getX() + ", " + me.getY());
        }

        if (grp != null) {
            return displayPopup(grp.getItemsForOMGraphicMenu(omg), me);
        }
        
        return false;
    }

    /**
     * Create a pop-up menu from GRP requests, over the mouse event location.
     * 
     * @return true if pop-up was presented, false if not.
     */
    protected boolean displayPopup(List<Component> contents, MouseEvent me) {
        if (DEBUG) {
            Debug.output("displayPopup(" + contents + ") " + me);
        }
        if (contents != null && !contents.isEmpty()) {
            JPopupMenu jpm = new JPopupMenu();
            for (Component comp : contents) {
                jpm.add(comp);
            }
            jpm.show((Component) me.getSource(), me.getX(), me.getY());
            return true;
        }
        return false;
    }

    /**
     * Notification that the user clicked on something else other than the
     * provided OMGraphic that was previously right-clicked on.
     * 
     * @return false
     */
    public boolean rightClickOff(OMGraphic omg, MouseEvent me) {
        if (DEBUG) {
            Debug.output("rightClickOff(" + omg.getClass().getName() + ") at " + me.getX() + ", " + me.getY());
        }

        return false;
    }

    /**
     * Notification that the mouse is not over an OMGraphic, but over the map at
     * some location.
     * 
     * @return false
     */
    public boolean mouseOver(MouseEvent me) {
        if (DEBUG) {
            Debug.output("mouseOver(MAP) at " + me.getX() + ", " + me.getY());
        }
        if (grp != null && grp.receivesMapEvents() && me instanceof MapMouseEvent) {
            return grp.mouseOver((MapMouseEvent) me);
        }

        return false;
    }

    /**
     * Notification that the mouse is over an OMGraphic. Makes all the highlight
     * calls.
     * 
     * @return true
     */
    public boolean mouseOver(OMGraphic omg, MouseEvent me) {
        if (DEBUG) {
            Debug.output("mouseOver(" + omg.getClass().getName() + ") at " + me.getX() + ", " + me.getY());
        }

        if (grp != null) {
            handleToolTip(grp.getToolTipTextFor(omg), me);
            handleInfoLine(grp.getInfoText(omg));
            if (grp.isHighlightable(omg)) {
                grp.highlight(omg);
            }
        }
        return true;
    }

    /**
     * Given a tool tip String, use the layer to get it displayed.
     */
    protected void handleToolTip(String tip, MouseEvent me) {
        if (lastToolTip != null && lastToolTip.equals(tip)) {
            return;
        }
        lastToolTip = tip;
        if (layer != null) {
            if (lastToolTip != null && lastToolTip.trim().length() > 0) {
                layer.fireRequestToolTip(lastToolTip);
                // forward the event to the tool tip manager so it will popup
                // the tool tip right away, otherwise an additional event is
                // required to trigger it
                ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                toolTipManager.mouseMoved(me);
            } else {
                layer.fireHideToolTip();
            }
        }
    }

    /**
     * Given an information line, use the layer to get it displayed on the
     * InformationDelegator.
     */
    protected void handleInfoLine(String line) {
        if (layer != null) {
            layer.fireRequestInfoLine((line == null) ? "" : line);
        }
    }

    /**
     * Notification that the mouse has moved off of an OMGraphic.
     */
    public boolean mouseNotOver(OMGraphic omg) {
        if (DEBUG) {
            Debug.output("mouseNotOver(" + omg.getClass().getName() + ")");
        }

        if (grp != null) {
            grp.unhighlight(omg);
        }
        handleToolTip(null, null);
        handleInfoLine(null);
        return false;
    }

    /**
     * Notify the GRP that the OMGraphic has been selected. Wraps the OMGraphic
     * in an OMGraphicList.
     */
    public void select(OMGraphic omg) {
        if (grp != null && grp.isSelectable(omg)) {
            OMGraphicList omgl = new OMGraphicList();
            omgl.add(omg);
            grp.select(omgl);
        }
    }

    /**
     * Notify the GRP that the OMGraphic has been deselected. Wraps the
     * OMGraphic in an OMGraphicList.
     */
    public void deselect(OMGraphic omg) {
        if (grp != null && grp.isSelectable(omg)) {
            OMGraphicList omgl = new OMGraphicList();
            omgl.add(omg);
            grp.deselect(omgl);
        }
    }

    /**
     * The last MouseEvent received, for later reference.
     */
    protected MouseEvent currentMouseEvent;

    /**
     * Set the last MouseEvent received.
     */
    protected void setCurrentMouseEvent(MouseEvent me) {
        currentMouseEvent = me;
    }

    /**
     * Get the last MouseEvent received.
     */
    public MouseEvent getCurrentMouseEvent() {
        return currentMouseEvent;
    }

    /**
     * Set the GestureResponsePolicy to notify of the mouse actions over the
     * layer's OMGraphicList.
     */
    public void setGRP(GestureResponsePolicy grp) {
        this.grp = grp;
    }

    /**
     * Get the GestureResponsePolicy that is being notified of the mouse actions
     * over the layer's OMGraphicList.
     */
    public GestureResponsePolicy getGRP() {
        return grp;
    }

    /**
     * Check whether the MapMouseInterpreter is responding to events.
     * 
     * @return true if willing to respond to MouseEvents.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set whether the MapMouseInterpreter responds
     * 
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

}