// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageServerUtils.java,v $
// $RCSfile: ImageServerUtils.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/01 20:43:38 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

import java.awt.Color;
import java.util.Properties;

/**
 * A class to contain convenience functions for parsing web image
 * requests.
 */
public class ImageServerUtils implements ImageServerConstants {

    /**
     * Create an OpenMap projection from the values stored in a
     * Properties object. The properties inside should be parsed out
     * from a map request, with the keywords being those defined in
     * the ImageServerConstants interface. Assumes that the shared
     * instance of the ProjectionFactory has been initialized with the
     * expected Projections.
     */
    public static Proj createOMProjection(Properties props,
                                          Projection defaultProj) {

        float scale = PropUtils.floatFromProperties(props,
                SCALE,
                defaultProj.getScale());
        int height = PropUtils.intFromProperties(props,
                HEIGHT,
                defaultProj.getHeight());
        int width = PropUtils.intFromProperties(props,
                WIDTH,
                defaultProj.getWidth());
        com.bbn.openmap.LatLonPoint llp = defaultProj.getCenter();
        float longitude = PropUtils.floatFromProperties(props,
                LON,
                llp.getLongitude());
        float latitude = PropUtils.floatFromProperties(props,
                LAT,
                llp.getLatitude());

        Class projClass = null;
        String projType = props.getProperty(PROJTYPE);
        
        if (projType != null) {
            projClass = ProjectionFactory.getProjClassForName(projType);
        }

        if (projClass == null) {
            projClass = defaultProj.getClass();
        }

        if (Debug.debugging("imageserver")) {
            Debug.output("ImageServerUtils.createOMProjection: projection "
                    + projClass.getName()
                    + ", with HEIGHT = "
                    + height
                    + ", WIDTH = "
                    + width + ", lat = " + latitude + ", lon = " + longitude);
        }

        Proj proj = (Proj) ProjectionFactory.makeProjection(projClass,
                latitude,
                longitude,
                scale,
                width,
                height);

        return (Proj) proj;
    }

    /**
     * Create a Color object from the properties TRANSPARENT and
     * BGCOLOR properties.
     * 
     * @return Color object for background.
     */
    public static Color getBackground(Properties props) {
        boolean transparent = PropUtils.booleanFromProperties(props,
                TRANSPARENT,
                false);

        Color backgroundColor = PropUtils.parseColorFromProperties(props,
                BGCOLOR,
                "FFFFFF");

        if (transparent) {
            backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0x00);
        }

        if (Debug.debugging("imageserver")) {
            Debug.output("ImageServerUtils.createOMProjection: projection color: "
                    + Integer.toHexString(backgroundColor.getRGB())
                    + ", transparent(" + transparent + ")");
        }

        return backgroundColor;
    }
}