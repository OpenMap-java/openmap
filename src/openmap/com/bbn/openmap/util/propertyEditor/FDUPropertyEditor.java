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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/FDUPropertyEditor.java,v $
// $RCSfile: FDUPropertyEditor.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;

/** 
 * FDUPropertyEditor - File, Directory and URL PropertyEditor.  This
 * is a PropertyEditor that provides a text field where a URL, file
 * path or directory path can be entered.  There is also a button that
 * brings up a file chooser, and anything chosen *replaces* the
 * contents in the text field.  
 */
public class FDUPropertyEditor extends MultiDirectoryPropertyEditor {
    
    /** Create MultiDirectoryPropertyEditor.  */
    public FDUPropertyEditor() {
	button = new JButton("Set");
    }

    /**
     * Returns a JButton that will bring up a JFileChooser dialog.
     * @return JButton button
     */
    public Component getCustomEditor() {
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser chooser = getFileChooser();
		    int returnVal = chooser.showDialog((Component)null, "Select");
		    if (returnVal==JFileChooser.APPROVE_OPTION) {
			String newFilename = chooser.getSelectedFile().getAbsolutePath();
			FDUPropertyEditor.this.setValue(newFilename);
			firePropertyChange();
		    }
		}
	    });

	JPanel jp = new JPanel();
	jp.add(textField);
	jp.add(button);
	return jp;
    }

    /**
     * Returns a JFileChooser that will choose a directory.  The
     * MultiSelectionEnabled doesn't work yet, so we have to have a workaround.
     * @return JFileChooser 
     */
    public JFileChooser getFileChooser() {
	JFileChooser chooser = new JFileChooser(getLastLocation());
	chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	chooser.setMultiSelectionEnabled(true);
	return chooser;
    }
}
