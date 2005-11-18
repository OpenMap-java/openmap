/*
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

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * A class that represents a point on the Earth as a three dimensional
 * unit length vector, rather than latitude and longitude. For the
 * theory and an efficient implementation using partial evaluation
 * see:
 * http://openmap.bbn.com/~kanderso/lisp/performing-lisp/essence.ps
 * 
 * This implementation matches the theory carefully, but does not use
 * partial evaluation.
 * 
 * <p>
 * For the area calculation see: http://math.rice.edu/~pcmi/sphere/
 * 
 * @author Ken Anderson
 * @author Sachin Date
 * @author Ben Lubin
 * @author Michael Thome
 * @version $Revision: 1.20 $ on $Date: 2005/11/18 19:39:35 $
 */
public class Geo {

    /* ****************************************************************
     * Constants for the shape of the earth. see
     * http://www.gfy.ku.dk/%7Eiag/HB2000/part4/groten.htm
     *************************************************************** */
    // Replaced by Length constants.
    //     public static final double radiusKM = 6378.13662; // in KM.
    //     public static final double radiusNM = 3443.9182; // in NM.
    // Replaced with WGS 84 constants
    //     public static final double flattening = 1.0/298.25642;
    public static final double flattening = 1.0 / 298.257223563;
    public static final double FLATTENING_C = (1.0 - flattening) * (1.0 - flattening);
    
    public static final double METERS_PER_NM = 1852;
    private static final double NPD_LTERM1 = 111412.84 / METERS_PER_NM;
    private static final double NPD_LTERM2 = -93.5 / METERS_PER_NM;
    private static final double NPD_LTERM3 = 0.118 / METERS_PER_NM;
    
    private double x;
    private double y;
    private double z;


    /**
     * Compute nautical miles per degree at a specified latitude (in
     * degrees). Calculation from NIMA:
     * http://pollux.nss.nima.mil/calc/degree.html
     */
    public final static double npdAtLat(double latdeg) {
        double lat = (latdeg * Math.PI) / 180.0;
        return ( NPD_LTERM1 * Math.cos(lat) + 
                 NPD_LTERM2 * Math.cos(3 * lat) +
                 NPD_LTERM3 * Math.cos(5 * lat) );
    }

    /** Convert from geographic to geocentric latitude (radians) */
    public static double geocentricLatitude(double geographicLatitude) {
        return Math.atan((Math.tan(geographicLatitude) * FLATTENING_C));
    }

    /** Convert from geocentric to geographic latitude (radians) */
    public static double geographicLatitude(double geocentricLatitude) {
        return Math.atan(Math.tan(geocentricLatitude) / FLATTENING_C);
    }

    /** Convert from degrees to radians. */
    public static double radians(double degrees) {
        return Length.DECIMAL_DEGREE.toRadians(degrees);
    }

    /** Convert from radians to degrees. */
    public static double degrees(double radians) {
        return Length.DECIMAL_DEGREE.fromRadians(radians);
    }

    /** Convert radians to kilometers. * */
    public static double km(double radians) {
        return Length.KM.fromRadians(radians);
    }

    /** Convert kilometers to radians. * */
    public static double kmToAngle(double km) {
        return Length.KM.toRadians(km);
    }

    /** Convert radians to nauticalMiles. * */
    public static double nm(double radians) {
        return Length.NM.fromRadians(radians);
    }

    /** Convert nautical miles to radians. * */
    public static double nmToAngle(double nm) {
        return Length.NM.toRadians(nm);
    }

    /**
     * Construct a Geo from its latitude and longitude.
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     */
    public Geo(double lat, double lon) {
        initialize(lat, lon);
    }

    /**
     * Construct a Geo from its latitude and longitude.
     * 
     * @param lat latitude.
     * @param lon longitude.
     * @param isDegrees should be true if the lat/lon are specified in
     *        decimal degrees, false if they are radians.
     */
    public Geo(float lat, float lon, boolean isDegrees) {
        if (isDegrees) {
            initialize(lat, lon);
        } else {
            initializeRadians(lat, lon);
        }
    }

    /** Construct a Geo from its parts. */
    public Geo(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Construct a Geo from another Geo. */
    public Geo(Geo geo) {
        this(geo.x, geo.y, geo.z);
    }
    
    public static final Geo makeGeoRadians(double latr, double lonr) {
      double rlat = geocentricLatitude(latr);
      double c = Math.cos(rlat);
      return new Geo(c * Math.cos(lonr),
                     c * Math.sin(lonr),
                     Math.sin(rlat)); 
    }
    public static final Geo makeGeoDegrees(double latd, double lond) {
      return makeGeoRadians(radians(latd), radians(lond));
    }
    
    public static final Geo makeGeo(double x, double y, double z) {
      return new Geo(x, y, z);
    }
    
    public static final Geo makeGeo(Geo p) {
      return new Geo(p.x, p.y, p.z);
    }

    /**
     * Initialize this Geo to match another.
     * 
     * @param g
     */
    public void initialize(Geo g) {
        x = g.x;
        y = g.y;
        z = g.z;
    }

    /**
     * Initialize this Geo with to represent coordinates.
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     */
    public void initialize(double lat, double lon) {
        initializeRadians(radians(lat), radians(lon));
    }
    
    /**
     * Initialize this Geo with to represent coordinates.
     * 
     * @param lat latitude in radians.
     * @param lon longitude in radians.
     */
    public void initializeRadians(double lat, double lon) {
        double rlat = geocentricLatitude(lat);
        double c = Math.cos(rlat);
        x = c * Math.cos(lon);
        y = c * Math.sin(lon);
        z = Math.sin(rlat);
    }

    /**
     * Find the midpoint Geo between this one and another on a Great
     * Circle line between the two. The result is undefined of the two
     * points are antipodes.
     * 
     * @param g2
     * @return midpoint Geo.
     */
    public Geo midPoint(Geo g2) {
        return add(g2).normalize();
    }

    public Geo interpolate(Geo g2, double x) {
        return scale(x).add(g2.scale(1 - x)).normalize();
    }

    public String toString() {
        return "Geo[" + getLatitude() + "," + getLongitude() + "]";
    }

    public double getLatitude() {
        return degrees(geographicLatitude(Math.atan2(z,
                Math.sqrt(x * x + y * y))));
    }

    public double getLongitude() {
        return degrees(Math.atan2(y, x));
    }

    public double getLatitudeRadians() {
        return geographicLatitude(Math.atan2(z, Math.sqrt(x * x + y * y)));
    }

    public double getLongitudeRadians() {
        return Math.atan2(y, x);
    }

    // Readers
    public final double x() {
        return this.x;
    }

    public final double y() {
        return this.y;
    }

    public final double z() {
        return this.z;
    }

    /** North pole. */
    public static final Geo north = new Geo(0.0, 0.0, 1.0);

    /** Dot product. */
    public double dot(Geo b) {
        return (this.x() * b.x() + this.y() * b.y() + this.z() * b.z());
    }

    /** Dot product. */
    public static double dot(Geo a, Geo b) {
        return (a.x() * b.x() + a.y() * b.y() + a.z() * b.z());
    }

    /** Euclidian length. */
    public double length() {
        return Math.sqrt(this.dot(this));
    }

    /** Multiply this by s. * */
    public Geo scale(double s) {
        return new Geo(this.x() * s, this.y() * s, this.z() * s);
    }

    /** Returns a unit length vector parallel to this. */
    public Geo normalize() {
        return this.scale(1.0 / this.length());
    }

    /** Vector cross product. */
    public Geo cross(Geo b) {
        return new Geo(this.y() * b.z() - this.z() * b.y(), 
                       this.z() * b.x() - this.x() * b.z(),
                       this.x() * b.y() - this.y() * b.x());
    }

    /** Eqvivalent to this.cross(b).length(). */
    public double crossLength(Geo b) {
        double x = this.y() * b.z() - this.z() * b.y();
        double y = this.z() * b.x() - this.x() * b.z();
        double z = this.x() * b.y() - this.y() * b.x();
        return Math.sqrt(x * x + y * y + z * z);
    }

    /** Eqvivalent to <code>this.cross(b).normalize()</code>. */
    public Geo crossNormalize(Geo b) {
        double x = this.y() * b.z() - this.z() * b.y();
        double y = this.z() * b.x() - this.x() * b.z();
        double z = this.x() * b.y() - this.y() * b.x();
        double L = Math.sqrt(x * x + y * y + z * z);
        return new Geo(x / L, y / L, z / L);
    }

    /** Eqvivalent to <code>this.cross(b).normalize()</code>. */
    public static Geo crossNormalize(Geo a, Geo b) {
        return a.crossNormalize(b);
    }

    /** Returns this + b. */
    public Geo add(Geo b) {
        return new Geo(this.x() + b.x(), this.y() + b.y(), this.z() + b.z());
    }

    /** Returns this - b. */
    public Geo subtract(Geo b) {
        return new Geo(this.x() - b.x(), this.y() - b.y(), this.z() - b.z());
    }
    
    public boolean equals(Geo v2) {
        return this.x == v2.x  && this.y == v2.y && this.z == v2.z;
    }

    /** Angular distance, in radians between this and v2. */
    public double distance(Geo v2) {
        return Math.atan2(v2.crossLength(this), v2.dot(this));
    }

    /** Angular distance, in radians between v1 and v2. */
    public static double distance(Geo v1, Geo v2) {
        return v1.distance(v2);
    }

    /** Angular distance, in radians between the two lat lon points. */
    public static double distance(double lat1, double lon1, double lat2,
                                  double lon2) {
        return Geo.distance(new Geo(lat1, lon1), new Geo(lat2, lon2));
    }

    /** Distance in kilometers. * */
    public double distanceKM(Geo v2) {
        return km(distance(v2));
    }

    /** Distance in kilometers. * */
    public static double distanceKM(Geo v1, Geo v2) {
        return v1.distanceKM(v2);
    }

    /** Distance in kilometers. * */
    public static double distanceKM(double lat1, double lon1, double lat2,
                                    double lon2) {
        return Geo.distanceKM(new Geo(lat1, lon1), new Geo(lat2, lon2));
    }

    /** Distance in nautical miles. * */
    public double distanceNM(Geo v2) {
        return nm(distance(v2));
    }

    /** Distance in nautical miles. * */
    public static double distanceNM(Geo v1, Geo v2) {
        return v1.distanceNM(v2);
    }

    /** Distance in nautical miles. * */
    public static double distanceNM(double lat1, double lon1, double lat2,
                                    double lon2) {
        return Geo.distanceNM(new Geo(lat1, lon1), new Geo(lat2, lon2));
    }

    /** Azimuth in radians from this to v2. */
    public double azimuth(Geo v2) {
        /*
         * n1 is the great circle representing the meridian of this.
         * n2 is the great circle between this and v2. The azimuth is
         * the angle between them but we specialized the cross
         * product.
         */
        Geo n1 = north.cross(this);
        Geo n2 = v2.cross(this);
        double az = Math.atan2(-north.dot(n2), n1.dot(n2));
        return (az > 0.0) ? az : 2.0 * Math.PI + az;
    }

    /**
     * Given 3 points on a sphere, p0, p1, p2, return the angle
     * between them in radians.
     */
    public static double angle(Geo p0, Geo p1, Geo p2) {
        return Math.PI - p0.cross(p1).distance(p1.cross(p2));
    }
    
    /**
     * Computes the area of a polygon on the surface of a unit sphere
     * given an enumeration of its point.. For a non unit sphere,
     * multiply this by the radius of sphere squared.
     */
    public static double area(Enumeration vs) {
        int count = 0;
        double area = 0;
        Geo v0 = (Geo) vs.nextElement();
        Geo v1 = (Geo) vs.nextElement();
        Geo p0 = v0;
        Geo p1 = v1;
        Geo p2 = null;
        while (vs.hasMoreElements()) {
            count = count + 1;
            p2 = (Geo) vs.nextElement();
            area = area + angle(p0, p1, p2);
            p0 = p1;
            p1 = p2;
        }

        count = count + 1;
        p2 = v0;
        area = area + angle(p0, p1, p2);
        p0 = p1;
        p1 = p2;

        count = count + 1;
        p2 = v1;
        area = area + angle(p0, p1, p2);

        return area - (count - 2) * Math.PI;
    }

    /**
     * Is the point, p, within radius radians of the great circle
     * segment between this and v2?
     */
    public boolean isInside(Geo v2, double radius, Geo p) {
        /*
         * gc is a unit vector perpendicular to the plane defined by
         * v1 and v2
         */
        Geo gc = this.crossNormalize(v2);

        /*
         * |gc . p| is the size of the projection of p onto gc (the
         * normal of v1,v2) cos(pi/2-r) is effectively the size of the
         * projection of a vector along gc of the radius length. If
         * the former is larger than the latter, than p is further
         * than radius from arc, so must not be isInside
         */
        if (Math.abs(gc.dot(p)) > Math.cos((Math.PI / 2.0) - radius))
            return false;

        /*
         * If p is within radius of either endpoint, then we know it
         * isInside
         */
        if (this.distance(p) <= radius || v2.distance(p) <= radius)
            return true;

        /* d is the vector from the v2 to v1 */
        Geo d = v2.subtract(this);

        /* L is the length of the vector d */
        double L = d.length();

        /* n is the d normalized to length=1 */
        Geo n = d.normalize();

        /* dp is the vector from p to v1 */
        Geo dp = p.subtract(this);

        /* size is the size of the projection of dp onto n */
        double size = n.dot(dp);

        /* p is inside iff size>=0 and size <= L */
        return (0 <= size && size <= L);
    }

    /**
     * do the segments v1-v2 and p1-p2 come within radius (radians) of
     * each other?
     */
    public static boolean isInside(Geo v1, Geo v2, double radius, Geo p1, Geo p2) {
        return v1.isInside(v2, radius, p1) || v1.isInside(v2, radius, p2)
                || p1.isInside(p2, radius, v1) || p1.isInside(p2, radius, v2);
    }

    /**
     * Static version of isInside uses conventional (decimal degree)
     * coordinates.
     */
    public static boolean isInside(double lat1, double lon1, double lat2,
                                   double lon2, double radius, double lat3,
                                   double lon3) {
        return (new Geo(lat1, lon1)).isInside(new Geo(lat2, lon2),
                radius,
                new Geo(lat3, lon3));
    }

    /**
     * Is Geo p inside the time bubble along the great circle segment
     * from this to v2 looking forward forwardRadius and backward
     * backwardRadius.
     */
    public boolean inBubble(Geo v2, double forwardRadius, double backRadius,
                            Geo p) {
        return distance(p) <= ((v2.subtract(this)
                .normalize()
                .dot(p.subtract(this)) > 0.0) ? forwardRadius : backRadius);
    }

    /** Returns the point opposite this point on the earth. */
    public Geo antipode() {
        return this.scale(-1.0);
    }

    /**
     * Find the intersection of the great circle between this and q
     * and the great circle normal to r.
     * <p>
     * 
     * That is, find the point, y, lying between this and q such that
     * 
     * <pre>
     * 
     *              y = [x*this + (1-x)*q]*c
     *              where c = 1/y.dot(y) is a factor for normalizing y.
     *              y.dot(r) = 0
     *              substituting:
     *              [x*this + (1-x)*q]*c.dot(r) = 0 or
     *              [x*this + (1-x)*q].dot(r) = 0
     *              x*this.dot(r) + (1-x)*q.dot(r) = 0
     *              x*a + (1-x)*b = 0
     *              x = -b/(a - b)
     *           
     * </pre>
     * 
     * We assume that this and q are less than 180 degrees appart.
     * When this and q are 180 degrees appart, the point -y is also a
     * valid intersection.
     * <p>
     * Alternatively the intersection point, y, satisfies y.dot(r) = 0
     * y.dot(this.crossNormalize(q)) = 0 which is satisfied by y =
     * r.crossNormalize(this.crossNormalize(q));
     *  
     */
    public Geo intersect(Geo q, Geo r) {
        double a = this.dot(r);
        double b = q.dot(r);
        double x = -b / (a - b);
        return this.scale(x).add(q.scale(1.0 - x)).normalize();
    }
    
    /** alias for computeCorridor(path, radius, radians(10), true) **/
    public static Geo[] computeCorridor(Geo[] path, double radius) {
      return computeCorridor(path, radius, radians(10.0), true);
    }
    
    /**
     * Wrap a fixed-distance corridor around an (open) path, as specified by an array of Geo.
     * @param path Open path
     * @param radius Distance from path to widen corridor, in angular radians.
     * @param err maximum angle of rounded edges, in radians.  If 0, will directly cut outside bends.
     * @param capp iff true, will round end caps
     * @return a closed polygon representing the specified corridor around the path.
     *
     */
    public static Geo[] computeCorridor(Geo[] path, double radius, double err, boolean capp) {
      assert path!=null;
      assert radius > 0.0;
      
      int pl = path.length;
      if (pl<2) return null;
      
      // final polygon will be right[0],...,right[n],left[m],...,left[0]
      ArrayList right = new ArrayList((int) (pl*1.5));
      ArrayList left = new ArrayList((int) (pl*1.5));
      
      Geo g0 = null;            // previous point
      Geo n0 = null;            // previous normal vector
      Geo l0 = null;
      Geo r0 = null;
      
      Geo g1 = path[0];         // current point
      
      for (int i=1; i<pl; i++) {
        Geo g2 = path[i];       // next point
        Geo n1 = g1.crossNormalize(g2);   // n is perpendicular to the vector from g1 to g2
        n1 = n1.scale(radius);  // normalize to radius
        // these are the offsets on the g2 side at g1
        Geo r1b = g1.add(n1);
        Geo l1b = g1.subtract(n1);
        
        if (n0 == null) {
          if (capp && err>0) {
            // start cap
            Geo[] arc = approximateArc(g1,l1b,r1b,err);
            for (int j=arc.length-1;j>=0;j--) {
              right.add(arc[j]);
            }
          } else {
            // no previous point - we'll just be square
            right.add(l1b);
            left.add(r1b);
          }
          // advance normals
          l0 = l1b;
          r0 = r1b;
        } else {
          // otherwise, compute a more complex shape
          
          // these are the right and left on the g0 side of g1
          Geo r1a = g1.add(n0);
          Geo l1a = g1.subtract(n0);
          
          double handed = g0.cross(g1).dot(g2);  // right or left handed divergence
          if (handed>0) { // left needs two points, right needs 1
            if (err>0) {
              Geo[] arc = approximateArc(g1,l1b,l1a,err);
              for (int j=arc.length-1;j>=0;j--) {
                right.add(arc[j]);
              }
            } else {
              right.add(l1a);
              right.add(l1b);
            }
            l0 = l1b;
            
            Geo ip = Intersection.segmentsIntersect(r0, r1a, r1b, g2.add(n1));
            left.add(ip);
            r0 = ip;            
          } else {
            Geo ip = Intersection.segmentsIntersect(l0, l1a, l1b, g2.subtract(n1));
            right.add(ip);
            l0 = ip;
            if (err>0) {
              Geo[] arc = approximateArc(g1,r1a,r1b,err);
              for (int j=0;j<arc.length;j++) {
                left.add(arc[j]);
              }
            } else {
              left.add(r1a);
              left.add(r1b);
            }
            r0=r1b;
          }
        } 
        
        // advance points
        g0=g1;
        n0=n1;
        g1=g2;
      }
      
      // finish it off
      Geo rn = g1.subtract(n0);
      Geo ln = g1.add(n0);
      if (capp && err>0) {
        // end cap
        Geo[] arc = approximateArc(g1,ln,rn,err);
        for (int j=arc.length-1; j>=0; j--) {
          right.add(arc[j]);
        }
      } else {
        right.add(rn);
        left.add(ln);
      }
      
      
      int ll = right.size();
      int rl = left.size();
      Geo[] result = new Geo[ll+rl];
      for (int i=0;i<ll;i++) {
        result[i]=(Geo) right.get(i);
      }
      int j=ll;
      for (int i=rl-1; i>=0; i--) {
        result[j++]=(Geo) left.get(i);
      }
      return result;
    }
    
    /** simple vector angle (not geocentric!) */
    static double simpleAngle(Geo p1, Geo p2) {
      return Math.acos(p1.dot(p2)/(p1.length() * p2.length()));
    }
    
    /** compute a polygonal approximation of an arc centered at pc, beginning at
     * p0 and ending at p1, going clockwise and including the two end points.
     * @param pc center point
     * @param p0 starting point
     * @param p1 ending point
     * @param err The maximum angle between approximates allowed, in radians.  Smaller values will
     * look better but will result in more returned points.
     * @return
     */
    public static final Geo[] approximateArc(Geo pc, Geo p0, Geo p1, double err) {
      
      double theta = angle(p0,pc,p1);
      // if the rest of the code is undefined in this situation, just skip it.
      if (Double.isNaN(theta)) {
        return new Geo[] {p0,p1};
      }
      
      int n = (int) (2.0 + Math.abs(theta/err));  // number of points (counting the end points)
      Geo[] result = new Geo[n];
      result[0] = p0;
      double dtheta = theta/(n-1);
      
      double rho = 0.0; // angle starts at 0 (directly at p0)
      
     for (int i = 1; i < n-1; i++) {
        rho += dtheta;
        // Rotate p0 around this so it has the right azimuth.
        result[i] = (new Rotation(pc, 2.0*Math.PI - rho)).rotate(p0);
      }
      result[n-1] = p1;
      
      return result;
    }
    
    public final Geo[] approximateArc(Geo p0, Geo p1, double err) {
      return approximateArc(this, p0, p1, err);
    }
    
    /** @deprecated use </b>#offset(double, double) */
    public Geo geoAt(double distance, double azimuth) {
      return offset(distance, azimuth);
    }
    
    /** Returns a Geo that is distance (radians), and azimuth (radians)
     * away from this. 
     * @param distance distance of this to the target point in radians.
     * @param azimuth Direction of target point from this, in radians, clockwise from north.
     * @note this is undefined at the north pole, at which point "azimuth" is undefined.
     */
    public Geo offset(double distance, double azimuth) {
      // m is normal the the meridian through this.
      Geo m = this.crossNormalize(north);
      // p is a point on the meridian distance <tt>distance</tt> from this.
      Geo p = (new Rotation(m, distance)).rotate(this);
      // Rotate p around this so it has the right azimuth.
      return (new Rotation(this, 2.0*Math.PI - azimuth)).rotate(p);
    }
    
    public Geo offset(Geo origin, double distance, double azimuth) {
      return origin.offset(distance, azimuth);
    }
    
    /*
    //same as offset, except using trig instead of vector mathematics
    public Geo trig_offset(double distance, double azimuth) {
      double latr = getLatitudeRadians();
      double lonr = getLongitudeRadians();
      
      double coslat = Math.cos(latr);
      double sinlat = Math.sin(latr);
      double cosaz = Math.cos(azimuth);
      double sinaz = Math.sin(azimuth);
      double sind = Math.sin(distance);
      double cosd = Math.cos(distance);
      
      return makeGeoRadians(Math.asin(sinlat * cosd + coslat * sind * cosaz),
                            Math.atan2(sind * sinaz, coslat * cosd - sinlat * sind * cosaz) + lonr);
    }
    */
}
