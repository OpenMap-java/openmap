// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.image.ImageScaler;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * OMMouseMode is a combination of the PanMouseMode, NavMouseMode,
 * SelectMouseMode and DistanceMouseMode. Press and drag to pan. Double click to
 * recenter, CTRL double click to recenter and zoom. Shift-CTRL-Double click to
 * center and zoom out. Double click to select OMGraphics. Right press and drag
 * to measure. Right click for popup menu.
 */
public class OMMouseMode extends CoordMouseMode implements ProjectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String OpaquenessProperty = "opaqueness";
	public final static String LeaveShadowProperty = "leaveShadow";
	public final static String UseCursorProperty = "useCursor";
	public final static String UnitProperty = "units";
	public final static String ShowCircleProperty = "showCircle";
	public final static String ShowAngleProperty = "showAngle";

	public final static float DEFAULT_OPAQUENESS = 1.0f;

	public final static transient String modeID = "Gestures";

	private boolean isPanning = false;
	private int oX, oY;
	private float opaqueness = DEFAULT_OPAQUENESS;
	private boolean leaveShadow = false;
	private boolean useCursor;
	public transient DecimalFormat df = new DecimalFormat("0");
	// The unit type, default mile
	private Length unit = Length.MILE;
	// Flag to display the azimuth angle. Default true
	boolean showAngle = true;

	/**
	 * rPoint1 is the anchor point of a line segment
	 */
	public Point2D rPoint1;
	/**
	 * rPoint2 is the new (current) point of a line segment
	 */
	public Point2D rPoint2;
	/**
	 * Flag, true if the mouse has already been pressed
	 */
	public boolean mousePressed = false;
	/**
	 * Vector to store all distance segments, first point and last point pairs
	 */
	public Vector<Point2D> segments = new Vector<Point2D>();
	/**
	 * Distance of the current segment
	 */
	public double distance = 0;
	/**
	 * The cumulative distance from the first mouse click
	 */
	public double totalDistance = 0;
	/**
	 * To display the rubberband circle, default true
	 */
	private boolean displayCircle = true;
	/**
	 * Special units value for displaying all units ... use only in properties
	 * file
	 */
	public final static String AllUnitsPropertyValue = "all";
	protected BufferedMapBean theMap = null;
	protected String coordString = null;

	protected OMGraphicList distanceList;

	public OMMouseMode() {
		super(modeID, true);
		setUseCursor(false);
		setLeaveShadow(true);
		setOpaqueness(DEFAULT_OPAQUENESS);
	}

	/**
	 * @return Returns the useCursor.
	 */
	public boolean isUseCursor() {
		return useCursor;
	}

	/**
	 * @param useCursor
	 *            The useCursor to set.
	 */
	public void setUseCursor(boolean useCursor) {
		this.useCursor = useCursor;
		if (useCursor) {
			/*
			 * For who like make his CustomCursor
			 */
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				ImageIcon pointer = new ImageIcon(getClass().getResource(
						"Gestures.gif"));
				Dimension bestSize = tk.getBestCursorSize(pointer
						.getIconWidth(), pointer.getIconHeight());
				Image pointerImage = ImageScaler.getOptimalScalingImage(pointer
						.getImage(), (int) bestSize.getWidth(), (int) bestSize
						.getHeight());
				Cursor cursor = tk.createCustomCursor(pointerImage, new Point(
						0, 0), "PP");
				setModeCursor(cursor);
				return;
			} catch (Exception e) {
				// Problem finding image probably, just move on.
			}
		}

		setModeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		prefix = PropUtils.getScopedPropertyPrefix(prefix);

		opaqueness = PropUtils.floatFromProperties(props, prefix
				+ OpaquenessProperty, opaqueness);
		leaveShadow = PropUtils.booleanFromProperties(props, prefix
				+ LeaveShadowProperty, leaveShadow);

		setUseCursor(PropUtils.booleanFromProperties(props, prefix
				+ UseCursorProperty, isUseCursor()));

		String name = props.getProperty(prefix + UnitProperty);
		if (name != null) {
			Length length = Length.get(name);
			if (length != null) {
				setUnit(length);
			} else if (name.equals(AllUnitsPropertyValue)) {
				setUnit(null);
			}
		}

		setDisplayCircle(PropUtils.booleanFromProperties(props, prefix
				+ ShowCircleProperty, isDisplayCircle()));
		setShowAngle(PropUtils.booleanFromProperties(props, prefix
				+ ShowAngleProperty, isShowAngle()));

	}

	public Properties getProperties(Properties props) {
		props = super.getProperties(props);
		String prefix = PropUtils.getScopedPropertyPrefix(this);
		props.put(prefix + OpaquenessProperty, Float.toString(getOpaqueness()));
		props.put(prefix + LeaveShadowProperty, Boolean
				.toString(isLeaveShadow()));
		props.put(prefix + UseCursorProperty, Boolean.toString(isUseCursor()));
		String unitValue = (unit != null ? unit.toString()
				: AllUnitsPropertyValue);
		props.put(prefix + UnitProperty, unitValue);
		props.put(prefix + ShowCircleProperty, new Boolean(isDisplayCircle())
				.toString());
		props.put(prefix + ShowAngleProperty, new Boolean(isShowAngle())
				.toString());
		return props;
	}

	public Properties getPropertyInfo(Properties props) {
		props = super.getPropertyInfo(props);

		PropUtils
				.setI18NPropertyInfo(
						i18n,
						props,
						OMMouseMode.class,
						OpaquenessProperty,
						"Transparency",
						"Transparency level for moving map, between 0 (clear) and 1 (opaque).",
						null);
		PropUtils.setI18NPropertyInfo(i18n, props, OMMouseMode.class,
				LeaveShadowProperty, "Leave Shadow",
				"Display current map in background while panning.",
				"com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

		PropUtils.setI18NPropertyInfo(i18n, props, OMMouseMode.class,
				UseCursorProperty, "Use Cursor",
				"Use hand cursor for mouse mode.",
				"com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

		PropUtils
				.setI18NPropertyInfo(
						i18n,
						props,
						OMMouseMode.class,
						UnitProperty,
						"Units",
						"Units to use for measurements, from Length.name possibilities.",
						null);

		PropUtils
				.setI18NPropertyInfo(
						i18n,
						props,
						OMMouseMode.class,
						ShowCircleProperty,
						"Show Distance Circle",
						"Flag to set whether the range circle is drawn at the end of the line (true/false).",
						"com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

		PropUtils
				.setI18NPropertyInfo(
						i18n,
						props,
						OMMouseMode.class,
						ShowAngleProperty,
						"Show Angle",
						"Flag to note the azimuth angle of the line in the information line (true/false).",
						"com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

		return props;
	}

	/**
	 * Checks the MouseEvent to see if a BufferedMapBean can be found.
	 * 
	 * @param evt
	 *            MouseEvent, or a MapMouseEvent
	 * @return BufferedMapBean, or null if source is not a BufferedMapBean.
	 */
	protected BufferedMapBean getBufferedMapBean(MouseEvent evt) {
		if (evt instanceof MapMouseEvent) {
			MapBean mb = ((MapMouseEvent) evt).getMap();
			if (mb instanceof BufferedMapBean) {
				return (BufferedMapBean) mb;
			}
		} else {
			Object src = evt.getSource();
			if (src instanceof BufferedMapBean) {
				return (BufferedMapBean) src;
			}
		}

		return null;
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 *      The first click for drag, the image is generated. This image is
	 *      redrawing when the mouse is move, but, I need to repain the original
	 *      image.
	 */
	public void mouseDragged(MouseEvent arg0) {

		BufferedMapBean mb = getBufferedMapBean(arg0);
		if (mb == null) {
			// OMMouseMode needs a BufferedMapBean
			return;
		}

		// Left mouse click, pan
		if (SwingUtilities.isLeftMouseButton(arg0)) {

			Point2D pnt = mb.getNonRotatedLocation(arg0);
			int x = (int) pnt.getX();
			int y = (int) pnt.getY();

			if (!isPanning) {

				oX = x;
				oY = y;

				isPanning = true;

			} else {

				mb.setPanningTransform(AffineTransform.getTranslateInstance(x
						- oX, y - oY));
				mb.repaint();
			}
		} else {

			theMap = mb;

			if (rPoint1 == null) {
				rPoint1 = theMap.getCoordinates(arg0);

			} else {
				// right mouse click, measure
				double lat1, lat2, long1, long2;
				// erase the old line and circle first
//				paintRubberband(rPoint1, rPoint2, coordString);
				// get the current mouse location in latlon
				rPoint2 = theMap.getCoordinates(arg0);

				lat1 = rPoint1.getY();
				long1 = rPoint1.getX();
				// lat, lon of current mouse position
				lat2 = rPoint2.getY();
				long2 = rPoint2.getX();
				// calculate great circle distance in nm
				// distance = getGreatCircleDist(lat1, long1,
				// lat2, long2, Length.NM);
				distance = GreatCircle.sphericalDistance(ProjMath
						.degToRad(lat1), ProjMath.degToRad(long1), ProjMath
						.degToRad(lat2), ProjMath.degToRad(long2));

				// calculate azimuth angle dec deg
				double azimuth = getSphericalAzimuth(lat1, long1, lat2, long2);
				coordString = createDistanceInformationLine(rPoint2, distance,
						azimuth);

				// paint the new line and circle up to the current
				// mouse location
				paintRubberband(rPoint1, rPoint2, coordString);
				theMap.repaint();
			}
		}
		super.mouseDragged(arg0);
	}

	/**
	 * Process a mouse pressed event. Add the mouse location to the segment
	 * vector. Calculate the cumulative total distance.
	 * 
	 * @param e
	 *            mouse event.
	 */
	public void mousePressed(MouseEvent e) {
		mouseSupport.fireMapMousePressed(e);
		e.getComponent().requestFocus();

		if (SwingUtilities.isRightMouseButton(e)) {
			// mouse has now been pressed
			mousePressed = true;

			MapBean mb = theMap;
			if (mb == null && e.getSource() instanceof MapBean) {
				mb = (MapBean) e.getSource();
			}

			if (mb != null) {
				// anchor the new first point of the line
				rPoint1 = mb.getCoordinates(e);
				// ensure the second point is not yet set.
				rPoint2 = null;
				// add the distance to the total distance
				totalDistance = 0;
			}
		}

	}

	public void mouseClicked(MouseEvent e) {
		Object obj = e.getSource();

		mouseSupport.fireMapMouseClicked(e);

		if (!(obj instanceof MapBean) || e.getClickCount() < 2)
			return;

		MapBean map = (MapBean) obj;
		Projection projection = map.getProjection();
		Proj p = (Proj) projection;

		Point2D llp = map.getCoordinates(e);

		boolean shift = e.isShiftDown();

		if (shift) {
			p.setScale(p.getScale() * 2.0f);
		} else {
			p.setScale(p.getScale() / 2.0f);
		}

		p.setCenter(llp);
		map.setProjection(p);
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 *      Make Pan event for the map.
	 */
	public void mouseReleased(MouseEvent arg0) {
		if (isPanning) {

			BufferedMapBean mb = getBufferedMapBean(arg0);
			if (mb == null) {
				return;
			}

			Projection proj = mb.getProjection();
			Point2D center = proj.forward(proj.getCenter());

			Point2D pnt = mb.getNonRotatedLocation(arg0);
			int x = (int) pnt.getX();
			int y = (int) pnt.getY();

			center.setLocation(center.getX() - x + oX, center.getY() - y + oY);
			mb.setCenter(proj.inverse(center));

			mb.setPanningTransform(null);

			isPanning = false;
			// bufferedMapImage = null; //clean up when not active...
		} else {
			if (theMap != null) {
				distanceList = null;
				// cleanup
				cleanUp();
				theMap = null;
			}
		}

		super.mouseReleased(arg0);
	}

	public boolean isLeaveShadow() {
		return leaveShadow;
	}

	public void setLeaveShadow(boolean leaveShadow) {
		this.leaveShadow = leaveShadow;
	}

	public float getOpaqueness() {
		return opaqueness;
	}

	public void setOpaqueness(float opaqueness) {
		this.opaqueness = opaqueness;
	}

	public boolean isPanning() {
		return isPanning;
	}

	public int getOX() {
		return oX;
	}

	public int getOY() {
		return oY;
	}

	/**
	 * PaintListener interface, notifying the MouseMode that the MapBean has
	 * repainted itself. Useful if the MouseMode is drawing stuff.
	 */
	public void listenerPaint(java.awt.Graphics g) {
		if (distanceList != null) {
			distanceList.render(g);
		}
	}

	@Override
	public void projectionChanged(ProjectionEvent e) {
		Projection p = e.getProjection();
		if (p != null && distanceList != null) {
			distanceList.generate(p);
		}
	}

	/**
	 * Draw a rubberband line and circle between two points
	 * 
	 * @param pt1
	 *            the anchor point.
	 * @param pt2
	 *            the current (mouse) position.
	 */
	@SuppressWarnings("serial")
	public void paintRubberband(Point2D pt1, Point2D pt2, String coordString) {		
		if (distanceList == null) {
			distanceList = new OMGraphicList() {
				public void render(Graphics g) {
					Graphics g2 = g.create();
					g2.setXORMode(java.awt.Color.lightGray);

					for (OMGraphic omg : this) {
						if (omg instanceof OMText) {
							omg.render(g);
						} else {
							omg.render(g2);
						}
					}

					g2.dispose();
				}
			};
		}

		distanceList.clear();

		paintLine(pt1, pt2);
		paintCircle(pt1, pt2);
		paintText(pt1, pt2, coordString);
	}

	/**
	 * Draw a rubberband line between two points
	 * 
	 * @param pt1
	 *            the anchor point.
	 * @param pt2
	 *            the current (mouse) position.
	 */
	public void paintLine(Point2D pt1, Point2D pt2) {
		if (pt1 != null && pt2 != null) {
			// the line connecting the segments
			OMLine cLine = new OMLine(pt1.getY(), pt1.getX(), pt2.getY(), pt2
					.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
			// get the map projection
			Projection proj = theMap.getProjection();
			// prepare the line for rendering
			cLine.generate(proj);

			distanceList.add(cLine);
		}
	}

	public void paintText(Point2D base, Point2D pt1, String coordString) {
		if (coordString != null) {

			base = theMap.getProjection().forward(base);
			pt1 = theMap.getProjection().forward(pt1);

			if (base.distance(pt1) > 3) {
				// g.drawString(coordString, (int) pt1.getX() + 5, (int) pt1
				// .getY() - 5);

				OMText text = new OMText((int) pt1.getX() + 5,
						(int) pt1.getY() - 5, coordString, OMText.JUSTIFY_LEFT);

				Font font = text.getFont();
				text.setFont(font.deriveFont(Font.BOLD, font.getSize() + 4));

				text.setLinePaint(Color.BLACK);
				
				text.setTextMatteColor(Color.WHITE);
				text.setTextMatteStroke(new BasicStroke(5));
				text.setMattingPaint(OMColor.clear);				
				
				text.generate(theMap.getProjection());
				distanceList.add(text);
			}

		}
	}

	/**
	 * Draw a rubberband circle between two points
	 * 
	 * @param pt1
	 *            the anchor point.
	 * @param pt2
	 *            the current (mouse) position.
	 */
	public void paintCircle(Point2D pt1, Point2D pt2) {
		// do all this only if want to display the rubberband circle
		if (displayCircle) {
			if (pt1 != null && pt2 != null) {
				// first convert degrees to radians
				double radphi1 = ProjMath.degToRad(pt1.getY());
				double radlambda0 = ProjMath.degToRad(pt1.getX());
				double radphi = ProjMath.degToRad(pt2.getY());
				double radlambda = ProjMath.degToRad(pt2.getX());
				// calculate the circle radius
				double dRad = GreatCircle.sphericalDistance(radphi1,
						radlambda0, radphi, radlambda);
				// convert into decimal degrees
				double rad = ProjMath.radToDeg(dRad);
				// make the circle
				OMCircle circle = new OMCircle(pt1.getY(), pt1.getX(), rad);
				// get the map projection
				Projection proj = theMap.getProjection();
				// prepare the circle for rendering
				circle.generate(proj);
				distanceList.add(circle);
			}
		} // end if(displayCircle)
	}

	/**
	 * Reset the segments and distances
	 */
	public void cleanUp() {
		// a quick way to clean the vector
		segments = new Vector<Point2D>();
		// reset the total distance
		totalDistance = 0.0;
		distance = 0.0;
		coordString = null;
	}

	/**
	 * Return the azimuth angle in decimal degrees from north. Based on
	 * spherical_azimuth. See class GreatCircle.java
	 * 
	 * @param phi1
	 *            latitude in decimal degrees of start point
	 * @param lambda0
	 *            longitude in decimal degrees of start point
	 * @param phi
	 *            latitude in decimal degrees of end point
	 * @param lambda
	 *            longitude in decimal degrees of end point
	 * @return float azimuth angle in degrees
	 */
	public double getSphericalAzimuth(double phi1, double lambda0, double phi,
			double lambda) {
		// convert arguments to radians
		double radphi1 = ProjMath.degToRad(phi1);
		double radlambda0 = ProjMath.degToRad(lambda0);
		double radphi = ProjMath.degToRad(phi);
		double radlambda = ProjMath.degToRad(lambda);
		// get the spherical azimuth in radians between the two points
		double az = GreatCircle.sphericalAzimuth(radphi1, radlambda0, radphi,
				radlambda);
		return ProjMath.radToDeg(az);
	}

	protected String createDistanceInformationLine(Point2D llp,
			double distance, double azimuth) {
		// setup the distance info to be displayed
		String unitInfo = null;
		// what unit is asked for
		if (unit == null) {
			unitInfo = df.format(Length.NM.fromRadians((float) distance))
					+ Length.NM.getAbbr() + ",  "
					+ df.format(Length.KM.fromRadians((float) distance))
					+ Length.KM.getAbbr() + ",  "
					+ df.format(Length.MILE.fromRadians((float) distance))
					+ Length.MILE.getAbbr() + "  ";
		} else {
			unitInfo = df.format(unit.fromRadians(distance)) + " "
					+ unit.getAbbr();
		}

		return unitInfo;
	}

	/**
	 * Set the unit of distance to be displayed: Length.NM, Length.KM or
	 * Length.MILE. If null, displays all of them.
	 */
	public void setUnit(Length units) {
		unit = units;
	}

	public boolean isShowAngle() {
		return showAngle;
	}

	public void setShowAngle(boolean showAngle) {
		this.showAngle = showAngle;
	}

	public boolean isDisplayCircle() {
		return displayCircle;
	}

	public void setDisplayCircle(boolean displayCircle) {
		this.displayCircle = displayCircle;
	}

	/**
	 * Return the unit of distance being displayed: Length.NM, Length.KM or
	 * Length.MILE. If null, displays all of them.
	 */
	public Length getUnit() {
		return unit;
	}

}
