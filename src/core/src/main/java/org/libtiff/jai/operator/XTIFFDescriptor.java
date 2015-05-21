package org.libtiff.jai.operator;

/*
 * XTIFF: eXtensible TIFF libraries for JAI.
 * 
 * The contents of this file are subject to the  JAVA ADVANCED IMAGING
 * SAMPLE INPUT-OUTPUT CODECS AND WIDGET HANDLING SOURCE CODE  License
 * Version 1.0 (the "License"); You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.sun.com/software/imaging/JAI/index.html
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License. 
 *
 * The Original Code is JAVA ADVANCED IMAGING SAMPLE INPUT-OUTPUT CODECS
 * AND WIDGET HANDLING SOURCE CODE. 
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 * Portions created by: Niles Ritter 
 * are Copyright (C): Niles Ritter, GeoTIFF.org, 1999,2000.
 * All Rights Reserved.
 * Contributor(s): Niles Ritter
 */

// Sun does not expose this in the API, but
//  its interface is described in the jai examples.
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;

import org.libtiff.jai.codecimpl.XTIFFCodec;
import org.libtiff.jai.codecimpl.XTIFFImage;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;

/**
 * XTIFFDescriptor: A single class that is both an OperationDescriptor and a
 * RenderedImageFactory for overriding the JAI "tiff" operation.
 * 
 * @see XTIFFDirectory
 * @see XTIFFTileCodecImpl
 */

public class XTIFFDescriptor extends OperationDescriptorImpl implements
        RenderedImageFactory {

    // The resource strings that provide the general documentation
    // and specify the parameter list for the GeoTIFF operation.
    private static final String[][] resources = {
            { "GlobalName", "xtiff" },
            { "LocalName", "xtiff" },
            { "Vendor", "libtiff.org" },
            { "Description", "A TIFF parser, extending the TIFF operation." },
            { "DocURL", "http://www.geotiff.org/javadocs/XTIFFDescriptor.html" },
            { "Version", "1.0" }, { "arg0Desc", "param1" },
            { "arg1Desc", "param2" } };

    // The parameter names for the GeoTIFF operation. Extenders may
    // want to rename them to something more meaningful.
    private static final String[] paramNames = { "stream", "param" };

    // The class types for the parameters of the GeoTIFF operation.
    // User defined classes can be used here as long as the fully
    // qualified name is used and the classes can be loaded.
    private static final Class[] paramClasses = {
            com.sun.media.jai.codec.SeekableStream.class,
            com.sun.media.jai.codec.TIFFDecodeParam.class };

    // The default parameter values for the GeoTIFF operation
    // when using a ParameterBlockJAI.
    private static final Object[] paramDefaults = { null, null };

    /**
     * Standard public constructor
     */
    public XTIFFDescriptor() {
        super(resources, 0, paramClasses, paramNames, paramDefaults);
    }

    /**
     * Create an XTIFFImage with the given ParameterBlock if the XTIFFImage can
     * handle the particular ParameterBlock. Otherwise, null image is returned.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        StringBuffer msg = new StringBuffer();
        if (!validateParameters(paramBlock, msg)) {
            return null;
        }
        try {
            SeekableStream in = (SeekableStream) paramBlock.getObjectParameter(0);
            XTIFFImage image = new XTIFFImage(in, (TIFFDecodeParam) paramBlock.getObjectParameter(1), 0);
            return image;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A convenience method for registering XTIFF methods into JAI, for extended
     * classes of XTIFFDescriptor.
     */
    public static void register(XTIFFDescriptor odesc) {

        OperationRegistry reg = JAI.getDefaultInstance().getOperationRegistry();

        // override tiff operation
        // reg.unregisterOperationDescriptor("tiff");

        // ...and register tiff with the new desc
        // reg.registerOperationDescriptor(odesc, "tiff");
        reg.registerDescriptor(odesc);

        // reg.registerRIF("tiff", "org.libtiff.jai", odesc);

        // re-register the tiff codec
        // ImageCodec.unregisterCodec("tiff");
        ImageCodec.registerCodec(new XTIFFCodec());
    }

    /**
     * A convenience method for registering the default XTIFF methods into JAI.
     */
    public static void register() {

        // Create the objects
        XTIFFDescriptor odesc = new XTIFFDescriptor();
        register(odesc);
    }
}
