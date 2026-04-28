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
 * An interface implemented by property editors requiring access to
 * the bean whose properties they are editing.
 */
public interface GenericPropertyEditorInterface {

    /**
     * This method is called by the
     * {@link com.bbn.openmap.tools.beanbox.GenericPropertySheet}on
     * property editors that implement this interface to set the
     * target bean in them. An editor implementing this method can use
     * it to cache the target bean.
     */
    public void setTargetBean(Object bean);

    /**
     * gets the target bean associated with a property editor.
     */
    public Object getTargetBean();
}