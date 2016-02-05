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

	private String featureTypeName;

	private FeatureGeometryType(String featureTypeName) {
		this.featureTypeName = featureTypeName;
	}

	public static FeatureGeometryType forName(String name) {
		for (FeatureGeometryType fgt : FeatureGeometryType.values()) {
			if (fgt.featureTypeName.equalsIgnoreCase(name)) {
				return fgt;
			}
		}
		return UNKNOWN;
	}

}
