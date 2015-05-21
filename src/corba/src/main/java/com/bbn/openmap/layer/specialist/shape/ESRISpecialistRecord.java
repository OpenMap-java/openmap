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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/shape/ESRISpecialistRecord.java,v $
// $RCSfile: ESRISpecialistRecord.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.shape;

import java.io.IOException;
import java.util.Vector;

import com.bbn.openmap.layer.specialist.SColor;

/**
 */
public interface ESRISpecialistRecord {

    /**
     * Writes the record to the given list.
     * <p>
     * Copy the poly points array because the OMPoly converts from
     * degrees to radians in place, trashing the shape.
     * 
     * @param list the Vector to write the graphic into.
     * @param lineColor the line color to use.
     * @param fillColor the fill color to use.
     */
    public void writeGraphics(Vector list, SColor lineColor, SColor fillColor)
            throws IOException;
}