package com.bbn.openmap.tools.icon;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

/**
 * Pre-defined shapes used for images on gui controls. All of these shapes are
 * defined within a region between 0,0 and 100,100 for scaling.
 * 
 * @author dietrick
 */
public enum OMIconPart {

	BIG_BOX(getPolygon(new int[] { 10, 10, 90, 90, 10 }, new int[] { 10, 90, 90, 10, 10 })),
	SMALL_BOX(getPolygon(new int[] { 30, 30, 70, 70, 30 }, new int[] { 30, 70, 70, 30, 30 })),
	FILL_BOX(getPolygon(new int[] { 10, 10, 50, 50, 30, 30, 70, 70, 50, 50, 90, 90, 10 },
			new int[] { 10, 90, 90, 70, 70, 30, 30, 70, 70, 90, 90, 10, 10 })),
	UL_TRI(getPolygon(new int[] { 10, 10, 75, 10 }, new int[] { 10, 75, 10, 10 })),
	LR_TRI(getPolygon(new int[] { 25, 90, 90, 25 }, new int[] { 90, 90, 25, 90 })),
	LL_UR_LINE(getPolygon(new int[] { 10, 90 }, new int[] { 90, 10 })),
	UL_LR_LINE(getPolygon(new int[] { 10, 90 }, new int[] { 10, 90 })),
	BIG_ARROW(getPolygon(new int[] { 50, 90, 80, 80, 20, 20, 10, 50 }, new int[] { 10, 40, 40, 90, 90, 40, 40, 10 })),
	MED_ARROW(getPolygon(new int[] { 50, 90, 70, 70, 30, 30, 10, 50 }, new int[] { 10, 50, 50, 90, 90, 50, 50, 10 })),
	SMALL_ARROW(getPolygon(new int[] { 50, 80, 60, 60, 40, 40, 20, 50 }, new int[] { 10, 50, 50, 90, 90, 50, 50, 10 })),
	CORNER_TRI(getPolygon(new int[] { 10, 50, 10 }, new int[] { 10, 10, 50 })),
	OPP_CORNER_TRI(getPolygon(new int[] { 50, 90, 50 }, new int[] { 50, 50, 90 })), CIRCLE(getCircle(50, 50, 46)),
	DOT(getCircle(50, 50, 6)),
	PLUS(getPolygon(new int[] { 25, 50, 50, 50, 50, 75, 75, 50, 50, 50, 50, 25, 25 },
			new int[] { 50, 50, 25, 25, 50, 50, 50, 50, 75, 75, 50, 50, 50 })),
	ADD_PLUS(getPolygon(new int[] { 70, 80, 80, 80, 80, 90 }, new int[] { 20, 20, 10, 30, 20, 20 })),
	MINUS(getPolygon(new int[] { 25, 75 }, new int[] { 50, 50 })), MAP_PIN_HEAD(getCircle(50, 33, 30)),
	MAP_PIN_BOTTOM(getPolygon(new int[] { 50, 30, 70, 50 }, new int[] { 90, 50, 50, 90 })),
	TRIANGLE(getPolygon(new int[] { 10, 50, 90, 10 }, new int[] { 90, 10, 90, 90 })),
	SQUAT_TRIANGLE(getPath(new int[] { 10, 50, 90 }, new int[] { 60, 40, 60 }));

	private final BasicIconPart iconPart;

	private OMIconPart(Shape s) {
		iconPart = new BasicIconPart(s);
	}

	/**
	 * 
	 * @return the IconPart for the enum part.
	 */
	public IconPart getIconPart() {
		return iconPart;
	}

	/**
	 * Create a pixel-based general path Shape. No limitations for general use,
	 * OMIconParts will have coordinates between 0-100.
	 * 
	 * @param xp x coordinates, between 0-100
	 * @param yp y coordinates, between 0-100
	 * @return Shape created from GeneralPath
	 */
	public static Shape getPath(int[] xp, int[] yp) {
		if (xp.length < 2) {
			return null;
		}

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xp.length);
		path.moveTo((float) xp[0], (float) yp[0]);
		for (int index = 1; index < xp.length; index++) {
			path.lineTo((float) xp[index], (float) yp[index]);
		}
		return path;
	}

	/**
	 * Create a pixel-based shape.
	 * 
	 * @param xp x coordinates of polygon
	 * @param yp y coordinates of polygon
	 * @return Polygon shape
	 */
	public static Shape getPolygon(int[] xp, int[] yp) {
		return new Polygon(xp, yp, xp.length);
	}

	/**
	 * Create a pixel-based shape.
	 * 
	 * @param x      center x coordinate
	 * @param y      center y coordinate
	 * @param radius radius of circle
	 * @return Shape from Ellipse
	 */
	public static Shape getCircle(double x, double y, double radius) {
		return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
	}

	/**
	 * Create a pixel-based shape.
	 * 
	 * @param x      center x coordinate
	 * @param y      center y coordinate
	 * @param radius radius of circle
	 * @param start  starting angle of arc in degrees
	 * @param end    ending angle of arc in degrees
	 * @return Shape from Arc2D
	 */
	public static Shape getArc(double x, double y, double radius, double start, double end, int type) {
		Arc2D arc = new Arc2D.Double(type);
		arc.setArcByCenter(x, y, radius, start, end, type);
		return arc;
	}

	/**
	 * Create the reload symbol used by layer palettes.
	 * 
	 * @return IconPart
	 */
	public static IconPart getReloadSymbol() {
		IconPartList ipList = new IconPartList();
		ipList.add(new BasicIconPart(getArc(50, 50, 30, 90, 270, Arc2D.OPEN)));
		ipList.add(new BasicIconPart(getPolygon(new int[] { 43, 65, 49 }, new int[] { 15, 20, 37 })));
		return ipList;
	}

	/**
	 * Create a settings symbol created from parts, used by layer palettes.
	 * 
	 * @return IconPart
	 */
	public static IconPart getSettingsSymbol() {
		IconPartList ipList = new IconPartList();
		ipList.add(new BasicIconPart(getCircle(50, 50, 30)));

		for (int i = 0; i < 360; i += 45) {
			double angle = Math.toRadians(i);
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double x1 = 30.0 * cos + 50;
			double y1 = 30.0 * sin + 50;
			double x2 = 40.0 * cos + 50;
			double y2 = 40.0 * sin + 50;

			Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);
			ipList.add(new BasicIconPart(line));
		}

		return ipList;
	}

}
