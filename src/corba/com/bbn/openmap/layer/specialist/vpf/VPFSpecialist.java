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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/vpf/VPFSpecialist.java,v $
// $RCSfile: VPFSpecialist.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.vpf;

import java.io.File;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.corba.CSpecialist.CheckButton;
import com.bbn.openmap.corba.CSpecialist.Comp;
import com.bbn.openmap.corba.CSpecialist.MouseEvent;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.WidgetChange;
import com.bbn.openmap.layer.specialist.SCheckBox;
import com.bbn.openmap.layer.specialist.Specialist;
import com.bbn.openmap.layer.vpf.LibrarySelectionTable;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.http.HttpServer;
/**
 * Implement the Specialist interface so that we can serve graphics to
 * OpenMap via CORBA.
 * <p>
 * 
 * In the properties file for OpenMap, you can set parameters that
 * determine what coverages and features are sent back to the
 * CSpecLayer. Specifically, these parameters are listed in the
 * staticArgs property as (key, value) pairs. For political boundaries
 * and coastlines, for instance:
 * <p>
 * 
 * staticArgs=(coverageType, bnd)(featureTypes, edge area) (edge,
 * polbndl coastl) (area, polbnda) (lineColor, FFFF0000) (fillColor,
 * FFAAAA66) (draw, edge area)
 * 
 * <p>
 * 
 * The (draw, edge area) pairing should be used if the user won't have
 * access to the palette for the CSpecLayer. The palette controls
 * whether certain feature types are drawn, and by default, areas are
 * not and need to be turned on in the palette. If you set the draw
 * key, the feature types listed will be forced to be returned if they
 * exist. The keys used here should match the featureTypes list, or
 * not, depending on what you want.
 * 
 * <p>
 * 
 * The contents of this staticArgs line should reflect the properties
 * that can be set via the VPFLayer properties. See those javadocs for
 * more details.
 */
public class VPFSpecialist extends Specialist {

    private String dcwpath;
    private Hashtable checkboxes = new Hashtable();
    private LibrarySelectionTable lst;
    private Hashtable comphash = new Hashtable();
    public static final String prefix = "VPFSpec";
    public static final float altCovScale = 30000000f;

    /**
     * default constructor is called when we're loading the class
     * directly into OpenMap.
     */
    public VPFSpecialist() {
        super("VPFSpecialist", (short) 2, true);
        //startHttpServer(0);
        initDcwFiles();
    }

    public VPFSpecialist(LibrarySelectionTable libtab) {
        super("VPFSpecialist", (short) 2, true);
        //startHttpServer(0);
        lst = libtab;
    }

    public VPFSpecialist(File dcwDataPath) {
        super("VPFSpecialist", (short) 2, true);
        //startHttpServer(0);
        dcwpath = dcwDataPath.getAbsolutePath();
        initDcwFiles();
    }

    public VPFSpecialist(String name) {
        super(name, (short) 2, true);
    }

    public java.util.Properties processTokens(String args, StringBuffer cov,
                                              StringBuffer altcov,
                                              boolean usealt) {

        java.util.StringTokenizer tok = new java.util.StringTokenizer(args, ")");

        String token = null;
        java.util.Properties props = new java.util.Properties();

        // First see if there are parens, otherwise just straight
        // coverage types
        while (true) {
            try {
                token = tok.nextToken();
                token = token.trim();

                if (token.startsWith("(")) {

                    // Get the name
                    // Remove the open paren
                    token = token.substring(1, token.length());
                    int comma = token.indexOf(",");
                    String key = token.substring(0, comma);
                    String value = token.substring(comma + 1, token.length());

                    // Remove whitespace
                    key = key.trim();
                    value = value.trim();

                    // Need to set cov or altcov or else props later
                    if (key.equals("coverageType")) {
                        // Coverage types require special handling,
                        // since
                        // coverage types get passed back to drawTile
                        // routine
                        // Check to see if one coverage or two
                        int spacedelim = value.indexOf(" ");
                        if (spacedelim == -1) {
                            // One coverage
                            cov.delete(0, cov.length());
                            cov = cov.append(value);
                            altcov = altcov.delete(0, altcov.length());
                            altcov.append("");

                        } else {
                            // Two coverages
                            cov.delete(0, cov.length());
                            cov = cov.append(value.substring(0, spacedelim));
                            altcov = altcov.delete(0, altcov.length());
                            altcov = altcov.append(value.substring(spacedelim + 1,
                                    value.length()));
                        }

                        // Set the property
                        props.setProperty(prefix + "." + key, value);

                    } else {
                        // Not a coverage type, so don't have to
                        // fool with cov and altcov
                        // Need to check for alt key prefix
                        // !! Assumes coverageType has been processed
                        // first

                        // If there is an alternate coverage and it's
                        // required, use it
                        if (usealt == true && altcov.length() > 0) {
                            if (key.startsWith("alt")) {
                                key = key.substring(3, key.length()); // 3 is
                                                                      // "alt",
                                                                      // remove
                                                                      // it
                                props.setProperty(prefix + "." + key, value);
                            } else {

                                // Need alt, not an alt prefix
                                continue;
                            }

                        } else {
                            // Alt not necessary
                            if (key.startsWith("alt")) {
                                // alt key, don't want it
                                continue;
                            } else {
                                props.setProperty(prefix + "." + key, value);
                            }
                        }
                    }

                } else {
                    // This case is for old style static args, when
                    // they
                    // were not (key,value) format

                    // Find out if there is an alternate coverage
                    int spacedelim = token.indexOf(" ");
                    if (spacedelim == -1) {
                        // No alternate coverage
                        cov.delete(0, cov.length());
                        cov = cov.append(token);
                        altcov = altcov.delete(0, altcov.length());
                        altcov.append("");
                    } else {
                        // Yes, alternate coverage
                        cov.delete(0, cov.length());
                        cov = cov.append(token.substring(0, spacedelim));
                        cov = altcov.delete(0, altcov.length());
                        altcov = altcov.append(token.substring(spacedelim + 1,
                                token.length()));
                    }
                }
            } catch (java.util.NoSuchElementException nee) {
                break;
            }
        }
        return props;
    }

    public UGraphic[] fillRectangle(
                                    com.bbn.openmap.corba.CSpecialist.CProjection p,
                                    com.bbn.openmap.corba.CSpecialist.LLPoint ll1,
                                    com.bbn.openmap.corba.CSpecialist.LLPoint ll2,
                                    java.lang.String staticArgs,
                                    org.omg.CORBA.StringHolder dynamicArgs,
                                    com.bbn.openmap.corba.CSpecialist.GraphicChange notifyOnChange,
                                    String uniqueID) {
        try {
            LatLonPoint newll1 = new LatLonPoint.Double(ll1.lat, ll1.lon);
            LatLonPoint newll2 = new LatLonPoint.Double(ll2.lat, ll2.lon);

            /*
             * If the we are zoomed out so that we _might_ have the
             * entire world on the screen, then let's check to see if
             * east and west are approximately equal. If they are,
             * change them to -180 and +180 so that we get the entire
             * world without having trouble with the floating point
             * rounding errors that allow west to be slightly less
             * than east (by .00001) and confuse our clipping routines
             * into thinking that there should be nothing on the
             * screen since nothing falls in that .00001 slice of the
             * world.
             */
            if ((p.scale > 100000000)
                    && MoreMath.approximately_equal(ll1.lon, ll2.lon, .01)) {
                newll1.setLongitude(-180.0f);
                newll2.setLongitude(180.0f);
            }

            forgetComps(uniqueID);
            Debug.message("vpfspecialist", "fillRectangle.. " + staticArgs);
            Hashtable dynArgs = parseDynamicArgs(dynamicArgs.value);
            VPFSpecialistGraphicWarehouse warehouse = new VPFSpecialistGraphicWarehouse();
            CheckButton buttons[] = null;
            SCheckBox s = (SCheckBox) checkboxes.get(uniqueID);
            if (s == null) {
                buttons = new CheckButton[3];
                Debug.message("vpfspecialist", "default buttons");
                buttons[0] = new CheckButton("Edges", false);
                buttons[1] = new CheckButton("Text", false);
                buttons[2] = new CheckButton("Area", false);
            } else {
                Debug.message("vpfspecialist", "palette buttons");
                buttons = s.buttons();
            }

            boolean showEdges = (getHashedValueAsBoolean(dynArgs, DynArgEdges) || buttons[0].checked);
            warehouse.setEdgeFeatures(showEdges);

            boolean showText = (getHashedValueAsBoolean(dynArgs, DynArgText) || buttons[1].checked);
            warehouse.setTextFeatures(showText);

            boolean showAreas = (getHashedValueAsBoolean(dynArgs, DynArgArea) || buttons[2].checked);
            warehouse.setAreaFeatures(showAreas);

            // Changeable String objects for processTokens call
            StringBuffer retcov = new StringBuffer();
            StringBuffer retaltcov = new StringBuffer("");

            // Now need to know in advance if we are using an
            // alternate coverage
            // for arguments
            boolean usealt;
            if (p.scale >= altCovScale)
                usealt = true;
            else
                usealt = false;

            java.util.Properties props = processTokens(staticArgs,
                    retcov,
                    retaltcov,
                    usealt);

            if (Debug.debugging("vpfspecialist")) {
                Debug.output("VPFSpecialist: with prefix " + prefix
                        + ", properties: " + props);
            }

            // After processing properties, make sure they're
            // available
            warehouse.setProperties(prefix, props);

            // Trim whitespace on coverages
            String cov = retcov.toString().trim();
            String altcov = retaltcov.toString().trim();

            // If got nothing back, null this object (hack to convert
            // StringBuffer to String
            if (altcov.equals("")) {
                Debug.message("vpfspecialist", "Altcov = null");
                altcov = null;
            }

            //warehouse.showPointFeatures(false);

            // tokenize the staticArgs. should be coverage type
            // followed by alternate coverage type.

            // Check both dynamic args and palette values when
            // deciding what to draw.
            lst.drawTile(p.scale,
                    p.width,
                    p.height,
                    ((altcov != null) && (p.scale >= altCovScale)) ? altcov
                            : cov,
                    warehouse,
                    newll1,
                    newll2);

            UGraphic[] retlist = warehouse.packGraphics();

            if (Debug.debugging("vpfspecialist")) {
                Debug.output("retlist.size(): " + retlist.length);
            }

            // DFD - I've commented the comphash method because I
            // can't figure out where it is used. In the
            // MATT Specialists, we processed mouse events on
            // particular objects, and allowed things to happen on the
            // server side. For this specialist, the receiveGesture
            // method doesn't do anything, and we risk keeping all
            // these objects around if the client dies before coming
            // back to clean up.

            //          comphash.put(uniqueID, warehouse.getComps());
            warehouse = null; // clean up.

            Debug.message("vpfspecialist", "returning from fillRectangle");

            return retlist;
        } catch (Throwable t) {
            t.printStackTrace();
            return new UGraphic[0];
        }
    }

    private synchronized void forgetComps(String uniqueID) {
        Comp[] oldcomps = (Comp[]) comphash.remove(uniqueID);
        if (oldcomps != null) {
            Debug.message("vpfspecialist", "Releasing comps");

            // Not sure how to do this in POA, but shouldn't be
            // a problem because we aren't adding the comp objects to
            // the graphics in the warehouse either.
            //          for (int i = 0; i < oldcomps.length; i++) {
            //              if (boa != null)
            //                  boa.deactivate_obj(oldcomps[i]);
            //          }
        }
    }

    public void receiveGesture(MouseEvent gesture, String uniqueID) {
    //      System.out.println("Gesture away...");
    //      addInfoText("Some text string");
    }

    public void makePalette(WidgetChange notifyOnChange, String staticArgs,
                            org.omg.CORBA.StringHolder dynamicArgs,
                            String uniqueID) {
        clearPalette();

        CheckButton buttons[] = new CheckButton[3];
        buttons[0] = new CheckButton("Edges", false);
        buttons[1] = new CheckButton("Text", false);
        buttons[2] = new CheckButton("Area", false);
        SCheckBox cb = new UsefulCheckbox("Feature Types:", buttons);
        checkboxes.put(uniqueID, cb);
        addPalette(cb.widget());
    }

    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-dcwpath")
                    || args[i].equalsIgnoreCase("-datapath")) {
                //              dcwpath = new File(args[++i]);
                dcwpath = args[++i];
            }
            if (args[i].equalsIgnoreCase("-verbose")) {
                Debug.put("vpfspecialist");
            }
        }

        super.parseArgs(args);

        initDcwFiles();
    }

    public void initDcwFiles() {
        if (dcwpath == null) {
            dcwpath = "/usr/local/matt/data/dcw";
        }
        try {
            // Parse the string to get all the possible paths...
            String[] paths = parsePaths(dcwpath);
            if (paths != null) {
                lst = new LibrarySelectionTable();

                for (int i = 0; i < paths.length; i++) {
                    System.out.println("VPFSpecialist: adding " + paths[i]
                            + " to server");
                    lst.addDataPath(paths[i]);
                }
            }
        } catch (com.bbn.openmap.io.FormatException f) {
            throw new java.lang.IllegalArgumentException(f.getMessage());
        }
    }

    /**
     * Take a string that represents a bunch of path names separated
     * by ";", and return an array of Strings.
     */
    public String[] parsePaths(String path) {
        String[] ret = null;
        String tok = ";";
        if (path != null) {
            if (Debug.debugging("vpfspecialist")) {
                System.out.println("VPFSpecialist: parsing path string: "
                        + path);
            }
            try {
                StringTokenizer token = new StringTokenizer(path, tok);
                int numPaths = token.countTokens();

                ret = new String[numPaths];
                for (int i = 0; i < numPaths; i++) {
                    ret[i] = token.nextToken();
                }
                return ret;
            } catch (java.util.NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void printHelp() {
        System.err.println("usage: java <specialist> -ior <file> -datapath <path1;path2;path3>");
    }

    public void signOff(String uniqueID) {
        forgetComps(uniqueID);
        if (Debug.debugging("vpfspecialist")) {
            Debug.output("Client |" + uniqueID + "| going away...");
        }
    }

    /**
     * Start an HttpServer on a port to listen for table requests
     * 
     * @param port the port to bind. 0 picks an arbitrary port
     * @return the HttpServer that got constructed, <code>null</code>
     *         if there was a problem.
     */
    public static HttpServer startHttpServer(int port) {
        try {
            HttpServer server = new HttpServer(port, true);
            server.addHttpRequestListener(new TableListener());
            return server;
        } catch (java.io.IOException e) {
            Debug.output("Unable to start http server:");
            return null;
        }
    }

    public final static String DynArgEdges = "edges";
    public final static String DynArgText = "text";
    public final static String DynArgArea = "area";

    /**
     * Parses dynamic args passed by specialist client. A
     * <code>Hashtable</code> is returned as a unified holder of all
     * dynamic arguments.
     */

    public static Hashtable parseDynamicArgs(String args) {
        Hashtable dynArgs = new Hashtable();
        if (args != null) {
            String lowerArgs = args.toLowerCase();

            dynArgs.put(DynArgEdges,
                    new Boolean(lowerArgs.indexOf(DynArgEdges) != -1));
            dynArgs.put(DynArgText,
                    new Boolean(lowerArgs.indexOf(DynArgText) != -1));
            dynArgs.put(DynArgArea,
                    new Boolean(lowerArgs.indexOf(DynArgArea) != -1));
        }
        return dynArgs;
    }

    /**
     * If <code>arg</code> maps to a <code>Boolean</code> in the
     * Hashtable, that value is returned, <code>false</code>
     * otherwise.
     * 
     * @param dynArgs the Hashtable to look in
     * @param arg the argument to return
     */
    public static boolean getHashedValueAsBoolean(Hashtable dynArgs, String arg) {
        Object obj = dynArgs.get(arg);
        if (obj == null) {
            return false;
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        Debug.init(System.getProperties());
        Debug.message("vpfspecialist", "VPFSpecialist starting up");
        int port = 0;
        startHttpServer(port);

        // Create the specialist server
        VPFSpecialist srv = new VPFSpecialist("VPFSpecialist");
        srv.start(args);
    }
}