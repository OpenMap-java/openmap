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
import com.bbn.openmap.proj.Proj;
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

        // http://johndeck.blogspot.com/2005_09_01_johndeck_archive.html
        // <54004> +proj=merc +lat_ts=0 +lon_0=0 +k=1.000000 +x_0=0 +y_0=0
        // +ellps=WGS84 +datum=WGS84 +units=m no_defs <>
        crss.put("EPSG:54004",
                new CoordinateReferenceSystem(MercatorMeterGCT.INSTANCE, MercatorLoader.class, Ellipsoid.WGS_84));

        // http://locative.us/freemap/gdal/data/cubewerx_extra.wkt
        // 41001,PROJCS["WGS84 / Simple Mercator",GEOGCS["WGS
        // 84",DATUM["WGS_1984",SPHEROID["WGS_1984",6378137,298.257223563]],PRIMEM["Greenwich",0],UNIT["Decimal_Degree",0.0174532925199433]],PROJECTION["Mercator_1SP"],PARAMETER["latitude_of_origin",0],PARAMETER["central_meridian",0],PARAMETER["false_easting",0],PARAMETER["false_northing",0],UNIT["Meter",1],AUTHORITY["EPSG","41001"]]
        crss.put("AUTO:41001",
                new CoordinateReferenceSystem(MercatorMeterGCT.INSTANCE, MercatorLoader.class, Ellipsoid.WGS_84));

        // http://wiki.osgeo.org/index.php/WMS_Tiling_Client_Recommendation
        crss.put("OSGEO:41001",
                new CoordinateReferenceSystem(MercatorMeterGCT.INSTANCE, MercatorLoader.class, Ellipsoid.WGS_84));

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

    public void prepareProjection(Proj proj) {
        // TODO: do we need this??
        
        // TODO: Needs to be updated to check for GeoProj.
//        proj.setPlanetRadius((float) ellipsoid.radius);
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
