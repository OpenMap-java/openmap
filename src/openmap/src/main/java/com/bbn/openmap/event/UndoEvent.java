/* 
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.event;

/**
 * An UndoEvent is an object that contains everything needed to reset its source
 * back to a specific state. Used by the UndoStack. This event is used by the
 * UndoStack to tell other objects to undo/redo - go to a certain state. The
 * events that tell the UndoStack to make these notifications are regular
 * ActionEvents, not these.
 * 
 * @author ddietrick
 */
public interface UndoEvent {

   /**
    * @return a short description of what will happen when setState() is
    *         invoked.
    */
   String getDescription();

   /**
    * Tell the UndoEvent to set its source object back to the state described by
    * this object.
    */
   void setState();

}
