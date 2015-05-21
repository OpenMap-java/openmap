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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/STextBox.java,v $
// $RCSfile: STextBox.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist._TextBoxStub;

/**
 * Class STextBox is a specialist palette widget. It is a window that
 * has a text area and a button to confirm the text entry to the
 * specialist.
 */
public class STextBox extends _TextBoxStub {

    /** Name of the text window, as it appears on the palette. */
    protected String label_;
    /** Contents of the text window. */
    protected String text_;

    public STextBox() {}

    public STextBox(String label, String text) {
        label_ = label;
        text_ = text;
    }

    public void label(java.lang.String label) {
        label_ = label;
    }

    public java.lang.String label() {
        return label_;
    }

    public void contents(String text) {
        text_ = text;
    }

    public String contents() {
        return text_;
    }

    public void pressed(java.lang.String box_label, java.lang.String text,
                        java.lang.String uniqueID) {
        //      System.out.println("TextBox:");
        //      System.out.println(" in box: " + box_label);
        //      System.out.println(" unique ID: " + uniqueID);
        //      System.out.println(" New contents of text box: " + text);
        text_ = text;
    }

    /**
     * The <b>widget </b> function should be used to get the object
     * needed for the <b>addPalette </b> specialist function, which
     * adds the palette widget to the palette widget list.
     */
    public UWidget widget() {
        UWidget uw = new UWidget();
        uw.tb(this);
        return uw;
    }
}

