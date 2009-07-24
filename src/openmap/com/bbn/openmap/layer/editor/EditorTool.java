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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/EditorTool.java,v $
// $RCSfile: EditorTool.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.Container;

import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.MapMouseInterpreter;

/**
 * An EditorTool is a component that provides specific functionality to the
 * EditorLayer. The EditorTool is responsible for modifying or creating a set of
 * OMGraphics in a certain way, where the OMGraphics actually represent a
 * specific set of objects on the map, and the actions on the OMGraphics need to
 * be controlled.
 */
public interface EditorTool extends MapMouseInterpreter, LightMapHandlerChild {

    /**
     * Method where the EditorLayer lets the tool know that the editing function
     * has come full circle, so the user interface can be adjusted.
     */
    public void drawingComplete(OMGraphic omg, OMAction action);

    /**
     * A method that is checked by the EditorLayer to see if the EditorTool
     * wants to receive mouse events.
     */
    public boolean wantsEvents();

    /**
     * A method that lets the EditorTool know whether its interface should be
     * visible. Usually, called when the layer is turned on or off.
     */
    public void setVisible(boolean value);

    /**
     * A method that lets the EditorTool respond to queries wondering whether
     * its interface is visible.
     */
    public boolean isVisible();

    /**
     * The method that allows an EditorTool provide it's GUI.
     */
    public Container getFace();

    /**
     * Called when the EditorLayer is removed from application.
     */
    public void dispose();
}