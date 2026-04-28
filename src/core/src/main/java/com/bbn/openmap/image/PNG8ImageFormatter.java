package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.http.HttpConnection;

public class PNG8ImageFormatter extends ImageIOFormatter {

    public PNG8ImageFormatter() {
        setFormatName("png");
    }

    public byte[] formatImage(BufferedImage bi) {
        BufferedImage reducedImage = null;
        if (bi.getColorModel().hasAlpha()) {
			reducedImage = ColorReducer.reduce32(bi, 256);
		} else {
			reducedImage = ColorReducer.reduce24(bi, 256);
		}
        try {
            ByteArrayOutputStream byo = new ByteArrayOutputStream();
            ImageIO.write(reducedImage, getFormatName(), byo);
            return byo.toByteArray();
        } catch (java.io.IOException e) {
            Debug.error("ImageIOFormatter caught IOException formatting image!");
            return new byte[0];
        }
    }

    public java.awt.Graphics getGraphics(int width, int height) {
        return getGraphics(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public ImageFormatter makeClone() {
        return new PNG8ImageFormatter();
    }

    public String getContentType() {
        return HttpConnection.CONTENT_PNG + "; mode=8bit";
    }

    public String getFormatLabel() {
        return WMTConstants.IMAGEFORMAT_PNG + "8";
    }

	@Override
	protected boolean imageFormatSupportAlphaChannel() {
		return true;
	}

	@Override
	protected boolean imageFormatSupportTransparentPixel() {
		return true;
	}

}
