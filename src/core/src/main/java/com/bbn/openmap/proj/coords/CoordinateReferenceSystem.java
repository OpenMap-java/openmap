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

    private Properties defaultProjectionParameters = new Properties();

    private AxisOrder axisOrder;

    protected static final Map<String, CoordinateReferenceSystem> crss =
            Collections.synchronizedMap(new TreeMap<String, CoordinateReferenceSystem>());

    static {
        // unprojected wgs84
        addCrs(new CoordinateReferenceSystem("EPSG:4326", LatLonGCT.INSTANCE, LLXYLoader.class, Ellipsoid.WGS_84, null, null,
                                             AxisOrder.northBeforeEast));
        addCrs(new CoordinateReferenceSystem("CRS:84", LatLonGCT.INSTANCE, LLXYLoader.class, Ellipsoid.WGS_84));

        // unprojected ED50
        addCrs(new CoordinateReferenceSystem("EPSG:4230", new DatumShiftGCT(Ellipsoid.INTERNATIONAL), LLXYLoader.class,
                                             Ellipsoid.INTERNATIONAL));

        // Spherical Mercator for overlaying with Google Maps
        // http://trac.openlayers.org/wiki/SphericalMercator
        addCrs(new CoordinateReferenceSystem("EPSG:900913", new MercatorMeterGCT(Planet.wgs84_earthEquatorialRadiusMeters_D,
                                                                                 Planet.wgs84_earthEquatorialRadiusMeters_D),
                                             MercatorLoader.class, Ellipsoid.WGS_84));
        addCrs(new CoordinateReferenceSystem("EPSG:3857", new MercatorMeterGCT(Planet.wgs84_earthEquatorialRadiusMeters_D,
                                                                               Planet.wgs84_earthEquatorialRadiusMeters_D),
                                             MercatorLoader.class, Ellipsoid.WGS_84));

        addUtms();

        // Estonian Coordinate System of 1997 - EPSG:3301
        // http://spatialreference.org/ref/epsg/3301/
        // bounding box values from a national WMS from Estonian
        addLcc("EPSG:3301", Ellipsoid.GRS_1980, 59.33333333333334, 58d, 57.51755393055556d, 24d, 500000, 6375000,
               new BoundingBox(300000, 6.3e+06, 800000, 6.7e+06), AxisOrder.northBeforeEast);

        // SWEREF 99 TM (EPSG:3006)
        // http://spatialreference.org/ref/epsg/3006/
        addUtm("EPSG:3006", 33, 'N', Ellipsoid.GRS_1980,
               new BoundingBox(218128.7031d, 6126002.9379d, 1083427.2970d, 7692850.9468d), AxisOrder.northBeforeEast);

        // ETRS89 / ETRS-TM35FIN
        // http://spatialreference.org/ref/epsg/3067/
        addUtm("EPSG:3067", 35, 'N', Ellipsoid.GRS_1980, new BoundingBox(50199.4814d, 6582464.0358d, 761274.6247d, 7799839.8902d));
    }

    private static void addLcc(String code, Ellipsoid ellps, double sp1, double sp2, double refLat, double centMeri,
                               double falseEast, double falseNorth, BoundingBox bbox, AxisOrder axisOrder) {

        Properties props = new Properties();
        props.put(LambertConformalLoader.StandardParallelOneProperty, Double.toString(sp1));
        props.put(LambertConformalLoader.StandardParallelTwoProperty, Double.toString(sp2));
        props.put(LambertConformalLoader.ReferenceLatitudeProperty, Double.toString(refLat));
        props.put(LambertConformalLoader.CentralMeridianProperty, Double.toString(centMeri));
        props.put(LambertConformalLoader.FalseEastingProperty, Double.toString(falseEast));
        props.put(LambertConformalLoader.FalseNorthingProperty, Double.toString(falseNorth));
        props.put(ProjectionFactory.DATUM, ellps);
        props.put(ProjectionFactory.CENTER, new LatLonPoint.Double(refLat, centMeri));

        addCrs(new CoordinateReferenceSystem(code, new LambertConformalGCT(props), LambertConformalLoader.class, ellps, props,
                                             bbox, axisOrder));
    }

    public static void addCrs(CoordinateReferenceSystem crs) {
        crss.put(crs.getCode(), crs);
    }

    private static void addUtms() {
        for (int zone = 1; zone <= 60; zone++) {

            String zoneCode = String.valueOf(zone);
            while (zoneCode.length() < 2) {
                zoneCode = "0" + zoneCode;
            }

            // wgs84 utm
            addUtm("EPSG:326" + zoneCode, zone, 'N', Ellipsoid.WGS_84, null);
            addUtm("EPSG:327" + zoneCode, zone, 'S', Ellipsoid.WGS_84, null);

            // ed50 utm
            if ((zone >= 28) && (zone <= 38)) {
                addUtm("EPSG:230" + zoneCode, zone, 'N', Ellipsoid.INTERNATIONAL, null);
            }

            // ETRS89 utm
            if ((zone >= 28) && (zone <= 38)) {
                addUtm("EPSG:258" + zoneCode, zone, 'N', Ellipsoid.GRS_1980, null);
            }

        }
    }

    private static void addUtm(String epsg, int zone_number, char zone_letter, Ellipsoid ellps, BoundingBox bbox) {
        addUtm(epsg, zone_number, zone_letter, ellps, bbox, AxisOrder.eastBeforeNorth);
    }

    private static void addUtm(String epsg, int zone_number, char zone_letter, Ellipsoid ellps, BoundingBox bbox,
                               AxisOrder axisOrder) {
        // some properties for the projection loader
        Properties projProps = new Properties();
        projProps.put(UTMProjectionLoader.ZONE_NUMBER, Integer.toString(zone_number));
        projProps.put(UTMProjectionLoader.ZONE_LETTER, Character.toString(zone_letter));
        UTMGCT utmgct = new UTMGCT(zone_number, zone_letter);
        utmgct.setEllipsoid(ellps);

        GeoCoordTransformation gct = utmgct;

        // add datum shift for non-wgs84/grs80
        if (!((ellps == Ellipsoid.WGS_84) || (ellps == Ellipsoid.GRS_1980))) {
            DatumShiftGCT egct = new DatumShiftGCT(ellps);
            gct = new MultiGCT(new GeoCoordTransformation[] {
                egct,
                utmgct
            });
        }

        addCrs(new CoordinateReferenceSystem(epsg, gct, UTMProjectionLoader.class, ellps, projProps, bbox, axisOrder));
    }

    public CoordinateReferenceSystem(String code, GeoCoordTransformation coordConverter, Class<?> projLoaderClass,
                                     Ellipsoid ellipsoid) {
        this.code = code;
        this.coordTransform = coordConverter;
        this.projLoaderClassName = projLoaderClass.getName();
        this.ellipsoid = ellipsoid;
        this.axisOrder = AxisOrder.eastBeforeNorth;

        defaultProjectionParameters.put(ProjectionFactory.DATUM, ellipsoid);
    }

    public CoordinateReferenceSystem(String code, GeoCoordTransformation coordConverter, Class<?> projLoaderClass,
                                     Ellipsoid ellipsoid, Properties projectionParameters, BoundingBox boundingBox,
                                     AxisOrder axisOrder) {
        this(code, coordConverter, projLoaderClass, ellipsoid);

        if (projectionParameters != null) {
            defaultProjectionParameters.putAll(projectionParameters);
        }
        this.boundingBox = boundingBox;
        this.axisOrder = axisOrder;
    }

    public static CoordinateReferenceSystem getForCode(String code) {
        CoordinateReferenceSystem crs = crss.get(code);
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
     * @return EPSG code
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

    public AxisOrder getAxisOrder() {
        return axisOrder;
    }

    public void prepareProjection(GeoProj proj) {
        // TODO: do we need this??
        proj.setPlanetRadius(ellipsoid.radius);
    }

    /**
     * Convert the given (projected) coordinate in the CRS to a LatLonPoint
     * without respect for axis order.
     * 
     * @param x
     * @param y
     * @return LatLonPoint from inverse projected x, y coordinate
     */
    public LatLonPoint inverse(double x, double y) {
        return coordTransform.inverse(x, y);
    }

    /**
     * Convert the given (projected) coordinate in the CRS to a LatLonPoint. If
     * the useAxisOrder parameter is true, then the
     * {@link CoordinateReferenceSystem}s {@link AxisOrder} will be used.
     * 
     * @param x
     * @param y
     * @return LatLonPoint from inverse projected x, y coordinate
     */
    public LatLonPoint inverse(double x, double y, boolean useAxisOrder) {
        if (useAxisOrder && (getAxisOrder() == AxisOrder.northBeforeEast)) {
            return coordTransform.inverse(y, x);
        }
        return coordTransform.inverse(x, y);
    }

    public Point2D forward(double lat, double lon) {
        return coordTransform.forward(lat, lon);
    }

    public Point2D forward(double lat, double lon, boolean useAxisOrder) {
        Point2D p = coordTransform.forward(lat, lon);
        if (useAxisOrder && (getAxisOrder() == AxisOrder.northBeforeEast)) {
            double x = p.getX();
            double y = p.getY();
            p.setLocation(y, x);
        }
        return p;
    }

    public int hashCode() {
        return getCode().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CoordinateReferenceSystem o = (CoordinateReferenceSystem) obj;
        return getCode().equals(o.getCode());
    }

}
