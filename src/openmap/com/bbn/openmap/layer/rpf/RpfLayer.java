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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfLayer.java,v $
// $RCSfile: RpfLayer.java,v $
// $Revision: 1.2 $
// $Date: 2003/02/20 02:43:50 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.rpf;


/*  Java Core  */
import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;

/*  OpenMap  */
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.layer.util.cacheHandler.CacheHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.SwingWorker;
import com.bbn.openmap.util.propertyEditor.Inspector;

/**
 * The RpfLayer fills the screen with RPF data.  There is also a tool
 * available that allows you to see the coverage of the available
 * data.  To view theimages, the projection of the map has to be set
 * in the ARC projection, which OpenMap calls the CADRG projection.
 * The RpfLayer can use several RPF directories at the same time, and
 * doesn't require that the data actually be there at runtime.  That
 * way, you can give a location where the data may be mouted during
 * runtime(i.e. CDROM) and the layer will still use the data. The
 * scale of the projection does not necessarily have to match the
 * scale of a map series for that series to be displayed.  There are
 * options, set in the RpfViewAttributes, that allow scaling of the
 * RPF images to match the map scale.<P>
 * 
 * The RpfLayer uses the RpfCacheManager to get the images it needs to
 * display.  Whenever the projection changes, the cache manager takes
 * the new projection and creates a OMGraphicList with the new image
 * frames and attribute text. <P>
 *
 * The RpfLayer gets its intial settings from properties. This should be
 * done right after the RpfLayer is created. The properties list contains
 * the location of the RPF directories, the opaqueness of the images,
 * the number of colors to use, and whether to show the images and/or
 * attributes by default.  An example of the RpfLayer properties: <P>
 * 
 *<pre>
 *#-----------------------------
 *# Properties for RpfLayer
 *#-----------------------------
 *# Mandatory properties
 *# This property should reflect the paths to the RPF directories
 *rpf.paths=/usr/local/matt/data/RPF /usr/local/matt/data/CIB/RPF
 *
 *# Optional Properties - the default will be set if these are not 
 *# included in the properties file: 
 *# Number between 0-255: 0 is transparent, 255 is opaque.  255 is default.
 *rpf.opaque=128
 *
 *# Number of colors to use on the maps - 16, 32, 216.  216 is default.
 *rpf.numberColors=216
 *
 *# Display maps on startup.  Default is true.
 *rpf.showMaps=true
 *
 *# Display attribute information on startup.  Default is false.
 *rpf.showInfo=false
 *
 *# Scale charts to match display scale.  Default is true.
 *rpf.scaleImages=true
 *
 *# The scale factor to allow when scaling images (2x, 4x, also mean 1/2, 1/4).  Default is 4.
 *rpf.imageScaleFactor=4
 *
 *# Delete the cache if the layer is removed from the map.  Default is false.
 *rpf.killCache=true
 *# Limit the display to the chart code specified. (GN, JN, ON, TP, etc.).
 *# Default is ANY
 *rpf.chartSeries=ANY
 *# Get the subframe attribute data from the Frame provider.
 *rpf.autofetchAttributes=false
 *# Set to true if you want the coverage tool available.
 *rpf.coverage=true
 *# Set the subframe cache size. (Number of subframes to hold on to, 256x256 pixels)
 *rpf.subframeCacheSize=128
 *# Then also include coverage properties, which are available in the RpfConstants.
 *#------------------------------------
 *# End of properties for RpfLayer
 *#------------------------------------
 *</pre>
 *
 */
public class RpfLayer extends OMGraphicHandlerLayer 
    implements ActionListener, RpfConstants, Serializable {
    
    /** The main source for the images and attribute information.  All
     * requests for graphic objects should go through this cache, and it
     * will automatically handle getting the frame files, decoding them,
     * and returning an object list.
     * */
    protected transient RpfCacheManager cache = null;
    /** The paths to the RPF directories, telling where the data is. */
    protected String[] paths;
    /** The display attributes for the maps.  This object should not
     * be replaced, because the caches all look at it, too.  Just
     * adjust the parameters within it. 
     * @see RpfViewAttributes */
    protected RpfViewAttributes viewAttributes;
    /** Flag to delete the cache if the layer is removed from the map. */
    protected boolean killCache = true;
    /** The supplier of frame data. */
    protected RpfFrameProvider frameProvider;
    /** The coverage tool for the layer. */
    protected RpfCoverage coverage;
    /** Subframe cache size. Default is 40.*/
    protected int subframeCacheSize;
    /** Auxillary subframe cache size. Default is 10.*/
    protected int auxSubframeCacheSize;

    /**
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values. Use this construct if you are
     * going to use a standard properties file, which will set the
     * paths. 
     */
    public RpfLayer() {
	viewAttributes = new RpfViewAttributes();
    }

    /** 
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     *
     * @param pathsToRPFDirs paths to the RPF directories that hold
     * A.TOC files.  
     */
    public RpfLayer(String[] pathsToRPFDirs) {
	setPaths(pathsToRPFDirs);
	viewAttributes = new RpfViewAttributes();
    }

    /**
     * Set the paths to the RPF directories, which are by default the
     * parents of the A.TOC table of contents files.  Creates the
     * RpfFrameProvider.
     *
     * @param pathsToRPFDirs Array of strings that list the paths to
     * RPF directories.  
     */
    public void setPaths(String[] pathsToRPFDirs) {
	if (pathsToRPFDirs != null) {
	    setFrameProvider(new RpfFrameCacheHandler(pathsToRPFDirs));
	} else {
	    Debug.output("RpfLayer: Need RPF directory paths.");
	    frameProvider = null;
	}
	paths = pathsToRPFDirs;
	this.cache = null;
    }

    /**
     * Get the paths to the RPF directories.
     *
     * @return String[]
     */
    public String[] getPaths() {
	return paths;
    }

    /**
     * Overriding what happens to the internal OMGraphicList when the
     * projection changes.  For this layer, we want to reset the
     * internal OMGraphicList when the projection changes.
     */
    protected void resetListForProjectionChange() {
	setList(null);
    }

   /** 
     *  Called when the layer is no longer part of the map.  In this
     *  case, we should disconnect from the server if we have a
     *  link. 
     */
    public void removed(java.awt.Container cont) {
	if (killCache) {
	    Debug.message("rpf", "RpfLayer: emptying cache!");
	    clearCache();
	}

	// need to reset this for when it gets added again, if it was
	// removed without the projection actually changing. This
	// helps when the cache needs to be rebuilt.
	setProjection((Projection)null); 
    }

    protected void setDefaultValues() {
	// defaults
	paths = null;
    }

    /**
     * Set all the RPF properties from a properties object.
     */
    public void setProperties(String prefix, java.util.Properties properties) {

	super.setProperties(prefix, properties);

	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	paths = LayerUtils.initPathsFromProperties(properties, 
						   prefix + RpfPathsProperty);

	String numColorsString = properties.getProperty(prefix + 
							NumColorsProperty);

	viewAttributes.setProperties(prefix, properties);

	subframeCacheSize = LayerUtils.intFromProperties(properties, prefix + CacheSizeProperty, RpfCacheHandler.SUBFRAME_CACHE_SIZE);

	auxSubframeCacheSize = LayerUtils.intFromProperties(properties, prefix + CacheSizeProperty, RpfCacheManager.SMALL_CACHE_SIZE);

	if (viewAttributes.chartSeries == null) 
	    viewAttributes.chartSeries = RpfViewAttributes.ANY;

	killCache = LayerUtils.booleanFromProperties(properties, prefix + KillCacheProperty, true);

	if (LayerUtils.booleanFromProperties(properties, 
					     prefix + CoverageProperty, false)) {
	    setCoverage(new RpfCoverage(this));
	    coverage.setProperties(prefix, properties);
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
	props = super.getProperties(props);
	String prefix = PropUtils.getScopedPropertyPrefix(this);

	// find out paths...
	String[] p = getPaths();
	StringBuffer pathString = new StringBuffer();
	if (p != null) {
	    for (int i = 0; i < p.length; i++) {
		if (p[i] != null) {
		    pathString.append(p[i]);
		    if (i < p.length - 1) {
			pathString.append(";"); // separate paths with ;
		    }
		}
	    }
	    props.put(prefix + RpfPathsProperty, pathString.toString());
	} else {
	    props.put(prefix + RpfPathsProperty, "");
	}
    
	props.put(prefix + KillCacheProperty, new Boolean(killCache).toString());
	props.put(prefix + CoverageProperty, new Boolean(coverage == null).toString());
	props.put(prefix + CacheSizeProperty, Integer.toString(subframeCacheSize));
	props.put(prefix + AuxCacheSizeProperty, Integer.toString(auxSubframeCacheSize));

	viewAttributes.setPropertyPrefix(prefix);
	viewAttributes.getProperties(props);

	if (coverage == null) {
	    RpfCoverage cov = new RpfCoverage(this);
	    cov.setProperties(prefix, new Properties());
	    cov.getProperties(props);
	} else {
	    coverage.getProperties(props);
	}

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
	
	list.put(RpfPathsProperty, "Paths to RPF directories.  Semi-colon separated paths");
	list.put(RpfPathsProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.MultiDirectoryPropertyEditor");

	list.put(KillCacheProperty, "Flag to trigger the cache to be cleared when layer is removed from the map.");
	list.put(KillCacheProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	list.put(CoverageProperty, "Flag that adds the coverage tool to the layer.");
	list.put(CoverageProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	list.put(CacheSizeProperty, "Number of frames to hold in the frame cache.");
	list.put(AuxCacheSizeProperty, "Number of subframes to hold in the subframe cache.");

	viewAttributes.getPropertyInfo(list);

	if (coverage == null) {
	    new RpfCoverage(this).getPropertyInfo(list);
	} else {
	    coverage.getPropertyInfo(list);
	}

	return list;
    }

    /**
     * Sets the current graphics list to the given list.
     *
     * @param aList a list of OMGraphics.
     */
    public synchronized void setGraphicList(OMGraphicList aList) {
	setList(aList);
    }

    /** Retrieves the current graphics list.  */
    public synchronized OMGraphicList getGraphicList() {
	return getList();
    }

    /**
     * Clear the frame cache.
     */
    public void clearCache() {

	if (frameProvider instanceof CacheHandler) {
	    ((CacheHandler)frameProvider).resetCache();
	}

	if (this.cache != null) {
	    this.cache.setViewAttributes(null);
	    this.cache.setFrameProvider(null);
	    this.cache.clearCaches();
	}

	frameProvider = null;

	setGraphicList(null);
	this.cache = null;
    }

    /**
     * Set the view attributes for the layer.  The frame provider view
     * attributes are updated, and the cache is cleared.
     * @param rva the RpfViewAttributes used for the layer.  
     */
    public void setViewAttributes(RpfViewAttributes rva) {
	viewAttributes = rva;
	if (this.cache != null) {
	    this.cache.setViewAttributes(rva);
	}
    }

    /** 
     * Get the view attributes or the layer.
     * @return RpfViewAttributes.
     */
    public RpfViewAttributes getViewAttributes() {
	return viewAttributes;
    }

    /**
     * Set the RpfCoverage tool used by the layer.  If the view
     * attributes chart series setting is not equal to
     * RpfViewAttributes.ANY, then the palette of the tool is not
     * shown.
     * @param cov the RpfCoverage tool.  
     */
    public void setCoverage(RpfCoverage cov) {
	coverage = cov;
	if (viewAttributes != null && coverage != null &&
	    !viewAttributes.chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY)) {
	    coverage.setShowPalette(false);
	}
    }

    /**
     * Return the coverage tool used by the layer.
     * @return RpfCoverage tool.
     */
    public RpfCoverage getCoverage() {
	return coverage;
    }

    /**
     * Set the RpfFrameProvider for the layer.  Clears out the cache,
     * and the frame provider gets the RpfViewAttributes held by the
     * layer.
     * @param fp the frame provider.  
     */
    public void setFrameProvider(RpfFrameProvider fp) {
	frameProvider = fp;
	if (this.cache != null) {
	    this.cache.setFrameProvider(frameProvider);
	}
    }

    /**
     * Return RpfFrameProvider used by the layer.
     */
    public RpfFrameProvider getFrameProvider() {
	return frameProvider;
    }

    /**
     *  Returns the Vector containing RpfCoverageBoxes that was
     *  returned from the RpfFrameProvider as a result of the last
     *  setCache call.  These provide rudimentary knowledge about what
     *  is being displayed.  This vector is from the primary cache
     *  handler.
     * @return Vector of RpfCoverageBoxes.  
     */
    public Vector getCoverageBoxes() {
	return this.cache.getCoverageBoxes();
    }

    /**
     * The projectionListener interface method that lets the Layer
     * know when the projection has changes, and therefore new graphics
     * have to created /supplied for the screen.
     *
     * @param e The projection event, most likely fired from a map
     * bean. 
     */
    public void projectionChanged(ProjectionEvent e) {
	projectionChanged(e, false);
    }

    /** 
     * Called from projectionListener interface method that lets the
     * Layer know when the projection has changes, and therefore new
     * graphics have to created /supplied for the screen.
     *
     * @param e The projection event, most likely fired from a map
     * bean. 
     * @param saveGraphicsForRedraw flag to test for whether the scale
     * and zone has changed for the projection.  If true, and the
     * scale and zone is the same, we'll just reproject and redraw the
     * current frames before getting new ones, to fake something
     * happening quickly.
     */
    public void projectionChanged(ProjectionEvent e, 
				  boolean saveGraphicsForRedraw) {
	Debug.message("basic", getName()+"|RpfLayer.projectionChanged()");

	// Need to grab a copy of the old projection in case
	// saveGraphicsForRedraw is true and the projection changes,
	// so we can test to see if the zone and scale have changed,
	// testing for reuse of current frames.
	Projection oldProj = getProjection();
	Projection newProj = setProjection(e);

	if (newProj == null) {
	    // Projection didn't change, nothing to do, already have
	    // good graphics and just need to paint...
	    repaint();
	    return;
	}

	if (saveGraphicsForRedraw && 
	    oldProj instanceof CADRG &&
	    newProj instanceof CADRG) {

	    CADRG cadrg1 = (CADRG) oldProj;
	    CADRG cadrg2 = (CADRG) newProj;
	    if (cadrg1.getScale() != cadrg2.getScale() ||
		cadrg1.getZone() != cadrg2.getZone()) {
		setGraphicList(null);
	    }
	    // else set graphic list escapes deletion...
	} else {
	    setGraphicList(null);
	}

	doPrepare();
    }

    /**
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the rpf.  <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the map will set the cancel bit in the
     * LayerThread, (the thread that is running the prepare).  If this
     * Layer needs to do any cleanups during the abort, it should do
     * so, but return out of the prepare asap.
     *
     * @return graphics list of images and attributes.
     */
    public OMGraphicList prepare() {

	if (isCancelled()) {
	    Debug.message("rpf", getName()+"|RpfLayer.prepare(): aborted.");
	    return null;
	}

	if (frameProvider == null) {
	    // Assuming running locally - otherwise the
	    // frameProvider should be set before we get here,
	    // like in setProperties or in the constructor.
	    setPaths(paths);
	    if (frameProvider == null) {
		// Doh! no paths were set!
		Debug.error(getName()+"|RpfLayer.prepare(): null frame provider - either no RPF paths were set, or no frame provider was assigned.  The RpfLayer has no way to get RPF data.");
		return new OMGraphicList();
	    }
	}

	if (this.cache == null) {
	    Debug.message("rpf", getName() + "|RpfLayer: Creating cache!");
	    this.cache = new RpfCacheManager(frameProvider, viewAttributes,
					     subframeCacheSize, auxSubframeCacheSize);
	}

	Projection projection = getProjection();

	if (coverage != null && coverage.isInUse()) {
	    coverage.prepare(frameProvider, projection, 
			     viewAttributes.chartSeries);
	}

	// Check to make sure the projection is CADRG
	if (!(projection instanceof CADRG) && 
	    (viewAttributes.showMaps || viewAttributes.showInfo)) {
	    fireRequestInfoLine("RpfLayer requires the CADRG projection for images or attributes!");
	    return null;
	}

	Debug.message("basic", getName()+"|RpfLayer.prepare(): doing it");

	// Setting the OMGraphicsList for this layer.  Remember, the
	// OMGraphicList is made up of OMGraphics, which are generated
	// (projected) when the graphics are added to the list.  So,
	// after this call, the list is ready for painting.

	// call getRectangle();
	if (Debug.debugging("rpf")) {
	    Debug.output(getName()+"|RpfLayer.prepare(): " +
			 "calling getRectangle " +
			 " with projection: " + projection +
			 " ul = " + projection.getUpperLeft() + " lr = " + 
			 projection.getLowerRight()); 
	}

	if (frameProvider.needViewAttributeUpdates()) {
	    frameProvider.setViewAttributes(viewAttributes);
	}

	OMGraphicList omGraphicList;
	try{
	    omGraphicList = this.cache.getRectangle(projection);
	} catch (java.lang.NullPointerException npe) {
	    Debug.error(getName() + 
			"|RpfLayer.prepare(): Something really bad happened - \n " +
			npe);
	    npe.printStackTrace();
	    omGraphicList = new OMGraphicList();
	    this.cache = null;
	}

	/////////////////////
	// safe quit
	int size = 0;
	if (omGraphicList != null) {
	    size = omGraphicList.size();	
	    if (Debug.debugging("basic")) {
		Debug.output("RpfLayer.prepare(): finished with "+
			     size + " graphics");
	    }
	}
	else 
	    Debug.message("basic", 
			  "RpfLayer.prepare(): finished with null graphics list");

	// Don't forget to project them.  Since they are only being
	// recalled if the projection hase changed, then we need to
	// force a reprojection of all of them because the screen
	// position has changed.
	omGraphicList.project(projection, true);
	return omGraphicList;
    }


    /**
     * Paints the layer.
     *
     * @param g the Graphics context for painting
     *
     */
    public void paint(java.awt.Graphics g) {
	Debug.message("rpf", "RpfLayer.paint()");

	OMGraphicList tmpGraphics = getGraphicList();

	if (tmpGraphics != null) {
	    tmpGraphics.render(g);
	}

	if (coverage != null && coverage.isInUse()) {
	    coverage.paint(g);
	}
    }

    
    //----------------------------------------------------------------------
    // GUI
    //----------------------------------------------------------------------
    private transient Box box = null;

    /**
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
	if (box == null) {
	    JCheckBox showMapsCheck, showInfoCheck, lockSeriesCheck;

	    box = Box.createVerticalBox();
	    Box box1 = Box.createVerticalBox();
	    Box box2 = Box.createVerticalBox();
	    JPanel topbox = new JPanel();
	    JPanel subbox2 = new JPanel();

	    showMapsCheck = new JCheckBox("Show Images", viewAttributes.showMaps);
	    showMapsCheck.setActionCommand(showMapsCommand);
	    showMapsCheck.addActionListener(this);

	    showInfoCheck = new JCheckBox("Show Attributes", viewAttributes.showInfo);
	    showInfoCheck.setActionCommand(showInfoCommand);
	    showInfoCheck.addActionListener(this);

	    boolean locked = viewAttributes.chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY)?false:true;
	    String lockedTitle = locked?
		(lockedButtonTitle + " - " + viewAttributes.chartSeries):
		unlockedButtonTitle;

	    lockSeriesCheck = new JCheckBox(lockedTitle, locked);
	    lockSeriesCheck.setActionCommand(lockSeriesCommand);
	    lockSeriesCheck.addActionListener(this);

	    box1.add(showMapsCheck);
	    box1.add(showInfoCheck);
	    box1.add(lockSeriesCheck);

	    if (coverage != null) {
		JCheckBox showCoverageCheck;
		if (coverage.isShowPalette()) {
		    showCoverageCheck = new JCheckBox("Show Coverage Tool", false);
		} else {
		    showCoverageCheck = new JCheckBox("Show Coverage", false);
		}
		showCoverageCheck.setActionCommand(showCoverageCommand);
		showCoverageCheck.addActionListener(this);
		box1.add(showCoverageCheck);
	    }

	    topbox.add(box1);
	    topbox.add(box2);
	    box.add(topbox);

	    JPanel opaquePanel = PaletteHelper.createPaletteJPanel("Map Opaqueness");
	    JSlider opaqueSlide = new JSlider(
		JSlider.HORIZONTAL, 0/*min*/, 255/*max*/, 
		viewAttributes.opaqueness/*inital*/);
	    java.util.Hashtable dict = new java.util.Hashtable();
	    dict.put(new Integer(0), new JLabel("clear"));
	    dict.put(new Integer(255), new JLabel("opaque"));
	    opaqueSlide.setLabelTable(dict);
	    opaqueSlide.setPaintLabels(true);
	    opaqueSlide.setMajorTickSpacing(50);
	    opaqueSlide.setPaintTicks(true);
	    opaqueSlide.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent ce) {
			JSlider slider = (JSlider) ce.getSource();
			if (slider.getValueIsAdjusting()) {
			    viewAttributes.opaqueness = slider.getValue();
			    fireRequestInfoLine("RPF Opaqueness set to " + 
						viewAttributes.opaqueness + 
						" for future requests.");
			}
		    }
		});
	    opaquePanel.add(opaqueSlide);
	    box.add(opaquePanel);

	    JButton redraw = new JButton("Redraw RPF Layer");
	    redraw.addActionListener(this);
	    subbox2.add(redraw);
	    box.add(subbox2);
	}
	return box;
    }


    //----------------------------------------------------------------------
    // ActionListener interface implementation
    //----------------------------------------------------------------------

    /**
     * The Action Listener method, that reacts to the palette widgets
     * actions.
     */
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	String cmd = e.getActionCommand();
	if (cmd == showMapsCommand) {
	    JCheckBox mapCheck = (JCheckBox)e.getSource();
	    viewAttributes.showMaps = mapCheck.isSelected();
	    repaint();
	} else if (cmd == showInfoCommand) {
	    JCheckBox infoCheck = (JCheckBox)e.getSource();
	    viewAttributes.showInfo = infoCheck.isSelected();
	    repaint();
	} else if (cmd == lockSeriesCommand) {
	    JCheckBox lockCheck = (JCheckBox)e.getSource();
	    boolean locked = lockCheck.isSelected();
	    if (locked) {
		Vector vector = getCoverageBoxes();
		String seriesName;

		if (vector == null || vector.size() == 0) {
		    seriesName = RpfViewAttributes.ANY;
		} else {
		    seriesName = ((RpfCoverageBox)vector.elementAt(0)).chartCode;
		}
		lockCheck.setText(lockedButtonTitle + " - " + 
				  seriesName);
		viewAttributes.chartSeries = seriesName;

	    } else {
		lockCheck.setText(unlockedButtonTitle);
		viewAttributes.chartSeries = RpfViewAttributes.ANY;
	    }

	} else if (cmd == showCoverageCommand) {
	    if (coverage != null) {
		JCheckBox coverageCheck = (JCheckBox)e.getSource();
		coverage.setInUse(coverageCheck.isSelected());
		if (coverage.isInUse()) {
		    coverage.prepare(frameProvider, getProjection(),
				     viewAttributes.chartSeries);
		}
		repaint();
	    }
	} else {
//  	    Debug.error("RpfLayer: Unknown action command \"" + cmd +
//  			"\" in RpfLayer.actionPerformed().");

	    // OK, not really sure what happened, just act like a
	    // reset.
	    doPrepare();
	}
    }

    /** Print out the contents of a properties file. */
    public static void main(String[] argv) {
	System.out.println("#########################################");
	System.out.println("# Properties for the JAVA RpfLayer");
	System.out.println("# Mandatory properties:");
	System.out.println("layer.class=com.bbn.openmap.layer.rpf.RpfLayer");
	System.out.println("layer.prettyName=CADRG");
	System.out.println("# This property should reflect the paths to the RPF directories");
	System.out.println("layer.paths=<Path to RPF dir>;/cdrom/cdrom0/RPF");
	System.out.println("# Optional properties - Defaults will be set for properties not included (defaults are listed):");
	System.out.println("# Number between 0-255: 0 is transparent, 255 is opaque");
	System.out.println("layer.opaque=255");
	System.out.println("# Number of colors to use on the maps - 16, 32, 216");
	System.out.println("layer.numberColors=216");
	System.out.println("# Display maps on startup");
	System.out.println("layer.showMaps=true");
	System.out.println("# Display attribute information on startup");
	System.out.println("layer.showInfo=false");
	System.out.println("# Scale images to match map scale");
	System.out.println("layer.scaleImages=true");
	System.out.println("# The scale factor to allow when scaling images (2x, 4x, also mean 1/2, 1/4).  Default is 4.");
	System.out.println("rpf.imageScaleFactor=4");
	System.out.println("# Reset the cache if layer is removed from map");
	System.out.println("layer.killCache=false");
	System.out.println("# Limit the display to the chart code specified. (GN, JN, ON, TP, etc.)");
	System.out.println("layer.chartSeries=ANY");
	System.out.println("# Set the subframe cache size. (Number of subframes to hold on to, 256x256 pixels");
	System.out.println("layer.subframeCacheSize=128");
	System.out.println("# Get the subframe attribute data from the frame provider.");
	System.out.println("rpf.autofetchAttributes=false");
	System.out.println("#If you want the coverage tool to be available");
	System.out.println("layer.coverage=true");
	System.out.println("#Then add coverage constants as needed.");
    }
}
