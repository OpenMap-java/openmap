package com.bbn.openmap.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
/*
 * 
 * Copyright (c) 2007, Sun Microsystems, Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * This code comes from the book "Filthy Rich Clients" Chapter 4.  
 * It is optimized for fast scaling.
 *
 * @author Chet Hasse
 * */
public class ImageScaler {

	/**
     * Progressive bilinear scaling: for any downscale size, scale
     * iteratively by halves using BILINEAR filtering until the proper 
     * size is reached.
     */
    public static Image getOptimalScalingImage(Image inputImage,
            int startSize, int endSize) {
        int currentSize = startSize;
        Image currentImage = inputImage;
        int delta = currentSize - endSize;
        int nextPow2 = currentSize >> 1;
        while (currentSize > 1) {
            if (delta <= nextPow2) {
                if (currentSize != endSize) {
                    BufferedImage tmpImage = new BufferedImage(endSize,
                            endSize, BufferedImage.TYPE_INT_RGB);
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
                        currentSize >> 1, BufferedImage.TYPE_INT_RGB);
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
}
