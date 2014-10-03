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
//$RCSfile: AOILayer.java,v $
//$Revision: 1.1 $
//$Date: 2007/08/16 22:15:27 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer;

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.util.PropUtils;

/**
 * A simple layer that lets you define areas to be drawn on the map by defining
 * coordinates for areas in the properties file. A sample of properties that
 * could be used for this layer:
 * <pre> 
 * 
 * aoi.class=com.bbn.hotwash.map.AOILayer
 * aoi.prettyName=Areas of Interest
 * aoi.aoi=area1 area2
 * aoi.area1.name=First Area
 * aoi.area1.coords=33.469604f 69.852425f 33.591957f 69.85425f 33.598362f 69.965256f 33.474995f 69.96891f 33.469604f 69.852425f
 * aoi.area1.lineColor=FF9900
 * aoi.area1.selectColor=FF9900
 * aoi.area1.fillColor=33FF9900
 * aoi.area1.lineWidth=2
 * aoi.area2.name=Second Area
 * aoi.area2.coords=34.59030485181645f 70.10225955962484f 34.70749132408063f 70.10062341994104f 34.705166929775665f 70.24468896438881f 34.58780191583231f 70.24351387509675f 34.59030485181645f 70.10225955962484f
 * aoi.area2.lineColor=CCFF00
 * aoi.area2.selectColor=CCFF00
 * aoi.area2.fillColor=33CCFF00
 * aoi.area2.lineWidth=2
 * 
 * </pre>
 * @author dietrick
 */
public class AOILayer extends OMGraphicHandlerLayer {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.AOILayer");

    public final static String AOIProperty = "aoi";
    public final static String AOICoordsProperty = "coords";
    public final static String AOINameProperty = "name";

    public AOILayer() {
        setMouseModeIDsForEvents(new String[] { "Gestures" });
    }

    public String getToolTipTextFor(OMGraphic omg) {
        return (String) omg.getAttribute(OMGraphic.TOOLTIP);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        DrawingAttributes attributes = new DrawingAttributes();
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        Vector<String> aois = PropUtils.parseSpacedMarkers(props.getProperty(prefix
                + AOIProperty));

        OMGraphicList list = new OMGraphicList();

        for (Iterator<String> it = aois.iterator(); it.hasNext();) {
            String aoi = it.next();

            String aoiPrefix = PropUtils.getScopedPropertyPrefix(prefix + aoi);
            Vector<String> coordV = PropUtils.parseSpacedMarkers(props.getProperty(aoiPrefix
                    + AOICoordsProperty));
            double[] coords = new double[coordV.size()];
            int coordCount = 0;
            for (Iterator<String> cit = coordV.iterator(); cit.hasNext();) {
                try {
                    coords[coordCount++] = Double.parseDouble(cit.next());
                } catch (NumberFormatException nfe) {
                    logger.warning("can't parse coords for " + aoi + ": "
                            + coordV);
                    break;
                }
            }

            if (coordCount < coordV.size()) {
                continue;
            }

            attributes.setProperties(aoiPrefix, props);
            String name = props.getProperty(aoiPrefix + AOINameProperty);

            OMPoly aoiGraphic = new OMPoly(coords, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE);
            attributes.setTo(aoiGraphic);
            aoiGraphic.putAttribute(OMGraphic.TOOLTIP, name);

            list.add(aoiGraphic);
        }

        setList(list);
    }

}
