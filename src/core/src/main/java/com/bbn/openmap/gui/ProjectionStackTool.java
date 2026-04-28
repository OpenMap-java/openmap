// **********************************************************************
//
// <copyright>
//
// BBN Technologies, a Verizon Company
// 10 Moulton Street
// Cambridge, MA 02138
// (617) 873-8000
//
// Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ProjectionStackTool.java,v $
// $RCSfile: ProjectionStackTool.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 17:49:51 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.bbn.openmap.proj.ProjectionStack;
import com.bbn.openmap.proj.ProjectionStackTrigger;
import com.bbn.openmap.util.Debug;

/**
 * The ProjectionStackTool contains buttons that can trigger a
 * ProjectionStack to change a projection of a MapBean to a previous
 * projection, or to a later projection if the active projection is in
 * the middle of the stack. The OpenMap ProjectionStack will look for
 * one of these, and connect itself to it if it finds one.
 */
public class ProjectionStackTool extends OMToolComponent implements
        ProjectionStackTrigger {

    protected Vector listeners;

    protected JButton backButton;
    protected JButton forwardButton;
    // protected JButton clearButton;

    protected static transient String backName = "backproj.gif";
    protected static transient String forwardName = "forwardproj.gif";
    protected static transient String dimBackName = "dimbackproj.gif";
    protected static transient String dimForwardName = "dimforwardproj.gif";

    protected boolean dimBackButton = true;
    protected boolean dimForwardButton = true;

    ImageIcon backIcon;
    ImageIcon dimBackIcon;
    ImageIcon forwardIcon;
    ImageIcon dimForwardIcon;

    public ProjectionStackTool() {
        super();
        setKey("projectionstacktool");
        resetButtons(!dimBackButton, !dimForwardButton);
        add(backButton);
        add(forwardButton);
        // add(clearButton);
    }

    /**
     * Add an ActionListener for events that trigger events to shift
     * the Projection stack. If you are hooking up a ProjectionStack,
     * you don't need to call this. The ProjectionStack will call this
     * when you can addProjectionStackTrigger on it.
     */
    public void addActionListener(ActionListener al) {
        if (backButton != null && forwardButton != null) {
            backButton.addActionListener(al);
            forwardButton.addActionListener(al);
            // clearButton.addActionListener(al);
        } else {
            if (listeners == null) {
                listeners = new Vector();
            }
            listeners.add(al);
        }
    }

    /**
     * Remove an ActionListener that receives events that trigger
     * events to shift the Projection stack. If you are hooking up a
     * ProjectionStack, you don't need to call this. The
     * ProjectionStack will call this when you can
     * removeProjectionStackTrigger on it.
     */
    public void removeActionListener(ActionListener al) {
        if (backButton != null && forwardButton != null) {
            backButton.removeActionListener(al);
            forwardButton.removeActionListener(al);
            // clearButton.removeActionListener(al);
        } else if (listeners != null) {
            listeners.remove(al);
        }
    }

    /**
     * To receive a status to let the trigger know if any projections
     * in the forward or backward stacks exist, possibly to disable
     * any gui widgets.
     * 
     * @param containsBackProjections there is at least one past
     *        projection in the back cache.
     * @param containsForwardProjections there is at least one future
     *        projection in the forward cache. Used when a past
     *        projection is being used.
     */
    public void updateProjectionStackStatus(boolean containsBackProjections,
                                            boolean containsForwardProjections) {
        dimBackButton = !containsBackProjections;
        dimForwardButton = !containsForwardProjections;
        resetButtons(containsBackProjections, containsForwardProjections);
    }

    public void resetButtons(boolean enableBackButton,
                             boolean enableForwardButton) {

        if (backIcon == null) {
            backIcon = new ImageIcon(getClass().getResource(backName));
        }

        if (dimBackIcon == null) {
            dimBackIcon = new ImageIcon(getClass().getResource(dimBackName));
        }

        if (forwardIcon == null) {
            forwardIcon = new ImageIcon(getClass().getResource(forwardName));
        }

        if (dimForwardIcon == null) {
            dimForwardIcon = new ImageIcon(getClass().getResource(dimForwardName));
        }

        ImageIcon active;
        String toolTip;
        String disabled = " ("
                + i18n.get(ProjectionStackTool.class, "disabled", "disabled")
                + ")";
        int size;

        toolTip = i18n.get(ProjectionStackTool.class,
                "backTip",
                "Go back to previous projection");
        if (enableBackButton) {
            active = backIcon;
        } else {
            active = dimBackIcon;
            toolTip += " " + disabled;
        }

        if (backButton == null) {
            backButton = new JButton(active);
            backButton.setMargin(new Insets(0, 0, 0, 0));
            backButton.setBorderPainted(false);
            backButton.setActionCommand(ProjectionStack.BackProjCmd);
            if (listeners != null) {
                size = listeners.size();
                for (int i = 0; i < size; i++) {
                    backButton.addActionListener((ActionListener) listeners.elementAt(i));

                }
            }

        } else {
            backButton.setIcon(active);
        }
        backButton.setToolTipText(toolTip);

        toolTip = i18n.get(ProjectionStackTool.class,
                "forwardTip",
                "Go forward to next projection");
        if (enableForwardButton) {
            active = forwardIcon;
        } else {
            active = dimForwardIcon;
            toolTip += " " + disabled;
        }

        if (forwardButton == null) {
            forwardButton = new JButton(active);
            forwardButton.setMargin(new Insets(0, 0, 0, 0));
            forwardButton.setBorderPainted(false);
            forwardButton.setActionCommand(ProjectionStack.ForwardProjCmd);

            if (listeners != null) {
                size = listeners.size();
                for (int i = 0; i < size; i++) {
                    forwardButton.addActionListener((ActionListener) listeners.elementAt(i));

                }
            }
        } else {
            forwardButton.setIcon(active);
        }
        forwardButton.setToolTipText(toolTip);

        // if (clearButton == null) {
        // clearButton = new JButton("Clear Stack");
        // clearButton.setMargin(new Insets(0,0,0,0));
        // clearButton.setBorderPainted(false);
        // clearButton.setActionCommand(ProjectionStack.ClearStacksCmd);

        // if (listeners != null) {
        // size = listeners.size();
        // for (int i = 0; i < size; i++) {
        // clearButton.addActionListener((ActionListener)listeners.elementAt(i));
        // }
        // }
        // }
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof ProjectionStack) {
            Debug.message("projectionstacktrigger",
                    "ProjectionStackTrigger adding a ProjectionStack");
            ((ProjectionStack) someObj).addProjectionStackTrigger(this);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof ProjectionStack) {
            Debug.message("projectionstacktrigger",
                    "ProjectionStackTrigger removing a ProjectionStack");
            ((ProjectionStack) someObj).removeProjectionStackTrigger(this);
        }
    }
}

