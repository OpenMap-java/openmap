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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ControlMenu.java,v $
// $RCSfile: ControlMenu.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import javax.swing.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.awt.*;
import java.util.Iterator;
import com.bbn.openmap.*;

/**
 * This object is a container for MouseModeMenuItem which provides
 * interaction with current MouseModes in MouseDelegator object.
 */
public class ControlMenu extends AbstractOpenMapMenu 
    implements MenuBarMenu {

    private String defaultText = "Control";
    private int defaultMnemonic = 'C';

    protected BackgroundColorButton bcb;
    protected ToolPanelButton toolPanelButton;

    public ControlMenu() {
	super();
	setText(I18N.get("menu.control", defaultText));
	setMnemonic(defaultMnemonic);
	add(new MouseModeMenu());

	bcb = new BackgroundColorButton("Set Background Color");
	add(bcb);	
    }
  
    public void findAndUnInit(Iterator it) {
	Object someObj;
	while (it.hasNext()) {
	    someObj = it.next();
	    if (someObj instanceof MapBean) {
		// do the initializing that need to be done here
		if (bcb.getMap() == (MapBean)someObj) {
		    bcb.setMap(null);
		}
		if (someObj instanceof ToolPanel) {
		    removeToolPanelToggle((ToolPanel)someObj);
		}
	    }	  
	}
    }

    public void findAndInit(Iterator it) {
	Object someObj;
	while (it.hasNext()) {
	    someObj = it.next();
	    if (someObj instanceof MapBean) {
		// do the initializing that need to be done here
		bcb.setMap((MapBean)someObj);
	    }
	    if (someObj instanceof ToolPanel) {
		addToolPanelToggle((ToolPanel)someObj);
	    }
	}
    }

    public void addToolPanelToggle(ToolPanel tp) {
	toolPanelButton = new ToolPanelButton(tp);
	add(toolPanelButton);
    }

    public void removeToolPanelToggle(ToolPanel tp) {
	if (toolPanelButton.forToolPanel(tp)) {
	    this.remove(toolPanelButton);
	}
	toolPanelButton = null;
    }
}
