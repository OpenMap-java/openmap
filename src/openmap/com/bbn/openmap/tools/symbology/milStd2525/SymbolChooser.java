// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolChooser.java,v $
// $RCSfile: SymbolChooser.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/17 00:23:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 */
public class SymbolChooser extends JPanel implements ActionListener {

    private static boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled", "Horizontal", and "None" (the default).
    private boolean playWithLineStyle = false;
    private String lineStyle = "Angled"; 
    protected boolean showAll = false;

    public final static String AddFeatureCmd = "AddFeatureCommand";
    public final static String ClearFeaturesCmd = "ClearFeaturesCommand";
    public final static String CreateLayerCmd = "CreateLayerCommand";
    public final static String EMPTY_FEATURE_LIST = null;

    DefaultMutableTreeNode currentSymbol = null;

    protected DrawingAttributes drawingAttributes = new DrawingAttributes();

    public final static String AREA = "area";
    public final static String TEXT = "text";
    public final static String EDGE = "edge";
    public final static String POINT = "point";
    public final static String CPOINT = "cpoint";
    public final static String EPOINT = "epoint";
    public final static String COMPLEX = "complex";
    public final static String UNKNOWN = "unknown";

    JButton addFeatureButton;
    JButton clearFeaturesButton;
    JButton createLayerButton;
    JTextField nameField;

    public SymbolChooser(SymbolReferenceLibrary srl) {

        //Create the nodes.
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(srl.getHead().getPrettyName());
	try {
	    createNodes(top, srl);
	} catch (FormatException fe) {
	    Debug.output("Caught FormatException reading data: " + fe.getMessage());
	}

	init(top);
    }

    public void init(DefaultMutableTreeNode top) {

        //Create a tree that allows one selection at a time.
        final JTree tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setVisibleRowCount(10);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = 
		    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if (node == null) return;

                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof SymbolPart) {
		    SymbolPart symbolPart = (SymbolPart)nodeInfo;
		    currentSymbol = node;
		    nameField.setText(symbolPart.getSymbolCode());
		}
            }
        });

        if (playWithLineStyle) {
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it. 
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

	
	createLayerButton = new JButton("Print Properties");
	createLayerButton.addActionListener(this);
	createLayerButton.setActionCommand(CreateLayerCmd);
	gridbag.setConstraints(createLayerButton, c);
	configPanel.add(createLayerButton);
	createLayerButton.setEnabled(false);

	JPanel currentFeatureListPanel = PaletteHelper.createVerticalPanel(" Current Features: ");
	
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
	JLabel nameLabel = new JLabel("SymbolCode: ");
	gridbag2.setConstraints(nameLabel, c2);
	namePanel.add(nameLabel);
	
	c2.fill = GridBagConstraints.HORIZONTAL;
	c2.weightx = 1.0;
	c2.weighty = 1.0;
	nameField = new JTextField();
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
    }

    public void actionPerformed(ActionEvent ae) {
	String command = ae.getActionCommand();

	if (command == AddFeatureCmd) {
	} else if (command == CreateLayerCmd) {

	}
    }

    private void createNodes(DefaultMutableTreeNode top, SymbolReferenceLibrary srl) 
	throws FormatException {
	SymbolPart head = srl.getHead();
	addNodes(top, head);
    }

    private void addNodes(DefaultMutableTreeNode node, SymbolPart sp) {

        DefaultMutableTreeNode newNode = null;
	java.util.List subs = sp.getSubs();
	if (subs != null) {
	    for (Iterator it = subs.iterator(); it.hasNext();) {
		sp = (SymbolPart)it.next();
		newNode = new DefaultMutableTreeNode(sp);
		node.add(newNode);
		addNodes(newNode, sp);
	    }
	}
    }

    protected static void launchFrame(JComponent content, boolean exitOnClose) {
        JFrame frame = new JFrame("Create Symbol");

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
	SymbolChooser vpfc = new SymbolChooser(new SymbolReferenceLibrary());
	launchFrame(vpfc, true);
    }
}
