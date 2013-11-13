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

package com.bbn.openmap.layer.imageTile;

import java.awt.Container;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JSlider;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.dataAccess.mapTile.MapTileFactory;
import com.bbn.openmap.dataAccess.mapTile.MapTileRequester;
import com.bbn.openmap.dataAccess.mapTile.ServerMapTileFactory;
import com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * A Layer that uses a MapTileFactory to display information (tiles) on the map.
 * Properties for this layer look like this:
 * 
 * <pre>
 * 
 * tiles.class=com.bbn.openmap.layer.imageTile.MapTileLayer
 * tiles.prettyName=TILES
 * tiles.tileFactory=com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory
 * tiles.rootDir=root_directory_of_tiles
 * #optional, .png is default
 * tiles.fileExt=.png
 * tiles.cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * # transform for naming convention of tiles default is OSMMapTileCoordinateTransform, but it depends on the source of tiles.  GDAL is TSMMapTileCoordinateTransform
 * tiles.mapTileTransform=com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform, or com.bbn.openmap.dataAccess.mapTile.TMSMapTileCoordinateTransform
 * 
 * </pre>
 * 
 * You can use a server that provides image tiles:
 * 
 * <pre>
 * 
 * tiles.class=com.bbn.openmap.layer.imageTile.MapTileLayer
 * tiles.prettyName=TILES
 * tiles.tileFactory=com.bbn.openmap.dataAccess.mapTile.ServerMapTileFactory
 * tiles.rootDir=URL root directory of tiles
 * # a local location to cache tiles, to reduce load on server.
 * tiles.localCacheRootDir=/data/tiles/osmtiles
 * 
 * # other properties are the same.
 * tiles.fileExt=.png
 * tiles.cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * # transform for naming convention of tiles default is OSMMapTileCoordinateTransform, but it depends on the source of tiles.  GDAL is TSMMapTileCoordinateTransform
 * mapTileTransform=com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform, or com.bbn.openmap.dataAccess.mapTile.TMSMapTileCoordinateTransform
 * 
 * </pre>
 * 
 * The rootDir property can be defined as a pattern, with the zoom level z, x
 * tile coordinate and y tile coordinate set using {z}{x}{y} for however the
 * tiles are stored or retrieved:
 * 
 * <pre>
 * rootDir=/data/tiles/{z}/{x}/{y}.png
 * 
 * #or, for the ServerMapTileFactory:
 * rootDir=http://someserver.com/tileset/{z}/{x}/{y}.png
 * 
 * </pre>
 * 
 * In this case, the fileExt won't be used as the code will assume you are
 * setting that.
 * 
 * To make things simpler, you can define a tiles.omp file that sits under the
 * tile root directory or at the top level of the jar file, and let it specify
 * the properties for the tile set. The properties in that file should be
 * unscoped:
 * 
 * <pre>
 * 
 * fileExt=.png
 * #for instance, for GDAL processed images you need this transform since tiles have difference reference coordinates
 * mapTileTransform=com.bbn.openmap.dataAccess.mapTile.TMSMapTileCoordinateTransform
 * #in jar file, should specify rootDir inside jar to tiles (don't need this for layers accessing local file system rootDirs, unless you want to specify z,x,y order differently):
 * rootDir=mytiles
 * 
 * #optional
 * attribution=map data 2013 OpenStreetMap
 * 
 * </pre>
 * 
 * If you do this last configuration, all you need to define is rootDir (and
 * prettyName, class) property for layer, and then define all other props with
 * data.
 * 
 * @author dietrick
 */
public class MapTileLayer extends OMGraphicHandlerLayer implements MapTileRequester {

    private static final long serialVersionUID = 1L;

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.imageTile.TileLayer");

    /**
     * Property that sets the class name of the MapTileFactory to use for this
     * layer.
     */
    public final static String TILE_FACTORY_CLASS_PROPERTY = "tileFactory";
    /**
     * Property to allow the MapTileFactory to call repaint on this layer as map
     * tiles become available. Default is false, enabling it will not allow this
     * layer to be used with an ImageServer (renderDataForProjection won't
     * work).
     */
    public final static String INCREMENTAL_UPDATES_PROPERTY = "incrementalUpdates";

    /**
     * A property to set if you want to force the layer to use tiles of a
     * certain zoom level.
     */
    public final static String ZOOM_LEVEL_PROPERTY = "zoomLevel";

    /**
     * A property to set for displaying attribution for the data used by the
     * layer.
     */
    public final static String DATA_ATTRIBUTION_PROPERTY = "attribution";

    /**
     * The MapTileFactory that knows how to fetch image files and create
     * OMRasters for them.
     */
    protected MapTileFactory tileFactory;
    /**
     * Flag to allow this layer to set itself as a repaint callback object on
     * the tile factory.
     */
    protected boolean incrementalUpdates = false;
    /**
     * The zoomLevel to use when requesting tiles from the MapTileFactory. Is -1
     * for default, which lets the factory choose the zoom level based on the
     * current scale setting. You can choose 1-20 if you want to force the layer
     * to use something else.
     */
    protected int zoomLevel = -1;

    /**
     * Attribution for the map data. If it exists, it will be displayed on the
     * lower left corner of the map.
     */
    protected String attribution = null;

    /**
     * Rendering parameters for attribution string.
     */
    protected DrawingAttributes attributionAttributes = DrawingAttributes.getDefaultClone();

    public MapTileLayer() {
        setRenderPolicy(new com.bbn.openmap.layer.policy.BufferedImageRenderPolicy(this));
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
        setTileFactory(new StandardMapTileFactory());
        // We need to make this layer uninterruptable, because that messes with
        // the image file loading.
        setInterruptable(false);
    }

    public MapTileLayer(MapTileFactory tileFactory) {
        this();
        this.tileFactory = tileFactory;
    }

    /**
     * OMGraphicHandlerLayer method, called with projection changes or whenever
     * else doPrepare() is called. Calls getTiles on the map tile factory.
     * 
     * @return OMGraphicList that contains tiles to be displayed for the current
     *         projection.
     */
    public synchronized OMGraphicList prepare() {

        Projection projection = getProjection();

        if (projection == null) {
            return null;
        }

        if (tileFactory != null) {
            OMGraphicList newList = new OMGraphicList();
            // setList(newList);

            OMText attrib = getAttributionGraphic();
            if (attrib != null) {
                newList.add(attrib);
            }

            return tileFactory.getTiles(projection, zoomLevel, newList);
        }
        return null;
    }

    /**
     * @return OMText for attribution text
     */
    private OMText getAttributionGraphic() {
        Projection proj = getProjection();
        if (attribution != null && proj != null) {
            OMText attText = new OMText(10, proj.getHeight() - 10, attribution, OMText.JUSTIFY_LEFT);
            if (attributionAttributes != null) {
                attributionAttributes.setTo(attText);
            }
            return attText;
        }

        return null;
    }

    public String getToolTipTextFor(OMGraphic omg) {
        return (String) omg.getAttribute(OMGraphic.TOOLTIP);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        attribution = props.getProperty(prefix + DATA_ATTRIBUTION_PROPERTY, attribution);
        attributionAttributes.setProperties(prefix, props);

        String tileFactoryClassString = props.getProperty(prefix + TILE_FACTORY_CLASS_PROPERTY);
        if (tileFactoryClassString != null) {
            MapTileFactory itf = (MapTileFactory) ComponentFactory.create(tileFactoryClassString, prefix, props);
            if (itf != null) {
                setTileFactory(itf);
            }
        } else {
            // Let's see if we can figure out what kind of MapTileFactory is
            // needed based on rootDir
            String rootDirString = props.getProperty(prefix
                    + StandardMapTileFactory.ROOT_DIR_PROPERTY);
            if (rootDirString != null) {
                try {
                    // We build URL here to test if the rootDir location exists.
                    // Comment out url to avoid dead store findbugs problem.
                    /* URL url = */new java.net.URL(rootDirString);
                    // If we get here, we have a protocol, looks remote, so we
                    // should make sure the
                    // ServerMapTileFactory is used.
                    if (!(getTileFactory() instanceof ServerMapTileFactory)) {
                        setTileFactory(new ServerMapTileFactory(rootDirString));
                    }

                } catch (MalformedURLException e) {
                    // no protocol or something, use default
                    // StandardMapTileFactory
                    if (!(getTileFactory() instanceof StandardMapTileFactory)) {
                        setTileFactory(new StandardMapTileFactory());
                    }
                }
            }
        }

        if (tileFactory instanceof PropertyConsumer) {
            ((PropertyConsumer) tileFactory).setProperties(prefix, props);
        }

        incrementalUpdates = PropUtils.booleanFromProperties(props, prefix
                + INCREMENTAL_UPDATES_PROPERTY, incrementalUpdates);

        setZoomLevel(PropUtils.intFromProperties(props, prefix + ZOOM_LEVEL_PROPERTY, zoomLevel));
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        if (tileFactory != null) {
            props.put(prefix + TILE_FACTORY_CLASS_PROPERTY, tileFactory.getClass().getName());
            if (tileFactory instanceof PropertyConsumer) {
                ((PropertyConsumer) tileFactory).getProperties(props);
            }
        }

        props.put(prefix + INCREMENTAL_UPDATES_PROPERTY, Boolean.toString(incrementalUpdates));
        props.put(prefix + ZOOM_LEVEL_PROPERTY, Integer.toString(zoomLevel));

        attributionAttributes.getProperties(props);

        return props;
    }

    /**
     * Called when the layer has been turned off and the projection changes,
     * signifying that the layer can clean up.
     */
    public void removed(Container cont) {
        MapTileFactory tileFactory = getTileFactory();
        if (tileFactory != null) {
            tileFactory.reset();
        }
    }

    public MapTileFactory getTileFactory() {
        return tileFactory;
    }

    public void setTileFactory(MapTileFactory tileFactory) {
        logger.fine("setting tile factory to: " + tileFactory.getClass().getName());
        // This allows for general faster response, but causes the map to jump
        // around a little bit when used with the BufferedImageRenderPolicy and
        // when the projection changes occur rapidly, like when zooming and
        // panning several times in a second. The generation/positioning can't
        // keep up. It'll settle out, but it might be better to be slower and
        // less confusing to the user.

        tileFactory.setMapTileRequester(this);

        this.tileFactory = tileFactory;
    }

    public boolean isIncrementalUpdates() {
        return incrementalUpdates;
    }

    public void setIncrementalUpdates(boolean incrementalUpdates) {
        this.incrementalUpdates = incrementalUpdates;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public java.awt.Component getGUI() {
        return getTransparencyAdjustmentPanel(i18n.get(MapTileLayer.class, "layerTransparencyGUILabel", "Layer Transparency"), JSlider.HORIZONTAL, getTransparency());
    }

    /**
     * @return the attribution
     */
    public String getAttribution() {
        return attribution;
    }

    /**
     * @param attribution the attribution to set
     */
    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    /**
     * @return the attributionAttributes
     */
    public DrawingAttributes getAttributionAttributes() {
        return attributionAttributes;
    }

    /**
     * @param attributionAttributes the attributionAttributes to set
     */
    public void setAttributionAttributes(DrawingAttributes attributionAttributes) {
        this.attributionAttributes = attributionAttributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.dataAccess.mapTile.MapTileRequestor#shouldContinue()
     */
    public boolean shouldContinue() {
        return !shouldWrapItUp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.dataAccess.mapTile.MapTileRequestor#listUpdated()
     */
    public void listUpdated() {
        if (incrementalUpdates) {
            repaint();
        }
    }

}
