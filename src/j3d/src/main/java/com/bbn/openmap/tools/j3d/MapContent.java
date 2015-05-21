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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/MapContent.java,v $
// $RCSfile: MapContent.java,v $
// $Revision: 1.7 $
// $Date: 2005/08/11 19:27:04 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.image.AcmeGifFormatter;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.sun.j3d.utils.image.TextureLoader;

/**
 * A more complex MapContent. The regular layers get drawn into a base
 * image that is overlaid upon the earth geometry which can have
 * shape, if elevation data is available. The other layers, that
 * provide objects that should be rendered in 3D space, get entered
 * into the scene as specific objects.
 * 
 * @author dietrick
 */
public class MapContent extends BranchGroup implements OM3DConstants {

    protected MapBean map;
    protected Proj proj;
    protected LayerHandler layerHandler;
    protected HashSet graphicHandlers;
    protected MapHandler mapHandler;

    public MapContent(MapHandler mapHandler, int contentMask) {
        super();
        init(mapHandler);

        if ((contentMask & CONTENT_MASK_OM3DGRAPHICHANDLERS) != 0) {
            createMapObjects();
        }
        if ((contentMask & CONTENT_MASK_IMAGEMAP) != 0) {
            createMapImage();
        }
        if ((contentMask & CONTENT_MASK_ELEVATIONMAP) != 0) {
            createMapElevations();
        }
    }

    protected void init(MapHandler mapHandler) {

        if (mapHandler == null) {
            return;
        }

        map = (MapBean) mapHandler.get("com.bbn.openmap.MapBean");
        proj = null;

        if (map != null) {
            proj = (Proj) map.getProjection();
        }

        this.mapHandler = mapHandler;
    }

    protected HashSet getGraphicHandlers(MapHandler mapHandler) {

        Debug.message("3d", "LayerMapContent: looking for OM3DGraphicHandlers.");

        HashSet ret = new HashSet();
        Iterator iterator = mapHandler.iterator();

        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof OM3DGraphicHandler) {
                Debug.message("3d", "LayerMapContent: found one, adding " + obj);
                ret.add(obj);
            }
        }

        // Have to do layers separately because they may not be
        // exposed to the MapHandler.
        layerHandler = (LayerHandler) mapHandler.get("com.bbn.openmap.LayerHandler");

        if (layerHandler != null) {
            Layer[] layers = layerHandler.getLayers();
            int size = layers.length;

            for (int i = size - 1; i >= 0; i--) {
                Layer layer = layers[i];

                if (layer.isVisible() && (layer instanceof OM3DGraphicHandler)) {
                    Debug.message("3d",
                            "LayerMapContent: found layer version, adding "
                                    + layer.getName());
                    ret.add(layer);
                }
            }
        }
        return ret;
    }

    /**
     * May be used for OM3DGraphicHandlers to get an idea of what the
     * map looks like.
     * 
     * @return The projection value
     */
    public Projection getProjection() {
        return proj;
    }

    /**
     * Goes through the MapHandler, and sets this object in any
     * OM3DGraphicHandler, so they can call back to load in Shape3D or
     * OMGraphic objects.
     */
    protected void createMapObjects() {

        if (mapHandler == null) {
            Debug.error("MapContent: MapHandler not set!");
            return;
        }

        graphicHandlers = getGraphicHandlers(mapHandler);

        Iterator iterator = graphicHandlers.iterator();
        while (iterator.hasNext()) {
            ((OM3DGraphicHandler) iterator.next()).addGraphicsToScene(this);
        }
    }

    /**
     * Callback method for OM3DGraphicHandlers, to add an OMGrid
     * object to the scene. For now, the values of the OMGrid
     * correspond to the height of the values in the grid. The heights
     * should be relative to the 3D scene, in relation to the OpenMap
     * projection (screen X/Y coordinates of the OpenMap projection
     * have a direct relationship with the X/Z axis of the 3D scene).
     * 
     * @param grid Description of the Parameter
     */
    public void add(OMGrid grid) {
        add(OMGraphicUtil.createShape3D(grid, 0, proj));
    }

    /**
     * Callback method for OM3DGraphicHandlers, to add an OMGraphics
     * to the scene.
     * 
     * @param omgraphic an OMGraphic.
     * @param height the height of the object. All points of the
     *        object are at this height. The height should be relative
     *        to the 3D scene, in relation to the OpenMap projection
     *        (screen X/Y coordinates of the OpenMap projection have a
     *        direct relationship with the X/Z axis of the 3D scene).
     *        If you have an object with varying height, create an
     *        Shape3D object instead and use the other add() method.
     */
    public void add(OMGraphic omgraphic, double height) {
        add(OMGraphicUtil.createShape3D(omgraphic, height));
    }

    /**
     * Callback method for OM3DGraphicHandlers, to add a Shape3D
     * object to the scene.
     * 
     * @param shape Description of the Parameter
     */
    public void add(Shape3D shape) {
        addChild(shape);
    }

    /**
     * Add the Shape3D objects from an iterator.
     * 
     * @param shapeIterator Iterator containing Shape3D objects.
     */
    protected void add(Iterator shapeIterator) {

        while (shapeIterator.hasNext()) {
            try {
                Shape3D shape = (Shape3D) shapeIterator.next();
                if (shape != null) {
                    add(shape);
                }
            } catch (ClassCastException cce) {
                continue;
            }
        }
    }

    /**
     * Add a TransformGroup from a OM3DGraphicHandler. This will
     * provide an object to the scene that can be controlled by an
     * outside source.
     * 
     * @param transformGroup any transform group containing Shape3D
     *        objects.
     */
    protected void add(TransformGroup transformGroup) {
        addChild(transformGroup);
    }

    protected void createMapElevations() {
        Debug.error("MapContent.createMapElevations not implemented.");
    }

    protected void createMapImage() {

        if (proj == null || mapHandler == null) {
            Debug.error("MapContent: MapHandler not set!");
            return;
        }

        int pwidth = 512;
        int pheight = 512;

        AcmeGifFormatter formatter = new AcmeGifFormatter();
        Graphics graphics = formatter.getGraphics(pwidth, pheight);

        Paint background;
        if (map == null) {
            background = MapBean.DEFAULT_BACKGROUND_COLOR;
        } else {
            background = map.getBckgrnd();
        }
        proj.drawBackground((Graphics2D) graphics, background);

        layerHandler = (LayerHandler) mapHandler.get("com.bbn.openmap.LayerHandler");
        if (layerHandler != null) {
            Debug.message("3d",
                    "LayerMapContent: putting layer graphics on the map.");
            Layer[] layers = layerHandler.getLayers();
            int size = layers.length;

            for (int i = size - 1; i >= 0; i--) {
                Layer layer = layers[i];

                if (layer.isVisible() && !(layer instanceof OM3DGraphicHandler)) {

                    layer.renderDataForProjection(proj, graphics);
                }
            }
        }

        BufferedImage bimage = formatter.getBufferedImage();

        // Now we have our textured image.

        QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES
                | GeometryArray.TEXTURE_COORDINATE_2);
        float height = (float) pheight;
        float width = (float) pwidth;

        Point3f p = new Point3f(0f, 0f, 0f);//-1.0f, 1.0f, 0.0f);
        plane.setCoordinate(0, p);
        p.set(0f, 0f, height);//-1.0f, -1.0f, 0.0f);
        plane.setCoordinate(1, p);
        p.set(width, 0f, height);//1.0f, -1.0f, 0.0f);
        plane.setCoordinate(2, p);
        p.set(0f, 0f, height);//1.0f, 1.0f, 0.0f);
        plane.setCoordinate(3, p);

        Point2f q = new Point2f(0.0f, 1.0f);
        plane.setTextureCoordinate(0, q);
        q.set(0.0f, 0.0f);
        plane.setTextureCoordinate(1, q);
        q.set(1.0f, 0.0f);
        plane.setTextureCoordinate(2, q);
        q.set(1.0f, 1.0f);
        plane.setTextureCoordinate(3, q);

        Appearance appear = new Appearance();

        TextureLoader loader = new TextureLoader(bimage);
        ImageComponent2D image = loader.getImage();

        if (Debug.debugging("3d")) {
            Debug.output("MapContent: image height: " + image.getHeight()
                    + ", width: " + image.getWidth());
        }

        // can't use parameterless constuctor
        Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
        texture.setImage(0, image);
        //texture.setEnable(false);

        appear.setTexture(texture);

        appear.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 0.1f));

        Shape3D planeObj = new Shape3D(plane, appear);
        addChild(planeObj);

    }

}