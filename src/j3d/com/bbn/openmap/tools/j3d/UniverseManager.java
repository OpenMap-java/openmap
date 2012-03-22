/**
 * Copyright (c) 1999 Justin Couch Java Source Raw J3D
 * Tutorial Version History Date Version Programmer
 * ---------- ------- ------------------------------------------
 * 01/08/1998 1.0.0 Justin Couch
 */

package com.bbn.openmap.tools.j3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Locale;
import javax.media.j3d.Node;
import javax.media.j3d.VirtualUniverse;

/**
 * Test class for representing a universe
 * <P>
 * 
 * Basic universe consisting of a default Locale and three branch
 * graphs for objects that exist in the display and world spaces, as
 * well as a separate branch for cameras.
 * 
 * @author Justin Couch
 * @version Who Cares!
 */
public class UniverseManager extends VirtualUniverse {

    private Locale locale;
    private BranchGroup view_group;
    private BranchGroup world_object_group;

    /**
     * Create the basic universe and all of the supporting
     * infrastructure that is needed by a J3D application. The default
     * setup just uses a single local located at the origin.
     */
    public UniverseManager() {
        this(null);
    }

    /**
     * Create the basic universe and all of the supporting
     * infrastructure that is needed by a J3D application. The default
     * setup just uses a single local located at the origin.
     * 
     * @param worldGroup Description of the Parameter
     */
    public UniverseManager(BranchGroup worldGroup) {
        locale = new Locale(this);

        view_group = new BranchGroup();
        view_group.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        if (worldGroup == null) {
            world_object_group = new BranchGroup();
            world_object_group.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        } else {
            world_object_group = worldGroup;
        }
    }

    /**
     * Add a camera to the world.
     * 
     * @param cam The camera that may be added
     */
    public void addCamera(Camera cam) {
        view_group.addChild(cam.getNode());
    }

    /**
     * Add an object to the world object group.
     * 
     * @param node The node that may be added
     */
    public void addWorldObject(Node node) {
        world_object_group.addChild(node);
    }

    public BranchGroup getWorldBranchGroup() {
        return world_object_group;
    }

    /**
     * Make the universe live by adding the objects to the locale
     */
    public void makeLive() {
        view_group.compile();
        world_object_group.compile();

        locale.addBranchGraph(view_group);
        locale.addBranchGraph(world_object_group);
    }
}