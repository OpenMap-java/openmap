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
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import javax.swing.JFileChooser;

/**
 * FUPropertyEditor - File and URL PropertyEditor. This is a
 * PropertyEditor that provides a text field where a URL or file path
 * can be entered. There is also a button that brings up a file
 * chooser, and anything chosen *replaces* the contents in the text
 * field.
 */
public class FUPropertyEditor extends FDUPropertyEditor {

    /** Create MultiDirectoryPropertyEditor. */
    public FUPropertyEditor() {
        super();
    }

    /**
     * Returns a JFileChooser that will choose a directory. The
     * MultiSelectionEnabled doesn't work yet, so we have to have a
     * workaround.
     * 
     * @return JFileChooser
     */
    public JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser(getLastLocation());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        return chooser;
    }
}