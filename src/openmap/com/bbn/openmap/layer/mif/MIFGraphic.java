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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/mif/MIFGraphic.java,v $
// $RCSfile: MIFGraphic.java,v $
// $Revision: 1.2 $
// $Date: 2004/02/09 13:33:37 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.mif;

/**
 * Interface that defines behavior specific to OMGraphics that are
 * specific to primitves created from MapInfo MIF files
 * 
 * @author Simon Bowen
 */
public interface MIFGraphic {
    /**
     * sets the scale at which the graphic becomes visible, if set to -1
     * the graphic is viaible at all scale levels.
     * 
     * @param scale
     */
    public void setVisibleScale(float visibleScale);

    public float getVisibleScale();

}
/** Last line of file **/
