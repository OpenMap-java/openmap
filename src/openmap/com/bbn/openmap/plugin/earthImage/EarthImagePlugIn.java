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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/earthImage/EarthImagePlugIn.java,v $
// $RCSfile: EarthImagePlugIn.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:12 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.earthImage;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.plugin.AbstractPlugIn;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * This class takes an image of the earth, and creates a background image from
 * it that matches an OpenMap projection. It currently assumes that the
 * degrees/pixel ratios are constant in both directions, the coordinate system
 * origins in both directions are at the center of the picture, and that the
 * left an right edges of the images are at -180/180 degrees longitude, and that
 * the top and bottom of the edges are at 90/-90 degrees latitude. I think the
 * code will work for images that do not cover the entire earth in this manner,
 * as long as the degree/pixel ratios are the same, but the ImageTranslator
 * limits would have to be adjusted to fit the source image.
 * 
 * #For the plugin layer pluginlayer.class=com.bbn.openmap.plugin.PlugInLayer
 * pluginlayer.prettyName=Whatever
 * pluginlayer.plugin=com.bbn.openmap.plugin.earthImage.EarthImagePlugIn
 * pluginlayer.plugin.image=path to file, URL or resource.
 */
public class EarthImagePlugIn
      extends AbstractPlugIn
      implements ImageServerConstants {

   protected BufferedImage bi = null;
   protected ImageTranslator it = null;
   public final static String ImageProperty = "image";
   protected String imageString = null;

   public EarthImagePlugIn() {
   }

   public EarthImagePlugIn(Component comp) {
      super(comp);
   }

   /**
    * @param p projection of the screen, holding scale, center coords, height,
    *        width.
    * @return an OMGraphicList containing an OMRaster with the image to be
    *         displayed.
    */
   public OMGraphicList getRectangle(Projection p) {
      OMGraphicList list = new OMGraphicList();

      // The first time through with a good bi, the it will be
      // created later. This routine will only be executed if the
      // image icon is no good.
      if (bi == null && it == null) {
         return list;
      }

      OMRaster ras = null;

      if (it == null) {
         it = new ImageTranslator(bi);
         bi = null; // don't hold onto it.
      }

      ras = it.getImage(p);

      if (ras != null) {
         list.add(ras);
      }

      list.generate(p);
      return list;
   }

   /**
    * Method to set the properties in the PropertyConsumer. The prefix is a
    * string that should be prepended to each property key (in addition to a
    * separating '.') in order for the PropertyConsumer to uniquely identify
    * properties meant for it, in the midst of of Properties meant for several
    * objects.
    * 
    * @param prefix a String used by the PropertyConsumer to prepend to each
    *        property value it wants to look up -
    *        setList.getProperty(prefix.propertyKey). If the prefix had already
    *        been set, then the prefix passed in should replace that previous
    *        value.
    * @param setList a Properties object that the PropertyConsumer can use to
    *        retrieve expected properties it can use for configuration.
    */
   public void setProperties(String prefix, Properties setList) {
      super.setProperties(prefix, setList);

      String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

      imageString = setList.getProperty(realPrefix + ImageProperty);

      if (imageString == null || imageString.length() == 0) {
         Debug.error("EarthImagePlugIn needs an image.");
         Debug.output(setList.toString());
         return;
      } else if (Debug.debugging("earthimage")) {
         Debug.output("EarthImagePlugIn:  fetching " + realPrefix + ImageProperty + " : " + imageString);
      }
      try {
         URL url = PropUtils.getResourceOrFileOrURL(this, imageString);
         bi = BufferedImageHelper.getBufferedImage(url, 0, 0, -1, -1);

         if (Debug.debugging("earthimage") && bi != null) {
            Debug.output("EarthImagePlugIn: buffered image OK");
         }
      } catch (MalformedURLException murle) {
         Debug.error("EarthImagePlugIn: image path is not good: " + imageString);
      } catch (InterruptedException ie) {
         Debug.error("EarthImagePlugIn: problem reading image from path: " + imageString);
      }
   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);

      String prefix = PropUtils.getScopedPropertyPrefix(this);

      props.put(prefix + ImageProperty, (imageString == null ? "" : imageString));

      return props;
   }

   public Properties getPropertyInfo(Properties props) {
      props = super.getPropertyInfo(props);
      props.put(ImageProperty, "Path to image file (URL, resource or file)");
      props.put(ImageProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

      props.put(initPropertiesProperty, ImageProperty);

      return props;
   }

   /**
    * The ImageTranslator is the object that takes the BufferedImage and creates
    * the OMRaster from it based on a Projection object.
    */
   public class ImageTranslator {

      protected int[] pixels = null;

      /** Image Icon width, */
      public int iwidth;
      /** Image Icon height, */
      public int iheight;
      /**
       * Horizontal degrees/pixel in the source BufferedImage. Assumed to be
       * constant across the image.
       */
      public float hor_dpp;
      /**
       * Vertical degrees/pixel in the source BufferedImage. Assumed to be
       * constant across the image.
       */
      public float ver_dpp;
      /**
       * The vertical origin pixel location in the source image for the
       * coordinate system origin (0 degrees latitude).
       */
      public int verOrigin;
      /**
       * The horizontal origin pixel location in the source image for the
       * coordinate system origin (0 degrees longitude).
       */
      public int horOrigin;

      /**
       * Create an image translator for an image assumed to be world wide
       * coverage, with the top at 90 degrees, the bottom at -90, the left side
       * at -180 and the right side at 180. Assumes the origin point is in the
       * middle of the image.
       */
      public ImageTranslator(BufferedImage bi) {
         if (bi != null) {
            iwidth = bi.getWidth();
            iheight = bi.getHeight();

            verOrigin = iheight / 2;
            horOrigin = iwidth / 2;

            hor_dpp = 360f / (float) iwidth;
            ver_dpp = 180f / (float) iheight;

            if (Debug.debugging("earthimage")) {
               Debug.output("ImageTranslator: getting image pixels w:" + iwidth + ", h:" + iheight + "\n     hor dpp:" + hor_dpp
                     + ", ver dpp:" + ver_dpp);
            }

            pixels = getPixels(bi, 0, 0, iwidth, iheight);

            // See if this saves on memory. Seems to.
            bi = null;
         }
      }

      /**
       * The pixels used in the OMRaster.
       */
      int[] tmpPixels = new int[0];

      /**
       * Given a projection, create an OMRaster that reflects the image warped
       * to that projection.
       */
      public OMRaster getImage(Projection p) {
         if (pixels != null && p != null) {
            int projHeight = p.getHeight();
            int projWidth = p.getWidth();

            // See if we can reuse the pixel array we have.
            if (tmpPixels.length != projWidth * projHeight) {
               tmpPixels = new int[projWidth * projHeight];
            }

            // /////////////////////////////////
            // For Testing...
            // LatLonPoint ul = p.getUpperLeft();
            // LatLonPoint lr = p.getLowerRight();

            // int ulhorIndex = horOrigin +
            // (int)(ul.getLongitude()/hor_dpp);
            // int ulverIndex = verOrigin -
            // (int)(ul.getLatitude()/ver_dpp);

            // int lrhorIndex = horOrigin +
            // (int)(lr.getLongitude()/hor_dpp);
            // int lrverIndex = verOrigin -
            // (int)(lr.getLatitude()/ver_dpp);

            // Debug.output("The image file will be referenced
            // from:\n " +
            // ulhorIndex + ", " + ulverIndex + "\n " +
            // lrhorIndex + ", " + lrverIndex);
            // /////////////////////////////////

            int clear = 0x00000000;

            Point ctp = new Point();
            LatLonPoint llp = new LatLonPoint.Double();
            LatLonPoint center = p.getCenter(new LatLonPoint.Double());

            for (int i = 0; i < projWidth; i++) {
               for (int j = 0; j < projHeight; j++) {
                  p.inverse(i, j, llp);

                  // index into the OMRaster pixel array
                  int tmpIndex = i + (j * projWidth);

                  // If the llp calculated isn't on the map,
                  // don't bother drawing it. Could be a space
                  // point in Orthographic projection, for
                  // instance.
                  if (llp.equals(center) || !((GeoProj)p).isPlotable(llp.getLatitude(), llp.getLongitude())) {
                     p.forward(llp, ctp);
                     if (ctp.x != i || ctp.y != j) {
                        tmpPixels[tmpIndex] = clear;
                        continue;
                     }
                  }

                  // Find the corresponding pixel location in
                  // the source image.
                  int horIndex = horOrigin + (int) (llp.getLongitude() / hor_dpp);
                  int verIndex = verOrigin - (int) (llp.getLatitude() / ver_dpp);

                  if (horIndex < 0 || horIndex >= iwidth || verIndex <= 0 || verIndex >= iheight) {
                     /**
                      * If the verIndex is 0, that means it's at 90N, which is
                      * kind of an abstract thing. So lets make it clear.
                      * Literally.
                      */

                     // pixel not on the source image. This
                     // happens if the image doesn't cover the
                     // entire earth.
                     continue;
                  }

                  int imageIndex = horIndex + (verIndex * iwidth);

                  if (imageIndex >= 0 && imageIndex < pixels.length) {
                     tmpPixels[tmpIndex] = pixels[imageIndex];
                     // } else {
                     // Debug.message("earthimage",
                     // "ImageTranslator: outside pixel
                     // range");
                  }
               }
            }

            Debug.message("earthimage", "ImageTranslator: finished creating image");
            return new OMRaster(0, 0, projWidth, projHeight, tmpPixels);
         } else {
            Debug.message("earthimage", "ImageTranslator: problem creating image");
         }

         // If you get here, something's not right.
         return null;
      }

      /**
       * Get the pixels from the BufferedImage. If anything goes wrong, returns
       * a int[0].
       */
      protected int[] getPixels(Image img, int x, int y, int w, int h) {
         int[] pixels = new int[w * h];
         PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
         try {
            pg.grabPixels();
         } catch (InterruptedException e) {
            Debug.error("ImageTranslator: interrupted waiting for pixels!");
            return new int[0];
         }

         if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("ImageTranslator: image fetch aborted or errored");
            return new int[0];
         }

         return pixels;
      }
   }
}