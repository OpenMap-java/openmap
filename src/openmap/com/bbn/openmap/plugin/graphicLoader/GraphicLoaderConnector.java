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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/graphicLoader/GraphicLoaderConnector.java,v $
// $RCSfile: GraphicLoaderConnector.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 20:35:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.graphicLoader;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.graphicLoader.GraphicLoader;
import com.bbn.openmap.plugin.PlugInLayer;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The GraphicLoaderConnector is a MapHandler membership listener,
 * looking for GraphicLoaders without receivers. This class uses the
 * MapHandler to find GraphicLoaders, and requires the LayerHandler to
 * be added to the MapHandler, also.
 * <P>
 * 
 * If the GraphicLoaderConnector finds a GraphicLoader that is not * hooked up to a receiver, it creates a GraphicLoaderPlugIn and
 * PlugInLayer, and adds the PlugInLayer to the LayerHandler. This
 * causes the LayerHandler to add the layer to the application. If the
 * GraphicLoaderConnector doesn't have a handle to the LayerHandler
 * when it finds a GraphicLoader, it adds the PlugInLayer it created
 * to an internal list to add to the LayerHandler when the connector
 * finds one.
 */
public class GraphicLoaderConnector extends OMComponent {

    protected LayerHandler layerHandler = null;
    protected int newLayerIndex = 0; // On Top by default
    protected boolean newLayerVisible = true; // Make new PlugInLayers
                                              // visible.
    protected List orphanGraphicLoaderPlugIns = null;
    public final static String NewLayerIndexProperty = "newLayerIndex";
    public final static String NewLayerVisibleProperty = "newLayerVisible";

    public GraphicLoaderConnector() {}

    /**
     * Set the index of any new layers to be added to the
     * LayerHandler. Negative numbers put the layer on top of the map.
     */
    public void setNewLayerIndex(int i) {
        newLayerIndex = i;
    }

    public int getNewLayerIndex() {
        return newLayerIndex;
    }

    /**
     * Set whether the new layers should initially be visible when
     * they are added to the map.
     */
    public void setNewLayerVisible(boolean set) {
        newLayerVisible = set;
    }

    public boolean setNewLayerVisible() {
        return newLayerVisible;
    }

    /**
     * Set the LayerHandler to be notified with any new PlugIn layers
     * containing the new GraphicLoaderPlugIns.
     */
    public void setLayerHandler(LayerHandler lh) {
        layerHandler = lh;
        if (orphanGraphicLoaderPlugIns != null) {
            if (Debug.debugging("glc")) {
                Debug.output("GraphicLoaderConnector: have LayerHandler, adding PlugInLayers from orphaned GraphicLoaders");
            }
            Iterator it = orphanGraphicLoaderPlugIns.iterator();
            while (it.hasNext()) {
                layerHandler.addLayer((PlugInLayer) it.next(), newLayerIndex);
            }
            orphanGraphicLoaderPlugIns = null;
        }
    }

    public LayerHandler getLayerHandler() {
        return layerHandler;
    }

    /**
     * Check to see if the GraphicLoader already has a receiver set
     * inside it. If it doesn't call hookUpGraphicLoaderWithLayer();
     */
    public void checkGraphicLoader(GraphicLoader gl) {
        if (gl.getReceiver() == null) {
            hookUpGraphicLoaderWithLayer(gl);
        }
    }

    /**
     * Assumes that the GraphicLoader doesn't already have a receiver.
     * Creates a GraphicLoaderPlugIn, and a PlugInLayer, and hooks
     * everything up. Then hands the PlugInLayer to the LayerHandler
     * to get set on the map.
     */
    public void hookUpGraphicLoaderWithLayer(GraphicLoader gl) {
        if (gl != null) {
            GraphicLoaderPlugIn glpi = new GraphicLoaderPlugIn();
            gl.setReceiver(glpi);
            glpi.setGraphicLoader(gl);
            LayerHandler lh = getLayerHandler();
            PlugInLayer pl = new PlugInLayer();
            pl.setPlugIn(glpi);
            pl.setName(gl.getName());
            pl.setVisible(newLayerVisible);
            if (lh != null) {
                lh.addLayer(pl, newLayerIndex);
            } else {
                // If we haven't seen the LayerHandler yet, add the
                // PlugInLayer to a list that we can use later when
                // the LayerHandler is found.
                if (orphanGraphicLoaderPlugIns == null) {
                    orphanGraphicLoaderPlugIns = new LinkedList();
                }
                orphanGraphicLoaderPlugIns.add(pl);
            }
        }
    }

    /**
     * Find GraphicLoaders and LayerHandler in the MapHandler.
     */
    public void findAndInit(Object obj) {
        if (obj instanceof GraphicLoader) {
            checkGraphicLoader((GraphicLoader) obj);
        }

        if (obj instanceof LayerHandler) {
            Debug.message("graphicLoader",
                    "GraphicLoaderConnector found a LayerHandler.");
            setLayerHandler((LayerHandler) obj);
        }
    }

    public void findAndUndo(Object obj) {
        if (obj instanceof LayerHandler) {
            Debug.message("graphicLoader",
                    "GraphicLoaderConnector removing a LayerHandler.");
            LayerHandler lh = getLayerHandler();
            if (lh != null && lh == (LayerHandler) obj) {
                setLayerHandler(null);
            }
        }
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        newLayerIndex = PropUtils.intFromProperties(props, prefix
                + NewLayerIndexProperty, newLayerIndex);

        newLayerVisible = PropUtils.booleanFromProperties(props, prefix
                + NewLayerVisibleProperty, newLayerVisible);

    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + NewLayerIndexProperty,
                Integer.toString(newLayerIndex));
        props.put(prefix + NewLayerVisibleProperty,
                new Boolean(newLayerVisible).toString());
        return props;
    }

    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.put(NewLayerIndexProperty,
                "The new layer index, where it should be added to the map. (0 on top)");
        list.put(NewLayerVisibleProperty,
                "Whether a new layer should initially be visible");
        list.put(NewLayerVisibleProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return list;
    }
}