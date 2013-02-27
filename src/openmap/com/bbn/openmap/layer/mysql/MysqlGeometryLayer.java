/* ***********************************************************************
 * This layer is for the reading and display of any spatial data retrieved
 * from a MySQL Database (Version 4.1). At this time MySQL 4.1 is available
 * only as alfa release, and represents the first version with support for 
 * the Datatype Geometry. Therefore be careful expecting to much.
 * Usefull information can be found in the chapter 9 of the MySQL Reference 
 * (Spatial Extensions in MySQL) 
 * http://www.mysql.com/documentation/mysql/bychapter/index.html#GIS_spatial_extensions_in_MySQL
 * partially this layer is inspired by Ian Batley's OracleSpatialLayer which 
 * can be found on the on the Open Map website.Thanks Ian.
 * This program is distributed freely and in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Copyright 2003 by the Author
 *
 * Author name: Uwe Baier uwe.baier@gmx.net
 * Version 1.0
 * ***********************************************************************
 */

package com.bbn.openmap.layer.mysql;

/* Java Core */
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * This layer is for the reading and display of any spatial data retrieved from
 * a MySQL Database (Version 4.1). At this time MySQL 4.1 is available only as
 * alfa release, and represents the first version with support for the Datatype
 * Geometry. Therefore, be careful in expecting too much. Usefull information
 * can be found in the chapter 9 of the MySQL Reference (Spatial Extensions in
 * MySQL) http://www.mysql.com/documentation/mysql/bychapter/index.html#
 * GIS_spatial_extensions_in_MySQL partially this layer is inspired by Ian
 * Batley's OracleSpatialLayer which can be found on the on the OpenMap website.
 * Thanks Ian.
 * <p>
 * 
 * MysqlGeometryLayer uses at this stage a set of Classes which wraps the
 * Geometries retrieved from the database. They are thought to be a provisorium
 * until a nice MySQL Geometry API is available. Coordinate values are stored as
 * values of double precision in arrays as a sequence of Latitude/Longitude
 * pairs. This differs from the database where values are stored as X/Y or
 * Easting/Northing pairs.
 * 
 * <p>
 * Properties to be set:
 * 
 * <pre>
 * 
 * 
 *   mygeo.prettyName=&amp;ltYour Layer Name&amp;gt
 *   mygeo.dbUrl=&amp;lt Driver Class &amp;gt eg.  &quot;jdbc:mysql://localhost/openmap?user=me&amp;password=secret&quot;
 *   mygeo.dbClass=&amp;lt Driver Class &amp;gt eg. &quot;com.mysql.jdbc.Driver&quot;
 *   mygeo.geomTable=&amp;ltDatabase Tablename&amp;gt
 *   mygeo.geomColumn=&amp;ltColumn name which contains the geometry&amp;gt
 *   mygeo.pointSymbol=&amp;ltFilename and path for image to use for point objects&amp;gtDefault is 
 *   # Optional Properties - use as required
 *   # NOTE: There are default for each of these 
 *   mygeo.lineColor=&amp;ltColor for lines&amp;gtDefault is red
 *   mygeo.lineWidth=&amp;ltPixel width of lines&amp;gtDefault is 0
 *   mygeo.fillColor=&amp;ltColor of fill&amp;gtDefault is red
 * 
 * 
 * </pre>
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlGeometryLayer extends OMGraphicHandlerLayer {

    /**
     * ; The connection String to use for the jdbc query, e.g.
     * "jdbc:mysql://localhost/openmap?user=me&password=secret"
     */
    protected String dbUrl = null;

    /**
     * The Property to set for the query: <b>dbUrl </b>.
     */
    public static final String dbUrlProperty = "dbUrl";

    /**
     * ; The driver to use.
     */
    protected String dbClass = null;

    /**
     * The property to use for specifying the driver: <b>dbClass </b>
     */
    public static final String dbClassProperty = "dbClass";

    /** Table name which contains the geometry to be used. */
    protected String geomTable = null;

    /**
     * Property to specify geomTable in the Database: <b>geomTable </b>.
     */
    public static final String geomTableProperty = "geomTable";

    /** Column name which contains the geometry to be used. */
    protected String geomColumn = null;

    /**
     * Property to specify geomColumn in the Database: <b>geomColumn </b>
     */
    public static final String geomColumnProperty = "geomColumn";

    /** The point Symbol set by the Properties */
    protected String pointSymbol = "";

    /**
     * Property to specify GIF or image file(symbol) to use for Points:
     * <b>pointSymbol </b>.
     */
    public static final String pointSymbolProperty = "pointSymbol";

    protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

    /**
     * The properties and prefix are managed and decoded here.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        dbClass = properties.getProperty(prefix + dbClassProperty);
        dbUrl = properties.getProperty(prefix + dbUrlProperty);
        geomTable = properties.getProperty(prefix + geomTableProperty);
        geomColumn = properties.getProperty(prefix + geomColumnProperty);
        pointSymbol = properties.getProperty(prefix + pointSymbolProperty);

        if (Debug.debugging("mysql")) {
            Debug.output("MysqlGeometryLayer (" + getName() + ") properties:");
            Debug.output("  " + dbClass);
            Debug.output("  " + dbUrl);
            Debug.output("  " + geomTable);
            Debug.output("  " + geomColumn);
        }

        drawingAttributes.setProperties(prefix, properties);
    }

    public synchronized OMGraphicList prepare() {

        Projection proj = getProjection();

        if (proj == null) {
            Debug.output("MysqlGeometryLayer.prepare: null projection!");
            return null;
        }

        OMGraphicList graphics = new OMGraphicList();

        try {

            Class.forName(dbClass).newInstance();
            Connection conn = DriverManager.getConnection(dbUrl);

            Statement stmt = conn.createStatement();

            Point2D ul = getProjection().getUpperLeft();
            Point2D lr = getProjection().getLowerRight();

            String q = "SELECT ID, AsText(" + geomColumn + ") FROM " + geomTable
                    + " WHERE MBRIntersects(GEO, GeomFromText('Polygon(( " + ul.getX() + " "
                    + ul.getY() + ", " + ul.getX() + " " + lr.getY() + ", " + lr.getX() + " "
                    + lr.getY() + ", " + lr.getX() + " " + ul.getY() + ", " + ul.getX() + " "
                    + ul.getY() + "))'))";

            if (Debug.debugging("mysql")) {
                Debug.output("MysqlGeometryLayer query: " + q);
            }

            ResultSet rs = stmt.executeQuery(q);

            graphics.clear();

            while (rs.next()) {

                String result = rs.getString(2);

                if (Debug.debugging("mysql")) {
                    Debug.output("MysqlGeometryLayer result: " + result);
                }

                MysqlGeometry mg = MysqlWKTGeometryFactory.createGeometry(result);
                OMGraphic omg = createGraphic(mg);
                omg.generate(proj);
                graphics.add(omg);
            }

            rs.close();
            conn.close();
            
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return graphics;
    }

    /**
     * Method createPoint. Renders a Point.
     * 
     * @param myPoint
     */
    protected OMGraphic createPoint(MysqlPoint myPoint) {
        ImageIcon actualPointSymbol = new ImageIcon(pointSymbol);
        OMRaster ompoint = new OMRaster((float) myPoint.getNorthings(), (float) myPoint.getEastings(), actualPointSymbol);

        drawingAttributes.setTo(ompoint);

        return ompoint;
    }

    /**
     * Method createLine. Renders a Linestring Geometry. ToDo: Holes
     * 
     * @param myLine - Database object which will be rendered
     */
    protected OMGraphic createLine(MysqlLine myLine) {

        OMPoly ompoly = new OMPoly(myLine.getCoordinateArray(), OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);

        drawingAttributes.setTo(ompoly);
        return ompoly;
    }

    /**
     * Method createPolygon. Renders a polygon geometry
     * 
     * @param myPoly - Database object which will be rendered
     */
    protected OMGraphic createPolygon(MysqlPolygon myPoly) {
        Vector v = myPoly.getRings();
        int size = v.size();

        OMGraphic ret = null;
        OMPoly ompoly = null;
        OMGraphicList subList = null;

        if (size > 1) {
            subList = new OMGraphicList();
            ret = subList;
        }

        for (int i = 0; i < size; i++) {
            ompoly = new OMPoly((double[]) v.elementAt(i), OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);

            drawingAttributes.setTo(ompoly);

            if (subList != null) {
                subList.add(ompoly);
            } else {
                ret = ompoly;
            }
        }

        return ret;
    }

    /**
     * Method chooses what type of geometry to render.
     * 
     * @param mg Database object which will be rendered
     */
    protected OMGraphic createGraphic(MysqlGeometry mg) {
        OMGraphic ret = null;

        if (mg != null) {
            String type = mg.getType();

            if (type.equals(MysqlGeometry.POINTTYPE)) {
                ret = createPoint((MysqlPoint) mg);
            } else if (type.equals(MysqlGeometry.LINESTRINGTYPE)) {
                ret = createLine((MysqlLine) mg);
            } else if (type.equals(MysqlGeometry.POLYGONTTYPE)) {
                ret = createPolygon((MysqlPolygon) mg);
            } else if (type.equals(MysqlGeometry.MULTIPOINTTYPE)
                    || type.equals(MysqlGeometry.MULTILINESTRINGTYPE)
                    || type.equals(MysqlGeometry.MULTIPOLYGONTYPE)
                    || type.equals(MysqlGeometry.GEOMETRYCOLLECTIONTYPE)) {

                MysqlMulti multi = (MysqlMulti) mg;
                OMGraphicList subList = new OMGraphicList();
                for (int i = 0; i < multi.countElements(); i++) {
                    OMGraphic subRet = null;
                    if (type.equals(MysqlGeometry.MULTIPOINTTYPE)) {
                        subRet = createPoint((MysqlPoint) multi.getElementByIndex(i));
                    } else if (type.equals(MysqlGeometry.MULTILINESTRINGTYPE)) {
                        subRet = createLine((MysqlLine) multi.getElementByIndex(i));
                    } else if (type.equals(MysqlGeometry.MULTIPOLYGONTYPE)) {
                        subRet = createPolygon((MysqlPolygon) multi.getElementByIndex(i));
                    } else if (type.equals(MysqlGeometry.GEOMETRYCOLLECTIONTYPE)) {
                        subRet = createGraphic((MysqlGeometry) multi.getElementByIndex(i));
                    }

                    if (subRet != null) {
                        subList.add(subRet);
                    }
                }
                ret = subList;
            } else {
                // Other types of geometry
                if (Debug.debugging("mysql")) {
                    Debug.output("MysqlGeometryLayer.createGeometry: Geometry type not supported");
                }
            }
        }
        return ret;
    }

    /**
     * Method DoubleToFloat. Used to cast arrays of double precision to float,
     * precision which is internally used by OpenMap. This is ugly, but I
     * preferred to keep the precision of values in the Geometry Classes the
     * same as they are in MySQL Database.
     * 
     * @param d
     * @return float[]
     */
    private float[] DoubleToFloat(double[] d) {
        float[] f = new float[d.length];
        for (int i = 0; i < d.length; i++) {
            f[i] = (float) d[i];
        }
        return f;
    }

}