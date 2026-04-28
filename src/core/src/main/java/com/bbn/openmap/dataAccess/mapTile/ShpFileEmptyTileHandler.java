package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Properties;

import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * Uses a Shape file to figure out if a tile is over water or over land, so the
 * proper color is used for an empty tile. In addition to all of the properties
 * listed in the SimpleEmptyTileHandler, there are other properties to set up a
 * shape layer to render into empty tiles in an attempt to fill in empty tiles
 * on the fly.
 * <P>
 * 
 * <pre>
 * emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.ShpFileEmptyTileHandler
 * shpFile=File, resource or URL to shape file for land representation.
 * 
 * # Properties to set how the shp file contents are rendered.
 * land.fillColor=hex RGB color
 * land.lineColor=hex RGB color
 * land.fillPattern=path to resource, file or URL of pattern to use for tile fill.
 * 
 * # From SimpleEmptyTileHandler superclass, handling the 'water'
 * # clear by default if not specified
 * background.fillColor=hex RGB color
 * background.lineColor=hex RGB color
 * background.fillPattern=path to resource, file or URL of pattern to use for tile fill.
 * 
 * # Zoom level to start using noCoverage attributes.  Is 0 by default if the shape file 
 * # is not specified.  If the shape file is specified and this isn't the zoom level 
 * # will be set to 20.
 * noCoverageZoom=zoom level when you don't want empty tiles, you want no coverage tiles
 * 
 * # How to render standard empty tiles, will be clear if not defined
 * noCoverage.fillColor=hex RGB color
 * noCoverage.lineColor=hex RGB color
 * noCoverage.fillPattern=path to resource, file or URL of pattern to use for tile fill.
 * </pre>
 * 
 * 
 * @author ddietrick
 */
public class ShpFileEmptyTileHandler extends SimpleEmptyTileHandler {
    public final static String LAND_ATTRIBUTES_PROPERTY = "land";
    public final static String SHP_FILE_PROPERTY = "shpFile";
    protected ShapeLayer shapeStuff;
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
                                              MapTileCoordinateTransform mtcTransform,
                                              Projection proj) {

        if (shapeStuff != null && zoomLevel < noCoverageZoom) {

            BufferedImage bi = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.getGraphics();

            LatLonPoint center = mtcTransform.tileUVToLatLon(new Point2D.Double(x + .5, y + .5), zoomLevel, new LatLonPoint.Double());
            Mercator merc = new Mercator(center, mtcTransform.getScaleForZoom(zoomLevel), TILE_SIZE, TILE_SIZE);

            ((Graphics2D) g).setPaint(backgroundAtts.getFillPaint());
            g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);

            if (shapeStuff != null) {
                shapeStuff.setDrawingAttributes(landAttributes);
                shapeStuff.renderDataForProjection(merc, g);
            }

            g.dispose();
            return bi;

        } else {
            return super.getImageForEmptyTile(imagePath, x, y, zoomLevel, mtcTransform, proj);
        }
    }

    public ShapeLayer getShapeStuff() {
        return shapeStuff;
    }

    public void setShapeStuff(ShapeLayer shapeStuff) {
        this.shapeStuff = shapeStuff;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        landAttributes.setProperties(prefix + LAND_ATTRIBUTES_PROPERTY, props);

        String shapeFileName = props.getProperty(prefix + SHP_FILE_PROPERTY);
        if (shapeFileName != null) {
            shapeStuff = new ShapeLayer(shapeFileName);
            shapeStuff.setDrawingAttributes(landAttributes);

            // If noCoverageZoom property is not set and the shape file is, then
            // make the default action to show the shape file.
            if (props.getProperty(prefix + NO_COVERAGE_ZOOM_PROPERTY) == null) {
                noCoverageZoom = 20;
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        landAttributes.getProperties(props);
        return props;
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
