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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MapBean.java,v $
// $RCSfile: MapBean.java,v $
// $Revision: 1.11 $
// $Date: 2004/02/11 03:36:39 $
// $Author: bmackiew $
// 
// **********************************************************************


package com.bbn.openmap;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;
import javax.swing.*;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.event.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.LatLonPoint;

/**
 * The MapBean is the main component of the OpenMap Development Kit.
 * It is a Java Bean that manages and displays a map.  A map is
 * comprised of a projection and a list of layers, and this class has
 * methods that allow you to control the projection parameters and to
 * add and remove layers.  Layers that are part of the map receive
 * dynamic notifications of changes to the underlying view and
 * projection.
 * <p>
 * Most of the methods in the MapBean are called from the Java AWT and
 * Swing code.  These methods make the MapBean a good "Swing citizen"
 * to its parent components, and you should not need to invoke them.
 * In general there are only two reasons to call MapBean methods:
 * controlling the projection, and adding or removing layers.
 * <p>
 * When controlling the MapBean projection, simply call the method
 * that applies - setCenter, pan, zoom, etc.  NOTE: If you are setting
 * more than one parameter of the projection, it's more efficient to
 * getProjection(), directly set the parameters of the projection
 * object, and then call setProjection() with the modified projection.
 * That way, each ProjectionListener of the MapBean (each layer) will
 * only receive one projectionChanged() method call, as opposed to
 * receiving one for each projection adjustment.
 * <p>
 * To add or remove layers, use the add() and remove() methods that
 * the MapBean inherits from java.awt.Container.  The add() method can
 * be called with an integer that indicates its desired position in
 * the layer list.
 * <P>
 * Changing the default clipping area may cause some Layers to not be
 * drawn completely, depending on what the clipping area is set to and
 * when the layer is trying to get itself painted.  When manually
 * adjusting clipping area, make sure that when restricted clipping is
 * over that a full repaint occurs if there is a chance that another
 * layer may be trying to paint itself.
 *
 * @see Layer 
 */
public class MapBean extends JComponent
    implements ComponentListener, ContainerListener,
               ProjectionListener, PanListener,
               ZoomListener, LayerListener,
               CenterListener, SoloMapComponent 
{

    public static final String LayersProperty = "MapBean.layers";
    public static final String CursorProperty = "MapBean.cursor";
    public static final String BackgroundProperty = "MapBean.background";
    public static final String ProjectionProperty = "MapBean.projection";

    /**
     * OpenMap title.
     */
    public static final String title = "OpenMap(tm)";

    /**
     * OpenMap version.
     */
    public static final String version = "4.6";

    /**
     * Suppress the copyright message on initialization.
     * But remember, the OpenMap License says you can't do this!
     */
    public static boolean suppressCopyright = false;

    private static final boolean DEBUG_TIMESTAMP = false;
    private static final boolean DEBUG_THREAD = true;

    private static final String copyrightNotice =
        "OpenMap(tm) Version " + version + "\r\n" +
        "  Copyright (C) BBNT Solutions LLC.  All rights reserved.\r\n" +
        "  See http://openmap.bbn.com/ for details.\r\n";

    public final static float DEFAULT_CENTER_LAT = 0.0f;
    public final static float DEFAULT_CENTER_LON = 0.0f;
    //zoomed all the way out
    public final static float DEFAULT_SCALE = Float.MAX_VALUE;
    public final static int DEFAULT_WIDTH = 640;
    public final static int DEFAULT_HEIGHT = 480;

    protected int minHeight = 100;
    protected int minWidth = 100;

    protected Proj projection = new Mercator(
            new LatLonPoint(DEFAULT_CENTER_LAT, DEFAULT_CENTER_LON),
            DEFAULT_SCALE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    protected ProjectionSupport projectionSupport;
    /**
     * Layers that are removed from the MapBean are held until the
     * next projection change.  When the projection changes, they are
     * notified that they have been removed from the map.  This list
     * is kept so that toggling a layer on and off won't cause them to
     * get rid of their resources, in case the user is just creating
     * different views of the map.
     */
    protected Vector removedLayers = new Vector(0);
    /**
     * Some users may want the layers deleted immediately when they
     * are removed from the map.  This flag controls that.  The
     * default behavior is to hold a reference to a layer and actually
     * release it when the projection changes (default = true).  Set
     * to false if you want the MapBean to tell a Layer it has been
     * removed immediately when it happens.
     */
    protected boolean layerRemovalDelayed = true;
    /**
     * This vector is to let the layers know when they have been added
     * to the map.
     */
    protected Vector addedLayers = new Vector(0);
    /** 
     * The PaintListeners want to know when the map has been
     * repainted.  
     */
    protected PaintListenerSupport painters = null;
    /**
     * The background color for this particular MapBean.  If null, the
     * setting for the projection, which in turn is set in the
     * Environment class, will be used.
     */
    protected Paint background = null;
    /**
     * The MapBeanRepaintPolicy to use to handler/filter/pace layer
     * repaint() requests.  If not set, a StandardMapBeanRepaintPolicy
     * will be used, which forwards repaint requests to Swing
     * normally.
     */
    protected MapBeanRepaintPolicy repaintPolicy = null;

    public final static Color DEFAULT_BACKGROUND_COLOR = new Color(191,239,255);

    /**
     * Return the OpenMap Copyright message.
     * @return String Copyright
     */
    public static String getCopyrightMessage() {
        return copyrightNotice;
    }

    /**
     * Construct a MapBean.
     */
    public MapBean() {
        if (Debug.debugging("mapbean")) {
            debugmsg("MapBean()");
        }
        if (! suppressCopyright) {
            Debug.output(copyrightNotice);
        }

        background = DEFAULT_BACKGROUND_COLOR;

        // Don't need one for every MapBean, just the first one.
        suppressCopyright = true;

        super.setLayout(new OverlayLayout(this));
        projectionSupport = new ProjectionSupport(this);
        addComponentListener(this);
        addContainerListener(this);

        //----------------------------------------
        // In a builder tool it seems that the OverlayLayout
        // makes the MapBean fail to resize.  And since it has
        // no children by default, it has no size.  So I add
        // a null Layer here to give it a default size.
        //----------------------------------------
        if (java.beans.Beans.isDesignTime()) {
            add(new Layer() {
                public void projectionChanged (ProjectionEvent e) {}
                public Dimension getPreferredSize() {
                    return new Dimension(100, 100);
                }
                });
        }

        setPreferredSize(new Dimension(projection.getWidth(), 
                                       projection.getHeight()));
    }

    /**
     * Return a stringified representation of the MapBean.
     * @return String
     */
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    /*----------------------------------------------------------------------
     * Window System overrides
     *----------------------------------------------------------------------*/

    /**
     * Adds additional constraints on possible children components.
     * The new component must be a Layer.  This method included as
     * a good container citizen, and should not be called directly.
     * Use the add() methods inherited from java.awt.Container instead.
     * @param comp Component
     * @param constraints Object
     * @param index int location
     */
    protected final void addImpl(Component comp,
                                 Object constraints,
                                 int index) {
        if (comp instanceof Layer) {
            super.addImpl(comp, constraints, index);
        } else {
            throw new IllegalArgumentException("only Layers can be added to a MapBean");
        }
    }

    /**
     * Prevents changing the LayoutManager.
     * Don't let anyone change the LayoutManager!  This is called by
     * the parent component and should not be called directly.
     */
    public final void setLayout(LayoutManager mgr) {
        throw new IllegalArgumentException("cannot change layout of Map");
    }

    /**
     * Return the minimum size of the MapBean window.
     * Included here to be a good citizen.  
     */
    public Dimension getMinimumSize() {
        return new Dimension(minWidth, minHeight);
    }

    /**
     * Set the minimum size of the MapBean window.
     * Included here to be a good citizen.  
     */
    public void setMinimumSize(Dimension dim) {
        minWidth = (int)dim.getWidth();
        minHeight = (int)dim.getHeight();
    }

    /**
     * Get the Insets of the MapBean.
     * This returns 0-length Insets.
     * <p>
     * This makes sure that there will be no +x,+y offset when drawing
     * graphics.  This is ok since any borders around the MapBean will get
     * drawn afterwards on top.
     * @return Insets 0-length Insets
     */
    public final Insets getInsets() {
        return insets;
    }

    private final transient static Insets insets = new Insets(0,0,0,0);

    /*----------------------------------------------------------------------
     * ComponentListener implementation
     *----------------------------------------------------------------------*/

    /**
     * ComponentListener interface method.  Should not be called
     * directly.  Invoked when component has been resized, and kicks
     * off a projection change.
     * @param e ComponentEvent
     */
    public void componentResized(ComponentEvent e) {
        if (Debug.debugging("mapbean")) {
            debugmsg("Size changed: " + getWidth() + " x " + getHeight());
        }
        projection.setWidth(getWidth());
        projection.setHeight(getHeight());
        fireProjectionChanged();
    }

    /**
     * ComponentListener interface method. Should not be called
     * directly.  Invoked when component has been moved.
     * @param e ComponentEvent
     */    
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * ComponentListener interface method.  Should not be called
     * directly.  Invoked when component has been shown.
     * @param e ComponentEvent
     */
    public void componentShown(ComponentEvent e) {
    }

    /**
     * ComponentListener interface method. Should not be called
     * directly.  Invoked when component has been hidden.
     * @param e ComponentEvent
     */
    public void componentHidden(ComponentEvent e) {
    }

    /*----------------------------------------------------------------------
     * 
     *----------------------------------------------------------------------*/

    /**
     * Add a ProjectionListener to the MapBean.
     * You do not need to call this method to add layers as
     * ProjectionListeners.  This method is called for the layer when it is
     * added to the MapBean.  Use this method for other objects that you want
     * to know about the MapBean's projection.
     * @param l ProjectionListener
     */
    public synchronized void addProjectionListener(ProjectionListener l) {
        projectionSupport.addProjectionListener(l);
        // Assume that it wants the current projection
        l.projectionChanged(new ProjectionEvent(this, getProjection()));
    }

    /**
     * Remove a ProjectionListener from the MapBean.
     * You do not need to call this method to remove layers that are
     * ProjectionListeners.  This method is called for the layer when it is
     * removed from the MapBean.  Use this method for other objects that you
     * want to remove from receiving projection events.
     * @param l ProjectionListener
     */
    public synchronized void removeProjectionListener(ProjectionListener l) {
        projectionSupport.removeProjectionListener(l);
    }

    /**
     * Called from within the MapBean when its projection listeners
     * need to know about a projection change.
     */
    protected void fireProjectionChanged() {
        // Fire the property change, so the messages get cleared out.
        // Then, if any of the layers have a problem with their new
        // projection, their messages will be displayed.
        firePropertyChange(ProjectionProperty, null, getProjection());
        projectionSupport.fireProjectionChanged(getProjection());
        purgeAndNotifyRemovedLayers();
    }

    /**
     * Clear the vector containing all of the removed layers, and let
     * those layers know they have been removed from the map.
     */
    public void purgeAndNotifyRemovedLayers() {
        // Tell any layers that have been removed that they have 
        // been removed
        if (removedLayers.size() == 0) {
            return;
        }
        for (int i=0; i<removedLayers.size(); i++) {
            Layer l = ((Layer)removedLayers.elementAt(i));
            l.removed(this);
        }
        removedLayers.removeAllElements();

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
     * @return float the current scale of the map
     * @see Projection#getScale
     */
    public float getScale() {
        return projection.getScale();
    }

    /**
     * Sets the scale of the map.
     * The Projection may silently disregard this setting, setting it to a
     * <strong>maxscale</strong> or <strong>minscale</strong> value.
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
    public LatLonPoint getCenter() {
        return projection.getCenter();
    }

    /**
     * Sets the center of the map.
     * @param newCenter the center point of the map
     * @see Proj#setCenter(LatLonPoint)
     */
    public void setCenter(LatLonPoint newCenter) {
        projection.setCenter(newCenter);
        fireProjectionChanged();
    }

    /**
     * Sets the center of the map.
     * @param lat the latitude of center point of the map in decimal degrees
     * @param lon the longitude of center point of the map in decimal degrees
     * @see Proj#setCenter(float, float)
     */
    public void setCenter(float lat, float lon) {
        projection.setCenter(lat, lon);
        fireProjectionChanged();
    }

    /**
     * Get the type of the projection.
     *
     * @return int type
     * @see Projection#getProjectionType
     */
    public int getProjectionType() {
        return projection.getProjectionType();
    }

    /**
     * Set the type of the projection.
     * If different from the current type, this installs a new
     * projection with the same center, scale, width, and height of
     * the previous one.
     * @param newType the new projection type
     */
    public void setProjectionType(int newType) {
        int oldType = projection.getProjectionType();
        if (oldType != newType) {
            LatLonPoint ctr = projection.getCenter();
            setProjection((Proj)(ProjectionFactory.makeProjection(newType,
                                                                  ctr.getLatitude(),
                                                                  ctr.getLongitude(),
                                                                  projection.getScale(),
                                                                  projection.getWidth(),
                                                                  projection.getHeight()
                                                                  )));
        }
    }

    /**
     * Set the background color of the map. If the background for this
     * MapBean is not null, the background of the projection will be
     * used.
     *
     * @param color java.awt.Color.  
     */
    public void setBackgroundColor(Color color) {
        setBackground(color);
    }

    public void setBackground(Color color) {
        super.setBackground(color);
        setBckgrnd((Paint)color);
    }

    /**
     * Set the background of the map. If the background for this
     * MapBean is not null, the background of the projection will be
     * used.
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
     * Get the background color of the map. If the background color
     * for this MapBean has been explicitly set, that value will be
     * returned. Otherwise, the background color of the projection
     * will be returned.  If the background is not a color (as opposed
     * to Paint) this method will return null.
     *
     * @return color java.awt.Color.  
     */
    public Color getBackground() {
        Paint ret = getBckgrnd();
        if (ret instanceof Color) {
            return (Color)ret;
        }

        return super.getBackground();
    }

    /**
     * Get the background of the map. If the background for this
     * MapBean has been explicitly set, that value will be
     * returned. Otherwise, the background of the projection
     * will be returned.
     *
     * @return color java.awt.Color.  
     */
    public Paint getBckgrnd() {
        Paint ret = background;
        if (ret == null) {
//          ret = projection.getBackgroundColor();
            ret = super.getBackground();
        }
        return ret;
    }

    /**
     * Get the projection property.
     * @return Projection
     */
    public Projection getProjection() {
        return projection;
    }
    
    /**
     * Set the projection.  Shouldn't be null, and won't do anything if it is.
     * @param aProjection Projection
     */
    public void setProjection(Projection aProjection) {
        if (aProjection != null) {
            setBufferDirty(true);
            projection = (Proj)aProjection;
            setPreferredSize(new Dimension(projection.getWidth(), projection.getHeight()));
            fireProjectionChanged();
        }
    }

    //------------------------------------------------------------
    // CenterListener interface
    //------------------------------------------------------------

    /**
     * Handles incoming <code>CenterEvents</code>.
     *
     * @param evt the incoming center event
     */
    public void center(CenterEvent evt) {
        setCenter(evt.getLatitude(), evt.getLongitude());
    }

    //------------------------------------------------------------
    // PanListener interface
    //------------------------------------------------------------

    /**
     * Handles incoming <code>PanEvents</code>.
     *
     * @param evt the incoming pan event
     */
    public void pan(PanEvent evt) {
        if (Debug.debugging("mapbean")) {
            debugmsg("PanEvent: " + evt);
        }
        float az = evt.getAzimuth();
        float c = evt.getArcDistance();
        if (Float.isNaN(c)) {
            projection.pan(az);
        } else {
            projection.pan(az, c);
        }

        fireProjectionChanged();
    }

    //------------------------------------------------------------
    // ZoomListener interface
    //------------------------------------------------------------

    /**
     * Zoom the Map.
     * Part of the ZoomListener interface.  Sets the scale of the
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

    //------------------------------------------------------------
    // ContainerListener interface
    //------------------------------------------------------------

    protected transient Layer[] currentLayers = new Layer[0];

    protected transient boolean doContainerChange = true;

    /**
     * ContainerListener Interface method.
     * Should not be called directly.  Part of the ContainerListener
     * interface, and it's here to make the MapBean a good Container
     * citizen. 
     * @param value boolean
     */
    public void setDoContainerChange(boolean value) {
        // if changing from false to true, call changeLayers()
        if (!doContainerChange &&  value) {
            doContainerChange = value;
            changeLayers(null);
        } else {
            doContainerChange = value;
        }
    }

    /**
     * ContainerListener Interface method.
     * Should not be called directly.  Part of the ContainerListener
     * interface, and it's here to make the MapBean a good Container
     * citizen. 
     * @return boolean
     */
    public boolean getDoContainerChange() {
        return doContainerChange;
    }

    /**
     * ContainerListener Interface method.
     * Should not be called directly.  Part of the ContainerListener
     * interface, and it's here to make the MapBean a good Container
     * citizen. 
     * @param e ContainerEvent
     */
    public void componentAdded(ContainerEvent e) {
        // Blindly cast.  addImpl has already checked to be
        // sure the child is a Layer.
        addProjectionListener((Layer)e.getChild());

        // If the new layer is in the queue to have removed() called
        // on it take it off the queue, and don't add it to the
        // added() queue (it doesn't know that it was removed, yet).
        // Otherwise, add it to the queue to have added() called on
        // it.
        if (!removedLayers.removeElement(e.getChild())) {
            addedLayers.addElement(e.getChild());
        }
        changeLayers(e);
    }

    /**
     * ContainerListener Interface method.  Should not be called
     * directly.  Part of the ContainerListener interface, and it's
     * here to make the MapBean a good Container citizen. Layers that
     * are removed are added to a list, which is cleared when the
     * projection changes.  If they are added to the MapBean again
     * before the projection changes, they are taken off the list,
     * added back to the MapBean, and are simply repainted.  This
     * prevents layers from doing unnecessary work if they are toggled
     * on and off without projection changes.
     * @param e ContainerEvent 
     * @see com.bbn.openmap.MapBean#purgeAndNotifyRemovedLayers
     */
    public void componentRemoved(ContainerEvent e) {
        // Blindly cast.  addImpl has already checked to be
        // sure the child is a Layer.
        removeProjectionListener((Layer)e.getChild());
        removedLayers.addElement(e.getChild());
        changeLayers(e);
    }

    /**
     * ContainerListener Interface method.
     * Should not be called directly.  Part of the ContainerListener
     * interface, and it's here to make the MapBean a good Container
     * citizen. 
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
        if (Debug.debugging("mapbean"))
            debugmsg("changeLayers() - firing change");
        firePropertyChange(LayersProperty, currentLayers, newLayers);

        // Tell the new layers that they have been added
        for (int i=0; i<addedLayers.size(); i++) {
            ((Layer)addedLayers.elementAt(i)).added(this);
        }
        addedLayers.removeAllElements();

        currentLayers = newLayers;

    }

    //------------------------------------------------------------
    // ProjectionListener interface
    //------------------------------------------------------------

    /**
     * ProjectionListener interface method.
     * Should not be called directly.
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
        Projection newProj = e.getProjection();
        if (! projection.equals(newProj)) {
            setProjection(newProj);
        }
    }

    /**
     * Set the Mouse cursor over the MapBean component.
     * @param newCursor Cursor
     */
    public void setCursor(Cursor newCursor){
        firePropertyChange(CursorProperty, this.getCursor(), newCursor);
        super.setCursor(newCursor);
    }

    /**
     * In addition to adding the PropertyChangeListener as the
     * JComponent method does, this method also provides the listener
     * with the initial version of the Layer and Cursor properties. 
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        super.addPropertyChangeListener(pcl);
        pcl.propertyChange(new PropertyChangeEvent(this, LayersProperty, 
                                                   currentLayers, currentLayers));
        pcl.propertyChange(new PropertyChangeEvent(this, CursorProperty, 
                                                   this.getCursor(), this.getCursor()));
        pcl.propertyChange(new PropertyChangeEvent(this, BackgroundProperty,
                                                   this.getBckgrnd(), 
                                                   this.getBckgrnd()));
    }

    protected final void debugmsg(String msg) {
        Debug.output(this.toString() + 
                     (DEBUG_TIMESTAMP?(" [" + System.currentTimeMillis() + "]"):"") +
                     (DEBUG_THREAD?(" [" + Thread.currentThread() + "]"):"") +
                     ": " + msg);
    }

    /**
     * Same as JComponent.paint(), except if there are no children
     * (Layers), the projection still paints the background and the
     * border is painted.
     */
    public void paint(Graphics g) {
        if (getComponentCount() == 0 && projection != null) {
            drawProjectionBackground(g);
            paintBorder(g);
        } else {
            super.paint(g);
        }
    }

    /**
     * Convenience method to test if Graphics is Graphics2D object,
     * and to try to do the right thing.
     */
    protected void drawProjectionBackground(Graphics g) {
        if (g instanceof Graphics2D) {
            projection.drawBackground((Graphics2D)g, getBckgrnd());
        } else {
            g.setColor(getBackground());
            projection.drawBackground(g);
        }
    }

    /**
     * Same as JComponent.paintChildren() except any PaintListeners
     * are notified and the border is painted over the children.
     */
    public void paintChildren(Graphics g) {
        paintChildren(g, null);
    }
        
    /**
     * Same as paintChildren, but allows you to set a clipping area
     * to paint.  Be careful with this, because if the clipping area
     * is set while some layer decides to paint itself, that layer may
     * not have all it's objects painted.
     */
    public void paintChildren(Graphics g, Rectangle clip) {

        g = getMapBeanRepaintPolicy().modifyGraphicsForPainting(g);

        if (clip != null) {
            g.setClip(clip);
        } else {
            // Had to do this to make the DrawingTool happy, or
            // anything else swing-like that wanted to be around or on
            // top of the MapBean.
            g.setClip(0, 0, getWidth(), getHeight());
        }

        drawProjectionBackground(g);
        super.paintChildren(g);

        // Take care of the PaintListeners...
        if (painters != null) {
            painters.paint(g);
        }

        // border gets overwritten accidentally, so redraw it now    
        paintBorder(g);
    }

    /**
     * Method that provides an option of whether or not to draw the
     * border when painting.  Usually called from another object
     * trying to control the Map appearance when events are flying
     * around.
     */
    public void paintChildrenWithBorder(Graphics g, boolean drawBorder) {
        drawProjectionBackground(g);
        if (drawBorder) {
            paintChildren(g);
        } else {
            super.paintChildren(g);
        }
    }

    /**
     * Add a PaintListener.
     * @param l PaintListener
     */
    public synchronized void addPaintListener(PaintListener l) {
        if (painters == null) {
            painters = new PaintListenerSupport(this);
        }
        painters.addPaintListener(l);
    }

    /**
     * Remove a PaintListener.
     * @param l PaintListener
     */
    public synchronized void removePaintListener(PaintListener l) {
        if (painters == null) {
            return;
        }
        painters.removePaintListener(l);

        // Should we get rid of the support if there are no painters?
        // The support will get created when a listener is added.
        if (painters.size() == 0) {
            painters = null;
        }
    }

    //------------------------------------------------------------
    // LayerListener interface
    //------------------------------------------------------------

    /**
     * LayerListener interface method.
     * A list of layers will be added, removed, or replaced based on
     * on the type of LayerEvent.
     * @param evt a LayerEvent
     */
    public void setLayers(LayerEvent evt) {
        Layer[] layers = evt.getLayers();
        int type = evt.getType();

        if (type == LayerEvent.ALL) {
            // Don't care about these at all...
            return;
        }

        // @HACK is this cool?:
        if (layers == null) {
            System.err.println("MapBean.setLayers(): layers is null!");
            return;
        }

        boolean oldChange = getDoContainerChange();
        setDoContainerChange(false);

        // use LayerEvent.REPLACE when you want to remove all current layers
        // add a new set
        if (type==LayerEvent.REPLACE) {
            if (Debug.debugging("mapbean")) {
                debugmsg("Replacing all layers");
            }
            removeAll();
            
            for (int i=0; i<layers.length; i++) {
                // @HACK is this cool?:
                if (layers[i] == null) {
                    System.err.println("MapBean.setLayers(): layer " + 
                                       i + " is null");
                    continue;
                }

                if (Debug.debugging("mapbean")) {
                    debugmsg("Adding layer[" +i+"]= " +layers[i].getName());
                }
                add(layers[i]);
                layers[i].setVisible(true);
            }

        }

        // use LayerEvent.ADD when adding and/or reshuffling layers
        else if (type == LayerEvent.ADD) {
            if (Debug.debugging("mapbean")) {
                debugmsg("Adding new layers");
            }
            for (int i=0; i<layers.length; i++) {
                if (Debug.debugging("mapbean")) {
                    debugmsg("Adding layer[" +i+"]= " +layers[i].getName());
                }
                add(layers[i]);
                layers[i].setVisible(true);
            }
        }
        
        // use LayerEvent.REMOVE when you want to delete layers from the map
        else if (type == LayerEvent.REMOVE) {
            if (Debug.debugging("mapbean")) {
                debugmsg("Removing layers");
            }
            for (int i=0; i<layers.length; i++) {
                if (Debug.debugging("mapbean")) {
                    debugmsg("Removing layer[" +i+"]= " +layers[i].getName());
                }
                remove(layers[i]);
            }
        }

        if (!layerRemovalDelayed) {
            purgeAndNotifyRemovedLayers();
        }

        setDoContainerChange(oldChange);
        repaint();
        revalidate();
    }

    /**
     * A call to try and get the MapBean to reduce flashing by
     * controlling when repaints happen, waiting for lower layers to
     * call for a repaint(), too.  Calls shouldForwardRepaint(Layer),
     * which acts as a policy for whether to forward the repaint up
     * the Swing tree.
     */
    public void repaint(Layer layer) {
//      Debug.output(layer.getName() + " - wants a repaint()");
        getMapBeanRepaintPolicy().repaint(layer);
    }

    /**
     * Set the MapBeanRepaintPolicy used by the MapBean.  This policy
     * can be used to pace/filter layer repaint() requests.
     */
    public void setMapBeanRepaintPolicy(MapBeanRepaintPolicy mbrp) {
        repaintPolicy = mbrp;
    }
    
    /**
     * Get the MapBeanRepaintPolicy used by the MapBean.  This policy
     * can be used to pace/filter layer repaint() requests.  If no
     * policy has been set, a StandardMapBeanRepaintPolicy will be
     * created, which simply forwards all requests.
     */
    public MapBeanRepaintPolicy getMapBeanRepaintPolicy() {
        if (repaintPolicy == null) {
            repaintPolicy = new StandardMapBeanRepaintPolicy(this);
        }
        return repaintPolicy;
    }

    /**
     * Convenience function to get the LatLonPoint representing a
     * screen location from a MouseEvent.  Returns null if the event
     * is null, or if the projection is not set in the MapBean.
     * Allocates new LatLonPoint with coordinates.
     */
    public LatLonPoint getCoordinates(MouseEvent event) {
        return getCoordinates(event, null);
    }

    /**
     * Convenience function to get the LatLonPoint representing a
     * screen location from a MouseEvent.  Returns null if the event
     * is null, or if the projection is not set in the MapBean.  Save
     * on memory allocation by sending in the LatLonPoint to fill.
     */
    public LatLonPoint getCoordinates(MouseEvent event, LatLonPoint llp) {
        Projection proj = getProjection();
        if (proj == null || event == null) {
            return null;
        }

        if (llp == null) {
            return proj.inverse(event.getX(), event.getY());
        } else {
            return proj.inverse(event.getX(), event.getY(), llp);
        }
    }

    /**
     * Interface-like method to query if the MapBean is buffered, so
     * you can control behavior better.  Allows the removal of
     * specific instance-like quieries for, say, BufferedMapBean, when
     * all you really want to know is if you have the data is
     * buffered, and if so, should be buffer be cleared.  For the
     * MapBean, always false.
     */
    public boolean isBuffered() {
        return false;
    }

    /**
     * Interface-like method to set a buffer dirty, if there is one.
     * In MapBean, there isn't.
     * @param value boolean
     */
    public void setBufferDirty(boolean value) {}

    /**
     * Checks whether the image buffer should be repainted.
     * @return boolean whether the layer buffer is dirty. Always 
     * true for MapBean, because a paint is always gonna 
     * need to happen.
     */
    public boolean isBufferDirty() {
        return true;
    }

    /**
     * If true (default) layers are held when they are removed, and
     * then released and notified of removal when the projection
     * changes.  This saves the layers from releasing resources if the
     * layer is simply being toggled on/off for different map views.
     */
    public void setLayerRemovalDelayed(boolean set) {
        layerRemovalDelayed = set;
    }

    /**
     * Return the flag for delayed layer removal.
     */
    public boolean isLayerRemovalDelayed() {
        return layerRemovalDelayed;
    }

    /**
     * Go through the layers, and for all of them that have the
     * autoPalette variable turned on, show their palettes.
     */
    public void showLayerPalettes() {
        Component[] comps = this.getComponents();
        for (int i = 0; i < comps.length; i++) {
            // they have to be layers
            if (((Layer)comps[i]).autoPalette) {
                ((Layer)comps[i]).showPalette();
            }
        }
    }

    /**
     * Turn off all layer palettes.
     */
    public void hideLayerPalettes() {
        Component[] comps = this.getComponents();
        for (int i = 0; i < comps.length; i++) {
            // they have to be layers
            ((Layer)comps[i]).hidePalette();
        }
    }

}
