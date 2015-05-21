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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/shape/ESRISpecialistPointRecord.java,v $
// $RCSfile: ESRISpecialistPointRecord.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.shape;

import java.io.IOException;
import java.util.Vector;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.layer.shape.ESRIPointRecord;
import com.bbn.openmap.layer.specialist.SColor;
import com.bbn.openmap.layer.specialist.SRect;

/**
 * An ESRI Point record for specialists.
 */
public class ESRISpecialistPointRecord extends ESRIPointRecord implements
        ESRISpecialistRecord {

    /**
     * Initializes this point from the given point.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public ESRISpecialistPointRecord(double x, double y) {
        super(x, y);
    }

    /**
     * Initialize a point record from the given buffer.
     * 
     * @param b the buffer
     * @param off the offset into the buffer where the data starts
     */
    public ESRISpecialistPointRecord(byte b[], int off) throws IOException {
        super(b, off);
    }

    /**
     * Generates OMGraphics and adds them to the given list.
     * 
     * @param list the Vector to write the graphic into.
     * @param lineColor the line color to use.
     * @param fillColor the fill color to use.
     */
    public void writeGraphics(Vector list, SColor lineColor, SColor fillColor)
            throws IOException {
        SRect sr = new SRect(new LLPoint((float) y, (float) x), (short) -1, (short) -1, (short) 1, (short) 1);

        sr.color(lineColor);
        sr.fillColor(fillColor);
        list.addElement(sr);
    }
}