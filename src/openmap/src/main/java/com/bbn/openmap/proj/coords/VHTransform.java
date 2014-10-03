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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/VHTransform.java,v $
// $RCSfile: VHTransform.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;

/* Cathleen Lancaster - 6 Hutcheson */

/**
 * The VH coordinate system is used by ATT to compute distance used in
 * determining phone call costs.
 * <P>
 * 
 * VH coordinates can be used to compute distance simply, see distance().
 * <P>
 * 
 * This code is based on C code provided by the authors mentioned below, as well
 * as Lisp code by Larry Denenberg of BBN.
 * <P>
 * 
 * I have ported this code to Java, unified the forward and inverse
 * transformations and added some comments. I've left basic code and comments
 * mostly intact.
 * <P>
 * 
 * The url's to the original emails are:
 * <P>
 * 
 * http://x11.dejanews.com/getdoc.xp?AN=177302113&CONTEXT=895858362.931528704&
 * hitnum=1 <BR>
 * http://x11.dejanews.com/getdoc.xp?AN=223540739&CONTEXT=895858362.931528704&
 * hitnum=5
 */
public class VHTransform implements GeoCoordTransformation {
    /* Polynomial constants */
    public static final double K1 = .99435487;
    public static final double K2 = .00336523;
    public static final double K3 = -.00065596;
    public static final double K4 = .00005606;
    public static final double K5 = -.00000188;

    /* PI in various forms */
    public static final double M_PI_2 = Math.PI / 2.0;

    /*
     * spherical coordinates of eastern reference point EX^2 + EY^2 + EZ^2 = 1
     */
    public static final double EX = .40426992;
    public static final double EY = .68210848;
    public static final double EZ = .60933887;

    /*
     * spherical coordinates of western reference point WX^2 + WY^2 + WZ^2 = 1
     */
    public static final double WX = .65517646;
    public static final double WY = .37733790;
    public static final double WZ = .65449210;

    /* spherical coordinates of V-H coordinate system */
    /* PX^2 + PY^2 + PZ^2 = 1 */
    public static final double PX = -0.555977821730048699;
    public static final double PY = -0.345728488161089920;
    public static final double PZ = 0.755883902605524030;

    /* Rotation by 76 degrees */
    public final static double rot = Math.toRadians(76.597497064);
    public final static double ROTC = Math.cos(rot);
    public final static double ROTS = Math.sin(rot);

    /* orthogonal translation values */
    public static final double TRANSV = 6363.235;
    public static final double TRANSH = 2250.700;

    /** radius of earth in sqrt(0.1)-mile units, minus 0.3 percent */
    public static final double RADIUS = 12481.103;

    public static final double K9 = RADIUS * ROTC;
    public static final double K10 = RADIUS * ROTS;

    public VHTransform() {
    }

    /** Return the V corresponding to the most recent toVH(). * */
    public double getV() {
        return this.resultV;
    }

    /** Return the H corresponding to the most recent toVH(). * */
    public double getH() {
        return this.resultH;
    }

    /**
     * Return the latitude corresponding to the most recent toLatLon(). *
     */
    public double getLat() {
        return this.resultLat;
    }

    /**
     * Return the longitude corresponding to the most recent toLatLon(). *
     */
    public double getLon() {
        return this.resultLon;
    }

    /** Return the distance in miles between 2 VH pairs. * */
    public static double distance(double v1, double h1, double v2, double h2) {
        double dv = v2 - v1;
        double dh = h2 - h1;
        // Was
        // return Math.sqrt(dv*dv + dh*dh)/10.0;
        // Now, thanks to Andrew Canfield
        return Math.sqrt((dv * dv + dh * dh) / 10.0);
    }

    private double resultV = 0.0;
    private double resultH = 0.0;
    private double resultLat = 0.0;
    private double resultLon = 0.0;

    /*
     * 
     * Subject: Re: AT&T V-H Coordinates From: shoppa@alph02.triumf.ca (Tim
     * Shoppa) Date: 1996/08/28 Message-ID: <telecom16.450.6@massis.lcs.mit.edu>
     * Newsgroups: comp.dcom.telecom [More Headers] [Subscribe to
     * comp.dcom.telecom]
     * 
     * In article <telecom16.437.8@massis.lcs.mit.edu>, Drew Larsen
     * <dlarsen@objectwave.com> wrote: > Ok folks, scratch your heads and see if
     * you can remember how to > translate a point on the earth measured in
     * latitude/longitude to the > commonly used V&H system used in the telecom
     * industry.
     * 
     * Below is a past post by Stu Jeffery containing a program which does the
     * conversion the other way. If anybody is willing to buy me a nice lunch
     * (my standard fee for two dimensional function inversion), I'll modify it
     * to go both ways :-)
     * 
     * Tim Shoppa, TRIUMF theory group | Internet: shoppa@triumf.ca TRIUMF,
     * Canada's National Meson Facility | Voice: 604-222-1047 loc 6446 4004
     * WESBROOK MALL, UBC CAMPUS | FAX: 604-222-1074 University of British
     * Columbia, Vancouver, B.C., CANADA V6T 2A3
     * 
     * Article: 54928 of comp.dcom.telecom Date: Tue, 29 Aug 1995 00:16:38 -0800
     * From: stu@shell.portal.com (Stu Jeffery) Subject: Re: V&H Questions
     * Message-ID: <telecom15.362.11@eecs.nwu.edu> X-Telecom-Digest: Volume 15,
     * Issue 362, Message 11 of 11
     * 
     * Attached is a C program that will do what you want. I don't know anything
     * more than what is here. I think it was posted in a news group, so use at
     * your own legal risk. I have compiled it and it works fine.
     * 
     * Going the other way is a bit more complicated. Probably the simplest way
     * is by successive approximation.
     * 
     * Good Luck.
     * 
     * -----------------------------------------
     */

    /**
     * Computes Bellcore/AT&T V & H (vertical and horizontal) coordinates from
     * latitude and longitude. Used primarily by local exchange carriers (LEC's)
     * to compute the V & H coordinates for wire centers.
     * <P>
     * 
     * This is an implementation of the Donald Elliptical Projection, a
     * Two-Point Equidistant projection developed by Jay K. Donald of AT&T in
     * 1956 to establish long-distance telephone rates. (ref:
     * "V-H Coordinate Rediscovered", Eric K. Grimmelmann, Bell Labs Tech. Memo,
     * 9/80. (References Jay Donald notes of Jan 17, 1957.)) Ashok Ingle of
     * Bellcore also wrote an internal memo on the subject.
     * <P>
     * 
     * The projection is specially modified for the ellipsoid and is confined to
     * the United States and southern Canada.
     * <P>
     * 
     * Derived from a program obtained from an anonymous author within Bellcore
     * by way of the National Exchange Carrier Association. Cleaned up and
     * improved a bit by Tom Libert (tom@comsol.com, libert@citi.umich.edu).
     * <P>
     * 
     * CASH REWARD for copies of the reference papers, or for an efficient
     * (non-iterative) inverse for this program! (i.e. a program to compute lat
     * & long from V & H).
     */

    /** lat and lon are in degrees, positive north and east. */
    public void toVH(double lat, double lon) {

        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon);
        
        /* Translate east by 52 degrees */
        double lon1 = lon + Math.toRadians(52.0);
        /* Convert latitude to geocentric latitude using Horner's rule */
        double latsq = lat * lat;
        double lat1 = lat
                * (K1 + (K2 + (K3 + (K4 + K5 * latsq) * latsq) * latsq) * latsq);

        /*
         * x, y, and z are the spherical coordinates corresponding to lat, lon.
         */
        double cos_lat1 = Math.cos(lat1);
        double x = cos_lat1 * Math.sin(-lon1);
        double y = cos_lat1 * Math.cos(-lon1);
        double z = Math.sin(lat1);
        /*
         * e and w are the cosine of the angular distance (radians) between our
         * point and the east and west centers.
         */
        double e = EX * x + EY * y + EZ * z;
        double w = WX * x + WY * y + WZ * z;
        e = e > 1.0 ? 1.0 : e;
        w = w > 1.0 ? 1.0 : w;
        e = M_PI_2 - Math.atan(e / Math.sqrt(1 - e * e));
        w = M_PI_2 - Math.atan(w / Math.sqrt(1 - w * w));
        /* e and w are now in radians. */
        double ht = (e * e - w * w + .16) / .8;
        double vt = Math.sqrt(Math.abs(e * e - ht * ht));
        vt = (PX * x + PY * y + PZ * z) < 0 ? -vt : vt;
        /* rotate and translate to get final v and h. */
        double v = TRANSV + K9 * ht - K10 * vt;
        double h = TRANSH + K10 * ht + K9 * vt;
        this.resultV = v;
        this.resultH = h;
    }

    /*
     * Stu Jeffery Internet: stu@shell.portal.com 1072 Seena Ave. voice:
     * 415-966-8199 Los Altos, CA. 94024 fax: 415-966-8456 ////// Subject: V & H
     * to Latitude and Longitude From: sicherman@lucent.com (Col. G.L.
     * Sicherman) Date: 1997/03/05 Message-ID:
     * <telecom17.57.10@massis.lcs.mit.edu> Newsgroups: comp.dcom.telecom [More
     * Headers] [Subscribe to comp.dcom.telecom]
     * 
     * 
     * Recently I wanted to convert some Bell Labs "V&H" coordinates to latitude
     * and longitude. A careful search through the Telecomm- unications Archives
     * turned up a C program for converting in the other direction, and many
     * pleas for what I was looking for. One poster even offered money!
     * 
     * Since I work for Bell Labs, I had no trouble getting a copy of Erik
     * Grimmelmann's legendary memorandum. (Don't get your hopes up - Bell Labs
     * has no intention of releasing it to the public!) Thus armed, I hacked up
     * the following C program, which ought to compile on any C platform. Its
     * input and output agree with the output and input of ll_to_vh (as hacked
     * by Tom Libert), and the comments summarize the math as explained by
     * Grimmelmann. Enjoy!
     */
    /**
     * V&H is a system of coordinates (V and H) for describing locations of rate
     * centers in the United States. The projection, devised by J. K. Donald, is
     * an "elliptical," or "doubly equidistant" projection, scaled down by a
     * factor of 0.003 to balance errors.
     * <P>
     * 
     * The foci of the projection, from which distances are measured accurately
     * (except for the scale correction), are at 37d 42m 14.69s N, 82d 39m
     * 15.27s W (in Floyd Co., Ky.) and 41d 02m 55.53s N, 112d 03m 39.35 W (in
     * Webster Co., Utah). They are just 0.4 radians apart.
     * <P>
     * 
     * Here is the transformation from latitude and longitude to V&H: First
     * project the earth from its ellipsoidal surface to a sphere. This alters
     * the latitude; the coefficients bi in the program are the coefficients of
     * the polynomial approximation for the inverse transformation. (The
     * function is odd, so the coefficients are for the linear term, the cubic
     * term, and so on.) Also subtract 52 degrees from the longitude.
     * <P>
     * 
     * For the rest, compute the arc distances of the given point to the
     * reference points, and transform them to the coordinate system in which
     * the line through the reference points is the X-axis and the origin is the
     * eastern reference point. The solution is
     * <P>
     * h = (square of distance to E - square of distance to W + square of
     * distance between E and W) / twice distance between E and W; <BR>
     * v = square root of absolute value of (square of distance to E - square of
     * h).
     * <P>
     * Reduce by three-tenths of a percent, rotate by 76.597497 degrees, and add
     * 6363.235 to V and 2250.7 to H.
     * <P>
     * 
     * To go the other way, as this program does, undo the final translation,
     * rotation, and scaling. The z-value Pz of the point on the x-y-z sphere
     * satisfies the quadratic Azz+Bz+c=0, where
     * <P>
     * A = (ExWz-EzWx)^2 + (EyWzx-EzWy)^2 + (ExWy-EyWx)^2; <BR>
     * B = -2[(Ex cos(arc to W) - Wx cos(arc to E))(ExWz-EzWx) - (Ey cos(arc to
     * W) -Wy cos(arc to E))(EyWz-EzWy)]; <BR>
     * C = (Ex cos(arc to W) - Wx cos(arc to E))^2 + (Ey cos(arc to W) - Wy
     * cos(arc to E))^2 - (ExWy - EyWx)^2.
     * <P>
     * Solve with the quadratic formula. The latitude is simply the arc sine of
     * Pz. Px and Py satisfy
     * <P>
     * ExPx + EyPy + EzPz = cos(arc to E); <BR>
     * WxPx + WyPy + WzPz = cos(arc to W).
     * <P>
     * Substitute Pz's value, and solve linearly to get Px and Py. The longitude
     * is the arc tangent of Px/Py. Finally, this latitude and longitude are
     * spherical; use the inverse polynomial approximation on the latitude to
     * get the ellipsoidal earth latitude, and add 52 degrees to the longitude.
     */
    public void toLatLon(double v0, double h0) {
        /* GX = ExWz - EzWx; GY = EyWz - EzWy */
        final double GX = 0.216507961908834992;
        final double GY = -0.134633014879368199;
        /* A = (ExWz-EzWx)^2 + (EyWz-EzWy)^2 + (ExWy-EyWx)^2 */
        final double A = 0.151646645621077297;
        /* Q = ExWy-EyWx; Q2 = Q*Q */
        final double Q = -0.294355056616412800;
        final double Q2 = 0.0866448993556515751;
        final double EPSILON = .0000001;

        double v = (double) v0;
        double h = (double) h0;

        double t1 = (v - TRANSV) / RADIUS;
        double t2 = (h - TRANSH) / RADIUS;
        double vhat = ROTC * t2 - ROTS * t1;
        double hhat = ROTS * t2 + ROTC * t1;
        double e = Math.cos(Math.sqrt(vhat * vhat + hhat * hhat));
        double w = Math.cos(Math.sqrt(vhat * vhat + (hhat - 0.4) * (hhat - 0.4)));
        double fx = EY * w - WY * e;
        double fy = EX * w - WX * e;
        double b = fx * GX + fy * GY;
        double c = fx * fx + fy * fy - Q2;
        double disc = b * b - A * c; /* discriminant */
        double x, y, z, delta;
        if (Math.abs(disc) < EPSILON) {
            // if (disc==0.0) { /* It's right on the E-W axis */
            z = b / A;
            x = (GX * z - fx) / Q;
            y = (fy - GY * z) / Q;
        } else {
            delta = Math.sqrt(disc);
            z = (b + delta) / A;
            x = (GX * z - fx) / Q;
            y = (fy - GY * z) / Q;
            if (vhat * (PX * x + PY * y + PZ * z) < 0) { /*
                                                          * wrong direction
                                                          */
                z = (b - delta) / A;
                x = (GX * z - fx) / Q;
                y = (fy - GY * z) / Q;
            }
        }
        double lat = Math.asin(z);

        /*
         * Use polynomial approximation for inverse mapping (sphere to
         * spheroid):
         */
        final double[] bi = { 1.00567724920722457, -0.00344230425560210245,
                0.000713971534527667990, -0.0000777240053499279217,
                0.00000673180367053244284, -0.000000742595338885741395,
                0.0000000905058919926194134 };
        double lat2 = lat * lat;
        /*
         * KRA: Didn't seem to work at first, so i unrolled it. double earthlat
         * = 0.0; for (int i=6; i>=0; i--) { earthlat = (earthlat + bi[i]) * (i
         * > 0? lat2 : lat); }
         */

        double earthlat = lat
                * (bi[0] + lat2
                        * (bi[1] + lat2
                                * (bi[2] + lat2
                                        * (bi[3] + lat2
                                                * (bi[4] + lat2
                                                        * (bi[5] + lat2 * (bi[6])))))));
        earthlat = Math.toDegrees(earthlat);

        /*
         * Adjust longitude by 52 degrees:
         */
        double lon = Math.toDegrees(Math.atan2(x, y));
        double earthlon = lon + 52.0;

        this.resultLat = earthlat;
        this.resultLon = -earthlon;
        // Col. G. L. Sicherman.
    }

    public void toLatLon(int v0, int h0) {
        toLatLon((double) v0, (double) h0);
    }

    public Point2D forward(double lat, double lon) {
        return forward(lat, lon, null);
    }

    public Point2D forward(double lat, double lon, Point2D ret) {
        if (ret == null) {
            ret = new Point2D.Double();
        }

        toVH(lat, lon);
        ret.setLocation(getV(), getH());
        return ret;
    }

    public LatLonPoint inverse(double v, double h) {
        return inverse(v, h, null);
    }

    public LatLonPoint inverse(double v, double h, LatLonPoint ret) {
        if (ret == null) {
            ret = new LatLonPoint.Double();
        }
        toLatLon(v, h);
        ret.setLocation(getLat(), getLon());
        return ret;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: VHTransform lat lon");
            System.exit(0);
        }

        try {
            double lat = Double.parseDouble(args[0]);
            double lon = Double.parseDouble(args[1]);

            VHTransform vh = new VHTransform();
            Point2D vhpnts = vh.forward(lat, lon);

            System.out.println(vhpnts);
        } catch (NumberFormatException nfe) {
            System.out.println("can't parse numbers, should be lat, lon");
        }
    }
}