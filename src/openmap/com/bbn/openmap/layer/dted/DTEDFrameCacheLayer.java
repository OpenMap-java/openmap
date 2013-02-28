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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrameCacheLayer.java,v $
// $RCSfile: DTEDFrameCacheLayer.java,v $
// $Revision: 1.10 $
// $Date: 2005/12/09 21:09:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

/*  Java Core  */
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.bbn.openmap.dataAccess.dted.DTEDConstants;
import com.bbn.openmap.dataAccess.dted.DTEDDirectoryHandler;
import com.bbn.openmap.dataAccess.dted.DTEDFrameCacheHandler;
import com.bbn.openmap.dataAccess.dted.DTEDNameTranslator;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.event.MapMouseInterpreter;
import com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter;
import com.bbn.openmap.omGraphics.grid.GeneratorLoader;
import com.bbn.openmap.omGraphics.grid.SlopeGeneratorLoader;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * The DTEDFrameCacheLayer fills the screen with DTED data. To view the DTED
 * iamges, the projection has to be set in an ARC projection, which OpenMap
 * calls the CADRG or LLXY projection. In Gesture mode, clicking on the map will
 * cause the DTEDFrameCacheLayer to place a point on the window and show the
 * elevation of that point. The Gesture response is not dependent on the scale
 * or projection of the screen.
 * <P>
 * 
 * The DTEDFrameCacheLayer uses the DTEDCacheHandler to get the images it needs.
 * The DTEDFrameCacheLayer receives projection change events, and then asks the
 * cache handler for the images it needs based on the new projection.
 * 
 * The DTEDFrameCacheLayer also relies on properties to set its variables, such
 * as the dted frame paths (there can be several at a time), the opaqueness of
 * the frame images, number of colors to use, and some other display variables.
 * The DTEDFrameCacheLayer properties look something like this:
 * <P>
 * 
 * NOTE: Make sure your DTED file and directory names are in lower case. You can
 * use the com.bbn.openmap.layer.rpf.ChangeCase class to make modifications if
 * necessary.
 * <P>
 * 
 * <pre>
 * 
 * 
 *    #------------------------------
 *    # Properties for DTEDFrameCacheLayer
 *    #------------------------------
 *    
 *    # Level of DTED data to use 0, 1, 2
 *    dted.level=0
 *    
 *    # height (meters or feet) between color changes in band shading
 *    dted.band.height=25
 *    
 *    # Minumum scale to display images. Larger numbers mean smaller scale, 
 *    # and are more zoomed out.
 *    dted.min.scale=20000000
 *    
 *    # Delete the cache if the layer is removed from the map.
 *    dted.kill.cache=true
 *   
 *    # Need to set GeneratorLoaders for DTED rendering.  These properties get
 *    # forwarded on to the DTEDFrameCacheHandler.
 *    dted.generators=greys colors
 *    dted.greys.class=com.bbn.openmap.omGraphics.grid.SlopeGeneratorLoader
 *    dted.greys.prettyName=Slope Shading
 *    dted.greys.colorsClass=com.bbn.openmap.omGraphics.grid.GreyscaleSlopeColors
 *    dted.colors.class=com.bbn.openmap.omGraphics.grid.SlopeGeneratorLoader
 *    dted.colors.prettyName=Elevation Shading
 *    dted.colors.colorsClass=com.bbn.openmap.omGraphics.grid.ColoredShadingColors
 *   
 *    #-------------------------------------
 *    # End of properties for DTEDFrameCacheLayer
 *    #-------------------------------------
 * 
 * 
 * </pre>
 * 
 * @see com.bbn.openmap.layer.rpf.ChangeCase
 */
public class DTEDFrameCacheLayer extends OMGraphicHandlerLayer implements ActionListener,
        Serializable, DTEDConstants {

    /** The cache handler. */
    protected transient DTEDFrameCacheHandler cache = new DTEDFrameCacheHandler(null);
    protected long minScale = 20000000;
    /** Flag to delete the cache if the layer is removed from the map. */
    protected boolean killCache = true;

    public static final String DTEDLevelProperty = "level";
    public static final String DTEDMinScaleProperty = "min.scale";
    public static final String DTEDKillCacheProperty = "kill.cache";

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
    public DTEDFrameCacheLayer() {
        setMouseModeIDsForEvents(new String[] { "Gestures" });
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     * 
     * @param dfc paths to the DTED directories that hold level 0 and 1 data.
     */
    public DTEDFrameCacheLayer(com.bbn.openmap.dataAccess.dted.DTEDFrameCache dfc) {
        this();
        setFrameCache(dfc);
    }

    public void setFrameCache(com.bbn.openmap.dataAccess.dted.DTEDFrameCache dfc) {
        cache.setFrameCache(dfc);
        cache.resetCache();
    }

    public com.bbn.openmap.dataAccess.dted.DTEDFrameCache getFrameCache() {
        if (cache != null) {
            return cache.getFrameCache();
        } else
            return null;
    }

    public DTEDFrameCacheHandler getCache() {
        return cache;
    }

    public void setCache(DTEDFrameCacheHandler cache) {
        this.cache = cache;
    }

    protected void setDefaultValues() {
        // defaults
        setMaxScale(20000000);
    }

    /**
     * Set all the DTED properties from a properties object.
     */
    public void setProperties(java.util.Properties properties) {
        setProperties(null, properties);
    }

    /**
     * Set all the DTED properties from a properties object.
     */
    public void setProperties(String prefix, java.util.Properties properties) {

        super.setProperties(prefix, properties);
        prefix = PropUtils.getScopedPropertyPrefix(this);

        setDtedLevel(PropUtils.intFromProperties(properties, prefix + DTEDLevelProperty, getDtedLevel()));
        cache.setProperties(prefix, properties);

    }

    /**
     * Called when the layer is no longer part of the map.
     */
    public void removed(java.awt.Container cont) {
        OMGraphicList rasters = getList();
        if (rasters != null) {
            rasters.clear();
        }
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof com.bbn.openmap.dataAccess.dted.DTEDFrameCache) {
            setFrameCache((com.bbn.openmap.dataAccess.dted.DTEDFrameCache) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj == getFrameCache()) {
            setFrameCache(null);
        }
    }

    /**
     * A flag to keep track of when the first time a warning was put up if the
     * projection isn't EquiArc.
     */
    protected boolean firstProjectionWarningSent = false;

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
            Debug.message("dted", getName() + "|DTEDFrameCacheLayer.prepare(): aborted.");
            return null;
        }

        if (cache == null) {
            Debug.message("dted", getName()
                    + "|DTEDFrameCacheLayer can't add anything to map because the DTEDFrameCache has not been set.");
        }

        Projection projection = getProjection();

        if (projection == null) {
            Debug.output("DTED Layer needs to be added to the MapBean before it can draw images!");
            return new OMGraphicList();
        }

        // Check to make sure the projection is EqualArc
        if (!(projection instanceof EqualArc)) {
            if (!firstProjectionWarningSent) {
                fireRequestInfoLine("  DTED requires an Equal Arc projection (CADRG/LLXY) to view images.");
                Debug.output("DTEDFrameCacheLayer: DTED requires an Equal Arc projection (CADRG/LLXY) to view images.");
                firstProjectionWarningSent = true;
            }
            return new OMGraphicList();
        }

        Debug.message("basic", getName() + "|DTEDFrameCacheLayer.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // OMGraphicList is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        // call getRectangle();
        if (Debug.debugging("dted")) {
            Debug.output(getName() + "|DTEDFrameCacheLayer.prepare(): " + "calling getRectangle "
                    + " with projection: " + projection + " ul = " + projection.getUpperLeft()
                    + " lr = " + projection.getLowerRight());
        }

        OMGraphicList omGraphicList;

        if (projection.getScale() < maxScale) {
            omGraphicList = cache.getRectangle((EqualArc) projection);
        } else {
            fireRequestInfoLine("  The scale is too small for DTED viewing.");
            Debug.error("DTEDFrameCacheLayer: scale (1:" + projection.getScale()
                    + ") is smaller than minimum (1:" + maxScale + ") allowed.");
            omGraphicList = new OMGraphicList();
        }
        // ///////////////////
        // safe quit
        int size = 0;
        if (omGraphicList != null) {
            size = omGraphicList.size();
            Debug.message("basic", getName() + "|DTEDFrameCacheLayer.prepare(): finished with "
                    + size + " graphics");

            // Don't forget to project them. Since they are only
            // being recalled if the projection has changed, then we
            // need to force a reprojection of all of them because the
            // screen position has changed.
            omGraphicList.project(projection, true);

        } else {
            Debug.message("basic", getName()
                    + "|DTEDFrameCacheLayer.prepare(): finished with null graphics list");
        }

        return omGraphicList;
    }

    /**
     * Paints the layer.
     * 
     * @param g the Graphics context for painting
     */
    public void paint(java.awt.Graphics g) {
        super.paint(g);

        if (location != null)
            location.render(g);
        location = null;
    }

    /**
     * Get the value set for which DTED level is being used, 0-2.
     */
    public int getDtedLevel() {
        if (cache != null) {
            return cache.getDtedLevel();
        } else
            return LEVEL_0;
    }

    public void setDtedLevel(int level) {
        if (cache != null) {
            cache.setDtedLevel(level);
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

    // ----------------------------------------------------------------------
    // GUI
    // ----------------------------------------------------------------------

    /** The user interface palette for the DTED layer. */
    protected Box paletteBox = null;

    /** Creates the interface palette. */
    public Component getGUI() {

        if (paletteBox == null) {
            if (Debug.debugging("dted"))
                Debug.output("DTEDFrameCacheLayer: creating DTED Palette.");

            paletteBox = Box.createVerticalBox();
            Box subbox1 = Box.createHorizontalBox();
            Box subbox3 = Box.createHorizontalBox();

            // The DTED Level selector
            JPanel levelPanel = PaletteHelper.createPaletteJPanel("DTED Level");
            ButtonGroup levels = new ButtonGroup();

            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (cache != null) {
                        String ac = e.getActionCommand();
                        int newLevel;
                        if (ac.equalsIgnoreCase(level2Command))
                            newLevel = LEVEL_2;
                        else if (ac.equalsIgnoreCase(level1Command))
                            newLevel = LEVEL_1;
                        else
                            newLevel = LEVEL_0;
                        setDtedLevel(newLevel);
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

            switch (getDtedLevel()) {
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

            // The DTED view selector from DTEDFrameCacheHandler
            JPanel viewPanel = PaletteHelper.createPaletteJPanel("View Type");
            viewPanel.add(cache.getGUI());

            JButton redraw = new JButton("Redraw DTED Layer");
            redraw.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    doPrepare();
                }
            });

            subbox1.add(levelPanel);
            subbox1.add(viewPanel);
            paletteBox.add(subbox1);
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

    /**
     * This is the easiest way to construct a DTEDFrameCacheLayer programmatically.
     * Create the Builder, configure it, and call create() to configure the layer.
     */

    public static class Builder {
        List<DTEDDirectoryHandler> dirHandlers;
        List<GeneratorLoader> loaders = new ArrayList<GeneratorLoader>();
        DTEDNameTranslator nTranslator;

        /**
         * Create a builder for a DTEDFrameCacheLayer.
         * @param dtedDirectory a path to the dted directory.
         */
        public Builder(String dtedDirectory) {
            this(new DTEDDirectoryHandler(dtedDirectory));
        }
        
        /**
         * Create a builder for a DTEDFrameCacheLayer.
         * 
         * @param dirHandler don't pass in a null value, things will get ugly.
         */
        public Builder(DTEDDirectoryHandler dirHandler) {
            dirHandlers = new ArrayList<DTEDDirectoryHandler>();
            dirHandlers.add(dirHandler);
        }

        /**
         * If set, this name translator will be added to all directory handlers
         * set in this builder. If not called the StandardDTEDNameTranslator
         * will be used.
         * 
         * @param translator DTEDNameTranslator.
         * @return this builder.
         */
        public Builder setNameTranslator(DTEDNameTranslator translator) {
            nTranslator = translator;
            return this;
        }

        /**
         * Add a generator loader to the DTEDFrameCache to be used by the layer.
         * If not called, the SloperGeneratorLoader will be used.
         * 
         * @param gLoader
         * @return this Builder.
         */
        public Builder addGeneratorLoader(GeneratorLoader gLoader) {
            if (loaders == null) {
                loaders = new ArrayList<GeneratorLoader>();
            }
            if (gLoader != null) {
                loaders.add(gLoader);
            }
            return this;
        }

        /**
         * Create the DTEDFrameCacheLayer.
         * @return the new layer, configured with Builder settings.
         */
        public DTEDFrameCacheLayer create() {
            com.bbn.openmap.dataAccess.dted.DTEDFrameCache dfc = new com.bbn.openmap.dataAccess.dted.DTEDFrameCache();
            for (DTEDDirectoryHandler dHandler : dirHandlers) {
                if (nTranslator != null) {
                    dHandler.setTranslator(nTranslator);
                }
                dfc.addDTEDDirectoryHandler(dHandler);
            }

            DTEDFrameCacheLayer layer = new DTEDFrameCacheLayer(dfc);
            com.bbn.openmap.dataAccess.dted.DTEDFrameCacheHandler dfcHandler = layer.getCache();
            
            if (!loaders.isEmpty()) {
                dfcHandler.setGeneratorLoaders(loaders);
            } else {
                dfcHandler.addGeneratorLoader(new SlopeGeneratorLoader());
            }
            
            dfcHandler.setActiveGeneratorLoader(dfcHandler.getGeneratorLoaders().get(0).getPrettyName());

            return layer;
        }
    }

}