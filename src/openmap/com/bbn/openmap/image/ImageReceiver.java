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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageReceiver.java,v $
// $RCSfile: ImageReceiver.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

/**
 * The interface describing the object that receives the formatted
 * image from the ImageServer.
 */
public interface ImageReceiver {

    /**
     * Receive the bytes from a image.
     * 
     * @param imageBytes the formatted image.
     */
    public void receiveImageData(byte[] imageBytes);
}

