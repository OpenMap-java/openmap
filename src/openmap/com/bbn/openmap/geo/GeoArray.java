//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: GeoArray.java,v $
//$Revision: 1.2 $
//$Date: 2007/02/13 20:02:10 $
//$Author: dietrick $
//
//**********************************************************************
package com.bbn.openmap.geo;

import com.bbn.openmap.MoreMath;

/**
 * A GeoArray is a interface that represents a set of Geo information. Rather
 * than keeping a set of Geo[] around and managing the memory for all of those
 * objects, the GeoArray provides an object that just holds onto the coordinates
 * of those points.
 * 
 * @author dietrick
 */
public interface GeoArray {

   /**
    * Get a Geo represented by the index i.
    * 
    * @param i
    * @return Geo at index i
    */
   Geo get(int i);

   /**
    * Load the values for Geo at index i into ret.
    * 
    * @param i
    * @param ret
    * @return ret filled in with values at index i
    */
   Geo get(int i, Geo ret);

   /**
    * Get the number of Geo points represented by this array.
    * 
    * @return number of geo points
    */
   int getSize();

   /**
    * Convert the GeoArray to an array of Geos.
    * 
    * @return Geo array from values
    */
   Geo[] toPointArray();

   /**
    * Convert the GeoArray to an array of decimal degree values, alternating
    * lat, lon, lat, lon.
    * 
    * @return lat,lon array representing decimal degrees of points.
    */
   double[] toLLDegrees();

   /**
    * Convert the GeoArray to an array of radian values, alternating lat, lon,
    * lat, lon.
    * 
    * @return lat, lon array representing radians for points.
    */
   double[] toLLRadians();

   /**
    * @param index the index of the Geo in the GeoArray to compare.
    * @param comp the Geo to compare to the indexed value.
    * @return true of x, y, and z of the Geos match.
    */
   boolean equals(int index, Geo comp);

   /**
    * Returns the perpendicular distance to the closest point on the edge of the
    * polygon.
    * 
    * @param geo the point to test against the poly
    * @param closestPoint will be filled with location of poly edge point closest to geo.
    * @return the distance in radians
    */
   double distance(Geo geo, Geo closestPoint);

   /**
    * Compute the area of the GeoArray polygon on the surface of a unit sphere
    * given an enumeration of its point. For a non unit sphere, multiply this by
    * the radius of sphere squared.
    * 
    * @return area value.
    */
   double area();

   /**
    * Ensure that the Geo array starts and ends with the same values. Will
    * replace the current coord array with one three floats longer if needed.
    */
   void closeArray();

   /**
    * Modify, if needed, the Geo array with the duplicates removed.
    */
   void removeDups();

   /**
    * A Mutable GeoArray is one where the points can be modified.
    * 
    * @author dietrick
    */
   public static interface Mutable
         extends GeoArray {

      /**
       * Set the values for the provided index to the values represented by g.
       * 
       * @param i
       * @param g
       */
      void set(int i, Geo g);

      /**
       * Set the values for the provided index to the values x, y, z, which are
       * vector Geo values, *not* lat, lon and height.
       * 
       * @param i
       * @param x
       * @param y
       * @param z
       */
      void set(int i, double x, double y, double z);

      /**
       * Set the values for the provided index to the latitude, longitude.
       * 
       * @param i
       * @param lat
       * @param lon
       * @param isDegrees true if lat/lon in decimal degrees.
       */
      void set(int i, double lat, double lon, boolean isDegrees);
   }

   /**
    * An abstract parent implementation class of GeoArray that handles common
    * methods.
    * 
    * @author dietrick
    */
   public static abstract class Adapter
         implements GeoArray {

      /**
       * Convert the GeoArray to an array of Geos.
       * 
       * @return Geo[]
       */
      public Geo[] toPointArray() {
         int size = getSize();
         Geo[] geos = new Geo[size];
         for (int i = 0; i < size; i++) {
            geos[i] = get(i, new Geo());
         }
         return geos;
      }

      /**
       * Convert the GeoArray to an array of decimal degree values, alternating
       * lat, lon, lat, lon.
       * 
       * @return lat/lon double[] of decimal degrees
       */
      public double[] toLLDegrees() {
         int size = getSize();
         double[] coords = new double[size * 2];
         Geo storage = new Geo();
         for (int i = 0; i < size; i++) {
            get(i, storage);
            int loc = i * 2;
            coords[loc] = storage.getLatitude();
            coords[loc + 1] = storage.getLongitude();
         }
         return coords;
      }

      /**
       * Convert the GeoArray to an array of radian values, alternating lat,
       * lon, lat, lon.
       * 
       * @return lat/lon double[] of radians
       */
      public double[] toLLRadians() {
         int size = getSize();
         double[] coords = new double[size * 2];
         Geo storage = new Geo();
         for (int i = 0; i < size; i++) {
            get(i, storage);
            int loc = i * 2;
            coords[loc] = storage.getLatitudeRadians();
            coords[loc + 1] = storage.getLongitudeRadians();
         }
         return coords;
      }

      /**
       * Computes the area of a polygon on the surface of a unit sphere. For a
       * non unit sphere, multiply this by the radius of sphere squared. The
       * value might be negative based on the counter-clockwise order of the
       * coordinates, but the absolute value is valid. This method will test for
       * closed polygon coordinates and handle that situation.
       */
      public double area() {
         int count = 0;
         double area = 0;
         Geo v0 = get(0, new Geo());
         Geo v1 = get(1, new Geo());
         Geo p0 = new Geo(v0);
         Geo p1 = new Geo(v1);
         Geo p2 = get(getSize() - 1, new Geo());
         // Having the first and last points the same messes up the
         // algorithm.
         // So skip the last point if it equals the first.
         boolean closed = p0.equals(p2);
         int size = getSize() - (closed ? 1 : 0);
         for (int i = 2; i < size; i++) {
            count++;
            get(i, p2);
            area += Geo.angle(p0, p1, p2);
            p0.initialize(p1);
            p1.initialize(p2);
         }

         count++;
         p2.initialize(v0);
         area += Geo.angle(p0, p1, p2);
         p0.initialize(p1);
         p1.initialize(p2);

         count++;
         p2.initialize(v1);
         area += Geo.angle(p0, p1, p2);

         return area - ((count - 2) * Math.PI);
      }

      /**
       * Returns the perpendicular distance to the closest point on the edge of
       * the polygon.
       * 
       * @param pnt the point to test against the poly
       * @param closestPoint if not null, will be set with the location of the
       *        point on the poly closest to pnt, you can read this object after
       *        this method call to get coordinates.
       * @return the distance in radians, or Double.POSITIVE_INFINITY if
       *         something weird happens.
       */
      public double distance(Geo pnt, Geo closestPoint) {
         double ret = java.lang.Double.POSITIVE_INFINITY;
         double testDist = ret;
         int size = getSize();
         Geo p0 = get(0, new Geo());
         Geo p1 = new Geo();
         Geo intersect = new Geo();

         for (int i = 1; i < size; i++) {
            get(i, p1);

            // Don't want to do distance test if end points are the same.
            if (p0.equals(p1)) {
               continue;
            }

            // The test needs to check two things - the distance between pnt and
            // the great circle line between p1 and p2. It should also check to
            // make sure that the perpendicular line intersects that great
            // circle line between p0 and p1. We'll calculate the distance
            // first, and then if the distance is the shortest seen so far,
            // we'll test the distance between that intersection point and make
            // sure that it's less than the distance between the two points.

            testDist = Intersection.pointCircleDistance(p0, p1, pnt);
            System.out.println("testing " + p0 + ", " + p1 + ", getting distance of " + testDist);

            if (testDist < ret) {
               // Find the point where the perpendicular line intersects the
               // great circle
               intersect = p0.intersect(p1, pnt, intersect);
               System.out.println("candidate received, gc intersected at " + intersect);

               if (Intersection.isOnSegment(p0, p1, intersect)) {
                  // Shortest distance, and between points
                  ret = testDist;
                  if (closestPoint != null) {
                     closestPoint.initialize(intersect);
                  }
               }
            }

            // Move to next point in array.
            p0.initialize(p1);
         }

         return ret;
      }
   }

   /**
    * An implementation of GeoArray and GeoArray.Mutable that contains
    * float-precision values. Holds the coordinates in a float array of x, y, z,
    * x, y, z values.
    * 
    * @author dietrick
    */
   public static class Float
         extends Adapter
         implements Mutable {

      private float[] coords;

      public Float(Geo[] geos) {
         coords = new float[geos.length * 3];
         for (int i = 0; i < geos.length; i++) {
            int loc = i * 3;
            Geo geo = geos[i];
            coords[loc] = (float) geo.x();
            coords[loc + 1] = (float) geo.y();
            coords[loc + 2] = (float) geo.z();
         }
      }

      public Float(GeoArray ga) {
         int size = ga.getSize();
         coords = new float[size * 3];
         Geo geo = new Geo();
         for (int i = 0; i < size; i++) {
            int loc = i * 3;
            ga.get(i, geo);
            coords[loc] = (float) geo.x();
            coords[loc + 1] = (float) geo.y();
            coords[loc + 2] = (float) geo.z();
         }
      }

      protected Float(float[] coords) {
         this.coords = coords;
      }

      public static Float createFromLatLonDegrees(float[] latlondeg) {
         int numCoordSets = latlondeg.length / 2;
         float[] coords = new float[numCoordSets * 3];
         Geo geo = new Geo();
         for (int i = 0; i < numCoordSets; i++) {
            geo.initialize(latlondeg[i * 2], latlondeg[i * 2 + 1]);
            int loc = i * 3;
            coords[loc] = (float) geo.x();
            coords[loc + 1] = (float) geo.y();
            coords[loc + 2] = (float) geo.z();
         }

         return new Float(coords);
      }

      public static Float createFromLatLonDegrees(double[] latlondeg) {
         int numCoordSets = latlondeg.length / 2;
         float[] coords = new float[numCoordSets * 3];
         Geo geo = new Geo();
         for (int i = 0; i < numCoordSets; i++) {
            geo.initialize(latlondeg[i * 2], latlondeg[i * 2 + 1]);
            int loc = i * 3;
            coords[loc] = (float) geo.x();
            coords[loc + 1] = (float) geo.y();
            coords[loc + 2] = (float) geo.z();
         }

         return new Float(coords);
      }

      public static Float createFromLatLonRadians(float[] latlonrad) {
         int numCoordSets = latlonrad.length / 2;
         float[] coords = new float[numCoordSets * 3];
         Geo geo = new Geo();
         for (int i = 0; i < numCoordSets; i++) {
            geo.initializeRadians(latlonrad[i * 2], latlonrad[i * 2 + 1]);
            int loc = i * 3;
            coords[loc] = (float) geo.x();
            coords[loc + 1] = (float) geo.y();
            coords[loc + 2] = (float) geo.z();
         }

         return new Float(coords);
      }

      public static Float createFromLatLonRadians(double[] latlonrad) {
         int numCoordSets = latlonrad.length / 2;
         float[] coords = new float[numCoordSets * 3];
         Geo geo = new Geo();
         for (int i = 0; i < numCoordSets; i++) {
            geo.initializeRadians(latlonrad[i * 2], latlonrad[i * 2 + 1]);
            int loc = i * 3;
            coords[loc] = (float) geo.x();
            coords[loc + 1] = (float) geo.y();
            coords[loc + 2] = (float) geo.z();
         }

         return new Float(coords);
      }

      public static Float createFromGeoCoords(float[] xyz) {
         return new Float(xyz);
      }

      public int getSize() {
         if (coords != null) {
            return coords.length / 3;
         }

         return 0;
      }

      public void set(int i, double x, double y, double z) {
         int loc = i * 3;
         coords[loc] = (float) x;
         coords[loc + 1] = (float) y;
         coords[loc + 2] = (float) z;
      }

      public void set(int i, Geo g) {
         set(i, g.x(), g.y(), g.z());
      }

      public void set(int i, double lat, double lon, boolean isDegrees) {
         set(i, new Geo(lat, lon, isDegrees));
      }

      public Geo get(int i) {
         return get(i, new Geo());
      }

      public Geo get(int i, Geo ret) {
         int loc = i * 3;
         double x = coords[loc];
         double y = coords[loc + 1];
         double z = coords[loc + 2];
         ret.initialize(x, y, z);
         return ret;
      }

      public boolean equals(int index, Geo comp) {
         int loc = index * 3;
         double x = coords[loc];
         double y = coords[loc + 1];
         double z = coords[loc + 2];
         return x == comp.x() && y == comp.y() && z == comp.z();
      }

      /**
       * Ensure that the Geo array starts and ends with the same values. Will
       * replace the current coord array with one three floats longer if needed.
       */
      public void closeArray() {
         int l = coords.length;
         int i = l - 3;
         if (coords[0] != coords[i] || coords[1] != coords[i + 1] || coords[2] != coords[i + 2]) {
            float[] newCoords = new float[l + 3];
            System.arraycopy(coords, 0, newCoords, 0, l);
            newCoords[l] = coords[0];
            newCoords[l + 1] = coords[1];
            newCoords[l + 2] = coords[2];
            coords = newCoords;
         }
      }

      /**
       * Modify, if needed, the Geo array with the duplicates removed.
       */
      public void removeDups() {
         Geo[] ga = toPointArray();
         Geo[] r = new Geo[ga.length];
         int p = 0;
         for (int i = 0; i < ga.length; i++) {
            if (p == 0 || !(r[p - 1].equals(ga[i]))) {
               r[p] = ga[i];
               p++;
            }
         }

         if (p != ga.length) {
            coords = new float[p * 3];
            for (int i = 0; i < p; i++) {
               int loc = i * 3;
               Geo geo = r[i];
               coords[loc] = (float) geo.x();
               coords[loc + 1] = (float) geo.y();
               coords[loc + 2] = (float) geo.z();
            }
         }
      }
   }

   /**
    * An implementation of GeoArray and GeoArray.Mutable that contains
    * double-precision values. Holds the coordinates in a double array of x, y,
    * z, x, y, z values.
    * 
    * @author dietrick
    */
   public static class Double
         extends Adapter
         implements Mutable {

      private double[] coords;

      public Double(Geo[] geos) {
         coords = new double[geos.length * 3];
         for (int i = 0; i < geos.length; i++) {
            int loc = i * 3;
            Geo geo = geos[i];
            coords[loc] = geo.x();
            coords[loc + 1] = geo.y();
            coords[loc + 2] = geo.z();
         }
      }

      public Double(GeoArray ga) {
         int size = ga.getSize();
         coords = new double[size * 3];
         Geo geo = new Geo();
         for (int i = 0; i < size; i++) {
            int loc = i * 3;
            ga.get(i, geo);
            coords[loc] = geo.x();
            coords[loc + 1] = geo.y();
            coords[loc + 2] = geo.z();
         }
      }

      protected Double(double[] coords) {
         this.coords = coords;
      }

      public static Double createFromLatLonDegrees(double[] latlondeg) {
         int numCoordSets = latlondeg.length / 2;
         double[] coords = new double[numCoordSets * 3];
         Geo geo = new Geo();
         for (int i = 0; i < numCoordSets; i++) {
            geo.initialize(latlondeg[i * 2], latlondeg[i * 2 + 1]);
            int loc = i * 3;
            coords[loc] = geo.x();
            coords[loc + 1] = geo.y();
            coords[loc + 2] = geo.z();
         }

         return new Double(coords);
      }

      public static Double createFromLatLonRadians(double[] latlonrad) {
         int numCoordSets = latlonrad.length / 2;
         double[] coords = new double[numCoordSets * 3];
         Geo geo = new Geo();
         for (int i = 0; i < numCoordSets; i++) {
            geo.initializeRadians(latlonrad[i * 2], latlonrad[i * 2 + 1]);
            int loc = i * 3;
            coords[loc] = geo.x();
            coords[loc + 1] = geo.y();
            coords[loc + 2] = geo.z();
         }

         return new Double(coords);
      }

      public static Double createFromGeoCoords(double[] xyz) {
         return new Double(xyz);
      }

      public int getSize() {
         if (coords != null) {
            return coords.length / 3;
         }

         return 0;
      }

      public void set(int i, double x, double y, double z) {
         int loc = i * 3;
         coords[loc] = x;
         coords[loc + 1] = y;
         coords[loc + 2] = z;
      }

      public void set(int i, Geo g) {
         set(i, g.x(), g.y(), g.z());
      }

      public void set(int i, double lat, double lon, boolean isDegrees) {
         set(i, new Geo(lat, lon, isDegrees));
      }

      public Geo get(int i) {
         return get(i, new Geo());
      }

      public Geo get(int i, Geo ret) {

         if (ret == null) {
            ret = new Geo();
         }

         int loc = i * 3;
         double x = coords[loc];
         double y = coords[loc + 1];
         double z = coords[loc + 2];
         ret.initialize(x, y, z);
         return ret;
      }

      public boolean equals(int index, Geo comp) {
         int loc = index * 3;
         double x = coords[loc];
         double y = coords[loc + 1];
         double z = coords[loc + 2];
         return x == comp.x() && y == comp.y() && z == comp.z();
      }

      /**
       * Ensure that the Geo array starts and ends with the same values. Will
       * replace the current coord array with one three double longer if needed.
       */
      public void closeArray() {
         int l = coords.length;
         int i = l - 3;
         if (coords[0] != coords[i] || coords[1] != coords[i + 1] || coords[2] != coords[i + 2]) {
            double[] newCoords = new double[l + 3];
            System.arraycopy(coords, 0, newCoords, 0, l);
            newCoords[l] = coords[0];
            newCoords[l + 1] = coords[1];
            newCoords[l + 2] = coords[2];
            coords = newCoords;
         }
      }

      /**
       * Modify, if needed, the Geo array with the duplicates removed.
       */
      public void removeDups() {
         Geo[] ga = toPointArray();
         Geo[] r = new Geo[ga.length];
         int p = 0;
         for (int i = 0; i < ga.length; i++) {
            if (p == 0 || !(r[p - 1].equals(ga[i]))) {
               r[p] = ga[i];
               p++;
            }
         }

         if (p != ga.length) {
            coords = new double[p * 3];
            for (int i = 0; i < p; i++) {
               int loc = i * 3;
               Geo geo = r[i];
               coords[loc] = geo.x();
               coords[loc + 1] = geo.y();
               coords[loc + 2] = geo.z();
            }
         }
      }
   }
}
