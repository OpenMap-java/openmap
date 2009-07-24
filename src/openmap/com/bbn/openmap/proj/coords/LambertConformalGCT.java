package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.proj.LambertConformal;
import com.bbn.openmap.proj.LambertConformalLoader;

public class LambertConformalGCT extends AbstractGCT {

    private LambertConformal lcc = null;

    public LambertConformalGCT(LambertConformal lcc) {
        this.lcc = lcc;
    }

    public LambertConformalGCT(Properties props) {
        LambertConformalLoader loader = new LambertConformalLoader();
        lcc = (LambertConformal) loader.create(props);
    }

    public synchronized Point2D forward(double lat, double lon, Point2D ret) {
        return lcc.LLToWorld(lat, lon, ret);
    }

    public synchronized LatLonPoint inverse(double x, double y, LatLonPoint ret) {
        return (LatLonPoint) lcc.worldToLL(x, y, ret);
    }

}
