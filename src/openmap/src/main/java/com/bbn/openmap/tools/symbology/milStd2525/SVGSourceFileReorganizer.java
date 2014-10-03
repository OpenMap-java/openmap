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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/wanderer/ChangeCase.java,v $
// $RCSfile: ChangeCase.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.io.File;
import java.io.IOException;

import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.wanderer.Wanderer;
import com.bbn.openmap.util.wanderer.WandererCallback;

/**
 * SVGSourceFileReorganizer is a simple utility that takes the source SVG files
 * from the DISA package and reorganizes them into a simplier file structure
 * that OpenMap classes can use. DISA has extraneous characters at the front of
 * the svg file names (a number for which symbol in a particular directory and a
 * period) and they are organized in a named directory structure. This wanderer
 * finds all of the svg files, chops off the first two characters and places
 * them in a target directory.
 * 
 * If you want to convert the files to raster formats (gif, png) use the batik rasterizer:
 * 
 * <pre>
 * 
 * java -jar batik-rasterizer.jar FILES
 * 
 * </pre>
 * 
 * Otherwise, to use this class:
 * 
 * <pre>
 * 
 *  Usage: java com.bbn.openmap.tools.symbology.milStd2525.SVGSourceFileReorganizer -target targetDir -source (dir path 1)(dir path 2) ...
 * 
 * </pre>
 */
public class SVGSourceFileReorganizer
        extends Wanderer
        implements WandererCallback {

    File rootDir;
    boolean verbose = false;

    public SVGSourceFileReorganizer(File rootDir) {
        super();
        this.rootDir = rootDir;
        this.rootDir.mkdirs();
        setExhaustiveSearch(true);
        setTopToBottom(false);
        setCallback(this);
    }

    public void setVerbose(boolean val) {
        verbose = val;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public boolean handleFile(File file) {
        File newFile;
        String parent = file.getParent();

        String fileName = file.getName();
        if (!fileName.endsWith(".svg")) {
            return true;
        }

        String newFileName = fileName.substring(2);

        File target = new File(rootDir, newFileName);
        try {
            if (verbose) {
                System.out.println("copying " + file + " to " + target);

                if (target.exists()) {
                    System.out.println("++++ overwriting " + target);
                }
            }

            FileUtils.copy(file, target, 2500);
        } catch (IOException ioe) {
            if (verbose) {
                System.out.println("Problem copying " + file + " to " + target);
            }
        }

        return true;
    }

    /**
     * Given a set of files or directories, parade through them to change their
     * case.
     * 
     * @param argv paths to files or directories, use -h to get a usage
     *        statement.
     */
    public static void main(String[] argv) {
        Debug.init();
        boolean toUpper = true;

        ArgParser ap = new ArgParser("ChangeCase");
        ap.add("source", "The directory to search for source svg files.", ArgParser.TO_END);
        ap.add("target", "The target directory to place gathered and modified files.", 1);
        ap.add("verbose", "Talk alot.");

        if (argv.length == 0) {
            ap.bail("", true);
        }

        ap.parse(argv);

        String[] sourceDirs;
        sourceDirs = ap.getArgValues("source");
        if (sourceDirs == null) {
            ap.bail("Need source directories", true);
        }

        String[] targetDirs;
        targetDirs = ap.getArgValues("target");
        if (targetDirs == null || targetDirs.length > 1) {
            ap.bail("Need target directory", true);
        }

        boolean verbose = false;
        String[] verboseTest = ap.getArgValues("verbose");
        if (verboseTest != null) {
            verbose = true;
        }

        if (targetDirs != null && sourceDirs != null) {
            SVGSourceFileReorganizer cc = new SVGSourceFileReorganizer(new File(targetDirs[0]));
            cc.setVerbose(verbose);

            // Assume that the arguments are paths to directories or
            // files.
            for (int i = 0; i < sourceDirs.length; i++) {
                cc.handleEntry(new File(sourceDirs[i]));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.util.wanderer.WandererCallback#handleDirectory(java.io
     * .File)
     */
    public boolean handleDirectory(File directory) {
        return true; // Continue wandering
    }
}