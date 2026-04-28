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
//$RCSfile: GeoTIFFModelFactory.java,v $
//$Revision: 1.3 $
//$Date: 2007/01/22 15:47:36 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image.geotiff;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotiff.image.KeyRegistry;
import org.geotiff.image.jai.GeoTIFFDirectory;

import com.bbn.openmap.dataAccess.image.ErrImageTile;
import com.bbn.openmap.dataAccess.image.ImageTile;

public class GeoTIFFModelFactory {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.image.geotiff.GeoTIFFModelFactory");

    protected GeoTIFFFile gtfFile;

    public GeoTIFFModelFactory(GeoTIFFFile gtf) {
        gtfFile = gtf;
    }

    public ImageTile getImageTile() throws IOException {
        return getImageTile(null, null);
    }

    public ImageTile getImageTile(GeoTIFFImageReader gtid,
                                  ImageTile.Cache cache) throws IOException {

        int modelType = gtfFile.getModelType();
        ImageTile ret = null;
        String errorMessage = null;
        /*
         * ModelTypeProjected = 1 (Projection Coordinate System)
         * ModelTypeGeographic = 2 Geographic latitude-longitude System)
         * ModelTypeGeocentric = 3 (Geocentric (X,Y,Z) Coordinate System)
         */
        int modelID = -1;
        switch (modelType) {
        case 1:
            modelID = gtfFile.getProjectedCSType();
            errorMessage = "Projection Model type (" + modelID + ", "
                    + KeyRegistry.getKey(KeyRegistry.EPSG_PCS, modelID)
                    + ") not handled yet";

            if (logger.isLoggable(Level.FINE)) {
                logger.info(errorMessage);
            }

            ret = new ErrImageTile("Image can't be positioned: "
                    + errorMessage);
            break;
        case 2:
        case 3:
            modelID = gtfFile.getGeographicType();
            if (logger.isLoggable(Level.FINE)) {
                logger.info("GeoModel type (" + modelID + "): "
                        + KeyRegistry.getKey(KeyRegistry.EPSG_GCS, modelID));
            }

            switch (modelID) {
            case 4326:
                ret = get4326(gtid, cache);
                break;
            default:
                errorMessage = "GeoModel type (" + modelID + ", "
                        + KeyRegistry.getKey(KeyRegistry.EPSG_GCS, modelID)
                        + ") not handled yet";
                logger.info(errorMessage);
                ret = new ErrImageTile("Image can't be positioned: "
                        + errorMessage);
            }
        }

        return ret;
    }

    protected ImageTile get4326(GeoTIFFImageReader gtid, ImageTile.Cache cache)
            throws IOException {
        // GCS_WGS_84
        GeoTIFFDirectory gtfd = gtfFile.getGtfDirectory();
        // There's got to be a way to figure out the pixel height and width of
        // the image without having to create the BufferedImage. We need those
        // for georeferencing the lower right corner right now, but it would be
        // good to only create the image if an ImageDecoder and cache weren't
        // provided.
        double[] tiePoints = gtfd.getTiepoints();
        double[] scaleMatrix = gtfd.getPixelScale();

        int imageWidth = gtfFile.getFieldIntValue(256);
        int imageHeight = gtfFile.getFieldIntValue(257);

        double ulat = tiePoints[4] + tiePoints[1] * scaleMatrix[1];
        double llon = tiePoints[3] - tiePoints[0] * scaleMatrix[0];
        double llat = tiePoints[4] - imageHeight * scaleMatrix[1];
        double rlon = tiePoints[3] + imageWidth * scaleMatrix[0];

        if (logger.isLoggable(Level.FINE)) {
            logger.info("Image should be at: " + ulat + ", " + llon
                    + " - to - " + llat + ", " + rlon);
        }

        if (gtid == null) {
            BufferedImage bi = gtfFile.getBufferedImage();
            return new ImageTile((float) ulat, (float) llon, (float) llat, (float) rlon, bi);
        } else {
            return new ImageTile((float) ulat, (float) llon, (float) llat, (float) rlon, gtid, cache);
        }
    }
}
