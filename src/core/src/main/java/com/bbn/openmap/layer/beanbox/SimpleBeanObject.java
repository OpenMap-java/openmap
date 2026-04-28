/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.layer.beanbox;

import java.awt.Image;

/**
 * A simple bean object. Contains a unique long id, a lat, lon
 * position an orientation measured in degrees clockwise from the +ve
 * Y axis and an optional graphic image or custom graphics class name.
 */
public class SimpleBeanObject {

    protected long id;
    protected float latitude;
    protected float longitude;
    protected float bearingInDeg;

    protected Image graphicImage;

    protected String customGraphicClassName;

    public SimpleBeanObject() {
        this(System.currentTimeMillis(), 0, 0, 0);
    }

    public SimpleBeanObject(long id, float latitude, float longitude,
            float bearingInDeg) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearingInDeg = bearingInDeg;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setLatitude(float lat) {
        this.latitude = lat;
    }

    public float getLatitude() {
        return this.latitude;
    }

    public void setLongitude(float lon) {
        this.longitude = lon;
    }

    public float getLongitude() {
        return this.longitude;
    }

    public float getBearingInDeg() {
        return bearingInDeg;
    }

    public void setBearingInDeg(float bearingInDeg) {
        this.bearingInDeg = bearingInDeg;
    }

    public Image getGraphicImage() {
        return graphicImage;
    }

    public void setGraphicImage(Image graphicImage) {
        this.graphicImage = graphicImage;
    }

    public String getCustomGraphicClassName() {
        return customGraphicClassName;
    }

    public void setCustomGraphicClassName(String className) {
        this.customGraphicClassName = className;
    }

    public String toString() {
        return "[SBO " + id + " " + latitude + " " + longitude + " "
                + bearingInDeg + " " + customGraphicClassName + " "
                + graphicImage + "]";
    }
}