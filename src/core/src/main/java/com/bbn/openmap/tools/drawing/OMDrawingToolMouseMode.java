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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMDrawingToolMouseMode.java,v $
// $Revision: 1.4 $ $Date: 2004/10/14 18:06:26 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import com.bbn.openmap.event.CoordMouseMode;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.util.Debug;

/**
 * The OMDrawingToolMouseMode is the MapMouseMode that handles the events for
 * the OMDrawingTool.
 */
public class OMDrawingToolMouseMode
      extends CoordMouseMode {

   private static final long serialVersionUID = 1L;

   /**
    * Mouse Mode identifier, which is "Drawing Tool".
    */
   public final static transient String modeID = "Drawing";

   protected OMDrawingTool drawingTool = null;

   /**
    * Construct an OMDrawingToolMouseMode. Sets the ID of the mode to the
    * modeID, the consume mode to true.
    */
   public OMDrawingToolMouseMode() {
      super(modeID, true);
   }

   /**
    * Construct a OMDrawingToolMouseMode. Lets you set the consume mode. If the
    * events are consumed, then a MouseEvent is sent only to the first
    * MapMouseListener that successfully processes the event. If they are not
    * consumed, then all of the listeners get a chance to act on the event.
    * 
    * @param omdt the drawing tool for this mousemode
    */
   public OMDrawingToolMouseMode(OMDrawingTool omdt) {
      this();
      drawingTool = omdt;
      setModeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
   }

   /**
    * Set the DrawingTool for the mouse mode. Expected to be self-called by the
    * mouse mode using the BeanContext.
    */
   protected void setDrawingTool(OMDrawingTool omdt) {
      drawingTool = omdt;
   }

   /**
    * Get the drawing tool for the mouse mode.
    */
   protected OMDrawingTool getDrawingTool() {
      return drawingTool;
   }

   /**
    * Get the EditableOMGraphic from the OMDrawingTool. Returns null if anything
    * isn't set up correctly.
    */
   protected EditableOMGraphic getCurrentGraphic() {
      if (drawingTool == null) {
         return null;
      }
      return drawingTool.getCurrentEditable();
   }

   // MouseListener and MouseMotionListener interface methods

   /**
     *  
     */
   public void mousePressed(MouseEvent e) {
      Debug.message("drawingtooldetail", "DrawingTool.mousePressed");
      EditableOMGraphic graphic = getCurrentGraphic();
      if (graphic != null) {
         graphic.mousePressed(e);
      }
      fireMouseLocation(e);
   }

   /**
     *  
     */
   public void mouseReleased(MouseEvent e) {
      Debug.message("drawingtooldetail", "DrawingTool.mousePressed");
      EditableOMGraphic graphic = getCurrentGraphic();
      if (graphic != null) {
         graphic.mouseReleased(e);
      }
      fireMouseLocation(e);
      if (drawingTool != null) {
         if (drawingTool.isMask(OMDrawingTool.DEACTIVATE_ASAP_BEHAVIOR_MASK)) {
            drawingTool.deactivate();
         }
      }
   }

   /**
     *  
     */
   public void mouseClicked(MouseEvent e) {
      Debug.message("drawingtooldetail", "DrawingTool.mouseClicked");
      EditableOMGraphic graphic = getCurrentGraphic();
      if (graphic != null) {
         graphic.mouseClicked(e);
      }
   }

   /**
     *  
     */
   public void mouseEntered(MouseEvent e) {
      Debug.message("drawingtooldetail", "DrawingTool.mouseEntered");
      EditableOMGraphic graphic = getCurrentGraphic();
      if (graphic != null) {
         graphic.mouseEntered(e);
      }
   }

   /**
     *  
     */
   public void mouseExited(MouseEvent e) {
      Debug.message("drawingtooldetail", "DrawingTool.mouseExited");
      EditableOMGraphic graphic = getCurrentGraphic();
      if (graphic != null) {
         graphic.mouseExited(e);
      }
   }

   /**
     *  
     */
   public void mouseDragged(MouseEvent e) {
      Debug.message("drawingtooldetail", "DrawingTool.mouseDragged");
      EditableOMGraphic graphic = getCurrentGraphic();
      if (graphic != null) {
         graphic.mouseDragged(e);
      }
      fireMouseLocation(e);
   }

   /**
     *  
     */
   public void mouseMoved(MouseEvent e) {
      Debug.message("drawingtooldetail", "DrawingTool.mouseMoved");
      EditableOMGraphic graphic = getCurrentGraphic();
      if (graphic != null) {
         graphic.mouseMoved(e);
      }
      fireMouseLocation(e);
   }

}