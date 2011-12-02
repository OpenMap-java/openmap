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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/GraphicUpdate.java,v $
// $RCSfile: GraphicUpdate.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * A simple object used by the GestureLinkResponse to associate an
 * action with a particular graphic, either with the graphic's ID for
 * MODIFY-type actions, or with the graphic for UPDATE-type actions.
 */
public class GraphicUpdate implements LinkPropertiesConstants {
    /**
     * A masked integer describing the action for the graphic. See
     * GestureLinkResponse fo the masks.
     */
    public int action = 0;
    /** The graphic's ID. */
    public String id = null;
    /** The graphic, for updates. */
    public OMGraphic graphic = null;

    /** Constructor for modify-type actions. */
    public GraphicUpdate(int graphicAction, String gid) {
        action = graphicAction;
        id = gid;
    }

    /** Constructor for update-type actions. */
    public GraphicUpdate(int graphicAction, OMGraphic omg) {
        action = graphicAction;
        if (omg != null) {
            Object obj = omg.getAppObject();
            if (obj instanceof LinkProperties) {
                id = ((LinkProperties) obj).getProperty(LPC_GRAPHICID);
            }
        }
        graphic = omg;
    }
}

