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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/CSVTiledImagePlugIn.java,v $
// $RCSfile: CSVTiledImagePlugIn.java,v $
// $Revision: 1.8 $
// $Date: 2006/01/13 21:05:22 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;

import com.bbn.openmap.io.CSVFile;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.ListResetPCPolicy;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The CSVTiledImageLayer can be used to load image files for background use.
 * The images are automatically scaled to fit the projection. The actual
 * accuracy of matching the scaled images to the map depends on the projection
 * of the images, and the projection type being used for the map. At larger
 * scales, the error may not be noticeable.
 * <P>
 * 
 * This Layer uses a csv file to find out about the images that should be
 * loaded. The layer assumes that the csv file has these fields, in this order:
 * 
 * <pre>
 * 
 *   Upper Latitude, Western Longitude, Lower Latitude, Eastern Longitude, URL of image
 *   42,-73.5,40,-72,file:///data/images/test1.jpg
 *   40,-73.5,38,-72,file:///data/images/test2.jpg
 * 
 * </pre>
 * 
 * Make sure there are not spaces in the data portion of the file!
 * <P>
 * The properties for the layer look like this:
 * 
 * <pre>
 * 
 *   layer.class=com.bbn.openmap.layer.image.CSVTiledImageLayer
 *   layer.prettyName=Name of Layer
 *   # URL of the data file
 *   layer.tileFile=URL of csv file
 *   # If the csv file has descriptive header line that should not be used for images. (default = true)
 *   layer.fileHasHeader=true
 * 
 * </pre>
 * 
 * The layer doesn't do anything fancy with image caching. The layer loads all
 * of the images when the layer is turned on. You might have to bump up the heap
 * size of the JVM when loading large or many images.
 * <p>
 * 
 * This class was inspired by, and created from parts of the ImageLayer
 * submission from Adrian Lumsden@sss, on 25-Jan-2002. Use the MediaTracker code
 * from that class. Update: The MediaTracker has been replaced by ImageIO
 * components in OpenMap 4.6.3.
 */
public class CSVTiledImageLayer extends OMGraphicHandlerLayer {
    /**
     * The set of tiles.
     */
    protected HashSet tiles = new HashSet();
    protected boolean DEBUG = false;
    /** The property for the data file - tileFile. */
    public final static String CSVFileNameProperty = "tileFile";
    /**
     * The property for whether the data file has a descriptive header on the
     * first line, to let the reader know to ignore that line - fileHasHeader.
     * Default is true.
     */
    public final static String FileHasHeaderProperty = "fileHasHeader";

    protected String tileFileName = null;
    protected boolean fileHasHeader = true;
    protected CSVFile tilefile = null;

    /**
     * Index of column for upper latitude of image in the csv file. Default 0.
     */
    public int ullatIndex = 0;
    /**
     * Index of column for western longitude of image in the csv file. Default
     * 1.
     */
    public int ullonIndex = 1;
    /**
     * Index of column for lower latitude of image in the csv file. Default 2.
     */
    public int lrlatIndex = 2;
    /**
     * Index of column for eastern longitude of image in the csv file. Default
     * 3.
     */
    public int lrlonIndex = 3;
    /**
     * Index of column for URL of image file in the csv file. Default 4.
     */
    public int urlIndex = 4;

    /**
     * Default constructor.
     */
    public CSVTiledImageLayer() {
        DEBUG = Debug.debugging("tiledimage");
        setProjectionChangePolicy(new ListResetPCPolicy(this));
    }

    /**
     * The prepare method is called when the projection change.
     * 
     * @return OMGraphicList to be painted for the current projection.
     */
    public OMGraphicList prepare() {

        OMGraphicList list = new OMGraphicList();
        Projection p = getProjection();
        if (DEBUG) {
            Debug.output("CSVTIPI: getRectangle");
        }

        if (tilefile == null) {
            loadTiles(tileFileName);
        }

        // doesn't matter if loadDataFile fails or not, the iterator
        // will be empty if it is.

        // Now, add the tiles that are on the screen.
        Iterator it = tiles.iterator();
        while (it.hasNext()) {

            OMScalingRaster tile = (OMScalingRaster) it.next();

            if (tile.isOnMap(p)) {
                if (DEBUG) {
                    Debug.output("CSVTIPI: image on map");
                }
                tile.generate(p);
                list.add(tile);
            } else if (DEBUG) {
                Debug.output("CSVTIPI: image not on map, skipping");
            }
        }

        return list;
    } // end getRectangle

    /**
     * PropertyConsumer method, setting the layer with properties that apply to
     * it.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        tileFileName = props.getProperty(realPrefix + CSVFileNameProperty);

        if (DEBUG) {
            Debug.output("CSVTIPI: file: " + tileFileName);
        }

        fileHasHeader = PropUtils.booleanFromProperties(props, realPrefix + FileHasHeaderProperty, fileHasHeader);

        if (DEBUG) {
            Debug.output("CSVTIPI: file has header: " + fileHasHeader);
        }
    }

    /**
     * Takes the URL to a csv file and parses it into OMScaledRasters, adding
     * them to the tiles HashSet.
     */
    protected void loadTiles(String csvFileName) {
        int imageCount = 0;

        if (csvFileName != null) {
            try {
                tilefile = new CSVFile(csvFileName);
                tilefile.setHeadersExist(fileHasHeader);
                tilefile.loadData(false);
                // MediaTracker tracker = new MediaTracker(component); // Create
                // a media tracker

                Iterator records = tilefile.iterator();
                while (records.hasNext()) {
                    Vector record = (Vector) records.next();

                    if (DEBUG) {
                        Debug.output("CSVTIPI: record: " + record);
                    }

                    String imageURLString = null;

                    try {

                        float ullat = ((Double) record.get(ullatIndex)).floatValue();
                        float ullon = ((Double) record.get(ullonIndex)).floatValue();
                        float lrlat = ((Double) record.get(lrlatIndex)).floatValue();
                        float lrlon = ((Double) record.get(lrlonIndex)).floatValue();
                        imageURLString = (String) record.get(urlIndex);

                        URL imageURL = PropUtils.getResourceOrFileOrURL(imageURLString);

                        FileCacheImageInputStream fciis = new FileCacheImageInputStream(imageURL.openStream(), null);
                        BufferedImage fileImage = ImageIO.read(fciis);

                        // ImageIcon ii = new ImageIcon(imageURL);
                        // Image fileImage = ii.getImage();

                        // try {
                        // tracker.addImage(fileImage, imageCount);
                        // tracker.waitForID(imageCount);
                        // } catch (Exception e) {
                        // if (Debug.debugging("csvtiledimage")) {
                        // e.printStackTrace();
                        // }
                        // } // Catch errors

                        OMScalingRaster omsr = new OMScalingRaster(ullat, ullon, lrlat, lrlon, fileImage);
                        tiles.add(omsr);
                        imageCount++;

                    } catch (MalformedURLException innerMurle) {
                        Debug.error("CSVTiledImageLayer: image tile path not valid: "
                                + imageURLString + ", skipping...");
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        Debug.error("CSVTiledImageLayer: having trouble reading line ("
                                + imageCount + "), skipping...\n" + aioobe.getMessage());
                    } catch (IOException ioe) {
                        Debug.error("CSVTiledImageLayer: having trouble reading line ("
                                + imageCount + "), skipping...\n" + ioe.getMessage());
                    }
                }

            } catch (MalformedURLException murle) {
                Debug.error("CSVTiledImageLayer: CSV tile file not valid: " + csvFileName);
            }
        }
    }

    /**
     * Method to fill in a Properties object, reflecting the current values of
     * the PropertyConsumer. If the PropertyConsumer has a prefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
     * 
     * @param getList a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new Properties
     *        object should be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        getList.put(prefix + CSVFileNameProperty, PropUtils.unnull(tileFileName));
        getList.put(prefix + FileHasHeaderProperty, new Boolean(fileHasHeader).toString());

        return getList;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.).
     * 
     * @param list a Properties object to load the PropertyConsumer properties
     *        into. If getList equals null, then a new Properties object should
     *        be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }

        list.put(CSVFileNameProperty, "URL to CSV data file");
        list.put(FileHasHeaderProperty, "Flag to note if CSV file has descriptive header line");
        list.put(FileHasHeaderProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return list;
    }

    public boolean isFileHasHeader() {
        return fileHasHeader;
    }

    public void setFileHasHeader(boolean fileHasHeader) {
        this.fileHasHeader = fileHasHeader;
    }

    public int getLrlatIndex() {
        return lrlatIndex;
    }

    public void setLrlatIndex(int lrlatIndex) {
        this.lrlatIndex = lrlatIndex;
    }

    public int getLrlonIndex() {
        return lrlonIndex;
    }

    public void setLrlonIndex(int lrlonIndex) {
        this.lrlonIndex = lrlonIndex;
    }

    public CSVFile getTilefile() {
        return tilefile;
    }

    public void setTilefile(CSVFile tilefile) {
        this.tilefile = tilefile;
    }

    public String getTileFileName() {
        return tileFileName;
    }

    public void setTileFileName(String tileFileName) {
        this.tileFileName = tileFileName;
    }

    public HashSet getTiles() {
        return tiles;
    }

    public void setTiles(HashSet tiles) {
        this.tiles = tiles;
    }

    public int getUllatIndex() {
        return ullatIndex;
    }

    public void setUllatIndex(int ullatIndex) {
        this.ullatIndex = ullatIndex;
    }

    public int getUllonIndex() {
        return ullonIndex;
    }

    public void setUllonIndex(int ullonIndex) {
        this.ullonIndex = ullonIndex;
    }

    public int getUrlIndex() {
        return urlIndex;
    }

    public void setUrlIndex(int urlIndex) {
        this.urlIndex = urlIndex;
    }
}
