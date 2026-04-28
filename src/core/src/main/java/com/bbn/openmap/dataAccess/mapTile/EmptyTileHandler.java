/* 
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.image.BufferedImage;

import com.bbn.openmap.proj.Projection;

/**
 * An EmptyTileHandler is a class that helps the MapTileFactory figure out what
 * to do with non-existent map tiles.
 * 
 * @author ddietrick
 */
public interface EmptyTileHandler {

    /**
     * The main call from the MapTileFactory to return something for the given
     * missing tile.
     * 
     * @param imagePath the path of the missing tile that is going to be used as
     *        cache lookup later.
     * @param x the uv x coordinate of the tile.
     * @param y the uv y coordinate of the tile.
     * @param zoomLevel the zoom level of the tile.
     * @param mtcTransform the transform that converts x,y coordinates to
     *        lat/lon and describes the layout of the uv tile coordinates.
     * @param proj the map projection, in case that matters what should be
     *        returned for the empty tile.
     * @return BufferedImage for image tile
     */
    BufferedImage getImageForEmptyTile(String imagePath, int x, int y, int zoomLevel, MapTileCoordinateTransform mtcTransform,
                                       Projection proj);

}
