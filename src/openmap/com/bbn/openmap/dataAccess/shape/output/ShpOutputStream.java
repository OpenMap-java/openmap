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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/output/ShpOutputStream.java,v $
// $RCSfile: ShpOutputStream.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.shape.output;

import java.io.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.dataAccess.shape.*;

/**
 * Writes data to a .shp file
 * @author Doug Van Auken
 */
public class ShpOutputStream {
    /**
     * An outputstream that writes primitive data types in little
     * endian or big endian 
     */
    private LittleEndianOutputStream _leos = null;

    /**
     * Creates an outputstream to write to
     * @param os The output stream to write to
     */
    public ShpOutputStream(OutputStream os) {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        _leos = new LittleEndianOutputStream(bos);
    }

    /**
     * Determine what type of list is given and write it out.
     * @param list The EsriGraphicList to write
     * @return The index data that is used to create the .shx file 
     */
    public int[][] writeGeometry(EsriGraphicList list) throws IOException {
        if (list instanceof EsriPolygonList ||
            list instanceof EsriPolylineList) {
            return writePolyGeometry(list);
        } else if (list instanceof EsriPointList) {
            return writePointGeometry(list);
        }
        return null;
    }

    /**
     * Calculates the content length for each record, in terms of
     * words as defined by ESRI documentation.  A word is 16 bits, so
     * a double is 4 words and an int is 2 words.
     * @param list The EsriGraphicList to write
     * @return The index data that is used to create the .shx file 
     */
    private int[][] createPointIndex(OMGraphicList list) {
        int[][] indexData = new int[2][list.size()];

        int pos = 50;
        for (int i=0; i < list.size(); i++) {
            //OMGraphicList sublist = (OMGraphicList)list.getOMGraphicAt(i);
            int contentLength = 0;
            contentLength += 2;                    //Shape Type
            contentLength += 4;                    //X
            contentLength += 4;                    //Y
            indexData[1][i] = contentLength;
            indexData[0][i] = pos;
            pos += contentLength + 4;
        }
        return indexData;
    }

    /**
     * Creates a two dimensional array holding a list of shape content
     * lengths and shape content length offsets, as defined in Esri
     * shape file documentation.  This array is used to create the
     * .shx file
     * @param list The list from which to create the respective array
     * @return The index data that is used to create the .shx file 
     */
    private int[][] createPolyIndex(EsriGraphicList list) {
        float[] data;

        int[][] indexData = new int[2][list.size()];
        int pos = 50;

        for (int i=0; i < list.size(); i++) {
            int contentLength = 0;
            OMGraphic graphic = (OMGraphic)list.getOMGraphicAt(i);

            contentLength += 2;                    //Shape Type
            contentLength += 16;                   //Box
            contentLength += 2;                    //NumParts
            contentLength += 2;                    //NumPoints

            if (graphic instanceof OMGraphicList) {
                OMGraphicList sublist = (OMGraphicList)graphic;
                contentLength += sublist.size() * 2; // offsets?
                
                for (int j=0; j < sublist.size(); j++) {
                    OMPoly poly = (OMPoly) sublist.getOMGraphicAt(j);
                    data = poly.getLatLonArray();

                    //each value equals 4 words
                    contentLength += data.length * 4;
                }
            } else {
                contentLength += 2; // offset?

                // Should be an EsriPolyline
                data = ((OMPoly)graphic).getLatLonArray();
                //each value equals 4 words
                contentLength += data.length * 4;
            }

            indexData[1][i] = contentLength;
            indexData[0][i] = pos;
            pos += contentLength + 4;
        }
        return indexData;
    }

    /**
     * Creates an array whose elements specify at what index a shapes geometry
     * begins
     * @param contentLengths The array for which to get offsets from
     * @return An array of record offsets
     */
    private int[] getRecordOffsets(int[] contentLengths) {
        int[] offsets = new int[contentLengths.length];
        int pos = 50;
        for (int i=0; i < contentLengths.length; i++) {
            offsets[i] = pos;
            pos += contentLengths[i] + 4;
        }
        return offsets;
    }

    /**
     * Creates an array whose elements specifies at what index a parts geometry
     * begins
     * @param sublist A list of shapes
     * @return An array of part offsets
     */
    private int[] getPartOffsets(OMGraphicList sublist) {
        int pos = 0;
        int[] offsets = new int[sublist.size()];
        for (int j=0; j < sublist.size(); j++) {
            OMPoly poly = (OMPoly)sublist.getOMGraphicAt(j);
            float[] data = poly.getLatLonArray();
            offsets[j] = pos / 2;
            pos += data.length;
        }
        return offsets;
    }

    /**
     * Iterates through a list of shapes, summing the points per part to determine
     * the number of points per shape
     * @param sublist A list of shapes
     * @return The number of points for a given shape
     */
    private int getPointsPerShape(OMGraphicList sublist) {
        int numPoints = 0;
        for (int i=0; i < sublist.size(); i++) {
            OMPoly poly = (OMPoly)sublist.getOMGraphicAt(i);
            float[] data = poly.getLatLonArray();
            numPoints += data.length;
        }
        numPoints /= 2;
        return numPoints;
    }

    private void writeExtents(float[] extents) 
        throws IOException {
            
        if (_leos == null) {
            return;
        }

        if (extents[0] == 90f &&
            extents[1] == 180f &&
            extents[2] == -90f &&
            extents[3] == -180f) {
                
            //Whoa! not set from defaults correctly!
            // use old, hardcoded way.
            _leos.writeLEDouble(-180.0);
            _leos.writeLEDouble(-90.0);
            _leos.writeLEDouble(180.0);
            _leos.writeLEDouble(90.0);
                
        } else {
            _leos.writeLEDouble((float)extents[1]);
            _leos.writeLEDouble((float)extents[0]);
            _leos.writeLEDouble((float)extents[3]);
            _leos.writeLEDouble((float)extents[2]);
        }

    }
            
    /**
     * Writes polygon geometry to the class scope LittleEndianInputStream.
     * @param list The list of geometry objects to save
     * @return A two dimensional array containing shape offsets and
     * content lengths 
     */
    public int[][] writePolyGeometry(EsriGraphicList list) 
        throws IOException {

        int numShapes = list.size();
        OMPoly poly;

        _leos.writeInt(9994);                                   //Byte 0 File Code
        _leos.writeInt(0);                                      //Byte 4 Unused
        _leos.writeInt(0);                                      //Byte 8 Unused
        _leos.writeInt(0);                                      //Byte 12 Unused
        _leos.writeInt(0);                                      //Byte 16 Unused
        _leos.writeInt(0);                                      //Byte 20 Unused

        int[][] indexData = createPolyIndex(list);
        int contentLength = list.size();
        contentLength *= 4;                                     //Multiply each shape by 4 words
        contentLength += 50;                                    //Add 50 words for the header section.

        _leos.writeInt(contentLength);                          //Byte 24 File Length
        _leos.writeLEInt(1000);                                 //Byte 28 Version
        _leos.writeLEInt(list.getType());                       //Byte 32 Shape Type

        //Writes bounding box.
        float[] extents = list.getExtents();
        writeExtents(extents);

        _leos.writeDouble(0.0);                                 //Byte 68
        _leos.writeDouble(0.0);                                 //Byte 76
        _leos.writeDouble(0.0);                                 //Byte 84
        _leos.writeDouble(0.0);                                 //Byte 92

        //Iterate through the list
        for (int i=0; i < list.size(); i++) {

            OMGraphic graphic = list.getOMGraphicAt(i);
            
            //Record header
            _leos.writeInt(i+1); // Record numbers start with 1
            _leos.writeInt(indexData[1][i]);
            
            //Beginning of Geometry data
            _leos.writeLEInt(list.getType());   //Little endian

            // More stuff needs to be written out for just the OMPoly case...
            // Single part, etc.

            if (graphic instanceof EsriGraphicList) {

                // Assumes that the elements of the top level list are
                // EsriGraphicLists, too.  This will probably be changing.
                EsriGraphicList sublist = (EsriGraphicList) graphic;

                //Writes bounding box.
                extents = sublist.getExtents();
                writeExtents(extents);

                //Writes number of parts
                int numParts = sublist.size();
                _leos.writeLEInt(numParts);

                //Write number of points per shape
                int numPoints = getPointsPerShape(sublist);
                _leos.writeLEInt(numPoints);

                //Write the offsets to each part for a given shape
                int[] offsets = getPartOffsets(sublist);

                for (int j=0; j < offsets.length;  j++) {
                    _leos.writeLEInt(offsets[j]);
                }

                //Write the geometry for each part
                for (int j=0; j < sublist.size(); j++) {
                    poly = (OMPoly)sublist.getOMGraphicAt(j);
                    float[] data = poly.getLatLonArray();
                    int n = 0;
                    while (n < data.length) {
                        Float lat = new Float(data[n++]);
                        Float lon = new Float(data[n++]);
                        _leos.writeLEDouble(Math.toDegrees(lon.doubleValue()));
                        _leos.writeLEDouble(Math.toDegrees(lat.doubleValue()));
                    }
                }
            } else {
                extents = ((EsriGraphic)graphic).getExtents();
                writeExtents(extents);

                //Writes number of parts for shape (1)
                _leos.writeLEInt(1);

                poly = (OMPoly)graphic;
                float[] data = poly.getLatLonArray();

                //Write number of points for shape
                _leos.writeLEInt(data.length/2);

                //Write the offsets to this shape
                _leos.writeLEInt(0);

                int n = 0;
                while (n < data.length) {
                    Float lat = new Float(data[n++]);
                    Float lon = new Float(data[n++]);
                    _leos.writeLEDouble(Math.toDegrees(lon.doubleValue()));
                    _leos.writeLEDouble(Math.toDegrees(lat.doubleValue()));
                }
            }
        }
        _leos.flush();
        _leos.close();
        return indexData;
    }

    /**
     * Writes point geometry to the class scope LittleEndianOutputStream.
     * @param list An EsriGraphicList of points
     * @return A two dimensional array containing shape offsets and
     * content lengths 
     */
    public int[][] writePointGeometry(EsriGraphicList list) throws IOException {
        _leos.writeInt(9994);                               //Big
        _leos.writeInt(0);                                  //Big
        _leos.writeInt(0);                                  //Big
        _leos.writeInt(0);                                  //Big
        _leos.writeInt(0);                                  //Big
        _leos.writeInt(0);                                  //Big

        int[][] indexData = createPointIndex(list);
        int contentLength = list.size() * 4 + 50;

        _leos.writeInt(contentLength);                  //Big
        _leos.writeLEInt(1000);            //Little
        _leos.writeLEInt(list.getType());

        //Writes bounding box.
        float[] extents = list.getExtents();
        writeExtents(extents);

        _leos.writeDouble(0.0);
        _leos.writeDouble(0.0);
        _leos.writeDouble(0.0);
        _leos.writeDouble(0.0);

        for (int i=0; i < list.size(); i++) {
            OMPoint point = (OMPoint)list.getOMGraphicAt(i);
            LatLonPoint pt = new LatLonPoint(point.getLat(), point.getLon());

            //Record header...
            _leos.writeInt(i+1); // Record numbers start with 1
            _leos.writeInt(indexData[1][i]);

            //Beginning of Geometry data
            _leos.writeLEInt(list.getType());   

            Float lat = new Float(pt.getLatitude());
            Float lon = new Float(pt.getLongitude());

            _leos.writeLEDouble(lon.doubleValue());
            _leos.writeLEDouble(lat.doubleValue());
        }
        _leos.flush();
        _leos.close();

        return indexData;
    }
}
