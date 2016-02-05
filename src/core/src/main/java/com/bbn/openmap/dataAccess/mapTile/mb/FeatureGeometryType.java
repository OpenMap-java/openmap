package com.bbn.openmap.dataAccess.mapTile.mb;

public enum FeatureGeometryType {

	POINT("Point"),
	MULTI_POINT("MultiPoint"),
	LINEAR_RING("LinearRing"),
	LINE_STRING("LineString"),
	MULTI_LINE_STRING("MultiLineString"),
	POLYGON("Polygon"),
	MULTI_POLYGON("MultiPolygon"),
	UNKNOWN("Unknown");

	private String featureType;

	private FeatureGeometryType(String featureTypeName) {
		this.featureType = featureTypeName;
	}

	public static FeatureGeometryType get(String type) {
		for (FeatureGeometryType fgt : FeatureGeometryType.values()) {
			if (fgt.featureType.equalsIgnoreCase(type)) {
				return fgt;
			}
		}
		return UNKNOWN;
	}

}
