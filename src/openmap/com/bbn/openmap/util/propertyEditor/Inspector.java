// Bart 20060831 -> i18n

/*
 * File: Inspector.java
 * Date: Jul 2001
 * Author: Kai Lessmann <klessman@intevation.de>
 * Copyright 2001 Intevation GmbH, Germany
 *
 * This file is Free Software to be included into OpenMap 
 * under its Free Software license.
 * Permission is granted to use, modify and redistribute.
 *
 * Intevation hereby grants BBN a royalty free, worldwide right and license 
 * to use, copy, distribute and make Derivative Works
 * of this Free Software created by Kai Lessmann 
 * and sublicensing rights of any of the foregoing.
 * 
 */

package com.bbn.openmap.util.propertyEditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Class to inspect a PropertyConsumer. Used by the LayerAddPanel class to
 * interactively configure a Layer object before it gets added to the map. This
 * class should suffice to "inspect" any PropertyConsumer on a very basic level,
 * handling is more convenient if property editor classes are available. The
 * behavior of the Inspector is configured through properties; the
 * propertiesInfo object of a PropertyConsumer may contain a
 * initPropertiesProperty which determines which properties are to be shown and
 * in which order, in a space separated list, i.e.
 * 
 * <code>
 * initPropertiesProperty=class prettyName shapeFile
 *
 * </code> If this property is not defined, then all the properties will be
 * displayed, in alphabetical order.
 * 
 * For each property there may be a editorProperty entry giving a PropertyEditor
 * class to instantiate as an editor for the property, i.e. <code>
 * shapeFile.editor=com.bbn.openmap.util.propertyEditor.FilePropertyEditor
 * </code>.
 */
public class Inspector implements ActionListener {

    /** A simple TextField as a String editor. */
    protected final String defaultEditorClass = "com.bbn.openmap.util.propertyEditor.TextPropertyEditor";

    /**
     * The PropertyConsumer being inspected. Set in inspectPropertyConsumer.
     */
    protected PropertyConsumer propertyConsumer = null;

    /** Handle to the GUI. Used for setVisible(true/false). */
    protected WindowSupport windowSupport = null;

    /** Action command for the cancelButton. */
    // public so it can be referenced from the actionListener
    public final static String cancelCommand = "cancelCommand";

    /** The action command for the doneButton. */
    // public so it can be referenced from the actionListener
    public final static String doneCommand = "doneCommand";

    /**
     * Hashtable containing property names, and their editors. Used to fetch
     * user inputs for configuring a property consumer.
     */
    protected Hashtable<String, PropertyEditor> editors = null;

    /**
     * Handle to call back the object that invokes this Inspector.
     */
    protected ActionListener actionListener = null;

    /**
     * Flag to print out the properties. Used when the Inspector is in
     * stand-alone mode, so that the properties are directed to stdout.
     */
    protected boolean print = false;

    // I18N mechanism
    static I18n i18n = Environment.getI18n();

    /**
     * Set an ActionListener for callbacks. Once a Layer object is configured,
     * i.e. the "Add" button has been clicked, an ActionListener that invoked
     * this Inspector can register here to be notified.
     */
    public void addActionListener(ActionListener al) {
        actionListener = al;
    }

    /** Does nothing. */
    public Inspector() {}

    /** Sets the actionListener. */
    public Inspector(ActionListener al) {
        actionListener = al;
    }

    /**
     * Inspect and configure a PropertyConsumer object. Main method of this
     * class. The argument PropertyConsumer is inspected through the
     * ProperyConsumer interface, the properties are displayed to be edited.
     */
    public void inspectPropertyConsumer(PropertyConsumer propertyConsumer) {
        String prefix = propertyConsumer.getPropertyPrefix();

        // construct GUI
        if (windowSupport != null) {
            windowSupport.killWindow();
            windowSupport = null;
        }

        JComponent comp = createPropertyGUI(propertyConsumer);
        windowSupport = new WindowSupport(comp, i18n.get(Inspector.class,
                "Inspector",
                "Inspector")
                + " - " + prefix);

        windowSupport.setMaxSize(-1, 500);
        windowSupport.displayInWindow();
    }

    @SuppressWarnings("unchecked")
    public Vector<String> sortKeys(Collection keySet) {
        Vector<String> vector = new Vector<String>(keySet.size());

        // OK, ok, this isn't the most efficient way to do this, but
        // it's simple. Shouldn't matter for what we are using it
        // for...
        Iterator it = keySet.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            int size = vector.size();
            for (int i = 0; i <= size; i++) {
                if (i == size) {
                    // System.out.println("Adding " + key + " at " +
                    // i);
                    vector.add(key);
                    break;
                } else {
                    int compare = key.compareTo((String) vector.elementAt(i));
                    if (compare < 0) {
                        // System.out.println(key + " goes before " +
                        // vector.elementAt(i) + " at " + i);
                        vector.add(i, key);
                        break;
                    }
                }
            }
        }
        return vector;
    }

    /**
     * Creates a JComponent with the properties to be changed. This component is
     * suitable for inclusion into a GUI.
     * 
     * @param pc The property consumer to create a gui for.
     * @return JComponent, a panel holding the interface to set the properties.
     */
    public JComponent createEmbeddedPropertyGUI(PropertyConsumer pc) {
        // fill variables
        this.propertyConsumer = pc;
        Properties props = new Properties();
        props = pc.getProperties(props);
        Properties info = new Properties();
        info = pc.getPropertyInfo(info);
        String prefix = pc.getPropertyPrefix();

        return createEmbeddedPropertyGUI(prefix, props, info);
    }

    /**
     * Creates a JComponent with the properties to be changed. This component is
     * suitable for inclusion into a GUI. Don't use this method directly! Use
     * the createPropertyGUI(PropertyConsumer) instead. You will get a
     * NullPointerException if you use this method without setting the
     * PropertyConsumer in the Inspector.
     * 
     * @param prefix the property prefix for the property consumer. Received
     *        from the PropertyConsumer.getPropertyPrefix() method. Properties
     *        that start with this prefix will have the prefix removed from the
     *        display, so the GUI will only show the actual property name.
     * @param props the properties received from the
     *        PropertyConsumer.getProperties() method.
     * @param info the properties received from the
     *        PropertyConsumer.getPropertyInfo() method, containing descriptions
     *        and any specific PropertyEditors that should be used for a
     *        particular property named in the PropertyConsumer.getProperties()
     *        properties.
     * @return JComponent, a panel holding the interface to set the properties.
     */
    public JComponent createEmbeddedPropertyGUI(String prefix,
                                                Properties props,
                                                Properties info) {

        if (Debug.debugging("inspectordetail")) {
            Debug.output("Inspector creating GUI for " + prefix
                    + "\nPROPERTIES " + props + "\nPROP INFO " + info);
        }

        // collect the properties that are going to be displayed...

        // First, check the initPropertiesProperty for the PropertyConsumer
        // stating which properties it wants included.
        String propertyList = info.getProperty(PropertyConsumer.initPropertiesProperty);
        Vector<String> sortedKeys;

        if (propertyList != null) {
            Vector<String> propertiesToShow = PropUtils.parseSpacedMarkers(propertyList);
            for (int i = 0; i < propertiesToShow.size(); i++) {
                propertiesToShow.set(i, prefix + "." + propertiesToShow.get(i));
            }
            sortedKeys = propertiesToShow;
        } else {
            // otherwise, show them all, in alphabetical order
            sortedKeys = sortKeys(props.keySet());
        }

        editors = new Hashtable<String, PropertyEditor>(sortedKeys.size());

        JPanel component = new JPanel();
        component.setLayout(new BorderLayout());

        JPanel propertyPanel = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 10, 2, 10);
        propertyPanel.setLayout(gridbag);

        int i = 0;
        for (String prop : sortedKeys) {

            // Marker is going to be the scoped version of prop, if needed.
            String marker = prop;
            if (prefix != null && prop.startsWith(prefix)) {
                marker = prop.substring(prefix.length() + 1);
            }

            if (marker.startsWith(".")) {
                marker = marker.substring(1);
            }

            String editorMarker = marker
                    + PropertyConsumer.ScopedEditorProperty;
            String editorClass = info.getProperty(editorMarker);
            if (editorClass == null) {
                editorClass = defaultEditorClass;
            }

            // instantiate PropertyEditor
            Class<?> propertyEditorClass = null;
            PropertyEditor editor = null;
            try {
                propertyEditorClass = Class.forName(editorClass);
                editor = (PropertyEditor) propertyEditorClass.newInstance();
                if (editor instanceof PropertyConsumer) {
                    ((PropertyConsumer) editor).setProperties(marker, info);
                }
                editors.put(prop, editor);
            } catch (Exception e) {
                e.printStackTrace();
                editorClass = null;
            }

            Component editorFace = null;
            if (editor != null && editor.supportsCustomEditor()) {
                editorFace = editor.getCustomEditor();
            } else {
                editorFace = new JLabel(i18n.get(Inspector.class,
                        "Does_not_support_custom_editor",
                        "Does not support custom editor"));

            }

            if (editor != null) {
                Object propVal = props.get(prop);
                if (Debug.debugging("inspector")) {
                    Debug.output("Inspector loading " + prop + "(" + propVal
                            + ")");
                }
                editor.setValue(propVal);
            }

            // Customized labels for each property, instead of the
            // abbreviated nature of the true property names.
            String labelMarker = marker + PropertyConsumer.LabelEditorProperty;
            String labelText = info.getProperty(labelMarker);
            if (labelText == null) {
                labelText = marker;
            }

            JLabel label = new JLabel(labelText + ":");
            label.setHorizontalAlignment(SwingConstants.RIGHT);

            c.gridx = 0;
            c.gridy = i++;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.EAST;

            gridbag.setConstraints(label, c);
            propertyPanel.add(label);

            c.gridx = 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1f;

            gridbag.setConstraints(editorFace, c);
            propertyPanel.add(editorFace);

            String toolTip = (String) info.get(marker);
            label.setToolTipText(toolTip == null ? i18n.get(Inspector.class,
                    "No_further_information_available",
                    "No further information available.") : toolTip);

        }

        // create the palette's scroll pane
        JScrollPane scrollPane = new JScrollPane(propertyPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        // scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
        component.add(scrollPane, BorderLayout.CENTER);
        return component;
    }

    /**
     * Creates a JComponent with the properties to be changed. This component is
     * suitable for inclusion into a GUI.
     * 
     * @param pc The property consumer to create a gui for.
     * @return JComponent, a panel holding the interface to set the properties.
     */
    public JComponent createPropertyGUI(PropertyConsumer pc) {
        // fill variables
        this.propertyConsumer = pc;
        Properties props = new Properties();
        props = pc.getProperties(props);
        Properties info = new Properties();
        info = pc.getPropertyInfo(info);
        String prefix = pc.getPropertyPrefix();

        return createPropertyGUI(prefix, props, info);
    }

    /**
     * Creates a JComponent with the properties to be changed. This component is
     * suitable for inclusion into a GUI. Don't use this method directly! Use
     * the createPropertyGUI(PropertyConsumer) instead. You will get a
     * NullPointerException if you use this method without setting the
     * PropertyConsumer in the Inspector.
     * 
     * @param prefix the property prefix for the property consumer. Received
     *        from the PropertyConsumer.getPropertyPrefix() method. Properties
     *        that start with this prefix will have the prefix removed from the
     *        display, so the GUI will only show the actual property name.
     * @param props the properties received from the
     *        PropertyConsumer.getProperties() method.
     * @param info the properties received from the
     *        PropertyConsumer.getPropertyInfo() method, containing descriptions
     *        and any specific PropertyEditors that should be used for a
     *        particular property named in the PropertyConsumer.getProperties()
     *        properties.
     * @return JComponent, a panel holding the interface to set the properties.
     */
    public JComponent createPropertyGUI(String prefix, Properties props,
                                        Properties info) {

        JComponent component = createEmbeddedPropertyGUI(prefix, props, info);

        JButton doneButton = null, cancelButton = null;

        JPanel buttons = new JPanel();
        if (print) {
            doneButton = new JButton(i18n.get(Inspector.class, "Print", "Print"));
            cancelButton = new JButton(i18n.get(Inspector.class, "Quit", "Quit"));
        } else {
            doneButton = new JButton(i18n.get(Inspector.class, "Ok", "Ok"));
            cancelButton = new JButton(i18n.get(Inspector.class,
                    "Cancel",
                    "Cancel"));
        }
        doneButton.addActionListener(this);
        doneButton.setActionCommand(doneCommand);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(cancelCommand);
        buttons.add(doneButton);
        buttons.add(cancelButton);

        component.add(buttons, BorderLayout.SOUTH);

        component.validate();
        return component;
    }

    /**
     * Implement the ActionListener interface. The actions registering here
     * should be generated by the two buttons in the Inspector GUI.
     */
    public void actionPerformed(ActionEvent e) {
        final String actionCommand = e.getActionCommand();
        String prefix = propertyConsumer.getPropertyPrefix();

        if (actionCommand == doneCommand) {// confirmed
            Properties props = collectProperties();

            if (!print) {
                if (windowSupport != null) {
                    windowSupport.killWindow();
                }
                propertyConsumer.setProperties(prefix, props);
                if (actionListener != null) {
                    actionListener.actionPerformed(e);
                }
            } else {
                Set<Entry<Object, Object>> entrySet = props.entrySet();
                Iterator<Entry<Object, Object>> it = entrySet.iterator();
                while (it.hasNext()) {
                    Entry<Object, Object> next = it.next();
                    String val = (String) next.getValue();
                    System.out.println(next.getKey() + "=" + val);
                }
            }

        } else if (actionCommand == cancelCommand) {// canceled
            if (actionListener != null && actionListener != this) {
                actionListener.actionPerformed(e);
            }
            propertyConsumer = null; // to be garb. coll'd
            if (windowSupport != null) {
                windowSupport.killWindow();
            }

            if (print) {
                System.exit(0);
            }
        }
    }

    /**
     * Tells the Inspector to collect the properties from the editors and set
     * them on its PropertyConsumer.
     */
    public void collectAndSetProperties() {
        if (propertyConsumer != null) {
            String prefix = propertyConsumer.getPropertyPrefix();
            Properties props = collectProperties();
            propertyConsumer.setProperties(prefix, props);
        }
    }

    /** Extracts properties from textfield[]. */
    public Properties collectProperties() {
        Properties props = new Properties();

        for (String key : editors.keySet()) {
            PropertyEditor editor = editors.get(key);
            if (editor != null) {
                String stuff = editor.getAsText();
                // If it's not defined with text, don't put it in the
                // properties. The layer should handle this and use
                // its default settings.
                if (stuff != null && stuff.length() > 0) {
                    props.put(key, stuff);
                }

                if (editor instanceof PropertyConsumer) {
                    ((PropertyConsumer) editor).getProperties(props);
                }
            }
        }
        return props;
    }

    public void setPrint(boolean p) {
        print = p;
    }

    public boolean getPrint() {
        return print;
    }

    public WindowSupport getWindowSupport() {
        return windowSupport;
    }

    /** test cases. */
    public static void main(String[] args) {
        Debug.init();
        String name = (args.length < 1) ? "com.bbn.openmap.layer.shape.ShapeLayer"
                : args[0];
        PropertyConsumer propertyconsumer = null;
        try {
            Class<?> c = Class.forName(name);
            propertyconsumer = (PropertyConsumer) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (propertyconsumer != null) {
            Properties props = new Properties(), info = new Properties();
            System.out.println("Inspecting " + name);

            String pp = name.substring(name.lastIndexOf(".") + 1);
            propertyconsumer.setPropertyPrefix(pp.toLowerCase());

            props = propertyconsumer.getProperties(props);
            info = propertyconsumer.getPropertyInfo(info);

            Inspector inspector = new Inspector();
            inspector.setPrint(true);
            inspector.addActionListener(inspector);
            inspector.inspectPropertyConsumer(propertyconsumer);
        }
    }
}