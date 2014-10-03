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

import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * A TemporalOMGeometryList object contains OMGeometries that change over time.
 * The time is milliseconds, generally from the unix epoch. This list can also hold regular
 * OMGeometries.
 */
public class TemporalOMGraphicList extends OMGraphicList implements
        TemporalOMGraphic {
    /**
     * Construct an TemporalOMGraphicList.
     */
    public TemporalOMGraphicList() {
        super(10);
    }

    /**
     * Construct an TemporalOMGraphicList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public TemporalOMGraphicList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Construct an TemporalOMGraphicList around a List of OMGraphics. The
     * TemporalOMGraphicList assumes that all the objects on the list are
     * OMGraphics, and never does checking. Live with the consequences if you
     * put other stuff in there.
     * 
     * @param list List of OMGraphics.
     */
    public TemporalOMGraphicList(List<OMGraphic> list) {
        super(list);
    }

    /**
     * Calls generate(proj, time) on temporal geometries, regular generate(proj)
     * on non-temporal OMGeometries.
     */
    public void generate(Projection proj, long time) {
        for (OMGraphic geom : this) {
            if (geom instanceof TemporalOMGraphic) {
                ((TemporalOMGraphic) geom).generate(proj, time);
            } else {
                geom.generate(proj);
            }
        }
    }
    
    /**
     * Add a location at a time to every TemporalGeometry contained within.
     */
    public void addTimeStamp(TemporalRecord timeStamp) {
        for (OMGraphic geom: this) {
            if (geom instanceof TemporalOMGraphic) {
                ((TemporalOMGraphic)geom).addTimeStamp(timeStamp);
            }
        }
    }

    /**
     * Remove a location at a certain time.
     */
    public boolean removeTimeStamp(TemporalRecord timeStamp) {
        for (OMGraphic geom: this) {
            if (geom instanceof TemporalOMGraphic) {
                ((TemporalOMGraphic)geom).removeTimeStamp(timeStamp);
            }
        }
        return true;
    }

    /**
     * Clear all time stamps.
     */
    public void clearTimeStamps() {
        for (OMGraphic geom: this) {
            if (geom instanceof TemporalOMGraphic) {
                ((TemporalOMGraphic)geom).clearTimeStamps();
            }
        }
    }

}
