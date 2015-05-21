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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/EditorLayer.java,v $
// $RCSfile: EditorLayer.java,v $
// $Revision: 1.14 $
// $Date: 2006/04/11 00:15:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.Container;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.layer.DrawingToolLayer;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.MapMouseInterpreter;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The EditorLayer is a layer that provides a specific set of tools to modify a
 * set of OMGraphics that represent specific types of objects. It has an
 * EditorTool that controls what the interface looks like, and controls
 * reception of the mouse events to direct their interpretation usefully. The
 * EditorLayer can use the following property:
 * 
 * <pre>
 * 
 * 
 *   # could be com.bbn.openmap.layer.editor.DrawingEditorTool, for instance
 *   editorLayer.editor=EditorTool class
 * 
 * 
 * </pre>
 */
public class EditorLayer extends DrawingToolLayer implements Tool {

    /**
     * The EditorTool controls the interface, and how OMGraphics are managed.
     */
    protected EditorTool editorTool = null;

    /**
     * The mouse mode used to direct mouse events to the editor.
     */
    protected EditorLayerMouseMode elmm = null;

    /**
     * The property to use of the EditorLayer doesn't really know what
     * EditorTool it will have. This property is used in setProperties if the
     * EditorTool isn't already set. If you extend the EditorLayer and
     * specifically set the EditorTool in the constructor, this property will be
     * ignored.
     */
    public final static String EditorToolProperty = "editor";

    protected int orientation = SwingConstants.HORIZONTAL;

    public EditorLayer() {
        super();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        if (editorTool == null) {
            String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
            String editorClassName = props.getProperty(realPrefix
                    + EditorToolProperty);
            if (editorClassName != null) {
                // Try to create with this layer as an argument.
                Object[] objArgs = { this };

                editorTool = (EditorTool) ComponentFactory.create(editorClassName,
                        objArgs,
                        prefix,
                        props);

                if (editorTool == null) {
                    // OK, try to create with an empty constructor.
                    editorTool = (EditorTool) ComponentFactory.create(editorClassName,
                            prefix,
                            props);
                }

                if (editorTool == null) {
                    String additionalInfo = ".";
                    if (editorClassName != null) {
                        additionalInfo = ", although an editor tool class ("
                                + editorClassName + ") was defined.";
                    }
                    Debug.error(getName()
                            + " doesn't have a EditorTool defined"
                            + additionalInfo);
                }
            }
        }
    }

    /**
     * Get and/or create the EditorLayerMouseMode that can be used specifically
     * for this layer, used to capture the MapBean's MouseEvents when an
     * EditorTool is invoked. The EditorLayerMouseMode is invisible, meaning it
     * won't show up in standard OpenMap GUI widgets as a viable MouseMode. It
     * is expected that the EditorTool will compensate for displaying what is
     * going on.
     * <P>
     * 
     * If the EditorLayerMouseMode isn't set programmatically, this method will
     * create one with this layer's name as the mouse mode ID. If the layer's
     * name hasn't been set, a temporary mouse mode will be returned, but with a
     * somewhat random name that may not really work as expected. Once the
     * layer's name gets set, however, a good, usable mouse mode will get picked
     * up and used.
     */
    public EditorLayerMouseMode getMouseMode() {
        if (elmm == null) {
            String ln = getName();
            if (ln == null) {
                // Try something unique, but don't make it permanent.
                // This will keep the layer cookin' along, but force a
                // new mouse mode until the name gets set.
                ln = this.getClass().getName() + System.currentTimeMillis();
                return new EditorLayerMouseMode(ln.intern(), true);
            }
            elmm = new EditorLayerMouseMode(ln.intern(), true);
        }
        return elmm;
    }

    /**
     * Need to do this so the EditorLayerMouseMode will be recreated with the
     * proper identification if the name of the layer changes.
     */
    public void setName(String name) {
        super.setName(name);
        elmm = null;
        /*
         * Need to call this in case the layer name gets set after the mouse
         * mode ids are set, so the new elmm id is included in this list. This
         * list might get populated with unused strings, but that should be OK,
         * as long as the one we need is on the list.
         */
        setMouseModeIDsForEvents(getMouseModeIDsForEvents());
    }

    /**
     * DrawingToolRequestor method. It's actually pretty important to call
     * EditorTool.drawingComplete() from here, too, if you create a subclass to
     * EditorLayer. The EditorTool needs to know this to reset the drawing tool
     * mouse mode, to get ready for another new OMGraphic if necessary.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
        super.drawingComplete(omg, action);
        if (editorTool != null) {
            editorTool.drawingComplete(omg, action);
        }
    }

    /**
     * Called by findAndInit(Iterator) so subclasses can find objects, too.
     */
    public void findAndInit(Object someObj) {
        // We don't want the EditorLayer to find the DrawingTool
        // in the MapHandler. The EditorTool should set its own.
        if (!(someObj instanceof DrawingTool)) {
            super.findAndInit(someObj);
        }

        if (editorTool != null) {
            editorTool.findAndInit(someObj);
        }

        if (someObj instanceof InformationDelegator
                || someObj instanceof SelectMouseMode) {
            getMouseMode().findAndInit(someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        if (editorTool != null) {
            editorTool.findAndUndo(someObj);
        }

        if (someObj instanceof InformationDelegator
                || someObj instanceof SelectMouseMode) {
            getMouseMode().findAndUndo(someObj);
        }
    }

    public void dispose() {
        if (editorTool != null) {
            editorTool.dispose();
        }
        super.dispose();
    }

    public void setMouseModeIDsForEvents(String[] modes) {
        // creates the MouseMode if needed
        EditorLayerMouseMode elmm = getMouseMode();

        String[] newModes = new String[modes.length + 1];
        System.arraycopy(modes, 0, newModes, 0, modes.length);
        newModes[modes.length] = elmm.getID();
        super.setMouseModeIDsForEvents(newModes);
    }

    public String[] getMouseModeIDsForEvents() {
        String[] modes = super.getMouseModeIDsForEvents();
        if (modes == null) {
            // Set the internal mouse mode as valid, since it hasn't
            // been set yet.
            setMouseModeIDsForEvents(new String[0]);
            // Since it's set now, return it.
            return super.getMouseModeIDsForEvents();
        } else {
            return modes;
        }
    }

    /**
     * Get the interpreter used to field and interpret MouseEvents, thereby
     * calling GestureResponsePolicy methods on this layer. It returns whatever
     * has been set as the interpreter, which could be null.
     */
    public MapMouseInterpreter getMouseEventInterpreter() {
        return mouseEventInterpreter;
    }

    public EditorTool getEditorTool() {
        return editorTool;
    }

    public void setEditorTool(EditorTool editorTool) {
        this.editorTool = editorTool;
    }

    /**
     * Part of a layer hack to notify the component listener when the component
     * is hidden. These components don't receive the ComponentHidden
     * notification. Remove when it works.
     */
    public void setVisible(boolean show) {
        if (editorTool != null) {
            editorTool.setVisible(show);
        }
        super.setVisible(show);
    }

    // /////////////////////////////
    // Tool interface methods
    // /////////////////////////////

    /**
     * The tool's interface. This is added to the tool bar.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        if (editorTool != null) {
            return editorTool.getFace();
        } else {
            return new JPanel();
        }
    }

    /**
     * The retrieval key for this tool. We use the property prefix for the key.
     * If the property prefix is not set then the name is used, which may not be
     * that unique.
     * 
     * @return String The key for this tool.
     */
    public String getKey() {
        String tmpKey = getPropertyPrefix();
        if (tmpKey == null) {
            tmpKey = getName();
            if (tmpKey == null) {
                tmpKey = getClass().getName();
            }
        }
        return tmpKey;
    }

    /**
     * Set the retrieval key for this tool. This call sets the key used for the
     * Tool interface method, which is generally the property prefix used for
     * this layer. Do not use this lightly, since the ToolPanel may be expecting
     * to find a key that is reflected in the openmap.properties file.
     * 
     * @param aKey The key for this tool.
     */
    public void setKey(String aKey) {
        setPropertyPrefix(aKey);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * A hook to get a handle on a new OMGraphic that is being created for
     * editing. The DrawingEditorTool calls this. NOOP here, but if you need a
     * handle to the new OMGraphic just as it's being created, here it is.
     * 
     * @param newOMG
     */
    protected void creatingOMGraphic(OMGraphic newOMG) {
    }
}
