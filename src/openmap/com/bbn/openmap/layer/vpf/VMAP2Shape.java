
// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VMAP2Shape.java,v $
// $RCSfile: VMAP2Shape.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import java.io.*;
import java.util.Properties;

import com.bbn.openmap.*;
import com.bbn.openmap.layer.vpf.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.DrawUtil;
import com.bbn.openmap.layer.shape.*;
import com.bbn.openmap.util.PropUtils;

/**
 * Convert NIMA VMAP geospatial data into ESRI shapefile format.
 */
public class VMAP2Shape {

    protected static String vmaptype = "bnd";
    protected static String propsFileName =
            System.getProperty("user.home") +
            System.getProperty("file.separator") +
            "openmap.properties";
    protected static String prefix = "vmapref";
    protected static boolean doThinning = false;
    protected static float fan_eps = 0.1f;
    protected static float zero_eps = 0.0001f;
    protected static float threshold = 0.5f;

    protected LibrarySelectionTable lst;
    protected transient LayerGraphicWarehouseSupport warehouse;

    public VMAP2Shape () {
    }

    public void writeShapeFile(String shapeFileName, OMGraphicList graphics) {
        OMGraphicList saveGraphics = new OMGraphicList();
        try {
            ShapeFile s = new ShapeFile(shapeFileName);
            int nGraphics = graphics.size();
            int nDumped = 0;
            if (nGraphics > 0) {
                OMGraphic omg = graphics.getOMGraphicAt(0);
                if ((omg instanceof OMPoly) &&
                    (omg.getRenderType() == OMGraphic.RENDERTYPE_LATLON))
                {
                    int shapeType = ((OMPoly)omg).isPolygon()
                        ? ShapeUtils.SHAPE_TYPE_POLYGON : ShapeUtils.SHAPE_TYPE_ARC;
                    System.out.println("shapeType="+shapeType);
                    s.setShapeType(shapeType);
                }
            }

            System.out.println(nGraphics + " candidates.");

            for (int i = 0; i < nGraphics; i++) {
                OMGraphic omg = graphics.getOMGraphicAt(i);
                if ((omg instanceof OMPoly) &&
                    (omg.getRenderType() == OMGraphic.RENDERTYPE_LATLON)) {
                    OMPoly poly = (OMPoly) omg;

                    if (doThinning && maybeThrowAwayPoly(poly)) {
                        continue;
                    }

                    saveGraphics.addOMGraphic(poly);
                } else {
                    System.out.println("Skipping candidate: " +
                                       omg.getClass().toString() + ", " +
                                       omg.getRenderType());
                }
            }
            graphics = saveGraphics;

            // join polylines
            if (false) {
                nGraphics = graphics.size();
                System.out.println(nGraphics + " candidates.");
                graphics = joinCommonLines(graphics);
            }

            // save graphics
            nGraphics = graphics.size();
            System.out.println("Dumping " + nGraphics + " graphics.");
            for (int i = 0; i < nGraphics; i++) {
                OMPoly poly = (OMPoly)graphics.getOMGraphicAt(i);
                float[] radians = poly.getLatLonArray();
                ESRIPolygonRecord epr = new ESRIPolygonRecord();
                epr.add(radians);
                epr.setPolygon(poly.isPolygon());//set POLYGON vs ARC
                s.add(epr);
                ++nDumped;
            }

            s.verify(true, true);
            s.verify(true, true);
            s.close();
            System.out.println("Wrote "+nDumped+" Graphics.");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // iterates through graphic list finding non-connected polylines.
    // iterates over these to find lines with common endpoints and
    // joining them.
    protected static OMGraphicList joinCommonLines (OMGraphicList list) {
        int size=list.size();
        int len1, len2;
        float lat1, lon1, lat2, lon2;
        OMGraphic obj;
        OMGraphicList newGraphics = new OMGraphicList();
        OMGraphicList plineGraphics = new OMGraphicList();

        // check for non-connected polylines
        System.out.println("finding polylines...");
        for (int i=0; i<size; i++) {
            obj = list.getOMGraphicAt(i);
            if ((obj instanceof OMPoly) && !((OMPoly)obj).isPolygon()) {
                plineGraphics.addOMGraphic(obj);
            } else {
                newGraphics.addOMGraphic(obj);
            }
        }

        // iterate through the polylines and join lines with common
        // endpoints
        size = plineGraphics.size();
        OMPoly poly1, poly2;
        float[] rads1, rads2, radians;
        System.out.println("maybe joining "+size+" polylines...");
        // nasty!: > O(n^2)
        for (int i=0; i<size; i++) {
            if (i%500==0) {
                System.out.println("checking pline i="+i);
            }
            for (int j=0; j<size; j++) {
                if (i==j) {
                    continue;
                }
                obj = plineGraphics.getOMGraphicAt(i);
                if (obj instanceof SinkGraphic) {
                    continue;
                }
                poly1 = (OMPoly)obj;
                rads1 = poly1.getLatLonArray();
                len1 = rads1.length;
                lat1 = ProjMath.radToDeg(rads1[len1-2]);
                lon1 = ProjMath.radToDeg(rads1[len1-1]);

                obj = plineGraphics.getOMGraphicAt(j);
                if (obj instanceof SinkGraphic) {
                    continue;
                }
                poly2 = (OMPoly)obj;
                rads2 = poly2.getLatLonArray();
                len2 = rads2.length;
                lat2 = ProjMath.radToDeg(rads2[0]);
                lon2 = ProjMath.radToDeg(rads2[1]);

                if (MoreMath.approximately_equal(lat1, lat2, zero_eps) &&
                        MoreMath.approximately_equal(lon1, lon2, zero_eps))
                {
//                  System.out.println("joining...");
                    radians = new float[len1+len2-2];
                    System.arraycopy(rads1, 0, radians, 0, len1);
                    System.arraycopy(rads2, 0, radians, len1-2, len2);
                    poly1.setLocation(radians, OMGraphic.RADIANS);
                    plineGraphics.setOMGraphicAt(SinkGraphic.getSharedInstance(), j);
                    j=-1;//redo search
                }
            }
        }

        // add the joined lines back to the data set
        size = plineGraphics.size();
        for (int i=0; i<size; i++) {
            obj = plineGraphics.getOMGraphicAt(i);
            if (obj instanceof OMPoly) {
                newGraphics.addOMGraphic(obj);
            }
        }
        return newGraphics;
    }

    // traverse array and coalesce adjacent points which are the same
    public static float[] coalesce_points (float[] radians, float eps, boolean ispolyg) {
        int write=2;
        int len = radians.length;
        for (int i=write-2, j=write; j<len; j+=2) {
            float lat1 = ProjMath.radToDeg(radians[i]);
            float lon1 = ProjMath.radToDeg(radians[i+1]);
            float lat2 = ProjMath.radToDeg(radians[j]);
            float lon2 = ProjMath.radToDeg(radians[j+1]);
            if (MoreMath.approximately_equal(
                        lat1,
                        lat2, eps) &&
                    MoreMath.approximately_equal(
                        lon1,
                        lon2, eps))
            {
                continue;
            }
            i=write;
            radians[write++] = radians[j];
            radians[write++] = radians[j+1];
        }
        // check for mid-phase line
        if (ispolyg && (write == 6) &&
                MoreMath.approximately_equal(radians[0], radians[4], eps) &&
                MoreMath.approximately_equal(radians[1], radians[5], eps))
        {
            write-=2;//eliminate wrapped vertex
        }
        float[] newrads = new float[write];
        System.arraycopy(radians, 0, newrads, 0, write);
        return newrads;
    }

    // return true if we should throw away the poly
    protected boolean maybeThrowAwayPoly (OMPoly poly) {
        float[] radians = poly.getLatLonArray();
        float lat, lon, thresh = ProjMath.degToRad(threshold);
        radians = coalesce_points(radians, 0.0001f, poly.isPolygon());
        poly.setLocation(radians, OMGraphic.RADIANS);//install new
        if (radians.length < 4) {
            return true;//throw away
        }
        if (poly.isPolygon() && (radians.length < 6)) {
            return true;
        }
        int len = radians.length;
        float d;
        for (int i=0; i<len; i+=2) {
            // test for proximity to 1-degree marks.  this hopefully
            // avoids the problem of throwing away tiled slivers.
            // (don't throw away poly)
            lat = ProjMath.radToDeg(radians[i]);
            lon = ProjMath.radToDeg(radians[i+1]);
            if (MoreMath.approximately_equal(lat, (float)(Math.round(lat)),
                                             zero_eps)) {
                return false;
            }
            if (MoreMath.approximately_equal(lon, (float)(Math.round(lon)),
                                             zero_eps)) {
                return false;
            }

            // check to see if all points fit within a certain
            // threshold.  this should eliminate small islands and
            // countries like Luxembourg.  sorry.
            for (int j=i+2; j<radians.length; j+=2) {
                d = DrawUtil.distance(
                        radians[i], radians[i+1],
                        radians[j], radians[j+1]);
                // outside threshold, don't throw away
                if (!MoreMath.approximately_equal(d, 0f, thresh)) {
                    return false;
                }
            }
        }

        if (poly.isPolygon()) {
            return true;//throw away
        }

        // throw away polyline if it's connected (island)
        return (MoreMath.approximately_equal(
                    ProjMath.radToDeg(radians[0]),
                    ProjMath.radToDeg(radians[radians.length-2]),
                    zero_eps) &&
                MoreMath.approximately_equal(
                    ProjMath.radToDeg(radians[1]),
                    ProjMath.radToDeg(radians[radians.length-1]),
                    zero_eps));
    }

    protected Properties loadProperties () {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propsFileName));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

    protected void setProperties (String prefix, Properties props) {

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        String[] paths = LayerUtils.initPathsFromProperties(
                props, realPrefix+VPFLayer.pathProperty);

        String defaultProperty = props.getProperty(
            realPrefix + VPFLayer.defaultLayerProperty);

        if (defaultProperty != null) {
            System.out.println("defaultProperty="+defaultProperty);
            realPrefix = defaultProperty + ".";
            props = VPFLayer.getDefaultProperties();
        }

        String coverage = props.getProperty(realPrefix + VPFLayer.coverageTypeProperty);
        if (coverage != null) {
            vmaptype = coverage;
            System.out.println("vmaptype="+vmaptype);
        }
        initLST(paths);
        if (lst.getDatabaseName().equals("DCW")) {
            System.out.println("creating VPFLayerDCWWarehouse");
            warehouse = new VPFLayerDCWWarehouse();
        } else {
            System.out.println("creating VPFLayerGraphicWarehouse");
            warehouse = new VPFLayerGraphicWarehouse();
        }

        warehouse.setDoThinning(doThinning);
        warehouse.setFanEpsilon(fan_eps);
        warehouse.setProperties(realPrefix, props);
    }

    protected void initLST (String[] paths) {
        try {
            if (lst == null) {
                lst = new LibrarySelectionTable(paths);
            }
        } catch (com.bbn.openmap.io.FormatException f) {
            throw new java.lang.IllegalArgumentException(f.getMessage());
        }
    }

    public OMGraphicList getRectangle() {
        int scale =  30000000;
        int width = 640;
        int height = 480;
        LatLonPoint upperLeft = new LatLonPoint(90.0f, -180.0f);
        LatLonPoint lowerRight = new LatLonPoint(-90.0f, 180.0f);

        warehouse.clear();

        System.out.println("VMAP2Shape.getRectangle(): " +
                           "calling drawTile with boundaries: " +
                           upperLeft + lowerRight);
        long start = System.currentTimeMillis();
        lst.drawTile(scale, width, height,
                     vmaptype,
                     warehouse,
                     upperLeft,
                     lowerRight);
        long stop = System.currentTimeMillis();
        System.out.println("VMAP2Shape.getRectangle(): read time: " +
                ((stop-start)/1000d) + " seconds");

        return warehouse.getGraphics();
    }

    public static void usage () {
        System.out.println("Usage: java VMAP2Shape [args] <outfile.shp>");
        System.out.println("Arguments:");
        System.out.println("\t-props <path>             path to properties file");
        System.out.println("                            default: "+propsFileName);
        System.out.println("\t-prefix <identifier>      vmap properties prefix");
        System.out.println("                            default: "+prefix);
        System.out.println("\t-thin <eps> <thresh>      do thinning");
        System.out.println("                            default eps="+fan_eps+" thresh="+threshold);
        System.exit(1);
    }

    public static void main (String args[]) {
        if ((args.length == 0) ||
            ((args.length == 1) && (args[0].startsWith("-")))) {
            usage();
        }

        com.bbn.openmap.util.Debug.init(System.getProperties());

        VMAP2Shape c = new VMAP2Shape();

        for (int i=0; i<args.length-1; i++) {
            if (args[i].equalsIgnoreCase("-props")) {
                propsFileName = args[++i];
            } else if (args[i].equalsIgnoreCase("-prefix")) {
                prefix = args[++i];
            } else if (args[i].equalsIgnoreCase("-thin")) {
                doThinning = true;
                fan_eps = Float.valueOf(args[++i]).floatValue();
                threshold = Float.valueOf(args[++i]).floatValue();
            } else {
                usage();
            }
        }

        c.setProperties(prefix, c.loadProperties());
        c.writeShapeFile(args[args.length-1], c.getRectangle());
    }
}
