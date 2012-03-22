/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditor;

/**
 * A class that provides support for a PropertyEditor that uses tags.
 */

public class PropertySelector extends Choice implements ItemListener {

    public PropertySelector(PropertyEditor pe) {
        editor = pe;
        String tags[] = editor.getTags();
        for (int i = 0; i < tags.length; i++) {
            addItem(tags[i]);
        }
        select(0);
        // This is a noop if the getAsText is not a tag.
        select(editor.getAsText());
        addItemListener(this);
    }

    public void itemStateChanged(ItemEvent evt) {
        String s = getSelectedItem();
        editor.setAsText(s);
    }

    public void repaint() {
        select(editor.getAsText());
    }

    PropertyEditor editor;
}