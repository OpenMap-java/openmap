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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDCoverageManager.java,v $
// $RCSfile: DTEDCoverageManager.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.dted;


/*  Java Core  */
import java.awt.*;
import java.util.StringTokenizer;
import java.util.Properties;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.*;

/*  OpenMap  */
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.gui.ProgressListenerGauge;
import com.bbn.openmap.io.*;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;


/** 
 * A DTEDCoverageManager knows how to look at DTED data and figure
 * out what coverage is available.  When it is constructed, it needs
 * to know where the data is located, and where it can look for a
 * coverage summary file.  If a coverage summary file is not
 * available, it will create one after looking at the data.  Using the
 * file is soooo much faster.  The coverage manager also takes a URL
 * of a coverage file, but assumes that the file is there.  If it
 * isn't, then it falls back on it's behavior of looking for a local
 * summary file, and then to the data itself, to come up with
 * coverage.
 */
public class DTEDCoverageManager {

    /** The default line color for level 0.  */
    public final static String defaultLevel0ColorString = "CE4F3F"; // redish
    /** The default line color for level 1.  */
    public final static String defaultLevel1ColorString = "339159"; // greenish
    /** The default line color for level 2.  */
    public final static String defaultLevel2ColorString = "0C75D3"; // bluish

    /** The color to outline the shapes for level 0. */
    protected Paint level0Color = new Color(Integer.parseInt(defaultLevel0ColorString, 16));
    /** The color to outline the shapes for level 1. */
    protected Paint level1Color = new Color(Integer.parseInt(defaultLevel1ColorString, 16));
    /** The color to outline the shapes for level 2. */
    protected Paint level2Color = new Color(Integer.parseInt(defaultLevel2ColorString, 16));

    /** A setting for how transparent to make the images.  The default
     * is 255, which is totally opaque. */
    protected int opaqueness = 255;
    /** Flag to fill the coverage rectangles. */
    protected boolean fillRects = false;

    /** The array of coverage for level 0 data. */
    protected boolean[][] level0Frames = null;
    /** The array of coverage for level 1 data. */
    protected boolean[][] level1Frames = null;
    /** The array of coverage for level 2 data. */
    protected boolean[][] level2Frames = null;

    protected ProgressSupport progressSupport;

    public DTEDCoverageManager(String[] paths, String[] paths2, 
                               String coverageURL, String coverageFile) {
        
        progressSupport = new ProgressSupport(this);

        if (!readCoverageFile(coverageURL, coverageFile)) {
            addProgressListener(new ProgressListenerGauge("Creating DTED Coverage File"));

            fireProgressUpdate(ProgressEvent.START,
                               "Building DTED Coverage file...", 0, 100);

            level0Frames = new boolean[180][360];
            level1Frames = new boolean[180][360];
            level2Frames = new boolean[180][360];

            if (paths != null || paths2 != null) {
                Debug.output("DTEDCoverageManager: Scanning for frames - This could take several minutes!");
                checkOutCoverage(paths, paths2);
            } else {
                Debug.message("dtedcov", "DTEDCoverageManader: No paths for DTED data given.");
            }

            //Write out the new coverage file.
            if (coverageFile != null) {
                fireProgressUpdate(ProgressEvent.UPDATE,
                                   "Writing DTED Coverage file...",
                                   100, 100);

                writeCoverageFile(coverageFile);
            } else {
                Debug.message("dtedcov", "DTEDCoverageManager: No file path specified to write coverage file!");
            }
            fireProgressUpdate(ProgressEvent.DONE,
                               "Wrote DTED Coverage file",
                               100, 100);
        }
    }

    /** 
     * Set the color arraignment for the rectangles.  Opaqueness is
     * not supported in JDK 1.1, so this doesn't really mean
     * anything.  It's a hook for later. 
     *
     * @param lev0Color Paint for level 0 frame rectangles.
     * @param lev1Color Paint for level 1 frame rectangles.
     * @param lev2Color Paint for level 2 frame rectangles.
     * @param opaque how transparent the frames should be if they are filled.
     * @param fillRectangles whether to fill the rectangles with Paint.
     */
    public void setPaint(Paint lev0Color, Paint lev1Color, Paint lev2Color, 
                         int opaque, boolean fillRectangles) {
        level0Color = lev0Color;
        level1Color = lev1Color;
        level2Color = lev2Color;
        opaqueness = opaque;
        fillRects = fillRectangles;
    }

    /**
     * The method that cycles through all the paths, looking for the
     * frames.  This takes time, so it's only done when a coverage
     * file can't be found.
     *
     * @param paths paths to the level 0 and 1 dted root directory.
     * @param paths2 paths to the level 2 dted root directory.
     */
    public void checkOutCoverage(String[] paths, String[] paths2) {
        int latindex, lonindex;
        int maxNumPaths = 0;

        if (paths != null)
            maxNumPaths = paths.length;
        if  ((paths2 != null) && (paths2.length > maxNumPaths))
            maxNumPaths = paths2.length;

        if (maxNumPaths == 0) {
            System.err.println("DTEDCoverageManader: No paths for DTED data given.");
            return;
        }

        if (paths != null) {
            Debug.message("dtedcov","DTEDCoverageManager: checking out DTED level 0, 1 at paths:");
            for (int d1 = 0; d1 < paths.length; d1++) {
                if (Debug.debugging("dtedcov")) {
                    Debug.output("       " + paths[d1]);
                }
                if (!BinaryFile.exists(paths[d1])) {
                    paths[d1] = null;
                    Debug.message("dtedcov","       - path invalid, ignoring.");
                }
            }
        } else {
            Debug.message("dtedcov","DTEDCoverageManager: No DTED level 0, 1 paths specified.");
        }
        
        if (paths2 != null) {
            Debug.message("dtedcov","DTEDCoverageManager: checking out DTED level 2 at paths:");
            for (int d1 = 0; d1 < paths2.length; d1++) {
                if (Debug.debugging("dtedcov")) {
                    Debug.output("       " + paths2[d1]);
                }
                if (!BinaryFile.exists(paths2[d1])) {
                    paths2[d1] = null;
                    Debug.message("dtedcov","       - path invalid, ignoring.");
                }                   
            }
        } else {
            Debug.message("dtedcov","DTEDCoverageManager: No DTED level 2 paths specified.");
        }

        for (int pathNum = 0; pathNum < maxNumPaths; pathNum++) {
            for (int lat = -90; lat < 90; lat++) {
                for (int lon = -180; lon < 180; lon++) {
                    latindex = lat + 90;
                    lonindex = lon + 180;
                    
                    if (paths != null && pathNum < paths.length) {
                        if (paths[pathNum] != null) {
                            if (level0Frames[latindex][lonindex] == false) {
                                level0Frames[latindex][lonindex] = 
                                    BinaryFile.exists(paths[pathNum] + File.separator + 
                                                      DTEDFrameUtil.lonToFileString((float)lon) + 
                                                      File.separator + 
                                                      DTEDFrameUtil.latToFileString((float) lat, 0));
                            }
                            
                            if (level1Frames[latindex][lonindex] == false) {
                                level1Frames[latindex][lonindex] = 
                                    BinaryFile.exists(paths[pathNum] + File.separator + 
                                                      DTEDFrameUtil.lonToFileString((float)lon) + 
                                                      File.separator + 
                                                      DTEDFrameUtil.latToFileString((float) lat, 1));

                            }
                        }
                    }
                    
                    if ((paths2 != null) && (pathNum < paths2.length)) {
                        if (paths2[pathNum] != null) {
                            if (level2Frames[latindex][lonindex] == false) {
                                level2Frames[latindex][lonindex] = 
                                    BinaryFile.exists(paths2[pathNum] + File.separator + 
                                                      DTEDFrameUtil.lonToFileString((float)lon) + 
                                                      File.separator + 
                                                      DTEDFrameUtil.latToFileString((float) lat, 1));
                            }
                        }
                    }
                }

                float pathFactor = ((float)pathNum + 1f)/(float)maxNumPaths;
                float latFactor = 100f*((float)lat + 90f)/180f;
                int whereWeAre = (int)(pathFactor * latFactor);
                if (Debug.debugging("dtedcov")) {
                    Debug.output("Building DTED Coverage, " + 
                                 whereWeAre + "% complete.");
                }

                fireProgressUpdate(ProgressEvent.UPDATE,
                                   "Finding DTED frames...", 
                                   whereWeAre, 100);
            }
        }

    }

    /**  
     * Method organizes the query based on the projection, and
     * returns the applicable rectangles representing the frame
     * coverages.  If the coverage spans over the dateline, then two
     * queries are performed, one for each side of the dateline.
     * 
     * @param proj the projection of the screen
     * @return an array of lists, one for each level of dted data.
     */
    public OMGraphicList[] getCoverageRects(Projection proj) {
        OMGraphicList[] ret1;
        OMGraphicList[] ret2;

        int LineType;

        LatLonPoint ul = proj.getUpperLeft();
        LatLonPoint lr = proj.getLowerRight();
        
        int startx = (int) Math.floor(ul.getLongitude());
        int endx = (int) Math.floor(lr.getLongitude());
        if (endx > 179) endx = 179;
        if (startx > 179) startx = 179;

        int starty = (int) Math.floor(lr.getLatitude());
        int endy = (int) Math.floor(ul.getLatitude());
        if (endy > 89) endy = 89;
        if (starty > 89) starty = 89;

        if (proj instanceof Cylindrical) LineType = OMGraphic.LINETYPE_STRAIGHT;
        else LineType = OMGraphic.LINETYPE_RHUMB;

        if (startx > endx) {
            ret1 = getCoverageRects(startx, starty, 179, endy, LineType);
            ret2 = getCoverageRects(-180, starty, endx, endy, LineType);
            ret1[0].add(ret2[0]);
            ret1[1].add(ret2[1]);
            ret1[2].add(ret2[2]);
            return ret1;
        }
        else return getCoverageRects(startx, starty, endx, endy, LineType);
    }

    /** 
     * Method looks at the coverage arrays, and returns the
     * applicable rectangles representing the frame coverages.
     * 
     * @param startx the western-most longitude.
     * @param starty the southern-most latitude.
     * @param endx the eastern-most longitude.
     * @param endy the northern-most latitude.
     * @param LineType the type of line to use on the rectangles -
     * Cylindrical projections can use straight lines, but other
     * projections should use Rhumb lines.
     * @return an array of lists, one for each level of dted data.
     */
    public OMGraphicList[] getCoverageRects(int startx, int starty, 
                                            int endx, int endy, int LineType) {
        OMGraphicList gl0 = new OMGraphicList();
        OMGraphicList gl1 = new OMGraphicList();
        OMGraphicList gl2 = new OMGraphicList();
        OMGraphicList[] ret = new OMGraphicList[3];

        ret[0] = gl0;
        ret[1] = gl1;
        ret[2] = gl2;

        OMRect rect;

        for (int lat = starty; lat <= endy; lat++) {
            for (int lon = startx; lon <= endx; lon++) {
                if (level0Frames[lat+90][lon+180]) {
                    rect = new OMRect((float) lat, (float) lon, 
                                      (float) lat + 1, (float) lon + 1, 
                                      LineType);
                    rect.setLinePaint(level0Color);
                    if (fillRects) rect.setFillPaint(level0Color);
                    gl0.add(rect);
                }
                
                if (level1Frames[lat+90][lon+180]) {
                    rect = new OMRect((float) lat+.1f, (float) lon+.1f, 
                                      (float) lat + .9f, (float) lon + .9f, 
                                      LineType);
                    rect.setLinePaint(level1Color);
                    if (fillRects) rect.setFillPaint(level1Color);
                    gl1.add(rect);
                }
                
                if (level2Frames[lat+90][lon+180]) {
                    rect = new OMRect((float) lat+.2f, (float) lon+.2f, 
                                      (float) lat + .8f, (float) lon + .8f, 
                                      LineType);
                    rect.setLinePaint(level2Color);
                    if (fillRects) rect.setFillPaint(level2Color);
                    gl2.add(rect);
                }
            }
        }                   
        return ret;
    }

    /** 
     * Read in the coverage file, which is basically three
     * byte[180][360] written out to file.  These are converted to
     * booleans.
     * 
     * @param coverage the path to the file.
     * @return whether the file was read!  True means yes.
     */
    public boolean readCoverageFile(String coverage) {
        try {
            Debug.message("dtedcov","DTEDCoverageManager: Reading coverage file - " + coverage);
            BinaryBufferedFile binFile = new BinaryBufferedFile(coverage);
            level0Frames = new boolean[180][];
            level1Frames = new boolean[180][];
            level2Frames = new boolean[180][];

            byte[] row = new byte[360];

            for (int level = 0; level < 3; level++) {
                for (int lat = 0; lat < 180; lat++) {
                    binFile.read(row);
                    
                    if (level == 0) level0Frames[lat] = convertBytesToBooleans(row);
                    else if (level == 1) level1Frames[lat] = convertBytesToBooleans(row);
                    else level2Frames[lat] = convertBytesToBooleans(row);
                }
            }
            return true;

        } catch (IOException ioe) {
            Debug.message("dtedcov", "DTEDCoverageManager: No coverage file - will create one at " + coverage);
            level0Frames = null;
            level1Frames = null;
            level2Frames = null;
            return false;
        }

    }

    /** 
     * Read in the coverage file, which is basically three
     * byte[180][360] written out to file.  These are converted to
     * booleans.
     * 
     * @param urlCov an url to a coverage file, if available.  Should be null if not used.
     * @param coverage the path to the file.
     * @return whether the file was read!  True means yes.
     */
    protected boolean readCoverageFile(String urlCov, String coverage) {
        URL url = null;
        BufferedInputStream bin = null;
        BinaryBufferedFile binFile = null;

        level0Frames = null;
        level1Frames = null;
        level2Frames = null;

        if (urlCov != null) {
            Debug.message("dtedcov","DTEDCoverageManager: Reading coverage file from URL - " + 
                          urlCov);
            try {
                url = new URL(urlCov);
                bin = new BufferedInputStream(url.openStream());
            } catch (MalformedURLException mue) {
                System.err.println("DTEDCoverageManager: Weird URL given : \"" +
                                   urlCov + "\"");
                bin = null;
            } catch (java.io.IOException e) {
                System.err.println("DTEDCoverageManager: Unable to read coverage file at \"" +
                                   urlCov + "\"");
                bin = null;
            } 
        }

        if (bin == null && coverage != null) {
            try {
                Debug.message("dtedcov","DTEDCoverageManager: Reading coverage file - " + 
                              coverage);
                binFile = new BinaryBufferedFile(coverage);
            } catch (java.io.IOException e) {
                System.err.println("DTEDCoverageManager: Unable to read coverage file at \"" +
                                   coverage + "\"");
            }
        }           
        
        if (bin != null || binFile != null) { 
            try { 
                level0Frames = new boolean[180][];
                level1Frames = new boolean[180][];
                level2Frames = new boolean[180][];
                
                byte[] row = new byte[360];
                
                for (int level = 0; level < 3; level++) {
                    for (int lat = 0; lat < 180; lat++) {
                        
                        if (bin != null) {
                            for (int k = 0; k < row.length; k++) {
                                row[k] = (byte)bin.read();
                            }
                        }
                        else binFile.read(row);
                        
                        if (level == 0) level0Frames[lat] = convertBytesToBooleans(row);
                        else if (level == 1) level1Frames[lat] = convertBytesToBooleans(row);
                        else level2Frames[lat] = convertBytesToBooleans(row);
                    }
                }
                return true;
                
            } catch (IOException ioe) {
                System.err.println("DTECoverageLayer: Error reading coverage.");

                level0Frames = null;
                level1Frames = null;
                level2Frames = null;
            }
        }

        if (level0Frames == null) {
            System.err.println("DTEDCoverageManager: Error reading coverage file - will try to create a valid one at " + coverage);
        }
        return false;
    }

    /**  
     * Convert the bytes read in to the file to the booleans used in
     * the coverage arrays.
     * 
     * @param row array of bytes
     * @return array of booleans
     */
    protected boolean[] convertBytesToBooleans(byte[] row) {
        boolean[] ret = new boolean[row.length];
        for (int i = 0; i < row.length; i++)
            ret[i] = (row[i] == 0) ? false: true;
        return ret;
    } 

    /** 
     * Convert the booleans to write out to bytes.
     * 
     * @param row the input array of booleans
     * @return an array of bytes.
     */
    protected byte[] convertBooleansToBytes(boolean[] row) {
        byte[] ret = new byte[row.length];
        for (int i = 0; i < row.length; i++)
            ret[i] = (row[i]) ? (byte)1:(byte)0;
        return ret;
    } 

    /** 
     * Write the coverage summary to a file.
     * 
     * @param covFilename the file name to write the arrays into.
     */
    public void writeCoverageFile(String covFilename) {
        try{
            Debug.message("dtedcov","DTEDCoverageManager: Writing coverage summary file...");
            
            FileOutputStream binFile = new FileOutputStream(covFilename);

            byte[] row;

            for (int level = 0; level < 3; level++) {
                for (int lat = 0; lat < 180; lat++) {
                    
                    if (level == 0) {
                        row = convertBooleansToBytes(level0Frames[lat]);
                    } else if (level == 1) {
                        row = convertBooleansToBytes(level1Frames[lat]);
                    } else {
                        row = convertBooleansToBytes(level2Frames[lat]);
                    }

                    binFile.write(row);
                }
            }
            binFile.close();

        } catch (IOException ioe) {
            System.err.println("DTEDCoverageManager: Error writing coverage file!");
        }
    }

    /**
     * Add a ProgressListener that will display build progress.
     */
    public void addProgressListener(ProgressListener list) {
        progressSupport.addProgressListener(list);
    }

    /**
     * Remove a ProgressListener that displayed build progress.
     */
    public void removeProgressListener(ProgressListener list) {
        progressSupport.removeProgressListener(list);
    }

    /**
     * Clear all progress listeners.
     */
    public void clearProgressListeners() {
        progressSupport.removeAll();
    }

    /**
     * Fire an build update to progress listeners.
     * @param frameNumber the current frame count
     * @param totalFrames the total number of frames. 
     */
    protected void fireProgressUpdate(int type, String task, 
                                      int frameNumber, 
                                      int totalFrames) {
        progressSupport.fireUpdate(type, task, totalFrames, frameNumber);
    }

}
