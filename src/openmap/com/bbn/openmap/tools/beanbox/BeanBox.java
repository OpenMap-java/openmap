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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.util.HashMap;
import java.util.Vector;

import com.bbn.openmap.image.ImageScaler;
import com.bbn.openmap.util.Debug;

/**
 * The BeanBox class manages a set of java beans that are delivered to
 * it from the {@link com.bbn.openmap.tools.beanbox.BeanPanel}via the
 * {@link com.bbn.openmap.tools.beanbox.BeanBoxDnDCatcher}. An
 * implementation of this abstract class is expected to be associated
 * with an openmap layer. The abstract BeanBox class performs
 * functions such as adding and removing beans from the associated
 * openmap layer and provides support for moving beans on the layer
 * and cut/copy/paste functionality.
 * <p>
 * Layer developers wishing to make use of the BeanBox's bean
 * management capabilities should:
 * <p>
 * <ul>
 * <li>Implement a layer specific version of this abstract class.
 * </li>
 * <li>Implement the
 * {@link com.bbn.openmap.tools.beanbox.BeanBoxHandler}interface in
 * the layer associated with the BeanBox implementation.</li>
 * </ul>
 * <p>
 * 
 * An implementation of this class should provide layer specific
 * versions of at least the following abstract base class methods:
 * <p>
 * <ul>
 * <li>addBean</li>
 * <li>removeBean</li>
 * <li>containsBean</li>
 * <li>setBeanProperties</li>
 * <li>getBeanAtLocation</li>
 * <li>findEnclosingContainer (should always return null if
 * implementation does not support
 * {@link com.bbn.openmap.tools.beanbox.BeanContainer}s)</li>
 * <li>relocateBean</li>
 * <li>beanChanged</li>
 * </ul>
 * <p>
 * Additionally, an implementation of this class can optionally
 * provide layer specific versions of the following base class
 * methods:
 * <p>
 * <ul>
 * <li>showSelected</li>
 * <li>showCut</li>
 * <li>showUnCut</li>
 * </ul>
 * <p>
 */

public abstract class BeanBox {

    private HashMap beanInfoMap;

    /**
     * Default constructor initializes the BeanBox.
     */
    public BeanBox() {
        beanInfoMap = new HashMap();
    }

    /**
     * Utility method to obtain the BeanInfo object associated with a
     * bean class that the BeanBox knows about. Returns a null if none
     * found.
     */
    public BeanInfo getBeanInfoForBean(String beanClassName) {
        BeanInfo beanInfo = (BeanInfo) beanInfoMap.get(beanClassName);
        if (beanInfo == null) {
            beanInfo = BeanPanel.findBeanInfo(beanClassName);
            if (beanInfo != null)
                beanInfoMap.put(beanClassName, beanInfo);
        }

        return beanInfo;
    }

    /**
     * This method is called by the BeanBoxDnDCatcher to give the
     * BeanBox the bean has has just been dropped on the map.
     * 
     * @param object a Vector containing in the following order: the dropped
     *        bean object, its BeanInfo and a Point object containing
     *        the drop location.
     */
    void addBean(Vector object) {

        if (object == null || object.size() != 3
                || !(object.get(1) instanceof BeanInfo)
                || !(object.get(2) instanceof Point)) {
            throw new IllegalArgumentException("bad drop object " + object);
        }

        Object bean = object.get(0);
        BeanInfo beanInfo = (BeanInfo) object.get(1);
        Point point = (Point) object.get(2);

        beanInfoMap.put(bean.getClass().getName(), beanInfo);

        prepareForAddition(bean, beanInfo, point);

    }

    /**
     * This method is called by a
     * {@link com.bbn.openmap.tools.beanbox.GenericPropertySheet}when
     * the user closes the propertysheet window.
     */
    void editComplete(Object bean) {
        if (!containsBean(bean)) {
            addBean(bean);
            doSpecialHandling(bean);
        }
    }

    /**
     * This method is called to prepare the bean for addition to the
     * beanbox. In the base class implementation, this method calls
     * the abstract setBeanProperties method. It then displays the
     * property sheet for the bean, thereby giving the user a chance
     * to edit the bean's properties before the bean is added to the
     * beanbox.
     */
    protected void prepareForAddition(Object bean, BeanInfo beanInfo,
                                      Point location) {

        try {
            setBeanProperties(bean, beanInfo, location);
            createPropertySheet(bean);
        } catch (Exception e) {
            //System.out.println(e);
            e.printStackTrace();
        }

    }

    /**
     * Utility method to create and display a property sheet to show
     * the specified bean's properties.
     */
    protected void createPropertySheet(Object bean) throws Exception {
        // Create a new Property Sheet.
        GenericPropertySheet propertySheet = new GenericPropertySheet(bean, 575, 20, null, this);
        propertySheet.setVisible(true);
    }

    /**
     * Checks if the specified bean should be added to a
     * {@link com.bbn.openmap.tools.beanbox.BeanContainer}.
     */
    protected void doSpecialHandling(Object bean) {

        // if bean is a container, add its id to our list of
        // containers
        // adding containers to other containers is not handled for
        // now because of unresolved issues about treating
        // over-lapping
        // containers.

        if (!(bean instanceof BeanContainer)) {

            // check if the bean was dropped into a container,
            // if so add it to the container
            BeanContainer container = findEnclosingContainer(bean);

            if (container != null)
                container.add(bean);
        }
    }

    /**
     * Returns the image that the cursor will be set to when the
     * specified bean is dragged over the map. Default implementation
     * returns the image contained in the BeanInfo for the bean, or
     * the default BeanPanel image if no image is found in the
     * BeanInfo.
     */
    protected Image getDragImage(Object bean) {
        BeanInfo beanInfo = this.getBeanInfoForBean(bean.getClass().getName());

        if (beanInfo == null) {
            if (Debug.debugging("beanbox"))
                Debug.output("No beanInfo found for bean: " + bean);
            return BeanPanel.defaultBeanIcon.getImage();
        }

        Image img = beanInfo.getIcon(BeanInfo.ICON_COLOR_32x32);

        if (img == null) {
            if (Debug.debugging("beanbox"))
                Debug.output("No image found in beanInfo for bean: " + bean);
            return BeanPanel.defaultBeanIcon.getImage();
        }

        Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
        if (Debug.debugging("beanbox"))
            Debug.output("" + d);
        img = ImageScaler.getOptimalScalingImage(img,(int) d.getWidth(),
                (int) d.getHeight());

        return img;
    }

    /**
     * This method is called when the user selects a bean, usually as
     * a a result of the clicking on it in a layer. An implementation
     * of this class can override this method to highlight the
     * selected bean using a mechanism specific to the openmap layer
     * associated with that BeanBox. Base class method does nothing.
     */
    public void showSelected(Object bean) {}

    /**
     * This method is called when the user 'cuts' a bean, usually as a
     * a result of the clicking on it in a layer and then pressing
     * Ctrl-X. An implementation of this class can override this
     * method to highlight the selected bean using a mechanism
     * specific to the openmap layer associated with that BeanBox.
     * Base class method does nothing.
     */
    public void showCut(Object bean) {}

    /**
     * This method is called when the user cancels a 'cut' operation,
     * usually as a a result of pressing the ESC key. An
     * implementation of this class can override this method to remove
     * any highlights on the bean marked for cutting. Base class
     * method does nothing.
     */
    public void showUnCut(Object bean) {}

    /**
     * This method is called when a bean is dropped on the layer
     * associated with this BeanBox.
     */
    public abstract void addBean(Object bean);

    /**
     * This method is called when a bean is moved from the layer
     * associated with this BeanBox to another layer.
     */
    public abstract void removeBean(Object bean);

    /**
     * This method is intended to check whether this BeanBox knows
     * about the specified bean.
     */
    public abstract boolean containsBean(Object bean);

    /**
     * This method is called when a bean is dropped on the layer
     * associated with this BeanBox. This method gives a chance to set
     * the dropped bean's properties based on the information in its
     * BeanInfo and the drop location. The called to addBean follows
     * this call in the drop sequence.
     */
    public abstract void setBeanProperties(Object bean, BeanInfo beanInfo,
                                           Point location);

    /**
     * Returns a bean contained in the layer at the specified map
     * location.
     * 
     * @return a bean Object or null if no bean is found at the
     *         location.
     */
    public abstract Object getBeanAtLocation(Point pointOnMap);

    /**
     * Returns a bean contained in the layer that implements the
     * BeanContainer interface and which contains the specified bean.
     * 
     * @return a BeanContainer object or null if no such container
     *         bean is found.
     */
    public abstract BeanContainer findEnclosingContainer(Object bean);

    /**
     * This method is called when a bean is moved from its present
     * location to the newlocation within the layer associated with
     * this BeanBox.
     */
    public abstract void relocateBean(Object bean, BeanInfo beanInfo,
                                      Point newLocation);

    /**
     * This method is called by a propertysheet whenever a bean
     * property changes. It is intended to provide a place for the
     * layer developer to update the layer if required.
     */
    public abstract void beanChanged(Object bean, String changedPropertyName);
}

