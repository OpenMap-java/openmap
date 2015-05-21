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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/FDUPropertyEditor.java,v $
// $RCSfile: FDUPropertyEditor.java,v $
// $Revision: 1.7 $
// $Date: 2005/05/24 17:55:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import javax.swing.JFileChooser;

/**
 * FDUPropertyEditor - File, Directory and URL PropertyEditor. This is
 * a PropertyEditor that provides a text field where a URL, file path
 * or directory path can be entered. There is also a button that
 * brings up a file chooser, and anything chosen *replaces* the
 * contents in the text field.
 */
public class FDUPropertyEditor extends FilePropertyEditor {

    /** Create FDUDirectoryPropertyEditor. */
    public FDUPropertyEditor() {}

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return true for FDUPropertyEditor.
     */
    public boolean isTextFieldEditable() {
        return true;
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return JFileChooser.FILES_AND_DIRECTORIES for FDUPropertyEditor.
     */
    public int getFileSelectionMode() {
        return JFileChooser.FILES_AND_DIRECTORIES;
    }

}