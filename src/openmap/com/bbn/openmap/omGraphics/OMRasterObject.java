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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMRasterObject.java,v $
// $RCSfile: OMRasterObject.java,v $
// $Revision: 1.16 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.image.ImageHelper;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DeepCopyUtil;

/**
 * The OMRasterObject is the parent class for OMRaster and OMBitmap objects. It
 * manages some of the same functions that both classes require in order to
 * create image pixel data from bytes or integers.
 * 
 * <P>
 * An ImageFilter may be applied to OMRasterObjects. These can be scale filters,
 * color filters, or maybe (?hopefully?) projection filters. These filters won't
 * change the original image data, and the original can be reconstructed by
 * resetting the filter to null, and generating the object.
 * <P>
 * 
 * For all classes in the OMRasterObject family, a java.awt.Shape object is
 * created for the border of the image. This Shape object is used for distance
 * calculations. If the OMRasterObject is selected(), however, this Shape will
 * be rendered with the OMGraphic parameters that are set in the OMGraphic.
 */
public abstract class OMRasterObject
      extends OMGraphicAdapter
      implements OMGraphic, ImageObserver {

   private static final long serialVersionUID = 1L;
   /**
    * The direct colormodel, for OMRasters, means the integer values passed in
    * as pixels, already reflect the RGB color values each pixel should display.
    */
   public final static int COLORMODEL_DIRECT = 0;
   /**
    * The indexed colormodel, for OMRasters, means that the byte array passed in
    * for the pixels has to be resolved with a colortable in order to create a
    * integer array of RGB pixels.
    */
   public final static int COLORMODEL_INDEXED = 1;
   /**
    * The ImageIcon colormodel means that the image is externally set, and we
    * just want to to display the image at the given location.
    */
   public final static int COLORMODEL_IMAGEICON = 2;
   /** If scaling the image, use the slower, smoothing algorithm. */
   public final static int SMOOTH_SCALING = 0;
   /**
    * If scaling the image, use the faster, replicating/clipping algorithm.
    */
   public final static int FAST_SCALING = 1;
   /**
    * colorModel helps figure out what kind of updates are necessary, by knowing
    * what kind of image we're dealing with. For the images created with a
    * ImageIcon, the attribute updates that don't relate to position will not
    * take affect.
    */
   protected int colorModel = COLORMODEL_DIRECT;
   /**
    * The pixels are used for the image that is drawn on the window. The pixels
    * are either passed in as an int[] in some constructors of the OMRaster, or
    * it is constructed in the OMBitmap and in OMRasters that have a colortable.
    */
   protected int[] pixels = null;

   /**
    * Horizontal location of the upper left corner of the image, or the x offset
    * from the lon for that corner, in pixels.
    */
   protected int x = 0;

   /**
    * Vertical location of the upper left corner of the image, or the y offset
    * from the lat for that corner, in pixels.
    */
   protected int y = 0;

   /**
    * The latitude of the upper left corner for the image, in decimal degrees.
    */
   protected double lat = 0.0f;

   /**
    * The longitude of the upper left corner for the image, in decimal degrees.
    */
   protected double lon = 0.0f;

   /**
    * The width of the image, in pixels. This always reflects the width of the
    * original image, even if a filter is applied to the image.
    */
   protected int width = 0;

   /**
    * The height of the image, in pixels. This always reflects the height of the
    * original image, even if a filter is applied to the image.
    */
   protected int height = 0;

   /**
    * The byte info for the image. OMBitmaps use each bit as an indication to
    * use the lineColor or the fillColor for each pixel (like a XBitmap).
    * OMRasters only use the bits when the image being created follows the
    * indexed colormodel. Then, the bits hold the colortable indexes that each
    * pixel needs to have a color substituted in later.
    */
   protected byte[] bits = null;

   /** The bitmap is drawn to the graphics. */
   protected transient Image bitmap = null;

   /**
    * Projected window pixel location of the upper left corner of the image.
    */
   protected transient Point point1 = null;

   /**
    * Projected window pixel location of the lower right corner of the image.
    */
   protected transient Point point2 = null;

   /**
    * The width of the image after scaling, if you want the image to be a
    * different size than the source.
    */
   protected int filteredWidth = 0;

   /**
    * The height of the image after scaling, if you want the image to be a
    * different size than the source.
    */
   protected int filteredHeight = 0;

   /** The image filter to use on the constructed image. */
   protected ImageFilter imageFilter = null;

   /**
    * Set if the projection has had attributes change that require a
    * repositioning of the image, not a regeneration.
    */
   protected boolean needToReposition = true;

   /**
    * Pixel height of the current projection. Used for efficient zoom-in
    * scaling.
    */
   transient int projHeight;

   /**
    * Pixel width of the current projection. Used for efficient zoom-in scaling.
    */
   transient int projWidth;

   /** the angle by which the image is to be rotated, in radians */
   protected double rotationAngle;

   public static Logger logger = Logger.getLogger("com.bbn.openmap.omGraphics.OMRasterObject");

   protected transient boolean DEBUG = logger.isLoggable(Level.FINE);

   /**
    * A Constructor that sets the graphic type to raster, render type to
    * unknown, line type to unknown, and the declutter type to none.
    */
   public OMRasterObject() {
      super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
   }

   /**
    * A Constructor that sets the graphic type, render type, line type and the
    * declutter type to the values you pass in. See OMGraphic for the
    * definitions of these attributes.
    * 
    * @param rType render type
    * @param lType line type
    * @param dcType declutter type
    */
   public OMRasterObject(int rType, int lType, int dcType) {
      super(rType, lType, dcType);
   }

   /**
    * The color model is set based on the constructor. This setting controls
    * what parameter changes are possible for different models of images.
    * 
    * @param cm the colormode that describes how the colors are being set -
    *        COLORMODEL_DIRECT, COLORMODEL_INDEXED, or COLORMODEL_IMAGEICON.
    */
   protected void setColorModel(int cm) {
      colorModel = cm;
   }

   /**
    * Get the color model type of the image.
    * 
    * @return COLORMODEL_DIRECT, COLORMODEL_INDEXED, or COLORMODEL_IMAGEICON.
    */
   public int getColorModel() {
      return colorModel;
   }

   /**
    * Set the flag for the object that lets the render method (which draws the
    * object) know that the object needs to be repositioned first.
    */
   public void setNeedToReposition(boolean value) {
      needToReposition = value;
   }

   /** Return the reposition status. */
   public boolean getNeedToReposition() {
      return needToReposition;
   }

   /**
    * Set the angle by which the image is to rotated.
    * 
    * @param angle the number of radians the image is to be rotated. Measured
    *        clockwise from horizontal.
    */
   public void setRotationAngle(double angle) {
      this.rotationAngle = angle;
      setNeedToRegenerate(true);
   }

   /**
    * Get the current rotation of the image.
    * 
    * @return the image rotation.
    */
   public double getRotationAngle() {
      return rotationAngle;
   }

   /**
    * Compute the raster objects pixels, based on the color model and the byte
    * values.
    * 
    * @return true if everything goes OK (height*width = pixel.length, etc.).
    */
   protected abstract boolean computePixels();

   /**
    * Called from within render(). This method should call rotate() on the
    * provided Graphics2D object, setting the rotation angle and the rotation
    * point. By default, the rotation angle is whatever is set in the
    * OMRasterObject, and the rotation point is the offset point plus half the
    * image width in the horizontal direction, and half the image in the
    * vertical direction.
    */
   protected void rotate(Graphics2D g) {
      int w = width;
      int h = height;

      Shape projectedShape = getShape();
      if (projectedShape != null) {
         java.awt.Rectangle rect = projectedShape.getBounds();
         w = (int) rect.getWidth();
         h = (int) rect.getHeight();
      }

      ((Graphics2D) g).rotate(rotationAngle, point1.x + w / 2, point1.y + h / 2);
   }

   /**
    * Render the raster on the java.awt.Graphics
    * 
    * @param graphics java.awt.Graphics to draw the image on.
    */
   public void render(Graphics graphics) {
      if (getNeedToRegenerate() || getNeedToReposition() || !isVisible()) {
         if (DEBUG) {
            logger.fine("OMRasterObject.render(): need to regenerate or not visible!");
         }
         return;
      }

      // copy the graphic, so our transform doesn't cascade to
      // others...
      Graphics g = graphics.create();

      // Just a little check to find out if someone is rushing
      // things. If a Image isn't fully loaded, the getWidth will
      // return -1. This is just a courtesy notification in case
      // someone isn't seeing their image, and don't know why.
      if (colorModel == COLORMODEL_IMAGEICON && (getWidth() == -1)) {
         logger.fine("OMRasterObject.render: Attempting to draw a Image that is not ready! Image probably wasn't available.");
      }

      if (g instanceof Graphics2D && rotationAngle != DEFAULT_ROTATIONANGLE) {
         // rotate about our image center point
         rotate((Graphics2D) g);
      }

      if (bitmap != null) {

         if (DEBUG) {
            logger.fine("OMRasterObject.render() | drawing " + width + "x" + height + " image at " + point1.x + ", " + point1.y);
         }
         if (g instanceof Graphics2D && bitmap instanceof RenderedImage) {
            // Affine translation for placement...
            ((Graphics2D) g).drawRenderedImage((RenderedImage) bitmap, new AffineTransform(1f, 0f, 0f, 1f, point1.x, point1.y));
            // Undo the affine translation for future graphics?? Apparently
            // not...
            // ((Graphics2D) g).translate(-point1.x, -point1.y);
         } else {
            g.drawImage(bitmap, point1.x, point1.y, this);
         }
      } else {
         if (DEBUG)
            logger.fine("OMRasterObject.render: ignoring null bitmap");
      }

      if (isSelected() || logger.isLoggable(Level.FINER)) {
         super.render(g);
      }

      renderLabel(graphics);
   }

   /**
    * Set the rectangle, based on the location and size of the image.
    */
   public void setShape() {

      // generate shape that is a boundary of the generated image.
      // We'll make it a GeneralPath rectangle.
      int w = width;
      int h = height;

      if (imageFilter != null) {
         w = filteredWidth;
         h = filteredHeight;
      }

      setShape(createBoxShape(point1.x, point1.y, w, h));
   }

   /**
    * Since the image doesn't necessarily need to be regenerated when it is
    * merely moved, raster objects have this function, called from generate()
    * and when a placement attribute is changed.
    * 
    * @return true if enough information is in the object for proper placement.
    * @param proj projection of window.
    */
   protected boolean position(Projection proj) {

      if (proj == null) {
         logger.fine("OMRasterObject: null projection in position!");
         return false;
      }

      projWidth = proj.getWidth();
      projHeight = proj.getHeight();

      switch (renderType) {

         case RENDERTYPE_LATLON:
            if (!proj.isPlotable(lat, lon)) {
               if (DEBUG) {
                  logger.fine("OMRasterObject: point is not plotable!");
               }
               setNeedToReposition(true);// so we don't render it!
               return false;
            }
            point1 = (Point) proj.forward(lat, lon, new Point());
            break;
         case RENDERTYPE_XY:
            point1 = new Point(x, y);
            break;
         case RENDERTYPE_OFFSET:
            if (!proj.isPlotable(lat, lon)) {
               if (DEBUG) {
                  logger.fine("OMRasterObject: point is not plotable!");
               }
               setNeedToReposition(true);// so we don't render it!
               return false;
            }
            point1 = (Point) proj.forward(lat, lon, new Point());
            point1.x += x;
            point1.y += y;
            break;
         case RENDERTYPE_UNKNOWN:
            if (DEBUG) {
               logger.fine("OMRasterObject.position(): ignoring unknown rendertype, wingin' it");
            }
            if (lat == 0 && lon == 0) {
               if (x == 0 && y == 0) {
                  if (DEBUG) {
                     logger.fine("OMRasterObject.position(): Not enough info in object to place it reasonably.");
                  }
                  point1 = new Point(-width, -height);
                  point2 = new Point(0, 0);
                  return false;
               } else {
                  point1 = new Point(x, y);
               }

            } else {
               if (!proj.isPlotable(lat, lon)) {
                  logger.fine("OMRasterObject: point is not plotable!");
                  return false;
               }
               point1 = (Point) proj.forward(lat, lon, new Point());
            }
            break;
      }

      point2 = new Point(0, 0);
      point2.x = point1.x + width;
      point2.y = point1.y + height;
      setNeedToReposition(false);
      return true;
   }

   /**
    * Set the image to be drawn, if the color model is COLORMODEL_IMAGEICON.
    * 
    * @param ii the image icon to use.
    */
   public void setImage(Image ii) {
      if (ii == null) {
         logger.fine("OMRasterObject.setImage(): image is null!");
         return;
      }
      colorModel = COLORMODEL_IMAGEICON;
      bitmap = ii;
      // Make sure the image is ready to draw. If not, this method
      // will be called again by the ImageObserver method
      // imageUpdate. Set the height and width anyway. If they are
      // -1, you know the image isn't ready - another way to find
      // out.
      width = bitmap.getWidth(this);
      height = bitmap.getHeight(this);

      if (!(ii instanceof RenderedImage)) {
         Toolkit.getDefaultToolkit().prepareImage(bitmap, -1, -1, this);
      }
   }

   /**
    * Get the image that will be put on the window.
    * 
    * @return the Image created by computePixels and generate().
    */
   public Image getImage() {
      return bitmap;
   }

   /**
    * Always true for images, affects distance measurements. Forces the
    * omGraphics package to treat the OMRasterObject as a filled shape.
    */
   public boolean shouldRenderFill() {
      return true;
   }

   /**
    * Set the pixels for the image for direct color model images. Checks to see
    * of the length matches the height * width, but doesn't do anything if they
    * don't match. Make sure it does.
    * 
    * @param values the pixel values.
    */
   public void setPixels(int[] values) {
      if (values.length != (height * width))
         logger.fine("OMRasterObject: new pixel[] size (" + +values.length + ") doesn't" + " match [height*width (" + height
               * width + ")]");
      pixels = values;
      setNeedToRegenerate(true);
   }

   /**
    * Return the pixels used for the image.
    * 
    * @return the integer array of ints used as integer colors for each pixel of
    *         the image.
    */
   public int[] getPixels() {
      return pixels;
   }

   /**
    * Change the x attribute, which matters only if the render type is
    * RENDERTYPE_XY or RENDERTYPE_OFFSET.
    * 
    * @param value the x location in pixels.
    */
   public void setX(int value) {
      if (x == value)
         return;
      x = value;
      setNeedToReposition(true);
   }

   /**
    * Returns the x attribute.
    * 
    * @return the x value, pixels from left of window or image origin.
    */
   public int getX() {
      return x;
   }

   /**
    * Change the y attribute, which matters only if the render type is
    * RENDERTYPE_XY or RENDERTYPE_OFFSET.
    * 
    * @param value the y location in pixels
    */
   public void setY(int value) {
      if (y == value)
         return;
      y = value;
      setNeedToReposition(true);
   }

   /**
    * Return the y attribute.
    * 
    * @return the y value, pixels from top or image origin.
    */
   public int getY() {
      return y;
   }

   /**
    * Return the map location of the image, after generation.
    * 
    * @return Point, null if not projected yet.
    */
   public Point getMapLocation() {
      return point1;
   }

   /**
    * Change the latitude attribute, which matters only if the render type is
    * RENDERTYPE_LATLON or RENDERTYPE_OFFSET.
    * 
    * @param value latitude in decimal degrees.
    */
   public void setLat(double value) {
      if (lat == value)
         return;
      lat = value;
      setNeedToReposition(true);
   }

   /**
    * Get the latitude.
    * 
    * @return the latitude in decimal degrees.
    */
   public double getLat() {
      return lat;
   }

   /**
    * Change the longitude attribute, which matters only if the render type is
    * RENDERTYPE_LATLON or RENDERTYPE_OFFSET.
    * 
    * @param value the longitude in decimal degrees.
    */
   public void setLon(double value) {
      if (lon == value)
         return;
      lon = value;
      setNeedToReposition(true);
   }

   /**
    * Get the longitude.
    * 
    * @return longitude in decimal degrees.
    */
   public double getLon() {
      return lon;
   }

   /**
    * Set the height of the image, in pixels.
    * 
    * @param value height in pixels.
    */
   public void setHeight(int value) {
      if (height == value)
         return;
      setNeedToRegenerate(true);
      height = value;
   }

   /**
    * Get the height of image.
    * 
    * @return height in pixels.
    */
   public int getHeight() {
      return height;
   }

   /**
    * Get the height of image after a filter was applied.
    * 
    * @return filteredHeight in pixels.
    */
   public int getFilteredHeight() {
      return filteredHeight;
   }

   /**
    * Set width of image.
    * 
    * @param value width in pixels.
    */
   public void setWidth(int value) {
      if (width == value)
         return;
      setNeedToRegenerate(true);
      width = value;
   }

   /**
    * Get width of image.
    * 
    * @return width of image in pixels.
    */
   public int getWidth() {
      return width;
   }

   /**
    * Get width of image, after a filter is applied.
    * 
    * @return filteredWidth of image in pixels.
    */
   public int getFilteredWidth() {
      return filteredWidth;
   }

   /**
    * Set the bytes used to create the pixels used to create the image. Used for
    * indexed color model images in OMRaster, and OMBitmaps.
    * 
    * @param values byte values
    */
   public void setBits(byte[] values) {
      setNeedToRegenerate(true);
      bits = values;
   }

   /**
    * Get the byte values for indexed color model images and OMBitmaps.
    * 
    * @return the bytes used to create the pixels.
    */
   public byte[] getBits() {
      return bits;
   }

   /**
    * Set a filter to be used on the constructed image. Applied at generate().
    * 
    * @param filter Image filter to apply to constructed raster.
    */
   public void setImageFilter(ImageFilter filter) {
      imageFilter = filter;
      filteredWidth = width;
      filteredHeight = height;

      setNeedToRegenerate(true);
   }

   /**
    * Return the image filter used on the image.
    * 
    * @return imagefilter, null if one wasn't set.
    */
   public ImageFilter getImageFilter() {
      return imageFilter;
   }

   /**
    * Convenience function to scale the Image to the xy size. Sets the
    * imageFilter to a ReplicateScaleFilter or AreaAveragingScaleFilter,
    * depending on the algorithm type.
    * 
    * @param w width to scale to, in pixels
    * @param h height to scale to, in pixels
    * @param algorithmType OMRasterObject parameter describing which scaling
    *        algorithm to use.
    */
   public void scaleTo(int w, int h, int algorithmType) {
      filteredWidth = w;
      filteredHeight = h;

      imageFilter = new TrimScaleFilter(filteredWidth, filteredHeight, algorithmType);
      setNeedToRegenerate(true);
   }

   /**
    * A method used to manipulate the image according to the parameters set by
    * the imageFilter in the OMRasterObject. Called from generate() if the
    * filteredWidth and filteredHeight differ from width and height.
    * 
    * @param image the Image to filter
    * @return the filtered image.
    */
   protected Image filterImage(Image image) {

      // Can we do a little clipping here?? If it's been projected,
      // maybe.

      // See if the frame is getting blown up, probably by at
      // least a certain margin, so we know that there will be time
      // savings as well as memory savings.

      if (imageFilter instanceof TrimScaleFilter) {
         TrimScaleFilter tf = (TrimScaleFilter) imageFilter;
         Image img = tf.trimExcessPixels();

         if (img != null) {
            image = img;
            imageFilter = tf.getFilterWithChanges();
            // we can play around with point1, since that is where
            // the
            // image is getting laid out. If point1.x or point1.y
            // < 0, we
            // can set it to zero. Assumes that the image has
            // already
            // been positioned.
            if (point1.x < 0)
               point1.x = 0;
            if (point1.y < 0)
               point1.y = 0;

            if (DEBUG) {
               logger.fine("OMRasterObject: newly located at " + point1);
            }
         } else if (DEBUG) {
            logger.fine("OMRasterObject: not being trimmed due to projection");
         }
      }

      if (Toolkit.getDefaultToolkit() != null && image != null) {
         ImageProducer prod = new FilteredImageSource(image.getSource(), imageFilter);
         return Toolkit.getDefaultToolkit().createImage(prod);
      } else
         return image;
   }

   /**
    * From the Image Observer Interface. Called when the image bits have
    * arrived, and therefore calls setImage() to reset all the OMRasterObject
    * parameters. <br>
    * Don't call this method!
    */
   public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {

      if ((infoflags & ImageObserver.ALLBITS) != 0) {
         if (colorModel == COLORMODEL_IMAGEICON) {
            setImage(img);
         }
         return false;// all set
      }
      return true;// need more info
   }

   /**
    * This is an effort to create an scaling ImageFilter that will trim off the
    * unused pixels, lessoning the load on the display server. It depends on
    * knowing several things about the projection and the current image
    * parameters, which is why it's not a stand-alone filter class.
    */
   protected class TrimScaleFilter
         extends AreaAveragingScaleFilter {
      ImageFilter actualFilter = null;
      int algorithmType;

      /**
       * Constructs an TrimScaleFilter that scales the pixels from its source
       * Image as specified by the width and height parameters.
       * 
       * @param width the target width to scale the image
       * @param height the target height to scale the image
       */
      public TrimScaleFilter(int width, int height) {
         super(width, height);
         algorithmType = FAST_SCALING;
      }

      /**
       * Constructs an AreaAveragingScaleFilter that scales the pixels from its
       * source Image as specified by the width and height parameters.
       * 
       * @param width the target width to scale the image
       * @param height the target height to scale the image
       * @param algorithmType FAST_SCALING or SMOOTH_SCALING - FAST is much
       *        faster!
       */
      protected TrimScaleFilter(int width, int height, int algorithmType) {
         super(width, height);
         this.algorithmType = algorithmType;
      }

      /**
       * Detect if the data is being delivered with the necessary hints to allow
       * the averaging algorithm to do its work. If the algorithmType is set to
       * FAST, I manipulate the hints to force the filter to act like a
       * ReplicateScaleFilter.
       * 
       * @see ImageConsumer#setHints
       */
      public void setHints(int hints) {
         int passthrough = hints;
         if (algorithmType == FAST_SCALING) {
            // / XOR
            passthrough = hints ^ ImageConsumer.TOPDOWNLEFTRIGHT;
         }
         super.setHints(passthrough);
      }

      /**
       * The filter must change if the requested image size changes because of
       * clipping. Get the good filter here, after calling trimExcessPixels().
       */
      protected ImageFilter getFilterWithChanges() {
         if (actualFilter == null) {
            return this;
         }
         return actualFilter;
      }

      /**
       * Get a trimmed-down image to expand to the map, that contains all the
       * pixels that will be visible after expansion. Returns null if the image
       * should be used as is, and the filter as well.
       */
      protected Image trimExcessPixels() {

         if (filteredWidth <= width && filteredHeight <= height) {
            if (DEBUG) {
               logger.fine("TrimScaleFilter.trimExcessPixels(): image not enlarged, using entire image.");
            }
            return null;
         }

         if (DEBUG) {
            logger.fine("TrimScaleFilter.trimExcessPixels(): clipping enlarged image.");
         }

         // Figure out the pixels of the old image being used in
         // the new image. Figure out the proj location of the
         // upper
         // left pixel of the new image. We want to substitute this
         // proj location for the projection location already
         // calculated. This should get overwritten later for any
         // projection changes.
         float widthScale = (float) filteredWidth / (float) width;
         float heightScale = (float) filteredHeight / (float) height;

         int startXPixelInSource = point1.x < 0 ? (int) ((-1.0 * point1.x) / widthScale) : 0;
         int startYPixelInSource = point1.y < 0 ? (int) ((-1 * point1.y) / heightScale) : 0;
         Point scaledDim = new Point((int) (point1.x + (width * widthScale)), (int) (point1.y + (height * heightScale)));
         int endXPixelInSource = (scaledDim.x > projWidth ? (int) ((projWidth - point1.x) / widthScale) + 1 : width);
         int endYPixelInSource = scaledDim.y > projHeight ? (int) ((projHeight - point1.y) / heightScale) + 1 : height;

         if (DEBUG) {
            logger.fine("TrimScaleFilter.trimExcessPixels(): image contributes " + startXPixelInSource + ", " + startYPixelInSource
                  + " to " + endXPixelInSource + ", " + endYPixelInSource);
         }

         // Create a buffered image out of the old image, clipping
         // out the unused pixels.

         if (DEBUG) {
            logger.fine("TrimScaleFilter.trimExcessPixels(): " + " new dimensions of scaled image "
                  + (int) ((endXPixelInSource - startXPixelInSource) * widthScale) + ", "
                  + (int) ((endYPixelInSource - startYPixelInSource) * heightScale));
         }

         // Get only the pixels you need.
         // Use a pixel grabber to get the right pixels.
         PixelGrabber pg =
               new PixelGrabber(bitmap, startXPixelInSource, startYPixelInSource, endXPixelInSource - startXPixelInSource,
                                endYPixelInSource - startYPixelInSource, true);
         int[] pix = ImageHelper.grabPixels(pg);
         if (pix == null) {
            return null;
         }

         // Set the filter to the dimensions. Need to remember to
         // ask for this!!!
         actualFilter =
               new TrimScaleFilter((int) ((endXPixelInSource - startXPixelInSource) * widthScale),
                                   (int) ((endYPixelInSource - startYPixelInSource) * heightScale), algorithmType);

         // create the new bitmap, which holds the image that gets
         // drawn
         Toolkit tk = Toolkit.getDefaultToolkit();
         Image image =
               tk.createImage(new MemoryImageSource(endXPixelInSource - startXPixelInSource, endYPixelInSource
                     - startYPixelInSource, pix, 0, endXPixelInSource - startXPixelInSource));
         return image;
      }
   }

   /**
    * Code derived from
    * http://www.dcs.shef.ac.uk/~tom/Java/Power/image_serialization.html
    */
   private void writeObject(ObjectOutputStream objectstream)
         throws IOException {

      // write non-transient, non-static data
      objectstream.defaultWriteObject();
      PixelGrabber grabber = new PixelGrabber(bitmap, 0, 0, -1, -1, true);

      if (colorModel == COLORMODEL_IMAGEICON && bitmap != null) {
         try {
            grabber.grabPixels();
         } catch (InterruptedException e) {
            System.out.println("error grabbing pixels");
         }

         Object pix = grabber.getPixels();
         Dimension dim = new Dimension(bitmap.getWidth(this), bitmap.getHeight(this));
         objectstream.writeObject(dim);
         objectstream.writeObject(pix);
      }
   }

   /**
    * Code derived from
    * http://www.dcs.shef.ac.uk/~tom/Java/Power/image_serialization.html
    */
   private void readObject(ObjectInputStream objectstream)
         throws IOException, ClassNotFoundException {

      Toolkit toolkit = Toolkit.getDefaultToolkit();
      try {
         // read non-transient, non-static data
         objectstream.defaultReadObject();

         if (colorModel == COLORMODEL_IMAGEICON) {
            Dimension dim = (Dimension) objectstream.readObject();
            Object img = objectstream.readObject();
            int[] pix = (int[]) img;
            bitmap = toolkit.createImage(new MemoryImageSource(dim.width, dim.height, pix, 0, dim.width));
            setImage(bitmap);
         }

      } catch (ClassNotFoundException ce) {
         System.out.println("class not found");
      }
   }

   public boolean hasLineTypeChoice() {
      return false;
   }

   public void restore(OMGeometry source) {
      super.restore(source);
      if (source instanceof OMRasterObject) {
         OMRasterObject rasterO = (OMRasterObject) source;
         this.colorModel = rasterO.colorModel;
         this.pixels = DeepCopyUtil.deepCopy(rasterO.pixels);
         this.x = rasterO.x;
         this.y = rasterO.y;
         this.lat = rasterO.lat;
         this.lon = rasterO.lon;
         this.width = rasterO.width;
         this.height = rasterO.height;
         this.bits = DeepCopyUtil.deepCopy(rasterO.bits);
         this.filteredWidth = rasterO.filteredWidth;
         this.filteredHeight = rasterO.filteredHeight;
         this.rotationAngle = rasterO.rotationAngle;
         // OKOK, again, not a deep copy.
         this.imageFilter = rasterO.imageFilter;
      }
   }
}