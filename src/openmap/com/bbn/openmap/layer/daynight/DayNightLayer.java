// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/daynight/DayNightLayer.java,v $
// $RCSfile: DayNightLayer.java,v $
// $Revision: 1.5 $
// $Date: 2004/01/26 18:18:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.daynight;


/*  Java Core  */
import java.awt.Point;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import java.net.URL;

/*  OpenMap  */
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;
import com.bbn.openmap.util.CSVTokenizer;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.omGraphics.*;

import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.Box;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** 
 * The DayNightLayer is a layer that draws the day/Night terminator
 * on the map.  When the layer is re-projected, it figures out the
 * brightest point on the earth (closest to the sun), and creates an
 * image that has daytime pixels clear and the nighttime pixels
 * shaded.  There are a couple of options available for the layer.
 * The terminator can be faded from light to dark, and the width of
 * the fading can be adjusted.  The color of the shading can be
 * changed.  The shading can reflect the current time, or be set to
 * display the shading of a specified time.  A time interval can be
 * set to have the layer automatically update at regular intervals.
 *
 * <P>The openmap.properties file can control the layer with the
 * following settings:
 * <code><pre>
 * # These are all optional, and can be omitted if you want to use the defaults.
 * # draw terminator as poly (faster calculation than image,
 * # defaults to true).
 * daynight.doPolyTerminator=true
 * # number of vertices for polygon terminator line.  this is only valid
 * # if doPolyTerminator is true...
 * daynight.terminatorVerts=360
 * # termFade - the distance of the transition of fade, as a percentage of PI.
 * daynight.termFade=.1
 * # currentTime - true to display the shading at the computer's current time.
 * daynight.currentTime=true
 * # overlayTime - time, in milliseconds from java/unix epoch, to set the layer
 * # time being displayed.  currentTime has to be false for this to be used.
 * daynight.overlayTime=919453689000
 * # updateInterval - time in milliseconds between updates.  currentTime has to be
 * # true for this to be used.
 * daynight.updateInterval=300000
 * # Color of the shading (32bit Hex ARGB)
 * daynight.nighttimeColor=64000000
 * </pre></code>
 * In addition, you can get this layer to work with the OpenMap viewer
 * by editing your openmap.properties file:
 * <code><pre>
 * # layers
 * openmap.layers=daynight ...
 * # class
 * daynight.class=com.bbn.openmap.layer.daynight.DayNightLayer
 * # name
 * daynight.prettyName=Day/Night Shading
 * </pre></code>
 *
 */
public class DayNightLayer extends OMGraphicHandlerLayer 
    implements ProjectionListener, ActionListener {
    /**
     * Default value of fade to the terminator line, set to .10f.
     * This means that the last 10% of the horizon will be faded
     * out. 
     */
    public static final transient float DEFAULT_TERM_FADE = .10f;
    /** Default update interval, which is never - updates occur on re-projections. */
    public static final transient int DO_NOT_UPDATE = -1;
    /** The color of daytime - default is white and clear. */
    protected Color daytimeColor;
    /** Default color string for daytime */
    protected String defaultDaytimeColorString = "00FFFFFF";
    /** the color of darkness - default is black. */
    protected Color nighttimeColor;
    /** Default color string for nighttime */
    protected String defaultNighttimeColorString = "7F000000";
    /**
     * Percentage of the distance from the horizon to the brightest
     * point to start fading to darkness. Expected to be between 0.0 and 0.5.
     */
    protected float termFade = DEFAULT_TERM_FADE;
    /**
     * If true, the layer will set the darkness according to the
     * current time. 
     */
    protected boolean currentTime = true;
    /** The time used to create the layer, in milliseconds from java/unix epoch. */
    protected long overlayTime;
    /** Update interval to automatically update the layer, in milli-seconds */
    protected int updateInterval = 300000;
    /** Update timer. */
    protected Timer timer;

    /**
     * Create the terminator line as a polygon.
     */
    protected boolean doPolyTerminator = true;

    /**
     * The number of vertices of the polygon terminator line.
     */
    protected int terminatorVerts = 360;

    /////// Properties
    public static final transient String DaytimeColorProperty = ".daytimeColor";
    public static final transient String NighttimeColorProperty = ".nighttimeColor";
    public static final transient String TermFadeProperty = ".termFade";
    public static final transient String CurrentTimeProperty = ".useCurrentTime";
    public static final transient String OverlayTimeProperty = ".overlayTime";
    public static final transient String UpdateIntervalProperty = ".updateInterval";
    public static final transient String DoPolyTerminatorProperty = ".doPolyTerminator";
    public static final transient String TerminatorVertsProperty = ".terminatorVerts";

    /** 
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     */
    public DayNightLayer () {}

    /** 
     * The properties and prefix are managed and decoded here, for
     * the standard uses of the DayNightLayer.
     *
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.  
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);

        String termFadeString = properties.getProperty(prefix + TermFadeProperty);
        String currentTimeString = properties.getProperty(prefix + CurrentTimeProperty);
        String overlayTimeString = properties.getProperty(prefix + OverlayTimeProperty);
        String updateIntervalString = properties.getProperty(prefix + 
                                                             UpdateIntervalProperty);
        
        if (currentTimeString != null)
            currentTime = Boolean.valueOf(currentTimeString).booleanValue();
        

        // If something stupid gets passed in here, just us the current time...
        try {
            if (overlayTimeString != null)
                overlayTime = Long.valueOf(overlayTimeString).longValue();
            if (overlayTime <= 0){
                currentTime = true;
            }
        } catch (NumberFormatException e) {
            System.err.println("DayNightLayer: Unable to parse " + 
                               OverlayTimeProperty +
                               " = " + overlayTimeString);
            currentTime = true;
        }

        try {
            if (updateIntervalString != null)
                updateInterval = Integer.valueOf(updateIntervalString).intValue();
            if (updateInterval <= 0){
                updateInterval = DO_NOT_UPDATE;
                System.err.println("DayNightLayer: Not updating display.");
            } else {
                timer = new Timer(updateInterval, this);
            }
        } catch (NumberFormatException e) {
            System.err.println("DayNightLayer: Unable to parse " + 
                               UpdateIntervalProperty +
                               " = " + updateIntervalString);
            updateInterval = DO_NOT_UPDATE;
        }

        try {
            if (termFadeString != null)
                termFade = Float.valueOf(termFadeString).floatValue();
            else termFade = DEFAULT_TERM_FADE;

            if (termFade < 0 || termFade >= .5){
                System.err.println("DayNightLayer: termFade funky value ignored.");
                termFade = DEFAULT_TERM_FADE;
            }
        } catch (NumberFormatException e) {
            System.err.println("DayNightLayer: Unable to parse " + 
                               TermFadeProperty +
                               " = " + termFadeString);
            termFade = DEFAULT_TERM_FADE;
        }

        daytimeColor = ColorFactory.parseColorFromProperties(
            properties,
            prefix + DaytimeColorProperty,
            defaultDaytimeColorString, true);

        nighttimeColor = ColorFactory.parseColorFromProperties(
            properties,
            prefix + NighttimeColorProperty,
            defaultNighttimeColorString, true);

        doPolyTerminator = Boolean.valueOf(properties.getProperty(
                    prefix + DoPolyTerminatorProperty, ""+doPolyTerminator)).booleanValue();

        try {
            terminatorVerts = Integer.parseInt(
                    properties.getProperty(
                        prefix+TerminatorVertsProperty, ""+terminatorVerts));
        } catch (NumberFormatException e) {
            System.err.println("DayNightLayer: Unable to parse " + 
                               TerminatorVertsProperty);
        }
    }

    /**
     * Handle an ActionEvent from the Timer.
     * @param ae action event from the timer.
     */
    public void actionPerformed(java.awt.event.ActionEvent ae){
        super.actionPerformed(ae);
        if (Debug.debugging("daynight")) {
            Debug.output(getName()+"| updating image via timer...");
        }
        doPrepare();
    }

    /**
     * Create the OMGraphic that acts as an overlay showing the
     * day/night terminator.  The brightest spot on the earth is
     * calculated, and then each pixel is inverse projected to find
     * out its coordinates.  Then the great circle distance is
     * calculated.  The terminator is assumed to be the great circle
     * where all the points are PI/2 away from the bright point. If
     * the termFade variable is set, then the difference in color over
     * the terminator is feathered, on equal amount of the terminator.
     *
     * @param projection the projection of the screen,
     * @return OMGraphic containing image to use for the layer.  The
     * image has been projected.
     */
    protected OMGraphic createImage(Projection projection) {

        if (currentTime) overlayTime = System.currentTimeMillis();

        if (Debug.debugging("daynight")) {
            Debug.output("DayNightLayer: Calculating sun position at time " +
                         Long.toString(overlayTime));
        }

        LatLonPoint brightPoint = SunPosition.sunPosition(overlayTime);

        Debug.message("daynight", "DayNightLayer: Calculated sun position");

        // Do a fast and relatively inexpensive calculation of the
        // terminator.  NOTE: for non-cylindrical projections we don't
        // create a full-hemisphere circle so that we don't get
        // flip-rendering problem...
        if (doPolyTerminator) {
            Debug.message("daynight", "DayNightLayer:  Creating polygon terminator");
            LatLonPoint darkPoint = GreatCircle.spherical_between(
                    brightPoint.radlat_,
                    brightPoint.radlon_,
                    (float)Math.PI,
                    (float)Math.PI/4f);
            OMCircle circle = new OMCircle(
                    darkPoint,
                    (projection instanceof Cylindrical) ? 90f : 89.0f,//HACK
                    Length.DECIMAL_DEGREE, terminatorVerts);
            circle.setPolarCorrection(true);
            circle.setFillPaint(nighttimeColor);
            circle.setLinePaint(nighttimeColor);
            circle.generate(projection);
            Debug.message("daynight", "DayNightLayer: Done creating polygon terminator");
            return circle;
        }

        int width = projection.getWidth();
        int height = projection.getHeight();
        int[] pixels = new int[width*height];

        OMRaster ret = new OMRaster((int)0, (int)0, width, height, pixels);



        float lat, lon;

        Debug.message("daynight", getName()+
                      "|createImage: Center of bright spot lat= " + 
                      brightPoint.getLatitude() + 
                      ", lon= " + brightPoint.getLongitude());

        // Light is clear and/or white  
        int light = daytimeColor.getRGB();

        // Allocate the memory here for the testPoint
        LatLonPoint testPoint = new LatLonPoint(0f, 0f);
        // great circle distance between the bright point and each pixel.
        float distance; 

        //  Set the darkeness value
        int dark = nighttimeColor.getRGB();// ARGB
        int darkness = dark >>> 24;// darkness alpha
        int value;

        // Calculate the fae limits around the terminator
        float upperFadeLimit =  (float)(MoreMath.HALF_PI*(1.0+termFade));
        float lowerFadeLimit =  (float)(MoreMath.HALF_PI*(1.0-termFade));
        int fadeColorValue = 0x00FFFFFF & (dark); // RGB

        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){

                testPoint = projection.inverse(i, j, testPoint);
                distance = GreatCircle.spherical_distance(brightPoint.radlat_, 
                                                          brightPoint.radlon_,
                                                          testPoint.radlat_, 
                                                          testPoint.radlon_);

                if (distance > upperFadeLimit) {
                    pixels[j*width+i] = dark;
                } else if (distance > lowerFadeLimit){
                    value = (int)(darkness * (1 - ((upperFadeLimit - distance)/
                                                   (upperFadeLimit - lowerFadeLimit))));
                    value <<= 24;
                    pixels[j*width+i] = fadeColorValue | value;
                } else {
                    pixels[j*width+i] = light;
                }
            }
        }

        ret.generate(projection);
        return ret;
    }

    /**
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the location.  <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the map will set the cancel bit in the
     * LayerThread, (the thread that is running the prepare).  If this
     * Layer needs to do any cleanups during the abort, it should do
     * so, but return out of the prepare asap.
     *
     */
    public OMGraphicList prepare() {

        OMGraphicList list = getList();
        if (list == null) {
            list = new OMGraphicList();
        } else {
            list.clear();
        }

        if (isCancelled()) {
            Debug.message("daynight", getName()+
                          "|DayNightLayer.prepare(): aborted.");
            return null;
        }

        Debug.message("basic", getName()+"|DayNightLayer.prepare(): doing it");

        OMGraphic ras = createImage(getProjection());
        if (timer != null) timer.restart();
        list.add(ras);

        return list;
    }

    /**
     * Get the time of the overlay.
     */
    public long getOverlayTime() {
        return overlayTime;
    }

    /**
     * Set the time for the overlay.
     */
    public void setOverlayTime(long ot) {
        overlayTime = ot;
        currentTime = false;
        doPrepare();
    }

    /**
     * Returns whether the layer will set the overlayTime to the time
     * the image is created.
     */
    public boolean getCurrentTime() {
        return currentTime;
    }

    /**
     * Set whether the layer should set the overlayTime to the time
     * the image is created.  If the time is being set to reflect a
     * time other than the current time, this needs to be set to
     * false.  It actually is, if you manually set the overlay time.
     */
    public void setCurrentTime(boolean ct) {
        currentTime = ct;
    }

    /**
     * Get the timer being used for automatic updates.  May be null if
     * a timer is not set.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * If you want the layer to update itself at certain intervals,
     * you can set the timer to do that.  Set it to null to disable it.
     */
    public void setTimer(Timer t) {
        timer = t;
    }

}
