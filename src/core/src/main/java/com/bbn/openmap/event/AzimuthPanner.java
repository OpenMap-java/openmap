/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.event;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URL;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;

/**
 * AzPanner is an abstract class that handles panning effects for Azimuth
 * projections.
 * 
 * @author dietrick
 */
public abstract class AzimuthPanner {
    OMGraphicList omGraphics;
    int oX, oY;
    DrawingAttributes azDrawing;

    private AzimuthPanner(int oX, int oY, DrawingAttributes azDrawing) {
        this.oX = oX;
        this.oY = oY;
        this.azDrawing = azDrawing;
    }

    public abstract void handlePan(MapBean map, MouseEvent me);

    public abstract void handleUnpan(MouseEvent me);

    public void render(Graphics g) {
        if (omGraphics != null) {
            omGraphics.render(g);
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see com.bbn.openmap.event.PaintListener#listenerPaint(Object source,
     * java.awt.Graphics)
     */
    public void listenerPaint(Object source, Graphics graphics) {
        if (omGraphics != null) {
            omGraphics.render(graphics);
        }
    }

    public static class Standard extends AzimuthPanner {

        public Standard(int oX, int oY, DrawingAttributes azDrawing) {
            super(oX, oY, azDrawing);
        }

        public void handlePan(MapBean mapBean, MouseEvent me) {

            Point2D pnt0 = mapBean.inverse(oX, oY, null);
            Point2D llp = mapBean.inverse(me.getX(), me.getY(), null);

            if (llp != null && !llp.equals(pnt0)) {
                OMGraphicList list = new OMGraphicList();
                OMLine line1 = new OMLine(89.9, llp.getX(), llp.getY(), llp.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
                OMLine line2 = new OMLine(llp.getY(), llp.getX(), -89.9, llp.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
                list.add(line1);
                list.add(line2);
                if (llp.getX() < 0) {
                    OMLine line3 = new OMLine(llp.getY(), -180, llp.getY(), llp.getX(), OMGraphic.LINETYPE_RHUMB);
                    OMLine line4 = new OMLine(llp.getY(), llp.getX(), llp.getY(), 0, OMGraphic.LINETYPE_RHUMB);
                    OMLine line5 = new OMLine(llp.getY(), 0, llp.getY(), 179.9, OMGraphic.LINETYPE_RHUMB);
                    list.add(line3);
                    list.add(line4);
                    list.add(line5);
                } else {
                    OMLine line3 = new OMLine(llp.getY(), 0, llp.getY(), llp.getX(), OMGraphic.LINETYPE_RHUMB);
                    OMLine line4 = new OMLine(llp.getY(), llp.getX(), llp.getY(), 180, OMGraphic.LINETYPE_RHUMB);
                    OMLine line5 = new OMLine(llp.getY(), -179, llp.getY(), 0, OMGraphic.LINETYPE_RHUMB);
                    list.add(line3);
                    list.add(line4);
                    list.add(line5);
                }

                OMLine line6 = new OMLine(pnt0.getY(), pnt0.getX(), llp.getY(), llp.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
                line6.addArrowHead(true);
                list.add(line6);

                getAzDrawing().setTo(list);
                omGraphics = list;

                list.generate(mapBean.getRotatedProjection());

                mapBean.repaint();
            }
        }

        public void handleUnpan(MouseEvent me) {
            omGraphics = null;
        }

    }

    public static class Shapefile extends Standard {
        EsriGraphicList list = null;

        public Shapefile(int oX, int oY, DrawingAttributes azDrawing, URL shapefile) {
            super(oX, oY, azDrawing);
            list = EsriGraphicList.getEsriGraphicList(shapefile, getAzDrawing(), null);
        }

        public void handlePan(MapBean mapBean, MouseEvent me) {

            Projection proj = mapBean.getRotatedProjection();
            Point2D pnt0 = mapBean.inverse(oX, oY, null);
            Point2D pnt = mapBean.inverse(me.getX(), me.getY(), null);

            Geo g0 = new Geo(pnt0.getY(), pnt0.getX());
            Geo g = new Geo(pnt.getY(), pnt.getX());

            double distance = g0.distance(g);
            double az = g.azimuth(g0);

            Point2D c = proj.getCenter();
            Geo ngc = new Geo(c.getY(), c.getX()).offset(distance, az);

            Proj newProj = (Proj) mapBean.getProjectionFactory().makeProjection(proj.getClass().getName(), proj);
            newProj.setRotationAngle(proj.getRotationAngle());
            newProj.setCenter(ngc.getLatitude(), ngc.getLongitude());
            
            if (list != null) {
                OMGraphicList newList = new OMGraphicList(list);
                newList.generate(newProj);
                omGraphics = newList;
                mapBean.repaint();
            }
        }

    }
}
