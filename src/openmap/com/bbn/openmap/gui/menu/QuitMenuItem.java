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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/QuitMenuItem.java,v $
// $RCSfile: QuitMenuItem.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/06 02:31:29 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

public class QuitMenuItem extends JMenuItem {

    public QuitMenuItem() {
	super("Quit");
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // HACK - need to call shutdown() on mapbean
		    // actually we should broadcast a shutdown
		    // event so thato ther gui components can
		    // clean up, and maybe only one can call exit.
		    System.exit(0);
		}
	    });
    }
}
