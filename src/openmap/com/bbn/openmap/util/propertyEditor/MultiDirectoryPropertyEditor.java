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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/MultiDirectoryPropertyEditor.java,v $
// $RCSfile: MultiDirectoryPropertyEditor.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A PropertyEditor that brings up a JFileChooser panel to several
 * files and directories. You can enter information in the text field,
 * and pressing the add button will bring up a file chooser. Anything
 * chosen in the file chooser will be appended to what is currently in
 * the text field.
 */
public class MultiDirectoryPropertyEditor extends FilePropertyEditor {

    /** The GUI component of this editor. */
    protected JTextField textField = new JTextField(15);
    protected char pathSeparator;

    /** Create MultiDirectoryPropertyEditor. */
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

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getFileChooser();
        int returnVal = chooser.showOpenDialog((Component) null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String newFilename = chooser.getSelectedFile().getAbsolutePath();
            newFilename = cleanUpName(newFilename);
            append(newFilename);
            firePropertyChange();
        }
    }

    /**
     * Returns a JButton that will bring up a JFileChooser dialog.
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

    /**
     * Returns a JFileChooser that will choose a directory. The
     * MultiSelectionEnabled doesn't work yet, so we have to have a
     * workaround.
     * 
     * @return JFileChooser
     */
    public JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        return chooser;
    }

    /**
     * Add a path to the end of the current path. Uses the
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
        if (!(string instanceof String))
            return;
        textField.setText((String) string);
    }

    /** Returns String from JTextfield. */
    public String getAsText() {
        return textField.getText();
    }
}