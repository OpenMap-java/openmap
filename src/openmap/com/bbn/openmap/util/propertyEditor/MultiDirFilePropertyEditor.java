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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/MultiDirFilePropertyEditor.java,v $
// $RCSfile: MultiDirFilePropertyEditor.java,v $
// $Revision: 1.1 $
// $Date: 2004/03/23 18:51:55 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;

/** 
 * A PropertyEditor that brings up a JFileChooser panel to several
 * files and directories. You can enter information in the text field,
 * and pressing the add button will bring up a file chooser. Anything
 * chosen in the file chooser will be appended to what is currently in
 * the text field.
 */
public class MultiDirFilePropertyEditor extends MultiDirectoryPropertyEditor {
    
    /** Create MultiDirFilePropertyEditor.  */
    public MultiDirFilePropertyEditor() {
        super();
    }

    /**
     * Returns a JFileChooser that will choose a directory.  The
     * MultiSelectionEnabled doesn't work yet, so we have to have a workaround.
     * @return JFileChooser 
     */
    public JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        return chooser;
    }

}
