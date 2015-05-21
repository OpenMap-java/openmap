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

import java.util.Comparator;

public class TemporalRecordComparator implements Comparator<TemporalRecord> {

    public int compare(TemporalRecord o1, TemporalRecord o2) {
        long time1 = o1.getTime();
        long time2 = o2.getTime();
        if (time1 < time2) {
            return -1;
        } else if (time1 > time2) {
            return 1;
        } else {
            return 0;
        }
    }

}
