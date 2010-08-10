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
// $Revision: 1.11 $
// $Date: 2008/10/16 03:26:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.shape.SpatialIndex.Entry;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;

/**
 * An OpenMap Layer that displays shape files. This loads the data up front and
 * then just reprojects/repaints when needed. Note that the ESRIRecords have
 * been updated so that the OMGraphics that get created from them are loaded
 * with an Integer object that notes the number of the record as it was read
 * from the .shp file. This lets you align the object with the correct attribute
 * data in the .dbf file.
 */
public class BufferedShapeLayer extends ShapeLayer {

   private static final long serialVersionUID = 1L;
   protected OMGraphicList bufferedList = null;

    /**
     * Initializes an empty shape layer.
     */
    public BufferedShapeLayer() {
        super();
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this));
    }

    /**
     * Creates a ShapeLayer that reads the give shape file.
     * 
     * @param pathToShpFile
     */
    public BufferedShapeLayer(String pathToShpFile) {
        super(pathToShpFile);
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this));
    }

    /**
     * Get the graphics for the entire planet.
     */
    protected OMGraphicList getWholePlanet() throws IOException,
            FormatException {
        spatialIndex.readIndexFile(null, coordTransform);
        return spatialIndex.getAllOMGraphics((OMGraphicList) null,
                drawingAttributes,
                (Projection) null,
                coordTransform);
    }

    /**
     * This overridden method checks to see if the buffered OMGraphicList is
     * created, and then returns a subset of OMGraphics that are actually on the
     * map. If the buffered OMGraphicList hasn't been created yet, it gets
     * created here.
     */
    public synchronized OMGraphicList prepare() {

        OMGraphicList list = getList();

        if (list != null) {
            list.clear();
        } else {
            list = new OMGraphicList();
        }

        if (spatialIndex == null)
            return list;

        try {
            if (bufferedList == null) {
                bufferedList = getWholePlanet();
            }
        } catch (FormatException fe) {
            logger.warning(fe.getMessage());
            return list;
        } catch (IOException ioe) {
            logger.warning(ioe.getMessage());
            return list;
        }

        // grab local
        Projection proj = getProjection();

        Point2D ul = proj.getUpperLeft();
        Point2D lr = proj.getLowerRight();
        double ulLat = ul.getY();
        double ulLon = ul.getX();
        double lrLat = lr.getY();
        double lrLon = lr.getX();

        // check for dateline anomaly on the screen. we check for
        // ulLon >= lrLon, but we need to be careful of the check for
        // equality because of floating point arguments...
        if (ProjMath.isCrossingDateline(ulLon, lrLon, proj.getScale())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(getName() + ": Dateline is on screen");
            }

            double ymin = Math.min(ulLat, lrLat);
            double ymax = Math.max(ulLat, lrLat);

            checkSpatialIndexEntries(ulLon, ymin, 180.0d, ymax, list, proj);
            checkSpatialIndexEntries(-180.0d, ymin, lrLon, ymax, list, proj);

        } else {

            double xmin = Math.min(ulLon, lrLon);
            double xmax = Math.max(ulLon, lrLon);
            double ymin = Math.min(ulLat, lrLat);
            double ymax = Math.max(ulLat, lrLat);
            checkSpatialIndexEntries(xmin, ymin, xmax, ymax, list, proj);
        }

        return list;
    }

    protected void checkSpatialIndexEntries(double xmin, double ymin,
                                            double xmax, double ymax,
                                            OMGraphicList retList,
                                            Projection proj) {
        // There should be the same number of objects in both iterators.
        Iterator<?> entryIt = spatialIndex.entries.iterator();
        Iterator<?> omgIt = bufferedList.iterator();

        OMGraphicList labels = null;
        if (spatialIndex.getDbf() != null) {
            labels = new OMGraphicList();
            retList.add(labels);
        }

        while (entryIt.hasNext() && omgIt.hasNext()) {
            Entry entry = (Entry) entryIt.next();
            OMGraphic omg = (OMGraphic) omgIt.next();
            if (entry.intersects(xmin, ymin, xmax, ymax)) {
                // We want to set attributes before the evaluate method is
                // called, since there might be special attributes set on the
                // omg based on dbf contents.
                drawingAttributes.setTo(omg);
                omg = spatialIndex.evaluate(omg, labels, proj);

                // omg can be null from the evaluate method, if the omg doesn't
                // pass proj and rule tests.
                if (omg != null) {
                    omg.generate(proj);
                    retList.add(omg);
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == RedrawCmd) {
            setList(null);
        }
        super.actionPerformed(e);
    }
    
    /**
     * This method gets called from setProperties.
     * 
     * @param realPrefix
     *            This prefix has already been scoped, which means it is an
     *            empty string if setProperties was called with a null prefix,
     *            or it's a String ending with a period if it was defined with
     *            characters.
     * @param props
     *            Properties containing information about files and the layer.
     */
    protected void setFileProperties(String realPrefix, Properties props) {
       bufferedList = null;
       super.setFileProperties(realPrefix, props);
    }
    
    public void setSpatialIndex(SpatialIndex si) {
       bufferedList = null;
       super.setSpatialIndex(si);
   }
}