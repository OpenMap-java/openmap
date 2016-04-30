/*
 *
 * Copyright (C) SISDEF Ltda. All rights reserved.
 *
 * Created on 25-feb-2005
 */
package com.bbn.openmap.event;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.image.ImageScaler;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.Cartesian;
import com.bbn.openmap.proj.Cylindrical;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * PanMouseMode it is a class for Pan operation on the visible map. This class
 * show actual map in transparent mode. 25-feb-2005. There are a couple of
 * properties that can be set in this mouse mode:
 * 
 * <pre>
 *           # Floating number between 0-1, with 1 being opaque, default .5
 *           panmm.opaqueness=.5f
 *           # True/false, to leave old map up behind panned version.
 *           panmm.leaveShadow=true
 * </pre>
 * 
 * @author cursor
 * @author Stephane Wasserhardt
 */
public class PanMouseMode extends CoordMouseMode implements ProjectionListener {

    public final static String OPAQUENESS_PROPERTY = "opaqueness";
    public final static String LEAVE_SHADOW_PROPERTY = "leaveShadow";
    public final static String USE_CURSOR_PROPERTY = "useCursor";
    public final static String AZ_PANNING_SHAPEFILE_PROPERTY = "azPanningShapefile";
    public final static String AZ_PANNING_PROPERTY = "azPanning";

    public final static float DEFAULT_OPAQUENESS = 0.5f;

    public final static transient String modeID = "Pan";
    private boolean isPanning = false;
    private BufferedImage bufferedMapImage = null;
    private OMRaster paintedImage = null;
    private int beanBufferWidth = 0;
    private int beanBufferHeight = 0;
    private int oX, oY;
    private MouseEvent lastMouseEvent;
    private float opaqueness;
    private boolean leaveShadow;
    private boolean useCursor;
    private AzimuthPanner azPanner = null;
    private String azPanningShapefile = null;
    private DrawingAttributes azDrawing = null;

    public PanMouseMode() {
        super(modeID, true);
        setUseCursor(true);
        setLeaveShadow(false);
        setOpaqueness(DEFAULT_OPAQUENESS);

        DrawingAttributes da = DrawingAttributes.getDefaultClone();
        da.setMatted(true);
        da.setMattingPaint(Color.LIGHT_GRAY);

        setAzDrawing(da);
    }

    public void setActive(boolean val) {
        if (!val) {
            if (bufferedMapImage != null) {
                bufferedMapImage.flush();
            }
            beanBufferWidth = 0;
            beanBufferHeight = 0;
            bufferedMapImage = null;
        }
    }

    /**
     * @return Returns the useCursor.
     */
    public boolean isUseCursor() {
        return useCursor;
    }

    /**
     * @param useCursor The useCursor to set.
     */
    public void setUseCursor(boolean useCursor) {
        this.useCursor = useCursor;
        if (useCursor) {
            /*
             * For who like make his CustomCursor
             */
            try {
                Toolkit tk = Toolkit.getDefaultToolkit();
                ImageIcon pointer = new ImageIcon(getClass().getResource("pan.gif"));
                Dimension bestSize = tk.getBestCursorSize(pointer.getIconWidth(), pointer.getIconHeight());
                Image pointerImage = ImageScaler.getOptimalScalingImage(pointer.getImage(), (int) bestSize.getWidth(), (int) bestSize.getHeight());
                Cursor cursor = tk.createCustomCursor(pointerImage, new Point(0, 0), "PP");
                setModeCursor(cursor);
                return;
            } catch (Exception e) {
                // Problem finding image probably, just move on.
            }
        }

        setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        opaqueness = PropUtils.floatFromProperties(props, prefix + OPAQUENESS_PROPERTY, opaqueness);
        leaveShadow = PropUtils.booleanFromProperties(props, prefix + LEAVE_SHADOW_PROPERTY, leaveShadow);

        azPanningShapefile = props.getProperty(prefix + AZ_PANNING_SHAPEFILE_PROPERTY, azPanningShapefile);
        setUseCursor(PropUtils.booleanFromProperties(props, prefix + USE_CURSOR_PROPERTY, isUseCursor()));

        azDrawing.setProperties(prefix + AZ_PANNING_PROPERTY, props);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + OPAQUENESS_PROPERTY, Float.toString(getOpaqueness()));
        props.put(prefix + LEAVE_SHADOW_PROPERTY, Boolean.toString(isLeaveShadow()));
        props.put(prefix + USE_CURSOR_PROPERTY, Boolean.toString(isUseCursor()));
        props.put(prefix + AZ_PANNING_SHAPEFILE_PROPERTY, PropUtils.unnull(azPanningShapefile));

        azDrawing.getProperties(props);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        PropUtils.setI18NPropertyInfo(i18n, props, PanMouseMode.class, OPAQUENESS_PROPERTY, "Transparency", "Transparency level for moving map, between 0 (clear) and 1 (opaque).", null);
        PropUtils.setI18NPropertyInfo(i18n, props, PanMouseMode.class, LEAVE_SHADOW_PROPERTY, "Leave Shadow", "Display current map in background while panning.", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, props, PanMouseMode.class, USE_CURSOR_PROPERTY, "Use Cursor", "Use hand cursor for mouse mode.", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        PropUtils.setI18NPropertyInfo(i18n, props, OMMouseMode.class, AZ_PANNING_SHAPEFILE_PROPERTY, "Az Projection Panning Shapefile", "Use a shapefile for azimuthal projection panning.", "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        azDrawing.getPropertyInfo(props);
        return props;
    }

    /**
     * PaintListener method.
     * 
     * @param source the source object, most likely the MapBean
     * @param g java.awt.Graphics
     */
    public void listenerPaint(Object source, Graphics g) {
        MapBean mapBean = source instanceof MapBean ? (MapBean) source : null;

        if (azPanner != null) {
            azPanner.render(g);
        } else if (mapBean != null) {
            if (isPanning && lastMouseEvent != null && bufferedMapImage != null) {

                /**
                 * TODO:  This doesn't work for rotated images, can't quite get the
                 * buffered image to render in the right location and not be
                 * rotated as well. So for now, if rotated, handle as if for
                 * azimuth. - DFD
                 */

                Graphics2D gr2d = (Graphics2D) g.create();
                Projection proj = mapBean.getRotatedProjection();

                if (!leaveShadow) {
                    gr2d.setPaint(mapBean.getBckgrnd());
                    // Takes care of rotated dimensions, too.
                    gr2d.fillRect(0, 0, proj.getWidth(), proj.getHeight());
                }

                Point2D pnt0 = proj.forward(mapBean.inverse(oX, oY, null));
                int startX = (int) pnt0.getX();
                int startY = (int) pnt0.getY();

                Point2D pnt = mapBean.getNonRotatedLocation(lastMouseEvent);
                int x = (int) pnt.getX();
                int y = (int) pnt.getY();

                /*
                 * Drawing image with transparency and in the mouse position
                 * minus original mouse click position
                 */
                gr2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaqueness));

                paintedImage.setX(x - startX);
                paintedImage.setY(y - startY);
                paintedImage.setRotationAngle(-proj.getRotationAngle());
                
                paintedImage.generate(proj);
                paintedImage.render(gr2d);

            } else {
                mapBean.removePaintListener(this);
            }
        }
    }

    public void mousePressed(MouseEvent arg0) {
        MapBean mapBean = arg0.getSource() instanceof MapBean ? (MapBean) arg0.getSource() : null;

        if (mapBean != null) {

            lastMouseEvent = arg0;
            oX = (int) arg0.getX();
            oY = (int) arg0.getY();
            // If the map is rotated, the size of the projection will be bigger
            // than
            // the size of the MapBean.
            Projection proj = mapBean.getRotatedProjection();
            int w = proj.getWidth();
            int h = proj.getHeight();

            if ((proj instanceof Cylindrical || proj instanceof Cartesian)
                    && proj.getRotationAngle() == 0.0) {
                
                if (bufferedMapImage == null) {
                    createBuffer(w, h);
                }

                Graphics2D g = (Graphics2D) bufferedMapImage.getGraphics();
                mapBean.paintChildren(g, null);

                Point2D ul = mapBean.inverse(0.0, 0.0, null);

                paintedImage = new OMRaster(MoreMath.latJLT90(ul.getY()), ul.getX(), 0, 0, bufferedMapImage);
                paintedImage.putAttribute(OMGraphicConstants.NO_ROTATE, Boolean.TRUE);

            } else {
                URL url = null;
                try {
                    url = PropUtils.getResourceOrFileOrURL(azPanningShapefile);
                } catch (MalformedURLException murle) {
                }

                if (url != null) {
                    azPanner = new AzimuthPanner.Shapefile(oX, oY, getAzDrawing(), url);
                } else {
                    azPanner = new AzimuthPanner.Standard(oX, oY, getAzDrawing());
                }
            }

            isPanning = true;
            mapBean.addPaintListener(this);
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     *      The first click for drag, the image is generated. This image is
     *      redrawing when the mouse is move, but, I need to repaint the
     *      original image.
     */
    public void mouseDragged(MouseEvent arg0) {

        MapBean mapBean = arg0.getSource() instanceof MapBean ? (MapBean) arg0.getSource() : null;
        lastMouseEvent = arg0;

        if (mapBean != null) {

            if (azPanner != null) {
                azPanner.handlePan(mapBean, arg0);
            }

            mapBean.repaint();
        }
        super.mouseDragged(arg0);
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     *      Make Pan event for the map.
     */
    public void mouseReleased(MouseEvent arg0) {

        MapBean mapBean = arg0.getSource() instanceof MapBean ? (MapBean) arg0.getSource() : null;

        if (azPanner != null) {
            azPanner.handleUnpan(arg0);
            azPanner = null;
        }

        if (isPanning && mapBean != null) {
            Projection proj = mapBean.getProjection();
            Point2D center = proj.forward(proj.getCenter());
            int x = (int) arg0.getX();
            int y = (int) arg0.getY();

            center.setLocation(center.getX() - x + oX, center.getY() - y + oY);

            isPanning = false; // needs to be here too so paintlistener doesn't
                               // get triggered
            mapBean.setCenter(mapBean.inverse(center.getX(), center.getY(), null));
        }

        oX = 0;
        oY = 0;
        isPanning = false;

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

    /**
     * @return the azPanningShapefile
     */
    public String getAzPanningShapefile() {
        return azPanningShapefile;
    }

    /**
     * @param azPanningShapefile the azPanningShapefile to set
     */
    public void setAzPanningShapefile(String azPanningShapefile) {
        this.azPanningShapefile = azPanningShapefile;
    }

    /**
     * @return the azDrawing
     */
    public DrawingAttributes getAzDrawing() {
        return azDrawing;
    }

    /**
     * @param azDrawing the azDrawing to set
     */
    public void setAzDrawing(DrawingAttributes azDrawing) {
        this.azDrawing = azDrawing;
    }

    public void projectionChanged(ProjectionEvent e) {
        Object obj = e.getSource();
        if (obj instanceof MapBean) {
            MapBean mb = (MapBean) obj;
            int w = mb.getWidth();
            int h = mb.getHeight();
            createBuffer(w, h);
        }
    }

    /**
     * Instantiates new image buffers if needed.<br>
     * This method is synchronized to avoid creating the images multiple times
     * if width and height doesn't change.
     * 
     * @param w mapBean's width.
     * @param h mapBean's height.
     */
    public synchronized void createBuffer(int w, int h) {
        if (w > 0 && h > 0 && (w != beanBufferWidth || h != beanBufferHeight)) {
            beanBufferWidth = w;
            beanBufferHeight = h;
            createBufferImpl(w, h);
        }
    }

    /**
     * Instantiates new image buffers.
     * 
     * @param w Non-zero mapBean's width.
     * @param h Non-zero mapBean's height.
     */
    protected void createBufferImpl(int w, int h) {
        // Release system resources used by previous images...
        if (bufferedMapImage != null) {
            bufferedMapImage.flush();
        }
        // New image...
        bufferedMapImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
}
