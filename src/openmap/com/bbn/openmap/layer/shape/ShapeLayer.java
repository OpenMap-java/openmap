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
// $Revision: 1.29 $
// $Date: 2008/10/16 17:58:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.dataAccess.shape.DbfHandler;
import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
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
 * ############################
 * # Properties for a shape layer
 * shapeLayer.class=com.bbn.openmap.layer.shape.ShapeLayer
 * shapeLayer.prettyName=Name_for_Menu
 * shapeLayer.shapeFile=&amp;ltpath to shapefile (.shp)&amp;gt
 * shapeLayer.spatialIndex=&amp;ltpath to generated spatial index file (.ssx)&amp;gt
 * shapeLayer.lineColor=ff000000
 * shapeLayer.fillColor=ff000000
 * # plus any other properties used by the DrawingAttributes object.
 * shapeLayer.pointImageURL=&amp;ltURL for image to use for point objects&amp;gt
 *        
 *        
 * # Optionally, Rule marker names specified in space-separated list
 * shapeLayer.rules=rule0 rule1
 * # global scale settings can be used so work is only performed within scale range of minScale/maxScale
 * shapeLayer.maxScale=1000000f
 * 
 * # rule0 definition:
 * # CLASS_RTE is a DBF column name
 * shapeLayer.rule0.key=CLASS_RTE
 * # operation, if key value is less than 2
 * shapeLayer.rule0.op=lt
 * shapeLayer.rule0.val=2
 * # If rule is met, then actions can be performed:
 * # DBF Column names can be added together in a label by specifying them in a space-separated list
 * shapeLayer.rule0.label=PREFIX PRETYPE NAME TYPE SUFFIX
 * # Labels can have scale limits imposed, so they don't appear if map scale is 
 * # greater than maxScale or less than minScale
 * shapeLayer.rule0.label.maxScale=1000000
 * # Visibility can be controlled with respect to scale as well
 * shapeLayer.rule0.render=true
 * shapeLayer.rule0.render.maxScale=1000000
 * # Rendering attributes can be specified.
 * shapeLayer.rule0.lineColor=FFFA73
 * shapeLayer.rule0.lineWidth=4
 * shapeLayer.rule0.mattingColor=55AAAAAA
 * 
 * # rule1 definition:
 * shapeLayer.rule1.key=CLASS_RTE
 * shapeLayer.rule1.op=all
 * shapeLayer.rule1.label=PREFIX PRETYPE NAME TYPE SUFFIX
 * shapeLayer.rule1.label.maxScale=200000
 * shapeLayer.rule1.render=true
 * shapeLayer.rule1.render.maxScale=500000
 * shapeLayer.rule1.lineColor=FFFFFF
 * shapeLayer.rule1.lineWidth=3
 * shapeLayer.rule1.mattingColor=55AAAAAA
 * ############################
 * 
 * </pre></code>
 * 
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.29 $ $Date: 2008/10/16 17:58:15 $
 * @see SpatialIndex
 */
public class ShapeLayer extends OMGraphicHandlerLayer implements ActionListener,
        DataBoundsProvider {

   private static final long serialVersionUID = 1L;

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.shape.ShapeLayer");

    /** The name of the property that holds the name of the shape file. */
    public final static String shapeFileProperty = "shapeFile";

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
    protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

    // For writing out to properties file later.
    String shapeFileName = null;
    String imageURLString = null;

    /**
     * Initializes an empty shape layer.
     */
    public ShapeLayer() {
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
        setRenderPolicy(new BufferedImageRenderPolicy(this));
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
     * A call-back method to override in case you want to change the BinaryFile
     * used to in the DbfHandler.
     * 
     * @param dbfFileName
     *            path to DBF file.
     * @return DbfHandler with BinaryFile set in it.
     * @throws FormatException
     * @throws IOException
     */
    protected DbfHandler createDbfHandler(String dbfFileName) throws FormatException,
            IOException {
        BinaryBufferedFile bbf = new BinaryBufferedFile(dbfFileName);
        return new DbfHandler(bbf);
    }

    /**
     * This method gets called from setProperties.
     * 
     * @param realPrefix
     *            This prefix has already been scoped, which means it is an
     *            empty string if setProperties was called with a null prefix,
     *            or it's a String ending with a period if it was defined with
     *            characters.
     * @param props
     *            Properties containing information about files and the layer.
     */
    protected void setFileProperties(String realPrefix, Properties props) {
        shapeFileName = props.getProperty(realPrefix + shapeFileProperty);
        shapeFileName = prependDataPathPrefix(shapeFileName);

        if (shapeFileName != null && shapeFileName.length() > 0) {

            spatialIndex = SpatialIndex.locateAndSetShapeData(shapeFileName);
            String dbfFileName = SpatialIndex.dbf(shapeFileName);

            try {
                if (BinaryFile.exists(dbfFileName)) {
                    DbfHandler dbfh = createDbfHandler(dbfFileName);
                    dbfh.setProperties(realPrefix, props);
                    spatialIndex.setDbf(dbfh);
                }
            } catch (FormatException fe) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.warning(getName() + ": Couldn't create DBF handler for "
                            + dbfFileName + ", FormatException: " + fe.getMessage());
                }
            } catch (IOException ioe) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.warning(getName() + ": Couldn't create DBF handler for "
                            + dbfFileName + ", IOException: " + ioe.getMessage());
                }
            }

            imageURLString = props.getProperty(realPrefix + pointImageURLProperty);

            try {
                if (imageURLString != null && imageURLString.length() > 0) {
                    URL imageURL = PropUtils.getResourceOrFileOrURL(this, imageURLString);
                    ImageIcon imageIcon = new ImageIcon(imageURL);
                    spatialIndex.setPointIcon(imageIcon);
                }
            } catch (MalformedURLException murle) {
                logger.warning(getName() + ": point image URL not so good: \n\t"
                        + imageURLString);
            } catch (NullPointerException npe) {
                // May happen if not connected to the internet.
                fireRequestMessage("Can't access icon image: \n    " + imageURLString);
            }

            setSpatialIndex(spatialIndex);

        } else {
            logger.warning(getName() + ": No Shape file was specified:\n\t" + realPrefix
                    + shapeFileProperty);
        }
    }

    /**
     * Initializes this layer from the given properties.
     * 
     * @param props
     *            the <code>Properties</code> holding settings for this layer
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        drawingAttributes.setProperties(prefix, props);

        String realPrefix = PropUtils.getScopedPropertyPrefix(this);
        setFileProperties(realPrefix, props);
        
        shadowX = PropUtils.intFromProperties(props, realPrefix + shadowXProperty, 0);
        shadowY = PropUtils.intFromProperties(props, realPrefix + shadowYProperty, 0);
    }

    /**
     * PropertyConsumer method.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + shapeFileProperty,
                  (shapeFileName == null ? "" : shapeFileName));
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

        if (spatialIndex != null) {
            DbfHandler dbfh = spatialIndex.getDbf();
            if (dbfh != null) {
                dbfh.getProperties(props);
            }
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
     * @param list
     *            a Properties object to load the PropertyConsumer properties
     *            into. If getList equals null, then a new Properties object
     *            should be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        String dummyMarker = PropUtils.getDummyMarkerForPropertyInfo(getPropertyPrefix(),
                                                                     null);

        PropUtils.setI18NPropertyInfo(
                                      i18n,
                                      list,
                                      ShapeLayer.class,
                                      dummyMarker,
                                      "Rendering Attributes",
                                      "Attributes that determine how the shapes will be drawn.",
                                      "com.bbn.openmap.omGraphics.DrawingAttributesPropertyEditor");

        list.put(initPropertiesProperty, shapeFileProperty + " " + " "
                + pointImageURLProperty + " " + shadowXProperty + " " + shadowYProperty
                + " " + dummyMarker + " " + AddToBeanContextProperty + " "
                + MinScaleProperty + " " + MaxScaleProperty);

        PropUtils.setI18NPropertyInfo(
                                      i18n,
                                      list,
                                      ShapeLayer.class,
                                      shapeFileProperty,
                                      shapeFileProperty,
                                      "Location of Shape file - .shp (File, CURL or relative file path).",
                                      "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        PropUtils.setI18NPropertyInfo(
                                      i18n,
                                      list,
                                      ShapeLayer.class,
                                      pointImageURLProperty,
                                      pointImageURLProperty,
                                      "Image file to use for map location of point data (optional).",
                                      "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        PropUtils.setI18NPropertyInfo(
                                      i18n,
                                      list,
                                      ShapeLayer.class,
                                      shadowXProperty,
                                      shadowXProperty,
                                      "Horizontal pixel offset for shadow image for shapes.",
                                      null);

        PropUtils.setI18NPropertyInfo(
                                      i18n,
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

    public String getInfoText(OMGraphic omg) {
        return (String) omg.getAttribute(OMGraphic.INFOLINE);
    }

    /**
     * If applicable, should return a tool tip for the OMGraphic. Return null if
     * nothing should be shown.
     */
    public String getToolTipTextFor(OMGraphic omg) {
        return (String) omg.getAttribute(OMGraphic.TOOLTIP);
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

        OMGraphicList list = getList();
        Projection projection = getProjection();

        if (projection == null) {
            logger.fine(getName() + ": prepare called with null projection");
            return new OMGraphicList();
        }

        if (spatialIndex == null) {
            logger.fine(getName() + ": spatialIndex is null!");

            if (list != null) {
                list.generate(projection, true);// all new graphics
                return list;
            } else {
                // What we'd really like to do is make this a buffered layer at
                // this point, if we can't find an ssx file and can't create
                // one.
                return new OMGraphicList();
            }
        }

        Point2D ul = projection.getUpperLeft();
        Point2D lr = projection.getLowerRight();
        double ulLat = ul.getY();
        double ulLon = ul.getX();
        double lrLat = lr.getY();
        double lrLon = lr.getX();

        if (list != null) {
            list.clear();
            list = new OMGraphicList();
        }

        // check for date line anomaly on the screen. we check for
        // ulLon >= lrLon, but we need to be careful of the check for
        // equality because of floating point arguments...
        if (ProjMath.isCrossingDateline(ulLon, lrLon, projection.getScale())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("ShapeLayer.computeGraphics(): Dateline is on screen");
            }

            double ymin = Math.min(ulLat, lrLat);
            double ymax = Math.max(ulLat, lrLat);

            try {

                list = spatialIndex.getOMGraphics(ulLon, ymin, 180.0d, ymax, list,
                                                  drawingAttributes, projection,
                                                  coordTransform);
                list = spatialIndex.getOMGraphics(-180.0d, ymin, lrLon, ymax, list,
                                                  drawingAttributes, projection,
                                                  coordTransform);

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

            double xmin = Math.min(ulLon, lrLon);
            double xmax = Math.max(ulLon, lrLon);
            double ymin = Math.min(ulLat, lrLat);
            double ymax = Math.max(ulLat, lrLat);

            try {
                list = spatialIndex.getOMGraphics(xmin, ymin, xmax, ymax, list,
                                                  drawingAttributes, projection,
                                                  coordTransform);
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

        return list;
    }

    /**
     * Renders the layer on the map.
     * 
     * @param g
     *            a graphics context
     */
    public void paint(Graphics g) {
        if (shadowX == 0 && shadowY == 0) {
            // Enabling buffer...
            super.paint(g);
        } else {
            // grab local for thread safety
            OMGraphicList omg = getList();

            if (omg != null) {
                if (logger.isLoggable(Level.FINE))
                    logger.fine("ShapeLayer.paint(): " + omg.size() + " omg" + " shadow="
                            + shadowX + "," + shadowY);

                if (shadowX != 0 || shadowY != 0) {
                    Graphics shadowG = g.create();
                    shadowG.translate(shadowX, shadowY);
                    omg.render(shadowG);
                } else {
                    omg.render(g);
                }

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("ShapeLayer.paint(): done");
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
            JLabel label  = new JLabel(i18n.get(ShapeLayer.class, "rendering_attributes", "Rendering Attributes: "));
            stuff.add(label);
            // stuff.setLayout(new BoxLayout(stuff,
            // BoxLayout.X_AXIS));
            // stuff.setAlignmentX(Component.LEFT_ALIGNMENT);

            DrawingAttributes da = getDrawingAttributes();
            if (da != null) {
                stuff.add(da.getGUI());
            }
            box.add(stuff);

            JPanel pal2 = new JPanel();
            JButton redraw = new JButton(i18n.get(ShapeLayer.class, "redrawLayerButton",
                                                  "Redraw Layer"));
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
        if (spatialIndex != null) {
            ESRIBoundingBox bounds = spatialIndex.getBounds();
            if (bounds != null) {
                box = new DataBounds(bounds.min.x, bounds.min.y, bounds.max.x,
                        bounds.max.y);
            }
        }
        return box;
    }

    /**
     * Called when the Layer is removed from the MapBean, giving an opportunity
     * to clean up.
     */
    public void removed(Container cont) {
        OMGraphicList list = getList();
        if (list != null) {
            list.clear();
            list = null;
        }
        
        if (spatialIndex != null) {
           spatialIndex.close(true);
        }

    }
}