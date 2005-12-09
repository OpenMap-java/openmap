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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/CADRGLoader.java,v $
// $RCSfile: CADRGLoader.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:09:01 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * ProjectionLoader to add the CADRG projection to an OpenMap
 * application.
 * 
 * @see BasicProjectionLoader
 */
public class CADRGLoader extends BasicProjectionLoader implements
        ProjectionLoader {

    public CADRGLoader() {
        super(CADRG.class,
              CADRG.CADRGName,
              "Equal-Arc projection for displaying RPF data (CADRG and CIB).");
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
            return new CADRG(llp, scale, width, height);

        } catch (Exception e) {
            if (Debug.debugging("proj")) {
                Debug.output("CADRGLoader: problem creating CADRG projection "
                        + e.getMessage());
            }
        }

        throw new ProjectionException("CADRGLoader: problem creating CADRG projection");
    }

}