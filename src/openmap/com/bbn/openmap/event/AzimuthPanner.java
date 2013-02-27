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

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
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
public abstract class AzimuthPanner implements PaintListener {
    OMGraphicList omGraphics;
    int oX, oY;
    MapBean mapBean;
    DrawingAttributes azDrawing;

    private AzimuthPanner(MapBean mb, int oX, int oY, DrawingAttributes azDrawing) {
        this.oX = oX;
        this.oY = oY;
        this.mapBean = mb;
        this.azDrawing = azDrawing;
    }

    public abstract void handlePan(MouseEvent me);

    public abstract void handleUnpan(MouseEvent me);

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
     * @see com.bbn.openmap.event.PaintListener#listenerPaint(java.awt.Graphics)
     */
    public void listenerPaint(Graphics graphics) {
        if (omGraphics != null) {
            omGraphics.render(graphics);
        }
    }

    public static class Standard extends AzimuthPanner {

        public Standard(MapBean mb, int oX, int oY, DrawingAttributes azDrawing) {
            super(mb, oX, oY, azDrawing);
        }

        public void handlePan(MouseEvent me) {
            Projection proj = mapBean.getProjection();
            Point2D center = proj.getCenter();
            Point2D centerXY = proj.forward(center);
            Point2D pnt = mapBean.getNonRotatedLocation(me);

            int x = (int) centerXY.getX() - (int) pnt.getX() + oX;
            int y = (int) centerXY.getY() - (int) pnt.getY() + oY;

            Point2D llp = proj.inverse(x, y);

            if (llp != null && !llp.equals(center)) {
                OMGraphicList list = new OMGraphicList();
                OMLine line1 = new OMLine(90, llp.getX(), llp.getY(), llp.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
                OMLine line2 = new OMLine(llp.getY(), llp.getX(), -90, llp.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
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

                OMLine line6 = new OMLine(center.getY(), center.getX(), llp.getY(), llp.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
                line6.addArrowHead(true);
                list.add(line6);

                getAzDrawing().setTo(list);

                omGraphics = list;
                list.generate(mapBean.getProjection());

                mapBean.addPaintListener(Standard.this);
                mapBean.repaint();
            }
        }

        public void handleUnpan(MouseEvent me) {
            if (mapBean != null) {
                mapBean.removePaintListener(Standard.this);
                omGraphics = null;
            }
        }

    }

    public static class Shapefile extends Standard {
        EsriGraphicList list = null;

        public Shapefile(MapBean mb, int oX, int oY, DrawingAttributes azDrawing, URL shapefile) {
            super(mb, oX, oY, azDrawing);
            list = EsriGraphicList.getEsriGraphicList(shapefile, getAzDrawing(), null);
        }

        public void handlePan(MouseEvent me) {
            Projection proj = mapBean.getProjection();
            Point2D center = proj.getCenter();
            Point2D centerXY = proj.forward(center);
            Point2D pnt = mapBean.getNonRotatedLocation(me);

            int x = (int) centerXY.getX() - (int) pnt.getX() + oX;
            int y = (int) centerXY.getY() - (int) pnt.getY() + oY;

            Point2D llp = proj.inverse(x, y);

            Proj newProj = (Proj) proj.makeClone();
            newProj.setCenter(llp);

            if (llp != null && !llp.equals(center) && list != null) {
                omGraphics = list;
                list.generate(newProj);

                mapBean.addPaintListener(Shapefile.this);
                mapBean.repaint();
            }
        }

    }
}
