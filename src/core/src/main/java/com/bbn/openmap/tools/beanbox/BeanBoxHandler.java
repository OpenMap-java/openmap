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
 * The BeanBoxHandler interface should be implemented by an openmap
 * layer that needs to interact with the
 * {@link com.bbn.openmap.tools.beanbox.BeanBoxDnDCatcher}via the
 * {@link com.bbn.openmap.tools.beanbox.BeanBox}instance associated
 * with the layer. The BeanBoxDnDCatcher class uses the getBeanBox()
 * method declared on this interface to obtain access to the
 * {@link com.bbn.openmap.tools.beanbox.BeanBox}object associated
 * with the layer.
 */
public interface BeanBoxHandler {

    /**
     * returns a layer specific implementation of the
     * {@link com.bbn.openmap.tools.beanbox.BeanBox}class instance.
     */
    public BeanBox getBeanBox();
}