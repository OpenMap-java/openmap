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
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.util;

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.dataAccess.image.WorldFile;
import com.bbn.openmap.geo.ConvexHull;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.geo.GeoArray;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.Debug;

/**
 * The ImageTranslator is the object that takes a BufferedImage and creates an
 * OMRaster from it based on a Projection object.
 */
public class ImageWarp {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.omGraphics.util.ImageWarp");

    /**
     * Source image pixels.
     */
    protected int[] pixels = null;

    /** Image width, */
    protected int iwidth;
    /** Image height, */
    protected int iheight;
    /**
     * Horizontal units/pixel in the source BufferedImage projection. Assumed to
     * be constant across the image.
     */
    protected double hor_upp;
    /**
     * Vertical units/pixel in the source BufferedImage projection. Assumed to
     * be constant across the image.
     */
    protected double ver_upp;
    /**
     * The vertical origin pixel location in the source image for the coordinate
     * system origin.
     */
    protected double verOrigin;
    /**
     * The horizontal origin pixel location in the source image for the
     * coordinate system origin.
     */
    protected double horOrigin;

    /**
     * A transformation for the projection of the source image. If not set, the
     * image is assumed to be equal arc projection.
     */
    protected GeoCoordTransformation geoTrans = new LatLonGCT();

    /**
     * The coordinate bounds of the image, represented in the coordinate system
     * of the image.
     */
    protected DataBounds sourceImageBounds;

    /**
     * The coordinate image bounds of the projected image on the map window.
     */
    protected DataBounds projectedImageBounds;

    /**
     * Create an image warp for an image assumed to be world wide coverage, with
     * the top at 90 degrees, the bottom at -90, the left side at -180 and the
     * right side at 180. Assumes the origin point is in the middle of the
     * image.
     */
    public ImageWarp(BufferedImage bi) {
        this(bi, LatLonGCT.INSTANCE, new DataBounds(-180.0, -90.0, 180.0, 90.0));
    }

    /**
     * Create an image warp with some additional transform information.
     * 
     * @param bi BufferedImage of the source
     * @param transform the GeoCoordTransformation for the projection of the
     *        image.
     * @param imageBounds the bounds of the image in the image's coordinate
     *        system.
     */
    public ImageWarp(BufferedImage bi, GeoCoordTransformation transform, DataBounds imageBounds) {
        if (bi != null) {
            iwidth = bi.getWidth();
            iheight = bi.getHeight();
            setGeoTrans(transform);
            setImageBounds(imageBounds);

            pixels = getPixels(bi, 0, 0, iwidth, iheight);

            // See if this saves on memory. Seems to.
            bi = null;
        }
    }

    /**
     * Create an image warp with some additional transform information.
     * 
     * @param bi BufferedImage of the source
     * @param transform the GeoCoordTransformation for the projection of the
     *        image.
     * @param worldFile the WorldFile describing the image's location.
     */
    public ImageWarp(BufferedImage bi, GeoCoordTransformation transform, WorldFile worldFile) {
        if (bi != null) {
            iwidth = bi.getWidth();
            iheight = bi.getHeight();
            setGeoTrans(transform);

            setImageBounds(worldFile);

            pixels = getPixels(bi, 0, 0, iwidth, iheight);

            // See if this saves on memory. Seems to.
            bi = null;
        }
    }

    /**
     * Create an image warp for an image assumed to be world wide coverage, with
     * the top at 90 degrees, the bottom at -90, the left side at -180 and the
     * right side at 180. Assumes the origin point is in the middle of the
     * image.
     * 
     * @param pix ARGB array of pixel values for image.
     * @param width pixel width of image.
     * @param height pixel height of image.
     */
    public ImageWarp(int[] pix, int width, int height) {
        this(pix, width, height, LatLonGCT.INSTANCE, new DataBounds(-180.0, -90.0, 180.0, 90.0));
    }

    /**
     * Create an image warp with some additional transform information.
     * 
     * @param pix ARGB array of pixel values for image.
     * @param width pixel width of image.
     * @param height pixel height of image.
     * @param transform the GeoCoordTransformation for the projection of the
     *        image.
     * @param imageBounds the bounds of the image in the image's coordinate
     *        system.
     */
    public ImageWarp(int[] pix, int width, int height, GeoCoordTransformation transform,
            DataBounds imageBounds) {
        if (pix != null) {
            iwidth = width;
            iheight = height;
            setGeoTrans(transform);
            setImageBounds(imageBounds);
            pixels = pix;
        }
    }

    /**
     * Create an image warp with some additional transform information.
     * 
     * @param pix ARGB array of pixel values for image.
     * @param width pixel width of image.
     * @param height pixel height of image.
     * @param transform the GeoCoordTransformation for the projection of the
     *        image.
     * @param worldFile the WorldFile describing the image's location.
     */
    public ImageWarp(int[] pix, int width, int height, GeoCoordTransformation transform,
            WorldFile worldFile) {
        if (pix != null) {
            iwidth = width;
            iheight = height;
            setGeoTrans(transform);
            setImageBounds(worldFile);
            pixels = pix;
        }
    }

    /**
     * The pixels used in the OMRaster.
     */
    // int[] tmpPixels = new int[0];
    /**
     * Return an OMRaster that covers the given projection, with the image
     * warped for the projection.
     * 
     * @param p map projection
     * @return OMRaster or null if the image isn't within the current
     *         projection.
     */
    public OMRaster getOMRaster(Projection p) {
        int[] pixels = getImagePixels(p);
        if (pixels != null && projectedImageBounds != null) {
            int width = (int) Math.ceil(projectedImageBounds.getWidth());
            int height = (int) Math.ceil(projectedImageBounds.getHeight());
            int x = (int) Math.floor(projectedImageBounds.getMin().getX());
            int y = (int) Math.floor(projectedImageBounds.getMin().getY());
            OMRaster raster = new OMRaster(x, y, width, height, pixels);
            raster.generate(p);
            return raster;
        }

        return null;
    }

    /**
     * Given a projection, return the pixels for an image that will cover the
     * projection area.
     * 
     * @param p map projection
     * @return int[] of ARGB pixels for an image covering the given projection.
     */
    public int[] getImagePixels(Projection p) {
        if (pixels != null && p != null) {

            projectedImageBounds = calculateProjectedImageBounds(p);

            if (projectedImageBounds == null) {
                // image isn't on the map.
                return null;
            }

            int projHeight = (int) Math.ceil(projectedImageBounds.getHeight());
            int projWidth = (int) Math.ceil(projectedImageBounds.getWidth());

            // See if we can reuse the pixel array we have.

            int[] tmpPixels = new int[projWidth * projHeight];
            int numTmpPixels = tmpPixels.length;
            logger.fine("tmpPixels[" + numTmpPixels + "]");
            int clear = 0x00000000;

            Point2D ctp = new Point2D.Double();
            Point2D ddll = new Point2D.Double();
            Point2D imageCoord = new Point2D.Double();
            Point2D center = p.getCenter();

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(projectedImageBounds.toString());
            }

            int minx = (int) Math.floor(projectedImageBounds.getMin().getX());
            int miny = (int) Math.floor(projectedImageBounds.getMin().getY());
            int maxx = (int) Math.ceil(projectedImageBounds.getMax().getX());
            int maxy = (int) Math.ceil(projectedImageBounds.getMax().getY());

            // i and j are map window pixel values.
            for (int i = minx; i < maxx; i++) {
                for (int j = miny; j < maxy; j++) {

                    // ix and iy are pixel coordinates of the destination image.
                    int ix = i - minx;
                    int iy = j - miny;

                    // index into the OMRaster pixel array
                    int tmpIndex = (ix + (iy * projWidth));

                    if (tmpIndex >= numTmpPixels) {
                        continue;
                    }

                    ddll = p.inverse(i, j, ddll);

                    // If the llp calculated isn't on the map,
                    // don't bother drawing it. Could be a space
                    // point in Orthographic projection, for
                    // instance.
                    if (ddll.equals(center)) {
                        p.forward(ddll, ctp);
                        if (ctp.getX() != i || ctp.getY() != j) {
                            tmpPixels[tmpIndex] = clear;
                            continue;
                        }
                    }

                    if (geoTrans != null) {
                        geoTrans.forward(ddll.getY(), ddll.getX(), imageCoord);
                    } else {
                        imageCoord = ddll;
                    }

                    if (!sourceImageBounds.contains(imageCoord)) {
                        tmpPixels[tmpIndex] = clear;
                        continue;
                    }

                    // Find the corresponding pixel location in
                    // the source image.
                    int horIndex = (int) Math.round(horOrigin + (imageCoord.getX() / hor_upp));
                    int verIndex = (int) Math.round(verOrigin + (imageCoord.getY() / ver_upp));

                    if (horIndex < 0 || horIndex >= iwidth || verIndex < 0 || verIndex >= iheight) {
                        // pixel not on the source image. This
                        // happens if the image doesn't cover the
                        // entire earth.
                        continue;
                    }

                    int imageIndex = horIndex + (verIndex * iwidth);

                    if (imageIndex >= 0 && imageIndex < pixels.length) {
                        tmpPixels[tmpIndex] = pixels[imageIndex];
                    }
                }
            }

            logger.fine("finished creating image");
            return tmpPixels;
        }

        logger.warning("problem creating image, no pixels: " + (pixels == null ? "true" : "false")
                + ", no projection:" + (p == null ? "true" : "false"));

        // If you get here, something's not right.
        return null;
    }

    protected DataBounds calculateProjectedImageBounds(Projection p) {

        // This doesn't seem to do anything but slow things down.
        // if (geoTrans.equals(LatLonGCT.INSTANCE)) {
        // // whole earth
        // logger.fine("just using whole screen image");
        // return new DataBounds(0, 0, p.getWidth(), p.getHeight());
        // }

        DataBounds db = null;
        if (sourceImageBounds != null) {
            int pw = p.getWidth();
            int ph = p.getHeight();
            Point2D min = sourceImageBounds.getMin();
            Point2D max = sourceImageBounds.getMax();
            double x1 = Math.floor(min.getX());
            double y1 = Math.floor(min.getY());
            double x2 = Math.ceil(max.getX());
            double y2 = Math.ceil(max.getY());
            double width = sourceImageBounds.getWidth();
            double height = sourceImageBounds.getHeight();

            // These are just memory savers, reused for every calculation.
            LatLonPoint tmpG = new LatLonPoint.Double();
            Point2D tmpP = new Point2D.Double();

            db = new DataBounds();
            db.setHardLimits(new DataBounds(0, 0, pw, ph));
            db.add(p.forward(geoTrans.inverse(x1, y1, tmpG), tmpP));
            db.add(p.forward(geoTrans.inverse(x1, y2, tmpG), tmpP));
            db.add(p.forward(geoTrans.inverse(x2, y1, tmpG), tmpP));
            db.add(p.forward(geoTrans.inverse(x2, y2, tmpG), tmpP));

            double numSplits = 4;

            double xSpacer = width / numSplits;
            double ySpacer = height / numSplits;

            for (int i = 1; i < numSplits; i++) {
                db.add(p.forward(geoTrans.inverse(Math.ceil(x1 + xSpacer * i), y1, tmpG), tmpP));
                db.add(p.forward(geoTrans.inverse(x1, Math.ceil(y1 + ySpacer * i), tmpG), tmpP));
                db.add(p.forward(geoTrans.inverse(Math.ceil(x1 + xSpacer * i), y2, tmpG), tmpP));
                db.add(p.forward(geoTrans.inverse(x2, Math.ceil(y1 + ySpacer * i), tmpG), tmpP));
            }

            if (db.getWidth() <= 0 || db.getHeight() <= 0) {
                logger.fine("dimensions of data bounds bad, returning null " + db);
                return null;
            }

        }
        return db;

    }

    /**
     * Get the pixels from the BufferedImage. If anything goes wrong, returns a
     * int[0].
     */
    protected int[] getPixels(Image img, int x, int y, int w, int h) {
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            Debug.error("ImageTranslator: interrupted waiting for pixels!");
            return new int[0];
        }

        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("ImageTranslator: image fetch aborted or errored");
            return new int[0];
        }

        return pixels;
    }

    public int getIwidth() {
        return iwidth;
    }

    public void setIwidth(int iwidth) {
        this.iwidth = iwidth;
    }

    public int getIheight() {
        return iheight;
    }

    public void setIheight(int iheight) {
        this.iheight = iheight;
    }

    public double getHor_dpp() {
        return hor_upp;
    }

    public void setHor_dpp(double hor_dpp) {
        this.hor_upp = hor_dpp;
    }

    public double getVer_dpp() {
        return ver_upp;
    }

    public void setVer_dpp(double ver_dpp) {
        this.ver_upp = ver_dpp;
    }

    public double getVerOrigin() {
        return verOrigin;
    }

    public void setVerOrigin(double verOrigin) {
        this.verOrigin = verOrigin;
    }

    public double getHorOrigin() {
        return horOrigin;
    }

    public void setHorOrigin(double horOrigin) {
        this.horOrigin = horOrigin;
    }

    public GeoCoordTransformation getGeoTrans() {
        return geoTrans;
    }

    public void setGeoTrans(GeoCoordTransformation geoTrans) {
        this.geoTrans = geoTrans;
    }

    public DataBounds getImageBounds() {
        return sourceImageBounds;
    }

    public void setImageBounds(DataBounds imageBounds) {
        this.sourceImageBounds = imageBounds;

        hor_upp = imageBounds.getWidth() / iwidth;
        // need the negative sign because latitudes increase in the opposite
        // direction as y pixel values.
        boolean yDirUp = imageBounds.isyDirUp();

        ver_upp = imageBounds.getHeight() / iheight;
        if (yDirUp) {
            ver_upp *= -1;
        }

        // We should be able to just go from the lower left corner of the image
        // and find zero from there, the min of both bounds values.

        double leftX = imageBounds.getMin().getX();
        double upperY = yDirUp ? imageBounds.getMax().getY() : imageBounds.getMin().getY();

        verOrigin = -upperY / ver_upp; // number of Y pixels to origin.
        horOrigin = -leftX / hor_upp; // number of X pixels to origin.

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getting image pixels w:" + iwidth + ", h:" + iheight + ", hor upp:"
                    + hor_upp + ", ver upp:" + ver_upp + ", verOrigin:" + verOrigin
                    + ", horOrigin:" + horOrigin);
            logger.fine(imageBounds.toString());
        }
    }

    public void setImageBounds(WorldFile worldFile) {
        hor_upp = worldFile.getXDim();
        // world file dimensions have direction, negative for going down
        ver_upp = worldFile.getYDim();

        double leftX = worldFile.getX();
        double upperY = worldFile.getY();

        verOrigin = -worldFile.getY() / ver_upp; // number of Y pixels to
        // origin.
        horOrigin = -leftX / hor_upp; // number of X pixels to origin.

        sourceImageBounds = new DataBounds(leftX, worldFile.getY() + ver_upp * iheight, leftX
                + hor_upp * iwidth, upperY);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getting image pixels w:" + iwidth + ", h:" + iheight + ", hor upp:"
                    + hor_upp + ", ver upp:" + ver_upp + ", verOrigin:" + verOrigin
                    + ", horOrigin:" + horOrigin);
            logger.fine(sourceImageBounds.toString());
        }
    }

    public static void main(String[] args) {
        new ImageWarp(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB), LatLonGCT.INSTANCE, new DataBounds(25, -90, 180, 90));
    }
}
