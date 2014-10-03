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

import java.awt.Component;

/**
 * Custom property editor for the
 * {@link com.bbn.openmap.tools.beanbox.BeanLayoutManager}property.
 */
public class BeanLayoutEditor
      extends PropertyBeanEditor {
   private GenericPropertySheet propSheet;

   /**
    * returns an instance of the
    * {@link com.bbn.openmap.tools.beanbox.GenericPropertySheet} which serves as
    * the custom editor component for the BeanLayoutManager property.
    */
   public Component getCustomEditor() {
      Object value = getValue();
      if (propSheet == null)
         propSheet = new GenericPropertySheet(value, 575, 20, this, null);
      else
         propSheet.setTarget(value);
      return propSheet;
   }
}
