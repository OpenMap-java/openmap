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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/wanderer/Wanderer.java,v $
// $RCSfile: Wanderer.java,v $
// $Revision: 1.8 $
// $Date: 2006/07/17 17:31:46 $
// $Author: mthome $
// 
// **********************************************************************

package com.bbn.openmap.util.wanderer;

import java.io.File;

import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * A Wanderer is a class that traverses a directory tree and finds files and
 * directories. It then makes a method call on the WandererCallback class to
 * have something done on those directories or files.
 */
public class Wanderer {

    WandererCallback callback = null;

    public Wanderer() {

    }

    public Wanderer(WandererCallback callback) {
        this();
        this.callback = callback;
    }

    public void setCallback(WandererCallback cb) {
        callback = cb;
    }

    public WandererCallback getCallback() {
        return callback;
    }

    /**
     * Given a file representing a top-level directory, start wandering the tree
     * and call handleDirectory or handleFile on the WandererCallback.
     * 
     * @param file File (directory) to start at.
     */
    public void handleEntry(File file) {
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

            if (not14) {
                dirTest = (filenames != null);
            }

            if (dirTest) {
                // It's a directory...
                handleDirectory(file, filenames);
                callback.handleDirectory(file);
            } else {
                callback.handleFile(file);
            }
        } catch (NullPointerException npe) {
        } catch (SecurityException se) {
        }
    }

    public void handleDirectory(File directory, String[] contentNames)
            throws SecurityException {

        File[] contents = new File[contentNames.length]; // file.listFiles();
        for (int i = 0; i < contents.length; i++)
            contents[i] = new File(directory.getAbsolutePath() + File.separator, contentNames[i]);

        for (int i = 0; i < contents.length; i++) {
            handleEntry(contents[i]);
        }
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

        ArgParser ap = new ArgParser("Wanderer");

        if (argv.length == 0) {
            ap.bail("", true);
        }

        String[] dirs = argv;

        Wanderer wanderer = new Wanderer(new TestWandererCallback());

        // Assume that the arguments are paths to directories or
        // files.
        for (int i = 0; i < dirs.length; i++) {
            wanderer.handleEntry(new File(dirs[i]));
        }
    }
}