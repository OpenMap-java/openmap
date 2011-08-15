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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMGrid.java,v $
// $RCSfile: OMGrid.java,v $
// $Revision: 1.11 $
// $Date: 2006/02/16 16:22:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.Point;

import com.bbn.openmap.omGraphics.grid.GridData;
import com.bbn.openmap.omGraphics.grid.OMGridData;
import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.omGraphics.grid.OMGridObjects;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * An OMGrid object is a two-dimensional container object for data. The grid can
 * be laid out in geographic or pixel space. There are two different ways that
 * the OMGrid can be used.
 * <P>
 * The data placed in the array can represent the attributes of what you want
 * the grid to represent - elevation values, temperatures, etc. In order to
 * render the data on the screen, you'll need to set the OMGridGenerator object,
 * and let it interpret the data to create the desired OMGraphics for you.
 * <P>
 * The OMGrid data values can also contain integer ID keys for objects contained
 * in the OMGridObjects object held by the OMGrid. By using the OMGrid in this
 * way, the OMGrid becomes a placeholder for other graphics, and will manage the
 * generate() function calls to those objects that are on the screen.
 * <P>
 * The OMGridGenerator object will take precedence over the OMGridObjects - If
 * the OMGridGenerator is set within the grid, the OMGridGenerator will create
 * the OMGraphics to be displayed for the grid, as opposed the OMGridObjects
 * getting a chance to generate themselves. The OMGrid extends OMGraphicList,
 * and the OMGraphics that the OMGridGenerator creates are added to the OMGrid.
 * If you want the OMGrid to hide the OMGraphics that are created, make it
 * vague.
 */
public class OMGrid
        extends OMGraphicList {

    /**
     * The orientation angle of the grid, in radians. Up/North is zero.
     */
    protected double orientation;
    /**
     * Number of rows in the data array. Gets set by the OMGrid depending on the
     * major of the grid.
     */
    protected int rows;
    /**
     * Number of columns in the data array. Gets set by the OMGrid depending on
     * the major of the grid.
     */
    protected int columns;
    /**
     * The starting latitude point of the grid. Only relevant when the data
     * points are laid out in a lat/lon grid, or when an x/y grid is anchored to
     * a lat/lon location. DOES NOT follow the OpenMap convention where area
     * object locations are defined by the upper left location - the location of
     * the grid is noted by the lower left corner, because grid data is usually
     * defined by the lower left location. Makes it easier to deal with overlap
     * rows and columns, and to calculate the locations of the rows and columns.
     */
    protected double latitude;
    /**
     * The starting longitude point of the grid. Only relevant when the data
     * points are laid out in a lat/lon grid, or when an x/y grid is anchored to
     * a lat/lon location. DOES NOT follow the OpenMap convention where area
     * object locations are defined by the upper left location - the location of
     * the grid is noted by the lower left corner, because grid data is usually
     * defined by the lower left location. Makes it easier to deal with overlap
     * rows and columns, and to calculate the locations of the rows and columns.
     */
    protected double longitude;
    /**
     * The vertical/latitude interval, the distance between row data points in
     * the vertical direction. For x/y grids, this can server as a pixel
     * multiplier. For lat/lon grids, it represents the decimal degrees between
     * grid points.
     */
    protected double verticalResolution;
    /**
     * The horizontal/longitude interval, the distance between column data
     * points in the horizontal direction. For x/y grids, this can server as a
     * pixel multiplier. For lat/lon grids, it represents the decimal degrees
     * between grid points.
     */
    protected double horizontalResolution;
    /**
     * The Object holding the data for the OMGrid. The GridData abstracts the
     * type of data that is held by the OMGrid. Note: the 0 index of the array
     * in both directions is in the lower left corner of the matrix. As you
     * increase indexes in both dimensions, you go up-right.
     */
    public GridData data;
    /**
     * If needed, the data array can hold numerical identifiers, which are keys
     * to objects stored in this hashtable. That way, the grid can be used to
     * hold an array of objects. If the objs are set, then the OMGrid object
     * automatically assumes that all graphic operations are supposed to involve
     * the objs.
     */
    protected OMGridObjects gridObjects = null;
    /**
     * Horizontal screen location of the upper left corner of the grid in
     * pixels, before projection, of XY and OFFSET grids.
     */
    protected Point point = null;
    /**
     * Horizontal screen location of the upper left corner of the grid in
     * pixels, after projection.
     */
    public Point point1 = null;
    /**
     * Horizontal screen location of the lower right corner of the grid in
     * pixels, after projection.
     */
    public Point point2 = null;
    /**
     * Pixel height of grid, set after generate. For non-equidistant
     * projections, this will be a bounding box height.
     */
    public int height = 0;
    /**
     * Pixel width of grid, set after generate. For non-equidistant projections,
     * this will be a bounding box width.
     */
    public int width = 0;

    /**
     * Value of a bad/invalid point in the grid. Has roots in the DTED way of
     * doing things.
     */
    public final static int GRID_NULL = -32767;

    /** An object that knows how to generate graphics for the matrix. */
    protected OMGridGenerator generator = null;

    /**
     * Means that the first dimension of the array refers to the column count.
     */
    public static final boolean COLUMN_MAJOR = true;
    /**
     * Means that the first dimension of the array refers to the row count.
     */
    public static final boolean ROW_MAJOR = false;
    /**
     * Keep track of which dimension different parts of the double array
     * represent. COLUMN_MAJOR is the default, meaning that the first dimension
     * of the array represents the vertical location in the array, and the
     * second is the horizontal location in the array.
     */
    protected boolean major = COLUMN_MAJOR;

    /**
     * The units, if needed, of the values contained in the grid data array.
     * Null value is default and acceptable.
     */
    protected Length units = null;

    /** Default constructor. */
    public OMGrid() {
    }

    /**
     * Create a OMGrid that covers a lat/lon area. Column major by default. If
     * your data is row major, use null for the data, set the major direction,
     * and then set the data.
     * 
     * @param lat latitude of lower left corner of the grid, in decimal degrees.
     * @param lon longitude of lower left corner of the grid, in decimal
     *        degrees.
     * @param vResolution the vertical resolution of the data, as decimal
     *        degrees per row.
     * @param hResolution the horizontal resolution of the data, as decimal
     *        degrees per column.
     * @param data a double array of integers, representing the rows and columns
     *        of data.
     */
    public OMGrid(double lat, double lon, double vResolution, double hResolution, int[][] data) {
        setRenderType(RENDERTYPE_LATLON);
        set(lat, lon, 0, 0, vResolution, hResolution, data);
    }

    /**
     * Create a OMGrid that covers a x/y screen area.Column major by default. If
     * your data is row major, use null for the data, set the major direction,
     * and then set the data.
     * 
     * @param x horizontal location, in pixels, of the left side of the grid
     *        from the left side of the map.
     * @param y vertical location, in pixels, of the top of the grid from the
     *        top side of the map.
     * @param vResolution the vertical resolution of the data, as pixels per
     *        row.
     * @param hResolution the horizontal resolution of the data, as pixels per
     *        column.
     * @param data a double array of integers, representing the rows and columns
     *        of data.
     */
    public OMGrid(int x, int y, double vResolution, double hResolution, int[][] data) {
        setRenderType(RENDERTYPE_XY);
        set(0.0f, 0.0f, x, y, vResolution, hResolution, data);
    }

    /**
     * Create a OMGrid that covers a x/y screen area, anchored to a lat/lon
     * point. Column major by default. If your data is row major, use null for
     * the data, set the major direction, and then set the data.
     * 
     * @param lat latitude of the anchor point of the grid, in decimal degrees.
     * @param lon longitude of the anchor point of the grid, in decimal degrees.
     * @param x horizontal location, in pixels, of the left side of the grid
     *        from the longitude anchor point.
     * @param y vertical location, in pixels, of the top of the grid from the
     *        latitude anchor point.
     * @param vResolution the vertical resolution of the data, as pixels per
     *        row.
     * @param hResolution the horizontal resolution of the data, as pixels per
     *        column.
     * @param data a double array of integers, representing the rows and columns
     *        of data.
     */
    public OMGrid(double lat, double lon, int x, int y, double vResolution, double hResolution, int[][] data) {
        setRenderType(RENDERTYPE_OFFSET);
        set(lat, lon, x, y, vResolution, hResolution, data);
    }

    /**
     * Create a OMGrid that covers a lat/lon area. Column major by default. If
     * your data is row major, use null for the data, set the major direction,
     * and then set the data.
     * 
     * @param lat latitude of lower left corner of the grid, in decimal degrees.
     * @param lon longitude of lower left corner of the grid, in decimal
     *        degrees.
     * @param vResolution the vertical resolution of the data, as decimal
     *        degrees per row.
     * @param hResolution the horizontal resolution of the data, as decimal
     *        degrees per column.
     * @param data GridData object holding rows and columns of grid data.
     */
    public OMGrid(double lat, double lon, double vResolution, double hResolution, GridData data) {
        setRenderType(RENDERTYPE_LATLON);
        set(lat, lon, 0, 0, vResolution, hResolution, data);
    }

    /**
     * Create a OMGrid that covers a x/y screen area.Column major by default. If
     * your data is row major, use null for the data, set the major direction,
     * and then set the data.
     * 
     * @param x horizontal location, in pixels, of the left side of the grid
     *        from the left side of the map.
     * @param y vertical location, in pixels, of the top of the grid from the
     *        top side of the map.
     * @param vResolution the vertical resolution of the data, as pixels per
     *        row.
     * @param hResolution the horizontal resolution of the data, as pixels per
     *        column.
     * @param data GridData object holding rows and columns of grid data.
     */
    public OMGrid(int x, int y, double vResolution, double hResolution, GridData data) {
        setRenderType(RENDERTYPE_XY);
        set(0.0f, 0.0f, x, y, vResolution, hResolution, data);
    }

    /**
     * Create a OMGrid that covers a x/y screen area, anchored to a lat/lon
     * point. Column major by default. If your data is row major, use null for
     * the data, set the major direction, and then set the data.
     * 
     * @param lat latitude of the anchor point of the grid, in decimal degrees.
     * @param lon longitude of the anchor point of the grid, in decimal degrees.
     * @param x horizontal location, in pixels, of the left side of the grid
     *        from the longitude anchor point.
     * @param y vertical location, in pixels, of the top of the grid from the
     *        latitude anchor point.
     * @param vResolution the vertical resolution of the data, as pixels per
     *        row.
     * @param hResolution the horizontal resolution of the data, as pixels per
     *        column.
     * @param data GridData object holding rows and columns of grid data.
     */
    public OMGrid(double lat, double lon, int x, int y, double vResolution, double hResolution, GridData data) {
        setRenderType(RENDERTYPE_OFFSET);
        set(lat, lon, x, y, vResolution, hResolution, data);
    }

    /**
     * Set the parameters of the OMGrid after construction.
     */
    protected void set(double lat, double lon, int x, int y, double vResolution, double hResolution, int[][] data) {
        set(lat, lon, x, y, vResolution, hResolution, new OMGridData.Int(data));
    }

    /**
     * Set the parameters of the OMGrid after construction.
     */
    protected void set(double lat, double lon, int x, int y, double vResolution, double hResolution, GridData data) {
        latitude = lat;
        longitude = lon;
        point = new Point(x, y);
        verticalResolution = vResolution;
        horizontalResolution = hResolution;
        setData(data);
    }

    /**
     * Set the vertical number of data points. Should correspond to the the
     * data, and to the major setting of the OMGrid. Will be set automatically
     * when the data is set.
     * 
     * @deprecated set when data is set.
     */
    public void setRows(int rows) {
        // this.rows = rows;
    }

    /**
     * Get the vertical number of data points.
     */
    public int getRows() {
        if (data != null) {
            return data.getNumRows();
        } else {
            return 0;
        }
    }

    public void setLatitude(double lat) {
        if (latitude == lat)
            return;
        latitude = lat;
        setNeedToRegenerate(true);
    }

    /**
     * Get the latitude of the lower left anchor point of the grid, in decimal
     * degrees.
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double lon) {
        if (longitude == lon)
            return;
        longitude = lon;
        setNeedToRegenerate(true);
    }

    /**
     * Get the latitude of the lower left anchor point of the grid, in decimal
     * degrees.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Get the screen location, or x/y offset from the lat/lon anchor point, of
     * the lower left corner of the grid.
     */
    public Point getPoint() {
        return point;
    }

    /**
     * Set the horizontal number of data points. Should correspond to the the
     * data, and to the major setting of the OMGrid. Will be set automatically
     * when the data is set. Does nothing.
     * 
     * @deprecated set when the data is set
     */
    public void setColumns(int columns) {
        // this.columns = columns;
    }

    /**
     * Set the horizontal number of data points.
     */
    public int getColumns() {
        if (data != null) {
            return data.getNumColumns();
        } else {
            return 0;
        }
    }

    /**
     * Set which dimension is defined first in the two dimensional array. If
     * COLUMN_MAJOR (true and the default), the first dimension of the data
     * array will represent the horizontal location of the data, and the second
     * dimension will represent the vertical location. Vice versa for
     * COLUMN_ROW. Calling this method will reset the column and row count to
     * match the data to the new orientation.
     */
    public void setMajor(boolean maj) {
        if (data != null && maj != data.getMajor()) {
            data.setMajor(maj);
        }
    }

    /**
     * Set which dimension is defined first in the two dimensional array.
     */
    public boolean getMajor() {
        if (data != null) {
            return data.getMajor();
        }
        return major;
    }

    /**
     * Set the angle that the grid should be rotated. May not be implemented for
     * some OMGridGenerators.
     * 
     * @param orient is the angle of the grid, in radians. Up/North is zero.
     */
    public void setOrientation(double orient) {
        orientation = orient;
    }

    /**
     * Get the angle that was set for the grid to be rotated. In radians,
     * up/north is zero.
     */
    public double getOrientation() {
        return orientation;
    }

    /**
     * Set the data of the grid. The major setting will cause this method to set
     * the number of rows and columns accordingly. The values in the array will
     * be interpreted to the OMGridGenerator that you provide to this OMGrid.
     * The OMGridGenerator will create what gets drawn on the map based on this
     * data. The int[][] will be wrapped by a GridData.Int object.
     */
    public void setData(int[][] data) {
        setData(new OMGridData.Int(data));
    }

    /**
     * Set the data of the grid. The major setting will cause this method to set
     * the number of rows and columns accordingly. The values in the array will
     * be interpreted to the OMGridGenerator that you provide to this OMGrid.
     * The OMGridGenerator will create what gets drawn on the map based on this
     * data.
     */
    public void setData(GridData data) {
        this.data = data;
    }

    /**
     * Get the data array for the OMGrid. What these numbers represent depends
     * on what OMGridGenerator is being used.
     */
    public GridData getData() {
        return data;
    }

    /**
     * There is an option in the OMGrid where the data array contains ID numbers
     * for a set of other objects. So the grid holds onto the location of these
     * objects, and the OMGridObjects provides the ID mapping to the actual
     * object.
     */
    public void setGridObjects(OMGridObjects someGridObjs) {
        gridObjects = someGridObjs;
    }

    /**
     * Get the OMGridObjects containing the mapping of the data array IDs to a
     * set of Objects.
     */
    public OMGridObjects getGridObjects() {
        return gridObjects;
    }

    /**
     * Set the OMGridGenerator that will interpret the data array and create
     * OMGraphics for it.
     */
    public void setGenerator(OMGridGenerator aGenerator) {
        generator = aGenerator;
    }

    /**
     * Get the OMGridGenerator being used to interpret the data array.
     */
    public OMGridGenerator getGenerator() {
        return generator;
    }

    /**
     * Set the number of decimal degrees between horizontal rows.
     */
    public void setVerticalResolution(double vRes) {
        verticalResolution = vRes;
    }

    /**
     * Get the number of decimal degrees between horizontal rows.
     */
    public double getVerticalResolution() {
        return verticalResolution;
    }

    /**
     * Set the number of decimal degrees between vertical columns.
     */
    public void setHorizontalResolution(double hRes) {
        horizontalResolution = hRes;
    }

    /**
     * Get the number of decimal degrees between vertical columns.
     */
    public double getHorizontalResolution() {
        return horizontalResolution;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Set the units for the grid data.
     */
    public void setUnits(Length length) {
        units = length;
    }

    /**
     * Get the units for the grid data.
     */
    public Length getUnits() {
        return units;
    }

    /**
     * Generate OMGraphics based on the data array. If there is an
     * OMGridGenerator, it will be used to generate OMGraphics from the data
     * array. If not, the OMGridObjects will be used to create OMGraphics for
     * the map.
     */
    public synchronized boolean generate(Projection proj) {

        double upLat;
        int columns = getColumns();
        int rows = getRows();
        // Clear out the OMGraphicList part
        super.clear();
        setShape(null);

        /**
         * Let's figure out the dimensions and location of the grid, relative to
         * the screen.
         */
        if (renderType == RENDERTYPE_LATLON) {
            /**
             * Means that the latitudeResolution and horizontalResolution refer
             * to degrees/datapoint.
             */
            double rightLon;

            rightLon = longitude + columns * horizontalResolution;
            upLat = latitude + rows * verticalResolution;

            point1 = (Point) proj.forward(upLat, longitude, new Point());
            point2 = (Point) proj.forward(latitude, rightLon, new Point());

            /** For later... */
            height = point2.y - point1.y;
            width = point2.x - point1.x;

            if (Debug.debugging("grid")) {
                Debug.output("OMGrid.generate:  height = " + height + ", width = " + width);
            }

        } else if (renderType == RENDERTYPE_XY || renderType == RENDERTYPE_OFFSET) {

            width = (int) Math.round(columns * horizontalResolution);
            height = (int) Math.round(rows * verticalResolution);

            if (renderType == RENDERTYPE_OFFSET) {
                upLat = latitude + columns * verticalResolution;
                point1 = (Point) proj.forward(upLat, longitude, new Point());
                point1.x += point.x;
                point1.y += point.y;
            } else {
                point1 = point;
            }

            point2 = new Point(point1.x + width, point1.y + height);
        } else {
            return false;
        }

        if (Debug.debugging("grid")) {
            Debug.output("OMGrid generated grid, at " + point1 + " and " + point2 + " with height " + height + " and width "
                    + width);
        }

        // THis has to happen here, in case the generator wants to
        // check the OMGrid coverage before deciding to do the work
        // for creating OMGraphics.
        setShape();

        /** Now generate the grid in the desired way... */
        if (generator != null && generator.needGenerateToRender()) {
            add(generator.generate(this, proj));
        } else if (gridObjects != null) {
            add(generateGridObjects(proj));
        }

        setNeedToRegenerate(false);

        return true;
    }

    /**
     * Set a bounding rectangle as this OMGrid's shape, based on the location
     * and size of the coverage of the grid.
     */
    public void setShape() {
        // If nothing is available as the shape, generate shape
        // that is a boundary of the generated image.
        // We'll make it a GeneralPath rectangle.
        int w = width;
        int h = height;

        setShape(createBoxShape(point1.x, point1.y, w, h));
    }

    /**
     * Render the OMGraphics created to represent the grid data.
     */
    public void render(Graphics g) {
        if (generator != null) {
            if ((needToRegenerate && generator.needGenerateToRender()) || !isVisible()) {
                Debug.message("grid", "OMGrid: need to generate or is not visible!");
                return;
            }
        }

        super.render(g);
    }

    /**
     * Called from generate() if there isn't a OMGridGenerator. Goes through the
     * grid, figuring out which data array indexes are on the map, and then
     * calls generate on those grid objects.
     */
    public OMGraphic generateGridObjects(Projection proj) {

        OMGraphicList graphiclist = new OMGraphicList();

        /**
         * There could be some way to optimize the search for objects in the
         * grid that are actually visible, but that would require knowledge of
         * the specifics of projections. Keeping this as generic as possible at
         * this point.
         */

        // Since GridObjects only work with a int array, we need to
        // check to see if the GridObject is of type GridData.Int and
        // only bother returning if it is.
        GridData gd = getData();
        if (gd instanceof GridData.Int) {
            GridData.Int gdi = (GridData.Int) gd;

            Point pt = new Point();
            boolean major = gdi.getMajor();
            int[][] data = gdi.getData();

            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data[0].length; y++) {

                    // First, calculate if the grid post is even on
                    // the map
                    if (major == COLUMN_MAJOR) {
                        if (renderType == RENDERTYPE_LATLON) {
                            pt = (Point) proj.forward(latitude + y * verticalResolution, longitude + x * horizontalResolution, pt);
                        } else {
                            pt.y = point1.y + (int) (y * verticalResolution);
                            pt.x = point1.x + (int) (x * horizontalResolution);
                        }
                    } else {
                        if (renderType == RENDERTYPE_LATLON) {
                            pt = (Point) proj.forward(latitude + x * verticalResolution, longitude + y * horizontalResolution, pt);
                        } else {
                            pt.y = point1.y + (int) (x * verticalResolution);
                            pt.x = point1.x + (int) (y * horizontalResolution);
                        }
                    }

                    if ((pt.x >= 0 || pt.x <= proj.getWidth()) && (pt.y >= 0 || pt.y <= proj.getHeight())) {
                        // It's on the map! Get a graphic from it!
                        graphiclist.add(gridObjects.generate(data[x][y], proj));
                    }
                }
            }
        }
        return graphiclist;
    }

    /**
     * The value at the closest SW post to the given lat/lon. This is just a
     * go-to-the-closest-post solution.
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     * @param proj map projection, which is needed for XY or OFFSET grids.
     * @return value found at the nearest grid point. This is an object returned
     *         from the GridObject data object, so what it is depends on that.
     *         You can test if it's a java.lang.Number object to get different
     *         values out of it if it is.
     */
    public Object valueAt(double lat, double lon, Projection proj) {

        int lat_index = -1;
        int lon_index = -1;

        if (renderType == RENDERTYPE_LATLON) {

            lat_index = (int) Math.round((lat - latitude) / verticalResolution);
            lon_index = (int) Math.round((lon - longitude) / horizontalResolution);

        } else if (renderType == RENDERTYPE_XY || renderType == RENDERTYPE_OFFSET) {
            if (getNeedToRegenerate()) {
                /** Only care about this if we need to... */
                if (proj == null) {
                    return null;
                }
                generate(proj);
            }

            Point pt = (Point) proj.forward(lat, lon, new Point());

            lat_index = (int) Math.round((pt.y - point1.y) / verticalResolution);
            lon_index = (int) Math.round((pt.x - point1.x) / horizontalResolution);
        }

        GridData gd = getData();

        if (gd != null && (lat_index >= 0 || lat_index < rows) && (lon_index >= 0 || lon_index < columns)) {
            Object obj = null;
            if (major == COLUMN_MAJOR) {
                obj = gd.get(lon_index, lat_index);
            } else {
                obj = gd.get(lat_index, lon_index);
            }

            return obj;
        }

        return null;
    }

    /**
     * Interpolated value at a given lat/lon - should be more precise than
     * valueAt(), but that depends on the resolution of the data. Works with
     * GridData.Int data objects.
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     * @param proj map projection, which is needed for XY or OFFSET grids.
     * @return value at lat/lon
     */
    public int interpValueAt(double lat, double lon, Projection proj) {
        double lat_index = -1;
        double lon_index = -1;

        GridData gridData = getData();

        if (!(gridData instanceof GridData.Int)) {
            Debug.error("OMGrid.interpValueAt only works for integer data.");
            return 0;
        }

        int[][] data = ((GridData.Int) gridData).getData();
        boolean major = gridData.getMajor();

        if (renderType == RENDERTYPE_LATLON) {

            lat_index = (lat - latitude) / verticalResolution;
            lon_index = (lon - longitude) / horizontalResolution;

        } else if (renderType == RENDERTYPE_XY || renderType == RENDERTYPE_OFFSET) {
            if (getNeedToRegenerate()) {
                /** Only care about this if we need to... */
                if (proj == null) {
                    return GRID_NULL;
                }
                generate(proj);
            }

            Point pt = (Point) proj.forward(lat, lon, new Point());

            lat_index = (pt.y - point1.y) / verticalResolution;
            lon_index = (pt.x - point1.x) / horizontalResolution;
        }

        if ((lat_index >= 0 || lat_index < rows) && (lon_index >= 0 || lon_index < columns)) {

            int lflon_index = (int) Math.floor(lon_index);
            int lclon_index = (int) Math.ceil(lon_index);
            int lflat_index = (int) Math.floor(lat_index);
            int lclat_index = (int) Math.ceil(lat_index);

            // ////////////////////////////////////////////////////
            // Print out grid of 20x20 elevations with
            // the "asked for" point being in the middle
            if (Debug.debugging("grid")) {
                System.out.println("***Elevation Map***");
                for (int l = lclat_index + 5; l > lflat_index - 5; l--) {
                    System.out.println();
                    for (int k = lflon_index - 5; k < lclon_index + 5; k++) {
                        if ((l >= 0 || l < rows) && (k >= 0 || k < columns)) {
                            if (major == COLUMN_MAJOR) {
                                System.out.print(data[k][l] + " ");
                            } else {
                                System.out.print(data[l][k] + " ");
                            }
                        }
                    }
                }
                System.out.println();
                System.out.println();
            }
            // ////////////////////////////////////////////////////

            int ul, ur, ll, lr;

            if (major == COLUMN_MAJOR) {
                ul = data[lflon_index][lclat_index];
                ur = data[lclon_index][lclat_index];
                ll = data[lflon_index][lclat_index];
                lr = data[lclon_index][lclat_index];
            } else {
                ul = data[lclat_index][lflon_index];
                ur = data[lclat_index][lclon_index];
                ll = data[lclat_index][lflon_index];
                lr = data[lclat_index][lclon_index];
            }

            double answer = resolve_four_points(ul, ur, lr, ll, lat_index, lon_index);
            return (int) Math.round(answer);
        }

        return GRID_NULL; // Considered a null value
    }

    /**
     * A try at interpolating the corners of the surrounding posts, given a lat
     * lon. Called from a function where the data for the lon has been read in.
     */
    private double resolve_four_points(int ul, int ur, int lr, int ll, double lat_index, double lon_index) {
        double top_avg = ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * (float) (ur - ul)) + ul;
        double bottom_avg = ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * (float) (lr - ll)) + ll;
        double right_avg = ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * (float) (ur - lr)) + lr;
        double left_avg = ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * (float) (ul - ll)) / 100.0F + ll;

        double lon_avg = ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * (top_avg - bottom_avg)) + bottom_avg;
        double lat_avg = ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * (right_avg - left_avg)) + left_avg;

        double result = (lon_avg + lat_avg) / 2.0;
        return result;
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMGrid) {
            OMGrid grid = (OMGrid) source;
            this.orientation = grid.orientation;
            this.rows = grid.rows;
            this.columns = grid.columns;
            this.latitude = grid.latitude;
            this.longitude = grid.longitude;
            this.verticalResolution = grid.verticalResolution;
            this.horizontalResolution = grid.horizontalResolution;
            this.height = grid.height;
            this.width = grid.width;
            this.major = grid.major;
            this.units = grid.units;

            if (grid.data != null) {
                this.data = grid.data.deepCopy();
            }
            if (grid.point != null) {
                this.point = new Point(grid.point);
            }
            if (grid.point1 != null) {
                this.point1 = new Point(grid.point1);
            }
            if (grid.point2 != null) {
                this.point2 = new Point(grid.point2);
            }
            // Not sure how to handle this, since gridObjects are pretty vague.
            if (grid.gridObjects != null) {
                this.gridObjects = grid.gridObjects;
            }
            if (grid.generator != null) {
                this.generator = grid.generator;
            }

        }
    }
}