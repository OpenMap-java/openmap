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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/graphicLoader/Attic/AbstractGraphicLoader.java,v $
// $RCSfile: AbstractGraphicLoader.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.plugin.graphicLoader;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import javax.swing.Timer;

import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The abstract AbstractGraphicLoader class is an OMGraphic managing
 * object.  It can talk to any source it wants to for configuring its
 * OMGraphicList, and then notifies its OMGraphicHandler with the
 * changes. The AbstractGraphicLoader comes with a built-in timer, in
 * case you want to check back with the graphic source within certain
 * intervals in order to update the graphics in an animated
 * fashion. This class also extends MapHandlerChild so if you want to
 * add it to the MapHandler to find other objects, you can override
 * the findAndInit() method.<P>
 *
 * This object is really intended to be used with the
 * GraphicLoaderPlugIn, but it doesn't really have to be.  If you have
 * a GraphicLoaderConnector in the MapHandler and then add a
 * GraphicLoader to the MapHandler, the GraphicLoaderConnector will
 * check to see if the GraphicLoader has a receiver.  If there isn't a
 * receiver, the GraphicLoaderConnetor will create a
 * GraphicLoaderPlugIn for you, connect the GraphicLoader to it, and
 * then add the GraphicLoaderPlugIn to the LayerHandler.  Then, the
 * GraphicLoader will have its graphics on the map.  
 */
public abstract class AbstractGraphicLoader extends OMComponent 
    implements GraphicLoader, ActionListener {

    protected Timer timer;
    protected int DO_NOT_UPDATE_TIMER = -1;
    protected OMGraphicHandler receiver = null;
    protected Projection proj; // last projection
    protected String name = "";

    public final static String TimerCmd = "TimerCommand";
    public final static String NameProperty = "prettyName";

    public AbstractGraphicLoader() {
	setTimer(new Timer(updateInterval, this));
    }

    public AbstractGraphicLoader(OMGraphicHandler receiver) {
	super();
	setReceiver(receiver);
	manageGraphics();
    }

    /**
     * The method where the AbstractGraphicLoader is expected to tell
     * the receiver what the OMGraphics are.
     * @see com.bbn.openmap.omGraphics.OMGraphicHandler#setList(OMGraphicList)
     */
    public abstract void manageGraphics();

    public abstract Component getGUI();

    public void setProjection(Projection p) {
	proj = p;
    }

    public Projection getProjection() {
	return proj;
    }

    public void setReceiver(OMGraphicHandler r) {
	receiver = r;
    }

    public OMGraphicHandler getReceiver() {
	return receiver;
    }

    /**
     * Get the timer being used for automatic updates.  May be null if
     * a timer is not set.
     */
    public Timer getTimer() {
	return timer;
    }

    /**
     * If you want the layer to update itself at certain intervals,
     * you can set the timer to do that.  Set it to null to disable it.
     */
    public void setTimer(Timer t) {
	timer = t;
    }

    /**
     * The delay between timer pulses, in milliseconds.
     */
    protected int updateInterval = 3000;

    public void setUpdateInterval(int delay) {
	updateInterval = delay;
	if (timer != null) {
	    timer.setDelay(updateInterval);
	    if (timer.isRunning()) {
		timer.restart();
	    }
	}
    }

    public int getUpdateInterval() {
	return updateInterval;
    }

    public void actionPerformed(ActionEvent ae) {}

    /**
     * Return a pretty name for the GUI to let folks know what the
     * loader does.
     */
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);

	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	name = props.getProperty(prefix + NameProperty);
    }

    public Properties getProperties(Properties props) {
	props = super.getProperties(props);

	String prefix = PropUtils.getScopedPropertyPrefix(this);

	props.put(prefix + NameProperty, getName());
	return props;
    }

    public Properties getPropertyInfo(Properties list) {
	list = super.getPropertyInfo(list);

	String prefix = PropUtils.getScopedPropertyPrefix(this);
	list.put(NameProperty, "A short name description for what this GraphicLoader does.");

	return list;
    }
}
