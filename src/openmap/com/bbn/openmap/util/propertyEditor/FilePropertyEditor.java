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
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JButton;
import javax.swing.JFileChooser;

/**
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * file. A single file choice can be made, and only choices that
 * reside on the local file system.
 */
public class FilePropertyEditor extends PropertyEditorSupport implements
        ActionListener {

    /** The Component returned by getCustomEditor(). */
    protected JButton button;

    /** Create FilePropertyEditor. */
    public FilePropertyEditor() {
        button = new JButton("Select file...");
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
            button.setText(newFilename);
            firePropertyChange();
        }
    }

    /**
     * Change double backslashes to forward slash, OK for java world.
     */
    protected String cleanUpName(String name) {
        // replace all back slashes with forward slashes to permit
        // safe writing and reading from PrintStreams
        return name.replace('\\', '/');
    }

    /**
     * Returns a JButton that will bring up a JFileChooser dialog.
     * 
     * @return JButton button
     */
    public Component getCustomEditor() {
        button.addActionListener(this);
        return button;
    }

    public JFileChooser getFileChooser() {
        return new JFileChooser(getLastLocation());
    }

    /** Implement PropertyEditor interface. */
    public void setValue(Object someObj) {
        if (someObj instanceof String) {
            button.setText((String) someObj);
        }
    }

    /** Implement PropertyEditor interface. */
    public String getAsText() {
        return button.getText();
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