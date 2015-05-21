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
//$RCSfile: RadialRegion.java,v $
//$Revision: 1.2 $
//$Date: 2007/01/30 20:37:01 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

public class RadialRegion extends BoundingCircle.Impl implements GeoExtent {

    protected Object id = RadialRegion.this;

    public RadialRegion(Geo center, double radius) {
        super(center, radius);
    }
    
    public RadialRegion(Geo[] region) {
        super(region);
    }

    public BoundingCircle getBoundingCircle() {
        return this;
    }

    public Object getID() {
        return id;
    }

    public void setID(Object id) {
        this.id = id;
    }
}
