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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/GestureResponsePolicy.java,v $
// $RCSfile: GestureResponsePolicy.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/08 22:32:19 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

import com.bbn.openmap.omGraphics.OMGeometry;

public interface GestureResponsePolicy {

    ////// Actions

    public void leftClick(MouseEvent me);

    public void leftClick(OMGeometry omg, MouseEvent me);

    public void leftClickOff(OMGeometry omg, MouseEvent me);

    public void rightClick(MouseEvent me);

    public void rightClick(OMGeometry omg, MouseEvent me);

    public void rightClickOff(OMGeometry omg, MouseEvent me);

    public void mouseOver(MouseEvent me);

    public void mouseOver(OMGeometry omg, MouseEvent me);

    public void mouseNotOver(OMGeometry omg);

    public void keyPressed(OMGeometry omg, int virtualKey);

    ////// Queries

    public boolean isEditable(OMGeometry omgr);

    public boolean isSelectable(OMGeometry omgr);

    ////// Reactions

    public void select(OMGeometry omg);

    public void deselect(OMGeometry omg);

    public OMGeometry cut(OMGeometry omg);

    public OMGeometry copy(OMGeometry omg);

    public void paste(OMGeometry omg);

    public String getToolTipTextFor(OMGeometry omg);

    public JPopupMenu modifyPopupMenuForMap(JPopupMenu jpm);

    public JPopupMenu modifyPopupMenuFor(OMGeometry omg, JPopupMenu jpm);

}
