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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/BackgroundColorButton.java,v $
// $RCSfile: BackgroundColorButton.java,v $
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

public class BackgroundColorButton extends JMenuItem implements ActionListener {

    protected MapBean map = null;

    public BackgroundColorButton(String title) {
	super(title);
	addActionListener(this);
    }

    public void setMap(MapBean mapbean) {
	map = mapbean;
    }

    public MapBean getMap() {
	return map;
    }

    public void actionPerformed(ActionEvent ae) {
	if (map != null) {
	    Paint newPaint = OMColorChooser.showDialog(this, getText(),
						       map.getBackgroundColor());
	    if (newPaint != null) {

		String colorString = Integer.toString(((java.awt.Color)newPaint).getRGB());
		Environment.set(Environment.BackgroundColor, colorString);
		((com.bbn.openmap.proj.Proj)map.getProjection()).setBackgroundColor((java.awt.Color)newPaint);
		map.setBckgrnd(newPaint);
	    }
	}
    }
}
