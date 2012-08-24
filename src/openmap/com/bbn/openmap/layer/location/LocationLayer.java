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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/LocationLayer.java,v $
// $RCSfile: LocationLayer.java,v $
// $Revision: 1.9 $
// $Date: 2006/01/18 17:44:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.bbn.openmap.I18n;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.layer.DeclutterMatrix;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * The LocationLayer is a layer that displays graphics supplied by
 * LocationHandlers. When the layer receives a new projection, it goes to each
 * LocationHandler and asks it for additions to the layer's graphic list. The
 * LocationHandlers maintain the graphics, and the layer maintains the overall
 * list.
 * 
 * The whole idea behind locations is that there are geographic places that are
 * to be marked with a graphic, and/or a text label. The location handler
 * handles the interface with the source and type of location to be displayed,
 * and the LocationLayer deals with all the locations in a generic way. The
 * LocationLayer is capable of using more than one LocationHandler.
 * <P>
 * 
 * As a side note, a Link is nothing more than a connection between Locations,
 * and is an extension of the Location Class. They have a graphic representing
 * the link, an optional label, and an extra set of location coordinates.
 * <P>
 * 
 * The layer responds to gestures with pop-up menus. Which menu appears depends
 * if the gesture affects a graphic.
 * <P>
 * 
 * The properties for this layer are:
 * <P>
 * 
 * <pre>
 * 
 *   ####################################
 *   # Properties for LocationLayer
 *   # Use the DeclutterMatrix to declutter the labels.
 *   locationlayer.useDeclutter=false
 *   # Which declutter matrix class to use.
 *   locationlayer.declutterMatrix=com.bbn.openmap.layer.DeclutterMatrix
 *   # Let the DeclutterMatrix have labels that run off the edge of the map.
 *   locationlayer.allowPartials=true
 *   # The list of location handler prefixes - each prefix should then
 *   # be used to further define the location handler properties.
 *   locationlayer.locationHandlers=handler1 handler2
 *   # Then come the handler properties...
 *   # At the least, each handler should have a .class property
 *   handler1.class=&lt;handler classname&gt;
 *   # plus any other properties handler1 needs - check the handler1 documentation.
 *   ####################################
 * 
 * </pre>
 */
public class LocationLayer
      extends OMGraphicHandlerLayer {

   /** The declutter matrix to use, if desired. */
   protected DeclutterMatrix declutterMatrix = null;
   /** Flag to use declutter matrix or not. */
   protected boolean useDeclutterMatrix = false;
   /**
    * Flag to let objects appear partially off the edges of the map, when
    * decluttering through the declutter matrix.
    */
   protected boolean allowPartials = true;
   /** Handlers load the data, and manage it for the layer. */
   protected LocationHandler[] dataHandlers;
   /** Pretty names for the handlers, for GUIs and such. */
   protected String[] dataHandlerNames;

   // ///////////////////
   // Variables to manage the gesturing mechanisms

   static final public String recenter = "Re-center map";
   static final public String cancel = "Cancel";

   public static final String UseDeclutterMatrixProperty = "useDeclutter";
   public static final String DeclutterMatrixClassProperty = "declutterMatrix";
   public static final String AllowPartialsProperty = "allowPartials";
   public static final String LocationHandlerListProperty = "locationHandlers";

   /**
    * The default constructor for the Layer. All of the attributes are set to
    * their default values.
    */
   public LocationLayer() {
      setRenderPolicy(new BufferedImageRenderPolicy(this));
      setMouseModeIDsForEvents(new String[] {
         "Gestures"
      });
   }

   /**
    * The properties and prefix are managed and decoded here, for the standard
    * uses of the LocationLayer.
    * 
    * @param prefix string prefix used in the properties file for this layer.
    * @param properties the properties set in the properties file.
    */
   public void setProperties(String prefix, Properties properties) {
      super.setProperties(prefix, properties);
      String realPrefix = "";

      if (prefix != null) {
         realPrefix = prefix + ".";
      }

      setLocationHandlers(realPrefix, properties);
      declutterMatrix = (DeclutterMatrix) PropUtils.objectFromProperties(properties, realPrefix + DeclutterMatrixClassProperty);
      allowPartials = PropUtils.booleanFromProperties(properties, realPrefix + AllowPartialsProperty, true);

      if (declutterMatrix != null) {
         useDeclutterMatrix =
               PropUtils.booleanFromProperties(properties, realPrefix + UseDeclutterMatrixProperty, useDeclutterMatrix);
         declutterMatrix.setAllowPartials(allowPartials);
         Debug.message("location", "LocationLayer: Found DeclutterMatrix to use");
         // declutterMatrix.setXInterval(3);
         // declutterMatrix.setYInterval(3);
      } else {
         useDeclutterMatrix = false;
      }
   }

   public void setDeclutterMatrix(DeclutterMatrix dm) {
      declutterMatrix = dm;
   }

   public DeclutterMatrix getDeclutterMatrix() {
      return declutterMatrix;
   }

   public void setUseDeclutterMatrix(boolean set) {
      useDeclutterMatrix = set;

      if (declutterButton != null) {
         declutterButton.setSelected(useDeclutterMatrix);
      }
   }

   public boolean getUseDeclutterMatrix() {
      return useDeclutterMatrix;
   }

   /**
    * Tell the location handlers to reload their data from their sources. If you
    * want these changes to appear on the map, you should call doPrepare() after
    * this call.
    */
   public void reloadData() {
      if (dataHandlers != null) {
         for (int i = 0; i < dataHandlers.length; i++) {
            dataHandlers[i].reloadData();
         }
      }
   }

   /**
    * Prepares the graphics for the layer. This is where the getRectangle()
    * method call is made on the location.
    * <p>
    * Occasionally it is necessary to abort a prepare call. When this happens,
    * the map will set the cancel bit in the LayerThread, (the thread that is
    * running the prepare). If this Layer needs to do any cleanups during the
    * abort, it should do so, but return out of the prepare asap.
    */
   public synchronized OMGraphicList prepare() {

      if (isCancelled()) {
         if (Debug.debugging("location")) {
            Debug.output(getName() + "|LocationLayer.prepare(): aborted.");
         }
         return null;
      }

      OMGraphicList omGraphicList = new OMGraphicList();
      omGraphicList.setTraverseMode(OMGraphicList.FIRST_ADDED_ON_TOP);

      Projection projection = getProjection();

      if (projection == null) {
         if (Debug.debugging("location")) {
            Debug.output(getName() + "|LocationLayer.prepare(): null projection, layer not ready.");
         }
         return omGraphicList;
      }

      if (Debug.debugging("location")) {
         Debug.output(getName() + "|LocationLayer.prepare(): doing it");
      }

      if (useDeclutterMatrix && declutterMatrix != null) {
         declutterMatrix.setWidth(projection.getWidth());
         declutterMatrix.setHeight(projection.getHeight());
         declutterMatrix.create();
      }

      // Setting the OMGraphicsList for this layer. Remember, the
      // Vector is made up of OMGraphics, which are generated
      // (projected) when the graphics are added to the list. So,
      // after this call, the list is ready for painting.

      // call getRectangle();
      if (Debug.debugging("location")) {
         Debug.output(getName() + "|LocationLayer.prepare(): " + "calling prepare with projection: " + projection + " ul = "
               + projection.getUpperLeft() + " lr = " + projection.getLowerRight());
      }

      Point2D ul = projection.getUpperLeft();
      Point2D lr = projection.getLowerRight();

      if (Debug.debugging("location")) {
         double delta = lr.getX() - ul.getX();
         Debug.output(getName() + "|LocationLayer.prepare(): " + " ul.lon =" + ul.getX() + " lr.lon = " + lr.getY() + " delta = "
               + delta);
      }
      if (dataHandlers != null) {
         for (int i = 0; i < dataHandlers.length; i++) {
            ((LocationHandler) dataHandlers[i]).get((float) ul.getY(), (float) ul.getX(), (float) lr.getY(), (float) lr.getX(),
                                                    omGraphicList);
         }
      }

      // ///////////////////
      // safe quit
      int size = 0;
      if (omGraphicList != null) {
         size = omGraphicList.size();
         if (Debug.debugging("basic")) {
            Debug.output(getName() + "|LocationLayer.prepare(): finished with " + size + " graphics");
         }

         // Don't forget to project them. Since they are only
         // being recalled if the projection has changed, then
         // we need to force a re-projection of all of them
         // because the screen position has changed.
         for (OMGraphic thingy : omGraphicList) {
            if (useDeclutterMatrix && thingy instanceof Location) {
               Location loc = (Location) thingy;
               loc.generate(projection, declutterMatrix);
            } else {
               thingy.generate(projection);
            }
         }
      } else if (Debug.debugging("basic")) {
         Debug.output(getName() + "|LocationLayer.prepare(): finished with null graphics list");
      }

      return omGraphicList;
   }

   /**
    * Paints the layer.
    * 
    * @param g the Graphics context for painting
    */
   public void paint(java.awt.Graphics g) {
      if (Debug.debugging("location")) {
         Debug.output(getName() + "|LocationLayer.paint()");
      }

      OMGraphicList omgList = getList();

      if (omgList != null) {

         // Draw from the bottom up, so it matches the palette, and
         // the order in which the handlers were loaded - the first
         // in the list is on top.

         // We need to go through list twice. The first time, draw
         // all the regular OMGraphics, and also draw all of the
         // graphics for the locations. The second time through,
         // draw the labels. This way, the labels won't be covered
         // up by graphics.

         // render locations
         for (OMGraphic omg : omgList) {
            if (omg instanceof Location) {
               ((Location) omg).renderLocation(g);
            } else {
               omg.render(g);
            }
         }

         // Now render labels
         for (OMGraphic omg : omgList) {
            if (omg instanceof Location) {
               ((Location) omg).renderName(g);
            }
         }

      } else {
         if (Debug.debugging("location")) {
            Debug.error(getName() + "|LocationLayer: paint(): Null list...");
         }
      }
   }

   /**
    * Parse the properties and set up the location handlers. The prefix will
    * should be null, or a prefix string with a period at the end, for scoping
    * purposes.
    */
   protected void setLocationHandlers(String prefix, Properties p) {

      String handlersValue = p.getProperty(prefix + LocationHandlerListProperty);

      if (Debug.debugging("location")) {
         Debug.output(getName() + "| handlers = \"" + handlersValue + "\"");
      }

      if (handlersValue == null) {
         if (Debug.debugging("location")) {
            Debug.output("No property \"" + prefix + LocationHandlerListProperty + "\" found in application properties.");
         }
         return;
      }

      // Divide up the names ...
      StringTokenizer tokens = new StringTokenizer(handlersValue, " ");
      Vector handlerNames = new Vector();
      while (tokens.hasMoreTokens()) {
         handlerNames.addElement(tokens.nextToken());
      }

      if (Debug.debugging("location")) {
         Debug.output("OpenMap.getLocationHandlers(): " + handlerNames);
      }

      int nHandlerNames = handlerNames.size();
      Vector handlers = new Vector(nHandlerNames);
      Vector goodNames = new Vector(nHandlerNames);
      for (int i = 0; i < nHandlerNames; i++) {
         String handlerName = (String) handlerNames.elementAt(i);
         String classProperty = handlerName + ".class";
         String className = p.getProperty(classProperty);

         String nameProperty = handlerName + ".prettyName";
         String prettyName = p.getProperty(nameProperty);

         if (className == null) {
            Debug.error("Failed to locate property \"" + classProperty + "\"\nSkipping handler \"" + handlerName + "\"");
            continue;
         }
         try {
            if (Debug.debugging("location")) {
               Debug.output("OpenMap.getHandlers():instantiating handler \"" + className + "\"");
            }

            Object obj = Class.forName(className).newInstance();// Works
            // for
            // applet!
            if (obj instanceof LocationHandler) {
               LocationHandler lh = (LocationHandler) obj;
               lh.setProperties(handlerName, p);
               lh.setLayer(this);
               handlers.addElement(lh);
               goodNames.addElement(prettyName != null ? prettyName : "");
            }
            if (false)
               throw new java.io.IOException();// fool javac
            // compiler
         } catch (java.lang.ClassNotFoundException e) {
            Debug.error("Handler class not found: \"" + className + "\"\nSkipping handler \"" + handlerName + "\"");
         } catch (java.io.IOException e) {
            Debug.error("IO Exception instantiating class \"" + className + "\"\nSkipping handler \"" + handlerName + "\"");
         } catch (Exception e) {
            Debug.error("Exception instantiating class \"" + className + "\": " + e);
         }
      }

      int nHandlers = handlers.size();

      dataHandlers = new LocationHandler[nHandlers];
      dataHandlerNames = new String[nHandlers];

      if (nHandlers != 0) {
         handlers.copyInto(dataHandlers);
         goodNames.copyInto(dataHandlerNames);
      }

   }

   /**
    * Let the LocationHandlers know that the layer has been removed.
    */
   public void removed(java.awt.Container cont) {
      if (dataHandlers != null) {
         for (int i = 0; i < dataHandlers.length; i++) {
            ((LocationHandler) dataHandlers[i]).removed(cont);
         }
      }
   }

   /**
    * Set the LocationHandlers for the layer. Make sure you update the
    * LocationHandler names, too, so the names correspond to these.
    * 
    * @param handlers an array of LocationHandlers.
    */
   public void setLocationHandlers(LocationHandler[] handlers) {
      dataHandlers = handlers;
      // Need to set the layer on the handlers.
      for (int i = 0; i < handlers.length; i++) {
         handlers[i].setLayer(this);
      }
      resetPalette();
   }

   /**
    * Get the LocationHandlers for this layer.
    */
   public LocationHandler[] getLocationHandlers() {
      return dataHandlers;
   }

   /**
    * Set the LocationHandler names suitable for a GUI. Make sure these end up
    * corresponding to the LocationHandlers.
    * 
    * @param handlerNames an array of Strings.
    */
   public void setLocationHandlerNames(String[] handlerNames) {
      dataHandlerNames = handlerNames;
      resetPalette();
   }

   /**
    * Get the LocationHandlers for this layer.
    */
   public String[] getLocationHandlerNames() {
      return dataHandlerNames;
   }

   /**
    * Called when the LocationHandlers are reset, or their names are reset, to
    * refresh the palette with the new information.
    */
   protected void resetPalette() {
      box = null;
      super.resetPalette();
   }

   /**
    * Overridden from Layer because we are placing our own scroll pane around
    * the LocationHandler GUIs.
    */
   protected WindowSupport createWindowSupport() {
      return new WindowSupport(getGUI(), getName());
   }

   // ----------------------------------------------------------------------
   // GUI
   // ----------------------------------------------------------------------

   protected Box box = null;
   protected JCheckBox declutterButton = null;

   /**
    * Provides the palette widgets to control the options of showing maps, or
    * attribute text.
    * 
    * @return Component object representing the palette widgets.
    */
   public java.awt.Component getGUI() {
      if (box == null) {
         box = Box.createVerticalBox();
         int nHandlers = 0;

         if (dataHandlers != null) {
            nHandlers = dataHandlers.length;
         }

         JPanel[] panels = new JPanel[nHandlers];
         Box box2 = Box.createVerticalBox();
         for (int i = 0; i < nHandlers; i++) {

            String handlerName;
            if (dataHandlerNames != null && i < dataHandlerNames.length) {
               handlerName = dataHandlerNames[i];
            } else {
               handlerName = "";
            }

            panels[i] = PaletteHelper.createPaletteJPanel(handlerName);
            panels[i].add(dataHandlers[i].getGUI());
            // box.add(panels[i]);
            box2.add(panels[i]);
         }

         JScrollPane scrollPane =
               new JScrollPane(box2, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

         scrollPane.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
         scrollPane.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);

         box.add(scrollPane);

         if (declutterMatrix != null) {
            JPanel dbp = new JPanel(new GridLayout(0, 1));

            declutterButton = new JCheckBox(i18n.get(LocationLayer.class, "declutterNames", "Declutter Names"), useDeclutterMatrix);
            declutterButton.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent ae) {
                  JCheckBox jcb = (JCheckBox) ae.getSource();
                  useDeclutterMatrix = jcb.isSelected();
                  if (isVisible()) {
                     doPrepare();
                  }
               }
            });
            declutterButton.setToolTipText(i18n.get(LocationLayer.class, "declutterNames", I18n.TOOLTIP,
                                                    "<HTML><BODY>Move location names so they don't overlap.<br>This may take awhile if you are zoomed out.</BODY></HTML>"));
            dbp.add(declutterButton);
            box.add(dbp);
         }
      }
      return box;
   }

   public String getToolTipTextFor(OMGraphic omg) {
      String ttText = null;
      if (omg instanceof Location) {
         ttText = ((Location) omg).getName();
      }
      return ttText;
   }

   public List<Component> getItemsForOMGraphicMenu(OMGraphic omg) {
      if (omg instanceof Location) {
         Location loc = (Location) omg;
         LocationHandler lh = loc.getLocationHandler();
         if (lh != null) {
            return lh.getItemsForPopupMenu(loc);
         }
      }

      return null;
   }

   // protected void showLocationPopup(MouseEvent evt, Location loc, MapBean
   // map) {
   // if (locMenu == null) {
   // locMenu = new LocationPopupMenu();
   // locMenu.setMap(map);
   // }
   // locMenu.removeAll();
   //
   // locMenu.setEvent(evt);
   // locMenu.setLoc(loc);
   //
   // locMenu.add(new LocationMenuItem(LocationLayer.recenter, locMenu, this));
   // locMenu.add(new LocationMenuItem(LocationLayer.cancel, locMenu, this));
   // locMenu.addSeparator();
   //
   //
   // locMenu.show(this, evt.getX(), evt.getY());
   // }

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
      props = super.getProperties(props);

      String prefix = PropUtils.getScopedPropertyPrefix(this);

      props.put(prefix + UseDeclutterMatrixProperty, new Boolean(useDeclutterMatrix).toString());

      if (declutterMatrix != null) {
         props.put(prefix + DeclutterMatrixClassProperty, declutterMatrix.getClass().getName());
         props.put(prefix + AllowPartialsProperty, new Boolean(declutterMatrix.isAllowPartials()).toString());
      }

      StringBuffer handlerList = new StringBuffer();

      // Need to hand this off to the location handlers, and build a
      // list of marker names to use in the LocationLayer property
      // list.
      if (dataHandlers != null) {
         for (int i = 0; i < dataHandlers.length; i++) {
            String pp = dataHandlers[i].getPropertyPrefix();
            handlerList.append(" ").append(pp);
            props.put(pp + ".prettyName", dataHandlerNames[i]);
            dataHandlers[i].getProperties(props);
         }
      }

      props.put(prefix + LocationHandlerListProperty, handlerList.toString());

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
      list = super.getPropertyInfo(list);

      PropUtils.setI18NPropertyInfo(i18n, list, LocationLayer.class, UseDeclutterMatrixProperty, "Use Declutter Matrix",
                                    "Flag for using the declutter matrix.",
                                    "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

      PropUtils.setI18NPropertyInfo(i18n, list, LocationLayer.class, DeclutterMatrixClassProperty, "Declutter Matrix Class",
                                    "Class name of the declutter matrix to use (com.bbn.openmap.layer.DeclutterMatrix).", null);

      PropUtils.setI18NPropertyInfo(i18n, list, LocationLayer.class, AllowPartialsProperty, "Allow partials",
                                    "Flag to allow labels to run off the edge of the map.",
                                    "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

      PropUtils.setI18NPropertyInfo(i18n,
                                    list,
                                    LocationLayer.class,
                                    LocationHandlerListProperty,
                                    "Location Handlers",
                                    "Space-separated list of unique names to use to scope the LocationHandler property definitions.",
                                    null);

      if (dataHandlers != null) {
         for (int i = 0; i < dataHandlers.length; i++) {
            dataHandlers[i].getPropertyInfo(list);
         }
      }

      return list;
   }
}
