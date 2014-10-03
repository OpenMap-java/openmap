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
//$RCSfile: WorldFileImageReader.java,v $
//$Revision: 1.1 $
//$Date: 2007/01/22 15:47:34 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;

import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

public class WorldFileImageReader implements ImageReader {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.image.WorldFileImageReader");
    protected WorldFile worldFile;
    protected URL fileURL;

    public WorldFileImageReader(URL fileURL) throws MalformedURLException,
            IOException {
        this.fileURL = fileURL;
        worldFile = WorldFile.get(fileURL);
    }

    public BufferedImage getBufferedImage() {
        try {
            BufferedImage bi = getJAIImage(fileURL);
            if (bi == null && worldFile != null) {
                // This means that the world file was found, and that the image
                // wasn't loaded. The most likely problem that causes this
                // situation is that JAI isn't installed on the system. So,
                // let's try using ImageIO.
                bi = getImageIOImage(fileURL);
            }

            return bi;
        } catch (IOException ioe) {
            logger.info("problem reading " + fileURL + ", IOException");
            ioe.printStackTrace();
        }
        return null;
    }

    public static BufferedImage getImageIOImage(URL fileURL) throws IOException {
        FileCacheImageInputStream fciis = new FileCacheImageInputStream(fileURL.openStream(), null);
        BufferedImage fileImage = ImageIO.read(fciis);
        return fileImage;
    }

    public static BufferedImage getJAIImage(URL fileURL) throws IOException {
        return BufferedImageHelper.getJAIBufferedImage("url", fileURL);
    }

    public ImageTile getImageTile(ImageTile.Cache cache) {
        try {
            if (worldFile instanceof ErrWorldFile) {
                return new ErrImageTile(((ErrWorldFile) worldFile).getProblemMessage());
            } else if (worldFile != null) {
                BufferedImage bi = getBufferedImage();
                if (bi != null) {

                    double ulat = worldFile.getY();
                    double llon = worldFile.getX();
                    double llat = ulat + worldFile.getYDim() * bi.getHeight();
                    double rlon = llon + worldFile.getXDim() * bi.getWidth();

                    if (logger.isLoggable(Level.FINE)) {
                        logger.info("Image should be at: " + ulat + ", " + llon
                                + " - to - " + llat + ", " + rlon);
                    }

                    return new ImageTile((float) ulat, (float) llon, (float) llat, (float) rlon, this, cache);
                }
            } else {
                logger.info("World file for " + fileURL + " can't be found.");
            }

        } catch (NullPointerException npe) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Problem creating image (NullPointerException) from "
                        + fileURL);
                npe.printStackTrace();
            }
        }

        return null;
    }

    public ImageTile getImageTile() {
        return getImageTile(null);
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("WorldFileImageReader:  Need a path/filename");
            System.exit(0);
        }

        logger.info("WorldFileImageReader: " + args[0]);

        String filePath = null;

        if (args.length > 0) {
            filePath = args[0];
        }

        if (filePath != null) {
            try {
                URL fileURL = PropUtils.getResourceOrFileOrURL(filePath);
                if (fileURL != null) {

                    WorldFileImageReader wfir = new WorldFileImageReader(fileURL);

                    BufferedImage bi = wfir.getBufferedImage();

                    CADRG crg = new CADRG(new LatLonPoint.Double(0, 0), 1500000, 600, 600);

                    final OMRaster omsr = new OMRaster(0, 0, bi);
                    omsr.generate(crg);

                    java.awt.Frame window = new java.awt.Frame(filePath) {
                        public void paint(java.awt.Graphics g) {
                            if (omsr != null) {
                                omsr.render(g);
                            }
                        }
                    };

                    window.addWindowListener(new java.awt.event.WindowAdapter() {
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            // need a shutdown event to notify other gui beans
                            // and
                            // then exit.
                            System.exit(0);
                        }
                    });

                    window.setSize(omsr.getWidth(), omsr.getHeight());
                    window.setVisible(true);
                    window.repaint();

                }

            } catch (MalformedURLException murle) {

            } catch (IOException ioe) {

            }
        }
    }

}
