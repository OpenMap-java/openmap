/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;

/** Custom property editor for a bean property that is itself a bean. */

public class PropertyBeanEditor
      extends PropertyEditorSupport {
   private GenericPropertySheet propSheet;

   /**
    * returns true;
    */
   public boolean isPaintable() {
      return true;
   }

   /**
    * represents the face of the custom property editor as a rectangular box
    * containing the text "Click to Edit".
    */
   public void paintValue(Graphics g, Rectangle box) {
      Color oldColor = g.getColor();
      Font oldFont = g.getFont();
      g.setColor(Color.blue);
      g.setFont(new Font(oldFont.getFontName(), Font.BOLD, oldFont.getSize()));
      g.drawRect(box.x - 1, box.y + 1, box.width - 2, box.height - 2);
      g.setColor(Color.white);
      g.drawString("Click to Edit", box.x + 10, box.y + box.height / 2 + 4);
      g.setFont(oldFont);
      g.setColor(oldColor);
   }

   /**
    * returns true.
    */
   public boolean supportsCustomEditor() {
      return true;
   }

   /**
    * returns an instance of the
    * {@link com.bbn.openmap.tools.beanbox.GenericPropertySheet} which serves as
    * the custom editor component for the bean property associated with this
    * editor.
    */
   public Component getCustomEditor() {
      if (propSheet == null)
         propSheet = new GenericPropertySheet(getValue(), 575, 20, this, null);
      return propSheet;
   }
}
