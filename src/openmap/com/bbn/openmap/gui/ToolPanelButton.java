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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/ToolPanelButton.java,v $
// $RCSfile: ToolPanelButton.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import com.bbn.openmap.*;
import com.bbn.openmap.omGraphics.OMColorChooser;

import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

/**
 * Menu item that holds onto the tool panel, and hides/displays it
 * when selected.  
 */
public class ToolPanelButton extends JMenuItem implements ActionListener {

    protected ToolPanel toolPanel = null;
    protected final static String hideLabel = "Hide Tool Panel";
    protected final static String displayLabel = "Display Tool Panel";

    public ToolPanelButton(ToolPanel tp) {
	super(tp.isVisible()?hideLabel:displayLabel);
	setToolPanel(tp);
	addActionListener(this);
    }

    public void setToolPanel(ToolPanel tp) {
	toolPanel = tp;
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
}
