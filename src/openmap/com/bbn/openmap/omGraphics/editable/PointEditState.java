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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/PointEditState.java,v $
// $RCSfile: PointEditState.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.editable;


import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.layer.util.stateMachine.*;
import com.bbn.openmap.util.Debug;

public class PointEditState extends GraphicEditState implements EOMGEditState {

    public PointEditState(EditableOMPoint eomp) {
        super(eomp);
    }

    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "PointStateMachine|edit state|mouseReleased");
        graphic.setGrabPoints(); // Needed for OMPoints that are point and clicked for placement.
        return super.mouseReleased(e);
    }
}










