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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfViewAttributes.java,v $
// $RCSfile: RpfViewAttributes.java,v $
// $Revision: 1.9 $
// $Date: 2005/08/09 19:25:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

import java.io.Serializable;
import java.util.Properties;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.OptionPropertyEditor;

/**
 * This class contains information to pass through the RpfCacheManager and
 * RpfCacheHandlers to describe limitations and parameters desired for data
 * view. It contains information about the numbers of colors to use, how opaque
 * to make the images, the chart series to retrieve, etc.
 */
public class RpfViewAttributes implements RpfConstants, PropertyConsumer, Serializable {

    public final static String ANY = "ANY";
    public final static String ALL = "ALL";
    public final static String COLORMODEL_DIRECT_STRING = "direct";
    public final static String COLORMODEL_INDEXED_STRING = "indexed";

    protected String propertyPrefix = null;

    /** Number of colors to use - 16, 32, 216 */
    public int numberOfColors;
    /**
     * The opaqueness of the image (transparency) 0-255: 0 is clear, 255 is
     * opaque.
     */
    public int opaqueness;
    /**
     * The image colormodel to use, indexed or colortable, for the OMRasters.
     */
    public int colorModel;
    /**
     * Flag to use to scale images or not. This will cause the caches to scale
     * the images to match the map scale, instead of requiring the map to be at
     * the same scale as the desired image.
     */
    public boolean scaleImages;
    /**
     * The limiting factor for image scaling. Make this too big, and you will
     * run out of memory.
     */
    public float imageScaleFactor;
    /**
     * The data series two-letter code to limit responses to. If you want any
     * data that is the closest match for the current map, use ANY (default). If
     * you want all of the RpfCoverageBoxes that have some coverage, use ALL.
     */
    public String chartSeries;
    /**
     * Flag to set if the CADRG projection is required. Might not be, if we
     * start warping images.
     */
    public boolean requireProjection;
    /** Flag to display images. */
    public boolean showMaps;
    /** Flag to display attribute information about the subframes. */
    public boolean showInfo;

    /**
     * Autofetch the subframe attributes from the FrameProvider. Use only if you
     * are interested in background information about the images.
     */
    public boolean autofetchAttributes;

    protected transient I18n i18n = Environment.getI18n();

    public RpfViewAttributes() {
        setDefaults();
    }

    public void setDefaults() {
        numberOfColors = RpfColortable.CADRG_COLORS;
        opaqueness = RpfColortable.DEFAULT_OPAQUENESS;
        scaleImages = true;
        imageScaleFactor = 4.0f;
        colorModel = OMRasterObject.COLORMODEL_DIRECT;
        chartSeries = ANY;
        requireProjection = true;
        showMaps = true;
        showInfo = false;
        autofetchAttributes = false;
    }

    // ========================================
    // PropertyConsumer interface
    // ========================================

    /**
     * Sets the properties. This particular method assumes that the marker name
     * is not needed, because all of the contents of this Properties object are
     * to be used for this object, and scoping the properties with a prefix is
     * unnecessary.
     * 
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(java.util.Properties props) {
        setProperties(null, props);
    }

    /**
     */
    public void setProperties(String prefix, java.util.Properties props) {
        propertyPrefix = prefix;

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        opaqueness = PropUtils.intFromProperties(props, prefix + OpaquenessProperty, opaqueness);

        numberOfColors = PropUtils.intFromProperties(props, prefix + NumColorsProperty, numberOfColors);

        showMaps = PropUtils.booleanFromProperties(props, prefix + ShowMapsProperty, showMaps);

        showInfo = PropUtils.booleanFromProperties(props, prefix + ShowInfoProperty, showInfo);

        scaleImages = PropUtils.booleanFromProperties(props, prefix + ScaleImagesProperty, scaleImages);

        chartSeries = props.getProperty(prefix + ChartSeriesProperty);

        autofetchAttributes = PropUtils.booleanFromProperties(props, prefix
                + AutoFetchAttributeProperty, autofetchAttributes);

        imageScaleFactor = PropUtils.floatFromProperties(props, prefix + ImageScaleFactorProperty, imageScaleFactor);

        String colormodel = props.getProperty(prefix + ColormodelProperty);
        if (colormodel != null && colormodel.equalsIgnoreCase(COLORMODEL_INDEXED_STRING)) {
            colorModel = OMRasterObject.COLORMODEL_INDEXED;
        } else {
            colorModel = OMRasterObject.COLORMODEL_DIRECT;
        }
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
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(propertyPrefix);

        props.put(prefix + OpaquenessProperty, Integer.toString(opaqueness));
        props.put(prefix + NumColorsProperty, Integer.toString(numberOfColors));
        props.put(prefix + ShowMapsProperty, new Boolean(showMaps).toString());
        props.put(prefix + ShowInfoProperty, new Boolean(showInfo).toString());
        props.put(prefix + ScaleImagesProperty, new Boolean(scaleImages).toString());
        props.put(prefix + ChartSeriesProperty, chartSeries);
        props.put(prefix + AutoFetchAttributeProperty, new Boolean(autofetchAttributes).toString());
        props.put(prefix + ImageScaleFactorProperty, Float.toString(imageScaleFactor));

        if (colorModel == OMRasterObject.COLORMODEL_INDEXED) {
            props.put(prefix + ColormodelProperty, COLORMODEL_INDEXED_STRING);
        } else {
            props.put(prefix + ColormodelProperty, COLORMODEL_DIRECT_STRING);
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
        if (list == null) {
            list = new Properties();
        }
        String interString;

        interString = i18n.get(RpfLayer.class, OpaquenessProperty, I18n.TOOLTIP, "Integer representing opaqueness level (0-255, 0 is clear).");
        list.put(OpaquenessProperty, interString);
        interString = i18n.get(RpfLayer.class, OpaquenessProperty, "Opaqueness");
        list.put(OpaquenessProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, NumColorsProperty, I18n.TOOLTIP, "Number of colors to use for the map images.");
        list.put(NumColorsProperty, interString);
        list.put(NumColorsProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ComboBoxPropertyEditor");
        list.put(NumColorsProperty + OptionPropertyEditor.ScopedOptionsProperty, "sixteen  thirtytwo twosixteen");
        list.put(NumColorsProperty + ".sixteen", "16");
        list.put(NumColorsProperty + ".thirtytwo", "32");
        list.put(NumColorsProperty + ".twosixteen", "216");
        interString = i18n.get(RpfLayer.class, NumColorsProperty, "Number of Colors");
        list.put(NumColorsProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, ShowMapsProperty, I18n.TOOLTIP, "Flag to display map images.");
        list.put(ShowMapsProperty, interString);
        list.put(ShowMapsProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
        interString = i18n.get(RpfLayer.class, ShowMapsProperty, "Display Images");
        list.put(ShowMapsProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, ShowInfoProperty, I18n.TOOLTIP, "Flag to show data attributes.");
        list.put(ShowInfoProperty, interString);
        list.put(ShowInfoProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
        interString = i18n.get(RpfLayer.class, ShowInfoProperty, "Display Attributes");
        list.put(ShowInfoProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, ScaleImagesProperty, I18n.TOOLTIP, "Flag to scale the images to fit the map scale.  If false, images appear when map scale fits the chart scale.");
        list.put(ScaleImagesProperty, interString);
        list.put(ScaleImagesProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
        interString = i18n.get(RpfLayer.class, ScaleImagesProperty, "Scale Images");
        list.put(ScaleImagesProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, ChartSeriesProperty, I18n.TOOLTIP, "The two-letter chart code to display.  ANY is default.");
        list.put(ChartSeriesProperty, interString);
        interString = i18n.get(RpfLayer.class, ChartSeriesProperty, "Chart Series Code");
        list.put(ChartSeriesProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, AutoFetchAttributeProperty, I18n.TOOLTIP, "Flag to tell the layer to automatically fetch the attribute data for the images.");
        list.put(AutoFetchAttributeProperty, interString);
        list.put(AutoFetchAttributeProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
        interString = i18n.get(RpfLayer.class, AutoFetchAttributeProperty, "Auto-fetch Attributes");
        list.put(AutoFetchAttributeProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, ImageScaleFactorProperty, I18n.TOOLTIP, "Multiplier to limit the scale differential that a given chart will be displayed for a map (4.0 is the default).");
        list.put(ImageScaleFactorProperty, interString);
        interString = i18n.get(RpfLayer.class, ImageScaleFactorProperty, "Image Scaling Factor");
        list.put(ImageScaleFactorProperty + LabelEditorProperty, interString);

        interString = i18n.get(RpfLayer.class, ColormodelProperty, I18n.TOOLTIP, "If 'indexed', the images will be built using a colortable.  This is not the default.");
        list.put(ColormodelProperty, interString);
        list.put(ColormodelProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ComboBoxPropertyEditor");
        list.put(ColormodelProperty + OptionPropertyEditor.ScopedOptionsProperty, "dir ind");
        list.put(ColormodelProperty + ".dir", COLORMODEL_DIRECT_STRING);
        list.put(ColormodelProperty + ".ind", COLORMODEL_INDEXED_STRING);
        interString = i18n.get(RpfLayer.class, ColormodelProperty, "Image Colormodel Type");
        list.put(ColormodelProperty + LabelEditorProperty, interString);

        return list;
    }

    /**
     * Specify what order properties should be presented in an editor.
     */
    public String getInitPropertiesOrder() {
        return " " + ShowMapsProperty + " " + ShowInfoProperty + " " + ScaleImagesProperty + " "
                + ImageScaleFactorProperty + " " + OpaquenessProperty + " " + NumColorsProperty
                + " " + ChartSeriesProperty + " " + AutoFetchAttributeProperty + " "
                + ColormodelProperty;
    }

    /**
     * Set the property key prefix that should be used by the PropertyConsumer.
     * The prefix, along with a '.', should be prepended to the property keys
     * known by the PropertyConsumer.
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
     * @return the property prefix
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }
}