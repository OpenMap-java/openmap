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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MiniBrowser.java,v $
// $RCSfile: MiniBrowser.java,v $
// $Revision: 1.1 $
// $Date: 2003/04/08 18:41:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import com.bbn.openmap.util.Debug;

public class MiniBrowser extends OMComponentPanel {

    JEditorPane jep;

    public MiniBrowser(String content) {
	this("text/html", content);
    }

    public MiniBrowser(String mimeType, String content) {

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setLayout(gridbag);

	jep = new JEditorPane(mimeType, content);
	jep.setEditable(false);
	jep.addHyperlinkListener(new HyperlinkListener() {
		public void hyperlinkUpdate(HyperlinkEvent e) {
		    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) e.getSource();

			if (e instanceof HTMLFrameHyperlinkEvent) {
			    HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
			    HTMLDocument doc = (HTMLDocument)pane.getDocument();
			    doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
			    try {
				pane.setPage(e.getURL());
			    } catch (Throwable t) {
				t.printStackTrace();
			    }
			}
		    }
		}
	    });

	JScrollPane jsp = new JScrollPane(jep,
					  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	c.fill = GridBagConstraints.BOTH;
	c.anchor = GridBagConstraints.NORTHWEST;
	c.insets = new Insets(5, 5, 5, 5);
	c.weightx = 1;
	c.weighty = 1;
	gridbag.setConstraints(jsp, c);
	add(jsp);

	c.gridy = 1;
	c.anchor = GridBagConstraints.EAST;
	c.weightx = 0;
	c.weighty = 0;
	c.fill= GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.SOUTH;
	
	WindowSupport ws = new WindowSupport(this, "");

	JButton dismissButton = new JButton("Close");
	dismissButton.setActionCommand(WindowSupport.KillWindowCmd);
	dismissButton.addActionListener(ws);
	gridbag.setConstraints(dismissButton, c);
	add(dismissButton);

	ws.displayInWindow(200, 200, 300, 300);
    }

    protected void finalize() {
	Debug.output("MiniBrowser getting gc'd");
    }

    public static void display(String content) {
	display("text/html", content);
    }

    public static void display(String mimeType, String content) {
	new MiniBrowser(mimeType, content);
    }
}
