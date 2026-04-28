/**
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        A Verizon Company
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2001, 2002 All Rights Reserved
 *
 */

package com.bbn.openmap.geo;

/**
 * Defines a 3-D rotation, M, such that the matrix multiplication, Mv, rotates
 * vector v an angle ,angle, counter clockwise about the Geo, g. See Newman and
 * Sproull, 1973, Principles of Interactive Computer Garaphics, McGraw-Hill, New
 * York, 463-465.
 */
public class Rotation {

    protected Geo g;
    double m00, m11, m22, m01, m10, m12, m21, m02, m20;

    public Rotation(Geo g, double angle) {
        this.g = g;
        this.setAngle(angle);
    }

    private void setAngle(double angle) {
        double x = g.x();
        double y = g.y();
        double z = g.z();
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double b = 1.0 - c;
        double bx = b * x;
        double by = b * y;
        double bz = b * z;
        double bxx = bx * x;
        double bxy = bx * y;
        double bxz = bx * z;
        double byy = by * y;
        double byz = by * z;
        double bzz = bz * z;
        double sx = s * x;
        double sy = s * y;
        double sz = s * z;
        m00 = c + bxx;
        m11 = c + byy;
        m22 = c + bzz;
        m01 = (-sz) + bxy;
        m10 = sz + bxy;
        m12 = (-sx) + byz;
        m21 = sx + byz;
        m02 = sy + bxz;
        m20 = (-sy) + bxz;

        /**
         * System.out.println (" Rotation " + m00 + " " + m11 + " " + m22 + "\n" +
         * m01 + " " + m10 + " " + m12 + "\n" + m21 + " " + m02 + " " + m20);
         */
    }

    public Geo rotate(Geo v) {
        double x = v.x(), y = v.y(), z = v.z();
        return new Geo(m00 * x + m01 * y + m02 * z, m10 * x + m11 * y + m12 * z, m20
                * x + m21 * y + m22 * z);
    }

    /**
     * Static method that does what creating a Rotation object can calling
     * rotate() on it does. Rotates vector v2 an angle counter clockwise about
     * the Geo, v1.
     * 
     * @param v1
     * @param angle
     * @param v2
     * @param ret The Geo to load the results in, may be null which will cause
     *        the method to allocate a new Geo object.
     * @return the ret Geo passed in, or a new one if ret was null.
     */
    public final static Geo rotate(Geo v1, double angle, Geo v2, Geo ret) {
        double x = v1.x();
        double y = v1.y();
        double z = v1.z();
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double b = 1.0 - c;
        double bx = b * x;
        double by = b * y;
        double bz = b * z;
        double bxx = bx * x;
        double bxy = bx * y;
        double bxz = bx * z;
        double byy = by * y;
        double byz = by * z;
        double bzz = bz * z;
        double sx = s * x;
        double sy = s * y;
        double sz = s * z;
        double m00 = c + bxx;
        double m11 = c + byy;
        double m22 = c + bzz;
        double m01 = (-sz) + bxy;
        double m10 = sz + bxy;
        double m12 = (-sx) + byz;
        double m21 = sx + byz;
        double m02 = sy + bxz;
        double m20 = (-sy) + bxz;

        /**
         * System.out.println (" Rotation " + m00 + " " + m11 + " " + m22 + "\n" +
         * m01 + " " + m10 + " " + m12 + "\n" + m21 + " " + m02 + " " + m20);
         */
        double x2 = v2.x();
        double y2 = v2.y();
        double z2 = v2.z();

        if (ret == null) {
            return new Geo(m00 * x2 + m01 * y2 + m02 * z2, m10 * x2 + m11 * y2
                    + m12 * z2, m20 * x2 + m21 * y2 + m22 * z2);
        }

        ret.initialize(m00 * x2 + m01 * y2 + m02 * z2, m10 * x2 + m11 * y2
                + m12 * z2, m20 * x2 + m21 * y2 + m22 * z2);

        return ret;
    }
}
