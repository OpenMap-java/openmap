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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolChooser.java,v $
// $RCSfile: SymbolChooser.java,v $
// $Revision: 1.16 $
// $Date: 2006/11/14 22:44:08 $
// $Author: kratkiew $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.bbn.openmap.event.ListenerSupport;
import com.bbn.openmap.gui.DimensionQueryPanel;
import com.bbn.openmap.image.AcmeGifFormatter;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.PaletteHelper;

/**
 * The SymbolChooser is a GUI symbol builder. It can be used in stand-alone mode
 * to create image files, or be integrated into a java application to create
 * ImageIcons.
 * <P>
 * To bring up this chooser, run this class as a standalone application, or call
 * showDialog(..)
 */
public class SymbolChooser extends JPanel implements ActionListener {

    public final static String CREATE_IMAGE_CMD = "CREATE_IMAGE_CMD";
    public final static String NAMEFIELD_CMD = "NAMEFIELD_CMD";
    public final static String EMPTY_FEATURE_LIST = null;
    public final static int DEFAULT_ICON_DIMENSION = 100;
    public final static String EMPTY_CODE = "---------------";

    protected static ImageIcon DEFAULT_SYMBOL_IMAGE;
    protected DrawingAttributes drawingAttributes = new DrawingAttributes();
    protected ImageIcon symbolImage;
    protected DefaultMutableTreeNode currentSymbol = null;
    protected SymbolTreeHolder currentSymbolTreeHolder;
    protected SymbolReferenceLibrary library;
    protected List trees;
    protected DimensionQueryPanel dqp;

    protected JButton clearFeaturesButton;
    protected JButton createImageFileButton;
    protected JTextField nameField;
    protected JLabel symbolImageLabel;
    protected JScrollPane treeView;
    protected JPanel optionPanel;
    protected Dimension iconDimension;
    protected boolean allowCreateImage = true;

    public SymbolChooser(SymbolReferenceLibrary srl) {

        library = srl;

        try {
            trees = createNodes(srl);
        } catch (FormatException fe) {
            Debug.output("SymbolChooser(): Caught FormatException reading data: "
                    + fe.getMessage());
        }

        init(srl, trees);
    }

    /**
     * Update the GUI with the contents of the provided SymbolTreeHolder,
     * reflecting a new set of symbols.
     * 
     * @param sth
     */
    public void setSelectedTreeHolder(SymbolTreeHolder sth) {
        treeView.setViewportView(sth.getTree());
        optionPanel.removeAll();
        optionPanel.add(sth.getOptionPanel());
        sth.handleNodeSelection((DefaultMutableTreeNode) sth.tree.getLastSelectedPathComponent());

        revalidate();
    }

    /**
     * Convenience function to get a standard blank image for those SymbolParts
     * that are not found by the SymbolImageMaker.
     * 
     * @return DEFAULT_SYMBOL_IMAGE
     */
    public static ImageIcon getNotFoundImageIcon() {
        if (DEFAULT_SYMBOL_IMAGE == null) {
            BufferedImage bi = new BufferedImage(DEFAULT_ICON_DIMENSION, DEFAULT_ICON_DIMENSION, BufferedImage.TYPE_INT_RGB);
            Graphics g = bi.getGraphics();
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, DEFAULT_ICON_DIMENSION, DEFAULT_ICON_DIMENSION);
            DEFAULT_SYMBOL_IMAGE = new ImageIcon(bi);
        }
        return DEFAULT_SYMBOL_IMAGE;
    }

    /**
     * Create the GUI based on the contents of the SymbolReferenceLibrary and
     * the SymbolPartTrees created from the options.
     * 
     * @param srl
     * @param trees
     */
    protected void init(SymbolReferenceLibrary srl, List trees) {

        // ///////////////////
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
                setSelectedTreeHolder((SymbolTreeHolder) jcb.getSelectedItem());
            }
        });

        currentSymbolTreeHolder = (SymbolTreeHolder) setChoices.getSelectedItem();

        setChoicePanel.add(setChoiceLabel);
        setChoicePanel.add(setChoices);

        treeView = new JScrollPane(currentSymbolTreeHolder.getTree());
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
        setImageIcon(getNotFoundImageIcon());
        symbolPanel.add(symbolImageLabel);

        outerc.weightx = 0.0;
        outerc.gridwidth = GridBagConstraints.REMAINDER;
        outergridbag.setConstraints(symbolPanel, outerc);

        dqp = new DimensionQueryPanel(getDesiredIconDimension());
        outergridbag.setConstraints(dqp, outerc);
        symbolPanel.add(dqp);

        add(symbolPanel);

        // ///////////////////

        optionPanel = PaletteHelper.createVerticalPanel(" Symbol Attributes ");
        optionPanel.add(((SymbolTreeHolder) setChoices.getSelectedItem()).getOptionPanel());
        outergridbag.setConstraints(optionPanel, outerc);
        add(optionPanel);

        // ///////////////////
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
        if (nameField == null) {
            nameField = new JTextField(EMPTY_CODE);
        }
        nameField.addActionListener(this);
        nameField.setActionCommand(NAMEFIELD_CMD);
        gridbag2.setConstraints(nameField, c2);
        namePanel.add(nameField);

        createImageFileButton = new JButton("Create Image File");
        createImageFileButton.addActionListener(this);
        createImageFileButton.setActionCommand(CREATE_IMAGE_CMD);
        createImageFileButton.setEnabled(false);
        createImageFileButton.setVisible(allowCreateImage);

        c2.weightx = 0.0;
        gridbag2.setConstraints(createImageFileButton, c2);
        namePanel.add(createImageFileButton);

        outerc.weighty = 0.0;
        outerc.gridwidth = GridBagConstraints.REMAINDER;
        outergridbag.setConstraints(namePanel, outerc);
        add(namePanel);
        // ///////////////////
        // Just call this to make sure that the stuff in the name
        // field matches the selected JTree
        setSelectedTreeHolder(currentSymbolTreeHolder);
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();

        if (command == CREATE_IMAGE_CMD && library != null && nameField != null) {
            try {
                setDesiredIconDimension(dqp.getDimension());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Width and height must be integers.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Dimension d = getDesiredIconDimension();
            ImageIcon ii = library.getIcon(getCode(), d);

            if (ii == null) {
                createImageFileButton.setEnabled(false);
                return;
            }

            try {
                BufferedImage bi = BufferedImageHelper.getBufferedImage(ii.getImage(),
                        0,
                        0,
                        (int) d.getWidth(),
                        (int) d.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                ImageFormatter formatter = new AcmeGifFormatter();
                byte[] imageBytes = formatter.formatImage(bi);
                String newFileName = FileUtils.getFilePathToSaveFromUser("Create File To Save");
                if (newFileName != null) {
                    FileOutputStream fos = new FileOutputStream(newFileName);
                    fos.write(imageBytes);
                    fos.flush();
                    fos.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (command == NAMEFIELD_CMD) {
            handleManualNameFieldUpdate(getCode());
        }

    }

    /**
     * Update the GUI to react to code typed into the string window.
     * 
     * @param text
     */
    protected void handleManualNameFieldUpdate(String text) {
        if (text == null)
            return;
        if (text.length() > 15) {
            text = text.substring(0, 15);
        }
        text = text.toUpperCase().replace('*', '-');

        for (Iterator it = trees.iterator(); it.hasNext();) {
            SymbolTreeHolder sth = (SymbolTreeHolder) it.next();

            if (sth != null) {
                DefaultMutableTreeNode node = sth.getNodeForCode(text);
                if (node != null) {
                    if (Debug.debugging("symbology")) {
                        Debug.output("SymbolChooser: Found node for " + text);
                    }
                    sth.getTree()
                            .setSelectionPath(new TreePath(node.getPath()));
                    sth.updateOptionsForCode(text);
                    setSelectedTreeHolder(sth);
                }
            }
        }
    }

    /**
     * Initialization method to create the SymbolTreeHolders from the
     * SymbolReferenceLibrary.
     * 
     * @param srl
     * @return List of nodes of the symbol part tree.
     * @throws FormatException
     */
    protected List createNodes(SymbolReferenceLibrary srl)
            throws FormatException {

        List treeList = new LinkedList();
        List subs = srl.getHead().getSubs();
        int count = 1;
        if (subs != null) {
            for (Iterator it = subs.iterator(); it.hasNext();) {
                SymbolPart schemeSymbolPart = (SymbolPart) it.next();

                CodeOptions options = ((CodeScheme) srl.positionTree.getFromChoices(count++)).getCodeOptions(null);

                treeList.add(new SymbolTreeHolder(schemeSymbolPart, options));
            }
        }

        return treeList;
    }

    /**
     * Get the current symbol code listed in the GUI.
     * 
     * @return code for current symbol.
     */
    public String getCode() {
        if (nameField != null)
            return nameField.getText();
        return EMPTY_CODE;
    }

    /**
     * Set the symbol code in the GUI.
     */
    public void setCode(String code) {
        if (nameField == null) {
            // If we do this here, the default jtree presented
            // will be able to put it's default symbol code in the
            // text field widget. Has to be done before that
            // default JTree is made.
            nameField = new JTextField(code);
        } else {
            nameField.setText(code);
        }
    }

    /**
     * Get the icon displayed in the GUI.
     * 
     * @return ImageIcon being displayed.
     */
    public ImageIcon getImageIcon() {
        return symbolImage;
    }

    /**
     * Set the current icon in the display.
     * 
     * @param ii
     */
    public void setImageIcon(ImageIcon ii) {
        symbolImage = ii;

        if (symbolImageLabel == null) {
            symbolImageLabel = new JLabel(symbolImage);
        } else {
            symbolImageLabel.setIcon(symbolImage);
        }
    }

    /**
     * Set the dimension o the icon to be created.
     * 
     * @param d
     */
    public void setDesiredIconDimension(Dimension d) {
        iconDimension = d;

        dqp.setDimension(getDesiredIconDimension());
    }

    /**
     * @return the dimension of the icon to be created.
     */
    public Dimension getDesiredIconDimension() {
        if (iconDimension == null) {
            iconDimension = new Dimension(DEFAULT_ICON_DIMENSION, DEFAULT_ICON_DIMENSION);
        }
        return iconDimension;
    }

    /**
     * @return Returns the allowCreateImage.
     */
    public boolean isAllowCreateImage() {
        return allowCreateImage;
    }

    /**
     * @param allowCreateImage The allowCreateImage to set.
     */
    public void setAllowCreateImage(boolean allowCreateImage) {
        this.allowCreateImage = allowCreateImage;
        if (createImageFileButton != null) {
            createImageFileButton.setVisible(allowCreateImage);
        }
    }

    public static ImageIcon showDialog(Component component, String title,
                                       SymbolReferenceLibrary srl,
                                       String defaultSymbolCode)
            throws HeadlessException {

        final SymbolChooser pane = new SymbolChooser(srl);

        SymbolTracker ok = new SymbolTracker(pane);
        JDialog dialog = createDialog(component, title, true, pane, ok, null);
        dialog.addWindowListener(new SymbolChooserDialog.Closer());
        dialog.addComponentListener(new SymbolChooserDialog.DisposeOnClose());
        pane.setCode(defaultSymbolCode);
        pane.handleManualNameFieldUpdate(defaultSymbolCode);
        dialog.setVisible(true); // blocks until user brings dialog down...

        return ok.getImageIcon();
    }

    /**
     * Creates JDialog window displaying a SymbolChooser.
     */
    public static JDialog createDialog(Component c, String title,
                                       boolean modal,
                                       SymbolChooser chooserPane,
                                       ActionListener okListener,
                                       ActionListener cancelListener)
            throws HeadlessException {

        return new SymbolChooserDialog(c, title, modal, chooserPane, okListener, cancelListener);
    }

    public static void main(String[] args) {
        Debug.init();

        ArgParser ap = new ArgParser("SymbolChooser");
        ap.add("type",
                "Type of symbol image set being used (PNG, GIF or SVG, PNG is default)",
                1);
        ap.add("path",
                "Path to root directory of symbol image set if not in classpath",
                1);
        ap.add("default", "15 character code for default icon", 1);
        ap.add("verbose", "Print messages");

        if (!ap.parse(args)) {
            ap.printUsage();
            System.exit(0);
        }

        String arg[];
        arg = ap.getArgValues("type");
        String symbolImageMakerClass = "com.bbn.openmap.tools.symbology.milStd2525.PNGSymbolImageMaker";
        if (arg != null) {
            if (arg[0].equalsIgnoreCase("SVG")) {
                symbolImageMakerClass = "com.bbn.openmap.tools.symbology.milStd2525.SVGSymbolImageMaker";
            } else if (arg[0].equalsIgnoreCase("GIF")) {
                symbolImageMakerClass = "com.bbn.openmap.tools.symbology.milStd2525.GIFSymbolImageMaker";
            }
        }

        String defaultSymbolCode = "SFPPV-----*****";
        arg = ap.getArgValues("default");
        if (arg != null) {
            defaultSymbolCode = arg[0];
        }

        arg = ap.getArgValues("verbose");
        if (arg != null) {
            Debug.put("symbology");
        }

        SymbolReferenceLibrary srl = new SymbolReferenceLibrary();
        if (srl.setSymbolImageMaker(symbolImageMakerClass) != null) {

            arg = ap.getArgValues("path");
            if (arg != null) {
                srl.getSymbolImageMaker().setDataPath(arg[0]);
            }

            SymbolChooser.showDialog(null,
                    "MIL-STD-2525B Symbol Chooser",
                    srl,
                    defaultSymbolCode);
        } else {
            Debug.output("Couldn't create SymbolImageMaker");
        }

        System.exit(0);
    }

    public class SymbolTreeHolder extends ListenerSupport implements
            TreeSelectionListener {
        // Optionally play with line styles. Possible values are
        // "Angled", "Horizontal", and "None" (the default).
        protected boolean playWithTreeLineStyle = false;
        protected String treeLineStyle = "Angled";

        protected JTree tree;
        protected JPanel optionPanel;
        protected CodeOptions options;
        protected Character[] optionChars = new Character[15];
        protected Hashtable optionMenuHashtable;

        public SymbolTreeHolder(SymbolPart schemeSymbolPart, CodeOptions opts) {
            super(schemeSymbolPart);
            DefaultMutableTreeNode top = new DefaultMutableTreeNode(schemeSymbolPart);
            addNodes(top, schemeSymbolPart);

            tree = new JTree(top);
            tree.getSelectionModel()
                    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.setVisibleRowCount(10);
            tree.addTreeSelectionListener(this);
            tree.setSelectionPath(new TreePath(top));

            if (playWithTreeLineStyle) {
                tree.putClientProperty("JTree.lineStyle", treeLineStyle);
            }

            options = opts;
            optionPanel = getOptionPanel();
        }

        public JTree getTree() {
            return tree;
        }

        protected void addNodes(DefaultMutableTreeNode node, SymbolPart sp) {

            DefaultMutableTreeNode newNode = null;
            List subs = sp.getSubs();
            if (subs != null) {
                for (Iterator it = subs.iterator(); it.hasNext();) {
                    sp = (SymbolPart) it.next();
                    newNode = new DefaultMutableTreeNode(sp);
                    node.add(newNode);
                    addNodes(newNode, sp);
                }
            }
        }

        public DefaultMutableTreeNode getNodeForCode(String code) {

            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
                    .getRoot();

            SymbolPart sp = (SymbolPart) root.getUserObject();

            if (Debug.debugging("symbology.detail")) {
                Debug.output("Tree root object has " + sp.getClass().getName()
                        + " user object with code |" + sp.getCode()
                        + "| at code position "
                        + sp.getCodePosition().startIndex);
            }

            if (sp.codeMatches(code)) {
                return getNodeForCodeStartingAt(root, code);
            } else {
                return null;
            }
        }

        protected DefaultMutableTreeNode getNodeForCodeStartingAt(
                                                                  DefaultMutableTreeNode node,
                                                                  String code) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode kid = (DefaultMutableTreeNode) enumeration.nextElement();
                SymbolPart ssp = (SymbolPart) kid.getUserObject();

                try {
                    if (code.charAt(ssp.getCodePosition().startIndex) == '-')
                        return node;

                    if (ssp.codeMatches(code)) {
                        return getNodeForCodeStartingAt(kid, code);
                    }
                } catch (StringIndexOutOfBoundsException sioobe) {
                } catch (NullPointerException npe) {
                }
            }

            return node;
        }

        /**
         * Given an text string, have the options available to the current
         * SymbolTreeHolder reflect those updates.
         * 
         * @param text
         */
        protected void updateOptionsForCode(String text) {
            for (Iterator it = options.getOptions().iterator(); it.hasNext();) {
                CodePosition cp = (CodePosition) it.next();
                JComboBox jcb = (JComboBox) optionMenuHashtable.get(cp);
                if (jcb != null) {
                    int numComps = jcb.getItemCount();
                    for (int i = 0; i < numComps; i++) {
                        if (((CodePosition) jcb.getItemAt(i)).codeMatches(text)) {
                            jcb.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        }

        public void valueChanged(TreeSelectionEvent e) {
            handleNodeSelection((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
        }

        public void updateInterfaceToLastSelectedNode() {
            handleNodeSelection((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
        }

        public void handleNodeSelection(DefaultMutableTreeNode node) {

            if (node == null) {
                setCode("");
                return;
            }

            Object nodeInfo = node.getUserObject();
            if (nodeInfo instanceof SymbolPart) {
                SymbolPart symbolPart = (SymbolPart) nodeInfo;
                currentSymbol = node;
                setCode(updateStringWithCurrentOptionChars(symbolPart.getSymbolCode()));
                ImageIcon ii = library.getIcon(getCode(),
                        new Dimension(DEFAULT_ICON_DIMENSION, DEFAULT_ICON_DIMENSION));
                if (createImageFileButton != null) {
                    createImageFileButton.setEnabled(ii != null);
                }

                if (ii == null) {
                    ii = getNotFoundImageIcon();
                }
                setImageIcon(ii);
            } else {
                setCode("");
                setImageIcon(getNotFoundImageIcon());
                createImageFileButton.setEnabled(false);
            }
        }

        public JPanel getOptionPanel() {
            if (optionPanel == null) {
                optionMenuHashtable = new Hashtable();
                optionPanel = new JPanel();
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints c = new GridBagConstraints();
                optionPanel.setLayout(gridbag);

                if (options != null) {
                    int i = 0;
                    for (Iterator it = options.getOptions().iterator(); it.hasNext();) {
                        CodePosition cp = (CodePosition) it.next();
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
                                    setPositionSetting((CodePosition) ((JComboBox) ae.getSource()).getSelectedItem());
                                    updateInterfaceToLastSelectedNode();
                                }
                            });

                            c.gridx = 1;
                            c.anchor = GridBagConstraints.WEST;
                            c.fill = GridBagConstraints.HORIZONTAL;
                            c.weightx = 1f;

                            gridbag.setConstraints(jcb, c);
                            optionPanel.add(jcb);
                            optionMenuHashtable.put(cp, jcb);
                        }
                    }
                } else {
                    optionPanel.add(new JLabel("No options available for these symbols."));
                }

            }
            return optionPanel;
        }

        public void setPositionSetting(CodePosition cp) {
            if (Debug.debugging("codeposition")) {
                Debug.output("Setting " + cp.getPrettyName() + " ["
                        + cp.getID() + "] at " + cp.getStartIndex() + ", "
                        + cp.getEndIndex());
            }
            updateOptionChars(cp);
            setCode(updateStringWithCurrentOptionChars(getCode()));
        }

        public void updateOptionChars(CodePosition cp) {
            String cpString = cp.getID();
            for (int i = 0; i < cpString.length(); i++) {
                char curChar = cpString.charAt(i);
                optionChars[cp.getStartIndex() + i] = new Character(curChar);
            }
        }

        public String updateStringWithCurrentOptionChars(
                                                         String currentSymbolCode) {
            try {
                StringBuffer buf = new StringBuffer(currentSymbolCode);
                for (int i = 0; i < optionChars.length; i++) {
                    Character c = optionChars[i];
                    if (c != null) {
                        buf.setCharAt(i, c.charValue());
                    }

                }
                currentSymbolCode = buf.toString();
            } catch (StringIndexOutOfBoundsException siobe) {
            } catch (NullPointerException npe) {
            }
            return currentSymbolCode;
        }

        public String toString() {
            return ((SymbolPart) getSource()).getCodePosition().getPrettyName();
        }

    }

}

/*
 * Class which builds a symbol chooser dialog consisting of a SymbolChooser with
 * "Ok", "Cancel", and "Reset" buttons. This class is based on the contents of
 * the JColorChooser components.
 */

class SymbolChooserDialog extends JDialog {
    private String initialCode;
    private SymbolChooser chooserPane;

    public SymbolChooserDialog(Component c, String title, boolean modal,
            SymbolChooser chooserPane, ActionListener okListener,
            ActionListener cancelListener) throws HeadlessException {

        super(JOptionPane.getFrameForComponent(c), title, modal);
        this.chooserPane = chooserPane;

        String okString = UIManager.getString("ColorChooser.okText");
        String cancelString = UIManager.getString("ColorChooser.cancelText");
        String resetString = UIManager.getString("ColorChooser.resetText");

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane, BorderLayout.CENTER);

        /*
         * Create Lower button panel
         */
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(okString);
        getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");
        if (okListener != null) {
            okButton.addActionListener(okListener);
        }
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPane.add(okButton);

        JButton cancelButton = new JButton(cancelString);

        cancelButton.setActionCommand("cancel");
        if (cancelListener != null) {
            cancelButton.addActionListener(cancelListener);
        }
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        buttonPane.add(resetButton);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        applyComponentOrientation(((c == null) ? getRootPane() : c).getComponentOrientation());

        pack();
        setLocationRelativeTo(c);
    }

    public void setVisible(boolean val) {
        if (val) {
            initialCode = chooserPane.getCode();
        }
        super.setVisible(val);
    }

    public void reset() {
        chooserPane.setCode(initialCode);
        chooserPane.handleManualNameFieldUpdate(initialCode);
    }

    static class Closer extends WindowAdapter implements Serializable {
        public void windowClosing(WindowEvent e) {
            Window w = e.getWindow();
            w.setVisible(false);
        }
    }

    static class DisposeOnClose extends ComponentAdapter implements
            Serializable {
        public void componentHidden(ComponentEvent e) {
            Window w = (Window) e.getComponent();
            w.dispose();
        }
    }

}

class SymbolTracker implements ActionListener, Serializable {
    SymbolChooser chooser;
    ImageIcon icon;

    public SymbolTracker(SymbolChooser c) {
        chooser = c;
    }

    public void actionPerformed(ActionEvent e) {
    // This is subject to too many timing problems, it's easier if we just get the icon
    // when asked in getImageIcon.

    // icon = chooser.library.getIcon(chooser.getCode(),
    // chooser.getDesiredIconDimension());
    }

    public ImageIcon getImageIcon() {
        try {
            return chooser.library.getIcon(chooser.getCode(),
                    chooser.getDesiredIconDimension());
        } catch (NullPointerException npe) {
            Debug.error("SymbolChooser.SymbolTracker: something messed up with chooser:");
            npe.printStackTrace();
        }
        return null;
    }
}
