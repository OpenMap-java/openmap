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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/app/OpenMapApplet.java,v $
// $RCSfile: OpenMapApplet.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.app;

import java.awt.BorderLayout;
import java.awt.Color;

import java.io.*;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import com.bbn.openmap.*;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;

/**
 * OpenMap Applet
 *
 */
public class OpenMapApplet extends JApplet {
    
    protected final String pinfo[][] = {
	{Environment.Latitude, "float", "Starting center latitude"},
	{Environment.Longitude, "float", "Starting center longitude"},
	{Environment.Scale, "float", "Starting Scale"},
	{Environment.Projection, "String", "Default projection type"},
	{"ORBdisableLocator", "boolean", "disable Visiborker Gatekeeper"},
	{"ORBgatekeeperIOR", "boolean", "URL to gatekeeper IOR."},
 	{"debug.basic", "none", "enable basic debugging"},
	{Environment.HelpURL, "String", "URL location of OpenMap help pages"}
    };

    /**
     * Returns information about this applet.<p>
     *
     * @return  a string containing information about the author, version, and
     *          copyright of the applet.
     * @since   JDK1.0
     */
    public String getAppletInfo() {
	return MapBean.getCopyrightMessage();
    }


    /**
     * Returns information about the parameters that are understood by 
     * this applet.
     * <p>
     * Each element of the array should be a set of three 
     * <code>Strings</code> containing the name, the type, and a 
     * description. For example:
     * <p><blockquote><pre>
     * String pinfo[][] = {
     *	 {"fps",    "1-10",    "frames per second"},
     *	 {"repeat", "boolean", "repeat image loop"},
     *	 {"imgs",   "url",     "images directory"}
     * };
     * </pre></blockquote>
     * <p>
     *
     * @return  an array describing the parameters this applet looks for.
     * @since   JDK1.0
     */
    public String[][] getParameterInfo() {
	return pinfo;
    }


    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it has been loaded into the system. It is always 
     * called before the first time that the <code>start</code> method is 
     * called. 
     * <p>
     * The implementation of this method provided by the 
     * <code>Applet</code> class does nothing. 
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#start()
     * @see     java.applet.Applet#stop()
     * @since   JDK1.0
     */
    public void init() {
      //OpenMapOld.init(this);// initialize Environment and debugging
        // Initialize as an applet
            Environment.init(this);
	    Debug.init(this,
		       new String[] {"debug.basic",
				     "debug.cspec",
				     "debug.layer",
				     "debug.mapbean",
				     "debug.plugin"
		       });
	    
	PropertyHandler propertyHandler = new PropertyHandler();
	MapBean mapBean = new BufferedMapBean();
	mapBean.setBorder(new BevelBorder(BevelBorder.LOWERED));

	// Initialize the map projection, scale, center with user prefs or
	// defaults
	String projName = Environment.get(Environment.Projection, 
					  Mercator.MercatorName);
	int projType = ProjectionFactory.getProjType(projName);
	mapBean.setProjectionType(projType);
	mapBean.setScale(Environment.getFloat(Environment.Scale,
					      Float.POSITIVE_INFINITY));
	mapBean.setCenter(new LatLonPoint(
	    Environment.getFloat(Environment.Latitude, 0f),
	    Environment.getFloat(Environment.Longitude, 0f)
	    ));

	MapHandler beanHandler = new MapHandler();
	
	try {
	    beanHandler.add(propertyHandler);
	    propertyHandler.createComponents(beanHandler);
	    beanHandler.add(mapBean);
	} catch (MultipleSoloMapComponentException msmce) {
	    Debug.error("OpenMapNG: tried to add multiple components of the same type when only one is allowed! - " + msmce);
	}
	Debug.message("app", "OpenMapApplet.init()");
	//new OpenMapOld().init();
    }

    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it should start its execution. It is called after 
     * the <code>init</code> method and each time the applet is revisited 
     * in a Web page. 
     * <p>
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#init()
     * @see     java.applet.Applet#stop()
     * @since   JDK1.0
     */
    public void start() {
	Debug.message("app", "OpenMapApplet.start()");
	super.start();
    }

    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it should stop its execution. It is called when 
     * the Web page that contains this applet has been replaced by 
     * another page, and also just before the applet is to be destroyed. 
     * <p>
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#init()
     * @since   JDK1.0
     */
    public void stop() {
	Debug.message("app", "OpenMapApplet.stop()");
	super.stop();
    }

    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it is being reclaimed and that it should destroy 
     * any resources that it has allocated. The <code>stop</code> method 
     * will always be called before <code>destroy</code>. 
     * <p>
     *
     * @see     java.applet.Applet#init()
     * @see     java.applet.Applet#start()
     * @see     java.applet.Applet#stop()
     * @since   JDK1.0
     */
    public void destroy() {
	Debug.message("app", "OpenMapApplet.destroy()");
	super.destroy();
    }
}
