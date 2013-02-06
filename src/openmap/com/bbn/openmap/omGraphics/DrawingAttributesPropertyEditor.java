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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/DrawingAttributesPropertyEditor.java,v $
// $RCSfile: DrawingAttributesPropertyEditor.java,v $
// $Revision: 1.2 $
// $Date: 2006/12/15 18:39:54 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.util.Properties;

import javax.swing.JPanel;

import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.PropertyConsumerPropertyEditor;

/**
 * A PropertyEditor for a set of DrawingAttributes properties, providing the
 * standard DrawingAttributes GUI for property configuration.
 */
public class DrawingAttributesPropertyEditor
      extends PropertyConsumerPropertyEditor {

   protected DrawingAttributes drawingAttributes;

   public DrawingAttributesPropertyEditor() {
   }

   public boolean supportsCustomEditor() {
      return true;
   }

   public void setCustomEditor(Component comp) {
   }

   /** Returns the editor GUI. */
   public Component getCustomEditor() {
      JPanel panel = new JPanel();
      GridBagLayout layout = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();

      c.anchor = GridBagConstraints.WEST;

      panel.setLayout(layout);
      Component comp = getDrawingAttributes().getGUI();
      if (comp != null) {
         layout.setConstraints(comp, c);
         panel.add(comp);
      }

      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0f;
      JPanel filler = new JPanel();
      layout.setConstraints(filler, c);
      panel.add(filler);

      return panel;
   }

   public DrawingAttributes getDrawingAttributes() {
      if (drawingAttributes == null) {
         drawingAttributes = DrawingAttributes.getDefaultClone();
      }
      return drawingAttributes;
   }

   public void setDrawingAttributes(DrawingAttributes drawingAttributes) {
      this.drawingAttributes = drawingAttributes;
   }

   public void focusGained(FocusEvent e) {
   }

   public void focusLost(FocusEvent e) {
      firePropertyChange();
   }

   public String getAsText() {
      return "";
   }

   /**
    * @param prefix the token to prefix the property names
    * @param props the <code>Properties</code> object
    */
   public void setProperties(String prefix, Properties props) {
      prefix = PropUtils.decodeDummyMarkerFromPropertyInfo(prefix);
      getDrawingAttributes().setProperties(prefix, props);
   }

   public Properties getProperties(Properties props) {
      if (drawingAttributes != null) {
         return drawingAttributes.getProperties(props);
      } else {
         return props;
      }
   }

}