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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/FilePropertyEditor.java,v $
// $RCSfile: FilePropertyEditor.java,v $
// $Revision: 1.7 $
// $Date: 2005/05/24 17:55:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * file. A single file choice can be made, and only choices that
 * reside on the local file system.
 */
public class FilePropertyEditor extends PropertyEditorSupport implements
        ActionListener {

    /** The Component returned by getCustomEditor(). */
    protected JButton button;
    protected JTextField textField = new JTextField(15);

    /** Create FilePropertyEditor. */
    public FilePropertyEditor() {
        button = new JButton(getButtonTitle());
        textField.setEditable(isTextFieldEditable());
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return "Set" for FilePropertyEditor.
     */
    public String getButtonTitle() {
        return "Set";
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return false for FilePropertyEditor.
     */
    public boolean isTextFieldEditable() {
        return false;
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return JFileChooser.FILES_ONLY for FilePropertyEditor.
     */
    public int getFileSelectionMode() {
        return JFileChooser.FILES_ONLY;
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return false for FilePropertyEditor.
     */
    public boolean isMultiSelectEnabled() {
        return false;
    }

    //
    //  PropertyEditor interface
    //

    /**
     * PropertyEditor interface.
     * 
     * @return true
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getFileChooser();
        int returnVal = chooser.showOpenDialog((Component) null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String newFilename = chooser.getSelectedFile().getAbsolutePath();
            newFilename = cleanUpName(newFilename);
            textField.setText(newFilename);
            firePropertyChange();
        }
    }

    /**
     * Change double backslashes to forward slash, OK for java world.
     */
    protected String cleanUpName(String name) {
        // replace all back slashes with forward slashes to permit
        // safe writing and reading from PrintStreams
        return name.replace('\\', '/').trim();
    }

    /**
     * Returns an uneditable text area with a JButton that will bring
     * up a JFileChooser dialog.
     * 
     * @return JButton button
     */
    public Component getCustomEditor() {
        button.addActionListener(this);

        JPanel jp = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        jp.setLayout(gridbag);

        c.weightx = 1f;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(textField, c);
        jp.add(textField);

        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(button, c);
        jp.add(button);
        return jp;
    }

    public JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser(getLastLocation());
        chooser.setFileSelectionMode(getFileSelectionMode());
        chooser.setMultiSelectionEnabled(isMultiSelectEnabled());
        return chooser;
    }

    /** Implement PropertyEditor interface. */
    public void setValue(Object someObj) {
        if (someObj instanceof String) {
            textField.setText((String) someObj);
        }
    }

    /** Implement PropertyEditor interface. */
    public String getAsText() {
        return textField.getText();
    }

    public String getLastLocation() {
        String currentLocation = getAsText();
        char sepChar = '/'; // Java path separator
        int lastSepIndex = currentLocation.lastIndexOf(sepChar);
        //      System.out.println(currentLocation + ", index of " +
        // sepChar + " is at " + lastSepIndex);
        if (currentLocation.equals("") || lastSepIndex == -1) {
            currentLocation = null;
        } else {
            String substring = currentLocation.substring(0, lastSepIndex);
            //          System.out.println(substring);
            currentLocation = substring;
        }
        return currentLocation;
    }
}