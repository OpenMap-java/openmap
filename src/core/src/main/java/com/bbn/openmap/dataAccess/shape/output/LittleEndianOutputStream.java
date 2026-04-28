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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/output/LittleEndianOutputStream.java,v $
// $RCSfile: LittleEndianOutputStream.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape.output;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

/**
 * Provides methods for writing data to an output stream in
 * LittleEndian and BigEndian. Adapted from the book, Java IO,
 * Elliotte Rusty Harold, Ch. 7.
 * 
 * @author Doug Van Auken
 */
public class LittleEndianOutputStream extends DataOutputStream {

    public void writeString(String string, int length) throws IOException {
        if (string.length() < length) {
            String newstring = zeroFill(string, length);
            byte[] bytes = newstring.getBytes();
            //         if(length==11){
            //         System.out.println("bytes.length=" + bytes.length);
            //         }
            out.write(bytes);
        } else {
            String newstring = string.substring(0, length);
            byte[] bytes = newstring.getBytes();
            //        if(length==11){
            //          System.out.println("bytes.length=" + bytes.length);
            //        }
            out.write(bytes);
        }
    }

    private String zeroFill(String string, int length) {
        char[] oldchars = string.toCharArray();
        char[] newchars = new char[length];
        for (int i = 0; i <= newchars.length - 1; i++) {
            if (i <= oldchars.length - 1) {
                newchars[i] = oldchars[i];
            } else {
                newchars[i] = 0;
            }
        }
        return new String(newchars);
    }

    /**
     * Constructor out The output stream to chain this one to.
     */
    public LittleEndianOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Writes a number of type short in little endian
     * 
     * @param s A number of type short
     */
    public void writeLEShort(short s) throws IOException {
        out.write(s & 0xFF);
        out.write((s >>> 8) & 0xFF);
        written += 2;
    }

    /**
     * Writes a number of type char in little endian param c An
     * integer that is upcast from a Char data type.
     */
    public void writeLEChar(int c) throws IOException {
        out.write(c & 0xFF);
        out.write((c >>> 8) & 0xFF);
        written += 2;
    }

    /**
     * Writes a number of type int in little endian
     * 
     * @param i A number of type int
     */
    public void writeLEInt(int i) throws IOException {
        out.write(i & 0xFF);
        out.write((i >>> 8) & 0xFF);
        out.write((i >>> 16) & 0xFF);
        out.write((i >>> 24) & 0xFF);
        written += 4;
    }

    /**
     * Writes a number of type long in little endian
     * 
     * @param l A number of type long
     */
    public void writeLELong(long l) throws IOException {
        out.write((int) l & 0xFF);
        out.write((int) (l >>> 8) & 0xFF);
        out.write((int) (l >>> 16) & 0xFF);
        out.write((int) (l >>> 24) & 0xFF);
        out.write((int) (l >>> 32) & 0xFF);
        out.write((int) (l >>> 40) & 0xFF);
        out.write((int) (l >>> 48) & 0xFF);
        out.write((int) (l >>> 56) & 0xFF);
        written += 8;
    }

    /**
     * Writes a number of type float in little endian
     * 
     * @param f A number of type float.
     */
    public final void writeLEFloat(float f) throws IOException {
        this.writeLEInt(Float.floatToIntBits(f));
    }

    /**
     * Writes a number a number of type double in little endian
     * 
     * @param d A number of type double
     */
    public final void writeLEDouble(double d) throws IOException {
        this.writeLELong(Double.doubleToLongBits(d));
    }

    /**
     * Writes a String in little endian
     * 
     * @param s A string
     */
    public void writeLEChars(String s) throws IOException {
        int length = s.length();
        for (int i = 0; i < length; i++) {
            int c = s.charAt(i);
            out.write(c & 0xFF);
            out.write((c >>> 8) & 0xFF);
        }
        written += length * 2;
    }

    public void writeLEUTF(String s) throws IOException {
        int numchars = s.length();
        int numbytes = 0;
        for (int i = 0; i < numchars; i++) {
            int c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
                numbytes++;
            else if (c > 0x07FF)
                numbytes += 3;
            else
                numbytes += 2;
        }

        if (numbytes > 65535)
            throw new UTFDataFormatException();

        out.write((numbytes >>> 8) & 0xFF);
        out.write(numbytes & 0xFF);
        for (int i = 0; i < numchars; i++) {
            int c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out.write(c);
            } else if (c > 0x07FF) {
                out.write(0xE0 | ((c >> 12) & 0x0F));
                out.write(0x80 | ((c >> 6) & 0x3F));
                out.write(0x80 | (c & 0x3F));
                written += 2;
            } else {
                out.write(0xC0 | ((c >> 6) & 0x1F));
                out.write(0x80 | (c & 0x3F));
                written += 1;
            }
        }
        written += numchars + 2;
    }
}