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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSDefinedState.java,v $
// $RCSfile: LOSDefinedState.java,v $
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

class LOSDefinedState extends State{

    protected LOSGenerator LOSTool;

    public LOSDefinedState(LOSGenerator tool){
	LOSTool = tool;
    }

    public boolean mousePressed(MouseEvent e){ 
	LOSTool.reset();
	LOSTool.layer.repaint();
	LOSTool.setCenter(e);
	LOSTool.stateMachine.setState(LOSStateMachine.TOOL_DRAW);
	return true;
    }

    public boolean mouseReleased(MouseEvent e){ 
	LOSTool.reset();
	return true;
    }

    public boolean mouseClicked(MouseEvent e){
	LOSTool.reset();
	return true;
    }

    public void actionPerformed(ActionEvent e){
	String ac = e.getActionCommand();
	if (ac.equalsIgnoreCase(TerrainLayer.createCommand)){
	    LOSTool.doImage();
	    LOSTool.stateMachine.setState(LOSStateMachine.TOOL_VIEW);
	}
	else LOSTool.reset();
    }
}
