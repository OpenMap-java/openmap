// **********************************************************************
//
// <copyright>
//
// BBN Technologies, a Verizon Company
// 10 Moulton Street
// Cambridge, MA 02138
// (617) 873-8000
//
// Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayerPane.java,v $
// $RCSfile: LayerPane.java,v $
// $Revision: 1.12 $
// $Date: 2006/06/13 21:33:42 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serializable;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;

/**
 * A LayerPane is a single instance of how a layer represented in the
 * LayersPanel. It contains three widgets: an on/off button; a palette button;
 * and a toggle button with the layer name.
 * <P>
 */
public class LayerPane extends JPanel implements Serializable, ActionListener,
        ComponentListener {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.gui.LayerPane");

    protected transient AbstractButton onoffButton;
    protected transient AbstractButton paletteButton;
    protected transient JToggleButton layerName;
    protected transient boolean selected;
    protected transient Layer layer;

    protected transient LayerHandler layerHandler;

    // the icons
    protected static transient URL url1;
    protected static transient ImageIcon paletteIcon;
    protected static transient URL url2;
    protected static transient ImageIcon paletteOnIcon;
    protected static transient URL url3;
    protected static transient ImageIcon layerOnIcon;
    protected static transient URL url4;
    protected static transient ImageIcon layerOffIcon;

    public transient final static String showPaletteCmd = "showPalette";
    public transient final static String toggleLayerCmd = "toggleLayerCmd";

    protected Color offColor;
    protected Color onColor = new Color(0xFF0066CC);

    protected I18n i18n = Environment.getI18n();

    // default initializations
    static {
        url1 = LayerPane.class.getResource("PaletteOff.gif");
        paletteIcon = new ImageIcon(url1, "Palette");
        url2 = LayerPane.class.getResource("PaletteOn.gif");
        paletteOnIcon = new ImageIcon(url2, "Palette on");

        url3 = LayerPane.class.getResource("BulbOn.gif");
        layerOnIcon = new ImageIcon(url3, "Layer selected");
        url4 = LayerPane.class.getResource("BulbOff.gif");
        layerOffIcon = new ImageIcon(url4, "Layer not selected");
    }

    /**
     * @param layer the layer to be represented by the pane.
     * @param bg the buttongroup for the layer
     * @param layerHandler the LayerHandler that contains information about the
     *        Layers.
     */
    public LayerPane(Layer layer, LayerHandler layerHandler, ButtonGroup bg) {
        super();
        this.layer = layer;
        setLayerHandler(layerHandler);
        createGUI(bg);
        layer.addComponentListener(this);
    }

    protected void createGUI(ButtonGroup bg) {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        layerName = new JToggleButton(layer.getName());
        layerName.setBorderPainted(false);
        layerName.addActionListener(this);

        offColor = layerName.getBackground();
        layerName.setToolTipText(i18n.get(LayerPane.class,
                "layerName.tooltip",
                "Click to select layer"));
        layerName.setHorizontalAlignment(SwingConstants.LEFT);
        bg.add(layerName);

        c.gridy = 0;
        c.gridx = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);

        onoffButton = getOnOffButton();
        gridbag.setConstraints(onoffButton, c);
        add(onoffButton);

        paletteButton = getPaletteButton();
        gridbag.setConstraints(paletteButton, c);
        add(paletteButton);

        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(layerName, c);
        add(layerName);
    }

    /**
     * Simply creates the AbstractButton object that turns the layer on/off.
     * Override this if you want to change the kind of button used.
     * 
     * @return AbstractButton in an off state.
     */
    protected AbstractButton createOnOffButton() {
        return new JCheckBox(layerOffIcon);
    }

    /**
     * Calls createOnOFfButton to create the button, and then adds all the
     * behavior settings to the button. Override this if you want to change
     * everything about the button.
     * 
     * @return AbstractButton in an off state.
     */
    protected AbstractButton getOnOffButton() {
        AbstractButton onoffButton = createOnOffButton();
        onoffButton.setSelectedIcon(layerOnIcon);
        onoffButton.setActionCommand(toggleLayerCmd);
        onoffButton.addActionListener(this);

        String interString = i18n.get(LayerPane.class,
                "onoffButton.tooltip",
                "Turn \"{0}\" layer on/off",
                layer.getName());
        onoffButton.setToolTipText(interString);
        // Determine if this layer has already been activated
        onoffButton.setSelected(layer.isVisible());
        return onoffButton;
    }

    /**
     * Simply creates the AbstractButton object that turns the layer palette
     * on/off. Override this if you want to change the kind of button used.
     * 
     * @return AbstractButton in an off state.
     */
    protected AbstractButton createPaletteButton() {
        return new JCheckBox(paletteIcon);
    }

    /**
     * Calls createPaletteButton to create the button, and then adds all the
     * behavior settings to the button. Override this if you want to change
     * everything about the button.
     * 
     * @return AbstractButton in an off state.
     */
    protected AbstractButton getPaletteButton() {
        AbstractButton paletteButton = createPaletteButton();
        paletteButton.setSelectedIcon(paletteOnIcon);
        paletteButton.setBorderPainted(false);

        String interString;
        if (!layer.hasGUI()) {
            interString = i18n.get(LayerPane.class,
                    "paletteButton.noPaletteAvailable.tooltip",
                    "No tools available for \"{0}\" layer",
                    layer.getName());
            paletteButton.setEnabled(false);
        } else {
            interString = i18n.get(LayerPane.class,
                    "paletteButton.paletteAvailable.tooltip",
                    "Display tools for \"{0}\" layer",
                    layer.getName());
        }
        paletteButton.setToolTipText(interString);

        paletteButton.setActionCommand(showPaletteCmd);
        paletteButton.addActionListener(this);
        return paletteButton;
    }

    /**
     * Used for the background LayerPanel marker.
     */
    protected LayerPane(String title) {
        super();
        // prevent null pointers somewhere...
        this.layer = com.bbn.openmap.layer.SinkLayer.getSharedInstance();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        JSeparator sep = new JSeparator();
        sep.setToolTipText(title);

        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(sep, c);
        add(sep);
    }

    public void setLayerHandler(LayerHandler in_layerHandler) {
        layerHandler = in_layerHandler;
    }

    /**
     * @return LayerHandler if it has been found in the MapHandler or set.
     */
    protected LayerHandler getLayerHandler() {
        return layerHandler;
    }

    /**
     * Same as cleanup, except the layer name toggle button gets removed from
     * the given button group.
     */
    public void cleanup(ButtonGroup bg) {
        if (bg != null) {
            bg.remove(layerName);
        }
        cleanup();
    }

    /**
     * LayerPane disconnects from listeners, nulls out components, shuts down
     * layer palette.
     */
    public void cleanup() {
        layerName.setSelected(false);

        if (layer != null) {
            this.layer.setPaletteVisible(false);
            this.layer.removeComponentListener(this);
        }
        if (onoffButton != null) {
            onoffButton.removeActionListener(this);
        }
        if (paletteButton != null) {
            paletteButton.removeActionListener(this);
        }
        layerName = null;
        this.layer = null;
        this.layerHandler = null;
        this.removeAll();
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 32);
    }

    public Dimension getMinimumSize() {
        return new Dimension(100, 20);
    }

    /**
     * @return whether the layer is on
     */
    public boolean isLayerOn() {
        return onoffButton.isSelected();
    }

    /**
     * Turns the button on or off
     */
    public void setLayerOn(boolean value) {
        onoffButton.setSelected(value);
    }

    /**
     * @return whether the palette for this layer is on
     */
    public boolean isPaletteOn() {
        return paletteButton.isSelected();
    }

    /**
     * Turns the palette button on or off
     */
    public void setPaletteOn(boolean value) {
        paletteButton.setSelected(value);
    }

    /**
     * @return the status of the layerName toggle button
     */
    public boolean isSelected() {
        return layerName.isSelected();
    }

    /**
     * Highlights/de-highlights the panel border.
     */
    public void setSelected(boolean select) {
        String command = select ? LayersPanel.LayerSelectedCmd
                : LayersPanel.LayerDeselectedCmd;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("LayerPane for " + getLayer().getName() + " " + command
                    + ", firing event");
        }

        if (select) {
            setBorder(BorderFactory.createLoweredBevelBorder());
        } else {
            setBorder(null);
        }

        firePropertyChange(command, null, getLayer());
    }

    /**
     * @return the layer represented by this LayerPane
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Tell the pane to check with the layer to get the current layer name for
     * it's label.
     */
    public void updateLayerLabel() {
        layerName.setText(getLayer().getName());
    }

    protected void showPalette() {
        layer.showPalette();
        // This is needed in case the palette button is pressed, and the palette
        // is already visible. Without this, the button toggles off even though
        // the palette remains up.
        setPaletteOn(true);
    }

    protected void hidePalette() {
        layer.hidePalette();
    }

    /**
     * ActionListener interface.
     * 
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {

        if (e.getSource().equals(paletteButton)) {
            layerName.doClick();
            showPalette();
        } else if (e.getSource().equals(onoffButton)) {
            layerName.doClick();
            // layer is selected, add it to or remove it from map
            if (layerHandler != null) {
                logger.fine("LayerPane|actionPerformed calling layerHandler.turnLayerOn()");
                layerHandler.turnLayerOn(onoffButton.isSelected(), layer);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Layer "
                        + layer.getName()
                        + (layer.isVisible() ? " is visible."
                                : " is NOT visible"));
            }
        } else if (e.getSource().equals(layerName)) {
            setSelected(layerName.isSelected());
        }
    }

    /**
     * Invoked when component has been resized.
     */
    public void componentResized(ComponentEvent e) {}

    /**
     * Invoked when component has been moved.
     */
    public void componentMoved(ComponentEvent e) {}

    /**
     * Invoked when component has been shown.
     */
    public void componentShown(ComponentEvent e) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("layer pane for " + layer.getName()
                    + " receiving componentShown event");
        }

        Component comp = e.getComponent();
        if (comp == null) {
        } else if (comp == layer) {
            if (isLayerOn() != true) {
                setLayerOn(true);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("layer " + layer.getName() + " is now visible.");
                }
            }
        } else if (comp == layer.getPalette()) {
            setPaletteOn(true);
        }
    }

    /**
     * Invoked when component has been hidden.
     */
    public void componentHidden(ComponentEvent e) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("layer pane for " + layer.getName()
                    + " receiving componentHidden event");
        }
        Component comp = e.getComponent();

        if (comp == layer) {
            if (isLayerOn() != false) {
                setLayerOn(false);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("layer " + layer.getName() + " is now hidden.");
                }
            }
        } else if (comp == layer.getPalette()) {
            setPaletteOn(false);
        } else if (comp == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("layer " + layer.getName() + " is now hidden.");
            }
        }
    }

    public static LayerPane getBackgroundLayerSeparator(String title) {
        return new LayerPane(title);
    }
}
