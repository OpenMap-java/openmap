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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/DateLayer.java,v $
// $RCSfile: DateLayer.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Graphics;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.Taskable;

/**
 * Layer that displays date and time. This Layer is a Taskable
 * (ActionListener) object so that it can be prompted by a
 * javax.swing.Timer object. This layer understands the following
 * properties: <code><pre>
 * 
 *  # display font as a Java font string
 *  date.font=SansSerif-Bold
 *  # like XWindows geometry: [+-]X[+-]Y, `+' indicates relative to
 *  # left edge or top edges, `-' indicates relative to right or bottom
 *  # edges, XX is x coordinate, YY is y coordinate
 *  date.geometry=+20-30
 *  # background rectangle color (ARGB)
 *  date.color.bg=ffb3b3b3
 *  # foreground text color (ARGB)
 *  date.color.fg=ff000000
 *  # date format (using java.text.SimpleDateFormat patterns)
 *  date.format=EEE, d MMM yyyy HH:mm:ss z
 *  
 * </pre></code>
 * <p>
 * In addition to the previous properties, you can get this layer to
 * work with the OpenMap viewer by adding/editing the additional
 * properties in your <code>openmap.properties</code> file:
 * <code><pre>
 * 
 *  # layers
 *  openmap.layers=date ...
 *  # class
 *  date.class=com.bbn.openmap.layer.DateLayer
 *  # name
 *  date.prettyName=Date &amp; Time
 *  
 * </pre></code> NOTE: the color properties do not support alpha value if
 * running on JDK 1.1...
 */
public class DateLayer extends LabelLayer implements Taskable, MapMouseListener {

    // property keys
    public final static transient String dateFormatProperty = "date.format";

    // properties
    // Dateformat default is similar to IETF standard date syntax:
    // "Sat, 12 Aug 1995 13:30:00 GMT", except for the local timezone.
    protected DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

    /**
     * Sets the properties for the <code>Layer</code>.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String dateFormatString = props.getProperty(prefix + dateFormatProperty,
                ((SimpleDateFormat) dateFormat).toPattern());

        dateFormat = new SimpleDateFormat(dateFormatString);

    }

    /**
     * Get a string representation of the current time. Format the
     * string using the current DateFormat.
     * 
     * @return String
     */
    public String getCurrentTimeString() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        return " " + dateFormat.format(cal.getTime()) + " ";
    }

    /**
     * Set the DateFormat used to display the date.
     * 
     * @param df DateFormat
     */
    protected void setDateFormat(DateFormat df) {
        dateFormat = df;
    }

    /**
     * Get the DateFormat used to display the date.
     * 
     * @return DateFormat
     */
    protected DateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Paints the layer.
     * 
     * @param g the Graphics context for painting
     */
    public void paint(Graphics g) {
        String data = getCurrentTimeString();
        if (Debug.debugging("datelayer")) {
            System.out.println("DateLayer.paint(): " + data);
        }
        labelText = data;
        super.paint(g);
    }

    /**
     * Get the sleep hint in milliseconds. The Taskable implementation
     * should determine the sleep (delay) interval between invocations
     * of its <code>actionPerformed()</code>.
     * <p>
     * NOTE: this is only a hint for the timer. It's the Taskable's
     * responsibility to determine if too little or too much time has
     * elapsed between invocations of <code>actionPerformed()</code>
     * (if it really matters).
     * 
     * @return int milliseconds of sleep interval
     */
    public int getSleepHint() {
        return 1000;//update every second
    }
}