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

import java.awt.Graphics;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.beanbox.BeanBox;
import com.bbn.openmap.tools.beanbox.BeanBoxHandler;

/**
 * An OpenMap Layer for displaying
 * {@link com.bbn.openmap.layer.beanbox.SimpleBeanObject}beans.
 * This class an example of an openmap layer that contains an instance
 * of the {@link com.bbn.openmap.tools.beanbox.BeanBox}class. This
 * layer maintains a reference to an instance of
 * {@link com.bbn.openmap.layer.beanbox.SimpleBeanBox}class which
 * is a sample implementation of the
 * {@link com.bbn.openmap.tools.beanbox.BeanBox}class. The
 * {@link com.bbn.openmap.layer.beanbox.SimpleBeanBox}class
 * manages the set of
 * {@link com.bbn.openmap.layer.beanbox.SimpleBeanObject}beans
 * that are displayed by this layer.
 */
public class SimpleBeanLayer extends Layer implements BeanBoxHandler {

    protected HashMap beans = new HashMap();
    protected HashMap graphics = new HashMap();

    protected Projection projection;

    protected SimpleBeanBox beanBox;

    public SimpleBeanLayer() {
        super();

        setName("Simple Bean Layer");

        addToBeanContext = true;

        beanBox = new SimpleBeanBox(this);
    }

    /**
     * @return the instance of
     *         {@link com.bbn.openmap.layer.beanbox.SimpleBeanBox}
     *         that is maintained by this layer.
     */
    public BeanBox getBeanBox() {
        return beanBox;
    }

    /** Gets the current projection */
    public Projection getProjection() {
        return projection;
    }

    /**
     * @return an instance of
     *         {@link com.bbn.openmap.layer.beanbox.SimpleBeanBox},
     *         which implements the MapMouseListener interface.
     */
    public MapMouseListener getMapMouseListener() {
        return (MapMouseListener) beanBox;
    }

    /** Implement ProjectionListener method inherited from Layer. */
    public void projectionChanged(ProjectionEvent event) {
        projection = event.getProjection();

        Collection values = graphics.values();
        Iterator iter = values.iterator();
        while (iter.hasNext())
            ((OMGraphic) iter.next()).generate(projection);
    }

    /** override Component method */
    public void paint(Graphics g) {

        Collection values = graphics.values();
        Iterator iter = values.iterator();
        while (iter.hasNext()) {
            OMGraphic graphic = (OMGraphic) iter.next();
            graphic.render(g);
        }
    }

    /**
     * Update all OMGraphic objects maintained by this layer using the
     * information stored in corresponding SimpleBeanObject beans.
     */
    public void updateGraphics() {

        Set keys = beans.keySet();

        Iterator iter = keys.iterator();

        while (iter.hasNext()) {

            Long id = (Long) iter.next();

            SimpleBeanObject bean = (SimpleBeanObject) beans.get(id);

            OMGraphic graphic = (OMGraphic) graphics.get(id);

            if ((graphic instanceof CustomGraphic)) {
                ((CustomGraphic) graphic).updateGraphic(bean);
            } else if (graphic instanceof OMRasterObject) {
                ((OMRasterObject) graphic).setLat(bean.getLatitude());
                ((OMRasterObject) graphic).setLon(bean.getLongitude());
                ((OMRasterObject) graphic).setRotationAngle(Math.toRadians(bean.getBearingInDeg()));
            }

            graphic.setNeedToRegenerate(true);

            if (projection != null)
                graphic.generate(projection);

        }

        repaint();

    }

    /**
     * Adds a bean to this layer.
     */
    public void addObject(SimpleBeanObject object) {

        beans.put(new Long(object.getId()), object);
        String customGraphicClassName = object.getCustomGraphicClassName();

        OMGraphic graphic = null;

        if (customGraphicClassName == null) {

            ImageIcon icon = new ImageIcon(object.getGraphicImage());
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            graphic = new OMRaster(object.getLatitude(), object.getLongitude(), -width / 2, -height / 2, icon);

            ((OMRaster) graphic).setRotationAngle(Math.toRadians(object.getBearingInDeg()));

            graphic.setRenderType(OMGraphicConstants.RENDERTYPE_OFFSET);

            graphic.setAppObject(new Long(object.getId()));

        } else {

            try {

                Class graphicClass = Class.forName(customGraphicClassName);

                Class parentClass = graphicClass;
                while (parentClass != null) {
                    if (parentClass == CustomGraphic.class) {
                        break;
                    } else
                        parentClass = parentClass.getSuperclass();
                }

                if (parentClass != null) {
                    Constructor constructor = graphicClass.getConstructor(new Class[] { SimpleBeanObject.class });
                    graphic = (CustomGraphic) constructor.newInstance(new Object[] { object });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (graphic != null) {

            graphic.setNeedToRegenerate(true);

            graphics.put(new Long(object.getId()), graphic);

            if (projection != null)
                graphic.generate(projection);

            repaint();
        }

    }

    /**
     * Removes a bean from this layer.
     */
    public void removeObject(Long id) {
        beans.remove(id);
        graphics.remove(id);
    }

    public void removeObject(long id) {
        removeObject(new Long(id));
    }

    /**
     * returns a bean with the specified id.
     */
    public SimpleBeanObject getObject(Long id) {
        return (SimpleBeanObject) beans.get(id);
    }

    public SimpleBeanObject getObject(long id) {
        return (SimpleBeanObject) beans.get(new Long(id));
    }

    /**
     * return all SimpleBeanObject beans maintained by this layer.
     */
    public Vector getObjects() {
        return new Vector(beans.values());
    }

    /**
     * return the OMGraphic object associated with the
     * SimpleBeanObject with the specified id.
     */
    public OMGraphic getGraphic(Long id) {
        return (OMGraphic) graphics.get(id);
    }

    public OMGraphic getGraphic(long id) {
        return (OMGraphic) graphics.get(new Long(id));
    }

    /**
     * Update the specified SimpleBeanObject object that may be
     * maintained by this layer.
     */
    public void updateObject(SimpleBeanObject object) {

        // not yet added to layer. ignore
        if (getObject(object.getId()) == null)
            return;

        removeObject(object.getId());
        addObject(object);
    }

}