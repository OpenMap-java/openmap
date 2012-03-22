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

import com.bbn.openmap.util.Debug;

/**
 * A class that holds static functions that do things we tend to do to
 * images a lot.
 */
public class ImageHelper {

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
                Debug.error("ImageHelper.grabPixels(): Error in loading image");
                return null;
            }

            int framebitCount = 0;
            while (true) {
                int status = pg.getStatus();

                if (Debug.debugging("image")) {
                    Debug.output("ImageHelper.grabPixels(): status = " + status);
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
                    Debug.error("ImageHelper.grabPixels(): Error in loading image");
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