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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/LayerAddPanel.java,v $
// $RCSfile: LayerAddPanel.java,v $
// $Revision: 1.9 $
// $Date: 2006/02/14 20:55:52 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.bbn.openmap.Environment;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.propertyEditor.Inspector;

/**
 * Class to interactively add a Layer to the map. A LayerAddPanel utilizes the
 * bean context mechanisms to keep up to date about the applications
 * LayerHandler and PropertyHandler (see findAndInit method). A property is used
 * to determine objects of which Layer subclasses can be instantiated (see
 * static String layerTypes), for configuration of a layer-to-be an Inspector is
 * invoked to inspect and configure a Layer object through the PropertyConsumer
 * interface.
 */
public class LayerAddPanel extends OMComponentPanel implements Serializable, ActionListener {
	/**
	 * Constant field containing markers used in properties file for layers that
	 * can be created using the LayerAddPanel.
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
	 * The list of available Layer classes. Is initiated with pretty names.
	 */
	protected JComboBox<String> list = null;
	/**
	 * Text field used to define new Layer class to create.
	 */
	protected JTextField classTextField = null;
	/** The String to use as a prefix for the new Layer's properties. */
	protected JTextField prefixTextField = null;
	/** Action command String for JButton. */
	protected final String configureActionCommand = "configureActionCommand";
	/** Contains Layer classes to be instantiated. */
	protected Hashtable<String, String> layerClasses = null;
	/** The Inspector to handle the configuration of the new Layer. */
	protected Inspector inspector = null;
	/** The layer to configure and add. */
	protected Object layer;
	/**
	 * JButton to use to create new Layer.
	 */
	protected JButton configureButton;

	/**
	 * Creates the LayerPanel.
	 */
	public LayerAddPanel() {
		super();
		if (Debug.debugging("addable")) {
			Debug.output("LayerAddPanel()");
		}
		inspector = new Inspector();
		inspector.addActionListener((ActionListener) this);
	}

	/**
	 * Creates the LayerPanel.
	 * 
	 * @param l the LayerHandler controlling the layers.
	 */
	public LayerAddPanel(PropertyHandler p, LayerHandler l) {
		this();
		propertyHandler = p;
		layerHandler = l;
	}

	public void createLayerClasses(Layer[] layers) {
		getLayerClasses().clear();

		for (Layer l : layers) {
			String name = l.getName();
			if (name == null) {
				name = l.getClass().getName();
				int lastDotIndex = name.lastIndexOf('.');
				if (lastDotIndex >= 0) {
					name = name.substring(lastDotIndex);
				}
			}

			addLayer(name, l.getClass().getName());
		}
	}

	/**
	 * Produces a dialog panel to add a Layer, with the layers given.
	 */
	public void createPanel(Layer[] layers) {
		createLayerClasses(layers);
		createPanel();
	}

	public final static String DefaultLayerName = "Layer Name";

	/**
	 * Produces a dialog panel to add a layer. If the layers haven't been
	 * manually added through createPanel(layers), then the PropertyHandler is
	 * consulted and the layer list is built from the layerTypes property.
	 */
	public void createPanel() {
		removeAll();

		configureButton = new JButton(i18n.get(LayerAddPanel.class, "configureButton", "Configure"));
		configureButton.addActionListener(this);
		configureButton.setActionCommand(configureActionCommand);

		String defaultLayerName = i18n.get(LayerAddPanel.class, "defaultLayerName", DefaultLayerName);
		prefixTextField = new JTextField(defaultLayerName, 12);

		Set<String> keys = getLayerClasses().keySet();
		String[] layerTypes = keys.toArray(new String[keys.size()]);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(10, 10, 5, 10);

		if (layerTypes.length == 0) {
			configureButton.setEnabled(false);
			classTextField = new JTextField(30);
			classTextField
					.setToolTipText(i18n.get(LayerAddPanel.class, "classFieldToolTip", "Class name of layer to add"));
			/*
			 * // Since there's no list provided, give a text box to let the
			 * user // specify which layer to create. String message =
			 * i18n.get(LayerAddPanel.class, "noLayersAvailableMessage",
			 * "No Layers available for creation."); JLabel label = new
			 * JLabel(message);
			 */

			classTextField.setInputVerifier(new InputVerifier() {

				@Override
				public boolean verify(JComponent input) {
					JTextField tf = (JTextField) input;
					String className = tf.getText();
					try {
						ClassLoader.getSystemClassLoader().loadClass(className);
					} catch (ClassNotFoundException cnfe) {
						configureButton.setEnabled(false);
						return false;
					}

					configureButton.setEnabled(true);
					return true;
				}

			});

			gridbag.setConstraints(classTextField, c);
			add(classTextField);

			configureButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String newClassName = (String) classTextField.getText();
					String prefix = prefixTextField.getText().trim();
					createLayer(newClassName, prefix);
				}
			});

		} else {
			list = new JComboBox<String>(layerTypes);
			gridbag.setConstraints(list, c);
			add(list);

			configureButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String prettyName = (String) list.getSelectedItem();
					String prefix = prefixTextField.getText().trim();

					if (prettyName == null) {
						return;
					}

					String newClassName = layerClasses.get(prettyName);

					createLayer(newClassName, prefix);
				}
			});

		}

		// Prefix/Name field
		c.insets = new Insets(5, 10, 10, 10);
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(prefixTextField, c);
		add(prefixTextField);

		// Add configure button
		c.weightx = 0;
		gridbag.setConstraints(configureButton, c);
		add(configureButton);

		invalidate();
	}

	public Hashtable<String, String> getLayerClasses() {
		if (layerClasses == null) {
			layerClasses = new Hashtable<String, String>();
		}
		return layerClasses;
	}

	public void addLayer(String prettyName, String className) {
		getLayerClasses().put(prettyName, className);
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		if (Debug.debugging("addable")) {
			Debug.output("LayerAddPanel.setProperties()");
		}
		getLayerTypes(props);
	}

	public Properties getProperties(Properties props) {
		props = super.getProperties(props);

		int layerNumber = 1;
		if (layerClasses != null) {
			StringBuffer layerList = new StringBuffer();

			for (String prettyName : layerClasses.keySet()) {
				String className = (String) layerClasses.get(prettyName);
				String markerName = "l" + (layerNumber++);
				layerList.append(markerName).append(" ");
				props.put(markerName + ".prettyName", prettyName);
				props.put(markerName + ".class", className);
			}
			props.put(Environment.OpenMapPrefix + "." + layerTypes, layerList.toString());
		}

		return props;
	}

	/**
	 * Gets Layer information from PropertyHandler. These layers are defined in
	 * the application properties under the openmap.layerTypes property.
	 * 
	 * @return Hashtable of prettyName String keys with classname values. Empty
	 *         Hashtable if no layers are available.
	 */
	protected Hashtable<String, String> getLayerTypes() {
		return getLayerTypes(null);
	}

	/**
	 * Gets Layer information from the given properties. These layers are
	 * defined in the application properties under the openmap.layerTypes
	 * property. If the given properties are null, then the property handler, if
	 * found, will be consulted directly.
	 * 
	 * @return Hashtable of prettyName String keys with classname values. Empty
	 *         Hashtable if no layers are available.
	 */
	protected Hashtable<String, String> getLayerTypes(Properties props) {
		Hashtable<String, String> layerHash = getLayerClasses();
		layerHash.clear();

		if (props == null) {
			if (propertyHandler != null) {
				props = propertyHandler.getProperties();
			} else {
				return layerHash;
			}
		}

		String prefix = Environment.OpenMapPrefix;
		String addableList = props.getProperty(prefix + "." + layerTypes);

		if (Debug.debugging("addable")) {
			Debug.output("LayerAddPanel: " + addableList);
		}

		Vector<String> layerClassList = PropUtils.parseSpacedMarkers(addableList);

		if (layerClassList == null) {
			return layerHash;
		}

		// debug info: available layers
		int unNamedCount = 1;
		for (String layerClassString : layerClassList) {
			String className = props.getProperty(layerClassString + ".class");
			String prettyName = props.getProperty(layerClassString + ".prettyName");

			if (prettyName == null) {
				prettyName = "Layer " + (unNamedCount++);
			}

			if (className != null) {
				if (Debug.debugging("addable")) {
					Debug.output("  adding " + className + ", " + className);
				}
				layerHash.put(prettyName, className);
			}
		}
		return layerHash;
	}

	/**
	 * Create a layer given a class name and property prefix to be used as a
	 * name.
	 * 
	 * @param className class of layer to create.
	 * @param prefix pretty name and property prefix.
	 */
	protected void createLayer(String className, String prefix) {
		Object obj = ComponentFactory.create(className);

		if (obj instanceof Layer) {
			layer = (Layer) obj;
			// Set the pretty name to what the user chose.
			((Layer) layer).setName(prefix);

			prefixTextField.setText("");
			if (classTextField != null) {
				classTextField.setText("");
			}

			WindowSupport ws = getWindowSupport();
			if (ws != null) {
				ws.cleanUp();
			}
		}

		if (obj instanceof PropertyConsumer) {
			// Set the prefix to a modified version of the pretty name.
			prefix = propertyHandler.getUniquePrefix(prefix);
			((PropertyConsumer) obj).setPropertyPrefix(prefix);
			inspector.inspectPropertyConsumer((PropertyConsumer) obj);
		}
	}

	/**
	 * Method associated with the ActionListener interface.
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand() == Inspector.doneCommand) {
			// the confirmation button of the Inspector panel was
			// pressed
			// find the beancontext and add the layer at hand (var.
			// layer)
			if (layer != null && layerHandler != null) {
				if (layer instanceof Layer) {
					// Let's add it on top, so the user can find it
					// easier, instead of adding it to the bottom and
					// having it lost behind some other layers.
					layerHandler.addLayer((Layer) layer, 0);
				}
				prefixTextField.setText(DefaultLayerName);
			} else if (layerHandler != null) {
				String message = i18n.get(LayerAddPanel.class, "noLayerHandlerMessage",
						"Layer Handler not found.\nCan't find anything to add the layer to.");
				JOptionPane.showMessageDialog(this, message);
			} else {
				String message = i18n.get(LayerAddPanel.class, "noLayerCreatedMessage", "No Layer instantiated.");
				JOptionPane.showMessageDialog(this, message);
			}
		} else if (e.getActionCommand() == Inspector.cancelCommand) {
			if (layer != null && propertyHandler != null) {
				propertyHandler.removeUsedPrefix(((PropertyConsumer) layer).getPropertyPrefix());
			}
		}
	}

	/**
	 * Show the panel in a JFrame.
	 */
	public void showPanel() {
		createPanel();
		prefixTextField.setText(DefaultLayerName);

		WindowSupport ws = getWindowSupport();

		MapHandler mh = (MapHandler) getBeanContext();
		Frame frame = null;
		if (mh != null) {
			frame = (Frame) mh.get(java.awt.Frame.class);
		}

		if (ws == null) {
			ws = new WindowSupport(this, i18n.get(LayerAddPanel.class, "title", "Add Layer"));
			setWindowSupport(ws);
		}

		ws.displayInWindow(frame, -1, -1, -1, -1);
	}

	/**
	 * Looks for PropertyHandler and LayerHandler.
	 */
	public void findAndInit(Object someObj) {
		if (someObj instanceof PropertyHandler) {
			// do the initializing that need to be done here
			Debug.message("layerspanel", "LayerAddPanel found a LayerHandler");
			propertyHandler = (PropertyHandler) someObj;
		}
		if (someObj instanceof LayerHandler) {
			layerHandler = (LayerHandler) someObj;
		}
	}

	/**
	 * Disconnect from any objects that are removed from MapHandler.
	 */
	public void findAndUndo(Object someObj) {
		if (someObj instanceof PropertyHandler && propertyHandler == someObj) {
			// do the initializing that need to be done here
			Debug.message("addable", "LayerAddPanel removing PropertyHandler");
			propertyHandler = null;
		}
		if (someObj instanceof LayerHandler && someObj == layerHandler) {
			Debug.message("addable", "LayerAddPanel removing LayerHandler");
			layerHandler = null;
		}
	}

}
