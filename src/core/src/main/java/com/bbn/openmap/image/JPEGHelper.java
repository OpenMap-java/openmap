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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/JPEGHelper.java,v $
// $RCSfile: JPEGHelper.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:51 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.bbn.openmap.util.Debug;

/**
 * This class provides some utility methods for creating jpeg encoded images. It
 * relies on the Sun JDK/SDK JPEG codec classes.
 */
public class JPEGHelper {

    /**
     * This class has only static methods, so there is no need to construct
     * anything.
     */
    private JPEGHelper() {
    }

    /**
     * Return a byte array that contains the JPEG encoded image.
     *
     * @param image the image to encode
     * @param quality the JPEG quality factor to use in encoding
     * @exception IOException an error occurred in encoding the image
     */
    public static byte[] encodeJPEG(BufferedImage image, float quality)
            throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageOutputStream iosout = new MemoryCacheImageOutputStream(out);

        if (Debug.debugging("jpeghelper")) {
            Debug.output("Got output stream..." + out);
        }

        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);

        if (Debug.debugging("jpeghelper")) {
            Debug.output("Got encode params...");
        }

        writer.setOutput(iosout);
        if (Debug.debugging("jpeghelper")) {
            Debug.output("Got jpeg encoder...");
        }

        IIOImage iioi = new IIOImage(image, null, null);
        writer.write(null, iioi, iwp);

        writer.dispose();
        if (Debug.debugging("jpeghelper")) {
            Debug.output("encoded?");
        }

        return out.toByteArray();
    }

    /**
     * Return a byte array that contains the JPEG encoded image.
     *
     * @param w the width of the image
     * @param h the height of the image
     * @param pixels the array of pixels in RGB directcolor
     * @param quality the JPEG quality factor to use in encoding
     * @exception IOException an error occurred in encoding the image
     */
    public static byte[] encodeJPEG(int w, int h, int[] pixels, float quality)
            throws IOException {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, w, h, pixels, 0, w);
        pixels = null;
        return encodeJPEG(bi, quality);
    }

    private static void encodeAndWriteJPEGFile(File file, BufferedImage image, float quality)
            throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);
        FileImageOutputStream output = new FileImageOutputStream(file);
        writer.setOutput(output);
        IIOImage iioi = new IIOImage(image, null, null);
        writer.write(null, iioi, iwp);
        writer.dispose();
    }

    /**
     * A test main that encodes an image url at various jpeg quality factors.
     *
     * @param args url [width height]
     */
    public static void main(String args[])
            throws Exception {
        if (args.length == 0) {
            System.out.println("java jpeg url [width height]");
            System.exit(-1);
        }

        Debug.init();

        int uw = -1;
        int uh = -1;
        if (args.length > 1) {
            uw = Integer.parseInt(args[1]);
            uh = Integer.parseInt(args[2]);
        }

        String urlsource = args[0];
        int lastslash = urlsource.lastIndexOf('/');
        if (lastslash == -1) {
            lastslash = 0;
        } else {
            lastslash++;
        }
        int lastdot = urlsource.lastIndexOf('.');
        if (lastdot == -1) {
            lastdot = 0;
        }
        String filebase = urlsource.substring(lastslash, lastdot);

        Debug.output("url=" + urlsource + " filebase=" + filebase);

        BufferedImage bi = BufferedImageHelper.getBufferedImage(new URL(urlsource), 0, 0, uw, uh);
        if (bi == null) {
            Debug.error("JPEGHelper: Image load failed");
        } else {
            PrintStream html = new PrintStream(new FileOutputStream(new File(filebase + ".html")));

            html.println("Source url = " + urlsource + " <br>");
            html.println(" width = " + uw + " height=" + uh + " pixels=" + uw * uh + " <hr>");
            for (int i = 0; i < 20; i++) {
                File f = new File(filebase + ((i < 10) ? "0" : "") + i + ".jpg");
                float quality = 0.0499f * i;
                encodeAndWriteJPEGFile(f, bi, quality);
                html.println("Image Quality Factor: " + quality + " <br>");
                html.println("Image Size (bytes) : " + f.length() + " <br>");
                html.println("<img src=\"" + f.getName() + "\"> <hr>");
            }

            html.close();
        }
        System.exit(-1); // awt stinks
    }
}