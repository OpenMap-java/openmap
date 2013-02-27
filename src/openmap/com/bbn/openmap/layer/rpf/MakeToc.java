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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/MakeToc.java,v $
// $RCSfile: MakeToc.java,v $
// $Revision: 1.14 $
// $Date: 2006/08/17 15:19:06 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * The meat of this code is based on source code provided by The MITRE
 * Corporation, through the browse application source code.  Many
 * thanks to Nancy Markuson who provided BBN with the software, and to
 * Theron Tock, who wrote the software, and Daniel Scholten, who
 * revised it - (c) 1994 The MITRE Corporation for those parts, and
 * used/distributed with permission. 
 */
package com.bbn.openmap.layer.rpf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Vector;

import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressListener;
import com.bbn.openmap.event.ProgressSupport;
import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.proj.coords.DMSLatLonPoint;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * This is a class that will generate A.TOC files that the RpfLayer requires.
 * A.TOC files provide the RpfLayer with an idea of what data is available to
 * it, its geographic coverage, and chart type. With the A.TOC contents, the
 * RpfLayer is able to find which frames are appropriate for a given projection
 * location. It is very important to have a valid A.TOC directory.
 * <P>
 * 
 * The RPF specification, MIL-STD-2411, has definitions for how frames are to be
 * laid out and found within a RPF directory. All RPF data is supposed to lie
 * under one RPF directory, and an A.TOC file, describing all the files and
 * their groupings, should be directly within the RPF directory. That's why the
 * RpfLayer needs a path to a RPF directory - it's really looking for the A.TOC
 * file, and knows where to find it. It also needs a path to the RPF directory
 * because it needs to prepend that path to the paths to the files that the
 * A.TOC file knows about.
 * <P>
 * 
 * The A.TOC files that can be created with this MakeToc class can be created to
 * contain absolute frame paths. The MakeToc class can take the paths to several
 * RPF directories, and create a single A.TOC file that preserves all of their
 * current file paths. You have to use a lot of caution with this capability,
 * however. These A.TOCs containing absolute file paths will not work if the
 * data is moved to another machine, or if referenced by a machine with a
 * different type file system (i.e. Windows). They may not work for other
 * implementations of code that display RPF data - the code in this package has
 * been modified to test for absolute file names.
 * <P>
 * 
 * That said, absolute file names should be used instead of giving the RpfLayer
 * several RPF directories. The RpfTocHandler does much less work when it is
 * allowed to group coverages together to make bigger areas.
 * <P>
 * 
 * This code was ported from C code provided in the original Mitre RPF package
 * that had limits to the number of frames that could make up the areas. I'll be
 * working to eliminate those limits, but I wanted to get a working version of
 * the code out there. I'm also planning on modifying this class so that it can
 * load the RpfTocHandler directly, therefore eliminating the need for A.TOCs
 * altogether when there is more than one RPF directory.
 * <P>
 * 
 * <pre>
 * 
 * 
 *   Usage:  java com.bbn.openmap.layer.rpf.MakeToc (RPF dir path) (RPF dir path) ...
 * 
 * 
 * </pre>
 * 
 * This will create an A.TOC file in the current directory for the RPF files in
 * the RPF directory paths. Use:
 * 
 * <pre>
 * 
 * 
 *   java com.bbn.openmap.layer.rpf.MakeToc -help
 * 
 * 
 * </pre>
 * 
 * for other options.
 * 
 * <P>
 * NOTE: Make sure that the RPF directories and their contents are in upper
 * case. It's a spec requirement, although with CD copies and FTP downloads, the
 * file name cases sometimes get switched. Use
 * com.bbn.openmap.layer.rpf.ChangeCase to modify the file name cases. Also, if
 * there is more than one RPF directory in the path to the image frames, use the
 * absolute path option. Otherwise, the code will focus on making the top-most
 * RPF directory the one to key the internal relative paths off of, and that
 * might not be what you want.
 * </P>
 * 
 * @see com.bbn.openmap.layer.rpf.ChangeCase
 */
public class MakeToc {

    /**
     * According to Dan Scholten's original code, this was 2 times the max -
     * changed from 30 on 6/17/94 to 200 for 81 JNC's in zone 1. This might not
     * be enough for world-wide coverage of larger scale maps that are now
     * available. This number may have to be increased depending on how much
     * data you need.
     */
    public final static int DEFAULT_MAX_SIDE = 200;

    public final static double EPS = 0.01;
    public final static double EPS2 = 0.0001;
    /** Output file name of the A.TOC file. */
    public final static String ATOC_FILENAME = "A.TOC";
    /** The boundary edge frame length for groups. */
    protected int maxSide = DEFAULT_MAX_SIDE;
    /** Flag to use relative frames paths - default is true. */
    protected boolean relativeFramePaths = true;
    /** The producer name for the frame files. Default is DMAAC. */
    protected String producer = "DMAAC";

    protected ProgressSupport progressSupport;

    /** An internal representation of a Frame file. */
    public class Frame {
        double left;
        double right;
        double top;
        double bottom;
        /* New DKS: for computing GEOREF #'s over polar region */
        double swlat;
        double swlon;
        double h_interval;
        double v_interval;
        double h_resolution;
        double v_resolution;
        String scale; // length 12
        char zone;
        boolean marked;
        int group;
        int x;
        int y;
        String filename;
        boolean cib;
        boolean cdted;

        public double EPS() {
            return (Math.abs(right - left) * MakeToc.EPS);
        }

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("Frame - ").append(filename).append("\n");
            s.append("  zone = ").append(zone).append("\n");
            s.append("  marked = ").append(marked).append("\n");
            s.append("  scale = ").append(scale).append("\n");
            s.append("  group = ").append(group).append("\n");

            if (Debug.debugging("maketocframe")) {
                s.append("  top = ").append(top).append("\n");
                s.append("  bottom = ").append(bottom).append("\n");
                s.append("  left = ").append(left).append("\n");
                s.append("  right = ").append(right).append("\n");
                s.append("  h_interval = ").append(h_interval).append("\n");
                s.append("  v_interval = ").append(v_interval).append("\n");
                s.append("  h_resolution = ").append(h_resolution).append("\n");
                s.append("  v_resolution = ").append(v_resolution).append("\n");
            }

            return s.toString();
        }
    }

    /** An internal representation of a boundary rectangle for frames. */
    public class Group {
        double[] horiz_pos;
        double[] vert_pos;
        int left;
        int right;
        int top;
        int bottom;
        String scale;
        char zone;
        double h_interval;
        double v_interval;
        double h_resolution;
        double v_resolution;
        boolean cib;
        boolean cdted;

        public Group() {
            horiz_pos = new double[maxSide];
            vert_pos = new double[maxSide];
        }

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("Group - \n");
            s.append("  zone = ").append(zone).append("\n");
            s.append("  scale = ").append(scale).append("\n");
            s.append("  left = ").append(left).append("\n");
            s.append("  right = ").append(right).append("\n");
            s.append("  top = ").append(top).append("\n");
            s.append("  bottom = ").append(bottom).append("\n");
            s.append("  is cdted = ").append(cdted).append("\n");
            s.append("  is cib = ").append(cib).append("\n");
            return s.toString();
        }
    }

    public MakeToc() {
        progressSupport = new ProgressSupport(this);
    }

    /**
     * Create an A.TOC file.
     * 
     * @param argv The arguments should at least include a path to a RPF file
     *        root directory. Other options can be found by using a -help
     *        option.
     */
    public static void main(String[] argv) {
        Debug.init();
        boolean Dchum = false;

        ArgParser ap = new ArgParser("MakeToc");
        ap.add("absolute", "Use absolute paths in A.TOC - Use for multiple RPF Directories");
        ap.add("boundary", "Maximum frames on a boundary edge (Default 200)", 1);
        ap.add("dchum", "DCHUM files are included.");
        ap.add("log", "Pathname of log file to list A.TOC creation output.", 1);
        ap.add("output", "Path to directory to place A.TOC file. (Default is current directory)", 1);
        ap.add("producer", "The producer of the frames (Default DMAAC).  Five letter code.", 1);
        ap.add("verbose", "Print out progress");
        ap.add("extraverbose", "Print out ALL progress");
        ap.add("nw", "Don't put up swing progress window (Use this if you are getting weird exceptions)");
        ap.add("paths", "Space separated paths to RPF directory or directories.  Should be last.  If more than one directory is listed, then absolute paths are used in the A.TOC file.", ArgParser.TO_END);

        if (!ap.parse(argv)) {
            ap.printUsage();
            System.exit(0);
        }

        String outputFile = "." + File.separator + RpfTocHandler.RPF_TOC_FILE_NAME;

        String arg[];
        arg = ap.getArgValues("output");
        if (arg != null) {
            outputFile = arg[0] + File.separator + RpfTocHandler.RPF_TOC_FILE_NAME;
        }

        arg = ap.getArgValues("log");
        if (arg != null) {
            String logfile = arg[0];
            Debug.directOutput(logfile, false, true);
            Debug.output("MakeToc: Creating log at " + logfile + " at "
                    + java.util.Calendar.getInstance().getTime());
        }

        arg = ap.getArgValues("dchum");
        if (arg != null) {
            Dchum = true;
        }

        arg = ap.getArgValues("verbose");
        if (arg != null) {
            Debug.put("maketoc");
        }

        arg = ap.getArgValues("extraverbose");
        if (arg != null) {
            Debug.put("maketoc");
            Debug.put("maketocdetail");
        }

        String[] paths = null;
        arg = ap.getArgValues("paths");
        if (arg != null) {
            paths = arg;
        } else {
            paths = ap.getRest();
        }

        if (paths == null || paths.length == 0) {
            Debug.output("MakeToc: need a path to start searching for RPF frames.");
            System.exit(0);
        }

        MakeToc mt = new MakeToc();

        // If the -nw argument was not used, add a progress gauge.
        arg = ap.getArgValues("nw");
        if (arg == null) {
            try {
                mt.addProgressListener(new com.bbn.openmap.gui.ProgressListenerGauge("RPF A.TOC File Creation"));
            } catch (RuntimeException re) {

            }
        }

        boolean argFlagged = false;
        arg = ap.getArgValues("absolute");
        if (arg != null) {
            argFlagged = true;
        }

        arg = ap.getArgValues("producer");
        if (arg != null) {
            mt.setProducer(arg[0]);
        }

        if ((paths != null && paths.length > 1) || argFlagged) {
            Debug.output("MakeToc:  creating A.TOC with absolute path names.");
            mt.setRelativeFramePaths(false);
        }

        arg = ap.getArgValues("boundary");
        int max_side = DEFAULT_MAX_SIDE;
        if (arg != null) {
            try {
                max_side = Integer.parseInt(arg[0]);
                if (max_side <= DEFAULT_MAX_SIDE) {
                    Debug.output("MakeToc: Boundary number specified (" + max_side
                            + ") is too small.  Using default of 200.");
                    max_side = DEFAULT_MAX_SIDE;
                }
            } catch (NumberFormatException nfe) {
                Debug.output("MakeToc: Tried to pass a bogus integer (" + arg[0]
                        + ") as a boundary limit.  Using default of 200.");
                max_side = DEFAULT_MAX_SIDE;
            }
        }
        mt.setMaxSide(max_side);
        mt.fireProgressUpdate(ProgressEvent.START, "Searching for RPF frames", 0, 100);

        paths = mt.searchForRpfFiles(paths);

        try {

            mt.create(paths, outputFile, Dchum);

        } catch (MakeTocException mte) {
            Debug.error("Problem creating A.TOC file: \n" + mte.getMessage());
        }

        System.exit(0);

    }

    /**
     * Create a A.TOC file specificed by the frame file list, at the location
     * specified.
     * 
     * @param rpfFilePaths An array of all RPF Frame file paths. If these paths
     *        are relative, the MakeToc class should be set for that.
     * @param outputFile the complete pathname to an A.TOC file to be written.
     * @exception MakeTocException if anything goes wrong.
     */
    public void create(String[] rpfFilePaths, String outputFile) throws MakeTocException {
        create(rpfFilePaths, outputFile, false);
    }

    /**
     * Create a A.TOC file specificed by the frame file list, at the location
     * specified.
     * 
     * @param rpfFilePaths An array of all RPF Frame file paths. If these paths
     *        are relative, the MakeToc class should be set for that.
     * @param outputFile the complete pathname to an A.TOC file to be written.
     * @param dchum If dchum is present, all frames get placed in their own
     *        group. False is default. Dchum are replacement subframes.
     * @exception MakeTocException if anything goes wrong.
     */
    public void create(String[] rpfFilePaths, String outputFile, boolean dchum)
            throws MakeTocException {

        RpfHeader head = new RpfHeader();
        Vector<Frame> frames = new Vector<Frame>(rpfFilePaths.length);
        Vector<Group> groups = new Vector<Group>();

        fireProgressUpdate(ProgressEvent.UPDATE, "Organizing frames", 0, 100);

        organizeFrames(rpfFilePaths, head, frames);

        if (head.standardNumber == null) {
            throw new MakeTocException("MakeToc: No RPF frames found.");
        }

        groupFrames(frames, groups, dchum);
        fireProgressUpdate(ProgressEvent.UPDATE, "Writing A.TOC file", 100, 100);
        writeTOCFile(outputFile, head, frames, groups);
        fireProgressUpdate(ProgressEvent.DONE, "A.TOC file complete", 100, 100);
    }

    /**
     * Look for RPF frame files, given a bunch of places to start looking. The
     * output of this can be passed to the create method.
     * 
     * @param startDirs Directory paths.
     * @return an array of strings representing path names to RPF frame files.
     */
    public String[] searchForRpfFiles(String[] startDirs) {
        RpfFileSearch search = new RpfFileSearch();
        for (int i = 0; i < startDirs.length; i++) {
            search.handleEntry(startDirs[i]);
        }
        return search.getFiles();
    }

    /**
     * Set whether to use relative frame paths in the A.TOC file.
     */
    public void setRelativeFramePaths(boolean setting) {
        relativeFramePaths = setting;
    }

    public boolean getRelativeFramePaths() {
        return relativeFramePaths;
    }

    /**
     * Set the 5 letter producer code for the frames. If you didn't make the
     * frames, they DMA probably did, so the default is applicable - DMAAC.
     * There are a bunch of accepted codes in the MIL-STD-2411 for producers.
     */
    public void setProducer(String setting) {
        if (setting.length() != 5) {
            if (setting.length() >= 5) {
                producer = setting.substring(0, 5);
            } else {
                producer = setting + createPadding(5 - setting.length(), false);
            }
        } else {
            producer = setting;
        }
    }

    /** Get the producer code currently set. */
    public String getProducer() {
        return producer;
    }

    /**
     * Set the Maximum number of frames along a group boundary edge. Don't
     * change this after starting to group the frames.
     */
    protected void setMaxSide(int set) {
        maxSide = set;
    }

    /**
     * Get the Maximum number of frames along a group boundary edge.
     */
    protected int getMaxSide() {
        return maxSide;
    }

    /** A little function to tell of one edge is near another. */
    protected boolean near(double a, double b, double eps) {
        return (Math.abs(a - b) < eps); /* EPS was 0.0001 */
    }

    /**
     * Get all the frame paths, and sort through them. This method sets up the
     * frames vector and loads each Frame with it's attributes, so it can be
     * grouped with its neighbors.
     * 
     * @param framePaths the array of RPF file paths.
     * @param head an RpfHeader object to load with production information, that
     *        will be put into the A.TOC file.
     * @param frames the frame vector to load.
     */
    public void organizeFrames(String[] framePaths, RpfHeader head, Vector<Frame> frames) {

        int tail;
        int i;

        /* New, DKS */
        // boolean Cib = false; /* CIB data flag: 1:I1(10M); 2:I2(5M) */
        // boolean Cdted = false; /* CDTED data flag: 1: DT1(100M) */
        boolean isoverview = false;
        boolean islegend = false;

        Frame frame;

        RpfFileSections.RpfCoverageSection coverage;

        Debug.message("maketoc", "MakeToc.organizeFrames: *** initial look at frames ***");

        /* # of frames = # of pathname records = #files */
        int nFrames = framePaths.length;

        if (Debug.debugging("maketoc")) {
            Debug.output("Number of frames: " + nFrames);
        }

        /* for each frame file */
        for (i = 0; i < nFrames; i++) {
            isoverview = false;
            islegend = false;

            String framePath = framePaths[i];

            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: frame number " + i + ", " + framePath);
            }

            try {
                BinaryFile binFile = new BinaryBufferedFile(framePath);

                // Frame file names are 8.3 notation, might want to
                // check
                // that here, to blow off dummy files.
                String fn = binFile.getName();
                if (fn.length() != 12) {
                    // Not a RPF Frame file
                    if (Debug.debugging("maketoc")) {
                        Debug.error("MakeToc: " + framePath + " is not a RPF image file - ignoring");
                    }
                    continue;
                }

                RpfFileSections rfs = new RpfFileSections();

                binFile.seek(0);

                if (!head.read(binFile)) {
                    // Not a RPF Frame file
                    if (Debug.debugging("maketoc")) {
                        Debug.error("MakeToc: " + framePath + " is not a RPF image file - ignoring");
                    }
                    continue;
                }

                binFile.seek(head.locationSectionLocation);

                rfs.parse(binFile);
                coverage = rfs.parseCoverageSection(binFile);

                if (coverage == null) {
                    Debug.error("MakeToc: error reading coverage section for " + framePath
                            + ", (file " + i + ") skipping");

                    binFile.close();
                    continue;
                }

                if (Debug.debugging("maketocframedetail")) {
                    Debug.output("MakeToc.organizeFrames: coverage section for " + framePath + ", "
                            + coverage);
                }

                binFile.close();
                binFile = null;

            } catch (FileNotFoundException e) {
                Debug.error("MakeToc: " + framePath + " not found, being ignored.");
                continue;
            } catch (IOException ioe) {
                Debug.error("MakeToc: File IO Error during read of: " + framePath
                        + "! Being ignored. \n" + ioe);
                continue;
            }

            frame = new Frame();
            frames.add(frame);

            frame.filename = framePath;

            // This will be the actual file name, without parental
            // path.
            String framename;

            tail = frame.filename.lastIndexOf(File.separatorChar);
            if (tail == -1) {
                framename = frame.filename;
            } else {
                framename = frame.filename.substring(++tail);
            }

            if (framename.length() != 12) {
                Debug.error("filename must be 12 chars long - " + framename);
                return;
            }

            // 9 is the character after the period.
            isoverview = (framename.charAt(9) == 'O');
            if (!isoverview) {
                islegend = framename.regionMatches(true, 9, "LG", 0, 2);
            }

            // Check and see of the file thinks it's name is the same
            // as it actually is. If they differ, rule in favor of
            // what the frame thinks it is.

            // Let's just be passive here, and name it to whatever it
            // is. If we found the frame, then we'll find it later,
            // too. -DFD

            // if (!framename.equals(head.filename)) { /* DKS */
            // File file = new File(frame.filename);
            // File newFile = new File(frame.filename.substring(0,
            // tail),
            // head.filename);
            // file.renameTo(newFile);
            // framename = head.filename;

            // Debug.output("WARNING: File \"" + framename +
            // "\" doesn't match internal name \"" + head.filename +
            // "\" - Fixed.");
            // }

            isoverview = false;
            islegend = false;
            String padding = null;
            String seriesCode = head.filename.substring(9, 11);
            RpfProductInfo rpi = RpfProductInfo.get(seriesCode);

            if (rpi == RpfConstants.UK) {

                String dblChkSeriesCode = framename.substring(9, 11);
                RpfProductInfo rpi2 = rpi;

                if (!seriesCode.equals(dblChkSeriesCode)) {
                    rpi2 = RpfProductInfo.get(dblChkSeriesCode);
                }

                if (rpi2 == RpfConstants.UK) {

                    Debug.output("MakeToc: " + frame.filename + " / " + head.filename
                            + " (filename/header) unknown map type " + seriesCode + " / "
                            + dblChkSeriesCode + " - ignoring.");
                    continue;
                }
            }

            String scaleString = rpi.scaleString;
            if (rpi.scale == RpfConstants.Various) {
                // need to figure out how to consult the frame for
                // what it is.
                // RpfAttributes.chartSeriesCode might have something
                // to base it off.
                // GNC = GN, JNC = JN, ONC = ON, TPC = TP, JOG = 15,
                // TLM50 = V7,
                // But I'm not sure about the others. For now, prompt
                // for scale.
                scaleString = promptForScale("What is the scale for " + frame.filename
                        + "? (Answer should look like: 1:XXX,XXX)");
                if (scaleString == null || scaleString.length() == 0) {
                    Debug.error("Bad input for scale for " + frame.filename + ", skipping.");
                    continue;
                }
            }

            if (rpi.dataType.equalsIgnoreCase(RpfConstants.CIB)) {
                frame.cib = true;
            } else if (rpi.dataType.equalsIgnoreCase(RpfConstants.CDTED)) {
                frame.cdted = true;
            } // else do nothing for CADRG

            // Set the string to length 12, was 15 for some reason.
            int scaleStringLength = 12;
            if (scaleString.length() < scaleStringLength) {
                padding = createPadding(scaleStringLength - scaleString.length(), false);
                scaleString += padding;
            } else if (scaleString.length() > scaleStringLength) {
                scaleString = scaleString.substring(0, scaleStringLength);
            }

            frame.scale = scaleString;
            frame.zone = head.filename.charAt(11);

            if (isoverview) {
                coverage.nwlat = coverage.nelat = coverage.nwlon = coverage.swlon = coverage.swlat = coverage.selat = coverage.nelon = coverage.selon = 0;
                coverage.latInterval = coverage.lonInterval = coverage.nsVertRes = coverage.ewHorRes = 0;
            }

            if (islegend) {
                coverage.nwlat = coverage.nelat = coverage.nwlon = coverage.swlon = coverage.swlat = coverage.selat = coverage.nelon = coverage.selon = 0;
                coverage.latInterval = coverage.lonInterval = coverage.nsVertRes = coverage.ewHorRes = 0;
            }

            /*
             * PBF 6-18-94 check for rectangular coverage or polar frame
             */
            if (frame.zone == '9' || frame.zone == 'J') {
                /*
                 * Polar. Convert boundary from lat-long degrees to pixels
                 */
                /* DKS 1/95: North pole: "9" code */
                if (frame.zone == '9') {
                    if (Debug.debugging("maketoc"))
                        Debug.output("Processing NORTH pole");

                    frame.left = (90.0 - coverage.nwlat)
                            * Math.sin(coverage.nwlon * Math.PI / 180.0) / coverage.latInterval;

                    frame.right = (90.0 - coverage.selat)
                            * Math.sin(coverage.selon * Math.PI / 180.0) / coverage.latInterval;

                    frame.top = -1 * (90.0 - coverage.nwlat)
                            * Math.cos(coverage.nwlon * Math.PI / 180.0) / coverage.latInterval;

                    frame.bottom = -1 * (90.0 - coverage.selat)
                            * Math.cos(coverage.selon * Math.PI / 180.0) / coverage.latInterval;
                } else { /* DKS 1/95: South pole: "J" code */
                    if (Debug.debugging("maketoc"))
                        Debug.output("Processing SOUTH pole");

                    frame.left = (90.0 + coverage.nwlat)
                            * Math.sin(coverage.nwlon * Math.PI / 180.0) / coverage.latInterval;

                    frame.right = (90.0 + coverage.selat)
                            * Math.sin(coverage.selon * Math.PI / 180.0) / coverage.latInterval;

                    frame.top = (90.0 + coverage.nwlat)
                            * Math.cos(coverage.nwlon * Math.PI / 180.0) / coverage.latInterval;

                    frame.bottom = (90.0 + coverage.selat)
                            * Math.cos(coverage.selon * Math.PI / 180.0) / coverage.latInterval;
                } /* if South pole */

                /* DKS 8/1/94: Added for GEOREF calc later */
                frame.swlat = coverage.swlat;
                frame.swlon = coverage.swlon;

                if (Debug.debugging("maketoc")) {
                    Debug.output("MakeToc: " + frame.filename + " is a Polar frame");
                } /* if Debug.debugging("maketoc") */

            } else {

                frame.left = coverage.nwlon;
                frame.right = coverage.selon;

                /*
                 * NEW, DKS 6/94. Correct for frame straddling 180 deg.
                 */
                if (coverage.selon < coverage.nwlon) {
                    frame.right = 180.0;
                }

                frame.top = coverage.nwlat;
                frame.bottom = coverage.selat;
            }

            frame.h_interval = coverage.lonInterval;
            frame.v_interval = coverage.latInterval;
            frame.h_resolution = coverage.ewHorRes;
            frame.v_resolution = coverage.nsVertRes;

            frame.marked = false;

            if (Debug.debugging("maketocframedetail")) {
                Debug.output("MakeToc: nw_lon = " + coverage.nwlon + ", se_lon = " + coverage.selon
                        + "\n         nwlat = " + coverage.nwlat + ", selat = " + coverage.selat
                        + "\n    NEW: swlat = " + coverage.swlat + ", swlon = " + coverage.swlon
                        + "\n         vert_interval = " + coverage.latInterval
                        + ", horiz_interval = " + coverage.lonInterval
                        + "\n         vertical resolution = " + coverage.nsVertRes
                        + ", horizontal resolution = " + coverage.ewHorRes + "\n         left = "
                        + frame.left + ", right = " + frame.right + "\n         top = " + frame.top
                        + ", bottom = " + frame.bottom + "\n");

            }
        } /* for i: each input frame file */
    }

    /**
     * Prompt for input.
     */
    protected String promptForScale(String query) {
        try {
            String answer = null;
            System.out.println(query);
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader bufr = new BufferedReader(isr);
            answer = bufr.readLine();
            return answer;
        } catch (IOException ioe) {
            Debug.error("MakeToc: IOException trying to get an answer from you.  Dang.");
            return null;
        }
    }

    /**
     * Create and write out an A.TOC file.
     * 
     * @param filename the output filename.
     * @param head the RpfHeader containing header information.
     * @param frames the frame Vector.
     * @param groups the file groups Vector.
     */
    protected void writeTOCFile(String filename, RpfHeader head, Vector<Frame> frames,
                                Vector<Group> groups) throws MakeTocException {

        short us;
        int i, j, tail;

        /*
         * DKS changed from left, right for polar zone: new left_bottom longit.
         */
        double left_b, left_t, right_b, right_t, top, bottom;
        double xleft, xright, ytop, ybottom;

        /* !! To be filled in later */
        int TOC_Nitf_hdr_size = 0; /*
                                    * ?? Nitf header size for output TOC
                                    */
        int Loc_sec_len; /* Location section length */
        int Bound_tbl_len; /* Boundary rectangle table length */
        int Frame_hdr_len = 13; /* Frame index header length */
        int Frame_index_rec_len = 33; /*
                                       * Frame index record length (was 37)
                                       */
        int Frame_sec_len; /* Frame section length */

        RandomAccessFile fout = null;
        int groupCount = groups.size();
        int nFrames = frames.size();

        /* cumulative pathname positions */
        int[] pathname_pos = new int[nFrames];

        /* List of pathnames: directories */
        String[] direct = new String[nFrames];

        /* Allocations for uniq directories */
        int[] uniq_dir_ptr = new int[nFrames]; /*
                                                * index from filename to uniq
                                                * direct.
                                                */
        int[] uniq_dir_pos = new int[nFrames]; /*
                                                * position of direct. name in
                                                * file
                                                */

        /* list of direct. names */
        String[] uniq_dir = new String[nFrames];

        String georef = "AAAAAA"; /* GEOREF # */
        Frame frame;
        Group group;

        // Right now, just write the new file locally.
        try {
            fout = new RandomAccessFile(filename, "rw");

            /* WRITE TOC : */
            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: *** writing TOC ***\n  at: " + filename);
            }

            /* HEADER SECTION */
            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: *** writing header section ***");
            }

            String charString;
            char[] nt = new char[1];
            nt[0] = '\0';

            /* DKS. Can't write structure because of pad bytes */
            /* fwrite(&head, sizeof(head), 1, fout); */

            fout.writeBoolean(head.endian); // Big Endian - should
            // match head.endian
            fout.writeShort(RpfHeader.HEADER_SECTION_LENGTH);
            fout.writeBytes("       A.TOC"); // has to be padded.
            fout.writeByte(head.neww);

            fout.writeBytes(head.standardNumber);
            if (head.standardNumber.length() < 15) {
                fout.writeBytes(createPadding(15 - head.standardNumber.length(), false));
            }

            fout.writeBytes(head.standardDate);
            if (head.standardDate.length() < 8) {
                fout.writeBytes(createPadding(8 - head.standardDate.length(), false));
            }

            // All this trouble just for a silly character.
            char[] charArray = new char[1];
            charArray[0] = head.classification;
            charString = new String(charArray);
            fout.writeBytes(charString);

            Debug.message("maketoc", "MakeToc: writing country(" + head.country + ") and release("
                    + head.release + ")");

            fout.writeBytes(head.country);
            fout.writeBytes(head.release);

            /*
             * New, DKS. no longer head.loc_sec_phys_loc. Always write 48.
             */
            /*
             * DFD - This isn't true, but since we don't care about NITF
             * formatting, it may be. Just write out where we are.
             */
            int location_section_location = (int) fout.getFilePointer() + 4;
            fout.writeInt(location_section_location);

            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: location section location is : " + location_section_location);
            }

            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: *** writing location section ***");
            }

            /* LOCATION SECTION */
            int Loc_hdr_len = 14; /* Location section header length */
            int Loc_sec_comp_len = 10; /*
                                        * Location section component length
                                        */

            /* 14 + 4 * 10 = 54 */
            Loc_sec_len = Loc_hdr_len + (RpfFileSections.TOC_LOCATION_KEY * Loc_sec_comp_len);
            fout.writeShort(Loc_sec_len);
            /* compon. loc tbl offset: location section hdr length */
            fout.writeInt(Loc_hdr_len);
            /* # records in location section: 4 */
            fout.writeShort(RpfFileSections.TOC_LOCATION_KEY);
            /* component location record length: 10 */
            fout.writeShort(Loc_sec_comp_len);

            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc:\n  location section length: " + Loc_sec_len
                        + "\n  location header length: " + Loc_hdr_len
                        + "\n  number of location records: " + RpfFileSections.TOC_LOCATION_KEY
                        + "\n  location section comp length: " + Loc_sec_comp_len);
            }

            /*
             * compon. aggregate len: unknown here. Fill in after doing all
             * else.
             */
            /* location component aggregate length file location */
            long agg_loc = fout.getFilePointer(); /* save for later */
            fout.writeInt(0); // place holder.

            /* Begin: location section, component location table */

            int Bound_hdr_len = 8; /* Boundary section header length */
            int Bound_rec_len = 132; /* Boundary record length */

            /* Boundary section length */
            int Bound_sec_len = Bound_hdr_len + (groupCount * Bound_rec_len);

            /* Compute frame section length, for later */
            pathname_pos[0] = 0; /* cum. offset */
            int uniq_dir_cnt = 0; /* # of unique directory paths. */

            // Looking for the directory name for each file.
            for (i = 0; i < nFrames; i++) { /* for each frame file */

                /*
                 * set tail to ptr to last occurrence of '/' in filename
                 */
                /* frames[i].filename is full pathname */
                frame = (Frame) frames.elementAt(i);

                tail = frame.filename.lastIndexOf(File.separatorChar);
                if (tail == -1) {
                    direct[i] = frame.filename;
                } else {
                    // Java-cise the name, so it meets spec.
                    direct[i] = frame.filename.substring(0, ++tail).replace('\\', '/');
                }

                if (Debug.debugging("maketocdetail"))
                    Debug.output("MakeToc: Matching directory: " + direct[i]);

                /* Match direct. names with list of uniq direct. names */

                /* flag for found name in list */
                boolean uniq_dir_match = false;
                String tmpDir = null;

                if (relativeFramePaths) {
                    int rpfIndex = direct[i].lastIndexOf("RPF");
                    if (rpfIndex == -1) {
                        rpfIndex = direct[i].lastIndexOf("rpf");
                    }
                    if (rpfIndex != -1) {
                        rpfIndex += 3;
                        if (direct[i].length() > rpfIndex && direct[i].charAt(rpfIndex) == '/') {

                            rpfIndex++;
                        }
                        tmpDir = "./" + direct[i].substring(rpfIndex);
                    } else {
                        if (Debug.debugging("maketoc")) {
                            Debug.output("RPF directory not found in directory path " + direct[i]
                                    + ", using absolute path");
                        }
                        tmpDir = direct[i];
                    }

                } else {
                    tmpDir = direct[i];
                }

                for (j = 0; j < uniq_dir_cnt; j++) {
                    if (tmpDir.equals(uniq_dir[j])) {
                        uniq_dir_ptr[i] = j;
                        uniq_dir_match = true;
                        if (Debug.debugging("maketocdetail"))
                            Debug.output("Found match with: " + uniq_dir[j]);
                        break;
                    }
                }

                if (!uniq_dir_match) {
                    uniq_dir[uniq_dir_cnt] = tmpDir;
                    uniq_dir_ptr[i] = uniq_dir_cnt;
                    if (Debug.debugging("maketoc"))
                        Debug.output("Adding Unique directory: " + uniq_dir[uniq_dir_cnt]);
                    uniq_dir_cnt++;
                } /* if */

            } /* for i */

            if (Debug.debugging("maketoc"))
                Debug.output("Uniq_dir_cnt: " + uniq_dir_cnt);

            /* compute uniq dir pathname table length */
            int path_table_len = 0;
            for (j = 0; j < uniq_dir_cnt; j++) {
                /* 2 for path length var. in hdr */
                path_table_len += 2 + uniq_dir[j].length();
            } /* for j */

            /* compute directory name positions in file */
            uniq_dir_pos[0] = 0;
            for (j = 1; j < uniq_dir_cnt; j++) {
                uniq_dir_pos[j] = uniq_dir_pos[j - 1] + 2 + uniq_dir[j - 1].length();
            } /* for j */

            for (j = 0; j < uniq_dir_cnt; j++) {
                if (Debug.debugging("maketocdetail"))
                    Debug.output("j: " + j + ", uniq_dir_pos[j]: " + uniq_dir_pos[j]);
            } /* for j */

            /* compute direct. positions for each input file pathname */
            for (i = 0; i < nFrames; i++) {
                pathname_pos[i] = uniq_dir_pos[uniq_dir_ptr[i]];
                if (Debug.debugging("maketocdetail"))
                    Debug.output("i: " + i + ", pathname_pos[i]:" + pathname_pos[i]);
            } /* for i */

            /*
             * frame file index record length: 9 + nFrames * 33 + path_table_len
             */
            Frame_sec_len = Frame_hdr_len + nFrames * Frame_index_rec_len + path_table_len;

            /* START LOCATION RECORD 1 */
            /* ID #: */
            fout.writeShort((short) RpfFileSections.LOC_BOUNDARY_SECTION_SUBHEADER);

            // The boundary section subheader is the first part of the
            // boundary rectangle section. The boundary section comes
            // right after the header (in this program - the spec will
            // allow it to be anywhere).

            /* Boundary section subheader length */
            fout.writeInt(Bound_hdr_len);

            /* DKS. Physical location */
            /* 0 + 48 + 54 */
            fout.writeInt(TOC_Nitf_hdr_size + RpfHeader.HEADER_SECTION_LENGTH + Loc_sec_len);

            /* START LOCATION RECORD 2 */
            /* ID #: */
            fout.writeShort((short) RpfFileSections.LOC_BOUNDARY_RECTANGLE_TABLE);

            /* Boundary rectangle table length */
            Bound_tbl_len = groupCount * Bound_rec_len;
            fout.writeInt(Bound_tbl_len);

            /* DKS. Physical location */
            /* 0 + 48 + 54 + 8 */
            fout.writeInt(TOC_Nitf_hdr_size + RpfHeader.HEADER_SECTION_LENGTH + Loc_sec_len
                    + Bound_hdr_len);

            Bound_sec_len = Bound_hdr_len + Bound_tbl_len;

            /* START LOCATION RECORD 3 */
            /* ID #: */
            fout.writeShort((short) RpfFileSections.LOC_FRAME_FILE_INDEX_SUBHEADER);

            /* length */
            fout.writeInt(Frame_hdr_len);

            /* physical index (offset) */
            fout.writeInt(TOC_Nitf_hdr_size + RpfHeader.HEADER_SECTION_LENGTH + Loc_sec_len
                    + Bound_sec_len);

            /* START LOCATION RECORD 4 */
            /* ID #: */
            fout.writeShort((short) RpfFileSections.LOC_FRAME_FILE_INDEX_SUBSECTION);

            /* length */
            /* Frame_sec_len computed above */
            fout.writeInt(Frame_sec_len - Frame_hdr_len);

            /* physical index (offset) */
            fout.writeInt(TOC_Nitf_hdr_size + RpfHeader.HEADER_SECTION_LENGTH + Loc_sec_len
                    + Bound_sec_len + Frame_hdr_len);

            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: boundary section at : " + fout.getFilePointer());
            }

            /* START BOUNDARY RECTANGLE SECTION */
            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: *** writing boundary rectangles ***");
            }

            /* Subheader */
            /* boundary rectangle table offset */
            fout.writeInt(0);

            /* # of boundary rectangle records */
            fout.writeShort((short) groupCount);

            /* boundary rectangle record length */
            fout.writeShort((short) Bound_rec_len);

            /* For each boundary rectangle record */
            for (i = 0; i < groupCount; i++) {

                group = (Group) groups.elementAt(i);

                /*
                 * Key off flag to write proper data to A.TOC for browse menu
                 * later
                 */
                if (group.cib) {
                    fout.writeBytes("CIB  ");
                    fout.writeBytes("8:1  "); /* Compr. ratio */
                } else if (group.cdted) {
                    fout.writeBytes("CDTED");
                    fout.writeBytes("6.5:1"); /*
                                               * Compr. ratio: VARIABLE
                                               */
                } else {
                    fout.writeBytes("CADRG");
                    fout.writeBytes("55:1 ");
                } /* else */

                // Should be 12 padded chars, check just in case...
                if (group.scale.length() < 12) {
                    fout.writeBytes(group.scale);
                    fout.writeBytes(createPadding(12 - group.scale.length(), false));
                } else {
                    fout.writeBytes(group.scale.substring(0, 12)); // Already
                    // 12
                    // padded
                    // chars
                }

                // All this trouble just for a silly character.
                charArray[0] = group.zone;
                charString = new String(charArray);
                fout.writeBytes(charString);

                /* DKS changed from AFESC to DMAAC 8/2/94 */
                /* Producer: */
                // Should be OpenMap BBN, I guess.
                fout.writeBytes(producer);

                /*
                 * PBF - If group is polar, change boundaries from rect
                 * coordinates to lat-lon -- 6-19-94
                 */
                if (group.zone == '9' || group.zone == 'J') { /*
                                                               * polar zone
                                                               */
                    /*
                     * DKS: switched x,y to match spec: x increases right, y up.
                     */
                    ytop = group.horiz_pos[group.top];
                    ybottom = group.horiz_pos[group.bottom];
                    xleft = group.vert_pos[group.left];
                    xright = group.vert_pos[group.right];

                    if (Debug.debugging("maketoc")) {
                        Debug.output("POLAR ZONE. ytop: " + ytop + ", ybottom: " + ybottom
                                + ", xleft: " + xleft + ", xright:" + xright);
                    }
                    /* see CADRG SPEC 89038, p. 50 */
                    /*
                     * FIND LATITUDES from x,y. x increases right, y up.
                     */

                    /* DKS new 1/95 to handle South pole separately. */
                    /* h_interval converts from pix to deg. */
                    if (group.zone == '9') { /* "9": NORTH POLE */
                        top = 90 - (Math.sqrt((ytop * ytop) + (xleft * xleft)) * (group.h_interval));
                        bottom = 90 - (Math.sqrt((ybottom * ybottom) + (xright * xright)) * (group.h_interval));
                    } /* North pole */

                    else { /* "J": South Pole */
                        top = -90
                                + (Math.sqrt((ytop * ytop) + (xleft * xleft)) * (group.h_interval));
                        bottom = -90
                                + (Math.sqrt((ybottom * ybottom) + (xright * xright)) * (group.h_interval));
                    } /* South pole */

                    if (Debug.debugging("maketoc"))
                        Debug.output("LATS. top: " + top + ", bottom: " + bottom);

                    /*
                     * Cvt from x,y to LONGITUDE; from radians to degrees
                     */

                    /* DKS added South pole case 1/95 */

                    if (group.zone == '9') { /* "9": NORTH POLE */
                        left_t = 180.0 / Math.PI
                                * Math.acos(-ytop / Math.sqrt((ytop * ytop) + (xleft * xleft)));
                        /* DKS fixed bug 1/95: from ytop to ybottom */
                        left_b = 180.0
                                / Math.PI
                                * Math.acos(-ybottom
                                        / Math.sqrt((ybottom * ybottom) + (xleft * xleft)));
                        right_t = 180.0 / Math.PI
                                * Math.acos(-ytop / Math.sqrt((ytop * ytop) + (xright * xright)));
                        /* DKS fixed bug 1/95: from ytop to ybottom */
                        right_b = 180.0
                                / Math.PI
                                * Math.acos(-ybottom
                                        / Math.sqrt((ybottom * ybottom) + (xright * xright)));
                    } /* if North pole */

                    else { /* South Pole */
                        left_t = 180.0 / Math.PI
                                * Math.acos(ytop / Math.sqrt((ytop * ytop) + (xleft * xleft)));
                        /* DKS fixed bug 1/95: from ytop to ybottom */
                        left_b = 180.0
                                / Math.PI
                                * Math.acos(ybottom
                                        / Math.sqrt((ybottom * ybottom) + (xleft * xleft)));
                        right_t = 180.0 / Math.PI
                                * Math.acos(ytop / Math.sqrt((ytop * ytop) + (xright * xright)));
                        /* DKS fixed bug 1/95: from ytop to ybottom */
                        right_b = 180.0
                                / Math.PI
                                * Math.acos(ybottom
                                        / Math.sqrt((ybottom * ybottom) + (xright * xright)));
                    } /* if South pole */

                    /* For both poles: */
                    if (xleft < 0) { /*
                                      * left half of earth has negative longits
                                      */
                        left_t = -left_t;
                        left_b = -left_b;
                    }
                    /* This will hardly ever happen: */
                    if (xright < 0) { /*
                                       * left half of earth has negative longs
                                       */
                        right_t = -right_t;
                        right_b = -right_b;
                    }

                    if (Debug.debugging("maketoc"))
                        Debug.output("LONGS. left_t: " + left_t + ", right_t: " + right_t);
                    if (Debug.debugging("maketoc"))
                        Debug.output("LONGS. left_b: " + left_b + ", right_b: " + right_b);

                    // #if 0
                    // /* !!!!!!!!!!!!!!!!!!! Fix to getlat [80,90],
                    // longit. [-180,180] */
                    // bottom = 80.0 ;
                    // top = 90.0 ;
                    // left = -180.0 ;
                    // right = 180.0 ;
                    // #endif

                } /* if polar zone */

                /* end DKS portion */
                /* end PBF cvt from xy to lat-long */

                else { /* non-polar zone */

                    left_t = group.vert_pos[group.left];
                    left_b = left_t;
                    right_t = group.vert_pos[group.right];
                    right_b = right_t;
                    top = group.horiz_pos[group.top];
                    bottom = group.horiz_pos[group.bottom];

                } /* else */

                // Debug.output("For RpfTocEntry, writing: \n top = "
                // + top +
                // "\n bottom = " + bottom + "\n left = " + left_t +
                // "\n right = " + right_t + "\n---------");

                // Writing all doubles

                fout.writeDouble(top);
                fout.writeDouble(left_t);
                fout.writeDouble(bottom);
                fout.writeDouble(left_b);
                fout.writeDouble(top);
                fout.writeDouble(right_t);
                fout.writeDouble(bottom);
                fout.writeDouble(right_b);

                fout.writeDouble(group.v_resolution);
                fout.writeDouble(group.h_resolution);
                fout.writeDouble(group.v_interval);
                fout.writeDouble(group.h_interval);

                /* # frames */
                fout.writeInt((int) (group.bottom - group.top));
                fout.writeInt((int) (group.right - group.left));
            }

            if (Debug.debugging("maketoc")) {
                Debug.output("MakeToc: *** writing frame section ***");
                Debug.output("MakeToc: started with a 'U'");
            }

            /* START FRAME SECTION */
            /* Now write frames */

            /* security classif */
            charArray[0] = 'U';
            charString = new String(charArray);
            fout.writeBytes(charString);

            /* frame file index tbl offset */
            fout.writeInt(0);

            /* # of frame file index records */
            fout.writeInt(nFrames);

            /* # of pathname (directory) records */
            /* DKS NEW: was nFrames: */
            fout.writeShort(uniq_dir_cnt);

            /* frame file index record length : 33 */
            fout.writeShort(Frame_index_rec_len);

            /* Frame file index subsection */
            for (i = 0; i < nFrames; i++) { /* for each frame file */
                frame = (Frame) frames.elementAt(i);
                group = (Group) groups.elementAt(frame.group);

                if (!frame.marked) {
                    Debug.error(frame.filename + ": not in a boundary rect??");
                    // continue;
                }

                /* NEW, DKS: +1 removed so range is [0,n]: */
                fout.writeShort(frame.group); /* Boundary rect. rec. # */

                /* Frame location ROW number */
                /*
                 * DKS. Changed from top to bottom to fix bug in Theron's frame
                 * numbering
                 */
                /*
                 * Should start numbering at BOTTOM (southern-most part) of
                 * group
                 */
                /* !!! Changed back so row num is never <= 0 */
                /* Alternative is bottom-y, not y-bottom. Try later */
                /*
                 * us = frames[i].y - groups[frames[i].group].bottom + 1;
                 */
                /* NEW, DKS: START AT 0, NOT 1: REMOVE "+ 1": */
                /* us = frames[i].y - groups[frames[i].group].top; */

                /*
                 * SMN The frames number are from the bottom left not top left
                 */
                us = (short) (group.bottom - frame.y - 1);

                if (Debug.debugging("maketocframedetail")) {
                    Debug.output("iframe: " + i + ", frame.y: " + frame.y);
                    Debug.output("frame.group: " + frame.group);
                    Debug.output("group.bottom:" + group.bottom);
                    Debug.output("group.top:" + group.top);
                    Debug.output("frame row #:" + us);
                }

                fout.writeShort(us);

                /* Frame location Column number */
                /* NEW, DKS: START AT 0, NOT 1: REMOVE "+ 1": */
                fout.writeShort((short) (frame.x - group.left));

                /* pathname record offset: */
                /*
                 * DKS 11/10: Now w.r.t. frame file index table subsection
                 */
                /*
                 * ui = head.HEADER_SECTION_LENGTH + Loc_sec_len + Bound_sec_len
                 * + Frame_hdr_len + nFrames*Frame_index_rec_len +
                 * pathname_pos[i] ;
                 */
                fout.writeInt((int) (nFrames * Frame_index_rec_len + pathname_pos[i]));

                String framename;
                tail = frame.filename.lastIndexOf(File.separatorChar);
                if (tail == -1) {
                    framename = frame.filename;
                } else {
                    framename = frame.filename.substring(++tail);
                }
                if (framename.length() > 12) {
                    Debug.error("MakeToc: encountered a frame name that's too long!\n" + framename);
                    framename = framename.substring(0, 12);
                }

                /* frame file name */
                fout.writeBytes(framename);

                String seriesCode = framename.substring(9, 11);

                /* Check for Overview image: affects GEOREF */
                if (!seriesCode.equalsIgnoreCase("OV") && !seriesCode.equalsIgnoreCase("LG")
                        && !seriesCode.equalsIgnoreCase("OI")) {
                    /* Not Overview or Lengend img */
                    /* DKS 8/1/94: handle polar zone separately */
                    if (frame.zone != '9' || frame.zone != 'J') { /*
                                                                   * polar zone
                                                                   */
                        georef = latlong2GEOREF(frame.swlat, frame.swlon);
                    } else { /* not polar */
                        georef = latlong2GEOREF(frame.bottom, frame.left);
                    } /* else */

                } else { /* Overview image has no GEOREF */
                    if (Debug.debugging("maketoc"))
                        Debug.output("Overview image has no GEOREF");
                    georef = "000000";
                } /* else */

                fout.writeBytes(georef);

                /* classification */
                // HACK - assumes unclassified data.
                fout.writeBytes(charString);

                fout.writeBytes(head.country);
                fout.writeBytes(head.release);
            } /* for i (each frame file) */

            Debug.message("maketoc", "MakeToc: *** writing directory section ***");

            /* Pathname table */
            /*
             * Write UNIQUE pathnames: really Directory name, e.g.
             * "./CENTRAL.USA/"
             */
            for (j = 0; j < uniq_dir_cnt; j++) {
                /* DKS new */
                /*
                 * write pathname length. !!?? may be padded in front to align
                 * on word boundary!!??
                 */
                fout.writeShort((short) (uniq_dir[j].length()));

                /* pathname */
                fout.writeBytes(uniq_dir[j]);
            } /* for j (each uniq directory) */

            /* No color table index section */

            /*
             * Go back and fill in component aggregate length in location
             * section
             */
            fout.seek(agg_loc);
            fout.writeInt((int) (Bound_sec_len + Frame_sec_len));

            fout.close();
            Debug.message("maketoc", "MakeToc: *** Normal end of make-toc ***");

        } catch (IOException ioe) {
            throw new MakeTocException(ioe.getMessage());
        }

    } /* main */

    /**
     * Take the Vector of frames, and group them into boundary rectangles,
     * represented by groups. If Dchum is present, all frames get placed in
     * their own group.
     * 
     * @param frames the frame Vector.
     * @param groups the group Vector.
     * @param isDchum flag to note if Dchum frames are present.
     */
    protected void groupFrames(Vector<Frame> frames, Vector<Group> groups, boolean isDchum)
            throws MakeTocException {

        Frame frame;
        Group group;
        int groupCount;

        int nFrames = frames.size();
        Debug.message("maketoc", "MakeToc: *** grouping frames ***");

        /* For each frame file */
        for (int i = 0; i < nFrames; i++) {
            Debug.message("maketocdetail", "MakeToc: group addition, starting outer loop");

            // Assuming that the vector objects are in the same order
            // as initially loaded.
            frame = (Frame) frames.elementAt(i);

            if (!frame.marked) {
                groupCount = groups.size();

                group = new Group();

                group.left = maxSide / 2;
                group.right = group.left + 1;
                group.top = maxSide / 2;
                group.bottom = group.top + 1;

                group.horiz_pos[group.top] = frame.top;
                group.horiz_pos[group.bottom] = frame.bottom;
                group.vert_pos[group.left] = frame.left;
                group.vert_pos[group.right] = frame.right;

                group.h_interval = frame.h_interval;
                group.v_interval = frame.v_interval;
                group.h_resolution = frame.h_resolution;
                group.v_resolution = frame.v_resolution;

                group.scale = frame.scale;
                group.zone = frame.zone;
                group.cib = frame.cib;
                group.cdted = frame.cdted;

                frame.x = group.left;
                /*
                 * DKS. Changed from top to bottom to fix bug in Theron's frame
                 * numbering
                 */
                /*
                 * Should start numbering at BOTTOM (southern-most part) of
                 * group
                 */
                /* DKS. Switched back to fix row # <=0 bug */
                frame.y = group.top;
                frame.group = groupCount;
                frame.marked = true;

                Debug.message("maketocdetail", "Maketoc.groupFrames: created group " + groupCount
                        + " for frame " + i + ", - " + frame.filename
                        + " checking other frames for neighbors");

                /*
                 * If Dchum, create 1 group for each file. No need for call to
                 * "add".
                 */
                if (!isDchum) {
                    for (int j = 0; j < nFrames; j++) {
                        if (i == j) {
                            Debug.message("maketocdetail", "Maketoc.groupFrames: inner loop, i = j = "
                                    + i
                                    + ", frame that created group added to group, expecting false return");
                            continue;
                        }
                        Frame f = (Frame) frames.elementAt(j);
                        if (addFrameToGroup(group, f, groupCount)) {
                            Debug.message("maketocdetail", "Maketoc.groupFrames: added frame " + j
                                    + " to group " + groupCount);
                            continue;
                        }
                    }
                }

                Debug.message("maketocdetail", "Maketoc.groupFrames: adding another group - "
                        + groupCount + " *******************\n\n");

                groups.add(group);
            } /* if !frame.marked */

            fireProgressUpdate(ProgressEvent.UPDATE, "Organizing frames", i, nFrames);

        }/* for (i = 0; i < nFrames; i++) */

        if (Debug.debugging("maketoc")) {
            Debug.output("MakeToc: Number of boundary rectangles (groups): " + groups.size());
        }
    }

    /**
     * Does the actual checking to see if the frame gets added to the group, by
     * checking the frame's location with the group's current boundaries, and
     * resizing the group boundary if the frame is touching it. Assumes
     * everything has been allocated in the group and frame. Not prepared for
     * either being null.
     * 
     * @param grp the group
     * @param frm the frame.
     * @param index the group index, referring to it's position in the Group
     *        Vector.
     */
    protected boolean addFrameToGroup(Group grp, Frame frm, int index) throws MakeTocException {

        int i;
        int x;
        int y;

        if (frm.scale == null || grp.scale == null) {
            // This is a strange situation. The product codes in the file name
            // or header aren't resolving into a scale.

            // if these aren't set up properly, then other parameters aren't set
            // up properly either.
            Debug.output("grp and frm scale is null for " + frm.filename + ", skipping");
            return false;
        } else if (frm.marked || !frm.scale.equalsIgnoreCase(grp.scale) || frm.zone != grp.zone) {
            Debug.message("maketocframedetail", "\nMakeToc.addFrameToGroup: no action needed for frame, returning.\n  frm.marked = "
                    + frm.marked
                    + "\n  frm.zone("
                    + frm.zone
                    + ") = grp.zone("
                    + grp.zone
                    + ")\n  frm.scale(" + frm.scale + ") = grp.scale(" + grp.scale + ")\n");
            return false;
        }

        Debug.message("maketocframedetail", "MakeToc.addFrameToGroup: adding unmarked frame");

        double eps = frm.EPS();

        /* DKS. EPS TOLERANCE ADDED throughout */
        if (frm.left >= grp.vert_pos[grp.left] - eps && frm.right <= grp.vert_pos[grp.right] + eps
                && frm.bottom >= grp.horiz_pos[grp.bottom] - eps
                && frm.top <= grp.horiz_pos[grp.top] + eps) {

            if (Debug.debugging("maketocdetail")) {
                Debug.output(frm.filename + " is in group " + index);
            }

        } else if (near(frm.right, grp.vert_pos[grp.left], eps)
                && frm.top <= grp.horiz_pos[grp.top] + eps
                && frm.bottom >= grp.horiz_pos[grp.bottom] - eps) {

            if (Debug.debugging("maketocdetail")) {
                Debug.output(frm.filename + " add frame to group " + index + ": left side");
            }

            if (grp.left == 0) {
                throw new MakeTocException("Boundary rectangle too small - Increase the boundary size to be larger than "
                        + maxSide);
            }

            grp.left--; /* add to left side */
            grp.vert_pos[grp.left] = frm.left;
        } else if (near(frm.left, grp.vert_pos[grp.right], eps)
                && frm.top <= grp.horiz_pos[grp.top] + eps
                && frm.bottom >= grp.horiz_pos[grp.bottom] - eps) {

            if (Debug.debugging("maketocdetail")) {
                Debug.output(frm.filename + ":add frame to group " + index + ": right side");
            }

            if (grp.right == maxSide) {
                throw new MakeTocException("Boundary rectangle too small - Increase the boundary size to be larger than "
                        + maxSide);
            }

            grp.vert_pos[grp.right] = frm.left;
            grp.right++; /* add to right */
            grp.vert_pos[grp.right] = frm.right;

        } else if (near(frm.bottom, grp.horiz_pos[grp.top], eps)
                && frm.right <= grp.vert_pos[grp.right] + eps
                && frm.left >= grp.vert_pos[grp.left] - eps) {

            if (Debug.debugging("maketocdetail")) {
                Debug.output(frm.filename + ":add frame to group " + index + ": top");
            }

            if (grp.top == 0) {
                throw new MakeTocException("Boundary rectangle too small - Increase the boundary size to be larger than "
                        + maxSide);
            }

            grp.top--; /* add to top */
            grp.horiz_pos[grp.top] = frm.top;
        } else if (near(frm.top, grp.horiz_pos[grp.bottom], eps)
                && frm.right <= grp.vert_pos[grp.right] + eps
                && frm.left >= grp.vert_pos[grp.left] - eps) {

            if (Debug.debugging("maketocdetail")) {
                Debug.output(frm.filename + ":add frame to group " + index + ": bottom");
            }

            if (grp.bottom == maxSide) {
                throw new MakeTocException("Boundary rectangle too small - Increase the boundary size to be larger than "
                        + maxSide);
            }

            grp.horiz_pos[grp.bottom] = frm.top;
            grp.bottom++; /* add to bottom */
            grp.horiz_pos[grp.bottom] = frm.bottom;
        } else {
            Debug.message("maketocframedetail", "MakeToc.add: frame not close enough to anything else, not adding to group.");
            return false;
        }

        x = y = -1;
        for (i = grp.left; i < grp.right; i++) {
            /*
             * PBF - Change from (==) to near function for polar 6-19-94
             */
            if (near(frm.left, grp.vert_pos[i], eps)) {
                x = i;
                break;
            }
        }

        for (i = grp.top; i < grp.bottom; i++) {
            /*
             * PBF - Change from (==) to near function for polar 6-19-94
             */
            if (near(frm.top, grp.horiz_pos[i], eps)) {
                y = i;
                break;
            }
        }

        if (x < 0 || y < 0) {
            Debug.output("MakeToc: " + frm.filename
                    + ": in rect but can't find boundary (horizontal" + (x < 0 ? " bad" : " OK")
                    + ", vertical" + (y < 0 ? " bad)" : " OK)"));

            if (Debug.debugging("maketocframedetail")) {

                Debug.output(" - For frame: \n  " + frm.toString());

                Debug.output(" - Group horizontal left: " + grp.left + " vs. right: " + grp.right);

                for (i = grp.left; i < grp.right; i++) {
                    /*
                     * PBF - Change from (==) to near function for polar 6-19-94
                     */
                    Debug.output(" - Checking horizontal: " + frm.left + " <-> " + grp.vert_pos[i]);
                    if (near(frm.left, grp.vert_pos[i], eps)) {
                        Debug.output(" Last one should have hit.");
                    }
                }

                Debug.output(" - Group vertical top: " + grp.horiz_pos[grp.top] + " vs. bottom: "
                        + grp.horiz_pos[grp.bottom] + ", frame top = " + frm.top
                        + " and frame bottom = " + frm.bottom);

                for (i = grp.top; i < grp.bottom; i++) {
                    /*
                     * PBF - Change from (==) to near function for polar 6-19-94
                     */
                    Debug.output(" - Checking vertical: " + frm.top + " <-> " + grp.horiz_pos[i]);
                    if (near(frm.top, grp.horiz_pos[i], eps)) {
                        Debug.output(" Last one should have hit.");
                    }
                }
            }

            throw new MakeTocException(frm.filename
                    + " in rect but can't find boundary (horizontal" + (x < 0 ? " bad" : " OK")
                    + ", vertical" + (y < 0 ? " bad)" : " OK)"));
        }

        /* DKS ABS, frm.EPS2 added */
        /*
         * DKS 8/16/94: h_resolution (meters/pix) will vary from frame to frame
         * NS
         */
        /* Therefore don't check for a match here */
        if (Math.abs(frm.h_interval - grp.h_interval) > EPS2
                || Math.abs(frm.v_interval - grp.v_interval) > EPS2) /*
                                                                      * deg /
                                                                      * pix
                                                                      */
        /*
         * Math.abs (frm.h_resolution - grp.h_resolution) > EPS2 || Math.abs
         * (frm.v_resolution - grp.v_resolution) > EPS2)
         */
        {
            Debug.error(frm.filename + ": interval mismatch\n  frm.h_interval: " + frm.h_interval
                    + ", grp.h_interval:" + grp.h_interval + "\n  frm.v_interval: "
                    + frm.v_interval + ", grp.v_interval: " + grp.v_interval
                    + "\n  frm.h_resolution: " + frm.h_resolution + ", grp.h_resolution: "
                    + grp.h_resolution + "\n  frm.h_resolution: " + frm.h_resolution
                    + ", grp.h_resolution: " + grp.h_resolution);
            throw new MakeTocException(frm.filename + " has mismatched frame resolution");
        }

        frm.marked = true;
        frm.group = index;
        frm.x = x;
        frm.y = y;
        grp.cib = frm.cib;
        grp.cdted = frm.cdted;
        return true;
    } /* add */

    /**
     * This program attempts to convert latitudes and longitudes given in a
     * decimal format into a GEOREF alphanumeric designation code. The first
     * letter of the code denotes the longitudinal 15 degree grid that contains
     * the area of interest. The second letter denotes the latitudinal 15 degree
     * grid. The third letter denotes the one degree longitudinal grid within
     * the 15 degree longitudinal grid. The fourth letter denotes the one degree
     * latitudinal grid within the 15 degree latitudinal grid. The fifth
     * character is a number denoting the minutes longitudinally to the nearest
     * 10. The sixth number denotes the minutes latitudinally to the nearest 10.
     * Wouldn't it just have been easier to use the decimal latitudes and
     * longitudes?
     */
    protected String latlong2GEOREF(double latitude, double longitude) {
        int i;
        char tmp = 'A'; // no reason for 'A'
        char tmp1 = 'A';
        char tmp2 = 'A';

        // These serve as tmps in integer form.
        int tmpi, tmpi1, tmpi2;

        /*
         * this portion of the code calculates the longitudinal part of the
         */
        /*
         * GEOREF number. I can't explain the logic -- I don't understand
         */
        /* how it works. All that I know is that it seems to. */
        LatLonPoint llp = new LatLonPoint.Double(latitude, longitude);
        DMSLatLonPoint dmsp = new DMSLatLonPoint(llp);

        char[] GEOSTRING = new char[6];

        if (longitude == 0.0000) {
            tmp = 'N';
            tmp1 = 'A';
            tmp2 = '0';
        } else if (longitude == -180.0000) {
            tmp = 'A';
            tmp1 = 'A';
            tmp2 = '0';
        } else if (longitude == 180.0000) {
            tmp = 'Z';
            tmp1 = 'Q';
            tmp2 = '9';
        } else if (longitude > 0.0000) {
            tmpi = dmsp.lon_degrees / 15;
            tmpi += 78;
            if (tmpi >= 79) {
                tmpi += 1;
            }
            if (tmpi > 90) {
                tmpi = 90;
            }
            tmp = (char) tmpi;

            // Setting i to a certain value, based on longitude.
            for (i = 0; i * 15 < (int) (longitude + 0.9999); i++) {
            }
            tmpi1 = 15 * i - (int) (longitude);
            if ((tmpi1 >= 3) && (tmpi1 < 8)) {
                tmpi1 += 1;
            } else if (tmpi1 >= 8) {
                tmpi1 += 2;
            }

            if (tmpi1 != 0) {
                tmpi1 = 82 - tmpi1;
                tmp1 = (char) tmpi1;
            } else {
                tmp1 = 'A';
            }
            if (tmp1 == 'R') {
                tmp1 = 'A';
            }
            tmpi2 = (int) ('0') + (dmsp.lon_minutes / 10);
            tmp2 = (char) tmpi2;

        } else if (longitude <= 0.0000) {

            tmpi = (int) (((double) dmsp.lon_degrees) / 15.0 - 0.999);
            tmpi = 77 - Math.abs(tmpi);
            if (tmpi >= 73) {
                tmpi += 1;
            }
            if (tmpi > 77) {
                tmpi = 77;
            }
            tmp = (char) tmpi;

            /* DKS changed from abs to fabs */
            for (i = 0; i * 15 < (int) (Math.abs((longitude - 0.9999))); i++) {
            }
            /* DKS changed from abs to fabs */
            tmpi1 = i * 15 - (int) (Math.abs((longitude - 0.9999)));
            if ((tmpi1 >= 8) && (tmpi1 < 13)) {
                tmpi1 += 1;
            } else if (tmpi1 >= 13) {
                tmpi1 += 2;
            }
            if (tmpi1 > 16) {
                tmpi1 = 16;
            }
            tmpi1 += 65;
            tmp1 = (char) tmpi1;

            if ((int) (dmsp.lon_minutes / 10) != 0) {
                tmpi2 = ((int) '0') + (6 - (int) (dmsp.lon_minutes / 10));
                tmp2 = (char) tmpi2;
            } else {
                tmp2 = '0';
            }
        }

        GEOSTRING[0] = tmp;
        GEOSTRING[2] = tmp1;
        GEOSTRING[4] = tmp2;

        /*
         * this portion of the code calculates the latitudinal part of the
         */
        /*
         * GEOREF number. I can't explain the logic -- I don't understand
         */
        /* how it works. All that I know is that it seems to. */

        if (latitude == 0.0000) {
            tmp = 'G';
            tmp1 = 'A';
            tmp2 = '0';
        } else if (latitude == 90.0000) {
            tmp = 'M';
            tmp1 = 'Q';
            tmp2 = '9';
        } else if (latitude == -90.0000) {
            tmp = 'A';
            tmp1 = 'A';
            tmp2 = '0';
        } else if (latitude > 0.0000) {
            tmpi = dmsp.lat_degrees / 15;
            tmpi += 71;
            if (tmpi >= 73) {
                tmpi += 1;
            }
            if (tmpi > 77) {
                tmpi = 77;
            }
            tmp = (char) tmpi;

            for (i = 0; i * 15 < (int) (latitude + 0.9999); i++) {
            }
            tmpi1 = 15 * i - (int) (latitude);
            if ((tmpi1 >= 3) && (tmpi1 < 8)) {
                tmpi1 += 1;
            } else if (tmpi1 >= 8) {
                tmpi1 += 2;
            }
            tmpi1 = 82 - tmpi1;
            tmp1 = (char) tmpi1;

            if (tmp1 == 'R') {
                tmp1 = 'A';
            }
            if ((dmsp.lat_minutes / 10) != 0) {
                tmpi2 = ((int) '0') + (int) (dmsp.lat_minutes / 10);
                tmp2 = (char) tmpi2;
            } else {
                tmp2 = '0';
            }

        } else if (latitude < 0.0000) {

            tmpi = (int) ((double) dmsp.lat_degrees / 15.0 - 0.999);
            tmpi = 71 - Math.abs(tmpi);
            if (tmpi < 65) {
                tmpi = 65;
            }
            /* DKS changed from abs to fabs */
            for (i = 0; i * 15 < (int) (Math.abs((latitude - 0.9999))); i++) {
            }
            /* DKS changed from abs to fabs */
            tmpi1 = i * 15 - (int) (Math.abs((latitude - 0.9999)));
            if ((tmpi1 >= 8) && (tmpi1 < 13)) {
                tmpi1 += 1;
            } else if (tmpi1 >= 13) {
                tmpi1 += 2;
            }
            if (tmpi1 > 16) {
                tmpi1 = 16;
            }
            tmpi1 = 65 + tmpi1;
            tmp1 = (char) tmpi1;

            tmpi2 = ((int) '0') + (6 - (int) (dmsp.lat_minutes / 10));
            tmp2 = (char) tmpi2;
        }

        GEOSTRING[1] = tmp;
        GEOSTRING[3] = tmp1;
        GEOSTRING[5] = tmp2;

        String ret = new String(GEOSTRING);
        if (Debug.debugging("maketocdetail")) {
            Debug.output("latlon2GEOREF: lat = " + latitude + ", lon = " + longitude
                    + ", GEOREF = " + ret);
        }

        return ret;
    } /* latlong2GEOREF() */

    public String createPadding(int length, boolean nullTerminated) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            if (i == length - 1 && nullTerminated) {
                sb.append("/0");
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
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
    protected void fireProgressUpdate(int type, String task, int frameNumber, int totalFrames) {
        progressSupport.fireUpdate(type, task, totalFrames, frameNumber);
    }
}