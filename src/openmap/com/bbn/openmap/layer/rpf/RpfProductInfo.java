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
       CATALOG = new java.util.Hashtable<String, RpfProductInfo>(60);
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
    * The main function prints out the RPF catalog, describing all the different
    * map types handled by the RPF package.
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