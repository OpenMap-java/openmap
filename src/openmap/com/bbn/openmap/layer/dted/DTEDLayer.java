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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDLayer.java,v $
// $RCSfile: DTEDLayer.java,v $
// $Revision: 1.13 $
// $Date: 2005/12/09 21:09:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

/*  Java Core  */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.event.MapMouseInterpreter;
import com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * The DTEDLayer fills the screen with DTED data. To view the DTED images, the
 * projection has to be set in a Equal ARC projection, which OpenMap calls the
 * CADRG projection or the LLXY projection. In Gesture mode, clicking on the map
 * will cause the DTEDLayer to place a point on the window and show the
 * elevation of that point. The Gesture response is not dependent on the scale
 * or projection of the screen.
 * <P>
 * 
 * The DTEDLayer uses the DTEDCacheManager to get the images it needs. The
 * DTEDLayer receives projection change events, and then asks the cache manager
 * for the images it needs based on the new projection.
 * 
 * The DTEDLayer also relies on properties to set its variables, such as the
 * dted frame paths (there can be several at a time), the opaqueness of the
 * frame images, number of colors to use, and some other display variables. The
 * DTEDLayer properties look something like this:
 * <P>
 * 
 * NOTE: Make sure your DTED file and directory names are in lower case. You can
 * use the com.bbn.openmap.util.wanderer.ChangeCase class to make modifications
 * if necessary.
 * <P>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 *     #------------------------------
 *     # Properties for DTEDLayer
 *     #------------------------------
 *     # This property should reflect the paths to the dted level 0, 1 and newer 2 (file extension .dt2) data directories, separated by a semicolon.
 *     dted.paths=/usr/local/matt/data/dted;/cdrom/cdrom0/dted
 *     
 *     # Number between 0-255: 0 is transparent, 255 is opaque
 *     dted.opaque=255
 *     
 *     # Number of colors to use on the maps - 16, 32, 216
 *     dted.number.colors=216
 *     
 *     # Level of DTED data to use 0, 1, 2
 *     dted.level=0
 *     
 *     # Type of display for the data
 *     # 0 = no shading at all
 *     # 1 = greyscale slope shading
 *     # 2 = band shading, in meters
 *     # 3 = band shading, in feet
 *     # 4 = subframe testing
 *     # 5 = elevation, colored
 *     dted.view.type=5
 *     
 *     # Contrast setting, 1-5
 *     dted.contrast=3
 *     
 *     # height (meters or feet) between color changes in band shading
 *     dted.band.height=25
 *     
 *     # Minumum scale to display images. Larger numbers mean smaller scale, 
 *     # and are more zoomed out.
 *     dted.min.scale=20000000
 *     
 *     # Delete the cache if the layer is removed from the map.
 *     dted.kill.cache=true
 *     # Number of frames to hold in the cache. The default is 
 *     # DTEDFrameCache.FRAME_CACHE_SIZE, which is 15 to help smaller systems.  Better
 *     # caching happens, the larger the number.
 *     dted.cacheSize=40
 *     #-------------------------------------
 *     # End of properties for DTEDLayer
 *     #-------------------------------------
 * 
 * 
 * 
 * 
 * </pre>
 * 
 * @see com.bbn.openmap.layer.rpf.ChangeCase
 */
public class DTEDLayer
        extends OMGraphicHandlerLayer {

    /** The cache manager. */
    protected transient DTEDCacheManager cache = null;
    /**
     * Set when the projection has changed while a swing worker is gathering
     * graphics, and we want him to stop early.
     */
    protected boolean cancelled = false;
    /**
     * The paths to the DTED Level 0, 1 directories, telling where the data is.
     * Newer level 2 data, with the .dt2 file extensions, should be set in this
     * path property.
     */
    protected String[] paths;
    /**
     * The level of DTED to use. Level 0 is 1km post spacing, Level 1 is 100m
     * post spacing. Level 2 is 30m post spacing
     */
    protected int dtedLevel = DTEDFrameSubframe.LEVEL_0;
    /**
     * The display type for the dted images. Slope shading is greyscale terrain
     * modeling with highlights and shading, with the 'sun' being in the
     * NorthWest. Colored Elevation shading is the same thing, except colors are
     * added to indicate the elevation. Band shading colors the pixels according
     * to a range of elevations.
     */
    protected int viewType = DTEDFrameSubframe.NOSHADING;
    /** The elevation range to use for each color in band shading. */
    protected int bandHeight = 25;
    /** A contrast adjustment, for slope shading (1-5). */
    protected int slopeAdjust = DTEDFrameSubframe.DEFAULT_SLOPE_ADJUST;
    protected int numColors = DTEDFrameColorTable.DTED_COLORS;
    protected int opaqueness = DTEDFrameColorTable.DEFAULT_OPAQUENESS;
    /** Flag to delete the cache if the layer is removed from the map. */
    protected boolean killCache = true;
    /** The number of frames held by the cache objects. */
    protected int cacheSize = DTEDCacheHandler.FRAME_CACHE_SIZE;

    public static final String DTEDPathsProperty = "paths";
    public static final String DTED2PathsProperty = "level2.paths";
    public static final String OpaquenessProperty = "opaque";
    public static final String NumColorsProperty = "number.colors";
    public static final String DTEDLevelProperty = "level";
    public static final String DTEDViewTypeProperty = "view.type";
    public static final String DTEDSlopeAdjustProperty = "contrast";
    public static final String DTEDBandHeightProperty = "band.height";
    public static final String DTEDMinScaleProperty = "min.scale";
    public static final String DTEDKillCacheProperty = "kill.cache";
    public static final String DTEDFrameCacheSizeProperty = "cacheSize";

    private String level0Command = "setLevelTo0";
    private String level1Command = "setLevelTo1";
    private String level2Command = "setLevelTo2";

    /** The elevation spot used in the gesture mode. */
    DTEDLocation location = null;

    /**
     * Instances of this class are used to display elevation labels on the map.
     */
    static class DTEDLocation {
        OMText text;
        OMRect dot;

        public DTEDLocation(int x, int y) {
            text = new OMText(x + 10, y, (String) null, (java.awt.Font) null, OMText.JUSTIFY_LEFT);

            dot = new OMRect(x - 1, y - 1, x + 1, y + 1);
            text.setLinePaint(java.awt.Color.red);
            dot.setLinePaint(java.awt.Color.red);
        }

        /**
         * Set the text to the elevation text.
         * 
         * @param elevation elevation of the point in meters.
         */
        public void setElevation(int elevation) {
            // m - ft conversion
            if (elevation < -100)
                text.setData("No Data Here");
            else {
                int elevation_ft = (int) ((float) elevation * 3.280840f);
                text.setData(elevation + " m / " + elevation_ft + " ft");
            }
        }

        /** Set the x-y location of the combo in the screen */
        public void setLocation(int x, int y) {
            text.setX(x + 10);
            text.setY(y);
            dot.setLocation(x - 1, y - 1, x + 1, y + 1);
        }

        public void render(java.awt.Graphics g) {
            text.render(g);
            dot.render(g);
        }

        public void generate(Projection proj) {
            text.generate(proj);
            dot.generate(proj);
        }
    }

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public DTEDLayer() {
        this(null);
    }

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     * 
     * @param pathsToDTEDDirs paths to the DTED directories that hold level 0, 1
     *        and 2 data.
     */
    public DTEDLayer(String[] pathsToDTEDDirs) {
        setName("DTED");
        setDefaultValues();
        setPaths(pathsToDTEDDirs);
        setMouseModeIDsForEvents(new String[] {
            "Gestures"
        });

        setRenderPolicy(new BufferedImageRenderPolicy(this));
    }

    /**
     * Set the paths to the DTED directories.
     */
    public void setPaths(String[] pathsToDTEDDirs) {
        paths = pathsToDTEDDirs;
        if (cache != null) {
            cache.setDtedDirPaths(pathsToDTEDDirs);
        }
    }

    /**
     * Get the paths to the DTED directories.
     */
    public String[] getPaths() {
        return paths;
    }

    public DTEDCacheManager getCache() {
        if (cache == null) {
            // Debug.output("DTEDLayer: Creating cache! (This is a one-time operation!)");
            cache = new DTEDCacheManager(paths, numColors, opaqueness);
            cache.setCacheSize(cacheSize);
            DTEDFrameSubframeInfo dfsi = new DTEDFrameSubframeInfo(viewType, bandHeight, dtedLevel, slopeAdjust);
            cache.setSubframeInfo(dfsi);
        }
        return cache;
    }

    public void setCache(DTEDCacheManager cache) {
        this.cache = cache;
    }

    protected void setDefaultValues() {
        // defaults
        paths = null;
        setOpaqueness(DTEDFrameColorTable.DEFAULT_OPAQUENESS);
        setDtedLevel(DTEDFrameSubframe.LEVEL_0);
        setBandHeight(DTEDFrameSubframe.DEFAULT_BANDHEIGHT);
        setSlopeAdjust(DTEDFrameSubframe.DEFAULT_SLOPE_ADJUST);
        setViewType(DTEDFrameSubframe.COLOREDSHADING);
        setMaxScale(20000000);
    }

    /**
     * Set all the DTED properties from a properties object.
     */
    public void setProperties(String prefix, java.util.Properties properties) {

        super.setProperties(prefix, properties);
        prefix = PropUtils.getScopedPropertyPrefix(this);

        paths = PropUtils.initPathsFromProperties(properties, prefix + DTEDPathsProperty, paths);
        setOpaqueness(PropUtils.intFromProperties(properties, prefix + OpaquenessProperty, getOpaqueness()));

        setNumColors(PropUtils.intFromProperties(properties, prefix + NumColorsProperty, getNumColors()));

        setDtedLevel(PropUtils.intFromProperties(properties, prefix + DTEDLevelProperty, getDtedLevel()));

        setViewType(PropUtils.intFromProperties(properties, prefix + DTEDViewTypeProperty, getViewType()));

        setSlopeAdjust(PropUtils.intFromProperties(properties, prefix + DTEDSlopeAdjustProperty, getSlopeAdjust()));

        setBandHeight(PropUtils.intFromProperties(properties, prefix + DTEDBandHeightProperty, getBandHeight()));

        // The Layer maxScale is talking the place of the DTEDLayer minScale
        // property.
        setMaxScale(PropUtils.floatFromProperties(properties, prefix + DTEDMinScaleProperty, getMaxScale()));

        setCacheSize((int) PropUtils.intFromProperties(properties, prefix + DTEDFrameCacheSizeProperty, getCacheSize()));

        setKillCache(PropUtils.booleanFromProperties(properties, prefix + DTEDKillCacheProperty, getKillCache()));

    }

    /**
     * Called when the layer is no longer part of the map. In this case, we
     * should disconnect from the server if we have a link.
     */
    public void removed(java.awt.Container cont) {
        if (killCache) {
            Debug.output("DTEDLayer: emptying cache!");
            cache = null;
        }
    }
    
    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method call is made on the dted.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this happens,
     * the map will set the cancel bit in the LayerThread, (the thread that is
     * running the prepare). If this Layer needs to do any cleanups during the
     * abort, it should do so, but return out of the prepare asap.
     * 
     */
    public synchronized OMGraphicList prepare() {

        if (isCancelled()) {
            Debug.message("dted", getName() + "|DTEDLayer.prepare(): aborted.");
            return null;
        }

        Projection projection = getProjection();

        if (projection == null) {
            Debug.error("DTED Layer needs to be added to the MapBean before it can draw images!");
            return new OMGraphicList();
        }

        DTEDCacheManager cache = getCache();

        if (!(projection instanceof EqualArc)) {
            //fireRequestInfoLine("DTED works faster with an Equal Arc projection (CADRG/LLXY).");
        }

        Debug.message("basic", getName() + "|DTEDLayer.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // OMGraphicList is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        // call getRectangle();
        if (Debug.debugging("dted")) {
            Debug.output(getName() + "|DTEDLayer.prepare(): " + "calling getRectangle " + " with projection: " + projection
                    + " ul = " + projection.getUpperLeft() + " lr = " + projection.getLowerRight());
        }

        OMGraphicList omGraphicList;

        if (projection.getScale() < maxScale) {
            omGraphicList = cache.getRectangle(projection);
        } else {
            fireRequestInfoLine("  The scale is too small for DTED viewing.");
            Debug.error("DTEDLayer: scale (1:" + projection.getScale() + ") is smaller than minimum (1:" + maxScale + ") allowed.");
            omGraphicList = new OMGraphicList();
        }
        // ///////////////////
        // safe quit
        int size = 0;
        if (omGraphicList != null) {
            size = omGraphicList.size();
            Debug.message("basic", getName() + "|DTEDLayer.prepare(): finished with " + size + " graphics");
            
            // // Don't forget to project them. Since they are only
            // // being recalled if the projection has changed, then we
            // // need to force a reprojection of all of them because the
            // // screen position has changed.
            // omGraphicList.project(projection, true);

        } else {
            Debug.message("basic", getName() + "|DTEDLayer.prepare(): finished with null graphics list");
        }

        return omGraphicList;
    }

    /**
     * Paints the layer.
     * 
     * @param g the Graphics context for painting
     */
    public void paint(java.awt.Graphics g) {
        Debug.message("dted", getName() + "|DTEDLayer.paint()");

        super.paint(g);

        if (location != null)
            location.render(g);
        location = null;
    }

    /**
     * Get the view type set for creating images.
     * <P>
     * 
     * <pre>
     * 
     * 
     * 
     * 
     *     0: DTEDFrameSubframe.NOSHADING
     *     1: DTEDFrameSubframe.SLOPESHADING
     *     2: DTEDFrameSubframe.COLOREDSHADING
     *     3: DTEDFrameSubframe.METERSHADING
     *     4: DTEDFrameSubframe.FEETSHADING
     * 
     * 
     * 
     * 
     * </pre>
     */
    public int getViewType() {
        return viewType;
    }

    public void setViewType(int vt) {
        switch (vt) {
            case DTEDFrameSubframe.NOSHADING:
            case DTEDFrameSubframe.SLOPESHADING:
            case DTEDFrameSubframe.COLOREDSHADING:
            case DTEDFrameSubframe.METERSHADING:
            case DTEDFrameSubframe.FEETSHADING:
                viewType = vt;
                if (cache != null) {
                    DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
                    dfsi.viewType = viewType;
                }

                break;
            default:
                // unchanged
        }
    }

    /**
     * Get the value for the interval between band colors for meter and feet
     * shading view types.
     */
    public int getBandHeight() {
        return bandHeight;
    }

    public void setBandHeight(int bh) {
        bandHeight = bh;
        if (cache != null) {
            DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
            dfsi.bandHeight = bandHeight;
        }
    }

    /**
     * Get the value for contrast adjustments, 1-5.
     */
    public int getSlopeAdjust() {
        return slopeAdjust;
    }

    public void setSlopeAdjust(int sa) {
        if (sa > 0 && sa <= 5) {
            slopeAdjust = sa;
            if (cache != null) {
                DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
                dfsi.slopeAdjust = slopeAdjust;
            }
        } else {
            Debug.output("DTEDLayer (" + getName() + ") being told to set slope adjustment to invalid value (" + sa
                    + "), must be 1-5");
        }
    }

    /**
     * Get the value set for which DTED level is being used, 0-2.
     */
    public int getDtedLevel() {
        return dtedLevel;
    }

    public void setDtedLevel(int level) {
        dtedLevel = level;
    }

    /**
     * Get the opaqueness value used for the images, 0-255.
     */
    public int getOpaqueness() {
        return opaqueness;
    }

    public void setOpaqueness(int o) {
        if (o >= 0) {
            opaqueness = o;
            if (cache != null) {
                cache.setOpaqueness(opaqueness);
            }
        }
    }

    /**
     * Get whether the cache will be killed when the layer is removed from the
     * map.
     */
    public boolean getKillCache() {
        return killCache;
    }

    public void setKillCache(boolean kc) {
        killCache = kc;
    }

    /**
     * Get the cache size, or how many DTED frames are held in memory.
     */
    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cs) {
        if (cs > 0) {
            cacheSize = cs;
        } else {
            Debug.output("DTEDLayer (" + getName() + ") being told to set cache size to invalid value (" + cs + ").");
        }
    }

    /**
     * Get the number of colors used in the DTED images.
     */
    public int getNumColors() {
        return numColors;
    }

    public void setNumColors(int nc) {
        if (nc > 0) {
            numColors = nc;

            if (cache != null) {
                cache.setNumColors(numColors);
            }
        }
    }

    // ----------------------------------------------------------------------
    // GUI
    // ----------------------------------------------------------------------

    /** The user interface palette for the DTED layer. */
    protected Box paletteBox = null;

    /** Creates the interface palette. */
    public java.awt.Component getGUI() {

        if (paletteBox == null) {
            if (Debug.debugging("dted"))
                Debug.output("DTEDLayer: creating DTED Palette.");

            paletteBox = Box.createVerticalBox();
            Box subbox1 = Box.createHorizontalBox();
            Box subbox2 = Box.createVerticalBox();
            Box subbox3 = Box.createHorizontalBox();

            // palette = new JPanel();
            // palette.setLayout(new GridLayout(0, 1));

            // The DTED Level selector
            JPanel levelPanel = PaletteHelper.createPaletteJPanel("DTED Level");
            ButtonGroup levels = new ButtonGroup();

            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (cache != null) {
                        String ac = e.getActionCommand();
                        int newLevel;
                        if (ac.equalsIgnoreCase(level2Command))
                            newLevel = DTEDFrameSubframe.LEVEL_2;
                        else if (ac.equalsIgnoreCase(level1Command))
                            newLevel = DTEDFrameSubframe.LEVEL_1;
                        else
                            newLevel = DTEDFrameSubframe.LEVEL_0;
                        DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
                        dfsi.dtedLevel = newLevel;
                        // cache.setSubframeInfo(dfsi);
                    }
                }
            };

            JRadioButton level0 = new JRadioButton("Level 0");
            level0.addActionListener(al);
            level0.setActionCommand(level0Command);
            JRadioButton level1 = new JRadioButton("Level 1");
            level1.addActionListener(al);
            level1.setActionCommand(level1Command);
            JRadioButton level2 = new JRadioButton("Level 2");
            level2.addActionListener(al);
            level2.setActionCommand(level2Command);

            levels.add(level0);
            levels.add(level1);
            levels.add(level2);

            switch (dtedLevel) {
                case 2:
                    level2.setSelected(true);
                    break;
                case 1:
                    level1.setSelected(true);
                    break;
                case 0:
                default:
                    level0.setSelected(true);
            }

            levelPanel.add(level0);
            levelPanel.add(level1);
            levelPanel.add(level2);

            // The DTED view selector
            JPanel viewPanel = PaletteHelper.createPaletteJPanel("View Type");
            String[] viewStrings = {
                "None",
                "Shading",
                "Elevation Shading",
                "Elevation Bands (Meters)",
                "Elevation Bands (Feet)"
            };

            JComboBox viewList = new JComboBox(viewStrings);
            viewList.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox jcb = (JComboBox) e.getSource();
                    int newView = jcb.getSelectedIndex();
                    switch (newView) {
                        case 0:
                            viewType = DTEDFrameSubframe.NOSHADING;
                            break;
                        case 1:
                            viewType = DTEDFrameSubframe.SLOPESHADING;
                            break;
                        case 2:
                            viewType = DTEDFrameSubframe.COLOREDSHADING;
                            break;
                        case 3:
                            viewType = DTEDFrameSubframe.METERSHADING;
                            break;
                        case 4:
                            viewType = DTEDFrameSubframe.FEETSHADING;
                            break;
                        default:
                            viewType = DTEDFrameSubframe.NOSHADING;
                    }
                    if (cache != null) {
                        DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
                        dfsi.viewType = viewType;
                        // cache.setSubframeInfo(dfsi);
                    }

                }
            });
            int selectedView;
            switch (viewType) {
                case 0:
                case 1:
                    selectedView = viewType;
                    break;
                case 2:
                case 3:
                    selectedView = viewType + 1;
                    break;
                case 4:
                    // This puts the layer in testing mode, and the menu
                    // changes.
                    String[] viewStrings2 = {
                        "None",
                        "Shading",
                        "Elevation Bands (Meters)",
                        "Elevation Bands (Feet)",
                        "Subframe Testing",
                        "Elevation Shading"
                    };
                    viewList = new JComboBox(viewStrings2);
                    viewList.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JComboBox jcb = (JComboBox) e.getSource();
                            int newView = jcb.getSelectedIndex();
                            if (cache != null) {
                                DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
                                dfsi.viewType = newView;
                                // cache.setSubframeInfo(dfsi);
                            }
                        }
                    });
                    selectedView = viewType;
                    break;
                case 5:
                    selectedView = 2; // DTEDFrameSubframe.COLOREDSHADING
                    break;
                default:
                    selectedView = DTEDFrameSubframe.NOSHADING;
            }

            viewList.setSelectedIndex(selectedView);
            viewPanel.add(viewList);

            // The DTED Contrast Adjuster
            JPanel contrastPanel = PaletteHelper.createPaletteJPanel("Contrast Adjustment");
            JSlider contrastSlide = new JSlider(JSlider.HORIZONTAL, 1/* min */, 5/* max */, slopeAdjust/* initial */);
            java.util.Hashtable<Integer, JLabel> dict = new java.util.Hashtable<Integer, JLabel>();
            dict.put(new Integer(1), new JLabel("min"));
            dict.put(new Integer(5), new JLabel("max"));
            contrastSlide.setLabelTable(dict);
            contrastSlide.setPaintLabels(true);
            contrastSlide.setMajorTickSpacing(1);
            contrastSlide.setPaintTicks(true);
            contrastSlide.setSnapToTicks(true);
            contrastSlide.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ce) {
                    JSlider slider = (JSlider) ce.getSource();
                    if (slider.getValueIsAdjusting()) {
                        fireRequestInfoLine(getName() + " - Contrast Slider value = " + slider.getValue());
                        slopeAdjust = slider.getValue();
                        if (cache != null) {
                            DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
                            dfsi.slopeAdjust = slopeAdjust;
                            // cache.setSubframeInfo(dfsi);
                        }
                    }
                }
            });
            contrastPanel.add(contrastSlide);

            // The DTED Band Height Adjuster
            JPanel bandPanel = PaletteHelper.createPaletteJPanel("Band Elevation Spacing");
            JSlider bandSlide = new JSlider(JSlider.HORIZONTAL, 0/* min */, 1000/* max */, bandHeight/* initial */);
            bandSlide.setLabelTable(bandSlide.createStandardLabels(250));
            bandSlide.setPaintLabels(true);
            bandSlide.setMajorTickSpacing(250);
            bandSlide.setMinorTickSpacing(50);
            bandSlide.setPaintTicks(true);
            bandSlide.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ce) {
                    JSlider slider = (JSlider) ce.getSource();
                    if (slider.getValueIsAdjusting()) {
                        fireRequestInfoLine(getName() + " - Band Slider value = " + slider.getValue());
                        bandHeight = slider.getValue();
                        if (cache != null) {
                            DTEDFrameSubframeInfo dfsi = cache.getSubframeInfo();
                            dfsi.bandHeight = bandHeight;
                            // cache.setSubframeInfo(dfsi);
                        }
                    }
                }
            });

            bandPanel.add(bandSlide);

            JButton redraw = new JButton("Redraw DTED Layer");
            redraw.setActionCommand(RedrawCmd);
            redraw.addActionListener(this);

            subbox1.add(levelPanel);
            subbox1.add(viewPanel);
            paletteBox.add(subbox1);
            subbox2.add(contrastPanel);
            subbox2.add(bandPanel);
            paletteBox.add(subbox2);
            subbox3.add(redraw);
            paletteBox.add(subbox3);
        }

        return paletteBox;
    }

    /**
     * Overridden to modify the MapMouseInterpreter used by the layer.
     */
    public synchronized MapMouseInterpreter getMouseEventInterpreter() {
        if (getMouseModeIDsForEvents() != null && mouseEventInterpreter == null) {
            setMouseEventInterpreter(new StandardMapMouseInterpreter(this) {
                public boolean leftClick(MouseEvent me) {
                    super.leftClick(me);
                    determineLocation(me);
                    return true;
                }

                public boolean leftClick(OMGraphic omg, MouseEvent me) {
                    super.leftClick(omg, me);
                    determineLocation(me);
                    return true;
                }

            });
        }

        return mouseEventInterpreter;
    }

    public boolean determineLocation(MouseEvent e) {
        Projection projection = getProjection();
        if (cache != null && projection != null) {
            LatLonPoint ll = projection.inverse(e.getX(), e.getY(), new LatLonPoint.Double());
            location = new DTEDLocation(e.getX(), e.getY());
            location.setElevation(cache.getElevation((float) ll.getY(), (float) ll.getX()));
            location.generate(projection);
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Don't need DTEDFrames highlighting themselves.
     */
    public boolean isHighlightable(OMGraphic omg) {
        return false;
    }

}