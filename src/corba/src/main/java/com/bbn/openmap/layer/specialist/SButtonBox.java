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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SButtonBox.java,v $
// $RCSfile: SButtonBox.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist._ButtonBoxStub;

/**
 * Class SButtonBox is a specialist palette widget. It is a box that
 * contains push buttons. Any button in the box triggers the
 * <b>pressed </b> function when it is pressed. The name of the button
 * can be used to determine which button was pressed, and the label of
 * the box can be used to tell which box the pressed button is from.
 */
public class SButtonBox extends _ButtonBoxStub {
    /** Name of the box, as it appears on the palette. */
    protected String label_;
    /** The names on the buttons, which also identify them. */
    protected String[] buttons_;

    public SButtonBox() {}

    public SButtonBox(String label, String[] buttons) {
        label_ = label;
        buttons_ = buttons;
    }

    public void label(java.lang.String label) {
        label_ = label;
    }

    public java.lang.String label() {
        return label_;
    }

    public void buttons(String[] buttons) {
        buttons_ = buttons;
    }

    public String[] buttons() {
        return buttons_;
    }

    public void pressed(java.lang.String box_label,
                        java.lang.String selected_button,
                        java.lang.String uniqueID) {
    //      System.out.println("ButtonBox: Button pressed:");
    //      System.out.println(" in box: " + box_label);
    //      System.out.println(" unique ID: " + uniqueID);
    //      System.out.println(" Button Name: " + selected_button);
    }

    /**
     * The <b>widget </b> function should be used to get the object
     * needed for the <b>addPalette </b> specialist function, which
     * adds the palette widget to the palette widget list.
     */
    public UWidget widget() {
        UWidget uw = new UWidget();
        uw.bb(this);
        return uw;
    }
}

