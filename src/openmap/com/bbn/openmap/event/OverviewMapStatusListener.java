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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/OverviewMapStatusListener.java,v
// $
// $RCSfile: OverviewMapStatusListener.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import com.bbn.openmap.proj.Projection;

public interface OverviewMapStatusListener {
    public void setSourceMapProjection(Projection proj);
}

