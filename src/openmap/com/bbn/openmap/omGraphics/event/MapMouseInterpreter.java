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
// $Revision: 1.2 $
// $Date: 2003/09/22 23:24:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;
import com.bbn.openmap.omGraphics.OMGraphic;

public interface MapMouseInterpreter {

    public boolean leftClick(MouseEvent me);

    public boolean leftClick(OMGraphic omg, MouseEvent me);

    public boolean leftClickOff(OMGraphic omg, MouseEvent me);

    public boolean rightClick(MouseEvent me);

    public boolean rightClick(OMGraphic omg, MouseEvent me);

    public boolean rightClickOff(OMGraphic omg, MouseEvent me);

    public boolean mouseOver(MouseEvent me);

    public boolean mouseOver(OMGraphic omg, MouseEvent me);

    public boolean mouseNotOver(OMGraphic omg);

    public boolean keyPressed(OMGraphic omg, int virtualKey);

    public void setGRP(GestureResponsePolicy urp);

    public GestureResponsePolicy getGRP();
}
