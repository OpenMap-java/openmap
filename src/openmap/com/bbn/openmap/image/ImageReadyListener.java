// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/Attic/ImageReadyListener.java,v $
// $RCSfile: ImageReadyListener.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.image;

/**
 * The interface that listens to the ImageGenerator to find out when
 * the java.awt.Image data is ready to be retrieved.  
 * @deprecated using the ImageServer with this methodology is unreliable.
 */
public interface ImageReadyListener {

    /**
     * The generator containing the layers, ready to paint the completed image.
     *
     * @param generator the ImageGenerator to contact to get the image.
     * @param requestID the identifier of the image for the
     * ImageReadyListener to use to figure out which image is ready.  
     */
    void imageReady(ImageGenerator generator, long requestID);
}
