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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayerStatusPane.java,v $
// $RCSfile: LayerStatusPane.java,v $
// $Revision: 1.2 $
// $Date: 2003/04/15 16:16:50 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.event.*;
import javax.accessibility.*;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.LayerStatusListener;
import com.bbn.openmap.util.Debug;

/** 
 * A LayerStatusPane is an extension to the LayerPane, where it is a
 * listener to the Layer and shows the layer status in the GUI.
 */
public class LayerStatusPane extends LayerPane 
    implements LayerStatusListener {

    // the icons
    protected static transient ImageIcon layerWorking;
    protected static transient ImageIcon layerDone;

    public transient final static String showPaletteCmd = "showPalette";
    public transient final static String toggleLayerCmd = "toggleLayerCmd";

    protected Color offColor;
    protected Color onColor = new Color(0xFF0066CC);

    // default initializations, extended from LayerPane...
    static {
	URL working = LayerStatusPane.class.getResource("BulbWorking.gif");
	layerWorking = new ImageIcon(working, "layer working");
	URL done  = LayerStatusPane.class.getResource("BulbDone.gif");
	layerDone = new ImageIcon(done, "layer displayed");
    }

    /**
     *  @param layer the layer to be represented by the pane.
     *  @param cake the list of layers currently displayed on the map.
     *  @param layerHandler the LayerHandler that contains information
     *  about the Layers. 
     */
    public LayerStatusPane(Layer layer, LayerHandler layerHandler, ButtonGroup bg) {
	super(layer, layerHandler, bg);
	getLayer().addLayerStatusListener(this);
    }

    protected LayerStatusPane(String title) {
	super(title);
    }

    /**
     * Update the Layer status. LayerStatusListener interface method.
     * @param evt LayerStatusEvent
     */
    public void updateLayerStatus(LayerStatusEvent evt) {
	switch (evt.getStatus()) {
	case LayerStatusEvent.START_WORKING:
	    onoffButton.setSelectedIcon(layerWorking);
	    break;
	case LayerStatusEvent.FINISH_WORKING:
	    onoffButton.setSelectedIcon(layerDone);
	    break;
	}
    }

    public void cleanup() {
	Layer l = getLayer();
	if (l != null) {
	    l.removeLayerStatusListener(this);
	}
	super.cleanup();
    }
}
