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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;

import javax.swing.JDialog;

/**
 * Provides support to the GenericPropertySheet for displaying a
 * custom PropertyEditor.
 */
class PropertyDialog extends Dialog implements ActionListener {

    private Button doneButton;
    private Component body;
    private final static int vPad = 5;
    private final static int hPad = 4;

    PropertyDialog(JDialog frame, PropertyEditor pe, int x, int y) {
        super(frame, pe.getClass().getName(), true);
        setLayout(null);
        body = pe.getCustomEditor();

        if (body instanceof Window) {
            if (!((Container) body).isVisible())
                ((Container) body).setVisible(true);
        } else {
            setLayout(new BorderLayout());
            add(body, BorderLayout.CENTER);
            doneButton = new Button("Done");
            doneButton.addActionListener(this);
            add(doneButton, BorderLayout.SOUTH);
            setLocation(x, y);
            setVisible(true);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        // Button down.
        dispose();
    }

    public void doLayout() {
        Insets ins = getInsets();
        Dimension bodySize = body.getPreferredSize();
        Dimension buttonSize = doneButton.getPreferredSize();

        int width = ins.left + 2 * hPad + ins.right + bodySize.width;
        int height = ins.top + 3 * vPad + ins.bottom + bodySize.height
                + buttonSize.height;

        body.setBounds(ins.left + hPad,
                ins.top + vPad,
                bodySize.width,
                bodySize.height);

        doneButton.setBounds((width - buttonSize.width) / 2,
                ins.top + (2 * hPad) + bodySize.height,
                buttonSize.width,
                buttonSize.height);

        setSize(width, height);

    }

}

