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
// $Revision: 1.1 $
// $Date: 2003/09/08 22:32:19 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

import com.bbn.openmap.Layer;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.util.Debug;

public class TestResponsePolicy implements GestureResponsePolicy {

    protected Layer layer;

    public TestResponsePolicy() {
    }

    public TestResponsePolicy(Layer layer) {
	this.layer = layer;
    }

    ////// Actions

    public void leftClick(MouseEvent me) {
	Debug.output("leftClick on MAP at " + me.getX() + ", " + me.getY());
    }

    public void leftClick(OMGeometry omg, MouseEvent me) {
	Debug.output("leftClick at " + me.getX() + ", " + me.getY());
    }

    public void leftClickOff(OMGeometry omg, MouseEvent me) {
	Debug.output("leftClickOff at " + me.getX() + ", " + me.getY());
    }

    public void rightClick(MouseEvent me) {
	Debug.output("rightClick on MAP at " + me.getX() + ", " + me.getY());
    }

    public void rightClick(OMGeometry omg, MouseEvent me) {
	Debug.output("rightClick at " + me.getX() + ", " + me.getY());
    }

    public void rightClickOff(OMGeometry omg, MouseEvent me) {
	Debug.output("rightClickOff at " + me.getX() + ", " + me.getY());
    }

    public void mouseOver(MouseEvent me) {
	Debug.output("mouseOver MAP at " + me.getX() + ", " + me.getY());
    }

    public void mouseOver(OMGeometry omg, MouseEvent me) {
	Debug.output("mouseOver at " + me.getX() + ", " + me.getY());
	if (layer != null) {
	    layer.fireRequestInfoLine(omg.getClass().getName());
	}
    }

    public void mouseNotOver(OMGeometry omg) {
	Debug.output("mouseNotOver");
	if (layer != null) {
	    layer.fireRequestInfoLine("");
	}
    }

    public void keyPressed(OMGeometry omg, int virtualKey) {
	Debug.output("keyPressed(" + virtualKey + ")");
    }

    ////// Queries

    public boolean isEditable(OMGeometry omgr) {
	Debug.output("isEditable()");
	return true;
    }

    public boolean isSelectable(OMGeometry omgr) {
	Debug.output("isSelectable()");
	return true;
    }

    ////// Reactions

    public void select(OMGeometry omg) {
	Debug.output("select()");
    }

    public void deselect(OMGeometry omg) {
	Debug.output("deselect()");
    }

    public OMGeometry cut(OMGeometry omg) {
	Debug.output("cut()");
	return omg;
    }

    public OMGeometry copy(OMGeometry omg) {
	Debug.output("copy()");
	return omg;
    }

    public void paste(OMGeometry omg) {
	Debug.output("paste()");
    }

    public String getToolTipTextFor(OMGeometry omg) {
	Debug.output("getToolTipTextFor()");
	return "TextResponsePolicy ToolTipText";
    }

    public JPopupMenu modifyPopupMenuForMap(JPopupMenu jpm) {
	Debug.output("modifyPopupMenuForMap()");
	return jpm;
    }

    public JPopupMenu modifyPopupMenuFor(OMGeometry omg, JPopupMenu jpm) {
	Debug.output("modifyPopupMenuFor()");
	return jpm;
    }

}
