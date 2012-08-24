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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/BasicStrokeEditorMenu.java,v $
// $RCSfile: BasicStrokeEditorMenu.java,v $
// $Revision: 1.8 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics;

/*  Java Core  */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.util.PropUtils;
import javax.swing.JComponent;

/**
 */
public class BasicStrokeEditorMenu extends JPopupMenu {

    protected BasicStroke basicStroke = null;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected float width; // must be >= 0f
    protected int endCaps; //CAP_ROUND, CAP_BUTT, CAP_SQUARE
    protected int lineJoins; //JOIN_BEVEL, JOIN_MITER, JOIN_ROUND
    protected float miterLimit; // 10f default, must be >= 1f
    protected float[] dash;
    protected float dashPhase;

    private I18n i18n = Environment.getI18n();

    protected JButton launchButton;

    public BasicStrokeEditorMenu() {
        this(new BasicStroke(1f));
    }

    public BasicStrokeEditorMenu(BasicStroke bs) {
        if (bs != null) {
            basicStroke = bs;
        } else {
            basicStroke = new BasicStroke(1f);
        }

        setBasicStroke(basicStroke);
    }

    public void setLaunchButton(JButton lb) {
        launchButton = lb;
    }

    public JButton getLaunchButton() {
        if (launchButton == null) {
            float buttonHeight = 20;
            ImageIcon icon = createIcon(getBasicStroke(),
                    50,
                    (int) buttonHeight,
                    true);
            launchButton = new JButton(icon);
            launchButton.setToolTipText(i18n.get(BasicStrokeEditorMenu.class, "Modify_Line_Parameters", "Modify Line Parameters"));
            launchButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JButton button = getLaunchButton();
                    JPopupMenu popup = new JPopupMenu();
                    setGUI(popup);
                    popup.show(button, button.getWidth(), 0);
                }
            });
        }
        return launchButton;
    }

    public void resetStroke() {
        BasicStroke oldStroke = basicStroke;
        setMiterLimit(miterLimit);
        basicStroke = new BasicStroke(width, endCaps, lineJoins, miterLimit, dash, dashPhase);
        if (launchButton != null) {
            launchButton.setIcon(createIcon(basicStroke, 50, 20, true));
        }
        pcs.firePropertyChange("line", oldStroke, basicStroke);
    }

    public void show(java.awt.Component invoker, int x, int y) {
        this.removeAll();
        setGUI(this);
        super.show(invoker, x, y);
    }

    public JComponent setGUI(JComponent popup) {

        //////// Line Cap
        JMenu capMenu = new JMenu(i18n.get(BasicStrokeEditorMenu.class, "Cap_Decoration", "Cap Decoration"));

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String command = ae.getActionCommand();
                try {
                    setEndCaps(Integer.parseInt(command));
                    resetStroke();
                } catch (NumberFormatException e) {
                }
            }
        };

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem button = new JRadioButtonMenuItem(i18n.get(BasicStrokeEditorMenu.class, "Butt", "Butt"), endCaps == BasicStroke.CAP_BUTT);
        button.setActionCommand(String.valueOf(BasicStroke.CAP_BUTT));
        group.add(button);
        button.addActionListener(listener);
        capMenu.add(button);

        button = new JRadioButtonMenuItem(i18n.get(BasicStrokeEditorMenu.class, "Round", "Round"), endCaps == BasicStroke.CAP_ROUND);
        button.setActionCommand(String.valueOf(BasicStroke.CAP_ROUND));
        group.add(button);
        button.addActionListener(listener);
        capMenu.add(button);

        button = new JRadioButtonMenuItem(i18n.get(BasicStrokeEditorMenu.class, "Square", "Square"), endCaps == BasicStroke.CAP_SQUARE);
        button.setActionCommand(String.valueOf(BasicStroke.CAP_SQUARE));
        group.add(button);
        button.addActionListener(listener);
        capMenu.add(button);

        //////// Line Joins

        JMenu joinMenu = new JMenu(i18n.get(BasicStrokeEditorMenu.class, "Joint_Decoration", "Joint Decoration"));

        listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String command = ae.getActionCommand();
                try {
                    setLineJoins(Integer.parseInt(command));
                    resetStroke();
                } catch (NumberFormatException e) {
                }
            }
        };

        group = new ButtonGroup();
        button = new JRadioButtonMenuItem(i18n.get(BasicStrokeEditorMenu.class, "Miter", "Miter"), lineJoins == BasicStroke.JOIN_MITER);
        button.setActionCommand(String.valueOf(BasicStroke.JOIN_MITER));
        group.add(button);
        button.addActionListener(listener);
        joinMenu.add(button);

        button = new JRadioButtonMenuItem(i18n.get(BasicStrokeEditorMenu.class, "Round", "Round"), lineJoins == BasicStroke.JOIN_ROUND);
        button.setActionCommand(String.valueOf(BasicStroke.JOIN_ROUND));
        group.add(button);
        button.addActionListener(listener);
        joinMenu.add(button);

        button = new JRadioButtonMenuItem(i18n.get(BasicStrokeEditorMenu.class, "Bevel", "Bevel"), lineJoins == BasicStroke.JOIN_BEVEL);
        button.setActionCommand(String.valueOf(BasicStroke.JOIN_BEVEL));
        group.add(button);
        button.addActionListener(listener);
        joinMenu.add(button);

        //////// Line Width

        JMenu widthMenu = new JMenu(i18n.get(BasicStrokeEditorMenu.class, "Line_Width", "Line_Width"));

        listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String command = ae.getActionCommand();
                try {
                    setStrokeWidth((float) Integer.parseInt(command));
                    resetStroke();
                } catch (NumberFormatException e) {
                }
            }
        };

        group = new ButtonGroup();
        ImageIcon ii;
        int i;
        for (i = 1; i < 13; i++) {
            ii = createIcon(new BasicStroke(i), 50, 20, true);
            button = new JRadioButtonMenuItem(" ", ii, (int) width == i);//without the space as a parameter these instances look strange with some Look&Feel
            button.setActionCommand(String.valueOf(i));
            group.add(button);
            button.addActionListener(listener);
            button.setMargin( new java.awt.Insets(0,10,0,10));
            button.setPreferredSize(new java.awt.Dimension(70,20));
            widthMenu.add(button);
        }

        //////// Dash Pattern

        JMenu dashMenu = new JMenu(i18n.get(BasicStrokeEditorMenu.class, "Dash_Pattern", "Dash Pattern"));

        listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String command = ae.getActionCommand();
                try {
                    setDash(stringToDashArray(command));
                    resetStroke();
                } catch (NumberFormatException e) {
                }
            }
        };

        group = new ButtonGroup();

        String[] patterns = new String[] { NONE, "1.0 3.0", "3.0 3.0", "12.0 10.0",
                "12.0 10.0 6.0 10.0", "20.0 10.0", "20.0 10.0 6.0 10.0", "20.0 10.0 6.0 10.0 6.0 10.0" };

        String currentDash = dashArrayToString(getDash());
        for (i = 0; i < patterns.length; i++) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, stringToDashArray(patterns[i]), 0.0f);
            ii = createIcon(dashStroke, 90, 10, true);

            button = new JRadioButtonMenuItem(" ", ii, currentDash.equals(patterns[i]));
            button.setActionCommand(patterns[i]);
            group.add(button);
            button.addActionListener(listener);
            button.setMargin( new java.awt.Insets(0,10,0,10));
            button.setPreferredSize(new java.awt.Dimension(110,20));
            dashMenu.add(button);
        }

        popup.add(widthMenu);
        popup.add(dashMenu);
        popup.add(capMenu);
        popup.add(joinMenu);

        return popup;
    }

    public final static String NONE = "No Dash Pattern";

    public static String dashArrayToString(float[] da) {
        if (da == null) {
            return NONE;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < da.length; i++) {
            sb.append(Float.toString(da[i])).append(" ");
        }
        return sb.toString().trim();
    }

    public static float[] stringToDashArray(String das) {
        if (das == null || das.equals(NONE) || das.length() == 0) {
            return null;
        }

        Vector floats = PropUtils.parseSpacedMarkers(das);
        float[] ret = new float[floats.size()];
        int index = 0;
        Enumeration thing = floats.elements();
        while (thing.hasMoreElements()) {
            String f = (String) thing.nextElement();
            try {
                ret[index++] = (new Float(f)).floatValue();
            } catch (NumberFormatException nfe) {
                return null;
            }
        }

        return ret;
    }

    public BasicStroke getBasicStroke() {
        return basicStroke;
    }

    public void setBasicStroke(BasicStroke bs) {
        basicStroke = bs;
        setStrokeWidth(basicStroke.getLineWidth());
        setMiterLimit(basicStroke.getMiterLimit());
        setDash(basicStroke.getDashArray());
        setDashPhase(basicStroke.getDashPhase());
        setLineJoins(basicStroke.getLineJoin());
        setEndCaps(basicStroke.getEndCap());
    }

    public void setPropertyChangeSupport(
                                         PropertyChangeSupport propertyChangeSupport) {
        pcs = propertyChangeSupport;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    /**
     * Given a BasicStroke, create an ImageIcon that shows it.
     *
     * @param stroke the BasicStroke to draw on the Icon.
     * @param width the width of the icon.
     * @param height the height of the icon.
     * @param horizontalOrientation if true, draw line on the icon
     *        horizontally, else draw it vertically.
     */
    public static ImageIcon createIcon(BasicStroke stroke, int width,
                                       int height, boolean horizontalOrientation) {

        BufferedImage bigImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bigImage.getGraphics();

        g.setBackground(OMColor.clear);
        g.setPaint(OMColor.clear);
        g.fillRect(0, 0, width, height);
        g.setPaint(Color.black);
        g.setStroke(stroke);
        if (horizontalOrientation) {
            g.drawLine(0, height / 2, width, height / 2);
        } else {
            g.drawLine(width / 2, 0, width / 2, height);
        }

        return new ImageIcon(bigImage);
    }

    /**
     * Given a BasicStroke, create an ImageIcon that shows it.
     *
     * @param stroke the BasicStroke to draw on the Icon.
     * @param width the width of the icon.
     * @param height the height of the icon.
     * @param horizontalOrientation if true, draw line on the icon
     *        horizontally, else draw it vertically.
     */
    public static ImageIcon createColorIcon(BasicStroke stroke, int width,
                                       int height, boolean horizontalOrientation,
                                       Color color, Color background, Color matting) {

    	BufferedImage bigImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g = (Graphics2D) bigImage.getGraphics();

    	g.setBackground(OMColor.clear);
    	g.setPaint(OMColor.clear);
    	g.fillRect(0, 0, width, height);

    	g.setPaint(background);
    	g.fillRect(0, 0, width, height);
    	if (matting != null) {
    		BasicStroke mattedStroke=new BasicStroke(((BasicStroke) stroke).getLineWidth() + 2f);
    		g.setStroke(mattedStroke);
    		g.setPaint(matting);
    		g.drawLine(0,height/2,width,height/2);
    	}
        g.setPaint(color);
        g.setStroke(stroke);
        if (horizontalOrientation) {
            g.drawLine(0, height / 2, width, height / 2);
        } else {
            g.drawLine(width / 2, 0, width / 2, height);
        }
        return new ImageIcon(bigImage);
    }

    public void setStrokeWidth(float w) {
        if (w < 1)
            w = 1;
        width = w;
    }

    public float getStrokeWidth() {
        return width;
    }

    public void setMiterLimit(float ml) {
        if (ml < 1f)
            miterLimit = 10f;
        else
            miterLimit = ml;
    }

    public float getMiterLimit() {
        return miterLimit;
    }

    public void setDash(float[] da) {
        dash = da;
    }

    public float[] getDash() {
        return dash;
    }

    public void setDashPhase(float dp) {
        dashPhase = dp;
    }

    public float getDashPhase() {
        return dashPhase;
    }

    public void setEndCaps(int ec) {
        endCaps = ec;
    }

    public int getEndCaps() {
        return endCaps;
    }

    public void setLineJoins(int lj) {
        lineJoins = lj;
    }

    public int getLineJoins() {
        return lineJoins;
    }
}