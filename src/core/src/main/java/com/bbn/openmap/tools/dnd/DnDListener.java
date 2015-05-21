//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/dnd/DnDListener.java,v $
//$RCSfile: DnDListener.java,v $
//$Revision: 1.2 $
//$Date: 2004/10/14 18:06:25 $
//$Author: dietrick $
//
//**********************************************************************
package com.bbn.openmap.tools.dnd;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.util.Hashtable;

import com.bbn.openmap.util.Debug;

/**
 * DnDListener acts as the base DnD and MouseListener class. Some of
 * its methods should be overriden by extending classes.
 * 
 * @see DefaultDnDCatcher
 */
public class DnDListener extends MouseDragGestureRecognizer implements
        DragSourceListener, DropTargetListener {

    // startDrag is a boolean flag. When set to true it enables
    // dragging.
    protected boolean startDrag = true;

    // Specifies the default DnD action.
    protected int default_action = DnDConstants.ACTION_MOVE;

    // A hashtable of custom cursors.
    private Hashtable cursors = new Hashtable();

    /**
     * Constructs a new DnDListener given the DragSource for the
     * Component.
     * 
     * @param ds the DragSource for the Component
     */
    protected DnDListener(DragSource ds) {
        this(ds, null);
    }

    /**
     * Construct a new DnDListener given the DragSource for the
     * Component c, and the Component to observe.
     * 
     * @param ds the DragSource for the Component c
     * @param c the Component to observe
     */
    protected DnDListener(DragSource ds, Component c) {
        this(ds, c, DnDConstants.ACTION_NONE);
    }

    protected DnDListener(DragSource ds, Component c, int act) {
        this(ds, c, act, null);
    }

    protected DnDListener(DragSource ds, Component c, int act,
            DragGestureListener dgl) {
        super(ds, c, act, dgl);
    }

    /**
     * This method is invoked to signify that the Drag and Drop
     * operation is complete. The getDropSuccess() method of the
     * <code>DragSourceDropEvent</code> can be used to determine the
     * termination state. The getDropAction() method returns the
     * operation that the <code>DropTarget</code> selected (via the
     * DropTargetDropEvent acceptDrop() parameter) to apply to the
     * Drop operation. Once this method is complete, the current
     * <code>DragSourceContext</code> and associated resources
     * become invalid.
     * <P>
     * 
     * @param dsde the <code>DragSourceDropEvent</code>
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        Debug.message("dndlistener", "dragDropEnd(source)");
        // set the startDrag flag to true to enable dragging.
        startDrag = true;
    }

    /**
     * Called as the hotspot enters a platform dependent drop site.
     * This method is invoked when the following conditions are true:
     * <UL>
     * <LI>The logical cursor's hotspot initially intersects a GUI
     * <code>Component</code>'s visible geometry.
     * <LI>That <code>Component</code> has an active
     * <code>DropTarget</code> associated with it.
     * <LI>The <code>DropTarget</code>'s registered
     * <code>DropTargetListener</code> dragEnter() method is invoked
     * and returns successfully.
     * <LI>The registered <code>DropTargetListener</code> invokes
     * the <code>DropTargetDragEvent</code>'s acceptDrag() method
     * to accept the drag based upon interrogation of the source's
     * potential drop action(s) and available data types (
     * <code>DataFlavor</code> s).
     * </UL>
     * <P>
     * 
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragEnter(DragSourceDragEvent dsde) {
        int action = dsde.getDropAction();
        Debug.message("dndlistener", "dragEnter (source)");
        Debug.message("dndlistener", "action=" + action);
        if (action == default_action) {
            dsde.getDragSourceContext()
                    .setCursor(getCursor(DragSource.DefaultMoveDrop));
        } else {
            dsde.getDragSourceContext()
                    .setCursor(getCursor(DragSource.DefaultMoveNoDrop));
        }

    }

    /**
     * Called when a drag operation has encountered the
     * <code>DropTarget</code>.
     * <P>
     * 
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dragEnter(DropTargetDragEvent dtde) {
        Debug.message("dndlistener", "dragEnter (target)");
        int action = dtde.getDropAction();
        Debug.message("dndlistener", "action=" + action);
        dtde.acceptDrag(action);
    }

    /**
     * Called as the hotspot exits a platform dependent drop site.
     * This method is invoked when the following conditions are true:
     * <UL>
     * <LI>The cursor's logical hotspot no longer intersects the
     * visible geometry of the <code>Component</code> associated
     * with the previous dragEnter() invocation.
     * </UL>
     * OR
     * <UL>
     * <LI>The <code>Component</code> that the logical cursor's
     * hotspot intersected that resulted in the previous dragEnter()
     * invocation no longer has an active <code>DropTarget</code> or
     * <code>DropTargetListener</code> associated with it.
     * </UL>
     * OR
     * <UL>
     * <LI>The current <code>DropTarget</code>'s
     * <code>DropTargetListener</code> has invoked rejectDrag()
     * since the last dragEnter() or dragOver() invocation.
     * </UL>
     * <P>
     * 
     * @param dse the <code>DragSourceEvent</code>
     */
    public void dragExit(DragSourceEvent dse) {
        Debug.message("dndlistener", "dragExit (source)");
        dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    /**
     * The drag operation has departed the <code>DropTarget</code>
     * without dropping.
     * <P>
     * 
     * @param dte the <code>DropTargetEvent</code>
     */
    public void dragExit(DropTargetEvent dte) {
        Debug.message("dndlistener", "dragExit (target)");
    }

    /**
     * Called as the hotspot moves over a platform dependent drop
     * site. This method is invoked when the following conditions are
     * true:
     * <UL>
     * <LI>The cursor's logical hotspot has moved but still
     * intersects the visible geometry of the <code>Component</code>
     * associated with the previous dragEnter() invocation.
     * <LI>That <code>Component</code> still has a
     * <code>DropTarget</code> associated with it.
     * <LI>That <code>DropTarget</code> is still active.
     * <LI>The <code>DropTarget</code>'s registered
     * <code>DropTargetListener</code> dragOver() method is invoked
     * and returns successfully.
     * <LI>The <code>DropTarget</code> does not reject the drag via
     * rejectDrag()
     * </UL>
     * <P>
     * 
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragOver(DragSourceDragEvent dsde) {
        Debug.message("dndlistener", "dragOver(source)");
        int action = dsde.getDropAction();
        if (action == default_action) {
            dsde.getDragSourceContext()
                    .setCursor(getCursor(DragSource.DefaultMoveDrop));
        } else {
            dsde.getDragSourceContext()
                    .setCursor(getCursor(DragSource.DefaultMoveNoDrop));
        }
    }

    /**
     * Called when a drag operation is ongoing on the
     * <code>DropTarget</code>.
     * <P>
     * 
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dragOver(DropTargetDragEvent dtde) {
        Debug.message("dndlistener", "dragOver(target)");
        dtde.acceptDrag(dtde.getDropAction());
    }

    /**
     * The drag operation has terminated with a drop on this
     * <code>DropTarget</code>. This method is responsible for
     * undertaking the transfer of the data associated with the
     * gesture. The <code>DropTargetDropEvent</code> provides a
     * means to obtain a <code>Transferable</code> object that
     * represents the data object(s) to be transfered.
     * <P>
     * From this method, the <code>DropTargetListener</code> shall
     * accept or reject the drop via the acceptDrop(int dropAction) or
     * rejectDrop() methods of the <code>DropTargetDropEvent</code>
     * parameter.
     * <P>
     * Subsequent to acceptDrop(), but not before,
     * <code>DropTargetDropEvent</code>'s getTransferable() method
     * may be invoked, and data transfer may be performed via the
     * returned <code>Transferable</code>'s getTransferData()
     * method.
     * <P>
     * At the completion of a drop, an implementation of this method
     * is required to signal the success/failure of the drop by
     * passing an appropriate <code>boolean</code> to the
     * <code>DropTargetDropEvent</code>'s dropComplete(boolean
     * success) method.
     * <P>
     * Note: The actual processing of the data transfer is not
     * required to finish before this method returns. It may be
     * deferred until later.
     * <P>
     * 
     * @param dtde the <code>DropTargetDropEvent</code>
     */
    public void drop(DropTargetDropEvent dtde) {}

    /**
     * Called when the user has modified the drop gesture. This method
     * is invoked when the state of the input device(s) that the user
     * is interacting with changes. Such devices are typically the
     * mouse buttons or keyboard modifiers that the user is
     * interacting with.
     * <P>
     * 
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {
        Debug.message("dndlistener", "dropActionChanged(source)");
        int action = dsde.getDropAction();
        Debug.message("dndlistener", "action=" + action);
        if (action == default_action) {
            dsde.getDragSourceContext()
                    .setCursor(getCursor(DragSource.DefaultMoveDrop));
        } else {
            dsde.getDragSourceContext()
                    .setCursor(getCursor(DragSource.DefaultMoveNoDrop));
        }
    }

    /**
     * Called if the user has modified the current drop gesture.
     * <P>
     * 
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
        Debug.message("dndlistener", "dropActionChanged(target)");
        int action = dtde.getDropAction();
        Debug.message("dndlistener", "action=" + action);
        dtde.acceptDrag(action);
    }

    /**
     * Get the Cursor object associated with the default_cursor. If
     * not found, return the passed cursor.
     */

    public Cursor getCursor(Cursor default_cursor) {
        Cursor cursor = (Cursor) cursors.get(default_cursor);
        return (cursor != null) ? cursor : default_cursor;
    }

    /**
     * Returns the default DnD action.
     * 
     * @return int
     */
    public int getDefaultAction() {
        return default_action;
    }

    /**
     * Sets a custom Cursor object associated with the default_cursor.
     */

    public void setCursor(Image img, Cursor default_cursor) {
        Point offset = new Point(0, 0);
        Cursor customCursor = Toolkit.getDefaultToolkit()
                .createCustomCursor(img, offset, "");
        cursors.put(default_cursor, customCursor);
    }

    /**
     * Sets the default DnD action.
     * 
     * @param newAction int
     */
    public void setDefaultAction(int newAction) {
        default_action = newAction;
    }
}