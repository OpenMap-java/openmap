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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/util/ArcCalc.java,v $
// $RCSfile: ArcCalc.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/10 22:28:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.util;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A class that calculates an arc between two points, given the point
 * coordinates, and an arc measurement that represents, in radians,
 * the length of the part of the circle that should be represented by
 * the arc.
 */
public class ArcCalc implements Serializable {

    /** Debugging list showing algorithm points. */
    protected transient OMGraphicList arcGraphics = null;

    protected transient float[] xpoints;
    protected transient float[] ypoints;

    /**
     * This setting is the amount of an angle, limited to a
     * semi-circle (PI) that the curve will represent. In other words,
     * the arc between the two end points is going to look like a 0
     * degrees of a circle (straight line, which is the default), or
     * 180 degrees of a circle (full semi-circle). Given in radians,
     * though, not degrees. OK?
     */
    protected double arcAngle = 0;

    /**
     * For x-y and offset lines that have an arc drawn between them,
     * tell which way the arc should be drawn, toward the Equator, or
     * away from it, generally. Default is true, to make it look like
     * great circle line for northern hemisphere lines.
     */
    protected boolean arcUp = true;

    /**
     * Set to true if the points for the arc line up from x2, y2 to
     * x1, y1
     */
    protected boolean reversed = false;

    /**
     * Set the arc that is drawn between the points of a x-y or offset
     * line. If the arc amount is negative, the arc will be flipped
     * over.
     * 
     * @param aa arcAngle, in radians, between 0-PI.
     * @param putArcUp arc peak above points.
     */
    public ArcCalc(double aa, boolean putArcUp) {
        arcAngle = aa;
        arcUp = putArcUp;

        // If it's negative, flip it over...
        if (aa < 0) {
            arcAngle *= -1.0;
            arcUp = !arcUp;
        }

        if (arcAngle > Math.PI) {
            arcAngle = Math.PI;
        }
    }

    /**
     * Return the arc angle set for this line. Will only be set if it
     * was set externally.
     * 
     * @return arc angle in radians.
     */
    public double getArcAngle() {
        return arcAngle;
    }

    /**
     * Returns true if the arc direction setting is upward, meaning
     * that the peak of the arc is above (or more so) the line that
     * goes between the two points.
     */
    public boolean isArcUp() {
        return arcUp;
    }

    /**
     * Generate the points that will generate the curved line between
     * two points. The arcAngle is the number of radians of a circle
     * that the arc should represent. Math.PI is the Max. The
     * setArcAngle should be called before this method is called, so
     * that the method knows what to create.
     */
    public void generate(int x1, int y1, int x2, int y2) {

        // The algorithm.
        //
        // Draw a straight line between the points, and figure out the
        // center point between them on the line. Then, on another
        // line that is perpendicular to the first line, figure out
        // where the point is that will act as a center of a circle.
        // That circle needs to pass through both points, and the
        // radius is such that the arc angle of the circle between the
        // points is the same as the arcAngle set for the ArcCalc.
        // Then, the arc needs to be generated. This is done by
        // looking at the circle, and figuring out the angle (from 0
        // to 2PI) that the line from the center to point 1, and then
        // the center to point 2. This gives us the angular extents
        // of the arc. Then we need to figure out the angle
        // increments needed to get good coordinates for the arc.
        // Then, starting at the low arc angle, we increment it to get
        // the coordinates for the arced line, a given radius away
        // from the circle center, between the arc angle extents.

        Point midPoint = new Point();
        Point arcCenter = new Point();
        Point2D peakPoint = new Point2D.Float();

        // pixel distance between points.
        double distance = Math.sqrt(Math.pow(Math.abs(y2 - y1), 2.0)
                + Math.pow(Math.abs(x2 - x1), 2.0));
        // slope of straight line between points.
        double straightLineSlope = Math.atan((double) (y2 - y1)
                / (double) (x2 - x1));

        // slope of line that the arc focus will reside on.
        double inverseSlope = straightLineSlope - (Math.PI / 2.0);

        if (Debug.debugging("arc")) {
            Debug.output("ArcCalc.generate: Slope is "
                    + Math.toDegrees(straightLineSlope)
                    + " degrees, distance = " + distance + " pixels.");
        }

        // centerX/Y is the midpoint between the two points.
        midPoint.setLocation(x1 + ((x2 - x1) / 2), y1 + ((y2 - y1) / 2));

        if (Debug.debugging("arc")) {
            Debug.output("ArcCalc.generate: Center point for (" + x1 + ", "
                    + y1 + ") to (" + x2 + ", " + y2 + ") is (" + midPoint.x
                    + ", " + midPoint.y + ")");
        }

        double arccos = Math.cos(arcAngle);
        double arcRadius;

        if (arccos != 1.0) {
            arcRadius = distance / Math.sqrt(2.0 * (1.0 - Math.cos(arcAngle)));
        } else {
            arcRadius = distance / Math.sqrt(2.0);
        }

        if (Debug.debugging("arc")) {
            Debug.output("ArcCalc.generate: radius of arc = " + arcRadius);
        }

        // R' is the distance down the inverse negative slope of the
        // line that the focus of the arc is located.

        // x is the distance along the right leg of the arc that is
        // left over after Rcos(arcAngle) is subtracted from it, in
        // order to derive the angle of the straight line between the
        // two points.

        double x = arcRadius - arcRadius * Math.cos(arcAngle);

        double rPrime = (distance / 2.0)
                * (Math.sqrt(1.0 - Math.pow(x / distance, 2.0)))
                / Math.sin(arcAngle / 2.0);

        if (Debug.debugging("arc")) {
            Debug.output("ArcCalc.generate: rPrime = " + rPrime);
        }

        int direction = 1;
        if (arcUp)
            direction = -1;

        // arcCenter.x and arcCenter.y are the coordinates of the
        // focus of the Arc.
        arcCenter.x = midPoint.x
                + (direction * (int) (rPrime * Math.cos(inverseSlope)));
        arcCenter.y = midPoint.y
                + (direction * (int) (rPrime * Math.sin(inverseSlope)));

        if (Debug.debugging("arc")) {

            Debug.output("ArcCalc.generateArc: creating supplimental graphics list");
            arcGraphics = new OMGraphicList();

            double dist1 = Math.sqrt(Math.pow((double) (arcCenter.x - x1), 2.0)
                    + Math.pow((double) (arcCenter.y - y1), 2.0));
            double dist2 = Math.sqrt(Math.pow((double) (arcCenter.x - x2), 2.0)
                    + Math.pow((double) (arcCenter.y - y2), 2.0));

            Debug.output("ArcCalc.generate: Center focus for arc is ("
                    + arcCenter.x + ", " + arcCenter.y
                    + ") along slope line of "
                    + Math.toDegrees(inverseSlope) + " degrees).");
            Debug.output("ArcCalc.generate: Distance to point 1 from arc focus = "
                    + dist1
                    + "\n                    Distance to point 2 from arc focus = "
                    + dist2);

            // Let's highlight the end points.
            OMRect point1 = new OMRect(x1 - 1, y1 - 1, x1 + 1, y1 + 1);
            OMRect point2 = new OMRect(x2 - 1, y2 - 1, x2 + 1, y2 + 1);
            OMRect arcPoint = new OMRect(arcCenter.x - 1, arcCenter.y - 1, arcCenter.x + 1, arcCenter.y + 1);

            point1.setLinePaint(OMColor.red);
            point2.setLinePaint(OMColor.red);
            arcPoint.setLinePaint(OMColor.blue);
            arcGraphics.add(point1);
            arcGraphics.add(point2);
            arcGraphics.add(arcPoint);

            OMLine line1 = new OMLine(x1, y1, x2, y2);
            OMLine line2 = new OMLine(midPoint.x, midPoint.y, arcCenter.x, arcCenter.y);
            arcGraphics.add(line1);
            arcGraphics.add(line2);
        }

        int realCount = 0;

        // Figure out the arc extents for each endpoint. I think
        // it's easier to keep track of the angles if they are always
        // positive, and we always go from smaller to larger.
        double startSlope = getRealAngle((float)arcCenter.getX(), (float)arcCenter.getY(), x1, y1);
        double endSlope = getRealAngle((float)arcCenter.getX(), (float)arcCenter.getY(), x2, y2);

        double smallSlope, largeSlope;
        double angleIncrement;

        smallSlope = (startSlope > endSlope) ? endSlope : startSlope;
        largeSlope = (smallSlope == startSlope) ? endSlope : startSlope;

        // Have to make sure we take the smaller arc around the
        // circle.
        while (Math.abs(smallSlope - largeSlope) > Math.PI) {
            if (Math.abs(largeSlope - smallSlope - Math.PI) < .001) {
                // Catch 180 degree angles that are close enough...
                break;
            }

            Debug.message("arc",
                    "ArcCalc.generate: Modifying the starting slope.");
            double tmpSlope = smallSlope + MoreMath.TWO_PI;
            smallSlope = largeSlope;
            largeSlope = tmpSlope;
        }

        // Experienced some trouble with vertical and horizontal half
        // circles. This took care of that.
        if (MoreMath.approximately_equal(arcAngle, Math.PI) && arcUp) {
            Debug.message("arc",
                    "ArcCalc.generate: Modifying 180 angle points.");
            double tmpSlope = smallSlope + MoreMath.TWO_PI;
            smallSlope = largeSlope;
            largeSlope = tmpSlope;
        }

        // Figure out the angle increment for grabbing coordinates -
        // use the larger dimension of the arc end point differences.
        if (Math.abs(y2 - y1) < Math.abs(x2 - x1)) {
            angleIncrement = Math.PI / Math.abs(x2 - x1);
        } else {
            angleIncrement = Math.PI / Math.abs(y2 - y1);
        }

        int numPoints = (int) (Math.abs(smallSlope - largeSlope)
                / angleIncrement + 2);
        float[] xPoints = new float[numPoints];
        float[] yPoints = new float[numPoints];

        if (Debug.debugging("arc")) {
            Debug.output("ArcCalc.generate: angle to x1, y1 is " + startSlope
                    + " (" + Math.toDegrees(startSlope)
                    + " degrees), angle to x2, y2 is " + endSlope + " ("
                    + Math.toDegrees(endSlope) + " degrees)");

            Debug.output("ArcCalc.generate: Starting angle is " + smallSlope
                    + "(" + Math.toDegrees(smallSlope)
                    + " degrees), end angle is " + largeSlope + " ("
                    + Math.toDegrees(largeSlope)
                    + " degrees), incrementing by " + angleIncrement + " ("
                    + Math.toDegrees(angleIncrement) + " degrees)");
        }

        reversed = false;
        // Get the coordinates of the arc from the arc extents.
        while (smallSlope < largeSlope && realCount < numPoints) {

            xPoints[realCount] = arcCenter.x
                    + (int) (arcRadius * Math.cos(smallSlope));
            yPoints[realCount] = arcCenter.y
                    + (int) (arcRadius * Math.sin(smallSlope));

            if (realCount == 0 && xPoints[realCount] == x2) {
                Debug.message("arc", "ArcCalc: line reversed");
                reversed = true;
            }

            if (Debug.debugging("arc") && realCount == 0) {
                OMLine startLine = new OMLine(arcCenter.x, arcCenter.y, (int) xPoints[0], (int) yPoints[0]);
                startLine.setLinePaint(OMColor.white);
                arcGraphics.add(startLine);
            } else if (Debug.debugging("arcdetail")) {
                Debug.output("  angle " + smallSlope + " (" + smallSlope * 180
                        / Math.PI + " degrees)  = " + xPoints[realCount] + ", "
                        + yPoints[realCount]);
            }

            if (Math.abs(largeSlope - smallSlope - (arcAngle / 2.0)) < angleIncrement) {
                // Found the halfway point, mark it...
                peakPoint.setLocation(xPoints[realCount], yPoints[realCount]);
                Debug.message("arc", "ArcCalc: Found a midpoint.");
            }

            smallSlope += angleIncrement;
            realCount++;
        }

        // Give the coordinates to the OMLine.
        xpoints = new float[realCount];
        ypoints = new float[realCount];

                
        System.arraycopy(xPoints, 0, xpoints, 0, realCount);
        System.arraycopy(yPoints, 0, ypoints, 0, realCount);
    }

    /**
     * Given the straight line between two points, figure out the
     * angle, in radians, of that line in relation to the coordinate
     * system on the screen. Always returns a positive value, and the
     * angle is from point 1 to point 2.
     */
    protected double getRealAngle(float x1, float y1, float x2, float y2) {
        double angle = 0;

        double horDiff = (double) (x2 - x1);
        double vertDiff = (double) (y2 - y1);

        // If there is no horizontal difference, then it's pointing
        // up or down.
        if (horDiff == 0) {
            if (vertDiff > 0) {
                angle = MoreMath.HALF_PI;
            } else if (vertDiff < 0) {
                angle = -MoreMath.HALF_PI;
            }
        } else {
            angle = Math.atan(vertDiff / horDiff);

            // It's pointed in the wrong direction... fix it here.
            if (horDiff < 0) {
                angle += Math.PI;
            }
        }

        // Either way, I think we want to make the angle positive.
        while (angle < 0) {
            angle += MoreMath.TWO_PI;
        }
        return angle;
    }

    public float[] getXPoints() {
        return xpoints;
    }

    public float[] getYPoints() {
        return ypoints;
    }

    public void generate(Projection proj) {
        if (proj != null && arcGraphics != null) {
            arcGraphics.generate(proj);
        }
    }

    public void render(Graphics g) {
        if (arcGraphics != null) {
            Debug.output("OMLine rendering " + arcGraphics.size()
                    + " arcGraphics.");
            arcGraphics.render(g);
        }
    }

    public OMGraphicList getArcGraphics() {
        if (arcGraphics == null) {
            return new OMGraphicList();
        } else {
            return arcGraphics;
        }
    }

    public boolean getReversed() {
        return reversed;
    }
}