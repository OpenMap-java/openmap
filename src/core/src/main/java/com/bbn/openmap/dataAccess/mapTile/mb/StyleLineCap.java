package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.BasicStroke;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public enum StyleLineCap {

	BUTT("butt", BasicStroke.CAP_BUTT), ROUND("round", BasicStroke.CAP_ROUND), SQUARE("square", BasicStroke.CAP_SQUARE);

	String name;
	int val;

	private StyleLineCap(String name, int basicStrokeVal) {
		this.name = name;
		this.val = basicStrokeVal;
	}

	public static StyleLineCap getForNode(JsonNode node) {
		JsonNode slp = node.get(StyleDrawingAttributes.LINE.LINE_CAP);
		if (slp != null) {
			return getForName(slp.asText());
		}
		return null;
	}

	public static StyleLineCap getForName(String nm) {
		for (StyleLineCap st : StyleLineCap.values()) {
			if (nm.contains(st.name)) {
				return st;
			}
		}
		return null;
	}

	public static StyleFunction<StyleLineCap> getFunction(JsonNode node) {
		StyleLineCap dflt = BUTT;
		if (node != null) {
			if (node.has("stops")) {

				JsonNode stopsNode = node.withArray("stops");
				if (stopsNode != null) {
					// System.out.println(node);
					Iterator<JsonNode> stops = stopsNode.iterator();
					if (stops != null) {
						StyleLineCap base = getForName(StyleNode.getAsText(node, "base", "butt"));
						StyleFunction<StyleLineCap> styleFunction = new StyleFunction<StyleLineCap>(base, dflt);
						while (stops.hasNext()) {
							JsonNode stop = stops.next();

							if (stop.isArray()) {
								int zoomLevel = stop.get(0).asInt();
								StyleLineCap val = getForName(stop.get(1).asText());

								styleFunction.add(zoomLevel, val);
							}
						}
						return styleFunction;
					}
				}
			} else {
				StyleLineCap val = getForNode(node);
				if (val != null) {
					return new StyleFunction<StyleLineCap>(val);
				}
			}
		}

		return new StyleFunction<StyleLineCap>(dflt);
	}

}
