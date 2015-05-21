/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.image.BufferedImage;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * A simple MapTileFactory that returns empty tiles with labels showing zoom
 * level and x, y coords, and borders. This does the same thing as enabling the
 * MAPTILE_DEBUGGING logger flag, but can be attached to a MapTileLayer which
 * can be turned off and on.
 * <p>
 * The properties for the layer looke like this (with tileDebug as an example prefix):
 * 
 * <pre>
 * tileDebug.class=com.bbn.openmap.layer.imageTile.MapTileLayer
 * tileDebug.prettyName=Map Tile Information
 * tileDebug.tileFactory=com.bbn.openmap.dataAccess.mapTile.DebugMapTileFactory
 * </pre>
 * 
 * @author dietrick
 */
public class DebugMapTileFactory extends StandardMapTileFactory {

    public DebugMapTileFactory() {
        rootDir = "EMPTYANDUNIMPORTANT";
    }

    public CacheObject load(Object key, int x, int y, int zoomLevel, Projection proj) {
        if (key instanceof String) {
            String imagePath = (String) key;
            BufferedImage bi = new BufferedImage(MapTileCoordinateTransform.TILE_SIZE, MapTileCoordinateTransform.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            OMGraphic raster;
            try {
                raster = createOMGraphicFromBufferedImage(bi, x, y, zoomLevel, proj);

                raster.putAttribute(OMGraphic.LABEL, new OMTextLabeler("Tile: " + zoomLevel + "|"
                        + x + "|" + y, OMText.JUSTIFY_CENTER));
                raster.setSelected(true);

                return new CacheObject(imagePath, raster);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return null;
    }
}
