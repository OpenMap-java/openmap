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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/DrawingEditorTool.java,v $
// $RCSfile: DrawingEditorTool.java,v $
// $Revision: 1.14 $
// $Date: 2008/09/16 18:37:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.gui.GridBagToolBar;
import com.bbn.openmap.gui.OMGraphicDeleteTool;
import com.bbn.openmap.layer.DrawingToolLayer;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.EditToolLoader;
import com.bbn.openmap.tools.drawing.OMCircleLoader;
import com.bbn.openmap.tools.drawing.OMDistanceLoader;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.drawing.OMDrawingToolMouseMode;
import com.bbn.openmap.tools.drawing.OMLineLoader;
import com.bbn.openmap.tools.drawing.OMPointLoader;
import com.bbn.openmap.tools.drawing.OMPolyLoader;
import com.bbn.openmap.tools.drawing.OMRectLoader;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The DrawingEditorTool is a EditorTool for the EditorLayer that will use a
 * custom OMDrawingTool to create OMGraphics as needed by the EditorTool. This
 * is a class that lets you define which EditToolLoaders the internal
 * OMDrawingTool will use for its own, targeted use. These definitions are
 * specified in the properties for the EditorLayer using this tool. This class
 * provides the OMDrawingTool and all the button mechanisms organized for smooth
 * behavior integrated with the regular OpenMap mouse modes.
 * 
 * Properties for an EditorLayer using the DrawingEditorTool:
 * 
 * <pre>
 * 
 * 
 *   # Layer declaration, dtlayer has to go in openmap.layers property
 *   dtlayer.class=com.bbn.openmap.layer.editor.EditorLayer
 *   # GUI name for layer, will also be the ID of the 'hidden' mouse mode used for tool.
 *   dtlayer.prettyName=Drawing Layer
 *   # List of other MouseModes to respond to for the layer, when *not* in editing mode.
 *   dtlayer.mouseModes=Gestures
 *   # Editor Tool to use with layer
 *   dtlayer.editor=com.bbn.openmap.layer.editor.DrawingEditorTool
 *   # This tool lets you hide the drawing attribute controls (lines,
 *   # colors) for the different objects. Default is true.
 *   dtlayer.showAttributes=false
 *  
 *   # List of EditToolLoaders to use in DrawingEditorTool
 *   dtlayer.loaders=polys points lines
 *   # EditToolLoader class to use for the polys
 *   dtlayer.polys.class=com.bbn.openmap.tools.drawing.OMPolyLoader
 *   # Set the DrawingAttributes class to use for polys
 *   dtlayer.polys.attributesClass=com.bbn.openmap.omGraphics.DrawingAttributes
 *   # Polys have specific line color, defaults for other settings.
 *   dtlayer.polys.lineColor=FFFF0000
 *  
 *   # EditToolLoader classes for points and lines, they get rendered
 *   # with whatever color was set last for the DrawingEditorTool.
 *   dtlayer.points.class=com.bbn.openmap.tools.drawing.OMPointLoader
 * dtlayer.lines.class=com.bbn.openmap.tools.drawing.OMLineLoader
 * 
 */
public class DrawingEditorTool extends AbstractEditorTool implements ActionListener,
        PropertyChangeListener, PropertyConsumer {

    /**
     * OMDrawingTool handling OMGraphic modifications and creations.
     */
    protected OMDrawingTool drawingTool = null;
    /**
     * A handler on the OMDrawingToolMouseMode that the OMDrawingTool is using,
     * for convenience. If this handle is not null, then that's an internal
     * signal for this EditorTool to know that it's active and interpreting
     * MouseEvents. If this is null, and the EditorTool wants events, that's a
     * signal to create a new OMGraphic (see mousePressed).
     */
    protected OMDrawingToolMouseMode omdtmm = null;
    /**
     * The class name of the next thing to create. Used as a signal to this
     * EditorTool that when the next appropriate MouseEvent comes in, this
     * "thing" should be created.
     */
    protected String thingToCreate = null;
    /**
     * The ButtonGroup to use for the face.
     */
    protected ButtonGroup bg = null;

    /**
     * The button that unpicks all the rest of the tool buttons. It is kept
     * invisible, but a member of all the other button's ButtonGroup. When
     * selected, all of the other buttons are deselected.
     */
    protected JToggleButton unpickBtn = null;

    protected GraphicAttributes ga = null;

    /**
     * The MouseDelegator that is controlling the MouseModes. We need to keep
     * track of what's going on so we can adjust our tools accordingly.
     */
    protected MouseDelegator mouseDelegator;

    /**
     * The ArrayList containing the EditToolLoaders for the drawing tool.
     */
    protected ArrayList<EditToolLoader> loaderList = new ArrayList<EditToolLoader>();

    public final static String RESET_CMD = "RESET_CMD";

    /**
     * Property prefix for PropertyConsumer interface.
     */
    protected String propertyPrefix;

    /**
     * Hashtable that holds default DrawingAttributes for different loaders.
     */
    protected Hashtable<String, DrawingAttributes> drawingAttributesTable;

    protected boolean showAttributes = true;

    public final static String ShowAttributesProperty = "showAttributes";
    public final static String LoaderProperty = "loaders";
    public final static String AttributesClassProperty = "attributesClass";
    public final static String DefaultDrawingAttributesClass = "com.bbn.openmap.omGraphics.DrawingAttributes";

    /**
     * The general constructor that can be called from subclasses to initialize
     * the drawing tool and interface. All that is left to do for subclasses is
     * to add EditToolLoaders to the DrawingEditorTool subclass.
     */
    public DrawingEditorTool(EditorLayer layer) {
        super(layer);

        drawingAttributesTable = new Hashtable<String, DrawingAttributes>();
        initDrawingTool();

        // Ensures that the drawing tool used by super classes fits
        // the OMGraphics created by the EditorTool
        layer.setDrawingTool(drawingTool);
    }

    /**
     * Method called in the AbstractDrawingEditorTool constructor.
     */
    public void initDrawingTool() {
        drawingTool = createDrawingTool();
        drawingTool.setUseAsTool(true); // prevents popup menu use.
        drawingTool.getMouseMode().setVisible(false);
        ga = drawingTool.getAttributes();
        ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        ga.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
    }

    protected OMDrawingTool createDrawingTool() {
        return new OMDrawingTool();
    }

    public void addEditToolLoader(EditToolLoader loader) {
        loaderList.add(loader);
        drawingTool.addLoader(loader);
    }

    public void removeEditToolLoader(EditToolLoader loader) {
        loaderList.remove(loader);
        drawingTool.removeLoader(loader);
    }

    public void clearEditToolLoaders() {
        loaderList.clear();
        drawingTool.setLoaders(null);
    }

    /**
     * Add the default (line, poly, rectangle, circle/range rings, point)
     * capabilities to the tool.
     */
    public void initDefaultDrawingToolLoaders() {
        addEditToolLoader(new OMDistanceLoader());
        addEditToolLoader(new OMLineLoader());
        addEditToolLoader(new OMPolyLoader());
        addEditToolLoader(new OMRectLoader());
        addEditToolLoader(new OMCircleLoader());
        addEditToolLoader(new OMPointLoader());
    }

    /**
     * The main method for getting the tool ready to create something. When
     * called, it sets the thingToCreate from the command, calls
     * setWantEvents(true), which calls resetForNewGraphic().
     */
    protected void setWantsEvents(String command) {

        if (Debug.debugging("editortool")) {
            Debug.output("DET.setWantsEvents(" + command + ")");
        }

        // Has to be called first
        thingToCreate = command;
        setWantsEvents(true);
    }

    /**
     * The EditorTool method, with the added bonus of resetting the tool if it
     * doesn't want events.
     */
    public void setWantsEvents(boolean value) {
        super.setWantsEvents(value);
        if (!value) {
            thingToCreate = null;
        }

        if (drawingTool != null && drawingTool.isActivated()) {
            drawingTool.resetGUIWhenDeactivated(true);
            drawingTool.deactivate();
        }

        resetForNewGraphic();

        if (drawingTool != null) {
            drawingTool.setVisible(showAttributes);// Just to make
            // sure...
        }
    }

    /**
     * Called by findAndInit(Iterator) so subclasses can find objects, too.
     */
    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);

        if (someObj instanceof MapBean || someObj instanceof InformationDelegator) {
            drawingTool.findAndInit(someObj);
        }

        if (someObj instanceof MouseDelegator) {
            setMouseDelegator((MouseDelegator) someObj);
            // I think we want to handle this differently. The
            // EditorToolLayer should get the Gestures MouseMode to
            // act as a proxy for the drawing tool mouse mode when a
            // tool is not being used.
            drawingTool.findAndInit(someObj);
        }

        if (someObj instanceof OMGraphicDeleteTool) {
            ((OMGraphicDeleteTool) someObj).findAndInit(getDrawingTool());
        }

    }

    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        unhook(someObj);
    }

    public void dispose() {
        Layer layer = getLayer();
        if (layer != null) {
            BeanContext bc = layer.getBeanContext();
            if (bc instanceof MapHandler) {
                MapHandler mh = (MapHandler) bc;

                unhook(mh.get(MouseDelegator.class));
                unhook(mh.get(MapBean.class));
                unhook(mh.get(InformationDelegator.class));
                unhook(mh.get(OMGraphicDeleteTool.class));
            }
        }
    }

    protected void unhook(Object someObj) {
        if (someObj == null) {
            return;
        }

        if (someObj == mouseDelegator) {
            setMouseDelegator(null);
        }

        if (someObj instanceof MapBean || someObj instanceof InformationDelegator) {
            drawingTool.findAndUndo(someObj);
        }

        if (someObj instanceof OMGraphicDeleteTool) {
            ((OMGraphicDeleteTool) someObj).findAndUndo(drawingTool);
        }
    }

    /**
     * When a graphic is complete, the drawing tool gets ready to make another.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
        // Watch out, gets called when drawingTool.deactivate() gets
        // called, so you can get in a loop if you try to do too much
        // here with regard to setting up the next OMGraphic to
        // create.
        if (thingToCreate != null) {
            drawingTool.resetGUIWhenDeactivated(false);
        }
        omdtmm = null;
    }

    /**
     * Called when the Tool should be reset to draw a new graphic. Currently
     * sets the OMDrawingToolMouseMode to null, which is a signal to the
     * DrawingEditorTool that if an appropriate MouseEvent is provided, that the
     * DrawingTool should be configured to create a new OMGraphic. If the
     * OMDrawingToolMouseMode is not null, then the MouseEvent is just given to
     * it.
     */
    public void resetForNewGraphic() {
        // if thingToCreate is null, then omdtmm will be set to null
        // and the drawingTool deactivated. If thingToCreate is not
        // null, omdtmm will be ready to receive mouse events for
        // editing the new OMGraphic.
        omdtmm = activateDrawingTool(thingToCreate);
    }

    /**
     * Does everything to make the DrawingEditorTool go to sleep, and disable
     * all buttons.
     */
    public void totalReset() {
        // Need to check if the tool wants events before just
        // deactivating the drawing tool - that can mess up a edit
        // session that is unrelated to the tool but still related to
        // the DrawingToolLayer.
        if (wantsEvents()) {
            setWantsEvents(false);
            if (unpickBtn != null) {
                unpickBtn.doClick();
            }

            if (mouseDelegator != null) {
                MapMouseMode[] modes = mouseDelegator.getMouseModes();
                if (modes != null && modes.length > 0)
                    mouseDelegator.setActiveMouseMode(modes[0]);
            }
        }
    }

    /**
     * Set the OMDrawingTool to use. It's created internally, though.
     */
    public void setDrawingTool(OMDrawingTool omdt) {
        drawingTool = omdt;
    }

    /**
     * Get the OMDrawingTool to use with this DrawingEditorTool.
     */
    public OMDrawingTool getDrawingTool() {
        return drawingTool;
    }

    public GraphicAttributes getGraphicAttributes() {
        return ga;
    }

    public void setGraphicAttributes(GraphicAttributes ga) {
        this.ga = ga;
    }

    public boolean isShowAttributes() {
        return showAttributes;
    }

    public void setShowAttributes(boolean showAttributes) {
        this.showAttributes = showAttributes;
    }

    /**
     * actionPerformed - Handle the mouse clicks on the button(s)
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (Debug.debugging("editortool")) {
            Debug.output("DET.actionPerformed(" + command + ")");
        }

        if (RESET_CMD.equals(command)) {
            setWantsEvents(false);
        } else if (command != null) {
            if (!command.equals(thingToCreate)) {

                EditorLayer elayer = (EditorLayer) getLayer();
                elayer.releaseProxyMouseMode();

                if (thingToCreate == null && mouseDelegator != null) {
                    mouseDelegator.setActiveMouseModeWithID(elayer.getMouseMode().getID());
                }

                // Calling with command will set 'thingToCreate' and
                // resetForNewGraphic
                setWantsEvents(command);
            } else {
                // This is the key to making this work with the OMMouseMode and
                // OverlayMapPanel (Main app). Clicking on the active toggle
                // button just resets the layer and passes control back to the
                // main mouse mode. Kinda like it for the regular OpenMap app,
                // too.
                totalReset();
            }
        }
    }

    /**
     * Method to set up the drawing tool with default behavior in order to
     * create a new OMGraphic. Will try to deactivate the OMDrawingTool if it
     * thinks it's busy.
     * 
     * @param ttc thingToCreate, classname of thing to create
     * @return OMDrawingToolMouseMode of DrawingTool if all goes well, null if
     *         the drawing tool can't create the new thingy.
     */
    protected OMDrawingToolMouseMode activateDrawingTool(String ttc) {
        if (drawingTool != null && ttc != null) {
            // If there is a pre-defined set of DrawingAttributes for
            // a particular OMGraphic, set those attributes in the
            // GraphicAttributes used in the OMDrawingTool.
            DrawingAttributes da = (DrawingAttributes) drawingAttributesTable.get(ttc);
            if (da != null) {
                da.setOrientation(ga.getOrientation());
                da.setTo(ga);
            }

            if (Debug.debugging("editortool")) {
                Debug.output("DrawingEditorTool.activateDrawingTool(" + ttc + ")");
            }

            drawingTool.setMask(OMDrawingTool.PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK
                    | OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);

            OMGraphic newOMG = drawingTool.create(ttc, ga, (DrawingToolRequestor) getLayer(), true);

            if (newOMG == null) {
                // Something bad happened, might as well try to clean
                // up.
                if (Debug.debugging("editortool")) {
                    Debug.output("DrawingEditorTool.activateDrawingTool() failed, cleaning up...");
                }
                drawingTool.deactivate();
                return null;
            }

            OMGraphicHandlerLayer el = getLayer();
            if (el instanceof EditorLayer) {
                ((EditorLayer) el).creatingOMGraphic(newOMG);
            }

            return drawingTool.getMouseMode();
        } else {
            if (Debug.debugging("editortool")) {
                Debug.output("DrawingEditorTool.activateDrawingTool(" + ttc
                        + ") with drawing tool = " + drawingTool);
            }
        }

        return null;
    }

    // //////////////////////
    // Mouse Listener events
    // //////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) {
        if (wantsEvents()) {
            if (omdtmm != null) {

                // if you only want one OMGraphic at a time:
                // OMGraphicList omgl = layer.getList();
                // if (omgl != null && !omgl.isEmpty()) {
                // omgl.clear();
                // layer.repaint();
                // }

                omdtmm.mousePressed(e);
            }
            return consumeEvents;
        } else {
            return super.mousePressed(e);
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
        if (wantsEvents()) {
            if (omdtmm != null) {
                omdtmm.mouseReleased(e);
                return true;
            } else {
                return false;
            }
        } else {
            return super.mouseReleased(e);
        }
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) {
        if (wantsEvents()) {
            if (omdtmm != null) {
                omdtmm.mouseClicked(e);
                return consumeEvents;
            } else {
                return false;
            }
        } else {
            return super.mouseClicked(e);
        }
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        if (wantsEvents()) {
            if (omdtmm != null) {
                omdtmm.mouseEntered(e);
            }
        } else {
            super.mouseEntered(e);
        }
    }

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
        if (wantsEvents()) {
            if (omdtmm != null) {
                omdtmm.mouseExited(e);
            }
        } else {
            super.mouseExited(e);
        }
    }

    // /////////////////////////////
    // Mouse Motion Listener events
    // /////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * The listener will receive these events if it
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
        if (wantsEvents()) {
            if (omdtmm != null) {
                omdtmm.mouseDragged(e);
                return consumeEvents;
            } else {
                return false;
            }
        } else {
            return super.mouseDragged(e);
        }
    }

    /**
     * Invoked when the mouse button has been moved on a component (with no
     * buttons down).
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {
        if (wantsEvents()) {
            if (omdtmm != null) {
                omdtmm.mouseMoved(e);
                return consumeEvents;
            } else if (thingToCreate != null) {
                // This is needed to reinitialize the drawing tool to
                // create another OMGraphic after the same one was
                // just completed. drawingComplete just nulls out
                // omdtmm so that the drawing tool can finish
                // deactivating. The first mouseMoved event should
                // get the next OMGraphic ready.
                omdtmm = activateDrawingTool(thingToCreate);
                return consumeEvents;
            } else {
                return false;
            }
        } else {
            return super.mouseMoved(e);
        }
    }

    // /////////////////////////////
    // Tool interface methods
    // /////////////////////////////

    public void setVisible(boolean value) {
        super.setVisible(value);
        if (!value) {
            totalReset();
        }
    }

    /**
     * The tool's interface. This is added to the tool bar.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        if (face == null) {
            JToolBar faceTB = new GridBagToolBar();
            int orientation = ((EditorLayer) getLayer()).getOrientation();
            faceTB.setOrientation(orientation);

            if (bg == null) {
                bg = new ButtonGroup();
            }

            fillFaceToolBar(faceTB, bg);

            unpickBtn = new JToggleButton("", false);
            unpickBtn.setActionCommand(RESET_CMD);
            unpickBtn.addActionListener(this);
            unpickBtn.setVisible(false);
            bg.add(unpickBtn);
            faceTB.add(unpickBtn);

            if (drawingTool != null && showAttributes) {
                drawingTool.setOrientation(orientation);
                faceTB.add(drawingTool);
                drawingTool.showPalette();
            }

            face = faceTB;
            face.setVisible(visible);
        }

        return face;
    }

    /**
     * Fill the Face's toolbar with buttons
     */
    protected void fillFaceToolBar(JToolBar faceTB, ButtonGroup bg) {
        for (EditToolLoader loader : loaderList) {
            String[] classnames = loader.getEditableClasses();

            for (int i = 0; i < classnames.length; i++) {
                ImageIcon icon = loader.getIcon(classnames[i]);
                JToggleButton btn = new JToggleButton(icon, false);
                btn.setToolTipText(loader.getPrettyName(classnames[i]));
                btn.setFocusable(false);
                btn.setActionCommand(classnames[i]);
                btn.addActionListener(this);
                bg.add(btn);
                faceTB.add(btn);
            }
        }
    }

    /**
     * Set the MouseDelegator used to hold the different MouseModes available to
     * the map.
     */
    public void setMouseDelegator(MouseDelegator md) {
        EditorLayer el = (EditorLayer) getLayer();

        if (mouseDelegator != null) {
            if (el != null) {
                mouseDelegator.removeMouseMode(el.getMouseMode());
            }
            mouseDelegator.removePropertyChangeListener(this);
        }

        mouseDelegator = md;

        if (mouseDelegator == null) {
            return;
        }

        if (el != null) {
            mouseDelegator.addMouseMode(el.getMouseMode());
        }

        mouseDelegator.addPropertyChangeListener(this);
    }

    /**
     * Get the MouseDelegator used to control mouse gestures over the map.
     */
    public MouseDelegator getMouseDelegator() {
        return mouseDelegator;
    }

    /**
     * Listen for changes to the active mouse mode and for any changes to the
     * list of available mouse modes
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == MouseDelegator.ActiveModeProperty) {
            /*
             * If the mouse mode changes, we want to reset ourselves to be ready
             * to just adjust what's on our layer.
             */
            String mmID = ((MapMouseMode) evt.getNewValue()).getID();

            if (Debug.debugging("editortool")) {
                Debug.output("DET.propertyChange: mousemode changed to " + mmID);
            }

            if (!mmID.equals(((EditorLayer) getLayer()).getMouseMode().getID())) {
                totalReset();
            }
            drawingTool.showPalette(); // Reset to basic parameters
        }
    }

    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        showAttributes = PropUtils.booleanFromProperties(props, prefix + ShowAttributesProperty, showAttributes);

        if (drawingTool != null) {
            drawingTool.setProperties(prefix, props);
        }

        String loaderListString = props.getProperty(prefix + LoaderProperty);

        if (loaderListString != null) {
            Vector<String> loaderVector = PropUtils.parseSpacedMarkers(loaderListString);
            for (String loaderPrefix : loaderVector) {
                String loaderPropertyPrefix = PropUtils.getScopedPropertyPrefix(prefix
                        + loaderPrefix);
                String loaderClassString = props.getProperty(loaderPropertyPrefix + "class");
                String loaderAttributeClass = props.getProperty(loaderPropertyPrefix
                        + AttributesClassProperty);
                if (loaderClassString != null) {
                    Object obj = ComponentFactory.create(loaderClassString, loaderPropertyPrefix, props);

                    if (obj instanceof EditToolLoader) {
                        EditToolLoader loader = (EditToolLoader) obj;

                        if (Debug.debugging("editortool")) {
                            Debug.output("DrawingEditorTool: adding " + loaderClassString);
                        }

                        addEditToolLoader(loader);

                        if (loaderAttributeClass != null) {

                            if (Debug.debugging("editortool")) {
                                Debug.output("DrawingEditorTool: getting attributes for "
                                        + loaderAttributeClass);
                            }

                            Object daObject = ComponentFactory.create(loaderAttributeClass, loaderPropertyPrefix, props);

                            if (daObject instanceof DrawingAttributes) {
                                if (Debug.debugging("editortool")) {
                                    Debug.output("DrawingEditorTool: attributes from "
                                            + loaderAttributeClass);
                                }

                                String[] classnames = loader.getEditableClasses();
                                for (int i = 0; i < classnames.length; i++) {
                                    drawingAttributesTable.put(classnames[i], (DrawingAttributes) daObject);
                                }

                            } else {
                                if (Debug.debugging("editortool")) {
                                    Debug.output("DrawingEditorTool: attributes not an instance of DrawingAttributes");
                                }
                            }

                        } else {
                            if (Debug.debugging("editortool")) {
                                Debug.output("DrawingEditorTool: attributes not defined for "
                                        + loaderClassString);
                            }

                        }
                    }

                } else {
                    Debug.output("DrawingEditorTool.setProperties:  no loader class provided for "
                            + loaderPropertyPrefix);
                }
            }

            // Pick up initial settings from properties from the layer, if it's
            // an editor layer.
            if (layer instanceof DrawingToolLayer && ga != null) {
                ((DrawingToolLayer) layer).getDrawingAttributes().setTo(ga);
            }

        } else {
            Debug.output("DrawingEditorTool.setProperties: no loaders set in properties");
        }
    }

    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }
        return props;
    }
}