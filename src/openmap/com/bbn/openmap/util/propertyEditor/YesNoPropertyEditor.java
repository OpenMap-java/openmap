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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/YesNoPropertyEditor.java,v $
// $RCSfile: YesNoPropertyEditor.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import java.beans.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.event.*;

/** 
 * A PropertyEditor that displays a Yes/No option.  The widget
 * returns true or false as a string when queried.
 */
public class YesNoPropertyEditor extends TrueFalsePropertyEditor {

    public YesNoPropertyEditor() {
	trueButton = new JRadioButton("Yes");
	falseButton = new JRadioButton("No");
    }
}
