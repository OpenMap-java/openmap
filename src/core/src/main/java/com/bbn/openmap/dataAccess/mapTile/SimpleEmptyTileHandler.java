package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.logging.Logger;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * An EmptyTileHandler that uses DrawingAttributes to create a rectangle to fill in for empty tiles. You can set one of
 * these up using the properties for a MapTileLayer, and those properties will trickle down through the MapTileServer,
 * which will in turn create one of these. <P>
 *
 * <pre>
 * emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.SimpleEmptyTileHandler
 *
 * # The zoom level to start using the no coverage parameters. O by default, so that the
 * # noCoverage parameters are used to create what is sent back by default.
 * noCoverageZoom=zoom level when you don't want empty tiles, you want no coverage tiles
 *
 * # If an image is not defined, these colors will be used to create no coverage
 * # tiles.  If not specified, nothing will be sent back.
 * noCoverage.fillColor=hex RGB color
 * noCoverage.lineColor=hex RGB color
 * noCoverage.fillPattern=path to resource, file or URL of pattern to use for tile fill.
 *
 * # If not specified, no image will be returned if zoom level less than noCoverageZoom.  If you
 * # want to use these parameters to set up on-the-fly filled tile images, make sure you also adjust
 * # the noCoverageZoom level.
 * background.fillColor=hex RGB color
 * background.lineColor=hex RGB color
 * background.fillPattern=path to resource, file or URL of pattern to use for tile fill.
 *
 * </pre>
 *
 * @author ddietrick
 */
public class SimpleEmptyTileHandler
        implements EmptyTileHandler, PropertyConsumer {

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.EmptyTileHandler");
   public final static String BACKGROUND_PROPERTY = "background";
   public final static String NO_COVERAGE_PROPERTY = "noCoverage";
   public final static String NO_COVERAGE_ZOOM_PROPERTY = "noCoverageZoom";
   public final static int TILE_SIZE = 256;
   protected DrawingAttributes backgroundAtts = DrawingAttributes.getDefaultClone();
   protected DrawingAttributes noCoverageAtts = DrawingAttributes.getDefaultClone();
   // Property prefix
   protected String prefix;
   protected BufferedImage emptyTileImage;
   protected BufferedImage backgroundTileImage;
   /**
    * The zoom level at which point the EmptyTileHandler will create no-coverage tiles, if defined.
    */
   protected int noCoverageZoom = 0;

   // Needed for ComponentFactory construction
   public SimpleEmptyTileHandler() {
      noCoverageAtts.setLinePaint(OMColor.clear);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.bbn.openmap.dataAccess.mapTile.EmptyTileHandler#getOMGraphicForEmptyTile (java.lang.String, int, int,
    * int, com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform, com.bbn.openmap.proj.Projection)
    */
   public BufferedImage getImageForEmptyTile(String imagePath, int x, int y, int zoomLevel,
           MapTileCoordinateTransform mtcTransform, Projection proj) {
      if (zoomLevel < noCoverageZoom) {
         return backgroundTileImage;
      } else {
         logger.fine("returning emptyTileImage: " + emptyTileImage);
         return emptyTileImage;
      }
   }

   public void setPropertyPrefix(String pref) {
      prefix = pref;
   }

   public String getPropertyPrefix() {
      return prefix;
   }

   public void setProperties(Properties props) {
      setProperties(null, props);
   }

   public void setProperties(String prefix, Properties props) {
      setPropertyPrefix(prefix);

      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      DrawingAttributes backgroundDA = DrawingAttributes.getDefaultClone();
      DrawingAttributes noCoverageDA = DrawingAttributes.getDefaultClone();

      backgroundDA.setProperties(prefix + BACKGROUND_PROPERTY, props);
      noCoverageDA.setProperties(prefix + NO_COVERAGE_PROPERTY, props);

      setBackgroundAtts(backgroundDA);
      setNoCoverageAtts(noCoverageDA);

      noCoverageZoom = PropUtils.intFromProperties(props, prefix + NO_COVERAGE_ZOOM_PROPERTY, noCoverageZoom);
   }

   public Properties getProperties(Properties props) {
      if (props == null) {
         props = new Properties();
      }

      getBackgroundAtts().getProperties(props);
      getNoCoverageAtts().getProperties(props);
      return props;
   }

   public Properties getPropertyInfo(Properties props) {
      if (props == null) {
         props = new Properties();
      }

      getBackgroundAtts().getPropertyInfo(props);
      getNoCoverageAtts().getPropertyInfo(props);
      return props;
   }

   /**
    * Create a BufferedImage from the provided DrawingAttributes.
    *
    * @param da DrawingAttributes
    * @return BudderedImage with TYPE_INT_ARGB
    */
   protected BufferedImage createTileImageFromDrawingAttributes(DrawingAttributes da) {
      BufferedImage bi = null;

      if (da != null) {
         OMRect rect = new OMRect(0, 0, TILE_SIZE, TILE_SIZE);
         da.setTo(rect);
         rect.generate(new com.bbn.openmap.proj.Mercator(new LatLonPoint.Double(), 100000, TILE_SIZE, TILE_SIZE));

         bi = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
         Graphics g = bi.getGraphics();
         rect.render(g);
         g.dispose();
      }

      return bi;
   }

   /**
    * @return the backgroundAtts
    */
   public DrawingAttributes getBackgroundAtts() {
      return backgroundAtts;
   }

   /**
    * Set the background drawing attributes and create the cached backgroundTileImage if the drawing attributes provided
    * doesn't match what's there. If backgroundAtts never gets set or is set to the default DrawingAttributes object,
    * the backgroundTileImage won't get created and no background images will be returned.
    *
    * @param backgroundAtts the backgroundAtts to set
    */
   public void setBackgroundAtts(DrawingAttributes backgroundAtts) {
      if (backgroundAtts != null && !backgroundAtts.equals(this.backgroundAtts)) {
         backgroundTileImage = createTileImageFromDrawingAttributes(backgroundAtts);
         this.backgroundAtts = backgroundAtts;
      }

      if (this.backgroundAtts == null) {
         this.backgroundAtts = DrawingAttributes.getDefaultClone();
      }
   }

   /**
    * @return the noCoverageAtts
    */
   public DrawingAttributes getNoCoverageAtts() {
      return noCoverageAtts;
   }

   /**
    * Set the no-coverage drawing attributes and create the cached emptyTileImage if the drawing attributes provided
    * doesn't match what's there. If noCoverageAtts never gets set or is set to the default DrawingAttributes object,
    * the emptyTileImage won't get created and no no-coverage images will be returned.
    *
    * @param noCoverageAtts the noCoverageAtts to set
    */
   public void setNoCoverageAtts(DrawingAttributes noCoverageAtts) {
      if (noCoverageAtts != null && !noCoverageAtts.equals(this.noCoverageAtts)) {
         logger.fine("the no coverage atts are not standard, creating a new emptyTileImage");
         emptyTileImage = createTileImageFromDrawingAttributes(noCoverageAtts);
         this.noCoverageAtts = noCoverageAtts;
      } else {
         logger.fine("++++++++ " + noCoverageAtts + " vs " + this.noCoverageAtts);
      }

      if (this.noCoverageAtts == null) {
         this.noCoverageAtts = DrawingAttributes.getDefaultClone();
      }
   }

   /**
    * @return the noCoverageZoom
    */
   public int getNoCoverageZoom() {
      return noCoverageZoom;
   }

   /**
    * @param noCoverageZoom the noCoverageZoom to set
    */
   public void setNoCoverageZoom(int noCoverageZoom) {
      this.noCoverageZoom = noCoverageZoom;
   }
}
