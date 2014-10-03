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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/DirectoryPropertyEditor.java,v $
// $RCSfile: DirectoryPropertyEditor.java,v $
// $Revision: 1.4 $
// $Date: 2005/05/24 17:55:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import javax.swing.JFileChooser;

/**
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * directory. A single directory choice can be made, and only choices
 * that reside on the local file system.
 */
public class DirectoryPropertyEditor extends FilePropertyEditor {

    /**
     *  
     */
    public DirectoryPropertyEditor() {}

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return JFileChooser.DIRECTORIES_ONLY for
     *         DirectoryPropertyEditor.
     */
    public int getFileSelectionMode() {
        return JFileChooser.DIRECTORIES_ONLY;
    }

}