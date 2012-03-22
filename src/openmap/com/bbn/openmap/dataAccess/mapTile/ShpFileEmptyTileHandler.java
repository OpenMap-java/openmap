
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Properties;

import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * Uses a Shape file to figure out if a tile is over water or over land, so the
 * proper color is used for an empty tile. In addition to all of the properties
 * listed in the SimpleEmptyTileHandler, there are other properties to set up
 * a shape layer to render into empty tiles in an attempt to fill in empty tiles
 * on the fly.
 * <P>
 *
 * <pre>
 * emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.ShpFileEmptyTileHandler
 * land.fillColor=hex RGB color
 * land.lineColor=hex RGB color
 * shpFile=File, resource or URL to shape file for land representation.
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
        MapTileCoordinateTransform mtcTransform, Projection proj) {

        OMRect rect = new OMRect(0, 0, TILE_SIZE, TILE_SIZE);
        BufferedImage bi = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();

        if (zoomLevel < noCoverageZoom) {

            LatLonPoint center = mtcTransform.tileUVToLatLon(new Point2D.Double(x + .5, y + .5), zoomLevel, new LatLonPoint.Double());
            Mercator merc = new Mercator(center, MapTileMaker.getScaleForZoom(zoomLevel), TILE_SIZE, TILE_SIZE);

            backgroundAtts.setTo(rect);
            rect.generate(merc);
            rect.render(g);

            if (shapeStuff != null) {
                shapeStuff.setDrawingAttributes(landAttributes);
                shapeStuff.renderDataForProjection(merc, g);
            }

        } else {

            if (noCoverageAtts == null) {
                return null;
            }

            noCoverageAtts.setTo(rect);
            rect.generate(proj);
            rect.render(g);
        }

        g.dispose();
        return bi;
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
