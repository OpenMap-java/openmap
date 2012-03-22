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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/AcmeGifFormatter.java,v $
// $RCSfile: AcmeGifFormatter.java,v $
// $Revision: 1.4 $
// $Date: 2008/02/20 01:41:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.util.Properties;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.http.HttpConnection;

/**
 * This formatter requires the Acme.JPM.Encoders package. That code
 * can be found at <a
 * href="http://www.acme.com/java">http://www.acme.com/java </a>.
 */
public class AcmeGifFormatter extends AbstractImageFormatter {

    public AcmeGifFormatter() {}

    public void setProperties(String prefix, Properties props) {}

    public ImageFormatter makeClone() {
        return new AcmeGifFormatter();
    }

    public byte[] formatImage(BufferedImage bi) {
        try {
            return AcmeGifHelper.encodeGif(bi);
        } catch (java.io.IOException ioe) {
            Debug.error("AcmeGifFormatter caught IOException formatting image!\n  "
                    + ioe);
            return new byte[0];
        }
    }

    /**
     * Get the Image Type created by the ImageFormatter. These
     * responses should adhere to the OGC WMT standard format labels.
     * Some are listed in the WMTConstants interface file.
     */
    public String getFormatLabel() {
        return WMTConstants.IMAGEFORMAT_GIF;
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