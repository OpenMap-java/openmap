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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/EarthquakeLayer.java,v $
// $RCSfile: EarthquakeLayer.java,v $
// $Revision: 1.8 $
// $Date: 2007/04/24 19:53:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * Get data about recent earthquakes from the USGS finger sites and display it.
 * <p>
 * Debugging information is printed when the OpenMap Viewer is launch with
 * -Ddebug.earthquake flag.
 * <P>
 * 
 * <pre>
 * 
 * 
 *   # Properties for the Earthquake Layer
 *   earthquake.sites=&lt;finger site&gt; &lt;finger site&gt; ...
 *   # in seconds
 *   earthquake.queryinterval=300
 * 
 * 
 * </pre>
 * 
 * NOTE: I'm not sure this layer works anymore, probably hasn't for some time
 * since the finger sites no longer provide data AFAIK. Leaving this layer in
 * the package as another example.
 */
public class EarthquakeLayer
        extends OMGraphicHandlerLayer
        implements MapMouseListener {

    public final static transient String fingerSitesProperty = "sites";
    public final static transient String queryIntervalProperty = "queryInterval";

    /**
     * Sites to finger user the user `quake'.
     */
    protected String fingerSites[] = {
        "scec.gps.caltech.edu",
        "geophys.washington.edu",
        "giseis.alaska.edu",
        "mbmgsun.mtech.edu",
        "quake.eas.slu.edu"
    };

    // Old sites
    // "gldfs.cr.usgs.gov",
    // "andreas.wr.usgs.gov",
    // "seismo.unr.edu",
    // "eqinfo.seis.utah.edu",
    // "sisyphus.idbsu.edu",
    // "info.seismo.usbr.gov",
    // "vtso.geol.vt.edu",
    // "tako.wr.usgs.gov",
    // "ldeo.columbia.edu"

    /**
     * Sites that are actively being queried.
     */
    protected boolean activeSites[] = new boolean[fingerSites.length];

    /** Default to 5 minutes. */
    private long fetchIntervalMillis = 300 * 1000;

    // lat-lon data of the earthquakes
    protected float llData[] = new float[0];

    // floating information about the earthquakes
    protected String infoData[] = new String[0];
    // floating information about the earthquakes
    protected String drillData[] = new String[0];

    private long lastDataFetchTime = 0;
    protected Color lineColor = Color.red;
    protected boolean showingInfoLine = false;

    /** The layer GUI. */
    protected JPanel gui = null;

    /**
     * Construct an EarthquakeLayer.
     */
    public EarthquakeLayer() {
        activeSites[0] = true;
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    /**
     * Fetch data from finger sites, if needed, generate the OMGraphics with the
     * current projection regardless.
     */
    public synchronized OMGraphicList prepare() {
        if (needToRefetchData()) {
            parseData(getEarthquakeData());
        }
        return generateGraphics();
    }

    /**
     * Fetches data if it hasn't been fetched in a while.
     */
    protected boolean needToRefetchData() {
        long now = System.currentTimeMillis();
        long last = lastDataFetchTime;

        if ((last + fetchIntervalMillis) < now) {
            lastDataFetchTime = now;
            return true;
        }
        return false;
    }

    /**
     * Create the graphics.
     */
    protected OMGraphicList generateGraphics() {
        OMGraphicList omgraphics = new OMGraphicList();
        OMPoint circ;
        OMText text;

        int circle_r = 2;
        int circle_h = 5;

        for (int i = 0, j = 0; i < llData.length; i += 2, j++) {

            // grouping
            OMGraphicList group = new OMGraphicList(2);

            // XY-Circle at LatLonPoint
            circ = new OMPoint(llData[i], llData[i + 1], circle_r);
            circ.setOval(true);
            circ.setFillPaint(lineColor);
            group.add(circ);

            // Info
            text =
                    new OMText(llData[i], llData[i + 1], 0, circle_h + 10, infoData[j], java.awt.Font.decode("SansSerif"),
                               OMText.JUSTIFY_CENTER);
            text.setLinePaint(lineColor);
            group.add(text);

            group.setAppObject(new Integer(j));// remember index
            omgraphics.add(group);
        }

        omgraphics.generate(getProjection(), false);
        return omgraphics;
    }

    /**
     * Parse the finger site data.
     * 
     * @param data Vector
     */
    protected void parseData(Vector data) {
        int nLines = data.size();
        llData = new float[2 * nLines];
        infoData = new String[nLines];
        drillData = new String[nLines];

        for (int i = 0, j = 0, k = 0; i < nLines; i++) {
            String line = (String) data.elementAt(i);

            // Read a line of input and break it down
            StringTokenizer tokens = new StringTokenizer(line);
            String sdate = tokens.nextToken();
            String stime = tokens.nextToken();
            String slat = tokens.nextToken();
            String slon = tokens.nextToken();
            if (slon.startsWith("NWSE"))// handle ` ' in LatLon data
                slon = tokens.nextToken();
            String sdep = tokens.nextToken();
            if (sdep.startsWith("NWSE"))// handle ` ' in LatLon data
                sdep = tokens.nextToken();
            String smag = tokens.nextToken();
            String q = tokens.nextToken();
            String scomment = tokens.nextToken("\r\n");
            if (q.length() > 1) {
                scomment = q + " " + scomment;
            }

            infoData[j] = smag;
            drillData[j++] = sdate + " " + stime + " (UTC)  " + slat + " " + slon + " " + smag + " " + scomment;

            // Remove NESW from lat and lon before converting to float
            int west = slon.indexOf("W");
            int south = slat.indexOf("S");

            if (west >= 0)
                slon = slon.replace('W', '\0');
            else
                slon = slon.replace('E', '\0');
            if (south >= 0)
                slat = slat.replace('S', '\0');
            else
                slat = slat.replace('N', '\0');
            slon = slon.trim();
            slat = slat.trim();

            float flat = 0, flon = 0;
            try {
                flat = new Float(slat).floatValue();
                flon = new Float(slon).floatValue();
            } catch (NumberFormatException e) {
                Debug.error("EarthquakeLayer.parseData(): " + e + " line: " + line);
            }

            // replace West and South demarcations with minus sign
            if (south >= 0)
                flat = -flat;
            if (west >= 0)
                flon = -flon;

            llData[k++] = flat;
            llData[k++] = flon;
        }
    }

    /**
     * Get the earthquake data from the USGS. Should be called in a SwingWorker
     * thread, or you will freeze the application.
     * 
     * @return Vector containing information from the websites.
     */
    protected Vector getEarthquakeData() {
        Vector linesOfData = new Vector();
        Socket quakefinger = null;
        PrintWriter output = null;
        BufferedReader input = null;
        String line;

        for (int i = 0; i < activeSites.length; i++) {
            // skip sites which aren't on the active list
            if (!activeSites[i])
                continue;

            try {
                if (Debug.debugging("earthquake")) {
                    Debug.output("Opening socket connection to " + fingerSites[i]);
                }
                quakefinger = new Socket(fingerSites[i], 79);// open
                // connection
                // to
                // finger
                // port
                quakefinger.setSoTimeout(120 * 1000);// 2 minute
                // timeout
                output = new PrintWriter(new OutputStreamWriter(quakefinger.getOutputStream()), true);
                input = new BufferedReader(new InputStreamReader(quakefinger.getInputStream()), 1);
                output.println("/W quake");// use `/W' flag for long
                // output
            } catch (IOException e) {
                Debug.error("EarthquakeLayer.getEarthquakeData(): " + "can't open or write to socket: " + e);
                continue;
            }

            try {
                // add data lines to list
                while ((line = input.readLine()) != null) {
                    if (Debug.debugging("earthquake")) {
                        Debug.output("EarthquakeLayer.getEarthQuakeData(): " + line);
                    }
                    if (line.length() == 0)
                        continue;
                    if (!Character.isDigit(line.charAt(0)))
                        continue;

                    line = hackY2K(line);
                    if (line == null)
                        continue;
                    linesOfData.addElement(line);
                }
            } catch (IOException e) {
                Debug.error("EarthquakeLayer.getEarthquakeData(): " + "can't read from the socket: " + e);
                if (isCancelled()) {
                    return null;
                }
            }

            try {
                if (quakefinger != null) {
                    quakefinger.close();
                }
            } catch (IOException e) {
                Debug.error("EarthquakeLayer.getEarthquakeData(): " + "error closing socket: " + e);
            }
        }

        // int nQuakes = linesOfData.size();
        // for (int i=0; i<nQuakes; i++) {
        // Debug.output((String)linesOfData.elementAt(i));
        // }
        return linesOfData;
    }

    // This is the USGS's date problem, not ours (of course when they
    // change their format, we'll have to update this).
    // Note that also this could just be a bogus line (not a dataline)
    // beginning with a number, so we've got to deal with it here.
    private String hackY2K(String date) {
        StringTokenizer tok = new StringTokenizer(date, "/");
        String year, month, day;
        try {
            year = tok.nextToken();
            month = tok.nextToken();
            day = tok.nextToken();
        } catch (NoSuchElementException e) {
            Debug.error("EarthquakeLayer: unparsable date: " + date);
            return null;
        }
        if (year.length() == 2) {
            int y;
            try {
                y = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                Debug.error("EarthquakeLayer: invalid year: " + year);
                return null;
            }
            // Sliding window technique...
            if (y > 70) {
                date = "19";
            } else {
                date = "20";
            }
        } else if (year.length() != 4) {
            Debug.error("EarthquakeLayer: unparsable year: " + year);
            return null;
        }

        date = date + year + "/" + month + "/" + day;
        return date;
    }

    /**
     * Gets the gui controls associated with the layer.
     * 
     * @return Component
     */
    public Component getGUI() {
        JPanel p;
        if (gui == null) {
            gui = PaletteHelper.createVerticalPanel("Earthquakes");

            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            gui.setLayout(gridbag);
            constraints.fill = GridBagConstraints.HORIZONTAL; // fill
            // horizontally
            constraints.gridwidth = GridBagConstraints.REMAINDER; // another
            // row
            constraints.anchor = GridBagConstraints.EAST; // tack to
            // the left
            // edge

            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = Integer.parseInt(e.getActionCommand(), 10);
                    activeSites[index] = !activeSites[index];
                }
            };
            p = PaletteHelper.createCheckbox("Sites", fingerSites, activeSites, al);
            gridbag.setConstraints(p, constraints);
            gui.add(p);

            JButton b = new JButton("Query Now");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // force refetch of data
                    lastDataFetchTime = 0;
                    doPrepare();
                }
            });
            gridbag.setConstraints(p, constraints);
            gui.add(b);

        }
        return gui;
    }

    /**
     * Returns the MapMouseListener object that handles the mouse events.
     * 
     * @return the MapMouseListener for the layer, or null if none
     */
    public MapMouseListener getMapMouseListener() {
        return this;
    }

    // ----------------------------------------------------------------
    // MapMouseListener interface methods
    // ----------------------------------------------------------------

    /**
     * Return a list of the modes that are interesting to the MapMouseListener.
     * The source MouseEvents will only get sent to the MapMouseListener if the
     * mode is set to one that the listener is interested in. Layers interested
     * in receiving events should register for receiving events in "select"
     * mode: <code>
     * <pre>
     * return new String[] { SelectMouseMode.modeID };
     * </pre>
     * <code>
     * 
     * @return String[] of modeID's
     * @see com.bbn.openmap.event.NavMouseMode#modeID
     * @see com.bbn.openmap.event.SelectMouseMode#modeID
     * @see com.bbn.openmap.event.NullMouseMode#modeID
     */
    public String[] getMouseModeServiceList() {
        return new String[] {
            com.bbn.openmap.event.SelectMouseMode.modeID
        };
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mousePressed(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseReleased(MouseEvent e) {
        OMGraphicList omgraphics = getList();
        if (omgraphics != null && drillData != null) {
            OMGraphic obj = omgraphics.findClosest(e.getX(), e.getY(), 4);
            if (obj != null) {
                int id = ((Integer) obj.getAppObject()).intValue();
                fireRequestInfoLine(drillData[id]);
                showingInfoLine = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component. The listener will
     * receive this event if it successfully processed
     * <code>mousePressed()</code>, or if no other listener processes the event.
     * If the listener successfully processes mouseClicked(), then it will
     * receive the next mouseClicked() notifications that have a click count
     * greater than one.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseClicked(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * The listener will receive these events if it successfully processes
     * mousePressed(), or if no other listener processes the event.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseDragged(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component (with no
     * buttons down).
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseMoved(MouseEvent e) {
        // clean up display
        if (showingInfoLine) {
            showingInfoLine = false;
            fireRequestInfoLine("");
        }
        return false;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed. This event
     * is intended to tell the listener that there was a mouse movement, but
     * that the event was consumed by another layer. This will allow a mouse
     * listener to clean up actions that might have happened because of another
     * motion event response.
     */
    public void mouseMoved() {
    }

    // ----------------------------------------------------------------
    // PropertyConsumer Interface
    // ----------------------------------------------------------------

    /**
     * Set the properties of the EarthquakeLayer.
     * 
     * @param prefix String
     * @param props Properties
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        // list of sites
        String sites = props.getProperty(prefix + fingerSitesProperty);
        if (sites != null) {
            Vector v = new Vector();
            String str;
            StringTokenizer tok = new StringTokenizer(sites);
            while (tok.hasMoreTokens()) {
                str = tok.nextToken();
                v.addElement(str);
            }
            int len = v.size();
            fingerSites = new String[len];
            activeSites = new boolean[len];
            activeSites[0] = true;
            for (int i = 0; i < len; i++) {
                fingerSites[i] = (String) v.elementAt(i);
            }
        }

        fetchIntervalMillis = PropUtils.intFromProperties(props, prefix + queryIntervalProperty, 300) * 1000;
    }

    /**
     * Get the associated properties object.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        return getProperties(propertyPrefix, props);
    }

    /**
     * Get the associated properties object. This method creates a Properties
     * object if necessary and fills it with the relevant data for this layer.
     * Relevant properties for EarthquakeLayers are the sites to retrieve earth
     * quake data from, and the interval in milliseconds (see class
     * description.)
     */
    public Properties getProperties(String prefix, Properties props) {
        props = super.getProperties(props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        StringBuffer sitesToFinger = new StringBuffer("");
        for (int i = 0; i < fingerSites.length; ++i) {
            sitesToFinger.append(fingerSites[i]);
            sitesToFinger.append(" ");
        }

        sitesToFinger.deleteCharAt(sitesToFinger.length() - 1);

        props.put(prefix + fingerSitesProperty, sitesToFinger.toString());
        props.put(prefix + queryIntervalProperty, Long.toString(fetchIntervalMillis));
        return props;
    }

    /**
     * Supplies the propertiesInfo object associated with this EarthquakeLayer
     * object. Contains the human readable descriptions of the properties and
     * the <code>initPropertiesProperty</code> (see Inspector class.)
     */
    public Properties getPropertyInfo(Properties info) {
        info = super.getPropertyInfo(info);

        info.put(fingerSitesProperty, "WWW sites to finger");
        info.put(queryIntervalProperty, "Query interval in seconds");
        return info;
    }
}