
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Creates Grey-scale map tile images.
 *
 * @author dietrick
 */
public class GreyscaleImagePreparer
    implements TileImagePreparer {

    /**
     * Overriding the method that creates empty tiles for places with no
     * coverage. We need to set the no-coverage attributes for the
     * EmptyTileHandler (if it's a SimpleEmptyTileHandler) to null, so it
     * doesn't create empty tiles when it's beyond the coverage zoom level
     * limit. Those tiles are normally returned as clear, but in the conversion
     * to greyscale they turn black.
     * @param factory callback object to find EmptyTileHandler.
     */
    public void prepareForEmptyTile(MapTileFactory factory) {
        EmptyTileHandler empTileHandler = factory.getEmptyTileHandler();
        if (empTileHandler instanceof SimpleEmptyTileHandler) {
            ((SimpleEmptyTileHandler) empTileHandler).setNoCoverageAtts(null);
        }
    }

    public BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
        throws InterruptedException {

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = image.getGraphics();
        g.drawImage(origImage, 0, 0, null);
        g.dispose();
        return image;
    }
}
