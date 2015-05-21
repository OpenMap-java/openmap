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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SCheckBox.java,v $
// $RCSfile: SCheckBox.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.CheckButton;
import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist._CheckBoxStub;

/**
 * Class SCheckBox is a specialist palette widget. It is a box of
 * buttons where none, one or all can be marked as active.
 */
public class SCheckBox extends _CheckBoxStub {
    /** Name of the slider, as it appears on the palette. */
    protected String label_;
    /** The name and identifier of each button. */
    protected CheckButton[] buttons_;

    public SCheckBox() {}

    public SCheckBox(String label, CheckButton[] buttons) {
        label_ = label;
        buttons_ = buttons;
    }

    public void label(java.lang.String label) {
        label_ = label;
    }

    public java.lang.String label() {
        return label_;
    }

    public void buttons(com.bbn.openmap.corba.CSpecialist.CheckButton[] buttons) {
        buttons_ = buttons;
    }

    public com.bbn.openmap.corba.CSpecialist.CheckButton[] buttons() {
        return buttons_;
    }

    public void selected(java.lang.String box_label,
                         com.bbn.openmap.corba.CSpecialist.CheckButton button,
                         java.lang.String uniqueID) {
    //      System.out.println("CheckBox: New button selected.");
    //      System.out.println(" in box: " + box_label);
    //      System.out.println(" unique ID: " + uniqueID);
    //      System.out.println(" button: " + button.button_label);
    }

    /**
     * The <b>widget </b> function should be used to get the object
     * needed for the <b>addPalette </b> specialist function, which
     * adds the palette widget to the palette widget list.
     */
    public UWidget widget() {
        UWidget uw = new UWidget();
        uw.cb(this);
        return uw;
    }
}

