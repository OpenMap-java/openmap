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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/ProfileDefinedState.java,v
// $
// $RCSfile: ProfileDefinedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.util.stateMachine.State;

class ProfileDefinedState extends State {

    protected ProfileGenerator profileTool;

    public ProfileDefinedState(ProfileGenerator tool) {
        profileTool = tool;
    }

    public boolean mousePressed(MouseEvent e) {
        profileTool.reset();
        return true;
    }

    public boolean mouseReleased(MouseEvent e) {
        profileTool.reset();
        return true;
    }

    public boolean mouseClicked(MouseEvent e) {
        profileTool.reset();
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        String ac = e.getActionCommand();
        if (ac.equalsIgnoreCase(TerrainLayer.createCommand)) {
            profileTool.layer.fireStatusUpdate(LayerStatusEvent.START_WORKING);
            profileTool.createProfileImage();
            profileTool.stateMachine.setState(ProfileStateMachine.TOOL_VIEW);
            profileTool.layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
        } else
            profileTool.reset();

    }
}