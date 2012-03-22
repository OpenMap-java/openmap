/*
 * **********************************************************************
 * 
 * BBN Corporation 10 Moulton St. Cambridge, MA 02138 (617) 873-2000
 * 
 * Copyright (C) 2002 This software is subject to copyright protection
 * under the laws of the United States and other countries.
 * 
 * **********************************************************************
 * 
 * $Source:
 * /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/NavBehaviorProvider.java,v $
 * $RCSfile: NavBehaviorProvider.java,v $ $Revision: 1.3 $ $Date:
 * 2004/02/09 13:33:36 $ $Author: dietrick $
 * 
 * **********************************************************************
 */
package com.bbn.openmap.tools.j3d;

import javax.media.j3d.Behavior;
import javax.media.j3d.TransformGroup;

import com.bbn.openmap.proj.Projection;

public interface NavBehaviorProvider {

    /**
     * Set the behavior for the viewing platform.
     * 
     * @param cameraTransformGroup The new viewingPlatformBehavior
     *        value
     * @return Description of the Return Value
     */
    public Behavior setViewingPlatformBehavior(
                                               TransformGroup cameraTransformGroup,
                                               Projection projection,
                                               float scaleFactor);

}