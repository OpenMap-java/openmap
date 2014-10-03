/* 
 */
package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.event.UndoEvent;
import com.bbn.openmap.event.UndoStack;
import com.bbn.openmap.event.UndoStackTrigger;

/**
 * UndoMenuItemStackTrigger holds a couple of menu items that can trigger an UndoStack.
 *  
 * @author dietrick
 */
public class UndoMenuItemStackTrigger
      implements UndoStackTrigger, ActionListener {
   JMenuItem undoMI;
   JMenuItem redoMI;

   protected final List<ActionListener> listeners;

   protected String nothingToUndoString;
   protected String nothingToRedoString;
   protected String undoString;
   protected String redoString;

   public UndoMenuItemStackTrigger() {
      listeners = Collections.synchronizedList(new ArrayList<ActionListener>());

      I18n i18n = Environment.getI18n();

      nothingToUndoString = i18n.get(this.getClass(), "nothingToUndoString", "Nothing to Undo");
      nothingToRedoString = i18n.get(this.getClass(), "nothingToRedoString", "Nothing to Redo");

      undoString = i18n.get(this.getClass(), "undoString", "Undo ");
      redoString = i18n.get(this.getClass(), "redoString", "Redo ");

      undoMI = new JMenuItem(nothingToUndoString);
      undoMI.setActionCommand(UndoStack.UndoCmd);
      redoMI = new JMenuItem(nothingToRedoString);
      redoMI.setActionCommand(UndoStack.RedoCmd);

      undoMI.addActionListener(this);
      redoMI.addActionListener(this);

      // Should be disabled until an event is received from the undo stack.
      undoMI.setEnabled(false);
      redoMI.setEnabled(false);
   }

   /*
    * Adding an action listener (UndoStack) to listen for undo and redo
    * requests.
    * 
    * @see com.bbn.openmap.event.UndoStackTrigger#addActionListener(java.awt
    * .event.ActionListener)
    */
   public void addActionListener(ActionListener al) {
      if (!listeners.contains(al)) {
         listeners.add(al);
      }
   }

   /*
    * Removing action listener that was listening for undo and redo requests.
    * 
    * @see com.bbn.openmap.event.UndoStackTrigger#removeActionListener(java.
    * awt.event.ActionListener)
    */
   public void removeActionListener(ActionListener al) {
      listeners.remove(al);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.bbn.openmap.event.UndoStackTrigger#updateUndoStackStatus(boolean,
    * boolean)
    */
   public void updateUndoStackStatus(UndoEvent undoEvent, UndoEvent redoEvent) {
      undoMI.setEnabled(undoEvent != null);
      redoMI.setEnabled(redoEvent != null);

      if (undoEvent != null) {
         undoMI.setText(undoString + undoEvent.getDescription());
      } else {
         undoMI.setText(nothingToUndoString);
      }

      if (redoEvent != null) {
         redoMI.setText(redoString + redoEvent.getDescription());
      } else {
         redoMI.setText(nothingToRedoString);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e) {
      List<ActionListener> listClone = new ArrayList<ActionListener>();
      listClone.addAll(listeners);

      for (ActionListener al : listClone) {
         al.actionPerformed(e);
      }
   }

   /**
    * @return the undoMI
    */
   public JMenuItem getUndoMenuItem() {
      return undoMI;
   }

   /**
    * @return the redoMI
    */
   public JMenuItem getRedoMenuItem() {
      return redoMI;
   }

}
