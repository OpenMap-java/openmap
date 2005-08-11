// **********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: DimensionQueryPanel.java,v $
//$Revision: 1.2 $
//$Date: 2005/08/11 20:39:19 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class DimensionQueryPanel extends JPanel {

    private JTextField hfield;
    private JTextField vfield;
    private JLabel htext;
    private JLabel vtext;
    private JLabel ptext1;
    private JLabel ptext2;
    public static final String HEIGHT_CMD = "DQP_HEIGHT_CMD";
    public static final String WIDTH_CMD = "DQP_WIDTH_CMD";

    public DimensionQueryPanel() {
        this(0, 0);
    }

    public DimensionQueryPanel(Dimension d) {
        this((int)d.getWidth(), (int)d.getHeight());
    }
    
    public DimensionQueryPanel(int width, int height) {

        htext = new JLabel("Width: ");
        htext.setHorizontalAlignment(SwingConstants.RIGHT);
        vtext = new JLabel("Height: ");
        vtext.setHorizontalAlignment(SwingConstants.RIGHT);
        hfield = new JTextField(Integer.toString(width), 5);
        hfield.setActionCommand(WIDTH_CMD);
        vfield = new JTextField(Integer.toString(height), 5);
        vfield.setActionCommand(HEIGHT_CMD);
        ptext1 = new JLabel(" pixels");
        ptext2 = new JLabel(" pixels");
        layoutPanel();
    }

    public void setWidth(int width) {
        hfield.setText(Integer.toString(width));
    }

    public int getWidth() {
        return Integer.parseInt(hfield.getText());
    }

    public void setHeight(int height) {
        vfield.setText(Integer.toString(height));
    }

    public int getHeight() {
        return Integer.parseInt(vfield.getText());
    }

    public void addActionListener(ActionListener al) {
        hfield.addActionListener(al);
        vfield.addActionListener(al);
    }
    
    public void removeActionListener(ActionListener al) {
        hfield.removeActionListener(al);
        vfield.removeActionListener(al);
    }
    
    public void layoutPanel() {

        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gb);

        c.insets = new Insets(3, 3, 3, 3);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;

        gb.setConstraints(htext, c);
        add(htext);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1f;
        c.anchor = GridBagConstraints.WEST;

        gb.setConstraints(hfield, c);
        add(hfield);

        c.gridx = 2;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        gb.setConstraints(ptext1, c);
        add(ptext1);

        // Next row

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;

        gb.setConstraints(vtext, c);
        add(vtext);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1f;
        c.anchor = GridBagConstraints.WEST;

        gb.setConstraints(vfield, c);
        add(vfield);

        c.gridx = 2;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        gb.setConstraints(ptext2, c);
        add(ptext2);
    }

    /**
     * @param d
     */
    public void setDimension(Dimension d) {
        setWidth((int)d.getWidth());
        setHeight((int)d.getHeight());
    }

    /**
     * @return Dimension of panel.
     */
    public Dimension getDimension() {
        return new Dimension(getWidth(), getHeight());
    }
}

