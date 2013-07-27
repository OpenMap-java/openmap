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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/SpatialIndex.java,v $
// $RCSfile: SpatialIndex.java,v $
// $Revision: 1.19 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.bbn.openmap.dataAccess.shape.DbfHandler;
import com.bbn.openmap.dataAccess.shape.EsriGraphicFactory;
import com.bbn.openmap.dataAccess.shape.ShapeUtils;
import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.PropUtils;

/**
 * A Spatial Index is a variation on a Shape Index, adding the bounding box of
 * the shape to the index.
 * <p>
 * The file has a 100 byte header identical to a Shape Index followed by <i>n
 * </i> records.
 * <p>
 * The record layout of the spatial index is as follows:
 * <p>
 * <TABLE BORDER COLS=5 WIDTH="100%" >
 * <TR>
 * <TD ALIGN=CENTER><b><i>Position </i> </b></TD>
 * <TD ALIGN=CENTER><b><i>Field </i> </b></TD>
 * <TD ALIGN=CENTER><b><i>Value </i> </b></TD>
 * <TD ALIGN=CENTER><b><i>Type </i> </b></TD>
 * <TD ALIGN=CENTER><b><i>Byte Order </i> </b></TD>
 * </TR>
 * <TR>
 * <TD ALIGN=CENTER>Byte 0</TD>
 * <TD ALIGN=CENTER>Offset</TD>
 * <TD ALIGN=CENTER>Offset</TD>
 * <TD ALIGN=CENTER>Integer</TD>
 * <TD ALIGN=CENTER>Big</TD>
 * </TR>
 * <TR>
 * <TD ALIGN=CENTER>Byte 4</TD>
 * <TD ALIGN=CENTER>Content Length</TD>
 * <TD ALIGN=CENTER>Content Length</TD>
 * <TD ALIGN=CENTER>Integer</TD>
 * <TD ALIGN=CENTER>Big</TD>
 * </TR>
 * <TR>
 * <TD ALIGN=CENTER>Byte 8</TD>
 * <TD ALIGN=CENTER>Bounding Box</TD>
 * <TD ALIGN=CENTER>Xmin</TD>
 * <TD ALIGN=CENTER>Double</TD>
 * <TD ALIGN=CENTER>Little</TD>
 * </TR>
 * <TR>
 * <TD ALIGN=CENTER>Byte 16</TD>
 * <TD ALIGN=CENTER>Bounding Box</TD>
 * <TD ALIGN=CENTER>Ymin</TD>
 * <TD ALIGN=CENTER>Double</TD>
 * <TD ALIGN=CENTER>Little</TD>
 * </TR>
 * <TR>
 * <TD ALIGN=CENTER>Byte 24</TD>
 * <TD ALIGN=CENTER>Bounding Box</TD>
 * <TD ALIGN=CENTER>Xmax</TD>
 * <TD ALIGN=CENTER>Double</TD>
 * <TD ALIGN=CENTER>Little</TD>
 * </TR>
 * <TR>
 * <TD ALIGN=CENTER>Byte 32</TD>
 * <TD ALIGN=CENTER>Bounding Box</TD>
 * <TD ALIGN=CENTER>Ymax</TD>
 * <TD ALIGN=CENTER>Double</TD>
 * <TD ALIGN=CENTER>Little</TD>
 * </TR>
 * </TABLE>
 * 
 * <H2>Usage</H2>
 * <DT>java com.bbn.openmap.layer.shape.SpatialIndex -d file.ssx</DT>
 * <DD><i>Dumps spatial index information, excluding bounding boxes to stdout.
 * Useful for comparing to a shape index. </i></DD>
 * <p>
 * <DT>java com.bbn.openmap.layer.shape.SpatialIndex -d -b file.ssx</DT>
 * <DD><i>Dumps spatial index information including bounding boxes to stdout.
 * </i></DD>
 * <p>
 * <DT>java com.bbn.openmap.layer.shape.SpatialIndex -c file.shp</DT>
 * <DD><i>Creates spatial index <code>file.ssx</code> from shape file
 * <code>file.shp</code>. </i></DD>
 * <p>
 * 
 * <H2>Notes</H2>
 * When reading the Shape file, the content length is the length of the record's
 * contents, exclusive of the record header (8 bytes). So the size that we need
 * to read in from the Shape file is actually denoted as ((contentLength * 2) +
 * 8). This converts from 16bit units to 8 bit bytes and adds the 8 bytes for
 * the record header.
 * 
 * <H2>To Do</H2>
 * <UL>
 * <LI>index arcs</LI>
 * <LI>index multipoints</LI>
 * </UL>
 * 
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.19 $ $Date: 2009/02/25 22:34:04 $
 * @see ShapeIndex
 */
public class SpatialIndex extends ShapeUtils {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.shape.SpatialIndex");

    /** Size of a shape file header in bytes. */
    public final static int SHAPE_FILE_HEADER_LENGTH = 100;

    /** Size of a shape file record header in bytes. */
    public final static int SHAPE_RECORD_HEADER_LENGTH = 8;

    /** Size of the spatial index header in bytes. */
    public final static int SPATIAL_INDEX_HEADER_LENGTH = 100;

    /** Size of the spatial index record in bytes. */
    public final static int SPATIAL_INDEX_RECORD_LENGTH = 40;

    /** Default size for shape record buffer. */
    public final static int DEFAULT_SHAPE_RECORD_SIZE = 50000;

    /** The shape file. */
    protected BinaryFile shp;

    /**
     * The handler for dbf file information.
     */
    protected DbfHandler dbf;

    /** The icon to use for point objects. */
    protected ImageIcon pointIcon;

    /** The bounds of all the shapes in the shape file. */
    protected ESRIBoundingBox bounds = null;

    /**
     * The file name for the shape file, for opening/reopening.
     */
    protected String shpFileName;

    /**
     * A cached list of the SpatialIndex file entries, for repeated reference.
     */
    protected List<Entry> entries;

    /**
     * A factory object to use to create OMGraphics from the shp file.
     */
    EsriGraphicFactory factory = new EsriGraphicFactory();

    /**
     * Opens a spatial index file for reading based on the location of the
     * provided shp file.
     * 
     * @param shpFilename the name of the spatial index file
     * @exception IOException if something goes wrong opening the file
     */
    public SpatialIndex(String shpFilename) throws IOException {
        this.shpFileName = shpFilename;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SpatialIndex(" + shpFilename + ");");
        }
    }

    /**
     * Opens a spatial index file and it's associated shape file.
     * 
     * @param ssxFilename the name of the spatial index file
     * @param shpFilename the name of the shape file
     * @exception IOException if something goes wrong opening the files
     * @deprecated ssx file is figured based on the shp file path
     */
    public SpatialIndex(String ssxFilename, String shpFilename) throws IOException {
        this(shpFilename);
    }

    /**
     * Figures out the ssx file name from the shp file name.
     * 
     * @param shpFileName
     * @return ssx file name from shape file name
     */
    public static String ssx(String shpFileName) {
        String ret = null;
        if (shpFileName != null) {
            ret = shpFileName.substring(0, shpFileName.indexOf(".shp")) + ".ssx";
        }
        return ret;
    }

    /**
     * Figures out the dbf file name from the shp file name.
     * 
     * @param shpFileName
     * @return dbf file name created from shp file name.
     */
    public static String dbf(String shpFileName) {
        String ret = null;
        if (shpFileName != null) {
            ret = shpFileName.substring(0, shpFileName.indexOf(".shp")) + ".dbf";
        }
        return ret;
    }

    /**
     * Get the box boundary containing all the shapes.
     */
    public ESRIBoundingBox getBounds() {
        return getBounds(null);
    }

    /**
     * Method that can be overridden to adjust how the BinaryFile is created for
     * shp files.
     * 
     * @param shapeFileName The path to shape file, absolute, relative or URL
     * @return BinaryFile
     * @throws IOException if the shapeFileName can't be found.
     */
    protected BinaryFile getShpFile(String shapeFileName) throws IOException {
        return new BinaryBufferedFile(shapeFileName);
    }

    /**
     * Returns the bounds of the shape file. If bounds don't exist, they are
     * read from the shape file header.
     * 
     * @param coordTransform
     * @return null if bounds can't been read, otherwise ESRIBoundingBox.
     */
    public ESRIBoundingBox getBounds(GeoCoordTransformation coordTransform) {
        if (bounds == null) {
            try {
                if (shpFileName != null) {
                    BinaryFile shpFile = getShpFile(shpFileName);
                    EsriGraphicFactory.Header header = new EsriGraphicFactory.Header(shpFile, coordTransform);
                    DataBounds dataBounds = header.getDataBounds();
                    if (dataBounds != null) {
                        Point2D min = dataBounds.getMin();
                        Point2D max = dataBounds.getMax();
                        bounds = new ESRIBoundingBox(min.getX(), min.getY());
                        bounds.addPoint(max.getX(), max.getY());
                    }
                    shpFile.close();
                }
            } catch (IOException ioe) {
                bounds = null;
            } catch (FormatException fe) {
                bounds = null;
            }
        }
        return bounds;
    }

    /**
     * Reset the bounds so they will be recalculated the next time a file is
     * read.
     */
    public void resetBounds() {
        bounds = null;
    }

    /**
     * Creates a record instance from the shape file data. Calls the appropriate
     * record constructor based on the shapeType, and passes the buffer and
     * offset to that constructor.
     * 
     * @param shapeType the shape file's shape type, enumerated in
     *        <code>ShapeUtils</code>
     * @param b the buffer pointing to the raw record data
     * @param off the offset of the data starting point in the buffer
     * @exception IOException if something goes wrong reading the file
     * @see ShapeUtils
     */
    public ESRIRecord makeESRIRecord(int shapeType, byte[] b, int off) throws IOException {
        switch (shapeType) {
        case SHAPE_TYPE_NULL:
            return null;
        case SHAPE_TYPE_POINT:
            // return new ESRIPointRecord(b, off);
            return new ESRIPointRecord(b, off, pointIcon);
        case SHAPE_TYPE_POLYGON:
        case SHAPE_TYPE_ARC:
            // case SHAPE_TYPE_POLYLINE:
            return new ESRIPolygonRecord(b, off);
        case SHAPE_TYPE_MULTIPOINT:
            logger.fine("SpatialIndex.makeESRIRecord: Arc NYI");
            return null;
            // return new ESRIMultipointRecord(b, off);
        default:
            return null;
        }
    }

    /**
     * Locates records in the shape file that intersect with the given
     * rectangle. The spatial index is searched for intersections and the
     * appropriate records are read from the shape file. Not really used
     * anymore, except for old code. Use EsriGraphicList or EsriGraphicFactory
     * instead.
     * 
     * @param xmin the smaller of the x coordinates
     * @param ymin the smaller of the y coordinates
     * @param xmax the larger of the x coordinates
     * @param ymax the larger of the y coordinates
     * @return an array of records that intersect the given rectangle
     * @exception IOException if something goes wrong reading the files
     */
    public ESRIRecord[] locateRecords(double xmin, double ymin, double xmax, double ymax)
            throws IOException, FormatException {

        boolean gatherBounds = false;

        if (bounds == null) {
            bounds = new ESRIBoundingBox();
            gatherBounds = true;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("locateRecords:\n\txmin: " + xmin + "; ymin: " + ymin + "\n\txmax: " + xmax
                    + "; ymax: " + ymax);
        }

        byte ixRecord[] = new byte[SPATIAL_INDEX_RECORD_LENGTH];
        int recNum = 0;
        Vector<ESRIRecord> v = new Vector<ESRIRecord>();
        int sRecordSize = DEFAULT_SHAPE_RECORD_SIZE;
        byte sRecord[] = new byte[sRecordSize];

        if (shpFileName == null) {
            return null;
        }

        BinaryBufferedFile ssx = new BinaryBufferedFile(ssx(shpFileName));
        if (shp == null) {
            shp = getShpFile(shpFileName);
        }

        // Need to figure out what the shape type is...
        ssx.seek(32);

        // int shapeType = readLEInt(ssx);
        // /
        ssx.byteOrder(false);
        int shapeType = ssx.readInteger();
        // /
        ssx.seek(100); // skip the file header

        while (true) {
            int result = ssx.read(ixRecord, 0, SPATIAL_INDEX_RECORD_LENGTH);
            // if (result == -1) {
            if (result <= 0) {
                break;// EOF
            } else {
                recNum++;
                double xmin2 = readLEDouble(ixRecord, 8);
                double ymin2 = readLEDouble(ixRecord, 16);
                double xmax2 = readLEDouble(ixRecord, 24);
                double ymax2 = readLEDouble(ixRecord, 32);
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Looking at rec num " + recNum);
                    logger.finer("  " + xmin2 + ", " + ymin2 + "\n  " + xmax2 + ", " + ymax2);
                }

                if (gatherBounds) {
                    bounds.addPoint(xmin2, ymin2);
                    bounds.addPoint(xmax2, ymax2);
                }

                if (intersects(xmin, ymin, xmax, ymax, xmin2, ymin2, xmax2, ymax2)) {

                    int offset = readBEInt(ixRecord, 0);
                    int byteOffset = offset * 2;
                    int contentLength = readBEInt(ixRecord, 4);
                    int recordSize = (contentLength * 2) + 8;
                    // System.out.print(".");
                    // System.out.flush();

                    if (recordSize < 0) {
                        logger.warning("SpatialIndex: supposed to read record size of "
                                + recordSize);
                        break;
                    }

                    if (recordSize > sRecordSize) {
                        sRecordSize = recordSize;
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("Shapefile SpatialIndex record size: " + sRecordSize);
                        }
                        sRecord = new byte[sRecordSize];
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("going to shp byteOffset = " + byteOffset
                                + " for record size = " + recordSize + ", offset = " + offset
                                + ", shape type = " + shapeType);
                    }

                    try {
                        shp.seek(byteOffset);
                        int nBytes = shp.read(sRecord, 0, recordSize);
                        if (nBytes < recordSize) {
                            logger.warning("Shapefile SpatialIndex expected " + recordSize
                                    + " bytes, but got " + nBytes + " bytes instead.");
                        }

                        ESRIRecord record = makeESRIRecord(shapeType, sRecord, 0);
                        v.addElement(record);
                    } catch (IOException ioe) {
                        logger.warning("SpatialIndex.locateRecords: IOException. ");
                        ioe.printStackTrace();
                        break;
                    }
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Processed " + recNum + " records");
            logger.fine("Selected " + v.size() + " records");
        }
        int nRecords = v.size();

        ssx.close();
        shp.close();
        shp = null;
        ESRIRecord result[] = new ESRIRecord[nRecords];
        v.copyInto(result);
        return result;

    }

    /**
     * The factory is used to filter and create OMGraphics from a shape file.
     * This accessor is provided in order to allow you to modify the data
     * projection it uses, or the line type.
     * 
     * @return EsriGraphicFactory being used to create EsriGraphics from shape
     *         file.
     */
    public EsriGraphicFactory getFactory() {
        if (factory == null) {
            factory = new EsriGraphicFactory();
            // You can set this in the ShapeLayer if you want, replacing
            // DrawingAttributes with GraphicAttributes with a LINETYPE set.
            
            // factory.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
        }
        return factory;
    }

    public void setFactory(EsriGraphicFactory factory) {
        this.factory = factory;
    }

    /**
     * Locates OMGraphics in the shape file that intersect with the given
     * rectangle. The spatial index is searched for intersections and the
     * appropriate OMGraphics are created from the shape file.
     * 
     * @param xmin the smaller of the x coordinates
     * @param ymin the smaller of the y coordinates
     * @param xmax the larger of the x coordinates
     * @param ymax the larger of the y coordinates
     * @param list OMGraphicList to add OMGraphics to and return, if null one
     *        will be created.
     * @param drawingAttributes DrawingAttributes to set on the OMGraphics.
     * @param mapProj the Map Projection for the OMGraphics so they can be
     *        generated right after creation.
     * @param dataProj for pre-projected data, a coordinate translator for the
     *        data's projection to use to translate the coordinates to decimal
     *        degree lat/lon. Can be null to leave the coordinates untouched.
     * @return an OMGraphicList containing OMGraphics that intersect the given
     *         rectangle
     * @exception IOException if something goes wrong reading the files
     */
    public OMGraphicList getOMGraphics(double xmin, double ymin, double xmax, double ymax,
                                       OMGraphicList list, DrawingAttributes drawingAttributes,
                                       Projection mapProj, GeoCoordTransformation dataProj)
            throws IOException, FormatException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("locateRecords:\n\txmin: " + xmin + "; ymin: " + ymin + "\n\txmax: " + xmax
                    + "; ymax: " + ymax);
        }

        if (list == null) {
            list = new OMGraphicList();
        }

        if (shp == null) {
            shp = getShpFile(shpFileName);
        }

        if (shp == null) {
            return list;
        }

        EsriGraphicFactory.ReadByteTracker byteTracker = new EsriGraphicFactory.ReadByteTracker();
        EsriGraphicFactory factory = getFactory();
        factory.setDataCoordTransformation(dataProj);

        OMGraphicList labels = new OMGraphicList();
        list.add(labels);

        for (Iterator<?> it = entryIterator(dataProj); it.hasNext();) {
            Entry entry = (Entry) it.next();

            if (entry.intersects(xmin, ymin, xmax, ymax)) {

                try {

                    OMGraphic omg = (OMGraphic) factory.makeEsriGraphicFromRecord(entry.getByteOffset(), shp, drawingAttributes, pointIcon, byteTracker);

                    if (omg != null) {

                        if (dbf != null) {
                            omg = dbf.evaluate(omg, labels, mapProj);

                            if (omg == null) {
                                // Failed dbf test, should be ignored.
                                continue;
                            }
                        }

                        if (mapProj != null) {
                            omg.generate(mapProj);
                        }
                        list.add(omg);
                    }

                } catch (IOException ioe) {
                    logger.warning("IOException message: " + ioe.getMessage());
                    ioe.printStackTrace();
                    break;
                }
            }
        }

        if (shp != null) {
            shp.close();
            // Not sure why we want to set this null here. It's cleaner, but a
            // new
            // BinaryFile object is created for every projection change.
            // shp = null;
        }

        if (dbf != null) {
            dbf.close();
        }

        return list;

    }

    /**
     * Retrieves all OMGraphics in the shape file.
     * 
     * @param retList OMGraphicList to add OMGraphics to and return, if null one
     *        will be created.
     * @param drawingAttributes DrawingAttributes to set on the OMGraphics.
     * @param mapProj the Map Projection for the OMGraphics so they can be
     *        generated right after creation. This will also be used by the
     *        DbfHandler, to determine if some OMGraphics should not be returned
     *        based on attribute settings.
     * @param dataProj for preprojected data, a coordinate translator for the
     *        data's projection to use to translate the coordinates to decimal
     *        degree lat/lon. Can be null to leave the coordinates untouched.
     * @return an OMGraphicList containing OMGraphics that intersect the given
     *         rectangle
     * @exception IOException if something goes wrong reading the files
     */
    public OMGraphicList getAllOMGraphics(OMGraphicList retList,
                                          DrawingAttributes drawingAttributes, Projection mapProj,
                                          GeoCoordTransformation dataProj)
            throws IOException, FormatException {

        if (retList == null) {
            retList = new OMGraphicList();
        }

        if (shp == null) {
            shp = getShpFile(shpFileName);
        }

        if (shp == null) {
            return retList;
        }

        EsriGraphicFactory factory = getFactory();
        factory.setDataCoordTransformation(dataProj);
        factory.getEsriGraphics(shp, drawingAttributes, pointIcon, mapProj, retList);

        shp.close();

        return retList;
    }

    /**
     * Takes the contents of the list and evaluates them against the information
     * contained in the DbfHandler set in this SpatialIndex class.
     * 
     * @param retList the list of OMGraphics to evaluate.
     * @param mapProj the current map projection to be used by the DbfHandler to
     *        determine if some OMGraphics should be visible.
     * @return OMGraphicList containing OMGraphics modified/passing evaluations
     *         rules in the DbfHandler.
     */
    public OMGraphicList evaluateDbf(OMGraphicList retList, Projection mapProj) {

        if (dbf != null) {
            OMGraphicList labels = new OMGraphicList();
            retList.add(labels);

            OMGraphicList testList = new OMGraphicList();
            for (OMGraphic omg : retList) {

                if (omg != null) {

                    omg = dbf.evaluate(omg, labels, mapProj);

                    if (mapProj != null) {
                        omg.generate(mapProj);
                    }
                    testList.add(omg);
                }
            }
            retList = testList;
        }

        return retList;
    }

    /**
     * Evaluates the OMGraphic against the DbfHandler rules.
     * 
     * @param omg the OMGraphic to evaluate.
     * @param labels for DbfHandler label rules. Assumes that you are managing
     *        display of the labels list.
     * @param mapProj for DbfHandler scale rules.
     * @return OMGraphic if it passes the rules.
     */
    public OMGraphic evaluate(OMGraphic omg, OMGraphicList labels, Projection mapProj) {
        if (dbf != null) {
            omg = dbf.evaluate(omg, labels, mapProj);
        }

        return omg;
    }

    /**
     * Skips the BinaryFile for the shp data to the offset and reads the record
     * data there, creating an OMGraphic from that data.
     * 
     * @param byteOffset , usually gotten from an Entry object.
     * @param drawingAttributes
     * @return OMGraphic from entry object.
     * @throws IOException
     * @throws FormatException
     */
    public OMGraphic getOMGraphicAtOffset(int byteOffset, DrawingAttributes drawingAttributes)
            throws IOException, FormatException {
        return (OMGraphic) getFactory().makeEsriGraphicFromRecord(byteOffset, shp, drawingAttributes, pointIcon, new EsriGraphicFactory.ReadByteTracker());
    }

    /**
     * Provides an iterator over the SpatialIndex entries.
     * 
     * @return iterator over entries
     * @throws IOException
     * @throws FormatException
     */
    public Iterator<Entry> entryIterator() throws IOException, FormatException {
        return entryIterator(null);
    }

    /**
     * Provides an iterator over the SpatialIndex entries.
     * 
     * @param dataTransform GeoCoordTransform for pre-projected data.
     * @return iterator over entries, data transformed.
     * @throws IOException
     * @throws FormatException
     */
    public Iterator<Entry> entryIterator(GeoCoordTransformation dataTransform)
            throws IOException, FormatException {
        if (entries == null) {
            boolean gatherBounds = false;
            if (bounds == null) {
                bounds = new ESRIBoundingBox();
                gatherBounds = true;
            }

            entries = readIndexFile(gatherBounds ? bounds : null, dataTransform);
        }

        return entries.iterator();
    }

    /**
     * 
     * @param bounds if not null, add min/max values to them.
     * @return list of entries.
     * @throws IOException
     * @throws FormatException
     */
    protected List<Entry> readIndexFile(ESRIBoundingBox bounds) throws IOException, FormatException {
        return readIndexFile(bounds, null);
    }

    /**
     * 
     * @param bounds if not null, add min/max values to them.
     * @param dataTransform GeoCoordTransform for pre-projected data.
     * @return list of entries
     * @throws IOException
     * @throws FormatException
     */
    protected List<Entry> readIndexFile(ESRIBoundingBox bounds, GeoCoordTransformation dataTransform)
            throws IOException, FormatException {
        entries = new ArrayList<Entry>();

        byte ixRecord[] = new byte[SPATIAL_INDEX_RECORD_LENGTH];

        if (shpFileName == null) {
            return entries;
        }

        String ssxFileName = ssx(shpFileName);

        if (!BinaryBufferedFile.exists(ssxFileName)) {
            // If we got this far without an ssx existing, then we should just
            // create one in memory.
            entries = SpatialIndex.MemoryIndex.create(shpFileName);
            return entries;
        }

        BinaryBufferedFile ssx = new BinaryBufferedFile(ssxFileName);

        ssx.byteOrder(false);
        ssx.seek(100); // skip the file header

        LatLonPoint llp = null;
        if (dataTransform != null) {
            llp = new LatLonPoint.Double();
        }

        while (true) {
            int result = ssx.read(ixRecord, 0, SPATIAL_INDEX_RECORD_LENGTH);
            if (result <= 0) {
                break;// EOF
            } else {
                double xmin = readLEDouble(ixRecord, 8);
                double ymin = readLEDouble(ixRecord, 16);
                double xmax = readLEDouble(ixRecord, 24);
                double ymax = readLEDouble(ixRecord, 32);
                int byteOffset = readBEInt(ixRecord, 0) * 2;

                if (dataTransform != null) {
                    llp = dataTransform.inverse(xmin, ymin, llp);
                    xmin = llp.getX();
                    ymin = llp.getY();
                    llp = dataTransform.inverse(xmax, ymax, llp);
                    xmax = llp.getX();
                    ymax = llp.getY();
                }

                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("entry:\t" + xmin + ", " + ymin + "\n\t" + xmax + ", " + ymax);
                }

                Entry entry = new Entry(xmin, ymin, xmax, ymax, byteOffset);
                entries.add(entry);

                if (bounds != null) {
                    bounds.addPoint(xmin, ymin);
                    bounds.addPoint(xmax, ymax);
                }
            }
        }

        ssx.close();

        return entries;

    }

    /**
     * Determines if two rectangles intersect. Actually, this method determines
     * if two rectangles don't intersect, and then returns a negation of that
     * result. But the bottom line is the same.
     * 
     * @param xmin1 the small x of rectangle 1
     * @param ymin1 the small y of rectangle 1
     * @param xmax1 the big x of rectangle 1
     * @param ymax1 the big y of rectangle 1
     * @param xmin2 the small x of rectangle 2
     * @param ymin2 the small y of rectangle 2
     * @param xmax2 the big x of rectangle 2
     * @param ymax2 the big y of rectangle 2
     * @return <code>true</code> if the rectangles intersect, <code>false</code>
     *         if they do not
     */
    protected static final boolean intersects(double xmin1, double ymin1, double xmax1,
                                              double ymax1, double xmin2, double ymin2,
                                              double xmax2, double ymax2) {
        return !((xmax1 <= xmin2) || (ymax1 <= ymin2) || (xmin1 >= xmax2) || (ymin1 >= ymax2));
    }

    /**
     * Displays the contents of this index.
     * 
     * @param showBounds true to show bounding box, false to skip it
     * @exception IOException if something goes wrong reading the file
     */
    public void dumpIndex(boolean showBounds) throws IOException {
        byte ixRecord[] = new byte[SPATIAL_INDEX_RECORD_LENGTH];
        int recNum = 0;

        if (shpFileName == null) {
            return;
        }

        BinaryBufferedFile ssx = new BinaryBufferedFile(ssx(shpFileName));
        ssx.seek(100); // skip the file header
        while (true) {
            int result = ssx.read(ixRecord, 0, SPATIAL_INDEX_RECORD_LENGTH);
            // if (result == -1) {
            if (result <= 0) {
                logger.info("Processed " + recNum + " records");
                break;// EOF
            } else {
                recNum++;
                int offset = readBEInt(ixRecord, 0);
                int length = readBEInt(ixRecord, 4);
                logger.info("Record "
                        + recNum
                        + ": "
                        + offset
                        + ", "
                        + length
                        + (showBounds ? ("; " + readLEDouble(ixRecord, 8) + ", "
                                + readLEDouble(ixRecord, 16) + ", " + readLEDouble(ixRecord, 24)
                                + ", " + readLEDouble(ixRecord, 32)) : ""));
            }
        }
        ssx.close();
    }

    /**
     * Prints a usage statement describing how to use this class from the
     * command line.
     * 
     * @param out The output stream to use for output
     */
    public static void printUsage(PrintStream out) {
        String className = SpatialIndex.class.getName();

        out.println("Usage:");
        out.println();
        out.println("java " + className + " -c file.shp");
        out.println("Creates spatial index <file.ssx> from " + "shape file <file.shp>.");
        out.println();
        out.println("java " + className + " -d file.shp");
        out.println("Dumps spatial index information, excluding "
                + "bounding boxes to stdout.  Useful for " + "comparing to a shape index.");
        out.println();
        out.println("java " + className + " -d -b file.shp");
        out.println("Dumps spatial index information including " + "bounding boxes to stdout.");
        out.println();
    }

    /**
     * Locate file 'fileName' in classpath, if it is not an absolute file name.
     * 
     * @return absolute name of the file as a string if found, null otherwise.
     */
    public static String locateFile(String name) {
        File file = new File(name);
        if (file.exists()) {
            return name;
        } else {
            java.net.URL url = ClassLoader.getSystemResource(name);

            // OK, now we want to look around for the file, in the
            // classpaths, and as a resource. It may be a file in
            // a classpath, available for direct access.
            if (url != null) {
                String newname = url.getFile();
                file = new File(newname);
                if (file.exists()) {
                    return newname;
                }
            }
        }
        return null;
    }

    /**
     * Create a SpatialIndex object with just a shape file name. If the shape
     * file is local, this method will attempt to build the spatial index file
     * and place it next to the shape file.
     */
    public static SpatialIndex locateAndSetShapeData(String shapeFileName) {
        SpatialIndex spi = null;

        if (shapeFileName == null) {
            return null;
        }

        int appendixIndex = shapeFileName.indexOf(".shp");

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("created with just the shape file " + shapeFileName);
        }

        if (appendixIndex != -1) {

            if (BinaryFile.exists(shapeFileName)) {
                // OK, the shape files exists - now look for spatial
                // index file next to it.
                String spatialIndexFileName = ssx(shapeFileName);

                // Now, see if the spatialIndexFileName exists, and if
                // not, create it.
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Trying to locate spatial index file " + spatialIndexFileName);
                }

                try {
                    spi = new SpatialIndex(shapeFileName);

                } catch (java.io.IOException ioe) {
                    logger.warning(ioe.getMessage());
                    ioe.printStackTrace();
                    spi = null;
                }
            } else {
                logger.warning("Couldn't locate shape file " + shapeFileName);
            }

        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("file " + shapeFileName + " doesn't look like a shape file");
            }
        }

        return spi;
    }

    /**
     * The driver for the command line interface. Reads the command line
     * arguments and executes appropriate calls.
     * <p>
     * See the file documentation for usage.
     * 
     * @param argv the command line arguments
     * @exception IOException if something goes wrong reading or writing the
     *            file
     */
    public static void main(String argv[]) throws IOException {
        int argc = argv.length;

        if (argc == 0) {
            // No arguments, give the user some help
            printUsage(System.out);
            System.exit(0);
        }

        logger.setLevel(Level.FINER);

        if (argv[0].equals("-d")) {
            if (argc == 2) {
                String name = argv[1];
                SpatialIndex si = new SpatialIndex(name);
                si.dumpIndex(false);
            } else if ((argc == 3) && (argv[1].equals("-b"))) {
                String name = argv[2];
                SpatialIndex si = new SpatialIndex(name);
                si.dumpIndex(true);
            } else {
                printUsage(System.err);
                System.exit(1);
            }
        } else if ((argc == 2) && argv[0].equals("-c")) {
            String shapeFile = argv[1];
            SpatialIndex.FileIndex.create(shapeFile);
        } else {
            printUsage(System.err);
            System.exit(1);
        }
    }

    /**
     * Set the icon to use for point objects, in general.
     * 
     * @param ii ImageIcon to use for icon.
     */
    public synchronized void setPointIcon(ImageIcon ii) {
        pointIcon = ii;
    }

    /**
     * Get the icon used for general point objects.
     * 
     * @return ImageIcon, null if not set.
     */
    public synchronized ImageIcon getPointIcon() {
        return pointIcon;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.io.Closable#close(boolean)
     */
    public boolean close(boolean done) {
        try {
            if (shp != null) {
                shp.close();
            }

            if (done) {
                shp = null;
            }

            if (done && entries != null) {
                entries.clear();
                entries = null;
            }

            return true;
        } catch (IOException ioe) {

        }

        return false;
    }

    /**
     * The spatial information for each shp entry is held in one of these.
     * 
     * @author dietrick
     */
    public static class Entry {

        double xMin;
        double yMin;
        double xMax;
        double yMax;
        int byteOffset;

        public Entry(double xMin, double yMin, double xMax, double yMax, int byteOffset) {
            this.xMin = xMin;
            this.yMin = yMin;
            this.xMax = xMax;
            this.yMax = yMax;
            this.byteOffset = byteOffset;
        }

        public boolean intersects(double xmin, double ymin, double xmax, double ymax) {
            return SpatialIndex.intersects(xmin, ymin, xmax, ymax, xMin, yMin, xMax, yMax);
        }

        public int getByteOffset() {
            return byteOffset;
        }

        public void addToBounds(ESRIBoundingBox bounds) {
            bounds.addPoint(xMin, yMin);
            bounds.addPoint(xMax, yMax);
        }

    }

    public DbfHandler getDbf() {
        return dbf;
    }

    public void setDbf(DbfHandler dbf) {
        this.dbf = dbf;
    }

    public static class FileIndex {

        protected FileIndex() {
        }

        public static void create(String shpFile) {
            FileIndex fi = new FileIndex();
            fi.createIndex(shpFile);
        }

        /**
         * Writes the spatial index for a polygon shape file.
         * 
         * @param is the shape file input stream
         * @param ptr the current position in the file
         * @param os the spatial index file output stream
         */
        protected void indexPolygons(InputStream is, long ptr, OutputStream os) {
            boolean moreRecords = true;
            byte rHdr[] = new byte[SHAPE_RECORD_HEADER_LENGTH];
            byte outBuf[] = new byte[SPATIAL_INDEX_RECORD_LENGTH];
            int result;
            int shapeType;
            int nRecords = 0;
            int recLengthWords, recLengthBytes /* , recNumber */;
            long recOffset;
            int recBufSize = 100000;
            byte recBuf[] = new byte[recBufSize];
            ESRIBoundingBox polyBounds;

            try {
                while (moreRecords) {
                    result = is.read(rHdr, 0, SHAPE_RECORD_HEADER_LENGTH);
                    if (result < 0) {
                        moreRecords = false;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Found " + nRecords + " records");
                            logger.fine("recBufSize = " + recBufSize);
                        }
                    } else {
                        nRecords++;
                        recOffset = ptr;
                        /* recNumber = */readBEInt(rHdr, 0);
                        recLengthWords = readBEInt(rHdr, 4);
                        recLengthBytes = recLengthWords * 2;

                        if (recLengthBytes > recBufSize) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Shapefile SpatialIndex increasing recBufSize to "
                                        + recLengthBytes);
                            }
                            recBufSize = recLengthBytes;
                            recBuf = new byte[recBufSize];
                        }

                        result = is.read(recBuf, 0, recLengthBytes);
                        // Null shapes are allowed in any shape file, at any
                        // time.
                        shapeType = readLEInt(recBuf, 0);
                        if (shapeType != SHAPE_TYPE_NULL) {
                            polyBounds = readBox(recBuf, 4);
                        } else {
                            polyBounds = new ESRIBoundingBox();
                        }
                        ptr += recLengthBytes + 8;

                        writeBEInt(outBuf, 0, (int) (recOffset / 2));
                        writeBEInt(outBuf, 4, recLengthWords);
                        writeLEDouble(outBuf, 8, polyBounds.min.x);
                        writeLEDouble(outBuf, 16, polyBounds.min.y);
                        writeLEDouble(outBuf, 24, polyBounds.max.x);
                        writeLEDouble(outBuf, 32, polyBounds.max.y);
                        os.write(outBuf, 0, SPATIAL_INDEX_RECORD_LENGTH);
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

        /**
         * Writes the spatial index for a point shape file.
         * 
         * @param is the shape file input stream
         * @param ptr the current position in the file
         * @param os the spatial index file output stream
         */
        protected void indexPoints(InputStream is, long ptr, OutputStream os) {
            boolean moreRecords = true;
            byte rHdr[] = new byte[SHAPE_RECORD_HEADER_LENGTH];
            byte outBuf[] = new byte[SPATIAL_INDEX_RECORD_LENGTH];
            int result;
            int nRecords = 0;
            int recLengthWords, recLengthBytes/* , recNumber */;
            long recOffset;
            int shapeType;
            int recBufSize = 20;
            byte recBuf[] = new byte[recBufSize];
            double x = 0;
            double y = 0;

            try {
                while (moreRecords) {
                    result = is.read(rHdr, 0, SHAPE_RECORD_HEADER_LENGTH);
                    if (result < 0) {
                        moreRecords = false;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Found " + nRecords + " records");
                            logger.fine("recBufSize = " + recBufSize);
                        }
                    } else {
                        nRecords++;
                        recOffset = ptr;
                        /* recNumber = */readBEInt(rHdr, 0);
                        recLengthWords = readBEInt(rHdr, 4);
                        recLengthBytes = recLengthWords * 2;

                        if (recLengthBytes > recBufSize) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Shapefile SpatialIndex increasing recBufSize to "
                                        + recLengthBytes);
                            }
                            recBufSize = recLengthBytes;
                            recBuf = new byte[recBufSize];
                        }

                        result = is.read(recBuf, 0, recLengthBytes);
                        // Null shapes are allowed in any shape file, at any
                        // time.
                        shapeType = readLEInt(recBuf, 0);
                        if (shapeType != SHAPE_TYPE_NULL) {
                            x = readLEDouble(recBuf, 4);
                            y = readLEDouble(recBuf, 12);
                        }
                        ptr += recLengthBytes + 8;

                        writeBEInt(outBuf, 0, (int) (recOffset / 2));
                        writeBEInt(outBuf, 4, recLengthWords);
                        writeLEDouble(outBuf, 8, x);
                        writeLEDouble(outBuf, 16, y);
                        writeLEDouble(outBuf, 24, x);
                        writeLEDouble(outBuf, 32, y);
                        os.write(outBuf, 0, SPATIAL_INDEX_RECORD_LENGTH);
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

        /**
         * Writes the spatial index for a null shape file.
         * 
         * @param is the shape file input stream
         * @param ptr the current position in the file
         * @param os the spatial index file output stream
         */
        protected void indexNulls(InputStream is, long ptr, OutputStream os) {
            boolean moreRecords = true;
            byte rHdr[] = new byte[SHAPE_RECORD_HEADER_LENGTH];
            byte outBuf[] = new byte[SPATIAL_INDEX_RECORD_LENGTH];
            int result;
            int nRecords = 0;
            int recLengthWords, recLengthBytes/* , recNumber */;
            long recOffset;
            int recBufSize = 20;
            byte recBuf[] = new byte[recBufSize];
            double x;
            double y;

            try {
                while (moreRecords) {
                    result = is.read(rHdr, 0, SHAPE_RECORD_HEADER_LENGTH);
                    if (result < 0) {
                        moreRecords = false;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Found " + nRecords + " records");
                            logger.fine("recBufSize = " + recBufSize);
                        }
                    } else {
                        nRecords++;
                        recOffset = ptr;
                        /* recNumber = */readBEInt(rHdr, 0);
                        recLengthWords = readBEInt(rHdr, 4);
                        recLengthBytes = recLengthWords * 2;

                        if (recLengthBytes > recBufSize) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Shapefile SpatialIndex increasing recBufSize to "
                                        + recLengthBytes);
                            }
                            recBufSize = recLengthBytes;
                            recBuf = new byte[recBufSize];
                        }

                        result = is.read(recBuf, 0, recLengthBytes);
                        x = 0;
                        y = 0;
                        ptr += recLengthBytes + 8;

                        writeBEInt(outBuf, 0, (int) (recOffset / 2));
                        writeBEInt(outBuf, 4, recLengthWords);
                        writeLEDouble(outBuf, 8, x);
                        writeLEDouble(outBuf, 16, y);
                        writeLEDouble(outBuf, 24, x);
                        writeLEDouble(outBuf, 32, y);
                        os.write(outBuf, 0, SPATIAL_INDEX_RECORD_LENGTH);
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

        /**
         * Creates a spatial index for a shape file. Reads the records from the
         * shape file, writing appropriate index records to the spatial index
         * file.
         * 
         * @param inFile the shape file or spatial index file, the method will
         *        figure it out based on the file name extension.
         */
        public void createIndex(String inFile) {
            String ssxFile = null;
            String shpFile = null;
            if (inFile.endsWith(".shp")) {
                shpFile = inFile;
                ssxFile = ssx(shpFile);
            } else if (inFile.endsWith(".ssx")) {
                ssxFile = inFile;
                shpFile = ssxFile.substring(0, ssxFile.indexOf(".ssx")) + ".shp";
            } else {
                return;
            }

            byte fileHeader[] = new byte[SHAPE_FILE_HEADER_LENGTH];
            FileInputStream shp = null;
            FileOutputStream ssx = null;
            int shapeType;
            try {
                shp = new FileInputStream(shpFile);
                ssx = new FileOutputStream(ssxFile);
                shp.read(fileHeader, 0, SHAPE_FILE_HEADER_LENGTH);
                ssx.write(fileHeader, 0, SHAPE_FILE_HEADER_LENGTH);
                shapeType = readLEInt(fileHeader, 32);
                switch (shapeType) {
                case SHAPE_TYPE_NULL:
                    indexNulls(shp, SHAPE_FILE_HEADER_LENGTH, ssx);
                    break;
                case SHAPE_TYPE_POINT:
                case SHAPE_TYPE_POINTZ:
                case SHAPE_TYPE_POINTM:
                    indexPoints(shp, SHAPE_FILE_HEADER_LENGTH, ssx);
                    break;
                case SHAPE_TYPE_MULTIPOINT:
                case SHAPE_TYPE_MULTIPOINTZ:
                case SHAPE_TYPE_MULTIPOINTM:
                    // case SHAPE_TYPE_ARC:
                case SHAPE_TYPE_POLYLINE:
                case SHAPE_TYPE_POLYLINEZ:
                case SHAPE_TYPE_POLYLINEM:
                case SHAPE_TYPE_POLYGON:
                case SHAPE_TYPE_POLYGONZ:
                case SHAPE_TYPE_POLYGONM:
                    indexPolygons(shp, SHAPE_FILE_HEADER_LENGTH, ssx);
                    break;
                default:
                    logger.warning("Unknown shape type: " + shapeType);
                }

            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (shp != null)
                        shp.close();
                    if (ssx != null)
                        ssx.close();
                } catch (java.io.IOException e) {
                }
            }
        }
    }

    public static class MemoryIndex {

        protected MemoryIndex() {
        }

        public static List<Entry> create(String shpFile) {
            MemoryIndex mi = new MemoryIndex();
            return mi.createIndex(shpFile);
        }

        /**
         * Writes the spatial index for a polygon shape file.
         * 
         * @param is the shape file input stream
         * @param ptr the current position in the file
         * @param entries a List of Entries to add to
         */
        protected void indexPolygons(InputStream is, long ptr, List<Entry> entries) {
            boolean moreRecords = true;
            byte rHdr[] = new byte[SHAPE_RECORD_HEADER_LENGTH];
            int result;
            int shapeType;
            int nRecords = 0;
            int recLengthWords, recLengthBytes /* , recNumber */;
            long recOffset;
            int recBufSize = 100000;
            byte recBuf[] = new byte[recBufSize];
            ESRIBoundingBox polyBounds;

            try {
                while (moreRecords) {
                    result = is.read(rHdr, 0, SHAPE_RECORD_HEADER_LENGTH);
                    if (result < 0) {
                        moreRecords = false;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Shapefile SpatialIndex Found " + nRecords + " records");
                            logger.fine("Shapefile SpatialIndex recBufSize = " + recBufSize);
                        }
                    } else {
                        nRecords++;
                        recOffset = ptr;
                        /* recNumber = */readBEInt(rHdr, 0);
                        recLengthWords = readBEInt(rHdr, 4);
                        recLengthBytes = recLengthWords * 2;

                        if (recLengthBytes > recBufSize) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Shapefile SpatialIndex increasing recBufSize to "
                                        + recLengthBytes);
                            }
                            recBufSize = recLengthBytes;
                            recBuf = new byte[recBufSize];
                        }

                        result = is.read(recBuf, 0, recLengthBytes);
                        // Null shapes are allowed in any shape file, at any
                        // time.
                        shapeType = readLEInt(recBuf, 0);
                        if (shapeType != SHAPE_TYPE_NULL) {
                            polyBounds = readBox(recBuf, 4);
                        } else {
                            polyBounds = new ESRIBoundingBox();
                        }
                        ptr += recLengthBytes + 8;

                        Entry entry = new Entry(polyBounds.min.x, polyBounds.min.y, polyBounds.max.x, polyBounds.max.y, (int) recOffset);
                        entries.add(entry);
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

        /**
         * Writes the spatial index for a point shape file.
         * 
         * @param is the shape file input stream
         * @param ptr the current position in the file
         * @param entries a List of Entries to add to
         */
        protected void indexPoints(InputStream is, long ptr, List<Entry> entries) {
            boolean moreRecords = true;
            byte rHdr[] = new byte[SHAPE_RECORD_HEADER_LENGTH];
            int result;
            int nRecords = 0;
            int recLengthWords, recLengthBytes/* , recNumber */;
            long recOffset;
            int shapeType;
            int recBufSize = 20;
            byte recBuf[] = new byte[recBufSize];
            double x = 0;
            double y = 0;

            try {
                while (moreRecords) {
                    result = is.read(rHdr, 0, SHAPE_RECORD_HEADER_LENGTH);
                    if (result < 0) {
                        moreRecords = false;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Found " + nRecords + " records");
                            logger.fine("recBufSize = " + recBufSize);
                        }
                    } else {
                        nRecords++;
                        recOffset = ptr;
                        /* recNumber = */readBEInt(rHdr, 0);
                        recLengthWords = readBEInt(rHdr, 4);
                        recLengthBytes = recLengthWords * 2;

                        if (recLengthBytes > recBufSize) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Shapefile SpatialIndex increasing recBufSize to "
                                        + recLengthBytes);
                            }
                            recBufSize = recLengthBytes;
                            recBuf = new byte[recBufSize];
                        }

                        result = is.read(recBuf, 0, recLengthBytes);
                        // Null shapes are allowed in any shape file, at any
                        // time.
                        shapeType = readLEInt(recBuf, 0);
                        if (shapeType != SHAPE_TYPE_NULL) {
                            x = readLEDouble(recBuf, 4);
                            y = readLEDouble(recBuf, 12);
                        }
                        ptr += recLengthBytes + 8;

                        Entry entry = new Entry(x, y, x, y, (int) recOffset);
                        entries.add(entry);
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

        /**
         * Writes the spatial index for a null shape file.
         * 
         * @param is the shape file input stream
         * @param ptr the current position in the file
         * @param entries a List of Entries to add to
         */
        protected void indexNulls(InputStream is, long ptr, List<Entry> entries) {
            boolean moreRecords = true;
            byte rHdr[] = new byte[SHAPE_RECORD_HEADER_LENGTH];
            int result;
            int nRecords = 0;
            int recLengthWords, recLengthBytes/* , recNumber */;
            long recOffset;
            int recBufSize = 20;
            byte recBuf[] = new byte[recBufSize];
            double x;
            double y;

            try {
                while (moreRecords) {
                    result = is.read(rHdr, 0, SHAPE_RECORD_HEADER_LENGTH);
                    if (result < 0) {
                        moreRecords = false;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Found " + nRecords + " records");
                            logger.fine("recBufSize = " + recBufSize);
                        }
                    } else {
                        nRecords++;
                        recOffset = ptr;
                        /* recNumber = */readBEInt(rHdr, 0);
                        recLengthWords = readBEInt(rHdr, 4);
                        recLengthBytes = recLengthWords * 2;

                        if (recLengthBytes > recBufSize) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Shapefile SpatialIndex increasing recBufSize to "
                                        + recLengthBytes);
                            }
                            recBufSize = recLengthBytes;
                            recBuf = new byte[recBufSize];
                        }

                        result = is.read(recBuf, 0, recLengthBytes);
                        x = 0;
                        y = 0;
                        ptr += recLengthBytes + 8;

                        Entry entry = new Entry(x, y, x, y, (int) recOffset);
                        entries.add(entry);
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

        /**
         * Creates a spatial index for a shape file. Reads the records from the
         * shape file, writing appropriate index records to the spatial index
         * file.
         * 
         * @param inFile the shape file.
         */
        public List<Entry> createIndex(String inFile) {
            String shpFile = null;
            List<Entry> entries = new ArrayList<Entry>();
            if (inFile.endsWith(".shp")) {
                shpFile = inFile;
            } else {
                logger.warning("can't create spatial index entries from non-shape file: " + inFile);
                return entries;
            }

            byte fileHeader[] = new byte[SHAPE_FILE_HEADER_LENGTH];
            BufferedInputStream shp = null;
            int shapeType;
            try {
                URL shpURL = PropUtils.getResourceOrFileOrURL(shpFile);
                if (shpURL == null) {
                    return entries;
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("creating spatial index entries for " + inFile);
                }
                shp = new BufferedInputStream(shpURL.openStream());
                shp.read(fileHeader, 0, SHAPE_FILE_HEADER_LENGTH);
                shapeType = readLEInt(fileHeader, 32);
                switch (shapeType) {
                case SHAPE_TYPE_NULL:
                    indexNulls(shp, SHAPE_FILE_HEADER_LENGTH, entries);
                    break;
                case SHAPE_TYPE_POINT:
                case SHAPE_TYPE_POINTZ:
                case SHAPE_TYPE_POINTM:
                    indexPoints(shp, SHAPE_FILE_HEADER_LENGTH, entries);
                    break;
                case SHAPE_TYPE_MULTIPOINT:
                case SHAPE_TYPE_MULTIPOINTZ:
                case SHAPE_TYPE_MULTIPOINTM:
                    // case SHAPE_TYPE_ARC:
                case SHAPE_TYPE_POLYLINE:
                case SHAPE_TYPE_POLYLINEZ:
                case SHAPE_TYPE_POLYLINEM:
                case SHAPE_TYPE_POLYGON:
                case SHAPE_TYPE_POLYGONZ:
                case SHAPE_TYPE_POLYGONM:
                    indexPolygons(shp, SHAPE_FILE_HEADER_LENGTH, entries);
                    break;
                default:
                    logger.warning("Unknown shape type: " + shapeType);
                }

            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (shp != null)
                        shp.close();
                } catch (java.io.IOException e) {
                }
            }

            return entries;
        }
    }
}
