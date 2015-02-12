package com.bbn.openmap.layer.geojson;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;

public class Point extends GeoJsonObject {

    private LngLatAlt coordinates;

    public Point() {
    }

    public Point(LngLatAlt coordinates) {
        this.coordinates = coordinates;
    }

    public Point(double longitude, double latitude) {
        coordinates = new LngLatAlt(longitude, latitude);
    }

    public Point(double longitude, double latitude, double altitude) {
        coordinates = new LngLatAlt(longitude, latitude, altitude);
    }

    public Point(OMPoint omPoint) {
        this(omPoint.getLon(), omPoint.getLat());
    }

    public LngLatAlt getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LngLatAlt coordinates) {
        this.coordinates = coordinates;
    }

    public OMGraphic convert() {
        double lat = coordinates.getLatitude();
        double lon = coordinates.getLongitude();
        OMPoint point = new OMPoint(lat, lon);
        point.getAttributes().putAll(getProperties());
        return point;
    }
}