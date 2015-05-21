/*
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */

package com.bbn.openmap.dataAccess.shape;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import com.bbn.openmap.geo.ExtentIndex;
import com.bbn.openmap.geo.ExtentIndex.ArrayListExtentIndexImpl;
import com.bbn.openmap.geo.GeoExtent;
import com.bbn.openmap.geo.GeoPath;
import com.bbn.openmap.geo.GeoPoint;
import com.bbn.openmap.geo.GeoRegion;
import com.bbn.openmap.geo.Intersection;
import com.bbn.openmap.geo.MatchCollector;
import com.bbn.openmap.geo.MatchFilter;
import com.bbn.openmap.geo.MatchParameters;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;

/**
 * A GeoExtentIndex that knows how to work with Shape files. Plots the shape
 * file contents in memory in lat/lon space, can then be used with the
 * Intersection class to do spatial analysis on the shape file contents.
 *
 * @author ddietrick
 */
public class ShapeGeoIndex
    extends ArrayListExtentIndexImpl {
    protected HashMap<Object, OMGraphic> omgraphics;

    private ShapeGeoIndex(Builder builder) {
        super(builder.numberOfBuckets, builder.margin);
        omgraphics = new HashMap<Object, OMGraphic>();
        EsriGraphicList graphicList = EsriGraphicList.getEsriGraphicList(builder.shapeFile, null,
            builder.geoCoordTransform);

        if (graphicList != null) {
            load(graphicList);
        }
    }

    /**
     * After you test for intersections with some GeoExtent, you get an iterator of GeoExtents.  You can get the ID
     * from each of those, which will in turn allow you to ask for the OMGraphic representing the shape from the given
     * file. The ID is an integer index into the shape file and attribute dbf file, btw.
     *
     * @param id The id provided by the extent in the intersection test.
     * @return OMGraphic or null (if not found).
     */
    public OMGraphic getForID(Object id) {
        if (omgraphics != null) {
            return omgraphics.get(id);
        }
        return null;
    }

    public void load(EsriGraphicList list) {
        if (list != null) {
            int type = list.getType();

            switch (type) {
                case ShapeConstants.SHAPE_TYPE_POINT:
                case ShapeConstants.SHAPE_TYPE_POINTM:
                case ShapeConstants.SHAPE_TYPE_POINTZ:
                    loadPoints(list);
                    break;
                case ShapeConstants.SHAPE_TYPE_POLYGON:
                case ShapeConstants.SHAPE_TYPE_POLYGONM:
                case ShapeConstants.SHAPE_TYPE_POLYGONZ:
                    loadPolygons(list);
                    break;
                case ShapeConstants.SHAPE_TYPE_POLYLINE:
                case ShapeConstants.SHAPE_TYPE_POLYLINEM:
                case ShapeConstants.SHAPE_TYPE_POLYLINEZ:
                    loadPolylines(list);
                    break;
                default:
                // case ShapeConstants.SHAPE_TYPE_NULL:
            }
        }
    }

    /**
     * @param list assuming list is not null, and has two levels at most -
     *        points, or lists of points.
     */
    private void loadPoints(EsriGraphicList list) {
        for (OMGraphic omg : list) {
            if (omg instanceof OMGraphicList) {

                Object recNum = ((OMGraphicList) omg).getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE);
                for (OMGraphic pnt : (OMGraphicList) omg) {
                    addPoint((OMPoint) pnt, recNum);
                }

            } else {
                addPoint((OMPoint) omg, omg.getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE));
            }
        }

    }

    private void addPoint(OMPoint omp, Object id) {
        double latitude = omp.getLat();
        double longitude = omp.getLon();

        GeoPoint.Impl geoPoint = new GeoPoint.Impl(latitude, longitude);
        geoPoint.setID(id);
        addExtent(geoPoint);
        omgraphics.put(id, omp);
    }

    /**
     * @param list assuming list is not null
     */
    private void loadPolygons(EsriGraphicList list) {
        for (OMGraphic omg : list) {
            if (omg instanceof OMGraphicList) {

                Object recNum = ((OMGraphicList) omg).getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE);
                for (OMGraphic poly : (OMGraphicList) omg) {
                    addPolygon((OMPoly) poly, recNum);
                }

            } else {
                addPolygon((OMPoly) omg, omg.getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE));
            }
        }
    }

    private void addPolygon(OMPoly omp, Object id) {
        double[] latlonArray = omp.getLatLonArray();

        GeoRegion.Impl region = new GeoRegion.Impl(latlonArray, false);
        region.setID(id);
        addExtent(region);
        omgraphics.put(id, omp);
    }

    /**
     * @param list assuming list is not null
     */
    private void loadPolylines(EsriGraphicList list) {
        for (OMGraphic omg : list) {
            if (omg instanceof OMGraphicList) {

                Object recNum = ((OMGraphicList) omg).getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE);
                for (OMGraphic pnt : (OMGraphicList) omg) {
                    addPolyline((OMPoly) pnt, recNum);
                }

            } else {
                addPolyline((OMPoly) omg, omg.getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE));
            }
        }
    }

    private void addPolyline(OMPoly omp, Object id) {
        double[] latlonArray = omp.getLatLonArray();

        GeoPath.Impl region = new GeoPath.Impl(latlonArray);
        region.setID(id);
        addExtent(region);
        omgraphics.put(id, omp);
    }

    /**
     * Get an iterator with all of the objects in this ShapeGeoIndex that
     * intersect with the given extent.
     *
     * @param extent GeoExtent (GeoPoint, GeoPath, GeoRegion) to test against.
     * @return Iterator over intersecting shape objects.
     */
    public Iterator getIntersections(GeoExtent extent) {
        return getIntersections(extent, new MatchFilter.MatchParametersMF(MatchParameters.STRICT),
            new MatchCollector.SetMatchCollector());
    }

    /**
     * Get an iterator with all of the objects in this ShapeGeoIndex that
     * intersect with the given extent.
     *
     * @param extent GeoExtext (GeoPoint, GeoPath, GeoRegion) to test against.
     * @param filter MatchFilter a MatchFilter can eliminate
     * @param collector
     * @return Iterator over intersecting shape objects.
     */
    public Iterator getIntersections(GeoExtent extent, MatchFilter filter, MatchCollector collector) {
        Intersection intersection = Intersection.intersector();

        intersection.consider(extent, this);
        collector = intersection.getCollector();

        return collector.iterator();
    }

    /**
     * Use this class to create a ShapeGeoIndex.
     *
     * @author ddietrick
     */
    public static class Builder {
        private URL shapeFile;
        private GeoCoordTransformation geoCoordTransform;
        private int numberOfBuckets = ExtentIndex.AbstractExtentIndex.D_NBUCKETS;
        private double margin = ExtentIndex.AbstractExtentIndex.D_MARGIN;

        public Builder(URL shape) {
            this.shapeFile = shape;
        }

        public Builder setGeoCoordTransformation(GeoCoordTransformation geoCoordTransformation) {
            this.geoCoordTransform = geoCoordTransformation;
            return this;
        }

        public Builder setNumberOfBuckets(int nBuckets) {
            this.numberOfBuckets = nBuckets;
            return this;
        }

        public Builder setMargin(double mar) {
            this.margin = mar;
            return this;
        }

        public ShapeGeoIndex create() {
            return new ShapeGeoIndex(this);
        }
    }
}
