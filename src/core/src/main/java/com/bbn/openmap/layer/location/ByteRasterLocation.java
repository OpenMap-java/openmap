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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/ByteRasterLocation.java,v $
// $RCSfile: ByteRasterLocation.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.Point;

import javax.swing.ImageIcon;

import com.bbn.openmap.layer.DeclutterMatrix;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.Projection;

/**
 * A Location that takes a byte array and creates a Raster for a Location
 * marker. The byte array should be the contents of an image file (gif, jpeg)
 * that an ImageIcon object can use to create an Image object.
 */
public class ByteRasterLocation
        extends Location {
    /** The spacing between the label and the outside of the image. */
    public int SPACING = 0;

    /**
     * Create a ByteRasterLocation at a latitude/longitude location.
     * 
     * @param latitude latitide in decimal degrees
     * @param longitude longitude in decimal degrees.
     * @param name the label for the location.
     * @param bytearr a byte array from an image file that an ImageIcon can use
     *        to create an Image icon. Can also be the binary contents of an
     *        image from a database query.
     */
    public ByteRasterLocation(double latitude, double longitude, String name, byte bytearr[]) {
        super(latitude, longitude, name, getIconRaster(latitude, longitude, bytearr));

        if (location instanceof OMRaster) {
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
        }

    }

    /**
     * Create a ByteRasterLocation at a screen x/y location.
     * 
     * @param x horizontal pixel screen location from the the left side of the
     *        map.
     * @param y vertical pixel screen location, from the top of the map.
     * @param name the label for the location.
     * @param bytearr a byte array from an image file that an ImageIcon can use
     *        to create an Image icon. Can also be the binary contents of an
     *        image from a database query.
     */
    public ByteRasterLocation(int x, int y, String name, byte bytearr[]) {
        super(x, y, name, getIconRaster(x, y, bytearr));

        if (location instanceof OMRaster) {
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
        }
    }

    /**
     * Create a ByteRasterLocation at a screen x/y location.
     * 
     * @param latitude latitide in decimal degrees
     * @param longitude longitude in decimal degrees.
     * @param xOffset horizontal pixel screen location from the longitude map
     *        point.
     * @param yOffset vertical pixel screen location, from the latitide map
     *        point.
     * @param name the label for the location.
     * @param bytearr a byte array from an image file that an ImageIcon can use
     *        to create an Image icon. Can also be the binary contents of an
     *        image from a database query.
     */
    public ByteRasterLocation(double latitude, double longitude, int xOffset, int yOffset, String name, byte bytearr[]) {
        super(latitude, longitude, xOffset, yOffset, name, getIconRaster(latitude, longitude, xOffset, yOffset, bytearr));

        if (location instanceof OMRaster) {
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
        }
    }

    /**
     * Create an OMRaster at a latitude/longitude, from a image byte array.
     * 
     * @param lat latitide in decimal degrees
     * @param lon longitude in decimal degrees.
     * @param bytearr a byte array from an image file that an ImageIcon can use
     *        to create an Image icon. Can also be the binary contents of an
     *        image from a database query.
     */
    public static OMRaster getIconRaster(double lat, double lon, byte bytearr[]) {

        ImageIcon icon = getIconRaster(bytearr);
        if (icon == null)
            return null;

        int offX = icon.getIconWidth() / 2;
        int offY = icon.getIconHeight() / 2;
        return new OMRaster(lat, lon, -offX, -offY, icon);
    }

    /**
     * Create a x/y OMRaster with an image byte array.
     * 
     * @param x horizontal pixel screen location from the the left side of the
     *        map.
     * @param y vertical pixel screen location, from the top of the map.
     * @param bytearr a byte array from an image file that an ImageIcon can use
     *        to create an Image icon. Can also be the binary contents of an
     *        image from a database query.
     */
    public static OMRaster getIconRaster(int x, int y, byte bytearr[]) {
        ImageIcon icon = getIconRaster(bytearr);
        if (icon == null)
            return null;

        int offX = icon.getIconWidth() / 2;
        int offY = icon.getIconHeight() / 2;
        return new OMRaster(x - offX, y - offY, icon);
    }

    /**
     * Create a lat/lon OMRaster, pffset to a certain pixel location, with an
     * image byte array.
     * 
     * @param lat latitide in decimal degrees
     * @param lon longitude in decimal degrees.
     * @param x horizontal pixel screen location from the longitude map point.
     * @param y vertical pixel screen location, from the latitide map point.
     * @param bytearr a byte array from an image file that an ImageIcon can use
     *        to create an Image icon. Can also be the binary contents of an
     *        image from a database query.
     */
    public static OMRaster getIconRaster(double lat, double lon, int x, int y, byte bytearr[]) {
        ImageIcon icon = getIconRaster(bytearr);
        if (icon == null)
            return null;

        int offX = icon.getIconWidth() / 2;
        int offY = icon.getIconHeight() / 2;
        return new OMRaster(lat, lon, x - offX, y - offY, icon);
    }

    /**
     * Create an ImageIcon from a byte array. The byte array should reflect the
     * contents of a standard image file.
     */
    public static ImageIcon getIconRaster(byte bytearr[]) {
        if (bytearr == null)
            return null;
        ImageIcon icon = new ImageIcon(bytearr);
        return icon;
    }

    /**
     * Given the label is this location has a height and width, find a clean
     * place on the map for it. Assumes label is not null.
     * 
     * @param declutter the DeclutterMatrix for the map.
     */
    protected void declutterLabel(DeclutterMatrix declutter, Projection proj) {

        super.declutterLabel(declutter, proj);

        if (isShowLocation()) {
            // Take up space with the label
            if (location instanceof OMRasterObject) {
                Point lp = ((OMRasterObject) location).getMapLocation();
                // This location is the upper left location of the
                // declutter matrix. The declutter matrix works from
                // lower left to upper right.
                if (lp != null) {
                    int locHeight = ((OMRasterObject) location).getHeight();
                    int locWidth = ((OMRasterObject) location).getWidth();
                    // Need to get this right for the DeclutterMatrix
                    // space, but changing lp changes where the
                    // location will appear - fix this later.
                    lp.y += locHeight;
                    declutter.setTaken(lp, locWidth, locHeight);
                    // Reset it to the original projected location.
                    lp.y -= locHeight;
                }
            }
        }
    }

    /**
     * Given a new latitude/longitude, reposition the graphic and label.
     */
    public void setGraphicLocations(double latitude, double longitude) {
        if (location instanceof OMRaster) {
            OMRaster ras = (OMRaster) location;
            ras.setLat(latitude);
            ras.setLon(longitude);

            label.setLat(latitude);
            label.setLon(longitude);
            setHorizontalLabelBuffer((ras.getWidth() / 2) + SPACING);

        }
    }

    /**
     * Given a new x/y screen location, reposition the graphic and label.
     */
    public void setGraphicLocations(int x, int y) {
        if (location instanceof OMRaster) {
            OMRaster ras = (OMRaster) location;
            ras.setX(x);
            ras.setY(y);

            label.setX(x);
            label.setY(y);
            setHorizontalLabelBuffer((ras.getWidth() / 2) + SPACING);
        }
    }

    /**
     * Given a new latitude/longitude with x/y offset points, reposition the
     * graphic and label.
     */
    public void setGraphicLocations(double latitude, double longitude, int offsetX, int offsetY) {
        if (location instanceof OMRaster) {
            OMRaster ras = (OMRaster) location;
            ras.setLat(latitude);
            ras.setLon(longitude);
            ras.setX(offsetX);
            ras.setY(offsetY);

            label.setLat(latitude);
            label.setLon(longitude);
            label.setX(offsetX);
            label.setY(offsetY);
            setHorizontalLabelBuffer((ras.getWidth() / 2) + SPACING);
        }
    }
}