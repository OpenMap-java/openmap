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

package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The ZoomLevelInfo class is used by the TileMaker and handles how tiles are
 * defined and created for a particular zoom level. It handles any bounds
 * restrictions, what layers should be rendered at this zoom level, and the path
 * to the tiles from the root directory. The properties for this component are:
 * <p>
 * 
 * <pre>
 * #Needed for property file creation of TileMaker
 * zoomMarker.class=com.bbn.openmap.image.ZoomLevelInfo
 * #Optional, to limit tile areas created, in sets of 4, must be in lat,lon order.
 * zoomMarker.bounds=lat lon lat lon
 * zoomMarker.description=Tiles for zoom level 4
 * #Marker names for layers to be rendered, the property prefixes for the layers held by TileMaker
 * zoomMarker.layers=lakes shape
 * zoomMarker.name=ZoomLayerInfo 4
 * zoomMarker.zoomLevel=4
 * zoomMarker.range=0
 * 
 * </pre>
 * 
 * Note that the zoomMarker keyword should be stored in the TileMaker zoomLevels
 * property list.
 * 
 * @author dietrick
 */
public class ZoomLevelMaker extends ZoomLevelInfo {

    public final static String BOUNDS_PROPERTY = "bounds";
    public final static String NAME_PROPERTY = "name";
    public final static String DESCRIPTION_PROPERTY = "description";
    public final static String ZOOM_LEVEL_PROPERTY = "zoomLevel";
    public final static String LAYERS_PROPERTY = "layers";
    public final static String RANGE_PROPERTY = "range";

    public final static int RANGE_NOT_SET = -1;

    protected String name;
    protected String description;
    protected List<String> layers;
    protected List<Layer> layerList;

    /**
     * The range should be equal or smaller than the zoom level, describing how
     * many other zoom levels should be created from the tiles created for this
     * zoom level (scaling). If the range is -1, then it hasn't been set and the
     * zoom level will be returned for this value.
     */
    protected int range = RANGE_NOT_SET;
    protected List<Rectangle2D> bounds = new LinkedList<Rectangle2D>();

    /**
     * Need this to create it from properties
     */
    public ZoomLevelMaker() {
    }

    /**
     * Create a ZoomLevelInfo object that contains information about what map
     * tiles should be created for this zoom level.
     * 
     * @param name
     * @param desc
     * @param zoomLevel
     */
    public ZoomLevelMaker(String name, String desc, int zoomLevel) {
        this.name = name;
        this.description = desc;
        this.zoomLevel = zoomLevel;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        name = props.getProperty(prefix + NAME_PROPERTY, name);
        description = props.getProperty(prefix + DESCRIPTION_PROPERTY, description);
        zoomLevel = PropUtils.intFromProperties(props, prefix + ZOOM_LEVEL_PROPERTY, zoomLevel);
        range = PropUtils.intFromProperties(props, prefix + RANGE_PROPERTY, range);

        String boundsPropertyStrings = props.getProperty(prefix + BOUNDS_PROPERTY);
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

        String layerPropertyStrings = props.getProperty(prefix + LAYERS_PROPERTY);
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
        props.put(prefix + ComponentFactory.ClassNameProperty, getClass().getName());
        props.put(prefix + NAME_PROPERTY, PropUtils.unnull(name));
        props.put(prefix + DESCRIPTION_PROPERTY, PropUtils.unnull(description));
        props.put(prefix + ZOOM_LEVEL_PROPERTY, Integer.toString(zoomLevel));

        if (range != RANGE_NOT_SET) {
            props.put(prefix + RANGE_PROPERTY, Integer.toString(range));
        }

        StringBuffer buf = new StringBuffer();
        for (String layerMarkerName : layers) {
            buf.append(layerMarkerName).append(" ");
        }
        props.put(prefix + LAYERS_PROPERTY, buf.toString().trim());

        buf = new StringBuffer();
        for (Rectangle2D bound : getBounds()) {
            double x = bound.getX();
            double y = bound.getY();
            buf.append(y).append(" ").append(x).append(" ").append((y + bound.getHeight())).append(" ").append((x + bound.getWidth())).append(" ");
        }
        props.put(prefix + BOUNDS_PROPERTY, buf.toString().trim());

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        I18n i18n = Environment.getI18n();

        PropUtils.setI18NPropertyInfo(i18n, props, com.bbn.openmap.dataAccess.mapTile.ZoomLevelMaker.class, NAME_PROPERTY, "Name", "Name for zoom level tiles", null);

        PropUtils.setI18NPropertyInfo(i18n, props, com.bbn.openmap.dataAccess.mapTile.ZoomLevelMaker.class, DESCRIPTION_PROPERTY, "Descroption", "Description for zoom level tiles", null);

        PropUtils.setI18NPropertyInfo(i18n, props, com.bbn.openmap.dataAccess.mapTile.ZoomLevelMaker.class, ZOOM_LEVEL_PROPERTY, "Zoom Level (1-20)", "Number for zoom level", null);

        PropUtils.setI18NPropertyInfo(i18n, props, com.bbn.openmap.dataAccess.mapTile.ZoomLevelMaker.class, BOUNDS_PROPERTY, "Bounds", "Bounds for tile creation (lat lon lat lon)", null);

        PropUtils.setI18NPropertyInfo(i18n, props, com.bbn.openmap.dataAccess.mapTile.ZoomLevelMaker.class, LAYERS_PROPERTY, "Layers", "Space separated marker names for layers used in tiles.", null);

        PropUtils.setI18NPropertyInfo(i18n, props, com.bbn.openmap.dataAccess.mapTile.ZoomLevelMaker.class, RANGE_PROPERTY, "Range", "Zoom level to create tiles down to, using the tiles created at this zoom level.", null);

        return props;
    }

    /**
     * @return the name of this zoom level info.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this zoom level info.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of this zoom level.
     * 
     * @return string description of zoom level
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description for this zoom level.
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the current marker name (property prefix) for layers considered for
     * this zoom level.
     * 
     * @return layers used for zoom level
     */
    public List<String> getLayers() {
        if (layers == null) {
            layers = new LinkedList<String>();
        }
        return layers;
    }

    /**
     * Set the marker names (property prefixes) for the layers that should be
     * considered for this zoom level.
     * 
     * @param layers
     */
    public void setLayers(List<String> layers) {
        this.layers = layers;
    }

    /**
     * Get the List of Layer Objects, if it's been set.
     * 
     * @return List of Layers
     */
    public List<Layer> getLayerList() {
        return layerList;
    }

    /**
     * Set a List of Layer objects. If this is set, the layer marker names won't
     * be used. This is a more programmatic approach, rather than using
     * properties and property prefixes of the layers to set them for this zoom
     * level.
     * 
     * @param layerList
     */
    public void setLayerList(List<Layer> layerList) {
        this.layerList = layerList;
    }

    public void setZoomLevel(int zoomLevel) {
        super.setZoomLevel(zoomLevel);
        scale = -1;
    }

    /**
     * Get bounds, defined as world coordinates (i.e. lat/lon). Does not cross
     * over date line.
     * 
     * @return the bounds for this zoom level
     */
    public List<Rectangle2D> getBounds() {
        return bounds;
    }

    /**
     * Set world coordinate bounds for tiles to be created. Should not cross
     * over date line.
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
     * @return a List of Rectangle2D of uv bounds for this zoom level
     */
    public List<Rectangle2D> getUVBounds(MapTileCoordinateTransform mtct, int zoomLevel) {
        List<Rectangle2D> ret = new LinkedList<Rectangle2D>();
        for (Rectangle2D bounds : getBounds()) {
            ret.add(getUVBounds(bounds, mtct, zoomLevel));
        }

        if (ret.isEmpty()) {
            int etc = getEdgeTileCount();
            ret.add(new Rectangle2D.Double(0, 0, etc, etc));
        }

        return ret;
    }

    /**
     * Create a bounding rectangle given the four coordinates, where the upper
     * left corner of the rectangle is the minimum x, y values and the width and
     * height are the difference between xs and ys.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return Rect2D, properly constructed from coordinates
     */
    public Rectangle2D createProperBounds(double x1, double y1, double x2, double y2) {
        double x = Math.min(x1, x2);
        double y = Math.min(y1, y2);
        double w = Math.abs(x1 - x2);
        double h = Math.abs(y1 - y2);
        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Get the range of this ZoomLevelMaker.
     * 
     * @return the range set for this zlm, or the current zoom level if the
     *         range has not been set.
     */
    public int getRange() {
        if (range <= RANGE_NOT_SET) {
            return getZoomLevel();
        }

        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    /**
     * @param uvx
     * @param uvy
     * @param mapTileMaker
     * @param proj
     * @return byte array of the tile image, raw image bytes.
     */
    public byte[] makeTile(double uvx, double uvy, MapTileMaker mapTileMaker, Proj proj) {
        if (layerList != null) {
            return mapTileMaker.makeTile(uvx, uvy, getZoomLevel(), layerList, proj, mapTileMaker.getBackground());
        }
        return mapTileMaker.makeTile(uvx, uvy, this, proj);
    }
}