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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Tool.java,v $
// $RCSfile: Tool.java,v $
// $Revision: 1.3 $
// $Date: 2003/12/23 20:47:47 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

/** 
 * Represents an item on the ToolPanel.  If the Tool is a
 * PropertyConsumer and a property prefix is used, the prefix should
 * be used as the key as it is supposed to be unique and can be used
 * as a known discriminator.
 *
 * @author john gash contributed to this notion of a Tool.
 */

public interface Tool {

    /** 
     * The retrieval tool's interface. This is added to the
     * tool bar.
     *
     * @return String The key for this tool.
     */
    public Container getFace();
    
    /** 
     * The retrieval key for this tool.
     *
     * @return String The key for this tool.
     */
    public String getKey();
    
    /** 
     * Set the retrieval key for this tool.
     *
     * @param aKey The key for this tool.
     */
    public void setKey(String aKey);
}
