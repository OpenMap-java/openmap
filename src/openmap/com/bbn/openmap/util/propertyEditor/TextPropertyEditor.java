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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/TextPropertyEditor.java,v $
// $RCSfile: TextPropertyEditor.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import java.beans.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.event.*;

/** 
 * A PropertyEditor that displays a TextField to edit a String. 
 */
public class TextPropertyEditor extends PropertyEditorSupport
    implements ActionListener, FocusListener {
    
    /** The GUI component of this editor. */
    JTextField textField = new JTextField(25);
    
    public boolean supportsCustomEditor() {
	return true;
    }
    
    /** Returns the editor GUI, ie a JTextField. */
    public Component getCustomEditor() {
	JPanel panel = new JPanel();
	textField.addActionListener(this);
	textField.addFocusListener(this);
	
	panel.add(textField);
	return panel;
    }
    
    public void actionPerformed(ActionEvent e) {
	//System.out.println("value changed");
	firePropertyChange();
    }

    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) { firePropertyChange(); }
    
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
