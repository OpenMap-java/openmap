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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/TrueFalsePropertyEditor.java,v $
// $RCSfile: TrueFalsePropertyEditor.java,v $
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
 * A PropertyEditor that displays a true/false option.  The widget
 * returns true or false as a string when queried.  If you extend this
 * class and override the constructor, you can change the two choices
 * presented, like "enabled/disabled, on/off", etc.  The two responses
 * will be true and false, however.
 */
public class TrueFalsePropertyEditor extends PropertyEditorSupport
    implements ActionListener, FocusListener {
    
    ButtonGroup buttonGroup = new ButtonGroup();
    boolean option = true;
    JRadioButton trueButton;
    JRadioButton falseButton;
    
    public final static String TrueString = "true";
    public final static String FalseString = "false";

    public TrueFalsePropertyEditor() {
	trueButton = new JRadioButton(TrueString);
	falseButton = new JRadioButton(FalseString);
    }
    
    public boolean supportsCustomEditor() {
	return true;
    }
    
    /** Returns the editor GUI, ie a JTextField. */
    public Component getCustomEditor() {
	JPanel panel = new JPanel();

	trueButton.setActionCommand(TrueString);
	trueButton.addActionListener(this);
	falseButton.setActionCommand(FalseString);
	falseButton.addActionListener(this);

	
	buttonGroup.add(trueButton);
	buttonGroup.add(falseButton);

	setSelected(option);

	panel.add(trueButton);
	panel.add(falseButton);
	
	return panel;
    }
    
    public void actionPerformed(ActionEvent e) {
	String ac = e.getActionCommand();
	setSelected(ac.equalsIgnoreCase(TrueString));
	
	//System.out.println("value changed");
	firePropertyChange();
    }

    public void setSelected(boolean set) {
	option = set;
	trueButton.setSelected(option);
	falseButton.setSelected(!option);
    }

    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) { firePropertyChange(); }
    
    /** Sets String in JTextField. */
    public void setValue(Object string) {
	if(!(string instanceof String)) {
	    return;
	}

	setSelected(((String)string).equalsIgnoreCase("true"));
    }

    /** Returns String from ButtonGroup. */
    public String getAsText() {
	if (option) {
	    return TrueString;
	}
	return FalseString;
    }
}
