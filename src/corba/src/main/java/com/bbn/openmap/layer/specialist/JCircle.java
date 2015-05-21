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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JCircle.java,v $
// $RCSfile: JCircle.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

// import netscape.application.*;
import java.io.Serializable;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.CirclePackage.ECircle;
import com.bbn.openmap.omGraphics.OMCircle;

/** JCircle - circles */
public class JCircle extends OMCircle implements Serializable, JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    /** Constructor. */
    public JCircle(ECircle ecircle) {
        super();
        JGraphic.fillOMGraphicParams(this, ecircle.egraphic);

        setX(ecircle.p1.x);
        setY(ecircle.p1.y);
        setLatLon(ecircle.ll1.lat, ecircle.ll1.lon);

        // HACK - Due to a problem in the projection libs, LatLon
        // ellipses aren't supported right now. So for now, we're
        // picking the major value and making a circle
        setRadius(ecircle.major);
        //      setMajor(ecircle.major);
        //      setMinor(ecircle.minor);

        setWidth(ecircle.width);
        setHeight(ecircle.height);
    }

    public void setObject(com.bbn.openmap.corba.CSpecialist.EComp aObject) {
        object = aObject;
    }

    public com.bbn.openmap.corba.CSpecialist.EComp getObject() {
        return object;
    }

    public void update(
                       com.bbn.openmap.corba.CSpecialist.GraphicPackage.GF_update update) {
        JGraphic.update((JObjectHolder) this, update);
    }

    /**
     * update() - takes a CircPackage.CF_update and changes the fields
     * that need to be. Called as a result of a gesture.
     */
    public void update(
                       com.bbn.openmap.corba.CSpecialist.CirclePackage.CF_update update) {

        needToRegenerate = true; // flag dirty
        // do the updates, but don't rerender just yet
        switch (update.discriminator().value()) {

        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.CirclePackage.settableFields._CF_ll1:
            LLPoint ll = update.ll1();
            setLatLon(ll.lat, ll.lon);
            break;

        case com.bbn.openmap.corba.CSpecialist.CirclePackage.settableFields._CF_p1:
            XYPoint pt1 = update.p1();
            setX(pt1.x);
            setY(pt1.y);
            break;

        case com.bbn.openmap.corba.CSpecialist.CirclePackage.settableFields._CF_major:
            // HACK - Projection lib doesn't handle LatLon Ellipses
            // -make it a circle
            setRadius(update.major());
            //            setMajor(update.major());
            break;

        case com.bbn.openmap.corba.CSpecialist.CirclePackage.settableFields._CF_minor:
            // HACK - Projection lib doesn't handle LatLon Ellipses
            // -make it a circle
            setRadius(update.minor());
            //            setMinor(update.minor());
            break;

        case com.bbn.openmap.corba.CSpecialist.CirclePackage.settableFields._CF_height:
            setHeight(update.height());
            break;

        case com.bbn.openmap.corba.CSpecialist.CirclePackage.settableFields._CF_width:
            setWidth(update.width());
            break;

        default:
            System.err.println("JCircle.update: invalid circle update");
            break;
        }
    }
}