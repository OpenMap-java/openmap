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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/PaletteHelper.java,v $
// $RCSfile: PaletteHelper.java,v $
// $Revision: 1.6 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

/*  AWT & Schwing  */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameListener;

import com.bbn.openmap.Layer;

/**
 * Helps create palette GUI widgets.
 */
public class PaletteHelper {

    /**
     * This class has only static methods, so there isn't any need to
     * instantiate an object.
     */
    private PaletteHelper() {}

    /**
     * Create a panel containing a checkbox.
     * 
     * @param boxlabel the string to use for the box title
     * @param buttons the list of button names
     * @param checked the initial state of each checkbox item
     * @param al the actionlistener to invoke for a button. the
     *        actioncommand is set to a string containing the integer
     *        index of the button.
     * @return the JPanel the the buttons are placed in
     * @see javax.swing.AbstractButton#setActionCommand(String)
     * @see javax.swing.JCheckBox
     */
    public static JPanel createCheckbox(String boxlabel, String[] buttons,
                                        boolean[] checked, ActionListener al) {

        JPanel jp = createPaletteJPanel(boxlabel);
        for (int j = 0; j < buttons.length; j++) {
            JCheckBox jcb = new JCheckBox(buttons[j]);
            jcb.setActionCommand(Integer.toString(j));//index of
                                                      // checked
            if (al != null)
                jcb.addActionListener(al);
            jcb.setSelected(checked[j]);
            jp.add(jcb);
        }
        return jp;
    }

    /**
     * Create a panel containing a radiobox.
     * 
     * @param boxlabel the string to use for the box title
     * @param buttons the list of button names
     * @param initiallySelected the index of the initially selected
     *        button. -1 for no button initially selected.
     * @param al the actionlistener to invoke for a button. the
     *        actioncommand is set to a string containing the integer
     *        index of the button.
     * @return the JPanel the the buttons are placed in
     * @see javax.swing.AbstractButton#setActionCommand(String)
     * @see javax.swing.ButtonGroup
     */
    public static JPanel createRadiobox(String boxlabel, String[] buttons,
                                        int initiallySelected, ActionListener al) {

        JPanel jp = createPaletteJPanel(boxlabel);

        ButtonGroup buttongroup = new ButtonGroup();

        for (int j = 0; j < buttons.length; j++) {
            JRadioButton jrb = new JRadioButton(buttons[j]);
            jrb.setActionCommand("" + j);//index in list
            jp.add(jrb);
            buttongroup.add(jrb);
            if (al != null) {
                jrb.addActionListener(al);
            }
            if (j == initiallySelected) {
                jrb.setSelected(true);
            } else {
                jrb.setSelected(false);
            }
        }
        return jp;
    }

    /**
     * Create a panel that does horizontal layout
     * 
     * @param title the title of the panel (null allowed)
     * @return the panel that got created
     */
    public static JPanel createHorizontalPanel(String title) {
        JPanel panel = new JPanel();
        //      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setLayout(new GridLayout(1, 0));
        if (title != null) {
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    title));
        } else {
            panel.setBorder(BorderFactory.createEtchedBorder());
        }
        return panel;
    }

    /**
     * Create a panel that does vertical layout
     * 
     * @param title the title of the panel (null allowed)
     * @return the panel that got created
     */
    public static JPanel createVerticalPanel(String title) {
        JPanel panel = new JPanel();
        //      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setLayout(new GridLayout(0, 1));
        if (title != null) {
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    title));
        } else {
            panel.setBorder(BorderFactory.createEtchedBorder());
        }
        return panel;
    }

    /**
     * Create a panel with a border and title
     * 
     * @param title the title of the panel (null allowed)
     * @return the panel that got created
     */
    public static JPanel createPaletteJPanel(String title) {
        JPanel panel = new JPanel();

        if (title != null) {
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    title));
        } else {
            panel.setBorder(BorderFactory.createEtchedBorder());
        }

        panel.setLayout(new GridLayout(0, 1));
        return panel;
    }

    /**
     * Create and add a text entry field to a JComponent.
     * 
     * @param title the title of the frame
     * @param entry the name of the entry field
     * @param parent the component to add ourselves to
     * @return the text field
     */
    public static JTextField createTextEntry(String title, String entry,
                                             JComponent parent) {

        JPanel pal = PaletteHelper.createHorizontalPanel(null);
        JLabel label = new JLabel(title);
        label.setHorizontalTextPosition(JLabel.RIGHT);
        JTextField tf = new JTextField(entry);
        label.setLabelFor(tf);
        pal.add(label);
        pal.add(tf);
        parent.add(pal);
        return tf;
    }

    /**
     * Create and add a text area to a JComponent.
     * 
     * @param title the title of the frame
     * @param entry the name of the entry field
     * @param parent the component to add ourselves to
     * @param rows the number of rows
     * @param cols the number of columns
     * @return the text area
     */
    public static JTextArea createTextArea(String title, String entry,
                                           JComponent parent, int rows, int cols) {
        JPanel pal = PaletteHelper.createHorizontalPanel(null);
        JLabel label = new JLabel(title);
        label.setHorizontalTextPosition(JLabel.RIGHT);
        JTextArea ta = new JTextArea(entry, rows, cols);
        JScrollPane jsp = new JScrollPane(ta);
        label.setLabelFor(jsp);
        pal.add(label);
        pal.add(jsp);
        parent.add(pal);
        return ta;
    }

    /**
     * Get a layer's associated palette as an internal window
     * 
     * @param layer the layer to get the palette for
     * @param ifl the listener to associate with the palette
     * @return the frame that the palette is in
     */
    public static JInternalFrame getPaletteInternalWindow(
                                                          Layer layer,
                                                          InternalFrameListener ifl) {
        return getPaletteInternalWindow(layer.getGUI(), layer.getName()
                + " Palette", ifl);
    }

    /**
     * Get a layer's associated palette as a top-level window
     * 
     * @param layer the layer to get the palette for
     * @param cl the listener to associate with the palette
     * @return the frame that the palette is in
     */
    public static JFrame getPaletteWindow(Layer layer, ComponentListener cl) {
        Component layerGUI = getLayerGUIComponent(layer);

        JPanel dismissBox = new JPanel();
        dismissBox.setLayout(new BoxLayout(dismissBox, BoxLayout.X_AXIS));
        dismissBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        dismissBox.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        dismissBox.add(Box.createHorizontalGlue());
        JButton dismiss = new JButton("Close");
        dismissBox.add(dismiss);
        dismissBox.add(Box.createHorizontalGlue());

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        pane.add(layerGUI);
        pane.add(dismissBox);

        final JFrame frame = getPaletteWindow(pane, layer.getName()
                + " Palette", cl);

        dismiss.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });
        return frame;
    }

    /**
     * Get a Component that represents a layer's GUI.
     * 
     * @param layer the layer to get the palette for
     * @return the Component that represents the GUI for the layer.
     */
    public static Component getLayerGUIComponent(Layer layer) {

        Component pal = layer.getGUI();
        if (pal == null) {
            pal = new JLabel("No Palette");
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        p.add(pal);

        return p;
    }

    /**
     * Get a layer's associated palette as an internal window
     * 
     * @param gui the Component to place in the window
     * @param ifl the listener to associate with the palette
     * @return the frame that the palette is in
     */
    public static JInternalFrame getPaletteInternalWindow(
                                                          Component gui,
                                                          String windowName,
                                                          InternalFrameListener ifl) {

        JInternalFrame paletteWindow;

        // create the palette's scroll pane
        JScrollPane scrollPane = new JScrollPane(gui, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);

        // create the palette internal window
        paletteWindow = new JInternalFrame(windowName, true, //resizable
                true, //closable
                false, //maximizable
                true //iconifiable
        );

        // add a window listener that destroys the palette when
        // the window is closed
        paletteWindow.addInternalFrameListener(ifl);
        paletteWindow.getContentPane().add(scrollPane);
        paletteWindow.setOpaque(true);

        //layout all the components
        paletteWindow.pack();

        return paletteWindow;
    }

    /**
     * Get a layer's associated palette as a top-level window
     * 
     * @param gui the Component to place in the window
     * @param cl the listener to associate with the palette
     * @return the frame that the palette is in
     */
    public static JFrame getPaletteWindow(Component gui, String windowName,
                                          ComponentListener cl) {

        JScrollPane scrollPane = new JScrollPane(gui, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);

        // create the palette internal window
        JFrame paletteWindow = new JFrame(windowName);

        paletteWindow.addComponentListener(cl);
        paletteWindow.getContentPane().add(scrollPane);
        //layout all the components
        paletteWindow.pack();
        return paletteWindow;
    }

    /**
     * Get a layer's associated palette as a top-level window
     * 
     * @param gui the Component to place in the window
     * @param cl the listener to associate with the palette
     * @return the frame that the palette is in
     */
    public static JFrame getNoScrollPaletteWindow(Component gui,
                                                  String windowName,
                                                  ComponentListener cl) {

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        pane.add(gui);
        JFrame paletteWindow = new JFrame(windowName);
        paletteWindow.addComponentListener(cl);
        paletteWindow.getContentPane().add(pane);
        paletteWindow.pack();

        return paletteWindow;
    }
    
    public static JComponent getToolBarFill(int orientation) {
        Dimension dim = null;
        if (orientation == SwingConstants.HORIZONTAL) {
            dim = new Dimension(2, 1);
        } else {
            dim = new Dimension(1, 2);
        }
        return new Box.Filler(dim, dim, dim);
    }
}