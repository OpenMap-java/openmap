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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/vpf/LineComp.java,v $
// $RCSfile: LineComp.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.vpf;

import com.bbn.openmap.corba.CSpecialist.ActionUnion;
import com.bbn.openmap.corba.CSpecialist.MouseEvent;
import com.bbn.openmap.layer.specialist.SComp;

/**
 * Extent the SComp class to allow us to remember a string we get
 * constructed with, and kick that back for any gesture we get.
 * 
 * @see com.bbn.openmap.layer.specialist.SComp
 */
public class LineComp extends SComp {
    /** The string we kick back on a gesture */
    private String response;

    /**
     * Construct a LineComp and give it a return string
     * 
     * @param value the respone we kick back on a gesture
     */
    LineComp(String value) {
        response = (value == null) ? "Graticule Line" : value;
    }

    /**
     * Our gesture override: any gesture causes us to kick back the
     * info string
     */
    public ActionUnion[] sendGesture(MouseEvent gesture, String uniqueID) {
        ActionUnion ret[] = new ActionUnion[1];
        ret[0] = new ActionUnion();
        ret[0].itext(response);

        return ret;
    }
}