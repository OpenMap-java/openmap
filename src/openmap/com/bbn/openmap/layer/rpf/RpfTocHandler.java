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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfTocHandler.java,v $
// $RCSfile: RpfTocHandler.java,v $
// $Revision: 1.16 $
// $Date: 2006/12/13 16:45:24 $
// $Author: dietrick $
//
// **********************************************************************

/**
 *  Modifications:
 *  1. Changed getBestCoverageEntry() to consider more than one zone.
 *  2. Changed getBestCoverageEntry() to return multiple entries.
 */

/*
 * The meat of this code is based on source code provided by The MITRE
 * Corporation, through the browse application source code.  Many
 * thanks to Nancy Markuson who provided BBN with the software, and to
 * Theron Tock, who wrote the software, and Daniel Scholten, who
 * revised it - (c) 1994 The MITRE Corporation for those parts, and
 * used/distributed with permission.  The RPF TOC reading mechanism is
 * the contributed part.
 */

package com.bbn.openmap.layer.rpf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The RpfTocHandler knows how to read A.TOC files for RPF raster data. The
 * A.TOC file describes the coverage found in the tree of data that accompanies
 * it. This coverage is described as a series of rectangles describing the frame
 * of groups of coverage, with common-scale maps, types for different CADRG
 * zones. The RpfTocHandler can also provide a description of the frames and
 * subframes to use for a screen with a given projection.
 * <P>
 * 
 * The RPF specification says that the frame paths and file names, from the RPF
 * directory, should be in upper-case letters. The paths and file names are
 * stored in the A.TOC file this way. Sometimes, however, through CDROM and
 * downloading quirks, the paths and file names, as stored on the hard drive,
 * are actually transferred to lower-case letters. This RpfTocHandler will check
 * for lower case letter paths, but only for all the letters to be lower case.
 * The frame will be marked as non-existent if some of the directories or
 * filenames have be transformed to uppercase.
 */
public class RpfTocHandler {

    public final static String RPF_TOC_FILE_NAME = "A.TOC";
    public final static String LITTLE_RPF_TOC_FILE_NAME = "a.toc";
    public final static int DEFAULT_FRAME_SPACE = 300; // frame file
    // in kilobytes

    protected RpfHeader head;
    protected String aTocFilePath;
    protected boolean aTocByteOrder;
    protected BinaryFile binFile;
    protected RpfFileSections.RpfLocationRecord[] locations;
    /** The boundary rectangles in the A.TOC file. */
    protected RpfTocEntry[] entries;
    protected String dir;
    protected boolean Dchum;
    protected long estimateDiskSpace; // uint
    protected int numBoundaries;
    protected long numFrameIndexRecords; // uint # frame file index records
    protected int indexRecordLength; // ushort, frame file index record
    // length
    protected long currencyTime;
    protected boolean valid = false;
    /**
     * Set by the RpfFrameProvider, and used to track down this particular TOC
     * to get to the frames offered by it's coverages.
     */
    private int tocNumber = 0;

    /**
     * Flag to note whether absolute pathnames are used in the A.TOC. Set to
     * false, because it's not supposed to be that way, according to the
     * specification. This is reset automatically when the A.TOC file is read.
     * If the first two characters of the directory paths are ./, then it stays
     * false.
     */
    protected boolean fullPathsInATOC = false;

    protected boolean DEBUG_RPF = false;
    protected boolean DEBUG_RPFTOC = false;
    protected boolean DEBUG_RPFTOCDETAIL = false;
    protected boolean DEBUG_RPFTOCFRAMEDETAIL = false;

    // Added zone extents
    private static final int CADRG_zone_extents[] = {
        0,
        32,
        48,
        56,
        64,
        68,
        72,
        76,
        80,
        90
    };

    public RpfTocHandler() {

        DEBUG_RPF = Debug.debugging("rpf");
        DEBUG_RPFTOC = Debug.debugging("rpftoc");
        DEBUG_RPFTOCDETAIL = Debug.debugging("rpftocdetail");
        DEBUG_RPFTOCFRAMEDETAIL = Debug.debugging("rpftocframedetail");

        estimateDiskSpace = DEFAULT_FRAME_SPACE;

        if (Debug.debugging("rpftoc")) {
            Debug.error("RpfTocHandler: No TOC parent directory name in constructor");
        }
    }

    /**
     * Should be used in situations where it is certain that this is the only
     * A.TOC in town.
     */
    public RpfTocHandler(String parentDir) {
        this(parentDir, 0);
    }

    /**
     * Used when there is more than one A.TOC being used, or where there is a
     * possibility of that happening, like in the RPF layer. The TOC number
     * should be unique for a certain RpfFrameProvider.
     * 
     * @param parentDir the RPF directory
     * @param TOCNumber a unique number to identify this TOC for a
     *        RpfFrameProvider.
     */
    public RpfTocHandler(String parentDir, int TOCNumber) {
        tocNumber = TOCNumber;
        estimateDiskSpace = DEFAULT_FRAME_SPACE;

        /* DKS. Open input "A.TOC" */
        valid = loadFile(parentDir);
        if (!valid) {
            Debug.error("RpfTocHandler: Invalid TOC File in " + parentDir);
        }
    }

    /**
     * Given a parent RPF directory, find the a.toc file directly inside it, as
     * dictated by the specification. Not called anymore - the BinaryFile does
     * the searching, and can find URL and jar files.
     * 
     * @param parentDir Path to the RPF directory.
     * @return File
     */
    public File getTocFile(String parentDir) {
        /* DKS. Open input "A.TOC" */
        File file = new File(parentDir + "/" + RPF_TOC_FILE_NAME);
        if (!file.exists()) {
            file = new File(parentDir + "/" + LITTLE_RPF_TOC_FILE_NAME);
            if (!file.exists()) {
                // Debug.error("RpfTocHandler: getTocFile(): file in
                // "+
                // parentDir + " not found");
                return null;
            }
        }

        if (DEBUG_RPFTOCDETAIL) {
            Debug.output("RpfTocHandler: getTocFile(): TOC file is " + file);
        }

        return file;
    }

    /**
     * True if the A.TOC file is readable/present/good.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * A way to check if the status of the A.TOC file is different, in case
     * another one has taken its place. Handy if the A.TOC is on a CDROM drive
     * and the disk has been swapped. Not valid anymore, with the advent of the
     * new BinaryFile, where the file information may not be available.
     */
    public boolean hasChanged() {
        // File tmpFile = getTocFile(dir);
        // if (tmpFile == null) {
        // return valid;
        // }
        // if (tmpFile.lastModified() != currencyTime && valid) {
        // valid = false;
        // return true;
        // }
        return false;
    }

    /** Re-read the A.TOC file in the parent directory. */
    public boolean reload() {
        return loadFile(dir);
    }

    /** Read the file and load its parameters into this object. */
    public boolean loadFile(String parentDir) {

        boolean ret = true;

        String upperCaseVersion = parentDir + "/" + RPF_TOC_FILE_NAME;
        String lowerCaseVersion = parentDir + "/" + LITTLE_RPF_TOC_FILE_NAME;

        try {

            if (BinaryFile.exists(upperCaseVersion)) {
                binFile = new BinaryBufferedFile(upperCaseVersion);
                aTocFilePath = upperCaseVersion;
            } else if (BinaryFile.exists(lowerCaseVersion)) {
                binFile = new BinaryBufferedFile(lowerCaseVersion);
                aTocFilePath = lowerCaseVersion;
            }

            if (binFile == null)
                return false;

            if (DEBUG_RPFTOC) {
                Debug.output("RpfTocHandler: TOC file is in " + parentDir);
            }

            dir = parentDir + "/";

            // With the new BinaryFile, we can't get to this
            // info, because we aren't using File objects anymore.
            // currencyTime = file.lastModified();

            if (!parseToc(binFile)) {
                ret = false;
                Debug.error("RpfTocHandler: loadFile(): error parsing A.TOC file!!");
            }

            aTocByteOrder = binFile.byteOrder();

            binFile.close();
        } catch (IOException e) {
            ret = false;
        }
        binFile = null;
        return ret;
    }

    protected boolean parseToc(BinaryFile binFile) {

        if (DEBUG_RPFTOC) {
            Debug.output("ENTER TOC parsing...");
        }

        try {
            // binFile should be set to the beginning at this point
            binFile.seek(0);

            // Read header
            head = new RpfHeader();
            if (!head.read(binFile))
                return false;

            if (DEBUG_RPFTOC) {
                Debug.output("RpfTocHandler.parseToc: read header:\n" + head);
            }

            binFile.seek(head.locationSectionLocation);
            RpfFileSections rfs = new RpfFileSections(binFile);

            // Everything must be OK to reach here...
            // DKS. fseek to start of location section: 48
            // DFD not necessarily 48! New A.TOCs are different.
            locations = rfs.getLocations(RpfFileSections.TOC_LOCATION_KEY);

            // Read boundary rectangles
            // Number of Boundary records
            // DKS: now phys_index, not index
            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): fseek to Boundary section subheader: " + locations[0].componentLocation);
            }

            binFile.seek(locations[0].componentLocation);

            // NEW
            long boundRectTableOffset = (long) binFile.readInteger();

            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): BoundRectTableOffset: " + boundRectTableOffset);
            }

            int n = (int) binFile.readShort();
            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): # Boundary rect. recs: " + n);
            }

            numBoundaries = n;

            // DKS new
            // Boundary record length
            int boundaryRecordLength = (int) binFile.readShort();

            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): should be 132: " + boundaryRecordLength);
            }

            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): fseek to Boundary Rectangle Table: " + locations[1].componentLocation);
            }
            binFile.seek(locations[1].componentLocation);

            entries = new RpfTocEntry[numBoundaries];

            // Read Boundary rectangle records
            for (int i = 0; i < n; i++) {
                if (DEBUG_RPFTOCDETAIL) {
                    Debug.output("RpfTocHandler: parseToc(): read boundary rec#: " + i);
                }

                // All this stuff moved to RpfTocEntry.java - DFD
                // 8/18/99
                entries[i] = new RpfTocEntry(binFile, tocNumber, i);

                if (DEBUG_RPFTOCDETAIL) {
                    Debug.output("RpfTocHandler: parseToc(): entry " + i + " has scale " + entries[i].scale + ", type "
                            + (entries[i].Cib ? "CIB" : "CADRG") + " in zone " + entries[i].zone);
                    if (entries[i].Cib)
                        Debug.output("RpfTocHandler: parseToc(): entry noted as a Cib entry.");
                }
            }

            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): Read frame file index section subheader at loc: "
                        + locations[2].componentLocation);
            }

            // Read # of frame file index records
            // Skip 1 byte security classification
            // locations[2] is loc of frame file index section
            // subheader
            binFile.seek(locations[2].componentLocation + 1);

            // NEW
            long frameIndexTableOffset = (long) binFile.readInteger();
            numFrameIndexRecords = (long) binFile.readInteger();
            int numPathnameRecords = (int) binFile.readShort();
            // indexRecordLength should now be 33, not 35
            indexRecordLength = (int) binFile.readShort();

            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): frameIndexTableOffset: " + frameIndexTableOffset);
                Debug.output("RpfTocHandler: parseToc(): # Frame file index recs: " + numFrameIndexRecords);
                Debug.output("RpfTocHandler: parseToc(): # pathname records: " + numPathnameRecords);
                Debug.output("RpfTocHandler: parseToc(): Index rec len(33): " + indexRecordLength);
            }

            // In previous version of the RpfTocHandler, we went ahead and read
            // in all of the RPF frame file paths. For very large collections of
            // data, expecially with large scale charts and imagery, this turns
            // out to use a huge amount of data. The code has been reorganized
            // to skip this part until it's been determined that those files are
            // actually going to be used by the layer.

            // readFrameInformation(binFile);

            // One thing that we still need to do at this point, is query some
            // of the frame file paths to find out what chart type is being held
            // for each RpfTocEntry, so during the coverage determination we can
            // decide whether to use an RpfTocEntry or not.
            figureOutChartSeriesForEntries(binFile);

        } catch (IOException ioe) {
            Debug.error("RpfTocHandler: IO ERROR parsing file!\n\t" + ioe);
            return false;
        } catch (FormatException fe) {
            Debug.error("RpfTocHandler: Format ERROR parsing file!\n\t" + fe);
            return false;
        }

        if (DEBUG_RPFTOC) {
            Debug.output("LEAVE TOC parsing...");
        }
        return true;
    }

    /**
     * Method that looks at one frame file for each RpfTocEntry, in order to
     * check the suffix and load the chart series information into the
     * RpfTocEntry. This is needed for when the RpfTocHandler is asked for
     * matching RpfTocEntries for a given scale and location when the chart type
     * has been limited to a certain chart code.
     * 
     * @param binFile
     * @throws IOException
     * @throws FormatException
     */
    protected void figureOutChartSeriesForEntries(BinaryFile binFile)
            throws IOException, FormatException {

        RpfTocEntry[] entriesAlreadyChecked = new RpfTocEntry[entries.length];
        System.arraycopy(entries, 0, entriesAlreadyChecked, 0, entries.length);

        // We just need the name of one file, just to see what the series code
        // is.

        for (int i = 0; i < numFrameIndexRecords; i++) {
            // Read frame file index records
            if (DEBUG_RPFTOCFRAMEDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): Read frame file index rec #: " + i);
            }

            // Index_subhdr_len (9) instead of table_offset (11)
            // indexRecordLength (33) instead of 35
            // componentLocation, not index
            // locations[3] is frame file index table subsection
            binFile.seek(locations[3].componentLocation + indexRecordLength * i);

            int boundaryId = (int) binFile.readShort();

            if (DEBUG_RPFTOCFRAMEDETAIL) {
                Debug.output("boundary id for frame: " + i + " is " + boundaryId);
            }

            if (boundaryId > numBoundaries - 1) {
                throw new FormatException("Bad boundary id in FF index record " + i);
            }

            RpfTocEntry entry = entriesAlreadyChecked[boundaryId];

            if (entry == null) {
                continue; // already checked.
            } else {
                entriesAlreadyChecked[boundaryId] = null;
            }

            /* int frameRow = (int) */binFile.readShort();
            /* int frameCol = (int) */binFile.readShort();
            /* long pathOffset = (long) */binFile.readInteger();
            String filename = binFile.readFixedLengthString(12);

            // Figure out the chart series ID
            int dot = filename.lastIndexOf('.');
            // Interned so we can look it up in the catalog
            // later...
            entry.setInfo(filename.substring(dot + 1, dot + 3).intern());
        } /* for i = numFrameIndexRecords */
    }

    /**
     * Should be called by the RpfFrameCacheHandler before any frame files are
     * loaded from a RpfTocEntry. The RpfFrameCacheHandler should ask the
     * RpfTocEntry if the frames have been loaded, and call this if they have
     * not.
     */
    protected void loadFrameInformation(RpfTocEntry rpfTocEntry) {
        try {
            if (binFile == null && aTocFilePath != null) {
                binFile = new BinaryBufferedFile(aTocFilePath);
                binFile.byteOrder(aTocByteOrder);
                readFrameInformation(binFile, rpfTocEntry);
                binFile.close();
                binFile = null;
            }
        } catch (IOException ioe) {
            Debug.error("RpfTocHandler: IO ERROR parsing file for frame information!\n\t" + ioe);
        } catch (FormatException fe) {
            Debug.error("RpfTocHandler: Format ERROR parsing file for frame information!\n\t" + fe);
        }
    }

    /**
     * Reads the BinaryFile to retrieve the Frame file information for the
     * entry.
     * 
     * @param binFile a valid, open BinaryFile.
     * @param entry the RpfTocEntry to fill.
     * @throws IOException
     * @throws FormatException
     */
    protected void readFrameInformation(BinaryFile binFile, RpfTocEntry entry)
            throws IOException, FormatException {
        int boundaryId, frameRow, frameCol; // ushort
        int currentPosition;
        int pathLength;
        long pathOffset; // uint, offset of frame file pathname
        RpfFrameEntry frame;

        int currentBoundaryIdForEntry = entry.coverage.entryNumber;

        // Read frame file index records
        for (int i = 0; i < numFrameIndexRecords; i++) {
            if (DEBUG_RPFTOCFRAMEDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): Read frame file index rec #: " + i);
            }

            // Index_subhdr_len (9) instead of table_offset (11)
            // indexRecordLength (33) instead of 35
            // componentLocation, not index
            // locations[3] is frame file index table subsection
            binFile.seek(locations[3].componentLocation + indexRecordLength * i);

            boundaryId = (int) binFile.readShort();

            if (boundaryId != currentBoundaryIdForEntry) {
                // Only load the frame names of the entry we are using...
                continue;
            }

            if (DEBUG_RPFTOCFRAMEDETAIL) {
                Debug.output("boundary id for frame: " + i + " is " + boundaryId);
            }

            // DKS NEW: changed from 1 to 0 to agree w/ spec. -1
            // added also.
            // if (boundaryId < 0 || boundaryId > numBoundaries -
            // 1 )
            if (boundaryId > numBoundaries - 1) {
                throw new FormatException("Bad boundary id in FF index record " + i);
            }

            frameRow = (int) binFile.readShort();
            frameCol = (int) binFile.readShort();

            // DKS. switched from horizFrames to vertFrames
            // DKS NEW: CHANGED FROM 1 to 0 to agree w/spec. ALSO
            // COL below
            // if (frameRow < 1 || frameRow > entry->vertFrames)
            if (frameRow > entry.vertFrames - 1) {
                throw new FormatException("Bad row number: " + frameRow + ", in FF index record " + i
                        + ", Min row num=0;  Max. row num:" + (entry.horizFrames - 1));
            }

            // DKS. switched from vertFrames to horizFrames
            if (frameCol > entry.horizFrames - 1) {
                throw new FormatException(" Bad col number in FF index record " + i);
            }

            // DKS NEW: -1 removed on frameRow, col
            // JRB
            // frame = &entry->frames[frameRow][frameCol];

            // [(entry->vertFrames - 1L)-frameRow] flips the array
            // over, so that the frames can be referenced
            // correctly from the top left, instead of the
            // specification notation of bottom left.

            frame = entry.getFrame((entry.vertFrames - 1) - frameRow, frameCol);

            if (frame.exists && DEBUG_RPFTOCDETAIL) {
                Debug.output("FF " + i + " is a duplicate");
            }

            // DKS: phys_loc deleted

            // pathname offset
            pathOffset = (long) binFile.readInteger();

            if (pathOffset < 0) {
                continue;
            }

            // Save file position for later
            currentPosition = (int) binFile.getFilePointer();

            // Go to start of pathname record
            // DKS. New pathOffset offset from start of frame file
            // index section of TOC??

            // DKS. Add pathoffset wrt frame file index table
            // subsection (loc[3])
            binFile.seek(locations[3].componentLocation + pathOffset);

            pathLength = (int) binFile.readShort();
            if (DEBUG_RPFTOCFRAMEDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): pathLength:" + pathLength);
            }

            // 1st part of directory name is passed as arg:
            // e.g. "../RPF2/"
            String rpfdir = dir;
            StringBuffer sBuf = new StringBuffer(pathLength);

            // read rest of directory name from toc
            // DKS: skip 1st 2 chars: "./":
            String pathTest = binFile.readFixedLengthString(2);
            if (pathTest.equals("./")) {
                fullPathsInATOC = false;
            } else {
                fullPathsInATOC = true;
            }

            if (!fullPathsInATOC) {
                // DKS: Make up for skipped 2 chars
                sBuf.append(binFile.readFixedLengthString(pathLength - 2));
            } else {
                sBuf.append(pathTest);
                sBuf.append(binFile.readFixedLengthString(pathLength - 2));
            }

            // Add the trim because it looks like NIMA doesn't
            // always get the pathLength correct...
            String directory = sBuf.toString().trim();
            if (DEBUG_RPFTOCFRAMEDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): frame directory: " + directory);
            }

            /* Go back to get filename tail */
            binFile.seek(currentPosition);

            String filename = binFile.readFixedLengthString(12);
            if (DEBUG_RPFTOCFRAMEDETAIL) {
                Debug.output("RpfTocHandler: parseToc(): frame filename: " + filename);
            }

            // Figure out the chart series ID
            int dot = filename.lastIndexOf('.');
            // Interned so we can look it up in the catalog
            // later...
            entry.setInfo(filename.substring(dot + 1, dot + 3).intern());

            // We duplicate this below!!!
            // frame.framePath = new String(frame.rpfdir +
            // frame.directory +
            // "/" + frame.filename);

            // DKS new DCHUM. Fill in last digit v of vv version
            // #. fffffvvp.JNz or ffffffvp.IMz for CIB boundaryId
            // will equal frame file number: 1 boundary rect. per
            // frame.

            // if (Dchum)
            // entries[boundaryId].version =
            // frame.filename.charAt(6);

            String tempPath;

            if (!fullPathsInATOC) {
                tempPath = rpfdir + directory + filename;
                frame.rpfdirIndex = (short) (rpfdir.length() - 3);
                frame.filenameIndex = (short) (rpfdir.length() + directory.length());
            } else {
                tempPath = directory + filename;
                frame.filenameIndex = (short) directory.length();
            }

            frame.framePath = tempPath;
            frame.exists = true;

            // You don't want to check for the existance of frames here, do
            // it at load time, and then mark the entry if the load fails.
            // If you do the check here, you waste a lot of I/O time and
            // effort. Assume it's there, the RPFFrame has been modified
            // to try lower case names if needed.

            if (frame.framePath == null) {
                Debug.output("RpfTocHandler: Frame " + tempPath
                        + " doesn't exist.  Please rebuild A.TOC file using MakeToc, or check read permissions for the file.");
            }
        } /* for i = numFrameIndexRecords */

    }

    /**
     * Util-like function that translates a long to the string representation
     * found in the A>TOC file.
     */
    public static String translateScaleToSeries(long scale) {
        if (scale == 0)
            return "Various    ";
        else if (scale == 50000L)
            return "1:50K      ";
        else if (scale == 100000L)
            return "1:100K     ";
        else if (scale == 200000L)
            return "1:200K     ";
        else if (scale == 250000L)
            return "1:250K     ";
        else if (scale == 500000L)
            return "1:500K     ";
        else if (scale == 1000000L)
            return "1:1M       ";
        else if (scale == 2000000L)
            return "1:2M       ";
        else if (scale == 5000000L)
            return "1:5M       ";
        else if (scale == 66666L)
            return "10M         ";
        else if (scale == 33333L)
            return "5M          ";
        else
            return (String) null;
    }

    /**
     * Given the scale string found in the A.TOC file, decode it into a 'long'
     * scale.
     */
    public static long textScaleToLong(String textScale) {

        long resolution = 1l;
        long realValue;
        int expLetter; // location of m, M, K
        int expLetterSmall;

        // Make sure there are no commas, commas seem to kill Long parsing.
        int commaIndex = textScale.indexOf(',');
        while (commaIndex != -1) {
            StringBuffer buf = new StringBuffer(textScale.substring(0, commaIndex));
            buf.append(textScale.substring(commaIndex + 1));
            textScale = buf.toString();
            commaIndex = textScale.indexOf(',');
        }

        int colon = textScale.indexOf(":");

        try {
            if (colon == -1) {
                // dealing with an imagery scale
                expLetter = textScale.indexOf("m");
                if (expLetter == -1) {
                    expLetter = textScale.indexOf("M");
                }

                if (expLetter != -1) {
                    resolution = Long.parseLong(textScale.substring(0, expLetter));
                    return (long) (resolution / .000150);
                }

                // If we get here, we're dealing with a chart scale that doesn't
                // have a 1: at the front of it, so continue on...
            }

            // dealing with a map scale
            String expValue = "";

            expLetter = textScale.lastIndexOf('K');
            expLetterSmall = textScale.lastIndexOf('k');

            if (expLetter == -1 && expLetterSmall == -1) {
                expLetter = textScale.lastIndexOf('M');
                expLetterSmall = textScale.lastIndexOf('m');

                if (expLetter != -1 || expLetterSmall != -1) {
                    expValue = "000000";
                }
            } else {
                expValue = "000";
            }

            StringBuffer buf;
            if (expValue.length() > 0) {
                // make sure we have the right index variable
                if (expLetter == -1) {
                    expLetter = expLetterSmall;
                }
                // If there isn't a colon, this should be OK
                buf = new StringBuffer(textScale.substring(colon + 1, expLetter));
                buf.append(expValue);
            } else {
                buf = new StringBuffer(textScale.substring(colon + 1));
            }

            String longString = buf.toString().trim();
            realValue = Long.parseLong(longString);

        } catch (NumberFormatException nfe) {
            if (Debug.debugging("rpftoc")) {
                Debug.output("textScaleToLong: Number Format Exception!!!!" + textScale);
            }
            return (long) RpfConstants.UK.scale;
        } catch (StringIndexOutOfBoundsException sioobe) {
            if (Debug.debugging("rpftoc")) {
                Debug.output("textScaleToLong: String index out of bounds:\n" + sioobe.getMessage());
            }
            return (long) RpfConstants.UK.scale;
        }

        if (colon != -1) {
            resolution = Long.parseLong(textScale.substring(0, colon));
        }

        long ret = (realValue / resolution);

        if (Debug.debugging("rpftoc")) {
            Debug.output("RpfTocHandler: textScaleToLong converted " + textScale + " to " + ret);
        }

        return ret;

    }

    protected int getASCIIZone(float ullat, int zone) {
        int z = zone;
        // Now convert it to ASCII to compare
        if (ullat > 0)
            z += 48; // for ASCII compare next
        else {
            z += 64;
            if (z == 73)
                z++; // Can't be equal to I -> J
        }
        return z;
    }

    /**
     * Given a coordinate box and a scale, return the entries that have coverage
     * over the given area. The chart types returned are dictated by the
     * chartSeriesCode passed in, which must be an entry from an
     * RpfProductInfo.seriesCode.
     * 
     * @param ullat upper left latitude, in decimal degrees
     * @param ullon upper left longitude, in decimal degrees
     * @param lrlat lower right latitude, in decimal degrees
     * @param lrlon lower right longitude, in decimal degrees
     * @param proj CADRG projection describing map.
     * @param chartSeriesCode chart selection. If null, all coverage boxes
     *        fitting on the screen will be returned.
     * @param coverages a list of potential coverages
     */
    public void getCatalogCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection proj, String chartSeriesCode,
                                   Vector<RpfCoverageBox> coverages) {
        if (!valid)
            return;

        String chartSeries;

        for (int i = 0; i < numBoundaries; i++) {

            // Try to get the boundary rectangle with the most
            // coverage, so reset the entry for this particular query.
            entries[i].coverage.reset();

            if (chartSeriesCode == null) {
                chartSeries = RpfViewAttributes.ANY;
            } else {
                chartSeries = chartSeriesCode;
            }

            if (chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY) || chartSeries.equalsIgnoreCase(entries[i].info.seriesCode)) {

                if (entries[i].coverage.setPercentCoverage(ullat, ullon, lrlat, lrlon) > 0f) {
                    coverages.addElement(entries[i].coverage);
                }
            }
        }
    }

    /**
     * Given a coordinate box and a scale, find the entry in the table of
     * contents file with the right data. Zone is always of the northern
     * hemisphere, and is transformed to southern inside if needed. The box will
     * get filled in with the correct information. The subframe description will
     * have scaling information for the subframes to be scaled to match the
     * scale. If proj is null, only exact matches will be found
     * 
     * NOTE: method getZone() of the CADRG projection is only relevant
     * (according to OpenMap documentation) when you're viewing a map type (ONC,
     * etc) at its proper scale (i.e. 1:1mil for ONC). There was a method in
     * RpfTocHandler that only checked a TOC for coverage if the TOC zone
     * matched the zone of the projection. This caused gaps of coverage when
     * viewing the maps at large scales that were different from their proper
     * scale (e.g. viewing JNC at 1:10mil). Modified this method so that it
     * obtains all the possible zones the current map projection could be in,
     * and compares the TOC zones to that set.
     * 
     * Note that this now returns a list of coverage entries instead of just
     * one.
     * 
     * @param ullat upper left latitude, in decimal degrees
     * @param ullon upper left longitude, in decimal degrees
     * @param lrlat lower right latitude, in decimal degrees
     * @param lrlon lower right longitude, in decimal degrees
     * @param proj CADRG projection describing map.
     * @param viewAtts view attributes determine chart selection.
     * @return a Vector of applicable RpfCoverageBoxes.
     */
    public List<RpfTocEntry> getBestCoverageEntry(float ullat, float ullon, float lrlat, float lrlon, Projection proj,
                                                  RpfViewAttributes viewAtts) {
        if (!valid)
            return null;

        List<RpfTocEntry> coverageEntries = new Vector<RpfTocEntry>();
        double scaleFactor = 0;
        double lowerScaleFactorLimit = 1.0;
        double upperScaleFactorLimit = 1.0;

        // Good for a preliminary check. It has to start at least as
        // 4 to have one corner matching.
        int prevBoundaryHits = 0;

        if (viewAtts != null) {
            lowerScaleFactorLimit = (double) (1.0 / viewAtts.imageScaleFactor);
            upperScaleFactorLimit = (double) viewAtts.imageScaleFactor;
        }

        int nscale = 0;
        int scale = (int) proj.getScale();

        RpfTocEntry bestEntry = null;

        if (DEBUG_RPFTOCDETAIL) {
            Debug.output("getBestCoverageEntry(): Checking for coverage");
            Debug.output("  nw_lat: " + ullat);
            Debug.output("  se_lat: " + lrlat);
            Debug.output("  nw_lon: " + ullon);
            Debug.output("  se_lon: " + lrlon);
        }

        CADRG cadrg = CADRG.convertProjection(proj);

        int zone = getASCIIZone(ullat, cadrg.getZone());
        char okZones[] = getOkZones(ullat, lrlat, (char) zone);

        for (RpfTocEntry currentEntry : entries) {

            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("********************");
                Debug.output("  tochandler: Boundary #" + currentEntry.coverage.entryNumber);
                Debug.output(currentEntry.toString());
            }

            // Try to get the boundary rectangle with the most
            // coverage, so reset the entry for this particular query.
            currentEntry.coverage.reset();

            // Find the scale of the boundary rectangle
            if (currentEntry.info == null || currentEntry.info.scale == RpfConstants.Various) {

                nscale = (int) textScaleToLong(currentEntry.scale);
                currentEntry.info = new RpfProductInfo();

                // Reset the RpfProductInfo to the listed parameters
                // in the A.TOC file.
                currentEntry.info.scale = (float) nscale;
                currentEntry.info.scaleString = currentEntry.scale;
                currentEntry.coverage.scale = (float) nscale;

            } else {
                currentEntry.coverage.scale = currentEntry.info.scale;
                nscale = (int) currentEntry.info.scale;
            }

            if (DEBUG_RPFTOCDETAIL) {
                Debug.output("getBestCoverageEntry(): Query scale = " + scale + " vs. brect scale = " + nscale);
            }

            // if you want an exact match for scale...
            if (viewAtts != null && !viewAtts.scaleImages) {
                if (scale == nscale) {
                    scaleFactor = 1.0;
                } else
                    scaleFactor = lowerScaleFactorLimit - 1.0;
            } else {
                scaleFactor = (double) nscale / (double) scale;
            }

            String chartSeries;
            if (viewAtts == null) {
                chartSeries = RpfViewAttributes.ANY;
            } else {
                chartSeries = viewAtts.chartSeries;
            }

            if (scaleFactor >= lowerScaleFactorLimit
                    && scaleFactor <= upperScaleFactorLimit
                    && (chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY) || chartSeries.equalsIgnoreCase(currentEntry.info.seriesCode))) {

                if (isOkZone(currentEntry.zone, okZones)) {
                    // sets currentEntry.coverage.boundaryHits
                    int hits = currentEntry.coverage.setBoundaryHits(ullat, ullon, lrlat, lrlon);

                    if (DEBUG_RPFTOCDETAIL) {
                        Debug.output("getBestCoverageEntry(): Boundary Hits = " + hits);
                    }

                    if (bestEntry != null) {

                        boolean betterScale = false;

                        float newScaleDiff = RpfFrameCacheHandler.scaleDifference(proj, currentEntry.coverage);
                        float bestScaleDiff = RpfFrameCacheHandler.scaleDifference(proj, bestEntry.coverage);

                        if (newScaleDiff <= bestScaleDiff) {
                            betterScale = true;
                        }

                        if (betterScale
                                && (currentEntry.coverage.setPercentCoverage(ullat, ullon, lrlat, lrlon) >= bestEntry.coverage.getPercentCoverage())
                                && (hits >= prevBoundaryHits || hits >= 6)) {

                            // Add to list if has any hits and is
                            // the best possible scale. If new scale
                            // difference
                            // is strictly better, remove other
                            // entries
                            if (newScaleDiff < bestScaleDiff) {
                                coverageEntries.clear();
                            }
                            coverageEntries.add(currentEntry);

                            bestEntry = currentEntry;
                            prevBoundaryHits = hits;

                            if (DEBUG_RPFTOC) {
                                Debug.output("getBestCoverageEntry(): Found a match in a BR with coverage of "
                                        + currentEntry.coverage.getPercentCoverage() + "%.");
                            }
                        } else if (betterScale && currentEntry.coverage.getPercentCoverage() > 0f) {

                            if (newScaleDiff < bestScaleDiff) {
                                coverageEntries.clear();
                            }
                            coverageEntries.add(currentEntry);
                            bestEntry = currentEntry;
                            prevBoundaryHits = hits;
                        }

                    } else if (hits > prevBoundaryHits
                            && (currentEntry.coverage.setPercentCoverage(ullat, ullon, lrlat, lrlon) > 0f)) {
                        bestEntry = currentEntry;
                        prevBoundaryHits = hits;

                        // Add to list of coverageEntries
                        coverageEntries.add(currentEntry);

                        if (DEBUG_RPFTOC) {
                            Debug.output("getBestCoverageEntry(): Found a match in a BR with coverage of "
                                    + currentEntry.coverage.getPercentCoverage() + "%.");

                        }
                    }
                }
            }
        }

        if (DEBUG_RPFTOC) {
            if (bestEntry != null) {
                Debug.output("getBestCoverageEntry(): found the best");
                Debug.output("################");
                Debug.output(bestEntry.toString());
                Debug.output("Returning the following coverage boxes: ");

                for (int i = 0; i < coverageEntries.size(); i++) {
                    Debug.output(coverageEntries.get(i).toString());
                }
            } else {
                Debug.output("getBestCoverageEntry(): no box found");
            }
        }

        return coverageEntries;
    }

    public static char[] getOkZones(float ullat, float lrlat, char zone) {
        // allow a maximum of 3 additional zones in either direction
        char[] okZones = new char[7];
        // add zone from projection
        okZones[0] = zone;
        // check above
        char currentZone = zone;
        char backupZone;
        int i = 0;
        for (; i < 3; i++) {
            if (isAboveZone(ullat, currentZone)) {
                backupZone = getHigherZone(currentZone);
                okZones[i + 1] = backupZone;
                currentZone = backupZone;
            } else
                break;
        }
        // check below
        int k = i;
        currentZone = zone;
        for (; k < i + 3; k++) {
            if (isBelowZone(ullat, currentZone)) {
                backupZone = getLowerZone(currentZone);
                okZones[k + 1] = backupZone;
                currentZone = backupZone;
            } else
                break;
        }
        int size = 0;
        for (int j = 0; j < okZones.length; j++) {
            if (okZones[j] != 0) {
                size++;
            }
        }
        char[] returnZones = new char[size];
        System.arraycopy(okZones, 0, returnZones, 0, size);
        return returnZones;
    }

    public static boolean isOkZone(char zone, char[] okZones) {
        boolean ok = false;
        for (int i = 0; i < okZones.length; i++) {
            if (zone == okZones[i]) {
                ok = true;
            }
        }
        return ok;
    }

    protected static boolean isBelowZone(float lowerLat, char zone) {
        float zoneLowerLat = getLowerZoneExtent(zone);
        if (lowerLat < zoneLowerLat) {
            return true;
        } else
            return false;
    }

    protected static boolean isAboveZone(float upperLat, char zone) {
        float zoneUpperLat = getUpperZoneExtent(zone);
        if (upperLat > zoneUpperLat) {
            return true;
        } else
            return false;
    }

    public static float getUpperZoneExtent(char zone) {

        if (zone >= '0' && zone <= '9') {
            int i = zone - 49;
            return CADRG_zone_extents[i + 1];
        } else {
            int i = zone - 65;
            if (i == 9)
                i--; // Special care for j
            return -1 * CADRG_zone_extents[i];
        }
    }

    public static float getLowerZoneExtent(char zone) {
        if (zone >= '0' && zone <= '9') {
            int i = zone - 49;
            return CADRG_zone_extents[i];
        } else {
            int i = zone - 65;
            if (i == 9)
                i--; // Special care for J
            return -1 * CADRG_zone_extents[i + 1];
        }
    }

    public static char getLowerZone(char zone) {
        // if zone = 'J' do nothing
        if (zone >= '2' && zone <= '9') {
            zone--;
        } else if (zone == '1') {
            zone = 'A';
        } else if (zone >= 'A' && zone < 'H') {
            zone++;
        } else if (zone == 'H') {
            zone = 'J';
        }
        return zone;
    }

    public static char getHigherZone(char zone) {
        // if zone = '9' do nothing
        if (zone >= '1' && zone < '9') {
            zone++;
        } else if (zone >= 'B' && zone < 'J' || zone >= 'b' && zone < 'j') {
            zone--;
        } else if (zone == 'J' || zone == 'j') {
            zone -= 2;
        } else if (zone == 'A' || zone == 'a') {
            zone = '1';
        }
        return zone;
    }

    /** Return the list of grouped frames. */
    public RpfTocEntry[] getEntries() {
        return entries;
    }

    public String getATocFilePath() {
        return aTocFilePath;
    }

    public void setATocFilePath(String tocFilePath) {
        aTocFilePath = tocFilePath;
    }

    public boolean isFullPathsInATOC() {
        return fullPathsInATOC;
    }

    public void setFullPathsInATOC(boolean fullPathsInATOC) {
        this.fullPathsInATOC = fullPathsInATOC;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            Debug.output("Usage: java RpfTocHandler <path to RPF directory>");
            return;
        }

        Debug.init(System.getProperties());

        RpfTocHandler toc = new RpfTocHandler();
        if (!toc.loadFile(args[0]))
            Debug.output("RpfTocHandler: NOT read successfully!");
        else {
            RpfTocEntry[] e = toc.getEntries();
            Debug.output("For A.TOC: " + args[0]);
            for (int i = 0; i < e.length; i++)
                Debug.output(e[i].toString());
        }

        System.exit(0);
    }

}
