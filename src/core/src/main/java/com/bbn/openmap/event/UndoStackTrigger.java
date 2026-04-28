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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionStackTrigger.java,v $
// $RCSfile: ProjectionStackTrigger.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:23 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.event.ActionListener;

/**
 * Provides Stack input by firing UndoCmd and RedoCmd commands, which cause
 * updates to whatever component described by the UndoEvent.
 */
public interface UndoStackTrigger {

   /**
    * Add an ActionListener for events that trigger events to shift the
    * undo/redo stack.
    */
   public void addActionListener(ActionListener al);

   /**
    * Remove an ActionListener that receives events that trigger events to shift
    * the undo/redo stack.
    */
   public void removeActionListener(ActionListener al);

   /**
    * To receive a status to let the trigger know if any projections in the
    * forward or backward stacks exist, possibly to disable any gui widgets.
    * 
    * @param undoEvent there is at least one event to undo.
    * @param redoEvent there is at least one event to redo.
    */
   public void updateUndoStackStatus(UndoEvent undoEvent, UndoEvent redoEvent);
}
