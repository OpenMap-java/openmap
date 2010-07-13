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
// $Source: /home/cvs/nodus/src/com/bbn/openmap/util/propertyEditor/MultiDirectoryPropertyEditor.java,v $
// $RCSfile: MultiDirectoryPropertyEditor.java,v $
// $Revision: 1.2 $
// $Date: 2006-10-25 12:21:51 $
// $Author: jourquin $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;

/**
 * A PropertyEditor that brings up a JFileChooser panel that allows
 * the user to choose one or more directories. The user can also enter
 * information in the text field, and pressing the add button will
 * bring up a file chooser. Anything chosen in the file chooser will
 * be appended to what is currently in the text field.
 */
public class MultiDirectoryPropertyEditor extends FilePropertyEditor {

    protected char pathSeparator;
    
    //  I18N mechanism
    static I18n i18n = Environment.getI18n();

    /** Create MultiDirectoryPropertyEditor. */
    public MultiDirectoryPropertyEditor() {
        setPathSeparator(';');
    }

    @Override
    public String getButtonTitle() {
        return i18n.get(MultiDirectoryPropertyEditor.class, "Add", "Add");
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return true for MultiDirectoryPropertyEditor.
     */
    @Override
    public boolean isTextFieldEditable() {
        return true;
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return JFileChooser.DIRECTORIES_ONLY for MultiDirectoryPropertyEditor.
     */
    @Override
    public int getFileSelectionMode() {
        return JFileChooser.DIRECTORIES_ONLY;
    }

    /**
     * Internal callback method that can be overridden by subclasses.
     * 
     * @return true for MultiDirectoryPropertyEditor.
     */
    @Override
    public boolean isMultiSelectEnabled() {
        return true;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getFileChooser();
        int returnVal = chooser.showOpenDialog((Component) null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File[] choices = chooser.getSelectedFiles();
            for (File element : choices) {
                String newFilename = element.getAbsolutePath();
                newFilename = cleanUpName(newFilename);
                append(newFilename);
            }
            firePropertyChange();
        }
    }

    /**
     * Add a path to the end of the current path. Uses the
     * pathSeparator between paths.
     */
    public void append(String addPath) {
        String currentPath = textField.getText();
        if (currentPath.length() == 0) {
            setValue(addPath);
        } else {
            setValue(currentPath.concat(";" + addPath));
        }
    }
}