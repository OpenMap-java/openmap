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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JGraphicList.java,v $
// $RCSfile: JGraphicList.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.CSpecialist.CColorPackage.EColor;
import com.bbn.openmap.CSpecialist.EComp;
import com.bbn.openmap.CSpecialist.GraphicPackage.*;
import com.bbn.openmap.CSpecialist.UGraphic;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import java.awt.Color;
import java.io.Serializable;

/** class JGraphic */
public class JGraphicList extends OMGraphicList {

    /**
     * Construct an OMGraphicList.
     */
    public JGraphicList() {
	super();
    };
    
    /**
     * Construct an OMGraphicList with an initial capacity. 
     * @param initialCapacity the initial capacity of the list 
     */
    public JGraphicList(int initialCapacity) {
	super (initialCapacity, 0);
    };

    /**
     * Construct an OMGraphicList with an initial capacity and
     * a standard increment value.
     * @param initialCapacity the initial capacity of the list 
     * @param capacityIncrement the capacityIncrement for resizing 
     */
    public JGraphicList(int initialCapacity, int capacityIncrement) {
        super (initialCapacity, capacityIncrement);
    };

    public OMGraphic getOMGraphicWithId(String gID) {
	java.util.Iterator targets = iterator();
	while (targets.hasNext()) {
	    OMGraphic graphic = (OMGraphic)targets.next();
	    if (graphic instanceof JObjectHolder) {
		com.bbn.openmap.CSpecialist.EComp ecomp = 
		    ((JObjectHolder)graphic).getObject();
		if (ecomp.cID.equals(gID)) {
		    return graphic;
		}
	    }
	}
	return null;
    }

}
