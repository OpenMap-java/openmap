package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.proj.coords.DatumShiftGCT;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class UTMProjectionLoader extends BasicProjectionLoader {

    public static final String ZONE_NUMBER = "ZONE_NUMBER";

    public static final String ZONE_LETTER = "ZONE_LETTER";

    public UTMProjectionLoader() {
        super(UTMProjection.class, "UTM Projection", "UTM Projection");
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

            int zone_number = PropUtils.intFromProperties(props, ZONE_NUMBER, 0);
            char zone_letter = ((String) props.get(ZONE_LETTER)).charAt(0);
            boolean isnorthern = (zone_letter == 'N');
            Ellipsoid ellps = (Ellipsoid) props.get(ProjectionFactory.DATUM);
            GeoProj proj = new UTMProjection(center, scale, width, height, zone_number, isnorthern, ellps);
            if ((ellps != null) && (ellps != Ellipsoid.WGS_84)) {
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
