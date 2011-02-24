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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OpenMapFrame.java,v $
// $RCSfile: OpenMapFrame.java,v $
// $Revision: 1.13 $
// $Date: 2006/02/27 15:11:34 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.util.PropUtils;

/**
 * The OpenMapFrame is the application window frame that holds the MapPanel, and
 * eventually the MapBean. It listens to the MapHandler for the addition of
 * Beans to the MapHandler BeanContext, and then positions the widgets it can
 * deal with within itself. The frame does not present itself until an MapPanel
 * is found.
 * 
 * <p>
 * The OpenMapFrame is intended to be used in an application environment. The
 * applet checks and code to handle the applet environment was moved to the
 * OpenMapApplet class.
 */
public class OpenMapFrame
        extends JFrame
        implements BeanContextMembershipListener, BeanContextChild, PropertyConsumer {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.gui.OpenMapFrame");

    /** Starting X coordinate of window, x */
    public static final String xProperty = "x";

    /** Starting Y coordinate of window, y */
    public static final String yProperty = "y";

    /**
     * The property to set the pixel width of the frame, width.
     */
    public static final String WidthProperty = "width";
    /**
     * The property to set the pixel height of the frame, height.
     */
    public static final String HeightProperty = "height";

    /**
     * The property to set the title of the frame, title.
     */
    public static final String TitleProperty = "title";

    /**
     * useAsInternalFrameRootPaneIfNecessary will tell the OpenMapFrame to set
     * its root pane as the Environment's desktop if the Environment has been
     * told to use internal frames, and if a root pane hasn't been set. True by
     * default.
     */
    protected boolean useAsInternalFrameRootPaneIfNecessary = true;

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    protected String propertyPrefix;
    protected int x = -1;
    protected int y = -1;
    protected int width = Integer.MAX_VALUE;
    protected int height = Integer.MAX_VALUE;
    boolean propsInitialized = false;

    /**
     * All OMComponentPanels have access to an I18n object, which is provided by
     * the Environment.
     */
    protected I18n i18n = Environment.getI18n();

    /**
     * Create the frame with "OpenMap <version>" in the title.
     */
    public OpenMapFrame() {
        this("");
    }

    /**
     * @param useAsInternalFrameRootPaneIfNecessary will tell the OpenMapFrame
     *        to set its root pane as the Environment's desktop if the
     *        Environment has been told to use internal frames, and if a root
     *        pane hasn't been set.
     */
    public OpenMapFrame(boolean useAsInternalFrameRootPaneIfNecessary) {
        this(Environment.get(Environment.Title), useAsInternalFrameRootPaneIfNecessary);
    }

    /**
     * Create a OpenMap frame with a title.
     * 
     * @param title The Frame title.
     */
    public OpenMapFrame(String title) {
        this(title, true);
    }

    /**
     * Create a OpenMap frame with a title, with a WindowListner that says what
     * to do when the OpenMapFrame is closed.
     * 
     * @param title The Frame title.
     * @param useAsInternalFrameRootPaneIfNecessary will tell the OpenMapFrame
     *        to set its root pane as the Environment's desktop if the
     *        Environment has been told to use internal frames, and if a root
     *        pane hasn't been set.
     */
    public OpenMapFrame(String title, boolean useAsInternalFrameRootPaneIfNecessary) {
        super(title);
        this.useAsInternalFrameRootPaneIfNecessary = useAsInternalFrameRootPaneIfNecessary;
    }

    /**
     * For applications, checks where the properties says the window should be
     * placed, and then uses the packed height and width to make adjustments.
     */
    protected void setPosition() {
        setPosition(getWidth(), getHeight());
    }

    protected void setPosition(int w, int h) {
        // get starting width and height
        pack();

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        logger.fine("Screen dimensions are " + d);

        if (w > d.width)
            w = d.width - d.width / 10;
        if (h > d.height)
            h = d.height - d.height / 10;

        if (x < 0)
            x = d.width / 2 - w / 2;
        if (y < 0)
            y = d.height / 2 - h / 2;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Setting window bounds from " + x + ", " + y + " for size " + w + ", " + h);
        }

        // compose the frame, but don't show it here
        // contentPane.setBounds(x, y, w, h);
        setBounds(x, y, w, h);
    }

    /**
     * Called when the OpenMapFrame is added to a BeanContext, and when other
     * objects are added to the BeanContext. The OpenMapFrame looks for objects
     * that it knows how to place upon itself (MapPanel, JMenuBar). The
     * OpenMapFrame does not check to see if the objects looked for are already
     * added to itself. It assumes that if some object type is getting added to
     * it, the caller must know what they are doing - just like a regular
     * JFrame.
     * 
     * @param it Iterator to use to go through the BeanContext objects.
     */
    public void findAndInit(Iterator<?> it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * Called when an object is added to the MapHandler.
     */
    public void findAndInit(Object someObj) {

        considerForContent(someObj);

        // We shouldn't find this if we've already defined one
        // in the MapPanel, but we have this for backward
        // compatibility.
        if (someObj instanceof JMenuBar) {
            logger.fine("OpenMapFrame: Found a MenuBar");
            getRootPane().setJMenuBar((JMenuBar) someObj);
            invalidate();
        }

        // Only do this if the properties haven't been set on the frame, yet.
        if (someObj instanceof PropertyHandler && !propsInitialized) {
            setProperties(((PropertyHandler) someObj).getProperties());
        }
    }

    /**
     * Method used to determine if an object should be added as content. Method
     * is here to make it easier for subclasses to override and add what they
     * want to content pane.
     * 
     * @param someObj
     */
    public void considerForContent(Object someObj) {
        if (someObj instanceof MapPanel && someObj instanceof Component && getContentPane().getComponentCount() == 0) {
            logger.fine("Found a MapPanel");
            setContent((Component) someObj);
        }
    }

    /**
     * Called with the MapPanel to be set in the Content Pane of this Frame. If
     * a MapPanel, a JMenuBar will be retrieved and added as well.
     * 
     * @param component component to be used as content.
     */
    public void setContent(Component component) {
        getContentPane().add((Component) component);

        if (component instanceof MapPanel) {
            MapPanel mapPanel = (MapPanel) component;

            JMenuBar jmb = mapPanel.getMapMenuBar();
            if (jmb != null) {
                logger.fine("OpenMapFrame: Got MenuBar from MapPanel");
                getRootPane().setJMenuBar(jmb);
            }
        }

        setPosition(width, height);
        invalidate();
        pack();
        setVisible(true);
    }

    /**
     * BeanContextMembership interface method. Called when objects are added to
     * the BeanContext.
     * 
     * @param bcme contains an Iterator that lets you go through the new
     *        objects.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        findAndInit(bcme.iterator());
    }

    /**
     * BeanContextMembership interface method. Called by BeanContext when
     * children are being removed. Unhooks itself from the objects that are
     * being removed if they are contained within the Frame.
     * 
     * @param bcme event that contains an Iterator to use to go through the
     *        removed objects.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        Iterator<?> it = bcme.iterator();
        while (it.hasNext()) {
            findAndUndo(it.next());
        }
    }

    /**
     * Called when an object is removed from the MapHandler.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapPanel && someObj instanceof Container) {
            logger.fine("OpenMapFrame: MapBean is being removed from frame");
            getContentPane().remove((Container) someObj);

            if (getJMenuBar() == ((MapPanel) someObj).getMapMenuBar()) {
                logger.fine("OpenMapFrame: Menu Bar is being removed");
                setJMenuBar(null);
            }
        }

        if (someObj instanceof JMenuBar) {
            if (getJMenuBar() == (JMenuBar) someObj) {
                logger.fine("OpenMapFrame: Menu Bar is being removed");
                setJMenuBar(null);
            }
        }

        if (this.equals(someObj)) {
            dispose();
        }
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
        if (beanContextChildSupport != null) {
            return beanContextChildSupport.getBeanContext();
        }

        return null;
    }

    /**
     * Method for BeanContextChild interface.
     * 
     * @param in_bc The context to which this object is being added
     */
    public void setBeanContext(BeanContext in_bc)
            throws PropertyVetoException {
        if (in_bc != null) {
            in_bc.addBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(in_bc);
            findAndInit(in_bc.iterator());
        }
    }

    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /** Method for BeanContextChild interface. */
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
        beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
    }

    // Implementation of PropertyConsumer Interface
    /**
     * Method to set the properties in the PropertyConsumer. It is assumed that
     * the properties do not have a prefix associated with them, or that the
     * prefix has already been set.
     * 
     * @param setList a properties object that the PropertyConsumer can use to
     *        retrieve expected properties it can use for configuration.
     */
    public void setProperties(Properties setList) {
        setProperties(null, setList);
    }

    /**
     * Method to set the properties in the PropertyConsumer. The prefix is a
     * string that should be prepended to each property key (in addition to a
     * separating '.') in order for the PropertyConsumer to uniquely identify
     * properties meant for it, in the midst of of Properties meant for several
     * objects.
     * 
     * @param prefix a String used by the PropertyConsumer to prepend to each
     *        property value it wants to look up -
     *        setList.getProperty(prefix.propertyKey). If the prefix had already
     *        been set, then the prefix passed in should replace that previous
     *        value.
     * @param setList a Properties object that the PropertyConsumer can use to
     *        retrieve expected properties it can use for configuration.
     */
    public void setProperties(String prefix, Properties setList) {
        propsInitialized = true;
        setPropertyPrefix(prefix);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        x = PropUtils.intFromProperties(setList, prefix + xProperty, x);
        y = PropUtils.intFromProperties(setList, prefix + yProperty, y);
        width = PropUtils.intFromProperties(setList, prefix + WidthProperty, width);
        height = PropUtils.intFromProperties(setList, prefix + HeightProperty, height);

        setTitle(setList.getProperty(prefix + TitleProperty, getTitle()));

        if (getContentPane().getComponentCount() > 0) {
            logger.fine("setting window dimensions");
            setPosition(width, height);
        }

        if (useAsInternalFrameRootPaneIfNecessary) {
            boolean useInternalFrames = PropUtils.booleanFromProperties(setList, Environment.UseInternalFrames, false);

            if (useInternalFrames && Environment.getInternalFrameDesktop() == null) {
                logger.fine("Setting OpenMapFrame as internal pane.");
                Environment.useInternalFrames(getRootPane());
            }
        }

    }

    /**
     * Method to fill in a Properties object, reflecting the current values of
     * the PropertyConsumer. If the PropertyConsumer has a prefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
     * 
     * @param getList a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new Properties
     *        object should be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        getList.setProperty(xProperty, "" + getBounds().x);
        getList.setProperty(yProperty, "" + getBounds().y);
        getList.setProperty(Environment.Width, Integer.toString(getWidth()));
        getList.setProperty(Environment.Height, Integer.toString(getHeight()));

        return getList;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.).
     * 
     * @param list a Properties object to load the PropertyConsumer properties
     *        into. If getList equals null, then a new Properties object should
     *        be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }

        list.setProperty("x", "Starting X coordinate of window");
        list.setProperty("y", "Starting Y coordinate of window");

        return list;
    }

    /**
     * Doesn't do anything. The OpenMapFrame looks for properties set with the
     * "openmap" property prefix. This method is part of the PropertyConsumer
     * interface.
     * 
     * @param prefix the prefix String.
     */
    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to the property
     * keys for Properties lookups.
     * 
     * @return the property prefix for the frame
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    public void setUseAsInternalFrameRootPaneIfNecessary(boolean val) {
        useAsInternalFrameRootPaneIfNecessary = true;
    }

    public boolean getUseAsInternalFrameRootPaneIfNecessary() {
        return useAsInternalFrameRootPaneIfNecessary;
    }

    /**
     * Calls dispose on the BeanContext (MapHandler) and then removes references
     * to other children.
     */
    public void dispose() {

        MapHandler mh = ((MapHandler) getBeanContext());
        if (mh != null) {
            mh.dispose();
        }

        beanContextChildSupport = null;

        getContentPane().removeAll();
        JMenuBar jmb = getJMenuBar();
        if (jmb != null) {
            jmb.removeAll();
        }
        setJMenuBar(null);
        getRootPane().remove(this);
        super.dispose();
    }
}