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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/CSpecPalette.java,v $
// $RCSfile: CSpecPalette.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

/*  AWT & Schwing  */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bbn.openmap.corba.CSpecialist.ButtonBox;
import com.bbn.openmap.corba.CSpecialist.CheckBox;
import com.bbn.openmap.corba.CSpecialist.CheckButton;
import com.bbn.openmap.corba.CSpecialist.ListBox;
import com.bbn.openmap.corba.CSpecialist.RadioBox;
import com.bbn.openmap.corba.CSpecialist.Slider;
import com.bbn.openmap.corba.CSpecialist.TextBox;
import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist.WidgetType;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 * The CSpecialist palette
 */
public class CSpecPalette extends JPanel implements
        com.bbn.openmap.util.Palette {

    private String clientID = null;
    private CSpecLayer layer = null;
    private GridBagLayout gridbag = null;
    private GridBagConstraints constraints = null;

    protected CSpecPalette(UWidget[] widgets, String clientID, CSpecLayer layer) {

        super();
        this.clientID = clientID;
        this.layer = layer;

        //      setLayout(new GridLayout(widgets.length, 1));

        gridbag = new GridBagLayout();
        constraints = new GridBagConstraints();
        setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL; // fill
                                                          // horizontally
        constraints.gridwidth = GridBagConstraints.REMAINDER; //another
                                                              // row
        constraints.anchor = GridBagConstraints.EAST; // tack to the
                                                      // left edge
        //      constraints.weightx = 0.0;
        createPalette(widgets);
        setSize(150, 300);
    }

    /**
     *  
     */
    private void createPalette(UWidget[] widgets) {

        for (int i = 0; i < widgets.length; i++) {
            switch (widgets[i].discriminator().value()) {
            case WidgetType._WT_CheckBox: {
                final CheckBox cb = widgets[i].cb();
                final CheckButton[] buttons = cb.buttons();
                final String boxlabel = cb.label();
                ActionListener al = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int index = Integer.parseInt(e.getActionCommand(), 10);
                        buttons[index].checked = !buttons[index].checked;
                        if (Debug.debugging("cspec")) {
                            Debug.output("Checkbutton " + index + " is "
                                    + buttons[index].checked);
                        }
                        try {
                            cb.selected(boxlabel, buttons[index], clientID);
                        } catch (Throwable t) {
                            layer.forgetPalette();
                        }
                        layer.setPaletteIsDirty(true);
                    }
                };
                String[] buttonLabels = new String[buttons.length];
                boolean[] checked = new boolean[buttons.length];
                for (int j = 0; j < buttons.length; j++) {
                    buttonLabels[j] = buttons[j].button_label;
                    checked[j] = buttons[j].checked;
                }
                JPanel jp = PaletteHelper.createCheckbox(boxlabel,
                        buttonLabels,
                        checked,
                        al);
                gridbag.setConstraints(jp, constraints);
                add(jp);
                break;
            }
            case WidgetType._WT_RadioBox: {
                final RadioBox rb = widgets[i].rb();
                final String[] buttons = rb.buttons();
                final String boxlabel = rb.label();
                String selected_button = rb.selected_button();
                JPanel jp = PaletteHelper.createPaletteJPanel(boxlabel);
                ActionListener al = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int index = Integer.parseInt(e.getActionCommand(), 10);
                        if (Debug.debugging("cspec")) {
                            Debug.output("Radiobutton " + index
                                    + " is selected");
                        }
                        try {
                            rb.selected(boxlabel, buttons[index], clientID);
                        } catch (Throwable t) {
                            layer.forgetPalette();
                        }
                        layer.setPaletteIsDirty(true);
                    }
                };
                ButtonGroup group = new ButtonGroup();
                for (int j = 0; j < buttons.length; j++) {
                    JRadioButton jrb = new JRadioButton(buttons[j]);
                    if (buttons[j].equals(selected_button))
                        jrb.setSelected(true);
                    jrb.setActionCommand("" + j);//index of checked
                    jrb.addActionListener(al);
                    group.add(jrb);
                    jp.add(jrb);
                }
                gridbag.setConstraints(jp, constraints);
                add(jp);
                break;
            }
            case WidgetType._WT_Slider: {
                final Slider slide = widgets[i].slide();
                final String boxlabel = slide.label();
                short start = slide.start();
                short end = slide.end();
                short value = slide.value();
                boolean vertical = slide.vertical();
                JPanel jp = PaletteHelper.createPaletteJPanel(boxlabel);
                final JSlider jslide = new JSlider((vertical) ? JSlider.VERTICAL
                        : JSlider.HORIZONTAL, start,//min
                        end,//max
                        value);
                jslide.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        int val = jslide.getValue();
                        if (Debug.debugging("cspec")) {
                            Debug.output("Slider value is " + val);
                        }
                        try {
                            slide.set(boxlabel, (short) val, clientID);
                        } catch (Throwable t) {
                            layer.forgetPalette();
                        }
                        layer.setPaletteIsDirty(true);
                    }
                });
                jp.add(jslide);
                gridbag.setConstraints(jp, constraints);
                add(jp);
                break;
            }
            case WidgetType._WT_ButtonBox: {
                final ButtonBox bb = widgets[i].bb();
                final String[] buttons = bb.buttons();
                final String boxlabel = bb.label();
                JPanel jp = PaletteHelper.createPaletteJPanel(boxlabel);
                ActionListener al = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int index = Integer.parseInt(e.getActionCommand(), 10);
                        if (Debug.debugging("cspec")) {
                            Debug.output("ButtonBox " + index + " pressed");
                        }
                        try {
                            bb.pressed(boxlabel, buttons[index], clientID);
                        } catch (Throwable t) {
                            layer.forgetPalette();
                        }
                        layer.setPaletteIsDirty(true);
                    }
                };
                for (int j = 0; j < buttons.length; j++) {
                    JButton jb = new JButton(buttons[j]);
                    jb.setActionCommand("" + j);//index of checked
                    jb.addActionListener(al);
                    jp.add(jb);
                }
                gridbag.setConstraints(jp, constraints);
                add(jp);
                break;
            }
            case WidgetType._WT_ListBox: {
                final ListBox lb = widgets[i].lb();
                final String[] data = lb.contents();
                final String boxlabel = lb.label();
                final String selected = lb.highlighted_item();

                final JList jlist = new JList(data);
                jlist.setPreferredSize(new java.awt.Dimension(150, 150));
                JScrollPane jsp = new JScrollPane(jlist);
                jsp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        boxlabel));

                jlist.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        String sel = (String) (jlist.getSelectedValue());
                        if (Debug.debugging("cspec")) {
                            Debug.output("ListBox " + sel + " selected");
                        }
                        try {
                            lb.selected(boxlabel, sel, clientID);
                        } catch (Throwable t) {
                            layer.forgetPalette();
                        }
                        layer.setPaletteIsDirty(true);
                    }
                });

                for (int j = 0; j < data.length; j++) {
                    if (selected.equals(data[j])) {
                        jlist.getSelectedIndex();
                        break;
                    }
                }
                gridbag.setConstraints(jsp, constraints);
                add(jsp);
                break;
            }
            case WidgetType._WT_TextBox: {
                final TextBox tb = widgets[i].tb();
                final String contents = tb.contents();
                final String boxlabel = tb.label();

                final JTextArea jt = new JTextArea(contents);
                JScrollPane jsp = new JScrollPane(jt);
                jsp.setPreferredSize(new java.awt.Dimension(150, 150));
                jsp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        boxlabel));

                ActionListener al = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String cmd = e.getActionCommand();
                        if (cmd.equals("ok")) {
                            try {
                                Debug.message("cspec", "TextBox pressed.");
                                tb.pressed(boxlabel, jt.getText(), clientID);
                            } catch (Throwable t) {
                                layer.forgetPalette();
                            }
                            layer.setPaletteIsDirty(true);
                        } else if (cmd.equals("clear")) {
                            jt.setText("");
                        }
                    }
                };

                JPanel buttonsPanel = new JPanel();
                JButton clear = new JButton("Clear");
                clear.setActionCommand("clear");//index of checked
                clear.addActionListener(al);
                buttonsPanel.add(clear);
                JButton ok = new JButton("OK");
                ok.setActionCommand("ok");//index of checked
                ok.addActionListener(al);
                buttonsPanel.add(ok);

                gridbag.setConstraints(jsp, constraints);
                add(jsp);
                gridbag.setConstraints(buttonsPanel, constraints);
                add(buttonsPanel);
                break;
            }
            default:
                System.err.println("CSpecPalette(): unknown widget!");
                break;
            }
        }
    }
}