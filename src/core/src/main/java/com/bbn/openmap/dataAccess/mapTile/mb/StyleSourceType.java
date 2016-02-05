package com.bbn.openmap.dataAccess.mapTile.mb;

import com.fasterxml.jackson.databind.JsonNode;

public enum StyleSourceType {
	VECTOR("vector"), RASTER("raster"), GEOJSON("geojson"), IMAGE("image"), VIDEO("video"), UNKNOWN("unknown");

	private static String TYPE = "type";
	
	private String name;

	private StyleSourceType(String name) {
		this.name = name;
	}

	public static StyleSourceType getForNode(JsonNode node) {
		return getForName(node.get(TYPE).toString());
	}

	public static StyleSourceType getForName(String nm) {
		for (StyleSourceType st : StyleSourceType.values()) {
			if (st.name.equalsIgnoreCase(nm)) {
				return st;
			}
		}
		return UNKNOWN;
	}
}
