package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.proj.coords.DatumShiftGCT;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class UTMProjectionLoader extends BasicProjectionLoader {

    public static final String ZONE_NUMBER = "zoneNumber";

    public static final String ZONE_LETTER = "zoneLetter";

    protected int defaultZoneNumber = 0;
    protected char defaultZoneLetter = 'N';

    public UTMProjectionLoader() {
        super(UTMProjection.class, "UTM Projection", "UTM Projection");
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        defaultZoneNumber = PropUtils.intFromProperties(props, prefix
                + ZONE_NUMBER, defaultZoneNumber);
        defaultZoneLetter = PropUtils.charFromProperties(props, prefix
                + ZONE_LETTER, defaultZoneLetter);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + ZONE_LETTER, "" + defaultZoneLetter);
        props.put(prefix + ZONE_NUMBER, Integer.toString(defaultZoneNumber));
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                UTMProjectionLoader.class,
                ZONE_LETTER,
                "Zone Letter",
                "The UTM Zone Letter (N or S)",
                null);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                UTMProjectionLoader.class,
                ZONE_NUMBER,
                "Zone Number",
                "The UTM Zone Number",
                null);

        return props;
    }

    public Projection create(Properties props) throws ProjectionException {
        try {
            LatLonPoint center = convertToLLP((Point2D) props.get(ProjectionFactory.CENTER));
            float scale = PropUtils.floatFromProperties(props,
                    ProjectionFactory.SCALE,
                    10000000);
            int height = PropUtils.intFromProperties(props,
                    ProjectionFactory.HEIGHT,
                    100);
            int width = PropUtils.intFromProperties(props,
                    ProjectionFactory.WIDTH,
                    100);

            // TODO I'm thinking that if we have a center lat/lon for the
            // projection we can figure out what the zone number and letter are.
            // We don't need to pass properties for them. On second thought,
            // maybe that should apply only if the defaults aren't set.

            int zone_number = PropUtils.intFromProperties(props,
                    ZONE_NUMBER,
                    defaultZoneNumber);

            char zone_letter = PropUtils.charFromProperties(props,
                    ZONE_LETTER,
                    defaultZoneLetter);
            boolean isnorthern = (zone_letter == 'N');
            String ellipsoidString = props.getProperty(ProjectionFactory.DATUM);
            // Assume WGS84 if not specified.
            Ellipsoid ellps = Ellipsoid.WGS_84;
            if (ellipsoidString != null) {
                ellps = Ellipsoid.getByName(ellipsoidString);
            }
            GeoProj proj = new UTMProjection(center, scale, width, height, zone_number, isnorthern, ellps);
            // handle GRS80 as WGS84 as they are almost the same
            if ((ellps != null)
                    && (!(ellps == Ellipsoid.WGS_84) || (ellps == Ellipsoid.GRS_1980))) {
                proj = new DatumShiftProjection(proj, new DatumShiftGCT(ellps));
            }
            return proj;
        } catch (Exception e) {
            if (Debug.debugging("proj")) {
                Debug.output("UTMProjectionLoader: problem creating UTM projection "
                        + e.getMessage());
                e.printStackTrace();
            }
        }

        throw new ProjectionException("UTMProjectionLoader: problem creating UTM projection");
    }

}
