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
 * The meat of this code is based on source code provided by The MITRE
 * Corporation, through the browse application source code.  Many
 * thanks to Nancy Markuson who provided BBN with the software, and to
 * Theron Tock, who wrote the software, and Daniel Scholten, who
 * revised it - (c) 1994 The MITRE Corporation for those parts, and
 * used/distributed with permission.  The RPF TOC reading mechanism is
 * the contributed part.
 */
package com.bbn.openmap.layer.rpf;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Contains basic information about the different map and imagery types
 * supported by the RPF format. This information is based on the specification
 * released by NIMA and contains the conventions they have listed in the RPF
 * specification.
 */
public class RpfProductInfo {

    /** The two-letter code for a particular map type. */
    public String seriesCode;
    /** The three-letter common abbreviation for the map type. */
    public String abbr;
    /** A String scale representation of the map type. */
    public String scaleString;
    /** The float number representation of the map type - 1:XXX . */
    public float scale;
    /** A Descriptive name for the map type. */
    public String name;
    /** The category of the map type - CADRG/CIB/DTED. */
    public String dataType;

    /**
     * A hashtable that stores all the known map types, with the two-letter code
     * as the key for retrieval.
     */
    private static Hashtable<String, RpfProductInfo> CATALOG;

    static {
        CATALOG = new java.util.Hashtable<String, RpfProductInfo>(190);
        CATALOG.put(RpfConstants.GN.seriesCode, RpfConstants.GN);
        CATALOG.put(RpfConstants.JN.seriesCode, RpfConstants.JN);
        CATALOG.put(RpfConstants.ON.seriesCode, RpfConstants.ON);
        CATALOG.put(RpfConstants.TP.seriesCode, RpfConstants.TP);
        CATALOG.put(RpfConstants.LF.seriesCode, RpfConstants.LF);
        CATALOG.put(RpfConstants.JG.seriesCode, RpfConstants.JG);
        CATALOG.put(RpfConstants.JA.seriesCode, RpfConstants.JA);
        CATALOG.put(RpfConstants.JR.seriesCode, RpfConstants.JR);
        CATALOG.put(RpfConstants.TF.seriesCode, RpfConstants.TF);
        CATALOG.put(RpfConstants.AT.seriesCode, RpfConstants.AT);
        CATALOG.put(RpfConstants.TC.seriesCode, RpfConstants.TC);
        CATALOG.put(RpfConstants.TL.seriesCode, RpfConstants.TL);
        CATALOG.put(RpfConstants.TT.seriesCode, RpfConstants.TT);
        CATALOG.put(RpfConstants.TQ.seriesCode, RpfConstants.TQ);
        CATALOG.put(RpfConstants.HA.seriesCode, RpfConstants.HA);
        CATALOG.put(RpfConstants.CO.seriesCode, RpfConstants.CO);
        CATALOG.put(RpfConstants.OA.seriesCode, RpfConstants.OA);
        CATALOG.put(RpfConstants.CG.seriesCode, RpfConstants.CG);
        CATALOG.put(RpfConstants.C1.seriesCode, RpfConstants.C1);
        CATALOG.put(RpfConstants.C2.seriesCode, RpfConstants.C2);
        CATALOG.put(RpfConstants.C3.seriesCode, RpfConstants.C3);
        CATALOG.put(RpfConstants.C4.seriesCode, RpfConstants.C4);
        CATALOG.put(RpfConstants.C5.seriesCode, RpfConstants.C5);
        CATALOG.put(RpfConstants.C6.seriesCode, RpfConstants.C6);
        CATALOG.put(RpfConstants.C7.seriesCode, RpfConstants.C7);
        CATALOG.put(RpfConstants.C8.seriesCode, RpfConstants.C8);
        CATALOG.put(RpfConstants.C9.seriesCode, RpfConstants.C9);
        CATALOG.put(RpfConstants.CA.seriesCode, RpfConstants.CA);
        CATALOG.put(RpfConstants.CB.seriesCode, RpfConstants.CB);
        CATALOG.put(RpfConstants.CC.seriesCode, RpfConstants.CC);
        CATALOG.put(RpfConstants.CD.seriesCode, RpfConstants.CD);
        CATALOG.put(RpfConstants.CE.seriesCode, RpfConstants.CE);
        CATALOG.put(RpfConstants.CF.seriesCode, RpfConstants.CF);
        CATALOG.put(RpfConstants.CH.seriesCode, RpfConstants.CH);
        CATALOG.put(RpfConstants.CJ.seriesCode, RpfConstants.CJ);
        CATALOG.put(RpfConstants.CK.seriesCode, RpfConstants.CK);
        CATALOG.put(RpfConstants.CL.seriesCode, RpfConstants.CL);
        CATALOG.put(RpfConstants.CN.seriesCode, RpfConstants.CN);
        CATALOG.put(RpfConstants.CP.seriesCode, RpfConstants.CP);
        CATALOG.put(RpfConstants.CQ.seriesCode, RpfConstants.CQ);
        CATALOG.put(RpfConstants.CR.seriesCode, RpfConstants.CR);
        CATALOG.put(RpfConstants.CS.seriesCode, RpfConstants.CS);
        CATALOG.put(RpfConstants.CT.seriesCode, RpfConstants.CT);
        CATALOG.put(RpfConstants.CM.seriesCode, RpfConstants.CM);
        CATALOG.put(RpfConstants.A1.seriesCode, RpfConstants.A1);
        CATALOG.put(RpfConstants.A2.seriesCode, RpfConstants.A2);
        CATALOG.put(RpfConstants.A3.seriesCode, RpfConstants.A3);
        CATALOG.put(RpfConstants.A4.seriesCode, RpfConstants.A4);
        CATALOG.put(RpfConstants.MM.seriesCode, RpfConstants.MM);
        CATALOG.put(RpfConstants.IM.seriesCode, RpfConstants.IM);
        CATALOG.put(RpfConstants.I1.seriesCode, RpfConstants.I1);
        CATALOG.put(RpfConstants.I2.seriesCode, RpfConstants.I2);
        CATALOG.put(RpfConstants.I3.seriesCode, RpfConstants.I3);
        CATALOG.put(RpfConstants.I4.seriesCode, RpfConstants.I4);
        CATALOG.put(RpfConstants.D1.seriesCode, RpfConstants.D1);
        CATALOG.put(RpfConstants.D2.seriesCode, RpfConstants.D2);
        CATALOG.put(RpfConstants.OV.seriesCode, RpfConstants.OV);
        CATALOG.put(RpfConstants.OI.seriesCode, RpfConstants.OI);
        CATALOG.put(RpfConstants.LG.seriesCode, RpfConstants.LG);
        CATALOG.put(RpfConstants.UK.seriesCode, RpfConstants.UK);
        CATALOG.put(RpfConstants.DT.seriesCode, RpfConstants.DT);
        CATALOG.put(RpfConstants.EG.seriesCode, RpfConstants.EG);
        CATALOG.put(RpfConstants.ES.seriesCode, RpfConstants.ES);
        CATALOG.put(RpfConstants.ET.seriesCode, RpfConstants.ET);
        CATALOG.put(RpfConstants.F1.seriesCode, RpfConstants.F1);
        CATALOG.put(RpfConstants.F2.seriesCode, RpfConstants.F2);
        CATALOG.put(RpfConstants.F3.seriesCode, RpfConstants.F3);
        CATALOG.put(RpfConstants.F4.seriesCode, RpfConstants.F4);
        CATALOG.put(RpfConstants.F5.seriesCode, RpfConstants.F5);
        CATALOG.put(RpfConstants.I5.seriesCode, RpfConstants.I5);
        CATALOG.put(RpfConstants.IV.seriesCode, RpfConstants.IV);
        CATALOG.put(RpfConstants.JO.seriesCode, RpfConstants.JO);
        CATALOG.put(RpfConstants.K1.seriesCode, RpfConstants.K1);
        CATALOG.put(RpfConstants.K2.seriesCode, RpfConstants.K2);
        CATALOG.put(RpfConstants.K3.seriesCode, RpfConstants.K3);
        CATALOG.put(RpfConstants.K7.seriesCode, RpfConstants.K7);
        CATALOG.put(RpfConstants.K8.seriesCode, RpfConstants.K8);
        CATALOG.put(RpfConstants.KB.seriesCode, RpfConstants.KB);
        CATALOG.put(RpfConstants.KE.seriesCode, RpfConstants.KE);
        CATALOG.put(RpfConstants.KM.seriesCode, RpfConstants.KM);
        CATALOG.put(RpfConstants.KR.seriesCode, RpfConstants.KR);
        CATALOG.put(RpfConstants.KS.seriesCode, RpfConstants.KS);
        CATALOG.put(RpfConstants.KU.seriesCode, RpfConstants.KU);
        CATALOG.put(RpfConstants.L1.seriesCode, RpfConstants.L1);
        CATALOG.put(RpfConstants.L2.seriesCode, RpfConstants.L2);
        CATALOG.put(RpfConstants.L3.seriesCode, RpfConstants.L3);
        CATALOG.put(RpfConstants.L4.seriesCode, RpfConstants.L4);
        CATALOG.put(RpfConstants.L5.seriesCode, RpfConstants.L5);
        CATALOG.put(RpfConstants.LN.seriesCode, RpfConstants.LN);
        CATALOG.put(RpfConstants.M1.seriesCode, RpfConstants.M1);
        CATALOG.put(RpfConstants.M2.seriesCode, RpfConstants.M2);
        CATALOG.put(RpfConstants.MH.seriesCode, RpfConstants.MH);
        CATALOG.put(RpfConstants.MI.seriesCode, RpfConstants.MI);
        CATALOG.put(RpfConstants.MJ.seriesCode, RpfConstants.MJ);
        CATALOG.put(RpfConstants.OH.seriesCode, RpfConstants.OH);
        CATALOG.put(RpfConstants.OW.seriesCode, RpfConstants.OW);
        CATALOG.put(RpfConstants.P1.seriesCode, RpfConstants.P1);
        CATALOG.put(RpfConstants.P2.seriesCode, RpfConstants.P2);
        CATALOG.put(RpfConstants.P3.seriesCode, RpfConstants.P3);
        CATALOG.put(RpfConstants.P4.seriesCode, RpfConstants.P4);
        CATALOG.put(RpfConstants.P5.seriesCode, RpfConstants.P5);
        CATALOG.put(RpfConstants.P6.seriesCode, RpfConstants.P6);
        CATALOG.put(RpfConstants.P7.seriesCode, RpfConstants.P7);
        CATALOG.put(RpfConstants.P8.seriesCode, RpfConstants.P8);
        CATALOG.put(RpfConstants.P9.seriesCode, RpfConstants.P9);
        CATALOG.put(RpfConstants.PA.seriesCode, RpfConstants.PA);
        CATALOG.put(RpfConstants.PB.seriesCode, RpfConstants.PB);
        CATALOG.put(RpfConstants.PC.seriesCode, RpfConstants.PC);
        CATALOG.put(RpfConstants.PD.seriesCode, RpfConstants.PD);
        CATALOG.put(RpfConstants.PE.seriesCode, RpfConstants.PE);
        CATALOG.put(RpfConstants.PF.seriesCode, RpfConstants.PF);
        CATALOG.put(RpfConstants.PG.seriesCode, RpfConstants.PG);
        CATALOG.put(RpfConstants.PH.seriesCode, RpfConstants.PH);
        CATALOG.put(RpfConstants.PI.seriesCode, RpfConstants.PI);
        CATALOG.put(RpfConstants.PJ.seriesCode, RpfConstants.PJ);
        CATALOG.put(RpfConstants.PK.seriesCode, RpfConstants.PK);
        CATALOG.put(RpfConstants.PL.seriesCode, RpfConstants.PL);
        CATALOG.put(RpfConstants.PM.seriesCode, RpfConstants.PM);
        CATALOG.put(RpfConstants.PN.seriesCode, RpfConstants.PN);
        CATALOG.put(RpfConstants.PO.seriesCode, RpfConstants.PO);
        CATALOG.put(RpfConstants.PP.seriesCode, RpfConstants.PP);
        CATALOG.put(RpfConstants.PQ.seriesCode, RpfConstants.PQ);
        CATALOG.put(RpfConstants.PR.seriesCode, RpfConstants.PR);
        CATALOG.put(RpfConstants.PS.seriesCode, RpfConstants.PS);
        CATALOG.put(RpfConstants.PT.seriesCode, RpfConstants.PT);
        CATALOG.put(RpfConstants.PU.seriesCode, RpfConstants.PU);
        CATALOG.put(RpfConstants.PV.seriesCode, RpfConstants.PV);
        CATALOG.put(RpfConstants.R1.seriesCode, RpfConstants.R1);
        CATALOG.put(RpfConstants.R2.seriesCode, RpfConstants.R2);
        CATALOG.put(RpfConstants.R3.seriesCode, RpfConstants.R3);
        CATALOG.put(RpfConstants.R4.seriesCode, RpfConstants.R4);
        CATALOG.put(RpfConstants.R5.seriesCode, RpfConstants.R5);
        CATALOG.put(RpfConstants.RC.seriesCode, RpfConstants.RC);
        CATALOG.put(RpfConstants.RL.seriesCode, RpfConstants.RL);
        CATALOG.put(RpfConstants.RR.seriesCode, RpfConstants.RR);
        CATALOG.put(RpfConstants.RV.seriesCode, RpfConstants.RV);
        CATALOG.put(RpfConstants.TN.seriesCode, RpfConstants.TN);
        CATALOG.put(RpfConstants.TR.seriesCode, RpfConstants.TR);
        CATALOG.put(RpfConstants.UL.seriesCode, RpfConstants.UL);
        CATALOG.put(RpfConstants.V1.seriesCode, RpfConstants.V1);
        CATALOG.put(RpfConstants.V2.seriesCode, RpfConstants.V2);
        CATALOG.put(RpfConstants.V3.seriesCode, RpfConstants.V3);
        CATALOG.put(RpfConstants.V4.seriesCode, RpfConstants.V4);
        CATALOG.put(RpfConstants.VH.seriesCode, RpfConstants.VH);
        CATALOG.put(RpfConstants.VN.seriesCode, RpfConstants.VN);
        CATALOG.put(RpfConstants.VT.seriesCode, RpfConstants.VT);
        CATALOG.put(RpfConstants.WA.seriesCode, RpfConstants.WA);
        CATALOG.put(RpfConstants.WB.seriesCode, RpfConstants.WB);
        CATALOG.put(RpfConstants.WC.seriesCode, RpfConstants.WC);
        CATALOG.put(RpfConstants.WD.seriesCode, RpfConstants.WD);
        CATALOG.put(RpfConstants.WE.seriesCode, RpfConstants.WE);
        CATALOG.put(RpfConstants.WF.seriesCode, RpfConstants.WF);
        CATALOG.put(RpfConstants.WG.seriesCode, RpfConstants.WG);
        CATALOG.put(RpfConstants.WH.seriesCode, RpfConstants.WH);
        CATALOG.put(RpfConstants.WI.seriesCode, RpfConstants.WI);
        CATALOG.put(RpfConstants.WK.seriesCode, RpfConstants.WK);
        CATALOG.put(RpfConstants.XD.seriesCode, RpfConstants.XD);
        CATALOG.put(RpfConstants.XE.seriesCode, RpfConstants.XE);
        CATALOG.put(RpfConstants.XF.seriesCode, RpfConstants.XF);
        CATALOG.put(RpfConstants.XG.seriesCode, RpfConstants.XG);
        CATALOG.put(RpfConstants.XH.seriesCode, RpfConstants.XH);
        CATALOG.put(RpfConstants.XI.seriesCode, RpfConstants.XI);
        CATALOG.put(RpfConstants.XJ.seriesCode, RpfConstants.XJ);
        CATALOG.put(RpfConstants.XK.seriesCode, RpfConstants.XK);
        CATALOG.put(RpfConstants.Y9.seriesCode, RpfConstants.Y9);
        CATALOG.put(RpfConstants.YA.seriesCode, RpfConstants.YA);
        CATALOG.put(RpfConstants.YB.seriesCode, RpfConstants.YB);
        CATALOG.put(RpfConstants.YC.seriesCode, RpfConstants.YC);
        CATALOG.put(RpfConstants.YD.seriesCode, RpfConstants.YD);
        CATALOG.put(RpfConstants.YE.seriesCode, RpfConstants.YE);
        CATALOG.put(RpfConstants.YF.seriesCode, RpfConstants.YF);
        CATALOG.put(RpfConstants.YI.seriesCode, RpfConstants.YI);
        CATALOG.put(RpfConstants.YJ.seriesCode, RpfConstants.YJ);
        CATALOG.put(RpfConstants.YZ.seriesCode, RpfConstants.YZ);
        CATALOG.put(RpfConstants.Z8.seriesCode, RpfConstants.Z8);
        CATALOG.put(RpfConstants.ZA.seriesCode, RpfConstants.ZA);
        CATALOG.put(RpfConstants.ZB.seriesCode, RpfConstants.ZB);
        CATALOG.put(RpfConstants.ZC.seriesCode, RpfConstants.ZC);
        CATALOG.put(RpfConstants.ZD.seriesCode, RpfConstants.ZD);
        CATALOG.put(RpfConstants.ZE.seriesCode, RpfConstants.ZE);
        CATALOG.put(RpfConstants.ZF.seriesCode, RpfConstants.ZF);
        CATALOG.put(RpfConstants.ZG.seriesCode, RpfConstants.ZG);
        CATALOG.put(RpfConstants.ZH.seriesCode, RpfConstants.ZH);
        CATALOG.put(RpfConstants.ZI.seriesCode, RpfConstants.ZI);
        CATALOG.put(RpfConstants.ZJ.seriesCode, RpfConstants.ZJ);
        CATALOG.put(RpfConstants.ZK.seriesCode, RpfConstants.ZK);
        CATALOG.put(RpfConstants.ZT.seriesCode, RpfConstants.ZT);
        CATALOG.put(RpfConstants.ZV.seriesCode, RpfConstants.ZV);
        CATALOG.put(RpfConstants.ZZ.seriesCode, RpfConstants.ZZ);
    }

    public RpfProductInfo() {
        seriesCode = RpfConstants.BLANK;
        abbr = RpfConstants.BLANK;
        scaleString = RpfConstants.BLANK;
        scale = RpfConstants.Various;
        name = RpfConstants.BLANK;
        dataType = RpfConstants.BLANK;
    }

    /**
     * Create a RpfProductInfo object.
     * 
     * @param sc the two-letter series code.
     * @param a the three letter acroynm.
     * @param ss the scale string.
     * @param s the display scale of the map.
     * @param n descriptive name of the map.
     * @param dt data type - CADRG-CIB-DTED.
     */
    public RpfProductInfo(String sc, String a, String ss, float s, String n, String dt) {
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
     * @param seriesCode needs to be the two letter code, in uppercase.
     */
    public static RpfProductInfo get(String seriesCode) {
        Hashtable<String, RpfProductInfo> cat = getCatalog();
        if (seriesCode == null)
            return RpfConstants.UK;
        RpfProductInfo rpi = cat.get(seriesCode);
        if (rpi == null)
            return RpfConstants.UK;
        return rpi;
    }

    /**
     * Returns the catalog of supported chart types. If it doesn't exist yet
     * (It's held as a static hashtable) it is created and loaded.
     * 
     * @return Hashtable of product information.
     */
    public static Hashtable<String, RpfProductInfo> getCatalog() {
        return CATALOG;
    }

    /**
     * The main function prints out the RPF catalog, describing all the
     * different map types handled by the RPF package.
     */
    public static void main(String[] argv) {
        Enumeration<RpfProductInfo> it = RpfProductInfo.getCatalog().elements();
        System.out.println("RPF Catalog:\n\n");
        while (it.hasMoreElements()) {
            System.out.println("----------------------");
            System.out.println(it.nextElement());
        }
    }
}