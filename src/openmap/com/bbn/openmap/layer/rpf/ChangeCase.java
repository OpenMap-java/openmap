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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/ChangeCase.java,v $
// $RCSfile: ChangeCase.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

import java.io.File;

import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * ChangeCase is a simple class that traverses a file system tree and
 * converts the contents to upper ot lower case letters, depending on
 * the options provided.
 * 
 * <pre>
 * 
 *  
 *    Usage: java com.bbn.openmap.layer.rpf.ChangeCase [-u|-l] (dir path 1)(dir path 2) ...  
 *   
 *  
 * </pre>
 */
public class ChangeCase {

    /**
     * Given a file, change it's name, and everything below it (if it's
     * a directory) to the case specified by toUpper.
     * 
     * @param file file to start at.
     * @param toUpper file will change to upper case if true, lower
     *        case if false.
     */
    public static void handleEntry(File file, boolean toUpper) {
        try {
            String[] filenames = file.list();

            if (filenames != null) {
                File[] contents = new File[filenames.length]; // file.listFiles();
                for (int i = 0; i < contents.length; i++)
                    contents[i] = new File(file.getAbsolutePath()
                            + File.separator, filenames[i]);

                for (int i = 0; i < contents.length; i++) {
                    handleEntry(contents[i], toUpper);
                }
            }

            File newFile;
            String parent = file.getParent();

            if (parent != null) {
                if (toUpper) {
                    newFile = new File(parent, file.getName().toUpperCase());
                } else {
                    newFile = new File(parent, file.getName().toLowerCase());
                }
            } else {
                if (toUpper) {
                    newFile = new File(file.getName().toUpperCase());
                } else {
                    newFile = new File(file.getName().toLowerCase());
                }
            }

            if (file.renameTo(newFile)) {
                System.out.println("Renamed "
                        + (file.getParent() == null ? "." : file.getParent())
                        + File.separator
                        + file.getName()
                        + " to "
                        + (newFile.getParent() == null ? "."
                                : newFile.getParent()) + File.separator
                        + newFile.getName());
            } else {
                System.out.println("Renaming "
                        + (file.getParent() == null ? "." : file.getParent())
                        + File.separator
                        + file.getName()
                        + " to "
                        + (newFile.getParent() == null ? "."
                                : newFile.getParent()) + File.separator
                        + newFile.getName() + " FAILED");
            }

        } catch (NullPointerException npe) {
        } catch (SecurityException se) {
        }
    }

    /**
     * Given a set of files or directories, parade through them to
     * change their case.
     * 
     * @param argv paths to files or directories, use -h to get a
     *        usage statement.
     */
    public static void main(String[] argv) {
        Debug.init();
        boolean toUpper = true;

        ArgParser ap = new ArgParser("ChangeCase");
        ap.add("upper",
                "Change file and directory names to UPPER CASE (default). <path> <path> ...",
                ArgParser.TO_END);
        ap.add("lower",
                "Change file and directory names to lower case. <path> <path> ...",
                ArgParser.TO_END);

        if (argv.length == 0) {
            ap.bail("", true);
        }

        ap.parse(argv);

        String[] dirs;
        dirs = ap.getArgValues("lower");
        if (dirs != null) {
            Debug.output("Converting to lower case names...");
            toUpper = false;
        } else {
            dirs = ap.getArgValues("upper");
            // No arguments given, going to default.
            if (dirs == null) {
                dirs = argv;
            }
            Debug.output("Converting to UPPER CASE names...");
        }

        // Assume that the arguments are paths to directories or
        // files.
        for (int i = 0; i < dirs.length; i++) {
            handleEntry(new File(dirs[i]), toUpper);
        }
    }
}