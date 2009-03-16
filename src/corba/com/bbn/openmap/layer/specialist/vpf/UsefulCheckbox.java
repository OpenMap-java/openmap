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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/vpf/UsefulCheckbox.java,v $
// $RCSfile: UsefulCheckbox.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.vpf;

import com.bbn.openmap.corba.CSpecialist.CheckButton;
import com.bbn.openmap.layer.specialist.SCheckBox;

/**
 * Class SCheckBox is a specialist palette widget. It is a box of
 * buttons where none, one or all can be marked as active.
 */
public class UsefulCheckbox extends SCheckBox {

    public UsefulCheckbox(String label, CheckButton[] buttons) {
        super(label, buttons);
    }

    public void selected(java.lang.String box_label,
                         com.bbn.openmap.corba.CSpecialist.CheckButton button,
                         java.lang.String uniqueID) {

        for (int i = 0; i < buttons_.length; i++) {
            if (buttons_[i].button_label.compareTo(button.button_label) == 0)
                buttons_[i].checked = !buttons_[i].checked;
        }
    }
}