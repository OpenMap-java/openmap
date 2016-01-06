package com.bbn.openmap.dataAccess.geojson;

import java.util.List;

import com.bbn.openmap.omGraphics.OMAreaList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;

public class MultiPolygon extends Geometry<List<List<LngLatAlt>>> {

    public MultiPolygon() {
    }

    public MultiPolygon(Polygon polygon) {
        add(polygon);
    }

    public MultiPolygon(OMGraphicList omgl) {

    }

    public MultiPolygon add(Polygon polygon) {
        coordinates.add(polygon.getCoordinates());
        return this;
    }

    public OMGraphic convert() {
        OMGraphicList omgl = new OMGraphicList();
        omgl.setVague(true);
        for (List<List<LngLatAlt>> polyPnts : coordinates) {

            if (polyPnts.size() > 1) {
                OMAreaList omal = new OMAreaList();
                for (List<LngLatAlt> innerPolyPnts : polyPnts) {
                    omgl.add(new OMPoly(OMGeoJSONUtil.convertToRadians(innerPolyPnts), OMGraphic.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE));
                }

                omgl.add(omal);
            } else {
                omgl.add(new OMPoly(OMGeoJSONUtil.convertToRadians(polyPnts.get(0)), OMGraphic.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE));
            }

        }
        omgl.getAttributes().putAll(getProperties());
        return omgl;
    }
}
