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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/OnOffPropertyEditor.java,v $
// $RCSfile: OnOffPropertyEditor.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import java.beans.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.event.*;

/** 
 * A PropertyEditor that displays a On/Off option.  The widget
 * returns true or false as a string when queried.
 */
public class OnOffPropertyEditor extends TrueFalsePropertyEditor {

    public OnOffPropertyEditor() {
        trueButton = new JRadioButton("On");
        falseButton = new JRadioButton("Off");
    }
}
