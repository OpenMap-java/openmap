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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/EventLayer.java,v $
// $RCSfile: EventLayer.java,v $
// $Revision: 1.9 $
// $Date: 2006/03/06 16:13:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.event.OMEventHandler;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.time.TemporalOMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.time.RealTimeHandler;
import com.bbn.openmap.time.TimeBounds;
import com.bbn.openmap.time.TimeBoundsHandler;
import com.bbn.openmap.time.TimeBoundsProvider;
import com.bbn.openmap.time.TimeEvent;
import com.bbn.openmap.time.TimeEventListener;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.PropUtils;

/**
 * The EventLayer contains all the TemporalOMGraphics and manages the time for
 * the graphics and how they should be displayed. This layer works as a
 * TimeBoundsProvider (an object that provides an active range of time, a
 * OMEventHandler (an object that provides OMEvents to the
 * EventPanel/EventPresenter), and a TimeEventListener (an object that changes
 * when the cloc changes).
 * <p>
 * 
 * Sample properties:
 * 
 * <pre>
 *    eventLayer.class=com.bbn.openmap.layer.time.EventLayer
 *    eventLayer.importer=com.bbn.openmap.layer.time.CSVEventImporter
 *    eventLayer.prettyName=Test Event
 *    
 *    ... see CSVEventImporter for its properties, other importers can be specified here.
 * </pre>
 */
public class EventLayer extends OMGraphicHandlerLayer implements
        DataBoundsProvider, TimeBoundsProvider, TimeEventListener,
        OMEventHandler {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.event.EventLayer");

    public final static String ImporterProperty = "importer";
    protected EventImporter importer = null;
    protected DataBounds dataBounds = null;

    protected TimeBounds timeBounds;
    protected long time;
    protected String mode;
    protected boolean active = false;

    public EventLayer() {
        dataBounds = new DataBounds();
    }

    /**
     * The main method call in the EventLayer that actually modifies the
     * OMGraphics and updates the map.
     */
    public synchronized OMGraphicList prepare() {
        Projection p = getProjection();
        TemporalOMGraphicList scenarioGraphics = null;
        OMGraphicList list = getList();

        // Need this check in here because the time stuff might cause this to be
        // called before the layer is actually added to the map.
        if (p != null) {
            boolean DEBUG = logger.isLoggable(Level.FINE);
            if (list == null || !(list instanceof TemporalOMGraphicList)) {
                scenarioGraphics = createData();
                if (scenarioGraphics == null) {
                    // Don't do anything
                    return null;
                }
                // The data importer should have set the time bounds, and that
                // should be calling this then.
                // callForTimeBoundsReset();
            } else {
                scenarioGraphics = new TemporalOMGraphicList(list);
            }

            long currentTime = getTime();

            if (DEBUG) {
                logger.fine("EventLayer (" + getName() + ") snapshot at "
                        + currentTime);
            }

            scenarioGraphics.generate(p, currentTime);

            if (DEBUG) {
                logger.fine("EventLayer (" + getName() + ") setting list of "
                        + scenarioGraphics.size() + " scenario graphics");
            }
        }

        return scenarioGraphics;
    }

    /**
     * Read the data files and construct the TemporalOMGraphics.
     */
    public synchronized TemporalOMGraphicList createData() {
        if (importer != null) {
            return importer.createData(this);
        } else {
            return null;
        }
    }

    /**
     * The properties and prefix are managed and decoded here, for the standard
     * uses of the EventLayer.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String importerClass = properties.getProperty(prefix + ImporterProperty);
        if (importerClass != null) {
            importer = (EventImporter) ComponentFactory.create(importerClass,
                    prefix,
                    properties);
        }
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
     * 
     * @param props a Properties object to load the PropertyConsumer properties
     *        into.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        if (importer != null) {
            props.put(prefix + ImporterProperty, importer.getClass().getName());
        }

        if (importer instanceof PropertyConsumer) {
            ((PropertyConsumer) importer).getProperties(props);
        }

        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.). This method takes care of the basic
     * LocationHandler parameters, so any LocationHandlers that extend the
     * AbstractLocationHandler should call this method, too, before adding any
     * specific properties.
     * 
     * @param list a Properties object to load the PropertyConsumer properties
     *        into. If getList equals null, then a new Properties object should
     *        be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        PropUtils.setI18NPropertyInfo(i18n,
                list,
                getClass(),
                ImporterProperty,
                "Data Importer",
                "Class name of data importer",
                null);

        if (importer instanceof PropertyConsumer) {
            ((PropertyConsumer) importer).getPropertyInfo(list);
        }

        return list;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * DataBoundsProvider method.
     */
    public DataBounds getDataBounds() {
        return dataBounds;
    }

    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);

        if (someObj instanceof TimeBoundsHandler) {
            addTimeBoundsHandler((TimeBoundsHandler) someObj);
        }

        if (someObj instanceof RealTimeHandler) {
            ((RealTimeHandler) someObj).addTimeEventListener(this);
        }
    }

    // TimeBoundsProvider methods.
    protected List<TimeBoundsHandler> timeBoundsHandlers = new ArrayList<TimeBoundsHandler>();

    public void addTimeBoundsHandler(TimeBoundsHandler tbh) {
        logger.fine("found TimeBoundsHandler: " + tbh.getClass().getName());
        timeBoundsHandlers.add(tbh);
    }

    protected void setTimeBounds(TimeBounds tb) {
        timeBounds = tb;
        if (active) {
            callForTimeBoundsReset();
        }
    }

    public TimeBounds getTimeBounds() {
        if (active) {
            return timeBounds;
        }
        return null;
    }

    public void handleTimeBounds(TimeBounds tb) {
    // NoOp, we don't really care what the current time bounds are
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (timeBounds != null) {
            callForTimeBoundsReset();
        }
    }

    public void removeTimeBoundsHandler(TimeBoundsHandler tbh) {
        timeBoundsHandlers.remove(tbh);
    }

    public void updateTime(TimeEvent te) {
        time = te.getSystemTime();
        if (active) {
            setList(prepare());
            repaint();
        }
    }

    public long getTime() {
        return time;
    }

    protected LinkedList<OMEvent> events = new LinkedList<OMEvent>();
    protected LinkedList<OMEvent> filters = new LinkedList<OMEvent>();

    public List<OMEvent> getEventList() {
        return getEventList(null);
    }

    public List<OMEvent> getEventList(List filters) {
        if (active) {
            return events;
        }
        return null;
    }

    public Boolean getFilterState(String filterName) {
        return (isVisible() ? Boolean.TRUE : Boolean.FALSE);
    }

    public List getFilters() {
        return filters;
    }

    public List<OMEvent> getMacroFilteredList(Collection eventCollection) {
        return events;
    }

    public void setFilterState(String filterName, Boolean state) {}

    public void callForTimeBoundsReset() {
        for (TimeBoundsHandler tbh : timeBoundsHandlers) {
            tbh.resetTimeBounds();
        }
    }

    public void setVisible(boolean setting) {
        super.setVisible(setting);
        setActive(setting);
    }

}