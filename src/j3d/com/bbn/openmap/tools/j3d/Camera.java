/**
 * Copyright (c) 1999 Justin Couch Java Source Raw J3D
 * Tutorial Version History Date Version Programmer
 * ---------- ------- ------------------------------------------
 * 01/08/1998 1.0.0 Justin Couch
 */

package com.bbn.openmap.tools.j3d;

// Standard imports
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

/**
 * This Camera class was modified from Justin Couch's
 * http://www.j3d.org tutorial examples. Test class for showing the
 * use of a View and ViewPlatform
 * <P>
 * 
 * Basic view consists of the standard placement.
 * 
 * @author Justin Couch
 * @author dietrick
 */
public class Camera implements OM3DConstants {

    private Group hud_group;
    private TransformGroup root_tx_grp;
    private Transform3D location;
    private ViewPlatform platform;
    private View view;
    private DirectionalLight headlight;

    private PhysicalBody body;
    private PhysicalEnvironment env;

    public Camera() {
        hud_group = new Group();
        hud_group.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        platform = new ViewPlatform();
        location = new Transform3D();

        root_tx_grp = new TransformGroup();
        root_tx_grp.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        root_tx_grp.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        root_tx_grp.setTransform(location);
        root_tx_grp.addChild(platform);
        root_tx_grp.addChild(hud_group);

        // now create the headlight
        headlight = new DirectionalLight();
        headlight.setCapability(Light.ALLOW_STATE_WRITE);
        headlight.setColor(White);
        headlight.setInfluencingBounds(LIGHT_BOUNDS);
        root_tx_grp.addChild(headlight);

        body = new PhysicalBody();
        env = new PhysicalEnvironment();

        view = new View();
        view.setBackClipDistance(BACK_CLIP_DISTANCE);
        view.setPhysicalBody(body);
        view.setPhysicalEnvironment(env);
        view.attachViewPlatform(platform);
    }

    /**
     * Set the canvas that this camera is using
     * 
     * @param canvas The canvas that is to be used for this camera
     */
    public void setCanvas(Canvas3D canvas) {
        view.addCanvas3D(canvas);
    }

    /**
     * Set the location of the camera. This is the location of the
     * center of the camera relative to whatever is used as its root
     * group node.
     * 
     * @param loc The location of the camera
     */
    public void setLocation(Vector3f loc) {
        location.setTranslation(loc);
        root_tx_grp.setTransform(location);
    }

    /**
     * Set the orientation of the camera.
     * 
     * @param angle The orientation of the camera
     */
    public void setOrientation(AxisAngle4f angle) {
        location.setRotation(angle);
        root_tx_grp.setTransform(location);
    }

    /**
     * Add some goemetry to the HUD area. This geometry must come
     * complete with its own parent transform to offset the object by
     * the appropriate amount. The camera does not do any auto-offsets
     * of geometry.
     * 
     * @param geom The geometry to add
     */
    public void addHUDObject(Node geom) {
        hud_group.addChild(geom);
    }

    /**
     * Enable the headlight that is attached to the camera.
     * 
     * @param enable True if the light is to be turned on
     */
    public void setHeadLight(boolean enable) {
        headlight.setEnable(enable);
    }

    /**
     * Get the J3D node that is used to represent the camera
     * 
     * @return The root TransformGroup of the camera
     */
    public Node getNode() {
        return root_tx_grp;
    }
}