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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfConstants.java,v $
// $RCSfile: RpfConstants.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:03 $
// $Author: dietrick $
//
// **********************************************************************
package com.bbn.openmap.layer.rpf;

/**
 * The properties and constants used for RPF data and RPF Coverages.
 */
public interface RpfConstants {

	/**
	 * Property to use for images or rectangles (when java supports it).
	 * "opaque"
	 */
	public static final String OpaquenessProperty = "opaque";
	/**
	 * Property to use to fill rectangles. "fill"
	 */
	public static final String FillProperty = "fill";
	/**
	 * Paths to the A.TOC properties. "paths"
	 */
	public static final String RpfPathsProperty = "paths";
	/**
	 * Number of colors to use in images. "numberColors"
	 */
	public static final String NumColorsProperty = "numberColors";
	/**
	 * Show the images. "showMaps"
	 */
	public static final String ShowMapsProperty = "showMaps";
	/**
	 * Show the attribute information for the RPF data. "showInfo"
	 */
	public static final String ShowInfoProperty = "showInfo";
	/**
	 * Scale toe images to match the map scale. "scaleImages"
	 */
	public static final String ScaleImagesProperty = "scaleImages";
	/**
	 * Delete the cache is the layer is removed from the map. "killCache"
	 */
	public static final String KillCacheProperty = "killCache";
	/**
	 * Set a limit on which chart types are displayed. "chartSeries"
	 */
	public static final String ChartSeriesProperty = "chartSeries";
	/**
	 * Tell the RpfLayer you want the coverage tool available. "coverage"
	 */
	public static final String CoverageProperty = "coverage";
	/**
	 * Tell the RpfLayer which colormodel to use (INDEXED or DIRECT (default)).
	 * "colormodel"
	 */
	public static final String ColormodelProperty = "colormodel";
	/**
	 * Tell the RpfLayer how big to make the subframe cache..
	 * "subframeCacheSize"
	 */
	public static final String CacheSizeProperty = "subframeCacheSize";
	/**
	 * Tell the RpfLayer how big to make the subframe cache..
	 * "auxSubframeCacheSize"
	 */
	public static final String AuxCacheSizeProperty = "auxSubframeCacheSize";
	/**
	 * Tell the RpfLayer to get the detailed subframe attributes for each
	 * subframe.
	 */
	public static final String AutoFetchAttributeProperty = "autofetchAttributes";
	/**
	 * The amount of scaling to allow on images. Default is 4x, which also means
	 * 1/4th
	 */
	public static final String ImageScaleFactorProperty = "imageScaleFactor";
	/**
	 * Property to specify that matching scale is more important that maximizing
	 * coverage. Default is true, so you can see the smaller coverage that might
	 * match map scale. This setting is more important for layer being used to
	 * create smaller tile images, and you might prefer fill coverage over small
	 * blank spaces.
	 */
	public static final String ScaleOverCoverageProperty = "scaleOverCoverage";

	// GUI button commands
	public static final String showMapsCommand = "mapsCheckCmd";
	public static final String showInfoCommand = "infoCheckCmd";
	public static final String lockSeriesCommand = "lockSeriesCmd";
	public static final String showCoverageCommand = "coverageCheckCmd";
	public final static String showCGCommand = "showCG";

	public final static String unlockedButtonTitle = "Limit Chart Selection";
	public final static String lockedButtonTitle = "Displaying Only";

	/**
	 * Used to turn a particular coverage on or off. Well, off, really. All
	 * coverages are turned on by default.
	 */
	public final static String ShowCoverageProperty = "showcov";
	/**
	 * Use this property to change the color used for a particular chart type,
	 * i.e. propertyPrefix.chartSeriesAbbr.color = hexColorValue.
	 */
	public final static String ColorProperty = "color";

	public static final String DefaultRPFCoveragePrefix = "rpfcov";

	public final static float Various = -1f;
	public final static String BLANK = "";
	public final static String VARIOUS = "Various";
	public final static String CADRG = "CADRG";
	public final static String CIB = "CIB";
	public final static String CDTED = "CDTED";
	public final static String CITYGRAPHICS = "City Graphics";
	public final static String TOPOLINEMAP = "Topographic Line Map";

}
