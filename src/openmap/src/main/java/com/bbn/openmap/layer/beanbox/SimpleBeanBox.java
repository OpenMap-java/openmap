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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.BeanInfo;
import java.util.Vector;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.tools.beanbox.BeanBox;
import com.bbn.openmap.tools.beanbox.BeanContainer;

/**
 * SimpleBeanBox is a sample implementation of the
 * {@link com.bbn.openmap.tools.beanbox.BeanBox}class. SimpleBeanBox manages
 * beans of type SimpleBeanObject.
 */
public class SimpleBeanBox extends BeanBox implements MapMouseListener {

    private SimpleBeanLayer layer;

    private static SimpleBeanBox thisBeanBox = null;

    public SimpleBeanBox(SimpleBeanLayer l) {
        super();
        layer = l;
    }

    public String[] getMouseModeServiceList() {
        return new String[] { SelectMouseMode.modeID };
    }

    public boolean mousePressed(MouseEvent evt) {
        return true;
    }

    public boolean mouseReleased(MouseEvent evt) {
        return true;
    }

    public boolean mouseClicked(MouseEvent evt) {
        return true;
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public boolean mouseDragged(MouseEvent evt) {
        return true;
    }

    public boolean mouseMoved(MouseEvent evt) {
        return true;
    }

    public void mouseMoved() {
    }

    /**
     * adds the specified bean to SimpleBeanLayer. This method is called by the
     * <code>com.bbn.openmap.tools.beanbox.BeanBoxDnDCatcher</code> class to add
     * the specified bean to a openmap layer.
     * 
     * @throws an IllegalArgumentException if bean is not of type
     *         SimpleBeanObject
     */
    public void addBean(Object bean) {

        // System.out.println("Enter> SimpleBeanBox.addBean");

        if (!(bean instanceof SimpleBeanObject))
            throw new IllegalArgumentException("not instanceof SimpleBeanObject");

        layer.addObject((SimpleBeanObject) bean);

        // System.out.println("Exit> SimpleBeanBox.addBean");

    }

    /**
     * removes the specified bean from SimpleBeanLayer. This method is called by
     * the <code>com.bbn.openmap.tools.beanbox.BeanBoxDnDCatcher</code> class to
     * remove the specified bean from a openmap layer.
     * 
     * @throws an IllegalArgumentException if bean is not of type
     *         SimpleBeanObject
     */
    public void removeBean(Object bean) {

        // System.out.println("Enter> SimpleBeanBox.removeBean");

        if (!(bean instanceof SimpleBeanObject))
            throw new IllegalArgumentException("not instanceof SimpleBeanObject");

        layer.removeObject(((SimpleBeanObject) bean).getId());

        // System.out.println("Exit> SimpleBeanBox.removeBean");

    }

    /**
     * checks if the specified bean is present in SimpleBeanLayer. returns true
     * if present, false otherwise.
     * 
     * @throws an IllegalArgumentException if bean is not of type
     *         SimpleBeanObject
     */

    public boolean containsBean(Object bean) {

        // System.out.println("Called> SimpleBeanBox.containsBean");

        if (!(bean instanceof SimpleBeanObject))
            throw new IllegalArgumentException("not instanceof SimpleBeanObject " + bean);

        return (layer.getObject(((SimpleBeanObject) bean).getId()) != null);
    }

    /**
     * Sets the image associated with the bean using the image present in the
     * BeanInfo. Also sets the bean's location. This method is called by the
     * <code>com.bbn.openmap.tools.beanbox.BeanBoxDnDCatcher</code> class to set
     * the bean's properties before it is displayed in a property sheet prior to
     * adding to an openmap layer.
     * 
     * @throws an IllegalArgumentException if bean is not of type
     *         SimpleBeanObject
     */
    public void setBeanProperties(Object bean, BeanInfo beanInfo, Point location) {

        // System.out.println("Enter>
        // SimpleBeanBox.setBeanProperties");

        if (!(bean instanceof SimpleBeanObject))
            throw new IllegalArgumentException("not instanceof SimpleBeanObject " + bean);

        SimpleBeanObject obj = (SimpleBeanObject) bean;

        Image img = beanInfo.getIcon(BeanInfo.ICON_COLOR_32x32);
        obj.setGraphicImage(img);

        Point2D llp = layer.getProjection().inverse(location.x, location.y);

        obj.setLatitude((float) llp.getY());
        obj.setLongitude((float) llp.getX());

        // System.out.println("Exit>
        // SimpleBeanBox.setBeanProperties");
    }

    /**
     * gets a Vector of beans that implement the
     * <code>com.bbn.openmap.tools.beanbox.BeanContainer</code> interface.
     * 
     * @return a possibly empty vector of container beans.
     */
    Vector getAllContainers() {

        Vector containers = new Vector();
        Vector list = layer.getObjects();

        if (list == null || list.isEmpty())
            return containers;

        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);

            if (obj instanceof BeanContainer)
                containers.add(obj);
        }

        return containers;
    }

    /**
     * returns a <code>BeanContainer</code> bean that contains the specified
     * bean object.
     * 
     * @throws an IllegalArgumentException if bean is not of type
     *         SimpleBeanObject
     */
    public BeanContainer findEnclosingContainer(Object bean) {

        // System.out.println("Called>
        // SimpleBeanBox.findEnclosingContainer");

        if (!(bean instanceof SimpleBeanObject)) {
            return null;
        }

        SimpleBeanObject obj = (SimpleBeanObject) bean;

        float objLat = obj.getLatitude();
        float objLon = obj.getLongitude();
        LatLonPoint llp = new LatLonPoint.Float(objLat, objLon);

        return findEnclosingContainer(llp);
    }

    /**
     * returns a SimpleBeanContainer bean that contains the specified
     * <code>LatLonPoint</code> on the map.
     */
    public SimpleBeanContainer findEnclosingContainer(LatLonPoint llp) {

        Vector containers = getAllContainers();

        for (int i = 0; i < containers.size(); i++) {
            SimpleBeanContainer container = (SimpleBeanContainer) containers.get(i);

            if (encloses(container, llp))
                return container;
        }

        return null;
    }

    /**
     * helper method returns true if the specified <code>LatLonPoint</code> is
     * contained within the specified <code>SimpleBeanContainer</code>.
     */
    boolean encloses(SimpleBeanContainer container, Point2D llp) {

        float topLat = container.getTopLatitude();
        float leftLon = container.getLeftLongitude();
        float botLat = container.getBottomLatitude();
        float rightLon = container.getRightLongitude();
        float lat = (float) llp.getY();
        float lon = (float) llp.getX();

        if ((lon > rightLon) || (lon < leftLon))
            return false;

        if ((lat > topLat) || (lat < botLat))
            return false;

        return true;
    }

    /**
     * finds a bean of type
     * <code>com.bbn.openmap.tools.beanbox.BeanContainer</code> that encloses
     * the specified (x,y) point. In case more than one container encloses the
     * specified point the first one found is returned. Returns null if none is
     * found.
     */
    BeanContainer findContainerBean(Point pointOnMap) {
        if (layer != null && layer.getProjection() != null) {
            Point2D llp = layer.getProjection().inverse(pointOnMap.x, pointOnMap.y);
            return findEnclosingContainer(llp);
        } else {
            return null;
        }
    }

    /**
     * returns a bean that does NOT implement
     * <code>com.bbn.openmap.tools.beanbox.BeanContainer</code> and which lies
     * closest to and within 10 pixels of the specified (x,y) point. Returns
     * null if no such object is found.
     */
    SimpleBeanObject findNonContainerBean(Point pointOnMap) {

        Vector list = layer.getObjects();

        double minSep = Double.MAX_VALUE;
        SimpleBeanObject closest = null;

        for (int i = 0; i < list.size(); i++) {

            Object o = list.get(i);

            SimpleBeanObject obj = (SimpleBeanObject) o;

            if (obj instanceof SimpleBeanContainer)
                continue;

            Point2D p2 = layer.getProjection().forward(new LatLonPoint.Float(obj.getLatitude(), obj.getLongitude()));

            double sep = almostEquals(pointOnMap, p2, 20);

            if (sep < minSep) {
                minSep = sep;
                closest = obj;
            }
        }

        return closest;
    }

    /**
     * return bean at specified location giving preference to non-container
     * beans over container beans. If neither type of bean is found to be close
     * enough to the specified location, a null is returned.
     */
    public Object getBeanAtLocation(Point pointOnMap) {

        // System.out.println("Called>
        // SimpleBeanBox.getBeanAtLocation");

        SimpleBeanObject obj = findNonContainerBean(pointOnMap);

        if (obj != null)
            return obj;

        BeanContainer container = findContainerBean(pointOnMap);

        if (container != null)
            return container;

        return null;
    }

    /**
     * returns the straight line separation in pixels between the specified
     * points if separation is equal to or less than the specified tolerance
     * amount, else returns Double.MAX_VALUE
     */

    double almostEquals(Point2D p1, Point2D p2, double tol) {

        double sepX = p1.getX() - p2.getX();
        double sepY = p1.getY() - p2.getY();
        double sep = Math.sqrt(sepX * sepX + sepY * sepY);

        if (sep <= tol)
            return sep;
        else
            return Double.MAX_VALUE;
    }

    /**
     * relocates the specified bean to the new location. This method is called
     * by the <code>com.bbn.openmap.tools.beanbox.BeanBoxDnDCatcher</code>
     * whenever the user moves a bean on the map within the same openmap layer.
     * 
     * @throws IllegalArgumentException is specified bean is not of type
     *         SimpleBeanObject.
     */
    public void relocateBean(Object bean, BeanInfo beanInfo, Point newLocation) {

        // System.out.println("Enter> SimpleBeanBox.relocateBean");

        if (!(bean instanceof SimpleBeanObject))
            throw new IllegalArgumentException("not instanceof SimpleBeanObject " + bean);

        SimpleBeanObject obj = (SimpleBeanObject) bean;

        Point2D llp = layer.getProjection().inverse(newLocation.x, newLocation.y);

        relocateSimpleBeanObject(obj, llp);

        layer.updateGraphics();

        // System.out.println("Exit> SimpleBeanBox.relocateBean");
    }

    /** relocates the specified SimpleBeanObject to the new location. */
    void relocateSimpleBeanObject(SimpleBeanObject obj, Point2D newllp) {

        SimpleBeanContainer oldContainer = null;

        // no support yet for containers within containers because
        // of unresolved issues regarding partially over-lapping
        // containers.
        if (!(obj instanceof SimpleBeanContainer)) {
            oldContainer = (SimpleBeanContainer) findEnclosingContainer(obj);
        }

        if (oldContainer != null) {
            oldContainer.remove(obj);
        }

        obj.setLatitude((float) newllp.getY());
        obj.setLongitude((float) newllp.getX());

        if (obj instanceof SimpleBeanContainer)
            ((SimpleBeanContainer) obj).validate();

        SimpleBeanContainer newContainer = null;

        if (!(obj instanceof SimpleBeanContainer)) {
            newContainer = (SimpleBeanContainer) findEnclosingContainer(obj);
        }

        if (newContainer != null) {
            newContainer.add(obj);
        }
    }

    /**
     * this method is a callback method that is called by a
     * <code>com.bbn.openmap.tools.beanbox.GenericPropertySheet</code> when the
     * user closes the property sheet.
     */
    public void beanChanged(Object bean, String changedPropertyName) {

        // System.out.println("Enter> SimpleBeanBox.beanChanged");

        if (!(bean instanceof SimpleBeanObject))
            throw new IllegalArgumentException("not instanceof SimpleBeanObject " + bean);

        SimpleBeanObject obj = (SimpleBeanObject) bean;

        layer.updateObject(obj);

        // System.out.println("Exit> SimpleBeanBox.beanChanged");

    }

    /**
     * returns the image that the cursor is set to when the specified bean is
     * dragged on the map.
     */
    protected Image getDragImage(Object bean) {

        // System.out.println("Called> SimpleBeanBox.getDragImage");

        if (!(bean instanceof SimpleBeanObject))
            throw new IllegalArgumentException("not instanceof SimpleBeanObject " + bean);

        SimpleBeanObject obj = (SimpleBeanObject) bean;

        OMGraphic graphic = layer.getGraphic(obj.getId());

        if (graphic instanceof OMRasterObject) {
            return ((OMRasterObject) graphic).getImage();
        } else
            return super.getDragImage(bean);

    }

}