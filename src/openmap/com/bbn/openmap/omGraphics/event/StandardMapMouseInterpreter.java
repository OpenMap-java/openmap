// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.8 $
// $Date: 2003/10/06 19:28:21 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

/**
 * The StandardMapMouseInterpreter is a basic implementation of the MapMouseInterpreter.
 */
public class StandardMapMouseInterpreter implements MapMouseInterpreter {

    protected boolean DEBUG = false;
    protected OMGraphicHandlerLayer layer = null;
    protected String[] mouseModeServiceList = null;
    protected String lastToolTip = null;
    protected GestureResponsePolicy grp = null;
    protected GeometryOfInterest clickInterest = null;
    protected GeometryOfInterest movementInterest = null;
    protected boolean consumeEvents = false;

    public StandardMapMouseInterpreter() {
	DEBUG = Debug.debugging("grp");
    }

    public StandardMapMouseInterpreter(OMGraphicHandlerLayer l) {
	this();
	setLayer(l);
    }

    public class GeometryOfInterest {
	OMGraphic omg;
	int button;
	boolean leftButton;

	public GeometryOfInterest(OMGraphic geom, MouseEvent me) {
	    omg = geom;
	    button = getButton(me);
	    leftButton = isLeftMouseButton(me);
	}

	public boolean appliesTo(OMGraphic geom) {
	    return (geom == omg);
	}

	public boolean appliesTo(OMGraphic geom, MouseEvent me) {
	    return (geom == omg && sameButton(me));
	}

	public boolean sameButton(MouseEvent me) {
	    return button == getButton(me);
	}
	
	public OMGraphic getGeometry() {
	    return omg;
	}

	public int getButton() {
	    return button;
	}

	/**
	 * Utility method to get around MouseEvent.getButton 1.4
	 * requirement.
	 */
	protected int getButton(MouseEvent me) {
	    // jdk 1.4 version
	    // return me.getButton();

	    // jdk 1.3 version Don't know if the numbers are the same
	    // as in me.getButton, shouldn't make a difference.
	    if (SwingUtilities.isLeftMouseButton(me)) {
		return 0;
	    } else if (SwingUtilities.isRightMouseButton(me)) {
		return 1;
	    } else {
		return 2;
	    }
	}

	public boolean isLeftButton() {
	    return leftButton;
	}
    }

    /**
     * A flag to tell the interpreter to be selfish about consuming
     * MouseEvents it receives.  If set to true, it will consume
     * events so that other MapMouseListeners will not receive the
     * events.  If false, lower layers will also receive events, which
     * will let them react too.  Intended to let other layers provide
     * information about what the mouse is over when editing is
     * occuring.
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

    public void setMouseModeServiceList(String[] list) {
	mouseModeServiceList = list;
    }

    public boolean isLeftMouseButton(MouseEvent me) {
	return SwingUtilities.isLeftMouseButton(me) && !me.isControlDown();
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
	return mouseModeServiceList;
    }

    protected void setClickInterest(GeometryOfInterest goi) {
	clickInterest = goi;
    }

    protected GeometryOfInterest getClickInterest() {
	return clickInterest;
    }

    protected void setMovementInterest(GeometryOfInterest goi) {
	movementInterest = goi;
    }

    protected GeometryOfInterest getMovementInterest() {
	return movementInterest;
    }

    /**
     * Return the OMGraphic object that is under a mouse event
     * occurance on the map, null if nothing applies.
     */
    public OMGraphic getGeometryUnder(MouseEvent me) {
	OMGraphic omg = null;
	OMGraphicList list = null;
	if (layer != null) {
	    list = layer.getList();
	    if (list != null) {
		omg = list.findClosest(me.getX(), me.getY(), 4);
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
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
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
	    select(omg);
	    setClickInterest(new GeometryOfInterest(omg, e));
	    ret = true;
	}

	return ret && consumeEvents;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
 	setCurrentMouseEvent(e);
	return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) {
 	setCurrentMouseEvent(e);
	GeometryOfInterest goi = getClickInterest();

	// If there is a click interest
	if (goi != null) {
	    // Tell the policy it an OMGraphic was clicked.
	    if (isLeftMouseButton(e)) {
		leftClick(goi.getGeometry(), e);
	    } else {
		rightClick(goi.getGeometry(), e);
	    }
	} else {
	    if (isLeftMouseButton(e)) {
		leftClick(e);
	    } else {
		rightClick(e);
	    }
	}

	return consumeEvents;
    }

    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
 	setCurrentMouseEvent(e);
    }

    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
 	setCurrentMouseEvent(e);
    }

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.  The listener will receive these events if it
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
 	setCurrentMouseEvent(e);
	GeometryOfInterest goi = getClickInterest();
	if (goi != null) {
	    setClickInterest(null);
	}

	return mouseMoved(e) && consumeEvents;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {
 	setCurrentMouseEvent(e);
	boolean ret = false;

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
     * Set whether to ignore the timer when movement is occuring over
     * an OMGraphic.  Sometimes unhighlight can be inappropriately
     * delayed when timer is enabled.
     */
    public void setNoTimerOverOMGraphic(boolean val) {
	noTimerOverOMGraphic = val;
    }

    public boolean getNoTimerOverOMGraphic() {
	return noTimerOverOMGraphic;
    }

    /**
     * The wait interval before a mouse over event gets triggered.
     */
    protected int mouseTimerInterval = 150;

    /**
     * Set the time interval that the mouse timer waits before calling
     * upateMouseMoved.  A negative number or zero will disable the
     * timer.
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

    protected MouseTimerListener mouseTimerListener = new MouseTimerListener();

    protected class MouseTimerListener implements ActionListener {

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
     * The real mouseMoved call, called when mouseMoved is called and,
     * if there is a mouse timer interval set, that interval time has
     * passed.
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
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {
	GeometryOfInterest goi = getMovementInterest();
	if (goi != null) {
	    mouseNotOver(goi.getGeometry());
	    setMovementInterest(null);
	}
    }

    public boolean leftClick(MouseEvent me) {
	if (DEBUG) {
	    Debug.output("leftClick(MAP) at " + me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean leftClick(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("leftClick(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	return true;
    }

    public boolean leftClickOff(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("leftClickOff(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean rightClick(MouseEvent me) {
	if (DEBUG) {
	    Debug.output("rightClick(MAP) at " + me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean rightClick(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("rightClick(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	return true;
    }

    public boolean rightClickOff(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("rightClickOff(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean mouseOver(MouseEvent me) {
	if (DEBUG) {
	    Debug.output("mouseOver(MAP) at " + me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean mouseOver(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("mouseOver(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	if (grp != null) {
	    handleToolTip(grp.getToolTipTextFor(omg));
	    handleInfoLine(grp.getInfoText(omg));
	    if (grp.isHighlightable(omg)) {
		grp.highlight(omg);
	    }
	}
	return true;
    }

    protected void handleToolTip(String tip) {
	if (lastToolTip == tip) {
	    return;
	}
	lastToolTip = tip;
	if (layer != null) {
	    if (lastToolTip != null) {
		layer.fireRequestToolTip(lastToolTip);
	    } else {
		layer.fireHideToolTip();
	    }
	}
    }

    protected void handleInfoLine(String line) {
	if (layer != null) {
	    layer.fireRequestInfoLine((line==null)?"":line);
	}
    }

    public boolean mouseNotOver(OMGraphic omg) {
	if (DEBUG) {
	    Debug.output("mouseNotOver(" + omg.getClass().getName() + ")");
	}

	if (grp != null) {
	    grp.unhighlight(omg);
	}
	handleToolTip(null);
	handleInfoLine(null);
	return false;
    }

    public boolean keyPressed(OMGraphic omg, int virtualKey) {
	if (DEBUG) {
	    Debug.output("keyPressed(" + omg.getClass().getName() + " , " + virtualKey + ")");
	}
	return true;
    }

    public void select(OMGraphic omg) {
	if (grp != null && grp.isSelectable(omg)) {
	    OMGraphicList omgl = new OMGraphicList();
	    omgl.add(omg);
	    grp.select(omgl);
	}
    }

    public void deselect(OMGraphic omg) {
	if (grp != null && grp.isSelectable(omg)) {
	    OMGraphicList omgl = new OMGraphicList();
	    omgl.add(omg);
	    grp.deselect(omgl);
	}
    }

    protected MouseEvent currentMouseEvent;

    protected void setCurrentMouseEvent(MouseEvent me) {
	currentMouseEvent = me;
    }

    public MouseEvent getCurrentMouseEvent() {
	return currentMouseEvent;
    }

    public void setGRP(GestureResponsePolicy grp) {
	this.grp = grp;
    }

    public GestureResponsePolicy getGRP() {
	return grp;
    }

}
