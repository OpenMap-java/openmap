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

import java.beans.BeanInfo;

/**
 * An action to perform on instantiated beans. Used by the BeanPanel.
 */

public interface DoOnBean {
    void action(JarInfo ji, BeanInfo bi, Class beanClass, String beanName);

    void error(String msg); // display an error message

    void error(String msg, Exception ex); // ditto, with an exception
}