package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import com.bbn.openmap.dataAccess.shape.ShapeGeoIndex;
import com.bbn.openmap.geo.GeoPoint;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * Uses a Shape file to figure out if a tile is over water or over land, so the
 * proper color is used for an empty tile. In addition to all of the properties
 * listed in the SimpleEmptyTileHandler, there are other properties to set up
 * the ShapeGeoIndex and the land drawing attribute properties.
 * <P>
 * 
 * <pre>
 * emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.ShpFileEmptyTileHandler
 * land.fillColor=hex RGB color
 * land.lineColor=hex RGB color
 * shpFile=File, resource or URL to shape file for land representation.  Tile locations will be tested against this file to determine if they are over land or sea.
 * 
 * # From SimpleEmptyTileHandler superclass:
 * # clear with black edges by default if not specified
 * background.fillColor=hex RGB color
 * background.lineColor=hex RGB color
 * # if not specified for levels with no tiles at all, background will look blocky
 * noCoverageZoom=zoom level when you don't want empty tiles, you want no coverage tiles
 * # optional, will be clear otherwise
 * noCoverage.fillColor=hex RGB color
 * noCoverage.lineColor=hex RGB color
 * noCoverage.fillPattern=path to resource, file or URL of pattern to use for tile fill.
 * </pre>
 * 
 * 
 * @author ddietrick
 */
public class ShpFileEmptyTileHandler
        extends SimpleEmptyTileHandler {

    public final static String LAND_ATTRIBUTES_PROPERTY = "land";
    public final static String SHP_FILE_PROPERTY = "shpFile";
    protected ShapeGeoIndex geoIndex;
    protected DrawingAttributes landAttributes = DrawingAttributes.getDefaultClone();

    public ShpFileEmptyTileHandler() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.dataAccess.mapTile.EmptyTileHandler#getOMGraphicForEmptyTile
     * (java.lang.String, int, int, int,
     * com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform,
     * com.bbn.openmap.proj.Projection)
     */
    public BufferedImage getImageForEmptyTile(String imagePath, int x, int y, int zoomLevel,
                                              MapTileCoordinateTransform mtcTransform, Projection proj) {

        Point2D pnt = new Point2D.Double(x, y);
        Point2D tileUL = mtcTransform.tileUVToLatLon(pnt, zoomLevel);
        
        /*
         * We just need one point in the tile, since it's all empty and either over land or sea.
         */

        OMRect rect = new OMRect(0, 0, TILE_SIZE, TILE_SIZE);

        if (zoomLevel < noCoverageZoom) {
            if (isOverLand(tileUL.getY(), tileUL.getX())) {
                landAttributes.setTo(rect);
            } else {
                backgroundAtts.setTo(rect);
            }
        } else {
            
            if (noCoverageAtts == null) {
                return null;
            }
            
            noCoverageAtts.setTo(rect);
        }

        rect.generate(proj);
        
        BufferedImage bi = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();
        rect.render(g);
        g.dispose();

        return bi;
    }

    /**
     * If there is no geoIndex set, this returns false.
     * 
     * @param lat latitude, decimal degrees
     * @param lon longitude, decimal degrees
     * @return true if the point is a hit on any object in the shape file.
     */
    protected boolean isOverLand(double lat, double lon) {
        if (geoIndex == null) {
            return false;
        }

        GeoPoint geoPoint = new GeoPoint.Impl(lat, lon);
        Iterator iterator = geoIndex.getIntersections(geoPoint);

        return iterator.hasNext();
    }

    /**
     * Creates the Geo Index from a shape file. Geo index gets set to null if
     * null is passed in, or if there's a problem reading the shape file.
     * 
     * @param fileName file, resource or URL to shape file (.shp)
     */
    public void setGeoIndex(String fileName) {
        if (fileName != null) {
            try {
                URL shapeURL = PropUtils.getResourceOrFileOrURL(fileName);
                geoIndex = new ShapeGeoIndex.Builder(shapeURL).create();
                return;
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.info("can't find the shape file");
            }
        }
        // Catch-all if anything goes wrong.
        geoIndex = null;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String shapeFileName = props.getProperty(prefix + SHP_FILE_PROPERTY);
        if (shapeFileName != null) {
            setGeoIndex(shapeFileName);
        }

        landAttributes.setProperties(prefix + LAND_ATTRIBUTES_PROPERTY, props);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        landAttributes.getProperties(props);
        return props;
    }

    /**
     * @return the geoIndex
     */
    public ShapeGeoIndex getGeoIndex() {
        return geoIndex;
    }

    /**
     * @param geoIndex the geoIndex to set
     */
    public void setGeoIndex(ShapeGeoIndex geoIndex) {
        this.geoIndex = geoIndex;
    }

    /**
     * @return the landAttributes
     */
    public DrawingAttributes getLandAttributes() {
        return landAttributes;
    }

    /**
     * @param landAttributes the landAttributes to set
     */
    public void setLandAttributes(DrawingAttributes landAttributes) {
        this.landAttributes = landAttributes;
    }
}
