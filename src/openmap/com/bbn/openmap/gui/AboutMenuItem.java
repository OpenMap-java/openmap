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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/AboutMenuItem.java,v $
// $RCSfile: AboutMenuItem.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import com.bbn.openmap.*;

/**
 * This object brings up OpenMap about information.
 */
public class AboutMenuItem extends JMenuItem
    implements ActionListener {

    protected JDialog aboutBox = null;

    public AboutMenuItem() {
	super("About");
	setMnemonic('t');
	addActionListener(this);
    }
  
    public void actionPerformed(ActionEvent ae) {
	if (aboutBox == null) {
	    aboutBox = createAboutBox();
	    aboutBox.getContentPane().setLayout(new BorderLayout());
	    aboutBox.getContentPane().add(createCopyrightViewer(),
					  BorderLayout.CENTER);
	    aboutBox.getContentPane().add(
		createAboutControls(aboutBox),
		BorderLayout.SOUTH
		);
	    aboutBox.pack();
	}
    
	aboutBox.setVisible(true);
    }

    protected JComponent createCopyrightViewer() {
	JTextArea viewer = new JTextArea(
	    MapBean.getCopyrightMessage()+
	    Environment.get("line.separator")+
	    Environment.get("line.separator")+
	    "Version " + Environment.get(Environment.Version)+
	    Environment.get("line.separator")+
	    "Build " + Environment.get(Environment.BuildDate, "<no build tag>")
	    );
	viewer.setEditable(false);
	JScrollPane scroller = new JScrollPane(viewer);
	return scroller;
    }

    protected Component createAboutControls(final JDialog window) {
	JButton button = new JButton("OK");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    window.setVisible(false);
		}
	    });
	Box box = Box.createHorizontalBox();
	box.add(button);
	return box;
    }

    protected JDialog createAboutBox() {
	java.awt.Container topContainer = getTopLevelAncestor();
	String title = "About " + Environment.get(Environment.Title);
	if (topContainer instanceof Frame) {
	    return new JDialog(
		(Frame)topContainer, title, true);
	} else {
	    JDialog d = new JDialog();
	    d.setTitle(title);
	    return d;
	}
    }
}
