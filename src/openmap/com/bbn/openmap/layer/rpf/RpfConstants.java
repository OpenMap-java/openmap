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
     * Property to use for images or rectangles (when java supports
     * it). "opaque"
     */
    public static final String OpaquenessProperty = "opaque";
    /** Property to use to fill rectangles. "fill" */
    public static final String FillProperty = "fill";
    /** Paths to the A.TOC properties. "paths" */
    public static final String RpfPathsProperty = "paths";
    /** Number of colors to use in images. "numberColors" */
    public static final String NumColorsProperty = "numberColors";
    /** Show the images. "showMaps" */
    public static final String ShowMapsProperty = "showMaps";
    /** Show the attribute information for the RPF data. "showInfo" */
    public static final String ShowInfoProperty = "showInfo";
    /** Scale toe images to match the map scale. "scaleImages" */
    public static final String ScaleImagesProperty = "scaleImages";
    /**
     * Delete the cache is the layer is removed from the map.
     * "killCache"
     */
    public static final String KillCacheProperty = "killCache";
    /** Set a limit on which chart types are displayed. "chartSeries" */
    public static final String ChartSeriesProperty = "chartSeries";
    /**
     * Tell the RpfLayer you want the coverage tool available.
     * "coverage"
     */
    public static final String CoverageProperty = "coverage";
    /**
     * Tell the RpfLayer which colormodel to use (INDEXED or DIRECT
     * (default)). "colormodel"
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
     * Tell the RpfLayer to get the detailed subframe attributes for
     * each subframe.
     */
    public static final String AutoFetchAttributeProperty = "autofetchAttributes";
    /**
     * The amount of scaling to allow on images. Default is 4x, which
     * also means 1/4th
     */
    public static final String ImageScaleFactorProperty = "imageScaleFactor";

    // GUI button commands
    public static final String showMapsCommand = "mapsCheckCmd";
    public static final String showInfoCommand = "infoCheckCmd";
    public static final String lockSeriesCommand = "lockSeriesCmd";
    public static final String showCoverageCommand = "coverageCheckCmd";
    public final static String showCGCommand = "showCG";
    public final static String showTLMCommand = "showTLM";
    public final static String showJOGCommand = "showJOG";
    public final static String showTPCCommand = "showTPC";
    public final static String showONCCommand = "showONC";
    public final static String showJNCCommand = "showJNC";
    public final static String showGNCCommand = "showGNC";
    public final static String showCIB10Command = "showCIB10";
    public final static String showCIB5Command = "showCIB5";
    public final static String showMISCCommand = "showMISC";

    public final static String unlockedButtonTitle = "Limit Chart Selection";
    public final static String lockedButtonTitle = "Displaying Only";

    /** Property setting to show this data on startup. "CG.showcov" */
    public static final String ShowCGProperty = "CG.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "CG.color"
     */
    public static final String CGColorProperty = "CG.color";

    /** Property setting to show this data on startup. "TLM.showcov" */
    public static final String ShowTLMProperty = "TLM.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "TLM.color"
     */
    public static final String TLMColorProperty = "TLM.color";

    /** Property setting to show this data on startup. "JOG.showcov" */
    public static final String ShowJOGProperty = "JOG.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "JOG.color"
     */
    public static final String JOGColorProperty = "JOG.color";

    /** Property setting to show this data on startup. "TPC.showcov" */
    public static final String ShowTPCProperty = "TPC.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "TPC.color"
     */
    public static final String TPCColorProperty = "TPC.color";

    /** Property setting to show this data on startup. "ONC.showcov" */
    public static final String ShowONCProperty = "ONC.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "ONC.color"
     */
    public static final String ONCColorProperty = "ONC.color";

    /** Property setting to show this data on startup. "JNC.showcov" */
    public static final String ShowJNCProperty = "JNC.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "JNC.color"
     */
    public static final String JNCColorProperty = "JNC.color";

    /** Property setting to show this data on startup. "GNC.showcov" */
    public static final String ShowGNCProperty = "GNC.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "GNC.color"
     */
    public static final String GNCColorProperty = "GNC.color";

    /** Property setting to show this data on startup. "CIB10.showcov" */
    public static final String ShowCIB10Property = "CIB10.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "CIB10.color"
     */
    public static final String CIB10ColorProperty = "CIB10.color";

    /** Property setting to show this data on startup. "CIB5.showcov" */
    public static final String ShowCIB5Property = "CIB5.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "CIB5.color"
     */
    public static final String CIB5ColorProperty = "CIB5.color";

    /** Property setting to show this data on startup. "MISC.showcov" */
    public static final String ShowMISCProperty = "MISC.showcov";
    /**
     * Property to use to change the color for coverage of this data.
     * "MISC.color"
     */
    public static final String MISCColorProperty = "MISC.color";

    public static final String DefaultRPFCoveragePrefix = "rpfcov";

    public final static float Various = -1f;
    public final static String BLANK = "";
    public final static String VARIOUS = "Various";
    public final static String CADRG = "CADRG";
    public final static String CIB = "CIB";
    public final static String CDTED = "CDTED";
    public final static String CITYGRAPHICS = "City Graphics";
    public final static String TOPOLINEMAP = "Topographic Line Map";

    public final static RpfProductInfo GN = new RpfProductInfo("GN", "GNC", "1:5,000,000", 5000000f, "Global Navigation Chart", CADRG);
    public final static RpfProductInfo JN = new RpfProductInfo("JN", "JNC", "1:2,000,000", 2000000f, "Jet Navigation Chart", CADRG);
    public final static RpfProductInfo ON = new RpfProductInfo("ON", "ONC", "1:1,000,000", 1000000f, "Operational Navigation Chart", CADRG);
    public final static RpfProductInfo TP = new RpfProductInfo("TP", "TPC", "1:500,000", 500000f, "Tactical Pilotage Chart", CADRG);
    public final static RpfProductInfo LF = new RpfProductInfo("LF", "LFC", "1:500,000", 500000f, "Low Flying Chart (UK)", CADRG);
    public final static RpfProductInfo JG = new RpfProductInfo("JG", "JOG", "1:250,000", 250000f, "Joint Operations Graphic", CADRG);
    public final static RpfProductInfo JA = new RpfProductInfo("JA", "JOG-A", "1:250,000", 250000f, "Joint Operations Graphic - Air", CADRG);
    public final static RpfProductInfo JR = new RpfProductInfo("JR", "JOG-R", "1:250,000", 250000f, "Joint Operations Graphic - Radar", CADRG);
    public final static RpfProductInfo TF = new RpfProductInfo("TF", "TFC", "1:250,000", 250000f, "Transit Flying Chart (UK)", CADRG);
    public final static RpfProductInfo AT = new RpfProductInfo("AT", "ATC", "1:200,000", 200000f, "Series 200 Air Target Chart", CADRG);
    public final static RpfProductInfo TC = new RpfProductInfo("TC", "TLM 100", "1:100,000", 100000f, TOPOLINEMAP, CADRG);
    public final static RpfProductInfo TL = new RpfProductInfo("TL", "TLM 50", "1:50,000", 50000f, TOPOLINEMAP, CADRG);
    public final static RpfProductInfo TT = new RpfProductInfo("TT", "TLM 25", "1:25,000", 25000f, TOPOLINEMAP, CADRG);
    public final static RpfProductInfo TQ = new RpfProductInfo("TQ", "TLM 24", "1:24,000", 24000f, "USGS Digital 7.5\" Quadrangles", CADRG);
    public final static RpfProductInfo HA = new RpfProductInfo("HA", "HA", VARIOUS, Various, "Harbor and Approach Charts", CADRG);
    public final static RpfProductInfo CO = new RpfProductInfo("CO", "CO", VARIOUS, Various, "Coastal Charts", CADRG);
    public final static RpfProductInfo OA = new RpfProductInfo("OA", "OPEAREA", VARIOUS, Various, "Naval Range Operating Area Chart", CADRG);
    public final static RpfProductInfo CG = new RpfProductInfo("CG", "CG", VARIOUS, Various, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C1 = new RpfProductInfo("C1", "CG", "1:10,000", 10000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C2 = new RpfProductInfo("C2", "CG", "1:10,560", 10560f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C3 = new RpfProductInfo("C3", "CG", "1:11,000", 11000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C4 = new RpfProductInfo("C4", "CG", "1:11,800", 11800f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C5 = new RpfProductInfo("C5", "CG", "1:12,000", 12000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C6 = new RpfProductInfo("C6", "CG", "1:12,500", 12500f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C7 = new RpfProductInfo("C7", "CG", "1:12,800", 12800f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C8 = new RpfProductInfo("C8", "CG", "1:14,000", 14000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo C9 = new RpfProductInfo("C9", "CG", "1:14,700", 14700f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CA = new RpfProductInfo("CA", "CG", "1:15,000", 15000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CB = new RpfProductInfo("CB", "CG", "1:15,500", 15500f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CC = new RpfProductInfo("CC", "CG", "1:16,000", 16000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CD = new RpfProductInfo("CD", "CG", "1:16,666", 16666f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CE = new RpfProductInfo("CE", "CG", "1:17,000", 17000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CF = new RpfProductInfo("CF", "CG", "1:17,500", 17500f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CH = new RpfProductInfo("CH", "CG", "1:18,000", 18000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CJ = new RpfProductInfo("CJ", "CG", "1:20,000", 20000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CK = new RpfProductInfo("CK", "CG", "1:21,000", 21000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CL = new RpfProductInfo("CL", "CG", "1:21,120", 21120f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CN = new RpfProductInfo("CN", "CG", "1:22,000", 22000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CP = new RpfProductInfo("CP", "CG", "1:23,000", 23000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CQ = new RpfProductInfo("CQ", "CG", "1:25,000", 25000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CR = new RpfProductInfo("CR", "CG", "1:26,000", 26000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CS = new RpfProductInfo("CS", "CG", "1:35,000", 35000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CT = new RpfProductInfo("CT", "CG", "1:36,000", 36000f, CITYGRAPHICS, CADRG);
    public final static RpfProductInfo CM = new RpfProductInfo("CM", "CM", VARIOUS, Various, "Combat Charts", CADRG);
    public final static RpfProductInfo A1 = new RpfProductInfo("A1", "CM", "1:10,000", 10000f, "Combat Graphics, 1:10,000 scale", CADRG);
    public final static RpfProductInfo A2 = new RpfProductInfo("A2", "CM", "1:25,000", 25000f, "Combat Graphics, 1:25,000 scale", CADRG);
    public final static RpfProductInfo A3 = new RpfProductInfo("A3", "CM", "1:50,000", 50000f, "Combat Graphics, 1:50,000 scale", CADRG);
    public final static RpfProductInfo A4 = new RpfProductInfo("A4", "CM", "1:100,000", 100000f, "Combat Graphics, 1:100,000 scale", CADRG);
    public final static RpfProductInfo MM = new RpfProductInfo("MM", BLANK, VARIOUS, Various, "(Miscellaneous Maps & Charts)", CADRG);
    public final static RpfProductInfo IM = new RpfProductInfo("IM", BLANK, "10m", 66666f, "Imagery, 10 meter resolution", CIB);
    public final static RpfProductInfo I1 = new RpfProductInfo("I1", BLANK, "10m", 66666f, "Imagery, 10 meter resolution", CIB);
    public final static RpfProductInfo I2 = new RpfProductInfo("I2", BLANK, "5m", 33333f, "Imagery, 5 meter resolution", CIB);
    public final static RpfProductInfo I3 = new RpfProductInfo("I3", BLANK, "2m", 13333.2f, "Imagery, 2 meter resolution", CIB);
    public final static RpfProductInfo I4 = new RpfProductInfo("I4", BLANK, "1m", 6666f, "Imagery, 1 meter resolution", CIB);
    public final static RpfProductInfo D1 = new RpfProductInfo("D1", BLANK, "100m", Various, "Elevation Data from DTED level 1", CDTED);
    public final static RpfProductInfo D2 = new RpfProductInfo("D2", BLANK, "30m", Various, "Elevation Data from DTED level 2", CDTED);
    public final static RpfProductInfo OV = new RpfProductInfo("OV", "Overview", VARIOUS, Various, "Overview Image", CADRG);
    public final static RpfProductInfo OI = new RpfProductInfo("OI", "Overview", VARIOUS, Various, "Overview Image", CADRG);
    public final static RpfProductInfo LG = new RpfProductInfo("LG", "Legend", VARIOUS, Various, "Legend Data", CADRG);
    public final static RpfProductInfo UK = new RpfProductInfo("UK", "Unknown", VARIOUS, Various, "Unknown", CADRG);
    public final static RpfProductInfo DT = new RpfProductInfo("DT", "CDTED", "100M", 666666f, "Compressed DTED", CDTED);

}