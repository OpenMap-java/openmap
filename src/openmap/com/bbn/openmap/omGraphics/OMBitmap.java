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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMBitmap.java,v $
// $RCSfile: OMBitmap.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.Serializable;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The OMBitmap lets you create a two color image. The display color
 * is the foreground color, and the fill color is the background
 * color. OMColors can be used, and their transparency values will be
 * implemented.
 * <p>
 * The array of bytes is used to create the picture. Each bit,
 * representing a pixel, is examined, and the color is set to the
 * display or fill color, depending on the bit value.
 * 
 * There isn't a height and width restriction on OMBitmaps, but you do
 * have to be careful about the byte array that is used for one. The
 * OMBitmap is based on an X bitmap, not a Windows bmp file. Again,
 * each bit of the bytes corresponds to a pixel. If the height and/or
 * width of the bitmap isn't a multiple of 8, you have to round up the
 * number of bytes used so that the excess bits are covered in that
 * extra byte for the row.
 * <P>
 * 
 * So, for a 4x4 bitmap, you need:
 * 
 * <pre>
 * 
 * 
 *  ooooxxxx|ooooxxxx|ooooxxxx|ooooxxxx -&gt; 1 by 4 bytes, 4 bytes total
 * 
 *  
 * </pre>
 * 
 * where x's are the bits being used for the bitmap ( and whether they
 * are 1 or 0 dictates either foreground or background color), and o's
 * are the leftover bits (they are ignored). The '|' are byte
 * boundaries.
 * <P>
 * 
 * The bits, per byte, look like they are used in reverse order
 * because they are - least significant bit, per byte, is used in that
 * order.
 * <P>
 * 
 * For a 6x6 bitmap:
 * 
 * <PRE>
 * 
 * ooxxxxxx|ooxxxxxx|ooxxxxxx|ooxxxxxx|ooxxxxxx|ooxxxxxx -> 1 x 6
 * bytes, 6 bytes total
 * 
 * </PRE>
 * 
 * for a 10x10 bitmap:
 * 
 * <PRE>
 * 
 * xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx|xxxxxxxx|ooooooxx ->
 * 2 by 10 bytes, 20 bytes total
 * 
 * </PRE>
 * 
 * There is the ability to add a filter to the OMBitmap, to change
 * it's appearance for rendering. The most common filter, which is
 * included as a kind of default, is the scale filter. Filtering the
 * OMRasterObject replaces the bitmap variable, which is the internal
 * java.awt.Image used for rendering.
 */
public class OMBitmap extends OMRasterObject implements Serializable {

    /** Create empty, and add parameters later. */
    public OMBitmap() {
        super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
    }

    /**
     * Created a OMBitmap with a Lat/lon placement.
     * 
     * @param lt latitude of upper left corner of bitmap.
     * @param ln longitude of upper left corner of bitmap.
     * @param w width of bitmap.
     * @param h height of bitmap.
     * @param bytes byte array of bitmap, each bit representing a
     *        pixel.
     */
    public OMBitmap(double lt, double ln, int w, int h, byte[] bytes) {

        super(RENDERTYPE_LATLON, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        lat = lt;
        lon = ln;
        width = w;
        height = h;
        bits = bytes;
    }

    /**
     * Create an OMBitmap with a X/Y window placement.
     * 
     * @param x1 window pixel x location of upper left corner of
     *        bitmap.
     * @param y1 window pixel y location of upper left corner of
     *        bitmap.
     * @param w width of bitmap.
     * @param h height of bitmap.
     * @param bytes byte array of bitmap, each bit representing a
     *        pixel.
     */
    public OMBitmap(int x1, int y1, int w, int h, byte[] bytes) {

        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        x = x1;
        y = y1;
        width = w;
        height = h;
        bits = bytes;
    }

    /**
     * Create an OMBitmap, located at a Lat/lon with a X/Y offset
     * placement.
     * 
     * @param lt latitude of upper left corner of bitmap.
     * @param ln longitude of upper left corner of bitmap.
     * @param offset_x1 window pixel x location from ln of upper left
     *        corner of bitmap.
     * @param offset_y1 window pixel y location from lt of upper left
     *        corner of bitmap.
     * @param w width of bitmap.
     * @param h height of bitmap.
     * @param bytes byte array of bitmap, each bit representing a
     *        pixel.
     */
    public OMBitmap(double lt, double ln, int offset_x1, int offset_y1, int w,
            int h, byte[] bytes) {

        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);

        lat = lt;
        lon = ln;
        x = offset_x1;
        y = offset_y1;
        width = w;
        height = h;
        bits = bytes;
    }

    /**
     * Set the bytes used to create the pixels used to create the
     * image. Checks to see of the length*8 matches the height *
     * width, but doesn't do anything if they don't match, except
     * print out a warning. Make sure it does.
     * 
     * @param values byte values containing bit pixel values.
     */
    public void setBits(byte[] values) {
        super.setBits(values);
        if ((values.length * 8) != (height * width))
            Debug.output("OMBitmap: new byte[] size (" + +values.length
                    + ") (*8) doesn't" + " match [height*width (" + height
                    * width + ")]");
    }

    /**
     * Create the image pixels from the display color and the fill
     * color values, and the bitmap bytes. All of these attributes
     * should have been filled in.
     * 
     * @return true if the pixels were successfully created.
     */
    protected boolean computePixels() {

        int foreground, background;
        int npix, i, j, k, w;
        int[] masks = { 1, 2, 4, 8, 16, 32, 64, 128 };

        int nPixels = width * height;
        if (bits == null || (bits.length * 8) < nPixels) {
            System.err.println("OMBitmap.computePixels(): not enough bits!");
            return false;
        }

        pixels = new int[nPixels];

        // Init colors.
        Paint paint = getDisplayPaint();
        if (paint instanceof Color) {
            foreground = ((Color) paint).getRGB();
        } else {
            foreground = Color.black.getRGB();
        }

        paint = getFillPaint();
        if (paint instanceof Color) {
            background = ((Color) paint).getRGB();
        } else {
            background = OMGraphic.clear.getRGB();
        }

        int defaultColor = 128 << 24;

        // Now, using the foreground and background colors, build a
        // set of
        // pixels by traversing bitwise through the bitmap data.

        // Determine the excess number of bits at the end of each row.
        int excess = width % 8; // Remainder

        // And how many bytes will be used represent each row?
        int bytes_per_row = width / 8;

        if (excess > 0) {
            Debug.message("omGraphics", "OMBitmap.computePixels(): excess byte");
            bytes_per_row++;
        }

        Debug.message("omGraphics", "OMBitmap.computePixels(): bits.length = "
                + bits.length);

        for (npix = 0, i = 0; i < height; i++) { // for each row
            for (j = 0, w = 0; j < bytes_per_row; j++) { // for each
                                                         // row's byte
                int idx = (i * bytes_per_row) + j;
                for (k = 0; // for each byte's bits
                (k < 8) & // bits per byte
                        (w < width) & // bits per row
                        (npix < nPixels); // bits per bitmap
                k++, w++, npix++) {
                    int set = masks[k] & bits[idx];
                    if (set > 0) {
                        pixels[npix] = foreground;
                    } else {
                        pixels[npix] = background;
                    }
                }
            }
        }

        if (npix < nPixels - 1) {
            for (i = npix; i < nPixels; i++) {
                pixels[i] = defaultColor;
            }
        }
        return true;
    }

    /**
     * Create the rendered image from the pixel values.
     * 
     * @return true if the OMBitmap has enough information and
     *         generates the rendered image successfully.
     */
    public boolean generate(Projection proj) {

        // Position() sets the bitmap location on the screen!!!!
        if (!position(proj)) {
            Debug.message("omgraphic",
                    "OMBitmap: positioning failed in generate!");
            return false;
        }

        if (getNeedToRegenerate() || bitmap == null) {

            computePixels();

            Toolkit tk = Toolkit.getDefaultToolkit();
            bitmap = tk.createImage(new MemoryImageSource(width, height, pixels, 0, width));
        }

        if (imageFilter != null) {
            bitmap = filterImage(bitmap);
        }

        setShape();

        setNeedToRegenerate(false);
        return true;
    }

    /**
     * This sets the bitmap to recompute the pixels if the foreground
     * paint is changed.
     * 
     * @param value the new line color.
     */
    public void setLinePaint(Paint value) {
        super.setLinePaint(value);
        if (!selected) {
            setNeedToRegenerate(true);
        }
    }

    /**
     * This sets the bitmap to recompute the pixels if the foreground
     * paint is changed.
     * 
     * @param value the new select color.
     */
    public void setSelectPaint(Paint value) {
        super.setSelectPaint(value);
        if (selected) {
            setNeedToRegenerate(true);
        }
    }

    /**
     * This sets the bitmap to recompute the pixels if the background
     * color is changed.
     * 
     * @param value the new background color
     */
    public void setFillPaint(Paint value) {
        super.setFillPaint(value);
        setNeedToRegenerate(true);
    }

    /**
     * Set the selected attribute to true, and sets the color to the
     * select color.
     */
    public void select() {
        if (!isSelected()) {
            super.select();
            setNeedToRegenerate(true);
        }
    }

    /**
     * Set the selected attribute to false, sets the color to the line
     * color.
     */
    public void deselect() {
        if (isSelected()) {
            super.deselect();
            setNeedToRegenerate(true);
        }
    }

}