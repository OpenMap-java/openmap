//Title: DistanceMouseMode.
//Version: 2.0
//Copyright:
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR DSTO BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
// OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
// BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
// USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
// DAMAGE.
//
//Author: R. Wathelet
//Company: Theatre Operations Branch, Defence Science & Technology
//Organisation (DSTO)

package com.bbn.openmap.event;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Planet;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * This mouse mode draws a rubberband line and circle between each mouse click
 * as the mouse is moved and displays the cumulative distance in nautical miles
 * (nm), kilometers (km), statute miles (miles) and the azimuth angle in decimal
 * degrees from north on the status bar. Several distance segments are allowed.
 * To erase (terminate) double click the mouse.
 * <p>
 * To use this mouse mode in the OpenMap demo (in setWidgets): create the mouse
 * mode, such as
 * <p>
 * DistanceMouseMode distMode = new DistanceMouseMode(true, id,
 * DistanceMouseMode.DISTANCE_ALL);
 * <p>
 * Add the distance mouse mode to the mouse delegator md.addMouseMode(distMode);
 * <p>
 * This class can easily be extended, for example to create waypoints for
 * objects.
 * <p>
 * NOTE: If some lines are not properly erased (because the mouse went outside
 * the map for example), just use the redraw from the menu.
 * <P>
 * 
 * You can set the units used for measurements by setting the property:
 * 
 * <pre>
 * 
 *      prefix.units= &amp;lt name for Length.java (km, miles, meters, nm, all) &amp;gt
 * 
 * </pre>
 * 
 * Note that "all" will display nm, km, and miles.
 * 
 */
public class DistanceMouseMode extends CoordMouseMode {

    /**
     * Mouse mode identifier, is "Distance". This is returned on getID()
     */
    public final static transient String modeID = "Distance";
    public final static String UnitProperty = "units";
    public final static String ShowCircleProperty = "showCircle";
    public final static String ShowAngleProperty = "showAngle";
    public final static String RepaintToCleanProperty = "repaintToClean";

    public transient DecimalFormat df = new DecimalFormat("0.###");

    /**
     * Special units value for displaying all units ... use only in properties
     * file
     */
    public final static String AllUnitsPropertyValue = "all";

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
     * The line type to be displayed, see OMGraphic. LINETYPE_GREATCIRCLE,
     * LINETYPE_RHUMB, LINETYPE_STRAIGHT default LINETYPE_GREATCIRCLE
     */
    public int lineType = OMGraphic.LINETYPE_GREATCIRCLE;

    /**
     * To display the rubberband circle, default true
     */
    private boolean displayCircle = true;

    // The unit type, default mile
    private Length unit = Length.MILE;

    // Flag to display the azimuth angle. Default true
    boolean showAngle = true;
    // Flag to repaint the map to clean up
    boolean repaintToClean = false;

    // The map bean
    protected transient MapBean theMap;

    /**
     * Construct a DistanceMouseMode. Default constructor. Sets the ID to the
     * modeID, and the consume mode to true. You need to setInfoDelegator,
     * setUnit and setLineType if you use this constructor.
     */
    public DistanceMouseMode() {
        this(true);
        // if you really want to change the cursor shape
        // setModeCursor(cursor.getPredefinedCursor(cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Construct a DistanceMouseMode. Lets you set the consume mode. If the
     * events are consumed, then a MouseEvent is sent only to the first
     * MapMouseListener that successfully processes the event. If they are not
     * consumed, then all of the listeners get a chance to act on the event. You
     * need to setInfoDelegator, setUnit and setLineType if you use this
     * constructor.
     * 
     * @param consumeEvents the mode setting.
     */
    public DistanceMouseMode(boolean consumeEvents) {
        super(modeID, consumeEvents);
        // if you really want to change the cursor shape
        // setModeCursor(cursor.getPredefinedCursor(cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Construct an DistanceMouseMode. For convenience for derived classes.
     * 
     * @param name the ID of the mode.
     * @param consumeEvents if true, events are propagated to the first
     *        MapMouseListener that successfully processes the event, if false,
     *        events are propagated to all MapMouseListeners. You need to
     *        setInfoDelegator, setUnit and setLineType if you use this
     *        constructor.
     */
    public DistanceMouseMode(String name, boolean consumeEvents) {
        super(name, consumeEvents);
        // if you really want to change the cursor shape
        // setModeCursor(cursor.getPredefinedCursor(cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Construct a DistanceMouseMode. Lets you set the consume mode. If the
     * events are consumed, then a MouseEvent is sent only to the first
     * MapMouseListener that successfully processes the event. If they are not
     * consumed, then all of the listeners get a chance to act on the event. You
     * need to the setLineType if you use this constructor.
     * 
     * @param consumeEvents the mode setting.
     * @param id the calling object's info delegator.
     * @param units the unit of distance that will be displayed, such as
     *        Length.NM, Length.KM or Length.MILE. If null, display all of them.
     */
    public DistanceMouseMode(boolean consumeEvents, InformationDelegator id,
            Length units) {
        super(modeID, consumeEvents);
        // if you really want to change the cursor shape
        // setModeCursor(cursor.getPredefinedCursor(cursor.CROSSHAIR_CURSOR));
        infoDelegator = id;
        unit = units;
    }

    /**
     * Construct a DistanceMouseMode. Lets you set the consume mode. If the
     * events are consumed, then a MouseEvent is sent only to the first
     * MapMouseListener that successfully processes the event. If they are not
     * consumed, then all of the listeners get a chance to act on the event. You
     * need to the setLineType if you use this constructor.
     * 
     * @param consumeEvents the mode setting.
     * @param id the calling object's info delegator.
     * @param units the unit of distance that will be displayed, such as
     *        Length.NM, Length.KM or Length.MILE. If null, display all of them.
     * @param lType the line type that will be displayed such as
     *        LINETYPE_GREATCIRCLE, LINETYPE_RHUMB, LINETYPE_STRAIGHT
     */
    public DistanceMouseMode(boolean consumeEvents, InformationDelegator id,
            Length units, int lType) {
        super(modeID, consumeEvents);
        // if you really want to change the cursor shape
        // setModeCursor(cursor.getPredefinedCursor(cursor.CROSSHAIR_CURSOR));
        infoDelegator = id;
        unit = units;
        lineType = lType;
    }

    /**
     * Construct a DistanceMouseMode. Lets you set the consume mode. If the
     * events are consumed, then a MouseEvent is sent only to the first
     * MapMouseListener that successfully processes the event. If they are not
     * consumed, then all of the listeners get a chance to act on the event.
     * 
     * @param consumeEvents the mode setting.
     * @param id the calling object's info delegator.
     */
    public DistanceMouseMode(boolean consumeEvents, InformationDelegator id) {
        super(modeID, consumeEvents);
        // if you really want to change the cursor shape
        // setModeCursor(cursor.getPredefinedCursor(cursor.CROSSHAIR_CURSOR));
        infoDelegator = id;
    }

    /**
     * Construct a DistanceMouseMode. For convenience for derived classes. Lets
     * you set the consume mode. If the events are consumed, then a MouseEvent
     * is sent only to the first MapMouseListener that successfully processes
     * the event. If they are not consumed, then all of the listeners get a
     * chance to act on the event.
     * 
     * @param name the ID of the mode.
     * @param consumeEvents the mode setting.
     * @param id the calling object's info delegator.
     */
    public DistanceMouseMode(String name, boolean consumeEvents,
            InformationDelegator id) {
        super(name, consumeEvents);
        // if you really want to change the cursor shape
        // setModeCursor(cursor.getPredefinedCursor(cursor.CROSSHAIR_CURSOR));
        infoDelegator = id;
    }

    /**
     * Process a mouseClicked event. Erase all drawn lines and circles upon a
     * double mouse click
     * 
     * @param e mouse event.
     */
    public void mouseClicked(MouseEvent e) {

        mouseSupport.fireMapMouseClicked(e);

        if (e.getSource() instanceof MapBean) {
            // if double (or more) mouse clicked
            if (e.getClickCount() >= 2) {
                // end of distance path
                mousePressed = false;
                // add the last point to the line segments
                segments.addElement(rPoint2);
                if (!repaintToClean) {
                    // erase all line segments
                    eraseLines();
                    // erase the last circle
                    eraseCircle();
                } else {
                    ((MapBean) e.getSource()).repaint();
                }
                // cleanup
                cleanUp();
            }
        }
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

        if (e.getSource() instanceof MapBean) {
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
            // add the anchor point to the list of line segments
            segments.addElement(rPoint1);
            // add the distance to the total distance
            totalDistance += distance;
        }
    }

    /**
     * Draw a rubberband line and circle as the mouse is moved. Calculate
     * distance and azimuth angle as the mouse moves. Display distance and
     * azimuth angle in on the infoDelegator.
     * 
     * @param e mouse event.
     */
    public void mouseMoved(MouseEvent e) {
        mouseSupport.fireMapMouseMoved(e);
        if (e.getSource() instanceof MapBean) {
            // only when the mouse has already been pressed
            if (mousePressed) {
                double lat1, lat2, long1, long2;
                // set the map bean
                theMap = (MapBean) (e.getSource());
                // erase the old line and circle first
                paintRubberband(rPoint1, rPoint2);
                // get the current mouse location in latlon
                rPoint2 = theMap.getCoordinates(e);
                // paint the new line and circle up to the current
                // mouse location
                paintRubberband(rPoint1, rPoint2);

                if (infoDelegator != null) {
                    Debug.message("mousemodedetail",
                            "DistanceMouseMode: firing mouse location");
                    // lat, lon of anchor point
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
                    double azimuth = getSphericalAzimuth(lat1,
                            long1,
                            lat2,
                            long2);
                    // convert total distance into all distance units
                    // String distNM =
                    // df.format(totalDistance+distance);
                    double tmpDistance = totalDistance + distance;
                    String infoLine = createDistanceInformationLine(rPoint2,
                            tmpDistance,
                            azimuth);
                    // setup the info event
                    InfoDisplayEvent info = new InfoDisplayEvent(this, infoLine, InformationDelegator.COORDINATE_INFO_LINE);
                    // ask the infoDelegator to display the info
                    infoDelegator.requestInfoLine(info);
                }
            } else {
                fireMouseLocation(e);
            }
        }
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
            unitInfo = unit.fromRadians((float) distance) + " "
                    + unit.getAbbr();
        }

        // add the mouse lat, lon
        StringBuffer infoLine = new StringBuffer();
        infoLine.append("Lat, Lon (").append(df.format(llp.getY())).append(", ")
                .append(df.format(llp.getX())).append("), distance (");

        // add the units
        infoLine.append(unitInfo).append(")");
        // add the azimuth angle if need be
        if (showAngle) {
            infoLine.append(", angle (").append(df.format(azimuth)).append(")");
        }
        return infoLine.toString();
    }

    /**
     * Process a mouseEntered event. Record the mouse source object, a map bean.
     * 
     * @param e mouse event.
     */
    public void mouseEntered(MouseEvent e) {
        mouseSupport.fireMapMouseEntered(e);
        // get the map bean
        if (e.getSource() instanceof MapBean)
            theMap = (MapBean) (e.getSource());
    }

    /**
     * Process a mouseExited event. If a line is being drawn (and mouse go off
     * the map), it will be erased. The anchor point rPoint1 is kept in case the
     * mouse comes back on the screen. Then, a new line will be drawn with the
     * original mouse press position.
     * 
     * @param e mouse event.
     */
    public void mouseExited(MouseEvent e) {
        mouseSupport.fireMapMouseExited(e);
        if (e.getSource() instanceof MapBean) {
            // erase the old line first
            paintRubberband(rPoint1, rPoint2);
            // set the second point to null so that a new line will be
            // re-drawn if the mouse comes back, and the line will use
            // the old starting point.
            rPoint2 = null;
        }
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
            OMLine cLine = new OMLine(pt1.getY(), pt1.getX(), pt2.getY(), pt2.getX(), lineType);
            // get the map projection
            Projection proj = theMap.getProjection();
            // prepare the line for rendering
            cLine.generate(proj);
            // render the line graphic
            cLine.render(g);
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
     * Draw a rubberband line and circle between two points
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     */
    public void paintRubberband(Point2D pt1, Point2D pt2) {
        if (theMap != null) {
            paintRubberband(pt1, pt2, theMap.getGraphics(true));
        }
    }

    /**
     * Draw a rubberband line and circle between two points
     * 
     * @param pt1 the anchor point.
     * @param pt2 the current (mouse) position.
     * @param g a java.awt.Graphics object to render into.
     */
    public void paintRubberband(Point2D pt1, Point2D pt2, Graphics g) {
        paintLine(pt1, pt2, g);
        paintCircle(pt1, pt2, g);
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
    }

    /**
     * Return the distance in the chosen unit between two points (in decimal
     * degrees). Based on spherical arc distance between two points. See class
     * GreatCircle.java
     * 
     * @param phi1 latitude in decimal degrees of start point
     * @param lambda0 longitude in decimal degrees of start point
     * @param phi latitude in decimal degrees of end point
     * @param lambda longitude in decimal degrees of end point
     * @param units the unit of distance, DISTANCE_NM, DISTANCE_KM,
     *        DISTANCE_MILE or all 3 types DISTANCE_ALL
     * @return double distance in chosen unit
     */
    public double getGreatCircleDist(double phi1, double lambda0, double phi,
                                     double lambda, int units) {
        double dist = 0;
        // convert arguments to radians
        double radphi1 = ProjMath.degToRad(phi1);
        double radlambda0 = ProjMath.degToRad(lambda0);
        double radphi = ProjMath.degToRad(phi);
        double radlambda = ProjMath.degToRad(lambda);
        // get the spherical distance in radians between the two
        // points
        double distRad = (double) GreatCircle.sphericalDistance(radphi1,
                radlambda0,
                radphi,
                radlambda);
        // in the chosen unit
        if (units == 0)
            dist = distRad * Planet.wgs84_earthEquatorialCircumferenceNMiles
                    / MoreMath.TWO_PI;
        if (units == 1)
            dist = distRad * Planet.wgs84_earthEquatorialCircumferenceKM
                    / MoreMath.TWO_PI;
        if (units == 2)
            dist = distRad * Planet.wgs84_earthEquatorialCircumferenceMiles
                    / MoreMath.TWO_PI;

        return dist;
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

    /**
     * Set the map bean.
     * 
     * @param aMap a map bean
     */
    public void setMapBean(MapBean aMap) {
        theMap = aMap;
    }

    /**
     * Return the map bean.
     */
    public MapBean getMapBean() {
        return theMap;
    }

    /**
     * Set the unit of distance to be displayed: Length.NM, Length.KM or
     * Length.MILE. If null, displays all of them.
     */
    public void setUnit(Length units) {
        unit = units;
    }

    /**
     * Return the unit of distance being displayed: Length.NM, Length.KM or
     * Length.MILE. If null, displays all of them.
     */
    public Length getUnit() {
        return unit;
    }

    /**
     * Switch the display of the azimuth angle on or off.
     * 
     * @param onOff true to display the azimuth angle, false to turn off
     */
    public void showAzimuth(boolean onOff) {
        showAngle = onOff;
    }

    /**
     * Whether the display of the azimuth angle on or off.
     */
    public boolean getShowAzimuth() {
        return showAngle;
    }

    /**
     * Set the line type to be drawn see also OMGraphic
     * 
     * @param lype either LINETYPE_GREATCIRCLE, LINETYPE_RHUMB,
     *        LINETYPE_STRAIGHT
     */
    public void setLineType(int lype) {
        lineType = lype;
    }

    /**
     * Return the line type either LINETYPE_GREATCIRCLE, LINETYPE_RHUMB,
     * LINETYPE_STRAIGHT
     */
    public int getLineType() {
        return lineType;
    }

    /**
     * Set the drawing of the rubberband circle on/off.
     * 
     * @param onOff true or false
     */
    public void showCircle(boolean onOff) {
        displayCircle = onOff;
    }

    /**
     * Get whether the drawing of the rubberband circle on/off.
     */
    public boolean getShowCircle() {
        return displayCircle;
    }

    public void setRepaintToClean(boolean rtc) {
        repaintToClean = rtc;
    }

    public boolean getRepaintToClean() {
        return repaintToClean;
    }

    /**
     * PropertyConsumer interface method.
     */
    public void setProperties(String prefix, Properties setList) {
        super.setProperties(prefix, setList);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String name = setList.getProperty(prefix + UnitProperty);
        if (name != null) {
            Length length = Length.get(name);
            if (length != null) {
                setUnit(length);
            } else if (name.equals(AllUnitsPropertyValue)) {
                setUnit(null);
            }
        }

        showCircle(PropUtils.booleanFromProperties(setList, prefix
                + ShowCircleProperty, true));
        showAzimuth(PropUtils.booleanFromProperties(setList, prefix
                + ShowAngleProperty, true));
        setRepaintToClean(PropUtils.booleanFromProperties(setList, prefix
                + RepaintToCleanProperty, false));
    }

    /**
     * PropertyConsumer interface method.
     */
    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        String unitValue = (unit != null ? unit.toString()
                : AllUnitsPropertyValue);
        getList.put(prefix + UnitProperty, unitValue);
        getList.put(prefix + ShowCircleProperty,
                new Boolean(getShowCircle()).toString());
        getList.put(prefix + ShowAngleProperty,
                new Boolean(getShowAzimuth()).toString());
        getList.put(prefix + RepaintToCleanProperty,
                new Boolean(getRepaintToClean()).toString());
        return getList;
    }

    /**
     * PropertyConsumer interface method.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.put(UnitProperty,
                "Units to use for measurements, from Length.name possibilities.");
        list.put(ShowCircleProperty,
                "Flag to set whether the range circle is drawn at the end of the line (true/false).");
        list.put(ShowAngleProperty,
                "Flag to note the azimuth angle of the line in the information line (true/false).");
        list.put(RepaintToCleanProperty,
                "Flag to tell the map to repaint to clean up on a double click (true/false).");
        return list;
    }

    /**
     * Called by the MapBean when it repaints, to let the MouseMode know when to
     * update itself on the map. PaintListener interface.
     */
    public void listenerPaint(java.awt.Graphics g) {
        for (int i = 0; i < segments.size() - 1; i++) {
            paintLine((LatLonPoint) (segments.elementAt(i)),
                    (LatLonPoint) (segments.elementAt(i + 1)),
                    g);
        }
        paintRubberband(rPoint1, rPoint2, g);
    }

    public boolean isDisplayCircle() {
        return displayCircle;
    }

    public void setDisplayCircle(boolean displayCircle) {
        this.displayCircle = displayCircle;
    }

    public boolean isShowAngle() {
        return showAngle;
    }

    public void setShowAngle(boolean showAngle) {
        this.showAngle = showAngle;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

}