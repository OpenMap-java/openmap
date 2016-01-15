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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfProductInfo.java,v $
// $RCSfile: RpfProductInfo.java,v $
// $Revision: 1.5 $
// $Date: 2005/02/11 22:34:14 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * This class describes the attributes of the different RPF products available. 
 */
package com.bbn.openmap.layer.rpf;

/**
 * Contains basic information about the different map and imagery types
 * supported by the RPF format. This information is based on the specification
 * released by NIMA and contains the conventions they have listed in the RPF
 * specification.
 */
public enum RpfProductInfo implements RpfConstants {

	GN("GN", "GNC", "1:5,000,000", 5000000f, "Global Navigation Chart", CADRG),
	JN("JN", "JNC", "1:2,000,000", 2000000f, "Jet Navigation Chart", CADRG),
	ON("ON", "ONC", "1:1,000,000", 1000000f, "Operational Navigation Chart", CADRG),
	TP("TP", "TPC", "1:500,000", 500000f, "Tactical Pilotage Chart", CADRG),
	LF("LF", "LFC-FR (Day)", "1:500,000", 500000f, "Low Flying Chart (Day) - Host Nation", CADRG),
	JG("JG", "JOG", "1:250,000", 250000f, "Joint Operations Graphic", CADRG),
	JA("JA", "JOG-A", "1:250,000", 250000f, "Joint Operations Graphic - Air", CADRG),
	JR("JR", "JOG-R", "1:250,000", 250000f, "Joint Operations Graphic - Radar", CADRG),
	TF("TF", "TFC", "1:250,000", 250000f, "Transit Flying Chart (UK)", CADRG),
	AT("AT", "ATC", "1:200,000", 200000f, "Series 200 Air Target Chart", CADRG),
	TC("TC", "TLM 100", "1:100,000", 100000f, "Topographic Line Map 1:100,000 scale", CADRG),
	TL("TL", "TLM 50", "1:50,000", 50000f, TOPOLINEMAP, CADRG),
	TT("TT", "TLM 25", "1:25,000", 25000f, "Topographic Line Map 1:25,000 scale", CADRG),
	TQ("TQ", "TLM 24", "1:24,000", 24000f, "Topographic Line Map 1:24,000 scale", CADRG),
	HA("HA", "HA", VARIOUS, Various, "Harbor and Approach Charts", CADRG),
	CO("CO", "CO", VARIOUS, Various, "Coastal Charts", CADRG),
	OA("OA", "OPEAREA", VARIOUS, Various, "Naval Range Operating Area Chart", CADRG),
	CG("CG", "CG", VARIOUS, Various, CITYGRAPHICS, CADRG),
	C1("C1", "CG", "1:10,000", 10000f, CITYGRAPHICS, CADRG),
	C2("C2", "CG", "1:10,560", 10560f, CITYGRAPHICS, CADRG),
	C3("C3", "CG", "1:11,000", 11000f, CITYGRAPHICS, CADRG),
	C4("C4", "CG", "1:11,800", 11800f, CITYGRAPHICS, CADRG),
	C5("C5", "CG", "1:12,000", 12000f, CITYGRAPHICS, CADRG),
	C6("C6", "CG", "1:12,500", 12500f, CITYGRAPHICS, CADRG),
	C7("C7", "CG", "1:12,800", 12800f, CITYGRAPHICS, CADRG),
	C8("C8", "CG", "1:14,000", 14000f, CITYGRAPHICS, CADRG),
	C9("C9", "CG", "1:14,700", 14700f, CITYGRAPHICS, CADRG),
	CA("CA", "CG", "1:15,000", 15000f, CITYGRAPHICS, CADRG),
	CB("CB", "CG", "1:15,500", 15500f, CITYGRAPHICS, CADRG),
	CC("CC", "CG", "1:16,000", 16000f, CITYGRAPHICS, CADRG),
	CD("CD", "CG", "1:16,666", 16666f, CITYGRAPHICS, CADRG),
	CE("CE", "CG", "1:17,000", 17000f, CITYGRAPHICS, CADRG),
	CF("CF", "CG", "1:17,500", 17500f, CITYGRAPHICS, CADRG),
	CH("CH", "CG", "1:18,000", 18000f, CITYGRAPHICS, CADRG),
	CJ("CJ", "CG", "1:20,000", 20000f, CITYGRAPHICS, CADRG),
	CK("CK", "CG", "1:21,000", 21000f, CITYGRAPHICS, CADRG),
	CL("CL", "CG", "1:21,120", 21120f, CITYGRAPHICS, CADRG),
	CN("CN", "CG", "1:22,000", 22000f, CITYGRAPHICS, CADRG),
	CP("CP", "CG", "1:23,000", 23000f, CITYGRAPHICS, CADRG),
	CQ("CQ", "CG", "1:25,000", 25000f, CITYGRAPHICS, CADRG),
	CR("CR", "CG", "1:26,000", 26000f, CITYGRAPHICS, CADRG),
	CS("CS", "CG", "1:35,000", 35000f, CITYGRAPHICS, CADRG),
	CT("CT", "CG", "1:36,000", 36000f, CITYGRAPHICS, CADRG),
	CM("CM", "CM", VARIOUS, Various, "Combat Charts", CADRG),
	A1("A1", "CM", "1:10,000", 10000f, "Combat Graphics, 1:10,000 scale", CADRG),
	A2("A2", "CM", "1:25,000", 25000f, "Combat Graphics, 1:25,000 scale", CADRG),
	A3("A3", "CM", "1:50,000", 50000f, "Combat Graphics, 1:50,000 scale", CADRG),
	A4("A4", "CM", "1:100,000", 100000f, "Combat Graphics, 1:100,000 scale", CADRG),
	MM("MM", BLANK, VARIOUS, Various, "(Miscellaneous Maps & Charts)", CADRG),
	IM("IM", BLANK, "10m", 66666f, "Imagery, 10 meter resolution", CIB),
	I1("I1", BLANK, "10m", 66666f, "Imagery, 10 meter resolution", CIB),
	I2("I2", BLANK, "5m", 33333f, "Imagery, 5 meter resolution", CIB),
	I3("I3", BLANK, "2m", 13333.2f, "Imagery, 2 meter resolution", CIB),
	I4("I4", BLANK, "1m", 6666f, "Imagery, 1 meter resolution", CIB),
	D1("D1", BLANK, "100m", Various, "Elevation Data from DTED level 1", CDTED),
	D2("D2", BLANK, "30m", Various, "Elevation Data from DTED level 2", CDTED),
	OV("OV", "Overview", VARIOUS, Various, "Overview Image", CADRG),
	OI("OI", "Overview", VARIOUS, Various, "Overview Image", CADRG),
	LG("LG", "Legend", VARIOUS, Various, "Legend Data", CADRG),
	UK("UK", "Unknown", VARIOUS, Various, "Unknown", CADRG),
	DT("DT", "CDTED", "100M", 666666f, "Compressed DTED", CDTED),

	/**
	 * New as of MIL-STD-2411-1 Change 3
	 */
	EG("EG", "NARC", "1:11M", 11000000f, "North Atlantic Route Chart", CADRG),
	ES("ES", "SEC", "1:500,000", 500000f, "VFR Sectional", CADRG),
	ET("ET", "SEC", "1:250,000", 250000f, "VFR Sectional Insets", CADRG),
	F1("F1", "TFC-1", "1:250,000", 250000f, "Transit Flying Chart (TBD #1)", CADRG),
	F2("F2", "TFC-2", "1:250,000", 250000f, "Transit Flying Chart (TBD #2)", CADRG),
	F3("F3", "TFC-3", "1:250,000", 250000f, "Transit Flying Chart (TBD #3)", CADRG),
	F4("F4", "TFC-4", "1:250,000", 250000f, "Transit Flying Chart (TBD #4)", CADRG),
	F5("F5", "TFC-5", "1:250,000", 250000f, "Transit Flying Chart (TBD #5)", CADRG),
	I5("I5", BLANK, ".5m", 3333f, "Imagery, .5 (half) meter resolution", CIB),
	IV("IV", BLANK, VARIOUS, Various, "Imagery, > 10 meter resolution", CIB),
	JO("JO", "OPG", "1:250,000", 250000f, "Operations Planning Graphic", CADRG),
	K1("K1", "ICM", "1:8,000", 8000f, "Image City Maps", CADRG),
	K2("K2", "ICM", "1:10,000", 10000f, "Image City Maps", CADRG),
	K3("K3", "ICM", "1:10,560", 10560f, "Image City Maps", CADRG),
	K7("K7", "ICM", "1:12,500", 12500f, "Image City Maps", CADRG),
	K8("K8", "ICM", "1:12,800", 12000f, "Image City Maps", CADRG),
	KB("KB", "ICM", "1:15,000", 15000f, "Image City Maps", CADRG),
	KE("KE", "ICM", "1:16,666", 16666f, "Image City Maps", CADRG),
	KM("KM", "ICM", "1:21,120", 21120f, "Image City Maps", CADRG),
	KR("KR", "ICM", "1:25,000", 25000f, "Image City Maps", CADRG),
	KS("KS", "ICM", "1:26,000", 26000f, "Image City Maps", CADRG),
	KU("KU", "ICM", "1:36,000", 36000f, "Image City Maps", CADRG),
	L1("L1", "LFC-1", "1:500,000", 500000f, "Low Flying Chart (TBD #1)", CADRG),
	L2("L2", "LFC-2", "1:500,000", 500000f, "Low Flying Chart (TBD #2)", CADRG),
	L3("L3", "LFC-3", "1:500,000", 500000f, "Low Flying Chart (TBD #3)", CADRG),
	L4("L4", "LFC-4", "1:500,000", 500000f, "Low Flying Chart (TBD #4)", CADRG),
	L5("L5", "LFC-5", "1:500,000", 500000f, "Low Flying Chart (TBD #5)", CADRG),
	LN("LN", "LFC (Night)", "1:500,000", 500000f, "Low Flying Chart (Night) - Host Nation", CADRG),
	M1("M1", "MIM", VARIOUS, Various, "Military Installation Map (TBD #1)", CADRG),
	M2("M2", "MIM", VARIOUS, Various, "Military Installation Map (TBD #2)", CADRG),
	MH("MH", "MIM", "1:25,000", 25000f, "Military Installation Maps", CADRG),
	MI("MI", "MIM", "1:50,000", 50000f, "Military Installation Maps", CADRG),
	MJ("MJ", "MIM", "1:100,000", 100000f, "Military Installation Maps", CADRG),
	OH("OH", "VHRC", "1:1,000,000", 1000000f, "VFR Helicopter Route Chart", CADRG),
	OW("OW", "WAC", "1:1,000,000", 1000000f, "High Flying Chart - Host Nation", CADRG),
	P1("P1", BLANK, "1:25,000", 25000f, "Special Military Map - Overlay", CADRG),
	P2("P2", BLANK, "1:25,000", 25000f, "Special Military Purpose", CADRG),
	P3("P3", BLANK, "1:25,000", 25000f, "Special Military Purpose", CADRG),
	P4("P4", BLANK, "1:25,000", 25000f, "Special Military Purpose", CADRG),
	P5("P5", BLANK, "1:50,000", 50000f, "Special Military Map - Overlay", CADRG),
	P6("P6", BLANK, "1:50,000", 50000f, "Special Military Purpose", CADRG),
	P7("P7", BLANK, "1:50,000", 50000f, "Special Military Purpose", CADRG),
	P8("P8", BLANK, "1:50,000", 50000f, "Special Military Purpose", CADRG),
	P9("P9", BLANK, "1:100,000", 100000f, "Special Military Map - Overlay", CADRG),
	PA("PA", BLANK, "1:100,000", 100000f, "Special Military Purpose", CADRG),
	PB("PB", BLANK, "1:100,000", 100000f, "Special Military Purpose", CADRG),
	PC("PC", BLANK, "1:100,000", 100000f, "Special Military Purpose", CADRG),
	PD("PD", BLANK, "1:250,000", 250000f, "Special Military Map - Overlay", CADRG),
	PE("PE", BLANK, "1:250,000", 250000f, "Special Military Purpose", CADRG),
	PF("PF", BLANK, "1:250,000", 250000f, "Special Military Purpose", CADRG),
	PG("PG", BLANK, "1:250,000", 250000f, "Special Military Purpose", CADRG),
	PH("PH", BLANK, "1:500,000", 500000f, "Special Military Map - Overlay", CADRG),
	PI("PI", BLANK, "1:500,000", 500000f, "Special Military Purpose", CADRG),
	PJ("PJ", BLANK, "1:500,000", 500000f, "Special Military Purpose", CADRG),
	PK("PK", BLANK, "1:500,000", 500000f, "Special Military Purpose", CADRG),
	PL("PL", BLANK, "1:1,000,000", 1000000f, "Special Military Map - Overlay", CADRG),
	PM("PM", BLANK, "1:1,000,000", 1000000f, "Special Military Purpose", CADRG),
	PN("PN", BLANK, "1:1,000,000", 1000000f, "Special Military Purpose", CADRG),
	PO("PO", BLANK, "1:1,000,000", 1000000f, "Special Military Purpose", CADRG),
	PP("PP", BLANK, "1:2,000,000", 2000000f, "Special Military Map - Overlay", CADRG),
	PQ("PQ", BLANK, "1:2,000,000", 2000000f, "Special Military Purpose", CADRG),
	PR("PR", BLANK, "1:2,000,000", 2000000f, "Special Military Purpose", CADRG),
	PS("PS", BLANK, "1:5,000,000", 5000000f, "Special Military Map - Overlay", CADRG),
	PT("PT", BLANK, "1:5,000,000", 5000000f, "Special Military Purpose", CADRG),
	PU("PU", BLANK, "1:5,000,000", 5000000f, "Special Military Purpose", CADRG),
	PV("PV", BLANK, "1:5,000,000", 5000000f, "Special Military Purpose", CADRG),
	R1("R1", BLANK, "1:50,000", 50000f, "Range Charts", CADRG),
	R2("R2", BLANK, "1:100,000", 100000f, "Range Charts", CADRG),
	R3("R3", BLANK, "1:250,000", 250000f, "Range Charts", CADRG),
	R4("R4", BLANK, "1:500,000", 500000f, "Range Charts", CADRG),
	R5("R5", BLANK, "1:1,000,000", 1000000f, "Range Charts", CADRG),
	RC("RC", "RGS-100", "1:10,000", 10000f, "Russian General Staff Maps", CADRG),
	RL("RL", "RGS-50", "1:50,000", 50000f, "Russian General Staff Maps", CADRG),
	RR("RR", "RGS-200", "1:200,000", 200000f, "Russian General Staff Maps", CADRG),
	RV("RV", "Riverine", "1:50,000", 50000f, "Riverine Map 1:50,000 scale", CADRG),
	TN("TN", "TFC (Night)", "1:250,000", 250000f, "Transit Flying Chart (Night) - Host nation", CADRG),
	TR("TR", "TLM 100", "1:200,000", 200000f, "Topographic Line Map 1:200,000 scale", CADRG),
	UL("UL", "TLM50-Other", "1:50,000", 50000f, "Topographic Line Map (other 1:50,000 scale)", CADRG),
	V1("V1", "HRC Inset", "1:50,000", 50000f, "Helicopter Route Chart Inset", CADRG),
	V2("V2", "HRC Inset", "1:62,500", 62500f, "Helicopter Route Chart Inset", CADRG),
	V3("V3", "HRC Inset", "1:90,000", 90000f, "Helicopter Route Chart Inset", CADRG),
	V4("V4", "HRC Inset", "1:250,000", 250000f, "Helicopter Route Chart Inset", CADRG),
	VH("VH", "HRC", "1:125,000", 125000f, "Helicopter Route Chart", CADRG),
	VN("VN", "VNC", "1:500,000", 500000f, "Visual Navigation Charts", CADRG),
	VT("VT", "VTAC", "1:250,000", 250000f, "VFR Terminal Area Chart", CADRG),
	WA("WA", BLANK, "1:250,000", 250000f, "IFR Enroute Low", CADRG),
	WB("WB", BLANK, "1:500,000", 500000f, "IFR Enroute Low", CADRG),
	WC("WC", BLANK, "1:750,000", 750000f, "IFR Enroute Low", CADRG),
	WD("WD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute Low", CADRG),
	WE("WE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute Low", CADRG),
	WF("WF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute Low", CADRG),
	WG("WG", BLANK, "1:2,500,000", 2500000f, "IFR Enroute Low", CADRG),
	WH("WH", BLANK, "1:3,000,000", 3000000f, "IFR Enroute Low", CADRG),
	WI("WI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute Low", CADRG),
	WK("WK", BLANK, "1:4,500,000", 4500000f, "IFR Enroute Low", CADRG),
	XD("XD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute High", CADRG),
	XE("XE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute High", CADRG),
	XF("XF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute High", CADRG),
	XG("XG", BLANK, "1:2,500,000", 2500000f, "IFR Enroute High", CADRG),
	XH("XH", BLANK, "1:3,000,000", 3000000f, "IFR Enroute High", CADRG),
	XI("XI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute High", CADRG),
	XJ("XJ", BLANK, "1:3,000,000", 3000000f, "IFR Enroute High", CADRG),
	XK("XK", BLANK, "1:4,500,000", 4500000f, "IFR Enroute High", CADRG),
	Y9("Y9", BLANK, "1:16,500,000", 16500000f, "IFR Enroute Area", CADRG),
	YA("YA", BLANK, "1:250,000", 250000f, "IFR Enroute Area", CADRG),
	YB("YB", BLANK, "1:500,000", 500000f, "IFR Enroute Area", CADRG),
	YC("YC", BLANK, "1:750,000", 750000f, "IFR Enroute Area", CADRG),
	YD("YD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute Area", CADRG),
	YE("YE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute Area", CADRG),
	YF("YF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute Area", CADRG),
	YI("YI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute Area", CADRG),
	YJ("YJ", BLANK, "1:4,000,000", 4000000f, "IFR Enroute Area", CADRG),
	YZ("YZ", BLANK, "1:12,000,000", 12000000f, "IFR Enroute Area", CADRG),
	Z8("Z8", BLANK, "1:16,000,000", 16000000f, "IFR Enroute High/Low", CADRG),
	ZA("ZA", BLANK, "1:250,000", 250000f, "IFR Enroute High/Low", CADRG),
	ZB("ZB", BLANK, "1:500,000", 500000f, "IFR Enroute High/Low", CADRG),
	ZC("ZC", BLANK, "1:750,000", 750000f, "IFR Enroute High/Low", CADRG),
	ZD("ZD", BLANK, "1:1,000,000", 1000000f, "IFR Enroute High/Low", CADRG),
	ZE("ZE", BLANK, "1:1,500,000", 1500000f, "IFR Enroute High/Low", CADRG),
	ZF("ZF", BLANK, "1:2,000,000", 2000000f, "IFR Enroute High/Low", CADRG),
	ZG("ZG", BLANK, "1:2,500,000", 2500000f, "IFR Enroute High/Low", CADRG),
	ZH("ZH", BLANK, "1:3,000,000", 3000000f, "IFR Enroute High/Low", CADRG),
	ZI("ZI", BLANK, "1:3,500,000", 3500000f, "IFR Enroute High/Low", CADRG),
	ZJ("ZJ", BLANK, "1:4,000,000", 4000000f, "IFR Enroute High/Low", CADRG),
	ZK("ZK", BLANK, "1:4,500,000", 4500000f, "IFR Enroute High/Low", CADRG),
	ZT("ZT", BLANK, "1:9,000,000", 9000000f, "IFR Enroute High/Low", CADRG),
	ZV("ZV", BLANK, "1:10,000,000", 10000000f, "IFR Enroute High/Low", CADRG),
	ZZ("ZZ", BLANK, "1:12,000,000", 12000000f, "IFR Enroute High/Low", CADRG);

	/** The two-letter code for a particular map type. */
	public final String seriesCode;
	/** The three-letter common abbreviation for the map type. */
	public final String abbr;
	/** A String scale representation of the map type. */
	public final String scaleString;
	/** The float number representation of the map type - 1:XXX . */
	public final float scale;
	/** A Descriptive name for the map type. */
	public final String name;
	/** The category of the map type - CADRG/CIB/DTED. */
	public final String dataType;

	/**
	 * Create a RpfProductInfo object.
	 * 
	 * @param sc
	 *            the two-letter series code.
	 * @param a
	 *            the three letter acroynm.
	 * @param ss
	 *            the scale string.
	 * @param s
	 *            the display scale of the map.
	 * @param n
	 *            descriptive name of the map.
	 * @param dt
	 *            data type - CADRG-CIB-DTED.
	 */
	private RpfProductInfo(String sc, String a, String ss, float s, String n, String dt) {
		seriesCode = sc;
		abbr = a;
		scaleString = ss;
		scale = s;
		name = n;
		dataType = dt;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("RPF Product: ").append(name).append("\n");
		sb.append("  Series Code: ").append(seriesCode).append("\n");
		sb.append("  Abbreviation: ").append(abbr).append("\n");
		sb.append("  Scale: ").append(scaleString);
		if (scale == RpfConstants.Various) {
			sb.append(" (Various)\n");
		} else {
			sb.append(" (").append(scale).append(")\n");
		}
		sb.append("  Data Type: ").append(dataType).append("\n");
		return sb.toString();
	}

	/**
	 * Returns the RpfProductInfo that has the given two-letter series code. If
	 * the code passed in is not recognized by the catalog, the UNKNOWN
	 * RpfProductInfo is returned.
	 * 
	 * @param seriesCode
	 *            needs to be the two letter code, in uppercase.
	 */
	public static RpfProductInfo get(String seriesCode) {

		if (seriesCode == null) {
			return UK;
		}

		for (RpfProductInfo rpi : RpfProductInfo.values()) {
			if (rpi.seriesCode.equalsIgnoreCase(seriesCode)) {
				return rpi;
			}
		}

		return UK;
	}

	/**
	 * The main function prints out the RPF catalog, describing all the
	 * different map types handled by the RPF package.
	 */
	public static void main(String[] argv) {
		System.out.println("RPF Catalog:\n\n");
		for (RpfProductInfo rpi : RpfProductInfo.values()) {
			System.out.println("----------------------");
			System.out.println(rpi);
		}
	}
}