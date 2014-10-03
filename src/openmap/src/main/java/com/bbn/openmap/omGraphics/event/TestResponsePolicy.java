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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/TestResponsePolicy.java,v
// $
// $RCSfile: TestResponsePolicy.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;

import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

public class TestResponsePolicy implements GestureResponsePolicy {

    protected OMGraphicList selected;

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

    public boolean receivesMapEvents() {
        Debug.output("receivesMapEvents");
        return true;
    }

    public OMGraphicList getSelected() {
        Debug.output("getSelected()");
        return selected;
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
        selected = omgl;
    }

    public void deselect(OMGraphicList omgl) {
        Debug.output("deselect(" + omgl.getDescription() + ")");
        selected = null;
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

    public List<Component> getItemsForMapMenu(MapMouseEvent me) {
        Debug.output("getMenuForMap(MAP)");
        return null;
    }

    public List<Component> getItemsForOMGraphicMenu(OMGraphic omg) {
        Debug.output("getMenuFor(" + omg.getClass().getName() + ")");
        List<Component> list = new LinkedList<Component>();
        list.add(new JMenuItem(omg.getClass().getName()));
        return list;
    }

    public boolean mouseOver(MapMouseEvent mme) {
        Debug.output("mouseOver(" + mme + ")");
        return true;
    }

    public boolean leftClick(MapMouseEvent mme) {
        Debug.output("leftClick(" + mme + ")");
        return true;
    }

}