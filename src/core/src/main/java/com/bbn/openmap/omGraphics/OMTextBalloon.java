/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import com.bbn.openmap.proj.Projection;

/**
 * A simple OMText object that acts like a pretty information balloon, complete
 * with little pointer to actual location. Fill color is balloon color. The
 * balloon will move to the center of the map based on the location of the point
 * referenced.
 *
 * @author dietrick
 */
public class OMTextBalloon extends OMText {

    Point2D llLoc;

    /**
     * Can only make these tied to a lat/lon point, internally, they will be
     * LATLON_OFFSET OMTexts.
     * 
     * @param lt
     * @param ln
     * @param stuff
     * @param just
     */
    public OMTextBalloon(double lt, double ln, String stuff, int just) {
        super(lt, ln, 0, 0, stuff, just);
    }

    /**
     * Can only make these tied to a lat/lon point, internally, they will be
     * LATLON_OFFSET OMTexts.
     * 
     * @param lt
     * @param ln
     * @param stuff
     * @param font
     * @param just
     */
    public OMTextBalloon(double lt, double ln, String stuff, Font font, int just) {
        super(lt, ln, 0, 0, stuff, font, just);
        setUseMaxWidthForBounds(true);
        setBaseline(BASELINE_TOP);
    }

    /**
     * Override this to figure out the offset to use for positioning based on
     * screen location of lat/lon
     */
    public boolean generate(Projection proj) {
        llLoc = setOffsets(proj);
        // Special handling for the first rendering, before font metrics are
        // known. You'll see further on down.
        if (fm == null) {
            putAttribute(OMGraphic.APP_OBJECT, proj);
        }
        return super.generate(proj);
    }

    /**
     * Set the offset points based on relative screen location of lat/lon point
     * after projection.
     * 
     * @param proj current projection
     * @return the current projected location for use in render.
     */
    protected Point2D setOffsets(Projection proj) {
        Point2D loc = proj.forward(lat, lon);

        double x = loc.getX();
        double y = loc.getY();
        boolean west = x <= proj.getWidth() / 2;
        boolean north = y <= proj.getHeight() / 2;

        Polygon p = getPolyBounds();
        Point newPt = new Point();
        if (p != null) {
            Rectangle r = p.getBounds();

            if (west) {
                newPt.x = (int) (-r.width * .25);
            } else {
                newPt.x = (int) (-r.width * .75);
            }

            if (north) {
                newPt.y = (int) (YOFFSET);
            } else {
                newPt.y = -(int) (YOFFSET + r.height);
            }

            point = newPt;
        }
        return loc;
    }

    int BBUFFER = 10;
    int DBBUFFER = BBUFFER * 2;
    int XOFFSET = 30;
    int YOFFSET = 50;

    /**
     * Special handling needed for first render, if font metrics aren't known
     * yet.
     */
    public void prepareForRender(Graphics g) {
        boolean doCheck = fm == null;
        super.prepareForRender(g);
        if (doCheck) {
            // First render, need to reset projected location.
            Object obj = getAttribute(OMGraphic.APP_OBJECT);
            if (obj instanceof Projection) {
                setOffsets((Projection) obj);
                generate((Projection) obj);
                removeAttribute(OMGraphic.APP_OBJECT);
            }
        }
    }

    public void render(Graphics g) {

        // This is to allow the polybounds to work on the first paint.
        prepareForRender(g);

        Polygon polyBounds = getPolyBounds();
        if (polyBounds != null) {
            Rectangle r = polyBounds.getBounds();

            // Shadow
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0x66000000, true));
            g2.setTransform(AffineTransform.getTranslateInstance(3, 3));
            g2.fillRoundRect(r.x - BBUFFER, r.y - BBUFFER, r.width + DBBUFFER, r.height + DBBUFFER, DBBUFFER, DBBUFFER);

            if (llLoc != null) {
                g2.fillPolygon(getPointer(r));
            }
            g2.dispose();
            //

            setGraphicsForFill(g);
            ((Graphics2D) g).fillRoundRect(r.x - BBUFFER, r.y - BBUFFER, r.width + DBBUFFER, r.height
                    + DBBUFFER, DBBUFFER, DBBUFFER);
            if (llLoc != null) {
                ((Graphics2D) g).fillPolygon(getPointer(r));
            }
        }

        super.render(g);
    }

    /**
     * Create simple triangle to point to actual location.
     * 
     * @param r bounding rectangle of text
     * @return Polygon to fill with matching color of balloon.
     */
    protected Polygon getPointer(Rectangle r) {
        int centerX = r.x + r.width / 2;
        int centerY = r.y + r.height / 2;
        int[] xPoints = new int[] { (int) llLoc.getX(), centerX - r.width / BBUFFER,
                centerX + r.width / BBUFFER, (int) llLoc.getX() };
        int[] yPoints = new int[] { (int) llLoc.getY(), centerY, centerY, (int) llLoc.getY() };

        return new Polygon(xPoints, yPoints, 4);
    }

}
