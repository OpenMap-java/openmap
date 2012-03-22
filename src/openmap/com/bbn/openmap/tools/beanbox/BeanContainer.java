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

import java.util.Vector;

/**
 * The {@link com.bbn.openmap.tools.beanbox.BeanBox}treats all bean
 * objects that implement this interface as container beans.
 */
public interface BeanContainer {

    /**
     * gets the contents of this container as a vector of bean
     * objects.
     */
    public Vector getContents();

    /**
     * sets the contents of this container as a vector of bean
     * objects.
     */
    public void setContents(Vector contents);

    /**
     * adds the specified bean object to this container.
     */
    public void add(Object bean);

    /**
     * removes the specified bean object from this container.
     */
    public void remove(Object bean);

    /**
     * removes all beans from this container.
     */
    public void removeAll();

    /**
     * checks if the specified bean lies in this container.
     */
    public boolean contains(Object bean);

    /**
     * gets the layout manager bean assocciated with this container.
     */
    public BeanLayoutManager getLayout();

    /**
     * sets the layout manager bean assocciated with this container.
     * The layout manager bean is responsible for laying out the
     * contents of this container.
     */
    public void setLayout(BeanLayoutManager layout);

    /**
     * gets the class name of the layout manager bean assocciated with
     * this container.
     */
    public String getLayoutClass();

    /**
     * sets the class name of the layout manager bean assocciated with
     * this container.
     */
    public void setLayoutClass(String lc);

    /**
     * calling this method is meant to generate a call to the layout
     * manager's layoutContainer method.
     */
    public void validate();
}