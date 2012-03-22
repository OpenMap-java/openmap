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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSDefinedState.java,v
// $
// $RCSfile: LOSDefinedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import com.bbn.openmap.util.stateMachine.State;
class LOSDefinedState extends State {

    protected LOSGenerator LOSTool;

    public LOSDefinedState(LOSGenerator tool) {
        LOSTool = tool;
    }

    public boolean mousePressed(MouseEvent e) {
        LOSTool.reset();
        LOSTool.layer.repaint();
        LOSTool.setCenter(e);
        LOSTool.stateMachine.setState(LOSStateMachine.TOOL_DRAW);
        return true;
    }

    public boolean mouseReleased(MouseEvent e) {
        LOSTool.reset();
        return true;
    }

    public boolean mouseClicked(MouseEvent e) {
        LOSTool.reset();
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        String ac = e.getActionCommand();
        if (ac.equalsIgnoreCase(TerrainLayer.createCommand)) {
            LOSTool.doImage();
            LOSTool.stateMachine.setState(LOSStateMachine.TOOL_VIEW);
        } else
            LOSTool.reset();
    }
}