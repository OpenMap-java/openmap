package com.bbn.openmap.layer.geojson;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoly;

public class LineString extends MultiPoint {

    public LineString() {
    }

    public LineString(LngLatAlt... points) {
        super(points);
    }

    public LineString(OMPoly poly) {
        for (LngLatAlt lla : OMGeoJSONUtil.convertToJSON(poly.getRawllpts(), true)) {
            add(lla);
        }
    }

    public LineString(OMLine line) {
        double[] ll = line.getLL();
        add(new LngLatAlt(ll[1], ll[0]));
        add(new LngLatAlt(ll[3], ll[2]));
    }

    public OMGraphic convert() {
        OMPoly poly = new OMPoly(OMGeoJSONUtil.convertToRadians(getCoordinates()), OMGraphic.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE);
        poly.getAttributes().putAll(getProperties());
        return poly;
    }

}
