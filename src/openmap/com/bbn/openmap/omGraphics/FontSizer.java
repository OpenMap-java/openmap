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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/FontSizer.java,v $
// $RCSfile: FontSizer.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Font;

/**
 * Helper class to OMText object that resizes font when scale changes.
 * Font gets bigger as you zoom in.
 */
public class FontSizer {

    protected Font font = OMText.DEFAULT_FONT;
    protected Font lastFont = OMText.DEFAULT_FONT;
    protected float baseScale = -1;
    protected float lastScale = -1;
    protected float curScale = -1;
    protected int minPointSize = font.getSize();
    protected int maxPointSize = font.getSize();
    /**
     * Default of 1. Used against the base scale/current scale ratio
     * to speed up or slow down font size changes based on scale.
     */
    protected int multiplier = 1;

    /**
     * @param font the font to use as the base font.
     * @param baseScale the scale where the base font is shown at its
     *        natural size.
     * @param multiplier to use against the ratio of base
     *        scale:current scale.
     * @param minPointSize the minimum point size to use for the
     *        scaled font.
     * @param maxPointSize the maximum point size to use for the
     *        scaled font.
     */
    public FontSizer(Font font, float baseScale, int multiplier,
            int minPointSize, int maxPointSize) {
        this(baseScale, multiplier, minPointSize, maxPointSize);
        this.font = font;
    }

    /**
     * Contructor that uses the OMText DEFAULT_FONT as the base font.
     * 
     * @param baseScale the scale where the base font is shown at its
     *        natural size.
     * @param multiplier to use against the ratio of base
     *        scale:current scale.
     * @param minPointSize the minimum point size to use for the
     *        scaled font.
     * @param maxPointSize the maximum point size to use for the
     *        scaled font.
     */
    public FontSizer(float baseScale, int multiplier, int minPointSize,
            int maxPointSize) {
        this.baseScale = baseScale;
        this.multiplier = multiplier;
        this.minPointSize = minPointSize;
        this.maxPointSize = maxPointSize;
    }

    /**
     * Set the base font.
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Get a font sized for the scale, given the size restrictions.
     */
    public Font getFont(float scale) {
        curScale = scale;
        return getScaledFont();
    }

    protected Font getScaledFont() {
        if (lastScale != curScale) {
            lastScale = curScale;

            if (baseScale < 0 || curScale < 0) {
                lastFont = font;
            } else {

                int newFontSize = multiplier
                        * (int) ((baseScale / curScale) * (float) font.getSize());

                if (newFontSize > maxPointSize) {
                    newFontSize = maxPointSize;
                } else if (newFontSize < minPointSize) {
                    newFontSize = minPointSize;
                }

                lastFont = new Font(font.getName(), font.getStyle(), newFontSize);
            }
        }

        return lastFont;
    }

    /**
     * Get the base font.
     */
    public Font getFont() {
        return font;
    }

    public void setMultiplier(int mul) {
        lastScale = -1;
        multiplier = mul;
    }

    public int getMultiplier() {
        return multiplier;
    }

}