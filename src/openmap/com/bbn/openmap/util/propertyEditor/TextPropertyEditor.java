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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/TextPropertyEditor.java,v $
// $RCSfile: TextPropertyEditor.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A PropertyEditor that displays a TextField to edit a String.
 */
public class TextPropertyEditor extends PropertyEditorSupport implements
        ActionListener, FocusListener {

    /** The GUI component of this editor. */
    JTextField textField = new JTextField(10);

    public boolean supportsCustomEditor() {
        return true;
    }

    /** Returns the editor GUI, ie a JTextField. */
    public Component getCustomEditor() {
        JPanel jp = new JPanel();
        textField.addActionListener(this);
        textField.addFocusListener(this);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        jp.setLayout(gridbag);

        c.weightx = 1f;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(textField, c);
        jp.add(textField);

        return jp;
    }

    public void actionPerformed(ActionEvent e) {
        //System.out.println("value changed");
        firePropertyChange();
    }

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
        firePropertyChange();
    }

    /** Sets String in JTextField. */
    public void setValue(Object string) {
        if (!(string instanceof String))
            return;
        textField.setText((String) string);
    }

    /** Returns String from JTextfield. */
    public String getAsText() {
        return textField.getText();
    }
}