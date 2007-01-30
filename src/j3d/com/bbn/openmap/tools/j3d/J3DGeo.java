//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: J3DGeo.java,v $
//$Revision: 1.2 $
//$Date: 2007/01/30 21:25:32 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.j3d;

import javax.vecmath.Point3d;

import com.bbn.openmap.geo.Geo;

/**
 * This class is basically a Geo class, with constructors and accessors that
 * know that J3D space is aligned differently. In J3D, the x axis increases to
 * the right, y axis increases up, and z increases out of the screen to the
 * viewer. In Geo, x is the same, y is positive into the screen, and z increases
 * up. J3DGeo deals with external components thinking of x, y, z in J3D space,
 * while internally representing things in normal Geo orientation.
 * 
 * @author dietrick
 */
public class J3DGeo extends Geo {
    
    public J3DGeo(double lat, double lon) {
        super(lat, lon);
    }

    public J3DGeo(double lat, double lon, boolean isDegrees) {
        super(lat, lon, isDegrees);
    }

    public J3DGeo(double lat, double lon, boolean isDegrees, double length) {
        super(lat, lon, isDegrees);
        setLength(length);
    }

    public J3DGeo(double x, double y, double z) {
        super(x, -z, y);
    }

    public J3DGeo(Geo geo) {
        super(geo);
    }

    public J3DGeo(Point3d pt) {
        this(pt.x, pt.y, pt.z);
    }

    public void initialize(double x, double y, double z) {
        super.initialize(x, -z, y);
    }

    /**
     * Reader for x in J3D axis representation (positive going to right of screen).
     * @return
     */
    public double getX() {
        return super.x();
    }
    
    /**
     * Reader for y in J3D axis representation (positive going to top of screen).
     * @return
     */
    public double getY() {
        return super.z();
    }

    /**
     * Reader for z in J3D axis representation (positive coming out of screen).
     * @return
     */
    public double getZ() {
        return -super.y();
    }
    
    /**
     * Create a J3DGeo with x, y, and z relative to Geo axis.
     * @param superX
     * @param superY
     * @param superZ
     * @return
     */
    protected J3DGeo create(double superX, double superY, double superZ) {
//        return new J3DGeo(new Geo(superX, superY, superZ));
        return new J3DGeo(superX, superZ, -superY);
    }

    /** Returns this + b. */
    public Geo add(Geo b) {
        return create(this.x() + b.x(), this.y() + b.y(), this.z() + b.z());
    }

    /** Returns this - b. */
    public Geo subtract(Geo b) {
        return create(this.x() - b.x(), this.y() - b.y(), this.z() - b.z());
    }
    
    /** Multiply this by s. * */
    public Geo scale(double s) {
        return create(this.x() * s, this.y() * s, this.z() * s);
    }
    
    /** Vector cross product. */
    public Geo cross(Geo b) {
        return create(this.y() * b.z() - this.z() * b.y(), this.z() * b.x()
                - this.x() * b.z(), this.x() * b.y() - this.y() * b.x());
    }
    
    /** Eqvivalent to <code>this.cross(b).normalize()</code>. */
    public Geo crossNormalize(Geo b) {
        double x = this.y() * b.z() - this.z() * b.y();
        double y = this.z() * b.x() - this.x() * b.z();
        double z = this.x() * b.y() - this.y() * b.x();
        double L = Math.sqrt(x * x + y * y + z * z);
        return create(x / L, y / L, z / L);
    }
    
    public Point3d getPoint3d() {
        return getPoint3d(new Point3d());
    }

    public Point3d getPoint3d(Point3d pt) {
        pt.set(getX(), getY(), getZ());
        return pt;
    }

}
