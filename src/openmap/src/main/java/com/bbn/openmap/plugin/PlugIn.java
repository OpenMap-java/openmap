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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/PlugIn.java,v $
// $RCSfile: PlugIn.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:20 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin;

import java.awt.Component;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * This interface describes a component that can supply OMGraphics based on a
 * Projection. PlugIns can be added to the map via a PlugInLayer.
 * 
 * @see com.bbn.openmap.plugin.PlugInLayer
 */
public interface PlugIn extends PropertyConsumer {

    /**
     * The setComponent command is provided so that the plugin can be told its
     * parent. This is to allow a plugin access to a repaint call if something
     * changes with the graphics it is providing.
     */
    public void setComponent(Component parent);

    /**
     * Get the component the plugin is using (most likely, its parent).
     */
    public Component getComponent();

    /**
     * Set the MapMouseListener for this PlugIn. The MapMouseListener is
     * responsible for handling the MouseEvents that are occurring over the map.
     * 
     * @param mml MapMouseListener.
     * @see com.bbn.openmap.event.MapMouseListener
     */
    public void setMapMouseListener(MapMouseListener mml);

    /**
     * Returns the MapMouseListener that the plugin thinks should be used to
     * handle its events, if asked.
     * 
     * @see com.bbn.openmap.event.MapMouseListener
     */
    public MapMouseListener getMapMouseListener();

    /**
     * The getRectangle call is the main call into the PlugIn module. The module
     * is expected to return an OMGraphicsList filled with OMGraphic objects
     * that are within the screen parameters passed. It's assumed that the
     * PlugIn will call generate(projection) on the OMGraphics returned! If you
     * don't call generate on the OMGraphics, they will not be displayed on the
     * map.
     * 
     * @param p projection of the screen, holding scale, center coords, height,
     *        width. May be null if the parent component hasn't had the
     *        projection set.
     * @return OMGraphicList.
     * @see com.bbn.openmap.proj.Projection
     * @see com.bbn.openmap.omGraphics.OMGraphicList
     */
    public OMGraphicList getRectangle(Projection p);

    /**
     * Gives the PlugIn a chance to present components that control its
     * attributes.
     */
    public java.awt.Component getGUI();

    /**
     * Notification to the PlugIn that it has been removed from the map, so it
     * can free resources.
     */
    public void removed();

    /**
     * Notification to the PlugIn that it has been removed from the application,
     * so it can disconnect from all other objects that may be holding a
     * reference to it.
     */
    public void dispose();

}