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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/BufferedShapeLayer.java,v $
// $RCSfile: BufferedShapeLayer.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 18:48:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.awt.event.*;
import java.util.*;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.io.FormatException;

/**
 * An OpenMap Layer that displays shape files. This loads the data up
 * front and then just reprojects/repaints when needed. Note that the
 * ESRIRecords have been updated so that the OMGraphics that get
 * created from them are loaded with an Integer object that notes the
 * number of the record as it was read from the .shp file. This lets
 * you align the object with the correct attribute data in the .dbf
 * file.
 */
public class BufferedShapeLayer extends ShapeLayer {

    /**
     * Initializes an empty shape layer.
     */
    public BufferedShapeLayer() {
        super();
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this));
    }

    /**
     * Sets the SpatialIndex on the layer, and reloads the cached
     * OMGraphicList.
     */
    public void setSpatialIndex(SpatialIndex si) {
        super.setSpatialIndex(si);
        try {
            setList(getWholePlanet());
        } catch (FormatException fe) {
            Debug.error("BufferedShapeLayer.setProperties(): FormatException reading file.\n"
                    + fe.getMessage());
            setList(null);
        }
    }

    /**
     * Get the graphics for the entire planet.
     */
    protected OMGraphicList getWholePlanet() throws FormatException {

        OMGeometryList masterList = new OMGeometryList();

        if (Debug.debugging("shape")) {
            Debug.output(getName() + "|BufferedShapeLayer.getWholePlanet(): "
                    + "fetching all graphics.");
        }
        try {
            ESRIRecord records[] = spatialIndex.locateRecords(-180d,
                    -90d,
                    180d,
                    90d);
            int nRecords = records.length;

            for (int i = 0; i < nRecords; i++) {
                OMGeometry geom = records[i].addOMGeometry(masterList);
                geom.setAppObject(new NumAndBox(records[i].getRecordNumber(), records[i].getBoundingBox()));
            }

        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        } catch (java.lang.NullPointerException npe) {
            Debug.error(getName()
                    + "|BufferedShapeLayer spatial index can't access files.");
        }

        if (Debug.debugging("shape")) {
            Debug.output(getName()
                    + "|BufferedShapeLayer.getWholePlanet(): finished fetch.");
        }

        return masterList;
    }

    public synchronized OMGraphicList prepare() {

        OMGraphicList masterList = getList();

        if (spatialIndex == null)
            return new OMGraphicList();

        try {
            if (masterList == null) {
                masterList = getWholePlanet();
            }
        } catch (FormatException fe) {
            Debug.error(fe.getMessage());
            return masterList;
        }

        // grab local
        Projection proj = getProjection();

        LatLonPoint ul = proj.getUpperLeft();
        LatLonPoint lr = proj.getLowerRight();
        float ulLat = ul.getLatitude();
        float ulLon = ul.getLongitude();
        float lrLat = lr.getLatitude();
        float lrLon = lr.getLongitude();

        drawingAttributes.setTo(masterList);

        // grab local refs
        ESRIPoint min, max;

        double xmin = (double) Math.min(ulLon, lrLon);
        double xmax = (double) Math.max(ulLon, lrLon);
        double ymin = (double) Math.min(ulLat, lrLat);
        double ymax = (double) Math.max(ulLat, lrLat);

        boolean dateLine = false;

        // check for dateline anomaly on the screen. we check
        // for ulLon >= lrLon, but we need to be careful of
        // the check for equality because of floating point
        // arguments...

        if ((ulLon > lrLon)
                || MoreMath.approximately_equal(ulLon, lrLon, .001f)) {
            if (Debug.debugging("shape")) {
                Debug.output("Dateline is on screen");
            }
            dateLine = true;
        }

        Iterator iterator = masterList.iterator();
        while (iterator.hasNext()) {

            OMGeometry geom = (OMGeometry) iterator.next();
            NumAndBox nab = (NumAndBox) geom.getAppObject();

            // If you can test for bounding box intersections,
            // then use the check to see if you can eliminate the
            // object from being drawn. Otherwise, just draw it
            // and let Java clip it.
            geom.setVisible(true);

            if (nab != null) {
                min = nab.getBoundingBox().min;
                max = nab.getBoundingBox().max;

                if (dateLine) {

                    if (!SpatialIndex.intersects(ulLon,
                            ymin,
                            180.0d,
                            ymax,
                            min.x,
                            min.y,
                            max.x,
                            max.y)
                            &&

                            !SpatialIndex.intersects(-180.0d,
                                    ymin,
                                    lrLon,
                                    ymax,
                                    min.x,
                                    min.y,
                                    max.x,
                                    max.y)) {

                        geom.setVisible(false);
                    }
                } else {
                    if (!SpatialIndex.intersects(xmin,
                            ymin,
                            xmax,
                            ymax,
                            min.x,
                            min.y,
                            max.x,
                            max.y)) {
                        geom.setVisible(false);
                    }
                }
            }
        }

        masterList.generate(proj, true);// all new graphics
        return masterList;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == RedrawCmd) {
            setList(null);
        }
        super.actionPerformed(e);
    }

}