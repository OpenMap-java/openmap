package com.bbn.openmap.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * This code comes from the book "Filthy Rich Clients" Chapter 4.  
 * It is optimized for fast scaling.
 *
 * @author Chet Hasse
 */
public class ImageScaler {

	/**
     * Progressive bilinear scaling: for any downscale size, scale
     * iteratively by halves using BILINEAR filtering until the proper 
     * size is reached.
     * 
     * @param inputImage the starting image
     * @param startSize a starting dimension of the image, either h or w
     * @param endSize a finishing dimension of the image, same as startSize h or w
     * @param imageType the image type for rendering, from BufferedImage.
     */
    public static Image getOptimalScalingImage(Image inputImage,
            int startSize, int endSize, int imageType) {
        int currentSize = startSize;
        Image currentImage = inputImage;
        int delta = currentSize - endSize;
        int nextPow2 = currentSize >> 1;
        while (currentSize > 1) {
            if (delta <= nextPow2) {
                if (currentSize != endSize) {
                    BufferedImage tmpImage = new BufferedImage(endSize,
                            endSize, imageType);
                    Graphics g = tmpImage.getGraphics();
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(currentImage, 0, 0, tmpImage.getWidth(), 
                            tmpImage.getHeight(), null);
                    currentImage = tmpImage;
                }
                return currentImage;
            } else {
                BufferedImage tmpImage = new BufferedImage(currentSize >> 1,
                        currentSize >> 1, imageType);
                Graphics g = tmpImage.getGraphics();
                ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(currentImage, 0, 0, tmpImage.getWidth(), 
                        tmpImage.getHeight(), null);
                currentImage = tmpImage;
                currentSize = currentImage.getWidth(null);
                delta = currentSize - endSize;
                nextPow2 = currentSize >> 1;
            }
        }
        return currentImage;
    }
    
	/**
     * Progressive bilinear scaling: for any downscale size, scale
     * iteratively by halves using BILINEAR filtering until the proper 
     * size is reached.  Assumes the image type is BufferedImage.TYPE_INT_ARGB.
     * 
     * @param inputImage the starting image
     * @param startSize a starting dimension of the image, either h or w
     * @param endSize a finishing dimension of the image, same as startSize h or w
     */
    public static Image getOptimalScalingImage(Image inputImage,
            int startSize, int endSize) {
    	return getOptimalScalingImage(inputImage, startSize, endSize, BufferedImage.TYPE_INT_ARGB);
    }
}
