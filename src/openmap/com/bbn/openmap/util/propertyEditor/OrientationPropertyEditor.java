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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/OrientationPropertyEditor.java,v $
// $RCSfile: OrientationPropertyEditor.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import javax.swing.JRadioButton;

public class OrientationPropertyEditor extends TrueFalsePropertyEditor {

    public final static String VERTICAL = "vertical";
    public final static String HORIZONTAL = "horizontal";

    public OrientationPropertyEditor() {
        setUseAltCommandStrings(true);
        trueButton = new JRadioButton("Vertical");
        trueButton.setActionCommand(VERTICAL);
        falseButton = new JRadioButton("Horizontal");
        falseButton.setActionCommand(HORIZONTAL);
    }
}
