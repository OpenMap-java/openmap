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
// $Revision: 1.2 $
// $Date: 2003/03/19 20:41:54 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import java.beans.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;

/** 
 * A PropertyEditor that displays an either/or option.  The widget
 * returns true or false as a string when queried, or the button's
 * action commands if set differently in subclasses.  If you extend
 * this class and override the constructor, you can change the two
 * choices presented, like "enabled/disabled, on/off", etc.  The
 * responses will be true and false, unless the action commands for
 * the trueButton and falseButton are set to something else and
 * useAltCommandStrings is set to true.
 */
public class TrueFalsePropertyEditor extends PropertyEditorSupport
    implements ActionListener, FocusListener {
    
    protected ButtonGroup buttonGroup = new ButtonGroup();
    protected boolean option = true;
    protected JRadioButton trueButton;
    protected JRadioButton falseButton;
    protected boolean useAltCommandStrings = false;

    public final static String TrueString = "true";
    public final static String FalseString = "false";

    public TrueFalsePropertyEditor() {
	trueButton = new JRadioButton(TrueString);
	falseButton = new JRadioButton(FalseString);
    }
    
    public boolean supportsCustomEditor() {
	return true;
    }
    
    public void setUseAltCommandStrings(boolean value) {
	useAltCommandStrings = value;
    }

    public boolean getUseAltCommandStrings() {
	return useAltCommandStrings;
    }

    /** Returns the editor GUI, ie a JTextField. */
    public Component getCustomEditor() {
	JPanel panel = new JPanel();

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	panel.setLayout(gridbag);

	if (!getUseAltCommandStrings()) {
	    trueButton.setActionCommand(TrueString);
	    falseButton.setActionCommand(FalseString);
	}
	trueButton.addActionListener(this);
	falseButton.addActionListener(this);
	
	buttonGroup.add(trueButton);
	buttonGroup.add(falseButton);

	setSelected(option);

	gridbag.setConstraints(trueButton, c);
	gridbag.setConstraints(falseButton, c);
	panel.add(trueButton);
	panel.add(falseButton);
	
	return panel;
    }
    
    public void actionPerformed(ActionEvent e) {
	String ac = e.getActionCommand();
	setSelected(ac.equalsIgnoreCase(trueButton.getActionCommand()));
	
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

	setSelected(((String)string).equalsIgnoreCase(trueButton.getActionCommand()));
    }

    /** Returns String from ButtonGroup. */
    public String getAsText() {
	if (option) {
	    return trueButton.getActionCommand();
	}
	return falseButton.getActionCommand();
    }
}
