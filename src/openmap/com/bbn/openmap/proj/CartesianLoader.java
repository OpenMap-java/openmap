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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/CartesianLoader.java,v $
// $RCSfile: CartesianLoader.java,v $
// $Revision: 1.2 $
// $Date: 2006/02/24 20:43:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * ProjectionLoader to add the Cartesian projection to an OpenMap application.
 * There are some properties you can set for the Cartesian projection, namely
 * limits on where it can pan. If you don't set the limits, the projection will
 * just keep going. You can use the anchorX and anchorY settings to hold the map
 * against some of the limits.
 * 
 * <pre>
 *  topLimit=top coordinate limit
 *  bottomLimit=bottom coordinate limit
 *  leftLimit=left side coordinate limit
 *  rightLimit=right side coordinate limit
 *  anchorX=horizontal coordinate to set on projection before checking limits
 *  anchorY=vertical coordinate to set on projection before checking limits
 * </pre>
 * 
 * @see BasicProjectionLoader
 */
public class CartesianLoader extends BasicProjectionLoader implements
        ProjectionLoader {

    public final static String TopLimitProperty = "topLimit";
    public final static String BottomLimitProperty = "bottomLimit";
    public final static String RightLimitProperty = "rightLimit";
    public final static String LeftLimitProperty = "leftLimit";
    public final static String AnchorXProperty = "anchorX";
    public final static String AnchorYProperty = "anchorY";

    /**
     * The coordinate limit of the left side of the projection. If the left side
     * of the map projection would show coordinates more left than this value,
     * the center of the map will be changed so that this value is on the edge.
     */
    protected double leftLimit = Double.NEGATIVE_INFINITY;
    /**
     * The coordinate limit of the right side of the projection. If the right
     * side of the map projection would show coordinates more right than this
     * value, the center of the map will be changed so that this value is on the
     * edge.
     */
    protected double rightLimit = Double.POSITIVE_INFINITY;
    /**
     * The coordinate limit of the top side of the projection. If the top side
     * of the map projection would show coordinates higher than this value, the
     * center of the map will be changed so that this value is on the edge.
     */
    protected double topLimit = Double.POSITIVE_INFINITY;
    /**
     * The coordinate limit of the bottom side of the projection. If the bottom
     * side of the map projection would show coordinates lower than this value,
     * the center of the map will be changed so that this value is on the edge.
     */
    protected double bottomLimit = Double.NEGATIVE_INFINITY;
    /**
     * A point that can be used for force the projection against the limits. Is
     * only used if the limits are set to be something other than infinity.
     */
    protected Point2D limitAnchorPoint;

    public CartesianLoader() {
        super(Cartesian.class,
              Cartesian.CartesianName,
              "Cartesian projection for displaying projected data.");
    }

    /**
     * Create the projection with the given parameters.
     * 
     * @throws exception if a parameter is missing or invalid.
     */
    public Projection create(Properties props) throws ProjectionException {

        try {
            Point2D center = (Point2D) props.get(ProjectionFactory.CENTER);
            float scale = PropUtils.floatFromProperties(props,
                    ProjectionFactory.SCALE,
                    10000000);
            int height = PropUtils.intFromProperties(props,
                    ProjectionFactory.HEIGHT,
                    100);
            int width = PropUtils.intFromProperties(props,
                    ProjectionFactory.WIDTH,
                    100);

            Cartesian proj = new Cartesian(center, scale, width, height);

            proj.setLimits(topLimit,
                    bottomLimit,
                    leftLimit,
                    rightLimit,
                    limitAnchorPoint);

            return proj;

        } catch (Exception e) {
            if (Debug.debugging("proj")) {
                Debug.output("CartesianLoader: problem creating Cartesian projection "
                        + e.getMessage());
            }
        }

        throw new ProjectionException("CartesianLoader: problem creating Cartesian projection");
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        leftLimit = PropUtils.doubleFromProperties(props, prefix
                + LeftLimitProperty, Double.NEGATIVE_INFINITY);
        rightLimit = PropUtils.doubleFromProperties(props, prefix
                + RightLimitProperty, Double.POSITIVE_INFINITY);
        topLimit = PropUtils.doubleFromProperties(props, prefix
                + TopLimitProperty, Double.POSITIVE_INFINITY);
        bottomLimit = PropUtils.doubleFromProperties(props, prefix
                + BottomLimitProperty, Double.NEGATIVE_INFINITY);

        double x = PropUtils.doubleFromProperties(props, prefix
                + AnchorXProperty, Double.POSITIVE_INFINITY);
        double y = PropUtils.doubleFromProperties(props, prefix
                + AnchorYProperty, Double.POSITIVE_INFINITY);

        if (x != Double.POSITIVE_INFINITY && y != Double.POSITIVE_INFINITY) {
            limitAnchorPoint = new Point2D.Double(x, y);
        }

    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        String x = "";
        String y = "";
        String top = "";
        String bottom = "";
        String left = "";
        String right = "";
        if (leftLimit != Double.NEGATIVE_INFINITY) {
            left = Double.toString(leftLimit);
        }
        if (topLimit != Double.POSITIVE_INFINITY) {
            top = Double.toString(topLimit);
        }
        if (rightLimit != Double.POSITIVE_INFINITY) {
            right = Double.toString(rightLimit);
        }
        if (bottomLimit != Double.NEGATIVE_INFINITY) {
            bottom = Double.toString(bottomLimit);
        }
        props.put(prefix + TopLimitProperty, top);
        props.put(prefix + BottomLimitProperty, bottom);
        props.put(prefix + RightLimitProperty, right);
        props.put(prefix + LeftLimitProperty, left);
        if (limitAnchorPoint != null) {
            x = Double.toString(limitAnchorPoint.getX());
            y = Double.toString(limitAnchorPoint.getY());
        }
        props.put(prefix + AnchorXProperty, x);
        props.put(prefix + AnchorYProperty, y);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                CartesianLoader.class,
                LeftLimitProperty,
                "Left Limit",
                "Coordinate limit for the left side of the map.",
                null);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                CartesianLoader.class,
                RightLimitProperty,
                "Right Limit",
                "Coordinate limit for the right side of the map.",
                null);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                CartesianLoader.class,
                TopLimitProperty,
                "Top Limit",
                "Coordinate limit for the top of the map.",
                null);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                CartesianLoader.class,
                BottomLimitProperty,
                "Bottom Limit",
                "Coordinate limit for the bottom of the map.",
                null);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                CartesianLoader.class,
                AnchorXProperty,
                "Anchor X",
                "Horizontal Coordinate for anchor point, used to hold projection against limits.",
                null);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                CartesianLoader.class,
                AnchorYProperty,
                "Anchor Y",
                "Horizontal Coordinate for anchor point, used to hold projection against limits.",
                null);

        props.put(initPropertiesProperty, PrettyNameProperty + " "
                + DescriptionProperty + " " + TopLimitProperty + " "
                + BottomLimitProperty + " " + RightLimitProperty + " "
                + LeftLimitProperty + " " + AnchorXProperty + " "
                + AnchorYProperty);

        return props;
    }

    public double getBottomLimit() {
        return bottomLimit;
    }

    public void setBottomLimit(double bottomLimit) {
        this.bottomLimit = bottomLimit;
    }

    public double getLeftLimit() {
        return leftLimit;
    }

    public void setLeftLimit(double leftLimit) {
        this.leftLimit = leftLimit;
    }

    public Point2D getLimitAnchorPoint() {
        return limitAnchorPoint;
    }

    public void setLimitAnchorPoint(Point2D limitAnchorPoint) {
        this.limitAnchorPoint = limitAnchorPoint;
    }

    public double getRightLimit() {
        return rightLimit;
    }

    public void setRightLimit(double rightLimit) {
        this.rightLimit = rightLimit;
    }

    public double getTopLimit() {
        return topLimit;
    }

    public void setTopLimit(double topLimit) {
        this.topLimit = topLimit;
    }

}