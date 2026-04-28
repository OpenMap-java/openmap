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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/PPMFormatter.java,v $
// $RCSfile: PPMFormatter.java,v $
// $Revision: 1.5 $
// $Date: 2008/02/20 01:41:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Properties;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.http.HttpConnection;
/**
 * A simple formatter to create PPM images.
 * <P>
 * I got a simple definition of a PPM image an wrote something to make
 * that. XV complains about the P3 image, but still draws it. No
 * complaints for the P6.
 */
public class PPMFormatter extends AbstractImageFormatter {

    public static final String RawBitsProperty = "rawbits";
    public static final String regularMagicNumber = "P3";
    public static final String rawbitsMagicNumber = "P6";
    public static final int HEADER_BUFFER_SIZE = 50; // approximate,
                                                     // no big deal
    public static final int MAX_COLOR_VALUE = 255; // I guess

    protected boolean rawbits;

    public PPMFormatter() {}

    public void setProperties(String prefix, Properties props) {
        rawbits = PropUtils.booleanFromProperties(props, (prefix == null ? ""
                : prefix)
                + RawBitsProperty, true);
    }

    public ImageFormatter makeClone() {
        PPMFormatter formatter = new PPMFormatter();
        formatter.rawbits = rawbits;
        return formatter;
    }

    public boolean getRawbits() {
        return rawbits;
    }

    public void setRawbits(boolean rb) {
        rawbits = rb;
    }

    public byte[] formatImage(BufferedImage bi) {

        int height = bi.getHeight();
        int width = bi.getWidth();

        int[] data = new int[width * height];
        bi.getRGB(0, 0, width, height, data, 0, width);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            int pixel, count, i, lastCount;
            if (!rawbits) {
                dos.writeBytes(regularMagicNumber);
                dos.writeBytes(" " + width);
                dos.writeBytes(" " + height);
                dos.writeBytes(" " + MAX_COLOR_VALUE + "\n");
                count = 0; // Keep lines less that 70 characters long

                // Keep track of the number of characters added per
                // pass
                lastCount = 0;

                Debug.output("PPMFormatter: Header is " + dos.size() + " bytes");
                Debug.output("PPMFormatter: Height = " + height);
                Debug.output("PPMFormatter: Width = " + width);
                Debug.output("PPMFormatter: data length = " + data.length);

                for (i = 0; i < data.length; i++) {

                    pixel = data[i];
                    int r = (pixel >>> 16) & 0x000000FF;
                    int g = (pixel >>> 8) & 0x000000FF;
                    int b = (pixel) & 0x000000FF;

                    dos.writeBytes(" " + r);
                    dos.writeBytes(" " + g);
                    dos.writeBytes(" " + b);
                    if (count > 57) {
                        dos.writeBytes("\n");
                        count = 0;
                    } else {
                        count += dos.size() - lastCount;
                    }
                    lastCount = dos.size();
                }
                Debug.output("PPMFormatter: after data, size is " + dos.size());

            } else {
                dos.writeBytes(rawbitsMagicNumber);
                dos.writeBytes(" " + width);
                dos.writeBytes(" " + height);
                dos.writeBytes(" " + MAX_COLOR_VALUE + "\n");
                for (i = 0; i < data.length; i++) {
                    pixel = data[i];
                    dos.writeByte(pixel >>> 16);
                    dos.writeByte(pixel >>> 8);
                    dos.writeByte(pixel);
                }
            }

            return baos.toByteArray();

        } catch (java.io.IOException ioe) {
            System.err.println("PPMFormatter caught IOException formatting image!");
            return new byte[0];
        }
    }

    /**
     * Get the Image Type created by the ImageFormatter. These
     * responses should adhere to the OGC WMT standard format labels.
     * Some are listed in the WMTConstants interface file.
     */
    public String getFormatLabel() {
        return WMTConstants.IMAGEFORMAT_PPM;
    }
    
    public String getContentType() {
        return HttpConnection.CONTENT_PPM;
    }
    
	@Override
	protected boolean imageFormatSupportAlphaChannel() {
		// TODO: Is this correct?
		return false;
	}

	@Override
	protected boolean imageFormatSupportTransparentPixel() {
		// TODO: Is this correct?
		return false;
	}


}