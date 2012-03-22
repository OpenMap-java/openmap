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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDAdmin.java,v $
// $RCSfile: DTEDAdmin.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/04 18:08:12 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * DTEDAdmin is a utility class that finds DTED frame files in order
 * to copy or delete them. You can specify coordinate boundaries and
 * DTED level as filters. Usage:
 * <P>
 * 
 * <pre>
 * 
 *   -help       Print usage statement, with arguments. (0 arguments expected)
 *   -boundary   upper lat, left lon, lower lat, right lon (4 arguments expected)
 *   -copy       Copy files to DTED directory. (1 argument expected)
 *   -level      DTED level to consider (0, 1, 2). (1 argument expected)
 *   -outside    Use files outside boundary. (0 arguments expected)
 *   -query      Print out files that meet parameters. (0 arguments expected)
 *   -remove     Delete DTED files. (0 arguments expected)
 *   -source     The source DTED directory path. (1 argument expected)
 *   -verbose    Print out progress. (0 arguments expected)
 *  
 * </pre>
 */
public class DTEDAdmin {

    protected boolean framesPrepped = false;
    protected int level_;
    protected double ullat_;
    protected double ullon_;
    protected double lrlat_;
    protected double lrlon_;
    protected boolean inside_;
    protected int equal_;

    protected LinkedList frameList = null;
    protected DTEDLocator locator = null;

    public final static int MAXLEVELS = 3;
    public final static int DTED_LARGER_LEVELS = 0;
    public final static int DTED_SMALLER_LEVELS = 1;
    public final static int DTED_NOTEQUAL_LEVELS = 2;
    public final static int DTED_EQUAL_LEVELS = 3;

    /**
     * Create a DTEDAdmin object, with file filter parameters to be
     * specified later.
     */
    public DTEDAdmin() {}

    /**
     * Create a DTEDAdmin with the following parameters.
     * 
     * @param dtedDir the source dted directory
     * @param ullat the upper latitude of the boundary box to use.
     * @param ullon the western latitude of the boundary box to use,
     *        greater than -180.
     * @param lrlat the lower latitude of the boundary box to use.
     * @param lrlon the eastern latitude of the boundary box to use,
     *        less than 180.
     * @param level the dted level to consider.
     * @param inside if true, files inside the boundary box will be
     *        considered. If false, files outside the box will be.
     * @param equal filter for the level - Possible values are
     *        DTED_LARGER_LEVELS (any file with a level greater than
     *        the one specified) , DTED_SMALLER_LEVELS (any file with
     *        a level less than the one specified),
     *        DTED_NOTEQUAL_LEVELS (any file with a level not equal to
     *        the one specified), and DTED_EQUAL_LEVELS (any file with
     *        the level specified).
     */
    public DTEDAdmin(String dtedDir, double ullat, double ullon, double lrlat,
            double lrlon, int level, boolean inside, int equal) {
        setFrameList(dtedDir, ullat, ullon, lrlat, lrlon, level, inside, equal);
    }

    /**
     * Create the internal list of frame files based on the following
     * parameters.
     * 
     * @param dtedDir the source dted directory
     * @param ullat the upper latitude of the boundary box to use.
     * @param ullon the western latitude of the boundary box to use,
     *        greater than -180.
     * @param lrlat the lower latitude of the boundary box to use.
     * @param lrlon the eastern latitude of the boundary box to use,
     *        less than 180.
     * @param level the dted level to consider.
     * @param inside if true, files inside the boundary box will be
     *        considered. If false, files outside the box will be.
     * @param equal filter for the level - Possible values are
     *        DTED_LARGER_LEVELS (any file with a level greater than
     *        the one specified) , DTED_SMALLER_LEVELS (any file with
     *        a level less than the one specified),
     *        DTED_NOTEQUAL_LEVELS (any file with a level not equal to
     *        the one specified), and DTED_EQUAL_LEVELS (any file with
     *        the level specified).
     */
    protected LinkedList organizeFrames(String dtedDir, double ullat,
                                        double ullon, double lrlat,
                                        double lrlon, int level,
                                        boolean inside, int equal) {

        framesPrepped = false;

        if (Debug.debugging("dted")) {
            Debug.output("DTEDAdmin: Checking for directory " + dtedDir);
        }

        LinkedList frames = null;

        if (true/* (new File(dtedDir)).exists() */) { // not
            level_ = level;
            ullat_ = ullat;
            ullon_ = ullon;
            lrlat_ = lrlat;
            lrlon_ = lrlon;
            inside_ = inside;
            equal_ = equal;

            Debug.output("DTEDAdmin: Figuring out which frames fit the criteria...");
            frames = getFrameList(dtedDir);
            framesPrepped = true;
        }
        return frames;
    }

    /**
     * Create the internal list of frame files based on the following
     * parameters.
     * 
     * @param dtedDir the source dted directory
     * @param ullat the upper latitude of the boundary box to use.
     * @param ullon the western latitude of the boundary box to use,
     *        greater than -180.
     * @param lrlat the lower latitude of the boundary box to use.
     * @param lrlon the eastern latitude of the boundary box to use,
     *        less than 180.
     * @param level the dted level to consider.
     * @param inside if true, files inside the boundary box will be
     *        considered. If false, files outside the box will be.
     * @param equal filter for the level - Possible values are
     *        DTED_LARGER_LEVELS (any file with a level greater than
     *        the one specified) , DTED_SMALLER_LEVELS (any file with
     *        a level less than the one specified),
     *        DTED_NOTEQUAL_LEVELS (any file with a level not equal to
     *        the one specified), and DTED_EQUAL_LEVELS (any file with
     *        the level specified).
     */
    public void setFrameList(String dtedDir, double ullat, double ullon,
                             double lrlat, double lrlon, int level,
                             boolean inside, int equal) {
        setFrameList(organizeFrames(dtedDir,
                ullat,
                ullon,
                lrlat,
                lrlon,
                level,
                inside,
                equal));
    }

    /**
     * Set the internal frame list, a LinkedList expected to hold File
     * objects.
     */
    protected void setFrameList(LinkedList ll) {
        frameList = ll;
    }

    /**
     * Get the internal frame list, a LinkedList expected to hold File
     * objects.
     */
    protected LinkedList getFrameList() {
        return frameList;
    }

    /**
     * Figure out the frames with the current parameters for the
     * source directory provided.
     */
    protected LinkedList getFrameList(String dtedDir) {
        int lev;
        boolean dothisone;
        int leftx = (int) Math.floor(ullon_);
        int rightx = (int) Math.ceil(lrlon_);
        int bottomy = (int) Math.floor(lrlat_);
        int topy = (int) Math.ceil(ullat_);

        LinkedList frames = new LinkedList();

        locator = new DTEDLocator(dtedDir);
        locator.organize();

        for (int hor = leftx; hor < rightx; hor++) {
            for (int ver = bottomy; ver < topy; ver++) {
                for (lev = 0; lev < MAXLEVELS; lev++) {
                    dothisone = false;
                    switch (equal_) {
                    case DTED_LARGER_LEVELS:
                        if (lev > level_)
                            dothisone = true;
                        break;
                    case DTED_SMALLER_LEVELS:
                        if (lev < level_)
                            dothisone = true;
                        break;
                    case DTED_NOTEQUAL_LEVELS:
                        if (lev != level_)
                            dothisone = true;
                        break;
                    case DTED_EQUAL_LEVELS:
                    default:
                        if (lev == level_)
                            dothisone = true;
                        break;
                    }

                    if (dothisone) {
                        File file = locator.get(ver, hor, lev);

                        if (file != null) {
                            if (Debug.debugging("dted")) {
                                Debug.output("DTEDAdmin adding "
                                        + file.getAbsolutePath() + " to list");
                            }
                            frames.add(file);
                        }
                    }
                }
            }
        }

        return frames;
    }

    /**
     * Get the internal frame list and copy those frames to the given
     * directory.
     * 
     * @return true if everything went OK, false if not enough
     *         information is available to create a source file list.
     */
    public boolean copyTo(String todteddir) {
        return copyTo(getFrameList(), todteddir);
    }

    /**
     * Get the internal frame list and copy those frames to the given
     * directory.
     * 
     * @param files a LinkedList of Files to copy.
     * @param todteddir a dted directory to copy files into.
     * @return true if everything went OK, false if not enough
     *         information is available to create a source file list.
     */
    protected boolean copyTo(LinkedList files, String todteddir) {
        if (files == null) {
            Debug.error("No files configured for copying!");
            return false;
        }

        Iterator it = files.iterator();

        while (it.hasNext()) {

            File file = (File) it.next();
            DTEDNameTranslator dnt = locator.getTranslator();

            try {
                dnt.set(file.getAbsolutePath());

                String dsd = dnt.getSubDirs();
                if (dsd != null && dsd.length() > 0) {
                    dsd = "/" + dsd;
                }

                File toDir = new File(todteddir + dsd);
                if (!toDir.exists()) {
                    toDir.mkdirs();
                }

                File outputFile = new File(toDir, dnt.getFileName());

                if (Debug.debugging("dted")) {
                    Debug.output("DTEDAdmin copying " + file.getAbsolutePath()
                            + " to " + outputFile.getAbsolutePath());
                }

                BinaryBufferedFile input = new BinaryBufferedFile(file);
                RandomAccessFile output = new RandomAccessFile(outputFile, "rw");
                byte[] bytes = new byte[4096];
                int numBytes = input.read(bytes);
                while (numBytes > 0) {
                    output.write(bytes, 0, numBytes);
                    numBytes = input.read(bytes);
                }

                input.close();
                output.close();

            } catch (FormatException fe) {
                continue;
            } catch (IOException ioe) {
                continue;
            }
        }
        return true;
    }

    /**
     * Delete the DTED files that meet the internal file list
     * parameters.
     * 
     * @return true if everything went OK, false if not enough
     *         information is available to create a source file list.
     */
    public boolean remove() {
        return remove(getFrameList());
    }

    /**
     * Delete the DTED files that meet the internal file list
     * parameters.
     * 
     * @param files the LinkedList of File objects representing DTED
     *        frame files.
     * @return true if everything went OK, false if not enough
     *         information is available to create a source file list.
     */
    protected boolean remove(LinkedList files) {
        if (files == null) {
            Debug.error("No files configured for removal!");
            return false;
        }

        Iterator it = files.iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            Debug.output("DTEDAdmin deleting " + file.getAbsolutePath());
            file.delete();
        }
        return true;
    }

    /**
     * Print out a list of DTED files that meet the current internal
     * parameters.
     */
    public boolean query() {
        return query(getFrameList());
    }

    /**
     * Print out a list of DTED files that are on the provided list of
     * File objects.
     */
    protected boolean query(LinkedList files) {
        if (files == null) {
            Debug.error("No files configured for query!");
            return false;
        }

        Iterator it = files.iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            Debug.output("  " + file.getAbsolutePath());
        }
        return true;
    }

    /**
     * Run DTEDAdmin from the command line.
     */
    public static void main(String[] argv) {
        Debug.init();

        ArgParser ap = new ArgParser("DTEDAdmin");
        ap.add("boundary", "upper lat, left lon, lower lat, right lon", 4, true);
        ap.add("copy", "Copy files to DTED directory.", 1);
        ap.add("level",
                "DTED level to consider (0, 1, 2), 0 is default.  Needs to be set for other levels.",
                1);
        ap.add("outside", "Use files outside boundary.");
        ap.add("query", "Print out files that meet parameters.");
        ap.add("remove", "Delete DTED files.");
        ap.add("source", "The source DTED directory path.", 1);
        ap.add("verbose", "Print out progress.");

        if (!ap.parse(argv)) {
            ap.printUsage();
            System.exit(0);
        }

        String arg[];
        String sourceDir = null;

        arg = ap.getArgValues("source");
        if (arg != null) {
            sourceDir = arg[0];
        }

        boolean inside = true;
        arg = ap.getArgValues("outside");
        if (arg != null) {
            inside = false;
        }

        int level = 0;
        arg = ap.getArgValues("level");
        if (arg != null) {
            try {
                level = Integer.parseInt(arg[0]);
            } catch (NumberFormatException nfe) {
                level = 0;
            }
        }

        arg = ap.getArgValues("verbose");
        if (arg != null) {
            Debug.put("dted");
        }

        DTEDAdmin admin = null;
        double ullat = 89;
        double ullon = -180;
        double lrlat = -90;
        double lrlon = 179;

        arg = ap.getArgValues("boundary");
        if (arg != null) {
            try {
                ullat = Double.parseDouble(arg[0]);
                ullon = Double.parseDouble(arg[1]);
                lrlat = Double.parseDouble(arg[2]);
                lrlon = Double.parseDouble(arg[3]);
            } catch (NumberFormatException nfe1) {
                Debug.error("DTEDAdmin: boundary coordinates not valid:\n"
                        + "  " + arg[0] + "\n  " + arg[1] + "\n  " + arg[2]
                        + "\n  " + arg[3]);
                System.exit(0);
            }
        }

        if (sourceDir != null) {
            admin = new DTEDAdmin(sourceDir, ullat, ullon, lrlat, lrlon, level, inside, DTEDAdmin.DTED_EQUAL_LEVELS);
        }

        arg = ap.getArgValues("copy");
        if (arg != null) {
            if (admin != null) {
                admin.copyTo(arg[0]);
            } else {
                Debug.error("DTEDAdmin:  frame parameters not set for copy.  Need source directory");
                System.exit(0);
            }
        }

        arg = ap.getArgValues("query");
        if (arg != null) {
            if (admin != null) {
                Debug.output("DTED frame files found:");
                admin.query();
            } else {
                Debug.error("DTEDAdmin:  frame parameters not set for query.  Need source directory");
                System.exit(0);
            }
        }

        arg = ap.getArgValues("remove");
        if (arg != null) {
            if (admin != null) {
                Debug.output("These files will be deleted:");
                admin.query();
                Debug.output("Are you sure you want to delete them? [y/N]");
                int answer = 'n';
                try {
                    answer = System.in.read();
                } catch (IOException ioe) {
                }

                if (answer == 'Y' || answer == 'y') {
                    admin.remove();
                } else {
                    Debug.output("File removal aborted.");
                }
            } else {
                Debug.error("DTEDAdmin:  frame parameters not set for copy.  Need source directory");
                System.exit(0);
            }

        }

    }

}