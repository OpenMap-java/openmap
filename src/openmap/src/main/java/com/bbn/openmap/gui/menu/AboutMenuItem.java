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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/AboutMenuItem.java,v $
// $RCSfile: AboutMenuItem.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;

/**
 * This object brings up OpenMap about information.
 */
public class AboutMenuItem extends JMenuItem implements ActionListener {

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
            aboutBox.getContentPane().add(createAboutControls(aboutBox),
                    BorderLayout.SOUTH);
            aboutBox.pack();
        }

        aboutBox.setVisible(true);
    }

    protected JComponent createCopyrightViewer() {
        StringBuffer sb = new StringBuffer(MapBean.getCopyrightMessage())
                .append(Environment.get("line.separator"))
                .append(Environment.get("line.separator")).append("Version ")
                .append(Environment.get(Environment.Version));

        String buildDate = Environment.get(Environment.BuildDate);
        if (buildDate != null) {
            sb.append(Environment.get("line.separator")).append("Build ").append(buildDate);
        }

        JTextArea viewer = new JTextArea(sb.toString());
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
            return new JDialog((Frame) topContainer, title, true);
        } else {
            JDialog d = new JDialog();
            d.setTitle(title);
            return d;
        }
    }
}