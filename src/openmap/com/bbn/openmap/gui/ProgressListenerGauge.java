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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ProgressListenerGauge.java,v $
// $RCSfile: ProgressListenerGauge.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import com.bbn.openmap.Environment;
import com.bbn.openmap.event.*;
import com.bbn.openmap.util.Debug;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ProgressListenerGauge extends JPanel 
    implements ProgressListener {

    protected JLabel message;
    protected JProgressBar jpb;

    /**
     * The frame used when the ProgressPanel is used in an application.  
     */
    protected transient JFrame progressWindowFrame;
    /**
     * The frame used when the ProgressPanel is used in an applet. 
     */
    protected transient JInternalFrame progressWindow;

    public ProgressListenerGauge() {
	init();
    }

    public ProgressListenerGauge(String windowTitle) {
	init();

	// Try to group the applet-specific stuff in here...
	if (Environment.getBoolean(Environment.UseInternalFrames)) {
	    
	    progressWindow = new JInternalFrame(
		windowTitle,
		/*resizable*/ true,
		/*closable*/ true,
		/*maximizable*/ false,
		/*iconifiable*/ true);
	    progressWindow.setContentPane(this);
	    progressWindow.pack();
	    progressWindow.setOpaque(true);
	    try {
		progressWindow.setClosed(true);//don't show until it's needed
	    } catch (java.beans.PropertyVetoException e) {}
	    setPosition(progressWindow);
	} else { // Working as an application...
	    progressWindowFrame = new JFrame(windowTitle);
	    progressWindowFrame.setContentPane(this);
	    progressWindowFrame.pack();
	    progressWindowFrame.setVisible(false);//don't show until it's needed
	    setPosition(progressWindowFrame);
	}
    }

    protected void init() {
	
	setLayout(new BorderLayout());
	add(new JLabel("     "), BorderLayout.EAST);
	add(new JLabel("     "), BorderLayout.WEST);
	add(new JLabel("      "), BorderLayout.NORTH);
	add(new JLabel("      "), BorderLayout.SOUTH);

	message = new JLabel("");
	jpb = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);

	JPanel cpanel = new JPanel();
	cpanel.setLayout(new GridLayout(0,1));
	cpanel.add(jpb);
	cpanel.add(message);

	add(cpanel, BorderLayout.CENTER);
	
	setPreferredSize(new Dimension(300, 75));
    }

    public void setVisible(boolean set) {

	if (progressWindow != null) {
	    if (progressWindow.isClosed()) {
		try {
		    progressWindow.setClosed(!set);
		} catch (java.beans.PropertyVetoException e) {}

		// hmmm is this the best way to do this?
		JLayeredPane desktop = 
		    Environment.getInternalFrameDesktop();
		
		if (desktop != null) {
		    desktop.remove(progressWindow);
		    desktop.add(progressWindow, 
				JLayeredPane.POPUP_LAYER);
		    progressWindow.setVisible(set);
		}
	    }
	} else if (progressWindowFrame != null) {
	    progressWindowFrame.setVisible(set);
	    progressWindowFrame.setState(Frame.NORMAL);
	} else {
	    super.setVisible(set);
	}
    }

    public void updateProgress(ProgressEvent evt) {

	int type = evt.getType();

	if (type == ProgressEvent.START ||
	    type == ProgressEvent.UPDATE) {

	    setVisible(true);
	    toFront();
	    message.setText(evt.getTaskDescription());
	    jpb.setValue(evt.getPercentComplete());
	} else {
	    setVisible(false);
	}
    }

    /**
     * For applications, checks where the Environment says the window
     * should be placed, and then uses the packed height and width to
     * make adjustments.
     */
    protected void setPosition(Component comp) {
	// get starting width and height
	int w = comp.getWidth();
	int h = comp.getHeight();

	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Debug.message("basic","Screen dimensions are " + d);
	int x = d.width/2 - w/2;
	int y = d.height/2 - h/2;
	
	if (Debug.debugging("basic")) {
	    Debug.output("Setting PLG frame X and Y from properties to " + x + " " + y);
	}

	// compose the frame, but don't show it here
	comp.setBounds(x,y,w,h);
    }

    // Trying to get the window to stay on top of everything else,
    // especially when the application is starting up.  Doesn't seem
    // to work.
    public void toFront() {
	if (progressWindow != null) {
	    progressWindow.toFront();
	} else if (progressWindowFrame != null) {
  	    progressWindowFrame.toFront();
	}
    }

}
