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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/ASRPDirectory.java,v $
// $RCSfile: ASRPDirectory.java,v $
// $Revision: 1.8 $
// $Date: 2005/12/09 21:09:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.bbn.openmap.dataAccess.iso8211.DDFField;
import com.bbn.openmap.dataAccess.iso8211.DDFModule;
import com.bbn.openmap.dataAccess.iso8211.DDFSubfield;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * An ASRP directory contains information needed to view images. It contains
 * multiple files, each containing complementary information about the image.
 * The GeneralInformationFile (GEN) contains information about the image such as
 * coverage and location. The QualityFile (QAL) contains accuracy and color
 * information. The GeoReferenceFile (GER) contains projection information, the
 * SourceFile (SOU) contains information about the map that was used to create
 * the images. The RasterGeoDataFile (IMG) contains the actual pixel
 * information.
 * <P>
 * 
 * This class knows how to use all of these files to create images, which are
 * made up of subframe tiles called blocks.
 */
public class ASRPDirectory extends CacheHandler implements ASRPConstants {

    protected GeneralInformationFile gen;
    protected QualityFile qal;
    protected RasterGeoDataFile img;
    protected GeoReferenceFile ger;
    protected SourceFile sou;

    /** List of tile indexes. */
    protected List tsi;
    /** Number of horizontal blocks. */
    protected int numHorBlocks_N;
    /** Number of vertical blocks. */
    protected int numVerBlocks_M;
    /** Number of horizontal pixels per block. */
    protected int numHorPixels_Q;
    /** Number of vertical pixels per block. */
    protected int numVerPixels_P;
    /**
     * When reading image bytes, the number of bits that represent the number of
     * pixels the next color index stands for.
     */
    protected int pixelCountBits;
    /**
     * When reading image bytes, the number of bits that represent the color
     * index.
     */
    protected int pixelValueBits;

    /* Bounding coordinates for coverage. */
    protected float swo, nea, neo, swa; // west lon, north lat, east
    // lon, south lat
    /* Upper left latitude/longitude for top left tile. */
    protected float lso, pso; // padded longitude, latitude of upper
    // left image corner
    /** Number of pixels 360 degrees east - west. */
    protected int arv;
    /** Number of pixels 360 degrees north - south. */
    protected int brv;
    /**
     * Calculated number of degrees per block in the horizontal direction.
     */
    protected float degPerHorBlock;
    /**
     * Calculated number of degrees per block in the vertical direction.
     */
    protected float degPerVerBlock;
    /** Byte offset into the IMG file where tile data starts. */
    protected int tileDataOffset;
    /** The colors from the QAL file. */
    protected Color[] colors;
    /** The OMRect used to track coverage boundaries. */
    protected OMRect bounds;

    protected File dir;

    /**
     * Protective mechanism, doesn't display data that has images with a base
     * scale that is more than a factor of the scaleFactor away from the scale
     * of the map.
     */
    protected double scaleFactor = 4;

    /**
     * Create a new ASRP directory for the given path. Calls initialize() which
     * will read in the different files to find out the attribute information
     * about the data.
     */
    public ASRPDirectory(String path) {

        dir = new File(path);

        if (dir.exists()) {
            try {
                initialize(dir.getPath(), dir.getName(), "01");
            } catch (IOException ioe) {
                Debug.error(ioe.getMessage());
                ioe.printStackTrace();
                return;
            }
        } else {
            Debug.error("ASRPDirectory (" + path + ") doesn't exist");
        }

    }

    public String getPath() {
        if (dir != null) {
            return dir.getPath();
        }
        return null;
    }

    /**
     * Get the OMRect used for calculating coverage area.
     */
    public OMRect getBounds() {
        if (bounds == null) {
            bounds = new OMRect(pso, lso, pso - degPerVerBlock * numVerBlocks_M, lso
                    + degPerHorBlock * numHorBlocks_N, OMGraphic.LINETYPE_GREATCIRCLE);
        }

        return bounds;
    }

    public void setScaleFactor(double scaleFactorIn) {
        scaleFactor = scaleFactorIn;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Return true of current bounds covers the projection area.
     */
    public boolean isOnMap(Projection proj) {
        OMRect bds = getBounds();
        bds.generate(proj);
        Shape s = bds.getShape();
        return s.intersects(0, 0, proj.getWidth(), proj.getHeight());
    }

    public boolean validScale(Projection proj) {
        if (proj instanceof EqualArc) {
            EqualArc ea = (EqualArc) proj;
            double xPixConstant = ea.getXPixConstant();

            double scale = xPixConstant / arv;

            boolean result = (scale < scaleFactor) && (scale > 1 / scaleFactor);
            if (Debug.debugging("asrp")) {
                Debug.output("Scale comparing arv = " + arv + ", "
                        + xPixConstant + ", result: " + result);
            }
            return result;
        }
        return false;
    }

    /**
     * Get an OMGraphicList of files that cover the projection. Returns an empty
     * list if the coverage isn't over the map.
     */
    public OMGraphicList checkProjAndGetTiledImages(Projection proj)
            throws IOException {

        if (!isOnMap(proj) || !validScale(proj)) {
            // off the map
            return new OMGraphicList();
        }

        return getTiledImages(proj);
    }

    /**
     * Assumes that the projection checks have occurred, have passed, and just
     * fetches the image tiles.
     */
    public OMGraphicList getTiledImages(Projection proj) throws IOException {

        float ullat = pso;
        float ullon = lso;
        float lrlat = ullat - (degPerVerBlock * numVerBlocks_M);
        float lrlon = ullon + (degPerHorBlock * numHorBlocks_N);

        Point2D llp1 = proj.getUpperLeft();
        Point2D llp2 = proj.getLowerRight();

        int startX = (int) Math.floor((llp1.getX() - ullon) / degPerHorBlock);
        int startY = (int) Math.floor((ullat - llp1.getY()) / degPerVerBlock);

        int endX = numHorBlocks_N
                - (int) Math.floor((lrlon - llp2.getX()) / degPerHorBlock);
        int endY = numVerBlocks_M
                - (int) Math.floor((llp2.getY() - lrlat) / degPerVerBlock);

        if (startX < 0)
            startX = 0;
        if (startY < 0)
            startY = 0;

        if (endX > numHorBlocks_N)
            endX = numHorBlocks_N;
        if (endY > numVerBlocks_M)
            endY = numVerBlocks_M;

        return getTiledImages(new Rectangle(startX, startY, endX - startX, endY
                - startY), proj);
    }

    /**
     * Provide an OMGraphicList containing the tile blocks described by the
     * rectangle.
     * 
     * @param rect rectangle defining the tile blocks to get. rect.x and rect.y
     *        describe the starting upper left block to get, rect.getWidth and
     *        rect.getHeight describe the number of tiles to the right and down
     *        from the first block to collect.
     */
    protected OMGraphicList getTiledImages(Rectangle rect, Projection proj)
            throws IOException {

        if (Debug.debugging("asrp")) {
            Debug.output("ASRPDirectory: fielding request for " + rect);
        }

        OMGraphicList list = new OMGraphicList();

        int startX = (int) rect.getX();
        int startY = (int) rect.getY();
        int endX = startX + (int) rect.getWidth();
        int endY = startY + (int) rect.getHeight();

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                OMGraphic omg = (OMGraphic) get(new String(x + "," + y).intern());
                if (omg != null) {
                    omg.generate(proj);
                    list.add(omg);
                }
            }
        }

        return list;
    }

    /**
     * Fetch the subframe tile block from the IMG file.
     */
    public OMScalingRaster getBlock(int x, int y) throws IOException {
        float ullat = pso - y * degPerVerBlock;
        float ullon = lso + x * degPerHorBlock;
        float lrlat = ullat - degPerVerBlock;
        float lrlon = ullon + degPerHorBlock;

        // Get image data.
        if (tsi != null) {

            int index = y * numHorBlocks_N + x;

            // Check for not blowing the list end...
            if (index >= tsi.size()) {
                return null;
            }

            // Subtracting one because the values look like they start
            // with 1.
            int blockOffset = ((DDFSubfield) tsi.get(index)).intValue() - 1;

            if (Debug.debugging("asrp")) {
                Debug.output("ASRPDirectory.getBlock: index of (" + x + ", "
                        + y + ") is " + blockOffset);
            }

            if (blockOffset < 0) {
                // Can't have a negative offset...
                if (Debug.debugging("asrp")) {
                    Debug.output("     skipping...");
                }

                return null;
            }

            DDFModule mod = img.getInfo();
            mod.seek(tileDataOffset + blockOffset);

            int byteCount = 0; // Which data byte is being set
            int numBlockPixels = numHorPixels_Q * numVerPixels_P;
            byte[] data = new byte[numBlockPixels]; // image byte data

            int rowCount = 0; // the per row count, should equal 128 (
            // numHorPixels_Q) at the end of every
            // row
            int cpc = 0; // current pixel count for file pointer
            int cpv = 0; // current pixel value for file pointer
            while (byteCount < numBlockPixels) {
                switch (pixelCountBits) {
                case 8:
                    cpc = mod.read();
                    break;
                case 4:
                    cpc = mod.read() >> 4;
                    // need to back pointer up 4 bits before reading
                    // cpv??
                    Debug.output("CAUTION:  4 bit count");
                    break;
                default:
                    cpc = 1;
                }

                cpv = mod.read();

                // OK, cpv has value, cpc says how many pixels that
                // goes in.

                try {
                    for (int c = 0; c < cpc; c++) {
                        rowCount++;
                        if (colors != null && cpv > colors.length) {
                            if (Debug.debugging("asrpdetail")) {
                                Debug.output("Got value that is too big for colortable");
                            }
                        }
                        data[byteCount + c] = (byte) cpv;
                    }
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    if (Debug.debugging("asrp")) {
                        Debug.output("ASRPDirectory.getBlock(): bad index for setting byte value: "
                                + aioobe.getMessage());
                    }
                    // This try/catch block is really for the data[]
                    // array indexing.

                    // if byteCount + x was greater than
                    // numBlockPixels,
                    // we should be at the end of the image bytes, so
                    // we
                    // shouldn't have to worry about rowCount not
                    // being
                    // properly updated.
                }

                byteCount += cpc;
                if (rowCount == numHorPixels_Q) {
                    rowCount = 0;
                }
            }

            if (Debug.debugging("asrpdetail")) {
                Debug.output("ASRPDirectory creating image covering (" + ullat
                        + ", " + ullon + "), (" + lrlat + ", " + lrlon + ")");
            }

            return new OMScalingRaster(ullat, ullon, lrlat, lrlon, numHorPixels_Q, numVerPixels_P, data, getColors(), 255);
        }

        return null;
    }

    /**
     * Get the colors from the QAL file.
     */
    protected Color[] getColors() {
        if (colors == null) {
            DDFField col = qal.getField(QualityFile.COLOUR_CODE_ID);

            List reds = col.getSubfields("NSR");
            List greens = col.getSubfields("NSG");
            List blues = col.getSubfields("NSB");

            int numColors = reds.size();
            colors = new Color[numColors];

            for (int count = 0; count < numColors; count++) {
                int red = ((DDFSubfield) reds.get(count)).intValue();
                int green = ((DDFSubfield) greens.get(count)).intValue();
                int blue = ((DDFSubfield) blues.get(count)).intValue();
                // Debug.output("Created color " + count + " with " +
                // red + ", " + green + ", " + blue);
                // The zero color is supposed to tbe null color, and
                // clear. Doesn't seem to be working.
                colors[count] = new Color(red, green, blue, (count == 0 ? 0
                        : 255));
            }

        }
        return colors;
    }

    /**
     * Read in the attribute information about the data.
     * 
     * @param dirPath path to the ASRP directory.
     * @param root name of all of the files, usually the same as the ASRP
     *        directory itself.
     * @param DD the occurrence number, usually '01' of the files.
     */
    protected void initialize(String dirPath, String root, String DD)
            throws IOException {
        String rootPath = dirPath + "/" + root + DD + ".";

        gen = new GeneralInformationFile(rootPath + GEN_NAME);
        ger = new GeoReferenceFile(rootPath + GER_NAME);
        qal = new QualityFile(rootPath + QAL_NAME);
        sou = new SourceFile(rootPath + SOURCE_NAME);
        img = new RasterGeoDataFile(rootPath + IMAGE_NAME);

        DDFField sprInfo = gen.getField(GeneralInformationFile.DATA_SET_PARAMETERS);
        numHorBlocks_N = sprInfo.getSubfield("NFC").intValue();
        numVerBlocks_M = sprInfo.getSubfield("NFL").intValue();
        numHorPixels_Q = sprInfo.getSubfield("PNC").intValue();
        numVerPixels_P = sprInfo.getSubfield("PNL").intValue();
        pixelCountBits = sprInfo.getSubfield("PCB").intValue();
        pixelValueBits = sprInfo.getSubfield("PVB").intValue();

        // assume there is a tile index map

        DDFField genInfo = gen.getField(GeneralInformationFile.GENERAL_INFORMATION);
        swo = genInfo.getSubfield("SWO").floatValue() / 3600f;
        neo = genInfo.getSubfield("NEO").floatValue() / 3600f;
        nea = genInfo.getSubfield("NEA").floatValue() / 3600f;
        swa = genInfo.getSubfield("SWA").floatValue() / 3600f;
        lso = genInfo.getSubfield("LSO").floatValue() / 3600f;
        pso = genInfo.getSubfield("PSO").floatValue() / 3600f;

        arv = genInfo.getSubfield("ARV").intValue();
        brv = genInfo.getSubfield("BRV").intValue();

        DDFField timInfo = gen.getField(GeneralInformationFile.TILE_INDEX_MAP);
        tsi = timInfo.getSubfields("TSI");

        DDFField pixelInfo = img.getField(RasterGeoDataFile.PIXEL);
        // Finding this out lets you use the tile index map to access
        // pixel data. This offset points to the start of the tile
        // data.
        tileDataOffset = pixelInfo.getHeaderOffset()
                + pixelInfo.getDataPosition();

        degPerHorBlock = 360f / (float) arv * (float) numHorPixels_Q;
        degPerVerBlock = 360f / (float) brv * (float) numVerPixels_P;

        if (Debug.debugging("asrp")) {
            Debug.output("For " + rootPath + "\n\thave blocks ("
                    + numHorBlocks_N + ", " + numVerBlocks_M
                    + ")\n\twith pixels (" + numHorPixels_Q + ", "
                    + numVerPixels_P + ")");
            Debug.output("\tCoverage from (" + nea + ", " + swo + ") to ("
                    + swa + ", " + neo + ")");
            Debug.output("\tPadded coverage starting at (" + pso + ", " + lso
                    + ")");
            Debug.output("\tNumber of pixels 360 e-w (" + arv + ") , n-s ("
                    + brv + ")");
            Debug.output("\tdegrees per horizontal block: " + degPerHorBlock
                    + ", vertical: " + degPerVerBlock);
            Debug.output("\tImage Data made up of count bits: "
                    + pixelCountBits + ", value bits: " + pixelValueBits);

            if (Debug.debugging("asrpdetail")) {
                Debug.output("Checking...");

                float latdiff = nea - swa;
                float londiff = neo - swo;

                float horPixels = arv * (londiff / 360f);
                float verPixels = brv * (latdiff / 360f);

                Debug.output("\tCalculating " + (horPixels / numHorPixels_Q)
                        + " hor blocks");
                Debug.output("\tCalculating " + (verPixels / numVerPixels_P)
                        + " hor blocks");
                Debug.output("\tCalculating "
                        + (lso + degPerHorBlock * numHorBlocks_N)
                        + " end latitude");
                Debug.output("\tCalculating "
                        + (pso - degPerVerBlock * numVerBlocks_M)
                        + " end latitude");
            }

        }

        getColors();

        gen.close();
        ger.close();
        qal.close();
        sou.close();
        img.close();
    }

    /**
     * A private class to store cached images.
     */
    private static class ASRPBlockCacheObject extends CacheObject {
        /**
         * @param id passed to superclass
         * @param obj passed to superclass
         */
        public ASRPBlockCacheObject(String id, OMGraphic obj) {
            super(id, obj);
        }

        /**
         * Calls dispose() on the contained frame, to make it eligible for
         * garbage collection.
         */
        //protected void finalize() {}
    }

    /**
     * Load a block image into the cache, based on the relative coordinates of
     * the block as a key.
     * 
     * @param key String of form 'x,y' identifying the relative location of
     *        the subframe image.
     * @return Block image, hidden as a CacheObject.
     */
    public CacheObject load(Object key) {
        if (key != null) {
            String xAndY = key.toString();
            int commaIndex = xAndY.indexOf(',');
            int x = Integer.parseInt(xAndY.substring(0, commaIndex));
            int y = Integer.parseInt(xAndY.substring(commaIndex + 1));

            if (Debug.debugging("asrpdetail")) {
                Debug.output("Getting tiled image " + x + ", " + y + " (from "
                        + xAndY + ")");
            }

            try {
                OMGraphic block = getBlock(x, y);
                if (block != null) {
                    return new ASRPBlockCacheObject(xAndY.intern(), block);
                }
            } catch (IOException ioe) {
                Debug.error("ASRPDirectory caught exception creating tiled image for "
                        + xAndY);
            }
        }
        return null;
    }

    public static void main(String[] argv) {
        Debug.init();

        if (argv.length < 1) {
            Debug.output("Usage: ASRPDirectory dir_pathname");
        } else {
            new ASRPDirectory(argv[0]);
        }

        System.exit(0);
    }

}