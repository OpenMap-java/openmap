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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/SunJPEGFormatter.java,v $
// $RCSfile: SunJPEGFormatter.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.image;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.layer.util.LayerUtils;

import java.util.*;
import java.awt.image.BufferedImage;

public class SunJPEGFormatter extends AbstractImageFormatter {

    public static final String QualityProperty = "imagequality";

    protected float imageQuality = .8f;

    public SunJPEGFormatter(){}

    public void setProperties(String prefix, Properties props) {
        imageQuality = LayerUtils.floatFromProperties(props, (prefix == null?"":prefix) + QualityProperty, imageQuality);
        if (Debug.debugging("image")) {
            Debug.output("SunJPEGFormatter setting image quality to: " + imageQuality);
        }
    }

    public ImageFormatter makeClone() {
        SunJPEGFormatter formatter =  new SunJPEGFormatter();
        formatter.setImageQuality(getImageQuality());
        return formatter;
    }

    public float getImageQuality() {
        return imageQuality;
    }

    /** For this formatter, image quality is a number in the 0-1 range. */
    public void setImageQuality(float quality) {
        imageQuality = quality;
    }

    public byte[] formatImage(BufferedImage bi) {
        try {
            return JPEGHelper.encodeJPEG(bi, imageQuality);
        } catch (java.io.IOException ioe) {
            Debug.error("SunJPEGFormatter caught IOException formatting image!");
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
