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
//$RCSfile: ImageDecoderLoader.java,v $
//$Revision: 1.2 $
//$Date: 2006/12/15 18:28:28 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image;

import java.net.URL;

public interface ImageDecoderLoader {
    ImageDecoder getImageDecoder(URL fileURL);

    boolean isLoadable(String fileName);

    boolean isLoadable(URL fileURL);
}
