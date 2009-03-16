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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SSlider.java,v $
// $RCSfile: SSlider.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist._SliderStub;

/**
 * Class SSlider is a specialist palette widget. It is a slide bar
 * with a marker that can be used to adjust the relative value of the
 * widget.
 */
public class SSlider extends _SliderStub {

    /** Name of the slider, as it appears on the palette. */
    protected String label_;
    /** The low range value on the left side of the slider. */
    protected short start_;
    /** The high range value on the right side of the slider. */
    protected short end_;
    /** The setting of the marker. */
    protected short current_value_;
    /** Orientation setting(up/down vs left/right). */
    protected boolean vertical_;

    public SSlider() {}

    public SSlider(String label, short start, short end, short initial_value,
            boolean vertical) {
        label_ = label;
        start_ = start;
        end_ = end;
        current_value_ = initial_value;
        vertical_ = vertical;
    }

    public void label(java.lang.String label) {
        label_ = label;
    }

    public java.lang.String label() {
        return label_;
    }

    public void start(short start) {
        start_ = start;
    }

    public short start() {
        return start_;
    }

    public void end(short end) {
        end_ = end;
    }

    public short end() {
        return end_;
    }

    public void value(short value) {
        current_value_ = value;
    }

    public short value() {
        return current_value_;
    }

    public void vertical(boolean vertical) {
        vertical_ = vertical;
    }

    public boolean vertical() {
        return vertical_;
    }

    public void set(java.lang.String label, short new_value,
                    java.lang.String uniqueID) {
        //      System.out.println("Slider:");
        //      System.out.println(" in: " + label);
        //      System.out.println(" unique ID: " + uniqueID);
        //      System.out.println(" New value of slider: " + new_value);

        current_value_ = new_value;
    }

    /**
     * The <b>widget </b> function should be used to get the object
     * needed for the <b>addPalette </b> specialist function, which
     * adds the palette widget to the palette widget list.
     */
    public UWidget widget() {
        UWidget uw = new UWidget();
        uw.slide(this);
        return uw;
    }
}

