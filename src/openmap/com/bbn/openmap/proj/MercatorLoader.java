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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/MercatorLoader.java,v $
// $RCSfile: MercatorLoader.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/11 20:39:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.Debug;

import java.util.Properties;

/**
 * ProjectionLoader to add the Mercator projection to an OpenMap
 * application.
 * 
 * @see BasicProjectionLoader
 */
public class MercatorLoader extends BasicProjectionLoader implements
        ProjectionLoader {

    public final static ProjectionLoader defaultMercator = new MercatorLoader();

    public MercatorLoader() {
        super(Mercator.class, Mercator.MercatorName, "Mercator Projection");
    }

    /**
     * Create the projection with the given parameters.
     * 
     * @throws exception if a parameter is missing or invalid.
     */
    public Projection create(Properties props) throws ProjectionException {

        try {
            LatLonPoint llp = (LatLonPoint) props.get(ProjectionFactory.CENTER);
            float scale = PropUtils.floatFromProperties(props,
                    ProjectionFactory.SCALE,
                    10000000);
            int height = PropUtils.intFromProperties(props,
                    ProjectionFactory.HEIGHT,
                    100);
            int width = PropUtils.intFromProperties(props,
                    ProjectionFactory.WIDTH,
                    100);
            return new Mercator(llp, scale, width, height);

        } catch (Exception e) {
            if (Debug.debugging("proj")) {
                Debug.output("MercatorLoader: problem creating Mercator projection "
                        + e.getMessage());
            }
        }

        throw new ProjectionException("MercatorLoader: problem creating Mercator projection");

    }

}