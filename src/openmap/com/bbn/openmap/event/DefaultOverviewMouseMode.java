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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/DefaultOverviewMouseMode.java,v $
// $RCSfile: DefaultOverviewMouseMode.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import com.bbn.openmap.gui.OverviewMapHandler;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

import java.awt.event.*;
import java.awt.Point;

/**
 * A MouseMode that handles drawing a box, or clicking on a point, but
 * directs the updates to the ControlledMapSupport of the overview map
 * handler, instead of the overview MapBean, which would have been the
 * normal behavior.
 */
public class DefaultOverviewMouseMode extends NavMouseMode2 {

    OverviewMapHandler overviewMapHandler;

    /**
     * Construct a OverviewMouseMode. Sets the ID of the mode to the
     * modeID, the consume mode to true, and the cursor to the
     * crosshair.
     */
    public DefaultOverviewMouseMode(OverviewMapHandler omh) {
        super(true);
        overviewMapHandler = omh;
    }

    /**
     * Handle a mouseReleased MouseListener event. If there was no
     * drag events, or if there was only a small amount of dragging
     * between the occurence of the mousePressed and this event, then
     * recenter the source map. Otherwise we get the second corner of
     * the navigation rectangle and try to figure out the best scale
     * and location to zoom in to based on that rectangle.
     * 
     * @param e MouseEvent to be handled
     */
    public void mouseReleased(MouseEvent e) {
        if (Debug.debugging("mousemode")) {
            System.out.println(getID()
                    + "|DefaultOverviewMouseMode.mouseReleased()");
        }
        Object obj = e.getSource();
        if (!mouseSupport.fireMapMouseReleased(e)) {
            if (!(obj instanceof MapBean) || !autoZoom || point1 == null)
                return;
            MapBean map = (MapBean) obj;
            Projection projection = map.getProjection();

            synchronized (this) {
                point2 = e.getPoint();
                int dx = Math.abs(point2.x - point1.x);
                int dy = Math.abs(point2.y - point1.y);

                // Dont bother redrawing if the rectangle is too small
                if ((dx < 5) || (dy < 5)) {
                    // clean up the rectangle, since point2 has the
                    // old value.
                    paintRectangle(map, point1, point2);

                    // If rectangle is too small in both x and y then
                    // recenter the map
                    if ((dx < 5) && (dy < 5)) {
                        LatLonPoint llp = projection.inverse(e.getPoint());
                        overviewMapHandler.getControlledMapListeners()
                                .setCenter(llp);
                    }
                    return;
                }

                // Figure out the new scale
                com.bbn.openmap.LatLonPoint ll1 = projection.inverse(point1);
                com.bbn.openmap.LatLonPoint ll2 = projection.inverse(point2);

                float deltaDegrees;
                int deltaPix;
                dx = Math.abs(point2.x - point1.x);
                dy = Math.abs(point2.y - point1.y);

                if (dx < dy) {
                    float dlat = Math.abs(ll1.getLatitude() - ll2.getLatitude());
                    deltaDegrees = dlat * 2;
                    deltaPix = overviewMapHandler.getSourceMap()
                            .getProjection()
                            .getHeight();
                } else {
                    float dlon;
                    float lat1, lon1, lon2;

                    // point1 is to the right of point2. switch the
                    // LatLonPoints so that ll1 is west (left) of ll2.
                    if (point1.x > point2.x) {
                        lat1 = ll1.getLatitude();
                        lon1 = ll1.getLongitude();
                        ll1.setLatLon(ll2);
                        ll2.setLatLon(lat1, lon1);
                    }

                    lon1 = ll1.getLongitude();
                    lon2 = ll2.getLongitude();

                    // allow for crossing dateline
                    if (lon1 > lon2) {
                        dlon = (180 - lon1) + (180 + lon2);
                    } else {
                        dlon = lon2 - lon1;
                    }

                    deltaDegrees = dlon * 2;
                    deltaPix = overviewMapHandler.getSourceMap()
                            .getProjection()
                            .getWidth();
                }

                float pixPerDegree = ((Proj) projection).getPlanetPixelCircumference() / 360;
                float newScale = pixPerDegree / (deltaPix / deltaDegrees);

                // Figure out the center of the rectangle
                com.bbn.openmap.LatLonPoint center = projection.inverse(point1.x,
                        point1.y);

                // Set the parameters of the projection and then set
                // the projection of the map. This way we save having
                // the MapBean fire two ProjectionEvents.
                overviewMapHandler.getControlledMapListeners()
                        .setScale(newScale);
                overviewMapHandler.getControlledMapListeners()
                        .setCenter(center);
            }
            // reset the points
            point1 = null;
            point2 = null;
        }
    }

    /**
     * Given a MapBean, which provides the projection, and the
     * starting point of a box (pt1), look at pt2 to see if it
     * represents the ratio of the projection map size. If it doesn't,
     * provide a point that does.
     */
    protected Point getRatioPoint(MapBean map, Point pt1, Point pt2) {
        Projection proj = overviewMapHandler.getSourceMap().getProjection();
        float mapRatio = (float) proj.getHeight() / (float) proj.getWidth();

        float boxHeight = (float) (pt1.y - pt2.y);
        float boxWidth = (float) (pt1.x - pt2.x);
        float boxRatio = Math.abs(boxHeight / boxWidth);
        int isNegative = -1;
        if (boxRatio > mapRatio) {
            // box is too tall, adjust boxHeight
            if (boxHeight < 0)
                isNegative = 1;
            boxHeight = Math.abs(mapRatio * boxWidth);
            pt2.y = pt1.y + (isNegative * (int) boxHeight);

        } else if (boxRatio < mapRatio) {
            // box is too wide, adjust boxWidth
            if (boxWidth < 0)
                isNegative = 1;
            boxWidth = Math.abs(boxHeight / mapRatio);
            pt2.x = pt1.x + (isNegative * (int) boxWidth);
        }
        return pt2;
    }
}