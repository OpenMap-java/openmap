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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/ProfileDrawState.java,v $
// $RCSfile: ProfileDrawState.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.terrain;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.event.LayerStatusEvent;

class ProfileDrawState extends State{

    protected ProfileGenerator profileTool;

    public ProfileDrawState(ProfileGenerator tool){
	profileTool = tool;
    }

    public boolean mouseReleased(MouseEvent e){ 
	profileTool.addProfileEvent(e);
	profileTool.layer.repaint();
	return true;
    }

    public boolean mouseDragged(MouseEvent e){
	profileTool.addProfileEvent(e);
	profileTool.layer.repaint();
	return true;
    }

    public boolean mouseClicked(MouseEvent e){
	if (e.getClickCount() > 1){
	    profileTool.stateMachine.setState(ProfileStateMachine.TOOL_DEFINED);
	}
	return true;
    }

    public void actionPerformed(ActionEvent e){
	String ac = e.getActionCommand();
	if (ac.equalsIgnoreCase(TerrainLayer.createCommand)){
	    profileTool.layer.fireStatusUpdate(LayerStatusEvent.START_WORKING);
	    profileTool.createProfileImage();
	    profileTool.stateMachine.setState(ProfileStateMachine.TOOL_VIEW);
	    profileTool.layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	}
	else if (ac.equalsIgnoreCase(TerrainLayer.clearCommand)){
	    profileTool.reset();
	}
    }
}










