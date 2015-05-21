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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/shape/ESRILinkPointRecord.java,v $
// $RCSfile: ESRILinkPointRecord.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:58 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.shape;

import java.io.IOException;

import com.bbn.openmap.layer.link.LinkGraphicList;
import com.bbn.openmap.layer.link.LinkProperties;
import com.bbn.openmap.layer.shape.ESRIPointRecord;

/**
 * An ESRI Point record.
 * 
 * @author Ray Tomlinson
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.4 $ $Date: 2004/10/14 18:05:58 $
 */
public class ESRILinkPointRecord extends ESRIPointRecord implements
        ESRILinkRecord {

    /**
     * Initializes this point from the given point.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public ESRILinkPointRecord(double x, double y) {
        super(x, y);
    }

    /**
     * Initialize a point record from the given buffer.
     * 
     * @param b the buffer
     * @param off the offset into the buffer where the data starts
     */
    public ESRILinkPointRecord(byte b[], int off) throws IOException {
        super(b, off);
    }

    /**
     * Generates OMGraphics and adds them to the given list.
     * 
     * @param lgl the graphics response to write the point to.
     * @param properties the semantic description of how the point
     *        should be drawn.
     */
    public void writeLinkGraphics(LinkGraphicList lgl, LinkProperties properties)
            throws IOException {
        lgl.addRectangle((float) y, (float) x, -1, -1, 1, 1, properties);
    }
}