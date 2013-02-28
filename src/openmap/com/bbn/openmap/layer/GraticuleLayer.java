// **********************************************************************
//
// <copyright>
//
// BBN Technologies, a Verizon Company
// 10 Moulton Street
// Cambridge, MA 02138
// (617) 873-8000
//
// Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/GraticuleLayer.java,v $
// $RCSfile: GraticuleLayer.java,v $
// $Revision: 1.17 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
//
// **********************************************************************

// Modified 28 September 2002 by David N. Allsopp to allow font size
// to be changed. See sections commented with 'DNA'.

// Modified 20 February 2010 by Steve C. Tang to allow graticule lines equally
// bisect from projection center to avoid swapping.  Also fixed possible dead-loop when 
// calculating label locations. 
// 

package com.bbn.openmap.layer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Cylindrical;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * Layer that draws graticule lines. If the showRuler property is set to true,
 * then longitude values are displayed on the bottom of the map, and latitude
 * values are displayed on the left side. If the show1And5Lines property is
 * true, then 5 degree lines are drawn when there are &lt;= threshold ten degree
 * latitude or longitude lines, and 1 degree lines are drawn when there are
 * &lt;= threshold five degree latitude or longitude degree lines.
 * 
 * <P>
 * The openmap.properties file can control the layer with the following
 * settings: <code><pre>
 *  
 *   
 *    # Show lat / lon spacing labels
 *    graticule.showRuler=true
 *    graticule.show1And5Lines=true
 *    # Controls when the five degree lines and one degree lines kick in
 *    #- when there is less than the threshold of ten degree lat or lon
 *    #lines, five degree lines are drawn. The same relationship is there
 *    #for one to five degree lines.
 *    graticule.threshold=2
 *    # the color of 10 degree spacing lines (Hex ARGB)
 *    graticule.10DegreeColor=FF000000
 *    # the color of 5 degree spacing lines (Hex ARGB)
 *    graticule.5DegreeColor=C7009900
 *    # the color of 1 degree spacing lines (Hex ARGB)
 *    graticule.1DegreeColor=C7003300
 *    # the color of the equator (Hex ARGB)
 *    graticule.equatorColor=FFFF0000
 *    # the color of the international dateline (Hex ARGB)
 *    graticule.datelineColor=7F000099
 *    # the color of the special lines (Hex ARGB)
 *    graticule.specialLineColor=FF000000
 *    # the color of the labels (Hex ARGB)
 *    graticule.textColor=FF000000
 *    
 *   
 * </pre></code> In addition, you can get this layer to work with the OpenMap
 * viewer by editing your openmap.properties file: <code><pre>
 *  
 *   
 *    # layers
 *    openmap.layers=graticule ...
 *    # class
 *    graticule.class=com.bbn.openmap.layer.GraticuleLayer
 *    # name
 *    graticule.prettyName=Graticule
 *    
 *   
 * </pre></code>
 * 
 */
public class GraticuleLayer
      extends OMGraphicHandlerLayer
      implements ActionListener {

   // default to not showing the ruler (mimicking older
   // GraticuleLayer)
   protected boolean defaultShowRuler = true;
   protected boolean defaultShowOneAndFiveLines = true;
   protected boolean defaultShowBelowOneLines = false;
   protected int defaultThreshold = 2;

   /**
    * Flag for lineType - true is LINETYPE_STRAIGHT, false is
    * LINETYPE_GREATCIRCLE.
    */
   protected boolean boxy = true;
   /**
    * Threshold is the total number of ten lines on the screen before the five
    * lines appear, and the total number of five lines on the screen before the
    * one lines appear.
    */
   protected int threshold = defaultThreshold;
   /** The ten degree latitude and longitude lines, premade. */
   protected OMGraphicList tenDegreeLines = null;
   /** The equator, dateline and meridian lines, premade. */
   protected OMGraphicList markerLines = null;

   private final static int SHOW_TENS = 0;
   private final static int SHOW_FIVES = 1;
   private final static int SHOW_ONES = 2;

   protected boolean showOneAndFiveLines = defaultShowOneAndFiveLines;
   protected boolean showBelowOneLines = defaultShowBelowOneLines;
   protected boolean showRuler = defaultShowRuler;

   // protected Font font = new Font("Helvetica",
   // java.awt.Font.PLAIN, 10);
   protected Font font = null;
   protected int fontSize = 10;

   // Color variables for different line types
   protected Color tenDegreeColor = null;
   protected Color fiveDegreeColor = null;
   protected Color oneDegreeColor = null;
   protected Color belowOneDegreeColor = null;
   protected Color equatorColor = null;
   protected Color dateLineColor = null;
   protected Color specialLineColor = null; // Tropic of Cancer,
   // Capricorn
   protected Color textColor = null;

   // Default colors to use, if not specified in the properties.
   protected String defaultTenDegreeColorString = "000000";
   protected String defaultFiveDegreeColorString = "33009900";
   protected String defaultOneDegreeColorString = "33003300";
   protected String defaultBelowOneDegreeColorString = "9900ff00";
   protected String defaultEquatorColorString = "990000";
   protected String defaultDateLineColorString = "000099";
   protected String defaultSpecialLineColorString = "000000";
   protected String defaultTextColorString = "000000";

   // property text values
   public static final String TenDegreeColorProperty = "10DegreeColor";
   public static final String FiveDegreeColorProperty = "5DegreeColor";
   public static final String OneDegreeColorProperty = "1DegreeColor";
   public static final String BelowOneDegreeColorProperty = "Below1DegreeColor";
   public static final String EquatorColorProperty = "equatorColor";
   public static final String DateLineColorProperty = "datelineColor";
   public static final String SpecialLineColorProperty = "specialLineColor";
   public static final String TextColorProperty = "textColor";
   public static final String ThresholdProperty = "threshold";
   public static final String ShowRulerProperty = "showRuler";
   public static final String ShowOneAndFiveProperty = "show1And5Lines";
   public static final String ShowBelowOneProperty = "showBelow1Lines";
   public static final String FontSizeProperty = "fontSize"; // DNA

   /**
    * Construct the GraticuleLayer.
    */
   public GraticuleLayer() {
      // precalculate for boxy
      boxy = true;
      setName("Graticule");
      setRenderPolicy(new BufferedImageRenderPolicy(this));
   }

   /**
    * The properties and prefix are managed and decoded here, for the standard
    * uses of the GraticuleLayer.
    * 
    * @param prefix string prefix used in the properties file for this layer.
    * @param properties the properties set in the properties file.
    */
   public void setProperties(String prefix, java.util.Properties properties) {
      super.setProperties(prefix, properties);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      tenDegreeColor =
            PropUtils.parseColorFromProperties(properties, prefix + TenDegreeColorProperty, defaultTenDegreeColorString, true);

      fiveDegreeColor =
            PropUtils.parseColorFromProperties(properties, prefix + FiveDegreeColorProperty, defaultFiveDegreeColorString, true);

      oneDegreeColor =
            PropUtils.parseColorFromProperties(properties, prefix + OneDegreeColorProperty, defaultOneDegreeColorString, true);

      belowOneDegreeColor =
            PropUtils.parseColorFromProperties(properties, prefix + BelowOneDegreeColorProperty, defaultBelowOneDegreeColorString,
                                               true);

      equatorColor = PropUtils.parseColorFromProperties(properties, prefix + EquatorColorProperty, defaultEquatorColorString, true);

      dateLineColor =
            PropUtils.parseColorFromProperties(properties, prefix + DateLineColorProperty, defaultDateLineColorString, true);

      specialLineColor =
            PropUtils.parseColorFromProperties(properties, prefix + SpecialLineColorProperty, defaultSpecialLineColorString, true);

      textColor = PropUtils.parseColorFromProperties(properties, prefix + TextColorProperty, defaultTextColorString, true);

      threshold = PropUtils.intFromProperties(properties, prefix + ThresholdProperty, defaultThreshold);

      fontSize = PropUtils.intFromProperties(properties, prefix + FontSizeProperty, fontSize);

      font = new Font("Helvetica", java.awt.Font.PLAIN, fontSize);

      setShowOneAndFiveLines(PropUtils.booleanFromProperties(properties, prefix + ShowOneAndFiveProperty,
                                                             defaultShowOneAndFiveLines));

      setShowBelowOneLines(PropUtils.booleanFromProperties(properties, prefix + ShowBelowOneProperty, defaultShowBelowOneLines));

      setShowRuler(PropUtils.booleanFromProperties(properties, prefix + ShowRulerProperty, defaultShowRuler));

      // So they will get re-created.
      tenDegreeLines = null;
      markerLines = null;
   }

   protected JCheckBox showRulerButton = null;
   protected JCheckBox show15Button = null;
   protected JCheckBox showBelow1Button = null;

   public void setShowOneAndFiveLines(boolean set) {
      showOneAndFiveLines = set;
      if (show15Button != null) {
         show15Button.setSelected(set);
      }
   }

   public void setShowBelowOneLines(boolean set) {
      showBelowOneLines = set;
      if (showBelow1Button != null) {
         showBelow1Button.setSelected(set);
      }
   }

   public boolean getShowOneAndFiveLines() {
      return showOneAndFiveLines;
   }

   public boolean getShowBelowOneLines() {
      return showBelowOneLines;
   }

   public void setShowRuler(boolean set) {
      showRuler = set;
      if (showRulerButton != null) {
         showRulerButton.setSelected(set);
      }
   }

   public boolean getShowRuler() {
      return showRuler;
   }

   /**
    * The properties and prefix are managed and decoded here, for the standard
    * uses of the GraticuleLayer.
    * 
    * @param properties the properties set in the properties file.
    */
   public Properties getProperties(Properties properties) {
      properties = super.getProperties(properties);

      String prefix = PropUtils.getScopedPropertyPrefix(this);
      String colorString;

      if (tenDegreeColor == null) {
         colorString = defaultTenDegreeColorString;
      } else {
         colorString = Integer.toHexString(tenDegreeColor.getRGB());
      }
      properties.put(prefix + TenDegreeColorProperty, colorString);

      if (fiveDegreeColor == null) {
         colorString = defaultFiveDegreeColorString;
      } else {
         colorString = Integer.toHexString(fiveDegreeColor.getRGB());
      }
      properties.put(prefix + FiveDegreeColorProperty, colorString);

      if (oneDegreeColor == null) {
         colorString = defaultOneDegreeColorString;
      } else {
         colorString = Integer.toHexString(oneDegreeColor.getRGB());
      }
      properties.put(prefix + OneDegreeColorProperty, colorString);

      if (belowOneDegreeColor == null) {
         colorString = defaultBelowOneDegreeColorString;
      } else {
         colorString = Integer.toHexString(belowOneDegreeColor.getRGB());
      }
      properties.put(prefix + BelowOneDegreeColorProperty, colorString);

      if (equatorColor == null) {
         colorString = defaultEquatorColorString;
      } else {
         colorString = Integer.toHexString(equatorColor.getRGB());
      }
      properties.put(prefix + EquatorColorProperty, colorString);

      if (dateLineColor == null) {
         colorString = defaultDateLineColorString;
      } else {
         colorString = Integer.toHexString(dateLineColor.getRGB());
      }
      properties.put(prefix + DateLineColorProperty, colorString);

      if (specialLineColor == null) {
         colorString = defaultSpecialLineColorString;
      } else {
         colorString = Integer.toHexString(specialLineColor.getRGB());
      }
      properties.put(prefix + SpecialLineColorProperty, colorString);

      if (textColor == null) {
         colorString = defaultTextColorString;
      } else {
         colorString = Integer.toHexString(textColor.getRGB());
      }
      properties.put(prefix + TextColorProperty, colorString);

      properties.put(prefix + ThresholdProperty, Integer.toString(threshold));
      properties.put(prefix + FontSizeProperty, Integer.toString(fontSize)); // DNA

      properties.put(prefix + ShowOneAndFiveProperty, new Boolean(showOneAndFiveLines).toString());

      properties.put(prefix + ShowBelowOneProperty, new Boolean(showBelowOneLines).toString());

      properties.put(prefix + ShowRulerProperty, new Boolean(showRuler).toString());

      return properties;
   }

   /**
    * The properties and prefix are managed and decoded here, for the standard
    * uses of the GraticuleLayer.
    * 
    * @param properties the properties set in the properties file.
    */
   public Properties getPropertyInfo(Properties properties) {
      properties = super.getPropertyInfo(properties);
      String interString;
      properties.put(initPropertiesProperty, TenDegreeColorProperty + " " + FiveDegreeColorProperty + " " + OneDegreeColorProperty
            + " " + /* BelowOneDegreeColorProperty + " " + */EquatorColorProperty + " " + DateLineColorProperty + " "
            + SpecialLineColorProperty + " " + ShowOneAndFiveProperty /*
                                                                       * + " " +
                                                                       * ShowBelowOneProperty
                                                                       */
            + " " + ShowRulerProperty + " " + ThresholdProperty + " " + FontSizeProperty);

      interString =
            i18n.get(GraticuleLayer.class, TenDegreeColorProperty, I18n.TOOLTIP, "Color of the ten degree graticule lines.");
      properties.put(TenDegreeColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, TenDegreeColorProperty, "Ten Degree Color");
      properties.put(TenDegreeColorProperty + LabelEditorProperty, interString);
      properties.put(TenDegreeColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString =
            i18n.get(GraticuleLayer.class, FiveDegreeColorProperty, I18n.TOOLTIP, "Color of the five degree graticule lines.");
      properties.put(FiveDegreeColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, FiveDegreeColorProperty, "Color of the five degree graticule lines.");
      interString = i18n.get(GraticuleLayer.class, FiveDegreeColorProperty, "File Degree Color");
      properties.put(FiveDegreeColorProperty + LabelEditorProperty, interString);
      properties.put(FiveDegreeColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString =
            i18n.get(GraticuleLayer.class, OneDegreeColorProperty, I18n.TOOLTIP, "Color of the one degree graticule lines.");
      properties.put(OneDegreeColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, OneDegreeColorProperty, "1 Degree Color");
      properties.put(OneDegreeColorProperty + LabelEditorProperty, interString);
      properties.put(OneDegreeColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString =
            i18n.get(GraticuleLayer.class, BelowOneDegreeColorProperty, I18n.TOOLTIP,
                     "Color of the sub-one degree graticule lines.");
      properties.put(BelowOneDegreeColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, BelowOneDegreeColorProperty, "Sub-One Degree Color");
      properties.put(BelowOneDegreeColorProperty + LabelEditorProperty, interString);
      properties.put(BelowOneDegreeColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString = i18n.get(GraticuleLayer.class, EquatorColorProperty, I18n.TOOLTIP, "Color of the Equator.");
      properties.put(EquatorColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, EquatorColorProperty, "Equator Line Color");
      properties.put(EquatorColorProperty + LabelEditorProperty, interString);
      properties.put(EquatorColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString = i18n.get(GraticuleLayer.class, DateLineColorProperty, I18n.TOOLTIP, "Color of the Date line.");
      properties.put(DateLineColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, DateLineColorProperty, "Date Line Color");
      properties.put(DateLineColorProperty + LabelEditorProperty, interString);
      properties.put(DateLineColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString =
            i18n.get(GraticuleLayer.class, SpecialLineColorProperty, I18n.TOOLTIP, "Color of Tropic of Cancer, Capricorn lines.");
      properties.put(SpecialLineColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, SpecialLineColorProperty, "Special Line Color");
      properties.put(SpecialLineColorProperty + LabelEditorProperty, interString);
      properties.put(SpecialLineColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString = i18n.get(GraticuleLayer.class, TextColorProperty, I18n.TOOLTIP, "Color of the line label text.");
      properties.put(TextColorProperty, interString);
      interString = i18n.get(GraticuleLayer.class, TextColorProperty, "Text Color");
      properties.put(TextColorProperty + LabelEditorProperty, interString);
      properties.put(TextColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

      interString =
            i18n.get(GraticuleLayer.class, ThresholdProperty, I18n.TOOLTIP,
                     "The number of lines showing before finer grain lines appear.");
      properties.put(ThresholdProperty, interString);
      interString = i18n.get(GraticuleLayer.class, ThresholdProperty, "Line Threshold");
      properties.put(ThresholdProperty + LabelEditorProperty, interString);

      interString = i18n.get(GraticuleLayer.class, ShowOneAndFiveProperty, I18n.TOOLTIP, "Show the one and five degree lines.");
      properties.put(ShowOneAndFiveProperty, interString);
      interString = i18n.get(GraticuleLayer.class, ShowOneAndFiveProperty, "Show 1 and 5 Lines");
      properties.put(ShowOneAndFiveProperty + LabelEditorProperty, interString);
      properties.put(ShowOneAndFiveProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.TrueFalsePropertyEditor");

      interString = i18n.get(GraticuleLayer.class, ShowBelowOneProperty, I18n.TOOLTIP, "Show the one and five degree lines.");
      properties.put(ShowBelowOneProperty, interString);
      interString = i18n.get(GraticuleLayer.class, ShowBelowOneProperty, "Show Sub-1 Lines");
      properties.put(ShowBelowOneProperty + LabelEditorProperty, interString);
      properties.put(ShowBelowOneProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.TrueFalsePropertyEditor");

      interString = i18n.get(GraticuleLayer.class, ShowRulerProperty, I18n.TOOLTIP, "Show the line label text.");
      properties.put(ShowRulerProperty, interString);
      interString = i18n.get(GraticuleLayer.class, ShowRulerProperty, "Show Labels");
      properties.put(ShowRulerProperty + LabelEditorProperty, interString);
      properties.put(ShowRulerProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.TrueFalsePropertyEditor");

      // DNA
      interString =
            i18n.get(GraticuleLayer.class, FontSizeProperty, I18n.TOOLTIP, "The size of the font, in points, of the line labels.");
      properties.put(FontSizeProperty, interString);
      interString = i18n.get(GraticuleLayer.class, FontSizeProperty, "Label Font Size");
      properties.put(FontSizeProperty + LabelEditorProperty, interString);
      // DNA
      return properties;
   }

   /**
    * Implementing the ProjectionPainter interface.
    */
   public synchronized void renderDataForProjection(Projection proj, java.awt.Graphics g) {
      if (proj == null) {
         Debug.error("GraticuleLayer.renderDataForProjection: null projection!");
         return;
      } else if (!proj.equals(getProjection())) {
         setProjection(proj.makeClone());
         // Figure out which line type to use
         if (proj instanceof Cylindrical)
            boxy = true;
         else
            boxy = false;

         setList(constructGraticuleLines());
      }
      paint(g);
   }

   /**
    * Invoked when the projection has changed or this Layer has been added to
    * the MapBean.
    * <p>
    * Perform some extra checks to see if reprojection of the graphics is really
    * necessary.
    * 
    * @param e ProjectionEvent
    * 
    */
   public void projectionChanged(ProjectionEvent e) {

      // extract the projection and check to see if it's really
      // different.
      // if it isn't then we don't need to do all the work again,
      // just
      // repaint.
      Projection proj = setProjection(e);
      if (proj == null) {
         repaint();
         return;
      }

      // Figure out which line type to use
      if (proj instanceof Cylindrical)
         boxy = true;
      else
         boxy = false;

      setList(null);
      doPrepare();
   }

   /**
    * Creates the OMGraphic list with graticule lines.
    */
   public synchronized OMGraphicList prepare() {
      return constructGraticuleLines();
   }

   /**
    * Create the graticule lines.
    * <p>
    * NOTES:
    * <ul>
    * <li>Currently graticule lines are hardcoded to 10 degree intervals.
    * <li>No thought has been given to clipping based on the view rectangle. For
    * non-boxy projections performance may be degraded at very large scales.
    * (But we make up for this by running the task in its own thread to support
    * liveness).
    * </ul>
    * 
    * @return OMGraphicList new graphic list
    */
   protected OMGraphicList constructGraticuleLines() {

      OMGraphicList newgraphics = new OMGraphicList(20);
      // Lets figure out which lines should be painted...
      Projection projection = getProjection();

      if (projection == null) {
         return newgraphics;
      }
      tenDegreeLines = null;
      // Need the cast to make Windows happy during the ant build for jdk1.5.0_22
      double ctrLon = ((Point2D)projection.getCenter()).getX();
      if (projection instanceof GeoProj) {
         ctrLon = ((GeoProj) projection).getReferenceLon();
      }

      if (showOneAndFiveLines || showRuler || showBelowOneLines) {

         Point2D ul = projection.getUpperLeft();
         Point2D lr = projection.getLowerRight();

         float left = (float) ul.getX();
         float right = (float) lr.getX();
         float up = (float) ul.getY();
         float down = (float) lr.getY();

         if (up > 80.0f)
            up = 80.0f;
         if (down > 80.0f)
            down = 80f; // unlikely
         if (up < -80.0f)
            up = -80.0f; // unlikely
         if (down < -80)
            down = -80.0f;

         int showWhichLines = evaluateSpacing(up, down, left, right);

         // Find out whether we need to do one or two queries,
         // depending on if we're straddling the dateline.
         if ((left > 0 && right < 0) || (left > right) || (Math.abs(left - right) < 1)) {
            // Test to draw the ones and fives, which will also do
            // the labels.

            if (showWhichLines != SHOW_TENS) {
               newgraphics.add(constructGraticuleLines(up, down, left, 180.0f, showWhichLines));
               newgraphics.add(constructGraticuleLines(up, down, -180.0f, right, showWhichLines));
            } else if (showRuler) { // Just do the labels for the
               // tens lines
               newgraphics.add(constructTensLabels(up, down, left, 180.0f, true));
               newgraphics.add(constructTensLabels(up, down, -180.0f, right, false));
            }
         } else {
            // Test to draw the ones and fives, which will also do
            // the labels.
            if (showWhichLines != SHOW_TENS) {
               newgraphics = constructGraticuleLines(up, down, left, right, showWhichLines);
            } else if (showRuler) { // Just do the labels for the
               // tens lines
               newgraphics.add(constructTensLabels(up, down, left, right, true));
            }
         }
      }

      OMGraphicList list;
      if (tenDegreeLines == null) {
         list = constructTenDegreeLines(ctrLon);
         tenDegreeLines = list;
      } else {
         synchronized (tenDegreeLines) {
            setLineTypeAndProject(tenDegreeLines, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_RHUMB);
         }
      }
      if (markerLines == null) {
         list = constructMarkerLines(ctrLon);
         markerLines = list;
      } else {
         synchronized (markerLines) {
            setLineTypeAndProject(markerLines, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_RHUMB);
         }
      }

      newgraphics.add(markerLines);
      newgraphics.add(tenDegreeLines);

      if (Debug.debugging("graticule")) {
         Debug.output("GraticuleLayer.constructGraticuleLines(): " + "constructed " + newgraphics.size() + " graticule lines");
      }

      return newgraphics;
   }

   /**
    * Figure out which graticule lines should be drawn based on the treshold set
    * in the layer, and the coordinates of the screen. Method checks for
    * crossing of the dateline, but still assumes that the up and down latitude
    * coordinates are less than abs(+/-80). This is because the projection
    * shouldn't give anything above 90 degrees, and we limit the lines to less
    * than 80..
    * 
    * @param up northern latitude coordinate, in decimal degrees,
    * @param down southern latitude coordinate, in decimal degrees.
    * @param left western longitude coordinate, in decimal degrees,
    * @param right eastern longitude coordinate, in decimal degrees.
    * @return which lines should be shown, either SHOW_TENS, SHOW_FIVES and
    *         SHOW_ONES.
    */
   protected int evaluateSpacing(float up, float down, float left, float right) {
      int ret = SHOW_TENS;

      // Set the flag for when labels are wanted, but not the 1 and
      // 5 lines;
      if (!showOneAndFiveLines && !showBelowOneLines) {
         return ret;
      }

      // Find the north - south difference
      float nsdiff = up - down;
      // And the east - west difference
      float ewdiff;
      // Check for straddling the dateline -west is positive while
      // right is negative, or, in a big picture view, the west is
      // positive, east is positive, and western hemisphere is
      // between them.
      if ((left > 0 && right < 0) || (left > right) || (Math.abs(left - right) < 1)) {
         ewdiff = (180.0f - left) + (right + 180.0f);
      } else {
         ewdiff = right - left;
      }

      // And use the lesser of the two.
      float diff = (nsdiff < ewdiff) ? nsdiff : ewdiff;
      // number of 10 degree lines
      if ((diff / 10) <= (float) threshold)
         ret = SHOW_FIVES;
      // number of five degree lines
      if ((diff / 5) <= (float) threshold)
         ret = SHOW_ONES;

      return ret;
   }

   /**
    * Construct the five degree and one degree graticule lines, depending on the
    * showWhichLines setting. Assumes that the coordinates passed in do not
    * cross the dateline, and that the up is not greater than 80 and that the
    * south is not less than -80.
    * 
    * @param up northern latitude coordinate, in decimal degrees,
    * @param down southern latitude coordinate, in decimal degrees.
    * @param left western longitude coordinate, in decimal degrees,
    * @param right eastern longitude coordinate, in decimal degrees.
    * @param showWhichLines indicator for which level of lines should be
    *        included, either SHOW_FIVES or SHOW_ONES. SHOW_TENS could be there,
    *        too, but then we wouldn't do anything.
    */
   protected OMGraphicList constructGraticuleLines(float up, float down, float left, float right, int showWhichLines) {
      OMGraphicList lines = new OMGraphicList();

      // Set the line limits for the lat/lon lines...
      int north = (int) Math.ceil(up);
      if (north > 80)
         north = 80;

      int south = (int) Math.floor(down);
      south -= (south % 10); // Push down to the lowest 10 degree
      // line.
      // for neg numbers, Mod raised it, lower it again. Also
      // handle straddling the equator.
      if ((south < 0 && south > -80) || south == 0)
         south -= 10;

      int west = (int) Math.floor(left);
      west -= (west % 10);
      // for neg numbers, Mod raised it, lower it again. Also
      // handle straddling the prime meridian.
      if ((west < 0 && west > -180) || west == 0)
         west -= 10;

      int east = (int) Math.ceil(right);
      if (east > 180)
         east = 180;

      int stepSize;
      int stepSum;
      double point_x, point_y;
      // Choose how far apart the lines will be.
      stepSize = ((showWhichLines == SHOW_ONES) ? 1 : 5);
      double[] llp;
      OMPoly currentLine;
      OMText currentText;

      // For calculating text locations
      Point point = new Point();
      LatLonPoint llpoint;

      Projection projection = getProjection();

      // generate other parallels of latitude be creating series
      // of polylines
      for (int i = south; i < north; i += stepSize) {
         float lat = (float) i;
         // generate parallel of latitude North/South of the
         // equator
         if (west < 0 && east > 0) {
            llp = new double[6];
            llp[2] = lat;
            llp[3] = 0f;
            llp[4] = lat;
            llp[5] = east;
         } else {
            llp = new double[4];
            llp[2] = lat;
            llp[3] = east;
         }
         llp[0] = lat;
         llp[1] = west;

         // Do not duplicate the 10 degree line.
         if ((lat % 10) != 0) {
            currentLine = new OMPoly(llp, OMGraphic.DECIMAL_DEGREES, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_RHUMB);
            if ((lat % 5) == 0) {
               currentLine.setLinePaint(fiveDegreeColor);
            } else {
               currentLine.setLinePaint(oneDegreeColor);
            }
            lines.add(currentLine);
         }

         if (showRuler && (lat % 2) == 0) {
            if (boxy) {
               projection.forward(lat, west, point);
               point.x = 0;
               llpoint = projection.inverse(point.x, point.y, new LatLonPoint.Double());
            } else {
               llpoint = new LatLonPoint.Double(lat, west);
               stepSum = 0;
               while (stepSum < 360) {
                  point_x = projection.forward(llpoint).getX();
                  if (point_x > 0 && point_x < projection.getWidth())
                     break;
                  stepSum += stepSize;
                  llpoint.setLongitude(llpoint.getX() + stepSize);
               }
            }

            currentText = new OMText(llpoint.getY(), llpoint.getX(),
            // Move them up a little
                                     (int) 2, (int) -2, Integer.toString((int) lat), font, OMText.JUSTIFY_LEFT);
            currentText.setLinePaint(textColor);
            lines.add(currentText);
         }
      }

      // generate lines of longitude
      for (int i = west; i < east; i += stepSize) {
         float lon = (float) i;

         if (north < 0 && south > 0) {
            llp = new double[6];
            llp[2] = 0f;
            llp[3] = lon;
            llp[4] = south;
            llp[5] = lon;
         } else {
            llp = new double[4];
            llp[2] = south;
            llp[3] = lon;
         }
         llp[0] = north;
         llp[1] = lon;

         if ((lon % 10) != 0) {
            currentLine =
                  new OMPoly(llp, OMGraphic.DECIMAL_DEGREES, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_GREATCIRCLE);
            if ((lon % 5) == 0) {
               currentLine.setLinePaint(fiveDegreeColor);
            } else {
               currentLine.setLinePaint(oneDegreeColor);
            }
            lines.add(currentLine);
         }

         if (showRuler && (lon % 2) == 0) {
            if (boxy) {
               projection.forward(south, lon, point);
               point.y = projection.getHeight();
               llpoint = projection.inverse(point.x, point.y, new LatLonPoint.Double());
            } else {
               llpoint = new LatLonPoint.Double(south, lon);
               stepSum = 0;
               while (stepSum < 360) {
                  point_y = projection.forward(llpoint).getY();
                  if (point_y > 0 && point_y < projection.getHeight())
                     break;
                  stepSum += stepSize;
                  llpoint.setLatitude(llpoint.getY() + stepSize);
               }
            }

            currentText = new OMText(llpoint.getY(), llpoint.getX(),
            // Move them up a little
                                     (int) 2, (int) -5, Integer.toString((int) lon), font, OMText.JUSTIFY_CENTER);
            currentText.setLinePaint(textColor);
            lines.add(currentText);

         }
      }

      if (Debug.debugging("graticule")) {
         Debug.output("GraticuleLayer.constructTenDegreeLines(): " + "constructed " + lines.size() + " graticule lines");
      }
      lines.generate(projection);
      return lines;
   }

   /** Create the ten degree lines. */
   protected OMGraphicList constructTenDegreeLines(double ctrLon) {

      OMGraphicList lines = new OMGraphicList(3);
      OMPoly currentLine;

      // generate other parallels of latitude by creating series
      // of polylines
      for (int i = 1; i <= 8; i++) {
         for (int j = -1; j < 2; j += 2) {
            float lat = (float) (10 * i * j);
            // generate parallel of latitude North/South of the
            // equator
            double[] llp = {
               lat,
               ctrLon - 180f,
               lat,
               ctrLon - 90f,
               lat,
               ctrLon,
               lat,
               ctrLon + 90f,
               lat,
               ctrLon + 180f
            };
            currentLine = new OMPoly(llp, OMGraphic.DECIMAL_DEGREES, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_RHUMB);
            currentLine.setLinePaint(tenDegreeColor);
            lines.add(currentLine);
         }
      }

      // generate lines of longitude
      for (int i = 1; i < 18; i++) {
         for (int j = -1; j < 2; j += 2) {
            float lon = (float) (10 * i * j);
            // not quite 90.0 for beautification reasons.
            double[] llp = {
               80f,
               lon,
               0f,
               lon,
               -80f,
               lon
            };
            if (MoreMath.approximately_equal(Math.abs(lon), 90f, 0.001f)) {
               llp[0] = 89.999f;
               llp[4] = -89.999f;
            }
            currentLine =
                  new OMPoly(llp, OMGraphic.DECIMAL_DEGREES, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_GREATCIRCLE);
            currentLine.setLinePaint(tenDegreeColor);
            lines.add(currentLine);
         }
      }

      if (Debug.debugging("graticule")) {
         Debug.output("GraticuleLayer.constructTenDegreeLines(): " + "constructed " + lines.size() + " graticule lines");
      }
      lines.generate(getProjection());
      return lines;
   }

   /**
    * Constructs the labels for the tens lines. Called from within the
    * constructGraticuleLines if the showRuler variable is true. Usually called
    * only if the ones and fives lines are not being drawn.
    * 
    * @param up northern latitude coordinate, in decimal degrees,
    * @param down southern latitude coordinate, in decimal degrees.
    * @param left western longitude coordinate, in decimal degrees,
    * @param right eastern longitude coordinate, in decimal degrees.
    * @param doLats do the latitude labels if true.
    * @return OMGraphicList of labels.
    */
   protected OMGraphicList constructTensLabels(float up, float down, float left, float right, boolean doLats) {

      OMGraphicList labels = new OMGraphicList();

      // Set the line limits for the lat/lon lines...
      int north = (int) Math.ceil(up);
      if (north > 80)
         north = 80;

      int south = (int) Math.floor(down);
      south -= (south % 10); // Push down to the lowest 10 degree
      // line.
      // for neg numbers, Mod raised it, lower it again
      if ((south < 0 && south > -70) || south == 0) {
         south -= 10;
      }

      int west = (int) Math.floor(left);
      west -= (west % 10);
      // for neg numbers, Mod raised it, lower it again
      if ((west < 0 && west > -170) || west == 0) {
         west -= 10;
      }

      int east = (int) Math.ceil(right);
      if (east > 180)
         east = 180;

      int stepSize = 10;
      int stepSum;
      double point_x, point_y;
      OMText currentText;

      // For calculating text locations
      Point point = new Point();
      LatLonPoint llpoint;
      Projection projection = getProjection();

      if (doLats) {

         // generate other parallels of latitude be creating series
         // of labels
         for (int i = south; i < north; i += stepSize) {
            float lat = (float) i;

            if ((lat % 2) == 0) {
               if (boxy) {
                  projection.forward(lat, west, point);
                  point.x = 0;
                  llpoint = projection.inverse(point.x, point.y, new LatLonPoint.Double());
               } else {
                  llpoint = new LatLonPoint.Double(lat, west);
                  stepSum = 0;
                  while (stepSum < 360) {
                     point_x = projection.forward(llpoint).getX();
                     if (point_x > 0 && point_x < projection.getWidth())
                        break;
                     stepSum += stepSize;
                     llpoint.setLongitude(llpoint.getX() + stepSize);
                  }
               }

               currentText = new OMText(llpoint.getY(), llpoint.getX(), (int) 2, (int) -2, // Move
                                        // them
                                        // up a
                                        // little
                                        Integer.toString((int) lat), font, OMText.JUSTIFY_LEFT);
               currentText.setLinePaint(textColor);
               labels.add(currentText);
            }
         }
      }

      // generate labels of longitude
      for (int i = west; i < east; i += stepSize) {
         float lon = (float) i;

         if ((lon % 2) == 0) {
            if (boxy) {
               projection.forward(south, lon, point);
               point.y = projection.getHeight();
               llpoint = projection.inverse(point.x, point.y, new LatLonPoint.Double());
            } else {
               llpoint = new LatLonPoint.Double(south, lon);
               stepSum = 0;
               while (stepSum < 360) {
                  point_y = projection.forward(llpoint).getY();
                  if (point_y > 0 && point_y < projection.getHeight())
                     break;
                  stepSum += stepSize;
                  llpoint.setLatitude(llpoint.getY() + stepSize);
               }
            }

            currentText = new OMText(llpoint.getY(), llpoint.getX(),
            // Move them up a little
                                     (int) 2, (int) -5, Integer.toString((int) lon), font, OMText.JUSTIFY_CENTER);
            currentText.setLinePaint(textColor);
            labels.add(currentText);

         }
      }

      if (Debug.debugging("graticule")) {
         Debug.output("GraticuleLayer.constructTensLabels(): " + "constructed " + labels.size() + " graticule labels");
      }
      labels.generate(projection);
      return labels;
   }

   /** Constructs the Dateline and Prime Meridian lines. */
   protected OMGraphicList constructMarkerLines(double ctrLon) {

      OMGraphicList lines = new OMGraphicList(3);
      OMPoly currentLine;

      // generate Prime Meridian and Dateline
      for (int j = 0; j < 360; j += 180) {
         float lon = (float) j;
         double[] llp = {
            90f,
            lon,
            0f,
            lon,
            -90f,
            lon
         };
         currentLine =
               new OMPoly(llp, OMGraphic.DECIMAL_DEGREES, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_GREATCIRCLE);
         currentLine.setLinePaint(dateLineColor);
         lines.add(currentLine);
      }

      // equator
      double[] llp = {
         0f,
         ctrLon - 180f,
         0f,
         ctrLon - 90f,
         0f,
         ctrLon,
         0f,
         ctrLon + 90f,
         0f,
         ctrLon + 180f
      };
      // polyline
      currentLine = new OMPoly(llp, OMGraphic.DECIMAL_DEGREES, boxy ? OMGraphic.LINETYPE_STRAIGHT : OMGraphic.LINETYPE_GREATCIRCLE);
      currentLine.setLinePaint(equatorColor);
      lines.add(currentLine);

      if (Debug.debugging("graticule")) {
         Debug.output("GraticuleLayer.constructMarkerLines(): " + "constructed " + lines.size() + " graticule lines");
      }
      lines.generate(getProjection());
      return lines;
   }

   /**
    * Take a graphic list, and set all the items on the list to the line type
    * specified, and project them into the current projection.
    * 
    * @param list the list containing the lines to change.
    * @param lineType the line type to change the lines to.
    */
   protected void setLineTypeAndProject(OMGraphicList list, int lineType) {
      int size = list.size();
      OMGraphic graphic;
      for (int i = 0; i < size; i++) {
         graphic = list.getOMGraphicAt(i);
         graphic.setLineType(lineType);
         graphic.generate(getProjection());
      }
   }

   // ----------------------------------------------------------------------
   // GUI
   // ----------------------------------------------------------------------

   /** The user interface palette for the Graticule layer. */
   protected Box paletteBox = null;

   /** Creates the interface palette. */
   public java.awt.Component getGUI() {

      if (paletteBox == null) {
         if (Debug.debugging("graticule"))
            Debug.output("GraticuleLayer: creating Graticule Palette.");

         paletteBox = Box.createVerticalBox();

         JPanel layerPanel =
               PaletteHelper.createPaletteJPanel(i18n.get(GraticuleLayer.class, "layerPanel", "Graticule Layer Options"));

         ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               String ac = e.getActionCommand();

               if (ac.equalsIgnoreCase(ShowRulerProperty)) {
                  JCheckBox jcb = (JCheckBox) e.getSource();
                  showRuler = jcb.isSelected();
               } else if (ac.equalsIgnoreCase(ShowOneAndFiveProperty)) {
                  JCheckBox jcb = (JCheckBox) e.getSource();
                  showOneAndFiveLines = jcb.isSelected();
               } else {
                  Debug.error("Unknown action command \"" + ac + "\" in GraticuleLayer.actionPerformed().");
               }
            }
         };

         showRulerButton = new JCheckBox(i18n.get(GraticuleLayer.class, "showRulerButton", "Show Lat/Lon Labels"), showRuler);
         showRulerButton.addActionListener(al);
         showRulerButton.setActionCommand(ShowRulerProperty);

         show15Button =
               new JCheckBox(i18n.get(GraticuleLayer.class, "show15Button", "Show 1, 5 Degree Lines"), showOneAndFiveLines);
         show15Button.addActionListener(al);
         show15Button.setActionCommand(ShowOneAndFiveProperty);

         // showBelow1Button = new JCheckBox(i18n.get(GraticuleLayer.class,
         // "showSub1Button",
         // "Show Sub-1 Degree Lines"), showBelowOneLines);
         // showBelow1Button.addActionListener(al);
         // showBelow1Button.setActionCommand(ShowBelowOneProperty);

         layerPanel.add(showRulerButton);
         layerPanel.add(show15Button);
         // layerPanel.add(showBelow1Button);
         paletteBox.add(layerPanel);

         JPanel subbox3 = new JPanel(new GridLayout(0, 1));

         JButton setProperties = new JButton(i18n.get(GraticuleLayer.class, "setProperties", "Preferences"));
         setProperties.setActionCommand(DisplayPropertiesCmd);
         setProperties.addActionListener(this);
         subbox3.add(setProperties);

         JButton redraw = new JButton(i18n.get(GraticuleLayer.class, "redraw", "Redraw Graticule Layer"));
         redraw.setActionCommand(RedrawCmd);
         redraw.addActionListener(this);
         subbox3.add(redraw);
         paletteBox.add(subbox3);
      }
      return paletteBox;
   }

   // ----------------------------------------------------------------------

   // ActionListener interface implementation
   // ----------------------------------------------------------------------

   /**
    * Used just for the redraw button.
    */
   public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      String command = e.getActionCommand();

      if (command == RedrawCmd) {
         // redrawbutton
         if (isVisible()) {
            doPrepare();
         }
      }
   }

}
