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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSStateMachine.java,v
// $
// $RCSfile: LOSStateMachine.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import com.bbn.openmap.util.stateMachine.State;
import com.bbn.openmap.util.stateMachine.StateMachine;

class LOSStateMachine extends StateMachine {

    public LOSGenerator losg;
    public static final int TOOL_DO_NOTHING = 0;
    public static final int TOOL_DRAW = 1;
    public static final int TOOL_DEFINED = 2;
    public static final int TOOL_VIEW = 3;

    public LOSStateMachine(LOSGenerator generator) {
        losg = generator;
        State[] losStates = init();
        setStates(losStates);

        // set reset state
        setResetState(TOOL_DO_NOTHING);
        reset();
        setMapMouseListenerResponses(true);
    }

    protected State[] init() {
        State[] LOSStates = new State[4];

        LOSStates[TOOL_DO_NOTHING] = new LOSDoNothingState(losg);
        LOSStates[TOOL_DRAW] = new LOSDrawState(losg);
        LOSStates[TOOL_DEFINED] = new LOSDefinedState(losg);
        LOSStates[TOOL_VIEW] = new LOSViewState(losg);

        return LOSStates;
    }
}

