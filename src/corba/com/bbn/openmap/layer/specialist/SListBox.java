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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SListBox.java,v $
// $RCSfile: SListBox.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist._ListBoxStub;

/**
 * The SListBox class is a specialist palette widget, used to present
 * a scrollable list of strings, with a selectable value from the
 * list.
 */
public class SListBox extends _ListBoxStub {

    /** Name of the list as it appears in the palette. */
    protected String label_;
    /** Arrays of Strings, each string on a line of the list */
    protected String[] contents_;
    /** Current choice on the list. */
    protected String currently_selected_item_;

    public SListBox() {}

    public SListBox(String label, String[] contents, String selected_item) {
        label_ = label;
        contents_ = contents;
        currently_selected_item_ = selected_item;
    }

    public void label(java.lang.String label) {
        label_ = label;
    }

    public java.lang.String label() {
        return label_;
    }

    public void highlighted_item(String item) {
        currently_selected_item_ = item;
    }

    public String highlighted_item() {
        return currently_selected_item_;
    }

    public void contents(String[] contents) {
        contents_ = contents;
    }

    public String[] contents() {
        return contents_;
    }

    public void selected(java.lang.String box_label,
                         java.lang.String selected_item,
                         java.lang.String uniqueID) {
        //      System.out.println("ListBox:");
        //      System.out.println(" in box: " + box_label);
        //      System.out.println(" unique ID: " + uniqueID);
        //      System.out.println(" Selected item: " + selected_item);
        currently_selected_item_ = selected_item;
    }

    /**
     * The <b>widget </b> function should be used to get the object
     * needed for the <b>addPalette </b> specialist function, which
     * adds the palette widget to the palette widget list.
     */
    public UWidget widget() {
        UWidget uw = new UWidget();
        uw.lb(this);
        return uw;
    }
}

