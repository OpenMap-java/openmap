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
// $Revision: 1.7 $
// $Date: 2009/02/26 21:16:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressListener;

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
        if (SwingUtilities.isEventDispatchThread()) {
            updateProgressFromEDT(evt);
        } else {
            SwingUtilities.invokeLater(new MyWorker(evt));
        }
    }

    public synchronized void updateProgressFromEDT(ProgressEvent evt) {
        int type = evt.getType();

        if (type == ProgressEvent.START || type == ProgressEvent.UPDATE) {
            setVisible(true);
            message.setText(evt.getTaskDescription());
            jpb.setValue(evt.getPercentComplete());
        } else {
            setVisible(false);
        }
    }

    public WindowSupport getWindowSupport() {
        return windowSupport;
    }

    public void setWindowSupport(WindowSupport windowSupport) {
        this.windowSupport = windowSupport;
    }

    class MyWorker implements Runnable {

        private ProgressEvent pe;

        public MyWorker(ProgressEvent pe) {
            this.pe = pe;
        }

        public void run() {
            try {
                updateProgressFromEDT(pe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}