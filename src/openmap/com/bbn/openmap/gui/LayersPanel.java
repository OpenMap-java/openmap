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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayersPanel.java,v $
// $RCSfile: LayersPanel.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;

import javax.swing.*;
import javax.accessibility.*;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.*;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.LayerSupport;
import com.bbn.openmap.LayerHandler;

/**
 * The LayersPanel displays the list of layers that OpenMap can display.
 * The layer name is displayed accompanied by an on/off button and a
 * tool palette button. Pressing the on/off button will cause the the
 * map to display/remove the layer. Pressing the tool palette button 
 * will cause a window to be displayed containing widgets specific to 
 * that layer. <p>
 *
 * The order of the layers in the list reflects the order that the
 * layers are displayed on the map, with the bottom-most layer listed
 * on the panel underneath all the the other layers displayed on the
 * map.  The order of the layers is determined by their order in the
 * Layer[] passed in the setLayers method.  <p>
 *
 * The order can be changed by selecting a layer by clicking on the
 * layer's name (or on either of buttons), then clicking on one of
 * the four buttons on the left side of the panel.  The four buttons
 * signify, from top to bottom: Move the selected layer to the top;
 * Move the selected layer up one position; Move the selected layer
 * down one position; Move the selected layer to the bottom. <P>
 *
 * The LayersPanel can be used within a BeanContext.  If it is added
 * to a BeanConext, it will look for a LayerHandler to add itself to
 * as a LayerListener.  The LayersPanel can only listen to one
 * LayerHandler, so if more than one is found, only the last one found
 * will be used.  If another LayerHandler is added to the BeanContext
 * later, the new LayerHandler will be used.  The LayersPanel is also
 * considered to be a Tool, which will cause a button that will bring
 * up the LayersPanel to be automatically added to the ToolPanel if a
 * ToolPanel is part of the BeanContext.
 */
public class LayersPanel extends OMToolComponent
    implements Serializable, ActionListener, LayerListener 
{
    /** Action command for the layer order buttons. */
    public final static String LayerTopCmd = "LayerTopCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerBottomCmd = "LayerBottomCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerUpCmd = "LayerUpCmd";
    /** Action command for the layer order buttons. */
    public final static String LayerDownCmd = "LayerDownCmd";
    /** Action command removing a layer. */
    public final static String LayerRemoveCmd = "LayerRemoveCmd";
    /** Action command adding a layer. */
    public final static String LayerAddCmd = "LayerAddCmd";
    
    /** Default key for the LayersPanel Tool. */
    public final static String defaultKey = "layerspanel";

    // Images
    protected static transient URL urlup;
    protected static transient ImageIcon upgif;
    protected static transient URL urlupc;
    protected static transient ImageIcon upclickedgif;
    protected static transient URL urltop;
    protected static transient ImageIcon topgif;
    protected static transient URL urltopc;
    protected static transient ImageIcon topclickedgif;
    protected static transient URL urldown;
    protected static transient ImageIcon downgif;
    protected static transient URL urldownc;
    protected static transient ImageIcon downclickedgif;
    protected static transient URL urlbottom;
    protected static transient ImageIcon bottomgif;
    protected static transient URL urlbottomc;
    protected static transient ImageIcon bottomclickedgif;
    protected static transient URL urldelete;
    protected static transient ImageIcon deletegif;
    protected static transient URL urldeletec;
    protected static transient ImageIcon deleteclickedgif;
    protected static transient URL urladd;
    protected static transient ImageIcon addgif;
    protected static transient URL urladdc;
    protected static transient ImageIcon addclickedgif;

    /**
     * The LayerHandler to listen to for LayerEvents, and also to
     * notify if the layer order should change. 
     */
    protected transient LayerHandler layerHandler = null;
    /**
     * Panel that lets you dynamically add and configure layers.
     */
    protected transient LayerAddPanel layerAddPanel = null;
    /**
     * The components holding the layer name label, the on/off
     * indicator and on button, and the palette on/off indicator and
     * palette on button. 
     */
    protected transient LayerPane[] panes;
    /** The internal component that holds the panes. */
    protected transient JPanel panesPanel;
    /** The scroll pane to use for panes. */
    protected transient JScrollPane scrollPane;
    /** The Layer order adjustment button group. */
    protected transient ButtonGroup bg;
    /** The ActionListener that will bring up the LayersPanel. */
    protected ActionListener actionListener;
    /**
     * The frame used when the LayersPanel is used in an application.  
     */
    protected transient JFrame layersWindowFrame;
    /** The frame used when the LayersPanel is used in an applet. */
    protected transient JInternalFrame layersWindow;
    /** The set of buttons that control the layers. */
    protected JPanel buttonPanel = null;

    /**
     * Static default initializations.
     */
    static {
	urlup = LayersPanel.class.getResource("Up.gif");
	upgif = new ImageIcon(urlup, "Up");

	urlupc = LayersPanel.class.getResource("Up.gif");
	upclickedgif = new ImageIcon(urlupc, "Up (clicked)");

	urltop = LayersPanel.class.getResource("DoubleUp.gif");
	topgif = new ImageIcon(urltop, "Top");

	urltopc = LayersPanel.class.getResource("DoubleUp.gif");
	topclickedgif = new ImageIcon(urltopc, "Top (clicked)");

	urldown = LayersPanel.class.getResource("Down.gif");
	downgif = new ImageIcon(urldown, "Down");

	urldownc = LayersPanel.class.getResource("Down.gif");
	downclickedgif = new ImageIcon(urldownc, "Down (clicked)");

	urlbottom = LayersPanel.class.getResource("DoubleDown.gif");
	bottomgif = new ImageIcon(urlbottom, "Bottom");

	urlbottomc = LayersPanel.class.getResource("DoubleDown.gif");
	bottomclickedgif = new ImageIcon(urlbottomc, "Bottom (clicked)");

	urldelete = LayersPanel.class.getResource("DeleteLayer.gif");
	deletegif = new ImageIcon(urldelete, "Delete");

	urldeletec = LayersPanel.class.getResource("DeleteLayer.gif");
	deleteclickedgif = new ImageIcon(urldeletec, "Delete (clicked)");

	urladd = LayersPanel.class.getResource("AddLayer.gif");
	addgif = new ImageIcon(urladd, "Add");

	urladdc = LayersPanel.class.getResource("AddLayer.gif");
	addclickedgif = new ImageIcon(urladdc, "Add (clicked)");
    }

    /**
     * Construct the LayersPanel.
     *
     * @param lHandler the LayerHandler controlling the layers.
     */
    public LayersPanel() {
	super();
	setKey(defaultKey);
	// lay out all widgets horizontally
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	setAlignmentX(LEFT_ALIGNMENT);
	setAlignmentY(TOP_ALIGNMENT);

	// create layer mover buttons
	buttonPanel = createButtonPanel();
  	add(buttonPanel);
    }

    /**
     * Construct the LayersPanel.
     *
     * @param lHandler the LayerHandler controlling the layers.
     */
    public LayersPanel(LayerHandler lHandler) {
	this();
	setLayerHandler(lHandler);
    }

    /** 
     * Set the LayerHandler that the LayersPanel listens to.  If the
     * LayerHandler passed in is not null, the LayersMenu will be
     * added to the LayerHandler LayerListener list, and the
     * LayersMenu will receive a LayerEvent with the current
     * layers. <P>
     *
     * If there is a LayerHandler that is already being listened to,
     * then the LayersPanel will remove itself from current LayerHandler
     * as a LayerListener, before adding itself to the new LayerHandler. <P>
     *
     * Lastly, if the LayerHandler passed in is null, the LayersPanel
     * will disconnect itself from any LayerHandler currently held,
     * and reset itself with no layers.
     *
     * @param lh LayerHandler to listen to, and to use to reorder the
     * layers.  
     */
    public void setLayerHandler(LayerHandler lh) {
	if (layerHandler != null) {
	    layerHandler.removeLayerListener(this);
	}
	layerHandler = lh;
	if (layerHandler != null) {
	    layerHandler.addLayerListener(this);
	} else {
	    setLayers(new Layer[0]);
	}
	updateLayerPanes(layerHandler);
    }

    /** 
     * Get the LayerHandler that the LayersPanel listens to and uses
     * to reorder layers.
     * @return LayerHandler.
     */
    public LayerHandler getLayerHandler() {
	return layerHandler;
    }

    /**
     * Set the layerpanes with the given layerhandler
     * @param layerHandler The LayerHandler controlling the layers
     */
    protected void updateLayerPanes(LayerHandler layerHandler) {
	LayerPane[] panes = getPanes();
	for (int i=0; i<panes.length; i++) {
	    panes[i].setLayerHandler(layerHandler);
	}
    }

    protected JButton add = null;

    /**
     * Set the panel that brings up an interface to dynamically add
     * layers.
     */
    public void setLayerAddPanel(LayerAddPanel lap) {
	layerAddPanel = lap;

	if (layerAddPanel != null) {
	    add = new JButton(addgif);
	    //  	add.setPressedIcon(bottomclickedgif);
	    add.setActionCommand(LayerAddCmd);
	    add.setToolTipText("Add a layer");
	    add.addActionListener(this);
	    buttonPanel.add(add);
	} else if (add != null) {
	    buttonPanel.remove(add);
	}

    }

    /**
     * Get the panel interface to dynamically add layers.
     */
    public LayerAddPanel getLayerAddPanel() {
	return layerAddPanel;
    }

    /**
     * LayerListener interface method.  A list of layers will be
     * added, removed, or replaced based on on the type of LayerEvent.
     * The LayersPanel only reacts to LayerEvent.ALL events, to reset
     * the components in the LayersPanel.
     *
     * @param evt a LayerEvent.  
     */
    public void setLayers(LayerEvent evt) {
        Layer[] layers = evt.getLayers();
	int type = evt.getType();

	if (type==LayerEvent.ALL) {
	    Debug.message("layerspanel", "LayersPanel received layers update");
	    setLayers(layers);
	}
    }

    /** 
     * Tool interface method. The retrieval tool's interface. This
     * method creates a button that will bring up the LayersPanel.
     *
     * @return String The key for this tool.  
     */
    public Container getFace() {
	JButton layerButton = null;

	if (getUseAsTool()) {
	    layerButton = new JButton(new ImageIcon(OMToolSet.class.getResource("layers.gif"), "Layer Controls"));
	    layerButton.setBorderPainted(false);
	    layerButton.setToolTipText("Layer Controls");
	    layerButton.setMargin(new Insets(0,0,0,0));
	    layerButton.addActionListener(getActionListener());
	}

	return layerButton;
    }	
    
    /** 
     * Get the ActionListener that triggers the LayersPanel.  Useful
     * to have to provide an alternative way to bring up the
     * LayersPanel.  
     * 
     * @return ActionListener
     */
    public ActionListener getActionListener() {

	if (actionListener == null) {
	    // Try to group the applet-specific stuff in here...
	    if (Environment.getBoolean(Environment.UseInternalFrames)) {

		layersWindow = new JInternalFrame(
		    "Layers",
		    /*resizable*/ true,
		    /*closable*/ true,
		    /*maximizable*/ false,
		    /*iconifiable*/ true);
		layersWindow.setBounds(2, 2, 328, 300);
		layersWindow.setContentPane(this);
		layersWindow.setOpaque(true);
		try {
		    layersWindow.setClosed(true);//don't show until it's needed
		} catch (java.beans.PropertyVetoException e) {}
		
		actionListener = ( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			    try {
				if (layersWindow.isClosed()) {
				    layersWindow.setClosed(false);
				    // hmmm is this the best way to do this?

				    JLayeredPane desktop = 
					Environment.getInternalFrameDesktop();

				    if (desktop != null) {
					desktop.remove(layersWindow);
					desktop.add(layersWindow, 
						    JLayeredPane.PALETTE_LAYER);
					layersWindow.setVisible(true);
				    }
				}
			    } catch (java.beans.PropertyVetoException e) {
				System.err.println(e);
			    }
			}
		    });
		
	    } else { // Working as an application...
		layersWindowFrame = new JFrame("Layers");
		layersWindowFrame.setBounds(2, 2, 328, 300);
		layersWindowFrame.setContentPane(this);
		layersWindowFrame.setVisible(false);//don't show until it's needed
		
		actionListener = ( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			    layersWindowFrame.setVisible(true);
			    layersWindowFrame.setState(java.awt.Frame.NORMAL);
			}
		    });
	    }
	}

	return actionListener;
    }

    /**
     * Set the layers that are in the LayersPanel.  Make sure that the
     * layer[] is the same as that passed to any other OpenMap
     * component, like the LayersMenu.  This method checks to see if
     * the layer[] has actually changed, in order or in size.  If it
     * has, then createPanel() is called to rebuild the LayersPanel.
     *
     * @param inLayers the array of layers.  
     */
    public void setLayers(Layer[] inLayers) {
	Layer[] layers = inLayers;

	if (inLayers == null) {
	    layers = new Layer[0];
	}

	if (Debug.debugging("layerspanel")) {
	    Debug.output("LayersPanel.setLayers() with " + 
			 layers.length + " layers.");
	}

	LayerPane[] panes = getPanes();

	if (panes == null || panes.length != layers.length) {
	    // if the panel hasn't been created yet, or if someone has
	    // changed the layers on us, rebuild the panel.
	    createPanel(layers);
	    return;
	}

	for (int i = 0; i < layers.length; i++) {
	    if (panes[i].getLayer() != layers[i]) {
		// If the layer order sways at all, then we start over
		// and rebuild the panel
		createPanel(layers);
		return;
	    } else {
		panes[i].updateLayerLabel();
	    }

	    // Do this just in case someone has changed something
	    // somewhere else...
	    panes[i].setLayerOn(layers[i].isVisible());
	}
	//  If we get here, it means that what we had is what we
	//  wanted.
    }

    protected LayerPane[] getPanes() {
	return panes;
    }

    protected void setPanes(LayerPane[] lpa) {
	panes = lpa;
    }

    /**
     * Create the panel that shows the LayerPanes.  This method
     * creates the on/off buttons, palette buttons, and layer labels,
     * and adds them to the scrollPane used to display all the layers.
     *
     * @param inLayers the Layer[] that reflects all possible layers
     * that can be added to the map.
     */
    public void createPanel(Layer[] inLayers) {
	Debug.message("layerspanel", "LayersPanel.createPanel()");

	if (scrollPane != null) {
	    remove(scrollPane);
	}

	Layer[] layers = inLayers;
	if (layers == null) {
	    layers = new Layer[0];
	}

	if (panesPanel == null) {
	    panesPanel = new JPanel();
	    panesPanel.setLayout(new BoxLayout(panesPanel, BoxLayout.Y_AXIS));
	    panesPanel.setAlignmentX(LEFT_ALIGNMENT);
	    panesPanel.setAlignmentY(BOTTOM_ALIGNMENT);
	} else {
	    ((BoxLayout)panesPanel.getLayout()).invalidateLayout(panesPanel);
	    panesPanel.removeAll();
	}

	deletePanes(getPanes());

	ButtonGroup tmpbg = new ButtonGroup();
	LayerPane[] panes = new LayerPane[layers.length];

	// populate the arrays of CheckBoxes and strings used to fill
	// the JPanel for the panes
	for (int i = 0; i < layers.length; i++) {
	    if (layers[i] == null) {
		Debug.output("LayersPanel caught null layer, " + i +
			     " out of " + layers.length);
		continue;
	    }
	    panes[i] = new LayerPane(layers[i], layerHandler, tmpbg);
	    panesPanel.add(panes[i]);
        }
	
	setPanes(panes);
	bg = tmpbg;

	if (scrollPane != null) {
	    remove(scrollPane);
	    scrollPane.removeAll();
	    scrollPane = null;
	}

	scrollPane = new JScrollPane(
	    panesPanel, 
	    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	add(scrollPane);
	revalidate();
    }

    public void deletePanes(LayerPane[] dpanes) {

	Debug.message("layerspanel", "LayersPanel.deletePanes()");
	if (dpanes != null) {
	    for (int i = 0; i < dpanes.length; i++) {
		if (dpanes[i] != null) {
		    dpanes[i].cleanup();
		}
		dpanes[i] = null;
	    }
	}

	// Shouldn't call this, but it's the only thing
	// that seems to make it work...
	if (Debug.debugging("helpgc")) {
	    System.gc();
	}
    }

    /**
     * Set up the buttons used to move layers up and down.
     *
     * @return JPanel containing the layer order control buttons.
     */
    protected JPanel createButtonPanel() {

  	JPanel buttonPanel = new JPanel();
  	buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
  	buttonPanel.setAlignmentY(CENTER_ALIGNMENT);
  	buttonPanel.setLayout( 
  	    new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

  	JButton top = new JButton(topgif);
	top.setActionCommand(LayerTopCmd);
	top.setPressedIcon(topclickedgif);
	top.setToolTipText("Move selected layer to top");
	top.addActionListener(this);
  	buttonPanel.add(top);

  	JButton up = new JButton(upgif);
	up.setActionCommand(LayerUpCmd);
	up.setPressedIcon(upclickedgif);
	up.setToolTipText("Move selected layer up one");
	up.addActionListener(this);
  	buttonPanel.add(up);

  	JButton down = new JButton(downgif);
	down.setPressedIcon(downclickedgif);
	down.setActionCommand(LayerDownCmd);
	down.setToolTipText("Move selected layer down one");
	down.addActionListener(this);
  	buttonPanel.add(down);

  	JButton bottom = new JButton(bottomgif);
	bottom.setPressedIcon(bottomclickedgif);
	bottom.setActionCommand(LayerBottomCmd);
	bottom.setToolTipText("Move selected layer to bottom");
	bottom.addActionListener(this);
  	buttonPanel.add(bottom);

	if (canDeleteLayers()) {
	    JLabel blank = new JLabel(" ");
	    buttonPanel.add(blank);
	    
	    JButton delete = new JButton(deletegif);
//  	    delete.setPressedIcon(bottomclickedgif);
	    delete.setActionCommand(LayerRemoveCmd);
	    delete.setToolTipText("Remove selected layer");
	    delete.addActionListener(this);
	    buttonPanel.add(delete);
	}

	return buttonPanel;
    }

    /**
     * This can be made into a variable that could get set in
     * properties but that would mean we would have to turn the
     * LayersPanel into a PropertyConsumer.  For now, if you don't
     * want users deleting layers, just extend this class and return
     * false, and then add your class to the properties file or to the
     * MapHandler.
     */
    public boolean canDeleteLayers() {
	return true;
    }

    /**
     * Method associated with the ActionListener interface.  This
     * method listens for action events meant to change the order of
     * the layers, as fired by the layer order buttons.
     *
     * @param e ActionEvent 
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {

	String command = e.getActionCommand();

	if (Debug.debugging("layerspanel")) {
	    Debug.output("LayersPanel.actionPerformed(): " + command);
	}

	LayerPane lp = null;
	int row = 0;

	LayerPane[] panes = getPanes();
	LayerPane[] tempPanes = null;

	if (command.equals(LayerTopCmd)) {
	    // Move layer selected layer to top
	    for (int i=0; i < panes.length; i++) {
		if (panes[i].isSelected()) {
		    lp = panes[i];
		    break;
		}
		row++;
	    }

	    if (lp == null || row == 0) return;

	    tempPanes = new LayerPane[panes.length];
	    tempPanes[0] = lp;
	    if (Debug.debugging("layerspanel")) {
		Debug.output("LayersPanel - Moving row to top: " + 
			     lp.getLayer().getName());
	    }

	    int j;
	    for (int i=1; i < panes.length; i++) {
		if (i <= row)
		    tempPanes[i] = panes[i-1];
		else 
		    tempPanes[i] = panes[i];
	    }
	    setPanes(tempPanes);
	    rejiggerMapLayers();

	} else if (command.equals(LayerBottomCmd)) {
	    // Move layer selected layer to bottom
	    for (int i=0; i < panes.length; i++) {
		if (panes[i].isSelected()) {
		    lp = panes[i];
		    break;
		}
		row++;
	    }

	    if (lp == null || row == panes.length -1) return;

	    tempPanes = new LayerPane[panes.length];
	    tempPanes[panes.length - 1] = lp;

	    int j;
	    for (int i=0; i < panes.length-1; i++) {
		if (i < row)
		    tempPanes[i] = panes[i];
		else
		    tempPanes[i] = panes[i+1];
	    }
	    // now reset the global arrays
	    setPanes(tempPanes);
	    rejiggerMapLayers();

	} else if (command.equals(LayerUpCmd)) {
	    // Move layer selected layer up one
	    for (int i=0; i < panes.length; i++) {
		if (panes[i].isSelected()) {
		    lp = panes[i];
		    break;
		}
		row++;
	    }

	    if (lp == null || row == 0)	return;

	    panes[row] = panes[row-1];
	    panes[row-1] = lp;
	    rejiggerMapLayers();

	} else if (command.equals(LayerDownCmd)) {
	    // Move layer selected layer up one
	    for (int i=0; i < panes.length; i++) {
		if (panes[i].isSelected()) {
		    lp = panes[i];
		    break;
		}
		row++;
	    }

	    if (lp == null || row == panes.length - 1) return;

	    panes[row] = panes[row+1];
	    panes[row+1] = lp;
	    rejiggerMapLayers();
	} else if (command.equals(LayerRemoveCmd)) {
	    if (layerHandler == null) {
		return;
	    }

	    for (int i=0; i < panes.length; i++) {
		if (panes[i].isSelected()) {
		    panes[i].layer.setPaletteVisible(false);
		    layerHandler.removeLayer(i);

		    // Shouldn't call this, but it's the only thing
		    // that seems to make it work...
		    if (Debug.debugging("helpgc")) {
			System.gc();
		    }

		    return;
		}
	    }

	    // OK, here's a hidden trick. If no layers are selected
	    // and the minus sign is clicked, then this is called.
	    System.gc();

	} else if (command.equals(LayerAddCmd)) {
	    if (layerAddPanel != null) {
		layerAddPanel.showPanel();
	    }
	}
    }

    /**
     * Makes a new layer cake of active layers to send to
     * LayerHandler.setLayers().
     *
     * @param neworder tells whether the order of the layers has
     * changed.
     * @param selectedRow the currently selected layer in the panel,
     * used to reset the scrollPane so that the row is visible (set to
     * -1 if unknown).  
     */
    protected void rejiggerMapLayers() {
	Debug.message("layerspanel", "LayersPanel.rejiggerMapLayers()");

	if (layerHandler == null) {
	    // Why bother doing anything??
	    return;
	}

	int selectedRow = -1;

	LayerPane[] panes = getPanes();

	Layer[] newLayers = new Layer[panes.length];
	for (int i = 0; i < panes.length; i++) {
	    newLayers[i] = panes[i].getLayer();
	    if (panes[i].isSelected()) {
		selectedRow = i;
	    }
	}

	panesPanel.removeAll();
	for (int i=0; i< panes.length; i++) {
	    panesPanel.add(panes[i]);
	}
	scrollPane.validate();
	
	// Scroll up or down as necessary to keep selected row viewable
	if (selectedRow >= 0) {
	    int spheight = scrollPane.getHeight();
	    JScrollBar sb = scrollPane.getVerticalScrollBar();
	    int sv = sb.getValue();
	    int paneheight = panes[selectedRow].getHeight();
	    int rowvalue = selectedRow*paneheight;
	    // Don't reset scrollBar unless the selected row
	    // is not in the viewable range
	    if (!((rowvalue > sv) && (rowvalue < spheight+sv))) {
		sb.setValue(rowvalue);
	    }
	}
	layerHandler.setLayers(newLayers);
    }

    /** 
     * Update the layer names - if a layer name has changed, tell the
     * LayerPanes to check with their layers to update their labels. 
     */
    public synchronized void updateLayerLabels() {
	LayerPane[] panes = getPanes();
	for (int i = 0; i < panes.length; i++) {
	    panes[i].updateLayerLabel();
	}
    }

    /**
     * Called when the LayersPanel is added the BeanContext, or when
     * another object is added to the BeanContext after the
     * LayerHandler has been added.  This allows the LayersPanel to
     * keep up-to-date with any objects that it may be interested in,
     * namely, the LayerHandler.  If a LayerHandler has already been
     * added, the new LayerHandler will replace it.
     *
     * @param it Iterator to use to go through the objects added to
     * the BeanContext.  
     */
    public void findAndInit(Object someObj) {
	if(someObj instanceof LayerHandler) {
	    // do the initializing that need to be done here
	    Debug.message("layerspanel","LayersPanel found a LayerHandler");
	    setLayerHandler((LayerHandler)someObj);
	}
	if (someObj instanceof LayerAddPanel) {
	    setLayerAddPanel((LayerAddPanel)someObj);
	}
    }

    /** 
     * BeanContextMembershipListener method.  Called when an object
     * has been removed from the parent BeanContext.  If a
     * LayerHandler is removed, and it's the current one being
     * listened to, then the layers in the panel will be wiped clean.
     *
     * @param bcme event that provides an iterator to use for the
     * removed objects.  
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof LayerHandler) {
	    // do the initializing that need to be done here
	    Debug.message("layerspanel","LayersPanel removing LayerHandler");
	    if (getLayerHandler() == (LayerHandler) someObj) {
		setLayerHandler(null);
	    }
	}
	if (someObj instanceof LayerAddPanel) {
	    if (getLayerAddPanel() == someObj) {
		setLayerAddPanel(null);
	    }
	}
    }
}

