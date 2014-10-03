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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkActionConstants.java,v $
// $RCSfile: LinkActionConstants.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

/**
 * The LinkActionConstants interface contains all the constants needed
 * for the LinkActionList and LinkActionRequest that are defined by
 * the Link Protocol.
 */
public interface LinkActionConstants {

    ///////// LinkActionList constants. ///////////////

    /** Action identifier - change/modify/add graphics. */
    public static final int ACTION_GRAPHICS = 0;
    /** Action identifier - change/modify/add GUI components. */
    public static final int ACTION_GUI = 1;
    /** Action identifier - center/zoom/change projection of map. */
    public static final int ACTION_MAP = 2;

    /**
     * Graphic action descriptor mask - raise the graphic on top of
     * others.
     */
    public static final int MODIFY_RAISE_GRAPHIC_MASK = 1 << 0;
    /** Graphic action descriptor mask - lower graphics below others. */
    public static final int MODIFY_LOWER_GRAPHIC_MASK = 1 << 1;
    /** Graphic action descriptor mask - delete the graphic. */
    public static final int MODIFY_DELETE_GRAPHIC_MASK = 1 << 2;
    /** Graphic action descriptor mask - select the graphic. */
    public static final int MODIFY_SELECT_GRAPHIC_MASK = 1 << 3;
    /** Graphic action descriptor mask - deselect the graphic. */
    public static final int MODIFY_DESELECT_GRAPHIC_MASK = 1 << 4;
    /** Graphic action descriptor mask - deselect all graphics. */
    public static final int MODIFY_DESELECTALL_GRAPHIC_MASK = 1 << 5;
    /** Graphic action descriptor mask - add a graphic. */
    public static final int UPDATE_ADD_GRAPHIC_MASK = 1 << 6;
    /** Graphic action descriptor mask - update the graphic. */
    public static final int UPDATE_GRAPHIC_MASK = 1 << 7;

    ///////// LinkActionResponse constants. ///////////////

    /**
     * Gesture descriptor mask - Set if the mouse buttons were pressed
     * and released.
     */
    public static final int MOUSE_CLICKED_MASK = 1 << 0;
    /** Gesture descriptor mask - Set if the mouse button was pressed. */
    public static final int MOUSE_PRESSED_MASK = 1 << 1;
    /** Gesture descriptor mask - Set if the mouse button was released. */
    public static final int MOUSE_RELEASED_MASK = 1 << 2;
    /**
     * Gesture descriptor mask - Set if the mouse moved with no
     * buttons pressed.
     */
    public static final int MOUSE_MOVED_MASK = 1 << 3;
    /**
     * Gesture descriptor mask - Set if the mouse cursor has entered
     * the map window.
     */
    public static final int MOUSE_ENTERED_MASK = 1 << 4;
    /**
     * Gesture descriptor mask - Set if the mouse cursor has left the
     * map window.
     */
    public static final int MOUSE_EXITED_MASK = 1 << 5;
    /**
     * Gesture descriptor mask - Set if the mouse moved with a button
     * down.
     */
    public static final int MOUSE_DRAGGED_MASK = 1 << 6;
    /** Gesture descriptor mask - Set if a keyboard button was pressed. */
    public static final int KEY_PRESSED_MASK = 1 << 7;
    /**
     * Gesture descriptor mask - Set if a keyboard button was
     * released.
     */
    public static final int KEY_RELEASED_MASK = 1 << 8;
    /**
     * Gesture descriptor mask - Set if a gesture is affiliated with a
     * particular graphic. The graphic ID will be available via the
     * getGraphicID() method.
     */
    public static final int GRAPHIC_ID_MASK = 1 << 9;
    /**
     * Gesture descriptor mask - Set if the integer is being sent to
     * the client to tell it what types of events the server is
     * interested in receiving.
     */
    public static final int CLIENT_NOTIFICATION_MASK = 1 << 10;
    /**
     * Gesture descriptor mask - Set if the server is interested in
     * receiving events even if a graphic gesture is handled locally.
     */
    public static final int SERVER_NOTIFICATION_MASK = 1 << 11;
}