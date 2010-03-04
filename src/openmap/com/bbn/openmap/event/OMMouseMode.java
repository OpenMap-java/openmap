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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapBeanRepaintPolicy;
import com.bbn.openmap.PanDelayMapBeanRepaintPolicy;
import com.bbn.openmap.image.ImageScaler;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine;
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

    public final static String OpaquenessProperty = "opaqueness";
    public final static String LeaveShadowProperty = "leaveShadow";
    public final static String UseCursorProperty = "useCursor";
    public final static String UnitProperty = "units";
    public final static String ShowCircleProperty = "showCircle";
    public final static String ShowAngleProperty = "showAngle";
    public final static String RepaintToCleanProperty = "repaintToClean";

    public final static float DEFAULT_OPAQUENESS = 1.0f;

    public final static transient String modeID = "Gestures";

    private boolean isPanning = false;
    private BufferedImage bufferedMapImage = null;
    private BufferedImage bufferedRenderingImage = null;
    private int beanBufferWidth = 0;
    private int beanBufferHeight = 0;
    private int oX, oY;
    private float opaqueness = DEFAULT_OPAQUENESS;
    private boolean leaveShadow = false;
    private boolean useCursor;
    public transient DecimalFormat df = new DecimalFormat("0.###");
    // The unit type, default mile
    private Length unit = Length.MILE;
    // Flag to display the azimuth angle. Default true
    boolean showAngle = true;
    // Flag to repaint the map to clean up
    boolean repaintToClean = false;
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
    protected MapBean theMap = null;
    protected String coordString = null;

    public OMMouseMode() {
        super(modeID, true);
        setUseCursor(false);
        setLeaveShadow(true);
        setOpaqueness(DEFAULT_OPAQUENESS);
    }

    public void setActive(boolean val) {
        if (!val) {
            if (bufferedMapImage != null) {
                bufferedMapImage.flush();
            }
            if (bufferedRenderingImage != null) {
                bufferedRenderingImage.flush();
            }
            beanBufferWidth = 0;
            beanBufferHeight = 0;
            bufferedMapImage = null;
            bufferedRenderingImage = null;
        }
    }

    /**
     * @return Returns the useCursor.
     */
    public boolean isUseCursor() {
        return useCursor;
    }

    /**
     * @param useCursor The useCursor to set.
     */
    public void setUseCursor(boolean useCursor) {
        this.useCursor = useCursor;
        if (useCursor) {
            /*
             * For who like make his CustomCursor
             */
            try {
                Toolkit tk = Toolkit.getDefaultToolkit();
                ImageIcon pointer = new ImageIcon(getClass().getResource("Gestures.gif"));
                Dimension bestSize = tk.getBestCursorSize(pointer.getIconWidth(),
                        pointer.getIconHeight());
                Image pointerImage = ImageScaler.getOptimalScalingImage(pointer.getImage(),
                        (int) bestSize.getWidth(),
                        (int) bestSize.getHeight());
                Cursor cursor = tk.createCustomCursor(pointerImage,
                        new Point(0, 0),
                        "PP");
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
        setRepaintToClean(PropUtils.booleanFromProperties(props, prefix
                + RepaintToCleanProperty, isRepaintToClean()));

    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + OpaquenessProperty, Float.toString(getOpaqueness()));
        props.put(prefix + LeaveShadowProperty,
                Boolean.toString(isLeaveShadow()));
        props.put(prefix + UseCursorProperty, Boolean.toString(isUseCursor()));
        String unitValue = (unit != null ? unit.toString()
                : AllUnitsPropertyValue);
        props.put(prefix + UnitProperty, unitValue);
        props.put(prefix + ShowCircleProperty,
                new Boolean(isDisplayCircle()).toString());
        props.put(prefix + ShowAngleProperty,
                new Boolean(isShowAngle()).toString());
        props.put(prefix + RepaintToCleanProperty,
                new Boolean(isRepaintToClean()).toString());
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                OMMouseMode.class,
                OpaquenessProperty,
                "Transparency",
                "Transparency level for moving map, between 0 (clear) and 1 (opaque).",
                null);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                OMMouseMode.class,
                LeaveShadowProperty,
                "Leave Shadow",
                "Display current map in background while panning.",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                OMMouseMode.class,
                UseCursorProperty,
                "Use Cursor",
                "Use hand cursor for mouse mode.",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                OMMouseMode.class,
                UnitProperty,
                "Units",
                "Units to use for measurements, from Length.name possibilities.",
                null);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                OMMouseMode.class,
                ShowCircleProperty,
                "Show Distance Circle",
                "Flag to set whether the range circle is drawn at the end of the line (true/false).",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                OMMouseMode.class,
                ShowAngleProperty,
                "Show Angle",
                "Flag to note the azimuth angle of the line in the information line (true/false).",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                OMMouseMode.class,
                RepaintToCleanProperty,
                "Paint to Clean",
                "Flag to tell the map to repaint to clean up on a double click (true/false).",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return props;
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     *      The first click for drag, the image is generated. This image is
     *      redrawing when the mouse is move, but, I need to repain the original
     *      image.
     */
    public void mouseDragged(MouseEvent arg0) {

        // Left mouse click, pan
        if (SwingUtilities.isLeftMouseButton(arg0)) {
            MapBean mb = ((MapBean) arg0.getSource());

            MapBeanRepaintPolicy mbrp = mb.getMapBeanRepaintPolicy();
            if (mbrp instanceof PanDelayMapBeanRepaintPolicy) {
                ((PanDelayMapBeanRepaintPolicy) mbrp).setPanning(true);
            }

            Point2D pnt = mb.getNonRotatedLocation(arg0);
            int x = (int) pnt.getX();
            int y = (int) pnt.getY();

            if (!isPanning) {

                oX = x;
                oY = y;

                isPanning = true;

            } else {

                ((BufferedMapBean) mb).panningTransform = AffineTransform.getTranslateInstance(x
                        - oX,
                        y - oY);
                mb.repaint();
            }
        } else {

            if (rPoint1 == null) {
                rPoint1 = theMap.getCoordinates(arg0);

            } else {
                // right mouse click, measure
                double lat1, lat2, long1, long2;
                // set the map bean
                theMap = (MapBean) (arg0.getSource());
                // erase the old line and circle first
                paintRubberband(rPoint1, rPoint2, coordString);
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
                distance = GreatCircle.sphericalDistance(ProjMath.degToRad(lat1),
                        ProjMath.degToRad(long1),
                        ProjMath.degToRad(lat2),
                        ProjMath.degToRad(long2));

                // calculate azimuth angle dec deg
                double azimuth = getSphericalAzimuth(lat1, long1, lat2, long2);
                coordString = createDistanceInformationLine(rPoint2,
                        distance,
                        azimuth);

                // paint the new line and circle up to the current
                // mouse location
                paintRubberband(rPoint1, rPoint2, coordString);
            }
        }
        super.mouseDragged(arg0);
    }

    /**
     * Process a mouse pressed event. Add the mouse location to the segment
     * vector. Calculate the cumulative total distance.
     * 
     * @param e mouse event.
     */
    public void mousePressed(MouseEvent e) {
        mouseSupport.fireMapMousePressed(e);
        e.getComponent().requestFocus();

        if (SwingUtilities.isRightMouseButton(e)) {
            // mouse has now been pressed
            mousePressed = true;
            // erase the old circle if any
            eraseCircle();

            if (theMap == null) {
                theMap = (MapBean) e.getSource();
            }

            // anchor the new first point of the line
            rPoint1 = theMap.getCoordinates(e);
            // ensure the second point is not yet set.
            rPoint2 = null;
            // add the distance to the total distance
            totalDistance = 0;
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

            Object obj = arg0.getSource();
            if (!(obj instanceof MapBean)) {
                return;
            }

            MapBean mb = (MapBean) obj;

            MapBeanRepaintPolicy mbrp = mb.getMapBeanRepaintPolicy();
            if (mbrp instanceof PanDelayMapBeanRepaintPolicy) {
                ((PanDelayMapBeanRepaintPolicy) mbrp).setPanning(false);
            }

            Projection proj = mb.getProjection();
            Point2D center = proj.forward(proj.getCenter());

            Point2D pnt = mb.getNonRotatedLocation(arg0);
            int x = (int) pnt.getX();
            int y = (int) pnt.getY();

            ((BufferedMapBean) mb).panningTransform = null;

            center.setLocation(center.getX() - x + oX, center.getY() - y + oY);
            mb.setCenter(proj.inverse(center));

            isPanning = false;
            // bufferedMapImage = null; //clean up when not active...
        } else {
            if (theMap != null) {
                if (!repaintToClean) {
                    // erase all line segments
                    eraseLines();
                    // erase the last circle
                    eraseCircle();
                } else {
                    theMap.repaint();
                }
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

    public void projectionChanged(ProjectionEvent e) {
        Object obj = e.getSource();
        if (obj instanceof MapBean) {
            MapBean mb = (MapBean) obj;
            int w = mb.getWidth();
            int h = mb.getHeight();
            createBuffers(w, h);
        }
    }

    /**
     * Instantiates new image buffers if needed.<br>
     * This method is synchronized to avoid creating the images multiple times
     * if width and height doesn't change.
     * 
     * @param w mapBean's width.
     * @param h mapBean's height.
     */
    public synchronized void createBuffers(int w, int h) {
        if (w > 0 && h > 0 && (w != beanBufferWidth || h != beanBufferHeight)) {
            beanBufferWidth = w;
            beanBufferHeight = h;
            createBuffersImpl(w, h);
        }
    }

    /**
     * Instantiates new image buffers.
     * 
     * @param w Non-zero mapBean's width.
     * @param h Non-zero mapBean's height.
     */
    protected void createBuffersImpl(int w, int h) {
        // Release system resources used by previous images...
        if (bufferedMapImage != null) {
            bufferedMapImage.flush();
        }
        if (bufferedRenderingImage != null) {
            bufferedRenderingImage.flush();
        }
        // New images...
        bufferedMapImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        bufferedRenderingImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Draw a rubberband line and circle between two points
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     */
    public void paintRubberband(Point2D pt1, Point2D pt2, String coordString) {
        if (theMap != null) {
            paintRubberband(pt1, pt2, coordString, theMap.getGraphics(true));
        }
    }

    /**
     * Draw a rubberband line and circle between two points
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     * @param g a java.awt.Graphics object to render into.
     */
    public void paintRubberband(Point2D pt1, Point2D pt2, String coordString,
                                Graphics g) {
        paintLine(pt1, pt2, g);
        paintCircle(pt1, pt2, g);
        paintText(pt1, pt2, coordString, g);
    }

    /**
     * Draw a rubberband line between two points
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     */
    public void paintLine(Point2D pt1, Point2D pt2) {
        if (theMap != null) {
            paintLine(pt1, pt2, theMap.getGraphics(true));
        }
    }

    /**
     * Draw a rubberband line between two points into the Graphics object.
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     * @param graphics a java.awt.Graphics object to render into.
     */
    public void paintLine(Point2D pt1, Point2D pt2, Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setXORMode(java.awt.Color.lightGray);
        g.setColor(java.awt.Color.darkGray);
        if (pt1 != null && pt2 != null) {
            // the line connecting the segments
            OMLine cLine = new OMLine(pt1.getY(), pt1.getX(), pt2.getY(), pt2.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
            // get the map projection
            Projection proj = theMap.getProjection();
            // prepare the line for rendering
            cLine.generate(proj);
            // render the line graphic
            cLine.render(g);
        }
    }

    public void paintText(Point2D base, Point2D pt1, String coordString,
                          Graphics graphics) {
        if (coordString != null) {
            Graphics2D g = (Graphics2D) graphics;
            g.setXORMode(java.awt.Color.lightGray);
            g.setColor(java.awt.Color.darkGray);
            base = theMap.getProjection().forward(base);
            pt1 = theMap.getProjection().forward(pt1);

            if (base.distance(pt1) > 3) {
                g.drawString(coordString,
                        (int) pt1.getX() + 5,
                        (int) pt1.getY() - 5);
            }
        }
    }

    /**
     * Draw a rubberband circle between two points
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     */
    public void paintCircle(Point2D pt1, Point2D pt2) {
        if (theMap != null) {
            paintCircle(pt1, pt2, theMap.getGraphics(true));
        }
    }

    /**
     * Draw a rubberband circle between two points
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     * @param graphics a java.awt.Graphics object to render into.
     */
    public void paintCircle(Point2D pt1, Point2D pt2, Graphics graphics) {
        // do all this only if want to display the rubberband circle
        if (displayCircle) {
            Graphics2D g = (Graphics2D) graphics;
            g.setXORMode(java.awt.Color.lightGray);
            g.setColor(java.awt.Color.darkGray);
            if (pt1 != null && pt2 != null) {
                // first convert degrees to radians
                double radphi1 = ProjMath.degToRad(pt1.getY());
                double radlambda0 = ProjMath.degToRad(pt1.getX());
                double radphi = ProjMath.degToRad(pt2.getY());
                double radlambda = ProjMath.degToRad(pt2.getX());
                // calculate the circle radius
                double dRad = GreatCircle.sphericalDistance(radphi1,
                        radlambda0,
                        radphi,
                        radlambda);
                // convert into decimal degrees
                double rad = ProjMath.radToDeg(dRad);
                // make the circle
                OMCircle circle = new OMCircle(pt1.getY(), pt1.getX(), rad);
                // get the map projection
                Projection proj = theMap.getProjection();
                // prepare the circle for rendering
                circle.generate(proj);
                // render the circle graphic
                circle.render(g);
            }
        } // end if(displayCircle)
    }

    /**
     * Erase all line segments.
     */
    public void eraseLines() {
        for (int i = 0; i < segments.size() - 1; i++) {
            paintLine((Point2D) (segments.elementAt(i)),
                    (Point2D) (segments.elementAt(i + 1)));
        }
    }

    /**
     * Erase the current segment circle.
     */
    public void eraseCircle() {
        paintCircle(rPoint1, rPoint2);
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
     * @param phi1 latitude in decimal degrees of start point
     * @param lambda0 longitude in decimal degrees of start point
     * @param phi latitude in decimal degrees of end point
     * @param lambda longitude in decimal degrees of end point
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
        double az = GreatCircle.sphericalAzimuth(radphi1,
                radlambda0,
                radphi,
                radlambda);
        return ProjMath.radToDeg(az);
    }

    protected String createDistanceInformationLine(Point2D llp,
                                                   double distance,
                                                   double azimuth) {
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

    public boolean isRepaintToClean() {
        return repaintToClean;
    }

    public void setRepaintToClean(boolean repaintToClean) {
        this.repaintToClean = repaintToClean;
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
