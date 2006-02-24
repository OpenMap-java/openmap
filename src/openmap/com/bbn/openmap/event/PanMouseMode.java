/*
 * 
 * Copyright (C) SISDEF Ltda. All rights reserved.
 * 
 * Created on 25-feb-2005
 */
package com.bbn.openmap.event;

import java.awt.AlphaComposite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Properties;

import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * PanMouseMode it is a class for Pan operation on the visible map. This class
 * show actual map in transparent mode. 25-feb-2005. There are a couple of
 * properties that can be set in this mouse mode:
 * <pre>
 * # Floating number between 0-1, with 1 being opaque, default .5
 * panmm.opaqueness=.5f
 * # True/false, to leave old map up behind panned version.
 * panmm.leaveShadow=true
 * </pre>
 * 
 * @author cursor
 */
public class PanMouseMode extends CoordMouseMode {

    public final static String OpaquenessProperty = "opaqueness";
    public final static String LeaveShadowProperty = "leaveShadow";

    public final float DEFAULT_OPAQUENESS = 0.5f;
    public final static transient String modeID = "Pan";

    private boolean isPanning = false;
    private BufferedImage img = null;
    private int oX, oY;

    private float opaqueness = DEFAULT_OPAQUENESS;
    private boolean leaveShadow = true;

    // private Cursor myPointer;

    public PanMouseMode() {
        super(modeID, true);

        setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        /*
         * For who like make his CustomCursor
         * 
         * Toolkit tk = Toolkit.getDefaultToolkit(); ImageIcon pointer = new
         * ImageIcon(getClass().getResource("/icons/pan.gif")); myPointer=
         * tk.createCustomCursor(pointer.getImage(), new Point(0,0), "PP");
         * setModeCursor(myPointer);
         */

    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        opaqueness = PropUtils.floatFromProperties(props, prefix
                + OpaquenessProperty, opaqueness);
        leaveShadow = PropUtils.booleanFromProperties(props, prefix
                + LeaveShadowProperty, leaveShadow);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + OpaquenessProperty, Float.toString(opaqueness));
        props.put(prefix + LeaveShadowProperty, Boolean.toString(leaveShadow));
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        String internString = i18n.get(PanMouseMode.class,
                OpaquenessProperty,
                I18n.TOOLTIP,
                "Transparency level for panned map (between 0 and 1).");
        props.put(Layer.AddToBeanContextProperty, internString);
        internString = i18n.get(PanMouseMode.class,
                OpaquenessProperty,
                "Opaqueness");
        props.put(OpaquenessProperty + LabelEditorProperty, internString);

        internString = i18n.get(PanMouseMode.class,
                LeaveShadowProperty,
                I18n.TOOLTIP,
                "Flag to display current map while panning.");
        props.put(Layer.AddToBeanContextProperty, internString);
        internString = i18n.get(PanMouseMode.class,
                LeaveShadowProperty,
                "Shadow current map");
        props.put(LeaveShadowProperty + LabelEditorProperty, internString);
        props.put(LeaveShadowProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return props;
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     *      The first click for drag, the image is generated. This image is
     *      redrawing when the mouse is move, but, I need to repain the original
     *      image.
     */
    public void mouseDragged(MouseEvent arg0) {

        int x = arg0.getX();
        int y = arg0.getY();

        MapBean mb = ((MapBean) arg0.getSource());
        Graphics2D gr2d = (Graphics2D) mb.getGraphics();

        if (!isPanning) {
            int w = mb.getWidth();
            int h = mb.getHeight();

            /*
             * Making the image
             */

            img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Graphics2D g = (Graphics2D) ge.createGraphics(img);
            g.setClip(0, 0, w, h);
            mb.paintAll(g);

            oX = x;
            oY = y;

            isPanning = true;

        } else {
            if (img != null) {

                /*
                 * Drawing original image whithout transparence and in the
                 * initial position
                 */
                if (leaveShadow) {
                    gr2d.drawImage(img, 0, 0, null);
                } else {
                    gr2d.setPaint(mb.getBckgrnd());
                    gr2d.fillRect(0, 0, mb.getWidth(), mb.getHeight());
                }

                /*
                 * Drawing image whith transparence and in the mouse position
                 * minus origianl mouse click position
                 */
                gr2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        opaqueness));
                gr2d.drawImage(img, x - oX, y - oY, null);
            }
        }
        super.mouseDragged(arg0);
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     *      Make Pan event for the map.
     */
    public void mouseReleased(MouseEvent arg0) {
        if (isPanning && arg0.getSource() instanceof MapBean) {
            MapBean mb = (MapBean) arg0.getSource();
            Projection proj = mb.getProjection();
            Point2D center = proj.forward(proj.getCenter());
            center.setLocation(center.getX() - arg0.getX() + oX, center.getY()
                    - arg0.getY() + oY);
            mb.setCenter(proj.inverse(center));
            isPanning = false;
            img = null;
        }
        super.mouseReleased(arg0);
    }

    public boolean isLeaveShadow() {
        return leaveShadow;
    }

    public void setLeaveShadow(boolean leaveShadow) {
        this.leaveShadow = leaveShadow;
    }

    public float getOpaqueness() {
        return opaqueness;
    }

    public void setOpaqueness(float opaqueness) {
        this.opaqueness = opaqueness;
    }

    public boolean isPanning() {
        return isPanning;
    }

    public int getOX() {
        return oX;
    }

    public int getOY() {
        return oY;
    }

}
