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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMDistance.java,v $
// $RCSfile: OMDistance.java,v $
// $Revision: 1.12 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.Iterator;

import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.DeepCopyUtil;

/**
 * OMGraphic object that represents a polyline, labeled with distances.
 */
public class OMDistance
      extends OMPoly {

   protected OMGraphicList labels = new OMGraphicList();
   protected OMGraphicList points = new OMGraphicList();

   protected Length distUnits = Length.NM;
   public DecimalFormat df = new DecimalFormat("0.#");
   /**
    * Paint used for labels
    */
   protected Paint labelPaint;
   /**
    * Font used for labels
    */
   protected Font labelFont;

   /**
    * Construct a default OMDistance.
    */
   public OMDistance() {
      super();
      setRenderType(RENDERTYPE_LATLON);
   }

   /**
    * Create an OMDistance from a list of float lat/lon pairs.
    * <p>
    * NOTES:
    * <ul>
    * <li>llPoints array is converted into radians IN PLACE for more efficient
    * handling internally if it's not already in radians! For even better
    * performance, you should send us an array already in radians format!
    * <li>If you want the poly to be connected (as a polygon), you need to
    * ensure that the first and last coordinate pairs are the same.
    * </ul>
    * 
    * @param llPoints array of lat/lon points, arranged lat, lon, lat, lon, etc.
    * @param units radians or decimal degrees. Use OMGraphic.RADIANS or
    *        OMGraphic.DECIMAL_DEGREES
    * @param lType line type, from a list defined in OMGraphic.
    */
   public OMDistance(double[] llPoints, int units, int lType, Length distanceUnits) {
      this(llPoints, units, lType, -1, distanceUnits);
   }

   /**
    * Create an OMDistance from a list of float lat/lon pairs.
    * <p>
    * NOTES:
    * <ul>
    * <li>llPoints array is converted into radians IN PLACE for more efficient
    * handling internally if it's not already in radians! For even better
    * performance, you should send us an array already in radians format!
    * <li>If you want the poly to be connected (as a polygon), you need to
    * ensure that the first and last coordinate pairs are the same.
    * </ul>
    * 
    * @param llPoints array of lat/lon points, arranged lat, lon, lat, lon, etc.
    * @param units radians or decimal degrees. Use OMGraphic.RADIANS or
    *        OMGraphic.DECIMAL_DEGREES
    * @param lType line type, from a list defined in OMGraphic.
    * @param nsegs number of segment points (only for LINETYPE_GREATCIRCLE or
    *        LINETYPE_RHUMB line types, and if &lt; 1, this value is generated
    *        internally)
    */
   public OMDistance(double[] llPoints, int units, int lType, int nsegs, Length distanceUnits) {
      super(llPoints, units, lType, nsegs);
      setDistUnits(distanceUnits);
   }

   /**
    * Set the Length object used to represent distances.
    */
   public void setDistUnits(Length distanceUnits) {
      distUnits = distanceUnits;
   }

   /**
    * Get the Length object used to represent distances.
    */
   public Length getDistUnits() {
      return distUnits;
   }

   public void setLocation(double[] llPoints, int units) {
      this.units = OMGraphic.RADIANS;
      if (units == OMGraphic.DECIMAL_DEGREES) {
         ProjMath.arrayDegToRad(llPoints);
      }
      rawllpts = llPoints;
      setNeedToRegenerate(true);
      setRenderType(RENDERTYPE_LATLON);
   }

   public void createLabels() {
      labels.clear();
      points.clear();

      if (rawllpts == null) {
         return;
      }
      if (rawllpts.length < 4) {
         return;
      }

      Geo lastGeo = new Geo(rawllpts[0], rawllpts[1], units == DECIMAL_DEGREES);
      double latpnt = rawllpts[0];
      double lonpnt = rawllpts[1];
      if (units == RADIANS) {
         latpnt = ProjMath.radToDeg(latpnt);
         lonpnt = ProjMath.radToDeg(lonpnt);
      }
      points.add(new OMPoint(latpnt, lonpnt, 1));
      Geo curGeo = null;
      float cumulativeDist = 0f;
      for (int p = 2; p < rawllpts.length; p += 2) {
         if (curGeo == null) {
            curGeo = new Geo(rawllpts[p], rawllpts[p + 1], units == DECIMAL_DEGREES);
         } else {
            if (units == DECIMAL_DEGREES) {
               curGeo.initialize(rawllpts[p], rawllpts[p + 1]);
            } else {
               curGeo.initializeRadians(rawllpts[p], rawllpts[p + 1]);
            }
         }

         float dist = getDist(lastGeo, curGeo);
         cumulativeDist += dist;

         labels.add(createLabel(lastGeo, curGeo, dist, cumulativeDist, distUnits));
         latpnt = rawllpts[p];
         lonpnt = rawllpts[p + 1];
         if (units == RADIANS) {
            latpnt = ProjMath.radToDeg(latpnt);
            lonpnt = ProjMath.radToDeg(lonpnt);
         }

         points.add(new OMPoint(latpnt, lonpnt, 1));
         lastGeo.initialize(curGeo);
      }
   }

   /**
    * Get an OMText label for a segments between the given lat/lon points whose
    * given distance and cumulative distance is specified.
    */
   public OMText createLabel(Geo g1, Geo g2, float dist, float cumulativeDist, Length distanceUnits) {
      Geo mid;
      switch (getLineType()) {
         case LINETYPE_STRAIGHT:
            float lat = (float) (g1.getLatitude() + g2.getLatitude()) / 2f;
            float lon = (float) (g1.getLongitude() + g2.getLongitude()) / 2f;
            mid = new Geo(lat, lon);
            break;
         case LINETYPE_RHUMB:
            System.err.println("Rhumb distance calculation not implemented.");
         case LINETYPE_GREATCIRCLE:
         case LINETYPE_UNKNOWN:
         default:
            mid = g1.midPoint(g2);
      }

      // String text = ((int)dist) + " (" + ((int)cumulativeDist) +
      // ")";

      String text =
            (df.format(distanceUnits.fromRadians(dist))) + " (" + (df.format(distanceUnits.fromRadians(cumulativeDist))) + ") "
                  + distanceUnits.getAbbr();
      OMText omtext = new OMText((float) mid.getLatitude(), (float) mid.getLongitude(), text, OMText.JUSTIFY_LEFT);
      // omtext.setLinePaint(new Color(200, 200, 255));
      return omtext;
   }

   /**
    * Return the distance between that lat/lons defined in radians. The returned
    * value is in radians.
    */
   public float getDist(Geo g1, Geo g2) {
      switch (getLineType()) {
         case LINETYPE_STRAIGHT:
            float lonDist = ProjMath.lonDistance((float) g2.getLongitude(), (float) g1.getLongitude());
            float latDist = (float) g2.getLatitude() - (float) g1.getLatitude();
            return (float) Math.sqrt(lonDist * lonDist + latDist * latDist);
         case LINETYPE_RHUMB:
            Debug.error("Rhumb distance calculation not implemented.");
         case LINETYPE_GREATCIRCLE:
         case LINETYPE_UNKNOWN:
         default:
            return (float) g1.distance(g2);
      }
   }

   /**
    * Prepare the poly for rendering.
    * 
    * @param proj Projection
    * @return true if generate was successful
    */
   public boolean generate(Projection proj) {
      boolean ret = super.generate(proj);
      createLabels();
      labels.generate(proj);
      points.generate(proj);
      return ret;
   }

   /**
    * Flag used by the EditableOMDistance to do quick movement paints in a
    * cleaner way.
    */
   protected boolean paintOnlyPoly = false;

   /**
    * Paint the poly. This works if generate() has been successful.
    * 
    * @param g java.awt.Graphics to paint the poly onto.
    */
   public void render(Graphics g) {
      super.render(g);

      if (!paintOnlyPoly) {
         renderPoints(g);
         renderLabels(g);
      }
   }

   /**
    * render points
    */
   protected void renderPoints(Graphics g) {
      Paint pointPaint = getLabelPaint();

      for (Iterator<OMGraphic> it = points.iterator(); it.hasNext();) {
         OMGraphic point = (OMPoint) it.next();
         point.setLinePaint(pointPaint);
         point.setFillPaint(pointPaint);
         point.render(g);
      }
   }

   /**
    * render labels
    */
   protected void renderLabels(Graphics g) {
      Font f = getFont();
      Paint labelPaint = getLabelPaint();
      Paint mattingPaint = getMattingPaint();
      boolean isMatted = isMatted();
      for (Iterator<OMGraphic> it = labels.iterator(); it.hasNext();) {
         OMText text = (OMText) it.next();
         text.setFont(f);
         text.setLinePaint(labelPaint);
         if (isMatted) {
            text.setFillPaint(mattingPaint);
         }
         text.render(g);
      }
   }

   /**
    * Set paint used for labels
    * 
    * @param lPaint paint used for labels
    */
   public void setLabelPaint(Paint lPaint) {
      labelPaint = lPaint;
   }

   /**
    * @return normal paint used for labels
    */
   public Paint getLabelPaint() {
      if (labelPaint == null) {
         return getLinePaint();
      }
      return labelPaint;
   }

   /**
    * @param font font used for labels
    */
   public void setFont(Font font) {
      if (font == null) {
         labelFont = OMText.DEFAULT_FONT;
      } else {
         labelFont = font;
      }
   }

   /**
    * @return font used for labels
    */
   public Font getFont() {
      if (labelFont == null) {
         labelFont = OMText.DEFAULT_FONT;
      }
      return labelFont;
   }

   private void writeObject(java.io.ObjectOutputStream stream)
         throws java.io.IOException {
      stream.defaultWriteObject();
      stream.writeObject(distUnits.getAbbr());
   }

   private void readObject(java.io.ObjectInputStream stream)
         throws java.io.IOException, ClassNotFoundException {
      stream.defaultReadObject();
      distUnits = Length.get((String) stream.readObject());
   }

   public void restore(OMGeometry source) {
      super.restore(source);
      if (source instanceof OMDistance) {
         OMDistance dist = (OMDistance) source;
         this.labels = DeepCopyUtil.deepCopy(dist.labels);
         this.points = DeepCopyUtil.deepCopy(dist.points);
         this.distUnits = dist.distUnits;
         this.df = new DecimalFormat(dist.df.toLocalizedPattern());
         this.labelPaint = dist.labelPaint;
         if (dist.labelFont != null) {
            this.labelFont = dist.labelFont.deriveFont(AffineTransform.TYPE_IDENTITY);
         }
      }
   }

}