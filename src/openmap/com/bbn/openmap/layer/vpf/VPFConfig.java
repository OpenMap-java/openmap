package com.bbn.openmap.layer.vpf;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.util.Debug;

public class VPFConfig extends JPanel implements ActionListener {

    private static boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled", "Horizontal", and "None" (the default).
    private boolean playWithLineStyle = false;
    private String lineStyle = "Angled"; 
    protected boolean showAll = false;

    public final static String AddFeatureCmd = "AddFeatureCommand";
    public final static String CreateLayerCmd = "CreateLayerCommand";

    DefaultMutableTreeNode currentFeature = null;

    protected DrawingAttributes drawingAttributes = new DrawingAttributes();
    protected boolean searchByFeature = true;
    protected String paths = "";

    protected HashSet layerCoverageTypes = new HashSet();
    protected HashSet layerFeatureTypes = new HashSet();

    public final static String AREA = "area";
    public final static String TEXT = "text";
    public final static String EDGE = "edge";
    public final static String POINT = "point";
    public final static String CPOINT = "cpoint";
    public final static String EPOINT = "epoint";
    public final static String COMPLEX = "complex";
    public final static String UNKNOWN = "unknown";

    protected Hashtable layerFeatures;
    protected Properties layerProperties;
    protected LayerHandler layerHandler;
    protected LibraryBean libraryBean;

    public VPFConfig(String[] dataPaths) {
	this(dataPaths, null);
    }

    public VPFConfig(String[] dataPaths, LayerHandler layerHandler) {

	this.layerHandler = layerHandler;

	if (dataPaths != null && dataPaths.length > 0) {
	    StringBuffer buf = new StringBuffer(dataPaths[0]);
	    for (int i = 1; i < dataPaths.length; i++) {
		buf.append(";");
		buf.append(dataPaths[i]);
	    }
	    paths = buf.toString();
	}

        //Create the nodes.
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("VPF Data Libraries");
	try {
	    createNodes(top, dataPaths);
	} catch (FormatException fe) {
	    Debug.output("Caught FormatException reading data: " + fe.getMessage());
	}

	init(top);
    }

    public VPFConfig(LibraryBean lb, LayerHandler layerHandler) {
	this.layerHandler = layerHandler;

        //Create the nodes.
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("VPF Data Libraries");
	try {
	    createNodes(top, lb.getLibrarySelectionTable());
	} catch (FormatException fe) {
	    Debug.output("Caught FormatException reading data: " + fe.getMessage());
	}

	init(top);
    }

    public void init(DefaultMutableTreeNode top) {

	layerFeatures = new Hashtable();

        //Create a tree that allows one selection at a time.
        final JTree tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = 
		    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if (node == null) return;

                Object nodeInfo = node.getUserObject();
                if (node.isLeaf() && nodeInfo instanceof FeatureInfo) {
		    FeatureInfo feature = (FeatureInfo)nodeInfo;
		    currentFeature = node;
		    // enable addToLayer button here.
                } else {
		    // disable addToLayer button here.
		}
            }
        });

        if (playWithLineStyle) {
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
        JScrollPane treeView = new JScrollPane(tree);

	// Create the configuration pane
	JPanel configPanel = new JPanel();
	configPanel.add(drawingAttributes.getGUI());

	JButton addFeatureButton = new JButton("Add Feature");
	addFeatureButton.addActionListener(this);
	addFeatureButton.setActionCommand(AddFeatureCmd);
	configPanel.add(addFeatureButton);

	JButton createLayerButton = new JButton("Create Layer");
	createLayerButton.addActionListener(this);
	createLayerButton.setActionCommand(CreateLayerCmd);
	configPanel.add(createLayerButton);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(configPanel);

//         Dimension minimumSize = new Dimension(100, 50);
//         configPanel.setMinimumSize(minimumSize);
//         treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); //XXX: ignored in some releases
                                           //of Swing. bug 4101306
        //workaround for bug 4101306:
        //treeView.setPreferredSize(new Dimension(100, 100)); 

//         splitPane.setPreferredSize(new Dimension(500, 300));

        //Add the split pane to this frame.
        add(splitPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent ae) {
	String command = ae.getActionCommand();

	if (command == AddFeatureCmd) {

	    if (currentFeature != null) {
                FeatureInfo feature = (FeatureInfo)currentFeature.getUserObject();
		CoverageInfo coverage = (CoverageInfo)((DefaultMutableTreeNode)currentFeature.getParent()).getUserObject();

		if (layerProperties == null) {
		    layerProperties = new Properties();
		}
		
		DrawingAttributes covDA = (DrawingAttributes)drawingAttributes.clone();
		covDA.setPropertyPrefix(feature.featureName);
		covDA.getProperties(layerProperties);

		layerCoverageTypes.add(coverage.coverageName);
		layerFeatureTypes.add(feature.featureTypeString);

		HashSet featureSet = ((HashSet)layerFeatures.get(feature.featureTypeString));

		if (featureSet == null) {
		    // If it's the first feature type
		    featureSet = new HashSet();
		    layerFeatures.put(feature.featureTypeString, featureSet);
		}
		// Add feature to feature type list
		featureSet.add(feature.featureName);

	    } else {
		Debug.error("No feature selected");
	    }
	} else if (command == CreateLayerCmd) {
	    if (layerProperties == null) {
		Debug.error("No features selected for new VPFLayer");
		return;
	    }

	    layerProperties.put(VPFLayer.pathProperty, paths);
	    layerProperties.put(VPFLayer.searchByFeatureProperty, new Boolean(searchByFeature).toString());
	    layerProperties.put(VPFLayer.coverageTypeProperty, 
				stringTogether(layerCoverageTypes.iterator()));
	    layerProperties.put(VPFLayer.featureTypesProperty, 
				stringTogether(layerFeatureTypes.iterator()));

	    Enumeration keys = layerFeatures.keys();

	    while (keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		HashSet featureSet = (HashSet)layerFeatures.get(key);
		layerProperties.put(key, stringTogether(featureSet.iterator()));
	    }

	    if (layerHandler != null) {
		VPFLayer layer = new VPFLayer();
		layer.setProperties(layerProperties);
		layerHandler.addLayer(layer);
	    } else {
		printProperties(layerProperties);
	    }
	}
    }

    private void printProperties(Properties props) {
	Enumeration keys = props.propertyNames();
	while (keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();
	    System.out.println(key + "=" + props.getProperty(key));
	}
    }

    private String stringTogether(Iterator it) {
	StringBuffer buf = null;

	while (it.hasNext()) {
	    String val = (String) it.next();
	    
	    if (buf == null) {
		buf = new StringBuffer(val);
	    } else {
		buf.append(" " + val);
	    }
	}

	if (buf == null) {
	    return "";
	} else {
	    return buf.toString();
	}
    }

    private class FeatureInfo {
        public String featureName;
	public String featureDescription;
	public String featureTypeString;
	public int featureType;
	public CoverageTable.FeatureClassRec record;

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

    private class CoverageInfo {
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
	Hashtable info = ct.getFeatureTypeInfo();
	for (Enumeration enum = info.elements(); enum.hasMoreElements();) {
	    CoverageTable.FeatureClassRec fcr = (CoverageTable.FeatureClassRec)enum.nextElement();

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
	    if (showAll || 
		addFeatureNodes(covNode, cat.getCoverageTable(coverage)) || 
		!cat.isTiledData()) {
		libraryNode.add(covNode);
	    }
	}
    }

    private void createNodes(DefaultMutableTreeNode top, LibrarySelectionTable lst) 
	throws FormatException {

        DefaultMutableTreeNode category = null;

	String[] libraries = lst.getLibraryNames();
	for (int libi = 0; libi < libraries.length; libi++) {
	    String library = libraries[libi];
	    category = new DefaultMutableTreeNode(library);
	    CoverageAttributeTable cat = lst.getCAT(library);
	    top.add(category);
	    addCoverageNodes(category, cat);
	}
    }

    private void createNodes(DefaultMutableTreeNode top, String[] dataPaths) 
	throws FormatException {

        DefaultMutableTreeNode category = null;

	for (int i = 0; i < dataPaths.length; i++) {
	    String rootpath = dataPaths[i];
	    LibrarySelectionTable lst = new LibrarySelectionTable(rootpath);
	    createNodes(top, lst);
	}
    }

    public static void createLayer(String[] vpfPaths, LayerHandler layerHandler) {
	launchFrame(new VPFConfig(vpfPaths, layerHandler), false);
    }

    public static void createLayer(LibraryBean libraryBean, LayerHandler layerHandler) {
	launchFrame(new VPFConfig(libraryBean, layerHandler), false);
    }

    protected static void launchFrame(JComponent content, boolean exitOnClose) {
        JFrame frame = new JFrame("VPF Data");

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
	VPFConfig vpfc = new VPFConfig(args);
	launchFrame(vpfc, true);
    }
}
