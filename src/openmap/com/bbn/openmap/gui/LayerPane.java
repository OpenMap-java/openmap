// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayerPane.java,v $
// $RCSfile: LayerPane.java,v $
// $Revision: 1.6 $
// $Date: 2004/01/26 18:18:07 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.accessibility.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.LayerSupport;
import com.bbn.openmap.util.Assert;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/** 
 * A LayerPane is a single instance of how a layer represented in the
 * LayersPanel. It contains three widgets: an on/off button; a palette
 * button; and a toggle button with the layer name. <P>
 */
public class LayerPane extends JPanel 
    implements Serializable, ActionListener, ComponentListener {

    protected transient JCheckBox onoffButton;

    // Next line uncommented for toggle button
//      protected transient JCheckBox paletteButton;
    // Comment next line out for toggle button
    protected transient JButton paletteButton;
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

    // default initializations
    static {
        url1 = LayerPane.class.getResource("PaletteOff.gif");
        paletteIcon = new ImageIcon(url1, "palette");
        url2 = LayerPane.class.getResource("PaletteOn.gif");
        paletteOnIcon = new ImageIcon(url2, "palette on");

        url3 = LayerPane.class.getResource("BulbOn.gif");
        layerOnIcon = new ImageIcon(url3, "layer selected");
        url4 = LayerPane.class.getResource("BulbOff.gif");
        layerOffIcon = new ImageIcon(url4, "layer not selected");
    }

    /**
     *  @param layer the layer to be represented by the pane.
     *  @param bg the buttongroup for the layer
     *  @param layerHandler the LayerHandler that contains information
     *  about the Layers. 
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

        onoffButton = new JCheckBox(layerOffIcon);
        onoffButton.setSelectedIcon(layerOnIcon);
        onoffButton.setActionCommand(toggleLayerCmd);
        onoffButton.addActionListener(this);
        onoffButton.setToolTipText("Turn " + layer.getName() + " layer on/off");

        // Determine if this layer has already been activated 
        onoffButton.setSelected(layer.isVisible());

        // add the palette show/hide checkbutton
//      paletteButton = new JCheckBox(paletteIcon);
//      paletteButton.setSelected(false);
//      paletteButton.setSelectedIcon(paletteOnIcon);
//      paletteButton.setToolTipText("Display/Hide tools for " 
//                                   + layer.getName() + " layer");

        paletteButton = new JButton(paletteIcon);
        paletteButton.setBorderPainted(false);
        if (layer.getGUI() == null) {
            paletteButton.setEnabled(false);
            paletteButton.setToolTipText("No tools available for " 
                                         + layer.getName() + " layer");
        } else {
            paletteButton.setToolTipText("Display tools for " 
                                         + layer.getName() + " layer");
        }

        paletteButton.setActionCommand(showPaletteCmd);
        paletteButton.addActionListener(this);

        layerName = new JToggleButton(layer.getName());
        layerName.setBorderPainted(false);
        layerName.addActionListener(this);
        offColor = layerName.getBackground();
        layerName.setToolTipText("Click to select layer");
        layerName.setHorizontalAlignment(SwingConstants.LEFT);
        bg.add(layerName);

        c.gridy = 0;
        c.gridx = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints(onoffButton, c);
        add(onoffButton);
        gridbag.setConstraints(paletteButton, c);
        add(paletteButton);

        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(1, 2, 1, 15);
        gridbag.setConstraints(layerName, c);
        add(layerName);
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
     * Same as cleanup, except the layer name toggle button gets
     * removed from the given button group.
     */
    public void cleanup(ButtonGroup bg) {
        if (bg != null) {
            bg.remove(layerName);
        }
        cleanup();
    }

    /**
     * LayerPane disconnects from listeners, nulls out components.
     */
    public void cleanup() {
        if (layer != null) {
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
     *  @return whether the layer is on
     */ 
    public boolean isLayerOn() {
        return onoffButton.isSelected();
    }
    /**
     *  Turns the button on or off
     */
    public void setLayerOn(boolean value) {
        onoffButton.setSelected(value);
    }

    /**
     *  @return whether the palette for this layer is on
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
     *  @return the status of the layerName toggle button
     */
    public boolean isSelected() { 
        return layerName.isSelected(); 
    }

    /**
     *  Highlights/unhighlights the layerName toggle button
     */
    public void setSelected(boolean select) {
        layerName.setSelected(select);

        String command = select?LayersPanel.LayerSelectedCmd:LayersPanel.LayerDeselectedCmd;

        if (Debug.debugging("layercontrol")) {
            Debug.output("LayerPane for " + getLayer().getName() +
                         " " + command + ", firing event");
        }

        firePropertyChange(command, null, getLayer());
    }
    
    /**
     * @return the layer represented by this LayerPane
     */
    public Layer getLayer() {
        return layer;
    }

    public void finalize() {
        if (Debug.debugging("gc")) {
            Debug.output("LayerPane getting GC'd");
        }
    }

    /** 
     * Tell the pane to check with the layer to get the current layer
     * name for it's label.  
     */
    public void updateLayerLabel() {
        layerName.setText(getLayer().getName());
    }

    protected void showPalette() {
        layer.showPalette();
    }
    
    
    protected void hidePalette() {
        layer.hidePalette();
    }
    
    /**
     * ActionListener interface.
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {

        if (e.getSource().equals(paletteButton)){
            setSelected(true);

            // This for a JButton control
            paletteButton.setIcon(paletteOnIcon);
            showPalette();

        } else if (e.getSource().equals(onoffButton)) {
            setSelected(true);
            // layer is selected, add it to or remove it from map
            if (layerHandler != null) {
                Debug.message("layerspanel","LayerPane|actionPerformed calling layerHandler.turnLayerOn()");
                layerHandler.turnLayerOn(onoffButton.isSelected(), layer);
            }

            if (Debug.debugging("layerspanel")){
                Debug.output("LayerPane: Layer " + layer.getName() + 
                             (layer.isVisible()?" is visible.":" is NOT visible"));
            }
        } else if (e.getSource().equals(layerName)) {
            setSelected(true);
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
        if (Debug.debugging("layerspanel")){
            Debug.output("LayerPane: layer pane for " + layer.getName() +
                         " receiving componentShown event");
        }

                Component comp = e.getComponent();
        if (comp == null) {
        } else if (comp == layer){
            if (isLayerOn() != true) {
                setLayerOn(true);
                if (Debug.debugging("layerspanel")){
                    Debug.output("LayerPane: layer " + layer.getName() +
                                 " is now visible.");
                }
            }
        } else if (comp == layer.getPalette()) {
            // Next line uncommented for toggle button
//          paletteButton.setSelected(true);
            // Comment next line out for toggle button
            paletteButton.setIcon(paletteOnIcon);
        }
    }

    /**
     * Invoked when component has been hidden.
     */
    public void componentHidden(ComponentEvent e) {
        if (Debug.debugging("layerspanel")){
            Debug.output("LayerPane: layer pane for " + layer.getName() +
                         " receiving componentHidden event");
        }
        Component comp = e.getComponent();

        if (comp == layer) {
            if (isLayerOn() != false){
                setLayerOn(false);
                if (Debug.debugging("layerspanel")){
                    Debug.output("LayerPane: layer " + layer.getName() +
                                 " is now hidden.");
                }
            }
        } else if (comp == layer.getPalette()) {
            // Next line uncommented for toggle button action
//          paletteButton.setSelected(false);
            // Comment next line out for toggle button action
            paletteButton.setIcon(paletteIcon);
        } else if (comp == null) {
            if (Debug.debugging("layerspanel")){
                Debug.output("LayerPane: layer " + layer.getName() +
                             " is now hidden.");
            }
        }
    }

    protected static LayerPane backgroundLayerSeparator;

    public static LayerPane getBackgroundLayerSeparator(String title) {
        if (backgroundLayerSeparator == null) {
            backgroundLayerSeparator = new LayerPane(title);
        }
        return backgroundLayerSeparator;
    }
}
