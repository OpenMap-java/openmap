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
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

import java.util.Vector;
import com.bbn.openmap.proj.CADRG;

/**
 * The RpfFrameProvider describes an interface to an object that can
 * provide information about RPF coverage over a certain area, and
 * also retrieve data from the RPF frame files.
 */
public interface RpfFrameProvider {

    /**
     * Returns true if the view attributes should be set if they
     * change at the RpfCacheHandler/RpfCacheManager. If the source of
     * the data is a server, this should return yes. If the source is
     * local, the view attributes will be a shared object and updates
     * are not nessary.
     */
    public boolean needViewAttributeUpdates();

    /**
     * Set the RpfViewAttribute object parameters, which describes
     * alot about what you'll be asking for later.
     */
    public void setViewAttributes(RpfViewAttributes rva);

    /**
     * Given a projection that describes a map or geographical area,
     * return RpfCoverageBoxes that let you know how to locate and ask
     * for RpfSubframes.
     */
    public Vector getCoverage(float ullat, float ullon, float lrlat,
                              float lrlon, CADRG p);

    /**
     * Given a projection that describes a map or geographical area,
     * return RpfCoverageBoxes that let you know what bounding boxes
     * of data are available.
     */
    public Vector getCatalogCoverage(float ullat, float ullon, float lrlat,
                                     float lrlon, CADRG p, String chartSeries);

    /**
     * Given an area and a two-letter chart series code, find the
     * percentage of coverage on the map that that chart series can
     * offer. If you want specific coverage information, use the
     * getCatalogCoverage call.
     * 
     * @see #getCatalogCoverage(float, float, float, float, CADRG,
     *      String)
     */
    public float getCalculatedCoverage(float ullat, float ullon, float lrlat,
                                       float lrlon, CADRG p, String chartSeries);

    /**
     * Given the indexes to a certain RpfTocEntry within a certain
     * A.TOC, find the frame/subframe data, decompress it, and return
     * image pixels. The tocNumber and entryNumber are given within
     * the RpfCoverageBox received from a getCoverage call.
     * 
     * @param tocNumber the toc id for a RpfTocHandler for a
     *        particular frame provider.
     * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a
     *        particular frame provider.
     * @param x the horizontal subframe index, from the left side of a
     *        boundary rectangle of the entry.
     * @param y the vertical subframe index, from the top side of a
     *        boundary rectangle of the entry.
     * @see #getCoverage(float, float, float, float, CADRG)
     * @return integer pixel data.
     */
    public int[] getSubframeData(int tocNumber, int entryNumber, int x, int y);

    public RpfIndexedImageData getRawSubframeData(int tocNumber,
                                                  int entryNumber, int x, int y);

    /**
     * Given the indexes to a certain RpfTocEntry within a certain
     * A.TOC, find the frame and return the attribute information. The
     * tocNumber and entryNumber are given within the RpfCoverageBox
     * received from a getCoverage call.
     * 
     * @param tocNumber the toc id for a RpfTocHandler for a
     *        particular frame provider.
     * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a
     *        particular frame provider.
     * @param x the horizontal subframe index, from the left side of a
     *        boundary rectangle of the entry.
     * @param y the vertical subframe index, from the top side of a
     *        boundary rectangle of the entry.
     * @see #getCoverage(float, float, float, float, CADRG)
     * @return string.
     */
    public String getSubframeAttributes(int tocNumber, int entryNumber, int x,
                                        int y);
}