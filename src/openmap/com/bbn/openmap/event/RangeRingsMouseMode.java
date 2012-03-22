package com.bbn.openmap.event;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Properties;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * Mouse mode for drawing temporary range rings on a map bean.<br>
 * The whole map bean is repainted each time the range rings needs to be
 * repainted. The map bean needs to use a mouseDelegator to repaint properly.<br>
 *
 * @author Stephane Wasserhardt
 *
 */
public class RangeRingsMouseMode
        extends CoordMouseMode {

    private static final long serialVersionUID = 6208201699394207932L;
    public final static transient String modeID = "RangeRings";
    /**
     * The property string used to set the numRings member variable.
     */
    public static final String NUM_RINGS_PROPERTY = "numRings";
    public transient DecimalFormat df = new DecimalFormat("0.###");
    /**
     * Format used to draw distances.
     */
    protected Format distanceFormat;
    /**
     * Number of rings to draw. Must be a positive integer, or else the value 1
     * will be used. Default value is 3.<br>
     */
    protected int numRings = 3;
    /**
     * Origin point of the range rings to be drawn.
     */
    protected LatLonPoint origin = null;
    /**
     * Temporary destination point of the range rings to be drawn.
     */
    protected LatLonPoint intermediateDest = null;
    /**
     * Destination point of the range rings to be drawn.
     */
    protected LatLonPoint destination = null;
    /**
     * Active MapBean.
     */
    protected MapBean mapBean;

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
    }

    /**
     * Return the map bean.
     *
     * @return The map bean.
     */
    public MapBean getMapBean() {
        return mapBean;
    }

    /**
     * Set the map bean.
     *
     * @param aMap a map bean
     */
    public void setMapBean(MapBean aMap) {
        mapBean = aMap;
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
        redraw();
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
        redraw();
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() instanceof MapBean) {
            setMapBean((MapBean) e.getSource());
            // if double (or more) mouse clicked
            if (e.getClickCount() >= 2) {
                // Clean the range rings

                cleanUp();

                redraw();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        e.getComponent().requestFocus();
    }

    public void mouseReleased(MouseEvent e) {
        if ((e.getComponent().hasFocus()) && (e.getSource() instanceof MapBean)) {
            setMapBean((MapBean) e.getSource());

            LatLonPoint pt = (LatLonPoint) getMapBean().getProjection().inverse(e.getPoint(), new LatLonPoint.Double());
            // If this is the first click (real first click or click after
            // "finished" rangeRings)
            if ((origin == null) || ((origin != null) && (destination != null))) {
                // First, we clear up the map (erase any previous rangeRings)
                cleanUp();
                redraw();

                origin = pt;
                // Just to be sure
                destination = null;

                startUp();
            } // This case is when only the origin is known
            else {
                // The click then corresponds to the selection of a destination
                destination = pt;

                finished();
            }

            redraw();
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (e.getSource() instanceof MapBean) {
            setMapBean((MapBean) (e.getSource()));
            if (origin != null) {
                intermediateDest = (LatLonPoint) getMapBean().getProjection().inverse(e.getPoint(), new LatLonPoint.Double());

                fireMouseLocation(e);

                update();

                redraw();
            } else {
                fireMouseLocation(e);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
        if (e.getSource() instanceof MapBean) {
            setMapBean((MapBean) e.getSource());
        }
    }

    /**
     * Repaints the map bean. When this mouse mode is active, it is registered
     * as a <code>paintListener</code> for the mapBean by the mouseDelegator, so
     * the <code>listenerPaint</code> method can draw the range rings on the map
     * bean.
     */
    public void redraw() {
        MapBean mb = getMapBean();
        if (mb != null) {
            mb.repaint();
        }
    }

    public void listenerPaint(Graphics g) {
        // We paint only if we know the origin ...
        if (origin != null) {
            paintOrigin(g);
            // ... and we paint the rings if we know either of destination or
            // intermediateDest
            if (destination != null) {
                paintRangeRings(destination, g);
            } else if (intermediateDest != null) {
                paintRangeRings(intermediateDest, g);
            }
        }
    }

    /**
     * Paints the origin point of the range rings and its label on the map bean.
     */
    protected void paintOrigin() {
        MapBean mb = getMapBean();
        if (mb != null) {
            Graphics g = mb.getGraphics(true);
            paintOrigin(g);
        }
    }

    /**
     * Paints the origin point of the range rings and its label on the given
     * Graphics.
     *
     * @param graphics The Graphics to paint on.
     */
    protected void paintOrigin(Graphics graphics) {
        MapBean mb = getMapBean();
        if (mb == null) {
            return;
        }
        paintOriginPoint(graphics);
        paintOriginLabel(graphics);
    }

    /**
     * Paints the origin point of the range rings on the given Graphics.
     *
     * @param graphics The Graphics to paint on.
     */
    protected void paintOriginPoint(Graphics graphics) {
        MapBean mb = getMapBean();

        Projection proj = mb.getProjection();

        OMPoint pt = new OMPoint((float) origin.getY(), (float) origin.getX());

        Graphics2D g = (Graphics2D) graphics;
        g.setPaintMode();
        g.setColor(Color.BLACK);

        preparePoint(pt);

        pt.generate(proj);
        pt.render(g);
    }

    /**
     * Paints the origin label of the range rings on the given Graphics.
     *
     * @param graphics The Graphics to paint on.
     */
    protected void paintOriginLabel(Graphics graphics) {
        MapBean mb = getMapBean();

        Projection proj = mb.getProjection();
        Point2D pt = proj.forward(origin);

        String infoText = getOriginLabel();

        Graphics2D g = (Graphics2D) graphics;
        Rectangle2D r = g.getFontMetrics().getStringBounds(infoText, graphics);

        pt.setLocation(pt.getX() - (r.getWidth() / 2d), pt.getY() - (r.getHeight() / 2d));

        OMText text = new OMText((int) pt.getX(), (int) pt.getY(), infoText, OMText.JUSTIFY_LEFT);

        g.setPaintMode();
        g.setColor(Color.BLACK);

        prepareLabel(text);

        text.generate(proj);
        text.render(g);
    }

    /**
     * Paints the circles and their labels on the map bean.
     *
     * @param dest The destination point, used with the <code>origin</code>
     *        member variable to compute the rings.
     */
    protected void paintRangeRings(LatLonPoint dest) {
        MapBean mb = getMapBean();
        if (mb != null) {
            Graphics g = mb.getGraphics(true);
            paintRangeRings(dest, g);
        }
    }

    /**
     * Paints the circles and their labels on the given Graphics.
     *
     * @param dest The destination point, used with the <code>origin</code>
     *        member variable to compute the rings.
     * @param graphics The Graphics to paint on.
     */
    protected void paintRangeRings(LatLonPoint dest, Graphics graphics) {
        MapBean mb = getMapBean();
        if (mb == null) {
            return;
        }

        Projection proj = mb.getProjection();

        AffineTransform xyTranslation = getTranslation(origin, dest, proj);

        LatLonPoint p = null;
        for (int i = 0; i < Math.max(1, numRings); i++) {
            if (p == null) {
                p = dest;
            } else {
                p = translate(p, xyTranslation, proj);
            }
            paintCircle(p, graphics);
            paintLabel(p, graphics);
        }
    }

    /**
     * Paints a unique circle centered on <code>origin</code> and which crosses
     * <code>dest</code> on the map bean.
     *
     * @param dest A point on the circle.
     */
    protected void paintCircle(LatLonPoint dest) {
        MapBean mb = getMapBean();
        if (mb != null) {
            Graphics g = mb.getGraphics(true);
            paintCircle(dest, g);
        }
    }

    /**
     * Paints a unique circle centered on <code>origin</code> and which crosses
     * <code>dest</code> on the given Graphics.
     *
     * @param dest A point on the circle.
     * @param graphics The Graphics to paint on.
     */
    protected void paintCircle(LatLonPoint dest, Graphics graphics) {
        double oLat = origin.getY();
        double oLon = origin.getX();

        double radphi1 = ProjMath.degToRad(oLat);
        double radlambda0 = ProjMath.degToRad(oLon);
        double radphi = ProjMath.degToRad(dest.getY());
        double radlambda = ProjMath.degToRad(dest.getX());

        // calculate the circle radius
        double dRad = GreatCircle.sphericalDistance(radphi1, radlambda0, radphi, radlambda);
        // convert into decimal degrees
        float rad = (float) ProjMath.radToDeg(dRad);

        // make the circle
        OMCircle circle = new OMCircle((float) oLat, (float) oLon, rad);

        prepareCircle(circle);

        // get the map projection
        Projection proj = getMapBean().getProjection();
        // prepare the circle for rendering
        circle.generate(proj);
        // render the circle graphic
        circle.render(graphics);
    }

    /**
     * Paints a label for the circle drawn using <code>dest</code> on the map
     * bean.
     *
     * @param dest A point on the circle.
     */
    protected void paintLabel(LatLonPoint dest) {
        MapBean mb = getMapBean();
        if (mb != null) {
            Graphics g = mb.getGraphics(true);
            paintLabel(dest, g);
        }
    }

    /**
     * Paints a label for the circle drawn using <code>dest</code> on the given
     * Graphics.
     *
     * @param dest A point on the circle.
     * @param graphics The Graphics to paint on.
     */
    protected void paintLabel(LatLonPoint dest, Graphics graphics) {
        String infoText = getLabelFor(dest);
        Graphics2D g = (Graphics2D) graphics;
        Rectangle2D r = g.getFontMetrics().getStringBounds(infoText, graphics);
        double th = r.getHeight();

        LatLonPoint llp = new LatLonPoint.Double(origin);
        double distance = llp.distance(dest);
        if (llp.getLatitude() > 0) {
            llp.setLatLon(Math.toRadians(llp.getLatitude()) - distance, Math.toRadians(llp.getLongitude()), true);
        } else {
            llp.setLatLon(Math.toRadians(llp.getLatitude()) + distance, Math.toRadians(llp.getLongitude()), true);
            th = -th / 2d;
        }

        Projection proj = getMapBean().getProjection();
        Point2D pt = proj.forward(llp);
        pt.setLocation(pt.getX() - (r.getWidth() / 2d), pt.getY() - th);

        OMText text = new OMText((int) pt.getX(), (int) pt.getY(), infoText, OMText.JUSTIFY_LEFT);

        g.setPaintMode();
        g.setColor(Color.BLACK);

        prepareLabel(text);

        text.generate(proj);
        text.render(g);
    }

    /**
     * Customizes the given OMPoint before it is rendered.
     *
     * @param point OMPoint.
     */
    protected void preparePoint(OMPoint point) {
    }

    /**
     * Customizes the given OMCicle before it is rendered.
     *
     * @param circle OMCircle.
     */
    protected void prepareCircle(OMCircle circle) {
    }

    /**
     * Customizes the given OMText before it is rendered.
     *
     * @param text OMText.
     */
    protected void prepareLabel(OMText text) {
        text.setLinePaint(Color.BLACK);
        text.setTextMatteColor(getMapBean().getBackground());
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
     * @param dest A point on a circle.
     * @return label String.
     */
    protected String getLabelFor(LatLonPoint dest) {
        Format distFormat = getDistanceFormat();
        double distance = origin.distance(dest);
        if (distFormat == null) {
            return Double.toString(distance);
        }
        return distFormat.format(new Double(distance));
    }

    /**
     * Called when the origin point of the range rings has been selected, before
     * painting on the map.
     */
    protected void startUp() {
    }

    /**
     * Called when the origin point of the range is is known, and the mouse is
     * moving on the map, but before painting on the map.
     */
    protected void update() {
    }

    /**
     * Called when the end point of the range rings has been selected, before
     * painting on the map.
     */
    protected void finished() {
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

    private AffineTransform getTranslation(LatLonPoint pt1, LatLonPoint pt2, Projection proj) {
        Point2D p1 = proj.forward(pt1);
        Point2D p2 = proj.forward(pt2);
        return AffineTransform.getTranslateInstance(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }

    private LatLonPoint translate(LatLonPoint pt, AffineTransform xyTranslation, Projection proj) {
        Point2D p = proj.forward(pt);
        xyTranslation.transform(p, p);
        return (LatLonPoint) proj.inverse(p, new LatLonPoint.Double());
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        numRings = PropUtils.intFromProperties(props, prefix + NUM_RINGS_PROPERTY, numRings);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(getPropertyPrefix());

        props.setProperty(prefix + NUM_RINGS_PROPERTY, Integer.toString(numRings));

        return props;
    }

    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.setProperty(NUM_RINGS_PROPERTY, "Number of range rings to be drawn (minimum=1; default=3).");

        return list;
    }
}
