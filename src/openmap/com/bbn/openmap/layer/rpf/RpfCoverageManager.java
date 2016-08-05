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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfCoverageManager.java,v $
// $RCSfile: RpfCoverageManager.java,v $
// $Revision: 1.6 $
// $Date: 2007/06/21 21:39:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * This is an object that provides coverage information on the Rpf data. It is
 * supposed to be a simple tool that lets you see the general location of data,
 * to guide you to the right place and scale of coverage. The layer really uses
 * the properties passed in to it to determine which RPF/A.TOC should be scanned
 * for the data. There is a palette for this layer, that lets you turn off the
 * coverage for different levels of Rpf. Right now, only City Graphics, TLM,
 * JOG, TPC, ONC, JNC, GNC and 5/10 meter CIB scales are are handled. All other
 * scales are tossed together under the misc setting. The City Graphics setting
 * shows all charts for scales greater than than 1:15k.
 */
public class RpfCoverageManager {

	/**
	 * The last line type of the edge of the rectangles. Used to determine
	 * whether the line needs to be re-projected based on a projection change.
	 */
	protected int currentLineType = OMGraphic.LINETYPE_RHUMB;

	/** The place to get the coverage information, */
	protected RpfFrameProvider frameProvider;

	/** Don't use this. */
	public RpfCoverageManager(RpfFrameProvider rfp) {
		frameProvider = rfp;
	}

	/**
	 * Get the map coverage
	 * 
	 * @param ullat
	 * @param ullon
	 * @param lrlat
	 * @param lrlon 
	 * @param proj projection for display
	 * @param chartSeries the chart series to query for, may be null for all coverages
	 * @param coverages  The Map to be modified
	 */
	protected void getCatalogCoverage(double ullat, double ullon, double lrlat, double lrlon, Projection proj,
			String chartSeries, Map<RpfProductInfo, RpfCoverage.RpfCoverageControl> coverages) {

		Debug.message("rpfcov", "RpfCoverageManager: Getting catalog coverage from RpfFrameProvider");
		if (proj == null || frameProvider == null) {
			return;
		}

		CADRG cadrg;
		if (proj instanceof CADRG) {
			cadrg = (CADRG) proj;
		} else {
			cadrg = new CADRG((LatLonPoint) proj.getCenter(new LatLonPoint.Float()), proj.getScale(), proj.getWidth(),
					proj.getHeight());
		}

		List<RpfCoverageBox> hemisphereData = new ArrayList<RpfCoverageBox>();

		if (ProjMath.isCrossingDateline(ullon, lrlon, proj.getScale())) {

			hemisphereData.addAll(frameProvider.getCatalogCoverage(ullat, ullon, lrlat, 180f, cadrg, chartSeries));
			hemisphereData.addAll(frameProvider.getCatalogCoverage(ullat, -180f, lrlat, lrlon, cadrg, chartSeries));
		} else {
			hemisphereData.addAll(frameProvider.getCatalogCoverage(ullat, ullon, lrlat, lrlon, cadrg, chartSeries));
		}

		boolean checkSeries = !(chartSeries == null || chartSeries.equals(RpfViewAttributes.ANY) || chartSeries.equals(RpfViewAttributes.ALL));
		
		for (RpfCoverageBox box : hemisphereData) {

			OMRect rect = new OMRect(box.nw_lat, box.nw_lon, box.se_lat, box.se_lon, currentLineType);

			RpfProductInfo rpi = RpfProductInfo.get(box.chartCode);
			
			
			if (rpi != null) {

				if (checkSeries && !rpi.seriesCode.equalsIgnoreCase(chartSeries)) {
					continue;
				}

				RpfCoverage.RpfCoverageControl control = coverages.get(rpi);
				if (control != null) {
					control.add(rect);
					rect.generate(proj);
				}
			}
		}
	}
}