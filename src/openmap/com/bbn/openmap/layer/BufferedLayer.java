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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/BufferedLayer.java,v $
// $RCSfile: BufferedLayer.java,v $
// $Revision: 1.7 $
// $Date: 2004/09/17 19:34:33 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;
import java.util.*;
import javax.swing.*;

/**
 * A BufferedLayer is a layer that buffers a group of layers into an
 * image.  When this layer repaints, the image gets rendered.  This
 * layer can be used to group a set of layers into one, and was
 * designed with the idea that it is a background layer where a more
 * animated layer would be on top of it. <P>
 *
 * This layer contains a MapBean, and any layer that gets added to it
 * simply gets added to the MapBean.  When a layer needs to redraw
 * itself, it can act normally, and the BufferedLayer will get updated
 * as needed.  If the MapBean is a BufferedMapBean (which it is by
 * default), then the layers will get buffered into an image. <P>
 *
 * There are some special considerations to think about when using
 * this layer if the background is at all transparent.  The image
 * buffer will need to be recreated at certain times in order to
 * prevent leftover images from the previous paintings.  When the
 * background is set for the layer, the transparency is tested if the
 * background is a Color and the setHasTransparentBackground() method
 * is called accordingly.  If a different Paint object is set in the
 * BufferedLayer, it's up to you to set this variable.  This causes a
 * new image to be created every time a new projection is provided to
 * the layer.  If the layers added to this BufferedLayer are active,
 * meaning that their content could change between projection changes,
 * you should set the hasActiveLayers flag to true.  this causes a new
 * image buffer to be created every time a layer repaints itself.
 * Again, this is only important if the background color of the layer
 * is transparent.<P>
 * 
 * The BufferedLayer can be configured in the openmap.properties file:
 * <pre>
 *
 * bufLayer.class=com.bbn.openmap.layer.BufferedLayer
 * bufLayer.prettyName=My Layer Group
 * bufLayer.layers=layer1 layer2 layer3
 * bufLayer.visibleLayers=layer1 layer3
 * bufLayer.hasActiveLayers=false
 *
 * </pre>
 * layer1, layer2, etc should be defined as any other openmap layer.
 */
public class BufferedLayer extends Layer implements PropertyChangeListener {

    public final static String LayersProperty = "layers";
    public final static String VisibleLayersProperty = "visibleLayers";
    public final static String HasActiveLayersProperty = "hasActiveLayers";

    /**
     * Used to recreate the buffer on every repaint() call made by
     * every layer.  Makes for a lot of image buffer creation.  If the
     * layers may call repaint() and change what they present between
     * projection changes, then this needs to be set to true.
     * Otherwise, the old graphics will still be visible.  This only
     * needs to be set if the background is at all transparent.  If
     * the background of the internal MapBean is opaque, set this to
     * false, which is the default.
     */
    protected boolean hasActiveLayers = false;

    /**
     * Used to tell the BufferedLayer that the background is
     * transparent.  Will cause a new image buffer to be created when
     * the projection changes, in order to cover up what was already
     * there.  This is set to true but default, since the internal
     * MapBean color is set to OMColor.clear.
     */
    protected boolean hasTransparentBackground = true;

    /**
     * The MapBean used as the group organized.  If this is a
     * BufferedMapBean, the layer will provide a buffered image.
     */
    MapBean mapBean;

    public BufferedLayer() {
        this.setLayout(new BorderLayout());

        // Adds the mapbean to the layer
        MapBean mb = new BLMapBean(this);

        // Add it the layer properly...
        setMapBean(mb);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        hasActiveLayers = LayerUtils.booleanFromProperties(props, prefix + HasActiveLayersProperty, hasActiveLayers);

        Vector layersValue = PropUtils.parseSpacedMarkers(props.getProperty(prefix + LayersProperty));
        Vector startuplayers = PropUtils.parseSpacedMarkers(props.getProperty(prefix + VisibleLayersProperty));

        Layer[] layers = LayerHandler.getLayers(layersValue, startuplayers, props);

        for (int i = 0; i < layers.length; i++) {
            mapBean.add(layers[i]);
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        StringBuffer layersListProperty = new StringBuffer();
        StringBuffer startupLayersListProperty = new StringBuffer();

        Component[] comps = mapBean.getComponents();
        for (int i = 0; i < comps.length; i++) {
            // they have to be layers
            Layer layer = (Layer)comps[i];
            String lPrefix = layer.getPropertyPrefix();
            boolean unsetPrefix = false;
            if (lPrefix == null) {
                lPrefix = "layer" + i;
                // I think we need to do this, in order to get proper
                // scoping in the properties.  We'll unset it later...
                layer.setPropertyPrefix(lPrefix);
                unsetPrefix = true;
            }
            layersListProperty.append(" " + lPrefix);

            if (layer.isVisible()) {
                startupLayersListProperty.append(" " + lPrefix);
            }

            Debug.output("BufferedLayer: getting properties for " + layer.getName() + " " + layer.getProperties(new Properties()));

            layer.getProperties(props);

            if (unsetPrefix) {
                layer.setPropertyPrefix(null);
            }
        }

        props.put(prefix + LayersProperty, layersListProperty.toString());
        props.put(prefix + VisibleLayersProperty, startupLayersListProperty.toString());
        props.put(prefix + HasActiveLayersProperty, new Boolean(hasActiveLayers).toString());

        return props;
    }

    /**
     * Not really implemented, because the mechanism for providing a
     * set of properties that let you add a variable number of new
     * objects as children to this one.
     */
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        


        return props;
    }

    /**
     * If true, will cause a new image buffer to be recreated for
     * every layer.repaint() call.  Should only be set to true if the
     * background is at all transparent, and if the layers could
     * change between projection changes.
     */
    public void setHasActiveLayers(boolean value) {
        hasActiveLayers = value;
    }

    public boolean getHasActiveLayers() {
        return hasActiveLayers;
    }

    /**
     * If true, will create a new image buffer when the projection
     * changes.  Should be set to true if the background has any
     * transparency.
     */
    public void setHasTransparentBackground(boolean value) {
        hasTransparentBackground = value;
    }

    public boolean getHasTransparentBackground() {
        return hasTransparentBackground;
    }

    /**
     * Remove all layers from the group.
     */
    public void clearLayers() {
        Component[] layers = getLayers();
        if (layers != null && layers.length > 0) {
            for (int i = 0; i < layers.length; i++) {
                removeLayer((Layer)layers[i]);
            }
        }

        resetPalette();
    }

    /**
     * Method for BeanContextChild interface. Gets an iterator from
     * the BeanContext to call findAndInit() over.  Sets BeanContext on sub-layers.
     */
    public void setBeanContext(BeanContext in_bc) 
        throws PropertyVetoException {
        super.setBeanContext(in_bc);

        Component[] layers = getLayers();
        if (layers != null && layers.length > 0) {
            for (int i = 0; i < layers.length; i++) {
                ((Layer)layers[i]).setBeanContext(in_bc);
            }
        }
    }

    /**
     * Add a layer to the group.  Sets the BeanContext on the added layer.
     */
    public void addLayer(Layer layer) {
        mapBean.add(layer);
        try {
            layer.setBeanContext(getBeanContext());
        } catch (PropertyVetoException nve) {
        }
        resetPalette();
    }

    /**
     * Remove the layer from group.
     */
    public void removeLayer(Layer layer) {
        mapBean.remove(layer);
        resetPalette();
    }

    /**
     * Return if there is at least one layer assigned to the group.
     */
    public boolean hasLayers() {
        return (mapBean.getComponentCount() > 0);
    }

    /**
     * Get the layers assigned to the internal MapBean.
     * @return a Component[].
     */
    public Component[] getLayers() {
        return mapBean.getComponents();
    }

    public void firePaletteEvent(ComponentEvent event) {
        super.firePaletteEvent(event);
        hasActiveLayers = (event.getID() == ComponentEvent.COMPONENT_SHOWN);
    }

    /**
     *  You can change what kind of MapBean is used to hold onto the
     *  layers.  This method just sets the new MapBean into the layer,
     *  as is.  If there was a previous MapBean with layers, they're
     *  gone and replaces with whatever is attached to the new
     *  MapBean.
     *
     * @param mb new MapBean
     */
    public void setMapBean(MapBean mb) {
        if (mapBean != null) {
            remove(mapBean);
        }

        mapBean = mb;
        add(mapBean, BorderLayout.CENTER);
    }

    /**
     * Get the current MapBean used in the BufferedLayer.
     * @return MapBean
     */
    public MapBean getMapBean() {
        return mapBean;
    }

    /**
     * Set the background color of the group.  Actually sets the
     * background color of the projection used by the internal
     * MapBean, and which then forces a repaint() on it.
     *
     * @param color java.awt.Color.  
     */
    public void setBackground(Color color) {
        setBckgrnd(color);
    }

    /**
     * Set the background paint of the group.  Actually sets the
     * background paint of the projection used by the internal
     * MapBean.
     *
     * @param paint java.awt.Paint
     */
    public void setBckgrnd(Paint paint) {
        mapBean.setBckgrnd(paint);

        if (paint instanceof Color) {
            setHasTransparentBackground(((Color)paint).getAlpha() < 255);
        } else {
            // then we don't know, assume it is.
            setHasTransparentBackground(true);
        }
    }

    /**
     * Get the background color of the image.  Actually returns the
     * background color of the projection of the internal MapBean.
     *
     * @return color java.awt.Color  
     */
    public Color getBackground() {
        return mapBean.getBackground();
    }

    /**
     * Get the background Paint object used for the internal MapBean.
     * @return java.awt.Paint
     */
    public Paint getBckgrnd(Paint paint) {
        return mapBean.getBckgrnd();
    }

    /**
     * Part of the Layer/ProjectionListener thing.  The internal
     * MapBean's projection gets set here, which forces the group
     * layers to receive it as well.
     */
    public void projectionChanged(com.bbn.openmap.event.ProjectionEvent pe) {
        Projection proj = setProjection(pe);

        if (proj != null && mapBean instanceof BLMapBean && hasTransparentBackground) {
            ((BLMapBean)mapBean).wipeImage();
        }
        mapBean.setProjection(proj);
    }

    /**
     * The GUI panel.
     */
    JPanel panel = null;

    /**
     * Should be called if layers are added or removed from the
     * buffer.
     */
    public void resetPalette() {
        panel = null;
        super.resetPalette();
    }

    /**
     * Get the GUI (palettes) for the layers.  The BufferedLayer
     * actually creates a JTabbedPane holding the palettes for all of
     * its layers, and also has a pane for itself that provides
     * visibility control for the group layers.
     */
    public Component getGUI() {
        if (panel == null) {
            Component[] layerComps = getLayers();

            panel = new JPanel();
            JTabbedPane tabs = new JTabbedPane();

            JPanel bfPanel = new JPanel();
            bfPanel.setLayout(new BoxLayout(bfPanel, BoxLayout.Y_AXIS));
            bfPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // LEFT
            bfPanel.setAlignmentY(Component.CENTER_ALIGNMENT); // BOTTOM
            tabs.addTab("Layer Visibility", bfPanel);

            for (int i = 0; i < layerComps.length; i++) {
                Layer layer = (Layer)layerComps[i];
                Component layerGUI = layer.getGUI();
                if (layerGUI != null) {
                    tabs.addTab(layer.getName(), layerGUI);
                }

                VisHelper layerVisibility = new VisHelper(layer);
                bfPanel.add(layerVisibility);
            }
            panel.add(tabs);
        }
        return panel;
    }

    public void paint(Graphics g) {
        if (hasLayers()) {
            super.paint(g);
        }
    }

    /**
     *  Class that helps track turning on/off layers in the buffered layer.
     */
    protected class VisHelper extends JCheckBox implements ActionListener {
        Layer layer;
        public VisHelper(Layer l) {
            super(l.getName(), l.isVisible());
            super.addActionListener(this);
            layer = l;
        }

        public void actionPerformed(ActionEvent ae) {
            layer.setVisible(((JCheckBox)ae.getSource()).isSelected());
            if (Debug.debugging("bufferedlayer")) {
                Debug.output("Turning " + layer.getName() + (((JCheckBox)ae.getSource()).isSelected()?" on":" off"));
            }

            if (mapBean instanceof BLMapBean && hasTransparentBackground) {
                ((BLMapBean)mapBean).wipeImage();
            }

            layer.repaint();
        }
    }

    /**
     * Part of the ProjectionPainter interface.  The group layers are
     * given the projection and the graphics to paint into.
     */
    public void renderDataForProjection(Projection proj, Graphics g) {
        Component[] layersComps = mapBean.getComponents();
        
        for (int i = layersComps.length - 1; i >= 0; i--) {
            Layer layer = (Layer) layersComps[i];
            layer.renderDataForProjection(proj, g);
        }
    }

    /**
     * PropertyChangeListener method, to listen for the source map's
     * background changes.  Act on if necessary.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName() == MapBean.BackgroundProperty) {
            mapBean.setBckgrnd((Paint)pce.getNewValue());
        }
    }

    /**
     * An simple extension of the BufferedMapBean that calls a layer,
     * presumably its parent, to call repaint().  This is necessary in
     * order to make sure Swing calls paint properly.  Only repaint()
     * is overridden in this class over a standard BufferedMapBean.
     */
    public class BLMapBean extends BufferedMapBean {
        /** The layer to call back. */
        Layer layer;
        
        /**
         * @param parent the parent layer of this MapBean.
         */
        public BLMapBean(Layer parent) {
            super();
            background = OMColor.clear;
            layer = parent;
        }

        /**
         * Set the buffer dirty, and call repaint on the layer.
         */
        public void repaint() {
            setBufferDirty(true);

            if (Debug.debugging("bufferedlayer")) {
                Debug.output("BLMapBean.repaint() has active layers = " + hasActiveLayers);
            }

            if (hasActiveLayers && hasTransparentBackground) {
                wipeImage();
            }

            if (layer != null) {
                layer.repaint();
            }
        }

        /**
         * We need this because if the background to the BufferedLayer
         * is clear, we need to clear out anything that was previously
         * there. This seems to be the only way to do it.
         */
        public void wipeImage() {
            setBufferDirty(true);

            // Need to do some optimization for this to figure out
            // which is really faster, recreating a new image, or
            // cycling though the pixels.  Plus, the image should be
            // reset if the background is slighly transparent if any
            // layer can change between overall cleansings.

            if (this.getBackground() == OMColor.clear) {
                drawingBuffer = createImage(this.getWidth(), this.getHeight());
            }
        }

        /**
         * We need the buffer to be able to be transparent.
         */
        public Image createImage(int width, int height) {
            if (Debug.debugging("bufferedlayer")) {
                Debug.output("BLMapBean.createImage()");
            }

            if (width <= 0) width = 1;
            if (height <= 0) height = 1;

            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
    }
}
