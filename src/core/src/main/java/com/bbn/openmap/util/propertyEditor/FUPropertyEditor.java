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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/FUPropertyEditor.java,v $
// $RCSfile: FUPropertyEditor.java,v $
// $Revision: 1.4 $
// $Date: 2005/05/24 17:55:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;


/**
 * FUPropertyEditor - File and URL PropertyEditor. This is a
 * PropertyEditor that provides a text field where a URL or file path
 * can be entered. There is also a button that brings up a file
 * chooser, and anything chosen *replaces* the contents in the text
 * field.
 */
public class FUPropertyEditor extends FilePropertyEditor {

    /** Create FUPropertyEditor. */
    public FUPropertyEditor() {}


    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return true for FUPropertyEditor.
     */
    public boolean isTextFieldEditable() {
        return true;
    }
}