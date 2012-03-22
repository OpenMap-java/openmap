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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/ControlledManager.java,v $
// $RCSfile: ControlledManager.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/11 21:34:55 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import java.awt.BorderLayout;

import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;

import com.bbn.openmap.MapHandler;
import com.bbn.openmap.proj.Projection;

/**
 * A 3D manager where the eye position is controlled by an outside
 * object. The keys should still work to control the view orientation,
 * just not position. (I don't think they do, yet).
 * 
 * @author dietrick
 */
public class ControlledManager extends MapContentManager {

    /**
     * The object controlling the viewer's position.
     */
    protected NavBehaviorProvider controller;

    public ControlledManager(MapHandler mapHandler, NavBehaviorProvider cont,
            int contentMask) {
        this(mapHandler, cont, new Background(0f, 0f, 0f), contentMask);
    }

    public ControlledManager(MapHandler mapHandler, NavBehaviorProvider cont,
            Background background, int contentMask) {
        super();

        //      background.setCapability(Background.ALLOW_APPLICATION_BOUNDS_WRITE);
        //      background.setCapability(Background.ALLOW_APPLICATION_BOUNDS_READ);
        //      background.setCapability(Background.ALLOW_COLOR_READ);
        //      background.setCapability(Background.ALLOW_COLOR_WRITE);

        setController(cont);
        this.setSceneBackground(background);
        addMapContent(mapHandler, objRootBG, contentMask);

        // Important!! Compiles the universe
        ((UniverseManager) universe).makeLive();
    }

    protected void setController(NavBehaviorProvider cont) {
        controller = cont;
    }

    public NavBehaviorProvider getController() {
        return controller;
    }

    public Behavior getMotionBehavior(TransformGroup cameraTransform,
                                      Projection projection) {

        Behavior behavior = null;
        if (controller != null) {
            behavior = controller.setViewingPlatformBehavior(cameraTransform,
                    projection,
                    scaleFactor);
        }
        return behavior;
    }

    public static JFrame getFrame(String title, int width, int height,
                                  MapHandler mapHandler,
                                  NavBehaviorProvider controller,
                                  Background background, int contentMask) {

        JFrame frame = new JFrame(title);
        frame.setSize(width, height);
        frame.getContentPane().setLayout(new BorderLayout());
        ControlledManager c3d = new ControlledManager(mapHandler, controller, background, contentMask);
        c3d.compileUniverse();
        frame.getContentPane().add("Center", c3d.getCanvas());
        return frame;
    }

}

