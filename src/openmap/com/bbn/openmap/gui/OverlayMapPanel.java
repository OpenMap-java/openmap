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

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.event.OMMouseMode;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.proj.ProjectionStack;
import com.bbn.openmap.util.PropUtils;

/**
 * An extension of the BasicMapPanel that uses an OverlayLayout on the panel in
 * the BorderLayout.CENTER position. Contains a transparent widgets JPanel for
 * placing components floating on top of the map. The default implementation of
 * layoutPanel() adds an EmbeddedNavPanel in the upper left position of the map,
 * as well as a ProjectionStack for it to use.
 * <p>
 * If a property prefix is set on this MapPanel, that property prefix can be
 * used to designate MapPanelChild objects for this MapPanel. The setName
 * variable should be set to true, and the children's parent name should match
 * whatever property prefix is given to the panel.
 */
public class OverlayMapPanel
      extends BasicMapPanel
      implements PropertyChangeListener {

   private static final long serialVersionUID = 1L;

   public final static String ACTIVE_WIDGET_COLOR_PROPERTY = "activeWidgets";
   public final static String INACTIVE_WIDGET_COLOR_PROPERTY = "inactiveWidgets";
   public final static String WIDGET_SIZE_PROPERTY = "widgetSize";
   protected int DEFAULT_WIDGET_BUTTON_SIZE = 15;

   /**
    * May be null, in which case the widgets should decide.
    */
   protected DrawingAttributes activeWidgetColors;
   /**
    * May be null, in which case the widgets should decide.
    */
   protected DrawingAttributes inactiveWidgetColors;
   /**
    * Defaults to 15;
    */
   protected int widgetButtonSize = DEFAULT_WIDGET_BUTTON_SIZE;

   /**
    * A transparent JPanel with a border layout, residing on top of the MapBean.
    */
   protected JPanel widgets;

   private JPanel centerContainer;

   /**
    * Creates an empty OverlayMapPanel that creates its own empty
    * PropertyHandler. The MapPanel will contain a MapBean, a MapHandler,
    * EmbeddedNavPanel and a PropertyHandler with no properties. The constructor
    * to use to create a blank map framework to add components to.
    */
   public OverlayMapPanel() {
      super(new PropertyHandler(new Properties()), false);
   }

   /**
    * Create a OverlayMapPanel with the option of delaying the search for
    * properties until the <code>create()</code> call is made.
    * 
    * @param delayCreation true to let the MapPanel know that the artful
    *        programmer will call <code>create()</code>
    */
   public OverlayMapPanel(boolean delayCreation) {
      super(null, delayCreation);
   }

   /**
    * Create a OverlayMapPanel that configures itself with the properties
    * contained in the PropertyHandler provided. If the PropertyHandler is null,
    * a new one will be created.
    */
   public OverlayMapPanel(PropertyHandler propertyHandler) {
      super(propertyHandler, false);
   }

   /**
    * Create a OverlayMapPanel that configures itself with properties contained
    * in the PropertyHandler provided, and with the option of delaying the
    * search for properties until the <code>create()</code> call is made.
    * 
    * @param delayCreation true to let the MapPanel know that the artful
    *        programmer will call <code>create()</code>
    */
   public OverlayMapPanel(PropertyHandler propertyHandler, boolean delayCreation) {
      super(propertyHandler, delayCreation);
   }

   /**
    * Calls layoutPanel(MapBean), which configures the panel.
    */
   protected void addMapBeanToPanel(MapBean map) {
      layoutPanel(map);
      map.addPropertyChangeListener(this);
   }

   public DrawingAttributes getActiveWidgetColors() {
      return activeWidgetColors;
   }

   public void setActiveWidgetColors(DrawingAttributes activeWidgetColors) {
      this.activeWidgetColors = activeWidgetColors;
   }

   public DrawingAttributes getInactiveWidgetColors() {
      return inactiveWidgetColors;
   }

   public void setInactiveWidgetColors(DrawingAttributes inactiveWidgetColors) {
      this.inactiveWidgetColors = inactiveWidgetColors;
   }

   public int getWidgetButtonSize() {
      return widgetButtonSize;
   }

   public void setWidgetButtonSize(int widgetButtonSize) {
      this.widgetButtonSize = widgetButtonSize;
   }

   /**
    * New method added, called from addMapBeanToPanel(MapBean).
    * 
    * @param map
    */
   protected void layoutPanel(MapBean map) {
      Dimension minimumSize = new Dimension(MapBean.DEFAULT_WIDTH, MapBean.DEFAULT_HEIGHT);

      JPanel hackPanel = new JPanel();
      hackPanel.setLayout(new BorderLayout());
      hackPanel.setOpaque(false);
      hackPanel.add(map, BorderLayout.CENTER);

      centerContainer = new JPanel();
      
      centerContainer.setLayout(new OverlayLayout(centerContainer));

      // These may be null, but the EmbeddedNavPanel will choose it's own
      // default colors if that is so.
      DrawingAttributes activeWidgetColors = getActiveWidgetColors();
      DrawingAttributes inactiveWidgetColors = getInactiveWidgetColors();
      int widgetButtonSize = getWidgetButtonSize();

      EmbeddedNavPanel navPanel = new EmbeddedNavPanel(activeWidgetColors, inactiveWidgetColors, widgetButtonSize);
      navPanel.setBounds(12, 12, navPanel.getMinimumSize().width, navPanel.getMinimumSize().height);

      addMapComponent(navPanel);
      addMapComponent(new ProjectionStack());

      EmbeddedScaleDisplayPanel scaleDisplay = new EmbeddedScaleDisplayPanel();
      addMapComponent(scaleDisplay);

      widgets = new JPanel();
      widgets.setLayout(new BorderLayout());
      widgets.setBackground(OMGraphicConstants.clear);
      widgets.setOpaque(false);
      widgets.setBounds(0, 0, map.getWidth(), map.getHeight());
      widgets.setMinimumSize(minimumSize);
      widgets.add(navPanel, BorderLayout.WEST);
      widgets.add(scaleDisplay, BorderLayout.EAST);

      setBorders(map, widgets);

      centerContainer.add(widgets);
      centerContainer.add(hackPanel);

      add(centerContainer, BorderLayout.CENTER);
   }

   /**
    * If you want different borders or color them differently, override this
    * method.
    * 
    * @param map
    * @param widgets
    */
   protected void setBorders(MapBean map, JPanel widgets) {

      if (map != null) {
         map.setBorder(null);
      }

      if (widgets != null) {
         widgets.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.GRAY, Color.DARK_GRAY));
      }
   }

   /** Include exit in the File menu. Call this before create(). */
   public void includeExitMenuItem() {
      addProperty("quitMenu.class", "com.bbn.openmap.gui.map.QuitMenuItem");
      appendProperty("fileMenu.items", "quitMenu");
   }

   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      DrawingAttributes awc = getActiveWidgetColors();
      if (awc == null) {
         awc = DrawingAttributes.getDefaultClone();
      }
      DrawingAttributes iwc = getInactiveWidgetColors();
      if (iwc == null) {
         iwc = DrawingAttributes.getDefaultClone();
      }

      // If no properties have been set for them, reset to null so the
      // EmbeddedNavPanel default colors are used.
      awc.setProperties(prefix + ACTIVE_WIDGET_COLOR_PROPERTY, props);
      if (awc.equals(DrawingAttributes.getDefaultClone())) {
         awc = null;
      }

      iwc.setProperties(prefix + INACTIVE_WIDGET_COLOR_PROPERTY, props);
      if (iwc.equals(DrawingAttributes.getDefaultClone())) {
         iwc = null;
      }

      setActiveWidgetColors(awc);
      setInactiveWidgetColors(iwc);

      setWidgetButtonSize(PropUtils.intFromProperties(props, prefix + WIDGET_SIZE_PROPERTY, getWidgetButtonSize()));
   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);
      String prefix = PropUtils.getScopedPropertyPrefix(this);

      DrawingAttributes awc = getActiveWidgetColors();
      if (awc != null) {
         awc.setPropertyPrefix(PropUtils.getScopedPropertyPrefix(this) + ACTIVE_WIDGET_COLOR_PROPERTY);
         awc.getProperties(props);
      }

      DrawingAttributes iwc = getInactiveWidgetColors();
      if (iwc != null) {
         iwc.setPropertyPrefix(PropUtils.getScopedPropertyPrefix(this) + INACTIVE_WIDGET_COLOR_PROPERTY);
         iwc.getProperties(props);
      }

      int widgetSize = getWidgetButtonSize();
      if (widgetSize != DEFAULT_WIDGET_BUTTON_SIZE) {
         props.put(prefix + WIDGET_SIZE_PROPERTY, Integer.toString(widgetSize));
      }

      return props;
   }

   /** A main() method that just brings up a JFrame containing the MapPanel. */
   public static void main(String argv[]) {
      SwingUtilities.invokeLater(new Runnable() {

         public void run() {
            JFrame f = new JFrame("Map");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            OverlayMapPanel map = new OverlayMapPanel(new PropertyHandler(new Properties()), true);
            map.create();

            map.getMapBean().setBackgroundColor(new Color(0x99b3cc));
            map.addMapComponent(new LayerHandler());
            map.addMapComponent(new MouseDelegator());
            map.addMapComponent(new OMMouseMode());
            ShapeLayer shapeLayer = new ShapeLayer("share/data/shape/cntry02/cntry02.shp");
            // shapeLayer.setAddAsBackground(true);
            map.addMapComponent(shapeLayer);
            map.includeExitMenuItem();
            f.setJMenuBar(map.getMapMenuBar());
            f.getContentPane().add(map);
            f.setSize(800, 600);
            f.setVisible(true);
         }

      });

   }

   /*
    * (non-Javadoc)
    * 
    * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
    * PropertyChangeEvent)
    */
   public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(MapBean.CursorProperty)) {
         centerContainer.setCursor(((Cursor) evt.getNewValue()));
      }
   }
   
}
