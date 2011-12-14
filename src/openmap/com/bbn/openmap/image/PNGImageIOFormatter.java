// **********************************************************************
// (C) Copyright 2004 NAVICON A/S. All Rights Reserved.
// http://www.navicon.dk
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: PNGImageIOFormatter.java,v $
//$Revision: 1.2 $
//$Date: 2008/02/20 01:41:08 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.image;

import com.bbn.openmap.util.http.HttpConnection;

public class PNGImageIOFormatter extends ImageIOFormatter {

    public PNGImageIOFormatter() {
        setFormatName("png");
    }

    public ImageFormatter makeClone() {
        return new PNGImageIOFormatter();
    }

    /**
     * Get the Image Type created by the ImageFormatter. These
     * responses should adhere to the OGC WMT standard format labels.
     * Some are listed in the WMTConstants interface file.
     */
    public String getFormatLabel() {
        return WMTConstants.IMAGEFORMAT_PNG;
    }
    
    public String getContentType() {
        return HttpConnection.CONTENT_PNG;
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

