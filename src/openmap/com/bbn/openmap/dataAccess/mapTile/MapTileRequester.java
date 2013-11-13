/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

/**
 * An interface for a Class that makes requests from a MapTileFactory, and may
 * provide intermittent status updates on whether to continue work or update the
 * current fetched tiles.
 * 
 * @author dietrick
 */
public interface MapTileRequester {

    /**
     * Called when the provided OMGraphicList has another tile added to it, so
     * the list can be repainted if the caller is interested in incremental
     * updates.
     */
    void listUpdated();

    /**
     * Asked by the MapTileFactory if the tile fetching should continue. Called
     * during stable times, just to make sure that the currently fetched tile
     * set is still wanted. Safe to always return true, but if you want the
     * TileFactory to forget what it's doing (like the projection changed), then
     * return false;
     * 
     * @return true of the MapTileFactory should keep collecting tiles for the
     *         current request.
     */
    boolean shouldContinue();

}
