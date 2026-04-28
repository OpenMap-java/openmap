package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public enum StyleVisibility {
	VISIBLE("visible", true), NONE("none", false);

	static String VISIBILITY = "visibility";
	final String name;
	final boolean visible;

	private StyleVisibility(String name, boolean visible) {
		this.name = name;
		this.visible = visible;
	}

	public static StyleVisibility getForNode(JsonNode node) {
		JsonNode vis = node.get(VISIBILITY);
		if (vis != null) {
			return getForName(vis.asText());
		}
		return null;
	}

	public static StyleVisibility getForName(String nm) {
		for (StyleVisibility st : StyleVisibility.values()) {
			if (nm.contains(st.name)) {
				return st;
			}
		}
		return null;
	}

	public boolean isVisible() {
		return visible;
	}

	public static StyleFunction<StyleVisibility> getFunction(JsonNode node) {
		StyleVisibility dflt = VISIBLE;
		if (node != null) {
			if (node.has("stops")) {

				JsonNode stopsNode = node.withArray("stops");
				if (stopsNode != null) {
					// System.out.println(node);
					Iterator<JsonNode> stops = stopsNode.iterator();
					if (stops != null) {
						StyleVisibility base = getForName(StyleNode.getAsText(node, "base", "visible"));
						StyleFunction<StyleVisibility> styleFunction = new StyleFunction<StyleVisibility>(base, dflt);
						while (stops.hasNext()) {
							JsonNode stop = stops.next();

							if (stop.isArray()) {
								int zoomLevel = stop.get(0).asInt();
								StyleVisibility val = getForName(stop.get(1).asText());

								styleFunction.add(zoomLevel, val);
							}
						}
						return styleFunction;
					}
				}
			} else {
				StyleVisibility val = getForNode(node);
				if (val != null) {
					return new StyleFunction<StyleVisibility>(val);
				}
			}
		}

		return null;
	}
}
