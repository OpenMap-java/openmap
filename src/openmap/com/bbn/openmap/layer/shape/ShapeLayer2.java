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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/Attic/ShapeLayer2.java,v $
// $RCSfile: ShapeLayer2.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:11 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.shape;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.*;
import com.bbn.openmap.util.SwingWorker;

/**
 * An OpenMap Layer that displays shape files.  ShapeLayer2 creates
 * OMGeometryLists from the ESRIRecords, which creates a single
 * java.awt.Shape object to render.  This may be faster, but there may
 * also be problems with shape files with graphics that span large
 * areas i.e. worldwide data.)
 *
 * ESRIRecords have been updated so that the OMGraphics that get
 * created from them are loaded with an Integer object that notes the
 * number of the record as it was read from the .shp file.  This lets
 * you align the object with the correct attribute data in the .dbf
 * file.
 * <p>
 * <code><pre>
 * ############################
 * # Properties for a shape layer
 * shapeLayer.class=com.bbn.openmap.layer.shape.ShapeLayer2
 * shapeLayer.prettyName=Name_for_Menu
 * shapeLayer.shapeFile=&ltpath to shapefile (.shp)&gt
 * shapeLayer.spatialIndex=&ltpath to generated spatial index file (.ssx)&gt
 * shapeLayer.lineColor=ff000000
 * shapeLayer.fillColor=ff000000
 * # plus any other properties used by the DrawingAttributes object.
 * shapeLayer.pointImageURL=&ltURL for image to use for point objects&gt
 * ############################
 * </pre></code>
 *
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.2 $ $Date: 2004/01/26 18:18:11 $
 * @see SpatialIndex 
 */
public class ShapeLayer2 extends ShapeLayer implements ActionListener {

    /**
     * Initializes an empty shape layer.
     */
    public ShapeLayer2() {
        super();
    }

    public ShapeLayer2(String shapeFileName) {
        super(shapeFileName);
    }

    /**
     * Add the graphics to one OMGeometryList, which will be
     * created.
     */
    protected OMGeometryList RecordList(ESRIRecord rec) {
        return RecordList(null, rec);
    }

    /**
     * Add all the graphics to one OMGeometryList.
     */
    protected OMGeometryList RecordList(OMGeometryList list, 
                                        ESRIRecord rec) {
        if (list == null) {
            list = new OMGeometryList(10);
        }

        OMGeometry geom = rec.addOMGeometry(list);
        geom.setAppObject(new NumAndBox(rec.getRecordNumber(),
                                        rec.getBoundingBox()));

        return list;
    }

    /**
     * Gets the layer graphics.
     * @return OMGraphicList
     */
    public OMGraphicList prepare() {

        if (spatialIndex == null) {
            Debug.message("shape", "ShapeLayer: spatialIndex is null!");
            return new OMGraphicList();
        }

        Projection projection = getProjection();
        LatLonPoint ul = projection.getUpperLeft();
        LatLonPoint lr = projection.getLowerRight();
        float ulLat = ul.getLatitude();
        float ulLon = ul.getLongitude();
        float lrLat = lr.getLatitude();
        float lrLon = lr.getLongitude();

        OMGeometryList stuff = new OMGeometryList();
        drawingAttributes.setTo(stuff);

        // check for dateline anomaly on the screen.  we check for
        // ulLon >= lrLon, but we need to be careful of the check for
        // equality because of floating point arguments...
        if ((ulLon > lrLon) ||
                MoreMath.approximately_equal(ulLon, lrLon, .001f))
        {
            if (Debug.debugging("shape")) {
                Debug.output("ShapeLayer.computeGraphics(): Dateline is on screen");
            }

            double ymin = (double) Math.min(ulLat, lrLat);
            double ymax = (double) Math.max(ulLat, lrLat);

            try {
                ESRIRecord records1[] = spatialIndex.locateRecords(
                    ulLon, ymin, 180.0d, ymax);
                ESRIRecord records2[] = spatialIndex.locateRecords(
                    -180.0d, ymin, lrLon, ymax);
                int nRecords1 = records1.length;
                int nRecords2 = records2.length;

                for (int i = 0; i < nRecords1; i++) {
                    RecordList(stuff, records1[i]);
                }
                for (int i = 0; i < nRecords2; i++) {
                    RecordList(stuff, records2[i]);
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            } catch (FormatException fe) {
                fe.printStackTrace();
            }
        } else {

            double xmin = (double) Math.min(ulLon, lrLon);
            double xmax = (double) Math.max(ulLon, lrLon);
            double ymin = (double) Math.min(ulLat, lrLat);
            double ymax = (double) Math.max(ulLat, lrLat);

            try {
                ESRIRecord records[] = spatialIndex.locateRecords(
                    xmin, ymin, xmax, ymax);
                int nRecords = records.length;
                for (int i = 0; i < nRecords; i++) {
                    RecordList(stuff, records[i]);
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            } catch (FormatException fe) {
                fe.printStackTrace();
            }
        }

        if (stuff != null) {
            stuff.generate(projection, true);//all new graphics
        }
        return stuff;
    }

}
