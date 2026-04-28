package com.bbn.openmap.dataAccess.geojson;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;


public class MultiPoint extends Geometry<LngLatAlt> {

	public MultiPoint() {
	}

	public MultiPoint(LngLatAlt... points) {
		super(points);
	}

    public MultiPoint(OMGraphicList omgl) {
        for (OMGraphic omg : omgl) {
            if (omg instanceof OMPoint) {
                add(new LngLatAlt(((OMPoint) omg).getLon(), ((OMPoint) omg).getLat()));
            }
        }
    }

    public OMGraphic convert() {
        OMGraphicList omgl = new OMGraphicList();
        omgl.setVague(true);
        for (LngLatAlt pnt : getCoordinates()) {
            omgl.add(new OMPoint(pnt.getLatitude(), pnt.getLongitude()));
        }
        omgl.getAttributes().putAll(getProperties());
        return omgl;
    }
}
