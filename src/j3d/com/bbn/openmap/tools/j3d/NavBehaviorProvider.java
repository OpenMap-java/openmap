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
 *  $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/NavBehaviorProvider.java,v $
 *  $RCSfile: NavBehaviorProvider.java,v $
 *  $Revision: 1.1.1.1 $
 *  $Date: 2003/02/14 21:35:48 $
 *  $Author: dietrick $
 *
 *  **********************************************************************
 */
package com.bbn.openmap.tools.j3d;

import javax.media.j3d.*;
import com.bbn.openmap.proj.Projection;
import com.sun.j3d.utils.universe.*;

public interface NavBehaviorProvider {

    /**
     * Set the behavior for the viewing platform.
     *
     * @param cameraTransformGroup  The new viewingPlatformBehavior value
     * @return                      Description of the Return Value
     */
    public Behavior setViewingPlatformBehavior(TransformGroup cameraTransformGroup, Projection projection, float scaleFactor);

}
