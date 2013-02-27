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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMDrawingToolLauncher.java,v $
// $RCSfile: OMDrawingToolLauncher.java,v $
// $Revision: 1.21 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.OMToolComponent;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.geom.NonRegional;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * This tool is a widget that calls the OMDrawingTool to create a specific
 * graphic. The launcher is completely configured by EditToolLaunchers it gets
 * told about from the DrawingTool, and OMGraphicHandlers that it finds in a
 * MapHandler. There are no methods to manually add stuff to this GUI.
 * <p>
 * 
 * There are two properties that can be set for the launcher:
 * 
 * <pre>
 * 
 * 
 * 
 * 
 *       # Number of launcher buttons to place in a row in that part of the
 *       # GUI. -1 (the default) is to keep them all on one line.
 *       omdtl.horizNumLoaderButtons=-1
 * 
 *       # If set to true, a text popup will be used for the OMGraphic
 *       # loaders instead of buttons (false is default).
 *       omdtl.useTextLabels=false
 * 
 * 
 * 
 * </pre>
 */
public class OMDrawingToolLauncher extends OMToolComponent implements ActionListener,
        PropertyChangeListener {

    protected DrawingTool drawingTool;
    protected boolean useTextEditToolTitles = false;
    protected GraphicAttributes defaultGraphicAttributes = new GraphicAttributes();
    public int maxHorNumLoaderButtons = -1;

    // Places buttons in alphabetical order
    // protected TreeMap loaders = new TreeMap();
    protected Vector<LoaderHolder> loaders = new Vector<LoaderHolder>();

    protected Vector<DrawingToolRequestor> drawingToolRequestors = new Vector<DrawingToolRequestor>();

    protected DrawingToolRequestor currentRequestor;
    protected String currentCreation;
    protected JComboBox requestors;
    protected JButton createButton;
    protected JPanel panel3;

    /**
     * Property for setting the maximum number of loader buttons to allow in the
     * horizontal direction in the GUI (horizNumLoaderButtons). -1 means to just
     * lay them out in one row.
     */
    public final static String HorizontalNumberOfLoaderButtonsProperty = "horizNumLoaderButtons";
    public final static String UseLoaderTextProperty = "useTextLabels";

    String[] rtc = { i18n.get(OMDrawingToolLauncher.class, "renderingType.LatLon", "Lat/Lon"),
            i18n.get(OMDrawingToolLauncher.class, "renderingType.XY", "X/Y"),
            i18n.get(OMDrawingToolLauncher.class, "renderingType.XYOffset", "X/Y Offset") };

    public final static String CreateCmd = "CREATE";

    /** Default key for the DrawingToolLauncher Tool. */
    public static final String defaultKey = "omdrawingtoollauncher";

    public OMDrawingToolLauncher() {
        super();

        setWindowSupport(new WindowSupport(this, i18n.get(OMDrawingToolLauncher.class, "omdrawingtoollauncher", "Drawing Tool Launcher")));
        setKey(defaultKey);
        defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        defaultGraphicAttributes.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
        resetGUI();
    }

    /**
     * Set the DrawingTool for this launcher.
     */
    public void setDrawingTool(DrawingTool dt) {
        if (drawingTool instanceof OMDrawingTool) {
            ((OMDrawingTool) drawingTool).removePropertyChangeListener(this);
        }

        drawingTool = dt;

        if (drawingTool instanceof OMDrawingTool) {
            ((OMDrawingTool) drawingTool).addPropertyChangeListener(this);
        }
    }

    public DrawingTool getDrawingTool() {
        return drawingTool;
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand().intern();

        Debug.message("drawingtool", "DrawingToolLauncher.actionPerformed(): " + command);

        // This is important. We need to set the current projection
        // before setting the projection in the MapBean. That way,
        // the projectionChanged method actions won't get fired
        if (command == CreateCmd) {
            // Get the active EditToolLoader
            DrawingTool dt = getDrawingTool();

            if (dt instanceof OMDrawingTool) {
                OMDrawingTool omdt = (OMDrawingTool) dt;

                if (omdt.isActivated()) {
                    omdt.deactivate();
                }
            }

            if (dt != null && currentCreation != null && currentRequestor != null) {
                // Copy the default GraphicAttributes into another
                // copy...
                GraphicAttributes ga = (GraphicAttributes) defaultGraphicAttributes.clone();

                // fire it up!
                dt.setBehaviorMask(OMDrawingTool.DEFAULT_BEHAVIOR_MASK);
                dt.create(currentCreation, ga, currentRequestor);
            } else {

                StringBuffer sb = new StringBuffer();
                StringBuffer em = new StringBuffer();

                if (dt == null) {
                    sb.append("   No drawing tool is available!\n");
                    em.append(i18n.get(OMDrawingToolLauncher.class, "noDrowingTool", "   No drawing tool is available!\n"));
                } else {
                    sb.append("   Drawing tool OK.\n");
                }

                if (currentCreation == null) {
                    sb.append("   No valid choice of graphic to create.\n");
                    em.append(i18n.get(OMDrawingToolLauncher.class, "noValidChoice", "   No valid choice of graphic to create.\n"));
                } else {
                    sb.append("   Graphic choice OK.\n");
                }

                if (currentRequestor == null) {
                    sb.append("   No valid receiver for the created graphic.\n");
                    em.append(i18n.get(OMDrawingToolLauncher.class, "noValidReceiver", "   No valid receiver for the created graphic.\n"));
                } else {
                    sb.append("   Graphic receiver OK.\n");
                }

                Debug.output("OMDrawingToolLauncher: Something is not set:\n" + sb.toString());

                MapHandler mapHandler = (MapHandler) getBeanContext();
                if (mapHandler != null) {
                    InformationDelegator id = (InformationDelegator) mapHandler.get("com.bbn.openmap.InformationDelegator");
                    if (id != null) {

                        id.displayMessage(i18n.get(OMDrawingToolLauncher.class, "problem", "Problem"), i18n.get(OMDrawingToolLauncher.class, "problemCreatingGraphic", "Problem creating new graphic:\n")
                                + em.toString());
                    }
                }
            }
        }
    }

    /**
     * Set the current requestor to receive a requested OMGraphic. Changes are
     * reflected in the GUI, and setCurrentRequestor() will eventually be
     * called.
     */
    public void setRequestor(String aName) {
        if (requestors != null) {
            if (aName != null) {
                requestors.setSelectedItem(aName);
            } else {
                if (!drawingToolRequestors.isEmpty()) {
                    setRequestor(((DrawingToolRequestor) drawingToolRequestors.elementAt(0)).getName());
                }
            }
        }
    }

    /**
     * Fills combobox with recent values.
     */
    private void resetCombo() {
        Object oldChoice = null;
        if (requestors != null) {
            oldChoice = requestors.getSelectedItem();

            ActionListener[] actions = requestors.getActionListeners();
            for (int loop = 0; loop < actions.length; loop++) {
                requestors.removeActionListener(actions[loop]);
            }
            requestors.removeAllItems();
            for (DrawingToolRequestor requestor : drawingToolRequestors) {
                requestors.addItem(requestor.getName());
            }
            if (oldChoice != null) {
                requestors.setSelectedItem(oldChoice);
            }
            for (int loop = 0; loop < actions.length; loop++) {
                requestors.addActionListener(actions[loop]);
            }
            setCurrentRequestor((String) requestors.getSelectedItem());
        }
        createButton.setEnabled(drawingToolRequestors != null && !drawingToolRequestors.isEmpty());
    }

    /**
     * Build the stuff that goes in the launcher.
     */
    public void resetGUI() {
        removeAll();

        JPanel palette = new JPanel();
        palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));
        palette.setAlignmentX(Component.CENTER_ALIGNMENT); // LEFT
        palette.setAlignmentY(Component.CENTER_ALIGNMENT); // BOTTOM

        String[] requestorNames = new String[drawingToolRequestors.size()];

        if (Debug.debugging("omdtl")) {
            Debug.output("Have " + requestorNames.length + " REQUESTORS");
        }

        for (int i = 0; i < requestorNames.length; i++) {
            requestorNames[i] = ((DrawingToolRequestor) drawingToolRequestors.elementAt(i)).getName();
            if (requestorNames[i] == null) {
                Debug.output("OMDrawingToolLauncher has a requestor that is unnamed.  Please assign a name to the requestor");
                requestorNames[i] = "-- Unnamed --";
            }
            if (Debug.debugging("omdtl")) {
                Debug.output("Adding REQUESTOR " + requestorNames[i] + " to menu");
            }
        }

        Object oldChoice = null;
        if (requestors != null) {
            oldChoice = requestors.getSelectedItem();
        }

        requestors = new JComboBox(requestorNames);
        requestors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcb = (JComboBox) e.getSource();
                String currentChoice = (String) jcb.getSelectedItem();
                setCurrentRequestor(currentChoice);
            }
        });

        if (requestorNames.length > 0) {
            if (oldChoice == null) {
                requestors.setSelectedIndex(0);
            } else {
                requestors.setSelectedItem(oldChoice);
            }
        }

        JPanel panel = PaletteHelper.createPaletteJPanel(i18n.get(OMDrawingToolLauncher.class, "panelSendTo", "Send To:"));
        panel.add(requestors);
        palette.add(panel);

        if (Debug.debugging("omdtl")) {
            Debug.output("Figuring out tools, using names");
        }

        panel = PaletteHelper.createPaletteJPanel(i18n.get(OMDrawingToolLauncher.class, "panelGraphicType", "Graphic Type:"));
        panel.add(getToolWidgets(useTextEditToolTitles));
        palette.add(panel);

        String[] renderTypes = new String[3];

        renderTypes[OMGraphic.RENDERTYPE_LATLON - 1] = rtc[0];
        renderTypes[OMGraphic.RENDERTYPE_XY - 1] = rtc[1];
        renderTypes[OMGraphic.RENDERTYPE_OFFSET - 1] = rtc[2];

        JComboBox renderTypeList = new JComboBox(renderTypes);
        renderTypeList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcb = (JComboBox) e.getSource();
                String currentChoice = (String) jcb.getSelectedItem();
                if (currentChoice.equals(rtc[2])) {
                    defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
                } else if (currentChoice.equals(rtc[1])) {
                    defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_XY);
                } else {
                    defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_LATLON);
                }
            }
        });

        renderTypeList.setSelectedIndex(defaultGraphicAttributes.getRenderType() - 1);

        panel = PaletteHelper.createHorizontalPanel(i18n.get(OMDrawingToolLauncher.class, "panelGraphicAttributes", "Graphic Attributes:"));
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        panel.setLayout(gridbag);

        String interString = i18n.get(OMDrawingToolLauncher.class, "panelRenderingType", "Rendering Type:");
        JPanel panel2 = new JPanel();
        JLabel renderTypeLabel = new JLabel(interString);
        panel2.add(renderTypeLabel);
        panel2.add(renderTypeList);

        panel3 = new JPanel();
        updateDrawingAttributesGUI();

        gridbag.setConstraints(panel2, c);
        panel.add(panel2);
        gridbag.setConstraints(panel3, c);
        panel.add(panel3);
        palette.add(panel);

        createButton = new JButton(i18n.get(OMDrawingToolLauncher.class, "createButton", "Create Graphic"));
        createButton.setActionCommand(CreateCmd);
        createButton.addActionListener(this);

        JPanel dismissBox = new JPanel();
        JButton dismiss = new JButton(i18n.get(OMDrawingToolLauncher.class, "dismiss", "Close"));
        dismissBox.setLayout(new BoxLayout(dismissBox, BoxLayout.X_AXIS));
        dismissBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        dismissBox.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        dismissBox.add(createButton);
        dismissBox.add(dismiss);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.BOTTOM_ALIGNMENT);
        add(palette);
        add(dismissBox);

        dismiss.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getWindowSupport().killWindow();
            }
        });
    }

    protected void updateDrawingAttributesGUI() {
        panel3.removeAll();
        String interString = i18n.get(OMDrawingToolLauncher.class, "panelLineColorTypes", "Line/Colors:");
        JLabel lcLabel = new JLabel(interString);
        panel3.add(lcLabel);
        panel3.add(defaultGraphicAttributes.getGUI());
    }

    protected JComponent getToolWidgets() {
        JPanel iconBar = new JPanel();
        // this parameters should be read from properties!
        iconBar.setLayout(new GridLayout(2, 4));
        ButtonGroup bg = new ButtonGroup();
        JToggleButton btn = null;
        boolean setFirstButtonSelected = true;

        for (LoaderHolder lh : loaders) {
            String pName = lh.prettyName;
            EditToolLoader etl = lh.loader;
            ImageIcon icon = etl.getIcon(getEditableClassName(pName));
            btn = new JToggleButton(icon);
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setToolTipText(pName);
            btn.setActionCommand(pName);
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setCurrentCreation(ae.getActionCommand());
                }
            });
            if (setFirstButtonSelected) {
                btn.setSelected(true);
                setCurrentCreation(pName);
                setFirstButtonSelected = false;
            }
            bg.add(btn);
            iconBar.add(btn);
        }
        return iconBar;
    }

    protected JComponent getToolWidgets(boolean useText) {

        if (useText) {
            // Set editables with all the pretty names.
            Vector<String> editables = new Vector<String>();

            for (LoaderHolder lh : loaders) {
                editables.add(lh.prettyName);
            }

            return createToolOptionMenu(editables);
        } else {
            return createToolButtonPanel();
        }
    }

    private JComboBox createToolOptionMenu(Vector<String> editables) {
        String[] toolNames = new String[editables.size()];
        for (int i = 0; i < toolNames.length; i++) {
            toolNames[i] = editables.elementAt(i);
            if (Debug.debugging("omdtl")) {
                Debug.output("Adding TOOL " + toolNames[i] + " to menu");
            }
        }

        JComboBox tools = new JComboBox(toolNames);
        tools.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcb = (JComboBox) e.getSource();
                String currentChoice = (String) jcb.getSelectedItem();
                setCurrentCreation(currentChoice);
            }
        });

        if (toolNames.length > 0) {
            tools.setSelectedIndex(0);
        }

        return tools;
    }

    private JPanel createToolButtonPanel() {
        // Otherwise, create a set of buttons.
        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        panel.setLayout(gridbag);

        ButtonGroup bg = new ButtonGroup();

        int toolbarCount = 0;
        boolean limitWidth = false;
        if (maxHorNumLoaderButtons >= 0) {
            limitWidth = true;
        }

        JToggleButton btn;
        JToolBar iconBar = null;
        boolean activeSet = false;
        for (LoaderHolder lh : loaders) {

            if (toolbarCount == 0) {
                iconBar = new JToolBar();
                iconBar.setFloatable(false);
                gridbag.setConstraints(iconBar, c);
                panel.add(iconBar);
            }

            String pName = lh.prettyName;
            EditToolLoader etl = lh.loader;
            ImageIcon icon = etl.getIcon(getEditableClassName(pName));

            btn = new JToggleButton(icon, !activeSet);
            btn.setToolTipText(pName);
            btn.setActionCommand(pName);
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setCurrentCreation(ae.getActionCommand());
                }
            });
            bg.add(btn);

            if (iconBar != null) {
                iconBar.add(btn);
            }

            toolbarCount++;

            // Just set one as active, the first one.
            if (!activeSet) {
                setCurrentCreation(pName);
                activeSet = true;
            }

            if (limitWidth && toolbarCount >= maxHorNumLoaderButtons) {
                toolbarCount = 0;
            }
        }

        return panel;
    }

    /**
     * Set the component that will receive the new/edited OMGraphic from the
     * DrawingTool. Does not change the GUI. Called when the combo box changes.
     * 
     * @param name GUI pretty name of requestor.
     */
    public void setCurrentRequestor(String name) {
        if (name == null) {
            return;
        }

        for (DrawingToolRequestor dtr : drawingToolRequestors) {
            if (name.equals(dtr.getName())) {
                currentRequestor = dtr;
                return;
            }
        }
        currentRequestor = null;
    }

    /**
     * Set the next thing to be created to be whatever the pretty name
     * represents. Sets currentCreation.
     * 
     * @param name GUI pretty name of thing to be created, from one of the
     *        EditToolLoaders.
     */
    public void setCurrentCreation(String name) {
        currentCreation = getEditableClassName(name);
    }

    /**
     * Given a pretty name, look through the EditToolLoaders and find out the
     * classname that goes with editing it.
     * 
     * @param prettyName GUI pretty name of tool, or thing to be created, from
     *        one of the EditToolLoaders.
     */
    public String getEditableClassName(String prettyName) {
        for (LoaderHolder lh : loaders) {
            EditToolLoader etl = lh.loader;
            String[] ec = etl.getEditableClasses();

            for (int i = 0; i < ec.length; i++) {
                if (prettyName.equals(etl.getPrettyName(ec[i]))) {
                    defaultGraphicAttributes.setEnableFillPaintChoice(!(etl instanceof NonRegional));
                    updateDrawingAttributesGUI();
                    panel3.revalidate();
                    return ec[i];
                }
            }
        }
        return null;
    }

    /**
     * This is the method that your object can use to find other objects within
     * the MapHandler (BeanContext). This method gets called when the object
     * gets added to the MapHandler, or when another object gets added to the
     * MapHandler after the object is a member.
     * 
     * @param someObj the object that was added to the BeanContext (MapHandler)
     *        that is being searched for. Find the ones you need, and hook
     *        yourself up.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof OMDrawingTool) {
            Debug.message("omdtl", "OMDrawingToolLauncher found a DrawingTool.");
            setDrawingTool((DrawingTool) someObj);
        }
        if (someObj instanceof DrawingToolRequestor) {
            if (Debug.debugging("omdtl")) {
                Debug.output("OMDrawingToolLauncher found a DrawingToolRequestor - "
                        + ((DrawingToolRequestor) someObj).getName());
            }
            drawingToolRequestors.add((DrawingToolRequestor) someObj);
            // resetGUI();
            resetCombo();
        }
    }

    /**
     * BeanContextMembershipListener method. Called when a new object is removed
     * from the BeanContext of this object. For the Layer, this method doesn't
     * do anything. If your layer does something with the childrenAdded method,
     * or findAndInit, you should take steps in this method to unhook the layer
     * from the object used in those methods.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof OMDrawingTool) {
            Debug.message("omdtl", "OMDrawingToolLauncher found a DrawingTool.");
            OMDrawingTool dt = (OMDrawingTool) someObj;
            if (dt == getDrawingTool()) {
                setDrawingTool(null);
                dt.removePropertyChangeListener(this);
            }
        }

        if (someObj instanceof DrawingToolRequestor) {
            if (Debug.debugging("omdtl")) {
                Debug.output("OMDrawingToolLauncher removing a DrawingToolRequestor - "
                        + ((DrawingToolRequestor) someObj).getName());
            }
            drawingToolRequestors.remove((DrawingToolRequestor) someObj);
            if (drawingToolRequestors.isEmpty()) {// there is no
                // Requestor, so
                // lets remove the
                // window.
                getWindowSupport().killWindow();
                currentRequestor = null;
            }
            // resetGUI();
            resetCombo();
            setRequestor(null);
        }
    }

    /**
     * Tool interface method. The retrieval tool's interface. This method
     * creates a button that will bring up the LauncherPanel.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        JToolBar jtb = null;
        if (getUseAsTool()) {
            jtb = new com.bbn.openmap.gui.GridBagToolBar();
            // "Drawing Tool Launcher";
            JButton drawingToolButton = new JButton(new ImageIcon(OMDrawingToolLauncher.class.getResource("Drawing.gif"), i18n.get(OMDrawingToolLauncher.class, "drawingToolButton", I18n.TOOLTIP, "Drawing Tool Launcher")));
            drawingToolButton.setToolTipText(i18n.get(OMDrawingToolLauncher.class, "drawingToolButton", I18n.TOOLTIP, "Drawing Tool Launcher"));
            drawingToolButton.addActionListener(getActionListener());
            jtb.add(drawingToolButton);
        }
        return jtb;
    }

    /**
     * Get the ActionListener that triggers the LauncherPanel. Useful to have to
     * provide an alternative way to bring up the LauncherPanel.
     * 
     * @return ActionListener
     */
    public ActionListener getActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                MapHandler mh = (MapHandler) getBeanContext();
                Frame frame = null;
                if (mh != null) {
                    frame = (Frame) mh.get(java.awt.Frame.class);
                }

                // -1 will get size from pack(), and get location (initially) to
                // the middle of the screen if the WindowSupport doesn't have
                // memory of where it's been.
                getWindowSupport().displayInWindow(frame, -1, -1, -1, -1);
            }
        };
    }

    /**
     * Get the attributes that initialize the graphic.
     */
    public GraphicAttributes getDefaultGraphicAttributes() {
        return defaultGraphicAttributes;
    }

    /**
     * Set the attributes that initialize the graphic.
     */
    public void setDefaultGraphicAttributes(GraphicAttributes ga) {
        defaultGraphicAttributes = ga;
    }

    /**
     * Set the loaders with an Iterator containing EditToolLoaders.
     */
    public void setLoaders(Iterator<EditToolLoader> iterator) {
        loaders.clear();
        while (iterator.hasNext()) {
            addLoader((EditToolLoader) iterator.next());
        }
    }

    /**
     * Returns an iterator of LoaderHolders.
     */
    public Iterator<LoaderHolder> getLoaders() {
        return loaders.iterator();
    }

    public void addLoader(EditToolLoader etl) {
        if (etl != null) {
            String[] classNames = etl.getEditableClasses();
            for (int i = 0; i < classNames.length; i++) {
                loaders.add(new LoaderHolder(etl.getPrettyName(classNames[i]), etl));
            }
        }
    }

    public void removeLoader(EditToolLoader etl) {
        if (etl != null) {
            for (LoaderHolder lh : loaders) {
                if (lh.loader == etl) {
                    loaders.remove(lh);
                }
            }
        }
    }

    /**
     * PropertyChangeListener method, to listen for the OMDrawingTool's list of
     * loaders that may or may not change.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName() == OMDrawingTool.LoadersProperty) {
            Object obj = pce.getNewValue();
            if (obj instanceof Vector) {
                Vector<EditToolLoader> loaders = (Vector<EditToolLoader>) obj;
                setLoaders(loaders.iterator());
                resetGUI();
            }
        }
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        maxHorNumLoaderButtons = PropUtils.intFromProperties(props, prefix
                + HorizontalNumberOfLoaderButtonsProperty, maxHorNumLoaderButtons);
        useTextEditToolTitles = PropUtils.booleanFromProperties(props, prefix
                + UseLoaderTextProperty, useTextEditToolTitles);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + HorizontalNumberOfLoaderButtonsProperty, Integer.toString(maxHorNumLoaderButtons));
        props.put(prefix + UseLoaderTextProperty, new Boolean(useTextEditToolTitles).toString());
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        String internString = i18n.get(OMDrawingToolLauncher.class, HorizontalNumberOfLoaderButtonsProperty, I18n.TOOLTIP, "Number of loader buttons to place horizontally");
        props.put(HorizontalNumberOfLoaderButtonsProperty, internString);

        internString = i18n.get(OMDrawingToolLauncher.class, HorizontalNumberOfLoaderButtonsProperty, "# Horizontal Buttons");
        props.put(HorizontalNumberOfLoaderButtonsProperty + LabelEditorProperty, internString);

        internString = i18n.get(OMDrawingToolLauncher.class, UseLoaderTextProperty, I18n.TOOLTIP, "Use text popup for loader selection.");
        props.put(UseLoaderTextProperty, internString);

        internString = i18n.get(OMDrawingToolLauncher.class, UseLoaderTextProperty, "Use Text For Selection");
        props.put(UseLoaderTextProperty + LabelEditorProperty, internString);

        return props;
    }

    public static class LoaderHolder {
        public String prettyName;
        public EditToolLoader loader;

        public LoaderHolder(String pn, EditToolLoader etl) {
            prettyName = pn;
            loader = etl;
        }
    }
}