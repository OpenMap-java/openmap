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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfCoverage.java,v $
// $RCSfile: RpfCoverage.java,v $
// $Revision: 1.2 $
// $Date: 2003/06/25 15:28:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.rpf;

/*  Java Core  */
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Vector;
import java.io.*;
import java.net.URL;

/*  OpenMap  */
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.io.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.propertyEditor.Inspector;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.SwingWorker;

import javax.swing.*;

/** 
 * This is a tool that provides coverage information on the Rpf
 * data.  It is supposed to be a simple tool that lets you see the
 * general location of data, to guide you to the right place and scale
 * of coverage. The layer really uses the properties passed in to it
 * to determine which RPF/A.TOC should be scanned for the data.  There
 * is a palette for this layer, that lets you turn off the coverage
 * for different levels of Rpf.  Right now, only City Graphics, TLM,
 * JOG, TPC, ONC, JNC, GNC and 5/10 meter CIB scales are are handled.
 * All other scales are tossed together under the misc setting.  The
 * City Graphics setting shows all charts for scales greater than than
 * 1:15k. 
 * <P><pre>The properties for this file are:
 * # Java Rpf properties
 * # Number between 0-255: 0 is transparent, 255 is opaque
 * jrpf.coverageOpaque=255
 * #Default is true, don't need this entry if you like it...
 * jrpf.CG.showcov=true
 * #Default colors don't need this entry
 * jrpf.CG.color=CE4F3F
 * # Other types can be substituted for CG (TLM, JOG, TPC, ONC, JNC, GNC, CIB10, CIB5, MISC)
 * # Fill the rectangle, default is true
 * jrpf.coverageFill=true
 *</pre>
 */
public class RpfCoverage implements ActionListener, RpfConstants, PropertyConsumer {
    
    /** The graphic list of objects to draw. */
    protected Vector omGraphics;

    /** Set when the projection has changed while a swing worker is
     * gathering graphics, and we want him to stop early. */
    protected boolean cancelled = false;
    protected RpfCoverageManager coverageManager = null;

    protected String propertyPrefix = null;

    /** Flag to tell the cache to return the coverage for city graphics. */
    protected boolean showCG;
    /** Flag to tell the cache to return the coverage for tlm. */
    protected boolean showTLM;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showJOG;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showTPC;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showONC;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showJNC;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showGNC;
    /** Flag to tell the cache to return the coverage for 10M CIB. */
    protected boolean showCIB10;
    /** Flag to tell the cache to return the coverage for 5M CIB. */
    protected boolean showCIB5;
    /** Flag to tell the cache to return the coverage for others. */
    protected boolean showMISC;

    /** The color to outline the shapes. */
    protected Color CGColor;
    /** The color to outline the shapes. */
    protected Color TLMColor;
    /** The color to outline the shapes. */
    protected Color JOGColor;
    /** The color to outline the shapes. */
    protected Color TPCColor;
    /** The color to outline the shapes. */
    protected Color ONCColor;
    /** The color to outline the shapes. */
    protected Color JNCColor;
    /** The color to outline the shapes. */
    protected Color GNCColor;
    /** The color to outline the shapes. */
    protected Color CIB10Color;
    /** The color to outline the shapes. */
    protected Color CIB5Color;
    /** The color to outline the shapes. */
    protected Color MISCColor;

    /** A setting for how transparent to make the images.  The default
     * is 255, which is totally opaque.  Not used right now.
     * */
    protected int opaqueness;
    /** Flag to fill the coverage rectangles. */
    protected boolean fillRects;

    /** Property to use for filled rectangles (when java supports it).*/
    public static final String OpaquenessProperty = "coverageOpaque";
    /** Property to use to fill rectangles.*/
    public static final String FillProperty = "coverageFill";

    /** The parent layer. */
    protected Layer layer;
    /** Flag to track when the RpfCoverage is active. */
    protected boolean inUse = false;
    /** Show the palette when showing coverage. Probably not needed
     *  for layers limiting chart seriestypes for display. */
    protected boolean showPalette = true;

    /**
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     */
    public RpfCoverage(Layer l) {
	layer = l;
    }

    /** Method that sets all the variables to the default values. */
    protected void setDefaultValues() {
	allCoveragesOn();
	opaqueness = RpfColortable.DEFAULT_OPAQUENESS;
	fillRects = true;
    }

    public boolean isInUse() {
	return inUse;
    }

    public void setInUse(boolean iu) {
	inUse = iu;
	if (showPalette || !inUse) {
	    // Always want it hidden if not in use.
	    getPaletteWindow().setVisible(inUse);
	}
    }

    public boolean isShowPalette() {
	return showPalette;
    }

    public void setShowPalette(boolean sp) {
	showPalette = sp;
	if (!showPalette) {
	    allCoveragesOn();
	}
    }

    public void allCoveragesOn() {
	showCG = true;
	showTLM = true;
	showJOG = true;
	showTPC = true;
	showONC = true;
	showJNC = true;
	showGNC = true;
	showCIB10 = true;
	showCIB5 = true;
	showMISC = true;
    }

    public void setProperties(java.util.Properties props) {
	setProperties(null, props);
    }

    /**
     * Set all the Rpf properties from a properties object.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
	setPropertyPrefix(prefix);

	setDefaultValues();

	prefix = PropUtils.getScopedPropertyPrefix(prefix);
		
	fillRects = LayerUtils.booleanFromProperties(properties,
						     prefix + FillProperty,
						     true);

	opaqueness = LayerUtils.intFromProperties(properties,
						  prefix + OpaquenessProperty,
						  RpfColortable.DEFAULT_OPAQUENESS);
	
	CGColor = LayerUtils.parseColorFromProperties(properties, 
						      prefix + CGColorProperty, 
						      RpfCoverageManager.defaultCGColorString);
	TLMColor = LayerUtils.parseColorFromProperties(properties, 
						       prefix + TLMColorProperty, 
						       RpfCoverageManager.defaultTLMColorString);
	JOGColor = LayerUtils.parseColorFromProperties(properties, 
						       prefix + JOGColorProperty, 
						       RpfCoverageManager.defaultJOGColorString);
	TPCColor = LayerUtils.parseColorFromProperties(properties, 
						       prefix + TPCColorProperty, 
						       RpfCoverageManager.defaultTPCColorString);
	ONCColor = LayerUtils.parseColorFromProperties(properties, 
						       prefix + ONCColorProperty, 
						       RpfCoverageManager.defaultONCColorString);
	JNCColor = LayerUtils.parseColorFromProperties(properties, 
						       prefix + JNCColorProperty, 
						       RpfCoverageManager.defaultJNCColorString);
	GNCColor = LayerUtils.parseColorFromProperties(properties, 
						       prefix + GNCColorProperty, 
						       RpfCoverageManager.defaultGNCColorString);
	CIB10Color = LayerUtils.parseColorFromProperties(properties, 
							 prefix + CIB10ColorProperty, 
							 RpfCoverageManager.defaultCIB10ColorString);
	CIB5Color = LayerUtils.parseColorFromProperties(properties, 
							prefix + CIB5ColorProperty, 
							RpfCoverageManager.defaultCIB5ColorString);
	MISCColor = LayerUtils.parseColorFromProperties(properties, 
							prefix + MISCColorProperty, 
							RpfCoverageManager.defaultMISCColorString);

	// If the palette is turned off, then we all of them have been
	// set to true.  Only the coverage of the limited series will
	// be asked for.
	if (showPalette) {
	    showCG = LayerUtils.booleanFromProperties(properties, prefix + ShowCGProperty, true);
	    showTLM = LayerUtils.booleanFromProperties(properties, prefix + ShowTLMProperty, true);
	    showJOG = LayerUtils.booleanFromProperties(properties, prefix + ShowJOGProperty, true);
	    showTPC = LayerUtils.booleanFromProperties(properties, prefix + ShowTPCProperty, true);
	    showONC = LayerUtils.booleanFromProperties(properties, prefix + ShowONCProperty, true);
	    showJNC = LayerUtils.booleanFromProperties(properties, prefix + ShowJNCProperty, true);
	    showGNC = LayerUtils.booleanFromProperties(properties, prefix + ShowGNCProperty, true);
	    showCIB10 = LayerUtils.booleanFromProperties(properties, prefix + ShowCIB10Property, true);
	    showCIB5 = LayerUtils.booleanFromProperties(properties, prefix + ShowCIB5Property, true);
	    showMISC = LayerUtils.booleanFromProperties(properties, prefix + ShowMISCProperty, true);
	}
    }

    /**
     * PropertyConsumer method, to fill in a Properties object,
     * reflecting the current values of the layer.  If the
     * layer has a propertyPrefix set, the property keys should
     * have that prefix plus a separating '.' prepended to each
     * propery key it uses for configuration.
     *
     * @param props a Properties object to load the PropertyConsumer
     * properties into.  If props equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

	String prefix = PropUtils.getScopedPropertyPrefix(propertyPrefix);

	props.put(prefix + FillProperty, new Boolean(fillRects).toString());
	props.put(prefix + OpaquenessProperty, Integer.toString(opaqueness));
	props.put(prefix + CGColorProperty, CGColor);
	props.put(prefix + TLMColorProperty, TLMColor);
	props.put(prefix + JOGColorProperty, JOGColor);
	props.put(prefix + TPCColorProperty, TPCColor);
	props.put(prefix + ONCColorProperty, ONCColor);
	props.put(prefix + JNCColorProperty, JNCColor);
	props.put(prefix + GNCColorProperty, GNCColor);
	props.put(prefix + CIB10ColorProperty, CIB10Color);
	props.put(prefix + CIB5ColorProperty, CIB5Color);
	props.put(prefix + MISCColorProperty, MISCColor);

	return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).  For Layer, this method should at least return the
     * 'prettyName' property.
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer. 
     */
    public Properties getPropertyInfo(Properties list) {
	if (list == null) {
	    list = new Properties();
	}
	
	list.put(FillProperty, "Flag to set if the coverage rectangles should be filled.");
	list.put(FillProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	list.put(OpaquenessProperty, "Integer representing opaqueness level (0-255, 0 is clear) of rectangles.");

	list.put(CGColorProperty, "Color string for City Graphics chart coverage.");
	list.put(CGColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(TLMColorProperty, "Color string for TLM chart coverage.");
	list.put(TLMColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(JOGColorProperty, "Color string for JOG chart coverage.");
	list.put(JOGColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(TPCColorProperty, "Color string for TPC chart coverage.");
	list.put(TPCColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(ONCColorProperty, "Color string for ONC chart coverage.");
	list.put(ONCColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(JNCColorProperty, "Color string for JNC chart coverage.");
	list.put(JNCColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(GNCColorProperty, "Color string for GNC chart coverage.");
	list.put(GNCColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(CIB10ColorProperty, "Color string for CIB 10 meter image coverage.");
	list.put(CIB10ColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(CIB5ColorProperty, "Color string for CIB 5 meter image coverage.");
	list.put(CIB5ColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	list.put(MISCColorProperty, "Color string for all other chart coverage.");
	list.put(MISCColorProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
	
	return list;
    }

    /**
     * Specify what order properties should be presented in an editor.
     */
    public String getInitPropertiesOrder() {
	return " " + FillProperty + " " + OpaquenessProperty + " " + GNCColorProperty + " " + JNCColorProperty + " " + ONCColorProperty + " " + TPCColorProperty + " " + JOGColorProperty + " " + TLMColorProperty + " " + CIB10ColorProperty + " " + CIB5ColorProperty + " " + MISCColorProperty;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer.  The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     *
     * @param prefix the prefix String.  
     */
    public void setPropertyPrefix(String prefix) {
	propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     *
     * @param String prefix String.  
     */
    public String getPropertyPrefix() {
	return propertyPrefix;
    }

    /** 
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the rpfcov.  <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the map will set the cancel bit in the
     * LayerThread, (the thread that is running the prepare).  If this
     * Layer needs to do any cleanups during the abort, it should do
     * so, but return out of the prepare asap.
     *
     * @return OMGraphicList of rectangles showing coverages.
     */
    public void prepare(RpfFrameProvider frameProvider,
			Projection projection, String chartSeries) {

	float ullat = 90f;
	float ullon = -180f;
	float lrlat = -90f;
	float lrlon = 180f;

	if (projection != null) {
	    ullat = projection.getUpperLeft().getLatitude();
	    ullon = projection.getUpperLeft().getLongitude();
	    lrlat = projection.getLowerRight().getLatitude();
	    lrlon = projection.getLowerRight().getLongitude();
	}
	
	Debug.message("basic","RpfCoverage.prepare(): doing it");

	// Setting the OMGraphicsList for this layer.  Remember, the
	// OMGraphicList is made up of OMGraphics, which are generated
	// (projected) when the graphics are added to the list.  So,
	// after this call, the list is ready for painting.

	// IF the data arrays have not been set up yet, do it!
	if (coverageManager == null) {
	    coverageManager = new RpfCoverageManager(frameProvider);
	    Color[] colors = new Color[10];
	    int opa = opaqueness << 24;
	    colors[0] = ColorFactory.createColor(((CGColor.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[1] = ColorFactory.createColor(((TLMColor.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[2] = ColorFactory.createColor(((JOGColor.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[3] = ColorFactory.createColor(((TPCColor.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[4] = ColorFactory.createColor(((ONCColor.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[5] = ColorFactory.createColor(((JNCColor.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[6] = ColorFactory.createColor(((GNCColor.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[7] = ColorFactory.createColor(((CIB10Color.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[8] = ColorFactory.createColor(((CIB5Color.getRGB() & 0x00FFFFFF) | opa), true);
	    colors[9] =ColorFactory.createColor(((MISCColor.getRGB() & 0x00FFFFFF) | opa), true) ;
	    coverageManager.setColors(colors, opaqueness, fillRects);
	}
	
	setGraphicLists(coverageManager.getCatalogCoverage(ullat, ullon, lrlat, lrlon,
							   projection, chartSeries));
    }

    public synchronized void setGraphicLists(Vector lists) {
	omGraphics = lists;
    }

    public synchronized Vector getGraphicLists() {
	return omGraphics;
    }

    /**
     * Paints the layer.
     *
     * @param g the Graphics context for painting
     *
     */
    public void paint(java.awt.Graphics g) {
	Debug.message("rpfcov", "RpfCoverage.paint()");

	Vector tmpGraphics = getGraphicLists();

	if (tmpGraphics != null) {
	    int length = tmpGraphics.size();
	    Debug.message("rpfcov", "RpfCoverage.painting(): " + length + " lists");
	    for (int k = length-1; k >=0; k--) {
		// HACK - this order is nicely arranged with the order
		// that lists are arrainged by the
		// RpfCoverageManager!!!!
		if (k == 0 && showCG)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 1 && showCIB5)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 2 && showTLM)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 3 && showCIB10)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 4 && showJOG)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 5 && showMISC)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 6 && showTPC)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 7 && showONC)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 8 && showJNC)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
		if (k == 9 && showGNC)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).render(g);
	    }
	} else {
	    Debug.message("rpfcov", "RpfCoverage.paint(): null graphics list");
	}
    }

    /**
     * Reproject the graphics you have.
     *
     * @param proj the projection to use
     *
     */
    public void generate(Projection proj) {
	Debug.message("rpfcov", "RpfCoverage.generate()");

	Vector tmpGraphics = getGraphicLists();

	if (tmpGraphics != null) {
	    int length = tmpGraphics.size();
	    for (int k = length-1; k >=0; k--) {
		// HACK - this order is nicely arranged with the order
		// that lists are arrainged by the
		// RpfCoverageManager!!!!
		if (k == 0)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 1)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 2)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 3)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 4)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 5)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 6)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 7)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 8)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
		if (k == 9)
		    ((OMGraphicList)tmpGraphics.elementAt(k)).generate(proj);
	    }
	}
    }

    //----------------------------------------------------------------------
    // GUI
    //----------------------------------------------------------------------
    /** Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * @return Component object representing the palette widgets.
     * */
    public java.awt.Component getGUI() {
	JCheckBox showCGCheck, showTLMCheck, showJOGCheck, showTPCCheck, showONCCheck, showJNCCheck, showGNCCheck, showCIB10Check, showCIB5Check, showMISCCheck;

	showCGCheck = new JCheckBox("Show City Graphic Coverage", showCG);
	showCGCheck.setActionCommand(showCGCommand);
	showCGCheck.addActionListener(this);
	showCGCheck.setForeground(CGColor);

	showTLMCheck = new JCheckBox("Show TLM (1:50k) Coverage", showTLM);
	showTLMCheck.setActionCommand(showTLMCommand);
	showTLMCheck.addActionListener(this);
	showTLMCheck.setForeground(TLMColor);
	
	showJOGCheck = new JCheckBox("Show JOG (1:250k) Coverage", showJOG);
	showJOGCheck.setActionCommand(showJOGCommand);
	showJOGCheck.addActionListener(this);
	showJOGCheck.setForeground(JOGColor);

	showTPCCheck = new JCheckBox("Show TPC (1:500k) Coverage", showTPC);
	showTPCCheck.setActionCommand(showTPCCommand);
	showTPCCheck.addActionListener(this);
	showTPCCheck.setForeground(TPCColor);
		
	showONCCheck = new JCheckBox("Show ONC (1:1M) Coverage", showONC);
	showONCCheck.setActionCommand(showONCCommand);
	showONCCheck.addActionListener(this);
	showONCCheck.setForeground(ONCColor);
	
	showJNCCheck = new JCheckBox("Show JNC (1:2M) Coverage", showJNC);
	showJNCCheck.setActionCommand(showJNCCommand);
	showJNCCheck.addActionListener(this);
	showJNCCheck.setForeground(JNCColor);
		
	showGNCCheck = new JCheckBox("Show GNC (1:5M) Coverage", showGNC);
	showGNCCheck.setActionCommand(showGNCCommand);
	showGNCCheck.addActionListener(this);
	showGNCCheck.setForeground(GNCColor);
	
	showCIB10Check = new JCheckBox("Show CIB 10m Coverage", showCIB10);
	showCIB10Check.setActionCommand(showCIB10Command);
	showCIB10Check.addActionListener(this);
	showCIB10Check.setForeground(CIB10Color);
	
	showCIB5Check = new JCheckBox("Show CIB 5m Coverage", showCIB5);
	showCIB5Check.setActionCommand(showCIB5Command);
	showCIB5Check.addActionListener(this);
	showCIB5Check.setForeground(CIB5Color);
	
	showMISCCheck = new JCheckBox("Show Coverage of all Others", showMISC);
	showMISCCheck.setActionCommand(showMISCCommand);
	showMISCCheck.addActionListener(this);
	showMISCCheck.setForeground(MISCColor);
		
	Box box = Box.createVerticalBox();
	box.add(showCGCheck);
	box.add(showTLMCheck);
	box.add(showJOGCheck);
	box.add(showTPCCheck);
	box.add(showONCCheck);
	box.add(showJNCCheck);
	box.add(showGNCCheck);
	box.add(showCIB10Check);
	box.add(showCIB5Check);
	box.add(showMISCCheck);
	return box;
    }

    protected JFrame paletteWindow = null;

    /**
     * Get RpfCoverage's associated palette as a top-level window
     * @return the frame that the palette is in
     */
    public JFrame getPaletteWindow() {
	
	if (paletteWindow == null) {
	    // create the palette's scroll pane
	    Component pal = getGUI();
	    if (pal == null)
		pal = new JLabel("No Palette");
	    
	    
	    JPanel p = new JPanel();
	    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	    p.setAlignmentX(Component.LEFT_ALIGNMENT);
	    p.setAlignmentY(Component.BOTTOM_ALIGNMENT);
	    p.add(pal);
	    
	    JScrollPane scrollPane = new JScrollPane(
		p,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
	    scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
	    
	    
	    // create the palette internal window
	    paletteWindow = new JFrame("RPF Coverage Palette");
	    
	    paletteWindow.setContentPane(scrollPane);
	    paletteWindow.pack();//layout all the components
	}
	return paletteWindow;
    }


    //----------------------------------------------------------------------
    // ActionListener interface implementation
    //----------------------------------------------------------------------

    /** The Action Listener method, that reacts to the palette widgets
     * actions.
     * */
    public void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	JCheckBox check = (JCheckBox)e.getSource();

	if (cmd == showCGCommand) showCG = check.isSelected();
	else if (cmd == showTLMCommand) showTLM = check.isSelected();
	else if (cmd == showJOGCommand) showJOG = check.isSelected();
	else if (cmd == showTPCCommand) showTPC = check.isSelected();
	else if (cmd == showONCCommand) showONC = check.isSelected();
	else if (cmd == showJNCCommand) showJNC = check.isSelected();
	else if (cmd == showGNCCommand) showGNC = check.isSelected();
	else if (cmd == showCIB10Command) showCIB10 = check.isSelected();
	else if (cmd == showCIB5Command) showCIB5 = check.isSelected();
	else if (cmd == showMISCCommand) showMISC = check.isSelected();
	else {
	    System.err.println("Unknown action command \"" + cmd +
			       "\" in RpfCoverageLayer.actionPerformed().");
	}
	layer.repaint();
    }
}
