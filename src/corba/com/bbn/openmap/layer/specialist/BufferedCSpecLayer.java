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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/BufferedCSpecLayer.java,v $
// $RCSfile: BufferedCSpecLayer.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:04 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.specialist;


/*  Java Core  */
import java.applet.Applet;
import java.awt.Point;
import java.awt.event.*;
import java.awt.Component;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Properties;

/*  CORBA  */
import org.omg.CORBA.ORB;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ShortHolder;
import org.omg.CORBA.StringHolder;
import com.bbn.openmap.CSpecialist.CProjection;
import com.bbn.openmap.CSpecialist.GraphicChange;
import com.bbn.openmap.CSpecialist.GraphicPackage.*;
import com.bbn.openmap.CSpecialist.BitmapPackage.*;
import com.bbn.openmap.CSpecialist.LinePackage.*;
import com.bbn.openmap.CSpecialist.PolyPackage.*;
import com.bbn.openmap.CSpecialist.RasterPackage.*;
import com.bbn.openmap.CSpecialist.RectanglePackage.*;
import com.bbn.openmap.CSpecialist.TextPackage.*;
import com.bbn.openmap.CSpecialist.CirclePackage.*;
import com.bbn.openmap.CSpecialist.UnitSymbolPackage.*;
import com.bbn.openmap.CSpecialist.U2525SymbolPackage.*;
import com.bbn.openmap.CSpecialist.LLPoint;
import com.bbn.openmap.CSpecialist.Server;
import com.bbn.openmap.CSpecialist.ServerHelper;
import com.bbn.openmap.CSpecialist.UGraphic;
import com.bbn.openmap.CSpecialist.UWidget;
import org.omg.CosNaming.*;

/*  OpenMap  */
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.Environment;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.SwingWorker;

/**
 * BufferedCSpecLayer is a Layer which communicates to CORBA Specialists.
 */
public class BufferedCSpecLayer extends CSpecLayer {

    private final static String[] debugTokens = {
        "debug.cspec"
    };

    // Cached graphics
    UGraphic[] graphics = null;


    /** Default constructor, that sets the MapMouseListener for this
     * layer to itself.
     * */
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
            System.out.println(getName() + "|BufferedCSpecLayer.getSpecGraphics()");
        }

        // If we have graphics, return them
        if (graphics != null) {
            System.out.println("Returning cached graphics");
            return graphics;
        }

        cproj = new CProjection ((short)(p.getProjectionType()),
                                 new LLPoint(p.getCenter().getLatitude(),
                                             p.getCenter().getLongitude()),
                                 (short)p.getHeight(),
                                 (short)p.getWidth(),
                                 (int)p.getScale());

        // lat-lon "box", (depends on the projection)
        LatLonPoint ul = p.getUpperLeft();
        LatLonPoint lr = p.getLowerRight();

//      ll1 = new LLPoint(ul.getLatitude(), ul.getLongitude());
//      ll2 = new LLPoint(lr.getLatitude(), lr.getLongitude());
        // Adjust lat/lon for total global area
        ll1 = new LLPoint(90.0f,-180.0f);
        ll2 = new LLPoint(-90.0f,180.0f);
        // check for cancellation
        if (isCancelled()) {
            dirtybits |= PREMATURE_FINISH;
            if (Debug.debugging("cspec"))
                System.out.println(getName() + "|BufferedCSpecLayer.getSpecGraphics(): aborted.");
            return null;
        }
        // check for null specialist
        if (spec == null) {
            System.err.println(getName() + "|BufferedCSpecLayer.getSpecGraphics(): null specialist!");
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
                    System.out.println(getName() +
                        "|BufferedCSpecLayer.getSpecGraphics():" +
                        " calling getRectangle with projection: " + p +
                        " ul=" + ul + " lr=" + lr +
                        " staticArgs=\"" + staticArguments + "\"" +
                        " dynamicArgs=\"" + dynamicArgsHolder.value + "\"" +
                        " clientID=" + clientID); 
            }
            long start = System.currentTimeMillis();
            graphics = spec.getRectangle(cproj,
                                         ll1, ll2,
                                         staticArguments,
                                         dynamicArgsHolder,
                                         selectDist,
                                         wantAreaEvents,
                                         notifyOnChange,
                                         clientID);
            long stop = System.currentTimeMillis();

            if (Debug.debugging("cspec")) {
                System.out.println(getName()+"|BufferedCSpecLayer.getSpecGraphics(): got " +
                                   graphics.length + " graphics in " + ((stop-start)/1000d) +
                                   " seconds.");
            }
        } catch (org.omg.CORBA.SystemException e) {
            dirtybits |= EXCEPTION;
            // don't freak out if we were only interrupted...
            if (e.toString().indexOf("InterruptedIOException") != -1) {
                System.err.println(getName()+"|BufferedCSpecLayer.getSpecGraphics(): " +
                        "getRectangle() call interrupted!");
            } else {
                System.err.println(getName()+"|BufferedCSpecLayer.getSpecGraphics(): " +
                                   "Caught CORBA exception: " + e);
                System.err.println(getName()+"|BufferedCSpecLayer.getSpecGraphics(): " +
                                   "Exception class: " + e.getClass().getName());
                e.printStackTrace();
            }

            // dontcha just love CORBA? reinit later
            setSpecialist(null);
            if (showDialogs) {
                postCORBAErrorMsg("CORBA Exception while getting graphics from\n" +
                                  getName() + " specialist:\n" + e.getClass().getName());
            }
        }
        return graphics;
    }


}
