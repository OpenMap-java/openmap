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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/OM3DConstants.java,v $
// $RCSfile: OM3DConstants.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/11 19:27:04 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

/**
 * An interface that any class can implement to get access to common
 * constant values.
 * 
 * @author dietrick
 */
public interface OM3DConstants {

    public final static Point3d ORIGIN = new Point3d(0, 0, 0);
    public final static double BACK_CLIP_DISTANCE = 10000.0;
    public final static BoundingSphere LIGHT_BOUNDS = new BoundingSphere(ORIGIN, BACK_CLIP_DISTANCE);

    /**
     * Standard light color.
     */
    public final static Color3f White = new Color3f(1, 1, 1);

    /**
     * Default bounds dimension.
     */
    public final static double DEFAULT_BOUNDS_DIMENSION = 1000.0;

    /**
     * Default size of the window edge.
     */
    public final static int DEFAULT_DIMENSION = 400;

    /**
     * Mask for Content. To tell them to look for any layers that are
     * OMGraphicHandlers, and add those layers' objects to the scene.
     */
    public final static int CONTENT_MASK_OMGRAPHICHANDLERLAYERS = 1 << 0;
    /**
     * Mask for Content. Tells the Content to look for
     * OM3DGraphicHandlers in the MapHandler. Any OM3dGraphicHandler
     * is given a reference to the Content, and the OM3DGraphicHandler
     * can call methods on the content to add graphics and shapes to
     * the scene.
     */
    public final static int CONTENT_MASK_OM3DGRAPHICHANDLERS = 1 << 1;
    /**
     * Mask for Content. To tell them to take all the standard OpenMap
     * layers and incorporate them into an image.
     */
    public final static int CONTENT_MASK_IMAGEMAP = 1 << 2;
    public final static int CONTENT_MASK_ELEVATIONMAP = 1 << 3;

}