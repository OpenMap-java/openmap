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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MouseDelegator.java,v $
// $RCSfile: MouseDelegator.java,v $
// $Revision: 1.7 $
// $Date: 2005/12/16 14:14:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.event.NullMouseMode;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.util.Debug;

/**
 * The MouseDelegator manages the MapMouseModes that handle MouseEvents on the
 * map. There should only be one MouseDelegator within a MapHandler.
 * 
 * @see com.bbn.openmap.event.MapMouseMode
 * @see com.bbn.openmap.event.AbstractMouseMode
 * @see com.bbn.openmap.event.NavMouseMode
 * @see com.bbn.openmap.event.SelectMouseMode
 */
public class MouseDelegator
      implements PropertyChangeListener, java.io.Serializable, BeanContextChild, BeanContextMembershipListener, SoloMapComponent {

   private static final long serialVersionUID = 1L;
   public final static transient String ActiveModeProperty = "NewActiveMouseMode";
   public final static transient String MouseModesProperty = "NewListOfMouseModes";
   /**
    * A property string used when firing PropertyChangeSupport notifications
    * when the mouse mode is acting as proxy for another mouse mode.
    */
   public static final String ProxyMouseModeProperty = "MouseModeProxy";

   /**
    * The active MapMouseMode.
    */
   protected transient MapMouseMode activeMouseMode = null;

   /**
    * The registered MapMouseModes.
    */
   protected transient Vector<MapMouseMode> mouseModes = new Vector<MapMouseMode>(0);

   /**
    * The MapBean.
    */
   protected transient MapBean map;

   /**
    * Need to keep a safe copy of the current layers that are part of the
    * MapBean in case a MouseMode gets added before the MapBean is set in the
    * MouseDelegator. Without this, you can get into a situation where new
    * MapMouseModes don't know about layers until the MouseDelegator receives a
    * property change event from the MapBean.
    */
   protected Layer[] currentLayers = null;

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
    * Construct a MouseDelegator with an associated MapBean.
    * 
    * @param map MapBean
    */
   public MouseDelegator(MapBean map) {
      setMap(map);
   }

   /**
    * Construct a MouseDelegator without an associated MapBean. You will need to
    * set the MapBean via <code>setMap()</code>.
    * 
    * @see #setMap
    */
   public MouseDelegator() {
      this(null);
   }

   /**
    * Set the associated MapBean.
    * 
    * @param mapbean MapBean
    */
   public void setMap(MapBean mapbean) {
      if (map != null) {
         map.removePropertyChangeListener(this);
         setInactive(activeMouseMode);
      }

      map = mapbean;
      if (map != null) {
         map.addPropertyChangeListener(this);
         setActive(activeMouseMode);
      }
   }

   /**
    * Get the associated MapBean.
    * 
    * @return MapBean
    */
   public MapBean getMap() {
      return map;
   }

   // ----------------------------------------------------------------------
   //
   // Mouse Event handling support
   //
   // ----------------------------------------------------------------------

   /**
    * Returns the ID string for the active Mouse Mode.
    * 
    * @return String ID of the active mouse mode.
    */
   public String getActiveMouseModeID() {
      if (activeMouseMode != null)
         return activeMouseMode.getID();
      else
         return null;
   }

   /**
    * Sets the mouse mode to the mode with the same ID string. If none of the
    * MouseEventDelagates have a matching ID string, the mode is not changed. <br>
    * The map mouse cursor is set to the recommended cursor retrieved from the
    * active mouseMode.
    * 
    * @param MouseModeID the string ID of the mode to set active.
    */
   public void setActiveMouseModeWithID(String MouseModeID) {
      if (MouseModeID == null) {
         Debug.error("MouseDelegator:setActiveMouseModeWithID() - null value");
         return;
      }

      MapMouseMode oldActive = activeMouseMode;
      setInactive(activeMouseMode);

      for (MapMouseMode med : mouseModes) {
         if (MouseModeID.equals(med.getID())) {
            setActive(med);
            if (Debug.debugging("mousemode")) {
               Debug.output("MouseDelegator.setActiveMouseModeWithID() setting new mode to mode " + med.getID());
            }
            break;
         }
      }

      firePropertyChange(ActiveModeProperty, oldActive, activeMouseMode);
   }

   /**
    * Returns the mouse mode delegate that is active at the moment.
    * 
    * @return MapMouseMode the active mouse mode
    */
   public MapMouseMode getActiveMouseMode() {
      return activeMouseMode;
   }

   /**
    * Sets the active mouse mode. If the MapMouseMode is not a member of the
    * current mouse modes, it is added to the list. <br>
    * The map mouse cursor is set to the recommended cursor retrieved from the
    * active mouseMode.
    * 
    * @param aMed a MapMouseMode to make active.
    */
   public void setActiveMouseMode(MapMouseMode aMed) {
      if (aMed == null) {
         Debug.error("MouseDelegator:setActiveMouseMode() - null value");
         return;
      }
      MapMouseMode oldActive = activeMouseMode;
      boolean isAlreadyAMode = false;

      for (MapMouseMode med : mouseModes) {
         // Need to go through the modes, turn off the other active
         // mode, and turn on this one.
         if (aMed.getID().equals(med.getID())) {
            isAlreadyAMode = true;
         }
      }

      if (!isAlreadyAMode) {
         addMouseMode(aMed);
      }

      setActive(aMed);

      firePropertyChange(ActiveModeProperty, oldActive, activeMouseMode);
   }

   /**
    * Returns an array of MapMouseModes that are available to the MapBean.
    * 
    * @return an array of MapMouseModes.
    */
   public MapMouseMode[] getMouseModes() {
      int nMouseModes = mouseModes.size();
      if (nMouseModes == 0)
         return (new MapMouseMode[0]);
      MapMouseMode[] result = new MapMouseMode[nMouseModes];
      for (int i = 0; i < nMouseModes; i++) {
         result[i] = (MapMouseMode) mouseModes.elementAt(i);
      }
      return result;
   }

   /**
    * Used to set the mouseModes available to the MapBean. The Delegator drops
    * all references to any mouseModes it knew about previously. It also sets
    * the index of the array to be the active mouse mode. <br>
    * The map mouse cursor is set to the recommended cursor retrieved from the
    * active mouseMode.
    * 
    * @param meds an array of MapMouseModes
    * @param activeIndex which mouse mode to make active
    */
   public void setMouseModes(MapMouseMode[] meds, int activeIndex) {
      mouseModes.clear();
      MapMouseMode oldActive = activeMouseMode;
      for (int i = 0; i < meds.length; i++) {
         mouseModes.add(meds[i]);

         if (i == activeIndex) { // activate the right mode
            setActive(meds[i]);
         }
      }

      firePropertyChange(MouseModesProperty, null, mouseModes);
      firePropertyChange(ActiveModeProperty, oldActive, activeMouseMode);
   }

   /**
    * Used to set the mouseModes available to the MapBean. The MapBean drops all
    * references to any mouseModes it knew about previously. The meds[0] mode is
    * made active, by default.
    * 
    * @param meds an array of MapMouseModes
    */
   public void setMouseModes(MapMouseMode[] meds) {
      setMouseModes(meds, 0);
   }

   /**
    * Adds a MapMouseMode to the MouseMode list. Does not make it the active
    * mode.
    * 
    * @param med the MouseEvent Delegate to add.
    */
   public void addMouseMode(MapMouseMode med) {

      if (med != null) {
         mouseModes.addElement(med);
         // All of the MouseModes will think they are active, but
         // the Delegator will only pass events to the one it
         // thinks is...
         if (mouseModes.size() == 1) {
            setActive(med);
         }

         if (currentLayers != null) {
            setupMouseModeWithLayers(med, currentLayers);
         }

         firePropertyChange(MouseModesProperty, null, mouseModes);
      }
   }

   /**
    * Removes a particular MapMouseMode from the MouseMode list.
    * 
    * @param med the MapMouseMode that should be removed.
    */
   public void removeMouseMode(MapMouseMode med) {
      boolean needToAdjustActiveMode = false;

      if (med == null) {
         return;
      }

      if (med.equals(activeMouseMode)) {
         needToAdjustActiveMode = true;
         setInactive(med);
      }

      for (MapMouseMode checkMM : mouseModes) {
         if (med.equals(checkMM)) {
            med.removeAllMapMouseListeners();

         }
         // Set the first mode to the active one, if deleting the
         // active one.
         else if (needToAdjustActiveMode) {
            setActive(checkMM);
            needToAdjustActiveMode = false;
         }
      }

      mouseModes.remove(med);

      firePropertyChange(MouseModesProperty, null, mouseModes);
   }

   /**
    * Removes a particular MapMouseMode from the MouseMode list, with the ID
    * given.
    * 
    * @param id the ID of the MapMouseMode that should be removed
    */
   public void removeMouseMode(String id) {
      for (MapMouseMode med : mouseModes) {
         if (id.equals(med.getID())) {
            removeMouseMode(med);
            break;
         }
      }
   }

   /**
    * Sets the three default OpenMap mouse modes. These modes are: NavMouseMode
    * (Map Navigation), the SelectMouseMode (MouseEvents go to Layers), and
    * NullMouseMode (MouseEvents are ignored).
    */
   public void setDefaultMouseModes() {
      MapMouseMode[] modes = new MapMouseMode[3];
      modes[0] = new NavMouseMode(true);
      modes[1] = new SelectMouseMode(true);
      modes[2] = new NullMouseMode();

      setMouseModes(modes);
   }

   /**
    * PropertyChangeListenter Interface method.
    * 
    * @param evt PropertyChangeEvent
    */
   public void propertyChange(PropertyChangeEvent evt) {
      String property = evt.getPropertyName();
      if (property == MapBean.LayersProperty) {
         // make a safe copy of the layers that are part of the
         // MapBean
         Layer[] layers = (Layer[]) evt.getNewValue();
         currentLayers = new Layer[layers.length];
         System.arraycopy(layers, 0, currentLayers, 0, layers.length);

         setupMouseModesWithLayers(currentLayers);
      }

      if (property.equals(ProxyMouseModeProperty)) {

         Object newObj = evt.getNewValue();
         if (newObj instanceof MapMouseMode) {
            map.setCursor(((MapMouseMode) newObj).getModeCursor());
         } else {
            map.setCursor(getActiveMouseMode().getModeCursor());
         }

         firePropertyChange(ProxyMouseModeProperty, evt.getOldValue(), newObj);
      }
   }

   /**
    * Does the work putting the layers given on each mouse mode's list of layers
    * to notify if it becomes active.
    */
   public void setupMouseModesWithLayers(Layer[] layers) {
      for (int j = 0; j < mouseModes.size(); j++) {
         // Clear out the old listeners first
         MapMouseMode mmm = (MapMouseMode) mouseModes.elementAt(j);
         setupMouseModeWithLayers(mmm, layers);
      }
   }

   /**
    * Gives a MapMouseMode access to a Layer[], and it will find the layers that
    * want to listen to it and will forward events to them if it is added to the
    * MapBean as a MouseListener or a MouseMotionListener.
    * 
    * @param mmm MapMouseMode
    * @param layers Layer[]
    */
   public void setupMouseModeWithLayers(MapMouseMode mmm, Layer[] layers) {
      mmm.removeAllMapMouseListeners();
      for (int i = 0; i < layers.length; i++) {
         // Add the listeners from each layer to the mouse mode.
         MapMouseListener tempmml = null;

         if (layers[i] != null) {
            tempmml = layers[i].getMapMouseListener();
         }

         if (tempmml == null) {
            continue;
         }
         String[] services = tempmml.getMouseModeServiceList();
         if (services != null) {
            for (int k = 0; k < services.length; k++) {
               if (mmm.getID().equals(services[k])) {
                  mmm.addMapMouseListener(tempmml);
                  if (Debug.debugging("mousemode")) {
                     Debug.output("MouseDelegator.setupMouseModeWithLayers():" + " layer = " + layers[i].getName() + " service = "
                           + mmm.getID());
                  }
                  break;
               }
            }
         }
      }
   }

   /**
    * Set the active MapMouseMode. This sets the MapMouseMode of the associated
    * MapBean.
    * 
    * @param mm MapMouseMode
    */
   public void setActive(MapMouseMode mm) {
      if (activeMouseMode != null) {
         setInactive(activeMouseMode);
      }

      activeMouseMode = mm;

      if (map != null && activeMouseMode != null) {
         if (Debug.debugging("mousemode")) {
            Debug.output("MouseDelegator.setActive(): " + mm.getID());
         }
         map.addMouseListener(mm);
         map.addMouseMotionListener(mm);
         map.addMouseWheelListener(mm);
         map.addPaintListener(mm);
         map.setCursor(activeMouseMode.getModeCursor());
         if (mm instanceof ProjectionListener) {
            map.addProjectionListener((ProjectionListener) mm);
         }
         activeMouseMode.setActive(true);
         activeMouseMode.addPropertyChangeListener(this);
      }
   }

   /**
    * Deactivate the MapMouseMode.
    * 
    * @param mm MapMouseMode.
    */
   public void setInactive(MapMouseMode mm) {
      if (map != null) {
         map.removeMouseListener(mm);
         map.removeMouseMotionListener(mm);
         map.removeMouseWheelListener(mm);
         map.removePaintListener(mm);
         // should set map's cursor to some default value??
         if (mm instanceof ProjectionListener) {
            map.removeProjectionListener((ProjectionListener) mm);
         }
      }
      if (activeMouseMode == mm) {
         activeMouseMode = null;
      }
      if (mm != null) {
         mm.setActive(false);
         mm.removePropertyChangeListener(this);
      }
   }

   /**
    * Eventually gets called when the MouseDelegator is added to the
    * BeanContext, and when other objects are added to the BeanContext anytime
    * after that. The MouseDelegator looks for a MapBean to manage MouseEvents
    * for, and MouseModes to use to manage those events. If a MapBean is added
    * to the BeanContext while another already is in use, the second MapBean
    * will take the place of the first.
    * 
    * @param it iterator to use to go through the new objects in the
    *        BeanContext.
    */
   public void findAndInit(Iterator<?> it) {
      while (it.hasNext()) {
         findAndInit(it.next());
      }
   }

   /**
    * Called when an object should be evaluated by the MouseDelegator to see if
    * it is needed.
    */
   public void findAndInit(Object someObj) {
      if (someObj instanceof MapBean) {
         Debug.message("mousedelegator", "MouseDelegator found a map.");
         setMap((MapBean) someObj);
      }
      if (someObj instanceof MapMouseMode) {
         Debug.message("mousedelegator", "MouseDelegator found a MapMouseMode.");
         addMouseMode((MapMouseMode) someObj);
      }
   }

   /**
    * BeanContextMembershipListener method. Called when new objects are added to
    * the parent BeanContext.
    * 
    * @param bcme event that contains an iterator that can be used to go through
    *        the new objects.
    */
   public void childrenAdded(BeanContextMembershipEvent bcme) {
      findAndInit(bcme.iterator());
   }

   /**
    * BeanContextMembershipListener method. Called when objects have been
    * removed from the parent BeanContext. The MouseDelegator looks for the
    * MapBean it is managing MouseEvents for, and any MouseModes that may be
    * removed.
    * 
    * @param bcme event that contains an iterator that can be used to go through
    *        the removed objects.
    */
   public void childrenRemoved(BeanContextMembershipEvent bcme) {
      Iterator<?> it = bcme.iterator();
      while (it.hasNext()) {
         findAndUndo(it.next());
      }
   }

   /**
    * Called by childrenRemoved.
    */
   public void findAndUndo(Object someObj) {
      if (someObj instanceof MapBean) {
         if (getMap() == (MapBean) someObj) {
            Debug.message("mousedelegator", "MouseDelegator: removing the map.");
            setMap(null);
         }
      }
      if (someObj instanceof MapMouseMode) {
         Debug.message("mousedelegator", "MouseDelegator: removing a MapMouseMode.");
         removeMouseMode((MapMouseMode) someObj);
      }

      if (someObj == this) {
         dispose();
      }
   }

   public void dispose() {
      if (mouseModes != null) {
         mouseModes.clear();
      }
      currentLayers = null;
   }

   /** Method for BeanContextChild interface. */
   public BeanContext getBeanContext() {
      return beanContextChildSupport.getBeanContext();
   }

   /** Method for BeanContextChild interface. */
   public void setBeanContext(BeanContext in_bc)
         throws PropertyVetoException {
      if (in_bc != null) {
         in_bc.addBeanContextMembershipListener(this);
         beanContextChildSupport.setBeanContext(in_bc);
         findAndInit(in_bc.iterator());
      }
   }

   /** Method for BeanContextChild interface. */
   public void addPropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
      pcSupport.addPropertyChangeListener(propertyName, in_pcl);
   }

   /** Method for BeanContextChild interface. */
   public void removePropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
      pcSupport.removePropertyChangeListener(propertyName, in_pcl);
   }

   public void addPropertyChangeListener(PropertyChangeListener listener) {
      // This function is why we don't use the
      // BeanContextChildSupport PropertyChangeSupport object.
      pcSupport.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      // This function is why we don't use the
      // BeanContextChildSupport PropertyChangeSupport object.
      pcSupport.removePropertyChangeListener(listener);
   }

   public void firePropertyChange(String property, Object oldObj, Object newObj) {

      pcSupport.firePropertyChange(property, oldObj, newObj);
   }

   /** Method for BeanContextChild interface. */
   public void addVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
      beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
   }

   /** Method for BeanContextChild interface. */
   public void removeVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
      beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
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

}
