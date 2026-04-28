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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/DrawingToolRequestor.java,v $
// $RCSfile: DrawingToolRequestor.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * A DrawingToolRequestor is an object that asks a DrawingTool to do
 * something for it. The drawingComplete method is called when the
 * DrawingTool is complete, letting the requestor know when it should
 * get the map repainted, or to send the EditableOMGraphic to another
 * object.
 */
public interface DrawingToolRequestor {
    /**
     * The method where a graphic, and an action to take on the
     * graphic, arrives.
     */
    public void drawingComplete(OMGraphic omg, OMAction action);

    /**
     * Needed to fill in a GUI with a receiver's name, to enable the
     * user to send a graphic to a specific object. Should be a pretty
     * name, suitable to let a user know what it is. It's important
     * that the requestor have a name, because that could be the key
     * that is used in some GUI components.
     */
    public String getName();
}