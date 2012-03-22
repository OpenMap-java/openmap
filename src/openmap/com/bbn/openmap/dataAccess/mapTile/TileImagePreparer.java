/*
 * TileImagePreparer.java        Jan 10, 2012 9:37:01 PM
 *
 * Copyright (c)  2012-2012 CSC, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * CSC, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with CSC.
 *
 */

package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * TODO: Describe the TileImagePreparer interface class here.
 *
 * @author dietrick
 */
public interface TileImagePreparer {
    /**
     * Sometimes, based on the color handling of the preparer, empty tiles need to be handled differently.
     * @param factory used to make calls on the factory to prepare for empty tile fetch.
     */
    void prepareForEmptyTile(MapTileFactory factory);

    /**
     * Method called to manipulate provided image in some way, returning modified image.
     *
     * @param origImage Any java Image
     * @param imageWidth pixel width
     * @param imageHeight pixel height
     * @return BufferedImage with any changes necessary.
     * @throws InterruptedException
     */
    BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
        throws InterruptedException;
}
