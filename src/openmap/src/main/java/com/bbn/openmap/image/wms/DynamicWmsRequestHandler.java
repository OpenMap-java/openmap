/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/DynamicWmsRequestHandler.java,v 1.2 2008/01/29 22:04:13 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.util.Debug;

/**
 * @version $Header:
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap
 *          /wms/DynamicWmsRequestHandler.java,v 1.1 2006/03/21 10:27:54 tomrak
 *          Exp $
 * @author Adam Dawidowski
 * @author wachu
 * @author pitek
 */
public class DynamicWmsRequestHandler
        extends WmsRequestHandler {

    // Handle the dynamic and static layers
    final LayerHandler mapLayerHandler;

    // layer names loaded from the file properties
    private final String staticLayersNames;

    public DynamicWmsRequestHandler(String scheme, String hostName, int port, String path, Properties props,
                                    LayerHandler mapLayerHandler)
            throws IOException, WMSException {
        super(scheme, hostName, port, path, props);
        this.mapLayerHandler = mapLayerHandler;
        // remember the name of the static layer that, when not to UpdateLayer y
        // retrieved from LayerHandlera
        staticLayersNames = props.getProperty("openmap.layers");
    }

    /**
     * export method used to read the new target data, in order to upload newly
     * created layers of graphics (using layerHandlera) on under the assumed
     * adowach earlier data for the layers with static data, ie maps of the
     * ports, are out to date, and only s remaining layer (read from
     * LayerHandler) and updated added to the array layers. Table stores layers
     * layers used by the image server to image generation.
     */
    public void updateLayers() {
        Debug.message("imageserver", "OGCMRH: updateLayers updating...");
        if (layers == null) {
            layers = new Layer[0];
            Debug.message("imageserver", "OGCMRH: updateLayers layers==null, so create new layer.");
        }
        if (getLayerHandler() == null) {
            Debug.message("imageserver", "OGCMRH: updateLayers: LayerHandler is null.");
            return;
        }
        Layer[] newLayers = getLayerHandler().getLayers();
        if (layers.length < newLayers.length) {
            // here come only once, when
            // first add layers of
            // targets Gdynia, Hel ...
            // vector at the encoder is used only when it will switch the new
            // layer (layers is a normal array and you can not enlarge it, so
            // use vector which later changed to an array)
            Debug.message("ms", "OGCserver: updateLayers: NEW LAYERS COMES.");

            List tmpLayers = new ArrayList();
            for (int j = 0; j < layers.length; j++) {
                /* add the old layers (static, washers ad maps) */
                tmpLayers.add(layers[j]);
            }

            for (int i = 0; i < newLayers.length; i++) {
                /*
                 * add new layer (dynamic maps) but no static that's already
                 * added
                 */
                boolean layerAlreadyAdded = false;
                for (int j = 0; j < layers.length; j++) {
                    /*
                     * Check that among the new layer has no chance static layer
                     * (ie the old layers)
                     */
                    String layerName = layers[j].getName();
                    String newLayerName = newLayers[i].getName();
                    if (layerName.trim().equalsIgnoreCase(newLayerName.trim())) {
                        layerAlreadyAdded = true;
                    }
                }
                if (!layerAlreadyAdded) {
                    /*
                     * if the layer is not checked for static (ie, port map) to
                     * j add to vector
                     */
                    tmpLayers.add(newLayers[i]);
                }
            }
            Layer[] newArrLayers = (Layer[]) tmpLayers.toArray(new Layer[tmpLayers.size()]);
            layers = newArrLayers;
            for (int j = 0; j < layers.length; j++) { // dodaj stare warstwy
                                                      // (statyczne, podkad
                                                      // mapy)
                layers[j].setPropertyPrefix(layers[j].getName());
                // Debug.message("imageserver", "OGCMRH: updateLayers NEW LAYERS
                // layer:" + layers[j].getName());
            }
        } else { // update the existing layers
            Debug.message("ms", "OGCserver: updateLayers: UPDATE LAYERS COMES.");
            for (int i = 0; i < newLayers.length; i++) {
                for (int j = 0; j < layers.length; j++) {
                    // Debug.message("imageserver", "OGCMRH: updateLayers layer:
                    // sprawdzam par warstw :>" + newLayers[i].getName()+"< i
                    // >"+layers[j].getName()+"<");
                    if (layers[j].getName().trim().equalsIgnoreCase(newLayers[i].getName().trim())) {
                        // if
                        // (newLayers[i].getName().trim().equalsIgnoreCase("Ports")
                        // ||
                        // newLayers[i].getName().trim().equalsIgnoreCase("PortsGd"))
                        // {
                        String[] oldLayers = staticLayersNames.split(" ");
                        boolean found = false;
                        for (int k = 0; k < oldLayers.length; k++) {
                            if (oldLayers[k].equals(newLayers[i].getName().trim())) {
                                found = true;
                                break;
                            }
                        }
                        // if
                        // (staticLayersNames.contains(newLayers[i].getName().trim()))
                        // {
                        // // Debug.message("imageserver", "OGCMRH:
                        // // updateLayers layer:" + newLayers[i].getName()+"
                        // // nie jest zmieniana ");
                        // } else { // update istniejacej warstwy
                        if (found == false) {
                            layers[j] = newLayers[i];
                            layers[j].setPropertyPrefix(layers[j].getName());
                            // Debug.message("imageserver", "OGCMRH:
                            // updateLayers layer:" + layers[j].getName());
                        }
                    }
                }
            }
        }

        // update map names
        createWmsLayers();
    }

    /**
     * return - calls layer handler
     * 
     * @return LayerHandler
     */
    public LayerHandler getLayerHandler() {
        return this.mapLayerHandler;
    }

}
