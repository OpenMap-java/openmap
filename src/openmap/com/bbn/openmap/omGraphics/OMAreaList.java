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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMAreaList.java,v $
// $RCSfile: OMAreaList.java,v $
// $Revision: 1.3 $
// $Date: 2003/07/12 04:13:26 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.*;
import java.net.*;
import java.util.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.GraphicList;
import com.bbn.openmap.omGraphics.grid.*;
import com.bbn.openmap.util.Debug;

/**
 * This class encapsulates a List of OMGeometries.  It's an
 * OMGraphic, so it contains information on how to draw them.  It's
 * also a subclass to the OMGraphicList, and relies on many
 * OMGraphicList methods.
 *
 * <p> The OMGeometryList assumes that all OMGeometries on it should
 * be rendered the same - same fill color, same edge color and stroke,
 * and will create one java.awt.Shape object from all the projected
 * OMGeometries for more efficient rendering. If your individual
 * OMGeometries have independing rendering characteristics, use the
 * OMGraphicList and OMGraphics.
 *
 * <p> Because the OMGeometryList creates a single java.awt.Shape
 * object for all of its contents, it needs to be generated() if an
 * OMGeometry is added or removed from the list.  If you don't
 * regenerate the OMGeometryList, the list will iterate through its
 * contents and render each piece separately.
 */
public class OMAreaList extends OMGeometryList 
    implements GraphicList, Serializable {

    /**
     * Construct an OMAreaList.
     */
    public OMAreaList() {
	super(10);
	setVague(true);
	setTraverseMode(LAST_ADDED_ON_TOP);
    };
    
    /**
     * Construct an OMGeometryList with an initial capacity. 
     *
     * @param initialCapacity the initial capacity of the list 
     */
    public OMAreaList(int initialCapacity) {
	super(initialCapacity);
	setVague(true);
	setTraverseMode(LAST_ADDED_ON_TOP);
    };

    /**
     * Construct an OMGeometryList around a List of OMGeometries.  The
     * OMGeometryList assumes that all the objects on the list are
     * OMGeometries, and never does checking.  Live with the
     * consequences if you put other stuff in there.
     * @param list List of OMGeometries.
     */
    public OMAreaList(java.util.List list) {
	super(list);
	setVague(true);
	setTraverseMode(LAST_ADDED_ON_TOP);
    }

    /**
     * Create the GeneralPath used for the internal Shape object, but
     * with a twist.  With the OMAreaList, all of the components are
     * combined to make a single area. So updateShape, which is called
     * from super.generate(), calls appendShapeEdge() instead of
     * appending each part Shape to the main Shape.  This method
     * closes off the GeneralPath Shape, so it will be considered a
     * polygon.
     */
    public synchronized void generate(Projection p, boolean forceProjectAll) {
	super.generate(p, forceProjectAll);
	if (shape != null) {
	    shape.closePath();
	}
    }

    /**
     * Given a OMGeometry, it calls generate/regenerate on it, and
     * then adds the GeneralPath shape within it to the OMGeometryList
     * shape object.  The edges of each part are combined to make one
     * big polygon.
     */
    protected void updateShape(OMGeometry geometry, Projection p, 
			       boolean forceProject) {
	if (forceProject) {
	    geometry.generate(p);
	} else {
	    geometry.regenerate(p);
	}
		    
	if (geometry.isVisible()) {
	    GeneralPath gp = (GeneralPath)geometry.getShape();

	    if (gp == null) {
		return;
	    }

	    shape = appendShapeEdge(shape, gp);
	}
    }

}
