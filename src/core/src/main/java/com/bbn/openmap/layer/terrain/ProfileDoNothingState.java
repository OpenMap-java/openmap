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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/ProfileDoNothingState.java,v
// $
// $RCSfile: ProfileDoNothingState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import java.awt.event.MouseEvent;

import com.bbn.openmap.util.stateMachine.State;

class ProfileDoNothingState extends State {

    protected ProfileGenerator profileTool;

    public ProfileDoNothingState(ProfileGenerator tool) {
        profileTool = tool;
    }

    public boolean mousePressed(MouseEvent e) {
        profileTool.addProfileEvent(e);
        profileTool.stateMachine.setState(ProfileStateMachine.TOOL_DRAW);
        return true;
    }
}

