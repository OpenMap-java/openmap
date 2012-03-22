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

import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;

/**
 * Utility class that takes a generic component editor and wraps it in
 * a Dialog box. This includes adding the Frame and the "ok" and
 * "cancel" buttons. This class is used by the
 * {@link com.bbn.openmap.tools.beanbox.GenericPropertySheet}to show
 * a bean customizer.
 */
public class CustomizerDialog extends Dialog implements ActionListener {

    private Component body;
    private Button doneButton;
    private static int vPad = 5;
    private static int hPad = 4;

    /**
     * Constructor taking the parent frame, the customizer component
     * and the target bean as arguments.
     */
    public CustomizerDialog(Frame frame, Customizer customizer, Object target) {
        super(frame, customizer.getClass().getName(), true);
        setLayout(null);

        body = (Component) customizer;
        add(body);

        doneButton = new Button("Done");
        doneButton.addActionListener(this);
        add(doneButton);

        int x = frame.getLocation().x + 30;
        int y = frame.getLocation().y + 100;
        setLocation(x, y);

        setVisible(true);
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

    /**
     * Disposes this dialog.
     */
    public void actionPerformed(ActionEvent evt) {
        // Our "done" button got pushed.
        dispose();
    }

}