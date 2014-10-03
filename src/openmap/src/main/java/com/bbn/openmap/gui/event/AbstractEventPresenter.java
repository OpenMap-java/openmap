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
// $Source:
// /cvs/darwars/ambush/aar/src/com/bbn/ambush/gui/AbstractEventPresenter.java,v
// $
// $RCSfile: AbstractEventPresenter.java,v $
// $Revision: 1.1 $
// $Date: 2007/08/16 22:15:20 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.event;

import java.awt.Component;
import java.util.Iterator;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.util.PropUtils;

public abstract class AbstractEventPresenter extends OMComponentPanel implements
        EventPresenter {

    protected String name;

    public AbstractEventPresenter() {
        name = "";
    }

    public void setProperties(String prefix, Properties props) {
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        name = props.getProperty(prefix + Layer.PrettyNameProperty, name);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + Layer.PrettyNameProperty, name);
        return props;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public Component getComponent() {
        return this;
    }

    /**
     * Optional optimized form.  Passes through to the simpler form by default.
     * @param start Start time (leftmost edge of timeline).
     * @param end End time (rightmost edge of timeline).
     * @param step Time span represented by a single pixel width.
     * @return iterator of events appropriate for display given parameters provided.
     */
    public Iterator<OMEvent> getActiveEvents(long start, long end, long step) {
        return getActiveEvents();
    }

}