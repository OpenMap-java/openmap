package org.geotiff.image;

import java.io.IOException;
import java.util.HashMap;

/**
 * The KeyRegistry provides the global registry for all sets of KeyMaps. All
 * methods are static.
 */
public class KeyRegistry {

    private static HashMap keyMaps = new HashMap();
    public static String GEOKEY = "geokey";
    public static String GEO_CTRANS = "ProjCoordTransGeoKey";
    public static String EPSG_PCS = "ProjectedCSTypeGeoKey";
    public static String EPSG_DATUM = "GeogGeodeticDatumGeoKey";
    public static String EPSG_ELLIPSE = "GeogEllipsoidGeoKey";
    public static String EPSG_GCS = "GeogGeographicTypeGeoKey";
    public static String EPSG_PM = "GeogPrimeMeridianGeoKey";
    public static String EPSG_PROJ = "ProjectionGeoKey";
    public static String EPSG_VERTCS = "VerticalCSTypeGeoKey";

    public static String UNIT_GEOG = "GeogLinearUnitsGeoKey";
    public static String UNIT_PROJ = "ProjLinearUnitsGeoKey";
    public static String UNIT_VERTCS = "VerticalUnitsGeoKey";

    public static KeyRegistry instance = new KeyRegistry();

    private KeyRegistry() {
        try {
            addKeyMap(EPSG_DATUM, "org/geotiff/epsg/epsg_datum.properties");
            addKeyMap(EPSG_ELLIPSE, "org/geotiff/epsg/epsg_ellipse.properties");
            addKeyMap(EPSG_GCS, "org/geotiff/epsg/epsg_gcs.properties");
            addKeyMap(EPSG_PCS, "org/geotiff/epsg/epsg_pcs.properties");
            addKeyMap(EPSG_PM, "org/geotiff/epsg/epsg_pm.properties");
            addKeyMap(EPSG_PROJ, "org/geotiff/epsg/epsg_proj.properties");
            addKeyMap(EPSG_VERTCS, "org/geotiff/epsg/epsg_vertcs.properties");
            addKeyMap(GEO_CTRANS, "org/geotiff/image/geo_ctrans.properties");
            addKeyMap(GEOKEY, "org/geotiff/image/geokey.properties");

            // A number of Keys use epsg units, so we share them
//            addKeyMap(UNIT_GEOG, "/org/geotiff/epsg/epsg_unit.properties");
            KeyMap units = getKeyMap(UNIT_GEOG);
            addKeyMap(UNIT_PROJ, units);
            addKeyMap(UNIT_VERTCS, units);
        } catch (IOException e) {
            // do nothing
        }
    }

    public static KeyRegistry getKeyRegistry() {
        return instance;
    }

    public static void addKeyMap(String name, KeyMap map) throws IOException {
        keyMaps.put(name, map);
    }

    public static void addKeyMap(String name, String resource)
            throws IOException {
        addKeyMap(name, new KeyMap(resource));
    }

    public static KeyMap getKeyMap(String name) {
        Object map = keyMaps.get(name);
        return (KeyMap) map;
    }

    public static int getCode(String map, String key) {
        KeyMap keyMap = getKeyMap(map);
        if (keyMap == null)
            return -1;
        return keyMap.getCode(key);
    }

    public static String getKey(String map, int code) {
        KeyMap keyMap = getKeyMap(map);
        if (keyMap == null)
            return null;
        return keyMap.getKey(code);
    }
}
