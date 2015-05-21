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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/MultiShapeLayer.java,v $
// $RCSfile: MultiShapeLayer.java,v $
// $Revision: 1.17 $
// $Date: 2008/10/16 03:26:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.awt.Component;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.PropUtils;

/**
 * An OpenMap Layer that displays multiple shape files. Note that the
 * ESRIRecords have been updated so that the OMGraphics that get created from
 * them are loaded with an Integer object that notes the number of the record as
 * it was read from the .shp file. This lets you align the object with the
 * correct attribute data in the .dbf file.
 * 
 * <p>
 * <code><pre>
 *  
 *   ############################
 *   # Properties for a multiple shape file layer
 *   shapeLayer.class=com.bbn.openmap.layer.shape.MultiShapeLayer
 *   shapeLayer.prettyName=Name_for_Menu
 *   shapeLayer.shapeFileList=marker_name1 marker_name2 ...
 *  
 *   shapeLayer.marker_name1.shapeFile=&amp;ltpath to shapefile (.shp)&amp;gt
 *   shapeLayer.marker_name1.spatialIndex=&amp;ltpath to generated spatial index file (.ssx)&amp;gt
 *   shapeLayer.marker_name1.lineColor=ff000000
 *   shapeLayer.marker_name1.fillColor=ff000000
 *   # plus any other properties used by the DrawingAttributes object.
 *   shapeLayer.marker_name1.pointImageURL=&amp;ltURL for image to use for point objects&amp;gt
 *   shapeLayer.marker_name1.enabled=true/false
 *  
 *   shapeLayer.marker_name2.shapeFile=&amp;ltpath to shapefile (.shp)&amp;gt
 *   shapeLayer.marker_name2.spatialIndex=&amp;ltpath to generated spatial index file (.ssx)&amp;gt
 *   shapeLayer.marker_name2.lineColor=ff000000
 *   shapeLayer.marker_name2.fillColor=ff000000
 *   # plus any other properties used by the DrawingAttributes object.
 *   shapeLayer.marker_name2.pointImageURL=&amp;ltURL for image to use for point objects&amp;gt
 *   shapeLayer.marker_name2.enabled=true/false
 *   ############################
 *   
 * </pre></code>
 * 
 * @version $Revision: 1.17 $ $Date: 2008/10/16 03:26:50 $
 * @see SpatialIndex
 */
public class MultiShapeLayer
        extends ShapeLayer {

    public final static String ShapeFileListProperty = "shapeFileList";
    protected Collection<SpatialIndexHandler> spatialIndexes;

    /**
     * Initializes an empty shape layer.
     */
    public MultiShapeLayer() {
        super();
    }

    public void setSpatialIndexes(Collection<SpatialIndexHandler> siv) {
        spatialIndexes = siv;
    }

    public Collection<SpatialIndexHandler> getSpatialIndexes() {
        return spatialIndexes;
    }

    /**
     * This method gets called from setProperties.
     * 
     * @param prefix This prefix has already been scoped, which means it is an
     *        empty string if setProperties was called with a null prefix, or
     *        it's a String ending with a period if it was defined with
     *        characters.
     * @param props Properties containing information about files and the layer.
     */
    protected void setFileProperties(String prefix, Properties props) {
        setSpatialIndexes(prefix, props);
    }

    /**
     * This method gets called from setFileProperties.
     * 
     * @param prefix This prefix has already been scoped, which means it is an
     *        empty string if setProperties was called with a null prefix, or
     *        it's a String ending with a period if it was defined with
     *        characters.
     * @param p Properties containing information about files and the layer.
     */
    protected void setSpatialIndexes(String prefix, Properties p) {

        String shapeFileList = p.getProperty(prefix + ShapeFileListProperty);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(getName() + "| list = \"" + shapeFileList + "\"");
        }

        Vector<String> shapeFileStrings = PropUtils.parseSpacedMarkers(shapeFileList);

        if (shapeFileStrings != null) {

            spatialIndexes = new Vector<SpatialIndexHandler>(shapeFileStrings.size());

            for (String listName : shapeFileStrings) {
                SpatialIndexHandler sih = new SpatialIndexHandler(prefix + listName, p);
                spatialIndexes.add(sih);

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(getName() + ": MultiShapeLayer adding: " + sih);
                }
            }
        } else {
            logger.fine(getName() + ": " + prefix + ShapeFileListProperty + " not set in properties");
        }
    }

    /**
     * PropertyConsumer method.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.remove(prefix + shapeFileProperty);
        props.remove(prefix + pointImageURLProperty);

        Iterator<SpatialIndexHandler> sis = spatialIndexes.iterator();
        StringBuffer list = new StringBuffer();
        while (sis.hasNext()) {
            SpatialIndexHandler sih = sis.next();
            sih.getProperties(props);
            String pp = sih.getPropertyPrefix();
            // Can't be null, if they are part of this layer...
            pp = pp.substring(pp.lastIndexOf('.') + 1);
            list.append(" ").append(pp);
        }

        props.put(prefix + ShapeFileListProperty, list.toString());
        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.).
     * 
     * @param props a Properties object to load the PropertyConsumer properties
     *        into. If getList equals null, then a new Properties object should
     *        be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        props.remove(shapeFileProperty);
        props.remove(pointImageURLProperty);
        props.remove(shapeFileProperty + ScopedEditorProperty);
        props.remove(pointImageURLProperty + ScopedEditorProperty);

        Iterator<SpatialIndexHandler> sis = spatialIndexes.iterator();
        while (sis.hasNext()) {
            sis.next().getPropertyInfo(props);
        }

        props.put(ShapeFileListProperty, "List of marker names for SpatialIndexHandlers");

        return props;
    }

    /**
     * Creates an OMGraphicList containing graphics from all SpatialIndex
     * objects and shapefiles.
     * 
     * @return OMGraphicList containing an OMGraphicList containing shapes from
     *         a particular shape file.
     */
    public synchronized OMGraphicList prepare() {

        if (spatialIndexes == null || spatialIndexes.isEmpty()) {
            logger.fine(getName() + ": spatialIndexes is empty!");
            return new OMGraphicList();
        }

        Projection projection = getProjection();

        if (projection == null) {
            // This can happen if the layer is part of a
            // ScaleFilterLayer, and the redraw button for this layer
            // is pressed before the ScaleFilterLayer gives it a
            // projection (which only happens if the layer is the
            // active one).
            logger.fine(getName() + ": prepare called with null projection");
            return new OMGraphicList();
        }

        Point2D ul = projection.getUpperLeft();
        Point2D lr = projection.getLowerRight();
        double ulLat = ul.getY();
        double ulLon = ul.getX();
        double lrLat = lr.getY();
        double lrLon = lr.getX();

        OMGraphicList masterList = new OMGraphicList();
        OMGraphicList list = null;

        // check for dateline anomaly on the screen. we check for
        // ulLon >= lrLon, but we need to be careful of the check for
        // equality because of floating point arguments...
        if (ProjMath.isCrossingDateline(ulLon, lrLon, projection.getScale())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(getName() + ": Dateline is on screen");
            }

            double ymin = Math.min(ulLat, lrLat);
            double ymax = Math.max(ulLat, lrLat);

            Iterator<SpatialIndexHandler> sii = spatialIndexes.iterator();
            while (sii.hasNext()) {
                SpatialIndexHandler sih = (SpatialIndexHandler) sii.next();
                if (!sih.enabled)
                    continue;

                try {

                    list = sih.getGraphics(ulLon, ymin, 180.0d, ymax, list, projection);
                    list = sih.getGraphics(-180.0d, ymin, lrLon, ymax, list, projection);
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                } catch (FormatException fe) {
                    fe.printStackTrace();
                }
                masterList.add(list);
            }
        } else {

            double xmin = (double) Math.min(ulLon, lrLon);
            double xmax = (double) Math.max(ulLon, lrLon);
            double ymin = (double) Math.min(ulLat, lrLat);
            double ymax = (double) Math.max(ulLat, lrLat);

            Iterator<SpatialIndexHandler> sii = spatialIndexes.iterator();
            while (sii.hasNext()) {
                SpatialIndexHandler sih = (SpatialIndexHandler) sii.next();

                if (!sih.enabled)
                    continue;

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(getName() + ": Getting graphics from " + sih.prettyName + " spatial index");
                }
                try {
                    list = sih.getGraphics(xmin, ymin, xmax, ymax, list, projection);
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                } catch (FormatException fe) {
                    fe.printStackTrace();
                }
                masterList.add(list);
            }
        }

        // OMGraphics already projected in SpatialIndexHandlers
        // if (masterList != null) {
        // masterList.generate(projection, true);//all new graphics
        // }
        return masterList;
    }

    public Component getGUI() {
        if (box == null) {

            box = new JPanel();
            JTabbedPane tabs = new JTabbedPane();

            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setAlignmentX(Component.LEFT_ALIGNMENT);

            Iterator<SpatialIndexHandler> sii = spatialIndexes.iterator();
            while (sii.hasNext()) {
                SpatialIndexHandler sih = sii.next();
                JPanel stuff = (JPanel) sih.getGUI();
                if (stuff != null) {
                    tabs.addTab(sih.getPrettyName(), stuff);
                }
            }

            box.add(tabs);

            JPanel pal2 = new JPanel();
            JButton redraw = new JButton("Redraw Layer");
            redraw.setActionCommand(RedrawCmd);
            redraw.addActionListener(this);
            pal2.add(redraw);
            box.add(pal2);
        }
        return box;
    }

    /**
     * DataBoundsInformer interface.
     */
    public DataBounds getDataBounds() {

        ESRIBoundingBox bounds = new ESRIBoundingBox();

        for (Iterator<SpatialIndexHandler> sii = spatialIndexes.iterator(); sii.hasNext();) {
            SpatialIndexHandler sih = sii.next();
            if (sih != null && sih.spatialIndex != null) {
                ESRIBoundingBox boundingBox = sih.spatialIndex.getBounds();
                if (boundingBox != null) {
                    bounds.addBounds(boundingBox);
                }
            }
        }

        return new DataBounds(bounds.min.x, bounds.min.y, bounds.max.x, bounds.max.y);
    }

}