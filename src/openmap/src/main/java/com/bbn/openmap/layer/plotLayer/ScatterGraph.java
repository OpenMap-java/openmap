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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/plotLayer/ScatterGraph.java,v
// $
// $RCSfile: ScatterGraph.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 18:44:25 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.plotLayer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.util.Debug;

public class ScatterGraph {

    // Color to display the plot in.
    private final Paint plot_color_ = Color.black;
    private final Paint graph_bg_color = Color.white;
    private final Paint select_color_ = Color.red;

    protected OMGraphicList plot_graphics_ = null;
    protected OMGraphicList plot_points_ = null;
    protected OMGraphicList plot_background_ = null;

    // pixels from edge of frame to the Plot itself
    private final int border_width_ = 50;

    private boolean axes_displayed_ = false;
    private float temp_scale_ = Float.NaN;
    private float year_scale_ = Float.NaN;

    private float max_year_ = Float.NaN;
    private float min_year_ = Float.NaN;

    private float max_temp_ = Float.NaN;
    private float min_temp_ = Float.NaN;

    // A Vector of the GLOBESites that are currently displayed on the
    // map.
    private Vector sites_displayed_ = null;

    // the size and position of the available drawing area
    private int frame_x, frame_y;
    private int frame_height_, frame_width_;
    private int plot_height_, plot_width_;
//    private int frame_xoffset_, frame_yoffset_;

//    private static final byte[] datapoint_bits_ = { (byte) 0x0e, (byte) 0x1f,
//            (byte) 0x1b, (byte) 0x1f, (byte) 0x0e };

    private void initialize(int height, int width, int xoffset, int yoffset,
                            Vector sites, float minyear, float maxyear,
                            float mintemp, float maxtemp) {
        plot_graphics_ = new OMGraphicList();
        plot_points_ = new OMGraphicList();
        plot_background_ = new OMGraphicList();

        plot_graphics_.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);
        plot_points_.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);
        plot_background_.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);

//

        max_year_ = maxyear;
        min_year_ = minyear;

        max_temp_ = maxtemp;
        min_temp_ = mintemp;

        resizeGraph(frame_x, frame_y, height, width);
        setDataPoints(sites);
        resetScale();

        plotData();
    }

    /**
     * Set up the size of the graph, and scale all the axes correctly.
     */
    public ScatterGraph(int height, int width, Vector sites, float minyear,
            float maxyear, float mintemp, float maxtemp) {
        initialize(height,
                width,
                0,
                0,
                sites,
                minyear,
                maxyear,
                mintemp,
                maxtemp);
    }

    public ScatterGraph(int height, int width, int xoffset, int yoffset,
            Vector sites, float minyear, float maxyear, float mintemp,
            float maxtemp) {
        initialize(height,
                width,
                xoffset,
                yoffset,
                sites,
                minyear,
                maxyear,
                mintemp,
                maxtemp);
    }

    public void resize(int x, int y, int newwidth, int newheight) {
        plot_graphics_.clear();
        plot_points_.clear();
        plot_background_.clear();

        resizeGraph(x, y, newheight, newwidth);
        axes_displayed_ = false;
        // replot everything on the graph
        plotData();
    }

    // If the window changes size, we need to resize the graph, and
    // replot.
    private void resizeGraph(int x, int y, int height, int width) {
        // Setup the sizes.
        frame_x = x;
        frame_y = y;
        frame_width_ = width;
        frame_height_ = height;
        plot_width_ = frame_width_ - 2 * border_width_;
        plot_height_ = frame_height_ - 2 * border_width_;

        resetScale();
    }

    /**
     * Set the max and min points (as well as the scale factors) for
     * the plot.
     * 
     * @param minyear lowest year
     * @param maxyear highest year
     * @param mintemp lowest temperature
     * @param maxtemp highest temperature
     */
    private void setScale(float minyear, float maxyear, float mintemp,
                          float maxtemp) {
        min_year_ = minyear;
        max_year_ = maxyear;
        min_temp_ = mintemp;
        max_temp_ = maxtemp;

        temp_scale_ = ((float) plot_height_ / (maxtemp - mintemp));
        year_scale_ = ((float) plot_width_ / (maxyear - minyear));
    }

    private void resetScale() {
        setScale(min_year_, max_year_, min_temp_, max_temp_);
    }

    /**
     * Give us new data points to plot.
     * 
     * @param sites the sites
     */
    public void setDataPoints(Vector sites) {
        if (sites != null) {
            sites_displayed_ = sites;
        } else {
            sites_displayed_ = new Vector();
        }
    }

    /**
     * Returns the location of the plot point for this value. This is
     * just (value - offset) * scale
     * 
     * @param value the value
     * @param scale the scale
     * @param offset the offset
     * @return the pixel position on the plot (not on the whole
     *         frame).
     */
    private int findPointOnScale(float value, float scale, float offset) {
        float newvalue = (value - offset) * scale;
        return (int) newvalue;
    }

    // gets the Y coordinate of the plot
    private int findTempPoint(float value) {
        return frame_y
                + frame_height_
                - (border_width_ + findPointOnScale(value,
                        temp_scale_,
                        min_temp_));
    }

    // gets the Y coordinate of the plot
    private int findYearPoint(float value) {
        return frame_x + border_width_
                + findPointOnScale(value, year_scale_, min_year_);
    }

    /**
     * This is used primarily for drawing the axes of the plot.
     * 
     * @param x1 x coordinate
     * @param y1 y coordinate
     * @param x2 x coordinate
     * @param y2 y coordinate
     * @param color color
     * @return an OMLine object, suitable for displaying on the map.
     */

    private OMLine createPlotLine(int x1, int y1, int x2, int y2, Paint color) {
        OMLine line = new OMLine(x1, y1, x2, y2);
        line.setLinePaint(color);
        line.setSelectPaint(Color.white);
        return line;
    }

    private OMLine createGraphLine(float year1, float temp1, float year2,
                                   float temp2) {
        int x1 = findYearPoint(year1);
        int y1 = findTempPoint(temp1);

        int x2 = findYearPoint(year2);
        int y2 = findTempPoint(temp2);

        OMLine line = createPlotLine(x1, y1, x2, y2, plot_color_);
        line.setLinePaint(plot_color_);
        line.setSelectPaint(select_color_);
        return line;
    }

    private OMText createLabel(String text, int x, int y, Paint color,
                               int justification) {
        Font default_font = new Font("TimesRoman", Font.PLAIN, 10);
        OMText label = new OMText(x, y, text, default_font, justification);

        label.setLinePaint(color);
        label.setSelectPaint(Color.white);
        return label;
    }

    private OMText createLabel(String text, int x, int y) {
        return createLabel(text, x, y, plot_color_, OMText.JUSTIFY_LEFT);
    }

    private Date GetDateFromFloat(float fdate) {
        long mseconds = (long) ((fdate - 1970) * 365.25 // days per
                                                        // year
                * 24 // hours per day
                * 60 // minutes per hour
                * 60 // seconds per minute
        * 1000); // milliseconds per second

        Date date = new Date(mseconds);
        return date;
    }

    private String GetStringDateFromFloat(float fdate) {
        Date date = GetDateFromFloat(fdate);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MM/dd/yyyy");
        String datestring = sdf.format(date);
        return datestring;
    }

    /**
     * Draw the axes of the Graph, and label them where appropriate
     */
    private void drawGraphAxes() {

        //      System.out.println("Plotting Axes");

        int top = frame_y + border_width_;
        int bottom = frame_y + frame_height_ - border_width_;
        int left = frame_x + border_width_;
        int right = frame_x + frame_width_ - border_width_;

        String min_year_string = GetStringDateFromFloat(min_year_);
        String max_year_string = GetStringDateFromFloat(max_year_);

        OMLine year_axis = createPlotLine(left,
                bottom,
                right,
                bottom,
                plot_color_);

        OMLine temp_axis = createPlotLine(left, top, left, bottom, plot_color_);

        OMText year_min_label = createLabel(min_year_string + " ",
                left,
                bottom + 10);

        OMText year_max_label = createLabel(max_year_string + " ",
                right - 30,
                bottom + 10);

        OMText temp_min_label = createLabel(min_temp_ + " ",
                left,
                bottom,
                plot_color_,
                OMText.JUSTIFY_RIGHT);

        OMText temp_max_label = createLabel(max_temp_ + " ",
                left,
                top,
                plot_color_,
                OMText.JUSTIFY_RIGHT);

        OMText temp_axis_label = createLabel("Temp", left, frame_y
                + (frame_height_ / 2), plot_color_, OMText.JUSTIFY_RIGHT);
        OMText year_axis_label = createLabel("Year", frame_x
                + (frame_width_ / 2), bottom + 15);

        // The background that the plot is drawn on.
        OMRect background = new OMRect(frame_x, frame_y, frame_x + frame_width_, frame_y
                + frame_height_);

        background.setFillPaint(graph_bg_color);
        background.setLinePaint(graph_bg_color);

        year_axis.setAppObject(this);
        temp_axis.setAppObject(this);

        plot_background_.add(background);

        plot_background_.add(year_axis);
        plot_background_.add(temp_axis);
        plot_background_.add(temp_axis_label);
        plot_background_.add(year_axis_label);

        plot_background_.add(year_min_label);
        plot_background_.add(year_max_label);
        plot_background_.add(temp_min_label);
        plot_background_.add(temp_max_label);

        // add the result to the plot
        plot_graphics_.add(plot_background_);

        axes_displayed_ = true;
    }

    public OMGraphicList getPlotGraphics() {
        return plot_graphics_;
    }

    private OMGraphic plotPoint(float year, float temp) {
        int x = findYearPoint(year);
        int y = findTempPoint(temp);

        String yearstring = GetStringDateFromFloat(year);

        String name = "Time: " + yearstring + ", Temperature: " + temp + "C ("
                + (int) ((9.0 / 5.0) * temp + 32) + "F)";

        OMGraphic graphic = new OMCircle(x, y, 2, 2);
        graphic.setLinePaint(plot_color_);
        graphic.setFillPaint(plot_color_);
        graphic.setSelectPaint(select_color_);
        graphic.setAppObject(name);

        return graphic;
    }

    private Enumeration sortEnumerationOfFloats(Enumeration enumeration) {
        Vector vec = new Vector();
        while (enumeration.hasMoreElements()) {
            vec.addElement(enumeration.nextElement());
        }

        Float[] result = new Float[vec.size()];
        vec.copyInto(result);
        Vector resultvec = new Vector(vec.size());

        for (int i = 0; i < vec.size(); i++) {
            for (int j = i + 1; j < vec.size(); j++) {
                if (result[i].floatValue() > result[j].floatValue()) {
                    Float t = result[i];
                    result[i] = result[j];
                    result[j] = t;
                }
            }
            // We now know that i contains the smallest element
            // in the remaining vector.
            resultvec.addElement(result[i]);
        }
        return resultvec.elements();
    }

    /**
     * Create OMGraphics for all the data points on the plot
     */
    public void plotData() {
        Debug.message("basic", "ScatterGraph.plotData()");
        Enumeration all_sites = sites_displayed_.elements();
        int num_elements = 0;
        int num_sites = 0;

        plot_points_.clear();

        // Do the Axes:
        if (!axes_displayed_) {
            drawGraphAxes();
        }

        while (all_sites.hasMoreElements()) {
            GLOBESite site = (GLOBESite) all_sites.nextElement();
            Enumeration years = sortEnumerationOfFloats(site.getAllYears());

            float last_year = Float.NaN;
            float last_temp = Float.NaN;

            num_sites++;

            while (years.hasMoreElements()) {
                float year = ((Float) years.nextElement()).floatValue();
                float temp = site.getValueForYear(year);
                OMGraphic point = plotPoint(year, temp);

                plot_points_.add(point);

                if (!Float.isNaN(last_year)) {
                    // Connect all the rest with a line.
                    OMGraphic line = createGraphLine(last_year,
                            last_temp,
                            year,
                            temp);
                    plot_points_.add(line);
                }

                // remember the last point we looked at.
                last_year = year;
                last_temp = temp;

                // plot a data point
                num_elements++;
            }
        }
        plot_graphics_.add(plot_points_);

        //              System.out.println("Data plotted: " +
        //                         num_sites + " sites, " +
        //                         num_elements + " datapoints");

    }

    public OMGraphic selectPoint(int x, int y, float range) {
        OMGraphic selection;
        selection = plot_points_.selectClosest(x, y, range);
        return selection;
    }

}