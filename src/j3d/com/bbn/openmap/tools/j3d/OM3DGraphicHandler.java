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
 *  $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/OM3DGraphicHandler.java,v $
 *  $RCSfile: OM3DGraphicHandler.java,v $
 *  $Revision: 1.4 $
 *  $Date: 2005/08/11 19:27:04 $
 *  $Author: dietrick $
 *
 *  **********************************************************************
 */
package com.bbn.openmap.tools.j3d;

/**
 * This is an interface that denotes an object that will contribute
 * objects into a Java 3D scene graph. If you write an object that
 * implements this interface, it will be called with a MapContent
 * object that you can use to add different objects to the scene.
 * 
 * @author dietrick
 */
public interface OM3DGraphicHandler {

    /**
     * Provide a MapContent object for the OM3DGraphicHandler to add
     * objects to. The MapContent object as three add() methods, one
     * for OMGraphics, one for OMGrid specifically, and one for
     * Shape3D objects. The OM3DGraphicHandler should go through its
     * graphics and add them to this MapContext object. This should be
     * done *before* the OM3DGraphicHandler returns from this method!
     * 
     * @param mapContent The feature to be added to the
     *        GraphicsToScene attribute
     * @see MapContent
     */
    public void addGraphicsToScene(MapContent mapContent);

}