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

import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyEditor;

/**
 * A class that provides support for a PropertyEditor that displays a
 * text property.
 */
public class PropertyText extends TextField implements KeyListener,
        FocusListener {

    public PropertyText(PropertyEditor pe) {
        super(pe.getAsText());
        editor = pe;
        addKeyListener(this);
        addFocusListener(this);
    }

    public void repaint() {
        setText(editor.getAsText());
    }

    protected void updateEditor() {
        try {
            editor.setAsText(getText());
        } catch (IllegalArgumentException ex) {
            // Quietly ignore.
        }
    }

    //----------------------------------------------------------------------
    // Focus listener methods.

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
        updateEditor();
    }

    //----------------------------------------------------------------------
    // Keyboard listener methods.

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            updateEditor();
        }
    }

    public void keyPressed(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    //----------------------------------------------------------------------
    private PropertyEditor editor;
}