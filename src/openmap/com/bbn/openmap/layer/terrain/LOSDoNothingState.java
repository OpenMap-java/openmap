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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSDoNothingState.java,v $
// $RCSfile: LOSDoNothingState.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:11 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.terrain;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import com.bbn.openmap.layer.util.stateMachine.*;

class LOSDoNothingState extends State{

    protected LOSGenerator LOSTool;

    public LOSDoNothingState(LOSGenerator tool){
        LOSTool = tool;
    }

    public boolean mousePressed(MouseEvent e){ 
        LOSTool.setCenter(e);
        LOSTool.stateMachine.setState(LOSStateMachine.TOOL_DRAW);
        return true;
    }
}










