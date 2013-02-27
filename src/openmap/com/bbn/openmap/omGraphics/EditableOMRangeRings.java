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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMRangeRings.java,v $
// $RCSfile: EditableOMRangeRings.java,v $
// $Revision: 1.15 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.event.UndoEvent;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 */
public class EditableOMRangeRings
      extends EditableOMCircle {

   // TODO need to update the memory mechanism between OMRangeRings that are
   // being edited/created so that statics aren't being used. The only other
   // place this info could be held is either in the drawing tool or in the
   // OMRangeRingsLoader (OMCircleLoader), and I don't think we have a handle to
   // either of them from here. Gah! I hate using statics, they make me feel
   // dirty.
   protected static int lastInterval;
   protected static Length lastUnit;
   protected static boolean snapToInterval = false;

   /**
    * Create the EditableOMRangeRings, setting the state machine to create the
    * circle off of the gestures.
    */
   public EditableOMRangeRings() {
      createGraphic(null);
   }

   /**
    * Create an EditableOMRangeRings with the circleType and renderType
    * parameters in the GraphicAttributes object.
    */
   public EditableOMRangeRings(GraphicAttributes ga) {
      createGraphic(ga);
   }

   /**
    * Create the EditableOMRangeRings with an OMCircle already defined, ready
    * for editing.
    *
    * @param omc OMCircle that should be edited.
    */
   public EditableOMRangeRings(OMRangeRings omc) {
      setGraphic(omc);
   }

   /**
    * Create and set the graphic within the state machine. The GraphicAttributes
    * describe the type of circle to create.
    */
   public void createGraphic(GraphicAttributes ga) {
      init();
      stateMachine.setUndefined();
      int renderType = OMGraphic.RENDERTYPE_LATLON;

      if (ga != null) {
         renderType = ga.getRenderType();
      }

      if (Debug.debugging("eomc")) {
         Debug.output("EditableOMRangeRings.createGraphic(): rendertype = " + renderType);
      }

      circle = new OMRangeRings(90f, -180f, 0f);

      if (ga != null) {
         ga.setTo(circle, true);
      }
   }

   /**
    * Modifies the gui to not include line type adjustments, and adds widgets to
    * control range ring settings.
    *
    * @param graphicAttributes the GraphicAttributes to use to get the GUI
    *        widget from to control those parameters for this EOMG.
    * @return java.awt.Component to use to control parameters for this EOMG.
    */
   public Component getGUI(GraphicAttributes graphicAttributes) {
      Debug.message("eomg", "EditableOMRangeRings.getGUI");
      if (graphicAttributes != null) {
         // JComponent panel = graphicAttributes.getColorAndLineGUI();
         JComponent toolbar = createAttributePanel(graphicAttributes);
         // panel.add(getRangeRingGUI());
         getRangeRingGUI(graphicAttributes.getOrientation(), toolbar);
         return toolbar;
      } else {
         return getRangeRingGUI();
      }
   }

   public void updateInterval(int val) {
      int oldInterval = ((OMRangeRings) circle).getInterval();
      ((OMRangeRings) circle).setInterval(val);
      lastInterval = val;
      if (intervalField != null) {
         intervalField.setText(Integer.toString(val));
      }

      if (snapToInterval) {
         setRadius(circle.getRadius());
      }
      if (oldInterval != val) {
         updateCurrentState(null);
      }
      redraw(null, true);
   }

   public void updateInterval(String intervalStr) {
      int oldValue = ((OMRangeRings) circle).getInterval();
      int value = interpretValue(intervalStr);

      if (value <= 0) {
         value = oldValue;
      }

      updateInterval(value);
   }

   public int interpretValue(String intervalStr) {
      int value = -1;
      try {
         if (intervalStr.toLowerCase().endsWith("m")) {
            intervalStr = intervalStr.substring(0, intervalStr.length() - 1);
            value = (int) df.parse(intervalStr).intValue() * 1000000;
         } else if (intervalStr.toLowerCase().endsWith("k")) {
            intervalStr = intervalStr.substring(0, intervalStr.length() - 1);
            value = df.parse(intervalStr).intValue() * 1000;
         } else if (intervalStr.trim().length() == 0) {
            // do nothing
         } else {
            value = df.parse(intervalStr).intValue();
         }
      } catch (java.text.ParseException e) {
         Debug.error("RangeRing interval value not valid: " + intervalStr);
      } catch (NumberFormatException e) {
         Debug.error("RangeRing interval value not valid: " + intervalStr);
      }
      return value;
   }

   // Need these three for UndoEvents to be able to update them if an update
   // event gets implemented.
   protected JTextField intervalField = null;
   protected JComboBox unitsCombo = null;
   protected JCheckBox snapCheckBox = null;

   protected JToolBar rrToolBar = null;
   protected transient DecimalFormat df = new DecimalFormat();
   protected JComponent attributeBox;

   protected JComponent getRangeRingGUI() {
      return getRangeRingGUI(SwingConstants.HORIZONTAL, (JComponent) null);
   }

   /**
    * Get the GUI associated with changing the Text.
    *
    * @param orientation SwingConstants.HORIZONTAL/VERTICAL
    * @param guiComp the JComponent to add stuff to. If the orientation is
    *        HORIZONTAL, the components will be added directly to this
    *        component, or to a new JComponent that is returned if null. If the
    *        orientation is Vertical, a button will be added to the guiComp, or
    *        returned. This button will call up a dialog box with the settings,
    *        since they don't really lay out vertically.
    * @return JComponent for controlling range-ring specific attributes.
    */
   protected JComponent getRangeRingGUI(int orientation, JComponent guiComp) {
      attributeBox = null;

      if (guiComp == null || orientation == SwingConstants.VERTICAL) {
         attributeBox = javax.swing.Box.createHorizontalBox();

         attributeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
         attributeBox.setAlignmentY(Component.CENTER_ALIGNMENT);

         if (orientation == SwingConstants.HORIZONTAL) {
            guiComp = attributeBox;
         }
      } else if (orientation == SwingConstants.HORIZONTAL) {
         attributeBox = guiComp;
      }

      if (guiComp == null) {
         guiComp = new JPanel();
      }

      guiComp.add(PaletteHelper.getToolBarFill(orientation));

      if (orientation == SwingConstants.VERTICAL) {
         JButton launchButton = new JButton("RR");
         launchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               if (attributeBox != null) {
                  JDialog dialog = new JDialog();
                  dialog.setContentPane(attributeBox);
                  dialog.setModal(true);
                  dialog.pack();
                  dialog.setLocationRelativeTo((JButton) ae.getSource());
                  dialog.setVisible(true);
               }
            }
         });
         guiComp.add(launchButton);
      }

      configureRangeRings();
      intervalField = makeIntervalField();
      attributeBox.add(intervalField);

      unitsCombo = makeUnitsCombo();
      attributeBox.add(unitsCombo);
      snapCheckBox = makeSnapCheckBox();
      attributeBox.add(snapCheckBox);

      return guiComp;
   }

   private void configureRangeRings() {
      ((OMRangeRings) circle).setInterval(getInterval());
      ((OMRangeRings) circle).setIntervalUnits(getUnits());
   }

   private int getInterval() {
      return (!isNewRing()) ? ((OMRangeRings) circle).getInterval() : haveUserSpecifiedValue() ? lastInterval
            : OMRangeRings.DEFAULT_INTERVAL;
   }

   private Length getUnits() {
      return (!isNewRing()) ? ((OMRangeRings) circle).getIntervalUnits() : haveUserSpecifiedValue() ? lastUnit : null;
   }

   private boolean isNewRing() {
      // we rely on interval units not being initialized during construction
      return (((OMRangeRings) circle).getIntervalUnits() == null);
   }

   private boolean haveUserSpecifiedValue() {
      // lastUnit is not null if the user made a selection with the comboBox
      return (lastUnit != null);
   }

   private JTextField makeIntervalField() {
      JTextField field = new JTextField(Integer.toString(((OMRangeRings) circle).getInterval()), 5);
      field.setMargin(new Insets(0, 1, 0, 1));
      // without minimum size set, field can be too small to use
      field.setMinimumSize(new Dimension(40, 18));
      field.setHorizontalAlignment(JTextField.RIGHT);
      field.setToolTipText(i18n.get(this, "intervalField.tooltip", "Value for interval between rings."));
      field.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            updateInterval(((JTextField) (ae.getSource())).getText());
         }
      });
      // Users forget to hit Enter, which is required for an action event,
      // then wonder why the rings they draw don't have the desired value.
      // Adding a focus listener addresses this issue.
      field.addFocusListener(new FocusAdapter() {
         public void focusLost(FocusEvent event) {
            if (!event.isTemporary()) {
               updateInterval(((JTextField) (event.getSource())).getText());
            }
         }
      });
      return field;
   }

   private JComboBox makeUnitsCombo() {
      Length[] available = Length.getAvailable();
      String[] unitStrings = new String[available.length + 1];

      String current = null;
      Length l = ((OMRangeRings) circle).getIntervalUnits();
      if (l != null) {
         current = l.toString();
      }

      int currentIndex = unitStrings.length - 1;

      for (int i = 0; i < available.length; i++) {
         unitStrings[i] = available[i].toString();
         if (unitStrings[i] != null && unitStrings[i].equals(current)) {
            currentIndex = i;
         }
      }
      unitStrings[unitStrings.length - 1] = i18n.get(this, "unitStrings.concentric", "concentric");

      JComboBox combo = new JComboBox(unitStrings);
      combo.setBorder(new EmptyBorder(0, 1, 0, 1));
      combo.setSelectedIndex(currentIndex);
      combo.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JComboBox jcb = (JComboBox) e.getSource();
            OMRangeRings rr = (OMRangeRings) circle;
            Length newLength = Length.get((String) jcb.getSelectedItem());
            Length oldLength = rr.getIntervalUnits();

            /*
             * If newLength is not null and oldLength is not null, just
             * translate the distance that is current specified. If newLength is
             * null, then find out how many rings are on the range ring and set
             * the interval to that. If oldLength is null, find out the radius
             * and divide it by the number of rings - 1.
             */

            int value = interpretValue(intervalField.getText());
            if (value <= 0) {
               value = 4;
            }

            if (newLength != null && oldLength != null) {
               value = (int) newLength.fromRadians(oldLength.toRadians(value));
            } else {
               int numSubCircles;
               if (rr.subCircles == null || rr.subCircles.length == 0) {
                  numSubCircles = 1;
               } else {
                  numSubCircles = rr.subCircles.length;
               }

               if (newLength == null) {
                  value = numSubCircles;
               } else if (oldLength == null) {
                  value = (int) Math.ceil(newLength.fromRadians(Length.DECIMAL_DEGREE.toRadians(rr.getRadius())) / numSubCircles);
               }
            }

            ((OMRangeRings) circle).setIntervalUnits(newLength);
            lastUnit = newLength;
            updateInterval(value);
         }
      });
      return combo;
   }

   private JCheckBox makeSnapCheckBox() {
      String snapText = i18n.get(this, "snapToInterval", "Snap");
      JCheckBox snapBox = new JCheckBox(snapText, isSnapToInterval());
      snapText = i18n.get(this, "snapToInterval", I18n.TOOLTIP, "Round radius to nearest interval value.");
      snapBox.setToolTipText(snapText);
      snapBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            setSnapToInterval(((JCheckBox) ae.getSource()).isSelected());
            if (snapToInterval) {
               setRadius(circle.getRadius());
            }
            updateCurrentState(null);
            redraw(null, true);
         }
      });
      return snapBox;
   }

   protected boolean drawLabelsHolder = true;

   /**
    * A convenience method that gives an EditableOMGraphic a chance to modify
    * the OMGraphic so it can be drawn quickly, by turning off labels, etc,
    * right before the XORpainting happens. The OMGraphic should be configured
    * so that the render method does the least amount of painting possible. Note
    * that the DrawingAttributes for the OMGraphic have already been set to
    * DrawingAttributes.DEFAULT (black line, clear fill).
    */
   protected void modifyOMGraphicForEditRender() {
      OMRangeRings omrr = (OMRangeRings) getGraphic();
      drawLabelsHolder = omrr.getDrawLabels();
      omrr.setDrawLabels(false);
   }

   /**
    * A convenience method that gives an EditableOMGraphic a chance to reset the
    * OMGraphic so it can be rendered normally, after it has been modified for
    * quick paints. The DrawingAttributes for the OMGraphic have already been
    * reset to their normal settings, from the DrawingAttributes.DEFAULT
    * settings that were used for the quick paint.
    */
   protected void resetOMGraphicAfterEditRender() {
      ((OMRangeRings) getGraphic()).setDrawLabels(drawLabelsHolder);
   }

   public boolean isSnapToInterval() {
      return snapToInterval;
   }

   public void setSnapToInterval(boolean sti) {
      snapToInterval = sti;
   }

   protected void setRadius(double radius) {
      if (circle != null) {
         if (snapToInterval) {
            OMRangeRings rr = (OMRangeRings) circle;
            Length units = rr.getIntervalUnits();
            if (units != null) {
               double rds = units.fromRadians(Length.DECIMAL_DEGREE.toRadians(radius));
               radius = Math.round(rds / rr.getInterval()) * rr.getInterval();
               radius = Length.DECIMAL_DEGREE.fromRadians(units.toRadians(radius));
            }
         }
         circle.setRadius(radius);
      }
   }

   /**
    * Create an UndoEvent that can get an OMRangeRing back to what it looks like
    * right now.
    */
   protected UndoEvent createUndoEventForCurrentState(String whatHappened) {
      if (whatHappened == null) {
         whatHappened = i18n.get(this.getClass(), "rangeRingUndoString", "Edit");
      }
      return new OMRangeRingUndoEvent(this, whatHappened);
   }

   /**
    * Subclass for undoing edits for OMRangeRing classes, handles events that
    * may affect the extra GUI widgets.
    *
    * @author ddietrick
    */
   public static class OMRangeRingUndoEvent
         extends OMGraphicUndoEvent
         implements UndoEvent {

      boolean snap;

      public OMRangeRingUndoEvent(EditableOMRangeRings eomp, String description) {
         super(eomp, description);
         snap = eomp.isSnapToInterval();
      }

      protected void setSubclassState() {
         OMRangeRings rrStateHolder = (OMRangeRings) stateHolder;
         EditableOMRangeRings eomrr = (EditableOMRangeRings) eomg;
         if (eomrr.snapCheckBox != null) {
            eomrr.snapCheckBox.setSelected(snap);
            eomrr.setSnapToInterval(snap);
         }

         if (eomrr.intervalField != null) {
            eomrr.intervalField.setText(Integer.toString(rrStateHolder.getInterval()));
         }

         Length intervalUnits = rrStateHolder.getIntervalUnits();
         if (eomrr.unitsCombo != null && intervalUnits != null) {
            eomrr.unitsCombo.setSelectedItem(intervalUnits.toString());
         }
      }
   }
}