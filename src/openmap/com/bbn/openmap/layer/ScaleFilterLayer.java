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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/ScaleFilterLayer.java,v $
// $RCSfile: ScaleFilterLayer.java,v $
// $Revision: 1.14 $
// $Date: 2008/09/26 12:07:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bbn.openmap.Layer;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.InfoDisplayListener;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.LayerStatusListener;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * An OpenMap Layer that encapsulates other layers and acts as a scale filter.
 * It will delegate responsibility to one of several layers depending on the
 * scale.
 * <p>
 * To use this layer, list it as a layer in the openmap.properties file in the
 * openmap.layers properties, as you would add any other layer. Then, add these
 * properties to the openmap.properties file. The layers added to the
 * ScaleFilterLayer do not get added to the openmap.layers property, but instead
 * get added to the scaledFilterLayer.layers property listed here. Then, the
 * properties for these layers are added to the openmap.properties file like any
 * other layer. <BR>
 * The properties for this layer look like this: <BR>
 * <BR>
 * <code><pre>
 *       
 *        #######################################
 *        # Properties for ScaleFilterLayer
 *        #######################################
 *        scaledFilterLayer.class=com.bbn.openmap.layer.ScaleFilterLayer
 *        scaledFilterLayer.prettyName=&amp;ltPretty name used on menu&amp;ge
 *        # List 2 or more layers, larger scale layers first
 *        scaledFilterLayer.layers=layer_1 layer_2 layer_3 ...
 *        # List the transition scales to switch between layers
 *        scaledFilterLayer.transitionScales= (transition scale from layer 1 to 2) (transition scale from layer 2 to 3) (...)
 *        #######################################
 *        
 * </pre></code>
 */
public class ScaleFilterLayer
        extends Layer
        implements InfoDisplayListener, LayerStatusListener, PropertyChangeListener, MapMouseListener {

    /**
     * The layers property.
     */
    public final static transient String layersProperty = "layers";

    /**
     * The transition scales property.
     */
    public final static transient String transitionScalesProperty = "transitionScales";

    /**
     * The layers.
     */
    protected Vector<Layer> layers;

    /**
     * The transition scales.
     */
    protected float[] transitionScales;

    /**
     * The default transition scale.
     */
    protected float defaultTransitionScale = 40000000f;

    /**
     * The index of the currently selected layer.
     */
    protected int targetIndex = -1;
    
    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.ScaleFilterLayer");

    /**
     * Initializes an empty layer.
     */
    public ScaleFilterLayer() {
        // Setting the overlay layout seemed like a good idea at the time, but
        // it introduces a strange bug where the bounds of the layer get set
        // between the center of the map and the lower left corner. This only
        // happens for the BufferedLayerMapBean, when there are buffered layers
        // active. Very strange, but not setting an overlay seems to work OK,
        // too.
        // setLayout(new OverlayLayout(this));

        // To get MouseDelegator, to make decisions on receiving mouse
        // modes for child layers.
        setAddToBeanContext(true);
    }

    /**
     * Get the Vector holding the Layers. If it hasn't been asked for yet, a
     * new, empty Vector will be returned, one that will be used internally.
     */
    public Vector<Layer> getLayers() {
        if (layers == null) {
            layers = new Vector<Layer>();
        }
        return layers;
    }

    /**
     * Get the transition scales used to set the active layer.
     */
    public float[] getTransitionScales() {
        return transitionScales;
    }

    /**
     * Programmatic way to set layers and scales. There should be one more layer
     * on the list than there is scale in the float array. Layers that should be
     * displayed for larger scale numbers (smaller scale) should be at the front
     * of the Vector list, and larger numbers should be at the front of the
     * scale array. For scale numbers larger than the first number in the array,
     * the first layer will be displayed. As the scale number decreases, other
     * layers will be displayed.
     * 
     * @param list Vector of layers
     * @param scales Array of transition scales.
     */
    public void setLayersAndScales(Vector<Layer> list, float[] scales) {
        layers = list;
        transitionScales = scales;
    }

    /**
     * Initializes this layer from the given properties.
     * 
     * @param props the <code>Properties</code> holding settings for this layer
     */
    public void setProperties(String prefix, Properties props) {
        // Clear out layer and scale state
        setLayersAndScales(null, null);

        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        parseLayers(prefix, props);
        parseScales(prefix, props);

        // Update our target layer. If there is a current projection and this
        // layer is active, we need to pass it along.
        if (getProjection() != null) {
            Layer currentLayer = configureAppropriateLayer(getProjection().getScale());
            fireStatusUpdate(LayerStatusEvent.START_WORKING);
            currentLayer.projectionChanged(new ProjectionEvent((Object) null, getProjection()));
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        float[] ts = getTransitionScales();
        StringBuffer tsBuffer = new StringBuffer();
        if (ts != null) {
            for (int i = 0; i < ts.length; i++) {
                tsBuffer.append(Float.toString(ts[i])).append(" ");
            }
        }
        props.put(prefix + transitionScalesProperty, tsBuffer.toString());

        StringBuffer layerBuffer = new StringBuffer();
        for (Layer layer : getLayers()) {
            layerBuffer.append(layer.getPropertyPrefix()).append(" ");
            layer.getProperties(props);
        }
        props.put(prefix + layersProperty, layerBuffer.toString());

        return props;
    }

    /**
     * Get the layer that's appropriate at the current scale. The targetedIndex
     * needs to be set before this is called. The targetedIndex is the index to
     * the layers array representing the current layer.
     * 
     * @return Layer
     */
    public Layer getAppropriateLayer() {
        try {
            return getLayers().elementAt(targetIndex);
        } catch (NullPointerException npe) {
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }

        return SinkLayer.getSharedInstance();
    }

    /**
     * Create the Layers from a property value string.
     * 
     * @param prefix String
     * @param props Properties
     */
    protected void parseLayers(String prefix, Properties props) {
        PropUtils.putDataPrefixToLayerList(this, props, prefix + layersProperty);

        String layersString = props.getProperty(prefix + layersProperty);
        Vector<Layer> layers = getLayers();
        if (layersString == null || layersString.length() == 0) {
            logger.info("ScaleFilterLayer(): null layersString!");
            return;
        }
        StringTokenizer tok = new StringTokenizer(layersString);
        while (tok.hasMoreTokens()) {
            Object obj;
            String layerName = tok.nextToken();
            String classProperty = layerName + ".class";
            String className = props.getProperty(classProperty);
            if (className == null) {
                logger.info("ScaleFilterLayer.parseLayers(): Failed to locate property \"" + classProperty + "\"");
                logger.info("ScaleFilterLayer.parseLayers(): Skipping layer \"" + layerName + "\"");
                className = SinkLayer.class.getName();
            }

            try {
                if (className.equals(SinkLayer.class.getName())) {
                    obj = SinkLayer.getSharedInstance();
                } else {
                    obj = Class.forName(className).newInstance();
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Instantiated " + className);
                }
            } catch (Exception e) {
                logger.info("Failed to instantiate \"" + className + "\": " + e);
                obj = SinkLayer.getSharedInstance();
            }

            // create the layer and set its properties
            if (obj instanceof Layer) {
                Layer l = (Layer) obj;
                l.setProperties(layerName, props);
                l.addLayerStatusListener(this);
                l.addInfoDisplayListener(this);
                layers.addElement(l);
            }
        }
    }

    /**
     * Create the transition scales from a property value string. If there are N
     * layers, there should be N-1 transition scales.
     * 
     * @param prefix String
     * @param props Properties
     */
    protected void parseScales(String prefix, Properties props) {
        StringTokenizer tok = null;
        Vector<Layer> layers = getLayers();
        int size = layers.size();
        if (size > 0) {
            --size;
        }
        transitionScales = new float[size];

        String scales = props.getProperty(prefix + transitionScalesProperty);
        if (scales == null) {
            logger.info("Failed to locate property \"" + transitionScalesProperty + "\"");
            if (transitionScales.length > 0) {
                transitionScales[0] = defaultTransitionScale;
            }
            for (int i = 1; i < transitionScales.length; i++) {
                transitionScales[i] = transitionScales[i - 1] / 3;
            }
            return;
        }

        try {
            tok = new StringTokenizer(scales);
            transitionScales[0] = (tok.hasMoreTokens()) ? new Float(tok.nextToken()).floatValue() : defaultTransitionScale;
        } catch (NumberFormatException e) {
            logger.info("ScaleFilterLayer.parseScales()1: " + e);
            transitionScales[0] = defaultTransitionScale;
        }

        if (tok != null) {
            for (int i = 1; i < transitionScales.length; i++) {
                try {
                    transitionScales[i] =
                            (tok.hasMoreTokens()) ? new Float(tok.nextToken()).floatValue() : transitionScales[i - 1] / 3;
                } catch (NumberFormatException e) {
                    logger.info("ScaleFilterLayer.parseScales()2: " + e);
                    transitionScales[i] = transitionScales[i - 1] / 3;
                }
            }
        }
    }

    /**
     * Implementing the ProjectionPainter interface.
     */
    public synchronized void renderDataForProjection(Projection proj, java.awt.Graphics g) {
        if (proj == null) {
            logger.info("null projection!");
            return;
        } else {
            setTargetIndex(proj.getScale());
            Layer layer = getAppropriateLayer();
            layer.renderDataForProjection(proj, g);
        }
    }

    /**
     * Calculate the index of the target layer. If there are N layers, there are
     * N-1 transitionScales. The ith layer is chosen if the scale is greater
     * than the ith transitionScale.
     * 
     * @param scale the current map scale
     * @return true if the targetIndex has changed as a result of the new scale.
     */
    public boolean setTargetIndex(float scale) {
        boolean changed = false;
        float[] target = transitionScales;

        int i = 0;
        if (target != null) {
            for (i = 0; i < target.length; i++) {
                if (scale > target[i]) {
                    break;
                }
            }
        }

        if (targetIndex != i) {
            changed = true;
        }
        targetIndex = i;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ScaleFilterLayer(" + getName() + ") targetIndex: " + targetIndex + ", changed: " + changed);
        }

        return changed;
    }

    /**
     * Handles projection change notification events. Throws out old graphics,
     * and requests new graphics from the spatial index based on the bounding
     * rectangle of the new <code>Projection</code>.
     * 
     * @param ev the new projection event
     */
    public void projectionChanged(ProjectionEvent ev) {
        // Lets the ScaleFilterLayer remember the projection, just in case.
        setProjection(ev);

        Projection proj = ev.getProjection();
        // get the appropriate layer and invoke projectionChanged
        Layer layer = configureAppropriateLayer(proj.getScale());

        fireStatusUpdate(LayerStatusEvent.START_WORKING);
        layer.projectionChanged(ev);
    }

    protected Layer configureAppropriateLayer(float scale) {
        Layer currentLayer = getAppropriateLayer();
        boolean changed = setTargetIndex(scale);

        // get the appropriate layer and invoke projectionChanged
        Layer layer = getAppropriateLayer();
        if (changed) {
            currentLayer.removeNotify();
            setPaletteTab(targetIndex);
            remove(currentLayer);

            // This will handle the repaint() requests from the
            // layer...
            add(layer);
            layer.addNotify();
            checkMouseMode();
        }

        return layer;
    }

    /**
     * Renders the scale-appropriate layer on the map.
     * 
     * @param g a graphics context
     */
    public void paint(Graphics g) {
        getAppropriateLayer().paint(g);
        fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
    }

    /**
     * Try to handle receiving LayerStatusEvents from child layers. May not
     * always work, depending on what thread sends/receives this event - usually
     * in the Swing thread, and the GUI can't always be updated as expected.
     * 
     * @param evt LayerStatusEvent
     */
    public void updateLayerStatus(LayerStatusEvent evt) {
        fireStatusUpdate(evt);
    }

    protected JPanel panel = null;
    protected JTabbedPane tabs = null;

    /**
     * Get the GUI (palettes) for the layers. The BufferedLayer actually creates
     * a JTabbedPane holding the palettes for all of its layers, and also has a
     * pane for itself that provides visibility control for the group layers.
     */
    public Component getGUI() {
        if (panel == null) {

            Iterator<Layer> it = getLayers().iterator();
            panel = new JPanel();
            tabs = new JTabbedPane();

            // bfPanel still needs controls for controlling scales,
            // etc, showing which one is showing, etc., as well as
            // some indication as which layer is currently active.

            JPanel bfPanel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            bfPanel.setLayout(gridbag);

            tabs.addTab("Scale Filter Controls", bfPanel);

            JButton gotoButton = new JButton("Go to Active Layer Tab");
            gotoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setPaletteTab(targetIndex);
                }
            });

            c.gridy = 0;
            gridbag.setConstraints(gotoButton, c);
            bfPanel.add(gotoButton);

            while (it.hasNext()) {
                Layer layer = it.next();
                Component layerGUI = layer.getGUI();
                if (layerGUI != null) {
                    tabs.addTab(layer.getName(), layerGUI);
                } else {
                    tabs.addTab(layer.getName(), getEmptyGUIFiller(layer));
                }
            }
            panel.add(tabs);
        }
        setPaletteTab(targetIndex);
        return panel;
    }

    public Component getEmptyGUIFiller(Layer layer) {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JPanel panel = new JPanel();
        panel.setLayout(gridbag);

        JLabel label = new JLabel("No properties available for");
        JLabel label2 = new JLabel("the " + layer.getName() + ".");

        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(label, c);
        gridbag.setConstraints(label2, c);
        panel.add(label);
        panel.add(label2);
        return panel;
    }

    protected void setPaletteTab(int layerIndex) {
        Vector<Layer> layers = getLayers();
        if (layers.size() > layerIndex && tabs != null && layerIndex < tabs.getTabCount()) {
            // +1 because the first tab is the ScaleFilterLayer tab
            tabs.setSelectedIndex(layerIndex + 1);
        }
    }

    // //////////////////////////////
    // InfoDisplayListener Methods
    // //////////////////////////////

    /**
     * Request to have a URL displayed in a Browser.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestURL(InfoDisplayEvent event) {
        fireRequestURL(new InfoDisplayEvent(this, event.getInformation()));
    }

    /**
     * Request to have a message displayed in a dialog window.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestMessage(InfoDisplayEvent event) {
        fireRequestMessage(new InfoDisplayEvent(this, event.getInformation()));
    }

    /**
     * Request to have an information line displayed in an application status
     * window.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestInfoLine(InfoDisplayEvent event) {
        fireRequestInfoLine(new InfoDisplayEvent(this, event.getInformation()));
    }

    /**
     * Request that plain text or html text be displayed in a browser.
     * 
     * @param event InfoDisplayEvent
     */
    public void requestBrowserContent(InfoDisplayEvent event) {
        fireRequestBrowserContent(new InfoDisplayEvent(this, event.getInformation()));
    }

    /**
     * Request that the MapBean cursor be set to a certain type.
     * 
     * @param cursor java.awt.Cursor to set over the MapBean.
     */
    public void requestCursor(java.awt.Cursor cursor) {
        fireRequestCursor(cursor);
    }

    /**
     * Request a tool tip be shown.
     * 
     * @param event The InfoDisplayEvent containing the text and requestor.
     */
    public void requestShowToolTip(InfoDisplayEvent event) {
        fireRequestToolTip(new InfoDisplayEvent(this, event.getInformation()));
    }

    /**
     * Request a tool tip be hidden.
     */
    public void requestHideToolTip() {
        fireHideToolTip();
    }

    /**
     * Try to handle mouse events for the current layer.
     */
    public synchronized MapMouseListener getMapMouseListener() {
        return this;
    }

    /** The current active mouse mode ID. */
    protected String mmID = null;
    /**
     * Flag to specify that the current layer wants events from the current
     * active mouse mode.
     */
    protected boolean coolMM = false;
    /**
     * The current MapMouseListener from the currently appropriate layer.
     */
    protected MapMouseListener clmml = null; // current layer map mouse listener

    /**
     * Set the coolMM flag, whenever the scale-appropriate layer changes, or if
     * the active mouse mode changes.
     */
    public synchronized boolean checkMouseMode() {
        // check the current MouseMode with the current layer
        coolMM = false;
        Layer layer = getAppropriateLayer();
        MapMouseListener mml = layer.getMapMouseListener();
        setCurrentLayerMapMouseListener(mml);
        if (mml != null) {
            String[] mmsl = mml.getMouseModeServiceList();
            for (int i = 0; i < mmsl.length; i++) {
                if (mmsl[i].intern() == mmID) {
                    coolMM = true;
                    break;
                }
            }
        }
        return coolMM;
    }

    /**
     * Pre-set the MapMouseListener to received events if the current layer
     * wants them.
     */
    public void setCurrentLayerMapMouseListener(MapMouseListener mml) {
        clmml = mml;
    }

    /**
     * Get the MapMouseListener to received events if the current layer wants
     * them. May be null, but coolMM should be false in that case.
     */
    public MapMouseListener getCurrentLayerMapMouseListener() {
        return clmml;
    }

    /**
     * Listen for changes to the active mouse mode and for any changes to the
     * list of available mouse modes
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == MouseDelegator.ActiveModeProperty) {
            mmID = ((MapMouseMode) evt.getNewValue()).getID().intern();
            checkMouseMode();
        }
    }

    /**
     * Return a list of the modes that are interesting to the MapMouseListener.
     * The source MouseEvents will only get sent to the MapMouseListener if the
     * mode is set to one that the listener is interested in. Layers interested
     * in receiving events should register for receiving events in "select"
     * mode: <code>
     * <pre>
     * return new String[] { SelectMouseMode.modeID };
     * </pre>
     * <code>
     * 
     * @return String[] of modeID's
     * @see com.bbn.openmap.event.NavMouseMode#modeID
     * @see com.bbn.openmap.event.SelectMouseMode#modeID
     * @see com.bbn.openmap.event.NullMouseMode#modeID
     */
    public String[] getMouseModeServiceList() {
        HashSet<String> mmsl = new HashSet<String>();
        Iterator<Layer> it = getLayers().iterator();
        while (it.hasNext()) {
            Layer l = (Layer) it.next();
            MapMouseListener mml = l.getMapMouseListener();
            if (mml != null) {
                String[] llist = mml.getMouseModeServiceList();
                for (int i = 0; i < llist.length; i++) {
                    mmsl.add(llist[i].intern());
                }
            }
        }
        String[] rets = new String[mmsl.size()];
        return mmsl.toArray(rets);
    }

    // Mouse Listener events
    // //////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mousePressed(MouseEvent e) {
        if (coolMM) {
            return getCurrentLayerMapMouseListener().mousePressed(e);
        } else {
            return false;
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseReleased(MouseEvent e) {
        if (coolMM) {
            return getCurrentLayerMapMouseListener().mouseReleased(e);
        } else {
            return false;
        }
    }

    /**
     * Invoked when the mouse has been clicked on a component. The listener will
     * receive this event if it successfully processed
     * <code>mousePressed()</code>, or if no other listener processes the event.
     * If the listener successfully processes <code>mouseClicked()</code>, then
     * it will receive the next <code>mouseClicked()</code> notifications that
     * have a click count greater than one.
     * <p>
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseClicked(MouseEvent e) {
        if (coolMM) {
            return getCurrentLayerMapMouseListener().mouseClicked(e);
        } else {
            return false;
        }

    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        if (coolMM) {
            getCurrentLayerMapMouseListener().mouseEntered(e);
        }

    }

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
        if (coolMM) {
            getCurrentLayerMapMouseListener().mouseExited(e);
        }
    }

    // Mouse Motion Listener events
    // /////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * The listener will receive these events if it successfully processes
     * mousePressed(), or if no other listener processes the event.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseDragged(MouseEvent e) {
        if (coolMM) {
            return getCurrentLayerMapMouseListener().mouseDragged(e);
        } else {
            return false;
        }
    }

    /**
     * Invoked when the mouse button has been moved on a component (with no
     * buttons down).
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseMoved(MouseEvent e) {
        if (coolMM) {
            return getCurrentLayerMapMouseListener().mouseMoved(e);
        } else {
            return false;
        }

    }

    /**
     * Handle a mouse cursor moving without the button being pressed. This event
     * is intended to tell the listener that there was a mouse movement, but
     * that the event was consumed by another layer. This will allow a mouse
     * listener to clean up actions that might have happened because of another
     * motion event response.
     */
    public void mouseMoved() {
        if (coolMM) {
            getCurrentLayerMapMouseListener().mouseMoved();
        }
    }

    /** Method for BeanContextChild interface. */
    public void setBeanContext(BeanContext in_bc)
            throws PropertyVetoException {

        for (Layer layer : getLayers()) {
            // You don't actually want to add the layer to the
            // BeanContext, because then the LayerHandler will pick it
            // up and add it to the main list of layers.

            layer.connectToBeanContext(in_bc);
        }

        super.setBeanContext(in_bc);
    }

    public void dispose() {
        try {
            for (Layer layer : getLayers()) {
                layer.disconnectFromBeanContext();
            }
        } catch (PropertyVetoException pve) {

        }

        BeanContext bc = getBeanContext();
        if (bc instanceof MapHandler) {
            MapHandler mh = (MapHandler) bc;
            findAndUndo(mh.get(MouseDelegator.class));
        }
        super.dispose();
    }

    /**
     * MapHandler child methods, passing found objects to child layers.
     */
    public void findAndInit(Object obj) {
        super.findAndInit(obj);

        if (obj instanceof MouseDelegator) {
            ((MouseDelegator) obj).addPropertyChangeListener(this);
        }

        for (Layer layer : getLayers()) {
            layer.findAndInit(obj);
        }
    }

    /**
     * MapHandler child methods, passing removed objects to child layers.
     */
    public void findAndUndo(Object obj) {
        super.findAndUndo(obj);

        if (obj == null) {
            return;
        }

        if (obj instanceof MouseDelegator) {
            ((MouseDelegator) obj).removePropertyChangeListener(this);
        }

        for (Layer layer : getLayers()) {
            layer.findAndUndo(obj);
        }
    }

}