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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/URLRasterLocation.java,v $
// $RCSfile: URLRasterLocation.java,v $
// $Revision: 1.9 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.net.URL;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.util.PropUtils;

/**
 * A Location that takes an URL for an image and creates a Raster for a Location
 * marker. The URL should be the contents of an image file (gif, jpeg) that an
 * ImageIcon object can use to create an Image object. The string for an icon
 * can be a path to a resource, file or URL, and the URLRasterLocation will
 * convert it to a URL.
 */
public class URLRasterLocation
        extends Location {
    /** The spacing between the label and the outside of the image. */
    public int SPACING = 0;

    /**
     * A constructor to enable creation of subclasses.
     */
    public URLRasterLocation() {
    }

    /**
     * Create a URLRasterLocation at a latitude/longitude location.
     * 
     * @param latitude latitide in decimal degrees
     * @param longitude longitude in decimal degrees.
     * @param name the label for the location.
     * @param iconURL a string to a URL for an image
     */
    public URLRasterLocation(double latitude, double longitude, String name, String iconURL) {
        super(latitude, longitude, name, getIconRaster(latitude, longitude, iconURL));

        if (location instanceof OMRaster) {
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
        }

    }

    /**
     * Create a URLRasterLocation at a latitude/longitude location.
     * 
     * @param latitude latitide in decimal degrees
     * @param longitude longitude in decimal degrees.
     * @param name the label for the location.
     * @param iconURL a URL for an image
     */
    public URLRasterLocation(double latitude, double longitude, String name, URL iconURL) {
        super(latitude, longitude, name, getIconRaster(latitude, longitude, iconURL));

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
     * @param iconURL a String for a URL for an image
     */
    public URLRasterLocation(int x, int y, String name, String iconURL) {
        super(x, y, name, getIconRaster(x, y, iconURL));

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
     * @param iconURL a URL for an image
     */
    public URLRasterLocation(int x, int y, String name, URL iconURL) {
        super(x, y, name, getIconRaster(x, y, iconURL));

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
     * @param iconURL a String for a URL for an image
     */
    public URLRasterLocation(double latitude, double longitude, int xOffset, int yOffset, String name, String iconURL) {
        super(latitude, longitude, xOffset, yOffset, name, getIconRaster(latitude, longitude, xOffset, yOffset, iconURL));

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
     * @param iconURL a URL for an image
     */
    public URLRasterLocation(double latitude, double longitude, int xOffset, int yOffset, String name, URL iconURL) {
        super(latitude, longitude, xOffset, yOffset, name, getIconRaster(latitude, longitude, xOffset, yOffset, iconURL));

        if (location instanceof OMRaster) {
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
        }
    }

    /**
     * Create an OMRaster at a latitude/longitude, from a image URL.
     * 
     * @param lat latitide in decimal degrees
     * @param lon longitude in decimal degrees.
     * @param iconURL a URL for an image
     */
    public static OMRaster getIconRaster(double lat, double lon, String iconURL) {
        URL url = getIconRasterURL(iconURL);
        if (url == null)
            return null;

        return getIconRaster(lat, lon, url);
    }

    /**
     * Create an OMRaster at a latitude/longitude, from a image URL.
     * 
     * @param lat latitide in decimal degrees
     * @param lon longitude in decimal degrees.
     * @param iconURL a URL for an image
     */
    public static OMRaster getIconRaster(double lat, double lon, URL iconURL) {

        ImageIcon icon = new ImageIcon(iconURL);
        if (icon == null)
            return null;

        int offX = icon.getIconWidth() / 2;
        int offY = icon.getIconHeight() / 2;
        return new OMRaster(lat, lon, -offX, -offY, icon);
    }

    /**
     * Create an OMRaster at a latitude/longitude, from a image URL.
     * 
     * @param x horizontal pixel screen location from the the left side of the
     *        map.
     * @param y vertical pixel screen location, from the top of the map.
     * @param iconURL a String for a URL for an image
     */
    public static OMRaster getIconRaster(int x, int y, String iconURL) {
        URL url = getIconRasterURL(iconURL);
        if (url == null)
            return null;

        return getIconRaster(x, y, url);
    }

    /**
     * Create an OMRaster at a latitude/longitude, from a image URL.
     * 
     * @param x horizontal pixel screen location from the the left side of the
     *        map.
     * @param y vertical pixel screen location, from the top of the map.
     * @param iconURL a URL for an image
     */
    public static OMRaster getIconRaster(int x, int y, URL iconURL) {
        ImageIcon icon = new ImageIcon(iconURL);
        if (icon == null)
            return null;

        int offX = icon.getIconWidth() / 2;
        int offY = icon.getIconHeight() / 2;
        return new OMRaster(x - offX, y - offY, icon);
    }

    /**
     * Create an OMRaster at a latitude/longitude, from a image URL.
     * 
     * @param lat latitide in decimal degrees
     * @param lon longitude in decimal degrees.
     * @param x horizontal pixel screen location from the longitude map point.
     * @param y vertical pixel screen location, from the latitide map point.
     * @param iconURL a String for URL for an image
     */
    public static OMRaster getIconRaster(double lat, double lon, int x, int y, String iconURL) {
        URL url = getIconRasterURL(iconURL);
        if (url == null)
            return null;

        return getIconRaster(lat, lon, x, y, url);
    }

    /**
     * Create an OMRaster at a latitude/longitude, from a image URL.
     * 
     * @param lat latitide in decimal degrees
     * @param lon longitude in decimal degrees.
     * @param x horizontal pixel screen location from the longitude map point.
     * @param y vertical pixel screen location, from the latitide map point.
     * @param iconURL a URL for an image
     */
    public static OMRaster getIconRaster(double lat, double lon, int x, int y, URL iconURL) {
        ImageIcon icon = new ImageIcon(iconURL);
        if (icon == null)
            return null;

        int offX = icon.getIconWidth() / 2;
        int offY = icon.getIconHeight() / 2;
        return new OMRaster(lat, lon, x - offX, y - offY, icon);
    }

    /**
     * Create an ImageIcon from a String to an image URL.
     * 
     * @param iconURL can be a path to a resource, file or URL.
     */
    public static URL getIconRasterURL(String iconURL) {
        try {
            return PropUtils.getResourceOrFileOrURL(null, iconURL);
        } catch (java.net.MalformedURLException mue) {
            throw new com.bbn.openmap.util.HandleError(mue);
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
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
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
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
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
            setHorizontalLabelBuffer((((OMRaster) location).getWidth() / 2) + SPACING);
        }
    }
}