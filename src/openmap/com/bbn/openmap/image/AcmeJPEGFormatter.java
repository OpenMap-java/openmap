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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/Attic/AcmeJPEGFormatter.java,v $
// $RCSfile: AcmeJPEGFormatter.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.image;

import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * This formatter requires the Acme.JPM.Encoders package.  That code
 * can be found at <a href="http://www.acme.com/java">http://www.acme.com/java</a>.
 */
public class AcmeJPEGFormatter extends AbstractImageFormatter {
    public static final String QualityProperty = "imagequality";
    public static final int DEFAULT_IMAGE_QUALITY = 80;

    /** The quality number is between 1-100. Quality degrades,
     *  compression improves with lower numbers. */
    protected int imageQuality;

    public AcmeJPEGFormatter() {
	imageQuality = DEFAULT_IMAGE_QUALITY;
    }

    public ImageFormatter makeClone() {
	AcmeJPEGFormatter formatter =  new AcmeJPEGFormatter();
	formatter.setImageQuality(getImageQuality());
	return formatter;
    }

    public void setProperties(String prefix, Properties props) {

	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	imageQuality = LayerUtils.intFromProperties(props, prefix + QualityProperty, imageQuality);

	if (Debug.debugging("image")){
	    Debug.output("AcmeJPEGFormatter setting image quality to: " +
			 imageQuality);
	}
    }

    public int getImageQuality() {
	return imageQuality;
    }

    /** For this formatter, image quality is a number in the 1-100 range. */
    public void setImageQuality(int quality) {
	imageQuality = quality;
    }

    public byte[] formatImage(BufferedImage bi) {
	try{
	    return AcmeJPEGHelper.encodeJPEG(bi, imageQuality);
	} catch (java.io.IOException ioe) {
	    Debug.error("AcmeJPEGFormatter caught IOException formatting image!");
	    return new byte[0];
	}
    }

    /**
     * Get the Image Type created by the ImageFormatter.  These
     * responses should adhere to the OGC WMT standard format labels.
     * Some are listed in the WMTConstants interface file.
     */
    public String getFormatLabel() {
	return WMTConstants.IMAGEFORMAT_JPEG;
    }
}


