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

import java.util.Iterator;
import java.util.TreeSet;

public class TemporalPointSupport extends TemporalSupport {

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

}
