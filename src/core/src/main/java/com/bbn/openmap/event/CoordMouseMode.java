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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/CoordMouseMode.java,v $
// $RCSfile: CoordMouseMode.java,v $
// $Revision: 1.9 $
// $Date: 2008/10/10 00:57:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.coordFormatter.BasicCoordInfoFormatter;
import com.bbn.openmap.util.coordFormatter.CoordInfoFormatter;
import com.bbn.openmap.util.coordFormatter.CoordInfoFormatterHandler;

/**
 * The CoordMouseMode is an abstract MouseMode extension to AbstractMouseMode
 * that can be used for Modes that want to use the BeanContext to hook up with
 * the InformationDelegator, and to send coordinate updates to be displayed in
 * the infoline.
 * <P>
 * 
 * The CoordMouseMode has been updated to use CoordInfoFormatters to allow more
 * flexibility in how coordinates are displayed in the InformationDelegator. You
 * can use the 'coordFormatter' property to set a particular CoordInfoFormatter
 * for this mouse mode. If you add a CoordInfoFormatterHandler to the
 * MapHandler, the mouse mode will use whatever formatter is active in that
 * CoordInfoFormatterHandler instead of what's specified in the properties.
 */
public abstract class CoordMouseMode extends AbstractMouseMode implements
        PropertyChangeListener {

    /**
     * The info delegator that will display the distance information
     */
    public InformationDelegator infoDelegator = null;

    /**
     * 'coordFormatter' property for setting the class of the coordinate
     * formatter.
     */
    public final static String CoordFormatterProperty = "coordFormatter";

    protected CoordInfoFormatter coordFormatter = new BasicCoordInfoFormatter();
    protected CoordInfoFormatterHandler coordFormatterHandler = null;

    public CoordMouseMode() {}

    /**
     * @param modeID the id for the mouse mode.
     * @param shouldConsumeEvents the mode setting, where the mousemode should
     *        pass the events on to other listeners or not, depending if one of
     *        the listeners used it or not.
     */
    public CoordMouseMode(String modeID, boolean shouldConsumeEvents) {
        super(modeID, shouldConsumeEvents);
    }

    /**
     * Set the information delegator.
     * 
     * @param id the information delegator that displays the distance values.
     */
    public void setInfoDelegator(InformationDelegator id) {
        infoDelegator = id;
    }

    /**
     * Return the information delegator.
     */
    public InformationDelegator getInfoDelegator() {
        return infoDelegator;
    }

    /**
     * Fires a mouse location to the InformationDelegator, and then calls the
     * super class method which calls the MouseSupport method.
     * 
     * @param e MouseEvent to be handled
     */
    public void mouseMoved(MouseEvent e) {
        fireMouseLocation(e);
        super.mouseMoved(e);
    }

    /**
     * Fires a mouse location to the InformationDelegator, and then calls the
     * super class method which calls the MouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mouseDragged(MouseEvent e) {
        fireMouseLocation(e);
        super.mouseDragged(e);
    }

    /**
     * If the MouseMode has been made inactive, clean out any input that might
     * have been made to the info line.
     */
    public void setActive(boolean active) {
        if (Debug.debugging("mousemode")) {
            Debug.output("CoordMouseMode(" + getPrettyName()
                    + "): made active (" + active + ")");
        }
        if (!active && infoDelegator != null) {
            infoDelegator.requestInfoLine(new InfoDisplayEvent(this, "", InformationDelegator.COORDINATE_INFO_LINE));
        }
    }

    /**
     * Sends the mouse event location, x/y and lat/lon, to the
     * InformationDelegator.
     */
    public void fireMouseLocation(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Point2D llp = null;
        Debug.message("mousemodedetail",
                "CoordMouseMode: firing mouse location");

        if (infoDelegator != null) {
            if (e.getSource() instanceof MapBean) {
                llp = ((MapBean) e.getSource()).getCoordinates(e);
            }
            String infoLine;
            infoLine = coordFormatter.createCoordinateInformationLine(x,
                    y,
                    llp,
                    this);

            // setup the info event
            InfoDisplayEvent info = new InfoDisplayEvent(this, infoLine, InformationDelegator.COORDINATE_INFO_LINE);
            // ask the infoDelegator to display the info
            infoDelegator.requestInfoLine(info);
        }
    }

    /**
     * Called when a CoordMouseMode is added to a BeanContext, or when another
     * object is added to the BeanContext after that. The CoordMouseMode looks
     * for an InformationDelegator to use to fire the coordinate updates. If
     * another InforationDelegator is added when one is already set, the later
     * one will replace the current one.
     * 
     * @param someObj an object being added to the BeanContext.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof InformationDelegator) {
            Debug.message("mousemode",
                    "NavMouseMode: found InformationDelegator");
            setInfoDelegator((InformationDelegator) someObj);
        }

        if (someObj instanceof CoordInfoFormatterHandler) {
            setCoordFormatterHandler((CoordInfoFormatterHandler) someObj);
        }
    }

    /**
     * BeanContextMembershipListener method. Called when objects have been
     * removed from the parent BeanContext. If an InformationDelegator is
     * removed from the BeanContext, and it's the same one that is currently
     * held, it will be removed.
     * 
     * @param someObj an object being removed from the BeanContext.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof InformationDelegator) {
            if (getInfoDelegator() == (InformationDelegator) someObj) {
                setInfoDelegator(null);
            }
        }

        if (someObj instanceof CoordInfoFormatterHandler
                && someObj == getCoordFormatterHandler()) {
            setCoordFormatterHandler(null);
        }
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
        String coordFormatterClassString = props.getProperty(realPrefix
                + CoordFormatterProperty);

        if (coordFormatterClassString != null) {
            Object obj = ComponentFactory.create(coordFormatterClassString,
                    prefix,
                    props);
            if (obj instanceof CoordInfoFormatter) {
                setCoordFormatter((CoordInfoFormatter) obj);
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        /**
         * Only act on behalf of coordFormatter if there's no
         * CoordInfoFormatterHandler. If there is, that object will take care of
         * the formatter's properties.
         */
        if (coordFormatter != null && coordFormatterHandler == null) {
            String prefix = PropUtils.getScopedPropertyPrefix(this);
            props.put(prefix + CoordFormatterProperty,
                    coordFormatter.getClass().getName());
            coordFormatter.getProperties(props);
        }

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        /**
         * Only act on behalf of coordFormatter if there's no
         * CoordInfoFormatterHandler. If there is, that object will take care of
         * the formatter's properties.
         */
        if (coordFormatter != null && coordFormatterHandler == null) {
            coordFormatter.getPropertyInfo(props);
        }

        return props;
    }

    public CoordInfoFormatter getCoordFormatter() {
        return coordFormatter;
    }

    public void setCoordFormatter(CoordInfoFormatter coordFormatter) {
        if (coordFormatter == null) {
            coordFormatter = new BasicCoordInfoFormatter();
        }
        this.coordFormatter = coordFormatter;
    }

    public CoordInfoFormatterHandler getCoordFormatterHandler() {
        return coordFormatterHandler;
    }

    public void setCoordFormatterHandler(
                                         CoordInfoFormatterHandler coordFormatterHandler) {
        if (this.coordFormatterHandler != null) {
            this.coordFormatterHandler.removePropertyChangeListener(CoordInfoFormatterHandler.FORMATTER_PROPERTY,
                    this);
        }

        this.coordFormatterHandler = coordFormatterHandler;

        if (coordFormatterHandler != null) {
            coordFormatterHandler.addPropertyChangeListener(CoordInfoFormatterHandler.FORMATTER_PROPERTY,
                    this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        CoordInfoFormatter newFormatter = (CoordInfoFormatter) evt.getNewValue();
        if (propertyName.equals(CoordInfoFormatterHandler.FORMATTER_PROPERTY)) {
            setCoordFormatter(newFormatter);
        }
    }

}
