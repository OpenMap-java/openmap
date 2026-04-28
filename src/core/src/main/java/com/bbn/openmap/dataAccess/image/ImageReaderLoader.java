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
//$RCSfile: ImageReaderLoader.java,v $
//$Revision: 1.1 $
//$Date: 2007/01/22 15:47:34 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image;

import java.net.URL;

/**
 * An ImageReaderLoader is an object used to determine if a particular
 * ImageReader can be used for a particular image file. It can also provide the
 * ImageReader loaded with the contents of that file.
 * 
 * @author dietrick
 */
public interface ImageReaderLoader {
    /**
     * @param fileURL a URL for an image file.
     * @return an ImageReader loaded with the image file contents, or null if
     *         the image can't be handled.
     */
    ImageReader getImageReader(URL fileURL);

    /**
     * A query method used to ask the ImageReaderLoader if an image can be
     * handled by the ImageReader it represents.
     * 
     * @param fileName path to image file.
     * @return true if yes.
     */
    boolean isLoadable(String fileName);

    /**
     * A query method used to ask the ImageReaderLoader if an image can be
     * handled by the ImageReader it represents.
     * 
     * @param fileURL URL of image file.
     * @return true if yes.
     */
    boolean isLoadable(URL fileURL);
}
