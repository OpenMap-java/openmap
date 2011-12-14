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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/LOSGenerator.java,v $
// $RCSfile: LOSGenerator.java,v $
// $Revision: 1.7 $
// $Date: 2005/12/09 21:09:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressListener;
import com.bbn.openmap.event.ProgressSupport;
import com.bbn.openmap.gui.ProgressListenerGauge;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.Planet;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;
import com.bbn.openmap.util.stateMachine.State;

/**
 * The LOSGenerator uses gestures to create a mask over the map. The
 * circular mask of green pixels shows what places are within the
 * sight of the center of the circle. Additional height can be added
 * to the center of the circle via the TerrainLayer palette, to
 * represent a tower, building, or aircraft.
 */
public class LOSGenerator implements TerrainTool {

    // These are used to control the algorithm type. Right now, the
    // first two are eliminated, since the azimuth algorithm is
    // faster
    // and more precise.
    final static int PRECISE = 0;
    final static int GOODENOUGH = 1;
    final static int AZIMUTH = 2;

    // RED
    Color toolColor = new Color(255, 0, 0);

    // The colors of pixels
    final static int INVISIBLE = 0;
    final static int VISIBLE = 1;
    final static int MAYBEVISIBLE = 2;
    int[] colortable;

    Projection proj;
    protected LOSStateMachine stateMachine;
    TerrainLayer layer;

    /** Lat lon of the center of hte circle. */
    LatLonPoint LOScenterLLP;
    /** The xy of the center of the circle. */
    Point LOScenterP = new Point();
    /** The height of the earth at the center point. */
    int LOScenterHeight;
    /** The height of the object at the center point. */
    int LOSobjectHeight = 0;
    /** The diameter of the circle of interest. */
    int LOSedge;
    protected OMGraphicList graphics = new OMGraphicList();
    OMRaster LOSimage; // The image for the mask
    OMCircle LOScirc; // The circle modified for the image definition
    int LOSprecision; // The flag for the algorithm type
    LatLonPoint LOSOffPagell = new LatLonPoint.Double(-79f, -170f);
    Point LOSOffPagep1 = new Point(-10, -10);

    /** The thread worker used to create the Terrain images. */
    LOSWorker currentWorker;
    /**
     * Set when the projection has changed while a swing worker is
     * gathering graphics, and we want him to stop early.
     */
    protected boolean cancelled = false;

    protected ProgressSupport progressSupport;

    class LOSWorker extends SwingWorker {
        /** Constructor used to create a worker thread. */
        public LOSWorker() {}

        /**
         * Compute the value to be returned by the <code>get</code>
         * method.
         */
        public Object construct() {
            Debug.message("terrain", layer.getName() + "|LOSWorker.construct()");
            layer.fireStatusUpdate(LayerStatusEvent.START_WORKING);
            createLOSImage();
            return null;
        }

        /**
         * Called on the event dispatching thread (not on the worker
         * thread) after the <code>construct</code> method has
         * returned.
         */
        public void finished() {
            layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
            workerComplete();
        }
    }

    /**
     * Not the preferred way to create one of these. It's full of
     * defaults.
     */
    private LOSGenerator() {
        init();
    }

    /**
     * The creation of the tool starts here. The DTED data cache is
     * passed in, along with a path to the dted directory to get more
     * data if needed.
     */
    public LOSGenerator(TerrainLayer tLayer) {
        layer = tLayer;
        init();
    }

    public synchronized OMGraphicList getGraphics() {
        return graphics;
    }

    public State getState() {
        return stateMachine.getState();
    }

    public void init() {
        progressSupport = new ProgressSupport(this);
        addProgressListener(new ProgressListenerGauge("LOS Mask Creation"));

        // colortable
        colortable = new int[3];
        colortable[INVISIBLE] = new Color(0, 0, 0, 0).getRGB();
        colortable[VISIBLE] = new Color(0, 255, 0, 255).getRGB();
        colortable[MAYBEVISIBLE] = new Color(255, 255, 0, 255).getRGB();

        stateMachine = new LOSStateMachine(this);

        // set the graphics
        reset(true, true);
        graphics.add(LOSimage);
        graphics.add(LOScirc);

    }

    public void doImage() {
        // If there isn't a worker thread working on this already,
        // create a thread that will do the real work. If there is
        // a thread working on this, then set the cancelled flag
        // in the layer.
        if (currentWorker == null) {
            currentWorker = new LOSWorker();
            currentWorker.execute();
        } else
            setCancelled(true);
    }

    /**
     * The TerrainWorker calls this method on the layer when it is
     * done working. If the calling worker is not the same as the
     * "current" worker, then a new worker is created.
     */
    protected synchronized void workerComplete() {
        if (!isCancelled()) {
            currentWorker = null;
            layer.repaint();
        } else {
            setCancelled(false);
            currentWorker = new LOSWorker();
            currentWorker.execute();
        }
    }

    /**
     * Used to set the cancelled flag in the layer. The swing worker
     * checks this once in a while to see if the projection has
     * changed since it started working. If this is set to true, the
     * swing worker quits when it is safe.
     */
    public synchronized void setCancelled(boolean set) {
        cancelled = set;
    }

    /** Check to see if the cancelled flag has been set. */
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    /**
     * Without arguments, the reset() call makes both graphics go
     * offscreen in their smallest size.
     */
    public void reset() {
        reset(true, true);
    }

    /**
     * Circ is for the circle to be reset, and image is for the image
     * to be reset. Sometimes you only want one to be moved.
     */
    public void reset(boolean circ, boolean image) {
        graphics.clear();
        if (image) {
            LOSimage = new OMRaster(LOSOffPagell.getLatitude(), LOSOffPagell.getLongitude(), LOSOffPagep1.x, LOSOffPagep1.y, 1, 1, new int[1]);
        }
        if (circ) {
            LOScirc = new OMCircle(LOSOffPagell.getLatitude(), LOSOffPagell.getLongitude(), 1, 1);
            LOScirc.setLinePaint(toolColor);
        }
        layer.repaint();
        stateMachine.reset();
    }

    /**
     * Called on every getRectangle, in order to let the cache get
     * sized right, and to reset the graphics if the scale changed
     * (since they won't make sense.
     */
    public void setScreenParameters(Projection p) {
        reset(true, true);
        proj = p;
        LOSprecision = AZIMUTH;
        graphics.generate(proj);
    }

    /**
     * Takes the member settings and manages the creation of the
     * image. A large vector of slope values are created, depending on
     * the size of the circle, and how many pixels are around it. Each
     * entry in the vector is the value of the largest slope value in
     * that direction. The image is created from the inside out, pixel
     * by pixel. The slope from the pixel to the center is calculated,
     * and then compared with the value for that direction (in the
     * vector). If the pixel's slope is larger, the point is visible,
     * and is colored that way. The vector is updated, and the cycle
     * continues.
     */
    public synchronized void createLOSImage() {
        if (Debug.debugging("los")) {
            Debug.output("createLOSimage: Entered with diameter = " + LOSedge);
        }

        if (layer == null || layer.frameCache == null) {
            Debug.error("LOSGenerator:  can't access the DTED data through the terrain layer.");
            return;
        }

        int squareRadius = LOSedge / 2 + 1;
        int[] newPixels = new int[LOSedge * LOSedge];
        float[] azimuthVals = new float[8 * (squareRadius - 1)];
        // center point of raster
        newPixels[((LOSedge / 2) * LOSedge) + squareRadius] = MAYBEVISIBLE;

        if (Debug.debugging("los")) {
            Debug.output("createLOSimage: size of azimuth array = "
                    + azimuthVals.length);
        }

        fireProgressUpdate(ProgressEvent.START,
                "Building LOS Image Mask...",
                0,
                100);
        int x, y;
        boolean mark = false;
        int markColor = colortable[INVISIBLE];
        int range;
        float pix_arc_interval = (float) (2 * Math.PI / azimuthVals.length);
        //  Do this in a spiral, around the center point.
        for (int round = 1; round < squareRadius; round++) {
            if (Debug.debugging("los")) {
                Debug.output("createLOSimage: round " + round);
            }
            y = LOScenterP.y - round;
            x = LOScenterP.x - round;

            if (round == 1) {
                mark = true;
                markColor = colortable[MAYBEVISIBLE];
            }

            else
                mark = false;

            if (LOSprecision == AZIMUTH) { // As of now, this is the
                // only option
                range = ((LOSedge * 4) - 4) / (round * 16);
                for (; x < LOScenterP.x + round; x++)
                    // top
                    resolveImagePoint(x,
                            y,
                            newPixels,
                            azimuthVals,
                            range,
                            pix_arc_interval,
                            mark,
                            markColor);
                for (; y < LOScenterP.y + round; y++)
                    // right
                    resolveImagePoint(x,
                            y,
                            newPixels,
                            azimuthVals,
                            range,
                            pix_arc_interval,
                            mark,
                            markColor);
                for (; x > LOScenterP.x - round; x--)
                    // bottom
                    resolveImagePoint(x,
                            y,
                            newPixels,
                            azimuthVals,
                            range,
                            pix_arc_interval,
                            mark,
                            markColor);
                for (; y > LOScenterP.y - round; y--)
                    // left
                    resolveImagePoint(x,
                            y,
                            newPixels,
                            azimuthVals,
                            range,
                            pix_arc_interval,
                            mark,
                            markColor);
            }

            int whereWeAre = (int) (100f * ((float) round / (float) squareRadius));
            fireProgressUpdate(ProgressEvent.UPDATE,
                    "Analyzing data...",
                    whereWeAre,
                    100);

        }

        fireProgressUpdate(ProgressEvent.UPDATE, "Creating Mask", 100, 100);

        LOSimage = new OMRaster(LOScenterLLP.getLatitude(), LOScenterLLP.getLongitude(), (-1 - LOSedge / 2), (-1 - LOSedge / 2), LOSedge, LOSedge, newPixels);
        LOSimage.generate(proj);
        graphics.clear();
        graphics.add(LOSimage);

        fireProgressUpdate(ProgressEvent.DONE, "LOS mask complete", 100, 100);

        if (Debug.debugging("los")) {
            Debug.output("createLOSimage: Done...");
        }
    }

    /**
     * Calculates the color for each pixel. After is gets the slope
     * value for that pixel, it manages the comparison to get the
     * pixel colored correctly.
     */
    protected void resolveImagePoint(int x, int y, int[] newPixels,
                                     float[] azimuthVals, int range,
                                     float pix_arc_interval, boolean mark,
                                     int colorForMark) {

        int ox = LOScenterP.x - LOSedge / 2;
        int oy = LOScenterP.y - LOSedge / 2;
        int dist = TerrainLayer.numPixelsBetween(LOScenterP.x,
                LOScenterP.y,
                x,
                y);
        if (dist > (LOSedge - 1) / 2) {
            mark = true;
            colorForMark = INVISIBLE;
        }
        if (dist == (LOSedge - 1) / 2) {
            mark = true;
            colorForMark = MAYBEVISIBLE;
        }

        // This needs to be before the next two lines after this
        LatLonPoint cord = proj.inverse(x, y, new LatLonPoint.Double());
        x -= ox;
        y -= oy;

        if (Debug.debugging("losdetail")) {
            Debug.output("resolveImagePoint x = " + x + ", y = " + y);
        }

        if (mark == true) {
            newPixels[x + y * LOSedge] = colorForMark;
            mark = false;
            return;
        }

        double arc_dist = LOScenterLLP.distance(cord);
        double arc_angle = LOScenterLLP.azimuth(cord);

        double slope = calculateLOSslope(cord, arc_dist);

        int index = (int) Math.round(arc_angle / pix_arc_interval);
        int maxIndex = (LOSedge * 4) - 4; // 4 corners out for
        // redundancy
        if (index < 0)
            index += maxIndex;
        else if (index >= maxIndex)
            index -= maxIndex;

        if (Debug.debugging("losdetail")) {
            Debug.output(" angle = " + arc_angle + ", index/maxIndex = "
                    + index + "/" + maxIndex + ", slope = " + slope
                    + " compared to slope[index]=" + azimuthVals[index]);
        }
        int color = colortable[INVISIBLE];
        if (azimuthVals[index] < slope) {
            for (int i = (index - range); i < index + range - 1; i++) {
                if (i < 0)
                    azimuthVals[maxIndex + i] = (float)slope;
                else if (i >= maxIndex)
                    azimuthVals[i - maxIndex] = (float)slope;
                else
                    azimuthVals[i] = (float)slope;
            }
            color = colortable[VISIBLE];
        }
        if (Debug.debugging("losdetail")) {
            Debug.output(" color = " + color);
        }
        newPixels[x + y * LOSedge] = color;
    }

    /**
     * CalculateLOSslope figures out the slope from the pixel to the
     * center, in radians. The arc_dist is in radians, and is the
     * radian arc distance of the point from the center point of the
     * image, on the earth. This slope calculation does take the
     * earth's curvature into account, based on the spherical model.
     */
    protected double calculateLOSslope(LatLonPoint cord, double arc_dist) {
        DTEDFrameCache frameCache = layer.frameCache;

        if (frameCache == null) {
            return 0;
        }

        int xyheight = frameCache.getElevation(cord.getLatitude(),
                cord.getLongitude());
        double ret = 0;
        double P = Math.sin(arc_dist)
                * (xyheight + Planet.wgs84_earthEquatorialRadiusMeters);

        double xPrime = Math.cos(arc_dist)
                * (xyheight + Planet.wgs84_earthEquatorialRadiusMeters);

        double bottom;
        double cutoff = LOScenterHeight
                + Planet.wgs84_earthEquatorialRadiusMeters;

        // Suggested changes, submitted by Mark Wigmore. Introduces
        // use of doubles, and avoidance of PI/2 tan() calculations.

        bottom = cutoff - xPrime;
        ret = MoreMath.HALF_PI_D - Math.atan(bottom / P);
        return ret;

        // Old way...
        //      if (xPrime < cutoff) {
        //          bottom = cutoff - xPrime;
        //          ret = Math.atan(P/bottom);

        //      } else if (xPrime == cutoff) {
        //          ret = MoreMath.HALF_PI_D;

        //      } else if (xPrime > cutoff) {
        //          double C = xPrime - cutoff;
        //          double gamma = Math.atan(P/C);
        //          ret = Math.PI - gamma;
        //      }

        //      return ret;
    }

    /**
     * Called when the circle is started. It starts the circle to be
     * drawn, and sets the parameters that will be needed to figure
     * out the image.
     * 
     * @param event mouse event where the circle should be started.
     */
    public void setCenter(MouseEvent event) {
        graphics.clear();
        LOScenterP.x = event.getX();
        LOScenterP.y = event.getY();
        LOScenterLLP = proj.inverse(LOScenterP.x, LOScenterP.y, new LatLonPoint.Double());
        LOScenterHeight = LOSobjectHeight;
        if (layer.frameCache != null) {
            LOScenterHeight += layer.frameCache.getElevation(LOScenterLLP.getLatitude(),
                    LOScenterLLP.getLongitude());
        }
        LOScirc.setLatLon(LOScenterLLP.getLatitude(),
                LOScenterLLP.getLongitude());
        LOScirc.generate(proj);

        graphics.add(LOScirc);
    }

    /**
     * Used to modify the circle parameters with another mouse event.
     * Takes care of resetting hte circle parameters and regenerating
     * the circle.
     */
    public void addLOSEvent(MouseEvent event) {
        graphics.clear();
        LOSedge = TerrainLayer.numPixelsBetween(LOScenterP.x,
                LOScenterP.y,
                event.getX(),
                event.getY()) * 2 + 1;

        LOScirc.setWidth(LOSedge);
        LOScirc.setHeight(LOSedge);
        LOScirc.generate(proj);
        graphics.add(LOScirc);
    }

    /**
     * Sets the new object height to use at the center of the circle.
     * The old object is subtracted out first to get the center height
     * of the ground before the new value is added.
     * 
     * @param value height of the object in meters.
     */
    public void setLOSobjectHeight(int value) {
        LOScenterHeight -= LOSobjectHeight;
        LOSobjectHeight = value;
        LOScenterHeight += LOSobjectHeight;
    }

    /**
     * Add a ProgressListener that will display build progress.
     */
    public void addProgressListener(ProgressListener list) {
        progressSupport.add(list);
    }

    /**
     * Remove a ProgressListener that displayed build progress.
     */
    public void removeProgressListener(ProgressListener list) {
        progressSupport.remove(list);
    }

    /**
     * Clear all progress listeners.
     */
    public void clearProgressListeners() {
        progressSupport.clear();
    }

    /**
     * Fire an build update to progress listeners.
     * 
     * @param frameNumber the current frame count
     * @param totalFrames the total number of frames.
     */
    protected void fireProgressUpdate(int type, String task, int frameNumber,
                                      int totalFrames) {
        progressSupport.fireUpdate(type, task, totalFrames, frameNumber);
    }

}

