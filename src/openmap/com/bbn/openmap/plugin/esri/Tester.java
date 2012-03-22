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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/esri/Tester.java,v $
// $RCSfile: Tester.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.esri;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.dataAccess.shape.EsriPoint;
import com.bbn.openmap.dataAccess.shape.EsriPolygon;
import com.bbn.openmap.dataAccess.shape.EsriPolygonList;
import com.bbn.openmap.dataAccess.shape.EsriPolyline;
import com.bbn.openmap.dataAccess.shape.EsriPolylineList;
import com.bbn.openmap.dataAccess.shape.ShapeConstants;
import com.bbn.openmap.dataAccess.shape.output.DbfOutputStream;
import com.bbn.openmap.dataAccess.shape.output.ShpOutputStream;
import com.bbn.openmap.dataAccess.shape.output.ShxOutputStream;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * A class to test the creation of shape file sets
 * 
 * @author Doug Van Auken
 */
public class Tester extends JFrame implements ShapeConstants {

    /**
     * Use default constructor set up UI
     */
    public Tester() {
        JButton cmdPolylineTest = new JButton("Test Polyline");
        cmdPolylineTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    testPolyline();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        JButton cmdPolygonTest = new JButton("Test Polygon");
        cmdPolygonTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    testPolygon();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        JButton cmdPointTest = new JButton("Test Point");
        cmdPointTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    testPoint();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        JPanel centerPanel = new JPanel();
        JPanel southPanel = new JPanel();
        southPanel.add(cmdPolylineTest);
        southPanel.add(cmdPolygonTest);
        southPanel.add(cmdPointTest);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        setSize(400, 300);
        setVisible(true);
    }

    /**
     * Creates a polyline shape file set
     */
    public void testPolyline() throws Exception {
        /*
         * Create the layer. Parameter 1 = layer name Param 2 = layer
         * type Param3 = number of columns to allocate in
         * DbfTableColumnModel
         */
        EsriLayer layer = new EsriLayer("Drawable Layer", SHAPE_TYPE_POLYLINE, 2);
        DbfTableModel model = layer.getModel();

        //Setup table structure
        //Setup column 0 to be character
        model.setDecimalCount(0, (byte) 0);
        model.setLength(0, (byte) 20);
        model.setColumnName(0, "Column1");
        model.setType(0, (byte) DbfTableModel.TYPE_CHARACTER);

        //Setup column 1 to be numeric
        model.setDecimalCount(1, (byte) 3);
        model.setLength(1, (byte) 20);
        model.setColumnName(1, "Column2");
        model.setType(1, (byte) DbfTableModel.TYPE_NUMERIC);

        addPolylineRecord1(layer);
        addPolylineRecord2(layer);
        addPolylineRecord3(layer);

        EsriGraphicList list = layer.getEsriGraphicList();

        ShpOutputStream pos = new ShpOutputStream(new FileOutputStream("polylinetest0.shp"));
        int[][] indexData = pos.writeGeometry(list);

        ShxOutputStream xos = new ShxOutputStream(new FileOutputStream("polylinetest0.shx"));
        xos.writeIndex(indexData, list.getType(), list.getExtents());

        DbfOutputStream dos = new DbfOutputStream(new FileOutputStream("polylinetest0.dbf"));
        dos.writeModel(model);
    }

    /**
     * Creates a polygon shape file set
     */
    public void testPolygon() throws Exception {
        /*
         * Create the layer. Parameter 1 = layer name Param 2 = layer
         * type Param3 = number of columns to allocate in
         * DbfTableColumnModel
         */
        EsriLayer layer = new EsriLayer("Polygon Layer", SHAPE_TYPE_POLYGON, 2);
        DbfTableModel model = layer.getModel();

        //Setup table structure
        //Setup column 0 to be character
        model.setDecimalCount(0, (byte) 0);
        model.setLength(0, (byte) 20);
        model.setColumnName(0, "Column1");
        model.setType(0, (byte) DbfTableModel.TYPE_CHARACTER);

        //Setup column 1 to be numeric
        model.setDecimalCount(1, (byte) 3);
        model.setLength(1, (byte) 20);
        model.setColumnName(1, "Column2");
        model.setType(1, (byte) DbfTableModel.TYPE_NUMERIC);

        addPolygonRecord1(layer);

        EsriGraphicList list = layer.getEsriGraphicList();

        ShpOutputStream pos = new ShpOutputStream(new FileOutputStream("polygontest5.shp"));
        int[][] indexData = pos.writeGeometry(list);

        ShxOutputStream xos = new ShxOutputStream(new FileOutputStream("polygontest5.shx"));
        xos.writeIndex(indexData, list.getType(), list.getExtents());

        DbfOutputStream dos = new DbfOutputStream(new FileOutputStream("polygontest5.dbf"));
        dos.writeModel(model);
    }

    /**
     * Creates a point shape file set
     */
    public void testPoint() throws Exception {
        /*
         * Create the layer. Parameter 1 = layer name Param 2 = layer
         * type Param3 = number of columns to allocate in
         * DbfTableColumnModel
         */
        EsriLayer layer = new EsriLayer("Point Layer", SHAPE_TYPE_POINT, 2);
        DbfTableModel model = layer.getModel();

        //Setup table structure
        //Setup column 0 to be character
        model.setDecimalCount(0, (byte) 0);
        model.setLength(0, (byte) 20);
        model.setColumnName(0, "Column1");
        model.setType(0, (byte) DbfTableModel.TYPE_CHARACTER);

        //Setup column 1 to be numeric
        model.setDecimalCount(1, (byte) 3);
        model.setLength(1, (byte) 20);
        model.setColumnName(1, "Column2");
        model.setType(1, (byte) DbfTableModel.TYPE_NUMERIC);

        addPoints(layer);

        EsriGraphicList list = layer.getEsriGraphicList();

        ShpOutputStream pos = new ShpOutputStream(new FileOutputStream("pointtest0.shp"));
        int[][] indexData = pos.writeGeometry(list);

        ShxOutputStream xos = new ShxOutputStream(new FileOutputStream("pointtest0.shx"));
        xos.writeIndex(indexData, list.getType(), list.getExtents());

        DbfOutputStream dos = new DbfOutputStream(new FileOutputStream("pointtest0.dbf"));
        dos.writeModel(model);
    }

    /**
     * Creates an EsriPolyline object with two parts
     */
    public void addPolylineRecord1(EsriLayer layer) {
        OMGraphicList shapeData = new EsriPolylineList();
        double[] part0 = new double[] { 35.0, -120.0, -25.0, -95.0, 56.0,
                -30.0 };
        double[] part1 = new double[] { -15.0, -110.0, 13.0, -80.0, -25.0,
                10.0 };
        EsriPolyline poly0 = new EsriPolyline(part0, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
        EsriPolyline poly1 = new EsriPolyline(part1, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
        shapeData.add(poly0); //part 1
        shapeData.add(poly1); //part 2

        ArrayList tabularData = new ArrayList();
        tabularData.add(0, "first value");
        tabularData.add(1, new Double(12.54));

        layer.addRecord(shapeData, tabularData);
    }

    /**
     * Creates an EsriPolyline object with one part
     */
    public void addPolylineRecord2(EsriLayer layer) {
        OMGraphicList shapeData = new EsriPolylineList();
        double[] part0 = new double[] { 12.0f, -175.0f, -30.0f, 85.0f, 25.0f,
                15.0f };
        EsriPolyline poly0 = new EsriPolyline(part0, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
        shapeData.add(poly0); //part 1

        ArrayList tabularData = new ArrayList();
        tabularData.add(0, "second value");
        tabularData.add(1, new Double(30.215));

        layer.addRecord(shapeData, tabularData);
    }

    /**
     * Creates an EsriPolyline object with three parts
     */
    public void addPolylineRecord3(EsriLayer layer) {
        //Create geometry data
        OMGraphicList shapeData = new EsriPolylineList();
        double[] part0 = new double[] { -25.0f, -140.0f, -50.0f, -95.0f, 65.0f,
                51.0f };
        double[] part1 = new double[] { -10.0f, -130.0f, -47.0f, -101.0f, 71.0f,
                59.0f };
        double[] part2 = new double[] { -15.0f, -151.0f, -49.0f, -100.0f, 76.0f,
                41.0f };

        EsriPolyline poly0 = new EsriPolyline(part0, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
        EsriPolyline poly1 = new EsriPolyline(part1, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
        EsriPolyline poly2 = new EsriPolyline(part2, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);

        shapeData.add(poly0);
        shapeData.add(poly1);
        shapeData.add(poly2);

        //Create tabular data (Be sure that the structure matches the
        // way the model is setup
        //(I will be implementing error handling in a second
        // version). Also, use Double
        //for number data types and String for other dat types
        ArrayList tabularData = new ArrayList();
        tabularData.add(0, "third value");
        tabularData.add(1, new Double(20.1578));

        //Add geometry and tabular data
        layer.addRecord(shapeData, tabularData);
    }

    /**
     * Creates and EsriPolygon object with one part
     */
    public void addPolygonRecord1(EsriLayer layer) {
        OMGraphicList shapeData = new EsriPolygonList();
        //float[] part0 = new float[]{45.0f, -120.0f, -45.0f, -95.0f,
        // 10.0f, 30.0f, 45.0f, -120.0f};
        double[] part0 = new double[] { 45.0f, -70.0f, 30.0f, -30.0f, 10.0f,
                -80.0f, 30.0f, -120.0f, 45.0f, -70.0f };
        //    float[] part1 = new float[]{35.0f, -70.0f, 30.0f, -110.0f,
        // 15.0f, -80.0f, 30.0f, -40.0f, 35.0f, 70.0f};
        //    float[] part2 = new float[]{25.0f, -70.0f, 30.0f, -100.0f,
        // 20.0f, -80.0f, 30.0f, -50.0f, 25.0f, 70.0f};

        EsriPolygon poly0 = new EsriPolygon(part0, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
        //    EsriPolygon poly1 = new EsriPolygon(part0,
        // OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
        //    EsriPolygon poly2 = new EsriPolygon(part0,
        // OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);

        shapeData.add(poly0); //part 0
        //    shapeData.add(poly1);
        //    shapeData.add(poly2);

        ArrayList tabularData = new ArrayList();
        tabularData.add(0, "first value");
        tabularData.add(1, new Double(12.54));

        layer.addRecord(shapeData, tabularData);
    }

    /**
     * Creates three EsriPoint objects
     */
    public void addPoints(EsriLayer layer) {
        ArrayList tabularData0 = new ArrayList();
        tabularData0.add(0, "first value");
        tabularData0.add(1, new Double(10.54));
        layer.addRecord(new EsriPoint(30.0f, -90.0f), tabularData0);

        ArrayList tabularData1 = new ArrayList();
        tabularData1.add(0, "second value");
        tabularData1.add(1, new Double(20.54));
        layer.addRecord(new EsriPoint(45.0f, -70.0f), tabularData1);

        ArrayList tabularData2 = new ArrayList();
        tabularData2.add(0, "third value");
        tabularData2.add(1, new Double(30.54));
        layer.addRecord(new EsriPoint(35.0f, -120.0f), tabularData2);
    }

    public static void main(String[] args) {
        new Tester();
    }
}

