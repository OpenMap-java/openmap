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
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
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
import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * The SymbolChooser is a GUI symbol builder.  It can be used in
 * stand-alone mode to create image files, or be integrated into a
 * java application to create ImageIcons.
 */
public class SymbolChooser extends JPanel implements ActionListener {

    private static boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled", "Horizontal", and "None" (the default).
    private boolean playWithLineStyle = false;
    private String lineStyle = "Angled"; 
    protected boolean showAll = false;

    public final static String CreateImageCmd = "CreateImageCommand";
    public final static String EMPTY_FEATURE_LIST = null;

    DefaultMutableTreeNode currentSymbol = null;
    SymbolTreeHolder selectedTreeHolder;

    protected DrawingAttributes drawingAttributes = new DrawingAttributes();

    JButton clearFeaturesButton;
    JButton createImageFileButton;
    JTextField nameField;

    SymbolReferenceLibrary library;
    List trees;
    JScrollPane treeView;
    JPanel optionPanel;

    BufferedImage symbolImage;

    public SymbolChooser(SymbolReferenceLibrary srl) {

        library = srl;

        try {
            trees = createNodes(srl);
        } catch (FormatException fe) {
            Debug.output("Caught FormatException reading data: " + fe.getMessage());
        }

        init(srl, trees);
    }

    public void setSelectedTreeHolder(SymbolTreeHolder sth) {
        selectedTreeHolder = sth;
        treeView.setViewportView(sth.getTree());
        optionPanel.removeAll();
        optionPanel.add(sth.getOptionPanel());
        nameField.setText("");

        paintSymbolImage();
        revalidate();
    }

    protected void paintSymbolImage() {
        if (symbolImage != null) {
            Graphics2D g = (Graphics2D)symbolImage.getGraphics();
            g.setPaint(getBackground());
            g.fill(new Rectangle(0, 0, 100, 100));
        }

        if (currentSymbol != null) {

        }
    }

    protected void init(SymbolReferenceLibrary srl, List trees) {

        /////////////////////
        // Create the tree window by creating the scroll pane and add
        // the tree to it.
        GridBagLayout outergridbag = new GridBagLayout();
        GridBagConstraints outerc = new GridBagConstraints();

        JPanel setChoicePanel = new JPanel();
        JLabel setChoiceLabel = new JLabel("Symbol Set:");
        JComboBox setChoices = new JComboBox(trees.toArray());
        setChoices.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox jcb = (JComboBox) e.getSource();
                    setSelectedTreeHolder((SymbolTreeHolder)jcb.getSelectedItem());
                }
            });

        setChoicePanel.add(setChoiceLabel);
        setChoicePanel.add(setChoices);

        treeView = new JScrollPane(((SymbolTreeHolder)setChoices.getSelectedItem()).getTree());
        setLayout(outergridbag);

        outerc.fill = GridBagConstraints.BOTH;
        outerc.gridwidth = GridBagConstraints.REMAINDER;
        outerc.weighty = 0.0;
        outerc.insets = new Insets(5, 10, 5, 10);
        outergridbag.setConstraints(setChoicePanel, outerc);
        add(setChoicePanel);

        outerc.weightx = 1.0;
        outerc.weighty = 1.0;
        outerc.gridwidth = GridBagConstraints.RELATIVE;
        outergridbag.setConstraints(treeView, outerc);
        add(treeView);

        // Add the symbol preview area to the right of the tree
        JPanel symbolPanel = PaletteHelper.createVerticalPanel(" Current Symbol ");
        symbolImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        paintSymbolImage();
        ImageIcon ii = new ImageIcon(symbolImage);
        JLabel symbolImageArea = new JLabel(ii);
        symbolPanel.add(symbolImageArea);

        outerc.weightx = 0.0;
        outerc.gridwidth = GridBagConstraints.REMAINDER;
        outergridbag.setConstraints(symbolPanel, outerc);
        add(symbolPanel);

        /////////////////////

        optionPanel = PaletteHelper.createVerticalPanel(" Symbol Attributes ");
        optionPanel.add(((SymbolTreeHolder)setChoices.getSelectedItem()).getOptionPanel());
        outergridbag.setConstraints(optionPanel, outerc);
        add(optionPanel);

        /////////////////////
        // gridbag2 is for the name panel and the recent symbols.
        GridBagLayout gridbag2 = new GridBagLayout();
        GridBagConstraints c2 = new GridBagConstraints();
        JPanel namePanel = new JPanel();
        namePanel.setLayout(gridbag2);
        
        c2.weightx = 0;
        c2.anchor = GridBagConstraints.WEST;
        JLabel nameLabel = new JLabel("Symbol Code: ");
        gridbag2.setConstraints(nameLabel, c2);
        namePanel.add(nameLabel);
        
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 1.0;
        nameField = new JTextField();
        gridbag2.setConstraints(nameField, c2);
        namePanel.add(nameField);


        createImageFileButton = new JButton("Create Image File");
        createImageFileButton.addActionListener(this);
        createImageFileButton.setActionCommand(CreateImageCmd);
        createImageFileButton.setEnabled(false);

        c2.weightx = 0.0;
        gridbag2.setConstraints(createImageFileButton, c2);
        namePanel.add(createImageFileButton);

        outerc.weighty = 0.0;
        outerc.gridwidth = GridBagConstraints.REMAINDER;
        outergridbag.setConstraints(namePanel, outerc);
        add(namePanel);
        /////////////////////

    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();

        if (command == CreateImageCmd) {

        }
    }

    private List createNodes(SymbolReferenceLibrary srl) 
        throws FormatException {

        List treeList = new LinkedList();
        List subs = srl.getHead().getSubs();
        int count = 1;
        if (subs != null) {
            for (Iterator it = subs.iterator(); it.hasNext();) {
                SymbolPart schemeSymbolPart = (SymbolPart)it.next();

                CodeOptions options = ((CodeScheme)srl.positionTree.getFromChoices(count++)).getCodeOptions(null);

                treeList.add(new SymbolTreeHolder(schemeSymbolPart, options));
            }
        }

        return treeList;
    }

    private void addNodes(DefaultMutableTreeNode node, SymbolPart sp) {

        DefaultMutableTreeNode newNode = null;
        List subs = sp.getSubs();
        if (subs != null) {
            for (Iterator it = subs.iterator(); it.hasNext();) {
                sp = (SymbolPart)it.next();
                newNode = new DefaultMutableTreeNode(sp);
                node.add(newNode);
                addNodes(newNode, sp);
            }
        }
    }

    protected static void launchFrame(JComponent content, String title, boolean exitOnClose) {
        JFrame frame = new JFrame(title);

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
        SymbolChooser sc = new SymbolChooser(new SymbolReferenceLibrary());
        launchFrame(sc, "MIL-STD-2525B Symbology Chooser", true);
    }


    public class SymbolTreeHolder extends ListenerSupport implements TreeSelectionListener {

        JTree tree;
        JPanel optionPanel;
        CodeOptions options;

        public SymbolTreeHolder(SymbolPart schemeSymbolPart, CodeOptions opts) {
            super(schemeSymbolPart);
            DefaultMutableTreeNode top = new DefaultMutableTreeNode(schemeSymbolPart);
            addNodes(top, schemeSymbolPart);

            tree = new JTree(top);
            tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.setVisibleRowCount(10);
            tree.addTreeSelectionListener(this);

            if (playWithLineStyle) {
                tree.putClientProperty("JTree.lineStyle", lineStyle);
            }

            options = opts;
            optionPanel = getOptionPanel();
        }

        public JTree getTree() {
            return tree;
        }

        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = 
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            
            if (node == null) return;
            
            Object nodeInfo = node.getUserObject();
            if (nodeInfo instanceof SymbolPart) {
                SymbolPart symbolPart = (SymbolPart)nodeInfo;
                currentSymbol = node;
                nameField.setText(symbolPart.getSymbolCode());
            } else {
                nameField.setText("");
            }
        }

        public JPanel getOptionPanel() {
            if (optionPanel == null) {
                optionPanel = new JPanel();
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints c = new GridBagConstraints();
                optionPanel.setLayout(gridbag);

                if (options != null) {
                    int i = 0;
                    for (Iterator it = options.getOptions().iterator(); it.hasNext();) {
                        CodePosition cp = (CodePosition)it.next();
                        List lt = cp.getPositionChoices();

                        if (lt != null) {
                            JLabel label = new JLabel(cp.getPrettyName() + ": ");

                            c.gridx = 0;
                            c.gridy = i++;
                            c.weightx = 0;
                            c.fill = GridBagConstraints.NONE;
                            c.anchor = GridBagConstraints.EAST;

                            gridbag.setConstraints(label, c);
                            optionPanel.add(label);

                            JComboBox jcb = new JComboBox(lt.toArray());
                            jcb.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent ae) {
                                        setPositionSetting((CodePosition)((JComboBox)ae.getSource()).getSelectedItem());
                                    }
                                });
                                                  
                            c.gridx = 1;
                            c.anchor = GridBagConstraints.WEST;
                            c.fill = GridBagConstraints.HORIZONTAL;
                            c.weightx = 1f;

                            gridbag.setConstraints(jcb, c);
                            optionPanel.add(jcb);
                        }
                    }
                } else {
                    optionPanel.add(new JLabel("No options available for these symbols."));
                }

            }
            return optionPanel;
        }

        public void setPositionSetting(CodePosition cp) {
            Debug.output("Setting " + cp.getPrettyName());
        }

        public String toString() {
            return ((SymbolPart)getSource()).getCodePosition().getPrettyName();
        }

    }

}
