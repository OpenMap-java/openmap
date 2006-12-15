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
//$RCSfile: GeoTIFFImageDecoder.java,v $
//$Revision: 1.2 $
//$Date: 2006/12/15 18:28:29 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image.geotiff;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.dataAccess.image.ImageDecoder;
import com.bbn.openmap.dataAccess.image.ImageTile;

public class GeoTIFFImageDecoder implements ImageDecoder {
    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.imageTile.GeoTIFFImageDecoder");
    GeoTIFFFile gtfFile;

    public GeoTIFFImageDecoder(URL fileURL) {
        try {
            gtfFile = new GeoTIFFFile(fileURL);
        } catch (IllegalArgumentException iae) {
            logger.warning("Problem creating GeoTIFF from " + fileURL);
            iae.printStackTrace();
        } catch (MalformedURLException murle) {

        } catch (IOException ioe) {

        }
    }
    
    public ImageTile getImageTile(ImageTile.Cache cache) {
        try {
            return gtfFile.getImageTile(this, cache);
        } catch (NullPointerException npe) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Problem creating GeoTIFF image (NullPointerException) from "
                        + gtfFile);
                npe.printStackTrace();
            }
        } catch (IOException ioe) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Problem creating GeoTIFF image (IOException) from "
                        + gtfFile);
                ioe.printStackTrace();
            }
        }

        return null;
    }

    public ImageTile getImageTile() {
        return getImageTile(null);
    }

    public BufferedImage getBufferedImage() {
        try {
            return gtfFile.getBufferedImage();
        } catch (NullPointerException npe) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Problem retrieving BufferedImage (NullPointerException) from "
                        + gtfFile);
                npe.printStackTrace();
            }
        } catch (IOException ioe) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Problem retrieving BufferedImage (IOException) from "
                        + gtfFile);
                ioe.printStackTrace();
            }
        }

        return null;
    }
}
