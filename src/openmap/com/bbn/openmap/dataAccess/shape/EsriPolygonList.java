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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolygonList.java,v $
// $RCSfile: EsriPolygonList.java,v $
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import java.awt.geom.*;
import java.util.Vector;
import java.util.Iterator;

/**
 * An EsriGraphicList ensures that only EsriPolygons are added to its list.
 * @author Doug Van Auken 
 * @author Don Dietrick
 */
public class EsriPolygonList extends EsriGraphicList {

    /**
     * Over-ride the add( ) method to trap for inconsistent shape
     * geometry.  If you are adding a OMGraphic that is not a list,
     * make sure this list is a sub-list containing multiple geometry
     * parts.  Only add another list to a top level EsriGraphicList.
     * @param shape the non-null OMGraphic to add 
     */
    public void add(OMGraphic shape) {
        try {

            if (shape instanceof OMPoly) {
                shape = EsriPolygon.convert((OMPoly) shape);
                // test for null in next if statement.
            }

            if (shape instanceof OMGraphicList) {
                OMGraphicList list = (OMGraphicList)shape;
                EsriGraphic graphic = (EsriGraphic)list.getOMGraphicAt(0);

                if (graphic instanceof EsriPolygon ||
                    graphic instanceof EsriPolygonList) {
                    graphics.add(shape);
                    addExtents(((EsriGraphicList)shape).getExtents());
                } else if (graphic instanceof OMGraphic) {
                    // Try recursively...
                    add((OMGraphic)graphic);
                } else {
                    Debug.message("esri", "EsriPolygonList.add()- graphic list isn't a EsriPolygonList, can't add.");
                }

            } else if (shape instanceof EsriPolygon) {
                graphics.add(shape);
                addExtents(((EsriPolygon)shape).getExtents());
            } else {
                Debug.message("esri", "EsriPolygonList.add()- graphic isn't a EsriPoly or OMPoly, can't add.");
            }
        } catch (ClassCastException cce) {
        }
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
     * Construct an EsriPolygonList with an initial capacity and
     * a standard increment value.
     *
     * @param initialCapacity the initial capacity of the list 
     * @param capacityIncrement the capacityIncrement for resizing 
     * @deprecated capacityIncrement doesn't do anything.
     */
    public EsriPolygonList(int initialCapacity, int capacityIncrement) {
        super(initialCapacity);
    }

    //converts rectangles into polygons
    public static OMPoly convert(OMRect omRect) {
        float[] rectPoints = new float[10];

        //get the northwest corner
        rectPoints[0] = (omRect.getNorthLat());
        rectPoints[1] = (omRect.getWestLon());
        //get the southwest corner
        rectPoints[2] = (omRect.getSouthLat());
        rectPoints[3] = (omRect.getWestLon());
        //get the southeast corner
        rectPoints[4] = (omRect.getSouthLat());
        rectPoints[5] = (omRect.getEastLon());
        //get the northeast corner
        rectPoints[6] = (omRect.getNorthLat());
        rectPoints[7] = (omRect.getEastLon());
        //get the northwest corner again to close the polygon
        rectPoints[8] = (omRect.getNorthLat());
        rectPoints[9] = (omRect.getWestLon());

        //using the OMRect data create an OMPoly
        OMPoly poly = new OMPoly(rectPoints, omRect.DECIMAL_DEGREES, omRect.getLineType());
        poly.setAppObject(omRect.getAppObject());
        DrawingAttributes da = new DrawingAttributes();
        da.setFrom(omRect);
        da.setTo(poly);
        return poly;
    }

    //converts circles into polygons
    public static OMPoly convert(OMCircle omCircle, Projection proj) {
        GeneralPath shape = omCircle.getShape();

        //get the PathIterator that defines the outline of the circle
        PathIterator circle = shape.getPathIterator(null);
        Vector initialPoints = new Vector();
        float[] segPoints = new float[2];

        while (!circle.isDone()) {
            //by passing segpoints the array is filled with each x\y point
            //iterated by the circle
            int segType = circle.currentSegment(segPoints);
            initialPoints.add(new Float(segPoints[0]));
            initialPoints.add(new Float(segPoints[1]));
            circle.next();
        }

        float[] circlePoints = new float[initialPoints.size()];

        if (proj == null) {
            return null;
        }

        //convert the x/y points to lat/lon points
        for (int p = 0; p < initialPoints.size(); p += 2) {
            LatLonPoint llp = proj.inverse(
                    ((Float)initialPoints.elementAt(p)).intValue(),
                    ((Float)initialPoints.elementAt(p + 1)).intValue());

            circlePoints[p] = llp.getLatitude();
            circlePoints[p + 1] = llp.getLongitude();
        }

        //using the circle data create an OMPoly
        OMPoly poly = new OMPoly(circlePoints, omCircle.DECIMAL_DEGREES, omCircle.getLineType());
        poly.setAppObject(omCircle.getAppObject());
        DrawingAttributes da = new DrawingAttributes();
        da.setFrom(omCircle);
        da.setTo(poly);
        return poly;
    }


    //converts range rings to circles which are passed to the
    //convertCircles() method to be converted to OMPolys
    public static OMGraphicList convert(OMRangeRings omRR, 
                                        Projection proj) {
        //get the array of circles
        OMCircle[] circles = omRR.createCircles();
        OMCircle circ;
        OMGraphicList circleList = new OMGraphicList();
        circleList.setAppObject(omRR.getAppObject());

        //get the line color and fill color that are to be passed with
        //the dbf info
//          Color lineColor = getColorString(dtlGraphic.getLineColor());
//          Color fillColor = getColorString(dtlGraphic.getFillColor());

        if (proj == null) {
            return circleList;
        }

        int i;
        for (i = 0; i < circles.length; i++) {
            //information passed to the dbflist includes the interval
            //units and the interval
//              dbfList = getDbfList("RangeRings(" + omRR.getIntervalUnits().toString() + "s)", omRR.getInterval() * (i + 1), lineColor, fillColor);

            //have to re-generate each circle in the range ring array
            if (circles[i].generate(proj)) {
                //call convertCircles to convert each ring to an OMPoly
                OMPoly poly = convert((OMCircle)circles[i], proj);
                //call the method to add this ring to the EsriLayer
                if (poly != null) {
                    circleList.add(poly);
                }
            } else {
                System.out.println("Could not generate circle from RangeRing");
                return null;
            }
        }

        //the RangeRings.createCircles() method used above only
        //creates the inner circles, therefore the RangeRing object
        //provides the outer ring that must be added to the layer

        //information passed to the dbflist includes the interval
        //units and the interval since we don't know the exact
        //interval of the last ring the string "less than" is applied
        //to the last rings interval
//          dbfList = getDbfList("RangeRings(" + omRR.getIntervalUnits().toString() + ")less than", omRR.getInterval() * (i + 1), lineColor, fillColor);

        DrawingAttributes da = new DrawingAttributes();
        da.setFrom(omRR);
        da.setTo(circleList);

        return circleList;
    }

    public EsriGraphic shallowCopy() {
        EsriPolygonList ret = new EsriPolygonList(size());
        for (Iterator iter = iterator(); iter.hasNext(); ) {
            EsriGraphic g = (EsriGraphic)iter.next();
            ret.add((OMGraphic)g.shallowCopy());
        }
        ret.setAppObject(getAppObject());
        return ret;
    }
}

