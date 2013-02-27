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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/BufferedImageHelper.java,v $
// $RCSfile: BufferedImageHelper.java,v $
// $Revision: 1.9 $
// $Date: 2006/08/09 21:08:31 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.bbn.openmap.util.ComponentFactory;

/**
 * This class provides some utility methods for creating a BufferedImage. It
 * will check to see if the Java Advanced Image package is available and use it
 * if it can.
 * 
 * @author dietrick - original implementation and reflection mods.
 * @author Fredrik Lyden - JAI inspiration and initial code.
 */
public class BufferedImageHelper {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.io.BufferedImageHelper");

    /**
     * This class has only static methods, so there is no need to construct
     * anything.
     */
    private BufferedImageHelper() {
    }

    /**
     * A test/instantiation copy of the JAI object to use if JAI is installed.
     */
    private static Object jaiObj = null;
    /**
     * Flag to use if the JAI has be checked for.
     */
    private static boolean checkedForJAI = false;

    /**
     * Get the JAI class if it's available.
     */
    protected static Object getJAI() {
        if (!checkedForJAI) {
            jaiObj = ComponentFactory.create("javax.media.jai.JAI");
            checkedForJAI = true;
        }
        return jaiObj;
    }

    /**
     * Run the operation on JAI to create BufferedImage. Uses reflection to
     * determine if JAI is available.
     * 
     * @param opName JAI opName, like "file" or "url"
     * @param param JAI object to use for operation, like the file path (String)
     *        or URL.
     * @return BufferedImage if JAI can be used to create it, null if anything
     *         goes wrong.
     */
    public static BufferedImage getJAIBufferedImage(String opName, Object param) {
        boolean DEBUG = logger.isLoggable(Level.FINE);

        Object jai = getJAI();

        if (jai == null) {
            return null;
        }

        if (DEBUG) {
            logger.fine("Using JAI to create image from " + opName);
        }

        try {
            // Do a little reflection to run methods on classes we
            // might not know about.
            Class[] createArgs = new Class[] {
                Class.forName("java.lang.String"),
                Class.forName("java.lang.Object")
            };

            Method createMethod = jai.getClass().getDeclaredMethod("create", createArgs);

            Object[] createParams = new Object[] {
                opName,
                param
            };
            Object planarImageObject = createMethod.invoke(jai, createParams);

            if (planarImageObject != null) {
                Method getBufferedImageMethod = planarImageObject.getClass().getMethod("getAsBufferedImage", (Class[]) null);

                return (BufferedImage) getBufferedImageMethod.invoke(planarImageObject, (Object[]) null);
            }

        } catch (ClassNotFoundException cnfe) {
            if (DEBUG) {
                logger.warning("BufferedImageHelper.getJAIBufferedImage() ClassNotFoundException error: \n" + cnfe.getMessage());
            }
        } catch (IllegalAccessException iae) {
            if (DEBUG) {
                logger.warning("BufferedImageHelper.getJAIBufferedImage() IllegalAccessException error: \n" + iae.getMessage());
            }
        } catch (InvocationTargetException ite) {
            if (DEBUG) {
                logger.warning("BufferedImageHelper.getJAIBufferedImage() InvocationTargetException error: \n" + ite.getMessage());
            }
        } catch (NoSuchMethodException nsme) {
            if (DEBUG) {
                logger.warning("BufferedImageHelper.getJAIBufferedImage() NoSuchMethodException error: " + nsme.toString());
                nsme.printStackTrace();
            }
        } catch (SecurityException se) {
            if (DEBUG) {
                logger.warning("BufferedImageHelper.getJAIBufferedImage() SecurityException error: \n" + se.getMessage());
            }
        } catch (Exception e) {
            if (DEBUG) {
                logger.warning("BufferedImageHelper.getJAIBufferedImage() Exception: \n" + e.getMessage());
            }
        }

        return null;
        // All this above to replace this:
        // PlanarImage planarImage = JAI.create(opName, param);
        // return getBufferedImage(planarImage.getAsBufferedImage(),
        // x, y, w, h);
    }

    /**
     * Run the operation on JAI to create BufferedImage. Uses reflection to
     * determine if JAI is available. If x or y is not zero, or w and h are not
     * the image dimensions, the image returned will be cropped/translated to
     * match the values.
     * 
     * @param opName JAI opName, like "file" or "url"
     * @param param JAI object to use for operation, like the file path (String)
     *        or URL.
     * @param x x start pixel
     * @param y y start pixel
     * @param w crop width (-1 uses image width)
     * @param h crop height (-1 uses image height)
     * @return BufferedImage if JAI can be used to create it, null if anything
     *         goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getJAIBufferedImage(String opName, Object param, int x, int y, int w, int h)
            throws InterruptedException {

        BufferedImage bi = getJAIBufferedImage(opName, param);

        // If the whole image isn't wanted, do another operation...
        if (bi != null && (x != 0 || y != 0 || w > 0 || h > 0)) {

            int imageType = BufferedImage.TYPE_INT_RGB;
            if (bi.getColorModel().hasAlpha()) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }

            return getBufferedImage(bi, x, y, w, h, imageType);
        }
        // else return null or the original image.
        return bi;
    }

    /**
     * Return a BufferedImage loaded from a URL.
     * 
     * @return BufferedImage if it can be created, null if anything goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getBufferedImage(URL url)
            throws InterruptedException {
        return getBufferedImage(url, 0, 0, -1, -1);
    }

    /**
     * Return a BufferedImage loaded from a URL. If JAI isn't available, checks
     * the file path to see if it ends in jpg or jpeg, and won't try to use an
     * alpha channel if it does.
     * 
     * @param url the source URL
     * @param x x start pixel
     * @param y y start pixel
     * @param w crop width (-1 uses image width)
     * @param h crop height (-1 uses image height)
     * @return BufferedImage if it can be created, null if anything goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getBufferedImage(URL url, int x, int y, int w, int h)
            throws InterruptedException {

        if (url == null) {
            return null;
        }

        BufferedImage bi = getJAIBufferedImage("url", url, x, y, w, h);

        if (bi != null) {
            return bi;
        }

        logger.fine("BufferedImageHelper.getBufferedImage(URL) can't use JAI, using ImageIcon");

        // if JAI is not installed....
        ImageIcon ii = new ImageIcon(url);
        String path = url.getPath();
        boolean noAlpha = path.endsWith("jpg") || path.endsWith("jpeg");

        return getBufferedImage(ii, x, y, w, h, !noAlpha);
    }

    /**
     * Return a BufferedImage loaded from a URL. Doesn't use JAI if available.
     * 
     * @param ii an ImageIcon created from the source.
     * @param x x start pixel
     * @param y y start pixel
     * @param w crop width (-1 uses image width)
     * @param h crop height (-1 uses image height)
     * @param hasAlpha whether the image should be transparent.
     * @return BufferedImage if it can be created, null if anything goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getBufferedImage(ImageIcon ii, int x, int y, int w, int h, boolean hasAlpha)
            throws InterruptedException {
        if (w <= 0)
            w = ii.getIconWidth();
        if (h <= 0)
            h = ii.getIconHeight();

        return getBufferedImage(ii.getImage(), x, y, w, h, (hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB));
    }

    /**
     * Return a BufferedImage loaded from a file path.
     * 
     * @return BufferedImage if it can be created, null if anything goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getBufferedImage(String path)
            throws InterruptedException {
        return getBufferedImage(path, 0, 0, -1, -1);
    }

    /**
     * Return a BufferedImage loaded from an image file path. If JAI isn't
     * available, checks the file path to see if it ends in jpg or jpeg, and
     * won't try to use an alpha channel if it does.
     * 
     * @param path file path to the image
     * @param x x start pixel
     * @param y y start pixel
     * @param w crop width (-1 uses image width)
     * @param h crop height (-1 uses image height)
     * @return BufferedImage if it can be created, null if anything goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getBufferedImage(String path, int x, int y, int w, int h)
            throws InterruptedException {

        BufferedImage bi = getJAIBufferedImage("file", path, x, y, w, h);

        if (bi != null) {
            return bi;
        }

        logger.fine("BufferedImageHelper.getBufferedImage(path) can't use JAI, using ImageIcon");

        // if JAI is not installed....
        ImageIcon ii = new ImageIcon(path);
        boolean noAlpha = path.endsWith("jpg") || path.endsWith("jpeg");

        return getBufferedImage(ii, x, y, w, h, !noAlpha);
    }

    /**
     * Return a BufferedImage loaded from a Image. The type of image is
     * BufferedImage.Type_INT_RGB. If you know the height and width, use them
     * because it's slower to have the class figure it out.
     * 
     * @param image the source Image
     * @param x x start pixel
     * @param y y start pixel
     * @param w crop width (-1 uses image width)
     * @param h crop height (-1 uses image height)
     * @return BufferedImage if it can be created, null if anything goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getBufferedImage(Image image, int x, int y, int w, int h)
            throws InterruptedException {
        return getBufferedImage(image, x, y, w, h, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Return a BufferedImage loaded from a Image. If you know the height and
     * width, use them because it's slower to have the class figure it out.
     * 
     * @param image the source Image
     * @param x x start pixel - the horizontal pixel location in the returned
     *        image that the provided image will be set.
     * @param y y start pixel - the vertical pixel location in the returned
     *        image that the provided image will be set.
     * @param w crop width (-1 uses image width)
     * @param h crop height (-1 uses image height)
     * @param imageType the image color model. See BufferedImage.
     * @return BufferedImage if it can be created, null if anything goes wrong.
     * @throws InterruptedException
     */
    public static BufferedImage getBufferedImage(Image image, int x, int y, int w, int h, int imageType)
            throws InterruptedException {

        if (w <= 0 || h <= 0) {
            logger.fine("BufferedImageHelper.getBufferedImage() don't know h/w, using pixel grabber");
            return getBufferedImageFromPixelGrabber(image, x, y, w, h, imageType);
        } else {
            BufferedImage bufferedImage = new BufferedImage(w, h, imageType);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, x, y, null);
            g2d.dispose();
            return bufferedImage;
        }
    }

    /**
     * Return a BufferedImage loaded from a Image, using a PixelGrabber. Good
     * for when you have an Image, not a BufferedImage, and don't know the width
     * and height. There is a performance penalty with this method, though.
     * 
     * @param image the source Image
     * @param x x start pixel - the horizontal pixel location in the returned
     *        image that the provided image will be set.
     * @param y y start pixel - the vertical pixel location in the returned
     *        image that the provided image will be set.
     * @param w crop width (-1 uses image width)
     * @param h crop height (-1 uses image height)
     * @param imageType the image color model. See BufferedImage.
     * @return BufferedImage if it can be created, null if anything goes wrong.
     */
    public static BufferedImage getBufferedImageFromPixelGrabber(Image image, int x, int y, int w, int h, int imageType) {

        PixelGrabber pg = new PixelGrabber(image, x, y, w, h, true);
        int[] pixels = ImageHelper.grabPixels(pg);

        if (pixels == null) {
            return null;
        }

        w = pg.getWidth();
        h = pg.getHeight();
        pg = null;

        BufferedImage bi = new BufferedImage(w, h, imageType);
        logger.fine("BufferedImageHelper.getBufferedImage(): Got buffered image...");

        // bi.setRGB(0, 0, w, h, pixels, 0, w);
        /**
         * Looking at the standard BufferedImage code, an int[0] is allocated
         * for every pixel. Maybe the memory usage is optimized for that, but it
         * goes through a call stack for every pixel to do it. Let's just cycle
         * through the data and write the pixels directly into the raster.
         */
        WritableRaster raster = (WritableRaster) bi.getRaster();
        raster.setDataElements(0, 0, w, h, pixels);

        logger.fine("BufferedImageHelper.getBufferedImage(): set pixels in image...");

        return bi;
    }
}