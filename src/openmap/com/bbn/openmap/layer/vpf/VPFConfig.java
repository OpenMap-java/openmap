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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFConfig.java,v $
// $RCSfile: VPFConfig.java,v $
// $Revision: 1.12 $
// $Date: 2006/03/06 16:13:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * A component that can look at the VPF configuration files at the top level of
 * the VPF directory structure, and provide an interface for defining an OpenMap
 * VPFLayer for chosen features.
 * <p>
 * 
 * If the VPFConfig is provided a LayerHandler, it will have a button that will
 * create a layer with selected features. If it doesn't have a LayerHandler, it
 * will provide a button to print out the properties for a VPFLayer for the
 * selected features. This class can be run in stand-alone mode to create
 * properties.
 */
public class VPFConfig
      extends JPanel
      implements ActionListener {

   // private static boolean DEBUG = false;

   private static final long serialVersionUID = 1L;
   // Optionally play with line styles. Possible values are
   // "Angled", "Horizontal", and "None" (the default).
   private boolean playWithLineStyle = false;
   private String lineStyle = "Angled";
   protected boolean showAll = false;
   protected boolean standAlone = false;

   public final static String AddFeatureCmd = "AddFeatureCommand";
   public final static String ClearFeaturesCmd = "ClearFeaturesCommand";
   public final static String CreateLayerCmd = "CreateLayerCommand";
   public final static String EMPTY_FEATURE_LIST = null;

   DefaultMutableTreeNode currentFeature = null;

   protected DrawingAttributes drawingAttributes = new DrawingAttributes();
   protected boolean searchByFeature = true;
   protected String paths = "";

   protected HashSet<String> layerCoverageTypes = new HashSet<String>();
   protected HashSet<String> layerFeatureTypes = new HashSet<String>();

   public final static String AREA = "area";
   public final static String TEXT = "text";
   public final static String EDGE = "edge";
   public final static String POINT = "point";
   public final static String CPOINT = "cpoint";
   public final static String EPOINT = "epoint";
   public final static String COMPLEX = "complex";
   public final static String UNKNOWN = "unknown";

   protected Hashtable<String, HashSet<String>> layerFeatures;
   protected Properties layerProperties;
   protected LayerHandler layerHandler;
   protected LibraryBean libraryBean;
   protected String layerName;
   protected VPFLayer layer;

   JButton addFeatureButton;
   JButton clearFeaturesButton;
   JButton createLayerButton;
   JTextArea currentFeatureList;
   JTextField nameField;

   LinkedList<DefaultMutableTreeNode> featureList = new LinkedList<DefaultMutableTreeNode>();

   public VPFConfig(String[] dataPaths, String layerName) {
      this(dataPaths, null, layerName);
   }

   public VPFConfig(String[] dataPaths, LayerHandler layerHandler, String layerName) {
      this(dataPaths, layerHandler, layerName, false);
   }

   protected VPFConfig(String[] dataPaths, LayerHandler layerHandler, String layerName, boolean standAlone) {

      this.layerHandler = layerHandler;
      this.standAlone = standAlone;
      this.layerName = layerName;

      paths = formatPaths(dataPaths);

      if (paths != null && paths.length() > 0) {
         // Create the nodes.
         DefaultMutableTreeNode top = new DefaultMutableTreeNode("VPF Data Libraries");
         try {
            createNodes(top, dataPaths);
         } catch (FormatException fe) {
            Debug.output("Caught FormatException reading data: " + fe.getMessage());
            if (standAlone) {
               System.exit(0);
            }
         }

         init(top);
      }
   }

   public VPFConfig(LibraryBean lb, LayerHandler layerHandler, String layerName) {
      this.layerHandler = layerHandler;
      this.layerName = layerName;

      // Create the nodes.
      DefaultMutableTreeNode top = new DefaultMutableTreeNode("VPF Data Libraries");
      try {
         createNodes(top, lb.getLibrarySelectionTable());
      } catch (FormatException fe) {
         Debug.output("Caught FormatException reading data: " + fe.getMessage());
      }

      init(top);
   }

   public VPFConfig(VPFLayer layer) {
      if (layer != null && layer.lst != null) {
         this.layer = layer;
         this.layerName = layer.getName();
         // Create the nodes.
         DefaultMutableTreeNode top = new DefaultMutableTreeNode("VPF Data Libraries");
         try {
            createNodes(top, layer.lst);
         } catch (FormatException fe) {
            Debug.output("Caught FormatException reading data: " + fe.getMessage());
         }
         init(top);
      }
   }

   public String formatPaths(String[] dataPaths) {

      if (dataPaths != null && dataPaths.length > 0) {

         // Take the time to replace any \ with /, it matters if
         // the properties get printed out for later.
         // Permanently set them from \ to / for when they get
         // passed to BinaryFile.
         dataPaths[0] = dataPaths[0].replace('\\', '/');
         StringBuffer buf = new StringBuffer(dataPaths[0]);

         for (int i = 1; i < dataPaths.length; i++) {
            buf.append(";");
            // Permanently set them from \ to / for when they get
            // passed to BinaryFile.
            dataPaths[i] = dataPaths[i].replace('\\', '/');
            buf.append(dataPaths[i]);
         }
         return buf.toString();
      }
      return null;
   }

   public void init(DefaultMutableTreeNode top) {

      layerFeatures = new Hashtable<String, HashSet<String>>();

      // Create a tree that allows one selection at a time.
      final JTree tree = new JTree(top);
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.setVisibleRowCount(10);

      // Listen for when the selection changes.
      tree.addTreeSelectionListener(new TreeSelectionListener() {
         public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (node == null)
               return;

            Object nodeInfo = node.getUserObject();
            if (node.isLeaf() && nodeInfo instanceof FeatureInfo) {
               currentFeature = node;
               // enable addToLayer button here.
               addFeatureButton.setEnabled(true);
            } else {
               // disable addToLayer button here.
               addFeatureButton.setEnabled(false);
            }
         }
      });

      if (playWithLineStyle) {
         tree.putClientProperty("JTree.lineStyle", lineStyle);
      }

      // Create the scroll pane and add the tree to it.
      GridBagLayout outergridbag = new GridBagLayout();
      GridBagConstraints outerc = new GridBagConstraints();

      JScrollPane treeView = new JScrollPane(tree);

      setLayout(outergridbag);

      outerc.fill = GridBagConstraints.BOTH;
      outerc.anchor = GridBagConstraints.WEST;
      outerc.insets = new Insets(10, 10, 10, 10);
      outerc.gridx = GridBagConstraints.REMAINDER;
      outerc.weighty = .75;
      outerc.weightx = 1.0;
      outergridbag.setConstraints(treeView, outerc);
      add(treeView);

      // Create the configuration pane
      JPanel configPanel = new JPanel();
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      configPanel.setLayout(gridbag);

      c.gridheight = GridBagConstraints.REMAINDER;
      Component da = drawingAttributes.getGUI();
      gridbag.setConstraints(da, c);
      configPanel.add(da);

      c.gridx = 1;
      c.gridheight = 1;
      c.gridy = 0;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(0, 5, 0, 5);
      addFeatureButton = new JButton("Add Feature");
      addFeatureButton.addActionListener(this);
      addFeatureButton.setActionCommand(AddFeatureCmd);
      gridbag.setConstraints(addFeatureButton, c);
      configPanel.add(addFeatureButton);
      addFeatureButton.setEnabled(false);

      clearFeaturesButton = new JButton("Clear Features");
      clearFeaturesButton.addActionListener(this);
      clearFeaturesButton.setActionCommand(ClearFeaturesCmd);
      c.gridy = GridBagConstraints.RELATIVE;
      gridbag.setConstraints(clearFeaturesButton, c);
      configPanel.add(clearFeaturesButton);
      clearFeaturesButton.setEnabled(false);

      if (layer != null) {
         createLayerButton = new JButton("Set Features on Layer");
      } else if (layerHandler != null) {
         createLayerButton = new JButton("Create Layer");
      } else {
         createLayerButton = new JButton("Print Properties");
      }
      createLayerButton.addActionListener(this);
      createLayerButton.setActionCommand(CreateLayerCmd);
      gridbag.setConstraints(createLayerButton, c);
      configPanel.add(createLayerButton);
      createLayerButton.setEnabled(false);

      JPanel currentFeatureListPanel = PaletteHelper.createVerticalPanel(" Current Features: ");
      currentFeatureList = new JTextArea(EMPTY_FEATURE_LIST);
      currentFeatureList.setEditable(false);
      JScrollPane featureListScrollPane = new JScrollPane(currentFeatureList);
      featureListScrollPane.setPreferredSize(new Dimension(150, 10));
      currentFeatureListPanel.add(featureListScrollPane);

      c.gridx = 2;
      c.gridy = 0;
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridheight = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(currentFeatureListPanel, c);
      configPanel.add(currentFeatureListPanel);

      GridBagLayout gridbag2 = new GridBagLayout();
      GridBagConstraints c2 = new GridBagConstraints();
      JPanel namePanel = new JPanel();
      namePanel.setLayout(gridbag2);

      c2.weightx = 0;
      c2.weighty = 0;
      c2.anchor = GridBagConstraints.WEST;
      JLabel nameLabel = new JLabel("Layer Name: ");
      gridbag2.setConstraints(nameLabel, c2);
      namePanel.add(nameLabel);

      c2.fill = GridBagConstraints.HORIZONTAL;
      c2.weightx = 1.0;
      c2.weighty = 1.0;
      nameField = new JTextField(layerName);
      gridbag2.setConstraints(nameField, c2);
      namePanel.add(nameField);

      outerc.anchor = GridBagConstraints.WEST;
      outerc.weighty = 0;
      outergridbag.setConstraints(namePanel, outerc);
      add(namePanel);

      outerc.fill = GridBagConstraints.HORIZONTAL;
      outerc.weighty = .25;
      outerc.anchor = GridBagConstraints.CENTER;
      outergridbag.setConstraints(configPanel, outerc);
      add(configPanel);

      DrawingAttributes oldDrawingAttributes = (DrawingAttributes) drawingAttributes.clone();

      if (layer != null) {
         // initialize currentFeatures list with anything the layer might
         // have
         // already.
         LayerGraphicWarehouseSupport warehouse = layer.getWarehouse();
         List<String> initialFeatureList = warehouse.getFeatures();

         for (String initialFeature : initialFeatureList) {
            // For each feature, find the corresponding tree node
            // and perform the AddFeatureCmd command
            loadCurrentFeatures(top, initialFeature, warehouse);
         }
      }

      oldDrawingAttributes.setTo(drawingAttributes);
   }

   @SuppressWarnings("unchecked")
   protected void loadCurrentFeatures(DefaultMutableTreeNode top, String featureName, LayerGraphicWarehouseSupport warehouse) {
      Enumeration<DefaultMutableTreeNode> treeEnum = top.children();
      while (treeEnum.hasMoreElements()) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeEnum.nextElement();
         Object obj = node.getUserObject();
         // Debug.output(node.getUserObject().getClass().getName()
         // + ", " + node.getUserObject());
         if (obj instanceof FeatureInfo) {
            FeatureInfo fi = (FeatureInfo) obj;
            if (fi.featureName.equals(featureName)) {
               currentFeature = node;

               if (warehouse instanceof VPFFeatureGraphicWarehouse) {
                  FeatureDrawingAttributes fda = ((VPFFeatureGraphicWarehouse) warehouse).getAttributesForFeature(featureName);
                  if (fda != null) {
                     fda.setTo(drawingAttributes);
                  }
               }

               actionPerformed(new ActionEvent(this, 0, AddFeatureCmd));
            }
         } else {
            loadCurrentFeatures(node, featureName, warehouse);
         }
      }
   }

   public void actionPerformed(ActionEvent ae) {
      String command = ae.getActionCommand();

      if (command == AddFeatureCmd) {

         if (currentFeature != null) {
            FeatureInfo feature = (FeatureInfo) currentFeature.getUserObject();
            // Save the current DrawingAttributes
            // settings for the feature.
            feature.drawingAttributes = (DrawingAttributes) drawingAttributes.clone();
            featureList.add(currentFeature);

            String cfl = currentFeatureList.getText();
            if (featureList.size() == 1) {
               cfl = feature.toString();
            } else {
               cfl += "\n" + feature.toString();
            }

            currentFeatureList.setText(cfl);

            currentFeature = null;
            createLayerButton.setEnabled(true);
            addFeatureButton.setEnabled(false);
            clearFeaturesButton.setEnabled(true);
         } else {
            Debug.error("No feature selected");
         }
      } else if (command == ClearFeaturesCmd) {
         featureList.clear();
         layerFeatures.clear();

         createLayerButton.setEnabled(false);
         addFeatureButton.setEnabled(false);
         clearFeaturesButton.setEnabled(false);
         currentFeatureList.setText(EMPTY_FEATURE_LIST);
      } else if (command == CreateLayerCmd) {
         if (featureList.isEmpty()) {
            Debug.error("No features selected for new VPFLayer");
            createLayerButton.setEnabled(false);
            clearFeaturesButton.setEnabled(false);
            return;
         }

         String name = nameField.getText();
         if (name == null) {
            name = "VPFLayer";
         }

         String propertyPrefix;
         if (layer != null) {
            propertyPrefix = PropUtils.getScopedPropertyPrefix(layer);
         } else {
            propertyPrefix = PropUtils.getScopedPropertyPrefix(name.replace(' ', '_').toLowerCase());
         }

         if (layer != null) {
            layerProperties = layer.getProperties(null);
         } else {
            layerProperties = new Properties();
         }

         layerCoverageTypes.clear();
         layerFeatureTypes.clear();
         layerFeatures.clear();

         if (standAlone) {
            layerProperties.put(propertyPrefix + "class", "com.bbn.openmap.layer.vpf.VPFLayer");
         }

         layerProperties.put(propertyPrefix + Layer.PrettyNameProperty, name);
         layerProperties.put(propertyPrefix + VPFLayer.pathProperty, paths);
         layerProperties.put(propertyPrefix + VPFLayer.searchByFeatureProperty, new Boolean(searchByFeature).toString());

         // Now, build up coverageTypeProperty and
         // featureTypesProperty
         // from the linked list of featureNodes...
         for (DefaultMutableTreeNode node : featureList) {
            addPropertiesForFeature(node, propertyPrefix, layerProperties);
         }

         // coverageTypeProperty and featureTypesProperty should
         // be built from above iteration, should push them into
         // properties...
         // List the coverages
         layerProperties.put(propertyPrefix + VPFLayer.coverageTypeProperty, stringTogether(layerCoverageTypes.iterator()));
         // List area/edge/point/text, whatever has been set up
         // with the chosen features.
         layerProperties.put(propertyPrefix + VPFLayer.featureTypesProperty, stringTogether(layerFeatureTypes.iterator()));

         // OK, now go through the layerFeature lists for
         // area/edge/text/point and add the property listing the
         // features associated with each type.
         for (String key : layerFeatures.keySet()) {
            HashSet<String> featureSet = layerFeatures.get(key);
            layerProperties.put(propertyPrefix + key, stringTogether(featureSet.iterator()));
         }

         if (layer != null) {
            layer.setConfigSettings(layer.getPropertyPrefix(), layerProperties);
         } else if (layerHandler != null) {
            VPFLayer layer = new VPFLayer();
            layer.setProperties(propertyPrefix, layerProperties);
            layerHandler.addLayer(layer);
         } else {
            printProperties(layerProperties);
         }

         // featureList.clear();
         //
         // currentFeatureList.setText(EMPTY_FEATURE_LIST);
         // createLayerButton.setEnabled(false);
         // addFeatureButton.setEnabled(false);
         // clearFeaturesButton.setEnabled(false);
      }
   }

   private void addPropertiesForFeature(DefaultMutableTreeNode featureNode, String propertyPrefix, Properties layerProperties) {
      FeatureInfo feature = (FeatureInfo) featureNode.getUserObject();
      CoverageInfo coverage = (CoverageInfo) ((DefaultMutableTreeNode) featureNode.getParent()).getUserObject();

      // Adding to coverage list
      layerCoverageTypes.add(coverage.coverageName);
      // Adding area, edge, text, point to list if it doesn't exist.
      layerFeatureTypes.add(feature.featureTypeString);

      // adding feature name to appropriate edge/area/text/point
      // list
      HashSet<String> featureSet = layerFeatures.get(feature.featureTypeString);

      if (featureSet == null) {
         // If it's the first category type for the feature
         featureSet = new HashSet<String>();
         layerFeatures.put(feature.featureTypeString, featureSet);
      }
      // Add feature to feature type list for edge/area/text/point
      featureSet.add(feature.featureName);
      feature.drawingAttributes.setPropertyPrefix(propertyPrefix + feature.featureName);
      feature.drawingAttributes.getProperties(layerProperties);
   }

   private void printProperties(Properties props) {
      Enumeration<?> keys = props.propertyNames();
      System.out.println("######## START Properties ########");
      while (keys.hasMoreElements()) {
         String key = (String) keys.nextElement();
         System.out.println(key + "=" + props.getProperty(key));
      }
      System.out.println("######## END Properties ########");
   }

   private String stringTogether(Iterator<String> it) {
      StringBuffer buf = null;

      while (it.hasNext()) {
         String val = it.next();

         if (buf == null) {
            buf = new StringBuffer(val);
         } else {
            buf.append(" ").append(val);
         }
      }

      if (buf == null) {
         return "";
      } else {
         return buf.toString();
      }
   }

   protected static class FeatureInfo {
      public String featureName;
      public String featureDescription;
      public String featureTypeString;
      public int featureType;
      public CoverageTable.FeatureClassRec record;
      public DrawingAttributes drawingAttributes;

      public FeatureInfo(CoverageTable ct, CoverageTable.FeatureClassRec fcr) {
         record = fcr;

         featureTypeString = UNKNOWN;
         if (fcr.type == CoverageTable.TEXT_FEATURETYPE) {
            featureTypeString = TEXT;
         } else if (fcr.type == CoverageTable.EDGE_FEATURETYPE) {
            featureTypeString = EDGE;
         } else if (fcr.type == CoverageTable.AREA_FEATURETYPE) {
            featureTypeString = AREA;
         } else if (fcr.type == CoverageTable.UPOINT_FEATURETYPE) {
            FeatureClassInfo fci = ct.getFeatureClassInfo(fcr.feature_class);
            if (fci == null) {
               featureTypeString = POINT;
            } else if (fci.getFeatureType() == CoverageTable.EPOINT_FEATURETYPE) {
               featureTypeString = EPOINT;
            } else if (fci.getFeatureType() == CoverageTable.CPOINT_FEATURETYPE) {
               featureTypeString = CPOINT;
            } else {
               featureTypeString = POINT;
            }
         } else if (fcr.type == CoverageTable.COMPLEX_FEATURETYPE) {
            featureTypeString = COMPLEX;
         }

         featureType = fcr.type;
         featureName = fcr.feature_class;
         featureDescription = fcr.description;
      }

      public String toString() {
         return featureDescription + " (" + featureTypeString + ")";
      }
   }

   protected static class CoverageInfo {
      public String coverageName;
      public String coverageDescription;

      public CoverageInfo(CoverageAttributeTable cat, String covName) {
         coverageName = covName;
         coverageDescription = cat.getCoverageDescription(covName);
      }

      public String toString() {
         return coverageDescription;
      }
   }

   private boolean addFeatureNodes(DefaultMutableTreeNode coverageNode, CoverageTable ct) {
      int numFeatures = 0;
      Hashtable<String, CoverageTable.FeatureClassRec> info = ct.getFeatureTypeInfo();
      for (CoverageTable.FeatureClassRec fcr : info.values()) {

         if (fcr.type == CoverageTable.SKIP_FEATURETYPE) {
            continue;
         }

         coverageNode.add(new DefaultMutableTreeNode(new FeatureInfo(ct, fcr)));
         numFeatures++;
      }
      return numFeatures > 0;
   }

   private void addCoverageNodes(DefaultMutableTreeNode libraryNode, CoverageAttributeTable cat) {
      String[] coverages = cat.getCoverageNames();
      for (int covi = 0; covi < coverages.length; covi++) {
         String coverage = coverages[covi];
         CoverageInfo covInfo = new CoverageInfo(cat, coverage);
         DefaultMutableTreeNode covNode = new DefaultMutableTreeNode(covInfo);
         if (showAll || addFeatureNodes(covNode, cat.getCoverageTable(coverage)) || !cat.isTiledData()) {
            libraryNode.add(covNode);
         }
      }
   }

   private void createNodes(DefaultMutableTreeNode top, LibrarySelectionTable lst)
         throws FormatException {

      DefaultMutableTreeNode category = null;

      for (String library : lst.getLibraryNames()) {
         category = new DefaultMutableTreeNode(library);
         CoverageAttributeTable cat = lst.getCAT(library);
         top.add(category);
         addCoverageNodes(category, cat);
      }
   }

   private void createNodes(DefaultMutableTreeNode top, String[] dataPaths)
         throws FormatException {

      for (int i = 0; i < dataPaths.length; i++) {
         String rootpath = dataPaths[i];
         LibrarySelectionTable lst = new LibrarySelectionTable(rootpath);
         createNodes(top, lst);
      }
   }

   public static void printFeatures(String[] dataPaths) {
      TreeMap<String, FeatureInfo> features = new TreeMap<String, FeatureInfo>();

      for (int i = 0; i < dataPaths.length; i++) {
         String rootpath = dataPaths[i];
         try {
            LibrarySelectionTable lst = new LibrarySelectionTable(rootpath);

            for (String libraryName : lst.getLibraryNames()) {

               CoverageAttributeTable cat = lst.getCAT(libraryName);

               String[] coverages = cat.getCoverageNames();
               for (int covi = 0; covi < coverages.length; covi++) {
                  String coverage = coverages[covi];

                  CoverageTable ct = cat.getCoverageTable(coverage);
                  Hashtable<String, CoverageTable.FeatureClassRec> info = ct.getFeatureTypeInfo();
                  for (CoverageTable.FeatureClassRec fcr : info.values()) {

                     if (fcr.type == CoverageTable.SKIP_FEATURETYPE) {
                        continue;
                     }

                     FeatureInfo fi = new FeatureInfo(ct, fcr);
                     if (!features.containsKey(fi.featureName)) {
                        features.put(fi.featureName, fi);
                     }
                  }
               }
            }

         } catch (FormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      if (!features.isEmpty()) {
         for (Entry<String, FeatureInfo> entry : features.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
         }
      }

   }

   public static void createLayer(String[] vpfPaths, LayerHandler layerHandler, String layerName) {
      launchFrame(new VPFConfig(vpfPaths, layerHandler, layerName), false);
   }

   public static void createLayer(LibraryBean libraryBean, LayerHandler layerHandler, String layerName) {
      launchFrame(new VPFConfig(libraryBean, layerHandler, layerName), false);
   }

   protected static void launchFrame(JComponent content, boolean exitOnClose) {
      JFrame frame = new JFrame("Create VPF Data Layer");

      frame.getContentPane().add(content);
      if (exitOnClose) {
         frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               System.exit(0);
            }
         });
      }

      frame.pack();
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      ArgParser argParser = new ArgParser("VPFConfig");
      argParser.add("features", "print available feature codes and descriptions, given paths to VPF directories", ArgParser.TO_END);
      argParser.add("path", "space-separated paths to VPF directories", ArgParser.TO_END);

      if (!argParser.parse(args)) {
         argParser.printUsage();
         System.exit(0);
      }

      String arg[];
      arg = argParser.getArgValues("features");
      if (arg != null) {
         VPFConfig.printFeatures(arg);
         System.exit(0);
      }

      arg = argParser.getArgValues("path");
      if (arg != null) {
         VPFConfig vpfc = new VPFConfig(arg, null, "VPF Layer", true);
         launchFrame(vpfc, true);
      } else {
         argParser.printUsage();
      }
   }
}