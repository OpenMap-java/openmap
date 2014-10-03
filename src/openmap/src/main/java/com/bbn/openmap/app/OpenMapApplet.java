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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/app/OpenMapApplet.java,v $
// $RCSfile: OpenMapApplet.java,v $
// $Revision: 1.9 $
// $Date: 2005/07/29 19:17:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.app;

import java.awt.Container;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

import javax.swing.JApplet;
import javax.swing.JMenuBar;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.gui.MapPanel;
import com.bbn.openmap.util.Debug;

/**
 * OpenMap Applet. Uses the MapHandler, via BeanContextMembershipListener
 * methods to lay out the MapPanel and JMenuBar. Creates a PropertyHandler that
 * will look for the openmap.properties file in the codebase. If the
 * 
 * <pre>
 * PROPERTIES
 * </pre>
 * 
 * applet parameter is specified with a different properties file, that file
 * will be used instead.
 */
public class OpenMapApplet
        extends JApplet
        implements BeanContextMembershipListener, BeanContextChild {

    public final static String PropertiesProperty = "PROPERTIES";

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    // pinfo used to have these parameters, too, but that doesn't
    // seem right to include visibroker arguments in the generic
    // applet parameter info.
    // {"ORBdisableLocator", "boolean", "disable Visiborker
    // Gatekeeper"},
    // {"ORBgatekeeperIOR", "boolean", "URL to gatekeeper IOR."},

    protected final String pinfo[][] = {
        {
            Environment.Latitude,
            "float",
            "Starting center latitude"
        },
        {
            Environment.Longitude,
            "float",
            "Starting center longitude"
        },
        {
            Environment.Scale,
            "float",
            "Starting Scale"
        },
        {
            Environment.Projection,
            "String",
            "Default projection type"
        },
        {
            "debug.basic",
            "none",
            "enable basic debugging"
        },
        {
            Environment.HelpURL,
            "String",
            "URL location of OpenMap help pages"
        }
    };

    /**
     * Returns information about this applet.
     * <p>
     * 
     * @return a string containing information about the author, version, and
     *         copyright of the applet.
     * @since JDK1.0
     */
    public String getAppletInfo() {
        return MapBean.getCopyrightMessage();
    }

    /**
     * Returns information about the parameters that are understood by this
     * applet.
     * <p>
     * Each element of the array should be a set of three <code>Strings</code>
     * containing the name, the type, and a description. For example:
     * <p>
     * <blockquote>
     * 
     * <pre>
     * String pinfo[][] = {
     *     {
     *         &quot;fps&quot;,
     *         &quot;1-10&quot;,
     *         &quot;frames per second&quot;
     *     },
     *     {
     *         &quot;repeat&quot;,
     *         &quot;boolean&quot;,
     *         &quot;repeat image loop&quot;
     *     },
     *     {
     *         &quot;imgs&quot;,
     *         &quot;url&quot;,
     *         &quot;images directory&quot;
     *     }
     * };
     * </pre>
     * 
     * </blockquote>
     * <p>
     * 
     * @return an array describing the parameters this applet looks for.
     * @since JDK1.0
     */
    public String[][] getParameterInfo() {
        return pinfo;
    }

    /**
     * Called by the browser or applet viewer to inform this applet that it has
     * been loaded into the system. It is always called before the first time
     * that the <code>start</code> method is called.
     * <p>
     * The implementation of this method provided by the <code>Applet</code>
     * class does nothing.
     * 
     * @see java.applet.Applet#destroy()
     * @see java.applet.Applet#start()
     * @see java.applet.Applet#stop()
     * @since JDK1.0
     */
    public void init() {
        // Initialize as an applet
        Environment.init(this);
        Debug.init(this, new String[] {
            "debug.basic",
            "debug.cspec",
            "debug.layer",
            "debug.mapbean",
            "debug.plugin"
        });

        String propValue = getParameter(PropertiesProperty);
        PropertyHandler propHandler = null;

        try {
            if (propValue != null) {
                PropertyHandler.Builder builder = new PropertyHandler.Builder().setPropertiesFile(propValue);
                propHandler = new PropertyHandler(builder);
                if (Debug.debugging("app")) {
                    Debug.output("OpenMapApplet: Using properties from " + propValue);
                }
            }
        } catch (MalformedURLException murle) {
            Debug.error("OpenMap: property file specified: " + propValue
                    + " doesn't exist, searching for default openmap.properties file...");
        } catch (IOException ioe) {
            Debug.error("OpenMap: There is a problem using the property file specified: " + propValue
                    + ", searching for default openmap.properties file...");
        }

        if (propHandler == null) {
            propHandler = new PropertyHandler();
        }

        MapPanel mapPanel = new BasicMapPanel(propHandler);
        mapPanel.getMapHandler().add(this);
        Debug.message("app", "OpenMapApplet.init()");
    }

    /**
     * Called by the browser or applet viewer to inform this applet that it
     * should start its execution. It is called after the <code>init</code>
     * method and each time the applet is revisited in a Web page.
     * <p>
     * 
     * @see java.applet.Applet#destroy()
     * @see java.applet.Applet#init()
     * @see java.applet.Applet#stop()
     * @since JDK1.0
     */
    public void start() {
        Debug.message("app", "OpenMapApplet.start()");
        super.start();
    }

    /**
     * Called by the browser or applet viewer to inform this applet that it
     * should stop its execution. It is called when the Web page that contains
     * this applet has been replaced by another page, and also just before the
     * applet is to be destroyed.
     * <p>
     * 
     * @see java.applet.Applet#destroy()
     * @see java.applet.Applet#init()
     * @since JDK1.0
     */
    public void stop() {
        Debug.message("app", "OpenMapApplet.stop()");
        super.stop();
    }

    /**
     * Called by the browser or applet viewer to inform this applet that it is
     * being reclaimed and that it should destroy any resources that it has
     * allocated. The <code>stop</code> method will always be called before
     * <code>destroy</code>.
     * <p>
     * 
     * @see java.applet.Applet#init()
     * @see java.applet.Applet#start()
     * @see java.applet.Applet#stop()
     * @since JDK1.0
     */
    public void destroy() {
        Debug.message("app", "OpenMapApplet.destroy()");
        super.destroy();
    }

    /**
     * The method called by BeanContextMembershipListener methods to find
     * components in the MapHandler.
     */
    public void findAndInit(Iterator it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * Called when an object is added to the MapHandler.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MapPanel && someObj instanceof Container) {
            getContentPane().add((Container) someObj);

            JMenuBar jmb = ((MapPanel) someObj).getMapMenuBar();
            if (jmb != null) {
                Debug.message("basic", "OpenMapApplet: Got MenuBar from MapPanel");
                getRootPane().setJMenuBar(jmb);
            }

            invalidate();
        }

        if (someObj instanceof JMenuBar) {
            getRootPane().setJMenuBar((JMenuBar) someObj);
            invalidate();
        }
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
        for (Iterator it = bcme.iterator(); it.hasNext();) {
            findAndUndo(it.next());
        }
    }

    /**
     * Called when an object is removed from the MapHandler.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapPanel && someObj instanceof Container) {
            Debug.message("basic", "OpenMapApplet: MapPanel is being removed from applet");
            getContentPane().remove((Container) someObj);

            if (getJMenuBar() == ((MapPanel) someObj).getMapMenuBar()) {
                Debug.message("basic", "OpenMapApplet: Menu Bar is being removed");
                setJMenuBar(null);
            }
        }

        if (someObj instanceof JMenuBar) {
            if (getJMenuBar() == (JMenuBar) someObj) {
                Debug.message("basic", "OpenMapApplet: MenuBar is being removed from applet");
                setJMenuBar(null);
            }
        }
    }

    /** Method for BeanContextChild interface. */
    public BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
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
}