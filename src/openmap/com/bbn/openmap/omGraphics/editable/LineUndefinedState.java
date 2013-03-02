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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/LineUndefinedState.java,v
// $
// $RCSfile: LineUndefinedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.bbn.openmap.omGraphics.EditableOMLine;
import com.bbn.openmap.omGraphics.EditableOMRect;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

public class LineUndefinedState extends ClckOrDrgUndefinedState {

    public LineUndefinedState(EditableOMLine eoml) {
        super(eoml);
        indexOfFirstPoint = EditableOMLine.STARTING_POINT_INDEX;
        indexOfSecondPoint = EditableOMLine.ENDING_POINT_INDEX;
    }
}