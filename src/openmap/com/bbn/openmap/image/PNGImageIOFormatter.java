// **********************************************************************
// (C) Copyright 2004 NAVICON A/S. All Rights Reserved.
// http://www.navicon.dk
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: PNGImageIOFormatter.java,v $
//$Revision: 1.1 $
//$Date: 2005/01/10 16:14:07 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.image;

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
}

