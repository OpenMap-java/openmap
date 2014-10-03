/* 
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.imageTile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.ListResetPCPolicy;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMWarpingImage;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.proj.coords.MercatorUVGCT;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor;

/**
 * A layer to assist with creating map tiles, it creates an overlay that
 * represents tile coverage for different zoom levels. You could just view the
 * tiles, but then you are limited to looking at that coverage at the scale for
 * that zoom level. This tool lets you figure out what tiles you have and what
 * you may need to create.
 * 
 * @author dietrick
 */
public class MapTileUtilLayer
      extends OMGraphicHandlerLayer
      implements ListSelectionListener {

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.layer.imageTile.MapTileUtilLayer");
   protected String tileRootDir;
   protected HashMap<String, BufferedImage> images;
   protected OMGraphicList omgraphics;

   public MapTileUtilLayer() {
      images = new HashMap<String, BufferedImage>();
      omgraphics = new OMGraphicList();
      coordTransform = LatLonGCT.INSTANCE;
      setProjectionChangePolicy(new ListResetPCPolicy(this));
   }

   public String getTileRootDir() {
      return tileRootDir;
   }

   public void setTileRootDir(String tileRootDir) {
      this.tileRootDir = tileRootDir;

      if (generateButton != null) {
         generateButton.setEnabled(tileRootDir != null && tileRootDir.length() != 0);
      }
   }

   protected OMGraphicList getOMGraphics() {
      return omgraphics;
   }

   protected void setOMGraphics(OMGraphicList omgl) {
      omgraphics = omgl;
   }

   public synchronized OMGraphicList prepare() {
      OMGraphicList ret = new OMGraphicList();

      OMGraphicList currentList = getOMGraphics();
      if (currentList != null) {
         ret.addAll(currentList);
      }

      Projection proj = getProjection();
      if (proj != null) {
         ret.generate(proj);
      }

      return ret;
   }

   protected BufferedImage getImageForZoomLevel(int zoomLevel) {
      int dimensionForZoom = (int) Math.pow(2, zoomLevel);
      return new BufferedImage(dimensionForZoom, dimensionForZoom, BufferedImage.TYPE_INT_ARGB);
   }

   protected OMWarpingImage getOMWarpingImage(BufferedImage bufImage, int zoomLevel) {
      GeoCoordTransformation gct = new MercatorUVGCT.TMS(zoomLevel);
      return new OMWarpingImage(bufImage, gct, new DataBounds(new Point2D.Double(0, 0), new Point2D.Double(bufImage.getWidth(),
                                                                                                           bufImage.getHeight())));
   }

   protected void colorImage(File zoomLevelDir, BufferedImage bufImage, int rgb) {
      int width = bufImage.getWidth();
      int height = bufImage.getHeight();

      for (int x = 0; x < width; x++) {
         File rowDir = new File(zoomLevelDir, Integer.toString(x));
         if (rowDir.exists()) {
            for (int y = 0; y < height; y++) {
               File colFile = new File(rowDir, Integer.toString(y) + ".png");
               if (colFile.exists()) {
                  bufImage.setRGB(x, y, rgb);
               }
            }
         }
      }
   }

   protected void generateMapTileImages() {

      if (imageList == null) {
         logger.info("imageList is null, something weird going on");
         return;
      }

      if (images != null) {
         images.clear();
      }

      if (tileRootDir != null) {
         File rootFile = new File(tileRootDir);
         if (rootFile.exists()) {
            for (int zoomLevel = 0; zoomLevel < 21; zoomLevel++) {
               BufferedImage buf = getImageForZoomLevel(zoomLevel);

               File levelDir = new File(rootFile, Integer.toString(zoomLevel));
               if (levelDir.exists()) {
                  colorImage(levelDir, buf, 0x77aaaa00);
                  images.put(levelDir.getName(), buf);
                  imageList.setListData(images.keySet().toArray());
               }
            }
         }
      }
   }

   protected void clearMapTileImages() {
       if (images != null) {
           images.clear();
       }
       
       if (imageList != null) {
           imageList.removeAll();
       }
   }
   
   
   JPanel gui = null;
   DirectoryPropertyEditor dirEditor = null;
   JButton generateButton = null;
   JButton clearButton = null;
   JList imageList = null;

   public JComponent getGUI() {
      if (gui == null) {

         gui = new JPanel(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();

         c.gridx = 0;
         c.gridy = 0;
         c.gridwidth = 1;
         c.gridheight = 1;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(10, 10, 0, 10);

         JLabel label1 = new JLabel("Tile Root Directory:");
         gui.add(label1, c);

         dirEditor = new DirectoryPropertyEditor();
         dirEditor.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
               DirectoryPropertyEditor de = (DirectoryPropertyEditor) pce.getSource();
               setTileRootDir(de.getAsText());
            }
         });
         c.gridx = 1;
         c.gridwidth = 2;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 1.0f;
         gui.add(dirEditor.getCustomEditor(), c);

         imageList = new JList();
         imageList.addListSelectionListener(this);
         imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

         JScrollPane scrollPane =
               new JScrollPane(imageList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         c.gridx = 0;
         c.gridy = 1;
         c.gridwidth = 3;
         c.fill = GridBagConstraints.BOTH;
         c.weighty = 1.0f;
         c.insets = new Insets(10, 10, 10, 10);

         gui.add(scrollPane, c);

         c.fill = GridBagConstraints.NONE;
         c.weightx = 0.0f;
         c.weighty = 0.0f;
         c.gridwidth = 1;
         c.gridy = 2;
         c.insets = new Insets(0, 10, 10, 10);

         clearButton = new JButton("Clear");
         clearButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 clearMapTileImages();
             }
         });
         generateButton = new JButton("Generate");
         generateButton.setEnabled(tileRootDir != null && tileRootDir.length() != 0);
         generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               generateMapTileImages();
            }
         });

         c.gridx = 0;
         gui.add(clearButton, c);

         c.gridx = 2;
         c.anchor = GridBagConstraints.NORTHEAST;
         gui.add(generateButton, c);
      }

      return gui;
   }

   public static void main(String[] args) {
      logger.info("for zoom level 17: " + Math.pow(2, 17));

      JFrame frame = new JFrame("GUI");
      MapTileUtilLayer layer = new MapTileUtilLayer();
      frame.setContentPane(layer.getGUI());

      frame.pack();
      frame.setVisible(true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
    * .ListSelectionEvent)
    */
   public void valueChanged(ListSelectionEvent arg0) {
      if (arg0.getValueIsAdjusting()) {
         int firstIndex = arg0.getFirstIndex();

         JList list = (JList) arg0.getSource();
         String zoomLevel = list.getSelectedValue().toString();

         if (images != null) {
            BufferedImage buf = images.get(zoomLevel);
            OMGraphicList omgl = new OMGraphicList();
            OMWarpingImage omwiImage = getOMWarpingImage(buf, Integer.parseInt(zoomLevel));
            omgl.add(omwiImage);
            setOMGraphics(omgl);
            doPrepare();
         }
         
         clearButton.setEnabled(((JList)arg0.getSource()).getComponentCount() > 0);
      }

   }

}
