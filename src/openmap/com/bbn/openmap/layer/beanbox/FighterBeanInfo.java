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

/**
 * A BeanInfo for the {@link com.bbn.openmap.layer.beanbox.Fighter}
 * bean.
 */
public class FighterBeanInfo extends SimpleBeanObjectBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>(7);

        PropertyDescriptor[] pds = super.getPropertyDescriptors();
        list.addAll(Arrays.asList(pds));

        try {
            list.add(new PropertyDescriptor("type", Fighter.class));
            list.add(new PropertyDescriptor("speedInNMPerHr", Fighter.class));
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return list.toArray(new PropertyDescriptor[list.size()]);
    }

    public Image getIcon(int iconKind) {

        Image image = loadImage("/com/bbn/openmap/layer/beanbox/fighter.gif");

        return image;
    }

}