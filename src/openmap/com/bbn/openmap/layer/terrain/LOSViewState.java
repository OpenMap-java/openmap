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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSViewState.java,v
// $
// $RCSfile: LOSViewState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import java.awt.event.ActionEvent;

import com.bbn.openmap.util.stateMachine.State;

class LOSViewState extends State {

    protected LOSGenerator LOSTool;

    public LOSViewState(LOSGenerator tool) {
        LOSTool = tool;
    }

    public void actionPerformed(ActionEvent e) {
        String ac = e.getActionCommand();
        if (ac.equalsIgnoreCase(TerrainLayer.createCommand)) {
            LOSTool.doImage();
        } else if (ac.equalsIgnoreCase(TerrainLayer.clearCommand)) {
            LOSTool.reset();
        }
    }
}

