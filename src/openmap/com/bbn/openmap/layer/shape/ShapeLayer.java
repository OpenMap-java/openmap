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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ShapeLayer.java,v $
// $RCSfile: ShapeLayer.java,v $
// $Revision: 1.22 $
// $Date: 2006/01/18 17:44:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * An OpenMap Layer that displays shape files. Note that the ESRIRecords have
 * been updated so that the OMGraphics that get created from them are loaded
 * with an Integer object that notes the number of the record as it was read
 * from the .shp file. This lets you align the object with the correct attribute
 * data in the .dbf file.
 * <p>
 * <code><pre>
 *   
 *    
 *     
 *      
 *       ############################
 *       # Properties for a shape layer
 *       shapeLayer.class=com.bbn.openmap.layer.shape.ShapeLayer
 *       shapeLayer.prettyName=Name_for_Menu
 *       shapeLayer.shapeFile=&amp;ltpath to shapefile (.shp)&amp;gt
 *       shapeLayer.spatialIndex=&amp;ltpath to generated spatial index file (.ssx)&amp;gt
 *       shapeLayer.lineColor=ff000000
 *       shapeLayer.fillColor=ff000000
 *       # plus any other properties used by the DrawingAttributes object.
 *       shapeLayer.pointImageURL=&amp;ltURL for image to use for point objects&amp;gt
 *       ############################
 *       
 *      
 *     
 *    
 * </pre></code>
 * 
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.22 $ $Date: 2006/01/18 17:44:15 $
 * @see SpatialIndex
 */
public class ShapeLayer extends OMGraphicHandlerLayer implements
        ActionListener, DataBoundsProvider {

    /** The name of the property that holds the name of the shape file. */
    public final static String shapeFileProperty = "shapeFile";

    /**
     * The name of the property that holds the name of the spatial index file.
     */
    public final static String spatialIndexProperty = "spatialIndex";

    /** The URL of an image to use for point objects. */
    public final static String pointImageURLProperty = "pointImageURL";

    // Note that shadows are really in the eye of the beholder
    // The X,Y shadow offset just pushes the resulting picture in the
    // direction of the offset and draws it there. By setting the
    // fill and line colors, you make it seem shadowy. By drawing
    // a layer as a shadow, and then again as a regular layer, you
    // get the proper effect.

    /** The name of the property that holds the offset of the shadow. */
    public final static String shadowXProperty = "shadowX";
    public final static String shadowYProperty = "shadowY";

    /** * The holders of the shadow offset. ** */
    protected int shadowX = 0;
    protected int shadowY = 0;

    /** The spatial index of the shape file to be rendered. */
    protected SpatialIndex spatialIndex;

    /**
     * The DrawingAttributes object to describe the rendering of graphics.
     */
    protected DrawingAttributes drawingAttributes = (DrawingAttributes) DrawingAttributes.DEFAULT.clone();

    // For writing out to properties file later.
    String shapeFileName = null;
    String spatialIndexFileName = null;
    String imageURLString = null;

    /**
     * Initializes an empty shape layer.
     */
    public ShapeLayer() {
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    public ShapeLayer(String shapeFileName) {
        this();
        spatialIndex = SpatialIndex.locateAndSetShapeData(shapeFileName);
    }

    public void setSpatialIndex(SpatialIndex si) {
        spatialIndex = si;
    }

    public SpatialIndex getSpatialIndex() {
        return spatialIndex;
    }

    /**
     * This method gets called from setProperties.
     * 
     * @param realPrefix This prefix has already been scoped, which means it is
     *        an empty string if setProperties was called with a null prefix, or
     *        it's a String ending with a period if it was defined with
     *        characters.
     * @param props Properties containing information about files and the layer.
     */
    protected void setFileProperties(String realPrefix, Properties props) {
        shapeFileName = props.getProperty(realPrefix + shapeFileProperty);

        spatialIndexFileName = props.getProperty(realPrefix
                + spatialIndexProperty);

        if (shapeFileName != null && !shapeFileName.equals("")) {
            SpatialIndex spatialIndex;
            if (spatialIndexFileName != null
                    && !spatialIndexFileName.equals("")) {
                spatialIndex = SpatialIndex.locateAndSetShapeData(shapeFileName,
                        spatialIndexFileName);
            } else {
                spatialIndex = SpatialIndex.locateAndSetShapeData(shapeFileName);
            }

            imageURLString = props.getProperty(realPrefix
                    + pointImageURLProperty);

            try {
                if (imageURLString != null && !imageURLString.equals("")) {
                    URL imageURL = PropUtils.getResourceOrFileOrURL(this,
                            imageURLString);
                    ImageIcon imageIcon = new ImageIcon(imageURL);
                    spatialIndex.setPointIcon(imageIcon);
                }
            } catch (MalformedURLException murle) {
                Debug.error("ShapeLayer.setFileProperties: point image URL not so good: \n\t"
                        + imageURLString);
            } catch (NullPointerException npe) {
                // May happen if not connected to the internet.
                fireRequestMessage("Can't access icon image: \n    "
                        + imageURLString);
            }

            setSpatialIndex(spatialIndex);

        } else {
            Debug.error("One of the following properties was null or empty:");
            Debug.error("\t" + realPrefix + shapeFileProperty);
            Debug.error("\t" + realPrefix + spatialIndexProperty);
        }
    }

    /**
     * Initializes this layer from the given properties.
     * 
     * @param props the <code>Properties</code> holding settings for this
     *        layer
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        String realPrefix = PropUtils.getScopedPropertyPrefix(this);

        setFileProperties(realPrefix, props);

        drawingAttributes = new DrawingAttributes(prefix, props);

        shadowX = PropUtils.intFromProperties(props, realPrefix
                + shadowXProperty, 0);
        shadowY = PropUtils.intFromProperties(props, realPrefix
                + shadowYProperty, 0);
    }

    /**
     * PropertyConsumer method.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + shapeFileProperty, (shapeFileName == null ? ""
                : shapeFileName));
        props.put(prefix + spatialIndexProperty,
                (spatialIndexFileName == null ? "" : spatialIndexFileName));
        props.put(prefix + pointImageURLProperty, (imageURLString == null ? ""
                : imageURLString));

        props.put(prefix + shadowXProperty, Integer.toString(shadowX));
        props.put(prefix + shadowYProperty, Integer.toString(shadowY));

        if (drawingAttributes != null) {
            drawingAttributes.setPropertyPrefix(getPropertyPrefix());
            drawingAttributes.getProperties(props);
        } else {
            DrawingAttributes da = (DrawingAttributes) DrawingAttributes.DEFAULT.clone();
            da.setPropertyPrefix(getPropertyPrefix());
            da.getProperties(props);
        }

        return props;
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
        list = super.getPropertyInfo(list);

        DrawingAttributes da;
        if (drawingAttributes != null) {
            da = drawingAttributes;
        } else {
            da = DrawingAttributes.DEFAULT;
        }

        da.getPropertyInfo(list);

        list.put(initPropertiesProperty, shapeFileProperty + " "
                + spatialIndexProperty + " " + pointImageURLProperty + " "
                + shadowXProperty + " " + shadowYProperty
                + da.getInitPropertiesOrder() + " " + AddToBeanContextProperty
                + " " + MinScaleProperty + " " + MaxScaleProperty);

        PropUtils.setI18NPropertyInfo(i18n,
                list,
                ShapeLayer.class,
                shapeFileProperty,
                shapeFileProperty,
                "Location of Shape file - .shp (File, CURL or relative file path).",
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                list,
                ShapeLayer.class,
                spatialIndexProperty,
                spatialIndexProperty,
                "Location of Spatial Index file - .ssx (File, URL or relative file path).",
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        
        PropUtils.setI18NPropertyInfo(i18n,
                list,
                ShapeLayer.class,
                pointImageURLProperty,
                pointImageURLProperty,
                "Image file to use for map location of point data (optional).",
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        
        PropUtils.setI18NPropertyInfo(i18n,
                list,
                ShapeLayer.class,
                shadowXProperty,
                shadowXProperty,
                "Horizontal pixel offset for shadow image for shapes.",
                null);
        
        PropUtils.setI18NPropertyInfo(i18n,
                list,
                ShapeLayer.class,
                shadowYProperty,
                shadowYProperty,
                "Vertical pixel offset for shadow image for shapes.",
                null);

        return list;
    }

    public void setDrawingAttributes(DrawingAttributes da) {
        drawingAttributes = da;
    }

    public DrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    /**
     * Create the OMGraphics using the shape file and SpatialIndex.
     * 
     * @return OMGraphicList
     * @deprecated use prepare() instead.
     */
    protected OMGraphicList computeGraphics() {
        return prepare();
    }

    /**
     * Create the OMGraphics using the shape file and SpatialIndex.
     * 
     * @return OMGraphicList
     */
    public synchronized OMGraphicList prepare() {

        if (spatialIndex == null) {
            Debug.message("shape", "ShapeLayer: spatialIndex is null!");
            return new OMGraphicList();
        }

        Projection projection = getProjection();

        if (projection == null) {
            Debug.message("basic", "ShapeLayer|" + getName()
                    + ": prepare called with null projection");
            return new OMGraphicList();
        }

        Point2D ul = projection.getUpperLeft();
        Point2D lr = projection.getLowerRight();
        double ulLat = ul.getY();
        double ulLon = ul.getX();
        double lrLat = lr.getY();
        double lrLon = lr.getX();

        OMGraphicList list = null;

        // check for dateline anomaly on the screen. we check for
        // ulLon >= lrLon, but we need to be careful of the check for
        // equality because of floating point arguments...
        if ((ulLon > lrLon)
                || MoreMath.approximately_equal(ulLon, lrLon, .001f)) {
            if (Debug.debugging("shape")) {
                Debug.output("ShapeLayer.computeGraphics(): Dateline is on screen");
            }

            double ymin = Math.min(ulLat, lrLat);
            double ymax = Math.max(ulLat, lrLat);

            try {
                ESRIRecord records1[] = spatialIndex.locateRecords(ulLon,
                        ymin,
                        180.0d,
                        ymax);
                ESRIRecord records2[] = spatialIndex.locateRecords(-180.0d,
                        ymin,
                        lrLon,
                        ymax);
                int nRecords1 = records1.length;
                int nRecords2 = records2.length;
                list = new OMGraphicList(nRecords1 + nRecords2);
                for (int i = 0; i < nRecords1; i++) {
                    records1[i].addOMGraphics(list, drawingAttributes);
                }
                for (int i = 0; i < nRecords2; i++) {
                    records2[i].addOMGraphics(list, drawingAttributes);
                }
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
                ESRIRecord records[] = spatialIndex.locateRecords(xmin,
                        ymin,
                        xmax,
                        ymax);
                int nRecords = records.length;
                list = new OMGraphicList(nRecords);
                for (int i = 0; i < nRecords; i++) {
                    records[i].addOMGraphics(list, drawingAttributes);
                }
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

        if (list != null) {
            list.generate(projection, true);// all new graphics
        }

        return list;
    }

    /**
     * Renders the layer on the map.
     * 
     * @param g a graphics context
     */
    public void paint(Graphics g) {
        if (shadowX == 0 && shadowY == 0) {
            // Enabling buffer...
            super.paint(g);
        } else {
            // grab local for thread safety
            OMGraphicList omg = getList();

            if (omg != null) {
                if (Debug.debugging("shape"))
                    Debug.output("ShapeLayer.paint(): " + omg.size() + " omg"
                            + " shadow=" + shadowX + "," + shadowY);

                if (shadowX != 0 || shadowY != 0) {
                    Graphics shadowG = g.create();
                    shadowG.translate(shadowX, shadowY);
                    omg.render(shadowG);
                } else {
                    omg.render(g);
                }

                if (Debug.debugging("shape")) {
                    Debug.output("ShapeLayer.paint(): done");
                }
            }
        }
    }

    protected transient JPanel box;

    public Component getGUI() {

        if (box == null) {

            box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel stuff = new JPanel();
            // stuff.setLayout(new BoxLayout(stuff,
            // BoxLayout.X_AXIS));
            // stuff.setAlignmentX(Component.LEFT_ALIGNMENT);

            DrawingAttributes da = getDrawingAttributes();
            if (da != null) {
                stuff.add(da.getGUI());
            }
            box.add(stuff);

            JPanel pal2 = new JPanel();
            JButton redraw = new JButton(i18n.get(ShapeLayer.class,
                    "redrawLayerButton",
                    "Redraw Layer"));
            redraw.setActionCommand(RedrawCmd);
            redraw.addActionListener(this);
            pal2.add(redraw);

            box.add(pal2);

        }
        return box;
    }

    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        String cmd = e.getActionCommand();
        if (cmd == RedrawCmd) {
            if (isVisible()) {
                doPrepare();
            }
        }
    }

    /**
     * DataBoundsInformer interface.
     */
    public DataBounds getDataBounds() {
        DataBounds box = null;
        if (spatialIndex != null) {
            ESRIBoundingBox bounds = spatialIndex.getBounds();
            if (bounds != null) {
                box = new DataBounds(bounds.min.x, bounds.min.y, bounds.max.x, bounds.max.y);
            }
        }
        return box;
    }
}