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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSDrawState.java,v $
// $RCSfile: LOSDrawState.java,v $
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

class LOSDrawState extends State{

    protected LOSGenerator LOSTool;

    public LOSDrawState(LOSGenerator tool){
	LOSTool = tool;
    }

    public boolean mouseReleased(MouseEvent e){ 
	LOSTool.addLOSEvent(e);
	LOSTool.layer.repaint();
	LOSTool.stateMachine.setState(LOSStateMachine.TOOL_DEFINED);
	return true;
    }

    public boolean mouseDragged(MouseEvent e){
	LOSTool.addLOSEvent(e);
	LOSTool.layer.repaint();
	return true;
    }
}










