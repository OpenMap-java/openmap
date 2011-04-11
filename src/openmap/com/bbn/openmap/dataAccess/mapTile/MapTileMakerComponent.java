/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.dataAccess.mapTile.ZoomLevelMakerFace.BoundsObject;
import com.bbn.openmap.dataAccess.mapTile.ZoomLevelMakerFace.LayerObject;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.MapMouseSupport;
import com.bbn.openmap.event.PaintListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.gui.MapPanelChild;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.image.ImageServer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMRect;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.drawing.OMRectLoader;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.TaskService;

/**
 * The MapTileMakerComponent is a GUI for creating properties that the
 * MapTileMaker uses to create tiles. You can add it to the MapHandler
 * (programmatically or via the openmap.components list in the
 * openmap.properties file) and it will attach itself to the left of the map. It
 * takes some properties itself:
 * <p>
 * 
 * <pre>
 * # the class definition, assuming that tileMakerComp was used as the marker name in the properties file.
 * tileMakerComp.class=com.bbn.openmap.dataAccess.mapTile.MapTileMakerComponent
 * # the path to the directory where tiles should be created.
 * tileMakerComp.rootDir=/dev/openmap/tiles
 * # a path to a file where the properties that the gui creates should be written.
 * tileMakerComp.file=/dev/openmap/tilemaker.properties
 * </pre>
 * 
 * <p>
 * There are some things left to do.
 * <ul>
 * <li>The component should read the file and set all the components to the last
 * settings contained in it.
 * <li>It should allow you to choose where the tiles should go instead of having
 * that location only defined in the properties file.
 * <li>It should let you choose image tile formats, right now it's defaulting to
 * png.
 * <li>Canceling the worker thread doesn't seem to be working, the thread isn't
 * responding to being canceled.
 * </ul>
 * 
 * @author dietrick
 */
public class MapTileMakerComponent
      extends OMComponentPanel
      implements MapPanelChild, LayerListener, DrawingToolRequestor, PaintListener, ProjectionListener {

   protected Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.MapTileMakerComponent");

   public final static String TILE_MAKER_PROPERTIES_FILE = "file";

   private static final long serialVersionUID = 1L;
   protected String preferredLocation = BorderLayout.WEST;
   protected String parentName = null;
   protected String fileName = null;
   protected String rootDir = null;
   protected boolean transparentTiles = false;

   protected LayerHandler layerHandler;

   protected List<ZoomLevelMakerFace> faces = new ArrayList<ZoomLevelMakerFace>();
   protected ZoomLevelMakerFace activeFace;

   protected OMDrawingTool drawingTool;
   protected DrawingAttributes rectDA = DrawingAttributes.getDefaultClone();

   public MapTileMakerComponent() {
      init();
   }

   protected void init() {

      drawingTool = new OMDrawingTool();
      drawingTool.getMouseMode().setVisible(false);
      drawingTool.getMouseMode().setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      drawingTool.addLoader(new OMRectLoader());

      GridBagLayout selfLayout = new GridBagLayout();
      setLayout(selfLayout);
      GridBagLayout outerLayout = new GridBagLayout();
      JPanel mainPanel = new JPanel(outerLayout);
      String map_tile_maker = i18n.get(MapTileMakerComponent.class, "map_tile_maker", "Map Tile Maker");
      mainPanel.setBorder(BorderFactory.createTitledBorder(map_tile_maker));
      GridBagConstraints outerC = new GridBagConstraints();
      outerC.insets = new Insets(0, 5, 5, 5);
      outerC.gridx = GridBagConstraints.REMAINDER;
      outerC.fill = GridBagConstraints.BOTH;
      outerC.weightx = 1.0f;
      outerC.weighty = 1.0f;

      String zoom_level = i18n.get(MapTileMakerComponent.class, "zoom_level", "Zoom Level");

      ZoomLevelMakerFace zlif = new ZoomLevelMakerFace(new ZoomLevelMaker(zoom_level + " " + 0, "", 0), this);
      faces.add(zlif);
      mainPanel.add(zlif, outerC);

      JPanel masterOptions = new JPanel(new GridBagLayout());
      String use_transparent_background =
            i18n.get(MapTileMakerComponent.class, "use_transparent_background", "Use transparent background for tiles");
      JCheckBox transparentButton = new JCheckBox(use_transparent_background, transparentTiles);
      transparentButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            transparentTiles = ((JCheckBox) ae.getSource()).isSelected();
         }
      });

      GridBagConstraints optC = new GridBagConstraints();
      optC.anchor = GridBagConstraints.WEST;
      optC.fill = GridBagConstraints.HORIZONTAL;
      optC.weightx = 1f;
      optC.gridx = GridBagConstraints.RELATIVE;

      masterOptions.add(transparentButton, optC);
      outerC.weighty = 0f;
      mainPanel.add(masterOptions, outerC);

      String make_tiles = i18n.get(MapTileMakerComponent.class, "make_tiles", "Make Tiles");
      JButton launchButton = new JButton(make_tiles);
      launchButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            confirmLaunchTileMaker();
         }
      });

      outerC.fill = GridBagConstraints.NONE;
      outerC.weightx = 0f;
      outerC.weighty = 0f;
      mainPanel.add(launchButton, outerC);
      outerC.fill = GridBagConstraints.BOTH;
      outerC.weightx = 1.0f;
      outerC.weighty = 1.0f;
      add(mainPanel, outerC);

      resetActive(faces.get(0));

   }

   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);

      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      rootDir = props.getProperty(prefix + MapTileMaker.ROOT_DIRECTORY_PROPERTY);
      fileName = props.getProperty(prefix + TILE_MAKER_PROPERTIES_FILE);
   }

   /**
    * 
    */
   protected void confirmLaunchTileMaker() {
      Frame frame = JOptionPane.getFrameForComponent(this);
      String confirm_configuration = i18n.get(MapTileMakerComponent.class, "confirm_configuration", "Confirm Configuration");
      JDialog dialog = new ConfirmationDialog(frame, null, confirm_configuration, this);
      dialog.setVisible(true);
   }

   protected void launchTileMaker(Properties props) {

      TreeSet<Object> sortedKeys = new TreeSet<Object>(props.keySet());
      if (fileName != null) {

         try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(fileName));
            for (Object key : sortedKeys) {
               bWriter.write(key + "=" + props.get(key) + "\n");
            }

            bWriter.flush();
            bWriter.close();
         } catch (IOException e) {
            logger.warning("caught exception writing out properties file");
            e.printStackTrace();
         }
      }

      final TileRunnable runner = new TileRunnable(props);
      Thread runnerThread = new Thread(runner);
      runnerThread.start();

      Frame frame = JOptionPane.getFrameForComponent(MapTileMakerComponent.this);

      JButton[] buttons = new JButton[1];
      JButton button1 = new JButton("Cancel");
      button1.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            runner.cancel();
         }
      });

      buttons[0] = button1;
      String creating_map_tiles = i18n.get(MapTileMakerComponent.class, "creating_map_tiles", "Creating Map Tiles...");
      JOptionPane pane =
            new JOptionPane(creating_map_tiles, JOptionPane.INFORMATION_MESSAGE, JOptionPane.CANCEL_OPTION, null, buttons, button1);

      cancelDialog = pane.createDialog(frame, "MapTileMaker Running");
      // cancelDialog.setModalityType(ModalityType.APPLICATION_MODAL);
      cancelDialog.setModal(true);
      cancelDialog.setVisible(true);
   }

   protected Dialog cancelDialog;

   /**
    * Called when a new tab is selected and a new ZLIF is active.
    * 
    * @param activeZlif
    */
   protected void resetActive(ZoomLevelMakerFace activeZlif) {
      activeFace = activeZlif;
      for (ZoomLevelMakerFace zlif : faces) {
         zlif.setActive(zlif.equals(activeZlif));
      }

      shuffleLayers(activeZlif);
      activeFace.enableBoundaryButtons(!drawingTool.isActivated());
      // Needed in case there aren't layers active in the new ZLIF and the
      // drawing tool is active.

      MapHandler mHandler = (MapHandler) getBeanContext();
      if (mHandler != null) {
         mHandler.get(MapBean.class).repaint();
      }
   }

   protected void shuffleLayers(ZoomLevelMakerFace activeZlif) {
      if (layerHandler != null) {
         Layer[] layers = layerHandler.getLayers();
         for (Layer layer : layers) {
            for (LayerObject lo : activeZlif.layerList) {

               if (lo.getLayer().equals(layer)) {
                  layer.setVisible(lo.isSelected());
               }
            }
         }
         layerHandler.setLayers();
      }
   }

   public void findAndInit(Object someObj) {
      super.findAndInit(someObj);

      if (someObj instanceof LayerHandler) {
         layerHandler = (LayerHandler) someObj;
         layerHandler.addLayerListener(this);
      }

      if (drawingTool != null) {
         drawingTool.findAndInit(someObj);
      }

      if (someObj instanceof MapBean) {
         MapBean map = (MapBean) someObj;
         map.addProjectionListener(this);
         map.addPaintListener(this);
      }
   }

   /**
    * Set the parent name that the MapPanelChild should attach itself too.
    * 
    * @param name
    */
   public void setParentName(String name) {
      this.parentName = name;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.bbn.openmap.gui.MapPanelChild#getParentName()
    */
   public String getParentName() {
      return parentName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.bbn.openmap.gui.MapPanelChild#getPreferredLocation()
    */
   public String getPreferredLocation() {
      return preferredLocation;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.bbn.openmap.gui.MapPanelChild#setPreferredLocation(java.lang.String)
    */
   public void setPreferredLocation(String string) {
      this.preferredLocation = string;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.bbn.openmap.event.LayerListener#setLayers(com.bbn.openmap.event.LayerEvent
    * )
    */
   public void setLayers(LayerEvent evt) {
      if (evt.getType() == LayerEvent.ALL) {
         Layer[] layers = evt.getLayers();

         for (ZoomLevelMakerFace zlif : faces) {
            zlif.setLayers(layers);
         }
      }
   }

   /**
    * @param layerObjects
    */
   protected void pushLayerSettingsToAll(List<LayerObject> layerObjects) {
      for (ZoomLevelMakerFace zlif : faces) {
         zlif.matchObjects(layerObjects);
      }
   }

   /**
    * @param boundsList
    */
   protected void pushBoundarySettingsToAll(List<BoundsObject> boundsList) {
      List<BoundsObject> copy = new ArrayList<BoundsObject>();
      copy.addAll(boundsList);

      for (ZoomLevelMakerFace zlif : faces) {
         zlif.matchBounds(copy);
      }
   }

   /**
    * Create a boundary rectangle from scratch, expecting the user to draw the
    * rectangle.
    */
   public void createRectangle() {
      GraphicAttributes ga = new GraphicAttributes();
      rectDA.setTo(ga);
      ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
      ga.setLineType(OMGraphic.LINETYPE_RHUMB);

      EditableOMRect eomr = new EditableOMRect(ga);

      eomr.setXorRendering(false);
      create(eomr);
   }

   /**
    * Called when the OMGraphic should be edited by the drawing tool. For
    * existing OMGraphics that have been clicked on, to start the editing
    * process.
    * 
    * @param omg OMGraphic to edit
    * @param mevent the last MouseEvent that caused the edit to occur, can be
    *        null.
    */
   public void edit(OMGraphic omg, MouseEvent mevent) {

      OMDrawingTool dt = getDrawingTool();

      if (dt != null && dt.canEdit(omg.getClass())) {

         dt.resetBehaviorMask();

         MouseDelegator mDelegator = dt.getMouseDelegator();
         MapMouseMode omdtmm = dt.getMouseMode();
         if (!omdtmm.isVisible()) {
            dt.setMask(OMDrawingTool.PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK);
         }

         if (dt.select(omg, this, mevent)) {

            if (activeFace != null) {
               activeFace.enableBoundaryButtons(!drawingTool.isActivated());
            }

            // OK, means we're editing - let's lock up the
            // MouseMode
            dt.getCurrentEditable().getStateMachine().setSelected();
            // Check to see if the DrawingToolMouseMode wants to
            // be invisible. If it does, ask the current
            // active MouseMode to be the proxy for it...
            if (!omdtmm.isVisible() && mDelegator != null) {
               MapMouseMode mmm = mDelegator.getActiveMouseMode();
               if (mmm.actAsProxyFor(omdtmm, MapMouseSupport.PROXY_DISTRIB_MOUSE_MOVED
                     & MapMouseSupport.PROXY_DISTRIB_MOUSE_DRAGGED & MapMouseSupport.PROXY_DISTRIB_MOUSE_CLICKED)) {
                  setProxyMouseMode(mmm);
               } else {
                  // WHOA, couldn't get proxy lock - bail
                  dt.deactivate();
               }
            }
         }
      }
   }

   /**
    * Launch the drawing tool to create a new EditableOMGraphic, which is
    * undefined at this point.
    * 
    * @param omg EditableOMGraphic in an undefiend state.
    * @return OMGraphic created for drawing tool.
    */
   protected OMGraphic create(EditableOMGraphic omg) {

      OMDrawingTool dt = getDrawingTool();
      OMGraphic ret = null;
      if (dt != null) {

         dt.resetBehaviorMask();

         MapMouseMode omdtmm = dt.getMouseMode();
         MouseDelegator mDelegator = dt.getMouseDelegator();
         if (!omdtmm.isVisible()) {
            dt.setMask(OMDrawingTool.PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK);
         }

         ret = dt.edit(omg, this, null);

         if (ret != null) {

            if (activeFace != null) {
               activeFace.enableBoundaryButtons(!drawingTool.isActivated());
            }

            // OK, means we're editing - let's lock up the
            // MouseMode
            dt.getCurrentEditable().getStateMachine().setUndefined();

            // Check to see if the DrawingToolMouseMode wants to
            // be invisible. If it does, ask the current
            // active MouseMode to be the proxy for it...
            if (!omdtmm.isVisible() && mDelegator != null) {
               MapMouseMode mmm = mDelegator.getActiveMouseMode();
               if (mmm.actAsProxyFor(omdtmm, MapMouseSupport.PROXY_DISTRIB_MOUSE_MOVED
                     & MapMouseSupport.PROXY_DISTRIB_MOUSE_DRAGGED & MapMouseSupport.PROXY_DISTRIB_MOUSE_CLICKED)) {
                  setProxyMouseMode(mmm);
               } else {
                  // WHOA, couldn't get proxy lock - bail
                  dt.deactivate();
               }
            }
         }
      }

      return ret;
   }

   /**
    * Called by the drawing tool when editing is complete.
    */
   public void drawingComplete(OMGraphic omg, OMAction action) {
      releaseProxyMouseMode();
      if (activeFace != null) {
         activeFace.handleBoundary(omg, action);
         if (activeFace != null) {
            activeFace.enableBoundaryButtons(!drawingTool.isActivated());
         }
      }
   }

   protected MapMouseMode proxyMMM = null;

   /**
    * Set the ProxyMouseMode for the internal drawing tool, if there is one. Can
    * be null. Used to reset the mouse mode when drawing's complete. This is the
    * mouse mode that the drawing tool mouse mode is hiding behind.
    */
   protected synchronized void setProxyMouseMode(MapMouseMode mmm) {
      proxyMMM = mmm;
   }

   /**
    * Get the ProxyMouseMode for the internal drawing tool, if there is one. May
    * be null. Used to reset the mouse mode when drawing's complete. This is the
    * mouse mode that the drawing tool mouse mode is hiding behind.
    */
   protected synchronized MapMouseMode getProxyMouseMode() {
      return proxyMMM;
   }

   /**
    * If the DrawingToolLayer is using a hidden OMDrawingTool, release the proxy
    * lock on the active MapMouseMode.
    */
   public void releaseProxyMouseMode() {
      MapMouseMode pmmm = getProxyMouseMode();
      OMDrawingTool dt = getDrawingTool();
      if (pmmm != null && dt != null) {
         if (pmmm.isProxyFor(dt.getMouseMode())) {

            pmmm.releaseProxy();
            setProxyMouseMode(null);
         }

         if (dt.isActivated()) {
            dt.deactivate();
         }
      }
   }

   String getFileName() {
      return fileName;
   }

   void setFileName(String fileName) {
      this.fileName = fileName;
   }

   OMDrawingTool getDrawingTool() {
      return drawingTool;
   }

   void setDrawingTool(OMDrawingTool drawingTool) {
      this.drawingTool = drawingTool;
   }

   DrawingAttributes getRectDA() {
      return rectDA;
   }

   void setRectDA(DrawingAttributes rectDA) {
      this.rectDA = rectDA;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.bbn.openmap.event.PaintListener#listenerPaint(java.awt.Graphics)
    */
   public void listenerPaint(Graphics graphics) {
      if (activeFace != null) {
         activeFace.paintBoundaries(graphics);
      }
   }

   protected Projection proj;

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.bbn.openmap.event.ProjectionListener#projectionChanged(com.bbn.openmap
    * .event.ProjectionEvent)
    */
   public void projectionChanged(ProjectionEvent e) {
      proj = e.getProjection();

      for (ZoomLevelMakerFace face : faces) {
         face.generate(proj);
      }
   }

   protected class ConfirmationDialog
         extends JDialog {
      private static final long serialVersionUID = 1L;
      Properties launchProps = new Properties();

      /**
       * @param frame
       * @param locationComp
       * @param title
       * @param mapTileMakerComponent
       */
      public ConfirmationDialog(Frame frame, Component locationComp, String title, MapTileMakerComponent mapTileMakerComponent) {
         super(frame, title, true);
         Container contentPane = getContentPane();

         JLabel info = new JLabel();

         StringBuffer content = new StringBuffer();

         int faceCount = 0;
         for (ZoomLevelMakerFace face : faces) {
            if (face.isInclude()) {
               faceCount++;
            }
         }

         if (faceCount == 0) {
            String no_zoom_levels_were_included =
                  i18n.get(MapTileMakerComponent.class, "no_zoom_levels_were_included",
                           "No zoom levels were included for tile creation");
            content.append("<html><body><p>").append(no_zoom_levels_were_included).append(".<p></body></html>");
            // Don't need scroll pane for this message.
            contentPane.add(info);
         } else {
            setPreferredSize(new Dimension(600, 500));

            // For the list, add scroll pane and set size
            JScrollPane scrollPane =
                  new JScrollPane(info, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            contentPane.add(scrollPane, BorderLayout.CENTER);

            content.append("<html><body>");
            String launching_maptilemaker =
                  i18n.get(MapTileMakerComponent.class, "launching_maptilemaker", "Launching MapTileMaker with these settings");
            content.append("<h3>").append(launching_maptilemaker).append(":</h3>");
            StringBuffer zoomLevelList = new StringBuffer();
            StringBuffer totalLayers = null;

            for (ZoomLevelMakerFace face : faces) {
               if (face.isInclude()) {
                  ZoomLevelMaker zli = face.getZoomLevelMaker();

                  zli.setPropertyPrefix("zoom" + zli.getZoomLevel());
                  zoomLevelList.append(" ").append(zli.getPropertyPrefix());
                  String zoom_level = i18n.get(MapTileMakerComponent.class, "zoom_level", "Zoom Level");
                  zli.name = zoom_level + " " + zli.getZoomLevel();
                  
                  int rangeVal = zli.getRange();
                  if (rangeVal < zli.getZoomLevel()) {
                     zli.name += " to " + rangeVal;
                  }
                  
                  String configuration_for = i18n.get(MapTileMakerComponent.class, "configuration_for", "Configuration for");
                  zli.description = configuration_for + " " + zli.name;
                  zli.layers = new ArrayList<String>();
                  zli.bounds = new ArrayList<Rectangle2D>();

                  boolean buildLayerList = false;

                  content.append("<p><hr><b>").append(zoom_level).append(" ").append(zli.name)
                         .append("</b>");
                  String layers_string = i18n.get(MapTileMakerComponent.class, "layers_string", "Layers");
                  content.append("<ul><b>").append(layers_string).append(":</b>");
                  for (LayerObject lo : face.layerList) {

                     if (totalLayers == null) {
                        totalLayers = new StringBuffer();
                        buildLayerList = true;
                     }

                     if (lo.isSelected()) {
                        content.append("<li>").append(lo.layer.getName());
                        zli.layers.add(lo.layer.getPropertyPrefix());
                     }

                     if (buildLayerList) {
                        totalLayers.append(lo.layer.getPropertyPrefix()).append(" ");
                        lo.layer.getProperties(launchProps);
                     }
                  }
                  content.append("</ul>");
                  String coverage_string = i18n.get(MapTileMakerComponent.class, "coverage_string", "Coverage");
                  content.append("<ul><b>").append(coverage_string).append(":</b>");

                  if (face.boundsObjectList == null || face.boundsObjectList.isEmpty()) {
                     int edgeTileCount = face.zfi.getEdgeTileCount();
                     String entire_earth = i18n.get(MapTileMakerComponent.class, "entire_earth", "Entire Earth");
                     content.append("<li>").append(entire_earth).append(" (").append(edgeTileCount).append("x")
                            .append(edgeTileCount).append(" tiles)");
                  } else {
                     for (BoundsObject bo : face.boundsObjectList) {
                        OMRect rect = bo.bounds;
                        content.append("<li>(").append(rect.getNorthLat()).append(", ").append(rect.getWestLon()).append(", ")
                               .append(rect.getSouthLat()).append(", ").append(rect.getEastLon()).append(")");

                        zli.bounds.add(zli.createProperBounds(rect.getWestLon(), rect.getNorthLat(), rect.getEastLon(),
                                                              rect.getSouthLat()));
                     }
                  }
                  content.append("</ul>");

                  zli.getProperties(launchProps);
               }
            }

            launchProps.put(MapTileMaker.ZOOM_LEVELS_PROPERTY, zoomLevelList.toString().trim());
            if (totalLayers != null) {
               launchProps.put(ImageServer.ImageServerLayersProperty, totalLayers.toString().trim());
            }
            if (transparentTiles) {
               launchProps.put(ImageServer.BackgroundProperty, "00000000");
            } else {
               MapHandler mh = (MapHandler) getBeanContext();
               MapBean mb = mh.get(MapBean.class);
               Color background = mb.getBackground();
               String colorString = Integer.toHexString(background.getRGB());
               launchProps.put(ImageServer.BackgroundProperty, colorString);
            }

            // TODO - make all of these options in the GUI
            launchProps.put(ImageServer.AntiAliasingProperty, "true");
            launchProps.put(MapTileMaker.ROOT_DIRECTORY_PROPERTY, rootDir);
            launchProps.put("formatters", "png");
            launchProps.put("png.class", "com.bbn.openmap.image.PNGImageIOFormatter");

            content.append("</body></html>");
         }

         info.setText(content.toString());
         info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

         JPanel buttonPanel = new JPanel();

         String create_map_tiles = i18n.get(MapTileMakerComponent.class, "create_map_tiles", "Create Map Tiles");
         JButton confirm = new JButton(create_map_tiles);
         confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               ConfirmationDialog.this.setVisible(false);
               launchTileMaker(getLaunchProps());
            }
         });

         if (faceCount > 0) {
            buttonPanel.add(confirm);
         }

         String cancel_string = i18n.get(MapTileMakerComponent.class, "cancel_string", "Cancel");
         JButton cancel = new JButton(cancel_string);
         cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               ConfirmationDialog.this.setVisible(false);
            }
         });
         buttonPanel.add(cancel);

         contentPane.add(buttonPanel, BorderLayout.PAGE_END);

         pack();
         setLocationRelativeTo(locationComp);
      }

      Properties getLaunchProps() {
         return launchProps;
      }

   }

   protected class TileWorker
         implements Callable<Boolean> {

      Properties props;

      protected TileWorker(Properties properties) {
         props = properties;

      }

      public Boolean call() {
         MapTileMaker tileMaker = new MapTileMaker(props);
         tileMaker.makeTiles();

         return Boolean.TRUE;
      }
   }

   protected class TileRunnable
         implements Runnable {

      Properties props;
      boolean cancel = false;
      Future<Boolean> work = null;

      protected TileRunnable(Properties properties) {
         props = properties;
      }

      public synchronized void cancel() {
         cancel = true;
      }

      public void run() {

         work = TaskService.singleton().spawn(new TileWorker(props));
         try {
            while (!work.isDone()) {
               if (cancel) {
                  work.cancel(true);
               }
            }
         } catch (CancellationException ce) {
         }

         if (cancelDialog != null) {
            cancelDialog.setVisible(false);
            cancelDialog.dispose();
            cancelDialog = null;
         }
      }
   }
}
