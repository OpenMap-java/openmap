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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MapBean.java,v $
// $RCSfile: MapBean.java,v $
// $Revision: 1.23 $
// $Date: 2009/02/05 18:46:11 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.OverlayLayout;

import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.PaintListener;
import com.bbn.openmap.event.PaintListenerSupport;
import com.bbn.openmap.event.PanEvent;
import com.bbn.openmap.event.PanListener;
import com.bbn.openmap.event.ProjectionChangeVetoException;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.ProjectionSupport;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.event.ZoomListener;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * The MapBean is the main component of the OpenMap Development Kit. It is a
 * Java Bean that manages and displays a map. A map is comprised of a projection
 * and a list of layers, and this class has methods that allow you to control
 * the projection parameters and to add and remove layers. Layers that are part
 * of the map receive dynamic notifications of changes to the underlying view
 * and projection.
 * <p>
 * Most of the methods in the MapBean are called from the Java AWT and Swing
 * code. These methods make the MapBean a good "Swing citizen" to its parent
 * components, and you should not need to invoke them. In general there are only
 * two reasons to call MapBean methods: controlling the projection, and adding
 * or removing layers.
 * <p>
 * When controlling the MapBean projection, simply call the method that applies
 * - setCenter, pan, zoom, etc. NOTE: If you are setting more than one parameter
 * of the projection, it's more efficient to getProjection(), directly set the
 * parameters of the projection object, and then call setProjection() with the
 * modified projection. That way, each ProjectionListener of the MapBean (each
 * layer) will only receive one projectionChanged() method call, as opposed to
 * receiving one for each projection adjustment.
 * <p>
 * To add or remove layers, use the add() and remove() methods that the MapBean
 * inherits from java.awt.Container. The add() method can be called with an
 * integer that indicates its desired position in the layer list.
 * <P>
 * Changing the default clipping area may cause some Layers to not be drawn
 * completely, depending on what the clipping area is set to and when the layer
 * is trying to get itself painted. When manually adjusting clipping area, make
 * sure that when restricted clipping is over that a full repaint occurs if
 * there is a chance that another layer may be trying to paint itself.
 * <P>
 * PropertyChangeListeners and ProjectionListeners both receive notifications of
 * the projection changes, but the PropertyChangeListeners receive them first.
 * If you want to have a component that limits the MapBean's projection
 * parameters, it should be a PropertyChangeListener on the MapBean, and throw a
 * ProjectionChangeVetoException whenever a Projection setting falls outside of
 * the limits. The ProjectionChangeVetoException should hold the alternate
 * settings allowed by the listener. When a ProjectionChangeVetoException is
 * thrown, all of the PropertyChangeListeners will receive another
 * PropertyChangeEvent notification, under the MapBean.projectionVetoed property
 * name. The old value for that property will be the rejected Projection object,
 * and the new value will be the ProjectionChangeVetoException containing the
 * new suggestions. The MapBean will then apply the suggestions and launch
 * another round of projection change notifications. The ProjectionListeners
 * only receive notification of Projections that have passed through the
 * PropertyChangeListeners.
 * 
 * @see Layer
 */
public class MapBean extends JComponent
        implements ComponentListener, ContainerListener, ProjectionListener, PanListener,
        ZoomListener, LayerListener, CenterListener, SoloMapComponent {

    private static Logger logger = Logger.getLogger(MapBean.class.getName());

    public static final String LayersProperty = "MapBean.layers";

    public static final String CursorProperty = "MapBean.cursor";

    public static final String BackgroundProperty = "MapBean.background";

    public static final String ProjectionProperty = "MapBean.projection";

    public static final String ProjectionVetoedProperty = "MapBean.projectionVetoed";

    /**
     * OpenMap title.
     */
    public static final String title = "OpenMap(tm)";

    /**
     * OpenMap version.
     */
    public static final String version = "5.1.15";

    /**
     * Suppress the copyright message on initialization.
     */
    public static boolean suppressCopyright = false;

    private static boolean DEBUG_TIMESTAMP = false;

    private static boolean DEBUG_THREAD = true;

    private static final String copyrightNotice = "OpenMap(tm) Version " + version + "\r\n"
            + "  Copyright (C) BBNT Solutions LLC.  All rights reserved.\r\n"
            + "  See http://openmap-java.org/ for details.\r\n";

    public final static float DEFAULT_CENTER_LAT = 0.0f;

    public final static float DEFAULT_CENTER_LON = 0.0f;

    // zoomed all the way out
    public final static float DEFAULT_SCALE = Float.MAX_VALUE;

    public final static int DEFAULT_WIDTH = 640;

    public final static int DEFAULT_HEIGHT = 480;

    protected int minHeight = 100;

    protected int minWidth = 100;

    protected Proj projection = new Mercator(new LatLonPoint.Double(DEFAULT_CENTER_LAT, DEFAULT_CENTER_LON), DEFAULT_SCALE, DEFAULT_WIDTH, DEFAULT_HEIGHT);

    protected final ProjectionSupport projectionSupport;

    /**
     * Layers that are removed from the MapBean are held until the next
     * projection change. When the projection changes, they are notified that
     * they have been removed from the map. This list is kept so that toggling a
     * layer on and off won't cause them to get rid of their resources, in case
     * the user is just creating different views of the map.
     */
    protected final Vector<Layer> removedLayers = new Vector<Layer>(0);

    /**
     * Some users may want the layers deleted immediately when they are removed
     * from the map. This flag controls that. The default behavior is to hold a
     * reference to a layer and actually release it when the projection changes
     * (default = true). Set to false if you want the MapBean to tell a Layer it
     * has been removed immediately when it happens.
     */
    protected boolean layerRemovalDelayed = true;

    /**
     * This vector is to let the layers know when they have been added to the
     * map.
     */
    protected final Vector<Layer> addedLayers = new Vector<Layer>(0);

    /**
     * The PaintListeners want to know when the map has been repainted.
     */
    protected final PaintListenerSupport painters;

    /**
     * The background color for this particular MapBean. If null, the setting
     * for the projection, which in turn is set in the Environment class, will
     * be used.
     */
    protected Paint background = null;

    /**
     * The MapBeanRepaintPolicy to use to handler/filter/pace layer repaint()
     * requests. If not set, a StandardMapBeanRepaintPolicy will be used, which
     * forwards repaint requests to Swing normally.
     */
    protected MapBeanRepaintPolicy repaintPolicy = null;
    /**
     * The angle, in radians, to rotate the map. 0.0 is north-up, clockwise is
     * positive.
     */
    protected double rotationAngle = 0;

    public final static Color DEFAULT_BACKGROUND_COLOR = new Color(191, 239, 255);

    /**
     * Return the OpenMap Copyright message.
     * 
     * @return String Copyright
     */
    public static String getCopyrightMessage() {
        return copyrightNotice;
    }

    /**
     * Construct a MapBean.
     */
    public MapBean() {
        this(true);
    }

    public MapBean(boolean useThreadedNotification) {
        if (logger.isLoggable(Level.FINE)) {
            debugmsg("MapBean()");
        }
        if (!suppressCopyright) {
            Debug.output(copyrightNotice);
        }

        background = DEFAULT_BACKGROUND_COLOR;

        // Don't need one for every MapBean, just the first one.
        suppressCopyright = true;

        super.setLayout(new OverlayLayout(this));
        projectionSupport = new ProjectionSupport(this, useThreadedNotification);
        addComponentListener(this);
        addContainerListener(this);

        painters = new PaintListenerSupport(this);

        // ----------------------------------------
        // In a builder tool it seems that the OverlayLayout
        // makes the MapBean fail to resize. And since it has
        // no children by default, it has no size. So I add
        // a null Layer here to give it a default size.
        // ----------------------------------------
        if (java.beans.Beans.isDesignTime()) {
            add(new Layer() {
                public void projectionChanged(ProjectionEvent e) {
                }

                public Dimension getPreferredSize() {
                    return new Dimension(100, 100);
                }
            });
        }

        setPreferredSize(new Dimension(projection.getWidth(), projection.getHeight()));

        DEBUG_TIMESTAMP = logger.isLoggable(Level.FINER);
        DEBUG_THREAD = logger.isLoggable(Level.FINER);
    }

    /**
     * Return a string-ified representation of the MapBean.
     * 
     * @return String representing mapbean.
     */
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * Call when getting rid of the MapBean, it releases pointers to all
     * listeners and kills the ProjectionSupport thread.
     */
    public void dispose() {
        setLayerRemovalDelayed(false);

        projectionSupport.dispose();
        painters.clear();
        addedLayers.removeAllElements();

        currentLayers = null;
        projectionFactory = null;

        removeComponentListener(this);
        removeContainerListener(this);
        removeAll();
        purgeAndNotifyRemovedLayers();
    }

    /*----------------------------------------------------------------------
     * Window System overrides
     *----------------------------------------------------------------------*/

    /**
     * Adds additional constraints on possible children components. The new
     * component must be a Layer. This method included as a good container
     * citizen, and should not be called directly. Use the add() methods
     * inherited from java.awt.Container instead.
     * 
     * @param comp Component
     * @param constraints Object
     * @param index int location
     */
    protected final void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof Layer) {
            super.addImpl(comp, constraints, index);
        } else {
            throw new IllegalArgumentException("only Layers can be added to a MapBean");
        }
    }

    /**
     * Prevents changing the LayoutManager. Don't let anyone change the
     * LayoutManager! This is called by the parent component and should not be
     * called directly.
     */
    public final void setLayout(LayoutManager mgr) {
        throw new IllegalArgumentException("cannot change layout of Map");
    }

    /**
     * Return the minimum size of the MapBean window. Included here to be a good
     * citizen.
     */
    public Dimension getMinimumSize() {
        return new Dimension(minWidth, minHeight);
    }

    /**
     * Set the minimum size of the MapBean window. Included here to be a good
     * citizen.
     */
    public void setMinimumSize(Dimension dim) {
        minWidth = (int) dim.getWidth();
        minHeight = (int) dim.getHeight();
    }

    /**
     * Get the Insets of the MapBean. This returns 0-length Insets.
     * <p>
     * This makes sure that there will be no +x,+y offset when drawing graphics.
     * This is ok since any borders around the MapBean will get drawn afterwards
     * on top.
     * 
     * @return Insets 0-length Insets
     */
    public final Insets getInsets() {
        return insets;
    }

    private final transient static Insets insets = new Insets(0, 0, 0, 0);

    /*----------------------------------------------------------------------
     * ComponentListener implementation
     *----------------------------------------------------------------------*/

    /**
     * ComponentListener interface method. Should not be called directly.
     * Invoked when component has been resized, and kicks off a projection
     * change.
     * 
     * @param e ComponentEvent
     */
    public void componentResized(ComponentEvent e) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Size changed: " + getWidth() + " x " + getHeight());
        }

        projection.setWidth(getWidth());
        projection.setHeight(getHeight());
        fireProjectionChanged();
    }

    /**
     * ComponentListener interface method. Should not be called directly.
     * Invoked when component has been moved.
     * 
     * @param e ComponentEvent
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * ComponentListener interface method. Should not be called directly.
     * Invoked when component has been shown.
     * 
     * @param e ComponentEvent
     */
    public void componentShown(ComponentEvent e) {
    }

    /**
     * ComponentListener interface method. Should not be called directly.
     * Invoked when component has been hidden.
     * 
     * @param e ComponentEvent
     */
    public void componentHidden(ComponentEvent e) {
    }

    /*----------------------------------------------------------------------
     *
     *----------------------------------------------------------------------*/

    /**
     * Add a ProjectionListener to the MapBean. You do not need to call this
     * method to add layers as ProjectionListeners. This method is called for
     * the layer when it is added to the MapBean. Use this method for other
     * objects that you want to know about the MapBean's projection.
     * 
     * @param l ProjectionListener
     */
    public synchronized void addProjectionListener(ProjectionListener l) {
        projectionSupport.add(l);
        // Assume that it wants the current projection
        try {
            l.projectionChanged(new ProjectionEvent(this, getRotatedProjection()));
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINER)) {
                logger.fine("ProjectionListener not handling projection well: "
                        + l.getClass().getName() + " : " + e.getClass().getName() + " : "
                        + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove a ProjectionListener from the MapBean. You do not need to call
     * this method to remove layers that are ProjectionListeners. This method is
     * called for the layer when it is removed from the MapBean. Use this method
     * for other objects that you want to remove from receiving projection
     * events.
     * 
     * @param l ProjectionListener
     */
    public synchronized void removeProjectionListener(ProjectionListener l) {
        projectionSupport.remove(l);
    }

    /**
     * Called from within the MapBean when its projection listeners need to know
     * about a projection change.
     */
    protected void fireProjectionChanged() {

        // This handles setting up the RotationHelper if it's needed.
        Projection proj = getRotatedProjection();

        // Fire the property change, so the messages get cleared out.
        // Then, if any of the layers have a problem with their new
        // projection, their messages will be displayed.
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MapBean firing projection: " + proj);
        }
        try {
            firePropertyChange(ProjectionProperty, null, proj);
        } catch (ProjectionChangeVetoException pcve) {
            firePropertyChange(ProjectionVetoedProperty, proj, pcve);
            pcve.updateWithParameters(this);
            return;
        }

        // Mark the layers as dirty, as a group, before notifying them of a
        // projection change. They will mark themselves clean when they call
        // repaint.
        for (Component c : getComponents()) {
            Layer l = (Layer) c;
            if (l != null) {
                // Weird, I know, but I've seen c be null and throw an
                // exception here.
                l.setReadyToPaint(false);
            }
        }

        projectionSupport.fireProjectionChanged(proj);
        purgeAndNotifyRemovedLayers();
    }

    /**
     * Clear the vector containing all of the removed layers, and let those
     * layers know they have been removed from the map.
     */
    public void purgeAndNotifyRemovedLayers() {
        // Tell any layers that have been removed that they have
        // been removed

        ArrayList<Layer> rLayers = new ArrayList<Layer>(removedLayers);
        removedLayers.clear();

        if (rLayers.isEmpty()) {
            return;
        }
        for (Layer layer : rLayers) {
            layer.removed(this);
        }

        // Shouldn't call this, but it's the only thing
        // that seems to make it work...
        // Seems to help gc'ing layers in a timely manner.
        if (Debug.debugging("helpgc")) {
            System.gc();
        }
    }

    /*----------------------------------------------------------------------
     * Properties
     *----------------------------------------------------------------------*/

    /**
     * Gets the scale of the map.
     * 
     * @return float the current scale of the map
     * @see Projection#getScale
     */
    public float getScale() {
        return projection.getScale();
    }

    /**
     * Sets the scale of the map. The Projection may silently disregard this
     * setting, setting it to a <strong>maxscale </strong> or <strong>minscale
     * </strong> value.
     * 
     * @param newScale the new scale
     * @see Proj#setScale
     */
    public void setScale(float newScale) {
        projection.setScale(newScale);
        fireProjectionChanged();
    }

    /**
     * Gets the center of the map in the form of a LatLonPoint.
     * 
     * @return the center point of the map
     * @see Projection#getCenter
     */
    public Point2D getCenter() {
        return projection.getCenter();
    }

    /**
     * Sets the center of the map.
     * 
     * @param newCenter the center point of the map
     * @see Proj#setCenter(Point2D)
     */
    public void setCenter(Point2D newCenter) {
        projection.setCenter(newCenter);
        fireProjectionChanged();
    }

    /**
     * Sets the center of the map.
     * 
     * @param lat the latitude of center point of the map in decimal degrees
     * @param lon the longitude of center point of the map in decimal degrees
     * @see Proj#setCenter(double, double)
     */
    public void setCenter(double lat, double lon) {
        projection.setCenter(new Point2D.Double(lon, lat));
        fireProjectionChanged();
    }

    /**
     * Sets the center of the map.
     * 
     * @param lat the latitude of center point of the map in decimal degrees
     * @param lon the longitude of center point of the map in decimal degrees
     * @see Proj#setCenter(double, double)
     */
    public void setCenter(float lat, float lon) {
        setCenter((double) lat, (double) lon);
    }

    /**
     * Set the background color of the map. If the background for this MapBean
     * is not null, the background of the projection will be used.
     * 
     * @param color java.awt.Color.
     */
    public void setBackgroundColor(Color color) {
        setBackground(color);
    }

    public void setBackground(Color color) {
        super.setBackground(color);
        setBckgrnd((Paint) color);
    }

    /**
     * We override this to set the paint mode on the Graphics before the border
     * is painted, otherwise we get an XOR effect in the border.
     */
    public void paintBorder(Graphics g) {
        g.setPaintMode();
        super.paintBorder(g);
    }

    /**
     * Set the background of the map. If the background for this MapBean is not
     * null, the background of the projection will be used.
     * 
     * @param paint java.awt.Paint.
     */
    public void setBckgrnd(Paint paint) {
        setBufferDirty(true);

        // Instead, do this.
        Paint oldBackground = background;
        background = paint;
        firePropertyChange(BackgroundProperty, oldBackground, background);

        repaint();
    }

    /**
     * Get the background color of the map. If the background color for this
     * MapBean has been explicitly set, that value will be returned. Otherwise,
     * the background color of the projection will be returned. If the
     * background is not a color (as opposed to Paint) this method will return
     * null.
     * 
     * @return color java.awt.Color.
     */
    public Color getBackground() {
        Paint ret = getBckgrnd();
        if (ret instanceof Color) {
            return (Color) ret;
        }

        return super.getBackground();
    }

    /**
     * Get the background of the map. If the background for this MapBean has
     * been explicitly set, that value will be returned. Otherwise, the
     * background of the projection will be returned.
     * 
     * @return color java.awt.Color.
     */
    public Paint getBckgrnd() {
        Paint ret = background;
        if (ret == null) {
            ret = super.getBackground();
        }
        return ret;
    }

    /**
     * Get the projection property, reflects the projection with no rotation.
     * 
     * @return current Projection of map.
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * @return the expanded rotated projection if map rotated, normal projection
     *         if not rotated. The rotated projection is larger than the MapBean
     *         and has extra offsets.
     */
    public Projection getRotatedProjection() {
        RotationHelper rotation = getUpdatedRotHelper();
        Projection proj = rotation != null ? rotation.getProjection() : projection;
        // Double check
        ((Proj) proj).setRotationAngle(getRotationAngle());
        return proj;
    }

    /**
     * Set the projection. Shouldn't be null, and won't do anything if it is.
     * 
     * @param aProjection Projection
     */
    public void setProjection(Projection aProjection) {
        if (aProjection != null && !aProjection.getProjectionID().contains("NaN")) {
            setBufferDirty(true);
            projection = (Proj) aProjection;
            setPreferredSize(new Dimension(projection.getWidth(), projection.getHeight()));
            fireProjectionChanged();
        }
    }

    // ------------------------------------------------------------
    // CenterListener interface
    // ------------------------------------------------------------

    /**
     * Handles incoming <code>CenterEvents</code>.
     * 
     * @param evt the incoming center event
     */
    public void center(CenterEvent evt) {
        setCenter(evt.getLatitude(), evt.getLongitude());
    }

    // ------------------------------------------------------------
    // PanListener interface
    // ------------------------------------------------------------

    /**
     * Handles incoming <code>PanEvents</code>.
     * 
     * @param evt the incoming pan event
     */
    public void pan(PanEvent evt) {
        if (logger.isLoggable(Level.FINE)) {
            debugmsg("PanEvent: " + evt);
        }
        float az = evt.getAzimuth() - (float) Math.toDegrees(rotationAngle);
        float c = evt.getArcDistance();
        if (Float.isNaN(c)) {
            projection.pan(az);
        } else {
            projection.pan(az, c);
        }

        fireProjectionChanged();
    }

    // ------------------------------------------------------------
    // ZoomListener interface
    // ------------------------------------------------------------

    /**
     * Zoom the Map. Part of the ZoomListener interface. Sets the scale of the
     * MapBean projection, based on a relative or absolute amount.
     * 
     * @param evt the ZoomEvent describing the new scale.
     */
    public void zoom(ZoomEvent evt) {
        float newScale;
        if (evt.isAbsolute()) {
            newScale = evt.getAmount();
        } else if (evt.isRelative()) {
            newScale = getScale() * evt.getAmount();
        } else {
            return;
        }
        setScale(newScale);
    }

    // ------------------------------------------------------------
    // ContainerListener interface
    // ------------------------------------------------------------

    protected transient Layer[] currentLayers = new Layer[0];

    protected transient boolean doContainerChange = true;

    /**
     * ContainerListener Interface method. Should not be called directly. Part
     * of the ContainerListener interface, and it's here to make the MapBean a
     * good Container citizen.
     * 
     * @param value boolean
     */
    public void setDoContainerChange(boolean value) {
        // if changing from false to true, call changeLayers()
        if (!doContainerChange && value) {
            doContainerChange = value;
            changeLayers(null);
        } else {
            doContainerChange = value;
        }
    }

    /**
     * ContainerListener Interface method. Should not be called directly. Part
     * of the ContainerListener interface, and it's here to make the MapBean a
     * good Container citizen.
     * 
     * @return boolean
     */
    public boolean getDoContainerChange() {
        return doContainerChange;
    }

    /**
     * ContainerListener Interface method. Should not be called directly. Part
     * of the ContainerListener interface, and it's here to make the MapBean a
     * good Container citizen.
     * 
     * @param e ContainerEvent
     */
    public void componentAdded(ContainerEvent e) {
        // Blindly cast. addImpl has already checked to be
        // sure the child is a Layer.
        Layer childLayer = (Layer) e.getChild();
        addProjectionListener(childLayer);

        // If the new layer is in the queue to have removed() called
        // on it take it off the queue, and don't add it to the
        // added() queue (it doesn't know that it was removed, yet).
        // Otherwise, add it to the queue to have added() called on
        // it.
        if (!removedLayers.removeElement(childLayer)) {
            addedLayers.addElement(childLayer);
        }
        changeLayers(e);
    }

    /**
     * ContainerListener Interface method. Should not be called directly. Part
     * of the ContainerListener interface, and it's here to make the MapBean a
     * good Container citizen. Layers that are removed are added to a list,
     * which is cleared when the projection changes. If they are added to the
     * MapBean again before the projection changes, they are taken off the list,
     * added back to the MapBean, and are simply repainted. This prevents layers
     * from doing unnecessary work if they are toggled on and off without
     * projection changes.
     * 
     * @param e ContainerEvent
     * @see com.bbn.openmap.MapBean#purgeAndNotifyRemovedLayers
     */
    public void componentRemoved(ContainerEvent e) {
        // Blindly cast. addImpl has already checked to be
        // sure the child is a Layer.
        Layer childLayer = (Layer) e.getChild();
        removeProjectionListener(childLayer);
        removedLayers.addElement(childLayer);
        changeLayers(e);
    }

    /**
     * ContainerListener Interface method. Should not be called directly. Part
     * of the ContainerListener interface, and it's here to make the MapBean a
     * good Container citizen.
     * 
     * @param e ContainerEvent
     */
    protected void changeLayers(ContainerEvent e) {
        // Container Changes can be disabled to speed adding/removing
        // multiple layers
        if (!doContainerChange) {
            return;
        }
        Component[] comps = this.getComponents();
        int ncomponents = comps.length;
        Layer[] newLayers = new Layer[ncomponents];
        System.arraycopy(comps, 0, newLayers, 0, ncomponents);
        if (logger.isLoggable(Level.FINE)) {
            debugmsg("changeLayers() - firing change");
        }
        firePropertyChange(LayersProperty, currentLayers, newLayers);

        // Tell the new layers that they have been added
        for (Layer layer : addedLayers) {
            layer.added(this);
        }
        addedLayers.removeAllElements();

        currentLayers = newLayers;

    }

    // ------------------------------------------------------------
    // ProjectionListener interface
    // ------------------------------------------------------------

    /**
     * ProjectionListener interface method. Should not be called directly.
     * 
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
        Projection newProj = e.getProjection();
        if (!projection.equals(newProj)) {
            setProjection(newProj);
        }
    }

    /**
     * Set the Mouse cursor over the MapBean component.
     * 
     * @param newCursor Cursor
     */
    public void setCursor(Cursor newCursor) {
        firePropertyChange(CursorProperty, this.getCursor(), newCursor);
        super.setCursor(newCursor);
    }

    /**
     * In addition to adding the PropertyChangeListener as the JComponent method
     * does, this method also provides the listener with the initial version of
     * the Layer and Cursor properties.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(pcl);
        pcl.propertyChange(new PropertyChangeEvent(this, LayersProperty, currentLayers, currentLayers));
        pcl.propertyChange(new PropertyChangeEvent(this, CursorProperty, this.getCursor(), this.getCursor()));
        pcl.propertyChange(new PropertyChangeEvent(this, BackgroundProperty, this.getBckgrnd(), this.getBckgrnd()));
    }

    protected final void debugmsg(String msg) {
        logger.fine(this.toString()
                + (DEBUG_TIMESTAMP ? (" [" + System.currentTimeMillis() + "]") : "")
                + (DEBUG_THREAD ? (" [" + Thread.currentThread() + "]") : "") + ": " + msg);
    }

    /**
     * Same as JComponent.paint(), except if there are no children (Layers), the
     * projection still paints the background and the border is painted.
     */
    public void paint(Graphics g) {
        if (projection != null) {
            drawProjectionBackground(g);
        }

        if (this.getComponentCount() > 0) {
            paintChildren(g, null);
        }

        paintPainters(g);

        // Border gets painted over by printChildren with special layer
        // handling.
        paintBorder(g);
    }

    /**
     * Convenience method to test if Graphics is Graphics2D object, and to try
     * to do the right thing.
     */
    protected void drawProjectionBackground(Graphics g) {
        if (g instanceof Graphics2D) {
            projection.drawBackground((Graphics2D) g, getBckgrnd());
        } else {
            g.setColor(getBackground());
            projection.drawBackground(g);
        }
    }

    /**
     * Same as JComponent.paintChildren() except any PaintListeners are notified
     * and the border is painted over the children.
     */
    public void paintChildren(Graphics g) {
        paintChildren(g, null);
        paintPainters(g);
    }

    public void paintPainters(Graphics g) {
        // Just want a quick, non-changing handle on the helper. Don't need to
        // configure it.
        RotationHelper rotationHelper = getRotHelper();

        if (rotationHelper != null) {
            rotationHelper.paintPainters(g);
        } else {
            painters.paint(g);
        }
    }

    /**
     * Same as paintChildren, but allows you to set a clipping area to paint. Be
     * careful with this, because if the clipping area is set while some layer
     * decides to paint itself, that layer may not have all it's objects
     * painted.
     */
    public void paintChildren(Graphics g, Rectangle clip) {

        g = getMapBeanRepaintPolicy().modifyGraphicsForPainting(g);

        drawProjectionBackground(g);
        RotationHelper rotationHelper = getRotHelper();
        if (rotationHelper != null) {
            rotationHelper.paintChildren(g, clip);
        } else {
            // Normal painting
            super.paintChildren(g);
        }
    }

    /**
     * A method that grabs the component list of the MapBean, and renders just
     * the layers from back to front. No clipping is set, other than what is set
     * on the Graphics object.
     * 
     * @param g Graphics
     */
    protected void paintLayers(Graphics g) {
        synchronized (getTreeLock()) {
            int i = getComponentCount() - 1;
            if (i < 0) {
                return;
            }

            for (; i >= 0; i--) {
                Component comp = getComponent(i);

                final boolean isLayer = comp instanceof Layer;

                if (isLayer && comp.isVisible()) {
                    comp.paint(g);
                }
            }
        }
    }

    public Graphics getGraphics(boolean rotateIfSet) {
        RotationHelper rotationHelper = getRotHelper();
        if (rotateIfSet && rotationHelper != null) {
            return rotationHelper.getGraphics();
        }

        return super.getGraphics();
    }

    /**
     * Method that provides an option of whether or not to draw the border when
     * painting. Usually called from another object trying to control the Map
     * appearance when events are flying around.
     */
    public void paintChildrenWithBorder(Graphics g, boolean drawBorder) {
        paintChildren(g);
        if (drawBorder) {
            paintBorder(g);
        }
    }

    /**
     * Add a PaintListener.
     * 
     * @param l PaintListener
     */
    public synchronized void addPaintListener(PaintListener l) {
        painters.add(l);
    }

    /**
     * Remove a PaintListener.
     * 
     * @param l PaintListener
     */
    public synchronized void removePaintListener(PaintListener l) {
        painters.remove(l);
    }

    // ------------------------------------------------------------
    // LayerListener interface
    // ------------------------------------------------------------

    /**
     * LayerListener interface method. A list of layers will be added, removed,
     * or replaced based on on the type of LayerEvent.
     * 
     * @param evt a LayerEvent
     */
    public void setLayers(LayerEvent evt) {
        setBufferDirty(true);
        Layer[] layers = evt.getLayers();
        int type = evt.getType();

        if (type == LayerEvent.ALL) {
            // Don't care about these at all...
            return;
        }

        // @HACK is this cool?:
        if (layers == null) {
            if (logger.isLoggable(Level.FINE)) {
                debugmsg("MapBean.setLayers(): layers is null!");
            }
            return;
        }

        boolean oldChange = getDoContainerChange();
        setDoContainerChange(false);

        // use LayerEvent.REPLACE when you want to remove all current
        // layers add a new set
        if (type == LayerEvent.REPLACE) {
            if (logger.isLoggable(Level.FINE)) {
                debugmsg("Replacing all layers");
            }
            removeAll();

            for (Layer layer : layers) {

                if (layer == null) {
                    if (logger.isLoggable(Level.FINE)) {
                        debugmsg("MapBean.setLayers(): skipping null layer from being added to MapBean");
                    }
                    continue;
                }

                if (logger.isLoggable(Level.FINE)) {
                    debugmsg("Adding layer[" + layer.getName() + "]");
                }
                add(layer);
                layer.setVisible(true);
            }

        }

        // use LayerEvent.ADD when adding and/or reshuffling layers
        else if (type == LayerEvent.ADD) {
            if (logger.isLoggable(Level.FINE)) {
                debugmsg("Adding new layers");
            }
            for (Layer layer : layers) {
                if (logger.isLoggable(Level.FINE)) {
                    debugmsg("Adding layer[" + layer.getName() + "]");
                }
                add(layer);
                layer.setVisible(true);
            }
        }

        // use LayerEvent.REMOVE when you want to delete layers from
        // the map
        else if (type == LayerEvent.REMOVE) {
            if (logger.isLoggable(Level.FINE)) {
                debugmsg("Removing layers");
            }
            for (Layer layer : layers) {
                if (logger.isLoggable(Level.FINE)) {
                    debugmsg("Removing layer[" + layer.getName() + "]");
                }
                remove(layer);
            }
        }

        if (!layerRemovalDelayed) {
            purgeAndNotifyRemovedLayers();
        }

        setDoContainerChange(oldChange);
        revalidate();
        repaint();
    }

    /**
     * A call to try and get the MapBean to reduce flashing by controlling when
     * repaints happen, waiting for lower layers to call for a repaint(), too.
     * Calls shouldForwardRepaint(Layer), which acts as a policy for whether to
     * forward the repaint up the Swing tree.
     */
    public void repaint(Layer layer) {
        setBufferDirty(true);
        if (logger.isLoggable(Level.FINER)) {
            String name = layer.getName();
            logger.finer((name == null ? layer.getClass().getName() : name)
                    + " - wants a repaint()");
        }
        getMapBeanRepaintPolicy().repaint(layer);
    }

    /**
     * Set the MapBeanRepaintPolicy used by the MapBean. This policy can be used
     * to pace/filter layer repaint() requests.
     */
    public void setMapBeanRepaintPolicy(MapBeanRepaintPolicy mbrp) {
        repaintPolicy = mbrp;
    }

    /**
     * Get the MapBeanRepaintPolicy used by the MapBean. This policy can be used
     * to pace/filter layer repaint() requests. If no policy has been set, a
     * StandardMapBeanRepaintPolicy will be created, which simply forwards all
     * requests.
     */
    public MapBeanRepaintPolicy getMapBeanRepaintPolicy() {
        if (repaintPolicy == null) {
            repaintPolicy = new StandardMapBeanRepaintPolicy(this);
        }
        return repaintPolicy;
    }

    /**
     * Convenience function to get the LatLonPoint representing a screen
     * location from a MouseEvent. Returns null if the event is null, or if the
     * projection is not set in the MapBean. Allocates new LatLonPoint with
     * coordinates. Takes rotation set on MapBean into account.
     */
    public Point2D getCoordinates(MouseEvent event) {
        return getCoordinates(event, null);
    }

    /**
     * Convenience function to get the LatLonPoint representing a screen
     * location from a MouseEvent. Returns null if the event is null, or if the
     * projection is not set in the MapBean. Save on memory allocation by
     * sending in the LatLonPoint to fill. Takes rotation set on MapBean into
     * account.
     */
    public <T extends Point2D> T getCoordinates(MouseEvent event, T llp) {
        Projection proj = getProjection();
        if (proj == null || event == null) {
            return null;
        }

        return inverse(event.getX(), event.getY(), llp);
    }

    /**
     * Convenience function to get the pixel Point2D representing a screen
     * location from a MouseEvent in the projection space (as if there is no
     * rotation set). Returns null if the event is null. This is used to talk to
     * the OMGraphics, since they don't know about the map rotation.
     */
    public Point2D getNonRotatedLocation(MouseEvent event) {
        return getNonRotatedLocation(event, null);
    }

    /**
     * Convenience function to get the pixel Point2D representing a screen
     * location from a MouseEvent in the projection space (as if there is no
     * rotation set). Returns null if the event is null. This is used to talk to
     * the OMGraphics, since they don't know about the map rotation.
     */
    public Point2D getNonRotatedLocation(MouseEvent event, Point2D pnt) {
        if (event == null) {
            return null;
        }

        if (pnt == null) {
            pnt = new Point2D.Double(event.getX(), event.getY());
        } else {
            pnt.setLocation(event.getX(), event.getY());
        }

        RotationHelper rotationHelper = getRotHelper();
        if (rotationHelper != null) {
            pnt = rotationHelper.inverseTransform(pnt, pnt);
        }

        return pnt;
    }

    /**
     * If the map has been rotated, get a shape that has been transformed into
     * the pixel space of the unrotated maps (the space the projected OMGraphics
     * know about).
     * 
     * @param shape input shape
     * @return GeneralPath for transform shape if map is rotated, the input
     *         shape if the map is not rotated.
     */
    public Shape getNonRotatedShape(Shape shape) {
        RotationHelper rotationHelper = getRotHelper();
        if (rotationHelper != null) {
            return rotationHelper.inverseTransform(shape);
        }
        return shape;
    }

    /**
     * Checks the rotation set on the MapBean and accounts for it before calling
     * inverse on the projection.
     * 
     * @param x horizontal window pixel from left side
     * @param y vertical window pixel from top
     * @param ret Point2D object returned with coordinates suitable for
     *        projection where mouse event is.
     * @return the provided T ret object, or new Point2D object from projection
     *         if ret is null.
     */
    public <T extends Point2D> T inverse(double x, double y, T ret) {
        RotationHelper rotationHelper = getRotHelper();
        return (rotationHelper == null) ? getProjection().inverse(x, y, ret)
                : rotationHelper.inverse(x, y, ret);
    }

    /**
     * Interface-like method to query if the MapBean is buffered, so you can
     * control behavior better. Allows the removal of specific instance-like
     * queries for, say, BufferedMapBean, when all you really want to know is if
     * you have the data is buffered, and if so, should be buffer be cleared.
     * For the MapBean, always false.
     */
    public boolean isBuffered() {
        return false;
    }

    /**
     * Interface-like method to set a buffer dirty, if there is one. In MapBean,
     * there isn't.
     * 
     * @param value boolean
     */
    public void setBufferDirty(boolean value) {
    }

    /**
     * Checks whether the image buffer should be repainted.
     * 
     * @return boolean whether the layer buffer is dirty. Always true for
     *         MapBean, because a paint is always gonna need to happen.
     */
    public boolean isBufferDirty() {
        return true;
    }

    /**
     * If true (default) layers are held when they are removed, and then
     * released and notified of removal when the projection changes. This saves
     * the layers from releasing resources if the layer is simply being toggled
     * on/off for different map views.
     * 
     * @param set the setting
     */
    public void setLayerRemovalDelayed(boolean set) {
        layerRemovalDelayed = set;
    }

    /**
     * @return the flag for delayed layer removal.
     */
    public boolean isLayerRemovalDelayed() {
        return layerRemovalDelayed;
    }

    /**
     * Go through the layers, and for all of them that have the autoPalette
     * variable turned on, show their palettes.
     */
    public void showLayerPalettes() {
        for (Component comp : getComponents()) {
            // they have to be layers
            Layer l = (Layer) comp;
            if (l.autoPalette) {
                l.showPalette();
            }
        }
    }

    /**
     * Turn off all layer palettes.
     */
    public void hideLayerPalettes() {
        for (Component comp : getComponents()) {
            // they have to be layers
            ((Layer) comp).hidePalette();
        }
    }

    protected ProjectionFactory projectionFactory;

    public ProjectionFactory getProjectionFactory() {
        if (projectionFactory == null) {
            projectionFactory = ProjectionFactory.loadDefaultProjections();
        }

        return projectionFactory;
    }

    public void setProjectionFactory(ProjectionFactory projFactory) {
        projectionFactory = projFactory;
    }

    protected RotationHelper rotHelper;

    /**
     * Handles all of the updating of the RotationHelper if needed, based on the
     * current rotation settings on the MapBean.
     * 
     * @return the locRotHelper, null if not needed.
     */
    protected RotationHelper getUpdatedRotHelper() {
        double rotAngle = getRotationAngle();
        Projection proj = getProjection();
        RotationHelper rotationHelper = getRotHelper();

        if (rotAngle != 0.0) {
            if (rotationHelper == null) {
                rotationHelper = new RotationHelper(rotAngle, proj);
                setRotHelper(rotationHelper);
            } else {
                rotationHelper.updateForBufferDimensions(proj);
                rotationHelper.updateAngle(rotAngle);
            }
        } else if (rotationHelper != null) {
            /*
             * Just because the angle is zero, let's check with the
             * rotationHelper. If the map is just passing through zero rotation,
             * keep it around. If we get a couple of projection changes with the
             * az set to zero, then get rid of the rotation helper.
             */
            if (rotationHelper.isStillNeeded(rotAngle)) {
                rotationHelper.updateForBufferDimensions(proj);
                rotationHelper.updateAngle(rotAngle);
            } else {
                setRotHelper(null);
                rotationHelper = null;
            }
        } // else return null rotationHelper

        return rotationHelper;
    }

    /**
     * Get the RotationHelper that assists with rotated maps.
     * 
     * @return RotationHelper, may be null if map isn't rotated.
     */
    protected RotationHelper getRotHelper() {
        return rotHelper;
    }

    /**
     * @param nRotHelper the locRotHelper to set as the current one. Disposes of
     *        the old one.
     */
    protected void setRotHelper(RotationHelper nRotHelper) {
        RotationHelper rotationHelper = this.rotHelper;
        if (rotationHelper != null) {
            rotationHelper.dispose();
        }

        this.rotHelper = nRotHelper;
    }

    /**
     * Set the rotation of the map in RADIANS.
     * 
     * @param angle radians of rotation, increasing clockwise.
     */
    public void setRotationAngle(double angle) {
        setRotationAngle(angle, false);
    }

    /**
     * Set the rotation of the map in RADIANS.
     * 
     * @param angle radians of rotation, increasing clockwise.
     * @param fastRotation if true, fireProjectionChange will not be called, and
     *        the RotationHelper will be used to spin image buffer.
     */
    public void setRotationAngle(double angle, boolean fastRotation) {
        if (this.rotationAngle != angle) {
            this.rotationAngle = angle;

            /*
             * moving into this block makes rotation work faster, and smooth.
             * However, it doesn't give the non-rotating OMGraphics a chance to
             * counteract the rotation.
             */
            if (fastRotation && angle != 0) {
                /*
                 * If only the angle changes, we can just update the
                 * locRotHelper angle, and reuse all of the other settings. If
                 * the angle changes and zero is involved,either way, get the
                 * rotation helper set up in fireProjectionChanged. The
                 * RotationHelper needs to be redefined for any other projection
                 * changes anyway.
                 */
                RotationHelper locRotHelper = getRotHelper();
                if (locRotHelper != null) {
                    locRotHelper.updateAngle(angle);
                    repaint();
                    return;
                }
            }

            fireProjectionChanged();
        }
    }

    /**
     * Get the rotation of the map in RADIANS.
     * 
     * @return the angle the map has been rotated, in RADIANS, clockwise is
     *         positive.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    protected class RotationHelper {

        Image rotImage;

        double angle;
        Point2D rotCenter;
        int rotBufferHeight;
        int rotBufferWidth;
        int rotXOffset;
        int rotYOffset;
        Projection rotProjection;
        AffineTransform rotTransform;

        private RotationHelper(double angle, Projection currentProjection) {
            updateForBufferDimensions(currentProjection);
            updateAngle(angle);
        }

        /**
         * We're going to try to do buffering with a image that will cover all
         * of the corners when the map is rotated. We'll measure the ground
         * distance from the center of the projection/map to each corner, and
         * take the longest to create a bounding circle. The NSEW of that
         * bounding circle (as a bounding box) Makes up the buffered image pixel
         * bounds, and the inverse projected coordinates of that box should be
         * returned as upper left and lower right coordinates when those methods
         * are called. The projection of that box should be the same as the
         * current projection, except for the new width and height.
         * 
         * Because the height and width are different for the buffered image,
         * we're going to have to translate it before it is rotated. We can
         * probably just tack on an additional translate to the rot. That
         * difference will be 1/2 the difference of the height and width between
         * the rot image and the original projection (mapbean dimensions).
         * 
         * @param proj the projection to use to create the current image buffer
         * @return boolean true if the rotBufferHeight and/or rotBufferWidth
         *         have changed, indicating that the image buffer was recreated
         *         for new dimensions.
         */
        protected boolean updateForBufferDimensions(Projection proj) {

            int currentRotBufferWidth = rotBufferWidth;
            int currentRotBufferHeight = rotBufferHeight;

            Point2D center = proj.getCenter();
            Point2D ul = proj.getUpperLeft();
            Point2D lr = proj.getLowerRight();

            /*
             * Woooooow, we're really going to have to work it, aren't we? We
             * need to handle GeoProj differently than Cartesian coords. That
             * seems to lend itself to moving this kind of calculations to the
             * super classes of the projection classes. *sigh*
             * 
             * For now, let's try assuming that GeoProj
             */
            Geo centerGeo = new Geo(center.getY(), center.getX());
            Geo ulGeo = new Geo(ul.getY(), ul.getX());
            Geo lrGeo = new Geo(lr.getY(), lr.getX());

            // Comparing the UL and LR corners for distance, get the greatest.
            double dist = Math.max(centerGeo.distance(ulGeo), centerGeo.distance(lrGeo));

            // Now calculate the bounds of that distance in 4 directions
            Geo N = Geo.offset(centerGeo, dist, 0);
            Geo S = Geo.offset(centerGeo, dist, Math.PI);
            Geo E = Geo.offset(centerGeo, dist, Math.PI / 2.0);
            Geo W = Geo.offset(centerGeo, dist, -Math.PI / 2);

            // Calculate the coordinates of new bounds for that distance from
            // center.
            Point2D newUL = new Point2D.Double(W.getLongitude(), N.getLatitude());
            Point2D newLR = new Point2D.Double(E.getLongitude(), S.getLatitude());

            // Calculate the pixel bounds of the new bounding box to get new
            // projection h, w
            Point2D newULPix = proj.forward(newUL);
            Point2D newLRPix = proj.forward(newLR);

            int reqRotBufferHeight = (int) Math.abs(newLRPix.getY() - newULPix.getY());
            int reqRotBufferWidth = (int) Math.abs(newLRPix.getX() - newULPix.getX());

            // If the image is a little bigger than we need, we can reuse. Only
            // replace it if it is significantly bigger, or at all smaller.
            boolean needNewHeightImage = reqRotBufferHeight > currentRotBufferHeight
                    || reqRotBufferHeight < .9 * currentRotBufferHeight;
            boolean needNewWidthImage = reqRotBufferWidth > currentRotBufferWidth
                    || currentRotBufferWidth < .9 * currentRotBufferWidth;

            boolean bufferImageResized = false;

            if (needNewHeightImage || needNewWidthImage) {
                this.rotImage = new BufferedImage(reqRotBufferWidth, reqRotBufferHeight, BufferedImage.TYPE_INT_ARGB);
                rotBufferWidth = reqRotBufferWidth;
                rotBufferHeight = reqRotBufferHeight;
                bufferImageResized = true;
            }

            rotProjection = projectionFactory.makeProjection(proj.getClass(), center, proj.getScale(), rotBufferWidth, rotBufferHeight);
            this.rotCenter = rotProjection.forward(center);

            /*
             * Now calculate the different in size between the current
             * projection and the buffered image projection, and the offset
             * needed for translation for proper painting.
             */
            this.rotXOffset = (rotProjection.getWidth() - proj.getWidth()) / 2;
            this.rotYOffset = (rotProjection.getHeight() - proj.getHeight()) / 2;

            return bufferImageResized;
        }

        public void updateAngle(double angle) {
            this.angle = angle;
            this.rotTransform = AffineTransform.getRotateInstance(angle, rotCenter.getX(), rotCenter.getY());
        }

        /**
         * @param az angle to test against
         * @return true if current angle or new angle is not zero. Two zero
         *         angles in a row is an indication that the RotationHelper is
         *         no longer needed.
         */
        public boolean isStillNeeded(double az) {
            return !(az == 0.0 && angle == 0.0);
        }

        /**
         * @return the projection of the image buffer that is big enough for
         *         rotated areas.
         */
        public Projection getProjection() {
            return rotProjection;
        }

        public void paintChildren(Graphics g, Rectangle clip) {

            if (rotProjection == null) {
                // We're not properly prepared for rotation, return;
                return;
            }

            Graphics2D g2 = (Graphics2D) rotImage.getGraphics();
            ((Proj) rotProjection).drawBackground(g2, getBckgrnd());
            g2.setTransform(rotTransform);
            paintLayers(g2);
            g.drawImage(rotImage, -rotXOffset, -rotYOffset, null);
            g2.dispose();
        }

        public void paintPainters(Graphics g) {
            if (!painters.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                AffineTransform transform = AffineTransform.getTranslateInstance(-rotXOffset
                        + getX(), -rotYOffset + getY());
                transform.concatenate(rotTransform);
                g2.setTransform(transform);

                painters.paint(g2);
                g2.dispose();
            }
        }

        /**
         * @return a Graphics object from the MapBean with the rotation
         *         transform applied.
         */
        public Graphics getGraphics() {
            Graphics2D g = (Graphics2D) MapBean.super.getGraphics().create();
            g.setTransform(rotTransform);
            return g;
        }

        /**
         * Performs a projection.inverse operation that also takes into account
         * rotation.
         * 
         * @param x pixel x
         * @param y pixel y
         * @param ret T in the coordinate space of projection.
         * @return T, either ret or a new object.
         */
        public <T extends Point2D> T inverse(double x, double y, T ret) {

            Point2D pnt = new Point2D.Double(x + rotXOffset, y + rotYOffset);

            try {
                pnt = rotTransform.inverseTransform(pnt, pnt);
                return getProjection().inverse(pnt, ret);
            } catch (NoninvertibleTransformException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }

            return ret;
        }

        /**
         * Returns dst, the unrotated pixel location of the map.
         * 
         * @param src the pixel point
         * @param dst
         * @return see above.
         */
        public Point2D inverseTransform(Point2D src, Point2D dst) {
            try {
                src.setLocation(src.getX() + rotXOffset, src.getY() + rotYOffset);
                dst = rotTransform.inverseTransform(src, dst);
            } catch (NoninvertibleTransformException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
            return dst;
        }

        /**
         * Returns a transformed version of the Shape, unrotated into the
         * projected pixel space of the layer OMGraphics.
         * 
         * @param shape to transform
         * @return the transformed shape.
         */
        public Shape inverseTransform(Shape shape) {

            float[] coords = new float[6];
            GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

            PathIterator pi = shape.getPathIterator(getInverseRotationTransform());
            while (!pi.isDone()) {
                int type = pi.currentSegment(coords);

                if (type == PathIterator.SEG_MOVETO) {
                    path.moveTo(coords[0], coords[1]);
                } else if (type == PathIterator.SEG_LINETO) {
                    path.lineTo(coords[0], coords[1]);
                } else if (type == PathIterator.SEG_CLOSE) {
                    path.closePath();
                } else {
                    if (type == PathIterator.SEG_QUADTO) {
                        path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    } else if (type == PathIterator.SEG_CUBICTO) {
                        path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    }
                }

                pi.next();
            }

            return path;
        }

        public AffineTransform getInverseRotationTransform() {
            try {
                AffineTransform translateOffset = AffineTransform.getTranslateInstance(rotXOffset, rotYOffset);
                AffineTransform transform = rotTransform.createInverse();
                translateOffset.preConcatenate(transform);
                return translateOffset;
            } catch (NoninvertibleTransformException e) {
                logger.log(Level.FINE, "AffineTransform problem", e);
            }

            return new AffineTransform();
        }

        public void dispose() {
            if (rotImage != null) {
                rotImage.flush();
            }
        }
    }

}