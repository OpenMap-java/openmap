// **********************************************************************
// (C) Copyright 2004 NAVICON A/S. All Rights Reserved.
// http://www.navicon.dk
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: ImageIOFormatter.java,v $
//$Revision: 1.2 $
//$Date: 2007/01/25 22:11:40 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import com.bbn.openmap.util.Debug;

public abstract class ImageIOFormatter extends AbstractImageFormatter {

    protected String formatName;

    public ImageIOFormatter() {}

    public String getFormatName() {
        return formatName;
    }

    /**
     * @param formatName The formatName to set.
     */
    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public byte[] formatImage(BufferedImage bi) {
        try {
            ByteArrayOutputStream byo = new ByteArrayOutputStream();
            ImageIO.write(bi, getFormatName(), byo);
            return byo.toByteArray();
        } catch (java.io.IOException ioe) {
            Debug.error("ImageIOFormatter caught IOException formatting image!");
            return new byte[0];
        }
    }
    
    /**
     * Return the applicable Graphics to use to paint the layers into.
     * If the internal BufferedImage hasn't been created yet, or has
     * been set to null, then a new buffered Image is created, set to
     * the size specified by the height and width. The ImageGenerator
     * extends MapBean. Remember to dispose of the graphics object
     * when you are done with it. Uses the BufferedImage.TYPE_INT_ARGB
     * colormodel.
     * 
     * @param width pixel width of Graphics.
     * @param height pixel height of Graphics.
     * @return java.awt.Graphics object to use.
     * @see java.awt.image.BufferedImage
     */
    public java.awt.Graphics getGraphics(int width, int height) {
        return getGraphics(width, height, BufferedImage.TYPE_INT_ARGB);
    }

}

