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
//$RCSfile: ImageReader.java,v $
//$Revision: 1.1 $
//$Date: 2007/01/22 15:47:35 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image;

import java.awt.image.BufferedImage;

public interface ImageReader {
    /**
     * Should be called by the the object managing the ImageTiles.
     * 
     * @return ImageTile, null if there is a problem.
     */
    ImageTile getImageTile();

    /**
     * Should be called by the object managing the ImageTiles.
     * 
     * @param cache The CacheHandler that should be used by the ImageTile for
     *        dynamic image loading.
     * @return ImageTile, null if there is a problem.
     */
    ImageTile getImageTile(ImageTile.Cache cache);

    /**
     * Will be called by the Image cache by the ImageTile to replenish the image
     * data as it is needed when the ImageTile is on the map.
     * 
     * @return BufferedImage of image.
     */
    BufferedImage getBufferedImage();
}
