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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/MapContentManager.java,v $
// $RCSfile: MapContentManager.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/11 21:34:55 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import java.awt.BorderLayout;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The 3D manager that takes an OpenMap MapHandler, and creates a 3D
 * world from the layers in it. The layer's OMGraphics are converted
 * to 3D objects.
 * 
 * @author dietrick
 */
public class MapContentManager extends OM3DManager {

    protected MapContentManager() {
        super.init();
    }

    /**
     * @param mapHandler the OpenMap MapHandler.
     */
    public MapContentManager(MapHandler mapHandler, int contentMask) {
        this(mapHandler, new Background(0f, 0f, 0f), contentMask);
    }

    /**
     * @param mapHandler the OpenMap MapHandler.
     * @param background the background object to use for the 3D
     *        scene. Use this to change the sky color, for instance.
     */
    public MapContentManager(MapHandler mapHandler, Background background,
            int contentMask) {
        super.init();
        createWorld(mapHandler, background, contentMask);
    }

    protected void createWorld(MapHandler mapHandler, Background background,
                               int contentMask) {
        setSceneBackground(background);
        // objRootBG gets created in super class
        addMapContent(mapHandler, objRootBG, contentMask);
    }

    /**
     * IMPORTANT! You need to call this to compile the universe after
     * everything is set.
     */
    public void compileUniverse() {
        // Important!! Compiles the universe
        ((UniverseManager) universe).makeLive();
    }

    public void addBehavior(Behavior behavior) {
        objRootBG.addChild(behavior);
    }

    /**
     * This is the main function that gets called when the
     * MapContentViewer is created, to create the world objects in the
     * universe. It builds the objects to put in the J3D world from
     * the objects contained within the MapHandler. This method calls
     * setCameraLocation(), so you can modify where the camera is
     * placed there, and then calls createMapContent() to have a
     * BranchGroup created with 3D objects. You can modify those
     * methods to adjust how things get created.
     * 
     * @param mapHandler The feature to be added to the MapContent
     *        attribute
     * @param worldGroup The feature to be added to the MapContent
     *        attribute
     */
    protected void addMapContent(MapHandler mapHandler, BranchGroup worldGroup,
                                 int contentMask) {

        Projection projection = null;

        if (mapHandler != null) {

            MapBean mapBean = (MapBean) mapHandler.get("com.bbn.openmap.MapBean");

            if (mapBean != null) {
                projection = mapBean.getProjection();
            }

            TransformGroup mapTransformGroup = new TransformGroup();
            mapTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            mapTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

            Debug.message("3d", "OM3DViewer: adding map content");

            BoundingSphere bs = new BoundingSphere(ORIGIN, boundsDimension);

            //BoundingLeaf boundingLeaf = new BoundingLeaf(bs);

            background.setApplicationBounds(bs);

            mapTransformGroup.addChild(background);

            createMapContent(mapTransformGroup, mapHandler, contentMask);

            // Lights up the whole world. If this isn't added, the
            // land is black
            AmbientLight ambientLight = new AmbientLight();
            ambientLight.setInfluencingBounds(bs);
            worldGroup.addChild(ambientLight);

            // ///////
            Behavior beh = getMotionBehavior((TransformGroup) getCamera().getNode(),
                    projection);
            beh.setSchedulingBounds(bs);

            worldGroup.addChild(beh);
            // ///////
            worldGroup.addChild(mapTransformGroup);
        }
    }

    protected void createMapContent(TransformGroup mapTransformGroup,
                                    MapHandler mapHandler, int contentMask) {
        if ((contentMask & CONTENT_MASK_OMGRAPHICHANDLERLAYERS) != 0) {
            mapTransformGroup.addChild(createLayerMapContent(mapHandler));
        }

        mapTransformGroup.addChild(createMapContent(mapHandler, contentMask));
    }

    /**
     * Called from addMapContent. Create a BranchGroup, and put your
     * 3D objects in it. The MapBean in the MapHandler has a
     * projection that can be used to lay things out, and the layers
     * have objects to render.
     * 
     * @param mapHandler Description of the Parameter
     * @return Description of the Return Value
     */
    protected Group createLayerMapContent(MapHandler mapHandler) {
        return new LayerMapContent(mapHandler);
    }

    /**
     * @param mapHandler Description of the Parameter
     * @return Description of the Return Value
     */
    protected Group createMapContent(MapHandler mapHandler, int contentMask) {
        return new MapContent(mapHandler, contentMask);
    }

    /**
     * Called from addMapContent(), to set up the Transforms for
     * motion. The projection is provided in case you want to locate
     * the camera or map in a certain geographical location.
     * 
     * @param cameraTransform Description of the Parameter
     * @param projection Description of the Parameter
     * @return The motionBehavior value
     */
    public Behavior getMotionBehavior(TransformGroup cameraTransform,
                                      Projection projection) {

        return new OMKeyBehavior(cameraTransform, projection);
    }

    public static JFrame getFrame(String title, int width, int height,
                                  MapHandler mapHandler, Background background,
                                  int contentMask) {

        JFrame frame = new JFrame(title);
        frame.setSize(width, height);
        frame.getContentPane().setLayout(new BorderLayout());
        MapContentManager mc3d = new MapContentManager(mapHandler, background, contentMask);
        mc3d.compileUniverse();
        frame.getContentPane().add("Center", mc3d.getCanvas());
        return frame;
    }
}
