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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSViewState.java,v $
// $RCSfile: LOSViewState.java,v $
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

class LOSViewState extends State{

    protected LOSGenerator LOSTool;

    public LOSViewState(LOSGenerator tool){
	LOSTool = tool;
    }

    public void actionPerformed(ActionEvent e){
	String ac = e.getActionCommand();
	if (ac.equalsIgnoreCase(TerrainLayer.createCommand)){
	    LOSTool.doImage();
	}
	else if (ac.equalsIgnoreCase(TerrainLayer.clearCommand)){
	    LOSTool.reset();
	}
    }
}









