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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/SpatialIndexHandler.java,v $
// $RCSfile: SpatialIndexHandler.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.shape;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.*;

/**
 * The SpatialIndexHandler keeps track of all the stuff dealing with a
 * particular shape file - file names, colors, etc.  You can ask it to
 * create OMGraphics based on a bounding box, and make adjustments to
 * it through its GUI.  
 */
public class SpatialIndexHandler implements PropertyConsumer {
    public SpatialIndex spatialIndex;
    public String shapeFileName = null;
    public String spatialIndexFileName = null;
    public String imageURLString = null;

    protected String prettyName = null;
    protected DrawingAttributes drawingAttributes;
    protected boolean enabled = true;
    protected boolean buffered = false;
    protected String propertyPrefix;

    public final static String EnabledProperty = "enabled";
    public final static String BufferedProperty = "buffered";

    public SpatialIndexHandler() {}

    public SpatialIndexHandler(String prefix, Properties props) {
	setProperties(prefix, props);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("For " + prettyName + ":\n");
	sb.append("  Shape file name: " + shapeFileName + "\n");
	sb.append("  Spatal index file name: " + spatialIndexFileName + "\n");
	sb.append("  image URL: " + imageURLString + "\n");
	sb.append("  drawing attributes: " + drawingAttributes + "\n");
	return sb.toString();
    }

    /**
     * Get the GUI that controls the attributes of the handler.
     */
    public JComponent getGUI() {
	JPanel stuff = new JPanel();
	stuff.setBorder(BorderFactory.createRaisedBevelBorder());
	stuff.add(new JLabel(prettyName));
	stuff.add(drawingAttributes.getGUI());

	JPanel checks = new JPanel(new GridLayout(0, 1));
	JCheckBox enableButton = new JCheckBox("Show");
	enableButton.setSelected(enabled);
	enableButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    JCheckBox jcb = (JCheckBox)ae.getSource();
		    enabled = jcb.isSelected();
		}
	    });
	checks.add(enableButton);

	JCheckBox bufferButton = new JCheckBox("Buffer");
	bufferButton.setSelected(buffered);
	bufferButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    JCheckBox jcb = (JCheckBox)ae.getSource();
		    buffered = jcb.isSelected();
		}
	    });
	checks.add(bufferButton);
	stuff.add(checks);

	return stuff;
    }

    /** Property Consumer method. */
    public void setPropertyPrefix(String prefix) {
	propertyPrefix = prefix;
    }

    /** Property Consumer method. */
    public String getPropertyPrefix() {
	return propertyPrefix;
    }

    /** Property Consumer method. */
    public void setProperties(Properties props) {
	setProperties(null, props);
    }

    /** Property Consumer method. */
    public void setProperties(String prefix, Properties props) {
	setPropertyPrefix(prefix);
	String realPrefix = PropUtils.getScopedPropertyPrefix(this);
	prettyName = props.getProperty(realPrefix + Layer.PrettyNameProperty);
	shapeFileName = props.getProperty(realPrefix + ShapeLayer.shapeFileProperty);
	spatialIndexFileName
	    = props.getProperty(realPrefix + ShapeLayer.spatialIndexProperty);
	    
	if (shapeFileName != null && !shapeFileName.equals("")) {
	    if (spatialIndexFileName != null ) {
		spatialIndex = SpatialIndex.locateAndSetShapeData(shapeFileName, spatialIndexFileName);
	    } else {
		spatialIndex = SpatialIndex.locateAndSetShapeData(shapeFileName);
	    }
		
	    imageURLString = props.getProperty(realPrefix + ShapeLayer.pointImageURLProperty);
		
	    try {
		if (imageURLString != null && !imageURLString.equals("")) {
		    URL imageURL = LayerUtils.getResourceOrFileOrURL(this, imageURLString);
		    ImageIcon imageIcon = new ImageIcon(imageURL);
		    spatialIndex.setPointIcon(imageIcon);
		}
	    } catch (MalformedURLException murle) {
		Debug.error("MultiShapeLayer.setProperties(" +
			    realPrefix + 
			    ": point image URL not so good: \n\t" + 
			    imageURLString);

	    } catch (NullPointerException npe) {
		// May happen if not connected to the internet.
		Debug.error("Can't access icon image: \n" + 
			    imageURLString);
	    }
		
	} else {
	    Debug.error(realPrefix + ": One of the following properties was null or empty:");
	    Debug.error("\t" + realPrefix + ShapeLayer.shapeFileProperty);
	    Debug.error("\t" + realPrefix + ShapeLayer.spatialIndexProperty);
	}
	    
	drawingAttributes = new DrawingAttributes(realPrefix, props);

	enabled = LayerUtils.booleanFromProperties(props, realPrefix + EnabledProperty, enabled);
	buffered = LayerUtils.booleanFromProperties(props, realPrefix + BufferedProperty, buffered);
    }

    /** Property Consumer method. */
    public Properties getProperties(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

	String prefix = PropUtils.getScopedPropertyPrefix(this);
	props.put(prefix + ShapeLayer.shapeFileProperty, 
		  (shapeFileName==null?"":shapeFileName));
	props.put(prefix + ShapeLayer.spatialIndexProperty, 
		  (spatialIndexFileName==null?"":spatialIndexFileName));
	props.put(prefix + ShapeLayer.pointImageURLProperty, 
		  (imageURLString==null?"":imageURLString));

	if (drawingAttributes != null) {
	    drawingAttributes.getProperties(props);
	} else {
	    DrawingAttributes da = (DrawingAttributes)DrawingAttributes.DEFAULT.clone();
	    da.setPropertyPrefix(prefix);
	    da.getProperties(props);
	}
	props.put(prefix + EnabledProperty, new Boolean(enabled).toString());
	props.put(prefix + BufferedProperty, new Boolean(buffered).toString());
	return props;
    }

    /** Property Consumer method. */
    public Properties getPropertyInfo(Properties props) {
	if (props == null) {
	    props = new Properties();
	}

	props.put(ShapeLayer.shapeFileProperty, "Location of Shape file - .shp (File, URL or relative file path).");
	props.put(ShapeLayer.shapeFileProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
	props.put(ShapeLayer.spatialIndexProperty, "Location of Spatial Index file - .ssx (File, URL or relative file path).");
	props.put(ShapeLayer.spatialIndexProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

	props.put(ShapeLayer.pointImageURLProperty, "Image file to use for map location of point data (optional).");
	props.put(ShapeLayer.pointImageURLProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

	if (drawingAttributes != null) {
	    drawingAttributes.getPropertyInfo(props);
	} else {
	    DrawingAttributes.DEFAULT.getPropertyInfo(props);
	}
	props.put(EnabledProperty, "Show file contents");
	props.put(EnabledProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

	props.put(BufferedProperty, "Read and hold entire file contents (may be faster)");
	props.put(BufferedProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

	return props;
    }

    /**
     * Create the OMGraphics out of the records that fall inside the
     * bounding box.
     * @param xmin double for the min horizontal limit of the bounding
     * box.
     * @param ymin double for the min vertical limit of the bounding
     * box.
     * @param xmax double for the max horizontal limit of the bounding
     * box.
     * @param ymax double for the max vertical limit of the bounding
     * box.
     */
    public OMGraphicList getGraphics(double xmin, double ymin,
				     double xmax, double ymax) 
	throws IOException, FormatException {
	return getGraphics(xmin, ymin, xmax, ymax, null);
    }


    /**
     * Given a bounding box, create OMGraphics from the ESRI records
     * in the shape file.
     * @param xmin double for the min horizontal limit of the bounding
     * box.
     * @param ymin double for the min vertical limit of the bounding
     * box.
     * @param xmax double for the max horizontal limit of the bounding
     * box.
     * @param ymax double for the max vertical limit of the bounding
     * box.
     * @param list OMGraphic list to add the new OMGraphics too.  If
     * null, a new OMGraphicList will be created.
     * @return OMGraphicList containing the new OMGraphics.  
     */
    public OMGraphicList getGraphics(double xmin, double ymin,
				     double xmax, double ymax,
				     OMGraphicList list) 
	throws IOException, FormatException {

	if (list == null) {
	    list = new OMGraphicList();
	}

	if (!buffered) {

	    // Clean up if buffering turned off.
	    if (masterList != null) {
		masterList = null;
	    }

	    OMGeometryList geometrys = new OMGeometryList();
	    drawingAttributes.setTo(geometrys);
	    list.add(geometrys);
	    
	    ESRIRecord records[] = 
		spatialIndex.locateRecords(xmin, ymin, xmax, ymax);
	    
	    int nRecords = records.length;

	    for (int i = 0; i < nRecords; i++) {
		ESRIRecord rec = records[i];

		OMGeometry geom = records[i].addOMGeometry(geometrys);
		geom.setAppObject(new NumAndBox(rec.getRecordNumber(),
						rec.getBoundingBox()));
	    }

	} else {
	    // grab local refs
	    ESRIPoint min, max;

	    if (masterList == null) {
		getWholePlanet();
	    }

	    drawingAttributes.setTo(masterList);
	    list.add(masterList);
	    
	    Iterator iterator = masterList.iterator();

	    while (iterator.hasNext()) {

		OMGeometry geom = (OMGeometry) iterator.next();
		Object obj = geom.getAppObject();

		// If you can test for bounding box intersections,
		// then use the check to see if you can eliminate the
		// object from being drawn.  Otherwise, just draw it
		// and let Java clip it.
		geom.setVisible(true);
		
		if (obj != null && obj instanceof NumAndBox) {
		    NumAndBox nab = (NumAndBox) obj;
		    min = nab.getBoundingBox().min;
		    max = nab.getBoundingBox().max;
		    
		    if (!SpatialIndex.intersects(
			xmin, ymin, xmax, ymax,
			min.x, min.y, max.x, max.y))
		    {
			geom.setVisible(false);
		    }
		}
	    }
	}

	return list;
    }

    /**
     * Gets the record graphics for a record with multiple graphics.
     * @return OMGraphicList
     */
    protected OMGraphicList RecordList(ESRIRecord rec, DrawingAttributes drawingAttributes) {
	int recNumber = rec.getRecordNumber();
	OMGraphicList recList = new OMGraphicList(10);
	if (drawingAttributes == null) {
	    drawingAttributes = new DrawingAttributes();
	}
	rec.addOMGraphics(recList, drawingAttributes);
	
	// Remember recordNumber to work with .dbf file
	recList.setAppObject(new Integer(recNumber)); 
	return recList;
    }

    /**
     * Master list for buffering.  Only used if buffering is enabled.
     */
    protected OMGeometryList masterList = null;

    /**
     * Get the graphics for the entire planet.
     */
    protected void getWholePlanet() throws FormatException {

	masterList = new OMGeometryList();

	if (Debug.debugging("shape")) {
	    Debug.output(prettyName + "|SpatialIndexHolder.getWholePlanet(): fetching all graphics.");
	}
	try {
	    ESRIRecord records[] = spatialIndex.locateRecords(
		    -180d, -90d, 180d, 90d);
	    int nRecords = records.length;

	    for (int i=0; i < nRecords; i++) {
  	   	OMGeometry geom = records[i].addOMGeometry(masterList);
		geom.setAppObject(new NumAndBox(records[i].getRecordNumber(),
						records[i].getBoundingBox()));
	    }

	} catch (java.io.IOException ex) {
	    ex.printStackTrace();
	    return;
	} catch (java.lang.NullPointerException npe) {
	    Debug.error(prettyName + "|SpatialIndexHolder can't access files.");
	    return;
	}

	if (Debug.debugging("shape")) {
	    Debug.output(prettyName + "|SpatialIndexHolder.getWholePlanet(): finished fetch.");
	}
    }

    public void setPrettyName(String set) {
	prettyName = set;
    }

    public String getPrettyName() {
	return prettyName;
    }

    public void setBuffered(boolean set) {
	buffered = set;
    }

    public boolean getBuffered() {
	return buffered;
    }

    public void setDrawingAttributes(DrawingAttributes set) {
	drawingAttributes = set;
    }

    public DrawingAttributes getDrawingAttributes() {
	return drawingAttributes;
    }

    public void setEnabled(boolean set) {
	enabled = set;
    }

    public boolean getEnabled() {
	return enabled;
    }
}
