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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/FDUPropertyEditor.java,v $
// $RCSfile: FDUPropertyEditor.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;

/**
 * FDUPropertyEditor - File, Directory and URL PropertyEditor. This is
 * a PropertyEditor that provides a text field where a URL, file path
 * or directory path can be entered. There is also a button that
 * brings up a file chooser, and anything chosen *replaces* the
 * contents in the text field.
 */
public class FDUPropertyEditor extends MultiDirectoryPropertyEditor {

    /** Create MultiDirectoryPropertyEditor. */
    public FDUPropertyEditor() {
        button = new JButton("Set");
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getFileChooser();
        int returnVal = chooser.showOpenDialog((Component) null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String newFilename = chooser.getSelectedFile().getAbsolutePath();
            newFilename = cleanUpName(newFilename);
            setValue(newFilename);
            firePropertyChange();
        }
    }

    /**
     * Returns a JFileChooser that will choose a directory. The
     * MultiSelectionEnabled doesn't work yet, so we have to have a
     * workaround.
     * 
     * @return JFileChooser
     */
    public JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser(getLastLocation());
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        return chooser;
    }
}