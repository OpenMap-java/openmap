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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriGraphicList.java,v $
// $RCSfile: EsriGraphicList.java,v $
// $Revision: 1.11 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.dataAccess.shape.input.ShpInputStream;
import com.bbn.openmap.dataAccess.shape.input.ShxInputStream;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.PropUtils;

/**
 * EsriGraphicList ensures that only supported geometry types are added to its
 * list. Each subclass of this EsriGraphicList list will hold polyline, polygon,
 * or point geometry -- other types of geometry are not supported. As shapes are
 * added to the list, EsriGraphicList will ensure that the type of geometry
 * being added is the same type of geometry as specified by the subclass list
 * type.
 * 
 * @author Doug Van Auken
 * @author Don Dietrick
 */
public abstract class EsriGraphicList
        extends OMGraphicList
        implements ShapeConstants, EsriGraphic {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.shape.EsriGraphicList");
    protected double[] extents;
    protected int type;

    /**
     * Over-ride the add( ) method to trap for inconsistent shape geometry. If
     * you are adding a OMGraphic that is not a list, make sure this list is a
     * sub-list containing multiple geometry parts. Only add another list to a
     * top level EsriGraphicList.
     * 
     * @param shape the non-null OMGraphic to add
     */
    public boolean add(OMGraphic shape) {
        return super.add(shape);
    }

    /**
     * Add an OMGraphic to the GraphicList. The OMGraphic must not be null.
     * 
     * @param g the non-null OMGraphic to add
     * @return true if addition is successful.
     * @exception IllegalArgumentException if OMGraphic is null
     */
    public boolean addOMGraphic(OMGraphic g) {
        return add(g);
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * Get the list type in ESRI type number form
     */
    public int getType() {
        return type;
    }

    /**
     * Construct an EsriGraphicList.
     */
    public EsriGraphicList() {
        super();
    }

    /**
     * Construct an EsriGraphicList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public EsriGraphicList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Construct an EsriGraphicList with an initial capacity and a standard
     * increment value.
     * 
     * @param initialCapacity the initial capacity of the list
     * @param capacityIncrement the capacityIncrement for resizing
     * @deprecated capacityIncrement doesn't do anything.
     */
    public EsriGraphicList(int initialCapacity, int capacityIncrement) {
        super(initialCapacity);
    }

    /**
     * The lat/lon extent of the EsriGraphicList contents, assumed to contain
     * miny, minx, maxy maxx in order of the array.
     */
    public void setExtents(double[] extents) {
        this.extents = extents;
    }

    /**
     * The lat/lon extent of the EsriGraphicList contents, returned as miny,
     * minx, maxy maxx in order of the array.
     */
    public double[] getExtents() {
        if (extents == null) {
            // These are set to their opposites to guarantee some
            // movement.
            extents = new double[] {
                90f,
                180f,
                -90f,
                -180f
            };
        }

        return extents;
    }

    public void addExtents(double[] graphicExtents) {
        double[] ex = getExtents();

        // Check both graphic extents in case they are inadvertently
        // switched.
        for (int i = 0; i < graphicExtents.length; i += 2) {
            if (ex[0] > graphicExtents[i])
                ex[0] = graphicExtents[i];
            if (ex[1] > graphicExtents[i + 1])
                ex[1] = graphicExtents[i + 1];
            if (ex[2] < graphicExtents[i])
                ex[2] = graphicExtents[i];
            if (ex[3] < graphicExtents[i + 1])
                ex[3] = graphicExtents[i + 1];
        }

        // System.out.println("extents of list: " +
        // ex[1] + ", " +
        // ex[0] + ", " +
        // ex[3] + ", " +
        // ex[2]);

    }

    /**
     * Set the DbfTableModel in the AppObject of this list that holds the
     * attribute information about this list's objects.
     */
    public void setTable(DbfTableModel dtm) {
        if (dtm != null) {
            putAttribute(DBF_ATTRIBUTE, dtm);
        } else {
            removeAttribute(DBF_ATTRIBUTE);
        }
    }

    /**
     * Get the DbfTableModel object from the AppObject of this list.
     */
    public DbfTableModel getTable() {
        Object obj = getAttribute(DBF_ATTRIBUTE);
        // Backward compatibility
        if (obj == null) {
            obj = getAppObject();
        }
        if (obj instanceof DbfTableModel) {
            return (DbfTableModel) obj;
        } else {
            return null;
        }
    }

    /**
     * Create a generic DbfTableModel for the contents of this list, where the
     * attributes hold rendering properties for the list contents. The table is
     * stored in the AppObject member variable of the list.
     */
    public void createTable() {
        // lineWidth, lineColor, fillColor, selectColor We could do
        // stroke info. Toss space in there for name, or general
        // attribute for later.
        // this.setAppObject(EsriShapeExport.createDefaultModel(this));
        putAttribute(DBF_ATTRIBUTE, EsriShapeExport.createDefaultModel(this));
    }

    /**
     * Reads the contents of the SHX and SHP files. The SHX file will be read
     * first by utilizing the ShapeIndex.open method. This method will return a
     * list of offsets, which the AbstractSupport.open method will use to
     * iterate through the contents of the SHP file.
     * 
     * @param shp The url of the SHP file
     * @param shx The url of the SHX file
     * @param drawingAttributes a DrawingAttributes object containing the
     *        rendering parameters you might want on the OMGraphics. The
     *        OMGraphic default (black edge, clear fill) will be used if this is
     *        null.
     * @param dbf a DbfTableModel, if you want each row of objects from the
     *        table (an array), inserted into their associated OMGraphic's
     *        appObject. The dbf will be added to the list appObject, so you can
     *        ask it questions later. If null, no problem. If the number of
     *        records doesn't match the OMGraphic list length, nothing will be
     *        done.
     * @return A new EsriGraphicList, null if there is a problem.
     * 
     * @deprecated use getGraphicList(URL, DrawingAttributes, DbfTableModel)
     */
    public static EsriGraphicList getEsriGraphicList(URL shp, URL shx, DrawingAttributes drawingAttributes, DbfTableModel dbf) {
        return getEsriGraphicList(shp, drawingAttributes, dbf, null);
    }

    /**
     * Reads the contents of the SHP files.
     * 
     * @param shp The url of the SHP file
     * @param drawingAttributes a DrawingAttributes object containing the
     *        rendering parameters you might want on the OMGraphics. The
     *        OMGraphic default (black edge, clear fill) will be used if this is
     *        null.
     * @param dbf a DbfTableModel. The dbf will be added to the list appObject,
     *        so you can ask it questions later. If null, no problem. If the
     *        number of records doesn't match the OMGraphic list length, nothing
     *        will be done.
     * @param coordTranslator a GeoCoordTransformation to use to convert
     *        coordinates to decimal degree lat/lon data.
     * @return A new EsriGraphicList, null if there is a problem.
     */
    public static EsriGraphicList getEsriGraphicList(URL shp, DrawingAttributes drawingAttributes, DbfTableModel dbf,
                                                     GeoCoordTransformation coordTranslator) {
        EsriGraphicList list = null;

        // Open and stream shp file
        try {
            InputStream is = shp.openStream();
            ShpInputStream pis = new ShpInputStream(is);
            if (drawingAttributes != null) {
                pis.setDrawingAttributes(drawingAttributes);
            }
            EsriGraphicFactory egf = new EsriGraphicFactory(OMGraphic.LINETYPE_GREATCIRCLE, coordTranslator);
            list = pis.getGeometry(egf);
            is.close();
        } catch (Exception e) {
            logger.warning("Not able to stream SHP file");
            if (logger.isLoggable(Level.FINE)) {
                e.printStackTrace();
            }
            return null;
        }

        if (list != null && dbf != null && dbf.getRowCount() == list.size()) {
            list.setTable(dbf);
        }

        return list;
    }

    /**
     * Reads the contents of the SHP files, including the DBF file, based on the
     * location of the shape file. The dbf will be added to the list appObject,
     * so you can ask it questions later. If null, no problem. If the number of
     * records doesn't match the OMGraphic list length, the dbf information
     * won't be added to the list.
     * 
     * @param shp The url of the SHP file
     * @param drawingAttributes a DrawingAttributes object containing the
     *        rendering parameters you might want on the OMGraphics. The
     *        OMGraphic default (black edge, clear fill) will be used if this is
     *        null.
     * @param coordTranslator used to transform coordinates to lat/lon decimal
     *        degrees for OpenMap.
     * @return A new EsriGraphicList, null if there is a problem.
     */
    public static EsriGraphicList getEsriGraphicList(URL shp, DrawingAttributes drawingAttributes,
                                                     GeoCoordTransformation coordTranslator) {
        DbfTableModel dbf = null;

        if (shp != null) {
            String shpPath = shp.getFile();
            String protocol = shp.getProtocol();
            String host = shp.getHost();

            if (shpPath != null && shpPath.endsWith(".shp")) {
                String dbfPath = shpPath.replace(".shp", ".dbf");
                URL dbfURL;
                try {
                    dbfURL = new URL(protocol, host, dbfPath);
                    dbf = DbfTableModel.getDbfTableModel(dbfURL);
                    dbfURL = null;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        return getEsriGraphicList(shp, drawingAttributes, dbf, coordTranslator);
    }

    public static void main(String[] args) {

        ArgParser ap = new ArgParser("EsriGraphicList");
        ap.add("fixcl", "Check and fix content length of Shape file", 1);
        ap.add("print", "Display text structure of shapes in Shape file", 1);

        if (!ap.parse(args)) {
            ap.printUsage();
            System.exit(0);
        }

        String[] fixit = ap.getArgValues("fixcl");
        if (fixit != null) {
            String shape = fixit[0];
            if (shape.endsWith(".shp")) {
                shape = shape.substring(0, shape.length() - 4);

                try {
                    URL shx = PropUtils.getResourceOrFileOrURL(shape + ".shx");
                    InputStream is = shx.openStream();
                    ShxInputStream pis = new ShxInputStream(is);
                    int[][] index = pis.getIndex();
                    is.close();

                    RandomAccessFile raf = new RandomAccessFile(shape + ".shp", "rw");
                    raf.seek(24);
                    int contentLength = raf.readInt();

                    int indexedContentLength = index[0][index[0].length - 1] + index[1][index[1].length - 1];

                    if (contentLength != indexedContentLength) {
                        System.out.println(shape + " content length - shp: " + contentLength + ", shx: " + indexedContentLength);
                        raf.seek(24);
                        raf.writeInt(indexedContentLength);
                    }
                    raf.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Shape " + shape + " doesn't look like a shape file");
            }
        }

        String[] printit = ap.getArgValues("print");
        if (printit != null) {
            try {
                EsriGraphicList.logger.setLevel(Level.FINER);
                EsriGraphicFactory.logger.setLevel(Level.FINER);
                URL eglURL = PropUtils.getResourceOrFileOrURL(printit[0]);
                EsriGraphicList egl = EsriGraphicList.getEsriGraphicList(eglURL, null, null);
                if (egl != null) {
                    System.out.println(egl.getDescription());
                }

            } catch (Exception e) {
                logger.warning(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
