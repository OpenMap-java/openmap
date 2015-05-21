/*
 *  **********************************************************************
 *
 *  BBN Corporation
 *  10 Moulton St.
 *  Cambridge, MA 02138
 *  (617) 873-2000
 *
 *  Copyright (C) 2002
 *  This software is subject to copyright protection under the laws of
 *  the United States and other countries.
 *
 *  **********************************************************************
 *
 *  $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/LayerMapContent.java,v $
 *  $RCSfile: LayerMapContent.java,v $
 *  $Revision: 1.7 $
 *  $Date: 2005/08/11 21:34:55 $
 *  $Author: dietrick $
 *
 *  **********************************************************************
 */
package com.bbn.openmap.tools.j3d;

import java.awt.Color;
import java.util.Iterator;

import javax.media.j3d.Group;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.Shape3D;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.ScaleFilterLayer;
import com.bbn.openmap.omGraphics.OMGraphicAdapter;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * Simple 3D world creation from OpenMap Layers that are OMGraphicHandlerLayers.
 * The Layer objects are not given height. Although you can control that with
 * the LayerSeparation variable. This LayerMapContent will also create a simple
 * plane colored the background color of the map.
 * 
 * @author dietrick
 */
public class LayerMapContent extends OrderedGroup {

    /**
     * The default amount of separation to place between layer objects (.1),
     * just to keep them from merging together.
     */
    public final static double DEFAULT_LAYER_SEPARATION = .1;

    /**
     * The height spacer that is being placed between layers, in straight 3D
     * coordinate space.
     */
    protected double layerSeparation = DEFAULT_LAYER_SEPARATION;

    /**
     * Constructor that creates a Group from the OMGraphicHandler layers that
     * are part of the map.
     * 
     * @param mapHandler MapHandler to use to get OMGraphicHandlerLayers.
     */
    public LayerMapContent(MapHandler mapHandler) {
        super();
        createMap(mapHandler);
    }

    /**
     * Constructor that creates a Group from a single OMGraphicHandler layer.
     * 
     * @param layer Description of the Parameter
     */
    public LayerMapContent(OMGraphicHandlerLayer layer) {
        addContent(this, layer);
    }

    /**
     * Add content to this Group. The OMGraphicHandler layers that are part of
     * the map with have their graphics may be added with a small separation
     * between the layers, depending on the value of layerSeparation.
     * 
     * @param mapHandler Description of the Parameter
     */
    protected void createMap(MapHandler mapHandler) {

        double baselineHeight = layerSeparation;

        LayerHandler lh = (LayerHandler) mapHandler.get("com.bbn.openmap.LayerHandler");
        if (lh != null) {
            Debug.message("3d",
                    "LayerMapContent: putting layer graphics on the map.");
            Layer[] layers = lh.getLayers();

            // Back to front makes the sea in the back, and keeps
            // first layers on top, to go with the OpenMap paradigm.
            for (int i = layers.length - 1; i >= 0; i--) {
                Layer layer = layers[i];
                if (layer.isVisible()) {
                    if (layer instanceof ScaleFilterLayer) {
                        ScaleFilterLayer sfl = (ScaleFilterLayer) layer;
                        layer = sfl.getAppropriateLayer();
                    }
                    if (layer instanceof OMGraphicHandlerLayer) {
                        addContent(this,
                                (OMGraphicHandlerLayer) layer,
                                baselineHeight += layerSeparation);
                    } else {
                        Debug.message("3d", "LayerMapContent: skipping layer "
                                + layer.getName());
                    }
                }
            }
        }

        addSea(this, mapHandler);
    }

    /**
     * Add the projection background color to the base level of the Java 3D map.
     * The MapHandler provides the MapBean and therefore the projection.
     * 
     * @param bg The feature to be added to the Sea attribute
     * @param mh The feature to be added to the Sea attribute
     */
    protected void addSea(Group bg, MapHandler mh) {
        MapBean map = (MapBean) mh.get("com.bbn.openmap.MapBean");
        if (map != null) {
            Debug.message("3d", "LayerMapContent: putting down sea.");
            Color seaColor = map.getBackground();

            Projection proj = map.getProjection();

            // Make the background strech a screen around the current
            // map, all directions.
            int width = proj.getWidth();
            int height = proj.getHeight();

            java.awt.geom.GeneralPath background =
            // OMGraphic.createBoxShape(0, 0, width, height);
            OMGraphicAdapter.createBoxShape(-width,
                    -height,
                    width * 3,
                    height * 3);

            addTo(bg,
                    OMGraphicUtil.createShape3D(background, 0, seaColor, true));
        }
    }

    /**
     * Add a layer to the Group.
     * 
     * @param bg The feature to be added to the Content attribute
     * @param layer The feature to be added to the Content attribute
     */
    protected void addContent(Group bg, OMGraphicHandlerLayer layer) {
        addContent(bg, layer, 0);
    }

    /**
     * Add a layer to the Group, at a specific height.
     * 
     * @param bg The feature to be added to the Content attribute
     * @param layer The feature to be added to the Content attribute
     * @param baselineHeight The feature to be added to the Content attribute
     */
    protected void addContent(Group bg, OMGraphicHandlerLayer layer,
                              double baselineHeight) {
        Debug.message("3d", "LayerMapContent: putting layer " + layer.getName()
                + " graphics on the map.");
        addTo(bg, OMGraphicUtil.createShape3D(layer.getList(), baselineHeight));
    }

    /**
     * Set the height spacer that is bing placed between layers.
     * 
     * @param separation The new layerSeparation value
     */
    public void setLayerSeparation(double separation) {
        layerSeparation = separation;
    }

    /**
     * Return the height spacer that is being placed between layers.
     * 
     * @return The layerSeparation value
     */
    public double getLayerSeparation() {
        return layerSeparation;
    }

    /**
     * Add the Shape3D objects from an iterator to a Group object.
     * 
     * @param bg The feature to be added to the To attribute
     * @param shapeIterator The feature to be added to the To attribute
     */
    protected void addTo(Group bg, Iterator shapeIterator) {

        while (shapeIterator.hasNext()) {
            try {
                Shape3D shape = (Shape3D) shapeIterator.next();
                if (shape != null) {
                    bg.addChild(shape);
                }
            } catch (ClassCastException cce) {
                continue;
            }
        }
    }
}