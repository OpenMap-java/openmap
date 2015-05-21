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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/BufferedCSpecLayer.java,v $
// $RCSfile: BufferedCSpecLayer.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:08:58 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

/*  CORBA  */
import java.awt.geom.Point2D;

import org.omg.CORBA.StringHolder;

import com.bbn.openmap.corba.CSpecialist.CProjection;
import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.Server;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * BufferedCSpecLayer is a Layer which communicates to CORBA
 * Specialists.
 */
public class BufferedCSpecLayer extends CSpecLayer {

    // private final static String[] debugTokens = { "debug.cspec" };

    // Cached graphics
    UGraphic[] graphics = null;

    /**
     * Default constructor, that sets the MapMouseListener for this
     * layer to itself.
     */
    public BufferedCSpecLayer() {
        super();
    }

    /**
     * perform the getRectangle() call on the specialist.
     * 
     * @param p Projection
     * @return UGraphic[] graphic list or null if error
     * 
     */
    protected UGraphic[] getSpecGraphics(Projection p) {
        CProjection cproj;
        LLPoint ll1, ll2;
        StringHolder dynamicArgsHolder;
        Server spec = getSpecialist();
        if (Debug.debugging("cspec")) {
            System.out.println(getName()
                    + "|BufferedCSpecLayer.getSpecGraphics()");
        }

        // If we have graphics, return them
        if (graphics != null) {
            System.out.println("Returning cached graphics");
            return graphics;
        }

        Point2D center = p.getCenter();
        cproj = new CProjection(MakeProjection.getProjectionType(p), new LLPoint((float) center.getY(), (float) center.getX()), (short) p.getHeight(), (short) p.getWidth(), (int) p.getScale());

        // lat-lon "box", (depends on the projection)
        Point2D ul = p.getUpperLeft();
        Point2D lr = p.getLowerRight();

        // ll1 = new LLPoint(ul.getLatitude(), ul.getLongitude());
        // ll2 = new LLPoint(lr.getLatitude(), lr.getLongitude());
        // Adjust lat/lon for total global area
        ll1 = new LLPoint(90.0f, -180.0f);
        ll2 = new LLPoint(-90.0f, 180.0f);
        // check for cancellation
        if (isCancelled()) {
            dirtybits |= PREMATURE_FINISH;
            if (Debug.debugging("cspec"))
                System.out.println(getName()
                        + "|BufferedCSpecLayer.getSpecGraphics(): aborted.");
            return null;
        }
        // check for null specialist
        if (spec == null) {
            System.err.println(getName()
                    + "|BufferedCSpecLayer.getSpecGraphics(): null specialist!");
            return null;
        }
        try {
            // Keep the gestures up-to-date
            mapGesture.setProjection(p);

            // Static Args can't go out null....
            String staticArguments = getStaticArgs();
            if (staticArguments == null) {
                staticArguments = "";
                setStaticArgs(staticArguments);
            }

            // neither can dynamic args
            dynamicArgsHolder = new StringHolder(getArgs());
            if (dynamicArgsHolder.value == null) {
                dynamicArgsHolder.value = "";
            }

            // call getRectangle();
            if (Debug.debugging("cspec")) {
                System.out.println(getName()
                        + "|BufferedCSpecLayer.getSpecGraphics():"
                        + " calling getRectangle with projection: " + p
                        + " ul=" + ul + " lr=" + lr + " staticArgs=\""
                        + staticArguments + "\"" + " dynamicArgs=\""
                        + dynamicArgsHolder.value + "\"" + " clientID="
                        + clientID);
            }
            long start = System.currentTimeMillis();
            graphics = spec.getRectangle(cproj,
                    ll1,
                    ll2,
                    staticArguments,
                    dynamicArgsHolder,
                    selectDist,
                    wantAreaEvents,
                    notifyOnChange,
                    clientID);
            long stop = System.currentTimeMillis();

            if (Debug.debugging("cspec")) {
                System.out.println(getName()
                        + "|BufferedCSpecLayer.getSpecGraphics(): got "
                        + graphics.length + " graphics in "
                        + ((stop - start) / 1000d) + " seconds.");
            }
        } catch (org.omg.CORBA.SystemException e) {
            dirtybits |= EXCEPTION;
            // don't freak out if we were only interrupted...
            if (e.toString().indexOf("InterruptedIOException") != -1) {
                System.err.println(getName()
                        + "|BufferedCSpecLayer.getSpecGraphics(): "
                        + "getRectangle() call interrupted!");
            } else {
                System.err.println(getName()
                        + "|BufferedCSpecLayer.getSpecGraphics(): "
                        + "Caught CORBA exception: " + e);
                System.err.println(getName()
                        + "|BufferedCSpecLayer.getSpecGraphics(): "
                        + "Exception class: " + e.getClass().getName());
                e.printStackTrace();
            }

            // dontcha just love CORBA? reinit later
            setSpecialist(null);
            if (showDialogs) {
                postCORBAErrorMsg("CORBA Exception while getting graphics from\n"
                        + getName() + " specialist:\n" + e.getClass().getName());
            }
        }
        return graphics;
    }

}