// JpegEncoder - write out an image as a JPEG
//
// Copyright (C) 1996 by Jef Poskanzer <jef@acme.com>.  All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/

package Acme.JPM.Encoders;

import java.util.*;
import java.io.*;
import java.awt.Image;
import java.awt.image.*;

/// Write out an image as a JPEG.
// DOESN'T WORK YET.
// <P>
// <A HREF="/resources/classes/Acme/JPM/Encoders/JpegEncoder.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
// <P>
// @see ToJpeg

public class JpegEncoder extends ImageEncoder
    {

    /// Constructor.
    // @param img The image to encode.
    // @param out The stream to write the JPEG to.
    public JpegEncoder( Image img, OutputStream out ) throws IOException
	{
	super( img, out );
	}

    /// Constructor.
    // @param prod The ImageProducer to encode.
    // @param out The stream to write the JPEG to.
    public JpegEncoder( ImageProducer prod, OutputStream out ) throws IOException
	{
	super( prod, out );
	}

    int qfactor = 100;

    /// Set the Q-factor.
    public void setQfactor( int qfactor )
	{
	this.qfactor = qfactor;
	}

    int width, height;
    int[][] rgbPixels;
    
    void encodeStart( int width, int height ) throws IOException
	{
	this.width = width;
	this.height = height;
	rgbPixels = new int[height][width];
	}

    void encodePixels(
	int x, int y, int w, int h, int[] rgbPixels, int off, int scansize )
	throws IOException
	{
	// Save the pixels.
	for ( int row = 0; row < h; ++row )
	    System.arraycopy(
		rgbPixels, row * scansize + off,
		this.rgbPixels[y + row], x, w );

	}
    
    void encodeDone() throws IOException
	{
	writeJfifHuffHeader();
	// !!!
	}
    

    // Some of the following code is derived from the Berkeley Continuous
    // Media Toolkit (http://bmrc.berkeley.edu/projects/cmt/), which is
    // Copyright (c) 1996 The Regents of the University of California.
    // All rights reserved.
    //
    // IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
    // DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING
    // OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE
    // UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
    // DAMAGE.
    //
    // THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
    // INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
    // AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
    // ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION
    // TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.


    // This array represents the default JFIF header for quality = 100 and
    // size = 640x480, with Huffman tables. The values are adjusted when a
    // file is generated.
    private static byte[] jfifHuff100Header = {
	// SOI
	(byte) 0xFF, (byte) 0xD8,

	// JFIF header
	(byte) 0xFF, (byte) 0xE0,		// Marker
	(byte) 0x00, (byte) 0x10,		// Length = 16 bytes
	(byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46,	// "JFIF"
	(byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00,
	(byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,

	// Start of frame (section B.2.2)
	(byte) 0xFF, (byte) 0xC0,		// Baseline DCT
	(byte) 0x00, (byte) 0x11,		// Length = 17 bytes
	(byte) 0x08,				// Sample precision
	(byte) 0x01, (byte) 0xE0,		// Height
	(byte) 0x02, (byte) 0x80,		// Width
	(byte) 0x03,				// Number of components = 3
	// Scan 1: 2:1 horiz, (byte) 1:1 vertical, (byte) use QT 0
	(byte) 0x01, (byte) 0x21, (byte) 0x00,
	// Scan 2: 1:1 horiz, (byte) 1:1 vertical, (byte) use QT 1
	(byte) 0x02, (byte) 0x11, (byte) 0x01,
	// Scan 3: 1:1 horiz, (byte) 1:1 vertical, (byte) use QT 1
	(byte) 0x03, (byte) 0x11, (byte) 0x01,

	// Define Quant table (section B.2.4.1)
	(byte) 0xFF, (byte) 0xDB,		// Marker
	(byte) 0x00, (byte) 0x84,		// Length (both tables)
	(byte) 0x00,			// 8 bit values, (byte) table 0
	(byte) 0x10, (byte) 0x0B, (byte) 0x0C, (byte) 0x0E, (byte) 0x0C,
	(byte) 0x0A, (byte) 0x10, (byte) 0x0E, (byte) 0x0D, (byte) 0x0E,
	(byte) 0x12, (byte) 0x11, (byte) 0x10, (byte) 0x13, (byte) 0x18,
	(byte) 0x28, (byte) 0x1A, (byte) 0x18, (byte) 0x16, (byte) 0x16,
	(byte) 0x18, (byte) 0x31, (byte) 0x23, (byte) 0x25, (byte) 0x1D,
	(byte) 0x28, (byte) 0x3A, (byte) 0x33, (byte) 0x3D, (byte) 0x3C,
	(byte) 0x39, (byte) 0x33, (byte) 0x38, (byte) 0x37, (byte) 0x40,
	(byte) 0x48, (byte) 0x5C, (byte) 0x4E, (byte) 0x40, (byte) 0x44,
	(byte) 0x57, (byte) 0x45, (byte) 0x37, (byte) 0x38, (byte) 0x50,
	(byte) 0x6D, (byte) 0x51, (byte) 0x57, (byte) 0x5F, (byte) 0x62,
	(byte) 0x67, (byte) 0x68, (byte) 0x67, (byte) 0x3E, (byte) 0x4D,
	(byte) 0x71, (byte) 0x79, (byte) 0x70, (byte) 0x64, (byte) 0x78,
	(byte) 0x5C, (byte) 0x65, (byte) 0x67, (byte) 0x63,

	(byte) 0x01,			// 8 bit values, (byte) table 1
	(byte) 0x11, (byte) 0x12, (byte) 0x12, (byte) 0x18, (byte) 0x15,
	(byte) 0x18, (byte) 0x2F, (byte) 0x1A, (byte) 0x1A, (byte) 0x2F,
	(byte) 0x63, (byte) 0x42, (byte) 0x38, (byte) 0x42, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,

	// Define huffman table (section B.2.4.1)
	(byte) 0xFF, (byte) 0xC4,		// Marker
	(byte) 0x00, (byte) 0x1F,		// Length (31 bytes)
	(byte) 0x00,				// DC, (byte) table 0
	(byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x01, (byte) 0x01,
	(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00,
	(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
	(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
	(byte) 0x09, (byte) 0x0A, (byte) 0x0B,

	// Define huffman table (section B.2.4.1)
	(byte) 0xFF, (byte) 0xC4,		// Marker
	(byte) 0x00, (byte) 0xB5,		// Length (181 bytes)
	(byte) 0x10,				// AC, (byte) table 0
	(byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x03, (byte) 0x03,
	(byte) 0x02, (byte) 0x04, (byte) 0x03, (byte) 0x05, (byte) 0x05,
	(byte) 0x04, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x01,
	(byte) 0x7D, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x00,
	(byte) 0x04, (byte) 0x11, (byte) 0x05, (byte) 0x12, (byte) 0x21,
	(byte) 0x31, (byte) 0x41, (byte) 0x06, (byte) 0x13, (byte) 0x51,
	(byte) 0x61, (byte) 0x07, (byte) 0x22, (byte) 0x71, (byte) 0x14,
	(byte) 0x32, (byte) 0x81, (byte) 0x91, (byte) 0xA1, (byte) 0x08,
	(byte) 0x23, (byte) 0x42, (byte) 0xB1, (byte) 0xC1, (byte) 0x15,
	(byte) 0x52, (byte) 0xD1, (byte) 0xF0, (byte) 0x24, (byte) 0x33,
	(byte) 0x62, (byte) 0x72, (byte) 0x82, (byte) 0x09, (byte) 0x0A,
	(byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1A,
	(byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29,
	(byte) 0x2A, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37,
	(byte) 0x38, (byte) 0x39, (byte) 0x3A, (byte) 0x43, (byte) 0x44,
	(byte) 0x45, (byte) 0x46, (byte) 0x47, (byte) 0x48, (byte) 0x49,
	(byte) 0x4A, (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56,
	(byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A, (byte) 0x63,
	(byte) 0x64, (byte) 0x65, (byte) 0x66, (byte) 0x67, (byte) 0x68,
	(byte) 0x69, (byte) 0x6A, (byte) 0x73, (byte) 0x74, (byte) 0x75,
	(byte) 0x76, (byte) 0x77, (byte) 0x78, (byte) 0x79, (byte) 0x7A,
	(byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87,
	(byte) 0x88, (byte) 0x89, (byte) 0x8A, (byte) 0x92, (byte) 0x93,
	(byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98,
	(byte) 0x99, (byte) 0x9A, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4,
	(byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8, (byte) 0xA9,
	(byte) 0xAA, (byte) 0xB2, (byte) 0xB3, (byte) 0xB4, (byte) 0xB5,
	(byte) 0xB6, (byte) 0xB7, (byte) 0xB8, (byte) 0xB9, (byte) 0xBA,
	(byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5, (byte) 0xC6,
	(byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0xCA, (byte) 0xD2,
	(byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7,
	(byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xE1, (byte) 0xE2,
	(byte) 0xE3, (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7,
	(byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xF1, (byte) 0xF2,
	(byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7,
	(byte) 0xF8, (byte) 0xF9, (byte) 0xFA,

	// Define huffman table (section B.2.4.1)
	(byte) 0xFF, (byte) 0xC4,		// Marker
	(byte) 0x00, (byte) 0x1F,		// Length (31 bytes)
	(byte) 0x01,				// DC, (byte) table 1
	(byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x01,
	(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
	(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
	(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
	(byte) 0x09, (byte) 0x0A, (byte) 0x0B,

	// Define huffman table (section B.2.4.1)
	(byte) 0xFF, (byte) 0xC4,		// Marker
	(byte) 0x00, (byte) 0xB5,		// Length (181 bytes)
	(byte) 0x11,				// AC, (byte) table 1
	(byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x02, (byte) 0x04,
	(byte) 0x04, (byte) 0x03, (byte) 0x04, (byte) 0x07, (byte) 0x05,
	(byte) 0x04, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x02,
	(byte) 0x77, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
	(byte) 0x11, (byte) 0x04, (byte) 0x05, (byte) 0x21, (byte) 0x31,
	(byte) 0x06, (byte) 0x12, (byte) 0x41, (byte) 0x51, (byte) 0x07,
	(byte) 0x61, (byte) 0x71, (byte) 0x13, (byte) 0x22, (byte) 0x32,
	(byte) 0x81, (byte) 0x08, (byte) 0x14, (byte) 0x42, (byte) 0x91,
	(byte) 0xA1, (byte) 0xB1, (byte) 0xC1, (byte) 0x09, (byte) 0x23,
	(byte) 0x33, (byte) 0x52, (byte) 0xF0, (byte) 0x15, (byte) 0x62,
	(byte) 0x72, (byte) 0xD1, (byte) 0x0A, (byte) 0x16, (byte) 0x24,
	(byte) 0x34, (byte) 0xE1, (byte) 0x25, (byte) 0xF1, (byte) 0x17,
	(byte) 0x18, (byte) 0x19, (byte) 0x1A, (byte) 0x26, (byte) 0x27,
	(byte) 0x28, (byte) 0x29, (byte) 0x2A, (byte) 0x35, (byte) 0x36,
	(byte) 0x37, (byte) 0x38, (byte) 0x39, (byte) 0x3A, (byte) 0x43,
	(byte) 0x44, (byte) 0x45, (byte) 0x46, (byte) 0x47, (byte) 0x48,
	(byte) 0x49, (byte) 0x4A, (byte) 0x53, (byte) 0x54, (byte) 0x55,
	(byte) 0x56, (byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A,
	(byte) 0x63, (byte) 0x64, (byte) 0x65, (byte) 0x66, (byte) 0x67,
	(byte) 0x68, (byte) 0x69, (byte) 0x6A, (byte) 0x73, (byte) 0x74,
	(byte) 0x75, (byte) 0x76, (byte) 0x77, (byte) 0x78, (byte) 0x79,
	(byte) 0x7A, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85,
	(byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8A,
	(byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96,
	(byte) 0x97, (byte) 0x98, (byte) 0x99, (byte) 0x9A, (byte) 0xA2,
	(byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7,
	(byte) 0xA8, (byte) 0xA9, (byte) 0xAA, (byte) 0xB2, (byte) 0xB3,
	(byte) 0xB4, (byte) 0xB5, (byte) 0xB6, (byte) 0xB7, (byte) 0xB8,
	(byte) 0xB9, (byte) 0xBA, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4,
	(byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9,
	(byte) 0xCA, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5,
	(byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA,
	(byte) 0xE2, (byte) 0xE3, (byte) 0xE4, (byte) 0xE5, (byte) 0xE6,
	(byte) 0xE7, (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xF2,
	(byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7,
	(byte) 0xF8, (byte) 0xF9, (byte) 0xFA,

	// Start of Scan (section B.2.3)
	(byte) 0xFF, (byte) 0xDA,		// Marker
	(byte) 0x00, (byte) 0x0C,		// Length of header
	(byte) 0x03,				// Number of image components
	// Scan 1: use DC/AC huff tables 0/0
	(byte) 0x01, (byte) 0x00,
	// Scan 2: use DC/AC huff tables 1/1
	(byte) 0x02, (byte) 0x11,
	// Scan 3: use DC/AC huff tables 1/1
	(byte) 0x03, (byte) 0x11,
	(byte) 0x00, (byte) 0x3F, (byte) 0x00	// Not used
	};

    // This array represents the default JFIF header for quality = 100 and
    // size = 640x480, without Huffman tables. The values are adjusted when a
    // file is generated.
    private static byte[] jfifNoHuff100Header = {
	// SOI
	(byte) 0xFF, (byte) 0xD8,

	// JFIF header
	(byte) 0xFF, (byte) 0xE0,		// Marker
	(byte) 0x00, (byte) 0x10,		// Length = 16 bytes
	(byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46,	// "JFIF"
	(byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00,
	(byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,

	// Start of frame (section B.2.2)
	(byte) 0xFF, (byte) 0xC0,		// Baseline DCT
	(byte) 0x00, (byte) 0x11,		// Length = 17 bytes
	(byte) 0x08,				// Sample precision
	(byte) 0x01, (byte) 0xE0,		// Height
	(byte) 0x02, (byte) 0x80,		// Width
	(byte) 0x03,				// Number of components = 3
	// Scan 1: 2:1 horiz, (byte) 1:1 vertical, (byte) use QT 0
	(byte) 0x01, (byte) 0x21, (byte) 0x00,
	// Scan 2: 1:1 horiz, (byte) 1:1 vertical, (byte) use QT 1
	(byte) 0x02, (byte) 0x11, (byte) 0x01,
	// Scan 3: 1:1 horiz, (byte) 1:1 vertical, (byte) use QT 1
	(byte) 0x03, (byte) 0x11, (byte) 0x01,

	// Define Quant table (section B.2.4.1)
	(byte) 0xFF, (byte) 0xDB,		// Marker
	(byte) 0x00, (byte) 0x84,		// Length (both tables)
	(byte) 0x00,				// 8 bit values, (byte) table 0
	(byte) 0x10, (byte) 0x0B, (byte) 0x0C, (byte) 0x0E, (byte) 0x0C,
	(byte) 0x0A, (byte) 0x10, (byte) 0x0E, (byte) 0x0D, (byte) 0x0E,
	(byte) 0x12, (byte) 0x11, (byte) 0x10, (byte) 0x13, (byte) 0x18,
	(byte) 0x28, (byte) 0x1A, (byte) 0x18, (byte) 0x16, (byte) 0x16,
	(byte) 0x18, (byte) 0x31, (byte) 0x23, (byte) 0x25, (byte) 0x1D,
	(byte) 0x28, (byte) 0x3A, (byte) 0x33, (byte) 0x3D, (byte) 0x3C,
	(byte) 0x39, (byte) 0x33, (byte) 0x38, (byte) 0x37, (byte) 0x40,
	(byte) 0x48, (byte) 0x5C, (byte) 0x4E, (byte) 0x40, (byte) 0x44,
	(byte) 0x57, (byte) 0x45, (byte) 0x37, (byte) 0x38, (byte) 0x50,
	(byte) 0x6D, (byte) 0x51, (byte) 0x57, (byte) 0x5F, (byte) 0x62,
	(byte) 0x67, (byte) 0x68, (byte) 0x67, (byte) 0x3E, (byte) 0x4D,
	(byte) 0x71, (byte) 0x79, (byte) 0x70, (byte) 0x64, (byte) 0x78,
	(byte) 0x5C, (byte) 0x65, (byte) 0x67, (byte) 0x63,

	(byte) 0x01,				// 8 bit values, (byte) table 1
	(byte) 0x11, (byte) 0x12, (byte) 0x12, (byte) 0x18, (byte) 0x15,
	(byte) 0x18, (byte) 0x2F, (byte) 0x1A, (byte) 0x1A, (byte) 0x2F,
	(byte) 0x63, (byte) 0x42, (byte) 0x38, (byte) 0x42, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,
	(byte) 0x63, (byte) 0x63, (byte) 0x63, (byte) 0x63,

	// Start of Scan (section B.2.3)
	(byte) 0xFF, (byte) 0xDA,		// Marker
	(byte) 0x00, (byte) 0x0C,		// Length of header
	(byte) 0x03,				// Number of image components
	// Scan 1: use DC/AC huff tables 0/0
	(byte) 0x01, (byte) 0x00,
	// Scan 2: use DC/AC huff tables 1/1
	(byte) 0x02, (byte) 0x11,
	// Scan 3: use DC/AC huff tables 1/1
	(byte) 0x03, (byte) 0x11,
	(byte) 0x00, (byte) 0x3F, (byte) 0x00	// Not used
	};

    private void writeJfifHuffHeader() throws IOException
	{
	byte[] newHeader = new byte[jfifHuff100Header.length];

	System.arraycopy(
	    jfifHuff100Header, 0, newHeader, 0, jfifHuff100Header.length );

	// Set image width in JFIF header.
	newHeader[27] = (byte) ( ( width >>> 8 ) & 0xff );
	newHeader[28] = (byte) ( width & 0xff );

	// Set image height in JFIF header.
	newHeader[25] = (byte) ( ( height >>> 8 ) & 0xff );
	newHeader[26] = (byte) ( height & 0xff );

	// Adjust the quality factor.
	//
	// The default quality factor is 100, therefore if
	// our quality factor does not equal 100 we must
	// scale the quantization matrices in the JFIF header.
	// Note that values are clipped to a max of 255.
	if ( qfactor != 100 )
	    {
	    for ( int i = 44; i < 108; ++i )
		{
		int t = ( newHeader[i] * qfactor ) / 100;
		newHeader[i] = (byte) Math.max( t, 0xff );
		}
	    for ( int i = 109; i < 173; ++i )
		{
		int t = ( newHeader[i] * qfactor ) / 100;
		newHeader[i] = (byte) Math.max( t, 0xff );
		}    
	    }

	// Write out buffer.
	out.write( newHeader );
	}

    private void writeJfifNoHuffHeader() throws IOException
	{
	byte[] newHeader = new byte[jfifNoHuff100Header.length];

	System.arraycopy(
	    jfifNoHuff100Header, 0, newHeader, 0, jfifNoHuff100Header.length );

	// Set image width in JFIF header.
	newHeader[27] = (byte) ( ( width >>> 8 ) & 0xff );
	newHeader[28] = (byte) ( width & 0xff );

	// Set image height in JFIF header.
	newHeader[25] = (byte) ( ( height >>> 8 ) & 0xff );
	newHeader[26] = (byte) ( height & 0xff );

	// Adjust the quality factor.
	//
	// The default quality factor is 100, therefore if
	// our quality factor does not equal 100 we must
	// scale the quantization matrices in the JFIF header.
	// Note that values are clipped to a max of 255.
	if ( qfactor != 100 )
	    {
	    for ( int i = 44; i < 108; ++i )
		{
		int t = ( newHeader[i] * qfactor ) / 100;
		newHeader[i] = (byte) Math.max( t, 0xff );
		}
	    for ( int i = 109; i < 173; ++i )
		{
		int t = ( newHeader[i] * qfactor ) / 100;
		newHeader[i] = (byte) Math.max( t, 0xff );
		}    
	    }

	// Write out buffer.
	out.write( newHeader );
	}

    }
