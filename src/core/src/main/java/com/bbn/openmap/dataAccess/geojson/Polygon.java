package com.bbn.openmap.dataAccess.geojson;

import java.util.Arrays;
import java.util.List;

import com.bbn.openmap.omGraphics.OMAreaList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Polygon extends Geometry<List<LngLatAlt>> {

    public Polygon() {
    }

    public Polygon(List<LngLatAlt> polygon) {
        add(polygon);
    }

    public Polygon(LngLatAlt... polygon) {
        add(Arrays.asList(polygon));
    }

    public Polygon(OMPoly poly) {
        setExteriorRing(OMGeoJSONUtil.convertToJSON(poly.getRawllpts(), true));
    }

    public void setExteriorRing(List<LngLatAlt> points) {
        coordinates.add(0, points);
    }

    @JsonIgnore
    public List<LngLatAlt> getExteriorRing() {
        assertExteriorRing();
        return coordinates.get(0);
    }

    @JsonIgnore
    public List<List<LngLatAlt>> getInteriorRings() {
        assertExteriorRing();
        return coordinates.subList(1, coordinates.size());
    }

    public List<LngLatAlt> getInteriorRing(int index) {
        assertExteriorRing();
        return coordinates.get(1 + index);
    }

    public void addInteriorRing(List<LngLatAlt> points) {
        assertExteriorRing();
        coordinates.add(points);
    }

    public void addInteriorRing(LngLatAlt... points) {
        assertExteriorRing();
        coordinates.add(Arrays.asList(points));
    }

    private void assertExteriorRing() {
        if (coordinates.isEmpty())
            throw new RuntimeException("No exterior ring definied");
    }

    public OMGraphic convert() {
        List<LngLatAlt> outer = getExteriorRing();
        List<List<LngLatAlt>> inners = getInteriorRings();

        OMPoly outerPoly = new OMPoly(OMGeoJSONUtil.convertToRadians(outer), OMGraphic.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE);

        if (!(inners == null || inners.isEmpty())) {
            OMAreaList areaList = new OMAreaList();

            areaList.add(outerPoly);

            for (List<LngLatAlt> polyPnts : inners) {
                areaList.add(new OMPoly(OMGeoJSONUtil.convertToRadians(polyPnts), OMGraphic.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE));
            }
            areaList.getAttributes().putAll(getProperties());
            return areaList;
        }
        outerPoly.getAttributes().putAll(getProperties());
        return outerPoly;
    }

}