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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfFrameCacheHandler.java,v $
// $RCSfile: RpfFrameCacheHandler.java,v $
// $Revision: 1.13 $
// $Date: 2007/02/26 17:34:04 $
// $Author: dietrick $
//
// **********************************************************************

/**
 *  Modifications:
 * 
 * 1. Changed getCoverage()
 *
 */

package com.bbn.openmap.layer.rpf;

import java.util.List;
import java.util.Vector;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * The RpfFrameCacheHandler does everything involved with handling RAW RPF
 * frames. If used locally, it can also deal with filling the role of
 * RpfFrameProvider. You create one of these with the paths to the RPF
 * directories, and then hand it to something that needs a RpfFrameProvider, or
 * that acts like one.
 */
public class RpfFrameCacheHandler
      extends CacheHandler
      implements RpfFrameProvider {

   /* Default frame cache size. */
   public final static int FRAME_CACHE_SIZE = 5;
   /** Colortable used on the frames. */
   protected RpfColortable colortable;

   /** For future use... */
   protected boolean Dchum = true;
   /** Special outlining for chummed subframes */
   protected boolean outlineChum = false;
   /** The Table of Contents files, parsed and ready to use. */
   protected RpfTocHandler[] tocs;
   /** View and display attributes for the data. */
   protected RpfViewAttributes viewAttributes = new RpfViewAttributes();

   /**
    * The default constructor.
    * 
    * @param RpfPaths the directory paths to the RPF directories.
    */
   public RpfFrameCacheHandler(String[] RpfPaths) {
      this(RpfPaths, FRAME_CACHE_SIZE);
   }

   /**
    * The constructor to use if you want to modify the number of frames held in
    * the cache..
    * 
    * @param RpfPaths the directory paths to the RPF directories.
    */
   public RpfFrameCacheHandler(String[] RpfPaths, int max_size) {
      super(max_size);
      tocs = createTocHandlers(RpfPaths);
      colortable = new RpfColortable();
   }

   /**
    * When you pre-initialize the RpfTocHandlers before giving them to the
    * RpfFrameCacheHandler.
    */
   public RpfFrameCacheHandler(RpfTocHandler[] tocHandlers) {
      tocs = tocHandlers;
      colortable = new RpfColortable();
   }

   // public void finalize() {
   // Debug.message("gc", "RpfFrameCacheHandler: getting GC'd");
   // }

   /**
    * RpfFrameProvider interface method. If this is being used as a frame
    * provider, it's local, right?
    */
   public boolean needViewAttributeUpdates() {
      return false;
   }

   /**
    * Should only be set via the object it is sending frame data to. Don't send
    * in a null value, since this is assumed to be valid in other parts of the
    * code.
    */
   public void setViewAttributes(RpfViewAttributes va) {
      viewAttributes = va;

      if (va != null && colortable != null) {
         colortable.setOpaqueness(va.opaqueness);
         colortable.setNumColors(va.numberOfColors);
      }
   }

   /**
    * RpfFrameProvider interface method. Return all the RpfCoverageBoxes that
    * fall in the area of interest.
    * 
    * @param ullat NW latitude.
    * @param ullon NW longitude.
    * @param lrlat SE latitude.
    * @param lrlon SE longitude
    * @param proj projection to use for zone decisions.
    * @param chartSeries RpfProductInfo.seriesCode entry.
    * @return Vector of RpfCoverageBoxes.
    */
   public Vector<RpfCoverageBox> getCatalogCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection proj, String chartSeries) {

      Vector<RpfCoverageBox> coverages = new Vector<RpfCoverageBox>();
      for (RpfTocHandler toc : tocs) {

         // Check the tochandlers for differences, and reload them
         // if necessary.
         if (toc.hasChanged())
            toc.reload();
         if (!toc.isValid())
            continue;

         toc.getCatalogCoverage(ullat, ullon, lrlat, lrlon, proj, chartSeries, coverages);
      }

      return coverages;
   }

   /**
    * Given an area and a two-letter chart series code, find the percentage of
    * coverage on the map that that chart series can offer. If you want specific
    * coverage information, use the getCatalogCoverage call. Don't send a chart
    * series code of ANY, since that doesn't make sense.
    * 
    * @param ullat NW latitude.
    * @param ullon NW longitude.
    * @param lrlat SE latitude.
    * @param lrlon SE longitude
    * @param p projection to use for zone decisions.
    * @param chartSeries RpfProductInfo.seriesCode entry.
    * @return percentage of map covered by specific chart type.
    * @see #getCatalogCoverage
    */
   public float getCalculatedCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection p, String chartSeries) {

      if (chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY)) {
         return 0f;
      }

      Vector<RpfCoverageBox> results = getCatalogCoverage(ullat, ullon, lrlat, lrlon, p, chartSeries);

      int size = results.size();

      if (size == 0) {
         return 0f;
      }

      // Now interpret the results and figure out the real total
      // percentage coverage for the chartSeries. First need to
      // figure out the current size of the subframes. Then create
      // a boolean matrix of those subframes that let you figure out
      // how many of them are available. Calculate the percentage
      // off that.
      int i, x, y;

      double frameLatInterval = Double.MAX_VALUE;
      double frameLonInterval = Double.MAX_VALUE;
      RpfCoverageBox rcb;
      for (i = 0; i < size; i++) {
         rcb = (RpfCoverageBox) results.elementAt(i);
         if (rcb.subframeLatInterval < frameLatInterval) {
            frameLatInterval = rcb.subframeLatInterval;
         }
         if (rcb.subframeLonInterval < frameLonInterval) {
            frameLonInterval = rcb.subframeLonInterval;
         }
      }

      if (frameLatInterval == Double.MAX_VALUE || frameLonInterval == Double.MAX_VALUE) {
         return 0.0f;
      }

      int numHFrames = (int) Math.ceil((lrlon - ullon) / frameLonInterval);
      int numVFrames = (int) Math.ceil((ullat - lrlat) / frameLatInterval);

      boolean[][] coverage = new boolean[numHFrames][numVFrames];
      for (i = 0; i < size; i++) {

         rcb = (RpfCoverageBox) results.elementAt(i);
         if (rcb.percentCoverage == 100) {
            return 1.0f;
         }

         for (y = 0; y < numVFrames; y++) {
            for (x = 0; x < numHFrames; x++) {
               // degree location of indexs
               float yFrameLoc = (float) (lrlat + (y * frameLatInterval));
               float xFrameLoc = (float) (ullon + (x * frameLonInterval));
               if (coverage[x][y] == false) {
                  if (rcb.within(yFrameLoc, xFrameLoc)) {
                     coverage[x][y] = true;
                  }
               }
            }
         }
      }

      float count = 0;

      for (y = 0; y < numVFrames; y++) {
         for (x = 0; x < numHFrames; x++) {
            if (coverage[x][y] == true) {
               // System.out.print("X");
               count++;
            } else {
               // System.out.print(".");
            }
         }
         // System.out.println();
      }

      return count / (float) (numHFrames * numVFrames);
   }

   /**
    * Given a projection which describes the map (or area of interest), return
    * the best RpfTocEntry, from all the A.TOC, that covers the area.
    * RpfFrameProvider method.
    * 
    * 1. Fixed bug that was preventing it from actually returning the best
    * coverage box in the first position of the Vector. 2. Prevented the method
    * from returning early when it found a box that claimed perfect coverage.
    * This was causing problems in some areas, where boxes that claimed coverage
    * didn't actually have it. This could be a data problem, so maybe we can add
    * the early return back in in the future. 3. Changed < to <= below. 4.
    * Iterates thru list returned by RpcTocHandler since the return type for
    * getBestCoverageEntry changed from RpfTocEntry to List.
    * 
    * @param ullat NW latitude.
    * @param ullon NW longitude.
    * @param lrlat SE latitude.
    * @param lrlon SE longitude
    * @param proj CADRG projection to use for zone decisions.
    * @return Vector of RpfCoverageBoxes.
    */
   public Vector<RpfCoverageBox> getCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection proj) {

      Debug.message("rpf", "RpfFrameCacheHandler: getCoverage()");

      Vector<RpfCoverageBox> coverageBoxes = new Vector<RpfCoverageBox>();

      int count = -1;
      
      for (RpfTocHandler toc : tocs) {
         count++;
         
         // Check the toc handlers for differences, and reload them
         // if necessary.
         if (toc.hasChanged())
            toc.reload();

         if (!toc.isValid())
            continue;

         // NOTE: returning a list of best coverage entries.
         // Added inner loop to deal with iterating over list of
         // results.
         List<RpfTocEntry> coverageEntries = toc.getBestCoverageEntry(ullat, ullon, lrlat, lrlon, proj, viewAttributes);

         for (RpfTocEntry currentEntry : coverageEntries) {

            // This is a test for total coverage. If we get total
            // coverage of an exact scale match, just return this
            // coverage box right away. If the scale is not a
            // perfect
            // match, then we will return the box that has
            // complete
            // coverage with the best scale. A boundaryHit of 8
            // means
            // total coverage. Trust me.
            if (currentEntry != null) {

               if (Debug.debugging("rpftoc")) {
                  System.out.println("RFCH: Toc " + count + " returned an entry");
               }

               RpfCoverageBox currentCoverage = currentEntry.coverage;

               // NOTE:
               // removed because some areas had boxes that
               // claimed perfect
               // coverage but didn't actually have it.
               // This could be a data problem, but in any case,
               // this work-around returns all relevant coverage
               // boxes.
               // if (currentCoverage.percentCoverage >= 100f &&
               // scaleDifference(proj, currentCoverage) == 0) {
               // coverageBoxes.removeAllElements();
               // coverageBoxes.addElement(currentCoverage);
               // return coverageBoxes;
               // } else {

               // You now ought to at least make sure that the
               // scales are the same for all A.TOCs. That way,
               // the subframe spacing will be the same. Put the
               // best coverage (smallest scale difference) at
               // the front of the list, and whittle it down from
               // there.

               Object[] coverageArray = new Object[coverageBoxes.size()];
               coverageBoxes.copyInto(coverageArray);
               coverageBoxes.removeAllElements();
               int size = coverageArray.length;

               // Set this here in case the vector is empty...
               // float currentScale = currentEntry.info.scale;

               if (size == 0) {
                  coverageBoxes.addElement(currentCoverage);

               } else {
                  boolean addedCurrent = false;
                  boolean okToAddCurrent = true;
                  for (int j = 0; j < size; j++) {
                     RpfCoverageBox rcb = (RpfCoverageBox) coverageArray[j];

                     if (!addedCurrent) {
                        if (j == 0) {

                           // So first, check to see if the
                           // current
                           // coverage is a better match than
                           // the
                           // current best, first considering
                           // scale, and then considering
                           // coverage.

                           // NOTE:
                           // Changed < to <=, because <
                           // gives
                           // higher priority to whatever is
                           // first in the
                           // list. I.e. if 2 boxes had the
                           // same
                           // scale difference but the second
                           // one actually
                           // had better coverage, it would
                           // have gotten
                           // discarded.
                           if (scaleDifference(proj, currentCoverage) <= scaleDifference(proj, rcb)
                                 && currentCoverage.percentCoverage >= rcb.percentCoverage) {
                              coverageBoxes.addElement(currentCoverage);
                              addedCurrent = true;
                              if (currentCoverage.scale == rcb.scale) {
                                 coverageBoxes.addElement(rcb);
                              } else {
                                 // if these scales don't
                                 // match, none of the
                                 // subsequent rcbs will
                                 // either. We're done.
                                 break;
                              }
                           } else { // rcb percent coverage
                              // is better
                              coverageBoxes.addElement(rcb);
                           }
                        } else { // j!= 0
                           if (currentCoverage.percentCoverage >= rcb.percentCoverage) {
                              if (((RpfCoverageBox) coverageBoxes.get(0)).scale == currentCoverage.scale) {

                                 coverageBoxes.add(currentCoverage);
                                 addedCurrent = true;
                              } else {

                                 okToAddCurrent = false;
                              }
                              coverageBoxes.add(rcb);
                           } else { // rcb percent coverage
                              // is better
                              // we already know that scale
                              // matches since it was
                              // added previously
                              coverageBoxes.add(rcb);
                           }
                        }
                     } else { // currentCoverage already added;
                        // all we need to do is add current
                        // rcb if scale matches
                        if (((RpfCoverageBox) coverageBoxes.get(0)).scale == rcb.scale) {
                           coverageBoxes.add(rcb);
                        } else {
                        }
                     }
                  }
                  // Add current if not added already and if
                  // scale matches.
                  // It's possible that we performed this check
                  // already --
                  // i.e. if currentCoverage had better %
                  // coverage
                  // than someone else but wasn't added because
                  // of the scale.
                  // If that happened, then okToAddCurrent is
                  // false.
                  if (!addedCurrent && okToAddCurrent) {
                     if (((RpfCoverageBox) coverageBoxes.get(0)).scale == currentCoverage.scale) {
                        coverageBoxes.add(currentCoverage);
                     } else {
                     }
                  }
               }
               // }
            } else {
               if (Debug.debugging("rpftoc")) {
                  System.out.println("RFCH: Toc " + count + " did NOT return an entry");
               }
            }
         }
      }

      return coverageBoxes;
   }

   /**
    * Given the indexes to a certain RpfTocEntry within a certain A.TOC, find
    * the frame and return the attribute information. The tocNumber and
    * entryNumber are given within the RpfCoverageBox received from a
    * getCoverage call.
    * 
    * @param tocNumber the toc id for a RpfTocHandler for a particular frame
    *        provider.
    * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a particular
    *        frame provider.
    * @param x the horizontal subframe index, from the left side of a boundary
    *        rectangle of the entry.
    * @param y the vertical subframe index, from the top side of a boundary
    *        rectangle of the entry.
    * @see #getCoverage
    * @return string.
    */
   public String getSubframeAttributes(int tocNumber, int entryNumber, int x, int y) {

      if (!tocs[tocNumber].isValid())
         return null;

      RpfTocEntry entry = tocs[tocNumber].entries[entryNumber];

      /* If beyond the image boundary, forget it */
      if (y < 0 || x < 0 || entry == null || y >= entry.vertFrames * 6 || x >= entry.horizFrames * 6) {

         return null;
      }

      if (!entry.isFramesLoaded()) {
         tocs[tocNumber].loadFrameInformation(entry);
      }
      RpfFrameEntry frameEntry = entry.getFrame(y / 6, x / 6);

      /* Get the right frame from the frame cache */
      RpfFrame frame = (RpfFrame) get(frameEntry);

      if (frame == null)
         return null;

      /*
       * This should never fail, since all subframes should be present
       */
      return frame.getReport(x, y, frameEntry, entry.Cib);
   }

   /**
    * Given the indexes to a certain RpfTocEntry within a certain A.TOC, find
    * the frame/subframe data, decompress it, and return image pixels. The
    * tocNumber and entryNumber are given within the RpfCoverageBox received
    * from a getCoverage call.
    * 
    * @param tocNumber the toc id for a RpfTocHandler for a particular frame
    *        provider.
    * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a particular
    *        frame provider.
    * @param x the horizontal subframe index, from the left side of a boundary
    *        rectangle of the entry.
    * @param y the vertical subframe index, from the top side of a boundary
    *        rectangle of the entry.
    * @see #getCoverage
    * @return integer pixel data.
    */
   public int[] getSubframeData(int tocNumber, int entryNumber, int x, int y) {

      if (!tocs[tocNumber].isValid()) {
         return null;
      }

      RpfTocEntry entry = tocs[tocNumber].entries[entryNumber];

      /* If beyond the image boundary, forget it */
      if (y < 0 || x < 0 || entry == null || y >= entry.vertFrames * 6 || x >= entry.horizFrames * 6) {
         return null;
      }

      if (!entry.isFramesLoaded()) {
         tocs[tocNumber].loadFrameInformation(entry);
      }
      RpfFrameEntry frameEntry = entry.getFrame(y / 6, x / 6);

      /* Get the right frame from the frame cache */
      RpfFrame frame = (RpfFrame) get(frameEntry);

      if (frame == null) {
         return null;
      }

      checkColortable(frame, frameEntry, entry, tocNumber, entryNumber);

      /*
       * This should never fail, since all subframes should be present
       */
      return frame.decompressSubframe(x, y, colortable);
   }

   public RpfIndexedImageData getRawSubframeData(int tocNumber, int entryNumber, int x, int y) {
      if (!tocs[tocNumber].isValid()) {
         return null;
      }

      RpfTocEntry entry = tocs[tocNumber].entries[entryNumber];

      /* If beyond the image boundary, forget it */
      if (y < 0 || x < 0 || entry == null || y >= entry.vertFrames * 6 || x >= entry.horizFrames * 6) {

         return null;
      }
      if (!entry.isFramesLoaded()) {
         tocs[tocNumber].loadFrameInformation(entry);
      }
      RpfFrameEntry frameEntry = entry.getFrame(y / 6, x / 6);

      /* Get the right frame from the frame cache */
      RpfFrame frame = (RpfFrame) get(frameEntry);

      if (frame == null)
         return null;

      checkColortable(frame, frameEntry, entry, tocNumber, entryNumber);

      RpfIndexedImageData riid = new RpfIndexedImageData();
      riid.imageData = frame.decompressSubframe(x, y);
      riid.colortable = colortable.colors;
      return riid;
   }

   /**
    * Take a bunch of stuff that has already been calculated, and then figure
    * out if a new colortable is needed. If it is, load it up with info. Called
    * from two different places, which is why it exists.
    * 
    * It's been determined that, for each subframe, the colortable from it's
    * parent frame should be used. Although RPF was designed and specified that
    * the colortable should be constant across zones, that's not always the
    * case.
    */
   protected void checkColortable(RpfFrame frame, RpfFrameEntry frameEntry, RpfTocEntry entry, int tocNumber, int entryNumber) {
      // Colortables are constant across chart types and zones. If
      // the current chart type and zone don't match the colortable,
      // read the proper one from the frame. All the frames inside
      // an entry, which is a boundary box, will certainly share a
      // colortable.
      // if (colortable.colors == null ||
      // !colortable.isSameATOCIndexes(tocNumber, entryNumber)) {

      // You know, we don't need to make the check - we should just
      // do this every time - the colortable is already created for
      // the frame, so we might as well use what we know to be good
      // for each subframe.

      if (true) {

         if (Debug.debugging("rpf")) {
            Debug.output("RpfFrameCacheHandler: getting new colors");
            Debug.output("RpfFrameCacheHandler: getting CIB colors = " + entry.Cib);
         }
         // Seems like there ought to be a better way to do this.
         colortable.setFrom(frame.getColortable());
         colortable.setCib(entry.Cib);
         colortable.setATOCIndexes(tocNumber, entryNumber);
         colortable.zone = entry.zone;
         colortable.seriesCode = entry.info.seriesCode;
      }

      if (viewAttributes != null) {
         // this is useless...
         // colortable.setNumColors(viewAttributes.numberOfColors);

         colortable.setOpaqueness(viewAttributes.opaqueness);
      }
   }

   /**
    * Set up the A.TOC files, to find out what coverage there is.
    * 
    * @param RpfPaths the paths to the RPF directories.
    * @return the RpfTocHandlers for the A.TOCs.
    */
   public static RpfTocHandler[] createTocHandlers(String[] RpfPaths) {

      RpfTocHandler[] tocs = new RpfTocHandler[(RpfPaths != null ? RpfPaths.length : 0)];
      if (RpfPaths != null) {
         for (int i = 0; i < tocs.length; i++) {
            tocs[i] = new RpfTocHandler(RpfPaths[i], i);
         }
      }
      return tocs;
   }

   /**
    * A customized way to retrieve a frame from the cache, using a
    * RpfFrameEntry. A RpfFrameEntry is the way to get the Dchum capability
    * kicked off in the frame. If you don't care about Dchum, use the other get
    * method. CacheHandler method.
    */
   public Object get(RpfFrameEntry rfe) {

      CacheObject ret = searchCache(rfe.framePath);
      if (ret != null)
         return ret.obj;

      ret = load(rfe);
      if (ret == null)
         return null;

      if (Debug.debugging("rpfdetail")) {
         System.out.println(rfe);
      }

      replaceLeastUsed(ret);
      return ret.obj;
   }

   public CacheObject load(Object key) {

      if (key instanceof RpfFrameEntry) {

         RpfFrameEntry rfe = (RpfFrameEntry) key;
         if (!rfe.exists) {
            if (Debug.debugging("rpf")) {
               System.out.println("RpfFrameCacheHandler: Frame doesn't exist!: " + rfe.framePath);
            }
            return null;
         }

         if (Debug.debugging("rpf")) {
            Debug.output("RpfFrameCacheHandler: Loading Frame " + rfe.framePath);
         }

         RpfFrame frame = new RpfFrame(rfe);
         if (frame.isValid()) {
            return new CacheObject(rfe.framePath, frame);
         } else {
            if (Debug.debugging("rpf")) {
               Debug.error("RpfFrameCacheHandler:  Couldn't find frame /" + rfe.framePath + "/ (" + rfe.framePath.length()
                     + " chars)");
            }
            rfe.exists = false;
         }
      }
      return null;
   }

   /**
    * Cachehandler method.
    */
   public void resizeCache(int max_size) {
      resetCache(max_size);
   }

   /**
    * CacheHandler method. Need to clear memory, get gc moving, and ready for
    * new objects
    */
   public void resetCache() {
      super.resetCache();
      Debug.message("rpf", "RpfFrameCacheHandler: reset frame cache.");
   }

   public static float scaleDifference(Projection proj, RpfCoverageBox box) {
      return (float) (Math.abs(proj.getScale() - box.scale));
   }

   public RpfColortable getColortable() {
      return colortable;
   }

   public void setColortable(RpfColortable colortable) {
      this.colortable = colortable;
   }
}
