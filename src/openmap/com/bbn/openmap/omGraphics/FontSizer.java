// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.3 $
// $Date: 2004/02/13 13:36:25 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.Font;

/**
 * Helper class to OMText object that resizes font when scale changes.
 */
public class FontSizer {

    protected Font font = OMText.DEFAULT_FONT;

    protected float baseScale = -1;

    protected float curScale = -1;

    protected int minPointSize = font.getSize();

    protected int maxPointSize = font.getSize();

    protected int pointSizeRatio = 1;

    /**
     * @param font the font to use as the base font.
     * @param baseScale the scale where the base font is shown at its natural size.
     * @param pointSizeRatio the ratio of how much the scale should change
     * for every point of font size, should be provided as (scale
     * number / 1 point size), or a whole number.
     * @param minPointSize the minimum point size to use for the scaled font.
     * @param maxPointSize the maximum point size to use for the scaled font.
     */
    public FontSizer(Font font, float baseScale, int pointSizeRatio, int minPointSize, int maxPointSize) {
        this(baseScale, pointSizeRatio, minPointSize, maxPointSize);
        this.font = font;
    }

    /**
     * Contructor that uses the OMText DEFAULT_FONT as the base font.
     * @param baseScale the scale where the base font is shown at its natural size.
     * @param pointSizeRatio the ratio of how much the scale should change
     * for every point of font size, should be provided as (scale
     * number / 1 point size), or a whole number.
     * @param minPointSize the minimum point size to use for the scaled font.
     * @param maxPointSize the maximum point size to use for the scaled font.
    */
    public FontSizer(float baseScale, int pointSizeRatio, int minPointSize, int maxPointSize) {
        this.baseScale = baseScale;
        this.pointSizeRatio = pointSizeRatio;
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
     * Get a font sized for the scale, given the restrictions.
     */
    public Font getFont(float scale) {
        curScale = scale;
        return getScaledFont();
    }

    public Font getScaledFont() {
        if (baseScale < 0 || curScale < 0) {
            return font;
        } else {
            return font;
        }
    }

    /**
     * Get the base font.
     */
    public Font getFont() {
        return font;
    }

}
