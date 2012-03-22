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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OMToolComponent.java,v $
// $RCSfile: OMToolComponent.java,v $
// $Revision: 1.9 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Properties;

import com.bbn.openmap.I18n;
import com.bbn.openmap.util.PropUtils;

/**
 * The OMToolComponent is an extension to OMComponentPanel that
 * provides a little support for those components that are also
 * OpenMap Tools. Those components may be used directly in the GUI, or
 * provide a Tool widget that will let the component be brought up in
 * a different window. This class provides some support for
 * controlling what gets set in the ToolPanel, effectively providing
 * more controls on how a component can be used. For instance, the
 * LayersPanel may be used directly in the GUI, integrated into the
 * main application window. If a ToolPanel is there as well, and both
 * are tossed into the MapHandler, the ToolPanel will display the
 * LayersPanel face even though it is unnecessary. The methods in this
 * super class make it possible for the LayersPanel to not provide a
 * Tool face in certain applications.
 * <p>
 * 
 * This class will handle the "isTool" property and set the useAsTool
 * setting as a result of properties being set.
 */
public abstract class OMToolComponent extends OMComponentPanel implements Tool {

    public final static String UseAsToolProperty = "isTool";

    public final static String defaultKey = "omtoolcomponent";
    protected String key = defaultKey;

    /**
     * This setting should be used to control if the face for this
     * tool is used.
     */
    protected boolean useAsTool = true;

    /**
     * Default gridbag layout.
     */
    protected GridBagLayout gridbag;
    /**
     * Default gridbag layout constraints.
     */
    protected GridBagConstraints c;

    public OMToolComponent() {
        super();
        setLayout(createLayout());
    }

    /**
     * Hook to allow subclasses to use a different layout than the
     * GridBagLayout. Set the layout on this class in this method.
     */
    protected LayoutManager createLayout() {
        gridbag = new GridBagLayout();
        c = getGridBagConstraints();
        return gridbag;
    }

    /**
     * If the default setLayout() method is used with the
     * GridBagLayout, this method will be called to get the
     * GridBagConstraints for that layout. This method can be
     * overridden to make general adjustments to the constraints, like
     * inset settings, etc.
     */
    protected GridBagConstraints getGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 2, 0, 2);
        return constraints;
    }

    /**
     * Overridden add method that takes care of GridBagLayout
     * constraints.
     */
    public Component add(Component component) {
        gridbag.setConstraints(component, c);
        return super.add(component);
    }

    /**
     * Tool interface method. The retrieval tool's interface. This is
     * added to the tool bar.
     * 
     * @return The container GUI for this tool, may be null.
     */
    public Container getFace() {
        if (getUseAsTool()) {
            return this;
        } else {
            return null;
        }
    }

    /**
     * The retrieval key for this tool
     * 
     * @return String The key for this tool.
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the retrieval key for this tool
     * 
     * @param aKey The key for this tool.
     */
    public void setKey(String aKey) {
        key = aKey;
    }

    /**
     * Set whether the Tool's face should be used. The subclasses to
     * this class should either remove all components from its face,
     * or make its face invisible if this is set to false.
     */
    public void setUseAsTool(boolean value) {
        useAsTool = value;
    }

    /**
     * Find the setting to let the Tool know whether its tool
     * interface should be used as well.
     */
    public boolean getUseAsTool() {
        return useAsTool;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        // Important for ToolPanel that controls what it is listening
        // for, instead of grabbing any Tool. The prefix will be used
        // as a discriminator.
        if (prefix != null) {
            setKey(prefix);
        }

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        setUseAsTool(PropUtils.booleanFromProperties(props, prefix
                + UseAsToolProperty, getUseAsTool()));
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + UseAsToolProperty, new Boolean(useAsTool).toString());
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        String internString = i18n.get(OMToolComponent.class,
                UseAsToolProperty,
                I18n.TOOLTIP,
                "Whether this component should be added to the Tool Panel");
        props.put(UseAsToolProperty, internString);

        internString = i18n.get(OMToolComponent.class,
                UseAsToolProperty,
                "Add to Tool Panel");
        props.put(UseAsToolProperty + LabelEditorProperty, internString);

        props.put(UseAsToolProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        return props;
    }
}