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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMArrowHead.java,v $
// $RCSfile: OMArrowHead.java,v $
// $Revision: 1.9 $
// $Date: 2005/12/08 21:12:11 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.Vector;

import com.bbn.openmap.omGraphics.geom.BasicGeometry;
import com.bbn.openmap.proj.DrawUtil;
import com.bbn.openmap.util.Debug;

/**
 * Basic implementation of arrowhead graphics. This class expects intimate
 * knowledge of an OMLine, and is used to add Arrowhead shapes to the actual
 * OMLine internal Shape object. You don't have to know about this class, just
 * call the OMLine methods that create arrowheads and the OMLine will take care
 * of the rest.
 */
public class OMArrowHead {

    public static final int ARROWHEAD_DIRECTION_FORWARD = 0;
    public static final int ARROWHEAD_DIRECTION_BACKWARD = 1;
    public static final int ARROWHEAD_DIRECTION_BOTH = 2;

    // These are base settings.
    protected static int DEFAULT_WINGTIP = 5;
    protected static int DEFAULT_WINGLENGTH = 20;

    protected Shape shape = null;
    protected int arrowDirectionType = ARROWHEAD_DIRECTION_FORWARD;
    protected int location = 100;
    protected int wingTip = 5;
    protected int wingLength = 20;

    public OMArrowHead(int arrowDirectionType, int location) {
        this(arrowDirectionType, location, DEFAULT_WINGTIP, DEFAULT_WINGLENGTH);
    }

    public OMArrowHead(int arrowDirectionType, int location, int wingtip,
            int winglength) {
        this.arrowDirectionType = arrowDirectionType;
        setLocation(location);
        this.wingTip = wingtip;
        this.wingLength = winglength;
    }

    public void generate(OMAbstractLine omal) {
        if (wingTip > 0 && wingLength > 0 && omal != null) {
            shape = createArrowHeads(arrowDirectionType,
                    location,
                    omal,
                    wingTip,
                    wingLength);
        } else {
            shape = null;
        }
    }

    public void render(Graphics g) {
        if (shape != null) {
            ((java.awt.Graphics2D) g).fill(shape);
        }
    }

    /**
     * Create an arrowhead for the provided line
     * 
     * @param arrowDirectionType ARROWHEAD_DIRECTION_FORWARD for the arrowhead
     *        pointing to the last coordinate of the OMLine,
     *        ARROWHEAD_DIRECTION_BACKWARD for the arrowhead pointing to the
     *        first coordinate in the OMLine, and ARROWHEAD_DIRECTION_BOTH for
     *        the arrowhead on both ends.
     * @param location A number between 0-100, reflecting the percentage of the
     *        line traversed before placing the arrowhead. For
     *        ARROWHEAD_DIRECTION_FORWARD and a location of 100, the arrowhead
     *        will be placed all the way at the end of the line. For a location
     *        of 50, the arrowhead will be placed in the middle of the line.
     * @param line OMLine to use to place arrowhead.
     * @return the GeneralPath for the arrowhead.
     */
    public static GeneralPath createArrowHeads(int arrowDirectionType,
                                               int location, OMAbstractLine line) {
        return createArrowHeads(arrowDirectionType,
                location,
                line,
                DEFAULT_WINGTIP,
                DEFAULT_WINGLENGTH);
    }

    /**
     * Create an arrowhead for the provided line
     * 
     * @param arrowDirectionType ARROWHEAD_DIRECTION_FORWARD for the arrowhead
     *        pointing to the last coordinate of the OMLine,
     *        ARROWHEAD_DIRECTION_BACKWARD for the arrowhead pointing to the
     *        first coordinate in the OMLine, and ARROWHEAD_DIRECTION_BOTH for
     *        the arrowhead on both ends.
     * @param location A number between 0-100, reflecting the percentage of the
     *        line traversed before placing the arrowhead. For
     *        ARROWHEAD_DIRECTION_FORWARD and a location of 100, the arrowhead
     *        will be placed all the way at the end of the line. For a location
     *        of 50, the arrowhead will be placed in the middle of the line.
     * @param line OMLine to use to place arrowhead.
     * @param wingTip Number of pixels to push the side of the arrowhead away
     *        from the line.
     * @param wingLength Number of pixels reflecting the arrowhead length.
     * @return the GeneralPath for the arrowhead.
     */
    public static GeneralPath createArrowHeads(int arrowDirectionType,
                                               int location,
                                               OMAbstractLine line,
                                               int wingTip, int wingLength) {

        Point[] locPoints = locateArrowHeads(arrowDirectionType, location, line);

        if (locPoints == null) {
            return null;
        }

        Stroke stroke = line.getStroke();
        float lineWidth = 1f;
        if (stroke instanceof BasicStroke) {
            lineWidth = ((BasicStroke) stroke).getLineWidth();
            wingTip += lineWidth;
            wingLength += lineWidth * 2;
        }

        GeneralPath shape = createArrowHead(locPoints[0],
                locPoints[1],
                wingTip,
                wingLength);

        int numLocPoints = locPoints.length;
        for (int i = 2; i < numLocPoints - 1; i += 2) {
            shape.append(createArrowHead(locPoints[i],
                    locPoints[i + 1],
                    wingTip,
                    wingLength), false);
        }

        return shape;
    }

    public static void addArrowHeads(int arrowDirectionType, int location,
                                     OMAbstractLine line) {

        Shape arrowHeads = createArrowHeads(arrowDirectionType, location, line);
        if (arrowHeads != null) {
            line.getShape().append(arrowHeads, false);
        }
    }

    protected static GeneralPath createArrowHead(Point from, Point to,
                                                 int wingTip, int wingLength) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        int dd = (int) DrawUtil.distance(to.x, to.y, from.x, from.y);

        if (dd < 6)
            dd = 6;

        int[] xpts = new int[3];
        int[] ypts = new int[3];
        xpts[0] = (int) (to.x + (dy * (wingTip) - dx * wingLength) / dd);
        ypts[0] = (int) (to.y + (dx * (-wingTip) - dy * wingLength) / dd);
        xpts[1] = (int) (to.x);
        ypts[1] = (int) (to.y);
        xpts[2] = (int) (to.x + (dy * (-wingTip) - dx * wingLength) / dd);
        ypts[2] = (int) (to.y + (dx * (wingTip) - dy * wingLength) / dd);

        return BasicGeometry.createShape(xpts, ypts, true);
    }

    /**
     * Create the ArrowHead objects for the lines, based on the settings. This
     * function is called while OMLine is being generated. User's don't need to
     * call this function. In fact, it assumes that generate() has been called
     * (or is being called) on the OMLine. It adds the ArrowHeads to the
     * GeneralPath Shape object.
     */
    protected static Point[] locateArrowHeads(int arrowDirection,
                                              int arrowLocation,
                                              OMAbstractLine line) {

        // NOTE: xpoints[0] refers to the original copy of the
        // xpoints, as opposed to the [1] copy, which gets used when the line
        // needs to wrap around the screen and show up on the other
        // side. Might have to think about the [1] points, and adding
        // a arrowhead there if it shows up in the future.

        if (line.xpoints == null || line.xpoints.length == 0
                || line.xpoints[0].length == 0) {
            // line doesn't know where it is...
            return null;
        }

        int pointIndex = line.xpoints[0].length - 1;
        if (Debug.debugging("arrowheads")) {
            Debug.output("createArrowHeads(): Number of points = " + pointIndex);
        }

        int drawingLinetype = OMLine.STRAIGHT_LINE; // default
        if (pointIndex > 1) {
            drawingLinetype = OMLine.CURVED_LINE;
        }

        // Used as the index for points in the xy point array to use
        // as anchors for the arrowheads
        int[] end = new int[2];
        int[] start = new int[2];
        end[0] = pointIndex;
        start[0] = 0;
        end[1] = 0;
        start[1] = pointIndex;

        // better names:
        int origEnd = pointIndex;
        int origStart = 0;

        int numArrows = 1; // default
        if (arrowDirection == OMArrowHead.ARROWHEAD_DIRECTION_BOTH) {
            numArrows = 2;
        }

        // one for the start and end of each arrowhead (there could be
        // two)
        Point sPoint1 = new Point();
        Point ePoint1 = new Point();
        Point sPoint2 = new Point();
        Point ePoint2 = new Point();

        // do we have to reverse the arrows?

        if (line instanceof OMLine) {
            OMLine omLine = (OMLine) line;
            if (omLine.arc != null && omLine.arc.getReversed() == true) {
                if (arrowDirection == OMArrowHead.ARROWHEAD_DIRECTION_FORWARD) {
                    arrowDirection = OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD;
                } else if (arrowDirection == OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD) {
                    arrowDirection = OMArrowHead.ARROWHEAD_DIRECTION_FORWARD;
                }
            }
        }

        Vector<Point> pointVec = new Vector<Point>();
        
        // The for loop is needed in case the projection library
        // created several projected versions of the line, those used
        // for wrapping around to the other side of the map.
        for (int lineNum = 0; lineNum < line.xpoints.length; lineNum++) {
            int[] xpoints = line.xpoints[lineNum];
            int[] ypoints = line.ypoints[lineNum];

            switch (drawingLinetype) {

            case OMLine.STRAIGHT_LINE:
                Debug.message("arrowheads",
                        "createArrowHeads(): Inside x-y space");
                int newEndX;
                int newEndY;
                int dx;
                int dy;
                float dd;

                // backwards arrow

                if (needBackwardArrow(arrowDirection)) {

                    // need to have the newEndX/Y point at the
                    // original start.

                    newEndX = xpoints[origStart];
                    newEndY = ypoints[origStart];

                    if (arrowLocation != 100) {
                        // find out where the location should be, but
                        // in
                        // reverse.
                        dx = xpoints[origStart] - xpoints[origEnd];
                        dy = ypoints[origStart] - ypoints[origEnd];
                        int offset = 0;
                        // Straight up or down
                        if (dx == 0) {
                            // doesn't matter, start and end the same
                            newEndX = xpoints[origEnd];
                            // calculate the percentage from start of line
                            offset = (int) ((float) dy * (arrowLocation / 100.0f));
                            // set the end at the begining...
                            newEndY = ypoints[origEnd] + offset;

                        } else {

                            dd = Math.abs((float) dy / (float) dx);
                            // If the line moves more x than y
                            if (Math.abs(dx) > Math.abs(dy)) {
                                // set the x
                                newEndX = xpoints[origEnd]
                                        + (int) ((float) dx * (arrowLocation / 100.0));
                                // find the y for that x and set that
                                newEndY = ypoints[origEnd];
                                offset = (int) ((float) Math.abs(xpoints[origEnd]
                                        - newEndX) * dd);

                                if (dy < 0) {
                                    newEndY -= offset;
                                } else {
                                    newEndY += offset;
                                }

                            } else {
                                // switch everything...set y end
                                newEndY = ypoints[origEnd]
                                        + (int) ((float) dy * (arrowLocation / 100.0));
                                // initialize the x to beginning
                                newEndX = xpoints[origEnd];
                                // calculate the difference x has to
                                // move based on y end
                                offset = (int) ((float) Math.abs(ypoints[origEnd]
                                        - newEndY) / dd);
                                // set the end
                                if (dx < 0) {
                                    newEndX -= offset;
                                } else {
                                    newEndX += offset;
                                }
                            }

                        }

                    } // if (arrowLocation != 100)

                    if (start[1] < 0) {
                        start[1] = 0;
                    }

                    // which point do we copy to?
                    if (numArrows == 2) {
                        // we copy the backwards arrow to
                        // sPoint2/ePoint2

                        sPoint2.x = xpoints[origEnd];
                        sPoint2.y = ypoints[origEnd];
                        ePoint2.x = newEndX;
                        ePoint2.y = newEndY;

                    } else {
                        // we copy the backwards arrow to
                        // sPoint1/ePoint1

                        sPoint1.x = xpoints[origEnd];
                        sPoint1.y = ypoints[origEnd];
                        ePoint1.x = newEndX;
                        ePoint1.y = newEndY;

                    }

                } // end if needBackwardArrow.

                if (needForwardArrow(arrowDirection)) {

                    newEndX = xpoints[origEnd];
                    newEndY = ypoints[origEnd];

                    if (arrowLocation != 100) {
                        // find out where the location should be.
                        dx = xpoints[origEnd] - xpoints[origStart];
                        dy = ypoints[origEnd] - ypoints[origStart];
                        int offset = 0;
                        // Straight up or down
                        if (dx == 0) {
                            // doesn't matter, start and end the same
                            newEndX = xpoints[origStart];
                            // calculate the percentage from start of
                            // line
                            offset = (int) ((float) dy * (arrowLocation / 100.0f));
                            // set the end at the begining...
                            newEndY = ypoints[origStart] + offset;

                        } else {

                            dd = Math.abs((float) dy / (float) dx);
                            // If the line moves more x than y
                            if (Math.abs(dx) > Math.abs(dy)) {
                                // set the x
                                newEndX = xpoints[origStart]
                                        + (int) ((float) dx * (arrowLocation / 100.0f));
                                // find the y for that x and set that
                                newEndY = ypoints[origStart];
                                offset = (int) ((float) Math.abs(xpoints[origStart]
                                        - newEndX) * dd);

                                if (dy < 0) {
                                    newEndY -= offset;
                                } else {
                                    newEndY += offset;
                                }

                            } else {
                                // switch everything...set y end
                                newEndY = ypoints[origStart]
                                        + (int) ((float) dy * (arrowLocation / 100.0));
                                // initialize the x to beginning
                                newEndX = xpoints[origStart];
                                // calculate the difference x has to
                                // move
                                // based on y end
                                offset = (int) ((float) Math.abs(ypoints[origStart]
                                        - newEndY) / dd);
                                // set the end
                                if (dx < 0) {
                                    newEndX -= offset;
                                } else {
                                    newEndX += offset;
                                }
                            }

                        }

                    } // end if (arrowLocation != 100)

                    // finally, copy the results to sPoint1/ePoint1

                    // no longer needed: if (start[0] < 0) { start[0]
                    // = 0;
                    // }

                    sPoint1.x = xpoints[origStart];
                    sPoint1.y = ypoints[origStart];
                    ePoint1.x = newEndX;
                    ePoint1.y = newEndY;

                }

                break;
            case OMLine.CURVED_LINE:
                Debug.message("arrowheads",
                        "createArrowHeads(): Curved line arrowhead");

                if (needBackwardArrow(arrowDirection)) {

                    Debug.message("arrowheads",
                            "createArrowHeads(): direction backward and");

                    // compute the backward index....
                    int bindex = pointIndex
                            - (int) ((float) pointIndex * (float) (arrowLocation / 100.0));
                    if (bindex == 0) {
                        bindex = 1;
                    }

                    if (numArrows == 2) {
                        // copy it to s/ePoint2

                        sPoint2.x = xpoints[bindex];
                        sPoint2.y = ypoints[bindex];
                        ePoint2.x = xpoints[bindex - 1];
                        ePoint2.y = ypoints[bindex - 1];
                    } else {
                        // copy it to s/ePoint1

                        sPoint1.x = xpoints[bindex];
                        sPoint1.y = ypoints[bindex];
                        ePoint1.x = xpoints[bindex - 1];
                        ePoint1.y = ypoints[bindex - 1];

                    }

                } // end if (needBackwardArrow(arrowDirection))

                if (needForwardArrow(arrowDirection)) {

                    int findex = (int) ((float) pointIndex * (float) (arrowLocation / 100.0));
                    if (findex == pointIndex) {
                        findex = findex - 1;
                    }

                    sPoint1.x = xpoints[findex];
                    sPoint1.y = ypoints[findex];
                    ePoint1.x = xpoints[findex + 1];
                    ePoint1.y = ypoints[findex + 1];

                } // end if (needForwardArrow(arrowDirection))

                break;
            } // end switch(drawingLinetype)
            
            pointVec.add((Point) sPoint1.clone());
            pointVec.add((Point) ePoint1.clone());
            
            if (numArrows > 1) {
                pointVec.add((Point) sPoint2.clone());
                pointVec.add((Point) ePoint2.clone());
            }
            
        }

        Point[] ret = new Point[pointVec.size()];
        int i = 0;
        for (Point point : pointVec) {
            ret[i++] = point;
        }
        
        return ret;
    }

    private static boolean needBackwardArrow(int arrowDir) {
        return (arrowDir == ARROWHEAD_DIRECTION_BACKWARD || arrowDir == ARROWHEAD_DIRECTION_BOTH);
    }

    private static boolean needForwardArrow(int arrowDir) {
        return (arrowDir == ARROWHEAD_DIRECTION_FORWARD || arrowDir == ARROWHEAD_DIRECTION_BOTH);
    }

    public int getArrowDirectionType() {
        return arrowDirectionType;
    }

    public void setArrowDirectionType(int arrowDirectionType) {
        this.arrowDirectionType = arrowDirectionType;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        if (location < 1)
            this.location = 1;
        else if (location > 100)
            this.location = 100;
        else
            this.location = location;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public int getWingLength() {
        return wingLength;
    }

    public void setWingLength(int wingLength) {
        this.wingLength = wingLength;
    }

    public int getWingTip() {
        return wingTip;
    }

    public void setWingTip(int wingTip) {
        this.wingTip = wingTip;
    }

}