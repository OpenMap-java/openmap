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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/stateMachine/StateMachine.java,v $
// $RCSfile: StateMachine.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.stateMachine;

import java.util.*;

/** The StateMachine lets you organize event handling, if the order of
 * the events are important to you.  You can use the setState commands
 * with the state you want, if you are holding on a copy of it.
 * Otherwise, the state machine assumes you know the index of the
 * state you want.
 * */
public class StateMachine {
    /** The states to track. */
    public Vector states = new Vector();
    /** The current state that will receive the next event. */
    protected State currentState;
    /** The state to go to whan all is bad. */
    protected State resetState;

    public StateMachine(){}

    /** Define the state machine using the array of states.  Order is
     * maintained. 
     * @param s array of states.
     * */
    public StateMachine(State[] s){
	for (int i = 0; i < s.length; i++)
	    states.addElement(s[i]);
    }

    /** Sets the current state to the reset state. */
    public void reset(){
	currentState = resetState;
    }

    /** Set the states to the new array.
     * @param s array of states.
     * */
    public void setStates(State[] s){
	for (int i = 0; i < s.length; i++)
	    states.addElement(s[i]);
    }

    /** Set the current state to the given state.  If the state is not
     * in the state machine, then the state is added to the end to the
     * state vector.
     * @param state the state to set to the current one.
     * */
    public void setState(State state){
	if (!states.contains(state))
	    states.addElement(state);
	currentState = state;
    }

    /** Set the current state to the state residing in the vector at
     * the given index.  If the index is larger than the number of
     * states in the machine, the statemachine is reset.
     * @param stateIndex the index of the current state.
     * */
    public void setState(int stateIndex){
	try {
	    currentState = (State) states.elementAt(stateIndex);
	} catch (ArrayIndexOutOfBoundsException e){
	    reset();
	    System.err.println("StateMachine: out of bounds exception caught!");
	}
    }

    /** Return the current State. */
    public State getState(){
	return currentState;
    }

    /** Return the state at the given index. If the index is larger
     * that the number of states, null is returned.
     * */
    public State getState(int stateIndex){
	try{
	    return (State) states.elementAt(stateIndex);
	} catch (ArrayIndexOutOfBoundsException e){
	    System.err.println("StateMachine: out of bounds exception caught!");
	    return null;
	}
    }

    /** Set the state to be gone to if the state machine is reset.  If
     * the state does not exist in the state machine already, it will
     * be added to the end of the state vector.
     * */
    public void setResetState(State state){
	if (!states.contains(state))
	    states.addElement(state);
	resetState = state;
    }

    /** Set the reset state to be used by the state machine.  If a bad
     * integer value is given, the first state in the state vector is
     * made the reset state.
     * */
    public void setResetState(int stateIndex){
	try{
	    resetState = (State) states.elementAt(stateIndex);
	} catch (ArrayIndexOutOfBoundsException e){
	    System.err.println("StateMachine: out of bounds exception caught!");
	    if (states.size() > 1) 
		resetState = (State) states.elementAt(0);
	}
    }

    /** Return the reset state. */
    public State getResetState(){
	return resetState;
    }

    /** Set the MapMouseListenerResponse for all the states. */
    public void setMapMouseListenerResponses(boolean value){
	Enumeration sItems = states.elements();
	while (sItems.hasMoreElements()){
	    State state = (State)sItems.nextElement();
	    state.setMapMouseListenerResponse(value);
	}
    }
}










