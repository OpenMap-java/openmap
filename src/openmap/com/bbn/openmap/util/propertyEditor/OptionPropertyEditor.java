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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/OptionPropertyEditor.java,v $
// $RCSfile: OptionPropertyEditor.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The base class for property editors that support a list of options
 * being presented in the GUI. When defining the properties that the
 * property editor will use for a PropertyConsumer in the
 * getPropertyInfo method, just use the name of the property that the
 * options are being used for:
 * 
 * <pre>
 * 
 * 
 *  BigNameOptionProperty.options=option1 option2 option3
 *  BigNameOptionProperty.option1=Big Name 1
 *  BigNameOptionProperty.option2=Big Name 2
 *  BigNameOptionProperty.option3=Big Name 3
 * 
 *  
 * </pre>
 * 
 * Don't use the property prefix for the PropertyConsumer being
 * defined, only the property.
 */
public abstract class OptionPropertyEditor extends
        PropertyConsumerPropertyEditor {

    public final static String OptionsProperty = "options";
    public final static String ScopedOptionsProperty = ".options";

    protected Component customEditor = null;

    public OptionPropertyEditor() {}

    public boolean supportsCustomEditor() {
        return true;
    }

    public void setCustomEditor(Component comp) {
        customEditor = comp;
    }

    /** Returns the editor GUI. */
    public Component getCustomEditor() {
        return customEditor;
    }

    public abstract void setOptions(String[] options);

    /** Sets option based on string. */
    public abstract void setValue(Object string);

    /** Returns String from option choices. */
    public abstract String getAsText();

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
        firePropertyChange();
    }

    /**
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties props) {
        // get the options, first from the space separated option
        // property list, then from the properties using the marker
        // names from that list.
        String[] options = null;
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        String optionListProperty = props.getProperty(prefix + OptionsProperty);
        if (optionListProperty != null) {
            Vector optionVector = PropUtils.parseSpacedMarkers(optionListProperty);
            options = new String[optionVector.size()];
            for (int i = 0; i < options.length; i++) {
                options[i] = props.getProperty(prefix
                        + (String) optionVector.elementAt(i));
            }

            setOptions(options);

        } else {
            Debug.error("OptionPropertyEditor for " + prefix
                    + " not given options");
        }
    }

}