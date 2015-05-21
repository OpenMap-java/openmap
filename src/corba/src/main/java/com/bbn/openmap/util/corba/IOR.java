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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/util/corba/IOR.java,v $
// $RCSfile: IOR.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 21:04:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.corba;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

/**
 * Utility class that reads and decodes CORBA IOR files. For debugging
 * purposes.
 */
public class IOR {

    static boolean debug = false;
    static boolean verbose = false;
    byte[] hex;

    IOR(byte[] rawIOR) {
        hex = rawIOR;
    }

    /**
     * The first four bytes are 'IOR:' The rest of the bytes are an
     * encapsulation of an IOR encoded in hex. So we first unencode
     * the hex, to create a new byte array that is the encapsulated
     * IOR.
     * 
     * Encapsulation is defined in the CORBA Spec, section 12.3.3 The
     * first byte indicates byte order: TRUE for BigEndian, False for
     * LittleEndian. In practice, it seems to be encoded as a 4-byte
     * value.
     * 
     * Next comes a type_id string. Strings are encoded as a long (4
     * bytes) indicating length, followed by n bytes of ascii text.
     * The nth byte is the null byte, for the benefit of C programs.
     * 
     * Next is a sequence of profiles. Sequences are encoded as a long
     * followed by n objects. In this case, a Profile consists of a
     * ProfileID (long, 4 bytes) followed by a IIOP::ProfileBody.
     * 
     * The ProfileID (see section 10.6) is either IOP or
     * MultipleComponents. I don't know anything about
     * MultipleComponents, as they are intended to be opaque. This
     * program only parses IOP Profiles, although it successfully
     * skips over Multiple Component Profiles.
     * 
     * An IOP Profile is a ProfileBody, defined in 12.7.2 of CORBA
     * spec. A ProfileBody consists of: A version, major and minor.
     * Spec'ed as two chars, in practice two shorts. A host string A
     * port (2 bytes) An object key. This is an opaque sequence of
     * bytes for use by the CORBA implementation that produced the
     * IOR.
     *  
     */
    void parse(PrintWriter out) {

        // Make sure first four bytes are 'IOR:'
        String prefix = new String(hex, 0, 4);
        if (!prefix.equals("IOR:")) {
            System.err.println("Invalid IOR");
            System.err.println("The first four bytes should be: 'IOR:'");
            System.err.println("Found: " + prefix);
            System.exit(1);
        }

        int iorLength = (hex.length - 4) / 2;
        byte[] ior = new byte[iorLength];
        for (int hexIndex = 4, iorIndex = 0; hexIndex < hex.length; hexIndex += 2, iorIndex++) {

            try {
                ior[iorIndex] = (byte) ((hexByteToInt(hex[hexIndex]) << 4) + (hexByteToInt(hex[hexIndex + 1])));
            } catch (NumberFormatException e) {
                System.err.println("Index: " + hexIndex);
                System.err.println(e);
                return;
            }
        }

        if (debug) {
            // print out all the bytes
            for (int i = 0; i < iorLength; i++) {
                System.out.println(i + ": " + ior[i] + ", " + (char) ior[i]);
            }
        }

        DataPointer dp = new DataPointer(debug);
        int endian = getLongAt(dp, ior);
        if (endian == 0)
            out.println("Big Endian");
        else
            out.println("Little Endian");

        int type_id_length = getLongAt(dp, ior);
        if (verbose) {
            out.println("type id length = " + type_id_length);
        }
        String type_id = getStringAt(dp, ior, type_id_length);
        out.println("Type ID = \"" + type_id + "\"");
        int nProfiles = getLongAt(dp, ior);
        if (nProfiles < 0) {
            System.err.println("Found " + nProfiles + " profiles.  Aborting");
            System.exit(1);
        }
        if (verbose) {
            if (nProfiles == 0) {
                out.println("There are no profiles.");
            } else if (nProfiles == 1) {
                out.println("There is 1 profile.");
            } else {
                out.println("There are " + nProfiles + " profiles.");
            }
        }

        for (int p = 0; p < nProfiles; p++) {
            int ProfileID = getLongAt(dp, ior);
            out.println("Profile " + p + ": ");
            if (ProfileID == 0) {
                out.println("\tID: TAG_INTERNET_IOP");
                int profileDataLength = getLongAt(dp, ior);
                if (verbose) {
                    out.println("\tProfile length: " + profileDataLength);
                }
                int major = getShortAt(dp, ior);
                int minor = getShortAt(dp, ior);
                out.println("\tIIOP Version: " + major + "." + minor);
                int hostLength = getLongAt(dp, ior);
                String host = getStringAt(dp, ior, hostLength);
                out.println("\tHost: " + host);
                int port = getShortAt(dp, ior);
                out.println("\tPort: " + port);
                int objectKeyLength = getLongAt(dp, ior);
                if (verbose) {
                    out.println("\tObject Key Length: " + objectKeyLength);
                }
                String objectKey = getStringAt(dp, ior, objectKeyLength);
                out.println("\tObject Key: \"" + objectKey + "\"");
            } else if (ProfileID == 1) {
                out.println("\tID: TAG_MULTIPLE_COMPONENTS");
                int profileDataLength = getLongAt(dp, ior);
                out.println("\tProfile length: " + profileDataLength);
                dp.incPointer(profileDataLength);
            } else {
                out.println("Unknown, value is " + ProfileID);
                return;
            }
        }

        if (dp.getPointer() == iorLength) {
            out.println("IOR read successfully");
        } else if (dp.getPointer() > iorLength) {
            System.err.println("Failure! Overran buffer.");
        } else if (dp.getPointer() < iorLength) {
            System.err.println("Failure! Incomplete read.");
        } else {
            System.err.println("Failure! Unknown state.");
        }
    }

    int hexByteToInt(byte b) {
        char hex = (char) b;
        if (('0' <= hex) && (hex <= '9')) {
            return (int) (hex - '0');
        } else if (('a' <= hex) && (hex <= 'f')) {
            return (int) (10 + (hex - 'a'));
        } else if (('A' <= hex) && (hex <= 'F')) {
            return (int) (10 + (hex - 'A'));
        } else {
            throw new NumberFormatException("byte: " + b);
        }
    }

    //     public static int getIntAt (int idx, byte[] b) {
    //      // gets an int at the specified location.
    //      return 0;
    //     }

    int getInt4At(DataPointer dp, byte[] b) {
        // gets a 4 byte integer off the byte array at index dp
        dp.align(4);
        int i = dp.getPointer();
        int x = ((b[i] << 24) + (b[i + 1] << 16) + (b[i + 2] << 8) + (b[i + 3] << 0));
        dp.incPointer(4);
        return x;
    }

    int getInt2At(DataPointer dp, byte[] b) {
        // gets a 2 byte integer off the byte array at index i
        dp.align(2);
        int i = dp.getPointer();
        int x = (b[i] << 8) + (b[i + 1] & 0xff);
        dp.incPointer(2);
        return x;
    }

    int getShortAt(DataPointer dp, byte[] b) {
        return getInt2At(dp, b);
    }

    int getLongAt(DataPointer dp, byte[] b) {
        return getInt4At(dp, b);
    }

    String getStringAt(DataPointer dp, byte[] b, int length) {
        // gets a string of length 'length' off the byte array at
        // index dp
        dp.align(1);
        int end = dp.getPointer() + length - 1; // Ignore the final
                                                // null
        StringBuffer buf = new StringBuffer(length);
        for (int j = dp.getPointer(); j < end; j++) {
            buf.append((char) b[j]);
        }
        dp.incPointer(length);
        return buf.toString();
    }

    public static void printUsage() {
        System.err.println("usage: java ior [-verbose] [-debug] filename");
    }

    /*
     * bytes 0-3 = 'IOR:' bytes 4-n are hex representation of IOR.
     * 
     *  
     */
    public static void main(String args[]) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String filename = null;

        // Parse the arguments
        for (int a = 0; a < args.length; a++) {
            if (args[a].charAt(0) == '-') {
                if (args[a].equals("-verbose"))
                    verbose = true;
                else if (args[a].equals("-debug"))
                    debug = true;
                else {
                    printUsage();
                    System.exit(1);
                }
            } else if (filename == null) {
                filename = args[a];
            } else {
                printUsage();
                System.exit(1);
            }
        }

        if (verbose || debug) {
            System.out.println("-------------------------------------------");
            System.out.println("verbose = " + verbose);
            System.out.println("debug = " + debug);
            System.out.println("filename = " + filename);
            System.out.println("-------------------------------------------");
            System.out.println();
        }

        byte[] contents = null;
        File f = new File(filename);
        try {
            FileInputStream fis = new FileInputStream(f);
            int nAvail = fis.available();
            contents = new byte[nAvail];
            /*int length = */fis.read(contents);
            fis.close();

            new IOR(contents).parse(new PrintWriter(System.out, true));

        } catch (java.io.FileNotFoundException e) {
            System.err.println("File not found: " + f);
            return;
        } catch (java.io.IOException e) {
            System.err.println("Error reading file " + f);
            return;
        }
    }

    /**
     * An abstraction of a pointer into a byte array. This pointer
     * allows its clients to align the pointer on arbitrary byte
     * boundaries, and increment freely.
     * 
     * It is particularly useful in Java where you can't pass
     * arguments by reference. By passing a DataPointer, a function
     * can align and increment the pointer transparently to its
     * clients.
     */
    class DataPointer {
        int ptr;
        boolean debug;

        DataPointer() {
            ptr = 0;
            debug = false;
        }

        DataPointer(boolean dbg) {
            ptr = 0;
            debug = dbg;
        }

        void incPointer(int increment) {
            if (debug)
                System.out.print("ptr: " + ptr + "+" + increment + " = ");
            ptr += increment;
            if (debug)
                System.out.println(ptr);
        }

        int getPointer() {
            return ptr;
        }

        void align(int boundary) {
            while ((ptr % boundary) != 0) {
                if (debug)
                    System.out.println("ptr: align: " + ptr + ", " + boundary);
                ptr++;
            }
        }
    }

}