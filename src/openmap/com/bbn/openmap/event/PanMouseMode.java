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

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * PanMouseMode it is a class for Pan operation on the visible map.
 * This class show actual map in transparent mode. 25-feb-2005
 * 
 * @author cursor
 */
public class PanMouseMode extends CoordMouseMode {

    public final static transient String modeID = "Pan";
    private boolean isPanning = false;

    private Point2D llp = null;
    private BufferedImage img = null;
    private MapBean mb = null;
    private int oX, oY;

    // private Cursor myPointer;

    public PanMouseMode() {
        super(modeID, true);

        setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        /*
         * For who like make his CustomCursor
         * 
         * Toolkit tk = Toolkit.getDefaultToolkit(); ImageIcon pointer =
         * new ImageIcon(getClass().getResource("/icons/pan.gif"));
         * myPointer= tk.createCustomCursor(pointer.getImage(), new
         * Point(0,0), "PP"); setModeCursor(myPointer);
         */

    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     *      The first click for drag, the image is generated. This
     *      image is redrawing when the mouse is move, but, I need to
     *      repain the original image.
     */
    public void mouseDragged(MouseEvent arg0) {

        int x = arg0.getX();
        int y = arg0.getY();

        mb = ((MapBean) arg0.getSource());
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

            isPanning = true;
            oX = x;
            oY = y;
            llp = mb.getProjection().inverse(x, y);
            gr2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    0.5f));
            gr2d.drawImage(img, 0, 0, null);

        } else {
            if (img != null) {

                /*
                 * Drawing original image whithout transparence and in
                 * the initial position
                 */
                gr2d.drawImage(img, 0, 0, null);
                /*
                 * Drawing image whith transparence and in the mouse
                 * position minus origianl mouse click position
                 */
                gr2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        0.5f));
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
        int x, y;
        if (isPanning && arg0.getSource() instanceof MapBean) {
            mb = (MapBean) arg0.getSource();

            x = arg0.getX();
            y = arg0.getY();
            Point2D lastLlp = ((MapBean) arg0.getSource()).getProjection()
                    .inverse(x, y);
            if (lastLlp instanceof LatLonPoint && llp instanceof LatLonPoint) {
                float az = (float) Math.toDegrees(((LatLonPoint)lastLlp).azimuth((LatLonPoint)llp));
                float dist = (float) Math.toDegrees(((LatLonPoint)lastLlp).distance((LatLonPoint)llp));
                mb.pan(new PanEvent(mb, az, dist));
            }
            isPanning = false;
        }
        super.mouseReleased(arg0);
    }

}
