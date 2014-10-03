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
//$RCSfile: GeoTIFFImageReaderLoader.java,v $
//$Revision: 1.1 $
//$Date: 2007/01/22 15:47:36 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image.geotiff;

import java.net.URL;

import com.bbn.openmap.dataAccess.image.ImageReader;
import com.bbn.openmap.dataAccess.image.ImageReaderLoader;

/**
 * An ImageReaderLoader that looks for GeoTIFF images to pass to GeoTIFFImageReaders.
 * 
 * @author dietrick
 */
public class GeoTIFFImageReaderLoader implements ImageReaderLoader {
    public GeoTIFFImageReaderLoader() {}

    public ImageReader getImageReader(URL fileURL) {
       return new GeoTIFFImageReader(fileURL);
    }

    public boolean isLoadable(String fileName) {
        return (fileName != null && fileName.toLowerCase().endsWith(".tif"));
    }

    public boolean isLoadable(URL fileURL) {
        return isLoadable(fileURL.getPath());
    }

}
