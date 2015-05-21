package com.bbn.openmap.layer.geojson;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoly;

public class MultiLineString extends Geometry<List<LngLatAlt>> {

	public MultiLineString() {
	}

	public MultiLineString(List<LngLatAlt> line) {
		add(line);
	}

    public MultiLineString(OMGraphicList omgl) {
        for (OMGraphic omg : omgl) {
            if (omg instanceof OMLine) {
                List<LngLatAlt> llaList = new ArrayList<LngLatAlt>();
                double[] ll = ((OMLine) omg).getLL();
                llaList.add(new LngLatAlt(ll[1], ll[0]));
                llaList.add(new LngLatAlt(ll[3], ll[2]));
                add(llaList);
            } else if (omg instanceof OMPoly) {
                List<LngLatAlt> llaList = new ArrayList<LngLatAlt>();
                for (LngLatAlt lla : OMGeoJSONUtil.convertToJSON(((OMPoly) omg).getRawllpts(), true)) {
                    llaList.add(lla);
                }
                add(llaList);
            }
        }
    }

    public OMGraphic convert() {
        OMGraphicList ret = new OMGraphicList();
        ret.setVague(true);
        for (List<LngLatAlt> line : getCoordinates()) {
            OMPoly poly = new OMPoly(OMGeoJSONUtil.convertToRadians(line), OMGraphic.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE);
            ret.add(poly);
        }
        ret.getAttributes().putAll(getProperties());
        return ret;
    }
}
