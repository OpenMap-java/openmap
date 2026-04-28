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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/RectUndefinedState.java,v
// $
// $RCSfile: RectUndefinedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import com.bbn.openmap.omGraphics.EditableOMRect;

public class RectUndefinedState extends ClckOrDrgUndefinedState {

    public RectUndefinedState(EditableOMRect eomr) {
        super(eomr);
        indexOfFirstPoint = EditableOMRect.NW_POINT_INDEX;
        indexOfSecondPoint = EditableOMRect.SE_POINT_INDEX;
    }

}