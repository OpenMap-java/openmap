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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfCoverage.java,v $
// $RCSfile: RpfCoverage.java,v $
// $Revision: 1.9 $
// $Date: 2005/12/09 21:09:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

/*  Java Core  */
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * This is a tool that provides coverage information on the Rpf data.
 * It is supposed to be a simple tool that lets you see the general
 * location of data, to guide you to the right place and scale of
 * coverage. The layer really uses the properties passed in to it to
 * determine which RPF/A.TOC should be scanned for the data. There is
 * a palette for this layer, that lets you turn off the coverage for
 * different levels of Rpf. Right now, only City Graphics, TLM, JOG,
 * TPC, ONC, JNC, GNC and 5/10 meter CIB scales are are handled. All
 * other scales are tossed together under the misc setting. The City
 * Graphics setting shows all charts for scales greater than than
 * 1:15k.
 * <P>
 * 
 * <pre>
 * 
 *  
 *   
 *    
 *     
 *      
 *       The properties for this file are:
 *        # Java Rpf properties
 *        # Number between 0-255: 0 is transparent, 255 is opaque
 *        jrpf.coverageOpaque=255
 *        #Default is true, don't need this entry if you like it...
 *        jrpf.CG.showcov=true
 *        #Default colors don't need this entry
 *        jrpf.CG.color=CE4F3F
 *        # Other types can be substituted for CG (TLM, JOG, TPC, ONC, JNC, GNC, CIB10, CIB5, MISC)
 *        # Fill the rectangle, default is true
 *        jrpf.coverageFill=true
 *       
 *       
 *      
 *     
 *    
 *   
 *  
 * </pre>
 */
public class RpfCoverage implements ActionListener, RpfConstants,
        PropertyConsumer {

    /** The graphic list of objects to draw. */
    protected OMGraphicList omGraphics;

    /**
     * Set when the projection has changed while a swing worker is
     * gathering graphics, and we want him to stop early.
     */
    protected boolean cancelled = false;
    protected RpfCoverageManager coverageManager = null;

    protected String propertyPrefix = null;

    /**
     * Flag to tell the cache to return the coverage for city
     * graphics.
     */
    protected boolean showCG = true;
    /** Flag to tell the cache to return the coverage for tlm. */
    protected boolean showTLM = true;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showJOG = true;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showTPC = true;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showONC = true;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showJNC = true;
    /** Flag to tell the cache to return the coverage for jog. */
    protected boolean showGNC = true;
    /** Flag to tell the cache to return the coverage for 10M CIB. */
    protected boolean showCIB10 = true;
    /** Flag to tell the cache to return the coverage for 5M CIB. */
    protected boolean showCIB5 = true;
    /** Flag to tell the cache to return the coverage for others. */
    protected boolean showMISC = true;

    /** The default color int value. */
    public final static int defaultCGColorInt = 0xAC4853;
    /** The default color int value. */
    public final static int defaultTLMColorInt = 0xCE4F3F;
    /** The default color int value. */
    public final static int defaultJOGColorInt = 0xAC7D74;
    /** The default color int value. */
    public final static int defaultTPCColorInt = 0xACCD10;
    /** The default color int value. */
    public final static int defaultONCColorInt = 0xFCCDE5;
    /** The default color int value. */
    public final static int defaultJNCColorInt = 0x7386E5;
    /** The default color int value. */
    public final static int defaultGNCColorInt = 0x55866B;
    /** The default color int value. */
    public final static int defaultCIB10ColorInt = 0x07516B;
    /** The default color int value. */
    public final static int defaultCIB5ColorInt = 0x071CE0;
    /** The default color int value. */
    public final static int defaultMISCColorInt = 0xF2C921;

    /** The color to outline the shapes. */
    protected Color CGColor = new Color(defaultCGColorInt);
    /** The color to outline the shapes. */
    protected Color TLMColor = new Color(defaultTLMColorInt);
    /** The color to outline the shapes. */
    protected Color JOGColor = new Color(defaultJOGColorInt);
    /** The color to outline the shapes. */
    protected Color TPCColor = new Color(defaultTPCColorInt);
    /** The color to outline the shapes. */
    protected Color ONCColor = new Color(defaultONCColorInt);
    /** The color to outline the shapes. */
    protected Color JNCColor = new Color(defaultJNCColorInt);
    /** The color to outline the shapes. */
    protected Color GNCColor = new Color(defaultGNCColorInt);
    /** The color to outline the shapes. */
    protected Color CIB10Color = new Color(defaultCIB10ColorInt);
    /** The color to outline the shapes. */
    protected Color CIB5Color = new Color(defaultCIB5ColorInt);
    /** The color to outline the shapes. */
    protected Color MISCColor = new Color(defaultMISCColorInt);

    /**
     * A setting for how transparent to make the images. The default
     * is 255, which is totally opaque. Not used right now.
     */
    protected int opaqueness = RpfColortable.DEFAULT_OPAQUENESS;
    /** Flag to fill the coverage rectangles. */
    protected boolean fillRects;

    /** Property to use for filled rectangles (when java supports it). */
    public static final String CoverageOpaquenessProperty = "coverageOpaque";
    /** Property to use to fill rectangles. */
    public static final String FillProperty = "coverageFill";

    /** The parent layer. */
    protected Layer layer;
    /** Flag to track when the RpfCoverage is active. */
    protected boolean inUse = false;
    /**
     * Show the palette when showing coverage. Probably not needed for
     * layers limiting chart seriestypes for display.
     */
    protected boolean showPalette = true;

    protected I18n i18n = Environment.getI18n();

    /**
     * The default constructor for the Layer. All of the attributes
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
     * @param prefix string prefix used in the properties file for
     *        this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        setPropertyPrefix(prefix);

        setDefaultValues();

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        fillRects = PropUtils.booleanFromProperties(properties, prefix
                + FillProperty, fillRects);

        showPalette = PropUtils.booleanFromProperties(properties, prefix
                + CoverageProperty, showPalette);

        opaqueness = PropUtils.intFromProperties(properties, prefix
                + CoverageOpaquenessProperty, opaqueness);

        CGColor = (Color) PropUtils.parseColorFromProperties(properties, prefix
                + CGColorProperty, CGColor);
        TLMColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + TLMColorProperty,
                TLMColor);
        JOGColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + JOGColorProperty,
                JOGColor);
        TPCColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + TPCColorProperty,
                TPCColor);
        ONCColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + ONCColorProperty,
                ONCColor);
        JNCColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + JNCColorProperty,
                JNCColor);
        GNCColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + GNCColorProperty,
                GNCColor);
        CIB10Color = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + CIB10ColorProperty,
                CIB10Color);
        CIB5Color = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + CIB5ColorProperty,
                CIB5Color);
        MISCColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + MISCColorProperty,
                MISCColor);

        // If the palette is turned off, then we all of them have been
        // set to true. Only the coverage of the limited series will
        // be asked for.
        if (showPalette) {
            showCG = PropUtils.booleanFromProperties(properties, prefix
                    + ShowCGProperty, showCG);
            showTLM = PropUtils.booleanFromProperties(properties, prefix
                    + ShowTLMProperty, showTLM);
            showJOG = PropUtils.booleanFromProperties(properties, prefix
                    + ShowJOGProperty, showJOG);
            showTPC = PropUtils.booleanFromProperties(properties, prefix
                    + ShowTPCProperty, showTPC);
            showONC = PropUtils.booleanFromProperties(properties, prefix
                    + ShowONCProperty, showONC);
            showJNC = PropUtils.booleanFromProperties(properties, prefix
                    + ShowJNCProperty, showJNC);
            showGNC = PropUtils.booleanFromProperties(properties, prefix
                    + ShowGNCProperty, showGNC);
            showCIB10 = PropUtils.booleanFromProperties(properties, prefix
                    + ShowCIB10Property, showCIB10);
            showCIB5 = PropUtils.booleanFromProperties(properties, prefix
                    + ShowCIB5Property, showCIB5);
            showMISC = PropUtils.booleanFromProperties(properties, prefix
                    + ShowMISCProperty, showMISC);
        }
    }

    /**
     * PropertyConsumer method, to fill in a Properties object,
     * reflecting the current values of the layer. If the layer has a
     * propertyPrefix set, the property keys should have that prefix
     * plus a separating '.' prepended to each property key it uses for
     * configuration.
     * 
     * @param props a Properties object to load the PropertyConsumer
     *        properties into. If props equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(propertyPrefix);

        props.put(prefix + FillProperty, new Boolean(fillRects).toString());
        props.put(prefix + CoverageProperty,
                new Boolean(showPalette).toString());
        props.put(prefix + CoverageOpaquenessProperty,
                Integer.toString(opaqueness));
        props.put(prefix + CGColorProperty,
                Integer.toHexString(CGColor.getRGB()));
        props.put(prefix + TLMColorProperty,
                Integer.toHexString(TLMColor.getRGB()));
        props.put(prefix + JOGColorProperty,
                Integer.toHexString(JOGColor.getRGB()));
        props.put(prefix + TPCColorProperty,
                Integer.toHexString(TPCColor.getRGB()));
        props.put(prefix + ONCColorProperty,
                Integer.toHexString(ONCColor.getRGB()));
        props.put(prefix + JNCColorProperty,
                Integer.toHexString(JNCColor.getRGB()));
        props.put(prefix + GNCColorProperty,
                Integer.toHexString(GNCColor.getRGB()));
        props.put(prefix + CIB10ColorProperty,
                Integer.toHexString(CIB10Color.getRGB()));
        props.put(prefix + CIB5ColorProperty,
                Integer.toHexString(CIB5Color.getRGB()));
        props.put(prefix + MISCColorProperty,
                Integer.toHexString(MISCColor.getRGB()));

        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer. The key
     * for each property should be the raw property name (without a
     * prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.). For Layer, this method should at least return the
     * 'prettyName' property.
     * 
     * @param list a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }
        String interString;
        interString = i18n.get(RpfLayer.class,
                FillProperty,
                I18n.TOOLTIP,
                "Flag to set if the coverage rectangles should be filled.");
        list.put(FillProperty, interString);
        list.put(FillProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                FillProperty,
                "Fill Coverage Rectangles");
        list.put(FillProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                CoverageProperty,
                I18n.TOOLTIP,
                "Flag to set the coverage palette should be shown.");
        list.put(CoverageProperty, interString);
        list.put(CoverageProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                CoverageProperty,
                "Show Coverage Palette");
        list.put(CoverageProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                CoverageOpaquenessProperty,
                I18n.TOOLTIP,
                "Integer representing opaqueness level (0-255, 0 is clear) of coverage rectangles.");
        list.put(CoverageOpaquenessProperty, interString);
        interString = i18n.get(RpfLayer.class,
                CoverageOpaquenessProperty,
                "Coverage Opaqueness");
        list.put(CoverageOpaquenessProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                CGColorProperty,
                I18n.TOOLTIP,
                "Color for City Graphics chart coverage.");
        list.put(CGColorProperty, interString);
        list.put(CGColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                CGColorProperty,
                "CG Coverage Color");
        list.put(CGColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                TLMColorProperty,
                I18n.TOOLTIP,
                "Color for TLM chart coverage.");
        list.put(TLMColorProperty, interString);
        list.put(TLMColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                TLMColorProperty,
                "TLM Coverage Color");
        list.put(TLMColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                JOGColorProperty,
                I18n.TOOLTIP,
                "Color for JOG chart coverage.");
        list.put(JOGColorProperty, interString);
        list.put(JOGColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                JOGColorProperty,
                "JOG Coverage Color");
        list.put(JOGColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                TPCColorProperty,
                I18n.TOOLTIP,
                "Color for TPC chart coverage.");
        list.put(TPCColorProperty, interString);
        list.put(TPCColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                TPCColorProperty,
                "TPC Coverage Color");
        list.put(TPCColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                ONCColorProperty,
                I18n.TOOLTIP,
                "Color for ONC chart coverage.");
        list.put(ONCColorProperty, interString);
        list.put(ONCColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                ONCColorProperty,
                "ONC Coverage Color");
        list.put(ONCColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                JNCColorProperty,
                I18n.TOOLTIP,
                "Color for JNC chart coverage.");
        list.put(JNCColorProperty, interString);
        list.put(JNCColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                JNCColorProperty,
                "JNC Coverage Color");
        list.put(JNCColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                GNCColorProperty,
                I18n.TOOLTIP,
                "Color for GNC chart coverage.");
        list.put(GNCColorProperty, interString);
        list.put(GNCColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                GNCColorProperty,
                "GNC Coverage Color");
        list.put(GNCColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                CIB10ColorProperty,
                I18n.TOOLTIP,
                "Color for CIB 10 meter image coverage.");
        list.put(CIB10ColorProperty, interString);
        list.put(CIB10ColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                CIB10ColorProperty,
                "CIB10 Coverage Color");
        list.put(CIB10ColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                CIB5ColorProperty,
                I18n.TOOLTIP,
                "Color for CIB 5 meter image coverage.");
        list.put(CIB5ColorProperty, interString);
        list.put(CIB5ColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                CIB5ColorProperty,
                "CIB5 Coverage Color");
        list.put(CIB5ColorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class,
                MISCColorProperty,
                I18n.TOOLTIP,
                "Color for all other chart/image coverage.");
        list.put(MISCColorProperty, interString);
        list.put(MISCColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        interString = i18n.get(RpfLayer.class,
                MISCColorProperty,
                "Misc Coverage Color");
        list.put(MISCColorProperty + LabelEditorProperty, interString);

        return list;
    }

    /**
     * Specify what order properties should be presented in an editor.
     */
    public String getInitPropertiesOrder() {
        return " " + FillProperty + " " + CoverageOpaquenessProperty + " "
                + GNCColorProperty + " " + JNCColorProperty + " "
                + ONCColorProperty + " " + TPCColorProperty + " "
                + JOGColorProperty + " " + TLMColorProperty + " "
                + CIB10ColorProperty + " " + CIB5ColorProperty + " "
                + MISCColorProperty;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer. The prefix, along with a '.', should be
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
     * @return the property prefix
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    /**
     * Prepares the graphics for the layer. This is where the
     * getRectangle() method call is made on the rpfcov.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this
     * happens, the map will set the cancel bit in the LayerThread,
     * (the thread that is running the prepare). If this Layer needs
     * to do any cleanups during the abort, it should do so, but
     * return out of the prepare asap.
     */
    public void prepare(RpfFrameProvider frameProvider, Projection projection,
                        String chartSeries) {

        float ullat = 90f;
        float ullon = -180f;
        float lrlat = -90f;
        float lrlon = 180f;

        if (projection != null) {
            ullat = ((LatLonPoint)projection.getUpperLeft()).getLatitude();
            ullon = ((LatLonPoint)projection.getUpperLeft()).getLongitude();
            lrlat = ((LatLonPoint)projection.getLowerRight()).getLatitude();
            lrlon = ((LatLonPoint)projection.getLowerRight()).getLongitude();
        }

        Debug.message("basic", "RpfCoverage.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // OMGraphicList is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        // IF the data arrays have not been set up yet, do it!
        if (coverageManager == null) {
            coverageManager = new RpfCoverageManager(frameProvider);
        }

        setGraphicLists(coverageManager.getCatalogCoverage(ullat,
                ullon,
                lrlat,
                lrlon,
                projection,
                chartSeries,
                getColors(),
                fillRects));
    }

    protected Color[] colors = null;

    protected void resetColors() {
        colors = null;
    }

    protected Color[] getColors() {
        if (colors == null) {
            colors = new Color[] { getModifiedColor(CGColor),
                    getModifiedColor(TLMColor), getModifiedColor(JOGColor),
                    getModifiedColor(TPCColor), getModifiedColor(ONCColor),
                    getModifiedColor(JNCColor), getModifiedColor(GNCColor),
                    getModifiedColor(CIB10Color), getModifiedColor(CIB5Color),
                    getModifiedColor(MISCColor) };
        }
        return colors;
    }

    /**
     * @return Returns the opaqueness.
     */
    public int getOpaqueness() {
        return opaqueness;
    }

    /**
     * @param opaqueness The opaqueness to set.
     */
    public void setOpaqueness(int opaqueness) {
        this.opaqueness = opaqueness;
        resetColors();
    }

    protected Color getModifiedColor(Color color) {
        if (opaqueness < 255) {
            int opa = opaqueness << 24;
            return ColorFactory.createColor(((color.getRGB() & 0x00FFFFFF) | opa),
                    true);
        } else {
            return ColorFactory.createColor(color.getRGB(), true);
        }
    }

    public synchronized void setGraphicLists(OMGraphicList lists) {
        omGraphics = lists;
    }

    public synchronized OMGraphicList getGraphicLists() {
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

        OMGraphicList tmpGraphics = getGraphicLists();

        if (tmpGraphics != null) {
            int length = tmpGraphics.size();
            Debug.message("rpfcov", "RpfCoverage.painting(): " + length
                    + " lists");
            for (int k = length - 1; k >= 0; k--) {
                // HACK - this order is nicely arranged with the order
                // that lists are arranged by the
                // RpfCoverageManager!!!!
                if (k == 0 && showCG)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 1 && showCIB5)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 2 && showTLM)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 3 && showCIB10)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 4 && showJOG)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 5 && showMISC)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 6 && showTPC)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 7 && showONC)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 8 && showJNC)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
                if (k == 9 && showGNC)
                    ((OMGraphicList) tmpGraphics.get(k)).render(g);
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

        OMGraphicList tmpGraphics = getGraphicLists();

        if (tmpGraphics != null) {
           tmpGraphics.generate(proj);
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
     * 
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

            JScrollPane scrollPane = new JScrollPane(p, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

    /**
     * The Action Listener method, that reacts to the palette widgets
     * actions.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        JCheckBox check = (JCheckBox) e.getSource();

        if (cmd == showCGCommand)
            showCG = check.isSelected();
        else if (cmd == showTLMCommand)
            showTLM = check.isSelected();
        else if (cmd == showJOGCommand)
            showJOG = check.isSelected();
        else if (cmd == showTPCCommand)
            showTPC = check.isSelected();
        else if (cmd == showONCCommand)
            showONC = check.isSelected();
        else if (cmd == showJNCCommand)
            showJNC = check.isSelected();
        else if (cmd == showGNCCommand)
            showGNC = check.isSelected();
        else if (cmd == showCIB10Command)
            showCIB10 = check.isSelected();
        else if (cmd == showCIB5Command)
            showCIB5 = check.isSelected();
        else if (cmd == showMISCCommand)
            showMISC = check.isSelected();
        else {
            System.err.println("Unknown action command \"" + cmd
                    + "\" in RpfCoverageLayer.actionPerformed().");
        }
        layer.repaint();
    }
}