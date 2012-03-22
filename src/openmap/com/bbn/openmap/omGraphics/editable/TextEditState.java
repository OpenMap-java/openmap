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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/TextEditState.java,v
// $
// $RCSfile: TextEditState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMText;
import com.bbn.openmap.util.Debug;

public class TextEditState extends GraphicEditState implements EOMGEditState {

    public TextEditState(EditableOMText eomt) {
        super(eomt);
    }

    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "TextStateMachine|edit state|mouseReleased");
        graphic.setGrabPoints(); // Needed for OMText that are point
        // and clicked for placement.
        return super.mouseReleased(e);
    }
}

