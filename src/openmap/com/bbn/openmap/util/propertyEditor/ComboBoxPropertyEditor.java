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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/ComboBoxPropertyEditor.java,v $
// $RCSfile: ComboBoxPropertyEditor.java,v $
// $Revision: 1.1 $
// $Date: 2004/05/25 02:29:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import com.bbn.openmap.util.Debug;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;

/**
 * The ComboBoxPropertyEditor provides a set of options within a JComboBox.
 * @see OptionPropertyEditor
 */
public class ComboBoxPropertyEditor extends OptionPropertyEditor {

    protected JComboBox combo;

    public ComboBoxPropertyEditor() {}

    public void setOptions(String[] options) {
        combo = new JComboBox(options);
        setCustomEditor(combo);
    }

    /** Sets option based on string. */
    public void setValue(Object string) {
        if (combo != null) {
            combo.setSelectedItem(string);
        } else {
            Debug.output("Setting " + string + " before ComboBoxPropertyEditor is ready");
        }
    }

    /** Returns String from option choices. */
    public String getAsText() {
        if (combo != null) {
            return (String)combo.getSelectedItem();
        } else {
            return "";
        }
    }

}