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
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

/*  Java Core  */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;

/* OpenMap */
import com.bbn.openmap.util.PropUtils;

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

        setStrokeWidth(basicStroke.getLineWidth());
        setMiterLimit(basicStroke.getMiterLimit());
        setDash(basicStroke.getDashArray());
        setDashPhase(basicStroke.getDashPhase());
        setEndCaps(basicStroke.getEndCap());
        setLineJoins(basicStroke.getLineJoin());

        setBasicStroke(basicStroke);
    }

    public void setLaunchButton(JButton lb) {
        launchButton = lb;
    }

    public JButton getLaunchButton() {
        if (launchButton == null) {
            BasicStroke bs = getBasicStroke();
            float buttonHeight = 20;
            ImageIcon icon = createIcon(getBasicStroke(),
                    50,
                    (int) buttonHeight,
                    true);
            launchButton = new JButton(icon);
            launchButton.setToolTipText("Modify Line Parameters");
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

    public JPopupMenu setGUI(JPopupMenu popup) {

        //////// Line Cap
        JMenu capMenu = new JMenu("Cap Decoration");

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
        JRadioButtonMenuItem button = new JRadioButtonMenuItem("Butt", endCaps == BasicStroke.CAP_BUTT);
        button.setActionCommand(String.valueOf(BasicStroke.CAP_BUTT));
        group.add(button);
        button.addActionListener(listener);
        capMenu.add(button);

        button = new JRadioButtonMenuItem("Round", endCaps == BasicStroke.CAP_ROUND);
        button.setActionCommand(String.valueOf(BasicStroke.CAP_ROUND));
        group.add(button);
        button.addActionListener(listener);
        capMenu.add(button);

        button = new JRadioButtonMenuItem("Square", endCaps == BasicStroke.CAP_SQUARE);
        button.setActionCommand(String.valueOf(BasicStroke.CAP_SQUARE));
        group.add(button);
        button.addActionListener(listener);
        capMenu.add(button);

        //////// Line Joins

        JMenu joinMenu = new JMenu("Joint Decoration");

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
        button = new JRadioButtonMenuItem("Miter", lineJoins == BasicStroke.JOIN_MITER);
        button.setActionCommand(String.valueOf(BasicStroke.JOIN_MITER));
        group.add(button);
        button.addActionListener(listener);
        joinMenu.add(button);

        button = new JRadioButtonMenuItem("Round", lineJoins == BasicStroke.JOIN_ROUND);
        button.setActionCommand(String.valueOf(BasicStroke.JOIN_ROUND));
        group.add(button);
        button.addActionListener(listener);
        joinMenu.add(button);

        button = new JRadioButtonMenuItem("Bevel", lineJoins == BasicStroke.JOIN_BEVEL);
        button.setActionCommand(String.valueOf(BasicStroke.JOIN_BEVEL));
        group.add(button);
        button.addActionListener(listener);
        joinMenu.add(button);

        //////// Line Width

        JMenu widthMenu = new JMenu("Line Width");

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
            button = new JRadioButtonMenuItem(ii, (int) width == i);
            button.setActionCommand(String.valueOf(i));
            group.add(button);
            button.addActionListener(listener);
            widthMenu.add(button);
        }

        //////// Dash Pattern

        JMenu dashMenu = new JMenu("Dash Pattern");

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

        String[] patterns = new String[] { null, "1 3", "3 3", "12 10",
                "12 10 6 10", "20 10", "20 10 6 10", "20 10 6 10 6 10" };

        String currentDash = dashArrayToString(getDash());
        for (i = 0; i < patterns.length; i++) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, stringToDashArray(patterns[i]), 0.0f);
            ii = createIcon(dashStroke, 90, 10, true);

            button = new JRadioButtonMenuItem(ii, currentDash.equals(patterns[i]));
            button.setActionCommand(patterns[i]);
            group.add(button);
            button.addActionListener(listener);
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
            sb.append(Float.toString(da[i]) + " ");
        }
        return sb.toString();
    }

    public static float[] stringToDashArray(String das) {
        if (das == null || das.equals(NONE) || das.equals("")) {
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