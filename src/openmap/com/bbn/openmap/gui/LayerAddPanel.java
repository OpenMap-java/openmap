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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayerAddPanel.java,v $
// $RCSfile: LayerAddPanel.java,v $
// $Revision: 1.3 $
// $Date: 2003/06/02 18:24:58 $
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
import java.util.*;

import javax.swing.*;
import javax.accessibility.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.LayerSupport;
import com.bbn.openmap.plugin.*;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.*;

/**
 * Class to interactively add a Layer to the map.  A LayerAddPanel
 * utilizes the bean context mechanisms to keep up to date about the
 * applications LayerHandler and PropertyHandler (see findAndInit
 * method).  A property is used to determine objects of which Layer
 * subclasses can be instantiated (see static String layerTypes), for
 * configuration of a layer-to-be an Inspector is invoked to inspect
 * and configure a Layer object through the PropertyConsumer
 * interface.  
 */
public class LayerAddPanel extends OMComponentPanel
    implements Serializable, ActionListener {
    /**
     * Constant field containing markers used in properties file for
     * layers that can be created using the LayerAddPanel.
     */
    public final static String layerTypes = "addableLayers";

    /**
     * Holds the PropertyHandler.
     */
    protected PropertyHandler propertyHandler = null;
    /**
     * Holds the LayerHandler.
     */
    protected LayerHandler layerHandler = null;
    /**
     * The list of available Layer classes.  Is initiated with pretty
     * names.
     */
    protected JComboBox list = null;
    /** The String to use as a prefix for the new Layer's properties. */
    protected JTextField prefixTextField = null;
    /** Action command String for JButton. */
    protected final String configureActionCommand = "configureActionCommand";
    /** Contains Layer classes to be instantiated.  */
    protected Hashtable layerClasses = null;
    /** The Inspector to handle the configuration of the new Layer. */
    protected Inspector inspector = null;
    /** The layer to configure and add. */
    protected Object layer; 
    
    /** 
     * Creates the LayerPanel.
     */
    public LayerAddPanel() {
	super();
	inspector = new Inspector();
	inspector.addActionListener((ActionListener)this);
	setWindowSupport(new WindowSupport(this, "Add Layer"));
    }
    
    /** 
     * Creates the LayerPanel.  
     * @param l the LayerHandler controlling the layers.
     */
    public LayerAddPanel(PropertyHandler p, LayerHandler l) {
	this();
	propertyHandler = p;
	layerHandler = l;
    }
    
    public void createLayerClasses(Layer[] layers) {
	if (layerClasses != null) {
	    layerClasses.clear();
	} else {
	    layerClasses = new Hashtable();
	}

	for (int i = 0; i < layers.length; i++) {
	    layerClasses.put(layers[i].getName(), 
			     layers[i].getClass().getName());
	}
    }

    /**
     * Produces a dialog panel to add a Layer, with the layers
     * given. 
     */
    public void createPanel(Layer[] layers) {
	createLayerClasses(layers);
	createPanel();
    }

    public final static String DefaultLayerName = "Layer Name";

    /**
     * Produces a dialog panel to add a layer.  If the layers haven't
     * been manually added through createPanel(layers), then the
     * PropertyHandler is consulted and the layer list is built from
     * the layerTypes property.
     */
    public void createPanel() {
	removeAll();
	JButton configureButton = new JButton("Configure");
	configureButton.addActionListener(this);
	configureButton.setActionCommand(configureActionCommand);

	prefixTextField = new JTextField(DefaultLayerName, 12);

	if (layerClasses == null) {
	    layerClasses = getLayerTypes();
	}

	Object[] layerTypes = layerClasses.keySet().toArray();

	if (layerTypes.length == 0) {
	    add(new JLabel("No Layers available for creation."));
	} else {
	    list = new JComboBox(layerTypes);
	    add(list);
	    add(prefixTextField);
	    add(configureButton);
	}
	invalidate();
    }
    
    /** 
     * Gets Layer information from PropertyHandler.  These layers are
     * defined in the application properties under the
     * openmap.layerTypes property.
     *
     * @return Hashtable of prettyName String keys with classname
     * values. Empty Hashtable if no layers are available.
     */
    protected Hashtable getLayerTypes() {
	Hashtable layerHash = new Hashtable();

	if (propertyHandler == null) {
	    return layerHash;
	}

	Properties props = propertyHandler.getProperties();
	String prefix = Environment.OpenMapPrefix;

	Vector typeList = PropUtils.parseSpacedMarkers(props.getProperty(prefix+"."+layerTypes));

	if (typeList == null) {
	    return layerHash;
	}

	// System.out.println("layerTypes:"+typeList+"."); 
	// debug info: available layers
	int unNamedCount = 1;
	for (int i=0; i<typeList.size(); ++i) {
	    String className = props.getProperty(typeList.get(i)+".class");
	    //System.out.println("looking for "+className+".");
	    //  		Object obj = Class.forName(className);
	    String prettyName = props.getProperty(typeList.get(i)+".prettyName");
	    if (prettyName == null) {
		prettyName = "Layer " + (unNamedCount++);
	    }
	    
	    if (className != null) {
		layerHash.put(prettyName, className);
	    }
	}
	return layerHash;
    }
    
    /**
     * Method associated with the ActionListener interface. 
     */
    public void actionPerformed(ActionEvent e) {
	
	if (e.getActionCommand()==configureActionCommand) {
	    // instanciate a default instance of the chosen layer
	    // and bring up the Inspector to configure it
	    String prettyName = (String)list.getSelectedItem();
	    String prefix = prefixTextField.getText().trim();

	    if (prettyName == null) {
		return;
	    }

	    String newClassName = (String)layerClasses.get(prettyName);

	    layer = ComponentFactory.create(newClassName, null, null);

	    if (layer instanceof PropertyConsumer) {

		if (layer instanceof PlugIn) {
		    PlugInLayer pil = new PlugInLayer();
		    pil.setPlugIn((PlugIn)layer);
		    pil.setName(prefix);
		    layer = pil;
		}

		if (layer instanceof Layer) {
		    // Set the pretty name to what the user chose.
		    ((Layer)layer).setName(prefix);
		}

		// Set the prefix to a modified version of the pretty name.
		prefix = propertyHandler.getUniquePrefix(prefix);

		((PropertyConsumer)layer).setPropertyPrefix(prefix);

		inspector.inspectPropertyConsumer((PropertyConsumer)layer);

	    }
	} else if (e.getActionCommand() == inspector.doneCommand) {
	    // the confirmation button of the Inspector panel was pressed
	    // find the beancontext and add the layer at hand (var. layer)
	    if (layer != null && layerHandler != null) {
		if (layer instanceof Layer) {
		    // Let's add it on top, so the user can find it
		    // easier, instead of adding it to the bottom and
		    // having it lost behind some other layers.
		    layerHandler.addLayer((Layer)layer, 0);
		} else if (layer instanceof PlugIn) {
		    PlugInLayer pil = (PlugInLayer)((PlugIn)layer).getComponent();
		    layerHandler.addLayer(pil, 0);
		}
		prefixTextField.setText(DefaultLayerName);
	    } else if (layerHandler != null) {
		JOptionPane.showMessageDialog(this, "Layer Handler not found.\nCan't find anything to add the layer to.");
	    } else {
		JOptionPane.showMessageDialog(this, "No Layer instantiated");
	    }
	} else if (e.getActionCommand() == inspector.cancelCommand) {
	    if (layer != null && propertyHandler != null) {
		propertyHandler.removeUsedPrefix(((PropertyConsumer)layer).getPropertyPrefix());
	    }
	} else {
	    showPanel();	    
	}
    }

    public void showPanel() {
	createPanel();
	prefixTextField.setText(DefaultLayerName);

	int x = 10;
	int y = 10;
	
	WindowSupport ws = getWindowSupport();

	Point loc = ws.getComponentLocation();
	if (loc != null) {
	    x = (int) loc.getX();
	    y = (int) loc.getY();
	}

	ws.displayInWindow(x, y, -1, -1);
    }

    /**
     * Looks for PropertyHandler and LayerHandler.
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof PropertyHandler) {
	    // do the initializing that need to be done here
	    Debug.message("layerspanel","LayerAddPanel found a LayerHandler");
	    propertyHandler = (PropertyHandler)someObj;
	}
	if (someObj instanceof LayerHandler) {
	    layerHandler = (LayerHandler)someObj;
	}
    }
    
    /** 
     * Disconnect from any objects that are removed from MapHandler.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof PropertyHandler &&
	    propertyHandler == someObj) {
	    // do the initializing that need to be done here
	    Debug.message("layerspanel","LayerAddPanel removing PropertyHandler");
	    propertyHandler = null;
	}
	if (someObj instanceof LayerHandler && 
	    someObj == layerHandler) {
	    Debug.message("layerspanel","LayerAddPanel removing LayerHandler");
	    layerHandler = null;
	}
    }
    
    /** Test cases. */
    public static void main(String[] args) {
	LayerAddPanel lap = new LayerAddPanel(new PropertyHandler(), null);
	Layer[] layers = new Layer[1];
	layers[0] = new com.bbn.openmap.layer.shape.ShapeLayer();
	
	lap.createPanel(layers);
    }
}

