package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.logging.Logger;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * An EmptyTileHandler that uses DrawingAttributes to create a rectangle to fill
 * in for empty tiles. You can set one of these up using the properties for a
 * MapTileLayer, and those properties will trickle down through the
 * MapTileServer, which will in turn create one of these.
 * <P>
 * 
 * <pre>
 * emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.SimpleEmptyTileHandler
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
 * @author ddietrick
 */
public class SimpleEmptyTileHandler
        implements EmptyTileHandler, PropertyConsumer {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.EmptyTileHandler");

    public final static String BACKGROUND_PROPERTY = "background";
    public final static String NO_COVERAGE_ZOOM_PROPERTY = "noCoverageZoom";
    public final static int TILE_SIZE = 256;

    protected DrawingAttributes backgroundAtts = DrawingAttributes.getDefaultClone();
    protected DrawingAttributes noCoverageAtts = DrawingAttributes.getDefaultClone();
    // Property prefix
    protected String prefix;
    /**
     * The zoom level at which point the EmptyTileHandler will create
     * no-coverage tiles, if defined.
     */
    protected int noCoverageZoom = 20;

    // Needed for ComponentFactory construction
    public SimpleEmptyTileHandler() {
        noCoverageAtts.setLinePaint(OMColor.clear);
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

        if (zoomLevel < noCoverageZoom) {
            backgroundAtts.setTo(rect);
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

    public void setPropertyPrefix(String pref) {
        prefix = pref;
    }

    public String getPropertyPrefix() {
        return prefix;
    }

    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);

        backgroundAtts.setProperties(prefix + BACKGROUND_PROPERTY, props);
        noCoverageAtts.setProperties(prefix + NO_COVERAGE_ZOOM_PROPERTY, props);

        noCoverageZoom = PropUtils.intFromProperties(props, prefix + NO_COVERAGE_ZOOM_PROPERTY, noCoverageZoom);
    }

    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        backgroundAtts.getProperties(props);
        noCoverageAtts.getProperties(props);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        backgroundAtts.getPropertyInfo(props);
        noCoverageAtts.getPropertyInfo(props);
        return props;
    }

    /**
     * @return the backgroundAtts
     */
    public DrawingAttributes getBackgroundAtts() {
        return backgroundAtts;
    }

    /**
     * @param backgroundAtts the backgroundAtts to set
     */
    public void setBackgroundAtts(DrawingAttributes backgroundAtts) {
        this.backgroundAtts = backgroundAtts;
    }

    /**
     * @return the noCoverageAtts
     */
    public DrawingAttributes getNoCoverageAtts() {
        return noCoverageAtts;
    }

    /**
     * Set to null to have nothing returned for tiles outside of the coverage zone.
     * @param noCoverageAtts the noCoverageAtts to set
     */
    public void setNoCoverageAtts(DrawingAttributes noCoverageAtts) {
        this.noCoverageAtts = noCoverageAtts;
    }

    /**
     * @return the noCoverageZoom
     */
    public int getNoCoverageZoom() {
        return noCoverageZoom;
    }

    /**
     * @param noCoverageZoom the noCoverageZoom to set
     */
    public void setNoCoverageZoom(int noCoverageZoom) {
        this.noCoverageZoom = noCoverageZoom;
    }

}
