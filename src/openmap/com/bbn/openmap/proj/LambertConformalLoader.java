// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/LambertConformalLoader.java,v $
// $RCSfile: LambertConformalLoader.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/15 19:38:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.I18n;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * ProjectionLoader to add the LambertConformal projection to an
 * OpenMap application.
 * 
 * @see BasicProjectionLoader
 */
public class LambertConformalLoader extends BasicProjectionLoader implements
        ProjectionLoader {

    public final static String CentralMeridianProperty = "centralMeridian";
    public final static String StandardParallelOneProperty = "standardParallel1";
    public final static String StandardParallelTwoProperty = "standardParallel2";
    public final static String ReferenceLatitudeProperty = "referenceLatitude";
    public final static String FalseEastingProperty = "falseEasting";
    public final static String FalseNorthingProperty = "faleNorthing";

    protected double centralMeridian = -71.50f;
    protected double standardParallel1 = 41.716667f;
    protected double standardParallel2 = 42.683333f;
    protected double referenceLatitude = 41.0;
    protected double falseEasting = 200000;
    protected double falseNorthing = 750000;

    public LambertConformalLoader() {
        super(LambertConformal.class,
              LambertConformal.LambertConformalName,
              "Lambert Conformal Projection");
    }

    /**
     * Create the projection with the given parameters.
     * 
     * @throws exception if a parameter is missing or invalid.
     */
    public Projection create(Properties props) throws ProjectionException {

        try {
            LatLonPoint llp = convertToLLP((Point2D) props.get(ProjectionFactory.CENTER));
            float scale = PropUtils.floatFromProperties(props,
                    ProjectionFactory.SCALE,
                    10000000);
            int height = PropUtils.intFromProperties(props,
                    ProjectionFactory.HEIGHT,
                    100);
            int width = PropUtils.intFromProperties(props,
                    ProjectionFactory.WIDTH,
                    100);
            double central_meridian = PropUtils.doubleFromProperties(props,
                    CentralMeridianProperty,
                    centralMeridian);
            double sp_one = PropUtils.doubleFromProperties(props,
                    StandardParallelOneProperty,
                    standardParallel1);
            double sp_two = PropUtils.doubleFromProperties(props,
                    StandardParallelTwoProperty,
                    standardParallel2);
            double rl = PropUtils.doubleFromProperties(props,
                    ReferenceLatitudeProperty,
                    referenceLatitude);
            double fe = PropUtils.doubleFromProperties(props,
                    FalseEastingProperty,
                    falseEasting);
            double fn = PropUtils.doubleFromProperties(props,
                    FalseNorthingProperty,
                    falseNorthing);
            Ellipsoid ellps = (Ellipsoid) props.get(ProjectionFactory.DATUM);

            return new LambertConformal(llp, scale, width, height, central_meridian, sp_one, sp_two, rl, fe, fn, ellps);

        } catch (Exception e) {
            if (Debug.debugging("proj")) {
                Debug.output("LambertConformalLoader: problem creating LambertConformal projection "
                        + e.getMessage());
            }
        }

        throw new ProjectionException("LambertConformalLoader: problem creating LambertConformal projection");

    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        centralMeridian = PropUtils.doubleFromProperties(props, prefix
                + CentralMeridianProperty, centralMeridian);
        standardParallel1 = PropUtils.doubleFromProperties(props, prefix
                + StandardParallelOneProperty, standardParallel1);
        standardParallel2 = PropUtils.doubleFromProperties(props, prefix
                + StandardParallelTwoProperty, standardParallel2);
        referenceLatitude = PropUtils.doubleFromProperties(props, prefix
                + ReferenceLatitudeProperty, referenceLatitude);
        falseEasting = PropUtils.doubleFromProperties(props, prefix
                + FalseEastingProperty, falseEasting);
        falseNorthing = PropUtils.doubleFromProperties(props, prefix
                + FalseNorthingProperty, falseNorthing);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + CentralMeridianProperty,
                Double.toString(centralMeridian));
        props.put(prefix + StandardParallelOneProperty,
                Double.toString(standardParallel1));
        props.put(prefix + StandardParallelTwoProperty,
                Double.toString(standardParallel2));
        props.put(prefix + ReferenceLatitudeProperty,
                Double.toString(referenceLatitude));
        props.put(prefix + FalseEastingProperty, Double.toString(falseEasting));
        props.put(prefix + FalseNorthingProperty,
                Double.toString(falseNorthing));

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        String internString = i18n.get(LambertConformalLoader.class,
                CentralMeridianProperty,
                "Central Meridian");
        props.put(CentralMeridianProperty + LabelEditorProperty, internString);

        internString = i18n.get(ProjectionLoader.class,
                CentralMeridianProperty,
                I18n.TOOLTIP,
                "Central Meridian Longitude for Projection");
        props.put(CentralMeridianProperty, internString);

        internString = i18n.get(LambertConformalLoader.class,
                StandardParallelOneProperty,
                "Standard Parallel 1");
        props.put(StandardParallelOneProperty + LabelEditorProperty,
                internString);

        internString = i18n.get(ProjectionLoader.class,
                StandardParallelOneProperty,
                I18n.TOOLTIP,
                "First Standard Parallel Latitude for Projection");
        props.put(StandardParallelOneProperty, internString);

        internString = i18n.get(LambertConformalLoader.class,
                StandardParallelTwoProperty,
                "Standard Parallel 2");
        props.put(StandardParallelTwoProperty + LabelEditorProperty,
                internString);

        internString = i18n.get(ProjectionLoader.class,
                StandardParallelTwoProperty,
                I18n.TOOLTIP,
                "Second Standard Parallel Latitude for Projection");
        props.put(StandardParallelTwoProperty, internString);

        internString = i18n.get(LambertConformalLoader.class,
                ReferenceLatitudeProperty,
                "Reference Latitude");
        props.put(ReferenceLatitudeProperty + LabelEditorProperty, internString);

        internString = i18n.get(ProjectionLoader.class,
                ReferenceLatitudeProperty,
                I18n.TOOLTIP,
                "The Reference Latitude of the Projection Origin");
        props.put(ReferenceLatitudeProperty, internString);

        internString = i18n.get(LambertConformalLoader.class,
                FalseEastingProperty,
                "False Easting");
        props.put(FalseEastingProperty + LabelEditorProperty, internString);

        internString = i18n.get(ProjectionLoader.class,
                FalseEastingProperty,
                I18n.TOOLTIP,
                "Meters added to projected location of origin E/W");
        props.put(FalseEastingProperty, internString);

        internString = i18n.get(LambertConformalLoader.class,
                FalseNorthingProperty,
                "False Northing");
        props.put(FalseNorthingProperty + LabelEditorProperty, internString);

        internString = i18n.get(ProjectionLoader.class,
                FalseNorthingProperty,
                I18n.TOOLTIP,
                "Meters added to projected location of origin N/S");
        props.put(FalseNorthingProperty, internString);

        props.put(initPropertiesProperty, PrettyNameProperty + " "
                + DescriptionProperty + " " + CentralMeridianProperty + " "
                + StandardParallelOneProperty + " "
                + StandardParallelTwoProperty + " " + ReferenceLatitudeProperty
                + " " + FalseEastingProperty + " " + FalseNorthingProperty);

        return props;
    }

}