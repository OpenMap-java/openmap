/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays the properties associated with a bean for editing. An instance of
 * this class is created by the {@link com.bbn.openmap.tools.beanbox.BeanBox}to
 * display the properties of a bean that the user has clicked on the map. An
 * instance of GenericPropertySheet can also be used as a custom editor
 * component for a bean property that is itself a bean.
 */
public class GenericPropertySheet extends JDialog implements PropertyChangeListener {

    /**
     * contains a reference to an internal panel that displays the bean's
     * properties.
     */
    protected PropertySheetPanel panel;

    /**
     * If an instance of this class is used as a custom editor component of a
     * bean's property that is itself a bean, this member contains a reference
     * to the custom editor.
     */
    protected PropertyEditor editor;

    /** the bean that this property sheet is associated with. */
    protected Object targetBean;

    /**
     * A GenericPropertySheet can be optionally associated with a BeanBox.
     */
    protected BeanBox beanBox;

    /**
     * contains the rectangular bounds of this GenericPropertySheet.
     */
    protected Rectangle bounds;

    /**
     * contains the number of editors displayed in this GenericPropertySheet.
     */
    protected int numEditorsToDisplay;

    /**
     * Constructs a property sheet dialog.
     * 
     * @param isModal whether the propertysheet should be displayed in a modal
     *        dialog.
     * @param title the title of this propertysheet.
     */
    public GenericPropertySheet(boolean isModal, String title) {
        super((JFrame) null, title, isModal);
    }

    /**
     * Constructs a property sheet dialog.
     * 
     * @param target the bean associated with this property sheet.
     * @param x the top-left x position of this property sheet.
     * @param y the top-left y position of this property sheet.
     * @param beanBox the beanBox that this propertysheet is associated with.
     *        This param is usually non-null only if this is a top-level
     *        property-sheet. When this param is non-null, this propertysheet
     *        will inform the BeanBox whenever a property on the bean changes by
     *        calling the beanChanged method on BeanBox. Additionally the
     *        propertysheet will call the editComplete method on the BeanBox
     *        when the user closes the window.
     */
    public GenericPropertySheet(Object target, int x, int y, PropertyEditor pe, BeanBox beanBox) {
        this(false, target, new Rectangle(x, y, 100, 100), pe, beanBox);
    }

    /**
     * Constructs a property sheet dialog.
     * 
     * @param isModal whether to display the propertysheet as a modal dialog.
     * @param target the bean property that this class handles.
     * @param bounds the boundaries to use
     * @param pe the parent PropertyEditor of this sheet. An instance of
     *        GenericPropertySheet is invoked from the getCustomEditor method of
     *        pe. The parent editor can be null, in which case this class
     *        behaves exactly as a regular property sheet class.
     * @param beanBox the beanBox that this propertysheet is associated with.
     *        This param is usually non-null only if this is a top-level
     *        property-sheet. When this param is non-null, this propertysheet
     *        will inform the BeanBox whenever a property on the bean changes by
     *        calling the beanChanged method on BeanBox.
     */

    public GenericPropertySheet(boolean isModal, Object target, Rectangle bounds,
            PropertyEditor pe, BeanBox beanBox) {
        super((JFrame) null, "Properties - <initializing...>", isModal);

        this.targetBean = target;

        /*
         * if (bounds == null) this.bounds = new Rectangle(0, 0, 100, 100); else
         * { this.bounds = new Rectangle(); this.bounds.x = bounds.x;
         * this.bounds.y = bounds.y; this.bounds.width = (bounds.width > 0) ?
         * bounds.width : 100; this.bounds.height = (bounds.height > 0) ?
         * bounds.height : 100; }
         */

        this.editor = pe;
        this.beanBox = beanBox;

        init();
        this.getContentPane().add(panel);
    }

    /**
     * Initializes the background, bounds, title, panel and adds a window
     * listener.
     */
    protected void init() {
        setBackground(Color.lightGray);
        // setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        initTitle();
        initPanel();
        addWindowListener();
    }

    /**
     * Initializes the property sheet panel.
     */
    protected void initPanel() {
        panel = new PropertySheetPanel(this);

        if (targetBean != null)
            panel.setTarget(targetBean);
    }

    /**
     * Initializes the property sheet's title.
     */
    protected void initTitle() {
        if (targetBean != null) {
            Class beanClass = targetBean.getClass();
            try {
                BeanInfo bi = Introspector.getBeanInfo(beanClass);
                String label = bi.getBeanDescriptor().getDisplayName();
                setTitle(label + " Properties");
            } catch (Exception ex) {
                System.err.println("GenericPropertySheet: couldn't find BeanInfo for " + beanClass);
                ex.printStackTrace();
            }
        }
    }

    /**
     * adds a window listener to this property sheet. The windowClosing method
     * calls the editComplete method on the BeanBox associated with this
     * property sheet if there is one.
     */
    protected void addWindowListener() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                if (beanBox != null)
                    beanBox.editComplete(targetBean);
                setVisible(false);
            }
        });
    }

    /**
     * Returns the JPanel object used to display all the editors in this
     * property sheet.
     */
    protected PropertySheetPanel getPropertySheetPanel() {
        return panel;
    }

    /**
     * Sets the frame size in order to accommodate all property editors.
     */
    protected void setFrameSize() {
        int approxControlBarWidth = 60;
        int approxTitleBarHeight = 20;
        int approxIconWidth = 20;
        int approxFontWidth = 8;
        int approxFontHeight = 25;
        int approxTitleWidth = (getTitle() == null) ? approxControlBarWidth * 2 : (approxIconWidth
                + (getTitle().length() * approxFontWidth) + approxControlBarWidth);
        int width = (approxTitleWidth > 220) ? approxTitleWidth : 220;
        //setSize(width, approxTitleBarHeight + numEditorsToDisplay * approxFontHeight * 2);
        setSize(width, approxTitleBarHeight + numEditorsToDisplay * approxFontHeight);        
    }

    /**
     * Sets the number of editors to be displayed on this property sheet.
     */
    protected void setNumEditorsToDisplay(int numEditorsToDisplay) {
        this.numEditorsToDisplay = numEditorsToDisplay;
    }

    /**
     * Sets the bean associated with this property sheet. The property sheet
     * will re-initialize to display the bean's properties when this method is
     * called.
     */
    public void setTarget(Object bean) {
        // System.out.println("Enter>
        // GenericPropertySheet.setTarget()");

        panel.setTarget(bean);
        Class beanClass = bean.getClass();

        try {
            BeanInfo bi = Introspector.getBeanInfo(beanClass);
            String label = bi.getBeanDescriptor().getDisplayName();
            setTitle("Properties for " + label);
        } catch (Exception ex) {
            System.err.println("GenericPropertySheet: couldn't find BeanInfo for " + beanClass);
            ex.printStackTrace();
        }

        setVisible(true);
        targetBean = bean;

        // System.out.println("Exit>
        // GenericPropertySheet.setTarget()");
    }

    // private void setCustomizer(Customizer c) {
    // panel.setCustomizer(c);
    // }

    /**
     * Required by interface PropertyChangeListener. This method is called
     * whenever one of the properties of the associated bean changes. If there
     * is a PropertyEditor associated with this property sheet, this method will
     * generate a call to the editor's setValue method. If there is a BeanBox
     * associated with this property sheet, this method will generate a call to
     * beanChanged method on the BeanBox.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        panel.wasModified(evt);

        if (editor != null)
            editor.setValue(targetBean);

        if (beanBox != null)
            beanBox.beanChanged(targetBean, evt.getPropertyName());
    }
}

// *****************************************************************************

/**
 * A utilty class used to display a bean's properties on a GenericPropertySheet.
 */

class PropertySheetPanel extends JPanel {

    private GenericPropertySheet _frame;

    // We need to cache the targets' wrapper so we can annotate it with
    // information about what target properties have changed during
    // design
    // time.
    private Object targetBean;
    private PropertyDescriptor[] properties;
    private PropertyEditor[] editors;
    private Object[] values;
    private Component[] views;
    private JLabel[] labels;

    private boolean processEvents;

    PropertySheetPanel(GenericPropertySheet frame) {
        _frame = frame;
        this.setLayout(null);
        this.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    }

    synchronized void setTarget(Object targ) {
        // System.out.println("Enter>
        // PropertySheetPanel.setTarget()");

        // We make the panel invisible during the reconfiguration
        // to try to reduce screen flicker.
        setVisible(false);

        removeAll();
        targetBean = targ;

        try {
            BeanInfo bi = Introspector.getBeanInfo(targetBean.getClass());
            properties = bi.getPropertyDescriptors();
        } catch (IntrospectionException ex) {
            error("GenericPropertySheet: Couldn't introspect", ex);
            return;
        }

        editors = new PropertyEditor[properties.length];
        values = new Object[properties.length];
        views = new Component[properties.length];
        labels = new JLabel[properties.length];
        int numEditorsToDisplay = 0;

        for (int i = 0; i < properties.length; i++) {
            String name = properties[i].getDisplayName();

            // Don't display hidden or expert properties.
            if (properties[i].isHidden() || properties[i].isExpert()) {
                System.out.println("Ignoring hidden or expert property " + name);
                continue;
            }

            Class type = properties[i].getPropertyType();
            Method getter = properties[i].getReadMethod();
            Method setter = properties[i].getWriteMethod();

            // Only display read/write properties.
            if ((getter == null) || (setter == null)) {
                System.out.println("Ignoring read-only/write-only property " + name);
                continue;
            }

            Component view = null;

            try {
                Object args[] = {};
                Object value = getter.invoke(targetBean, args);
                values[i] = value;
                PropertyEditor editor = null;
                Class pec = properties[i].getPropertyEditorClass();

                if (pec != null) {
                    try {
                        editor = (PropertyEditor) pec.newInstance();
                    } catch (Exception ex) {
                        // Drop through.
                        System.out.println("Cannot instantiate editor class: " + pec);
                        System.out.println("Will try to find editor using "
                                + "PropertyEditorManager");
                    }
                }

                if (editor == null)
                    editor = PropertyEditorManager.findEditor(type);

                editors[i] = editor;

                // If we can't edit this component, skip it.
                if (editor == null) {
                    // If it's a user-defined property we give a
                    // warning.
                    String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();

                    if (getterClass.indexOf("java.") != 0)
                        System.err.println("Warning: Can't find public property editor for property \""
                                + name + "\".  Skipping.");

                    continue;
                }

                // System.out.println("About to set value " + value);
                editor.setValue(value);
                editor.addPropertyChangeListener(_frame);

                // Now figure out how to display it...
                if (editor.isPaintable() && editor.supportsCustomEditor())
                    view = new PropertyCanvas(_frame, editor);
                else if (editor.getTags() != null)
                    view = new PropertySelector(editor);
                else if (editor.getAsText() != null) {
                    view = new PropertyText(editor);
                } else {
                    System.err.println("Warning: Property \"" + name
                            + "\" has non-displayabale editor. Skipping.");
                    continue;
                }

                if (editor instanceof GenericPropertyEditorInterface)
                    ((GenericPropertyEditorInterface) editor).setTargetBean(targetBean);
            } catch (InvocationTargetException ex) {
                System.err.println("Skipping property " + name + " ; exception on target: "
                        + ex.getTargetException());
                ex.getTargetException().printStackTrace();
                continue;
            } catch (Exception ex) {
                System.err.println("Skipping property " + name + "; exception: " + ex);
                ex.printStackTrace();
                continue;
            }

            labels[i] = new JLabel(name, JLabel.LEFT);
            views[i] = view;

            numEditorsToDisplay++;
        } // end for

        this.setLayout(new GridLayout(numEditorsToDisplay, 2));

        for (int i = 0; i < properties.length; i++)
            if (views[i] != null) {
                add(labels[i]);
                add(views[i]);
            }

        _frame.setNumEditorsToDisplay(numEditorsToDisplay);
        _frame.setFrameSize();
        processEvents = true;
        setVisible(true);

        // System.out.println("Exit> PropertySheetPanel.setTarget()");
    } // end setTarget

    synchronized void setCustomizer(Customizer c) {
        if (c != null)
            c.addPropertyChangeListener(_frame);
    }

    synchronized void wasModified(PropertyChangeEvent evt) {
        // System.out.println("Enter>
        // PropertySheetPanel.wasModified");
        // System.out.println("evt = " + evt);

        if (!processEvents) {
            // System.out.println("Exit>GPS::wasModified");
            return;
        }

        if (evt.getSource() instanceof PropertyEditor) {
            PropertyEditor editor = (PropertyEditor) evt.getSource();
            // System.out.println("editor="+editor);

            for (int i = 0; i < editors.length; i++) {
                if (editors[i] == editor) {
                    PropertyDescriptor property = properties[i];
                    Object value = editor.getValue();

                    // if value is the string "null", reset it to null
                    if ((value != null) && (value instanceof String)
                            && ((String) value).trim().equalsIgnoreCase("null"))
                        value = null;

                    values[i] = value;
                    Method setter = property.getWriteMethod();

                    try {
                        Object args[] = { value };
                        args[0] = value;
                        setter.invoke(targetBean, args);
                    } catch (InvocationTargetException ex) {
                        if (ex.getTargetException() instanceof PropertyVetoException) {
                            // warning("Vetoed; reason is: "
                            // +
                            // ex.getTargetException().getMessage());
                            // temp deadlock fix...I need to remove the
                            // deadlock.
                            System.err.println("WARNING: Vetoed; reason is: "
                                    + ex.getTargetException().getMessage());
                        } else
                            error("InvocationTargetException while updating " + property.getName(), ex.getTargetException());
                    } catch (Exception ex) {
                        error("Unexpected exception while updating " + property.getName(), ex);
                    }

                    if ((views[i] != null) && (views[i] instanceof PropertyCanvas)) {
                        // System.out.println("repainting view");
                        views[i].repaint();
                    }

                    break;
                }
            }
        }

        // System.out.println("updating other values...");

        // we want to update in the target
        // Now re-read all the properties and update the editors
        // for any other properties that have changed.
        for (int i = 0; i < properties.length; i++) {
            Object o;

            try {
                Method getter = properties[i].getReadMethod();
                Object args[] = {};
                o = getter.invoke(targetBean, args);
            } catch (Exception ex) {
                // System.out.println(" setting o to null");
                o = null;
            }

            // System.out.println(" values[" + i + "]=" + values[i]);

            // check if 'o' is of type Object[]
            if ((o instanceof Object[]) && (values[i] instanceof Object[])) {
                Object[] oldVal = (Object[]) values[i];
                Object[] newVal = (Object[]) o;

                if (newVal.length == oldVal.length) {
                    for (int j = 0; j < newVal.length; j++)
                        if (!newVal[j].equals(oldVal[j]))
                            break;

                    continue;
                }
            } else if ((o == values[i]) || ((o != null) && o.equals(values[i])))
                // The property is equal to its old value.
                continue;

            values[i] = o;

            // System.out.println(" editors[" + i + "]=" +
            // editors[i]);

            // Make sure we have an editor for this property...
            if (editors[i] == null)
                continue;

            // System.out.println(" calling setValue on
            // editors["+i+"]="+editors[i]);

            // The property has changed! Update the editor.
            editors[i].setValue(o);

            if (views[i] != null)
                views[i].repaint();
        }

        // Make sure the target bean gets repainted.
        if (Beans.isInstanceOf(targetBean, Component.class))
            ((Component) (Beans.getInstanceOf(targetBean, Component.class))).repaint();

        // System.out.println("Exit->
        // PropertySheetPanel.wasModified");
    }

    // ----------------------------------------------------------------------

    // private void warning(String s) {
    // System.out.println("Warning: " + s);
    // }

    // ----------------------------------------------------------------------
    // Log an error.

    private void error(String message, Throwable th) {
        String mess = message + ":\n" + th;
        System.err.println(message);
        th.printStackTrace();
        System.out.println(mess);
    }

    // ----------------------------------------------------------------------
}
