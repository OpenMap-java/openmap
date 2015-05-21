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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/StandardMapMouseInterpreter.java,v $
// $RCSfile: StandardMapMouseInterpreter.java,v $
// $Revision: 1.18 $
// $Date: 2007/10/01 21:43:38 $
// $Author: epgordon $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.Debug;

/**
 * The DBLClickSelectMapMouseInterpreter is an extension of the
 * StandardMapMouseInterpreter that limits selection to map objects that have
 * been double clicked on, instead of the single click used by default in the
 * super class.
 */
public class DBLClickSelectMapMouseInterpreter
      extends StandardMapMouseInterpreter {

   /**
    * The OMGraphicLayer should be set at some point before use.
    */
   public DBLClickSelectMapMouseInterpreter() {
      DEBUG = Debug.debugging("grp");
   }

   /**
    * The standard constructor.
    */
   public DBLClickSelectMapMouseInterpreter(OMGraphicHandlerLayer l) {
      this();
      setLayer(l);
   }

   // Mouse Listener events
   // //////////////////////

   /**
    * Invoked when a mouse button has been pressed on a component.
    * 
    * @param e MouseEvent
    * @return false if nothing was pressed over, or the consumeEvents setting if
    *         something was.
    */
   public boolean mousePressed(MouseEvent e) {
      if (DEBUG) {
         Debug.output("SMMI: mousePressed()");
      }
      setCurrentMouseEvent(e);
      boolean ret = false;

      GeometryOfInterest goi = getClickInterest();
      OMGraphic omg = getGeometryUnder(e);

      if (goi != null && !goi.appliesTo(omg, e)) {
         // If the click doesn't match the geometry or button
         // of the geometry of interest, need to tell the goi
         // that is was clicked off, and set goi to null.
         if (goi.isLeftButton()) {
            leftClickOff(goi.getGeometry(), e);
         } else {
            rightClickOff(goi.getGeometry(), e);
         }
         setClickInterest(null);
      }

      if (omg != null) {
         setClickInterest(new GeometryOfInterest(omg, e));
      }

      ret = testForAndHandlePopupTrigger(e);

      // if (omg != null && !ret) {
      // select(omg);
      // ret = true;
      // }

      return ret && consumeEvents;
   }

   // Mouse Motion Listener events
   // /////////////////////////////

   /**
    * Handle a left-click on an OMGraphic. Does nothing by default.
    * 
    * @return true
    */
   public boolean leftClick(OMGraphic omg, MouseEvent e) {
      boolean ret = false;

      if (omg != null && !ret && e.getClickCount() > 1) {
         select(omg);
         ret = true;
      }

      return ret && consumeEvents;
   }

}