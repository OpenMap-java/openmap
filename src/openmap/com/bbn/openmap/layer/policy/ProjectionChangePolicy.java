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
// $Revision: 1.1 $
// $Date: 2003/03/10 22:03:57 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.policy;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;

public interface ProjectionChangePolicy {

    public OMGraphicHandlerLayer getLayer();

    public void projectionChanged(ProjectionEvent pe);
}
