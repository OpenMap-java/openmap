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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/OnOffPropertyEditor.java,v $
// $RCSfile: OnOffPropertyEditor.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import javax.swing.JRadioButton;

/**
 * A PropertyEditor that displays a On/Off option. The widget returns
 * true or false as a string when queried.
 */
public class OnOffPropertyEditor extends TrueFalsePropertyEditor {

    public OnOffPropertyEditor() {
        trueButton = new JRadioButton("On");
        falseButton = new JRadioButton("Off");
    }
}