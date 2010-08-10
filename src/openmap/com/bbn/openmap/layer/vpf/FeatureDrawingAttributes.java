// **********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: FeatureDrawingAttributes.java,v $
//$Revision: 1.5 $
//$Date: 2006/03/06 16:13:59 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.OptionPropertyEditor;

/**
 * The FeatureDrawingAttributes class is used by the VPFFeatureGraphicWarehouse
 * to control the fetching and display of feature attributes out of the feature
 * attribute file, controlled by the FeatureClassInfo class.
 * 
 * @author dietrick
 */
public class FeatureDrawingAttributes
      extends DrawingAttributes {

   private static final long serialVersionUID = 1L;
   /**
    * The main GUI panel containing the superclass GUI and attribute-fetching
    * GUI.
    */
   protected JPanel guiPanel;
   /**
    * The GUI panel containing the attribute fetching choices.
    */
   protected JPanel attributePanel;
   /**
    * A handle to the FeatureClassInfo class containing the attribute
    * information.
    */
   protected FeatureClassInfo fci;
   /**
    * The GUI combo box for attribute choices.
    */
   protected JComboBox attributeJCB;
   /**
    * The GUI combo box for choices on how to display the attributes.
    */
   protected JComboBox displayTypeJCB;
   /**
    * The chosen display type, which gets set as a property in each OMGraphic
    * for retrieval by the layer.
    */
   protected String displayType;
   /**
    * The chosen attribute column index in the FCI file.
    */
   protected int attributeCol;
   /**
    * The desired attribute column name as specified in properties.
    */
   protected String attributeColName;

   public static final String DisplayTypeProperty = "attributeDisplay";
   public static final String AttributeProperty = "attribute";

   /**
    * Default creation of the FeatureDrawingAttributes.
    */
   public FeatureDrawingAttributes() {
      super();
   }

   /**
    * @param props
    */
   public FeatureDrawingAttributes(Properties props) {
      super(props);
   }

   /**
    * @param prefix
    * @param props
    */
   public FeatureDrawingAttributes(String prefix, Properties props) {
      super(prefix, props);
   }

   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);
      if (props == null) {
         return;
      }
      prefix = PropUtils.getScopedPropertyPrefix(prefix);
      setDisplayType(props.getProperty(prefix + DisplayTypeProperty));
      attributeColName = props.getProperty(prefix + AttributeProperty, attributeColName);
   }

   /**
    * PropertyConsumer method that retrieves the current values of settable
    * properties.
    */
   public Properties getProperties(Properties props) {
      props = super.getProperties(props);
      String prefix = PropUtils.getScopedPropertyPrefix(this);
      props.put(prefix + DisplayTypeProperty, PropUtils.unnull(displayType));
      props.put(prefix + AttributeProperty, PropUtils.unnull(attributeColName));

      return props;
   }

   /**
    * PropertyConsumer method that gathers information about the settable
    * properties.
    */
   public Properties getPropertyInfo(Properties props) {
      props = super.getPropertyInfo(props);

      props.put(DisplayTypeProperty, "How the property should be displayed.");
      props.put(DisplayTypeProperty + LabelEditorProperty, "Attribute display type");
      props.put(DisplayTypeProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ComboBoxPropertyEditor");
      props.put(DisplayTypeProperty + OptionPropertyEditor.ScopedOptionsProperty, "none tt il l");
      props.put(DisplayTypeProperty + ".none", "None");
      props.put(DisplayTypeProperty + ".tt", "Tooltip");
      props.put(DisplayTypeProperty + ".il", "Information Line");
      props.put(DisplayTypeProperty + ".l", "Label");

      props.put(AttributeProperty, "The Name of the Attribute to display.");
      props.put(AttributeProperty + LabelEditorProperty, "Attribute name");
      return props;
   }

   /**
    * Set the attributes chosen in the GUI on the OMGraphic.
    * 
    * @param omg the OMGraphic to set the attribute information on.
    * @param id The ID number of the map feature that the OMGraphic represents.
    */
   public void setTo(OMGraphic omg, int id) {
      super.setTo(omg);

      // now set the attributes on the OMGraphic based on the
      // GUI/property settings.
      if (fci != null) {
         String dt = getDisplayType();
         if (dt != null) {
            String tooltip = fci.getAttribute(id, getAttributeCol(), null);
            // Might want to to .equals here, test for speed effect.
            // if (dt.equals(OMGraphicConstants.LABEL)) {
            if (dt == OMGraphicConstants.LABEL) {
               OMTextLabeler omtl = new OMTextLabeler(tooltip);
               super.setTo(omtl);
               omg.putAttribute(dt, omtl);
            } else {
               omg.putAttribute(dt, tooltip);
            }
         }
      }
   }

   /**
    * Retrieve the column index number out of the feature class info file that
    * is being used/displayed.
    * 
    * @return column index of attribute information.
    */
   protected int getAttributeCol() {
      return attributeCol;
   }

   /**
    * Set the column index number in the feature class info file that will be
    * used/displayed.
    */
   protected void setAttributeCol(int col) {
      attributeCol = col;
   }

   /**
    * Return the GUI controls for this feature = the basic DrawingAttributes GUI
    * from the superclass, plus the other attribute display controls.
    */
   public Component getGUI() {
      if (guiPanel == null) {
         guiPanel = new JPanel();
         GridBagLayout gridbag = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         guiPanel.setLayout(gridbag);

         c.gridwidth = GridBagConstraints.REMAINDER;

         Component sgui = super.getGUI();
         gridbag.setConstraints(sgui, c);
         guiPanel.add(sgui);

         // Attribute GUI
         attributePanel = new JPanel();
         attributePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Attribute Control "));
         GridBagLayout gridbag2 = new GridBagLayout();
         GridBagConstraints c2 = new GridBagConstraints();
         attributePanel.setLayout(gridbag2);

         c2.gridwidth = GridBagConstraints.RELATIVE;
         c2.anchor = GridBagConstraints.WEST;

         JLabel label = new JLabel("Name: ");
         label.setToolTipText("Choose which attribute to display for each instance of this feature type.");
         gridbag2.setConstraints(label, c2);
         attributePanel.add(label);

         c2.gridwidth = GridBagConstraints.REMAINDER;

         attributeJCB = new JComboBox();
         attributeJCB.setToolTipText("Choose which attribute to display for each instance of this feature type.");
         attributeJCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               JComboBox jcb = (JComboBox) ae.getSource();
               FCIChoice fcic = (FCIChoice) jcb.getSelectedItem();
               if (fcic != null) {
                  setAttributeCol(fcic.getColumn());
                  setAttributeColName(fcic.getAttribute());
               }
            }
         });
         gridbag2.setConstraints(attributeJCB, c2);
         attributePanel.add(attributeJCB);

         c2.gridwidth = GridBagConstraints.RELATIVE;

         label = new JLabel("How: ");
         label.setToolTipText("Choose how to display the attribute.");
         gridbag2.setConstraints(label, c2);
         attributePanel.add(label);

         c2.gridwidth = GridBagConstraints.REMAINDER;

         DisplayTypeChoice[] dtc = new DisplayTypeChoice[] {
            new DisplayTypeChoice("None", null),
            new DisplayTypeChoice(OMGraphicConstants.TOOLTIP, OMGraphicConstants.TOOLTIP),
            new DisplayTypeChoice(OMGraphicConstants.INFOLINE, OMGraphicConstants.INFOLINE),
            new DisplayTypeChoice(OMGraphicConstants.LABEL, OMGraphicConstants.LABEL)
         };

         displayTypeJCB = new JComboBox(dtc);
         displayTypeJCB.setToolTipText("Choose how to display the attribute.");
         displayTypeJCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               JComboBox jcb = (JComboBox) ae.getSource();
               DisplayTypeChoice dtc = (DisplayTypeChoice) jcb.getSelectedItem();
               setDisplayType(dtc.getDisplayCommand());
            }
         });

         if (OMGraphicConstants.TOOLTIP.equalsIgnoreCase(displayType)) {
            displayTypeJCB.setSelectedIndex(1);
         } else if (OMGraphicConstants.INFOLINE.equalsIgnoreCase(displayType)) {
            displayTypeJCB.setSelectedIndex(2);
         } else if (OMGraphicConstants.LABEL.equalsIgnoreCase(displayType)) {
            displayTypeJCB.setSelectedIndex(3);
         }

         gridbag2.setConstraints(displayTypeJCB, c2);
         attributePanel.add(displayTypeJCB);

         updateAttributeGUI();
         // End attribute GUI

         gridbag.setConstraints(attributePanel, c);
         guiPanel.add(attributePanel);
      }
      return guiPanel;
   }

   /**
     * 
     */
   protected void updateAttributeGUI() {
      if (attributePanel != null && attributeJCB != null && displayTypeJCB != null) {

         if (fci != null) {
            attributeJCB.removeAllItems();

            DcwColumnInfo[] dci = fci.getColumnInfo();
            int colCount = dci.length;

            // StringBuffer sb = new StringBuffer();

            // Need to save current attributeColName, because
            // setting a new choice on an empty combo box will set
            // it to that first added attribute automatically.
            String cacn = attributeColName;
            for (int i = 0; i < colCount; i++) {
               FCIChoice fcic = new FCIChoice(dci[i].getColumnName(), dci[i].getColumnDescription(), i);
               attributeJCB.addItem(fcic);
               if (dci[i].getColumnName().equalsIgnoreCase(cacn)) {
                  attributeJCB.setSelectedItem(fcic);
               }
            }
            attributePanel.setVisible(true);
            attributeJCB.setEnabled(true);
            displayTypeJCB.setEnabled(true);
         } else {
            attributePanel.setVisible(false);
            attributeJCB.setEnabled(false);
            displayTypeJCB.setEnabled(false);
         }
      }
   }

   /**
    * @return Returns the displayType.
    */
   public String getDisplayType() {
      return displayType;
   }

   /**
    * @param displayType The displayType to set.
    */
   public void setDisplayType(String displayType) {
      this.displayType = displayType;

      // Check and update for not-null, so that equality check can be used
      // later
      if (displayType != null) {
         if (displayType.equalsIgnoreCase(OMGraphicConstants.TOOLTIP)) {
            displayType = OMGraphicConstants.TOOLTIP;
         } else if (displayType.equalsIgnoreCase(OMGraphicConstants.LABEL)) {
            displayType = OMGraphicConstants.LABEL;
         } else if (displayType.equalsIgnoreCase(OMGraphicConstants.INFOLINE)) {
            displayType = OMGraphicConstants.INFOLINE;
         }
      }
   }

   /**
    * @return Returns the fci.
    */
   public FeatureClassInfo getFci() {
      return fci;
   }

   /**
    * @param fci The fci to set.
    */
   public void setFci(FeatureClassInfo fci) {
      this.fci = fci;
      if (attributeColName != null) {
         int col = fci.whatColumn(attributeColName);
         if (col >= 0) {
            setAttributeCol(col);
         }
      }
      updateAttributeGUI();
   }

   /**
    * @return Returns the attributeColName.
    */
   public String getAttributeColName() {
      return attributeColName;
   }

   /**
    * @param attributeColName The attributeColName to set.
    */
   public void setAttributeColName(String attributeColName) {
      this.attributeColName = attributeColName;
   }

   public class DisplayTypeChoice {

      protected String displayName;
      protected String displayCommand;

      public DisplayTypeChoice(String dn, String dc) {
         displayName = dn;
         displayCommand = dc;
      }

      public String getDisplayCommand() {
         return displayCommand;
      }

      public String getDisplayName() {
         return displayName;
      }

      public String toString() {
         return getDisplayName();
      }

   }

   public class FCIChoice {
      protected String attribute;
      protected String description;
      protected int column;

      public FCIChoice(String att, String desc, int col) {
         attribute = att;
         description = desc;
         column = col;
      }

      public String getAttribute() {
         return attribute;
      }

      public String getDescription() {
         return description;
      }

      public int getColumn() {
         return column;
      }

      public String toString() {
         return getDescription() + " (" + getAttribute() + ")";
      }

   }

}