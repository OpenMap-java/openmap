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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Azimuth.java,v $
// $RCSfile: Azimuth.java,v $
// $Revision: 1.4 $
// $Date: 2004/02/04 16:05:43 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.*;
import java.util.ArrayList;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.Environment;
import com.bbn.openmap.util.Debug;

/**
 * Base of all azimuthal projections.
 * <p>
 * @see Orthographic
 * @see Gnomonic
 */
public abstract class Azimuth extends Proj {

    // encapsules extra variables to forward() method call
    protected static class AzimuthVar {
        // invalid_forward - flag value marks a forward() of a point which
        // is not visible.
        boolean invalid_forward = false;        // last forward() was invalid
        // current_azimuth - azimuth (direction from center) of last invalid
        // forwarded point.
        float current_azimuth = Float.NaN;      // azimuth of last forward
        // extra slot
        int index;
    }

    // HACK
    final static float ACCEPTABLE_AZ = ProjMath.degToRad(5);
    
    protected Point world;                      // world width and height in pixels.

    /**
     * Traverse poly vertices in clockwise order.
     */
    protected boolean clockwise = true;


    /**
     * Construct an azimuthal projection.
     * <p>
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     * @param type projection type
     */
    public Azimuth(
            LatLonPoint center, float scale, int width, int height, int type)
    {
        super(center, scale, width, height, type);
    }


    /**
     * Return stringified description of this projection.
     * <p>
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
        return " world(" + world.x + "," + world.y + ") " + super.toString();
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" parameters
     * used in the forward() and inverse() calls.<p>
     *
     */
    protected void computeParameters() {
        planetPixelRadius = planetRadius * pixelsPerMeter;
        planetPixelCircumference = MoreMath.TWO_PI*planetPixelRadius;

        // minscale is the minimum scale allowable (before integer wrapping
        // can occur)
        minscale = (float)Math.ceil((2*planetPixelRadius)/(int)Integer.MAX_VALUE);
        if (minscale < 1)
            minscale = 1;
        if (scale < minscale)
            scale = minscale;

        // maxscale = scale at which a world hemisphere fits in the window
        maxscale = (width < height)
            ? (float)(planetPixelRadius*2)/(float)width
            : (float)(planetPixelRadius*2)/(float)height;
        if (maxscale < minscale) {
            maxscale = minscale;
        }
        if (scale > maxscale) {
            scale = maxscale;
        }
        scaled_radius = planetPixelRadius/scale;

        if (world == null)
            world = new Point(0,0);

        // width of the world in pixels at current scale.  We see only
        // one hemisphere.
        world.x = (int)((planetPixelRadius*2)/scale);

        // calculate cutoff scale for XWindows workaround
        XSCALE_THRESHOLD = (int)((planetPixelRadius*2)/64000);//fudge it a little bit

        if (Debug.debugging("proj")) {
            Debug.output("Azimuth.computeParameters(): " +
                    "world.x = " + world.x + " XSCALE_THRESHOLD = " +
                    XSCALE_THRESHOLD);
        }
    }


    /**
     * Toggle clockwise traversal of poly vertices.
     * @param value boolean
     */
    public void setClockwiseTraversal(boolean value) {
        clockwise = value;
    }


    /**
     * Get poly-traversal setting (clockwise or counter-clockwise).
     * @return boolean
     */
    public boolean isClockwiseTraversal() {
        return clockwise;
    }


    /**
     * Forward project a point.
     * Wrapper around Azimuth-specific forwarding.
     */
    public final Point forward(LatLonPoint llp, Point pt) {
        return _forward(normalize_latitude(llp.radlat_),
                        wrap_longitude(llp.radlon_),
                        pt, null);
    }


    /** 
     * Forward project a point. Wrapper around Azimuth-specific
     * forwarding.  Lat and lon assumed to be decimal degrees.
     */
    public final Point forward(float lat, float lon, Point pt) {
        return _forward(
                normalize_latitude(ProjMath.degToRad(lat)),
                wrap_longitude(ProjMath.degToRad(lon)),
                pt, null);
    }


    /**
     * Forward project a point.  Wrapper around Azimuth-specific
     * forwarding.  Lat/lon assumed to be radians.  isRadian only used
     * to create a different method signature - assumed to be true.
     */
    public final Point forward(float lat, float lon, Point pt, boolean isRadian) {
        return _forward(
                normalize_latitude(lat), wrap_longitude(lon), pt, null);

    }


    /**
     * Forward project a point.
     * If the point is not within the viewable hemisphere, return flags in
     * AzimuthVar variable if specified.
     * @param lat latitude in radians
     * @param lon longitude in radians
     * @param pt Point
     * @param azVar AzimuthVar or null
     * @return Point pt
     */
    protected abstract Point _forward(
            float lat, float lon, Point pt, AzimuthVar azVar);


    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(±180, c)</code> pan south
     * <li><code>pan(-90, c)</code> pan west
     * <li><code>pan(0, c)</code> pan north
     * <li><code>pan(90, c)</code> pan east
     * </ul>
     * @param Az azimuth "east of north" in decimal degrees:
     * <code>-180 &lt;= Az &lt;= 180</code>
     */
    public void pan(float Az) {
        if (MoreMath.approximately_equal(Math.abs(Az), 180f, 0.01f)) {
            _panS();
        } else if (MoreMath.approximately_equal(Az, -135f, 0.01f)) {
            _panSW();
        } else if (MoreMath.approximately_equal(Az, -90f, 0.01f)) {
            _panW();
        } else if (MoreMath.approximately_equal(Az, -45f, 0.01f)) {
            _panNW();
        } else if (MoreMath.approximately_equal(Az, 0f, 0.01f)) {
            _panN();
        } else if (MoreMath.approximately_equal(Az, 45f, 0.01f)) {
            _panNE();
        } else if (MoreMath.approximately_equal(Az, 90f, 0.01f)) {
            _panE();
        } else if (MoreMath.approximately_equal(Az, 135f, 0.01f)) {
            _panSE();
        } else {
            super.pan(Az);
        }
    }


    /**
     * Pan the map northwest.
     */
    protected void _panNW() {
        if (overNorthPole()) {
            setCenter(new LatLonPoint(NORTH_POLE, ctrLon-((float)Math.PI/4), true));
        } else {
            LatLonPoint to = inverse(0, 0);
            float lat = inverse((int)(width/2), 0).radlat_;//center lat
            to.setLatitude(ProjMath.radToDeg(lat));
            // check for large planet
            if (MoreMath.approximately_equal(to.radlon_, ctrLon, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, -((float)Math.PI/4f));
            }
            setCenter(to);
        }
    }


    /**
     * Pan the map north.
     */
    protected void _panN() {
        if (overNorthPole()) {
            setCenter(LatLonPoint.NORTH_POLE, ProjMath.radToDeg(ctrLon));
        } else {
            LatLonPoint to = inverse((int)(width/2), 0);
            // check for large planet
            if (MoreMath.approximately_equal(to.radlat_, ctrLat, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, 0);
            }
            setCenter(to);
        }
    }


    /**
     * Pan the map northeast.
     */
    protected void _panNE() {
        if (overNorthPole()) {
            setCenter(new LatLonPoint(NORTH_POLE, ctrLon+((float)Math.PI/4), true));
        } else {
            LatLonPoint to = inverse(width-1, 0);
            float lat = inverse((int)(width/2), 0).radlat_;//center lat
            to.setLatitude(ProjMath.radToDeg(lat));
            // check for large planet
            if (MoreMath.approximately_equal(to.radlon_, ctrLon, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, (float)(Math.PI/4f));
            }
            setCenter(to);
        }
    }


    /**
     * Pan the map east.
     */
    protected void _panE() {
        // when we're over the poles, then pan by 45 degrees each time
        if (overNorthPole() || overSouthPole())
            setCenter(new LatLonPoint(ctrLat, ctrLon+((float)Math.PI/4), true));
        // otherwise approximate something good
        else {
            LatLonPoint to = inverse(new Point(width-1, (int)(height/2)));
            to.setLatitude(ProjMath.radToDeg(ctrLat));//keep the same latitude
            // check for large planet
            if (MoreMath.approximately_equal(to.radlon_, ctrLon, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, (float)(Math.PI/2f));
            }
            setCenter(to);
        }
    }


    /**
     * Pan the map southeast.
     */
    protected void _panSE() {
        if (overSouthPole()) {
            setCenter(new LatLonPoint(SOUTH_POLE, ctrLon+((float)Math.PI/4), true));
        } else {
            LatLonPoint to = inverse(width-1, height-1);
            float lat = inverse((int)(width/2), height-1).radlat_;//center lat
            to.setLatitude(ProjMath.radToDeg(lat));
            // check for large planet
            if (MoreMath.approximately_equal(to.radlon_, ctrLon, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, (float)(0.75f*Math.PI));
            }
            setCenter(to);
        }
    }


    /**
     * Pan the map south.
     */
    protected void _panS() {
        if (overSouthPole()) {
            setCenter(LatLonPoint.SOUTH_POLE, ProjMath.radToDeg(ctrLon));
        } else {
            LatLonPoint to = inverse((int)(width/2), height);
            // check for large planet
            if (MoreMath.approximately_equal(to.radlat_, ctrLat, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, (float)Math.PI);
            }
            setCenter(to);
        }
    }


    /**
     * Pan the map southwest.
     */
    protected void _panSW() {
        if (overSouthPole()) {
            setCenter(new LatLonPoint(SOUTH_POLE, ctrLon-((float)Math.PI/4), true));
        } else {
            LatLonPoint to = inverse(0, height-1);
            float lat = inverse((int)(width/2), height-1).radlat_;//center lat
            to.setLatitude(ProjMath.radToDeg(lat));
            // check for large planet
            if (MoreMath.approximately_equal(to.radlon_, ctrLon, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, (float)(-0.75f*Math.PI));
            }
            setCenter(to);
        }
    }


    /**
     * Pan the map west.
     */
    protected void _panW() {
        // when we're over the poles, then pan by 45 degrees each time
        if (overNorthPole() || overSouthPole())
            setCenter(new LatLonPoint(ctrLat, ctrLon-((float)Math.PI/4), true));
        // otherwise approximate something good
        else {
            LatLonPoint to = inverse(new Point(0, (int)(height/2)));
            to.setLatitude(ProjMath.radToDeg(ctrLat));//keep the same latitude
            // check for large planet
            if (MoreMath.approximately_equal(to.radlon_, ctrLon, 0.0001f)) {
                // cast out to hemisphere edge
                to = GreatCircle.spherical_between(
                        ctrLat, ctrLon, MoreMath.HALF_PI, (float)(-Math.PI/2f));
            }
            setCenter(to);
        }
    }


    /**
     * Checks if the north pole is visible on the screen.
     * <p>
     * @return boolean
     */
    public boolean overNorthPole() {
        return overPoint(NORTH_POLE, 0f);
    }


    /**
     * Checks if the south pole is visible on the screen.
     * <p>
     * @return boolean
     */
    public boolean overSouthPole() {
        return overPoint(SOUTH_POLE, 0f);
    }


    /**
     * Checks if the point is visible on the screen.
     * @param lat latitude in radians
     * @param lon longitude in radians
     * @return boolean true if visible, false if not
     */
    public boolean overPoint(float lat, float lon) {
        AzimuthVar azVar = new AzimuthVar();
        Point pt = _forward(lat, lon, new Point(), azVar);
        if (azVar.invalid_forward) {
            return false;
        }
        if ((pt.x < 0) || (pt.x > width) || (pt.y < 0) || (pt.y > height)) {
            return false;
        }
        return true;
    }


    /**
     * Forward project a lat/lon Poly.
     * This is a complex method.  Please read the in-code
     * documentation for an explanation of the algorithm.
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or
     * rhumbline line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     */
    protected ArrayList _forwardPoly(
        float[] rawllpts, int ltype, int nsegs, boolean isFilled)
    {
        // Idea:
        //      The azimuthal projection family (mostly) shows one
        //      hemisphere only.
        //      A Poly can be projected in 3 ways:
        //              1) fully inside hemisphere
        //              2) fully outside hemisphere
        //              3) partially inside/outside hemisphere
        //      Case 3 is the hard one to deal with.  A Poly which is
        //      partially inside will be projected into 1 or more
        //      subsections.  If the poly isn't filled, we can just
        //      return the subsections.  If the poly is filled, then
        //      we need to add points that lie on the hemisphere edge
        //      in order to complete the polygon.
        //
        // Algorithm:
        //      * Iterate through rawllpts, projecting as we go.  If
        //      we see any inside->outside and outside->inside
        //      transitions, mark them and save them.
        //
        //      * Return all projected points if case 1, or nothing
        //      if case 2.
        //      
        //      * For case 3:
        //
        //              * If poly isn't filled, just return the
        //              subsections determined while iterating.  For
        //              example return four projected subsections in
        //              the following ways:
        //
        //                                  AA
        //              /--------------------------------------------\
        //              |  1  |xx|     2     |xxxx|   3    |xx|  4   |
        //              \--------------------------------------------/
        //               ^---^    ^---------^      ^------^    ^----^
        //
        //                                  BB
        //              /--------------------------------------------\
        //              |xx| 1  |x|   2    |xxxxx|   3   |xx|   4  |x|
        //              \--------------------------------------------/
        //                  ^--^  ^-------^       ^-----^    ^----^
        //
        //                                  CC
        //              /--------------------------------------------\
        //              |x| 1  |xx|   2    |xxxx|   3   |xxxx|   4   |
        //              \--------------------------------------------/
        //                 ^--^    ^-------^     ^-----^      ^-----^
        //
        //                                  DD
        //              /--------------------------------------------\
        //              |  1  |xxx|   2    |xxxxx|   3   |xx|   4  |x|
        //              \--------------------------------------------/
        //               ^---^    ^-------^       ^-----^    ^----^
        //
        //              * Otherwise poly is filled.  There is a
        //              special case of AA above where we need to wrap
        //              the vertices so that the coloring is done
        //              correctly for 3 subsections:
        //
        //                             AA with wrap
        //              /--------------------------------------------\
        //              |  1b |xx|      2    |xxxx|   3    |xx|  1a  |
        //              \--------------------------------------------/
        //               ----^    ^---------^      ^------^    ^-----
        //
        //              We wrap by copying the finishing 1b section to
        //              the back:
        //
        //              /--------------------------------------------+-----\
        //              |  1b |xx|      2    |xxxx|   3    |xx|  1a  | 1b  |
        //              \--------------------------------------------+-----/
        //                        ^---------^      ^------^    ^----------^
        //
        //      * For filled polys, we also need to add vertices along
        //      the horizon edge in order to make sure the rendering
        //      is done properly along the horizon edge.  The
        //      interested hacker (the one who's still reading this
        //      comment at this point!) should refer to the method
        //      following this one for a description of the caveats
        //      and the extra processing needed for filled polys.
        //

        int len = rawllpts.length>>>1;
        if (len < 2)
            return new ArrayList(0);

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype))
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);

        int invalid_count=0;//number of invalid points
        boolean curr_invalid, prev_invalid = false;//previous invalid forward
        Point temp = new Point();
        AzimuthVar az_first=null, az_save=null, azVar=new AzimuthVar();
        ArrayList sections = new ArrayList(128);
        int[] x_, xs = new int[len];
        int[] y_, ys = new int[len];

        // handle first point
        _forward(rawllpts[0], rawllpts[1], temp, azVar);
        xs[0]=temp.x; ys[0]=temp.y;
        prev_invalid = azVar.invalid_forward;
        if (prev_invalid) {
            ++invalid_count;
        } else {
            // save beginning of subsection if not filled
            azVar.index=0;
            azVar.current_azimuth = 
                GreatCircle.spherical_azimuth(ctrLat, ctrLon, rawllpts[0], rawllpts[1]);
//          Debug.output("marker0="+azVar.index+
//                  " az="+ProjMath.radToDeg(azVar.current_azimuth));
            if (!isFilled) {
                sections.add(azVar);
            } else {
                az_first=azVar;
            }
            azVar = new AzimuthVar();
        }

        // iterate through all rawllpts
        int i=0, j=0;
        for (i=1, j=2; i<len; i++, j+=2) {
            azVar.invalid_forward=false;//reset forward flag
            _forward(rawllpts[j], rawllpts[j+1], temp, azVar);
            curr_invalid=azVar.invalid_forward;
            xs[i]=temp.x; ys[i]=temp.y;
            if (!curr_invalid && prev_invalid) {
                // record transition (outside -> inside)
                azVar.index = i-1;//include outside point
                azVar.current_azimuth =
                    GreatCircle.spherical_azimuth(ctrLat, ctrLon, rawllpts[j-2], rawllpts[j-1]);
//              Debug.output("marker oi="+azVar.index+
//                      " az="+ProjMath.radToDeg(azVar.current_azimuth));
                sections.add(azVar);
                azVar = new AzimuthVar();
            } else if (curr_invalid) {
                if (!prev_invalid) {
                    // record transition (inside -> outside)
                    azVar.index = i;//include outside point
                    if (isFilled && (invalid_count == 0)) {
                        az_save = azVar;//save wrap-end
                    } else {
//                      Debug.output("marker io="+azVar.index+
//                              " az="+ProjMath.radToDeg(azVar.current_azimuth));
                        sections.add(azVar);
                    }
                    azVar = new AzimuthVar();
                }
                ++invalid_count;
            }
            prev_invalid=curr_invalid;
        }

        // poly completely inside
        if (invalid_count == 0) {
            ArrayList ret_val = new ArrayList(2);
            ret_val.add(xs);
            ret_val.add(ys);
            return ret_val;
        }
        // poly completely outside
        if (invalid_count == len) {
            return new ArrayList(0);
        }

        // handle poly that is partially inside hemisphere

        // cases AA & CC
        if (!prev_invalid) {
            // special case AA wrapping:
            if (isFilled && (az_save != null)) {
                // copy the wrapped portion into 
                int l = az_save.index;
                x_ = new int[len+l];
                y_ = new int[len+l];
                System.arraycopy(xs, 0, x_, 0, len);
                System.arraycopy(ys, 0, y_, 0, len);
                System.arraycopy(xs, 0, x_, len, l);
                System.arraycopy(ys, 0, y_, len, l);
                az_save.index=len+l;
//              Debug.output("wrap end="+az_save.index+
//                      " az="+ProjMath.radToDeg(az_save.current_azimuth));
                sections.add(az_save);//complete section
                xs=x_;
                ys=y_;
            // case CC or AA (non-wrapping):
            } else {
                if (isFilled && (az_save == null)) {
                    Debug.output("AA, filled, no-wrap!");
                }
                azVar.index = i;
                j = rawllpts.length;
                azVar.current_azimuth = 
                    GreatCircle.spherical_azimuth(ctrLat, ctrLon, rawllpts[j-2], rawllpts[j-1]);
//              Debug.output("marker end="+azVar.index+
//                      " az="+ProjMath.radToDeg(azVar.current_azimuth));
                sections.add(azVar);
            }
        // special case DD
        } else if (az_save != null) {
            Debug.output("DD, filled!");
            sections.add(az_first);
            sections.add(az_save);
        }

        int size = sections.size();
        ArrayList ret_val = new ArrayList(size);

        // filled poly: handle fill problems
        if (isFilled && (len > 2)) {
            generateFilledPoly(xs, ys, sections, ret_val);
            return ret_val;
        }

        // non-filled poly: just extract the subsections
        for (j=0; j<size; j+=2) {
            AzimuthVar az1 = (AzimuthVar)sections.get(j);
            AzimuthVar az2 = (AzimuthVar)sections.get(j+1);
            int off1 = az1.index;
            int off2 = az2.index;
            int l = off2-off1;
            x_ = new int[l];
            y_ = new int[l];
            System.arraycopy(xs, off1, x_, 0, l);
            System.arraycopy(ys, off1, y_, 0, l);
            ret_val.add(x_);
            ret_val.add(y_);
        }
        return ret_val;
    }//_forwardPoly()


    // This is meant to be called from _forwardPoly() after
    // determining that a FILLED polygon straddles the edge of the
    // projection/hemisphere.
    //
    // IDEA:
    //
    // We already have a bunch of sections of the poly.  Connect the
    // sections by adding extra edge points along the
    // projection/hemisphere edge.  From 1 polygon we can potentially
    // get N sub-sections, each of which needs to be connected and
    // filled.  To create the hemisphere edge points we need to know
    // the orientation of the fill (vertices in clockwise or
    // counter-clockwise order).
    //
    // The following ASCII picture indicates a simple case where a
    // polygon straddles the hemisphere.
    //
    //                       |
    //     Inside            |                Outside
    //   Hemisphere     _____| <- in->out    Hemisphere
    //              ___x     +
    //             x         |      Add vertices `+' along hemisphere
    //   Clockwise |  Fill   |      edge (in clockwise order for this
    //   Vertices  |  Color  +      example to complete filled poly
    //      `x'    x  Here   /      subsection.
    //              \       /
    //              x      +
    //             /      / <-- Hemisphere Edge
    //            /      /
    //   ________x___+__+
    //           ^
    //            \
    //             out->in
    //
    // PROBLEMS:
    //
    // Unfortunately the previous example is one of the simple cases,
    // and the algorithm quickly runs into difficulty with certain
    // polys.  The core of the algorithm is to take the azimuth of the
    // inside->outside vertex, find the next closest outside->inside
    // azimuth (in the indicated order), and add vertices between them
    // to complete the edge.
    //
    // The assumption is that the azimuths of the in->out and the
    // out->in vertices remain in the indicated order (clockwise or
    // counter-clockwise).  Unfortunately this is a faulty assumption
    // in many cases, and leads to a HACK!  The azimuth values of the
    // edge points may actually appear to switch order, which blows up
    // our hemisphere edge calculations.  The algorithm corrects
    // (hacks-around) this problem by disallowing excessively wide
    // polygons (in->out and out->in azimuth difference of > 180°).
    // (Thus, we stamp out one problem only to introduce another, but
    // this problem as easier to work around as we shall see below).
    //
    // PROBLEMATIC WIDE POLYS:
    //
    // Consider a polygon band around the world following east-west
    // rhumblines at latitudes ±20:
    // (-20,-10),(-20,-90),(-20,±180),(-20,90),(-20,10),
    // (20,10),(20,90),(20,±180),(20,-90),(20,-10),(-20,-10)
    //
    // Obviously we want to draw color INSIDE this band, but as we
    // will see, this doesn't happen in certain viewing positions.
    // Imagine viewing this polygon in an orthographic projection
    // centered at the north pole: you would see the northern edge of
    // the poly enter the hemisphere at 10E, circle around the long
    // way going east and leave the hemisphere at 10W.  Although the
    // polyline is drawn correctly, the fill side will be rendered
    // incorrectly since the algorithm will connect the polygon the
    // short way around the projection edge.  Here's the picture:
    //
    //        __________________
    //       /                  \
    //      /    _____x______    \
    //     /    /            \    \
    //    /    /              \    \
    //   /    /                \    \
    //  /    /                  \    \
    // /    /      Azimuth       \    \
    // |   /        ±180°         |   |
    // |   |          |           |   |
    // |   |          |           |   |
    // |   x    -90°--N--90°      x   |
    // |   |          |           |   |
    // |   |          |           |   |
    // |   |         0°           /   |
    // \    \                    /    /
    //  \    \                  /    /
    //   \    \  in->   out->  /    /
    //    \    \  out    in   /    /
    //     \    \__        __/    /
    //      \      x      x      /
    //       \_____|______|_____/
    //              ^^^^^^
    //
    // POLY WORKAROUNDS:
    //
    // You may need to break large polygons like the example one into
    // smaller subsections.  So for the example above, break it into 3
    // or 4 swaths (poly1 from 10W-90W, poly2 from 90W-180W, poly 3
    // from 10E-90E, poly4 from 90E-180E).  You may then choose to use
    // these polygons to draw the FILL ONLY, and also include the
    // original poly to draw the bounding outline (remember that
    // polylines don't have the fill problem just described!)
    //
    private void generateFilledPoly(
            int[] xs, int[] ys, ArrayList sections, ArrayList ret_vec)
    {
        AzimuthVar beginAz, oiAz, ioAz;
        ArrayList merged = null;
        ArrayList masterList = new ArrayList();
        float[] edgePoints = null;

        // begin, in->out, out->in, end indices
        int bg=0, io=1, oi, en;
        int vertexCount = 0;

        // iterate over the sections
        while (sections.size() > 0) {
            beginAz = (AzimuthVar)sections.get(bg);//out->in begin section
            ioAz = (AzimuthVar)sections.get(io);//in->out end section

            //find next closest out->in section
            oi = findClosestAzimuth(
                    sections, ioAz.current_azimuth, clockwise);
            oiAz = (AzimuthVar)sections.get(oi);
            en = oi+1;

            // closed a section
            if (oi == bg) {
                // finished a complex section
                if (merged != null) {
                    // close the polygon
                    edgePoints = getHemisphereEdge(
                            oiAz.current_azimuth, ioAz.current_azimuth);
                    vertexCount += (edgePoints.length>>>1);
                    merged.add(edgePoints);
                    masterList.add(new Integer(vertexCount));
                    merged = null;
                    vertexCount=0;
                }

                // otherwise doing a simple section
                else {
                    hemisphereClip(xs, ys, oiAz, ioAz, ret_vec);
                }
                sections.remove(io);
                sections.remove(bg);
                bg=0; io=1;//restart parse
                continue;
            }

            // merge complex section

            if (merged == null) {
//              Debug.output("complex-edge filled poly");
                // start new complex section
                merged = new ArrayList();
                masterList.add(merged);
                merged.add(beginAz);
                merged.add(ioAz);
                vertexCount += ioAz.index - beginAz.index;
            }
            // connect the sections
            edgePoints = getHemisphereEdge(
                    oiAz.current_azimuth, ioAz.current_azimuth);
            vertexCount += (edgePoints.length>>>1);
            merged.add(edgePoints);
            AzimuthVar endAz = (AzimuthVar)sections.get(en);
            merged.add(oiAz);
            merged.add(endAz);
            vertexCount += endAz.index - oiAz.index;

            // remove intermediary azvars
            sections.set(io, endAz);
            sections.remove(en);
            sections.remove(oi);
        }

        // Create closed polys from the merged sections (if any)
        Point temp = new Point();
        int masterSize = masterList.size();
        for (int i=0; i<masterSize; i+=2) {
            merged = (ArrayList)masterList.get(i);
            vertexCount = ((Integer)masterList.get(i+1)).intValue();

            // allocate space for all vertices
            int[] x_ = new int[vertexCount];
            int[] y_ = new int[vertexCount];
            int off=0, off1, off2, l, edgelen;

            int size = merged.size();
            for (int j=0; j<size; j+=3) {

                // extract section
                off1 = ((AzimuthVar)merged.get(j)).index;
                off2 = ((AzimuthVar)merged.get(j+1)).index;
                l = off2-off1;
                System.arraycopy(xs, off1, x_, off, l);
                System.arraycopy(ys, off1, y_, off, l);
                off += l;

                // project horizon edge
                edgePoints = (float[])merged.get(j+2);
                edgelen = edgePoints.length;
                for (int k=0; k<edgelen; k+=2) {
                    _forward(edgePoints[k], edgePoints[k+1], temp, null);
                    x_[off] = temp.x;
                    y_[off] = temp.y;
                    ++off;
                }
            }

            // store the fully-closed vertices in return ArrayList
            ret_vec.add(x_);
            ret_vec.add(y_);
        }
    }

    // Find the closest azimuth value to the one listed.  Check in
    // appropriate clockwise or counter-clockwise direction.  This is
    // called from generateFilledPoly().
    private int findClosestAzimuth(
            ArrayList sections, float az, boolean clockwise)
    {
        float delta;
        float closest = (clockwise) ? -MoreMath.TWO_PI : MoreMath.TWO_PI;
        int id=-1;
        AzimuthVar oiAz;
        // determine closest out->in azimuth
        for (int k=sections.size()-2; k>=0; k-=2) {
            oiAz = (AzimuthVar)sections.get(k);//out->in azimuth
            delta = az - oiAz.current_azimuth;//az delta along horizon
            if (delta > Math.PI)
                delta = -MoreMath.TWO_PI + delta;
            else if (delta < -Math.PI)
                delta = MoreMath.TWO_PI + delta;
            if (clockwise) {
                if (delta > 0)
                    delta = -MoreMath.TWO_PI + delta;
                if (closest <= delta) {
                    closest = delta;
                    id = k;
                }
            } else {
                if (delta < 0)
                    delta = MoreMath.TWO_PI + delta;
                if (closest >= delta) {
                    closest = delta;
                    id = k;
                }
            }
        }
        //save distance and index of closest az
        return id;
    }

    // Calculate radian points along the hemisphere edge between two
    // azimuths.  (Azimuths "East-of-North" relative to center point
    // of projection).  This is called from generateFilledPoly().
    private float[] getHemisphereEdge(float oiAz, float ioAz) {
//      Debug.output(
//      "oiAz="+ProjMath.radToDeg(oiAz)+" ioAz="+ProjMath.radToDeg(ioAz));
        // get the azimuth delta, and normalize it
        float delta = oiAz - ioAz;
        if (delta > Math.PI)
            delta = -MoreMath.TWO_PI + delta;
        else if (delta < -Math.PI)
            delta = MoreMath.TWO_PI + delta;
        delta = Math.abs(delta);

//      Debug.output("delta="+ProjMath.radToDeg(delta));

        // get the two LatLonPoints on the edge.  
        LatLonPoint ll1 = GreatCircle.spherical_between(
            ctrLat, ctrLon, MoreMath.HALF_PI, ioAz);
        LatLonPoint ll2 = GreatCircle.spherical_between(
            ctrLat, ctrLon, MoreMath.HALF_PI, oiAz);

//      Debug.output("ll1="+ll1+" ll2="+ll2);

        // calculate an acceptable number of points along horizon
        // edge from ioAz to oiAz.
        int npts = (int)(Math.abs(delta)/ACCEPTABLE_AZ);
        if (npts == 0) ++npts;
        float[] radpts = GreatCircle.great_circle(
            ll1.radlat_, ll1.radlon_,
            ll2.radlat_, ll2.radlon_, npts, true);

        return radpts;
    }

    // Clip filled poly section along the hemisphere edge.  Add the
    // closed poly coordinates to the return ArrayList.  This is called
    // from generateFilledPoly().
    private void hemisphereClip(
            int[] xs, int[] ys,
            AzimuthVar oiAz, AzimuthVar ioAz,
            ArrayList ret_vec)
    {
        float[] radpts = getHemisphereEdge(
                oiAz.current_azimuth, ioAz.current_azimuth);
        int len = radpts.length;
        int m = len>>>1;
        int off1 = oiAz.index;
        int off2 = ioAz.index;
        int l = off2-off1;

        int[] x_ = new int[l+m];
        int[] y_ = new int[l+m];
        System.arraycopy(xs, off1, x_, 0, l);
        System.arraycopy(ys, off1, y_, 0, l);

        // add the hemisphere edge points to the list
        Point temp = new Point();
        for (int i=l, j=0; j<len; i++, j+=2) {
            _forward(radpts[j], radpts[j+1], temp, null);
            x_[i] = temp.x;
            y_[i] = temp.y;
        }
        ret_vec.add(x_);
        ret_vec.add(y_);
    }


    /**
     * Forward project a raw array of radian points.
     * This assumes nothing about the array of coordinates.  In no way does it
     * assume the points are connected or that the composite figure is to be
     * filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * <p>
     * @param rawllpts array of lat,lon,... in radians
     * @param rawoff offset into rawllpts
     * @param xcoords x coordinates
     * @param ycoords y coordinates
     * @param visible coordinates visible?
     * @param copyoff offset into x,y,visible arrays
     * @param copylen number of coordinates (coordinate arrays should be at
     * least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     * visible.
     */
    public boolean forwardRaw(
            float[] rawllpts, int rawoff,
            int[] xcoords, int[] ycoords, boolean[] visible,
            int copyoff, int copylen)
    {
        Point temp = new Point();
        AzimuthVar azVar = new AzimuthVar();
        boolean ok = true;
        int end = copylen+copyoff;
        for (int i=copyoff, j=rawoff; i<end; i++, j+=2) {
            _forward(rawllpts[j], rawllpts[j+1], temp, azVar);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;
            ok = !azVar.invalid_forward;
            visible[i] = ok;
        }
        return ok;
    }


    // print out polygon
//    private static final void dumpPoly (float[] rawllpts) {
//      Debug.output("poly:");
//      for (int i=0; i<rawllpts.length; i+=2) {
//          System.out.print("["+
//                  ProjMath.radToDeg(rawllpts[i])+","+
//                  ProjMath.radToDeg(rawllpts[i+1])+"] ");
//      }
//      Debug.output("");
//    }


    /**
     * Draw the background for the projection.
     * @param g Graphics2D
     * @param paint java.awt.Paint to use for the background
     */
    public void drawBackground(Graphics2D g, java.awt.Paint paint) {
        g.setPaint(paint);
        drawBackground(g);
    }

    /**
     * Assume that the Graphics has been set with the Paint/Color
     * needed, just render the shape of the background.
     */
    public void drawBackground(Graphics g) {
        // if we're zoomed in, just draw background
        if (scale <= 20000000f) {
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Paint oldPaint = null;
        if (g instanceof Graphics2D) {
            oldPaint = ((Graphics2D)g).getPaint();
        } else {
            oldPaint = g.getColor();
        }

        // space... the final frontier
        g.setColor(spaceColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw the background color as a circle
        int s = world.x;
        if (g instanceof Graphics2D) {
            ((Graphics2D)g).setPaint(oldPaint);
        } else {
            g.setColor((Color)oldPaint);
        }

        g.fillArc(
                width/2 -s/2, height/2 -s/2,
                s/*-1*/, s/*-1*/,
                0, 360);
    }

    protected static Color spaceColor = Color.black;

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return "Azimuth";
    }

}
