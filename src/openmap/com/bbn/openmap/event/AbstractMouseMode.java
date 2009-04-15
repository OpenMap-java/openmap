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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/AbstractMouseMode.java,v $
// $RCSfile: AbstractMouseMode.java,v $
// $Revision: 1.15 $
// $Date: 2008/10/16 19:33:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.OptionPropertyEditor;

/**
 * Base class of the MouseModes. It takes care of the administrative aspects of
 * being a mouse mode, but does not respond to MouseEvents.
 * <p>
 * The ID and pretty name can be set in the properties file.
 * 
 * <pre>
 * 
 * 
 *    # Name that layers use to get events from this mode
 *    mousemode.id=ID
 *    # Tooltip and Menu name for mode
 *    mousemode.prettyName=Display Name
 * 
 * 
 * 
 * 
 * </pre>
 * 
 * This class delegates much of the work of managing its listeners to a
 * MapMouseSupport object.
 * 
 * @see MapMouseSupport
 */
public class AbstractMouseMode extends OMComponent implements MapMouseMode,
        Serializable {

    /**
     * The identifier for the mode, which is also the name that will be used in
     * a used interface describing the mode to a user.
     */
    protected String ID = null;

    /**
     * The object used to handle the listeners and to pass out the event to the
     * layers interesed in it.
     */
    protected MapMouseSupport mouseSupport;

    /**
     * The cursor that appears on the map when this Mouse Mode is active.
     */
    protected Cursor cursor = Cursor.getDefaultCursor();

    /**
     * The Icon that can be used in a GUI. Can be null. The class will look for
     * a resource gif file that has the same ID string - Navigation.gif for the
     * NavMouseMode, for instance.
     */
    protected transient Icon guiIcon = null;

    protected transient boolean visible = true;

    protected boolean mouseWheelListener = true;

    protected String prettyName;

    protected String iconName;

    protected boolean zoomWhenMouseWheelUp = ZOOM_IN;

    /**
     * Zoom direction in when mouse wheel rotated up.
     */
    public static final boolean ZOOM_IN = true;
    /**
     * Zoom direction out when mouse wheel rotated up.
     */
    public static final boolean ZOOM_OUT = false;

    /**
     * The MouseModeID to use for a particular instance of a MapMouseMode. If
     * not set, the default mouse mode ID of the MapMouseMode will be used.
     */
    public static final String IDProperty = "id";

    /**
     * The String to use for a key lookup in a Properties object to find the
     * name to use in a GUI relating to this Mouse Mode.
     */
    public static final String PrettyNameProperty = "prettyName";

    /**
     * The java.awt.Cursor id that should be used for the mouse mode.
     * 
     * @see java.awt.Cursor
     */
    public static final String CursorIDProperty = "cursorID";

    /**
     * A property that lets you specify the resource to use for the icon for the
     * MouseMode.
     */
    public static final String IconProperty = "icon";

    /**
     * A property that lets you specify if the mode zooms in or out when the
     * mouse wheel is rotated up. Appropriate values are ZOOM_IN or ZOOM_OUT.
     */
    public static final String MouseWheelZoomProperty = "mouseWheelUp";

    /**
     * A property that lets you turn off the mouse wheel listening
     * functionality. If enabled, the mouse wheel changes the scale of the map.
     */
    public static final String MouseWheelListenerProperty = "mouseWheelListener";

    /**
     * Construct an AbstractMouseMode. Default constructor, allocates the mouse
     * support object.
     */
    public AbstractMouseMode() {
        this("Unnamed Mode", true);
    }

    /**
     * Construct an AbstractMouseMode.
     * 
     * @param name the ID of the mode.
     * @param shouldConsumeEvents if true, events are propagated to the first
     *        MapMouseListener that successfully processes the event, if false,
     *        events are propagated to all MapMouseListeners
     */
    public AbstractMouseMode(String name, boolean shouldConsumeEvents) {
        mouseSupport = new MapMouseSupport(this, shouldConsumeEvents);
        ID = name;
        setIconName(name + ".gif");
    }

    /**
     * Internal callback method that lets subclasses override a class to use as
     * a resource point for icon image retrieval.
     * 
     * @return Class that has icon image file next to it in classpath.
     */
    protected Class<?> getClassToUseForIconRetrieval() {
        return getClass();
    }

    /**
     * Sets the GUI icon based on the name of the resource provided. The
     * resource will be checked against the classpath, and if it isn't found,
     * the mouse mode will be asked for the class to use for icon retrieval.
     * 
     * @param iName
     */
    public void setIconName(String iName) {
        iconName = iName;
        java.net.URL url = null;

        try {
            url = PropUtils.getResourceOrFileOrURL(iName);
        } catch (MalformedURLException murle) {

        }

        if (url == null) {
            url = getClassToUseForIconRetrieval().getResource(iconName);
        }

        if (url != null) {
            guiIcon = new ImageIcon(url);
        }
    }

    public String getIconName() {
        return iconName;
    }

    /**
     * Returns the id (mode name).
     * 
     * @return String ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Set the id (mode name).
     * 
     * @param id string that identifies the delegate.
     */
    public void setID(String id) {
        ID = id;
    }

    public void setPrettyName(String pn) {
        prettyName = pn;
    }

    /**
     * Return a pretty name, suitable for the GUI. If set, is independent of the
     * mode ID. If not set, is the same as the mode ID.
     */
    public String getPrettyName() {
        if (prettyName == null) {
            return i18n.get(this.getClass(), PrettyNameProperty, ID);
        } else {
            return prettyName;
        }
    }

    /**
     * Gets the mouse cursor recommended for use when this mouse mode is active.
     * 
     * @return Cursor the mouse cursor recommended for use when this mouse mode
     *         is active.
     */
    public Cursor getModeCursor() {
        return cursor;
    }

    /**
     * Sets the cursor that is recommended for use on the map when this mouse
     * mode is active.
     * 
     * @param curs the cursor that is recommended for use on the map when this
     *        mouse mode is active.
     */
    public void setModeCursor(Cursor curs) {
        cursor = curs;
    }

    /**
     * Sets the cursor that is recommended for use on the map when this mouse
     * mode is active.
     * 
     * @param cursorID the cursor ID member variable string, i.e. DEFAULT_CURSOR
     * @see java.awt.Cursor
     */
    public void setModeCursor(String cursorID) {
        if (cursorID != null) {

            try {
                int cid = java.awt.Cursor.class.getField(cursorID).getInt(null);

                setModeCursor(Cursor.getPredefinedCursor(cid));

            } catch (NoSuchFieldException nsfe) {
            } catch (IllegalAccessException iae) {
            }
        }
    }

    /**
     * Gets the Icon to represent the Mouse Mode in a GUI. May be null.
     */
    public Icon getGUIIcon() {
        return guiIcon;
    }

    /**
     * Set the icon that should be used for this Mouse Mode in a GUI.
     */
    public void setGUIIcon(Icon icon) {
        guiIcon = icon;
    }

    /**
     * Sets how the delegate passes out events. If the value passed in is true,
     * the delegate will only pass the event to the first listener that can
     * respond to the event. If false, the delegate will pass the event on to
     * all its listeners.
     * 
     * @param value true for limited distribution.
     */
    public void setConsumeEvents(boolean value) {
        mouseSupport.setConsumeEvents(value);
    }

    /**
     * Returns how the delegate (and it's mouse support) is set up to distribute
     * events.
     * 
     * @return true if only one listner gets to act on an event.
     */
    public boolean isConsumeEvents() {
        return mouseSupport.isConsumeEvents();
    }

    public boolean isZoomWhenMouseWheelUp() {
        return zoomWhenMouseWheelUp;
    }

    public void setZoomWhenMouseWheelUp(boolean zoomWhenMouseWheelUp) {
        this.zoomWhenMouseWheelUp = zoomWhenMouseWheelUp;
    }

    /**
     * Add a MapMouseListener to the MouseMode. The listener will then get
     * events from the delegator if the delegator is active.
     * 
     * @param l the MapMouseListener to add.
     */
    public void addMapMouseListener(MapMouseListener l) {
        mouseSupport.addMapMouseListener(l);
    }

    /**
     * Remove a MapMouseListener from the MouseMode.
     * 
     * @param l the MapMouseListener to remove.
     */
    public void removeMapMouseListener(MapMouseListener l) {
        mouseSupport.removeMapMouseListener(l);
    }

    /**
     * Remove all MapMouseListeners from the mode.
     */
    public void removeAllMapMouseListeners() {
        mouseSupport.removeAllMapMouseListeners();
    }

    /**
     * Invoked when the mouse has been clicked on a component. Calls
     * fireMapMouseClicked on MouseSupport.
     * 
     * @param e MouseEvent
     */
    public void mouseClicked(MouseEvent e) {
        mouseSupport.fireMapMouseClicked(e);
    }

    /**
     * Invoked when a mouse button has been pressed on a component. Calls
     * fiewMapMousePressed on the MouseSupport. Also requests focus on the
     * source of the MouseEvent, so that key events can be processed.
     * 
     * @param e MouseEvent
     */
    public void mousePressed(MouseEvent e) {
        e.getComponent().requestFocus();
        mouseSupport.fireMapMousePressed(e);
    }

    /**
     * Invoked when a mouse button has been released on a component. Calls
     * fireMapMouseReleased on the MouseSupport.
     * 
     * @param e MouseEvent
     */
    public void mouseReleased(MouseEvent e) {
        mouseSupport.fireMapMouseReleased(e);
    }

    /**
     * Invoked when the mouse enters a component. Calls fireMapMouseEntered on
     * the MouseSupport.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        mouseSupport.fireMapMouseEntered(e);
    }

    /**
     * Invoked when the mouse exits a component. This does nothing. Extend this
     * class to add functionality.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
        mouseSupport.fireMapMouseExited(e);
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * Calls fireMapMouseDragged on the MouseSupport.
     * 
     * @param e MouseEvent
     */
    public void mouseDragged(MouseEvent e) {
        mouseSupport.fireMapMouseDragged(e);
    }

    /**
     * Invoked when the mouse button has been moved on a component (with no
     * buttons no down). Calls fireMapMouseMoved on the MouseSupport.
     * 
     * @param e MouseEvent
     */
    public void mouseMoved(MouseEvent e) {
        mouseSupport.fireMapMouseMoved(e);
    }

    /**
     * Invoked from the MouseWheelListener interface.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (mouseWheelListener) {
            int rot = e.getWheelRotation();
            if (e.getSource() instanceof MapBean) {
                MapBean mb = (MapBean) e.getSource();
                if (rot > 0) {
                    // Positive, zoom out
                    mb.zoom(new ZoomEvent(mb, ZoomEvent.RELATIVE, 1.1f));
                } else {
                    mb.zoom(new ZoomEvent(mb, ZoomEvent.RELATIVE, .9f));
                }
            }
        }
    }

    /**
     * Check setting for whether MouseMode responds to mouse wheel events.
     * 
     * @return
     */
    public boolean isMouseWheelListener() {
        return mouseWheelListener;
    }

    /**
     * Set whether MouseMode responds to mouse wheel events.
     * 
     * @param mouseWheelListener
     */
    public void setMouseWheelListener(boolean mouseWheelListener) {
        this.mouseWheelListener = mouseWheelListener;
    }

    /**
     * Part of the MapMouseMode interface. Called when the MouseMode is made
     * active or inactive.
     * 
     * @param active true if the mode has been made active, false if it has been
     *        made inactive.
     */
    public void setActive(boolean active) {}

    /**
     * Set a MouseSupport explicitly.
     * 
     * @param support The new MapMouseSupport instance
     */
    public void setMouseSupport(MapMouseSupport support) {
        mouseSupport = support;
    }

    /**
     * Get the MouseSupport.
     * 
     * @return the MapMouseSupport used by the MouseMode.
     */
    public MapMouseSupport getMouseSupport() {
        return mouseSupport;
    }

    /**
     * Method to let the MouseDelegator know if the MapMouseMode should be
     * visible, as opposed to a MapMouseMode that is being provided and
     * controlled by another tool. True by default.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Method to set if the MapMouseMode should be visible, as opposed to a
     * MapMouseMode that is being provided and controlled by another tool.
     */
    public void setVisible(boolean value) {
        visible = value;
    }

    /**
     * Request to have the parent MapMouseMode act as a proxy for a MapMouseMode
     * that wants to remain hidden. Can be useful for directing events to one
     * object. This version sets the proxy distribution mask to zero, which
     * means that none of this support objects targets will be notified of
     * events.
     * 
     * @param mmm the hidden MapMouseMode for this MapMouseMode to send events
     *        to.
     * @return true if the proxy setup (essentially a lock) is successful, false
     *         if the proxy is already set up for another listener.
     */
    public boolean actAsProxyFor(MapMouseMode mmm) {
        return actAsProxyFor(mmm, 0);
    }

    /**
     * Request to have the MapMouseMode act as a proxy for a MapMouseMode that
     * wants to remain hidden. Can be useful for directing events to one object.
     * 
     * @param mmm the hidden MapMouseMode for this MapMouseMode to send events
     *        to.
     * @param pdm the proxy distribution mask to use, which lets this support
     *        object notify its targets of events if the parent is acting as a
     *        proxy.
     * @return true if the proxy setup (essentially a lock) is successful, false
     *         if the proxy is already set up for another listener.
     */
    public boolean actAsProxyFor(MapMouseMode mmm, int pdm) {
        return mouseSupport.setProxyFor(mmm, pdm);
    }

    /**
     * Can check if the MapMouseMode is acting as a proxy for another
     * MapMouseMode.
     */
    public boolean isProxyFor(MapMouseMode mmm) {
        return mouseSupport.isProxyFor(mmm);
    }

    /**
     * Release the proxy lock on the MapMouseMode.
     */
    public void releaseProxy() {
        mouseSupport.releaseProxy();
    }

    /**
     * Set the mask that dictates which events get sent to this support object's
     * targets even if the parent mouse mode is acting as a proxy.
     */
    public void setProxyDistributionMask(int mask) {
        mouseSupport.setProxyDistributionMask(mask);
    }

    /**
     * Get the mask that dictates which events get sent to this support object's
     * targets even if the parent mouse mode is acting as a proxy.
     */
    public int getProxyDistributionMask() {
        return mouseSupport.getProxyDistributionMask();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String prettyNameString = props.getProperty(prefix + PrettyNameProperty);
        if (prettyNameString != null) {
            setPrettyName(prettyNameString);
        }

        String idString = props.getProperty(prefix + IDProperty);
        if (idString != null) {
            setID(idString);
        }

        setModeCursor(props.getProperty(prefix + CursorIDProperty));

        String iconString = props.getProperty(prefix + IconProperty);
        if (iconString != null) {
            setIconName(iconString);
        }

        mouseWheelListener = PropUtils.booleanFromProperties(props, prefix
                + MouseWheelListenerProperty, mouseWheelListener);

        zoomWhenMouseWheelUp = PropUtils.booleanFromProperties(props, prefix
                + MouseWheelZoomProperty, zoomWhenMouseWheelUp);

        String zwmwu = props.getProperty(prefix + MouseWheelZoomProperty);
        if (zwmwu != null) {
            try {
                boolean zSetting = getClass().getField(zwmwu).getBoolean(null);
                setZoomWhenMouseWheelUp(zSetting);
            } catch (NoSuchFieldException nsfe) {
            } catch (IllegalAccessException iae) {
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        if (prettyName != null) {
            props.put(prefix + PrettyNameProperty, prettyName);
        }

        props.put(prefix + IDProperty, getID());

        int cursorType = getModeCursor().getType();

        Field[] cFields = Cursor.class.getFields();
        for (int i = 0; i < cFields.length; i++) {
            Field f = cFields[i];

            String name = f.getName();
            if (name.endsWith("_CURSOR")) {
                try {
                    int testType = f.getInt(null);
                    if (testType == cursorType) {
                        props.put(prefix + CursorIDProperty, name);
                        break;
                    }
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            }
        }

        if (zoomWhenMouseWheelUp) {
            props.put(prefix + MouseWheelZoomProperty, "ZOOM_IN");
        } else {
            props.put(prefix + MouseWheelZoomProperty, "ZOOM_OUT");
        }

        props.put(prefix + MouseWheelListenerProperty,
                Boolean.toString(mouseWheelListener));

        props.put(prefix + IconProperty, PropUtils.unnull(getIconName()));

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        Class<?> thisClass = getClass();
        String internString = i18n.get(thisClass,
                PrettyNameProperty,
                I18n.TOOLTIP,
                "Presentable name for Mouse Mode.");
        props.put(Layer.AddToBeanContextProperty, internString);
        internString = i18n.get(thisClass, PrettyNameProperty, "Name");
        props.put(PrettyNameProperty + LabelEditorProperty, internString);

        internString = i18n.get(thisClass,
                IDProperty,
                I18n.TOOLTIP,
                "Internal ID for Mouse Mode, used by Layers.");
        props.put(Layer.AddToBeanContextProperty, internString);
        internString = i18n.get(thisClass, IDProperty, "ID");
        props.put(IDProperty + LabelEditorProperty, internString);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                thisClass,
                IconProperty,
                "Icon",
                "Icon to use for mouse mode.",
                null);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                thisClass,
                MouseWheelZoomProperty,
                "Mouse Wheel Zoom Direction",
                "Action to take when the mouse wheel is rolled up.",
                "com.bbn.openmap.util.propertyEditor.ComboBoxPropertyEditor");
        props.put(MouseWheelZoomProperty
                + OptionPropertyEditor.ScopedOptionsProperty, "zoomin  zoomout");
        props.put(MouseWheelZoomProperty + ".zoomin", "ZOOM_IN");
        props.put(MouseWheelZoomProperty + ".zoomout", "ZOOM_OUT");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                thisClass,
                MouseWheelListenerProperty,
                "Mouse Wheel Zoom",
                "Setting for whether mouse wheel controls map zoom",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                thisClass,
                CursorIDProperty,
                "Cursor",
                "Cursor to use for this mouse mode.",
                "com.bbn.openmap.util.propertyEditor.ComboBoxPropertyEditor");

        StringBuffer cOptions = new StringBuffer();
        Field[] cFields = Cursor.class.getFields();
        for (int i = 0; i < cFields.length; i++) {
            Field f = cFields[i];

            String name = f.getName();
            if (name.endsWith("_CURSOR")) {
                String cName = f.getName();
                props.put(CursorIDProperty + "." + cName, cName);
                cOptions.append(" " + cName);
            }
        }

        props.put(CursorIDProperty + OptionPropertyEditor.ScopedOptionsProperty,
                cOptions.toString().trim());

        return props;
    }

    /**
     * PaintListener interface, notifying the MouseMode that the MapBean has
     * repainted itself. Useful if the MouseMode is drawing stuff.
     */
    public void listenerPaint(java.awt.Graphics g) {}

    public static class MouseWheelZoomEditor {
        public MouseWheelZoomEditor() {

        }
    }
}