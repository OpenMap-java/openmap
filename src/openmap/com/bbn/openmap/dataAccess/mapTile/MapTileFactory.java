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

import java.awt.Component;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * An object that fetches tiles for a given projection. It could cache the
 * tiles, it can get them from anywhere it might want to.
 * 
 * @author dietrick
 */
public interface MapTileFactory {
    /**
     * Create an OMGraphicList with a set of tiles on it.
     * 
     * @param proj
     * @return OMGraphicList that was created.
     * @throws InterruptedException
     */
    OMGraphicList getTiles(Projection proj);

    /**
     * Create an OMGraphicList that covers the projection with tiles that suit
     * the specified zoom level.
     * 
     * @throws InterruptedException
     */
    OMGraphicList getTiles(Projection proj, int zoomLevel);

    /**
     * Add tiles to OMGraphicList provided that suit the given projection.
     * 
     * @param proj
     * @param list
     * @return the OMGraphicList provided.
     * @throws InterruptedException
     */
    OMGraphicList getTiles(Projection proj, int zoomLevel, OMGraphicList list);

    /**
     * Set a MapTileRequestor in the tile factory that should be told to repaint
     * when new tiles become available, and to check with during the tile fetch
     * whether to keep going or not. listUpdate will be called when a new tile
     * has been added to the OMGraphicList passed in the getTiles method, and
     * shouldContinue will be called during stable times during the getTiles
     * fetch.
     * 
     * @param requestor callback MapTileRequestor to ask status questions.
     */
    void setMapTileRequester(MapTileRequester requestor);

    /**
     * Tell the factory to clean up resources.
     */
    void reset();

    /**
     * Get object that handles empty tiles.
     * 
     * @return EmptyTileHandler used by the factory.
     */
    EmptyTileHandler getEmptyTileHandler();
}
