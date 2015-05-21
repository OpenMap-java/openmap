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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/LibraryBean.java,v $
// $RCSfile: LibraryBean.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.Serializable;
import java.util.Properties;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A bean to be used for sharing LibrarySelectionTable objects between instances
 * of VPFLayer.
 * 
 * <pre>
 * 
 * # Assuming that you have a VPF Layer specifying a .libraryBean property
 * # with a value of &quot;VMAPData&quot;, you need to specify the following properties:
 * # Required - the java class information
 * VMAPData.class=com.bbn.openmap.layer.vpf.LibraryBean
 * # as in the layer .vpfPath, a ';' separated list of paths to VPF data
 * VMAPData.vpfPath=e:/VMAPLV0
 * # the name of the library bean, used by the VPFLayers to identify
 * # this bean as the one they want to use.
 * VMAPData.name=VMAPLEVEL0
 * # Maximum number of tiles to cache.
 * VMAPData.cacheSize=25
 * 
 * </pre>
 * 
 * The VMAPData maker name, or whatever other name you decide to name it, has to
 * be added to the openmap.components property list so the LibraryBean will be
 * created and added to the MapHandler. Of course, you could add the LibraryBean
 * to the MapHandler programmatically if you wanted to.
 */
public class LibraryBean
      implements PropertyConsumer, Serializable {

   private static final long serialVersionUID = 1L;

   /** used for explicitly naming a library bean (name). */
   public static final String nameProperty = "name";

   /**
    * property extension used to set the VPF root directory (vpfPath).
    */
   public static final String pathProperty = "vpfPath";

   /** Maximum size of tile cache (cacheSize). */
   public static final String cacheSizeProperty = "cacheSize";

   /** the lst for the path */
   private transient LibrarySelectionTable lst = null;

   /** the name of the bean set in properties, or the marker name */
   String beanName;

   /** used by set/getPropertyPrefix */
   private String propertyPrefix = null;

   /** the paths used in constructing the lst */
   private String[] paths;

   /**
    * The VPFFeatureCache to use for cached features.
    */
   protected transient VPFFeatureCache featureCache;

   /**
    * Construct an empty bean.
    */
   public LibraryBean() {
      featureCache = new VPFFeatureCache();
   }

   public void setProperties(Properties setList) {
      setProperties(getPropertyPrefix(), setList);
   }

   public void setProperties(String prefix, Properties setList) {
      setPropertyPrefix(prefix);
      String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

      paths = PropUtils.initPathsFromProperties(setList, realPrefix + pathProperty, paths);

      String beanName = setList.getProperty(realPrefix + nameProperty);
      this.beanName = (beanName == null) ? prefix : beanName;

      if (Debug.debugging("vpf")) {
         Debug.output("LibraryBean.setProperties(): " + prefix + " " + this.beanName + " initialized");
      }
      try {
         if (paths == null) {
            Debug.output("VPF LibraryBean: path not set - expected " + realPrefix + pathProperty + " property");
         } else {
            lst = new LibrarySelectionTable(paths);
         }
      } catch (com.bbn.openmap.io.FormatException f) {
         Debug.output(f.getMessage());
      } catch (NullPointerException npe) {
         Debug.output("LibraryBean.setProperties:" + prefix + ": path name not valid");
      }

      int cacheSize = PropUtils.intFromProperties(setList, realPrefix + cacheSizeProperty, featureCache.getCacheSize());
      featureCache.resetCache(cacheSize);
   }

   /**
    * Gets the name of the component - if the name was explicitly set, then
    * return that, otherwise return the property prefix.
    */
   public String getName() {
      return beanName;
   }

   /**
    * Not a good PropertyConsumer yet, doesn't return values.
    */
   public Properties getProperties(Properties getList) {
      if (getList == null) {
         getList = new Properties();
      }
      String prefix = PropUtils.getScopedPropertyPrefix(this);
      getList.put(prefix + nameProperty, beanName);
      getList.put(prefix + cacheSizeProperty, Integer.toString(featureCache.getCacheSize()));
      return getList;
   }

   /**
     */
   public Properties getPropertyInfo(Properties list) {
      if (list == null) {
         list = new Properties();
      }

      list.put(nameProperty, "Name of Library Bean.");
      list.put(pathProperty, "List of VPF directories.");
      list.put(pathProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.MultiDirectoryPropertyEditor");
      list.put(cacheSizeProperty, "Maximun number of tiles to cache (25 is default).");

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
    * @return the property prefix
    */
   public String getPropertyPrefix() {
      return propertyPrefix;
   }

   /**
    * Returns the LST for the path of this object.
    * 
    * @return an LST, null if the object didn't construct properly
    */
   public LibrarySelectionTable getLibrarySelectionTable() {
      return lst;
   }

   /**
    * Creates a new VPFFeatureWarehouse every time, with the shared
    * featureCache.
    * 
    * @return VPFCachedFeatureGraphicWarehouse used by LibraryBean
    */
   public VPFCachedFeatureGraphicWarehouse getWarehouse() {
      if (Debug.debugging("vpf")) {
         Debug.output("LibraryBean.getWarehouse(): creating warehouse.");
      }

      return new VPFCachedFeatureGraphicWarehouse(featureCache);
   }
}