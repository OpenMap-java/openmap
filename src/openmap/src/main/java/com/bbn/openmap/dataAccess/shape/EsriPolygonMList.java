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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolygonMList.java,v $
// $RCSfile: EsriPolygonMList.java,v $
// $Revision: 1.2 $
// $Date: 2006/08/25 15:36:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.util.Iterator;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;

/**
 * An EsriGraphicList ensures that only EsriPolygonMs are added to its list.
 * 
 * @author Doug Van Auken
 * @author Don Dietrick
 */
public class EsriPolygonMList extends EsriPolygonList {

    /**
     * Construct an EsriPolygonList.
     */
    public EsriPolygonMList() {
        super();
    }

    /**
     * Construct an EsriPolygonList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public EsriPolygonMList(int initialCapacity) {
        super(initialCapacity);
    }

    public EsriPolygon convert(OMPoly ompoly) {
        return EsriPolygonM.convert(ompoly);
    }

    /**
     * Get the list type in ESRI type number form - 25.
     */
    public int getType() {
        return SHAPE_TYPE_POLYGONM;
    }

    public EsriGraphic shallowCopy() {
        EsriPolygonMList ret = new EsriPolygonMList(size());
        for (Iterator iter = iterator(); iter.hasNext();) {
            EsriGraphic g = (EsriGraphic) iter.next();
            ret.add((OMGraphic) g.shallowCopy());
        }
        ret.setAppObject(getAppObject());
        return ret;
    }
}
