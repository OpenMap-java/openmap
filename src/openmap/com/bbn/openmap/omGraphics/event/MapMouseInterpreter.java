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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/MapMouseInterpreter.java,v $
// $RCSfile: MapMouseInterpreter.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/08 22:32:19 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import com.bbn.openmap.event.MapMouseListener;

public interface MapMouseInterpreter extends MapMouseListener {

    public void setGRP(GestureResponsePolicy urp);

    public GestureResponsePolicy getGRP();
}
