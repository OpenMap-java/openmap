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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MapHandler.java,v $
// $RCSfile: MapHandler.java,v $
// $Revision: 1.9 $
// $Date: 2008/09/28 19:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextServicesSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MapHandler is an extension of the BeanContextServicesSupport, with the
 * added capability of being able to set the policy on handling
 * SoloMapComponents. The MapHandler can be thought of as a generic map
 * container, that contains the MapBean, all the layers that may be part of the
 * map, and all the gesture handling components for that map. Given that
 * definition, there are some OpenMap components that need to have a one-to-one
 * relationship with a MapHandler. For instance, a MapHandler should only
 * contain one MapBean, LayerHandler, MouseDelegator and PropertyHandler.
 * Objects that have this one-to-one relationship with the MapHandler should
 * implement the SoloMapComponent interface, so the MapHandler can control what
 * happens when more than one instance of a particular SoloMapComponent type is
 * added to the MapBean. Other objects that get added to the MapHandler that
 * hook up with SoloMapComponents should expect that only one version of those
 * components should be used - if another instance gets added to the MapHandler,
 * the objects should disconnect from the SoloMapComponent that they are using,
 * and connect to the new object added. With this behavior, these objects can
 * gracefully adapt to the SoloMapComponent policies that are set in the
 * MapHandler:
 * 
 * <UL>
 * 
 * <LI>You can set the MapHandler with the policy to let duplicate
 * SoloMapComponents be added to the MapHandler. If a duplicate SoloMapComponent
 * is added, then all objects using that type of SoloMapComponent should
 * gracefully use the latest version added.
 * 
 * <LI>The MapHandler can have to policy to limit the addition of duplicate
 * SoloMapComponents. If a duplicate is added, and exception is thrown. In this
 * case, the original SoloMapComponent must be removed from the MapHandler
 * before the second instance is added.
 * 
 * </UL>
 */
public class MapHandler
      extends BeanContextServicesSupport {

   private static final long serialVersionUID = 1L;

   public static Logger logger = Logger.getLogger("com.bbn.openmap.MapHandler");

   protected SoloMapComponentPolicy policy;
   protected boolean DEBUG = false;
   protected boolean addInProgress = false;
   protected Vector<Object> addLaterVector = null;

   public MapHandler() {
      DEBUG = logger.isLoggable(Level.FINE);
   }

   /**
    * Set the policy of behavior for the MapHandler on how it should act when
    * multiple instances of a certain SoloMapComponents are added to it.
    * 
    * @see SoloMapComponentReplacePolicy
    * @see SoloMapComponentRejectPolicy
    */
   public void setPolicy(SoloMapComponentPolicy smcp) {
      policy = smcp;
   }

   /**
    * Get the policy that sets the behavior of the MapHandler when it encounters
    * the situation of multiple instances of a particular SoloMapComponent.
    */
   public SoloMapComponentPolicy getPolicy() {
      if (policy == null) {
         policy = new SoloMapComponentRejectPolicy();
      }
      return policy;
   }

   /**
    * Call made from the add() method for objects that added when another object
    * was being added, setting up a ConcurrentModificationException condition.
    * This is a coping mechanism.
    */
   protected synchronized void addLater(Object obj) {
      if (addLaterVector == null) {
         addLaterVector = new Vector<Object>();
      }
      if (DEBUG) {
         logger.fine("=== Adding " + obj.getClass().getName() + " to list for later addition");
      }
      addLaterVector.add(obj);
   }

   /**
    * Call to add any objects on the addLaterVector to the MapHandler. These are
    * objects that were previously added which another object was being added,
    * setting up a ConcurrentModificationException condition. Part of the coping
    * mechanism.
    */
   protected synchronized void purgeLaterList() {
      Vector<Object> tmpList = addLaterVector;
      addLaterVector = null;

      if (tmpList != null) {
         Iterator<Object> it = tmpList.iterator();
         while (it.hasNext()) {
            Object obj = it.next();
            if (DEBUG) {
               logger.fine("+++ Adding " + obj.getClass().getName() + " to MapHandler from later list.");
            }
            add(obj);
         }
         tmpList.clear();
      }
   }

   protected synchronized void setAddInProgress(boolean value) {
      addInProgress = value;
   }

   protected synchronized boolean isAddInProgress() {
      return addInProgress;
   }

   /**
    * Add an object to the MapHandler BeanContextSupport. Uses the current
    * SoloMapComponentPolicy to handle the SoloMapComponents added. May throw
    * MultipleSoloMapComponentException if the policy is a
    * SoloMapComponentRejectPolicy and the SoloMapComponent is a duplicate type
    * of another component already added.
    * 
    * @param obj the map component to nest within this BeanContext.
    * @return true if addition is successful, false if not.
    */
   public synchronized boolean add(Object obj) {
      try {
         boolean passedSoloMapComponentTest = true;
         if (obj instanceof SoloMapComponent) {
            try {
               passedSoloMapComponentTest = getPolicy().canAdd(this, obj);
            } catch (MultipleSoloMapComponentException msmce) {
               logger.fine(msmce.getMessage());
               return false;
            }
         }
         
         if (obj != null && passedSoloMapComponentTest) {

            if (isAddInProgress()) {
               if (DEBUG) {
                  logger.fine("MapHandler: Attempting to add while add in progress, adding [" + obj.getClass().getName()
                        + "]object to list");
               }
               addLater(obj);
            } else {
               setAddInProgress(true);
               boolean ret = super.add(obj);
               setAddInProgress(false);
               purgeLaterList();
               return ret;
            }
         }

      } catch (java.util.ConcurrentModificationException cme) {
         if (obj != null) {
            logger
                  .warning("MapHandler caught ConcurrentModificationException when adding ["
                        + obj.getClass().getName()
                        + "]. The addition of this component to the MapHandler is causing some other component to attempt to be added as well, and the coping mechanism in the MapHandler is not handling it well.");
            if (DEBUG) {
               cme.printStackTrace();
            }
            addLater(obj);
            setAddInProgress(false);
         }
      }
      return false;
   }

   public String toString() {
      return getClass().getName();
   }

   /**
    * Given a class name, find the object in the MapHandler. If the class is not
    * a SoloMapComponent and there are more than one of them in the MapHandler,
    * you will get the first one found.
    */
   @SuppressWarnings("unchecked")
   public Object get(String classname) {
      Class someClass = null;
      try {
         someClass = Class.forName(classname);
      } catch (ClassNotFoundException cnfe) {
      }

      return get(someClass);
   }

   /**
    * Given a Class, find the object in the MapHandler. If the class is not a
    * SoloMapComponent and there are more than one of them in the MapHandler,
    * you will get the first one found.
    */
   public <T> T get(Class<T> someClass) {
      Collection<T> collection = getAll(someClass);

      Iterator<T> it = collection.iterator();
      while (it.hasNext()) {
         return it.next();
      }

      return null;
   }

   /**
    * Given a Class name, find all the objects in the MapHandler that are
    * assignment-compatible object of that Class. A Collection is always
    * returned, although it may be empty.
    */
   @SuppressWarnings("unchecked")
   public Collection getAll(String classname) {
      Class someClass = null;
      try {
         someClass = Class.forName(classname);
      } catch (ClassNotFoundException cnfe) {
      }

      return getAll(someClass);
   }

   /**
    * Given a Class, find all the objects in the MapHandler that are
    * assignment-compatible with that Class. A Collection is always returned,
    * although it may be empty.
    */
   public <T> Collection<T> getAll(Class<T> someClass) {
      Collection<T> collection = new LinkedList<T>();

      if (someClass != null) {
         Iterator<?> it = iterator();
         while (it.hasNext()) {
            Object someObj = it.next();
            if (someClass.isInstance(someObj)) {
               collection.add(someClass.cast(someObj));
            }
         }
      }

      return collection;
   }

   /**
    * Added because apparently, the BeanContext doesn't check to see if the
    * object is also a membership listener to remove it from that list. This
    * method removes the object from that list, too, if it is a
    * BeanContextMembershipListener.
    */
   public boolean remove(Object obj) {
      boolean ret = super.remove(obj);
      if (obj instanceof BeanContextMembershipListener) {
         super.removeBeanContextMembershipListener((BeanContextMembershipListener) obj);
      }
      return ret;
   }

   /**
    * Method to call with an object you don't want to add to this MapHandler,
    * but you want to make it available to all the MapHandlerChildren in it.
    * 
    * @param obj
    */
   public void present(Object obj) {
      for (Object someObj : this) {
         if (someObj instanceof MapHandlerChild) {
            ((MapHandlerChild) someObj).findAndInit(obj);
         }
      }
   }

   /**
    * Create an iterator copy, to avoid ConcurrentModificationExceptions in the
    * MapHandler if one of the components wants to add more components when the
    * MapHandler is set as the BeanContext on them.
    */
   @SuppressWarnings("unchecked")
   public Iterator iterator() {
      Iterator it = super.iterator();
      LinkedList list = new LinkedList();
      while (it.hasNext()) {
         list.add(it.next());
      }

      return list.iterator();
   }

   /**
    * Calls dispose() on the contained MapBean and removes all objects from
    * BeanContext.
    */
   @SuppressWarnings("unchecked")
   public void dispose() {
      addLaterVector = null;

      MapBean mb = (MapBean) get(com.bbn.openmap.MapBean.class);
      if (mb != null) {
         remove(mb);
         mb.dispose();
      }

      Iterator kids = iterator();
      int size = size();
      ArrayList kidList = new ArrayList(size);

      while (kids.hasNext()) {
         kidList.add(kids.next());
      }

      for (kids = kidList.iterator(); kids.hasNext();) {
         Object obj = kids.next();
         remove(obj);
      }

   }

}
