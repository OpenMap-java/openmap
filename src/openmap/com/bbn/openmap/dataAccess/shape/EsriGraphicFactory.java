//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: EsriGraphicFactory.java,v $
//$Revision: 1.10 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.awt.geom.Point2D;
import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.bbn.openmap.dataAccess.shape.input.LittleEndianInputStream;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.DataBounds;

/**
 * EsriGraphicFactory creates OMGraphics/EsriGraphics from a shape file.
 * 
 * @author ddietrick
 */
public class EsriGraphicFactory implements ShapeConstants {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.shape.EsriGraphicFactory");

    protected int lineType = OMGraphic.LINETYPE_STRAIGHT;
    protected GeoCoordTransformation dataTransformation = null;
    protected Class<?> precision = Float.TYPE;

    protected static boolean verbose = false;

    /**
     * Will create shapes with straight line types (faster for rendering) and no
     * data transformation).
     */
    public EsriGraphicFactory() {
    }

    /**
     * Create a factory
     * 
     * @param lineType the line type to use for polys
     * @param dataTransformation the transformation to use on data to convert it
     *        to lat/lon decimal degrees.
     */
    public EsriGraphicFactory(int lineType, GeoCoordTransformation dataTransformation) {
        this.lineType = lineType;
        this.dataTransformation = dataTransformation;
    }

    /**
     * Create an OMGraphicList containing OMGraphics representing shape file
     * contents.
     * 
     * @param shp BinaryFile from shp file.
     * @param drawingAttributes DrawingAttribute dictating rendering.
     * @param pointRepresentation what to use for point object rendering.
     * @param mapProj current map projection, if not null will be used to
     *        position OMGraphics.
     * @param list The OMGraphicList to add OMGraphics to, returned. OK if null.
     * @return OMGraphicList containing OMGraphics for shapes.
     * @throws IOException
     * @throws FormatException
     */
    public OMGraphicList getEsriGraphics(BinaryFile shp, DrawingAttributes drawingAttributes,
                                         Object pointRepresentation, Projection mapProj,
                                         OMGraphicList list) throws IOException, FormatException {
        shp.seek(0);
        verbose = logger.isLoggable(Level.FINER);
        Header header = new Header(shp, dataTransformation);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(header.toString());
        }
        if (list == null) {
            list = createEsriGraphicList(header.shapeType);
        }
        int offset = 100; // next byte past header;
        // BAJ 20070604 Stop here for empty shape files
        if (header.fileLength == offset) {
            if (verbose) {
                logger.finer("Header file length doesn't == 100: " + header.fileLength);
            }
            return list;
        }
        // Put a flag in here to force the file to be read until EOF
        boolean ignoreFileLength = logger.isLoggable(Level.FINE);

        EsriGraphicFactory.ReadByteTracker byteTracker = new EsriGraphicFactory.ReadByteTracker();
        try {
            OMGraphic eg = makeEsriGraphicFromRecord(offset, shp, drawingAttributes, pointRepresentation, byteTracker);
            // 8 for shape type and record length
            offset += byteTracker.currentCount + 8;

            while (offset != header.fileLength || ignoreFileLength) {
                projGraphicAndAdd(eg, list, mapProj);
                try {
                    eg = makeEsriGraphicFromRecord(offset, shp, drawingAttributes, pointRepresentation, byteTracker);
                } catch (EOFException eof) {
                    logger.fine("File length (" + header.fileLength
                            + " bytes) is incorrect, file was read as much as possible (" + offset
                            + " bytes).");
                    eg = null;
                    break;
                }
                // 8 for shape type and record length
                offset += byteTracker.currentCount + 8;
            }

            if (eg != null) {
                projGraphicAndAdd(eg, list, mapProj);
            }

        } catch (FormatException fe) {
            fe.printStackTrace();
        }

        return list;
    }

    /**
     * Create OMGraphics from input stream from shp file.
     * 
     * @param iStream stream created from shp file
     * @param drawingAttributes DrawingAttributes for rendering
     * @param pointRepresentation representation of point object rendering
     * @param mapProj current map projection
     * @param list list to add OMGraphics to, OK if null.
     * @return OMGraphicList containing shapes.
     * @throws IOException
     * @throws FormatException
     */
    public OMGraphicList getEsriGraphics(LittleEndianInputStream iStream,
                                         DrawingAttributes drawingAttributes,
                                         Object pointRepresentation, Projection mapProj,
                                         OMGraphicList list) throws IOException, FormatException {
        Header header = new Header(iStream, dataTransformation);
        verbose = logger.isLoggable(Level.FINER);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(header.toString());
        }
        if (list == null) {
            list = createEsriGraphicList(header.shapeType);
        }
        int offset = 100; // next byte past header;
        // BAJ 20070604 Stop here for empty shape files
        if (header.fileLength == offset) {
            if (verbose) {
                logger.info("Header file length doesn't == 100: " + header.fileLength);
            }
            return list;
        }
        // Put a flag in here to force the file to be read until EOF
        boolean ignoreFileLength = logger.isLoggable(Level.FINE);

        EsriGraphicFactory.ReadByteTracker byteTracker = new EsriGraphicFactory.ReadByteTracker();
        try {
            OMGraphic eg = makeEsriGraphicFromRecord(offset, iStream, drawingAttributes, pointRepresentation, byteTracker);
            // 8 for shape type and record length
            offset += byteTracker.currentCount + 8;

            while (offset != header.fileLength || ignoreFileLength) {
                projGraphicAndAdd(eg, list, mapProj);
                try {
                    eg = makeEsriGraphicFromRecord(offset, iStream, drawingAttributes, pointRepresentation, byteTracker);
                } catch (EOFException eof) {
                    logger.fine("File length (" + header.fileLength
                            + " bytes) is incorrect, file was read as much as possible (" + offset
                            + " bytes).");
                    eg = null;
                    break;
                }
                // 8 for shape type and record length
                offset += byteTracker.currentCount + 8;
            }

            if (eg != null) {
                projGraphicAndAdd(eg, list, mapProj);
            }

        } catch (FormatException fe) {
            fe.printStackTrace();
        }

        return list;
    }

    protected void projGraphicAndAdd(OMGraphic eg, OMGraphicList list, Projection mapProj) {
        if (eg != null) {
            if (mapProj != null) {
                eg.generate(mapProj);
            }

            list.add((OMGraphic) eg);
        }
    }

    public OMGraphic makeEsriGraphicFromRecord(int byteOffset, BinaryFile shp,
                                               DrawingAttributes drawingAttributes,
                                               Object pointRepresentation,
                                               ReadByteTracker byteTracker)
            throws IOException, FormatException {
        shp.seek(byteOffset);
        shp.byteOrder(true);

        int recordNumber = shp.readInteger();
        int recordContentLength = shp.readInteger() * 2;
        byteTracker.reset(recordContentLength);

        OMGraphic omg = makeEsriGraphic(shp, drawingAttributes, pointRepresentation, byteTracker);
        if (omg != null) {
            omg.putAttribute(SHAPE_INDEX_ATTRIBUTE, new Integer(recordNumber - 1));
        }

        return omg;
    }

    public OMGraphic makeEsriGraphicFromRecord(int byteOffset, LittleEndianInputStream iStream,
                                               DrawingAttributes drawingAttributes,
                                               Object pointRepresentation,
                                               ReadByteTracker byteTracker)
            throws IOException, FormatException {

        int recordNumber = iStream.readInt();
        int recordContentLength = iStream.readInt() * 2;
        byteTracker.reset(recordContentLength);

        OMGraphic omg = makeEsriGraphic(iStream, drawingAttributes, pointRepresentation, byteTracker);
        if (omg != null) {
            omg.putAttribute(SHAPE_INDEX_ATTRIBUTE, new Integer(recordNumber - 1));
        }

        return omg;
    }

    /**
     * Creates a OMGraphic from the shape file data.
     * 
     * @param shpFile BinaryFile positioned for this record
     * @param drawingAttributes rendering attributes for OMGraphic
     * @param pointRepresentation object to use for representing point data
     * @param byteTracker keeps track of how many bytes were used for this
     *        record.
     * @return OMGraphic for record
     * @exception IOException if something goes wrong reading the file
     * @exception FormatException
     */
    protected OMGraphic makeEsriGraphic(BinaryFile shpFile, DrawingAttributes drawingAttributes,
                                        Object pointRepresentation, ReadByteTracker byteTracker)
            throws IOException, FormatException {
        /*
         * SHAPE_TYPE_NULL = 0; SHAPE_TYPE_POINT = 1; SHAPE_TYPE_ARC = 3;
         * SHAPE_TYPE_POLYLINE = 3; SHAPE_TYPE_POLYGON = 5;
         * SHAPE_TYPE_MULTIPOINT = 8; SHAPE_TYPE_POINTZ = 11;
         * SHAPE_TYPE_POLYLINEZ = 13; SHAPE_TYPE_POLYGONZ = 15;
         * SHAPE_TYPE_MUILTIPOINTZ = 18; SHAPE_TYPE_POINTM = 21;
         * SHAPE_TYPE_POLYLINEM = 23; SHAPE_TYPE_POLYGONM = 25;
         * SHAPE_TYPE_MULTIPOINTM = 28; SHAPE_TYPE_MULTIPATCH = 31;
         */
        EsriGraphic eg = null;
        shpFile.byteOrder(false);

        int shapeType = shpFile.readInteger();
        byteTracker.addRead(4);

        if (verbose) {
            logger.info("reading shape type: " + shapeType + ", "
                    + ShapeUtils.getStringForType(shapeType));
        }

        switch (shapeType) {

        case SHAPE_TYPE_NULL:
            break;
        case SHAPE_TYPE_POINT:
            eg = createPointGraphic(shpFile, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYLINE:
            eg = createPolylineGraphic(shpFile, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYGON:
            eg = createPolygonGraphic(shpFile, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPOINT:
            eg = createMultiPointGraphic(shpFile, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POINTZ:
            eg = createPointZGraphic(shpFile, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYLINEZ:
            eg = createPolylineZGraphic(shpFile, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYGONZ:
            eg = createPolygonZGraphic(shpFile, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPOINTZ:
            eg = createMultiPointZGraphic(shpFile, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POINTM:
            eg = createPointMGraphic(shpFile, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYLINEM:
            eg = createPolylineMGraphic(shpFile, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYGONM:
            eg = createPolygonMGraphic(shpFile, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPOINTM:
            eg = createMultiPointMGraphic(shpFile, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPATCH:

        default:

        }

        return (OMGraphic) eg;

    }

    /**
     * Create OMGraphic from next record
     * 
     * @param iStream stream to read from, positioned at next record
     * @param drawingAttributes rendering attributes
     * @param pointRepresentation point rendering representation
     * @param byteTracker keeps track of how many bytes are used for this
     *        record.
     * @return OMGraphic for record
     * @throws IOException
     * @throws FormatException
     */
    protected OMGraphic makeEsriGraphic(LittleEndianInputStream iStream,
                                        DrawingAttributes drawingAttributes,
                                        Object pointRepresentation, ReadByteTracker byteTracker)
            throws IOException, FormatException {
        /*
         * SHAPE_TYPE_NULL = 0; SHAPE_TYPE_POINT = 1; SHAPE_TYPE_ARC = 3;
         * SHAPE_TYPE_POLYLINE = 3; SHAPE_TYPE_POLYGON = 5;
         * SHAPE_TYPE_MULTIPOINT = 8; SHAPE_TYPE_POINTZ = 11;
         * SHAPE_TYPE_POLYLINEZ = 13; SHAPE_TYPE_POLYGONZ = 15;
         * SHAPE_TYPE_MUILTIPOINTZ = 18; SHAPE_TYPE_POINTM = 21;
         * SHAPE_TYPE_POLYLINEM = 23; SHAPE_TYPE_POLYGONM = 25;
         * SHAPE_TYPE_MULTIPOINTM = 28; SHAPE_TYPE_MULTIPATCH = 31;
         */
        EsriGraphic eg = null;

        int shapeType = iStream.readLEInt();
        byteTracker.addRead(4);

        if (verbose) {
            logger.info("reading shape type: " + shapeType + ", "
                    + ShapeUtils.getStringForType(shapeType));
        }

        switch (shapeType) {

        case SHAPE_TYPE_NULL:
            break;
        case SHAPE_TYPE_POINT:
            eg = createPointGraphic(iStream, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYLINE:
            eg = createPolylineGraphic(iStream, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYGON:
            eg = createPolygonGraphic(iStream, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPOINT:
            eg = createMultiPointGraphic(iStream, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POINTZ:
            eg = createPointZGraphic(iStream, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYLINEZ:
            eg = createPolylineZGraphic(iStream, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYGONZ:
            eg = createPolygonZGraphic(iStream, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPOINTZ:
            eg = createMultiPointZGraphic(iStream, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POINTM:
            eg = createPointMGraphic(iStream, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYLINEM:
            eg = createPolylineMGraphic(iStream, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_POLYGONM:
            eg = createPolygonMGraphic(iStream, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPOINTM:
            eg = createMultiPointMGraphic(iStream, pointRepresentation, drawingAttributes, byteTracker);
            break;
        case SHAPE_TYPE_MULTIPATCH:

        default:

        }

        return (OMGraphic) eg;

    }

    // ///////// Point and Multi-Point Shapes

    protected EsriGraphic createPointGraphic(double x, double y, Object representation,
                                             DrawingAttributes drawingAttributes) {

        if (dataTransformation != null) {
            Point2D llp = dataTransformation.inverse(x, y);

            if (verbose) {
                logger.info("point: " + x + ", " + y + " converted to " + llp);
            }

            x = llp.getX();
            y = llp.getY();
        } else if (verbose) {
            logger.info("point: " + x + ", " + y);
        }

        EsriGraphic ret = null;

        if (representation == null) {
            ret = new EsriPoint((float) y, (float) x);
        } else if (representation instanceof ImageIcon) {
            ret = new EsriIconPoint((float) y, (float) x, (ImageIcon) representation);
        } else if (representation instanceof String) {
            ret = new EsriTextPoint((float) y, (float) x, (String) representation, OMText.JUSTIFY_CENTER);
        }

        if (drawingAttributes != null && ret != null) {
            drawingAttributes.setTo((OMGraphic) ret);
        }
        return ret;
    }

    /**
     * Reads the ShapeFile and creates a OMPoint/OMRaster/OMText from the point
     * object.
     * 
     * @param shpFile with the file pointer right after the shape record shape
     *        type bytes. It's assumed that the shape type has been read to
     *        determine that the shapeType for this record is a Point record.
     * @param representation The object to use for representing the Point. If
     *        the object is an ImageIcon, that image is used for a scaling icon
     *        at this point. If it's a String, and OMText will be created for
     *        that Point (center-justified). If it's null, the drawing
     *        attributes values will be used for an OMPoint.
     * @param drawingAttributes the attributes for the OMGraphic.
     * @param byteTracker
     * @return OMPoint or OMScalingRaster or OMText
     * @throws IOException
     */
    protected EsriGraphic createPointGraphic(BinaryFile shpFile, Object representation,
                                             DrawingAttributes drawingAttributes,
                                             ReadByteTracker byteTracker)
            throws IOException, FormatException {

        double x = shpFile.readDouble();
        double y = shpFile.readDouble();
        byteTracker.addRead(2 * 8);
        return createPointGraphic(x, y, representation, drawingAttributes);
    }

    protected EsriGraphic createPointGraphic(LittleEndianInputStream iStream,
                                             Object representation,
                                             DrawingAttributes drawingAttributes,
                                             ReadByteTracker byteTracker)
            throws IOException, FormatException {

        double x = iStream.readLEDouble();
        double y = iStream.readLEDouble();
        byteTracker.addRead(2 * 8);
        return createPointGraphic(x, y, representation, drawingAttributes);
    }

    protected EsriGraphic createMultiPointGraphic(BinaryFile shpFile, Object representation,
                                                  DrawingAttributes drawingAttributes,
                                                  ReadByteTracker byteTracker)
            throws IOException, FormatException {
        // Skip reading the bounding box, 4 doubles
        shpFile.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);
        int numParts = shpFile.readInteger();
        EsriPointList multiPart = new EsriPointList();
        multiPart.setVague(true);

        for (int i = 0; i < numParts; i++) {
            EsriGraphic part = createPointGraphic(shpFile, representation, drawingAttributes, byteTracker);
            if (part != null) {
                multiPart.add((OMGraphic) part);
            }
        }

        return multiPart;
    }

    protected EsriGraphic createMultiPointGraphic(LittleEndianInputStream iStream,
                                                  Object representation,
                                                  DrawingAttributes drawingAttributes,
                                                  ReadByteTracker byteTracker)
            throws IOException, FormatException {
        // Skip reading the bounding box, 4 doubles
        iStream.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);
        int numParts = iStream.readLEInt();
        EsriPointList multiPart = new EsriPointList();
        multiPart.setVague(true);

        for (int i = 0; i < numParts; i++) {
            EsriGraphic part = createPointGraphic(iStream, representation, drawingAttributes, byteTracker);
            if (part != null) {
                multiPart.add((OMGraphic) part);
            }
        }

        return multiPart;
    }

    protected EsriGraphic createPointZGraphic(BinaryFile shpFile, Object representation,
                                              DrawingAttributes drawingAttributes,
                                              ReadByteTracker byteTracker)
            throws IOException, FormatException {
        double x = shpFile.readDouble();
        double y = shpFile.readDouble();
        double z = shpFile.readDouble();
        double m = shpFile.readDouble();
        byteTracker.addRead(4 * 8);

        EsriGraphic ret = createPointGraphic(x, y, representation, drawingAttributes);
        ret.setType(SHAPE_TYPE_POINTZ);
        ((OMGraphic) ret).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, new Double(m));
        ((OMGraphic) ret).putAttribute(ShapeConstants.SHAPE_Z_ATTRIBUTE, new Double(z));
        return ret;
    }

    protected EsriGraphic createPointZGraphic(LittleEndianInputStream iStream,
                                              Object representation,
                                              DrawingAttributes drawingAttributes,
                                              ReadByteTracker byteTracker)
            throws IOException, FormatException {
        double x = iStream.readLEDouble();
        double y = iStream.readLEDouble();
        double z = iStream.readLEDouble();
        double m = iStream.readLEDouble();
        byteTracker.addRead(4 * 8);

        EsriGraphic ret = createPointGraphic(x, y, representation, drawingAttributes);
        ret.setType(SHAPE_TYPE_POINTZ);
        ((OMGraphic) ret).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, new Double(m));
        ((OMGraphic) ret).putAttribute(ShapeConstants.SHAPE_Z_ATTRIBUTE, new Double(z));
        return ret;
    }

    protected EsriGraphic createMultiPointZGraphic(BinaryFile shpFile, Object representation,
                                                   DrawingAttributes drawingAttributes,
                                                   ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic multiPart = createMultiPointGraphic(shpFile, representation, drawingAttributes, byteTracker);

        if (multiPart instanceof EsriGraphicList) {
            ((EsriGraphicList) multiPart).setType(SHAPE_TYPE_MULTIPOINTZ);
            int numPoints = ((EsriGraphicList) multiPart).size();

            double minZ = shpFile.readDouble();
            double maxZ = shpFile.readDouble();
            double[] zs = new double[numPoints];
            for (int i = 0; i < numPoints; i++) {
                zs[i] = shpFile.readDouble();
            }

            ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MIN_Z_ATTRIBUTE, new Double(minZ));
            ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MAX_Z_ATTRIBUTE, new Double(maxZ));
            ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_Z_ATTRIBUTE, zs);

            byteTracker.addRead((2 + numPoints) * 8);

            if (byteTracker.numLeft() > 0) {
                double minM = shpFile.readDouble();
                double maxM = shpFile.readDouble();
                double[] ms = new double[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    ms[i] = shpFile.readDouble();
                }

                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
                byteTracker.addRead((2 + numPoints) * 8);
            }
        }

        return multiPart;
    }

    protected EsriGraphic createMultiPointZGraphic(LittleEndianInputStream iStream,
                                                   Object representation,
                                                   DrawingAttributes drawingAttributes,
                                                   ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic multiPart = createMultiPointGraphic(iStream, representation, drawingAttributes, byteTracker);

        if (multiPart instanceof EsriGraphicList) {
            ((EsriGraphicList) multiPart).setType(SHAPE_TYPE_MULTIPOINTZ);
            int numPoints = ((EsriGraphicList) multiPart).size();

            double minZ = iStream.readLEDouble();
            double maxZ = iStream.readLEDouble();
            double[] zs = new double[numPoints];
            for (int i = 0; i < numPoints; i++) {
                zs[i] = iStream.readLEDouble();
            }

            ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MIN_Z_ATTRIBUTE, new Double(minZ));
            ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MAX_Z_ATTRIBUTE, new Double(maxZ));
            ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_Z_ATTRIBUTE, zs);

            byteTracker.addRead((2 + numPoints) * 8);

            if (byteTracker.numLeft() > 0) {
                double minM = iStream.readLEDouble();
                double maxM = iStream.readLEDouble();
                double[] ms = new double[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    ms[i] = iStream.readLEDouble();
                }

                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
                byteTracker.addRead((2 + numPoints) * 8);
            }
        }

        return multiPart;
    }

    protected EsriGraphic createPointMGraphic(BinaryFile shpFile, Object representation,
                                              DrawingAttributes drawingAttributes,
                                              ReadByteTracker byteTracker)
            throws IOException, FormatException {
        double x = shpFile.readDouble();
        double y = shpFile.readDouble();
        double m = shpFile.readDouble();
        byteTracker.addRead(3 * 8);

        EsriGraphic ret = createPointGraphic(x, y, representation, drawingAttributes);
        ret.setType(SHAPE_TYPE_POINTM);
        ((OMGraphic) ret).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, new Double(m));
        return ret;
    }

    protected EsriGraphic createPointMGraphic(LittleEndianInputStream iStream,
                                              Object representation,
                                              DrawingAttributes drawingAttributes,
                                              ReadByteTracker byteTracker)
            throws IOException, FormatException {
        double x = iStream.readLEDouble();
        double y = iStream.readLEDouble();
        double m = iStream.readLEDouble();
        byteTracker.addRead(3 * 8);

        EsriGraphic ret = createPointGraphic(x, y, representation, drawingAttributes);
        ret.setType(SHAPE_TYPE_POINTM);
        ((OMGraphic) ret).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, new Double(m));
        return ret;
    }

    protected EsriGraphic createMultiPointMGraphic(BinaryFile shpFile, Object representation,
                                                   DrawingAttributes drawingAttributes,
                                                   ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic multiPart = createMultiPointGraphic(shpFile, representation, drawingAttributes, byteTracker);

        if (multiPart instanceof EsriGraphicList) {
            ((EsriGraphicList) multiPart).setType(SHAPE_TYPE_MULTIPOINTM);
            int numPoints = ((EsriGraphicList) multiPart).size();

            if (byteTracker.numLeft() > 0) {
                double minM = shpFile.readDouble();
                double maxM = shpFile.readDouble();
                double[] ms = new double[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    ms[i] = shpFile.readDouble();
                }

                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
                byteTracker.addRead((2 + numPoints) * 8);
            }
        }

        return multiPart;
    }

    protected EsriGraphic createMultiPointMGraphic(LittleEndianInputStream iStream,
                                                   Object representation,
                                                   DrawingAttributes drawingAttributes,
                                                   ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic multiPart = createMultiPointGraphic(iStream, representation, drawingAttributes, byteTracker);

        if (multiPart instanceof EsriGraphicList) {
            ((EsriGraphicList) multiPart).setType(SHAPE_TYPE_MULTIPOINTM);
            int numPoints = ((EsriGraphicList) multiPart).size();

            if (byteTracker.numLeft() > 0) {
                double minM = iStream.readLEDouble();
                double maxM = iStream.readLEDouble();
                double[] ms = new double[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    ms[i] = iStream.readLEDouble();
                }

                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                ((OMGraphic) multiPart).putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
                byteTracker.addRead((2 + numPoints) * 8);
            }
        }

        return multiPart;
    }

    // ///////// Polygon Shapes

    protected EsriGraphic createPolygonGraphic(BinaryFile shpFile,
                                               DrawingAttributes drawingAttributes,
                                               ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyGraphic(shpFile, SHAPE_TYPE_POLYGON, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolygonGraphic(LittleEndianInputStream iStream,
                                               DrawingAttributes drawingAttributes,
                                               ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyGraphic(iStream, SHAPE_TYPE_POLYGON, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolylineGraphic(BinaryFile shpFile,
                                                DrawingAttributes drawingAttributes,
                                                ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyGraphic(shpFile, SHAPE_TYPE_POLYLINE, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolylineGraphic(LittleEndianInputStream iStream,
                                                DrawingAttributes drawingAttributes,
                                                ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyGraphic(iStream, SHAPE_TYPE_POLYLINE, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolyGraphic(BinaryFile shpFile, int shapeType,
                                            DrawingAttributes drawingAttributes,
                                            ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic ret = null;

        // Skip reading the bounding box, 4 doubles
        shpFile.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);

        int numParts = shpFile.readInteger();
        int numPoints = shpFile.readInteger();
        byteTracker.addRead(2 * 4);

        if (numParts > 0) {
            ret = getPolys(shpFile, numParts, numPoints, shapeType, drawingAttributes, byteTracker);
        }
        return ret;
    }

    protected EsriGraphic createPolyGraphic(LittleEndianInputStream iStream, int shapeType,
                                            DrawingAttributes drawingAttributes,
                                            ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic ret = null;

        // Skip reading the bounding box, 4 doubles
        iStream.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);

        int numParts = iStream.readLEInt();
        int numPoints = iStream.readLEInt();
        byteTracker.addRead(2 * 4);

        if (numParts > 0) {
            ret = getPolys(iStream, numParts, numPoints, shapeType, drawingAttributes, byteTracker);
        }
        return ret;
    }

    protected EsriGraphic createPolygonZGraphic(BinaryFile shpFile,
                                                DrawingAttributes drawingAttributes,
                                                ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(shpFile, SHAPE_TYPE_POLYGONZ, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolygonZGraphic(LittleEndianInputStream iStream,
                                                DrawingAttributes drawingAttributes,
                                                ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(iStream, SHAPE_TYPE_POLYGONZ, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolylineZGraphic(BinaryFile shpFile,
                                                 DrawingAttributes drawingAttributes,
                                                 ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(shpFile, SHAPE_TYPE_POLYLINEZ, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolylineZGraphic(LittleEndianInputStream iStream,
                                                 DrawingAttributes drawingAttributes,
                                                 ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(iStream, SHAPE_TYPE_POLYLINEZ, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolyZGraphic(BinaryFile shpFile, int shapeType,
                                             DrawingAttributes drawingAttributes,
                                             ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic ret = null;

        // Skip reading the bounding box, 4 doubles
        shpFile.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);

        int numParts = shpFile.readInteger();
        int numPoints = shpFile.readInteger();
        byteTracker.addRead(2 * 4);

        if (numParts > 0) {
            ret = getPolys(shpFile, numParts, numPoints, shapeType, drawingAttributes, byteTracker);
        }

        double minZ = shpFile.readDouble();
        double maxZ = shpFile.readDouble();
        double[] zs = new double[numPoints];
        for (int i = 0; i < numPoints; i++) {
            zs[i] = shpFile.readDouble();
        }

        OMGraphic omg = (OMGraphic) ret;
        if (omg != null) {
            omg.putAttribute(ShapeConstants.SHAPE_MIN_Z_ATTRIBUTE, new Double(minZ));
            omg.putAttribute(ShapeConstants.SHAPE_MAX_Z_ATTRIBUTE, new Double(maxZ));
            omg.putAttribute(ShapeConstants.SHAPE_Z_ATTRIBUTE, zs);
        }
        byteTracker.addRead((2 + numPoints) * 8);

        if (byteTracker.numLeft() > 0) {
            double minM = shpFile.readDouble();
            double maxM = shpFile.readDouble();
            double[] ms = new double[numPoints];
            for (int i = 0; i < numPoints; i++) {
                ms[i] = shpFile.readDouble();
            }

            if (omg != null) {
                omg.putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                omg.putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                omg.putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
                byteTracker.addRead((2 + numPoints) * 8);
            }
        }

        return ret;
    }

    protected EsriGraphic createPolyZGraphic(LittleEndianInputStream iStream, int shapeType,
                                             DrawingAttributes drawingAttributes,
                                             ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic ret = null;

        // Skip reading the bounding box, 4 doubles
        iStream.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);

        int numParts = iStream.readLEInt();
        int numPoints = iStream.readLEInt();
        byteTracker.addRead(2 * 4);

        if (numParts > 0) {
            ret = getPolys(iStream, numParts, numPoints, shapeType, drawingAttributes, byteTracker);
        }

        double minZ = iStream.readLEDouble();
        double maxZ = iStream.readLEDouble();
        double[] zs = new double[numPoints];
        for (int i = 0; i < numPoints; i++) {
            zs[i] = iStream.readLEDouble();
        }

        OMGraphic omg = (OMGraphic) ret;
        if (omg != null) {
            omg.putAttribute(ShapeConstants.SHAPE_MIN_Z_ATTRIBUTE, new Double(minZ));
            omg.putAttribute(ShapeConstants.SHAPE_MAX_Z_ATTRIBUTE, new Double(maxZ));
            omg.putAttribute(ShapeConstants.SHAPE_Z_ATTRIBUTE, zs);
        }
        byteTracker.addRead((2 + numPoints) * 8);

        if (byteTracker.numLeft() > 0) {
            double minM = iStream.readLEDouble();
            double maxM = iStream.readLEDouble();
            double[] ms = new double[numPoints];
            for (int i = 0; i < numPoints; i++) {
                ms[i] = iStream.readLEDouble();
            }

            if (omg != null) {
                omg.putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                omg.putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                omg.putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
            }
            byteTracker.addRead((2 + numPoints) * 8);
        }

        return ret;
    }

    protected EsriGraphic createPolygonMGraphic(BinaryFile shpFile,
                                                DrawingAttributes drawingAttributes,
                                                ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(shpFile, SHAPE_TYPE_POLYGONM, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolygonMGraphic(LittleEndianInputStream iStream,
                                                DrawingAttributes drawingAttributes,
                                                ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(iStream, SHAPE_TYPE_POLYGONM, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolylineMGraphic(BinaryFile shpFile,
                                                 DrawingAttributes drawingAttributes,
                                                 ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(shpFile, SHAPE_TYPE_POLYLINEM, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolylineMGraphic(LittleEndianInputStream iStream,
                                                 DrawingAttributes drawingAttributes,
                                                 ReadByteTracker byteTracker)
            throws IOException, FormatException {
        return createPolyZGraphic(iStream, SHAPE_TYPE_POLYLINEM, drawingAttributes, byteTracker);
    }

    protected EsriGraphic createPolyMGraphic(BinaryFile shpFile, int shapeType,
                                             DrawingAttributes drawingAttributes,
                                             ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic ret = null;

        // Skip reading the bounding box, 4 doubles
        shpFile.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);

        int numParts = shpFile.readInteger();
        int numPoints = shpFile.readInteger();
        byteTracker.addRead(2 * 4);

        if (numParts > 0) {
            ret = getPolys(shpFile, numParts, numPoints, shapeType, drawingAttributes, byteTracker);

            if (byteTracker.numLeft() > 0) {
                double minM = shpFile.readDouble();
                double maxM = shpFile.readDouble();
                double[] ms = new double[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    ms[i] = shpFile.readDouble();
                }

                OMGraphic omg = (OMGraphic) ret;
                omg.putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                omg.putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                omg.putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
                byteTracker.addRead((2 + numPoints) * 8);
            }
        }
        return ret;
    }

    protected EsriGraphic createPolyMGraphic(LittleEndianInputStream iStream, int shapeType,
                                             DrawingAttributes drawingAttributes,
                                             ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic ret = null;

        // Skip reading the bounding box, 4 doubles
        iStream.skipBytes(4 * 8);
        byteTracker.addRead(4 * 8);

        int numParts = iStream.readLEInt();
        int numPoints = iStream.readLEInt();
        byteTracker.addRead(2 * 4);

        if (numParts > 0) {
            ret = getPolys(iStream, numParts, numPoints, shapeType, drawingAttributes, byteTracker);

            if (byteTracker.numLeft() > 0) {
                double minM = iStream.readLEDouble();
                double maxM = iStream.readLEDouble();
                double[] ms = new double[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    ms[i] = iStream.readLEDouble();
                }

                OMGraphic omg = (OMGraphic) ret;
                omg.putAttribute(ShapeConstants.SHAPE_MIN_MEASURE_ATTRIBUTE, new Double(minM));
                omg.putAttribute(ShapeConstants.SHAPE_MAX_MEASURE_ATTRIBUTE, new Double(maxM));
                omg.putAttribute(ShapeConstants.SHAPE_MEASURE_ATTRIBUTE, ms);
                byteTracker.addRead((2 + numPoints) * 8);
            }
        }
        return ret;
    }

    protected EsriGraphic getPolys(BinaryFile shpFile, int numParts, int numPoints, int shapeType,
                                   DrawingAttributes drawingAttributes, ReadByteTracker byteTracker)
            throws IOException, FormatException {
        EsriGraphic ret = null;
        if (verbose) {
            logger.info("creating polygon for entry, parts(" + numParts + ") numPoints("
                    + numPoints + ")");
        }
        if (numParts > 1) {
            ret = createEsriGraphicList(shapeType);
            if (ret != null) {
                ((EsriGraphicList) ret).setVague(true);
            }
        }

        int[] parts = new int[numParts];
        for (int i = 0; i < numParts; i++) {
            parts[i] = shpFile.readInteger();
        }
        byteTracker.addRead(numParts * 4);

        int origin = parts[0];
        int length = 0;
        double[] coords;
        for (int i = 1; i < numParts; i++) {
            int nextOrigin = parts[i];
            length = nextOrigin - origin;

            coords = getCoords(shpFile, length, isPolygon(shapeType), dataTransformation, byteTracker);

            if (verbose) {
                logger.info("creating " + ShapeUtils.getStringForType(shapeType) + "(" + i
                        + ") with coords[" + getCoordString(coords) + "]");
            }

            EsriGraphic omp = createEsriPoly(shapeType, coords, lineType, drawingAttributes);
            if (ret != null) {
                ((EsriGraphicList) ret).add((OMGraphic) omp);
            }

            origin = nextOrigin;
        }

        length = numPoints - origin;

        coords = getCoords(shpFile, length, isPolygon(shapeType), dataTransformation, byteTracker);

        if (verbose) {
            logger.info("creating " + ShapeUtils.getStringForType(shapeType) + " with coords["
                    + getCoordString(coords) + "]");
        }

        EsriGraphic omp = createEsriPoly(shapeType, coords, lineType, drawingAttributes);

        if (ret != null) {
            ((EsriGraphicList) ret).add((OMGraphic) omp);
        } else {
            ret = (EsriGraphic) omp;
        }
        return ret;
    }

    protected String getCoordString(double[] coords) {
        StringBuffer coordString = new StringBuffer();
        for (int j = 0; j < coords.length; j += 2) {
            coordString.append((j > 0 ? ":" : "")).append(coords[j]).append(",").append(coords[j + 1]);
        }
        return coordString.toString();
    }

    protected EsriGraphic getPolys(LittleEndianInputStream iStream, int numParts, int numPoints,
                                   int shapeType, DrawingAttributes drawingAttributes,
                                   ReadByteTracker byteTracker) throws IOException, FormatException {
        EsriGraphic ret = null;

        if (numParts > 1) {
            ret = createEsriGraphicList(shapeType);
            if (ret != null) {
                ((EsriGraphicList) ret).setVague(true);
            }
        }

        int[] parts = new int[numParts];
        for (int i = 0; i < numParts; i++) {
            parts[i] = iStream.readLEInt();
        }
        byteTracker.addRead(numParts * 4);

        int origin = parts[0];
        int length = 0;
        double[] coords;
        for (int i = 1; i < numParts; i++) {
            int nextOrigin = parts[i];
            length = nextOrigin - origin;

            coords = getCoords(iStream, length, isPolygon(shapeType), dataTransformation, byteTracker);

            if (verbose) {
                logger.info("creating " + ShapeUtils.getStringForType(shapeType) + "(" + i
                        + ") with coords[" + getCoordString(coords) + "]");
            }

            EsriGraphic omp = createEsriPoly(shapeType, coords, lineType, drawingAttributes);
            if (ret != null) {
                ((EsriGraphicList) ret).add((OMGraphic) omp);
            }

            origin = nextOrigin;
        }

        length = numPoints - origin;

        coords = getCoords(iStream, length, isPolygon(shapeType), dataTransformation, byteTracker);

        if (verbose) {
            logger.info("creating " + ShapeUtils.getStringForType(shapeType) + " with coords["
                    + getCoordString(coords) + "]");
        }

        EsriGraphic omp = createEsriPoly(shapeType, coords, lineType, drawingAttributes);

        if (ret != null) {
            ((EsriGraphicList) ret).add((OMGraphic) omp);
        } else {
            ret = (EsriGraphic) omp;
        }
        return ret;
    }

    protected float[] getFloatCoords(BinaryFile shpFile, int length, boolean isPolygon,
                                     GeoCoordTransformation dataTrans, ReadByteTracker bitTracker)
            throws IOException, FormatException {

        float[] coords = new float[isPolygon ? length * 2 + 2 : length * 2];
        int j = 0;

        // Create the llp here and reuse it for coordinate transformations.
        LatLonPoint llp = null;
        if (dataTrans != null) {
            llp = new LatLonPoint.Double();
        }

        for (j = 0; j < length; j++) {
            double x = shpFile.readDouble();
            double y = shpFile.readDouble();
            bitTracker.addRead(2 * 8);

            if (dataTrans != null) {
                llp = dataTrans.inverse(x, y, llp);
                x = llp.getRadLon();
                y = llp.getRadLat();
            } else {
                x = ProjMath.degToRad(x);
                y = ProjMath.degToRad(y);
            }

            coords[j * 2] = (float) y;
            coords[j * 2 + 1] = (float) x;
        }

        if (isPolygon) {
            coords[j * 2] = coords[0];
            coords[j * 2 + 1] = coords[1];
        }
        return coords;
    }

    protected float[] getFloatCoords(LittleEndianInputStream iStream, int length,
                                     boolean isPolygon, GeoCoordTransformation dataTrans,
                                     ReadByteTracker bitTracker)
            throws IOException, FormatException {

        float[] coords = new float[isPolygon ? length * 2 + 2 : length * 2];
        int j = 0;

        // Create the llp here and reuse it for coordinate transformations.
        LatLonPoint llp = null;
        if (dataTrans != null) {
            llp = new LatLonPoint.Double();
        }

        for (j = 0; j < length; j++) {
            double x = iStream.readLEDouble();
            double y = iStream.readLEDouble();
            bitTracker.addRead(2 * 8);

            if (dataTrans != null) {
                llp = dataTrans.inverse(x, y, llp);
                x = llp.getRadLon();
                y = llp.getRadLat();
            } else {
                x = ProjMath.degToRad(x);
                y = ProjMath.degToRad(y);
            }

            coords[j * 2] = (float) y;
            coords[j * 2 + 1] = (float) x;
        }

        if (isPolygon) {
            coords[j * 2] = coords[0];
            coords[j * 2 + 1] = coords[1];
        }
        return coords;
    }

    protected double[] getCoords(BinaryFile shpFile, int length, boolean isPolygon,
                                 GeoCoordTransformation dataTrans, ReadByteTracker bitTracker)
            throws IOException, FormatException {

        double[] coords = new double[isPolygon ? length * 2 + 2 : length * 2];
        int j = 0;

        // Create the llp here and reuse it for coordinate transformations.
        LatLonPoint llp = null;
        if (dataTrans != null) {
            llp = new LatLonPoint.Double();
        }

        for (j = 0; j < length; j++) {
            double x = shpFile.readDouble();
            double y = shpFile.readDouble();
            bitTracker.addRead(2 * 8);

            if (dataTrans != null) {
                llp = dataTrans.inverse(x, y, llp);
                x = llp.getRadLon();
                y = llp.getRadLat();
            } else {
                x = ProjMath.degToRad(x);
                y = ProjMath.degToRad(y);
            }

            coords[j * 2] = y;
            coords[j * 2 + 1] = x;
        }

        if (isPolygon) {
            coords[j * 2] = coords[0];
            coords[j * 2 + 1] = coords[1];
        }
        return coords;
    }

    protected double[] getCoords(LittleEndianInputStream iStream, int length, boolean isPolygon,
                                 GeoCoordTransformation dataTrans, ReadByteTracker bitTracker)
            throws IOException, FormatException {

        double[] coords = new double[isPolygon ? length * 2 + 2 : length * 2];
        int j = 0;

        // Create the llp here and reuse it for coordinate transformations.
        LatLonPoint llp = null;
        if (dataTrans != null) {
            llp = new LatLonPoint.Double();
        }

        for (j = 0; j < length; j++) {
            double x = iStream.readLEDouble();
            double y = iStream.readLEDouble();
            bitTracker.addRead(2 * 8);

            if (dataTrans != null) {
                llp = dataTrans.inverse(x, y, llp);
                x = llp.getRadLon();
                y = llp.getRadLat();
            } else {
                x = ProjMath.degToRad(x);
                y = ProjMath.degToRad(y);
            }

            coords[j * 2] = y;
            coords[j * 2 + 1] = x;
        }

        if (isPolygon) {
            coords[j * 2] = coords[0];
            coords[j * 2 + 1] = coords[1];
        }
        return coords;
    }

    public static boolean isPolygon(int shapeType) {
        return shapeType == SHAPE_TYPE_POLYGON || shapeType == SHAPE_TYPE_POLYGONZ
                || shapeType == SHAPE_TYPE_POLYGONM;
    }

    public static EsriGraphic createEsriPoly(int shapeType, double[] coords, int lineType,
                                             DrawingAttributes da) {

        if (da == null) {
            da = DrawingAttributes.DEFAULT;
        }

        EsriGraphic ret = null;
        switch (shapeType) {
        case SHAPE_TYPE_POLYGON:
            ret = new EsriPolygon(coords, OMPoly.RADIANS, lineType);
            da.setTo((OMGraphic) ret);
            break;
        case SHAPE_TYPE_POLYLINE:
            ret = new EsriPolyline(coords, OMPoly.RADIANS, lineType);
            da.setTo((OMGraphic) ret);
            ((OMGraphic) ret).setFillPaint(OMColor.clear);
            break;
        case SHAPE_TYPE_POLYGONM:
            ret = new EsriPolygonM(coords, OMPoly.RADIANS, lineType);
            da.setTo((OMGraphic) ret);
            break;
        case SHAPE_TYPE_POLYGONZ:
            ret = new EsriPolygonZ(coords, OMPoly.RADIANS, lineType);
            da.setTo((OMGraphic) ret);
            break;
        case SHAPE_TYPE_POLYLINEM:
            ret = new EsriPolylineM(coords, OMPoly.RADIANS, lineType);
            da.setTo((OMGraphic) ret);
            ((OMGraphic) ret).setFillPaint(OMColor.clear);
            break;
        case SHAPE_TYPE_POLYLINEZ:
            ret = new EsriPolylineZ(coords, OMPoly.RADIANS, lineType);
            da.setTo((OMGraphic) ret);
            ((OMGraphic) ret).setFillPaint(OMColor.clear);
            break;
        }
        return ret;
    }

    public static EsriGraphicList createEsriGraphicList(int shapeType) {
        EsriGraphicList ret = null;
        switch (shapeType) {
        case SHAPE_TYPE_NULL:
            break;
        case SHAPE_TYPE_POINT:
        case SHAPE_TYPE_MULTIPOINT:
        case SHAPE_TYPE_POINTZ:
        case SHAPE_TYPE_MULTIPOINTZ:
        case SHAPE_TYPE_POINTM:
        case SHAPE_TYPE_MULTIPOINTM:
            ret = new EsriPointList();
            ret.setType(shapeType);
            break;
        case SHAPE_TYPE_POLYGON:
            ret = new EsriPolygonList();
            break;
        case SHAPE_TYPE_POLYLINE:
            ret = new EsriPolylineList();
            break;
        case SHAPE_TYPE_POLYGONM:
            ret = new EsriPolygonMList();
            break;
        case SHAPE_TYPE_POLYGONZ:
            ret = new EsriPolygonZList();
            break;
        case SHAPE_TYPE_POLYLINEM:
            ret = new EsriPolylineMList();
            break;
        case SHAPE_TYPE_POLYLINEZ:
            ret = new EsriPolylineZList();
            break;
        }
        return ret;
    }

    public GeoCoordTransformation getDataCoordTransformation() {
        return dataTransformation;
    }

    public void setDataCoordTransformation(GeoCoordTransformation dataTrans) {
        this.dataTransformation = dataTrans;
    }

    public int getLineType() {
        return lineType;
    }

    public void setLineType(int lineType) {
        this.lineType = lineType;
    }

    public Class<?> getPrecision() {
        return precision;
    }

    public void setPrecision(Class<?> precision) {
        this.precision = precision;
    }

    public static class ReadByteTracker {
        int totalCount;
        int currentCount;

        public ReadByteTracker() {

        }

        public ReadByteTracker(int tc) {
            totalCount = tc;
        }

        public int numLeft() {
            return totalCount - currentCount;
        }

        public int addRead(int num) {
            currentCount += num;
            return currentCount;
        }

        public void reset(int newTotal) {
            totalCount = newTotal;
            currentCount = 0;
        }

        public String toString() {
            return "ReadByteTracker has noted " + currentCount + " of " + totalCount
                    + " bytes read";
        }
    }

    public static class Header {
        public int fileCode;
        public int fileLength;
        public int version;
        public int shapeType;
        public double xMin;
        public double yMin;
        public double xMax;
        public double yMax;
        public double zMin;
        public double zMax;
        public double mMin;
        public double mMax;

        public Header(BinaryFile shp) throws IOException, FormatException {
            this(shp, null);
        }

        public Header(BinaryFile shp, GeoCoordTransformation dataTransformation)
                throws IOException, FormatException {
            shp.byteOrder(true);
            shp.seek(0);
            fileCode = shp.readInteger();
            shp.skipBytes(20); // unused
            fileLength = shp.readInteger() * 2;
            shp.byteOrder(false);
            version = shp.readInteger();
            shapeType = shp.readInteger();
            xMin = shp.readDouble();
            yMin = shp.readDouble();
            xMax = shp.readDouble();
            yMax = shp.readDouble();
            zMin = shp.readDouble();
            zMax = shp.readDouble();
            mMin = shp.readDouble();
            mMax = shp.readDouble();

            if (dataTransformation != null) {
                LatLonPoint llpmin = dataTransformation.inverse(xMin, yMin);
                xMin = llpmin.getLongitude();
                yMin = llpmin.getLatitude();
                LatLonPoint llpmax = dataTransformation.inverse(xMax, yMax);
                xMax = llpmax.getLongitude();
                yMax = llpmax.getLatitude();
            }
        }

        public Header(LittleEndianInputStream iStream) throws IOException {
            this(iStream, null);
        }

        public Header(LittleEndianInputStream iStream, GeoCoordTransformation dataTransformation)
                throws IOException {
            fileCode = iStream.readInt();
            iStream.skipBytes(20); // unused
            fileLength = iStream.readInt() * 2;
            version = iStream.readLEInt();
            shapeType = iStream.readLEInt();
            xMin = iStream.readLEDouble();
            yMin = iStream.readLEDouble();
            xMax = iStream.readLEDouble();
            yMax = iStream.readLEDouble();
            zMin = iStream.readLEDouble();
            zMax = iStream.readLEDouble();
            mMin = iStream.readLEDouble();
            mMax = iStream.readLEDouble();

            if (dataTransformation != null) {
                LatLonPoint llpmin = dataTransformation.inverse(xMin, yMin);
                xMin = llpmin.getLongitude();
                yMin = llpmin.getLatitude();
                LatLonPoint llpmax = dataTransformation.inverse(xMax, yMax);
                xMax = llpmax.getLongitude();
                yMax = llpmax.getLatitude();
            }
        }

        public DataBounds getDataBounds() {
            return new DataBounds(xMin, yMin, xMax, yMax);
        }

        public String toString() {
            return "header[fc=" + fileCode + ",len=" + fileLength + ",ver=" + version + ",type="
                    + shapeType + "]";
        }
    }
}
