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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProgressEvent.java,v $
// $RCSfile: ProgressEvent.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * An event that provides information on progress on a certain task.
 */
public class ProgressEvent extends java.util.EventObject {

    /**
     * Progress event type, start process.
     */
    public static final int START = 0;
    /**
     * Progress event type, update of current process.
     */
    public static final int UPDATE = 1;
    /**
     * Progress event type, notification of process completion.
     */
    public static final int DONE = 2;

    protected float finishedValue = 0;
    protected float currentValue = 0;
    protected String taskDescription = "";
    protected int type = 0;

    /**
     * Construct a ProgressEvent.
     * 
     * @param source Object
     * @param finishValue the ending value
     * @param currentValue the current value
     */
    public ProgressEvent(Object source, int type, String taskDescription,
            float finishValue, float currentValue) {
        super(source);
        this.finishedValue = finishValue;
        this.currentValue = currentValue;
        this.taskDescription = taskDescription;
        this.type = type;
    }

    /**
     * Get the value that current will have to get to to be finished.
     * 
     * @return finished.
     */
    public float getFinishedValue() {
        return finishedValue;
    }

    /**
     * Get the current value representing progress.
     */
    public float getCurrentValue() {
        return currentValue;
    }

    /**
     * Get a string describing what the task is.
     */
    public String getTaskDescription() {
        return taskDescription;
    }

    /**
     * Provide a percentage of progress completed, or -1 if no
     * finished value has been provided.
     */
    public int getPercentComplete() {
        if (finishedValue != 0) {
            int ret = (int) (currentValue / finishedValue * 100f);
            //          com.bbn.openmap.util.Debug.output("pe.percentComplete:
            // " + currentValue + "/" + finishedValue + " = " + ret);
            return ret;
        } else {
            return -1;
        }
    }

    public int getType() {
        return type;
    }
}