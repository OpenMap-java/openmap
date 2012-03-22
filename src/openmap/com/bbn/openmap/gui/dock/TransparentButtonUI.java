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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/dock/TransparentButtonUI.java,v $
// $RCSfile: TransparentButtonUI.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 17:50:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.dock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 * A subclass of the metal UI that draws a semi-transparent button on
 * top of other components.
 * 
 * @author Ben Lubin
 * @version $Revision: 1.4 $ on $Date: 2005/08/09 17:50:51 $
 * @since 12/5/02
 */
public class TransparentButtonUI extends MetalButtonUI {
    private final static TransparentButtonUI transButtonUI = new TransparentButtonUI();

    private static final int VshadowSize = 4;
    private static final int HshadowSize = 2;

    private static final int VpressOffset = 3;
    private static final int HpressOffset = 1;

    // ********************************
    //          Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent c) {
        return transButtonUI;
    }

    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setBorder(BorderFactory.createEmptyBorder(0,
                0,
                VshadowSize,
                HshadowSize));
        b.setFocusPainted(false);
    }

    public Color getBGColor(AbstractButton c) {
        Color bg = c.getBackground();
        return new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 200);
    }

    public Color getSelectedBGColor(AbstractButton c) {
//        Color bg = c.getBackground();
        return new Color(225, 225, 125, 200);
    }

    Color darkShadow = new Color(0, 0, 0, 200);

    public Color getDarkShadow() {
        return darkShadow;
    }

    Color mediumShadow = new Color(0, 0, 0, 125);

    public Color getMediumShadow() {
        return mediumShadow;
    }

    Color lightShadow = new Color(0, 0, 0, 50);

    public Color getLightShadow() {
        return lightShadow;
    }

    // ********************************
    //          Paint Methods
    // ********************************

    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Dimension size = b.getSize();

        // perform UI specific press action, e.g. Windows L&F shifts
        // text
        if (model.isArmed() && model.isPressed() || model.isSelected()) {
            //We're pressed:
            g = g.create(HpressOffset, VpressOffset, size.width, size.height);
            Dimension sSize = new Dimension(size.width - HpressOffset, size.height
                    - VpressOffset);
            drawShadow(g, sSize, VshadowSize - VpressOffset, HshadowSize
                    - HpressOffset);
        } else {
            //We're not pressed:
            drawShadow(g, size, VshadowSize, HshadowSize);
        }

        //Paint a background:
        if (model.isArmed() && model.isPressed() || model.isSelected()) {
            //Selected color:
            g.setColor(getSelectedBGColor(b));
        } else {
            //Button's color
            g.setColor(getBGColor(b));
        }
        g.fillRect(0, 0, size.width - HshadowSize, size.height - VshadowSize);
        super.paint(g, c);
    }

    private void drawShadow(Graphics g, Dimension size, int vShadowSize,
                            int hShadowSize) {
        drawHArea(g,
                size,
                getDarkShadow(),
                vShadowSize,
                hShadowSize,
                0,
                vShadowSize / 3);
        drawHArea(g,
                size,
                getMediumShadow(),
                vShadowSize,
                hShadowSize,
                vShadowSize / 3,
                vShadowSize * 2 / 3);
        drawHArea(g,
                size,
                getLightShadow(),
                vShadowSize,
                hShadowSize,
                vShadowSize * 2 / 3,
                vShadowSize);
        int last = 0;
        int next = hShadowSize / 3;
        if (hShadowSize % 3 == 2) {
            next += 1;
        }
        drawVArea(g,
                size,
                getDarkShadow(),
                vShadowSize,
                hShadowSize,
                last,
                next);
        last = next;
        if (hShadowSize * 2 / 3 > last) {
            next = hShadowSize * 2 / 3;
            drawVArea(g,
                    size,
                    getMediumShadow(),
                    vShadowSize,
                    hShadowSize,
                    last,
                    next);
        }
        last = next;
        if (hShadowSize > last) {
            next = hShadowSize;
            drawVArea(g,
                    size,
                    getLightShadow(),
                    vShadowSize,
                    hShadowSize,
                    last,
                    next);
        }
    }

    private void drawHArea(Graphics g, Dimension size, Color c,
                           int vShadowSize, int hShadowSize, int low, int high) {
        g.setColor(c);
        for (int i = low; i < high; i++) {
            drawHLine(g, size, vShadowSize, hShadowSize, i);
        }
    }

    private void drawHLine(Graphics g, Dimension size, int vShadowSize,
                           int hShadowSize, int i) {
        g.drawLine(i * hShadowSize / vShadowSize,
                size.height - vShadowSize + i,
                size.width - hShadowSize + (i * hShadowSize / vShadowSize),
                size.height - vShadowSize + i);
    }

    private void drawVArea(Graphics g, Dimension size, Color c,
                           int vShadowSize, int hShadowSize, int low, int high) {
        g.setColor(c);
        for (int i = low; i < high; i++) {
            drawVLine(g, size, vShadowSize, hShadowSize, i);
        }
    }

    private void drawVLine(Graphics g, Dimension size, int vShadowSize,
                           int hShadowSize, int i) {
        g.drawLine(size.width - hShadowSize + i,
                (i * vShadowSize / hShadowSize),
                size.width - hShadowSize + i,
                size.height - vShadowSize + (i * vShadowSize / hShadowSize) - 1);
    }

    /** From super class. Don't want to do metal's behavior... */
    protected void paintButtonPressed(Graphics g, AbstractButton b) {}
}