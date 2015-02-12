package com.bbn.openmap.layer.geojson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

public class GeometryCollection extends GeoJsonObject implements Iterable<GeoJsonObject> {

	private List<GeoJsonObject> geometries = new ArrayList<GeoJsonObject>();

	public List<GeoJsonObject> getGeometries() {
		return geometries;
	}

	public void setGeometries(List<GeoJsonObject> geometries) {
		this.geometries = geometries;
	}

	public Iterator<GeoJsonObject> iterator() {
		return geometries.iterator();
	}

	public GeometryCollection add(GeoJsonObject geometry) {
		geometries.add(geometry);
		return this;
	}

    public OMGraphic convert() {
        OMGraphicList ret = new OMGraphicList();
        for (GeoJsonObject jgo : getGeometries()) {
            ret.add(jgo.convert());
        }
        ret.getAttributes().putAll(getProperties());
        return ret;
    }

}
