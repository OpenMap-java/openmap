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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/ToolPanelToggleMenuItem.java,v $
// $RCSfile: ToolPanelToggleMenuItem.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/06 02:31:29 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.menu;

import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.gui.ToolPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

/**
 * Menu item that holds onto the tool panel, and hides/displays it
 * when selected.  
 */
public class ToolPanelToggleMenuItem extends JMenuItem 
    implements ActionListener, LightMapHandlerChild {

    protected ToolPanel toolPanel = null;
    protected final static String hideLabel = "Hide Tool Panel";
    protected final static String displayLabel = "Display Tool Panel";

    public ToolPanelToggleMenuItem() {
	// assume that the tool panel isn't there.  
	// Won't be visible if it isn't.
	super(hideLabel);
	init(null);
    }

    public ToolPanelToggleMenuItem(ToolPanel tp) {
	super(tp.isVisible()?hideLabel:displayLabel);
	init(tp);
    }

    public void init(ToolPanel tp) {
	setToolPanel(tp);
	addActionListener(this);
    }

    public void setToolPanel(ToolPanel tp) {
	toolPanel = tp;
	this.setVisible(toolPanel != null);
    }

    public ToolPanel getToolPanel() {
	return toolPanel;
    }

    public boolean forToolPanel(ToolPanel tp) {
	return (tp == toolPanel);
    }

    public void actionPerformed(ActionEvent ae) {
	if (toolPanel != null) {
	    boolean selected = toolPanel.isVisible();
	    toolPanel.setVisible(!selected);
	    setText(selected?displayLabel:hideLabel);
	}
    }

    public void findAndInit(Object someObj) {
	if (someObj instanceof ToolPanel) {
	    setToolPanel((ToolPanel)someObj);
	}
    }

    public void findAndUndo(Object someObj) {
	if (someObj instanceof ToolPanel &&
	    getToolPanel() == (ToolPanel)someObj) {
	    setToolPanel(null);
	}
    }
}
