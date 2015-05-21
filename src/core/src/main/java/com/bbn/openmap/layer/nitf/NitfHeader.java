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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/nitf/NitfHeader.java,v $
// $RCSfile: NitfHeader.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:01 $
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

package com.bbn.openmap.layer.nitf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * The NitfHeader reads the header information in a NITF (National Imagery
 * Transmission Format) and makes the section location information available.
 */
public class NitfHeader {

    public final static int NITF_IMAGE_DES_SIZE = 16;
    public final static int NITF_SYMBOLS_DES_SIZE = 10;
    public final static int NITF_LABELS_DES_SIZE = 7;
    public final static int NITF_TEXT_DES_SIZE = 9;
    public final static int NITF_DATAEXT_DES_SIZE = 13;
    public final static int NITF_RESEXT_DES_SIZE = 11;
    public final static int NITF_USERDEF_SIZE = 19;

    /** File type and Version */
    public String FHDR;// [9]
    /** Compliance Level */
    public String CLEVEL;// [2]
    /** System Type */
    public String STYPE;// [3]
    /** Originating Station ID */
    public String OSTAID;// [10]
    /** File Date and Time */
    public String FDT;// [14]
    /** File Title */
    public String FTITLE;// [80]
    /** File Security Classification */
    public String FSCLAS;// [1]
    /** File Codewords */
    public String FSCODE;// [40]
    /** File Control and Handling */
    public String FSCTLH;// [40]
    /** File Releasing Instructions */
    public String FSREL;// [40]
    /** File Classification Authority */
    public String FSCAUT;// [20]
    /** File Security Control Number */
    public String FSCTLN;// [20]
    /** File Security Downgrade */
    public String FSDWNG;// [6]
    /** File Downgrading Event */
    public String FSDEVT;// [40]
    /** Message Copy Number */
    public String FSCOP;// [5]
    /** Message Number of Copies */
    public String FSCPYS;// [5]
    /** Encryption */
    public String ENCRYP;// [1]
    /** Originator's Name */
    public String ONAME;// [27]
    /** Originator's Phone Number */
    public String OPHONE;// [18]
    /** File Length */
    public String FL;// [12]
    /** NITF File Header Length */
    public String HL;// [6]

    public NitfHeaderAmounts nha;
    public NitfUserDef nud;

    public NitfHeader() {
        nha = new NitfHeaderAmounts();
        nud = new NitfUserDef();
    }

    static public class NitfHeaderAmounts {
        /** Number of Images */
        public String NUMI;// [3]
        /** Number of Symbols */
        public String NUMS;// [3]
        /** Number of Labels */
        public String NUML;// [3]
        /** Number of Text Files */
        public String NUMT;// [3]
        /** Number of Data Extensions */
        public String NUMDES;// [3]
        /** Number of Reserved Extensions */
        public String NUMRES;// [3]
    }

    static public class NitfImageDescription {
        public String LISH;// [6]
        public String LI;// [10]
    }

    static public class NitfSymbolsDescription {
        public String LSSH;// [4]
        public String LS;// [6]
    }

    static public class NitfLabelDescription {
        public String LLSH;// [4]
        public String LL;// [3]
    }

    static public class NitfTextDescription {
        public String LTSH;// [4]
        public String LT;// [5]
    }

    static public class NitfDataExtDescription {
        public String LDSH;// [4]
        public String LD;// [9]
    }

    static public class NitfResExtDescription {
        public String LRSH;// [4]
        public String LR;// [7]
    }

    static public class NitfUserDef {
        public String UDHDL;// [5]
        public String UDHOFL;// [3]
        public String RETAG;// [6]
        public String REL;// [5]
    }

    /**
     * Reads the header part of the file. Will seek automatically to the
     * beginning of the file.
     * 
     * @param binFile BinaryFile, opened on the NITF file.
     */
    public boolean read(BinaryFile binFile) {

        try {
            binFile.seek(0);
            FHDR = binFile.readFixedLengthString(9);
            if (!FHDR.startsWith("NITF"))
                return false; /* Not an NITF file */

            CLEVEL = binFile.readFixedLengthString(2);
            STYPE = binFile.readFixedLengthString(4);
            OSTAID = binFile.readFixedLengthString(10);
            FDT = binFile.readFixedLengthString(14);
            FTITLE = binFile.readFixedLengthString(80);
            FSCLAS = binFile.readFixedLengthString(1);
            FSCODE = binFile.readFixedLengthString(40);
            FSCTLH = binFile.readFixedLengthString(40);
            FSREL = binFile.readFixedLengthString(40);
            FSCAUT = binFile.readFixedLengthString(20);
            FSCTLN = binFile.readFixedLengthString(20);
            FSDWNG = binFile.readFixedLengthString(6);

            if (FSDWNG.startsWith("999998"))
                FSDEVT = binFile.readFixedLengthString(40);

            FSCOP = binFile.readFixedLengthString(5);
            FSCPYS = binFile.readFixedLengthString(5);
            ENCRYP = binFile.readFixedLengthString(1);
            ONAME = binFile.readFixedLengthString(27);
            OPHONE = binFile.readFixedLengthString(18);
            FL = binFile.readFixedLengthString(12);
            HL = binFile.readFixedLengthString(6);

            nha = readSectionInfo(binFile);

            nud.UDHDL = binFile.readFixedLengthString(5);// [5]
            nud.UDHOFL = binFile.readFixedLengthString(3);// [3]
            nud.RETAG = binFile.readFixedLengthString(6);// [6]
            nud.REL = binFile.readFixedLengthString(5);// [5]

        } catch (IOException e) {
            System.err.println("NitfHeader: File IO Error while reading header information:");
            System.err.println(e);
            return false;
        } catch (FormatException f) {
            System.err.println("NitfHeader: File IO Format error while reading header information:");
            System.err.println(f);
            return false;
        }

        return true;
    }

    protected NitfHeaderAmounts readSectionInfo(BinaryFile binFile) {

        try {
            nha.NUMI = binFile.readFixedLengthString(3);
            binFile.seek(binFile.getFilePointer()
                    + (Integer.parseInt(nha.NUMI) * NITF_IMAGE_DES_SIZE));

            nha.NUMS = binFile.readFixedLengthString(3);
            binFile.seek(binFile.getFilePointer()
                    + (Integer.parseInt(nha.NUMS) * NITF_SYMBOLS_DES_SIZE));

            nha.NUML = binFile.readFixedLengthString(3);
            binFile.seek(binFile.getFilePointer()
                    + (Integer.parseInt(nha.NUML) * NITF_LABELS_DES_SIZE));

            nha.NUMT = binFile.readFixedLengthString(3);
            binFile.seek(binFile.getFilePointer()
                    + (Integer.parseInt(nha.NUMT) * NITF_TEXT_DES_SIZE));

            nha.NUMDES = binFile.readFixedLengthString(3);
            binFile.seek(binFile.getFilePointer()
                    + (Integer.parseInt(nha.NUMDES) * NITF_DATAEXT_DES_SIZE));

            nha.NUMRES = binFile.readFixedLengthString(3);
        } catch (IOException e) {
            System.err.println("NitfHeader: File IO Error while reading header information:");
            System.err.println(e);
            return null;
        } catch (FormatException f) {
            System.err.println("NitfHeader: File IO Format error while reading header information:");
            System.err.println(f);
            return null;
        }

        return nha;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("\n\nNITF Information about ").append(FTITLE).append("\n");
        s.append("-------------------\n");
        s.append("File Type: ").append(FHDR).append("\n");
        s.append("Compliance Level: ").append(CLEVEL).append("\n");
        s.append("System Type: ").append(STYPE).append("\n");
        s.append("Originating Station: ").append(OSTAID).append("\n");
        s.append("File Date and Time: ").append(FDT).append("\n");
        s.append("Originator's Name: ").append(ONAME).append("\n");
        s.append("File Length: ").append(FL).append("\n\n");

        s.append(nha.NUMI).append(" image\n");
        s.append(nha.NUMS).append(" symbol\n");
        s.append(nha.NUML).append(" label\n");
        s.append(nha.NUMT).append(" text\n");
        s.append(nha.NUMDES).append(" dataext\n");
        s.append(nha.NUMRES).append(" resext\n");
        return s.toString();
    }

    public final static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java NitfHeader <path to NITF file>");
            return;
        }

        File file = new File(args[0]);
        BinaryFile binFile = null;
        try {
            binFile = new BinaryBufferedFile(file);
            // binFile = new BinaryFile(file);

            NitfHeader header = new NitfHeader();
            if (header.read(binFile)) {
                System.out.println(header);
            } else {
                System.out.println("NitfHeader: NOT read successfully!");
            }
        } catch (FileNotFoundException e) {
            System.err.println("NitfHeader: file " + args[0] + " not found");
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("NitfHeader: File IO Error while handling NITF header:");
            System.err.println(ioe);
        }
    }
}