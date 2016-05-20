package com.bbn.openmap.geo;

import org.junit.Assert;
import org.junit.Test;

public class GeoTest {
    @Test
    public void testLatLonConversion2() {
      System.out.println("@Test - testLatLonConversion");

      double bosLat =   42.366978;
      double bosLon =  -71.022362;
      double sfoLat =   37.615223;
      double sfoLon = -122.389977;


      double distNM = Geo.distanceNM(bosLat, bosLon, sfoLat, sfoLon);
      System.out.println("distNM: " + distNM);
      double distKM1 = distNM * Geo.METERS_PER_NM / 1000;
      System.out.println("distKM1: " + distKM1);

      double distKM2 = Geo.distanceKM(bosLat, bosLon, sfoLat, sfoLon);
      System.out.println("distKM2: " + distKM2);

      // from Planet.java
      double wgs84_earthEquatorialRadiusMeters_D = 6378137.0;
      double circumferenceMeters = wgs84_earthEquatorialRadiusMeters_D * 2.0 * Math.PI;
      double wgs84_earthEquatorialCircumferenceNMiles_D = 21600.0;

      double circumf_meters_per_nm = circumferenceMeters / wgs84_earthEquatorialCircumferenceNMiles_D;

      System.out.println("circumf_meters_per_nm: " + circumf_meters_per_nm);
      System.out.println("Geo.METERS_PER_NM: " + Geo.METERS_PER_NM);

      Assert.assertEquals(distKM1, distKM2, 1);
    }
}
