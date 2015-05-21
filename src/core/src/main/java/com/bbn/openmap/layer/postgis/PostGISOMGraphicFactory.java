/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

import org.postgis.Geometry;
import org.postgis.GeometryCollection;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Projection;

/**
 * Creates OMGraphics from PostGIS/PostGRES geometries. If you pass in a
 * projection and DrawingAttributes, it will set those on the resulting
 * OMGraphics as they are processed.
 * 
 * @author dietrick
 */
public class PostGISOMGraphicFactory {

    Projection proj;
    DrawingAttributes drawingAttributes;

    /**
     * Create a factory to process PostGIS geometries.
     * 
     * @param projection the OpenMap projection to use to generate OMGraphics.
     * @param da the DrawingAttributes to set on the OMGraphics.
     */
    public PostGISOMGraphicFactory(Projection projection, DrawingAttributes da) {
        this.proj = projection;
        this.drawingAttributes = da;
    }

    /**
     * Main call to transform Geometry into OMGraphics.
     * 
     * @param geometry PostGIS geometry
     * @return OMGraphic
     */
    public OMGraphic transformGeometryToOM(Geometry geometry) {
        int type = geometry.getType();
        switch (type) {
        case Geometry.POINT:
            return transformPointToOM((Point) geometry);
        case Geometry.LINESTRING:
            return transformLineStringToOM((LineString) geometry);
        case Geometry.POLYGON:
            return transformPolygonToOM((Polygon) geometry);
        case Geometry.MULTIPOINT:
            return transformMultiPointToOM((MultiPoint) geometry);
        case Geometry.MULTILINESTRING:
            return trasformMultiLineStringToOM((MultiLineString) geometry);
        case Geometry.MULTIPOLYGON:
            return transformMultiPolygonToOM((MultiPolygon) geometry);
        case Geometry.GEOMETRYCOLLECTION:
            return transformGeometryCollectionToOM((GeometryCollection) geometry);
        default:
            throw new IllegalArgumentException("Unknown Geometry Type: " + type);
        }

    }

    /**
     * Transform a GeometryCollection to an OMGraphicList, iterating through the
     * collection and converting the contents to OMGraphics.
     * 
     * @param collection GeometryCollection
     * @return OMGraphicList
     */
    private OMGraphicList transformGeometryCollectionToOM(GeometryCollection collection) {
        Geometry[] geometries = collection.getGeometries();
        OMGraphicList list = new OMGraphicList(geometries.length);
        for (int k = 0; k < geometries.length; k++) {
            int type = geometries[k].getType();
            switch (type) {
            case Geometry.POINT:
                list.add(transformPointToOM((Point) geometries[k]));
                break;
            case Geometry.LINESTRING:
                list.add(transformLineStringToOM((LineString) geometries[k]));
                break;
            case Geometry.POLYGON:
                list.add(transformPolygonToOM((Polygon) geometries[k]));
                break;
            case Geometry.MULTIPOINT:
                list.add(transformMultiPointToOM((MultiPoint) geometries[k]));
                break;
            case Geometry.MULTILINESTRING:
                list.add(trasformMultiLineStringToOM((MultiLineString) geometries[k]));
                break;
            case Geometry.MULTIPOLYGON:
                list.add(transformMultiPolygonToOM((MultiPolygon) geometries[k]));
                break;
            case Geometry.GEOMETRYCOLLECTION:
                list.add(transformGeometryCollectionToOM((GeometryCollection) geometries[k]));
                break;
            default:
                throw new IllegalArgumentException("Unknown Geometry Type: " + type);
            }
        }
        return list;
    }

    /**
     * Apply DrawingAttributes and generate with projection, if they have been
     * provided.
     * 
     * @param omg OMGraphic to process
     * @return OMGraphic
     */
    private <T extends OMGraphic> T process(T omg) {
        if (drawingAttributes != null) {
            drawingAttributes.setTo(omg);
        }
        if (proj != null) {
            omg.generate(proj);
        }
        return omg;
    }

    private OMPoint transformPointToOM(Point point) {
        return process(new OMPoint(point.y, point.x));
    }

    private OMPoly transformLineStringToOM(LineString lineString) {
        Point[] points = lineString.getPoints();
        int count = points.length;

        OMPoly ompoly = null;

        double[] lineCoords = new double[count * 2];
        for (int k = 0; k < count; k++) {
            Point p = points[k];
            final double x = p.getX();
            final double y = p.getY();

            lineCoords[2 * k] = y;
            lineCoords[2 * k + 1] = x;
        }

        return process(new OMPoly(lineCoords, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT));
    }

    /**
     * Creates OMGraphics from Polygon.
     * 
     * @param polygon
     * @return OMPoly if polygon has 0 rings, OMGraphicList if there are more.
     */
    private OMGraphic transformPolygonToOM(Polygon polygon) {
        int nRings = polygon.numRings();
        OMGraphicList list = new OMGraphicList(nRings);

        for (int i = 0; i < nRings; i++) {
            Point[] ringPoints = polygon.getRing(i).getPoints();
            int size = ringPoints.length;

            double[] ringCoords = new double[size * 2];
            for (int k = 0; k < size; k++) {
                Point p = ringPoints[k];
                final double x = p.getX();
                final double y = p.getY();

                ringCoords[2 * k] = y;
                ringCoords[2 * k + 1] = x;
            }

            OMPoly poly = process(new OMPoly(ringCoords, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT));

            if (nRings == 0) {
                return poly;
            }

            list.add(poly);
        }

        return list;
    }

    private OMGraphicList transformMultiPointToOM(MultiPoint multiPoint) {
        Point[] points = multiPoint.getPoints();
        OMGraphicList list = new OMGraphicList(points.length);
        for (Point p : points) {
            list.add(process(new OMPoint(p.getY(), p.getX())));
        }
        return list;
    }

    private OMGraphicList trasformMultiLineStringToOM(MultiLineString multiLineString) {
        LineString[] lines = multiLineString.getLines();
        OMGraphicList list = new OMGraphicList(lines.length);
        for (LineString lineString : lines) {
            list.add(transformLineStringToOM(lineString));
        }
        return list;
    }

    private OMGraphicList transformMultiPolygonToOM(MultiPolygon multiPolygon) {
        Polygon[] polygons = multiPolygon.getPolygons();
        OMGraphicList list = new OMGraphicList(polygons.length);
        for (Polygon polygon : polygons) {
            list.add(transformPolygonToOM(polygon));
        }
        return list;
    }
}
