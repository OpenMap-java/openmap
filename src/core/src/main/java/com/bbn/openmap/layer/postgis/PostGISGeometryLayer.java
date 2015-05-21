/* ***********************************************************************
 * This layer is for the reading and display of any spatial data
 * retrieved from a PostGIS Database. 
 * The code is heavily copied by the MySQL layer and the PostGIS JDBC Driver
 * 
 * Properties to be set:
 * 
 * 
 *  prettyName=&amp;ltYour Layer Name&amp;gt
 *  dbUrl=&amp;lt Driver Class &amp;gt eg.  &quot;jdbc:postgresql_postGIS://localhost/postgres?user=user&amp;password=password&quot;
 *  dbClass=&amp;lt Driver Class &amp;gt eg. &quot;org.postgis.DriverWrapper&quot;
 *  geomTable=&amp;ltDatabase Tablename&amp;gt
 *  geomColumn=&amp;ltColumn name which contains the geometry&amp;gt
 *  pointSymbol=&amp;ltFilename and path for image to use for point objects&amp;gtDefault is 
 *  # Optional Properties - use as required
 *  # NOTE: There are default for each of these 
 *  lineColor=&amp;ltColor for lines&amp;gtDefault is red
 *  lineWidth=&amp;ltPixel width of lines&amp;gtDefault is 0
 *  fillColor=&amp;ltColor of fill&amp;gtDefault is red
 * This program is distributed freely and in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Coded in 2007 by Filippo Di Natale, Milano - Italy
 *
 * Author name: Filippo Di Natale 
 * 
 * ***********************************************************************
 */

package com.bbn.openmap.layer.postgis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.layer.policy.ListResetPCPolicy;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/*  POSTGIS  */
/*  OpenMap  */

/**
 * This layer is for the reading and display of any spatial data retrieved from
 * a PostGIS Database.
 * 
 * Properties to be set:
 * 
 * <pre>
 * prettyName=&amp;ltYour Layer Name&amp;gt 
 * dbUrl=&amp;lt Driver Class &amp;gt
 * eg.
 * &quot;jdbc:postgresql_postGIS://localhost/postgres?user=user&amp;
 * password=password&quot; 
 * dbClass=&amp;lt Driver Class &amp;gt eg.
 * &quot;org.postgis.DriverWrapper&quot; 
 * geomTable=&amp;ltDatabase Tablename&amp;gt 
 * geomColumn=&amp;ltColumn name which contains the geometry&amp;gt 
 * pointSymbol=&amp;ltFilename and path for image to use for point objects&amp;gtDefault is 
 * # Optional Properties - use as required 
 * # NOTE: There are default for each of these 
 * lineColor=&amp;ltColor for lines&amp;gtDefault is red 
 * lineWidth=&amp;ltPixel width of lines&amp;gtDefault is 1
 * fillColor=&amp;ltColor of fill&amp;gtDefault is red
 * 
 * </pre>
 * 
 * Example Usage:
 * 
 * PostGISGeometryLayer postgisLayer = new PostGISGeometryLayer();
 * 
 * <pre>
 * Properties postgisLayerProps = new Properties();
 * postgisLayerProps.put(&quot;prettyName&quot;, &quot;Province&quot;);
 * postgisLayerProps.put(&quot;dbUrl&quot;, &quot;jdbc:postgresql_postGIS://localhost/postgres?user=user&amp;password=password&quot;);
 * postgisLayerProps.put(&quot;dbClass&quot;, &quot;org.postgis.DriverWrapper&quot;);
 * postgisLayerProps.put(&quot;geomTable&quot;, &quot;my_spatial_table&quot;);
 * postgisLayerProps.put(&quot;geomColumn&quot;, &quot;the_geom&quot;);
 * postgisLayer.setProperties(postgisLayerProps);
 * postgisLayer.setVisible(true);
 * mapHandler.add(postgisLayer);
 * </pre>
 * 
 */
public class PostGISGeometryLayer extends OMGraphicHandlerLayer {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.PostGISGeometryLayer");

    private static final long serialVersionUID = 1L;

    /**
     * The Property to set for the query: <b>dbUrl </b>.
     */
    public static final String DB_URL_PROPERTY = "dbUrl";

    /**
     * The property to use for specifing the driver: <b>dbClass </b>
     */
    public static final String DB_CLASS_PROPERTY = "dbClass";

    /**
     * ; The connection String to use for the jdbc query, e.g.
     * "jdbc:mysql://localhost/openmap?user=me&password=secret"
     */
    protected String dbUrl = null;

    /**
     * The driver to use. The default class is org.postgis.DriverWrapper
     */
    protected String dbClass = "org.postgis.DriverWrapper";

    List<FeatureQuery> featureQueryList = new ArrayList<FeatureQuery>();

    /**
     * Property to specify GIF or image file(symbol) to use for Points:
     * <b>pointSymbol </b>.
     */
    public static final String POINT_SYMBOL_PROPERTY = "pointSymbol";

    public PostGISGeometryLayer() {
        setProjectionChangePolicy(new ListResetPCPolicy(this));
        setRenderPolicy(new BufferedImageRenderPolicy(this));
        setMouseModeIDsForEvents(new String[] { SelectMouseMode.modeID });

        // This is a temporary feature query for testing....
        addFeatureQuery(new FeatureQuery("planet_osm_roads", "way", "900913").addWhereRule(new SpatialWhereRule()).addWhereRule(new StringWhereRule("NOT planet_osm_roads.highway is NULL")));
    }

    /**
     * The properties and prefix are managed and decoded here.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        dbClass = properties.getProperty(prefix + DB_CLASS_PROPERTY, dbClass);
        dbUrl = properties.getProperty(prefix + DB_URL_PROPERTY);

        if (logger.isLoggable(Level.FINE)) {
            StringBuilder buf = new StringBuilder();
            buf.append("PostGISGeometryLayer (").append(getName()).append(") properties:");
            buf.append("\n\t").append(dbClass);
            buf.append("\n\t").append(dbUrl);
            logger.fine(buf.toString());
        }
    }

    /**
     * Set the provided Properties file with the properties of this layer.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.setProperty(prefix + DB_CLASS_PROPERTY, dbClass);
        props.setProperty(prefix + DB_URL_PROPERTY, dbUrl);

        return props;
    }

    public synchronized OMGraphicList prepare() {

        Projection proj = getProjection();

        if (proj == null) {
            return null;
        }

        OMGraphicList graphics = new OMGraphicList();

        try {

            Class.forName(dbClass).newInstance();

            Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement();
            ProjectionInfo projInfo = new ProjectionInfo(getProjection());

            for (FeatureQuery featureQuery : featureQueryList) {
                try {
                    graphics.add(featureQuery.queryDatabaseAndMakeOMGraphics(stmt, projInfo));
                } catch (QueryException qe) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("halting database query: " + qe.getMessage());
                    }
                }
            }

            conn.close();

        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Problem with getting connection to " + dbUrl + " with the driver "
                        + dbClass);
                e.printStackTrace();
            }
        }

        return graphics;
    }

    public void addFeatureQuery(FeatureQuery fQuery) {
        featureQueryList.add(fQuery);
    }

}