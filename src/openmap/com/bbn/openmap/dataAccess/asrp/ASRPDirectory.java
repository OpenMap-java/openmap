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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/ASRPDirectory.java,v $
// $RCSfile: ASRPDirectory.java,v $
// $Revision: 1.1 $
// $Date: 2004/03/04 04:14:29 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.asrp;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.dataAccess.iso8211.*;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ASRPDirectory implements ASRPConstants {

    protected GeneralInformationFile gen;
    protected QualityFile qal;
    protected RasterGeoDataFile img;
    protected GeoReferenceFile ger;
    protected SourceFile sou;

    /** List of tile indexes. */
    protected List tsi;

    protected int numHorBlocks_N;
    protected int numVerBlocks_M;
    protected int numHorPixels_Q;
    protected int numVerPixels_P;
    protected int pixelCountBits;
    protected int pixelValueBits;
   
    /* Bounding coordinates for coverage. */
    protected float swo, nea, neo, swa; // west lon, north lat, east lon, south lat
    /* Upper left latitude/longitude for top left tile. */
    protected float lso, pso; // padded longitude, latitude of upper left image corner

    protected int arv;  // number pixels 360 degrees e-w
    protected int brv; // number of pixels 360 degrees n-s

    protected float degPerHorBlock; // horizontal degrees per block
    protected float degPerVerBlock; // vertical degrees per block

    protected int tileDataOffset;

    protected Color[] colors;

    public ASRPDirectory(String path) {

        File dir = new File(path);

        
        if (dir.exists()) {
            try {
                initialize(dir.getPath(), dir.getName(), "01");
            } catch (IOException ioe) {
                Debug.error(ioe.getMessage());
                ioe.printStackTrace();
                return;
            }
        }

    }

    public OMGraphicList getTiledImages(Projection proj) throws IOException {
        float ullat = pso;
        float ullon = lso;

        LatLonPoint llp1 = proj.getUpperLeft();
        LatLonPoint llp2 = proj.getLowerRight();

        int startX = (int) ((llp1.getLongitude() - ullon) / degPerHorBlock);
        int startY = (int) ((ullat - llp1.getLatitude()) / degPerVerBlock);
        
        int endX = (int) ((llp2.getLongitude() - ullon) / degPerHorBlock);
        int endY = (int) ((ullat - llp2.getLatitude()) / degPerVerBlock);

        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;

        if (endX > numHorBlocks_N) endX = numHorBlocks_N;
        if (endY > numVerBlocks_M) endY = numVerBlocks_M;

        return getTiledImages(new Rectangle(startX, startY, endX - startX, endY - startY), proj);
    }

    /**
     * Provide an OMGraphicList containing the tile blocks described by the rectangle.
     * @param rect rectangle defining the tile blocks to get.  rect.x
     * and rect.y describe the starting upper left block to get,
     * rect.getWidth and rect.getHeight describe the number of tiles to the
     * right and down from the first block to collect.
     */
    protected OMGraphicList getTiledImages(Rectangle rect, Projection proj) throws IOException {

        Debug.output("fielding request for " + rect);

        OMGraphicList list = new OMGraphicList();

        int startX = (int)rect.getX();
        int startY = (int)rect.getY();
        int endX = startX + (int)rect.getWidth();
        int endY = startY + (int)rect.getHeight();

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                OMGraphic omg = getBlock(x, y);
                omg.generate(proj);
                list.add(omg);
            }
        }

        return list;
    }

    public OMScalingRaster getBlock(int x, int y) throws IOException {
        float ullat = pso - y*degPerVerBlock;
        float ullon = lso + x*degPerHorBlock;
        float lrlat = ullat - degPerVerBlock;
        float lrlon = ullon + degPerHorBlock;

        // Get image data.
        if (tsi != null) {

            int index = y * numHorBlocks_N + x;

            // Subtracting one because the values look like they start
            // with 1.
            int blockOffset = ((DDFSubfield)tsi.get(index)).intValue() - 1;

            if (Debug.debugging("asrp")) {
                Debug.output("ASRPDirectory.getBlock: index of (" + 
                             x + ", " + y + ") is " + blockOffset);
            }

            DDFModule mod = img.getInfo();
            mod.seek(tileDataOffset + blockOffset);

            int byteCount = 0;  // Which data byte is being set
            int numBlockPixels = numHorPixels_Q * numVerPixels_P;
            byte[] data = new byte[numBlockPixels]; // image byte data

            int rowCount = 0; // the per row count, should equal 128 ( numHorPixels_Q) at the end of every row
            int numRow = 0;
            int cpc = 0; // current pixel count for file pointer
            int cpv = 0; // current pixel value for file pointer
            while (byteCount < numBlockPixels) {
                switch (pixelCountBits) {
                case 8:
                    cpc = mod.read();
                    break;
                case 4:
                    cpc = mod.read() >> 4;
                    // need to back pointer up 4 bits before reading cpv??
                    Debug.output("CAUTION:  4 bit count");
                    break;
                default:
                    cpc = 1;
                }

                cpv = mod.read();
                
                // OK, cpv has value, cpc says how many pixels that goes in.

                for (int c = 0; c < cpc; c++) {
                    rowCount++;
                    if (colors != null && cpv > colors.length) {
                        Debug.output("Got value that is too big for colortable");
                    }
                    data[byteCount + c] = (byte) cpv;
                }
                
                byteCount += cpc;
                if (rowCount == numHorPixels_Q) {
                    rowCount = 0;
                }
            }

            if (Debug.debugging("asrpdetail")) {
                Debug.output("ASRPDirectory creating image covering (" +
                             ullat + ", " +  ullon + 
                             "), (" + lrlat + ", " + lrlon + ")");
            }

            return new OMScalingRaster(ullat, ullon, lrlat, lrlon,
                                       numHorPixels_Q, numVerPixels_P,
                                       data, getColors(), 255);
        }

        return null;
    }


    protected Color[] getColors() {
        if (colors == null) {
            DDFField col = qal.getField(QualityFile.COLOUR_CODE_ID);

            List reds = col.getSubfields("NSR");
            List greens = col.getSubfields("NSG");
            List blues = col.getSubfields("NSB");

            int numColors = reds.size();
            colors = new Color[numColors];

            for (int count = 0; count < numColors; count++) {
                int red = ((DDFSubfield)reds.get(count)).intValue();
                int green = ((DDFSubfield)greens.get(count)).intValue();;
                int blue = ((DDFSubfield)blues.get(count)).intValue();
//                 Debug.output("Created color " + count + " with " + red + ", " + green + ", " + blue);
                colors[count] = new Color(red, green, blue, (count == 0?0:255));
            }

        }
        return colors;
    }
 
    protected void initialize(String dirPath, String root, String DD) throws IOException {
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

        //assume there is a tile index map

        DDFField genInfo = gen.getField(GeneralInformationFile.GENERAL_INFORMATION);
        swo = genInfo.getSubfield("SWO").floatValue()/3600f;
        neo = genInfo.getSubfield("NEO").floatValue()/3600f;
        nea = genInfo.getSubfield("NEA").floatValue()/3600f;
        swa = genInfo.getSubfield("SWA").floatValue()/3600f;
        lso = genInfo.getSubfield("LSO").floatValue()/3600f;
        pso = genInfo.getSubfield("PSO").floatValue()/3600f;

        arv = genInfo.getSubfield("ARV").intValue();
        brv = genInfo.getSubfield("BRV").intValue();

        DDFField timInfo = gen.getField(GeneralInformationFile.TILE_INDEX_MAP);
        tsi = timInfo.getSubfields("TSI");

        DDFField pixelInfo = img.getField(RasterGeoDataFile.PIXEL);
        // Finding this out lets you use the tile index map to access
        // pixel data.  This offset points to the start of the tile
        // data.
        tileDataOffset = pixelInfo.getHeaderOffset() + pixelInfo.getDataPosition();

        degPerHorBlock = 360f/(float)arv * (float)numHorPixels_Q;
        degPerVerBlock = 360f/(float)brv * (float)numVerPixels_P;

        if (Debug.debugging("asrp")) {
            Debug.output("For " + rootPath + "\n\thave blocks (" + 
                         numHorBlocks_N + ", " + numVerBlocks_M + ")\n\twith pixels (" + 
                         numHorPixels_Q + ", " + numVerPixels_P + ")");
            Debug.output("\tCoverage from (" + nea + ", " + swo + ") to (" +
                         swa + ", " + neo + ")");
            Debug.output("\tPadded coverage starting at (" + pso + ", " + lso + ")");
            Debug.output("\tNumber of pixels 360 e-w (" + arv + ") , n-s (" + brv + ")");
            Debug.output("\tdegrees per horizontal block: " + degPerHorBlock +
                         ", vertical: " + degPerVerBlock);
            Debug.output("\tImage Data made up of count bits: " + pixelCountBits +
                         ", value bits: " + pixelValueBits);

            if (Debug.debugging("asrpdetail")) {
                Debug.output("Checking...");

                float latdiff = nea - swa;
                float londiff = neo - swo;

                float horPixels = arv * (londiff/360f);
                float verPixels = brv * (latdiff/360f);

                Debug.output("\tCalculating " + (horPixels / numHorPixels_Q) + " hor blocks");
                Debug.output("\tCalculating " + (verPixels / numVerPixels_P) + " hor blocks");
                Debug.output("\tCalculating " + (lso + degPerHorBlock * numHorBlocks_N) + 
                             " end latitude");
                Debug.output("\tCalculating " + (pso - degPerVerBlock * numVerBlocks_M) + 
                             " end latitude");
            }
            
        }

        getColors();

        gen.close();
        ger.close();
        qal.close();
        sou.close();
        img.close();
   }

    public static void main(String[] argv) {
        Debug.init();

        if (argv.length < 1) {
            Debug.output("Usage: ASRPDirectory dir_pathname");
        } else {
            ASRPDirectory dir = new ASRPDirectory(argv[0]);
        }

        System.exit(0);
    }

}