// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/ProjectionChangePolicy.java,v $
// $RCSfile: ProjectionChangePolicy.java,v $
// $Revision: 1.2 $
// $Date: 2003/08/28 22:25:04 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.policy;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;

public interface ProjectionChangePolicy {

    public OMGraphicHandlerLayer getLayer();

    public void projectionChanged(ProjectionEvent pe);

    public void workerComplete(OMGraphicList aList);
}
