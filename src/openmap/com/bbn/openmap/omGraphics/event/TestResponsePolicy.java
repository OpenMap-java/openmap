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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/TestResponsePolicy.java,v $
// $RCSfile: TestResponsePolicy.java,v $
// $Revision: 1.2 $
// $Date: 2003/09/22 23:24:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

public class TestResponsePolicy implements GestureResponsePolicy {

    public TestResponsePolicy() {}

    ////// Queries

    public boolean isHighlightable(OMGraphic omg) {
	Debug.output("isHighlightable(" + omg.getClass().getName() + ")");
	return true;
    }

    public boolean isSelectable(OMGraphic omg) {
	Debug.output("isSelectable(" + omg.getClass().getName() + ")");
	return true;
    }

    ////// Reactions
 
    /** Fleeting change of appearance. */
    public void highlight(OMGraphic omg) {
	Debug.output("highlight(" + omg.getClass().getName() + ")");
    }

    public void unhighlight(OMGraphic omg) {
	Debug.output("unhighlight(" + omg.getClass().getName() + ")");
    }

   public void select(OMGraphicList omgl) {
	Debug.output("select(" + omgl.getDescription() + ")");
    }

    public void deselect(OMGraphicList omgl) {
	Debug.output("deselect(" + omgl.getDescription() + ")");
    }

    public OMGraphicList cut(OMGraphicList omgl) {
	Debug.output("cut(" + omgl.getDescription() + ")");
	return omgl;
    }

    public OMGraphicList copy(OMGraphicList omgl) {
	Debug.output("copy(" + omgl.getDescription() + ")");
	return omgl;
    }

    public void paste(OMGraphicList omgl) {
	Debug.output("paste(" + omgl.getDescription() + ")");
    }

    public String getInfoText(OMGraphic omg) {
	Debug.output("getInfoTextFor(" + omg.getClass().getName() + ")");
	return omg.getClass().getName();
    }

    public String getToolTipTextFor(OMGraphic omg) {
	Debug.output("getToolTipTextFor(" + omg.getClass().getName() + ")");
	return "TextResponsePolicy ToolTipText";
    }

    public JPopupMenu modifyPopupMenuForMap(JPopupMenu jpm) {
	Debug.output("modifyPopupMenuForMap(MAP)");
	return jpm;
    }

    public JPopupMenu modifyPopupMenuFor(OMGraphic omg, JPopupMenu jpm) {
	Debug.output("modifyPopupMenuFor(" + omg.getClass().getName() + ")");
	return jpm;
    }

}
