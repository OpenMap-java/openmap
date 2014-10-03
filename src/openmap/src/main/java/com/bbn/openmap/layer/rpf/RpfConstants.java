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
     * Delete the cache is the layer is removed from the map. "killCache"
     */
    public static final String KillCacheProperty = "killCache";
    /** Set a limit on which chart types are displayed. "chartSeries" */
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
     * Property to use to change the color for coverage of this data. "CG.color"
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
    public final static RpfProductInfo LF = new RpfProductInfo("LF", "LFC-FR (Day)", "1:500,000", 500000f, "Low Flying Chart (Day) - Host Nation", CADRG);
    public final static RpfProductInfo JG = new RpfProductInfo("JG", "JOG", "1:250,000", 250000f, "Joint Operations Graphic", CADRG);
    public final static RpfProductInfo JA = new RpfProductInfo("JA", "JOG-A", "1:250,000", 250000f, "Joint Operations Graphic - Air", CADRG);
    public final static RpfProductInfo JR = new RpfProductInfo("JR", "JOG-R", "1:250,000", 250000f, "Joint Operations Graphic - Radar", CADRG);
    public final static RpfProductInfo TF = new RpfProductInfo("TF", "TFC", "1:250,000", 250000f, "Transit Flying Chart (UK)", CADRG);
    public final static RpfProductInfo AT = new RpfProductInfo("AT", "ATC", "1:200,000", 200000f, "Series 200 Air Target Chart", CADRG);
    public final static RpfProductInfo TC = new RpfProductInfo("TC", "TLM 100", "1:100,000", 100000f, "Topographic Line Map 1:100,000 scale", CADRG);
    public final static RpfProductInfo TL = new RpfProductInfo("TL", "TLM 50", "1:50,000", 50000f, TOPOLINEMAP, CADRG);
    public final static RpfProductInfo TT = new RpfProductInfo("TT", "TLM 25", "1:25,000", 25000f, "Topographic Line Map 1:25,000 scale", CADRG);
    public final static RpfProductInfo TQ = new RpfProductInfo("TQ", "TLM 24", "1:24,000", 24000f, "Topographic Line Map 1:24,000 scale", CADRG);
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

    /** New as of MIL-STD-2411-1 Change 3 */
    public final static RpfProductInfo EG = new RpfProductInfo("EG", "NARC", "1:11M", 11000000f, "North Atlantic Route Chart", CADRG);
    public final static RpfProductInfo ES = new RpfProductInfo("ES", "SEC", "1:500,000", 500000f, "VFR Sectional", CADRG);
    public final static RpfProductInfo ET = new RpfProductInfo("ET", "SEC", "1:250,000", 250000f, "VFR Sectional Insets", CADRG);
    public final static RpfProductInfo F1 = new RpfProductInfo("F1", "TFC-1", "1:250,000", 250000f, "Transit Flying Chart (TBD #1)", CADRG);
    public final static RpfProductInfo F2 = new RpfProductInfo("F2", "TFC-2", "1:250,000", 250000f, "Transit Flying Chart (TBD #2)", CADRG);
    public final static RpfProductInfo F3 = new RpfProductInfo("F3", "TFC-3", "1:250,000", 250000f, "Transit Flying Chart (TBD #3)", CADRG);
    public final static RpfProductInfo F4 = new RpfProductInfo("F4", "TFC-4", "1:250,000", 250000f, "Transit Flying Chart (TBD #4)", CADRG);
    public final static RpfProductInfo F5 = new RpfProductInfo("F5", "TFC-5", "1:250,000", 250000f, "Transit Flying Chart (TBD #5)", CADRG);
    public final static RpfProductInfo I5 = new RpfProductInfo("I5", BLANK, ".5m", 3333f, "Imagery, .5 (half) meter resolution", CIB);
    public final static RpfProductInfo IV = new RpfProductInfo("IV", BLANK, VARIOUS, Various, "Imagery, > 10 meter resolution", CIB);
    public final static RpfProductInfo JO = new RpfProductInfo("JO", "OPG", "1:250,000", 250000f, "Operations Planning Graphic", CADRG);
    public final static RpfProductInfo K1 = new RpfProductInfo("K1", "ICM", "1:8,000", 8000f, "Image City Maps", CADRG);
    public final static RpfProductInfo K2 = new RpfProductInfo("K2", "ICM", "1:10,000", 10000f, "Image City Maps", CADRG);
    public final static RpfProductInfo K3 = new RpfProductInfo("K3", "ICM", "1:10,560", 10560f, "Image City Maps", CADRG);
    public final static RpfProductInfo K7 = new RpfProductInfo("K7", "ICM", "1:12,500", 12500f, "Image City Maps", CADRG);
    public final static RpfProductInfo K8 = new RpfProductInfo("K8", "ICM", "1:12,800", 12000f, "Image City Maps", CADRG);
    public final static RpfProductInfo KB = new RpfProductInfo("KB", "ICM", "1:15,000", 15000f, "Image City Maps", CADRG);
    public final static RpfProductInfo KE = new RpfProductInfo("KE", "ICM", "1:16,666", 16666f, "Image City Maps", CADRG);
    public final static RpfProductInfo KM = new RpfProductInfo("KM", "ICM", "1:21,120", 21120f, "Image City Maps", CADRG);
    public final static RpfProductInfo KR = new RpfProductInfo("KR", "ICM", "1:25,000", 25000f, "Image City Maps", CADRG);
    public final static RpfProductInfo KS = new RpfProductInfo("KS", "ICM", "1:26,000", 26000f, "Image City Maps", CADRG);
    public final static RpfProductInfo KU = new RpfProductInfo("KU", "ICM", "1:36,000", 36000f, "Image City Maps", CADRG);
    public final static RpfProductInfo L1 = new RpfProductInfo("L1", "LFC-1", "1:500,000", 500000f, "Low Flying Chart (TBD #1)", CADRG);
    public final static RpfProductInfo L2 = new RpfProductInfo("L2", "LFC-2", "1:500,000", 500000f, "Low Flying Chart (TBD #2)", CADRG);
    public final static RpfProductInfo L3 = new RpfProductInfo("L3", "LFC-3", "1:500,000", 500000f, "Low Flying Chart (TBD #3)", CADRG);
    public final static RpfProductInfo L4 = new RpfProductInfo("L4", "LFC-4", "1:500,000", 500000f, "Low Flying Chart (TBD #4)", CADRG);
    public final static RpfProductInfo L5 = new RpfProductInfo("L5", "LFC-5", "1:500,000", 500000f, "Low Flying Chart (TBD #5)", CADRG);
    public final static RpfProductInfo LN = new RpfProductInfo("LN", "LFC (Night)", "1:500,000", 500000f, "Low Flying Chart (Night) - Host Nation", CADRG);
    public final static RpfProductInfo M1 = new RpfProductInfo("M1", "MIM", VARIOUS, Various, "Military Installation Map (TBD #1)", CADRG);
    public final static RpfProductInfo M2 = new RpfProductInfo("M2", "MIM", VARIOUS, Various, "Military Installation Map (TBD #2)", CADRG);
    public final static RpfProductInfo MH = new RpfProductInfo("MH", "MIM", "1:25,000", 25000f, "Military Installation Maps", CADRG);
    public final static RpfProductInfo MI = new RpfProductInfo("MI", "MIM", "1:50,000", 50000f, "Military Installation Maps", CADRG);
    public final static RpfProductInfo MJ = new RpfProductInfo("MJ", "MIM", "1:100,000", 100000f, "Military Installation Maps", CADRG);
    public final static RpfProductInfo OH = new RpfProductInfo("OH", "VHRC", "1:1,000,000", 1000000f, "VFR Helicopter Route Chart", CADRG);
    public final static RpfProductInfo OW = new RpfProductInfo("OW", "WAC", "1:1,000,000", 1000000f, "High Flying Chart - Host Nation", CADRG);
    public final static RpfProductInfo P1 = new RpfProductInfo("P1", BLANK, "1:25,000", 25000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo P2 = new RpfProductInfo("P2", BLANK, "1:25,000", 25000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo P3 = new RpfProductInfo("P3", BLANK, "1:25,000", 25000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo P4 = new RpfProductInfo("P4", BLANK, "1:25,000", 25000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo P5 = new RpfProductInfo("P5", BLANK, "1:50,000", 50000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo P6 = new RpfProductInfo("P6", BLANK, "1:50,000", 50000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo P7 = new RpfProductInfo("P7", BLANK, "1:50,000", 50000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo P8 = new RpfProductInfo("P8", BLANK, "1:50,000", 50000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo P9 = new RpfProductInfo("P9", BLANK, "1:100,000", 100000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo PA = new RpfProductInfo("PA", BLANK, "1:100,000", 100000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PB = new RpfProductInfo("PB", BLANK, "1:100,000", 100000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PC = new RpfProductInfo("PC", BLANK, "1:100,000", 100000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PD = new RpfProductInfo("PD", BLANK, "1:250,000", 250000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo PE = new RpfProductInfo("PE", BLANK, "1:250,000", 250000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PF = new RpfProductInfo("PF", BLANK, "1:250,000", 250000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PG = new RpfProductInfo("PG", BLANK, "1:250,000", 250000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PH = new RpfProductInfo("PH", BLANK, "1:500,000", 500000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo PI = new RpfProductInfo("PI", BLANK, "1:500,000", 500000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PJ = new RpfProductInfo("PJ", BLANK, "1:500,000", 500000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PK = new RpfProductInfo("PK", BLANK, "1:500,000", 500000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PL = new RpfProductInfo("PL", BLANK, "1:1,000,000", 1000000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo PM = new RpfProductInfo("PM", BLANK, "1:1,000,000", 1000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PN = new RpfProductInfo("PN", BLANK, "1:1,000,000", 1000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PO = new RpfProductInfo("PO", BLANK, "1:1,000,000", 1000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PP = new RpfProductInfo("PP", BLANK, "1:2,000,000", 2000000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo PQ = new RpfProductInfo("PQ", BLANK, "1:2,000,000", 2000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PR = new RpfProductInfo("PR", BLANK, "1:2,000,000", 2000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PS = new RpfProductInfo("PS", BLANK, "1:5,000,000", 5000000f, "Special Military Map - Overlay", CADRG);
    public final static RpfProductInfo PT = new RpfProductInfo("PT", BLANK, "1:5,000,000", 5000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PU = new RpfProductInfo("PU", BLANK, "1:5,000,000", 5000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo PV = new RpfProductInfo("PV", BLANK, "1:5,000,000", 5000000f, "Special Military Purpose", CADRG);
    public final static RpfProductInfo R1 = new RpfProductInfo("R1", BLANK, "1:50,000", 50000f, "Range Charts", CADRG);
    public final static RpfProductInfo R2 = new RpfProductInfo("R2", BLANK, "1:100,000", 100000f, "Range Charts", CADRG);
    public final static RpfProductInfo R3 = new RpfProductInfo("R3", BLANK, "1:250,000", 250000f, "Range Charts", CADRG);
    public final static RpfProductInfo R4 = new RpfProductInfo("R4", BLANK, "1:500,000", 500000f, "Range Charts", CADRG);
    public final static RpfProductInfo R5 = new RpfProductInfo("R5", BLANK, "1:1,000,000", 1000000f, "Range Charts", CADRG);
    public final static RpfProductInfo RC = new RpfProductInfo("RC", "RGS-100", "1:10,000", 10000f, "Russian General Staff Maps", CADRG);
    public final static RpfProductInfo RL = new RpfProductInfo("RL", "RGS-50", "1:50,000", 50000f, "Russian General Staff Maps", CADRG);
    public final static RpfProductInfo RR = new RpfProductInfo("RR", "RGS-200", "1:200,000", 200000f, "Russian General Staff Maps", CADRG);
    public final static RpfProductInfo RV = new RpfProductInfo("RV", "Riverine", "1:50,000", 50000f, "Riverine Map 1:50,000 scale", CADRG);
    public final static RpfProductInfo TN = new RpfProductInfo("TN", "TFC (Night)", "1:250,000", 250000f, "Transit Flying Chart (Night) - Host nation", CADRG);
    public final static RpfProductInfo TR = new RpfProductInfo("TR", "TLM 100", "1:200,000", 200000f, "Topographic Line Map 1:200,000 scale", CADRG);
    public final static RpfProductInfo UL = new RpfProductInfo("UL", "TLM50-Other", "1:50,000", 50000f, "Topographic Line Map (other 1:50,000 scale)", CADRG);
    public final static RpfProductInfo V1 = new RpfProductInfo("V1", "HRC Inset", "1:50,000", 50000f, "Helicopter Route Chart Inset", CADRG);
    public final static RpfProductInfo V2 = new RpfProductInfo("V2", "HRC Inset", "1:62,500", 62500f, "Helicopter Route Chart Inset", CADRG);
    public final static RpfProductInfo V3 = new RpfProductInfo("V3", "HRC Inset", "1:90,000", 90000f, "Helicopter Route Chart Inset", CADRG);
    public final static RpfProductInfo V4 = new RpfProductInfo("V4", "HRC Inset", "1:250,000", 250000f, "Helicopter Route Chart Inset", CADRG);
    public final static RpfProductInfo VH = new RpfProductInfo("VH", "HRC", "1:125,000", 125000f, "Helicopter Route Chart", CADRG);
    public final static RpfProductInfo VN = new RpfProductInfo("VN", "VNC", "1:500,000", 500000f, "Visual Navigation Charts", CADRG);
    public final static RpfProductInfo VT = new RpfProductInfo("VT", "VTAC", "1:250,000", 250000f, "VFR Terminal Area Chart", CADRG);
    public final static RpfProductInfo WA = new RpfProductInfo("WA", BLANK, "1:250,000", 250000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WB = new RpfProductInfo("WB", BLANK, "1:500,000", 500000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WC = new RpfProductInfo("WC", BLANK, "1:750,000", 750000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WD = new RpfProductInfo("WD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WE = new RpfProductInfo("WE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WF = new RpfProductInfo("WF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WG = new RpfProductInfo("WG", BLANK, "1:2,500,000", 2500000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WH = new RpfProductInfo("WH", BLANK, "1:3,000,000", 3000000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WI = new RpfProductInfo("WI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo WK = new RpfProductInfo("WK", BLANK, "1:4,500,000", 4500000f, "IFR Enroute Low", CADRG);
    public final static RpfProductInfo XD = new RpfProductInfo("XD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo XE = new RpfProductInfo("XE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo XF = new RpfProductInfo("XF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo XG = new RpfProductInfo("XG", BLANK, "1:2,500,000", 2500000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo XH = new RpfProductInfo("XH", BLANK, "1:3,000,000", 3000000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo XI = new RpfProductInfo("XI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo XJ = new RpfProductInfo("XJ", BLANK, "1:3,000,000", 3000000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo XK = new RpfProductInfo("XK", BLANK, "1:4,500,000", 4500000f, "IFR Enroute High", CADRG);
    public final static RpfProductInfo Y9 = new RpfProductInfo("Y9", BLANK, "1:16,500,000", 16500000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YA = new RpfProductInfo("YA", BLANK, "1:250,000", 250000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YB = new RpfProductInfo("YB", BLANK, "1:500,000", 500000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YC = new RpfProductInfo("YC", BLANK, "1:750,000", 750000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YD = new RpfProductInfo("YD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YE = new RpfProductInfo("YE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YF = new RpfProductInfo("YF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YI = new RpfProductInfo("YI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YJ = new RpfProductInfo("YJ", BLANK, "1:4,000,000", 4000000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo YZ = new RpfProductInfo("YZ", BLANK, "1:12,000,000", 12000000f, "IFR Enroute Area", CADRG);
    public final static RpfProductInfo Z8 = new RpfProductInfo("Z8", BLANK, "1:16,000,000", 16000000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZA = new RpfProductInfo("ZA", BLANK, "1:250,000", 250000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZB = new RpfProductInfo("ZB", BLANK, "1:500,000", 500000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZC = new RpfProductInfo("ZC", BLANK, "1:750,000", 750000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZD = new RpfProductInfo("ZD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZE = new RpfProductInfo("ZE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZF = new RpfProductInfo("ZF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZG = new RpfProductInfo("ZG", BLANK, "1:2,500,000", 2500000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZH = new RpfProductInfo("ZH", BLANK, "1:3,000,000", 3000000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZI = new RpfProductInfo("ZI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZJ = new RpfProductInfo("ZJ", BLANK, "1:4,000,000", 4000000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZK = new RpfProductInfo("ZK", BLANK, "1:4,500,000", 4500000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZT = new RpfProductInfo("ZT", BLANK, "1:9,000,000", 9000000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZV = new RpfProductInfo("ZV", BLANK, "1:10,000,000", 10000000f, "IFR Enroute High/Low", CADRG);
    public final static RpfProductInfo ZZ = new RpfProductInfo("ZZ", BLANK, "1:12,000,000", 12000000f, "IFR Enroute High/Low", CADRG);

}