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
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap/wms/DynamicWmsRequestHandler.java,v
 *          1.1 2006/03/21 10:27:54 tomrak Exp $
 * @author Adam Dawidowski
 * @author wachu
 * @author pitek
 */
public class DynamicWmsRequestHandler extends WmsRequestHandler {

    // uchwyt na warstwy dynamiczne i statyczne
    final LayerHandler mapLayerHandler;

    // nazwy warstw staycznych (mapa portów), ³adowanych z pliku properties
    private final String staticLayersNames;

    public DynamicWmsRequestHandler(String scheme, String hostName, int port, String path, Properties props,
            LayerHandler mapLayerHandler) throws IOException, WMSException {
        super(scheme, hostName, port, path, props);
        this.mapLayerHandler = mapLayerHandler;
        // zapamiêtaj nazwy warstw statycznych , aby przy UpdateLayer nie by³y
        // one pobierane z LayerHandlera
        staticLayersNames = props.getProperty("openmap.layers");
    }

    /**
     * metoda wywo³ywana po odczycie nowych danych o celach , w celu wgrania
     * nowo utworzonych warstw graficznych (przy wykorzystaniu layerHandlera) na
     * podstawie wczeœniej za³adowach danych o celach przy czym warstwy ze
     * statycznymi danymi tzn mapami portow , nie s¹ uaktualniane, a tylko
     * pozosta³e warstwy (czytane z LayerHandler'a) s¹ uaktualniane b¹dz
     * dodawane do tablicy layers. Tablica layers przechowuje warstwy
     * wykorzystywane przez image serwera do generowania obrazka.
     */
//    public void updateLayers() {
//        Debug.message("imageserver", "OGCMRH: updateLayers updating...");
//        if (layers == null) {
//            layers = new Layer[0];
//            Debug.message("imageserver", "OGCMRH: updateLayers layers==null, so create new layer.");
//        }
//        if (getLayerHandler() == null) {
//            Debug.message("imageserver", "OGCMRH: updateLayers: LayerHandler is null.");
//            return;
//        }
//        Layer[] newLayers = getLayerHandler().getLayers();
//        if (layers.length < newLayers.length) { // tu wejdzie tylko raz , przy
//                                                // pierwszym dodaniu warstw z
//                                                // celami Gdynia, Hel,...
//            // wektor u¿ywany tylko gdy pojawi¹ siê nowe warstwy (layers jest
//            // zwyk³a tablica i nie mo¿na jej powiêkszyæ, wiêc wykorzystuje
//            // wektor który poŸniej zmienie na tablicê)
//            Debug.message("ms", "OGCserver: updateLayers: NEW LAYERS COMES.");
//
//            List tmpLayers = new ArrayList();
//            for (int j = 0; j < layers.length; j++) { // dodaj stare warstwy
//                                                        // (statyczne, podk³ad
//                                                        // mapy)
//                tmpLayers.add(layers[j]);
//            }
//
//            for (int i = 0; i < newLayers.length; i++) { // dodaj nowe
//                                                            // warstwy
//                                                            // (dynamiczne ,
//                                                            // podk³ad mapy) ale
//                                                            // bez ju¿
//                                                            // statycznych które
//                                                            // s¹ ju¿ dodane
//                boolean layerAlreadyAdded = false;
//                for (int j = 0; j < layers.length; j++) { // sprawdz czy wsród
//                                                            // nowych warstw nie
//                                                            // ma przypadkiem
//                                                            // warstwy
//                                                            // statycznej (czyli
//                                                            // starej warstwy)
//                    String layerName = layers[j].getName();
//                    String newLayerName = newLayers[i].getName();
//                    if (layerName.trim().equalsIgnoreCase(newLayerName.trim())) {
//                        layerAlreadyAdded = true;
//                    }
//                }
//                if (!layerAlreadyAdded) { // gdy sprawdzana warstwa nie jest
//                                            // statyczna (tzn mapa portu) to j¹
//                                            // dodaj do wektora
//                    tmpLayers.add(newLayers[i]);
//                }
//            }
//            Layer[] newArrLayers = (Layer[]) tmpLayers.toArray(new Layer[0]);
//            layers = newArrLayers;
//            for (int j = 0; j < layers.length; j++) { // dodaj stare warstwy
//                                                        // (statyczne, podk³ad
//                                                        // mapy)
//                layers[j].setPropertyPrefix(layers[j].getName());
//                // Debug.message("imageserver", "OGCMRH: updateLayers NEW LAYERS
//                // layer:" + layers[j].getName());
//            }
//        } else { // update istniejacych warstw
//            Debug.message("ms", "OGCserver: updateLayers: UPDATE LAYERS COMES.");
//            for (int i = 0; i < newLayers.length; i++) {
//                for (int j = 0; j < layers.length; j++) {
//                    // Debug.message("imageserver", "OGCMRH: updateLayers layer:
//                    // sprawdzam parê warstw :>" + newLayers[i].getName()+"< i
//                    // >"+layers[j].getName()+"<");
//                    if (layers[j].getName().trim().equalsIgnoreCase(newLayers[i].getName().trim())) {
//                        // if
//                        // (newLayers[i].getName().trim().equalsIgnoreCase("Ports")
//                        // ||
//                        // newLayers[i].getName().trim().equalsIgnoreCase("PortsGd"))
//                        // {
//                        if (staticLayersNames.contains(newLayers[i].getName().trim())) {
//                            // Debug.message("imageserver", "OGCMRH:
//                            // updateLayers layer:" + newLayers[i].getName()+"
//                            // nie jest zmieniana ");
//                        } else { // update istniejacej warstwy
//                            layers[j] = newLayers[i];
//                            layers[j].setPropertyPrefix(layers[j].getName());
//                            // Debug.message("imageserver", "OGCMRH:
//                            // updateLayers layer:" + layers[j].getName());
//                        }
//                    }
//                }
//            }
//        }
//        // aktualizacja mapowania nazw
//        createWmsLayers();
//    }
    
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
        if (layers.length < newLayers.length) { // tu wejdzie tylko raz , przy
                                                // pierwszym dodaniu warstw z
                                                // celami Gdynia, Hel,...
            // wektor u¿ywany tylko gdy pojawi¹ siê nowe warstwy (layers jest
            // zwyk³a tablica i nie mo¿na jej powiêkszyæ, wiêc wykorzystuje
            // wektor który poŸniej zmienie na tablicê)
            Debug.message("ms", "OGCserver: updateLayers: NEW LAYERS COMES.");

            List tmpLayers = new ArrayList();
            for (int j = 0; j < layers.length; j++) { // dodaj stare warstwy
                                                        // (statyczne, podk³ad
                                                        // mapy)
                tmpLayers.add(layers[j]);
            }

            for (int i = 0; i < newLayers.length; i++) { // dodaj nowe
                                                            // warstwy
                                                            // (dynamiczne ,
                                                            // podk³ad mapy) ale
                                                            // bez ju¿
                                                            // statycznych które
                                                            // s¹ ju¿ dodane
                boolean layerAlreadyAdded = false;
                for (int j = 0; j < layers.length; j++) { // sprawdz czy wsród
                                                            // nowych warstw nie
                                                            // ma przypadkiem
                                                            // warstwy
                                                            // statycznej (czyli
                                                            // starej warstwy)
                    String layerName = layers[j].getName();
                    String newLayerName = newLayers[i].getName();
                    if (layerName.trim().equalsIgnoreCase(newLayerName.trim())) {
                        layerAlreadyAdded = true;
                    }
                }
                if (!layerAlreadyAdded) { // gdy sprawdzana warstwa nie jest
                                            // statyczna (tzn mapa portu) to j¹
                                            // dodaj do wektora
                    tmpLayers.add(newLayers[i]);
                }
            }
            Layer[] newArrLayers = (Layer[]) tmpLayers.toArray(new Layer[0]);
            layers = newArrLayers;
            for (int j = 0; j < layers.length; j++) { // dodaj stare warstwy
                                                        // (statyczne, podk³ad
                                                        // mapy)
                layers[j].setPropertyPrefix(layers[j].getName());
                // Debug.message("imageserver", "OGCMRH: updateLayers NEW LAYERS
                // layer:" + layers[j].getName());
            }
        } else { // update istniejacych warstw
            Debug.message("ms", "OGCserver: updateLayers: UPDATE LAYERS COMES.");
            for (int i = 0; i < newLayers.length; i++) {
                for (int j = 0; j < layers.length; j++) {
                    // Debug.message("imageserver", "OGCMRH: updateLayers layer:
                    // sprawdzam parê warstw :>" + newLayers[i].getName()+"< i
                    // >"+layers[j].getName()+"<");
                    if (layers[j].getName().trim().equalsIgnoreCase(newLayers[i].getName().trim())) {
                        // if
                        // (newLayers[i].getName().trim().equalsIgnoreCase("Ports")
                        // ||
                        // newLayers[i].getName().trim().equalsIgnoreCase("PortsGd"))
                        // {
                     	String[] oldLayers=staticLayersNames.split(" ");
                    	boolean found=false;
                    	for (int k=0;k<oldLayers.length;k++) {
                    		if (oldLayers[k].equals(newLayers[i].getName().trim())) {
                    			found=true;
                    			break;
                    		}
                    	}
//                         if (staticLayersNames.contains(newLayers[i].getName().trim())) {
//                            // Debug.message("imageserver", "OGCMRH:
//                            // updateLayers layer:" + newLayers[i].getName()+"
//                            // nie jest zmieniana ");
//                        } else { // update istniejacej warstwy
                    	if (found==false) {
                            layers[j] = newLayers[i];
                            layers[j].setPropertyPrefix(layers[j].getName());
                            // Debug.message("imageserver", "OGCMRH:
                            // updateLayers layer:" + layers[j].getName());
                        }
                    }
                }
            }
        }

        // aktualizacja mapowania nazw
        createWmsLayers();
    }

    /**
     * return - zwraca layer hadlera
     * 
     * @return
     */
    public LayerHandler getLayerHandler() {
        return this.mapLayerHandler;
    }

}
