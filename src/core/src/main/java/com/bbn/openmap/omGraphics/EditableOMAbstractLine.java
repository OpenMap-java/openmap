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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMAbstractLine.java,v $
// $RCSfile: EditableOMAbstractLine.java,v $
// $Revision: 1.2 $
// $Date: 2005/08/10 22:25:08 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * The EditableOMAbstractLine encompasses an OMAbstractLine, providing
 * a mechanism for adding an arrowhead menu in the line editor menu.
 */
public abstract class EditableOMAbstractLine extends EditableOMGraphic {

    protected JMenu arrowheadMenu = null;

    public Component getGUI(GraphicAttributes graphicAttributes) {
        if (graphicAttributes != null) {
            JMenu ahm = getArrowHeadMenu();
            graphicAttributes.setLineMenuAdditions(new JMenu[] { ahm });
        }
        return null;
    }

    public JMenu getArrowHeadMenu() {

        if (arrowheadMenu == null) {
            arrowheadMenu = new JMenu(i18n.get(EditableOMAbstractLine.class, "Arrows", "Arrows"));

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String command = ae.getActionCommand();
                    try {
                        int what = Integer.parseInt(command);
                        if (what < 0) {
                            ((OMAbstractLine) getGraphic()).addArrowHead(false);
                        } else {
                            ((OMAbstractLine) getGraphic()).addArrowHead(what);
                        }
                        generate(getProjection());
                        repaint();
                    } catch (NumberFormatException e) {
                    }
                }
            };

            boolean doArrowHead = ((OMAbstractLine) getGraphic()).hasArrowHead();
            int currentDirection = OMArrowHead.ARROWHEAD_DIRECTION_FORWARD;

            if (doArrowHead) {
                currentDirection = ((OMAbstractLine) getGraphic()).getArrowHead().getArrowDirectionType();
            }

            int descDir = -1; // this description direction

            ButtonGroup group = new ButtonGroup();
            ImageIcon ii = createArrowIcon(new BasicStroke(1), 50, 20, descDir);
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(ii, !doArrowHead);
            button.setActionCommand(String.valueOf(descDir));
            group.add(button);
            button.addActionListener(listener);
            arrowheadMenu.add(button);

            for (descDir = OMArrowHead.ARROWHEAD_DIRECTION_FORWARD; descDir <= OMArrowHead.ARROWHEAD_DIRECTION_BOTH; descDir++) {
                ii = createArrowIcon(new BasicStroke(1), 50, 20, descDir);
                button = new JRadioButtonMenuItem(ii, doArrowHead
                        && currentDirection == descDir);
                button.setActionCommand(String.valueOf(descDir));
                group.add(button);
                button.addActionListener(listener);
                arrowheadMenu.add(button);
            }
        }
        return arrowheadMenu;
    }

    /**
     * Given some arrowhead parameters, create an ImageIcon that shows
     * it.
     *
     * @param stroke the BasicStroke to draw on the Icon.
     * @param width the width of the icon.
     * @param height the height of the icon.
     * @param arrowHeadType -1 for no arrowhead, use the OMArrowHead
     *        directions for other versions.
     */
    public ImageIcon createArrowIcon(BasicStroke stroke, int width, int height,
                                     int arrowHeadType) {

        BufferedImage bigImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bigImage.getGraphics();

        int middleY = height / 2;

        g.setBackground(OMColor.clear);
        g.setPaint(OMColor.clear);
        g.fillRect(0, 0, width, height);
        g.setPaint(Color.black);
        g.setStroke(stroke);
        g.drawLine(0, middleY, width, middleY);

        int upTip = (int) ((float) height * .25);
        int downTip = (int) ((float) height * .75);
        Polygon poly = null;
        if (arrowHeadType == OMArrowHead.ARROWHEAD_DIRECTION_FORWARD
                || arrowHeadType == OMArrowHead.ARROWHEAD_DIRECTION_BOTH) {
            int rightWingX = (int) ((float) width * .75);
            poly = new Polygon(new int[] { width, rightWingX, rightWingX }, new int[] {
                    middleY, upTip, downTip }, 3);
            g.fill(poly);
            g.draw(poly); // Seems to help with rendering problem.
        }

        if (arrowHeadType == OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD
                || arrowHeadType == OMArrowHead.ARROWHEAD_DIRECTION_BOTH) {
            int leftWingX = (int) ((float) width * .25);
            poly = new Polygon(new int[] { 0, leftWingX, leftWingX }, new int[] {
                    middleY, upTip, downTip }, 3);
            g.fill(poly);
            g.draw(poly); // Seems to help with rendering problem.
        }

        return new ImageIcon(bigImage);
    }
}