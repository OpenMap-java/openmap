// Bart 20060831 -> i18n

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
// $Source: /home/cvs/nodus/src/com/bbn/openmap/util/propertyEditor/TrueFalsePropertyEditor.java,v $
// $RCSfile: TrueFalsePropertyEditor.java,v $
// $Revision: 1.2 $
// $Date: 2006-10-25 12:21:52 $
// $Author: jourquin $
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

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;

/**
 * A PropertyEditor that displays an either/or option. The widget
 * returns true or false as a string when queried, or the button's
 * action commands if set differently in subclasses. If you extend
 * this class and override the constructor, you can change the two
 * choices presented, like "enabled/disabled, on/off", etc. The
 * responses will be true and false, unless the action commands for
 * the trueButton and falseButton are set to something else and
 * useAltCommandStrings is set to true.
 */
public class TrueFalsePropertyEditor extends PropertyEditorSupport implements
        ActionListener, FocusListener {

    protected ButtonGroup buttonGroup = new ButtonGroup();
    protected boolean option = true;
    protected JRadioButton trueButton;
    protected JRadioButton falseButton;
    protected boolean useAltCommandStrings = false;
    
    //  I18N mechanism
    static I18n i18n = Environment.getI18n();

    public final static String TrueString = "true";
    public final static String FalseString = "false";

    public TrueFalsePropertyEditor() {
        trueButton = new JRadioButton(i18n.get(TrueFalsePropertyEditor.class, "True", "True"));
        falseButton = new JRadioButton(i18n.get(TrueFalsePropertyEditor.class, "False", "False"));
    }

    @Override
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
    @Override
    public Component getCustomEditor() {
        JPanel panel = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
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
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
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

    public void focusLost(FocusEvent e) {
        firePropertyChange();
    }

    /** Sets String in JTextField. */
    @Override
    public void setValue(Object string) {
        if (!(string instanceof String)) {
            return;
        }

        setSelected(((String) string).equalsIgnoreCase(trueButton.getActionCommand()));
    }

    /** Returns String from ButtonGroup. */
    @Override
    public String getAsText() {
        if (option) {
            return trueButton.getActionCommand();
        }
        return falseButton.getActionCommand();
    }
}