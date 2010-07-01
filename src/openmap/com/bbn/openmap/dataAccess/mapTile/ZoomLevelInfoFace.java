/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;

/**
 * A class that visually manages the settings for a ZoomLevelInfo object. Works
 * inside the MapTileMakerComponent.
 * 
 * @author dietrick
 */
public class ZoomLevelInfoFace
      extends OMComponentPanel {

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.ZoomLevelInfoFace");

   private static final long serialVersionUID = 1L;
   protected ZoomLevelInfo zfi;
   boolean active = false;
   boolean include = false;

   protected JList boundsList;
   protected List<BoundsObject> boundsObjectList;
   protected BoundsListModel boundsModel;
   protected OMGraphicList boundaries = new OMGraphicList();

   protected List<LayerObject> layerList = new ArrayList<LayerObject>();
   protected MapTileMakerComponent organizer;

   protected JPanel layerPanel;
   protected JCheckBox includeButton;
   protected JButton createBoundaryButton;
   protected JButton editBoundaryButton;
   protected JButton deleteBoundaryButton;

   public ZoomLevelInfoFace(ZoomLevelInfo zfi, MapTileMakerComponent mtmc) {

      this.zfi = zfi;
      this.organizer = mtmc;
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      int zl = zfi.getZoomLevel();
      int etc = zfi.getEdgeTileCount();
      String title = i18n.get(ZoomLevelInfoFace.class, "zoom_level", "Zoom Level") + " " + zl;
      String tileString = etc != 1 ? "tiles" : "tile";
      DecimalFormat df = new DecimalFormat("000,000");
      String scale = "1:" + df.format(zfi.getScale());

      JPanel introPanel = new JPanel();

      JLabel intro =
            new JLabel("<html><body><b>" + title + " - </b>" + etc + "x" + etc + " " + tileString + " at scale </body></html>");
      c.gridx = GridBagConstraints.RELATIVE;
      c.insets = new Insets(3, 0, 3, 0);
      c.anchor = GridBagConstraints.WEST;
      introPanel.add(intro, c);

      JButton scaleButton = new JButton(scale);
      scaleButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));

      scaleButton.setToolTipText(i18n.get(ZoomLevelInfoFace.class, "set_map_scale_to", TOOL_TIP_TEXT_KEY, "Set map scale to") + " "
            + scale);
      scaleButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            MapHandler mHandler = (MapHandler) organizer.getBeanContext();
            MapBean map = mHandler.get(MapBean.class);
            map.setScale(getZoomLevelInfo().getScale());
         }
      });

      introPanel.add(scaleButton, c);
      add(introPanel, c);

      includeButton =
            new JCheckBox(
                          i18n.get(ZoomLevelInfoFace.class, "include_this_zoom_level", "Include this zoom level when making tiles"),
                          false);
      includeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            include = ((JToggleButton) ae.getSource()).isSelected();
         }
      });

      c.gridx = GridBagConstraints.REMAINDER;
      c.anchor = GridBagConstraints.WEST;
      add(includeButton, c);

      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0f;
      c.weighty = .1f;

      layerPanel = new JPanel(new GridBagLayout());
      String layers_for_title = i18n.get(ZoomLevelInfoFace.class, "layers_for_title", "Layers for");
      layerPanel.setBorder(BorderFactory.createTitledBorder(layers_for_title + " " + title + " "));
      setLayers(layerList);
      add(layerPanel, c);

      JPanel boundsPanel = new JPanel(new BorderLayout());
      String boundaries_for_title = i18n.get(ZoomLevelInfoFace.class, "boundaries_for_title", "Boundaries for");
      boundsPanel.setBorder(BorderFactory.createTitledBorder(boundaries_for_title + " " + title + " "));
      boundsObjectList = new ArrayList<BoundsObject>();
      boundsModel = new BoundsListModel();
      boundsList = new JList(boundsModel);
      boundsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      boundsList.setLayoutOrientation(JList.VERTICAL);
      boundsList.setVisibleRowCount(-1);
      boundsList.addListSelectionListener(new SelectionListener());
      boundsList.addMouseListener(new ListMouseListener());

      JScrollPane scrollableBoundsList =
            new JScrollPane(boundsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      boundsPanel.add(scrollableBoundsList, BorderLayout.CENTER);

      JPanel boundsButtonPanel = new JPanel();

      ImageIcon ii = createImageIcon("add_16x16.png");
      createBoundaryButton = new JButton(ii);
      String create_a_boundary_rectangle =
            i18n.get(ZoomLevelInfoFace.class, "create_a_boundary_rectangle", "Create a boundary rectangle");
      createBoundaryButton.setToolTipText(create_a_boundary_rectangle);
      createBoundaryButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            organizer.createRectangle();
         }
      });
      boundsButtonPanel.add(createBoundaryButton);

      ii = createImageIcon("edit_16x16.png");
      editBoundaryButton = new JButton(ii);
      String edit_a_selected_boundary_rectangle =
            i18n.get(ZoomLevelInfoFace.class, "edit_a_selected_boundary_rectangle", "Edit a selected boundary rectangle");
      editBoundaryButton.setToolTipText(edit_a_selected_boundary_rectangle);
      editBoundaryButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            if (boundsList.getSelectedIndex() != -1) {
               BoundsObject selected = (BoundsObject) boundsList.getSelectedValue();
               organizer.edit(selected.bounds, null);
            }
         }
      });
      boundsButtonPanel.add(editBoundaryButton);

      ii = createImageIcon("remov_16x16.png");
      deleteBoundaryButton = new JButton(ii);
      String delete_a_selected_boundary_rectangle =
            i18n.get(ZoomLevelInfoFace.class, "delete_a_selected_boundary_rectangle", "Delete a selected boundary rectangle");
      deleteBoundaryButton.setToolTipText(delete_a_selected_boundary_rectangle);
      deleteBoundaryButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            if (boundsList.getSelectedIndex() != -1) {
               BoundsObject selected = (BoundsObject) boundsList.getSelectedValue();
               boundaries.remove(selected.bounds);
               boundsObjectList.remove(selected);
               boundsList.repaint();

               if (organizer.drawingTool.isActivated()) {
                  organizer.drawingTool.deactivate(OMAction.DELETE_GRAPHIC_MASK);
               }

               ((MapHandler) organizer.getBeanContext()).get(MapBean.class).repaint();
            }
         }
      });
      boundsButtonPanel.add(deleteBoundaryButton);

      ii = createImageIcon("push_16x16.png");
      JButton applyToAllZooms = new JButton(ii);
      String apply_boundaries_to_all_zoom_levels =
            i18n.get(ZoomLevelInfoFace.class, "apply_boundaries_to_all_zoom_levels", "Apply boundaries to all zoom levels");
      applyToAllZooms.setToolTipText(apply_boundaries_to_all_zoom_levels);
      applyToAllZooms.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            organizer.pushBoundarySettingsToAll(boundsObjectList);
         }
      });
      boundsButtonPanel.add(applyToAllZooms);

      boundsPanel.add(boundsButtonPanel, BorderLayout.SOUTH);
      c.weighty = .8f;
      add(boundsPanel, c);
   }

   protected void enableBoundaryButtons(boolean setting) {
      createBoundaryButton.setEnabled(setting);

      boolean somethingSelected = boundsList.getSelectedIndex() != -1;

      editBoundaryButton.setEnabled(setting && somethingSelected);
      deleteBoundaryButton.setEnabled(setting && somethingSelected);
   }

   /**
    * Given a set of Layers, look at the internal list and make sure there are
    * layer objects that match. Purges LayerObjects that don't represent layers,
    * and adds LayerObjects as needed. Calls setLayers with LayerObjects.
    * 
    * @param layers
    */
   protected void setLayers(Layer[] layers) {

      List<LayerObject> layerObjects = new ArrayList<LayerObject>();

      for (Layer layer : layers) {
         boolean foundOne = false;
         for (LayerObject lo : layerList) {
            if (lo.getLayer().equals(layer)) {
               foundOne = true;
               layerObjects.add(lo);
               break;
            }
         }

         if (!foundOne) {
            layerObjects.add(new LayerObject(layer));
         }
      }

      setLayers(layerObjects);
   }

   /**
    * Update the layer panel to have buttons for the layer objects. Doesn't do
    * any checking, just adds them to the layerPanel, and adds the button that
    * pushes this ZLIF's layer settings to all other layers.
    * 
    * @param layerObjects
    */
   protected void setLayers(List<LayerObject> layerObjects) {
      this.layerList = layerObjects;

      layerPanel.removeAll();
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0f;
      c.anchor = GridBagConstraints.WEST;

      for (LayerObject lo : layerObjects) {

         String layerMarker = lo.getLayer().getPropertyPrefix();
         if (layerMarker != null) {
            lo.setSelected(zfi.getLayers().contains(layerMarker));
         }

         layerPanel.add(lo, c);
      }

      c.fill = GridBagConstraints.BOTH;
      c.weighty = 1.0f;
      layerPanel.add(Box.createGlue(), c);

      c.fill = GridBagConstraints.NONE;
      c.weighty = 0f;
      c.weightx = 0f;
      c.anchor = GridBagConstraints.CENTER;

      ImageIcon ii = createImageIcon("push_16x16.png");
      JButton applyToAllZooms = new JButton(ii);
      String apply_level_settings_to_all_zoom_levels =
            i18n.get(ZoomLevelInfoFace.class, "apply_level_settings_to_all_zoom_levels", "Apply level settings to all zoom levels");
      applyToAllZooms.setToolTipText(apply_level_settings_to_all_zoom_levels);
      applyToAllZooms.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            organizer.pushLayerSettingsToAll(layerList);
         }
      });
      layerPanel.add(applyToAllZooms, c);
   }

   /**
    * Given a list of LayerObjects, make the visibility of the internal layers
    * match the list.
    * 
    * @param layerObjects
    */
   protected void matchObjects(List<LayerObject> layerObjects) {

      for (LayerObject lo : layerList) {
         for (LayerObject toMatch : layerObjects) {
            Layer matchedLayer = toMatch.getLayer();
            if (lo.getLayer().equals(matchedLayer)) {

               boolean turnOn = toMatch.isSelected();
               String layerMarker = matchedLayer.getPropertyPrefix();

               lo.setSelected(turnOn);

               if (turnOn) {
                  if (!zfi.getLayers().contains(layerMarker)) {
                     zfi.getLayers().add(layerMarker);
                  }
               } else {
                  zfi.getLayers().remove(layerMarker);
               }
            }
         }

         setInclude(!zfi.getLayers().isEmpty());
      }
   }

   public void matchBounds(List<BoundsObject> bounds) {
      boundsModel.clear();
      boundaries.clear();

      for (BoundsObject bo : bounds) {
         BoundsObject copy = bo.clone();
         boundsModel.addElement(copy);
         boundaries.add(bo.bounds);
      }
   }

   /**
    * Whether this ZLIF is the active tab in the MapTileMakerComponent.
    * 
    * @return
    */
   protected boolean isActive() {
      return active;
   }

   /**
    * Set this as the active ZLIF in MapTileMakerComponent.
    * 
    * @param active
    */
   protected void setActive(boolean active) {
      this.active = active;
   }

   /**
    * Whether this ZoomLevel should be used in the tile creation or skipped.
    * 
    * @return
    */
   public boolean isInclude() {
      return include;
   }

   /**
    * Set whether this ZoomLevel should be used in the tile creation.
    * 
    * @param include
    */
   public void setInclude(boolean include) {
      this.include = include;
      includeButton.setSelected(include);
   }

   ZoomLevelInfo getZoomLevelInfo() {
      return zfi;
   }

   void setZoomLevelInfo(ZoomLevelInfo zfi) {
      this.zfi = zfi;
   }

   /**
    * Called from MapTileMakerComponent if this is the active ZLIF when the
    * drawing tool completes, assigning the boundary to this ZLIF (or whatever
    * action is provided).
    * 
    * @param omg
    * @param action
    */
   public void handleBoundary(OMGraphic omg, OMAction action) {
      boundaries.doAction(omg, action);

      boundsModel.clear();
      int count = 1;
      for (OMGraphic omr : boundaries) {
         if (omr instanceof OMRect) {
            String bounding_rectangle = i18n.get(ZoomLevelInfoFace.class, "bounding_rectangle", "Bounding Rectangle");
            boundsModel.addElement(new BoundsObject((OMRect) omr, bounding_rectangle + " " + (count++)));
         }
      }

   }

   /**
    * Called from the MapTileMakerComponent, so this ZLIF is ready to paint its
    * boundaries if it is activated.
    * 
    * @param proj
    * @return
    */
   protected boolean generate(Projection proj) {
      if (boundaries != null) {
         return boundaries.generate(proj);
      }
      return false;
   }

   /**
    * Called from the MapTileMakerComponent, when this is the active ZLIF so the
    * current boundaries are painted on top of the map.
    * 
    * @param graphics
    */
   protected void paintBoundaries(Graphics graphics) {
      if (boundaries != null) {
         boundaries.render(graphics);
      }
   }

   /**
    * Bounds list model for boundary JList.
    * 
    * @author dietrick
    */
   private final class BoundsListModel
         extends AbstractListModel {
      private static final long serialVersionUID = 1L;

      public int getSize() {
         return boundsObjectList.size();
      }

      public Object getElementAt(int index) {
         return boundsObjectList.get(index);
      }

      @SuppressWarnings("unused")
      public void editElement(int index) {
         fireContentsChanged(this, index, index);
      }

      @SuppressWarnings("unused")
      public void insertElement(BoundsObject obj, int index) {
         boundsObjectList.add(index, obj);
         fireIntervalAdded(this, index, index);
      }

      public void addElement(BoundsObject obj) {
         int index = getSize();
         boundsObjectList.add(obj);
         fireIntervalAdded(this, index, index);
      }

      @SuppressWarnings("unused")
      public BoundsObject removeElementAt(int index) {
         BoundsObject obj = boundsObjectList.remove(index);
         fireIntervalRemoved(this, index, index);
         return obj;
      }

      public void clear() {
         int size = boundsObjectList.size();
         boundsObjectList.clear();
         fireIntervalRemoved(this, 0, size);
      }
   }

   /**
    * The list object used to represent a boundary.
    * 
    * @author dietrick
    */
   public class BoundsObject
         implements Cloneable {
      protected OMRect bounds;
      protected String name;

      public BoundsObject(OMRect rect, String displayName) {
         bounds = rect;
         name = displayName;
      }

      public String toString() {
         return name;
      }

      public BoundsObject clone() {
         OMRect copy =
               new OMRect(bounds.getNorthLat(), bounds.getWestLon(), bounds.getSouthLat(), bounds.getEastLon(),
                          OMGraphic.LINETYPE_RHUMB);
         DrawingAttributes atts = DrawingAttributes.getDefaultClone();
         atts.setFrom(bounds);
         atts.setTo(copy);
         return new BoundsObject(bounds, name);
      }
   }

   /**
    * A component used to represent a layer/layer setting in the face.
    * 
    * @author dietrick
    */
   public class LayerObject
         extends JCheckBox {
      private static final long serialVersionUID = 1L;
      protected Layer layer;

      public LayerObject(Layer layer) {
         super(layer.getName(), layer.isVisible());
         this.layer = layer;
         addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               String pp = getLayer().getPropertyPrefix();
               if (((JCheckBox) ae.getSource()).isSelected()) {
                  if (!zfi.getLayers().contains(pp)) {
                     zfi.getLayers().add(pp);
                  }
               } else {
                  zfi.getLayers().remove(pp);
               }

               if (active /* duh */&& organizer != null) {
                  organizer.shuffleLayers(ZoomLevelInfoFace.this);
                  setInclude(zfi.getLayers() != null && !zfi.getLayers().isEmpty());
               }
            }
         });
      }

      Layer getLayer() {
         return layer;
      }

      public String toString() {
         return layer.getName();
      }
   }

   /**
    * A class that listens for selections on the boundary list.
    * 
    * @author dietrick
    */
   private final class SelectionListener
         implements ListSelectionListener {

      public void valueChanged(ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
            boolean somethingSelected = boundsList.getSelectedIndex() != -1;
            editBoundaryButton.setEnabled(somethingSelected);
            deleteBoundaryButton.setEnabled(somethingSelected);
         }
      }
   }

   /**
    * A class that listens for double-clicks on the boundary list, launching an
    * editor for that rectangle.
    * 
    * @author dietrick
    */
   private class ListMouseListener
         extends MouseAdapter {
      @Override
      public void mouseClicked(MouseEvent e) {
         if (e.getClickCount() == 2) {
            int index = boundsList.locationToIndex(e.getPoint());
            if (index != -1) {
               BoundsObject selected = (BoundsObject) boundsList.getSelectedValue();
               organizer.edit(selected.bounds, null);
            }
         }
      }
   }

   public ImageIcon createImageIcon(String path) {
      URL imgURL = ZoomLevelInfoFace.class.getClassLoader().getResource("com/bbn/openmap/dataAccess/mapTile/" + path);
      if (imgURL != null) {
         return new ImageIcon(imgURL);
      } else {
         System.err.println("Couldn't find file: " + path);
         return null;
      }
   }
}
