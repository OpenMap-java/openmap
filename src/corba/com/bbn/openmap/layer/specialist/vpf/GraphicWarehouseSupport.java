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
// $Source:
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/vpf/GraphicWarehouseSupport.java,v
// $
// $RCSfile: GraphicWarehouseSupport.java,v $
// $Revision: 1.8 $
// $Date: 2009/02/23 22:37:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.vpf;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.bbn.openmap.corba.CSpecialist.Comp;
import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;
import com.bbn.openmap.layer.specialist.GraphicList;
import com.bbn.openmap.layer.specialist.SColor;
import com.bbn.openmap.layer.specialist.SPoly;
import com.bbn.openmap.layer.specialist.SText;
import com.bbn.openmap.layer.vpf.CoordFloatString;
import com.bbn.openmap.layer.vpf.LibrarySelectionTable;
import com.bbn.openmap.layer.vpf.VPFGraphicWarehouse;
import com.bbn.openmap.proj.coords.LatLonPoint;

public abstract class GraphicWarehouseSupport
      implements VPFGraphicWarehouse {

   /** HACK around antarctica display problem. */
   final transient protected static float antarcticaThreshold = -89.9f;

   /** hang on to the graphics that we build */
   protected GraphicList graphics;

   /** remember if we draw edge features */
   private boolean drawEdgeFeatures;
   /** remember if we draw text features */
   private boolean drawTextFeatures;
   /** remember if we draw area features */
   private boolean drawAreaFeatures;
   /** remember if we draw point features */
   private boolean drawPointFeatures;

   private static SColor cmap[] = null;
   static {
       cmap = new SColor[8];
       // cmap[0] = ns(255, 0,0);
       // cmap[1] = ns(0, 255, 0);
       // cmap[2] = ns(0, 0, 255);
       cmap[0] = ns(205, 192, 176);
       cmap[1] = ns(255, 192, 203);
       cmap[2] = ns(221, 160, 221);
       cmap[3] = ns(162, 205, 90);
       cmap[4] = ns(255, 218, 185);
       cmap[5] = ns(255, 160, 122);
       cmap[6] = ns(205, 201, 165);
       cmap[7] = ns(216, 191, 216);
       // cmap[8] = ns(255, 165, 0);
       // cmap[9] = ns( 0, 255, 0);
       // cmap[10] = ns( 0, 255, 255);
    }
   
   /**
     *  
     */
   public GraphicWarehouseSupport() {
      graphics = new GraphicList();
   }

   /**
    * Lets the warehouse know that a different CoverageAttributeTable will be
    * using it. Default action is to do nothing.
    */
   public void resetForCAT() {
   }

   /**
    * Return the GUI for certain warehouse attributes. By default, return the
    * GUI for the DrawingAttributes object being used for rendering attributes
    * of the graphics.
    * 
    * @param lst LibrarySelectionTable to use to get information about the data,
    *        if needed. Not needed here.
    */
   public Component getGUI(LibrarySelectionTable lst) {
      return null;
   }

   /**
    * set if we draw edge features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setEdgeFeatures(boolean newvalue) {
      drawEdgeFeatures = newvalue;
   }

   /**
    * Return true if we may draw some edge features.
    */
   public boolean drawEdgeFeatures() {
      return drawEdgeFeatures;
   }

   /**
    * set if we draw text features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setTextFeatures(boolean newvalue) {
      drawTextFeatures = newvalue;
   }

   /**
    * Return true if we may draw some text features.
    */
   public boolean drawTextFeatures() {
      return drawTextFeatures;
   }

   /**
    * set if we draw area features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setAreaFeatures(boolean newvalue) {
      com.bbn.openmap.util.Debug.message("vpfspecialist", "Setting area features to " + newvalue);
      drawAreaFeatures = newvalue;
   }

   /**
    * Return true if we may draw some area features.
    */
   public boolean drawAreaFeatures() {
      return drawAreaFeatures;
   }

   /**
    * set if we draw point features
    * 
    * @param newvalue <code>true</code> for drawing, false otherwise
    */
   public void setPointFeatures(boolean newvalue) {
      drawPointFeatures = newvalue;
   }

   /**
    * Return true if we may draw some point features.
    */
   public boolean drawPointFeatures() {
      return drawPointFeatures;
   }

   /**
     *  
     */
   protected SPoly createAreaSPoly(List jpts, int totalSize, LatLonPoint ll1, LatLonPoint ll2, float dpplat, float dpplon) {
      int size = jpts.size();
      List ipts = new ArrayList(size * 2);
      // HACK maybe should fold this loop into the loop inside
      // generatePolyPts()
      for (int j = 0; j < size; j++) {
         CoordFloatString cfs = (CoordFloatString) jpts.get(j);
         int cfscnt = cfs.tcount;
         int cfssz = cfs.tsize;
         double cfsvals[] = cfs.vals;
         if (cfscnt > 0) { // normal
            for (int i = 0; i < cfscnt; i++) {
               ipts.add(new LLPoint((float) cfsvals[i * cfssz + 1], (float) cfsvals[i * cfssz]));
            }
         } else { // reverse
            cfscnt *= -1; // normalize
            for (int i = cfscnt - 1; i >= 0; i--) {
               ipts.add(new LLPoint((float) cfsvals[i * cfssz + 1], (float) cfsvals[i * cfssz]));
            }
         }
      }

      LLPoint pts[] =
            generatePolyPts(ipts, ll1.getLatitude(), ll2.getLatitude(), ll2.getLongitude(), ll1.getLongitude(), dpplat, dpplon);
      if (pts == null) {
         // Debug.message("dcwSpecialist.clipping",
         // "Completely eliminated poly");
         return null;
      }

      SPoly py = new SPoly(pts, LineType.LT_Straight);
      return py;
   }

   /**
     *  
     */
   public LLPoint[] generatePolyPts(List ipts, float north, float south, float east, float west, float dpplat, float dpplon) {
      int coordcount = ipts.size();

      /*
       * Let me explain. We might be inserting some extra points to work around
       * a problem displaying Antarctica in cylindrical projections. So the
       * initial capacity of the Vector is set to the number of points. The
       * increment value is set to 5 since that's how many points we'll add if
       * we hit the antarctica thing. That way we're not allocating a huge new
       * Vector when we don't really need to.
       */
      Vector vPts = new Vector(coordcount, 5);

      // HACK: we will rewrite the data for the Antarctica polygon
      // so that
      // it will display "correctly" in the cylindrical projections.
      // only check if bottom edge of screen below a certain
      // latitude
      boolean weaseledOurWayAroundAntarcticAnomaly = (south >= -62f);

      LLPoint prevPt = null;

      for (int i = 0; i < coordcount; i++) {

         LLPoint pt = (LLPoint) ipts.get(i);
         float lllat = pt.lat;

         if ((prevPt != null) && (i != (coordcount - 1)) && (Math.abs(prevPt.lat - pt.lat) < dpplat)
               && (Math.abs(prevPt.lon - pt.lon) < dpplon)) {

            continue;

         }

         vPts.add(pt);
         prevPt = pt;

         if (!weaseledOurWayAroundAntarcticAnomaly && (lllat < antarcticaThreshold)) {
            weaseledOurWayAroundAntarcticAnomaly = true;
            System.out.println("AreaTable.generateSPoly(): Antarctica!");
            // another HACK: we're assuming data is going from
            // west to east,
            // so we wrap the other way
            vPts.add(new LLPoint(-89.99f, 179.99f));
            vPts.add(new LLPoint(-89.99f, 90f));
            vPts.add(new LLPoint(-89.99f, 0f));
            vPts.add(new LLPoint(-89.99f, -90f));
            vPts.add(new LLPoint(-89.99f, -179.99f));

            prevPt = (LLPoint) vPts.lastElement();

            // advance to western hemisphere where we
            // pick up the real data again
            while (((LLPoint) ipts.get(i)).lon > 0) {
               ++i;
            }
         }
      }

      int nPts = vPts.size();

      if (nPts == 0) {

         return null;

      } else {

         LLPoint pts[] = new LLPoint[nPts];
         vPts.copyInto(pts);
         return pts;

      }
   }

   SColor edgeColors[] = {
      new SColor((short) 65535, (short) 0, (short) 0), // red
      new SColor((short) 0, (short) 65535, (short) 0), // green
      new SColor((short) 0, (short) 0, (short) 65535), // blue
      new SColor((short) 32768, (short) 32768, (short) 32768), // grey50
      new SColor((short) 65535, (short) 65535, (short) 65535)
   // black
         };

   /**
     *  
     */
   public SPoly createEdgeSPoly(CoordFloatString coords, LatLonPoint ll1, LatLonPoint ll2, float dpplat, float dpplon) {
      // System.out.print(".");
      // System.out.flush();
      LLPoint pts[] = clipToScreen(coords, ll1.getLatitude(), /* north */
      ll2.getLatitude(), /* south */
      ll2.getLongitude(), /* east */
      ll1.getLongitude(), /* west */
      dpplat, dpplon);

      // LLPoint pts[] = clipToScreen_tcm(coords,
      // ll1.getLatitude(), /* north */
      // ll2.getLatitude(), /* south */
      // ll2.getLongitude(), /* east */
      // ll1.getLongitude(), /* west */
      // dpplat,
      // dpplon);

      // int pts_len = (pts == null) ? -1 : pts.length;
      // int pts_tcm_len = (pts_tcm == null) ? -1 : pts_tcm.length;

      // if ( pts_len == pts_tcm_len ) {
      // } else {
      // System.out.println("Pts: "
      // + ((pts == null) ? -1 : pts.length)
      // + "; Pts2: "
      // + ((pts_tcm == null) ? -1 : pts_tcm.length));
      // }

      if (pts == null) {
         /* Completely eliminated poly */
         // System.out.println("eliminated poly!");
         // System.out.println("\tLL1: " + ll1);
         // System.out.println("\tLL2: " + ll2);
         // System.out.println("\tdpplat: " + dpplat);
         // System.out.println("\tdpplon: " + dpplon);
         // System.out.println("\tcoords: " + coords);
         return null;
      }

      SPoly py = new SPoly(pts, LineType.LT_Straight);
      return py;
   }

   /**
    * Clip raw lon/lats to the screen.
    * 
    * @return LLPoint[]
    */
   public LLPoint[] clipToScreen(CoordFloatString cfs, float north, float south, float east, float west, float dpplat, float dpplon) {
      LLPoint pts[] = new LLPoint[cfs.maxIndex()];
      int lpcount = 0, outcount = 0, elimscale = 0;
      double cfslls[] = cfs.vals;
      int cfstupsize = cfs.tsize;
      for (int i = 0; i < pts.length; i++) {
         double lllon = cfslls[cfstupsize * i];
         double lllat = cfslls[cfstupsize * i + 1];
         if ((lllat < south) || (lllat > north) || ((west < east) && ((lllon < west) || (lllon > east)))
               || ((west > east) && (lllon < west) && (lllon > east))) {
            outcount++;
            if (((lpcount > 1) && (outcount > 2)) || ((lpcount == 1) && (outcount > 1))) {
               pts[lpcount] = new LLPoint((float) lllat, (float) lllon); // overwrite
               // previous
               continue;
            }
         } else {
            outcount = 0;
         }
         if ((lpcount > 0) && (i != (pts.length - 1)) && (java.lang.Math.abs(pts[lpcount - 1].lat - lllat) < dpplat)
               && (java.lang.Math.abs(pts[lpcount - 1].lon - lllon) < dpplon)) {
            elimscale++;
            continue;
         }
         pts[lpcount++] = new LLPoint((float) lllat, (float) lllon);
      }
      // only 1 point in poly, and it was out of bounds...
      if ((lpcount == 1) && (outcount > 0))
         lpcount = 0;

      if (lpcount != cfs.maxIndex()) {
         LLPoint newpts[] = new LLPoint[lpcount];
         System.arraycopy(pts, 0, newpts, 0, lpcount);
         pts = newpts;
         // System.out.println("Old(" + cfs.maxIndex() +
         // ")-area(" + elimarea+")-scale(" + elimscale +
         // ") = new(" + pts.length + ")");
      }
      if (pts.length == 0) {
         return null;
      }
      return pts;
   }

   /**
     *  
     */
   public LLPoint[] clipToScreen_tcm(CoordFloatString cfs, float north, float south, float east, float west, float dpplat,
                                     float dpplon) {

      if (west > east) {

      }

      if ((west < east) && com.bbn.openmap.MoreMath.approximately_equal(east, west, 0.001f)) {
         float tmp = west;
         west = east;
         east = tmp;
      }

      LLPoint pts[] = new LLPoint[cfs.maxIndex()];
      int lpcount = 0, outcount = 0, elimscale = 0;
      double cfslls[] = cfs.vals;
      int cfstupsize = cfs.tsize;
      for (int i = 0; i < pts.length; i++) {
         double lllon = cfslls[cfstupsize * i];
         double lllat = cfslls[cfstupsize * i + 1];
         if ((lllat < south) || (lllat > north) || ((west < east) && ((lllon < west) || (lllon > east)))
               || ((west > east) && (lllon < west) && (lllon > east))) {
            outcount++;
            if (((lpcount > 1) && (outcount > 2)) || ((lpcount == 1) && (outcount > 1))) {
               pts[lpcount] = new LLPoint((float) lllat, (float) lllon); // overwrite
               // previous
               continue;
            }
         } else {
            outcount = 0;
         }
         if ((lpcount > 0) && (i != (pts.length - 1)) && (java.lang.Math.abs(pts[lpcount - 1].lat - lllat) < dpplat)
               && (java.lang.Math.abs(pts[lpcount - 1].lon - lllon) < dpplon)) {
            elimscale++;
            continue;
         }
         pts[lpcount++] = new LLPoint((float) lllat, (float) lllon);
      }
      // only 1 point in poly, and it was out of bounds...
      if ((lpcount == 1) && (outcount > 0))
         lpcount = 0;

      if (lpcount != cfs.maxIndex()) {
         LLPoint newpts[] = new LLPoint[lpcount];
         System.arraycopy(pts, 0, newpts, 0, lpcount);
         pts = newpts;
         // System.out.println("Old(" + cfs.maxIndex() +
         // ")-area(" + elimarea+")-scale(" + elimscale +
         // ") = new(" + pts.length + ")");
      }
      if (pts.length == 0) {
         return null;
      }
      return pts;
   }

   protected static SColor textColors[] = {
      new SColor((short) (244 * 255), (short) (164 * 255), (short) (96 * 255)),
      new SColor((short) (210 * 255), (short) (180 * 255), (short) (140 * 255)),
      new SColor((short) (210 * 255), (short) (105 * 255), (short) (30 * 255)),
      new SColor((short) (188 * 255), (short) (143 * 255), (short) (143 * 255)),
      new SColor((short) (205 * 255), (short) (92 * 255), (short) (92 * 255)),
      new SColor((short) (178 * 255), (short) (34 * 255), (short) (34 * 255))
   };

   /**
     *  
     */
   public SText createTextSText(String text, float latitude, float longitude) {
      SText py = new SText();
      py.rType(RenderType.RT_LatLon);
      py.data(text);
      py.ll1(new LLPoint(latitude, longitude));
      return py;
   }

   // public void drawTile(GraphicList spec, float dpplat, float
   // dpplon,
   // LatLonPoint ll1, LatLonPoint ll2, int[] hack) {
   // SColor colors[] = new SColor[6];
   // colors[0] = new
   // SColor((short)(244*255),(short)(164*255),(short)(96*255));
   // colors[1] = new
   // SColor((short)(210*255),(short)(180*255),(short)(140*255));
   // colors[2] = new
   // SColor((short)(210*255),(short)(105*255),(short)(30*255));
   // colors[3] = new
   // SColor((short)(188*255),(short)(143*255),(short)(143*255));
   // colors[4] = new
   // SColor((short)(205*255),(short)(92*255),(short)(92*255));
   // colors[5] = new
   // SColor((short)(178*255),(short)(34*255),(short)(34*255));

   // float ll1lat = ll1.getLatitude();
   // float ll1lon = ll1.getLongitude();
   // float ll2lat = ll2.getLatitude();
   // float ll2lon = ll2.getLongitude();

   // Vector v;
   // try {
   // while ((v = parseRow()) != null) {
   // System.out.println("tt1"); // TCMDBG
   // String textval = (String)(v.elementAt(textColumn));
   // MutableInt texttype = new MutableInt(-1);
   // String desc = covtable.getTextDescription(v, texttype);

   // CoordFloatString coords =
   // (CoordFloatString)(v.elementAt(coordColumn));
   // LLPoint pts = new LLPoint(coords.getYasFloat(0),
   // coords.getXasFloat(0));

   // if ((pts.lat < ll2lat) || (pts.lat > ll1lat) ||
   // (pts.lon < ll1lon) || (pts.lon > ll2lon)) {
   // continue;
   // }

   // hack[0] = hack[0] + 1;
   // hack[1] = hack[1] + coords.maxIndex();
   // SText py = new SText();
   // py.rType(RenderType.RT_LatLon);
   // py.data(textval);
   // py.ll1(pts);
   // py.object(new LineComp(desc));
   // if (texttype.value < 0) {
   // py.color(colors[5]);
   // } else {
   // py.color(colors[texttype.value%5]);
   // }
   // if (spec != null) {
   // spec.addSGraphic(py);
   // }
   // }
   // } catch (FormatException f) {
   // System.out.println("Exception: " + f.getClass() + " " +
   // f.getMessage());
   // }
   // }

   public UGraphic[] packGraphics() {
      return graphics.packGraphics();
   }

   Comp[] getComps() {
      return graphics.getComps();
   }
  
   public static SColor ns(java.awt.Color color) {
      int r = color.getRed();
      int g = color.getGreen();
      int b = color.getBlue();

      return ns(r, g, b);
   }

   private static SColor ns(int r, int g, int b) {
      return new SColor((short) (r * 256), (short) (g * 256), (short) (b * 256));
   }

   public static SColor getSColor(int place) {
      return (cmap[place % cmap.length]);
   }

   /**
    * Return true if we may draw some entity node(point) features.
    */
   public boolean drawEPointFeatures() {
      return false;
   }

   /**
    * Return true if we may draw some connected node(point) features.
    */
   public boolean drawCPointFeatures() {
      return false;
   }

   /**
    * Get a List of Strings listing all the feature types wanted. Returned with
    * the area features first, then text features, then line features, then
    * point features.
    */
   public List getFeatures() {
      return new ArrayList();
   }

   /**
    * Set which library to use. If null, all applicable libraries in database
    * will be searched.
    */
   private List<String> useLibrary = null;

   /**
    * Set the VPF library to use. If null, all libraries will be searched. Null
    * is default.
    */
   public void setUseLibrary(String lib) {
      useLibrary = new ArrayList<String>();
      useLibrary.add(lib);
   }

   /**
    * Get a library name to limit selections from. Used by the
    * LibrarySelectionTable to find out if selection from database should be
    * limited to tiles or feature from certain libraries. Specified here instead
    * of the LibrarySelectionTable in case the LST is shared among layers, since
    * the warehouse is specific to a particular layer.
    */
   public String getUseLibrary() {
      if (useLibrary != null && useLibrary.size() > 0) {
         return useLibrary.get(0);
      } else {
         return null;
      }
   }

   /**
    * Set the VPF libraries to use, by name. If null, all libraries will be
    * searched. Null is default.
    */
   public void setUseLibraries(List<String> libNames) {
      useLibrary = libNames;
   }

   /**
    * Get a list of VPF library names that should be used, specified at
    * configuration.
    */
   public List<String> getUseLibraries() {
      return useLibrary;
   }

   /**
    * Utility method to check if the specified library name has been set by the
    * configuration as one to use.
    * 
    * @param libName the library name to test
    * @return true if the useLibrary list has not been set, is empty, or if the
    *         provided name starts with the specified string entry (Good for
    *         specifying sets of like-libraries).
    */
   public boolean checkLibraryForUsage(String libName) {
      boolean useLibrary = true;
      List<String> libraryNames = getUseLibraries();
      if (libraryNames != null && libraryNames.size() > 0) {
         useLibrary = false;
         for (String libraryName : libraryNames) {
            if (libName.startsWith(libraryName)) {
               useLibrary = true;
               break;
            }
         }
      }
      return useLibrary;
   }

}
