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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/input/ShpInputStream.java,v $
// $RCSfile: ShpInputStream.java,v $
// $Revision: 1.3 $
// $Date: 2003/08/14 22:42:18 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.shape.input;

import java.io.*;
import java.awt.*;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.dataAccess.shape.*;
import com.bbn.openmap.util.Debug;

/**
 * Reads geometry data from a .shp file.
 * @author Doug Van Auken
 */
public class ShpInputStream implements ShapeConstants {
    /** 
     * An input stream to process primitives in Little Endian or Big
     * Endian 
     */
    private LittleEndianInputStream _leis = null;

    protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();
    
    /**
     * Constructor
     * @param is An inputstream to chain with LittleEndianInputStream
     */
    public ShpInputStream(InputStream is) {
	BufferedInputStream bis = new BufferedInputStream(is);
	_leis = new LittleEndianInputStream(bis);
    }
    
    /**
     * Set the DrawingAttributes to use for drawing the graphics.
     */
    public void setDrawingAttributes(DrawingAttributes da) {
	drawingAttributes = da;
    }

    /**
     * Get the DrawingAttributes used for drawing the graphics.
     */
    public DrawingAttributes getDrawingAttributes() {
	return drawingAttributes;
    }

    /**
     * Reads geometry from a .shp file
     * @param indexData The index data retreived from the .shx file
     * @return EsriGraphicList A list of geometry
     */
    public EsriGraphicList getGeometry(int[][] indexData) throws Exception{
	EsriGraphicList list = null;
	int shapeType = readHeader();
	if (shapeType==1) {
	    list = getPointGeometry(indexData);
	}
	else if (shapeType==3) {
	    list = getPolyGeometry(indexData, 3);
	}
	else if (shapeType==5) {
	    list = getPolyGeometry(indexData, 5);
	}
	return list;
    }
    
    /**
     * Creates an array that specifies at what index a parts geometry
     * begins with
     * @return An array whose elements denote the position where a
     * part begins witin an array of point data for a given shape 
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
     * Iterates through the given input stream to contruct geometry objects
     * @param is The input stream from which to iterate through
     * @param vector A list of offsets obtained by iterating through
     * the associated SHX file
     * @return list An OMGraphicList that contains the collection of
     * objects created by iterating through this input stream 
     */
    public EsriGraphicList getPointGeometry(int[][] indexData) 
	throws Exception {

	EsriGraphicList list = new EsriPointList();
	double lambda, phi;
	int numVertices;
	int numShapes = indexData[1].length;
	
	EsriPoint point;
	for (int i=0; i<numShapes; i++) {
	    int shpRecord = _leis.readInt();
	    int shpContentLength = _leis.readInt();
	    int shpType = _leis.readLEInt();
	    
	    lambda = _leis.readLEDouble();
	    phi = _leis.readLEDouble();
	    
	    Double d1 = new Double(lambda);
	    Double d2 = new Double(phi);
	    
	    float f1 = d1.floatValue();
	    float f2 = d2.floatValue();
	    
	    point = new EsriPoint(f2, f1);
	    point.setAppObject(new Integer(shpRecord));
	    if (drawingAttributes != null) {
		drawingAttributes.setTo(point);
	    } else {
		DrawingAttributes.DEFAULT.setTo(point);
	    }
	    list.add(point);
	}
	return list;
    }
    
    /**
     * Iterates through each part of shape to obtain the total number of points
     * @param sublist A list that contains multiple parts
     * @return The total number of points for a given shape
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
    
    
    /**
     * Iterates through the given input stream to contruct geometry objects
     * @param is The input stream from which to iterate through
     * @param vector A list of offsets obtained by iterating through
     * the associated SHX file
     * @return list An OMGraphicList that contains the collection of
     * objects created by iterating through this input stream 
     */
    public EsriGraphicList getPolyGeometry(int[][] indexData, int shapeType) 
	throws Exception {

	EsriGraphicList list = null;
	if (shapeType == SHAPE_TYPE_POLYLINE) {
	    list = new EsriPolylineList();
	}
	else if (shapeType == SHAPE_TYPE_POLYGON) {
	    list = new EsriPolygonList();
	}
	
	int numVertices;
	int pos = 0;
	
	int numShapes = indexData[1].length;
	
	for (int t=0; t < numShapes; t++) {
	    int shpRecord = _leis.readInt();
	    int shpContentLength = _leis.readInt();
	    int shpType = _leis.readLEInt();
	    
	    double xLeft = _leis.readLEDouble();
	    double xBottom = _leis.readLEDouble();
	    double xRight = _leis.readLEDouble();
	    double xTop = _leis.readLEDouble();
	    int numParts = _leis.readLEInt();
	    int numPoints = _leis.readLEInt();
	    
	    double[] ptsx = new double[numPoints];
	    double[] ptsy = new double[numPoints];
	    int[] offsets = new int[numParts];

	    // OK, we don't want to create a sublist unless the poly
	    // has multiple parts.  Remember that.  sublist will only
	    // be created if there is more than one part.

	    for (int n=0; n < numParts; n++) {
		offsets[n] = _leis.readLEInt();
	    }

	    float[] points;
	    OMGraphic poly = null;
	    EsriGraphicList sublist = null;

	    if (numParts > 1) {
		if (shapeType == SHAPE_TYPE_POLYLINE) {
		    sublist = new EsriPolylineList();
		} else if (shapeType == SHAPE_TYPE_POLYGON) {
		    sublist = new EsriPolygonList();
		}

		sublist.setVague(true); // Treat sublist as one OMGraphic.
		sublist.setAppObject(new Integer(shpRecord));
	    }

	    for (int j=0; j < numParts; j++) {
		int i=0;
		if (j!=numParts-1) {
		    numVertices = (offsets[j + 1]) - offsets[j];
		    points = new float[numVertices*2];
		}
		else {
		    numVertices = (numPoints - offsets[j]);
		    points = new float[numVertices*2];
		}
		for (int n=0; n < numVertices; n++) {
		    double lambda = _leis.readLEDouble();
		    double phi = _leis.readLEDouble();
		    
		    Double d1 = new Double(phi);
		    Double d2 = new Double(lambda);
		    
		    points[i++] = d1.floatValue();
		    points[i++] = d2.floatValue();
		}

		if (shapeType == SHAPE_TYPE_POLYLINE) {
		    poly = new EsriPolyline(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
		} else if (shapeType == SHAPE_TYPE_POLYGON) {
		    poly = new EsriPolygon(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
		}

		poly.setAppObject(new Integer(shpRecord));
		
		if (drawingAttributes != null) {
		    drawingAttributes.setTo(poly);
		} else {
		    DrawingAttributes.DEFAULT.setTo(poly);
		}

		if (poly instanceof EsriPolyline) {
		    // Just to make sure it gets rendered as a
		    // polyline.  The OMPoly code will render it as a
		    // polygon if the fill color is not clear.
		    poly.setFillPaint(OMColor.clear);
		}

		if (sublist != null) {
		    sublist.addOMGraphic(poly);
		}
	    }

	    // sublist is null for non multi-part geometries.
	    
	    if (sublist != null) {
		list.add(sublist);
	    } else {
		list.add(poly);
	    }

	    if (Debug.debugging("esri")) {
		EsriGraphic eg = null;
		if (sublist == null) {
		    eg = (EsriGraphic)poly;
		} else {
		    eg = sublist;
		}

		float[] ex1 = eg.getExtents();
		Debug.output("extents of list: xmin=" +
			     ex1[1] + ", ymin=" +
			     ex1[0] + ", xmax=" +
			     ex1[3] + ", ymax=" +
			     ex1[2]);
		Debug.output("list.size=" + list.size());
	    }
	}


	if (Debug.debugging("esri")) {
	    float[] ex = list.getExtents();
	    Debug.output("extents of list: xmin=" +
			 ex[1] + ", ymin=" +
			 ex[0] + ", xmax=" +
			 ex[3] + ", ymax=" +
			 ex[2]);
	}
	
	return list;
    }
    
    /**
     * Reads the header section of a .shp file
     * @return the shape type
     */
    public int readHeader() throws IOException{
	int fileCode = _leis.readInt();
	_leis.skipBytes(20);
	int fileLength = _leis.readInt();
	int version = _leis.readLEInt();
	int shapeType = _leis.readLEInt();
	double xMin = _leis.readLEDouble();
	double yMin = _leis.readLEDouble();
	double xMax = _leis.readLEDouble();
	double yMax = _leis.readLEDouble();
	double zMin = _leis.readLEDouble();
	double zMax = _leis.readLEDouble();
	double mMin = _leis.readLEDouble();
	double mMax = _leis.readLEDouble();
	return shapeType;
    }
}
