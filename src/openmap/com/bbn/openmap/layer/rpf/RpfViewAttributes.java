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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfViewAttributes.java,v $
// $RCSfile: RpfViewAttributes.java,v $
// $Revision: 1.2 $
// $Date: 2003/06/25 15:28:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.rpf;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.propertyEditor.Inspector;
import com.bbn.openmap.util.PropUtils;

/** 
 * This class contains information to pass through the
 * RpfCacheManager and RpfCacheHandlers to describe limitations and
 * parameters desired for data view.  It contains information about
 * the numbers of colors to use, how opaque to make the images, the
 * chart series to retrieve, etc.
 */
public class RpfViewAttributes implements RpfConstants, PropertyConsumer {

    public final static String ANY = "ANY";
    public final static String ALL = "ALL";
    protected String propertyPrefix = null;

    /** Number of colors to use - 16, 32, 216 */
    public int numberOfColors;
    /** The opaqueness of the image (transparency) 0-255: 0 is clear,
     *  255 is opaque. */
    public int opaqueness;
    /** The image colormodel to use, indexed or colortable, for the
     *  OMRasters. */
    public int colorModel;
    /** Flag to use to scale images or not.  This will cause the
     *  caches to scale the images to match the map scale, instead of
     *  requiring the map to be at the same scale as the desired
     *  image. */
    public boolean scaleImages;
    /** The limiting factor for image scaling.  Make this too big, and
     *  you will run out of memory. */
    public float imageScaleFactor;
    /** The data series two-letter code to limit responses to.  If you
     *  want any data that is the closest match for the current map,
     *  use ANY (default).  If you want all of the RpfCoverageBoxes
     *  that have some coverage, use ALL. */
    public String chartSeries;
    /** Flag to set if the CADRG projection is required. Might not be,
     *  if we start warping images.*/
    public boolean requireProjection;
    /** Flag to display images. */
    public boolean showMaps;
    /** Flag to display attribute information about the subframes. */
    public boolean showInfo;
    /** CADRG Projection of the map. */
    public CADRG proj;
    /** Autofetch the subframe attributes from the FrameProvider. Use
     *  only if you are interested in background information about
     *  the images. */
    public boolean autofetchAttributes;

    public RpfViewAttributes(){
	setDefaults();
    }

    public void setDefaults(){
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
     * Sets the properties.  This particular method assumes
     * that the marker name is not needed, because all of the contents
     * of this Properties object are to be used for this object, and
     * scoping the properties with a prefix is unnecessary.
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

	opaqueness = LayerUtils.intFromProperties(props, prefix + OpaquenessProperty, opaqueness);
	
	numberOfColors = LayerUtils.intFromProperties(props, prefix + NumColorsProperty, numberOfColors);

	showMaps = LayerUtils.booleanFromProperties(props, prefix + ShowMapsProperty, showMaps);

	showInfo = LayerUtils.booleanFromProperties(props, prefix + ShowInfoProperty, showInfo);

	scaleImages = LayerUtils.booleanFromProperties(props, prefix + ScaleImagesProperty, scaleImages);

	chartSeries = props.getProperty(prefix + ChartSeriesProperty);

	autofetchAttributes = LayerUtils.booleanFromProperties(props, prefix + AutoFetchAttributeProperty, autofetchAttributes);

	imageScaleFactor =  LayerUtils.floatFromProperties(props, prefix + ImageScaleFactorProperty, imageScaleFactor);

	String colormodel = props.getProperty(prefix + ColormodelProperty);
	if (colormodel != null && colormodel.equalsIgnoreCase("indexed")) {
	    colorModel = OMRasterObject.COLORMODEL_INDEXED;
	} else {
	    colorModel = OMRasterObject.COLORMODEL_DIRECT;
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

	props.put(prefix + OpaquenessProperty, Integer.toString(opaqueness));
	props.put(prefix + NumColorsProperty, Integer.toString(numberOfColors));
	props.put(prefix + ShowMapsProperty, new Boolean(showMaps).toString());
	props.put(prefix + ShowInfoProperty, new Boolean(showInfo).toString());
	props.put(prefix + ScaleImagesProperty, new Boolean(scaleImages).toString());
	props.put(prefix + ChartSeriesProperty, chartSeries);
	props.put(prefix + AutoFetchAttributeProperty, new Boolean(autofetchAttributes).toString());
	props.put(prefix + ImageScaleFactorProperty, Float.toString(imageScaleFactor));

	if (colorModel == OMRasterObject.COLORMODEL_INDEXED) {
	    props.put(prefix + ColormodelProperty, "indexed");
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
	
	list.put(OpaquenessProperty, "Integer representing opaqueness level (0-255, 0 is clear)");
	list.put(NumColorsProperty, "Number of colors to use for the maps (16, 32, 216)");
	list.put(ShowMapsProperty, "Flag to display maps");
	list.put(ShowMapsProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	list.put(ShowInfoProperty, "Flag to show data attributes");
	list.put(ShowInfoProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	list.put(ScaleImagesProperty, "Flag to scale the images to fit the map scale.  If false, images appear when map scale fits the chart scale.");
	list.put(ScaleImagesProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	list.put(ChartSeriesProperty, "The chart scale code to display.  ANY is default");
	list.put(AutoFetchAttributeProperty, "Flag to tell the layer to automatically fetch the attribute data for the images");
	list.put(AutoFetchAttributeProperty + ScopedEditorProperty,
		 "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

	list.put(ImageScaleFactorProperty, "Multiplier to limit the scales that a given chart will be displayed for a map (4.0 is the default).");
	list.put(ColormodelProperty, "If 'indexed', the images will be built using a colortable.  This is not the default.");

	return list;
    }

    /**
     * Specify what order properties should be presented in an editor.
     */
    public String getInitPropertiesOrder() {
	return " " + ShowMapsProperty + " " + ShowInfoProperty + " " + ScaleImagesProperty + " " + ImageScaleFactorProperty + " " + OpaquenessProperty + " " + NumColorsProperty + " " + ChartSeriesProperty + " " + AutoFetchAttributeProperty + " " + ColormodelProperty;
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
}
