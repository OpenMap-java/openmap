// Bart 20060831 -> i18n

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
//$Revision: 1.6 $
//$Date: 2006-10-25 12:21:51 $
//$Author: jourquin $
//
//**********************************************************************

package com.bbn.openmap.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;

public class DimensionQueryPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9129280747489252728L;
    private JTextField hfield;
    private JTextField vfield;
    private JLabel htext;
    private JLabel vtext;
    private JLabel ptext1;
    private JLabel ptext2;
    public static final String HEIGHT_CMD = "DQP_HEIGHT_CMD";
    public static final String WIDTH_CMD = "DQP_WIDTH_CMD";
    //  I18N mechanism
    static I18n i18n = Environment.getI18n();

    public DimensionQueryPanel() {
        this(0, 0);
    }

    public DimensionQueryPanel(Dimension d) {
        this((int)d.getWidth(), (int)d.getHeight());
    }
    
    public DimensionQueryPanel(int width, int height) {

        htext = new JLabel(MessageFormat.format("{0}:",i18n.get(DimensionQueryPanel.class, "Width", "Width")));        
        htext.setHorizontalAlignment(SwingConstants.RIGHT);
        vtext = new JLabel(MessageFormat.format("{0}:",i18n.get(DimensionQueryPanel.class, "Height", "Height")));        
        vtext.setHorizontalAlignment(SwingConstants.RIGHT);
        hfield = new DimensionQueryField(Integer.toString(width), 5);
        hfield.setActionCommand(WIDTH_CMD);
        vfield = new DimensionQueryField(Integer.toString(height), 5);
        vfield.setActionCommand(HEIGHT_CMD);
        ptext1 = new JLabel(MessageFormat.format(" {0}", i18n.get(DimensionQueryPanel.class, "pixels", "pixels")));        
        ptext2 = new JLabel(MessageFormat.format(" {0}", i18n.get(DimensionQueryPanel.class, "pixels", "pixels")));
        layoutPanel();
    }

    public void setFieldWidth(int width) {
        hfield.setText(Integer.toString(width));
    }

    public int getFieldWidth() {
        return Integer.parseInt(hfield.getText());
    }

    public void setFieldHeight(int height) {
        vfield.setText(Integer.toString(height));
    }

    public int getFieldHeight() {
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
        setFieldWidth((int)d.getWidth());
        setFieldHeight((int)d.getHeight());
    }

    /**
     * @return Dimension of panel.
     */
    public Dimension getDimension() {
        return new Dimension(getFieldWidth(), getFieldHeight());
    }

    /**
     * Customized text field that sends an action event when focus is lost (in
     * addition to when the user hits "enter"). Fixes situations where listeners
     * were not getting dimension updates when user failed to hit enter (a
     * common occurrence).
     */
    private class DimensionQueryField extends JTextField implements FocusListener {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private DimensionQueryField(String text, int columns) {
            super(text, columns);
            this.addFocusListener(this);
        }

        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            fireActionPerformed();
        }
    }
}

