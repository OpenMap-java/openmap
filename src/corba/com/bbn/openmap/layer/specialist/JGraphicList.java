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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JGraphicList.java,v $
// $RCSfile: JGraphicList.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

/** class JGraphic */
public class JGraphicList
      extends OMGraphicList {

   /**
    * Construct an OMGraphicList.
    */
   public JGraphicList() {
      super();
   }

   /**
    * Construct an OMGraphicList with an initial capacity.
    * 
    * @param initialCapacity the initial capacity of the list
    */
   public JGraphicList(int initialCapacity) {
      super(initialCapacity);
   }

   /**
    * Construct an OMGraphicList with an initial capacity and a standard
    * increment value.
    * 
    * @param initialCapacity the initial capacity of the list
    * @param capacityIncrement the capacityIncrement for resizing
    */
   public JGraphicList(int initialCapacity, int capacityIncrement) {
      super(initialCapacity);
   }

   public OMGraphic getOMGraphicWithId(String gID) {
      java.util.Iterator targets = iterator();
      while (targets.hasNext()) {
         OMGraphic graphic = (OMGraphic) targets.next();
         if (graphic instanceof JObjectHolder) {
            com.bbn.openmap.corba.CSpecialist.EComp ecomp = ((JObjectHolder) graphic).getObject();
            if (ecomp.cID.equals(gID)) {
               return graphic;
            }
         }
      }
      return null;
   }

}