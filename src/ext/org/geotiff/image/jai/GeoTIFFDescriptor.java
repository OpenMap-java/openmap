package org.geotiff.image.jai;

/*
 * JAI GeoTIFF extensions.
 * Copyright (c) 2000 Niles Ritter.
 */
import org.libtiff.jai.codec.XTIFFDirectory;
import org.libtiff.jai.operator.XTIFFDescriptor;

/**
 * The GeoTIFFDescriptor is a single class that is both an OperationDescriptor
 * and a RenderedImageFactory for the overridden "tiff" operation.
 * 
 * @author Niles Ritter
 */

public class GeoTIFFDescriptor extends XTIFFDescriptor {

    /**
     * The public Constructor.
     */
    public GeoTIFFDescriptor() {
        super();
    }

    private static boolean alreadyCalled = false;

    /**
     * A convenience method for registering the "geotiff" methods into JAI. This
     * needs only be called once before using GeoTIFF methods.
     */
    public synchronized static void register() {
        if (!alreadyCalled) {
            // Create the objects
            GeoTIFFDescriptor odesc = new GeoTIFFDescriptor();
            XTIFFDescriptor.register(odesc);

            // Tell XTIFF to create a GeoTIFF directory instead of XTIFF.
            XTIFFDirectory.setFactory(new GeoTIFFFactory());
            alreadyCalled = true;
        }
    }
}
