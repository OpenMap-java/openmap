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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/OM3DManager.java,v $
// $RCSfile: OM3DManager.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/11 19:27:04 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.media.j3d.Background;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Vector3f;

/**
 * An abstract 3D manager object, containing content, canvas, camera
 * and the universe. The canvase is the thing you add to the GUI to
 * see the 3D world.
 * 
 * @author dietrick
 */
public abstract class OM3DManager implements OM3DConstants {

    protected float scaleFactor = 1f;

    /**
     * The Universe for the scene.
     */
    protected UniverseManager universe = null;

    /**
     * The root branch group of worldly objects.
     */
    protected BranchGroup objRootBG = null;

    /**
     * The camera, the viewer's eye.
     */
    protected Camera cam = null;

    /**
     * The bounding sphere radius for the root branch group.
     */
    protected double boundsDimension = DEFAULT_BOUNDS_DIMENSION;

    /**
     * The Background to the universe.
     */
    protected Background background = new Background();

    /**
     */
    protected Canvas3D canvas = null;

    protected OM3DManager() {
        init();
    }

    /**
     * Set up the canvas and the universe.
     */
    protected void init() {

        // Try to set a GraphicsConfiguration...
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getBestConfiguration(template);

        canvas = new Canvas3D(config);
        canvas.setSize(new Dimension(DEFAULT_DIMENSION, DEFAULT_DIMENSION));

        objRootBG = new BranchGroup();
        objRootBG.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        constructWorld(objRootBG);
    }

    /**
     * Construct everything that we want in the basic test world
     * 
     * @param worldBranchGroup Description of the Parameter
     */
    protected void constructWorld(BranchGroup worldBranchGroup) {
        // create the basic universe
        UniverseManager univ = new UniverseManager(worldBranchGroup);

        cam = new Camera();
        Vector3f loc = new Vector3f(0, 0, 0);
        cam.setLocation(loc);
        cam.setHeadLight(true);
        cam.setCanvas(canvas);
        univ.addCamera(cam);

        universe = univ;
    }

    /**
     * Get the Camera, which is controlling the viewer's perspective.
     * 
     * @return The camera value
     */
    public Camera getCamera() {
        return cam;
    }

    public Canvas3D getCanvas() {
        return canvas;
    }

    /**
     * Get the BranchGroup that represents the head of the world
     * object tree.
     * 
     * @return The world value
     */
    public BranchGroup getWorld() {
        return objRootBG;
    }

    public void setScaleFactor(float sf) {
        scaleFactor = sf;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setBoundsDimension(double bd) {
        boundsDimension = bd;
    }

    public double getBoundsDimension() {
        return boundsDimension;
    }

    public VirtualUniverse getUniverse() {
        return universe;
    }

    public void setSceneBackground(Background background) {
        this.background = background;
    }

    public Background getSceneBackground() {
        return background;
    }
}