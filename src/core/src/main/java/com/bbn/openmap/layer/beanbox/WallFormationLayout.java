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

import java.util.Vector;

import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.tools.beanbox.BeanLayoutManager;
import com.bbn.openmap.util.Debug;

/**
 * A layout manager for laying out SimpleBeanbObject beans in a wall
 * formation. The WallFormationLayout object is itself a bean.
 */
public class WallFormationLayout extends BeanLayoutManager {

    private float separationInNM;
    private float bearingInDeg;

    public WallFormationLayout() {
        this(35, 90, null);
    }

    public WallFormationLayout(float sep, float bearing, SimpleBeanContainer c) {
        this.separationInNM = sep;
        this.bearingInDeg = bearing;
        super._container = c;
    }

    public float getSeparationInNM() {
        return separationInNM;
    }

    public void setSeparationInNM(float sep) {
        separationInNM = sep;
        layoutContainer();
    }

    public float getBearingInDeg() {
        return bearingInDeg;
    }

    public void setBearingInDeg(float bearing) {

        if (bearing < 0)
            bearing = 360 - bearing;

        bearingInDeg = bearing % 360;

        layoutContainer();
    }

    /**
     * Called by the SimpleBeanContainer to layout its contents.
     */
    public void layoutContainer() {

        //System.out.println("Called> layoutContainer");

        if (super._container == null)
            return;

        SimpleBeanContainer container = (SimpleBeanContainer) _container;

        Vector contents = super._container.getContents();

        if (contents == null || contents.isEmpty())
            return;

        LatLonPoint midllp = new LatLonPoint.Float(container.getLatitude(), container.getLongitude());

        LatLonPoint[] llps = new LatLonPoint[contents.size()];

        if (contents.size() == 1)
            llps[0] = midllp;
        else {
            float angle1Deg = bearingInDeg - 90;
            float angle1Rad = (float) Math.toRadians(angle1Deg);
            float angle2Deg = bearingInDeg + 90;
            float angle2Rad = (float) Math.toRadians(angle2Deg);
            int numBeans = contents.size();
            float spanNM = (numBeans - 1) * separationInNM;
            LatLonPoint cornerllp1 = midllp.getPoint(
                    Length.NM.toRadians(spanNM / 2),
                    angle1Rad);
            llps[0] = cornerllp1;

            for (int i = 1; i < contents.size(); i++)
                llps[i] = GreatCircle.sphericalBetween((float)cornerllp1.getRadLat(),
                        (float)cornerllp1.getRadLon(),
                        Length.NM.toRadians(separationInNM) * i,
                        angle2Rad);
        }

        for (int i = 0; i < contents.size(); i++) {

            Long id = (Long) contents.get(i);
            Debug.error("Here in WallFormationLayout, trying to get layer SimpleBeanObjects");
            /*SimpleBeanObject obj = SimpleBeanLayer.getLayer().getObject(id);

            if (obj == null)
                continue;

            obj.setLatitude(llps[i].getLatitude());
            obj.setLongitude(llps[i].getLongitude());

            obj.setBearingInDeg(bearingInDeg);
            */

        }

        // SimpleBeanLayer.getLayer().updateGraphics();
    }

}