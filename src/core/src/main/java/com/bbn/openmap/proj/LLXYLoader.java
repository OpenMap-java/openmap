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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/LLXYLoader.java,v $
// $RCSfile: LLXYLoader.java,v $
// $Revision: 1.5 $
// $Date: 2008/09/19 14:20:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.proj.coords.DatumShiftGCT;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * ProjectionLoader to add the LLXY projection to an OpenMap application.
 * 
 * @see BasicProjectionLoader
 */
public class LLXYLoader extends BasicProjectionLoader implements
        ProjectionLoader {

    public LLXYLoader() {
        super(LLXY.class,
              LLXY.LLXYName,
              "Equivalent projection, used for many web data sources.");
    }

    /**
     * Create the projection with the given parameters.
     * 
     * @throws exception if a parameter is missing or invalid.
     */
    public Projection create(Properties props) throws ProjectionException {

        try {
            LatLonPoint llp = convertToLLP((Point2D) props.get(ProjectionFactory.CENTER));
            float scale = PropUtils.floatFromProperties(props,
                    ProjectionFactory.SCALE,
                    10000000);
            int height = PropUtils.intFromProperties(props,
                    ProjectionFactory.HEIGHT,
                    100);
            int width = PropUtils.intFromProperties(props,
                    ProjectionFactory.WIDTH,
                    100);

            GeoProj proj = new LLXY(llp, scale, width, height);

            Ellipsoid ellps = (Ellipsoid) props.get(ProjectionFactory.DATUM);
            if ((ellps != null) && (ellps != Ellipsoid.WGS_84)) {
                proj = new DatumShiftProjection(proj, new DatumShiftGCT(ellps));
            }

            return proj;

        } catch (Exception e) {
            if (Debug.debugging("proj")) {
                Debug.output("LLXYLoader: problem creating LLXY projection "
                        + e.getMessage());
            }
        }

        throw new ProjectionException("LLXYLoader: problem creating LLXY projection");

    }

}