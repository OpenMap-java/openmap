// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ProgressListenerGauge.java,v
// $
// $RCSfile: ProgressListenerGauge.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import com.bbn.openmap.event.*;
import com.bbn.openmap.util.Debug;

import java.awt.*;
import javax.swing.*;

public class ProgressListenerGauge extends JPanel implements ProgressListener {

    protected JLabel message;
    protected JProgressBar jpb;

    protected boolean createWindowsForDisplay = false;
    protected String title;

    protected WindowSupport windowSupport = null;

    public ProgressListenerGauge() {
        init();
    }

    public ProgressListenerGauge(String windowTitle) {
        createWindowsForDisplay = true;
        title = windowTitle;
        init();
    }

    protected synchronized void manageWindow(boolean visible) {
        if (visible) {
            if (windowSupport == null) {
                windowSupport = new WindowSupport(this, title);
            }
            windowSupport.displayInWindow();
        } else {
            if (windowSupport != null) {
                windowSupport.killWindow();
            }
        }
    }

    protected synchronized void init() {
        setLayout(new BorderLayout());
        add(new JLabel("     "), BorderLayout.EAST);
        add(new JLabel("     "), BorderLayout.WEST);
        add(new JLabel("      "), BorderLayout.NORTH);
        add(new JLabel("      "), BorderLayout.SOUTH);
        message = new JLabel("");
        jpb = new JProgressBar(0, 100);
        JPanel cpanel = new JPanel();
        cpanel.setLayout(new GridLayout(0, 1));
        cpanel.add(jpb);
        cpanel.add(message);

        add(cpanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(300, 75));
    }

    public synchronized void setVisible(boolean set) {
        if (createWindowsForDisplay) {
            manageWindow(set);
        } else {
            super.setVisible(set);
        }
    }

    public synchronized void updateProgress(ProgressEvent evt) {
        int type = evt.getType();

        if (type == ProgressEvent.START || type == ProgressEvent.UPDATE) {
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
    protected synchronized void setPosition(Component comp) {
        // get starting width and height
        int w = comp.getWidth();
        int h = comp.getHeight();

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Debug.message("basic", "Screen dimensions are " + d);
        int x = d.width / 2 - w / 2;
        int y = d.height / 2 - h / 2;

        if (Debug.debugging("basic")) {
            Debug.output("Setting PLG frame X and Y from properties to " + x
                    + " " + y);
        }

        // compose the frame, but don't show it here
        comp.setBounds(x, y, w, h);
    }

}