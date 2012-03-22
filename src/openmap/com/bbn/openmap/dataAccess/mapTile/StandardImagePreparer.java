package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Image;
import java.awt.image.BufferedImage;

import com.bbn.openmap.image.BufferedImageHelper;

/**
 * This is the standard map tile preparer, setting up the factory with ARGB
 * images.
 * 
 * @author dietrick
 */
public class StandardImagePreparer
      implements TileImagePreparer {

   public void prepareForEmptyTile(MapTileFactory factory) {
      // Noop
   }

   public BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
         throws InterruptedException {

      if (origImage instanceof BufferedImage) {
         return (BufferedImage) origImage;
      } else {
         return BufferedImageHelper.getBufferedImage(origImage, 0, 0, imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
      }
   }
}
