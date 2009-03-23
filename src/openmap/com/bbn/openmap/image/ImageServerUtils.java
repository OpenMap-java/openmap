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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageServerUtils.java,v $
// $RCSfile: ImageServerUtils.java,v $
// $Revision: 1.10 $
// $Date: 2006/02/16 16:22:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A class to contain convenience functions for parsing web image requests.
 */
public class ImageServerUtils implements ImageServerConstants {

    /**
     * Create an OpenMap projection from the values stored in a Properties
     * object. The properties inside should be parsed out from a map request,
     * with the keywords being those defined in the ImageServerConstants
     * interface. Assumes that the shared instance of the ProjectionFactory has
     * been initialized with the expected Projections.
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
        Point2D llp = defaultProj.getCenter();
        float longitude = PropUtils.floatFromProperties(props,
                LON,
                (float) llp.getX());
        float latitude = PropUtils.floatFromProperties(props,
                LAT,
                (float) llp.getY());

        Class<? extends Projection> projClass = null;
        String projType = props.getProperty(PROJTYPE);

        ProjectionFactory projFactory = ProjectionFactory.loadDefaultProjections();
        if (projType != null) {
            projClass = projFactory.getProjClassForName(projType);
        }

        if (projClass == null) {
            projClass = defaultProj.getClass();
        }

        if (Debug.debugging("imageserver")) {
            Debug.output("ImageServerUtils.createOMProjection: projection "
                    + projClass.getName() + ", with HEIGHT = " + height
                    + ", WIDTH = " + width + ", lat = " + latitude + ", lon = "
                    + longitude + ", scale = " + scale);
        }

        Proj proj = (Proj) projFactory.makeProjection(projClass,
                new Point2D.Float(longitude, latitude),
                scale,
                width,
                height);

        return (Proj) proj;
    }

    /**
     * Create a Color object from the properties TRANSPARENT and BGCOLOR
     * properties. Default color returned is white.
     * 
     * @param props the Properties containing background color information.
     * @return Color object for background.
     */
    public static Color getBackground(Properties props) {
        return (Color) getBackground(props, Color.white);
    }

    /**
     * Create a Color object from the properties TRANSPARENT and BGCOLOR
     * properties. Default color returned is white.
     * 
     * @param props the Properties containing background color information.
     * @param defPaint the default Paint to use in case the color isn't defined
     *        in the properties.
     * @return Color object for background.
     */
    public static Paint getBackground(Properties props, Paint defPaint) {
        boolean transparent = PropUtils.booleanFromProperties(props,
                TRANSPARENT,
                false);

        Paint backgroundColor = PropUtils.parseColorFromProperties(props,
                BGCOLOR,
                defPaint);

        if (backgroundColor == null) {
            backgroundColor = Color.white;
        }

        if (transparent) {
            if (backgroundColor instanceof Color) {
                Color bgc = (Color) backgroundColor;
                backgroundColor = new Color(bgc.getRed(), bgc.getGreen(), bgc.getBlue(), 0x00);
            } else {
                backgroundColor = OMColor.clear;
            }
        }

        if (Debug.debugging("imageserver")) {
            Debug.output("ImageServerUtils.createOMProjection: projection color: "
                    + (backgroundColor instanceof Color ? Integer.toHexString(((Color) backgroundColor).getRGB())
                            : backgroundColor.toString())
                    + ", transparent("
                    + transparent + ")");
        }

        return backgroundColor;
    }
}