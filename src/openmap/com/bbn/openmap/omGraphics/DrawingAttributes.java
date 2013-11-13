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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/DrawingAttributes.java,v $
// $RCSfile: DrawingAttributes.java,v $
// $Revision: 1.28 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************
package com.bbn.openmap.omGraphics;

/*
 * Java Core
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.awt.ShapeRenderer;
import com.bbn.openmap.omGraphics.geom.NonRegional;
import com.bbn.openmap.tools.icon.IconPartList;
import com.bbn.openmap.tools.icon.OMIconFactory;
import com.bbn.openmap.tools.icon.OpenMapAppPartCollection;
import com.bbn.openmap.util.HashCodeUtil;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.OptionPropertyEditor;

/**
 * DrawingAttributes provides a mechanism for loading and managing different
 * drawing attributes that may be used. Several layers need to be able to have
 * Properties define how objects should be drawn, and the list of these drawing
 * attributes tend to be the same. The DrawingAttributes class fishes out the
 * applicable properties for you, creates the objects needed, and then lets you
 * get those objects when needed.
 * <P>
 * 
 * The list of properties that the DrawingAttributes object can handle are
 * listed below. If a property is not set, the default value will be used.
 * 
 * <pre>
 * 
 * 
 * 
 * 
 *     # The Edge or Line color
 *     lineColor=AARRGGBB (Hex ARGB Color, black is default)
 *     # The Fill color for 2D shapes
 *     fillColor=AARRGGBB (Hex ARGB Color, clean is default)
 *     # A highlight color to switch a graphic to when &quot;selected&quot;.
 *     selectColor=AARRGGBB (Hex ARGB Color, black is default)
 *     # A file or URL that can be used for a fill pattern, in place of the fill color.
 *     fillPattern=file://file (default is N/A)
 *     # The line width of the edge of the graphic
 *     lineWidth=int (1 is default)
 *     # A pattern to use for a dashed line, reflected as a
 *     # space-separated list of numbers, which are interpreted as on dash
 *     # length, off dash length, on dash length, etc.
 *     dashPattern=10 5 3 5 (5 5 is the default if an error occurs reading the numbers, a non-dashed line is the default.)
 *     The phase for the dash pattern,
 *     dashPhase=0.0f (0 is the default)
 *     # The scale to use for certain measurements, so that fill patterns
 *     # can be scaled depending on the map scale compared to the
 *     # baseScale.
 *     baseScale=XXXXXX (where 1:XXXXXX is the scale to use.  N/A for the default).
 *     # Set whether any OMPoints that are given to the DrawingAttributes object are oval or rectangle.
 *     pointOval=false
 *     # Set the pixel radius of any OMPoint given to the DrawingAttributes object.
 * pointRadius=2
 * 
 * 
 * 
 * 
 */
public class DrawingAttributes
      implements ActionListener, Serializable, Cloneable, PropertyConsumer, PropertyChangeListener, ShapeRenderer {

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.omGraphics.DrawingAttributes");
   /**
    *
    */
   private static final long serialVersionUID = -3375553175496133974L;
   /**
    * The name of the property that holds the line paint of the graphics.
    */
   public final static String linePaintProperty = "lineColor";
   // /**
   // * The name of the property that holds the text paint for Text,
   // * in case that should be different for labels, etc.
   // */
   // public final static String textPaintProperty = "textColor";
   /**
    * The name of the property that holds the fill paint of the graphics.
    */
   public final static String fillPaintProperty = "fillColor";
   /**
    * The name of the property that holds the select paint of the graphics,
    * which is the line paint that gets set with the default OMGraphic.select()
    * action.
    */
   public final static String selectPaintProperty = "selectColor";
   /**
    * The name of the property that holds the matting paint of the graphics,
    * which is the wider line paint that gets set when matting is enabled.
    */
   public final static String mattingPaintProperty = "mattingColor";
   /**
    * The property that specifies an URL or file a image file to be used to
    * construct the Paint object for a texture fill pattern. If the fillPattern
    * is null, the fillPaint will be used.
    */
   public static final String fillPatternProperty = "fillPattern";
   /**
    * The name of the property that holds the lineWidth of the graphics.
    */
   public final static String lineWidthProperty = "lineWidth";
   /**
    * The name of the property that holds a dashed pattern for lines. This will
    * be used to build the stroke object for lines. This pattern should be two
    * space-separated numbers, the first representing the pixel length of the
    * line in the dash, the second being the space pixel length of the dash.
    */
   public final static String dashPatternProperty = "dashPattern";
   /**
    * The name of the property that holds a dashed phase for lines. This will be
    * used to build the stroke object for lines.
    */
   public final static String dashPhaseProperty = "dashPhase";
   /**
    * The name of the property that holds the cap for the ends of lines.
    * BasicStroke values apply, CAP_BUTT, CAP_ROUND, CAP_SQUARE.
    */
   public final static String capProperty = "cap";
   /**
    * The name of the property that holds the join for lines. BasicStroke values
    * apply, JOIN_MITER, JOIN_ROUND, JOIN_BEVEL.
    */
   public final static String joinProperty = "join";
   /**
    * The name of the property that controls miterLimits.
    */
   public final static String miterLimitProperty = "miterLimit";
   /**
    * The base scale to use for the image provided for the fill pattern. As the
    * scale of the map changes, the base scale can be used as a reference to
    * change the resolution of the pattern. This scale will also be used for
    * strokes.
    */
   public static final String baseScaleProperty = "baseScale";
   /**
    * Set whether a thin black matting should be drawing around the OMGraphic.
    */
   public static final String mattedProperty = "matted";
   /**
    * Property for whether OMPoints should be oval. "pointOval"
    */
   public static final String PointOvalProperty = "pointOval";
   /**
    * Property for the pixel radius of OMPoints. "pointRadius"
    */
   public static final String PointRadiusProperty = "pointRadius";
   public final static int NONE = -1;
   /**
    * The default line paint. (black)
    */
   public final static String defaultLinePaintString = "0"; // black
   /**
    * The default fill paint. (none)
    */
   public final static String defaultFillPaintString = "-1"; // none
   /**
    * The default fill paint. (black)
    */
   public final static String defaultSelectPaintString = "0"; // black
   /**
    * The default matting paint. (black)
    */
   public final static String defaultMattingPaintString = "0"; // black
   /**
    * The default line width
    */
   public final static float defaultLineWidth = 1f;
   /**
    * The default dash phase, which is zero.
    */
   public final static float defaultDashPhase = 0f;
   /**
    * The default dash length, for opaque and transparent parts.
    */
   public final static float defaultDashLength = 5f;
   /**
    * The paint to outline the shapes.
    */
   protected Paint linePaint = Color.black;
   // /** The paint for text. Default to black. */
   // protected Paint textPaint = linePaint;
   /**
    * The select paint for the shapes.
    */
   protected Paint selectPaint = Color.black;
   /**
    * The paint to fill the shapes.
    */
   protected Paint fillPaint = OMColor.clear;
   /**
    * The paint to use for matting.
    */
   protected Paint mattingPaint = Color.black;
   /**
    * A TexturePaint pattern, if defined. Overrules fillPaint if fillPaint is
    * null or clear.
    */
   protected TexturePaint fillPattern = null;
   /**
    * The line stroke, for dashes, etc.
    */
   protected transient Stroke stroke = new BasicStroke(1);
   /**
    * The base scale for scaling the fill pattern image. If NONE, then the
    * resolution of the raw image will always be used.
    */
   protected float baseScale = NONE;
   /**
    * Whether a thin black matting line should be rendered around the OMGraphic.
    */
   protected boolean matted = false;
   protected String propertyPrefix = null;
   protected String fPattern = null; // for writing out the
   // properties
   /**
    * The isOval setting to set on OMPoints.
    */
   protected boolean pointOval = OMPoint.DEFAULT_ISOVAL;
   /**
    * The pixel radius to set on OMPoints.
    */
   protected int pointRadius = OMPoint.DEFAULT_RADIUS;
   /**
    * A good ol' generic DrawingAttributes object for all to use. Black lines,
    * clear fill paint.
    */
   public final static DrawingAttributes DEFAULT = new DrawingAttributes();
   /**
    * Support object to notify listeners when something has changed.
    */
   protected PropertyChangeSupport propertyChangeSupport = null;
   /**
    * For internationalization.
    */
   protected I18n i18n = Environment.getI18n();
   /**
    * Command for line color string adjustments.
    */
   public final static String LineColorCommand = "LineColor";
   /**
    * Command for fill color string adjustments.
    */
   public final static String FillColorCommand = "FillColor";
   /**
    * Command for select color string adjustments.
    */
   public final static String SelectColorCommand = "SelectColor";
   /**
    * Command for matting color string adjustments.
    */
   public final static String MattingColorCommand = "MattingColor";
   /**
    * Command for adding matting.
    */
   public final static String MattedCommand = "MattedCommand";
   private JButton lineColorButton;
   private JButton fillColorButton;
   private JButton selectColorButton;
   private JButton mattingColorButton;
   private JToggleButton mattedCheckBox;
   protected JMenuItem lineColorItem;
   protected JMenuItem fillColorItem;
   protected JMenuItem selectColorItem;
   protected JMenuItem mattingColorItem;
   protected JCheckBoxMenuItem mattedEnabledItem;
   protected final static int icon_width = 20;
   protected final static int icon_height = 20;
   /**
    * Flag to disable choice of fill paint selection, from an external source.
    */
   protected boolean enableFillPaintChoice = true;
   public static boolean alwaysSetTextToBlack = false;
   protected transient BasicStrokeEditorMenu bse;
   protected int orientation = SwingConstants.HORIZONTAL;

   /**
    * Any additional JMenu items that should be added to the line menu.
    */
   protected JMenu[] lineMenuAdditions = null;

   /**
    * Create a DrawingAttributes with the default settings - clear fill paint
    * and pattern, sold black edge line of width 1.
    */
   public DrawingAttributes() {
      setProperties(null, null);
   }

   /**
    * Create the DrawingAttributes and call setProperties without a prefix for
    * the properties. Call setProperties without a prefix for the properties.
    * 
    * @param props the Properties to look in.
    */
   public DrawingAttributes(Properties props) {
      setProperties(null, props);
   }

   /**
    * Create the DrawingAttributes and call setProperties with a prefix for the
    * properties.
    * 
    * @param prefix the prefix marker to use for a property, like
    *        prefix.propertyName. The period is added in this function.
    * @param props the Properties to look in.
    */
   public DrawingAttributes(String prefix, Properties props) {
      setProperties(prefix, props);
   }

   /**
    * Shallow clone.
    */
   public Object clone() {
      try {
         return super.clone();
      } catch (CloneNotSupportedException e) {
         return null;
      }
   }

   public Stroke cloneBasicStroke() {
      if (stroke instanceof BasicStroke) {
         BasicStroke bs = (BasicStroke) stroke;
         return new BasicStroke(bs.getLineWidth(), bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(),
                                bs.getDashPhase());
      } else {
         return new BasicStroke(1);
      }
   }

   /**
    * Shallow.
    */
   public void setTo(DrawingAttributes clone) {
      clone.linePaint = linePaint;
      // clone.textPaint = textPaint;
      clone.selectPaint = selectPaint;
      clone.fillPaint = fillPaint;
      clone.mattingPaint = mattingPaint;
      clone.fillPattern = fillPattern;
      clone.setStroke(stroke);
      clone.baseScale = baseScale;
      clone.matted = matted;
      clone.pointOval = pointOval;
      clone.pointRadius = pointRadius;
      clone.enableFillPaintChoice = enableFillPaintChoice;
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final DrawingAttributes da = (DrawingAttributes) obj;
      return eqTest(da.linePaint, linePaint) && eqTest(da.selectPaint, selectPaint) && eqTest(da.fillPaint, fillPaint)
            && eqTest(da.mattingPaint, mattingPaint) && eqTest(da.fillPattern, fillPattern) && eqTest(da.stroke, stroke)
            && eqTest(da.baseScale, baseScale) && eqTest(da.matted, matted) && da.pointOval == pointOval
            && da.pointRadius == pointRadius;
   }

   /**
    * Space saver method used by equals(Object).
    * 
    * @param obj1
    * @param obj2
    * @return true if objects are equal.  Object equivalence and equals() test.
    */
   private boolean eqTest(Object obj1, Object obj2) {
      // Should handle object equivalence and nulls
      if (obj1 == obj2) {
         return true;
      }
      return obj1 != null && obj1.equals(obj2);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      int result = HashCodeUtil.SEED;
      // collect the contributions of various fields
      result = HashCodeUtil.hash(result, linePaint);
      result = HashCodeUtil.hash(result, fillPaint);
      result = HashCodeUtil.hash(result, selectPaint);
      result = HashCodeUtil.hash(result, mattingPaint);
      result = HashCodeUtil.hash(result, fillPattern);
      result = HashCodeUtil.hash(result, pointOval);
      result = HashCodeUtil.hash(result, pointRadius);
      result = HashCodeUtil.hash(result, fillPattern);
      result = HashCodeUtil.hash(result, stroke);
      result = HashCodeUtil.hash(result, matted);
      return result;
   }

   /**
    * If you want to get a DEFAULT DrawingAttributes object that you may modify,
    * get your own copy.
    */
   public static DrawingAttributes getDefaultClone() {
      return (DrawingAttributes) DEFAULT.clone();
   }

   /**
    * Simple push of OMGraphic attributes from one to another.
    */
   public void transfer(OMGraphic from, OMGraphic to) {
      setFrom(from);
      setTo(to);
   }

   /**
    * Simple push of OMGraphic attributes from one to another. Don't put in a
    * loop, creates a DrawingAttributes object to do the transfer. If in a loop,
    * create one and reuse it with the none-static method.
    * 
    * @param from
    * @param to
    */
   public static void sTransfer(OMGraphic from, OMGraphic to) {
      DrawingAttributes da = getDefaultClone();
      da.transfer(from, to);
   }

   /**
    * Call setProperties without a prefix for the properties.
    * 
    * @param props the Properties to look in.
    * @deprecated use setProperties(props).
    */
   public void init(Properties props) {
      setProperties(null, props);
   }

   /**
    * Look at the Properties, and fill in the drawing attributes based in it's
    * contents. If a property is not in the properties, it's set to its default
    * setting.
    * 
    * @param prefix the prefix marker to use for a property, like
    *        prefix.propertyName. The period is added in this function.
    * @param props the Properties to look in.
    * @deprecated use setProperties(prefix, props).
    */
   public void init(String prefix, Properties props) {
      setProperties(prefix, props);
   }

   /**
    * Set the Stroke to use for the edge of a graphic.
    */
   public void setStroke(Stroke stroke) {
      Stroke oldStroke = this.stroke;
      this.stroke = stroke;

      // We don't want to call getBasicStrokeEditor, that creates
      // the editor if it doesn't exist, which may be problematic
      // for cases where there is no Graphics Display.
      if (stroke instanceof BasicStroke && bse != null) {
         bse.setBasicStroke((BasicStroke) stroke);
         // This requires that the JRE has a display, which may be
         // unnecessary in some situations where the editor is
         // never used.
         // BasicStrokeEditorMenu tmpbse = getBasicStrokeEditor();
         // if (tmpbse != null) {
         // tmpbse.setBasicStroke((BasicStroke) stroke);
         // }
      }

      if (propertyChangeSupport != null) {
         propertyChangeSupport.firePropertyChange("stroke", oldStroke, stroke);
      }
   }

   /**
    * Get the Stroke used for the lines of a graphic.
    */
   public Stroke getStroke() {
      return stroke;
   }

   /**
    * Get the Stroke object, scaled for comparison to the base scale. If the
    * base scale equals NONE, it's the same as getStroke().
    * 
    * @param scale scale to compare to the base scale.
    */
   public Stroke getStrokeForScale(float scale) {
      if (baseScale != NONE && stroke instanceof BasicStroke) {
         BasicStroke bs = (BasicStroke) stroke;
         float lineWidth = bs.getLineWidth();
         float[] dash = bs.getDashArray();
         float scaleFactor = scale / baseScale;
         int endCaps = bs.getEndCap();
         int lineJoins = bs.getLineJoin();
         float miterLimit = bs.getMiterLimit();

         lineWidth *= scaleFactor;
         for (int i = 0; i < dash.length; i++) {
            dash[i] *= scaleFactor;
         }

         return new BasicStroke(lineWidth, endCaps, lineJoins, miterLimit, dash, bs.getDashPhase());
      }
      return stroke;
   }

   /**
    * Get the Paint for these attributes, and scale it for the scale compared to
    * the base scale set if the fill Paint is a TexturePattern. If the base
    * scale equals NONE, or if the Paint is not a TexturePaint, it's the same as
    * getFillPaint().
    * 
    * @param scale scale to compare to the base scale.
    * @return a Paint object to use for the fill, scaled if necessary.
    */
   public Paint getFillPaintForScale(float scale) {
      if (fillPattern != null) {
         if (baseScale != NONE) {
            BufferedImage bi = fillPattern.getImage();
            float scaleFactor = scale / baseScale;
            Image image =
                  bi.getScaledInstance((int) (bi.getWidth() * scaleFactor), (int) (bi.getHeight() * scaleFactor),
                                       Image.SCALE_SMOOTH);
            try {
               bi = BufferedImageHelper.getBufferedImage(image, 0, 0, -1, -1);

               return new TexturePaint(bi, new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
            } catch (InterruptedException ie) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.warning("DrawingAttributes: Interrupted Exception scaling texture paint");
               }
            }
         }
         return fillPattern;
      } else {
         return fillPaint;
      }
   }

   /**
    * Set the edge paint for the graphics created for the coverage type.
    * 
    * @param lPaint the paint.
    */
   public void setLinePaint(Paint lPaint) {
      if (lPaint == linePaint) {
         return;
      }

      Paint oldPaint = linePaint;
      linePaint = lPaint;

      if (lineColorButton != null) {
         lineColorButton.setIcon(getIconForPaint(linePaint, false));
      }

      if (mattedCheckBox != null) {
         mattedCheckBox.setIcon(getMattedIcon(mattingPaint, linePaint));
      }

      propertyChangeSupport.firePropertyChange("linePaint", oldPaint, linePaint);
   }

   /**
    * Get the line paint for the graphics created for the coverage type.
    * 
    * @return the line paint to use for the edges.
    */
   public Paint getLinePaint() {
      return linePaint;
   }

   /**
    * Set the selected edge paint for the graphics created for the coverage
    * type.
    * 
    * @param sPaint the paint.
    */
   public void setSelectPaint(Paint sPaint) {
      if (sPaint == selectPaint) {
         return;
      }

      Paint oldPaint = selectPaint;
      selectPaint = sPaint;

      if (selectColorButton != null) {
         selectColorButton.setIcon(getIconForPaint(selectPaint, false));
      }

      propertyChangeSupport.firePropertyChange("selectPaint", oldPaint, selectPaint);
   }

   /**
    * Get the line paint for the graphics created for the coverage type.
    * 
    * @return the select line paint to use for the edges.
    */
   public Paint getSelectPaint() {
      return selectPaint;
   }

   /**
    * Set the fill paint for the graphics created for the coverage type.
    * 
    * @param fPaint the paint.
    */
   public void setFillPaint(Paint fPaint) {
      if (fPaint == fillPaint) {
         return;
      }

      Paint oldPaint = fillPaint;
      fillPaint = fPaint;

      if (fillColorButton != null) {
         fillColorButton.setIcon(getIconForPaint(fillPaint, true));
      }

      propertyChangeSupport.firePropertyChange("fillPaint", oldPaint, fillPaint);
   }

   /**
    * Get the fill paint for the graphics created for the coverage type. This
    * used to return the fillPattern if it was defined. Now, it always returns
    * the fillPaint.
    * 
    * @return the fill paint to use for the areas.
    */
   public Paint getFillPaint() {
      return fillPaint;
   }

   /**
    * Set the matting paint for the graphics created for the coverage type. The
    * matting paint is the paint used for the matting line painted around the
    * edge, two pixels wider than the edge line width. Black by default, only
    * painted when the matting variable is set to true.
    * 
    * @param mPaint the paint.
    */
   public void setMattingPaint(Paint mPaint) {
      if (mPaint == mattingPaint) {
         return;
      }

      Paint oldPaint = mattingPaint;
      mattingPaint = mPaint;

      if (mattingColorButton != null) {
         mattingColorButton.setIcon(getMattingIconForPaint());
      }

      if (mattedCheckBox != null) {
         mattedCheckBox.setIcon(getMattedIcon(mattingPaint, linePaint));
      }

      propertyChangeSupport.firePropertyChange("mattingPaint", oldPaint, mattingPaint);
   }

   /**
    * Get the matting paint for the OMGraphics
    * 
    * @return the matting paint to use for the areas.
    */
   public Paint getMattingPaint() {
      return mattingPaint;
   }

   /**
    * Set the fill pattern TexturePaint to be used as the fill color. If not
    * null, the fillPattern will be returned from getFillPaint() instead of
    * fillPaint.
    * 
    * @param fPattern the TexturePaint to set.
    */
   public void setFillPattern(TexturePaint fPattern) {
      Paint oldPattern = fPattern;
      fillPattern = fPattern;

      if (fillColorButton != null) {
         // GUI doesn't handle fill patterns yet.
      }

      propertyChangeSupport.firePropertyChange("fillPattern", oldPattern, fillPattern);
   }

   /**
    * Get the TexturePaint set as the fill pattern.
    * 
    * @return TexturePaint.
    */
   public TexturePaint getFillPattern() {
      return fillPattern;
   }

   /**
    * Set the base scale to use for the texture paint and stroke. If this is set
    * to a negative number, then no scaling of the paint or stroke will be
    * performed.
    * 
    * @param bScale the base scale to use - 1:bScale.
    */
   public void setBaseScale(float bScale) {
      if (bScale > 0) {
         baseScale = bScale;
      } else {
         baseScale = NONE;
      }
   }

   /**
    * Get the base scale that the texture paint and dashes are set for. If the
    * texture paint and stroke are asked for with a scale, those values will be
    * adjusted accordingly.
    * 
    * @return base scale for paint and stroke.
    */
   public float getBaseScale() {
      return baseScale;
   }

   /**
    * Return whether the OMGraphic has matting around the edge.
    */
   public boolean isMatted() {
      return matted;
   }

   /**
    * Set whether the OMGraphic should have matting around the edge.
    */
   public void setMatted(boolean set) {
      boolean oldMatted = matted;
      matted = set;

      if (mattedCheckBox != null) {
         mattedCheckBox.setSelected(matted);
      }

      propertyChangeSupport.firePropertyChange("matted", oldMatted, matted);
   }

   /**
    * Set the pixel radius given to OMPoint objects.
    */
   public void setPointRadius(int radius) {
      pointRadius = radius;
   }

   /**
    * Get the pixel radius given to OMPoint objects.
    */
   public int getPointRadius() {
      return pointRadius;
   }

   /**
    * Set the oval setting given to OMPoint objects.
    */
   public void setPointOval(boolean value) {
      pointOval = value;
   }

   /**
    * Get the oval setting given to OMPoint objects.
    */
   public boolean isPointOval() {
      return pointOval;
   }

   /**
    * Set the DrawingAttributes parameters based on the current settings of an
    * OMGraphic.
    */
   public void setFrom(OMGraphic graphic) {
      setFrom(graphic, false);
   }

   /**
    * Set the DrawingAttributes parameters based on the current settings of an
    * OMGraphic, and reset the GUI of the DrawingAttributes object if desired.
    */
   public void setFrom(OMGraphic graphic, boolean resetGUI) {
      if (graphic == null) {
         return;
      }

      matted = graphic.isMatted();
      mattingPaint = graphic.getMattingPaint();

      linePaint = graphic.getLinePaint();
      selectPaint = graphic.getSelectPaint();
      fillPaint = graphic.getFillPaint();
      fillPattern = graphic.getTextureMask();

      // Need to put this in to keep the gui up to date. Calling
      // setStroke fires off a propertyChange reaction that
      // potentially harms other parameters, like renderType.
      stroke = graphic.getStroke();

      if (graphic instanceof OMPoint) {
         pointRadius = ((OMPoint) graphic).getRadius();
         pointOval = ((OMPoint) graphic).isOval();
      }

      enableFillPaintChoice = !(graphic instanceof NonRegional);

      // Don't want to call this here, it is CPU intensive.
      // resetGUI should be called only when the GUI needs to be
      // updated.
      if (resetGUI) {
         resetGUI();
      }

      if (propertyChangeSupport != null) {
         propertyChangeSupport.firePropertyChange("all", true, true);
      }
   }

   /**
    * Set all the attributes for the graphic that are contained within this
    * DrawingAttributes class.
    * <P>
    * 
    * If the fillPattern is set to a TexturePaint, and the fillPaint is null or
    * clear, then the fillPattern will be set as the fill paint. Otherwise, the
    * fillPaint will be set in the OMGraphic, and the fillPattern will be set
    * too. If the OMGraphic.textureMask is != null, then it will get painted on
    * top of the fillPaint. Makes for effects if the fillPattern has some
    * transparent spots.
    * 
    * @param graphic OMGraphic.
    */
   public void setTo(OMGraphic graphic) {
      setTo(graphic, false);
   }

   /**
    * Set all the attributes for the graphic that are contained within this
    * DrawingAttributes class.
    * <P>
    * 
    * If the fillPattern is set to a TexturePaint, and the fillPaint is null or
    * clear, then the fillPattern will be set as the fill paint. Otherwise, the
    * fillPaint will be set in the OMGraphic, and the fillPattern will be set
    * too. If the OMGraphic.textureMask is != null, then it will get painted on
    * top of the fillPaint. Makes for effects if the fillPattern has some
    * transparent spots.
    * 
    * @param graphic OMGraphic.
    * @param resetGUI reset the GUI if desired, set the enableFillPaintChoice
    *        option if OMGraphic allows it.
    */
   public void setTo(OMGraphic graphic, boolean resetGUI) {
      if (graphic == null) {
         return;
      }

      // More efficient to touch each OMGraphic once if this is a list,
      // instead of cycling through for each parameter.
      if (graphic instanceof OMGraphicList) {
         for (OMGraphic omg : ((OMGraphicList) graphic)) {
            setTo(omg, resetGUI);
         }
         return;
      }

      setOMGraphicEdgeAttributes(graphic);

      // If the fillPattern is set to a TexturePaint, and the
      // fillPaint is null or clear, then the fillPattern will be
      // set as the fill paint. Otherwise, the fillPaint will be
      // set in the OMGraphic, and the fillPattern will be set too.
      // If the OMGraphic.textureMask is != null, then it will get
      // painted on top of the fillPaint. Makes for effects if the
      // fillPattern has some transparent spots.
      if (fillPattern != null && (fillPaint == null || isClear(fillPaint))) {
         graphic.setFillPaint(fillPattern);
      } else {
         graphic.setFillPaint(fillPaint);
         graphic.setTextureMask(fillPattern);
      }

      graphic.setMatted(matted);
      graphic.setMattingPaint(mattingPaint);

      if (graphic instanceof OMPoint) {
         ((OMPoint) graphic).setRadius(pointRadius);
         ((OMPoint) graphic).setOval(pointOval);
      }

      // The GraphicAttribute might be rendering options for this graphic,
      // needs to know if fill paint choices are available.
      if (resetGUI) {
         enableFillPaintChoice = !(graphic instanceof NonRegional);
         resetGUI();
      }
   }

   /**
    * Set the graphic attributes that only pertain to boundaries. This is good
    * for polylines, where setting the fill paint will close up the polyline
    * making it a polygon. So if you want to paint edge data, use this function.
    * Sets line paint, line width, and stroke if graphic is a OMGraphic
    * 
    * @param graphic OMGraphic
    */
   public void setOMGraphicEdgeAttributes(OMGraphic graphic) {
      graphic.setLinePaint(linePaint);
      graphic.setSelectPaint(selectPaint);

      if (stroke != null) {
         graphic.setStroke(stroke);
      } else {
         graphic.setStroke(OMGraphic.BASIC_STROKE);
      }
   }

   /**
    * Set all the attributes for the graphic that are contained within this
    * DrawingAttributes class. Get the TexturePaint for these attributes, and
    * scale it for the scale compared to the base scale set. If the base scale
    * equals NONE, the fill pattern is not changed with relation to scale.
    * 
    * @param graphic OMGraphic.
    * @param scale scale to compare to the base scale.
    */
   public void setOMGraphicAttributesForScale(OMGraphic graphic, float scale) {
      setOMGraphicEdgeAttributesForScale(graphic, scale);
      graphic.setFillPaint(getFillPaintForScale(scale));
   }

   /**
    * Set the graphic attributes that only pertain to boundaries. This is good
    * for polylines, where setting the fill paint will close up the polyline
    * making it a polygon. So if you want to paint edge data, use this function.
    * Sets line paint, line width, and stroke if graphic is a OMGraphic The
    * stroke, if the base scale is set, is adjusted accordingly.
    * 
    * @param graphic OMGraphic.
    * @param scale scale to compare to the base scale.
    */
   public void setOMGraphicEdgeAttributesForScale(OMGraphic graphic, float scale) {

      graphic.setLinePaint(linePaint);
      graphic.setSelectPaint(selectPaint);
      if (stroke != null) {
         graphic.setStroke(getStrokeForScale(scale));
      } else {
         graphic.setStroke(OMGraphic.BASIC_STROKE);
      }
   }

   /**
    * A lock to use to limit the number of JColorChoosers that can pop up for a
    * given DrawingAttributes GUI.
    */
   private boolean colorChooserLock = false;

   /**
    * Get the lock to use a JColorChooser. Returns true if you got the lock,
    * false if you didn't.
    */
   protected synchronized boolean getLock() {
      if (colorChooserLock == false) {
         colorChooserLock = true;
         return colorChooserLock;
      } else {
         return false;
      }
   }

   /**
    * Release the lock on the JColorChooser.
    */
   protected synchronized void releaseLock() {
      colorChooserLock = false;
   }

   /**
    * The DrawingAttributes method for handling ActionEvents. Used to handle the
    * GUI actions, like changing the colors, line widths, etc.
    */
   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      String command = e.getActionCommand();
      String interString;
      Paint tmpPaint;
      if (command == LineColorCommand && linePaint instanceof Color) {
         interString = i18n.get(DrawingAttributes.class, "chooseLineColor", "Choose Line Color");
         tmpPaint = getNewPaint((Component) source, interString, (Color) linePaint);
         if (tmpPaint != null) {
            setLinePaint(tmpPaint);
         }

      } else if (command == FillColorCommand && fillPaint instanceof Color) {
         interString = i18n.get(DrawingAttributes.class, "chooseFillColor", "Choose Fill Color");
         tmpPaint = getNewPaint((Component) source, interString, (Color) fillPaint);
         if (tmpPaint != null) {
            setFillPaint(tmpPaint);
         }

      } else if (command == SelectColorCommand && selectPaint instanceof Color) {
         interString = i18n.get(DrawingAttributes.class, "chooseSelectColor", "Choose Select Color");
         tmpPaint = getNewPaint((Component) source, interString, (Color) selectPaint);
         if (tmpPaint != null) {
            setSelectPaint(tmpPaint);
         }
      } else if (command == MattingColorCommand && mattingPaint instanceof Color) {
         interString = i18n.get(DrawingAttributes.class, "chooseMattingColor", "Choose Matting Color");
         tmpPaint = getNewPaint((Component) source, interString, (Color) mattingPaint);
         if (tmpPaint != null) {
            setMattingPaint(tmpPaint);
         }
      } else if (command == MattedCommand) {
         setMatted(mattedEnabledItem.getState());
      } else {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("unrecognized command > " + command);
         }
      }
   }

   /**
    * A convenience method to get a color from a JColorChooser. Null will be
    * returned if the JColorChooser lock is in place, or if something else is
    * done where the JColorChooser would normally return null.
    * 
    * @param source the source component for the JColorChooser.
    * @param title the String to label the JColorChooser window.
    * @param startingColor the color to give to the JColorChooser to start with.
    *        Returned if the cancel button is pressed.
    * @return Color chosen from the JColorChooser, null if lock for chooser
    *         can't be acquired.
    */
   protected Color getNewPaint(Component source, String title, Color startingColor) {
      Color newPaint = null;
      if (getLock()) {
         newPaint = OMColorChooser.showDialog(source, title, startingColor);
         releaseLock();
      }
      return newPaint;
   }

   /**
    * Get the GUI components that control the DrawingAttributes.
    * 
    * @return JButton returns a JButton that triggers a pop-up menu.
    */
   public Component getGUI() {
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("DrawingAttributes: creating palette.");
      }

      return getALineButton();
   }

   /**
    * Callout method to add stuff to popup menu before the stroke editor is
    * consulted for additions. Adds colors and line menu additions (arrowhead
    * controls for lines, for instance).
    * 
    * @param popup
    */
   protected void setPreStrokeMenuOptions(JComponent popup) {
      popup.add(getColorMenu());

      JMenu[] menus = getLineMenuAdditions();

      if (menus != null) {
         for (int i = 0; i < menus.length; i++) {
            JMenu menu = menus[i];
            if (menu != null) {
               popup.add(menu);
            }
         }
      }
   }

   /**
    * Calls the editor for strokes to get popup menu addition for stroke
    * editing.
    * 
    * @param popup
    */
   protected void setStrokeMenuOptions(JComponent popup) {
      if (stroke instanceof BasicStroke) {
         BasicStrokeEditorMenu tmpbse = getBasicStrokeEditor();
         if (tmpbse != null) {
            tmpbse.setGUI(popup);
         }
      }
   }

   /**
    * Callout method to add stuff to menu after the stroke menus.
    * 
    * @param popup
    */
   protected void setPostStrokeMenuOptions(JComponent popup) {
      // Nothing to add here...
   }

   /**
    * Get a JButton used to bring up the line menu. An ActionListener is added
    * that will bring up line/color popup menu.
    * 
    * @return a new JButton is created, every time.
    */
   public JButton getALineButton() {
      JButton lineButton = new DrawingAttributesGUIButton(this);

      lineButton.setToolTipText(i18n.get(DrawingAttributes.class, "drawingAttributesButton", I18n.TOOLTIP,
                                         "Modify Drawing Parameters"));

      lineButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent ae) {
            JButton button = (JButton) ae.getSource();
            JPopupMenu popup = new JPopupMenu();

            setPreStrokeMenuOptions(popup);
            setStrokeMenuOptions(popup);
            setPostStrokeMenuOptions(popup);

            popup.show(button, button.getWidth(), 0);
         }
      });

      return lineButton;
   }

   public JMenu getColorAndLineMenu() {
      JMenu menu = new JMenu(i18n.get(DrawingAttributes.class, "drawingAttributesButton", "Colors and Lines"));

      setPreStrokeMenuOptions(menu);
      setStrokeMenuOptions(menu);
      setPostStrokeMenuOptions(menu);

      return menu;
   }

   /**
    * Gets the JMenu that has the color control options.
    * 
    * @return JMenu with options to bring up color interfaces.
    */
   public JMenu getColorMenu() {
      JMenu colorMenu = null;
      colorMenu = new JMenu(i18n.get(GraphicAttributes.class, "Color", "Color"));

      if (lineColorItem == null) {
         resetGUI();
      }

      colorMenu.add(lineColorItem);
      colorMenu.add(fillColorItem);
      colorMenu.add(selectColorItem);
      colorMenu.add(mattingColorItem);
      colorMenu.add(new JSeparator());
      colorMenu.add(mattedEnabledItem);

      fillColorItem.setEnabled(enableFillPaintChoice);
      return colorMenu;
   }

   /**
    * A hook to add to the line menu brought up in the GUI for the
    * DrawingAttributes.
    */
   public void setLineMenuAdditions(JMenu[] lma) {
      lineMenuAdditions = lma;
   }

   public JMenu[] getLineMenuAdditions() {
      return lineMenuAdditions;
   }

   /**
    * Updates the color and line stroke control buttons to match the current
    * settings.
    */
   public void resetGUI() {
      String interString;

      if (lineColorItem == null) {
         interString = i18n.get(DrawingAttributes.class, "lineColorItem", "Change Edge Color");
         lineColorItem = new JMenuItem(interString);
         lineColorItem.setActionCommand(LineColorCommand);
         lineColorItem.addActionListener(this);
         interString = i18n.get(DrawingAttributes.class, "lineColorItem", I18n.TOOLTIP, "Change edge color for rendering.");
         lineColorItem.setToolTipText(interString);
      }

      if (fillColorItem == null) {
         interString = i18n.get(DrawingAttributes.class, "fillColorItem", "Change Fill Color");
         fillColorItem = new JMenuItem(interString);
         fillColorItem.setActionCommand(FillColorCommand);
         fillColorItem.addActionListener(this);
         interString = i18n.get(DrawingAttributes.class, "fillColorItem", I18n.TOOLTIP, "Change fill color for rendering.");
         fillColorItem.setToolTipText(interString);
      }

      if (selectColorItem == null) {
         interString = i18n.get(DrawingAttributes.class, "selectColorItem", "Change Highlight Edge Color");
         selectColorItem = new JMenuItem(interString);
         selectColorItem.setActionCommand(SelectColorCommand);
         selectColorItem.addActionListener(this);
         interString =
               i18n.get(DrawingAttributes.class, "selectColorItem", I18n.TOOLTIP,
                        "Change highlight edge color rendered during selection.");
         selectColorItem.setToolTipText(interString);
      }

      if (mattingColorItem == null) {
         interString = i18n.get(DrawingAttributes.class, "mattingColorItem", "Change Matted Edge Color");
         mattingColorItem = new JMenuItem(interString);
         mattingColorItem.setActionCommand(MattingColorCommand);
         mattingColorItem.addActionListener(this);
         interString =
               i18n.get(DrawingAttributes.class, "mattingColorItem", I18n.TOOLTIP,
                        "Change the color of the border around the edge.");
         mattingColorItem.setToolTipText(interString);
      }

      if (mattedCheckBox == null) {
         interString = i18n.get(DrawingAttributes.class, "mattedEnableItem", "Enable Matting on Edge");
         mattedEnabledItem = new JCheckBoxMenuItem(interString, matted);
         mattedEnabledItem.setActionCommand(MattedCommand);
         mattedEnabledItem.addActionListener(this);
         interString = i18n.get(DrawingAttributes.class, "mattedEnableItem", I18n.TOOLTIP, "Enable/Disable matting on edge.");
         mattedEnabledItem.setToolTipText(interString);
      }

      if (stroke instanceof BasicStroke) {
         BasicStrokeEditorMenu tmpbse = getBasicStrokeEditor();
         if (tmpbse != null) {
            tmpbse.setBasicStroke((BasicStroke) stroke);
         }
      }
   }

   /**
    * Create an ImageIcon from a java.awt.Paint.
    * 
    * @param paint java.awt.Paint
    * @param width icon pixel width
    * @param height icon pixel height
    */
   public static ImageIcon getPaletteIcon(Paint paint, int width, int height) {
      BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
      graphics.setPaint(paint);
      graphics.fillRect(0, 0, width, height);

      return new ImageIcon(bufferedImage);
   }

   /**
    * Get the PropertyChangeSupport object to register anything that is
    * interested in finding out when some parameter has changed.
    */
   public PropertyChangeSupport getPropertyChangeSupport() {
      return propertyChangeSupport;
   }

   public void setPropertyChangeSupport(PropertyChangeSupport support) {
      propertyChangeSupport = support;
   }

   public static Color calculateTextColor(Color color) {
      if (alwaysSetTextToBlack) // Mac OS X
      {
         return Color.black;
      }

      int red = color.getRed();
      int green = color.getGreen();
      int blue = color.getBlue();
      int alpha = color.getAlpha();

      if (alpha < 128) {
         return Color.black;
      }

      int newred, newgreen, newblue;

      newred = normalizeOn128(red);
      newgreen = normalizeOn128(green);
      newblue = normalizeOn128(blue);

      return new Color(newred, newgreen, newblue);
   }

   public static int normalizeOn128(int value) {
      if (value >= 255) {
         return 0;
      } else if (value <= 0) {
         return 255;
      } else if (value <= 128) {
         return 192;
      }
      return 64;
   }

   /**
    * Sets the properties for the <code>DrawingAttributes</code>. This
    * particular method assumes that the marker name is not needed, because all
    * of the contents of this Properties object are to be used for this object,
    * and scoping the properties with a prefix is unnecessary.
    * 
    * @param props the <code>Properties</code> object.
    */
   public void setProperties(java.util.Properties props) {
      setProperties(getPropertyPrefix(), props);
   }

   public BasicStrokeEditorMenu getBasicStrokeEditor() {
      if (bse == null && stroke instanceof BasicStroke) {
         try {
            bse = new BasicStrokeEditorMenu((BasicStroke) getStroke());
            bse.getPropertyChangeSupport().addPropertyChangeListener(this);
         } catch (Exception e) {
            // This happens if a java Toolkit is not available.
            bse = null;
         }
      }
      return bse;
   }

   /**
    * Sets the properties for the <code>DrawingAttributes</code>. Part of the
    * PropertyConsumer interface. DrawingAttributess which override this method
    * should do something like:
    * 
    * <code><pre>
    * public void setProperties(String prefix, Properties props) {
    *     super.setProperties(prefix, props);
    *     // do local stuff
    * }
    * </pre></code>
    * 
    * If the addToBeanContext property is not defined, it is set to false here.
    * 
    * @param prefix the token to prefix the property names
    * @param props the <code>Properties</code> object
    */
   public void setProperties(String prefix, Properties props) {

      propertyChangeSupport = new PropertyChangeSupport(this);
      setPropertyPrefix(prefix);

      if (props == null) {
         return;
      }

      String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

      // Set up the drawing attributes.
      linePaint = PropUtils.parseColorFromProperties(props, realPrefix + linePaintProperty, linePaint);

      selectPaint = PropUtils.parseColorFromProperties(props, realPrefix + selectPaintProperty, selectPaint);

      mattingPaint = PropUtils.parseColorFromProperties(props, realPrefix + mattingPaintProperty, mattingPaint);

      // textPaint =
      // PropUtils.parseColorFromProperties(
      // props, realPrefix + textPaintProperty,
      // textPaint);

      fillPaint = PropUtils.parseColorFromProperties(props, realPrefix + fillPaintProperty, fillPaint);

      matted = PropUtils.booleanFromProperties(props, realPrefix + mattedProperty, matted);

      pointRadius = PropUtils.intFromProperties(props, realPrefix + PointRadiusProperty, pointRadius);
      pointOval = PropUtils.booleanFromProperties(props, realPrefix + PointOvalProperty, pointOval);

      float lineWidth;
      boolean basicStrokeDefined = false;

      if (stroke instanceof BasicStroke) {
         basicStrokeDefined = true;
      }

      lineWidth =
            PropUtils.floatFromProperties(props, realPrefix + lineWidthProperty,
                                          (basicStrokeDefined ? ((BasicStroke) stroke).getLineWidth() : defaultLineWidth));

      baseScale = PropUtils.floatFromProperties(props, realPrefix + baseScaleProperty, baseScale);

      // Look for a dash pattern properties to come up with a stroke
      String dPattern = props.getProperty(realPrefix + dashPatternProperty);
      if (basicStrokeDefined && dPattern != null && dPattern.length() > 0) {
         float dashPhase;
         float[] lineDash;
         // OK, it exists, come up with a stroke.
         try {
            StringTokenizer t = new StringTokenizer(dPattern);
            int arraySize = t.countTokens();
            lineDash = new float[arraySize];

            int dashCount = 0;
            while (t.hasMoreTokens()) {
               String segment = t.nextToken();
               lineDash[dashCount++] = Float.parseFloat(segment);
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("read " + segment);
               }
            }

         } catch (NoSuchElementException nsee) {
            logger.fine("DrawingAttributes.init: dash pattern attributes wrong - should be dashPattern=(number pixels on) (number pixels off)");
            lineDash = null;
         } catch (NumberFormatException nfe) {
            logger.fine("DrawingAttributes.init: Number format exception for dashPattern");
            lineDash = null;
         } catch (NullPointerException npe) {
            logger.fine("DrawingAttributes.init: Caught null pointer exception - probably resulting from non-float number format exception for dashPattern");
            lineDash = null;
         }

         if (lineDash == null) {
            if (basicStrokeDefined) {
               lineDash = ((BasicStroke) stroke).getDashArray();
            } else {
               lineDash = new float[2];
               lineDash[0] = defaultDashLength;
               lineDash[1] = defaultDashLength;
            }
         }

         int dashCount = 0;
         for (int x = 0; x < lineDash.length; x++) {
            dashCount += lineDash[x];
         }
         if (dashCount == 0) {
            lineDash = null;
         }

         String dPhase = props.getProperty(realPrefix + dashPhaseProperty);
         if (dPhase != null && dPhase.length() > 0) {
            try {
               dashPhase = Float.valueOf(dPhase).floatValue();
            } catch (NumberFormatException nfe) {
               logger.fine("DrawingAttributes.init: Number format exception for dashPhase");
               dashPhase = defaultDashPhase;
            }
         } else {
            if (basicStrokeDefined) {
               dashPhase = ((BasicStroke) stroke).getDashPhase();
            } else {
               dashPhase = defaultDashPhase;
            }
         }

         String capPropertyString = props.getProperty(realPrefix + capProperty);
         int cap = BasicStroke.CAP_BUTT;
         if (capPropertyString != null) {
            try {
               cap = java.awt.BasicStroke.class.getField(capPropertyString).getInt(null);
            } catch (NoSuchFieldException nsfe) {
            } catch (IllegalAccessException iae) {
            }
         }

         String joinPropertyString = props.getProperty(realPrefix + capProperty);
         int join = BasicStroke.JOIN_MITER;
         if (joinPropertyString != null) {
            try {
               join = java.awt.BasicStroke.class.getField(joinPropertyString).getInt(null);
            } catch (NoSuchFieldException nsfe) {
            } catch (IllegalAccessException iae) {
            }
         }

         float miterLimit = PropUtils.floatFromProperties(props, realPrefix + miterLimitProperty, 10.0f);

         setStroke(new BasicStroke(lineWidth, cap, join, miterLimit, lineDash, dashPhase));

      } else if (basicStrokeDefined) {
         BasicStroke currentStroke = (BasicStroke) getStroke();
         setStroke(new BasicStroke(lineWidth, currentStroke.getEndCap(), currentStroke.getLineJoin(),
                                   currentStroke.getMiterLimit(), currentStroke.getDashArray(), currentStroke.getDashPhase()));
      }

      // OK, Fill pattern next...
      fPattern = props.getProperty(realPrefix + fillPatternProperty);
      if (fPattern != null && fPattern.length() != 0) {

         try {

            URL textureImageURL = PropUtils.getResourceOrFileOrURL(fPattern);

            if (textureImageURL != null) {

               BufferedImage bi = BufferedImageHelper.getBufferedImage(textureImageURL, 0, 0, -1, -1);

               fillPattern = new TexturePaint(bi, new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
            }
         } catch (MalformedURLException murle) {
            logger.fine("DrawingAttributes.init: bad texture URL - \n     " + realPrefix + fillPatternProperty);
            fillPattern = null;
         } catch (InterruptedException ie) {
            logger.fine("DrawingAttributes.init: bad problems getting texture URL - \n" + ie);
            fillPattern = null;
         }
      }
   }

   /**
    * PropertyConsumer method, to fill in a Properties object, reflecting the
    * current values of the layer. If the layer has a propertyPrefix set, the
    * property keys should have that prefix plus a separating '.' prepended to
    * each property key it uses for configuration.
    * 
    * @param props a Properties object to load the PropertyConsumer properties
    *        into. If props equals null, then a new Properties object should be
    *        created.
    * @return Properties object containing PropertyConsumer property values. If
    *         getList was not null, this should equal getList. Otherwise, it
    *         should be the Properties object created by the PropertyConsumer.
    */
   public Properties getProperties(Properties props) {
      if (props == null) {
         props = new Properties();
      }

      String prefix = PropUtils.getScopedPropertyPrefix(this);

      if (linePaint instanceof Color) {
         props.put(prefix + linePaintProperty, PropUtils.getProperty((Color) linePaint));
      }
      // if (textPaint instanceof Color) {
      // props.put(prefix + textPaintProperty,
      // PropUtils.getProperty((Color)textPaint));
      // }
      if (fillPaint instanceof Color) {
         props.put(prefix + fillPaintProperty, PropUtils.getProperty((Color) fillPaint));
      }
      if (selectPaint instanceof Color) {
         props.put(prefix + selectPaintProperty, PropUtils.getProperty((Color) selectPaint));
      }
      if (mattingPaint instanceof Color) {
         props.put(prefix + mattingPaintProperty, PropUtils.getProperty((Color) mattingPaint));
      }

      props.put(prefix + PointRadiusProperty, Integer.toString(pointRadius));
      props.put(prefix + PointOvalProperty, new Boolean(pointOval).toString());

      props.put(prefix + fillPatternProperty, (fPattern == null ? "" : fPattern));

      Stroke bs = getStroke();

      if (bs == null) {
         bs = new BasicStroke();
      }

      if (bs instanceof BasicStroke) {
         props.put(prefix + lineWidthProperty, Float.toString(((BasicStroke) bs).getLineWidth()));

         float[] fa = ((BasicStroke) bs).getDashArray();
         if (fa != null) {
            StringBuffer dp = new StringBuffer();
            for (int i = 0; i < fa.length; i++) {
               dp.append(" ").append(Float.toString(fa[i]));
            }
            props.put(prefix + dashPatternProperty, dp.toString().trim());
            props.put(prefix + dashPhaseProperty, Float.toString(((BasicStroke) bs).getDashPhase()));
         } else {
            props.put(prefix + dashPatternProperty, "");
            props.put(prefix + dashPhaseProperty, "");
         }

         props.put(prefix + capProperty, Integer.toString(((BasicStroke) bs).getEndCap()));
         props.put(prefix + joinProperty, Integer.toString(((BasicStroke) bs).getLineJoin()));
         props.put(prefix + miterLimitProperty, Float.toString(((BasicStroke) bs).getMiterLimit()));

      }

      if (baseScale != NONE) {
         props.put(prefix + baseScaleProperty, Float.toString(baseScale));
      }

      props.put(prefix + mattedProperty, new Boolean(matted).toString());

      return props;
   }

   /**
    * Method to fill in a Properties object with values reflecting the
    * properties able to be set on this PropertyConsumer. The key for each
    * property should be the raw property name (without a prefix) with a value
    * that is a String that describes what the property key represents, along
    * with any other information about the property that would be helpful
    * (range, default value, etc.).
    * 
    * @param list a Properties object to load the PropertyConsumer properties
    *        into. If getList equals null, then a new Properties object should
    *        be created.
    * @return Properties object containing PropertyConsumer property values. If
    *         getList was not null, this should equal getList. Otherwise, it
    *         should be the Properties object created by the PropertyConsumer.
    */
   public Properties getPropertyInfo(Properties list) {
      if (list == null) {
         list = new Properties();
      }
      String interString;

      interString = i18n.get(DrawingAttributes.class, linePaintProperty, I18n.TOOLTIP, "Edge color for graphics.");
      list.put(linePaintProperty, interString);
      interString = i18n.get(DrawingAttributes.class, linePaintProperty, linePaintProperty);
      list.put(linePaintProperty + LabelEditorProperty, interString);
      list.put(linePaintProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      // list.put(textPaintProperty, "Text color for graphics.");
      // list.put(textPaintProperty + ScopedEditorProperty,
      // "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString = i18n.get(DrawingAttributes.class, fillPaintProperty, I18n.TOOLTIP, "Fill color for graphics.");
      list.put(fillPaintProperty, interString);
      interString = i18n.get(DrawingAttributes.class, fillPaintProperty, fillPaintProperty);
      list.put(fillPaintProperty + LabelEditorProperty, interString);
      list.put(fillPaintProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString = i18n.get(DrawingAttributes.class, selectPaintProperty, I18n.TOOLTIP, "Selected edge color for graphics.");
      list.put(selectPaintProperty, interString);
      interString = i18n.get(DrawingAttributes.class, selectPaintProperty, selectPaintProperty);
      list.put(selectPaintProperty + LabelEditorProperty, interString);
      list.put(selectPaintProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString = i18n.get(DrawingAttributes.class, mattingPaintProperty, I18n.TOOLTIP, "Matting edge color for graphics.");
      list.put(mattingPaintProperty, interString);
      interString = i18n.get(DrawingAttributes.class, mattingPaintProperty, mattingPaintProperty);
      list.put(mattingPaintProperty + LabelEditorProperty, interString);
      list.put(mattingPaintProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString =
            i18n.get(DrawingAttributes.class, fillPatternProperty, I18n.TOOLTIP,
                     "Image file to use for fill pattern for graphics (optional).");
      list.put(fillPatternProperty, interString);
      interString = i18n.get(DrawingAttributes.class, fillPatternProperty, fillPatternProperty);
      list.put(fillPatternProperty + LabelEditorProperty, interString);
      list.put(fillPatternProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

      interString = i18n.get(DrawingAttributes.class, lineWidthProperty, I18n.TOOLTIP, "Line width for edges of graphics");
      list.put(lineWidthProperty, interString);
      interString = i18n.get(DrawingAttributes.class, lineWidthProperty, lineWidthProperty);
      list.put(lineWidthProperty + LabelEditorProperty, interString);

      // list.put(dashPatternProperty, "<HTML><BODY>Line dash
      // pattern, represented by<br>space separated numbers<br> (on
      // off on ...)</BODY></HTML>");
      interString =
            i18n.get(DrawingAttributes.class, dashPatternProperty, I18n.TOOLTIP,
                     "Line dash pattern, represented by space separated numbers (on off on ...)");
      list.put(dashPatternProperty, interString);
      interString = i18n.get(DrawingAttributes.class, dashPatternProperty, dashPatternProperty);
      list.put(dashPatternProperty + LabelEditorProperty, interString);

      interString = i18n.get(DrawingAttributes.class, dashPhaseProperty, I18n.TOOLTIP, "Phase for dash pattern (Default is 0)");
      list.put(dashPhaseProperty, interString);
      interString = i18n.get(DrawingAttributes.class, dashPhaseProperty, dashPhaseProperty);
      list.put(dashPhaseProperty + LabelEditorProperty, interString);

      interString =
            i18n.get(DrawingAttributes.class,
                     baseScaleProperty,
                     I18n.TOOLTIP,
                     "<HTML><BODY>Scale which should be used as the base scale for the <br>patterns and line width. If set, size of pattern and <br>widths will be adjusted to the map scale</BODY></HTML>");
      list.put(baseScaleProperty, interString);
      interString = i18n.get(DrawingAttributes.class, baseScaleProperty, baseScaleProperty);
      list.put(baseScaleProperty + LabelEditorProperty, interString);

      interString =
            i18n.get(DrawingAttributes.class, mattedProperty, I18n.TOOLTIP,
                     "Flag to enable a thin black matting to be drawn around graphics.");
      list.put(mattedProperty, interString);
      interString = i18n.get(DrawingAttributes.class, mattedProperty, mattedProperty);
      list.put(mattedProperty + LabelEditorProperty, interString);
      list.put(mattedProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

      interString = i18n.get(DrawingAttributes.class, PointRadiusProperty, I18n.TOOLTIP, "Pixel radius of point objects.");
      list.put(PointRadiusProperty, interString);
      interString = i18n.get(DrawingAttributes.class, PointRadiusProperty, "Point pixel radius");
      list.put(PointRadiusProperty + LabelEditorProperty, interString);

      interString = i18n.get(DrawingAttributes.class, PointOvalProperty, I18n.TOOLTIP, "Set points to be oval or rectangular.");
      list.put(PointOvalProperty, interString);
      interString = i18n.get(DrawingAttributes.class, PointOvalProperty, "Points are oval");
      list.put(PointOvalProperty + LabelEditorProperty, interString);
      list.put(PointOvalProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

      PropUtils.setI18NPropertyInfo(i18n, list, DrawingAttributes.class, capProperty, "Line Cap",
                                    "Type of cap to use on end of lines.",
                                    "com.bbn.openmap.util.propertyEditor.ComboBoxPropertyEditor");

      list.put(capProperty + OptionPropertyEditor.ScopedOptionsProperty, "butt round square");
      list.put(capProperty + ".butt", "CAP_BUTT");
      list.put(capProperty + ".round", "CAP_ROUND");
      list.put(capProperty + ".square", "CAP_SQUARE");

      PropUtils.setI18NPropertyInfo(i18n, list, DrawingAttributes.class, joinProperty, "Line Join",
                                    "Type of joint to use on corner of line joins.",
                                    "com.bbn.openmap.util.propertyEditor.ComboBoxPropertyEditor");

      list.put(joinProperty + OptionPropertyEditor.ScopedOptionsProperty, "miter round bevel");
      list.put(joinProperty + ".miter", "JOIN_MITER");
      list.put(joinProperty + ".round", "JOIN_ROUND");
      list.put(joinProperty + ".bevel", "JOIN_BEVEL");

      PropUtils.setI18NPropertyInfo(i18n, list, DrawingAttributes.class, miterLimitProperty, "Miter Limit",
                                    "Number of pixels to use for line joints.", null);

      // This line messes order up when called by classes using
      // DrawingAttributes.
      // list.put(initPropertiesProperty, getInitPropertiesOrder());

      return list;
   }

   public String getInitPropertiesOrder() {
      return " " + linePaintProperty + " " + selectPaintProperty + " " + fillPaintProperty + " " + /*
                                                                                                    * textPaintProperty
                                                                                                    * +
                                                                                                    * " "
                                                                                                    * +
                                                                                                    */mattingPaintProperty + " "
            + fillPatternProperty + " " + mattedProperty + " " + lineWidthProperty + " " + dashPatternProperty + " "
            + dashPhaseProperty + " " + capProperty + " " + joinProperty + " " + miterLimitProperty + " " + PointRadiusProperty
            + " " + PointOvalProperty;
   }

   /**
    * Set the property key prefix that should be used by the PropertyConsumer.
    * The prefix, along with a '.', should be prepended to the property keys
    * known by the PropertyConsumer.
    * 
    * @param prefix the prefix String.
    */
   public void setPropertyPrefix(String prefix) {
      propertyPrefix = prefix;
   }

   /**
    * Get the property key prefix that is being used to prepend to the property
    * keys for Properties lookups.
    * 
    * @return the prefix String.
    */
   public String getPropertyPrefix() {
      return propertyPrefix;
   }

   public void propertyChange(PropertyChangeEvent pce) {
      if (pce.getSource() instanceof BasicStrokeEditorMenu) {
         setStroke((BasicStroke) pce.getNewValue());
      }
   }

   public String toString() {
      StringBuffer sb = new StringBuffer("DrawingAttributes[");
      sb.append("linePaint(").append(linePaint).append("), ");
      sb.append("selectPaint(").append(selectPaint).append("), ");
      // sb.append("textPaint(").append(textPaint).append("), ");
      sb.append("mattingPaint(").append(mattingPaint).append("), ");
      sb.append("fillPaint(").append(fillPaint).append("), ");
      sb.append("fillPattern(").append(fillPattern).append("), ");
      sb.append("stroke(").append(stroke).append("), ");
      sb.append("baseScale(").append(baseScale).append("), ");
      sb.append("matted(").append(new Boolean(matted).toString()).append(")]");
      return sb.toString();
   }

   /**
    * Render the Shape into the Graphics2D object, using the mattingPaint,
    * fillPaint, fillPattern, linePaint and stroke contained in this
    * DrawingAttributes object.
    */
   public void render(Graphics2D g, Shape shape) {
      render(g, shape, false);
   }

   /**
    * Render the Shape into the Graphics2D object, using the mattingPaint,
    * fillPaint, fillPattern, linePaint and stroke contained in this
    * DrawingAttributes object.
    * 
    * @param g java.awt.Graphics2D object to render into
    * @param shape java.awt.Shape to draw
    * @param replaceColorWithGradient flag to specify replacement of fill and
    *        edge colors with a GradientPaint to give a light to dark look. You
    *        can set the Paints in the DrawingAttributes object with
    *        GradientPaints if you want more control over the GradientPaint, but
    *        this will let the DrawingAttributes object take a shot at creating
    *        one for a Color that fits the shape given.
    */
   public void render(Graphics2D g, Shape shape, boolean replaceColorWithGradient) {

      if (matted) {
         if (stroke instanceof BasicStroke) {
            g.setStroke(new BasicStroke(((BasicStroke) stroke).getLineWidth() + 2f));
            g.setPaint(mattingPaint);
            g.draw(shape);
         }
      }

      if (!isClear(fillPaint)) {
         g.setStroke(OMGraphicConstants.BASIC_STROKE);

         if (replaceColorWithGradient) {
            g.setPaint(getGradientPaintForShape(shape, fillPaint));
         } else {
            g.setPaint(fillPaint);
         }

         g.fill(shape);
         // Seems to help with a rendering problem, not sure why.
         // Without this the DrawingAttributes fill icon would not
         // be drawn until it was set again. This way, it always
         // appears. Might be a Mac thing.
         g.draw(shape);

         if (fillPattern != null && fillPattern != fillPaint) {
            g.setPaint(fillPattern);
            g.fill(shape);
         }
      }

      if (linePaint != fillPaint) {
         g.setStroke(getStroke());
         if (replaceColorWithGradient) {
            g.setPaint(getGradientPaintForShape(shape, linePaint));
         } else {
            g.setPaint(linePaint);
         }
         g.draw(shape);
      }
   }

   /**
    * Create a GradientPaint object for the given shape.
    * 
    * @param shape shape to take measurements from to set GradientPaint settings
    *        - .3 h/w lighter to .7 h/w darker.
    * @param paint the base color to use for gradient.
    * @return GradientPaint for shape.
    */
   public static Paint getGradientPaintForShape(Shape shape, Paint paint) {
      if (paint instanceof Color) {
         Color color = (Color) paint;
         Rectangle rect = shape.getBounds();
         paint =
               new GradientPaint((float) rect.getWidth() * .3f, (float) rect.getHeight() * .3f, color.brighter().brighter(),
                                 (float) rect.getWidth() * .7f, (float) rect.getHeight() * .7f, color.darker().darker());
      }
      return paint;
   }

   /**
    * @return a matting paint choice icon for the current settings of this
    *         DrawingAttributes object, with the matting paint used.
    */
   public ImageIcon getMattingIconForPaint() {

      Paint paint = getMattingPaint();

      DrawingAttributes da = new DrawingAttributes();
      da.setLinePaint(paint);
      da.setStroke(new BasicStroke(3));

      DrawingAttributes innerda = new DrawingAttributes();
      innerda.setLinePaint(Color.white);
      innerda.setStroke(new BasicStroke(1));

      OpenMapAppPartCollection collection = OpenMapAppPartCollection.getInstance();
      IconPartList parts = new IconPartList();

      if (paint instanceof Color) {
         Color color = (Color) paint;
         Paint opaqueColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
         DrawingAttributes opaqueDA = new DrawingAttributes();
         opaqueDA.setLinePaint(opaqueColor);
         opaqueDA.setStroke(new BasicStroke(3));

         parts.add(collection.get("LR_TRI", opaqueDA));
         parts.add(collection.get("UL_TRI", da));
         parts.add(collection.get("LR_TRI", innerda));
         parts.add(collection.get("UL_TRI", innerda));
      } else {
         parts.add(collection.get("BIG_BOX", da));
         parts.add(collection.get("BIG_BOX", innerda));
      }

      return OMIconFactory.getIcon(icon_width, icon_height, parts);
   }

   /**
    * @param paint the paint to use for the icon.
    * @param fill if fill color should be used.
    * @return an ImageIcon for the provided paint object, two triangles in upper
    *         left and lower right. Upper left version has transparency set.
    */
   public static ImageIcon getIconForPaint(Paint paint, boolean fill) {

      if (paint == null) {
         paint = Color.black;
      }

      DrawingAttributes da = new DrawingAttributes();
      da.setLinePaint(paint);
      da.setStroke(new BasicStroke(2));
      if (fill) {
         da.setFillPaint(paint);
      }

      OpenMapAppPartCollection collection = OpenMapAppPartCollection.getInstance();
      IconPartList parts = new IconPartList();

      if (paint instanceof Color || paint == OMColor.clear) {
         Color color = (Color) paint;
         Color opaqueColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
         DrawingAttributes opaqueDA = new DrawingAttributes();
         opaqueDA.setLinePaint(opaqueColor);
         opaqueDA.setStroke(new BasicStroke(2));

         if (fill) {
            opaqueDA.setFillPaint(opaqueColor);
         }

         parts.add(collection.get("LR_TRI", opaqueDA));
         parts.add(collection.get("UL_TRI", da));
      } else {
         parts.add(collection.get("BIG_BOX", da));
      }

      return OMIconFactory.getIcon(icon_width, icon_height, parts);
   }

   /**
    * @param mattingPaint
    * @param linePaint
    * @return an ImageIcon that shows a square with the matting paint and line
    *         paint.
    */
   public static ImageIcon getMattedIcon(Paint mattingPaint, Paint linePaint) {
      DrawingAttributes da = new DrawingAttributes();
      da.setMattingPaint(mattingPaint);
      da.setStroke(new BasicStroke(2));

      DrawingAttributes fillda = new DrawingAttributes();
      fillda.setLinePaint(linePaint);
      fillda.setFillPaint(linePaint);
      da.setStroke(new BasicStroke(2));

      OpenMapAppPartCollection collection = OpenMapAppPartCollection.getInstance();

      IconPartList parts = new IconPartList();
      parts.add(collection.get("FILL_BOX", fillda));
      parts.add(collection.get("BIG_BOX", da));
      parts.add(collection.get("SMALL_BOX", da));

      return OMIconFactory.getIcon(icon_width, icon_height, parts);
   }

   /**
    * Given a BasicStroke, create an ImageIcon that shows it.
    * 
    * @param attributes attributes to use for drawing the icon for the stroke.
    * @param width the width of the icon.
    * @param height the height of the icon.
    * @param horizontalOrientation if true, draw line on the icon horizontally,
    *        else draw it vertically.
    */
   public static ImageIcon getDrawingAttributesIcon(DrawingAttributes attributes, int width, int height,
                                                    boolean horizontalOrientation) {

      BufferedImage bigImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = (Graphics2D) bigImage.getGraphics();

      g.setBackground(OMColor.clear);

      if (attributes.enableFillPaintChoice) {
         g.setPaint(attributes.fillPaint);
         g.fillRect(0, 0, width, height);
      }

      if (attributes.matted) {
         BasicStroke mattedStroke = new BasicStroke(((BasicStroke) attributes.stroke).getLineWidth() + 2f);
         g.setStroke(mattedStroke);
         g.setPaint(attributes.mattingPaint);
         g.drawLine(0, height / 2, width, height / 2);
      }

      g.setPaint(attributes.linePaint);
      g.setStroke(attributes.stroke);
      if (horizontalOrientation) {
         g.drawLine(0 + 3, height / 2, width - 3, height / 2);
      } else {
         g.drawLine(width / 2, 0 + 3, width / 2, height - 3);
      }

      return new ImageIcon(bigImage);
   }

   public boolean isEnableFillPaintChoice() {
      return enableFillPaintChoice;
   }

   public void setEnableFillPaintChoice(boolean enableFillPaintChoice) {
      this.enableFillPaintChoice = enableFillPaintChoice;
   }

   public int getOrientation() {
      return orientation;
   }

   public void setOrientation(int orientation) {
      this.orientation = orientation;
   }

   /**
    * Checks if the Paint is clear.
    * 
    * @param paint Paint or null.
    * @return true if Paint is null or is a Color with a 0 alpha value.
    */
   public static boolean isClear(Paint paint) {
      if (paint instanceof Color) {
         return ((((Color) paint).getRGB() & 0xff000000) == 0);
      } else {
         return false;
      }
   }

   /**
    * A JButton that listens for PropertyChange notifications from a
    * DrawingAttributes object, so it can update its icon.
    * 
    * @author dietrick
    */
   protected static class DrawingAttributesGUIButton
         extends JButton
         implements PropertyChangeListener {

      DrawingAttributes parentDA;

      protected DrawingAttributesGUIButton(DrawingAttributes da) {
         super(getDrawingAttributesIcon(da, icon_width, icon_height, true));
         da.propertyChangeSupport.addPropertyChangeListener(DrawingAttributesGUIButton.this);
         parentDA = da;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
       * PropertyChangeEvent)
       */
      public void propertyChange(PropertyChangeEvent evt) {
         DrawingAttributesGUIButton.this.setIcon(getDrawingAttributesIcon(parentDA, icon_width, icon_height, true));
      }
   }

   /**
    * Builders are usually for immutable objects, but it seems kinda handy to
    * have one for this class, to eliminate lines of code.
    * 
    * @author ddietrick
    */
   public static class Builder {

      DrawingAttributes da = null;

      public Builder() {
         da = DrawingAttributes.getDefaultClone();
      }

      public Builder setLinePaint(Paint lPaint) {
         da.setLinePaint(lPaint);
         return this;
      }

      public Builder setFillPaint(Paint fPaint) {
         da.setFillPaint(fPaint);
         return this;
      }

      public Builder setSelectPaint(Paint sPaint) {
         da.setSelectPaint(sPaint);
         return this;
      }

      public Builder setStroke(Stroke s) {
         da.setStroke(s);
         return this;
      }

      public Builder setLineWidth(int lineWidth) {
         da.setStroke(new BasicStroke(lineWidth));
         return this;
      }

      public Builder setFillPattern(TexturePaint tPaint) {
         da.setFillPattern(tPaint);
         return this;
      }

      public Builder setMattingPaint(Paint mPaint) {
         da.setMattingPaint(mPaint);
         return this;
      }

      public Builder setMatted(boolean matted) {
         da.setMatted(matted);
         return this;
      }

      public Builder setPointOval(boolean oval) {
         da.setPointOval(oval);
         return this;
      }

      public Builder setPointRadius(int radius) {
         da.setPointRadius(radius);
         return this;
      }

      public Builder setOrientation(int orientation) {
         da.setOrientation(orientation);
         return this;
      }

      public Builder setFrom(DrawingAttributes anotherDa) {
         anotherDa.setTo(da);
         return this;
      }

      public Builder setFrom(OMGraphic omg) {
         da.setFrom(omg);
         return this;
      }

      public DrawingAttributes build() {
         return da;
      }
   }
}