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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/DirectoryPropertyEditor.java,v $
// $RCSfile: DirectoryPropertyEditor.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import javax.swing.*;

/** 
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * directory.  A single directpry choice can be made, and only choices
 * that reside on the local file system.
 */
public class DirectoryPropertyEditor extends FilePropertyEditor {
    
    /** Create DirectoryPropertyEditor.  */
    public DirectoryPropertyEditor() {
	button = new JButton("Select directory...");
    }

    /**
     * Returns a JFileChooser that will choose a directory.
     * @return JFileChooser
     */
    public JFileChooser getFileChooser() {
	JFileChooser chooser = new JFileChooser();
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	return chooser;
    }
}
