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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/LayerGraphicWarehouseSupport.java,v $
// $Revision: 1.6 $ $Date: 2004/02/01 21:21:59 $ $Author: dietrick $
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FanCompress;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.io.FormatException;

import java.awt.Color;
import java.awt.Component;
import java.util.*;

/**
 * Implement a graphic factory that builds OMGraphics.
 * 
 * @see com.bbn.openmap.omGraphics.OMGraphic
 */
public abstract class LayerGraphicWarehouseSupport
    implements VPFGraphicWarehouse {

    protected DrawingAttributes drawingAttributes = new DrawingAttributes();

    /** HACK around antarctica display problem. */
    final transient protected static float antarcticaThreshold =
        ProjMath.degToRad(-89.9f);

    /** hang on to the graphics that we build */
    protected OMGraphicList graphics;

    /** remember if we draw edge features */
    private boolean drawEdgeFeatures;
    /** remember if we draw text features */
    private boolean drawTextFeatures;
    /** remember if we draw area features */
    private boolean drawAreaFeatures;
    /** remember if we draw entity point features */
    private boolean drawEPointFeatures;
    /** remember if we draw connected point features */
    private boolean drawCPointFeatures;

    /** thinning variables.  note that thinning is meant to be done
      offline, so this is not optimized... */
    private static boolean doThinning = false;
    private static double fan_eps = 0.01f;

    /**
     * Construct an object, initialiazes graphiclist
     */
    public LayerGraphicWarehouseSupport() {
        graphics = new OMGraphicList();
        graphics.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);
    }
                               
    /**
     * Get the current graphics list.
     * @return the OMGraphicList.
     */
    public OMGraphicList getGraphics() {
        return graphics;
    }

    /**
     * Get the DrawingAttributes used for the coverage type.
     */
    public DrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    /**
     * Set the drawing attributes for the coverage type.
     */
    public void setDrawingAttributes(DrawingAttributes da) {
        drawingAttributes = da;
    }
    
    /** 
     * Lets the warehouse know that a different
     * CoverageAttributeTable will be using it.  Default action is to
     * do nothing.  
     */
    public void resetForCAT() {}

    /**
     * Set which library to use. If null, all applicable libraries in
     * database will be searched.
     */
    private String useLibrary = null;

    /**
     * Set the VPF library to use.  If null, all libraries will be
     * searched.  Null is default.
     */
    public void setUseLibrary(String lib) {
        useLibrary = lib;
    }

    /**
     * Get the VPF library to use.
     */
    public String getUseLibrary() {
        return useLibrary;
    }

    /**
     * Return the GUI for certain warehouse attributes.  By default,
     * return the GUI for the DrawingAttributes object being used for
     * rendering attributes of the graphics.  
     *
     * @param lst LibrarySelectionTable to use to get information
     * about the data, if needed.  Not needed here.
     */
    public Component getGUI(LibrarySelectionTable lst) {
        if (drawingAttributes != null) {
            return drawingAttributes.getGUI();
        } else {
            return null;
        }
    }

    /**
     * Clears the contained list of graphics.
     */
    public void clear() {
        graphics.clear();
    }

    /**
     * set if we draw edge features
     * @param newvalue <code>true</code> for drawing, false otherwise
     */
    public void setEdgeFeatures(boolean newvalue) {
        drawEdgeFeatures = newvalue;
    }

    /**
     * Return true if we may draw some edge features.
     */
    public boolean drawEdgeFeatures() {
        return drawEdgeFeatures;
    }

    /**
     * set if we draw text features
     * @param newvalue <code>true</code> for drawing, false otherwise
     */
    public void setTextFeatures(boolean newvalue) {
        drawTextFeatures = newvalue;
    }

    /**
     * Return true if we may draw some text features.
     */
    public boolean drawTextFeatures() {
        return drawTextFeatures;
    }
  
    /**
     * set if we draw area features
     * @param newvalue <code>true</code> for drawing, false otherwise
     */
    public void setAreaFeatures(boolean newvalue) {
        drawAreaFeatures = newvalue;
    }

    /**
     * Return true if we may draw some area features.
     */
    public boolean drawAreaFeatures() {
        return drawAreaFeatures;
    }

    /**
     * set if we draw entity point features
     * @param newvalue <code>true</code> for drawing, false otherwise
     */
    public void setEPointFeatures(boolean newvalue) {
        drawEPointFeatures = newvalue;
    }

    /**
     * Return true if we may draw some entity point features.
     */
    public boolean drawEPointFeatures() {
        return drawEPointFeatures;
    }

    /**
     * set if we draw connected point features
     * @param newvalue <code>true</code> for drawing, false otherwise
     */
    public void setCPointFeatures(boolean newvalue) {
        drawCPointFeatures = newvalue;
    }

    /**
     * Return true if we may draw some connected point features.
     */
    public boolean drawCPointFeatures() {
        return drawCPointFeatures;
    }

    /**
     * Sets the features (lines, areas, text, points) that get displayed
     * @param features a whitespace-separated list of features to display
     */
    public void setFeatures(String features) {
        // If someone gives us a list of features, we need to make sure thats
        // what we use.
        setAreaFeatures(false);
        setEdgeFeatures(false);
        setTextFeatures(false);
        setEPointFeatures(false);
        setCPointFeatures(false);
        StringTokenizer t = new StringTokenizer(features);
        while (t.hasMoreTokens()) {
            String token = t.nextToken();
            if (token.equalsIgnoreCase(VPFUtil.Area)) {
                setAreaFeatures(true);
            } else if (token.equalsIgnoreCase(VPFUtil.Edge)) {
                setEdgeFeatures(true);
            } else if (token.equalsIgnoreCase(VPFUtil.EPoint)) {
                setEPointFeatures(true);
            } else if (token.equalsIgnoreCase(VPFUtil.CPoint)) {
                setCPointFeatures(true);
            } else if (token.equalsIgnoreCase(VPFUtil.Text)) { 
                setTextFeatures(true);
            } else {
                Debug.output("LayerGraphicsWarehouseSupport: ignoring feature: " + token);
            }
        }
    }
      
    /**
     * set drawing attribute properties
     * @param prefix the prefix for our properties
     * @param props the Properties object we use to look up valuse
     */
    public void setProperties(String prefix, Properties props) {
        String features;
        
        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        features = props.getProperty(realPrefix + VPFLayer.featureTypesProperty);
        drawingAttributes.setProperties(prefix, props);

        if (features != null) {
            setFeatures(features);
        }
    }

    /**
     * create a filled polygon
     * @param ipts a list of CoordFloatString objects
     * @param totalSize the total number of points
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @param doAntarcticaWorkaround hack for funny DCW antarctica data
     * (passed to warehouse)
     */
    public static OMPoly createAreaOMPoly(List ipts, int totalSize,
                                          LatLonPoint ll1, LatLonPoint ll2,
                                          float dpplat, float dpplon,
                                          boolean doAntarcticaWorkaround) {
        int i, j, size = ipts.size();
        int npts=0;

        // thin the data
//      if (doThinning) {
//          totalSize = doThinning(ipts);
//      }

        // *2 for pairs
        float [] llpts = new float[totalSize*2];

        // only do it if we're in the vicinity
        if (doAntarcticaWorkaround) {
            doAntarcticaWorkaround = (ll2.getLatitude() < -62f);
        }

        for (j=0; j<size; j++) {
            CoordFloatString cfs = (CoordFloatString)ipts.get(j);
            int cfscnt = cfs.tcount;
            int cfssz = cfs.tsize;
            float cfsvals[] = cfs.vals;
            if (cfscnt > 0) {   // normal
                for (i=0; i < cfscnt; i++) {
                    llpts[npts++] = ProjMath.degToRad(cfsvals[i*cfssz+1]);//lat
                    llpts[npts++] = ProjMath.degToRad(cfsvals[i*cfssz]);//lon
                }
            } else {            // reverse
                cfscnt *= -1;
                for (i=cfscnt-1; i>=0; i--) {
                    llpts[npts++] = ProjMath.degToRad(cfsvals[i*cfssz+1]);//lat
                    llpts[npts++] = ProjMath.degToRad(cfsvals[i*cfssz]);//lon
                }
            }
        }

        // HACK: we will rewrite the data for the Antarctica polygon so that
        // it will display "correctly" in the cylindrical projections.
        //only check if bottom edge of screen below a certain latitude
        if (doAntarcticaWorkaround) {
            float[] newllpts = new float[llpts.length];
            for (i=0; i < newllpts.length; i+=2) {
                newllpts[i] = llpts[i];
                newllpts[i+1] = llpts[i+1];

                if (newllpts[i] < antarcticaThreshold)
                {
                    Debug.message("vpf", "AreaTable.generateOMPoly(): Antarctica!");
                    //HACK: we're assuming data is going from west to east,
                    //so we wrap the other way
                    newllpts[  i] = ProjMath.degToRad(-89.99f); newllpts[++i] = ProjMath.degToRad(179.99f);
                    newllpts[++i] = ProjMath.degToRad(-89.99f); newllpts[++i] = ProjMath.degToRad(90f);
                    newllpts[++i] = ProjMath.degToRad(-89.99f); newllpts[++i] = ProjMath.degToRad(0f);
                    newllpts[++i] = ProjMath.degToRad(-89.99f); newllpts[++i] = ProjMath.degToRad(-90f);
                    newllpts[++i] = ProjMath.degToRad(-89.99f); newllpts[++i] = ProjMath.degToRad(-179.99f);
                    ++i;//lat
                    //HACK: advance to western hemisphere where we
                    //pick up the real data again
                    while (llpts[i+1] > 0) {//lon
                        newllpts[i] = ProjMath.degToRad(-89.99f);
                        newllpts[++i] = ProjMath.degToRad(-179.99f);
                        ++i;//lat
                    }
                    i-=2;
                }
            }
            llpts = newllpts;
        }

        // create polygon - change to OMPoly for jdk 1.1.x compliance.
        OMPoly py = new OMPoly(llpts,
                               OMGraphic.RADIANS,
                               OMGraphic.LINETYPE_STRAIGHT);

        return py;
  }

    /**
     * Create an OMPoly corresponding to a VPF edge feature
     * @param coords the coordinates to use for the poly
     * @param ll1 upper left, used for clipping
     * @param ll2 lower right, used for clipping
     * @param dpplat used for latitude thinning
     * @param dpplon used for longitude thinning
     */
    public static OMPoly createEdgeOMPoly(CoordFloatString coords,
                                          LatLonPoint ll1, LatLonPoint ll2,
                                          float dpplat, float dpplon)
    {
        // thin the data
//      if (doThinning) {
//          List ipts = new ArrayList(1);
//          ipts.add(coords);
//          doThinning(ipts);
//      }

        float[] llpts = coords.vals; //NOTE: lon,lat order!

        // handle larger tuples (do extra O(n) loop to extract only
        // lon/lats.
        if (coords.tsize > 2) {// assume 3
            /*
            if (Debug.debugging("vpf")) {
                Debug.output("EdgeTable.drawTile: big tuple size: "
                             + coords.tsize);
            }
            */
            float[] newllpts = new float[coords.tcount*2];//*2 for pairs
            int len = newllpts.length;
            for (int i=0, j=0; i<len; i+=2, j+=3) {
                newllpts[i] = ProjMath.degToRad(llpts[j+1]);//lat
                newllpts[i+1] = ProjMath.degToRad(llpts[j]);//lon
            }
            llpts = newllpts;
        } else {
            float lon;
            int len = llpts.length;
            for (int i=0; i<len; i+=2) {
                lon = ProjMath.degToRad(llpts[i]);
                llpts[i] = ProjMath.degToRad(llpts[i+1]);//lat
                llpts[i+1] = lon;//lon
            }
        }

        // create polyline - change to OMPoly for jdk 1.1.x compliance.
        OMPoly py = new OMPoly(llpts,
                               OMGraphic.RADIANS,
                               OMGraphic.LINETYPE_STRAIGHT);
        return py;
    }


    /**
     * Set doThinning.
     * @param value boolean
     */
    public static void setDoThinning(boolean value) {
        doThinning = value;
    }


    /**
     * Are we thinning?.
     * @return boolean
     */
    public static boolean isDoThinning() {
        return doThinning;
    }


    /**
     * Set fan compression epsilon.
     * @param value double
     */
    public static void setFanEpsilon(double value) {
        fan_eps = value;
    }


    /**
     * Get fan compression epsilon.
     * @return double
     */
    public static double getFanEpsilon() {
        return fan_eps;
    }

    /**
     * do fan compression of raw edge points
     */
    protected static int doThinning(List ipts) {
        int size = ipts.size();
        int totalSize=0;
        for (int j=0; j<size; j++) {

            // get poly
            CoordFloatString cfs = (CoordFloatString)ipts.get(j);
            int cfscnt = cfs.tcount;
            int cfssz = cfs.tsize;
            float[] cfsvals = cfs.vals;
            int npts = 0;

            // handle reverse
            boolean rev = (cfscnt < 0);
            if (rev) {
                cfscnt = -cfscnt;
            }

            // copy points
            float[] llpts = new float[cfscnt<<1];
            for (int i=0; i < cfscnt; i++) {
                llpts[npts++] = cfsvals[i*cfssz];//lon
                llpts[npts++] = cfsvals[i*cfssz+1];//lat
            }

            // thin points
            FanCompress.FloatCompress fan = new FanCompress.FloatCompress(llpts);
            FanCompress.fan_compress(fan, fan_eps);

            // install new points
            cfs.vals = fan.getArray();  // install thinned 2-tuple array
            cfs.tcount = cfs.vals.length>>>1;// num pairs
            cfs.tsize = 2;// HACK lossy...
            totalSize+=cfs.tcount;
            if (rev) {
                cfs.tcount *= -1;
            }
        }
        return totalSize;
    }

    /**
     * Create an OMText object corresponding to a VPF text feature
     * @param text the text
     * @param latitude the latitude of where to place the text
     * @param longitude the longitude of where to place the text
     */
    public static OMText createOMText(String text,
                                      float latitude, float longitude) {

        OMText txt = new OMText(latitude, longitude,
                                text, OMText.JUSTIFY_LEFT);
        return txt;
    }

    /**
     * Create an OMPoint object corresponding to a VPF node feature
     * @param latitude the latitude of where to place the text
     * @param longitude the longitude of where to place the text
     */
    public static OMPoint createOMPoint(float latitude, float longitude) {

        return new OMPoint(latitude, longitude);
    }
}
