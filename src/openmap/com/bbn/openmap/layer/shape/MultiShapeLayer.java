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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/MultiShapeLayer.java,v $
// $RCSfile: MultiShapeLayer.java,v $
// $Revision: 1.3 $
// $Date: 2003/03/21 22:39:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.shape;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.*;
import com.bbn.openmap.util.SwingWorker;

/**
 * An OpenMap Layer that displays multiple shape files.  Note that the
 * ESRIRecords have been updated so that the OMGraphics that get
 * created from them are loaded with an Integer object that notes the
 * number of the record as it was read from the .shp file.  This lets
 * you align the object with the correct attribute data in the .dbf
 * file.
 *
 * <p>
 * <code><pre>
 * ############################
 * # Properties for a multiple shape file layer
 * shapeLayer.class=com.bbn.openmap.layer.shape.MultiShapeLayer
 * shapeLayer.prettyName=Name_for_Menu
 * shapeLayer.shapeFileList=marker_name1 marker_name2 ...
 *
 * shapeLayer.marker_name1.shapeFile=&ltpath to shapefile (.shp)&gt
 * shapeLayer.marker_name1.spatialIndex=&ltpath to generated spatial index file (.ssx)&gt
 * shapeLayer.marker_name1.lineColor=ff000000
 * shapeLayer.marker_name1.fillColor=ff000000
 * # plus any other properties used by the DrawingAttributes object.
 * shapeLayer.marker_name1.pointImageURL=&ltURL for image to use for point objects&gt
 * shapeLayer.marker_name1.enabled=true/false
 *
 * shapeLayer.marker_name2.shapeFile=&ltpath to shapefile (.shp)&gt
 * shapeLayer.marker_name2.spatialIndex=&ltpath to generated spatial index file (.ssx)&gt
 * shapeLayer.marker_name2.lineColor=ff000000
 * shapeLayer.marker_name2.fillColor=ff000000
 * # plus any other properties used by the DrawingAttributes object.
 * shapeLayer.marker_name2.pointImageURL=&ltURL for image to use for point objects&gt
 * shapeLayer.marker_name2.enabled=true/false
 * ############################
 * </pre></code>
 *
 * @version $Revision: 1.3 $ $Date: 2003/03/21 22:39:13 $
 * @see SpatialIndex 
 */
public class MultiShapeLayer extends ShapeLayer {

    public final static String ShapeFileListProperty = "shapeFileList";
    protected Collection spatialIndexes;

    /**
     * Initializes an empty shape layer.
     */
    public MultiShapeLayer() {}

    public void setSpatialIndexes(Collection siv) {
	spatialIndexes = siv;
    }

    public Collection getSpatialIndexes() {
	return spatialIndexes;
    }

    /**
     * Initializes this layer from the given properties.
     *
     * @param props the <code>Properties</code> holding settings for
     * this layer 
     */
    public void setProperties(String prefix, Properties props) {
	// super.setProperties(prefix, props);
	setPropertyPrefix(prefix);

	String realPrefix = PropUtils.getScopedPropertyPrefix(this);

	/// From Layer.java

	String prettyName = realPrefix + PrettyNameProperty;
	
	String defaultName = getName(); 
	if (defaultName == null) {
	    defaultName = "Anonymous";
	}

	setName(props.getProperty(prettyName, defaultName));

	setAddToBeanContext(com.bbn.openmap.layer.util.LayerUtils.booleanFromProperties(props, realPrefix + AddToBeanContextProperty, addToBeanContext));

	autoPalette = com.bbn.openmap.layer.util.LayerUtils.booleanFromProperties(props, realPrefix + AutoPaletteProperty, autoPalette);

	/// end from Layer.java

	setSpatialIndexes(realPrefix, props);

	shadowX = LayerUtils.intFromProperties(props, realPrefix + shadowXProperty, 0);
	shadowY = LayerUtils.intFromProperties(props, realPrefix + shadowYProperty, 0);
    }

    /**
     */
    protected void setSpatialIndexes(String prefix, Properties p) {
	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	String listValue = p.getProperty(prefix + ShapeFileListProperty);

	if (Debug.debugging("shape")){
	    Debug.output(getName() + "| list = \"" + listValue + "\"");
	}

	if (listValue == null) {
	    Debug.error("No property \"" + prefix + 
			ShapeFileListProperty +
			"\" found in application properties.");
	    return;
	}

	// Divide up the names ...
	StringTokenizer tokens = new StringTokenizer(listValue, " ");
	Collection shapeFiles = new Vector();
	while(tokens.hasMoreTokens()) {
	    shapeFiles.add(tokens.nextToken());
	}

	spatialIndexes = new Vector(shapeFiles.size());
	Iterator list = shapeFiles.iterator();

	while (list.hasNext()) {
	    String listName = (String)list.next();
	    SpatialIndexHandler sih = 
		new SpatialIndexHandler(prefix + listName, p);
	    spatialIndexes.add(sih);

	    if (Debug.debugging("shape")) {
		Debug.output("MultiShapeLayer adding: "+ sih);
	    }
	}
    }

    /**
     * PropertyConsumer method.
     */    
    public Properties getProperties(Properties props) {
	props = super.getProperties(props);

	String prefix = PropUtils.getScopedPropertyPrefix(this);

	props.remove(prefix + shapeFileProperty);
	props.remove(prefix + spatialIndexProperty);
	props.remove(prefix + pointImageURLProperty);

	Iterator sis = spatialIndexes.iterator();
	StringBuffer list = new StringBuffer();
	while (sis.hasNext()) {
	    SpatialIndexHandler sih = (SpatialIndexHandler)sis.next();
	    sih.getProperties(props);
	    String pp = sih.getPropertyPrefix();
	    // Can't be null, if they are part of this layer...
	    pp = pp.substring(pp.lastIndexOf('.') + 1);
	    list.append(" " + pp);
	}

	props.put(prefix + ShapeFileListProperty, list.toString());
	return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.  
     */
    public Properties getPropertyInfo(Properties props) {
	props = super.getPropertyInfo(props);
	props.remove(shapeFileProperty);
	props.remove(spatialIndexProperty);
	props.remove(pointImageURLProperty);
	props.remove(shapeFileProperty + ScopedEditorProperty);
	props.remove(spatialIndexProperty + ScopedEditorProperty);
	props.remove(pointImageURLProperty + ScopedEditorProperty);

	Iterator sis = spatialIndexes.iterator();
	while (sis.hasNext()) {
	    ((SpatialIndexHandler)sis.next()).getPropertyInfo(props);
	}

	props.put(ShapeFileListProperty, "List of marker names for SpatialIndexHandlers");

	return props;
    }

    /**
     * Creates an OMGraphicList containing graphics from all
     * SpatialIndex objects and shapefiles.
     * @return OMGraphicList containing an OMGraphicList containing
     * shapes from a particular shape file.
     */
    public OMGraphicList prepare() {

	if (spatialIndexes == null || spatialIndexes.size() == 0) {
	    Debug.message("shape", "MultiShapeLayer: spatialIndexes is empty!");
	    return new OMGraphicList();
	}

	Projection projection = getProjection();

	LatLonPoint ul = projection.getUpperLeft();
	LatLonPoint lr = projection.getLowerRight();
	float ulLat = ul.getLatitude();
	float ulLon = ul.getLongitude();
	float lrLat = lr.getLatitude();
	float lrLon = lr.getLongitude();

	OMGraphicList masterList = new OMGraphicList();
	OMGraphicList list = null;
	SpatialIndexHandler sih;
	Iterator sii;

	// check for dateline anomaly on the screen.  we check for
	// ulLon >= lrLon, but we need to be careful of the check for
	// equality because of floating point arguments...
	if ((ulLon > lrLon) ||
		MoreMath.approximately_equal(ulLon, lrLon, .001f))
	{
	    if (Debug.debugging("shape")) {
		Debug.output("MultiShapeLayer.computeGraphics(): Dateline is on screen");
	    }
	    
	    double ymin = (double) Math.min(ulLat, lrLat);
	    double ymax = (double) Math.max(ulLat, lrLat);
	    
	    sii = spatialIndexes.iterator();
	    while (sii.hasNext()) {
		sih = (SpatialIndexHandler)sii.next();
		if (!sih.enabled) continue;
		
		try {

		    list = sih.getGraphics(ulLon, ymin, 180.0d, ymax, list);
		    list = sih.getGraphics(-180.0d, ymin, lrLon, ymax, list);
		} catch (java.io.IOException ex) {
		    ex.printStackTrace();
		} catch (FormatException fe) {
		    fe.printStackTrace();
		}
		masterList.add(list);
	    }
	} else {

	    double xmin = (double) Math.min(ulLon, lrLon);
	    double xmax = (double) Math.max(ulLon, lrLon);
	    double ymin = (double) Math.min(ulLat, lrLat);
	    double ymax = (double) Math.max(ulLat, lrLat);

	    sii = spatialIndexes.iterator();
	    while (sii.hasNext()) {
		sih = (SpatialIndexHandler)sii.next();

		if (!sih.enabled) continue;

		if (Debug.debugging("shape")) {
		    Debug.output("  Getting graphics from " + sih.prettyName + " spatial index");
		}
		try {
		    list = sih.getGraphics(xmin, ymin, xmax, ymax, list);
		} catch (java.io.IOException ex) {
		    ex.printStackTrace();
		} catch (FormatException fe) {
		    fe.printStackTrace();
		}
		masterList.add(list);
	    }
	}

	if (masterList != null) {
	    masterList.generate(projection, true);//all new graphics
	}
	return masterList;
    }

    public Component getGUI() {
	if (box == null) {

	    box = new JPanel();
	    JTabbedPane tabs = new JTabbedPane();

	    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
  	    box.setAlignmentX(Component.LEFT_ALIGNMENT);

	    Iterator sii = spatialIndexes.iterator();
	    while (sii.hasNext()) {
		SpatialIndexHandler sih = (SpatialIndexHandler)sii.next();
		JPanel stuff = (JPanel)sih.getGUI();
		if (stuff != null) {
		    tabs.addTab(sih.getPrettyName(), stuff);
		}
	    }

	    box.add(tabs);

	    JPanel pal2 = new JPanel();
	    JButton redraw = new JButton("Redraw Layer");
	    redraw.setActionCommand(RedrawCmd);
	    redraw.addActionListener(this);
	    pal2.add(redraw);
	    box.add(pal2);
	}
	return box;
    }
    
    /**
     * DataBoundsInformer interface.
     */
    public DataBounds getDataBounds() {
	DataBounds box = null;

	ESRIBoundingBox bounds = new ESRIBoundingBox();	
	Iterator sii = spatialIndexes.iterator();
	while (sii.hasNext()) {
	    SpatialIndex si = (SpatialIndex)sii.next();
	    if (si != null) {
		ESRIBoundingBox boundingBox = spatialIndex.getBounds();
		if (bounds != null) {
		    bounds.addBounds(boundingBox);
		}
	    }
	}
	return box;
    }

}
