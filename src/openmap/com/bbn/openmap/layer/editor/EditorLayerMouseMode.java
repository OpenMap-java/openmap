// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/EditorLayerMouseMode.java,v $
// $RCSfile: EditorLayerMouseMode.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.editor;

import com.bbn.openmap.event.SelectMouseMode;

public class EditorLayerMouseMode extends SelectMouseMode {

    /**
     * Mouse Mode identifier, which is "EditorLayer".  This is
     * returned on getID().  This mouse mode is invisible, so id
     * doesn't have to be pretty for the GUI.
     */
    public transient String modeID = null;

    /**
     * Construct a EditorLayerMouseMode.
     * Default constructor.  Sets the ID to the modeID, and the
     * consume mode to true. 
     * @param idToUse a uniqueID to use just for a particular layer.
     */
    public EditorLayerMouseMode(String idToUse) {
	this(idToUse, true);
    }

    /**
     * Construct a EditorLayerMouseMode.
     * The constructor that lets you set the consume mode. 
     * @param idToUse a uniqueID to use just for a particular layer.
     * @param consumeEvents the consume mode setting.
     */
    public EditorLayerMouseMode(String idToUse, boolean consumeEvents) {
	super(idToUse, consumeEvents);
	modeID = idToUse;
	setVisible(false);
   }
}
