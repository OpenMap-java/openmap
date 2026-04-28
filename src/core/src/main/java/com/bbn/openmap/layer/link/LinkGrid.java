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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkGrid.java,v $
// $RCSfile: LinkGrid.java,v $
// $Revision: 1.7 $
// $Date: 2007/02/26 17:12:43 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.grid.GridData;
import com.bbn.openmap.util.Debug;

/**
 * Reading and writing the Link protocol version of a grid object. It is assumed
 * that the client will know how to render/handle this grid when it arrives,
 * since it doesn't have any implicit drawing attributes, despite having a
 * semantics object.
 */
public class LinkGrid implements LinkGraphicConstants {

    /**
     * Lat/Lon placement.
     * 
     * @param lt latitude of the top of the grid.
     * @param ln longitude of the left side of the grid.
     * @param rows number of vertical points of the grid.
     * @param columns number of horizontal points of the grid.
     * @param orientation the direction of the vertical axits of the grid, in
     *        radians from up ( North).
     * @param vResolution degrees/point between rows of the grid.
     * @param hResolution degrees/point between columns of the grid.
     * @param major designation of the presentation of the data, as columns
     *        (COLUMN_MAJOR) or rows (ROW_MAJOR).
     * @param data data points of the grid.
     * @param properties description of drawing attributes.
     * @param dos the data output stream to write the object to.
     * @throws IOException
     */
    public static void write(float lt, float ln, int rows, int columns,
                             float orientation, float vResolution,
                             float hResolution, int major, int[] data,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.GRID_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_GRID);
        dos.writeByte(RENDERTYPE_LATLON);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(rows);
        dos.writeInt(columns);
        dos.writeFloat(orientation);
        dos.writeFloat(vResolution);
        dos.writeFloat(hResolution);
        dos.writeByte(major);

        dos.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            dos.writeInt(data[i]);
        }

        properties.write(dos);
    }

    /**
     * XY placement.
     * 
     * @param x1 window location of the left side of the grid.
     * @param y1 window location of the top of the grid.
     * @param rows number of vertical points of the grid.
     * @param columns number of horizontal points of the grid.
     * @param orientation the direction of the vertical axits of the grid, in
     *        radians from up ( North).
     * @param vResolution pixels/point between rows of the grid.
     * @param hResolution pixels/point between columns of the grid.
     * @param major designation of the presentation of the data, as columns
     *        (COLUMN_MAJOR) or rows (ROW_MAJOR).
     * @param data data points of the grid.
     * @param properties description of drawing attributes.
     * @param dos the data output stream to write the object to.
     * @throws IOException
     */
    public static void write(int x1, int y1, int rows, int columns,
                             float orientation, float vResolution,
                             float hResolution, int major, int[] data,
                             LinkProperties properties, DataOutputStream dos)
            throws IOException {

        dos.write(Link.GRID_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_GRID);
        dos.writeByte(RENDERTYPE_XY);
        dos.writeInt(x1);
        dos.writeInt(y1);

        dos.writeInt(rows);
        dos.writeInt(columns);
        dos.writeFloat(orientation);
        dos.writeFloat(vResolution);
        dos.writeFloat(hResolution);
        dos.writeByte(major);

        dos.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            dos.writeInt(data[i]);
        }

        properties.write(dos);
    }

    /**
     * Lat/lon placement with XY offset.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param rows number of vertical points of the grid.
     * @param columns number of horizontal points of the grid.
     * @param orientation the direction of the vertical axits of the grid, in
     *        radians from up ( North).
     * @param vResolution pixels/point between rows of the grid.
     * @param hResolution pixels/point between columns of the grid.
     * @param major designation of the presentation of the data, as columns
     *        (COLUMN_MAJOR) or rows (ROW_MAJOR).
     * @param data data points of the grid.
     * @param properties description of drawing attributes.
     * @param dos the data output stream to write the object to.
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
                             int rows, int columns, float orientation,
                             float vResolution, float hResolution, int major,
                             int[] data, LinkProperties properties,
                             DataOutputStream dos) throws IOException {

        dos.write(Link.GRID_HEADER.getBytes());
        dos.writeByte(GRAPHICTYPE_GRID);
        dos.writeByte(RENDERTYPE_OFFSET);
        dos.writeFloat(lt);
        dos.writeFloat(ln);
        dos.writeInt(offset_x1);
        dos.writeInt(offset_y1);

        dos.writeInt(rows);
        dos.writeInt(columns);
        dos.writeFloat(orientation);
        dos.writeFloat(vResolution);
        dos.writeFloat(hResolution);
        dos.writeByte(major);

        dos.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            dos.writeInt(data[i]);
        }

        properties.write(dos);
    }

    public static void write(OMGrid grid, Link link, LinkProperties props)
            throws IOException {

        int major = grid.getMajor() ? LinkGraphicConstants.ROW_MAJOR
                : LinkGraphicConstants.COLUMN_MAJOR;

        int rows = grid.getRows();
        int columns = grid.getColumns();

        GridData gd = grid.getData();

        if (!(gd instanceof GridData.Int)) {
            Debug.output("LinkGrid requires OMGrid containing integer data.");
            return;
        }

        int[][] d = ((GridData.Int) gd).getData();
        int[] data = new int[rows * columns];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                data[(i * d[0].length) + j] = d[i][j];
            }
        }

        switch (grid.getRenderType()) {
        case OMGrid.RENDERTYPE_LATLON:
            LinkGrid.write((float) grid.getLatitude(),
                    (float) grid.getLongitude(),
                    rows,
                    columns,
                    (float) grid.getOrientation(),
                    (float) grid.getVerticalResolution(),
                    (float) grid.getHorizontalResolution(),
                    major,
                    data,
                    props,
                    link.dos);
            break;
        case OMGrid.RENDERTYPE_XY:
            LinkGrid.write((int) grid.getPoint().getX(),
                    (int) grid.getPoint().getY(),
                    rows,
                    columns,
                    (float) grid.getOrientation(),
                    (float) grid.getVerticalResolution(),
                    (float) grid.getHorizontalResolution(),
                    major,
                    data,
                    props,
                    link.dos);
            break;
        case OMGrid.RENDERTYPE_OFFSET:
            LinkGrid.write((float) grid.getLatitude(),
                    (float) grid.getLongitude(),
                    (int) grid.getPoint().getX(),
                    (int) grid.getPoint().getY(),
                    rows,
                    columns,
                    (float) grid.getOrientation(),
                    (float) grid.getVerticalResolution(),
                    (float) grid.getHorizontalResolution(),
                    major,
                    data,
                    props,
                    link.dos);
            break;
        default:
            Debug.error("LinkGrid.write: grid rendertype unknown.");
        }
    }

    /**
     * Read a Grid off a DataInputStream. Assumes the Grid header has already
     * been read.
     * 
     * @param dis DataInputStream to read from.
     * @return OMGrid
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMGrid
     */
    public static OMGrid read(DataInputStream dis) throws IOException {
        return read(dis, null);
    }

    /**
     * Read a Grid off a DataInputStream. Assumes the Grid header has already
     * been read.
     * 
     * @param dis DataInputStream to read from.
     * @param propertiesBuffer a LinkProperties object used to cache previous
     *        settings that can be set on the OMPoly being read.
     * @return OMGrid
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMGrid
     */
    public static OMGrid read(DataInputStream dis,
                              LinkProperties propertiesBuffer)
            throws IOException {

        Debug.message("linkdetail", "LinkGrid: reading from link.");

        OMGrid grid = null;
        float lat = 0;
        float lon = 0;
        int x = 0;
        int y = 0;

        int renderType = dis.readByte();

        switch (renderType) {
        case RENDERTYPE_OFFSET:
            lat = dis.readFloat();
            lon = dis.readFloat();
            Debug.message("linkdetail", "LinkGrid: Offset Lat/Lon = " + lat
                    + "/" + lon + " with");
            // Fall through...
        case RENDERTYPE_XY:
            x = dis.readInt();
            y = dis.readInt();
            Debug.message("linkdetail", "LinkGrid: x/y = " + x + "/" + y);
            break;
        case RENDERTYPE_LATLON:
        default:
            lat = dis.readFloat();
            lon = dis.readFloat();
            Debug.message("linkdetail", "LinkGrid: Lat/Lon = " + lat + "/"
                    + lon);
        }

        int rows = dis.readInt();
        int columns = dis.readInt();
        float orientation = dis.readFloat();
        float vResolution = dis.readFloat();
        float hResolution = dis.readFloat();
        int major = dis.readByte();

        int length = dis.readInt();

        Debug.message("linkdetail", "LinkGrid details: rows = "
                + rows
                + ", columns = "
                + columns
                + ", orientation = "
                + orientation
                + ", vertical resolution = "
                + vResolution
                + ", horizontal resolution = "
                + hResolution
                + ", major dimension = "
                + (major == LinkGraphicConstants.COLUMN_MAJOR ? "COLUMN_MAJOR"
                        : "ROW_MAJOR") + ", with number of points = " + length);

        int[][] data;
        if (major == LinkGraphicConstants.COLUMN_MAJOR) {
            data = new int[columns][rows];
        } else {
            data = new int[rows][columns];
        }

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = dis.readInt();
                // Debug.message("linkdetail", "LinkGrid reading " +
                // (rows*i + j) + " " +
                // (major !=
                // LinkGraphicConstants.COLUMN_MAJOR?"column":"row")+
                // " " + j + " = " + Integer.toHexString(data[i][j]) +
                // " (" + data[i][j] + ")");
            }
            // Debug.message("linkdetail", "LinkGrid reading " +
            // (major ==
            // LinkGraphicConstants.COLUMN_MAJOR?"column":"row")+
            // " " + i);
        }

        Debug.message("linkdetail", "LinkGrid read all the data.");

        switch (renderType) {
        case RENDERTYPE_OFFSET:
            grid = new OMGrid(lat, lon, x, y, vResolution, hResolution, data);
            break;
        case RENDERTYPE_XY:
            grid = new OMGrid(x, y, vResolution, hResolution, data);
            break;
        case RENDERTYPE_LATLON:
        default:
            grid = new OMGrid(lat, lon, vResolution, hResolution, data);
        }

        Debug.message("linkdetail", "LinkGrid created OMGrid.");

        if (grid != null) {
            grid.setMajor(major == LinkGraphicConstants.COLUMN_MAJOR ? true
                    : false);
            grid.setOrientation(orientation);
            LinkProperties.loadPropertiesIntoOMGraphic(dis, grid, propertiesBuffer);
        }

        Debug.message("linkdetail", "LinkGrid done.");

        return grid;
    }
}