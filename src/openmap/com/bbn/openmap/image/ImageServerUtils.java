// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageServerUtils.java,v $
// $RCSfile: ImageServerUtils.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.image;

import java.util.Properties;

import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;

/**
 * A class to contain convenience functions for parsing web image
 * requests.
 */
public class ImageServerUtils implements ImageServerConstants {

    /**
     * Create an OpenMap projection from the values stored in a
     * Properties object.  The properties inside should be parsed out
     * from a map request, with the keywords being those defined in
     * the ImageServerConstants interface. 
     */
    protected static Proj createOMProjection(Properties props, 
					     Projection defaultProj) {

	float scale = LayerUtils.floatFromProperties(props, SCALE, 
						     defaultProj.getScale());
	int height = LayerUtils.intFromProperties(props, HEIGHT, 
						  defaultProj.getHeight());
	int width = LayerUtils.intFromProperties(props, WIDTH, 
						 defaultProj.getWidth());
	com.bbn.openmap.LatLonPoint llp = defaultProj.getCenter();
	float longitude = LayerUtils.floatFromProperties(props,  LON, 
							 llp.getLongitude());
	float latitude = LayerUtils.floatFromProperties(props, LAT, 
							llp.getLatitude());
	String projType = props.getProperty(PROJTYPE);
	int projID;
	if (projType == null) {
	    projID = defaultProj.getProjectionType();
	} else {
	    projID = ProjectionFactory.getProjType(projType);
	}

	Debug.output("SHIS: projection of type " + projType + 
		     ", with HEIGHT = " + height + 
		     ", WIDTH = " + width + 
		     ", lat = " + latitude + 
		     ", lon = " + longitude);

	Proj proj = (Proj) ProjectionFactory.makeProjection(
	    projID, latitude, longitude, scale, width, height);
	

	boolean transparent = LayerUtils.booleanFromProperties(props,
							       TRANSPARENT,
							       false);

	java.awt.Color backgroundColor = LayerUtils.parseColorFromProperties(props,
								    BGCOLOR,
								    "FFFFFF");
	if (transparent) {
	    backgroundColor = new java.awt.Color(backgroundColor.getRed(),
						 backgroundColor.getGreen(),
						 backgroundColor.getBlue(),
						 0x00);
	}

	proj.setBackgroundColor(backgroundColor);

	return (Proj) proj;
    }
    
}
