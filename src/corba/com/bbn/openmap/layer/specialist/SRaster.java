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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SRaster.java,v $
// $RCSfile: SRaster.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.CTEntry;
import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.UpdateGraphic;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.DeclutterType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;
import com.bbn.openmap.corba.CSpecialist.RasterPackage.ERaster;
import com.bbn.openmap.corba.CSpecialist.RasterPackage.RASF_update;

/**
 * SRaster is a specialist graphic object that creates and manipulates
 * image data. It can be used for image icons, or for map subframes
 * that tile together to cover the whole display.
 */
public class SRaster extends SGraphic /* used to be _RasterImplBase */{
    /** YX screen location of the upper left corner of the image. */
    protected XYPoint p1_;
    /** Lat/lon location of the upper left corner of the image. */
    protected LLPoint ll1_;
    /** Width of the image. */
    protected short width_;
    /** Height of the image. */
    protected short height_;
    /** The location (left-right) of the hot spot of the image */
    protected short x_hot_;
    /** The location (up-down) of the hot spot of the image. */
    protected short y_hot_;
    /** The image pixel data. */
    protected byte[] pixels_;
    /** The number of colors in the color table for the image. */
    protected short colorsTotal_;
    /** The array of RGB colors, aka the color table. */
    protected CTEntry[] ct_;
    /**
     * The index into the colortable for the invisible pixel. Not
     * used.
     */
    protected short transparent_;

    /** Generic object with nothing allocated internally. */
    public SRaster() {
        super(GraphicType.GT_Raster,
              RenderType.RT_Unknown,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint((short) 0, (short) 0);
        ll1_ = new LLPoint(0f, 0f);
        width_ = 0;
        height_ = 0;
        x_hot_ = 0;
        y_hot_ = 0;
        pixels_ = new byte[0];
        colorsTotal_ = 0;
        transparent_ = 0;
        ct_ = new CTEntry[0];
    }

    /** Lat/Lon placement */
    public SRaster(LLPoint ll1, short width, short height, byte[] pixels,
            short colorsTotal, CTEntry[] ct, short transparent, short x_hot,
            short y_hot) {

        super(GraphicType.GT_Raster,
              RenderType.RT_LatLon,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint((short) 0, (short) 0);
        ll1_ = ll1;
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        pixels_ = pixels;
        transparent_ = transparent;
        makeMatch(colorsTotal, ct);
    }

    /** XY placement. */
    public SRaster(short x1, short y1, short width, short height,
            byte[] pixels, short colorsTotal, CTEntry[] ct, short transparent,
            short x_hot, short y_hot) {
        super(GraphicType.GT_Raster,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint(x1, y1);
        ll1_ = new LLPoint(0f, 0f);
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        pixels_ = pixels;
        makeMatch(colorsTotal, ct);
        transparent_ = transparent;
    }

    /** Lat/lon placement with XY offset. */
    public SRaster(LLPoint ll1, short offset_x1, short offset_y1, short width,
            short height, byte[] pixels, short colorsTotal, CTEntry[] ct,
            short transparent, short x_hot, short y_hot) {
        super(GraphicType.GT_Raster,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint(offset_x1, offset_y1);
        ll1_ = ll1;
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        pixels_ = pixels;
        makeMatch(colorsTotal, ct);
        transparent_ = transparent;
    }

    /**
     * Lat/Lon placement. Use this if you are building images in a
     * cache, for tiled mapping data or something else where the data
     * is not yet known. The memory for the pixels will be allocated,
     * and then they can be set with image data later when a database
     * is accessed.
     * 
     * @see #setPixel(short x, short y, byte ctIndex)
     * @see #setColor(int index, CTEntry color)
     */
    public SRaster(LLPoint ll1, short width, short height, short x_hot,
            short y_hot) {
        super(GraphicType.GT_Raster,
              RenderType.RT_LatLon,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint((short) 0, (short) 0);
        ll1_ = ll1;
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        pixels_ = new byte[height * width];
        colorsTotal_ = 0;
        transparent_ = 0;
        ct_ = new CTEntry[0];
    }

    /**
     * XY placement. Use this if you are building images in a cache,
     * for tiled mapping data or something else where the data is not
     * yet known. The memory for the pixels will be allocated, and
     * then they can be set with image data later when a database is
     * accessed.
     * 
     * @see #setPixel(short x, short y, byte ctIndex)
     * @see #setColor(int index, CTEntry color)
     */
    public SRaster(short x1, short y1, short width, short height, short x_hot,
            short y_hot) {
        super(GraphicType.GT_Raster,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint(x1, y1);
        ll1_ = new LLPoint(0f, 0f);
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        pixels_ = new byte[height * width];
        colorsTotal_ = 0;
        transparent_ = 0;
        ct_ = new CTEntry[0];
    }

    /**
     * Lat/lon placement with XY offset. Use this if you are building
     * images in a cache, for tiled mapping data or something else
     * where the data is not yet known. The memory for the pixels will
     * be allocated, and then they can be set with image data later
     * when a database is accessed.
     * 
     * @see #setPixel(short x, short y, byte ctIndex)
     * @see #setColor(int index, CTEntry color)
     */
    public SRaster(LLPoint ll1, short offset_x1, short offset_y1, short width,
            short height, short x_hot, short y_hot) {
        super(GraphicType.GT_Raster,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint(offset_x1, offset_y1);
        ll1_ = ll1;
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        pixels_ = new byte[height * width];
        colorsTotal_ = 0;
        transparent_ = 0;
        ct_ = new CTEntry[0];
    }

    /**
     * Set image pixel data.
     * 
     * @param x Horizontal location of pixel from left.
     * @param y Vertical location of pixel from top.
     * @param ctIndex The array index of the applicable color in the
     *        color table.
     */
    public boolean setPixel(short x, short y, byte ctIndex) {
        if (boundsSafe(x, y)) {
            pixels_[(y * width_) + x] = ctIndex;
            return true;
        }
        return false; //fail
    }

    /**
     * Set image pixel data.
     * 
     * @param x Horizontal location of pixel from left.
     * @param y Vertical location of pixel from top.
     */
    public byte getPixel(short x, short y) {
        if (boundsSafe(x, y)) {
            return pixels_[(y * width_) + x];
        }
        return 0; //fail - but also the ct[0] - hmmmmm.
    }

    /**
     * Set color entry in the colortable.
     * 
     * @param index Place in the color table array for the color.
     * @param color The color of the entry.
     */
    public boolean setColor(int index, CTEntry color) {
        if (index < colorsTotal_) {
            ct_[index] = color;
            return true;
        }
        return false;
    }

    /**
     * Get color entry in the colortable.
     * 
     * @param index Place in the color table array for the color.
     */
    public CTEntry getColor(int index) {
        if (index < colorsTotal_)
            return ct_[index];
        return null;
    }

    private boolean boundsSafe(short x, short y) {
        if ((y < 0) || (y >= height_) || (x < 0) || (x >= width_))
            return false;
        return true;
    }

    /** Force the colorsTotal_/ct_ to be the larger of the two. */
    private void makeMatch(short colorsTotal, CTEntry[] ct) {
        if (colorsTotal == ct.length) {
            ct_ = ct;
            colorsTotal_ = colorsTotal;
        } else {
            if (ct.length > colorsTotal) {
                colorsTotal_ = (short) ct.length;
                ct_ = ct;
            } else {
                ct_ = new CTEntry[colorsTotal];
                for (int i = 0; i < ct.length; i++)
                    ct_[i] = ct[i];
            }
        }
    }

    // Object methods for member variables - Any resizing of the
    // height or width recreates the pixel array, deleting all that
    // was before. Setting the number of colors or color table
    // automatically makes them align to the larger value. I try to
    // copy previous color values to the new table.
    public void p1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
    }

    public com.bbn.openmap.corba.CSpecialist.XYPoint p1() {
        return p1_;
    }

    public void ll1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
    }

    public com.bbn.openmap.corba.CSpecialist.LLPoint ll1() {
        return ll1_;
    }

    public void width(short width) {
        width_ = width;
        pixels_ = new byte[width_ * height_];
    }

    public short width() {
        return width_;
    }

    public void height(short height) {
        height_ = height;
        pixels_ = new byte[width_ * height_];
    }

    public short height() {
        return height_;
    }

    public void x_hot(short x_hot) {
        x_hot_ = x_hot;
    }

    public short x_hot() {
        return x_hot_;
    }

    public void y_hot(short y_hot) {
        y_hot_ = y_hot;
    }

    public short y_hot() {
        return y_hot_;
    }

    public void pixels(byte[] pixels) {
        pixels_ = pixels;
    }

    public byte[] pixels() {
        return pixels_;
    }

    public void colorsTotal(short num_colors) {
        CTEntry[] tmp = ct_;
        makeMatch(num_colors, tmp);
    }

    public short colorsTotal() {
        return colorsTotal_;
    }

    public void ct(CTEntry[] ct) {
        makeMatch(colorsTotal_, ct);
    }

    public CTEntry[] ct() {
        return ct_;
    }

    public void transparent(short transparent) {
        transparent_ = transparent;
    }

    public short transparent() {
        return transparent_;
    }

    public ERaster fill() {

        return new ERaster(eg, p1_, ll1_, pixels_, width_, height_, x_hot_, y_hot_, colorsTotal_, ct_, transparent_);
    }

    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.eras(fill());
        return ugraphic;
    }

    public void changeP1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
        RASF_update gupdate = new RASF_update();
        gupdate.p1(p1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLl1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
        RASF_update gupdate = new RASF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeWidth(short width) {
        width_ = width;
        RASF_update gupdate = new RASF_update();
        gupdate.width(width);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeHeight(short height) {
        height_ = height;
        RASF_update gupdate = new RASF_update();
        gupdate.height(height);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeX_hot(short x_hot) {
        x_hot_ = x_hot;
        RASF_update gupdate = new RASF_update();
        gupdate.x_hot(x_hot);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeY_hot(short y_hot) {
        y_hot_ = y_hot;
        RASF_update gupdate = new RASF_update();
        gupdate.y_hot(y_hot);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changePixels(byte[] pixels) {
        pixels_ = pixels;
        RASF_update gupdate = new RASF_update();
        gupdate.pixels(pixels);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeColorsTotal(short num_colors) {
        colorsTotal_ = num_colors;
        RASF_update gupdate = new RASF_update();
        gupdate.colorsTotal(num_colors);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeCt(CTEntry[] ct) {
        ct_ = ct;
        RASF_update gupdate = new RASF_update();
        gupdate.ct(ct);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeTransparent(short transparent) {
        transparent_ = transparent;
        RASF_update gupdate = new RASF_update();
        gupdate.transparent(transparent);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rasf_update(gupdate);
        addGraphicChange(ug);
    }
}

