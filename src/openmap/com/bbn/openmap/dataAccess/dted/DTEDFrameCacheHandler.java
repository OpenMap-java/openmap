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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDFrameCacheHandler.java,v $
// $RCSfile: DTEDFrameCacheHandler.java,v $
// $Revision: 1.6 $
// $Date: 2006/01/13 22:05:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.grid.GeneratorLoader;
import com.bbn.openmap.omGraphics.grid.OMGridGenerator;
import com.bbn.openmap.omGraphics.grid.SinkGenerator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * The DTEDFrameCacheHandler is a cache for objects being rendered on the map as
 * a result of reading in DTED data. It communicates with a DTEDFrameCache to
 * get OMGrid data from the actual DTED data files, and then sets
 * OMGridGenerators on those OMGrids to create representations of the DTED.
 * <P>
 * 
 * The DTEDFrameCacheHandler uses GeneratorLoaders to create OMGridGenerators
 * for its OMGrids. The GeneratorLoaders provide a GUI for controlling those
 * OMGridGenerator parameters. The list of GeneratorLoaders can be set via
 * Properties. In general, properties being set for a DTEDFrameCacheHandler:
 * <P>
 * 
 * <pre>
 * 
 * 
 *    markerName.generators=greys colors
 *    markerName.greys.class=com.bbn.openmap.omGraphics.grid.SlopeGeneratorLoader
 *    markerName.greys.prettyName=Slope Shading
 *    markerName.greys.colorsClass=com.bbn.openmap.omGraphics.grid.GreyscaleSlopeColors
 *    markerName.colors.class=com.bbn.openmap.omGraphics.grid.SlopeGeneratorLoader
 *    markerName.colors.prettyName=Elevation Shading
 *    markerName.colors.colorsClass=com.bbn.openmap.omGraphics.grid.ColoredShadingColors
 * 
 * 
 * </pre>
 * 
 * The only properties that are required for the DTEDFrameCacheHandler are the
 * generators property, and then the .class properties for the generator loader
 * class names. All of the other generator loader properties are sent to the
 * generator loader for interpretation.
 * <p>
 * 
 * The markerName is generally provided by the parent component of the
 * DTEDFrameCacheHandler, like the DTEDFrameCacheLayer.
 */
public class DTEDFrameCacheHandler extends CacheHandler implements DTEDConstants, PropertyConsumer,
        PropertyChangeListener {

    public final static String GeneratorLoadersProperty = "generators";

    /** The real frame cache. */
    protected DTEDFrameCache frameCache;

    // Setting up the screen...
    protected double frameUp, frameDown, frameLeft, frameRight;

    // Returning the images...
    protected boolean firstImageReturned = true;
    protected double frameLon = 0.0;
    protected double frameLat = 0.0;
    protected boolean newframe = false;

    protected int dtedLevel = LEVEL_0;
    /**
     * The active GeneratorLoader providing OMGridGenerators to the OMGrids.
     */
    protected GeneratorLoader activeGeneratorLoader = null;
    /**
     * The list of GeneratorLoaders.
     */
    protected List<GeneratorLoader> generatorLoaders = new ArrayList<GeneratorLoader>();

    /**
     * The DTEDFrameCache must be set at some point.
     */
    protected DTEDFrameCacheHandler() {
        this(null);
    }

    /**
     * Create a handler for the DTEDFrameCache.
     */
    public DTEDFrameCacheHandler(DTEDFrameCache dfc) {
        setFrameCache(dfc);
    }

    /**
     * Set the DTEDFrameCache.
     */
    public void setFrameCache(DTEDFrameCache dfc) {
        frameCache = dfc;
        resetCache();
    }

    /**
     * Get the DTEDFrameCache.
     */
    public DTEDFrameCache getFrameCache() {
        return frameCache;
    }

    /**
     * Get an elevation at a point. Always uses the cache to load the frame and
     * get the data. DTED data is in meters.
     */
    public int getElevation(float lat, float lon) {
        if (frameCache != null) {
            return frameCache.getElevation(lat, lon);
        } else {
            return DTEDFrameCache.NO_DATA;
        }
    }

    /**
     * Set the DTED level to get from the DTEDFrameCache.
     */
    public void setDtedLevel(int level) {
        dtedLevel = level;
    }

    /**
     * Get the DTED level being retrieved from the DTEDFrameCache.
     */
    public int getDtedLevel() {
        return dtedLevel;
    }

    /**
     * Set the active GeneratorLoader based on a pretty name from one of the
     * loaders.
     */
    public void setActiveGeneratorLoader(String active) {
        for (GeneratorLoader gl : generatorLoaders) {
            if (active.equals(gl.getPrettyName()) && gl != activeGeneratorLoader) {
                activeGeneratorLoader = gl;
                resetCache();
            }
        }
    }

    /**
     * Get a new OMGridGenerator from the active GeneratorLoader.
     */
    public OMGridGenerator getGenerator() {
        if (activeGeneratorLoader != null) {
            return activeGeneratorLoader.getGenerator();
        } else if (generatorLoaders != null && generatorLoaders.size() > 0) {
            activeGeneratorLoader = generatorLoaders.get(0);
            return activeGeneratorLoader.getGenerator();
        } else {
            return new SinkGenerator();
        }
    }

    /**
     * GUI Panel holding the GeneratorLoader GUIs.
     */
    final JPanel cards = new JPanel(new CardLayout());

    /**
     * Get the GUI for the GeneratorLoaders.
     */
    public Component getGUI() {
        JPanel pane = new JPanel(new BorderLayout());

        int numLoaders = generatorLoaders.size();
        String comboBoxItems[] = new String[numLoaders];
        int count = 0;

        for (GeneratorLoader gl : generatorLoaders) {
            String prettyName = gl.getPrettyName();
            comboBoxItems[count++] = prettyName;

            Component glGui = gl.getGUI();
            if (glGui == null) {
                glGui = new JLabel("No options available.");
            }

            cards.add(glGui, prettyName);
        }

        JComboBox cb = new JComboBox(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                CardLayout cl = (CardLayout) (cards.getLayout());
                String active = (String) evt.getItem();
                cl.show(cards, active);
                setActiveGeneratorLoader(active);
            }
        });
        // Put the JComboBox in a JPanel to get a nicer look.
        JPanel comboBoxPane = new JPanel(); // use FlowLayout
        comboBoxPane.add(cb);

        pane.add(comboBoxPane, BorderLayout.NORTH);
        pane.add(cards, BorderLayout.CENTER);
        return pane;
    }

    /**
     * The call to the cache that lets you choose what kind of information is
     * returned. This function also figures out what part of the earth is
     * covered on the screen, and creates auxillary cache handlers as needed.
     * 
     * @param proj The projection of the screen (CADRG).
     * @return List of rasters to display.
     */
    public OMGraphicList getRectangle(Projection proj) {

        double[] lat = new double[3];
        double[] lon = new double[3];

        // This next bit of mumbo jumbo is to handle the equator and
        // dateline: Worst case, crossing both, treat each area
        // separately, so it is the same as handling four requests for
        // data - above and below the equator, and left and right of
        // the dateline. Normal case, there is only one box. Two
        // boxes if crossing only one of the boundaries.

        int xa = 2;
        int ya = 2;
        int lat_minus = 2;
        int lon_minus = 2;
        // Set up checks for equator and dateline
        Point2D ll1 = proj.getUpperLeft();
        Point2D ll2 = proj.getLowerRight();

        lat[0] = ll1.getY();
        lon[0] = ll1.getX();
        lat[1] = ll2.getY();
        lon[1] = ll2.getX();
        lat[2] = ll2.getY();
        lon[2] = ll2.getX();

        if (lon[0] > 0 && lon[2] < 0) {
            lon[1] = -179.999; // put a little breather on the
            // dateline
            lon_minus = 1;
        }
        if (lat[0] > 0 && lat[2] < 0) {
            lat[1] = -0.0001; // put a little breather on the
            // equator
            lat_minus = 1;
        }

        if (Debug.debugging("dteddetail")) {
            Debug.output("For :");
            Debug.output("lat[0] " + lat[0]);
            Debug.output("lon[0] " + lon[0]);
            Debug.output("lat[1] " + lat[1]);
            Debug.output("lon[1] " + lon[1]);
            Debug.output("lat[2] " + lat[2]);
            Debug.output("lon[2] " + lon[2]);
            Debug.output("lat_minus = " + lat_minus);
            Debug.output("lon_minus = " + lon_minus);
        }

        /*
         * Look at all the paths if needed. Worst case, there are four boxes on
         * the screen. Best case, there is one. The things that create boxes and
         * dictates how large they are are the equator and the dateline. When
         * the screen straddles one or both of these lat/lon lines, lon_minus
         * and lat_minus get adjusted, causing two or four different calls to
         * the tochandler to get the data above/below the equator, and
         * left/right of the dateline. Plus, each path gets checked until the
         * required boxes are filled.
         */

        if (Debug.debugging("dted")) {
            Debug.output("--- DTEDFrameCacheHandler: getting images: ---");
        }

        setProjection(proj, lat[ya - lat_minus], lon[xa - lon_minus], lat[ya], lon[xa]);

        OMGraphicList list = loadListFromHandler(null);

        // Dateline split
        if (lon_minus == 1) {
            setProjection(proj, lat[ya - lat_minus], lon[0], lat[ya], -1f * lon[1]); // -1
                                                                                     // to
                                                                                     // make
                                                                                     // it
                                                                                     // 180
            list = loadListFromHandler(list);
        }

        // Equator Split
        if (lat_minus == 1) {
            setProjection(proj, lat[0], lon[xa - lon_minus], -1f * lat[1], // flip
                    // breather
                    lon[xa]);
            list = loadListFromHandler(list);
        }

        // Both!!
        if (lon_minus == 1 && lat_minus == 1) {
            setProjection(proj, lat[0], lon[0], -1f * lat[1],// flip
                    // breather
                    -1f * lon[1]);// -1 to make it 180, not -180
            list = loadListFromHandler(list);
        }

        if (Debug.debugging("dted")) {
            Debug.output("--- DTEDFrameCacheHandler: finished getting images ---");
        }

        return list;
    }

    /**
     * Method that pings the cache for images based on the projection that has
     * been set on it. If the cache returns null from getNextImage(), it's done.
     * Method creates and returns a graphics list if the one passed in is null,
     * otherwise it returns the one passed in.
     */
    protected OMGraphicList loadListFromHandler(OMGraphicList graphics) {
        if (graphics == null) {
            graphics = new OMGraphicList();
        }

        OMGraphic image = getNextImage();

        while (image != null) {
            graphics.add(image);
            image = getNextImage();
        }

        return graphics;
    }

    /**
     * The method to call to let the cache handler know what the projection
     * looks like so it can figure out which frames (and subframes) will be
     * needed.
     * 
     * @param proj the projection of the screen.
     */
    public void setProjection(Projection proj) {
        Point2D ul = proj.getUpperLeft();
        Point2D lr = proj.getLowerRight();
        setProjection(proj, ul.getY(), ul.getX(), lr.getY(), lr.getX());
    }

    /**
     * The method to call to let the cache handler know what the projection
     * looks like so it can figure out which frames (and subframes) will be
     * needed. Should be called when the CacheHandler is dealing with just a
     * part of the map, such as when the map covers the dateline or equator.
     * 
     * @param proj the projection of the screen.
     * @param lat1 latitude of the upper left corner of the window, in decimal
     *        degrees.
     * @param lon1 longitude of the upper left corner of the window, in decimal
     *        degrees.
     * @param lat2 latitude of the lower right corner of the window, in decimal
     *        degrees.
     * @param lon2 longitude of the lower right corner of the window, in decimal
     *        degrees.
     */
    public void setProjection(Projection proj, double lat1, double lon1, double lat2, double lon2) {

        firstImageReturned = true;

        // upper lat of top frame of the screen
        // lower lat of bottom frame of the screen
        // left lon of left frame of the screen
        // upper lon of right frame of the screen
        frameUp = Math.floor(lat1);
        frameDown = Math.floor(lat2);
        frameLeft = Math.floor(lon1);
        frameRight = Math.ceil(lon2);

        if (Debug.debugging("dted"))
            Debug.output("frameUp = " + frameUp + ", frameDown = " + frameDown + ", frameLeft = "
                    + frameLeft + ", frameRight = " + frameRight);
    }

    /**
     * Returns the next OMGraphic image. When setProjection() is called, the
     * cache sets the projection parameters it needs, and also resets this
     * popping mechanism. When this mechanism is reset, you can keep calling
     * this method to get another subframe image. When it returns a null value,
     * it is done. It will automatically skip over window frames it doesn't
     * have, and return the next one it does have. It traverses from the top
     * left to right frames, and top to bottom for each column of frames. It
     * handles all the subframes for a frame at one time.
     * 
     * @return OMGraphic image.
     */
    public OMGraphic getNextImage() {

        if (Debug.debugging("dted"))
            Debug.output("--- DTEDFrameCacheHandler: getNextImage:");

        while (true) {

            if (firstImageReturned == true) {
                frameLon = frameLeft;
                frameLat = frameDown;
                newframe = true;
                firstImageReturned = false;
            } else if (frameLon < frameRight) {
                // update statics to look for next frame
                if (frameLat < frameUp) {
                    frameLat++;
                } else {
                    frameLat = frameDown;
                    frameLon++;
                }
                newframe = true;
            } else { // bounds exceeded, all done
                return (OMGraphic) null;
            }

            if (newframe && frameLon < frameRight) {
                if (Debug.debugging("dted")) {
                    Debug.output(" gni: Getting new frame Lat = " + frameLat + " Lon = " + frameLon);
                }

                OMGraphic omg = get(frameLat, frameLon, dtedLevel);
                if (omg != null) {
                    return omg;
                }
            }
        }
    }

    /**
     * Return an OMGraphic for the Dted Frame, given A lat, lon and dted level.
     * 
     * @param lat latitude of point
     * @param lon longitude of point
     * @param level the dted level wanted (0, 1, 2)
     * @return OMGraphic, most likely an OMGrid.
     */
    public OMGraphic get(double lat, double lon, int level) {
        // First, put together a key from the above info, and then
        // look for it in the local cache. If it's not there, then go
        // to the DTEDFrameCache.

        String key = new String(lat + ":" + lon + ":" + level);

        CacheObject ret = searchCache(key);
        if (ret != null) {
            if (Debug.debugging("dted")) {
                Debug.output("DTEDFrameCacheHandler.get():  retrieving frame from cache (" + lat
                        + ":" + lon + ":" + level + ")");
            }
            return (OMGraphic) ret.obj;
        }

        ret = load(key, lat, lon, level);
        if (ret == null) {
            return null;
        }

        replaceLeastUsed(ret);
        if (Debug.debugging("dted")) {
            Debug.output("DTEDFrameCacheHandler.get():  loading new frame into cache (" + lat + ":"
                    + lon + ":" + level + ")");
        }
        return (OMGraphic) ret.obj;
    }

    /**
     * Load a dted frame into the cache, based on the path of the frame as a
     * key. Implements abstract CacheHandler method.
     * 
     * @param key key to remember raster created for DTED frame.
     * @return DTED frame, hidden as a CacheObject.
     */
    public CacheObject load(String key, double lat, double lon, int level) {
        if (frameCache != null) {

            DTEDFrame frame = frameCache.get(lat, lon, level);
            if (frame != null) {
                OMGrid omgrid = frame.getOMGrid();

                // Need to create a unique generator for each OMGrid.
                omgrid.setGenerator(getGenerator());
                return new DTEDCacheObject(key, omgrid);
            }
        }

        return null;
    }

    public CacheObject load(Object key) {
        // Do nothing, because this implementation doesn't use it.
        // The get() method has been overridden to c all the other
        // load method with addition needed information to better call
        // the DTEDFrameCache.
        return null;
    }

    /**
     * A private class that makes sure that cached frames get disposed properly.
     */
    private static class DTEDCacheObject extends CacheObject {
        /**
         * Construct a DTEDCacheObject, just calls superclass constructor
         * 
         * @param id passed to superclass
         * @param obj passed to superclass
         */
        public DTEDCacheObject(String id, OMGraphic omg) {
            super(id, omg);
        }
    }

    // //// PropertyConsumer Interface Methods

    /**
     * Token uniquely identifying this component in the application properties.
     */
    protected String propertyPrefix = null;

    /**
     * Sets the properties for the OMComponent.
     * 
     * @param props the <code>Properties</code> object.
     */
    public void setProperties(java.util.Properties props) {
        setProperties(getPropertyPrefix(), props);
    }

    /**
     * Sets the properties for the OMComponent.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, java.util.Properties props) {
        setPropertyPrefix(prefix);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
        String generatorList = props.getProperty(realPrefix + GeneratorLoadersProperty);
        if (generatorList != null) {
            Vector<String> generatorMarkers = PropUtils.parseSpacedMarkers(generatorList);
            for (String gmString : generatorMarkers) {
                String loaderPrefix = realPrefix + gmString;
                String loaderClassnameProperty = loaderPrefix + ".class";
                String classname = props.getProperty(loaderClassnameProperty);

                try {
                    GeneratorLoader loader = (GeneratorLoader) ComponentFactory.create(classname);
                    loader.setProperties(loaderPrefix, props);

                    generatorLoaders.add(loader);
                    loader.addPropertyChangeListener(this);
                    // Initialize
                    if (activeGeneratorLoader == null) {
                        activeGeneratorLoader = loader;
                    }
                } catch (ClassCastException cce) {
                    Debug.output("DTEDFrameCacheHandler created " + classname
                            + ", but it's not a GeneratorLoader");
                } catch (NullPointerException npe) {
                    Debug.error("DTEDFrameCacheHandler:  problem creating generator loader: "
                            + classname + " from " + loaderClassnameProperty);
                }
            }
        }
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the OMComponent. If the component has a propertyPrefix
     * set, the property keys should have that prefix plus a separating '.'
     * prepended to each property key it uses for configuration.
     * 
     * @param props a Properties object to load the PropertyConsumer properties
     *        into. If props equals null, then a new Properties object should be
     *        created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        StringBuffer sb = new StringBuffer();
        for (GeneratorLoader gl : generatorLoaders) {
            String pref = gl.getPropertyPrefix();
            props.put(pref + ".class", gl.getClass().getName());
            gl.getProperties(props);

            int index = pref.indexOf(prefix);
            if (index != -1) {
                pref = pref.substring(index + prefix.length());
            }
            sb.append(pref).append(" ");
        }

        props.put(prefix + GeneratorLoadersProperty, sb.toString());

        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.). For Layer, this method should at least
     * return the 'prettyName' property.
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
        // Not sure how to set up an inspector to create child classes
        // yet.
        return list;
    }

    /**
     * Set the property key prefix that should be used by the PropertyConsumer.
     * The prefix, along with a '.', should be prepended to the property keys
     * known by the PropertyConsumer.
     * 
     * @param prefix the prefix String.
     */
    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to the property
     * keys for Properties lookups.
     * 
     * @return the property prefix string
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    /**
     * The DTEDFrameCacheHandler needs to sign up as a PropertyChangeListener so
     * if anything on the GeneratorLoader GUI changes, it knows to dump the
     * current representations so they can be rebuild with the current GUI
     * settings.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        clear();
    }

    public List<GeneratorLoader> getGeneratorLoaders() {
        if (generatorLoaders == null) {
            generatorLoaders = new ArrayList<GeneratorLoader>();
        }
        return generatorLoaders;
    }

    public void setGeneratorLoaders(List<GeneratorLoader> generatorLoaders) {
        this.generatorLoaders = generatorLoaders;
    }
    
    public void clearGeneratorLoaders() {
        getGeneratorLoaders().clear();
    }
    
    public void addGeneratorLoader(GeneratorLoader gl) {
        getGeneratorLoaders().add(gl);
    }
    
    public boolean removeGeneratorLoader(GeneratorLoader gl) {
        return getGeneratorLoaders().remove(gl);
    }

}
