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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SRadioBox.java,v $
// $RCSfile: SRadioBox.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist._RadioBoxStub;

/**
 * Class SRadioBox is a specialist palette widget. It is a box of
 * buttons, of which only one can be pressed at a time. Selecting one
 * deselects any other.
 * 
 * <pre>
 * 
 *   Parameters:
 *      label - name of the slider, as it appears on the palette.
 *      buttons - the names and identifier of the buttons.
 *      currently_selected_button - button marked as pressed.
 *  
 * </pre>
 * 
 * The widget() function should be used to get the object needed for
 * the addPalette() function, which adds the palette widget to the
 * palette widget list.
 */
public class SRadioBox extends _RadioBoxStub {

    protected String label_;
    protected String[] buttons_;
    protected String currently_selected_button_;

    public SRadioBox() {}

    public SRadioBox(String label, String[] buttons,
            String default_selected_button) {
        label_ = label;
        buttons_ = buttons;
        currently_selected_button_ = default_selected_button;
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

    public void selected_button(String button) {
        currently_selected_button_ = button;
    }

    public String selected_button() {
        return currently_selected_button_;
    }

    public void selected(java.lang.String box_label,
                         java.lang.String selected_button,
                         java.lang.String uniqueID) {
        //      System.out.println("RadioBox: New Radio button selected:");
        //      System.out.println(" in box: " + box_label);
        //      System.out.println(" unique ID: " + uniqueID);
        currently_selected_button_ = selected_button;
    }

    public UWidget widget() {
        UWidget uw = new UWidget();
        uw.rb(this);
        return uw;
    }
}

