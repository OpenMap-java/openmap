//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.time;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.omGraphics.OMScalingIcon;
import com.bbn.openmap.proj.Projection;

public class TemporalOMScalingIcon
        extends OMScalingIcon
        implements TemporalOMGraphic {

    /**
     * Construct a blank TemporalOMScalingIcon, to be filled in with set calls.
     */
    public TemporalOMScalingIcon(Object id, int renderType, boolean interpolate) {
        this.id = id;
        this.interpolate = interpolate;
        setRenderType(renderType);
        timeStamps = new TemporalPointSupport(renderType);
    }

    // /////////////////////////////////// INT PIXELS - DIRECT
    // COLORMODEL

    /**
     * Creates an TemporalOMScalingIcon from images, Lat/Lon placement with a
     * direct colormodel image.
     * 
     * @param id the id of the TemporalOMScalingIcon
     * @param renderType rendertype of coordinates to be used in moving this
     *        around
     * @param interpolate flag to interpolate position between TemporalRecords.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @param baseScale the scale where the icon will be show regular size.
     * @see #setPixel
     */
    public TemporalOMScalingIcon(Object id, int renderType, boolean interpolate, int w, int h, int[] pix, float baseScale) {
        this(id, renderType, interpolate);
        setColorModel(OMRasterObject.COLORMODEL_DIRECT);
        setWidth(w);
        setHeight(h);
        setPixels(pix);
        this.baseScale = baseScale;
    }

    // //////////////////////////////////// IMAGEICON

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon.
     * 
     * @param id the id of the TemporalOMScalingIcon
     * @param renderType rendertype of coordinates to be used in moving this
     *        around
     * @param interpolate flag to interpolate position between TemporalRecords.
     * @param ii ImageIcon used for the image.
     * @param baseScale the scale where the icon will be show regular size.
     */
    public TemporalOMScalingIcon(Object id, int renderType, boolean interpolate, ImageIcon ii, float baseScale) {
        this(id, renderType, interpolate);
        setWidth(ii.getIconWidth());
        setHeight(ii.getIconHeight());
        setColorModel(OMRasterObject.COLORMODEL_IMAGEICON);
        setImage(ii.getImage());
        setBaseScale(baseScale);
    }

    /**
     * Create an scaling icon that will be moved around.
     * 
     * @param id the id of the TemporalOMScalingIcon
     * @param renderType rendertype of coordinates to be used in moving this
     *        around
     * @param interpolate flag to interpolate position between TemporalRecords.
     * @param ii ImageIcon used for the image.
     */
    public TemporalOMScalingIcon(Object id, int renderType, boolean interpolate, ImageIcon ii) {
        this(id, renderType, interpolate, ii.getImage(), 4000000);
        setMaxScale(4000000);
        setMinScale(4000000);
    }

    /**
     * Create an scaling icon that will be moved around.
     * 
     * @param id ID of object.
     * @param renderType the interpreted path between known positions
     * @param interpolate whether the positions between known timestamps should
     *        be interpolated.
     * @param ii ImageIcon used for image
     * @param baseScale
     */
    public TemporalOMScalingIcon(Object id, int renderType, boolean interpolate, Image ii, float baseScale) {
        this(id, renderType, interpolate);
        setColorModel(OMRasterObject.COLORMODEL_IMAGEICON);
        setImage(ii);
        setBaseScale(baseScale);
    }

    /**
     * Create a scaling icon that moves.
     * 
     * @param id ID of icon
     * @param renderType how position will be interpolated between know
     *        positions
     * @param interpolate whether position should be interpolated between known
     *        positions.
     * @param image java Image to use for icon.
     */
    public TemporalOMScalingIcon(Object id, int renderType, boolean interpolate, Image image) {
        this(id, renderType, interpolate);
        setColorModel(OMRasterObject.COLORMODEL_IMAGEICON);
        setImage(image);
        setBaseScale(40000000);
        setMaxScale(4000000);
        setMinScale(4000000);
    }

    // //////////////////////////////////// BYTE PIXELS with
    // COLORTABLE

    /**
     * Lat/Lon placement with a indexed colormodel, which is using a colortable
     * and a byte array to construct the int[] pixels.
     * 
     * @param id the id of the TemporalOMScalingIcon
     * @param renderType rendertype of coordinates to be used in moving this
     *        around
     * @param interpolate flag to interpolate position between TemporalRecords.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @param baseScale the scale where the icon will be show regular size.
     * @see #setPixel
     */
    public TemporalOMScalingIcon(Object id, int renderType, boolean interpolate, int w, int h, byte[] bytes, Color[] colorTable,
                                 int trans, float baseScale) {

        this(id, renderType, interpolate);
        setColorModel(OMRasterObject.COLORMODEL_INDEXED);
        setBaseScale(baseScale);
        setWidth(w);
        setHeight(h);
        setBits(bytes);
        setColors(colorTable);
        setTransparent(trans);
    }

    protected Object id;

    /**
     * A list of points where this point should be.
     */
    protected TemporalPointSupport timeStamps;

    /**
     * Flag to indicate that intermediate positions between locations should be
     * interpolated.
     */
    protected boolean interpolate = false;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public boolean isInterpolate() {
        return interpolate;
    }

    public void setInterpolate(boolean interpolate) {
        this.interpolate = interpolate;
    }

    /**
     * Checks the internal id against the given one.
     */
    public boolean thisIsYou(Object n) {
        return id.equals(n);
    }

    /**
     * Add a TimeStamp to the point.
     */
    public void addTimeStamp(TemporalRecord timeStamp) {
        timeStamps.add(timeStamp);
        setNeedToRegenerate(true);
    }

    public boolean removeTimeStamp(TemporalRecord timeStamp) {
        return timeStamps.remove(timeStamp);
    }

    public void clearTimeStamps() {
        timeStamps.clear();
    }

    /**
     * Given a time, figure out the location. If the time is before the earliest
     * time or after the latest time, the location will be set to the first or
     * last known location, but the marker will made invisible. If the time is
     * in between the first and last time, the position will be interpolated.
     */
    public TemporalPoint setPosition(long time) {
        return timeStamps.getPosition(time, interpolate);
    }

    /**
     * Prepare the ScenarioPoint to be rendered in its position at a certain
     * time.
     */
    public void generate(Projection p, long time) {

        TemporalPoint tp = setPosition(time);

        if (tp == null) {
            return;
        }

        Point2D pt = tp.getLocation();
        switch (renderType) {
            case RENDERTYPE_XY:
                setX((int) pt.getX());
                setY((int) pt.getY());
                break;
            default:
                setLat(pt.getY());
                setLon(pt.getX());
        }

        super.generate(p);
    }

}
