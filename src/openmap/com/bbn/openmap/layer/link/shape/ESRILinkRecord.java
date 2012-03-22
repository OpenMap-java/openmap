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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/shape/ESRILinkRecord.java,v $
// $RCSfile: ESRILinkRecord.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:58 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.shape;

import java.io.IOException;

import com.bbn.openmap.layer.link.LinkGraphicList;
import com.bbn.openmap.layer.link.LinkProperties;

/**
 */
public interface ESRILinkRecord {

    /**
     * Writes the record to the given list.
     * <p>
     * Copy the poly points array because the OMPoly converts from
     * degrees to radians in place, trashing the shape.
     * 
     * @param lgl the graphics response of the link to write the
     *        object to.
     * @param properties the properties of the polys
     */
    public void writeLinkGraphics(LinkGraphicList lgl, LinkProperties properties)
            throws IOException;
}