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

package com.bbn.openmap.examples.beanbox;

import java.awt.*;
import java.beans.*;
import java.util.*;

/**
 * A BeanInfo for the
 * {@link com.bbn.openmap.examples.beanbox.SimpleBeanObject}bean.
 */
public class SimpleBeanObjectBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        ArrayList list = new ArrayList(4);

        try {
            list.add(new PropertyDescriptor("id", SimpleBeanObject.class));
            list.add(new PropertyDescriptor("latitude", SimpleBeanObject.class));
            list.add(new PropertyDescriptor("longitude", SimpleBeanObject.class));
            list.add(new PropertyDescriptor("bearingInDeg", SimpleBeanObject.class));
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return (PropertyDescriptor[]) list.toArray(new PropertyDescriptor[0]);
    }

    public Image getIcon(int iconKind) {

        Image image = loadImage("/com/bbn/openmap/examples/beanbox/simplebean.gif");

        return image;
    }

}