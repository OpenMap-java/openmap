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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriGraphicList.java,v $
// $RCSfile: EsriGraphicList.java,v $
// $Revision: 1.4 $
// $Date: 2004/02/09 13:33:36 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.dataAccess.shape.input.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.util.Debug;
import java.io.InputStream;
import java.net.URL;

/**
 * EsriGraphicList ensures that only supported geometry types are
 * added to its list.  Each subclass of this EsriGraphicList list will
 * hold polyline, polygon, or point geometry -- other types of
 * geometry are not supported.  As shapes are added to the list,
 * EsriGraphicList will ensure that the type of geometry being added
 * is the same type of geometry as specified by the subclass list type.
 * @author Doug Van Auken
 * @author Don Dietrick
 */
public abstract class EsriGraphicList extends OMGraphicList 
    implements ShapeConstants, EsriGraphic {

    protected float[] extents;

    /**
     * Over-ride the add( ) method to trap for inconsistent shape
     * geometry.  If you are adding a OMGraphic that is not a list,
     * make sure this list is a sub-list containing multiple geometry
     * parts.  Only add another list to a top level EsriGraphicList.
     * @param shape the non-null OMGraphic to add 
     */
    public void add(OMGraphic shape) {
        super.add(shape);
    }

    /**
     * Add an OMGraphic to the GraphicList. The OMGraphic must not be null.
     * @param g the non-null OMGraphic to add
     * @exception IllegalArgumentException if OMGraphic is null
     */
    public void addOMGraphic(OMGraphic g) {
        add(g);
    }

    /**
     * Get the list type in ESRI type number form
     */
    abstract public int getType();

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
     * Construct an EsriGraphicList with an initial capacity and
     * a standard increment value.
     *
     * @param initialCapacity the initial capacity of the list 
     * @param capacityIncrement the capacityIncrement for resizing 
     * @deprecated capacityIncrement doesn't do anything.
     */
    public EsriGraphicList(int initialCapacity, int capacityIncrement) {
        super(initialCapacity);
    }

    /**
     * The lat/lon extent of the EsriGraphicList contents, assumed to
     * contain miny, minx, maxy maxx in order of the array.  
     */
    public void setExtents(float[] extents) {
        this.extents = extents;
    }

    /**
     * The lat/lon extent of the EsriGraphicList contents, returned as
     * miny, minx, maxy maxx in order of the array.  
     */
    public float[] getExtents() {
        if (extents == null) {
            // These are set to their opposites to guarantee some
            // movement.
            extents = new float[] { 90f, 180f, -90f, -180f };
        }

        return extents;
    }

    public void addExtents(float[] graphicExtents) {
        float[] ex = getExtents();

        // Check both graphic extents in case they are inadvertently
        // switched.
        for (int i = 0; i < graphicExtents.length; i+=2) {
            if (ex[0] > graphicExtents[i]) ex[0] = graphicExtents[i];
            if (ex[1] > graphicExtents[i+1]) ex[1] = graphicExtents[i+1];
            if (ex[2] < graphicExtents[i]) ex[2] = graphicExtents[i];
            if (ex[3] < graphicExtents[i+1]) ex[3] = graphicExtents[i+1];
        }

//      System.out.println("extents of list: " +
//                         ex[1] + ", " +
//                         ex[0] + ", " +
//                         ex[3] + ", " +
//                         ex[2]);


    }

    /**
     * Set the DbfTableModel in the AppObject of this list that holds
     * the attribute information about this list's objects.
     */
    public void setTable(DbfTableModel dtm) {
        setAppObject(dtm);
    }

    /**
     * Get the DbfTableModel object from the AppObject of this list.
     */
    public DbfTableModel getTable() {
        Object obj = getAppObject();
        if (obj instanceof DbfTableModel) {
            return (DbfTableModel)obj;
        } else {
            return null;
        }
    }

    /**
     * Create a generic DbfTableModel for the contents of this list,
     * where the attributes hold rendering properties for the list
     * contents.  The table is stored in the AppObject member variable
     * of the list.
     */
    public void createTable() {
        // lineWidth, lineColor, fillColor, selectColor We could do
        // stroke info.  Toss space in there for name, or general
        // attribute for later.
        DbfTableModel dtm = new DbfTableModel(5);

    }

    /*
     * Reads the contents of the SHX and SHP files.  The SHX file will
     * be read first by utilizing the ShapeIndex.open method.  This
     * method will return a list of offsets, which the
     * AbstractSupport.open method will use to iterate through the
     * contents of the SHP file.
     * @param shp The url of the SHP file
     * @param shx The url of the SHX file
     * @param drawingAttributes a DrawingAttributes object containing
     * the rendering parameters you might want on the OMGraphics.  The
     * OMGraphic default (black edge, clear fill) will be used if this
     * is null.
     * @param dbf a DbfTableModel, if you want each row of objects
     * from the table (an array), inserted into their associated
     * OMGraphic's appObject.  The dbf will be added to the list
     * appObject, so you can ask it questions later.  If null, no
     * problem.  If the number of records doesn't match the OMGraphic
     * list length, nothing will be done.
     * @return A new EsriGraphicList, null if there is a problem.
     */
    public static EsriGraphicList getEsriGraphicList(URL shp, URL shx, 
                                                     DrawingAttributes drawingAttributes,
                                                     DbfTableModel dbf) {
        EsriGraphicList list = null;
        ShxInputStream xis;
        int[][] indexData = null;

        try {
            InputStream is = shx.openStream();
            xis = new ShxInputStream(is);
            indexData = xis.getIndex();
            is.close();
        } catch (Exception e) {
            Debug.error("EsriGraphicList: Unable to stream SHX file");
            if (Debug.debugging("shape")) {
                e.printStackTrace();
            }
            return null;
        }
        
        //Open and stream shp file
        try {
            InputStream is = shp.openStream();
            ShpInputStream pis = new ShpInputStream(is);
            if (drawingAttributes != null) {
                pis.setDrawingAttributes(drawingAttributes);
            }
            list = pis.getGeometry(indexData);
            is.close();
        } catch (Exception e) {
            Debug.error("EsriGraphicList: Not able to stream SHP file");
            if (Debug.debugging("shape")) {
                e.printStackTrace();
            }
            return null;
        }

        if (list != null && dbf != null && dbf.getRowCount() == list.size()) {
            list.setTable(dbf);
            java.util.Iterator it = list.iterator();
            int count = 0;

            while (it.hasNext()) {
                OMGraphic graphic = (OMGraphic)it.next();
                graphic.setAppObject(dbf.getRecord(count++));
            }
        }

        return list;
    }
}

