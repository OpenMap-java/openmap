//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: RpfUtil.java,v $
//$Revision: 1.4 $
//$Date: 2007/03/12 20:52:46 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer.rpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FileUtils;

/**
 * The RpfUtil is a program that will allow you to manage RPF data
 * geographically. You can copy and delete RPF data frames from RPF
 * data directories, make an A.TOC file for a RPF directory (even
 * after copying and/or deleting), and you can zip an RPF directory
 * into an archive if you like. You can limit the commands to only
 * affect those frames completely inside or outside a specified area,
 * or for those frames that intersect and are contained by that area
 * (default). You can also limit the charts affected based on their
 * map scales, providing a scale and indicating that you only want
 * those frames that match the scale, do not match the scale, are
 * greater or less than the scale. Greater than or less than refers to
 * the scale number of the chart being greater or less than the scale
 * number provided, NOT whether the resulting scale ratio is greater or
 * less than. These things are opposite if each other, and we're just
 * working with the numbers.
 * <p>
 * 
 * When you call RpfUtil.setRpfDir, that calls organizeFrames(rpfDir),
 * which creates a list of the frames affected by the scale and
 * boundary settings. Then, the operation runs on those frames.
 * <p>
 * 
 * It is important that the A.TOC file in the source RPF directory
 * reflects the current state of the data in that directory. If you
 * are not sure, run this program with the -maketoc option on that
 * directory to update it.
 * 
 * @author dietrick
 */
public class RpfUtil {

    public final static char SCALE_EQUALS = 'e';
    public final static char SCALE_NOTEQUALS = 'n';
    public final static char SCALE_GREATERTHAN = 'g';
    public final static char SCALE_LESSTHAN = 'l';
    public final static float ALL_SCALES = 0f;

    public final static int BOUNDED = 0;
    public final static int INSIDE = 1;
    public final static int OUTSIDE = 2;

    protected int boundaryLimits = BOUNDED;
    protected float scale = ALL_SCALES;
    protected char scaleDelim = SCALE_EQUALS;
    protected float upperLat = 90f;
    protected float leftLon = -180;
    protected float lowerLat = -90f;
    protected float rightLon = 180f;
    protected String rpfDir;

    protected boolean verbose = false;

    protected List<String> frameList;

    /**
     * Create a default RpfUtil considering all data.
     */
    public RpfUtil() {}

    /**
     * Create a RpfUtil considering data intersecting the provided
     * boundary. The RPF data directory still needs to be set.
     * 
     * @param ulat upper latitude
     * @param llon left longitude
     * @param llat lower latitude
     * @param rlon right longitude
     */
    public RpfUtil(float ulat, float llon, float llat, float rlon) {
        this(null, ulat, llon, llat, rlon, 0f, SCALE_EQUALS, BOUNDED);
    }

    /**
     * Create a RpfUtil considering data intersecting the provided
     * boundary, involving the provided RPF directory.
     * 
     * @param rpfDir the RPF directory to search and consider frames
     *        over.
     * @param ulat upper latitude
     * @param llon left longitude
     * @param llat lower latitude
     * @param rlon right longitude
     */
    public RpfUtil(String rpfDir, float ulat, float llon, float llat, float rlon) {
        this(rpfDir, ulat, llon, llat, rlon, 0f, SCALE_EQUALS, BOUNDED);
    }

    /**
     * Full control over the RpfUtil settings.
     * 
     * @param rpfDir the RPF directory to search and consider frames
     *        over.
     * @param ulat upper latitude
     * @param llon left longitude
     * @param llat lower latitude
     * @param rlon right longitude
     * @param scale the scale of the charts to consider.
     * @param scaleDelimiter character 'g'reater than, 'l'ess than,
     *        'n'ot equal to, 'e'qual to. e is the default.
     * @param boundaryLimits INSIDE, OUTSIDE or BOUNDARY
     */
    public RpfUtil(String rpfDir, float ulat, float llon, float llat,
            float rlon, float scale, char scaleDelimiter, int boundaryLimits) {
        this.upperLat = ulat;
        this.lowerLat = llat;
        this.leftLon = llon;
        this.rightLon = rlon;
        this.scale = scale;
        this.scaleDelim = scaleDelimiter;
        this.boundaryLimits = boundaryLimits;

        setRpfDir(rpfDir);
    }

    /**
     * Creates the list of frames to consider, based on settings. This
     * method does a cursory check of scale settings before moving to
     * geographical settings.
     * 
     * @param rpfDir
     * @return List of relative path names to frames.
     */
    protected List<String> organizeFrames(String rpfDir) {
        RpfTocHandler toc = new RpfTocHandler(rpfDir);
        List<String> frameList = new LinkedList<String>();

        if (toc.isValid()) {
            RpfTocEntry[] entries = toc.getEntries();
            if (verbose) {
                Debug.output("Figuring out which frames fit the criteria...");
            }
            for (int i = 0; i < entries.length; i++) {
                RpfTocEntry entry = entries[i];
                toc.loadFrameInformation(entry);

                double udinterval = (entry.coverage.nw_lat - entry.coverage.se_lat)
                        / entry.vertFrames;
                double rlinterval = (entry.coverage.se_lon - entry.coverage.nw_lon)
                        / entry.horizFrames;

                if (scale > 0) {
                    float rectScale = (float) RpfTocHandler.textScaleToLong(entry.scale);

                    if (rectScale == RpfConstants.UK.scale) {
                        if (verbose) {
                            Debug.output("  RpfTocEntry[" + i
                                    + "] scale unknown ("
                                    + entry.coverage.chartCode + "), skipping");
                        }
                        continue;
                    }

                    switch (scaleDelim) {
                    case SCALE_EQUALS:
                        if (scale == rectScale)
                            frameList.addAll(getFrameList(entry,
                                    rlinterval,
                                    udinterval));
                        break;
                    case SCALE_GREATERTHAN:
                        if (scale >= rectScale)
                            frameList.addAll(getFrameList(entry,
                                    rlinterval,
                                    udinterval));
                        break;
                    case SCALE_LESSTHAN:
                        if (scale <= rectScale)
                            frameList.addAll(getFrameList(entry,
                                    rlinterval,
                                    udinterval));
                        break;
                    case SCALE_NOTEQUALS:
                        if (scale != rectScale)
                            frameList.addAll(getFrameList(entry,
                                    rlinterval,
                                    udinterval));
                    default:
                        break;
                    } // switch

                } else {
                    frameList.addAll(getFrameList(entry, rlinterval, udinterval));
                }

            }
        }

        return frameList;

    }

    /**
     * Middle management for frames for A.TOC entry box.
     * 
     * @param entry RpfTocEntry to consider.
     * @param rlinterval right to left decimal degree interval for
     *        entry.
     * @param udinterval up to down decimal degree interval for entry
     * @return List of frame strings that pass current settings.
     */
    protected List<String> getFrameList(RpfTocEntry entry, double rlinterval,
                                double udinterval) {
        List<String> frameList = new LinkedList<String>();
        
        for (int hor = 0; hor < entry.horizFrames; hor++) {
            for (int ver = 0; ver < entry.vertFrames; ver++) {

                RpfFrameEntry frame = entry.getFrame(ver, hor);

                double left = entry.coverage.nw_lon + (rlinterval * hor);
                double right = left + rlinterval;
                double up = entry.coverage.nw_lat - (udinterval * ver);
                double down = up - udinterval;

                if (frame.exists
                        && frameFitsCriteria(left,
                                right,
                                up,
                                down,
                                rlinterval,
                                udinterval)) {
                    String name = frame.framePath.substring(frame.rpfdirIndex + 3);

                    frameList.add(name);

                    if (verbose) {
                        Debug.output(" getFrameList: adding file " + name);
                    }
                }

            }
        }

        return frameList;
    }

    /**
     * Geographical evaluation of frame file
     * 
     * @return true if file should be added to the list.
     */
    protected boolean frameFitsCriteria(double left, double right, double up,
                                        double down, double rlinterval,
                                        double udinterval) {

        switch (boundaryLimits) {
        case OUTSIDE:
            return (left < leftLon && right < leftLon)
                    || (left > rightLon && right > rightLon)
                    || (up < lowerLat && down < lowerLat)
                    || (up > upperLat && down > upperLat);
        case INSIDE:
            return (left > leftLon && right > leftLon && left < rightLon
                    && right < rightLon && up > lowerLat && down > lowerLat
                    && up < upperLat && down < upperLat);
        default:
            return (((right <= rightLon && left >= leftLon - rlinterval)
                    || (left >= leftLon && right <= rightLon + rlinterval) || (left <= leftLon && right >= rightLon)) && ((up <= upperLat
                    + udinterval && down >= lowerLat)
                    || (down >= lowerLat - udinterval && up <= upperLat) || (up >= upperLat && down <= lowerLat)));
        }
    }

    /**
     * Copy the frame files currently set on the FrameList to the
     * provided RPF directory.
     * 
     * @param toRpfDir
     * @return true if it works.
     */
    public boolean copy(String toRpfDir) {
        File toDir = new File(toRpfDir);
        boolean ret = false;
        String sourceRpfDir = getRpfDir();
        if ((toDir.exists() || toDir.mkdirs()) && frameList != null) {
            if (verbose) {
                Debug.output("From " + sourceRpfDir + " to " + toRpfDir + ":");
            }
            for (Iterator<String> it = frameList.iterator(); it.hasNext();) {
                String relativeFilePath = "/" + it.next();
                File fromFile = new File(sourceRpfDir + relativeFilePath);
                File toFile = new File(toRpfDir + relativeFilePath);
                File toParent = toFile.getParentFile();
                if (!toParent.exists()) {
                    toParent.mkdirs();
                }
                if (verbose) {
                    Debug.output("Copying " + relativeFilePath);
                }

                try {
                    FileUtils.copy(fromFile, toFile, 400000);
                } catch (IOException ioe) {
                    Debug.error("RpfUtil.copy:  IOExeption copying files: "
                            + ioe.getMessage());
                    return false;
                }
            }
            ret = true;
        }

        return ret;
    }

    /**
     * Create an A.TOC file for the provided RPF directory.
     * 
     * @param rpfDir
     * @return true if it works.
     */
    public boolean maketoc(String rpfDir) {
        boolean ret = false;

        MakeToc mt = new MakeToc();
        String[] paths = new String[] { rpfDir };

        // paths is going to be reset to list all of the RPF frame
        // file paths.
        paths = mt.searchForRpfFiles(paths);

        try {
            mt.create(paths,
                    rpfDir + "/" + RpfTocHandler.RPF_TOC_FILE_NAME,
                    false);
            ret = true;
        } catch (MakeTocException mte) {
            Debug.error("Problem creating A.TOC file: \n" + mte.getMessage());
        }

        return ret;
    }

    /**
     * Delete the files in the provided RPF directory that match the
     * current scale and boundary settings.
     * 
     * @param rpfDir
     * @return true if it works.
     */
    public boolean delete(String rpfDir) {
        boolean ret = false;

        List<String> frameList = organizeFrames(rpfDir);
        if (frameList != null) {
            for (Iterator<String> it = frameList.iterator(); it.hasNext();) {
                String relativeFilePath = "/" + it.next();
                File fromFile = new File(rpfDir + relativeFilePath);
                if (fromFile.exists() && fromFile.delete() && verbose) {
                    Debug.output("Deleting " + fromFile.getPath());
                }
            }
            ret = true;
        }

        return ret;
    }

    /**
     * Store the contents of the toBeZippedName directory into a zip
     * file with the provided name. If the zip file name doesn't end
     * in .zip, it will.
     * 
     * @param zipLocation
     * @param toBeZippedName
     * @return true if it works.
     */
    public boolean zip(String zipLocation, String toBeZippedName) {
        boolean ret = false;

        if (verbose)
            Debug.put("zip");

        File toBeZipped = new File(toBeZippedName);
        if (toBeZipped.exists()) {
            try {
                FileUtils.saveZipFile(zipLocation, toBeZipped);
                ret = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Just run a query on the provided directory, listing the names
     * of the frames that will be affected by the current scale and
     * boundary settings.
     * 
     * @param rpfDir
     */
    public void query(String rpfDir) {
        List<String> frameList = organizeFrames(rpfDir);
        if (frameList != null) {
            for (Iterator<String> it = frameList.iterator(); it.hasNext();) {
                Debug.output(it.next().toString());
            }
        }
    }

    /**
     * The RpfUtil class can be run as a java program. This program
     * will allow commands to be stacked, but will execute them in
     * this order - copy, delete, maketoc, zip. Only one version of
     * each argument is allowed per execution.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Debug.init();
        ArgParser ap = new ArgParser("RpfUtil");
        ap.add("copy",
                "Copy RPF data from one RPF directory to another. (-copy from to)",
                2);
        ap.add("delete",
                "Delete RPF data from a RPF directory. (-delete from)",
                1);
        ap.add("maketoc",
                "Create an A.TOC file in a RPF directory. (-maketoc from).",
                1);
        ap.add("zip",
                "Create a zip file from a RPF directory. (-zip zipFileName from)",
                2);
        ap.add("query",
                "Print the paths of files that fit the criteria, but do nothing",
                1);
        ap.add("scale",
                "The scale to use for criteria in matching chart types, followed by a letter describing the relationship of matching frame scales to give scale ('g'reater than, 'l'ess than, 'n'ot equal to, 'e'qual to). (optional)",
                2);
        ap.add("boundary",
                "Coordinates of bounding box (upper lat, left lon, lower lat, right lon) (optional)",
                4,
                true);
        ap.add("inside",
                "Flag to manage RPF frames inside bounding box. (default, optional)");
        ap.add("outside",
                "Flag to manage RPF frames outside bounding box. (optional)");
        ap.add("verbose", "Print out progress");
        ap.add("extraverbose", "Print out ALL progress");

        if (!ap.parse(args)) {
            ap.printUsage();
            System.exit(0);
        }

        float ulat = 90f;
        float llat = -90f;
        float llon = -180f;
        float rlon = 180f;

        String arg[];
        arg = ap.getArgValues("boundary");
        if (arg != null) {
            boolean boundaryCoordinateProblem = true;
            try {
                ulat = Float.parseFloat(arg[0]);
                llon = Float.parseFloat(arg[1]);
                llat = Float.parseFloat(arg[2]);
                rlon = Float.parseFloat(arg[3]);

                boundaryCoordinateProblem = ulat > 90 || llon < -180
                        || llat < -90 || rlon > 180 || ulat <= llat
                        || llon >= rlon;

            } catch (NumberFormatException nfe) {
                Debug.error("Parsing error for boundary coordinates");
            }

            if (boundaryCoordinateProblem) {
                Debug.error("Boundary coordinates are screwy...");
                ap.printUsage();
                System.exit(0);
            }
        }

        RpfUtil rpfUtil = new RpfUtil(ulat, llon, llat, rlon);

        rpfUtil.verbose = (ap.getArgValues("verbose") != null);

        arg = ap.getArgValues("outside");
        if (arg != null) {
            rpfUtil.setBoundaryLimits(RpfUtil.OUTSIDE);
        }

        arg = ap.getArgValues("inside");
        if (arg != null) {
            rpfUtil.setBoundaryLimits(RpfUtil.INSIDE);
        }

        arg = ap.getArgValues("scale");
        if (arg != null) {
            try {
                rpfUtil.setScale(Float.parseFloat(arg[0]));
                rpfUtil.setScaleDelim(arg[1].charAt(0));
            } catch (NumberFormatException nfe) {
                Debug.error("Scale value is screwy...");
                ap.printUsage();
                System.exit(0);
            }
        }

        arg = ap.getArgValues("query");
        if (arg != null) {
            rpfUtil.query(arg[0]);
            System.exit(0);
        }

        arg = ap.getArgValues("copy");
        if (arg != null) {
            rpfUtil.setRpfDir(arg[0]);
            if (!rpfUtil.copy(arg[1])) {
                Debug.output("Problem copying frames");
            }
        }

        arg = ap.getArgValues("delete");
        if (arg != null && !rpfUtil.delete(arg[0])) {
            Debug.output("Problem deleting files.");
        }

        arg = ap.getArgValues("maketoc");
        if (arg != null && !rpfUtil.maketoc(arg[0])) {
            Debug.output("Problem creating A.TOC file for frames.");
        }

        arg = ap.getArgValues("zip");
        if (arg != null && !rpfUtil.zip(arg[0], arg[1])) {
            Debug.output("Problem creating zip file: " + arg[0]);
        }
    }

    public int getBoundaryLimits() {
        return boundaryLimits;
    }

    /**
     * Set whether the frames need to be INSIDE, OUTSIDE or inside and
     * touching the BOUNDARY of the geographical area set in the
     * RpfUtil object.
     * 
     * @param boundaryLimits
     */
    public void setBoundaryLimits(int boundaryLimits) {
        this.boundaryLimits = boundaryLimits;
    }

    public List<String> getFrameList() {
        return frameList;
    }

    public void setFrameList(List<String> frameList) {
        this.frameList = frameList;
    }

    public float getLeftLon() {
        return leftLon;
    }

    public void setLeftLon(float leftLon) {
        this.leftLon = leftLon;
    }

    public float getLowerLat() {
        return lowerLat;
    }

    public void setLowerLat(float lowerLat) {
        this.lowerLat = lowerLat;
    }

    public float getRightLon() {
        return rightLon;
    }

    public void setRightLon(float rightLon) {
        this.rightLon = rightLon;
    }

    public String getRpfDir() {
        return rpfDir;
    }

    /**
     * Creates the list of frames matching the geographical and scale
     * parameters of the frames within the directory.
     * 
     * @param rpfDir
     */
    public void setRpfDir(String rpfDir) {
        this.rpfDir = rpfDir;
        if (rpfDir != null) {
            frameList = organizeFrames(rpfDir);
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public char getScaleDelim() {
        return scaleDelim;
    }

    /**
     * Set whether the frames considered should have scale numbers
     * greater than (g), less than (l), not equal to (n) or equal to
     * (e). Equal to is the default, and that's set if any other value
     * os provided.
     * 
     * @param scaleDelim
     */
    public void setScaleDelim(char scaleDelim) {
        if (scaleDelim == SCALE_NOTEQUALS || scaleDelim == SCALE_GREATERTHAN
                || scaleDelim == SCALE_LESSTHAN) {
            this.scaleDelim = scaleDelim;
        } else {
            this.scaleDelim = SCALE_EQUALS;
        }
    }

    public float getUpperLat() {
        return upperLat;
    }

    public void setUpperLat(float upperLat) {
        this.upperLat = upperLat;
    }

}
