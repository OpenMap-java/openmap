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
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:07 $
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

    protected boolean createWindowsForDisplay = false;
    protected String title;

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

        createWindowsForDisplay = true;
        title = windowTitle;
    }

    protected void manageWindow(boolean visible) {
        if (visible) {
            if (progressWindow == null && progressWindowFrame == null) {
                // create one or the other, try to group the
                // applet-specific stuff in here...
                if (Environment.getBoolean(Environment.UseInternalFrames)) {
                    progressWindow = new JInternalFrame(
                        title,
                        /*resizable*/ true,
                        /*closable*/ true,
                        /*maximizable*/ false,
                        /*iconifiable*/ true);
                    progressWindow.setContentPane(this);
                    progressWindow.pack();
                    progressWindow.setOpaque(true);
                    progressWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    setPosition(progressWindow);

                    JLayeredPane desktop = 
                        Environment.getInternalFrameDesktop();
                
                    if (desktop != null) {
                        desktop.remove(progressWindow);
                        desktop.add(progressWindow, 
                                    JLayeredPane.POPUP_LAYER);
                        progressWindow.show();
                    }

                } else { // Working as an application...
                    progressWindowFrame = new JFrame(title);
                    progressWindowFrame.setContentPane(this);
                    progressWindowFrame.pack();
                    setPosition(progressWindowFrame);
                    progressWindowFrame.setState(Frame.NORMAL);
                    progressWindowFrame.show();
                    progressWindowFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                }
            }

            if (progressWindow != null) {
                progressWindow.toFront();
            } else if (progressWindowFrame != null) {
                progressWindowFrame.toFront();
            }

        } else {
            if (progressWindow != null) {
                progressWindow.dispose();
                progressWindow = null;
            } else if (progressWindowFrame != null) {
                progressWindowFrame.dispose();
                progressWindowFrame = null;
            }
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
        if (createWindowsForDisplay) {
            manageWindow(set);
        } else {
            super.setVisible(set);
        }
    }

    public void updateProgress(ProgressEvent evt) {

        int type = evt.getType();

        if (type == ProgressEvent.START ||
            type == ProgressEvent.UPDATE) {

            setVisible(true);
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
