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

/**
 * A SimpleBeanObject that represents a Fighter. Contains two fighter
 * specific fields: type (eg. F16, M21 etc) and speedInNMPerHr.
 */
public class Fighter extends SimpleBeanObject {

    protected String type;

    protected float speedInNMPerHr;

    public Fighter() {
        super();
        this.setType("F16");
        this.setSpeedInNMPerHr(1200);
    }

    public Fighter(long id, String type, float lat, float lon,
            float bearingInDeg, float speedInNMPerHr) {
        super(id, lat, lon, bearingInDeg);
        this.setType(type);
        this.setSpeedInNMPerHr(speedInNMPerHr);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public float getSpeedInNMPerHr() {
        return speedInNMPerHr;
    }

    public void setSpeedInNMPerHr(float speedInNMPerHr) {
        this.speedInNMPerHr = speedInNMPerHr;
    }

    public String toString() {
        return "[FIGHTER " + id + " " + latitude + " " + longitude + " "
                + bearingInDeg + " " + customGraphicClassName + " "
                + graphicImage + " " + type + " " + speedInNMPerHr + "]";
    }

}