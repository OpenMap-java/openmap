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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/OM3DViewerLauncher.java,v $
// $RCSfile: OM3DViewerLauncher.java,v $
// $Revision: 1.7 $
// $Date: 2006/03/06 16:40:28 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MapHandlerChild;
import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.tools.j3d.geometry.Curtain;
import com.bbn.openmap.util.Debug;

/**
 * @author dietrick
 */
public class OM3DViewerLauncher extends MapHandlerChild implements
        ActionListener, Tool, OM3DGraphicHandler {

    /**
     * Default key for the DrawingToolLauncher Tool.
     */
    public final static String defaultKey = "OM3DViewerLauncher";
    /**
     * The key used when this DrawingToolLauncher is used as a Tool.
     */
    protected String key = defaultKey;
    /**
     * The frame used when the DrawingToolLauncher is used in an application.
     */
    protected transient JFrame viewer;

    public final static String CreateCmd = "CREATE";

    public OM3DViewerLauncher() {}

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand().intern();

        Debug.message("3d", "OM3DViewerLauncher.actionPerformed(): " + command);

        if (command == CreateCmd) {

            viewer = MapContentManager.getFrame("OpenMap 3D",
                    500,
                    500,
                    (MapHandler) getBeanContext(),
                    new javax.media.j3d.Background(.3f, .3f, .3f),
                    OM3DConstants.CONTENT_MASK_OMGRAPHICHANDLERLAYERS
                            | OM3DConstants.CONTENT_MASK_OM3DGRAPHICHANDLERS);
            viewer.setVisible(true);
        }
    }

    /**
     * This is the method that your object can use to find other objects within
     * the MapHandler (BeanContext). This method gets called when the object
     * gets added to the MapHandler, or when another object gets added to the
     * MapHandler after the object is a member.
     * 
     * @param it Iterator to use to go through a list of objects. Find the ones
     *        you need, and hook yourself up.
     */
    public void findAndInit(Iterator it) {}

    /**
     * BeanContextMembershipListener method. Called when a new object is removed
     * from the BeanContext of this object. For the Layer, this method doesn't
     * do anything. If your layer does something with the childrenAdded method,
     * or findAndInit, you should take steps in this method to unhook the layer
     * from the object used in those methods.
     * 
     * @param bcme Description of the Parameter
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {}

    /**
     * Tool interface method. The retrieval tool's interface. This method
     * creates a button that will bring up the LauncherPanel.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        JButton launcherButton = new JButton(new ImageIcon(OM3DViewerLauncher.class.getResource("launcher.gif"), "3D Viewer Launcher"));
        launcherButton.setBorderPainted(false);
        launcherButton.setToolTipText("3D Viewer Launcher");
        launcherButton.setMargin(new Insets(0, 0, 0, 0));
        launcherButton.addActionListener(this);
        launcherButton.setActionCommand(CreateCmd);
        return launcherButton;
    }

    /**
     */
    public void resetGUI() {}

    /**
     * Tool interface method. The retrieval key for this tool.
     * 
     * @return String The key for this tool.
     */
    public String getKey() {
        return key;
    }

    /**
     * Tool interface method. Set the retrieval key for this tool.
     * 
     * @param aKey The new key value
     */
    public void setKey(String aKey) {
        key = aKey;
    }

    protected Curtain curtain = new Curtain();

    /**
     * OM3DGraphicHandler method, gets called from MapContent.
     * 
     * @see OM3DGraphicHandler
     */
    public void addGraphicsToScene(MapContent mapContent) {
        curtain.addGraphicsToScene(mapContent);
    }

    public void setOrientation(int orientation) {}

    public int getOrientation() {
        return SwingConstants.HORIZONTAL;
    }

}