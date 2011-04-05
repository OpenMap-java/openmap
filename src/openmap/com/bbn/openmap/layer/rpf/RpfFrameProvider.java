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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfFrameProvider.java,v $
// $RCSfile: RpfFrameProvider.java,v $
// $Revision: 1.4 $
// $Date: 2005/05/23 20:08:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

import java.util.Vector;

import com.bbn.openmap.proj.Projection;

/**
 * The RpfFrameProvider describes an interface to an object that can provide
 * information about RPF coverage over a certain area, and also retrieve data
 * from the RPF frame files.
 */
public interface RpfFrameProvider {

   /**
    * Returns true if the view attributes should be set if they change at the
    * RpfCacheHandler/RpfCacheManager. If the source of the data is a server,
    * this should return yes. If the source is local, the view attributes will
    * be a shared object and updates are not necessary.
    */
   public boolean needViewAttributeUpdates();

   /**
    * Set the RpfViewAttribute object parameters, which describes a lot about
    * what you'll be asking for later.
    */
   public void setViewAttributes(RpfViewAttributes rva);

   /**
    * Given a projection that describes a map or geographical area, return
    * RpfCoverageBoxes that let you know how to locate and ask for RpfSubframes.
    */
   public Vector<RpfCoverageBox> getCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection p);

   /**
    * Given a projection that describes a map or geographical area, return
    * RpfCoverageBoxes that let you know what bounding boxes of data are
    * available.
    * 
    * @param ullat upper lat
    * @param ullon left lon
    * @param lrlat lower lat
    * @param lrlon right lon
    * @param chartSeries can be null to see all/any.
    */
   public Vector<RpfCoverageBox> getCatalogCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection p, String chartSeries);

   /**
    * Given an area and a two-letter chart series code, find the percentage of
    * coverage on the map that that chart series can offer. If you want specific
    * coverage information, use the getCatalogCoverage call.
    * 
    * @see #getCatalogCoverage(float, float, float, float, Projection, String)
    */
   public float getCalculatedCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection p, String chartSeries);

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
    * @see #getCoverage(float, float, float, float, Projection)
    * @return integer pixel data.
    */
   public int[] getSubframeData(int tocNumber, int entryNumber, int x, int y);

   public RpfIndexedImageData getRawSubframeData(int tocNumber, int entryNumber, int x, int y);

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
    * @see #getCoverage(float, float, float, float, Projection)
    * @return string.
    */
   public String getSubframeAttributes(int tocNumber, int entryNumber, int x, int y);
}