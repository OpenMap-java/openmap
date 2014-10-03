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

package com.bbn.openmap.layer.beanbox;

import java.awt.Image;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;

import com.bbn.openmap.tools.beanbox.BeanLayoutEditor;

/**
 * A BeanInfo for the
 * {@link com.bbn.openmap.layer.beanbox.SimpleBeanContainer}bean
 */
public class SimpleBeanContainerBeanInfo extends SimpleBeanObjectBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>(8);

        PropertyDescriptor[] pds = super.getPropertyDescriptors();
        list.addAll(Arrays.asList(pds));

        try {
            list.add(new PropertyDescriptor("widthInNM", SimpleBeanContainer.class));
            list.add(new PropertyDescriptor("heightInNM", SimpleBeanContainer.class));
            PropertyDescriptor pd = new PropertyDescriptor("layoutClass", SimpleBeanContainer.class);
            pd.setPropertyEditorClass(LayoutClassEditor.class);
            list.add(pd);

            pd = new PropertyDescriptor("layoutManager", SimpleBeanContainer.class, "getLayout", "setLayout");

            // since layoutManager property is itself a bean
            // (of type BeanLayoutManager), use the
            // general purpose BeanLayoutEditor class provided in the
            // tools.beanbox
            // package as the editor class of this bean property.
            pd.setPropertyEditorClass(BeanLayoutEditor.class);

            list.add(pd);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return list.toArray(new PropertyDescriptor[list.size()]);
    }

    public Image getIcon(int iconKind) {

        Image image = loadImage("/com/bbn/openmap/layer/beanbox/simplebeancontainer.gif");

        return image;
    }
}