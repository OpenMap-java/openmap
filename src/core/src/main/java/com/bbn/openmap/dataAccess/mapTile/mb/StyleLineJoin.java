package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.BasicStroke;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public enum StyleLineJoin {

	BEVEL("bevel", BasicStroke.JOIN_BEVEL),
	ROUND("round", BasicStroke.JOIN_ROUND),
	MITER("miter", BasicStroke.JOIN_MITER);

	String name;
	int val;

	private StyleLineJoin(String name, int basicStrokeVal) {
		this.name = name;
		this.val = basicStrokeVal;
	}

	public static StyleLineJoin getForNode(JsonNode node) {
		JsonNode slp = node.get(StyleDrawingAttributes.LINE.LINE_JOIN);
		if (slp != null) {
			return getForName(slp.asText());
		}
		return null;
	}

	public static StyleLineJoin getForName(String nm) {
		for (StyleLineJoin st : StyleLineJoin.values()) {
			if (nm.contains(st.name)) {
				return st;
			}
		}
		return null;
	}

	public static StyleFunction<StyleLineJoin> getFunction(JsonNode node) {
		StyleLineJoin dflt = MITER;
		if (node != null) {
			if (node.has("stops")) {

				JsonNode stopsNode = node.withArray("stops");
				if (stopsNode != null) {
					// System.out.println(node);
					Iterator<JsonNode> stops = stopsNode.iterator();
					if (stops != null) {
						StyleLineJoin base = getForName(StyleNode.getAsText(node, "base", "butt"));
						StyleFunction<StyleLineJoin> styleFunction = new StyleFunction<StyleLineJoin>(base, dflt);
						while (stops.hasNext()) {
							JsonNode stop = stops.next();

							if (stop.isArray()) {
								int zoomLevel = stop.get(0).asInt();
								StyleLineJoin val = getForName(stop.get(1).asText());

								styleFunction.add(zoomLevel, val);
							}
						}
						return styleFunction;
					}
				}
			} else {
				StyleLineJoin val = getForNode(node);
				if (val != null) {
					return new StyleFunction<StyleLineJoin>(val);
				}
			}
		}

		return new StyleFunction<StyleLineJoin>(dflt);
	}

}
