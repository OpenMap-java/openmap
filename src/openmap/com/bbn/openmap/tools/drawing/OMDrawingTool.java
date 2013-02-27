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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMDrawingTool.java,v $
// $RCSfile: OMDrawingTool.java,v $
// $Revision: 1.33 $
// $Date: 2007/12/03 23:47:37 $
// $Author: dietrick $
//
// **********************************************************************
package com.bbn.openmap.tools.drawing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.PaintListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.UndoStack;
import com.bbn.openmap.gui.OMToolComponent;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.gui.menu.UndoMenuItemStackTrigger;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMGraphicList;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.SinkGraphic;
import com.bbn.openmap.omGraphics.editable.GraphicSelectedState;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.omGraphics.event.EOMGListener;
import com.bbn.openmap.omGraphics.event.SelectionListener;
import com.bbn.openmap.omGraphics.event.SelectionProvider;
import com.bbn.openmap.omGraphics.event.SelectionSupport;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The OMDrawingTool implements the DrawingTool interface, and can be used to
 * adjust the drawing parameters of OMGraphics. It is basically a manager for
 * directing MouseEvents and MouseMotionEvents from a Component to a
 * EditableOMGraphic. The EditableOMGraphic is responsible for interpreting the
 * events and making the adjustments to the OMGraphic it has wrapped within. The
 * OMDrawingTool also tries to keep the presentation of the OMGraphic up to
 * date, by managing the repaints of the Component to include the graphic being
 * modified.
 * <P>
 * 
 * The OMDrawingTool is also a com.bbn.openmap.gui.Tool, which allows it to
 * appear in the OpenMap toolbar. The OMDrawingTool keeps track of whether it is
 * a tool, and appears accordingly:
 * <P>
 * 
 * If the OMDrawingTool is being used as a tool (getUseAsTool() == true), then
 * it will set itself to be visible. If you are putting an OMDrawingTool in the
 * OpenMap application and you want the color/line/graphic options to be visible
 * in the toolbar, use the itTool property for the OMDrawingTool in the
 * properties file. If you are using your own OMDrawingTool, in your
 * EditorLayerTool for instance, you should set useAsTool(true) programmatically
 * to get the visibility of the tool to appear. There is a property to tell the
 * OMDrawingTool to be visible when it is inactive, and that flag is true by
 * default. You can set that property (visibleWhenInactive) to change this
 * behavior.
 * 
 * If the OMDrawingTool is not being used as a tool, it can be brought up in a
 * window. This window can be brought up with a right click or control-click on
 * the object being edited.
 * <P>
 * 
 * If the OMGraphic being edited doesn't want to have the OMDrawingTool visible,
 * it won't be. Neither the tool nor the option to bring the window up won't be
 * displayed with a right/control click.
 * <P>
 * 
 * The OMDrawingTool uses a behavior mask to give control over how it behaves.
 * You can control if the attribute palette appears, if a popup gui appears by
 * default when the editing is complete, or appear when the alt+mouse key or
 * right mouse key is pressed. You should set this mask if you are not sure
 * about the values that other components may have set on the OMDrawingTool.
 * <P>
 * 
 * The OMDrawingTool uses EditToolLoaders to determine what EditableOMGraphic
 * can be used for a particular class name or OMGraphic type. If a loader for an
 * OMGraphic type is not found, then that OMGraphic type won't be handled, and
 * the tool will react to a create() or edit() call with a null object pointer.
 * If a loader is found, and the OMgraphic can be edited or modified, then the
 * create() or edit() methods will return a pointer to the OMGraphic being
 * modified.
 * <P>
 * 
 * The GUI for the OMDrawingTool is multi-layered. The OMDrawingTool contains a
 * GraphicsAttributes object, which is an extension of the GraphicAttributes
 * object. The GraphicAttributes GUI within the tool lets you change the colors,
 * line width and line dash pattern of the current OMGraphic. The
 * GraphicAttributes contribution to the GUI is not yet implemented, but will
 * let you change the render type and line type of the OMGraphic. Finally, the
 * EditableOMGraphic is given an opportunity to change and set parameters of the
 * OMGraphic that is knows about - for instance, the EditableOMLine object will
 * soon provide an interface to set arrowheads on the lines, as well as set the
 * amount of arc a line has (it's currently not implemented).
 * <P>
 */
public class OMDrawingTool extends OMToolComponent implements DrawingTool, Serializable,
        PropertyChangeListener, ProjectionListener, EOMGListener, PaintListener, SelectionProvider {

    /**
     * A GraphicAttributes object that describes the current coloring parameters
     * for the current graphic.
     */
    protected GraphicAttributes graphicAttributes = GraphicAttributes.getGADefaultClone();
    /**
     * The current graphic being modified.
     */
    protected EditableOMGraphic currentEditable;
    /**
     * The MouseDelegator to use to get mouse events directed to the
     * DrawingTool.
     */
    protected MouseDelegator mouseDelegator;
    /**
     * A placeholder for the last mouse mode active before the drawing tool took
     * over.
     */
    protected MapMouseMode formerMouseMode = null;
    /**
     * The JComponent the drawing tool is servicing, usually the MapBean.
     */
    protected JComponent canvas;
    /**
     * The objects that know how to create a EditableOMGraphic for a particular
     * class name or OMGraphic.
     */
    protected Hashtable loaders = new Hashtable();
    /**
     * The ordered list of EditToolLoaders, for notification. Preservers order,
     * no duplicates.
     */
    protected Vector rawLoaders = new Vector();
    /**
     * The MouseMode used for the drawing tool.
     */
    protected OMDrawingToolMouseMode dtmm;
    /**
     * Flag to allow drawing tool to sense when an OMGraphic is clicked off and
     * deactivate. True by default.
     */
    protected boolean allowDrawingToolToDeactivateItself = true;
    /**
     * The component to notify when the drawing tool is finished.
     */
    protected DrawingToolRequestor requestor = null;
    /**
     * The current projection.
     */
    protected Projection projection = null;
    /**
     * A support object to handle telling listeners that the drawing tool is in
     * the process of editing an object, hence making it selected.
     */
    protected SelectionSupport selectionSupport = null;
    /**
     * The stack for keeping track of edits and allowing them to be reverted.
     */
    protected UndoStack undoStack = null;
    protected UndoMenuItemStackTrigger undoTrigger = null;
    /**
     * A behavior mask to show the GUI for the OMDrawingTool. Since the
     * OMDrawingTool is a com.bbn.openmap.gui.Tool object, it will only appear
     * on the tool panel if it has been added to it, and if it is being used as
     * a tool.
     */
    public final static int SHOW_GUI_BEHAVIOR_MASK = 1 << 0; // + 1
    /**
     * A behavior mask to add a menu item to the popup that will allow the GUI
     * to appear. If the OMDrawingTool is not being used as a tool and this is
     * set along with USE_POPUP_BEHAVIOR_MASK or ALT_POPUP_BEHAVIOR_MASK, then
     * the OMDrawingTool will appear in a window when the <B>Change Appearance
     * </B> option is selected in the popup menu.
     */
    public final static int GUI_VIA_POPUP_BEHAVIOR_MASK = 1 << 1; // + 2
    /**
     * Flag to tell the OMDrawingTool to display a popup when
     * gesturing/modifications appear to be over. Was the default action of the
     * tool, but was moved to only happening when the ctrl key or right mouse
     * button is pressed. You can force the old behavior by setting this.
     */
    public final static int USE_POPUP_BEHAVIOR_MASK = 1 << 2; // + 4
    /**
     * Allow a GUI popup to appear over the map when the gesturing/modifications
     * appear to be over, and when the ctrl key or right mouse button is
     * pressed.
     */
    public final static int ALT_POPUP_BEHAVIOR_MASK = 1 << 3; // + 8
    /**
     * Set the flag for the behavior that will tell the OMDrawingTool to *NOT*
     * add the OMDrawingToolMouseMode to the MouseDelegator as the active mouse
     * mode when activated. Should be called before create/edit is called, and
     * then you have to make sure that you provide MouseEvents to the
     * OMDrawingToolMouseMode or EditableOMGraphic in order to modify the
     * OMGraphic. Don't call this if you have already started using the tool,
     * the tool won't do anything if anything else is currently being modified.
     */
    public final static int PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK = 1 << 4; // + 16
    /**
     * This behavior is used internally, when the OMDrawingTool should be told
     * to clean up as soon as it is safe.
     */
    public final static int DEACTIVATE_ASAP_BEHAVIOR_MASK = 1 << 5; // + 32
    /**
     * A convenience value that tells the OMDrawingTool to show the GUI if it is
     * a tool, or to only display the popup with the ctrl key or right mouse
     * button if it isn't. A combination of SHOW_GUI, GUI_VIA_POPUP and
     * ALT_POPUP.
     */
    public final static int DEFAULT_BEHAVIOR_MASK = 11;
    /**
     * A convenience value that tells the OMDrawingTool to not show the GUI, but
     * show the popup with the alt key, and the popup has the ability to delete
     * the OMGraphic. A combination of GUI_VIA_POPUP and ALT_POPUP.
     */
    public final static int QUICK_CHANGE_BEHAVIOR_MASK = 10;
    /**
     * A integer that is looked at, bitwise, to determine different behaviors.
     */
    protected int behaviorMask = DEFAULT_BEHAVIOR_MASK;
    /**
     * Used for property change notifications.
     */
    public final static String LoadersProperty = "OMDrawingTool.loaders";
    /**
     * Debug flag turned on when <B>drawingtool </B> debug flag enabled.
     */
    protected boolean DEBUG = false;
    /**
     * A handle to the InformationDelegator to use for status messages.
     */
    protected InformationDelegator informationDelegator = null;
    /**
     * A Vector of Classes that can be handled by the OMDrawingTool. Constructed
     * the first time canEdit() is called after an EditToolLoader is added or
     * removed.
     */
    protected Vector possibleEditableClasses = null;
    /**
     * Just a helper flag to reduce work caused by unnecessary deactivate calls.
     * Set internally in activate() and deactivate().
     */
    protected boolean activated = false;
    /**
     * Tell the drawing tool to be invisible when it is inactive. True by
     * default.
     */
    protected boolean visibleWhenInactive = true;
    /**
     * The property, visibleWhenIactive, to set to false if you want that
     * behavior.
     */
    public final static String VisibleWhenInactiveProperty = "visibleWhenInactive";
    /**
     * The property list defining behavior mask values that should be set.
     */
    public final static String BehaviorProperty = "behavior";
    /**
     * Flag to tell tool to reset the GUI when it is deactivated. The only time
     * you would want this to be false (true is default) is when you are
     * creating many objects of the same type, and don't want the gui to keep
     * going back and forth between the default and special settings. Usually
     * set to in the drawingComplete method of an EditorTool. Reset to true when
     * showPalette is called.
     */
    protected boolean resetGUIWhenDeactivated = true;

    /**
     * Create a OpenMap Drawing Tool.
     */
    public OMDrawingTool() {
        super();
        setBorder(BorderFactory.createEmptyBorder());
        DEBUG = Debug.debugging("drawingtool");
        selectionSupport = new SelectionSupport(this);
        setAttributes(new GraphicAttributes());
        setMouseMode(createMouseMode());
        undoStack = new UndoStack();
        undoTrigger = new UndoMenuItemStackTrigger();
        undoStack.addUndoStackTrigger(undoTrigger);

        // Shouldn't assume that the drawing tool is a tool. This can
        // be set in the properties if it should be. Otherwise, the
        // default action is to appear on a right click called from
        // the GUI.
        setUseAsTool(false);
    }

    /**
     * Create the mouse mode used with the drawing tool. Called in the default
     * empty constructor, returns a OMDrawingToolMouseMode by default.
     */
    protected OMDrawingToolMouseMode createMouseMode() {
        return new OMDrawingToolMouseMode(this);
    }

    /**
     * Create a new OMGraphic, encased in a new EditableOMGraphic that can
     * modify it. If a loader cannot be found that can handle a graphic with the
     * given classname, this method will return a null object. If you aren't
     * sure of the behavior mask set in the tool, and you want a particular
     * behavior, set it before calling this method.
     * 
     * @param classname the classname of the graphic to create.
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @return OMGraphic of the classname given, null if the DrawingTool can't
     *         create it.
     */
    public OMGraphic create(String classname, DrawingToolRequestor requestor) {
        return create(classname, null, requestor);
    }

    /**
     * Create a new OMGraphic, encased in a new EditableOMGraphic that can
     * modify it. If a loader cannot be found that can handle a graphic with the
     * given classname, this method will return a null object. If you aren't
     * sure of the behavior mask set in the tool, and you want a particular
     * behavior, set it before calling this method.
     * 
     * @param classname the classname of the graphic to create.
     * @param ga GraphicAttributes object that contains more information about
     *        the type of line to be created.
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @return OMGraphic of the classname given, null if the DrawingTool can't
     *         create it.
     */
    public OMGraphic create(String classname, GraphicAttributes ga, DrawingToolRequestor requestor) {
        return create(classname, ga, requestor, isMask(SHOW_GUI_BEHAVIOR_MASK));
    }

    /**
     * Create a new OMGraphic, encased in a new EditableOMGraphic that can
     * modify it. If a loader cannot be found that can handle a graphic with the
     * given classname, this method will return a null object. This method gives
     * you the option of suppressing the GUI for the EditableOMGraphic. If you
     * aren't sure of the behavior mask set in the tool, and you want a
     * particular behavior, set it before calling this method.
     * 
     * @param classname the classname of the graphic to create.
     * @param ga GraphicAttributes object that contains more information about
     *        the type of line to be created.
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @param showGUI set to true (default) if a GUI showing attribute controls
     *        should be displayed. The behaviorMask will be adjusted
     *        accordingly.
     * @return OMGraphic of the classname given, null if the DrawingTool can't
     *         create it.
     */
    public OMGraphic create(String classname, GraphicAttributes ga, DrawingToolRequestor requestor,
                            boolean showGUI) {

        if (getCurrentEditable() != null) {
            if (DEBUG) {
                Debug.output("OMDrawingTool.edit(): can't create " + classname
                        + ", drawing tool busy with another graphic.");
            }
            return null;
        }

        if (DEBUG) {
            Debug.output("OMDrawingTool.create(" + classname + ")");
        }

        if (showGUI) {
            if (DEBUG) {
                Debug.output("OMDrawingTool.create(): showing GUI per request");
            }
            setMask(SHOW_GUI_BEHAVIOR_MASK);
        } else {
            if (DEBUG) {
                Debug.output("OMDrawingTool.create(): NOT showing GUI per request");
            }
            unsetMask(SHOW_GUI_BEHAVIOR_MASK);
        }

        EditableOMGraphic eomg = getEditableGraphic(classname, ga);

        if (eomg == null || eomg.getGraphic() == null) {
            return null;
        }

        setAttributes(ga);
        eomg.setShowGUI(isMask(SHOW_GUI_BEHAVIOR_MASK));
        eomg.setActionMask(OMGraphic.ADD_GRAPHIC_MASK);

        return edit(eomg, requestor);
    }

    /**
     * Given an OMGraphic, wrap it in the applicable EditableOMGraphic, allow
     * the user to make modifications, and then call
     * requestor.drawingComplete(). If you aren't sure of the behavior mask set
     * in the tool, and you want a particular behavior, set it before calling
     * this method.
     * 
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @return OMGraphic being modified, null if the OMDrawingTool can't figure
     *         out what to use for the modifications.
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor) {
        return edit(g, requestor, g.getShowEditablePalette());
    }

    /**
     * Given an OMGraphic, wrap it in the applicable EditableOMGraphic, allow
     * the user to make modifications, and then call
     * requestor.drawingComplete(). This methods gives you the option to
     * suppress the GUI from the EditableOMGraphic. If you aren't sure of the
     * behavior mask set in the tool, and you want a particular behavior, set it
     * before calling this method.
     * 
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @param showGUI set to true (default) if a GUI showing attribute controls
     *        should be displayed. The behaviorMask will be adjusted
     *        accordingly.
     * @return OMGraphic being modified, null if the OMDrawingTool can't figure
     *         out what to use for the modifications.
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor, boolean showGUI) {

        if (g == null) {
            if (DEBUG) {
                Debug.output("OMDrawingTool.edit(): can't edit null OMGraphic.");
            }
            return null;
        }

        if (getCurrentEditable() != null) {
            if (DEBUG) {
                Debug.output("OMDrawingTool.edit(): can't edit " + g.getClass().getName()
                        + ", drawing tool busy with another graphic.");
            }
            return null;
        }

        this.requestor = requestor;

        if (showGUI) {
            if (DEBUG) {
                Debug.output("OMDrawingTool.edit(): showing GUI per request");
            }
            setMask(SHOW_GUI_BEHAVIOR_MASK);
        } else {
            if (DEBUG) {
                Debug.output("OMDrawingTool.edit(): NOT showing GUI per request");
            }
            unsetMask(SHOW_GUI_BEHAVIOR_MASK);
        }

        EditableOMGraphic eomg = getEditableGraphic(g);

        if (eomg != null) {
            eomg.setShowGUI(isMask(SHOW_GUI_BEHAVIOR_MASK));
            eomg.setActionMask(OMGraphic.UPDATE_GRAPHIC_MASK);
            return edit(eomg, requestor);
        }

        return null;
    }

    /**
     * Given an EditableOMGraphic, use it to make modifications, and then call
     * requestor.drawingComplete(). The requestor is responsible for setting up
     * the correct initial state of the EditableOMGraphic. The requestor will be
     * given the action mask that is set in the EditableOMGraphic at this point,
     * if no other external modifications to it are made. If you aren't sure of
     * the behavior mask set in the tool, and you want a particular behavior,
     * set it before calling this method.
     * 
     * This method is called by other edit methods.
     * 
     * @param eomg OMGraphic to modify
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @return OMGraphic being modified contained within the EditableOMGraphic.
     */
    public OMGraphic edit(EditableOMGraphic eomg, DrawingToolRequestor requestor) {

        if (setCurrentEditable(eomg)) {

            // resetGUI() for current EOMG doesn't need to be called
            // here, it's called later from activate

            if (DEBUG) {
                Debug.output("OMDrawingTool.edit success");
            }

            this.requestor = requestor;

            if (currentEditable != null) {
                graphicAttributes.setFrom(currentEditable.getGraphic());
                activate();

                // Check currentEditable in case activating caused
                // something strange to happen, most likely with
                // activating the MouseModes.
                if (currentEditable != null) {
                    return currentEditable.getGraphic();
                }
            }
        }

        if (DEBUG) {
            Debug.output("OMDrawingTool.edit(): can't edit " + eomg.getClass().getName()
                    + ", drawing tool busy with another graphic.");
        }

        return null;
    }

    /**
     * A slightly different edit method, where the EditableOMGraphic is put
     * directly into edit mode, and the mouse events immediately start making
     * modifications to the OMGraphic. The palette is not shown, but if you set
     * the GUI_VIA_POPUP_BEHAVIOR_MASK on the OMDrawingTool, the option to bring
     * up the drawing tool palette will be presented to the user. If you aren't
     * sure of the behavior mask set in the tool, and you want a particular
     * behavior, set it before calling this method.
     * 
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @param e MouseEvent to use to start editing with.
     * @return OMGraphic being modified.
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor, MouseEvent e) {

        OMGraphic ret = null;

        if (getCurrentEditable() == null) {
            EditableOMGraphic eomg = getEditableGraphic(g);
            if (eomg != null) {
                ret = edit(eomg, requestor, e);
            }
        }

        return ret;
    }

    /**
     * A slightly different edit method, where the EditableOMGraphic is put
     * directly into edit mode, and the mouse events immediately start making
     * modifications to the OMGraphic. If you aren't sure of the behavior mask
     * set in the tool, and you want a particular behavior, set it before
     * calling this method.
     * 
     * @param eomg EditableOMGraphic to modify
     * @param requestor the Component that is requesting the OMGraphic. The
     *        requestor gets notified when the user is finished with the
     *        DrawingTool and the graphic is ready.
     * @param e MouseEvent to use to start editing with.
     * @return OMGraphic being modified contained within the EditableOMGraphic.
     */
    public OMGraphic edit(EditableOMGraphic eomg, DrawingToolRequestor requestor, MouseEvent e) {

        OMGraphic ret = null;
        if (eomg != null) {
            eomg.setActionMask(OMGraphic.UPDATE_GRAPHIC_MASK);

            ret = edit(eomg, requestor);

            if (ret != null) {
                currentEditable.handleInitialMouseEvent(e);
            }
        }

        return ret;
    }

    /**
     * Returns true of the OMGraphic is being edited, or is on an
     * EditableOMGraphicList being manipulated.
     */
    public boolean isEditing(OMGraphic omg) {
        boolean ret = false;
        EditableOMGraphic eomg = getCurrentEditable();
        if (eomg != null
                && eomg.getGraphic() == omg
                || (eomg instanceof EditableOMGraphicList && ((OMGraphicList) ((EditableOMGraphicList) eomg).getGraphic()).contains(omg))) {
            ret = true;
        }
        return ret;
    }

    public void deselect(OMGraphic omg) {
        if (DEBUG) {
            Debug.output("OMDrawingTool.deselect()");
        }

        if (getCurrentEditable() != null) {
            if (currentEditable.getGraphic() == omg) {
                deactivate();
            } else {
                if (currentEditable instanceof EditableOMGraphicList) {
                    ((EditableOMGraphicList) currentEditable).remove(omg);
                    canvas.repaint();
                }
            }
        }
    }

    /**
     * @return true if the OMDrawingTool is editing where it wasn't before.
     */
    public boolean select(OMGraphic omg, DrawingToolRequestor req, MouseEvent e) {

        if (DEBUG) {
            Debug.output("OMDrawingTool.select()");
        }

        OMGraphic ret = null;
        boolean currentlyEditing = (getCurrentEditable() != null);
        if (currentlyEditing) {
            boolean repaintCanvas = true;
            if (!(currentEditable instanceof EditableOMGraphicList)) {
                if (DEBUG) {
                    Debug.output("OMDrawingTool.select:  already working on OMGraphic, creating an EditableOMGraphicList for selection mode");
                }

                EditableOMGraphicList eomgl = new EditableOMGraphicList(new OMGraphicList());
                eomgl.setProjection(getProjection());
                DrawingToolRequestorList rl = new DrawingToolRequestorList();
                // Add what's current to the requestor list
                rl.add(currentEditable.getGraphic(), requestor);
                // then add the current editable to the eomgl
                eomgl.add(currentEditable);
                currentEditable.removeEOMGListener(this);

                // tell selectionlisteners to disregard the current
                // thing.
                setCurrentEditable(null);
                // reset the requestor to the requestor list
                requestor = rl;
                // now reactivate with the eomgl
                setCurrentEditable(eomgl);

                if (DEBUG) {
                    EditableOMGraphic ce = getCurrentEditable();
                    Debug.output("OMDrawingTool: current editable is: "
                            + (ce == null ? "null" : ce.getClass().getName()));
                }
                // Activate the list to make sure the listeners are
                // set up
                // so the map gets repainted with the new EOMG in
                // selected mode
                activate(false);

                // Don't need to repaint if we call activate()
                repaintCanvas = false;
            } else {
                // We already have an EditableOMGraphicList, just add
                // the new stuff to it.

                if (DEBUG) {
                    Debug.output("OMDrawingTool.select:  already working on EditableOMGraphicList");
                }
            }

            // OK, even if we've just created the new EOMGL and added
            // a previous OMG to it, we still need to deal with the
            // OMG that has just been added into the method.
            ((EditableOMGraphicList) currentEditable).add(omg, this);

            if (requestor instanceof DrawingToolRequestorList) {
                ((DrawingToolRequestorList) requestor).add(omg, req);
            } else {
                Debug.error("OHHHH, THE HORRORS!");
                Thread.dumpStack();
            }

            // OK, make the EditableOMGraphic list react to the new
            // mouse event, which will also set the state machine,
            // which will flow to the new EditableOMGraphic.
            ((EditableOMGraphicList) currentEditable).handleInitialMouseEvent(e);

            // Make sure the list is returned.
            ret = currentEditable.getGraphic();

            // Only need to call canvas.repaint() if activate isn't
            // called, and this is where that will happen. This makes
            // the grab points show up on the new OMGraphic.
            if (repaintCanvas && canvas != null) {
                canvas.repaint();
            }
        } else {
            if (DEBUG) {
                Debug.output("OMDrawingTool.select:  activating for: " + omg.getClass().getName());
            }

            // Since this is the first OMG in the tool at this point,
            // it's standard editing behavior....
            ret = edit(omg, req, e);
        }

        return ret != null;
    }

    /**
     * Given a classname, check the EditToolLoaders and create the OMGraphic it
     * represents wrapped in an EditableOMGraphic.
     * 
     * @param classname the classname of an OMGraphic to create.
     * @param ga GraphicAttributes needed to initialize the OMGraphic.
     * @return EdtiableOMGraphic, or null if none of the loaders can figure out
     *         what to make.
     */
    public EditableOMGraphic getEditableGraphic(String classname, GraphicAttributes ga) {

        EditableOMGraphic eomg = null;
        EditToolLoader loader = (EditToolLoader) loaders.get(classname);
        if (loader == null) {

            if (DEBUG) {
                Debug.output("OMDrawingTool.getEditableGraphic(" + classname
                        + ") - rechecking loaders");
            }

            // The loaders may be able to instantiate objects they
            // don't want in the GUI - check to see if they can..
            for (Iterator things = loaders.values().iterator(); things.hasNext();) {
                EditToolLoader ldr = (EditToolLoader) things.next();
                eomg = ldr.getEditableGraphic(classname, ga);
                if (eomg != null) {
                    break;
                }
            }

        } else {
            eomg = loader.getEditableGraphic(classname, ga);
        }

        if (eomg instanceof EditableOMGraphicList) {
            ((EditableOMGraphicList) eomg).init(this);
        }

        return eomg;
    }

    /**
     * Given an OMGraphic, check the EditToolLoaders and wrap it in an
     * EditableOMGraphic.
     * 
     * @param g the OMGraphic being wrapped.
     * @return EdtiableOMGraphic, or null if none of the loaders can figure out
     *         what to make.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic g) {

        // This is what we need to do to handle an OMGraphicList
        // handled for editing, but we still need to come up with a
        // way to handle DrawingToolRequestors.

        // if (g instanceof OMGraphicList) {
        // EditableOMGraphicList eomgl =
        // new EditableOMGraphicList((OMGraphicList)g);
        // eomgl.init(this);
        // return eomgl;
        // }

        Set keys = loaders.keySet();
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (DEBUG) {
                Debug.output("OMDrawingTool.getEditableGraphic(" + g.getClass().getName()
                        + "): looking at (" + key + ") loader.");
            }

            try {
                Class kc = Class.forName(key);
                Class gc = g.getClass();
                if (kc == gc || kc.isAssignableFrom(gc)) {
                    EditToolLoader loader = (EditToolLoader) loaders.get(key);

                    if (loader == null) {
                        return null;
                    }

                    // There is a reason why the generation of the
                    // graphic is done here. I think it has to do
                    // with something with the creation of the
                    // EditableOMGraphic and its display with the
                    // GrabPoints.
                    generateOMGraphic(g);

                    EditableOMGraphic eomg = loader.getEditableGraphic(g);

                    if (DEBUG) {
                        Debug.output("OMDrawingTool.getEditableGraphic(" + g.getClass().getName()
                                + "): found one.");
                    }

                    return eomg;
                }
            } catch (ClassNotFoundException cnfe) {
                if (DEBUG) {
                    Debug.output("OMDrawingTool.getEditableGraphic(" + g.getClass().getName()
                            + ") comparision couldn't find class for " + key);
                }
            }
        }
        return null;
    }

    /**
     * Return true if the OMDrawingTool can edit the OMGraphic. Meant to be a
     * low-cost check, with a minimal allocation of memory.
     */
    public boolean canEdit(Class omgc) {
        Iterator iterator;
        if (possibleEditableClasses == null) {
            Set keys = loaders.keySet();
            possibleEditableClasses = new Vector(keys.size());
            iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                try {
                    possibleEditableClasses.add(Class.forName(key));
                } catch (ClassNotFoundException cnfe) {
                    // Don't worry about this now...
                }
            }
        }

        iterator = possibleEditableClasses.iterator();
        while (iterator.hasNext()) {
            Class kc = (Class) iterator.next();
            if (kc == omgc || kc.isAssignableFrom(omgc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the EditableOMGraphic being used, if it hasn't already been set. You
     * can set it to null all the time. This method triggers the selection
     * listeners.
     */
    public synchronized boolean setCurrentEditable(EditableOMGraphic eomg) {

        if (currentEditable == null || eomg == null) {
            // Moved here so that currentEditable is set when the
            // events are fired, in case someone want's to know when
            // an OMGraphic has been selected.
            currentEditable = eomg;

            if (selectionSupport != null) {
                if (eomg == null && currentEditable != null) {
                    // No longer being edited.
                    selectionSupport.fireSelection(currentEditable.getGraphic(), requestor, false);
                } else if (eomg != null) {
                    // Starting to be edited.
                    selectionSupport.fireSelection(eomg.getGraphic(), requestor, true);
                } // else all is null, ignore...
            }

            if (currentEditable != null) {
                currentEditable.setUndoStack(undoStack);
                return true;
            }
        }

        return false;
    }

    /**
     * Get the current EditableOMGraphic being used by the drawing tool. Could
     * be null if nothing valid is happening, i.e. if the OMDrawingTool isn't
     * actively editing something.
     */
    public synchronized EditableOMGraphic getCurrentEditable() {
        return currentEditable;
    }

    /**
     * If you need your OMDrawingToolMouseMode to do something a little
     * different, you can substitute your subclass here. Don't set this to null.
     */
    public void setMouseMode(OMDrawingToolMouseMode adtmm) {
        dtmm = adtmm;
    }

    /**
     * If you want to run the drawing tool in passive mode, you'll need a handle
     * on the mouseMode to feed events to.
     */
    public OMDrawingToolMouseMode getMouseMode() {
        return dtmm;
    }

    /**
     * Add an EditToolLoader to the Hashtable of loaders that the OMDrawingTool
     * can use to create/modify OMGraphics.
     */
    public void addLoader(EditToolLoader loader) {
        String[] classnames = loader.getEditableClasses();
        rawLoaders.add(loader);
        // Add the loader to the hashtable, with the classnames as
        // keys. Then, when we get a request for a classname, we do
        // a lookup and get the proper loader for the key.
        if (classnames != null) {
            for (int i = 0; i < classnames.length; i++) {
                loaders.put(classnames[i].intern(), loader);
            }
            possibleEditableClasses = null;
        }
        firePropertyChange(LoadersProperty, null, rawLoaders);
    }

    /**
     * Make sure that new property change listeners receive a current list of
     * edit tool loaders.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            super.addPropertyChangeListener(listener);
            listener.propertyChange(new PropertyChangeEvent(this, LoadersProperty, null, rawLoaders));
        }
    }

    /**
     * Remove an EditToolLoader from the Hashtable of loaders that the
     * OMDrawingTool can use to create/modify OMGraphics.
     */
    public void removeLoader(EditToolLoader loader) {
        String[] classnames = loader.getEditableClasses();

        // Remove the loader to the hashtable, with the classnames as
        // keys.
        if (classnames != null) {
            for (int i = 0; i < classnames.length; i++) {
                EditToolLoader etl = (EditToolLoader) loaders.get(classnames[i].intern());
                if (etl == loader) {
                    loaders.remove(classnames[i]);
                } else {
                    if (DEBUG) {
                        Debug.output("DrawingTool.removeLoader: loader to be removed isn't the current loader for "
                                + classnames[i] + ", ignored.");
                    }
                }
            }
            rawLoaders.remove(loader);
            firePropertyChange(LoadersProperty, null, rawLoaders);
            possibleEditableClasses = null;
        }
    }

    /**
     * Get all the loaders the OMDrawingTool has access to.
     */
    public EditToolLoader[] getLoaders() {

        Set keys = loaders.keySet();
        EditToolLoader[] etls = new EditToolLoader[keys.size()];

        Iterator iterator = keys.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            etls[count++] = (EditToolLoader) loaders.get(iterator.next());
        }
        return etls;
    }

    /**
     * Set the loaders that the OMDrawingTool has access to.
     */
    public void setLoaders(EditToolLoader[] etls) {
        loaders.clear();
        rawLoaders.clear();
        if (etls != null) {
            for (int i = 0; i < etls.length; i++) {
                addLoader(etls[i]);
            }
        }
    }

    public void resetGUIWhenDeactivated(boolean value) {
        resetGUIWhenDeactivated = value;
    }

    /**
     * Get the GUI that dictates what the OMDrawingTool has control over. This
     * should include a section on controlling the GraphicAttributes, a section
     * for controls provided by the current EditableOMGraphic for parameters
     * unique to the EOMG, and any other controls that the tool may need. This
     * method now returns this OMDrawingTool, but also serves as a reset method
     * for the GUI to configure itself for the current EditableOMGraphic.
     * <P>
     * 
     * To create different types of graphics, the OMDrawingToolMouseMode can be
     * used, to attach to a layer to make it a drawing layer. The Loaders can be
     * queried to get their trigger graphics so you can load the drawing tool
     * with a particular loader to create a particular graphic. But here, we
     * just deal with the actual controls over the particular graphic loaded and
     * being modified.
     * <P>
     * 
     * @return this.
     */
    public Component getGUI() {
        if (!resetGUIWhenDeactivated) {
            return this;
        }

        removeAll();
        Component eomgc = null;

        graphicAttributes.setLineMenuAdditions(null);
        graphicAttributes.setOrientation(getOrientation());

        // We're adding these separately now, not as part of the interface for
        // the
        // EditableOMGraphic.
        if (graphicAttributes != null) {

            add(graphicAttributes.getGUI());
        }

        if (currentEditable != null) {
            // GUI specific to a particular EditableOMGraphic type.
            eomgc = currentEditable.getGUI(graphicAttributes);
            if (eomgc != null) {
                add(eomgc);
            }
        }

        revalidate();
        return this;
    }

    public void setInformationDelegator(InformationDelegator id) {
        informationDelegator = id;
    }

    public InformationDelegator getInformationDelegator() {
        return informationDelegator;
    }

    /**
     * Put the message in a display line that the OMDrawingTool is using.
     */
    public void setRemarks(String message) {
        if (informationDelegator != null) {
            informationDelegator.displayInfoLine(message, InformationDelegator.MAP_OBJECT_INFO_LINE);
        }
    }

    /**
     * Convenience function to tell if the OMDrawingTool is currently working on
     * an OMGraphic.
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Turn the OMDrawingTool on, attaching it to the MouseDelegator or the
     * canvas component it is assigned to. Also brings up the drawing palette.
     * Called automatically from the create/edit methods.
     */
    protected synchronized void activate() {
        activate(true);
    }

    /**
     * Turn the OMDrawingTool on with the caveat that the OMDrawingTool may be
     * active already, and that a complete hookup may not be needed. If a
     * complete hookup is needed, this methid will attach the OMDrawingTool to
     * the MouseDelegator or the canvas component it is assigned to and display
     * the drawing palette. Called automatically from the create/edit methods
     * for complete hookup. Partial hookup is called from select() methods.
     */
    protected synchronized void activate(boolean completeHookup) {
        activated = true;

        if (DEBUG) {
            Debug.output("OMDrawingTool: activate()");
        }
        if (currentEditable != null && graphicAttributes != null) {
            // For partial hookups, for select() we don't need this.
            if (completeHookup) {
                graphicAttributes.setTo(currentEditable.getGraphic());
                currentEditable.getGraphic().setVisible(false);
            }
            currentEditable.addEOMGListener(this);

            // If we're editing a existing OMGraphic, we need to save current
            // state. We can check that because existing OMGraphics should be
            // selected at this point.
            if (currentEditable.getStateMachine().getState() instanceof GraphicSelectedState) {
                currentEditable.updateCurrentState(null);
            }
        }

        if (!isMask(PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK) && completeHookup) {
            if (mouseDelegator != null) {
                if (Debug.debugging("drawingtooldetail")) {
                    Debug.output("OMDrawingTool.activate() mousemode connecting to MouseDelegator");
                }
                formerMouseMode = mouseDelegator.getActiveMouseMode();
                mouseDelegator.setActiveMouseMode(dtmm);

            } else if (canvas != null) {
                // If a MouseDelegator is not being used, go directly
                // to
                // the MapBean.
                if (Debug.debugging("drawingtooldetail")) {
                    Debug.output("OMDrawingTool.activate() mousemode connecting directly to canvas");
                }
                canvas.addMouseListener(dtmm);
                canvas.addMouseMotionListener(dtmm);
            } else {
                Debug.error("Drawing Tool can't find a map to work with");
            }
        }

        // The Drawing tool is added as a projection listener so that
        // it can properly update the current graphic if the map
        // projection changes during graphic creation/edit.

        if (canvas != null) {
            if (canvas instanceof MapBean && completeHookup) {
                ((MapBean) canvas).addPaintListener(this);
                ((MapBean) canvas).addProjectionListener(this);
            }
            // Gets the graphic highlighted on the map, if needed.
            canvas.repaint();
        }

        if (completeHookup) {
            // Show the gui.
            showPalette();
        }
    }

    /**
     * Turn the drawing tool off, disconnecting it from the MouseDelegator or
     * canvas component, and removing the palette. Called automatically from the
     * mouse mode an GUI when appropriate, although you can force a cleanup if
     * needed by calling this method. Calling this version of deactivate() just
     * uses the action mask stored in the EditableOMGraphic, which knows if the
     * graphic is being updated or created.
     */
    public void deactivate() {
        int actionMask = 0;
        if (currentEditable != null) {
            actionMask = currentEditable.getActionMask();
        }
        deactivate(actionMask);
    }

    /**
     * Turn the drawing tool off, disconnecting it from the MouseDelegator or
     * canvas component, and removing the palette. This version can called when
     * you want to control what action is taken by the receiver.
     * 
     * @param actionToDoWithOMGraphic a masked int from OMGraphicConstants that
     *        describes an OMAction to take on the current editable.
     * @see com.bbn.openmap.omGraphics.OMGraphicConstants
     */
    public synchronized void deactivate(int actionToDoWithOMGraphic) {
        if (DEBUG) {
            Debug.output("OMDrawingTool: deactivate("
                    + (activated ? "while active" : "while inactive") + ")");
        }

        // Don't waste effort;
        if (!activated) {
            return;
        }

        if (!isMask(PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK)) {
            if (mouseDelegator != null) {
                mouseDelegator.setActiveMouseMode(formerMouseMode);
                mouseDelegator.removeMouseMode(dtmm);
            } else if (canvas != null) {
                // If a MouseDelegator is not being used, go directly
                // to
                // the canvas.
                canvas.removeMouseListener(dtmm);
                canvas.removeMouseMotionListener(dtmm);
            }
        }

        if (canvas != null) {
            if (canvas instanceof MapBean) {
                ((MapBean) canvas).removeProjectionListener(this);
                ((MapBean) canvas).removePaintListener(this);
            }
        }

        OMGraphic g = null;

        if (currentEditable != null) {
            if (!(currentEditable.getStateMachine().getState() instanceof com.bbn.openmap.omGraphics.editable.GraphicUndefinedState)) {
                g = currentEditable.getGraphic();
            }
            currentEditable.removeEOMGListener(this);
        }

        // ////////////////////////////////
        // Clean up, then notify listener

        setCurrentEditable(null);
        // hide the gui while currentEditable is null, so it resets to
        // the default.
        hidePalette();
        unsetMask(DEACTIVATE_ASAP_BEHAVIOR_MASK);
        popup = null;
        activated = false;
        undoStack.clearStacks(true, true);

        // End cleanup
        // ////////////////////////////////

        /**
         * Just need something to allow editors to clean up with the mouse
         * modes. Only does this if the action to take is to delete the
         * non-existent graphic.
         */
        if (g == null && (actionToDoWithOMGraphic & OMAction.DELETE_GRAPHIC_MASK) > 0) {
            g = SinkGraphic.getSharedInstance();
        }

        if (g != null && requestor != null) {
            g.setVisible(true);
            OMAction action = new OMAction();
            action.setMask(actionToDoWithOMGraphic);
            generateOMGraphic(g);
            notifyListener(g, action);
        }

        // By putting this here, it gives the listener the slight
        // opportunity to not have the gui reset right away. This
        // opportunity gives an editor tool a smoother runtime when
        // duplicate objects are being created one after another, and
        // you don't want all the GUI reconfiguring to happen when it
        // will just go back to the same thing in a second.
        getGUI();

    }

    /**
     * If the projection is not null, generate the OMGraphic.
     */
    protected void generateOMGraphic(OMGraphic g) {
        if (g != null && g.getNeedToRegenerate()) {
            Projection proj = getProjection();
            if (proj != null) {
                g.generate(proj);
            } else if (DEBUG) {
                Debug.output("OMDrawingTool: graphic needs generation: " + g.getNeedToRegenerate());
            }
        }
    }

    /**
     * Notify the listener of an action to a graphic.
     * 
     * @param graphic the graphic being created/modified
     * @param action the OMAction telling the listener what to do with the
     *        graphic.
     */
    public void notifyListener(OMGraphic graphic, OMAction action) {
        if (requestor != null) {
            if (DEBUG) {
                Debug.output("OMDrawingTool: notifying requestor, graphic with action");
            }
            requestor.drawingComplete(graphic, action);
        }

        // in case the requestor is a layer that is not visible
        if (canvas != null) {
            canvas.repaint();
        }
    }

    /**
     * ProjectionListener method. Helps if the currentEditable is set.
     */
    public void projectionChanged(ProjectionEvent e) {
        setProjection((Projection) e.getProjection().makeClone());
    }

    /**
     * Set the current projection. Tells the currentEditable what it is too.
     */
    public void setProjection(Projection proj) {
        projection = proj;
        if (currentEditable != null) {
            currentEditable.setProjection(projection);
        }
    }

    /**
     * Get the current projection, if one has been provided. If one has not been
     * provided, then the canvas is checked to see if it is a MapBean. If it is,
     * then that projection is returned. If that doesn't work, it will finally
     * return null.
     */
    public Projection getProjection() {
        if (projection == null && canvas instanceof MapBean) {
            projection = ((MapBean) canvas).getProjection();
        }
        return projection;
    }

    /**
     * Set the GraphicAttributes object used to fill the OMGraphic
     * java.awt.Graphics parameters.
     */
    public void setAttributes(GraphicAttributes da) {
        if (graphicAttributes != null) {
            graphicAttributes.getPropertyChangeSupport().removePropertyChangeListener(this);
        }

        if (da == null) {
            graphicAttributes = GraphicAttributes.DEFAULT;
        } else {
            graphicAttributes = da;
        }

        graphicAttributes.getPropertyChangeSupport().addPropertyChangeListener(this);

        if (currentEditable != null) {
            graphicAttributes.setTo(currentEditable.getGraphic());
        }
    }

    /**
     * Get the DrawingAttributes driving the parameters of the current graphic.
     */
    public GraphicAttributes getAttributes() {
        return graphicAttributes;
    }

    /**
     * PaintListener interface. We want to know when the canvas is repainted.
     * 
     * @param g the Graphics to draw into.
     */
    public void listenerPaint(Graphics g) {
        // Call repaintRender here because if the graphic is in the
        // middle of being moved, we'll draw it in the mouse event
        // thread. Otherwise, it gets set in the image for the
        // background, which looks bad.
        if (currentEditable != null) {
            // do g.create() to prevent Stroke remnants from affecting
            // the Border of the canvas.
            currentEditable.repaintRender(g.create());
        }
    }

    /**
     * Set the MouseDelegator used to receive mouse events.
     */
    public void setMouseDelegator(MouseDelegator md) {
        if (mouseDelegator != null) {
            mouseDelegator.removePropertyChangeListener(this);
        }

        mouseDelegator = md;

        if (mouseDelegator != null) {
            mouseDelegator.addPropertyChangeListener(this);
        }
    }

    /**
     * Get the MouseDelegator used to receive mouse events.
     */
    public MouseDelegator getMouseDelegator() {
        return mouseDelegator;
    }

    public void setCursor(java.awt.Cursor cursor) {
        if (canvas != null) {
            canvas.setCursor(cursor);
        }
    }

    public java.awt.Cursor getCursor() {
        if (canvas != null) {
            return canvas.getCursor();
        } else {
            return null;
        }
    }

    /**
     * Set the JComponent this thing is directing events for. If the
     * MouseDelegator is not set, the Canvas is contacted to get MouseEvents
     * from. Within the BeanContext, the OMDrawingTool looks for MapBeans to use
     * as canvases.
     */
    public void setCanvas(JComponent can) {
        canvas = can;
    }

    /**
     * Get the JComponent this thing is directing events for.
     */
    public JComponent getCanvas() {
        return canvas;
    }

    /**
     * Set whether the Tool's face should be used. The subclasses to this class
     * should either remove all components from its face, or make its face
     * invisible if this is set to false.
     */
    // public void setUseAsTool(boolean value) {
    // super.setUseAsTool(value);
    // }
    /**
     * Called from the findAndInit(Iterator) method, when objects are added to
     * the MapHandler. so the OMDrawingTool can hook up with what it needs. An
     * InformationDelegator is used to provide map coordinates of the mouse
     * movements. The MouseDelegator is used to intercept MouseEvents when the
     * OMDrawingTool is activated. The MapBean is used to get mouse events if
     * the MouseDelegator isn't loaded, and is also used to help out with smooth
     * repaints() in general. EditToolLoaders are looked for to load into the
     * OMDrawingTool to handler different graphic requests.
     */
    public void findAndInit(Object someObj) {

        if (someObj instanceof InformationDelegator) {
            if (DEBUG) {
                Debug.output("DrawingTool: found InformationDelegator");
            }
            if (dtmm != null) {
                dtmm.setInfoDelegator((InformationDelegator) someObj);
            }
            setInformationDelegator((InformationDelegator) someObj);
        }
        if (someObj instanceof MouseDelegator) {
            if (DEBUG) {
                Debug.output("DrawingTool: found MouseDelegator.");
            }
            setMouseDelegator((MouseDelegator) someObj);
        }
        if (someObj instanceof MapBean) {
            if (DEBUG) {
                Debug.output("DrawingTool: found MapBean.");
            }
            setCanvas((JComponent) someObj);
        }
        if (someObj instanceof EditToolLoader) {
            if (DEBUG) {
                Debug.output("DrawingTool: found EditToolLoader: " + someObj.getClass().getName());
            }
            addLoader((EditToolLoader) someObj);
        }
    }

    /**
     * Called by childrenRemoved, it provides a good method for handling any
     * object you may want to take away from the OMDrawingTool. The
     * OMDrawingTool figures out if it should disconnect itseld from the object.
     */
    public void findAndUndo(Object someObj) {
        if (someObj == getInformationDelegator()) {
            if (dtmm != null && dtmm.getInfoDelegator() == (InformationDelegator) someObj) {
                dtmm.setInfoDelegator(null);
            }
            setInformationDelegator(null);
        }
        if (someObj == getMouseDelegator()) {
            setMouseDelegator(null);
        }
        if (someObj == getCanvas()) {
            setCanvas(null);
        }
        if (someObj instanceof EditToolLoader) {
            removeLoader((EditToolLoader) someObj);
        }
    }

    // ////////////// end BeanContext stuff
    /**
     * Display the palette.
     */
    public void showPalette() {
        Debug.message("drawingtool", "OMDrawingTool.showPalette()");
        resetGUIWhenDeactivated = true;
        getGUI(); // resets the gui.

        /*
         * This repaint needs to be here because sometimes the GUI doesn't
         * update on the revalidate being called in the GUI.
         */
        repaint();

        // Should only be visible if the tool isn't being used as a
        // tool, which means that it's being held by something else,
        // or if it is a tool and the SHOW_GUI flag is set.
        boolean shouldBeVisible = !getUseAsTool()
                || (isMask(SHOW_GUI_BEHAVIOR_MASK) && getUseAsTool());

        setVisible(shouldBeVisible);
    }

    /**
     * Hide the OMDrawingTool palette.
     */
    public void hidePalette() {
        Debug.message("drawingtool", "OMDrawingTool.hidePalette()");

        setVisible(visibleWhenInactive);

        WindowSupport ws = getWindowSupport();
        if (ws != null) {
            ws.killWindow();
        }
    }

    public void showInWindow() {
        if (!getUseAsTool() && getWindowSupport() == null) {
            setWindowSupport(new WindowSupport(getGUI(), i18n.get(OMDrawingTool.class, "drawingtool", "Drawing Tool")));
        }

        WindowSupport ws = getWindowSupport();

        if (ws != null && !getUseAsTool()) {
            MapHandler mh = (MapHandler) getBeanContext();
            Frame frame = null;
            int xoffset = 0;
            int yoffset = 0;
            if (mh != null) {
                frame = (Frame) mh.get(java.awt.Frame.class);
                if (frame != null) {
                    xoffset = frame.getX();
                    yoffset = frame.getY();
                }
            }

            ws.displayInWindow(frame, WindowSupport.Dlg.class, windowx + xoffset, windowy + yoffset, -1, -1);
        } else {
            Debug.output("OMDrawingTool.showPalette(): NOT showing palette, ws == null:"
                    + (ws == null) + ", used as tool:" + getUseAsTool());
        }
    }

    /**
     * A integer that is looked at internally, bitwise, to determine different
     * behaviors. If you care about specific behavior of the DrawingTool, you
     * should set this to what you want to make sure the tool acts the way you
     * want.
     */
    public void setBehaviorMask(int mask) {
        behaviorMask = mask;
    }

    /**
     * A integer that is looked at internally, bitwise, to determine different
     * behaviors.
     */
    public int getBehaviorMask() {
        return behaviorMask;
    }

    /**
     * Set the behavior mask to the default.
     */
    public void resetBehaviorMask() {
        behaviorMask = DEFAULT_BEHAVIOR_MASK;
    }

    /**
     * Set a particular mask bit in the internal value.
     * 
     * @param mask an OMDrawingTool behavior mask.
     * @return the changed integer value.
     */
    public int setMask(int mask) {
        behaviorMask = OMAction.setMask(behaviorMask, mask);
        return behaviorMask;
    }

    /**
     * Unset a particular mask bit in the internal value.
     * 
     * @param mask an OMDrawingTool behavior mask.
     * @return the changed integer value.
     */
    public int unsetMask(int mask) {
        behaviorMask = OMAction.unsetMask(behaviorMask, mask);
        return behaviorMask;
    }

    /**
     * Return whether a mask value is set in the internal value.
     * 
     * @param mask an OMDrawingTool behavior mask.
     * @return whether the value bit is set on the internal value.
     */
    public boolean isMask(int mask) {
        return OMAction.isMask(behaviorMask, mask);
    }

    /**
     * PropertyChangeListener method. If DrawingAttribute parameters change,
     * this method is called, and we update the OMGraphic parameters.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source instanceof DrawingAttributes && currentEditable != null) {
            graphicAttributes.setTo(currentEditable.getGraphic());

            if (projection != null) {
                currentEditable.regenerate(projection);
            }

            if (canvas != null) {
                canvas.repaint();
            }
        } else if (source.equals(mouseDelegator)) {
            Object oldValue = pce.getOldValue();
            if (this.equals(oldValue)) {
                deactivate();
            }
        }
    }

    /**
     * Used to hold the last thing displayed to the remarks window.
     */
    String lastRemarks = "";
    JPopupMenu popup = null;
    int windowx, windowy;

    /**
     * This is a EOMGListener method, and gets called by the EditableOMGraphic
     * when something changes.
     */
    public void eomgChanged(EOMGEvent event) {
        if (Debug.debugging("drawingtooldetail")) {
            Debug.output("OMDrawingTool.eomgChanged()");
        }

        Cursor cursor = event.getCursor();
        if (cursor != null) {
            setCursor(cursor);
        }

        // We might have used the InformationDelgator to put the
        // comments
        // in the info line, but that can't work, because we are
        // already putting the lat/lon info on the info line.

        // Updated, 4.6 - now that the InformationDelegator has new
        // places for coordinate information and map object
        // information, we can sent the info there, and it looks OK.

        String message = event.getMessage();
        if (message != null && !message.equals(lastRemarks)) {
            lastRemarks = message;
            setRemarks(message);
        }

        if (event.shouldShowGUI() && isMask(ALT_POPUP_BEHAVIOR_MASK)) {
            if (DEBUG) {
                Debug.output("OMDrawingTool.eomgChanged(): try for menu.");
            }
            MouseEvent me = event.getMouseEvent();

            // While we're here, get a good place for the window in
            // case we need to put it up later.
            if (currentEditable != null) {

                currentEditable.getStateMachine().setSelected();
                currentEditable.redraw(me, true);

                Shape ces = currentEditable.getGraphic().getShape();
                if (ces != null) {
                    Rectangle rect = ces.getBounds();
                    windowx = (int) rect.getX();
                    windowy = (int) rect.getY() - 50;
                }
            }

            /**
             * Let's see if we should bring up pop-up menu with all sorts of
             * lovely options - if the right mouse key was pressed, or if the
             * ctrl key was pressed with the mouse button being released,
             * display the option menu. Otherwise, just get ready to end.
             */
            boolean popupIsUp = doPopup(me.getX(), me.getY(), null);

            currentEditable.setPopupIsUp(popupIsUp);
        } else if (event.shouldDeactivate() && isAllowDrawingToolToDeactivateItself()) {
            if (DEBUG) {
                Debug.output("OMDrawingTool.eomgChanged(): omdt being told to deactivate");
            }

            if (isMask(USE_POPUP_BEHAVIOR_MASK) && !getUseAsTool()) {
                EditableOMGraphic eomg = getCurrentEditable();
                if (eomg != null) {
                    java.awt.Shape shape = eomg.getGraphic().getShape();
                    Rectangle rect = shape.getBounds();

                    Vector vec = new Vector();
                    vec.add(new JSeparator());

                    JMenuItem done = new JMenuItem("Done");
                    done.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            deactivate();
                        }
                    });
                    vec.add(done);

                    if (!doPopup((int) (rect.getX() + rect.getWidth()), (int) (rect.getY() + rect.getHeight()), vec)) {
                        deactivate();
                    }
                }
            } else {
                deactivate();
            }
        }
    }

    public boolean isAllowDrawingToolToDeactivateItself() {
        return allowDrawingToolToDeactivateItself;
    }

    public void setAllowDrawingToolToDeactivateItself(boolean allow) {
        allowDrawingToolToDeactivateItself = allow;
    }

    protected boolean doPopup(int x, int y, java.util.List additionalOptions) {
        // TODO This prevents piggybacking and updating of menu. Eliminate the
        // test, always create a new popup. Also, popup should be protected, not
        // visible from subclasses.

        // Also, DrawingToolRequestor should be modified to allow the
        // OMDrawingTool to ask it for additionalOptions for a given
        // EditableOMGraphic

        // Also also, the OMGraphic should store the class name of the
        // editableomgraphic it wants to have used on it in it's attributes.

        if (additionalOptions != null && !additionalOptions.isEmpty()) {
            popup = null;
        }

        boolean showPopup = (popup != null);

        if (popup == null && !getUseAsTool()) {
            popup = createPopupMenu();

            if (additionalOptions != null && !additionalOptions.isEmpty() && popup != null) {
                for (Iterator it = additionalOptions.iterator(); it.hasNext();) {
                    Object obj = it.next();
                    if (obj instanceof JMenuItem) {
                        popup.add((JMenuItem) obj);
                    }
                }
            }

            showPopup = (popup != null);
        }

        if (showPopup) {

            JComponent map = null;
            if (mouseDelegator != null) {
                map = mouseDelegator.getMap();
            } else if (canvas != null) {
                // If a MouseDelegator is not being used, go
                // directly to the MapBean.
                map = canvas;
            }

            if (map != null && x >= 0 && y >= 0) {
                popup.show(map, x, y);
            } else {
                Debug.error("OMDrawingTool: no " + (map == null ? "/component" : "/")
                        + ((x < 0 || y < 0) ? "location/" : "/") + " to show popup on!");
            }
            return true;
        }

        return false;
    }

    public JPopupMenu createPopupMenu() {

        OMGraphic g = getCurrentEditable().getGraphic();
        JPopupMenu pum = new JPopupMenu();

        if ((g.getAttribute(OMGraphicConstants.CHANGE_APPEARANCE)) == null
                || ((Boolean) g.getAttribute(OMGraphicConstants.CHANGE_APPEARANCE)).booleanValue()) {

            JMenuItem gui = new JMenuItem(i18n.get(OMDrawingTool.class, "popupMenuChangeAppearance", "Attributes..."));
            gui.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    EditableOMGraphic eomg = getCurrentEditable();
                    if (eomg != null) {
                        boolean previous = eomg.getShowGUI();
                        eomg.setShowGUI(true);
                        setVisible(true);
                        if (!getUseAsTool()) {
                            showInWindow();
                        }
                        eomg.setShowGUI(previous);
                        eomg.getStateMachine().setSelected();
                    }
                }
            });

            // This is where we are going to add the DrawingAttributes menu. We
            // need to take it off the
            // EOMG gui palettes, though
            pum.add(graphicAttributes.getColorAndLineMenu());

            if (isMask(SHOW_GUI_BEHAVIOR_MASK | GUI_VIA_POPUP_BEHAVIOR_MASK) && !getUseAsTool()) {
                pum.add(gui);
            } else {
                Debug.output("Not adding Change Appearance to popup: guiViaPopup("
                        + isMask(SHOW_GUI_BEHAVIOR_MASK) + ") isTool(" + getUseAsTool() + ")");
            }
        }

        if ((g.getAttribute(OMGraphicConstants.REMOVABLE)) == null
                || ((Boolean) g.getAttribute(OMGraphicConstants.REMOVABLE)).booleanValue()) {

            JMenuItem delete = new JMenuItem(i18n.get(OMDrawingTool.class, "popupMenuDelete", "Delete"));
            delete.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    OMAction action = new OMAction();
                    action.setMask(OMGraphic.DELETE_GRAPHIC_MASK);
                    EditableOMGraphic eomg = getCurrentEditable();
                    if (eomg != null) {
                        OMGraphic g = eomg.getGraphic();
                        if (g != null) {
                            notifyListener(g, action);
                        }
                    }
                    setCurrentEditable(null);
                    deactivate();
                }
            });
            pum.add(delete);
        }

        pum.addSeparator();
        pum.add(undoTrigger.getUndoMenuItem());
        pum.add(undoTrigger.getRedoMenuItem());

        // JMenuItem reset = new JMenuItem("Undo Changes");
        // reset.setEnabled(false);
        // reset.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent ae) {
        // if (currentEditable != null) {
        // currentEditable.reset();
        // }
        // }
        // });

        // pum.add(reset);

        return pum.getComponentCount() > 0 ? pum : null;
    }

    // ////////// SelectionListener support
    public void addSelectionListener(SelectionListener list) {
        if (selectionSupport != null) {
            selectionSupport.addSelectionListener(list);
        }
    }

    public void removeSelectionListener(SelectionListener list) {
        if (selectionSupport != null) {
            selectionSupport.removeSelectionListener(list);
        }
    }

    public void clearSelectionListeners() {
        if (selectionSupport != null) {
            selectionSupport.clearSelectionListeners();
        }
    }

    // ////////// SelectionListener support ends
    public static void main(String[] args) {
        OMDrawingTool omdt = new OMDrawingTool();
        omdt.showPalette();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        visibleWhenInactive = PropUtils.booleanFromProperties(props, prefix
                + VisibleWhenInactiveProperty, visibleWhenInactive);
        getGUI();
        setVisible(visibleWhenInactive);

        String behaviorList = props.getProperty(prefix + BehaviorProperty);

        if (behaviorList != null) {
            Vector behaviorStrings = PropUtils.parseSpacedMarkers(behaviorList);
            int behavior = 0;
            for (Iterator it = behaviorStrings.iterator(); it.hasNext();) {
                String behaviorString = (String) it.next();
                try {
                    int val = OMDrawingTool.class.getField(behaviorString).getInt(null);
                    behavior |= val;
                } catch (NoSuchFieldException nsfe) {
                } catch (IllegalAccessException iae) {
                }

            }

            setMask(behavior);
        }

    }
    // TODO need to override getProperties to include Behavior mask settings.
}