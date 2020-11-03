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
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * A simple layer that lets you define point group sets to be drawn on the map
 * by defining coordinates for points in the properties file. It's similar to
 * the AOILayer except the coordinates are drawn as a set of points, and are not
 * connected. Each group of points share rendering properties. A sample of
 * properties that could be used for this layer:
 * 
 * <pre>
 * 
 * pgl.class=com.bbn.openmap.layer.PointGroupLayer
 * pgl.prettyName=Points of Interest
 * pgl.goi=goi1 goi2
 * pgl.goi1.name=First Grouping
 * pgl.goi1.coords=33.469604f 69.852425f 33.591957f 69.85425f 33.598362f 69.965256f 33.474995f 69.96891f 33.469604f 69.852425f
 * pgl.goi1.lineColor=FF9900
 * pgl.goi1.selectColor=FF9900
 * pgl.goi1.fillColor=33FF9900
 * pgl.goi1.lineWidth=2
 * pgl.goi1.pointOval=false
 * pgl.goi1.pointRadius=3
 * pgl.goi2.name=Second Grouping
 * pgl.goi2.coords=34.59030485181645f 70.10225955962484f 34.70749132408063f 70.10062341994104f 34.705166929775665f 70.24468896438881f 34.58780191583231f 70.24351387509675f 34.59030485181645f 70.10225955962484f
 * pgl.goi2.lineColor=CCFF00
 * pgl.goi2.selectColor=CCFF00
 * pgl.goi2.fillColor=33CCFF00
 * pgl.goi2.lineWidth=2
 * pgl.goi2.pointOval=true
 * pgl.goi2.pointRadius=5
 * 
 * </pre>
 * 
 * @author dietrick
 */
public class PointGroupLayer extends OMGraphicHandlerLayer {

	public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.PointGroupLayer");

	public final static String GOIProperty = "goi";
	public final static String GOICoordsProperty = "coords";
	public final static String GOINameProperty = "name";

	public PointGroupLayer() {
		setMouseModeIDsForEvents(new String[] { "Gestures" });
	}

	public String getToolTipTextFor(OMGraphic omg) {
		return (String) omg.getAttribute(OMGraphic.TOOLTIP);
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		DrawingAttributes attributes = new DrawingAttributes();
		prefix = PropUtils.getScopedPropertyPrefix(prefix);

		List<String> aois = PropUtils.parseSpacedMarkers(props.getProperty(prefix + GOIProperty));

		OMGraphicList list = new OMGraphicList();

		for (Iterator<String> it = aois.iterator(); it.hasNext();) {
			String aoi = it.next();

			String aoiPrefix = PropUtils.getScopedPropertyPrefix(prefix + aoi);
			List<String> coordV = PropUtils.parseSpacedMarkers(props.getProperty(aoiPrefix + GOICoordsProperty));

			attributes.setProperties(aoiPrefix, props);
			String name = props.getProperty(aoiPrefix + GOINameProperty);

			OMGraphicList groupList = new OMGraphicList();

			for (Iterator<String> cit = coordV.iterator(); cit.hasNext();) {
				try {
					double lat = Double.parseDouble(cit.next());
					double lon = Double.parseDouble(cit.next());

					OMPoint omp = new OMPoint(lat, lon);
					attributes.setTo(omp);
					omp.putAttribute(OMGraphic.TOOLTIP, "Location: " + lat + ", " + lon);
					groupList.add(omp);

				} catch (NumberFormatException nfe) {
					logger.warning("can't parse coords for " + aoi + ": " + coordV);
					break;
				}
			}

			groupList.putAttribute(OMGraphic.TOOLTIP, name);

			list.add(groupList);
		}

		setList(list);
	}

}
