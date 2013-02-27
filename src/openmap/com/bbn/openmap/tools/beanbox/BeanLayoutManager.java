/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

/**
 * A BeanLayoutManager is a bean that manages the layout of a
 * BeanContainer.
 */
public abstract class BeanLayoutManager implements java.io.Serializable {
    /**
     * BeanContainer whose contents are laid out using this layout
     * manager.
     */
    protected BeanContainer _container;

    /** default constructor needed for beanification. */
    public BeanLayoutManager() {}

    /**
     * Called by the container to layout its contents. Default method
     * does nothing.
     */
    public abstract void layoutContainer();

    /** sets the container. */
    public void setContainer(BeanContainer c) {
        _container = c;
    }

    /** Gets the container associated with this layout manager. */
    public BeanContainer getContainer() {
        return _container;
    }

}