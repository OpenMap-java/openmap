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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/daynight/DayNightLayer.java,v $
// $RCSfile: DayNightLayer.java,v $
// $Revision: 1.12 $
// $Date: 2006/04/07 17:36:01 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.daynight;

/*  Java Core  */
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Timer;

import com.bbn.openmap.I18n;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.Cylindrical;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The DayNightLayer is a layer that draws the day/Night terminator on
 * the map. When the layer is re-projected, it figures out the
 * brightest point on the earth (closest to the sun), and creates an
 * image that has daytime pixels clear and the nighttime pixels
 * shaded. There are a couple of options available for the layer. The
 * terminator can be faded from light to dark, and the width of the
 * fading can be adjusted. The color of the shading can be changed.
 * The shading can reflect the current time, or be set to display the
 * shading of a specified time. A time interval can be set to have the
 * layer automatically update at regular intervals.
 * 
 * <P>
 * The openmap.properties file can control the layer with the
 * following settings: <code><pre>
 * 
 *  # These are all optional, and can be omitted if you want to use the defaults.
 *  # draw terminator as poly (faster calculation than image,
 *  # defaults to true).
 *  daynight.doPolyTerminator=true
 *  # number of vertices for polygon terminator line.  this is only valid
 *  # if doPolyTerminator is true...
 *  daynight.terminatorVerts=360
 *  # termFade - the distance of the transition of fade, as a percentage of PI.
 *  daynight.termFade=.1
 *  # currentTime - true to display the shading at the computer's current time.
 *  daynight.currentTime=true
 *  # overlayTime - time, in milliseconds from java/unix epoch, to set the layer
 *  # time being displayed.  currentTime has to be false for this to be used.
 *  daynight.overlayTime=919453689000
 *  # updateInterval - time in milliseconds between updates.  currentTime has to be
 *  # true for this to be used.
 *  daynight.updateInterval=300000
 *  # Color of the shading (32bit Hex ARGB)
 *  daynight.nighttimeColor=64000000
 *  
 * </pre></code> In addition, you can get this layer to work with the
 * OpenMap viewer by editing your openmap.properties file: <code><pre>
 * 
 *  # layers
 *  openmap.layers=daynight ...
 *  # class
 *  daynight.class=com.bbn.openmap.layer.daynight.DayNightLayer
 *  # name
 *  daynight.prettyName=Day/Night Shading
 *  
 * </pre></code>
 *  
 */
public class DayNightLayer extends OMGraphicHandlerLayer implements
        ProjectionListener, ActionListener {
    /**
     * Default value of fade to the terminator line, set to .10f. This
     * means that the last 10% of the horizon will be faded out.
     */
    public static final float DEFAULT_TERM_FADE = .10f;
    /**
     * Default update interval, which is never - updates occur on
     * re-projections.
     */
    public static final int DO_NOT_UPDATE = -1;

    /** The color of daytime - default is white and clear. */
    protected Color daytimeColor = new Color(0x00FFFFFF, true);
    /** the color of darkness - default is black. */
    protected Color nighttimeColor = new Color(0x7F000000, true);

    /**
     * Percentage of the distance from the horizon to the brightest
     * point to start fading to darkness. Expected to be between 0.0
     * and 0.5.
     */
    protected float termFade = DEFAULT_TERM_FADE;
    /**
     * If true, the layer will set the darkness according to the
     * current time.
     */
    protected boolean currentTime = true;
    /**
     * The time used to create the layer, in milliseconds from
     * java/unix epoch.
     */
    protected long overlayTime = 0;
    /**
     * Update interval to automatically update the layer, in
     * milli-seconds
     */
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
    public static final String DaytimeColorProperty = "daytimeColor";
    public static final String NighttimeColorProperty = "nighttimeColor";
    public static final String TermFadeProperty = "termFade";
    public static final String CurrentTimeProperty = "useCurrentTime";
    public static final String OverlayTimeProperty = "overlayTime";
    public static final String UpdateIntervalProperty = "updateInterval";
    public static final String DoPolyTerminatorProperty = "doPolyTerminator";
    public static final String TerminatorVertsProperty = "terminatorVerts";

    /**
     * The default constructor for the Layer. All of the attributes
     * are set to their default values.
     */
    public DayNightLayer() {
        setName("Day-Night");
    }

    /**
     * The properties and prefix are managed and decoded here, for the
     * standard uses of the DayNightLayer.
     * 
     * @param prefix string prefix used in the properties file for
     *        this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        overlayTime = PropUtils.longFromProperties(properties, prefix
                + OverlayTimeProperty, overlayTime);
        if (overlayTime <= 0) {
            currentTime = true;
        }

        currentTime = PropUtils.booleanFromProperties(properties, prefix
                + CurrentTimeProperty, currentTime);

        updateInterval = PropUtils.intFromProperties(properties, prefix
                + UpdateIntervalProperty, updateInterval);

        if (updateInterval > 0) {
            timer = new Timer(updateInterval, this);
        }

        termFade = PropUtils.floatFromProperties(properties, prefix
                + TermFadeProperty, termFade);

        if (termFade < 0 || termFade >= .5) {
            Debug.output("DayNightLayer: termFade funky value ignored.");
            termFade = DEFAULT_TERM_FADE;
        }

        daytimeColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + DaytimeColorProperty,
                daytimeColor);
        nighttimeColor = (Color) PropUtils.parseColorFromProperties(properties,
                prefix + NighttimeColorProperty,
                nighttimeColor);

        doPolyTerminator = PropUtils.booleanFromProperties(properties, prefix
                + DoPolyTerminatorProperty, doPolyTerminator);
        terminatorVerts = PropUtils.intFromProperties(properties, prefix
                + TerminatorVertsProperty, terminatorVerts);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + OverlayTimeProperty, Long.toString(overlayTime));
        props.put(prefix + CurrentTimeProperty,
                new Boolean(currentTime).toString());
        props.put(prefix + UpdateIntervalProperty,
                Integer.toString(updateInterval));
        props.put(prefix + TermFadeProperty, Float.toString(termFade));
        props.put(prefix + DaytimeColorProperty,
                Integer.toHexString(daytimeColor.getRGB()));
        props.put(prefix + NighttimeColorProperty,
                Integer.toHexString(nighttimeColor.getRGB()));
        props.put(prefix + DoPolyTerminatorProperty,
                new Boolean(doPolyTerminator).toString());
        props.put(prefix + TerminatorVertsProperty,
                Integer.toString(terminatorVerts));

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        String interString;

        interString = i18n.get(DayNightLayer.class,
                OverlayTimeProperty,
                I18n.TOOLTIP,
                "The time used to create the layer, in milliseconds from java/unix epoch (leave empty for current time).");
        props.put(OverlayTimeProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                OverlayTimeProperty,
                OverlayTimeProperty);
        props.put(OverlayTimeProperty + LabelEditorProperty, interString);

        interString = i18n.get(DayNightLayer.class,
                CurrentTimeProperty,
                I18n.TOOLTIP,
                "If true, the layer will set the darkness according to the current time.");
        props.put(CurrentTimeProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                CurrentTimeProperty,
                CurrentTimeProperty);
        props.put(CurrentTimeProperty + LabelEditorProperty, interString);
        props.put(CurrentTimeProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        interString = i18n.get(DayNightLayer.class,
                UpdateIntervalProperty,
                I18n.TOOLTIP,
                "Update interval to automatically update the layer, in milli-seconds.");
        props.put(UpdateIntervalProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                UpdateIntervalProperty,
                UpdateIntervalProperty);
        props.put(UpdateIntervalProperty + LabelEditorProperty, interString);

        interString = i18n.get(DayNightLayer.class,
                TermFadeProperty,
                I18n.TOOLTIP,
                "Percentage of the distance from the horizon to the brightest point to start fading to darkness, 0.0 to 0.5.");
        props.put(TermFadeProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                TermFadeProperty,
                TermFadeProperty);
        props.put(TermFadeProperty + LabelEditorProperty, interString);

        interString = i18n.get(DayNightLayer.class,
                DaytimeColorProperty,
                I18n.TOOLTIP,
                "Color for the daytime area, if polygon terminator isn't used.");
        props.put(DaytimeColorProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                DaytimeColorProperty,
                DaytimeColorProperty);
        props.put(DaytimeColorProperty + LabelEditorProperty, interString);
        props.put(DaytimeColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

        interString = i18n.get(DayNightLayer.class,
                NighttimeColorProperty,
                I18n.TOOLTIP,
                "Color for the nighttime area.");
        props.put(NighttimeColorProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                NighttimeColorProperty,
                NighttimeColorProperty);
        props.put(NighttimeColorProperty + LabelEditorProperty, interString);
        props.put(NighttimeColorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

        interString = i18n.get(DayNightLayer.class,
                DoPolyTerminatorProperty,
                I18n.TOOLTIP,
                "Render with polygon instead of image (it's faster).");
        props.put(DoPolyTerminatorProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                DoPolyTerminatorProperty,
                DoPolyTerminatorProperty);
        props.put(DoPolyTerminatorProperty + LabelEditorProperty, interString);
        props.put(DoPolyTerminatorProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        interString = i18n.get(DayNightLayer.class,
                TerminatorVertsProperty,
                I18n.TOOLTIP,
                "Number of vertices of the polygon terminator (more is smoother).");
        props.put(TerminatorVertsProperty, interString);
        interString = i18n.get(DayNightLayer.class,
                TerminatorVertsProperty,
                TerminatorVertsProperty);
        props.put(TerminatorVertsProperty + LabelEditorProperty, interString);

        props.put(initPropertiesProperty, CurrentTimeProperty + " "
                + OverlayTimeProperty + " " + UpdateIntervalProperty + " "
                + NighttimeColorProperty + " " + DoPolyTerminatorProperty + " "
                + TerminatorVertsProperty + " " + DaytimeColorProperty + " "
                + TermFadeProperty + " " + RemovableProperty + " "
                + AddAsBackgroundProperty);

        return props;
    }

    /**
     * Handle an ActionEvent from the Timer.
     * 
     * @param ae action event from the timer.
     */
    public void actionPerformed(java.awt.event.ActionEvent ae) {
        super.actionPerformed(ae);
        if (Debug.debugging("daynight")) {
            Debug.output(getName() + "| updating image via timer...");
        }
        doPrepare();
    }

    /**
     * Create the OMGraphic that acts as an overlay showing the
     * day/night terminator. The brightest spot on the earth is
     * calculated, and then each pixel is inverse projected to find
     * out its coordinates. Then the great circle distance is
     * calculated. The terminator is assumed to be the great circle
     * where all the points are PI/2 away from the bright point. If
     * the termFade variable is set, then the difference in color over
     * the terminator is feathered, on equal amount of the terminator.
     * 
     * @param projection the projection of the screen,
     * @return OMGraphic containing image to use for the layer. The
     *         image has been projected.
     */
    protected OMGraphic createImage(Projection projection) {

        if (currentTime)
            overlayTime = System.currentTimeMillis();

        if (Debug.debugging("daynight")) {
            Debug.output("DayNightLayer: Calculating sun position at time "
                    + Long.toString(overlayTime));
        }

        LatLonPoint brightPoint = SunPosition.sunPosition(overlayTime);

        Debug.message("daynight", "DayNightLayer: Calculated sun position");

        // Do a fast and relatively inexpensive calculation of the
        // terminator. NOTE: for non-cylindrical projections we don't
        // create a full-hemisphere circle so that we don't get
        // flip-rendering problem...
        if (doPolyTerminator) {
            Debug.message("daynight",
                    "DayNightLayer:  Creating polygon terminator");
            LatLonPoint darkPoint = brightPoint.getPoint(Math.PI, Math.PI / 4);
            OMCircle circle = new OMCircle((float)darkPoint.getY(), (float)darkPoint.getX(), (projection instanceof Cylindrical) ? 90f
                    : 89.0f,//HACK
                    Length.DECIMAL_DEGREE, terminatorVerts);
            circle.setPolarCorrection(true);
            circle.setFillPaint(nighttimeColor);
            circle.setLinePaint(nighttimeColor);
            circle.generate(projection);
            Debug.message("daynight",
                    "DayNightLayer: Done creating polygon terminator");
            return circle;
        }

        int width = projection.getWidth();
        int height = projection.getHeight();
        int[] pixels = new int[width * height];

        OMRaster ret = new OMRaster((int) 0, (int) 0, width, height, pixels);

        Debug.message("daynight", getName()
                + "|createImage: Center of bright spot lat= "
                + brightPoint.getLatitude() + ", lon= "
                + brightPoint.getLongitude());

        // Light is clear and/or white
        int light = daytimeColor.getRGB();

        // Allocate the memory here for the testPoint
        LatLonPoint testPoint = new LatLonPoint.Float(0f, 0f);
        // great circle distance between the bright point and each
        // pixel.
        double distance;

        //  Set the darkeness value
        int dark = nighttimeColor.getRGB();// ARGB
        int darkness = dark >>> 24;// darkness alpha
        int value;

        // Calculate the fae limits around the terminator
        float upperFadeLimit = (float) (MoreMath.HALF_PI * (1.0 + termFade));
        float lowerFadeLimit = (float) (MoreMath.HALF_PI * (1.0 - termFade));
        int fadeColorValue = 0x00FFFFFF & (dark); // RGB

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                projection.inverse(i, j, testPoint);
                distance = brightPoint.distance(testPoint);

                if (distance > upperFadeLimit) {
                    pixels[j * width + i] = dark;
                } else if (distance > lowerFadeLimit) {
                    value = (int) (darkness * (1 - ((upperFadeLimit - distance) / (upperFadeLimit - lowerFadeLimit))));
                    value <<= 24;
                    pixels[j * width + i] = fadeColorValue | value;
                } else {
                    pixels[j * width + i] = light;
                }
            }
        }

        ret.generate(projection);
        return ret;
    }

    /**
     * Prepares the graphics for the layer. This is where the
     * getRectangle() method call is made on the location.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this
     * happens, the map will set the cancel bit in the LayerThread,
     * (the thread that is running the prepare). If this Layer needs
     * to do any cleanups during the abort, it should do so, but
     * return out of the prepare asap.
     *  
     */
    public synchronized OMGraphicList prepare() {

        OMGraphicList list = getList();
        if (list == null) {
            list = new OMGraphicList();
        } else {
            list.clear();
        }

        if (isCancelled()) {
            Debug.message("daynight", getName()
                    + "|DayNightLayer.prepare(): aborted.");
            return null;
        }

        Debug.message("basic", getName() + "|DayNightLayer.prepare(): doing it");

        OMGraphic ras = createImage(getProjection());
        if (timer != null)
            timer.restart();
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
     * the image is created. If the time is being set to reflect a
     * time other than the current time, this needs to be set to
     * false. It actually is, if you manually set the overlay time.
     */
    public void setCurrentTime(boolean ct) {
        currentTime = ct;
    }

    /**
     * Get the timer being used for automatic updates. May be null if
     * a timer is not set.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * If you want the layer to update itself at certain intervals,
     * you can set the timer to do that. Set it to null to disable it.
     */
    public void setTimer(Timer t) {
        timer = t;
    }

}