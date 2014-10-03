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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/OrientationPropertyEditor.java,v
// $
// $RCSfile: OrientationPropertyEditor.java,v $
// $Revision: 1.1 $
// $Date: 2006-08-31 15:56:07 $
// $Author: jourquin $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import javax.swing.JRadioButton;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;

public class OrientationPropertyEditor extends TrueFalsePropertyEditor {

    //  I18N mechanism
    static I18n i18n = Environment.getI18n();
    
    public final static String VERTICAL = "vertical";
    public final static String HORIZONTAL = "horizontal";

    public OrientationPropertyEditor() {
        setUseAltCommandStrings(true);
        trueButton = new JRadioButton(i18n.get(OrientationPropertyEditor.class, "Vertical", "Vertical"));
        trueButton.setActionCommand(VERTICAL);
        falseButton = new JRadioButton(i18n.get(OrientationPropertyEditor.class, "Horizontal", "Horizontal"));
        falseButton.setActionCommand(HORIZONTAL);
    }
}