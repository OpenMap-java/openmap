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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/BasicStrokeEditor.java,v $
// $RCSfile: BasicStrokeEditor.java,v $
// $Revision: 1.7 $
// $Date: 2005/08/09 20:01:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

/*  Java Core  */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * The BasicStrokeEditor provides a GUI to adjust BasicStroke
 * parameters. It can provide a default button to launch itself, or a
 * button can be set. This class is being replaced by the
 * BasicStrokeEditorMenu.
 */
public class BasicStrokeEditor extends JDialog implements ActionListener {

    protected BasicStroke basicStroke = null;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected float width; // must be >= 0f
    protected int endCaps; //CAP_ROUND, CAP_BUTT, CAP_SQUARE
    protected int lineJoins; //JOIN_BEVEL, JOIN_MITER, JOIN_ROUND
    protected float miterLimit; // 10f default, must be >= 1f
    protected float[] dash;
    protected float dashPhase;

    public BasicStrokeEditor() {
        basicStroke = new BasicStroke(1f);
    }

    public BasicStrokeEditor(BasicStroke bs) {
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
    }

    public void resetStroke() {
        BasicStroke oldStroke = basicStroke;
        setMiterLimit(miterLimit);
        basicStroke = new BasicStroke(width, endCaps, lineJoins, miterLimit, dash, dashPhase);
        pcs.firePropertyChange("line", oldStroke, basicStroke);
    }

    protected void widgetsToSettings() {
        try {
            float w = new Float(widthField.getText()).floatValue();
            if (w < 0)
                w = 0;
            width = w;
        } catch (NumberFormatException nfe) {
            width = 1f;
        }

        try {
            setMiterLimit(new Float(miterLimitField.getText()).floatValue());
        } catch (NumberFormatException nfe) {
            setMiterLimit(10f);
        }

        dash = stringToDashArray(dashField.getText());

        try {
            float dp = new Float(dashPhaseField.getText()).floatValue();
            if (dp < 0f)
                dp = 0f;
            dashPhase = dp;
        } catch (NumberFormatException nfe) {
            dashPhase = 0f;
        }

        endCaps = capBox.getSelectedIndex();
        lineJoins = joinBox.getSelectedIndex();
    }

    protected transient JTextField widthField, miterLimitField, dashField,
            dashPhaseField;
    protected transient JPanel palette = null;
    protected transient JButton closebutton;
    protected transient JButton applybutton;
    protected transient JComboBox capBox;
    protected transient JComboBox joinBox;

    public void setGUI() {

        if (palette == null) {
            setTitle("Modify Line Stroke Parameters");
            palette = new JPanel();
            palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));

            JPanel capPanel = PaletteHelper.createPaletteJPanel("Line Cap Decoration");
            String[] capStrings = { "Butt", "Round", "Square" };
            capBox = new JComboBox(capStrings);
            capBox.addActionListener(this);
            capPanel.add(capBox);
            palette.add(capPanel);

            JPanel joinPanel = PaletteHelper.createPaletteJPanel("Line Joint Decoration");

            String[] joinStrings = { "Miter", "Round", "Bevel" };
            joinBox = new JComboBox(joinStrings);
            joinBox.addActionListener(this);
            joinPanel.add(joinBox);
            palette.add(joinPanel);

            JPanel widthPanel = new JPanel();
            widthPanel.setLayout(new GridLayout(0, 1));

            JLabel widthLabel = new JLabel("Line Width");
            widthField = new JTextField(Float.toString(basicStroke.getLineWidth()), 4);
            widthField.setToolTipText("Enter pixel width of line.");
            widthPanel.add(widthLabel);
            widthPanel.add(widthField);

            JPanel mlPanel = new JPanel();
            mlPanel.setLayout(new GridLayout(0, 1));

            JLabel miterLimitLabel = new JLabel("Miter Limit");
            miterLimitField = new JTextField(Float.toString(basicStroke.getMiterLimit()), 4);
            miterLimitField.setToolTipText("Min angle for corner decorations.");

            mlPanel.add(miterLimitLabel);
            mlPanel.add(miterLimitField);

            JPanel dlPanel = new JPanel();
            dlPanel.setLayout(new FlowLayout());

            JLabel dashLabel = new JLabel("Dash Pattern");
//            float[] da = basicStroke.getDashArray();
//            String dashArrayString = dashArrayToString(da);

            dashField = new JTextField(dashArrayToString(basicStroke.getDashArray()), 15);
            dashField.setToolTipText("Number of pixels on off on ...");
            dlPanel.add(dashLabel);
            dlPanel.add(dashField);

            JPanel dpPanel = new JPanel();
            dpPanel.setLayout(new GridLayout(0, 1));

            JLabel dashPhaseLabel = new JLabel("Dash Phase");
            dashPhaseField = new JTextField(Float.toString(basicStroke.getDashPhase()), 4);
            dashPhaseField.setToolTipText("Phase to start dash array.");
            dpPanel.add(dashPhaseLabel);
            dpPanel.add(dashPhaseField);

            JPanel textFieldPanel = new JPanel();
            textFieldPanel.setLayout(new GridLayout(0, 3));
            textFieldPanel.add(widthPanel);
            textFieldPanel.add(dpPanel);
            textFieldPanel.add(mlPanel);
            palette.add(textFieldPanel);
            palette.add(dlPanel);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(0, 2));
            closebutton = new JButton("Cancel");
            closebutton.addActionListener(this);
            applybutton = new JButton("OK");
            applybutton.addActionListener(this);
            buttonPanel.add(applybutton);
            buttonPanel.add(closebutton);

            palette.add(buttonPanel);

            getContentPane().add(palette);
            this.pack();

        } else {
            widthField.setText(Float.toString(basicStroke.getLineWidth()));
            miterLimitField.setText(Float.toString(basicStroke.getMiterLimit()));
            dashField.setText(dashArrayToString(basicStroke.getDashArray()));
            dashPhaseField.setText(Float.toString(basicStroke.getDashPhase()));
        }

        // Set palette to current conditions;
        capBox.setSelectedIndex(basicStroke.getEndCap());
        joinBox.setSelectedIndex(basicStroke.getLineJoin());
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
        return sb.toString();
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

    public final static String LaunchCmd = "LAUNCH";

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == applybutton) {
            widgetsToSettings();
            resetStroke();
            this.setVisible(false);
        } else if (event.getSource() == closebutton) {
            this.setVisible(false);
        } else if (event.getActionCommand() == LaunchCmd) {
            setGUI();
            this.setVisible(true);
        } else {
            widgetsToSettings();
            resetStroke();
        }
    }

    public BasicStroke getBasicStroke() {
        return basicStroke;
    }

    public void setBasicStroke(BasicStroke bs) {
        basicStroke = bs;
        if (launchButton != null) {
            //          float buttonHeight = (bs == null?11:bs.getLineWidth() +
            // 10f);
            float buttonHeight = 20;
            launchButton.setIcon(createIcon(getBasicStroke(),
                    40,
                    (int) buttonHeight,
                    true));
        }
    }

    JButton launchButton;

    public void setLaunchButton(JButton lb) {
        launchButton = lb;
    }

    public JButton getLaunchButton() {
        if (launchButton == null) {
//            BasicStroke bs = getBasicStroke();
//            float buttonHeight = (bs == null ? 11 : bs.getLineWidth() + 10f);
            float buttonHeight = 20;
            ImageIcon icon = createIcon(getBasicStroke(),
                    40,
                    (int) buttonHeight,
                    true);
            launchButton = new JButton(icon);
            launchButton.setToolTipText("Modify Line Stroke");
            launchButton.addActionListener(this);
            launchButton.setActionCommand(LaunchCmd);
        }
        return launchButton;
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