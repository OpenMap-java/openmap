package com.bbn.openmap.event;

import static com.bbn.openmap.event.DistanceMouseMode.ShowAngleProperty;
import static com.bbn.openmap.event.DistanceMouseMode.ShowCircleProperty;
import static com.bbn.openmap.event.DistanceMouseMode.UnitProperty;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Properties;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * Mouse mode for drawing temporary range rings on a map bean.<br>
 * The whole map bean is repainted each time the range rings needs to be
 * repainted. The map bean needs to use a mouseDelegator to repaint properly.
 * <br>
 * 
 * @author Stephane Wasserhardt
 * 
 */
public class RangeRingsMouseMode extends CoordMouseMode {

    private static final long serialVersionUID = 6208201699394207932L;
    public final static transient String modeID = "RangeRings";
    /**
     * The property string used to set the numRings member variable.
     */
    public static final String NUM_RINGS_PROPERTY = "numRings";
    public static final String UNITS_PROPERTY = "units";
    public transient DecimalFormat df = new DecimalFormat("0.###");
    /**
     * Format used to draw distances.
     */
    protected Format distanceFormat = new DecimalFormat("0.###");
    /**
     * Number of rings to draw. Must be a positive integer, or else the value 1
     * will be used. Default value is 3.<br>
     */
    protected int numRings = 3;
    /**
     * Origin point of the range rings to be drawn.
     */
    protected Point2D origin = null;
    /**
     * Temporary destination point of the range rings to be drawn.
     */
    protected Point2D intermediateDest = null;
    /**
     * Destination point of the range rings to be drawn.
     */
    protected Point2D destination = null;

    protected DrawingAttributes rrAttributes = DrawingAttributes.getDefaultClone();
    /**
     * Distance units for label.
     */
    protected Length units = Length.MILE;

    public RangeRingsMouseMode() {
        this(true);
    }

    public RangeRingsMouseMode(boolean shouldConsumeEvents) {
        super(modeID, shouldConsumeEvents);
        init();
    }

    public RangeRingsMouseMode(String name, boolean shouldConsumeEvents) {
        super(name, shouldConsumeEvents);
        init();
    }

    protected void init() {
        setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        rrAttributes.setLinePaint(Color.GRAY);
        rrAttributes.setMattingPaint(Color.LIGHT_GRAY);
        rrAttributes.setMatted(true);
    }

    /**
     * Give the Format object used to display distances.
     * 
     * @return Format.
     */
    public Format getDistanceFormat() {
        return distanceFormat;
    }

    /**
     * Sets the Format object used to display distances.
     * 
     * @param distanceFormat Format.
     */
    public void setDistanceFormat(Format distanceFormat) {
        this.distanceFormat = distanceFormat;
    }

    /**
     * Returns the number of rings to display.
     * 
     * @return the number of rings to display.
     */
    public int getNumRings() {
        return numRings;
    }

    /**
     * Sets the number of rings to display.
     * 
     * @param numRings the number of rings to display.
     */
    public void setNumRings(int numRings) {
        this.numRings = numRings;
    }

    public void setActive(boolean active) {
        if (!active) {
            cleanUp();
        }
    }

    public void mouseClicked(MouseEvent e) {
        MapBean theMap = e.getSource() instanceof MapBean ? (MapBean) e.getSource() : null;
        if (theMap != null) {
            // if double (or more) mouse clicked
            if (e.getClickCount() >= 2) {
                // Clean the range rings
                cleanUp();
                theMap.repaint();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        Object obj = e.getSource();
        if (obj instanceof MapBean) {
            MapBean theMap = (MapBean) obj;
            if (origin == null) {
                Point pnt = e.getPoint();
                origin = theMap.inverse(pnt.getX(), pnt.getY(), new LatLonPoint.Double());
                theMap.addPaintListener(this);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        Object obj = e.getSource();
        if (obj instanceof MapBean) {
            MapBean theMap = (MapBean) obj;

            if (origin != null && destination == null) {
                Point pnt = e.getPoint();
                Point2D originPnt = theMap.getProjection().forward(origin);
                if (Math.abs(originPnt.getX() - pnt.getX()) > 5
                        && Math.abs(originPnt.getY() - pnt.getY()) > 5) {
                    destination = theMap.inverse(pnt.getX(), pnt.getY(), new LatLonPoint.Double());
                    intermediateDest = null;
                }
            }

            theMap.repaint();
        }
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    public void mouseMoved(MouseEvent e) {
        Object obj = e.getSource();
        if (obj instanceof MapBean) {
            MapBean theMap = (MapBean) obj;
            if (origin != null && destination == null) {
                Point pnt = e.getPoint();
                intermediateDest = theMap.inverse(pnt.getX(), pnt.getY(), new LatLonPoint.Double());
                fireMouseLocation(e);
                theMap.repaint();
            } else {
                fireMouseLocation(e);
            }
        }
    }

    /**
     * PaintListener method.
     * 
     * @param source the source object, most likely the MapBean
     * @param g java.awt.Graphics
     */
    public void listenerPaint(Object source, Graphics g) {
        MapBean theMap = source instanceof MapBean ? (MapBean) source : null;
        if (theMap != null) {
            if (origin != null) {
                paintOrigin(origin, g, theMap);
                // ... and we paint the rings if we know either destination or
                // intermediateDest
                if (destination != null) {
                    paintRangeRings(origin, destination, g, theMap);
                } else if (intermediateDest != null) {
                    paintRangeRings(origin, intermediateDest, g, theMap);
                }
            } else {
                theMap.removePaintListener(this);
            }
        }
    }

    /**
     * Paints the origin point of the range rings and its label on the given
     * Graphics.
     * 
     * @param llp the location of the origin.
     * @param graphics The Graphics to paint on.
     */
    protected void paintOrigin(Point2D llp, Graphics graphics, MapBean theMap) {
        paintOriginPoint(llp, graphics, theMap);
        paintOriginLabel(llp, graphics, theMap);
    }

    /**
     * Paints the origin point of the range rings on the given Graphics.
     * 
     * @param originPnt the origin point
     * @param graphics The Graphics to paint on.
     */
    protected void paintOriginPoint(Point2D originPnt, Graphics graphics, MapBean theMap) {
        if (theMap != null && originPnt != null) {
            OMPoint pt = new OMPoint(originPnt.getY(), originPnt.getX());
            preparePoint(pt);
            pt.generate(theMap.getRotatedProjection());
            pt.render(graphics);
        }
    }

    /**
     * Paints the origin label of the range rings on the given Graphics.
     * 
     * @param originPnt the origin point
     * @param graphics The Graphics to paint on.
     */
    protected void paintOriginLabel(Point2D originPnt, Graphics graphics, MapBean theMap) {
        if (theMap != null && originPnt != null) {
            OMText text = new OMText(originPnt.getY(), originPnt.getX(), getOriginLabel(), OMText.JUSTIFY_CENTER);
            text.setBaseline(OMText.BASELINE_BOTTOM);
            text.putAttribute(OMGraphicConstants.NO_ROTATE, Boolean.TRUE);
            prepareLabel(text);
            text.generate(theMap.getRotatedProjection());
            text.render(graphics);
        }
    }

    /**
     * Paints the circles and their labels on the given Graphics.
     * 
     * @param originPnt the origin location
     * @param dest the location of the inner ring.
     * @param graphics The Graphics to paint on.
     */
    protected void paintRangeRings(Point2D originPnt, Point2D dest, Graphics graphics,
                                   MapBean theMap) {
        Geo originGeo = new Geo(originPnt.getY(), originPnt.getX(), true);
        Geo destGeo = new Geo(dest.getY(), dest.getX(), true);
        double distance = originGeo.distance(destGeo); // radians

        for (int i = 1; i <= Math.max(1, numRings); i++) {
            double ringDist = distance * (double) i;

            paintCircle(originGeo, ringDist, graphics, theMap);
            paintLabel(originGeo, ringDist, graphics, theMap);
        }
    }

    /**
     * Paints a unique circle centered on <code>origin</code> and which crosses
     * <code>dest</code> on the given Graphics.
     * 
     * @param originGeo the origin location
     * @param distance the distance of the circle from the origin, in radians
     * @param graphics The Graphics to paint on.
     */
    protected void paintCircle(Geo originGeo, double distance, Graphics graphics, MapBean theMap) {
        OMCircle circle = new OMCircle(originGeo.getLatitude(), originGeo.getLongitude(), Length.DECIMAL_DEGREE.fromRadians(distance));
        prepareCircle(circle);
        circle.generate(theMap.getRotatedProjection());
        circle.render(graphics);
    }

    /**
     * Paints a label for the circle drawn using <code>dest</code> on the given
     * Graphics.
     * 
     * @param originGeo the Geo for the origin location
     * @param distance the distance of circle in radians.
     * @param graphics The Graphics to paint in.
     */
    protected void paintLabel(Geo originGeo, double distance, Graphics graphics, MapBean theMap) {
        Geo ringGeo = originGeo.offset(distance, Math.PI);
        OMText text = new OMText(ringGeo.getLatitude(), ringGeo.getLongitude(), getLabelFor(distance), OMText.JUSTIFY_CENTER);
        text.putAttribute(OMGraphicConstants.NO_ROTATE, Boolean.TRUE);
        text.setBaseline(OMText.BASELINE_BOTTOM);
        prepareLabel(text);
        text.generate(theMap.getRotatedProjection());
        text.render(graphics);
    }

    /**
     * Customizes the given OMPoint before it is rendered.
     * 
     * @param point OMPoint.
     */
    protected void preparePoint(OMPoint point) {
        rrAttributes.setTo(point);
    }

    /**
     * Customizes the given OMCicle before it is rendered.
     * 
     * @param circle OMCircle.
     */
    protected void prepareCircle(OMCircle circle) {
        rrAttributes.setTo(circle);
    }

    /**
     * Customizes the given OMText before it is rendered.
     * 
     * @param text OMText.
     */
    protected void prepareLabel(OMText text) {
        rrAttributes.setTo(text);

        text.setLinePaint(rrAttributes.getLinePaint());
        text.setTextMatteColor((Color) rrAttributes.getMattingPaint());
        text.setTextMatteStroke(new BasicStroke(4));
    }

    /**
     * Returns the String to be used as a labeler for the origin point of the
     * range rings.
     * 
     * @return label String.
     */
    protected String getOriginLabel() {
        return "(" + df.format(origin.getY()) + ", " + df.format(origin.getX()) + ")";
    }

    /**
     * Returns the String to be used as a labeler for the circle drawn using
     * <code>dest</code>.
     * 
     * @param distance The distance from the origin for the label, in radians.
     * @return label String.
     */
    protected String getLabelFor(double distance) {

        Format distFormat = getDistanceFormat();

        if (distFormat == null) {
            return Double.toString(distance);
        }
        return distFormat.format(new Double(units.fromRadians(distance))) + " " + units.getAbbr();
    }

    /**
     * Called when the range rings must be cleared, before repainting a clean
     * map.
     */
    protected void cleanUp() {
        origin = null;
        intermediateDest = null;
        destination = null;
    }

    private AffineTransform getTranslation(Point2D pt1, Point2D pt2, Projection proj) {
        Point2D p1 = proj.forward(pt1);
        Point2D p2 = proj.forward(pt2);
        return AffineTransform.getTranslateInstance(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }

    private LatLonPoint translate(Point2D pt, AffineTransform xyTranslation, Projection proj) {
        Point2D p = proj.forward(pt);
        xyTranslation.transform(p, p);
        return (LatLonPoint) proj.inverse(p, new LatLonPoint.Double());
    }

    /**
     * Set properties for this mouse mode
     * 
     * @param prefix property prefix that should be prepended to property keys.
     * @param props the properties containing key-values.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        rrAttributes.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        units = Length.get(props.getProperty(prefix + UNITS_PROPERTY, units.getAbbr()));
        numRings = PropUtils.intFromProperties(props, prefix + NUM_RINGS_PROPERTY, numRings);
    }

    /**
     * Get the current Properties for this mouse mode.
     * 
     * @param props The Properties object to add props to. A Properties object
     *        will be created if null.
     * @return props
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        rrAttributes.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(getPropertyPrefix());
        props.setProperty(prefix + NUM_RINGS_PROPERTY, Integer.toString(numRings));
        props.setProperty(prefix + UNITS_PROPERTY, units.getAbbr());

        return props;
    }

    /**
     * Return property info metadata for this PropertyConsumer.
     * 
     * @param list Properties to add to, may be null.
     * @return Properties for this object.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list = rrAttributes.getPropertyInfo(list);
        list.setProperty(NUM_RINGS_PROPERTY, "Number of range rings to be drawn (minimum=1; default=3).");
        list.setProperty(UNITS_PROPERTY, "Units of ring distance");
        list.setProperty(initPropertiesProperty, UNITS_PROPERTY + " " + NUM_RINGS_PROPERTY);

        return list;
    }
}
