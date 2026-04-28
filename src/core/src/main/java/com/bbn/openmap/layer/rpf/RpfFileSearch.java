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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfFileSearch.java,v $
// $RCSfile: RpfFileSearch.java,v $
// $Revision: 1.8 $
// $Date: 2006/08/17 15:19:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

import java.io.File;
import java.util.Vector;

import com.bbn.openmap.util.Debug;

/**
 * This class rummages through a file structure looking for RPF files. If there
 * isn't a RPF directory somewhere in the absolute path for a file, it's not
 * considered to be a RPF file.
 */
public class RpfFileSearch {

    /** The list of RPF frame files. */
    protected Vector<String> files = new Vector<String>();

    public RpfFileSearch() {
    }

    /** Construct and go, starting with the given directory pathname. */
    public RpfFileSearch(String startingDir) {
        handleEntry(startingDir);
    }

    /** Search, starting with the given directory pathname. */
    public boolean handleEntry(String startingDir) {
        File startingFile = null;

        // This line, if included, wipes out files received from
        // previous dirs. Yikes!
        // files.clear();

        if (startingDir != null) {
            startingFile = new File(startingDir);
            if (!startingFile.exists()) {
                startingFile = null;
                Debug.output("RpfFileSearch: " + startingDir
                        + " doesn't exist.");
                return false;
            }
        }

        if (startingFile == null) {
            return false;
        }

        boolean rpfDir = false;
        String start = startingFile.getAbsolutePath();
        if ((start.indexOf("RPF") != -1) || (start.indexOf("rpf") != -1)) {
            rpfDir = true;
        }

        if (Debug.debugging("maketoc")) {
            Debug.output("RpfFileSearch: Starting RPF file search from "
                    + startingDir + ", RPF directory "
                    + (rpfDir ? "found." : "not found."));
        }

        handleEntry(startingFile, rpfDir);
        return true;
    }

    /**
     * Search, given a file, plus a flag to let it know if the RPF directory is
     * somewhere above the file in the file sytem.
     */
    public void handleEntry(File file, boolean RPFDirFound) {
        try {

            String[] filenames = file.list();
            boolean dirTest = false;
            boolean not14 = false;

            try {
                java.lang.reflect.Method method = file.getClass()
                        .getDeclaredMethod("isDirectory", (Class[]) null);
                Object obj = method.invoke(file, (Object[]) null);
                if (obj instanceof Boolean) {
                    dirTest = ((Boolean) obj).booleanValue();
                }
            } catch (NoSuchMethodException nsme) {
                not14 = true;
            } catch (SecurityException se) {
                not14 = true;
            } catch (IllegalAccessException iae) {
                not14 = true;
            } catch (IllegalArgumentException iae2) {
                not14 = true;
            } catch (java.lang.reflect.InvocationTargetException ite) {
                not14 = true;
            }

            if ((dirTest || not14) && filenames != null) {
                if (Debug.debugging("maketocdetail")) {
                    Debug.output("RpfFileSearch.handleEntry(" + file + ", "
                            + RPFDirFound + "), file is a directory");
                }
                File[] contents = new File[filenames.length]; // file.listFiles();
                for (int i = 0; i < contents.length; i++) {
                    contents[i] = new File(file, filenames[i]);
                }

                for (int i = 0; i < contents.length; i++) {
                    boolean rpf = false;
                    if (!RPFDirFound) {
                        rpf = filenames[i].equalsIgnoreCase("RPF");
                    }
                    handleEntry(contents[i], RPFDirFound || rpf);
                }
            } else {
                if (Debug.debugging("maketocdetail")) {
                    Debug.output("RpfFileSearch.handleEntry(" + file + ", "
                            + RPFDirFound + "), adding to list...");
                }

                String parent = file.getParent();
                if (RPFDirFound) {
                    if (parent != null) {
                        files.add(file.getParent() + File.separator
                                + file.getName());
                    } else {
                        files.add("." + File.separator + file.getName());
                    }
                }
            }

        } catch (NullPointerException npe) {
        } catch (SecurityException se) {
        }
    }

    /**
     * Get the file list as a String[].
     */
    public String[] getFiles() {
        String[] fs = new String[files.size()];
        files.toArray(fs);
        return fs;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("RpfFileSearch:\n");
        for (int i = 0; i < files.size(); i++) {
            s.append("  file ").append(i).append(": ").append(files.elementAt(i)).append("\n");
        }
        return s.toString();
    }

    public static void main(String[] argv) {

        Debug.init();
        RpfFileSearch search = new RpfFileSearch();

        // Assume that the arguments are paths to directories or
        // files.
        for (int i = 0; i < argv.length; i++) {
            search.handleEntry(argv[i]);
        }

        System.out.println(search);
    }
}