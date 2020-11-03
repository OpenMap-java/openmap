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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/BufferedLayer.java,v $
// $RCSfile: BufferedLayer.java,v $
// $Revision: 1.13 $
// $Date: 2008/10/01 15:26:30 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * A BufferedLayer is a layer that buffers a group of layers into an image. When
 * this layer repaints, the image gets rendered. This layer can be used to group
 * a set of layers into one, and was designed with the idea that it is a
 * background layer where a more animated layer would be on top of it.
 * <P>
 *
 * This layer contains a MapBean, and any layer that gets added to it simply
 * gets added to the MapBean. When a layer needs to redraw itself, it can act
 * normally, and the BufferedLayer will get updated as needed. If the MapBean is
 * a BufferedMapBean (which it is by default), then the layers will get buffered
 * into an image.
 * <P>
 *
 * The BufferedLayer can be configured in the openmap.properties file:
 *
 * <pre>
 *
 *
 *  bufLayer.class=com.bbn.openmap.layer.BufferedLayer
 *  bufLayer.prettyName=My Layer Group
 *  bufLayer.layers=layer1 layer2 layer3
 *  bufLayer.visibleLayers=layer1 layer3
 * </pre>
 *
 * layer1, layer2, etc should be defined as any other openmap layer.
 */
public class BufferedLayer extends OMGraphicHandlerLayer implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    public final static String LayersProperty = "layers";
    public final static String VisibleLayersProperty = "visibleLayers";

    /**
     * Used to tell the BufferedLayer that the background is transparent. Will
     * cause a new image buffer to be created when the projection changes, in
     * order to cover up what was already there. This is set to true but
     * default, since the internal MapBean color is set to OMColor.clear.
     */
    protected boolean hasTransparentBackground = true;

    /**
     * The MapBean used as the group organized. If this is a BufferedMapBean,
     * the layer will provide a buffered image.
     */
    MapBean mapBean;

    public BufferedLayer() {
        this.setLayout(new BorderLayout());

        // Adds the mapbean to the layer
        MapBean mb = new BLMapBean();

        // Add it the layer properly...
        setMapBean(mb);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        PropUtils.putDataPrefixToLayerList(this, props, prefix + LayersProperty);

        List<String> layersValue = PropUtils.parseSpacedMarkers(props.getProperty(prefix + LayersProperty));
        List<String> startuplayers = PropUtils.parseSpacedMarkers(props.getProperty(prefix + VisibleLayersProperty));

        Layer[] layers = LayerHandler.getLayers(layersValue, startuplayers, props);

        for (Layer layer : layers) {
            mapBean.add(layer);
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
            Layer layer = (Layer) comps[i];
            String lPrefix = layer.getPropertyPrefix();
            boolean unsetPrefix = false;
            if (lPrefix == null) {
                lPrefix = "layer" + i;
                // I think we need to do this, in order to get proper
                // scoping in the properties. We'll unset it later...
                layer.setPropertyPrefix(lPrefix);
                unsetPrefix = true;
            }
            layersListProperty.append(" ").append(lPrefix);

            if (layer.isVisible()) {
                startupLayersListProperty.append(" ").append(lPrefix);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("BufferedLayer: getting properties for " + layer.getName() + " "
                        + layer.getProperties(new Properties()));
            }

            layer.getProperties(props);

            if (unsetPrefix) {
                layer.setPropertyPrefix(null);
            }
        }

        props.put(prefix + LayersProperty, layersListProperty.toString());
        props.put(prefix + VisibleLayersProperty, startupLayersListProperty.toString());

        return props;
    }

    /**
     * Not really implemented, because the mechanism for providing a set of
     * properties that let you add a variable number of new objects as children
     * to this one.
     */
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        return props;
    }

    /**
     * If true, will create a new image buffer when the projection changes.
     * Should be set to true if the background has any transparency.
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
                removeLayer((Layer) layers[i]);
            }
        }

        resetPalette();
    }

    /**
     * Method for BeanContextChild interface. Gets an iterator from the
     * BeanContext to call findAndInit() over. Sets BeanContext on sub-layers.
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        super.setBeanContext(in_bc);

        Component[] layers = getLayers();
        if (layers != null && layers.length > 0) {
            for (int i = 0; i < layers.length; i++) {
                ((Layer) layers[i]).setBeanContext(in_bc);
            }
        }
    }

    /**
     * Add a layer to the group. Sets the BeanContext on the added layer.
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
     *
     * @return a Component[].
     */
    public Component[] getLayers() {
        return mapBean.getComponents();
    }

    /**
     * Returns true if all children are are ready to be painted - that is, they
     * have called for a repaint.
     * 
     * @return the readyToPaint
     */
    public boolean isReadyToPaint() {
        for (Component c : getLayers()) {
            if (!((Layer) c).isReadyToPaint()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set whether this layer should be ready to paint. Called when the
     * projection changes, to make child layers 'dirty'. We don't change the
     * super classes' version of the variable, because it's never consulted and
     * it's probably better that it's always true by default.
     * 
     * @param readyToPaint the readyToPaint to set
     */
    public void setReadyToPaint(boolean readyToPaint) {
        for (Component c : getLayers()) {
            ((Layer) c).setReadyToPaint(readyToPaint);
        }
    }

    /**
     * You can change what kind of MapBean is used to hold onto the layers. This
     * method just sets the new MapBean into the layer, as is. If there was a
     * previous MapBean with layers, they're gone and replaces with whatever is
     * attached to the new MapBean.
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
     *
     * @return MapBean
     */
    public MapBean getMapBean() {
        return mapBean;
    }

    /**
     * Set the background color of the group. Actually sets the background color
     * of the projection used by the internal MapBean, and which then forces a
     * repaint() on it.
     *
     * @param color java.awt.Color.
     */
    public void setBackground(Color color) {
        setBckgrnd(color);
    }

    /**
     * Set the background paint of the group. Actually sets the background paint
     * of the projection used by the internal MapBean.
     *
     * @param paint java.awt.Paint
     */
    public void setBckgrnd(Paint paint) {
        mapBean.setBckgrnd(paint);

        if (paint instanceof Color) {
            setHasTransparentBackground(((Color) paint).getAlpha() < 255);
        } else {
            // then we don't know, assume it is.
            setHasTransparentBackground(true);
        }
    }

    /**
     * Get the background color of the image. Actually returns the background
     * color of the projection of the internal MapBean.
     *
     * @return color java.awt.Color
     */
    public Color getBackground() {
        return mapBean.getBackground();
    }

    /**
     * Get the background Paint object used for the internal MapBean.
     *
     * @return java.awt.Paint
     */
    public Paint getBckgrnd(Paint paint) {
        return mapBean.getBckgrnd();
    }

    /**
     * We don't want a projection change policy called here, because that will
     * call for a repaint. We want the buffered layers to call for a repaint if
     * necessary.
     */
    @Override
    public void projectionChanged(ProjectionEvent pevent) {
        // Just pass it on, let the layers decide if they have to update.
        mapBean.setProjection(pevent.getProjection());
    }

    /**
     * Called when the layer is removed from the MapBean, and after the
     * projection changes. Lets the layer know to release memory that might not
     * be needed.
     */
    @Override
    public void removed(Container cont) {
        Component[] layers = getLayers();
        if (layers != null && layers.length > 0) {
            for (int i = 0; i < layers.length; i++) {
                ((Layer) layers[i]).remove(cont);
            }
        }
    }

    /**
     * The GUI panel.
     */
    JPanel panel = null;

    /**
     * Should be called if layers are added or removed from the buffer.
     */
    public void resetPalette() {
        panel = null;
        super.resetPalette();
    }

    /**
     * Get the GUI (palettes) for the layers. The BufferedLayer actually creates
     * a JTabbedPane holding the palettes for all of its layers, and also has a
     * pane for itself that provides visibility control for the group layers.
     */
    public Component getGUI() {
        if (panel == null) {
            Component[] layerComps = getLayers();

            panel = new JPanel();
            GridBagLayout pGridbag = new GridBagLayout();
            GridBagConstraints pC = new GridBagConstraints();
            pC.fill = GridBagConstraints.BOTH;
            pC.weightx = 1.0f;
            pC.weighty = 1.0f;
            panel.setLayout(pGridbag);

            JTabbedPane tabs = new JTabbedPane();
            pGridbag.setConstraints(tabs, pC);
            panel.add(tabs);

            JPanel bfPanel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0.0f;
            c.weighty = 0.0f;
            c.anchor = GridBagConstraints.CENTER;
            c.gridwidth = GridBagConstraints.REMAINDER;
            bfPanel.setLayout(gridbag);

            tabs.addTab("Layer Visibility", bfPanel);

            for (int i = 0; i < layerComps.length; i++) {
                Layer layer = (Layer) layerComps[i];
                Component layerGUI = layer.getGUI();
                if (layerGUI != null) {
                    tabs.addTab(layer.getName(), layerGUI);
                }

                VisHelper layerVisibility = new VisHelper(layer);
                gridbag.setConstraints(layerVisibility, c);
                bfPanel.add(layerVisibility);
            }
        }
        return panel;
    }

    public void paint(Graphics g) {
        if (hasLayers()) {
            mapBean.paintChildren(g);
        }
        mapBean.paintPainters(g);
    }

    /**
     * Class that helps track turning on/off layers in the buffered layer.
     */
    protected class VisHelper extends JCheckBox implements ActionListener {

        private static final long serialVersionUID = 1L;
        Layer layer;

        public VisHelper(Layer l) {
            super(l.getName(), l.isVisible());
            super.addActionListener(this);
            layer = l;
        }

        public void actionPerformed(ActionEvent ae) {
            layer.setVisible(((JCheckBox) ae.getSource()).isSelected());
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Turning " + layer.getName()
                        + (((JCheckBox) ae.getSource()).isSelected() ? " on" : " off"));
            }

            layer.repaint();
        }
    }

    /**
     * Part of the ProjectionPainter interface. The group layers are given the
     * projection and the graphics to paint into.
     */
    public void renderDataForProjection(Projection proj, Graphics g) {
        Component[] layersComps = mapBean.getComponents();

        for (int i = layersComps.length - 1; i >= 0; i--) {
            Layer layer = (Layer) layersComps[i];
            if (layer.isVisible()) {
                layer.renderDataForProjection(proj, g);
            }
        }
    }

    /**
     * PropertyChangeListener method, to listen for the source map's background
     * changes. Act on if necessary.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName() == MapBean.BackgroundProperty) {
            mapBean.setBckgrnd((Paint) pce.getNewValue());
        }
    }

    public void dispose() {
        if (mapBean != null) {
            mapBean.dispose();
        }

        if (panel != null) {
            panel.removeAll();
            panel = null;
        }
    }

    public void setBufferDirty(boolean value) {
        mapBean.setBufferDirty(value);
    }

    /**
     * An simple extension of the BufferedMapBean that calls a layer, presumably
     * its parent, to call repaint(). This is necessary in order to make sure
     * Swing calls paint properly. Only repaint() is overridden in this class
     * over a standard BufferedMapBean.
     */
    public class BLMapBean extends BufferedMapBean {

        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public BLMapBean() {
            super(false);
            background = OMColor.ALMOST_CLEAR;
        }

        /**
         * For the Buffered Layer MapBean, the background color is always clear.
         * Let the layers add the color...
         *
         * @return color java.awt.Color.
         */
        public Color getBackground() {
            return OMColor.ALMOST_CLEAR;
        }

        /**
         * Set the buffer dirty, and call repaint on the layer.
         */
        public void repaint(Layer layer) {
            super.setBufferDirty(true);
            BufferedLayer.this.repaint();
        }

        /**
         * Fine-tuned for the purposes of a BufferedLayer - just create a new
         * image if the buffer is dirty.
         */
        public void paintChildren(Graphics g, Rectangle clip) {
            BufferedImage localDrawingBuffer = drawingBuffer;
            if (bufferDirty) {

                // Reset will clear out the pixels if the size of the buffer is
                // appropriate for the projection
                localDrawingBuffer = resetDrawingBuffer(localDrawingBuffer, getProjection());

                // We need to draw the projection background with oh-so-slight
                // transparent rect to allow semi-transparent layers to render
                // properly.

                drawProjectionBackground(localDrawingBuffer.getGraphics());
                /**
                 * We used to call paintLayers here.
                 */
                // paintLayers(localDrawingBuffer.getGraphics());
                /**
                 * But, there are problems with rendering if the projection
                 * changes quickly (rotation, following a track, etc) and
                 * there's an animated layer active. Calling
                 * renderDataForProjection on the background layer eliminates
                 * flickering with background layers and the background color.
                 * Sometimes having a BufferedImageRenderPolicy on some of these
                 * background layers also causes flashing. Since the
                 * BufferedLayer provides buffering, those layers can have a
                 * StandardRenderPolicy.
                 */
                renderDataForProjection(getProjection(), localDrawingBuffer.getGraphics());

                // Reassign the drawingBuffer if a new buffer was allocated.
                drawingBuffer = localDrawingBuffer;
            }

            if (localDrawingBuffer != null) {
                g.drawImage(localDrawingBuffer, 0, 0, null);
            }

        }

        /**
         * @return the expanded rotated projection if map rotated, normal
         *         projection if not rotated. The rotated projection is larger
         *         than the MapBean and has extra offsets.
         */
        public Projection getRotatedProjection() {
            RotationHelper rotation = getUpdatedRotHelper();
            Projection proj = rotation != null ? rotation.getProjection() : projection;
            /**
             * The original method in the superclass sets the rotation angle on
             * the projection, but the rotation in this map bean is 0, so
             * calling this just messes up rotation settings in the projection,
             * stomping it to zero. This has the effect of breaking
             * anti-rotation in OMText and OMScalingIcons.
             */
            // ((Proj) proj).setRotationAngle(getRotationAngle());
            return proj;
        }

        /**
         * We don't want the BLMapBean to be hanging on to removed layers,
         * that'll be done at a higher level.
         */
        public void componentRemoved(ContainerEvent e) {
            super.componentRemoved(e);
            removedLayers.clear();
        }
    }
}