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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/DrawingEditorTool.java,v $
// $RCSfile: DrawingEditorTool.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.gui.MouseModeButtonPanel;
import com.bbn.openmap.gui.OMGraphicDeleteTool;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.tools.drawing.*;
import com.bbn.openmap.util.Debug;

/**
 * The DrawingEditorTool is a EditorTool for the EditorLayer that has
 * a face much like a standard drawing layer.  It appears integrated
 * into the application, and receives all the new OMGraphics that are
 * created.
 */
public class DrawingEditorTool extends AbstractDrawingEditorTool {

    /**
     * A constructor that adds the default tool loaders automatically.
     */
    public DrawingEditorTool(EditorLayer layer) {
	super(layer);

	addEditToolLoader(new OMLineLoader());
	addEditToolLoader(new OMPolyLoader());
	addEditToolLoader(new OMRectLoader());
	addEditToolLoader(new OMCircleLoader());
	addEditToolLoader(new OMPointLoader());
    }
}
