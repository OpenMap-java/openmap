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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfColortable.java,v $
// $RCSfile: RpfColortable.java,v $
// $Revision: 1.5 $
// $Date: 2007/02/26 17:34:03 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * The meat of this code is based on source code provided by
 * The MITRE Corporation, through the browse application source
 * code.  Many thanks to Nancy Markuson who provided BBN with the
 * software, and Theron Tock, who wrote the software, and to
 * Daniel Scholten, who revised it - (c) 1994 The MITRE
 * Corporation for those parts, and used/distributed with permission.
 */

package com.bbn.openmap.layer.rpf;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

/**
 * Set up the colors used in creating the images. They are created from RGB
 * value arrays read in from the RPF file. If the number of colors that are
 * allowed is less than 216, then the RpfColortable looks inside the RpfFile and
 * uses the color conversion tables inside. There will still be 216 color
 * indexes, but some of the colors will be duplicates.
 */
public class RpfColortable {

    public final static int CADRG_COLORS = 216;
    public final static int COLORS_216 = 0;
    public final static int COLORS_32 = 1;
    public final static int COLORS_16 = 2;
    public final static int CIB_SPEC_CODE_ID = 3;
    public final static int DEFAULT_OPAQUENESS = 255;

    /**
     * Color conversion table (to be filled) from within frame, colortable
     * section. The colortable is always 216 entries long. If you want fewer
     * colors, some of the entries are duplicated.
     */
    public int[] colorConvTable = new int[CADRG_COLORS];
    /** Index to use a color conversion table, and if so, which one. */
    protected int reducedColorTable = COLORS_216;
    protected int numColors = 0;
    protected boolean Cib = false;
    protected int opaqueness = DEFAULT_OPAQUENESS;
    /** The actual OMColors to use in the image construction. */
    public Color[] colors = null;
    /** Zone ID for these colors. */
    public char zone;
    /** Chart Series Code for these colors. */
    public String seriesCode;
    /**
     * The index of the A.TOC file in the RpfTocHandler being used for the
     * current colors.
     */
    protected int tocNumber = -1;
    /**
     * The index of the RpfEntry in the A.TOC file being used for the current
     * colors.
     */
    protected int entryNumber = -1;

    public RpfColortable() {
        this(CADRG_COLORS, DEFAULT_OPAQUENESS, false);
    }

    public RpfColortable(int nColors) {
        this(nColors, DEFAULT_OPAQUENESS, false);
    }

    public RpfColortable(int nColors, int opaque, boolean cib) {
        setNumColors(nColors);
        setOpaqueness(opaque);
        setCib(cib);
    }

    /**
     * Load the provided colortable with the color values of this colortable.
     * The A.TOC values that are set by the RpfFrameCacheHandler are not set,
     * just the color table information read from the RPF frame file.
     * 
     * @param colortable
     */
    public void setFrom(RpfColortable colortable) {
        colors = colortable.colors;
        reducedColorTable = colortable.reducedColorTable;
        colorConvTable = colortable.colorConvTable;
        Cib = colortable.Cib;
        opaqueness = colortable.opaqueness;
        zone = colortable.zone;
    }

    /**
     * Set the alpha values of the OMColors, which governs the
     * transparency/opaqueness of the images.
     * 
     * @param value index between 0-255 (0 is transparent, 255 is opaque)
     */
    public void setOpaqueness(int value) {
        opaqueness = value;

        if (colors != null) {
            for (int i = 0; i < colors.length; i++) {
                Color tmp = colors[i];
                colors[i] = new Color(tmp.getRed(), tmp.getGreen(), tmp.getBlue(), opaqueness);
            }
        }
    }

    public int getOpaqueness() {
        return opaqueness;
    }

    /**
     * Set the alpha values of the OMColors, which governs the
     * transparency/opaqueness of the images. This method lets you set the value
     * as a percentage between 0-100.
     * 
     * @param percent index between 0-100 (0 is transparent, 100 is opaque)
     */
    public void setOpaquePercent(int percent) {
        setOpaqueness((int) ((float) (percent * 2.55)));
    }

    public int getOpaquePercent() {
        return (int) ((float) opaqueness * 100.0 / 255.0);
    }

    public void setNumColors(int numColorsValue) {
        numColors = numColorsValue;

        if (numColors >= 216)
            reducedColorTable = COLORS_216;
        else if (numColors >= 32)
            reducedColorTable = COLORS_32;
        else
            reducedColorTable = COLORS_16;
    }

    /** Returns the number of colors. */
    public int getNumColors() {
        return numColors;
    }

    /**
     * Returns the color reduction index. These values correspond to the
     * constants defined in this class.
     */
    public int getColorTableReduction() {
        return reducedColorTable;
    }

    /**
     * If this object is going to provide colors for CIB imagery, you have to
     * let this object know that. Set this to true. It is false by default.
     * 
     * @param value true if the colortable will be used for greyscale images.
     */
    public void setCib(boolean value) {
        Cib = value;
    }

    public boolean isCib() {
        return Cib;
    }

    /**
     * Should be set when a new colortable is read in, so that you can tell when
     * you don't have to read a new one.
     */
    public void setATOCIndexes(int tocIndex, int entryIndex) {
        tocNumber = tocIndex;
        entryNumber = entryIndex;
    }

    /**
     * Return true of the toc index and entry index are the same as what the
     * colortable is currently holding.
     */
    public boolean isSameATOCIndexes(int tocIndex, int entryIndex) {
        return (tocIndex == tocNumber && entryIndex == entryNumber);
    }

    /**
     * Not really used, but someone might need them. Returns the A.TOC index
     * number of the colors, to compare to see if a new colortable is needed.
     */
    public int getTocNumber() {
        return tocNumber;
    }

    /**
     * Not really used, but someone might need them. Returns the A.TOC entry
     * number of the colors, to compare to see if a new colortable is needed.
     */
    public int getEntryNumber() {
        return entryNumber;
    }

    /**
     * The method to call to read in the colortable from within the RPF file.
     * The method will use the input to determine where in the file to read
     * from.
     * 
     * @param binFile the file to read it in from.
     * @param loc the RpfLocationRecord that tells you where the sections are.
     * @return an array of OMColors to use in images.
     */
    public Color[] parseColorLookUpTable(BinaryFile binFile,
                                         RpfFileSections.RpfLocationRecord[] loc) {

        if (Debug.debugging("rpfcolortable")) {
            Debug.output("RpfColortable:  creating new colors for colortable.");
        }

        // change this to the proper color structure
        Color[] rgb = new Color[CADRG_COLORS]; /* DKS NEW: 216 */
        int i, j;

        long ncr;
        int red, green, blue, alpha;

        int numColorOffsetRecs; // uchar, # of color/gray offset
        // records */
        int numColorConvOffsetRecs; // uchar
        int offsetRecordLength = 17; // ushort

        /* see frame.h */
        ColorOffset[] colorOffset;

        long colormapOffsetTableOffset; // uint

        /* color converter subsection hdr */
        long colorConvOffsetTableOffset; // uint
        int colorConvOffsetRecl; // ushort
        int colorConvRecl; // ushort

        boolean foundLUT; /* found lut flag */

        if (Debug.debugging("rpfdetail")) {
            Debug.output("ENTER PARSE Colortable");
        }

        try {
            /* Go find the color table: loc[0].id=LOC_CLUT */
            if (Debug.debugging("rpfdetail")) {
                Debug.output("RpfColortable: Color/gray section subheader (ID=134) location: "
                        + loc[0].componentLocation);
            }
            binFile.seek(loc[0].componentLocation);

            /* Read section subheader */
            /* Number of offset records: 2 */
            numColorOffsetRecs = binFile.read();
            /* Number of cc offset records: 3 */
            numColorConvOffsetRecs = binFile.read();

            if (Debug.debugging("rpfdetail")) {
                Debug.output("RpfColortable: numColorOffsetRecs(3): "
                        + numColorOffsetRecs);
                Debug.output("RpfColortable: numColorConvOffsetRecs(2): "
                        + numColorConvOffsetRecs);
            }

            /* DKS. New, read array of structures */
            /* Read colormap offset table */
            colorOffset = new ColorOffset[numColorOffsetRecs];

            /*
             * DKS. Read color/gray offset records (colormap subsection)
             */
            if (Debug.debugging("rpfdetail")) {
                Debug.output("RpfColortable: Colormap subsection loc[1]: "
                        + loc[1].componentLocation);
            }

            binFile.seek(loc[1].componentLocation);

            /* colormap offset table offset: length 4 */
            colormapOffsetTableOffset = (long) binFile.readInteger();

            /* offset record length:17? length 2 */
            offsetRecordLength = (int) binFile.readShort();

            if (Debug.debugging("rpfdetail")) {
                Debug.output("RpfColortable: colormapOffsetTableOffset: "
                        + colormapOffsetTableOffset);
                Debug.output("RpfColortable: offsetRecordLength:"
                        + offsetRecordLength);
            }

            if (reducedColorTable == COLORS_216 || Cib) { /*
                                                             * 216 or 217 colors
                                                             * desired. No cct
                                                             * reading needed
                                                             */
                /* Read colormap offset table */
                for (i = 0; i < numColorOffsetRecs; i++) { /* 3 */
                    colorOffset[i] = new ColorOffset();

                    colorOffset[i].tableId = (int) binFile.readShort();
                    colorOffset[i].numColorRecords = (long) (binFile.readInteger() & 0xFFFFFFFFL);
                    colorOffset[i].colorElementLength = binFile.read();
                    colorOffset[i].histogramRecordLength = (int) binFile.readShort();
                    colorOffset[i].colorTableOffset = (long) binFile.readInteger() & 0xFFFFFFFFL;
                    colorOffset[i].histogramTableOffset = (long) binFile.readInteger() & 0xFFFFFFFFL;

                    if (Debug.debugging("rpfdetail")) {
                        Debug.output("RpfColortable: Parse_clut: " + i);
                        Debug.output(colorOffset[i].toString());
                    }

                    // May look hackish, but 3 is the specification
                    // number for CIB
                    if (colorOffset[i].tableId == CIB_SPEC_CODE_ID) {
                        Cib = true;
                    } else {
                        Cib = false;
                    }

                    /* look for numColorRecords[i] == 216 or 217 */
                    ncr = colorOffset[i].numColorRecords;
                    if ((ncr == 216) || (ncr == 217))
                        foundLUT = true;
                    else
                        foundLUT = false;

                    if (Debug.debugging("rpfdetail")) {
                        Debug.output("RpfColortable: foundLUT of desired 216?: "
                                + foundLUT);
                    }

                    if (foundLUT) {

                        /*
                         * Read the color/gray records: 216 or 217 (transp)
                         * color table.
                         */
                        /* loc[1] is colormap subsection */
                        binFile.seek(loc[1].componentLocation
                                + colorOffset[i].colorTableOffset);
                        if (ncr >= CADRG_COLORS) {
                            if (Debug.debugging("rpf")) {
                                Debug.error("RpfColortable: ncr is not correct, wingin' it ("
                                        + ncr + ")");
                            }
                            ncr = CADRG_COLORS;
                        }
                        for (j = 0; j < ncr; j++) { /* 216 or 217 */
                            colorConvTable[j] = j;
                            // Allocate the OMColor here......
                            if (Cib) {
                                red = binFile.read() & 0x00ff; /*
                                                                 * read mono
                                                                 * byte value
                                                                 */
                                alpha = opaqueness;
                                green = red;
                                blue = red;
                            } else {
                                red = binFile.read() & 0x00ff; /*
                                                                 * read byte
                                                                 * value
                                                                 */
                                green = binFile.read() & 0x00ff; /*
                                                                     * read byte
                                                                     * value
                                                                     */
                                blue = binFile.read() & 0x00ff; /*
                                                                 * read byte
                                                                 * value
                                                                 */
                                alpha = binFile.read(); /*
                                                         * read byte value
                                                         */

                                alpha = opaqueness;

                                /* DKS NEW TRANSP */
                                if (ncr == 217 && rgb[(int) (ncr - 1)] == null) { /*
                                                                                     * transp
                                                                                     * exists
                                                                                     */
                                    alpha = 255;
                                    red = 255;
                                    green = 255;
                                    blue = 255;
                                    rgb[(int) (ncr - 1)] = new Color(red, green, blue, alpha);
                                } /* if */
                            } /* else */

                            rgb[j] = new Color(red, green, blue, alpha);

                            if (Debug.debugging("rpfcolortable")) {
                                if (j == 0)
                                    Debug.output("RpfColortable:\n\n---Full color table---\n");
                                Debug.output("RpfColortable:red: " + red
                                        + ", green: " + green + ", blue: "
                                        + blue + ", alpha: " + alpha);
                            }
                        } /* for j */
                        break; /* out of for i */
                    } /* if foundLUT */
                } /* for i */
            } /* if reducedColorTable == COLOR_216 */

            else { /* cct needed */

                /* DKS. Read cct records */
                if (Debug.debugging("rpfdetail")) {
                    Debug.output("RpfColortable: color converter subsection loc[2]:"
                            + loc[2].componentLocation);
                }
                binFile.seek(loc[2].componentLocation);

                colorConvOffsetTableOffset = (long) binFile.readInteger();
                colorConvOffsetRecl = (int) binFile.readShort();
                colorConvRecl = (int) binFile.readShort();

                if (Debug.debugging("rpfdetail")) {
                    Debug.output("RpfColortable: colorConvOffsetTableOffset:"
                            + colorConvOffsetTableOffset);
                    Debug.output("RpfColortable: colorConvOffsetRecl:"
                            + colorConvOffsetRecl);
                    Debug.output("RpfColortable: colorConvRecl:"
                            + colorConvRecl);
                }

                ColorConversionTable[] cct = new ColorConversionTable[numColorConvOffsetRecs];

                /* Color Converter offset table */
                for (i = 0; i < numColorConvOffsetRecs; i++) { /*
                                                                 * 2 cct recs
                                                                 */

                    cct[i] = new ColorConversionTable();
                    cct[i].colorConvTableId = (int) binFile.readShort();
                    cct[i].colorConvNumRecs = (long) binFile.readInteger();
                    cct[i].colorConvTableOffset = (long) binFile.readInteger();
                    cct[i].colorConvSourceTableOffset = (long) binFile.readInteger();
                    cct[i].colorConvTargetTableOffset = (long) binFile.readInteger();

                    if (Debug.debugging("rpfdetail")) {
                        Debug.output("RpfColortable: color conversion table - "
                                + i);
                        Debug.output(cct[i].toString());
                    }

                } /* for i */

                colorOffset = new ColorOffset[numColorConvOffsetRecs];

                for (i = 0; i < numColorConvOffsetRecs; i++) { /* 2 */
                    /*
                     * Read colormap subsection for this target table: find #
                     * color/gray recs.
                     */
                    binFile.seek(loc[1].componentLocation
                            + cct[i].colorConvTargetTableOffset);

                    colorOffset[i] = new ColorOffset();

                    colorOffset[i].tableId = (int) binFile.readShort();
                    colorOffset[i].numColorRecords = (long) binFile.readInteger();

                    /* look for numColorRecords[i] == 216 or 217 */
                    ncr = colorOffset[i].numColorRecords;

                    /* numColorRecords[0] can't be 216 for a cct */
                    /* Read, use 32 or 33 clrs */
                    if ((((ncr == 32) || (ncr == 33)) && (reducedColorTable == COLORS_32))
                            || (((ncr == 16) || (ncr == 17)) && (reducedColorTable == COLORS_16))) {
                        /* Read, use 16 or 17 clrs */
                        foundLUT = true;
                    } else {
                        foundLUT = false;
                    }

                    if (Debug.debugging("rpfdetail")) {
                        Debug.output("RpfColortable: foundLUT?:" + foundLUT);
                    }

                    if (foundLUT) { /*
                                     * continue reading colormap subsection
                                     */
                        colorOffset[i].colorElementLength = binFile.read();
                        colorOffset[i].histogramRecordLength = (int) binFile.readShort();
                        colorOffset[i].colorTableOffset = (long) binFile.readInteger();
                        colorOffset[i].histogramTableOffset = (long) binFile.readInteger();

                        if (Debug.debugging("rpfdetail")) {
                            Debug.output("RpfColortable: Parse_clut: " + i);
                            Debug.output(colorOffset[i].toString());
                        }

                        // ////////////////////////////
                        /*
                         * loc[1] is colormap subsection. Seek to color/gray
                         * table.
                         */
                        binFile.seek(loc[1].componentLocation
                                + colorOffset[i].colorTableOffset);

                        /*
                         * Read the color/gray records: 32 or 33, or 16 or 17
                         * color tables
                         */
                        for (j = 0; j < ncr; j++) { /*
                                                     * 32 or 33, or 16 or 17
                                                     */
                            red = binFile.read() & 0x00ff; /*
                                                             * read byte value
                                                             */
                            green = binFile.read() & 0x00ff; /*
                                                                 * read byte
                                                                 * value
                                                                 */
                            blue = binFile.read() & 0x00ff; /*
                                                             * read byte value
                                                             */
                            alpha = binFile.read(); /* read byte value */

                            alpha = opaqueness;

                            /* DKS NEW TRANSP */
                            if (ncr == 217 && rgb[(int) (ncr - 1)] == null) { /*
                                                                                 * transp
                                                                                 * exists
                                                                                 */
                                alpha = opaqueness;
                                red = 255;
                                green = 255;
                                blue = 255;
                                rgb[(int) (ncr - 1)] = new Color(red, green, blue, alpha);
                            } /* if */

                            rgb[j] = new Color(red, green, blue, alpha);

                            if (Debug.debugging("rpfcolortable")) {
                                if (j == 0)
                                    Debug.output("RpfColortable:\n\n---CCT color table---\n");
                                Debug.output("RpfColortable: red:" + red
                                        + ", green:" + green + ", blue:" + blue
                                        + ", alpha: " + alpha);
                            }
                        } /* for j */

                        /* go to start of color converter table */
                        /* loc[2] is color converter subsection */
                        binFile.seek(loc[2].componentLocation
                                + cct[i].colorConvTableOffset);

                        if (Debug.debugging("rpfdetail")) {
                            Debug.output("RpfColortable: i:" + i
                                    + ", colorConvTableOffset[i]:"
                                    + cct[i].colorConvTableOffset);
                            Debug.output("RpfColortable: Read cct values at file location:"
                                    + binFile.getFilePointer());
                        }

                        for (j = 0; j < cct[i].colorConvNumRecs; j++) {
                            colorConvTable[j] = binFile.readInteger();
                            if (Debug.debugging("rpfcolortable"))
                                Debug.output("RpfColortable: j:" + j
                                        + ", colorConvTable[j]:"
                                        + colorConvTable[j]);
                        }

                        break; /* for i */
                    } /* if foundLUT */
                } /* for i = numColorConvOffsetRecs */
            } /* else CCT needed */

            if (reducedColorTable == COLORS_216) { /*
                                                     * 216 colors chosen
                                                     */
                if (Debug.debugging("rpfdetail"))
                    Debug.output("RpfColortable: WARNING - Full 216 colors being used\n");
                for (j = 0; j < CADRG_COLORS; j++) { /* 216 */
                    colorConvTable[j] = j;
                } /* for j */
            }

            // Since the CIB doesn't contain ccts, we need to fake
            // it...
            if (Cib && reducedColorTable != COLORS_216) {
                int divisor, midoffset;
                if (reducedColorTable == COLORS_32) {
                    divisor = 8;
                    midoffset = 4;
                } else {
                    divisor = 16;
                    midoffset = 8;
                }

                for (j = 0; j < CADRG_COLORS; j++) { /* 216 */
                    red = (int) (rgb[j].getRed() / divisor) * divisor
                            + midoffset;
                    green = (int) (rgb[j].getGreen() / divisor) * divisor
                            + midoffset;
                    blue = (int) (rgb[j].getBlue() / divisor) * divisor
                            + midoffset;
                    alpha = rgb[j].getAlpha();

                    rgb[j] = new Color(red, green, blue, alpha);

                    if (Debug.debugging("rpfcolortable")) {
                        if (j == 0)
                            Debug.output("RpfColortable:\n\n---Final color table CIB---\n");
                        Debug.output("RpfColortable: Color " + j + " red: "
                                + rgb[j].getRed() + ", green: "
                                + rgb[j].getGreen() + ", blue: "
                                + rgb[j].getBlue());
                    }
                }

            } // if Cib
            // For CADRG that has a cct or also Cib that doesn't
            /* DKS. cct added here instead of load_frame */
            else if (reducedColorTable != COLORS_216) {
                for (j = 0; j < CADRG_COLORS; j++) { /* 216 */
                    red = rgb[colorConvTable[j]].getRed();
                    green = rgb[colorConvTable[j]].getGreen();
                    blue = rgb[colorConvTable[j]].getBlue();
                    alpha = rgb[colorConvTable[j]].getAlpha();

                    rgb[j] = new Color(red, green, blue, alpha);

                    if (Debug.debugging("rpfcolortable")) {
                        if (j == 0)
                            Debug.output("RpfColortable:\n\n---Final color table---\n");

                        Debug.output("RpfColortable: Color " + j + " red: "
                                + rgb[j].getRed() + ", green: "
                                + rgb[j].getGreen() + ", blue: "
                                + rgb[j].getBlue());
                    }
                } /* for j */
            }

            if (Debug.debugging("rpfdetail")) {
                Debug.output("RpfColortable: LEAVE PARSE Colortable");
            }
        } catch (IOException ioe) {
            Debug.error("RpfTocHandler: IO ERROR parsing file!\n" + ioe);
            return null;
        } catch (FormatException fe) {
            Debug.error("RpfTocHandler: Format ERROR parsing file!\n" + fe);
            return null;
        }

        colors = rgb;
        return rgb;
    } /* parse_clut.c */

    static public class ColorOffset {
        public int tableId;
        public long numColorRecords;
        public int colorElementLength; // uchar
        public int histogramRecordLength;
        public long colorTableOffset;
        public long histogramTableOffset;

        public ColorOffset() {}

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("RpfColortable: tableId 2:CADRG; 3:CIB): ").append(tableId)
                    .append("\n");
            s.append("RpfColortable: numColorRecords: ").append(numColorRecords)
                    .append("\n");
            s.append("RpfColortable: colorElementLength: ")
                    .append(colorElementLength).append("\n");
            s.append("RpfColortable: histogramRecordLength: ")
                    .append(histogramRecordLength).append("\n");
            s.append("RpfColortable: colorTableOffset: ")
                    .append(colorTableOffset).append("\n");
            s.append("RpfColortable: histogramTableOffset: ")
                    .append(histogramTableOffset);
            return s.toString();
        }
    }

    static public class ColorConversionTable {

        public int colorConvTableId; // ushort
        public long colorConvNumRecs; // uint
        public long colorConvTableOffset; // uint
        public long colorConvSourceTableOffset; // uint
        public long colorConvTargetTableOffset; // uint

        public ColorConversionTable() {}

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("RpfColortable: colorConvTableId: ")
                    .append(colorConvTableId).append("\n");
            s.append("RpfColortable: colorConvNumRecs: ")
                    .append(colorConvNumRecs).append("\n");
            s.append("RpfColortable: colorConvTableOffset: ")
                    .append(colorConvTableOffset).append("\n");
            s.append("RpfColortable: colorConvSourceTableOffset: ")
                    .append(colorConvSourceTableOffset).append("\n");
            s.append("RpfColortable: colorConvTargetTableOffset: ")
                    .append(colorConvTargetTableOffset);
            return s.toString();
        }
    }

    public static void main(String[] args) {

        Debug.init(System.getProperties());

        if (args.length != 1) {
            Debug.output("Usage: java RpfColortable <path to RPF frame>");
            return;
        }

        File file = new File(args[0]);
        BinaryFile binFile = null;
        try {
            binFile = new BinaryBufferedFile(file);
        } catch (FileNotFoundException e) {
            Debug.error("RpfHeader: file " + args[0] + " not found");
            System.exit(1);
        } catch (IOException ioe) {
            Debug.error("RpfHeader: File IO Error while handling colortable:\n"
                    + ioe);
            System.exit(1);
        }

        RpfColortable tbl = new RpfColortable();
        RpfFileSections rfs = new RpfFileSections();
        RpfHeader head = new RpfHeader();

        head.read(binFile);
        rfs.parse(binFile);
        Color[] colors = rfs.parseColorSection(binFile, tbl);

        if (colors == null)
            Debug.output("RpfColortable: NOT read successfully!");

    }
}
