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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/MultiDirectoryPropertyEditor.java,v $
// $RCSfile: MultiDirectoryPropertyEditor.java,v $
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
 * A PropertyEditor that brings up a JFileChooser panel to several
 * directories. You can enter information in the text field, and
 * pressing the add button will bring up a file chooser. Anything
 * chosen in the file chooser will be appended to what is currently in
 * the text field.
 */
public class MultiDirectoryPropertyEditor extends FilePropertyEditor {
    
    /** The GUI component of this editor. */
    protected JTextField textField = new JTextField(15);
    protected char pathSeparator;

    /** Create MultiDirectoryPropertyEditor.  */
    public MultiDirectoryPropertyEditor() {
	button = new JButton("Add");
	setPathSeparator(';');
    }

    /**
     * Set the character to use when appending paths.
     */
    public void setPathSeparator(char c) {
	pathSeparator = c;
    }

    public char getPathSeparator() {
	return pathSeparator;
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
			MultiDirectoryPropertyEditor.this.append(newFilename);
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
	JFileChooser chooser = new JFileChooser();
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	chooser.setMultiSelectionEnabled(true);
	return chooser;
    }

    /**
     * Add a path to the end of the current path.  Uses the
     * pathSeparator between paths.
     */
    public void append(String addPath) {
	String currentPath = textField.getText();
	if (currentPath.equals("")) {
	    setValue(addPath);
	} else {
	    setValue(currentPath.concat(";" + addPath));
	}
    }

    /** Sets String in JTextField. */
    public void setValue(Object string) {
	if(!(string instanceof String))
	    return;
	textField.setText((String)string);
    }

    /** Returns String from JTextfield. */
    public String getAsText() {
	return textField.getText();
    }

}
