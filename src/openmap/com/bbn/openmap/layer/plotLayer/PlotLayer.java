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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/plotLayer/PlotLayer.java,v $
// $RCSfile: PlotLayer.java,v $
// $Revision: 1.7 $
// $Date: 2005/08/09 18:44:25 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.plotLayer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;

import com.bbn.openmap.Environment;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 *  
 */
public class PlotLayer extends OMGraphicHandlerLayer implements MapMouseListener {

    private ScatterGraph graph = null;
    private boolean show_plot_ = false;

    // The currently selected graphic.
    private OMGraphic selectedGraphic;
    private Vector<GLOBESite> selectedGraphics = null;

    // Where do we get the data from?
    // default to use GLOBE atmospheric temperature.
    private String datasource = "com/bbn/openmap/layer/plotLayer/AT.gst_small.txt";

    // "http://globe.ngdc.noaa.gov/sda/student_data/AT.gst.txt";

    private GLOBETempData temperature_data = null;

    // The control palette
    private JPanel pal = null;

    /**
     * X position of the plot rectangle.
     */
    protected int plotX = 100;

    /**
     * Y position of the plot rectangle.
     */
    protected int plotY = 100;

    /**
     * Width of the plot rectangle.
     */
    protected int plotWidth = 320;

    /**
     * Height of the plot rectangle.
     */
    protected int plotHeight = 200;

    /**
     * Construct the PlotLayer.
     */
    public PlotLayer() {

        // setList(plotDataSources());
    }

    public synchronized OMGraphicList prepare() {
        if (graph == null) {
            GLOBETempData temperature_data = getDataSource();
            if (temperature_data != null) {
                graph = new ScatterGraph(678, 790, null, temperature_data.overall_min_year_, temperature_data.overall_max_year_, temperature_data.overall_min_temp_, temperature_data.overall_max_temp_);

                setList(plotDataSources(temperature_data));

            }
        }

        Projection proj = getProjection();
        if (proj != null && graph != null) {
            // graph.resize(plotX, plotY, plotWidth, plotHeight);
            graph.resize(0, 0, proj.getWidth(), proj.getHeight());
        }
        return super.prepare();
    }

    /**
     * Search for the data in the directories listing in the CLASSPATH. We
     * should also check to see if the datafile is specified as a URL so that we
     * can load it as such.
     */
    private GLOBETempData getDataSource() {

        // load the data from the CLASSPATH
        Vector<String> dirs = Environment.getClasspathDirs();
        FileInputStream is = null;
        int nDirs = dirs.size();
        if (nDirs > 0) {
            for (String dir : dirs) {
                File datafile = new File(dir, datasource);
                if (datafile.isFile()) {
                    try {
                        is = new FileInputStream(datafile);
                        // System.out.println("datafile="+datafile);
                        break;
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (is == null) {
                System.err.println("Unable to load datafile \"" + datasource + "\" from CLASSPATH");
            }
        } else {
            System.err.println("No directories in CLASSPATH!");
            System.err.println("Unable to load datafile \"" + datasource + "\" from CLASSPATH");
        }
        if (is == null)
            return null;

        // Parse the data
        GLOBETempData temperature_data = null;
        try {
            temperature_data = new GLOBETempData();
            temperature_data.loadData(is);
        } catch (IOException e) {
            System.err.println(e);
        }
        return temperature_data;
    }

    /** Put the data points on the map. */
    private OMGraphicList plotDataSources(GLOBETempData temperature_data) {
        Debug.message("basic", "PlotLayer.plotDataSources()");
        int num_graphics = 0;

        OMGraphicList graphics = new OMGraphicList();
        graphics.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);

        Enumeration site_enum = temperature_data.getAllSites();
        while (site_enum.hasMoreElements()) {
            GLOBESite site = (GLOBESite) site_enum.nextElement();
            // Debug.message("basic", "PlotLayer adds " + site.getName());
            graphics.add(site.getGraphic());
            num_graphics++;
        }

        Debug.message("basic", "Plotlayer found " + num_graphics + " distinct sites");

        // Find the sites that are visible on the map.
        return graphics;
    }

    /** Build and display the plot. */
    private OMGraphic generatePlot() {
        // System.out.println("Generating Plot ");
        if (graph != null) {
            graph.setDataPoints(selectedGraphics);
            graph.plotData();
            return graph.getPlotGraphics();
        }
        return null;
    }

    private void showPlot() {
        show_plot_ = true;

        OMGraphic plot = generatePlot();
        OMGraphicList list = getList();

        if (plot != null && list != null) {
            // System.out.println("Making plot visible..");
            list.add(plot);
            // generate the graphics for rendering.
            list.generate(getProjection(), false);
        }
        repaint();
    }

    private void hidePlot() {
        // System.out.println("Making plot IN-visible..");
        show_plot_ = false;
        if (graph != null) {
            OMGraphic plot = graph.getPlotGraphics();
            OMGraphicList list = getList();

            if (list != null && plot != null) {
                list.remove(plot);
            }
        }
        repaint();
    }

    /**
     * Add the data from the clicked site to the list of things we are drawing.
     */
    private void addSelectionToPlotList() {
        if (selectedGraphic != null) {
            // Change the color of the clicked ones
            selectedGraphic.setLinePaint(Color.blue);

            if (selectedGraphics == null) {
                selectedGraphics = new Vector<GLOBESite>();
            }

            Object app_obj = selectedGraphic.getAppObject();

            if (app_obj instanceof GLOBESite) {
                GLOBESite site = (GLOBESite) app_obj;
                if (!selectedGraphics.contains(app_obj)) {
                    Debug.message("basic", "Adding to plot list...");
                    selectedGraphics.addElement(site);
                    selectedGraphic.setFillPaint(Color.yellow);
                } else {
                    Debug.message("basic", "Removing from plot list...");
                    selectedGraphics.removeElement(site);
                    selectedGraphic.setFillPaint(Color.red);
                    selectedGraphic.setLinePaint(Color.red);
                }

            }
        } else {
            Debug.message("basic", "Nothing to add to plot list!");
        }
    }

    /**
     * Returns self as the <code>MapMouseListener</code> in order to receive
     * <code>MapMouseEvent</code>s. If the implementation would prefer to
     * delegate <code>MapMouseEvent</code>s, it could return the delegate from
     * this method instead.
     * 
     * @return The object to receive <code>MapMouseEvent</code> s or null if
     *         this layer isn't interested in <code>MapMouseEvent</code> s
     */
    public MapMouseListener getMapMouseListener() {
        return this;
    }

    public Component getGUI() {
        if (pal == null) {
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = Integer.parseInt(e.getActionCommand(), 10);
                    switch (index) {
                    case 0:
                        if (show_plot_)
                            hidePlot();
                        else
                            showPlot();
                        break;
                    default:
                        throw new RuntimeException("argh!");
                    }
                }
            };
            pal = PaletteHelper.createCheckbox("Plot Control", new String[] { "Show Temperature Plot" }, new boolean[] { show_plot_ }, al);
        }
        return pal;
    }

    // ----------------------------------------------------------------------
    // MapMouseListener interface implementation
    // ----------------------------------------------------------------------

    /**
     * Indicates which mouse modes should send events to this <code>Layer</code>
     * .
     * 
     * @return String[] of mouse mode names
     * 
     * @see com.bbn.openmap.event.MapMouseListener
     * @see com.bbn.openmap.MouseDelegator
     */
    public String[] getMouseModeServiceList() {
        return new String[] { SelectMouseMode.modeID };
    }

    // graphic position variables when moving the plot graphic
    private int prevX, prevY;
    private boolean grabbed_plot_graphics_ = false;

    /**
     * Called whenever the mouse is pressed by the user and one of the requested
     * mouse modes is active.
     * 
     * @param e the press event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mousePressed(MouseEvent e) {
        if (show_plot_ && graph != null) {
            int x = e.getX();
            int y = e.getY();
            if ((x >= plotX) && (x <= plotX + plotWidth) && (y >= plotY)
                    && (y <= plotY + plotWidth)) {

                grabbed_plot_graphics_ = true;
                // grab the location
                prevX = x;
                prevY = y;
            }
        }
        return false;
    }

    /**
     * Called whenever the mouse is released by the user and one of the
     * requested mouse modes is active.
     * 
     * @param e the release event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseReleased(MouseEvent e) {
        grabbed_plot_graphics_ = false;
        return false;
    }

    /**
     * Called whenever the mouse is clicked by the user and one of the requested
     * mouse modes is active.
     * 
     * @param e the click event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseClicked(MouseEvent e) {
        // System.out.println("XY: " + e.getX() + " " + e.getY() );
        if (selectedGraphic != null && !show_plot_) {
            switch (e.getClickCount()) {
            case 1:
                /**
                 * One click adds the site to our list of sites to plot.
                 */
                addSelectionToPlotList();
                generatePlot();
                repaint();
                break;
            case 2:
                /**
                 * Double click means generate the plot.
                 */
                // System.out.println("Saw DoubleClick!");
                repaint();
                break;
            default:
                break;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called whenever the mouse enters this layer and one of the requested
     * mouse modes is active.
     * 
     * @param e the enter event
     * @see #getMouseModeServiceList
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Called whenever the mouse exits this layer and one of the requested mouse
     * modes is active.
     * 
     * @param e the exit event
     * @see #getMouseModeServiceList
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Called whenever the mouse is dragged on this layer and one of the
     * requested mouse modes is active.
     * 
     * @param e the drag event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseDragged(MouseEvent e) {
        if (grabbed_plot_graphics_) {
            int x = e.getX();
            int y = e.getY();
            int dx = x - prevX;
            int dy = y - prevY;

            plotX += dx;
            plotY += dy;
            prevX = x;
            prevY = y;

            graph.resize(plotX, plotY, plotWidth, plotHeight);
            OMGraphicList plotGraphics = graph.getPlotGraphics();
            // regenerate the plot graphics
            plotGraphics.generate(getProjection(), true);
            repaint();
        }
        return false;
    }

    /**
     * Called whenever the mouse is moved on this layer and one of the requested
     * mouse modes is active.
     * <p>
     * Tries to locate a graphic near the mouse, and if it is found, it is
     * highlighted and the Layer is repainted to show the highlighting.
     * 
     * @param e the move event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseMoved(MouseEvent e) {
        OMGraphic newSelectedGraphic;

        if (show_plot_ && graph != null) {

            newSelectedGraphic = graph.selectPoint(e.getX(), e.getY(), 4.0f);

            if (newSelectedGraphic != null) {
                String infostring = (String) (newSelectedGraphic.getAppObject());
                if (infostring != null) {
                    fireRequestInfoLine(infostring);
                }
            } else {
                fireRequestInfoLine("");
            }

            return true;
        } else {
            OMGraphicList list = getList();
            if (list != null) {
                newSelectedGraphic = list.selectClosest(e.getX(), e.getY(), 4.0f);

                if (newSelectedGraphic != null
                        && (selectedGraphic == null || newSelectedGraphic != selectedGraphic)) {

                    Debug.message("basic", "Making selection...");

                    selectedGraphic = newSelectedGraphic;
                    // selectedGraphic.setLineColor(Color.yellow);
                    selectedGraphic.regenerate(getProjection());

                    // display site info on map
                    GLOBESite site = (GLOBESite) (newSelectedGraphic.getAppObject());
                    if (site != null) {
                        fireRequestInfoLine(site.getInfo());
                    }

                    repaint();
                } else if (selectedGraphic != null && newSelectedGraphic == null) {

                    // revert color of un-moused object.
                    Debug.message("basic", "Clearing selection...");
                    // selectedGraphic.setLineColor(Color.red);
                    selectedGraphic.regenerate(getProjection());
                    fireRequestInfoLine("");
                    selectedGraphic = null;
                    repaint();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Called whenever the mouse is moved on this layer and one of the requested
     * mouse modes is active, and the gesture is consumed by another active
     * layer. We need to deselect anything that may be selected.
     * 
     * @see #getMouseModeServiceList
     */
    public void mouseMoved() {
        getList().deselect();
        repaint();
    }
}