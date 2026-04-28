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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/EOMGListenerSupport.java,v $
// $RCSfile: EOMGListenerSupport.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.util.Debug;

/**
 * This is a utility class that can be used by beans that need support
 * for handling EOMGListeners and calling the EOMGListener method. You
 * can use an instance of this class as a member field of your bean
 * and delegate work to it.
 */
public class EOMGListenerSupport extends ListenerSupport<EOMGListener> {

    /**
     * Construct a EOMGListenerSupport.
     */
    public EOMGListenerSupport() {
        this(null);
    }

    /**
     * Construct a EOMGListenerSupport.
     * 
     * @param graphic source graphic
     */
    public EOMGListenerSupport(EditableOMGraphic graphic) {
        super(graphic);
    }

    /**
     * Set the source object.
     * 
     * @param graphic source EditableOMGraphic
     */
    public synchronized void setEOMG(EditableOMGraphic graphic) {
        setSource(graphic);
    }

    /**
     * Get the source object.
     * 
     * @return EditableOMGraphic
     */
    public synchronized EditableOMGraphic getEOMG() {
        return (EditableOMGraphic) getSource();
    }

    /**
     * Send a eomgChanged event to all registered listeners.
     * 
     * @param event EOMGEvent
     */
    public synchronized void fireEvent(EOMGEvent event) {

        if (isEmpty())
            return;

        for (EOMGListener target : this) {
            target.eomgChanged(event);

            if (Debug.debugging("eomgdetail")) {
                Debug.output("EOMGListenerSupport.fireStatusChanged(): target is: "
                        + target);
            }
        }
    }
}