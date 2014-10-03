// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/areas/AreaHandler.java,v $
// $RCSfile: AreaHandler.java,v $
// $Revision: 1.4.2.8 $
// $Date: 2009/03/03 04:59:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape.areas;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.ShapeConstants;
import com.bbn.openmap.dataAccess.shape.input.DbfInputStream;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.shape.CSVShapeInfoFile;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.layer.shape.SpatialIndex;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * An object to organize graphics in a shapefile and their corresponding
 * attributes in OpenMap. A properties object can determine how areas/graphics
 * are to be colored, or you can grab the graphics directly and color them
 * yourself. It's called AreaHandler because it was originally intended to be a
 * management tool for political boundary areas, but it should work for all
 * shapefiles, really. This object uses a CSV file created from the DBF file
 * that usually accompanies the shapefile. Also, this class does inflict a
 * startup burden on the map. Because all the organizational effort occurs in
 * setProperties(), it occurs even if the handler isn't used in an active Layer.
 * <P>
 * Here is a sample of what this thing is looking for by way of properties:
 * <P>
 * 
 * <pre>
 * 
 * 
 *                  layer.class=com.bbn.openmap.layer.shape.areas.AreaShapeLayer
 *                  layer.prettyName=Layer Name
 *                  layer.shapeFile=/usr/local/data/shape/shapefile.shp
 *                  layer.spatialIndex=/usr/local/data/shape/shapefile.ssx
 *                 
 *                  # Now, provide a data file that says what the shapes in the .shp
 *                  # file are.  You can use the DBF file:
 *                  layer.dbfFile=/usr/local/data/shape/shapefile.dbf
 *                  # OR a csv file, created yourself or from the .dbf file.  There
 *                  # should be the same number of entries in the .csv file that are in
 *                  # the .shp file.
 *                  layer.csvFile=/usr/local/data/shape/shapefile.csv
 *                  # An attribute to tell the AreaHandler to skip over the first row
 *                  # of the csv file if it contains descriptive column header names.
 *                  layer.csvFileHasHeader=true
 *                 
 *                  # Default DrawingAttributes properties for everything not defined
 *                  # specifically:
 *                  layer.lineColor=ff000000
 *                  layer.fillColor=ffff00ff
 *                 
 *                  # Now add any other attributes accepted by the DrawingAttributes
 *                  # object, with the prefix as stated above, i.e. layer.lineColor)
 *                  #
 *                  # The first column index is 0, not 1.
 *                  #
 *                  # The key index specifies which column in the csv file contains
 *                  # unique area names that are listed in the areas list here in the
 *                  # properties.  In this case, it's the column that contains MA in one
 *                  # of its rows.
 *                  layer.keyIndex=4
 *                 
 *                  # The name index is the column in the csv file that contains what
 *                  # should be displayed in the application when a shape is chosen - the
 *                  # object's proper name.
 *                  layer.nameIndex=4
 *                  layer.areas=MA RI
 *                  layer.areas.MA.fillColor=ffff0000
 *                  layer.areas.MA.lineColor=ff00ff00
 *                  layer.areas.RI.fillColor=ffff0000
 *                  layer.areas.RI.lineColor=ff00ff00
 * 
 * 
 * </pre>
 * 
 * <P>
 */
public class AreaHandler implements PropertyConsumer {

    /**
     * The known political areas, based on the list of OMGraphics each entry
     * contains.
     */
    protected Hashtable politicalAreas;
    /** The property that lists special colored areas. */
    public static final String areasProperty = "areas";
    /**
     * A property that sets an image URL to use for point objects. Only one
     * image for all point objects.
     */
    public static final String pointImageURLProperty = "pointImageURL";
    /**
     * The property that specifies an index location for the area search key for
     * a shape graphic in the database file. Default is 1. The contents of this
     * column should match the area key used to specify the drawingattributes of
     * that particular object as listed in these properties.
     */
    public static final String keyIndexProperty = "keyIndex";
    /**
     * The property that specifies an index location for the area name for a
     * shape graphic in the database file. Default is 0.
     */
    public static final String nameIndexProperty = "nameIndex";
    /**
     * The resource name, URL or file name of the serialized graphics file.
     */
    public static final String CacheFileProperty = "cacheFile";
    /**
     * The name of the property that holds the name of the CSV file with the
     * area attributes, like the name and the abbreviation (or search Key).
     */
    public final static String csvFileProperty = "csvFile";
    /** Set if the CSVFile has a header record. Default is true. */
    public final static String csvHeaderProperty = "csvFileHasHeader";
    /**
     * The name of the property that holds the name of the DBF file with the
     * area attributes, like the name and the abbreviation (or search Key).
     */
    public final static String dbfFileProperty = "dbfFile";
    /**
     * The list of areas that have special coloring needs. Used to write the
     * properties back out.
     */
    protected Vector areasItems = new Vector();
    /**
     * The index of the column that holds the name of the area. This name will
     * be used for display in the GUI for a particular map object.
     */
    protected int nameIndex = 0;
    /**
     * The index of the column that holds the search key of the area. This is
     * the field that is the key to use for the Hashtable holding all the area
     * descriptions, and should be unique for each named area.
     */
    protected int keyIndex = 1;
    /** The URL location of the cached graphics file. */
    protected URL cacheURL = null;
    /** The graphics list */
    protected OMGraphicList omgraphics = null;

    /**
     * Default draw parameters of the graphics that don't have something
     * specific set for it.
     */
    protected DrawingAttributes drawingAttributes;

    /** The location of the CSV attribute file. */
    protected CSVShapeInfoFile infoFile = null;

    /** The DBF attribute file table model. */
    protected DbfTableModel dbfModel = null;

    /**
     * Flag that specifies that the first line consists of header information,
     * and should not be mapped to a graphic.
     */
    protected boolean csvHasHeader = true;

    protected Properties originalProperties = null;
    protected String originalPrefix = null;
    protected SpatialIndex spatialIndex = null;
    protected GeoCoordTransformation coordTransform = null;

    // public AreaHandler() {}

    /**
     * Construct an AreaHandler. Needs an external SpatialIndex, and default
     * DrawingAttributes.
     */
    public AreaHandler(SpatialIndex si, DrawingAttributes da) {
        setDrawingAttributes(da);
        setSpatialIndex(si);
    }

    public void setDrawingAttributes(DrawingAttributes da) {
        drawingAttributes = da;
    }

    public DrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    public void setSpatialIndex(SpatialIndex si) {
        spatialIndex = si;
    }

    public SpatialIndex getSpatialIndex() {
        return spatialIndex;
    }

    public Hashtable getPoliticalAreas() {
        return politicalAreas;
    }

    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    /**
     * Initializes this object from the given properties
     * 
     * @param props
     *            the <code>Properties</code> holding settings for this object
     */
    public void setProperties(String prefix, Properties props) {
        if (Debug.debugging("areas")) {
            Debug.output("AreaHandler: setting properties");
        }

        setPropertyPrefix(prefix);
        originalProperties = props;

        // These will get initialized when someone asks for it.
        // Otherwise, it delays the startup of the map.
        politicalAreas = null;

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        String transClassName = props.getProperty(realPrefix
                + ShapeLayer.TransformProperty);
        if (transClassName != null) {
            try {
                coordTransform = (GeoCoordTransformation) ComponentFactory.create(
                                                                                  transClassName,
                                                                                  realPrefix
                                                                                          + ShapeLayer.TransformProperty,
                                                                                  props);
            } catch (ClassCastException cce) {

            }
        }
    }

    /** PropertyConsumer method. */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        if (coordTransform instanceof PropertyConsumer) {
            ((PropertyConsumer) coordTransform).getProperties(props);
        }

        return props;
    }

    /** PropertyConsumer method. */
    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        return props;
    }

    /** PropertyConsumer method. */
    public void setPropertyPrefix(String pre) {
        originalPrefix = pre;
    }

    /** PropertyConsumer method. */
    public String getPropertyPrefix() {
        return originalPrefix;
    }

    /**
     * Go through the properties, loading the shapefile, information file and
     * attributes files, and resolve how everything should be drawn. Might take
     * awhile if the files are large. Called from getRectangle, which is called
     * when the AreaShapeLayer is added to the map.
     * 
     * @param prefix
     *            property file entry header name
     * @param props
     *            the properties to look through.
     */
    public void initialize(String prefix, Properties props) {

        if (props == null) {
            Debug.error("AreaHandler: initialize received bad input:\n\tprefix: "
                    + prefix + "\n\tproperties: " + (props == null ? "null" : "OK"));
            politicalAreas = null;
            return;
        }

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        politicalAreas = new Hashtable();

        // OK, Get the graphics. We are not expecting that all the
        // graphics in the file are not too much to handle. Also, we
        // test for the serialized graphics file first, and if it
        // isn't designated, then look for a shapefile and spatial
        // index file to create an OMGraphicsList.
        String cacheFile = props.getProperty(prefix + CacheFileProperty);

        // First find the resource, if not, then try as a file-URL...
        try {
            cacheURL = PropUtils.getResourceOrFileOrURL(this, cacheFile);

            if (cacheURL != null) {
                omgraphics = readCachedGraphics(cacheURL);
            } else {
                // We'll use the spatial index set from the
                // ShapeLayer.

                // Now, get the attribute file
                String dbfFile = props.getProperty(prefix + dbfFileProperty);
                URL dbfFileURL = null;
                if (dbfFile != null) {
                    dbfFileURL = PropUtils.getResourceOrFileOrURL(this, dbfFile);
                }
                if (dbfFileURL != null) {
                    InputStream is = dbfFileURL.openStream();
                    dbfModel = new DbfTableModel(new DbfInputStream(is));
                }
                if (dbfModel == null) {
                    String csvFile = props.getProperty(prefix + csvFileProperty);
                    URL infofileURL = null;
                    if (csvFile != null) {
                        infofileURL = PropUtils.getResourceOrFileOrURL(this, csvFile);
                    }

                    // Read them in.
                    if (infofileURL != null) {
                        infoFile = new CSVShapeInfoFile(csvFile);
                        infoFile.setHeadersExist(PropUtils.booleanFromProperties(
                                                                                 props,
                                                                                 prefix
                                                                                         + csvHeaderProperty,
                                                                                 true));
                        infoFile.loadData(true);
                    }
                }
            }
        } catch (java.net.MalformedURLException murle) {
            omgraphics = new OMGraphicList();
        } catch (java.io.IOException ioe) {
            omgraphics = new OMGraphicList();
        } catch (Exception exc) {
            omgraphics = new OMGraphicList();
        }

        // This is handled properly yet. The PoliticalArea should be
        // updated to handle URLs for area points, and have different
        // icons for different areas.
        // String defaultPointImageURLString =
        // props.getProperty(prefix + pointImageURLProperty);

        // Now, match the attributes to the graphics. Find the
        // indexes of the name and the search key. Also figure out
        // which areas have special coloring needs.
        keyIndex = PropUtils.intFromProperties(props, prefix + keyIndexProperty, keyIndex);
        nameIndex = PropUtils.intFromProperties(props, prefix + nameIndexProperty,
                                                nameIndex);
        String areas = props.getProperty(prefix + areasProperty);

        if (areas == null)
            areas = "";

        StringTokenizer tokenizer = new StringTokenizer(areas, " ");
        // All this uses the properties to set the individual colors
        // of any area
        String currentArea;

        while (tokenizer.hasMoreTokens()) {
            currentArea = tokenizer.nextToken();

            PoliticalArea newParams = new PoliticalArea(currentArea);

            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler: setting SPECIALIZED attributes for \""
                        + newParams.id + "\"");
            }

            areasItems.addElement(currentArea);

            newParams.drawingAttributes = new DrawingAttributes(prefix + areasProperty
                    + "." + currentArea, props);

            politicalAreas.put(newParams.id.toUpperCase().intern(), newParams);
        }

        if (Debug.debugging("areas")) {
            Debug.output("AreaHandler: finished initialization");
        }
    }

    /**
     * Read a cache of OMGraphics
     */
    public OMGraphicList readCachedGraphics(URL url) throws java.io.IOException {

        if (Debug.debugging("areas")) {
            Debug.output("Reading cached graphics");
        }

        OMGraphicList omgraphics = new OMGraphicList();

        if (url != null) {
            omgraphics.readGraphics(url);
        }
        return omgraphics;
    }

    /**
     * Get all the graphics from the shapefile, colored appropriately.
     */
    public OMGraphicList getGraphics() {
        if (omgraphics == null) {
            omgraphics = new OMGraphicList();
            try {
                spatialIndex.getOMGraphics(-180, -90, 180, 90, omgraphics,
                                           drawingAttributes, (Projection) null,
                                           coordTransform);

                updateDrawingParameters(omgraphics);

            } catch (IOException ioe) {
                Debug.error(ioe.getMessage());
            } catch (FormatException fe) {
                Debug.error(fe.getMessage());
            }
        }
        return omgraphics;
    }

    protected void updateDrawingParameters(OMGraphicList omgl) {
        if (omgl != null) {
            for (OMGraphic omg : omgl) {
                Integer recNum = (Integer) omg.getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE);
                if (recNum != null) {
                    getDrawParams(recNum.intValue()).setTo(omg);
                    omg.putAttribute(OMGraphic.INFOLINE, getName(recNum));
                }
            }
        }
    }

    /**
     * Get the graphics for a particular lat/lon area.
     * 
     * @param ulLat
     *            upper left latitude, in decimal degrees.
     * @param ulLon
     *            upper left longitude, in decimal degrees.
     * @param lrLat
     *            lower right latitude, in decimal degrees.
     * @param lrLon
     *            lower right longitude, in decimal degrees.
     * @return OMGraphicList
     */
    public OMGraphicList getGraphics(double ulLat, double ulLon, double lrLat,
                                     double lrLon) {
        return getGraphics(ulLat, ulLon, lrLat, lrLon, (Projection) null);
    }

    /**
     * Get the graphics for a particular lat/lon area.
     * 
     * @param ulLat
     *            upper left latitude, in decimal degrees.
     * @param ulLon
     *            upper left longitude, in decimal degrees.
     * @param lrLat
     *            lower right latitude, in decimal degrees.
     * @param lrLon
     *            lower right longitude, in decimal degrees.
     * @param proj
     *            the current map projection.
     * @return OMGraphicList
     */
    public OMGraphicList getGraphics(double ulLat, double ulLon, double lrLat,
                                     double lrLon, Projection proj) {

        if (cacheURL != null) {
            return omgraphics;
        }

        if (spatialIndex == null) {
            return new OMGraphicList();
        }

        if (politicalAreas == null) {
            initialize(originalPrefix, originalProperties);
        }

        OMGraphicList list = new OMGraphicList();

        // check for dateline anomaly on the screen. we check for
        // ulLon >= lrLon, but we need to be careful of the check for
        // equality because of floating point arguments...
        if (ProjMath.isCrossingDateline(ulLon, lrLon, proj.getScale())) {
            if (Debug.debugging("areas")) {
                Debug.output("AreaHander.getGraphics(): Dateline is on screen");
            }

            double ymin = Math.min(ulLat, lrLat);
            double ymax = Math.max(ulLat, lrLat);

            try {

                list = spatialIndex.getOMGraphics(ulLon, ymin, 180.0d, ymax, list,
                                                  drawingAttributes, proj, coordTransform);
                list = spatialIndex.getOMGraphics(-180.0d, ymin, lrLon, ymax, list,
                                                  drawingAttributes, proj, coordTransform);

            } catch (InterruptedIOException iioe) {
                // This means that the thread has been interrupted,
                // probably due to a projection change. Not a big
                // deal, just return, don't do any more work, and let
                // the next thread solve all problems.
                list = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (FormatException fe) {
                fe.printStackTrace();
            }

        } else {

            double xmin = (double) Math.min(ulLon, lrLon);
            double xmax = (double) Math.max(ulLon, lrLon);
            double ymin = (double) Math.min(ulLat, lrLat);
            double ymax = (double) Math.max(ulLat, lrLat);
            try {
                list = spatialIndex.getOMGraphics(xmin, ymin, xmax, ymax, list,
                                                  drawingAttributes, proj, coordTransform);
            } catch (InterruptedIOException iioe) {
                // This means that the thread has been interrupted,
                // probably due to a projection change. Not a big
                // deal, just return, don't do any more work, and let
                // the next thread solve all problems.
                list = null;
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            } catch (FormatException fe) {
                fe.printStackTrace();
            }
        }

        updateDrawingParameters(list);
        return list;
    }

    /**
     * Return the graphic name, given the infofile vector on the graphic. The
     * AreaHandler knows which one is the name. Returns an empty string if
     * something goes wrong.
     */
    public String getName(Vector vector) {
        try {
            String string = (String) vector.elementAt(nameIndex);
            return string;
        } catch (ClassCastException cce) {
        }
        return "";
    }

    /**
     * Get the name of the object at the index of the list.  This is a
     * zero-based index.  Remember, the record number out of the shape
     * file is one-based.  The index stored in the OMGraphic attribute
     * created from the shape files is zero-based to help with
     * lookups.
     *
     * @param integer a zero-based index.
     * @return an empty string if something goes wrong, or the name as
     * a String.
     */
    public String getName(Integer integer) {
        try {
            if (infoFile != null) {
                Vector vector = infoFile.getRecord(integer.intValue());
                if (vector != null) {
                    return (String) vector.elementAt(nameIndex);
                }
            } else if (dbfModel != null) {
                Object obj = dbfModel.getValueAt(integer.intValue(), nameIndex);
                if (obj != null) {
                    if (obj instanceof String) {
                        return (String) obj;
                    } else {
                        return obj.toString();
                    }
                }
            }
        } catch (ClassCastException cce) {
        }
        return "";
    }

    /**
     * Given the shapefile record number, find the drawing parameters that
     * should be used for the shape. 
     *
     * @param recordNumber the zero-based record number from the OMGraphicList.
     */
    public DrawingAttributes getDrawParamsFromCSV(int recordNumber) {
        if (infoFile == null) {
            return drawingAttributes;
        }

        // OFF BY ONE!!! The shape record numbers
        // assigned to the records start with 1, while
        // everything else we do starts with 0...
        Vector info = infoFile.getRecord(recordNumber);

        if (info == null) {
            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler.getDrawParameters: record " + recordNumber
                        + " has no info");
            }
            return drawingAttributes;
        }

        Object keyObj = info.elementAt(keyIndex);
        String key = null;
        PoliticalArea pa = null;

        if (keyObj != null) {
            key = createStringFromKeyObject(keyObj);
            pa = (PoliticalArea) politicalAreas.get(key);
        }

        if (pa == null) {
            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler.getDrawParameters: record " + recordNumber
                        + " has key \"" + key + "\" and DEFAULT attributes");
            }
            return drawingAttributes;
        } else {
            // Only bother with this the first time around.
            if (pa.name == null) {
                String name = (String) info.elementAt(nameIndex);
                if (name != null) {
                    pa.name = name;
                } else {
                    pa.name = "";
                }
            }

            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler.getDrawParameters: record " + recordNumber
                        + " has key \"" + key + "\" and SPECIALIZED attributes");
            }
            return pa.drawingAttributes;
        }
    }

    /**
     * Given the shapefile record number, find the drawing parameters from the
     * DBF model that should be used for the shape. Returns the default coloring
     * if the key for the drawing parameters isn't found.
     *
     * @param recordNumber the zero-based record number from the OMGraphicList
     */
    public DrawingAttributes getDrawParamsFromDBF(int recordNumber) {
        if (dbfModel == null) {
            return drawingAttributes;
        }

        if (dbfModel == null || dbfModel.getRowCount() < recordNumber) {
            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler.getDrawParameters: record " + recordNumber
                        + " has no info");
            }
            return drawingAttributes;
        }

        Object keyObj = dbfModel.getValueAt(recordNumber, keyIndex);
        String key = null;
        PoliticalArea pa = null;

        if (keyObj != null) {
            key = createStringFromKeyObject(keyObj);
            pa = (PoliticalArea) politicalAreas.get(key);
        }

        if (pa == null) {
            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler.getDrawParameters: record " + recordNumber
                        + " has key \"" + key + "\" and DEFAULT attributes");
            }
            return drawingAttributes;
        } else {
            // Only bother with this the first time around.
            if (pa.name == null) {
                // String name = (String) info.elementAt(nameIndex);
                String name = (String) dbfModel.getValueAt(recordNumber, nameIndex);
                if (name != null) {
                    pa.name = name;
                } else {
                    pa.name = "";
                }
            }

            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler.getDrawParameters: record " + recordNumber
                        + " has key \"" + key + "\" and SPECIALIZED attributes");
            }
            return pa.drawingAttributes;
        }
    }

    /**
     * OK, we can't assume that we are assigning a string as a key, you might
     * want to key in on a specific attribute that is a number, like the country
     * coloring code that ESRI has in the country file. We're going to assume
     * that if the number has an integer value, it shouldn't have decimal
     * places. That is, a 1.0 will be truncated to 1, because that makes more
     * sense in a data file where you are using a key as a factor. If the double
     * value doesn't match the integer value, though, we'll assume that's what
     * was meant and leave it alone.
     * <p>
     */
    protected String createStringFromKeyObject(Object keyObj) {
        String key = null;
        if (keyObj instanceof String) {
            key = ((String) keyObj).toUpperCase().intern();
        } else if (keyObj instanceof Number) {
            Number keyNum = (Number) keyObj;
            if (keyNum.doubleValue() == (double) keyNum.intValue()) {
                // Strips off empty decimal places, for sure
                key = Integer.toString(keyNum.intValue()).intern();
            } else {
                key = Double.toString(keyNum.doubleValue()).intern();
            }
        } else {
            try {
                key = keyObj.toString().toUpperCase().intern();
            } catch (Exception e) {
                Debug.error("AreaHandler.createStringFromKeyObject: bad key object:"
                        + keyObj);
            }
        }
        return key;
    }

    /**
     * Given the shapefile record number, find the drawing parameters that
     * should be used for the shape. Returns the default coloring if the key for
     * the drawing parameters isn't found.
     *
     * @param recordNumber the zero-based record number from the OMGraphics.
     */
    public DrawingAttributes getDrawParams(int recordNumber) {
        if (dbfModel != null)
            return getDrawParamsFromDBF(recordNumber);
        else
            return getDrawParamsFromCSV(recordNumber);
    }

    /**
     * This function takes an OMGraphicList and loads each one with the array
     * representing the records in the dbf file. Each graphics stores the
     * graphic in its object slot.
     */
    public void loadDbfModelIntoGraphics(OMGraphicList list) {
        if (list != null && dbfModel.getRowCount() > 0) {
            int numgraphics = list.size();

            for (int i = 0; i < numgraphics; i++) {
                try {
                    OMGraphic omg = list.getOMGraphicAt(i);
                    Integer recnum = (Integer) (omg.getAttribute(ShapeConstants.SHAPE_INDEX_ATTRIBUTE));
                    // OFF BY ONE!!! The shape record numbers
                    // assigned to the records start with 0, while
                    // everything else we do starts with 0. The DbfTableModel
                    // follows java convention and starts at 0. The integer
                    // stored in the OMG should know it too.
                    Object inforec = dbfModel.getRecord(recnum.intValue());
                    omg.putAttribute(ShapeConstants.SHAPE_DBF_INFO_ATTRIBUTE, inforec);
                } catch (ClassCastException cce) {
                    if (Debug.debugging("shape")) {
                        cce.printStackTrace();
                    }
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
    }

    /**
     * Find a PoliticalArea named by the search key. If the shapefile is large,
     * the first query will take a little extra time on the first query to read
     * in the files.
     * 
     * @param area_key
     *            the lookup key, of which the index for the column was
     *            designated in the properties file.
     */
    public PoliticalArea findPoliticalArea(String area_key) {

        // Right now, this method gathers all the graphics in the
        // shape file, groups them, and then returns the PoliticalArea
        // for the key. In the future, it would be nice to have the
        // option to actually search through the data file, find the
        // indexes of the graphics that go to the area, and assemble a
        // temporary list to pass back.

        if (politicalAreas == null) {
            Debug.message("areas", "AreaHandler: initializing graphic attributes");
            initialize(originalPrefix, originalProperties);

            if (omgraphics == null) {
                omgraphics = getGraphics();
                if (dbfModel != null)
                    loadDbfModelIntoGraphics(omgraphics);
                else
                    infoFile.loadIntoGraphics(omgraphics);
            }

            politicalAreas = determinePoliticalAreas(omgraphics);
            Debug.message("areas", "AreaHandler: completed initialization");
        }

        if (politicalAreas != null) {
            String key = area_key.toUpperCase().intern(); // Just to
            // be sure.

            return (PoliticalArea) politicalAreas.get(key);
        } else {
            Debug.error("AreaHandler: initialization failed for " + originalPrefix
                    + "\n\tNo data will be displayed");
            return null;
        }
    }

    /**
     * Find the graphics that are represented by an search key. If the shapefile
     * is large, the first query will take a little extra time on the first
     * query to read in the files.
     * 
     * @param area_key
     *            the lookup key, of which the index for the column was
     *            designated in the properties file.
     */
    public OMGeometryList findGraphics(String area_key) {
        PoliticalArea area = findPoliticalArea(area_key);
        if (area == null) {
            return null;
        } else {
            return area.getGeometry();
        }
    }

    /**
     * DeterminePoliticalAreas goes over a list of omgraphics, and spits out a
     * hashtable that holds PoliticalArea objects for every area key.
     * 
     * @param graphicList
     *            the list of graphics. The top level graphic entries on the
     *            list represent areas.
     */
    public Hashtable determinePoliticalAreas(OMGraphicList graphicList) {
        if (Debug.debugging("areas")) {
            Debug.output("AreaHandler: Determining political areas from OMGraphicList");
        }

        Hashtable poli_areas = new Hashtable();
        return determinePoliticalAreas(graphicList, poli_areas);
    }

    /**
     * DeterminePoliticalAreas goes over a list of omgraphics, and spits out a
     * hashtable that holds PoliticalArea objects for every area key. When an ID
     * is found in the graphics, it is checked in the hashtable for like
     * graphics, and added to that PoliticalArea if found. If not found, a new
     * PoliticalArea is created and placed in the Hashtable. This will duplicate
     * graphics if you call it more than once for the same graphic list.
     * 
     * @param graphicList
     *            the list of graphics. The top level graphic entries on the
     *            list represent areas.
     */
    public Hashtable determinePoliticalAreas(OMGraphicList graphicList,
                                             Hashtable poli_areas) {

        // Simple case. No graphics means an empty list of regions.
        String name = null;
        String key = null;

        if (graphicList != null) {
            int size = graphicList.size();
            for (int i = 0; i < size; i++) {
                OMGraphic graphic = graphicList.getOMGraphicAt(i);
                // below should be a vector like [ "Massachusetts",
                // "MA" ];

                Object obj = graphic.getAttribute(ShapeConstants.SHAPE_DBF_INFO_ATTRIBUTE);
                if (obj == null) {
                    if (Debug.debugging("areas")) {
                        Debug.error("AreaHandler: No attributes for graphic #" + i);
                    }
                    continue;
                }

                if (obj instanceof Vector) {
                    Vector pair = (Vector) obj;

                    name = (String) pair.elementAt(nameIndex);
                    key = ((String) pair.elementAt(keyIndex)).toUpperCase().intern();
                    if (Debug.debugging("areas")) {
                        Debug.output("AreaHandler: looking at " + name + ", " + key);
                    }
                } else if (obj instanceof String) {
                    // Assume that the key is stored here, I guess.
                    key = (String) obj;
                    if (Debug.debugging("areas")) {
                        Debug.output("AreaHandler: String app object, looking at " + key);
                    }
                } else {
                    if (Debug.debugging("areas")) {
                        Debug.output("AreaHandler: Unidentified app object type " + obj);
                    }
                }

                // Get the political area object for this keyiation.
                PoliticalArea area = (PoliticalArea) poli_areas.get(key);

                if (area == null) { // key is not in table
                    area = new PoliticalArea(name, key);
                    poli_areas.put(key, area); // add it to the table
                    // AreaDrawParams adp =
                    // (AreaDrawParams)drawingParams.get(key);
                    // if (adp != null) {
                    // area.setDrawingAttributes(adp.drawingAttributes);
                    // }
                }

                // Add the graphic to the list for this political
                // area.
                area.addGraphic(graphic);
            }

            if (Debug.debugging("areas")) {
                Debug.output("AreaHandler: Finished determinePoliticalAreas: "
                        + poli_areas.size() + " areas defined.");
            }
        }

        return poli_areas;
    }

    protected Color getColor(String colorString) {
        Color result = Color.black;
        try {
            result = PropUtils.parseColor(colorString);
        } catch (NumberFormatException nfe) {
            result = GetColorFromString(colorString);
        }
        return result;
    }

    /**
     * This function would return a Color object for string such as red,
     * green,.. (all that are available from java.awt.color class). It can also
     * return a specific color represented by HEX or Octal number like
     * 0xffeeffee
     */
    protected Color GetColorFromString(String token) {
        String tokstring = (String) token;

        Color result = Color.black;

        if (Debug.debugging("areas")) {
            Debug.output("AreaHandler: GetColorFromString(" + tokstring + ")");
        }

        // Thank the heavens for Emacs macros!
        if (tokstring.equals("black"))
            result = Color.black;
        else if (tokstring.equals("blue"))
            result = Color.blue;
        else if (tokstring.equals("cyan"))
            result = Color.cyan;
        else if (tokstring.equals("darkGray"))
            result = Color.darkGray;
        else if (tokstring.equals("gray"))
            result = Color.gray;
        else if (tokstring.equals("green"))
            result = Color.green;
        else if (tokstring.equals("lightGray"))
            result = Color.lightGray;
        else if (tokstring.equals("magenta"))
            result = Color.magenta;
        else if (tokstring.equals("orange"))
            result = Color.orange;
        else if (tokstring.equals("pink"))
            result = Color.pink;
        else if (tokstring.equals("red"))
            result = Color.red;
        else if (tokstring.equals("white"))
            result = Color.white;
        else if (tokstring.equals("yellow"))
            result = Color.yellow;
        else
            // decode a hex color string.
            result = Color.decode(tokstring);

        if (Debug.debugging("areas")) {
            Debug.output("AreaHandler.GetColorFromToken returns (" + result + ")");
        }
        return result;
    }

    public GeoCoordTransformation getCoordTransform() {
        return coordTransform;
    }

    public void setCoordTransform(GeoCoordTransformation dataTransform) {
        this.coordTransform = dataTransform;
    }

    /**
     * This main function basically reads in the data sources (the shape file
     * and the csv information file, and creates a serialized graphics file that
     * will act like a cache later.
     */
    public static void main(String[] argv) {
        String propertiesFile = null;
        String prefix = null;
        String outputFile = null;

        Debug.init();

        if (argv.length < 6)
            printUsage();

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equalsIgnoreCase("-props")) {
                propertiesFile = argv[++i];
            } else if (argv[i].equalsIgnoreCase("-prefix")) {
                prefix = argv[++i];
            } else if (argv[i].equalsIgnoreCase("-file")) {
                outputFile = argv[++i];
            }
        }

        if (propertiesFile == null || prefix == null || outputFile == null) {
            printUsage();
        }

        try {
            Properties properties = new Properties();
            // Read in the properties.
            URL propertiesURL = new URL(propertiesFile);
            InputStream is = propertiesURL.openStream();
            properties.load(is);

            // Let's make a file
            ShapeLayer sl = new ShapeLayer();
            sl.setProperties(prefix, properties);

            AreaHandler ah = new AreaHandler(sl.getSpatialIndex(),
                    sl.getDrawingAttributes());

            // Set the properties in the handler.
            ah.setProperties(prefix, properties);
            // Write the saved graphics.
            ah.getGraphics().writeGraphics(outputFile);

        } catch (java.net.MalformedURLException murle) {
            Debug.error("Bad URL for properties file : " + propertiesFile);
            printUsage();
        } catch (java.io.IOException ioe) {
            Debug.error("IOException creating cached graphics file: " + outputFile);
            printUsage();
        }
    }

    public static void printUsage() {
        Debug.output("Usage: java com.bbn.openmap.layer.shape.areas.AreaHandler -props <URL to properties file> -prefix <handler property prefix> -file <path to output file>");
        System.exit(-1);
    }

}