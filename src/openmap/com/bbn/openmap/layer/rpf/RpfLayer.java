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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfLayer.java,v $
// $RCSfile: RpfLayer.java,v $
// $Revision: 1.23 $
// $Date: 2008/09/17 20:47:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

/*  Java Core  */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.bbn.openmap.I18n;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.event.MapMouseInterpreter;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheHandler;

/**
 * The RpfLayer fills the screen with RPF data. There is also a tool available
 * that allows you to see the coverage of the available data. To view theimages,
 * the projection of the map has to be set in the ARC projection, which OpenMap
 * calls the CADRG projection. The RpfLayer can use several RPF directories at
 * the same time, and doesn't require that the data actually be there at
 * runtime. That way, you can give a location where the data may be mouted
 * during runtime(i.e. CDROM) and the layer will still use the data. The scale
 * of the projection does not necessarily have to match the scale of a map
 * series for that series to be displayed. There are options, set in the
 * RpfViewAttributes, that allow scaling of the RPF images to match the map
 * scale.
 * <P>
 * 
 * The RpfLayer uses the RpfCacheManager to get the images it needs to display.
 * Whenever the projection changes, the cache manager takes the new projection
 * and creates a OMGraphicList with the new image frames and attribute text.
 * <P>
 * 
 * The RpfLayer gets its initial settings from properties. This should be done
 * right after the RpfLayer is created. The properties list contains the
 * location of the RPF directories, the opaqueness of the images, the number of
 * colors to use, and whether to show the images and/or attributes by default.
 * An example of the RpfLayer properties:
 * <P>
 * 
 * <pre>
 * 
 * 
 *           #-----------------------------
 *           # Properties for RpfLayer
 *           #-----------------------------
 *           # Mandatory properties
 *           # This property should reflect the paths to the RPF directories
 *           rpf.paths=/usr/local/matt/data/RPF;/usr/local/matt/data/CIB/RPF
 *           
 *           # Optional Properties - the default will be set if these are not 
 *           # included in the properties file: 
 *           # Number between 0-255: 0 is transparent, 255 is opaque.  255 is default.
 *           rpf.opaque=128
 *           
 *           # Number of colors to use on the maps - 16, 32, 216.  216 is default.
 *           rpf.numberColors=216
 *           
 *           # Display maps on startup.  Default is true.
 *           rpf.showMaps=true
 *           
 *           # Display attribute information on startup.  Default is false.
 *           rpf.showInfo=false
 *           
 *           # Scale charts to match display scale.  Default is true.
 *           rpf.scaleImages=true
 *           
 *           # The scale factor to allow when scaling images (2x, 4x, also mean 1/2, 1/4).  Default is 4.
 *           rpf.imageScaleFactor=4
 *           
 *           # Delete the cache if the layer is removed from the map.  Default is false.
 *           rpf.killCache=true
 *           # Limit the display to the chart code specified. (GN, JN, ON, TP, etc.).
 *           # Default is ANY
 *           rpf.chartSeries=ANY
 *           # Get the subframe attribute data from the Frame provider.
 *           rpf.autofetchAttributes=false
 *           # Set to true if you want the coverage tool available.
 *           rpf.coverage=true
 *           # Set the subframe cache size. (Number of subframes to hold on to, 256x256 pixels)
 *           rpf.subframeCacheSize=128
 *           # Then also include coverage properties, which are available in the RpfConstants.
 *           #------------------------------------
 *           # End of properties for RpfLayer
 *           #------------------------------------
 * 
 * 
 * </pre>
 * 
 */
public class RpfLayer
        extends OMGraphicHandlerLayer
        implements ActionListener, RpfConstants, Serializable {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.layer.rpf.RpfLayer");
    protected static Logger rpfLogger = Logger.getLogger("RPF");

    private static final long serialVersionUID = 1L;
    /**
     * The main source for the images and attribute information. All requests
     * for graphic objects should go through this cache, and it will
     * automatically handle getting the frame files, decoding them, and
     * returning an object list.
     */
    protected transient RpfCacheManager cache = null;
    /** The paths to the RPF directories, telling where the data is. */
    protected String[] paths;
    /**
     * The display attributes for the maps. This object should not be replaced,
     * because the caches all look at it, too. Just adjust the parameters within
     * it.
     * 
     * @see RpfViewAttributes
     */
    protected RpfViewAttributes viewAttributes;
    /** Flag to delete the cache if the layer is removed from the map. */
    protected boolean killCache = true;
    /** The supplier of frame data. */
    protected RpfFrameProvider frameProvider;
    /** The coverage tool for the layer. */
    protected RpfCoverage coverage;
    /** Subframe cache size. Default is 40. */
    protected int subframeCacheSize = RpfCacheHandler.SUBFRAME_CACHE_SIZE;
    /** Auxiliary subframe cache size. Default is 10. */
    protected int auxSubframeCacheSize = RpfCacheManager.SMALL_CACHE_SIZE;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values. Use this construct if you are going to use a
     * standard properties file, which will set the paths.
     */
    public RpfLayer() {
        setName("RPF");
        viewAttributes = new RpfViewAttributes();
        setRenderPolicy(new BufferedImageRenderPolicy(this));
        setMouseModeIDsForEvents(new String[] {
            SelectMouseMode.modeID
        });

        showSubframes(false);
    }

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     * 
     * @param pathsToRPFDirs paths to the RPF directories that hold A.TOC files.
     */
    public RpfLayer(String[] pathsToRPFDirs) {
        this();
        setPaths(pathsToRPFDirs);
    }

    /**
     * Set the paths to the RPF directories, which are by default the parents of
     * the A.TOC table of contents files. Creates the RpfFrameProvider.
     * 
     * @param pathsToRPFDirs Array of strings that list the paths to RPF
     *        directories.
     */
    public void setPaths(String[] pathsToRPFDirs) {
        if (paths != null && pathsToRPFDirs != null && paths.length == pathsToRPFDirs.length) {
            // If the paths haven't changed, don't do anything.
            boolean same = true;
            for (int i = 0; i < paths.length; i++) {
                same = same && paths[i].equals(pathsToRPFDirs[i]);
            }

            if (same && frameProvider != null) {
                return;
            }
        }

        if (pathsToRPFDirs != null) {
            setFrameProvider(new RpfFrameCacheHandler(pathsToRPFDirs));
        } else {
            logger.warning("Need RPF directory paths.");
            frameProvider = null;
        }
        paths = pathsToRPFDirs;

        setCoverage(new RpfCoverage(this));

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
     * Called when the layer is no longer part of the map. In this case, we
     * should disconnect from the server if we have a link.
     */
    public void removed(java.awt.Container cont) {
        if (killCache) {
            rpfLogger.fine("emptying cache!");
            clearCache();
        }

        // need to reset this for when it gets added again, if it was
        // removed without the projection actually changing. This
        // helps when the cache needs to be rebuilt.
        setProjection((Projection) null);
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

        setPaths(PropUtils.initPathsFromProperties(properties, prefix + RpfPathsProperty, paths));

        viewAttributes.setProperties(prefix, properties);
        showSubframes(viewAttributes.showInfo);

        subframeCacheSize = PropUtils.intFromProperties(properties, prefix + CacheSizeProperty, subframeCacheSize);

        auxSubframeCacheSize = PropUtils.intFromProperties(properties, prefix + CacheSizeProperty, auxSubframeCacheSize);

        if (viewAttributes.chartSeries == null)
            viewAttributes.chartSeries = RpfViewAttributes.ANY;

        killCache = PropUtils.booleanFromProperties(properties, prefix + KillCacheProperty, killCache);

        if (coverage != null) {
            coverage.setProperties(prefix, properties);
        }

        resetPalette();
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
     * 
     * @param props a Properties object to load the PropertyConsumer properties
     *        into. If props equals null, then a new Properties object should be
     *        created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
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
                        pathString.append(";"); // separate paths with
                        // ;
                    }
                }
            }
            props.put(prefix + RpfPathsProperty, pathString.toString());
        } else {
            props.put(prefix + RpfPathsProperty, "");
        }

        props.put(prefix + KillCacheProperty, new Boolean(killCache).toString());
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
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.). For Layer, this method should at least
     * return the 'prettyName' property.
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
        String interString;

        interString =
                i18n.get(RpfLayer.class, RpfPathsProperty, I18n.TOOLTIP, "Paths to RPF directories.  Semi-colon separated paths.");
        list.put(RpfPathsProperty, interString);
        list.put(RpfPathsProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.MultiDirectoryPropertyEditor");
        interString = i18n.get(RpfLayer.class, RpfPathsProperty, "Data Path");
        list.put(RpfPathsProperty + LabelEditorProperty, interString);

        interString =
                i18n.get(RpfLayer.class, KillCacheProperty, I18n.TOOLTIP,
                         "Flag to trigger the cache to be cleared when layer is removed from the map.");
        list.put(KillCacheProperty, interString);
        list.put(KillCacheProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
        interString = i18n.get(RpfLayer.class, KillCacheProperty, "Clear Cache");
        list.put(KillCacheProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, CacheSizeProperty, I18n.TOOLTIP, "Number of frames to hold in the frame cache.");
        list.put(CacheSizeProperty, interString);
        interString = i18n.get(RpfLayer.class, CacheSizeProperty, "Frame Cache Size");
        list.put(CacheSizeProperty + LabelEditorProperty, interString);

        interString =
                i18n.get(RpfLayer.class, AuxCacheSizeProperty, I18n.TOOLTIP, "Number of frames to hold in aux. frame caches.");
        list.put(AuxCacheSizeProperty, interString);
        interString = i18n.get(RpfLayer.class, AuxCacheSizeProperty, "Aux Frame Cache Size");
        list.put(AuxCacheSizeProperty + LabelEditorProperty, interString);

        viewAttributes.getPropertyInfo(list);

        RpfCoverage tmpCov = coverage;
        if (tmpCov == null) {
            tmpCov = new RpfCoverage(this);
        }

        tmpCov.getPropertyInfo(list);

        list.put(initPropertiesProperty,
                 RpfPathsProperty + " " + KillCacheProperty + " " + CacheSizeProperty + " " + AuxCacheSizeProperty + " "
                         + viewAttributes.getInitPropertiesOrder() + " " + AddToBeanContextProperty + " " + AddAsBackgroundProperty
                         + " " + RemovableProperty + " " + CoverageProperty + " " + tmpCov.getInitPropertiesOrder());

        return list;
    }

    public void resetPalette() {
        box = null;
        if (coverage != null) {
            if (coverage.isInUse()) {
                coverage.resetColors();
            }
        }

        super.resetPalette();
    }

    /**
     * Sets the current graphics list to the given list.
     * 
     * @param aList a list of OMGraphics.
     */
    public synchronized void setGraphicList(OMGraphicList aList) {
        setList(aList);
    }

    /** Retrieves the current graphics list. */
    public synchronized OMGraphicList getGraphicList() {
        return getList();
    }

    /**
     * Clear the frame cache.
     */
    public void clearCache() {

        if (frameProvider instanceof CacheHandler) {
            ((CacheHandler) frameProvider).resetCache();
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
     * Set the view attributes for the layer. The frame provider view attributes
     * are updated, and the cache is cleared.
     * 
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
     * 
     * @return RpfViewAttributes.
     */
    public RpfViewAttributes getViewAttributes() {
        return viewAttributes;
    }

    /**
     * Set the RpfCoverage tool used by the layer. If the view attributes chart
     * series setting is not equal to RpfViewAttributes.ANY, then the palette of
     * the tool is not shown.
     * 
     * @param cov the RpfCoverage tool.
     */
    public void setCoverage(RpfCoverage cov) {
        coverage = cov;
        if (coverage != null) {
            if (viewAttributes != null && !viewAttributes.chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY)) {
                coverage.setShowPalette(false);
            }
            coverage.coverageManager = null;
        }
    }

    /**
     * Return the coverage tool used by the layer.
     * 
     * @return RpfCoverage tool.
     */
    public RpfCoverage getCoverage() {
        return coverage;
    }

    /**
     * Set the RpfFrameProvider for the layer. Clears out the cache, and the
     * frame provider gets the RpfViewAttributes held by the layer.
     * 
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
     * Returns the Vector containing RpfCoverageBoxes that was returned from the
     * RpfFrameProvider as a result of the last setCache call. These provide
     * rudimentary knowledge about what is being displayed. This vector is from
     * the primary cache handler.
     * 
     * @return Vector of RpfCoverageBoxes.
     */
    public Vector<RpfCoverageBox> getCoverageBoxes() {
        return this.cache.getCoverageBoxes();
    }

    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method call is made on the rpf.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this happens,
     * the map will set the cancel bit in the LayerThread, (the thread that is
     * running the prepare). If this Layer needs to do any cleanups during the
     * abort, it should do so, but return out of the prepare asap.
     * 
     * @return graphics list of images and attributes.
     */
    public synchronized OMGraphicList prepare() {

        if (isCancelled()) {
            rpfLogger.fine("aborted.");
            return null;
        }

        Projection projection = getProjection();
        OMGraphicList retList = new OMGraphicList();
        retList.setTraverseMode(OMList.FIRST_ADDED_ON_TOP);

        if (frameProvider == null) {
            // Assuming running locally - otherwise the
            // frameProvider should be set before we get here,
            // like in setProperties or in the constructor.
            setPaths(paths);
            if (frameProvider == null) {
                // Doh! no paths were set!
                logger.warning(getName()
                        + ": null frame provider - either no RPF paths were set, or no frame provider was assigned.  The RpfLayer has no way to get RPF data.");
                return retList;
            }
        }

        if (coverage != null && coverage.isInUse()) {
            coverage.prepare(frameProvider, projection, viewAttributes.chartSeries);
            retList.add(coverage.getGraphicLists());
        }

        // Check the current minScale and maxScale set on the layer, ignore if
        // projection scale is out of range.
        if (!isProjectionOK(projection)) {
            return retList;
        }

        if (this.cache == null) {
            rpfLogger.fine(getName() + ": Creating cache!");
            this.cache = new RpfCacheManager(frameProvider, viewAttributes, subframeCacheSize, auxSubframeCacheSize);
        }

        // Check to make sure the projection is CADRG
        if (!(projection instanceof EqualArc) && (viewAttributes.showMaps || viewAttributes.showInfo)) {
            //fireRequestInfoLine("RpfLayer runs faster with an Equal Arc projection (CADRG/LLXY).");
        }

        rpfLogger.fine(getName() + " doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // OMGraphicList is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        // call getRectangle();
        if (rpfLogger.isLoggable(Level.FINE)) {
            rpfLogger.fine(getName() + "calling getRectangle " + " with projection: " + projection + " ul = "
                    + projection.getUpperLeft() + " lr = " + projection.getLowerRight());
        }

        if (frameProvider.needViewAttributeUpdates()) {
            frameProvider.setViewAttributes(viewAttributes);
        }

        try {

            // OMGraphics are generated by the RpfCacheHandlers when fetched
            retList.addAll(this.cache.getRectangle(projection));

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(getName() + ": finished with " + retList.size() + " graphics");
            }

        } catch (java.lang.NullPointerException npe) {
            logger.warning(getName() + ": Something really bad happened - \n " + npe);
            npe.printStackTrace();
            retList = new OMGraphicList();
            this.cache = null;
        }

        return retList;
    }

//    public void paint(java.awt.Graphics g) {
//        logger.info("painting started");
//        super.paint(g);
//        logger.info("painting complete");
//    }

    public boolean isHighlightable(OMGraphic omg) {
        return viewAttributes.showInfo && omg instanceof OMRaster && omg.isSelected();
    }

    public String getToolTipTextFor(OMGraphic omg) {
        return (String) omg.getAttribute(OMGraphic.TOOLTIP);
    }

    public void highlight(OMGraphic omg) {

    }

    public void unhighlight(OMGraphic omg) {

    }

    // ----------------------------------------------------------------------
    // GUI
    // ----------------------------------------------------------------------
    private transient Box box = null;

    /**
     * Provides the palette widgets to control the options of showing maps, or
     * attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
        if (box == null) {
            JCheckBox showInfoCheck, lockSeriesCheck;

            box = Box.createVerticalBox();
            Box box1 = Box.createVerticalBox();
            JPanel topbox = new JPanel();
            JPanel subbox2 = new JPanel();

            showInfoCheck = new JCheckBox("Show Attributes", viewAttributes.showInfo);
            showInfoCheck.setActionCommand(showInfoCommand);
            showInfoCheck.addActionListener(this);

            String tmpCS = viewAttributes.chartSeries;
            if (tmpCS == null) {
                tmpCS = RpfViewAttributes.ANY;
            }

            boolean locked = !tmpCS.equalsIgnoreCase(RpfViewAttributes.ANY);
            String lockedTitle = locked ? (lockedButtonTitle + " - " + tmpCS) : unlockedButtonTitle;

            lockSeriesCheck = new JCheckBox(lockedTitle, locked);
            lockSeriesCheck.setActionCommand(lockSeriesCommand);
            lockSeriesCheck.addActionListener(this);

            // box1.add(showMapsCheck);
            box1.add(showInfoCheck);
            box1.add(lockSeriesCheck);

            if (coverage != null) {
                JCheckBox showCoverageCheck = new JCheckBox("Show Coverage", coverage.isInUse());
                showCoverageCheck.setActionCommand(showCoverageCommand);
                showCoverageCheck.addActionListener(this);
                box1.add(showCoverageCheck);
            }

            JButton setProperties = new JButton(i18n.get(RpfLayer.class, "setProperties", "Change All Settings"));
            setProperties.setActionCommand(DisplayPropertiesCmd);
            setProperties.addActionListener(this);
            box1.add(setProperties);

            topbox.add(box1);
            box.add(topbox);

            JPanel opaquePanel =
                    getTransparencyAdjustmentPanel(i18n.get(RpfLayer.class, "layerTransparencyGUILabel", "Layer Transparency"),
                                                   JSlider.HORIZONTAL, viewAttributes.opaqueness / 255f);
            box.add(opaquePanel);

            JButton redraw = new JButton("Redraw RPF Layer");
            redraw.addActionListener(this);
            subbox2.add(redraw);
            box.add(subbox2);
        }
        return box;
    }

    // public void setTransparency(float value) {
    // super.setTransparency(value);
    // viewAttributes.opaqueness = (int) (value * 255f);
    // }

    // ----------------------------------------------------------------------
    // ActionListener interface implementation
    // ----------------------------------------------------------------------

    /**
     * The Action Listener method, that reacts to the palette widgets actions.
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        String cmd = e.getActionCommand();
        if (cmd == showMapsCommand) {
            JCheckBox mapCheck = (JCheckBox) e.getSource();
            viewAttributes.showMaps = mapCheck.isSelected();
            repaint();
        } else if (cmd == showInfoCommand) {
            JCheckBox infoCheck = (JCheckBox) e.getSource();
            boolean showInfo = infoCheck.isSelected();
            viewAttributes.showInfo = showInfo;
            showSubframes(showInfo);
            doPrepare();
        } else if (cmd == lockSeriesCommand) {
            JCheckBox lockCheck = (JCheckBox) e.getSource();
            boolean locked = lockCheck.isSelected();
            if (locked) {
                Vector<RpfCoverageBox> coverageBoxes = getCoverageBoxes();
                String seriesName;

                if (coverageBoxes == null || coverageBoxes.isEmpty()) {
                    seriesName = RpfViewAttributes.ANY;
                } else {
                    seriesName = (coverageBoxes.elementAt(0)).chartCode;
                }

                if (seriesName == null) {
                    seriesName = RpfViewAttributes.ANY;
                    fireRequestMessage("The "
                            + getName()
                            + " Layer is having trouble determining what kind\nof charts are being displayed.  Can't establish lock for charts\ncurrently being viewed.");
                }

                lockCheck.setText(lockedButtonTitle + " - " + seriesName);
                viewAttributes.chartSeries = seriesName;

            } else {
                lockCheck.setText(unlockedButtonTitle);
                viewAttributes.chartSeries = RpfViewAttributes.ANY;
            }

        } else if (cmd == showCoverageCommand) {
            if (coverage != null) {
                JCheckBox coverageCheck = (JCheckBox) e.getSource();
                coverage.setInUse(coverageCheck.isSelected());
                if (coverage.isInUse()) {
                    coverage.prepare(frameProvider, getProjection(), viewAttributes.chartSeries);
                }
                doPrepare();
            }
        } else {
            // logger.warning("RpfLayer: Unknown action command \"" + cmd
            // +
            // "\" in RpfLayer.actionPerformed().");

            // OK, not really sure what happened, just act like a
            // reset.
            doPrepare();
        }
    }

    protected void showSubframes(boolean show) {
        OMGraphicList list = getList();
        if (list != null) {
            list.setSelected(show);
        }

        MapMouseInterpreter mmi = getMouseEventInterpreter();
        if (mmi != null) {
            mmi.setActive(show);
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