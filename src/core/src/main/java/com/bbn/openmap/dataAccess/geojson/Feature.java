package com.bbn.openmap.dataAccess.geojson;

import com.bbn.openmap.omGraphics.OMGraphic;

public class Feature extends GeoJsonObject {

	private GeoJsonObject geometry;
	private String id;

	public GeoJsonObject getGeometry() {
		return geometry;
	}

	public void setGeometry(GeoJsonObject geometry) {
		this.geometry = geometry;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OMGraphic convert() {
		OMGraphic omg = getGeometry().convert();
		omg.getAttributes().putAll(getProperties());
		return omg;
	}
}
