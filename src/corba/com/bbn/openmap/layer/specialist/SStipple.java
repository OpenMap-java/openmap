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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SStipple.java,v $
// $RCSfile: SStipple.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist._CStippleStub;
import com.bbn.openmap.corba.CSpecialist.CStipplePackage.EStipple;

/**
 * SStipple is a parameter for specialist graphics objects. It is a
 * bitmap that represents a pattern that can be used for a line or the
 * inside of an object.
 */
public class SStipple extends _CStippleStub {

    /** Height of stipple. */
    protected short height_;
    /** Width of stipple. */
    protected short width_;
    /** Data of stipple. */
    protected byte[] data_;

    // Need several constructors
    public SStipple() {
        width_ = 0;
        height_ = 0;
        data_ = new byte[0];
    }

    public SStipple(short w, short h, byte[] data) {
        width_ = w;
        height_ = h;
        data_ = data;
    }

    // The SStipple methods
    public void height(short height) {
        height_ = height;
    }

    public short height() {
        return height_;
    }

    public void width(short width) {
        width_ = width;
    }

    public short width() {
        return width_;
    }

    public void data(byte[] data) {
        data_ = data;
    }

    public byte[] data() {
        return data_;
    }

    public EStipple fill() {
        return new EStipple(this, height_, width_, data_);
    }
}