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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolylineZList.java,v $
// $RCSfile: EsriPolylineZList.java,v $
// $Revision: 1.2 $
// $Date: 2006/08/25 15:36:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.util.Iterator;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;

/**
 * An EsriGraphicList ensures that only EsriPolygons are added to its list.
 * 
 * @author Doug Van Auken
 * @author Don Dietrick
 */
public class EsriPolylineZList extends EsriPolylineList {

    /**
     * Construct an EsriPolylineList.
     */
    public EsriPolylineZList() {
        super();
    }

    /**
     * Construct an EsriPolylineList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public EsriPolylineZList(int initialCapacity) {
        super(initialCapacity);
    }

    public EsriPolyline convert(OMPoly ompoly) {
        return EsriPolylineZ.convert(ompoly);
    }

    /**
     * Get the list type in ESRI type number form - 13.
     */
    public int getType() {
        return SHAPE_TYPE_POLYLINEZ;
    }

    public EsriGraphic shallowCopy() {
        EsriPolylineZList ret = new EsriPolylineZList(size());
        ret.setAttributes(getAttributes());
        for (Iterator iter = iterator(); iter.hasNext();) {
            EsriGraphic g = (EsriGraphic) iter.next();
            ret.add((OMGraphic) g.shallowCopy());
        }
        return ret;
    }
}
