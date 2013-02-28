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

package com.bbn.openmap.layer.beanbox;

import com.bbn.openmap.tools.beanbox.BeanLayoutManager;

/**
 * A layout manager that represents a null layout. This is as good as
 * not specifying a layout for the
 * {@link SimpleBeanContainer}.
 */
public class NullLayout extends BeanLayoutManager {

    /** does nothing. */
    public void layoutContainer() {
    // NOOP
    }
}