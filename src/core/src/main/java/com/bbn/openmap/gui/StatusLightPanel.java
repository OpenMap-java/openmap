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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/StatusLightPanel.java,v $
// $RCSfile: StatusLightPanel.java,v $
// $Revision: 1.7 $
// $Date: 2009/02/26 21:16:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.LayerStatusListener;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 */
public class StatusLightPanel extends OMComponentPanel implements
        LayerStatusListener, PropertyChangeListener, PropertyConsumer {

    protected int numlayers = 0;
    protected MapBean map;
    protected Layer[] layers;
    protected Hashtable<Layer, JButton> statusLights = new Hashtable<Layer, JButton>();
    protected JComponent container = null;

    protected final static transient URL greyURL = StatusLightPanel.class.getResource("grey.gif");
    public final static transient ImageIcon greyIcon = new ImageIcon(greyURL, "unknown");

    protected final static transient URL redURL = StatusLightPanel.class.getResource("red.gif");
    public final static transient ImageIcon redIcon = new ImageIcon(redURL, "working");

    protected final static transient URL greenURL = StatusLightPanel.class.getResource("green.gif");
    public final static transient ImageIcon greenIcon = new ImageIcon(greenURL, "stable");

    protected boolean waitingForLayers = false;
    protected boolean showWaitCursor = false;

    /**
     * Flag to make the status light buttons act as triggers to bring
     * up the layer palettes.
     */
    protected boolean lightTriggers = true;
    public final static String LightTriggersProperty = "triggers";

    public final static String defaultKey = "StatusLightPanel";
    protected String key = defaultKey;

    public StatusLightPanel() {
        super();
        setLayout(new GridLayout(1, 0));
        reset();
    }

    public void setMap(MapBean map) {
        if (this.map != null) {
            this.map.removePropertyChangeListener(this);
        }

        this.map = map;

        if (map != null) {
            map.addPropertyChangeListener(this);
        }
    }

    public MapBean getMap() {
        return map;
    }

    /**
     * Listen for the layer changes within the MapBean, to display the
     * status lights for each layer.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == MapBean.LayersProperty) {
            listenToLayers((Layer[]) evt.getNewValue());
        }
    }

    /**
     * Method to add the StatusLightPanel as an Information Display
     * Event listener to a list of layers. Should not be called
     * directly, because it is called as a result of the property
     * change (layers) of the MapBean.
     */
    protected void listenToLayers(Layer[] newLayers) {
        if (SwingUtilities.isEventDispatchThread()) {
            listenToLayersFromEDT(newLayers);
        } else {
            SwingUtilities.invokeLater(new MyWorker(newLayers));
        }
    }
    
    protected void listenToLayersFromEDT(Layer[] newLayers) {
        int i;

        if (layers != null) {
            for (i = 0; i < layers.length; i++) {
                if (layers[i] != null) {
                    layers[i].removeLayerStatusListener(this);

                    boolean stillOnMap = false;
                    for (int j = 0; j < newLayers.length; j++) {
                        if (layers[i] == newLayers[j]) {
                            stillOnMap = true;
                        }
                    }
                    if (!stillOnMap) {
                        JButton light = (JButton) statusLights.get(layers[i]);
                        light.removeActionListener(layers[i]);
                        statusLights.remove(layers[i]);
                    }

                    layers[i] = null;
                }
            }
        }

        if (newLayers != null) {
            for (i = 0; i < newLayers.length; i++) {
                // Could call addLayer(newLayers[i] instead, but I
                // don't want to call setStatusBar unnecessarily.
                if (newLayers[i] != null) {
                    newLayers[i].addLayerStatusListener(this);
                    getStatusLightForLayer(newLayers[i]);
                } else {
                    Debug.message("statuslights",
                            "StatusLightPanel: null layer in new layer array");
                }
            }
            layers = newLayers;
            reset();
        }
    }

    /**
     * This method is really a get and set. The JLabel status gif is
     * returned out of the HashTable for the layer. If the layer isn't
     * in the HashTable, the new light is created, and a tooltip for
     * it is set.
     * 
     * @param layer the layer for the needed light.
     * @return JLabel representing the status of the layer.
     */
    protected JButton getStatusLightForLayer(Layer layer) {
        if (layer == null) {
            return null;
        }

        JButton newLight = statusLights.get(layer);

        if (newLight == null) {
            //          newLight = new JButton(greyIcon);
            newLight = new JButton(greenIcon);
            newLight.setToolTipText(layer.getName());
            newLight.setMargin(new Insets(2, 1, 2, 1));
            statusLights.put(layer, newLight);

            if (lightTriggers) {
                newLight.setActionCommand(Layer.DisplayPaletteCmd);
                newLight.addActionListener(layer);
                newLight.setBorderPainted(true);
            } else {
                newLight.setBorderPainted(false);
            }
        }
        return newLight;
    }

    /**
     * The method that updates the StatusLight display with the
     * correct layer status representation.
     */
    public void reset() {
        removeAll();
        if (container != null) {
            container.removeAll();
        }

        if (lightTriggers) {
//            if (container == null) {
//                container = new JToolBar();
//                container.setFloatable(false);
//            }
            
            if (container == null) {
                container = new JPanel();
                container.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
            }
            
            add(container);
        } else {
            container = null;
        }

        if (layers != null) {
            for (int i = 0; i < layers.length; i++) {
                JButton statusgif = getStatusLightForLayer(layers[i]);
                if (statusgif != null) {
                    if (lightTriggers) {
                        container.add(statusgif);
                    } else {
                        add(statusgif);
                    }
                }
            }
        }
        revalidate();
    }

    /**
     * Set the light in the window to be a certain color, depending on
     * the working status. If the layer light isn't stored, the whole
     * thing is blown off. If the icon is red, then the watch cursor
     * is requested, if allowed by showWaitCursor.
     * 
     * @param layer the layer to update.
     * @param icon the icon light representing the status.
     */
    protected void setLayerStatus(Layer layer, Icon icon) {
        JButton statusgif = statusLights.get(layer);
        if (statusgif != null) {
            statusgif.setIcon(icon);

            if (this.map != null) {
                if (icon == redIcon && showWaitCursor) {
                    //                  this.map.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    waitingForLayers = true;
                } else if (icon == greenIcon) {
                    waitingForLayers = false;
                    for (JButton light : statusLights.values()) {
                        if (light.getIcon() == redIcon) {
                            waitingForLayers = true;
                        }
                    }

                    //                  if (!waitingForLayers) {
                    //                      if (currentMapBeanCursor != null) {
                    //                          this.map.setCursor(currentMapBeanCursor);
                    //                      } else {
                    //                          resetCursor();
                    //                      }
                    //                  }
                }
            }
        }
    }

    ///////////////////////////////////////////
    //  LayerStatusListener interface

    /**
     * Update the Layer status.
     * 
     * @param evt LayerStatusEvent
     */
    public void updateLayerStatus(LayerStatusEvent evt) {
        switch (evt.getStatus()) {
        // these need to be coordinated correctly by the Layer,
        // otherwise
        // we'll get phantom status ticks or maybe an ArrayOutOfBounds
        // negative...
        case LayerStatusEvent.START_WORKING:
            setLayerStatus((Layer) evt.getSource(), redIcon);
            break;
        case LayerStatusEvent.STATUS_UPDATE:
            break;
        case LayerStatusEvent.FINISH_WORKING:
            setLayerStatus((Layer) evt.getSource(), greenIcon);
            break;
        default:
            System.err.println("InformationDelegator.updateLayerStatus(): "
                    + "unknown status: " + evt.getStatus());
            break;
        }
    }

    /**
     * Called when an object has been added to the BeanContext. The
     * InformationDelegator will look for certain objects it needs.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MapBean) {
            setMap((MapBean) someObj);
        }
    }

    /**
     * Called when an object is being removed from the BeanContext.
     * Will cause the object to be disconnected from the
     * InformationDelegator if it is being used.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapBean) {
            setMap(null);
        }
    }

    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        setLightTriggers(PropUtils.booleanFromProperties(props, prefix
                + LightTriggersProperty, lightTriggers));
    }

    public void setLightTriggers(boolean set) {
        lightTriggers = set;
        statusLights.clear();
        reset();
    }

    public boolean getLightTriggers() {
        return lightTriggers;
    }

    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + LightTriggersProperty,
                new Boolean(lightTriggers).toString());
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                StatusLightPanel.class,
                LightTriggersProperty,
                "Enable Triggers",
                "Layer status indicators should launch layer palettes.",
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
                listenToLayersFromEDT(layers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

