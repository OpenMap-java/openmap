// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriGraphic.java,v $
// $RCSfile: EsriGraphic.java,v $
// $Revision: 1.3 $
// $Date: 2004/02/09 13:33:36 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.shape;

/**
 * An interface to typecast OMGraphics.
 * @author Doug Van Auken
 */
public interface EsriGraphic {

    /**
     * The lat/lon extent of the EsriGraphic, assumed to contain miny,
     * minx, maxy maxx in order of the array.  
     */
    public void setExtents(float[] extents);
    /**
     * The lat/lon extent of the EsriGraphic, returned as miny, minx,
     * maxy maxx in order of the array.  
     */
    public float[] getExtents();

    /**
     * Shallow copy this graphic.  The contract is that the cloned
     * object should be capable of being placed on a different layer.
     */
    public EsriGraphic shallowCopy();
}
