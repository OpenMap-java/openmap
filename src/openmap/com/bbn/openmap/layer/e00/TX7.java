// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/e00/TX7.java,v $
// $RCSfile: TX7.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.e00;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.bbn.openmap.omGraphics.OMGraphicAdapter;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Planet;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Description of the Class that displays a String along a polygon or a polyline
 * defined by lat lon points.
 * 
 * @author paricaud
 */
public class TX7 extends OMGraphicAdapter {
    double w = 1, angle = 0;
    LatLonPoint llp1 = new LatLonPoint.Double(),
            llp2 = new LatLonPoint.Double();
    Point pt1 = new Point(), pt2 = new Point();
    AffineTransform at = new AffineTransform();
    double llpoints[];
    String str;
    Font font = defaultFont;
    GlyphVector gv;
    // GeneralPath path;
    double distance;
    boolean badprojection;
    final static Font defaultFont = new Font("Arial", Font.PLAIN, 10);

    /**
     * Constructor for the TX7 object
     * 
     * @param llpoints
     *            array on lat lon lat lon ...
     * @param str
     *            Text
     * @param isRadian
     *            true if lat lons given in radians
     * @since
     */
    public TX7(double llpoints[], String str, boolean isRadian) {
        this(llpoints, str, isRadian, null);
    }

    /**
     * Constructor for the TX7 object
     * 
     * @param llpoints
     *            array on lat lon lat lon ...
     * @param str
     *            Text
     * @param isRadian
     *            true if lat lons given in radians
     * @param font
     *            font used to draw text
     * @since
     */
    public TX7(double llpoints[], String str, boolean isRadian, Font font) {
        if (str == null)
            this.str = " ";
        else
            this.str = str;
        if (font == null)
            this.font = defaultFont;
        else
            this.font = font;
        setLocation(llpoints, isRadian);
    }

    /**
     * Constructor for the TX7 object
     * 
     * @param llpoints
     *            array on lat lon lat lon ...
     * @param str
     *            Text *
     * @since
     */
    public TX7(double llpoints[], String str) {
        this(llpoints, str, true, null);
    }

    /**
     * Sets the text attribute of the TX7 object
     * 
     * @param S
     *            The new text value
     * @since
     */
    public void setText(String S) {
        str = S;
        compute();
    }

    /**
     * Sets the font attribute of the TX7 object
     * 
     * @param f
     *            The new font value
     * @since
     */
    public void setFont(Font f) {
        font = f;
        compute();
    }

    /**
     * Sets the location attribute of the TX7 object
     * 
     * @param llpoints
     *            array on lat lon lat lon ...
     * @param isRadian
     *            true if lat lons given in radians
     * @since
     */
    public void setLocation(double[] llpoints, boolean isRadian) {
        this.llpoints = llpoints;
        if (!isRadian) {
            float cv = (float) (Math.PI / 180.0);
            for (int i = 0; i < llpoints.length; i++)
                llpoints[i] *= cv;
        }
        compute();
    }

    /**
     * Gets the font attribute of the TX7 object
     * 
     * @return The font value
     * @since
     */
    public Font getFont() {
        return font;
    }

    /**
     * Gets the text attribute of the TX7 object
     * 
     * @return The text value
     * @since
     */
    public String getText() {
        return str;
    }

    /**
     * Gets the location attribute of the TX7 object
     * 
     * @return array on lat lon lat lon ... in radians
     * @since
     */
    public double[] getLocation() {
        return llpoints;
    }

    /**
     * generate with a new projection
     * 
     * @param proj
     *            new Projection
     * @return Description of the Returned Value
     * @since
     */
    public boolean generate(Projection proj) {
        proj.forward(llp1, pt1);
        proj.forward(llp2, pt2);
        double dx = pt2.x - pt1.x;
        double dy = pt2.y - pt1.y;
        at.setToTranslation(pt1.x, pt1.y);
        at.rotate(Math.atan2(dy, dx) - angle, 0, 0);
        double sc = Math.sqrt(dx * dx + dy * dy);
        badprojection = (Double.isNaN(sc) || sc / distance * proj.getScale() > 1000000);
        if (badprojection)
            Debug.message("e00", "badprojection " + str);
        else {
            sc /= w;
            at.scale(sc, sc);
        }
        return true;
    }

    /**
     * render
     * 
     * @param g
     *            Graphics
     * @since
     */
    public void render(Graphics g) {
        if (!visible)
            return;
        if (badprojection)
            return;
        g.setColor(Color.red);
        Graphics2D g2d = (Graphics2D) g;
        if (selected)
            g2d.setPaint(selectPaint);
        else
            g2d.setPaint(linePaint);
        AffineTransform saveAT = g2d.getTransform();
        // Perform transformation
        g2d.transform(at);
        // Render
        g2d.drawGlyphVector(gv, 0, 0);
        g.setColor(Color.blue);
        /*
         * if (path != null) { Stroke st = g2d.getStroke(); g2d.setStroke(new
         * BasicStroke(.3f)); g2d.draw(path); g2d.setStroke(st); }
         */
        // Restore original transform
        g2d.setTransform(saveAT);
    }

    /**
     * compute the glyphVector
     * 
     * @since
     */
    void compute() {
        double lt1;
        double ln1;
        double lt2;
        double ln2;
        FontRenderContext frc = new FontRenderContext(new AffineTransform(),
                true, true);
        gv = font.createGlyphVector(frc, str);
        Rectangle2D r = gv.getLogicalBounds();
        w = r.getWidth();
        angle = 0;
        int nseg = llpoints.length / 2 - 1;
        lt1 = llpoints[0];
        ln1 = llpoints[1];
        llp1.setLatLon(lt1, ln1, true);
        lt2 = llpoints[2 * nseg];
        ln2 = llpoints[2 * nseg + 1];
        llp2.setLatLon(lt2, ln2, true);
        distance = GreatCircle.sphericalDistance(lt1, ln1, lt2, ln2)
                * Planet.wgs84_earthEquatorialRadiusMeters;
        // System.out.println(nseg+" "+llp1+" "+llp2);
        setNeedToRegenerate(true);
        visible = false;

        double[] ds = new double[nseg];
        double[] az = new double[nseg];
        double[] cs = new double[nseg];
        int j = 2;
        double s = 0;
        double corr = 0;
        double dz;
        double az0 = 0;
        for (int i = 0; i < nseg; i++) {
            // if(j>llpoints.length-2){System.out.println(j+" "+i+"
            // "+nseg);nseg=1;break;}
            lt2 = (float) llpoints[j++];
            ln2 = (float) llpoints[j++];
            if (lt2 == lt1 && ln2 == ln1) {
                // suppress null segments
                i--;
                nseg--;
            } else {
                s += GreatCircle.sphericalDistance(lt1, ln1, lt2, ln2);
                ds[i] = s;
                az[i] = GreatCircle.sphericalAzimuth(lt1, ln1, lt2, ln2);
                if (i > 0) {
                    dz = (float) Math.tan((az[i] - az0) / 2);
                    if (dz < 0) {
                        cs[i - 1] = -dz;
                        corr -= 2 * dz;
                    }
                }
                az0 = az[i];
                lt1 = lt2;
                ln1 = ln2;
            }
        }
        if (nseg <= 1)
            return;
        // now try to play with little boxes
        // rotate them either on upper left corner or lower lef corner
        // probably can be simplified ...
        visible = true;
        LineMetrics lm = font.getLineMetrics("MM", frc);
        if (lm == null) {
            System.out.println("null metrics");
            return;
        }
        float h = (float) lm.getAscent();
        // System.out.println("ascent:" + h + " w:" + w + " s:" + s +
        // " corr:" + corr + " wc:" + (w - corr * h) + " " + str);
        corr = 0f;
        w -= corr * h;
        float sc = (float) (w / s);
        for (int i = 0; i < nseg; i++)
            ds[i] *= sc;
        int m = gv.getNumGlyphs();
        float[] gp = gv.getGlyphPositions(0, m, null);
        if (gp == null)
            System.out.println("gp null");
        // path = new GeneralPath();
        AffineTransform at;
        double dx;
        double dy;
        double x = 0;
        double y = 0;
        double xa;
        double ya;
        double s0 = 0;
        double ps;
        double s1;
        double s2;
        double theta;
        double theta2;
        double thetai;
        double dtheta;
        double ch0 = 0;
        double cos1;
        double sin1;
        double cos2;
        double sin2;
        j = 0;
        for (int i = 0; i < m; i++) {
            if (i == m - 1 || gp == null) {
                s = (float) w;
            } else {
                s = gp[2 * i + 2];
            }

            ps = s - s0;
            theta = az[j];
            cos1 = Math.cos(theta);
            sin1 = Math.sin(theta);
            float ch = (float) (cs[j] * h);
            if (s + ch0 < ds[j] - ch || j == nseg - 1) {
                xa = x;
                ya = y;
                x += ps * cos1;
                y += ps * sin1;
            } else {
                theta2 = az[j + 1];
                cos2 = Math.cos(theta2);
                sin2 = Math.sin(theta2);
                dtheta = theta2 - theta;
                s1 = ds[j] - ch - ch0 - s0;
                s2 = s1 * Math.sin(dtheta);
                s2 = ps * ps - s2 * s2;
                s2 = Math.sqrt(s2) - s1 * Math.cos(dtheta);
                dx = s1 * cos1 + s2 * cos2;
                dy = s1 * sin1 + s2 * sin2;
                thetai = Math.atan2(dy, dx);
                if (ch == 0) {
                    xa = x;
                    ya = y;
                } else {
                    dx += ch * cos1 + ch * cos2;
                    dy += ch * sin1 + ch * sin2;
                    xa = x + h * sin1 - h * Math.sin(thetai);
                    ya = y - h * cos1 + h * Math.cos(thetai);
                }
                x += dx;
                y += dy;
                j++;
                ch0 = ch;
                theta = thetai;
            }

            gv.setGlyphPosition(i, new Point2D.Double(xa, ya));
            if (theta != 0) {
                at = new AffineTransform();
                at.rotate(theta);
                gv.setGlyphTransform(i, at);
            }
            s0 = s;
            /*
             * path.moveTo((float) xa, (float) ya); xa += ps * Math.cos(theta);
             * ya += ps * Math.sin(theta); path.lineTo((float) xa, (float) ya);
             * xa += h * Math.sin(theta); ya -= h * Math.cos(theta);
             * path.lineTo((float) xa, (float) ya); xa -= ps * Math.cos(theta);
             * ya -= ps * Math.sin(theta); path.lineTo((float) xa, (float) ya);
             * path.closePath();
             */
        }
        angle = Math.atan2(y, x);
        w = Math.sqrt(x * x + y * y);

    }
}
