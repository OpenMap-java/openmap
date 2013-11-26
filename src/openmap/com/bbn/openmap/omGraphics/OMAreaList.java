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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMAreaList.java,v $
// $RCSfile: OMAreaList.java,v $
// $Revision: 1.9 $
// $Date: 2005/01/10 16:58:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.geom.GeneralPath;
import java.io.Serializable;

import com.bbn.openmap.proj.Projection;

/**
 * This class encapsulates a list of OMGeometries that are connected to form one
 * area. Different types of vector OMGeometries can be combined and used to
 * create a unique shape over the map. OMRasterObjects will contribute their
 * shape/size, but not their images. The OMGeometries should be added in a
 * clockwise format.
 * 
 * <P>
 * KNOWN ISSUES: OMAreaLists that wrap around the back of the earth and showing
 * up on both edges of the map are not handled well - you'll end up with lines
 * going horizontally across the map. It's on the to-do list to fix this.
 */
public class OMAreaList extends OMGeometryList implements Serializable {

    /**
     * Construct an OMAreaList.
     */
    public OMAreaList() {
        super(10);
        init();
    }

    /**
     * Construct an OMAreaList with a capacity to be combined from an initial
     * amount of OMGeometries.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public OMAreaList(int initialCapacity) {
        super(initialCapacity);
        init();
    }

    /**
     * Construct an OMAreaList around a List of OMGeometries. The OMAreaList
     * assumes that all the objects on the list are vector OMGeometries, and
     * never does checking. Live with the consequences if you put other stuff in
     * there.
     * 
     * @param list List of vector OMGeometries.
     */
    public OMAreaList(java.util.List<OMGeometry> list) {
        addAll(list);
        init();
    }

    /**
     * Initialization that sets the OMAreaList, which is a modified
     * OMGraphicList, to be vague and constructed in a first added, first used
     * order. connectParts, a variable from OMGeometryList, is also set to true.
     */
    protected void init() {
        setVague(true);
        setTraverseMode(LAST_ADDED_ON_TOP);
        setConnectParts(true);
    }

    /**
     * Create the GeneralPath used for the internal Shape objects held by the
     * OMGeometries added. With the OMAreaList, all of the components are
     * combined to make a single area. So updateShape, which is called from
     * super.generate(), calls appendShapeEdge() instead of appending each part
     * Shape to the main Shape. This method closes off the GeneralPath Shape, so
     * it will be considered a polygon.
     */
    public synchronized boolean generate(Projection p, boolean forceProjectAll) {
        boolean isGenerated = super.generate(p, forceProjectAll);
        GeneralPath projectedShape = getShape();
        if (projectedShape != null) {
            projectedShape.closePath();
        }
        return isGenerated;
    }

    /**
     * Given a OMGeometry, it calls generate/regenerate on it, and then adds the
     * GeneralPath shape within it to the OMGeometryList shape object. The edges
     * of each part are combined to make one big polygon.
     */
    protected void updateShape(OMGeometry geometry, Projection p,
                               boolean forceProject) {
        if (forceProject) {
            geometry.generate(p);
        } else {
            geometry.regenerate(p);
        }

        if (geometry.isVisible()) {
            GeneralPath gp = geometry.getShape();

            if (gp == null) {
                return;
            }

            setShape(appendShapeEdge(getShape(), gp, connectParts));
            // save memory?? by deleting the shape in each part, since
            // they are each contributing to the whole.
            geometry.setShape((GeneralPath) null);
        }
    }

    /**
     * Overrides the OMGeometryList and OMGraphicList methods to just call
     * _distance() on the internal shape object.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param limit the max distance that a graphic has to be within to be
     *        returned, in pixels.
     * @param resetSelect deselect any OMGraphic touched.
     * @return OMDist
     */
    public synchronized OMDist<OMGeometry> findClosest(double x, double y,
                                                       float limit,
                                                       boolean resetSelect) {

        OMDist<OMGeometry> omd = new OMDist<OMGeometry>();
        float currentDistance = Float.MAX_VALUE;

        // cannot select a graphic which isn't visible
        if (!isVisible()) {
            omd.omg = null;
        } else {
            if (resetSelect)
                deselect();
            currentDistance = _distance(x, y);
        }

        if (currentDistance < limit) {
            omd.omg = this;
            omd.d = currentDistance;
        }

        return omd;
    }

}