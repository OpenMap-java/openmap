// **********************************************************************
// (C) Copyright 2004 NAVICON A/S. All Rights Reserved.
// http://www.navicon.dk
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: GIFImageIOFormatter.java,v $
//$Revision: 1.2 $
//$Date: 2008/02/20 01:41:08 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.http.HttpConnection;

public class GIFImageIOFormatter extends ImageIOFormatter {

    public GIFImageIOFormatter() {
        setFormatName("gif");
    }

    public ImageFormatter makeClone() {
        return new GIFImageIOFormatter();
    }

    public byte[] formatImage(BufferedImage bi) {
        BufferedImage reducedImage = ColorReducer.reduce24(bi, 256);
        try {
            ByteArrayOutputStream byo = new ByteArrayOutputStream();
            ImageIO.write(reducedImage, getFormatName(), byo);
            return byo.toByteArray();
        } catch (java.io.IOException e) {
            Debug.error("ImageIOFormatter caught IOException formatting image!");
            return new byte[0];
        }
    }

    /**
     * Get the Image Type created by the ImageFormatter. These responses should
     * adhere to the OGC WMT standard format labels. Some are listed in the
     * WMTConstants interface file.
     */
    public String getFormatLabel() {
        return WMTConstants.IMAGEFORMAT_GIF;
    }

    public String getContentType() {
        return HttpConnection.CONTENT_GIF;
    }

	@Override
	protected boolean imageFormatSupportAlphaChannel() {
		return false;
	}

	@Override
	protected boolean imageFormatSupportTransparentPixel() {
		return true;
	}

}
