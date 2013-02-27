//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/dnd/DefaultDnDCatcher.java,v $
//$RCSfile: DefaultDnDCatcher.java,v $
//$Revision: 1.6 $
//$Date: 2005/08/09 20:45:09 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.dnd;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.location.Location;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.SinkGraphic;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * DefaultDnDCatcher manages Drag and Drop events on the map.
 * 
 * Drag: When a mouseDragged event occurs, DropListenerSupport forwards it to
 * the DefaultDnDCatcher (consume() method). If it's the first mouseDragged
 * event, dragGestureRecognized is fired and drag starts.
 * 
 * Drop: Each layer in the LayerHandler listens to the drop events. When a drop
 * occurs, a list of potential targets (layers) is shown in the popup menu.
 * 
 * DefaultDnDCatcher recognizes Location as the droppable object.
 * 
 * DefaultDnDCatcher recognizes OMGraphicHandlerLayer layers as potential drop
 * targets.
 */

public class DefaultDnDCatcher extends DnDListener implements BeanContextChild,
        BeanContextMembershipListener, PropertyChangeListener, Serializable,
        ProjectionListener, LayerListener, ActionListener {

    /**
     * PropertyChangeSupport for handling listeners.
     */
    protected PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

    /**
     * BeanContextChildSupport object provides helper functions for
     * BeanContextChild interface.
     */
    protected BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport();

    /**
     * Hashtable for keeping references to potential drop targets
     */
    protected Hashtable layers = new Hashtable();

    // a reference to the MouseDelegator object in the MapHandler
    protected transient MouseDelegator md;

    // a copy of current projection
    protected transient Projection proj;

    // object that is being passed in transferable
    protected Object transferData;

    protected Point dropLocation;

    /**
     * Constructs a new DefaultDnDCatcher.
     */
    public DefaultDnDCatcher() {
        this(new DragSource());
    }

    /**
     * Constructs a new DefaultDnDCatcher given the DragSource for the
     * Component.
     * 
     * @param ds the DragSource for the Component
     */
    public DefaultDnDCatcher(DragSource ds) {
        this(ds, null);
    }

    /**
     * Construct a new DefaultDnDCatcher given the DragSource for the Component
     * c, and the Component to observe.
     * 
     * @param ds the DragSource for the Component c
     * @param c the Component to observe
     */
    public DefaultDnDCatcher(DragSource ds, Component c) {
        this(ds, c, DnDConstants.ACTION_NONE);
    }

    public DefaultDnDCatcher(DragSource ds, Component c, int act) {
        this(ds, c, act, null);
    }

    public DefaultDnDCatcher(DragSource ds, Component c, int act,
            DragGestureListener dgl) {
        super(ds, c, act, dgl);
        dragSource = getDragSource();
        dragGestureListener = new ComponentDragGestureListener(this, this);
        setSourceActions(DnDConstants.ACTION_MOVE);
    }

    /**
     * Invoked when an action from the popup menu occurs.
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        Object source = e.getSource();
        if (!(source instanceof JMenuItem))
            return;

        JMenuItem mi = (JMenuItem) source;
        String name = mi.getText();
        OMGraphicHandlerLayer targetLayer = (OMGraphicHandlerLayer) layers.get(name);

        if (targetLayer == null) {
            Debug.message("defaultdndcatcher",
                    "ERROR> DefaultDnDCatcher::actionPerformed: "
                            + "no layer found with name " + name);
            return;
        }

        targetLayer.doAction((OMGraphic) transferData,
                new OMAction(OMAction.UPDATE_GRAPHIC_MASK));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcSupport.addPropertyChangeListener(listener);
    }

    /** Method for BeanContextChild interface. */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener in_pcl) {
        pcSupport.addPropertyChangeListener(propertyName, in_pcl);
    }

    /** Method for BeanContextChild interface. */
    public void addVetoableChangeListener(String propertyName,
                                          VetoableChangeListener in_vcl) {
        beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }

    /**
     * BeanContextMembershipListener method. Called when new objects are added
     * to the parent BeanContext.
     * 
     * @param bcme event that contains an iterator that can be used to go
     *        through the new objects.
     */
    public void childrenAdded(BeanContextMembershipEvent bcme) {
        findAndInit(bcme.iterator());
    }

    /**
     * BeanContextMembershipListener method. Called when objects have been
     * removed from the parent BeanContext. The DefaultDnDCatcher looks for the
     * MapBean it is managing DnD and MouseEvents for, and any layers that may
     * be removed.
     * 
     * @param bcme event that contains an iterator that can be used to go
     *        through the removed objects.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
        Iterator it = bcme.iterator();
        while (it.hasNext()) {
            findAndUndo(it.next());
        }
    }

    /**
     * The method is invoked on mousePressed, mouseReleased, and mouseDragged
     * events that come from the MapBean through DropListenerSupport.
     * 
     * @return boolean
     * @param e java.awt.event.MouseEvent
     */
    public boolean consume(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(e);
        } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(e);
        } else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
            mouseDragged(e);
        }

        return false;
    }

    /**
     * The drag operation has terminated with a drop on this
     * <code>DropTarget</code>. This method is responsible for undertaking the
     * transfer of the data associated with the gesture. The
     * <code>DropTargetDropEvent</code> provides a means to obtain a
     * <code>Transferable</code> object that represents the data object(s) to be
     * transfered.
     * <P>
     * From this method, the <code>DropTargetListener</code> shall accept or
     * reject the drop via the acceptDrop(int dropAction) or rejectDrop()
     * methods of the <code>DropTargetDropEvent</code> parameter.
     * <P>
     * Subsequent to acceptDrop(), but not before,
     * <code>DropTargetDropEvent</code>'s getTransferable() method may be
     * invoked, and data transfer may be performed via the returned
     * <code>Transferable</code>'s getTransferData() method.
     * <P>
     * At the completion of a drop, an implementation of this method is required
     * to signal the success/failure of the drop by passing an appropriate
     * <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.
     * <P>
     * Note: The actual processing of the data transfer is not required to
     * finish before this method returns. It may be deferred until later.
     * <P>
     * 
     * @param dtde the <code>DropTargetDropEvent</code>
     */
    public void drop(java.awt.dnd.DropTargetDropEvent dtde) {

        //
        // Accept the drop and get transferable object.
        //
        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
        transferData = extractTransferData(dtde);
        dropLocation = extractDropLocation(dtde);
        dtde.dropComplete(true);

        if (transferData == null || dropLocation == null)
            return;

        JPopupMenu popup = new JPopupMenu();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                "Available Drop Targets:");

        titledBorder.setTitleColor(Color.gray);
        popup.setBorder(titledBorder);

        Border compoundborder = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(2, 2, 2, 2));

        //
        // Check whether the dropped object is of type Location
        // (has exact x and y coordinates).
        //
        if (transferData instanceof Location) {
            ((Location) transferData).setLocation(dropLocation.x,
                    dropLocation.y,
                    proj);

            OMGraphicHandlerLayer omlayer = null;
            String layer_name;
            Enumeration keys = layers.keys();

            while (keys.hasMoreElements()) {
                layer_name = keys.nextElement().toString();
                omlayer = (OMGraphicHandlerLayer) layers.get(layer_name);

                if (omlayer.isVisible()) {
                    JMenuItem menuItem = new JMenuItem(layer_name);
                    menuItem.setHorizontalTextPosition(SwingConstants.CENTER);
                    menuItem.setBorder(compoundborder);
                    menuItem.addActionListener(this);
                    popup.add(menuItem);
                }
            }

            popup.addSeparator();
        }

        JMenuItem menuItem = new JMenuItem("CANCEL");
        menuItem.setForeground(Color.red);
        menuItem.setHorizontalTextPosition(SwingConstants.CENTER);
        menuItem.setBorder(compoundborder);

        popup.add(menuItem);

        popup.setPreferredSize(new Dimension(150, (popup.getComponentCount() + 1) * 25));

        //
        // Show a popup menu of available drop targets.
        //
        popup.show(((DropTarget) dtde.getSource()).getComponent(),
                dropLocation.x,
                dropLocation.y);

    }

    /**
     * Gets the location where the drop action occurred.
     */
    private Point extractDropLocation(DropTargetDropEvent dtde) {
        if (dtde == null) {
            Debug.message("defaultdndcatcher",
                    "ERROR> BDnDC::getTransferData(): dropEvent is null");
            return null;
        }
        return dtde.getLocation();
    }

    /**
     * Gets the object that is passed in transferable in DropTargetDropEvent.
     */

    private Object extractTransferData(DropTargetDropEvent dtde) {
        if (dtde == null) {
            Debug.message("defaultdndcatcher",
                    "ERROR> DefaultDnDCatcher::getTransferData(): dropEvent is null");
            return null;
        }

        Transferable tr = dtde.getTransferable();
        try {
            return tr.getTransferData(DefaultTransferableObject.OBJECT_FLAVOR);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Called when an object should be evaluated by the DefaultDnDCatcher to see
     * if it is needed.
     */
    public void findAndInit(Object someObj) {

        if (someObj instanceof MouseDelegator)
            md = (MouseDelegator) someObj;

        if (someObj instanceof MapBean) {
            ((MapBean) someObj).addProjectionListener(this);
            setProjection(((MapBean) someObj).getProjection().makeClone());
        }

        if (someObj instanceof LayerHandler) {
            LayerHandler lh = (LayerHandler) someObj;
            lh.addLayerListener(this);
            setLayers(lh.getLayers());
        }
    }

    /**
     * Eventually gets called when the DefaultDnDCatcher is added to the
     * BeanContext, and when other objects are added to the BeanContext anytime
     * after that. The DefaultDnDCatcher looks for LayerHandler to get
     * OMGraphicHandlerLayer layers to manage Drag and Drop events for. If a
     * MapBean is added to the BeanContext while another already is in use, the
     * second MapBean will take the place of the first.
     * 
     * @param it iterator to use to go through the new objects in the
     *        BeanContext.
     */
    public void findAndInit(Iterator it) {
        while (it.hasNext()) {
            findAndInit(it.next());
        }
    }

    /**
     * Called by childrenRemoved.
     */
    public void findAndUndo(Object someObj) {

        if (someObj == md) {
            md = null;
        }

        if (someObj instanceof MapBean) {
            ((MapBean) someObj).removeProjectionListener(this);
            setProjection((Projection) null);
        }

        if (someObj instanceof LayerHandler) {
            LayerHandler lh = (LayerHandler) someObj;
            lh.removeLayerListener(this);
            setLayers((Layer[]) null);
        }
    }

    public void firePropertyChange(String property, Object oldObj, Object newObj) {

        pcSupport.firePropertyChange(property, oldObj, newObj);
    }

    /**
     * Report a vetoable property update to any registered listeners. If anyone
     * vetos the change, then fire a new event reverting everyone to the old
     * value and then rethrow the PropertyVetoException.
     * <P>
     * 
     * No event is fired if old and new are equal and non-null.
     * <P>
     * 
     * @param name The programmatic name of the property that is about to change
     * 
     * @param oldValue The old value of the property
     * @param newValue - The new value of the property
     * 
     * @throws PropertyVetoException if the recipient wishes the property change
     *         to be rolled back.
     */
    public void fireVetoableChange(String name, Object oldValue, Object newValue)
            throws PropertyVetoException {
        beanContextChildSupport.fireVetoableChange(name, oldValue, newValue);
    }

    /**
     * @return the current BeanContext associated with the JavaBean
     */
    public java.beans.beancontext.BeanContext getBeanContext() {
        return beanContextChildSupport.getBeanContext();
    }

    /**
     * Gets current projection.
     */

    public Projection getProjection() {
        return proj;
    }

    /**
     * The mouseDragged event gets interpreted as DragGestureRecognized when
     * startDrag boolean is true. After the first mouseDragged event, set
     * startDrag to false.
     * 
     */
    public void mouseDragged(MouseEvent e) {
        Debug.message("defaultdndcatcher", "mouseDragged, startDrag="
                + startDrag);
        if (startDrag) {
            startDrag = false;
            if (md.getActiveMouseMode() instanceof SelectMouseMode) {
                appendEvent(e);
                setComponent((Component) e.getSource());
                fireDragGestureRecognized(DnDConstants.ACTION_MOVE,
                        ((MouseEvent) getTriggerEvent()).getPoint());
            }
        }
    }

    /**
     * On mouseReleased, set startDrag to true in order to enable dragging.
     */

    public void mouseReleased(MouseEvent e) {
        startDrag = true;
    }

    /**
     * Invoked when there has been a fundamental change to the Map.
     * <p>
     * Layers are expected to recompute their graphics (if this makes sense),
     * and then <code>repaint()</code> themselves.
     * 
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
        setProjection(e);
    }

    /**
     * This method gets called when a bound property is changed.
     * 
     * @param evt A PropertyChangeEvent object describing the event source and
     *        the property that has changed.
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {}

    /**
     * remove a property change listener to this bean child
     */
    public void removePropertyChangeListener(
                                             String name,
                                             java.beans.PropertyChangeListener pcl) {}

    /**
     * remove a vetoable change listener to this child
     */
    public void removeVetoableChangeListener(
                                             String name,
                                             java.beans.VetoableChangeListener vcl) {}

    /**
     * A change in the value of the nesting BeanContext property of this
     * BeanContextChild may be vetoed by throwing the appropriate exception.
     * 
     * @param in_bc the new BeanContext for this object
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        if (in_bc != null) {
            in_bc.addBeanContextMembershipListener(this);
            beanContextChildSupport.setBeanContext(in_bc);
            findAndInit(in_bc.iterator());
        }
    }

    /**
     * DefaultDnDCatcher adds itself to each layer as the DropTargetListener.
     * This is needed in order to capture drop events from any layer on the map,
     * and then apply the events to the applicable layers.
     */
    public void setLayers(Layer[] allLayers) {
        // remove old layers list
        layers.clear();
        if (allLayers != null) {
            for (int i = 0; i < allLayers.length; i++) {
                // create a new drop target
                /* dropTarget = */new DropTarget(allLayers[i], DnDConstants.ACTION_MOVE, this);
                if (allLayers[i] instanceof OMGraphicHandlerLayer) {
                    Debug.message("DnDCatcher", "Layers changed");
                    // keep a reference to potential drop target
                    layers.put(allLayers[i].getName(), allLayers[i]);
                }
            }
        }
    }

    /**
     * The method is invoked when there is a change in layers property in the
     * LayerHandler.
     */

    public void setLayers(LayerEvent evt) {
        if (evt.getType() == LayerEvent.ALL) {
            setLayers(evt.getLayers());
        }
    }

    /**
     * This method lets you take the ProjectionEvent received from the MapBean,
     * and lets you know if you should do something with it. MUST to be called
     * in the projectionChanged() method of your layer, if you want to refer to
     * the projection later. If this methods returns null, you probably just
     * want to call repaint() if your layer.paint() method is ready to paint
     * what it should.
     * 
     * @param projEvent the ProjectionEvent from the ProjectionListener method.
     * @return The new Projection if it is different from the one we already
     *         have, null if is the same as the current one.
     */
    public Projection setProjection(ProjectionEvent projEvent) {
        Projection newProjection = projEvent.getProjection();

        if (!newProjection.equals(getProjection())) {
            Projection clone = newProjection.makeClone();
            setProjection(clone);
            return clone;
        } else {
            return null;
        }
    }

    /**
     * Sets the current projection.
     */

    public void setProjection(Projection projection) {
        proj = projection;
    }

    /**
     * Invoked on dragGestureRecognized in the ComponentDragGestureListener
     * class.
     * 
     */
    public void startDragAction(DragGestureEvent dge, DragSourceListener dsl) {

        // create a Transferable object here.

        // Create a location object that can be dropped on a layer.

        // dragSource.startDrag(dge,
        // getCursor(DragSource.DefaultMoveDrop), new
        // DefaultTransferableObject(new BasicLocation()), dsl);

        // SinkGraphic is a singleton object used as sample. No action
        // on a layer will be done at drop.
        dragSource.startDrag(dge,
                getCursor(DragSource.DefaultMoveDrop),
                new DefaultTransferableObject(SinkGraphic.getSharedInstance()),
                dsl);
    }
}