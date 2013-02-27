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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfHeader.java,v $
// $RCSfile: RpfHeader.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:04 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * The meat of this code is based on source code provided by
 * The MITRE Corporation, through the browse application source
 * code.  Many thanks to Nancy Markuson who provided BBN with the
 * software, and to Theron Tock, who wrote the software, and
 * Daniel Scholten, who revised it - (c) 1994 The MITRE
 * Corporation for those parts, and used with permission.
 */

package com.bbn.openmap.layer.rpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

/**
 * Reads the part of the RpfFrame that gives a basic location map to the
 * locations of the file.
 */
public class RpfHeader {

    public static final int RPF_DATE_LOCATION = 31;
    public static final int HEADER_SECTION_LENGTH = 48;
    public static final int TOC_NITF_HEADER_LENGTH = 410;
    public static final int FRAME_SHORT_NITF_HEADER_LENGTH = 413;
    public static final int FRAME_LONG_NITF_HEADER_LENGTH = 426;

    public boolean endian = false;
    public short headerSectionLength;
    public String filename; // 12 chars
    public byte neww; // new/replacement/update indicator
    public String standardNumber; // 15 chars
    public String standardDate; // 8 chars
    public char classification; // 1 char
    public String country; // 2 chars
    public String release; // 2 chars
    public int locationSectionLocation;

    public RpfHeader() {
    }

    /**
     * Starts at the beginning of the file and handles the NITF header if it is
     * there.
     */
    public boolean read(BinaryFile binFile) {

        boolean nitf = new com.bbn.openmap.layer.nitf.NitfHeader().read(binFile);

        // If something failed, try it the old way...
        if (!nitf) {
            try {
                binFile.seek(0);
                int headerOffset = handleNITFHeader(binFile);

                if (headerOffset < 0)
                    return false;
                else
                    binFile.seek(headerOffset);

            } catch (IOException e) {
                Debug.error("RpfHeader: File IO Error while reading header information:\n" + e);
                return false;
            }
        }

        return readHeader(binFile);
    }

    /** Assumes that the binFile is set to the header location. */
    public boolean readHeader(BinaryFile binFile) {

        try {
            // Read header
            byte[] endianByte = binFile.readBytes(1, false);
            if (endianByte[0] > 0)
                endian = true;

            binFile.byteOrder(!endian); // opposite meanings

            headerSectionLength = binFile.readShort();

            byte[] filenameArray = binFile.readBytes(12, false);
            filename = new String(filenameArray);

            // Read rest of header so we can write it later
            byte[] newwByte = binFile.readBytes(1, false);
            neww = newwByte[0];

            byte[] standardNumberArray = binFile.readBytes(15, false);
            standardNumber = new String(standardNumberArray);

            byte[] standardDateArray = binFile.readBytes(8, false);
            standardDate = new String(standardDateArray);

            classification = binFile.readChar();

            byte[] countryArray = binFile.readBytes(2, false);
            country = new String(countryArray);

            byte[] releaseArray = binFile.readBytes(2, false);
            release = new String(releaseArray);

            locationSectionLocation = binFile.readInteger();

            if (Debug.debugging("rpfheader")) {
                Debug.output(this.toString());
            }

        } catch (IOException e) {
            Debug.error("RpfHeader: File IO Error while reading header information:\n" + e);
            return false;
        } catch (FormatException f) {
            Debug.error("RpfHeader: File IO Format error while reading header information:\n" + f);
            return false;
        }

        return true;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("RpfHeader: endian is ").append(endian).append("\n");
        s.append("RpfHeader: header section length = ").append(headerSectionLength).append("\n");
        s.append("RpfHeader: filename = ").append(filename).append("\n");
        s.append("RpfHeader: neww = ").append(neww).append("\n");
        s.append("RpfHeader: standard number = ").append(standardNumber).append("\n");
        s.append("RpfHeader: standard date = ").append(standardDate).append("\n");
        s.append("RpfHeader: classification = ").append(classification).append("\n");
        s.append("RpfHeader: country = ").append(country).append("\n");
        s.append("RpfHeader: release = ").append(release).append("\n");
        s.append("RpfHeader: location section location = ").append(locationSectionLocation).append("\n");
        return s.toString();
    }

    /**
     * Checks to see if the file is in NITF or not, and then puts the file
     * pointer in the right place to start reading the header for the file. If
     * the file is in NITF format, it skips the NITF header, and if it isn't, it
     * resets the pointer to the beginning.
     */
    public int handleNITFHeader(BinaryFile binFile) {
        try {

            byte[] nitfArray = binFile.readBytes(4, false);
            String nitf = new String(nitfArray);

            binFile.seek(RPF_DATE_LOCATION);
            nitfArray = binFile.readBytes(2, false);
            nitf = new String(nitfArray);

            if (nitf.equalsIgnoreCase("19") || nitf.equalsIgnoreCase("20"))
                return 0;

            binFile.seek(RPF_DATE_LOCATION + TOC_NITF_HEADER_LENGTH);
            nitfArray = binFile.readBytes(2, false);
            nitf = new String(nitfArray);

            if (nitf.equalsIgnoreCase("19") || nitf.equalsIgnoreCase("20"))
                return TOC_NITF_HEADER_LENGTH;

            binFile.seek(RPF_DATE_LOCATION + FRAME_SHORT_NITF_HEADER_LENGTH);
            nitfArray = binFile.readBytes(2, false);
            nitf = new String(nitfArray);

            if (nitf.equalsIgnoreCase("19") || nitf.equalsIgnoreCase("20"))
                return FRAME_SHORT_NITF_HEADER_LENGTH;

            binFile.seek(RPF_DATE_LOCATION + FRAME_LONG_NITF_HEADER_LENGTH);
            nitfArray = binFile.readBytes(2, false);
            nitf = new String(nitfArray);

            if (nitf.equalsIgnoreCase("19") || nitf.equalsIgnoreCase("20"))
                return FRAME_LONG_NITF_HEADER_LENGTH;

        } catch (IOException e) {
            Debug.error("RpfHeader: File IO Error while handling NITF header:\n" + e);
            return -1;
        } catch (FormatException f) {
            Debug.error("RpfHeader: File IO Format error while reading header information:\n" + f);
            return -1;
        }

        return -1;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java RpfHeader <path to A.TOC or RPF frame file>");
            return;
        }

        File file = new File(args[0]);
        BinaryFile binFile = null;
        try {
            binFile = new BinaryBufferedFile(file);
            // binFile = new BinaryFile(file);
            RpfHeader header = new RpfHeader();
            if (header.read(binFile))
                System.out.println(header);
            else {
                System.out.println("RpfHeader: NOT read successfully!");
            }
        } catch (FileNotFoundException e) {
            System.err.println("RpfHeader: file " + args[0] + " not found");
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("RpfHeader: File IO Error while handling NITF header:");
            System.err.println(ioe);
        }

    }
}