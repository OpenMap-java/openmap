package com.bbn.openmap.dataAccess.mapTile.mb;

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
		return VISIBLE;
	}

	public static StyleVisibility getForName(String nm) {
		for (StyleVisibility st : StyleVisibility.values()) {
			if (nm.contains(st.name)) {
				System.out.println("returning " + st.name);
				return st;
			}
		}
		return VISIBLE;
	}
	
	public boolean isVisible() {
		return visible; 
	}
}
