/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.geojson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bbn.openmap.omGraphics.OMAreaList;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRangeRings;
import com.bbn.openmap.omGraphics.OMRect;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Conversion methods for GeoJSON to OpenMap.
 *
 * @author dietrick
 */
public class OMGeoJSONUtil {

    public static double[] convertToRadians(List<LngLatAlt> lla) {

        double[] ret = new double[lla.size() * 2];
        int index = 0;
        for (LngLatAlt coord : lla) {
            ret[index++] = Math.toRadians(coord.getLatitude());
            ret[index++] = Math.toRadians(coord.getLongitude());
        }

        return ret;
    }

    public static double[] convertToDegrees(List<LngLatAlt> lla) {

        double[] ret = new double[lla.size() * 2];
        int index = 0;
        for (LngLatAlt coord : lla) {
            ret[index++] = coord.getLatitude();
            ret[index++] = coord.getLongitude();
        }

        return ret;
    }

    /**
     * Convert coord double[] to list of LngLatAlt.
     * 
     * @param coords lat, lon order
     * @param isRadians true if radians, false for degrees.
     * @return List of LngLatAlt
     */
    public static List<LngLatAlt> convertToJSON(double[] coords, boolean isRadians) {
        List<LngLatAlt> ret = new ArrayList<LngLatAlt>(coords.length / 2);
        for (int index = 0; index < coords.length - 1; index += 2) {
            double lat = coords[index];
            double lon = coords[index + 1];

            if (isRadians) {
                lat = Math.toDegrees(lat);
                lon = Math.toDegrees(lon);
            }

            ret.add(new LngLatAlt(lon, lat));
        }

        return ret;
    }

    /**
     * Method to call if you have an OMGraphic/OMGraphicList and want to convert
     * it to GeoJSON. There are restrictions to what can be converted. OMPolys
     * are converted to Polygons, OMLine and OMPolys that are open are converted
     * to LineStrings, OMPoints are converted to Points. OMCircles and OMRects
     * are converted to Polygons. Vague OMGraphicLists containing OMPolys or
     * open OMPolys or OMLines or OMPoints are converted to MultiPolygon,
     * MultiLineString or MultiPoint respectively. Anything that can't be
     * handled is ignored.
     * <P>
     * 
     * From here, all you need to do to write out JSON is this:
     * 
     * <pre>
     * String json = new ObjectMapper().writeValueAsString(fCollection);
     * </pre>
     * 
     * @param omg OMGraphic to convert
     * @return FeatureCollection.
     */
    public static FeatureCollection convert(OMGraphic omg) {

        FeatureCollection featureCollection = new FeatureCollection();

        if (omg instanceof OMGraphicList) {
            Map<String, Object> properties = convert(omg.getAttributes());
            featureCollection.setProperties(properties);
        }
        convert(omg, featureCollection);

        return featureCollection;
    }

    /**
     * Used to convert the OMGraphics attribute table to the properties JSON
     * wants.
     * 
     * @param attributes Map<Object, Object>
     * @return Map<String, Object>
     */
    public static Map<String, Object> convert(Map<Object, Object> attributes) {
        Map<String, Object> ret = new HashMap<String, Object>();
        for (Object key : attributes.keySet()) {
            ret.put(key.toString(), attributes.get(key));
        }
        return ret;
    }

    /**
     * Method to call if you have an OMGraphic/OMGraphicList and want to add
     * contents to a particular FeatureCollection as GeoJSON.
     * 
     * @param omg OMGraphic to convert.
     * @param featureCollection the collection to add new Features to.
     */
    public static void convert(OMGraphic omg, FeatureCollection featureCollection) {

        if (omg instanceof OMAreaList) {
            // Is a polygon with interior rings
        } else if (omg instanceof OMGraphicList) {
            OMGraphicList omgl = (OMGraphicList) omg;
            if (omgl.isVague() && omgl.size() > 0) {
                OMGraphic vomg = omgl.get(0);
                if (vomg instanceof OMPoly) {
                    if (((OMPoly) vomg).isPolygon()) {
                        featureCollection.add(new MultiPolygon(omgl), omgl);
                    } else {
                        featureCollection.add(new MultiLineString(omgl), omgl);
                    }
                } else if (vomg instanceof OMLine) {
                    featureCollection.add(new MultiLineString(omgl), omgl);
                } else if (vomg instanceof OMPoint) {
                    featureCollection.add(new MultiPoint(omgl), omgl);
                }

            } else {
                // Each OMGraphic needs to be made into a Feature and added to
                // main list.
                for (OMGraphic omg1 : (OMGraphicList) omgl) {
                    convert(omg1, featureCollection);
                }
            }
        } else {
            // Create the Feature from the OMGraphic and add it to the
            // collection
            if (omg instanceof OMPoly) {
                if (((OMPoly) omg).isPolygon()) {
                    featureCollection.add(new Polygon((OMPoly) omg), omg);
                } else {
                    featureCollection.add(new LineString((OMPoly) omg), omg);
                }
            } else if (omg instanceof OMPoint) {
                featureCollection.add(new Point((OMPoint) omg), omg);
            } else if (omg instanceof OMLine) {
                featureCollection.add(new LineString((OMLine) omg), omg);
            } else if (omg instanceof OMRangeRings) {

            } else if (omg instanceof OMCircle) {

            } else if (omg instanceof OMRect) {

            }
        }
    }
}
