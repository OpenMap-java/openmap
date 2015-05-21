// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageHelper.java,v $
// $RCSfile: ImageHelper.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that holds static functions that do things we tend to do to
 * images a lot.
 */
public class ImageHelper {

    public final static Logger logger = Logger.getLogger("com.bbn.openmap.image.ImageHelper");
    
    /**
     * Take a PixelGrabber and get the pixels out of it.
     * 
     * @param pg PixelGrabber
     * @return int[] of pixels, null if anything bad happens.
     */
    public static int[] grabPixels(PixelGrabber pg) {

        // Get only the pixels you need.
        // Use a pixel grabber to get the right pixels.
        try {
            pg.startGrabbing();

            boolean grabbed = pg.grabPixels();

            if (!grabbed) {
                logger.fine("Error in loading image, no pixels grabbed");
                return null;
            }

            int framebitCount = 0;
            while (true) {
                int status = pg.getStatus();

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("pixelgrabber status = " + status);
                }

                if ((status & ImageObserver.ALLBITS) != 0) {
                    break;
                }
                if ((status & ImageObserver.FRAMEBITS) != 0) {
                    // Give some cycles to be sure - some times it
                    // seems
                    // to not really be ready,
                    if (framebitCount < 20) {
                        framebitCount++;
                    }
                    break;
                }
                if ((status & ImageObserver.ERROR) != 0) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Error in loading image, ImageObserver error");
                    }
                    return null;
                }
                Thread.sleep(100);
            }
            return (int[]) pg.getPixels();

        } catch (InterruptedException ie) {
            return null;
        }
    }
}