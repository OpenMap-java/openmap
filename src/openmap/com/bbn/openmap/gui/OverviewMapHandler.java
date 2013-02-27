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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OverviewMapHandler.java,v $
// $RCSfile: OverviewMapHandler.java,v $
// $Revision: 1.15 $
// $Date: 2006/08/09 21:08:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.Environment;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.DefaultOverviewMouseMode;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.OverviewMapStatusListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.layer.OverviewMapAreaLayer;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The OverviewMapHandler contains a MapBean that contains a projection that
 * reflects another MapBean's projection. It manages the two MapBeans and the
 * differences in the projections betwen them. The OverviewMapHandler can have a
 * projection type independent of that of the source MapBean (the MapBean that
 * the OverviewMapHandler's MapBean is paying attention to). It also contains a
 * scale factor, which is a multiplier to use against the scale of the source
 * MapBean's scale.
 * <P>
 * 
 * The OverviewMapHandler MapBean can also be used to control the source
 * MapBean's projection center and scale. The source MapBean just needs to be
 * added to the OverviewMapHandler by
 * OverviewMapHandler.addControlledMap(MapBean).
 * <P>
 * 
 * The OverviewMapHandler needs to be added to the source MapBean as a
 * ProjectionListener. Then, the overview MapBean can be added to the
 * ContentPane of a Component by calling
 * Component.setContentPane(OverviewMapHandler.getMap()); The OverviewMapHandler
 * Should also be added as a ComponentListener to the Component.
 * <P>
 * 
 * After the first projectionChanged() call is received, the OverviewMapHandler
 * knows about the source MapBean. Since the OverviewMapHandler is a
 * ComponentListener and will therefore find out when it's parent is hidden, it
 * will disengage and engage itself from the source MapBean as it's visibility
 * changes.
 * <P>
 * 
 * To get the overview map to appear in the OpenMap application, add the
 * following properties to your openmap.properties file:
 * 
 * <pre>
 * 
 * 
 *       # First, add overviewMapHandler to the openmap.components marker name list.  Then, add:
 *     
 *       overviewMapHandler.class=com.bbn.opemap.gui.OverviewMapHandler
 *       overviewMapHandler.overviewLayers=overviewLayer
 *       overviewMapHandler.overviewScaleFactor=10f
 *       overviewMapHandler.overviewMinScale=100f
 *       # Set the Unit of Measure for the overviewMinScale property
 *       # Omission of  overviewMinScaleUom indicates that overviewMinScale
 *       # is an projection map scale instead of a distance.
 *       overviewMapHandler.overviewMinScaleUom=km
 *     
 *       # 'overviewStatusLayer' is a marker name for any attributes you may
 *       # want to pass to the overviewStatusLayer instance, in addition to
 *       # being used to define the class to use for that special layer.
 *       overviewMapHandler.overviewStatusLayer.class=com.bbn.openmap.layer.OverviewMapAreaLayer
 *       # Properties can be passed to the overview status layer by listing
 *       # them with the OverviewMapHandler prefix.
 *     
 *       # Set the line color for the coverage box outline...
 *       # overviewMapHandler.lineColor=FFFF0000
 *     
 *       # A sample overview map layer
 *       overviewLayer.class=com.bbn.openmap.layer.shape.ShapeLayer
 *       overviewLayer.prettyName=Overview
 *       overviewLayer.shapeFile=/home/dietrick/dev/openmap/share/dcwpo-browse.shp
 *       overviewLayer.spatialIndex=/home/dietrick/dev/openmap/share/dcwpo-browse.ssx
 *       overviewLayer.lineColor=ff000000
 *       overviewLayer.fillColor=ffbdde83
 * 
 * 
 * </pre>
 * 
 * <p>
 * 
 * If layers are not added to the overview map, then it won't show up in the
 * application.
 */
public class OverviewMapHandler extends OMToolComponent implements ProjectionListener,
        Serializable, PropertyConsumer, PropertyChangeListener, ComponentListener {

    public final static String OverviewMapHandlerLayerProperty = "overviewLayers";
    public final static String ScaleFactorProperty = "overviewScaleFactor";
    public final static String ProjectionTypeProperty = "overviewProjectionType";
    public final static String MinScaleProperty = "overviewMinScale";
    public final static String MinScaleUomProperty = "overviewMinScaleUom";
    public final static String StatusLayerProperty = "overviewStatusLayer";
    public final static String ControlSourceMapProperty = "overviewControlSourceMap";
    public final static String BackgroundSlaveProperty = "backgroundSlave";
    public final static float defaultScaleFactor = 20f;
    public final static float defaultMinScale = 500000f;
    public final static Length defaultMinScaleUom = null;

    /** The multiplier to apply to the scale of the project received. */
    protected float scaleFactor;
    /**
     * The minimum scale to use for the window. If it gets too small with a
     * general type layer, it won't be any use.
     */
    protected float minScale;

    /**
     * The minimum scale unit of measure to use for the window. Use during
     * initialization to compute a projection scale from a distance.
     */
    protected Length minScaleUom;

    /** The map of the overview panel. */
    protected transient MapBean map;
    /**
     * The source MapBean to show the overview of. Gets set when the first
     * projectionChanged() gets called. Also used to disconnect from the MapBean
     * when the component that this OverviewMapHandler is listening to is
     * hidden, and to connect to the MapBean when the component is shown.
     */
    protected transient MapBean sourceMap;
    /** The projection of the overview map bean. */
    protected transient Proj projection;
    /**
     * A layer that can be set to constantly be on the top of the map. If the
     * status layer is also a OverviewMapStatusListener, it also receives the
     * source map projection when that changes, which gives it the capability to
     * draw stuff based on that.
     */
    protected Layer statusLayer;
    /**
     * The support to send the source MapBean setCenter and setScale commands if
     * a controlled map is added - usually the source map bean.
     */
    protected transient ControlledMapSupport controlledMaps;
    /** The mouse mode to use for the overview map. */
    protected MapMouseMode mmm;
    /**
     * The thing listening for a request to bring up a JFrame or JInternalFrame.
     */
    protected ActionListener overviewFrameActionListener = null;
    /** Indicates if OverviewMap should be controlling sourceMap. */
    protected boolean controlSourceMap = true;
    /** Default Frame title for OverviewMapHandler */
    public static final String defaultFrameTitle = "Overview Map";
    /** String The Frame Title */
    protected String frameTitle = defaultFrameTitle;
    /** Default key for Tool */
    public static final String defaultKey = "overviewmaphandler";

    /**
     * Flag to change the background color to whatever the source map's is
     * changed to. True byt default.
     */
    protected boolean backgroundSlave = true;

    public final static int INITIAL_WIDTH = 200;
    public final static int INITIAL_HEIGHT = 100;

    /**
     * Default constructor. make sure init(someProperties) is called before you
     * attempt to use this object
     */
    public OverviewMapHandler() {
        super();
        setKey(defaultKey);
        setLayout(new BorderLayout());
        createOverviewMap();

        // Set up a default...
        projection = createStartingProjection(null);
        addComponentListener(this);
        // Create this when we need it.
        // setWindowSupport(new WindowSupport(this, new WindowSupport.Dlg(null,
        // "Overview Map")));
    }

    /**
     * Create an OverviewMapHandler with properties that do not contain a
     * prefix.
     * 
     * @param props properties object.
     */
    public OverviewMapHandler(Properties props) throws Exception {
        this(null, props);
    }

    /**
     * Create an OverviewMapHandler with properties that do contain a prefix.
     * 
     * @param prefix the prefix for all the properties that apply to the
     *        OverviewMapHandler.
     * @param props properties object.
     */
    public OverviewMapHandler(String prefix, Properties props) throws Exception {

        this();
        setProperties(prefix, props);
    }

    /**
     * Create an OverviewMapHandler for given MapBean.
     * 
     * @param srcMap srcMapBean
     * @param prefix the prefix to place in front of each property - i.e., so
     *        that each property will be under prefix.propertyName. The period
     *        between the two will be added.
     * @param props properties object.
     */
    public OverviewMapHandler(MapBean srcMap, String prefix, Properties props) throws Exception {
        this(prefix, props);
        setSourceMap(srcMap);
    }

    /**
     * Create the MapBean used for the overview map, and suppress the copyright
     * message at the same time.
     */
    protected void createOverviewMap() {
        // We don't need another copyright message, right?
        MapBean.suppressCopyright = true;
        map = new BufferedMapBean();
        this.add(map, BorderLayout.CENTER);
    }

    /**
     * Initialize based on properties, which will not have a prefix.
     * 
     * @deprecated use setProperties(props).
     */
    public void init(Properties props) throws Exception {
        setProperties(null, props);
    }

    /**
     * Initialize an OverviewMapHandler with properties that do contain a
     * prefix.
     * 
     * @param prefix the prefix to place in front of each property - i.e., so
     *        that each property will be under prefix.propertyName. The period
     *        between the two will be added.
     * @param props properties object.
     * @deprecated use setProperties(prefix, props).
     */
    public void init(String prefix, Properties props) throws Exception {
        setProperties(prefix, props);
    }

    /**
     * Sets the properties for the <code>Layer</code>. This allows
     * <code>Layer</code> s to get a richer set of parameters than the
     * <code>setArgs</code> method. Part of the PropertyConsumer interface.
     * Layers which override this method should do something like: <code><pre>
     * public void setProperties(String prefix, Properties props) {
     *     super.setProperties(prefix, props);
     *     // do local stuff
     * }
     * </pre></code> If the addToBeanContext property is not defined, it is set
     * to false here.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, java.util.Properties props) {
        propertyPrefix = prefix;

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        Vector<String> overviewLayers = PropUtils.parseSpacedMarkers(props.getProperty(prefix
                + OverviewMapHandlerLayerProperty));

        if (overviewLayers.isEmpty()) {
            Debug.message("overview", "OverviewMapHandler:  created without layers!");
        }

        scaleFactor = PropUtils.floatFromProperties(props, prefix + ScaleFactorProperty, defaultScaleFactor);

        minScale = PropUtils.floatFromProperties(props, prefix + MinScaleProperty, defaultMinScale);

        String uom = props.getProperty(prefix + MinScaleUomProperty);
        if (uom != null) {
            minScaleUom = Length.get(uom);
            setMinScale(minScale, minScaleUom);
        }

        backgroundSlave = PropUtils.booleanFromProperties(props, prefix + BackgroundSlaveProperty, backgroundSlave);

        setControlSourceMap(PropUtils.booleanFromProperties(props, prefix
                + ControlSourceMapProperty, controlSourceMap));

        String statusLayerName = props.getProperty(prefix + StatusLayerProperty + ".class");
        if (statusLayerName != null) {
            statusLayer = (Layer) ComponentFactory.create(statusLayerName, prefix
                    + StatusLayerProperty, props);
            if (statusLayer == null) {
                Debug.error("OverviewMapHandler.setProperties: status layer not set.");
            }
        } else {
            statusLayer = new OverviewMapAreaLayer();
        }

        statusLayer.setProperties(prefix, props);

        projection = createStartingProjection(props.getProperty(prefix + ProjectionTypeProperty));

        setLayers(LayerHandler.getLayers(overviewLayers, overviewLayers, props));
    }

    protected ProjectionFactory getProjectionFactory() {
        if (sourceMap != null) {
            return sourceMap.getProjectionFactory();
        }
        return ProjectionFactory.loadDefaultProjections();
    }

    private Proj createStartingProjection(String projName) {

        ProjectionFactory projectionFactory = getProjectionFactory();
        Class<? extends Projection> projClass = projectionFactory.getProjClassForName(projName);

        if (projClass == null) {
            projClass = Mercator.class;
        }

        // The scale, lat/lon and size shouldn't matter, because the
        // size will get reset when it is added to a component, and
        // the projection will change when it is added to a MapBean
        // as a projection listener.p
        return (Proj) projectionFactory.makeProjection(projClass, new Point2D.Float(Environment.getFloat(Environment.Latitude, 0f), Environment.getFloat(Environment.Longitude, 0f)), Environment.getFloat(Environment.Scale, Float.POSITIVE_INFINITY)
                * scaleFactor, INITIAL_WIDTH, INITIAL_HEIGHT);
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
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

        // Build marker list
        StringBuffer layerList = new StringBuffer();
        Component[] comps = map.getComponents();
        int ncomponents = comps.length;
        for (int i = 0; i < ncomponents; i++) {
            Layer layer = (Layer) comps[i];
            if (layer != statusLayer) { // Take care of the
                // statusLayer later.
                layerList.append(" ").append(layer.getPropertyPrefix());
                layer.getProperties(props);
            }
        }
        props.put(prefix + OverviewMapHandlerLayerProperty, layerList.toString());

        props.put(prefix + ScaleFactorProperty, Float.toString(scaleFactor));
        props.put(prefix + ProjectionTypeProperty, map.getProjection().getName());
        props.put(prefix + MinScaleProperty, Float.toString(minScale));
        props.put(prefix + BackgroundSlaveProperty, new Boolean(backgroundSlave).toString());

        if (statusLayer != null) {
            props.put(prefix + StatusLayerProperty, statusLayer.getClass().getName());
            statusLayer.getProperties(props);
        }

        props.put(prefix + ControlSourceMapProperty, new Boolean(controlSourceMap).toString());

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

        list.put(OverviewMapHandlerLayerProperty, "Space separated list of marker names of layers to use as background on the overview map.");
        list.put(ScaleFactorProperty, "Multiplier reflecting the difference between the scale of the overview map and the source map (default is 20.0).");
        list.put(ProjectionTypeProperty, "Projection name to use for overview map (Default is mercator).");
        list.put(MinScaleProperty, "Minimum scale of overview map (Default is 500,000.0).");
        list.put(StatusLayerProperty, "Class name of layer to use as the active layer on the overview map, receiving mouse events (Default is com.bbn.openmap.layer.OverviewMapAreaLayer).");
        list.put(ControlSourceMapProperty, "Flag to have the source map controlled by gestures on the overview map (true/false, default is true).");
        list.put(ControlSourceMapProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.TrueFalsePropertyEditor");
        list.put(BackgroundSlaveProperty, "Flag to have the map mimic any changes made to the source map's background (true/false, default is true).");
        list.put(BackgroundSlaveProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.TrueFalsePropertyEditor");

        statusLayer.getPropertyInfo(list);

        return list;
    }

    /**
     * Sets the sourceMap associated with this OverviewMap. if controlSourceMap
     * property is set, srcMap will also be controlled by this OverviewHandler
     * Passing a null value will remove the current sourceMap from the list of
     * Maps that this handler is controlling and set sourceMap to null.
     * 
     * @param srcMap srcMap.
     */
    public void setSourceMap(MapBean srcMap) {
        if (sourceMap != null) {
            removeControlledMap(sourceMap);
            sourceMap.removeProjectionListener(this);
            sourceMap.removePropertyChangeListener(this);
        }

        // Add the sourceMap to a set of listeners that wish to be
        // controlled by this OverviewMapHandler
        if (srcMap != null) {
            if (controlSourceMap == true) {
                addControlledMap(srcMap);
            }

            // Check and see if the overview map window is up. If it
            // is, we should add the overview map as a projection
            // listener to it. Note: overview map windows went away
            // due to window support, but not sure how it affected
            // this statement. Keeping commented code here for
            // reference in case behavior is weird. Seems to be
            // working as expected, though. DFD
            // if ((overviewWindowFrame != null &&
            // overviewWindowFrame.isShowing()) ||
            // (overviewWindow != null && overviewWindow.isShowing())
            // ||

            // Turns out non-tool overview maps weren't becoming
            // projection change listeners...
            if (!getUseAsTool() && isVisible()) {
                srcMap.addProjectionListener(this);
            }

            srcMap.addPropertyChangeListener(this);
        }

        sourceMap = srcMap;
    }

    /**
     * Get the map that the overview map is listening to.
     */
    public MapBean getSourceMap() {
        return sourceMap;
    }

    /**
     * Set the string used for the frame title of the overview map.
     */
    public void setFrameTitle(String in_frameTitle) {
        frameTitle = in_frameTitle;
    }

    public String getFrameTitle() {
        return frameTitle;
    }

    /**
     * Set whether the map's background mimics changes to the source map's
     * background.
     * 
     * @param set true to enable mimicking.
     */
    public void setBackgroundSlave(boolean set) {
        backgroundSlave = set;
    }

    /**
     * Get whether the map's background mimics changes to the source map's
     * background.
     */
    public boolean getBackgroundSlave() {
        return backgroundSlave;
    }

    /**
     * Default value of this property is true. if you want your sourceMap to be
     * controlled by this OverviewMapHandler, set the value of this property for
     * this OverviewHandler. This will allow, for instance, clicking on the
     * overview map to recenter the source map. That depends on the overview map
     * mouse mode, however.
     * 
     * @param value
     */
    public void setControlSourceMap(boolean value) {
        if (sourceMap != null) {
            if (value == true && controlSourceMap == false) {
                addControlledMap(sourceMap);
            }
            if (value == false && controlSourceMap == true) {
                removeControlledMap(sourceMap);
            }
        }
        controlSourceMap = value;
    }

    public boolean getControlSourceMap() {
        return controlSourceMap;
    }

    /**
     * Set the layers in the Overview MapBean. An AreaLayer is automatically
     * added on top.
     */
    public void setLayers(Layer[] layers) {
        map.setLayers(new LayerEvent(this, LayerEvent.REPLACE, new Layer[0]));
        if (statusLayer != null) {
            map.add(statusLayer);
        }
        map.setLayers(new LayerEvent(this, LayerEvent.ADD, layers));
    }

    /**
     * Part of the ProjectionListener interface. The new projections from the
     * source MapBean arrive here.
     * 
     * @param projEvent the projection event from the source MapBean.
     */
    public void projectionChanged(ProjectionEvent projEvent) {
        if (sourceMap == null) {
            sourceMap = (MapBean) projEvent.getSource();
            map.setBckgrnd(sourceMap.getBckgrnd());
        }

        Projection proj = projEvent.getProjection();
        if (proj == null) {
            return;
        }

        if (statusLayer instanceof OverviewMapStatusListener) {
            ((OverviewMapStatusListener) statusLayer).setSourceMapProjection(proj);
        }

        float newScale = proj.getScale() * scaleFactor;

        // Adjust the newScale based on the ratio of the
        // source projection width and the overview
        // map projection width.
        Projection sourceProj = sourceMap.getProjection();
        newScale *= (float) sourceProj.getWidth() / (float) projection.getWidth();

        if (newScale < minScale) {
            newScale = minScale;
        }

        projection.setScale(newScale);
        projection.setCenter(proj.getCenter());
        map.setProjection(projection);
    }

    /**
     * Set the MapMouseMode for the overview map. If you want the status layer
     * to listen to the mouse mode, you have to get the layer and wire it up
     * yourself.
     */
    public void setMouseMode(MapMouseMode ammm) {
        // If we're removing a mouse mode, disconnect it from the map.
        if (ammm == null) {
            deactivateMouseMode();
        }
        mmm = ammm;
        activateMouseMode();
    }

    /**
     * Get the MapMouseMode used for the overview map.
     */
    public MapMouseMode getMouseMode() {
        return mmm;
    }

    /**
     * Adds the mouse mode as a listener to the overview map. If the mouse mode
     * is null, the default is created.
     */
    public void activateMouseMode() {
        if (mmm == null) {
            mmm = new DefaultOverviewMouseMode(this);
        }
        if (map != null) {
            map.addMouseListener(mmm);
            map.addMouseMotionListener(mmm);
        }
    }

    /**
     * Disconnects the mouse mode from the overview map.
     */
    public void deactivateMouseMode() {
        if (mmm != null) {
            map.removeMouseListener(mmm);
            map.removeMouseMotionListener(mmm);
        }
    }

    /**
     * Add a controlled MapBean to the OverviewMapHandler. Use this method to
     * add another MapBean to the overview map in order to have its projection
     * controlled by the overview panel. If the overview panel is clicked on,
     * the listening MapBean will be recentered. If a box is drawn with a mouse
     * drag, the scale of the controlled map will be modified.
     * 
     * @param l MapBean.
     */
    public void addControlledMap(MapBean l) {
        if (l != null) {
            if (controlledMaps == null) {
                controlledMaps = new ControlledMapSupport(map);
                // If nobody has been listening don't draw anything.
                // Since someone is now being controlled, we'll do the
                // drawing.
                activateMouseMode();
            }
            controlledMaps.add(l);
        }
    }

    /**
     * Remove a controlled MapBean from the OverviewMapHandler.
     * 
     * @param l a MapBean.
     */
    public void removeControlledMap(MapBean l) {
        if (controlledMaps != null) {
            controlledMaps.remove(l);

            if (controlledMaps.isEmpty()) {
                deactivateMouseMode();
            }
        }
    }

    /**
     * Get the overview MapBean.
     * 
     * @return overview MapBean.
     */
    public MapBean getMap() {
        return map;
    }

    /**
     * Set the overview MapBean.
     */
    public void setMap(MapBean map) {
        if (map != null) {
            // get rid of any other MapBean that may have been added
            // to the JPanel.
            this.remove(map);
        }
        this.map = map;
        this.add(map, BorderLayout.CENTER);
    }

    /**
     * Get the ControlledMapSupport, which usually contains the source map.
     */
    public ControlledMapSupport getControlledMapListeners() {
        return controlledMaps;
    }

    /**
     * Set the ControlledMapSupport, which usually contains the source map.
     */
    public void setControlledMapListeners(ControlledMapSupport list) {
        controlledMaps = list;
    }

    /**
     * Get the status layer, which is always drawn on top of the other layers,
     * and maintained separately from other layers.
     */
    public Layer getStatusLayer() {
        return statusLayer;
    }

    /**
     * Get the status layer, which is always drawn on top of the other layers,
     * and maintained separately from other layers. If the layer is also an
     * OverviewMapStatusListener, it will receive source map projection changes,
     * so it can draw stuff on itself representing what's going on the source
     * map.
     */
    public void setStatusLayer(Layer layer) {
        statusLayer = layer;
    }

    /**
     * Set the scale factor to use between the source MapBean and the overview
     * MapBean. It's a direct multiplier, so the overview MapBean can actually
     * be a magnified map, too. The overview map scale = source MapBean scale *
     * scaleFactor.
     * 
     * @param setting scale factor
     */
    public void setScaleFactor(float setting) {
        scaleFactor = setting;
    }

    /**
     * Get the scale factor used for the overview MapBean.
     */
    public float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Set the projection of the overview MapBean. Lets you set the type,
     * really. The scale and center will be reset when a projection event is
     * received.
     */
    public void setProjection(Proj proj) {
        projection = proj;
    }

    /**
     * Get the current projection of the overview MapBean.
     */
    public Proj getProjection() {
        return projection;
    }

    /**
     * Set the minimum scale to use for the overview map. If this is set too
     * small with a very general map layer, it won't be of any use, really, if
     * it gets really zoomed in.
     * 
     * @param setting the scale setting - 1:setting
     */
    public void setMinScale(float setting) {
        if (setting > 0) {
            minScale = setting;
        }
    }

    /**
     * Set the minimum scale to use for the overview map. If this is set too
     * small with a very general map layer, it won't be of any use, really, if
     * it gets really zoomed in.
     * 
     * @param width setting the scale setting - 1:setting
     * @param uom unit of measure for width
     */
    public void setMinScale(float width, Length uom) {
        if (width > 0) {
            Projection p = map.getProjection();

            Length projUom = p.getUcuom();

            if (projUom == null) {
                // We're going to assume meters, since this really only applies
                // to pre-projected projections like Cartesian, and we might as
                // well make it something. Not sure if it matters, as long as
                // we're consistent.
                projUom = Length.METER;
            }

            // This is really not the radius but the half width in radians.
            float radius = uom.toRadians(width) / 2;

            Point2D center = p.getCenter();
            Point2D left = new Point2D.Double(center.getX(), center.getY());
            Point2D right = new Point2D.Double(center.getX(), center.getY());

            double newLeftX = projUom.fromRadians(projUom.toRadians(left.getX()) - radius);
            double newRightX = projUom.fromRadians(projUom.toRadians(right.getX()) + radius);

            left.setLocation(newLeftX, left.getY());
            right.setLocation(newRightX, right.getY());

            minScale = ProjMath.getScale(left, right, p);
        }
    }

    public float getMinScale() {
        return minScale;
    }

    /**
     * Invoked when component has been shown. This component should be the
     * component that contains the OverviewMapHandler.
     */
    public void componentShown(ComponentEvent e) {
        if (sourceMap != null) {
            sourceMap.addProjectionListener(this);
        }
    }

    /**
     * Invoked when component has been hidden. This component should be the
     * component that contains the OverviewMapHandler.
     */
    public void componentHidden(ComponentEvent e) {
        if (sourceMap != null) {
            sourceMap.removeProjectionListener(this);
        }
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Return an ActionListener that will bring up an independent window with an
     * Overview Map.
     * 
     * @return ActionListener that brings up a Window when an actionPerformed is
     *         called.
     */
    public ActionListener getOverviewFrameActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                WindowSupport ws = getWindowSupport();

                MapHandler mh = (MapHandler) getBeanContext();
                Frame frame = null;
                if (mh != null) {
                    frame = (Frame) mh.get(java.awt.Frame.class);
                }

                if (ws == null) {
                    ws = new WindowSupport(OverviewMapHandler.this, new WindowSupport.Dlg(frame, "Overview Map"));
                    setWindowSupport(ws);
                }

                int w = INITIAL_WIDTH;
                int h = INITIAL_HEIGHT;
                Dimension dim = ws.getComponentSize();
                if (map != null && dim != null) {
                    w = (int) dim.getWidth();
                    h = (int) dim.getHeight();
                }

                ws.displayInWindow(frame, -1, -1, w, h);
            }
        };
    }

    /** Tool interface method. */
    public Container getFace() {
        JButton b = null;
        if (getUseAsTool()) {
            URL url = getClass().getResource("overview.gif");
            b = new JButton(new ImageIcon(url, frameTitle));
            b.setToolTipText(frameTitle);
            b.setMargin(new Insets(0, 0, 0, 0));
            b.addActionListener(getOverviewFrameActionListener());
            b.setBorderPainted(false);
        }
        return b;
    }

    /**
     * Called when the OverviewMapHandler is added to the BeanContext, and
     * whenever an object is added to the BeanContext after that. The
     * OverviewMapHandler looks for a MapBean to use as a source map, and for a
     * PropertiesHandler object to use to load itself with layers and other
     * properties. If a source MapBean is already set and another MapBean is
     * found, the last MapBean will be used as the source MapBean. Every time a
     * PropertyHandler is found, the OverviewMapHandler will reinitialize
     * itself.
     * 
     * @param someObj the object being added to the BeanContext
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof com.bbn.openmap.MapBean) {
            Debug.message("overview", "OverviewMapHandler found a MapBean object");
            setSourceMap((MapBean) someObj);
        }
    }

    /**
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapBean) {
            if (getSourceMap() == (MapBean) someObj) {
                Debug.message("overview", "OverviewMapHandler: removing source MapBean");
                setSourceMap(null);
            }
        }

        if (someObj.equals(this)) {
            dispose();
        }
    }

    /**
     * Support for directing the setCenter and setScale calls to any MapBeans
     * that care to be listening.
     */
    public class ControlledMapSupport extends ListenerSupport<MapBean> {

        /**
         * Construct a ControlledMapSupport.
         * 
         * @param aSource source Object
         */
        public ControlledMapSupport(Object aSource) {
            super(aSource);
        }

        /**
         * Set the center coordinates on all registered listeners.
         * 
         * @param llp the new center point
         */
        public void setCenter(Point2D llp) {
            for (MapBean mapBean : this) {
                mapBean.setCenter(llp);
            }
        }

        /**
         * Set the scale on all registered listeners.
         * 
         * @param scale the new scale
         */
        public void setScale(float scale) {
            for (MapBean mapBean : this) {
                mapBean.setScale(scale);
            }
        }
    }

    /**
     * PropertyChangeListener method, to listen for the source map's background
     * changes. Act on if necessary.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName() == MapBean.BackgroundProperty && backgroundSlave) {
            map.setBckgrnd((Paint) pce.getNewValue());
        }
    }

    public void dispose() {
        controlledMaps.clear();
        map.dispose();
    }
}