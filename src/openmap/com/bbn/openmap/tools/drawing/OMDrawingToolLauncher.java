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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMDrawingToolLauncher.java,v $
// $RCSfile: OMDrawingToolLauncher.java,v $
// $Revision: 1.7 $
// $Date: 2003/10/07 15:39:41 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.Environment;
import com.bbn.openmap.gui.OMToolComponent;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MapHandlerChild;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;
import javax.swing.*;

/**
 * This tool is a widget that calls the OMDrawingTool to create a
 * specific graphic.  The launcher is completely configured by
 * EditToolLaunchers and OMGraphicHandlers that it finds in a
 * MapHandler.  There are no methods to manually add stuff to this
 * GUI.
 */
public class OMDrawingToolLauncher extends OMToolComponent implements ActionListener, PropertyChangeListener {

    protected DrawingTool drawingTool;
    protected boolean useTextEditToolTitles = false;
    protected GraphicAttributes defaultGraphicAttributes = new GraphicAttributes();

    protected TreeMap loaders = new TreeMap();
    protected Vector drawingToolRequestors = new Vector();

    protected DrawingToolRequestor currentRequestor;
    protected String currentCreation;
    protected JComboBox requestors;

    String[] rtc = { "Lat/Lon", "X/Y", "X/Y Offset" };
    public final static String CreateCmd = "CREATE";

    /** Default key for the DrawingToolLauncher Tool. */
    public static final String defaultKey = "omdrawingtoollauncher";

    public OMDrawingToolLauncher() {
	super();
	setWindowSupport(new WindowSupport(this, "Drawing Tool Launcher"));
	setKey(defaultKey);
	defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	defaultGraphicAttributes.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
	resetGUI();
    }

    /**
     * Set the DrawingTool for this launcher.
     */
    public void setDrawingTool(DrawingTool dt) {
	if (drawingTool != null && drawingTool instanceof OMDrawingTool) {
	    ((OMDrawingTool)drawingTool).removePropertyChangeListener(this);
	}

	drawingTool = dt;

	if (drawingTool != null && drawingTool instanceof OMDrawingTool) {
	    ((OMDrawingTool)drawingTool).addPropertyChangeListener(this);
	}
    }

    public DrawingTool getDrawingTool() {
	return drawingTool;
    }

    public void actionPerformed(ActionEvent ae) {
    	String command = ae.getActionCommand().intern();
	
	Debug.message("drawingtool", 
		      "DrawingToolLauncher.actionPerformed(): " + command);

	Object source = ae.getSource();

	//  This is important.  We need to set the current projection
	//  before setting the projection in the MapBean.  That way,
	//  the projectionChanged method actions won't get fired
	if (command == CreateCmd) {
	    // Get the active EditToolLoader
	    DrawingTool dt = getDrawingTool();

	    if (dt instanceof OMDrawingTool) {
		OMDrawingTool omdt = (OMDrawingTool) dt;

		if (omdt.isActivated()) {
		    omdt.deactivate();
		}
	    }

	    if (dt != null && currentCreation != null && 
		currentRequestor != null) {
		// Copy the default GraphicAttributes into another copy...
		GraphicAttributes ga = (GraphicAttributes)defaultGraphicAttributes.clone();
		
		// fire it up!
		dt.setBehaviorMask(OMDrawingTool.DEFAULT_BEHAVIOR_MASK);
		dt.create(currentCreation, ga, currentRequestor);
	    } else {

		StringBuffer sb = new StringBuffer();
		StringBuffer em = new StringBuffer();

		if (dt == null) {
		    sb.append("   No drawing tool is available!\n");
		    em.append("   No drawing tool is available!\n");
		} else {
		    sb.append("   Drawing tool OK.\n");
		}

		if (currentCreation == null) {
		    sb.append("   No valid choice of graphic to create.\n");
		    em.append("   No valid choice of graphic to create.\n");
		} else {
		    sb.append("   Graphic choice OK.\n");
		}

		if (currentRequestor == null) {
		    sb.append("   No valid receiver for the created graphic.\n");
		    em.append("   No valid receiver for the created graphic.\n");
		} else {
		    sb.append("   Graphic receiver OK.\n");
		}

		Debug.output("OMDrawingToolLauncher: Something is not set:\n" +
			     sb.toString());

		MapHandler mapHandler = (MapHandler)getBeanContext();
		if (mapHandler != null) {
		    InformationDelegator id = (InformationDelegator)mapHandler.get("com.bbn.openmap.InformationDelegator");
		    if (id != null) {
			id.displayMessage("Problem", "Problem creating new graphic:\n" + em.toString());
		    }
		}
	    }
	}
    }

    /**
     * Set the current requestor to receive a requested OMGraphic.
     * Changes are reflected in the GUI, and setCurrentRequestor()
     * will eventually be called.
     */
    public void setRequestor(String aName) {
	if (requestors != null) {
	    requestors.setSelectedItem(aName);
	}
    }

    /**
     *  Build the stuff that goes in the launcher.
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
	    requestorNames[i] = ((DrawingToolRequestor)drawingToolRequestors.elementAt(i)).getName();
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
		    String currentChoice = (String)jcb.getSelectedItem();
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

	JPanel panel = PaletteHelper.createPaletteJPanel("Send To:");
	panel.add(requestors);
	palette.add(panel);
	    
	if (Debug.debugging("omdtl")) {
	    Debug.output("Figuring out tools, using names");
	}
	
	panel = PaletteHelper.createPaletteJPanel("Graphic Type:");
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
		    String currentChoice = (String)jcb.getSelectedItem();
		    if (currentChoice == rtc[2]) {
			defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
		    } else if (currentChoice == rtc[1]) {
			defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_XY);
		    } else {
			defaultGraphicAttributes.setRenderType(OMGraphic.RENDERTYPE_LATLON);
		    }		    
		}
	    });

	renderTypeList.setSelectedIndex(defaultGraphicAttributes.getRenderType() - 1);
	
	panel = PaletteHelper.createVerticalPanel("Graphic Attributes:");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	JPanel panel2 = PaletteHelper.createVerticalPanel("Rendering Type:");
	panel2.add(renderTypeList);
	JPanel panel3 = PaletteHelper.createVerticalPanel("Line Types and Colors:");
	panel3.add(defaultGraphicAttributes.getGUI());
	panel.add(panel2);
	panel.add(panel3);
	palette.add(panel);

	JButton createButton = new JButton("Create Graphic");
	createButton.setActionCommand(CreateCmd);
	createButton.addActionListener(this);

	JPanel dismissBox = new JPanel();
	JButton dismiss = new JButton("Close");
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

    protected JComponent getToolWidgets(boolean useText) {	

	// Set editables with all the pretty names.
	Vector editables = new Vector();
	Iterator graphicNames = loaders.keySet().iterator();
	while (graphicNames.hasNext()) {
	    String graphicName = (String) graphicNames.next();
	    editables.add(graphicName);
	}

	if (useText) {
	    return createToolOptionMenu(editables);
	} else {
	    return createToolButtonPanel(editables);
	}
    }

    private JComboBox createToolOptionMenu(Vector editables) {
	String[] toolNames = new String[editables.size()];
	for (int i = 0; i < toolNames.length; i++) {
	    toolNames[i] = (String)editables.elementAt(i);
	    if (Debug.debugging("omdtl")) {
		Debug.output("Adding TOOL " + toolNames[i] + " to menu");
	    }
	}
	    
	JComboBox tools = new JComboBox(toolNames);
	tools.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JComboBox jcb = (JComboBox) e.getSource();
		    String currentChoice = (String)jcb.getSelectedItem();
		    setCurrentCreation(currentChoice);
		}
	    });
	    
	if (toolNames.length > 0) {
	    tools.setSelectedIndex(0);
	}
	    
	return tools;
    }

    private JToolBar createToolButtonPanel(Vector editables) {
	// Otherwise, create a set of buttons.
	JToggleButton btn;
	JToolBar iconBar = new JToolBar();
	iconBar.setFloatable(false);

	ButtonGroup bg = new ButtonGroup();

	for (int i = 0; i < editables.size(); i++) {
	    String pName = (String)editables.elementAt(i);
	    EditToolLoader etl = (EditToolLoader)loaders.get(pName);
	    ImageIcon icon = etl.getIcon(getEditableClassName(pName));
	    btn = new JToggleButton(icon, i==0);
	    btn.setToolTipText(pName);
	    btn.setActionCommand(pName);
	    btn.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			setCurrentCreation(ae.getActionCommand());
		    }
		});
	    bg.add(btn);

	    iconBar.add(btn);

	    // Just set one as active, the first one.
	    if (i == 0) {
		setCurrentCreation(pName);
	    }
	}

	return iconBar;
    }

    /**
     * Set the component that will receive the new/edited OMGraphic
     * from the DrawingTool.  Does not change the GUI.  Called when
     * the combo box changes.
     * @param name GUI pretty name of requestor.
     */
    public void setCurrentRequestor(String name) {
	Enumeration objs = drawingToolRequestors.elements();
	while (objs.hasMoreElements()) {
	    DrawingToolRequestor dtr = 
		(DrawingToolRequestor) objs.nextElement();
	    if (name.equals(dtr.getName())) {
		currentRequestor = dtr;
		return;
	    }
	}
	currentRequestor = null;
    }

    /**
     * Set the next thing to be created to be whatever the pretty name
     * represents.  Sets currentCreation.
     * @param name GUI pretty name of thing to be created, from one of
     * the EditToolLoaders.
     */
    public void setCurrentCreation(String name) {
	currentCreation = getEditableClassName(name);
    }

    /**
     * Given a pretty name, look through the EditToolLoaders and find
     * out the classname that goes with editing it.
     * @param prettyName GUI pretty name of tool, or thing to be
     * created, from one of the EditToolLoaders.
     */
    public String getEditableClassName(String prettyName) {
	Iterator objs = getLoaders();
	while (objs.hasNext()) {
	    EditToolLoader etl = (EditToolLoader) objs.next();
	    String[] ec = etl.getEditableClasses();
	    for (int i = 0; i < ec.length; i++) {
		if (prettyName.equals(etl.getPrettyName(ec[i]))) {
		    return ec[i];
		}
	    }
	}
	return null;
    }

    /**
     * This is the method that your object can use to find other
     * objects within the MapHandler (BeanContext).  This method gets
     * called when the object gets added to the MapHandler, or when
     * another object gets added to the MapHandler after the object is
     * a member.  
     *
     * @param it Iterator to use to go through a list of objects.
     * Find the ones you need, and hook yourself up.
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof OMDrawingTool){
	    Debug.message("omdtl","OMDrawingToolLauncher found a DrawingTool.");
	    setDrawingTool((DrawingTool)someObj);
	}
	if (someObj instanceof DrawingToolRequestor) {
	    if (Debug.debugging("omdtl")) {
		Debug.output("OMDrawingToolLauncher found a DrawingToolRequestor - " + ((DrawingToolRequestor)someObj).getName());
	    }
	    drawingToolRequestors.add(someObj);
	    resetGUI();
	}
    }

    /**
     * BeanContextMembershipListener method.  Called when a new object
     * is removed from the BeanContext of this object.  For the Layer,
     * this method doesn't do anything.  If your layer does something
     * with the childrenAdded method, or findAndInit, you should take
     * steps in this method to unhook the layer from the object used
     * in those methods.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof OMDrawingTool){
	    Debug.message("omdtl","OMDrawingToolLauncher found a DrawingTool.");
	    OMDrawingTool dt = (OMDrawingTool) someObj;
	    if (dt == getDrawingTool()) {
		setDrawingTool(null);
		dt.removePropertyChangeListener(this);
	    }
	}
	if (someObj instanceof DrawingToolRequestor) {
	    if (Debug.debugging("omdtl")) {
		Debug.output("OMDrawingToolLauncher removing a DrawingToolRequestor - " + ((DrawingToolRequestor)someObj).getName());
	    }
	    drawingToolRequestors.remove(someObj);
	    resetGUI();
	}
    }

    /** 
     * Tool interface method. The retrieval tool's interface. This
     * method creates a button that will bring up the LauncherPanel.
     *
     * @return String The key for this tool.  
     */
    public Container getFace() {
	JButton drawingToolButton = null;
	if (getUseAsTool()) {
	    drawingToolButton = new JButton(new ImageIcon(OMDrawingToolLauncher.class.getResource("launcher.gif"), "Drawing Tool Launcher"));
	    drawingToolButton.setBorderPainted(false);
	    drawingToolButton.setToolTipText("Drawing Tool Launcher");
	    drawingToolButton.setMargin(new Insets(0,0,0,0));
	    drawingToolButton.addActionListener(getActionListener());
	}
	return drawingToolButton;
    }	

    /** 
     * Get the ActionListener that triggers the LauncherPanel.  Useful
     * to have to provide an alternative way to bring up the
     * LauncherPanel.  
     * 
     * @return ActionListener
     */
    public ActionListener getActionListener() {
	return new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		    // -1 will get size from pack();
		    int w = -1;
		    int h = -1;
		    int x = 10;
		    int y = 10;
	    
		    Point loc = getWindowSupport().getComponentLocation();
		    if (loc != null) {
			x = (int) loc.getX();
			y = (int) loc.getY();
		    }

		    MapHandler mh = (MapHandler) getBeanContext();
		    Frame frame = null;
		    if (mh != null) {
			frame = (Frame)mh.get(java.awt.Frame.class);
		    }

		    getWindowSupport().displayInWindow(frame, x, y, w, h);
		}
	    };
    }

    /**
     * Get the attributes that initalize the graphic.
     */
    public GraphicAttributes getDefaultGraphicAttributes() {
	return defaultGraphicAttributes;
    }

    /**
     * Set the attributes that initalize the graphic.
     */
    public void setDefaultGraphicAttributes(GraphicAttributes ga) {
	defaultGraphicAttributes = ga;
    }

    public void setLoaders(Iterator iterator) {
	loaders.clear();
	while (iterator.hasNext()) {
	    addLoader((EditToolLoader)iterator.next());
	}
    }

    public Iterator getLoaders() {
	return loaders.values().iterator();
    }

    public void addLoader(EditToolLoader etl) {
	String[] classNames = etl.getEditableClasses();
	for (int i = 0; i < classNames.length; i++) {
	    loaders.put(etl.getPrettyName(classNames[i]), etl);
	}
    }

    public void removeLoader(EditToolLoader etl) {
	loaders.remove(etl);
    }

    /**
     * PropertyChangeListener method, to listen for the
     * OMDrawingTool's list of loaders that may or may not change.
     */
    public void propertyChange(PropertyChangeEvent pce) {
	if (pce.getPropertyName() == OMDrawingTool.LoadersProperty) {
	    setLoaders(((Hashtable)pce.getNewValue()).values().iterator());
	    resetGUI();
	}
    }
}
