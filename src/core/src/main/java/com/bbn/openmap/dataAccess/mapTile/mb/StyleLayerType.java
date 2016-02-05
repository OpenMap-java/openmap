package com.bbn.openmap.dataAccess.mapTile.mb;

import com.fasterxml.jackson.databind.JsonNode;

public enum StyleLayerType {

	FILL("fill"),
	LINE("line"),
	SYMBOL("symbol"),
	CIRCLE("circle"),
	RASTER("raster"),
	BACKGROUND("background"),
	UNKNOWN("unknown");

	private static String TYPE = "type";

	String name;

	private StyleLayerType(String name) {
		this.name = name;
	}

	public static StyleLayerType getFromLayerNode(JsonNode layerNode) {
		return getForName(layerNode.get(TYPE).toString());
	}

	public static StyleLayerType getForName(String nm) {
		for (StyleLayerType slt : StyleLayerType.values()) {
			if (nm.contains(slt.name)) {
				return slt;
			}
		}
		return UNKNOWN;
	}
}
