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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/shape/ShapeSpecialist.java,v $
// $RCSfile: ShapeSpecialist.java,v $
// $Revision: 1.5 $
// $Date: 2007/06/21 21:39:44 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.specialist.shape;

import java.awt.Color;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.corba.CSpecialist.MouseEvent;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.WidgetChange;
import com.bbn.openmap.layer.shape.ESRIRecord;
import com.bbn.openmap.layer.shape.SpatialIndex;
import com.bbn.openmap.layer.specialist.SColor;
import com.bbn.openmap.layer.specialist.SGraphic;
import com.bbn.openmap.layer.specialist.Specialist;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Implements the Specialist interface so that we can serve graphics to OpenMap
 * via CORBA. This specialist handles shape files, with the coordinates in the
 * shape files being in decimal degrees, not pre-projected x-y coordinates. The
 * specialist looks for a property file to know where where the shape file and
 * spatial index file is, and how to color the shapes.
 */
public class ShapeSpecialist
    extends Specialist {
    /** The name of the property that holds the name of the shape file. */
    public final static String shapeFileProperty = "shapeFile";
    /**
     * The name of the property that holds the name of the spatial index file.
     */
    public final static String spatialIndexProperty = "spatialIndex";
    /**
     * The name of the property that holds the line color of the graphics.
     */
    public final static String lineColorProperty = "lineColor";
    /**
     * The name of the property that holds the fill color of the graphics.
     */
    public final static String fillColorProperty = "fillColor";
    /** The spatial index to use to pick the graphics to return. */
    protected SpecialistSpatialIndex spatialIndex;
    /** The read-in properties. */
    protected Properties properties = null;
    /** The color to outline the shapes. */
    protected SColor lineColor = null;
    /** The color to fill the shapes. */
    protected SColor fillColor = null;

    // final private static SColor nullColor = new SColor((short) 0, (short) 0,
    // (short) 0);
    // final private static EStipple nullStipple = new EStipple(null, (short) 0,
    // (short) 0, new byte[0]);
    // final private static EComp nullComp = new EComp(null, "");
    // final private static XYPoint nullP1 = new XYPoint((short) 0, (short) 0);
    // final private static XYPoint[] nullPA = new XYPoint[0];
    // final private static LLPoint nullLL1 = new LLPoint(0.0f, 0.0f);
    /**
     * default constructor is called when we're loading the class directly into
     * OpenMap. Not used.
     */
    public ShapeSpecialist() {
        super("ShapeSpecialist", (short) 2, false);
    }

    /**
     * The real constructor to use.
     *
     * @param shapeFile the shapefile.
     */
    public ShapeSpecialist(String shapeFile) {
        super("ShapeSpecialist", (short) 2, false);
        init(shapeFile);
    }

    /**
     * Loads the spatial index from the shape files.
     *
     * @param shapeFile the shapefile.
     */
    public void init(String shapeFile) {
        spatialIndex = locateAndSetShapeData(shapeFile);
    }

    /**
     * Gets the layer graphics.
     *
     * @param ll1 the upper left LLPoint.
     * @param ll2 the lower right LLPoint.
     * @return Vector of ESRISpecialistRecords.
     */
    protected Vector computeGraphics(com.bbn.openmap.corba.CSpecialist.LLPoint ll1,
        com.bbn.openmap.corba.CSpecialist.LLPoint ll2) {

        if (spatialIndex == null) {
            return new Vector();
        }

        Vector list = null;

        // check for dateline anomaly on the screen. we check for
        // ll1.lon >=
        // ll2.lon, but we need to be careful of the check for
        // equality because
        // of floating point arguments...
        // Since we don't have the projection scale here, we're going to assume
        // that we're not zoomed in.
        if (ProjMath.isCrossingDateline(ll1.lon, ll2.lon, 1000000f)) {

            if (Debug.debugging("shape")) {
                Debug.output("Dateline is on screen");
            }

            double ymin = (double) Math.min(ll1.lat, ll2.lat);
            double ymax = (double) Math.max(ll1.lat, ll2.lat);

            try {
                ESRIRecord records1[] = spatialIndex.locateRecords(ll1.lon,
                    ymin,
                    180.0d,
                    ymax);
                ESRIRecord records2[] = spatialIndex.locateRecords(-180.0d,
                    ymin,
                    ll2.lon,
                    ymax);
                int nRecords1 = records1.length;
                int nRecords2 = records2.length;
                list = new Vector(nRecords1 + nRecords2);
                for (int i = 0; i < nRecords1; i++) {
                    ((ESRISpecialistRecord) records1[i]).writeGraphics(list,
                        lineColor,
                        fillColor);
                }
                for (int i = 0; i < nRecords2; i++) {
                    ((ESRISpecialistRecord) records2[i]).writeGraphics(list,
                        lineColor,
                        fillColor);
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            } catch (com.bbn.openmap.io.FormatException fe) {
                fe.printStackTrace();
            }

        } else {

            double xmin = (double) Math.min(ll1.lon, ll2.lon);
            double xmax = (double) Math.max(ll1.lon, ll2.lon);
            double ymin = (double) Math.min(ll1.lat, ll2.lat);
            double ymax = (double) Math.max(ll1.lat, ll2.lat);

            try {
                ESRIRecord records[] = spatialIndex.locateRecords(xmin,
                    ymin,
                    xmax,
                    ymax);
                int nRecords = records.length;
                list = new Vector(nRecords);
                for (int i = 0; i < nRecords; i++) {
                    ((ESRISpecialistRecord) records[i]).writeGraphics(list,
                        lineColor,
                        fillColor);
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            } catch (com.bbn.openmap.io.FormatException fe) {
                fe.printStackTrace();
            }
        }

        return list;
    }

    /**
     * The CSpecialist function.
     */
    public UGraphic[] fillRectangle(
        com.bbn.openmap.corba.CSpecialist.CProjection p,
        com.bbn.openmap.corba.CSpecialist.LLPoint ll1,
        com.bbn.openmap.corba.CSpecialist.LLPoint ll2,
        java.lang.String staticArgs,
        org.omg.CORBA.StringHolder dynamicArgs,
        com.bbn.openmap.corba.CSpecialist.GraphicChange notifyOnChange,
        String uniqueID) {
        // System.out.println("ShapeSpecialist.fillRectangle()");
        try {
            Vector list = computeGraphics(ll1, ll2);
            int len = list.size();

            UGraphic[] ugraphics = new UGraphic[len];
            for (int i = 0; i < len; i++) {
                SGraphic sg = (SGraphic) list.elementAt(i);
                ugraphics[i] = sg.ufill();
            }

            // System.out.println("ShapeSpecialist.fillRectangle():
            // got "+ugraphics.length+" graphics");
            return ugraphics;
        } catch (Throwable t) {
            System.err.println("ShapeSpecialist.fillRectangle(): " + t);
            t.printStackTrace();

            // Don't throw another one! Try to recover!
            // throw new RuntimeException();

            return new UGraphic[0];
        }
    }

    protected SpecialistSpatialIndex locateAndSetShapeData(String shapeFileName) {

        SpatialIndex si = null;

        try {
            si = new SpecialistSpatialIndex(shapeFileName);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return (SpecialistSpatialIndex) si;
    }

    public void signOff(String uniqueID) {
        System.out.println("ShapeSpecialist.signOff()");
    }

    public void receiveGesture(MouseEvent gesture, String uniqueID) {
    }

    public void makePalette(WidgetChange notifyOnChange, String staticArgs,
        org.omg.CORBA.StringHolder dynamicArgs,
        String uniqueID) {
    }

    public void printHelp() {
        System.err.println("usage: java [java/vbj args] <specialist class> [specialist args]");
        System.err.println("");
        System.err.println("       Java Args:");
        System.err.println("       -mx<NUM>m               Set max Java heap in Megs");
        System.err.println("");
        System.err.println("       VBJ Args:");
        System.err.println("       -DORBmbufSize=8388608   Define the VBJ buffer size");
        System.err.println("       -DORBdebug              Enable VBJ debugging");
        System.err.println("");
        System.err.println("       Specialist Args:");
        System.err.println("       -ior <iorfile>                  IOR file");
        System.err.println("       -properties \"<file> ...\"      Path to properties file");
    }

    public void parseArgs(String[] args) {
        Color lcolor = null;
        Color fcolor = null;

        for (int i = 0; i < args.length; i++) {

            if (args[i].equalsIgnoreCase("-properties") &&
                 (args.length > (i + 1))) {
                properties = loadProps(args[i + 1]);

                lcolor = PropUtils.parseColorFromProperties(properties,
                    lineColorProperty,
                    "FF000000");
                lineColor = new SColor((short) ((lcolor.getRed()) * 65535 / 255), (short) ((lcolor.getGreen()) * 65535 /
                    255), (short) ((lcolor.getBlue()) * 65535 / 255));
                if (properties.getProperty(fillColorProperty) != null) {

                    fcolor = PropUtils.parseColorFromProperties(properties,
                        fillColorProperty,
                        "FF000000");

                    fillColor = new SColor((short) ((fcolor.getRed()) * 65535 / 255), (short) ((fcolor.getGreen()) *
                        65535 / 255), (short) ((fcolor.getBlue()) * 65535 / 255));
                }

                String shp = properties.getProperty(shapeFileProperty);

                // System.out.println("Getting " + shp + " and " +
                // ssx);

                init(shp);
            }
        }

        if (properties == null) {
            System.out.println("Need properties file!");
            System.out.println("");
            System.out.println("#######################################");
            System.out.println("shapeFile=<path to shape file (.shp)>");
            System.out.println("spatialIndex=<path to spatial index file (.ssx)>");
            System.out.println("lineColor=<hex ARGB color> i.e. FF000000 for black");
            System.out.println("fillColor=<hex ARGB color> i.e. FF000000 for black>");
            System.out.println("#######################################");
            System.out.println("");
            printHelp();
            System.exit(0);
        }

        super.parseArgs(args);
        System.out.println("Using colors -> lcolor = " + lcolor + ", fcolor = " +
             fcolor);
    }

    /**
     * Load the named file from the named directory into the given
     * <code>Properties</code> instance. If the file is not found a warning is
     * issued. If an IOExceptio occurs, a fatal error is printed and the
     * application will exit.
     *
     * @param file the name of the file
     * @return the loaded properties
     */
    public Properties loadProps(String file) {
        java.io.File propsFile = new java.io.File(file);
        Properties props = new Properties();
        try {
            java.io.InputStream propsStream = new java.io.FileInputStream(propsFile);
            props.load(propsStream);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("ShapeSpecialist did not find properties file: \"" +
                 file + "\"");
            System.exit(1);
        } catch (java.io.IOException e) {
            System.err.println("Caught IO Exception reading configuration file \"" +
                 propsFile + "\"");
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

    public static void main(String[] args) {
        Debug.init(System.getProperties());

        // Create the specialist server
        ShapeSpecialist srv = new ShapeSpecialist();
        srv.parseArgs(args);
        srv.start(args);
    }
}