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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/OMGraphicUtil.java,v $
// $RCSfile: OMGraphicUtil.java,v $
// $Revision: 1.7 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.HashSet;
import java.util.Iterator;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Color4b;
import javax.vecmath.Point3d;

import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.grid.GridData;
import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.omGraphics.grid.SimpleColorGenerator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;
import com.sun.j3d.utils.geometry.Triangulator;

/**
 * This class handles translating OMGraphics into a Java 3D Scene.
 * 
 * @author dietrick
 */
public class OMGraphicUtil {

    public final static int DEFAULT_NPOINTS_BUFFER_SIZE = 100;
    public final static Iterator NULL_ITERATOR = new HashSet().iterator();

    /**
     * Method takes an OMGraphic, creates one or more Shape3D objects
     * out of it, and returns an Iterator containing them.
     * 
     * @param graphic the OMGraphic.
     * @return Iterator containing Shape3D objects.
     */
    public static Iterator createShape3D(OMGraphic graphic) {
        return createShape3D(graphic, 0);
    }

    /**
     * Method takes an OMGraphic, creates one or more Shape3D objects
     * out of it, and returns an Iterator containing them.
     * 
     * @param graphic the OMGraphic.
     * @param baselineHeight the baselined height for all the graphics
     *        on the list.
     * @return Iterator containing Shape3D objects.
     */
    public static Iterator createShape3D(OMGraphic graphic,
                                         double baselineHeight) {

        Debug.message("3detail", "OMGraphicUtil.createShape3D()");

        boolean DEBUG_SHAPE = Debug.debugging("3dshape");

        if (graphic == null) {
            return NULL_ITERATOR;
        }

        if (graphic instanceof OMGraphicList) {

            HashSet set = new HashSet();

            for (OMGraphic subgraphic : (OMGraphicList) graphic) {
                Debug.message("3detail",
                        "OMGraphicUtil.createShape3D():  recursivly adding list...");
                Iterator iterator = createShape3D(subgraphic, baselineHeight);
                while (iterator.hasNext()) {
                    set.add(iterator.next());
                }
            }
            return set.iterator();
        } else {

            if (DEBUG_SHAPE) {
                Debug.output("OMGraphicUtil.createShape3D():  adding shape...");
            }

            Shape shape = graphic.getShape();

            if (shape != null) {
                // Handle the shapes, depending on if they should
                // be filled or not...

                // First, determine wether this is a line or
                // polygon thingy, and set the color accordingly.

                // Text should be handled differently - might need
                // to render text, and the background block.

                if (graphic.shouldRenderFill()) {
                    // Do polygons.
                    return createShape3D(shape,
                            baselineHeight,
                            graphic.getFillColor(),
                            true);
                } else if (graphic.shouldRenderEdge()) {
                    // Might as well make sure it's not totally
                    // clear before creating lines.
                    return createShape3D(shape,
                            baselineHeight,
                            graphic.getDisplayColor(),
                            false);
                } else if (DEBUG_SHAPE) {
                    Debug.output("OMGraphicUtil.createShape3D(): can't render graphic");
                }
            } else if (DEBUG_SHAPE) {
                Debug.output("OMGraphicUtil.createShape3D(): shape from graphic is null");
            }
        }
        return NULL_ITERATOR;
    }

    /**
     * Create an Iterator containing a set of Shape3D objects, created
     * from OMGrid. Currently only works for OMGrids containing
     * GridData.Int data.
     * 
     * @param grid the OMGrid to create a 3D terrain object from.
     * @param baselineHeight the baselined height for all the values
     *        in the grid, if the OMGridGenerator wants to use it.
     * @param projection the map projection
     * @return an iterator containing all Shape3D objects
     */
    public static Iterator createShape3D(OMGrid grid, double baselineHeight,
                                         Projection projection) {

        TriangleStripArray gridStrip;

        if (grid.getRenderType() != OMGraphic.RENDERTYPE_LATLON) {
            Debug.error("OMGraphicUtil.createShape3D:  can't handle non-LATLON grids yet");
            return NULL_ITERATOR;
        }

        boolean DEBUG = false;
        if (Debug.debugging("3dgrid")) {
            DEBUG = true;
        }

        //      if (grid.getGenerator() == null && grid.getFillColor() ==
        // OMColor.clear) {
        //          return createWireFrame(grid, baselineHeight, projection);
        //      }

        Color fColor = grid.getFillColor();
        Color lColor = grid.getLineColor();

        boolean polyline = (fColor == OMColor.clear);
        if (DEBUG) {
            Debug.output("Polyline = " + polyline);
        }
        Color3f fillcolor = new Color3f(fColor);
        Color3f linecolor = new Color3f(lColor);

        int numRows = grid.getRows();
        int numCols = grid.getColumns();

        // create triangle strip for twist
        int stripCount = numRows - 1;
        int numberVerticesPerStrip = numCols * 2;
        int[] stripCounts = new int[stripCount];

        for (int i = 0; i < stripCount; i++) {
            stripCounts[i] = numberVerticesPerStrip;
        }

        LatLonPoint anchorLL = new LatLonPoint.Double(grid.getLatitude(), grid.getLongitude());
//        Point anchorP = projection.forward(anchorLL);
        double vRes = grid.getVerticalResolution();
        double hRes = grid.getHorizontalResolution();

        gridStrip = new TriangleStripArray(stripCount * numberVerticesPerStrip, TriangleStripArray.COORDINATES
                | TriangleStripArray.COLOR_3 | TriangleStripArray.NORMALS, stripCounts);

        // OK, what you want to do is calculate the index of the
        // vertices, add the correct multiplication of offsets in
        // degree space, and then inverse project that point to get
        // the actual coordinates.

        Point p = new Point();
        int pointer = 0;

        GridData gridData = grid.getData();

        if (!(gridData instanceof GridData.Int)) {
            // Need to fix this to work with all GridData types!
            Debug.error("OMGrid.interpValueAt only works for integer data.");
        }

        int[][] data = ((GridData.Int) gridData).getData();
        boolean major = gridData.getMajor();

        //         int[][] data = grid.getData();
        //         boolean major = grid.getMajor();

        int dataPoint;
        Color3f color;

        SimpleColorGenerator generator = null;
        OMGridGenerator tempGen = grid.getGenerator();

        if (tempGen instanceof SimpleColorGenerator) {
            generator = (SimpleColorGenerator) tempGen;
        }

        for (int j = 0; j < numRows - 1; j++) {

            if (DEBUG) {
                Debug.output("Creating strip " + j);
            }
            // I think the '-' should be '+'... (changed, DFD)
            double lat1 = anchorLL.getY() + (j * vRes);
            double lat2 = anchorLL.getY() + (( j + 1.0) * vRes);

            for (int k = 0; k < numCols; k++) {

                if (DEBUG) {
                    Debug.output("   working row " + k);
                }

                double lon = anchorLL.getX() + (k * hRes);

                projection.forward(lat1, lon, p);
                if (major) {
                    dataPoint = data[k][j];
                } else {
                    dataPoint = data[j][k];
                }

                gridStrip.setCoordinate(pointer,
                        new Point3d((float) p.getX(), (float) dataPoint, (float) p.getY()));

                if (DEBUG) {
                    Debug.output("       1st coord " + p.getX() + ", "
                            + dataPoint + ", " + p.getY());
                }

                projection.forward(lat2, lon, p);

                if (major) {
                    dataPoint = data[k][j + 1];
                } else {
                    dataPoint = data[j + 1][k];
                }
                gridStrip.setCoordinate(pointer + 1,
                        new Point3d((float) p.getX(), (float) dataPoint, (float) p.getY()));

                if (DEBUG) {
                    Debug.output("       2nd coord " + p.getX() + ", "
                            + dataPoint + ", " + p.getY());
                }

                // Need the TriangleStripArray.COLOR_3 Attribute set
                // above
                if (generator == null) {
                    if (polyline) {
                        color = linecolor;
                    } else {
                        color = fillcolor;
                    }
                } else {
                    color = new Color3f(new Color(generator.calibratePointValue(dataPoint)));
                }

                gridStrip.setColor(pointer++, color);
                gridStrip.setColor(pointer++, color);
                // else
                //              pointer += 2;
            }
        }

        Shape3D shape = new Shape3D(gridStrip);

        Appearance appear = new Appearance();
        PolygonAttributes polyAttrib = new PolygonAttributes();
        if (polyline) {
            polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        }
        polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);

        appear.setPolygonAttributes(polyAttrib);

        shape.setAppearance(appear);

        HashSet set = new HashSet();
        set.add(shape);

        return set.iterator();
    }

    /**
     * Create an Iterator containing a set of Shape3D objects, created
     * from a java.awt.Shape.
     * 
     * @param shape java.awt.Shape object.
     * @param baselineHeight the baselined height for all the values
     *        in the grid, if the OMGridGenerator wants to use it.
     * @param color the color to make the object.
     * @param filled whether or not to fill the object with color.
     * @return Iterator containing Shape3D objects created from shape
     *         object.
     */
    public static Iterator createShape3D(Shape shape, double baselineHeight,
                                         Color color, boolean filled) {

        int bufferSize = DEFAULT_NPOINTS_BUFFER_SIZE;

        double[] data = expandArrayD(bufferSize, null);
        int dataIndex = 0;

        // How many spaces are left in the buffer.
        int refreshCounter = bufferSize;
        int[] stripCount = new int[1];
        stripCount[0] = 0;

        // null is AffineTransform...
        PathIterator pi2 = shape.getPathIterator(null);

        // flatness might need to be calculated, based
        // on scale or something. Depends on how many
        // points there should be for an accurate
        // shape rendition.
        float flatness = .25f;
        FlatteningPathIterator pi = new FlatteningPathIterator(pi2, flatness);

        double[] coords = new double[6];
        double pntx = 0;
        double pnty = 0;
        double pntz = baselineHeight;

        HashSet set = new HashSet();
        Shape3D shape3D = null;

        Debug.message("3detail",
                "OMGraphicUtil.createShape3D(): figuring out coordinates");

        // Creating the data[]

        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);

            switch (type) {
            case PathIterator.SEG_MOVETO:
                if (dataIndex != 0) {
                    shape3D = createShape3D(data,
                            dataIndex,
                            stripCount,
                            color,
                            filled);
                    if (shape3D != null) {
                        set.add(shape3D);
                    }
                    data = expandArrayD(bufferSize, null);
                    dataIndex = 0;
                }
            case PathIterator.SEG_LINETO:

                // SEG_MOVETO is the first point of
                // the shape, SEG_LINETO are the
                // middle and end points. SEG_CLOSE
                // confirms the close, but we don't
                // need it.
                pntx = coords[0];
                pnty = coords[1];

                if (Debug.debugging("3detail")) {
                    Debug.output("Shape coordinates: " + pntx + ", " + pnty);
                }

                // Get Z here, if you want to set the height of the
                // coordinate...
                // pntz =

                // See if there is space in the buffer.
                if (dataIndex >= data.length) {
                    data = expandArrayD(bufferSize, data);
                    refreshCounter = bufferSize;
                }

                data[dataIndex++] = pntx;
                data[dataIndex++] = pntz;
                data[dataIndex++] = pnty;

                //              data[dataIndex++] = pntx;
                //              data[dataIndex++] = pnty;
                //              data[dataIndex++] = pntz;

                stripCount[0]++;

                refreshCounter -= 3;
                break;
            default:
                // Do nothing, because it's a repeat
                // of the last SEG_LINETO point.

                Debug.message("3detail", "Shape coordinates: " + coords[0]
                        + ", " + coords[1] + " rounding out SEG_CLOSE");
            }

            pi.next();
        }

        if (dataIndex != 0) {
            shape3D = createShape3D(data, dataIndex, stripCount, color, filled);
            if (shape3D != null) {
                set.add(shape3D);
            }
        }

        return set.iterator();
    }

    /**
     * Create a Shape3D from raw components. May return null. Assumes
     * a stripCount array of size one.
     * 
     * @param data Description of the Parameter
     * @param realDataIndex Description of the Parameter
     * @param stripCount Description of the Parameter
     * @param color Description of the Parameter
     * @param filled Description of the Parameter
     * @return Description of the Return Value
     */
    public static Shape3D createShape3D(double[] data, int realDataIndex,
                                        int[] stripCount, Color color,
                                        boolean filled) {
        try {
            double[] newData = new double[realDataIndex];
            System.arraycopy(data, 0, newData, 0, realDataIndex);
            if (filled) {
                return createFilled(newData, stripCount, color);
            } else {
                return createEdges(newData, color);
            }
        } catch (java.lang.IllegalArgumentException iae) {
            Debug.error("OMGraphicUtil.createShape3D():  IllegalArgumentException caught: \n"
                    + iae.toString());

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < stripCount.length; i++) {
                sb.append("{" + stripCount[i] + "}");
            }

            Debug.output("Something funny happened on "
                    + (filled ? "filled" : "edge") + " data[" + data.length
                    + "], reflecting " + data.length / 3
                    + " nodes, with stripCount[" + stripCount.length + "] "
                    + sb.toString());
        }
        return null;
    }

    public static Shape3D createFilled(double[] data, int[] stripCount,
                                       Color color)
            throws IllegalArgumentException {

        // j + 1 is the number of shapes.
        // Might have to track the number of coordinates per shape.

        // Use a Triangulator to take geometry data and create
        // polygons out of it.
        Debug.message("3detail", "OMGraphicUtil: adding polygon, data length "
                + data.length + ", reflecting " + data.length / 3
                + " nodes, with a strip count of " + stripCount.length);
        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(data);
        gi.setStripCounts(stripCount);

        Triangulator tr = new Triangulator();
        //        Triangulator tr = new Triangulator(1);
        Debug.message("3detail", "OMGraphicUtil: begin triangulation");
        tr.triangulate(gi);
        Debug.message("3detail", "OMGraphicUtil: end triangulation");
        gi.recomputeIndices();

        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(gi);
        gi.recomputeIndices();

        Stripifier st = new Stripifier();
        st.stripify(gi);
        gi.recomputeIndices();

        Shape3D shape3D = new Shape3D();
        shape3D.setAppearance(createMaterialAppearance(color));
        shape3D.setGeometry(gi.getGeometryArray());

        return shape3D;
    }

    public static Shape3D createEdges(double[] data, Color color)
            throws IllegalArgumentException {

        int numPoints = data.length / 3;

        // Create a line for the polyline.
        Debug.message("3detail", "OMGraphicUtil: adding polyline of "
                + numPoints + " points.");

        LineStripArray la = new LineStripArray(numPoints, LineArray.COORDINATES
                | LineArray.COLOR_4, new int[] { numPoints });

        la.setCoordinates(0, data);
        Color4b[] colors = createColorArray(numPoints, color);
        la.setColors(0, colors);
        return new Shape3D(la);
    }

    public static Appearance createMaterialAppearance(Color color) {

        Appearance materialAppear = new Appearance();

        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
        materialAppear.setPolygonAttributes(polyAttrib);

        Material material = new Material();
        // Might want to look into using a Color4b at some point
        material.setAmbientColor(new Color3f(color));
        materialAppear.setMaterial(material);

        return materialAppear;
    }

    public static Appearance createWireFrameAppearance(Color color) {

        Appearance materialAppear = new Appearance();
        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        materialAppear.setPolygonAttributes(polyAttrib);
        ColoringAttributes redColoring = new ColoringAttributes();
        redColoring.setColor(1.0f, 0.0f, 0.0f);
        materialAppear.setColoringAttributes(redColoring);

        return materialAppear;
    }

    /**
     * Create an array of Color4b objects for an OMGraphic
     * representation. The colors in an OMGraphic are ARGB, which is
     * why this creates a Color4b object. Since all the parts of an
     * OMGraphic are colored the same, create the array of colors to
     * be all the same color retrieved from the OMGraphic.
     * 
     * @param size Description of the Parameter
     * @param color Description of the Parameter
     * @return Description of the Return Value
     */
    public static Color4b[] createColorArray(int size, Color color) {
        Color4b[] colors = new Color4b[size];
        for (int i = 0; i < size; i++) {
            colors[i] = new Color4b(color);
        }
        return colors;
    }

    /**
     * Create an array to hold double data for 3d polygons and lines.
     * 
     * @param bufferSize the number of
     * 
     * <pre>
     * points
     * </pre>
     * 
     * to buffer. Equals three doubles per point.
     * @param currentArray if not null, will create an array the size
     *        of the current array plus the size needed to hold the
     *        desired number of points.
     * @return a double[].
     */
    public static double[] expandArrayD(int bufferSize, double[] currentArray) {

        if (currentArray == null) {
            return new double[bufferSize * 3];
        }
        int length = currentArray.length;
        double[] ret = new double[length + bufferSize * 3];
        System.arraycopy(currentArray, 0, ret, 0, length);

        return ret;
    }

    /**
     * Create an array to hold float data for 3d polygons and lines.
     * 
     * @param bufferSize the number of
     * 
     * <pre>
     * points
     * </pre>
     * 
     * to buffer. Equals three floats per point.
     * @param currentArray if not null, will create an array the size
     *        of the current array plus the size needed to hold the
     *        desired number of points.
     * @return a float[].
     */
    public static float[] expandArrayF(int bufferSize, float[] currentArray) {

        if (currentArray == null) {
            return new float[bufferSize * 3];
        }
        int length = currentArray.length;
        float[] ret = new float[length + bufferSize * 3];
        System.arraycopy(currentArray, 0, ret, 0, length);

        return ret;
    }

}