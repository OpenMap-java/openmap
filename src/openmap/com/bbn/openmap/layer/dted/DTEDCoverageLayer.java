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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDCoverageLayer.java,v $
// $RCSfile: DTEDCoverageLayer.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.dted;


/*  Java Core  */
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

/*  OpenMap  */
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;

import javax.swing.JCheckBox;
import javax.swing.Box;


/** 
 * This is a Layer that provides coverage information on the DTED
 * data.  The layer really uses the properties passed in to it to
 * determine which directories should be scanned for the data, and
 * where to locate a coverage file.  If a coverage file is not
 * available, the layer will take the time to scan the directories and
 * create one, placing it in the desired location (as specified in the
 * properties).  There is a palette for this layer, that lets you turn
 * off the coverage for different levels of DTED.  Right now, only
 * level 0, 1 and 2 are handled.  
 * <pre>
 * The properties for this file are:
 * 
 * # Java DTED properties
 * dtedcov.class=com.bbn.openmap.layer.dted.DTEDCoverageLayer
 * dtedcov.prettyName=DTED Coverage
 * # This property should reflect the paths to the RPF directories
 * #jdted.paths=/tmp/data/dted
 * dtedcov.paths=/usr/local/matt/data/dted /cdrom/cdrom0/dted
 * #DTED Level 2 data!
 * dtedcov.level2.paths=/net/blatz/u5/DTEDLV2
 * # Number between 0-255: 0 is transparent, 255 is opaque
 * dtedcov.opaque=255
 * dtedcov.coverageFile=/usr/local/matt/data/dted/coverage.dat
 * # option ----
 * #dtedcov.coverageURL=http://location.of.coverage.file
 * #Default is true, don't need this entry if you like it...
 * dtedcov.level0.showcov=true
 * #Default colors don't need this entry
 * dtedcov.level0.color=CE4F3F
 * #Default is true, don't need this entry if you like it...
 * dtedcov.level1.showcov=true
 * #Default colors don't need this entry
 * dtedcov.level1.color=339159
 * #Default is true, don't need this entry if you like it...
 * dtedcov.level2.showcov=true
 * #Default colors don't need this entry
 * dtedcov.level2.color=0C75D3
 * </pre>
 */
public class DTEDCoverageLayer extends Layer 
    implements ProjectionListener, ActionListener {
    
    /** The graphic list of objects to draw. */
    protected OMGraphicList[] omGraphics;
    /** Projection that gets set on a projection event. */
    protected Projection projection;
    /** Set when the projection has changed while a swing worker is
     * gathering graphics, and we want him to stop early. */
    protected boolean cancelled = false;
    /** The paths to the DTED directories, telling where the data is. */
    protected String[] paths;
    /** The paths to the DTED Level 2 directories, telling where the
     * data is. */
    protected String[] paths2;

    /** Flag to tell the cache to return the coverage for level 0 dted. */
    protected boolean showDTEDLevel0;
    /** Flag to tell the cache to return the coverage for level 1 dted. */
    protected boolean showDTEDLevel1;
    /** Flag to tell the cache to return the coverage for level 0 dted. */
    protected boolean showDTEDLevel2;

    /** The color to outline the shapes for level 0. */
    protected Color level0Color;
    /** The color to outline the shapes for level 1. */
    protected Color level1Color;
    /** The color to outline the shapes for level 2. */
    protected Color level2Color;

    /**
     * A setting for how transparent to make the images.  The default
     * is 255, which is totally opaque.
     */
    protected int opaqueness;
    /** Flag to fill the coverage rectangles. */
    protected boolean fillRects;
    /*
     * Location of coverage summary file.  If it doesn't exists, one
     * will be created here for later use.
     */
    protected String coverageFile = null;

    /** Location of coverage summary file, if supplied as a URL.  If
     * it doesn't exists, a coverage file will be used instead. */
    protected String coverageURL = null;

    protected DTEDCoverageManager coverageManager = null;

    private static final String showLevel0Command = "showLevel0";
    private static final String showLevel1Command = "showLevel1";
    private static final String showLevel2Command = "showLevel2";

    /** The property describing the locations of level 0 and 1 data. */
    public static final String DTEDPathsProperty = ".paths";
    /** The property describing the locations of level 2 data. */
    public static final String DTED2PathsProperty = ".level2.paths";

    /** Property setting to show level 0 data on startup. */
    public static final String ShowLevel0Property = ".level0.showcov";
    /** Property to use to change the color for coverage of level 0 data. */
    public static final String Level0ColorProperty = ".level0.color";

    /** Property setting to show level 1 data on startup. */
    public static final String ShowLevel1Property = ".level1.showcov";
    /** Property to use to change the color for coverage of level 1 data. */
    public static final String Level1ColorProperty = ".level1.color";

    /** Property setting to show level 2 data on startup. */
    public static final String ShowLevel2Property = ".level2.showcov";
    /** Property to use to change the color for coverage of level 2 data. */
    public static final String Level2ColorProperty = ".level2.color";
    /** Property to use for filled rectangles (when java supports it).*/
    public static final String OpaquenessProperty = ".opaque";
    /** Property to use to fill rectangles.*/
    public static final String FillProperty = ".fill";
    /** The file to read/write coverage summary.  If it doesn't exist
     * here, it will be created and placed here.*/
    public static final String CoverageFileProperty = ".coverageFile";
    /** A URL to read coverage summary.  If it doesn't exist,
     * the coverage file will be tried. */
    public static final String CoverageURLProperty = ".coverageURL";

    /** The swing worker that goes off in it's own thread to get
     * graphics.
     */
    protected DTEDCoverageWorker currentWorker;

    /** Since we can't have the main thread taking up the time to
     * create images, we use this worker thread to do it.
     */
    class DTEDCoverageWorker extends SwingWorker {
	/** Constructor used to create a worker thread. */
	public DTEDCoverageWorker () {
	    super();
	}

	/**  Compute the value to be returned by the <code>get</code>
	 * method.  */
	public Object construct() {
	    Debug.message("dtedcov", getName()+
			  "|DTEDCoverageWorker.construct()");
	    fireStatusUpdate(LayerStatusEvent.START_WORKING);
	    try {
		return prepare();
	    } catch (OutOfMemoryError e) {
		String msg = getName() + 
		    "|DTEDCoverageLayer.DTEDCoverageWorker.construct(): " + e;
		Debug.error(msg);
		e.printStackTrace();
		fireRequestMessage(new InfoDisplayEvent(this, msg));
		fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
		return null;
	    }
	}

	/** Called on the event dispatching thread (not on the worker
	 * thread) after the <code>construct</code> method has
	 * returned.  */
	public void finished() {
	    workerComplete(this);
	    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	}
    }

    /** The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     */
    public DTEDCoverageLayer () {}

    /** Method that sets all the variables to the default values. */
    protected void setDefaultValues(){
	paths = null;
	paths2 = null;

	showDTEDLevel0 = true;
	showDTEDLevel1 = true;
	showDTEDLevel2 = true;
	opaqueness = DTEDFrameColorTable.DEFAULT_OPAQUENESS;
	fillRects = false;
    }

    /**
     * Set all the DTED properties from a properties object.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {

	super.setProperties(prefix, properties);
	setDefaultValues();
		
	paths = LayerUtils.initPathsFromProperties(properties,
				 prefix + DTEDPathsProperty);
	paths2 = LayerUtils.initPathsFromProperties(properties,
				 prefix + DTED2PathsProperty);

	coverageFile = properties.getProperty(prefix + CoverageFileProperty);
	coverageURL = properties.getProperty(prefix + CoverageURLProperty);
	
	fillRects = LayerUtils.booleanFromProperties(properties,
				 prefix + FillProperty,
				 false);

	opaqueness = LayerUtils.intFromProperties(properties,
				 prefix + OpaquenessProperty,
				 DTEDFrameColorTable.DEFAULT_OPAQUENESS);

	level0Color = LayerUtils.parseColorFromProperties(properties,
				 prefix + Level0ColorProperty,
				 DTEDCoverageManager.defaultLevel0ColorString);

	level1Color = LayerUtils.parseColorFromProperties(properties,
				 prefix + Level1ColorProperty,
				 DTEDCoverageManager.defaultLevel1ColorString);

	level2Color = LayerUtils.parseColorFromProperties(properties,
				 prefix + Level2ColorProperty,
				 DTEDCoverageManager.defaultLevel2ColorString);

	showDTEDLevel0 = LayerUtils.booleanFromProperties(properties,
				 prefix + ShowLevel0Property,
				 true);
							  
	showDTEDLevel1 = LayerUtils.booleanFromProperties(properties,
				 prefix + ShowLevel1Property,
				 true);
							  
	showDTEDLevel2 = LayerUtils.booleanFromProperties(properties,
				 prefix + ShowLevel2Property,
				 true);
    }

    /**
     * Sets the current graphics lists to the given list.
     *
     * @param aList a list of OMGraphics 
     */
    public synchronized void setGraphicLists (OMGraphicList[] aList) {
	omGraphics = aList;
    }

    /** Retrieves the current graphics lists.  */
    public synchronized OMGraphicList[] getGraphicLists () {
	return omGraphics;
    }

    /**
     * Used to set the cancelled flag in the layer.  The swing worker
     * checks this once in a while to see if the projection has
     * changed since it started working.  If this is set to true, the
     * swing worker quits when it is safe. 
     */
    public synchronized void setCancelled(boolean set){
	cancelled = set;
    }

    /** Check to see if the cancelled flag has been set. */
    public synchronized boolean isCancelled(){
	return cancelled;
    }

    /** 
     * Implementing the ProjectionPainter interface.
     */
    public synchronized void renderDataForProjection(Projection proj, java.awt.Graphics g){
	if (proj == null){
	    Debug.error("DTEDCoverageLayer.renderDataForProjection: null projection!");
	    return;
	} else if (!proj.equals(projection)){
	    projection = proj.makeClone();
	    setGraphicLists(prepare());
	}
	paint(g);
    }

    /**
     * The projectionListener interface method that lets the Layer
     * know when the projection has changes, and therefore new graphics
     * have to created /supplied for the screen.
     *
     * @param e The projection event, most likely fired from a map bean.
     */
    public void projectionChanged (ProjectionEvent e) {
	Debug.message("basic", getName()+
		      "|DTEDCoverageLayer.projectionChanged()");

	if (projection != null){
	    if (projection.equals(e.getProjection()))
		// Nothing to do, already have it and have acted on it...
	      {
		repaint();
		return;
	      }
	}
 	setGraphicLists(null);

	projection = (Projection)e.getProjection().makeClone();
	// If there isn't a worker thread working on this already,
	// create a thread that will do the real work. If there is
	// a thread working on this, then set the cancelled flag
	// in the layer.
	if (currentWorker == null) {
	    currentWorker = new DTEDCoverageWorker();
	    currentWorker.execute();
	}
	else setCancelled(true);
    }

    /**
     * The DTEDCoverageWorker calls this method on the layer when it is
     * done working.  If the calling worker is not the same as the
     * "current" worker, then a new worker is created.
     *
     * @param worker the worker that has the graphics.
     */
    protected synchronized void workerComplete (DTEDCoverageWorker worker) {
	if (!isCancelled()) {
	    currentWorker = null;
	    setGraphicLists((OMGraphicList[])worker.get());
	    repaint();
	}
	else{
	    setCancelled(false);
	    currentWorker = new DTEDCoverageWorker();
	    currentWorker.execute();
	}
    }

    /**
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the dtedcov.  <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the map will set the cancel bit in the
     * LayerThread, (the thread that is running the prepare).  If this
     * Layer needs to do any cleanups during the abort, it should do
     * so, but return out of the prepare asap.
     */
    public OMGraphicList[] prepare () {

	if (isCancelled()){
	    Debug.message("dtedcov", getName()+
			  "|DTEDCoverageLayer.prepare(): aborted.");
	    return null;
	}

	Debug.message("basic", getName()+
		      "|DTEDCoverageLayer.prepare(): doing it");

	// Setting the OMGraphicsList for this layer.  Remember, the
	// OMGraphicList is made up of OMGraphics, which are generated
	// (projected) when the graphics are added to the list.  So,
	// after this call, the list is ready for painting.

	// call getRectangle();
	if (Debug.debugging("dtedcov")) {
	    Debug.output(getName()+"|DTEDCoverageLayer.prepare(): " +
			 "calling prepare with projection: " + projection +
			 " ul = " + projection.getUpperLeft() + " lr = " + 
			 projection.getLowerRight()); 
	}

	// IF the coverage manager has not been set up yet, do it!
	if (coverageManager == null){
	    coverageManager = new DTEDCoverageManager(paths, paths2, 
						      coverageURL, coverageFile);
	    coverageManager.setPaint(level0Color, level1Color, level2Color, 
				     opaqueness, fillRects);
	}

	OMGraphicList[] omGraphicLists = coverageManager.getCoverageRects(projection);

	/////////////////////
	// safe quit
	int size = 0;
	if (omGraphicLists != null){
	    for (int j = 0; j < omGraphicLists.length; j++){
		size = omGraphicLists[j].size();	
		Debug.message("basic", getName()+
			      "|DTEDCoverageLayer.prepare(): finished with "+
			      size + " level " + j + " graphics");
	    }
	}
	else 
	    Debug.message("basic", getName()+
	      "|DTEDCoverageLayer.prepare(): finished with null graphics list");

	// Don't forget to project them.  Since they are only being
	// recalled if the projection hase changed, then we need to
	// force a reprojection of all of them because the screen
	// position has changed.
	for (int k = 0; k < omGraphicLists.length; k++)
	    omGraphicLists[k].project(projection, true);

	return omGraphicLists;
    }


    /**
     * Paints the layer.
     *
     * @param g the Graphics context for painting
     */
    public void paint (java.awt.Graphics g) {
	Debug.message("dtedcov", getName()+"|DTEDCoverageLayer.paint()");

	OMGraphicList[] tmpGraphics = getGraphicLists();

	if (tmpGraphics != null) {
	    for (int k = 0; k < tmpGraphics.length; k++){
		if (k == 0 && showDTEDLevel0)
		    tmpGraphics[k].render(g);
		if (k == 1 && showDTEDLevel1)
		    tmpGraphics[k].render(g);
		if (k == 2 && showDTEDLevel2)
		    tmpGraphics[k].render(g);
	    }
	}
    }

    //----------------------------------------------------------------------
    // GUI
    //----------------------------------------------------------------------
    /**
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     *
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
	JCheckBox showLevel0Check, showLevel1Check, showLevel2Check;

	showLevel0Check = new JCheckBox("Show Level 0 Coverage", showDTEDLevel0);
	showLevel0Check.setActionCommand(showLevel0Command);
	showLevel0Check.addActionListener(this);

	showLevel1Check = new JCheckBox("Show Level 1 Coverage", showDTEDLevel1);
	showLevel1Check.setActionCommand(showLevel1Command);
	showLevel1Check.addActionListener(this);
	
	showLevel2Check = new JCheckBox("Show Level 2 Coverage", showDTEDLevel2);
	showLevel2Check.setActionCommand(showLevel2Command);
	showLevel2Check.addActionListener(this);
	
	Box box = Box.createVerticalBox();
	box.add(showLevel0Check);
	box.add(showLevel1Check);
	box.add(showLevel2Check);
	return box;
    }


    //----------------------------------------------------------------------
    // ActionListener interface implementation
    //----------------------------------------------------------------------

    /**
     * The Action Listener method, that reacts to the palette widgets
     * actions.
     */
    public void actionPerformed (ActionEvent e) {
	super.actionPerformed(e);
	String cmd = e.getActionCommand();
	if (cmd == showLevel0Command) {
	    JCheckBox level0Check = (JCheckBox)e.getSource();
	    showDTEDLevel0 = level0Check.isSelected();
	    repaint();
	} else if (cmd == showLevel1Command) {
	    JCheckBox level1Check = (JCheckBox)e.getSource();
	    showDTEDLevel1 = level1Check.isSelected();
	    repaint();
	} else if (cmd == showLevel2Command) {
	    JCheckBox level2Check = (JCheckBox)e.getSource();
	    showDTEDLevel2 = level2Check.isSelected();
	    repaint();
	}
    }
}
