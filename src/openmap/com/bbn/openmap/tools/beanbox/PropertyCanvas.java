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

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyEditor;

import javax.swing.JDialog;

/**
 * Provides support to the GenericPropertySheet for displaying a
 * custom PropertyEditor.
 */
public class PropertyCanvas extends Canvas implements MouseListener {

    public PropertyCanvas(JDialog frame, PropertyEditor pe) {
        this.frame = frame;
        editor = pe;
        addMouseListener(this);
    }

    public void paint(Graphics g) {
        Rectangle box = new Rectangle(2, 2, getSize().width - 4, getSize().height - 4);
        editor.paintValue(g, box);
    }

    private boolean ignoreClick = false;

    public void mouseClicked(MouseEvent evt) {
        if (!ignoreClick) {
            try {
                ignoreClick = true;
                int x = frame.getLocation().x - 30;
                int y = frame.getLocation().y + 50;
                new PropertyDialog(frame, editor, x, y);
            } finally {
                ignoreClick = false;
            }
        }
    }

    public void mousePressed(MouseEvent evt) {}

    public void mouseReleased(MouseEvent evt) {}

    public void mouseEntered(MouseEvent evt) {}

    public void mouseExited(MouseEvent evt) {}

    private JDialog frame;
    private PropertyEditor editor;
}