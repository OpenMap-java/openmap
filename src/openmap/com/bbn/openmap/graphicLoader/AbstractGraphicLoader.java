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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/AbstractGraphicLoader.java,v $
// $RCSfile: AbstractGraphicLoader.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 17:44:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Timer;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * The abstract AbstractGraphicLoader class is an OMGraphic managing
 * object. It can talk to any source it wants to for configuring its
 * OMGraphicList, and then notifies its OMGraphicHandler with the
 * changes. The AbstractGraphicLoader comes with a built-in timer, in
 * case you want to check back with the graphic source within certain
 * intervals in order to update the graphics in an animated fashion.
 * This class also extends MapHandlerChild so if you want to add it to
 * the MapHandler to find other objects, you can override the
 * findAndInit() method.
 * <P>
 * 
 * This object is really intended to be used with the
 * GraphicLoaderPlugIn, but it doesn't really have to be. If you have
 * a GraphicLoaderConnector in the MapHandler and then add a
 * GraphicLoader to the MapHandler, the GraphicLoaderConnector will
 * check to see if the GraphicLoader has a receiver. If there isn't a
 * receiver, the GraphicLoaderConnetor will create a
 * GraphicLoaderPlugIn for you, connect the GraphicLoader to it, and
 * then add the GraphicLoaderPlugIn to the LayerHandler. Then, the
 * GraphicLoader will have its graphics on the map.
 */
public abstract class AbstractGraphicLoader extends OMComponent implements
        GraphicLoader, ActionListener {

    protected Timer timer;
    protected int DO_NOT_UPDATE_TIMER = -1;
    protected OMGraphicHandler receiver = null;
    protected Projection proj; // last projection
    protected String name = "";

    public final static String TimerCmd = "TimerCommand";
    public final static String NameProperty = "prettyName";

    public AbstractGraphicLoader() {
        createTimer();
    }

    public AbstractGraphicLoader(OMGraphicHandler receiver) {
        super();
        setReceiver(receiver);
        manageGraphics();
    }

    /**
     * The method where the AbstractGraphicLoader is expected to tell
     * the receiver what the OMGraphics are. This gets called by
     * default by the actionPerformed() method, which is called by
     * default by the built-in timer when it is running.
     * 
     * @see com.bbn.openmap.omGraphics.OMGraphicHandler#setList(OMGraphicList)
     */
    public abstract void manageGraphics();

    /**
     * Provide a GUI for controlling the GraphicLoader. It's OK if
     * it's null.
     */
    public Component getGUI() {
        return null;
    }

    /**
     * Calls manageGraphics() if projection is different().
     */
    public void setProjection(Projection p) {
        if (!p.equals(getProjection())) {
            proj = p;
            manageGraphics();
        }
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
     * Get the timer being used for automatic updates. May be null if
     * a timer is not set.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * If you want the layer to update itself at certain intervals,
     * you can set the timer to do that. Set it to null to disable it.
     * If the current timer is not null, the graphic loader is removed
     * as an ActionListener. If the new one is not null, the graphic
     * loader is added as an ActionListener.
     */
    public void setTimer(Timer t) {
        if (timer != null) {
            timer.stop();
            timer.removeActionListener(this);
        }

        timer = t;
        if (timer != null) {
            timer.addActionListener(this);
        }
    }

    /**
     * Creates a timer with the current updateInterval and calls
     * setTimer().
     */
    public void createTimer() {
        Timer t = new Timer(updateInterval, this);
        t.setInitialDelay(0);
        setTimer(t);
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

    /**
     * Method gets called by the timer if it's running. Will also get
     * called if any other component is using this class as an
     * ActionListener. By default, calls manageGraphics();
     */
    public void actionPerformed(ActionEvent ae) {
        manageGraphics();
    }

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

        name = props.getProperty(prefix + NameProperty, name);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + NameProperty, getName());
        return props;
    }

    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);
        list.put(NameProperty,
                "A short name description for what this GraphicLoader does.");
        return list;
    }
}