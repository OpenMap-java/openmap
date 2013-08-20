package org.libtiff.jai.codec;

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

/**
 * XTIFF. eXtensible TIFF Library.
 * 
 * A class containing common image type-codes and public tags.
 * 
 */

public abstract class XTIFF {
    public static final int TIFFTAG_NEWSUBFILETYPE = 254;
    public static final int FILETYPE_REDUCEDIMAGE = 1;
    public static final int FILETYPE_PAGE = 2;
    public static final int FILETYPE_MASK = 4;
    public static final int TIFFTAG_OSUBFILETYPE = 255;
    public static final int OFILETYPE_IMAGE = 1;
    public static final int OFILETYPE_REDUCEDIMAGE = 2;
    public static final int OFILETYPE_PAGE = 3;
    public static final int TIFFTAG_IMAGE_WIDTH = 256;
    public static final int TIFFTAG_IMAGE_LENGTH = 257;
    public static final int TIFFTAG_BITS_PER_SAMPLE = 258;
    public static final int TIFFTAG_COMPRESSION = 259;
    public static final int COMPRESSION_NONE = 1;
    public static final int COMPRESSION_FAX_G3_1D = 2;
    public static final int COMPRESSION_FAX_G3_2D = 3;
    public static final int COMPRESSION_FAX_G4_2D = 4;
    public static final int COMPRESSION_LZW = 5;
    public static final int COMPRESSION_OJPEG = 6;
    public static final int COMPRESSION_JPEG = 7;
    public static final int COMPRESSION_NEXT = 32766;
    public static final int COMPRESSION_CCITTRLEW = 32771;
    public static final int COMPRESSION_PACKBITS = 32773;
    public static final int COMPRESSION_THUNDERSCAN = 32809;
    public static final int COMPRESSION_DEFLATE = 32946;
    public static final int COMPRESSION_IT8CTPAD = 32895;
    public static final int COMPRESSION_IT8LW = 32896;
    public static final int COMPRESSION_IT8MP = 32897;
    public static final int COMPRESSION_IT8BL = 32898;
    public static final int COMPRESSION_PIXARFILM = 32908;
    public static final int COMPRESSION_PIXARLOG = 32909;
    public static final int COMPRESSION_DCS = 32947;
    public static final int COMPRESSION_JBIG = 34661;
    public static final int COMPRESSION_SGILOG = 34676;
    public static final int COMPRESSION_SGILOG24 = 34677;
    public static final int TIFFTAG_PHOTOMETRIC_INTERPRETATION = 262;
    public static final int PHOTOMETRIC_WHITE_IS_ZERO = 0;
    public static final int PHOTOMETRIC_BLACK_IS_ZERO = 1;
    public static final int PHOTOMETRIC_RGB = 2;
    public static final int PHOTOMETRIC_PALETTE = 3;
    public static final int PHOTOMETRIC_TRANSPARENCY = 4;
    public static final int PHOTOMETRIC_CMYK = 5;
    public static final int PHOTOMETRIC_YCBCR = 6;
    public static final int PHOTOMETRIC_CIELAB = 8;
    public static final int PHOTOMETRIC_LOGL = 32844;
    public static final int PHOTOMETRIC_LOGLUV = 32845;
    public static final int TIFFTAG_THRESHHOLDING = 263;
    public static final int THRESHHOLD_BILEVEL = 1;
    public static final int THRESHHOLD_HALFTONE = 2;
    public static final int THRESHHOLD_ERRORDIFFUSE = 3;
    public static final int TIFFTAG_CELLWIDTH = 264;
    public static final int TIFFTAG_CELLLENGTH = 265;
    public static final int TIFFTAG_FILL_ORDER = 266;
    public static final int FILL_ORDER_MSB2LSB = 1;
    public static final int FILL_ORDER_LSB2MSB = 2;
    public static final int TIFFTAG_DOCUMENTNAME = 269;
    public static final int TIFFTAG_IMAGEDESCRIPTION = 270;
    public static final int TIFFTAG_MAKE = 271;
    public static final int TIFFTAG_MODEL = 272;
    public static final int TIFFTAG_STRIPOFFSETS = 273;
    public static final int TIFFTAG_ORIENTATION = 274;
    public static final int ORIENTATION_TOPLEFT = 1;
    public static final int ORIENTATION_TOPRIGHT = 2;
    public static final int ORIENTATION_BOTRIGHT = 3;
    public static final int ORIENTATION_BOTLEFT = 4;
    public static final int ORIENTATION_LEFTTOP = 5;
    public static final int ORIENTATION_RIGHTTOP = 6;
    public static final int ORIENTATION_RIGHTBOT = 7;
    public static final int ORIENTATION_LEFTBOT = 8;
    public static final int TIFFTAG_STRIP_OFFSETS = 273;
    public static final int TIFFTAG_SAMPLES_PER_PIXEL = 277;
    public static final int TIFFTAG_ROWS_PER_STRIP = 278;
    public static final int TIFFTAG_STRIP_BYTE_COUNTS = 279;
    public static final int TIFFTAG_MINSAMPLEVALUE = 280;
    public static final int TIFFTAG_MAXSAMPLEVALUE = 281;
    public static final int TIFFTAG_X_RESOLUTION = 282;
    public static final int TIFFTAG_Y_RESOLUTION = 283;
    public static final int TIFFTAG_PLANARCONFIG = 284;
    public static final int PLANARCONFIG_CONTIG = 1;
    public static final int PLANARCONFIG_SEPARATE = 2;
    public static final int TIFFTAG_PAGENAME = 285;
    public static final int TIFFTAG_XPOSITION = 286;
    public static final int TIFFTAG_YPOSITION = 287;
    public static final int TIFFTAG_FREEOFFSETS = 288;
    public static final int TIFFTAG_FREEBYTECOUNTS = 289;
    public static final int TIFFTAG_GRAYRESPONSEUNIT = 290;
    public static final int GRAYRESPONSEUNIT_10S = 1;
    public static final int GRAYRESPONSEUNIT_100S = 2;
    public static final int GRAYRESPONSEUNIT_1000S = 3;
    public static final int GRAYRESPONSEUNIT_10000S = 4;
    public static final int GRAYRESPONSEUNIT_100000S = 5;
    public static final int TIFFTAG_GRAYRESPONSECURVE = 291;
    public static final int TIFFTAG_T4_OPTIONS = 292;
    public static final int T4_2DENCODING = 1;
    public static final int T4_UNCOMPRESSED = 2;
    public static final int T4_FILLBITS = 4;
    public static final int TIFFTAG_T6_OPTIONS = 293;
    public static final int T6_UNCOMPRESSED = 2;
    public static final int TIFFTAG_RESOLUTION_UNIT = 296;
    public static final int RESUNIT_NONE = 1;
    public static final int RESUNIT_INCH = 2;
    public static final int RESUNIT_CENTIMETER = 3;
    public static final int TIFFTAG_PAGENUMBER = 297;
    public static final int TIFFTAG_COLORRESPONSEUNIT = 300;
    public static final int COLORRESPONSEUNIT_10S = 1;
    public static final int COLORRESPONSEUNIT_100S = 2;
    public static final int COLORRESPONSEUNIT_1000S = 3;
    public static final int COLORRESPONSEUNIT_10000S = 4;
    public static final int COLORRESPONSEUNIT_100000S = 5;
    public static final int TIFFTAG_TRANSFERFUNCTION = 301;
    public static final int TIFFTAG_SOFTWARE = 305;
    public static final int TIFFTAG_DATETIME = 306;
    public static final int TIFFTAG_ARTIST = 315;
    public static final int TIFFTAG_HOSTCOMPUTER = 316;
    public static final int TIFFTAG_PREDICTOR = 317;
    public static final int TIFFTAG_WHITEPOINT = 318;
    public static final int TIFFTAG_PRIMARYCHROMATICITIES = 319;
    public static final int TIFFTAG_COLORMAP = 320;
    public static final int TIFFTAG_HALFTONEHINTS = 321;
    public static final int TIFFTAG_TILE_WIDTH = 322;
    public static final int TIFFTAG_TILE_LENGTH = 323;
    public static final int TIFFTAG_TILE_OFFSETS = 324;
    public static final int TIFFTAG_TILE_BYTE_COUNTS = 325;
    public static final int TIFFTAG_BADFAXLINES = 326;
    public static final int TIFFTAG_CLEANFAXDATA = 327;
    public static final int CLEANFAXDATA_CLEAN = 0;
    public static final int CLEANFAXDATA_REGENERATED = 1;
    public static final int CLEANFAXDATA_UNCLEAN = 2;
    public static final int TIFFTAG_CONSECUTIVEBADFAXLINES = 328;
    public static final int TIFFTAG_SUBIFD = 330;
    public static final int TIFFTAG_INKSET = 332;
    public static final int INKSET_CMYK = 1;
    public static final int TIFFTAG_INKNAMES = 333;
    public static final int TIFFTAG_NUMBEROFINKS = 334;
    public static final int TIFFTAG_DOTRANGE = 336;
    public static final int TIFFTAG_TARGETPRINTER = 337;
    public static final int TIFFTAG_EXTRASAMPLES = 338;
    public static final int TIFFTAG_EXTRA_SAMPLES = 338;
    public static final int EXTRA_SAMPLE_UNSPECIFIED = 0;
    public static final int EXTRA_SAMPLE_ASSOCALPHA = 1;
    public static final int EXTRA_SAMPLE_UNASSALPHA = 2;
    public static final int TIFFTAG_SAMPLE_FORMAT = 339;
    public static final int SAMPLE_FORMAT_UINT = 1;
    public static final int SAMPLE_FORMAT_INT = 2;
    public static final int SAMPLE_FORMAT_IEEEFP = 3;
    public static final int SAMPLE_FORMAT_VOID = 4;
    public static final int TIFFTAG_S_MIN_SAMPLE_VALUE = 340;
    public static final int TIFFTAG_S_MAX_SAMPLE_VALUE = 341;
    public static final int TIFFTAG_COPYRIGHT = 33432;
    public static final int TIFFTAG_GEO_TIEPOINTS = 33922;
    public static final int TIFFTAG_GEO_PIXEL_SCALE = 33550;
    public static final int TIFFTAG_GEO_TRANS_MATRIX = 34264;
    public static final int TIFFTAG_GEO_KEY_DIRECTORY = 34735;
    public static final int TIFFTAG_GEO_DOUBLE_PARAMS = 34736;
    public static final int TIFFTAG_GEO_ASCII_PARAMS = 34737;

    // JAI (not TIFF) Image types
    public static final int TYPE_BILEVEL_WHITE_IS_ZERO = 0;
    public static final int TYPE_BILEVEL_BLACK_IS_ZERO = 1;
    public static final int TYPE_GREYSCALE_WHITE_IS_ZERO = 2;
    public static final int TYPE_GREYSCALE_BLACK_IS_ZERO = 3;
    public static final int TYPE_RGB = 4;
    public static final int TYPE_ARGB_PRE = 5;
    public static final int TYPE_ARGB = 6;
    public static final int TYPE_ORGB = 7;
    public static final int TYPE_RGB_EXTRA = 8;
    public static final int TYPE_PALETTE = 9;
    public static final int TYPE_TRANS = 10;
}
