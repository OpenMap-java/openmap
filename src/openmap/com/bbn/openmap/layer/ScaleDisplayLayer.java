/* **********************************************************************
 *
 *  ROLANDS & ASSOCIATES Corporation
 *  500 Sloat Avenue
 *  Monterey, CA 93940
 *  (831) 373-2025
 *
 *  Copyright (C) 2002, 2003 ROLANDS & ASSOCIATES Corporation. All rights reserved.
 *  Openmap is a trademark of BBN Technologies, A Verizon Company
 *
 *
 * **********************************************************************
 *
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/ScaleDisplayLayer.java,v $
 * $Revision: 1.9 $
 * $Date: 2005/12/09 21:09:08 $
 * $Author: dietrick $
 *
 * **********************************************************************
 */

package com.bbn.openmap.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * Layer objects are components which can be added to the MapBean to make a map.
 * <p>
 * Layers implement the ProjectionListener interface to listen for
 * ProjectionEvents. When the projection changes, they may need to refetch,
 * regenerate their graphics, and then repaint themselves into the new view.
 * <p>
 * ### Layer used by the overview handler <br>
 * scaleLayer.class=com.rolands.jtlsweb.map.layer.ScaleDisplayLayer <br>
 * scaleLayer.prettyName=Scale <br>
 * scaleLayer.lineColor=ff777777 <br>
 * scaleLayer.textColor=ff000000 <br>
 * scaleLayer.unitOfMeasure=nm <br>
 * scaleLayer.locationXoffset=-10 <br>
 * scaleLayer.locationYoffset=-20 <br>
 * scaleLayer.width=150 <br>
 * scaleLayer.height=10 <br>
 * <br>
 * unitOfMeasure - any com.bbn.openmap.proj.Length instance returned by
 * Length.get(string). <br>
 * locationXoffset - offset in pixels from left/right, positive from left edge,
 * negative from right edge <br>
 * locationYoffset - offset in pixels from top/bottom, positive from top edge,
 * negative from bottom edge <br>
 * width - width of scale indicator bar in pixels <br>
 * height - height of scale indicator bar in pixels <br>
 * <br>
 */
public class ScaleDisplayLayer
      extends OMGraphicHandlerLayer {

   public ScaleDisplayLayer() {
      super();
      setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
      setUnitOfMeasure(Length.KM.toString());
   }

   protected Logger logger = Logger.getLogger("com.bbn.openmap.layer.ScaleDisplayLayer");

   // Color variables for different line types
   protected java.awt.Color lineColor = null;
   protected java.awt.Color textColor = null;

   // Default colors to use, if not specified in the properties.
   protected String defaultLineColorString = "FFFFFF";
   protected String defaultTextColorString = "FFFFFF";
   protected String defaultUnitOfMeasureString = "km";
   protected int defaultLocationXoffset = -10;
   protected int defaultLocationYoffset = -10;
   protected int defaultWidth = 150;
   protected int defaultHeight = 10;

   // property text values
   public static final String UnitOfMeasureProperty = "unitOfMeasure";
   public static final String LocationXOffsetProperty = "locationXoffset";
   public static final String LocationYOffsetProperty = "locationYoffset";
   public static final String WidthProperty = "width";
   public static final String HeightProperty = "height";

   protected String unitOfMeasure = null;
   protected Length uom = Length.get(defaultUnitOfMeasureString);
   protected String uomAbbr = uom.getAbbr();
   protected int locationXoffset = defaultLocationXoffset;
   protected int locationYoffset = defaultLocationYoffset;
   protected int width = defaultWidth;
   protected int height = defaultHeight;

   protected DrawingAttributes dAttributes = DrawingAttributes.getDefaultClone();

   /**
    * Sets the properties for the <code>Layer</code>. This allows
    * <code>Layer</code> s to get a richer set of parameters than the
    * <code>setArgs</code> method.
    * 
    * @param prefix the token to prefix the property names
    * @param properties the <code>Properties</code> object
    */
   public void setProperties(String prefix, Properties properties) {
      super.setProperties(prefix, properties);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      dAttributes.setProperties(prefix, properties);

      String unitOfMeasureString = properties.getProperty(prefix + UnitOfMeasureProperty);
      if (unitOfMeasureString != null) {
         setUnitOfMeasure(unitOfMeasureString);
      }

      locationXoffset = PropUtils.intFromProperties(properties, prefix + LocationXOffsetProperty, defaultLocationXoffset);

      locationYoffset = PropUtils.intFromProperties(properties, prefix + LocationYOffsetProperty, defaultLocationYoffset);

      width = PropUtils.intFromProperties(properties, prefix + WidthProperty, defaultWidth);

      height = PropUtils.intFromProperties(properties, prefix + HeightProperty, defaultHeight);
   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);
      String prefix = PropUtils.getScopedPropertyPrefix(this);

      dAttributes.setProperties(props);

      props.put(prefix + LocationXOffsetProperty, Integer.toString(locationXoffset));
      props.put(prefix + LocationYOffsetProperty, Integer.toString(locationYoffset));
      props.put(prefix + WidthProperty, Integer.toString(width));
      props.put(prefix + HeightProperty, Integer.toString(height));

      props.put(prefix + UnitOfMeasureProperty, unitOfMeasure);

      return props;
   }

   public synchronized OMGraphicList prepare() {
      int w, h, left_x = 0, right_x = 0, lower_y = 0, upper_y = 0;
      Projection projection = getProjection();
      OMGraphicList graphics = new OMGraphicList();

      w = projection.getWidth();
      h = projection.getHeight();
      if (locationXoffset < 0) {
         left_x = w + locationXoffset - width;
         right_x = w + locationXoffset;
      } else if (locationXoffset >= 0) {
         left_x = locationXoffset;
         right_x = locationXoffset + width;
      }
      if (locationYoffset < 0) {
         upper_y = h + locationYoffset - height;
         lower_y = h + locationYoffset;
      } else if (locationYoffset >= 0) {
         upper_y = locationYoffset;
         lower_y = locationYoffset + height;
      }

      graphics.clear();

      OMLine line = new OMLine(left_x, lower_y, right_x, lower_y);
      dAttributes.setTo(line);
      graphics.add(line);

      line = new OMLine(left_x, lower_y, left_x, upper_y);
      dAttributes.setTo(line);
      graphics.add(line);

      line = new OMLine(right_x, lower_y, right_x, upper_y);
      dAttributes.setTo(line);
      graphics.add(line);

      /*
       * We need to use better coordinates to measure distance, like the same
       * pixel distance at the center of the map. There's a problem using the
       * lower right location, in that those distances decrease as you zoom out.
       */

      int y = h / 2;
      int x = w / 2;
      int xSide = (right_x - left_x) / 2;

      LatLonPoint loc1 = projection.inverse(x - xSide, y, new LatLonPoint.Double());
      LatLonPoint loc2 = projection.inverse(x + xSide, y, new LatLonPoint.Double());

      double dist = uom.fromRadians(loc1.distance(loc2));

      String outtext;
      if (dist < 1.0f) {
         outtext = String.format("%.3f %s", dist, uomAbbr);
      } else if (dist < 10.0f) {
         outtext = String.format("%.2f %s", dist, uomAbbr);
      } else if (dist < 100.0f) {
         outtext = String.format("%.1f %s", dist, uomAbbr);
      } else {
         outtext = String.format("%.0f %s", dist, uomAbbr);
      }

      OMText text = new OMText((left_x + right_x) / 2, lower_y - 3, "" + outtext, OMText.JUSTIFY_CENTER);

      Font font = text.getFont();
      text.setFont(font.deriveFont(font.getStyle(), font.getSize() + 4));

      dAttributes.setTo(text);
      text.setTextMatteColor((Color) dAttributes.getMattingPaint());
      text.setTextMatteStroke(new BasicStroke(5));
      text.setMattingPaint(OMColor.clear);
      graphics.add(text);
      graphics.generate(projection);

      return graphics;
   }

   /**
    * Getter for property unitOfMeasure.
    * 
    * @return Value of property unitOfMeasure.
    */
   public String getUnitOfMeasure() {
      return this.unitOfMeasure;
   }

   /**
    * Setter for property unitOfMeasure.
    * 
    * @param unitOfMeasure New value of property unitOfMeasure.
    * 
    * @throws PropertyVetoException
    */
   public void setUnitOfMeasure(String unitOfMeasure) {
      if (unitOfMeasure == null)
         unitOfMeasure = Length.KM.toString();
      this.unitOfMeasure = unitOfMeasure;

      // There is a bug in the Length.get() method that will not
      // return
      // the correct (or any value) for a requested uom.
      // This does not work:
      // uom = com.bbn.openmap.proj.Length.get(unitOfMeasure);

      // Therefore, The following code correctly obtains the proper
      // Length object.

      Length[] choices = Length.getAvailable();
      uom = null;
      for (int i = 0; i < choices.length; i++) {
         if (unitOfMeasure.equalsIgnoreCase(choices[i].toString()) || unitOfMeasure.equalsIgnoreCase(choices[i].getAbbr())) {
            uom = choices[i];
            break;
         }
      }

      // of no uom is found assign Kilometers as the default.
      if (uom == null)
         uom = Length.KM;

      uomAbbr = uom.getAbbr();

   }

   JPanel palettePanel;
   ButtonGroup uomButtonGroup;
   Vector<JRadioButton> buttons = new Vector<JRadioButton>();

   /** Creates the interface palette. */
   public java.awt.Component getGUI() {

      if (palettePanel == null) {

         logger.fine("creating palette.");

         palettePanel = new JPanel();
         uomButtonGroup = new ButtonGroup();

         palettePanel.setLayout(new javax.swing.BoxLayout(palettePanel, javax.swing.BoxLayout.Y_AXIS));
         palettePanel.setBorder(new javax.swing.border.TitledBorder("Unit Of Measure"));

         java.awt.event.ActionListener al = new ActionListener() {
            // We don't have to check for action commands or anything like that.
            // We know this listener is going to be added to JRadioButtons that
            // are labeled with abbreviations for length.
            public void actionPerformed(ActionEvent e) {
               JRadioButton jrb = (JRadioButton) e.getSource();
               setUnitOfMeasure(jrb.getText());
            }
         };

         for (Length lengthType : Length.getAvailable()) {
            JRadioButton jrb = new JRadioButton();
            jrb.setText(lengthType.getAbbr());
            jrb.setToolTipText(lengthType.toString());
            uomButtonGroup.add(jrb);
            palettePanel.add(jrb);

            jrb.addActionListener(al);

            jrb.setSelected(unitOfMeasure.equalsIgnoreCase(lengthType.getAbbr()));
            buttons.add(jrb);
         }

      } else {
         for (JRadioButton button : buttons) {
            button.setSelected(uom.getAbbr().equalsIgnoreCase(button.getText()));
         }
      }

      return palettePanel;
   }
}
