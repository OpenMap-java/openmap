//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: GeoTIFFImageDecoderLoader.java,v $
//$Revision: 1.2 $
//$Date: 2006/12/15 18:28:29 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image.geotiff;

import java.net.URL;

import com.bbn.openmap.dataAccess.image.ImageDecoder;
import com.bbn.openmap.dataAccess.image.ImageDecoderLoader;

public class GeoTIFFImageDecoderLoader implements ImageDecoderLoader {
    public GeoTIFFImageDecoderLoader() {}

    public ImageDecoder getImageDecoder(URL fileURL) {
       return new GeoTIFFImageDecoder(fileURL);
    }

    public boolean isLoadable(String fileName) {
        return (fileName != null && fileName.toLowerCase().endsWith(".tif"));
    }

    public boolean isLoadable(URL fileURL) {
        return isLoadable(fileURL.getPath());
    }

}
