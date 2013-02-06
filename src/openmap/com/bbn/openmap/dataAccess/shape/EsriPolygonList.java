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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolygonList.java,v $
// $RCSfile: EsriPolygonList.java,v $
// $Revision: 1.12 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRangeRings;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * An EsriGraphicList ensures that only EsriPolygons are added to its list.
 * 
 * @author Doug Van Auken
 * @author Don Dietrick
 */
public class EsriPolygonList extends EsriGraphicList {

    /**
     * Over-ride the add( ) method to trap for inconsistent shape geometry. If
     * you are adding a OMGraphic that is not a list, make sure this list is a
     * sub-list containing multiple geometry parts. Only add another list to a
     * top level EsriGraphicList.
     * 
     * @param shape the non-null OMGraphic to add
     */
    public boolean add(OMGraphic shape) {
        boolean ret = false;
        try {

            if (typeMatches(shape)) {
                ret = graphics.add(shape);
                addExtents(((EsriGraphic) shape).getExtents());
            } else if (shape instanceof OMPoly) {
                EsriPolygon eg = convert((OMPoly) shape);
                if (typeMatches(eg)) {
                    ret = graphics.add(eg);
                    addExtents(eg.getExtents());
                }
            } else if (shape instanceof OMGraphicList
                    && !((OMGraphicList) shape).isVague()) {
                for (Iterator<OMGraphic> it = ((OMGraphicList) shape).iterator(); it.hasNext();) {
                    add((OMGraphic) it.next());
                }
                ret = true;
            } else {
                Debug.message("esri",
                        "EsriPolygonList.add()- graphic isn't a EsriPoly or OMPoly, can't add.");
            }
        } catch (ClassCastException cce) {
        }
        return ret;
    }

    public EsriPolygon convert(OMPoly ompoly) {
        return EsriPolygon.convert(ompoly);
    }

    public boolean typeMatches(OMGraphic omg) {
        return (omg instanceof EsriGraphic && ((EsriGraphic) omg).getType() == getType());
    }

    /**
     * Get the list type in ESRI type number form - 5.
     */
    public int getType() {
        return SHAPE_TYPE_POLYGON;
    }

    /**
     * Construct an EsriPolygonList.
     */
    public EsriPolygonList() {
        super();
    }

    /**
     * Construct an EsriPolygonList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public EsriPolygonList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Construct an EsriPolygonList with an initial capacity and a standard
     * increment value.
     * 
     * @param initialCapacity the initial capacity of the list
     * @param capacityIncrement the capacityIncrement for resizing
     * @deprecated capacityIncrement doesn't do anything.
     */
    public EsriPolygonList(int initialCapacity, int capacityIncrement) {
        super(initialCapacity);
    }

    // converts rectangles into polygons
    public static OMPoly convert(OMRect omRect) {
        double[] rectPoints = new double[10];

        // get the northwest corner
        rectPoints[0] = (omRect.getNorthLat());
        rectPoints[1] = (omRect.getWestLon());
        // get the southwest corner
        rectPoints[2] = (omRect.getSouthLat());
        rectPoints[3] = (omRect.getWestLon());
        // get the southeast corner
        rectPoints[4] = (omRect.getSouthLat());
        rectPoints[5] = (omRect.getEastLon());
        // get the northeast corner
        rectPoints[6] = (omRect.getNorthLat());
        rectPoints[7] = (omRect.getEastLon());
        // get the northwest corner again to close the polygon
        rectPoints[8] = (omRect.getNorthLat());
        rectPoints[9] = (omRect.getWestLon());

        // using the OMRect data create an OMPoly
        OMPoly poly = new OMPoly(rectPoints, OMGraphic.DECIMAL_DEGREES, omRect.getLineType());
        poly.setAttributes(omRect.getAttributes());
        DrawingAttributes da = new DrawingAttributes();
        da.setFrom(omRect);
        da.setTo(poly);
        return poly;
    }

    // converts circles into polygons
    public static OMPoly convert(OMCircle omCircle, Projection proj) {
        GeneralPath shape = omCircle.getShape();

        // get the PathIterator that defines the outline of the circle
        PathIterator circle = shape.getPathIterator(null);
        Vector<Float> initialPoints = new Vector<Float>();
        double[] segPoints = new double[2];

        while (!circle.isDone()) {
            // by passing segpoints the array is filled with each x\y
            // point iterated by the circle
            int segType = circle.currentSegment(segPoints);
            initialPoints.add(new Float(segPoints[0]));
            initialPoints.add(new Float(segPoints[1]));
            circle.next();
        }

        double[] circlePoints = new double[initialPoints.size()];

        if (proj == null) {
            return null;
        }

        LatLonPoint.Double llp = new LatLonPoint.Double();
        // convert the x/y points to lat/lon points
        for (int p = 0; p < initialPoints.size(); p += 2) {
            proj.inverse(((Float) initialPoints.elementAt(p)).doubleValue(),
                    ((Float) initialPoints.elementAt(p + 1)).doubleValue(),
                    llp);

            circlePoints[p] = (float) llp.getRadLat();
            circlePoints[p + 1] = (float) llp.getRadLon();
        }

        // using the circle data create an OMPoly
        OMPoly poly = new OMPoly(circlePoints, OMGraphic.RADIANS, omCircle.getLineType());
        poly.setAttributes(omCircle.getAttributes());
        DrawingAttributes da = new DrawingAttributes();
        da.setFrom(omCircle);
        da.setTo(poly);
        return poly;
    }

    // converts range rings to circles which are passed to the
    // convertCircles() method to be converted to OMPolys
    public static OMGraphicList convert(OMRangeRings omRR, Projection proj) {
        // get the array of circles
        OMCircle[] circles = omRR.createCircles();
        OMGraphicList circleList = new OMGraphicList();
        circleList.setAttributes(omRR.getAttributes());

        // get the line color and fill color that are to be passed
        // with
        // the dbf info
        // Color lineColor =
        // getColorString(dtlGraphic.getLineColor());
        // Color fillColor =
        // getColorString(dtlGraphic.getFillColor());

        if (proj == null) {
            return circleList;
        }

        for (int i = 0; i < circles.length; i++) {
            // information passed to the dbflist includes the interval
            // units and the interval
            // dbfList = getDbfList("RangeRings(" +
            // omRR.getIntervalUnits().toString() + "s)",
            // omRR.getInterval() * (i + 1), lineColor, fillColor);

            // have to re-generate each circle in the range ring array
            if (circles[i].generate(proj)) {
                // call convertCircles to convert each ring to an
                // OMPoly
                OMPoly poly = convert((OMCircle) circles[i], proj);
                // call the method to add this ring to the EsriLayer
                if (poly != null) {
                    circleList.add(poly);
                }
            } else {
                System.out.println("Could not generate circle from RangeRing");
                return null;
            }
        }

        // the RangeRings.createCircles() method used above only
        // creates the inner circles, therefore the RangeRing object
        // provides the outer ring that must be added to the layer

        // information passed to the dbflist includes the interval
        // units and the interval since we don't know the exact
        // interval of the last ring the string "less than" is applied
        // to the last rings interval
        // dbfList = getDbfList("RangeRings(" +
        // omRR.getIntervalUnits().toString() + ")less than",
        // omRR.getInterval() * (i + 1), lineColor, fillColor);

        DrawingAttributes da = new DrawingAttributes();
        da.setFrom(omRR);
        da.setTo(circleList);

        return circleList;
    }

    public EsriGraphic shallowCopy() {
        EsriPolygonList ret = new EsriPolygonList(size());
        for (Iterator<OMGraphic> iter = iterator(); iter.hasNext();) {
            EsriGraphic g = (EsriGraphic) iter.next();
            ret.add((OMGraphic) g.shallowCopy());
        }
        ret.setAttributes(getAttributes());
        return ret;
    }
}
