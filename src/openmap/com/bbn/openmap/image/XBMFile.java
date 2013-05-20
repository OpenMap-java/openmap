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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/XBMFile.java,v $
// $RCSfile: XBMFile.java,v $
// $Revision: 1.5 $
// $Date: 2005/05/25 04:56:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * XBMFile is a class which reads in a bitmap file, and provides the
 * parameters of the file. If called from the command line, it draws
 * the bitmap to the command line using spaces and asterisks. It just
 * needs a valid path to a bitmap file, either a .xbm or .bm format,
 * or a File object for a bitmap file.
 */
public class XBMFile {
    public File f;
    public short width;
    public short height;
    public byte[] bits;
    public int paddedWidth;

    // Internal
    String fileStuffString; // string version of the file
    int pict_size;

    public XBMFile(String filename) {
        f = new File(filename);
        if (f.exists()) {
            readin();
        } else
            System.out.println("XBMFile: framename not valid.");
    }

    public XBMFile(File fileObj) {
        f = fileObj;
        if (f.exists()) {
            readin();
        } else
            System.out.println("XBMFile: file not valid.");
    }

    /**
     * This method leaves all to the caller, by creating a bitmap from
     * scratch. Any modification to the bits must take into account
     * the paddedWidth of the bitmap. The paddedWidth is only a factor
     * of the width is is not an even multiple of eight. So, if you
     * want to change the bits settings, you'll have to do the
     * counting yourself, and remember it's little endian, and done by
     * the byte.
     * 
     * @param h Height of bitmap.
     * @param w Width of bitmap.
     * @param b byte string representing the pixels.
     *  
     */
    public XBMFile(short w, short h, byte[] b) {
        width = w;
        height = h;
        paddedWidth = (int) Math.ceil((float) width / 8.0) * 8;
        int dim = paddedWidth * height;
        if (b.length != dim) {
            System.out.println("XBMFile: byte[] doesn't fit into dimensions");
            if (b.length > dim)
                bits = b;
            else {
                bits = new byte[paddedWidth * height];
                for (int i = 0; i < b.length; i++)
                    bits[i] = b[i];
            }
        } else
            bits = b;
    }

    /**
     * readin() parses the file and sets the object variables. It
     * assumes that the file f is valid/exists.
     */
    private void readin() {
        try {
            byte[] fileStuff = new byte[(int) f.length()];
            FileInputStream in = new FileInputStream(f);
            in.read(fileStuff);
            fileStuffString = new String(fileStuff);
            int startWidth = fileStuffString.indexOf("width") + 6;
            int endWidth = fileStuffString.indexOf(System.getProperty("line.separator"), startWidth);
            int startHeight = fileStuffString.indexOf("height") + 7;
            int endHeight = fileStuffString.indexOf(System.getProperty("line.separator"), startHeight);
            int startBits = fileStuffString.indexOf("0x");
            int endBits = fileStuffString.indexOf("};");

            width = Short.parseShort(fileStuffString.substring(startWidth,
                    endWidth));
            height = Short.parseShort(fileStuffString.substring(startHeight,
                    endHeight));

            paddedWidth = (int) Math.ceil((float) width / 8.0) * 8;
            int num_bytes = paddedWidth * height;
            pict_size = (int) height * (int) Math.ceil((float) width / 8.0);
            bits = new byte[num_bytes];
            setBits(fileStuffString.substring(startBits, endBits));
            in.close();
        } catch (IOException e3) {
            System.out.println("XBMFile: Error reading file.");
        }
    }

    /**
     * Setting the bits based on a string of 0x00 numbers. Assumes
     * that the length of the tmpString is sufficient as dictated by
     * the height and width settings.
     */
    public void setBits(String tmpString) {
        // ASCII version of things
        // System.out.println(tmpString);
        int num_bytes = paddedWidth * height;
        int i = 0;
        int j = 0;
        while (j < num_bytes) {
            i = (tmpString.indexOf("0x", i));
            int first = Character.digit(tmpString.charAt(i + 2), 16) * 0x10;
            int second = Character.digit(tmpString.charAt(i + 3), 16);

            bits[j] = new Integer(first + second).byteValue();
            j++;
            i += 4;
        }
    }

    /**
     * Methods that handle the printout for stand alone use.
     */
    private int widthCheck(int widthTrak) {
        if (widthTrak > paddedWidth - 2) {
            System.out.println();
            widthTrak = 0;
        } else
            widthTrak++;
        return widthTrak;
    }

    /**
     * Prints the bitmap to stdout, using * and spaces
     *  
     */
    public void printout() {

        //      System.out.println("Width = " + width + " | Height = " +
        // height
        //                         + " | Bit length = " + bits.length);
        int widthTrak = -1;
        for (int i = 0; i < pict_size; i++) {
            int val = new Byte(bits[i]).intValue();

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x01) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x02) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x04) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x08) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x10) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x20) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x40) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');

            widthTrak = widthCheck(widthTrak);
            if ((val & 0x80) != 0)
                System.out.print((char) '*');
            else
                System.out.print((char) ' ');
        }
        System.out.println();
    }

    /**
     * The main function just prints the bitmap to stdout.
     * 
     * parameters: args is the path to the file
     */
    public static void main(String args[]) {

        if (args.length < 1) {
            System.out.println("XBMFile:  Need a path/filename");
            System.exit(0);
        }

        System.out.println("XBMFile: " + args[0]);
        XBMFile xbm = new XBMFile(args[0]);
        xbm.printout();
    }
}

