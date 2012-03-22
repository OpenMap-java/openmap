/* **********************************************************************
 * 
 *  Ranching Systems Group
 *  Texas Agricultural Experiment Station
 *  Department of Rangeland Ecology and Management
 *  Mail Stop 212
 *  College Station, TX 77801
 * 
 *  Copyright (C) 2000
 *  This software is subject to copyright protection under the laws of 
 *  the United States and other countries.
 * 
 * **********************************************************************
 *   Fri Feb  4 10:20:34 CST 2000 Dan Schmitt (creation)
 * **********************************************************************
 */

package com.bbn.openmap.layer.nexrad;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

/**
 * Implement a Layer that displays nexrad rainfall data. <code><pre>
 * 
 *  # zero value color (ARGB)
 *  nexrad.color.bg=ffb3b3b3
 *  # max value color (ARGB)
 *  nexrad.color.fg=ff000000
 *  # max rainfall value
 *  nexrad.rain.max=6000
 *  # min rainfall value
 *  nexrad.rain.min=0
 *  # URL for data file
 *  nexrad.url=http://a.b.gov/file
 *  
 * </pre></code>
 * <p>
 * NOTE: the color properties do not support alpha value if running on JDK 1.1.
 */
public class NexradLayer extends OMGraphicHandlerLayer {

    // property keys
    public final static transient String plotColorProperty = ".color.plot";
    public final static transient String fontProperty = ".font";
    public final static transient String maxRainProperty = ".rain.max";
    public final static transient String minRainProperty = ".rain.min";
    public final static transient String dataURLProperty = ".url";

    // properties
    protected Color plotColor = new Color(0, 0, 200);
    protected int maxRain = 1000;
    protected int minRain = 0;
    protected URL dataURL = null;
    protected Font legendFont = Font.decode("SanSerif");

    /**
     * Construct the DateLayer.
     */
    public NexradLayer() {
        setName("Nexrad");
    }

    /**
     * Sets the properties for the <code>Layer</code>.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        plotColor = ColorFactory.parseColorFromProperties(props, prefix
                + plotColorProperty, "0000C8");

        String tmpMaxRain = props.getProperty(prefix + maxRainProperty);
        if (tmpMaxRain != null) {
            try {
                maxRain = Integer.valueOf(tmpMaxRain).intValue();
            } catch (NumberFormatException e) {
                maxRain = 1000;
            }
        }

        String tmpMinRain = props.getProperty(prefix + minRainProperty);
        if (tmpMinRain != null) {
            try {
                minRain = Integer.valueOf(tmpMinRain).intValue();
            } catch (NumberFormatException e) {
                minRain = 0;
            }
        }
        try {
            dataURL = new URL(props.getProperty(prefix + dataURLProperty));
            loadData(dataURL);
        } catch (java.net.MalformedURLException e) {
            dataURL = null;
        }

        legendFont = Font.decode(props.getProperty(prefix + fontProperty));

    }

    public void loadData(URL theDataStream) {
        try {
            BufferedReader f = new BufferedReader(new InputStreamReader(theDataStream.openStream()));
            StringTokenizer tok = new StringTokenizer(f.readLine());
            int ulhrapx = (new Integer(tok.nextToken())).intValue();
            int ulhrapy = (new Integer(tok.nextToken())).intValue();
            int maxx = (new Integer(tok.nextToken())).intValue();
            int maxy = (new Integer(tok.nextToken())).intValue();
            int rain[][] = new int[maxx][maxy];

            if (Debug.debugging("nexrad")) {
                Debug.output("NexradLayer: Reading " + theDataStream + " "
                        + maxx + " " + maxy);
            }
            for (int any = 0; any < maxy; any++) {
                tok = new StringTokenizer(f.readLine());
                for (int anx = 0; anx < maxx; anx++) {
                    rain[anx][any] = (new Integer(tok.nextToken())).intValue();
                }
            }
            f.close();
            if (Debug.debugging("nexrad")) {
                Debug.output("NexradLayer: Completed " + theDataStream + " "
                        + maxx + " " + maxy);
            }
            setList(createGraphics(ulhrapx, ulhrapy, maxx, maxy, rain));
            // } catch (java.io.IOException oops) {
        } catch (Exception oops) {
            Debug.error("NexradLayer.loadData: Failed to read " + theDataStream);
            oops.printStackTrace();
        }
    }

    public OMGraphicList createGraphics(int ulhrapx, int ulhrapy, int xcount,
                                        int ycount, int rain[][]) {

        OMGraphicList graphics = new OMGraphicList();

        double ul[] = { 0, 0 };
        double ur[] = { 0, 0 };
        double ll[] = { 0, 0 };
        double lr[] = { 0, 0 };

        for (int x = 0; x < xcount; x++) {
            ll = hrap2lonlat(ulhrapx + x, ulhrapy);
            lr = hrap2lonlat(ulhrapx + x + 1, ulhrapy);
            for (int y = 0; y < ycount; y++) {
                ul = ll;
                ur = lr;
                ll = hrap2lonlat(ulhrapx + x, ulhrapy + y + 1);
                lr = hrap2lonlat(ulhrapx + x + 1, ulhrapy + y + 1);
                if (rain[x][y] > 0) {

                    if (Debug.debugging("nexrad")) {
                        Debug.output("NexradLayer: Rain " + rain[x][y] + " "
                                + x + " " + y);
                    }

                    double polypoints[] = { ul[0], ul[1], ur[0], ur[1], lr[0],
                            lr[1], ll[0], ll[1], ul[0], ul[1] };
                    OMPoly poly = new OMPoly(polypoints, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
                    Color plotc = scaledColor(rain[x][y]);
                    poly.setFillPaint(plotc);
                    poly.setLinePaint(plotc);
                    graphics.add(poly);
                }
            }
        }
        return graphics;
    }

    /**
     * Paints the layer.
     * 
     * @param g the Graphics context for painting
     */
    public void paint(Graphics g) {
        super.paint(g);
        plotScale(g);
    }

    public void plotScale(Graphics g) {
        g.setColor(Color.red);
        int nameMin = 1;
        Font tmpFont = g.getFont();
        g.setFont(legendFont);
        g.drawString(maxRain + " mm", 30, 30);
        g.drawString(nameMin + " mm", 30, 450);
        g.setFont(tmpFont);
        int range = 430;
        for (int k = 0; k <= range; k += 5) {
            g.setColor(scaledColor(minRain + (k * (maxRain - minRain) / range)));
            g.fillRect(10, 450 - k, 10, 5);
        }
    }

    public Color scaledColor(int value) {
        int alphaValue = 15 * value / maxRain;
        if (alphaValue > 15)
            alphaValue = 15;
        if (alphaValue < 0)
            alphaValue = 0;
        alphaValue *= 10;
        alphaValue += 55;
        return (new Color(plotColor.getRed(), plotColor.getGreen(), plotColor.getBlue(), alphaValue));
    }

    public double[] hrap2lonlat(int xhrap, int yhrap) {
        float mesh = 4762.5f;
        float earthr = 6371200.0f;
        float stlond = -105.0f;
        float stlatd = 60.0f;
        float x = (xhrap - 401.0f) * mesh;
        float y = (yhrap - 1601.0f) * mesh;
        float arg = (float) (Math.pow(Math.pow(x, 2.0) + Math.pow(y, 2.0), 0.5) / (earthr * (1 + Math.sin(Math.toRadians(stlatd)))));
        float latd = 90.0f - 2.0f * (float) Math.toDegrees(Math.atan(arg));
        float ang = (float) Math.toDegrees(Math.atan2(y, x));
        if (y > 0) {
            ang = 270.0f - stlond - ang;
        } else {
            ang = -90.0f - stlond - ang;
        }
        float lond = 0f;
        if (ang < 180) {
            lond = -1.0f * ang;
        } else {
            lond = 360.0f - ang;
        }
        double res[] = { latd, lond };
        return res;
    }
}