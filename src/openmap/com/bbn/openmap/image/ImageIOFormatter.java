// **********************************************************************
// (C) Copyright 2004 NAVICON A/S. All Rights Reserved.
// http://www.navicon.dk
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: ImageIOFormatter.java,v $
//$Revision: 1.1 $
//$Date: 2005/01/10 16:14:07 $
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

}

