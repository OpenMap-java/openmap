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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/esri/EsriLayer.java,v $
// $RCSfile: EsriLayer.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.plugin.esri;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.omGraphics.*;

import com.bbn.openmap.dataAccess.shape.*;
import com.bbn.openmap.dataAccess.shape.input.*;
import com.bbn.openmap.dataAccess.shape.output.*;

/**
 * EsriLayer loads Esri shape file sets from web servers or local file systems,
 * and it enables the creation of shape file sets.
 *
 * To create a file from a remote location:
 * <code><pre>
 *   URL dbf = new URL("http://www.webserver.com/file.dbf");
 *   URL shp = new URL("http://www.webserver.com/file.shp");
 *   URL shx = new URL("http://www.webserver.com/file.shx");
 *   EsriLayer layer = new EsriLayer("name", dbf, shp, shx);
 * </pre></code>
 *
 * To open a shape file set from the local file system:
 * <code><pre>
 *   File dbf = new File("c:/data/file.dbf");
 *   File shp = new File("c:/data/file.shp");
 *   File shx = new File("c:/data/file.shx");
 *   EsriLayer layer = new EsriLayer("name", dbf.toURL(), shp.toURL(), shx.toURL());
 * </pre></code>
 * <code>
 *
 * To create a zero content shape file set from which the user can add shapes at runtime:
 * <code><pre>
 *   EsriLayer layer = new EsriLayer("name", EsriLayer.TYPE_POLYLINE);
 * </pre></code>
 * <code>
 *
 * To add features to an EsriLayer:
 * <code><pre>
 * </pre></code>
 *   OMGraphicList shapeData = new OMGraphicList();
 *   ArrayList tabularData = new ArrayList();
 *   float[] part0 = new float[]{35.0f, -120.0f, -25.0f, -95.0f, 56.0f, -30.0f};
 *   float[] part1 = new float[]{-15.0f, -110.0f, 13.0f, -80.0f, -25.0f, 10.0f};
 *   OMPoly poly0 = new OMPoly(part0, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
 *   OMPoly poly1 = new OMPoly(part1, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
 *   shapeData.add(poly0);  //part 1
 *   shapeData.add(poly1);  //part 2
 *   shapeData.generate(_mapBean.getProjection());
 *   tabularData.add(0, "a value");
 *   layer.addRecord(shapeData, tabularData);
 *   layer.repaint();
 * </pre></code>
 *
 * To configure an EsriLayer through a properties file, specify file references
 * in terms of URLs.
 *
 * To reference a file on Windows 2000:
 * <code><pre>
 *   esri.class = com.bbn.openmap.plugin.esri.EsriLayer
 *   esri.prettyName = Esri Example
 *   esri.dbf = file:///c:/data/shapefile.dbf
 *   esri.shp = file:///c:/data/shapefile.shp
 *   esri.shx = file:///c:/data/shapefile.shx
 * </pre></code>
 *
 * To reference a file on RedHat Linux 6.2:
 * <code><pre>
 *   esri.class = com.bbn.openmap.plugin.esri.EsriLayer
 *   esri.prettyName = Esri Example
 *   esri.dbf = file:///home/dvanauke/resources/shapefile.dbf
 *   esri.shp = file:///home/dvanauke/resources/shapefile.shp
 *   esri.shx = file:///home/dvanauke/resources/shapefile.shx
 * </pre></code>
 *
 * To reference a file on a web server:
 * <code><pre>
 *   esri.class = com.bbn.openmap.plugin.esri.EsriLayer
 *   esri.prettyName = Esri Example
 *   esri.dbf = http://www.webserver.com/shapefile.dbf
 *   esri.shp = http://www.webserver.com/shapefile.shp
 *   esri.shx = http://www.webserver.com/shapefile.shx
 * </pre></code>
 * @author Doug Van Auken
 */
public class EsriLayer extends Layer implements ShapeConstants {

    private EsriGraphicList _list = null;
    private DbfTableModel _model = null;
    private JFrame _tableFrame = null;
    private JScrollPane _pane = null;
    private int _type = -1;
    
    public static final String PARAM_DBF = ".dbf";
    public static final String PARAM_SHX = ".shx";
    public static final String PARAM_SHP = ".shp";
    
    /**
     * Creates an EsriLayer that will be configured through the
     * <code>setProperties()</code> method
     */
    public EsriLayer() {
	System.out.println("in default constructor");
    }
    
    /**
     * Creates an empty EsriLayer, useable for adding features at run-time
     * @param name The name of the layer
     * @param type The type of layer
     * @param columnCount The number of columns in the dbf model
     */
    public EsriLayer(String name, int type, int columnCount) throws Exception {
	setName(name);

	switch (type) {
	case SHAPE_TYPE_POINT:
	    _list = new EsriPointList();
	    break;
	case SHAPE_TYPE_POLYGON:
	    _list = new EsriPolygonList();
	    break;
	case SHAPE_TYPE_POLYLINE:
	    _list = new EsriPolylineList();
	    break;
	default:
	    _list = null;
	}

	_model = new DbfTableModel(columnCount);
    }
    
    /**
     * Creates an EsriLayer from a set of shape files
     * @param label The name of the layer that may be used to
     * reference the layer
     * @param dbf The url referencing the dbf extension file
     * @param shp The url referencing the shp extension file
     * @param shx The url referencing the shx extension file 
     */
    public EsriLayer(String name, URL dbf, URL shp, URL shx) {
	setName(name);
	_list = getGeometry(shp, shx);
	_model = getDbfTableModel(dbf);
    }
    
    
    /**
     * Handles adding records to the geometry list and the DbfTableModel
     * @param graphic An OMGraphic to add the graphics list
     * @param record A record to add to the DbfTableModel
     */
    public void addRecord(OMGraphic graphic, ArrayList record) {
	if (_list != null) {
	    _list.add(graphic);
	}

	if (_model != null) {
	    _model.addRecord(record);
	}
    }
    
    /**
     * Creates a DbfTableModel for a given .dbf file
     * @param dbf The url of the file to retrieve.
     * @return The DbfTableModel for this layer
     */
    private DbfTableModel getDbfTableModel(URL dbf) {
	URL url;
	DbfTableModel model = null;
	try {
	    InputStream is = dbf.openStream();
	    try{
		model = new DbfTableModel(new DbfInputStream(is));
	    }
	    catch(Exception exception) {
		System.out.println(exception);
	    }
	}
	catch(Exception exception) {
	    System.out.println(exception);
	}
	return model;
    }
    
    /**
     * Returns the EsriGraphicList for this layer
     * @return The EsriGraphicList for this layer
     */
    public EsriGraphicList getEsriGraphicList() {
	return _list;
    }
    
    /*
     * Reads the contents of the SHX and SHP files.  The SHX file will
     * be read first by utilizing the ShapeIndex.open method.  This
     * method will return a list of offsets, which the
     * AbstractSupport.open method will use to iterate through the
     * contents of the SHP file.
     * @param sho The url of the SHP file
     * @param shx The url of the SHX file
     * @return A new EsriGraphicList 
     */
    public EsriGraphicList getGeometry(URL shp, URL shx) {
	EsriGraphicList list;
	ShxInputStream xis;
	int[][] indexData = null;
	URL url;
	Vector vector;
	try {
	    InputStream is = shx.openStream();
	    try{
		xis = new ShxInputStream(is);
		indexData = xis.getIndex();
	    }
	    catch(Exception exception) {
		System.out.println(exception);
	    }
	    is.close();
	}
	catch (Exception e) {
	    System.out.println("Unable to stream shx file");
	    return null;
	}
	
	//Open and stream shp file
	try {
	    InputStream is = shp.openStream();
	    try {
		ShpInputStream pis = new ShpInputStream(is);
		list = pis.getGeometry(indexData);
	    }
	    catch (Exception e) {
		System.out.println("Not able to stream SHP file");
		return null;
	    }
	    is.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	return list;
    }
    
    /**
     * Returns the associated table model for this layer
     * @return The associated table model for this layer
     */
    public DbfTableModel getModel() {
	return _model;
    }
    
    /**
     * Returns whether this layer is of type 0 (point), 3 (polyline),
     * or 5(polygon)
     * @return An int representing the type of layer, as specified in
     * Esri's shape file format specification 
     */
    public int getType() {
	return _type;
    }
    
    public void paint(Graphics g) {
	if (_list != null) {
	    _list.render(g);
	}
    }
    
    public void projectionChanged(ProjectionEvent e) {
	if (_list != null) {
	    _list.generate(e.getProjection());
	    repaint();
	}
    }
    
    /**
     * Filters the DbfTableModel given a SQL like string
     * @param query A SQL like string to filter the DbfTableModel
     */
    public void query(String query) {
	//to be implemented
    }
    
    /**
     * Refreshes the display
     * @param projection The projection of the current display
     */
    public void refresh(Projection projection) {
	_list.generate(projection);
	//_model.fireTableStructureChanged();
	repaint();
    }
    
    /**
     * Sets the DbfTableModel
     * @param DbfTableModel The DbfModel to set for this layer
     */
    public void setModel(DbfTableModel model) {
	if(_model != null) {
	    _model = model;
	}
    }
    
    /**
     * Sets the properties for the <code>Layer</code>.
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties properties) {
	super.setProperties(prefix, properties);
	String dbf = properties.getProperty(prefix + PARAM_DBF);
	String shx = properties.getProperty(prefix + PARAM_SHX);
	String shp = properties.getProperty(prefix + PARAM_SHP);
	
	try{
	    setName("testing");
	    _list = getGeometry(new URL(shp), new URL(shx));
	    _model = getDbfTableModel(new URL(dbf));
	}
	catch(Exception exception) {
	    System.out.println(exception);
	}
    }
}
