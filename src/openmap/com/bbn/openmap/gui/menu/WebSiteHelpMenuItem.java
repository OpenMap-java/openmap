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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/WebSiteHelpMenuItem.java,v $
// $RCSfile: WebSiteHelpMenuItem.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/06 02:31:29 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.menu;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import com.bbn.openmap.Environment;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.LightMapHandlerChild;

/**
 * This object tells a browser to bring up the OpenMap Website help.
 */
public class WebSiteHelpMenuItem extends JMenuItem
    implements ActionListener, LightMapHandlerChild {

    protected InformationDelegator informationDelegator = null;

    public WebSiteHelpMenuItem() {
	super("OpenMap WebSite Help");
	setMnemonic('h');
	addActionListener(this);
	setEnabled(false); // enabled when InformationDelegator found.
    }
  
    /**
     * @param in_informationDelegator
     */
    public void setInformationDelegator(InformationDelegator in_informationDelegator) {
	informationDelegator = in_informationDelegator;
	setEnabled(informationDelegator != null);
    }
  
    /**
     * Return current value of InformationDelegator.
     */
    protected InformationDelegator getInformationDelegator() {
	return informationDelegator;
    }
  
    public void actionPerformed(ActionEvent ae) {
	if (informationDelegator != null) {
	    informationDelegator.displayURL(Environment.get(Environment.HelpURL, "http://openmap.bbn.com/doc/user-guide.html"));
	}
    }

    public void findAndInit(Object someObj) {
	if (someObj instanceof InformationDelegator) {
	    setInformationDelegator((InformationDelegator)someObj);
	}
    }

    public void findAndUndo(Object someObj) {
	if (someObj instanceof InformationDelegator &&
	    getInformationDelegator() == (InformationDelegator)someObj) {
	    setInformationDelegator(null);
	}
    }
}
