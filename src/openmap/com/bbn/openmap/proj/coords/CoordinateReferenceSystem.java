package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.LLXYLoader;
import com.bbn.openmap.proj.LambertConformalLoader;
import com.bbn.openmap.proj.MercatorLoader;
import com.bbn.openmap.proj.Planet;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.proj.ProjectionLoader;
import com.bbn.openmap.proj.UTMProjectionLoader;

public class CoordinateReferenceSystem {

    private String code;

    private GeoCoordTransformation coordTransform;

    private ProjectionLoader projLoader;

    private BoundingBox boundingBox;

    private String projLoaderClassName;

    private Ellipsoid ellipsoid = Ellipsoid.WGS_84;

    private Properties defaultProjectionParameters;

    protected static final Map<String, CoordinateReferenceSystem> crss = Collections.synchronizedMap(new TreeMap<String, CoordinateReferenceSystem>());

    static {
        // unprojected wgs84
        addCrs(new CoordinateReferenceSystem("EPSG:4326", LatLonGCT.INSTANCE, LLXYLoader.class, Ellipsoid.WGS_84));
        addCrs(new CoordinateReferenceSystem("CRS:84", LatLonGCT.INSTANCE, LLXYLoader.class, Ellipsoid.WGS_84));

        // unprojected ED50
        addCrs(new CoordinateReferenceSystem("EPSG:4230", new DatumShiftGCT(Ellipsoid.INTERNATIONAL), LLXYLoader.class, Ellipsoid.INTERNATIONAL));

        // Spherical Mercator for overlaying with Google Maps
        // http://trac.openlayers.org/wiki/SphericalMercator
        addCrs(new CoordinateReferenceSystem("EPSG:900913", new MercatorMeterGCT(Planet.wgs84_earthEquatorialRadiusMeters_D, Planet.wgs84_earthEquatorialRadiusMeters_D), MercatorLoader.class, Ellipsoid.WGS_84));

        addUtms();

        // Estonian Coordinate System of 1997 - EPSG:3301
        // http://spatialreference.org/ref/epsg/3301/
        // bounding box is needed by uDig. bounding box values from a national
        // WMS from Estonian
        addLcc("EPSG:3301",
                Ellipsoid.GRS_1980,
                59.33333333333334,
                58d,
                57.51755393055556d,
                24d,
                500000,
                6375000,
                new BoundingBox(300000, 6.3e+06, 800000, 6.7e+06));
    }

    private static void addLcc(String code, Ellipsoid ellps, double sp1,
                               double sp2, double refLat, double centMeri,
                               double falseEast, double falseNorth,
                               BoundingBox bbox) {

        Properties props = new Properties();
        props.put(LambertConformalLoader.StandardParallelOneProperty,
                Double.toString(sp1));
        props.put(LambertConformalLoader.StandardParallelTwoProperty,
                Double.toString(sp2));
        props.put(LambertConformalLoader.ReferenceLatitudeProperty,
                Double.toString(refLat));
        props.put(LambertConformalLoader.CentralMeridianProperty,
                Double.toString(centMeri));
        props.put(LambertConformalLoader.FalseEastingProperty,
                Double.toString(falseEast));
        props.put(LambertConformalLoader.FalseNorthingProperty,
                Double.toString(falseNorth));
        props.put(ProjectionFactory.DATUM, ellps);
        props.put(ProjectionFactory.CENTER,
                new LatLonPoint.Double(refLat, centMeri));

        addCrs(new CoordinateReferenceSystem(code, new LambertConformalGCT(props), LambertConformalLoader.class, ellps, props, bbox));
    }

    private static void addCrs(CoordinateReferenceSystem crs) {
        crss.put(crs.getCode(), crs);
    }

    private static void addUtms() {
        for (int zone = 1; zone <= 60; zone++) {

            String zoneCode = String.valueOf(zone);
            while (zoneCode.length() < 2) {
                zoneCode = "0" + zoneCode;
            }

            // wgs84 utm
            addUtm("EPSG:326" + zoneCode, zone, 'N', Ellipsoid.WGS_84);
            addUtm("EPSG:327" + zoneCode, zone, 'S', Ellipsoid.WGS_84);

            // ed50 utm
            if ((zone >= 28) && (zone <= 38)) {
                addUtm("EPSG:230" + zoneCode,
                        zone,
                        'N',
                        Ellipsoid.INTERNATIONAL);
            }
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
        UTMGCT utmgct = new UTMGCT(zone_number, zone_letter);
        utmgct.setEllipsoid(ellps);

        GeoCoordTransformation gct = utmgct;

        // add datum shift for non-wgs84/grs80
        if (!((ellps == Ellipsoid.WGS_84) || (ellps == Ellipsoid.GRS_1980))) {
            DatumShiftGCT egct = new DatumShiftGCT(ellps);
            gct = new MultiGCT(new GeoCoordTransformation[] { egct, utmgct });
        }

        addCrs(new CoordinateReferenceSystem(epsg, gct, UTMProjectionLoader.class, ellps, projProps));
    }

    public CoordinateReferenceSystem(String code,
            GeoCoordTransformation coordConverter, Class<?> projLoaderClass,
            Ellipsoid ellipsoid) {
        this.code = code;
        this.coordTransform = coordConverter;
        this.projLoaderClassName = projLoaderClass.getName();
        this.ellipsoid = ellipsoid;

        defaultProjectionParameters = new Properties();
        defaultProjectionParameters.put(ProjectionFactory.DATUM, ellipsoid);
    }

    public CoordinateReferenceSystem(String code,
            GeoCoordTransformation coordConverter, Class<?> projLoaderClass,
            Ellipsoid ellipsoid, Properties projectionParameters) {
        this(code,
             coordConverter,
             projLoaderClass,
             ellipsoid,
             projectionParameters,
             null);
    }

    public CoordinateReferenceSystem(String code,
            GeoCoordTransformation coordConverter, Class<?> projLoaderClass,
            Ellipsoid ellipsoid, Properties projectionParameters,
            BoundingBox boundingBox) {
        this(code, coordConverter, projLoaderClass, ellipsoid);

        defaultProjectionParameters.putAll(projectionParameters);
        this.boundingBox = boundingBox;
    }

    public static CoordinateReferenceSystem getForCode(String code) {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) crss.get(code);
        // TODO: handle extra parameters like
        // AUTO2:42003,0.3048006096012192,-100,45. See ISO/DIS 19128 wms v1.3.0
        // chapter 6.7.3.4
        // TODO: clone to simplify transformator by not being thread safe?
        return crs;
    }

    public static Collection<String> getCodes() {
        return crss.keySet();
    }

    protected ProjectionLoader projectionLoader() {

        if (projLoader != null) {
            return projLoader;
        }

        try {
            Class<?> cl = Class.forName(projLoaderClassName);
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

    /**
     * Return a EPSG code like "EPSG:4326"
     * 
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * Return the bounding box of this coordinate system or null if the bounding
     * box is not defined.
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
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

    public Point2D forward(double lat, double lon) {
        return coordTransform.forward(lat, lon);
    }

    public int hashCode() {
        return getCode().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof CoordinateReferenceSystem) {
            CoordinateReferenceSystem o = (CoordinateReferenceSystem) obj;
            return getCode().equals(o.getCode());
        }
        return false;
    }

}
