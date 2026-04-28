//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.time;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.TreeSet;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class TemporalPointSupport extends TemporalSupport {

    protected int renderType = OMGraphic.RENDERTYPE_UNKNOWN;

    public TemporalPointSupport(int renderType) {
        this.renderType = renderType;
    }

    @Override
    public TreeSet<TemporalPoint> createTemporalSet() {
        if (temporals == null) {
            temporals = new TreeSet<TemporalPoint>(new TemporalRecordComparator());
        }
        return (TreeSet<TemporalPoint>) temporals;
    }

    @Override
    public Iterator<TemporalPoint> iterator() {
        return (Iterator<TemporalPoint>) temporals.iterator();
    }

    /**
     * Just returns the TemporalRecord that is closes to the current time.
     * Assumes neither previous or next are null.
     * 
     * @param time the current time.
     * @param previous TemporalRecord that occurred before current time.
     * @param next TemporalRecord that occurred after current time.
     * @return closest one.
     */
    protected TemporalRecord interpolate(long time, TemporalRecord previous,
                                         TemporalRecord next) {
        TemporalRecord ret = null;
        double top = time - previous.getTime();
        double bottom = next.getTime() - previous.getTime();
        double percent = top / bottom;

        switch (renderType) {
        case OMGraphic.RENDERTYPE_XY:
            // TODO - simple geometric interpolation instead of choosing closest
            // record.
            ret = super.interpolate(time, previous, next);
            break;
        default:
            // assume lat/lons
            Point2D prevPt = ((TemporalPoint) previous).getLocation();
            Point2D nextPt = ((TemporalPoint) next).getLocation();
            LatLonPoint prevLL;
            LatLonPoint nextLL;
            if (prevPt instanceof LatLonPoint) {
                prevLL = (LatLonPoint) prevPt;
            } else {
                prevLL = new LatLonPoint.Double(prevPt);
            }
            if (nextPt instanceof LatLonPoint) {
                nextLL = (LatLonPoint) nextPt;
            } else {
                nextLL = new LatLonPoint.Double(nextPt);
            }

            double[] pts = GreatCircle.greatCircle(prevLL.getRadLat(),
                    prevLL.getRadLon(),
                    nextLL.getRadLat(),
                    nextLL.getRadLon(),
                    100,
                    true);

            int index = (int) (2 * Math.floor(100 * percent));

            ret = new TemporalPoint(new LatLonPoint.Double(pts[index], pts[index + 1], true), time);
        }

        return ret;
    }
}
