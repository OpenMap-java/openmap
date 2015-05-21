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
// $Source:
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/MakeProjection.java,v
// $
// $RCSfile: MakeProjection.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:08:58 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.CProjection;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Orthographic;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class MakeProjection {
    Projection p;

    public final static short MercatorType = 0;
    public final static short CADRGType = 1;
    public final static short OrthographicType = 2;

    public MakeProjection(CProjection mp) {
        p = getProjection(mp);
    }

    public Projection getProj() {
        return p;
    }

    public static Projection getProjection(CProjection mp) {
        Projection p;
        LatLonPoint center = new LatLonPoint.Double(mp.center.lat, mp.center.lon);

        switch (mp.kind) {
        case CADRGType:
            p = new CADRG(center, mp.scale, mp.width, mp.height);
            break;
        case OrthographicType:
            p = new Orthographic(center, mp.scale, mp.width, mp.height);
            break;
        default:
            p = new Mercator(center, mp.scale, mp.width, mp.height);
        }
        return p;
    }
    
    public static short getProjectionType(Projection mp) {
        if (mp instanceof CADRG) {
            return CADRGType;
        } else if (mp instanceof Orthographic) {
            return OrthographicType;
        }
        return MercatorType;
    }
}