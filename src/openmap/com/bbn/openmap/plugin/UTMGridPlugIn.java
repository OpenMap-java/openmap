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
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/UTMGridPlugIn.java,v $
// $RCSfile: UTMGridPlugIn.java,v $
// $Revision: 1.21 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.I18n;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColorChooser;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.geom.BasicGeometry;
import com.bbn.openmap.omGraphics.geom.PolygonGeometry;
import com.bbn.openmap.omGraphics.geom.PolylineGeometry;
import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;
import com.bbn.openmap.proj.coords.UTMPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.quadtree.QuadTree;

/**
 * The UTMGridPlugIn renders UTM Zone areas, and renders a grid marking
 * equal-distance areas around the center of the current projection. This
 * distance grid only extends east-west for 500km in both directions from the
 * center of the current zone because that is the extent of accuracy for those
 * measurements - after that, you get too far away from the central meridian for
 * the current UTM zone.
 * <p>
 * 
 * Currently, this plugin only draws 100km distance squares. Updates on the way.
 * The plugin has the following properties that may be set:
 * <p>
 * 
 * <pre>
 * 
 * 
 *    # Turn zone area labels on when zoomed in closer than 1:33M (true
 *    # is default)
 *    showZones=true
 *    showLabels=true
 *    # Color for UTM Zone area boundaries
 *    utmGridColor=hex AARRGGBB value
 *    # Color for the distance area grid lines
 *    distanceGridColor= hex AARRGGBB value
 *    labelCutoffScale=scale to start showing labels, default is 33000000
 *    show100KmGrid=false
 *    utmGridColor
 *    distanceGridColor
 *    distanceGridResolution=0 (not shown by default, 1 = 10000 meter grid, 5 is 1 meter grid)
 *    mgrsLabels=false
 * 
 * </pre>
 */
public class UTMGridPlugIn extends OMGraphicHandlerPlugIn {

    protected boolean UTM_DEBUG = false;
    protected boolean UTM_DEBUG_VERBOSE = false;

    public final static int INTERVAL_100K = 100000;
    public final static float DEFAULT_UTM_LABEL_CUTOFF_SCALE = 33000000;

    protected boolean showZones = true;
    protected boolean showLabels = true;
    protected float labelCutoffScale = DEFAULT_UTM_LABEL_CUTOFF_SCALE;
    protected boolean show100kGrid = false;
    protected boolean labelsAsMGRS = false;
    /**
     * Resolution should be MGRS accuracy, 0 for none, 1-5 otherwise, where 1 =
     * 10000 meter grid, 5 is 1 meter grid.
     */
    protected int distanceGridResolution = 0;
    protected Paint utmGridPaint = Color.black;
    protected Paint distanceGridPaint = Color.black;

    /**
     * Used to hold OMText UTM zone labels.
     */
    protected QuadTree labelTree;
    /**
     * Used for UTM zone labels.
     */
    protected OMGraphicList labelList;
    /**
     * The vertical list of OMLines used for UTM zones.
     */
    protected OMGeometryList verticalList;
    /**
     * The horizontal list of OMLines used for UTM zones.
     */
    protected OMGeometryList horizontalList;

    public final static String ShowLabelsProperty = "showLabels";
    public final static String ShowZonesProperty = "showZones";
    public final static String LabelCutoffScaleProperty = "labelCutoffScale";
    public final static String Show100kGridProperty = "show100KmGrid";
    public final static String UTMGridColorProperty = "utmGridColor";
    public final static String DistanceGridColorProperty = "distanceGridColor";
    public final static String DistanceGridResolutionProperty = "distanceGridResolution";
    public final static String LabelsAsMGRSProperty = "mgrsLabels";

    public UTMGridPlugIn() {
        UTM_DEBUG = Debug.debugging("utmgrid");
        UTM_DEBUG_VERBOSE = Debug.debugging("utmgrid_verbose");
    }

    protected OMGeometryList createUTMZoneVerticalLines() {

        OMGeometryList verticalList = new OMGeometryList();
        double[] points = null;

        for (double lon = -180; lon < 180; lon += 6) {
            if (lon == 6) {
                points = new double[] { 56f, lon, -80f, lon };
            } else if (lon > 6 && lon < 42) {
                points = new double[] { 72f, lon, -80f, lon };
            } else {
                points = new double[] { 84f, lon, -80f, lon };
            }
            verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));
        }

        points = new double[] { 72f, 6f, 64f, 6f };
        verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

        points = new double[] { 64f, 3f, 56f, 3f };
        verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

        points = new double[] { 84f, 9f, 72f, 9f };
        verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

        points = new double[] { 84f, 21f, 72f, 21f };
        verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

        points = new double[] { 84f, 33f, 72f, 33f };
        verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

        verticalList.setLinePaint(utmGridPaint);

        return verticalList;
    }

    protected OMGeometryList createUTMZoneHorizontalLines() {
        OMGeometryList horizontalList = new OMGeometryList();
        double[] points = null;

        for (double lat = -80f; lat <= 72f; lat += 8f) {
            points = new double[] { lat, -180f, lat, -90f, lat, 0f, lat, 90f,
                    lat, 180f };
            horizontalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB));
        }

        points = new double[] { 84f, -180f, 84f, -90f, 84f, 0f, 84f, 90f, 84f,
                180f };
        horizontalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB));

        horizontalList.setLinePaint(utmGridPaint);

        return horizontalList;
    }

    protected QuadTree createUTMZoneLabels() {

        QuadTree labelTree = new QuadTree();

        // Need to use MGRSPoint to get MGRS zone letters, the UTM
        // zone letters are N and S for the hemisphere, which isn't
        // very interesting.
        UTMPoint utm = new MGRSPoint();
        LatLonPoint llp = new LatLonPoint.Double();
        float latitude;
        float longitude;

        for (int lat = -80; lat <= 72; lat += 8) {
            for (int lon = -180; lon < 180; lon += 6) {

                latitude = (float) lat;
                longitude = (float) lon;

                if (lat == 56 && lon == 6) {
                    longitude = 3f;
                } else if (lat == 72 && (lon > 0 && lon < 42)) {
                    continue;
                }
                llp.setLatLon(latitude, longitude);
                addLabel(llp, UTMPoint.LLtoUTM(llp, utm), labelTree);
            }
        }

        latitude = 72f;
        llp.setLatLon(latitude, 9f);
        addLabel(llp, UTMPoint.LLtoUTM(llp, utm), labelTree);
        llp.setLongitude(21f);
        addLabel(llp, UTMPoint.LLtoUTM(llp, utm), labelTree);
        llp.setLongitude(33f);
        addLabel(llp, UTMPoint.LLtoUTM(llp, utm), labelTree);

        return labelTree;
    }

    protected QuadTree createMGRSZoneLabels() {

        QuadTree labelTree = new QuadTree();

        // Need to use MGRSPoint to get MGRS zone letters, the UTM
        // zone letters are N and S for the hemisphere, which isn't
        // very interesting.
        MGRSPoint mgrs = new MGRSPoint();
        LatLonPoint llp = new LatLonPoint.Double();
        float latitude;
        float longitude;

        for (int lat = -80; lat <= 72; lat += 8) {
            for (int lon = -180; lon < 180; lon += 6) {

                latitude = (float) lat;
                longitude = (float) lon;

                if (lat == 56 && lon == 6) {
                    longitude = 3f;
                } else if (lat == 72 && (lon > 0 && lon < 42)) {
                    continue;
                }
                llp.setLatLon(latitude, longitude);
                addLabel(llp, MGRSPoint.LLtoMGRS(llp, mgrs), labelTree);
            }
        }

        latitude = 72f;
        llp.setLatLon(latitude, 9f);
        addLabel(llp, MGRSPoint.LLtoMGRS(llp, mgrs), labelTree);
        llp.setLongitude(21f);
        addLabel(llp, MGRSPoint.LLtoMGRS(llp, mgrs), labelTree);
        llp.setLongitude(33f);
        addLabel(llp, MGRSPoint.LLtoMGRS(llp, mgrs), labelTree);

        return labelTree;
    }

    protected void addLabel(LatLonPoint llp, UTMPoint utm, QuadTree labelTree) {
        double latitude = llp.getY();
        double longitude = llp.getX();
        labelTree.put((float) latitude,
                (float) longitude,
                new OMText(latitude, longitude, 2, -2, utm.zone_number + ""
                        + utm.zone_letter, OMText.JUSTIFY_LEFT));
    }

    /**
     * Called to create 100km distance grid lines. Was originally designed to
     * accept different gridLineInterval distances, but has only been debugged
     * and tested for 100000.
     * 
     * @param utm the UTMPoint of the center of the area to create lines for.
     */
    protected OMGraphicList createEquiDistanceLines(UTMPoint utm,
                                                    int gridLineInterval) {

        OMGraphicList list = new OMGraphicList();

        // Used to calculate the endpoints of the horizontal lines.
        UTMPoint utm1 = new UTMPoint(utm);
        UTMPoint utm2 = new UTMPoint(utm);
        LatLonPoint point1 = new LatLonPoint.Double();
        LatLonPoint point2 = new LatLonPoint.Double();

        // Used to calculate the pieces of the vertical lines.
        UTMPoint utmp = new UTMPoint(utm);
        LatLonPoint llp = new LatLonPoint.Double();

        int i;
        OMLine line;
        BasicGeometry poly;

        double lat2;
        int endNorthing = (int) Math.floor(utm.northing / INTERVAL_100K) + 10;
        int startNorthing = (int) Math.floor(utm.northing / INTERVAL_100K) - 10;

        int numVertLines = 9;
        int numHorLines = endNorthing - startNorthing;

        double[][] vertPoints = new double[numVertLines][numHorLines * 2];

        if (UTM_DEBUG_VERBOSE) {
            Debug.output("Array is [" + vertPoints.length + "]["
                    + vertPoints[0].length + "]");
        }

        int coordCount = 0;
        boolean doPolys = true;

        utm1.easting = INTERVAL_100K;
        utm2.easting = 9 * INTERVAL_100K;

        // Horizontal lines
        for (i = startNorthing; i < endNorthing; i++) {
            utm1.northing = (float) i * gridLineInterval;
            utm2.northing = utm1.northing;
            utmp.northing = utm1.northing;

            if (doPolys) {
                for (int j = 0; j < numVertLines; j++) {
                    utmp.easting = (float) (j + 1) * gridLineInterval;
                    llp = utmp.toLatLonPoint(Ellipsoid.WGS_84, llp);

                    vertPoints[j][coordCount] = llp.getY();
                    vertPoints[j][coordCount + 1] = llp.getX();

                    if (UTM_DEBUG_VERBOSE) {
                        Debug.output("for vline " + j + ", point " + i
                                + ", easting: " + utmp.easting + ", northing: "
                                + utmp.northing + ", lat:"
                                + vertPoints[j][coordCount] + ", lon:"
                                + vertPoints[j][coordCount + 1]);
                    }
                }
                coordCount += 2;
            }

            point1 = utm1.toLatLonPoint(Ellipsoid.WGS_84, point1);
            point2 = utm2.toLatLonPoint(Ellipsoid.WGS_84, point2);

            lat2 = point1.getLatitude();

            if (lat2 < 84f) {
                line = new OMLine(point1.getY(), point1.getX(), point2.getY(), point2.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
                line.setLinePaint(distanceGridPaint);
                list.add(line);
            }
        }

        if (doPolys) {
            OMGeometryList polys = new OMGeometryList();
            for (i = 0; i < vertPoints.length; i++) {
                if (UTM_DEBUG_VERBOSE) {
                    for (int k = 0; k < vertPoints[i].length; k += 2) {
                        System.out.println(" for poly " + i + ": lat = "
                                + vertPoints[i][k] + ", lon = "
                                + vertPoints[i][k + 1]);
                    }
                }
                poly = new PolylineGeometry.LL(vertPoints[i], OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE);
                polys.add(poly);
            }
            polys.setLinePaint(distanceGridPaint);
            list.add(polys);
        } else {

            // This doesn't seem to calculate the right
            // lines, although it looks like it should.

            if (UTM_DEBUG) {
                Debug.output("Doing vertical lines");
            }

            utm1.northing = startNorthing;
            utm2.northing = endNorthing;

            // Vertical lines
            for (i = 1; i <= 9; i++) {
                utm1.easting = i * 100000f;
                utm2.easting = i * 100000f;

                point1 = utm1.toLatLonPoint(Ellipsoid.WGS_84, point1);
                point2 = utm2.toLatLonPoint(Ellipsoid.WGS_84, point2);

                line = new OMLine(point1.getY(), point1.getX(), point2.getY(), point2.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
                line.setLinePaint(distanceGridPaint);
                list.add(line);
            }
        }

        return list;
    }

    /**
     * Create a list of rectangles representing equal areas of MGRS coordinates
     * around a lat/lon location. The rectangles are laid out on the MGRS grid,
     * their size determined by the accuracy limitation given, which reflects
     * how many digits are provided in a MGRS coordinate. Uses WGS 84 ellipsoid.
     * 
     * @param llp the lat/lon point of concern.
     * @param accuracy the number of digits for northing and easting values of a
     *        MGRS coordinate, which implicitly translates to meters - 5 (1
     *        meter) to 1 (10,000 meter).
     * @param numRects the number of rectangles in each direction from the llp
     *        to create.
     */
    protected OMGeometryList createMGRSRectangles(LatLonPoint llp,
                                                  int accuracy, int numRects) {
        return createMGRSRectangles(llp, accuracy, numRects, Ellipsoid.WGS_84);
    }

    /**
     * Create a list of rectangles representing equal areas of MGRS coordinates
     * around a lat/lon location. The rectangles are laid out on the MGRS grid,
     * their size determined by the accuracy limitation given, which reflects
     * how many digits are provided in a MGRS coordinate.
     * 
     * @param llp the lat/lon point of concern.
     * @param accuracy the number of digits for northing and easting values of a
     *        MGRS coordinate, which implicitly translates to meters - 5 (1
     *        meter) to 1 (10,000 meter).
     * @param numRects the number of rectangles in each direction from the llp
     *        to create.
     * @param ellipsoid the ellipsoid to use.
     */
    protected OMGeometryList createMGRSRectangles(LatLonPoint llp,
                                                  int accuracy, int numRects,
                                                  Ellipsoid ellipsoid) {
        MGRSPoint mgrs = new MGRSPoint();
        mgrs.setAccuracy(accuracy);
        MGRSPoint.LLtoMGRS(llp, ellipsoid, mgrs);

        double accuracyBonus = 100000 / Math.pow(10, accuracy);

        OMGeometryList list = new OMGeometryList();

        for (double i = -numRects * accuracyBonus; i < numRects * accuracyBonus; i += accuracyBonus) {
            for (double j = -numRects * accuracyBonus; j < numRects
                    * accuracyBonus; j += accuracyBonus) {
                if (Debug.debugging("utmdistancegrid")) {
                    System.out.print(".");
                }
                list.add(createMGRSRectangle(mgrs,
                        i,
                        j,
                        accuracyBonus,
                        ellipsoid));
            }
            if (Debug.debugging("utmdistancegrid")) {
                System.out.println();
            }
        }

        return list;
    }

    /**
     * Create a polygon representing an equi-distant area, at a meters offset
     * with a meters interval.
     * 
     * @param mgrsBasePoint the center point of interest that has been
     *        normalized for the units of the rectangle (meters, km, etc).
     * @param voffset vertical offset in meters, normalized for units, for
     *        entire polygon.
     * @param hoffset horizontal offset in meters, normalized for units, for
     *        entire polygon.
     * @param interval edge length of rectangle polygon in meters, normalized
     *        for units.
     * @param ellipsoid Ellipsoid for coordinate translation.
     */
    protected OMGeometry createMGRSRectangle(MGRSPoint mgrsBasePoint,
                                             double voffset, double hoffset,
                                             double interval, Ellipsoid ellipsoid) {

        double[] llpoints = new double[10];

        double easting = mgrsBasePoint.easting + hoffset;
        double northing = mgrsBasePoint.northing + voffset;
        int zone_number = mgrsBasePoint.zone_number;
        char zone_letter = mgrsBasePoint.zone_letter;

        LatLonPoint llp1 = new LatLonPoint.Double();
        llp1 = MGRSPoint.MGRStoLL(ellipsoid,
                northing,
                easting,
                zone_number,
                zone_letter,
                llp1);
        llpoints[0] = llp1.getY();
        llpoints[1] = llp1.getX();
        llpoints[8] = llp1.getY();
        llpoints[9] = llp1.getX();

        MGRSPoint.MGRStoLL(ellipsoid,
                northing,
                easting + interval,
                zone_number,
                zone_letter,
                llp1);
        llpoints[2] = llp1.getY();
        llpoints[3] = llp1.getX();

        MGRSPoint.MGRStoLL(ellipsoid,
                northing + interval,
                easting + interval,
                zone_number,
                zone_letter,
                llp1);
        llpoints[4] = llp1.getY();
        llpoints[5] = llp1.getX();

        MGRSPoint.MGRStoLL(ellipsoid,
                northing + interval,
                easting,
                zone_number,
                zone_letter,
                llp1);
        llpoints[6] = llp1.getY();
        llpoints[7] = llp1.getX();

        MGRSPoint mgrs = new MGRSPoint(northing, easting, zone_number, zone_letter);
        mgrs.resolve(mgrsBasePoint.getAccuracy());
        // MGRSPoint.MGRStoLL(mgrs, ellipsoid, llp1);
        String mgrsString = mgrs.getMGRS();

        if (Debug.debugging("utmgriddetail"))
            Debug.output(" - assigning " + mgrsString + " to poly with "
                    + mgrs.getAccuracy());

        PolygonGeometry poly = new PolygonGeometry.LL(llpoints, OMGraphic.DECIMAL_DEGREES, (interval <= 1000 ? OMGraphic.LINETYPE_STRAIGHT
                : OMGraphic.LINETYPE_GREATCIRCLE));
        poly.setAppObject(mgrsString);
        return poly;
    }

    /**
     * The getRectangle call is the main call into the PlugIn module. The module
     * is expected to fill the graphics list with objects that are within the
     * screen parameters passed.
     * 
     * @param p projection of the screen, holding scale, center coords, height,
     *        width.
     */
    public OMGraphicList getRectangle(Projection p) {

        OMGraphicList list = getList();

        if (verticalList == null) {
            verticalList = createUTMZoneVerticalLines();
            horizontalList = createUTMZoneHorizontalLines();
            if (labelsAsMGRS) {
                labelTree = createMGRSZoneLabels();
            } else {
                labelTree = createUTMZoneLabels();
            }
        }

        list.clear();

        if (showZones) {
            list.add(verticalList);
            list.add(horizontalList);
        }

        LatLonPoint center = p.getCenter(new LatLonPoint.Double());
        UTMPoint utm = new UTMPoint(center);

        if (show100kGrid) {
            Debug.message("utmgrid", "Creating 100k distance lines...");

            OMGraphicList hunKLines = createEquiDistanceLines(utm, 100000);
            list.add(hunKLines);
        }

        if (distanceGridResolution > 0) {
            Debug.message("utmgrid", "Creating distance lines...");

            double decisionAid = 100000f / Math.pow(10, distanceGridResolution);
            double dglc = 30f * decisionAid; // distance grid label
            // cutoff
            // Debug.output("Basing decision to display labels on " +
            // dglc);

            int numberBasedForScale = (int) (p.getScale() / (2 * decisionAid));
            if (numberBasedForScale > 10) {
                numberBasedForScale = 10;
            }
            // Debug.output(numberBasedForScale + "");

            OMGeometryList geoList = createMGRSRectangles(center,
                    distanceGridResolution,
                    numberBasedForScale);

            if (showLabels && p.getScale() <= dglc) {
                Debug.message("utmgrid",
                        "Creating labels for distance lines ...");

                OMGraphicList textList = new OMGraphicList();
                LatLonPoint llp = new LatLonPoint.Double();
                Point point = new Point();
                Iterator<OMGeometry> it = geoList.iterator();
                while (it.hasNext()) {
                    PolygonGeometry.LL pll = (PolygonGeometry.LL) it.next();
                    String labelString = (String) (pll).getAppObject();
                    if (labelString == null) {
                        continue;
                    }
                    double[] ll = pll.getLatLonArray();
                    llp.setLatLon(ll[0], ll[1], true);

                    p.forward(llp, point);

                    double x = point.getX();
                    double y = point.getY();
                    int buffer = 20;

                    // Lame attempt of testing whether the label is
                    // on-screen
                    if ((x > -buffer || x < p.getWidth() + buffer)
                            && (y > -buffer || y < p.getHeight() + buffer)) {

                        OMText label = new OMText(llp.getY(), llp.getX(), 4, -4, labelString, OMText.JUSTIFY_LEFT);
                        label.setLinePaint(distanceGridPaint);
                        textList.add(label);
                    }
                }
                list.add(textList);
            }

            geoList.setLinePaint(distanceGridPaint);
            list.add(geoList);
        }

        if (labelList != null) {
            labelList.clear();
        } else {
            labelList = new OMGraphicList();
        }

        if (showLabels && p.getScale() <= labelCutoffScale) {
            Debug.message("utmgrid", "Creating labels for map...");
            Point2D ul = p.getUpperLeft();
            Point2D lr = p.getLowerRight();

            Vector labels = labelTree.get((float) ul.getY(),
                    (float) ul.getX(),
                    (float) lr.getY(),
                    (float) lr.getX());

            labelList.setTargets(labels);
            labelList.setLinePaint(getUTMGridPaint());
            list.add(labelList);
        }

        Debug.message("utmgrid", "Generating OMGraphics...");
        list.generate(p);
        Debug.message("utmgrid", "Done.");
        return list;
    } // end getRectangle

    public Component getGUI() {
        JPanel panel = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);

        JCheckBox setZonesButton = new JCheckBox(i18n.get(UTMGridPlugIn.class,
                "setZonesButton",
                "Show UTM Zone Grid"), showZones);
        setZonesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JCheckBox button = (JCheckBox) ae.getSource();
                showZones = button.isSelected();
                doPrepare();
            }
        });
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(setZonesButton, c);
        panel.add(setZonesButton);

        JCheckBox set100kGridButton = new JCheckBox(i18n.get(UTMGridPlugIn.class,
                "set100kGridButton",
                "Show 100Km Distance Grid"), show100kGrid);
        set100kGridButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JCheckBox button = (JCheckBox) ae.getSource();
                show100kGrid = button.isSelected();
                doPrepare();
            }
        });

        c.gridy = 1;
        gridbag.setConstraints(set100kGridButton, c);
        panel.add(set100kGridButton);

        JCheckBox setLabelsButton = new JCheckBox(i18n.get(UTMGridPlugIn.class,
                "setLabelsButton",
                "Show Zone Labels"), showLabels);
        setLabelsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JCheckBox button = (JCheckBox) ae.getSource();
                showLabels = button.isSelected();
                doPrepare();
            }
        });
        c.gridy = 2;
        gridbag.setConstraints(setLabelsButton, c);
        panel.add(setLabelsButton);

        JPanel resPanel = PaletteHelper.createPaletteJPanel(i18n.get(UTMGridPlugIn.class,
                "resPanel",
                "Distance Grid Units"));

        String[] resStrings = {
                i18n.get(UTMGridPlugIn.class, "resStrings.noGrid", " No Grid "),
                i18n.get(UTMGridPlugIn.class,
                        "resStrings.10000m",
                        " 10,000 meter   "),
                i18n.get(UTMGridPlugIn.class,
                        "resStrings.1000m",
                        " 1000 meter "),
                i18n.get(UTMGridPlugIn.class, "resStrings.100m", " 100 meter "),
                i18n.get(UTMGridPlugIn.class, "resStrings.10m", " 10 meter "),
                i18n.get(UTMGridPlugIn.class, "resStrings.1m", " 1 meter ") };

        JComboBox resList = new JComboBox(resStrings);
        resList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcb = (JComboBox) e.getSource();
                setDistanceGridResolution(jcb.getSelectedIndex());
                doPrepare();
            }
        });
        resList.setSelectedIndex(getDistanceGridResolution());

        resPanel.add(resList);

        c.gridy = 3;
        c.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints(resPanel, c);
        panel.add(resPanel);

        JButton utmGridColorButton = new JButton(DrawingAttributes.getIconForPaint(getUTMGridPaint(),
                true));
        utmGridColorButton.setContentAreaFilled(false);
        utmGridColorButton.setBorder(null);
        utmGridColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                Color tmpPaint = getNewPaint((Component) ae.getSource(),
                        i18n.get(UTMGridPlugIn.class,
                                "utmGridColorChooser",
                                "Choose UTM Grid Color"),
                        (Color) getUTMGridPaint());
                if (tmpPaint != null) {
                    setUTMGridPaint(tmpPaint);
                    ((JButton) ae.getSource()).setIcon(DrawingAttributes.getIconForPaint(tmpPaint,
                            true));
                    doPrepare();
                }
            }
        });

        JLabel utmGridLabel = new JLabel(i18n.get(UTMGridPlugIn.class,
                "utmGridColorButton",
                "Set UTM Grid Color"));

        JPanel utmGridPanel = new JPanel();
        utmGridPanel.add(utmGridColorButton);
        utmGridPanel.add(utmGridLabel);

        c.gridy = 4;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(utmGridPanel, c);
        panel.add(utmGridPanel);

        JButton distGridColorButton = new JButton(DrawingAttributes.getIconForPaint(getDistanceGridPaint(),
                true));
        distGridColorButton.setContentAreaFilled(false);
        distGridColorButton.setBorder(null);
        distGridColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                Color tmpPaint = getNewPaint((Component) ae.getSource(),
                        i18n.get(UTMGridPlugIn.class,
                                "distanceGridColorChooser",
                                "Choose Distance Grid Color"),
                        (Color) getDistanceGridPaint());
                if (tmpPaint != null) {
                    setDistanceGridPaint(tmpPaint);
                    ((JButton) ae.getSource()).setIcon(DrawingAttributes.getIconForPaint(tmpPaint,
                            true));
                    doPrepare();
                }
            }
        });
        JLabel distanceGridLabel = new JLabel(i18n.get(UTMGridPlugIn.class,
                "distGridColorButton",
                "Set Distance Grid Color"));
        JPanel distGridPanel = new JPanel();
        distGridPanel.add(distGridColorButton);
        distGridPanel.add(distanceGridLabel);
        c.gridy = 5;
        gridbag.setConstraints(distGridPanel, c);
        panel.add(distGridPanel);

        return panel;
    }

    /**
     * A convenience method to get a color from a JColorChooser. Null will be
     * returned if the JColorChooser lock is in place, or if something else is
     * done where the JColorChooser would normally return null.
     * 
     * @param source the source component for the JColorChooser.
     * @param title the String to label the JColorChooser window.
     * @param startingColor the color to give to the JColorChooser to start
     *        with. Returned if the cancel button is pressed.
     * @return Color chosen from the JColorChooser, null if lock for chooser
     *         can't be acquired.
     */
    protected Color getNewPaint(Component source, String title,
                                Color startingColor) {
        Color newPaint = null;
        if (getLock()) {
            newPaint = OMColorChooser.showDialog(source, title, startingColor);
            releaseLock();
        }
        return newPaint;
    }

    /**
     * A lock to use to limit the number of JColorChoosers that can pop up for a
     * given DrawingAttributes GUI.
     */
    private boolean colorChooserLock = false;

    /**
     * Get the lock to use a JColorChooser. Returns true if you got the lock,
     * false if you didn't.
     */
    protected synchronized boolean getLock() {
        if (colorChooserLock == false) {
            colorChooserLock = true;
            return colorChooserLock;
        } else {
            return false;
        }
    }

    /**
     * Release the lock on the JColorChooser.
     */
    protected synchronized void releaseLock() {
        colorChooserLock = false;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        showLabels = PropUtils.booleanFromProperties(props, prefix
                + ShowLabelsProperty, showLabels);
        showZones = PropUtils.booleanFromProperties(props, prefix
                + ShowZonesProperty, showZones);
        show100kGrid = PropUtils.booleanFromProperties(props, prefix
                + Show100kGridProperty, show100kGrid);
        labelCutoffScale = PropUtils.floatFromProperties(props, prefix
                + LabelCutoffScaleProperty, labelCutoffScale);
        utmGridPaint = PropUtils.parseColorFromProperties(props, prefix
                + UTMGridColorProperty, utmGridPaint);
        distanceGridPaint = PropUtils.parseColorFromProperties(props, prefix
                + DistanceGridColorProperty, distanceGridPaint);
        setDistanceGridResolution(PropUtils.intFromProperties(props, prefix
                + DistanceGridResolutionProperty, distanceGridResolution));
        labelsAsMGRS = PropUtils.booleanFromProperties(props, prefix
                + LabelsAsMGRSProperty, labelsAsMGRS);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + ShowLabelsProperty, Boolean.toString(showLabels));
        props.put(prefix + ShowZonesProperty, new Boolean(showZones).toString());
        props.put(prefix + LabelCutoffScaleProperty,
                Float.toString(labelCutoffScale));
        props.put(prefix + Show100kGridProperty, Boolean.toString(show100kGrid));
        props.put(prefix + UTMGridColorProperty,
                Integer.toHexString(((Color) utmGridPaint).getRGB()));
        props.put(prefix + DistanceGridColorProperty,
                Integer.toHexString(((Color) distanceGridPaint).getRGB()));
        props.put(prefix + DistanceGridResolutionProperty,
                Integer.toString(distanceGridResolution));
        props.put(prefix + LabelsAsMGRSProperty, Boolean.toString(labelsAsMGRS));
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        String interString;

        interString = i18n.get(UTMGridPlugIn.class,
                ShowZonesProperty,
                I18n.TOOLTIP,
                "Show UTM Zone Grid Lines.");
        props.put(ShowZonesProperty, interString);
        interString = i18n.get(UTMGridPlugIn.class,
                ShowZonesProperty,
                ShowZonesProperty);
        props.put(ShowZonesProperty + LabelEditorProperty, interString);
        props.put(ShowZonesProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        interString = i18n.get(UTMGridPlugIn.class,
                UTMGridColorProperty,
                I18n.TOOLTIP,
                "Color for UTM Zone Grid lines.");
        props.put(UTMGridColorProperty, interString);
        interString = i18n.get(UTMGridPlugIn.class,
                UTMGridColorProperty,
                UTMGridColorProperty);
        props.put(UTMGridColorProperty + LabelEditorProperty, interString);
        props.put(UTMGridColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

        interString = i18n.get(UTMGridPlugIn.class,
                ShowLabelsProperty,
                I18n.TOOLTIP,
                "Show Labels for Grid Lines");
        props.put(ShowLabelsProperty, interString);
        interString = i18n.get(UTMGridPlugIn.class,
                ShowLabelsProperty,
                ShowLabelsProperty);
        props.put(ShowLabelsProperty + LabelEditorProperty, interString);
        props.put(ShowLabelsProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        interString = i18n.get(UTMGridPlugIn.class,
                Show100kGridProperty,
                I18n.TOOLTIP,
                "Show 100Km Distance Grid Lines");
        props.put(Show100kGridProperty, interString);
        interString = i18n.get(UTMGridPlugIn.class,
                Show100kGridProperty,
                Show100kGridProperty);
        props.put(Show100kGridProperty + LabelEditorProperty, interString);
        props.put(Show100kGridProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        interString = i18n.get(UTMGridPlugIn.class,
                DistanceGridColorProperty,
                I18n.TOOLTIP,
                "Color for Equal-Distance Grid Lines.");
        props.put(DistanceGridColorProperty, interString);
        interString = i18n.get(UTMGridPlugIn.class,
                DistanceGridColorProperty,
                DistanceGridColorProperty);
        props.put(DistanceGridColorProperty + LabelEditorProperty, interString);
        props.put(DistanceGridColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

        interString = i18n.get(UTMGridPlugIn.class,
                DistanceGridResolutionProperty,
                I18n.TOOLTIP,
                "Meter Resolution for Distance Grid Lines (0-5)");
        props.put(DistanceGridResolutionProperty, interString);
        interString = i18n.get(UTMGridPlugIn.class,
                DistanceGridResolutionProperty,
                DistanceGridResolutionProperty);
        props.put(DistanceGridResolutionProperty + LabelEditorProperty,
                interString);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                UTMGridPlugIn.class,
                LabelsAsMGRSProperty,
                "Labels as MGRS",
                "Flag to display labels in MGRS notation, or UTM.",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        props.put(initPropertiesProperty, ShowZonesProperty + " "
                + UTMGridColorProperty + " " + Show100kGridProperty + " "
                + ShowLabelsProperty + " " + DistanceGridResolutionProperty
                + " " + DistanceGridColorProperty + " " + LabelsAsMGRSProperty);
        return props;
    }

    public void setShowZones(boolean value) {
        showZones = value;
    }

    public boolean isShowZones() {
        return showZones;
    }

    public void setShowLabels(boolean value) {
        showLabels = value;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public void setLabelCutoffScale(float value) {
        labelCutoffScale = value;
    }

    public float getLabelCutoffScale() {
        return labelCutoffScale;
    }

    public void setShow100kGrid(boolean value) {
        show100kGrid = value;
    }

    public boolean isShow100kGrid() {
        return show100kGrid;
    }

    /**
     * Resolution should be MGRS accuracy, 0 for none, 1-5 otherwise, where 1 =
     * 10000 meter grid, 5 is 1 meter grid.
     */
    public void setDistanceGridResolution(int value) {
        distanceGridResolution = value;
        if (distanceGridResolution < 0
                || distanceGridResolution > MGRSPoint.ACCURACY_1_METER) {
            distanceGridResolution = 0;
        }
    }

    public int getDistanceGridResolution() {
        return distanceGridResolution;
    }

    public void setUTMGridPaint(Paint value) {
        utmGridPaint = value;

        if (verticalList != null) {
            verticalList.setLinePaint(getUTMGridPaint());
            horizontalList.setLinePaint(getUTMGridPaint());
        }
    }

    public Paint getUTMGridPaint() {
        return utmGridPaint;
    }

    public void setDistanceGridPaint(Paint value) {
        distanceGridPaint = value;
    }

    public Paint getDistanceGridPaint() {
        return distanceGridPaint;
    }
}