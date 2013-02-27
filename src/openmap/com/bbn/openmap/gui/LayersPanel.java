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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayersPanel.java,v $
// $RCSfile: LayersPanel.java,v $
// $Revision: 1.17 $
// $Date: 2009/02/26 21:16:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.bbn.openmap.BufferedLayerMapBean;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The LayersPanel displays the list of layers that OpenMap can display. The
 * layer name is displayed accompanied by an on/off button and a tool palette
 * button. Pressing the on/off button will cause the the map to display/remove
 * the layer. Pressing the tool palette button will cause a window to be
 * displayed containing widgets specific to that layer.
 * <p>
 * 
 * The order of the layers in the list reflects the order that the layers are
 * displayed on the map, with the bottom-most layer listed on the panel
 * underneath all the the other layers displayed on the map. The order of the
 * layers is determined by their order in the Layer[] passed in the setLayers
 * method.
 * <p>
 * 
 * The order of the layers can be changed by sending the LayersPanel an
 * ActionEvent with one of the string commands in the class, or by sending a
 * PropertyChangeEvent with a command and a Layer as the new value.
 * <P>
 * 
 * In the standard GUI, the order can be changed by selecting a layer by
 * clicking on the layer's name (or on either of buttons), then clicking on one
 * of the four buttons on the left side of the panel. The four buttons signify,
 * from top to bottom: Move the selected layer to the top; Move the selected
 * layer up one position; Move the selected layer down one position; Move the
 * selected layer to the bottom.
 * <P>
 * 
 * The LayersPanel can be used within a BeanContext. If it is added to a
 * BeanConext, it will look for a LayerHandler to add itself to as a
 * LayerListener. The LayersPanel can only listen to one LayerHandler, so if
 * more than one is found, only the last one found will be used. If another
 * LayerHandler is added to the BeanContext later, the new LayerHandler will be
 * used. The LayersPanel is also considered to be a Tool, which will cause a
 * button that will bring up the LayersPanel to be automatically added to the
 * ToolPanel if a ToolPanel is part of the BeanContext.
 * <P>
 * 
 * When the LayersPanel discovers a BufferedLayerMapBean is being used, it adds
 * a special LayerPane to its LayerPane list that shows which layers are being
 * buffered in the MapBean. This special LayerPane shows up as a line in the
 * list, and all layers below that line are being specially buffered by the
 * BufferedLayerMapBean.
 * <P>
 * 
 * The properties that can be set for the LayersPanel:
 * 
 * <pre>
 * 
 * 
 * 
 *          # Use LayerStatusPanes for the layers if true, otherwise
 *          # LayerPanes.  LayerStatusPanes turn the on/off bulbs to green/red
 *          # bulbs when the layer is resting/working.  LayerPanes just show
 *          # yellow bulbs when the layer is part of the map.
 *          showStatus=true
 *          # When the BufferedLayerMapBean is used, a divider will be
 *          # displayed in the list of layers showing which layers are in the
 *          # MapBean buffer (below the line).  Commands to move layers, by
 *          # default, respect this divider, requiring more commands to have
 *          # layers cross it.
 *          boundary=true
 *          # Add control buttons - use &quot;none&quot; for no button.  If undefined,
 *          # the LayerControlButtonPanel will be created automatically.
 *          controls=com.bbn.openmap.gui.LayerControlButtonPanel
 *          # Any control properties added here, prepended by &quot;controls&quot;...
 *          controls.configuration=WEST
 * 
 * 
 * 
 * </pre>
 */
public class LayersPanel extends OMToolComponent implements Serializable,
        ActionListener, LayerListener, PropertyChangeListener {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.gui.LayersPanel");

    /** Action command for the layer order buttons. */
    public final static String LayerTopCmd = "LayerTopCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerBottomCmd = "LayerBottomCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerUpCmd = "LayerUpCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerDownCmd = "LayerDownCmd";
    /** Action command removing a layer. */
    public final static String LayerRemoveCmd = "LayerRemoveCmd";
    /** Action command adding a layer. */
    public final static String LayerAddCmd = "LayerAddCmd";
    /** Action command for notification that a layer has been selected. */
    public final static String LayerSelectedCmd = "LayerSelected";
    /**
     * Action command for notification that a layer has been deselected. Not so
     * reliable. Usually a selection notification means that others are
     * deselected.
     */
    public final static String LayerDeselectedCmd = "LayerDeselected";

    /**
     * A property to set the class to create for layer order controls. If
     * undefined, a LayerControlButtonPanel in its default configuration will be
     * created. For no controls added, use (none) for this property.
     */
    public final static String ControlButtonsProperty = "controls";
    /**
     * A property that can be used for controlling how the to top and to bottom
     * commands will be interpreted when a BufferedLayerMapBean is used. See the
     * definition of bufferedBoundary.
     */
    public final static String BufferedBoundaryProperty = "boundary";
    /**
     * A property that can be used for controlling what type of LayerPanes are
     * used. If true (default) a LayerStatusPane will be created for each layer.
     * Otherwise, a LayerPane will be used.
     */
    public final static String ShowStatusProperty = "showStatus";

    /**
     * A value for the (controls) property to not include control buttons in the
     * interface.
     */
    public final static String NO_CONTROLS = "none";

    /** Default key for the LayersPanel Tool. */
    public final static String defaultKey = "layerspanel";

    /**
     * The LayerHandler to listen to for LayerEvents, and also to notify if the
     * layer order should change.
     */
    protected transient LayerHandler layerHandler = null;
    /**
     * Panel that lets you dynamically add and configure layers.
     */
    protected transient LayerAddPanel layerAddPanel = null;
    /**
     * The components holding the layer name label, the on/off indicator and on
     * button, and the palette on/off indicator and palette on button.
     */
    protected transient List<LayerPane> panes;
    /** The internal component that holds the panes. */
    protected transient JPanel panesPanel;
    /** The scroll pane to use for panes. */
    protected transient JScrollPane scrollPane;
    /** The Layer order adjustment button group. */
    protected transient ButtonGroup bg;
    /** The ActionListener that will bring up the LayersPanel. */
    protected ActionListener actionListener;
    /** The set of buttons that control the layers. */
    protected LayerControlButtonPanel controls = null;
    /**
     * Hashtable that tracks LayerPanes for layers, with the layer as the key
     * and LayerPane as the value.
     */
    protected Hashtable<Layer, LayerPane> paneLookUp = new Hashtable<Layer, LayerPane>();
    /**
     * A special LayerPane used when the LayersPanel senses that a
     * BufferedLayerMapBean is being used. This LayersPanel is a separating line
     * showing which layers are part of the MapBean's buffer, and which are not.
     */
    protected LayerPane backgroundLayerSeparator = null;
    /**
     * Behavior flag so that if there is a background buffered layer on the
     * MapBean, and a buffered layer divider in the LayersPanel, whether
     * commands instructing a layer to the top or bottom of the list should
     * honor the virtual boundary between buffered and unbuffered layers. That
     * is, if a layer is on the bottom of the buffered list and is instructed to
     * go to the top of the overall list, it will only first travel to the top
     * of the buffered layers. On a subsequent top command, it will go to the
     * top of the list. The same behavior applies for going down. True is
     * default. If set to false, these commands will just send the selected
     * layer to the top and bottom of the entire list.
     */
    protected boolean bufferedBoundary = true;

    /**
     * Behavior flag that determines what kind of LayerPane is used for the
     * layers. If true (default) the LayerStatusPane will be used. Otherwise,
     * the LayerPane will be used instead.
     */
    protected boolean showStatus = true;

    /**
     * Construct the LayersPanel.
     */
    public LayersPanel() {
        super();
        setKey(defaultKey);
        setLayout(new BorderLayout());
        // setWindowSupport(new WindowSupport(this, i18n.get(LayersPanel.class,
        // "title",
        // "Layers")));
    }

    /**
     * Construct the LayersPanel.
     * 
     * @param lHandler the LayerHandler controlling the layers.
     */
    public LayersPanel(LayerHandler lHandler) {
        this();
        setLayerHandler(lHandler);
    }

    /**
     * Construct the LayersPanel.
     * 
     * @param lHandler the LayerHandler controlling the layers.
     * @param addLayerControls if true, buttons that modify layer positions will
     *        be added.
     */
    public LayersPanel(LayerHandler lHandler, boolean addLayerControls) {
        this(lHandler);
        if (addLayerControls) {
            addLayerControls();
        }
    }

    /**
     * Set the LayerHandler that the LayersPanel listens to. If the LayerHandler
     * passed in is not null, the LayersMenu will be added to the LayerHandler
     * LayerListener list, and the LayersMenu will receive a LayerEvent with the
     * current layers.
     * <P>
     * 
     * If there is a LayerHandler that is already being listened to, then the
     * LayersPanel will remove itself from current LayerHandler as a
     * LayerListener, before adding itself to the new LayerHandler.
     * <P>
     * 
     * Lastly, if the LayerHandler passed in is null, the LayersPanel will
     * disconnect itself from any LayerHandler currently held, and reset itself
     * with no layers.
     * 
     * @param lh LayerHandler to listen to, and to use to reorder the layers.
     */
    public void setLayerHandler(LayerHandler lh) {
        if (layerHandler != null) {
            layerHandler.removeLayerListener(this);
        }
        layerHandler = lh;
        if (layerHandler != null) {
            layerHandler.addLayerListener(this);
        } else {
            setLayers(new Layer[0]);
        }
        updateLayerPanes(layerHandler);
    }

    /**
     * Get the LayerHandler that the LayersPanel listens to and uses to reorder
     * layers.
     * 
     * @return LayerHandler.
     */
    public LayerHandler getLayerHandler() {
        return layerHandler;
    }

    /**
     * Set the LayerPanes with the given LayerHandler.
     * 
     * @param layerHandler The LayerHandler controlling the layers
     */
    protected void updateLayerPanes(LayerHandler layerHandler) {
        for (LayerPane pane : getPanes()) {
            pane.setLayerHandler(layerHandler);
        }
    }

    /**
     * LayerListener interface method. A list of layers will be added, removed,
     * or replaced based on on the type of LayerEvent. The LayersPanel only
     * reacts to LayerEvent.ALL events, to reset the components in the
     * LayersPanel.
     * 
     * @param evt a LayerEvent.
     */
    public void setLayers(LayerEvent evt) {
        Layer[] layers = evt.getLayers();
        int type = evt.getType();

        if (type == LayerEvent.ALL) {
            logger.fine("LayersPanel received layers update");
            setLayers(layers);
        }
    }

    /**
     * Tool interface method. The retrieval tool's interface. This method
     * creates a button that will bring up the LayersPanel.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        JButton layerButton = null;

        if (getUseAsTool()) {
            layerButton = new JButton(new ImageIcon(OMToolSet.class.getResource("layers.gif"), "Layer Controls"));
            layerButton.setBorderPainted(false);
            // layerButton.setToolTipText("Layer Controls");
            layerButton.setToolTipText(i18n.get(LayersPanel.class,
                    "layerButton",
                    I18n.TOOLTIP,
                    "Layer Controls"));
            layerButton.setMargin(new Insets(0, 0, 0, 0));
            layerButton.addActionListener(getActionListener());
        }

        return layerButton;
    }

    /**
     * Get the ActionListener that triggers the LayersPanel. Useful to have to
     * provide an alternative way to bring up the LayersPanel.
     * 
     * @return ActionListener
     */
    public ActionListener getActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WindowSupport ws = getWindowSupport();

                if (ws == null) {
                    ws = new WindowSupport(LayersPanel.this, i18n.get(LayersPanel.class,
                            "title",
                            "Layers"));
                    setWindowSupport(ws);
                }

                // Initial settings.
                int w = 328;
                int h = 300;

                Dimension dim = ws.getComponentSize();
                if (dim != null) {
                    w = (int) dim.getWidth();
                    h = (int) dim.getHeight();
                }

                int x = -1;
                int y = -1;

                Point loc = ws.getComponentLocation();
                if (loc != null) {
                    x = (int) loc.getX();
                    y = (int) loc.getY();
                }

                MapHandler mh = (MapHandler) getBeanContext();
                Frame frame = null;
                if (mh != null) {
                    frame = (Frame) mh.get(java.awt.Frame.class);
                }

                ws.displayInWindow(frame, x, y, w, h);
            }
        };
    }

    /**
     * Set the layers that are on the menu. Calls for AWT thread to update
     * layers
     * 
     * @param inLayers the array of layers.
     */
    public void setLayers(Layer[] inLayers) {
        if (SwingUtilities.isEventDispatchThread()) {
            setLayersFromEDT(inLayers);
        } else {
            SwingUtilities.invokeLater(new MyWorker(inLayers));
        }
    }

    /**
     * Set the layers that are in the LayersPanel. Make sure that the layer[] is
     * the same as that passed to any other OpenMap component, like the
     * LayersMenu. This method checks to see if the layer[] has actually
     * changed, in order or in size. If it has, then createPanel() is called to
     * rebuild the LayersPanel. Should be called within the Event Dispatch
     * Thread.
     * 
     * @param inLayers the array of layers.
     */
    protected void setLayersFromEDT(Layer[] inLayers) {
        Layer[] layers = inLayers;

        if (inLayers == null) {
            layers = new Layer[0];
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("LayersPanel.setLayers() with " + layers.length
                    + " layers.");
        }

        List<LayerPane> panes = getPanes();
        int separatorOffset = 0;
        if (backgroundLayerSeparator != null
                && panes.contains(backgroundLayerSeparator)) {
            separatorOffset = 1;
        }

        if (panes.size() - separatorOffset != layers.length) {
            // if the panel hasn't been created yet, or if someone has
            // changed the layers on us, rebuild the panel.
            createPanel(layers);
            return;
        }

        int i = 0;
        Iterator<LayerPane> it = panes.iterator();
        while (it.hasNext() && i < layers.length) {
            LayerPane pane = (LayerPane) it.next();

            if (pane == backgroundLayerSeparator) {
                continue;
            }

            if (pane.getLayer() != layers[i]) {
                // If the layer order sways at all, then we start over
                // and rebuild the panel
                createPanel(layers);
                return;
            } else {
                pane.updateLayerLabel();
            }

            // Do this just in case someone has changed something
            // somewhere else...
            pane.setLayerOn(layers[i].isVisible());
            i++;
        }

        // One last check for a mismatch...
        if (it.hasNext() || i < layers.length) {
            createPanel(layers);
        }
        // If we get here, it means that what we had is what we
        // wanted.
    }

    protected List<LayerPane> getPanes() {
        if (panes == null) {
            panes = new LinkedList<LayerPane>();
        }
        return panes;
    }

    protected void setPanes(List<LayerPane> lpa) {
        panes = lpa;
    }

    GridBagLayout panelGridbag;
    GridBagConstraints pgbc;

    /**
     * Create the panel that shows the LayerPanes. This method creates the
     * on/off buttons, palette buttons, and layer labels, and adds them to the
     * scrollPane used to display all the layers.
     * 
     * @param inLayers the Layer[] that reflects all possible layers that can be
     *        added to the map.
     */
    public void createPanel(Layer[] inLayers) {
        logger.fine("creating panel");

        Layer[] layers = inLayers;
        if (layers == null) {
            layers = new Layer[0];
        }

        if (panesPanel == null) {
            panesPanel = new JPanel();
            panelGridbag = new GridBagLayout();
            pgbc = new GridBagConstraints();

            panesPanel.setLayout(panelGridbag);

            pgbc.gridwidth = GridBagConstraints.REMAINDER;
            pgbc.anchor = GridBagConstraints.NORTHWEST;
            pgbc.fill = GridBagConstraints.HORIZONTAL;
            pgbc.weightx = 1.0f;

        } else {
            ((GridBagLayout) panesPanel.getLayout()).invalidateLayout(panesPanel);
            panesPanel.removeAll();
        }

        if (bg == null) {
            bg = new ButtonGroup() {
                public void setSelected(ButtonModel m, boolean b) {
                    if (!b) {
                        for (LayerPane pane : getPanes()) {
                            pane.setSelected(false);
                        }
                    }
                    super.setSelected(m, b);
                }
            };
        }

        List<LayerPane> panes = new LinkedList<LayerPane>();
        List<LayerPane> backgroundPanes = new LinkedList<LayerPane>();

        // populate the arrays of CheckBoxes and strings used to fill
        // the JPanel for the panes
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];
            if (layer == null) {
                logger.fine("caught null layer, " + i + " out of "
                        + layers.length);
                continue;
            }

            LayerPane lpane = (LayerPane) paneLookUp.get(layer);

            if (lpane == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Creating LayerPane for " + layer.getName());
                }
                lpane = createLayerPaneForLayer(layer, layerHandler, bg);
                lpane.addPropertyChangeListener(LayerSelectedCmd, this);
                lpane.addPropertyChangeListener(LayerDeselectedCmd, this);
                paneLookUp.put(layer, lpane);
            } else {
                // In case this has been modified elsewhere...
                lpane.setLayerOn(layer.isVisible());
            }

            if (layer.getAddAsBackground() && backgroundLayerSeparator != null) {
                backgroundPanes.add(lpane);
            } else {
                panes.add(lpane);

                panelGridbag.setConstraints(lpane, pgbc);
                panesPanel.add(lpane);
            }
        }

        if (!backgroundPanes.isEmpty()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Adding BackgroundLayerSeparator");
            }
            panes.add(backgroundLayerSeparator);
            panelGridbag.setConstraints(backgroundLayerSeparator, pgbc);
            panesPanel.add(backgroundLayerSeparator);
            panes.addAll(backgroundPanes);

            for (LayerPane lp : backgroundPanes) {
                panelGridbag.setConstraints(lp, pgbc);
                panesPanel.add(lp);
            }

        } else if (backgroundLayerSeparator != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("No layers are background layers, adding separator");
            }
            panes.add(backgroundLayerSeparator);
            panelGridbag.setConstraints(backgroundLayerSeparator, pgbc);
            panesPanel.add(backgroundLayerSeparator);
        }

        addFillerToPanesPanel();

        setPanes(panes);

        if (scrollPane == null) {
            scrollPane = new JScrollPane(panesPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(scrollPane, BorderLayout.CENTER);
        }

        revalidate();
    }

    protected void addFillerToPanesPanel() {
        JPanel filler = new JPanel();
        pgbc.fill = GridBagConstraints.BOTH;
        pgbc.weighty = 1.0f;
        panelGridbag.setConstraints(filler, pgbc);
        panesPanel.add(filler);
        pgbc.fill = GridBagConstraints.HORIZONTAL;
        pgbc.weighty = 0.0f;
    }

    /**
     * Called when a new LayerPane needs to be created for a layer. You can use
     * this to extend LayerPane and return something else that fits your GUI.
     */
    protected LayerPane createLayerPaneForLayer(Layer layer,
                                                LayerHandler layerHandler,
                                                ButtonGroup bg) {
        if (showStatus) {
            return new LayerStatusPane(layer, layerHandler, bg);
        } else {
            return new LayerPane(layer, layerHandler, bg);
        }
    }

    public void deletePanes(List<LayerPane> dpanes) {
        logger.fine("deleting panes");
        if (dpanes != null) {
            paneLookUp.clear();

            for (LayerPane pane : dpanes) {
                if (pane != null && pane != backgroundLayerSeparator) {
                    pane.removePropertyChangeListener(this);
                    pane.cleanup(bg);
                }
            }
        }
    }

    /**
     * Tell the LayersPanel to add layer control buttons. Does nothing if the
     * controls are already set.
     */
    public void addLayerControls() {
        if (getControls() == null) {
            setControls(createControlButtons());
        }
    }

    /**
     * Set up the buttons used to move layers up and down, or add/remove layers.
     * The button component should hook itself up to the LayersPanel, and assume
     * that the LayersPanel has a BorderLayout with the list in the center spot.
     */
    public LayerControlButtonPanel createControlButtons() {
        return new LayerControlButtonPanel(this);
    }

    /**
     * Should be called internally, when the LayersPanel creates the
     * LayerControlButtonPanel. If called from the LCBP, a loop will ensue.
     * 
     * @param lcbp
     */
    protected void setControlsAndNotify(LayerControlButtonPanel lcbp) {
        setControls(lcbp);
        if (lcbp != null) {
            lcbp.setLayersPanel(this);
        }
    }

    /**
     * Simply sets the controls.
     * 
     * @param lcbp
     */
    public void setControls(LayerControlButtonPanel lcbp) {
        controls = lcbp;
    }

    public LayerControlButtonPanel getControls() {
        return controls;
    }

    /**
     * Method associated with the ActionListener interface. This method listens
     * for action events meant to change the order of the layers, as fired by
     * the layer order buttons.
     * 
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(command);
        }

        try {
            LayerPane pane = findSelectedPane();
            if (pane != null) {
                moveLayer(pane, command);
            }
        } catch (NullPointerException npe) {
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }

    /**
     * Change a layer's position.
     */
    public void moveLayer(Layer layer, String command) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(command + " for " + layer.getName());
        }

        moveLayer((LayerPane) paneLookUp.get(layer), command);
    }

    /**
     * Change a layer's position, with the layer represented by a LayerPane.
     */
    protected void moveLayer(LayerPane lp, String command) {

        if (lp == null) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("LayerPane not represented on list");
            }

            if (command == LayerRemoveCmd) {
                // OK, here's a hidden trick. If no layers are
                // selected and the minus sign is clicked, then this is called.
                System.gc();
            }
            return;
        }

        List<LayerPane> panes = getPanes();
        int row = panes.indexOf(lp);

        boolean boundary = false;
        int bls_row = -1;
        if (backgroundLayerSeparator != null) {
            bls_row = panes.indexOf(backgroundLayerSeparator);
            boundary = bufferedBoundary;
        }

        if (command.equals(LayerTopCmd)) {
            // Move layer selected layer to top
            panes.remove(lp);
            if (boundary && bls_row > 0 && row > bls_row + 1) {
                // If the backgroundLayerSeparator is more than one
                // above it, move to just below it on the first top
                // command.
                panes.add(bls_row + 1, lp);
            } else {
                panes.add(0, lp);
            }

            rejiggerMapLayers();
        } else if (command.equals(LayerBottomCmd)) {
            // Move layer selected layer to bottom
            panes.remove(lp);

            if (boundary && bls_row > 0 && row < bls_row - 1) {
                // If the backgroundLayerSeparator is more than one
                // below it, move to just above it on the first top
                // command.
                panes.add(bls_row - 1, lp);
            } else {
                panes.add(panes.size(), lp);
            }

            rejiggerMapLayers();
        } else if (command.equals(LayerUpCmd)) {
            // Move layer selected layer up one
            if (row <= 0)
                return;
            panes.remove(row);
            panes.add(row - 1, lp);
            rejiggerMapLayers();
        } else if (command.equals(LayerDownCmd)) {
            // Move layer selected layer up one
            if (row < 0 || row == panes.size() - 1)
                return;
            panes.remove(row);
            panes.add(row + 1, lp);
            rejiggerMapLayers();
        } else if (command.equals(LayerRemoveCmd)) {

            if (layerHandler == null || !lp.getLayer().removeConfirmed()) {
                return;
            }

            // This order is somewhat important. lp.getLayer() will
            // be null after lp.cleanup. lp.setSelected() will cause
            // a series of property change notifications.

            layerHandler.removeLayer(lp.getLayer());
            lp.cleanup(bg);

            return;

        } else if (command.equals(LayerAddCmd)) {
            if (layerAddPanel != null) {
                layerAddPanel.showPanel();
            }
        }
    }

    /**
     * Find the selected LayerPane in the current LayerPane list. Will return
     * null if there isn't a selected pane.
     */
    protected LayerPane findSelectedPane() {
        for (LayerPane pane : getPanes()) {
            if (pane.isSelected()) {
                return pane;
            }
        }
        return null;
    }

    /**
     * Makes a new layer cake of active layers to send to
     * LayerHandler.setLayers().
     */
    protected void rejiggerMapLayers() {
        logger.fine("jiggering.......");

        if (layerHandler == null) {
            // Why bother doing anything??
            return;
        }

        int selectedRow = -1;

        panesPanel.removeAll();
        panelGridbag.invalidateLayout(panesPanel);

        List<LayerPane> panes = getPanes();
        List<Layer> layerList = new LinkedList<Layer>();

        int bufferIndex = Integer.MAX_VALUE;

        int i = 0; // track layer index

        for (LayerPane pane : panes) {

            if (pane == backgroundLayerSeparator) {
                panelGridbag.setConstraints(pane, pgbc);
                panesPanel.add(backgroundLayerSeparator);
                bufferIndex = i++;
                continue;
            }

            Layer layer = pane.getLayer();
            layer.setAddAsBackground(i > bufferIndex);
            panelGridbag.setConstraints(pane, pgbc);
            panesPanel.add(pane);
            layerList.add(layer);

            if (pane.isSelected()) {
                selectedRow = i;
            }
            i++;
        }

        addFillerToPanesPanel();

        scrollPane.revalidate();

        // Scroll up or down as necessary to keep selected row
        // viewable
        if (selectedRow >= 0) {
            int spheight = scrollPane.getHeight();
            JScrollBar sb = scrollPane.getVerticalScrollBar();
            int sv = sb.getValue();
            int paneheight = ((LayerPane) panes.get(selectedRow)).getHeight();
            int rowvalue = selectedRow * paneheight;
            // Don't reset scrollBar unless the selected row
            // is not in the viewable range
            if (!((rowvalue > sv) && (rowvalue < spheight + sv))) {
                sb.setValue(rowvalue);
            }
        }

        Object[] layerArray = layerList.toArray();
        int length = layerArray.length;
        Layer[] newLayers = new Layer[length];

        for (int j = 0; j < length; j++) {
            newLayers[j] = (Layer) layerArray[j];
        }

        layerHandler.setLayers(newLayers);
    }

    /**
     * Update the layer names - if a layer name has changed, tell the LayerPanes
     * to check with their layers to update their labels.
     */
    public synchronized void updateLayerLabels() {
        for (LayerPane pane : getPanes()) {
            pane.updateLayerLabel();
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String command = pce.getPropertyName();
        Object obj = pce.getNewValue();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("receiving PropertyChangeEvent " + command + ", "
                    + pce.toString());
        }

        if ((command == LayerSelectedCmd || command == LayerDeselectedCmd)
                && obj instanceof Layer) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("layer panel notification that layer is selected: "
                        + ((Layer) obj).getName());
            }
            firePropertyChange(command, null, ((Layer) obj));

        } else if ((command == LayersPanel.LayerTopCmd
                || command == LayersPanel.LayerBottomCmd
                || command == LayersPanel.LayerUpCmd
                || command == LayersPanel.LayerDownCmd || command == LayersPanel.LayerRemoveCmd)
                && obj instanceof Layer) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("layer panel notification that layer should be raised: "
                        + ((Layer) obj).getName());
            }
            moveLayer((Layer) obj, command);
        }
    }

    /**
     * Called when the LayersPanel is added the BeanContext, or when another
     * object is added to the BeanContext after the LayerHandler has been added.
     * This allows the LayersPanel to keep up-to-date with any objects that it
     * may be interested in, namely, the LayerHandler. If a LayerHandler has
     * already been added, the new LayerHandler will replace it.
     * 
     * @param someObj the object being added to the BeanContext
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof LayerHandler) {
            // do the initializing that need to be done here
            logger.fine("LayersPanel found a LayerHandler");
            setLayerHandler((LayerHandler) someObj);
        }

        if (someObj instanceof BufferedLayerMapBean) {
            logger.fine("LayersPanel found BufferedLayerMapBean, creating separator panel");
            backgroundLayerSeparator = LayerPane.getBackgroundLayerSeparator(" --- Background Layers --- ");
        }

        // Don't want to forward ourselves on to controls, supposedly
        // they already know.
        if (controls != null && someObj != this) {
            controls.findAndInit(someObj);
        }
    }

    /**
     * BeanContextMembershipListener method. Called when an object has been
     * removed from the parent BeanContext. If a LayerHandler is removed, and
     * it's the current one being listened to, then the layers in the panel will
     * be wiped clean.
     * 
     * @param someObj the object being removed from the BeanContext
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof LayerHandler) {
            // do the initializing that need to be done here
            logger.fine("LayersPanel removing LayerHandler");
            if (getLayerHandler() == (LayerHandler) someObj) {
                setLayerHandler(null);
            }
        }

        // Don't want to forward ourselves on to controls, supposedly
        // they already know.
        if (controls != null && someObj != this) {
            controls.findAndUndo(someObj);
        }

        if (someObj instanceof Layer) {
            paneLookUp.remove((Layer) someObj);
        }
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String controlString = props.getProperty(prefix
                + ControlButtonsProperty);

        if (controlString != NO_CONTROLS) {
            if (controlString == null) {
                setControls(createControlButtons());
            } else {
                Object obj = ComponentFactory.create(controlString, prefix
                        + ControlButtonsProperty, props);

                if (obj instanceof LayerControlButtonPanel) {
                    setControlsAndNotify((LayerControlButtonPanel) obj);
                }
            }
        }

        bufferedBoundary = PropUtils.booleanFromProperties(props, prefix
                + BufferedBoundaryProperty, bufferedBoundary);
        showStatus = PropUtils.booleanFromProperties(props, prefix
                + ShowStatusProperty, showStatus);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        LayerControlButtonPanel controls = getControls();
        if (controls != null) {
            props.put(prefix + ControlButtonsProperty, controls.getClass()
                    .getName());
            controls.getProperties(props);
        }
        props.put(prefix + BufferedBoundaryProperty,
                new Boolean(bufferedBoundary).toString());
        props.put(prefix + ShowStatusProperty,
                new Boolean(showStatus).toString());
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        String interString = i18n.get(LayersPanel.class,
                ControlButtonsProperty,
                I18n.TOOLTIP,
                "Class to use for layer control buttons (Optional)");
        props.put(ControlButtonsProperty, interString);
        interString = i18n.get(LayersPanel.class,
                ControlButtonsProperty,
                "Button Panel Control");
        props.put(ControlButtonsProperty + LabelEditorProperty, interString);

        LayerControlButtonPanel controls = getControls();
        if (controls != null) {
            controls.getPropertyInfo(props);
        }

        interString = i18n.get(LayersPanel.class,
                BufferedBoundaryProperty,
                I18n.TOOLTIP,
                "Force layer movement to respect background layer boundary.");
        props.put(BufferedBoundaryProperty, interString);
        interString = i18n.get(LayersPanel.class,
                BufferedBoundaryProperty,
                "Use Background Layers");
        props.put(BufferedBoundaryProperty + LabelEditorProperty, interString);
        props.put(BufferedBoundaryProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        interString = i18n.get(LayersPanel.class,
                ShowStatusProperty,
                I18n.TOOLTIP,
                "Use Layer Panes that show layer status.");
        props.put(ShowStatusProperty, interString);
        interString = i18n.get(LayersPanel.class,
                ShowStatusProperty,
                "Show Layer Status");
        props.put(ShowStatusProperty + LabelEditorProperty, interString);
        props.put(ShowStatusProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return props;
    }

    class MyWorker implements Runnable {

        private Layer[] layers;

        public MyWorker(Layer[] inLayers) {
            layers = inLayers.clone();
        }

        public void run() {
            try {
                setLayersFromEDT(layers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
