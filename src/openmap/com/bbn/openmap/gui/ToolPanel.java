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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ToolPanel.java,v $
// $RCSfile: ToolPanel.java,v $
// $Revision: 1.13 $
// $Date: 2006/03/06 15:41:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.gui.menu.ToolPanelToggleMenuItem;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Represents the toolbar containing tools to apply to the map. Tools can be
 * added in sequential order, and retrieved using the tool's keyword. NOTE:
 * Every time a string is passed into a method of this class, the interned
 * version of it is used as a key.
 * <P>
 * 
 * When the ToolPanel is part of the BeanContext, it looks for Tools that have
 * also been added to the BeanContext. If there is more than one ToolPanel in a
 * BeanContext at a time, both will show the same Tool faces. The 'components'
 * property can be used to control which tools can be added to a specific
 * instance of a ToolPanel. That property should contain a space separated list
 * of prefixes used for Tools, which in turn should be set in the Tools as their
 * keys.
 * 
 * @see Tool
 * @author john gash
 */
public class ToolPanel
      extends JToolBar
      implements BeanContextChild, BeanContextMembershipListener, MapPanelChild, PropertyConsumer, ComponentListener {

   private static final long serialVersionUID = 1L;
   /** The set of tools contained on the toolbar. */
   protected Hashtable<String, Tool> items = new Hashtable<String, Tool>();
   /**
    * A flag to note whether the ToolPanel inserts spaces between tools.
    */
   protected boolean autoSpace = false;

   /**
    * BeanContextChildSupport object provides helper functions for
    * BeanContextChild interface.
    */
   private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);

   /**
    * The property prefix used for this ToolPanel.
    */
   protected String propertyPrefix = null;

   /**
    * A list of components to use for filtering tools found in the MapHandler to
    * add to this ToolPanel.
    */
   public final static String ComponentsProperty = "components";

   /**
    * A list of components to use for filtering out tools found in the
    * MapHandler. Components added to this list will NOT be added to this
    * ToolPanel.
    */
   public final static String AvoidComponentsProperty = "avoid";

   public final static String MembershipProperty = "membership";

   public final static String NameProperty = "name";

   /**
    * A filter list of components to look for and add.
    */
   protected List<String> componentList = null;

   /**
    * A filter list of components to avoid.
    */
   protected List<String> avoidList = null;

   protected GridBagLayout gridbag = new GridBagLayout();
   protected GridBagConstraints c = new GridBagConstraints();
   protected String parentName = null;

   /**
    * Holder that expands in the GridBagLayout, keeping things pushed to the
    * left side of the toolpanel.
    */
   protected JLabel filler = null;

   /**
    * Constructor
    */
   public ToolPanel() {
      setLayout(gridbag);
      setFloatable(false);
      setVisible(false);
      setName("Tool Panel");
   }

   /**
    * Add an item to the tool bar.
    * 
    * @param key The key associated with the item.
    * @param item The Tool to add.
    */
   public void add(String key, Tool item) {
      add(key, item, -1);
   }

   /**
    * A little array used to track what indexes are already used, to prevent the
    * GridBagLayout from placing things on top of each other.
    */
   protected boolean[] usedIndexes;
   public int MAX_INDEXES = 101;

   /**
    * Provides the next available component index for placement, starting at 0.
    */
   protected int getNextAvailableIndex() {
      return getNextAvailableIndex(0);
   }

   /**
    * Provides the next available component index for placement, given a
    * starting index.
    */
   protected int getNextAvailableIndex(int startAt) {

      if (usedIndexes == null) {
         usedIndexes = new boolean[MAX_INDEXES];
      }

      if (startAt < 0)
         startAt = 0;
      if (startAt >= MAX_INDEXES)
         startAt = MAX_INDEXES - 1;

      int i = startAt;
      // Find the first unused
      for (; i < MAX_INDEXES && usedIndexes[i] == true; i++) {
      }
      usedIndexes[i] = true;

      return i;
   }

   /**
    * Add an item to the tool bar.
    * 
    * @param key The key associated with the item.
    * @param item The Tool to add.
    * @param index The position index for placement of the tool. -1 puts it at
    *        the end, and if the position is greater than the size, it is placed
    *        at the end. This class does not remember where items were asked to
    *        be placed, so later additions may mess up intended order.
    */
   public void add(String key, Tool item, int index) {

      int orientation = getOrientation();
      boolean hOrient = orientation == SwingConstants.HORIZONTAL;
      item.setOrientation(orientation);

      Container face = item.getFace();

      if (face != null) {
         face.addComponentListener(this);
         items.put(key.intern(), item);

         if (autoSpace) {
            index *= 2;
         }

         if (hOrient) {
            c.weightx = 0;
            c.gridx = getNextAvailableIndex(index);
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
         } else {
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = getNextAvailableIndex(index);
            c.anchor = GridBagConstraints.NORTH;
         }

         gridbag.setConstraints(face, c);
         add(face);

         if (filler == null) {
            if (hOrient) {
               c.gridx = getNextAvailableIndex(MAX_INDEXES);
               c.anchor = GridBagConstraints.EAST;
               c.weightx = 1;
            } else {
               c.gridy = getNextAvailableIndex(MAX_INDEXES);
               c.anchor = GridBagConstraints.SOUTH;
               c.weighty = 1;
            }

            filler = new JLabel("");
            gridbag.setConstraints(filler, c);
            add(filler);
         }

         if (autoSpace) {
            JLabel l = new JLabel(" ");
            gridbag.setConstraints(l, c);
            add(l);
         }
      }

      setVisibility();
      firePropertyChange(MembershipProperty, null, items);
   }

   /**
    * Add an item to the tool bar. Assumes that the key will be picked out of
    * the Tool.
    * 
    * @param item The Tool to add.
    */
   public void add(Tool item) {
      add(item, -1);
   }

   /**
    * Add an item to the tool bar. Assumes that the key will be picked out of
    * the Tool.
    * 
    * @param item The Tool to add.
    * @param index The position to add the Tool. -1 will add it to the end.
    */
   public void add(Tool item, int index) {
      try {
         add(item.getKey(), item, index);
      } catch (NullPointerException npe) {
         if (item != null) {
            Debug.error("ToolPanel.add(): no name for " + item.getClass().getName());
            npe.printStackTrace();
         } else {
            Debug.error("ToolPanel.add(): no name for null tool.");
         }
      }
   }

   /**
    * Get an item from the tool bar.
    * 
    * @param key The key associated with the item.
    * @return The tool associated with the key, null if not found.
    */
   public Tool get(String key) {
      return (Tool) items.get(key.intern());
   }

   /** Remove a tool with the right key */
   public void remove(String key) {
      Tool tool = (Tool) items.remove(key.intern());
      if (tool != null) {
         remove(tool.getFace());
         tool.getFace().removeComponentListener(this);
         firePropertyChange(MembershipProperty, null, items);
      }
   }

   /** Add a space between tools. */
   protected void addSpace() {
      add(new JLabel(" "));
   }

   /** Set whether spaces are placed between tools. */
   public void setAutoSpace(boolean set) {
      autoSpace = set;
   }

   /**
    * BorderLayout.NORTH by default for this class.
    */
   protected String preferredLocation = java.awt.BorderLayout.NORTH;

   /**
    * MapPanelChild method.
    */
   public void setPreferredLocation(String value) {
      preferredLocation = value;
   }

   /** MapPanelChild method. */
   public String getPreferredLocation() {
      return preferredLocation;
   }

   /** Find out whether spaces are being placed between tools. */
   public boolean isAutoSpace() {
      return autoSpace;
   }

   /**
    * Set the list of strings used by the ToolPanel to figure out which Tools
    * should be added (in the findAndInit()) method and where they should go.
    */
   public void setComponentList(List<String> list) {
      componentList = list;
   }

   /**
    * Get the list of strings used by the ToolPanel to figure out which Tools
    * should be added (in the findAndInit()) method and where they should go.
    */
   public List<String> getComponentList() {
      return componentList;
   }

   /**
    * Set the list of strings used by the ToolPanel to figure out which Tools
    * should not be added (in the findAndInit()) method.
    */
   public void setAvoidList(List<String> list) {
      avoidList = list;
   }

   /**
    * Get the list of strings used by the ToolPanel to figure out which Tools
    * should not be added (in the findAndInit()) method.
    */
   public List<String> getAvoidList() {
      return avoidList;
   }

   /**
    * Called when the ToolPanel is added to the BeanContext, and when new
    * objects are added to the BeanContext after that. The ToolPanel looks for
    * Tools that are part of the BeanContext.
    * 
    * @param it iterator to use to go through the new objects.
    */
   public void findAndInit(Iterator<Object> it) {
      while (it.hasNext()) {
         findAndInit(it.next());
      }
   }

   /**
    * Figure out if the string key is in the provided list, and provide the
    * location index of it is.
    * 
    * @param key the key of the component to check for.
    * @param list the list of keys to check.
    * @return -1 if not on the list, the index starting at 0 if it is.
    */
   protected int keyOnList(String key, List<String> list) {
      int ret = -1;
      int index = 0;
      if (list != null) {
         for (String listKey : list) {
            if (listKey.equalsIgnoreCase(key)) {
               ret = index;
               break;
            }
            index++;
         }
      }
      return ret;
   }

   public void findAndInit(Object someObj) {
      if (someObj instanceof Tool) {
         String key = ((Tool) someObj).getKey();
         List<String> list = getComponentList();
         int index;
         if (list != null) {
            index = keyOnList(key, list);

            if (index >= 0) {
               if (Debug.debugging("basic")) {
                  Debug.output("ToolPanel: found a tool Object " + key + " for placement at " + index);
               }

               add((Tool) someObj, index);
            }

         } else {
            index = keyOnList(key, getAvoidList());
            if (index < 0) {
               Debug.message("basic", "ToolPanel: found a tool Object");
               add((Tool) someObj);
            }
         }
      }
   }

   /**
    * Get a menu item that controls the visibility of this ToolPanel.
    * 
    * @return ToolPanelToggleMenuItem
    */
   public ToolPanelToggleMenuItem getToggleMenu() {
      return new ToolPanelToggleMenuItem(this);
   }

   /**
    * Checks to see if the menu item controls this ToolPanel.
    * 
    * @param mi
    * @return true if menu item refers to this tool panel.
    */
   public boolean checkToolPanelToggleMenuItem(ToolPanelToggleMenuItem mi) {
      return (mi != null && mi.getToolPanel().equals(this));
   }

   /**
    * BeanContextMembershipListener method. Called when objects have been added
    * to the parent BeanContext.
    * 
    * @param bcme the event containing the iterator with new objects.
    */
   public void childrenAdded(BeanContextMembershipEvent bcme) {
      findAndInit(bcme.iterator());
   }

   /**
    * BeanContextMembershipListener method. Called when objects have been
    * removed from the parent BeanContext. If the ToolPanel finds a Tool in the
    * list, it removes it from the ToolPanel.
    * 
    * @param bcme the event containing the iterator with removed objects.
    */
   public void childrenRemoved(BeanContextMembershipEvent bcme) {
      Iterator<Object> it = bcme.iterator();
      Object someObj;
      while (it.hasNext()) {
         someObj = it.next();
         if (someObj instanceof Tool) {
            // do the initializing that need to be done here
            Debug.message("toolpanel", "ToolPanel removing tool Object");
            remove(((Tool) someObj).getKey());
         }
      }
   }

   /** Method for BeanContextChild interface. */
   public BeanContext getBeanContext() {
      return beanContextChildSupport.getBeanContext();
   }

   /**
    * Method for BeanContextChild interface. Called when the ToolPanel is added
    * to the BeanContext.
    * 
    * @param in_bc the BeanContext.
    */
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
      beanContextChildSupport.addPropertyChangeListener(propertyName, in_pcl);
   }

   /** Method for BeanContextChild interface. */
   public void removePropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
      beanContextChildSupport.removePropertyChangeListener(propertyName, in_pcl);
   }

   /** Method for BeanContextChild interface. */
   public void addVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
      beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
   }

   /** Method for BeanContextChild interface. */
   public void removeVetoableChangeListener(String propertyName, VetoableChangeListener in_vcl) {
      beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
   }

   public void setPropertyPrefix(String prefix) {
      propertyPrefix = prefix;
   }

   public String getPropertyPrefix() {
      return propertyPrefix;
   }

   public void setProperties(Properties props) {
      setProperties(null, props);
   }

   public void setProperties(String prefix, Properties props) {
      setPropertyPrefix(prefix);

      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      String componentsString = props.getProperty(prefix + ComponentsProperty);

      if (componentsString != null) {
         setComponentList(PropUtils.parseSpacedMarkers(componentsString));
      }

      String avoidComponentsString = props.getProperty(prefix + AvoidComponentsProperty);

      if (avoidComponentsString != null) {
         setAvoidList(PropUtils.parseSpacedMarkers(avoidComponentsString));
      }

      String preferredLocationString = props.getProperty(prefix + PreferredLocationProperty);

      if (preferredLocationString != null) {
         try {
            preferredLocationString = (String) java.awt.BorderLayout.class.getField(preferredLocationString).get(null);
         } catch (NoSuchFieldException nsfe) {
            preferredLocationString = null;
         } catch (IllegalAccessException iae) {
            preferredLocationString = null;
         }

         if (preferredLocationString != null) {
            setPreferredLocation(preferredLocationString);
            if (preferredLocationString.equalsIgnoreCase("WEST") || preferredLocationString.equalsIgnoreCase("EAST")) {
               setOrientation(SwingConstants.VERTICAL);
            }
         }
      }

      setName(props.getProperty(prefix + NameProperty, getName()));
      setParentName(props.getProperty(prefix + ParentNameProperty, getParentName()));
   }

   /**
    * Take a List of strings, and return a space-separated version. Return null
    * if the List is null.
    */
   protected StringBuffer rebuildListProperty(List<String> aList) {
      StringBuffer list = null;
      if (aList != null) {
         list = new StringBuffer();
         for (String toolKey : aList) {
            list.append(toolKey).append(" ");
         }
      }
      return list;
   }

   public Properties getProperties(Properties props) {
      if (props == null) {
         props = new Properties();
      }

      String prefix = PropUtils.getScopedPropertyPrefix(this);

      StringBuffer listProp = rebuildListProperty(getComponentList());
      if (listProp != null) {
         props.put(prefix + ComponentsProperty, listProp.toString());
      }

      listProp = rebuildListProperty(getAvoidList());
      if (listProp != null) {
         props.put(prefix + AvoidComponentsProperty, listProp.toString());
      }

      PropUtils.putIfNotDefault(props, prefix + PreferredLocationProperty, getPreferredLocation());
      PropUtils.putIfNotDefault(props, prefix + NameProperty, getName());
      PropUtils.putIfNotDefault(props, prefix + ParentNameProperty, getParentName());
      return props;
   }

   public Properties getPropertyInfo(Properties props) {
      if (props == null) {
         props = new Properties();
      }

      I18n i18n = Environment.getI18n();

      PropUtils.setI18NPropertyInfo(i18n, props, ToolPanel.class, ComponentsProperty, "Tool Names",
                                    "List of Names of Tools to Add", null);
      PropUtils.setI18NPropertyInfo(i18n, props, ToolPanel.class, AvoidComponentsProperty, "Avoid Tool Names",
                                    "List of Names of Tools to Not Add", null);
      PropUtils.setI18NPropertyInfo(i18n, props, ToolPanel.class, PreferredLocationProperty, "Location",
                                    "Preferred Location of Tool Panel", null);
      PropUtils.setI18NPropertyInfo(i18n, props, ToolPanel.class, NameProperty, "Tool Name", "Name of This Tool Panel", null);

      return props;
   }

   /**
    * If any of the components are visible, set the ToolPanel to be visible. If
    * all of them are invisible, make the ToolPanel invisible.
    */
   protected void setVisibility() {
      setVisible(areComponentsVisible());
   }

   public boolean areComponentsVisible() {
      Enumeration<Tool> enumeration = items.elements();
      while (enumeration.hasMoreElements()) {
         Tool tool = enumeration.nextElement();
         Container face = tool.getFace();
         // make sure tool != filler - filler(JPanel) test is for object equivalence
         if (!filler.equals(tool) && face != null && face.isVisible()) {
            return true;
         }
      }
      return false;
   }

   public void componentHidden(ComponentEvent ce) {
      setVisibility();
   }

   public void componentMoved(ComponentEvent ce) {

   }

   public void componentResized(ComponentEvent ce) {

   }

   public void componentShown(ComponentEvent ce) {
      setVisibility();
   }

   public String getParentName() {
      return parentName;
   }

   public void setParentName(String parentName) {
      this.parentName = parentName;
   }

}
