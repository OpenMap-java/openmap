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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/AcmeGifHelper.java,v $
// $RCSfile: AcmeGifHelper.java,v $
// $Revision: 1.5 $
// $Date: 2005/01/10 16:14:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.bbn.openmap.util.Debug;

/**
 * This class provides some utility methods for creating gif encoded
 * images, using the Acme code available at http://www.acme.com/java/
 * A copy has been included in the contrib directory.
 */
public class AcmeGifHelper {

    /**
     * This class has only static methods, so there is no need to
     * construct anything.
     */
    private AcmeGifHelper() {}

    /**
     * Return a byte array that contains the GIF encoded image.
     * 
     * @param image the image to encode
     * @exception IOException an error occurred in encoding the image
     */
    public static byte[] encodeGif(BufferedImage image) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (Debug.debugging("acmegifhelper")) {
            Debug.output("Got output stream..." + out);
        }

        Acme.JPM.Encoders.GifEncoder enc = new Acme.JPM.Encoders.GifEncoder(image, out);
        if (Debug.debugging("acmegifhelper")) {
            Debug.output("Got gif encoder...");
        }

        enc.encode();
        if (Debug.debugging("acmegifhelper")) {
            Debug.output("encoded?");
        }

        return out.toByteArray();
    }

    /**
     * Return a byte array that contains the GIF encoded image.
     * 
     * @param w the width of the image
     * @param h the height of the image
     * @param pixels the array of pixels in RGB directcolor
     * @exception IOException an error occurred in encoding the image
     */
    public static byte[] encodeGif(int w, int h, int[] pixels)
            throws IOException {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, w, h, pixels, 0, w);
        pixels = null;
        return encodeGif(bi);
    }
}