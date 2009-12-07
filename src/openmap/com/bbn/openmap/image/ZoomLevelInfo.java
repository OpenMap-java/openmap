//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.image;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The ZoomLevelInfo class is used by the TileMaker and handles how tiles are
 * defined and created for a particular zoom level. It handles any bounds
 * restrictions, what layers should be rendered at this zoom level, and the path
 * to the tiles from the root directory. The properties for this component are:
 * 
 * <pre>
 * #Needed for property file creation of TileMaker
 * zoomMarker.class=com.bbn.openmap.image.ZoomLevelInfo
 * #Optional, to limit tile areas created, in sets of 4
 * zoomMarker.bounds=lat lon lat lon lat lon lat lon
 * zoomMarker.description=Tiles for zoom level 4
 * #Marker names for layers to be rendered, the property prefixes for the layers held by TileMaker
 * zoomMarker.layers=lakes shape
 * zoomMarker.name=ZoomLayerInfo 4
 * zoomMarker.zoomLevel=4
 * 
 * </pre>
 * 
 * Note that the zoomMarker keyword should be stored in the TileMaker zoomLevels
 * property list.
 * 
 * @author dietrick
 */
public class ZoomLevelInfo extends OMComponent {

    public final static String BOUNDS_PROPERTY = "bounds";
    public final static String NAME_PROPERTY = "name";
    public final static String DESCRIPTION_PROPERTY = "description";
    public final static String ZOOM_LEVEL_PROPERTY = "zoomLevel";
    public final static String LAYERS_PROPERTY = "layers";

    protected String name;
    protected String description;
    protected List<String> layers;
    protected int zoomLevel = 0;
    protected float scale = -1f;
    protected List<Rectangle2D> bounds = new LinkedList<Rectangle2D>();

    /**
     * 
     */
    public ZoomLevelInfo() {}
    
    /**
     * 
     * @param name
     * @param desc
     * @param zoomLevel
     */
    public ZoomLevelInfo(String name, String desc, int zoomLevel) {
       this.name = name;
       this.description = desc;
       this.zoomLevel = zoomLevel;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        name = props.getProperty(prefix + NAME_PROPERTY, name);
        description = props.getProperty(prefix + DESCRIPTION_PROPERTY,
                description);
        zoomLevel = PropUtils.intFromProperties(props, prefix
                + ZOOM_LEVEL_PROPERTY, zoomLevel);

        String boundsPropertyStrings = props.getProperty(prefix
                + BOUNDS_PROPERTY);
        if (boundsPropertyStrings != null) {
            Vector<String> boundsStrings = PropUtils.parseSpacedMarkers(boundsPropertyStrings);
            int count = 0;
            while (boundsStrings != null && !boundsStrings.isEmpty()
                    && boundsStrings.size() >= count + 4) {
                double lat1 = Double.parseDouble(boundsStrings.get(count));
                double lon1 = Double.parseDouble(boundsStrings.get(count + 1));
                double lat2 = Double.parseDouble(boundsStrings.get(count + 2));
                double lon2 = Double.parseDouble(boundsStrings.get(count + 3));

                bounds.add(createProperBounds(lon1, lat1, lon2, lat2));
                count += 4;
            }
        }

        String layerPropertyStrings = props.getProperty(prefix
                + LAYERS_PROPERTY);
        if (layerPropertyStrings != null) {
            Vector<String> layerStrings = PropUtils.parseSpacedMarkers(layerPropertyStrings);
            if (layerStrings != null && !layerStrings.isEmpty()) {
                getLayers().addAll(layerStrings);
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + ComponentFactory.ClassNameProperty,
                getClass().getName());
        props.put(prefix + NAME_PROPERTY, PropUtils.unnull(name));
        props.put(prefix + DESCRIPTION_PROPERTY, PropUtils.unnull(description));
        props.put(prefix + ZOOM_LEVEL_PROPERTY, Integer.toString(zoomLevel));

        StringBuffer buf = new StringBuffer();
        for (String layerMarkerName : layers) {
            buf.append(layerMarkerName + " ");
        }
        props.put(prefix + LAYERS_PROPERTY, buf.toString().trim());

        buf = new StringBuffer();
        for (Rectangle2D bound : getBounds()) {
            double x = bound.getX();
            double y = bound.getY();
            buf.append(y + " " + x + " " + (y + bound.getHeight()) + " "
                    + (x + bound.getWidth()) + " ");
        }
        props.put(prefix + BOUNDS_PROPERTY, buf.toString().trim());

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        I18n i18n = Environment.getI18n();

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                com.bbn.openmap.image.ZoomLevelInfo.class,
                NAME_PROPERTY,
                "Name",
                "Name for zoom level tiles",
                null);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                com.bbn.openmap.image.ZoomLevelInfo.class,
                DESCRIPTION_PROPERTY,
                "Descroption",
                "Description for zoom level tiles",
                null);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                com.bbn.openmap.image.ZoomLevelInfo.class,
                ZOOM_LEVEL_PROPERTY,
                "Zoom Level (1-20)",
                "Number for zoom level",
                null);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                com.bbn.openmap.image.ZoomLevelInfo.class,
                BOUNDS_PROPERTY,
                "Bounds",
                "Bounds for tile creation (lat lon lat lon)",
                null);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                com.bbn.openmap.image.ZoomLevelInfo.class,
                LAYERS_PROPERTY,
                "Layers",
                "Space separated marker names for layers used in tiles.",
                null);

        return props;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLayers() {
        if (layers == null) {
            layers = new LinkedList<String>();
        }
        return layers;
    }

    public void setLayers(List<String> layers) {
        this.layers = layers;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public float getScale() {
        if (scale < 0) {
            Projection proj = new Mercator(new LatLonPoint.Double(), 1000000, 500, 500);
            scale = TileMaker.getScaleForZoomAndProjection(proj, zoomLevel);
        }
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getEdgeTileCount() {
        return (int) Math.pow(2, zoomLevel);
    }

    /**
     * Get bounds, defined as world coordinates (i.e. lat/lon). Does not cross
     * over dateline.
     * 
     * @return
     */
    public List<Rectangle2D> getBounds() {
        return bounds;
    }

    /**
     * Set world coordinate bounds for tiles to be created. Should not cross
     * over dateline.
     * 
     * @param bounds No checks performed - x, y have to be the min, height and
     *        width must not exceed boundary limits (lat +/- 85, lon +/- 180)
     *        when added to x, y.
     */
    public void addBounds(Rectangle2D bounds) {
        this.bounds.add(bounds);
    }

    /**
     * Get the bounds as defined as UV tile limits.
     * 
     * @return
     */
    public List<Rectangle2D> getUVBounds() {
        List<Rectangle2D> ret = new LinkedList<Rectangle2D>();
        for (Rectangle2D bounds : getBounds()) {

            double x = bounds.getX();
            double y = bounds.getY();
            double h = bounds.getHeight();
            double w = bounds.getWidth();
            Point2D point1 = new Point2D.Double(x, y + h);
            Point2D point2 = new Point2D.Double(x + w, y);
            Point2D uluv = TileMaker.latLonToTileUV(point1, getZoomLevel());
            Point2D lruv = TileMaker.latLonToTileUV(point2, getZoomLevel());
            x = Math.floor(uluv.getX());
            y = Math.floor(uluv.getY());
            w = Math.ceil(lruv.getX() - x);
            h = Math.ceil(lruv.getY() - y);

            ret.add(new Rectangle2D.Double(x, y, w, h));

        }

        if (ret.size() == 0) {
            int etc = getEdgeTileCount();
            ret.add(new Rectangle2D.Double(0, 0, etc, etc));
        }

        return ret;
    }

    public String formatParentDirectoryName(String rootDir, int uvx, int uvy) {
        return rootDir + "/" + getZoomLevel() + "/" + uvx;
    }

    public String formatImageFilePath(String rootDir, int uvx, int uvy) {
        return rootDir + "/" + getZoomLevel() + "/" + uvx + "/" + uvy;
    }

    public Rectangle2D createProperBounds(double x1, double y1, double x2,
                                          double y2) {
        double x = Math.min(x1, x2);
        double y = Math.min(y1, y2);
        double w = Math.abs(x1 - x2);
        double h = Math.abs(y1 - y2);
        return new Rectangle2D.Double(x, y, w, h);
    }
}