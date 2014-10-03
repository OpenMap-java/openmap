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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/esri/EsriLayer.java,v $
// $RCSfile: EsriLayer.java,v $
// $Revision: 1.10 $
// $Date: 2006/08/25 15:36:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.esri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.dataAccess.shape.EsriPointList;
import com.bbn.openmap.dataAccess.shape.EsriPolygonList;
import com.bbn.openmap.dataAccess.shape.EsriPolylineList;
import com.bbn.openmap.dataAccess.shape.ShapeConstants;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * EsriLayer loads Esri shape file sets from web servers or local file
 * systems, and it enables the creation of shape file sets.
 * 
 * To create a shape file set from a remote location: <code><pre>
 * URL dbf = new URL(&quot;http://www.webserver.com/file.dbf&quot;);URL shp = new URL(&quot;http://www.webserver.com/file.shp&quot;);
 *    URL shx = new URL(&quot;http://www.webserver.com/file.shx&quot;);
 *    EsriLayer layer = new EsriLayer(&quot;name&quot;, dbf, shp, shx);
 *  
 * </pre></code>
 * 
 * To open a shape file set from the local file system: <code><pre>
 * String dbf = &quot;c:/data/file.dbf&quot;;String shp = &quot;c:/data/file.shp&quot;;
 *    String shx = &quot;c:/data/file.shx&quot;;
 *    EsriLayer layer = new EsriLayer(&quot;name&quot;, dbf, shp, shx, DrawingAttributes.DEFAULT);
 *  
 * </pre></code>
 * <code>
 *
 * To create a zero content shape file set from which the user can add shapes at runtime:
 * <code><pre>
 * EsriLayer layer = new EsriLayer(&quot;name&quot;, EsriLayer.TYPE_POLYLINE);
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
 * in terms of resources, files or URLs.
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
 *   esri.dbf = /home/dvanauke/resources/shapefile.dbf
 *   esri.shp = /home/dvanauke/resources/shapefile.shp
 *   esri.shx = /home/dvanauke/resources/shapefile.shx
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
public class EsriLayer extends OMGraphicHandlerLayer implements ShapeConstants {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    protected DbfTableModel _model = null;
    protected String dbf;
    protected String shx;
    protected String shp;

    protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

    /**
     * Creates an EsriLayer that will be configured through the
     * <code>setProperties()</code> method
     */
    public EsriLayer() {}

    /**
     * Creates an empty EsriLayer, usable for adding features at
     * run-time
     * 
     * @param name The name of the layer
     * @param type The type of layer
     * @param columnCount The number of columns in the dbf model
     */
    public EsriLayer(String name, int type, int columnCount) throws Exception {
        setName(name);

        switch (type) {
        case SHAPE_TYPE_POINT:
            setList(new EsriPointList());
            break;
        case SHAPE_TYPE_POLYGON:
            setList(new EsriPolygonList());
            break;
        case SHAPE_TYPE_POLYLINE:
            setList(new EsriPolylineList());
            break;
        default:
        }

        _model = new DbfTableModel(columnCount);
    }

    /**
     * Creates an EsriLayer from a set of shape files
     * 
     * @param name The name of the layer that may be used to reference
     *        the layer
     * @param dbf The url referencing the dbf extension file
     * @param shp The url referencing the shp extension file
     * @param shx The url referencing the shx extension file
     */
    public EsriLayer(String name, String dbf, String shp, String shx,
            DrawingAttributes da) throws MalformedURLException {
        this(name,
             PropUtils.getResourceOrFileOrURL(dbf),
             PropUtils.getResourceOrFileOrURL(shp),
             PropUtils.getResourceOrFileOrURL(shx),
             da);
    }

    /**
     * Creates an EsriLayer from a set of shape files
     * 
     * @param name The name of the layer that may be used to reference
     *        the layer
     * @param dbf The url referencing the dbf extension file
     * @param shp The url referencing the shp extension file
     * @param shx The url referencing the shx extension file
     */
    public EsriLayer(String name, URL dbf, URL shp, URL shx) {
        this(name, dbf, shp, shx, DrawingAttributes.getDefaultClone());
    }

    /**
     * Creates an EsriLayer from a set of shape files
     * 
     * @param name The name of the layer that may be used to reference
     *        the layer
     * @param dbf The url referencing the dbf extension file
     * @param shp The url referencing the shp extension file
     * @param shx The url referencing the shx extension file
     * @param da DrawingAttributes to use to render the layer
     *        contents.
     */
    public EsriLayer(String name, URL dbf, URL shp, URL shx,
            DrawingAttributes da) {
        setName(name);
        drawingAttributes = da;
        setModel(DbfTableModel.getDbfTableModel(dbf));
        setList(EsriGraphicList.getEsriGraphicList(shp,
                drawingAttributes,
                getModel(), coordTransform));
    }

    /**
     * Handles adding records to the geometry list and the
     * DbfTableModel
     * 
     * @param graphic An OMGraphic to add the graphics list
     * @param record A record to add to the DbfTableModel
     */
    public void addRecord(OMGraphic graphic, ArrayList<Object> record) {
        OMGraphicList _list = getList();
        
        // Associate the record directly in the OMGraphic
        graphic.putAttribute(SHAPE_DBF_INFO_ATTRIBUTE, record);
        
        if (_list != null) {
            _list.add(graphic);
        }

        if (_model != null) {
            _model.addRecord(record);
        }
    }

    /**
     * Returns the EsriGraphicList for this layer
     * 
     * @return The EsriGraphicList for this layer
     */
    public EsriGraphicList getEsriGraphicList() {
        return (EsriGraphicList) getList();
    }

    /**
     * Returns the associated table model for this layer
     * 
     * @return The associated table model for this layer
     */
    public DbfTableModel getModel() {
        return _model;
    }

    /**
     * Returns whether this layer is of type 0 (point), 3 (polyline),
     * or 5(polygon)
     * 
     * @return An int representing the type of layer, as specified in
     *         Esri's shape file format specification
     */
    public int getType() {
        EsriGraphicList egl = getEsriGraphicList();
        if (egl != null) {
            return egl.getType();
        }
        return -1;
    }

    /**
     * Filters the DbfTableModel given a SQL like string
     * 
     * @param query A SQL like string to filter the DbfTableModel
     */
    public void query(String query) {
    //to be implemented
    }

    /**
     * Sets the DbfTableModel
     * 
     * @param model The DbfModel to set for this layer
     */
    public void setModel(DbfTableModel model) {
        _model = model;
    }

    /**
     * Sets the properties for the <code>Layer</code>.
     * 
     * @param prefix the token to prefix the property names
     * @param properties the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        drawingAttributes.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        shp = properties.getProperty(prefix + PARAM_SHP);
        shx = properties.getProperty(prefix + PARAM_SHX);
        dbf = properties.getProperty(prefix + PARAM_DBF);

        if (shp != null) {
            if ((shx == null || shx.length() == 0)) {
                shx = shp.substring(0, shp.lastIndexOf('.') + 1) + PARAM_SHX;
            }

            if ((dbf == null || dbf.length() == 0)) {
                dbf = shp.substring(0, shp.lastIndexOf('.') + 1) + PARAM_DBF;
            }

            try {
                setModel(DbfTableModel.getDbfTableModel(PropUtils.getResourceOrFileOrURL(dbf)));
                setList(EsriGraphicList.getEsriGraphicList(PropUtils.getResourceOrFileOrURL(shp),
                        drawingAttributes,
                        getModel(), coordTransform));
            } catch (Exception exception) {
                Debug.error("EsriLayer(" + getName()
                        + ") exception reading Shape files:\n "
                        + exception.getMessage());
            }
        }
    }

    /**
     * PropertyConsumer method.
     * 
     * @param properties the <code>Properties</code> object
     * @return the <code>Properties</code> object
     */
    public Properties getProperties(Properties properties) {
        properties = super.getProperties(properties);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        properties.setProperty(prefix + PARAM_DBF, PropUtils.unnull(dbf));
        properties.setProperty(prefix + PARAM_SHX, PropUtils.unnull(shx));
        properties.setProperty(prefix + PARAM_SHP, PropUtils.unnull(shp));

        // Need to make sure they line up.
        drawingAttributes.setPropertyPrefix(getPropertyPrefix());
        drawingAttributes.getProperties(properties);

        return properties;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer. The key
     * for each property should be the raw property name (without a
     * prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     * 
     * @param list a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.put(PARAM_DBF, "Location URL of the dbf file.");
        list.put(PARAM_DBF + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        list.put(PARAM_SHX, "Location URL of the shx file.");
        list.put(PARAM_SHX + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        list.put(PARAM_SHP, "Location URL of the shp file.");
        list.put(PARAM_SHP + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        drawingAttributes.getPropertyInfo(list);

        list.put(initPropertiesProperty, PARAM_SHP + " " + PARAM_SHX + " "
                + PARAM_DBF + " " + drawingAttributes.getInitPropertiesOrder());

        return list;
    }

}

