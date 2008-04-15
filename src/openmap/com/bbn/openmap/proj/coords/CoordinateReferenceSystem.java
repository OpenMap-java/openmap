package com.bbn.openmap.proj.coords;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.LLXYLoader;
import com.bbn.openmap.proj.MercatorLoader;
import com.bbn.openmap.proj.Planet;
import com.bbn.openmap.proj.ProjectionLoader;
import com.bbn.openmap.proj.UTMProjectionLoader;

public class CoordinateReferenceSystem {

    private GeoCoordTransformation coordTransform;

    private ProjectionLoader projLoader;

    private String projLoaderClassName;

    private Ellipsoid ellipsoid = Ellipsoid.WGS_84;

    private Properties defaultProjectionParameters;

    protected static final Map<String, CoordinateReferenceSystem> crss;

    static {
        crss = Collections.synchronizedMap(new TreeMap<String, CoordinateReferenceSystem>());

        crss.put("EPSG:4326",
                new CoordinateReferenceSystem(LatLonGCT.INSTANCE, LLXYLoader.class, Ellipsoid.WGS_84));
        crss.put("CRS:84",
                new CoordinateReferenceSystem(LatLonGCT.INSTANCE, LLXYLoader.class, Ellipsoid.WGS_84));

        // Spherical Mercator for overlaying with Google Maps
        // http://trac.openlayers.org/wiki/SphericalMercator
        crss.put("EPSG:900913",
                new CoordinateReferenceSystem(new MercatorMeterGCT(
                        Planet.wgs84_earthEquatorialRadiusMeters_D,
                        Planet.wgs84_earthEquatorialRadiusMeters_D), MercatorLoader.class,
                        Ellipsoid.WGS_84));

        addUtms();

    }

    private static void addUtms() {
        for (int zone = 1; zone <= 60; zone++) {

            String zoneCode = String.valueOf(zone);
            while (zoneCode.length() < 2) {
                zoneCode = "0" + zoneCode;
            }

            // addUtm("EPSG:32631", 31, 'N', Ellipsoid.WGS_84);
            addUtm("EPSG:326" + zoneCode, zone, 'N', Ellipsoid.WGS_84);
            // addUtm("EPSG:32731", 31, 'S', Ellipsoid.WGS_84);
            addUtm("EPSG:327" + zoneCode, zone, 'S', Ellipsoid.WGS_84);

            // addUtm("EPSG:25833", 33, 'N', Ellipsoid.GRS_1980);
            // TODO: is this correct?
            // addUtm("EPSG:258" + zoneCode, zone, 'N', Ellipsoid.GRS_1980);
        }
    }

    private static void addUtm(String epsg, int zone_number, char zone_letter,
                               Ellipsoid ellps) {
        // some properties for the projection loader
        Properties projProps = new Properties();
        projProps.put(UTMProjectionLoader.ZONE_NUMBER,
                Integer.toString(zone_number));
        projProps.put(UTMProjectionLoader.ZONE_LETTER,
                Character.toString(zone_letter));
        projProps.put(UTMProjectionLoader.ELLIPSOID, ellps);
        // The northing and easting values of the UTMPoint are not important,
        // the utm point is only used as a placeholder for zone numbers and
        // letters, and as a placeholder for n and e for inverse calculations.
        UTMPoint utmp = new UTMPoint(0, 0, zone_number, zone_letter);
        UTMGCT gct = new UTMGCT(utmp);
        gct.setEllipsoid(ellps);
        crss.put(epsg,
                new CoordinateReferenceSystem(gct, UTMProjectionLoader.class, ellps, projProps));
    }

    public CoordinateReferenceSystem(GeoCoordTransformation coordConverter,
            Class projLoaderClass, Ellipsoid ellipsoid) {
        this.coordTransform = coordConverter;
        this.projLoaderClassName = projLoaderClass.getName();
        this.ellipsoid = ellipsoid;
        this.defaultProjectionParameters = new Properties();
    }

    public CoordinateReferenceSystem(GeoCoordTransformation coordConverter,
            Class projLoaderClass, Ellipsoid ellipsoid,
            Properties projectionParameters) {
        this.coordTransform = coordConverter;
        this.projLoaderClassName = projLoaderClass.getName();
        this.ellipsoid = ellipsoid;
        this.defaultProjectionParameters = projectionParameters;
    }

    public static CoordinateReferenceSystem getForCode(String code) {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) crss.get(code);
        // TODO: handle extra parameters like
        // AUTO2:42003,0.3048006096012192,-100,45. See ISO/DIS 19128 wms v1.3.0
        // chapter 6.7.3.4
        return crs;
    }

    public static Collection getCodes() {
        return crss.keySet();
    }

    protected ProjectionLoader projectionLoader() {

        if (projLoader != null) {
            return projLoader;
        }

        try {
            Class cl = Class.forName(projLoaderClassName);
            Object o = cl.newInstance();
            projLoader = (ProjectionLoader) o;
            return projLoader;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (InstantiationException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage());
        }

    }

    public GeoProj createProjection(Properties overrideProjectionParameters) {
        Properties projectionParameters = new Properties();
        projectionParameters.putAll(defaultProjectionParameters);
        projectionParameters.putAll(overrideProjectionParameters);
        return (GeoProj) projectionLoader().create(projectionParameters);
    }

    public void prepareProjection(GeoProj proj) {
        // TODO: do we need this??
        proj.setPlanetRadius((float) ellipsoid.radius);
    }

    /**
     * Convert the given (projected) coordinate in the CRS to a LatLonPoint.
     * 
     * TODO: should we return null or throw if not possible?
     * 
     * @param x
     * @param y
     * @return
     */
    public LatLonPoint inverse(double x, double y) {
        return coordTransform.inverse(x, y);
    }

}
