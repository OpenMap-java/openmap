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
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import javax.swing.JButton;
import javax.swing.JFileChooser;

/**
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * directory. A single directpry choice can be made, and only choices
 * that reside on the local file system.
 */
public class DirectoryPropertyEditor extends FilePropertyEditor {

    /** Create DirectoryPropertyEditor. */
    public DirectoryPropertyEditor() {
        button = new JButton("Select directory...");
    }

    /**
     * Returns a JFileChooser that will choose a directory.
     * 
     * @return JFileChooser
     */
    public JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser;
    }
}